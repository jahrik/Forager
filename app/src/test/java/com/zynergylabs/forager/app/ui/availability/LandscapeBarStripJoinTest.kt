package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.ui.map.MapSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * RECORD -729 (dispatch 2026-09-28-729): in a short landscape window, not navigating, the search bar and the compass strip meet
 * at the window's centre at one height, split by a 1 dp line. The owner, verbatim: "Landscape mode strip can extend to meet the
 * search bar. The search bar height can change to meet the height of the strip. The two can meet at direct center, and they can
 * be split by a simple vertical line between the two"; shown as steps, "Yes, that's it (Recommended)".
 *
 * Through the real [AvailabilityScreen] at the S22's two landscape windows (780 x 360 and 823 x 384 dp), at ROTATION_90 (the
 * punch-hole, and so the bar, on the left; the rail on the right) and ROTATION_270 (the reverse). Every test first proves the
 * screen saw the rotation it is about. Insets are zero under Robolectric, so the punch-hole-side controls edge is the window's
 * edge here; against a real cut-out it is a device item. A live GPS fix, a fixed 315 degree heading and 3,000 m (9843 ft), the
 * longest readouts the earlier landscape tests use.
 */
abstract class LandscapeBarStripJoinTests {
    val composeRule = createComposeRule()
    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 3_000.0, accuracyMeters = 5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private var longPresses = 0
    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }
    private var generation by mutableStateOf(0)
    private var rotationSeen: Int? = null

    private fun setScreen(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            key(generation) {
                AvailabilityScreen(
                    uiState = AvailabilityUiState(liveFix = fix),
                    compassProvider = FixedCompass(315f),
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
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeen)
    }

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    private fun centreOf(r: DpRect): Dp = (r.left + r.right) / 2
    private fun DpRect.d() = "[%.2f, %.2f][%.2f, %.2f]".format(left.value, top.value, right.value, bottom.value)

    private fun barOnLeft(rotation: Int) = rotation == Surface.ROTATION_90

    // ── The join: centre, height and the line ──

    private fun assertJoin(rotation: Int) {
        setScreen(rotation)
        val window = composeRule.onRoot().getUnclippedBoundsInRoot()
        val mapBounds = bounds(MAP_TAG)
        val centre = centreOf(mapBounds)
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        val rail = bounds(COMPACT_NAVIGATION_RAIL_TAG)
        val line = bounds(LANDSCAPE_BAR_STRIP_LINE_TAG)
        println("MEASURED rotation $rotation: window ${window.d()}, centre ${centre.value}, bar ${bar.d()}, strip ${strip.d()}, line ${line.d()}, rail ${rail.d()}")

        assertEquals("positive control: the map is the window's width, so its centre is the window's", centreOf(window).value, centre.value, 0.5f)
        if (barOnLeft(rotation)) {
            assertEquals("the bar starts at the punch-hole (left) edge", window.left.value, bar.left.value, 0.5f)
            assertEquals("the bar's right edge is the centre", centre.value, bar.right.value, 0.5f)
            assertEquals("the strip's left edge is the centre", centre.value, strip.left.value, 0.5f)
            assertEquals("the strip ends at the rail's inner edge", rail.left.value, strip.right.value, 0.5f)
        } else {
            assertEquals("the bar ends at the punch-hole (right) edge", window.right.value, bar.right.value, 0.5f)
            assertEquals("the bar's left edge is the centre", centre.value, bar.left.value, 0.5f)
            assertEquals("the strip's right edge is the centre", centre.value, strip.right.value, 0.5f)
            assertEquals("the strip starts at the rail's inner edge", rail.right.value, strip.left.value, 0.5f)
        }
        assertEquals("the bar and the strip start at the same top", strip.top.value, bar.top.value, 0.5f)
        assertEquals("the bar is the strip's height", (strip.bottom - strip.top).value, (bar.bottom - bar.top).value, 0.5f)
        assertTrue("the strip keeps its 36 dp floor (${(strip.bottom - strip.top).value})", (strip.bottom - strip.top).value >= 36f - 0.5f)

        assertEquals("the line is 1 dp wide", 1f, (line.right - line.left).value, 0.1f)
        assertEquals("the line sits at the centre", centre.value, centreOf(line).value, 0.5f)
        assertEquals("the line starts at the bar's top", bar.top.value, line.top.value, 0.5f)
        assertEquals("the line ends at the bar's bottom, nothing below it over the map", bar.bottom.value, line.bottom.value, 0.5f)
    }

    @Test fun `at ROTATION_90 the bar and the strip meet at the centre at one height, with the line between them`() = assertJoin(Surface.ROTATION_90)

    @Test fun `at ROTATION_270 the bar and the strip meet at the centre at one height, with the line between them`() = assertJoin(Surface.ROTATION_270)

    // ── The three-dot button, by real touches ──

    private fun assertButtonOpensMenu(rotation: Int) {
        setScreen(rotation)
        val strip = bounds(STRIP_TAG)
        val button = bounds(MAP_QUICK_SETTINGS_BUTTON_TAG)
        println("MEASURED rotation $rotation: button ${button.d()} in strip ${strip.d()}")
        assertEquals("36 dp wide: ${button.d()}", 36f, (button.right - button.left).value, 0.5f)
        assertEquals("36 dp tall: ${button.d()}", 36f, (button.bottom - button.top).value, 0.5f)
        assertTrue("at the strip's far right, inside it: ${button.d()} in ${strip.d()}", strip.right - button.right < 1.dp && button.left >= strip.left)
        val inset = 3.dp
        val points = listOf(
            centreOf(button) to (button.top + button.bottom) / 2,
            button.left + inset to button.top + inset,
            button.right - inset to button.top + inset,
            button.left + inset to button.bottom - inset,
            button.right - inset to button.bottom - inset,
        )
        for ((x, y) in points) {
            composeRule.touchAt(x, y)
            assertTrue("a real touch at (${x.value}, ${y.value}) in ${button.d()} opened the menu", shown(MAP_QUICK_SETTINGS_MENU_TAG))
            composeRule.runOnIdle { generation++ }
            composeRule.waitForIdle()
            assertFalse("a fresh screen before the next touch", shown(MAP_QUICK_SETTINGS_MENU_TAG))
        }
    }

    @Test fun `at ROTATION_90 real touches across the three-dot button open the menu`() = assertButtonOpensMenu(Surface.ROTATION_90)

    @Test fun `at ROTATION_270 real touches across the three-dot button open the menu`() = assertButtonOpensMenu(Surface.ROTATION_270)

    // ── The map's long-press reaches through around the join ──

    private fun assertLongPressesAroundJoin(rotation: Int) {
        setScreen(rotation)
        val centre = centreOf(bounds(MAP_TAG))
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        val clickable = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
            .map { with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) } }
        // The bar is a Surface and takes every touch on itself; the strip takes touches only on its coordinates and its button.
        val notMap = clickable + bar
        val stripSide = if (barOnLeft(rotation)) 1 else -1
        val points = buildList {
            for (dx in listOf(-12, -4, 0, 4, 12)) {
                add(centre + dx.dp to bar.bottom + 4.dp)
                add(centre + dx.dp to bar.bottom + 16.dp)
            }
            // On the strip itself beside the join: its compass needle's end at ROTATION_90 (map); its button at 270 (skipped).
            for (dx in listOf(2, 6, 10)) add(centre + (stripSide * dx).dp to (strip.top + strip.bottom) / 2)
        }
        var sampled = 0
        val skipped = mutableListOf<String>()
        for ((x, y) in points) {
            if (notMap.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) {
                skipped += "(${x.value}, ${y.value})"
                continue
            }
            val before = longPresses
            val p = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
            composeRule.onRoot().performTouchInput { longClick(p) }
            composeRule.waitForIdle()
            assertEquals("a long-press at (${x.value}, ${y.value}) beside the join must reach the map", before + 1, longPresses)
            sampled++
        }
        println("MEASURED rotation $rotation: long-presses sampled $sampled, skipped on controls $skipped")
        assertTrue("at least 10 points below the join were sampled, not $sampled", sampled >= 10)
        if (barOnLeft(rotation)) assertTrue("at ROTATION_90 the strip's end at the join was sampled too ($sampled)", sampled >= 12)
    }

    @Test fun `at ROTATION_90 long-presses around the join reach the map`() = assertLongPressesAroundJoin(Surface.ROTATION_90)

    @Test fun `at ROTATION_270 long-presses around the join reach the map`() = assertLongPressesAroundJoin(Surface.ROTATION_270)

    // ── The readouts are whole, in both coordinate formats ──

    private fun layoutOf(tag: String): Pair<String, TextLayoutResult> {
        val node = composeRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text } ?: ""
        return text to results.single()
    }

    private fun readoutsShown(): List<String> {
        val shownTags = listOf(COMPASS_STRIP_HEADING_TAG, COMPASS_STRIP_ELEVATION_TAG, COMPASS_STRIP_COORDINATES_TAG).filter { shown(it) }
        return shownTags.map { tag ->
            val (text, layout) = layoutOf(tag)
            assertEquals("$tag <$text> is one line", 1, layout.lineCount)
            assertFalse("$tag <$text> is whole, not ellipsised", layout.isLineEllipsized(0))
            assertEquals("$tag <$text> shows every character", text.length, layout.getLineEnd(0, visibleEnd = true))
            text
        }
    }

    private fun assertReadoutsWhole(rotation: Int) {
        setScreen(rotation)
        assertTrue("positive control: the coordinates are shown", shown(COMPASS_STRIP_COORDINATES_TAG))
        val mgrs = readoutsShown()
        val coordinates = bounds(COMPASS_STRIP_COORDINATES_TAG)
        composeRule.touchAt(centreOf(coordinates), (coordinates.top + coordinates.bottom) / 2)
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        val decimal = readoutsShown()
        println("MEASURED rotation $rotation strip readouts: MGRS $mgrs; decimal $decimal")
        assertTrue("positive control: the touch switched the format (${mgrs.last()} to ${decimal.last()})", mgrs.last() != decimal.last())
        assertTrue("the decimal coordinates read as decimal: ${decimal.last()}", decimal.last().contains("45.52"))
    }

    @Test fun `at ROTATION_90 the strip's readouts are whole in both coordinate formats`() = assertReadoutsWhole(Surface.ROTATION_90)

    @Test fun `at ROTATION_270 the strip's readouts are whole in both coordinate formats`() = assertReadoutsWhole(Surface.ROTATION_270)

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
        const val MAP_TAG = "landscape-bar-strip-map"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeBarStripJoin780Test : LandscapeBarStripJoinTests()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeBarStripJoin823Test : LandscapeBarStripJoinTests()
