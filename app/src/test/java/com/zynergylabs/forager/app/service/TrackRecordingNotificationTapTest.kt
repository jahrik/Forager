package com.zynergylabs.forager.app.service

import android.Manifest
import android.app.Application
import android.content.Intent
import android.location.LocationManager
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ForagerApplication
import com.zynergylabs.forager.app.MainActivity
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * What a tap on the recording notification asks the platform to do (dispatch 2026-09-28-415).
 *
 * **The fault, seen on the S22:** with Forager open and a recording running, Home, then a tap on
 * the notification's body, left two `MainActivity` records in the task, the new one on top. The
 * notification's intent carried no flags, and `MainActivity` is a standard activity, so a plain
 * start made a second copy.
 *
 * **What this test shows, and what it does not.** It takes the notification the service really
 * posted to `startForeground` and reads the intent behind its tap. It shows what is asked of the
 * platform: `FLAG_ACTIVITY_SINGLE_TOP` and `FLAG_ACTIVITY_CLEAR_TOP`, the same two the backup
 * notification sets. It does not show what the platform then does with a live task; Robolectric
 * has no task stack to bring forward. That is the phone step in the dispatch's report.
 *
 * In its own class so that `TrackRecordingServiceTest` is not edited. A started recording is
 * stopped inside the test body and waited for, as that class's header requires.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrackRecordingNotificationTapTest {

    @Test
    fun `a tap on the recording notification opens MainActivity asking for the existing screen, not a new copy on top`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val container = (context as ForagerApplication).container
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        // No provider enabled: the tracker registers no listener, so nothing is left waiting on a fix.
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        shadowOf(locationManager).setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        shadowOf(locationManager).setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        val trackId = runBlocking { container.startTrackUseCase(null) }.getOrThrow().id
        val start = Intent(context, TrackRecordingService::class.java).apply {
            action = TrackRecordingService.ACTION_START
            putExtra(TrackRecordingService.EXTRA_TRACK_ID, trackId)
            putExtra(TrackRecordingService.EXTRA_MODE, TrackRecordingMode.HIGH_ACCURACY.name)
        }
        val controller = Robolectric.buildService(TrackRecordingService::class.java, start)
        val service = controller.create().get()
        try {
            controller.startCommand(0, 1)
            val notification = shadowOf(service).lastForegroundNotification
            assertNotNull("the service must be in the foreground before there is a notification to tap", notification)

            val tap = shadowOf(notification.contentIntent)
            assertTrue("the tap opens an Activity", tap.isActivityIntent)
            assertEquals(MainActivity::class.java.name, tap.savedIntent.component?.className)
            val wanted = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            assertEquals(
                "the tap must ask for the MainActivity that already exists (SINGLE_TOP and CLEAR_TOP), " +
                    "or a standard activity is started again on top of itself. Flags were 0x${Integer.toHexString(tap.savedIntent.flags)}",
                wanted,
                tap.savedIntent.flags and wanted,
            )
        } finally {
            controller.withIntent(Intent(context, TrackRecordingService::class.java).setAction(TrackRecordingService.ACTION_STOP)).startCommand(0, 2)
            val deadline = System.currentTimeMillis() + 10_000L
            while (!shadowOf(service).isStoppedBySelf && System.currentTimeMillis() < deadline) Thread.sleep(25L)
            controller.destroy()
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(250L)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
