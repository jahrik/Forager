package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
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
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
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

/**
 * Dispatch 2026-09-28-677 (data part B; the owner, RECORD -656, "Plain words + labels (Recommended)"): the navigation display
 * and the compass strip in words and with labels, through the real [AvailabilityScreen] while returning (the display) and
 * while not (the strip).
 *
 * The dispatch's own checks: at 360 dp, with long strings, in landscape, each line must fit, whole, and the display must not
 * grow. "Long" is chosen, not typical: the longest turn ("Sharp right · 169°"), a four-digit route in feet with "by trail",
 * the status line's straight line at its longest feet form, a four-digit altitude in feet, and an MGRS reference. Every line
 * is read through its own laid-out text ([SemanticsActions.GetTextLayoutResult]): not ellipsised, every character visible,
 * and the box as wide as the text needs, as `AvailabilityScreenReturnRouteTest` reads "Unable to calculate route".
 *
 * Native graphics, as that class: without it Robolectric measures text at a few dp wide and every fit would pass on fake
 * widths. The display's heights are Robolectric's, which reports no status bar; on the S22 the display sits ~30 dp lower,
 * and its 1.5 dp clearance from the central third (2026-10-07 day check) is a device item.
 *
 * Geometry: the fix at 45.52 N, 122.68 W, 3,000 m up (9843 ft); the start due north at 380 m (straight "≈ 1250 ft" with 12.5 m
 * accuracy); the route's lookahead due east, so a device facing 281° turns 169° to the right to it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvailabilityScreenNavigationWordsTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 3_000.0, accuracyMeters = 12.5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.52 + 380.0 / 111_195.0, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val east = LatLng(45.52, -122.679)

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

    private var route by mutableStateOf<ReturnRoute?>(null)

    private fun setScreen(facing: Float, route: ReturnRoute?, navigating: Boolean = true) {
        this.route = route
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(liveFix = fix),
                isRecording = navigating,
                isReturning = navigating,
                navigationTarget = start.takeIf { navigating },
                returnRoute = this.route ?: ReturnRoute.Pending,
                compassProvider = FixedCompass(facing),
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
        composeRule.waitForIdle()
    }

    private fun node(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun textOf(tag: String): String = node(tag).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    /** The label node reading [label] inside [parentTag]. */
    private fun label(parentTag: String, label: String): SemanticsNodeInteraction =
        composeRule.onNode(hasTestTag(NAVIGATION_LABEL_TAG) and hasText(label) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)

    private fun bounds(interaction: SemanticsNodeInteraction): DpRect = interaction.getUnclippedBoundsInRoot()

    /** [interaction]'s one line is whole: not ellipsised, every character drawn, and its box as wide as the text needs. */
    private fun assertWhole(name: String, interaction: SemanticsNodeInteraction) {
        val results = mutableListOf<TextLayoutResult>()
        interaction.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val layout = results.single()
        val text = layout.layoutInput.text.text
        val box = with(composeRule.density) { interaction.fetchSemanticsNode().boundsInRoot.width.toDp() }
        val needs = with(composeRule.density) { layout.multiParagraph.maxIntrinsicWidth.toDp() }
        println("MEASURED $name <$text>: box $box, needs $needs, visible ${layout.getLineEnd(0, visibleEnd = true)} of ${text.length}")
        assertFalse("$name <$text>: not ellipsised", layout.isLineEllipsized(0))
        assertEquals("$name <$text>: every character visible", text.length, layout.getLineEnd(0, visibleEnd = true))
        assertTrue("$name <$text>: box $box holds the text's $needs", box >= needs - 0.5.dp)
    }

    /** Every line of the display, labels included, is whole. */
    private fun assertDisplayWhole() {
        listOf(
            "distance" to NAVIGATION_HUD_DISTANCE_TAG,
            "distance kind" to NAVIGATION_HUD_DISTANCE_KIND_TAG,
            "turn" to NAVIGATION_HUD_TARGET_TAG,
            "status" to NAVIGATION_HUD_STATUS_TAG,
            "heading" to NAVIGATION_HUD_HEADING_TAG,
            "elevation" to NAVIGATION_HUD_ELEVATION_TAG,
            "coordinates" to NAVIGATION_HUD_COORDINATES_TAG,
        ).forEach { (name, tag) -> assertWhole(name, node(tag)) }
        assertWhole("heading label", label(NAVIGATION_HUD_TAG, HEADING_LABEL))
        assertWhole("altitude label", label(NAVIGATION_HUD_TAG, ALTITUDE_LABEL))
    }

    @Test
    fun `on the route the display reads the turn in words, the route by trail, the straight line straight, and labels its heading and altitude`() {
        setScreen(facing = 45f, route = ReturnRoute.Ahead(east, 1_500.0))

        assertEquals("Right · 45°", textOf(NAVIGATION_HUD_TARGET_TAG))
        assertEquals("0.9 mi", textOf(NAVIGATION_HUD_DISTANCE_TAG))
        assertEquals("by trail", textOf(NAVIGATION_HUD_DISTANCE_KIND_TAG))
        assertEquals("≈ 1250 ft straight", textOf(NAVIGATION_HUD_STATUS_TAG))
        assertEquals("45° NE", textOf(NAVIGATION_HUD_HEADING_TAG))
        assertEquals("9843 ft", textOf(NAVIGATION_HUD_ELEVATION_TAG))
        // Each label sits before its own reading, on its line.
        val facing = bounds(label(NAVIGATION_HUD_TAG, HEADING_LABEL))
        val heading = bounds(node(NAVIGATION_HUD_HEADING_TAG))
        assertTrue("\"Facing\" $facing is left of the heading $heading", facing.right <= heading.left)
        val alt = bounds(label(NAVIGATION_HUD_TAG, ALTITUDE_LABEL))
        val elevation = bounds(node(NAVIGATION_HUD_ELEVATION_TAG))
        assertTrue("\"Alt\" $alt is left of the altitude $elevation", alt.right <= elevation.left)
        // The kind follows the figure on the same line.
        val figure = bounds(node(NAVIGATION_HUD_DISTANCE_TAG))
        val kind = bounds(node(NAVIGATION_HUD_DISTANCE_KIND_TAG))
        assertTrue("the kind $kind follows the figure $figure", kind.left >= figure.right)
        assertTrue("the kind $kind is on the figure's line $figure", kind.top < figure.bottom && kind.bottom > figure.top)
    }

    @Test
    fun `at 360 dp the longest lines fit whole`() {
        setScreen(facing = 281f, route = ReturnRoute.Ahead(east, 390.0))

        assertEquals("Sharp right · 169°", textOf(NAVIGATION_HUD_TARGET_TAG))
        assertEquals("1280 ft", textOf(NAVIGATION_HUD_DISTANCE_TAG))
        assertEquals("281° W", textOf(NAVIGATION_HUD_HEADING_TAG))
        assertDisplayWhole()
    }

    /**
     * The words and labels do not grow the display: with the longest lines it is exactly as tall as with short ones. The second
     * row was already there with a fix; the labels sit on its line and the kind on the figure's.
     */
    @Test
    fun `the longest lines leave the display as tall as short ones`() {
        setScreen(facing = 281f, route = ReturnRoute.Ahead(east, 390.0))
        val long = bounds(node(NAVIGATION_HUD_TAG)).let { it.bottom - it.top }
        // Before the first route: a dash in the large slot, no kind, no turn; the same second row.
        composeRule.runOnIdle { route = ReturnRoute.Pending }
        composeRule.waitForIdle()
        assertEquals("—", textOf(NAVIGATION_HUD_DISTANCE_TAG))
        assertEquals("", textOf(NAVIGATION_HUD_TARGET_TAG))
        val short = bounds(node(NAVIGATION_HUD_TAG)).let { it.bottom - it.top }
        println("MEASURED display height: long lines $long, short lines $short")
        assertEquals("the display's height with the longest lines", short.value, long.value, 0.5f)
    }

    /** A real touch at the centre of [interaction], as a finger switches the coordinate format. */
    private fun touchCentre(interaction: SemanticsNodeInteraction) {
        interaction.performTouchInput { click(center) }
        composeRule.waitForIdle()
    }

    /**
     * Amendment 1 (RECORD -680; the owner: "Shorter decimals, no labels (Recommended)"): the decimal pair reads
     * "45.5200, -122.6800" and, with the longest lines, fits whole at 360 dp, as MGRS does. The tap still switches.
     */
    @Test
    fun `at 360 dp the display's decimal coordinates fit whole beside the longest lines`() {
        setScreen(facing = 281f, route = ReturnRoute.Ahead(east, 390.0))
        touchCentre(node(NAVIGATION_HUD_COORDINATES_TAG))
        assertEquals("45.5200, -122.6800", textOf(NAVIGATION_HUD_COORDINATES_TAG))
        assertDisplayWhole()
        touchCentre(node(NAVIGATION_HUD_COORDINATES_TAG))
        assertEquals("a second tap switches back to MGRS", coordinatesStripText(LatLng(fix.lat, fix.lng), false), textOf(NAVIGATION_HUD_COORDINATES_TAG))
    }

    @Test
    fun `at 360 dp the strip's decimal coordinates fit whole beside its labels`() {
        setScreen(facing = 315f, route = null, navigating = false)
        val mgrs = coordinatesStripText(LatLng(fix.lat, fix.lng), false)
        touchCentre(composeRule.onNode(hasText(mgrs) and hasAnyAncestor(hasTestTag(STRIP_TAG)), useUnmergedTree = true))
        val decimal = composeRule.onNode(hasText("45.5200, -122.6800") and hasAnyAncestor(hasTestTag(STRIP_TAG)), useUnmergedTree = true)
        assertWhole("strip decimal coordinates", decimal)
        assertWhole("strip heading", node(COMPASS_STRIP_HEADING_TAG))
        assertWhole("strip elevation", node(COMPASS_STRIP_ELEVATION_TAG))
    }

    @Test
    fun `at 360 dp the strip's labelled line fits whole`() {
        setScreen(facing = 315f, route = null, navigating = false)

        assertEquals("315° NW", textOf(COMPASS_STRIP_HEADING_TAG))
        assertEquals("9843 ft", textOf(COMPASS_STRIP_ELEVATION_TAG))
        assertWhole("strip heading", node(COMPASS_STRIP_HEADING_TAG))
        assertWhole("strip elevation", node(COMPASS_STRIP_ELEVATION_TAG))
        assertWhole("strip heading label", label(STRIP_TAG, HEADING_LABEL))
        assertWhole("strip altitude label", label(STRIP_TAG, ALTITUDE_LABEL))
        assertWhole("strip coordinates", composeRule.onNode(hasText(coordinatesStripText(LatLng(fix.lat, fix.lng), false)) and hasAnyAncestor(hasTestTag(STRIP_TAG)), useUnmergedTree = true))
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
    }
}

/**
 * The same display in a short landscape window, the S22's 780 x 360 dp: the display is capped at 360 dp wide on the rail
 * side, so its lines must fit as at 360 dp portrait, and it must stay clear of the window's central third. The 1.5 dp
 * clearance the S22 showed (2026-10-07 day check) includes a status bar Robolectric does not report; that margin is a device
 * item, and this asserts only that the display ends above the third here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AvailabilityScreenNavigationWordsLandscapeTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 3_000.0, accuracyMeters = 12.5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.52 + 380.0 / 111_195.0, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val east = LatLng(45.52, -122.679)

    @Test
    fun `in landscape the longest lines fit whole and the display ends above the central third`() {
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(liveFix = fix),
                isRecording = true,
                isReturning = true,
                navigationTarget = start,
                returnRoute = ReturnRoute.Ahead(east, 390.0),
                compassProvider = object : CompassProvider {
                    override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(281f, HeadingUncertainty.Estimated(2f), 0L))
                },
                computeTrueHeading = ComputeTrueHeadingUseCase(object : DeclinationProvider {
                    override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
                }),
                currentTime = CurrentTimeProvider { t + 1_000L },
                mapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) },
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
        composeRule.waitForIdle()

        val node = { tag: String -> composeRule.onNodeWithTag(tag, useUnmergedTree = true) }
        assertEquals("Sharp right · 169°", node(NAVIGATION_HUD_TARGET_TAG).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text })
        val assertAllWhole = { format: String ->
            listOf(
                NAVIGATION_HUD_DISTANCE_TAG, NAVIGATION_HUD_DISTANCE_KIND_TAG, NAVIGATION_HUD_TARGET_TAG, NAVIGATION_HUD_STATUS_TAG,
                NAVIGATION_HUD_HEADING_TAG, NAVIGATION_HUD_ELEVATION_TAG, NAVIGATION_HUD_COORDINATES_TAG,
            ).forEach { tag ->
                val interaction = node(tag)
                val results = mutableListOf<TextLayoutResult>()
                interaction.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
                val layout = results.single()
                val text = layout.layoutInput.text.text
                println("MEASURED landscape ($format) $tag <$text>: visible ${layout.getLineEnd(0, visibleEnd = true)} of ${text.length}")
                // Dispatch 2026-09-28-685, Amendment 2 (RECORD -699; the owner: "Yes, everywhere (Recommended)"): the display
                // is never wider than the room beside the search bar, at every font scale, so at this width a line may end in
                // "…". It was "not ellipsised, every character visible"; "by trail" now shows 5 of 8 here. Still one line,
                // and whole wherever it is not ellipsised; the display must not overlap the search bar (below).
                assertEquals("$format, $tag <$text>: one line", 1, layout.lineCount)
                if (!layout.isLineEllipsized(0)) {
                    assertEquals("$format, $tag <$text>: every character visible", text.length, layout.getLineEnd(0, visibleEnd = true))
                }
            }
            val hudBounds = node(NAVIGATION_HUD_TAG).getUnclippedBoundsInRoot()
            val barBounds = node(SEARCH_ENTRY_BAR_TAG).getUnclippedBoundsInRoot()
            assertFalse(
                "$format: the display $hudBounds must not overlap the search bar $barBounds",
                hudBounds.left < barBounds.right && barBounds.left < hudBounds.right && hudBounds.top < barBounds.bottom && barBounds.top < hudBounds.bottom,
            )
        }
        assertAllWhole("MGRS")
        // Amendment 1 (RECORD -680): the decimal pair, switched by a real touch, fits whole here too.
        node(NAVIGATION_HUD_COORDINATES_TAG).performTouchInput { click(center) }
        composeRule.waitForIdle()
        assertEquals("45.5200, -122.6800", node(NAVIGATION_HUD_COORDINATES_TAG).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text })
        assertAllWhole("decimal")
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val hud = node(NAVIGATION_HUD_TAG).getUnclippedBoundsInRoot()
        val thirdTop = root.top + (root.bottom - root.top) / 3
        println("MEASURED landscape display: $hud, central third from ${thirdTop.value} dp, clearance ${(thirdTop - hud.bottom).value} dp")
        assertTrue("the display $hud ends above the central third (from $thirdTop)", hud.bottom <= thirdTop)
    }
}
