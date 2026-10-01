package com.zynergylabs.forager.app.ui.map.fanout

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A tap on a stack with a bubble showing (dispatch 2026-09-28-387, Part A; the owner, "1 yes"):
 * `Fan A open, X's bubble over it > tap stack B > X's bubble closes, fan A closes, fan B opens`, and with no fan open, the bubble closes and the
 * fan opens. The bubble is closed through its own sink, not the plain tap, which also leaves fullscreen. The handler is the real class; the map SDK behind it is
 * [FanOutTestScene]; "a bubble is up" is the host's flag, as `SightingsMap` passes it.
 */
class MapTapHandlerBubbleStackTest {

    private val scene = FanOutTestScene()
    private val fan = MarkerFanOutState()
    private val sinks = RecordingSinks()
    private var bubbleUp = false
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = sinks,
        bubbleOpen = { bubbleUp },
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private fun tapAtSpot() = tapAt(scene.xPx(scene.centre.lng), scene.yPx(scene.centre.lat))

    private fun tapAt(xPx: Float, yPx: Float) = handler.onMapTap(LatLng(scene.centre.lat, scene.centre.lng), xPx, yPx)

    /** Stack A (a find and a photo on the centre spot), fanned out fully. */
    private fun openFanA(): List<FanMember> {
        scene.add(MapLayerIds.FINDS, "a-find", lat = 45.0001, lng = -122.0001)
        scene.add(MapLayerIds.PHOTOS, "a-photo")
        tapAtSpot()
        fan.progress = 1f
        assertTrue("fan A is open", fan.isOpen)
        sinks.events.clear()
        return fan.members
    }

    /** Stack B, a waypoint and a planned trip together, well clear of fan A. */
    private fun addStackB() {
        scene.addAtScreen(MapLayerIds.WAYPOINTS, "b-waypoint", 100f, 1500f)
        scene.addAtScreen(MapLayerIds.PLANNED_TRIPS, "b-trip", 100f, 1500f)
    }

    private fun drawnPx(m: FanMember): Pair<Float, Float> {
        val marker = scene.markersOf(listOf(m.key)).single()
        return (marker.xPx + m.offset.xDp * scene.density) to (marker.yPx + m.offset.yDp * scene.density)
    }

    // The step the owner confirmed.

    @Test
    fun `fan A and a bubble over it, a tap on stack B - the bubble closes, fan A folds and fan B opens`() {
        openFanA()
        addStackB()
        bubbleUp = true

        tapAt(100f, 1500f)

        assertEquals("fan B is the open fan", setOf("b-waypoint", "b-trip"), fan.members.map { it.key.featureId }.toSet())
        assertTrue(fan.isOpen)
        assertEquals("the bubble was closed, once, and not by the plain tap, which also leaves fullscreen", listOf("closeBubble"), sinks.events)
    }

    @Test
    fun `no fan and a bubble showing, a tap on a stack closes the bubble and opens the fan`() {
        scene.add(MapLayerIds.FINDS, "a-find", lat = 45.0001, lng = -122.0001)
        scene.add(MapLayerIds.PHOTOS, "a-photo")
        bubbleUp = true

        tapAtSpot()

        assertTrue("the stack fanned", fan.isOpen)
        assertEquals(listOf("closeBubble"), sinks.events)
    }

    // What stays as it was.

    @Test
    fun `a tap on a stack with no bubble showing opens the fan and reports nothing`() {
        scene.add(MapLayerIds.FINDS, "a-find", lat = 45.0001, lng = -122.0001)
        scene.add(MapLayerIds.PHOTOS, "a-photo")
        bubbleUp = false

        tapAtSpot()

        assertTrue(fan.isOpen)
        assertTrue(sinks.events.isEmpty())
    }

    @Test
    fun `a tap on a single marker with a bubble showing gives that marker's outcome and no extra plain tap`() {
        openFanA()
        scene.addAtScreen(MapLayerIds.WAYPOINTS, "single", 100f, 1700f)
        bubbleUp = true

        tapAt(100f, 1700f)

        assertEquals(listOf("feature:${MapLayerIds.WAYPOINTS}:single"), sinks.events)
        assertFalse("the open fan folds, as it did", fan.isOpen)
    }

    @Test
    fun `a tap on another icon of the open fan gives its outcome and the fan stays open`() {
        val members = openFanA()
        val other = members.first { it.key.featureId == "a-photo" }
        val (x, y) = drawnPx(other)
        bubbleUp = true

        tapAt(x, y)

        assertEquals(listOf("feature:${MapLayerIds.PHOTOS}:a-photo"), sinks.events)
        assertTrue("the fan stays open behind the bubble", fan.isOpen)
    }

    @Test
    fun `a tap on empty map with a bubble over the fan closes only the bubble, and the next tap folds the fan`() {
        openFanA()
        bubbleUp = true

        tapAt(900f, 1900f)
        assertEquals("the plain tap closes the bubble", listOf("plain"), sinks.events)
        assertTrue("and the fan stays", fan.isOpen)

        bubbleUp = false
        sinks.events.clear()
        tapAt(900f, 1900f)
        assertFalse("the next one folds it", fan.isOpen)
        assertEquals(listOf("plain"), sinks.events)
    }
}
