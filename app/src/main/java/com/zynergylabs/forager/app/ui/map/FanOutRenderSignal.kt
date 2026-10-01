package com.zynergylabs.forager.app.ui.map

import android.graphics.RectF
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
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
 * the listener when it finishes or the caller cancels it, so nothing queries once the answer is known or no longer wanted. [onFrame] is called
 * with the number of frames queried so far (the instrumented build counts them with it). Must be called on the main thread, where the map's
 * events arrive. Device-only: a `MapView` cannot be built under Robolectric.
 */
internal suspend fun awaitCopiesRendered(
    mapView: MapView,
    map: MapLibreMap,
    members: List<FanMember>,
    onFrame: (Int) -> Unit = {},
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
            }
        }
        mapView.addOnDidFinishRenderingFrameListener(listener)
        continuation.invokeOnCancellation { mapView.removeOnDidFinishRenderingFrameListener(listener) }
    }
}
