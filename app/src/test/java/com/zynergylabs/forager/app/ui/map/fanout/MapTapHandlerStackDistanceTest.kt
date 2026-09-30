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
 * The stacking distance (dispatch 2026-09-29-57, item 6, amendment -255; the owner: "32 dp"): two
 * markers are a stack, and fan out on a tap, only when both axis distances between them are under 32 dp.
 * Their touch squares (48 dp, `FAN_TOUCH_DP`) may overlap without that: the owner's example is two find
 * glyphs about 15 dp apart horizontally and 35 dp vertically, which used to fan and now open their own
 * bubbles. Every tap goes through [MapTapHandler.onMapTap], the one entry `SightingsMap`'s click listener
 * calls; the SDK behind it is the test's own projection ([FanOutTestScene]).
 */
class MapTapHandlerStackDistanceTest {

    private val scene = FanOutTestScene(density = 2f)
    private val fan = MarkerFanOutState()
    private val sinks = RecordingSinks()
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = sinks,
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private val originXPx = 400f
    private val originYPx = 800f

    private fun dp(value: Float) = value * scene.density

    private fun tapAt(xPx: Float, yPx: Float) = handler.onMapTap(LatLng(scene.centre.lat, scene.centre.lng), xPx, yPx)

    /** Two finds, "a" at the origin and "b" [dxDp] right and [dyDp] down of it; a real tap on "a". */
    private fun tapAWithBAt(dxDp: Float, dyDp: Float) {
        scene.addAtScreen(MapLayerIds.FINDS, "a", originXPx, originYPx)
        scene.addAtScreen(MapLayerIds.FINDS, "b", originXPx + dp(dxDp), originYPx + dp(dyDp))
        tapAt(originXPx, originYPx)
    }

    @Test
    fun `the owner's pair, 15 dp apart across and 35 dp down, does not fan and each opens its own bubble`() {
        tapAWithBAt(15f, 35f)
        assertFalse("35 dp apart vertically is not a stack", fan.isOpen)
        assertEquals(listOf("feature:${MapLayerIds.FINDS}:a"), sinks.events)

        sinks.events.clear()
        tapAt(originXPx + dp(15f), originYPx + dp(35f))
        assertFalse(fan.isOpen)
        assertEquals("a real tap on the other glyph opens that one's bubble", listOf("feature:${MapLayerIds.FINDS}:b"), sinks.events)
    }

    @Test
    fun `two finds 20 dp apart are a stack and fan`() {
        tapAWithBAt(0f, 20f)
        assertTrue(fan.isOpen)
        assertEquals(emptyList<String>(), sinks.events)
        assertEquals(setOf("a", "b"), fan.members.map { it.key.featureId }.toSet())
    }

    @Test
    fun `vertical boundary - 31 dp apart fans, 32 dp apart does not`() {
        tapAWithBAt(0f, 31f)
        assertTrue("31 dp down is under the stacking distance", fan.isOpen)
    }

    @Test
    fun `vertical boundary - exactly 32 dp apart does not fan`() {
        tapAWithBAt(0f, 32f)
        assertFalse("32 dp down is not under the stacking distance", fan.isOpen)
        assertEquals(listOf("feature:${MapLayerIds.FINDS}:a"), sinks.events)
    }

    @Test
    fun `horizontal boundary - 31 dp apart fans, 32 dp apart does not`() {
        tapAWithBAt(31f, 0f)
        assertTrue("31 dp across is under the stacking distance", fan.isOpen)
    }

    @Test
    fun `horizontal boundary - exactly 32 dp apart does not fan`() {
        tapAWithBAt(32f, 0f)
        assertFalse("32 dp across is not under the stacking distance", fan.isOpen)
        assertEquals(listOf("feature:${MapLayerIds.FINDS}:a"), sinks.events)
    }

    @Test
    fun `the stacking distance is its own constant and the touch size is unchanged`() {
        assertEquals(32f, FAN_STACK_DP, 0f)
        assertEquals("the owner's touch floor stays 48 dp", 48f, FAN_TOUCH_DP, 0f)
    }
}
