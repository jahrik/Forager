package com.zynergylabs.forager.app.service

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.AppContainer
import com.zynergylabs.forager.app.ForagerApplication
import com.zynergylabs.forager.app.alert.BACK_BY_NOTIFICATION_ID
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
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
 * Back by not firing on the L3 walk (dispatch 2026-09-28-796, RECORD -793 to -795): the cases
 * `TrackRecordingServiceBackByTest` never had. That class sets a time already past before the service
 * starts, so its first evaluation, at start, fires it; nothing reached a **future** time set **after**
 * the service had begun, through the service's own loop, or with a **Return in progress**, as on the
 * walk (set at about 13:10 for 13:55, Return at 13:48, no alert posted: the S22's usagestats has no
 * `back_by_alert` interruption that day).
 *
 * The real [TrackRecordingService] and the real [AppContainer]. The time is set on the container's
 * `BackByWatch`, the one instance `MainActivity` hands the recording ViewModel (`MainActivity.kt:234`,
 * `container` being the application's, `:63`) and the one the service drives; the ViewModel's
 * `setBackBy` makes exactly this call (`TrackRecordingViewModel.kt:314-318`). Return is the call the
 * ViewModel's `startReturn` makes (`TrackRecordingViewModel.kt:822`). The container's clock is the
 * system clock, so these tests wait in real time, for one tick of the 15 s loop at most.
 *
 * Every test stops what it started inside its own body, in a `finally`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrackRecordingServiceBackByLoopTest {

    private lateinit var context: Application
    private lateinit var container: AppContainer
    private lateinit var shadowLocationManager: ShadowLocationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        container = (context as ForagerApplication).container
        shadowOf(context).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        shadowLocationManager = shadowOf(locationManager)
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
    }

    /** (a) A future time, set after the service has begun, is reached through the service's own 15 s loop. */
    @Test
    fun `a future time set after the service has begun fires through the service's own loop`() {
        var controller: ServiceController<TrackRecordingService>? = null
        try {
            val (c, trackId) = startedRecording()
            controller = c
            simulateGpsFix(metresNorth = 0.0)
            val at = System.currentTimeMillis() + 2_000L
            assertTrue("the watch takes the time", container.backByWatch.set(trackId, at))
            val notification = awaitBackByNotification(timeoutMillis = LOOP_WAIT_MILLIS)
            assertNotNull("no back-by alert within ${LOOP_WAIT_MILLIS / 1_000} s of a time set 2 s ahead, after the service began", notification)
        } finally {
            controller?.let { end(it) }
        }
    }

    /** (b) As (a), with a Return in progress before the time, and the walker well away from the start, as on the walk. */
    @Test
    fun `with a Return in progress and the walker far from the start, the time still fires through the loop`() {
        var controller: ServiceController<TrackRecordingService>? = null
        try {
            val (c, trackId) = startedRecording()
            controller = c
            simulateGpsFix(metresNorth = 0.0)
            simulateGpsFix(metresNorth = 2_000.0)
            assertTrue("Return taken", container.returnWatch.startReturn(trackId))
            val at = System.currentTimeMillis() + 2_000L
            assertTrue("the watch takes the time", container.backByWatch.set(trackId, at))
            val deadline = System.currentTimeMillis() + LOOP_WAIT_MILLIS
            var notification: Notification? = null
            while (notification == null && System.currentTimeMillis() < deadline) {
                simulateGpsFix(metresNorth = 2_000.0)
                notification = awaitBackByNotification(timeoutMillis = 1_000L)
            }
            assertNotNull("no back-by alert within ${LOOP_WAIT_MILLIS / 1_000} s of a time set 2 s ahead, during a Return, 2 km from the start", notification)
        } finally {
            controller?.let { end(it) }
        }
    }

    /**
     * The walk's suspected mechanism, modelled (unconfirmed on the device; see the report): the alert
     * waits only on the service's 15 s `delay`, and a `delay` counts time the CPU is awake. Fixes did
     * reach the service on the walk (the off-track alert at 13:57:48 is driven by them), so a fix
     * after the time is a moment the service is certainly running. This asks whether such a fix
     * brings the alert, or whether it still waits for the timer.
     */
    @Test
    fun `a GPS fix after the time brings the alert without waiting for the 15 s timer`() {
        var controller: ServiceController<TrackRecordingService>? = null
        try {
            val (c, trackId) = startedRecording()
            controller = c
            simulateGpsFix(metresNorth = 0.0)
            val at = System.currentTimeMillis() + 500L
            assertTrue("the watch takes the time", container.backByWatch.set(trackId, at))
            while (System.currentTimeMillis() <= at) Thread.sleep(50L)
            val fixAt = System.currentTimeMillis()
            simulateGpsFix(metresNorth = 0.0)
            val notification = awaitBackByNotification(timeoutMillis = FIX_WAIT_MILLIS)
            assertNotNull(
                "no back-by alert within ${FIX_WAIT_MILLIS / 1_000} s of a GPS fix ${fixAt - at} ms past the time: the alert waits for the 15 s timer, not the fix",
                notification,
            )
        } finally {
            controller?.let { end(it) }
        }
    }

    private fun startedRecording(): Pair<ServiceController<TrackRecordingService>, String> {
        val trackId = runBlocking { container.startTrackUseCase(null) }.getOrThrow().id
        val controller = Robolectric.buildService(TrackRecordingService::class.java, startIntent(trackId))
        controller.create().get()
        controller.startCommand(0, 1)
        // The start-of-recording evaluation runs with nothing set; let it pass before setting a time.
        idleAndSettle()
        Thread.sleep(250L)
        idleAndSettle()
        return controller to trackId
    }

    private fun startIntent(trackId: String) =
        Intent(context, TrackRecordingService::class.java).apply {
            action = TrackRecordingService.ACTION_START
            putExtra(TrackRecordingService.EXTRA_TRACK_ID, trackId)
            putExtra(TrackRecordingService.EXTRA_MODE, TrackRecordingMode.HIGH_ACCURACY.name)
        }

    private fun stopIntent() = Intent(context, TrackRecordingService::class.java).setAction(TrackRecordingService.ACTION_STOP)

    /** One GPS fix [metresNorth] of a fixed start, stamped now on a whole second, as the S22's GPS stamps them. */
    private fun simulateGpsFix(metresNorth: Double) {
        shadowLocationManager.simulateLocation(
            Location(LocationManager.GPS_PROVIDER).apply {
                latitude = 45.0 + metresNorth / 111_195.0
                longitude = -122.0
                accuracy = 5f
                time = System.currentTimeMillis() / 1_000L * 1_000L
                elapsedRealtimeNanos = 10_000_000_000L
            },
        )
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun backByNotification() =
        shadowOf(context.getSystemService(NotificationManager::class.java)).getNotification(BACK_BY_NOTIFICATION_ID)

    private fun awaitBackByNotification(timeoutMillis: Long): Notification? {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            backByNotification()?.let { return it }
            idleAndSettle()
        }
        return backByNotification()
    }

    private fun end(controller: ServiceController<TrackRecordingService>) {
        val service = controller.get()
        if (!shadowOf(service).isStoppedBySelf) {
            controller.withIntent(stopIntent()).startCommand(0, 99)
            val deadline = System.currentTimeMillis() + 5_000L
            while (!shadowOf(service).isStoppedBySelf && System.currentTimeMillis() < deadline) Thread.sleep(25L)
        }
        controller.destroy()
        idleAndSettle()
        context.getSystemService(NotificationManager::class.java).cancelAll()
        shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(250L)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun idleAndSettle() {
        shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(25L)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private companion object {
        /** One 15 s tick of the loop, with room for a slow machine. */
        const val LOOP_WAIT_MILLIS = 25_000L

        /** Well inside one tick: an alert this soon after the fix came from the fix, not the timer. */
        const val FIX_WAIT_MILLIS = 3_000L
    }
}
