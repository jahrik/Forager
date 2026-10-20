package com.zynergylabs.forager.app.ui.availability

import androidx.compose.runtime.MutableIntState

/**
 * The layout limits [CompactMapTab]'s children share, carried as one object so a spacing fix touches this file and the one child
 * that draws it (UI split Phase 2.3, dispatch 2026-10-19-35; survey `docs/audits/2026-10-18-ui-split-survey.md` on branch
 * `ui-split-survey`, section 6.3). The cluster's own bounds and measures are not here: they already live in
 * [MapIconClusterState], which every child reads.
 *
 * **A view over State objects, never a copy** (the pattern of [com.zynergylabs.forager.app.ui.map.SightingsMapState]).
 * [CompactMapTab] still remembers the three measured sizes where it always did, in its map branch, with the same start value and in
 * the same order, and remembers this holder unkeyed straight after them, in the same branch, so the holder lives exactly as long as
 * the States it wraps. Each read here is a getter on that State and each write a setter on it. A value copied in would be stale the
 * moment the State changed: the trap the frame's extraction found (`docs/audits/2026-09-26-split-stage-g-completion-report.md`).
 *
 * **Where the reads happen is unchanged.** The body reads the strip's height where it always did and hands the children plain
 * values; only [CompactMapTopStrip] (which writes all three) and [CompactMapNavigationDisplay] (which reads the landscape two) take
 * the holder, and each reads or writes through it at the same place it used the State before.
 */
internal class CompactMapLayoutState(
    private val compassStripHeightState: MutableIntState,
    private val landscapeStripHeightState: MutableIntState,
    private val landscapeStripWidthState: MutableIntState,
) {
    /**
     * The portrait strip's measured height, written by [CompactMapTopStrip] while not navigating (0 while navigating, in a
     * landscape window, and before its first layout). Not the one-line clearance, which is one text line's height.
     */
    var compassStripHeightPx: Int
        get() = compassStripHeightState.intValue
        set(value) { compassStripHeightState.intValue = value }

    /** RECORD -729: the strip's measured height in a landscape window (the search bar is given it); 0 elsewhere. */
    var landscapeStripHeightPx: Int
        get() = landscapeStripHeightState.intValue
        set(value) { landscapeStripHeightState.intValue = value }

    /** RECORD -732: and its measured width (the scaffold gives the bar the rest of the room); 0 elsewhere. */
    var landscapeStripWidthPx: Int
        get() = landscapeStripWidthState.intValue
        set(value) { landscapeStripWidthState.intValue = value }
}
