package com.zynergylabs.forager.app.ui.map.layers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tap precedence (map layers L0a, A4; L0 design ruling 3, "Markers, then lines, then colour"; the
 * owner's ruling 4 on `prompts/preserved/2026-09-27-30.md`: the offline outline is tappable and its
 * fill is not, and a finger-sized box is queried so thin lines are hittable). Headless: the native
 * hit test only produces [TapHit]s, and these functions decide among them.
 */
class TapPrecedenceTest {

    private val order = orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT)

    private fun hit(layerId: String, featureId: String? = "id-$layerId") = TapHit(layerId, featureId)

    @Test
    fun `every marker and line is queried, never a casing or the offline fill`() {
        assertEquals(
            listOf(
                MapLayerIds.OFFLINE_REGION_OUTLINE,
                MapLayerIds.BREADCRUMB,
                MapLayerIds.KEPT_TRACKS,
                MapLayerIds.SEARCH_CENTRE,
                MapLayerIds.SIGHTINGS,
                MapLayerIds.PLANNED_TRIPS,
                MapLayerIds.WAYPOINTS,
                MapLayerIds.FINDS,
                MapLayerIds.PHOTOS,
            ),
            tappableLayerIds(order),
        )
    }

    @Test
    fun `a marker wins over a line whichever order the hits arrive in`() {
        val marker = hit(MapLayerIds.SIGHTINGS)
        val line = hit(MapLayerIds.KEPT_TRACKS)
        assertEquals(marker, tapWinner(listOf(line, marker), order))
        assertEquals(marker, tapWinner(listOf(marker, line), order))
    }

    @Test
    fun `the offline outline is a line - it loses to a marker and wins over nothing tappable`() {
        val outline = hit(MapLayerIds.OFFLINE_REGION_OUTLINE)
        assertEquals(hit(MapLayerIds.FINDS), tapWinner(listOf(outline, hit(MapLayerIds.FINDS)), order))
        assertEquals(outline, tapWinner(listOf(hit(MapLayerIds.OFFLINE_REGION_FILL), outline), order))
    }

    @Test
    fun `within the markers the layer drawn on top wins`() {
        assertEquals(hit(MapLayerIds.WAYPOINTS), tapWinner(listOf(hit(MapLayerIds.WAYPOINTS), hit(MapLayerIds.SIGHTINGS)), order))
        assertEquals(hit(MapLayerIds.PHOTOS), tapWinner(listOf(hit(MapLayerIds.FINDS), hit(MapLayerIds.PHOTOS), hit(MapLayerIds.PLANNED_TRIPS)), order))
    }

    @Test
    fun `within the lines the layer drawn on top wins`() {
        assertEquals(hit(MapLayerIds.KEPT_TRACKS), tapWinner(listOf(hit(MapLayerIds.KEPT_TRACKS), hit(MapLayerIds.BREADCRUMB)), order))
        assertEquals(hit(MapLayerIds.BREADCRUMB), tapWinner(listOf(hit(MapLayerIds.OFFLINE_REGION_OUTLINE), hit(MapLayerIds.BREADCRUMB)), order))
    }

    @Test
    fun `within one layer the first hit the query returned wins`() {
        val first = hit(MapLayerIds.WAYPOINTS, "wp-1")
        val second = hit(MapLayerIds.WAYPOINTS, "wp-2")
        assertEquals(first, tapWinner(listOf(first, second), order))
    }

    @Test
    fun `hits only on untappable or unknown layers win nothing, and no hits win nothing`() {
        assertNull(tapWinner(listOf(hit(MapLayerIds.BREADCRUMB_CASING), hit(MapLayerIds.OFFLINE_REGION_FILL), hit("not-a-layer")), order))
        assertNull(tapWinner(emptyList(), order))
    }

    @Test
    fun `a colour field wins only when no marker or line is hit`() {
        val field = MapLayerSpec(
            id = "field",
            kind = LayerKind.COLOUR_FIELD,
            renderer = LayerRenderer.FILL,
            sourceId = "field-src",
            zGroup = ZGroup.COLOUR_FIELDS,
            paletteRole = null,
            userToggleable = true,
            userOpacity = true,
            userReorderable = true,
            tapGroup = TapGroup.COLOUR_FIELD,
            baseOpacities = listOf(BaseOpacity(OpacityProperty.FILL, 0.6f)),
        )
        val withField = orderedLayers(listOf(field) + MAP_LAYER_REGISTRY, MapLayersState.DEFAULT)
        assertEquals(hit("field"), tapWinner(listOf(hit("field"), hit(MapLayerIds.OFFLINE_REGION_FILL)), withField))
        assertEquals(hit(MapLayerIds.BREADCRUMB), tapWinner(listOf(hit("field"), hit(MapLayerIds.BREADCRUMB)), withField))
    }

    // resolveTap: the point decides, the box only when the point has nothing tappable.

    @Test
    fun `what lies under the point decides, and the box is not queried`() {
        var boxQueries = 0
        val winner = resolveTap(listOf(hit(MapLayerIds.SIGHTINGS)), { boxQueries++; listOf(hit(MapLayerIds.PHOTOS)) }, order)
        assertEquals(hit(MapLayerIds.SIGHTINGS), winner)
        assertEquals(0, boxQueries)
    }

    @Test
    fun `with nothing tappable under the point the box decides`() {
        assertEquals(hit(MapLayerIds.BREADCRUMB), resolveTap(emptyList(), { listOf(hit(MapLayerIds.BREADCRUMB)) }, order))
        assertEquals(
            hit(MapLayerIds.BREADCRUMB),
            resolveTap(listOf(hit(MapLayerIds.OFFLINE_REGION_FILL)), { listOf(hit(MapLayerIds.BREADCRUMB)) }, order),
        )
    }

    @Test
    fun `nothing under the point or in the box wins nothing`() {
        assertNull(resolveTap(emptyList(), { emptyList() }, order))
    }

    @Test
    fun `the tap box is 48dp square, the minimum touch target`() {
        assertEquals(48f, TAP_BOX_DP)
    }
}
