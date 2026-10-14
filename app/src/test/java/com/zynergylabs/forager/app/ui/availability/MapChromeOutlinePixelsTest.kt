package com.zynergylabs.forager.app.ui.availability

import android.graphics.Bitmap
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MAP_ICON_STACK_BORDER_COLOR_DARK
import com.zynergylabs.forager.app.ui.map.MAP_ICON_STACK_BORDER_COLOR_LIGHT
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.theme.ForagerTheme
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * Dispatch 2026-10-13-02 (RECORD -966, -967): the icon bar's outline on the map's other chrome, read from rendered pixels of the
 * real [AvailabilityScreen] over a flat grey stand-in map, day and night, portrait and landscape.
 *
 * - **Present** on each piece's sides that face the map and on its rounded corners: the pill, the compass strip (and its card
 *   grown down while navigating), the bottom bar, the rail and the landscape navigation card.
 * - **Absent** on every side that lies on the screen's own edge (RECORD -967, the owner: "2 yes").
 * - **One line where two pieces touch** (the owner: "1 C one line between the two like there is now"): across each join the
 *   run of outline-coloured pixels is one line wide (2 to 4 px at xxhdpi, 1 dp being 3 px), not two (6 px), and not none.
 *
 * What counts as the outline: a pixel within 0.07 of the luminance the outline colour ([MAP_ICON_STACK_BORDER_COLOR_LIGHT] /
 * [MAP_ICON_STACK_BORDER_COLOR_DARK], its own 0.4 alpha) composites to over the piece's own fill, read at a reference point on
 * the same piece, and at least 0.12 from that fill. The fill, the cast shadows (a few percent) and the pill's shadow stepped
 * onto the strip (about 17% at its darkest) all fall outside that band; the line is about 0.26 from the fill by day and 0.3 at
 * night. Robolectric reports zero insets, so every side here that would sit beside a system bar sits on the window's edge;
 * against real system bars the sides beside them are device items.
 *
 * Each state is also written as a PNG under the build's test-results directory, which CI uploads; nothing goes into the
 * repository.
 */
abstract class MapChromeOutlinePixelsTests(private val dark: Boolean, private val rotation: Int?) {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 104.9, accuracyMeters = 5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.5135, lng = -122.67, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val mapColour = Color(0xFF808080)

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).background(mapColour))
    }

    private val outline = if (dark) MAP_ICON_STACK_BORDER_COLOR_DARK else MAP_ICON_STACK_BORDER_COLOR_LIGHT
    private val theme = if (dark) "night" else "day"
    private val orientation = if (rotation == null) "portrait" else "landscape"

    private lateinit var image: PixelMap

    private fun setScreen(navigating: Boolean, shot: String) {
        if (rotation != null) Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            ForagerTheme(darkTheme = dark) {
                AvailabilityScreen(
                    uiState = AvailabilityUiState(liveFix = fix),
                    isRecording = navigating,
                    isReturning = navigating,
                    navigationTarget = if (navigating) start else null,
                    returnRoute = ReturnRoute.Ahead(LatLng(45.5135, -122.67), 245.0),
                    compassProvider = FixedCompass(134f),
                    computeTrueHeading = ComputeTrueHeadingUseCase(NoDeclination),
                    currentTime = CurrentTimeProvider { t + 1_000L },
                    mapSlot = map,
                    onUseCurrentLocation = {},
                    onManualLatChanged = {},
                    onManualLngChanged = {},
                    onSearchManualCoordinates = {},
                    onRadiusChanged = {},
                    onMonthSelected = {},
                    onMapTabSelected = {},
                    onSeasonalTabSelected = {},
                    onTaxonSearchQueryChanged = {},
                    onTaxonSearchResultSelected = {},
                    onDismissTaxonSuggestions = {},
                    onReopenTaxonSuggestions = {},
                    onPlaceTripPin = { _, _, _ -> },
                    onDeletePlannedTrip = {},
                    onRecentSearchSelected = {},
                    onOfflineMapLatChanged = {},
                    onOfflineMapLngChanged = {},
                    onOfflineMapRadiusChanged = {},
                    onOfflineMapNameChanged = {},
                    onOfflineMapsOpened = {},
                    onDownloadOfflineMaps = {},
                    onDeleteOfflineRegion = {},
                    onNightModeMapsChanged = {},
                    onThemeModeChanged = {},
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
        capture(shot)
    }

    private fun capture(shot: String) {
        val bitmap: ImageBitmap = composeRule.onRoot().captureToImage()
        val dir = File("build/test-results/testDebugUnitTest/map-chrome-outline-screens").apply { mkdirs() }
        val file = File(dir, "$orientation-$shot-$theme.png")
        file.outputStream().use { bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SCREENSHOT|${file.absolutePath}|${bitmap.width}x${bitmap.height}")
        image = bitmap.toPixelMap()
    }

    // ── Reading pixels ──

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    private fun px(d: Dp): Int = (d.value * composeRule.density.density).roundToInt()

    /** The first pixel inside a left or top edge, and the middle of the 3 px a 1 dp line lays inside a right or bottom one. */
    private fun inStart(edge: Dp) = px(edge) + 1
    private fun inEnd(edge: Dp) = px(edge) - 2

    private fun at(x: Int, y: Int): Color = image[x, y]
    private fun at(x: Dp, y: Dp): Color = image[px(x), px(y)]

    private fun lum(c: Color) = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
    private fun rgb(c: Color) = "(%.3f, %.3f, %.3f; lum %.3f)".format(c.red, c.green, c.blue, lum(c))

    /** The outline colour at its own alpha, over [fill]. */
    private fun lineOver(fill: Color): Color {
        val a = outline.alpha
        return Color(
            red = outline.red * a + fill.red * (1f - a),
            green = outline.green * a + fill.green * (1f - a),
            blue = outline.blue * a + fill.blue * (1f - a),
        )
    }

    private fun isLine(p: Color, fill: Color) = abs(lum(p) - lum(lineOver(fill))) < 0.07f && abs(lum(p) - lum(fill)) > 0.12f

    private fun assertLine(what: String, x: Int, y: Int, fill: Color) {
        val p = at(x, y)
        assertTrue("$what: the outline at ($x, $y) px: read ${rgb(p)}, the outline over the fill ${rgb(lineOver(fill))}, the fill ${rgb(fill)}", isLine(p, fill))
    }

    /** Within a pixel either way, for a point on a rounded corner's arc. */
    private fun assertLineNear(what: String, x: Int, y: Int, fill: Color) {
        val around = (-1..1).flatMap { dx -> (-1..1).map { dy -> at(x + dx, y + dy) } }
        assertTrue(
            "$what: the outline within a pixel of ($x, $y) px: read ${around.joinToString { rgb(it) }}, the outline over the fill ${rgb(lineOver(fill))}",
            around.any { isLine(it, fill) },
        )
    }

    private fun assertNoLine(what: String, x: Int, y: Int, fill: Color) {
        val p = at(x, y)
        assertTrue(
            "$what: no outline at ($x, $y) px, the piece's own fill: read ${rgb(p)}, the fill ${rgb(fill)}, the outline over it ${rgb(lineOver(fill))}",
            !isLine(p, fill) && abs(lum(p) - lum(fill)) < 0.05f,
        )
    }

    /**
     * A pixel on the line across a join: the outline over either piece's fill ([isLine]), and more than [JOIN_CLEAR_OF_FILLS]
     * from both fills. The second condition is from CI run 38507319758 at bac9de39, the first run of this class: by day the first
     * rows of the pill's shadow stepped onto the portrait strip read 0.678 to 0.725 (strip fill 0.832, pill fill 0.902). That is
     * within 0.07 of the outline over the pill's fill, so [isLine] alone counted them as a second line (6 px, not 3). They are
     * 0.11 to 0.15 from the strip's own fill; the line itself measured 0.235 or more from both fills at every join that run read
     * (day and night, portrait and landscape).
     */
    private fun isJoinLine(p: Color, fillA: Color, fillB: Color) =
        (isLine(p, fillA) || isLine(p, fillB)) &&
            abs(lum(p) - lum(fillA)) > JOIN_CLEAR_OF_FILLS && abs(lum(p) - lum(fillB)) > JOIN_CLEAR_OF_FILLS

    /** The outline-coloured pixels in a run across a join, read against the fill on either side ([isJoinLine]). */
    private fun lineRun(what: String, pixels: List<Pair<Int, Int>>, fillA: Color, fillB: Color): Int {
        val read = pixels.map { (x, y) -> at(x, y) }
        val count = read.count { isJoinLine(it, fillA, fillB) }
        println("MEASURED $orientation $theme $what: $count outline pixels across ${read.joinToString { "%.3f".format(lum(it)) }}")
        return count
    }

    private fun assertOneLine(what: String, pixels: List<Pair<Int, Int>>, fillA: Color, fillB: Color) {
        val count = lineRun(what, pixels, fillA, fillB)
        assertTrue(
            "$what: one line across the join, 2 to 4 px (1 dp is 3 px), not two (6) and not none: read $count across " +
                pixels.joinToString { (x, y) -> rgb(at(x, y)) },
            count in 2..4,
        )
    }

    /** On a rounded corner of radius 16 dp, the outline's 45 degree point, half a line inside the edge. */
    private val arcInset: Dp = 16.dp - (16.dp - 0.5.dp) * 0.70710677f

    // ── Portrait ──

    protected fun portraitPill() {
        setScreen(navigating = false, shot = "pill-strip-bar")
        val pill = bounds(SEARCH_ENTRY_BAR_TAG)
        val mid = (pill.top + pill.bottom) / 2
        val fill = at(pill.left + 4.dp, mid)
        assertLine("the pill's bottom", px(pill.left + 24.dp), inEnd(pill.bottom), fill)
        assertLineNear("the pill's rounded top-left corner", px(pill.left + arcInset), px(pill.top + arcInset), fill)
        assertLineNear("the pill's rounded bottom-right corner", px(pill.right - arcInset), px(pill.bottom - arcInset), fill)
        assertNoLine("the pill's left side, on the screen's edge", inStart(pill.left), px(mid), fill)
        assertNoLine("the pill's right side, on the screen's edge", inEnd(pill.right), px(mid), at(pill.right - 4.dp, mid))
        assertNoLine("the pill's top, the top of the map's area", px(pill.right - 30.dp), inStart(pill.top), at(pill.right - 30.dp, pill.top + 4.dp))
    }

    protected fun portraitStrip(navigating: Boolean) {
        setScreen(navigating = navigating, shot = if (navigating) "navigating" else "strip")
        val pill = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        val stripFill = at(strip.left + 4.dp, strip.top + 10.dp)
        val pillFill = at(pill.left + 4.dp, (pill.top + pill.bottom) / 2)
        assertLine("the strip's card's bottom", px(strip.left + 24.dp), inEnd(strip.bottom), stripFill)
        assertLineNear("the strip's card's rounded bottom-left corner", px(strip.left + arcInset), px(strip.bottom - arcInset), stripFill)
        assertNoLine("the strip's left side, on the screen's edge", inStart(strip.left), px(strip.top + 10.dp), stripFill)
        assertNoLine("the strip's right side, on the screen's edge", inEnd(strip.right), px(strip.top + 10.dp), at(strip.right - 4.dp, strip.top + 10.dp))
        // The tuck: the strip's top lies under the pill's lower edge. One line there, the pill's; none from the strip showing
        // through the pill or laid beside it.
        val x = px(pill.right - 30.dp)
        val join = px(pill.bottom)
        assertOneLine("the pill and the strip", (join - 9..join + 9).map { x to it }, pillFill, stripFill)
    }

    protected fun portraitBottomBar() {
        setScreen(navigating = false, shot = "pill-strip-bar")
        val bar = bounds(COMPACT_BOTTOM_NAV_TAG)
        val mid = (bar.top + bar.bottom) / 2
        val fill = at(bar.left + 4.dp, mid)
        assertLine("the bottom bar's top", px(bar.left + 24.dp), inStart(bar.top), fill)
        assertLineNear("the bottom bar's rounded top-left corner", px(bar.left + arcInset), px(bar.top + arcInset), fill)
        assertLineNear("the bottom bar's rounded top-right corner", px(bar.right - arcInset), px(bar.top + arcInset), fill)
        assertNoLine("the bottom bar's left side, on the screen's edge", inStart(bar.left), px(mid), fill)
        assertNoLine("the bottom bar's right side, on the screen's edge", inEnd(bar.right), px(mid), at(bar.right - 4.dp, mid))
    }

    // ── Landscape ──

    protected fun landscapePillAndStrip() {
        setScreen(navigating = false, shot = "pill-strip-rail")
        val pill = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        val rail = bounds(COMPACT_NAVIGATION_RAIL_TAG)
        println("MEASURED $orientation $theme: pill $pill, strip $strip, rail $rail")
        val pillMid = (pill.top + pill.bottom) / 2
        val pillFill = at(pill.left + 4.dp, pillMid)
        val stripFill = at(strip.left + 24.dp, strip.top + 3.dp)
        val railFill = at(rail.left + 6.dp, (strip.top + strip.bottom) / 2)
        assertEquals("the pill meets the strip", pill.right.value, strip.left.value, 0.5f)
        assertEquals("the strip meets the rail", strip.right.value, rail.left.value, 0.5f)

        assertLine("the pill's bottom", px(pill.left + 40.dp), inEnd(pill.bottom), pillFill)
        assertLineNear("the pill's rounded bottom-left corner", px(pill.left + arcInset), px(pill.bottom - arcInset), pillFill)
        assertNoLine("the pill's outer side, on the screen's edge", inStart(pill.left), px(pillMid), pillFill)
        assertNoLine("the pill's top, the top of the map's area", px(pill.left + 60.dp), inStart(pill.top), at(pill.left + 60.dp, pill.top + 3.dp))

        assertLine("the strip's bottom", px(strip.left + 24.dp), inEnd(strip.bottom), stripFill)
        assertNoLine("the strip's top, the top of the map's area", px(strip.left + 24.dp), inStart(strip.top), stripFill)

        val y = px(strip.top + 3.dp)
        val join = px(strip.left)
        assertOneLine("the pill and the strip", (join - 9..join + 9).map { it to y }, pillFill, stripFill)
        val railY = px((strip.top + strip.bottom) / 2)
        val railJoin = px(rail.left)
        assertOneLine("the strip and the rail", (railJoin - 9..railJoin + 9).map { it to railY }, stripFill, railFill)
    }

    protected fun landscapeRail() {
        setScreen(navigating = false, shot = "pill-strip-rail")
        val rail = bounds(COMPACT_NAVIGATION_RAIL_TAG)
        val mid = (rail.top + rail.bottom) / 2
        val fill = at(rail.left + 6.dp, mid)
        assertLine("the rail's inner edge", inStart(rail.left), px(mid), fill)
        assertLineNear("the rail's rounded inner top corner", px(rail.left + arcInset), px(rail.top + arcInset), fill)
        assertLineNear("the rail's rounded inner bottom corner", px(rail.left + arcInset), px(rail.bottom - arcInset), fill)
        assertNoLine("the rail's outer side, on the screen's edge", inEnd(rail.right), px(mid), at(rail.right - 6.dp, mid))
    }

    protected fun landscapeCard() {
        setScreen(navigating = true, shot = "navigating")
        val card = bounds(NAVIGATION_HUD_TAG)
        val rail = bounds(COMPACT_NAVIGATION_RAIL_TAG)
        println("MEASURED $orientation $theme: card $card, rail $rail")
        assertEquals("the card meets the rail", card.right.value, rail.left.value, 0.5f)
        val low = card.bottom - 4.dp
        val fill = at(card.left + 4.dp, low)
        // The join is read 30 dp below the card's top, on the rail's straight inner edge. CI run 38507319758 read it at `low`,
        // 4 dp above the screen's bottom, which is inside the rail's rounded inner bottom corner: the run beside the card read
        // the map (0.502), not the rail.
        val joinY = card.top + 30.dp
        val joinFill = at(card.left + 4.dp, joinY)
        val railFill = at(rail.left + 6.dp, joinY)
        assertLine("the card's top", px(card.left + 40.dp), inStart(card.top), fill)
        assertLineNear("the card's rounded top-left corner", px(card.left + arcInset), px(card.top + arcInset), fill)
        assertLine("the card's inner side, facing the map", inStart(card.left), px(low), fill)
        assertNoLine("the card's bottom, on the screen's edge", px(card.left + 4.dp), inEnd(card.bottom), fill)
        val y = px(joinY)
        val join = px(card.right)
        assertOneLine("the card and the rail", (join - 9..join + 9).map { it to y }, joinFill, railFill)
    }

    /**
     * Fullscreen: the pill and the rail slide away, so the strip's side by the pill faces the map and the strip draws it; its
     * side by the rail is then on the window's edge (Robolectric's zero insets) and carries none.
     */
    protected fun landscapeFullscreenStrip() {
        setScreen(navigating = false, shot = "pill-strip-rail")
        composeRule.onNodeWithContentDescription("Fullscreen").performTouchInput { click(center) }
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
        capture("fullscreen")
        assertFalse("the pill has slid away", shown(SEARCH_ENTRY_BAR_TAG))
        assertFalse("the rail has slid away", shown(COMPACT_NAVIGATION_RAIL_TAG))
        val strip = bounds(STRIP_TAG)
        val window = composeRule.onRoot().getUnclippedBoundsInRoot()
        println("MEASURED $orientation $theme fullscreen: strip $strip, window $window")
        val fill = at(strip.left + 24.dp, strip.top + 3.dp)
        assertLine("the strip's side by the slid-away pill", inStart(strip.left), px(strip.top + 3.dp), fill)
        assertEquals("the strip's other side is on the window's edge", window.right.value, strip.right.value, 0.5f)
        val mid = (strip.top + strip.bottom) / 2
        assertNoLine("the strip's side on the window's edge", inEnd(strip.right), px(mid), at(strip.right - 4.dp, mid))
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
        const val MAP_TAG = "map-chrome-outline-map"

        /** See [isJoinLine]. */
        const val JOIN_CLEAR_OF_FILLS = 0.18f
    }
}

abstract class MapChromeOutlinePortraitTests(dark: Boolean) : MapChromeOutlinePixelsTests(dark, rotation = null) {
    @Test fun `the pill is outlined on its bottom and rounded corners, not on its sides or top`() = portraitPill()

    @Test fun `the strip is outlined on its bottom and rounded corners, not on its sides, and the pill's edge is the one line between them`() = portraitStrip(navigating = false)

    @Test fun `navigating, the strip's card is outlined at its grown bottom, and the pill's edge is still the one line`() = portraitStrip(navigating = true)

    @Test fun `the bottom bar is outlined on its top and rounded corners, not on its sides`() = portraitBottomBar()
}

abstract class MapChromeOutlineLandscapeTests(dark: Boolean) : MapChromeOutlinePixelsTests(dark, rotation = Surface.ROTATION_90) {
    @Test fun `the pill and the strip are outlined where they face the map, with one line on each join`() = landscapePillAndStrip()

    @Test fun `the rail is outlined on its inner edge and corners, not its outer side`() = landscapeRail()

    @Test fun `the navigation card is outlined on its top and inner side, not its bottom, with one line against the rail`() = landscapeCard()

    @Test fun `in fullscreen the strip draws its side by the pill, and none on the window's edge`() = landscapeFullscreenStrip()
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeOutlinePortraitDayTest : MapChromeOutlinePortraitTests(dark = false)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeOutlinePortraitNightTest : MapChromeOutlinePortraitTests(dark = true)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeOutlineLandscapeDayTest : MapChromeOutlineLandscapeTests(dark = false)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MapChromeOutlineLandscapeNightTest : MapChromeOutlineLandscapeTests(dark = true)
