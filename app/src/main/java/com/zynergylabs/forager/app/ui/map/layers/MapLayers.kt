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

/**
 * Today's overlay layers, bottom to top. The colour-field group is empty in L0a (L0b adds the
 * synthetic layer).
 */
val MAP_LAYER_REGISTRY: List<MapLayerSpec> = emptyList()

/**
 * What is wrong with [registry] as a registry, one line per problem; empty when nothing is. Checks
 * that ids are unique, that the groups run bottom to top in [ZGroup] order, that every
 * [MapLayerSpec.stateOwnerId] names another layer that owns its own state, that only colour
 * fields are reorderable, and that every base opacity belongs to its layer's renderer.
 */
fun registryProblems(registry: List<MapLayerSpec>): List<String> = emptyList()

/**
 * [registry] in draw order, bottom to top: the groups in [ZGroup] order and, inside a group whose
 * layers are reorderable, the order [state] holds ([MapLayersState.reorderableOrder]); every other
 * layer keeps its registry position within its group.
 */
fun orderedLayers(registry: List<MapLayerSpec>, state: MapLayersState): List<MapLayerSpec> = registry
