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
import com.zynergylabs.forager.app.ui.availability.AvailabilityUiState
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

    private fun setScreen(
        waypointReferenceCounts: Map<String, Int> = mapOf("wp-creek" to 2, "wp-oak" to 1),
        chip: RecordsSubTab = RecordsSubTab.WAYPOINTS,
    ) {
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
                    availabilityUiState = AvailabilityUiState(),
                    distanceUnit = DistanceUnit.MILES,
                    currentTime = CurrentTimeProvider { 0L },
                    onOfflineMapLatChanged = {},
                    onOfflineMapLngChanged = {},
                    onOfflineMapRadiusChanged = {},
                    onOfflineMapNameChanged = {},
                    onOfflineMapsOpened = {},
                    onDownloadOfflineMaps = {},
                    onDeleteOfflineRegion = {},
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
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("2"))
        composeRule.onNodeWithTag(logbookRowTag(RecordType.WAYPOINTS, "wp-oak")).assertExists()

        // Swiped in All itself: the logbook's row is swipeable too.
        swipeRowLeft(RecordType.WAYPOINTS, "wp-oak")

        composeRule.onNodeWithTag(logbookRowTag(RecordType.WAYPOINTS, "wp-oak")).assertDoesNotExist()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.WAYPOINTS)).assert(hasText("1"))
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.ALL)).assert(hasText("1"))
        composeRule.onNodeWithText("Waypoint deleted · used in 1 journal entry").assertIsDisplayed()
        assertEquals(emptyList<String>(), waypointRepository.deletedIds)
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
