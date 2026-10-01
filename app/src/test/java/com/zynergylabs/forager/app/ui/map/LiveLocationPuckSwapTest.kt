package com.zynergylabs.forager.app.ui.map

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.PuckPlacementProbe
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-318, fail 3: after a basemap swap the folded stack's glyph covered the puck, and leaving
 * Maps and coming back put it right.
 *
 * What MapLibre 13.5.0 does on a swap (javap on `MapLibreMap.notifyStyleLoaded`, `LocationComponent` and
 * `LocationLayerController`): the new style loads, and the SDK's own `LocationComponent.onFinishLoadingStyle` re-adds the
 * puck's layers *before* this app's style callback has added any of its own, with the options it already holds, whose
 * `layerBelow` is the fan's casing layer, which is not in the new style yet. The app's callback then adds the registry's
 * layers and the fan's above the puck, and calls `activateLocationComponent` again: `initialize` returns at once
 * (already initialised) and `applyStyle` hands the same options to a position manager whose `update` says "unchanged", so
 * the puck's layers are never moved. A fresh `MapView` initialises after the layers exist, which is why it was fine.
 *
 * [PuckPlacementProbe] is the SDK's own `update`, so the assertions below are about its real answer. That the layers then
 * move on a real map, and that [activateLiveLocationIfPermitted] makes this call, is device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LiveLocationPuckSwapTest {

    private val options = liveLocationComponentOptions(ApplicationProvider.getApplicationContext())

    /** The SDK's position manager as a style load leaves it: built from the options the component already holds. */
    private fun heldAfterStyleLoad() = PuckPlacementProbe(options)

    @Test
    fun `premise - the SDK does not move the puck when handed the options it already holds`() {
        assertFalse(
            "the SDK's update() must say 'unchanged' for identical options: that is what left the puck where the style load put it",
            heldAfterStyleLoad().wouldReplaceLayers(options),
        )
    }

    @Test
    fun `on a swap the puck's layers are placed again below the fan, after the app's layers exist`() {
        val probe = heldAfterStyleLoad()
        val replacement = puckReplacementOptions(alreadyInitialised = true, options = options)
        assertNotNull("a swap needs a first placement to make the final one count", replacement)
        assertTrue("the first placement must itself move the puck", probe.wouldReplaceLayers(replacement!!))
        assertTrue(
            "after a swap the final options must make the SDK remove and re-add the puck's layers, now that the fan's layer exists",
            probe.wouldReplaceLayers(options),
        )
        assertEquals(FanOutIds.LEGS_CASING_LAYER, options.layerBelow())
    }

    @Test
    fun `on a first activation nothing is applied first`() {
        assertNull(
            "a component that is not initialised builds its layers at the options' position when activated; no extra step",
            puckReplacementOptions(alreadyInitialised = false, options = options),
        )
    }

    @Test
    fun `the first placement does not change anything but where the puck sits`() {
        val replacement: LocationComponentOptions = puckReplacementOptions(alreadyInitialised = true, options = options)!!
        assertEquals(options.trackingAnimationDurationMultiplier(), replacement.trackingAnimationDurationMultiplier(), 0.0001f)
        assertNull(replacement.layerAbove())
    }
}
