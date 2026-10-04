package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Dispatch 2026-09-28-502, step 3: the dashed line navigating to a waypoint runs from the walker's fix to
 * the waypoint. With no fix, or one the HUD treats as lost, there is no walker to draw from; once arrived,
 * the line ends, as the return's does.
 *
 * And the owner's choice at the desk check ("Keep the last line, faded"): a line already drawn is kept,
 * faded, once the fix is lost, and comes back bright with a fresh fix; with no line drawn yet (the app
 * just opened), there is still none until the first fix.
 */
class StraightLineToTargetTest {

    private val t = 1_700_000_000_000L
    private val creek = Waypoint("wp-1", 45.326, -122.634, null, "Creek pin", "", 1_000L)
    private val oak = Waypoint("wp-2", 45.30, -122.60, null, "Big oak", "", 2_000L)

    private fun fix(metersSouth: Double, at: Long = t) =
        LocationFix.Update(lat = creek.lat - metersSouth / 111_195.08, lng = creek.lng, altitude = null, accuracyMeters = 3.79f, timestampEpochMillis = at)

    private fun lineFrom(from: LocationFix.Update, to: Waypoint = creek) = listOf(LatLng(from.lat, from.lng), LatLng(to.lat, to.lng))

    private val sixMinutesLater = t + 6 * 60_000L

    @Test
    fun `from the walker's fix to the waypoint`() {
        val from = fix(500.0)
        assertEquals(listOf(LatLng(from.lat, from.lng), LatLng(creek.lat, creek.lng)), straightLineToTarget(from, creek, t + 1_000L))
    }

    @Test
    fun `no fix, or a lost one, draws no line`() {
        assertNull(straightLineToTarget(null, creek, t))
        assertNull("six minutes old is lost", straightLineToTarget(fix(500.0), creek, sixMinutesLater))
    }

    @Test
    fun `within 15 m the walker has arrived and the line ends`() {
        assertNull(straightLineToTarget(fix(12.0), creek, t + 1_000L))
        assertEquals(2, straightLineToTarget(fix(18.0), creek, t + 1_000L)?.size)
    }

    // ── The owner's "Keep the last line, faded" (desk check, 2026-10-04) ──

    @Test
    fun `with a current fix the line is current, from that fix`() {
        val from = fix(500.0)
        assertEquals(StraightLine(lineFrom(from), isCurrent = true), nextStraightLine(null, from, creek, t + 1_000L))
    }

    @Test
    fun `once the fix is lost, the line already drawn is kept, faded`() {
        val from = fix(500.0)
        val drawn = nextStraightLine(null, from, creek, t + 1_000L)

        assertEquals(StraightLine(lineFrom(from), isCurrent = false), nextStraightLine(drawn, from, creek, sixMinutesLater))
        assertEquals("no fix at all keeps it too", StraightLine(lineFrom(from), isCurrent = false), nextStraightLine(drawn, null, creek, sixMinutesLater))
    }

    @Test
    fun `a fresh fix brings it back current, from the new fix`() {
        val kept = StraightLine(lineFrom(fix(500.0)), isCurrent = false)
        val fresh = fix(300.0, at = sixMinutesLater)
        assertEquals(StraightLine(lineFrom(fresh), isCurrent = true), nextStraightLine(kept, fresh, creek, sixMinutesLater + 1_000L))
    }

    @Test
    fun `with no line drawn yet, as when the app has just opened, there is none until the first fix`() {
        assertNull(nextStraightLine(null, null, creek, t))
        assertNull(nextStraightLine(null, fix(500.0), creek, sixMinutesLater))
    }

    @Test
    fun `arrival ends the line, kept or not`() {
        val drawn = nextStraightLine(null, fix(500.0), creek, t + 1_000L)
        assertNull(nextStraightLine(drawn, fix(10.0), creek, t + 1_000L))
    }

    @Test
    fun `a line to another waypoint is not kept for this one`() {
        val toOak = StraightLine(lineFrom(fix(500.0), oak), isCurrent = true)
        assertNull(nextStraightLine(toOak, null, creek, sixMinutesLater))
    }

    @Test
    fun `how long until a fix is lost, so the line can fade at that moment`() {
        assertEquals(4 * 60_000L, millisUntilFixLost(fix(500.0), t + 60_000L))
        assertEquals("already lost", 0L, millisUntilFixLost(fix(500.0), sixMinutesLater))
        assertNull("no fix, nothing to wait for", millisUntilFixLost(null, t))
    }
}
