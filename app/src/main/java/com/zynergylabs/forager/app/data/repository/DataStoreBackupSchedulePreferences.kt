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

    private companion object {
        const val DATA_STORE_NAME = "backup_schedule_preferences"
        val KEY_ENABLED = booleanPreferencesKey("backup.enabled")
        val KEY_FREQUENCY = stringPreferencesKey("backup.frequency")
        val KEY_FOLDER = stringPreferencesKey("backup.folder_uri")
    }
}
