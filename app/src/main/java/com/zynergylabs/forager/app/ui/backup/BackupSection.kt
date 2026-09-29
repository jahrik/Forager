package com.zynergylabs.forager.app.ui.backup

/** Test tags for the Backup section (tests-first: the section itself is not built yet). */
internal const val BACKUP_AUTOMATIC_SWITCH_TAG = "backup-automatic-switch"
internal const val BACKUP_MESSAGE_TAG = "backup-message"
internal const val BACKUP_FOLDER_NAME_TAG = "backup-folder-name"
internal const val RESTORE_PROMPT_TAG = "restore-prompt"

internal fun backupFrequencyTag(frequency: com.zynergylabs.forager.app.domain.BackupFrequency) = "backup-frequency-${frequency.name}"
