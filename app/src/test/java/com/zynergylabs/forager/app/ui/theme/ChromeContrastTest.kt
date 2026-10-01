package com.zynergylabs.forager.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.pow

/**
 * C1 (dispatch 2026-09-28-210, item 5): every text and icon colour now drawn on the navigation bar's colour
 * (`surfaceContainer`), the one token map chrome takes, checked at WCAG 2.2 AA in both themes: 4.5:1 for
 * text, 3:1 for icons and other non-text. Pure arithmetic over the two colour schemes, as [ThemeContrastTest]
 * is; the two share nothing but the method.
 *
 * **What each pair is.** The chrome's own content colour (`Color.White` in dark, [Bark] in light: the
 * search bar, panel, strip, HUD, chips, legend, cluster, bubbles, handles), `onSurface` (dialogs, sheets,
 * the drawer, menus, the centre-pin row), `onSurfaceVariant` (the navigation bar's own content colour, a
 * dialog's body text, and now the snackbar's text), `primary` (text buttons in dialogs and the snackbar's
 * action) and `error` (the "Stale" label in a region's bubble). The handles' outline is `White` or [Bark]
 * at 0.7 over the token, and is held to 3:1.
 *
 * **What it does not establish.** Contrast against the token is contrast against the fill's own colour; over
 * a map the fill is 0.8 of it, so the real background is a blend with whatever terrain is beneath, which no
 * test here can see (device-only). Colours drawn by shared composables that this list does not name (a
 * record type's accent in a details sheet) are not enumerated: unverified.
 */
class ChromeContrastTest {

    private val textMinimum = 4.5
    private val nonTextMinimum = 3.0

    private fun luminance(color: Color): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }

    private fun ratio(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    /** [top] at its own alpha over [bottom]. */
    private fun over(top: Color, bottom: Color): Color {
        val a = top.alpha
        return Color(
            red = top.red * a + bottom.red * (1f - a),
            green = top.green * a + bottom.green * (1f - a),
            blue = top.blue * a + bottom.blue * (1f - a),
        )
    }

    private fun textPairs(scheme: ColorScheme, chromeContent: Color): List<Pair<String, Color>> = listOf(
        "the chrome's own content colour" to chromeContent,
        "onSurface" to scheme.onSurface,
        "onSurfaceVariant, the navigation bar's own content colour" to scheme.onSurfaceVariant,
        "primary, a dialog's text button and the snackbar's action" to scheme.primary,
        "error, a region bubble's Stale label" to scheme.error,
    )

    private fun check(name: String, scheme: ColorScheme, chromeContent: Color): List<String> {
        val token = scheme.surfaceContainer
        val failures = mutableListOf<String>()
        for ((label, colour) in textPairs(scheme, chromeContent)) {
            val r = ratio(colour, token)
            if (r < textMinimum) failures += "%s: %s on the token is %.2f:1, below AA text (%.1f:1)".format(name, label, r, textMinimum)
        }
        val outline = over(chromeContent.copy(alpha = 0.7f), token)
        val r = ratio(outline, token)
        if (r < nonTextMinimum) failures += "%s: the handles' outline (%s at 0.7) on the token is %.2f:1, below the non-text bar (%.1f:1)".format(name, if (chromeContent == Color.White) "White" else "Bark", r, nonTextMinimum)
        return failures
    }

    @Test
    fun `every text and icon colour drawn on the navigation bar's colour clears AA in both themes`() {
        val failures = check("dark", DarkColors, Color.White) + check("light", LightColors, Bark)
        if (failures.isNotEmpty()) fail("Contrast failures:\n  " + failures.joinToString("\n  "))
    }

    /**
     * Why the snackbar's content colour changed. It was `inverseOnSurface` on `inverseSurface`; on the token it
     * is a dark colour on a dark fill in dark, and the mirror in light. This pins that the old pair fails, so the
     * change is a response to a measured failure and not a taste (dispatch item 5: content changes only where
     * contrast would fail).
     */
    @Test
    fun `the snackbar's old content colour fails on the token in dark, which is why it changed`() {
        val r = ratio(DarkColors.inverseOnSurface, DarkColors.surfaceContainer)
        assertTrue("inverseOnSurface on the token in dark is %.2f:1; expected below %.1f:1".format(r, textMinimum), r < textMinimum)
    }

    @Test
    fun `the snackbar's old action colour fails on the token in dark`() {
        val r = ratio(DarkColors.inversePrimary, DarkColors.surfaceContainer)
        assertTrue("inversePrimary on the token in dark is %.2f:1; expected below %.1f:1".format(r, textMinimum), r < textMinimum)
    }

    /** A guard on the guard, as [ThemeContrastTest] has: black on white is exactly 21:1. */
    @Test
    fun `the contrast calculation is correct`() {
        val r = ratio(Color.Black, Color.White)
        assertTrue("black on white should be 21:1, got %.2f".format(r), kotlin.math.abs(r - 21.0) < 0.01)
    }
}
