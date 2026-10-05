package com.zynergylabs.forager.app.ui.map

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.ShownPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-510: how the dot is drawn for each kind of position, as the options MapLibre is
 * handed. What those options look like on screen is device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "xxhdpi")
class PositionLookOptionsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `precise is MapLibre's own dot, as before this dispatch`() {
        val options = liveLocationComponentOptions(context, look = PositionLook.PRECISE)
        // MapLibre 13.5.0's own defaults (its res/values: maplibre_accuracyAlpha 0.15, maplibre_enableStaleState true), no tint.
        assertEquals(0.15f, options.accuracyAlpha(), 0.001f)
        assertTrue(options.enableStaleState())
        assertNull(options.foregroundTintColor())
        assertNull(options.backgroundTintColor())
    }

    @Test
    fun `approximate is a soft dot in a stronger circle, which the app, not MapLibre's 30 s clock, greys`() {
        val options = liveLocationComponentOptions(context, look = PositionLook.APPROXIMATE)

        assertEquals(APPROXIMATE_DOT_COLOR, options.foregroundTintColor())
        assertTrue("soft: the dot is see-through", (APPROXIMATE_DOT_COLOR ushr 24) < 0xFF)
        assertEquals(APPROXIMATE_ACCURACY_ALPHA, options.accuracyAlpha(), 0.001f)
        assertTrue("the circle is stronger than MapLibre's own 15%", APPROXIMATE_ACCURACY_ALPHA > 0.15f)
        assertFalse(options.enableStaleState())
    }

    @Test
    fun `last known is a grey dot and circle`() {
        val options = liveLocationComponentOptions(context, look = PositionLook.LAST_KNOWN)

        assertEquals(LAST_KNOWN_COLOR, options.foregroundTintColor())
        assertEquals(LAST_KNOWN_COLOR, options.accuracyColor())
        assertEquals(LAST_KNOWN_COLOR, options.bearingTintColor())
        assertFalse(options.enableStaleState())
    }

    @Test
    fun `the look survives the navigation view's options`() {
        val options = liveLocationComponentOptions(context, navigating = true, look = PositionLook.APPROXIMATE)

        assertTrue(options.trackingGesturesManagement())
        assertEquals(APPROXIMATE_DOT_COLOR, options.foregroundTintColor())
    }

    @Test
    fun `the look follows the one rule the label, the strip and the HUD read`() {
        val gps = LocationFix.Update(45.5, -122.6, null, 5f, 1_000L)
        val reading = LocationFix.Update(45.51, -122.61, null, 120f, 2_123L)
        assertEquals(PositionLook.PRECISE, positionLookOf(ShownPosition.Precise(gps), gps))
        assertEquals(PositionLook.APPROXIMATE, positionLookOf(ShownPosition.Approximate(reading), gps))
        assertEquals(PositionLook.LAST_KNOWN, positionLookOf(ShownPosition.LastKnown(reading), gps))
        assertEquals(PositionLook.LAST_KNOWN, positionLookOf(ShownPosition.LastKnown(reading), null))
        // The held GPS fix, lost: MapLibre's own grey dot, as before.
        assertEquals(PositionLook.PRECISE, positionLookOf(ShownPosition.LastKnown(gps), gps))
        assertEquals(PositionLook.PRECISE, positionLookOf(ShownPosition.None, null))
    }
}
