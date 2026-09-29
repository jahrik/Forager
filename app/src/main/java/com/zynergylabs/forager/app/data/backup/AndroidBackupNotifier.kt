package com.zynergylabs.forager.app.data.backup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zynergylabs.forager.app.domain.BackupNotifier
import com.zynergylabs.forager.app.domain.ScheduledBackupNotice

/** The "Backups" channel's id. */
internal const val BACKUP_CHANNEL_ID = "backups"

/** Tags the one-time job "Try again" enqueues. */
internal const val BACKUP_RETRY_WORK_TAG = "journal-backup-retry"

/** Posts a [ScheduledBackupNotice] in the "Backups" channel. (Tests-first stub: posts nothing.) */
class AndroidBackupNotifier(private val context: Context) : BackupNotifier {
    override fun notify(notice: ScheduledBackupNotice): Boolean = false
}

/** The "Try again" action on the "didn't finish" notification: runs one backup to the same folder. (Tests-first stub.) */
class BackupRetryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}

/** The extra a notification's tap carries so the app opens at the Backup section. */
const val EXTRA_OPEN_BACKUP_SECTION = "com.zynergylabs.forager.app.OPEN_BACKUP_SECTION"

/** Whether [intent] asks the app to open at the Backup section. (Tests-first stub.) */
fun opensBackupSection(intent: Intent?): Boolean = false
