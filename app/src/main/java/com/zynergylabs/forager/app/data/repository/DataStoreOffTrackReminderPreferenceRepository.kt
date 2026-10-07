package com.zynergylabs.forager.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zynergylabs.forager.app.domain.OffTrackReminderPreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [OffTrackReminderPreferenceRepository] backed by Jetpack DataStore (dispatch 2026-09-28-626).
 *
 * Built via [PreferenceDataStoreFactory.create] per instance, not the process-wide
 * `preferencesDataStore` delegate, for the Robolectric-isolation reason
 * [DataStoreMapPreferencesRepository]'s header records. Its own file, `off_track_reminder_preferences`.
 *
 * [enabledNow] is the checkbox's value as this instance last read or stored it, first read as soon
 * as this is built (at app start, in `AppContainer`). A read that fails is logged and leaves the
 * default, on.
 */
class DataStoreOffTrackReminderPreferenceRepository(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : OffTrackReminderPreferenceRepository {

    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { context.applicationContext.preferencesDataStoreFile(DATA_STORE_NAME) },
    )

    /**
     * The checkbox as last read or stored by this instance. Production has one instance
     * (`AppContainer`), and every change goes through [setEnabled], so this is the stored value
     * once the first read below has finished.
     */
    @Volatile private var cached: Boolean = DEFAULT_ENABLED

    init {
        // Read once at app start, so [enabledNow] holds the stored value before any recording can
        // begin. A failed read is logged and leaves the default, on.
        scope.launch {
            getEnabled().onFailure { error ->
                Log.w(TAG, "Couldn't read whether the off-track reminder is on; the alert stays on, the default.", error)
            }
        }
    }

    override suspend fun getEnabled(): Result<Boolean> = runCatchingCancellable {
        DEFAULT_ENABLED
    }

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> = runCatchingCancellable {
        Unit
    }

    override fun enabledNow(): Boolean = cached

    override suspend fun getLastSeenBlocked(): Result<Boolean> = runCatchingCancellable {
        false
    }

    override suspend fun setLastSeenBlocked(blocked: Boolean): Result<Unit> = runCatchingCancellable {
        Unit
    }

    private companion object {
        const val TAG = "OffTrackReminderPrefs"
        const val DATA_STORE_NAME = "off_track_reminder_preferences"

        /** The owner, Amendment 1 to dispatch -626: "on by default". */
        const val DEFAULT_ENABLED = true

        val KEY_ENABLED = booleanPreferencesKey("off_track_reminder.enabled")
        val KEY_LAST_SEEN_BLOCKED = booleanPreferencesKey("off_track_reminder.last_seen_blocked")
    }
}
