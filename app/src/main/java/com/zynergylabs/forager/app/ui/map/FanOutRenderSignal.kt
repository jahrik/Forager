package com.zynergylabs.forager.app.ui.map

import android.graphics.RectF
import android.util.Log
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOutHideGate
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.coroutines.resume

/**
 * What the renderer must report, once a fan's copies are on screen (dispatch 2026-09-28-369, amendment -371): the id of every
 * icon copy (`image:featureId`, as [fanFrameCollections] writes it), the observation id of every dot copy, and the number
 * of dot copies that have no numeric observation id to match by (counted, not matched).
 */
internal data class ExpectedCopies(val iconIds: Set<String>, val dotIds: Set<String>, val unnumberedDots: Int) {
    val isEmpty: Boolean get() = iconIds.isEmpty() && dotIds.isEmpty() && unnumberedDots == 0
}

/** What [members]' copies will be called in the renderer's answers. A member with no icon and no dot has no copy ([fanFrameCollections] logs and skips it). */
internal fun expectedCopies(members: List<FanMember>): ExpectedCopies {
    val icons = mutableSetOf<String>()
    val dots = mutableSetOf<String>()
    var unnumbered = 0
    for (member in members) {
        val layerId = member.key.layerId
        if (layerId == MapLayerIds.SIGHTINGS) {
            val observationId = member.key.featureId.toLongOrNull()
            if (observationId != null) dots += observationId.toString() else unnumbered++
        } else {
            val image = markerIconForLayer(layerId)?.imageId ?: continue
            icons += "$image:${member.key.featureId}"
        }
    }
    return ExpectedCopies(icons, dots, unnumbered)
}

/** True when every expected copy is among what the renderer reported: the signal that the copies are drawn. An empty expectation is true at once. */
internal fun copiesDrawn(expected: ExpectedCopies, renderedIconIds: Set<String>, renderedDotIds: Set<String>, renderedUnnumberedDots: Int): Boolean =
    renderedIconIds.containsAll(expected.iconIds) &&
        renderedDotIds.containsAll(expected.dotIds) &&
        renderedUnnumberedDots >= expected.unnumberedDots

/**
 * Suspends until the renderer reports every copy of [members] in a frame it has drawn, then returns true. Event-driven, not a wait: it
 * adds an `OnDidFinishRenderingFrameListener` and asks `queryRenderedFeatures` of the fan's own icon and dot layers on each frame, and removes
 * the listener when it finishes or the caller cancels it, so nothing queries once the answer is known or no longer wanted. [giveUp] is asked
 * on each frame that did not complete it, with the number of frames queried so far; when it says yes the wait ends and returns false, so the
 * caller can take its logged last resort. [onFrame] is called with the same count. Must be called on the main thread, where the map's
 * events arrive. Device-only: a `MapView` cannot be built under Robolectric.
 */
internal suspend fun awaitCopiesRendered(
    mapView: MapView,
    map: MapLibreMap,
    members: List<FanMember>,
    onFrame: (Int) -> Unit = {},
    giveUp: (framesQueried: Int) -> Boolean = { false },
): Boolean {
    val expected = expectedCopies(members)
    if (expected.isEmpty) return true
    return suspendCancellableCoroutine { continuation ->
        var frames = 0
        lateinit var listener: MapView.OnDidFinishRenderingFrameListener
        listener = MapView.OnDidFinishRenderingFrameListener { _, _, _ ->
            frames++
            onFrame(frames)
            val box = RectF(0f, 0f, mapView.width.toFloat(), mapView.height.toFloat())
            val icons = map.queryRenderedFeatures(box, FanOutIds.ICONS_LAYER).mapNotNull { feature ->
                val image = feature.getStringProperty(FanOutIds.IMAGE_PROPERTY)
                val id = feature.getStringProperty(FEATURE_ID_PROPERTY)
                if (image != null && id != null) "$image:$id" else null
            }.toSet()
            val dotFeatures = map.queryRenderedFeatures(box, FanOutIds.DOTS_LAYER)
            val dotIds = dotFeatures.mapNotNull { it.getNumberProperty("observationId")?.toLong()?.toString() }.toSet()
            val unnumbered = dotFeatures.count { it.getNumberProperty("observationId") == null }
            if (copiesDrawn(expected, icons, dotIds, unnumbered)) {
                mapView.removeOnDidFinishRenderingFrameListener(listener)
                if (continuation.isActive) continuation.resume(true)
            } else if (giveUp(frames)) {
                mapView.removeOnDidFinishRenderingFrameListener(listener)
                if (continuation.isActive) continuation.resume(false)
            }
        }
        mapView.addOnDidFinishRenderingFrameListener(listener)
        continuation.invokeOnCancellation { mapView.removeOnDidFinishRenderingFrameListener(listener) }
    }
}

private const val SIGNAL_TAG = "MarkerFanOut"

/** The rendered frames at rest after which a hide still waiting is done anyway, and logged: the last resort, not the way it normally ends (the signal arrives in about three frames from the open). */
internal const val REST_FRAMES_BEFORE_GIVING_UP = 3

/** True when at least one of [members] is inside the map view: with none on screen there is nothing to blink and no copies to wait for. */
private fun anyMemberOnScreen(map: MapLibreMap, mapView: MapView, members: List<FanMember>): Boolean = members.any { member ->
    val at = map.projection.toScreenLocation(LatLng(member.lat, member.lng))
    at.x in 0f..mapView.width.toFloat() && at.y in 0f..mapView.height.toFloat()
}

/**
 * Hides the originals of [members] once their copies are drawn: the wait named by [generation] in [gate]. Three ways it ends, each by the
 * gate so a wait that was replaced or cancelled hides nothing: the renderer reports the copies ([awaitCopiesRendered]); no member is on
 * screen, so there is nothing to wait for and the originals are hidden at once (logged, info); or the fan has been at rest
 * ([atRest]) for [REST_FRAMES_BEFORE_GIVING_UP] rendered frames without the report, when they are hidden anyway and a warning says so.
 * Cancelled with its caller, which removes the frame listener. Device-only.
 */
internal suspend fun hideWhenCopiesDrawn(
    style: Style,
    map: MapLibreMap,
    mapView: MapView,
    gate: FanOutHideGate,
    members: List<FanMember>,
    generation: Int,
    atRest: () -> Boolean,
) {
    if (!anyMemberOnScreen(map, mapView, members)) {
        Log.i(SIGNAL_TAG, "None of the ${members.size} fanned markers is on screen; their originals are hidden at once.")
        gate.onCopiesDrawn(generation)?.let { applyFanOutHiding(style, it.hide) }
        return
    }
    var framesAtRest = 0
    val drawn = awaitCopiesRendered(mapView, map, members, giveUp = {
        if (atRest()) framesAtRest++
        framesAtRest >= REST_FRAMES_BEFORE_GIVING_UP
    })
    val step = if (drawn) {
        gate.onCopiesDrawn(generation)
    } else {
        gate.onRest()?.also {
            Log.w(SIGNAL_TAG, "The renderer did not report the ${members.size} copies drawn within $REST_FRAMES_BEFORE_GIVING_UP rendered frames of the fan being at rest; their originals were hidden anyway.")
        }
    }
    step?.let { applyFanOutHiding(style, it.hide) }
}
