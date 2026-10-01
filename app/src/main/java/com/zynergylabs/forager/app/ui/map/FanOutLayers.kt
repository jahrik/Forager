package com.zynergylabs.forager.app.ui.map

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import com.google.gson.JsonArray
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.MapProbe
import com.zynergylabs.forager.app.ui.map.fanout.ProbedMarker
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.LayerKind
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayerSpec
import com.zynergylabs.forager.app.ui.map.layers.TapGroup
import com.zynergylabs.forager.app.ui.map.layers.TapHit
import com.zynergylabs.forager.app.ui.theme.MapPalette
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

/*
 * The map-side half of the marker fan-out (dispatch 2026-09-28-197). The fanned markers are copies:
 * the originals of a fanned stack are filtered out of their own layers while the fan is up, and the
 * copies are drawn from one source of this file's own (one layer per kind), above every registry layer, moving from
 * their true spot to their ring place as the fan's progress goes 0 to 1. The arithmetic is in
 * `fanout/MarkerFanOut.kt`; the SDK-facing calls are here, and are device-only: a `MapView` cannot be
 * built under Robolectric, so what the tests reach is [fanFrameCollections] (the features pushed) and
 * [fanOutHiddenFilter] (the expression built), not that the map then draws them.
 */

internal object FanOutIds {
    const val LEGS_CASING_LAYER = "fan-out-legs-casing-layer"
    const val LEGS_LAYER = "fan-out-legs-layer"
    /** The background circle under each copy (dispatch 2026-09-28-265); it replaced the white halo. */
    const val CIRCLES_LAYER = "fan-out-circles-layer"
    const val DOTS_LAYER = "fan-out-dots-layer"
    const val ICONS_LAYER = "fan-out-icons-layer"

    /** The one source every fan feature goes into, so one push is one `setGeoJson` (dispatch 2026-09-28-369, amendment -373). */
    const val FAN_SOURCE = "fan-out-source"

    /** The feature property naming what a feature is, so each layer can pick its own out of [FAN_SOURCE]. */
    const val KIND_PROPERTY = "kind"
    const val KIND_LEG = "leg"
    const val KIND_CIRCLE = "circle"
    const val KIND_DOT = "dot"
    const val KIND_ICON = "icon"

    /** The feature property naming the bitmap a copy draws. */
    const val IMAGE_PROPERTY = "image"

    /** The feature property holding a copy's `icon-offset`, `[x, y]` in dp (dispatch 2026-09-28-275). */
    const val ICON_OFFSET_PROPERTY = "iconOffset"

    /** The feature property holding a circle's scale, 0 to 1 (dispatch 2026-09-28-299). */
    const val CIRCLE_SCALE_PROPERTY = "circleScale"

    /** The feature property holding a copy's `symbol-sort-key`: its registry layer's place in the draw order (dispatch 2026-09-28-318). */
    const val SORT_KEY_PROPERTY = "sortKey"
}

/** The leg's own line width, in dp: "a thin line". Its casing is [CASING_WIDTH_DP] wider on each side, as a track's is. */
internal const val FAN_LEG_WIDTH_DP = 1.5f

/**
 * Adds the fan-out's sources and layers, empty, above everything already in [style]: legs (a casing
 * and its line), then the background circles, sighting dots, and marker icons. Called once per style
 * load, after the registry's layers, since a layer added later draws on top. The live-location puck
 * is placed below the fan's lowest layer, [FanOutIds.LEGS_CASING_LAYER] (`liveLocationComponentOptions`
 * in SightingsMap.kt), so it draws above the registry's layers and below the whole fan; it is
 * activated after this runs, which is what lets it name a layer here. [chromeColour] is the
 * circles' colour to begin with; it is set again when the app's theme changes ([applyFanCircleStyle]).
 */
internal fun addFanOutLayers(style: Style, palette: MapPalette, chromeColour: Int) {
    // One source for the whole fan, each layer picking its own kind out of it (dispatch 2026-09-28-369, amendment -373): the
    // legs, circles, dots and icons of a push then reach the renderer as one tile update, so a drawn frame cannot hold the
    // circles of one push beside the icons of an older one, which four sources (four separate re-tilings) did on every
    // animated frame: the icons trailed the circles by one to three pushes, up to 36 dp apart (the S22's per-frame log).
    style.addSource(GeoJsonSource(FanOutIds.FAN_SOURCE, FeatureCollection.fromFeatures(emptyList())))

    fun leg(id: String, colour: Int, widthDp: Float) = LineLayer(id, FanOutIds.FAN_SOURCE).withProperties(
        PropertyFactory.lineColor(colour),
        PropertyFactory.lineWidth(widthDp),
        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
    ).also { it.setFilter(fanKindFilter(FanOutIds.KIND_LEG)) }
    style.addLayer(leg(FanOutIds.LEGS_CASING_LAYER, palette.casing, FAN_LEG_WIDTH_DP + 2 * CASING_WIDTH_DP))
    style.addLayer(leg(FanOutIds.LEGS_LAYER, palette.searchCentre, FAN_LEG_WIDTH_DP))

    // Below the dots and the icons, above the legs: the circle is the copy's background.
    style.addLayer(
        CircleLayer(FanOutIds.CIRCLES_LAYER, FanOutIds.FAN_SOURCE).withProperties(*fanCircleProperties(fanCircleStyle(chromeColour)))
            .also { it.setFilter(fanKindFilter(FanOutIds.KIND_CIRCLE)) },
    )
    // The dot: the sighting layer's own paint, so a fanned dot is the dot it was, ring and all.
    style.addLayer(
        CircleLayer(FanOutIds.DOTS_LAYER, FanOutIds.FAN_SOURCE).withProperties(*sightingCircleProperties(palette))
            .also { it.setFilter(fanKindFilter(FanOutIds.KIND_DOT)) },
    )
    style.addLayer(
        SymbolLayer(FanOutIds.ICONS_LAYER, FanOutIds.FAN_SOURCE).withProperties(*fanIconLayerProperties())
            .also { it.setFilter(fanKindFilter(FanOutIds.KIND_ICON)) },
    )
}

/**
 * The icon layer's own properties, a function so the test can read the values it is built with (a
 * `SymbolLayer` cannot be built under Robolectric, a `PropertyValue` can).
 */
internal fun fanIconLayerProperties(): Array<PropertyValue<*>> = arrayOf(
    PropertyFactory.iconImage(Expression.get(FanOutIds.IMAGE_PROPERTY)),
    PropertyFactory.iconAllowOverlap(true),
    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
    // Each copy's own offset: the glyph's centring offset times the fold's progress, so a copy's body is on its circle's
    // centre when spread and its anchor is on the coordinate, exactly where its original draws, when folded.
    PropertyFactory.iconOffset(Expression.get(FanOutIds.ICON_OFFSET_PROPERTY)),
    // Which copy is on top where they overlap: the place of its original's layer in the draw order, so the copies stack as
    // the originals do (dispatch 2026-09-28-318, fail 5). Without a sort key, `icon-allow-overlap` makes this layer order its
    // copies by viewport y, which says nothing about the registry, and the front glyph changed when the copies gave way to
    // the originals. Rejected: ordering the features alone, which the y-ordering overrides.
    PropertyFactory.symbolSortKey(Expression.get(FanOutIds.SORT_KEY_PROPERTY)),
)

/**
 * [style]'s paint as MapLibre property values: radius (dp), colour, opacity, no ring. The radius is [FanCircleStyle.radiusDp] times
 * each circle's [FanOutIds.CIRCLE_SCALE_PROPERTY] (the fold's progress), so it grows from nothing as the fan opens and is gone
 * when it has folded (dispatch 2026-09-28-299). Device-only, like every SDK call here.
 */
private fun fanCircleProperties(style: FanCircleStyle): Array<PropertyValue<*>> = arrayOf(
    PropertyFactory.circleRadius(Expression.product(Expression.literal(style.radiusDp), Expression.get(FanOutIds.CIRCLE_SCALE_PROPERTY))),
    PropertyFactory.circleColor(style.colour),
    PropertyFactory.circleOpacity(style.opacity),
    PropertyFactory.circleStrokeWidth(0f),
)

/**
 * Sets the circle layer's paint to [chromeColour]'s [fanCircleStyle], on a style already loaded, so a
 * change of the app's theme recolours the circles without reloading the map. A layer the style does not
 * have is logged, not skipped silently.
 */
internal fun applyFanCircleStyle(style: Style, chromeColour: Int) {
    val layer = style.getLayer(FanOutIds.CIRCLES_LAYER)
    if (layer == null) {
        Log.w(FAN_OUT_TAG, "The ${FanOutIds.CIRCLES_LAYER} layer is not in the loaded style; the circles were not recoloured.")
        return
    }
    layer.setProperties(*fanCircleProperties(fanCircleStyle(chromeColour)))
}

/** The contents of the fan's source at one moment, by kind (they are pushed as one: [fanPushPlan]). */
internal data class FanFrame(
    val legs: FeatureCollection,
    val circles: FeatureCollection,
    val dots: FeatureCollection,
    val icons: FeatureCollection,
)

private val EMPTY_FRAME = FanFrame(
    FeatureCollection.fromFeatures(emptyList()),
    FeatureCollection.fromFeatures(emptyList()),
    FeatureCollection.fromFeatures(emptyList()),
    FeatureCollection.fromFeatures(emptyList()),
)

/**
 * What the fan's source holds for [members], by kind, each at the place [at] gives (their moving position).
 *
 *  - **legs:** a line from each member's true position to where it is now, so the line back to the
 *    true spot is there at every frame, including the first;
 *  - **icons:** each bitmap marker's own image ([markerIconForLayer]), so a copy keeps its icon, and
 *    its [FanOutIds.ICON_OFFSET_PROPERTY]: [fanCentringOffsetDp] **times [progress]**. Spread (1) the glyph's
 *    body is centred on the copy's circle; folded (0) the offset is nothing, so the copy draws exactly where its
 *    original marker does (anchor on the coordinate, `markerSymbolLayer` has no offset) and clearing the copies
 *    moves nothing. The owner, dispatch 2026-09-28-299: "After being fanned out, the icons return to their start
 *    position. But sometimes they don't perfectly align back in their position when the animation finishes,
 *    resulting in the icons snapping into place." The offset was the full centring offset at every progress, so the
 *    pin (14 dp), the find (12.5 dp) and the flag (7.59 dp across, 12.5 down, as it was then; -358 made it 6.98 and 11.5) jumped by that much at the end of a
 *    fold and the start of an open; the photo and search centre, whose anchor is their centre, and the dots, which
 *    have no offset, never did;
 *  - **circles:** one point per copy, where the copy is now: the layer draws the copy's background
 *    circle there (dispatch 2026-09-28-265; it replaced the journal-entry halo a kept record's copy had), its
 *    radius times its [FanOutIds.CIRCLE_SCALE_PROPERTY], which is [progress]: with the glyph's offset scaled, a
 *    full circle at the coordinate would leave a pin's body up to 14 dp off its centre near 0, so the owner chose
 *    to shrink it with the fold (-299);
 *  - **dots:** a sighting, with its `observationId` and its `selected` flag from [focusedObservationId],
 *    the properties the dot layer's own paint reads.
 *
 * A member whose layer draws no marker of ours is left out and logged, never drawn as a guess.
 */
internal fun fanFrameCollections(
    members: List<FanMember>,
    at: (FanMember) -> LatLng,
    focusedObservationId: Long?,
    /** The fold's progress, 0 folded to 1 spread: what the icon offsets and the circles' scale are multiplied by. */
    progress: Float,
    /** The registry's layers in draw order, bottom to top: where a copy's original draws among the others. */
    drawOrder: List<MapLayerSpec> = MAP_LAYER_REGISTRY,
): FanFrame {
    if (members.isEmpty()) return EMPTY_FRAME
    val legs = mutableListOf<Feature>()
    val circles = mutableListOf<Feature>()
    val dots = mutableListOf<Feature>()
    val icons = mutableListOf<Feature>()
    for (member in members) {
        val now = at(member)
        val layerId = member.key.layerId
        val image = markerIconForLayer(layerId)?.imageId
        when {
            layerId == MapLayerIds.SIGHTINGS -> dots += Feature.fromGeometry(Point.fromLngLat(now.lng, now.lat)).apply {
                val observationId = member.key.featureId.toLongOrNull()
                if (observationId != null) addNumberProperty("observationId", observationId)
                addBooleanProperty("selected", observationId != null && observationId == focusedObservationId)
            }
            image != null -> {
                icons += Feature.fromGeometry(Point.fromLngLat(now.lng, now.lat)).apply {
                    addStringProperty(FanOutIds.IMAGE_PROPERTY, image)
                    val offset = markerIconForLayer(layerId)!!.glyph.fanCentringOffsetDp()
                    addProperty(FanOutIds.ICON_OFFSET_PROPERTY, JsonArray().apply { add(offset.xDp * progress); add(offset.yDp * progress) })
                    addStringProperty(FEATURE_ID_PROPERTY, member.key.featureId)
                    val place = drawOrder.indexOfFirst { it.id == layerId }
                    if (place < 0) Log.w(FAN_OUT_TAG, "$layerId is not in the draw order; its copy sorts below every other.")
                    addNumberProperty(FanOutIds.SORT_KEY_PROPERTY, place)
                }
            }
            else -> {
                Log.w(FAN_OUT_TAG, "A fanned marker on $layerId has no icon to draw; it is left out.")
                continue
            }
        }
        circles += Feature.fromGeometry(Point.fromLngLat(now.lng, now.lat)).apply { addNumberProperty(FanOutIds.CIRCLE_SCALE_PROPERTY, progress) }
        legs += Feature.fromGeometry(
            LineString.fromLngLats(listOf(Point.fromLngLat(member.lng, member.lat), Point.fromLngLat(now.lng, now.lat))),
        )
    }
    return FanFrame(
        legs = FeatureCollection.fromFeatures(legs),
        circles = FeatureCollection.fromFeatures(circles),
        dots = FeatureCollection.fromFeatures(dots),
        icons = FeatureCollection.fromFeatures(icons),
    )
}

/**
 * The filter that hides [ids] from a layer: every feature whose [property] is not one of them.
 * `["all"]` (an `all` of nothing, which is true of every feature) for none, which is also how a hidden
 * layer is let go, since this SDK version has no "remove filter" call that this file has verified. Not
 * `literal(true)`: whether the native filter parser takes a bare boolean is unverified, while `["all"]`
 * is the canonical match-everything filter. Device-only either way.
 */
internal fun fanOutHiddenFilter(property: String, ids: List<Any>): Expression =
    if (ids.isEmpty()) {
        Expression.all()
    } else {
        Expression.all(*ids.map { id ->
            when (id) {
                is Number -> Expression.neq(Expression.get(property), Expression.literal(id))
                else -> Expression.neq(Expression.get(property), Expression.literal(id.toString()))
            }
        }.toTypedArray())
    }

/**
 * Hides the originals of [members] from their own layers, and their journal halos, or, for an empty
 * list, shows everything again. Every marker layer and marker halo is set each time, so a fan that
 * changes layers, or folds, restores exactly what the last one hid. Called when the set of fanned
 * markers changes, not per frame.
 */
internal fun applyFanOutHiding(style: Style, members: List<FanMember>) {
    val idsByLayer = members.groupBy { it.key.layerId }
    val affected = MAP_LAYER_REGISTRY.filter { it.tapGroup == TapGroup.MARKER || (it.kind == LayerKind.MARKER && it.drawnWith != null) }
    for (spec in affected) {
        // A halo hides the same records its marker does.
        val owner = spec.drawnWith ?: spec.id
        val isSighting = owner == MapLayerIds.SIGHTINGS
        val ids = idsByLayer[owner].orEmpty().map { if (isSighting) (it.key.featureId.toLongOrNull() ?: it.key.featureId) else it.key.featureId }
        val filter = fanOutHiddenFilter(if (isSighting) "observationId" else FEATURE_ID_PROPERTY, ids)
        when (val layer = style.getLayer(spec.id)) {
            is SymbolLayer -> layer.setFilter(filter)
            is CircleLayer -> layer.setFilter(filter)
            null -> Log.w(FAN_OUT_TAG, "Layer ${spec.id} is not in the loaded style; its originals were not hidden.")
            else -> Log.w(FAN_OUT_TAG, "Layer ${spec.id} is a ${layer::class.java.simpleName}; its originals were not hidden.")
        }
    }
}

/**
 * What one push of [frame] sends where: a single entry, every feature of the frame in [FanOutIds.FAN_SOURCE], each marked with its
 * [FanOutIds.KIND_PROPERTY] so its own layer can pick it out ([fanKindFilter]). One entry is one `setGeoJson`, so the frame's legs,
 * circles, dots and icons are re-tiled together. An empty frame is still one push (an empty collection), which is how releasing a fan
 * clears every layer at once.
 */
internal fun fanPushPlan(frame: FanFrame): List<Pair<String, FeatureCollection>> {
    val features = mutableListOf<Feature>()
    fun add(collection: FeatureCollection, kind: String) {
        for (feature in collection.features().orEmpty()) {
            feature.addStringProperty(FanOutIds.KIND_PROPERTY, kind)
            features += feature
        }
    }
    add(frame.legs, FanOutIds.KIND_LEG)
    add(frame.circles, FanOutIds.KIND_CIRCLE)
    add(frame.dots, FanOutIds.KIND_DOT)
    add(frame.icons, FanOutIds.KIND_ICON)
    return listOf(FanOutIds.FAN_SOURCE to FeatureCollection.fromFeatures(features))
}

/** The filter that keeps one [kind] of the fan's features: a layer's own out of the shared source. */
internal fun fanKindFilter(kind: String): Expression = Expression.eq(Expression.get(FanOutIds.KIND_PROPERTY), Expression.literal(kind))

/**
 * Pushes [frame] into the fan's source, in one `setGeoJson` ([fanPushPlan]). A source missing from the style is logged, not skipped
 * silently: every style load adds it ([addFanOutLayers]).
 */
internal fun pushFanFrame(style: Style, frame: FanFrame) {
    fanPushPlan(frame).forEach { (sourceId, collection) ->
        val source = style.getSourceAs<GeoJsonSource>(sourceId)
        if (source == null) Log.w(FAN_OUT_TAG, "The $sourceId source is not in the loaded style; the fan was not drawn.") else source.setGeoJson(collection)
    }
}

/**
 * Where [member] is at [progress]: its true position on the screen now, plus its offset scaled by the
 * progress, converted back to a coordinate. Screen space, so a camera that moves while the fan folds
 * carries the copies with their true spots rather than stranding them.
 */
internal fun fanMemberLatLng(map: MapLibreMap, member: FanMember, progress: Float, density: Float): LatLng {
    val truth = map.projection.toScreenLocation(MapLibreLatLng(member.lat, member.lng))
    val moved = map.projection.fromScreenLocation(
        PointF(truth.x + member.offset.xDp * density * progress, truth.y + member.offset.yDp * density * progress),
    )
    return LatLng(moved.latitude, moved.longitude)
}

/**
 * [MapProbe] over the live map: `queryRenderedFeatures` for hits, one layer per call because a
 * returned feature does not say which layer drew it (as the click listener always did), and the
 * projection for each marker's own position.
 */
internal class MapLibreProbe(
    private val map: MapLibreMap,
    override val density: Float,
    /** Where a marker's record is now, from the lists the map draws; `null` when it is no longer drawn (never guessed). */
    private val locate: (FanKey) -> LatLng? = { null },
) : MapProbe {
    override fun hitsAt(xPx: Float, yPx: Float, layerIds: List<String>): List<TapHit> =
        layerIds.flatMap { id -> map.queryRenderedFeatures(PointF(xPx, yPx), id).map { tapHitOf(id, it) } }

    override fun hitsInBox(xPx: Float, yPx: Float, halfPx: Float, layerIds: List<String>): List<TapHit> {
        val box = RectF(xPx - halfPx, yPx - halfPx, xPx + halfPx, yPx + halfPx)
        return layerIds.flatMap { id -> map.queryRenderedFeatures(box, id).map { tapHitOf(id, it) } }
    }

    override fun markersOf(keys: List<FanKey>): List<ProbedMarker> = keys.mapNotNull { key ->
        val at = locate(key) ?: return@mapNotNull null
        val screen = map.projection.toScreenLocation(MapLibreLatLng(at.lat, at.lng))
        ProbedMarker(key, at.lat, at.lng, screen.x, screen.y)
    }

    override fun markersInBox(xPx: Float, yPx: Float, halfPx: Float, layerIds: List<String>): List<ProbedMarker> {
        val box = RectF(xPx - halfPx, yPx - halfPx, xPx + halfPx, yPx + halfPx)
        return layerIds.flatMap { id ->
            map.queryRenderedFeatures(box, id).mapNotNull { feature ->
                val point = feature.geometry() as? Point ?: return@mapNotNull null
                val featureId = tapHitOf(id, feature).featureId ?: return@mapNotNull null
                val screen = map.projection.toScreenLocation(MapLibreLatLng(point.latitude(), point.longitude()))
                ProbedMarker(FanKey(id, featureId), point.latitude(), point.longitude(), screen.x, screen.y)
            }
        }.distinctBy { it.key }
    }
}

private const val FAN_OUT_TAG = "MarkerFanOut"
