package com.zynergylabs.forager.app.ui.map

import org.maplibre.android.style.layers.TransitionOptions

/**
 * The style's transition with MapLibre's symbol fade (placement transitions) turned off and
 * everything else as [current] has it (dispatch 2026-09-28-369 and its amendment 2026-09-28-370).
 * The duration and delay are the paint transitions the other layers run on (the fan-clarity fade
 * of the other markers to 80%, layer opacity changes), so they are kept, not set.
 *
 * STUB, written before the fix so the test can be seen to fail: returns [current] unchanged.
 */
internal fun transitionWithoutPlacementFade(current: TransitionOptions): TransitionOptions = current
