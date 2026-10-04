package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.RouteLine
import com.zynergylabs.forager.app.domain.model.LatLng
import org.maplibre.geojson.FeatureCollection

/** Tests-first stub (dispatch 2026-09-28-497). */
data class RouteOnMap(val line: RouteLine?, val arrivedAt: LatLng?)

internal object RouteHomeIds {
    const val PASSED_SOURCE = "route-home-passed"
    const val PASSED_LAYER = "route-home-passed-layer"
    const val AHEAD_SOURCE = "route-home-ahead"
    const val AHEAD_LAYER = "route-home-ahead-layer"
    const val ARRIVED_SOURCE = "route-home-arrived"
    const val ARRIVED_LAYER = "route-home-arrived-layer"
    const val ARRIVED_IMAGE = "route-home-arrived-image"
}

/** Stub. */
internal fun routeHomeFeatureCollections(route: RouteOnMap?): Map<String, FeatureCollection> =
    listOf(RouteHomeIds.PASSED_SOURCE, RouteHomeIds.AHEAD_SOURCE, RouteHomeIds.ARRIVED_SOURCE).associateWith { FeatureCollection.fromFeatures(emptyList()) }

/** Stub. */
internal const val ROUTE_AHEAD_FADED_OPACITY = 1f

/** Stub. */
internal fun routeAheadOpacity(route: RouteOnMap?): Float = 1f
