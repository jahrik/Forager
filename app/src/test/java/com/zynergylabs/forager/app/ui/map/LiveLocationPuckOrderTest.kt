package com.zynergylabs.forager.app.ui.map

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-290: an open fan draws above the live-location puck. [liveLocationComponentOptions]
 * is what MapLibre is handed at activation, and `LocationComponentOptions` (unlike `Style`) builds under
 * Robolectric, so what is asserted here is the real options object: where the puck is told to sit, and
 * that the one other thing this file sets on it is untouched. That MapLibre then honours the position on
 * a real map is device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LiveLocationPuckOrderTest {

    private val options = liveLocationComponentOptions(ApplicationProvider.getApplicationContext())

    @Test
    fun `the puck is placed below the fan's lowest layer`() {
        assertEquals(
            "the puck must be positioned below the fan's lowest layer, so an open fan draws over it",
            FanOutIds.LEGS_CASING_LAYER,
            options.layerBelow(),
        )
    }

    @Test
    fun `the puck is not also positioned above a layer`() {
        // MapLibre consults layerAbove first, so one set here would win over layerBelow.
        assertNull(options.layerAbove())
    }

    @Test
    fun `the tracking animation multiplier is unchanged`() {
        assertEquals(locationIndicatorTrackingAnimationMultiplier(), options.trackingAnimationDurationMultiplier(), 0.0001f)
    }
}
