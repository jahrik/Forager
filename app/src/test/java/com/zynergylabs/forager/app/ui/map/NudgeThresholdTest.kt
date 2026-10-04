package com.zynergylabs.forager.app.ui.map

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Dispatch 2026-09-28-477, Amendment 1 (the owner: "1.5 finger widths, 72 dp", and "Confirmed" to the
 * step path): a drag leaves following only past 72 dp, and the nudge's give follows the line, up to
 * 36 dp. Whether MapLibre lets go at that distance is device-only.
 */
class NudgeThresholdTest {

    private val density = 3.75f // the S22's

    @Test
    fun `the line a drag must pass to leave following is 72 dp`() {
        assertEquals(72f, NAVIGATION_NUDGE_THRESHOLD_DP)
    }

    @Test
    fun `the give follows the line, up to 36 dp however far the finger goes`() {
        val give = NudgeGive(NAVIGATION_NUDGE_THRESHOLD_DP * density)
        give.down(0f, 0f, NudgeOffset.ZERO)
        assertEquals(36f * density, give.move(500f * density, 0f, pointerCount = 1, canGive = true)!!.dxPx, 0.01f)
    }

    @Test
    fun `a flick between the old and the new line gives half its travel, uncapped`() {
        val give = NudgeGive(NAVIGATION_NUDGE_THRESHOLD_DP * density)
        give.down(0f, 0f, NudgeOffset.ZERO)
        assertEquals("60 dp of travel, past the old 48 dp line", 30f * density, give.move(60f * density, 0f, pointerCount = 1, canGive = true)!!.dxPx, 0.01f)
    }
}
