package com.zynergylabs.forager.app.ui.backup

import androidx.lifecycle.ViewModel
import com.zynergylabs.forager.app.domain.BackupFiles
import com.zynergylabs.forager.app.domain.BackupFrequency
import com.zynergylabs.forager.app.domain.BackupSchedulePreferences
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.BackupScheduler
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.JournalBackup
import com.zynergylabs.forager.app.domain.RestoreMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Every string the Backup section shows the user beyond its labels: the approved messages, and only those. */
enum class BackupMessage(val text: String) {
    BACKUP_SAVED("Backup saved."),
    BACKUP_FAILED("Couldn't save the backup."),
    AUTOMATIC_NEEDS_FOLDER("Automatic backup is off until you choose a folder."),
    RESTORE_COMPLETE("Restore complete."),
    RESTORE_FAILED("Couldn't restore that backup."),
}

/** What the Backup section draws. [pendingRestoreUri] is set while the "Restore this backup?" prompt is up. */
data class BackupUiState(
    val schedule: BackupScheduleSettings = BackupScheduleSettings(),
    val busy: Boolean = false,
    val message: BackupMessage? = null,
    val pendingRestoreUri: String? = null,
)

/** The Backup section's state and callbacks, as [com.zynergylabs.forager.app.ui.availability.SettingsContent] takes them. */
data class BackupControls(
    val state: BackupUiState = BackupUiState(),
    val onBackUpNow: (uri: String) -> Unit = {},
    val onAutomaticChanged: (Boolean) -> Unit = {},
    val onFrequencyChanged: (BackupFrequency) -> Unit = {},
    val onFolderChosen: (uri: String) -> Unit = {},
    val onRestoreFileChosen: (uri: String) -> Unit = {},
    val onRestoreConfirmed: (RestoreMode) -> Unit = {},
    val onRestoreCancelled: () -> Unit = {},
)

/** (Tests-first stub: holds state and does nothing.) */
class BackupViewModel(
    private val backup: JournalBackup,
    private val preferences: BackupSchedulePreferences,
    private val scheduler: BackupScheduler,
    private val files: BackupFiles,
    private val errorLog: ErrorLog,
    /** Where the file work runs; a test passes `Dispatchers.Unconfined` so it finishes before the screen is asked. */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    /** Called after a restore that worked, so the app can reload what its screens hold. */
    private val afterRestore: () -> Unit = {},
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState

    /** The section's controls for [state], the state the screen collected. (Stub: the callbacks do nothing.) */
    fun controls(state: BackupUiState): BackupControls = BackupControls(state = state)
}
