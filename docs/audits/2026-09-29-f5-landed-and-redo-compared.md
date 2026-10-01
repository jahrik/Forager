# F5, the landed build and the redo compared (planner, 2026-09-29)

**Asked for.** The owner, verbatim: "Run a diff on the old F5 and new F5 and document the procedural differences into the audit folder."

**What is compared.** Two builds of the same dispatch, F5 (2026-09-28-216): backups without recent searches, and GPX exports cleaned from the cache. The dispatch is `prompts/preserved/2026-09-29-48.md`, and the launch prompt is `prompts/preserved/2026-09-29-50.md`. Both coders worked from those files.

| | Landed F5 | Redo |
|---|---|---|
| Its own commits | `5cffbb10` pre-registration and tests; `dd93caf5` tests-first results and M1; `981bff8d` build; `770fcf6f` report | `76b71b41` pre-registration; `eea15726` tests first; `9b54e5a2` build; `29d009ae` VACUUM dropped; `426786f5` report |
| Where it is | Branch `privacy-fixes`, no longer on the remote. Merged into `journal-redesign` by `947b2d9d`, `e954208b` and `2fa093ef` | Its own branch, head `426786f5`. Not merged anywhere |
| Base | `8f260834`. Its app code is the launch base's: `git diff 445b292b 8f260834 -- app` is empty | `445b292b`, the launch prompt's base |
| Report | `docs/audits/2026-09-29-privacy-fixes-completion-report.md`, the dispatch's name | A report in `docs/audits/` at `426786f5`, under another name, since the landed F5 holds the dispatch's |
| Commit times (PDT) | 13:05 to 13:19 | 13:27 to 13:46 |
| Attribution | The Co-Authored-By trailer on its four commits names the model its session ran on. That is not the model the launch prompt sets | The trailer on its five commits names the model the launch prompt sets |
| Why it exists | The dispatch | The owner's instruction, as the redo reports it: redo F5 on a new branch so the attribution is true |

**How it was compared:**
- **The code:** `git diff 981bff8d 426786f5 -- app`. Both builds start from the same app code. `981bff8d` is the landed build before its merges brought in other sessions' work, so the diff holds only the two F5s' differences.
- **The reports and prompts:** both completion reports, the dispatch and the launch prompt, read in full.
- **D58:** this planner's check was run over each build's own commits, diffs and messages.

Line numbers are at `981bff8d` for the landed build and at `426786f5` for the redo. `K/` is `app/src/main/java/com/zynergylabs/forager/app/`.

## 1. What a user gets: the same

Both builds do what the dispatch asks, in the same way as far as a user can see:
- **Searches out of backups:** a backup empties `cached_searches` on its snapshot copy, straight after the snapshot is taken. The live database is never written. Restore is unchanged, because it never read that table.
- **GPX cleanup:** `.gpx` files in `cacheDir/tracks` last written more than one hour ago are deleted first thing in `write`, and at app start, off the main thread.
- **Nothing sharper than one hour:** both found no share-completion signal (the chooser at `TrackExportPanel.kt:266` returns nothing), so both built the hour.

## 2. Procedure, rule by rule

| Rule (source) | Landed F5 | Redo |
|---|---|---|
| Confirm the model (launch prompt, "First") | Reported as unconfirmed, in the hand-back only. The report keeps model identifiers out of repository files, citing the environment's rule | The report's title, header and body name the model (11 lines), and so do its branch and report names. It says the model is the session's configured one, "not independently confirmed" |
| Read `CLAUDE.md` at the base (launch prompt) | Recorded: no conflict, and the line the prompt disapplies is not there (`grep -c` gives 0) | Not mentioned in the report |
| Quote the dispatch files and every planner message verbatim (launch prompt) | Both files appended in full. The report states that no planner message reached the session | Quotes the owner's "2 A / 3 A", the owner's instruction as relayed, and the planner's cancel message. The dispatch files are not quoted |
| Worktree and branch `privacy-fixes` (dispatch) | Yes | A new worktree and branch, on the owner's instruction as it reports it |
| Base `445b292b` (launch prompt) | Cut at `8f260834`. The report shows the app code is identical | `445b292b` |
| Verify every premise (launch prompt) | A premise table of nine rows, each with file:line and a result | Premises in prose, with file:line |
| The wrong premise: restore already never writes `cached_searches` | Found, and flagged as a wrong premise. The two restore tests became guards, and a mutation at the base (M1, making the table journal data) showed before any build that they can fail | Found. The two restore tests were predicted as guards, and revert checks after the build (R3a, R3b: Replace or Merge copies the table) showed they can fail |
| Pre-register, and push it before building (launch prompt) | Pre-registration and the six tests in one commit, `5cffbb10`, pushed to `privacy-fixes-wip` before any build | Pre-registration alone (`76b71b41`), then the tests (`eea15726`) |
| Tests fail first, for the stated reason (launch prompt) | 71 run, 4 failed, each on its predicted message; 0 compile errors; 4 XML files, none stale | 51 run, 4 failed, each on its predicted message; no compile errors |
| Revert checks from saved copies (launch prompt) | Six (R1 to R6) plus M1, each predicted in advance. Each file was restored byte-identical to its saved copy with the forward change present, and `git status` was clean afterwards. Includes two threshold mutations (any age; two hours) and clearing the live database instead of the copy | Six (R1 twice, R2, R3a, R3b, R4, R5), restored and compared with `cmp` and `git diff`. No threshold mutation, and no live-database check |
| A failed prediction | None | R2: removing the VACUUM was predicted to fail the byte test, and nothing failed. The report records it as a finding |
| "Nothing else changes"; no design decisions (dispatch, launch prompt) | Built only what the dispatch names. Leftover bytes in the file were left as an observation, with options (a) to (c) for the owner | Its build commit `9b54e5a2` added a `VACUUM` the dispatch does not name. It was removed in `29d009ae` once R2's prediction failed |
| The full suite, from a cleared results directory (launch prompt) | 3223 / 0 / 0 / 24 in 395 XML files, none stale, in one run, on the build merged with the then-current `journal-redesign`. That matches its prediction exactly. The planner repeated it at `06b394b9` (-233) | 3145 / 0 / 0 / 24 in 379 XML files, on the second run. The first run had one failure, in `LeavingTheJournalFixesTest`, which then passed three times alone and on the rerun. The count is at the older base, without the work that landed on `journal-redesign` since |
| Push to `journal-redesign`, broken work to `privacy-fixes-wip` (dispatch, launch prompt) | Yes | Its own branch only |
| The report at the dispatch's path (dispatch) | Yes | Another name |
| Device-only list (dispatch) | Eight steps, cheapest first, each with a pass condition and the evidence to keep, two marked as observations | Three items, without pass conditions or evidence |
| Not tested | Seven items | Three items |
| D58 (launch prompt) | Clean over its own commits, and its report states the check | Clean over its own commits. Its report does not mention the check |
| Hand-back | To the previous planner (-232) | To this planner, after the role moved |

## 3. The code, where the two differ

| Area | Landed F5 | Redo |
|---|---|---|
| What the backup clears | Every table `JournalTables.excluded` names, today `cached_searches` alone, in one transaction (`K/data/backup/RoomJournalBackup.kt:110`, `:186-196`) | `cached_searches` by name, in one statement, with no transaction (`RoomJournalBackup.kt:109`, `:184-187`) |
| Documentation of the clearing | The class doc and the `JournalTables.excluded` doc (`JournalTables.kt:95`) both say a backup leaves these rows out and a restore never reads them | Neither is updated. The function's doc (`RoomJournalBackup.kt:180-181`) states that Android's SQLite defaults `secure_delete` on. In the same sentence it says this was probed only under Robolectric, not on a device. The landed F5 treats the same question as open (device item 3) |
| A GPX file that cannot be deleted | `Files.deleteIfExists`, so a failure comes with its reason. Each failure is logged through an injected `ErrorLog` (`K/export/TrackGpxExporter.kt:36`, `:88-90`), on the export path and at start alike | `File.delete()`, which gives no reason. It returns `StaleSweep(found, deleted)` (`TrackGpxExporter.kt:71-78`). App start logs a shortfall (`K/ForagerApplication.kt:116`), but `write` discards the result (`TrackGpxExporter.kt:47`). **So a failed delete during an export is not logged.** That runs against `CLAUDE.md`, "Errors and failure paths": no fallback that is not logged when it fires |
| The age constant | `private const val STALE_EXPORT_AGE_MILLIS` (`TrackGpxExporter.kt:108`) | `const val MAX_EXPORT_AGE_MILLIS`, public (`TrackGpxExporter.kt:89`) |
| The constructor | Gains a defaulted `errorLog` parameter, so no existing call changes | Unchanged |

## 4. The tests, where the two differ

| Case | Landed F5 (6 tests) | Redo (7 tests) |
|---|---|---|
| A backup holds no search rows, and the phone keeps its searches | T1 | Test 1 |
| No search text left in the snapshot's bytes | An observation only, from a throwaway test that was never committed. Search bytes were absent under Robolectric, with `secure_delete` = 1 | A committed test (test 2). Its own report shows it cannot tell `DELETE` alone from `DELETE` plus `VACUUM` under Robolectric (R2) |
| Replace and Merge of an older backup leave the phone's searches | T2, T3, as guards | Tests 3 and 4, as guards, over schema 16 and 17 backups |
| `write` deletes a 61-minute export and keeps a 59-minute one | T4 | Test 5 |
| A Share through the real UI deletes a stale export first | T5, a real touch on the row's Share (`RecordDetailsSheetTest`) | None |
| A non-`.gpx` file in the folder is left alone | None. Listed under "Not tested" | Test 6 |
| App start deletes a stale export | T6 (`ForagerApplicationGpxCacheTest`, a second `onCreate`) | Test 7 (its own start test) |

## 5. For the owner's choice

Both builds give users the same behaviour. They cannot both be merged, because they change the same code. The choice between them is the owner's. The facts that bear on it, from the sections above:
- **The landed F5:**
  - it is on `journal-redesign` with the planner's suite at the current base, and it is Part 3's base;
  - it logs a failed delete on both paths;
  - it has a real-UI test of the Share path;
  - its report documents each rule the launch prompt sets;
  - its trailer is not the model the launch prompt sets.
- **The redo:**
  - its trailer is the model the launch prompt sets;
  - it adds a byte test and a non-`.gpx` guard;
  - a failed delete during an export goes unlogged;
  - it clears without a transaction;
  - it states an unverified platform default in a code comment;
  - it sits at the older base. Taking it would need the landed code removed, a merge onto the current `journal-redesign`, the suite rerun, and Part 3's base moved.

## The owner's decision

The owner, verbatim, with the model identifiers replaced as in Appendix B: "Discard the redo and keep the findings in the audit file in PR 140, we are measuring [model B] vs [model A]."

- **The landed F5 stays** on `journal-redesign`.
- **The redo is not used.** ~~Its branch (head `426786f5`) is deleted from the remote, and its worktree is removed once found clean.~~ **Corrected by the owner, before anything was deleted**, verbatim: "We are keeping the F5 that already landed / Keep the branch / Do not delete", then "Don't delete it. Just don't use it". The branch and its worktree stay as they are. They are not merged, not built on and not deleted.
- **Its findings stay here.** The redo's code and its report are kept below, so this file stands on its own:
  - **Appendix A:** the redo's full diff against its base.
  - **Appendix B:** its completion report, word for word.
- **The measurement.** The two builds are the owner's comparison of two models on one dispatch.
  - **Build A** is the landed F5. It ran on model A, which its commits' Co-Authored-By trailers name.
  - **Build B** is the redo. It ran on model B, the model the launch prompt sets (`prompts/preserved/2026-09-29-50.md`, "First, confirm the model").
  - Sections 2 to 4 above are the measurement.

## Appendix A. The redo's code, `git diff 445b292b 426786f5 -- app`

~~~~diff
diff --git a/app/src/main/java/com/zynergylabs/forager/app/ForagerApplication.kt b/app/src/main/java/com/zynergylabs/forager/app/ForagerApplication.kt
index bf426909..c87f44b2 100644
--- a/app/src/main/java/com/zynergylabs/forager/app/ForagerApplication.kt
+++ b/app/src/main/java/com/zynergylabs/forager/app/ForagerApplication.kt
@@ -7,6 +7,7 @@ import com.zynergylabs.forager.app.crash.CrashUncaughtExceptionHandler
 import com.zynergylabs.forager.app.data.backup.ScheduledBackupDependencies
 import com.zynergylabs.forager.app.data.backup.ScheduledBackupDependenciesProvider
 import com.zynergylabs.forager.app.domain.ErrorLog
+import com.zynergylabs.forager.app.export.TrackGpxExporter
 import com.zynergylabs.forager.app.domain.RunScheduledBackupUseCase
 import com.zynergylabs.forager.app.domain.ScheduledBackupReporter
 import com.zynergylabs.forager.app.diagnostics.DebugDiagnostics
@@ -53,6 +54,7 @@ class ForagerApplication : Application(), ScheduledBackupDependenciesProvider {
         installCrashHandler()
         initializeMapLibreAtStart()
         sweepOrphanedCaptures(startedAt)
+        deleteStaleGpxExports()
     }
 
     /**
@@ -103,6 +105,18 @@ class ForagerApplication : Application(), ScheduledBackupDependenciesProvider {
         }
     }
 
+    /**
+     * [TrackGpxExporter.deleteStaleExports] at start (F5, dispatch 2026-09-28-216; owner, "3 A"), so an
+     * export the phone never shared again does not sit in the cache. Off the main thread, like the capture sweep.
+     */
+    private fun deleteStaleGpxExports() {
+        applicationScope.launch {
+            val sweep = TrackGpxExporter.forContext(this@ForagerApplication).deleteStaleExports()
+            if (sweep.deleted > 0) Log.i(TAG, "Deleted ${sweep.deleted} exported GPX file(s) more than an hour old.")
+            if (sweep.found > sweep.deleted) Log.w(TAG, "${sweep.found - sweep.deleted} exported GPX file(s) more than an hour old could not be deleted.")
+        }
+    }
+
     private companion object {
         const val TAG = "ForagerApplication"
     }
diff --git a/app/src/main/java/com/zynergylabs/forager/app/data/backup/RoomJournalBackup.kt b/app/src/main/java/com/zynergylabs/forager/app/data/backup/RoomJournalBackup.kt
index 9c1353cf..743aa61b 100644
--- a/app/src/main/java/com/zynergylabs/forager/app/data/backup/RoomJournalBackup.kt
+++ b/app/src/main/java/com/zynergylabs/forager/app/data/backup/RoomJournalBackup.kt
@@ -106,6 +106,7 @@ class RoomJournalBackup(
         try {
             val snapshot = File(scratch, BackupManifest.DATABASE_ENTRY)
             takeSnapshot(snapshot)
+            clearSearchesFromSnapshot(snapshot)
             val (schemaVersion, photoPaths) = SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                 requireIntegrity(db, "the snapshot")
                 val paths = db.rawQuery("SELECT relativePath FROM log_photos", null).use { c ->
@@ -173,6 +174,19 @@ class RoomJournalBackup(
         File(target.path + "-shm").delete()
     }
 
+    /**
+     * A backup holds no `cached_searches` rows (F5, dispatch 2026-09-28-216; owner, "2 A. Leave searches out of
+     * backups"): each carries the coordinates it was run for. Cleared in the snapshot **copy** only, never on the
+     * live database. No `VACUUM`: Android's SQLite defaults `secure_delete` on, which zeroes deleted content in
+     * place (probed under Robolectric, where `PRAGMA secure_delete` reads 1; not probed on a device), and a
+     * `VACUUM` would rewrite the whole snapshot for nothing.
+     */
+    private fun clearSearchesFromSnapshot(snapshot: File) {
+        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
+            db.execSQL("DELETE FROM cached_searches")
+        }
+    }
+
     // ---- restore -------------------------------------------------------------------------------
 
     private fun doRestore(source: InputStream, mode: RestoreMode): RestoreReport {
diff --git a/app/src/main/java/com/zynergylabs/forager/app/export/TrackGpxExporter.kt b/app/src/main/java/com/zynergylabs/forager/app/export/TrackGpxExporter.kt
index 4b97165d..99a16507 100644
--- a/app/src/main/java/com/zynergylabs/forager/app/export/TrackGpxExporter.kt
+++ b/app/src/main/java/com/zynergylabs/forager/app/export/TrackGpxExporter.kt
@@ -44,6 +44,7 @@ class TrackGpxExporter(private val exportDir: File) {
      * own points do not name.
      */
     fun write(track: Track, fullRecord: List<TrackPointRecord>, waypoints: List<Waypoint>): File {
+        deleteStaleExports()
         exportDir.mkdirs()
         val file = File(exportDir, fileNameFor(track))
         file.writeText(
@@ -59,6 +60,23 @@ class TrackGpxExporter(private val exportDir: File) {
         return file
     }
 
+    /**
+     * Deletes the `.gpx` files in the export directory that were last written more than [MAX_EXPORT_AGE_MILLIS]
+     * ago, and returns what it found and deleted (F5, dispatch 2026-09-28-216; owner, "3 A"). Called before each new export
+     * and at app start. An hour, not the moment the share sheet closes: the receiving app may still be reading
+     * the shared file then, and the code has no completion signal from the share (`startActivity` of a chooser
+     * returns nothing). A file that cannot be deleted shows as [StaleSweep.found] above
+     * [StaleSweep.deleted] for the caller to log; the next call tries again.
+     */
+    fun deleteStaleExports(): StaleSweep {
+        val cutoff = System.currentTimeMillis() - MAX_EXPORT_AGE_MILLIS
+        val stale = exportDir.listFiles { f -> f.isFile && f.extension == "gpx" && f.lastModified() < cutoff } ?: return StaleSweep(found = 0, deleted = 0)
+        return StaleSweep(found = stale.size, deleted = stale.count { it.delete() })
+    }
+
+    /** What a sweep found and what it managed to delete; [found] above [deleted] means a file could not be removed. */
+    data class StaleSweep(val found: Int, val deleted: Int)
+
     private fun fileNameFor(track: Track): String {
         val timestamp = FILE_NAME_FORMAT.format(
             Instant.ofEpochMilli(track.startedAtEpochMillis).atZone(ZoneId.systemDefault()),
@@ -67,6 +85,9 @@ class TrackGpxExporter(private val exportDir: File) {
     }
 
     companion object {
+        /** How long an exported file stays in the cache: long enough for the app it was shared to to finish reading it. */
+        const val MAX_EXPORT_AGE_MILLIS = 60L * 60L * 1000L
+
         private val FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")
 
         /**
diff --git a/app/src/test/java/com/zynergylabs/forager/app/ForagerApplicationGpxStartTest.kt b/app/src/test/java/com/zynergylabs/forager/app/ForagerApplicationGpxStartTest.kt
new file mode 100644
index 00000000..d7d5cf25
--- /dev/null
+++ b/app/src/test/java/com/zynergylabs/forager/app/ForagerApplicationGpxStartTest.kt
@@ -0,0 +1,35 @@
+package com.zynergylabs.forager.app
+
+import androidx.test.core.app.ApplicationProvider
+import java.io.File
+import org.junit.Assert.assertFalse
+import org.junit.Assert.assertTrue
+import org.junit.Test
+import org.junit.runner.RunWith
+import org.robolectric.RobolectricTestRunner
+import org.robolectric.annotation.Config
+
+/**
+ * Exported GPX files older than an hour leave the cache at app start (F5, dispatch 2026-09-28-216, owner "3 A"),
+ * through the real entry point: the application's own `onCreate`. Robolectric already ran it once before the
+ * test body, so the test lays its files down and runs it again, as a new process start would.
+ */
+@RunWith(RobolectricTestRunner::class)
+@Config(sdk = [36])
+class ForagerApplicationGpxStartTest {
+
+    @Test
+    fun `startup removes an export older than an hour from the cache and keeps a recent one`() {
+        val app = ApplicationProvider.getApplicationContext<ForagerApplication>()
+        val dir = File(app.cacheDir, "tracks").apply { mkdirs() }
+        val stale = File(dir, "forager-track-old.gpx").apply { writeText("<gpx/>"); check(setLastModified(System.currentTimeMillis() - 61 * 60_000L)) }
+        val fresh = File(dir, "forager-track-recent.gpx").apply { writeText("<gpx/>"); check(setLastModified(System.currentTimeMillis() - 59 * 60_000L)) }
+
+        app.onCreate()
+
+        val deadline = System.currentTimeMillis() + 5_000
+        while (stale.exists() && System.currentTimeMillis() < deadline) Thread.sleep(20)
+        assertFalse("an export 61 minutes old is still in the cache after startup", stale.exists())
+        assertTrue("an export 59 minutes old was deleted at startup", fresh.exists())
+    }
+}
diff --git a/app/src/test/java/com/zynergylabs/forager/app/data/backup/JournalBackupTest.kt b/app/src/test/java/com/zynergylabs/forager/app/data/backup/JournalBackupTest.kt
index e77e5e81..9828e4c3 100644
--- a/app/src/test/java/com/zynergylabs/forager/app/data/backup/JournalBackupTest.kt
+++ b/app/src/test/java/com/zynergylabs/forager/app/data/backup/JournalBackupTest.kt
@@ -733,6 +733,63 @@ class JournalBackupTest {
         assertEquals("the scratch copy is gone", emptyList<String>(), b.scratchLeftovers())
     }
 
+    // ---- recent searches stay out of backups (F5, dispatch 2026-09-28-216, owner "2 A") ---------
+
+    private fun Phone.seedSearches() {
+        insert("cached_searches", "key" to "s1", "entriesJson" to "SEARCH_TEXT_ONE_9f3a", "lat" to 45.123456, "lng" to -122.654321)
+        insert("cached_searches", "key" to "s2", "entriesJson" to "SEARCH_TEXT_TWO_9f3a", "lat" to 46.5, "lng" to -121.5)
+    }
+
+    @Test
+    fun `a backup made with searches on the phone holds no cached_searches rows, and the phone keeps its searches`() {
+        val a = phone().apply { seedFullJournal(); seedSearches() }
+        val searchesBefore = a.dump(listOf("cached_searches"))
+        assertEquals("the seed put two searches on the phone", 2L, a.count("cached_searches"))
+        val snapshot = tmp.newFile("snapshot.db").apply { writeBytes(readZip(a.backUp()).getValue("forager.db")) }
+
+        SQLiteDatabase.openDatabase(snapshot.path, null, SQLiteDatabase.OPEN_READONLY).use {
+            assertEquals("cached_searches rows in the backup's snapshot", 0L, it.rawQuery("SELECT COUNT(*) FROM cached_searches", null).use { c -> c.moveToFirst(); c.getLong(0) })
+            assertTrue("the rest of the journal is still in it", it.rawQuery("SELECT COUNT(*) FROM waypoints", null).use { c -> c.moveToFirst(); c.getLong(0) } > 0)
+        }
+        assertEquals("the live database still has its searches, whole", searchesBefore, a.dump(listOf("cached_searches")))
+    }
+
+    @Test
+    fun `no trace of a search's text or coordinates is left in the backup's snapshot file`() {
+        val a = phone().apply { seedFullJournal(); seedSearches() }
+        val snapshot = readZip(a.backUp()).getValue("forager.db")
+
+        // The rows being gone is not the claim: the text they held must not survive in the file's free pages either.
+        val text = String(snapshot, Charsets.ISO_8859_1)
+        assertFalse("search text found in the snapshot file", "SEARCH_TEXT_ONE_9f3a" in text || "SEARCH_TEXT_TWO_9f3a" in text)
+    }
+
+    @Test
+    fun `Replace of a backup that holds searches leaves the phone's own searches untouched and adds none of the backup's`() {
+        for (version in listOf(16, 17)) {
+            val old = OlderBackup.build(OlderBackup.helper(), tmp.newFolder(), version)
+            val b = phone().apply { seedSearches() }
+            val searches = b.dump(listOf("cached_searches"))
+
+            b.restore(old.archive, RestoreMode.REPLACE).getOrThrow()
+
+            assertEquals("schema $version backup, Replace: the phone's searches", searches, b.dump(listOf("cached_searches")))
+        }
+    }
+
+    @Test
+    fun `Merge of a backup that holds searches leaves the phone's own searches untouched and adds none of the backup's`() {
+        for (version in listOf(16, 17)) {
+            val old = OlderBackup.build(OlderBackup.helper(), tmp.newFolder(), version)
+            val b = phone().apply { seedSearches() }
+            val searches = b.dump(listOf("cached_searches"))
+
+            b.restore(old.archive, RestoreMode.MERGE).getOrThrow()
+
+            assertEquals("schema $version backup, Merge: the phone's searches", searches, b.dump(listOf("cached_searches")))
+        }
+    }
+
     // ---- versions ------------------------------------------------------------------------------
 
     @Test
diff --git a/app/src/test/java/com/zynergylabs/forager/app/export/TrackGpxExporterTest.kt b/app/src/test/java/com/zynergylabs/forager/app/export/TrackGpxExporterTest.kt
index 29d794c3..a4572582 100644
--- a/app/src/test/java/com/zynergylabs/forager/app/export/TrackGpxExporterTest.kt
+++ b/app/src/test/java/com/zynergylabs/forager/app/export/TrackGpxExporterTest.kt
@@ -9,7 +9,9 @@ import com.zynergylabs.forager.app.domain.model.Waypoint
 import java.time.Instant
 import java.time.ZoneId
 import java.time.format.DateTimeFormatter
+import java.io.File
 import org.junit.Assert.assertEquals
+import org.junit.Assert.assertFalse
 import org.junit.Assert.assertTrue
 import org.junit.Rule
 import org.junit.Test
@@ -121,4 +123,31 @@ class TrackGpxExporterTest {
         )
         assertTrue("the waypoint's own id must reach the file", file.readText().contains("id=\"w1\""))
     }
+
+    // ---- exports leave the cache after an hour (F5, dispatch 2026-09-28-216, owner "3 A") ------
+
+    private fun File.agedMinutes(minutes: Long): File = apply { check(setLastModified(System.currentTimeMillis() - minutes * 60_000L)) }
+
+    @Test
+    fun `write first removes an export older than an hour and keeps one that is not`() {
+        val dir = tempFolder.newFolder("tracks")
+        val stale = File(dir, "forager-track-old.gpx").apply { writeText("<gpx/>") }.agedMinutes(61)
+        val fresh = File(dir, "forager-track-recent.gpx").apply { writeText("<gpx/>") }.agedMinutes(59)
+
+        val written = TrackGpxExporter(dir).write(track, fullRecord = fullRecord, waypoints = emptyList())
+
+        assertFalse("an export 61 minutes old is still in the cache", stale.exists())
+        assertTrue("an export 59 minutes old was deleted", fresh.exists())
+        assertTrue("the new export is there", written.exists())
+    }
+
+    @Test
+    fun `write leaves an old file that is not a gpx export alone`() {
+        val dir = tempFolder.newFolder("tracks")
+        val other = File(dir, "notes.txt").apply { writeText("keep") }.agedMinutes(600)
+
+        TrackGpxExporter(dir).write(track, fullRecord = fullRecord, waypoints = emptyList())
+
+        assertTrue("a file that is not a .gpx export was deleted", other.exists())
+    }
 }
~~~~

## Appendix B. The redo's completion report, at `426786f5`

The report's own path was `docs/audits/` on the redo branch. **Redacted:** 15 model identifiers are replaced, because model identifiers are kept out of repository files in this environment. `[model A]` stands for the landed build's model and `[model B]` for the model the launch prompt sets, including where they appear inside branch names. Nothing else is changed.

~~~~markdown
# F5 redone on [model B]: backups without recent searches, GPX cache cleaned (dispatch 2026-09-28-216)

**Coder session, model `[model B]`** (the session was configured with it; the serving model was not independently confirmed).

## Why this exists

F5 already landed on `journal-redesign` (`981bff8d`, report `770fcf6f`, planner suite 3223/0/0/24). It was built on [model A]. The owner asked
for it to be redone on [model B], on a new branch, so the attribution is true. The owner's words, as relayed in this session: "F5 is stated
to be done by [model B] but [model A] was used on last run. So we have to redo it on [model B] to make it true. Start a new branch".

The planner's message in the same session, sent before it knew of the owner's instruction: "F5 is done ... cancel this dispatch as done. Build
nothing and push nothing." The owner's instruction was followed; the planner was told.

- **Dispatch (governs):** `prompts/preserved/2026-09-29-48.md` at `445b292b`, "2 A / 3 A".
- **Base:** `445b292b`, which has no F5 (verified: `git grep cached_searches` in `data/backup/` finds only `JournalTables.kt:96` and the no-op at `RoomJournalBackup.kt:162`).
- **Branch / worktree:** `privacy-fixes-redo-[model B]`, `/home/zynergy-labs/Zynergy/forager-wt/privacy-fixes-redo-[model B]`. The dispatch names `privacy-fixes`, which already exists with the landed F5.
- **Push target:** `origin/privacy-fixes-redo-[model B]`, not `journal-redesign` (the dispatch says journal-redesign; the owner said new branch, and pushing a second F5 onto the branch that has one would conflict).

## Pre-registration (written and pushed before any test or fix)

Reading, at base `445b292b`:
- `RoomJournalBackup.kt:104-145` `doBackUp` takes the snapshot (`:108`), reads it read-only (`:109`), zips it (`:140`). `:149-174` `takeSnapshot`: copy under the write lock, then fold the copy (`:168-171`). `:162` is `DELETE FROM cached_searches WHERE 0`, a no-op that only takes the write lock.
- Restore (`:178-200`) reads only `JournalTables.journal` specs (`:239`, `:250`, `:321`, `:346`, `:362`); `cached_searches` is in `JournalTables.excluded` (`JournalTables.kt:95-97`), so no restore path writes it.
- `TrackGpxExporter.kt:46-60` `write`; `:78` `forContext` puts files in `cacheDir/tracks`; nothing deletes them. The only caller is `TrackExportPanel.kt:264`, which then calls `startActivity(Intent.createChooser(...))` at `:266`, with no result callback, so the code shows no completion signal sharper than the planner's hour. The dispatch's "stop if a sharper safe signal" does not trigger.
- `ForagerApplication.kt:50-56` `onCreate`; the capture sweep at `:93-` is the pattern for a background start task.

Predictions (each test, at base):

| # | Test | Predicted at base | Reason |
|---|------|-------------------|--------|
| 1 | backup with searches: the snapshot in the zip has 0 `cached_searches` rows, and the phone keeps its 2 | **FAIL** | snapshot holds 2 rows; the message names 2 vs 0 |
| 2 | backup with searches: no trace of a search's text is left in the snapshot bytes | **FAIL** | text is in the snapshot file. Also fails with DELETE alone (free pages keep the text), so it is what makes VACUUM necessary |
| 3 | Replace of a backup that has searches (schema 16 and 17) leaves the phone's own searches equal | **PASS** | restore never reads `cached_searches` (`:239-362`). A guard: passing at base is predicted, not a surprise |
| 4 | Merge of the same, likewise | **PASS** | same |
| 5 | `write` removes an export older than an hour and keeps one 59 minutes old | **FAIL** | the stale file is still there |
| 6 | `write` leaves an old non-`.gpx` file alone | **PASS** | nothing deletes anything at base; guards the scope of the deletion |
| 7 | app start removes a stale export and keeps a fresh one | **FAIL** | the stale file is still there after 5 s |

Pass conditions after the fix: 1, 2, 5, 7 pass; 3, 4, 6 stay passing; the full suite has 0 failures, counted from the JUnit XML in a cleared results directory.

Revert checks planned, each from a copy saved before editing:
- remove the `DELETE` in the snapshot: tests 1 and 2 fail;
- remove only the `VACUUM`: test 2 fails, test 1 passes;
- make Replace/Merge copy `cached_searches` in: tests 3 / 4 fail (the guard bites);
- remove the deletion call from `write`: test 5 fails; remove it from `onCreate`: test 7 fails.

Not testable here: the real share sheet and a receiving app still reading the file (device only).

## Tests first, at base `445b292b` (before any fix)

Run: `./gradlew --offline :app:testDebugUnitTest --tests '*JournalBackupTest' --tests '*TrackGpxExporterTest' --tests '*ForagerApplicationGpxStartTest'`, results directory cleared first, every XML newer than the run's start. 51 tests, 4 failed, 0 errors; build log has no compile errors. Exactly the four predicted, each for the stated reason:

| # | Test | Result | Message |
|---|------|--------|---------|
| 1 | backup made with searches holds no rows | FAIL | `cached_searches rows in the backup's snapshot expected:<0> but was:<2>` |
| 2 | no trace of search text in the snapshot | FAIL | `search text found in the snapshot file` |
| 3 | Replace leaves the phone's searches | PASS (predicted) | |
| 4 | Merge leaves the phone's searches | PASS (predicted) | |
| 5 | `write` removes an export older than an hour | FAIL | `an export 61 minutes old is still in the cache` |
| 6 | `write` leaves a non-gpx file alone | PASS (predicted) | |
| 7 | app start removes a stale export | FAIL | `an export 61 minutes old is still in the cache after startup` |

## What landed (on `privacy-fixes-redo-[model B]`)

- `RoomJournalBackup.kt`: new `clearSearchesFromSnapshot`, called in `doBackUp` right after `takeSnapshot`; opens the snapshot **copy** read-write and runs `DELETE FROM cached_searches`. The live database is never written. The no-op `DELETE ... WHERE 0` in `takeSnapshot` is unchanged (it still takes the write lock). Restore is unchanged: it never touched `cached_searches`, and tests 3 and 4 now hold that.
- `TrackGpxExporter.kt`: `deleteStaleExports()` returns a `StaleSweep(found, deleted)` for `.gpx` files in the export directory last modified over `MAX_EXPORT_AGE_MILLIS` (one hour) ago; `write` calls it first.
- `ForagerApplication.kt`: `onCreate` calls `deleteStaleGpxExports()`, on `applicationScope` like the capture sweep, logging a count at INFO and a warning if a file could not be deleted.
- Tests: 4 in `JournalBackupTest`, 2 in `TrackGpxExporterTest`, new `ForagerApplicationGpxStartTest` (1).

Commits: `9b54e5a2` (fix), `29d009ae` (VACUUM dropped); pre-registration `76b71b41`, tests first `eea15726`.

## Verification

- **Tests forward:** the three classes, 51 tests, 0 failures (before the VACUUM change), then the same after.
- **Revert checks** (each from a copy saved before editing, restored from that copy, build log with 0 compile errors, forward change confirmed present afterwards by `cmp` and `git diff`):

| Check | Edit | Result |
|-------|------|--------|
| R1 (twice, once on the final code) | remove the `DELETE` | tests 1 and 2 fail: `expected:<0> but was:<2>`; `search text found in the snapshot file` |
| R2 | remove the `VACUUM` (since dropped) | **nothing failed. Prediction wrong**, see Findings |
| R3a | Replace also copies `cached_searches` | test 3 fails, `schema 16 backup, Replace: the phone's searches expected:...` |
| R3b | Merge also copies `cached_searches` | test 4 fails, `schema 16 backup, Merge: ...` |
| R4 | `write` no longer sweeps | test 5 fails: `an export 61 minutes old is still in the cache` |
| R5 | `onCreate` no longer sweeps | test 7 fails: `... still in the cache after startup` |

- **Full suite**, results directory cleared, every XML newer than the run start: 379 files, **3145 tests, 0 failures, 0 errors, 24 skipped** (second run). The first run had **1 failure**, `LeavingTheJournalFixesTest` "F3 the Maps search bar shows on Maps while a find is kept open on the Journal", `IllegalArgumentException: performMeasureAndLayout called during measure layout`. Unrelated to this change (Compose layout, none of these files); the class then passed 34/34 three times alone, and the whole suite passed on rerun. Not touched. It reads as a flake seen once in three runs of that class's code, with no cause established.

## Findings

1. **A prediction failed (R2).** I predicted that `DELETE` alone would leave the deleted text in the file and that test 2 would fail without a `VACUUM`. It passed. A probe (a throwaway test, not committed) showed `PRAGMA secure_delete` reads 1 by default under Robolectric's SQLite 3.44.3, and that the text was also absent with `secure_delete=OFF` on a whole-table `DELETE`. So test 2 cannot tell `DELETE` from `DELETE` + `VACUUM`. I dropped the `VACUUM` (see Decisions). Test 2 still bites the `DELETE` (R1).
2. **The base had no F5** and the branch the dispatch names already holds the landed F5 (from the [model A] run); this is a redo on a new branch. See the top of this report.
3. **The code shows no share-completion signal** (`TrackExportPanel.kt:266`, `startActivity(Intent.createChooser(...))`, no result). The dispatch's "stop if sharper" did not trigger; the one-hour rule is what was built.

## Decisions I made

- Branch and worktree names `privacy-fixes-redo-[model B]` (the owner said "a new branch"; `privacy-fixes` and `privacy-fixes-[model B]` are in use). Pushed to that branch, not `journal-redesign`, on the owner's "new branch". Report file name differs from the dispatch's `2026-09-29-privacy-fixes-completion-report.md`, which the landed F5 holds on `journal-redesign`.
- Base `445b292b`, the dispatch's, so the redo is comparable and the new branch has an F5 to build.
- `VACUUM` added then removed (Finding 1). The owner or planner may prefer an explicit `PRAGMA secure_delete=ON`; it costs nothing, but no test could bite it here either.
- Only `.gpx` files are swept, by last-modified time, not every file in `cacheDir/tracks`.
- The sweep at start runs off the main thread, like the capture sweep.
- `deleteStaleExports` returns `StaleSweep(found, deleted)`, not a bare count, so a file that could not be deleted is logged instead of swallowed.

## Flags outside scope

- The landed F5 on `journal-redesign` and this branch do the same job differently; they will conflict if both are merged. Which one is kept is for the owner. Merge is not authorised and was not done.
- `/home/zynergy-labs/Zynergy/forager-wt/privacy-fixes-[model B]` had a live session with uncommitted F5 edits from base `445b292b` (seen 13:23 PDT). Possibly a duplicate of this run; not touched.
- The `LeavingTheJournalFixesTest` flake above.
- Planner role moved during the session (to the session "You are the planner...", ref `05172f`); the hand-back goes there.

## Not tested / device-only

- On a phone: make a backup with a search on it, unzip it, and confirm `forager.db` has no `cached_searches` rows and no search text (`strings forager.db | grep`), and that the phone's searches still show afterwards. This also settles whether `secure_delete` is on in the device's SQLite, which was only probed under Robolectric.
- On a phone: share a track, and confirm the receiving app opens it; confirm the file is gone from the app's cache after an hour and a restart (`run-as ... ls cache/tracks`).
- The one-hour boundary itself is tested at 59 and 61 minutes, not at exactly 60.
~~~~
