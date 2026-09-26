package com.zynergylabs.forager.app.ui.availability

// Split-AvailabilityScreen Stage F: the map's overlays and dialogs, moved verbatim out of
// AvailabilityScreen.kt. Three blocks, lines of the file as of 3bd0efe: 3076-3087
// (PendingMapAction, its KDoc moved as it was), 5008-5114 (TripDatePickerDialog,
// WaypointNameDialog) and 5345-5673 (OBSERVATION_DATE_FORMAT,
// OBSERVATION_BUBBLE_BASE_DIRECTION_DEG, OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH,
// OBSERVATION_BUBBLE_ARROW_BASE_HALF_WIDTH, AnchoredAtScreenPoint, ObservationBubble,
// TaxonMapFilterChip, MapMessage). Same package as Stages A to E, so every same-package reference
// resolves unchanged. Pure move: no signature, name or body changed. Seven widenings, private ->
// internal: PendingMapAction, TripDatePickerDialog, WaypointNameDialog, AnchoredAtScreenPoint,
// ObservationBubble, TaxonMapFilterChip and MapMessage, each used by both map tabs, CompactMapTab
// (AvailabilityCompactMapUi.kt) and MapTab (AvailabilityWideLayoutUi.kt). No symbol left behind is
// reached from here. Seam F (the wide layout) was released by the owner for this split, as recorded
// in the Understory amendment merged in #130.

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorDark
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorLight
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.theme.Bark
import com.zynergylabs.forager.app.ui.theme.LocalForagerDarkTheme
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The map tab: the map itself takes the whole content area apart from the foraging-areas toggle
 * and detail below it, which are bounded so they can't repeat the starvation this layout exists
 * to fix.
 *
 * Which of the three-way menu's options is currently mid-pick, between the menu closing and the
 * centre-pin picker confirming — see [MapTab]/[CompactMapTab]'s own doc comments. There is no
 * "none, but the picker is still showing" state: the picker overlay's own visibility is driven by
 * this being non-null, not a separate flag, so the two can never disagree about whether a pick is
 * in progress.
 */
internal enum class PendingMapAction { PLAN_TRIP, LOG_FIND, DROP_WAYPOINT }

/**
 * Confirms a date and name for a trip pin placed via [com.zynergylabs.forager.app.ui.map.CentrePinLocationPicker].
 *
 * The date is restricted to today-or-later — planning a trip in the past makes no sense, per the
 * user's own framing of this feature. [SavePlannedTripUseCase][com.zynergylabs.forager.app.domain.SavePlannedTripUseCase]
 * enforces the same floor independently; this is the UI convenience, not the invariant itself.
 *
 * The name field starts pre-filled with [defaultTripName] and stays freely editable; the confirm
 * button disables on a blank name so the "name is never blank" invariant
 * ([PlannedTrip.name][com.zynergylabs.forager.app.domain.model.PlannedTrip.name]) can't be violated from this
 * dialog, mirroring `SavePlannedTripUseCase`'s own `require` for the same reason the date floor is
 * mirrored there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDatePickerDialog(
    defaultName: String,
    onConfirm: (LocalDate, String) -> Unit,
    onDismiss: () -> Unit,
) {
    // DatePicker works in UTC-midnight epoch millis regardless of device time zone, so both the
    // floor and the confirmed selection are converted through UTC to stay consistent with it.
    val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayUtcMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtcMillis
        },
    )
    var name by remember { mutableStateOf(defaultName) }
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                // Guards the "name is never blank" invariant from this dialog — see this
                // function's own doc comment.
                enabled = name.isNotBlank(),
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(date, name)
                },
            ) { Text("Plan trip") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        Column {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Trip name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
            )
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Confirms a name for a waypoint placed via [com.zynergylabs.forager.app.ui.map.CentrePinLocationPicker] —
 * [TripDatePickerDialog] without the date, since a waypoint has none ([Waypoint] carries no target date, unlike
 * [PlannedTrip]). Same blank-name guard, mirroring
 * [CreateWaypointUseCase][com.zynergylabs.forager.app.domain.CreateWaypointUseCase]'s own `require`.
 */
@Composable
internal fun WaypointNameDialog(defaultName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(defaultName) }
    // usePlatformDefaultWidth = false, matching M3's own DatePickerDialog default rather than
    // AlertDialog's (true): an AlertDialog/plain Dialog with an OutlinedTextField as its sole
    // focusable content, reached through the compact scaffold (ModalNavigationDrawer plus its own
    // BackHandlers), never settles under this Robolectric test setup — real, reproduced
    // AppNotIdleException, isolated by removing pieces one at a time (the text field alone is fine;
    // AddActionTile -> this dialog alone is fine; TripDatePickerDialog's own text field, reached
    // through this exact same scaffold, is fine). The one structural difference from
    // TripDatePickerDialog left once AlertDialog vs. plain Dialog was ruled out is this property.
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(Spacing.md),
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text("Name this waypoint", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Waypoint name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name) }) { Text("Drop waypoint") }
                }
            }
        }
    }
}

private val OBSERVATION_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/**
 * The direction [ObservationBubble] sits from the dot it names, measured clockwise from up
 * (0°/up, 90°/right, 180°/down, 270°/left), in the *map's own north-up frame* — not screen space.
 * [AnchoredAtScreenPoint] rotates this by the map's live bearing to get where the bubble actually
 * lands on screen, which is the whole point of it being a map-relative constant rather than a
 * screen-relative one: up-and-left of the dot at bearing 0° (the same corner this composable's
 * predecessor hard-coded — see its own git history) stays up-and-left *of the dot*, not up-and-left
 * *of the screen*, as the map turns underneath it.
 */
private const val OBSERVATION_BUBBLE_BASE_DIRECTION_DEG = 315f

/**
 * How far the arrow's own tip extends past [ObservationBubble]'s own edge, toward the dot it
 * names. Purely a visual "sticks out and reads as pointed" clearance — [rectEdgeIntersection]
 * already guarantees the tip's *base* sits exactly on the bubble's boundary before this is added.
 */
private val OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH = Spacing.md

/** Half the width of the arrow's own base, straddling the point [rectEdgeIntersection] returns. */
private val OBSERVATION_BUBBLE_ARROW_BASE_HALF_WIDTH = Spacing.xs

/**
 * Places [content] — [ObservationBubble] itself, handed the exact angle (screen-space, clockwise
 * from up) its own arrow should point back along — so that arrow's tip lands precisely on
 * [anchorPx], the tapped sighting's live screen position in the same px coordinate space
 * [MapSlot]'s own `onSightingTap` reports (see that callback's doc comment). Replaces an earlier
 * revision that hard-coded a squared-off bubble corner as an implied arrow at a fixed "5 o'clock"
 * offset from the dot — per the project owner's own follow-up call, that read ambiguously in a
 * dense cluster and didn't survive map rotation, so this version draws a real, explicit arrow
 * ([ObservationBubble]'s own [Canvas]) and keeps its tip glued to the dot's exact center rather
 * than an offset point near it.
 *
 * [bearingDeg] is what makes this "remain static on the map, but rotate with the map orientation"
 * — the project owner's own framing. [OBSERVATION_BUBBLE_BASE_DIRECTION_DEG] fixes where the
 * bubble sits *relative to the dot, in the map's own north-up frame*; rotating that by the map's
 * live bearing below is what keeps the bubble's placement correct — up-and-left of the dot, not
 * up-and-left of the screen — as the map turns, the same way a label pinned to the map surface
 * itself would. [ObservationBubble]'s own text never rotates: only the angle passed to it and the
 * bubble's own screen position change, so it stays legible through a rotate gesture rather than
 * turning upside down at some bearings — the project owner's own explicit constraint ("as long as
 * it's not spinning with the map and can read it legibly while rotating the map, and stays
 * pointing directly on the observation dot").
 *
 * A custom [Layout], not a plain [Box] + `Modifier.offset`, for the same reason as before: placing
 * the bubble so its arrow tip lands exactly on [anchorPx] needs the bubble's own measured size,
 * which isn't known until after it's laid out. Reports its own occupied size as the full incoming
 * [Constraints] (the whole map [Box]), with the child placed freely inside at the computed point,
 * clamped to stay on-screen — placing a child outside its parent's own declared bounds would leave
 * it undependably hit-testable. Clamping can still pull the bubble away from the angle-exact spot
 * near a screen edge, same limitation the predecessor's own corner-anchoring had — the arrow drawn
 * from wherever the bubble actually lands is still geometrically consistent with itself, just no
 * longer touching the dot exactly in that corner case.
 *
 * [minY] raises the lowest the bubble's own top edge may land — [CompactMapTab]'s own call site
 * passes `compassStripClearance`, a real measurement of the compass strip's own type style, so a
 * marker tapped near the map's top edge never anchors a bubble underneath that strip's full-width
 * touch-interception band (the exact hazard this file's own "Known pitfalls" precedent already
 * documents, and the reason the strip is composed after the map's own content in the first place).
 * A plain [Dp] parameter, not a lambda: an earlier version of this call site read the strip's real
 * `onGloballyPositioned` layout height back through a `mutableStateOf`, and separately a version
 * of this parameter itself was made a `() -> Int` lambda — both were tried while chasing a real,
 * reproducible regression (`AvailabilityScreenMapIconStackTest`'s "tapping elsewhere on the map
 * dismisses the observation bubble" test, bisected line by line) and neither survived: this
 * signature and a one-time, non-reactive measurement at the call site is the configuration proven
 * not to reproduce it. [MapTab] has no such strip and passes `0`.
 */
@Composable
internal fun AnchoredAtScreenPoint(
    anchorPx: Offset,
    bearingDeg: Float,
    minY: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable (arrowAngleDeg: Float) -> Unit,
) {
    // The direction FROM the dot TO the bubble, in screen space: the map-relative base direction,
    // rotated backward by the camera's own clockwise rotation — the map (and everything drawn
    // relative to it) appears to turn counter-clockwise on screen as the camera turns clockwise, so
    // subtracting bearingDeg is what keeps this pinned to the dot's own frame rather than the
    // screen's.
    val screenDirectionFromDotDeg = (OBSERVATION_BUBBLE_BASE_DIRECTION_DEG - bearingDeg).mod(360f)
    // The arrow's own direction: from the bubble back toward the dot, the exact opposite of where
    // the bubble sits relative to it. This is the one value ObservationBubble needs to draw an
    // arrow consistent with wherever this Layout ends up placing it.
    val arrowAngleDeg = (screenDirectionFromDotDeg + 180f).mod(360f)
    Layout(content = { content(arrowAngleDeg) }, modifier = modifier) { measurables, constraints ->
        val minYPx = minY.roundToPx()
        // ObservationBubble's own measured size already includes OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH
        // of reserved margin on every side (its own Modifier.padding — see that composable's doc
        // comment), so the *card's* own half-extents for rectEdgeIntersection are this placeable's,
        // shrunk back down by that same margin — otherwise this would compute where the reserved
        // margin's own outer edge sits, not the visible card's, and the tip would land short of
        // anchorPx by one tail length.
        val placeable = measurables.first().measure(Constraints())
        val tailPx = OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH.toPx()
        val cardHalfWidth = placeable.width / 2f - tailPx
        val cardHalfHeight = placeable.height / 2f - tailPx
        // Where the card's own boundary sits, and how far past it the arrow's tip extends toward
        // the dot — see rectEdgeIntersection's own doc comment. The ideal (pre-clamp) placeable
        // center is anchorPx walked backward along that same tip vector, so placing the placeable
        // there makes the tip land exactly on anchorPx.
        val edgePoint = rectEdgeIntersection(cardHalfWidth, cardHalfHeight, arrowAngleDeg)
        val radians = Math.toRadians(arrowAngleDeg.toDouble())
        val tipOffsetFromCenter = Offset(
            edgePoint.x + sin(radians).toFloat() * tailPx,
            edgePoint.y - cos(radians).toFloat() * tailPx,
        )
        val idealCenter = anchorPx - tipOffsetFromCenter
        val halfWidth = placeable.width / 2f
        val halfHeight = placeable.height / 2f
        val x = (idealCenter.x - halfWidth)
            .roundToInt()
            .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
        val y = (idealCenter.y - halfHeight)
            .roundToInt()
            .coerceIn(minYPx, (constraints.maxHeight - placeable.height).coerceAtLeast(minYPx))
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(x, y)
        }
    }
}

/**
 * Tapping a sighting dot's own detail — species name and observed date, the two facts
 * [SightingsMap]'s doc comment describes this as rebuilding from the vendor-native
 * title/snippet popup MapLibre's style-layer geometry has no equivalent for. "View on iNaturalist"
 * hands off to [launchINaturalistObservation] rather than showing anything about the observation
 * this app doesn't already have cached in [Sighting] — no confidence score, no candidate species
 * list, nothing this app would need to fetch or judge itself.
 *
 * A small floating bubble over the map, not a modal [AlertDialog] (this composable's first form,
 * replaced after hardware feedback that a blocking dialog for something this minor "got in the
 * way of the UX"). No scrim, no window of its own: it sits in the same [Box] as the map and lets
 * every tap outside its own bounds fall straight through to the map beneath — dismissing itself is
 * the caller's job, wired to the map's plain [onTap] the same way `CompactMapTab`'s "tap to restore
 * chrome while fullscreen" already works, not something this composable can do on its own the way
 * [AlertDialog]'s `onDismissRequest` (a tap on the scrim, or system back) could.
 *
 * [arrowAngleDeg] — [AnchoredAtScreenPoint]'s own computed direction back toward the dot this
 * bubble names, screen-space, clockwise from up — drives a real, explicit triangular tail drawn by
 * [Modifier.drawBehind] rather than the earlier revision's single squared-off corner (an implied
 * arrow that only worked from one fixed relative position). Reserves
 * [OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH] of otherwise-invisible margin on every side via
 * [Modifier.padding] so the tail has room to protrude past the visible card's own rounded-rect
 * silhouette regardless of which side [arrowAngleDeg] currently points it out of — the same margin
 * [AnchoredAtScreenPoint] already accounts for when it places this composable, so the two agree on
 * where the tail's tip actually lands without any state passed between them: both independently
 * apply the identical [rectEdgeIntersection] formula to the same inputs. The card itself is a plain
 * [RoundedCornerShape] again (all four corners), not one squared — the tail is what reads as a
 * speech/notification bubble now, not an asymmetric corner.
 */
@Composable
internal fun ObservationBubble(
    sighting: Sighting,
    onViewOnINaturalist: () -> Unit,
    onDismiss: () -> Unit,
    arrowAngleDeg: Float,
    modifier: Modifier = Modifier,
) {
    // Same theme-aware, 80%-opacity fill as MapIconBar/MapModePicker/AddActionTile — one visual
    // language for every control floating over the map, per the project owner's own request to
    // bring this bubble in line with the rest rather than Material's own surfaceContainerHigh. The
    // arrow tail below is filled with this exact same colour so it reads as part of one continuous
    // shape with the card, not a separate decoration.
    val isDarkTheme = LocalForagerDarkTheme.current
    val fillColor = if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight
    Box(
        modifier = modifier
            // Drawn before padding is applied below, so this sees the full outer bounds (card plus
            // the reserved tail margin on every side) rather than the shrunken interior padding
            // leaves for the card — the same "background paints the full area, padding only moves
            // content" ordering Modifier.background(...).padding(...) already relies on elsewhere.
            .drawBehind {
                val tailPx = OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH.toPx()
                val baseHalfWidthPx = OBSERVATION_BUBBLE_ARROW_BASE_HALF_WIDTH.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val cardHalfWidth = center.x - tailPx
                val cardHalfHeight = center.y - tailPx
                val edgePoint = rectEdgeIntersection(cardHalfWidth, cardHalfHeight, arrowAngleDeg)
                val radians = Math.toRadians(arrowAngleDeg.toDouble())
                val dirX = sin(radians).toFloat()
                val dirY = -cos(radians).toFloat()
                val baseCenter = center + edgePoint
                val tip = Offset(baseCenter.x + dirX * tailPx, baseCenter.y + dirY * tailPx)
                // Perpendicular to the tip direction, so the tail's base straddles baseCenter along
                // whichever edge it actually sits on — correct for any angle without needing to
                // know which of the card's four edges that is.
                val base1 = Offset(baseCenter.x - dirY * baseHalfWidthPx, baseCenter.y + dirX * baseHalfWidthPx)
                val base2 = Offset(baseCenter.x + dirY * baseHalfWidthPx, baseCenter.y - dirX * baseHalfWidthPx)
                drawPath(
                    Path().apply {
                        moveTo(base1.x, base1.y)
                        lineTo(tip.x, tip.y)
                        lineTo(base2.x, base2.y)
                        close()
                    },
                    color = fillColor,
                )
            }
            .padding(OBSERVATION_BUBBLE_ARROW_TAIL_LENGTH),
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .testTag("observation-bubble")
                // Consumes its own taps via a plain pointerInput, not Modifier.clickable — this
                // backdrop isn't itself a button (no ripple, no accessibility click action to fake),
                // it only needs to swallow the gesture so it doesn't fall through to the map beneath.
                // The same "a Surface wins hit-testing over whatever's beneath it in a Box" fact
                // MapIconBar's own doc comment documents, relied on here rather than guarded against:
                // a tap on the bubble should never also count as the map's own "tap elsewhere" dismiss
                // gesture. The close icon and "View on iNaturalist" text below still get first crack at
                // any tap that actually lands on them, same as any nested clickable inside one of these.
                .pointerInput(Unit) { detectTapGestures {} },
            shape = RoundedCornerShape(20.dp),
            color = fillColor,
            contentColor = if (isDarkTheme) Color.White else Bark,
            shadowElevation = 6.dp,
            tonalElevation = 3.dp,
        ) {
            Row(
                // fillMaxWidth() matters here, not just padding: a Row that wraps to its own content's
                // size can't also give the Column below a meaningful weight(1f) — Compose measures a
                // weighted child against "remaining space" that only exists once the Row's own width is
                // bounded/definite. Without this, the Column collapsed to near zero and wrapped
                // "Chanterelle" one character per line (caught by this bubble's own interaction tests,
                // not visual review).
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.md, top = Spacing.sm, end = Spacing.xs, bottom = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        sighting.commonName ?: sighting.scientificName,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (sighting.commonName != null) {
                        Text(sighting.scientificName, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                    }
                    Row(
                        // fillMaxWidth so SpaceBetween has real room to push the link to the far
                        // right — same reasoning as the outer Row's own fillMaxWidth comment above.
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            sighting.observedOn?.format(OBSERVATION_DATE_FORMAT) ?: "Observation date unknown",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "View on iNaturalist",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable(onClick = onViewOnINaturalist)
                                .testTag("observation-bubble-view-on-inaturalist"),
                        )
                    }
                    Text(
                        accuracyLabel(sighting.positionalAccuracyMeters),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("observation-bubble-accuracy"),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp).testTag("observation-bubble-close"),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * The standing indicator that [MapTab]/[CompactMapTab] are showing "View on Map"'s filtered
 * sightings rather than every mapped one — CLAUDE.md: a partial/filtered result has to say so, not
 * render identically to the unfiltered view. [label] is `"<species> (<count>)"`, computed by each
 * caller from the same [Sighting] list the map actually draws (see [MapTab]'s own doc comment on
 * `mapTaxonFilterLabel` for why it isn't read from [AvailabilityForecast] directly). Same
 * theme-aware, 80%-opacity fill as [ObservationBubble]/`MapIconBar`/`MapModePicker` — one visual
 * language for every control floating over the map.
 */
@Composable
internal fun TaxonMapFilterChip(label: String, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val isDarkTheme = LocalForagerDarkTheme.current
    Surface(
        modifier = modifier.testTag("map-taxon-filter-chip"),
        shape = RoundedCornerShape(percent = 50),
        color = if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight,
        contentColor = if (isDarkTheme) Color.White else Bark,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = Spacing.md, end = Spacing.xs, top = Spacing.xs, bottom = Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Showing: $label", style = MaterialTheme.typography.labelMedium)
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(24.dp).testTag("map-taxon-filter-clear"),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Show all species", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
internal fun MapMessage(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
    )
}
