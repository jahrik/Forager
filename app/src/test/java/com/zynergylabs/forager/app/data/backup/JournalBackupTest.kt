package com.zynergylabs.forager.app.data.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.BackupException
import com.zynergylabs.forager.app.domain.RestoreMode
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Journal backup and restore (dispatch 2026-09-28-127), through [RoomJournalBackup]'s public operations over
 * real Room databases in real files: the archive, Replace, Merge, refusal of a bad or newer backup, and that a
 * failure leaves the phone exactly as it was.
 *
 * What is compared is the data, not a proxy: every column of every row (`Phone.dump`), and every photo file's
 * bytes (`Phone.files`). A test that restores and then asserts "a row exists" would pass with half the columns
 * lost.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JournalBackupTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val phones = mutableListOf<Phone>()
    private var made = 0

    private fun phone(hooks: RoomJournalBackupHooks = RoomJournalBackupHooks()): Phone =
        Phone(context, "phone-${made++}.db", tmp.root, hooks).also { phones += it }

    @After
    fun closeAll() = phones.forEach { runCatching { it.close() } }

    private fun Phone.restore(bytes: ByteArray, mode: RestoreMode) = runBlocking { service.restore(ByteArrayInputStream(bytes), mode) }

    private fun assertRefused(result: Result<*>, why: String) {
        val e = result.exceptionOrNull()
        assertNotNull("$why: expected a failure, got $result", e)
        assertTrue("$why: expected a BackupException, got ${e!!.javaClass.simpleName}: ${e.message}", e is BackupException)
    }

    private fun assertUnchanged(p: Phone, before: Map<String, List<String>>, filesBefore: Map<String, String>) {
        assertEquals("the phone's rows are unchanged", before, p.dump())
        assertEquals("the phone's photo files are unchanged", filesBefore, p.files())
        assertEquals("no scratch file is left behind", emptyList<String>(), p.scratchLeftovers())
    }

    // ---- the archive ---------------------------------------------------------------------------

    @Test
    fun `a backup is a zip of the manifest first, the snapshot and the photos, and nothing else`() {
        val a = phone().apply { seedFullJournal() }
        val zip = readZip(a.backUp())

        assertEquals(listOf("manifest.json", "forager.db", "photos/p1.jpg", "photos/p2.jpg", "photos/p3.jpg", "photos/p4.jpg"), zip.keys.toList())
        val manifest = JSONObject(String(zip.getValue("manifest.json")))
        assertEquals(1, manifest.getInt("formatVersion"))
        assertEquals(42L, manifest.getLong("appVersionCode"))
        assertEquals(16, manifest.getInt("schemaVersion"))
        assertTrue("created time is stamped", manifest.getLong("createdAtEpochMillis") > 0)
        val files = manifest.getJSONArray("files")
        assertEquals(5, files.length())
        for (i in 0 until files.length()) {
            val f = files.getJSONObject(i)
            assertEquals("hash of ${f.getString("path")}", sha256(zip.getValue(f.getString("path"))), f.getString("sha256"))
            assertEquals("size of ${f.getString("path")}", zip.getValue(f.getString("path")).size.toLong(), f.getLong("bytes"))
        }
    }

    @Test
    fun `the snapshot inside a backup is a whole database that passes an integrity check and holds the phone's rows`() {
        val a = phone().apply { seedFullJournal() }
        val snapshot = tmp.newFile("snapshot.db").apply { writeBytes(readZip(a.backUp()).getValue("forager.db")) }

        val db = SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READONLY)
        db.use {
            assertEquals("ok", it.rawQuery("PRAGMA integrity_check", null).use { c -> c.moveToFirst(); c.getString(0) })
            assertEquals(16, it.version)
            assertEquals(a.count("track_points"), it.rawQuery("SELECT COUNT(*) FROM track_points", null).use { c -> c.moveToFirst(); c.getLong(0) })
        }
    }

    @Test
    fun `a photo row whose file is missing is left out of the archive, counted, and the backup still succeeds`() {
        val a = phone().apply {
            seedFullJournal()
            File(filesDir, "photos/p4.jpg").delete()
        }
        val out = java.io.ByteArrayOutputStream()
        val report = runBlocking { a.service.backUp(out) }.getOrThrow()

        assertEquals(3, report.photoFiles)
        assertEquals(1, report.photoFilesMissing)
        assertFalse(readZip(out.toByteArray()).containsKey("photos/p4.jpg"))
        assertTrue("the gap is logged, not swallowed", a.logged.any { "p4" in it })
    }

    @Test
    fun `the snapshot holds the write lock while it copies, so no writer can commit mid-copy`() {
        var writerFinishedDuringCopy: Boolean? = null
        val writerDone = AtomicBoolean(false)
        lateinit var a: Phone
        a = phone(
            RoomJournalBackupHooks(snapshotLockHeld = {
                thread {
                    a.insert("waypoints", "id" to "late")
                    writerDone.set(true)
                }
                Thread.sleep(600)
                writerFinishedDuringCopy = writerDone.get()
            }),
        )
        a.seedFullJournal()

        runBlocking { a.service.backUp(java.io.ByteArrayOutputStream()) }.getOrThrow()

        assertEquals("a writer started during the copy must still be waiting", false, writerFinishedDuringCopy)
        val deadline = System.currentTimeMillis() + 5_000
        while (!writerDone.get() && System.currentTimeMillis() < deadline) Thread.sleep(20)
        assertTrue("and gets through once the copy is done", writerDone.get())
    }

    // ---- Replace -------------------------------------------------------------------------------

    @Test
    fun `back up then Replace into an empty phone gives every journal table's rows and every photo file back, equal`() {
        val a = phone().apply { seedFullJournal(); insert("planned_trips", "id" to "trip-1") }
        for (t in JOURNAL_TABLE_NAMES) assertTrue("the seed fills $t, so equality below is not vacuous", a.count(t) > 0)
        val b = phone()

        val report = b.restore(a.backUp(), RestoreMode.REPLACE).getOrThrow()

        assertEquals(RestoreMode.REPLACE, report.mode)
        assertEquals(a.dump(JOURNAL_TABLE_NAMES), b.dump(JOURNAL_TABLE_NAMES))
        assertEquals("photo files, byte for byte", a.files(), b.files())
        assertEquals("planned trips are not journal data and are not restored", 0L, b.count("planned_trips"))
        assertEquals("no scratch file is left behind", emptyList<String>(), b.scratchLeftovers())
    }

    @Test
    fun `Replace deletes the phone's own journal data and the photo files no restored row uses`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone().apply {
            insert("waypoints", "id" to "own-w", "name" to "Phone's own")
            addPhoto("own-p", ByteArray(20) { 9 })
            insert("mushroom_log_entries", "id" to "own-f", "isDraft" to 0L)
            insert("planned_trips", "id" to "own-trip")
        }

        b.restore(a.backUp(), RestoreMode.REPLACE).getOrThrow()

        assertEquals(a.dump(JOURNAL_TABLE_NAMES), b.dump(JOURNAL_TABLE_NAMES))
        assertFalse("the phone's own photo file is gone", File(b.filesDir, "photos/own-p.jpg").exists())
        assertEquals(a.files(), b.files())
        assertEquals("its planned trip is not journal data and stays", 1L, b.count("planned_trips"))
    }

    @Test
    fun `Replace keeps a live photo file that shares a name with the backup's until the swap is done, and puts it back if the swap fails`() {
        val a = phone().apply { seedFullJournal() }
        var seenDuringSwap: ByteArray? = null
        lateinit var b: Phone
        b = phone(
            RoomJournalBackupHooks(afterPhotosCopied = {
                // Copied in, rows not yet written: the phone's own same-named file must still be recoverable.
                seenDuringSwap = File(b.filesDir, "photos").listFiles()!!.firstOrNull { it.name.startsWith("p1.jpg") && it.name != "p1.jpg" }?.readBytes()
                error("stop here")
            }),
        )
        b.apply {
            addPhoto("p1", ByteArray(10) { 5 })
            insert("waypoints", "id" to "own-w")
        }
        val rows = b.dump()
        val files = b.files()

        val result = b.restore(a.backUp(), RestoreMode.REPLACE)

        assertRefused(result, "a fault after the photos are copied")
        assertNotNull("the phone's own p1 was set aside, not overwritten in place", seenDuringSwap)
        assertUnchanged(b, rows, files)
    }

    @Test
    fun `a failure after the rows are written and before the commit leaves the phone's rows and files exactly as they were`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone(RoomJournalBackupHooks(afterRowsWritten = { error("power cut") })).apply {
            insert("waypoints", "id" to "own-w", "name" to "Phone's own")
            addPhoto("own-p", ByteArray(20) { 9 })
        }
        val rows = b.dump()
        val files = b.files()

        val result = b.restore(a.backUp(), RestoreMode.REPLACE)

        assertRefused(result, "a fault inside the transaction")
        assertUnchanged(b, rows, files)
    }

    // ---- Merge ---------------------------------------------------------------------------------

    @Test
    fun `Merge adds what the phone lacks, skips an id it already has and keeps the phone's copy, and leaves no row dangling`() {
        val a = phone().apply {
            seedFullJournal()
            // A second entry keeping a waypoint that A itself no longer has, and a photo that exists on both phones.
            insert("cartography_entries", "id" to "e2", "text" to "Another day", "isDraft" to 0L)
            insert("cartography_entry_track_refs", "entryId" to "e2", "trackId" to "t1")
            insert("cartography_entry_waypoint_refs", "entryId" to "e2", "waypointId" to "gone-w")
            insert("cartography_entry_photo_refs", "entryId" to "e2", "photoId" to "p1")
        }
        val b = phone().apply {
            insert("waypoints", "id" to "w1", "name" to "Device copy")
            insert("waypoints", "id" to "own-w", "name" to "Phone's own")
            addPhoto("p1", ByteArray(10) { 5 })
            insert("cartography_entries", "id" to "e1", "text" to "Device entry", "isDraft" to 0L)
            insert("cartography_entry_track_refs", "entryId" to "e1", "trackId" to "own-t", "name" to "device ref")
            insert("tracks", "id" to "own-t", "name" to "Own track")
            repeat(5) { insert("track_points", "trackId" to "own-t") }
        }
        val devicePhoto = File(b.filesDir, "photos/p1.jpg").readBytes()
        val backup = a.backUp()

        val report = b.restore(backup, RestoreMode.MERGE).getOrThrow()

        // The phone's copy wins, whole.
        assertEquals("Device copy", b.scalar("SELECT name FROM waypoints WHERE id='w1'"))
        assertEquals("Device entry", b.scalar("SELECT text FROM cartography_entries WHERE id='e1'"))
        assertEquals("only the phone's own ref row for e1: none of the backup's were added", "1", b.scalar("SELECT COUNT(*) FROM cartography_entry_track_refs WHERE entryId='e1'"))
        assertEquals("device ref", b.scalar("SELECT name FROM cartography_entry_track_refs WHERE entryId='e1'"))
        assertTrue("its own photo file is untouched", devicePhoto.contentEquals(File(b.filesDir, "photos/p1.jpg").readBytes()))
        assertEquals("the phone's own rows are all still there", "Phone's own", b.scalar("SELECT name FROM waypoints WHERE id='own-w'"))
        // What the phone lacked arrives.
        assertEquals("Big oak", b.scalar("SELECT name FROM waypoints WHERE id='w2'"))
        assertEquals("Morning loop", b.scalar("SELECT name FROM tracks WHERE id='t1'"))
        assertEquals("t1's points arrive", "3", b.scalar("SELECT COUNT(*) FROM track_points WHERE trackId='t1'"))
        assertTrue("with ids from this phone's own counter, not the backup's 1 to 3", b.scalar("SELECT MIN(id) FROM track_points WHERE trackId='t1'")!!.toLong() > 5)
        assertEquals("the region arrives", "Cedar Creek", b.scalar("SELECT name FROM offline_regions WHERE id=7"))
        assertEquals("e2 arrives", "Another day", b.scalar("SELECT text FROM cartography_entries WHERE id='e2'"))
        assertEquals("f1's photo p1 exists on the phone, so the link is kept", "1", b.scalar("SELECT COUNT(*) FROM log_entry_photos WHERE entryId='f1' AND photoId='p1'"))
        // Photo files: only for inserted rows.
        assertEquals(listOf("p1.jpg", "p2.jpg", "p3.jpg", "p4.jpg"), File(b.filesDir, "photos").list()!!.sorted())
        // No dangling rows anywhere, and the one that would have dangled is dropped and counted.
        assertEquals("gone-w had no record to point at", "0", b.scalar("SELECT COUNT(*) FROM cartography_entry_waypoint_refs WHERE entryId='e2'"))
        assertEquals(emptyList<String>(), danglingRows(b))
        assertEquals("the one dependent row that would have dangled was dropped and counted", 1, report.rowsDropped)
    }

    @Test
    fun `Merge sets a link to NULL when its target is on neither phone, the rule the app already applies on a delete`() {
        val a = phone().apply {
            insert("waypoints", "id" to "w-lonely", "name" to "Lonely", "trackId" to "no-such-track")
            insert("mushroom_log_entries", "id" to "f-lonely", "isDraft" to 0L, "offlineRegionId" to 99L)
        }
        val b = phone()

        b.restore(a.backUp(), RestoreMode.MERGE).getOrThrow()

        assertEquals("NULL", b.scalar("SELECT COALESCE(CAST(trackId AS TEXT), 'NULL') FROM waypoints WHERE id='w-lonely'"))
        assertEquals("NULL", b.scalar("SELECT COALESCE(CAST(offlineRegionId AS TEXT), 'NULL') FROM mushroom_log_entries WHERE id='f-lonely'"))
    }

    @Test
    fun `a Merge of a backup into the phone that made it changes nothing`() {
        val a = phone().apply { seedFullJournal() }
        val rows = a.dump()
        val files = a.files()

        val report = a.restore(a.backUp(), RestoreMode.MERGE).getOrThrow()

        assertEquals(0, report.rowsInserted)
        assertUnchanged(a, rows, files)
    }

    // ---- refusing a bad backup -----------------------------------------------------------------

    @Test
    fun `a truncated zip is refused and leaves the phone untouched`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone().apply { insert("waypoints", "id" to "own-w"); addPhoto("own-p", ByteArray(8) { 1 }) }
        val rows = b.dump(); val files = b.files()
        val backup = a.backUp()

        val result = b.restore(backup.copyOf(backup.size / 2), RestoreMode.REPLACE)

        assertRefused(result, "half a zip")
        assertUnchanged(b, rows, files)
    }

    @Test
    fun `a photo whose bytes do not match the manifest's hash is refused by name, and the phone is untouched`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone().apply { insert("waypoints", "id" to "own-w"); addPhoto("own-p", ByteArray(8) { 1 }) }
        val rows = b.dump(); val files = b.files()
        val entries = readZip(a.backUp())
        entries["photos/p2.jpg"] = entries.getValue("photos/p2.jpg").also { it[0] = (it[0] + 1).toByte() } // a valid zip, a changed photo

        val result = b.restore(writeZip(entries), RestoreMode.REPLACE)

        assertRefused(result, "a changed photo")
        assertTrue("the reason names the file and the hash: ${result.exceptionOrNull()?.message}", result.exceptionOrNull()!!.message!!.let { "photos/p2.jpg" in it && "hash" in it })
        assertUnchanged(b, rows, files)
    }

    @Test
    fun `a snapshot that fails its integrity check is refused, and so is a zip with no manifest`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone().apply { insert("waypoints", "id" to "own-w") }
        val rows = b.dump(); val files = b.files()
        val entries = readZip(a.backUp())

        val noManifest = LinkedHashMap(entries).also { it.remove("manifest.json") }
        assertRefused(b.restore(writeZip(noManifest), RestoreMode.REPLACE), "no manifest")

        val scrambled = LinkedHashMap(entries)
        val snapshot = scrambled.getValue("forager.db").copyOf()
        for (i in 4096 until minOf(snapshot.size, 4096 + 2048)) snapshot[i] = 0x55 // body of page 2 onward, header intact
        scrambled["forager.db"] = snapshot
        val manifest = JSONObject(String(scrambled.getValue("manifest.json")))
        manifest.getJSONArray("files").let { files ->
            for (i in 0 until files.length()) if (files.getJSONObject(i).getString("path") == "forager.db") {
                files.getJSONObject(i).put("sha256", sha256(snapshot))
            }
        }
        scrambled["manifest.json"] = manifest.toString().toByteArray()
        assertRefused(b.restore(writeZip(scrambled), RestoreMode.REPLACE), "a damaged snapshot with a matching hash")

        assertUnchanged(b, rows, files)
    }

    // ---- versions ------------------------------------------------------------------------------

    @Test
    fun `a backup newer than the app is refused, with the reason logged, and the phone is untouched`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone().apply { insert("waypoints", "id" to "own-w") }
        val rows = b.dump(); val files = b.files()
        val entries = readZip(a.backUp())
        val manifest = JSONObject(String(entries.getValue("manifest.json"))).put("schemaVersion", 17)
        entries["manifest.json"] = manifest.toString().toByteArray()

        val result = b.restore(writeZip(entries), RestoreMode.REPLACE)

        assertRefused(result, "a backup from a newer schema")
        assertTrue("the reason is logged: ${b.logged}", b.logged.any { "17" in it && "newer" in it })
        assertUnchanged(b, rows, files)
    }

    @Test
    fun `a backup whose format version this build does not know is refused`() {
        val a = phone().apply { seedFullJournal() }
        val b = phone()
        val entries = readZip(a.backUp())
        entries["manifest.json"] = JSONObject(String(entries.getValue("manifest.json"))).put("formatVersion", 2).toString().toByteArray()

        assertRefused(b.restore(writeZip(entries), RestoreMode.REPLACE), "an unknown format version")
        assertEquals(0L, b.count("waypoints"))
    }

    @Test
    fun `a backup from schema 15 restores through the registered migration, on a scratch copy`() {
        val helper = OlderBackup.helper()
        val old = OlderBackup.build(helper, tmp.root, version = 15)
        val b = phone().apply { insert("waypoints", "id" to "own-w", "name" to "Phone's own") }

        b.restore(old.archive, RestoreMode.REPLACE).getOrThrow()

        assertEquals("the migrated column exists and took its default", "0", b.scalar("SELECT shownOnMap FROM cartography_entries"))
        assertEquals("a value from the old snapshot survived the migration", "name-1", b.scalar("SELECT name FROM waypoints"))
        assertEquals("text-1", b.scalar("SELECT text FROM cartography_entries"))
        for (t in JOURNAL_TABLE_NAMES) assertEquals("one seeded row in $t", 1L, b.count(t))
        assertEquals("the scratch copy is gone", emptyList<String>(), b.scratchLeftovers())
    }

    @Test
    fun `an older backup's migration runs on a scratch copy and never on the live database`() {
        val helper = OlderBackup.helper()
        val old = OlderBackup.build(helper, tmp.root, version = 15)
        var liveVersionDuringRestore = -1
        lateinit var b: Phone
        b = phone(RoomJournalBackupHooks(afterPhotosCopied = { liveVersionDuringRestore = b.database.openHelper.readableDatabase.version }))

        b.restore(old.archive, RestoreMode.REPLACE).getOrThrow()

        assertEquals("the live database stayed at the app's own version throughout", 16, liveVersionDuringRestore)
    }

    // ---- what the table list is ----------------------------------------------------------------

    @Test
    fun `every table in the schema is either journal data or explicitly excluded, and the specs match the schema`() {
        val schemaTables = Phone.allTables()
        val journal = JournalTables.journal.map { it.name }
        assertEquals("the journal list, as ruling 3 B names it", JOURNAL_TABLE_NAMES.sorted(), journal.sorted())
        assertEquals("nothing is both, and nothing is neither", schemaTables.sorted(), (journal + JournalTables.excluded.keys).sorted())
        assertEquals(emptyList<String>(), journal.intersect(JournalTables.excluded.keys).toList())
        val pk = schemaPrimaryKeys(Phone.SCHEMA)
        for (spec in JournalTables.journal) assertEquals("${spec.name}'s key columns", pk.getValue(spec.name), spec.keyColumns)
        assertEquals("the schema version this build restores up to is the one the database declares", 16, com.zynergylabs.forager.app.data.local.ForagerDatabase.SCHEMA_VERSION)
        assertEquals(com.zynergylabs.forager.app.data.local.ForagerDatabase.SCHEMA_VERSION, phone().database.openHelper.readableDatabase.version)
    }

    // ---- helpers -------------------------------------------------------------------------------

    /** Every owned row's owner and named records, checked to exist: the rows a Merge must never leave dangling. */
    private fun danglingRows(p: Phone): List<String> {
        val out = mutableListOf<String>()
        for (spec in JournalTables.journal.filter { it.kind == JournalTables.Kind.OWNED }) {
            val refs = listOfNotNull(spec.owner) + spec.needs
            for (r in refs) {
                val n = p.scalar("SELECT COUNT(*) FROM `${spec.name}` WHERE `${r.column}` NOT IN (SELECT `${r.targetKey}` FROM `${r.table}`)")!!.toLong()
                if (n > 0) out += "${spec.name}.${r.column} -> ${r.table}: $n"
            }
        }
        return out
    }
}

internal val JOURNAL_TABLE_NAMES = listOf(
    "mushroom_log_entries", "log_photos", "log_entry_photos", "tracks", "track_points", "waypoints", "offline_regions",
    "cartography_entries", "cartography_entry_track_refs", "cartography_entry_waypoint_refs",
    "cartography_entry_offline_region_refs", "cartography_entry_find_refs", "cartography_entry_photo_refs",
)
