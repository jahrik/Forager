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
 */
class StraightLineToTargetTest {

    private val t = 1_700_000_000_000L
    private val creek = Waypoint("wp-1", 45.326, -122.634, null, "Creek pin", "", 1_000L)

    private fun fix(metersSouth: Double, at: Long = t) =
        LocationFix.Update(lat = creek.lat - metersSouth / 111_195.08, lng = creek.lng, altitude = null, accuracyMeters = 3.79f, timestampEpochMillis = at)

    @Test
    fun `from the walker's fix to the waypoint`() {
        val from = fix(500.0)
        assertEquals(listOf(LatLng(from.lat, from.lng), LatLng(creek.lat, creek.lng)), straightLineToTarget(from, creek, t + 1_000L))
    }

    @Test
    fun `no fix, or a lost one, draws no line`() {
        assertNull(straightLineToTarget(null, creek, t))
        assertNull("six minutes old is lost", straightLineToTarget(fix(500.0), creek, t + 6 * 60_000L))
    }

    @Test
    fun `within 15 m the walker has arrived and the line ends`() {
        assertNull(straightLineToTarget(fix(12.0), creek, t + 1_000L))
        assertEquals(2, straightLineToTarget(fix(18.0), creek, t + 1_000L)?.size)
    }
}
