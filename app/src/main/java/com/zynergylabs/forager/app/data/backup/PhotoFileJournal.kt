package com.zynergylabs.forager.app.data.backup

import com.zynergylabs.forager.app.domain.BackupException
import com.zynergylabs.forager.app.domain.ErrorLog
import java.io.File
import java.util.UUID

/**
 * The photo-file half of a restore, kept undoable until the database half has committed (dispatch item 4: "keep
 * the live database and photos recoverable until the new ones are fully in place").
 *
 * [place] puts one of the backup's photo files at its path under `filesDir`. If a live file is already there
 * with the same bytes nothing happens; with different bytes it is **moved aside** (`<name>.pre-restore-<n>`),
 * never overwritten, and the backup's file is copied in. [rollback] removes what was copied in and moves every
 * set-aside file back, so the phone's folder is as it was; [commit] deletes the set-aside files once the
 * database side is committed. [deleteOld] is for after that: a photo the replaced journal used and the restored
 * journal does not.
 */
internal class PhotoFileJournal(private val filesDir: File, private val errorLog: ErrorLog) {
    private val added = mutableListOf<File>()
    private val setAside = mutableListOf<Pair<File, File>>() // (where it belongs, where it is now)

    val placedCount: Int get() = added.size

    fun place(path: String, backupFile: File) {
        if (!BackupArchive.isSafeEntryName(path)) throw BackupException("refusing to place a photo at $path")
        val target = File(filesDir, path)
        target.parentFile?.mkdirs()
        if (target.exists()) {
            if (BackupArchive.sha256Hex(target) == BackupArchive.sha256Hex(backupFile)) return
            val aside = File(target.parentFile, "${target.name}.pre-restore-${UUID.randomUUID()}")
            if (!target.renameTo(aside)) throw BackupException("could not set $path aside")
            setAside += target to aside
        }
        // Move, not copy, when the two folders allow it: the staged file is scratch and is deleted after, so a
        // move saves writing the photo twice on a phone that may be short of space.
        if (!backupFile.renameTo(target)) backupFile.copyTo(target, overwrite = false)
        added += target
    }

    fun rollback() {
        for (file in added) file.delete()
        for ((target, aside) in setAside) {
            target.delete()
            if (!aside.renameTo(target)) errorLog.w(TAG, "restore: could not put ${target.name} back after a failed restore", BackupException("left at ${aside.path}"))
        }
        added.clear()
        setAside.clear()
    }

    fun commit() {
        for ((_, aside) in setAside) aside.delete()
        setAside.clear()
    }

    /** Deletes the photo file at [path] if it is a path this format allows; a delete that does not work is logged, not fatal (an orphan file is harmless). */
    fun deleteOld(path: String) {
        if (!BackupArchive.isSafeEntryName(path)) return
        val file = File(filesDir, path)
        if (file.exists() && !file.delete()) errorLog.w(TAG, "restore: could not delete the old photo file $path", BackupException("delete failed: $path"))
    }

    private companion object {
        const val TAG = "PhotoFileJournal"
    }
}
