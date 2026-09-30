package com.zynergylabs.forager.app.ui.map

import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * A headless model of MapLibre Native's raster shader, for night-mode paints (dispatch
 * 2026-09-28-310, topo night). Pure Kotlin, no Android or MapLibre types, so
 * `TopoNightStreetSwitchTest` runs sampled tiles through the paint that `styleJsonFor` actually produces.
 * [nightColorOf] is the same model frozen at V1 for colours this project sets itself; this one takes
 * the five paint properties as they arrive.
 *
 * **What it models.** `shaders/raster.fragment.glsl` and `raster_layer_tweaker.cpp` at the pinned
 * `13.5.0` (read from the tag for this change, and cited from the same files in
 * `docs/audits/2026-09-27-marker-swatch-board.md` section 2): per pixel, on gamma-encoded 0-1 channels,
 * spin (hue rotate) by the circulant weights, then saturation toward the pixel's own channel mean,
 * then contrast about 0.5, then `mix(brightness-min, brightness-max, rgb)`, clamped by the
 * framebuffer and rounded to 8 bits. The spin keeps each pixel's channel mean, so the output's channel
 * mean is an affine function of the input's: that is why no raster paint can put both a black label
 * and a pale road above a mid ground (see the completion report).
 *
 * **Where it may differ from the device.** It is per pixel and opaque. It does not model: bilinear
 * resampling between tile pixels at a fractional zoom; the parent/child tile cross-fade
 * (`raster-fade-duration`, where two tile levels blend under one paint); premultiplied-alpha edges;
 * the GPU's own rounding; or the paint transition (`raster-*-transition`) that eases a property
 * change. The swatch board measured the V1 case of this model against S22 captures at a mean
 * per-channel error of 0.63/255; the paint has not been measured on a device.
 */

/** The five raster paint properties the shader reads. Defaults are the style spec's. */
internal data class RasterPaint(
    val brightnessMin: Double = 0.0,
    val brightnessMax: Double = 1.0,
    val hueRotate: Double = 0.0,
    val saturation: Double = 0.0,
    val contrast: Double = 0.0,
)

/**
 * The tile zoom MapLibre Native 13.5.0 fetches for a raster source of [tileSize] at [mapZoom]:
 * `round(zoom + log2(512 / tileSize))` (`util::coveringZoomLevel`, `src/mbgl/util/tile_cover.cpp`;
 * `tileSize_D` is 512 in `include/mbgl/util/constants.hpp`). A 256 px source is therefore one level
 * ahead of the map zoom, and tile level N is shown for map zoom N - 1.5 up to, but not including,
 * N - 0.5. A style expression's `["zoom"]` is the map zoom, not the tile level.
 */
internal fun rasterTileZoomFor(mapZoom: Double, tileSize: Int = 256): Int =
    (mapZoom + log2(512.0 / tileSize)).roundToInt()

/**
 * The raster paint [paint] (a layer's `"paint"` object) gives: each property is a number, or the style
 * spec's default when missing. Night topo's two layers carry constant paints (V1), so nothing here reads a
 * zoom; a property that is an expression (`["step", ...]`, `["interpolate", ...]`) throws, because reading
 * it as a guessed number would be a fabricated value. (An earlier version of this model evaluated `step`
 * and `interpolate` on `["zoom"]` for the half-amplitude paint; it was removed with that paint.)
 */
internal fun rasterPaintOf(paint: JsonObject): RasterPaint {
    val defaults = RasterPaint()
    fun value(key: String, default: Double): Double = paint[key]?.let { number(it, key) } ?: default
    return RasterPaint(
        brightnessMin = value("raster-brightness-min", defaults.brightnessMin),
        brightnessMax = value("raster-brightness-max", defaults.brightnessMax),
        hueRotate = value("raster-hue-rotate", defaults.hueRotate),
        saturation = value("raster-saturation", defaults.saturation),
        contrast = value("raster-contrast", defaults.contrast),
    )
}

private fun number(element: JsonElement, key: String): Double =
    (element as? JsonPrimitive)?.doubleOrNull ?: error("$key: expected a number, found $element (expressions are not evaluated)")

/**
 * One opaque pixel (ARGB, alpha carried through) as the shader renders it under [paint]. With
 * `RasterPaint(1.0, 0.0, 180.0)` this is [nightColorOf] up to rounding, which `TopoNightStreetSwitchTest`
 * asserts over the sampled tiles.
 */
internal fun rasterShade(argb: Int, paint: RasterPaint): Int {
    val alpha = (argb ushr 24) and 0xFF
    val r = ((argb shr 16) and 0xFF) / 255.0
    val g = ((argb shr 8) and 0xFF) / 255.0
    val b = (argb and 0xFF) / 255.0

    val spin = Math.toRadians(paint.hueRotate)
    val s = sin(spin)
    val c = cos(spin)
    val root3 = sqrt(3.0)
    val wx = (2 * c + 1) / 3
    val wy = (-root3 * s - c + 1) / 3
    val wz = (root3 * s - c + 1) / 3
    val spun = doubleArrayOf(
        r * wx + g * wy + b * wz,
        r * wz + g * wx + b * wy,
        r * wy + g * wz + b * wx,
    )

    val saturationFactor = if (paint.saturation > 0) 1.0 - 1.0 / (1.001 - paint.saturation) else -paint.saturation
    val contrastFactor = if (paint.contrast > 0) 1.0 / (1.0 - paint.contrast) else 1.0 + paint.contrast
    val average = (r + g + b) / 3.0

    fun channel(v0: Double): Int {
        val v1 = v0 + (average - v0) * saturationFactor
        val v2 = (v1 - 0.5) * contrastFactor + 0.5
        val v3 = paint.brightnessMin + (paint.brightnessMax - paint.brightnessMin) * v2
        return (v3.coerceIn(0.0, 1.0) * 255.0).roundToInt()
    }
    return (alpha shl 24) or (channel(spun[0]) shl 16) or (channel(spun[1]) shl 8) or channel(spun[2])
}
