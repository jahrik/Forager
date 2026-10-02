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
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.ReturnWatch
import com.zynergylabs.forager.app.domain.ReturnWatchState
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
import kotlinx.coroutines.test.advanceTimeBy
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
 * The screen's ViewModel and [ReturnWatch] together, on a plain JVM, around the moment the app is
 * swiped away from recents (dispatch 2026-09-28-400; Part 1 pinned the fault here, Amendment 2
 * moved the off-track decision out of the ViewModel).
 *
 * **What moved.** The decision, the returning flag, the rolling window and the cooldown are
 * [ReturnWatch]'s, held by the app's container and driven by the recording service. The ViewModel
 * calls it for Return and copies its state. So when the Activity is destroyed and this ViewModel
 * is cleared, the decision is still there, and it still delivers as long as something feeds it.
 *
 * **Who stands in for the service here.** No service runs on a plain JVM. [serviceBegins] and the
 * watch half of [fixArrives] are the three calls `TrackRecordingService` makes (begin, every raw
 * fix, end), made by hand. The real service making them is
 * `TrackRecordingServiceSwipeAwayTest`. A real fix reaches two listeners, the service's and the
 * ViewModel's own, so [fixArrives] sends each fix both ways.
 *
 * **What stands in for the swipe-away:** `ViewModelStore.clear()`, as in Part 1. On the S22 the
 * real swipe was checked on 2026-10-02 (the report's "Phone check").
 *
 * ## Two tests here still pin a fault, and say so in their names
 *
 * The reopened screen (the dispatch's second path) is Part 3. Until then a ViewModel built after
 * the swipe does not know a recording is running, and pressing Record makes a second track. Those
 * two tests are green while that is still true. They are replaced when Part 3 lands.
 *
 * ## The poll loop
 *
 * `beginPolling` is an unbounded `delay` loop while recording (`CLAUDE.md`, "A stalled test run in
 * this repo is an unstopped poll loop"). [runRecordingTest] stops every ViewModel this class
 * built, in a `finally`, inside the test body. Only `runCurrent` and `advanceTimeBy`, never
 * `advanceUntilIdle`, once a recording has started.
 */
class TrackRecordingSwipeAwayTest {

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

    /** Every alert handed over, in order. One instance per test method. */
    private val delivered = mutableListOf<Alert>()

    /** What the ViewModels logged: tag and message. */
    private val logged = mutableListOf<String>()

    /** Storage, the fix stream and the watch: what outlives a destroyed Activity, shared by both ViewModels of a test. */
    private val tracks = SwipeAwayTracks()
    private val waypoints = SwipeAwayWaypoints()
    private val fixes = MutableSharedFlow<LocationFix>()
    private var trackIds = 0
    private var waypointIds = 0
    private val clock = CurrentTimeProvider { 1_000L }
    private val returnWatch = ReturnWatch(ComputeReturnToStartUseCase(), DetectOffTrackUseCase(), { delivered += it }, clock)

    /** Whether a service is recording, in this test's world: only then does a fix reach the watch. */
    private var serviceRecording = false

    private fun buildViewModel() = TrackRecordingViewModel(
        trackRepository = tracks,
        // A counter, not a fixed id: the second-recording test needs the second row to be a second row.
        startTrack = StartTrackUseCase(tracks, currentTime = clock, idGenerator = { "track-${++trackIds}" }),
        getWaypoints = GetWaypointsUseCase(waypoints),
        createWaypoint = CreateWaypointUseCase(waypoints, currentTime = clock, idGenerator = { "waypoint-${++waypointIds}" }),
        deleteWaypoint = DeleteWaypointUseCase(waypoints),
        deleteTrack = DeleteTrackUseCase(tracks, waypoints, InMemoryKeptTrackPaths()),
        computeReturnToStart = ComputeReturnToStartUseCase(),
        locationTracker = object : LocationTracker { override val fixes = this@TrackRecordingSwipeAwayTest.fixes },
        getTracks = GetTracksUseCase(tracks),
        returnWatch = returnWatch,
        alertAudibility = object : AlertAudibility {
            override fun current() = AlertAudibilityState(RingerMode.NORMAL, doNotDisturbOn = false, notificationsEnabled = true)
        },
        errorLog = ErrorLog { _, message, _ -> logged += message },
        currentTime = clock,
        zone = ZoneOffset.UTC,
    ).also(createdViewModels::add)

    /** A ViewModel held the way an Activity holds one, so [ViewModelStore.clear] reaches its `onCleared`. */
    private fun viewModelIn(store: ViewModelStore): TrackRecordingViewModel =
        ViewModelProvider(store, viewModelFactory { initializer { buildViewModel() } })[TrackRecordingViewModel::class.java]

    /** `TrackRecordingService.startRecording`'s call. */
    private fun serviceBegins(trackId: String) {
        returnWatch.begin(trackId)
        serviceRecording = true
    }

    /** `TrackRecordingService.stopRecording`'s call. */
    private fun serviceEnds(trackId: String) {
        returnWatch.end(trackId)
        serviceRecording = false
    }

    /** One fix from the platform: to the service's listener (so to the watch) and to the ViewModel's own. */
    private suspend fun TestScope.fixArrives(lat: Double, t: Long) {
        val fix = LocationFix.Update(lat = lat, lng = -122.0, altitude = null, accuracyMeters = 5f, timestampEpochMillis = t)
        if (serviceRecording) returnWatch.onFix(TrackPoint(fix.lat, fix.lng, fix.altitude, fix.accuracyMeters, fix.timestampEpochMillis))
        fixes.emit(fix)
        runCurrent()
    }

    /**
     * Record, the service begins, the first gated fix seeds the origin at 45.000, Return, and one
     * reading 111 m north of the start. One reading is below the three-reading window, so nothing
     * has alerted yet. Everything through the real entry points.
     */
    private suspend fun TestScope.recordAndStartReturning(vm: TrackRecordingViewModel) {
        vm.startRecording(TrackRecordingMode.HIGH_ACCURACY)
        runCurrent()
        serviceBegins(vm.uiState.value.activeTrack!!.trackId)
        fixArrives(lat = 45.000, t = 2_000L)
        assertEquals("precondition: the origin is seeded from the first gated fix", 45.000, vm.uiState.value.originWaypoint?.lat)
        vm.startReturn()
        runCurrent()
        fixArrives(lat = 45.001, t = 7_000L)
        assertTrue("precondition: a return is under way", vm.uiState.value.isReturning)
        assertEquals("precondition: one reading is not yet a trend, so nothing has been delivered", 0, delivered.size)
    }

    /**
     * Each about 111 m further from the start than the last. With the reading
     * [recordAndStartReturning] already took, the second of these completes a three-reading window
     * whose net increase is far past the 25 m threshold.
     */
    private suspend fun TestScope.walkAwayFromTheStart() {
        fixArrives(lat = 45.002, t = 12_000L)
        fixArrives(lat = 45.003, t = 17_000L)
        fixArrives(lat = 45.004, t = 22_000L)
    }

    /**
     * The control from Part 1, kept: with the screen alive, the readings in [walkAwayFromTheStart]
     * deliver exactly one off-track alert. Since Amendment 2 it also shows the copy: the screen's
     * "off track now" is the watch's.
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
     *
     * **This was Part 1's fault test.** It asserted that nothing was delivered. The assertion that
     * turned is the last one: an empty list then, one off-track alert now. The readings are the
     * control's. The ViewModel is cleared and reads no fixes; the decision is the watch's, and the
     * service (stood in for here) goes on feeding it.
     */
    @Test
    fun `with a return under way and the ViewModel cleared, walking away from the start still delivers the off-track alert`() = runRecordingTest {
        val store = ViewModelStore()
        val vm = viewModelIn(store)
        recordAndStartReturning(vm)
        assertEquals("precondition: the ViewModel is the stream's one collector here", 1, fixes.subscriptionCount.value)

        // The swipe-away, as far as this ViewModel can tell: the Activity's store is cleared.
        store.clear()
        runCurrent()

        assertEquals("the cleared ViewModel reads no fixes", 0, fixes.subscriptionCount.value)
        assertTrue("the return is still under way: it is the watch's, and the watch was not cleared", returnWatch.state.value.isReturning)

        walkAwayFromTheStart()

        assertEquals(
            "the walker moved steadily away from the start on a return, with no ViewModel alive, and the alert was delivered",
            listOf(Alert(kind = AlertKind.OFF_TRACK, overridesSilence = false)),
            delivered,
        )
    }

    /**
     * The handover itself (Amendment 2, step 3): Return on the screen is Return on the watch, and
     * what the watch decides is what the screen shows. The second half is driven with no fix
     * reaching the ViewModel at all, so the screen's "off track now" can only have come from the
     * watch.
     */
    @Test
    fun `Return on the screen marks the watch as returning, and the watch's off-track shows on the screen`() = runRecordingTest {
        val vm = viewModelIn(ViewModelStore())
        vm.startRecording(TrackRecordingMode.HIGH_ACCURACY)
        runCurrent()
        serviceBegins("track-1")
        fixArrives(lat = 45.000, t = 2_000L)

        vm.startReturn()
        assertTrue("the watch is returning the moment the screen's callback returns", returnWatch.state.value.isReturning)
        assertTrue("and the screen says so without waiting", vm.uiState.value.isReturning)

        // To the watch only, as the service feeds it. Nothing is emitted on the ViewModel's own stream.
        listOf(45.001 to 7_000L, 45.002 to 12_000L, 45.003 to 17_000L).forEach { (lat, t) ->
            returnWatch.onFix(TrackPoint(lat, -122.0, null, 5f, t))
        }
        runCurrent()

        assertTrue("the watch decided off track", returnWatch.state.value.isOffTrack)
        assertTrue("and the screen's copy follows", vm.uiState.value.isOffTrack)
        assertEquals(1, delivered.size)

        vm.stopReturn()
        assertFalse(returnWatch.state.value.isReturning)
        assertFalse(vm.uiState.value.isReturning)
        assertFalse(vm.uiState.value.isOffTrack)
        vm.stopRecording()
    }

    /**
     * Amendment 2, ruling 3: Record, then Return before the service has begun the watch. The
     * service begins a moment after the Record tap; a Return tapped inside that moment is kept.
     */
    @Test
    fun `Return tapped before the service has begun the watch is not lost`() = runRecordingTest {
        val vm = viewModelIn(ViewModelStore())
        vm.startRecording(TrackRecordingMode.HIGH_ACCURACY)
        runCurrent()

        vm.startReturn() // the service has not begun the watch yet
        assertTrue(vm.uiState.value.isReturning)

        serviceBegins("track-1")
        runCurrent()
        assertTrue("the service beginning the same track keeps the return", vm.uiState.value.isReturning)
        assertEquals(ReturnWatchState(trackId = "track-1", isReturning = true), returnWatch.state.value)
        vm.stopRecording()
    }

    /**
     * The start point is handed to the watch by the ViewModel whenever it changes, and the watch
     * keeps the last one: the first breadcrumb until the origin waypoint exists, then the origin,
     * the same rule the screen's own distance follows. Read back as the watch's distance to it.
     */
    @Test
    fun `the watch measures to the first breadcrumb until the origin waypoint exists, then to the origin`() = runRecordingTest {
        val vm = viewModelIn(ViewModelStore())
        vm.startRecording(TrackRecordingMode.HIGH_ACCURACY)
        runCurrent()
        serviceBegins("track-1")
        tracks.appendForTest("track-1", TrackPoint(45.000, -122.0, null, null, 1_000L))
        advanceTimeBy(POLL_INTERVAL_MILLIS)
        runCurrent()

        returnWatch.onFix(TrackPoint(45.002, -122.0, null, 5f, 2_000L))
        assertEquals("to the first breadcrumb at 45.000", 222.4, returnWatch.state.value.returnToStart!!.distanceMeters, 1.0)

        fixArrives(lat = 45.001, t = 3_000L) // the first gated fix: the origin waypoint, at 45.001
        assertEquals(45.001, vm.uiState.value.originWaypoint?.lat)
        returnWatch.onFix(TrackPoint(45.002, -122.0, null, 5f, 4_000L))
        assertEquals("to the origin at 45.001", 111.2, returnWatch.state.value.returnToStart!!.distanceMeters, 1.0)
        vm.stopRecording()
    }

    /**
     * A stop from the notification ends the recording in the service, which ends the watch. The
     * screen's copy of the return follows at once, before the screen has re-read the track's row.
     */
    @Test
    fun `when the service ends the watch, the screen's return ends with it`() = runRecordingTest {
        val vm = viewModelIn(ViewModelStore())
        recordAndStartReturning(vm)

        serviceEnds("track-1")
        runCurrent()

        assertFalse(vm.uiState.value.isReturning)
        assertFalse(vm.uiState.value.isOffTrack)
        vm.stopRecording()
    }

    /**
     * **Still a fault, and Part 3's to fix** (the dispatch's second path, confirmed by the owner,
     * not built here). A ViewModel built after the first was cleared does not know a recording is
     * still open: `init` loads waypoints and tracks and nothing else, and `resyncRecordingState`
     * returns at once when `activeTrack` is null.
     *
     * It also holds one of Amendment 2's design points: the ViewModel copies the watch's state
     * only when the watch is for the ViewModel's own active track. The watch is still returning
     * for `track-1` here, and the reopened screen, which has no active track, does not show it.
     *
     * **When Part 3 lands:** `isRecording` and `isReturning` here become true.
     */
    @Test
    fun `PART 3 FAULT, still true - a ViewModel built while a recording runs reports not recording and not returning`() = runRecordingTest {
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
        assertTrue("precondition: the watch is still returning for the first track", returnWatch.state.value.isReturning)

        val state = reopened.uiState.value
        assertFalse("FAULT: the record button reads this, and it says nothing is recording", state.isRecording)
        assertNull(state.activeTrack)
        assertFalse("FAULT: the return that is under way is not shown on the reopened screen", state.isReturning)
        assertNull("no start point is known either, so the HUD would have no target", state.originWaypoint)
        assertTrue(state.breadcrumbPoints.isEmpty())
        assertEquals(
            "the open row is loaded into the Records list, with no end time; nothing treats that as a live recording",
            listOf("track-1" to null),
            state.tracks.map { it.id to it.endedAtEpochMillis },
        )
    }

    /**
     * **Still a fault, and Part 3's to fix.** Pressing record on the reopened screen creates a
     * second track row beside the first, which is still open. Nothing in `startRecording` looks
     * for an open row. What the service does with the second row's start is pinned in
     * `TrackRecordingServiceSwipeAwayTest`.
     *
     * New since Amendment 2, and recorded so it is not a surprise: Return on that second screen is
     * refused. The watch is begun for the first track, a return for another track is not accepted,
     * and the ViewModel logs it. Before the move the second screen would have marked itself
     * returning against a track that gets no points.
     */
    @Test
    fun `PART 3 FAULT, still true - pressing record on the reopened screen creates a second open track row, and its Return is refused`() = runRecordingTest {
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

        logged.clear()
        reopened.startReturn()
        assertFalse("the second screen's Return is refused: the watch belongs to the first track", reopened.uiState.value.isReturning)
        assertEquals("track-1", returnWatch.state.value.trackId)
        assertEquals(listOf("Return was not started for track 'track-2': the recording service is recording another track."), logged)
        reopened.stopRecording()
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 15_000L
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

    /** Puts a point on a track without going through [appendPoints]: a breadcrumb the poll will read, as the service would have written it. */
    fun appendForTest(trackId: String, point: TrackPoint) {
        tracks[trackId] = tracks.getValue(trackId).let { it.copy(points = it.points + point) }
    }
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
