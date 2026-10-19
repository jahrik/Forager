package com.zynergylabs.forager.app.ui.availability

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.Layout
import com.zynergylabs.forager.app.ui.motion.clickableWithShapedPress
import com.zynergylabs.forager.app.ui.motion.BouncingIconButton
import com.zynergylabs.forager.app.ui.motion.WordSwap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.unit.IntSize
import com.zynergylabs.forager.app.ui.motion.LocalReduceMotion
import com.zynergylabs.forager.app.ui.motion.MotionTokens
import com.zynergylabs.forager.app.ui.motion.rememberLastShown
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.zynergylabs.forager.app.ui.map.MAP_ICON_STACK_BORDER_COLOR_LIGHT
import com.zynergylabs.forager.app.ui.map.MAP_ICON_STACK_BORDER_COLOR_DARK
import com.zynergylabs.forager.app.ui.map.MapChromeOutlineSides
import com.zynergylabs.forager.app.ui.map.mapChromeOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.semantics.text
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.fixFreshness
import com.zynergylabs.forager.app.domain.isApproaching
import com.zynergylabs.forager.app.domain.isInApproachingZone
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.formatDistanceWithAccuracy
import com.zynergylabs.forager.app.domain.relativeBearingDegrees
import com.zynergylabs.forager.app.domain.DistanceKind
import com.zynergylabs.forager.app.domain.turnText
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorDark
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorLight
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.map.TrueHeadingReading
import com.zynergylabs.forager.app.ui.map.NavigationFacing
import com.zynergylabs.forager.app.ui.theme.Bark
import com.zynergylabs.forager.app.ui.theme.LocalForagerDarkTheme
import com.zynergylabs.forager.app.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Navigation HUD, stage one, consolidated by the navigation-chrome dispatch: a north compass and a
 * target compass, both in **true** north, the straight-line distance to the target, an exit, and
 * — since the compass strip hides while navigating — the strip's elevation and coordinates on a
 * second row, the coordinates still tappable to toggle MGRS and decimal degrees. The target is
 * the active track's origin waypoint; there is no picker in this stage.
 *
 * ## The card (dispatch 2026-10-12-06, rearranged by dispatch 2026-10-12-08)
 *
 * Since dispatch 2026-10-12-08 (RECORD -945, -948, -950, -951) the compass strip stays while navigating and its line drops the
 * north arrow and the heading; the navigation info ([NavigationInfo]) shows the distance, the turn line with both arrows and the
 * heading, the kind word, the evening stack and the X. In portrait it is inside the strip's own card, under a thin line; in
 * landscape it is this composable's card, hanging under the strip at the strip's width. Height, the grid reference and the
 * three-dot button are the strip's. Parts of the description below that speak of rows, of elevation and coordinates on this
 * display, or of the strip being hidden while navigating describe the layout before those dispatches; the readout itself
 * ([navigationReadout]) is unchanged apart from [NavigationHudReadout.statusIsPositionStatus].
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
 * - **Words and labels** (dispatch 2026-09-28-677, data part B; the owner, RECORD -656, "Plain words + labels"): the turn
 *   under the target arrow reads "Slight left · 10°" ([turnText], bands in `domain/NavigationWords.kt`), where it read
 *   "Turn 350°"; the large figure is followed by what it measures, "by trail" or "straight" ([DistanceKind]); the heading
 *   and the elevation carry short labels ([ALTITUDE_LABEL]; the heading's "Facing" was removed by RECORD -728). The heading moved from under the north arrow
 *   to the second row to make room: at 360 dp a labelled heading, the longest turn words and a distance with its kind
 *   cannot share the first row (widths in `docs/ui/2026-10-07-data-b-track-nav-report.md`).
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
 *   **Superseded for the word by dispatch 2026-09-28-578 (Amendment 1, RECORD -579):** arrival's
 *   radius always held [isApproaching], so "Approaching" never showed without "Arrived". The word now
 *   has its own zone, within 100 m and not arrived ([isInApproachingZone]), where the needle still
 *   points; arrived, "Arrived" stands alone and the status line is empty, or the fix's age when stale.
 *   [isApproaching] still gates the needle and the target column, as above.
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
 *   decision D2 keeps for navigating to a waypoint: since dispatch 2026-09-28-502 that is how
 *   `CompactMapTab` passes it while a chosen waypoint is the target. "Arrived" reads in either mode.
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
    /**
     * Dispatch 2026-09-28-510: the newest reading the live gate refused, and the platform's last known
     * location. Shown in place of the GPS fix only by [hudReadout]'s rule; never measured from for
     * "Arrived", which reads [liveFix] alone.
     */
    approximateFix: LocationFix.Update? = null,
    lastKnownFix: LocationFix.Update? = null,
    /**
     * The sundown line (dispatch 2026-09-28-592, plan task T3): the recording's, moved here from the
     * strip once Return or Navigate is tapped, or, navigating with no recording, sunset and dark only.
     * `null` draws nothing.
     */
    sundownLine: String? = null,
    /** Back by's line (dispatch 2026-09-28-645), under the sundown line; `null` draws nothing. */
    backByLine: String? = null,
    /**
     * RECORD -770 (the owner: "Drop the evening line then (Recommended)"): the lowest the display may end, in the root, while the
     * landscape L is below it, so that the bar is covered by no more than its Fullscreen row; `null` puts no limit. Where the
     * evening lines would take the display past it, they leave the display ([eveningLineFits]); the alerts are unaffected.
     */
    eveningLineBottomLimitInRootPx: Float? = null,
    /**
     * RECORD -955 ("C yes", extending -770 to the landscape card on the bottom edge, RECORD -953): the highest the card's top may
     * reach, in the root, so that it covers neither Reset north nor the walker's dot; `null` puts no limit. Where the evening
     * lines would take the card's top above it, they leave the card ([eveningStackFitsBelow]); the alerts are unaffected.
     */
    eveningLineTopLimitInRootPx: Float? = null,
    /**
     * RECORD -844, -845: the return has ended on arrival and the display holds "Arrived" until it closes by
     * itself. The large slot reads "Arrived" whatever the fix says ([heldArrivalReadout]), so a drive-off
     * inside the hold does not turn it back into a distance.
     */
    arrivalHeld: Boolean = false,
    /** Dispatch 2026-10-12-06: the rounded card's corners, and how far its fill reaches up behind the piece above (RoundedMapCard.kt). */
    cardCorners: MapCardCorners = MapCardCorners.BOTTOM,
    tuck: Dp = 0.dp,
    /** Dispatch 2026-10-12-08: the corners of the piece the card is tucked under (the landscape compass strip). */
    upperCorners: MapCardCorners = MapCardCorners.ALL,
    /** Dispatch 2026-10-13-02 (RECORD -966, -967): the card's outlined sides ([landscapeColumnOutlineSides]); `null` draws none. */
    outlineSides: MapChromeOutlineSides? = null,
) {
    val isDarkTheme = LocalForagerDarkTheme.current
    CompositionLocalProvider(LocalContentColor provides if (isDarkTheme) Color.White else Bark) {
        val fill = if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight
        // The rounded card on its own: in landscape, hanging under the compass strip at its width (RECORD -946, -950, -951), and
        // wherever a test composes the display alone. Still a plain background with no pointer input (RoundedMapCard.kt): what
        // covers the map is what it shows, and only its controls (the X and Try again) take touches.
        Box(
            modifier = modifier
                .mapCardSurface(fill = fill, corners = cardCorners, tuck = tuck, upperCorners = upperCorners, outline = mapChromeOutline(), outlineSides = outlineSides)
                .testTag(NAVIGATION_HUD_TAG)
                .mapChromeContainerColor(fill),
        ) {
            NavigationInfo(
                heading = heading,
                liveFix = liveFix,
                target = target,
                distanceUnit = distanceUnit,
                currentTime = currentTime,
                showDecimalDegrees = showDecimalDegrees,
                onExit = onExit,
                route = route,
                onRetryRoute = onRetryRoute,
                facing = facing,
                approximateFix = approximateFix,
                lastKnownFix = lastKnownFix,
                sundownLine = sundownLine,
                backByLine = backByLine,
                eveningLineBottomLimitInRootPx = eveningLineBottomLimitInRootPx,
                eveningLineTopLimitInRootPx = eveningLineTopLimitInRootPx,
                arrivalHeld = arrivalHeld,
            )
        }
    }
}

/**
 * Dispatch 2026-10-12-08 (RECORD -945, -948, -950): the navigation info, without a surface of its own: inside the compass strip's
 * card under a thin line in portrait ("have the card just extend down rather than a dual tuck"), and inside [NavigationHud]'s card
 * in landscape. As the owner's approved drawing has it:
 *
 * ```
 * |        804 ft         | Sunset 12:41 AM X|
 * | ahead 1° ▲ ▲ 134° SE  | in 2 h 28 min    |
 * |       by trail        | dark 1:13        |
 * ```
 * - **Left part**, each line centred (RECORD -945, "Both (Recommended)"): the distance, about twice the other text; the turn words
 *   with the needle after them and the north arrow and heading right after that, the two arrows neighbours ("The arrows being
 *   side by side is a compass visual"); the kind word, "by trail" or "straight", a small line of its own (RECORD -945, "Full
 *   dividers + by trail under figure").
 * - **A divider** the parts' full height (RECORD -945), kept when there is no evening stack (RECORD -951).
 * - **Right part**: the evening stack (RECORD -941), centred; then the X at the right edge.
 * - Under them, each only while it has something to say: the route message with Try again, and the status line. The status line
 *   leaves out what the strip already says about the position (no fix, approximate, last seen: RECORD -951, shown once, on the
 *   strip).
 * Height, the grid reference and the three-dot button are the strip's, so nothing shows twice (RECORD -950).
 */
@Composable
internal fun NavigationInfo(
    heading: State<TrueHeadingReading>,
    liveFix: LocationFix.Update?,
    target: Waypoint?,
    distanceUnit: DistanceUnit,
    currentTime: CurrentTimeProvider,
    showDecimalDegrees: Boolean,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    route: ReturnRoute? = null,
    onRetryRoute: () -> Unit = {},
    facing: NavigationFacing = NavigationFacing.FACING_UP,
    approximateFix: LocationFix.Update? = null,
    lastKnownFix: LocationFix.Update? = null,
    sundownLine: String? = null,
    backByLine: String? = null,
    eveningLineBottomLimitInRootPx: Float? = null,
    eveningLineTopLimitInRootPx: Float? = null,
    arrivalHeld: Boolean = false,
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
    val liveReadout = hudReadout(reading, liveFix, approximateFix, lastKnownFix, target, distanceUnit, now, showDecimalDegrees, route, facing)
    val readout = if (arrivalHeld) heldArrivalReadout(liveReadout) else liveReadout
    // RECORD -955: the info's own bottom in the root, for a card anchored to the bottom edge (RECORD -953), whose top is its bottom
    // less its height. The height the decision uses is built from parts the evening stack does not change (the top padding, the left
    // part, the lines below, the bottom padding), never read back from the card: a first build read the card's measured height,
    // which mid-animation fed the decision back into itself and never settled (CI 38304300338, the T11 harness at font 2.0).
    var infoBottomInRootPx by remember { mutableFloatStateOf(Float.NaN) }
    var lastEveningShown by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { infoBottomInRootPx = it.positionInRoot().y + it.size.height },
    ) {
        val metrics = rememberInfoMetrics()
        val partsPx = constraints.maxWidth - metrics.fixedPx
        // RECORD -770: where the parts and the lines under them end without the evening stack, which the stack does not change, so
        // the decision to leave it out cannot feed back into what it is decided on.
        var partsTopInRootPx by remember { mutableFloatStateOf(Float.NaN) }
        var leftHeightPx by remember { mutableFloatStateOf(0f) }
        var linesBelowHeightPx by remember { mutableFloatStateOf(0f) }
        val pieces = eveningPieces(sundownLine, backByLine)
        val eveningStyle = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
        val pieceHeightPx = rememberTextMeasurer().measure("Sunset", eveningStyle, maxLines = 1).size.height
        val bottomPaddingPx = with(LocalDensity.current) { Spacing.xs.toPx() }
        val topPaddingPx = with(LocalDensity.current) { CARD_TOP_PADDING.toPx() }
        val heightWithStackPx = topPaddingPx + maxOf(leftHeightPx, (pieces.size * pieceHeightPx).toFloat()) + linesBelowHeightPx + bottomPaddingPx
        // A margin once shown, so a top a pixel either side of the limit cannot flip the decision back and forth.
        val marginPx = if (lastEveningShown) with(LocalDensity.current) { EVENING_LIMIT_MARGIN.toPx() } else 0f
        val eveningShown = pieces.isNotEmpty() && eveningLineFits(
            partsTopInRootPx + linesBelowHeightPx,
            maxOf(leftHeightPx, (pieces.size * pieceHeightPx).toFloat()),
            bottomPaddingPx,
            eveningLineBottomLimitInRootPx,
        ) && eveningStackFitsBelow(infoBottomInRootPx, heightWithStackPx, eveningLineTopLimitInRootPx?.let { it - marginPx })
        SideEffect { lastEveningShown = eveningShown }
        val shownSundown = sundownLine.takeIf { eveningShown }
        val shownBackBy = backByLine.takeIf { eveningShown }
        // Each evening line's own visibility, so the right part keeps a leaving line's width until its exit has run. Without it the
        // part dropped to no width the moment the lines went, and the pieces still animating out wrapped a character a line,
        // spiking the card's height for the length of the exit (caught by HudSundownLineGrowTest, CI run 38259665051).
        val sundownVisibility = remember { MutableTransitionState(shownSundown != null) }
        val backByVisibility = remember { MutableTransitionState(shownBackBy != null) }
        SideEffect {
            sundownVisibility.targetState = shownSundown != null
            backByVisibility.targetState = shownBackBy != null
        }
        val lastSundown = rememberLastShown(shownSundown)
        val lastBackBy = rememberLastShown(shownBackBy)
        val holdSundown = shownSundown ?: lastSundown.takeIf { sundownVisibility.currentState || !sundownVisibility.isIdle }
        val holdBackBy = shownBackBy ?: lastBackBy.takeIf { backByVisibility.currentState || !backByVisibility.isIdle }
        val fit = measuredNavigationInfoFit(readout, partsPx, metrics, holdSundown, holdBackBy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // RECORD -936 (the owner: "Yes, slightly tighter (Recommended)"): 4 dp at the sides; RECORD -943: 2 dp at the top.
                .padding(start = CARD_SIDE_PADDING, end = CARD_SIDE_PADDING, top = CARD_TOP_PADDING, bottom = Spacing.xs),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoParts(
                    readout = readout,
                    fit = fit,
                    metrics = metrics,
                    sundownLine = shownSundown,
                    backByLine = shownBackBy,
                    sundownVisibility = sundownVisibility,
                    backByVisibility = backByVisibility,
                    reserveRight = eveningPieces(holdSundown, holdBackBy).isNotEmpty(),
                    onLeftHeight = { leftHeightPx = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { partsTopInRootPx = it.positionInRoot().y },
                )
                Column(modifier = Modifier.onGloballyPositioned { linesBelowHeightPx = it.size.height.toFloat() }) {
                    CardRouteAndStatusLines(readout, partsPx, onRetryRoute)
                }
            }
            // The X at the card's right edge, top-aligned beside the parts and the lines under them, its own 48 dp touch box.
            BouncingIconButton(
                onClick = onExit,
                modifier = Modifier.testTag(NAVIGATION_HUD_EXIT_TAG),
            ) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Stop navigating")
            }
        }
    }
}

/**
 * Dispatch 2026-10-12-08: the navigation info's fixed parts, in pixels at this density and font. [fixedPx] is what the info takes
 * from its width before the two parts: its side padding and the X.
 */
internal class InfoMetrics(
    val fixedPx: Int,
    val dividerPx: Int,
    val gapPx: Int,
    val arrowPx: Int,
)

@Composable
private fun rememberInfoMetrics(): InfoMetrics = with(LocalDensity.current) {
    val padding = 2 * CARD_SIDE_PADDING.roundToPx()
    InfoMetrics(
        fixedPx = padding + HUD_EXIT_TOUCH_SIZE.roundToPx(),
        dividerPx = CARD_DIVIDER_WIDTH.roundToPx() + 2 * CARD_DIVIDER_GAP.roundToPx(),
        gapPx = Spacing.xs.roundToPx(),
        arrowPx = COMPASS_ICON_SIZE.roundToPx(),
    )
}

/**
 * RECORD -945 (the owner: "Both (Recommended)", a bigger distance): about twice the size of the other text, as in the mockup:
 * headlineSmall's 24 sp against labelMedium's 12, bold, tabular figures. Was titleMedium bold, one step up.
 */
@Composable
private fun cardFigureStyle(): TextStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum")

/**
 * The figure the left part draws: the route message moves to its own line under the parts (see [CardRouteAndStatusLines]);
 * with [short], "within 16 ft" reads "≤ 16 ft" (RECORD -936).
 */
private fun columnFigure(readout: NavigationHudReadout, short: Boolean = false): String = when {
    readout.distanceText == ROUTE_UNAVAILABLE_TEXT -> NO_HEADING_TEXT
    short -> distanceShortForm(readout.distanceText) ?: readout.distanceText
    else -> readout.distanceText
}

@Composable
private fun measuredNavigationInfoFit(
    readout: NavigationHudReadout,
    partsPx: Int,
    metrics: InfoMetrics,
    sundownLine: String?,
    backByLine: String?,
): NavigationInfoFit {
    val measurer = rememberTextMeasurer()
    val small = MaterialTheme.typography.labelMedium
    val evening = small.copy(fontFeatureSettings = "tnum")
    val figureStyle = cardFigureStyle()
    fun widthOf(text: String, style: TextStyle) = measurer.measure(text, style, maxLines = 1, softWrap = false).size.width
    val turn = readout.targetText
    return navigationInfoFit(
        NavigationInfoWidths(
            availablePx = partsPx,
            dividerPx = metrics.dividerPx,
            gapPx = metrics.gapPx,
            arrowPx = metrics.arrowPx,
            figurePx = widthOf(columnFigure(readout), figureStyle),
            figureShortPx = distanceShortForm(readout.distanceText)?.let { widthOf(it, figureStyle) },
            kindPx = readout.distanceKindText?.let { widthOf(it, small) },
            turnWholePx = turn.takeIf { it.isNotEmpty() }?.let { widthOf(it, small) },
            turnShortPx = turnShortForm(turn)?.let { widthOf(it, small) },
            headingPx = if (readout.secondRowShown) widthOf(readout.headingText, small.copy(fontFeatureSettings = "tnum")) else 0,
            headingIsStatus = !readout.headingIsReading,
            headingIconPx = metrics.arrowPx,
            eveningPiecePx = eveningPieces(sundownLine, backByLine).maxOfOrNull { widthOf(it, evening) },
        ),
        turnWhole = turn,
        turnShort = turnShortForm(turn),
    ).let { fit -> if (fit.overflows) fit.also { android.util.Log.w(CARD_LOG_TAG, "navigation info overflows at $partsPx px: $fit") } else fit }
}

/**
 * The left part, the full-height divider and the right part (dispatch 2026-10-12-08). The right part is as wide as the widest
 * evening piece ([NavigationInfoFit.rightPx]); the left takes the rest, and the divider is measured last, at the taller part's
 * height, so it always runs the parts' full height (RECORD -945).
 */
@Composable
private fun InfoParts(
    readout: NavigationHudReadout,
    fit: NavigationInfoFit,
    metrics: InfoMetrics,
    sundownLine: String?,
    backByLine: String?,
    sundownVisibility: MutableTransitionState<Boolean>,
    backByVisibility: MutableTransitionState<Boolean>,
    /** Whether the right part keeps its width: an evening line shown, or one still leaving. */
    reserveRight: Boolean,
    onLeftHeight: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dividerColor = if (LocalForagerDarkTheme.current) MAP_ICON_STACK_BORDER_COLOR_DARK else MAP_ICON_STACK_BORDER_COLOR_LIGHT
    Layout(
        modifier = modifier,
        contents = listOf(
            { LeftPart(readout, fit, Modifier.onGloballyPositioned { onLeftHeight(it.size.height.toFloat()) }) },
            {
                Box(
                    modifier = Modifier
                        .padding(horizontal = CARD_DIVIDER_GAP)
                        .width(CARD_DIVIDER_WIDTH)
                        .background(dividerColor)
                        .testTag(NAVIGATION_HUD_DIVIDER_TAG),
                )
            },
            { EveningStack(sundownLine, backByLine, sundownVisibility, backByVisibility) },
        ),
    ) { (left, divider, right), constraints ->
        val total = constraints.maxWidth
        // The right part keeps the widest piece's width; where even that leaves the left part short (an overflow the fit
        // reports), the left part stays whole and the pieces wrap in what is left, never cut (RECORD -941).
        val wantRight = if (reserveRight) fit.rightPx else 0
        val rightWidth = if (fit.overflows) minOf(wantRight, maxOf(0, total - metrics.dividerPx - fit.leftPx)) else wantRight
        val leftWidth = maxOf(0, total - metrics.dividerPx - rightWidth)
        val leftPlaceable = left.single().measure(constraints.copy(minWidth = leftWidth, maxWidth = leftWidth, minHeight = 0))
        val rightPlaceable = right.single().measure(constraints.copy(minWidth = rightWidth, maxWidth = rightWidth, minHeight = 0))
        val height = maxOf(leftPlaceable.height, rightPlaceable.height)
        val dividerPlaceable = divider.single().measure(
            constraints.copy(minWidth = metrics.dividerPx, maxWidth = metrics.dividerPx, minHeight = height, maxHeight = height),
        )
        layout(total, height) {
            leftPlaceable.place(0, 0)
            dividerPlaceable.place(leftWidth, 0)
            rightPlaceable.place(leftWidth + metrics.dividerPx, 0)
        }
    }
}

/**
 * The left part, each line centred (RECORD -945): the distance; the turn words, the needle, the north arrow and the heading on one
 * line, the two arrows neighbours (RECORD -950); the kind word, only when there is one.
 */
@Composable
private fun LeftPart(readout: NavigationHudReadout, fit: NavigationInfoFit, modifier: Modifier = Modifier) {
    val small = MaterialTheme.typography.labelMedium
    val distanceColor = if (readout.distanceDeEmphasised) LocalContentColor.current.copy(alpha = 0.5f) else LocalContentColor.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        WordSwap(text = columnFigure(readout, fit.figureShort)) { shown ->
            Text(
                text = shown,
                style = cardFigureStyle(),
                color = distanceColor,
                maxLines = 1,
                softWrap = false,
                // The route message's own tag is on its line when it moves there; the dash here is the left part's.
                modifier = if (readout.distanceText == ROUTE_UNAVAILABLE_TEXT) Modifier else Modifier.testTag(NAVIGATION_HUD_DISTANCE_TAG),
            )
        }
        Row(
            modifier = Modifier.heightIn(min = COMPASS_ICON_SIZE),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Always composed, empty when there is no turn to word (the approach, no compass, no aim), as before. Its gap is its
            // own end padding, there only with words, so an empty slot adds no gap (what [navigationInfoFit] measures).
            WordSwap(text = fit.turnText, contentAlignment = Alignment.CenterEnd) { shown ->
                Text(
                    text = shown,
                    style = small,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(end = if (shown.isEmpty()) 0.dp else Spacing.xs).testTag(NAVIGATION_HUD_TARGET_TAG),
                )
            }
            // The needle stays whatever the words beside it do (T11 fixes), dimmed when there is nothing to point at. Its tag is
            // on the unrotated square it is laid out in, so its bounds are where it sits, not its rotated drawing's box.
            Box(modifier = Modifier.size(COMPASS_ICON_SIZE).testTag(NAVIGATION_HUD_NEEDLE_TAG)) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = null,
                    tint = if (readout.targetArrowDegrees != null) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f),
                    modifier = Modifier.size(COMPASS_ICON_SIZE).rotate(readout.targetArrowDegrees ?: 0f),
                )
            }
            // The two arrows neighbours (RECORD -950: "The arrows being side by side is a compass visual"): one small gap.
            Spacer(Modifier.width(NAVIGATION_ARROWS_GAP))
            if (fit.headingAsIcon) {
                // RECORD -934: a compass status whose words do not fit takes RECORD -735's crossed-out compass, its words kept as
                // the icon's description for a screen reader.
                Icon(
                    imageVector = Icons.Filled.ExploreOff,
                    contentDescription = readout.headingText,
                    modifier = Modifier.size(COMPASS_ICON_SIZE).testTag(NAVIGATION_HUD_NORTH_ARROW_TAG),
                )
            } else {
                // The north compass, right after the needle (RECORD -950): rotated by minus the heading, as before.
                Box(modifier = Modifier.size(COMPASS_ICON_SIZE).testTag(NAVIGATION_HUD_NORTH_ARROW_TAG)) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(COMPASS_ICON_SIZE).rotate(readout.northArrowDegrees ?: 0f),
                    )
                }
                // Motion Part 2, item 6 (RECORD -651: "Numbers instant, words fade"). With no fix and only the dash to say, the
                // heading is not drawn (NavigationHudReadout.secondRowShown).
                if (readout.secondRowShown) {
                    Spacer(Modifier.width(Spacing.xs))
                    WordSwap(text = readout.headingText) { shown ->
                        Text(
                            text = shown,
                            style = small.copy(fontFeatureSettings = "tnum"),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag(NAVIGATION_HUD_HEADING_TAG),
                        )
                    }
                }
            }
        }
        // RECORD -945 ("by trail under figure"): the kind word, a small line of its own under the turn line, only when there is one.
        readout.distanceKindText?.let { kind -> DistanceKindText(kind, distanceColor) }
    }
}

/**
 * The evening lines broken at their " · " joins into short pieces, the sundown line's first, then Back by's (RECORD -941: "Sunset
 * 12:41 AM", "in 2 h 28 min", "dark 1:13", "Back by …").
 */
internal fun eveningPieces(sundownLine: String?, backByLine: String?): List<String> =
    listOfNotNull(sundownLine, backByLine).flatMap { line -> line.split(" · ").map(String::trim).filter(String::isNotEmpty) }

/**
 * RECORD -941: the evening lines stacked in the right part, one piece per line, centred (RECORD -945), never ending in "…" (a piece
 * wider than the part wraps; the fit reserves the widest piece's width, so it only wraps where the fit reports an overflow). Each
 * line keeps its own tag on a column whose text is the whole line, so it reads as it did; each piece carries
 * [NAVIGATION_HUD_EVENING_PIECE_TAG]. Each line fades and grows as before (RECORD -672), or only fades under reduced motion.
 */
@Composable
private fun EveningStack(
    sundownLine: String?,
    backByLine: String?,
    sundownVisibility: MutableTransitionState<Boolean>,
    backByVisibility: MutableTransitionState<Boolean>,
) {
    val reduceMotion = LocalReduceMotion.current
    val lineFade = MotionTokens.mapPopUpFadeSpec<Float>()
    val lineGrow = MotionTokens.mapPopUpGrowSpec<IntSize>()
    val enter = if (reduceMotion) fadeIn(animationSpec = lineFade) else fadeIn(animationSpec = lineFade) + expandVertically(animationSpec = lineGrow, expandFrom = Alignment.Top)
    val exit = if (reduceMotion) fadeOut(animationSpec = lineFade) else fadeOut(animationSpec = lineFade) + shrinkVertically(animationSpec = lineGrow, shrinkTowards = Alignment.Top)
    val style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        for ((line, tag, visibility) in listOf(
            Triple(sundownLine, NAVIGATION_HUD_SUNDOWN_LINE_TAG, sundownVisibility),
            Triple(backByLine, NAVIGATION_HUD_BACK_BY_LINE_TAG, backByVisibility),
        )) {
            val shownLine = rememberLastShown(line)
            AnimatedVisibility(visibleState = visibility, enter = enter, exit = exit, label = "hudEveningStack") {
                shownLine?.let { whole ->
                    Column(
                        modifier = Modifier.fillMaxWidth().semantics { text = AnnotatedString(whole) }.testTag(tag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        eveningPieces(whole, null).forEach { piece ->
                            WordSwap(text = piece) { shown ->
                                Text(text = shown, style = style, modifier = Modifier.testTag(NAVIGATION_HUD_EVENING_PIECE_TAG))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The lines under the parts that say what is wrong or what else is true (RECORD -935: everything today's status line said, shown
 * only when it has something to say), in [widthPx]:
 * - the route message, when the route home cannot be calculated, with "Try again" beside it when recomputing could change that
 *   (RECORD -932, step 3; in landscape too, RECORD -934). It carries [NAVIGATION_HUD_DISTANCE_TAG], the large slot's tag, since
 *   it is the large slot's words moved here; the left part shows a dash meanwhile. Where even "No route" cannot sit beside
 *   "Try again" (font 2.0 in a narrow window), "Try again" takes its own line under it, as before;
 * - the status line. Since dispatch 2026-10-12-08 the kind word is a line of its own in the left part (RECORD -945), so it no
 *   longer leads this line; and what the compass strip already says about the position (no fix, approximate, last seen) is left
 *   out here, so it shows once, on the strip (RECORD -951).
 */
@Composable
private fun CardRouteAndStatusLines(
    readout: NavigationHudReadout,
    widthPx: Int,
    onRetryRoute: () -> Unit,
) {
    val statusStyle = MaterialTheme.typography.labelMedium
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    fun widthOf(text: String, style: TextStyle) = measurer.measure(text, style, maxLines = 1, softWrap = false).size.width
    val gapPx = with(density) { Spacing.sm.roundToPx() }
    if (readout.distanceText == ROUTE_UNAVAILABLE_TEXT) {
        val retryPx = with(density) { (RETRY_ICON_SIZE + Spacing.xs).roundToPx() } + widthOf(ROUTE_RETRY_TEXT, retryTextStyle())
        val besideRoom = if (readout.routeRetryOffered) widthPx - retryPx - gapPx else widthPx
        val message = if (widthOf(ROUTE_UNAVAILABLE_TEXT, statusStyle) <= besideRoom) ROUTE_UNAVAILABLE_TEXT else ROUTE_UNAVAILABLE_SHORT_TEXT
        val beside = !readout.routeRetryOffered || retryFitsBeside(widthPx, retryPx, gapPx, widthOf(ROUTE_UNAVAILABLE_SHORT_TEXT, statusStyle))
        val messageText: @Composable () -> Unit = {
            WordSwap(text = if (beside) message else ROUTE_UNAVAILABLE_TEXT) { shown ->
                Text(
                    text = shown,
                    style = statusStyle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(NAVIGATION_HUD_DISTANCE_TAG),
                )
            }
        }
        if (beside) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(modifier = Modifier.weight(1f, fill = false)) { messageText() }
                if (readout.routeRetryOffered) RouteRetryRow(onRetryRoute)
            }
        } else {
            messageText()
            RouteRetryRow(onRetryRoute)
        }
    }
    val separatorPx = widthOf("·", statusStyle) + 2 * with(density) { Spacing.xs.roundToPx() }
    val statusFit = statusLineFit(readout, kindInStatus = false, maxPx = widthPx, leadSeparatorPx = separatorPx) { widthOf(it, statusStyle) }
    val status = if (readout.statusIsPositionStatus) "" else statusFit.status
    if (status.isEmpty()) {
        // Nothing to say: no line (RECORD -935, "only when it has something to say"). An empty, zero-height node keeps the
        // status's tag, reading "", so "the status line is empty" stays a statement a test can check.
        Text(text = "", style = statusStyle, modifier = Modifier.height(0.dp).testTag(NAVIGATION_HUD_STATUS_TAG))
    } else {
        WordSwap(text = status) { shown ->
            Text(
                text = shown,
                style = statusStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(NAVIGATION_HUD_STATUS_TAG),
            )
        }
    }
}

/**
 * A short label and its reading on one line, a space apart (dispatch 2026-09-28-677; the owner, RECORD -656: "heading and
 * altitude labelled"): "Facing 123° SE", "Alt 1352 ft". [label] `null` draws the reading alone, for a status that names
 * itself. Shared by the navigation display's second row and the compass strip, so the two label alike. The label is its own
 * text node ([NAVIGATION_LABEL_TAG]), so the reading's node, and every test that reads it, holds the reading alone.
 */
@Composable
internal fun LabelledReadout(
    label: String?,
    /** The label's style: labelMedium on the navigation display; the strip's 14 sp readout style there (RECORD -728). */
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    reading: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        if (label != null) {
            Text(
                text = label,
                style = labelStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(NAVIGATION_LABEL_TAG),
            )
        }
        reading()
    }
}


/** The X's touch box: Material's icon button at its 48 dp minimum interactive size. */
private val HUD_EXIT_TOUCH_SIZE = 48.dp

/** What the large figure measures, "by trail" or "straight", in the status line's type (dispatch -677; RECORD -713). */
@Composable
private fun DistanceKindText(kind: String, color: Color, modifier: Modifier = Modifier) {
    WordSwap(text = kind, modifier = modifier) { shown ->
        Text(
            text = shown,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.testTag(NAVIGATION_HUD_DISTANCE_KIND_TAG),
        )
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
            .clickableWithShapedPress(role = Role.Button, onClickLabel = ROUTE_RETRY_TEXT, onClick = onRetryRoute)
            .testTag(NAVIGATION_HUD_RETRY_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = primary, modifier = Modifier.size(RETRY_ICON_SIZE))
        Text(
            text = ROUTE_RETRY_TEXT,
            style = retryTextStyle(),
            color = primary,
            maxLines = 1,
        )
    }
}

/** "Try again"'s type: the primary colour comes from the row; measured with this by the landscape layout (T11 fixes). */
@Composable
private fun retryTextStyle(): TextStyle = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline)

private val COMPASS_ICON_SIZE = 22.dp

/** RECORD -955: the evening lines, once shown, stay while the card's top is within this of its limit (no flip-flop). */
private val EVENING_LIMIT_MARGIN = 4.dp

/** Dispatch 2026-10-12-08: the gap between the turn needle and the north arrow, neighbours on one line (RECORD -950). */
private val NAVIGATION_ARROWS_GAP = Spacing.xs

/** Dispatch 2026-10-12-06: the card's column dividers, thin lines with this gap either side (6 dp, RECORD -936's tighter spacing). */
private val CARD_DIVIDER_WIDTH = 1.dp
private val CARD_DIVIDER_GAP = 6.dp
/** RECORD -936 (the owner: "Yes, slightly tighter (Recommended)"): the card's own side padding. */
private val CARD_SIDE_PADDING = Spacing.xs
/** RECORD -943: the card's top padding, under the pill. */
private val CARD_TOP_PADDING = 2.dp
private const val CARD_LOG_TAG = "NavigationCard"
private val RETRY_ICON_SIZE = 18.dp
private val RETRY_ROW_MIN_HEIGHT = 48.dp
private const val AGE_TICK_MILLIS = 1_000L
