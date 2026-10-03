package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.Waypoint
import kotlin.math.cos
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [routeHome] on hand-built tracks (dispatch 2026-09-28-417, way-back-route Part 1; plan task T5).
 *
 * Tracks are laid out on a local grid in metres, x east and y north of a base point, and turned
 * into latitude and longitude by [at]. The haversine then measures them to within a few
 * centimetres of the grid figures, so positions are compared within half a metre.
 *
 * **What a "route point" is here:** the track as recorded, joined to itself wherever it passes
 * within 10 m of itself ([joinedTrackHome]), walked from the most recent stored point back to the
 * first, and then to the origin waypoint if there is one. The lookahead is always one of those
 * points.
 *
 * **The rulings these tests hold** (the dispatch file's "Rulings on the coder's findings"):
 * - The lookahead is (hop + 25 m) along the route from the most recent stored point. The stored
 *   track lags the walker, so it starts from where the walker must at least have got to.
 * - It stops at the last route point for which every route point before it lies within 10 m of
 *   the straight line from the walker. That reuses the self-join ruling's ε.
 * - The route is withheld when the walker is in [HopBand.FAR] of the most recent stored point, and
 *   when there is no usable point.
 */
class RouteHomeTest {

    // ---- The dispatch's named cases --------------------------------------------------------------

    /**
     * An out-and-back walk. Out 100 m north in 10 m steps; back 40 m south over the same ground,
     * so the return points join the outbound ones. The walker stands on the last stored point.
     * The lookahead is 25 m further south along the route: ahead of the walker, toward the start.
     */
    @Test
    fun `out and back - the lookahead is 25 m further along the way home, ahead of the walker`() {
        val track = trackOf(northLine(0..100 step 10) + northLine(90 downTo 60 step 10))

        val route = routeHome(track, current = at(0.0, 60.0), origin = null) as RouteHome.Ahead

        assertNear("25 m on from the walker toward the start; the first route point at or past it is 30 m on", at(0.0, 30.0), route.lookahead)
        assertEquals(30.0, route.lookaheadAlongRouteMeters, 0.5)
        assertFalse(route.aimsAtRouteEnd)
        assertEquals("the route distance is PathHome's", pathHome(track, at(0.0, 60.0), null)!!.totalMeters, route.routeMeters, 1e-9)
    }

    /**
     * The same walk with the stored track lagging: the walker is 22 m south of the last stored
     * point (nothing of the way back stored yet). Measured from the stored point alone, 25 m on
     * would be 3 m ahead of the walker; with the hop added it is 25 m ahead of them.
     */
    @Test
    fun `out and back with the stored track 22 m behind the walker - the lookahead is still 25 m ahead of them`() {
        val track = trackOf(northLine(0..100 step 10))
        val walker = at(0.0, 78.0)

        val route = routeHome(track, current = walker, origin = null) as RouteHome.Ahead

        assertNear("(22 + 25) m along from the stored point at 100 m north: the first route point at or past 53 m north", at(0.0, 50.0), route.lookahead)
        assertTrue("south of the walker, toward the start", route.lookahead.lat < walker.lat)
    }

    /**
     * A loop that has not closed. North 100 m, east 100 m, south 60 m: the walker is on the third
     * side, 40 m north of the line they started on and 100 m east of the start. The start is near
     * across ground they never walked; the route goes back the way they came. The lookahead is on
     * their own third side, behind them as they face, which is the retrace ruling honoured: never
     * across unwalked ground.
     */
    @Test
    fun `a loop not yet closed - the route goes back the way the walker came, and so does the lookahead`() {
        val track = trackOf(northLine(0..100 step 10) + eastLine(10..100 step 10, y = 100.0) + southLine(90 downTo 40 step 10, x = 100.0))

        val route = routeHome(track, current = at(100.0, 40.0), origin = null) as RouteHome.Ahead

        assertNear(at(100.0, 70.0), route.lookahead)
        assertEquals("the whole walk back: 60 + 100 + 100", 260.0, route.routeMeters, 1.0)
    }

    /**
     * The same loop closed: the walker has come round to within 10 m of where they started, the
     * track joins itself there, and the route is the few metres left. The lookahead is the start.
     */
    @Test
    fun `a loop closed - the track joins itself near the start and the lookahead is the start`() {
        val track = trackOf(northLine(0..100 step 10) + eastLine(10..100 step 10, y = 100.0) + southLine(90 downTo 0 step 10, x = 100.0) + westLine(90 downTo 5 step 5, y = 0.0))

        val route = routeHome(track, current = at(5.0, 0.0), origin = null) as RouteHome.Ahead

        assertNear(at(0.0, 0.0), route.lookahead)
        assertTrue("short: the join, not the loop", route.routeMeters < 10.0)
        assertTrue(route.aimsAtRouteEnd)
    }

    /**
     * A sharp bend. The way home runs west 40 m along an east-west leg, turns a right angle and
     * runs south. The walker is 15 m east of the corner. 25 m on is 10 m past the corner, so the
     * straight line to it passes inside the corner: by 8.3 m, which is inside the 10 m the guard
     * allows. This holds the property the dispatch asks for: no route point between the walker
     * and the lookahead is more than 10 m off the line the needle points along.
     */
    @Test
    fun `a sharp bend - the line to the lookahead never leaves the walked route by more than 10 m`() {
        val track = trackOf(northLine(0..100 step 5) + eastLine(5..40 step 5, y = 100.0))
        val walker = at(15.0, 100.0)

        // The walker stands 25 m back along the leg from the last stored point (40 m east): hop 25.
        val route = routeHome(track, current = walker, origin = null) as RouteHome.Ahead

        val beforeLookahead = routePointsUpTo(track, route.lookahead)
        val worst = beforeLookahead.maxOf { offLine(it, walker, route.lookahead) }
        assertTrue("worst route point off the needle's line: $worst m", worst <= SELF_JOIN_EPSILON_METERS + 0.1)
        assertTrue("and the lookahead is past the corner, on the way home", route.lookahead.lat < at(0.0, 100.0).lat)
    }

    /**
     * A walker 30 m east of the path: still on the route ([HopBand.COUNTED]), so it is shown.
     * (hop + 25) m along would be 55 m on, and the line from the walker to there would pass the
     * last stored point 26 m off. The guard stops the lookahead at the last route point whose line
     * keeps every point before it within 10 m.
     */
    @Test
    fun `a walker 30 m off the path is still on the route, and the lookahead stops where the line to it stays within 10 m`() {
        val track = trackOf(northLine(0..100 step 5))
        val walker = at(30.0, 100.0)

        val route = routeHome(track, current = walker, origin = null) as RouteHome.Ahead

        assertEquals(HopBand.COUNTED, route.hopBand)
        assertNear("the route point 10 m on from the last stored point: 15 m on would put it 12 m off the line", at(0.0, 90.0), route.lookahead)
        assertTrue(route.lookaheadAlongRouteMeters < 30.0 + ROUTE_LOOKAHEAD_METERS)
    }

    /** A walker 60 m from the last stored point is off the route: withheld, with its reason. */
    @Test
    fun `a walker 60 m off the path is off the route - withheld, with the reason`() {
        val track = trackOf(northLine(0..100 step 5))

        val route = routeHome(track, current = at(60.0, 100.0), origin = null)

        assertEquals(RouteHome.Withheld(RouteWithheldReason.OFF_ROUTE, HopBand.FAR), route)
    }

    /** The FAR band's own hysteresis, carried by the caller: once off the route, back only below 45 m. */
    @Test
    fun `back from off the route only below 45 m, as PathHome's far band`() {
        val track = trackOf(northLine(0..100 step 5))

        val at47 = routeHome(track, current = at(47.0, 100.0), origin = null, previousHopBand = HopBand.FAR)
        val at44 = routeHome(track, current = at(44.0, 100.0), origin = null, previousHopBand = HopBand.FAR)

        assertEquals(RouteHome.Withheld(RouteWithheldReason.OFF_ROUTE, HopBand.FAR), at47)
        assertEquals(HopBand.COUNTED, (at44 as RouteHome.Ahead).hopBand)
    }

    /** A track shorter than the lookahead: it aims at the route's end, the origin waypoint when there is one. */
    @Test
    fun `a track too short for 25 m of lookahead aims at the route's end, the origin when there is one`() {
        val track = trackOf(northLine(0..15 step 5))
        val origin = Waypoint(id = "wp-origin", lat = at(0.0, -3.0).lat, lng = at(0.0, -3.0).lng, altitude = null, name = "Start", note = "", createdAtEpochMillis = 0L, trackId = "t")

        val withOrigin = routeHome(track, current = at(0.0, 15.0), origin = origin) as RouteHome.Ahead
        val withoutOrigin = routeHome(track, current = at(0.0, 15.0), origin = null) as RouteHome.Ahead

        assertNear(at(0.0, -3.0), withOrigin.lookahead)
        assertTrue(withOrigin.aimsAtRouteEnd)
        assertNear(at(0.0, 0.0), withoutOrigin.lookahead)
        assertTrue(withoutOrigin.aimsAtRouteEnd)
        assertEquals(pathHome(track, at(0.0, 15.0), origin)!!.totalMeters, withOrigin.routeMeters, 1e-9)
    }

    /** No usable points: withheld, never a straight-line stand-in. */
    @Test
    fun `a track with no usable points is withheld, with the reason`() {
        val route = routeHome(trackOf(emptyList()), current = at(0.0, 0.0), origin = null)

        assertEquals(RouteHome.Withheld(RouteWithheldReason.NO_USABLE_POINTS, null), route)
    }

    // ---- Said plainly by the planner's ruling: where it still lands behind -----------------------

    /**
     * **The case where the lookahead still lands behind the walker, shown, not hidden.** The way
     * home from the last stored point runs 30 m north, 12 m east, then south past the walker:
     * a U the walker has already walked round inside the lag. In a straight line they are only
     * 12 m from the stored point, so (hop + 25) m along is still on the U's first side, and the
     * guard stops it earlier still. The lookahead is 20 m along the route; the walker is about
     * 74 m along. The needle points back up the U.
     *
     * Not fixed: the ruling chose this rule knowing the case exists, and the lag behind it is the
     * service's batching, which is off limits here (reported as a finding).
     */
    @Test
    fun `a path that winds hard inside the lag - the lookahead lands behind the walker, as ruled`() {
        // Recorded order: from home at (12,-60) north to (12,30), west to (0,30), south to (0,0).
        val track = trackOf(northLine(-60..30 step 5, x = 12.0) + westLine(7 downTo 2 step 5, y = 30.0) + listOf(at(0.0, 30.0)) + southLine(25 downTo 0 step 5, x = 0.0))
        val walker = at(12.0, -2.0)

        val route = routeHome(track, current = walker, origin = null) as RouteHome.Ahead

        assertNear("on the U's first side, 20 m along: behind the walker, who is about 74 m along", at(0.0, 20.0), route.lookahead)
        assertEquals(20.0, route.lookaheadAlongRouteMeters, 0.5)
    }

    // ---- The route distance is PathHome's, on every shape above ----------------------------------

    @Test
    fun `the route distance is the same number PathHome gives, on every shape`() {
        val shapes = listOf(
            trackOf(northLine(0..100 step 10) + northLine(90 downTo 60 step 10)) to at(0.0, 60.0),
            trackOf(northLine(0..100 step 10)) to at(0.0, 78.0),
            trackOf(northLine(0..100 step 10) + eastLine(10..100 step 10, y = 100.0) + southLine(90 downTo 40 step 10, x = 100.0)) to at(100.0, 40.0),
            trackOf(northLine(0..100 step 5)) to at(30.0, 100.0),
        )
        shapes.forEach { (track, walker) ->
            val expected = pathHome(track, walker, null)!!
            val route = routeHome(track, walker, null) as RouteHome.Ahead
            assertEquals(expected.totalMeters, route.routeMeters, 1e-9)
            assertEquals(expected.hopBand, route.hopBand)
        }
    }

    // ---- Helpers ---------------------------------------------------------------------------------

    private fun at(x: Double, y: Double) = LatLng(BASE_LAT + y / METERS_PER_DEGREE, BASE_LNG + x / (METERS_PER_DEGREE * cos(Math.toRadians(BASE_LAT))))

    private fun northLine(ys: IntProgression, x: Double = 0.0) = ys.map { at(x, it.toDouble()) }
    private fun southLine(ys: IntProgression, x: Double) = ys.map { at(x, it.toDouble()) }
    private fun eastLine(xs: IntProgression, y: Double) = xs.map { at(it.toDouble(), y) }
    private fun westLine(xs: IntProgression, y: Double) = xs.map { at(it.toDouble(), y) }

    private fun trackOf(points: List<LatLng>) = Track(
        id = "t",
        name = null,
        startedAtEpochMillis = 0L,
        endedAtEpochMillis = null,
        points = points.mapIndexed { i, p -> TrackPoint(lat = p.lat, lng = p.lng, altitude = null, accuracyMeters = null, timestampEpochMillis = i * 5_000L) },
    )

    /** The route's points, in route order, up to (not including) the one at [lookahead]. Uses the same route the function walks. */
    private fun routePointsUpTo(track: Track, lookahead: LatLng): List<LatLng> {
        val route = joinedTrackRoute(track.points).route.map { LatLng(track.points[it].lat, track.points[it].lng) }
        return route.takeWhile { GeoDistance.metersBetween(it, lookahead) > 0.5 }
    }

    /** Metres from [p] to the straight segment [a]-[b], on a flat local grid. */
    private fun offLine(p: LatLng, a: LatLng, b: LatLng): Double {
        fun xy(q: LatLng) = Pair((q.lng - a.lng) * METERS_PER_DEGREE * cos(Math.toRadians(a.lat)), (q.lat - a.lat) * METERS_PER_DEGREE)
        val (bx, by) = xy(b)
        val (px, py) = xy(p)
        val len2 = bx * bx + by * by
        val t = if (len2 == 0.0) 0.0 else ((px * bx + py * by) / len2).coerceIn(0.0, 1.0)
        return hypot(px - t * bx, py - t * by)
    }

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
