package com.zynergylabs.forager.app.data.backup

import com.zynergylabs.forager.app.domain.BackupException
import java.io.File
import java.io.FilterOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

/**
 * The backup file's format, version 1: a zip whose **first** entry is `manifest.json`, then `forager.db` (the
 * database snapshot), then `photos/<name>` for each photo file. The manifest carries `formatVersion`,
 * `appVersionCode`, `schemaVersion` (the snapshot's `PRAGMA user_version`), `createdAtEpochMillis` and, for every
 * other entry, its path, size in bytes and SHA-256. **Settings (DataStore) are not in it** (owner, ruling 3 B).
 * The manifest first means a reader learns what to expect, and what to refuse, before it has read a byte of the rest.
 */
internal data class BackupManifest(
    val formatVersion: Int,
    val appVersionCode: Long,
    val schemaVersion: Int,
    val createdAtEpochMillis: Long,
    val files: List<ManifestFile>,
) {
    fun toJson(): ByteArray {
        val list = JSONArray()
        for (f in files) list.put(JSONObject().put("path", f.path).put("sha256", f.sha256).put("bytes", f.bytes))
        return JSONObject()
            .put("formatVersion", formatVersion)
            .put("appVersionCode", appVersionCode)
            .put("schemaVersion", schemaVersion)
            .put("createdAtEpochMillis", createdAtEpochMillis)
            .put("files", list)
            .toString(2)
            .toByteArray(Charsets.UTF_8)
    }

    companion object {
        const val FORMAT_VERSION = 1
        const val NAME = "manifest.json"
        const val DATABASE_ENTRY = "forager.db"
        const val PHOTOS_PREFIX = "photos/"

        fun parse(bytes: ByteArray): BackupManifest {
            val o = try {
                JSONObject(String(bytes, Charsets.UTF_8))
            } catch (e: org.json.JSONException) {
                throw BackupException("the manifest is not readable JSON", e)
            }
            try {
                val files = o.getJSONArray("files")
                return BackupManifest(
                    formatVersion = o.getInt("formatVersion"),
                    appVersionCode = o.getLong("appVersionCode"),
                    schemaVersion = o.getInt("schemaVersion"),
                    createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                    files = (0 until files.length()).map {
                        val f = files.getJSONObject(it)
                        ManifestFile(f.getString("path"), f.getString("sha256"), f.getLong("bytes"))
                    },
                )
            } catch (e: org.json.JSONException) {
                throw BackupException("the manifest is missing a field: ${e.message}", e)
            }
        }
    }
}

internal data class ManifestFile(val path: String, val sha256: String, val bytes: Long)

/** A backup extracted into a scratch folder with every file checked against the manifest. */
internal class StagedBackup(val manifest: BackupManifest, val root: File) {
    val databaseFile: File get() = File(root, BackupManifest.DATABASE_ENTRY)

    /** The photo files by their path inside the archive (`photos/<name>`). */
    val photoFiles: Map<String, File> =
        manifest.files.filter { it.path.startsWith(BackupManifest.PHOTOS_PREFIX) }.associate { it.path to File(root, it.path) }
}

internal object BackupArchive {

    /** [path] is a name this format allows: exactly `forager.db`, or `photos/` plus one plain file name. Anything else could write outside the scratch folder. */
    fun isSafeEntryName(path: String): Boolean {
        if (path == BackupManifest.DATABASE_ENTRY) return true
        if (!path.startsWith(BackupManifest.PHOTOS_PREFIX)) return false
        val name = path.removePrefix(BackupManifest.PHOTOS_PREFIX)
        return name.isNotEmpty() && '/' !in name && '\\' !in name && name != "." && name != ".." && '\u0000' !in name
    }

    fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Writes the archive to [sink] without closing it (the caller owns the stream): the manifest, then the
     * snapshot, then [photos] (archive path to file) in path order. Returns the number of bytes written.
     */
    fun write(sink: OutputStream, manifest: BackupManifest, snapshot: File, photos: Map<String, File>): Long {
        val counting = CountingOutputStream(sink)
        val zip = ZipOutputStream(counting)
        zip.putNextEntry(ZipEntry(BackupManifest.NAME))
        zip.write(manifest.toJson())
        zip.closeEntry()
        zip.putNextEntry(ZipEntry(BackupManifest.DATABASE_ENTRY))
        snapshot.inputStream().use { it.copyTo(zip, BUFFER) }
        zip.closeEntry()
        // Photos are already compressed images; deflating them again costs time and saves nothing.
        zip.setLevel(Deflater.NO_COMPRESSION)
        for ((path, file) in photos.toSortedMap()) {
            zip.putNextEntry(ZipEntry(path))
            file.inputStream().use { it.copyTo(zip, BUFFER) }
            zip.closeEntry()
        }
        zip.finish()
        counting.flush()
        return counting.count
    }

    /**
     * Reads the archive from [source] into [scratch], checking as it goes: the manifest must come first and be a
     * format this build knows; every other entry must be one the manifest lists, by a safe name, once; and at the
     * end every listed file must be there with exactly the listed size and SHA-256. Anything else throws a
     * [BackupException] naming the file, and the caller deletes [scratch]. Nothing outside [scratch] is touched.
     */
    fun stage(source: InputStream, scratch: File): StagedBackup {
        val zip = ZipInputStream(source)
        val first = zip.nextEntry ?: throw BackupException("the file is not a backup: it has no entries")
        if (first.name != BackupManifest.NAME) throw BackupException("the file is not a backup: the first entry is ${first.name}, not ${BackupManifest.NAME}")
        val manifest = BackupManifest.parse(zip.readBytes())
        if (manifest.formatVersion != BackupManifest.FORMAT_VERSION) {
            throw BackupException("backup format version ${manifest.formatVersion} is not one this app can read (it reads ${BackupManifest.FORMAT_VERSION})")
        }
        val listed = manifest.files.associateBy { it.path }
        if (listed.size != manifest.files.size) throw BackupException("the manifest lists a file twice")
        for (path in listed.keys) if (!isSafeEntryName(path)) throw BackupException("the manifest lists a name this format does not allow: $path")
        if (BackupManifest.DATABASE_ENTRY !in listed) throw BackupException("the manifest does not list ${BackupManifest.DATABASE_ENTRY}")

        scratch.mkdirs()
        val seen = HashSet<String>()
        while (true) {
            val entry = zip.nextEntry ?: break
            val name = entry.name
            if (entry.isDirectory) continue
            val expected = listed[name] ?: throw BackupException("the archive holds a file the manifest does not list: $name")
            if (!seen.add(name)) throw BackupException("the archive holds $name twice")
            val target = File(scratch, name)
            target.parentFile?.mkdirs()
            val digest = MessageDigest.getInstance("SHA-256")
            var bytes = 0L
            target.outputStream().use { out ->
                val buffer = ByteArray(BUFFER)
                while (true) {
                    val n = zip.read(buffer)
                    if (n < 0) break
                    digest.update(buffer, 0, n)
                    out.write(buffer, 0, n)
                    bytes += n
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            if (bytes != expected.bytes) throw BackupException("$name is $bytes bytes, the manifest says ${expected.bytes}")
            if (!hash.equals(expected.sha256, ignoreCase = true)) throw BackupException("hash of $name does not match the manifest's")
        }
        for (path in listed.keys) if (path !in seen) throw BackupException("the archive is missing $path, which the manifest lists")
        return StagedBackup(manifest, scratch)
    }

    private const val BUFFER = 64 * 1024

    private class CountingOutputStream(out: OutputStream) : FilterOutputStream(out) {
        var count = 0L
            private set

        override fun write(b: Int) {
            out.write(b)
            count++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            count += len
        }

        override fun close() = flush() // never closes the caller's stream
    }
}
