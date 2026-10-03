package com.zynergylabs.forager.app.ui.map

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-457, Parts B and C: the location component's options while navigating turn on
 * MapLibre's tracking-gesture management, with the nudge and two-finger thresholds in pixels; outside
 * navigation they are what they were (management off). What MapLibre then does with a finger is
 * device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "xxhdpi")
class NavigationGestureOptionsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `while navigating, a nudge under a finger's width and a pinch's drift do not end following`() {
        val density = context.resources.displayMetrics.density
        val options = liveLocationComponentOptions(context, navigating = true)
        assertTrue(options.trackingGesturesManagement())
        assertEquals(NAVIGATION_NUDGE_THRESHOLD_DP * density, options.trackingInitialMoveThreshold(), 0.5f)
        assertEquals(NAVIGATION_MULTI_FINGER_MOVE_THRESHOLD_DP * density, options.trackingMultiFingerMoveThreshold(), 0.5f)
    }

    @Test
    fun `outside navigation the options are as before`() {
        assertFalse(liveLocationComponentOptions(context).trackingGesturesManagement())
    }
}
