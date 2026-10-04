package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import kotlin.math.cos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dispatch 2026-09-28-497 (plan task T7): the way back as a line. [routeHome] exposes the whole route
 * and the part ahead of the walker's assumed place; [nextRouteLine] keeps the route as it was at
 * Return (drawn dimmed, so whatever the walker has passed shows grey) and the latest route ahead
 * (drawn bright), and keeps the last line, faded, while the route is withheld.
 */
class RouteLineTest {

    private val outAndBack = trackOf(northLine(0..100 step 10))

    @Test
    fun `the route runs from the most recent stored point to the start, and on to the origin`() {
        val origin = waypoint(at(5.0, 0.0))
        val ahead = routeHome(outAndBack, current = at(0.0, 100.0), origin = origin) as RouteHome.Ahead
        assertNear("starts at the most recent stored point", at(0.0, 100.0), ahead.route.first())
        assertNear("ends at the origin", at(5.0, 0.0), ahead.route.last())
        assertEquals("every stored point, then the origin", 12, ahead.route.size)
        assertEquals("the walker stands on the last point: all of it is ahead", ahead.route, ahead.routeAhead)
    }

    @Test
    fun `the part ahead starts at the walker's assumed place, hop metres along, not at a projection`() {
        // The stored track lags: the walker is 22 m south of the last stored point.
        val ahead = routeHome(outAndBack, current = at(0.0, 78.0), origin = null) as RouteHome.Ahead
        assertNear(at(0.0, 100.0), ahead.route.first())
        assertNear("the first route point at least 22 m along", at(0.0, 70.0), ahead.routeAhead.first())
        assertNear(at(0.0, 0.0), ahead.routeAhead.last())
        assertEquals(ahead.route.drop(3), ahead.routeAhead)
    }

    @Test
    fun `the first route of a return is kept as the line at Return, later ones only move the line ahead`() {
        val first = ahead(route = line(100, 90, 80, 0), routeAhead = line(100, 90, 80, 0))
        val atReturn = nextRouteLine(null, first)!!
        assertEquals(line(100, 90, 80, 0), atReturn.atReturn)
        assertEquals(line(100, 90, 80, 0), atReturn.ahead)
        assertTrue(atReturn.aheadIsCurrent)

        val later = nextRouteLine(atReturn, ahead(route = line(60, 50, 0), routeAhead = line(50, 0)))!!
        assertEquals("the line at Return stays, so what has been passed shows dimmed", line(100, 90, 80, 0), later.atReturn)
        assertEquals(line(50, 0), later.ahead)
        assertTrue(later.aheadIsCurrent)
    }

    @Test
    fun `a withheld route keeps the last line ahead, faded, until a fresh route returns`() {
        val drawn = nextRouteLine(null, ahead(route = line(100, 0), routeAhead = line(100, 0)))!!
        val withheld = nextRouteLine(drawn, RouteHome.Withheld(RouteWithheldReason.OFF_ROUTE, HopBand.FAR))!!
        assertEquals(line(100, 0), withheld.ahead)
        assertEquals(line(100, 0), withheld.atReturn)
        assertFalse("faded", withheld.aheadIsCurrent)

        val back = nextRouteLine(withheld, ahead(route = line(40, 0), routeAhead = line(40, 0)))!!
        assertTrue(back.aheadIsCurrent)
        assertEquals(line(40, 0), back.ahead)
    }

    @Test
    fun `with no line yet, a withheld route draws nothing`() {
        assertNull(nextRouteLine(null, RouteHome.Withheld(RouteWithheldReason.NO_USABLE_POINTS, null)))
    }

    private fun ahead(route: List<LatLng>, routeAhead: List<LatLng>) = RouteHome.Ahead(
        lookahead = routeAhead.first(),
        routeMeters = 0.0,
        hopBand = HopBand.NONE,
        lookaheadAlongRouteMeters = 0.0,
        aimsAtRouteEnd = false,
        pathHome = PathHome(trackMeters = 0.0, hopMeters = 0.0, hopBand = HopBand.NONE, originLegMeters = null, pointCount = 2, joinsOnRoute = 0),
        route = route,
        routeAhead = routeAhead,
    )

    private fun line(vararg ys: Int) = ys.map { at(0.0, it.toDouble()) }

    private fun at(x: Double, y: Double) = LatLng(BASE_LAT + y / METERS_PER_DEGREE, BASE_LNG + x / (METERS_PER_DEGREE * cos(Math.toRadians(BASE_LAT))))

    private fun northLine(ys: IntProgression) = ys.map { at(0.0, it.toDouble()) }

    private fun trackOf(points: List<LatLng>) = Track(
        id = "t",
        name = null,
        startedAtEpochMillis = 0L,
        endedAtEpochMillis = null,
        points = points.mapIndexed { i, p -> TrackPoint(lat = p.lat, lng = p.lng, altitude = null, accuracyMeters = null, timestampEpochMillis = i * 5_000L) },
    )

    private fun waypoint(p: LatLng) = Waypoint(
        id = "origin", lat = p.lat, lng = p.lng, altitude = null, name = "Start", note = "",
        createdAtEpochMillis = 0L, trackId = "t", designation = WaypointDesignation.ORIGIN,
    )

    private fun assertNear(expected: LatLng, actual: LatLng) = assertNear("", expected, actual)

    private fun assertNear(message: String, expected: LatLng, actual: LatLng) {
        val off = GeoDistance.metersBetween(expected, actual)
        assertTrue("$message: expected within 0.5 m of $expected, was $actual, $off m off", off <= 0.5)
    }

    private companion object {
        const val BASE_LAT = 45.0
        const val BASE_LNG = -122.0
        const val METERS_PER_DEGREE = 111_195.08
    }
}
