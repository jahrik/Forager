package com.zynergylabs.forager.app.ui.map.fanout

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.layers.MapLayerSpec
import com.zynergylabs.forager.app.ui.map.layers.TapHit

/** STUB (tests-first commit): every tap is reported as a plain tap. */
interface MapProbe {
    val density: Float
    fun hitsAt(xPx: Float, yPx: Float, layerIds: List<String>): List<TapHit>
    fun hitsInBox(xPx: Float, yPx: Float, halfPx: Float, layerIds: List<String>): List<TapHit>
    fun markersInBox(xPx: Float, yPx: Float, halfPx: Float, layerIds: List<String>): List<ProbedMarker>
}

interface MapTapSinks {
    fun onPlainTap()
    fun onSightingTap(observationId: Long?, xPx: Float, yPx: Float)
    fun onFeatureTap(layerId: String, featureId: String, xPx: Float, yPx: Float, at: LatLng)
    fun onUnidentifiedFeature(layerId: String)
}

class MapTapHandler(
    private val fan: MarkerFanOutState,
    private val probe: MapProbe,
    private val drawOrder: () -> List<MapLayerSpec>,
    private val sinks: MapTapSinks,
) {
    fun onMapTap(at: LatLng, xPx: Float, yPx: Float) {
        sinks.onPlainTap()
    }

    fun onCameraMoveStarted() {}

    fun onContentChanged() {}
}
