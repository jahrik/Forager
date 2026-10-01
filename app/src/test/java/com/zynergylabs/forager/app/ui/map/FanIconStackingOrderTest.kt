package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOffset
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-318, fail 5: at the fold's last frames and the open's first, the front glyph changed (the mushroom
 * over the camera, then the camera over the mushroom), because the copies and the originals stacked differently.
 *
 * The originals each draw from their own registry layer, so among different glyphs the registry's layer order decides which
 * is on top. The copies all draw from one layer, [FanOutIds.ICONS_LAYER]; with `icon-allow-overlap` and no sort key the style
 * spec orders such a layer by viewport y, which says nothing about the registry, and at progress 0 the copies share a place.
 * A copy therefore carries its original layer's place in the draw order as [FanOutIds.SORT_KEY_PROPERTY], read by the
 * layer's `symbol-sort-key`. That MapLibre honours the key is device-only; what is asserted here is what is pushed and what
 * the layer is built with.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FanIconStackingOrderTest {

    private fun member(layerId: String, id: String) = FanMember(FanKey(layerId, id), 45.0, -122.0, 0f, 0f, FanOffset(0f, -48f))

    private val coincident = LatLng(45.0, -122.0)

    /** A stack with a pin, a find, a photo and a flag, listed in an order that is not the registry's (nor its reverse). */
    private val stack = listOf(
        member(MapLayerIds.FINDS, "find"),
        member(MapLayerIds.PLANNED_TRIPS, "flag"),
        member(MapLayerIds.PHOTOS, "photo"),
        member(MapLayerIds.WAYPOINTS, "pin"),
    )

    private fun iconsAt(progress: Float, members: List<FanMember> = stack) =
        fanFrameCollections(members, { coincident }, null, progress).icons.features().orEmpty()

    private fun registryPlace(layerId: String) = MAP_LAYER_REGISTRY.indexOfFirst { it.id == layerId }

    private val layerOfFeature = mapOf("find" to MapLayerIds.FINDS, "flag" to MapLayerIds.PLANNED_TRIPS, "photo" to MapLayerIds.PHOTOS, "pin" to MapLayerIds.WAYPOINTS)

    @Test
    fun `every copy carries the place of its original's layer in the draw order, per glyph`() {
        for (progress in listOf(0f, 0.5f, 1f)) {
            val keys = iconsAt(progress).associate { it.getStringProperty("featureId") to it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY)?.toInt() }
            assertEquals(
                "at progress $progress each copy's sort key must be its layer's index in the registry's draw order",
                layerOfFeature.mapValues { registryPlace(it.value) },
                keys,
            )
        }
    }

    @Test
    fun `sorted by sort key the copies stack as their originals' layers do, bottom to top`() {
        val byKey = iconsAt(0f).sortedBy { it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY)?.toInt() ?: -1 }.map { it.getStringProperty("featureId") }
        val byLayer = layerOfFeature.keys.sortedBy { registryPlace(layerOfFeature.getValue(it)) }
        assertEquals(byLayer, byKey)
        assertEquals("the four glyph types must stack in four distinct places", 4, iconsAt(0f).map { it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY)?.toInt() }.toSet().size)
    }

    @Test
    fun `the key follows the draw order it is given, not a fixed table`() {
        val reversed = MAP_LAYER_REGISTRY.reversed()
        val icons = fanFrameCollections(stack, { coincident }, null, 0f, drawOrder = reversed).icons.features().orEmpty()
        val keys = icons.associate { it.getStringProperty("featureId") to it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY)?.toInt() }
        assertEquals(layerOfFeature.mapValues { entry -> reversed.indexOfFirst { it.id == entry.value } }, keys)
    }

    @Test
    fun `the icon layer sorts by that property`() {
        val sortKey = fanIconLayerProperties().firstOrNull { it.name == "symbol-sort-key" }
        assertNotNull("the fan's icon layer must set symbol-sort-key, or the sort key is never read", sortKey)
        assertEquals(listOf("get", FanOutIds.SORT_KEY_PROPERTY), sortKey!!.value.toString().let { raw -> raw.trim('[', ']').split(",").map { it.trim().trim('"') } })
    }
}
