package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import com.zynergylabs.forager.app.domain.AppThemePreferenceRepository
import com.zynergylabs.forager.app.domain.ComputeFruitingLagDistributionUseCase
import com.zynergylabs.forager.app.domain.ComputeTripWindowsUseCase
import com.zynergylabs.forager.app.domain.DEFAULT_STALE_THRESHOLD_DAYS
import com.zynergylabs.forager.app.domain.DeletePlannedTripUseCase
import com.zynergylabs.forager.app.domain.GetAvailabilityUseCase
import com.zynergylabs.forager.app.domain.GetConditionsUseCase
import com.zynergylabs.forager.app.domain.GetPlannedTripsUseCase
import com.zynergylabs.forager.app.domain.GetRecentSearchesUseCase
import com.zynergylabs.forager.app.domain.GetSeasonalPatternUseCase
import com.zynergylabs.forager.app.domain.GetSightingsUseCase
import com.zynergylabs.forager.app.domain.GetTodaysForecastUseCase
import com.zynergylabs.forager.app.domain.GetTripWindowsUseCase
import com.zynergylabs.forager.app.domain.HistoricalWeatherProvider
import com.zynergylabs.forager.app.domain.InMemorySearchCacheRepository
import com.zynergylabs.forager.app.domain.LocationProvider
import com.zynergylabs.forager.app.domain.MapPreferencesRepository
import com.zynergylabs.forager.app.domain.MushroomRepository
import com.zynergylabs.forager.app.domain.OfflineMapRepository
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.PlannedTripRepository
import com.zynergylabs.forager.app.domain.PredictAvailabilityUseCase
import com.zynergylabs.forager.app.domain.SavePlannedTripUseCase
import com.zynergylabs.forager.app.domain.SearchTaxaUseCase
import com.zynergylabs.forager.app.domain.TaxonSearchRepository
import com.zynergylabs.forager.app.domain.TripPlanningWeatherProvider
import com.zynergylabs.forager.app.domain.UnitSystemPreferenceRepository
import com.zynergylabs.forager.app.domain.WeatherProvider
import com.zynergylabs.forager.app.domain.model.AppThemeMode
import com.zynergylabs.forager.app.domain.model.ConditionsSummary
import com.zynergylabs.forager.app.domain.model.DailyWeather
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.SightingsPage
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.domain.model.TaxonSearchResult
import com.zynergylabs.forager.app.domain.model.UnitSystem
import com.zynergylabs.forager.app.domain.model.WeatherSeries
import com.zynergylabs.forager.app.ui.availability.AvailabilityViewModel
import java.time.LocalDate
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.Alert
import com.zynergylabs.forager.app.domain.AlertAudibility
import com.zynergylabs.forager.app.domain.AlertAudibilityState
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.ComputeReturnToStartUseCase
import com.zynergylabs.forager.app.domain.CreateWaypointUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeleteWaypointUseCase
import com.zynergylabs.forager.app.domain.DetectOffTrackUseCase
import com.zynergylabs.forager.app.domain.GetTracksUseCase
import com.zynergylabs.forager.app.domain.GetWaypointsUseCase
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.LocationTracker
import com.zynergylabs.forager.app.domain.RingerMode
import com.zynergylabs.forager.app.domain.StartTrackUseCase
import com.zynergylabs.forager.app.domain.TrackRepository
import com.zynergylabs.forager.app.domain.WaypointRepository
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.Basemap
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.track.TrackRecordingViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Journal redesign J4 (`prompts/preserved/2026-09-27-21.md`): delete with swipe and Undo, driven
 * through the real [JournalTab] rows, the real owning ViewModel over a fake repository, a real
 * [SnackbarHost], and [PendingDeleteSnackbarEffects] fed by the same notice builders `MainActivity`
 * uses. Every delete assertion reads the fake repository's own call list, not a callback.
 *
 * Swipes are real `performTouchInput { swipeLeft() }` gestures on the rows, on more than one row
 * position. The snackbar's text is asserted whole. The snackbar's timeout is the host's own
 * [androidx.compose.material3.SnackbarDuration.Long], reached by advancing the test clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class JournalPendingDeleteTest {

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

    private val waypointRepository = PendingDeleteWaypointRepository(PD_WAYPOINTS)
    private lateinit var trackViewModel: TrackRecordingViewModel
    private val offlineMapRepository = PendingDeleteOfflineMapRepository(PD_REGIONS)
    private lateinit var availabilityViewModel: AvailabilityViewModel
    private val searchCache = InMemorySearchCacheRepository()

    private fun setScreen(
        waypointReferenceCounts: Map<String, Int> = mapOf("wp-creek" to 2, "wp-oak" to 1),
        chip: RecordsSubTab = RecordsSubTab.WAYPOINTS,
        regionReferenceCounts: Map<Long, Int> = mapOf(7L to 2, 8L to 0),
    ) {
        availabilityViewModel = AvailabilityViewModel(
            locationProvider = PendingDeleteUnusedLocationProvider,
            locationTracker = PendingDeleteNoOpLocationTracker,
            getAvailability = GetAvailabilityUseCase(PredictAvailabilityUseCase(PendingDeleteEmptyRepository), searchCache),
            getRecentSearches = GetRecentSearchesUseCase(searchCache),
            getSightings = GetSightingsUseCase(PendingDeleteEmptyRepository),
            searchTaxa = SearchTaxaUseCase(PendingDeleteEmptyRepository),
            getConditions = GetConditionsUseCase(PendingDeleteWeather),
            getTripWindows = GetTripWindowsUseCase(PendingDeleteWeather, ComputeTripWindowsUseCase()),
            getPlannedTrips = GetPlannedTripsUseCase(PendingDeletePlannedTrips),
            savePlannedTrip = SavePlannedTripUseCase(PendingDeletePlannedTrips),
            deletePlannedTrip = DeletePlannedTripUseCase(PendingDeletePlannedTrips),
            getSeasonalPattern = GetSeasonalPatternUseCase(
                GetSightingsUseCase(PendingDeleteEmptyRepository),
                PendingDeleteWeather,
                ComputeFruitingLagDistributionUseCase(),
            ),
            offlineMapRepository = offlineMapRepository,
            mapPreferencesRepository = PendingDeleteMapPreferences,
            unitSystemPreferenceRepository = PendingDeleteUnitSystem,
            appThemePreferenceRepository = PendingDeleteTheme,
            getTodaysForecast = GetTodaysForecastUseCase(PendingDeleteWeather),
            getOfflineRegionReferenceCount = { id -> regionReferenceCounts[id] ?: 0 },
        )
        val trackRepository = PendingDeleteTrackRepository()
        trackViewModel = TrackRecordingViewModel(
            trackRepository = trackRepository,
            startTrack = StartTrackUseCase(trackRepository, currentTime = PD_TIME, idGenerator = { "track-new" }),
            getWaypoints = GetWaypointsUseCase(waypointRepository),
            createWaypoint = CreateWaypointUseCase(waypointRepository, currentTime = PD_TIME, idGenerator = { "wp-new" }),
            deleteWaypoint = DeleteWaypointUseCase(waypointRepository),
            computeReturnToStart = ComputeReturnToStartUseCase(),
            detectOffTrack = DetectOffTrackUseCase(),
            locationTracker = PendingDeleteNoOpLocationTracker,
            getTracks = GetTracksUseCase(trackRepository),
            alertDelivery = { _: Alert -> },
            alertAudibility = PendingDeleteAudible,
            getWaypointReferenceCount = { id -> waypointReferenceCounts[id] ?: 0 },
        )
        composeRule.setContent {
            val hostState = remember { SnackbarHostState() }
            val track by trackViewModel.uiState.collectAsState()
            val availability by availabilityViewModel.uiState.collectAsState()
            val journalState = rememberJournalScreenState()
            Box(modifier = Modifier.fillMaxSize()) {
                JournalTab(
                    uiState = MushroomLogUiState(),
                    onOpenCameraForLogEntry = {},
                    onOpenCameraForAlbum = {},
                    onOpenCameraForCartographyEntry = {},
                    mapSlot = PD_STUB_MAP,
                    pickerRegion = Region(lat = 45.326, lng = -122.634, radiusKm = 15),
                    basemap = Basemap.DEFAULT,
                    onOpenEntry = {},
                    onCloseEntry = {},
                    onStartEntry = { _, _ -> },
                    onEntryChanged = {},
                    onStartEditingEntry = {},
                    onSaveEntry = {},
                    onCancelEditing = {},
                    onLeaveEditingIncidentally = {},
                    onAddPhoto = {},
                    onRemovePhoto = {},
                    onPullPhoto = {},
                    onDeleteEntry = {},
                    onSaveErrorDismissed = {},
                    cartographyUiState = CartographyUiState(),
                    onOpenCartographyEntry = {},
                    onStartCartographyEntry = {},
                    onCloseCartographyEntry = {},
                    onCartographyTextChanged = {},
                    onCartographyTagsChanged = {},
                    onSetFindDecision = { _, _ -> },
                    onSetTrackDecision = { _, _ -> },
                    onSetWaypointDecision = { _, _ -> },
                    onSetOfflineRegionDecision = { _, _ -> },
                    onToggleKeptPhoto = {},
                    onFinishCartographyEntry = {},
                    onDeleteCartographyEntry = {},
                    getCartographyEntryMapData = { _, _ -> PD_EMPTY_MAP_DATA },
                    getCartographyEntryOfflineRegion = { _, _ -> null },
                    getCartographyEntryCurrentLocation = { LocationResult.LocationUnavailable },
                    // What MainActivity passes: the real state, and the pending-delete request.
                    availabilityUiState = availability,
                    distanceUnit = DistanceUnit.MILES,
                    currentTime = CurrentTimeProvider { 0L },
                    onOfflineMapLatChanged = {},
                    onOfflineMapLngChanged = {},
                    onOfflineMapRadiusChanged = {},
                    onOfflineMapNameChanged = {},
                    onOfflineMapsOpened = {},
                    onDownloadOfflineMaps = {},
                    onDeleteOfflineRegion = availabilityViewModel::requestDeleteOfflineRegion,
                    tracks = track.tracks,
                    onTracksOpened = {},
                    // What MainActivity passes: the visible list, and the pending-delete request.
                    waypoints = track.visibleWaypoints,
                    waypointsErrorMessage = track.waypointsErrorMessage,
                    onDeleteWaypoint = trackViewModel::requestRemoveWaypoint,
                    waypointEntryReferenceCounts = track.waypointEntryReferenceCounts,
                    journalState = journalState,
                )
                SnackbarHost(hostState, modifier = Modifier.align(Alignment.BottomCenter))
            }
            PendingDeleteSnackbarEffects(
                notices = listOfNotNull(
                    waypointDeleteNotice(track.pendingWaypointDelete, trackViewModel::undoRemoveWaypoint, trackViewModel::commitRemoveWaypoint),
                    offlineRegionDeleteNotice(
                        availability.pendingOfflineRegionDelete,
                        availabilityViewModel::undoDeleteOfflineRegion,
                        availabilityViewModel::commitDeleteOfflineRegion,
                    ),
                ),
                hostState = hostState,
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Records").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(chip)).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    private fun swipeRowLeft(type: RecordType, id: String) {
        composeRule.onNodeWithTag(swipeToDeleteTag(type, id)).performScrollTo().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    /** Past [androidx.compose.material3.SnackbarDuration.Long] (10 s) and the exit animation. */
    private fun letSnackbarTimeOut() {
        composeRule.mainClock.advanceTimeBy(SNACKBAR_LONG_MILLIS + 1_000L)
        composeRule.waitForIdle()
    }

    private fun touchUndo() {
        composeRule.onNodeWithText("Undo").assert(hasClickAction()).performTouchInput { click() }
        composeRule.waitForIdle()
    }

    // ── Waypoints (D2) ──

    @Test
    fun `swiping a waypoint row end to start hides it, warns of its references, and deletes nothing yet`() {
        setScreen()

        // The top row, then (after Undo) the second: more than one row position.
        swipeRowLeft(RecordType.WAYPOINTS, "wp-creek")
        composeRule.onNodeWithText("Waypoint deleted · used in 2 journal entries").assertIsDisplayed()
        composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.WAYPOINTS, "wp-creek")).assertDoesNotExist()
        composeRule.onNodeWithText("Creek pin").assertDoesNotExist()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
        touchUndo()

        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")
        composeRule.onNodeWithText("Waypoint deleted · used in 1 journal entry").assertIsDisplayed()
        composeRule.onNodeWithText("Big oak").assertDoesNotExist()
        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
    }

    @Test
    fun `a waypoint no entry uses says only Waypoint deleted`() {
        setScreen(waypointReferenceCounts = emptyMap())

        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")

        composeRule.onNodeWithText("Waypoint deleted").assertIsDisplayed()
        composeRule.onNodeWithText("used in", substring = true).assertDoesNotExist()
    }

    @Test
    fun `the waypoint row has no trash icon any more`() {
        setScreen()
        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
        composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.WAYPOINTS, "wp-creek")).assertExists()
        composeRule.onNode(androidx.compose.ui.test.hasContentDescription("Remove waypoint Creek pin")).assertDoesNotExist()
    }

    @Test
    fun `swiping start to end does not delete a waypoint`() {
        setScreen()

        composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.WAYPOINTS, "wp-creek")).performScrollTo().performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
        composeRule.onNodeWithText("Waypoint deleted", substring = true).assertDoesNotExist()
        letSnackbarTimeOut()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
    }

    @Test
    fun `Undo brings the waypoint row back and deletes nothing, even after the timeout would have passed`() {
        setScreen()
        swipeRowLeft(RecordType.WAYPOINTS, "wp-creek")

        touchUndo()

        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
        composeRule.onNodeWithText("Waypoint deleted", substring = true).assertDoesNotExist()
        letSnackbarTimeOut()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
    }

    @Test
    fun `when the snackbar times out, exactly one delete runs, with the swiped waypoint's id`() {
        setScreen()
        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)

        letSnackbarTimeOut()

        assertEquals(listOf("wp-oak"), waypointRepository.deletedIds)
        composeRule.onNodeWithText("Waypoint deleted", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Big oak").assertDoesNotExist()
        composeRule.onNodeWithText("Creek pin").assertIsDisplayed()
    }

    @Test
    fun `a second swipe commits the first waypoint when its snackbar replaces the first`() {
        setScreen()
        swipeRowLeft(RecordType.WAYPOINTS, "wp-creek")

        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")

        assertEquals(listOf("wp-creek"), waypointRepository.deletedIds)
        composeRule.onNodeWithText("Waypoint deleted · used in 1 journal entry").assertIsDisplayed()
        composeRule.onNodeWithText("Waypoint deleted · used in 2 journal entries").assertDoesNotExist()
        letSnackbarTimeOut()
        assertEquals(listOf("wp-creek", "wp-oak"), waypointRepository.deletedIds)
    }

    @Test
    fun `the waypoint row's Delete accessibility action does the same pending delete`() {
        setScreen()
        val node = composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.WAYPOINTS, "wp-oak")).performScrollTo().fetchSemanticsNode()
        val delete = node.config[SemanticsActions.CustomActions].single { it.label == "Delete" }

        composeRule.runOnUiThread { delete.action() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Waypoint deleted · used in 1 journal entry").assertIsDisplayed()
        composeRule.onNodeWithText("Big oak").assertDoesNotExist()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
        letSnackbarTimeOut()
        assertEquals(listOf("wp-oak"), waypointRepository.deletedIds)
    }

    @Test
    fun `a pending waypoint is gone from its chip count, from All, and from All's count`() {
        setScreen(chip = RecordsSubTab.ALL)
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.WAYPOINTS)).assert(hasText("2"))
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("4"))
        composeRule.onNodeWithTag(logbookRowTag(RecordType.WAYPOINTS, "wp-oak")).assertExists()

        // Swiped in All itself: the logbook's row is swipeable too.
        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")

        composeRule.onNodeWithTag(logbookRowTag(RecordType.WAYPOINTS, "wp-oak")).assertDoesNotExist()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.WAYPOINTS)).assert(hasText("1"))
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("3"))
        composeRule.onNodeWithText("Waypoint deleted · used in 1 journal entry").assertIsDisplayed()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
    }
    // ── Offline regions (D3) ──

    @Test
    fun `swiping a region row hides it, warns of its references, and runs no tile delete yet`() {
        setScreen(chip = RecordsSubTab.OFFLINE_MAPS)
        composeRule.onNodeWithText("Tile budget: 150", substring = true).assertExists()

        // Two row positions: the first region, then (after Undo) the second.
        swipeRowLeft(RecordType.OFFLINE_MAPS, "7")
        composeRule.onNodeWithText("Offline map deleted · used in 2 journal entries").assertIsDisplayed()
        composeRule.onNodeWithText("Ridge").assertDoesNotExist()
        // Its tiles are still on disk, so the budget still counts them until the delete runs.
        composeRule.onNodeWithText("Tile budget: 150", substring = true).assertExists()
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
        touchUndo()
        composeRule.onNodeWithText("Ridge").assertExists()

        swipeRowLeft(RecordType.OFFLINE_MAPS, "8")
        composeRule.onNodeWithText("Offline map deleted").assertIsDisplayed()
        composeRule.onNodeWithText("Saddle").assertDoesNotExist()
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
    }

    @Test
    fun `the region row has no Delete button any more`() {
        setScreen(chip = RecordsSubTab.OFFLINE_MAPS)
        composeRule.onNodeWithText("Ridge").assertExists()
        composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.OFFLINE_MAPS, "7")).assertExists()
        composeRule.onNode(hasText("Delete") and hasClickAction()).assertDoesNotExist()
    }

    @Test
    fun `Undo brings the region back and deletes no tiles, even after the timeout`() {
        setScreen(chip = RecordsSubTab.OFFLINE_MAPS)
        swipeRowLeft(RecordType.OFFLINE_MAPS, "7")

        touchUndo()
        letSnackbarTimeOut()

        composeRule.onNodeWithText("Ridge").assertExists()
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
    }

    @Test
    fun `the region's tile delete runs once, with its id, only when the snackbar times out`() {
        setScreen(chip = RecordsSubTab.OFFLINE_MAPS)
        swipeRowLeft(RecordType.OFFLINE_MAPS, "7")
        composeRule.mainClock.advanceTimeBy(SNACKBAR_LONG_MILLIS / 2)
        composeRule.waitForIdle()
        assertEquals("not before the snackbar ends", emptyList<Long>(), offlineMapRepository.deletedIds)

        letSnackbarTimeOut()

        assertEquals(listOf(7L), offlineMapRepository.deletedIds)
        composeRule.onNodeWithText("Ridge").assertDoesNotExist()
        composeRule.onNodeWithText("Saddle").assertExists()
    }

    @Test
    fun `the region row's Delete accessibility action does the same pending delete`() {
        setScreen(chip = RecordsSubTab.OFFLINE_MAPS)
        val node = composeRule.onNodeWithTag(swipeToDeleteTag(RecordType.OFFLINE_MAPS, "8")).performScrollTo().fetchSemanticsNode()
        val delete = node.config[SemanticsActions.CustomActions].single { it.label == "Delete" }

        composeRule.runOnUiThread { delete.action() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Offline map deleted").assertIsDisplayed()
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
        letSnackbarTimeOut()
        assertEquals(listOf(8L), offlineMapRepository.deletedIds)
    }

    @Test
    fun `a pending region is gone from its chip count, from All, and from All's count`() {
        setScreen(chip = RecordsSubTab.ALL)
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.OFFLINE_MAPS)).assert(hasText("2"))
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("4"))

        swipeRowLeft(RecordType.OFFLINE_MAPS, "8")

        composeRule.onNodeWithTag(logbookRowTag(RecordType.OFFLINE_MAPS, "8")).assertDoesNotExist()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.OFFLINE_MAPS)).assert(hasText("1"))
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("3"))
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
    }

    @Test
    fun `a region swipe after a waypoint swipe commits the waypoint when the region's snackbar replaces it`() {
        setScreen(chip = RecordsSubTab.ALL)
        swipeRowLeft(RecordType.WAYPOINTS, "wp-creek")
        composeRule.onNodeWithText("Waypoint deleted · used in 2 journal entries").assertIsDisplayed()

        swipeRowLeft(RecordType.OFFLINE_MAPS, "7")

        assertEquals(listOf("wp-creek"), waypointRepository.deletedIds)
        composeRule.onNodeWithText("Offline map deleted · used in 2 journal entries").assertIsDisplayed()
        composeRule.onNodeWithText("Waypoint deleted · used in 2 journal entries").assertDoesNotExist()
        assertEquals(emptyList<Long>(), offlineMapRepository.deletedIds)
        letSnackbarTimeOut()
        assertEquals(listOf(7L), offlineMapRepository.deletedIds)
    }
}

private const val SNACKBAR_LONG_MILLIS = 10_000L

private val PD_TIME = CurrentTimeProvider { 1_000L }

private val PD_WAYPOINTS = listOf(
    Waypoint(id = "wp-creek", lat = 45.5, lng = -122.6, altitude = null, name = "Creek pin", note = "", createdAtEpochMillis = 1_758_100_000_000L),
    Waypoint(id = "wp-oak", lat = 45.6, lng = -122.7, altitude = null, name = "Big oak", note = "", createdAtEpochMillis = 1_758_000_000_000L),
)

private val PD_EMPTY_MAP_DATA = CartographyEntryMapData(
    trackPolylines = emptyList(),
    findMarkers = emptyList(),
    waypointMarkers = emptyList(),
    photoMarkers = emptyList(),
    offlineRegionCircles = emptyList(),
)

private val PD_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

private object PendingDeleteNoOpLocationTracker : LocationTracker {
    override val fixes: Flow<LocationFix> = emptyFlow()
}

private object PendingDeleteAudible : AlertAudibility {
    override fun current(): AlertAudibilityState =
        AlertAudibilityState(ringerMode = RingerMode.NORMAL, doNotDisturbOn = false, notificationsEnabled = true)
}

/** Waypoints kept in memory; every [delete] call is recorded, in order, for the tests to read. */
private class PendingDeleteWaypointRepository(initial: List<Waypoint>) : WaypointRepository {
    private val waypoints = initial.associateByTo(LinkedHashMap()) { it.id }
    val deletedIds = mutableListOf<String>()

    override suspend fun getAll(): Result<List<Waypoint>> = Result.success(waypoints.values.toList())
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Waypoint>> =
        Result.failure(UnsupportedOperationException("getForDay is not part of this test's path"))
    override suspend fun getById(id: String): Result<Waypoint?> = Result.success(waypoints[id])
    override suspend fun getForTrack(trackId: String): Result<List<Waypoint>> =
        Result.failure(UnsupportedOperationException("getForTrack is not part of this test's path"))
    override suspend fun detachFromTrack(trackId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("detachFromTrack is not part of this test's path"))
    override suspend fun save(waypoint: Waypoint): Result<Unit> {
        waypoints[waypoint.id] = waypoint
        return Result.success(Unit)
    }
    override suspend fun delete(id: String): Result<Unit> {
        deletedIds += id
        waypoints.remove(id)
        return Result.success(Unit)
    }
}

/** No tracks; nothing here records one. */
private class PendingDeleteTrackRepository : TrackRepository {
    override suspend fun getAll(): Result<List<Track>> = Result.success(emptyList())
    override suspend fun getById(id: String): Result<Track?> = Result.success(null)
    override suspend fun getFullRecord(id: String): Result<List<TrackPointRecord>> = Result.success(emptyList())
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Track>> =
        Result.success(emptyList())
    override suspend fun create(track: Track): Result<Unit> = Result.failure(UnsupportedOperationException("recording is not part of this test's path"))
    override suspend fun appendPoints(trackId: String, points: List<TrackPoint>): Result<Unit> = Result.success(Unit)
    override suspend fun end(trackId: String, endedAtEpochMillis: Long): Result<Unit> = Result.success(Unit)
    override suspend fun setOriginWaypoint(trackId: String, waypointId: String): Result<Unit> = Result.success(Unit)
    override suspend fun delete(id: String): Result<Unit> = Result.failure(UnsupportedOperationException("tracks are not deletable"))
}

private val PD_REGIONS = listOf(
    OfflineRegionSummary(id = 7L, name = "Ridge", region = Region(45.5, -122.6, 5), minZoom = 10.0, maxZoom = 15.0, tileCount = 100, sizeBytes = 1_000_000L, createdAtEpochMillis = 1_758_300_000_000L),
    OfflineRegionSummary(id = 8L, name = "Saddle", region = Region(45.3, -122.4, 5), minZoom = 10.0, maxZoom = 15.0, tileCount = 50, sizeBytes = 1_000_000L, createdAtEpochMillis = 1_758_200_000_000L),
)

/** Regions kept in memory; every [deleteRegion] call (MapLibre's tile delete and the row, in the real one) is recorded. */
private class PendingDeleteOfflineMapRepository(initial: List<OfflineRegionSummary>) : OfflineMapRepository {
    private val regions = initial.toMutableList()
    val deletedIds = mutableListOf<Long>()

    override suspend fun download(name: String, region: Region, onProgress: (downloaded: Int, total: Int) -> Unit): Result<OfflineRegionSummary> =
        Result.failure(UnsupportedOperationException("downloading is not part of this test's path"))
    override suspend fun deleteRegion(id: Long): Result<Unit> {
        deletedIds += id
        regions.removeAll { it.id == id }
        return Result.success(Unit)
    }
    override suspend fun listRegions(): Result<List<OfflineRegionSummary>> = Result.success(regions.toList())
}

private object PendingDeleteUnusedLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): LocationResult = LocationResult.LocationUnavailable
}

private object PendingDeleteEmptyRepository : MushroomRepository, TaxonSearchRepository {
    override suspend fun getSpeciesCounts(region: Region, month: Int, filter: TaxonFilter) =
        Result.success(emptyList<SpeciesObservationCount>())
    override suspend fun getSightings(region: Region, month: Int, filter: TaxonFilter) =
        Result.success(SightingsPage(sightings = emptyList<Sighting>(), totalResults = 0))
    override suspend fun searchTaxa(query: String) = Result.success(emptyList<TaxonSearchResult>())
}

private object PendingDeleteWeather : WeatherProvider, TripPlanningWeatherProvider, HistoricalWeatherProvider {
    override suspend fun getRecentPrecipitation(region: Region) =
        Result.success(ConditionsSummary(region = region, totalPrecipitationMm = 0.0, daysSinceSignificantRain = null))
    override suspend fun getWeatherSeries(region: Region): Result<WeatherSeries> =
        Result.failure(UnsupportedOperationException("trip windows are not part of this test's path"))
    override suspend fun getHistoricalPrecipitation(region: Region, from: LocalDate, through: LocalDate): Result<List<DailyWeather>> =
        Result.failure(UnsupportedOperationException("seasonal pattern is not part of this test's path"))
}

private object PendingDeletePlannedTrips : PlannedTripRepository {
    override suspend fun getAll(): Result<List<PlannedTrip>> = Result.success(emptyList())
    override suspend fun save(trip: PlannedTrip): Result<Unit> = Result.failure(UnsupportedOperationException("planned trips are not part of this test's path"))
    override suspend fun delete(id: String): Result<Unit> = Result.failure(UnsupportedOperationException("planned trips are not part of this test's path"))
}

private object PendingDeleteMapPreferences : MapPreferencesRepository {
    override suspend fun getLastPickedRegion(): Result<Region?> = Result.success(null)
    override suspend fun setLastPickedRegion(region: Region): Result<Unit> = Result.success(Unit)
    override suspend fun getStaleThresholdDays(): Result<Int> = Result.success(DEFAULT_STALE_THRESHOLD_DAYS)
    override suspend fun setStaleThresholdDays(days: Int): Result<Unit> = Result.success(Unit)
    override suspend fun getNightModeMaps(): Result<Boolean> = Result.success(false)
    override suspend fun setNightModeMaps(night: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun getMapFullscreen(): Result<Boolean> = Result.success(false)
    override suspend fun setMapFullscreen(fullscreen: Boolean): Result<Unit> = Result.success(Unit)
}

private object PendingDeleteUnitSystem : UnitSystemPreferenceRepository {
    override suspend fun getUnitSystem(): Result<UnitSystem> = Result.success(UnitSystem.IMPERIAL)
    override suspend fun setUnitSystem(system: UnitSystem): Result<Unit> = Result.success(Unit)
}

private object PendingDeleteTheme : AppThemePreferenceRepository {
    override suspend fun getThemeMode(): Result<AppThemeMode> = Result.success(AppThemeMode.LIGHT)
    override suspend fun setThemeMode(mode: AppThemeMode): Result<Unit> = Result.success(Unit)
}
