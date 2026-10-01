package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOffset
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What one push of the fan puts where (dispatch 2026-09-28-369, amendment -373): a single source and a single `setGeoJson`, each
 * feature saying what it is, so the legs, circles, dots and icons of a frame reach the renderer together and cannot be drawn from
 * different pushes. The pure plan only; that the layers then pick their features out by kind and that the renderer draws them in
 * step is device-only (a `MapView` cannot be built under Robolectric), so the probe's per-frame log and the recordings are the
 * evidence for that part.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FanPushPlanTest {

    private fun member(layerId: String, id: String) = FanMember(FanKey(layerId, id), 45.0, -122.0, 0f, 0f, FanOffset(0f, -48f))

    private val moved = LatLng(45.001, -122.002)

    private fun frame(progress: Float = 0.5f) = fanFrameCollections(
        listOf(member(MapLayerIds.FINDS, "find"), member(MapLayerIds.PHOTOS, "photo"), member(MapLayerIds.SIGHTINGS, "42")),
        { moved }, null, progress,
    )

    @Test
    fun `a frame is one push into one source`() {
        val plan = fanPushPlan(frame())

        assertEquals("one setGeoJson per frame", 1, plan.size)
        assertEquals(FanOutIds.FAN_SOURCE, plan.single().first)
    }

    @Test
    fun `every feature of the frame is in that push`() {
        val f = frame()
        val total = f.legs.features()!!.size + f.circles.features()!!.size + f.dots.features()!!.size + f.icons.features()!!.size

        assertEquals(total, fanPushPlan(f).single().second.features()!!.size)
        assertTrue("a frame with three members has features", total > 0)
    }

    @Test
    fun `each feature says what it is`() {
        val f = frame()
        val features = fanPushPlan(f).single().second.features()!!
        fun count(kind: String) = features.count { it.getStringProperty(FanOutIds.KIND_PROPERTY) == kind }

        assertEquals(f.legs.features()!!.size, count(FanOutIds.KIND_LEG))
        assertEquals(f.circles.features()!!.size, count(FanOutIds.KIND_CIRCLE))
        assertEquals(f.dots.features()!!.size, count(FanOutIds.KIND_DOT))
        assertEquals(f.icons.features()!!.size, count(FanOutIds.KIND_ICON))
        assertEquals("no feature without a kind", features.size, listOf(FanOutIds.KIND_LEG, FanOutIds.KIND_CIRCLE, FanOutIds.KIND_DOT, FanOutIds.KIND_ICON).sumOf { count(it) })
    }

    @Test
    fun `a leg stays a line and the rest stay points`() {
        val features = fanPushPlan(frame()).single().second.features()!!

        for (feature in features) {
            val kind = feature.getStringProperty(FanOutIds.KIND_PROPERTY)
            val geometry = feature.geometry()
            if (kind == FanOutIds.KIND_LEG) assertTrue("a leg is a line", geometry is LineString) else assertTrue("$kind is a point", geometry is Point)
        }
    }

    @Test
    fun `the properties the layers read are kept`() {
        val features = fanPushPlan(frame(progress = 0.5f)).single().second.features()!!
        val circle = features.first { it.getStringProperty(FanOutIds.KIND_PROPERTY) == FanOutIds.KIND_CIRCLE }
        val icon = features.first { it.getStringProperty(FanOutIds.KIND_PROPERTY) == FanOutIds.KIND_ICON }
        val dot = features.first { it.getStringProperty(FanOutIds.KIND_PROPERTY) == FanOutIds.KIND_DOT }

        assertEquals(0.5, circle.getNumberProperty(FanOutIds.CIRCLE_SCALE_PROPERTY).toDouble(), 1e-6)
        assertNotNull(icon.getStringProperty(FanOutIds.IMAGE_PROPERTY))
        assertNotNull(icon.getProperty(FanOutIds.ICON_OFFSET_PROPERTY))
        assertNotNull(icon.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY))
        assertEquals(42L, dot.getNumberProperty("observationId").toLong())
    }

    @Test
    fun `a frame with nothing to draw is still one push, so releasing a fan clears every layer at once`() {
        val plan = fanPushPlan(fanFrameCollections(emptyList(), { moved }, null, 0f))

        assertEquals(1, plan.size)
        assertEquals(FanOutIds.FAN_SOURCE, plan.single().first)
        assertEquals(0, plan.single().second.features()!!.size)
    }

    @Test
    fun `a layer picks its own kind out of the shared source`() {
        val filter = fanKindFilter(FanOutIds.KIND_CIRCLE).toString()

        assertTrue("the filter names the kind property: $filter", filter.contains(FanOutIds.KIND_PROPERTY))
        assertTrue("the filter names the kind it keeps: $filter", filter.contains(FanOutIds.KIND_CIRCLE))
    }
}
