package com.zynergylabs.forager.app.ui.availability

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import com.zynergylabs.forager.app.ui.log.RecordType
import com.zynergylabs.forager.app.ui.log.swipeToDeleteTag
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.OfflineMapRepository
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.AppThemeMode
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.ui.diagnostics.DIAGNOSTICS_ENTRY_LABEL
import com.zynergylabs.forager.app.ui.diagnostics.DIAGNOSTICS_TITLE
import com.zynergylabs.forager.app.ui.diagnostics.directoryHeading
import com.zynergylabs.forager.app.ui.map.Basemap
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Two pieces of [AvailabilityScreen], measured headlessly:
 *
 * 1. The quick-fire mode icon over the map's own top-right corner, and that tapping it opens
 *    [MapModePicker], and that tapping a mode chip there actually changes which [Basemap] the map
 *    slot receives — not just that an icon is somewhere on screen. Settings' old "Choose Maps
 *    Service" section is gone entirely — see [com.zynergylabs.forager.app.ui.map.MapMode]'s own doc comment for
 *    what superseded it — so this file no longer tests it.
 * 2. The "Offline Maps" sub-tab, reached through the Journal's Records tab (Journal restructure
 *    Stage 1 moved Offline Maps and Recorded Tracks out of Settings — see [com.zynergylabs.forager.app.ui.log.RecordsTab]):
 *    reachable regardless of the selected [com.zynergylabs.forager.app.ui.map.MapMode] (offline downloads always
 *    target a fixed source internally, so nothing about reaching it depends on the live mode
 *    selection — see `com.zynergylabs.forager.app.domain.OfflineMapRepository`'s doc comment), its navigation
 *    (a flat tab among Waypoints/Offline Maps/Recorded Tracks, left by tapping another tab rather
 *    than a back arrow), and picking a region by panning its
 *    [com.zynergylabs.forager.app.ui.map.CentrePinLocationPicker] map and confirming with OK, instead of typing
 *    coordinates.
 *
 * The map is stubbed, same reasoning as [AvailabilityScreenLayoutTest]: composing the real one
 * starts osmdroid. [CapturingMapSlot] backs *both* the main Map tab's map and the Offline Maps
 * submenu's picker map — both are mounted through the same [AvailabilityScreen.mapSlot] parameter,
 * and both can be composed at once (the main tab stays mounted behind the drawer while a submenu is
 * open), so the stub tells them apart by content: the picker always gets empty sightings/areas/
 * planned-trips, the main tab's ([SEARCHED_STATE]) doesn't.
 *
 * Not covered here, and not verifiable headlessly: the icon's and picker map's actual pixel
 * position/legibility over real map tiles — see README's "Not yet verified".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class AvailabilityScreenSettingsPanelTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private var capturedBasemap: Basemap? = null
    private var capturedOfflinePickerBasemap: Basemap? = null
    private var capturedNightMode: Boolean? = null
    private var capturedNightModeLoaded: Boolean? = null
    private var capturedOfflinePickerNightModeLoaded: Boolean? = null
    private var capturedThemeMode: AppThemeMode? = null
    private var capturedAutoSaveLocation: Boolean? = null
    private var capturedLockCamera: Boolean? = null

    /** See this class's doc comment for why the two map instances are told apart by content. */
    private val CapturingMapSlot: MapSlot = { _, content, renderMode, _, _, _, _, onCameraIdle, modifier ->
        if (content.sightings.isEmpty() && content.plannedTrips.isEmpty()) {
            capturedOfflinePickerBasemap = renderMode.basemap
            capturedOfflinePickerNightModeLoaded = renderMode.nightModeLoaded
            Column(modifier.testTag(OFFLINE_PICKER_MAP_TAG)) {
                Button(onClick = { onCameraIdle(PICKED_LOCATION) }) { Text("Simulate pan to test location") }
            }
        } else {
            capturedBasemap = renderMode.basemap
            capturedNightMode = renderMode.night
            capturedNightModeLoaded = renderMode.nightModeLoaded
            Box(modifier.testTag(MAP_SLOT_TAG))
        }
    }

    private fun setScreen(tracks: List<Track> = emptyList(), uiState: AvailabilityUiState = SEARCHED_STATE) {
        composeRule.setContent {
            AvailabilityScreen(
                uiState = uiState,
                onUseCurrentLocation = {},
                onManualLatChanged = {},
                onManualLngChanged = {},
                onSearchManualCoordinates = {},
                onRadiusChanged = {},
                onMonthSelected = {},
                onMapTabSelected = {},
                onSeasonalTabSelected = {},
                onTaxonSearchQueryChanged = {},
                onTaxonSearchResultSelected = {},
                onDismissTaxonSuggestions = {},
                onReopenTaxonSuggestions = {},
                onPlaceTripPin = { _, _, _ -> },
                onDeletePlannedTrip = {},
                onRecentSearchSelected = {},
                onOfflineMapLatChanged = {},
                onOfflineMapLngChanged = {},
                onOfflineMapRadiusChanged = {},
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                onNightModeMapsChanged = {},
                onThemeModeChanged = {},
                mapSlot = CapturingMapSlot,
                tracks = tracks,
            )
        }
    }

    /**
     * Unlike [setScreen], wires the offline-map callbacks to real local state so panning and
     * confirming the picker map (see [CapturingMapSlot]) actually round-trips into
     * [AvailabilityUiState] the same way [AvailabilityViewModel]'s real
     * `onOfflineMapLatChanged`/`onOfflineMapLngChanged` do — needed for the "picking a region sets
     * it" test, which otherwise has nothing to observe.
     */
    private fun setScreenWithOfflineMapsState(initial: AvailabilityUiState = SEARCHED_STATE) {
        composeRule.setContent {
            var current by remember { mutableStateOf(initial) }
            AvailabilityScreen(
                uiState = current,
                onUseCurrentLocation = {},
                onManualLatChanged = {},
                onManualLngChanged = {},
                onSearchManualCoordinates = {},
                onRadiusChanged = {},
                onMonthSelected = {},
                onMapTabSelected = {},
                onSeasonalTabSelected = {},
                onTaxonSearchQueryChanged = {},
                onTaxonSearchResultSelected = {},
                onDismissTaxonSuggestions = {},
                onReopenTaxonSuggestions = {},
                onPlaceTripPin = { _, _, _ -> },
                onDeletePlannedTrip = {},
                onRecentSearchSelected = {},
                onOfflineMapLatChanged = { text -> current = current.copy(offlineMapLatText = text) },
                onOfflineMapLngChanged = { text -> current = current.copy(offlineMapLngText = text) },
                onOfflineMapRadiusChanged = { radius -> current = current.copy(offlineMapRadiusKm = radius) },
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                onNightModeMapsChanged = { night -> current = current.copy(nightModeMaps = night) },
                onAutoSaveLocationToPhotosChanged = { enabled ->
                    current = current.copy(autoSaveLocationToPhotos = enabled)
                    capturedAutoSaveLocation = enabled
                },
                onLockCameraToPortraitChanged = { enabled ->
                    current = current.copy(lockCameraToPortrait = enabled)
                    capturedLockCamera = enabled
                },
                onThemeModeChanged = { mode ->
                    current = current.copy(themeMode = mode)
                    capturedThemeMode = mode
                },
                mapSlot = CapturingMapSlot,
            )
        }
    }

    /**
     * Settings moved one level deeper (map/navigation redesign dispatch B): no longer its own
     * bottom-nav tab, it's a sticky entry at the bottom of the "Tools" drawer's content (see
     * [CompactToolsDrawerContent]'s `showSettings` state and [SettingsEntryRow]). No
     * `performScrollTo()` needed: [SettingsEntryRow] sits outside [SearchControls]'s own internal
     * `verticalScroll` region, as a fixed sibling below it in the drawer's outer `Column` — that
     * outer Column has no scroll of its own, and `SearchControls`'s `Modifier.weight(1f)` sizes it
     * to exactly the remaining space, so the row is always laid out on screen at the Column's
     * bottom rather than scrolled out of view.
     */
    private fun openSettings() {
        composeRule.onNodeWithText("Tools").performClick()
        composeRule.onNodeWithText("Settings").performClick()
    }

    /**
     * Dispatch 2026-09-28-707 (the owner: "Move the two camera options at the bottom to a settings
     * menu inside the camera itself"): Settings no longer shows "Automatically Save Location to
     * Photos" or "Lock camera to portrait", nor their explanations. They are the camera's gear panel's
     * now, where `InAppCameraSettingsPanelTest` holds what these two tests held here: the defaults
     * (location on, lock off), the explanations, and that a touch writes the value.
     *
     * Replaces `the photo-location checkbox starts on, explains itself, and toggles` and `the
     * lock-camera checkbox starts off, explains that sideways photos save portrait, and toggles`,
     * which found both rows in Settings.
     */
    @Test
    fun `Settings no longer shows the two camera settings`() {
        setScreenWithOfflineMapsState()
        openSettings()
        composeRule.waitForIdle()

        // Settings is the page showing: its own rows are there.
        composeRule.onNodeWithText("Night Maps").assertIsDisplayed()
        composeRule.onNodeWithText("Crash Logs").performScrollTo().assertIsDisplayed()
        for (text in listOf(PHOTO_LOCATION_SETTING_LABEL, PHOTO_LOCATION_SETTING_EXPLANATION, LOCK_CAMERA_SETTING_LABEL, LOCK_CAMERA_SETTING_EXPLANATION)) {
            assertEquals("'$text' is not in Settings", 0, composeRule.onAllNodesWithText(text).fetchSemanticsNodes().size)
        }
        assertEquals("nothing is written just by opening Settings", null, capturedAutoSaveLocation)
        assertEquals(null, capturedLockCamera)
    }

    /**
     * Unit tests run against the debug variant, so the debug source set's entry row is the one
     * composed here; in release it composes nothing and this route does not exist. The panel reads
     * the real (empty) Robolectric `filesDir`, so the assertion is on the zero-count heading it
     * produces from that, not on a stub.
     */
    @Test
    fun `Settings offers Diagnostics in a debug build, one tap below Crash Logs, and it opens the panel`() {
        setScreenWithOfflineMapsState()
        openSettings()

        composeRule.onNodeWithText("Crash Logs").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(DIAGNOSTICS_ENTRY_LABEL).performScrollTo().assertIsDisplayed().performClick()

        composeRule.onNodeWithText(DIAGNOSTICS_TITLE).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(directoryHeading("photos/", 0)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(directoryHeading("captures/", 0)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Back to Settings").performClick()
        composeRule.onNodeWithText(DIAGNOSTICS_ENTRY_LABEL).performScrollTo().assertIsDisplayed()
    }
}

private const val MAP_SLOT_TAG = "settings-panel-test-map-slot"
private const val OFFLINE_PICKER_MAP_TAG = "settings-panel-test-offline-picker-map-slot"

private val REGION = Region(lat = 45.326, lng = -122.634, radiusKm = 15)
private val PICKED_LOCATION = LatLng(lat = 44.5, lng = -121.5)

private const val TRACK_STARTED_AT = 1_756_400_000_000L

/** Mirrors `TrackExportPanel`'s own private `formatTrackTimestamp` exactly, against the JVM's own default zone, so this stays correct under whatever timezone the test runs in. */
private fun expectedTrackTimestampText(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a").format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

private fun sighting(index: Int) = Sighting(
    observationId = index.toLong(),
    taxonId = 100L + index,
    scientificName = "Species $index",
    commonName = "Species $index",
    lat = REGION.lat + index * 0.001,
    lng = REGION.lng + index * 0.001,
    observedOn = LocalDate.of(2025, 8, 1),
    photoUrl = null,
)

private val SEARCHED_STATE = AvailabilityUiState(
    region = REGION,
    sightings = List(4) { sighting(it) },
)

/** J4b L5's one downloaded region. */
private val L5_REGION = OfflineRegionSummary(
    id = 5L,
    name = "Molalla Ridge",
    region = Region(lat = 45.1, lng = -122.5, radiusKm = 5),
    minZoom = OfflineMapRepository.MIN_ZOOM,
    maxZoom = OfflineMapRepository.MAX_ZOOM,
    tileCount = 1200,
    sizeBytes = 5_000_000L,
    createdAtEpochMillis = 0L,
)
