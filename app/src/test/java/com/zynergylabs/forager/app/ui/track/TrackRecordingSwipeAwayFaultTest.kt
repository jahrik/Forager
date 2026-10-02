package com.zynergylabs.forager.app.ui.track

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zynergylabs.forager.app.domain.Alert
import com.zynergylabs.forager.app.domain.AlertAudibility
import com.zynergylabs.forager.app.domain.AlertAudibilityState
import com.zynergylabs.forager.app.domain.AlertDelivery
import com.zynergylabs.forager.app.domain.AlertKind
import com.zynergylabs.forager.app.domain.ComputeReturnToStartUseCase
import com.zynergylabs.forager.app.domain.CreateWaypointUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeleteTrackUseCase
import com.zynergylabs.forager.app.domain.DeleteWaypointUseCase
import com.zynergylabs.forager.app.domain.DetectOffTrackUseCase
import com.zynergylabs.forager.app.domain.GetTracksUseCase
import com.zynergylabs.forager.app.domain.GetWaypointsUseCase
import com.zynergylabs.forager.app.domain.InMemoryKeptTrackPaths
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.LocationTracker
import com.zynergylabs.forager.app.domain.RingerMode
import com.zynergylabs.forager.app.domain.StartTrackUseCase
import com.zynergylabs.forager.app.domain.TrackRepository
import com.zynergylabs.forager.app.domain.WaypointRepository
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import com.zynergylabs.forager.app.domain.model.Waypoint
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * **These tests describe a fault. A green run here does not mean "works"; it means the fault is
 * still there, exactly as written down.** Dispatch 2026-09-28-400, Part 1 (plan task T1,
 * `prompts/preserved/2026-10-02-01.md`): pin today's behaviour before anything is built. They are
 * replaced, not deleted, when the fix lands, and each one says which assertion is expected to
 * turn red then.
 *
 * The fault, in the user's terms: a recording is running and the walker has tapped Return. The app
 * is swiped away from recents. The Activity is destroyed and its [TrackRecordingViewModel] is
 * cleared, and the off-track decision, the returning flag, the rolling window and the cooldown all
 * live in that ViewModel (`domain/AlertDelivery.kt`'s header). The recording service keeps writing
 * points; nothing will say "Off track" again for that recording. Reopening the app builds a new
 * ViewModel that reads none of this back.
 *
 * ## What stands in for the swipe-away
 *
 * `ViewModelStore.clear()`. That is what an Activity's own store does when the Activity is
 * destroyed for good, and it is the route `TrackRecordingViewModelTest`'s pending-delete test
 * already uses to reach `onCleared`. What no test in this file reaches: whether the platform
 * really destroys the Activity and keeps the process on a swipe from recents. That is the phone's.
 *
 * ## Why the fixes arrive through the tracker and not through `returnToStart`
 *
 * `returnToStart` is public and a test could call it on a cleared ViewModel, and it would deliver.
 * Nothing in the app does that: after the clear, the only caller it ever had
 * (`beginLocationTracking`'s collector) is cancelled. So the readings here go in the way the
 * platform sends them, through [LocationTracker.fixes], and the test also asserts how many
 * collectors that stream has. A reading nobody is subscribed to is the fault itself, seen directly.
 *
 * ## The poll loop
 *
 * `beginPolling` is an unbounded `delay` loop while recording (`CLAUDE.md`, "A stalled test run in
 * this repo is an unstopped poll loop"). [runRecordingTest] stops every ViewModel this class
 * built, in a `finally`, inside the test body. Rebuilt here, not inherited, because
 * `TrackRecordingViewModelTest` is not open and its fixtures are private to its file. Only
 * `runCurrent`, never `advanceUntilIdle`, once a recording has started.
 */
class TrackRecordingSwipeAwayFaultTest {

    private val dispatcher = StandardTestDispatcher()
    private val createdViewModels = mutableListOf<TrackRecordingViewModel>()

    @Before fun setUpMain() = Dispatchers.setMain(dispatcher)

    @After fun tearDownMain() = Dispatchers.resetMain()

    private fun runRecordingTest(body: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            body()
        } finally {
            createdViewModels.forEach { it.stopRecording() }
        }
    }

    /** Every alert any ViewModel in a test handed over, in order. One instance per test method. */
    private val delivered = mutableListOf<Alert>()

    /** Storage and the fix stream: what outlives a destroyed Activity, shared by both ViewModels of a test. */
    private val tracks = SwipeAwayTracks()
    private val waypoints = SwipeAwayWaypoints()
    private val fixes = MutableSharedFlow<LocationFix>()
    private var trackIds = 0
    private var waypointIds = 0
    private val clock = CurrentTimeProvider { 1_000L }

    private fun buildViewModel() = TrackRecordingViewModel(
        trackRepository = tracks,
        // A counter, not a fixed id: the second-recording test needs the second row to be a second row.
        startTrack = StartTrackUseCase(tracks, currentTime = clock, idGenerator = { "track-${++trackIds}" }),
        getWaypoints = GetWaypointsUseCase(waypoints),
        createWaypoint = CreateWaypointUseCase(waypoints, currentTime = clock, idGenerator = { "waypoint-${++waypointIds}" }),
        deleteWaypoint = DeleteWaypointUseCase(waypoints),
        deleteTrack = DeleteTrackUseCase(tracks, waypoints, InMemoryKeptTrackPaths()),
        computeReturnToStart = ComputeReturnToStartUseCase(),
        detectOffTrack = DetectOffTrackUseCase(),
        locationTracker = object : LocationTracker { override val fixes = this@TrackRecordingSwipeAwayFaultTest.fixes },
        getTracks = GetTracksUseCase(tracks),
        alertDelivery = AlertDelivery { delivered += it },
        alertAudibility = object : AlertAudibility {
            override fun current() = AlertAudibilityState(RingerMode.NORMAL, doNotDisturbOn = false, notificationsEnabled = true)
        },
        currentTime = clock,
        zone = ZoneOffset.UTC,
    ).also(createdViewModels::add)

    /** A ViewModel held the way an Activity holds one, so [ViewModelStore.clear] reaches its `onCleared`. */
    private fun viewModelIn(store: ViewModelStore): TrackRecordingViewModel =
        ViewModelProvider(store, viewModelFactory { initializer { buildViewModel() } })[TrackRecordingViewModel::class.java]

    private fun fix(lat: Double, t: Long) =
        LocationFix.Update(lat = lat, lng = -122.0, altitude = null, accuracyMeters = 5f, timestampEpochMillis = t)

    /**
     * Start a recording, take the first gated fix as the origin at 45.000, tap Return, and take
     * one reading 111 m north of the start. One reading is below the three-reading window, so
     * nothing has alerted yet. Everything through the real entry points: `startRecording`,
     * the tracker's stream, `startReturn`.
     */
    private suspend fun TestScope.recordAndStartReturning(vm: TrackRecordingViewModel) {
        vm.startRecording(TrackRecordingMode.HIGH_ACCURACY)
        runCurrent()
        fixes.emit(fix(lat = 45.000, t = 2_000L))
        runCurrent()
        assertEquals("precondition: the origin is seeded from the first gated fix", 45.000, vm.uiState.value.originWaypoint?.lat)
        vm.startReturn()
        runCurrent()
        fixes.emit(fix(lat = 45.001, t = 7_000L))
        runCurrent()
        assertTrue("precondition: a return is under way", vm.uiState.value.isReturning)
        assertEquals("precondition: one reading is not yet a trend, so nothing has been delivered", 0, delivered.size)
    }

    /**
     * The readings that follow: each about 111 m further from the start than the last. With the
     * reading [recordAndStartReturning] already took, the second of these completes a three-reading
     * window whose net increase is far past the 25 m threshold. Shared by the control and the
     * fault test so the two cannot drift into using different readings.
     */
    private suspend fun TestScope.walkAwayFromTheStart() {
        fixes.emit(fix(lat = 45.002, t = 12_000L))
        runCurrent()
        fixes.emit(fix(lat = 45.003, t = 17_000L))
        runCurrent()
        fixes.emit(fix(lat = 45.004, t = 22_000L))
        runCurrent()
    }

    /**
     * **The control, not the fault.** With the ViewModel alive, the readings in
     * [walkAwayFromTheStart] deliver exactly one off-track alert. Without this, the fault test
     * below could pass on readings that would never have alerted anyone, which is the "check that
     * never saw the data that could fail it" family in `CLAUDE.md`. This one is expected to stay
     * green after the fix.
     */
    @Test
    fun `control - with the screen's ViewModel alive, walking away from the start delivers exactly one off-track alert`() = runRecordingTest {
        val vm = viewModelIn(ViewModelStore())
        recordAndStartReturning(vm)

        walkAwayFromTheStart()

        assertEquals(listOf(Alert(kind = AlertKind.OFF_TRACK, overridesSilence = false)), delivered)
        assertTrue(vm.uiState.value.isOffTrack)
        vm.stopRecording()
    }

    /**
     * Dispatch 2026-09-28-400, the first path:
     * `Recording, returning > the app is swiped away > the walker moves away > the alert arrives`.
     * **Today nothing arrives.** The same readings as the control above, after the ViewModel is
     * cleared: no collector is left on the fix stream, and nothing is delivered.
     *
     * The track row is still open when the ViewModel is cleared: nothing in `onCleared` ends it,
     * and the service (not in this test) is what goes on writing to it.
     *
     * **When the fix lands:** the delivery count here must become 1, from whatever then owns the
     * decision, with no ViewModel alive. That is this test's replacement.
     */
    @Test
    fun `FAULT today - with a return under way and the ViewModel cleared, readings that would alert deliver nothing`() = runRecordingTest {
        val store = ViewModelStore()
        val vm = viewModelIn(store)
        recordAndStartReturning(vm)
        assertEquals("precondition: the ViewModel is the stream's one collector", 1, fixes.subscriptionCount.value)

        // The swipe-away, as far as this ViewModel can tell: the Activity's store is cleared.
        store.clear()
        runCurrent()

        assertEquals(
            "the cleared ViewModel was the only thing reading fixes for the off-track decision, and it has stopped",
            0,
            fixes.subscriptionCount.value,
        )
        assertNull(
            "the track is still open in storage: clearing the ViewModel does not end the recording",
            tracks.getById("track-1").getOrThrow()?.endedAtEpochMillis,
        )

        walkAwayFromTheStart()

        assertEquals(
            "FAULT: the walker moved steadily away from the start on a return, and no alert was delivered, " +
                "because the decision died with the ViewModel",
            emptyList<Alert>(),
            delivered,
        )
    }

    /**
     * Dispatch premise 3, the ViewModel's half: a ViewModel built after the first was cleared does
     * not know a recording is still open. `init` loads waypoints and tracks and nothing else, and
     * `resyncRecordingState` returns at once when `activeTrack` is null, so entering the
     * foreground changes nothing.
     *
     * The one trace the new ViewModel does hold is the open row in [TrackRecordingUiState.tracks]
     * (end time null). Nothing reads that as "a recording is running".
     *
     * **When the fix lands:** this is the second path in the dispatch, which the owner has not
     * confirmed. If a reopened app is to show the recording, `isRecording` here becomes true.
     */
    @Test
    fun `FAULT today - a ViewModel built while a track is still open reports not recording, even after it enters the foreground`() = runRecordingTest {
        val firstStore = ViewModelStore()
        recordAndStartReturning(viewModelIn(firstStore))
        firstStore.clear()
        runCurrent()

        // Forager is opened again: a new Activity, so a new store and a new ViewModel over the same storage.
        val reopened = viewModelIn(ViewModelStore())
        runCurrent()
        reopened.onEnteredForeground() // what MainActivity's lifecycle observer calls on ON_START
        runCurrent()

        val openRow = tracks.getById("track-1").getOrThrow()
        assertNull("precondition: the first recording's row is still open, as it is while the service records", openRow?.endedAtEpochMillis)

        val state = reopened.uiState.value
        assertFalse("FAULT: the record button reads this, and it says nothing is recording", state.isRecording)
        assertNull(state.activeTrack)
        assertFalse("FAULT: the return that was under way is not known to the reopened screen", state.isReturning)
        assertNull("no start point is known either, so the HUD would have no target", state.originWaypoint)
        assertTrue(state.breadcrumbPoints.isEmpty())
        assertEquals(
            "the open row is loaded into the Records list, with no end time; nothing treats that as a live recording",
            listOf("track-1" to null),
            state.tracks.map { it.id to it.endedAtEpochMillis },
        )
    }

    /**
     * Dispatch premise 4, the ViewModel's half: pressing record on the reopened screen creates a
     * second track row beside the first, which is still open. Nothing in `startRecording` looks
     * for an open row. What the service then does with the second row's `ACTION_START` is pinned
     * in `TrackRecordingServiceSwipeAwayFaultTest`.
     *
     * **When the fix lands:** whether a second row may be created over an open one is part of the
     * second path, which is the owner's. If it may not, the two-row assertion here turns red.
     */
    @Test
    fun `FAULT today - pressing record on the reopened screen creates a second open track row beside the first`() = runRecordingTest {
        val firstStore = ViewModelStore()
        recordAndStartReturning(viewModelIn(firstStore))
        firstStore.clear()
        runCurrent()
        val reopened = viewModelIn(ViewModelStore())
        runCurrent()

        reopened.startRecording(TrackRecordingMode.HIGH_ACCURACY) // what the record button calls when isRecording is false
        runCurrent()

        assertEquals("the screen now shows the second track as the one being recorded", "track-2", reopened.uiState.value.activeTrack?.trackId)
        assertEquals(
            "FAULT: two track rows are open at once",
            listOf("track-1" to null, "track-2" to null),
            tracks.getAll().getOrThrow().sortedBy { it.id }.map { it.id to it.endedAtEpochMillis },
        )
        reopened.stopRecording()
    }
}

private class SwipeAwayTracks : TrackRepository {
    private val tracks = mutableMapOf<String, Track>()
    override suspend fun getAll(): Result<List<Track>> = Result.success(tracks.values.toList())
    override suspend fun getById(id: String): Result<Track?> = Result.success(tracks[id])
    override suspend fun getFullRecord(id: String): Result<List<TrackPointRecord>> =
        Result.failure(UnsupportedOperationException("getFullRecord is not part of this test's path"))
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Track>> =
        Result.failure(UnsupportedOperationException("getForDay is not part of this test's path"))
    override suspend fun create(track: Track): Result<Unit> = Result.success(Unit).also { tracks[track.id] = track }
    override suspend fun appendPoints(trackId: String, points: List<TrackPoint>): Result<Unit> =
        Result.failure(UnsupportedOperationException("appendPoints is the service's write, and no service runs in this test"))
    override suspend fun end(trackId: String, endedAtEpochMillis: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException("end is the service's write, and no service runs in this test"))
    override suspend fun setOriginWaypoint(trackId: String, waypointId: String): Result<Unit> {
        val existing = tracks[trackId] ?: return Result.failure(IllegalStateException("no such track"))
        tracks[trackId] = existing.copy(originWaypointId = waypointId)
        return Result.success(Unit)
    }
    override suspend fun delete(id: String): Result<Unit> = Result.success(Unit).also { tracks.remove(id) }
}

private class SwipeAwayWaypoints : WaypointRepository {
    private val saved = mutableMapOf<String, Waypoint>()
    override suspend fun getAll(): Result<List<Waypoint>> = Result.success(saved.values.toList())
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Waypoint>> =
        Result.failure(UnsupportedOperationException("getForDay is not part of this test's path"))
    override suspend fun getById(id: String): Result<Waypoint?> = Result.success(saved[id])
    override suspend fun getForTrack(trackId: String): Result<List<Waypoint>> = Result.success(saved.values.filter { it.trackId == trackId })
    override suspend fun detachFromTrack(trackId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("detachFromTrack is not part of this test's path"))
    override suspend fun save(waypoint: Waypoint): Result<Unit> = Result.success(Unit).also { saved[waypoint.id] = waypoint }
    override suspend fun delete(id: String): Result<Unit> = Result.success(Unit).also { saved.remove(id) }
}
