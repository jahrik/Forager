package com.zynergylabs.forager.app.ui.map.fanout

import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dispatch 2026-09-28-267, Part A (item 8c). On the device the reopened fan opened at a camera idle and was folded
 * 2 ms later by the effect that folds a fan when the loaded style changes (`onContentChanged`), so it never showed.
 * These drive the real [MapTapHandler] and [MarkerFanOutState] through the map's two events in both orders.
 */
class FanReopenCoordinatorTest {

    private val scene = FanOutTestScene(density = 2f)
    private val fan = MarkerFanOutState()
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = RecordingSinks(),
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private val keys = listOf(FanKey(MapLayerIds.FINDS, "a"), FanKey(MapLayerIds.FINDS, "b"), FanKey(MapLayerIds.PHOTOS, "c"))
    private var waiting: List<FanKey>? = keys
    private val unavailable = mutableListOf<Int>()
    private val reopen = FanReopenCoordinator({ handler }, { waiting.also { waiting = null } }, { unavailable += it })

    init {
        scene.addAtScreen(MapLayerIds.FINDS, "a", 400f, 800f)
        scene.addAtScreen(MapLayerIds.FINDS, "b", 410f, 810f)
        scene.addAtScreen(MapLayerIds.PHOTOS, "c", 405f, 795f)
    }

    @Test
    fun `an idle before the style-loaded content effect still leaves the fan open`() {
        // The order the device logged: style loaded, camera idle (with the style), then the effect keyed on the style.
        reopen.onCameraIdle(styleLoaded = true)
        reopen.onContentEffect(styleLoaded = true)

        assertTrue("the fan is open after both events: the effect folded it", fan.isOpen)
        assertEquals(keys.toSet(), fan.members.map { it.key }.toSet())
    }

    @Test
    fun `the content effect before the idle leaves the fan open`() {
        reopen.onContentEffect(styleLoaded = true)
        reopen.onCameraIdle(styleLoaded = true)

        assertTrue(fan.isOpen)
    }

    @Test
    fun `an idle before the style has loaded neither reopens nor spends the keys`() {
        reopen.onCameraIdle(styleLoaded = false)
        assertEquals(false, fan.isOpen)
        assertEquals(keys, waiting)
    }

    @Test
    fun `a later content change folds the reopened fan`() {
        reopen.onContentEffect(styleLoaded = true)
        reopen.onCameraIdle(styleLoaded = true)
        reopen.onContentEffect(styleLoaded = true)

        assertEquals(false, fan.isOpen)
    }
}
