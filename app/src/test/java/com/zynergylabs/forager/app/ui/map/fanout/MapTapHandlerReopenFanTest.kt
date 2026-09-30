package com.zynergylabs.forager.app.ui.map.fanout

import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The new "open fan for these keys" path (dispatch 2026-09-29-57, item 8, amendment -262 "Remember and reopen"): the
 * fan a user left when they opened a find on the Journal comes back when they return by Back, through
 * [MapTapHandler.openFanFor]. A member that no longer exists is left out; fewer than two remaining opens nothing.
 * The SDK behind the probe is the test's own projection ([FanOutTestScene]).
 */
class MapTapHandlerReopenFanTest {

    private val scene = FanOutTestScene(density = 2f)
    private val fan = MarkerFanOutState()
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = RecordingSinks(),
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private val find = { id: String -> FanKey(MapLayerIds.FINDS, id) }

    private fun stackOfThree() {
        scene.addAtScreen(MapLayerIds.FINDS, "a", 400f, 800f)
        scene.addAtScreen(MapLayerIds.FINDS, "b", 410f, 810f)
        scene.addAtScreen(MapLayerIds.PHOTOS, "c", 405f, 795f)
    }

    @Test
    fun `the keys a fan had reopen it with those members`() {
        stackOfThree()

        val opened = handler.openFanFor(listOf(find("a"), find("b"), FanKey(MapLayerIds.PHOTOS, "c")))

        assertTrue("a fan opened", opened)
        assertTrue(fan.isOpen)
        assertEquals(setOf(find("a"), find("b"), FanKey(MapLayerIds.PHOTOS, "c")), fan.members.map { it.key }.toSet())
    }

    @Test
    fun `a member that no longer exists is left out, and the rest still fan`() {
        stackOfThree()

        val opened = handler.openFanFor(listOf(find("a"), find("b"), FanKey(MapLayerIds.PHOTOS, "gone")))

        assertTrue(opened)
        assertEquals("the deleted record is not in the fan", setOf(find("a"), find("b")), fan.members.map { it.key }.toSet())
    }

    @Test
    fun `fewer than two members left opens no fan and says so`() {
        stackOfThree()

        val opened = handler.openFanFor(listOf(find("a"), find("gone-1"), find("gone-2")))

        assertFalse("one survivor is not a fan", opened)
        assertFalse(fan.isOpen)
        assertEquals(emptyList<FanMember>(), fan.members)
    }

    @Test
    fun `no keys opens nothing`() {
        stackOfThree()
        assertFalse(handler.openFanFor(emptyList()))
        assertFalse(fan.isOpen)
    }

    @Test
    fun `the reopened fan is the same fan a tap on the stack opens, member for member`() {
        stackOfThree()
        handler.onMapTap(com.zynergylabs.forager.app.domain.model.LatLng(scene.centre.lat, scene.centre.lng), 400f, 800f)
        val tapped = fan.members
        assertTrue("a tap on the stack opened it", fan.isOpen && tapped.size == 3)
        fan.release()
        scene.hidden = { emptySet() }

        handler.openFanFor(tapped.map { it.key })

        assertEquals("same members in the same order and places", tapped, fan.members)
    }
}
