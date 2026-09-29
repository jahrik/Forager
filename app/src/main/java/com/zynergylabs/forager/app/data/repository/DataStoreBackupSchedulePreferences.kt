package com.zynergylabs.forager.app.data.repository

import android.content.Context
import com.zynergylabs.forager.app.domain.BackupSchedulePreferences
import com.zynergylabs.forager.app.domain.BackupScheduleSettings

/** (Tests-first stub.) */
class DataStoreBackupSchedulePreferences(context: Context) : BackupSchedulePreferences {
    override suspend fun get(): Result<BackupScheduleSettings> = Result.failure(UnsupportedOperationException("backup schedule preferences: not built"))

    override suspend fun save(settings: BackupScheduleSettings): Result<Unit> = Result.failure(UnsupportedOperationException("backup schedule preferences: not built"))
}
