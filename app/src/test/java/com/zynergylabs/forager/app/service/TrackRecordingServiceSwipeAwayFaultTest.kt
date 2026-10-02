package com.zynergylabs.forager.app.service

import android.Manifest
import android.app.Application
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.AppContainer
import com.zynergylabs.forager.app.ForagerApplication
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import com.zynergylabs.forager.app.ui.track.TrackRecordingViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLocationManager

/**
 * **These tests describe a fault. A green run here does not mean "works"; it means the fault is
 * still there, exactly as written down.** Dispatch 2026-09-28-400, Part 1 (plan task T1,
 * `prompts/preserved/2026-10-02-01.md`): pin today's behaviour before anything is built. They are
 * replaced, not deleted, when the fix lands.
 *
 * The plain-JVM half is `TrackRecordingSwipeAwayFaultTest`, which shows the off-track decision
 * dying with the ViewModel. This class adds the real [TrackRecordingService] and the real
 * [AppContainer], so the two things that are supposed to agree about a recording (the service and
 * the screen's ViewModel) are both the production classes over the same database.
 *
 * ## What is real here, and the three steps that are not
 *
 * Real: the ViewModel's own callbacks (`startRecording`, `stopRecording`, `onEnteredForeground`),
 * the service through its own `onStartCommand`, the tracker over Robolectric's `LocationManager`,
 * and the Room database behind [AppContainer].
 *
 * Not reached, and said plainly:
 * 1. **The swipe from recents.** It is stood in for by `ViewModelStore.clear()` with the service
 *    left running. Whether the platform really destroys the Activity, keeps the process and leaves
 *    the service alone is the phone's to show. Robolectric has no task to remove.
 * 2. **`MainActivity`'s `LaunchedEffect(trackUiState.activeTrack)`** (`MainActivity.kt:411-438`),
 *    which is what turns a new `activeTrack` into `ACTION_START` and a cleared one into
 *    `ACTION_STOP`. No test in this repository composes `MainActivity`. [startIntent] and
 *    [stopIntent] are built here from the same three constants that effect uses, as
 *    `TrackRecordingServiceTest.startIntent` already does.
 * 3. **The process being killed** (premise 6). Only the null intent a sticky restart delivers is
 *    reachable; whether and when the system restarts the service is not.
 *
 * ## Ending what was started
 *
 * `TrackRecordingServiceTest`'s header records what an unstopped recording does to the next test
 * class's sandbox. Every test here that starts the service stops it through `ACTION_STOP` inside
 * its own body, waits for the stop to finish, destroys the controller and drains, in a `finally`.
 * Every ViewModel is stopped there too, which is `CLAUDE.md`'s poll-loop rule applied here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrackRecordingServiceSwipeAwayFaultTest {

    private lateinit var context: Application
    private lateinit var container: AppContainer
    private lateinit var shadowLocationManager: ShadowLocationManager
    private val createdViewModels = mutableListOf<TrackRecordingViewModel>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        container = (context as ForagerApplication).container
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        shadowLocationManager = shadowOf(locationManager)
        // One provider, so each collector of the tracker's stream is exactly one registered listener
        // and the listener count below reads as "how many things are listening for fixes".
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
    }

    /** Built the way `MainActivity`'s factory builds it, and held in a store so `clear()` reaches `onCleared`. */
    private fun viewModelIn(store: ViewModelStore): TrackRecordingViewModel =
        ViewModelProvider(
            store,
            viewModelFactory {
                initializer {
                    TrackRecordingViewModel(
                        container.trackRepository,
                        container.startTrackUseCase,
                        container.getWaypointsUseCase,
                        container.createWaypointUseCase,
                        container.deleteWaypointUseCase,
                        container.computeReturnToStartUseCase,
                        container.detectOffTrackUseCase,
                        container.locationTracker,
                        container.getTracksUseCase,
                        container.alertDelivery,
                        container.alertAudibility,
                        deleteTrack = container.deleteTrackUseCase,
                    ).also(createdViewModels::add)
                }
            },
        )[TrackRecordingViewModel::class.java]

    /** The intent `MainActivity.kt:413-427` sends when `activeTrack` becomes non-null. HIGH_ACCURACY is what the record button passes (`MainActivity.kt:602`). */
    private fun startIntent(trackId: String) =
        Intent(context, TrackRecordingService::class.java).apply {
            action = TrackRecordingService.ACTION_START
            putExtra(TrackRecordingService.EXTRA_TRACK_ID, trackId)
            putExtra(TrackRecordingService.EXTRA_MODE, TrackRecordingMode.HIGH_ACCURACY.name)
        }

    /** The intent `MainActivity.kt:434-436` sends when `activeTrack` becomes null after a start. */
    private fun stopIntent() = Intent(context, TrackRecordingService::class.java).setAction(TrackRecordingService.ACTION_STOP)

    /**
     * Dispatch premise 3: after the swipe-away the service records, and a reopened app does not
     * know. The service is left running while the first ViewModel's store is cleared; a second
     * ViewModel is built over the same container and enters the foreground.
     *
     * The listener count is the direct reading of who is still watching the walker: two while the
     * screen is alive (the service's, and the ViewModel's own for the off-track decision), one
     * after the clear. The one that is left is the service's, which decides nothing.
     *
     * **When the fix lands:** the last block is the dispatch's second path, which the owner has not
     * confirmed. If a reopened app is to show the recording, `isRecording` there becomes true.
     */
    @Test
    fun `FAULT today - the service goes on recording after the ViewModel is cleared, and a reopened ViewModel reports not recording`() {
        val firstStore = ViewModelStore()
        val first = viewModelIn(firstStore)
        var controller: ServiceController<TrackRecordingService>? = null
        try {
            first.startRecording(TrackRecordingMode.HIGH_ACCURACY)
            val trackId = awaitActiveTrackId(first)
            assertNotNull("the ViewModel must have created a track before there is anything to swipe away from", trackId)
            controller = Robolectric.buildService(TrackRecordingService::class.java, startIntent(trackId!!))
            val service = controller.create().get()
            controller.startCommand(0, 1)
            assertNotNull("precondition: the service is in the foreground, recording", shadowOf(service).lastForegroundNotification)
            assertEquals("precondition: the service and the screen's ViewModel are both listening for fixes", 2, awaitListenerCount(2))

            // The swipe-away, as far as either class can tell: the Activity's store is cleared, the service is not told anything.
            firstStore.clear()

            assertEquals(
                "FAULT: one listener is left, the service's. The one that fed the off-track decision is gone",
                1,
                awaitListenerCount(1),
            )
            assertFalse("the service has not stopped itself", shadowOf(service).isStoppedBySelf)
            assertNull("the track is still open in storage", trackRow(trackId)?.endedAtEpochMillis)

            // Forager is opened again: a new Activity, so a new ViewModel. ON_START calls onEnteredForeground.
            val reopened = viewModelIn(ViewModelStore())
            reopened.onEnteredForeground()
            assertTrue(
                "precondition: the reopened ViewModel has finished loading, so what it reports below is its settled state",
                awaitTrackListed(reopened, trackId),
            )

            assertFalse(
                "FAULT: the record button reads isRecording, and it says nothing is recording while the service records",
                reopened.uiState.value.isRecording,
            )
            assertNull(reopened.uiState.value.activeTrack)
            assertFalse(reopened.uiState.value.isReturning)
            assertEquals("the reopened ViewModel is not listening for fixes either", 1, awaitListenerCount(1))
            assertFalse("and the service is still going", shadowOf(service).isStoppedBySelf)
            assertNull(trackRow(trackId)?.endedAtEpochMillis)
        } finally {
            endEverything(controller)
        }
    }

    /**
     * Dispatch premise 4, the one the dispatch asked to be settled first: a second recording
     * started over the first. Confirmed. The record button on the reopened screen creates a second
     * track row; its `ACTION_START` reaches a service whose `recordingJob` is not null and is
     * dropped without a log line (`TrackRecordingService.kt:82`); every point goes on being
     * written to the first track.
     *
     * Twenty fixes are sent because twenty accepted points is the service's batch
     * (`FLUSH_BATCH_SIZE`), the only write a test can wait for without waiting thirty real
     * seconds. They are spaced so that HIGH_ACCURACY's sampler accepts each one (10 s and about
     * 111 m apart, 5 m accuracy). That the first track then holds twenty points is the positive
     * half: the fixes did flow, so the second track's zero is a reading and not an empty stream.
     *
     * Then the stop button: the screen stops, `ACTION_STOP` ends the **first** track, and the
     * second row is left open for good. Nothing ever ends it.
     *
     * **When the fix lands:** whether a reopened app may start a second recording at all is the
     * owner's (the dispatch's second path). Whatever is decided, "two open rows, points in the
     * wrong one" must not survive it.
     */
    @Test
    fun `FAULT today - a second recording started over the first is ignored by the service, so its row gets no points and is never ended`() {
        val firstStore = ViewModelStore()
        val first = viewModelIn(firstStore)
        var controller: ServiceController<TrackRecordingService>? = null
        try {
            first.startRecording(TrackRecordingMode.HIGH_ACCURACY)
            val firstTrackId = awaitActiveTrackId(first)
            assertNotNull(firstTrackId)
            controller = Robolectric.buildService(TrackRecordingService::class.java, startIntent(firstTrackId!!))
            val service = controller.create().get()
            controller.startCommand(0, 1)
            assertNotNull("precondition: the service is recording the first track", shadowOf(service).lastForegroundNotification)
            assertEquals(2, awaitListenerCount(2))
            firstStore.clear()
            assertEquals(1, awaitListenerCount(1))

            val reopened = viewModelIn(ViewModelStore())
            reopened.onEnteredForeground()
            assertTrue(awaitTrackListed(reopened, firstTrackId))
            assertFalse("precondition: the reopened screen offers Record, not Stop", reopened.uiState.value.isRecording)

            // The record button, with isRecording false: MainActivity.kt:602.
            reopened.startRecording(TrackRecordingMode.HIGH_ACCURACY)
            val secondTrackId = awaitActiveTrackId(reopened)
            assertNotNull(secondTrackId)
            assertNotEquals("FAULT: a second track row was created while the first is still open", firstTrackId, secondTrackId)
            assertNull(trackRow(firstTrackId)?.endedAtEpochMillis)
            assertNull(trackRow(secondTrackId!!)?.endedAtEpochMillis)

            // What MainActivity's LaunchedEffect sends for the new activeTrack.
            controller.withIntent(startIntent(secondTrackId)).startCommand(0, 2)
            assertEquals(
                "the service did not start a second collection for the second track: its one listener, and the reopened ViewModel's",
                2,
                awaitListenerCount(2),
            )

            repeat(FLUSH_BATCH_SIZE) { index -> simulateFix(index) }

            assertEquals(
                "FAULT: the points went to the first track, the one the screen no longer shows",
                FLUSH_BATCH_SIZE,
                awaitPointCount(firstTrackId, FLUSH_BATCH_SIZE),
            )
            assertEquals(
                "FAULT: the track the screen shows as recording has no points at all",
                0,
                pointCount(secondTrackId),
            )

            // The stop button on the reopened screen, then the ACTION_STOP MainActivity's LaunchedEffect sends for it.
            reopened.stopRecording()
            controller.withIntent(stopIntent()).startCommand(0, 3)

            assertNotNull("the stop ended the first track, the only one the service knows", awaitEndedAt(firstTrackId))
            assertTrue(awaitStoppedBySelf(service))
            assertNull(
                "FAULT: the second track's row is never ended. The service has stopped and nothing else writes an end time",
                trackRow(secondTrackId)?.endedAtEpochMillis,
            )
            assertEquals("and it still has no points", 0, pointCount(secondTrackId))
        } finally {
            endEverything(controller)
        }
    }

    /**
     * Dispatch premise 6, the part a headless test reaches: `START_STICKY` asks the system to
     * recreate the service after the process is killed, and what it then delivers is a null
     * intent. `onStartCommand` starts nothing for it: no foreground notification, no listener, and
     * it asks to be sticky again.
     *
     * The positive half of these two readings is in the tests above, where `ACTION_START` on this
     * same class does produce a foreground notification and a listener.
     *
     * Not reached: whether and when the system restarts the service, and what Android does with a
     * restarted service that never calls `startForeground`. Out of scope to fix (the dispatch).
     */
    @Test
    fun `today - a null intent, which is what a sticky restart delivers after the process is killed, starts nothing`() {
        val controller = Robolectric.buildService(TrackRecordingService::class.java)
        val service = controller.create().get()
        try {
            val result = service.onStartCommand(null, 0, 1)
            idleAndSettle()

            assertEquals(Service.START_STICKY, result)
            assertNull("nothing was promoted to the foreground", shadowOf(service).lastForegroundNotification)
            assertEquals("nothing is listening for fixes", 0, shadowLocationManager.requestLocationUpdateListeners.size)
            assertFalse("and the service did not stop itself either: it sits there doing nothing", shadowOf(service).isStoppedBySelf)
        } finally {
            controller.destroy()
            drainBeforeTeardown()
        }
    }

    /**
     * One GPS fix, [index] steps north of 45.000, delivered through the platform's own listener
     * path. Wall time and elapsed time both move 10 s per fix: the tracker asks the platform for at
     * most one fix a second, and HIGH_ACCURACY's sampler wants 5 s and 5 m between kept points.
     */
    private fun simulateFix(index: Int) {
        shadowLocationManager.simulateLocation(
            Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 45.000 + index * 0.001
                longitude = -122.0
                accuracy = 5f
                time = FIRST_FIX_EPOCH_MILLIS + index * 10_000L
                elapsedRealtimeNanos = (index + 1) * 10_000_000_000L
            },
        )
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun trackRow(trackId: String) = runBlocking { container.trackRepository.getById(trackId) }.getOrThrow()

    /** Every stored point, kept or not: the unfiltered read, so the read seam's network-fix filter cannot hide a write. */
    private fun pointCount(trackId: String): Int = runBlocking { container.trackRepository.getFullRecord(trackId) }.getOrThrow().size

    /** Polls until the track holds at least [atLeast] points, and returns the count it last read, so a shortfall fails with the number. */
    private fun awaitPointCount(trackId: String, atLeast: Int, timeoutMillis: Long = 10_000L): Int {
        val deadline = System.currentTimeMillis() + timeoutMillis
        var count = pointCount(trackId)
        while (count < atLeast && System.currentTimeMillis() < deadline) {
            idleAndSettle()
            count = pointCount(trackId)
        }
        return count
    }

    /** Polls until exactly [expected] listeners are registered, and returns the count it last read. */
    private fun awaitListenerCount(expected: Int, timeoutMillis: Long = 10_000L): Int {
        val deadline = System.currentTimeMillis() + timeoutMillis
        var count = shadowLocationManager.requestLocationUpdateListeners.size
        while (count != expected && System.currentTimeMillis() < deadline) {
            idleAndSettle()
            count = shadowLocationManager.requestLocationUpdateListeners.size
        }
        return count
    }

    private fun awaitActiveTrackId(viewModel: TrackRecordingViewModel, timeoutMillis: Long = 10_000L): String? {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            viewModel.uiState.value.activeTrack?.let { return it.trackId }
            idleAndSettle()
        }
        return null
    }

    /** `init`'s `loadTracks` is a database read; this waits for it, so a "not recording" read after it is not just "not loaded yet". */
    private fun awaitTrackListed(viewModel: TrackRecordingViewModel, trackId: String, timeoutMillis: Long = 10_000L): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (viewModel.uiState.value.tracks.any { it.id == trackId }) return true
            idleAndSettle()
        }
        return false
    }

    private fun awaitEndedAt(trackId: String, timeoutMillis: Long = 10_000L): Long? {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            trackRow(trackId)?.endedAtEpochMillis?.let { return it }
            Thread.sleep(25L)
        }
        return null
    }

    private fun awaitStoppedBySelf(service: TrackRecordingService, timeoutMillis: Long = 5_000L): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (shadowOf(service).isStoppedBySelf) return true
            Thread.sleep(25L)
        }
        return false
    }

    /**
     * Stops every ViewModel and the service, whatever state the body left them in, and waits for
     * the service's stop to finish before the controller is destroyed. See this class's header.
     */
    private fun endEverything(controller: ServiceController<TrackRecordingService>?) {
        createdViewModels.forEach { it.stopRecording() }
        if (controller != null) {
            val service = controller.get()
            if (!shadowOf(service).isStoppedBySelf) {
                controller.withIntent(stopIntent()).startCommand(0, 99)
                awaitStoppedBySelf(service)
            }
            controller.destroy()
        }
        idleAndSettle()
        drainBeforeTeardown()
    }

    private fun idleAndSettle() {
        shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(25L)
        shadowOf(Looper.getMainLooper()).idle()
    }

    /** As `TrackRecordingServiceTest.drainBeforeTeardown`, for the reason recorded there. */
    private fun drainBeforeTeardown() {
        shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(250L)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private companion object {
        /** Mirrors `TrackRecordingService`'s own private constant of the same name. */
        const val FLUSH_BATCH_SIZE = 20
        const val FIRST_FIX_EPOCH_MILLIS = 1_790_000_000_000L
    }
}
