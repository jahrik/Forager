package com.zynergylabs.forager.app.data.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.BackupScheduler
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.RunScheduledBackupUseCase

/** What the worker needs, reached through the application, so the worker keeps the two-argument constructor WorkManager instantiates. */
interface ScheduledBackupDependencies {
    val runScheduledBackup: RunScheduledBackupUseCase
    val errorLog: ErrorLog
}

/** (Tests-first stub.) */
class ScheduledBackupWorker @JvmOverloads constructor(
    context: Context,
    params: WorkerParameters,
    private val dependencies: ScheduledBackupDependencies? = null,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.failure()
}

/** (Tests-first stub: does nothing.) */
class WorkManagerBackupScheduler(private val workManager: WorkManager) : BackupScheduler {
    override fun apply(settings: BackupScheduleSettings) = Unit

    companion object {
        const val UNIQUE_WORK_NAME = "journal-backup"
    }
}
