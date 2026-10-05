package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.NavigationFacing
import com.zynergylabs.forager.app.ui.map.TrueHeadingReading

/** The HUD's status line with only an approximate position (the owner's words, RECORD -508). */
internal const val APPROXIMATE_HUD_TEXT = "Approximate, finding GPS…"

/**
 * Everything the HUD draws, from the GPS fix, the approximate reading and the last known one
 * (dispatch 2026-09-28-510).
 */
internal fun hudReadout(
    heading: TrueHeadingReading,
    liveFix: LocationFix.Update?,
    approximateFix: LocationFix.Update?,
    lastKnownFix: LocationFix.Update?,
    target: Waypoint?,
    distanceUnit: DistanceUnit,
    nowEpochMillis: Long,
    showDecimalDegrees: Boolean = false,
    route: ReturnRoute? = null,
    facing: NavigationFacing = NavigationFacing.FACING_UP,
): NavigationHudReadout = navigationReadout(heading, liveFix, target, distanceUnit, nowEpochMillis, showDecimalDegrees, route, facing)
