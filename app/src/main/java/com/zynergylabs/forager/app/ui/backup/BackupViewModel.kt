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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

/**
 * The Backup section's logic: the manual backup, the automatic one's settings, and the restore prompt.
 *
 * - **What the user is told** is only ever a [BackupMessage] (the approved copy). The reason for a failure goes to
 *   [errorLog] and nowhere else on screen.
 * - **The automatic backup is off until the user turns it on, and can only be on with a folder** (owner, "5 C"):
 *   turning it on with no folder leaves it off and says [BackupMessage.AUTOMATIC_NEEDS_FOLDER].
 * - **A restore asks first.** Choosing a file only opens the prompt; nothing is restored until Replace or Merge.
 * - Its own scope on [ioDispatcher] (cancelled in [onCleared]) rather than `viewModelScope`, so a plain JVM test can
 *   run it on `Dispatchers.Unconfined` without an Android main looper.
 */
class BackupViewModel(
    private val backup: JournalBackup,
    private val preferences: BackupSchedulePreferences,
    private val scheduler: BackupScheduler,
    private val files: BackupFiles,
    private val errorLog: ErrorLog,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    /**
     * Called after a restore that worked. The app's screens read Room once and hold what they read (there is no
     * Flow anywhere in `data/local`), so without this a restore would change the database and leave every open
     * screen showing the old data until the next launch. `MainActivity` passes the reloads.
     */
    private val afterRestore: () -> Unit = {},
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState

    init {
        scope.launch {
            preferences.get().fold(
                onSuccess = { saved -> _uiState.update { it.copy(schedule = saved) } },
                onFailure = { errorLog.w(TAG, "could not read the saved backup schedule; the section starts from the off default", it) },
            )
        }
    }

    /** The section's controls for [state], the state the screen collected; the callbacks act on the live state. */
    fun controls(state: BackupUiState): BackupControls = BackupControls(
        state = state,
        onBackUpNow = ::onBackUpNow,
        onAutomaticChanged = ::onAutomaticChanged,
        onFrequencyChanged = ::onFrequencyChanged,
        onFolderChosen = ::onFolderChosen,
        onRestoreFileChosen = ::onRestoreFileChosen,
        onRestoreConfirmed = ::onRestoreConfirmed,
        onRestoreCancelled = ::onRestoreCancelled,
    )

    fun onBackUpNow(uri: String) {
        _uiState.update { it.copy(busy = true, message = null) }
        scope.launch {
            val saved = try {
                files.openForWrite(uri).use { sink -> backup.backUp(sink) }
            } catch (e: Exception) {
                Result.failure(e)
            }
            saved.onFailure { errorLog.w(TAG, "backup failed: ${it.message}", it) }
            _uiState.update { it.copy(busy = false, message = if (saved.isSuccess) BackupMessage.BACKUP_SAVED else BackupMessage.BACKUP_FAILED) }
        }
    }

    fun onAutomaticChanged(enabled: Boolean) {
        val current = _uiState.value.schedule
        if (enabled && current.folderUri == null) {
            _uiState.update { it.copy(message = BackupMessage.AUTOMATIC_NEEDS_FOLDER) }
            return
        }
        changeSchedule(current.copy(enabled = enabled))
    }

    fun onFrequencyChanged(frequency: BackupFrequency) = changeSchedule(_uiState.value.schedule.copy(frequency = frequency))

    fun onFolderChosen(uri: String) {
        try {
            files.keepAccessToFolder(uri)
        } catch (e: Exception) {
            errorLog.w(TAG, "could not keep access to the chosen backup folder: ${e.message}", e)
            return
        }
        changeSchedule(_uiState.value.schedule.copy(folderUri = uri))
    }

    fun onRestoreFileChosen(uri: String) {
        _uiState.update { it.copy(pendingRestoreUri = uri, message = null) }
    }

    fun onRestoreCancelled() {
        _uiState.update { it.copy(pendingRestoreUri = null) }
    }

    fun onRestoreConfirmed(mode: RestoreMode) {
        val uri = _uiState.value.pendingRestoreUri ?: return
        _uiState.update { it.copy(pendingRestoreUri = null, busy = true, message = null) }
        scope.launch {
            val restored = try {
                files.openForRead(uri).use { source -> backup.restore(source, mode) }
            } catch (e: Exception) {
                Result.failure(e)
            }
            restored.onFailure { errorLog.w(TAG, "restore failed: ${it.message}", it) }
            if (restored.isSuccess) afterRestore()
            _uiState.update { it.copy(busy = false, message = if (restored.isSuccess) BackupMessage.RESTORE_COMPLETE else BackupMessage.RESTORE_FAILED) }
        }
    }

    /** Saves [next], then shows it and hands it to the scheduler; a save that fails is logged and changes nothing. */
    private fun changeSchedule(next: BackupScheduleSettings) {
        _uiState.update { it.copy(message = null) }
        scope.launch {
            preferences.save(next).fold(
                onSuccess = {
                    _uiState.update { it.copy(schedule = next) }
                    scheduler.apply(next)
                },
                onFailure = { errorLog.w(TAG, "could not save the backup schedule; it is unchanged: ${it.message}", it) },
            )
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    private companion object {
        const val TAG = "BackupViewModel"
    }
}
