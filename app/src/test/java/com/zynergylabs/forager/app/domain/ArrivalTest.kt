package com.zynergylabs.forager.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dispatch 2026-09-28-497, Amendment 1 (the owner: "Twice accuracy, at least 15 m"): arrival at the
 * start is within max(2 x accuracy, 15 m), straight line, and 15 m with no accuracy reported.
 * "Approaching" ([isApproaching]) is unchanged.
 */
class ArrivalTest {

    @Test
    fun `with the S22's constant 3_79 m accuracy, arrival is at 15 m, not 7_6 m`() {
        assertTrue("14.9 m", hasArrived(14.9, 3.79f))
        assertTrue("15 m exactly", hasArrived(15.0, 3.79f))
        assertFalse("15.1 m", hasArrived(15.1, 3.79f))
        assertFalse("approaching at 7.6 m is unchanged: the two are separate rules", isApproaching(8.0, 3.79f))
    }

    @Test
    fun `with a poorer fix, twice its accuracy`() {
        assertTrue(hasArrived(19.9, 10f))
        assertFalse(hasArrived(20.1, 10f))
    }

    @Test
    fun `with no accuracy reported, 15 m`() {
        assertTrue(hasArrived(14.0, null))
        assertFalse(hasArrived(16.0, null))
    }
}
