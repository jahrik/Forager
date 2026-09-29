package com.zynergylabs.forager.app.ui.backup

import com.zynergylabs.forager.app.domain.BackupException
import com.zynergylabs.forager.app.domain.BackupFiles
import com.zynergylabs.forager.app.domain.BackupReport
import com.zynergylabs.forager.app.domain.BackupSchedulePreferences
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.BackupScheduler
import com.zynergylabs.forager.app.domain.JournalBackup
import com.zynergylabs.forager.app.domain.RestoreMode
import com.zynergylabs.forager.app.domain.RestoreReport
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/** A [JournalBackup] that writes fixed bytes on a backup and records what a restore was given; either can be made to fail. */
internal class FakeJournalBackup : JournalBackup {
    var failBackUp = false
    var failRestore = false
    val restored = mutableListOf<Pair<RestoreMode, ByteArray>>()
    var backUps = 0

    override suspend fun backUp(sink: OutputStream): Result<BackupReport> {
        backUps++
        if (failBackUp) return Result.failure(BackupException("fake: backup failed"))
        sink.write(BACKUP_BYTES)
        return Result.success(BackupReport(photoFiles = 0, photoFilesMissing = 0, archiveBytes = BACKUP_BYTES.size.toLong()))
    }

    override suspend fun restore(source: InputStream, mode: RestoreMode): Result<RestoreReport> {
        restored += mode to source.readBytes()
        if (failRestore) return Result.failure(BackupException("fake: restore failed"))
        return Result.success(RestoreReport(mode, rowsInserted = 1, recordsSkipped = 0, rowsDropped = 0, photoFilesAdded = 0))
    }

    companion object {
        val BACKUP_BYTES = "PK-fake-backup".toByteArray()
    }
}

internal class FakeSchedulePreferences(var stored: BackupScheduleSettings = BackupScheduleSettings()) : BackupSchedulePreferences {
    var failGet = false
    override suspend fun get(): Result<BackupScheduleSettings> =
        if (failGet) Result.failure(IllegalStateException("fake: unreadable")) else Result.success(stored)

    override suspend fun save(settings: BackupScheduleSettings): Result<Unit> {
        stored = settings
        return Result.success(Unit)
    }
}

internal class FakeScheduler : BackupScheduler {
    val applied = mutableListOf<BackupScheduleSettings>()
    override fun apply(settings: BackupScheduleSettings) {
        applied += settings
    }
}

/**
 * In-memory files by URI. **There is deliberately no delete here, because [BackupFiles] has none**: nothing
 * that runs a backup can remove a file, which is how "one file per run and nothing is deleted" is held by
 * construction, not by a test that hopes.
 */
internal class FakeBackupFiles : BackupFiles {
    val written = linkedMapOf<String, ByteArrayOutputStream>()
    val contents = mutableMapOf<String, ByteArray>()
    val created = mutableListOf<Pair<String, String>>()
    val keptFolders = mutableListOf<String>()
    var failOpen = false
    var failFolder = false

    override fun openForWrite(uri: String): OutputStream {
        if (failOpen) throw java.io.IOException("fake: cannot open $uri")
        return ByteArrayOutputStream().also { written[uri] = it }
    }

    override fun openForRead(uri: String): InputStream {
        if (failOpen) throw java.io.IOException("fake: cannot open $uri")
        return ByteArrayInputStream(contents.getValue(uri))
    }

    override fun createInFolder(folderUri: String, displayName: String): OutputStream {
        if (failFolder) throw java.io.IOException("fake: folder unreadable")
        created += folderUri to displayName
        return ByteArrayOutputStream().also { written["$folderUri/$displayName#${created.size}"] = it }
    }

    override fun keepAccessToFolder(folderUri: String) {
        keptFolders += folderUri
    }
}
