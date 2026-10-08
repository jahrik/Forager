package com.zynergylabs.forager.app.ui.availability

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Dispatch 2026-09-28-685, Amendment 2 (RECORD -699; the owner: "Coordinates take priority (Recommended)"): the strip keeps
 * its coordinates and gives way with its readouts, facing first. Widths in px: coordinates 200, each separator 20, minimum 48.
 */
class StripReadoutsShownTest {

    private fun shown(available: Int, heading: Int = 100, elevation: Int = 60) =
        stripReadoutsShown(availablePx = available, coordinatesPx = 200, headingPx = heading, elevationPx = elevation, separatorPx = 20, minimumPx = 48)

    @Test
    fun `room for everything whole keeps both readouts`() = assertEquals(StripReadoutsShown(heading = true, elevation = true), shown(400))

    @Test
    fun `short of whole but room for both at their minimum keeps both, to shorten`() =
        assertEquals(StripReadoutsShown(heading = true, elevation = true), shown(200 + 40 + 96))

    @Test
    fun `one px short of both at their minimum drops facing, and altitude stays`() =
        assertEquals(StripReadoutsShown(heading = false, elevation = true), shown(200 + 40 + 96 - 1))

    @Test
    fun `room for neither drops both`() = assertEquals(StripReadoutsShown(heading = false, elevation = false), shown(200 + 20 + 48 - 1))

    @Test
    fun `a readout narrower than the minimum needs only its own width`() =
        assertEquals(StripReadoutsShown(heading = true, elevation = true), shown(200 + 40 + 30 + 20, heading = 30, elevation = 20))

    @Test
    fun `the general rule drops from the front and keeps a lone readout when it fits`() {
        assertEquals(listOf(true), readoutsKeptBeside(availablePx = 200 + 20 + 48, coordinatesPx = 200, readoutsPx = listOf(100), separatorPx = 20, minimumPx = 48))
        assertEquals(listOf(false), readoutsKeptBeside(availablePx = 200 + 20 + 47, coordinatesPx = 200, readoutsPx = listOf(100), separatorPx = 20, minimumPx = 48))
    }
}
