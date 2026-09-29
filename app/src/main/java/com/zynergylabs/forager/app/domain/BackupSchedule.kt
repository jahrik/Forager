package com.zynergylabs.forager.app.domain

import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId

/** How often the automatic backup runs (owner, "2 C": daily, weekly or monthly). */
enum class BackupFrequency { DAILY, WEEKLY, MONTHLY }

/**
 * The automatic backup's settings (owner, "5 C scheduled set to off by default, must be turned on by user").
 * [enabled] is false until the user turns it on, and it can only be true with a [folderUri]. [frequency] has no
 * ruled default, so it starts at [BackupFrequency.WEEKLY]; the user changes it before or after turning the switch on.
 */
data class BackupScheduleSettings(
    val enabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.WEEKLY,
    val folderUri: String? = null,
)

/** A file [createInFolder] made: where it is, and the stream to write it. */
class BackupTarget(val uri: String, val stream: OutputStream)

/** DataStore-backed in production, one flat file (CLAUDE.md: DataStore for flat settings). A failed read is a [Result.failure], never a default. */
interface BackupSchedulePreferences {
    suspend fun get(): Result<BackupScheduleSettings>

    suspend fun save(settings: BackupScheduleSettings): Result<Unit>

    /** A scheduled-backup notice that could not be shown as a notification, waiting to be shown in the app once; `null` when there is none. */
    suspend fun pendingNotice(): Result<ScheduledBackupNotice?>

    suspend fun setPendingNotice(notice: ScheduledBackupNotice?): Result<Unit>
}

/** Makes the operating system's job list match [settings]: periodic work when enabled, none otherwise. WorkManager in production. */
interface BackupScheduler {
    fun apply(settings: BackupScheduleSettings)
}

/**
 * The files a backup is written to and a restore reads from, addressed by the URI string the Storage Access
 * Framework gave the user. Behind an interface so the domain and the tests never touch `ContentResolver`.
 */
interface BackupFiles {
    fun openForWrite(uri: String): OutputStream

    fun openForRead(uri: String): InputStream

    /** A new file called [displayName] in the folder [folderUri]; the provider may rename it if the name is taken (nothing is overwritten). */
    fun createInFolder(folderUri: String, displayName: String): BackupTarget

    /**
     * Deletes the file at [uri] and reports whether it is gone. **Only ever called with a file the same run
     * created**, when that run's write failed or the person cancelled (owner, "7 A"); nothing prunes old backups.
     */
    fun delete(uri: String): Boolean

    /**
     * How many bytes the file at [uri] holds now, from the provider's own size column (`OpenableColumns.SIZE`), or `null`
     * when the provider does not say. Asked of the file the Save picker returned, before anything is written to it.
     */
    fun sizeOf(uri: String): Long?

    /** Keeps read and write access to [folderUri] across restarts (a persisted URI permission). */
    fun keepAccessToFolder(folderUri: String)
}

/** `forager-backup-<date>.zip`, the default file name (approved copy), the date being [epochMillis] in [zone]. */
fun backupFileName(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    "forager-backup-${Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()}.zip"

/**
 * One scheduled run: reads the settings, and writes one new file to the chosen folder. **One file per run and
 * nothing is deleted**, so old backups accumulate until the user removes them (the dispatch's carve-out from
 * ruling a retention policy). A run with the setting off, no folder, or an unreadable folder is a
 * [Result.failure], so the caller (the worker) reports it rather than counting it a success.
 */
class RunScheduledBackupUseCase(
    private val backup: JournalBackup,
    private val preferences: BackupSchedulePreferences,
    private val files: BackupFiles,
    private val clock: CurrentTimeProvider = SystemCurrentTimeProvider,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val errorLog: ErrorLog = ErrorLog { _, _, _ -> },
) {
    suspend operator fun invoke(): Result<BackupReport> {
        val settings = preferences.get().getOrElse { return Result.failure(it) }
        if (!settings.enabled) return Result.failure(BackupException("the scheduled backup ran while the setting is off; nothing was written"))
        val folder = settings.folderUri ?: return Result.failure(BackupException("the scheduled backup ran with no folder chosen; nothing was written"))
        val name = backupFileName(clock.nowEpochMillis(), zone)
        val target = try {
            files.createInFolder(folder, name)
        } catch (e: Exception) {
            return Result.failure(BackupException("could not create $name in the backup folder: ${e.message}", e))
        }
        // No screen to ask on: unreadable photos are skipped and counted in the report (owner, "yes that sounds good").
        val written = try {
            target.stream.use { backup.backUp(it, UnreadablePhotoPolicy.SKIP) }
        } catch (e: Exception) {
            Result.failure(e)
        }
        // A run that failed leaves nothing behind: the file it created is removed, and only that file (owner, "7 A").
        if (written.isFailure) removeIncomplete(target.uri)
        return written
    }

    private fun removeIncomplete(uri: String) {
        val removed = try {
            files.delete(uri)
        } catch (e: Exception) {
            errorLog.w("RunScheduledBackup", "could not delete the incomplete backup file $uri: ${e.message}", e)
            return
        }
        if (!removed) errorLog.w("RunScheduledBackup", "could not delete the incomplete backup file $uri", BackupException("delete reported false for $uri"))
    }
}
