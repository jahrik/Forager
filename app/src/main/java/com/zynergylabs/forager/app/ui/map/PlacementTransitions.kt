package com.zynergylabs.forager.app.ui.map

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.TransitionOptions

/**
 * The style's transition with MapLibre's symbol fade (placement transitions) turned off and
 * everything else as [current] has it (dispatch 2026-09-28-369 and its amendment 2026-09-28-370).
 * The duration and delay are the paint transitions the other layers run on (the fan-clarity fade
 * of the other markers to 80%, layer opacity changes), so they are kept, not set.
 *
 * Why the symbol fade is off: the fan's icons are a symbol layer whose source is pushed on every
 * animation frame, and MapLibre fades a newly placed symbol in over the placement transition, so the
 * icons dimmed, blinked against their circles and came back about 260 ms after the fan had stopped
 * (Part 1 report, 2026-10-01-fan-flicker-part-1-report.md). The SDK has no per-layer control over
 * that fade (`javap` of `SymbolLayer` and `Style`), so the owner chose it off for the whole style.
 * Rejected: switching it off only while a fan moves (untested, still map-wide), moving the icons
 * by a paint property or drawing them as image quads (untested, a risk to the look at rest).
 */
internal fun transitionWithoutPlacementFade(current: TransitionOptions): TransitionOptions =
    TransitionOptions(current.duration, current.delay, false)

/**
 * Applies [transitionWithoutPlacementFade] to a style that has just finished loading. Called from
 * the one `setStyle` callback, so it holds for every basemap, by day and night, online and offline.
 * Device-only: a `Style` cannot be built under Robolectric.
 */
internal fun disableSymbolFade(style: Style) {
    style.transition = transitionWithoutPlacementFade(style.transition)
}
