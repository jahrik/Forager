package com.zynergylabs.forager.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zynergylabs.forager.app.domain.BackupFrequency
import com.zynergylabs.forager.app.domain.BackupSchedulePreferences
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.ScheduledBackupNotice
import kotlinx.coroutines.flow.first

/**
 * [BackupSchedulePreferences] backed by Jetpack DataStore: flat settings, so DataStore and not Room (CLAUDE.md,
 * Room for data that relates; DataStore for flat settings), in its own file and built through
 * [PreferenceDataStoreFactory.create] rather than the process-wide delegate, for the Robolectric isolation
 * reason [DataStoreMapPreferencesRepository] records. Keys are namespaced `backup.`.
 *
 * Nothing stored means the defaults of [BackupScheduleSettings]: **off**, weekly, no folder. A stored frequency
 * this build does not know (a file written by a newer build) is a failure, not a quiet default.
 */
class DataStoreBackupSchedulePreferences(context: Context) : BackupSchedulePreferences {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.applicationContext.preferencesDataStoreFile(DATA_STORE_NAME) },
    )

    override suspend fun get(): Result<BackupScheduleSettings> = runCatchingCancellable {
        val prefs = dataStore.data.first()
        val defaults = BackupScheduleSettings()
        BackupScheduleSettings(
            enabled = prefs[KEY_ENABLED] ?: defaults.enabled,
            frequency = prefs[KEY_FREQUENCY]?.let { stored ->
                BackupFrequency.entries.firstOrNull { it.name == stored } ?: error("unknown backup frequency '$stored'")
            } ?: defaults.frequency,
            folderUri = prefs[KEY_FOLDER],
        )
    }

    override suspend fun save(settings: BackupScheduleSettings): Result<Unit> = runCatchingCancellable {
        dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = settings.enabled
            prefs[KEY_FREQUENCY] = settings.frequency.name
            if (settings.folderUri != null) prefs[KEY_FOLDER] = settings.folderUri else prefs.remove(KEY_FOLDER)
        }
        Unit
    }

    override suspend fun pendingNotice(): Result<ScheduledBackupNotice?> = runCatchingCancellable {
        when (val stored = dataStore.data.first()[KEY_PENDING_NOTICE]) {
            null -> null
            NOTICE_DID_NOT_FINISH -> ScheduledBackupNotice.DidNotFinish
            else -> if (stored.startsWith(NOTICE_SKIPPED_PREFIX)) {
                ScheduledBackupNotice.SavedWithSkippedPhotos(stored.removePrefix(NOTICE_SKIPPED_PREFIX).toInt())
            } else {
                error("unknown scheduled-backup notice '$stored'")
            }
        }
    }

    override suspend fun setPendingNotice(notice: ScheduledBackupNotice?): Result<Unit> = runCatchingCancellable {
        dataStore.edit { prefs ->
            when (notice) {
                null -> prefs.remove(KEY_PENDING_NOTICE)
                ScheduledBackupNotice.DidNotFinish -> prefs[KEY_PENDING_NOTICE] = NOTICE_DID_NOT_FINISH
                is ScheduledBackupNotice.SavedWithSkippedPhotos -> prefs[KEY_PENDING_NOTICE] = NOTICE_SKIPPED_PREFIX + notice.count
            }
        }
        Unit
    }

    override suspend fun scheduledBackupFiles(): Result<List<String>> = Result.success(emptyList()) // STUB (tests-first commit)

    override suspend fun setScheduledBackupFiles(uris: List<String>): Result<Unit> = Result.success(Unit) // STUB

    override suspend fun notificationPermissionAsked(): Result<Boolean> = Result.success(false) // STUB

    override suspend fun setNotificationPermissionAsked(): Result<Unit> = Result.success(Unit) // STUB

    private companion object {
        const val DATA_STORE_NAME = "backup_schedule_preferences"
        val KEY_ENABLED = booleanPreferencesKey("backup.enabled")
        val KEY_FREQUENCY = stringPreferencesKey("backup.frequency")
        val KEY_FOLDER = stringPreferencesKey("backup.folder_uri")
        val KEY_PENDING_NOTICE = stringPreferencesKey("backup.pending_notice")
        const val NOTICE_DID_NOT_FINISH = "did_not_finish"
        const val NOTICE_SKIPPED_PREFIX = "skipped:"
    }
}
