package com.zynergylabs.forager.app.ui.map

import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.geometry.Offset

/**
 * Where "Navigate" was tapped for a waypoint (dispatch 2026-09-28-502, Amendment 1, the owner's
 * "Back to where you tapped Navigate"): one Back ends the navigation and puts the walker back at this
 * step. Each kind names the waypoint.
 */
sealed interface WaypointNavigationOrigin {
    val waypointId: String

    /** The waypoint's bubble on the Maps tab, where it was anchored when Navigate was tapped. */
    data class MapBubble(override val waypointId: String, val anchorPx: Offset, val bearingDeg: Float) : WaypointNavigationOrigin

    /** The waypoint's details sheet, opened from its bubble on the Maps tab. */
    data class MapDetails(override val waypointId: String) : WaypointNavigationOrigin

    /** The waypoint's row in Records. */
    data class RecordsRow(override val waypointId: String) : WaypointNavigationOrigin

    /** The waypoint's details sheet, opened from Records. */
    data class RecordsDetails(override val waypointId: String) : WaypointNavigationOrigin
}

/** Saves a [WaypointNavigationOrigin] as one string. */
internal val WaypointNavigationOriginSaver: Saver<WaypointNavigationOrigin?, String> = Saver(
    save = { "" },
    restore = { null },
)
