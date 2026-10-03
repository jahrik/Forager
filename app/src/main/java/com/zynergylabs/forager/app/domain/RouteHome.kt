package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.Waypoint

/** SKELETON (dispatch 2026-09-28-417, tests first): the shape only. It always withholds. */
sealed interface RouteHome {
    data class Ahead(
        val lookahead: LatLng,
        val routeMeters: Double,
        val hopBand: HopBand,
        val lookaheadAlongRouteMeters: Double,
        val aimsAtRouteEnd: Boolean,
    ) : RouteHome

    data class Withheld(val reason: RouteWithheldReason, val hopBand: HopBand?) : RouteHome
}

enum class RouteWithheldReason { OFF_ROUTE, NO_USABLE_POINTS }

const val ROUTE_LOOKAHEAD_METERS = 25.0

@Suppress("UNUSED_PARAMETER")
fun routeHome(track: Track, current: LatLng, origin: Waypoint?, previousHopBand: HopBand = HopBand.NONE): RouteHome =
    RouteHome.Withheld(RouteWithheldReason.NO_USABLE_POINTS, null)
