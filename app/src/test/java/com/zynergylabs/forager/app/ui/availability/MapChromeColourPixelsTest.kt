package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.MAP_BUBBLE_TAG
import com.zynergylabs.forager.app.ui.log.recordsFilterChipTestTag
import com.zynergylabs.forager.app.ui.log.RecordsSubTab
import com.zynergylabs.forager.app.ui.log.JOURNAL_DETAIL_PANE_TAG
import com.zynergylabs.forager.app.domain.model.LatLng
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
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
    /** [color] over the screen's own background at [alpha]. */
    private fun over(color: Color, alpha: Float, reference: Color) = Color(
        red = color.red * alpha + reference.red * (1f - alpha),
        green = color.green * alpha + reference.green * (1f - alpha),
        blue = color.blue * alpha + reference.blue * (1f - alpha),
    )

    @Test
    fun `the search panel is drawn as the token at 0_8`() {
        composeRule.setContent { MapChromeTestScreen(state, map, roles, darkTheme = dark) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG, useUnmergedTree = true).performTouchInput { click(center) }
        composeRule.waitForIdle()
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val density = composeRule.density
        fun at(x: Dp, y: Dp): Color = with(density) { image[x.toPx().toInt(), y.toPx().toInt()] }
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val token = if (dark) SurfaceContainerDark else SurfaceContainerLight
        val panel = composeRule.onNodeWithTag(SEARCH_DROPDOWN_TAG).getUnclippedBoundsInRoot()
        // The panel covers the middle of the screen, so the reference is the bare map at the foot's edge, above the bottom bar.
        val reference = at(root.left + 4.dp, root.bottom - 200.dp)
        assertSame("the search panel is the token at 0.8", over(token, MAP_CHROME_OVER_MAP_ALPHA, reference), at(panel.left + 3.dp, panel.top + 3.dp))
    }

    @Test
    fun `a map bubble is drawn as the token at 0_8`() {
        val bubbleMap = BubbleMapSlot(listOf(StubGlyph(MapLayerIds.WAYPOINTS, BUBBLE_WAYPOINT.id, 60.dp, 380.dp, LatLng(BUBBLE_WAYPOINT.lat, BUBBLE_WAYPOINT.lng))))
        composeRule.setContent { MapChromeTestScreen(state, bubbleMap, roles, darkTheme = dark) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(glyphTag(BUBBLE_WAYPOINT.id), useUnmergedTree = true).performTouchInput { click(center) }
        composeRule.waitForIdle()
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val density = composeRule.density
        fun at(x: Dp, y: Dp): Color = with(density) { image[x.toPx().toInt(), y.toPx().toInt()] }
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val token = if (dark) SurfaceContainerDark else SurfaceContainerLight
        val card = composeRule.onNodeWithTag(MAP_BUBBLE_TAG).getUnclippedBoundsInRoot()
        val reference = at(root.left + 4.dp, root.bottom - 200.dp)
        // Inside the card's left edge at mid-height, clear of its text; the card has a 6 dp shadow that darkens a little.
        assertSameColourShadowed("the bubble is the token at 0.8", over(token, MAP_CHROME_OVER_MAP_ALPHA, reference), at(card.left + 4.dp, (card.top + card.bottom) / 2))
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

/**
 * The wide record-details pane: opaque, and now the token (planner: "It takes the token colour and STAYS solid"). It was
 * `surface`; the pulse read it as surfaceContainerLow. Read from pixels because its semantic seam repeats the colour it is given.
 */
abstract class MapChromeColourPanePixelsTests(private val dark: Boolean) {
    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private val map = BubbleMapSlot(emptyList())
    private val state = MapChromeScreenState()
    private val roles = MapChromeRoles()

    @Test
    fun `the wide details pane is drawn as the token, solid`() {
        composeRule.setContent { MapChromeTestScreen(state, map, roles, darkTheme = dark) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Mushroom Log").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Records").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.WAYPOINTS)).performScrollTo().performClick()
        composeRule.waitForIdle()
        val row = "records-swipe-waypoints-${BUBBLE_WAYPOINT.id}"
        composeRule.onNodeWithTag(row).performScrollTo()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(row).performTouchInput { click(androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.25f)) }
        composeRule.waitForIdle()
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val density = composeRule.density
        val pane = composeRule.onNodeWithTag(JOURNAL_DETAIL_PANE_TAG).getUnclippedBoundsInRoot()
        val token = if (dark) SurfaceContainerDark else SurfaceContainerLight
        // The pane's own corner, inside its bounds, clear of the content and of the system-bar padding.
        val px = with(density) { image[(pane.left + 3.dp).toPx().toInt(), (pane.top + 3.dp).toPx().toInt()] }
        val ok = abs(px.red - token.red) <= 2f / 255f && abs(px.green - token.green) <= 2f / 255f && abs(px.blue - token.blue) <= 2f / 255f
        assertTrue("the pane's fill is the token, solid: expected (%.3f, %.3f, %.3f), read (%.3f, %.3f, %.3f)".format(token.red, token.green, token.blue, px.red, px.green, px.blue), ok)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w840dp-h1024dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeColourPanePixelsDarkTest : MapChromeColourPanePixelsTests(dark = true)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w840dp-h1024dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeColourPanePixelsLightTest : MapChromeColourPanePixelsTests(dark = false)
