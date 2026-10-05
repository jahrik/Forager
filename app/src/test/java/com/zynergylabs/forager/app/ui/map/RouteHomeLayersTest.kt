package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.RouteLine
import com.zynergylabs.forager.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

/**
 * Dispatch 2026-09-28-497 (plan task T7): what the map's three route sources receive. The line at
 * Return goes to the dimmed, grey source, so whatever the walker has passed shows grey; the line
 * ahead to the bright one, faded while the route is withheld; once arrived the line ahead ends and
 * the start's ring is drawn. How MapLibre draws them is device-only.
 */
class RouteHomeLayersTest {

    private val atReturn = listOf(LatLng(45.0, -122.0), LatLng(45.001, -122.0), LatLng(45.002, -122.0))
    private val ahead = listOf(LatLng(45.001, -122.0), LatLng(45.002, -122.0))

    @Test
    fun `the line at Return goes to the dimmed source, the line ahead to the bright one`() {
        val sources = routeHomeFeatureCollections(RouteOnMap(RouteLine(atReturn, ahead, aheadIsCurrent = true), arrivedAt = null))
        assertEquals(atReturn, lineOf(sources.getValue(RouteHomeIds.PASSED_SOURCE)))
        assertEquals(ahead, lineOf(sources.getValue(RouteHomeIds.AHEAD_SOURCE)))
        assertEquals("no ring before arrival", 0, sources.getValue(RouteHomeIds.ARRIVED_SOURCE).features()!!.size)
        assertEquals("bright while current", 1f, routeAheadOpacity(RouteOnMap(RouteLine(atReturn, ahead, true), null)))
    }

    @Test
    fun `a withheld route keeps its last line ahead, faded`() {
        val route = RouteOnMap(RouteLine(atReturn, ahead, aheadIsCurrent = false), arrivedAt = null)
        assertEquals(ahead, lineOf(routeHomeFeatureCollections(route).getValue(RouteHomeIds.AHEAD_SOURCE)))
        assertEquals(ROUTE_AHEAD_FADED_OPACITY, routeAheadOpacity(route))
        assertTrue("faded is visibly less", ROUTE_AHEAD_FADED_OPACITY < 0.7f)
    }

    @Test
    fun `once arrived the line ahead ends and the ring is drawn at the start`() {
        val start = LatLng(45.0, -122.0)
        val sources = routeHomeFeatureCollections(RouteOnMap(RouteLine(atReturn, ahead, true), arrivedAt = start))
        assertEquals("the line ahead ends", 0, sources.getValue(RouteHomeIds.AHEAD_SOURCE).features()!!.size)
        assertEquals("what was walked stays, dimmed", atReturn, lineOf(sources.getValue(RouteHomeIds.PASSED_SOURCE)))
        val ring = sources.getValue(RouteHomeIds.ARRIVED_SOURCE).features()!!.single().geometry() as Point
        assertEquals(start.lat, ring.latitude(), 0.0)
        assertEquals(start.lng, ring.longitude(), 0.0)
    }

    @Test
    fun `no route draws nothing, and a line of one point is no line`() {
        routeHomeFeatureCollections(null).values.forEach { assertEquals(0, it.features()!!.size) }
        val onePoint = routeHomeFeatureCollections(RouteOnMap(RouteLine(atReturn, listOf(LatLng(45.0, -122.0)), true), null))
        assertEquals(0, onePoint.getValue(RouteHomeIds.AHEAD_SOURCE).features()!!.size)
    }

    private fun lineOf(collection: FeatureCollection): List<LatLng> =
        (collection.features()!!.single().geometry() as LineString).coordinates().map { LatLng(it.latitude(), it.longitude()) }
}
