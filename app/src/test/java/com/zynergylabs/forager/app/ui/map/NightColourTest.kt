package com.zynergylabs.forager.app.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [nightColorOf] against the V1 model as the swatch board records it
 * (`docs/audits/2026-09-27-marker-swatch-board.md` section 2): MapLibre Native's raster shader at
 * `raster-brightness-min 1`, `raster-brightness-max 0`, `raster-hue-rotate 180` gives
 * `night = clamp(c + 1 - 2 * mean(r, g, b))` per gamma-encoded channel, with a stated residual
 * against the S22 captures of P99 1.0/255.
 *
 * Expected values are the board's own section 3 feature pairs, typed here as literals (day cluster,
 * and "night = V1 of the same member pixels"), not computed from the code under test. Each pair is
 * a cluster mean, so a channel may differ from V1 of the rounded day mean by rounding; every pair
 * sits within 1/255 by hand calculation before this file was written.
 */
class NightColourTest {

    private fun rgb(hex: Int): Int = (0xFF shl 24) or hex
    private fun channels(argb: Int) = listOf((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)

    /** Swatch board section 3, Topo then Street features: day cluster to V1 night. */
    private val boardPairs = listOf(
        0xA78A41 to 0xB09249,
        0xD2A65F to 0x976C24,
        0xBE984A to 0xA78234,
        0x204FFD to 0x2D5BFE,
        0x7395CB to 0x3B5D92,
        0xF29529 to 0xD17408,
        0xE5E9E1 to 0x1B1E16,
        0xD5D6CD to 0x2F3027,
        0xAFB2AF to 0x4E524E,
        0x70726E to 0x8F918D,
        0xAED4DD to 0x183E47,
        0xFAD5A3 to 0x573201,
        0xD0B890 to 0x6A5229,
        0xC49D4F to 0xA37C2E,
        0xA6A55D to 0x8B8941,
        0xE892A2 to 0x7F2939,
        0xF9B29C to 0x732C16,
    )

    @Test
    fun `every swatch-board day-night pair is matched within the model's residual`() {
        for ((day, night) in boardPairs) {
            val got = channels(nightColorOf(rgb(day)))
            val expected = channels(rgb(night))
            got.zip(expected).forEachIndexed { i, (g, e) ->
                assertTrue(
                    "day #%06X channel $i: got $g, board says $e (residual P99 is 1/255)".format(day),
                    kotlin.math.abs(g - e) <= 1,
                )
            }
        }
    }

    @Test
    fun `black and white swap`() {
        assertEquals(rgb(0xFFFFFF), nightColorOf(rgb(0x000000)))
        assertEquals(rgb(0x000000), nightColorOf(rgb(0xFFFFFF)))
    }

    @Test
    fun `alpha is carried through unchanged`() {
        val halfBlack = (0x80 shl 24) or 0x000000
        assertEquals((0x80 shl 24) or 0xFFFFFF, nightColorOf(halfBlack))
    }

    /**
     * V1 adds the same amount to every channel, so HSV hue is unchanged wherever no channel clamps
     * (planner ruling 3: the check is in HSV). The fixtures are the board pairs whose V1 does not
     * clamp and whose HSV saturation is at least 0.2; #204FFD is left out because its blue channel
     * clamps. Oklab hue does drift (the board's #fad5a3 goes 74.9 to 65.9 degrees); that is not
     * what this asserts.
     */
    @Test
    fun `HSV hue is preserved within 2 degrees for saturated colours that do not clamp`() {
        val saturatedUnclamped = listOf(
            0xA78A41, 0xD2A65F, 0xBE984A, 0x7395CB, 0xF29529, 0xAED4DD,
            0xFAD5A3, 0xD0B890, 0xC49D4F, 0xA6A55D, 0xE892A2, 0xF9B29C,
        )
        for (day in saturatedUnclamped) {
            val (dayHue, daySaturation) = hueAndSaturation(rgb(day))
            assertTrue("fixture #%06X must be saturated".format(day), daySaturation >= 0.2)
            val (nightHue, _) = hueAndSaturation(nightColorOf(rgb(day)))
            val drift = kotlin.math.abs(((nightHue - dayHue + 540.0) % 360.0) - 180.0)
            assertTrue("#%06X: day hue %.1f, night hue %.1f".format(day, dayHue, nightHue), drift <= 2.0)
        }
    }

    private fun hueAndSaturation(argb: Int): Pair<Double, Double> {
        val (r, g, b) = channels(argb).map { it / 255.0 }
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val hue = when {
            delta == 0.0 -> 0.0
            max == r -> 60.0 * (((g - b) / delta) % 6.0)
            max == g -> 60.0 * (((b - r) / delta) + 2.0)
            else -> 60.0 * (((r - g) / delta) + 4.0)
        }.let { if (it < 0) it + 360.0 else it }
        return hue to (if (max == 0.0) 0.0 else delta / max)
    }
}
