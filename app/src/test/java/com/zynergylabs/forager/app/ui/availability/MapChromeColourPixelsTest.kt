package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.ui.map.MAP_CHROME_OVER_MAP_ALPHA
import com.zynergylabs.forager.app.ui.map.MAP_ICON_BAR_TAG
import com.zynergylabs.forager.app.ui.theme.SurfaceContainerDark
import com.zynergylabs.forager.app.ui.theme.SurfaceContainerLight
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * C1 (dispatch 2026-09-28-210): what the phone's portrait Maps tab actually **draws**, read from rendered pixels, not from the
 * container colours the surfaces declare (`MapChromeColourTest` reads those through semantics, which repeat the colour the
 * surface is given; only pixels show that what is drawn is what is declared). The owner: "Have them be the same color as the
 * bottom app navigation bar." So the search bar, the compass strip and the icon bar are each compared with the navigation bar
 * in the same frame, and each with the navigation bar's colour at the standing 0.8 over the screen's own background (the
 * stand-in map draws nothing, as in `LandscapeLClusterPixelsTest`). Native graphics, as `captureToImage` needs.
 */
abstract class MapChromeColourPixelsTests(private val dark: Boolean) {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private val map = BubbleMapSlot(emptyList())
    private val state = MapChromeScreenState()
    private val roles = MapChromeRoles()

    private fun rgb(c: Color) = "(%.3f, %.3f, %.3f)".format(c.red, c.green, c.blue)

    private fun assertSame(message: String, expected: Color, actual: Color, tolerance: Float = 2f / 255f) {
        assertTrue(
            "$message: expected ${rgb(expected)}, read ${rgb(actual)}",
            abs(expected.red - actual.red) <= tolerance && abs(expected.green - actual.green) <= tolerance && abs(expected.blue - actual.blue) <= tolerance,
        )
    }

    /**
     * The icon bar has a 2 dp shadow (`shadowElevation`, unchanged by this dispatch) that shows through its translucent fill and
     * darkens what is under it slightly: in light, about 3% (read 0.933, 0.922, 0.875 against 0.961, 0.945, 0.898 declared), and about 7% in dark (0.114 against 0.122),
     * So the bar is held to the same colour **darkened by a neutral factor of at most 10%**: each channel is the
     * expected one times one common factor, not a different hue. A Cream or Bark bar fails this, having a different hue.
     */
    private fun assertSameColourShadowed(message: String, expected: Color, actual: Color) {
        val k = actual.red / expected.red
        val ok = k in 0.90f..1.003f &&
            abs(actual.green - expected.green * k) <= 2f / 255f && abs(actual.blue - expected.blue * k) <= 2f / 255f
        assertTrue("$message: expected ${rgb(expected)} darkened by a factor of 0.90 to 1, read ${rgb(actual)} (factor %.3f)".format(k), ok)
    }

    @Test
    fun `the search bar, the compass strip, the icon bar and the navigation bar are drawn as one colour`() {
        composeRule.setContent { MapChromeTestScreen(state, map, roles, darkTheme = dark) }
        composeRule.waitForIdle()
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val density = composeRule.density
        fun at(x: Dp, y: Dp): Color = with(density) { image[x.toPx().toInt(), y.toPx().toInt()] }
        fun bounds(tag: String) = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val token = if (dark) SurfaceContainerDark else SurfaceContainerLight
        val over = MAP_CHROME_OVER_MAP_ALPHA
        val reference = at((root.left + root.right) / 2, (root.top + root.bottom) / 2)
        fun composite(chrome: Color, alpha: Float) = Color(
            red = chrome.red * alpha + reference.red * (1f - alpha),
            green = chrome.green * alpha + reference.green * (1f - alpha),
            blue = chrome.blue * alpha + reference.blue * (1f - alpha),
        )
        val expected = composite(token, over)

        val nav = bounds(COMPACT_BOTTOM_NAV_TAG)
        val navPixel = at(nav.left + 4.dp, (nav.top + nav.bottom) / 2)
        assertSame("the navigation bar over the map is the token at 0.8", expected, navPixel)

        val search = bounds(SEARCH_ENTRY_BAR_TAG)
        assertSame("the search bar is drawn as the navigation bar is", navPixel, at(search.left + 4.dp, (search.top + search.bottom) / 2))
        assertSame("the search bar is the token at 0.8", expected, at(search.left + 4.dp, (search.top + search.bottom) / 2))

        val strip = bounds("compass-elevation-strip")
        assertSame("the compass strip is the token at 0.8", expected, at(strip.left + 4.dp, (strip.top + strip.bottom) / 2))

        // The icon bar is a 0.5 layer on the cluster's 0.6 container: 0.8 together, on a point clear of an icon (as the L test).
        val bar = bounds(MAP_ICON_BAR_TAG)
        assertSameColourShadowed("the icon bar reads as the token at 0.8 (0.5 over 0.6)", expected, at(bar.left + 6.dp, bar.top + 24.dp))

        assertTrue(
            "sanity: the reference (the map's own background) is not the chrome colour itself",
            abs(reference.red - expected.red) > 2f / 255f || abs(reference.green - expected.green) > 2f / 255f || abs(reference.blue - expected.blue) > 2f / 255f,
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeColourPixelsDarkTest : MapChromeColourPixelsTests(dark = true)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeColourPixelsLightTest : MapChromeColourPixelsTests(dark = false)
