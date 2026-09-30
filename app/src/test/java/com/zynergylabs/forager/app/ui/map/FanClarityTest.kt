package com.zynergylabs.forager.app.ui.map

import androidx.compose.ui.graphics.toArgb
import com.zynergylabs.forager.app.ui.map.layers.LayerPaint
import com.zynergylabs.forager.app.ui.map.layers.LayerState
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.OpacityProperty
import com.zynergylabs.forager.app.ui.map.layers.layerPaintFor
import com.zynergylabs.forager.app.ui.theme.SurfaceContainerDark
import com.zynergylabs.forager.app.ui.theme.SurfaceContainerLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * The values behind the fan's two look changes (dispatch 2026-09-28-265, items 2 and 3), as pure
 * functions the map reads. That `SightingsMap` then sets them on live MapLibre layers, and that they
 * look right by day and by night, is device-only: a `MapView` cannot be built under Robolectric.
 */
class FanClarityTest {

    private fun spec(id: String) = MAP_LAYER_REGISTRY.single { it.id == id }

    private fun fadedIds() = MAP_LAYER_REGISTRY.filter { fadedWhileFanned(it) }.map { it.id }.toSet()

    @Test
    fun `the layers that fade are exactly the marker icons - finds, photos, waypoints, planned trips and sighting dots`() {
        assertEquals(
            setOf(MapLayerIds.FINDS, MapLayerIds.PHOTOS, MapLayerIds.WAYPOINTS, MapLayerIds.PLANNED_TRIPS, MapLayerIds.SIGHTINGS),
            fadedIds(),
        )
    }

    @Test
    fun `no line, fill or colour field fades`() {
        val notMarkers = MAP_LAYER_REGISTRY.filter { it.id !in fadedIds() }.map { it.id }
        val tracksAndFills = listOf(
            MapLayerIds.KEPT_TRACKS, MapLayerIds.KEPT_TRACKS_CASING, MapLayerIds.BREADCRUMB, MapLayerIds.BREADCRUMB_CASING,
            MapLayerIds.OFFLINE_REGION_FILL, MapLayerIds.OFFLINE_REGION_OUTLINE, MapLayerIds.FORECAST_CHANTERELLES,
            MapLayerIds.FORECAST_CHICKEN_OF_THE_WOODS,
        )
        assertEquals(emptyList<String>(), tracksAndFills.filter { it !in notMarkers })
    }

    @Test
    fun `while a fan is open a fading layer draws at 80 percent of what it drew`() {
        val paint = layerPaintFor(spec(MapLayerIds.FINDS), MapLayersState.DEFAULT)
        val faded = fanFadedPaint(spec(MapLayerIds.FINDS), paint, fanOpen = true)
        assertEquals(listOf(0.8f), faded.opacities.map { it.value })
        assertEquals(OpacityProperty.ICON, faded.opacities.single().property)
        assertEquals("visibility is not the fade's to change", paint.visible, faded.visible)
    }

    @Test
    fun `the sighting dot's fill and ring are both scaled, keeping their own base`() {
        val paint = layerPaintFor(spec(MapLayerIds.SIGHTINGS), MapLayersState.DEFAULT)
        val faded = fanFadedPaint(spec(MapLayerIds.SIGHTINGS), paint, fanOpen = true)
        assertEquals(
            paint.opacities.map { it.property to it.value * 0.8f },
            faded.opacities.map { it.property to it.value },
        )
    }

    @Test
    fun `a folded fan gives back exactly the paint the layer state resolves to`() {
        val state = MapLayersState(layers = mapOf(MapLayerIds.FINDS to LayerState(visible = true, opacity = 0.5f)))
        for (id in fadedIds()) {
            val paint = layerPaintFor(spec(id), state)
            assertEquals("fan closed leaves $id as it was", paint, fanFadedPaint(spec(id), paint, fanOpen = false))
            assertSame(paint, fanFadedPaint(spec(id), paint, fanOpen = false))
        }
    }

    @Test
    fun `a layer opacity the user set is kept under the fade, multiplied not replaced`() {
        val state = MapLayersState(layers = mapOf(MapLayerIds.FINDS to LayerState(visible = true, opacity = 0.5f)))
        val paint = layerPaintFor(spec(MapLayerIds.FINDS), state)
        assertEquals(0.5f, paint.opacities.single().value, 0f)
        assertEquals(0.4f, fanFadedPaint(spec(MapLayerIds.FINDS), paint, fanOpen = true).opacities.single().value, 1e-6f)
    }

    @Test
    fun `a layer that does not fade is returned untouched even while a fan is open`() {
        for (id in listOf(MapLayerIds.KEPT_TRACKS, MapLayerIds.OFFLINE_REGION_FILL, MapLayerIds.BREADCRUMB, MapLayerIds.SEARCH_CENTRE)) {
            val paint: LayerPaint = layerPaintFor(spec(id), MapLayersState.DEFAULT)
            assertEquals(paint, fanFadedPaint(spec(id), paint, fanOpen = true))
        }
    }

    @Test
    fun `the circle is 48 dp across, so 24 dp in radius, at 80 percent, in the chrome colour it is given`() {
        val day = fanCircleStyle(SurfaceContainerLight.toArgb())
        val night = fanCircleStyle(SurfaceContainerDark.toArgb())
        assertEquals(24f, day.radiusDp, 0f)
        assertEquals(0.8f, day.opacity, 0f)
        assertEquals(SurfaceContainerLight.toArgb(), day.colour)
        assertEquals(SurfaceContainerDark.toArgb(), night.colour)
        assertEquals(48f, FAN_CIRCLE_DIAMETER_DP, 0f)
    }
}
