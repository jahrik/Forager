package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.ui.map.layers.LayerKind
import com.zynergylabs.forager.app.ui.map.layers.LayerPaint
import com.zynergylabs.forager.app.ui.map.layers.MapLayerSpec
import com.zynergylabs.forager.app.ui.map.layers.OpacityValue
import com.zynergylabs.forager.app.ui.map.layers.TapGroup

/*
 * The two look changes that make an open fan readable (dispatch 2026-09-28-265; the owner: "have icons
 * that aren't fanned out fade to 80% opacity, and give the fanned out icons a themed background at 80%
 * opacity ... around 48dp"). This file is the values and the rule for which layers take them, pure and
 * headless. That `SightingsMap` then sets them on live MapLibre layers is device-only: a `MapView`
 * cannot be built under Robolectric.
 */

/**
 * What every marker icon that is not part of an open fan draws at, as a multiplier on what it drew:
 * 80%. A multiplier rather than an absolute value, as the Layers sheet's opacity is (`LayerState`): the
 * sighting dot's fill is 0.7 at base, and an absolute 0.8 would make it more opaque, not less.
 */
internal const val FAN_FADE_OPACITY = 0.8f

/** The circle behind each fanned copy: 48 dp across, the touch size (the owner: "around 48dp"). */
internal const val FAN_CIRCLE_DIAMETER_DP = 48f

/** The circle's opacity: the standing 80% of chrome over the map ([MAP_CHROME_OVER_MAP_ALPHA]). */
internal const val FAN_CIRCLE_OPACITY = MAP_CHROME_OVER_MAP_ALPHA

/**
 * Whether [spec] fades while a fan is open: the registry's tappable marker layers, which are the finds,
 * photos, waypoints, planned trips and sighting dots. Not a line, a fill or a colour field. Not the
 * search-centre reticle, which is a marker layer but takes no taps and is not one of the stacked
 * records the fan sorts out, nor a journal halo, which is a decoration.
 */
internal fun fadedWhileFanned(spec: MapLayerSpec): Boolean =
    spec.kind == LayerKind.MARKER && spec.tapGroup == TapGroup.MARKER

/**
 * [paint] (what the layer's own state resolves to, [com.zynergylabs.forager.app.ui.map.layers.layerPaintFor])
 * with the fade applied while a fan is open: each opacity times [FAN_FADE_OPACITY], visibility untouched.
 * Closed, or for a layer that does not fade, [paint] itself: the same instance, so the restore is
 * exactly the state's value and never a remembered copy.
 */
internal fun fanFadedPaint(spec: MapLayerSpec, paint: LayerPaint, fanOpen: Boolean): LayerPaint =
    if (!fanOpen || !fadedWhileFanned(spec)) {
        paint
    } else {
        paint.copy(opacities = paint.opacities.map { OpacityValue(it.property, it.value * FAN_FADE_OPACITY) })
    }

/** The paint of the circle layer behind the fanned copies: radius in dp, colour as ARGB, opacity 0 to 1. */
internal data class FanCircleStyle(val radiusDp: Float, val colour: Int, val opacity: Float)

/**
 * The circle in [chromeColour], the map chrome's own colour (the navigation bar's and icon cluster's
 * container, day or night as the app is), 48 dp across at 80%.
 */
internal fun fanCircleStyle(chromeColour: Int): FanCircleStyle =
    FanCircleStyle(radiusDp = FAN_CIRCLE_DIAMETER_DP / 2f, colour = chromeColour, opacity = FAN_CIRCLE_OPACITY)
