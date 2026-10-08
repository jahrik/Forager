package com.zynergylabs.forager.app.ui.availability

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.ui.log.ScreenEdge

/**
 * How wide the compass strip and the navigation display may be in the short-landscape Map tab without reaching the
 * search bar (dispatch 2026-09-28-685, Amendment 1, RECORD -694; the owner: "Stay beside the search bar, '…'
 * (Recommended)"). On the S22 at font scale 2.0 the strip (content-width) and the display (full width up to
 * [LANDSCAPE_HUD_MAX_WIDTH]) grew from the rail's side across the centre line and over the bar, and nothing bounded
 * them by it. Now they stay in the room between the rail-side controls edge and the bar's end; text that does not fit
 * there ends in "…" on one line, so nothing gets taller and the central third's clearance is unchanged.
 *
 * The bar's end is worked out the way the scaffold places the bar (`AvailabilityCompactScaffold`'s
 * `landscapeSearchWidth`): from the punch-hole-side controls edge, `min(LANDSCAPE_SEARCH_MAX_WIDTH, half the window
 * minus that edge's inset minus LANDSCAPE_SEARCH_CENTRE_GAP)`. Recomputed from the same constants rather than read from
 * the bar's measured size, because reading a measured position back through state for placement caused a bisected
 * regression on this screen (`AvailabilityCompactMapUi`'s note on `onGloballyPositioned`).
 *
 * The rejected alternative, placing both below the bar's measured height, would push the display into the window's
 * central third, whose 1.5 dp clearance on the S22 is the owner's to change.
 */
internal fun railSideRoomBesideSearchBar(windowWidth: Dp, searchSideInset: Dp, railSideInset: Dp): Dp {
    val barWidth = minOf(LANDSCAPE_SEARCH_MAX_WIDTH, windowWidth / 2 - searchSideInset - LANDSCAPE_SEARCH_CENTRE_GAP).coerceAtLeast(0.dp)
    return (windowWidth - railSideInset - searchSideInset - barWidth).coerceAtLeast(0.dp)
}

/**
 * Caps the width of what follows at [railSideRoomBesideSearchBar]. Goes **after** `padding(controlsPadding)` on a child
 * of the map's full-size Box, so the width it is offered is the window less both insets, from which the window is
 * recovered. With the search bar on the same side as the rail (no such phone is known), or no rail, [railEdge] or
 * [searchEdge] tell it so and it changes nothing.
 */
internal fun Modifier.besideLandscapeSearchBar(
    railEdge: ScreenEdge?,
    searchEdge: ScreenEdge?,
    controlsPadding: PaddingValues,
    layoutDirection: LayoutDirection,
): Modifier {
    if (railEdge == null || searchEdge == null || railEdge == searchEdge) return this
    val left = controlsPadding.calculateLeftPadding(layoutDirection)
    val right = controlsPadding.calculateRightPadding(layoutDirection)
    val (searchSideInset, railSideInset) = if (searchEdge == ScreenEdge.Left) left to right else right to left
    return layout { measurable, constraints ->
        if (!constraints.hasBoundedWidth) {
            val placeable = measurable.measure(constraints)
            return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        }
        val window = constraints.maxWidth.toDp() + searchSideInset + railSideInset
        val room = railSideRoomBesideSearchBar(window, searchSideInset, railSideInset).roundToPx()
        val maxWidth = minOf(constraints.maxWidth, room)
        val placeable = measurable.measure(constraints.copy(minWidth = minOf(constraints.minWidth, maxWidth), maxWidth = maxWidth))
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}
