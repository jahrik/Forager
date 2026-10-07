package com.zynergylabs.forager.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zynergylabs.forager.app.domain.AppThemePreferenceRepository
import com.zynergylabs.forager.app.domain.model.AppThemeMode
import kotlinx.coroutines.flow.first

/**
 * [AppThemePreferenceRepository] backed by Jetpack DataStore — see that interface's doc comment
 * for why. Built via [PreferenceDataStoreFactory.create] directly rather than the
 * `by preferencesDataStore(name = ...)` singleton delegate, for the same Robolectric
 * test-isolation reason [DataStoreMapPreferencesRepository] documents on itself.
 *
 * [KEY_THEME_MODE_LEGACY_DARK] is this preference's original, boolean-only shape (`true`/`false`
 * for dark/light) — real installs already have it persisted, so the tri-state
 * [AppThemeMode]/[KEY_THEME_MODE] this repository now stores instead has to read that key as a
 * fallback rather than silently reverting an existing user's choice to the new default. A read with
 * no usable [KEY_THEME_MODE] value falls back to it, and a read with neither falls back further to
 * [AppThemeMode.SYSTEM_DEFAULT] — the project owner's own explicit choice for a brand-new install
 * with nothing persisted yet, distinct from the legacy-boolean fallback above, which still resolves
 * to [AppThemeMode.LIGHT]/[AppThemeMode.DARK] to preserve an existing pre-tri-state user's own
 * already-made choice exactly. See [getThemeMode].
 *
 * "No usable value" covers two cases, not one (dispatch 2026-09-28-658, D2, correcting this comment,
 * which said only a missing value falls back): no [KEY_THEME_MODE] at all, and a stored name this
 * build does not know (a downgrade, or a renamed mode). The second takes the same fallbacks, and is
 * logged when it does, so a reset theme leaves a trace. Whether an unknown name should instead fail
 * the read, as the camera grid setting's does, is left to the owner (scout item D3).
 */
class DataStoreAppThemePreferenceRepository(context: Context) : AppThemePreferenceRepository {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.applicationContext.preferencesDataStoreFile(DATA_STORE_NAME) },
    )

    override suspend fun getThemeMode(): Result<AppThemeMode> = runCatchingCancellable {
        val prefs = dataStore.data.first()
        val storedName = prefs[KEY_THEME_MODE]
        val stored = storedName?.let { name -> AppThemeMode.entries.firstOrNull { it.name == name } }
        if (storedName != null && stored == null) {
            Log.w(TAG, "Unknown stored theme '$storedName'; falling back to the legacy setting or the system default.")
        }
        stored
            ?: prefs[KEY_THEME_MODE_LEGACY_DARK]?.let { dark -> if (dark) AppThemeMode.DARK else AppThemeMode.LIGHT }
            ?: AppThemeMode.SYSTEM_DEFAULT
    }

    override suspend fun setThemeMode(mode: AppThemeMode): Result<Unit> = runCatchingCancellable {
        dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode.name }
    }

    private companion object {
        const val TAG = "AppThemePreference"
        const val DATA_STORE_NAME = "app_theme_preferences"
        val KEY_THEME_MODE = stringPreferencesKey("app_theme.mode")
        val KEY_THEME_MODE_LEGACY_DARK = booleanPreferencesKey("app_theme.dark")
    }
}
