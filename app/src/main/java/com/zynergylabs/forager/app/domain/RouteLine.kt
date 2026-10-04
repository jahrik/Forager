package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng

/** Tests-first stub (dispatch 2026-09-28-497). */
data class RouteLine(
    val atReturn: List<LatLng>,
    val ahead: List<LatLng>,
    val aheadIsCurrent: Boolean,
)

/** Tests-first stub. */
fun nextRouteLine(previous: RouteLine?, routeHome: RouteHome?): RouteLine? = null
