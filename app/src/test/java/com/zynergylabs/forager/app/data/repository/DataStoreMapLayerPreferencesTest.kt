package com.zynergylabs.forager.app.data.repository

import android.app.Application
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.MapLayerPreferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The map's layer choices (map layers L0b, B3) through the real `map_preferences` DataStore: written
 * through one [DataStoreMapPreferencesRepository], read back by a **recreated** one, the way a restart
 * reads them.
 *
 * A second live DataStore on one file is refused by DataStore itself
 * (`DataStorePhotoLocationPreferenceRepositoryTest`), so the first instance is released before the
 * second is made: its scope is cancelled and joined, and DataStore 1.2.1 closes its file when that scope
 * completes (`DataStoreImpl`'s write actor closes the storage connection, which removes the file from
 * `FileStorage`'s active set; read with `javap`). The repository takes the scope for exactly this.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataStoreMapLayerPreferencesTest {

    private fun context() = ApplicationProvider.getApplicationContext<Application>()
    private fun dataStoreFile() = File(context().filesDir, "datastore/map_preferences.preferences_pb")

    private val scopes = mutableListOf<Job>()

    private fun repository(): Pair<DataStoreMapPreferencesRepository, Job> {
        val job = SupervisorJob()
        scopes += job
        return DataStoreMapPreferencesRepository(context(), CoroutineScope(Dispatchers.IO + job)) to job
    }

    @Before
    fun setUp() {
        dataStoreFile().delete()
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        dataStoreFile().delete()
    }

    @Test
    fun `nothing stored reads back as no stored choices`() = runTest {
        val (repository, _) = repository()

        assertEquals(MapLayerPreferences.NONE, repository.getMapLayerPreferences().getOrThrow())
    }

    @Test
    fun `visibility, opacity and order written by one instance are read back by a recreated one`() = runTest {
        val (writer, writerScope) = repository()
        writer.setLayerVisible("find-markers-layer", false).getOrThrow()
        writer.setLayerVisible("kept-tracks-layer", false).getOrThrow()
        writer.setLayerOpacity("forecast-chanterelles-layer", 0.4f).getOrThrow()
        writer.setLayerOrder(listOf("forecast-chicken-of-the-woods-layer", "forecast-chanterelles-layer")).getOrThrow()
        writerScope.cancelAndJoin()

        val (reader, _) = repository()

        assertEquals(
            MapLayerPreferences(
                visibility = mapOf("find-markers-layer" to false, "kept-tracks-layer" to false),
                opacity = mapOf("forecast-chanterelles-layer" to 0.4f),
                order = listOf("forecast-chicken-of-the-woods-layer", "forecast-chanterelles-layer"),
            ),
            reader.getMapLayerPreferences().getOrThrow(),
        )
    }

    @Test
    fun `the choices are written to map_preferences under the map-layer keys, beside the existing map settings`() = runTest {
        val (writer, writerScope) = repository()
        writer.setNightModeMaps(true).getOrThrow()
        writer.setLayerVisible("photo-markers-layer", false).getOrThrow()
        writer.setLayerOpacity("forecast-chanterelles-layer", 0.25f).getOrThrow()
        writer.setLayerOrder(listOf("forecast-chanterelles-layer", "forecast-chicken-of-the-woods-layer")).getOrThrow()
        writerScope.cancelAndJoin()

        // Read the file itself, with no repository in between, so the key names are what is checked.
        val rawScope = SupervisorJob().also { scopes += it }
        val raw = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + rawScope)) { dataStoreFile() }
        val prefs = raw.data.first()

        assertEquals(false, prefs[booleanPreferencesKey("map.layer.photo-markers-layer.visible")])
        assertEquals(0.25f, prefs[floatPreferencesKey("map.layer.forecast-chanterelles-layer.opacity")])
        assertEquals("forecast-chanterelles-layer,forecast-chicken-of-the-woods-layer", prefs[stringPreferencesKey("map.layer_order")])
        assertEquals("the existing night-mode key is untouched", true, prefs[booleanPreferencesKey("night_mode.maps")])
    }

    @Test
    fun `a second write to one layer replaces the first, and the other map settings still read back`() = runTest {
        val (repository, _) = repository()
        repository.setMapFullscreen(true).getOrThrow()
        repository.setLayerVisible("waypoints-layer", false).getOrThrow()
        repository.setLayerVisible("waypoints-layer", true).getOrThrow()
        repository.setLayerOpacity("forecast-chanterelles-layer", 0.9f).getOrThrow()
        repository.setLayerOpacity("forecast-chanterelles-layer", 0.3f).getOrThrow()

        val stored = repository.getMapLayerPreferences().getOrThrow()

        assertEquals(mapOf("waypoints-layer" to true), stored.visibility)
        assertEquals(mapOf("forecast-chanterelles-layer" to 0.3f), stored.opacity)
        assertEquals(true, repository.getMapFullscreen().getOrThrow())
    }
}
