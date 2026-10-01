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
 * An open fan stays open when the map moves by itself and closes when the user moves it (dispatch 2026-09-28-380), and its touch areas are where its
 * icons are drawn after the map has moved. The handler is the real class; the map SDK behind it is [FanOutTestScene]. Which cause a given camera move
 * has is [CameraMoveClassifierTest]'s and the recordings'.
 */
class MapTapHandlerFanHoldsTest {

    private val scene = FanOutTestScene()
    private val fan = MarkerFanOutState()
    private val sinks = RecordingSinks()
    private val warnings = mutableListOf<String>()
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = sinks,
        warn = { warnings += it },
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private fun tapAtSpot() = handler.onMapTap(LatLng(scene.centre.lat, scene.centre.lng), scene.xPx(scene.centre.lng), scene.yPx(scene.centre.lat))

    private fun tapAtPx(xPx: Float, yPx: Float) = handler.onMapTap(LatLng(scene.centre.lat, scene.centre.lng), xPx, yPx)

    private fun openAFan(): List<FanMember> {
        scene.add(MapLayerIds.PHOTOS, "p1")
        scene.add(MapLayerIds.PHOTOS, "p2")
        scene.add(MapLayerIds.FINDS, "f1")
        tapAtSpot()
        fan.progress = 1f
        assertTrue("the fan is open", fan.isOpen)
        return fan.members
    }

    /** Where [m] is drawn now, in px of the map view: where the map has put its marker, plus the fan's own offset. */
    private fun drawnPx(m: FanMember): Pair<Float, Float> {
        val marker = scene.markersOf(listOf(m.key)).single()
        return (marker.xPx + m.offset.xDp * scene.density) to (marker.yPx + m.offset.yDp * scene.density)
    }

    @Test
    fun `a location-following move leaves an open fan open`() {
        openAFan()

        handler.onCameraMoveStarted(CameraMoveCause.LOCATION_FOLLOW)

        assertTrue(fan.isOpen)
        assertEquals("nothing folds it: it is still fully open", 1f, fan.progress, 0f)
    }

    @Test
    fun `a run of follower moves, small or large, never closes it`() {
        openAFan()

        repeat(40) { handler.onCameraMoveStarted(CameraMoveCause.LOCATION_FOLLOW) }

        assertTrue(fan.isOpen)
    }

    @Test
    fun `the user's touch on the map closes it`() {
        openAFan()

        handler.onCameraMoveStarted(CameraMoveCause.USER_GESTURE)

        assertFalse(fan.isOpen)
    }

    @Test
    fun `a move the user asked for closes it`() {
        openAFan()

        handler.onCameraMoveStarted(CameraMoveCause.APP_REQUESTED)

        assertFalse(fan.isOpen)
    }

    @Test
    fun `a move nothing is known to cause closes it, as before`() {
        openAFan()

        handler.onCameraMoveStarted(CameraMoveCause.UNKNOWN)

        assertFalse(fan.isOpen)
    }

    @Test
    fun `a fan the map has carried off screen is not closed for being off screen`() {
        openAFan()
        scene.panXPx = 100_000f // far outside any viewport

        handler.onCameraMoveStarted(CameraMoveCause.LOCATION_FOLLOW)

        assertTrue("no rule closes a fan for being off screen", fan.isOpen)
    }

    @Test
    fun `after the map has moved under an open fan, a tap where an icon is now drawn finds it`() {
        val members = openAFan()
        val target = members.first()
        scene.panXPx = 240f // the map moved under the fan; the fan's icons moved with it
        scene.panYPx = -180f
        val (x, y) = drawnPx(target)

        tapAtPx(x, y)

        assertTrue("the fan stays open behind the bubble", fan.isOpen)
        assertEquals(listOf("feature:${target.key.layerId}:${target.key.featureId}"), sinks.events)
    }

    @Test
    fun `after the map has moved, a tap where the icon used to be does not find it`() {
        val members = openAFan()
        val target = members.first()
        val (oldX, oldY) = drawnPx(target)
        scene.panXPx = 240f
        scene.panYPx = -180f

        tapAtPx(oldX, oldY)

        assertTrue("an empty tap folds the fan and reports nothing about the old icon", sinks.events.none { it.contains(target.key.featureId) })
        assertFalse(fan.isOpen)
    }

    @Test
    fun `the hit test finds hidden originals' places, so no tap takes the fallback`() {
        val members = openAFan()
        scene.panXPx = 60f
        val (x, y) = drawnPx(members.last())

        tapAtPx(x, y)

        assertEquals("the originals are hidden by the fan and still located: no fallback was needed", emptyList<String>(), warnings)
    }

    @Test
    fun `a member the map cannot locate keeps its stored place, and says so`() {
        val members = openAFan()
        val lost = members.first()
        val (storedX, storedY) = (lost.trueXDp + lost.offset.xDp) * scene.density to (lost.trueYDp + lost.offset.yDp) * scene.density
        scene.markers.removeAll { it.layerId == lost.key.layerId && it.featureId == lost.key.featureId }

        tapAtPx(storedX, storedY)

        assertEquals(listOf("feature:${lost.key.layerId}:${lost.key.featureId}"), sinks.events)
        assertTrue("the fallback is reported: $warnings", warnings.any { it.contains(lost.key.featureId) })
    }
}
