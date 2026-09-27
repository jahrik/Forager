package com.zynergylabs.forager.app.ui.map.layers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The layer registry (map layers L0a, A1): which layers exist, in which z-group, in which order,
 * and what each one's description says. Headless: the registry is plain data.
 *
 * The expected order below is today's order (`SightingsMap.kt`'s hand-ordered `addLayer` sequence
 * before L0a, read at `488d361`) with the one change the owner accepted (ruling 1 on
 * `prompts/preserved/2026-09-27-30.md`): the search centre and the sighting dots move from below the
 * track lines to the markers group, above them.
 */
class MapLayerRegistryTest {

    /** Bottom to top, as `initializeOverlayLayers` added them before L0a. */
    private val orderBeforeL0a = listOf(
        MapLayerIds.OFFLINE_REGION_FILL,
        MapLayerIds.OFFLINE_REGION_OUTLINE,
        MapLayerIds.SEARCH_CENTRE,
        MapLayerIds.SIGHTINGS,
        MapLayerIds.BREADCRUMB_CASING,
        MapLayerIds.BREADCRUMB,
        MapLayerIds.KEPT_TRACKS_CASING,
        MapLayerIds.KEPT_TRACKS,
        MapLayerIds.PLANNED_TRIPS,
        MapLayerIds.WAYPOINTS,
        MapLayerIds.FINDS,
        MapLayerIds.PHOTOS,
    )

    private val expectedOrder = listOf(
        MapLayerIds.OFFLINE_REGION_FILL,
        MapLayerIds.OFFLINE_REGION_OUTLINE,
        MapLayerIds.BREADCRUMB_CASING,
        MapLayerIds.BREADCRUMB,
        MapLayerIds.KEPT_TRACKS_CASING,
        MapLayerIds.KEPT_TRACKS,
        MapLayerIds.SEARCH_CENTRE,
        MapLayerIds.SIGHTINGS,
        MapLayerIds.PLANNED_TRIPS,
        MapLayerIds.WAYPOINTS,
        MapLayerIds.FINDS,
        MapLayerIds.PHOTOS,
    )

    private fun spec(id: String) = MAP_LAYER_REGISTRY.single { it.id == id }

    @Test
    fun `every current layer is in the registry exactly once, and nothing else is`() {
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        assertEquals("ids are unique", ids.size, ids.toSet().size)
        assertEquals(orderBeforeL0a.toSet(), ids.toSet())
        assertEquals(12, ids.size)
    }

    @Test
    fun `the registry lists today's layers bottom to top, with the search centre and sightings among the markers`() {
        assertEquals(expectedOrder, MAP_LAYER_REGISTRY.map { it.id })
    }

    @Test
    fun `the only change from the order before L0a is the search centre and the sightings moving above every line`() {
        val moved = setOf(MapLayerIds.SEARCH_CENTRE, MapLayerIds.SIGHTINGS)
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        assertEquals("every other layer keeps its relative order", orderBeforeL0a - moved, ids - moved)
        val lastLine = ids.indexOfLast { spec(it).zGroup == ZGroup.LINES }
        moved.forEach { assertTrue("$it draws above every line", ids.indexOf(it) > lastLine) }
    }

    @Test
    fun `the groups run colour fields, areas, lines, markers, bottom to top, and the colour-field group is empty`() {
        val groups = MAP_LAYER_REGISTRY.map { it.zGroup }
        assertEquals("groups never step down", groups.sortedBy { it.ordinal }, groups)
        assertEquals(listOf(ZGroup.COLOUR_FIELDS, ZGroup.AREAS, ZGroup.LINES, ZGroup.MARKERS), ZGroup.entries.toList())
        assertTrue(MAP_LAYER_REGISTRY.none { it.zGroup == ZGroup.COLOUR_FIELDS })
        assertEquals(listOf(MapLayerIds.OFFLINE_REGION_FILL), MAP_LAYER_REGISTRY.filter { it.zGroup == ZGroup.AREAS }.map { it.id })
    }

    @Test
    fun `the real registry has no problems`() {
        assertEquals(emptyList<String>(), registryProblems(MAP_LAYER_REGISTRY))
    }

    @Test
    fun `each layer's kind, renderer and source are the ones the map builds it with`() {
        val expected = mapOf(
            MapLayerIds.OFFLINE_REGION_FILL to Triple(LayerKind.AREA, LayerRenderer.FILL, MapSourceIds.OFFLINE_REGIONS),
            MapLayerIds.OFFLINE_REGION_OUTLINE to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.OFFLINE_REGIONS),
            MapLayerIds.BREADCRUMB_CASING to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.BREADCRUMB),
            MapLayerIds.BREADCRUMB to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.BREADCRUMB),
            MapLayerIds.KEPT_TRACKS_CASING to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.KEPT_TRACKS),
            MapLayerIds.KEPT_TRACKS to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.KEPT_TRACKS),
            MapLayerIds.SEARCH_CENTRE to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.SEARCH_CENTRE),
            MapLayerIds.SIGHTINGS to Triple(LayerKind.MARKER, LayerRenderer.CIRCLE, MapSourceIds.SIGHTINGS),
            MapLayerIds.PLANNED_TRIPS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.PLANNED_TRIPS),
            MapLayerIds.WAYPOINTS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.WAYPOINTS),
            MapLayerIds.FINDS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.FINDS),
            MapLayerIds.PHOTOS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.PHOTOS),
        )
        assertEquals(expected, MAP_LAYER_REGISTRY.associate { it.id to Triple(it.kind, it.renderer, it.sourceId) })
    }

    @Test
    fun `markers and lines are tappable, casings and the offline fill are not`() {
        val expected = mapOf(
            MapLayerIds.OFFLINE_REGION_FILL to TapGroup.NONE,
            MapLayerIds.OFFLINE_REGION_OUTLINE to TapGroup.LINE,
            MapLayerIds.BREADCRUMB_CASING to TapGroup.NONE,
            MapLayerIds.BREADCRUMB to TapGroup.LINE,
            MapLayerIds.KEPT_TRACKS_CASING to TapGroup.NONE,
            MapLayerIds.KEPT_TRACKS to TapGroup.LINE,
            MapLayerIds.SEARCH_CENTRE to TapGroup.MARKER,
            MapLayerIds.SIGHTINGS to TapGroup.MARKER,
            MapLayerIds.PLANNED_TRIPS to TapGroup.MARKER,
            MapLayerIds.WAYPOINTS to TapGroup.MARKER,
            MapLayerIds.FINDS to TapGroup.MARKER,
            MapLayerIds.PHOTOS to TapGroup.MARKER,
        )
        assertEquals(expected, MAP_LAYER_REGISTRY.associate { it.id to it.tapGroup })
    }

    @Test
    fun `base opacities are today's values - the sighting fill 0_7 and ring 0_85, the offline fill 0_2, everything else 1`() {
        assertEquals(
            listOf(BaseOpacity(OpacityProperty.CIRCLE, 0.7f), BaseOpacity(OpacityProperty.CIRCLE_STROKE, 0.85f)),
            spec(MapLayerIds.SIGHTINGS).baseOpacities,
        )
        assertEquals(listOf(BaseOpacity(OpacityProperty.FILL, 0.2f)), spec(MapLayerIds.OFFLINE_REGION_FILL).baseOpacities)
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.LINE }.forEach {
            assertEquals(it.id, listOf(BaseOpacity(OpacityProperty.LINE, 1f)), it.baseOpacities)
        }
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.SYMBOL }.forEach {
            assertEquals(it.id, listOf(BaseOpacity(OpacityProperty.ICON, 1f)), it.baseOpacities)
        }
    }

    @Test
    fun `a casing follows its own track's state, and the offline outline follows the fill`() {
        val owners = MAP_LAYER_REGISTRY.filter { it.stateOwnerId != null }.associate { it.id to it.stateOwnerId }
        assertEquals(
            mapOf(
                MapLayerIds.BREADCRUMB_CASING to MapLayerIds.BREADCRUMB,
                MapLayerIds.KEPT_TRACKS_CASING to MapLayerIds.KEPT_TRACKS,
                MapLayerIds.OFFLINE_REGION_OUTLINE to MapLayerIds.OFFLINE_REGION_FILL,
            ),
            owners,
        )
    }

    @Test
    fun `no current layer is reorderable or carries a credit`() {
        assertTrue(MAP_LAYER_REGISTRY.none { it.userReorderable })
        assertTrue(MAP_LAYER_REGISTRY.none { it.credit != null })
    }

    // registryProblems: each rule shown able to fail.

    private fun layer(
        id: String,
        group: ZGroup,
        renderer: LayerRenderer = LayerRenderer.LINE,
        opacities: List<BaseOpacity> = listOf(BaseOpacity(OpacityProperty.LINE, 1f)),
        owner: String? = null,
        reorderable: Boolean = false,
    ) = MapLayerSpec(
        id = id,
        kind = LayerKind.LINE,
        renderer = renderer,
        sourceId = "src-$id",
        zGroup = group,
        paletteRole = null,
        userToggleable = true,
        userOpacity = true,
        userReorderable = reorderable,
        tapGroup = TapGroup.LINE,
        baseOpacities = opacities,
        stateOwnerId = owner,
    )

    @Test
    fun `registryProblems names a repeated id`() {
        val problems = registryProblems(listOf(layer("a", ZGroup.LINES), layer("a", ZGroup.LINES)))
        assertTrue(problems.toString(), problems.any { "a" in it && "more than once" in it })
    }

    @Test
    fun `registryProblems names a group that steps down`() {
        val problems = registryProblems(listOf(layer("m", ZGroup.MARKERS), layer("l", ZGroup.LINES)))
        assertTrue(problems.toString(), problems.any { "l" in it && "below" in it })
    }

    @Test
    fun `registryProblems names an owner that is missing, and an owner that itself follows another`() {
        val missing = registryProblems(listOf(layer("a", ZGroup.LINES, owner = "nowhere")))
        assertTrue(missing.toString(), missing.any { "nowhere" in it })
        val chained = registryProblems(
            listOf(layer("a", ZGroup.LINES), layer("b", ZGroup.LINES, owner = "a"), layer("c", ZGroup.LINES, owner = "b")),
        )
        assertTrue(chained.toString(), chained.any { "c" in it && "b" in it })
    }

    @Test
    fun `registryProblems names a reorderable layer outside the colour fields`() {
        val problems = registryProblems(listOf(layer("a", ZGroup.LINES, reorderable = true)))
        assertTrue(problems.toString(), problems.any { "a" in it && "reorderable" in it })
    }

    @Test
    fun `registryProblems names an opacity property of another renderer`() {
        val problems = registryProblems(
            listOf(layer("a", ZGroup.MARKERS, renderer = LayerRenderer.SYMBOL, opacities = listOf(BaseOpacity(OpacityProperty.CIRCLE, 1f)))),
        )
        assertTrue(problems.toString(), problems.any { "a" in it && "circle-opacity" in it })
    }

    // orderedLayers

    @Test
    fun `with the default state the draw order is the registry's`() {
        assertEquals(MAP_LAYER_REGISTRY, orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT))
    }

    @Test
    fun `orderedLayers puts the groups in order even when the registry does not`() {
        val registry = listOf(layer("m", ZGroup.MARKERS), layer("l", ZGroup.LINES), layer("a", ZGroup.AREAS))
        assertEquals(listOf("a", "l", "m"), orderedLayers(registry, MapLayersState.DEFAULT).map { it.id })
    }

    @Test
    fun `colour fields draw in the user's order, bottom to top, unnamed ones after in registry order`() {
        val registry = listOf(
            layer("c1", ZGroup.COLOUR_FIELDS, reorderable = true),
            layer("c2", ZGroup.COLOUR_FIELDS, reorderable = true),
            layer("c3", ZGroup.COLOUR_FIELDS, reorderable = true),
            layer("l", ZGroup.LINES),
        )
        val state = MapLayersState(reorderableOrder = listOf("c3", "gone", "c1"))
        assertEquals(listOf("c3", "c1", "c2", "l"), orderedLayers(registry, state).map { it.id })
    }

    @Test
    fun `a stored order cannot move a layer that is not reorderable`() {
        val registry = listOf(layer("l1", ZGroup.LINES), layer("l2", ZGroup.LINES))
        val state = MapLayersState(reorderableOrder = listOf("l2", "l1"))
        assertEquals(listOf("l1", "l2"), orderedLayers(registry, state).map { it.id })
    }
}
