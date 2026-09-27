package com.zynergylabs.forager.app.ui.map.layers

import com.zynergylabs.forager.app.ui.theme.MapPalette

/**
 * The map layer model, as plain data: map layers L0a (`prompts/preserved/2026-09-27-29.md`, with the
 * owner's rulings in `prompts/preserved/2026-09-27-30.md`).
 *
 * Nothing in this package names a MapLibre or Android type, so all of it runs headless
 * (`MapLayerRegistryTest`, `MapLayerStateTest`, `TapPrecedenceTest`). `SightingsMap` is still the
 * only place a [MapLayerSpec] becomes a native layer: it walks [orderedLayers] and adds one native
 * layer per spec, in that order, so **the draw order is this registry's**, not the order of any
 * hand-written sequence of `addLayer` calls.
 *
 * Named `layers` rather than something broader: it describes the layers on top of the basemap and
 * their state, and nothing about the basemap itself (that stays `Basemap`/`BasemapStyles`).
 */

/** What a layer draws, as the owner's tap ruling and the layer sheet (L0b) talk about it. */
enum class LayerKind { COLOUR_FIELD, AREA, LINE, MARKER }

/**
 * The z-bands above the basemap, **bottom to top in declaration order**: colour fields < areas <
 * lines < markers (the L0a dispatch, A1). The ordinal is the order; [orderedLayers] and
 * [registryProblems] both read it, so reordering these constants reorders the map.
 */
enum class ZGroup { COLOUR_FIELDS, AREAS, LINES, MARKERS }

/**
 * Which kind of native layer [SightingsMap][com.zynergylabs.forager.app.ui.map.SightingsMap] builds
 * for a spec. Separate from [LayerKind] because one kind has two renderers today (a marker is a
 * circle, the sighting dot, or a symbol, every bitmap marker), and each renderer has its own opacity
 * property ([OpacityProperty.renderer]).
 */
enum class LayerRenderer { FILL, LINE, CIRCLE, SYMBOL }

/**
 * The tap precedence the owner ruled (L0 design rulings, 3: "Markers, then lines, then colour"):
 * lower [precedence] wins. [NONE] is a layer a tap never lands on: a casing (decoration under its
 * own track, which is itself tappable), and the offline region's fill (owner's ruling 4 on
 * 2026-09-27-30: the outline is tappable, taps on the fill fall through).
 */
enum class TapGroup(val precedence: Int?) {
    MARKER(0),
    LINE(1),
    COLOUR_FIELD(2),
    NONE(null),
}

/**
 * The [MapPalette] role a layer is drawn in. Each role reads the one [MapPalette] field of the same
 * name, so the registry names a colour without holding one. `MapLayerRegistryTest` checks every
 * current layer's role against the colour `SightingsMap` actually builds it with.
 */
enum class PaletteRole(val colourOf: (MapPalette) -> Int) {
    OFFLINE_REGION(MapPalette::offlineRegion),
    CASING(MapPalette::casing),
    SEARCH_CENTRE(MapPalette::searchCentre),
    SIGHTING_DOT(MapPalette::sightingDot),
    BREADCRUMB(MapPalette::breadcrumb),
    KEPT_TRACK(MapPalette::keptTrack),
    PLANNED_TRIP(MapPalette::plannedTrip),
    WAYPOINT(MapPalette::waypoint),
    FIND(MapPalette::find),
    PHOTO(MapPalette::photo),
}

/** An opacity paint property, named as MapLibre names it, and the renderer it belongs to. */
enum class OpacityProperty(val styleName: String, val renderer: LayerRenderer) {
    FILL("fill-opacity", LayerRenderer.FILL),
    LINE("line-opacity", LayerRenderer.LINE),
    CIRCLE("circle-opacity", LayerRenderer.CIRCLE),
    CIRCLE_STROKE("circle-stroke-opacity", LayerRenderer.CIRCLE),
    ICON("icon-opacity", LayerRenderer.SYMBOL),
}

/** One of a layer's opacity properties at the value it draws with when its opacity setting is 1. */
data class BaseOpacity(val property: OpacityProperty, val base: Float)

/**
 * One map layer, described without constructing it.
 *
 * - [userToggleable], [userOpacity] and [userReorderable] say what the Layers sheet (L0b) may offer
 *   for this layer. Nothing in L0a reads them to decide what is drawn: [layerPaintFor] applies
 *   whatever state it is given. Their values here are provisional and listed in the L0a completion
 *   report for L0b to confirm.
 * - [baseOpacities] are today's values: `SightingsMap` builds each layer with exactly these (at
 *   state opacity 1), so the registry is the one place a layer's opacity is written down.
 * - [stateOwnerId] names the layer whose visibility and opacity this one follows. A casing follows
 *   its own track and the offline region's outline follows its fill, because to a user they are one
 *   overlay; `null` means the layer has its own state.
 * - [credit] is the attribution text this layer adds to the map's caption while it is visible
 *   ([activeLayerCredits]). No current layer has one; the forecast layers (L0b and later) will.
 */
data class MapLayerSpec(
    val id: String,
    val kind: LayerKind,
    val renderer: LayerRenderer,
    val sourceId: String,
    val zGroup: ZGroup,
    /** `null` for a layer whose colours come from its own data (a colour field, L0b). */
    val paletteRole: PaletteRole?,
    val userToggleable: Boolean,
    val userOpacity: Boolean,
    val userReorderable: Boolean,
    val tapGroup: TapGroup,
    val baseOpacities: List<BaseOpacity>,
    val stateOwnerId: String? = null,
    val credit: String? = null,
)

/**
 * Every overlay layer id. Fixed strings, referenced from the registry, from `SightingsMap` and from
 * whatever receives a tap ([TapHit.layerId]), so a typo is a compile error, not a missing layer.
 * The values are the ones `SightingsMap` has always used.
 */
object MapLayerIds {
    const val OFFLINE_REGION_FILL = "offline-region-circles-layer"
    const val OFFLINE_REGION_OUTLINE = "offline-region-circles-outline-layer"
    const val SEARCH_CENTRE = "search-center-layer"
    const val SIGHTINGS = "sightings-layer"
    const val BREADCRUMB_CASING = "breadcrumb-trail-casing-layer"
    const val BREADCRUMB = "breadcrumb-trail-layer"
    const val KEPT_TRACKS_CASING = "kept-tracks-casing-layer"
    const val KEPT_TRACKS = "kept-tracks-layer"
    const val PLANNED_TRIPS = "planned-trips-layer"
    const val WAYPOINTS = "waypoints-layer"
    const val FINDS = "find-markers-layer"
    const val PHOTOS = "photo-markers-layer"
}

/** Every overlay GeoJSON source id, the values `SightingsMap` has always used. */
object MapSourceIds {
    const val OFFLINE_REGIONS = "offline-region-circles"
    const val SEARCH_CENTRE = "search-center"
    const val SIGHTINGS = "sightings"
    const val BREADCRUMB = "breadcrumb-trail"
    const val KEPT_TRACKS = "kept-tracks"
    const val PLANNED_TRIPS = "planned-trips"
    const val WAYPOINTS = "waypoints"
    const val FINDS = "find-markers"
    const val PHOTOS = "photo-markers"
}

private val LINE_OPACITY = listOf(BaseOpacity(OpacityProperty.LINE, 1f))
private val ICON_OPACITY = listOf(BaseOpacity(OpacityProperty.ICON, 1f))

/** The offline region's fill opacity, today's value (Journal Stage 2d). */
private const val OFFLINE_REGION_FILL_OPACITY = 0.2f

/** The sighting dot's fill opacity, today's value: about the deleted osmdroid version's 0xB3 alpha. */
private const val SIGHTING_DOT_OPACITY = 0.7f

/**
 * The sighting dot's ring opacity, today's value: a light, near-opaque stroke (not translucent like
 * the fill) so each dot's boundary stays crisp in a dense cluster.
 */
private const val SIGHTING_DOT_STROKE_OPACITY = 0.85f

private fun marker(id: String, sourceId: String, role: PaletteRole, toggleable: Boolean) = MapLayerSpec(
    id = id,
    kind = LayerKind.MARKER,
    renderer = LayerRenderer.SYMBOL,
    sourceId = sourceId,
    zGroup = ZGroup.MARKERS,
    paletteRole = role,
    userToggleable = toggleable,
    userOpacity = false,
    userReorderable = false,
    tapGroup = TapGroup.MARKER,
    baseOpacities = ICON_OPACITY,
)

private fun line(id: String, sourceId: String, role: PaletteRole, tapGroup: TapGroup, owner: String?) = MapLayerSpec(
    id = id,
    kind = LayerKind.LINE,
    renderer = LayerRenderer.LINE,
    sourceId = sourceId,
    zGroup = ZGroup.LINES,
    paletteRole = role,
    // A layer that follows another's state is not offered on its own.
    userToggleable = owner == null,
    userOpacity = false,
    userReorderable = false,
    tapGroup = tapGroup,
    baseOpacities = LINE_OPACITY,
    stateOwnerId = owner,
)

/**
 * Today's overlay layers, bottom to top. The colour-field group is empty in L0a (L0b adds the
 * synthetic layer).
 *
 * Within each group the order is the one `SightingsMap` drew before L0a, and the reasons recorded
 * there still hold: the offline region's fill is lowest, so a coverage circle never covers a
 * marker; each casing is directly below its own track; planned trips, then waypoints, then finds
 * and photos, so a pin never sits under a sibling marker at the same point.
 *
 * **One visible change, accepted by the owner** (ruling 1 on `prompts/preserved/2026-09-27-30.md`,
 * "Accept: markers above lines"): the search centre and the sighting dots used to be added between
 * the offline outline and the breadcrumb casing, so every track line drew over them. As markers
 * they now draw above every line, the convention point markers follow elsewhere, and the order a
 * finger wins in ([tapWinner]) is now the order things are drawn in.
 *
 * `userToggleable` follows the owner's overlay list for the Layers sheet (layer ruling 3: finds,
 * waypoints, tracks, planned trips, offline maps, journal entries); photos are counted with finds as
 * part of an entry. The sighting dots and the search centre are not in that list, so they are not
 * toggleable here. `userOpacity` is offered for colour fields only (layer ruling 1: "each with its
 * own opacity slider"); the multiplier itself applies to any layer. Both are for L0b to confirm.
 */
val MAP_LAYER_REGISTRY: List<MapLayerSpec> = listOf(
    MapLayerSpec(
        id = MapLayerIds.OFFLINE_REGION_FILL,
        kind = LayerKind.AREA,
        renderer = LayerRenderer.FILL,
        sourceId = MapSourceIds.OFFLINE_REGIONS,
        zGroup = ZGroup.AREAS,
        paletteRole = PaletteRole.OFFLINE_REGION,
        userToggleable = true,
        userOpacity = false,
        userReorderable = false,
        // Owner's ruling 4: taps on the fill fall through; the outline is what a finger hits.
        tapGroup = TapGroup.NONE,
        baseOpacities = listOf(BaseOpacity(OpacityProperty.FILL, OFFLINE_REGION_FILL_OPACITY)),
    ),
    line(MapLayerIds.OFFLINE_REGION_OUTLINE, MapSourceIds.OFFLINE_REGIONS, PaletteRole.CASING, TapGroup.LINE, owner = MapLayerIds.OFFLINE_REGION_FILL),
    line(MapLayerIds.BREADCRUMB_CASING, MapSourceIds.BREADCRUMB, PaletteRole.CASING, TapGroup.NONE, owner = MapLayerIds.BREADCRUMB),
    line(MapLayerIds.BREADCRUMB, MapSourceIds.BREADCRUMB, PaletteRole.BREADCRUMB, TapGroup.LINE, owner = null),
    line(MapLayerIds.KEPT_TRACKS_CASING, MapSourceIds.KEPT_TRACKS, PaletteRole.CASING, TapGroup.NONE, owner = MapLayerIds.KEPT_TRACKS),
    line(MapLayerIds.KEPT_TRACKS, MapSourceIds.KEPT_TRACKS, PaletteRole.KEPT_TRACK, TapGroup.LINE, owner = null),
    marker(MapLayerIds.SEARCH_CENTRE, MapSourceIds.SEARCH_CENTRE, PaletteRole.SEARCH_CENTRE, toggleable = false),
    MapLayerSpec(
        id = MapLayerIds.SIGHTINGS,
        kind = LayerKind.MARKER,
        renderer = LayerRenderer.CIRCLE,
        sourceId = MapSourceIds.SIGHTINGS,
        zGroup = ZGroup.MARKERS,
        paletteRole = PaletteRole.SIGHTING_DOT,
        userToggleable = false,
        userOpacity = false,
        userReorderable = false,
        tapGroup = TapGroup.MARKER,
        baseOpacities = listOf(
            BaseOpacity(OpacityProperty.CIRCLE, SIGHTING_DOT_OPACITY),
            BaseOpacity(OpacityProperty.CIRCLE_STROKE, SIGHTING_DOT_STROKE_OPACITY),
        ),
    ),
    marker(MapLayerIds.PLANNED_TRIPS, MapSourceIds.PLANNED_TRIPS, PaletteRole.PLANNED_TRIP, toggleable = true),
    marker(MapLayerIds.WAYPOINTS, MapSourceIds.WAYPOINTS, PaletteRole.WAYPOINT, toggleable = true),
    marker(MapLayerIds.FINDS, MapSourceIds.FINDS, PaletteRole.FIND, toggleable = true),
    marker(MapLayerIds.PHOTOS, MapSourceIds.PHOTOS, PaletteRole.PHOTO, toggleable = true),
)

/**
 * What is wrong with [registry] as a registry, one line per problem; empty when nothing is. Checks
 * that ids are unique, that the groups run bottom to top in [ZGroup] order, that every
 * [MapLayerSpec.stateOwnerId] names another layer that owns its own state, that only colour
 * fields are reorderable, and that every base opacity belongs to its layer's renderer.
 */
fun registryProblems(registry: List<MapLayerSpec>): List<String> = buildList {
    registry.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys.forEach {
        add("layer id $it appears more than once")
    }
    registry.zipWithNext().forEach { (lower, upper) ->
        if (upper.zGroup.ordinal < lower.zGroup.ordinal) {
            add("${upper.id} (${upper.zGroup}) is listed after ${lower.id} (${lower.zGroup}), a group it belongs below")
        }
    }
    val byId = registry.associateBy { it.id }
    registry.forEach { spec ->
        spec.stateOwnerId?.let { ownerId ->
            val owner = byId[ownerId]
            when {
                owner == null -> add("${spec.id} follows $ownerId, which is not in the registry")
                owner.stateOwnerId != null -> add("${spec.id} follows $ownerId, which itself follows ${owner.stateOwnerId}")
            }
        }
        if (spec.userReorderable && spec.zGroup != ZGroup.COLOUR_FIELDS) {
            add("${spec.id} is reorderable but is in ${spec.zGroup}; only colour fields are")
        }
        spec.baseOpacities.filter { it.property.renderer != spec.renderer }.forEach {
            add("${spec.id} is a ${spec.renderer} layer but sets ${it.property.styleName}")
        }
    }
}

/**
 * [registry] in draw order, bottom to top: the groups in [ZGroup] order and, inside a group whose
 * layers are reorderable, the order [state] holds ([MapLayersState.reorderableOrder]); every other
 * layer keeps its registry position within its group.
 */
fun orderedLayers(registry: List<MapLayerSpec>, state: MapLayersState): List<MapLayerSpec> {
    val userRank = state.reorderableOrder.withIndex().associate { it.value to it.index }
    return ZGroup.entries.flatMap { group ->
        val members = registry.filter { it.zGroup == group }
        val (reorderable, fixed) = members.partition { it.userReorderable }
        if (reorderable.isEmpty()) {
            members
        } else {
            // sortedBy is stable, so layers the stored order does not name keep their registry order.
            reorderable.sortedBy { userRank[it.id] ?: Int.MAX_VALUE } + fixed
        }
    }
}
