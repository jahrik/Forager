package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTouchInput
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
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MapSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
 * Dispatch 2026-10-12-06 (RECORD -932, -934): the rounded pill, the navigation card tucked under it and the compass strip, on
 * the real screen, with real touches at screen coordinates (CLAUDE.md: a semantic click asserts wiring, not routing; and a
 * finger is not a point, so each target is touched at several points across its own bounds).
 *
 * - The card's controls take touches across them: the X, the three-dot button, the grid reference and Try again.
 * - The X sits above the three-dot button at the card's right edge (the owner: "Have the X button go above the three dot menu
 *   in the top corner. The three dot menu can go in the bottom corner").
 * - The map keeps everything else: the card's rounded lower corners, just below the card, the strip's corners, and the pill's
 *   own rounded corners (the pill is a `Surface`, which takes touches; it must take them only inside its rounded shape).
 * - The pill's lower band, where the card's tuck lies beneath it, is still the pill's: a long-press there does not reach the
 *   map, though the card above it in z-order is drawn there.
 * Every sample is counted and the count asserted, so an empty sample cannot pass.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RoundedMapCardTouchTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 50.0, accuracyMeters = 12.5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.53, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)

    private var returning by mutableStateOf(true)
    private var route by mutableStateOf<ReturnRoute>(ReturnRoute.Unavailable(canRetry = true))
    private var generation by mutableIntStateOf(0)
    private var exits = 0
    private var retries = 0
    private var longPresses = 0

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }

    private fun setScreen() {
        composeRule.setContent {
            key(generation) {
                AvailabilityScreen(
                    uiState = AvailabilityUiState(liveFix = fix),
                    isRecording = true,
                    isReturning = returning,
                    onToggleReturning = { exits++ },
                    navigationTarget = start,
                    returnRoute = route,
                    onRetryRoute = { retries++ },
                    compassProvider = FixedCompass(45f),
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
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
    }

    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun bounds(tag: String): DpRect = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().first().let { n ->
        with(composeRule.density) { DpRect(n.boundsInRoot.left.toDp(), n.boundsInRoot.top.toDp(), n.boundsInRoot.right.toDp(), n.boundsInRoot.bottom.toDp()) }
    }

    private fun textOf(tag: String): String =
        composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().first().config[SemanticsProperties.Text].joinToString { it.text }

    private fun at(x: Dp, y: Dp) = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }

    private fun touchAt(x: Dp, y: Dp) {
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput { down(at(x, y)); up() }
        composeRule.waitForIdle()
    }

    private fun longPressAt(x: Dp, y: Dp) {
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput {
            down(at(x, y))
            advanceEventTime(700)
            up()
        }
        composeRule.waitForIdle()
    }

    /** The centre and four points 3 dp inside each corner of [r]. */
    private fun across(r: DpRect): List<Pair<Dp, Dp>> {
        val i = 3.dp
        return listOf(
            (r.left + r.right) / 2 to (r.top + r.bottom) / 2,
            r.left + i to r.top + i,
            r.right - i to r.top + i,
            r.left + i to r.bottom - i,
            r.right - i to r.bottom - i,
        )
    }

    private fun controls(): List<DpRect> = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
        .filterNot { it.config.getOrNull(SemanticsProperties.TestTag) == MAP_TAG }
        .map { with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) } }

    private fun inAny(x: Dp, y: Dp, rects: List<DpRect>) = rects.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }

    // ── The card's controls ──

    @Test
    fun `real touches across the X stop navigating, at every point sampled`() {
        setScreen()
        val points = across(bounds(NAVIGATION_HUD_EXIT_TAG))
        for ((x, y) in points) {
            val before = exits
            touchAt(x, y)
            assertEquals("a real touch at (${x.value}, ${y.value}) on the X stopped navigating", before + 1, exits)
        }
        assertEquals(5, points.size)
    }

    @Test
    fun `the X sits above the three-dot button at the card's right edge, and real touches across the button open its menu`() {
        setScreen()
        val exit = bounds(NAVIGATION_HUD_EXIT_TAG)
        val button = bounds(MAP_QUICK_SETTINGS_BUTTON_TAG)
        val card = bounds(NAVIGATION_HUD_TAG)
        assertTrue("the X $exit is above the button $button", exit.bottom <= button.top + 0.5.dp)
        assertTrue("the X is in the card's top right: $exit in $card", card.right - exit.right < 12.dp && exit.top - card.top < 12.dp)
        assertTrue("the button is at the card's right: $button in $card", card.right - button.right < 18.dp)
        var sampled = 0
        for ((x, y) in across(button)) {
            touchAt(x, y)
            assertTrue("a real touch at (${x.value}, ${y.value}) on the three-dot button opened the menu", shown(MAP_QUICK_SETTINGS_MENU_TAG))
            composeRule.runOnIdle { generation++ }
            composeRule.waitForIdle()
            sampled++
        }
        assertEquals(5, sampled)
    }

    @Test
    fun `real touches across the grid reference toggle it each time`() {
        setScreen()
        val r = bounds(NAVIGATION_HUD_COORDINATES_TAG)
        var last = textOf(NAVIGATION_HUD_COORDINATES_TAG)
        var sampled = 0
        for ((x, y) in across(r)) {
            touchAt(x, y)
            val now = textOf(NAVIGATION_HUD_COORDINATES_TAG)
            assertNotEquals("a real touch at (${x.value}, ${y.value}) on the grid reference toggled it", last, now)
            last = now
            sampled++
        }
        assertEquals(5, sampled)
    }

    @Test
    fun `Try again sits on the line under the columns, beside its message, and real touches across it retry`() {
        setScreen()
        val retry = bounds(NAVIGATION_HUD_RETRY_TAG)
        val needle = bounds(NAVIGATION_HUD_NEEDLE_TAG)
        assertTrue("Try again $retry is under the columns (needle $needle)", retry.top >= needle.bottom - 0.5.dp)
        assertEquals("the message is on its line", "Unable to calculate route", textOf(NAVIGATION_HUD_DISTANCE_TAG))
        val message = bounds(NAVIGATION_HUD_DISTANCE_TAG)
        assertTrue("beside it, on one line: message $message, Try again $retry", message.right <= retry.left && message.bottom > retry.top && message.top < retry.bottom)
        var sampled = 0
        for ((x, y) in across(retry)) {
            val before = retries
            touchAt(x, y)
            assertEquals("a real touch at (${x.value}, ${y.value}) on Try again retried", before + 1, retries)
            sampled++
        }
        assertEquals(5, sampled)
        composeRule.runOnIdle { route = ReturnRoute.Ahead(LatLng(45.52, -122.679), 1_500.0) }
        composeRule.waitForIdle()
        assertTrue("Try again goes once a route is found", !shown(NAVIGATION_HUD_RETRY_TAG))
    }

    // ── The map keeps the rest ──

    @Test
    fun `long-presses at the card's rounded lower corners and just below it reach the map`() {
        setScreen()
        val card = bounds(NAVIGATION_HUD_TAG)
        val taken = controls()
        val points = listOf(
            card.left + 2.dp to card.bottom - 2.dp,
            card.right - 2.dp to card.bottom - 2.dp,
            card.left + 4.dp to card.bottom - 1.dp,
            card.right - 4.dp to card.bottom - 1.dp,
        ) + listOf(20.dp, 90.dp, 180.dp, 270.dp, 340.dp).flatMap { x -> listOf(x to card.bottom + 3.dp, x to card.bottom + 12.dp) }
        var sampled = 0
        for ((x, y) in points) {
            if (inAny(x, y, taken)) continue
            val before = longPresses
            longPressAt(x, y)
            assertEquals("a long-press at (${x.value}, ${y.value}) by the card $card reached the map", before + 1, longPresses)
            sampled++
        }
        assertTrue("sampled enough points: $sampled", sampled >= 10)
    }

    @Test
    fun `long-presses at the pill's rounded corners reach the map, and its lower band over the tuck stays the pill's`() {
        setScreen()
        val pill = bounds(SEARCH_ENTRY_BAR_TAG)
        var sampled = 0
        for ((x, y) in listOf(pill.left + 2.dp to pill.top + 2.dp, pill.right - 2.dp to pill.top + 2.dp, pill.left + 3.dp to pill.top + 1.dp, pill.right - 3.dp to pill.top + 1.dp)) {
            val before = longPresses
            longPressAt(x, y)
            assertEquals("a long-press at (${x.value}, ${y.value}) in the pill's rounded corner reached the map", before + 1, longPresses)
            sampled++
        }
        assertEquals(4, sampled)
        // The pill's lower band, where the card's tuck lies beneath it: the pill takes it, the map does not.
        for (x in listOf(60.dp, 180.dp, 300.dp)) {
            val before = longPresses
            longPressAt(x, pill.bottom - 4.dp)
            assertEquals("a long-press at (${x.value}, ${(pill.bottom - 4.dp).value}) on the pill's lower band stayed the pill's", before, longPresses)
        }
    }

    @Test
    fun `not navigating, long-presses at the strip's rounded corners and just below it reach the map`() {
        returning = false
        setScreen()
        val strip = bounds(STRIP_TAG)
        val taken = controls()
        val points = listOf(
            strip.left + 2.dp to strip.bottom - 2.dp,
            strip.right - 2.dp to strip.bottom - 2.dp,
        ) + listOf(20.dp, 120.dp, 240.dp, 340.dp).map { x -> x to strip.bottom + 4.dp }
        var sampled = 0
        for ((x, y) in points) {
            if (inAny(x, y, taken)) continue
            val before = longPresses
            longPressAt(x, y)
            assertEquals("a long-press at (${x.value}, ${y.value}) by the strip $strip reached the map", before + 1, longPresses)
            sampled++
        }
        assertTrue("sampled enough points: $sampled", sampled >= 5)
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
        const val MAP_TAG = "rounded-card-map"
    }
}
