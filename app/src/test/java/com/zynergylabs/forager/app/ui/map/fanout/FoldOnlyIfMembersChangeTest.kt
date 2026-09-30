package com.zynergylabs.forager.app.ui.map.fanout

import androidx.compose.ui.geometry.Offset
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.MapReturnMemory
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Intent 2026-09-28-274, Step 2. The owner's rule, verbatim: "Fold only if members change". An open fan stays open
 * through map changes that don't touch its members. If a member disappears, the fan re-fans the survivors, or closes
 * if fewer than 2 remain. A layer switch that hides the fan's layer still closes it, because its members disappear.
 *
 * Why it exists: the device log (c-step1-h4-excerpt.txt) showed the reopen after a delete succeeding
 * (`openFanFor` 7 of 7) and a content effect folding it 10 s later, because every content change folded, whether or
 * not a member was in it. Each case goes through the entry point the map's effect calls: [MapTapHandler.onContentChanged]
 * for the handler, and [FanReopenCoordinator.onContentEffect] for the return path.
 *
 * "Present" is the member's key in the content the map now draws ([MapProbe.markersOf]) on a layer that is switched on.
 */
class FoldOnlyIfMembersChangeTest {

    private val scene = FanOutTestScene(density = 2f)
    private val fan = MarkerFanOutState()
    private val switchedOff = mutableSetOf<String>()
    private val handler = MapTapHandler(
        fan = fan,
        probe = scene,
        drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
        sinks = RecordingSinks(),
        layerDrawn = { it !in switchedOff },
    ).also { scene.hidden = { fan.members.map { it.key }.toSet() } }

    private val a = FanKey(MapLayerIds.FINDS, "a")
    private val b = FanKey(MapLayerIds.FINDS, "b")
    private val c = FanKey(MapLayerIds.PHOTOS, "c")

    /** The markers of [keys] within a touch size of one another: a stack, as the device had. Opened by a real tap on it. */
    private fun openStack(vararg keys: FanKey) {
        keys.forEachIndexed { i, key -> scene.add(key.layerId, key.featureId, 45.0 + 0.0001 * (i % 2), -122.0 - 0.0001 * (i / 2)) }
        handler.onMapTap(LatLng(45.0, -122.0), scene.xPx(-122.0), scene.yPx(45.0))
        assertTrue("setup: a tap on the stack opened it", fan.isOpen)
        assertEquals("setup", keys.toSet(), fan.members.map { it.key }.toSet())
    }

    private fun openStackOfThree() = openStack(a, b, c)

    private fun remove(key: FanKey) {
        scene.markers.removeAll { it.layerId == key.layerId && it.featureId == key.featureId }
    }

    @Test
    fun `1 - an unrelated change leaves the open fan open, with the same members and no restart`() {
        openStackOfThree()
        val members = fan.members
        val generation = fan.generation

        // A record added elsewhere, and one deleted elsewhere: the map's content re-emits, no member is touched.
        scene.add(MapLayerIds.FINDS, "elsewhere", 45.2, -122.2)
        scene.add(MapLayerIds.FINDS, "deleted-elsewhere", 45.3, -122.3)
        handler.onContentChanged()
        remove(FanKey(MapLayerIds.FINDS, "deleted-elsewhere"))
        handler.onContentChanged()
        handler.onContentChanged() // and the same content again, as the 10 s effect on the device did

        assertTrue("the fan stayed open through changes that don't touch its members", fan.isOpen)
        assertEquals(members, fan.members)
        assertEquals("it was not folded and reopened either", generation, fan.generation)
    }

    @Test
    fun `2 - a member disappears with two or more surviving, and the fan re-fans the survivors`() {
        openStackOfThree()
        val generation = fan.generation

        remove(b)
        handler.onContentChanged()

        assertTrue(fan.isOpen)
        assertEquals("the survivors, without the deleted one", setOf(a, c), fan.members.map { it.key }.toSet())
        assertNotEquals("the fan was re-opened over the survivors", generation, fan.generation)
    }

    @Test
    fun `3 - a member disappears and fewer than two survive, and the fan folds`() {
        openStackOfThree()

        remove(b)
        remove(c)
        handler.onContentChanged()

        assertFalse("one survivor is not a fan", fan.isOpen)
    }

    @Test
    fun `4a - the fan's layer is switched off so all its members disappear, and it folds`() {
        openStack(a, b) // a fan of two finds

        switchedOff += MapLayerIds.FINDS
        handler.onContentChanged()

        assertFalse("every member's layer is off", fan.isOpen)
    }

    @Test
    fun `4b - a layer switched off takes its members out of the fan, and the rest are re-fanned`() {
        openStackOfThree()

        switchedOff += MapLayerIds.PHOTOS
        handler.onContentChanged()

        assertTrue(fan.isOpen)
        assertEquals("the photo is out; the two finds remain", setOf(a, b), fan.members.map { it.key }.toSet())
    }

    @Test
    fun `7 - a member that moved re-fans at its current position`() {
        openStackOfThree()
        val generation = fan.generation

        val moved = scene.markers.first { it.featureId == "b" }
        scene.markers[scene.markers.indexOf(moved)] = moved.copy(lat = 45.00015)
        handler.onContentChanged()

        assertTrue(fan.isOpen)
        assertEquals(setOf(a, b, c), fan.members.map { it.key }.toSet())
        assertEquals(45.00015, fan.members.first { it.key == b }.lat, 1e-9)
        assertNotEquals("re-fanned over the new place", generation, fan.generation)
    }

    @Test
    fun `a layer switched off since is left out of a reopen, as the reopen's own comment says`() {
        openStackOfThree()
        val keys = fan.members.map { it.key }
        fan.release()
        switchedOff += MapLayerIds.PHOTOS

        assertTrue(handler.openFanFor(keys))

        assertEquals(setOf(a, b), fan.members.map { it.key }.toSet())
    }

    // ---- through the coordinator: the return path, and a style change ----

    private val memory = MapReturnMemory(warn = {})
    private val unavailable = mutableListOf<Int>()
    private val reopen = FanReopenCoordinator({ handler }, { memory.takeFanKeys() }, { unavailable += it })

    @Test
    fun `5 - Back after a delete gives a fan of the survivors, still open after the content effects that follow`() {
        scene.add(a.layerId, a.featureId, 45.0, -122.0)
        scene.add(c.layerId, c.featureId, 45.0, -122.0001)
        // b was in the fan when "Open in Journal" was tapped, and was then deleted from its page.
        memory.openFanKeys = listOf(a, b, c)
        memory.remember("b", Offset(200f, 300f), 0f)
        assertTrue(memory.onFindDeleted("b"))

        // The order the device logged: the new map's style loads and its content effect runs, the camera goes idle,
        // and the reopen fans the survivors. Then the effects that follow, as the one 10 s later.
        reopen.onContentEffect(styleLoaded = true)
        reopen.onCameraIdle(styleLoaded = true)
        assertTrue("setup: the survivors are fanned", fan.isOpen)
        assertEquals(setOf(a, c), fan.members.map { it.key }.toSet())
        reopen.onContentEffect(styleLoaded = true)
        reopen.onContentEffect(styleLoaded = true)

        assertTrue("the fan of the survivors is still open after the effects that follow the delete", fan.isOpen)
        assertEquals(setOf(a, c), fan.members.map { it.key }.toSet())
        assertEquals("nothing was reported unavailable", emptyList<Int>(), unavailable)
    }

    @Test
    fun `6 - a new style still folds an open fan, as before - the owner's rule does not name it`() {
        val firstStyle = Any()
        val secondStyle = Any()
        reopen.onContentEffect(styleLoaded = true, style = firstStyle) // the style's own load
        openStackOfThree()
        reopen.onContentEffect(styleLoaded = true, style = firstStyle) // a content change in the same style
        assertTrue("setup: the same style's effect leaves the fan", fan.isOpen)

        reopen.onContentEffect(styleLoaded = true, style = secondStyle)

        assertFalse("a replaced style folds the fan, unchanged", fan.isOpen)
    }
}
