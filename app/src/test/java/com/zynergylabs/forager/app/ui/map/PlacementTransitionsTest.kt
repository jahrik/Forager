package com.zynergylabs.forager.app.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.maplibre.android.style.layers.TransitionOptions

/**
 * What the map does to a loaded style's transition (dispatch 2026-09-28-369, amendment -370): the
 * symbol fade goes off, the duration and delay stay as the style had them. The pure function only;
 * that the style-load callback applies it, and that the map then stops fading symbols, is
 * device-only (a `Style` cannot be built under Robolectric), so the recordings are the evidence.
 */
class PlacementTransitionsTest {

    @Test
    fun `the symbol fade is switched off`() {
        val result = transitionWithoutPlacementFade(TransitionOptions(300, 0, true))

        assertFalse("placement transitions must be off", result.isEnablePlacementTransitions)
    }

    @Test
    fun `the duration and delay the style had are kept`() {
        val result = transitionWithoutPlacementFade(TransitionOptions(450, 20, true))

        assertEquals(450L, result.duration)
        assertEquals(20L, result.delay)
        assertFalse(result.isEnablePlacementTransitions)
    }

    @Test
    fun `a transition already without the fade stays that way`() {
        val result = transitionWithoutPlacementFade(TransitionOptions(300, 0, false))

        assertEquals(300L, result.duration)
        assertEquals(0L, result.delay)
        assertFalse(result.isEnablePlacementTransitions)
    }
}
