package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.hasArrived
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.FixFreshness
import com.zynergylabs.forager.app.domain.GeoDistance
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.RouteHome
import com.zynergylabs.forager.app.domain.RouteWithheldReason
import com.zynergylabs.forager.app.domain.ageMillis
import com.zynergylabs.forager.app.domain.fixFreshness
import com.zynergylabs.forager.app.domain.isApproaching
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.formatDistanceMeters
import com.zynergylabs.forager.app.domain.model.formatDistanceWithAccuracy
import com.zynergylabs.forager.app.domain.relativeBearingDegrees
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorDark
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorLight
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.map.TrueHeadingReading
import com.zynergylabs.forager.app.ui.map.COMPASS_NORTH_UP_TEXT
import com.zynergylabs.forager.app.ui.map.COMPASS_CALIBRATING_TEXT
import com.zynergylabs.forager.app.ui.map.NavigationFacing
import com.zynergylabs.forager.app.ui.theme.Bark
import com.zynergylabs.forager.app.ui.theme.LocalForagerDarkTheme
import com.zynergylabs.forager.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

internal const val NAVIGATION_HUD_TAG = "navigation-hud"
internal const val NAVIGATION_HUD_EXIT_TAG = "navigation-hud-exit"
internal const val NAVIGATION_HUD_HEADING_TAG = "navigation-hud-heading"
internal const val NAVIGATION_HUD_DISTANCE_TAG = "navigation-hud-distance"
internal const val NAVIGATION_HUD_STATUS_TAG = "navigation-hud-status"
internal const val NAVIGATION_HUD_TARGET_TAG = "navigation-hud-target"
internal const val NAVIGATION_HUD_ELEVATION_TAG = "navigation-hud-elevation"
internal const val NAVIGATION_HUD_COORDINATES_TAG = "navigation-hud-coordinates"
internal const val NAVIGATION_HUD_RETRY_TAG = "navigation-hud-retry"

/**
 * The one message the strip and the HUD both show when there is no fix. One cause, one statement —
 * not "compass needs a fix · elevation unavailable · coordinates unavailable", three fragments the
 * owner read on device as three separate breakages (navigation-chrome dispatch, item 4). Keyed on
 * the *missing fix*, never on [TrueHeadingReading.NeedsFix]: a phone with no magnetometer and no
 * fix reads [TrueHeadingReading.NoSensor] (`rememberTrueHeading` checks the sensor first), and is
 * still, first, a location problem.
 */
internal const val NO_FIX_MESSAGE = "Location services unavailable"

/**
 * Navigation HUD, stage one, consolidated by the navigation-chrome dispatch: a north compass and a
 * target compass, both in **true** north, the straight-line distance to the target, an exit, and
 * — since the compass strip hides while navigating — the strip's elevation and coordinates on a
 * second row, the coordinates still tappable to toggle MGRS and decimal degrees. The target is
 * the active track's origin waypoint; there is no picker in this stage.
 *
 * ## Where it mounts, and why
 *
 * A child of `CompactMapTab`'s own map `Box` — the same `fillMaxSize()` Box `mapSlot` lives in,
 * because chrome composed even one level outside it rendered fully opaque over the map's
 * `AndroidView` on a real device (see `searchBarSlot`'s own doc comment there). Inserted after
 * the taxon filter chip and **before** `ForagerBottomNav`: after the icon cluster so this panel's
 * own controls win any overlap with it, before the nav so the nav keeps winning its own band (a
 * later-composed nav once swallowed the picker's OK tap — that ordering is load-bearing). Top-
 * aligned, full width, wrapping its content, padded by `topInset` alone: the compass strip is not
 * composed while this is (`CompactMapTab` gates both on one `isNavigating`), so there is nothing
 * above this panel to clear except the search bar, and it tracks that bar's fullscreen slide the
 * way the strip does. It never wraps `mapSlot`, never touches the modifier passed to it, and
 * feeds nothing into `MapOverlayContent` or `MapRenderMode`, so no map effect keys on it and the
 * map is never animated, re-measured or re-fitted (`AvailabilityScreenLayoutTest`'s measured-
 * height guard, with a HUD-open case that now also enters and leaves navigation).
 *
 * ## The exit
 *
 * The close button is part of this panel, so it is reachable whenever the HUD is — independent of
 * the icon cluster, which can be minimised or dragged to an edge. It sits at the panel's right
 * end but the panel is composed after the cluster, so even a cluster dragged up to its upward
 * bound cannot cover it. The control pill's lit return toggle exits too — two direct exits, kept
 * deliberately (navigation-chrome dispatch, owner's ruling): the toggle is the entry and a lit
 * toggle that ignores a second tap would be worse; the close button is the one guaranteed
 * reachable. **System back is not an exit** (navigation-chrome amendment): it raises a prompt
 * that back can only dismiss — see `AvailabilityScreen`'s back chain and the reason recorded
 * there. Exit means `stopReturn()` — stage one's HUD *is* the return mode, see
 * `TrackRecordingViewModel.startReturn`.
 *
 * ## What it shows, and when
 *
 * - **Heading** comes from the one [TrueHeadingReading] the strip reads when it is showing, so
 *   the two can never disagree — and since the strip hides while this shows, the heading is on
 *   screen exactly once. The north compass's arrow is rotated by *minus* the heading (it points
 *   to true north relative to the way the device faces); the target compass's arrow by the
 *   bearing *relative to the heading* ([relativeBearingDegrees]) — where to turn, not an absolute
 *   bearing. Both arrows are device-relative, deliberately the same convention.
 * - **No sensor**: the north compass says so and the target compass shows nothing — no needle and
 *   no text. It used to fall back to the absolute true bearing as text; the two-data-corrections
 *   dispatch (Part C, owner decision) withdrew that: an absolute bearing the user cannot orient to
 *   is a number without a use whether the compass is absent or untrusted, the same reasoning the
 *   approach and unreliable cases already recorded. **Unreliable compass** (compass-reliability dispatch — the
 *   sensor is present and its reading is not to be trusted, decided upstream by
 *   [com.zynergylabs.forager.app.domain.CompassTrustJudge] with hysteresis): the north compass reads "Compass
 *   unreliable" with its arrow unrotated, and the needle and its text are withheld, ranked between
 *   a lost fix and the approach threshold — see the precedence comment in [navigationReadout]. **No fix**: the heading label is a dash and the status line
 *   carries [NO_FIX_MESSAGE] — one message for one cause; the second row is not shown at all,
 *   since "elevation unavailable · coordinates unavailable" would be the same cause twice more.
 *   See [TrueHeadingReading.NeedsFix] for why not magnetic-until-then. No GPS-course fallback.
 * - **Distance** is straight-line from the current fix to the target, in the user's unit, and
 *   never more precise than the fix: "within 16 ft" inside the error circle (the "0 ft" the owner
 *   saw on device), "≈ 10 m" beyond it, plain formatting only when no accuracy was reported
 *   ([formatDistanceWithAccuracy], location-accuracy dispatch item 2). Never "arrived" —
 *   "Approaching" once inside twice the fix's reported accuracy ([isApproaching]). The fix itself
 *   has already passed the live-fix gate upstream ([com.zynergylabs.forager.app.domain.acceptLiveFix]): a fix
 *   worse than 50 m never reaches this panel, and the held one ages into the stale states below.
 * - **The needle is not drawn inside that same threshold** (navigation-chrome dispatch, item 5,
 *   diagnosed on device by the owner). Bearing to a nearby point is geometrically unstable: at
 *   10 m with 8 m accuracy, ordinary GPS drift swings the computed bearing through tens of
 *   degrees, and no heading smoothing can fix it because the heading is not what is wrong. Inside
 *   the threshold the target column shows **nothing** under its dimmed icon — the HUD shows the
 *   distance once, in the distance slot, and "Approaching" (owner's call; a first cut put the
 *   distance in the column too and the owner read "9 ft · 9 ft" on device) — and the no-sensor
 *   bearing text is withheld too, since an absolute bearing you cannot orient to is a number
 *   without a use. One constant gates both the needle and the word:
 *   [isApproaching]. Two thresholds would drift, producing a needle that vanishes before the label
 *   appears or the reverse. The bearing is never smoothed as a substitute — a smoothed unstable
 *   bearing is a stable wrong direction.
 * - **Stale fix** ([fixFreshness], HUD-only policy): past 30 s the distance de-emphasises and its
 *   age is shown — "Approaching · last fix 45 s ago" when both hold, since neither fact replaces
 *   the other; past 5 min the distance and needle are withheld and only the age remains. A
 *   one-second ticker inside this leaf keeps the age moving; it recomposes this panel only.
 * - **The route home** (dispatch 2026-09-28-423, plan task T6; decisions D1, D3 and D5), given as
 *   [ReturnRoute] while returning. The needle aims at the route's lookahead point, a point of the
 *   walked route, not straight at the start. The large figure is the distance home **along the
 *   route** (D3, option B), in plain formatting: a sum over many stored points, not one fix's
 *   radius. The status line's otherwise-empty state carries the straight line to the start,
 *   labelled "Straight line" so the two figures cannot be confused, with the accuracy-aware
 *   formatting that belongs to it. It yields to the messages that line already carried (stale,
 *   lost, approaching), as "Path home" did before it. A withheld route reads "Unable to calculate
 *   route" in the large slot (the owner's words) with no needle, and offers "Try again" only where
 *   recomputing could change the answer. Before the first route result the large slot is a dash
 *   (the planner's ruling on question 3). "Approaching" is still measured against the start
 *   itself. With no [ReturnRoute] at all the HUD is the straight-line HUD it was before, which
 *   decision D2 keeps for navigating to a waypoint; nothing passes it that way today, since
 *   `AvailabilityScreen`'s HUD is the return HUD and always passes a route.
 * - **No origin waypoint** (a track whose first gated fix never came): says so. Nothing is
 *   substituted.
 * - **Elevation and coordinates** come from the same fix, through the same [coordinatesStripText]
 *   the strip uses; the MGRS/decimal choice is hoisted to `CompactMapTab` and shared with the
 *   strip, so a format chosen here survives leaving navigation (CLAUDE.md, UX defaults). The
 *   coordinates segment's tap band is the row's remaining width by ~24dp — a real target, not the
 *   bare text — because with the cluster minimised in fullscreen this toggle is one of only three
 *   reachable affordances on screen.
 *
 * The map stays live underneath — dimming was settled against, since it saves nothing.
 */
@Composable
internal fun NavigationHud(
    heading: State<TrueHeadingReading>,
    liveFix: LocationFix.Update?,
    target: Waypoint?,
    distanceUnit: DistanceUnit,
    currentTime: CurrentTimeProvider,
    showDecimalDegrees: Boolean,
    onToggleCoordinateFormat: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    /** The route home while returning; see this composable's "The route home" and [ReturnRoute]. */
    route: ReturnRoute? = null,
    /** "Try again": recompute the route now. Offered only when [NavigationHudReadout.routeRetryOffered]. */
    onRetryRoute: () -> Unit = {},
    /**
     * Which way the map faces while navigating (dispatch 2026-09-28-430). While a stuck compass is
     * retried, and once the map has turned north-up, the heading label says so; see [navigationReadout].
     */
    facing: NavigationFacing = NavigationFacing.FACING_UP,
) {
    // Read here, in this leaf, never higher — see rememberTrueHeading's own doc comment.
    val reading by heading
    // The age ticker: the fix's timestamp is fixed, the clock moves. Lives in this composable so
    // only this panel recomposes each second.
    val now by produceState(initialValue = currentTime.nowEpochMillis(), currentTime) {
        while (true) {
            delay(AGE_TICK_MILLIS)
            value = currentTime.nowEpochMillis()
        }
    }
    val isDarkTheme = LocalForagerDarkTheme.current
    val readout = navigationReadout(reading, liveFix, target, distanceUnit, now, showDecimalDegrees, route, facing)

    CompositionLocalProvider(LocalContentColor provides if (isDarkTheme) Color.White else Bark) {
        // A plain Box with a background, deliberately opaque to touches only where its content
        // is: this panel *is* chrome and wraps its own height, so what it covers is what it
        // shows — the same shape as the compass strip it replaces while navigating.
        Box(
            modifier = modifier
                .background(
                    color = if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight,
                    shape = RectangleShape,
                )
                .testTag(NAVIGATION_HUD_TAG)
                .mapChromeContainerColor(if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // North compass.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            modifier = Modifier
                                .size(COMPASS_ICON_SIZE)
                                .rotate(readout.northArrowDegrees ?: 0f),
                        )
                        Text(
                            text = readout.headingText,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            modifier = Modifier.testTag(NAVIGATION_HUD_HEADING_TAG),
                        )
                    }
                    // Target compass.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            tint = if (readout.targetArrowDegrees != null) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(COMPASS_ICON_SIZE)
                                .rotate(readout.targetArrowDegrees ?: 0f),
                        )
                        Text(
                            text = readout.targetText,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            modifier = Modifier.testTag(NAVIGATION_HUD_TARGET_TAG),
                        )
                    }
                    // Distance and status.
                    Column(modifier = Modifier.weight(1f)) {
                        val distanceStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum")
                        val distanceColor = if (readout.distanceDeEmphasised) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
                        Text(
                            text = readout.distanceText,
                            style = distanceStyle,
                            color = distanceColor,
                            maxLines = 1,
                            modifier = Modifier.testTag(NAVIGATION_HUD_DISTANCE_TAG),
                        )
                        if (readout.routeRetryOffered) RouteRetryRow(onRetryRoute)
                        Text(
                            text = readout.statusText,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            modifier = Modifier.testTag(NAVIGATION_HUD_STATUS_TAG),
                        )
                    }
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.testTag(NAVIGATION_HUD_EXIT_TAG),
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Stop navigating")
                    }
                }
                // Second row: what the hidden strip was carrying that this panel was not. Present
                // only with a fix — without one the status line above already says the one thing
                // there is to say. The coordinates segment takes the row's remaining width as its
                // tap band, padded to ~24dp tall; the elevation is a plain readout.
                if (readout.coordinatesText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Text(
                            text = readout.elevationText.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            modifier = Modifier.testTag(NAVIGATION_HUD_ELEVATION_TAG),
                        )
                        Text("·", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = readout.coordinatesText,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onToggleCoordinateFormat)
                                .padding(vertical = Spacing.xs)
                                .testTag(NAVIGATION_HUD_COORDINATES_TAG),
                        )
                    }
                }
            }
        }
    }
}

/**
 * "⟳ Try again", on its own line under "Unable to calculate route" (dispatch 2026-09-28-423; the
 * owner chose "Own line under it", shown that on one line beside the message the owner's sentence
 * was cut to "Unable to ca…" on a 360 dp phone and to 16 of its 25 characters on the S22). The
 * message keeps the large slot's line whole, and this row makes the HUD one row taller **only while
 * a route is withheld and recomputing could change that**; with no usable points it is not drawn,
 * and that state keeps the HUD's usual height.
 *
 * At least 48 dp tall, the minimum touch target, laid out at that height rather than relying on
 * hit testing to extend a shorter row, so what a finger can reach is what is drawn. "Try again"
 * reads as something to tap, not as more text: the primary colour, underlined, behind a refresh
 * icon. A screen reader announces the row as a button whose action is "Try again".
 */
@Composable
private fun RouteRetryRow(onRetryRoute: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .heightIn(min = RETRY_ROW_MIN_HEIGHT)
            .clickable(role = Role.Button, onClickLabel = ROUTE_RETRY_TEXT, onClick = onRetryRoute)
            .testTag(NAVIGATION_HUD_RETRY_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = primary, modifier = Modifier.size(RETRY_ICON_SIZE))
        Text(
            text = ROUTE_RETRY_TEXT,
            style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
            color = primary,
            maxLines = 1,
        )
    }
}

/** The refresh control's label, the planner's wording (dispatch 2026-09-28-423), shown to the owner with its placement. */
internal const val ROUTE_RETRY_TEXT = "Try again"

/** Everything the HUD draws, as plain values — the pure half, so a sign or threshold error is a pinned-literal test failure, not a visual one. */
internal data class NavigationHudReadout(
    val headingText: String,
    /** Rotation for the north arrow, device-relative; `null` when there is no heading to draw from. */
    val northArrowDegrees: Float?,
    val targetText: String,
    /** Rotation for the target arrow, device-relative; `null` when there is no needle to draw — no heading, fix lost, or inside the approach threshold. */
    val targetArrowDegrees: Float?,
    val distanceText: String,
    val distanceDeEmphasised: Boolean,
    val statusText: String,
    /** The fix's altitude, or "Elevation unavailable" for a fix that carries none; `null` with no fix at all (the row is not drawn). */
    val elevationText: String?,
    /** MGRS or the labelled decimal pair, per the shared toggle; `null` with no fix at all (the row is not drawn). */
    val coordinatesText: String?,
    /** True when the large slot says the route could not be calculated and recomputing could change that: "Try again" is offered. */
    val routeRetryOffered: Boolean = false,
)

/**
 * The route home as the HUD shows it while returning (dispatch 2026-09-28-423, plan task T6). See
 * [returnRouteOf] for how it is read from [RouteHome].
 */
sealed interface ReturnRoute {
    /** No route result yet: the first seconds of a return, before there is a good fix. The large slot is a dash, not a failure. */
    data object Pending : ReturnRoute

    /** The needle aims at [lookahead]; the large figure is [routeMeters]. */
    data class Ahead(val lookahead: LatLng, val routeMeters: Double) : ReturnRoute

    /**
     * Withheld: "Unable to calculate route". [canRetry] is false where recomputing against the
     * same inputs cannot change the answer, a track with no usable points.
     */
    data class Unavailable(val canRetry: Boolean) : ReturnRoute
}

/**
 * [RouteHome] as the HUD shows it. `null`, no result yet, is [ReturnRoute.Pending]. A route
 * withheld because the walker is off it ([RouteWithheldReason.OFF_ROUTE]) offers "Try again", since
 * the walker moving back towards it changes the answer; one withheld for
 * [RouteWithheldReason.NO_USABLE_POINTS] does not, since nothing would (dispatch 2026-09-28-423).
 */
fun returnRouteOf(routeHome: RouteHome?): ReturnRoute = when (routeHome) {
    null -> ReturnRoute.Pending
    is RouteHome.Ahead -> ReturnRoute.Ahead(routeHome.lookahead, routeHome.routeMeters)
    is RouteHome.Withheld -> ReturnRoute.Unavailable(canRetry = routeHome.reason == RouteWithheldReason.OFF_ROUTE)
}

/** The large slot's words once the walker has arrived at the start (dispatch 2026-09-28-497). */
internal const val ARRIVED_TEXT = "Arrived"

/**
 * Whether the walker has arrived at [start] (dispatch 2026-09-28-497, plan task T7): a fix that is
 * not lost, within [hasArrived]'s radius, straight line. The one rule the HUD's "Arrived" and the
 * map's ring both read, so the two cannot disagree.
 */
internal fun arrivedAtStart(liveFix: LocationFix.Update?, start: Waypoint, nowEpochMillis: Long): Boolean {
    if (liveFix == null || fixFreshness(liveFix.ageMillis(nowEpochMillis)) == FixFreshness.LOST) return false
    return hasArrived(GeoDistance.metersBetween(LatLng(liveFix.lat, liveFix.lng), LatLng(start.lat, start.lng)), liveFix.accuracyMeters)
}

/** The large slot's words when the route is withheld: the owner's, 2026-09-12. */
internal const val ROUTE_UNAVAILABLE_TEXT = "Unable to calculate route"


internal fun navigationReadout(
    heading: TrueHeadingReading,
    liveFix: LocationFix.Update?,
    target: Waypoint?,
    distanceUnit: DistanceUnit,
    nowEpochMillis: Long,
    showDecimalDegrees: Boolean = false,
    route: ReturnRoute? = null,
    facing: NavigationFacing = NavigationFacing.FACING_UP,
): NavigationHudReadout {
    val headingDegrees = (heading as? TrueHeadingReading.Available)?.degrees
    val headingText = when (heading) {
        is TrueHeadingReading.Available -> "${heading.degrees.roundToInt() % 360}° ${cardinalDirection(heading.degrees)}"
        TrueHeadingReading.NoSensor -> "Compass unavailable"
        // Compass-reliability dispatch: present, reporting, not to be trusted. No cause named —
        // see the strip's own branch for why. The north arrow is not rotated (headingDegrees is
        // null for this state) and the needle below is withheld.
        TrueHeadingReading.Unreliable -> "Compass unreliable"
        // A dash, not a message: the status line carries NO_FIX_MESSAGE, once (owner's call).
        TrueHeadingReading.NeedsFix -> "—"
    }.let { label ->
        // Dispatch 2026-09-28-430, ruling E (continuation 2026-09-28-432): one place, one word. While
        // the map's navigation view retries a stuck compass, and once it has turned north-up, this
        // label is the notice; no separate one is drawn on the map. Only in those two states, which
        // only an unreliable compass or none at all produces.
        when (facing) {
            NavigationFacing.FACING_UP -> label
            NavigationFacing.CALIBRATING -> COMPASS_CALIBRATING_TEXT
            NavigationFacing.NORTH_UP -> COMPASS_NORTH_UP_TEXT
        }
    }
    val compassUnreliable = heading is TrueHeadingReading.Unreliable
    val northArrowDegrees = headingDegrees?.let { -it }
    val elevationText = liveFix?.let { fix -> fix.altitude?.let { "${it.roundToInt()} m" } ?: "Elevation unavailable" }
    val coordinatesText = liveFix?.let { coordinatesStripText(LatLng(it.lat, it.lng), showDecimalDegrees) }

    if (liveFix == null) {
        return NavigationHudReadout(headingText, northArrowDegrees, "Target", null, "—", false, NO_FIX_MESSAGE, null, null)
    }
    if (target == null) {
        return NavigationHudReadout(headingText, northArrowDegrees, "No target", null, "—", false, "No origin waypoint for this track", elevationText, coordinatesText)
    }

    val here = LatLng(liveFix.lat, liveFix.lng)
    val there = LatLng(target.lat, target.lng)
    // Where the needle aims: the route's lookahead while returning (decision D1), the target
    // itself with no route given. A route that is pending or withheld has nothing to aim at, so
    // there is no needle (decision D5: never a straight line standing in for the route).
    val aim = when (route) {
        null -> there
        is ReturnRoute.Ahead -> route.lookahead
        ReturnRoute.Pending, is ReturnRoute.Unavailable -> null
    }
    val bearing = aim?.let { GeoDistance.initialBearingDegrees(here, it) }
    val distanceMeters = GeoDistance.metersBetween(here, there)
    val age = liveFix.ageMillis(nowEpochMillis)
    val freshness = fixFreshness(age)
    // The one threshold — see the class doc's needle paragraph.
    val approaching = freshness != FixFreshness.LOST && isApproaching(distanceMeters, liveFix.accuracyMeters)
    // Never more precision than the fix supports — "within 16 ft" inside the error circle, "≈ 10 m"
    // beyond it, today's formatting when no accuracy was reported. See formatDistanceWithAccuracy.
    val straightLineText = formatDistanceWithAccuracy(distanceMeters, liveFix.accuracyMeters, distanceUnit)
    val distanceText = when {
        freshness == FixFreshness.LOST -> "—"
        // Dispatch -497: the return's arrival at the start. Only while returning (a route is given);
        // navigation stays on until the walker ends it.
        route != null && arrivedAtStart(liveFix, target, nowEpochMillis) -> ARRIVED_TEXT
        route == null -> straightLineText
        route is ReturnRoute.Ahead -> formatDistanceMeters(route.routeMeters, distanceUnit)
        route is ReturnRoute.Unavailable -> ROUTE_UNAVAILABLE_TEXT
        else -> "—"
    }

    // Four things withhold the needle, and their ORDER IS DELIBERATE (compass-reliability
    // dispatch, owner decision) — do not let branch position imply it, and do not insert a fifth
    // term without deciding where it ranks:
    //   1. Lost fix wins. A needle needs heading AND position; the position failure is the more
    //      fundamental, and the user's remedy is different.
    //   2. Unreliable compass next. The heading exists and cannot be trusted.
    //   3. Approach threshold last. The only one of the three that is not a failure.
    //   4. No heading at all (no sensor) is the existing case — and since the two-data-corrections
    //      dispatch (Part C) it withholds the text as well, not just the needle.
    // `compassUnreliable` is named in the `if` even though headingDegrees is already null for that
    // state, so the term is visible where the order is stated rather than implied by a null.
    // A fifth term, ranked last (dispatch 2026-09-28-423): no aim, a route pending or withheld.
    // It is not a failure of the compass or the fix, so it ranks below all four.
    val targetArrowDegrees = if (headingDegrees != null && freshness != FixFreshness.LOST && !compassUnreliable && !approaching && bearing != null) relativeBearingDegrees(bearing, headingDegrees) else null
    val targetText = when {
        freshness == FixFreshness.LOST -> "Target"
        // Same rendering as the approach case below, and for the same reason the owner recorded
        // there: an absolute bearing you cannot orient to is a number without a use. Unreliable
        // leaves the user in that position.
        compassUnreliable -> ""
        // Nothing — not the distance (that was one number in two slots on device, "9 ft · 9 ft ·
        // Approaching"), not a dash, not a placeholder. The distance slot carries the one number.
        approaching -> ""
        // No aim: the large slot already says why (a dash, or "Unable to calculate route").
        bearing == null -> ""
        headingDegrees != null -> "Turn ${relativeBearingDegrees(bearing, headingDegrees).roundToInt() % 360}°"
        // No sensor. This branch used to read "Bearing N° X" — the one state that still showed the
        // absolute bearing as text. The compass-reliability dispatch asked for the unreliable case
        // to match it and was wrong about what it did; the follow-up (two-data-corrections dispatch,
        // Part C, owner decision) brought no-sensor into line with approach and unreliable instead:
        // a number the user cannot orient to is withheld, the distance slot carries the one number.
        else -> ""
    }
    // Dispatch 2026-09-28-423 (decision D3, option B): while returning, the straight line moves
    // here from the large slot, labelled, since the large slot now holds the route figure. It
    // takes the state where the line was empty, the one "Path home" took before it: a stale or
    // lost fix already owns the line with a message the walker needs more, and "Approaching" is
    // left as it is until plan task T7 moves it onto the start's glyph. One line of labelMedium
    // has no room for both a label and a stale fix's age.
    val statusText = when (freshness) {
        FixFreshness.LOST -> "No fix for ${formatFixAge(age)}"
        FixFreshness.STALE -> if (approaching) "Approaching · last fix ${formatFixAge(age)} ago" else "Last fix ${formatFixAge(age)} ago"
        FixFreshness.FRESH -> when {
            approaching -> "Approaching"
            route != null -> "Straight line $straightLineText"
            else -> ""
        }
    }
    return NavigationHudReadout(
        headingText = headingText,
        northArrowDegrees = northArrowDegrees,
        targetText = targetText,
        targetArrowDegrees = targetArrowDegrees,
        distanceText = distanceText,
        distanceDeEmphasised = freshness == FixFreshness.STALE,
        statusText = statusText,
        elevationText = elevationText,
        coordinatesText = coordinatesText,
        routeRetryOffered = freshness != FixFreshness.LOST && route is ReturnRoute.Unavailable && route.canRetry,
    )
}

/** "48 s" under a minute, "6 min" from a minute on — coarse on purpose; the number's job is "old", not a stopwatch. */
internal fun formatFixAge(ageMillis: Long): String {
    val seconds = (ageMillis / 1_000L).coerceAtLeast(0L)
    return if (seconds < 60L) "$seconds s" else "${seconds / 60L} min"
}

private val COMPASS_ICON_SIZE = 22.dp
private val RETRY_ICON_SIZE = 18.dp
private val RETRY_ROW_MIN_HEIGHT = 48.dp
private const val AGE_TICK_MILLIS = 1_000L
