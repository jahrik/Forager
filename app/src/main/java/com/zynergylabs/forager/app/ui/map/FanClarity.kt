package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.ui.map.layers.LayerPaint
import com.zynergylabs.forager.app.ui.map.layers.MapLayerSpec

// STUB for the failing-tests commit (dispatch 2026-09-28-265): every function returns the behaviour
// before the change, so the tests fail on their assertions and not on a missing symbol.

internal const val FAN_FADE_OPACITY = 0.8f
internal const val FAN_CIRCLE_DIAMETER_DP = 48f
internal const val FAN_CIRCLE_OPACITY = 1f

internal fun fadedWhileFanned(spec: MapLayerSpec): Boolean = false

internal fun fanFadedPaint(spec: MapLayerSpec, paint: LayerPaint, fanOpen: Boolean): LayerPaint = paint

internal data class FanCircleStyle(val radiusDp: Float, val colour: Int, val opacity: Float)

internal fun fanCircleStyle(chromeColour: Int): FanCircleStyle = FanCircleStyle(0f, chromeColour, 1f)
