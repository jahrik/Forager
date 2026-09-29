package com.zynergylabs.forager.app.data.backup

import android.content.Context
import com.zynergylabs.forager.app.data.local.ForagerDatabase
import com.zynergylabs.forager.app.domain.BackupReport
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.JournalBackup
import com.zynergylabs.forager.app.domain.RestoreMode
import com.zynergylabs.forager.app.domain.RestoreReport
import com.zynergylabs.forager.app.domain.SystemCurrentTimeProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Seams a test uses to stop the operation at a chosen point: [snapshotLockHeld] runs while the snapshot holds
 * the database's write lock, [afterPhotosCopied] once a restore has copied photo files in but before it writes
 * rows, and [afterRowsWritten] inside the restore's transaction after the rows are written and before it
 * commits. A hook that throws makes the operation fail there, which is how the rollback is exercised.
 * Production passes none.
 */
class RoomJournalBackupHooks(
    val snapshotLockHeld: () -> Unit = {},
    val afterPhotosCopied: () -> Unit = {},
    val afterRowsWritten: () -> Unit = {},
)

/** The journal backup over the app's Room database and photo folder. (Tests-first stub: every operation is unsupported.) */
class RoomJournalBackup(
    private val context: Context,
    private val database: ForagerDatabase,
    private val databaseFile: File,
    private val filesDir: File,
    private val scratchDir: File,
    private val appVersionCode: Long,
    private val errorLog: ErrorLog,
    private val clock: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val hooks: RoomJournalBackupHooks = RoomJournalBackupHooks(),
) : JournalBackup {
    override suspend fun backUp(sink: OutputStream): Result<BackupReport> =
        Result.failure(UnsupportedOperationException("journal backup: not built"))

    override suspend fun restore(source: InputStream, mode: RestoreMode): Result<RestoreReport> =
        Result.failure(UnsupportedOperationException("journal restore: not built"))
}
