package com.zynergylabs.forager.app.ui.map.layers

import com.zynergylabs.forager.app.ui.theme.MapPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
 *
 * Map layers L0b, B7: the colour-field group, empty in L0a, now holds the two synthetic test layers,
 * below every other layer, with their own flags (toggle, opacity slider, reorder on) and no other
 * layer's flags changed. The assertions that pinned the empty group are updated to pin the new one.
 *
 * J8 (`prompts/preserved/2026-09-28-52.md`, J8-2): five journal-entry halos, each directly below the
 * record layer it decorates (below its casing, for a line with one), in the new `JOURNAL_ENTRY` role,
 * taking no taps, all following the one "Journal entries" switch. No other layer moved; the assertions
 * that pinned the registry's contents are re-pinned with the five added, and the halos' own rules are
 * new tests below.
 *
 * The night outline border (`prompts/preserved/2026-09-28-79.md`; owner: "The outline should have a
 * white border", "Yes the night outline only"): one line layer directly below the offline outline, in
 * the track casing's pattern, so the region's halo now sits below it as the kept tracks' halo sits
 * below their casing. It takes no taps, follows the fill's switch and draws at its own base opacity.
 * No other layer moved; the assertions that pin the registry's contents are re-pinned with it added,
 * and its own rules are a new test below.
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

    /** L0b's two synthetic colour fields, bottom to top (registry order). */
    private val colourFields = listOf(MapLayerIds.FORECAST_CHICKEN_OF_THE_WOODS, MapLayerIds.FORECAST_CHANTERELLES)

    /** J8's five halos, bottom to top (registry order). */
    private val journalHalos = listOf(
        MapLayerIds.JOURNAL_ENTRY_REGIONS,
        MapLayerIds.JOURNAL_ENTRY_TRACKS,
        MapLayerIds.JOURNAL_ENTRY_WAYPOINTS,
        MapLayerIds.JOURNAL_ENTRY_FINDS,
        MapLayerIds.JOURNAL_ENTRY_PHOTOS,
    )

    /** The night border under the offline outline (dispatch `2026-09-28-79`). */
    private val nightBorder = listOf(MapLayerIds.OFFLINE_REGION_BORDER)

    private val expectedOrder = colourFields + listOf(
        MapLayerIds.OFFLINE_REGION_FILL,
        MapLayerIds.JOURNAL_ENTRY_REGIONS,
        MapLayerIds.OFFLINE_REGION_BORDER,
        MapLayerIds.OFFLINE_REGION_OUTLINE,
        MapLayerIds.BREADCRUMB_CASING,
        MapLayerIds.BREADCRUMB,
        MapLayerIds.JOURNAL_ENTRY_TRACKS,
        MapLayerIds.KEPT_TRACKS_CASING,
        MapLayerIds.KEPT_TRACKS,
        MapLayerIds.SEARCH_CENTRE,
        MapLayerIds.SIGHTINGS,
        MapLayerIds.PLANNED_TRIPS,
        MapLayerIds.JOURNAL_ENTRY_WAYPOINTS,
        MapLayerIds.WAYPOINTS,
        MapLayerIds.JOURNAL_ENTRY_FINDS,
        MapLayerIds.FINDS,
        MapLayerIds.JOURNAL_ENTRY_PHOTOS,
        MapLayerIds.PHOTOS,
    )

    private fun spec(id: String) = MAP_LAYER_REGISTRY.single { it.id == id }

    @Test
    fun `every current layer is in the registry exactly once, and nothing else is`() {
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        assertEquals("ids are unique", ids.size, ids.toSet().size)
        assertEquals((orderBeforeL0a + colourFields + journalHalos + nightBorder).toSet(), ids.toSet())
        assertEquals(20, ids.size)
    }

    @Test
    fun `the registry lists today's layers bottom to top, with the search centre and sightings among the markers`() {
        assertEquals(expectedOrder, MAP_LAYER_REGISTRY.map { it.id })
    }

    @Test
    fun `the only change from the order before L0a is the search centre and the sightings moving above every line`() {
        val moved = setOf(MapLayerIds.SEARCH_CENTRE, MapLayerIds.SIGHTINGS)
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        assertEquals("every other layer keeps its relative order", orderBeforeL0a - moved, ids - moved - colourFields.toSet() - journalHalos.toSet() - nightBorder.toSet())
        val lastLine = ids.indexOfLast { spec(it).zGroup == ZGroup.LINES }
        moved.forEach { assertTrue("$it draws above every line", ids.indexOf(it) > lastLine) }
    }

    @Test
    fun `the groups run colour fields, areas, lines, markers, bottom to top, and the colour-field group holds the two synthetic layers`() {
        val groups = MAP_LAYER_REGISTRY.map { it.zGroup }
        assertEquals("groups never step down", groups.sortedBy { it.ordinal }, groups)
        assertEquals(listOf(ZGroup.COLOUR_FIELDS, ZGroup.AREAS, ZGroup.LINES, ZGroup.MARKERS), ZGroup.entries.toList())
        assertEquals(colourFields, MAP_LAYER_REGISTRY.filter { it.zGroup == ZGroup.COLOUR_FIELDS }.map { it.id })
        assertEquals("one registry colour field per colour-field spec", colourFields.toSet(), COLOUR_FIELDS.map { it.layerId }.toSet())
        assertEquals(listOf(MapLayerIds.OFFLINE_REGION_FILL), MAP_LAYER_REGISTRY.filter { it.zGroup == ZGroup.AREAS }.map { it.id })
    }

    @Test
    fun `the real registry has no problems`() {
        assertEquals(emptyList<String>(), registryProblems(MAP_LAYER_REGISTRY))
    }

    @Test
    fun `each layer's kind, renderer and source are the ones the map builds it with`() {
        val expected = mapOf(
            MapLayerIds.FORECAST_CHICKEN_OF_THE_WOODS to Triple(LayerKind.COLOUR_FIELD, LayerRenderer.FILL, MapSourceIds.FORECAST_CHICKEN_OF_THE_WOODS),
            MapLayerIds.FORECAST_CHANTERELLES to Triple(LayerKind.COLOUR_FIELD, LayerRenderer.FILL, MapSourceIds.FORECAST_CHANTERELLES),
            MapLayerIds.OFFLINE_REGION_FILL to Triple(LayerKind.AREA, LayerRenderer.FILL, MapSourceIds.OFFLINE_REGIONS),
            MapLayerIds.OFFLINE_REGION_OUTLINE to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.OFFLINE_REGIONS),
            MapLayerIds.OFFLINE_REGION_BORDER to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.OFFLINE_REGIONS),
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
            // J8: one source per halo, so a symbol layer is never fed a line.
            MapLayerIds.JOURNAL_ENTRY_REGIONS to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.JOURNAL_ENTRY_REGIONS),
            MapLayerIds.JOURNAL_ENTRY_TRACKS to Triple(LayerKind.LINE, LayerRenderer.LINE, MapSourceIds.JOURNAL_ENTRY_TRACKS),
            MapLayerIds.JOURNAL_ENTRY_WAYPOINTS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.JOURNAL_ENTRY_WAYPOINTS),
            MapLayerIds.JOURNAL_ENTRY_FINDS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.JOURNAL_ENTRY_FINDS),
            MapLayerIds.JOURNAL_ENTRY_PHOTOS to Triple(LayerKind.MARKER, LayerRenderer.SYMBOL, MapSourceIds.JOURNAL_ENTRY_PHOTOS),
        )
        assertEquals(expected, MAP_LAYER_REGISTRY.associate { it.id to Triple(it.kind, it.renderer, it.sourceId) })
    }

    @Test
    fun `record markers, lines and colour fields are tappable, casings, the offline fill, the search centre, the recording trail and the journal halos are not`() {
        val expected = mapOf(
            // M1 (planner's ruling): cells are tappable in their own group, which resolveTap ranks
            // after both of its stages, so a near miss on a line or marker still reaches the box.
            MapLayerIds.FORECAST_CHICKEN_OF_THE_WOODS to TapGroup.COLOUR_FIELD,
            MapLayerIds.FORECAST_CHANTERELLES to TapGroup.COLOUR_FIELD,
            MapLayerIds.OFFLINE_REGION_FILL to TapGroup.NONE,
            MapLayerIds.OFFLINE_REGION_OUTLINE to TapGroup.LINE,
            // -79: the night border is decoration under the outline, as a casing is under its track.
            MapLayerIds.OFFLINE_REGION_BORDER to TapGroup.NONE,
            MapLayerIds.BREADCRUMB_CASING to TapGroup.NONE,
            // M1 (owner's ruling 4, "Not tappable"): the recording trail and the search centre.
            MapLayerIds.BREADCRUMB to TapGroup.NONE,
            MapLayerIds.KEPT_TRACKS_CASING to TapGroup.NONE,
            MapLayerIds.KEPT_TRACKS to TapGroup.LINE,
            MapLayerIds.SEARCH_CENTRE to TapGroup.NONE,
            MapLayerIds.SIGHTINGS to TapGroup.MARKER,
            MapLayerIds.PLANNED_TRIPS to TapGroup.MARKER,
            MapLayerIds.WAYPOINTS to TapGroup.MARKER,
            MapLayerIds.FINDS to TapGroup.MARKER,
            MapLayerIds.PHOTOS to TapGroup.MARKER,
            // J8: a halo takes no taps, so M1's tap routing is unchanged.
            MapLayerIds.JOURNAL_ENTRY_REGIONS to TapGroup.NONE,
            MapLayerIds.JOURNAL_ENTRY_TRACKS to TapGroup.NONE,
            MapLayerIds.JOURNAL_ENTRY_WAYPOINTS to TapGroup.NONE,
            MapLayerIds.JOURNAL_ENTRY_FINDS to TapGroup.NONE,
            MapLayerIds.JOURNAL_ENTRY_PHOTOS to TapGroup.NONE,
        )
        assertEquals(expected, MAP_LAYER_REGISTRY.associate { it.id to it.tapGroup })
    }

    @Test
    fun `base opacities are today's values - the sighting fill 0_7 and ring 0_85, the offline fill 0_2, the night border 0_85, everything else 1`() {
        assertEquals(
            listOf(BaseOpacity(OpacityProperty.CIRCLE, 0.7f), BaseOpacity(OpacityProperty.CIRCLE_STROKE, 0.85f)),
            spec(MapLayerIds.SIGHTINGS).baseOpacities,
        )
        assertEquals(listOf(BaseOpacity(OpacityProperty.FILL, 0.2f)), spec(MapLayerIds.OFFLINE_REGION_FILL).baseOpacities)
        colourFields.forEach { assertEquals(it, listOf(BaseOpacity(OpacityProperty.FILL, 0.6f)), spec(it).baseOpacities) }
        assertEquals(listOf(BaseOpacity(OpacityProperty.LINE, 0.85f)), spec(MapLayerIds.OFFLINE_REGION_BORDER).baseOpacities)
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.LINE && it.id != MapLayerIds.OFFLINE_REGION_BORDER }.forEach {
            assertEquals(it.id, listOf(BaseOpacity(OpacityProperty.LINE, 1f)), it.baseOpacities)
        }
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.SYMBOL }.forEach {
            assertEquals(it.id, listOf(BaseOpacity(OpacityProperty.ICON, 1f)), it.baseOpacities)
        }
    }

    @Test
    fun `a casing follows its own track's state, the offline outline and its border follow the fill, and the halos follow the Journal entries switch`() {
        val owners = MAP_LAYER_REGISTRY.filter { it.stateOwnerId != null }.associate { it.id to it.stateOwnerId }
        assertEquals(
            mapOf(
                MapLayerIds.BREADCRUMB_CASING to MapLayerIds.BREADCRUMB,
                MapLayerIds.KEPT_TRACKS_CASING to MapLayerIds.KEPT_TRACKS,
                MapLayerIds.OFFLINE_REGION_OUTLINE to MapLayerIds.OFFLINE_REGION_FILL,
                MapLayerIds.OFFLINE_REGION_BORDER to MapLayerIds.OFFLINE_REGION_FILL,
                MapLayerIds.JOURNAL_ENTRY_REGIONS to JOURNAL_ENTRIES_SWITCH_LAYER_ID,
                MapLayerIds.JOURNAL_ENTRY_WAYPOINTS to JOURNAL_ENTRIES_SWITCH_LAYER_ID,
                MapLayerIds.JOURNAL_ENTRY_FINDS to JOURNAL_ENTRIES_SWITCH_LAYER_ID,
                MapLayerIds.JOURNAL_ENTRY_PHOTOS to JOURNAL_ENTRIES_SWITCH_LAYER_ID,
            ),
            owners,
        )
    }

    @Test
    fun `only the colour fields are reorderable, offer an opacity slider and carry a credit, and no other layer's flags changed`() {
        assertEquals(colourFields, MAP_LAYER_REGISTRY.filter { it.userReorderable }.map { it.id })
        assertEquals(colourFields, MAP_LAYER_REGISTRY.filter { it.userOpacity }.map { it.id })
        assertEquals(colourFields.associateWith { SYNTHETIC_DATA_CREDIT }, MAP_LAYER_REGISTRY.filter { it.credit != null }.associate { it.id to it.credit })
        assertEquals(
            "toggleable: L0a's seven, plus the two colour fields, plus J8's Journal entries switch",
            colourFields + listOf(
                MapLayerIds.OFFLINE_REGION_FILL,
                MapLayerIds.BREADCRUMB,
                JOURNAL_ENTRIES_SWITCH_LAYER_ID,
                MapLayerIds.KEPT_TRACKS,
                MapLayerIds.PLANNED_TRIPS,
                MapLayerIds.WAYPOINTS,
                MapLayerIds.FINDS,
                MapLayerIds.PHOTOS,
            ),
            MAP_LAYER_REGISTRY.filter { it.userToggleable }.map { it.id },
        )
    }

    @Test
    fun `each journal halo sits directly below what it decorates, in the JOURNAL_ENTRY role, and is drawn only with that record`() {
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        // halo to (the record layer it decorates, the layer it sits directly below: the record's casing, for a line with one)
        val expected = mapOf(
            // -79: the offline outline's casing is its night border, so the region's halo sits below that.
            MapLayerIds.JOURNAL_ENTRY_REGIONS to (MapLayerIds.OFFLINE_REGION_OUTLINE to MapLayerIds.OFFLINE_REGION_BORDER),
            MapLayerIds.JOURNAL_ENTRY_TRACKS to (MapLayerIds.KEPT_TRACKS to MapLayerIds.KEPT_TRACKS_CASING),
            MapLayerIds.JOURNAL_ENTRY_WAYPOINTS to (MapLayerIds.WAYPOINTS to MapLayerIds.WAYPOINTS),
            MapLayerIds.JOURNAL_ENTRY_FINDS to (MapLayerIds.FINDS to MapLayerIds.FINDS),
            MapLayerIds.JOURNAL_ENTRY_PHOTOS to (MapLayerIds.PHOTOS to MapLayerIds.PHOTOS),
        )
        assertEquals(
            "only the halos decorate another layer",
            expected.mapValues { it.value.first },
            MAP_LAYER_REGISTRY.filter { it.drawnWith != null }.associate { it.id to it.drawnWith },
        )
        expected.forEach { (halo, pair) ->
            assertEquals("$halo is directly below ${pair.second}", ids.indexOf(pair.second) - 1, ids.indexOf(halo))
            assertEquals(halo, PaletteRole.JOURNAL_ENTRY, spec(halo).paletteRole)
        }
        assertEquals(journalHalos, MAP_LAYER_REGISTRY.filter { it.paletteRole == PaletteRole.JOURNAL_ENTRY }.map { it.id })
    }

    /**
     * Dispatch `2026-09-28-79`: the night border is part of the outline's layer group, directly below
     * the outline and above the region's halo (the kept tracks' order: halo, casing, line). It takes no
     * taps, so what a tap reaches is unchanged; it is not offered on its own and follows the fill's
     * switch, hiding with it; and it draws at [OpacityProperty.LINE] 0.85, in a role that is opaque
     * white at night and fully transparent by day.
     */
    @Test
    fun `the offline outline's night border sits directly below the outline and above the region's halo, takes no taps and follows the fill`() {
        val border = MAP_LAYER_REGISTRY.singleOrNull { it.id == MapLayerIds.OFFLINE_REGION_BORDER }
        assertNotNull("no border layer under the offline outline in the registry", border)
        val ids = MAP_LAYER_REGISTRY.map { it.id }
        assertEquals("directly below the outline", ids.indexOf(MapLayerIds.OFFLINE_REGION_OUTLINE) - 1, ids.indexOf(border!!.id))
        assertEquals("directly above the region's halo", ids.indexOf(MapLayerIds.JOURNAL_ENTRY_REGIONS) + 1, ids.indexOf(border.id))
        assertEquals(ZGroup.LINES, border.zGroup)
        assertEquals(TapGroup.NONE, border.tapGroup)
        assertFalse("not a tap target", border.id in tappableLayerIds(orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT)))
        assertEquals(MapLayerIds.OFFLINE_REGION_FILL, border.stateOwnerId)
        assertFalse(border.userToggleable)
        assertFalse(border.userOpacity)
        assertFalse(border.userReorderable)
        assertNull("a casing, not a decoration", border.drawnWith)
        assertEquals(listOf(BaseOpacity(OpacityProperty.LINE, 0.85f)), border.baseOpacities)
        val fillHidden = MapLayersState(layers = mapOf(MapLayerIds.OFFLINE_REGION_FILL to LayerState(visible = false)))
        assertFalse("hides with the Offline maps switch", layerPaintFor(border, fillHidden).visible)
        assertEquals("opaque white at night", "#FFFFFFFF", "#%08X".format(border.paletteRole!!.colourOf(MapPalette.NIGHT)))
        assertEquals("fully transparent by day", 0, border.paletteRole!!.colourOf(MapPalette.DAY) ushr 24)
    }

    @Test
    fun `registryProblems names a decoration of a missing layer, one above what it decorates, and one that takes taps`() {
        fun halo(id: String, decorates: String, tap: TapGroup = TapGroup.NONE) = layer(id, ZGroup.LINES).copy(tapGroup = tap, drawnWith = decorates)
        val missing = registryProblems(listOf(halo("h", decorates = "nowhere")))
        assertTrue(missing.toString(), missing.any { "h" in it && "nowhere" in it })
        val above = registryProblems(listOf(layer("rec", ZGroup.LINES), halo("h", decorates = "rec")))
        assertTrue(above.toString(), above.any { "h" in it && "rec" in it && "below" in it })
        val tappable = registryProblems(listOf(halo("h", decorates = "rec", tap = TapGroup.LINE), layer("rec", ZGroup.LINES)))
        assertTrue(tappable.toString(), tappable.any { "h" in it && "taps" in it })
        // The same shapes, correct, have no problems: each rule above failed on its own fault.
        assertEquals(emptyList<String>(), registryProblems(listOf(halo("h", decorates = "rec"), layer("rec", ZGroup.LINES))))
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
