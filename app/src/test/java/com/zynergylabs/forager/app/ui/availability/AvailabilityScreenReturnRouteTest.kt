package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MapSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-423 (plan task T6): the HUD follows the route, read on the real screen.
 * [AvailabilityScreen] is composed while returning with each [ReturnRoute] state `MainActivity`
 * can pass it (`returnRouteOf(trackUiState.routeHome)`), and the HUD's own text is read back.
 * That the ViewModel produces those states, through Return, fixes and the polled track, is
 * `TrackRecordingViewModelTest`'s; `MainActivity`'s one line joining the two is not exercised by a
 * test, as the path-home line it replaces was not.
 *
 * The fix sits at 45.52 N, 122.68 W; the start is 0.01° of latitude due north of it (1112 m,
 * "0.7 mi"); the device faces 45° true (a trusted 45° and no declination). The route's lookahead
 * is due east, a 45° turn, so a needle aimed at the start (a 315° turn) cannot pass for it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class AvailabilityScreenReturnRouteTest {

    private val composeRule = createComposeRule()

    // As AvailabilityScreenLandscapeB2Test: the rule's host activity must be declared to the
    // package manager before the rule launches it.
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
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 50.0, accuracyMeters = 12.5f, timestampEpochMillis = t)
    private val start = Waypoint(id = "origin", lat = 45.53, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val east = LatLng(45.52, -122.679)

    private var route by mutableStateOf<ReturnRoute?>(ReturnRoute.Pending)
    private var retries = 0
    private var longPresses = 0

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }

    private fun setScreen(initial: ReturnRoute?) {
        route = initial
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(liveFix = fix),
                isRecording = true,
                isReturning = true,
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
        composeRule.waitForIdle()
    }

    private fun textOfTag(tag: String): String =
        composeRule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    private fun boundsOf(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

    @Test
    fun `on the route the large figure is the route distance, the status line the straight line, and the needle turns to the lookahead`() {
        setScreen(ReturnRoute.Ahead(east, 1_500.0))

        assertEquals("0.9 mi", textOfTag(NAVIGATION_HUD_DISTANCE_TAG))
        assertEquals("Straight line 0.7 mi", textOfTag(NAVIGATION_HUD_STATUS_TAG))
        assertEquals("Turn 45°", textOfTag(NAVIGATION_HUD_TARGET_TAG))
    }

    @Test
    fun `before the first route the large slot is a dash and the status line the straight line`() {
        setScreen(ReturnRoute.Pending)

        assertEquals("—", textOfTag(NAVIGATION_HUD_DISTANCE_TAG))
        assertEquals("Straight line 0.7 mi", textOfTag(NAVIGATION_HUD_STATUS_TAG))
        assertEquals("", textOfTag(NAVIGATION_HUD_TARGET_TAG))
    }

    @Test
    fun `a withheld route reads Unable to calculate route in the large slot, with the straight line kept`() {
        setScreen(ReturnRoute.Unavailable(canRetry = false))

        assertTrue(textOfTag(NAVIGATION_HUD_DISTANCE_TAG).startsWith("Unable to calculate route"))
        assertEquals("Straight line 0.7 mi", textOfTag(NAVIGATION_HUD_STATUS_TAG))
        assertEquals("", textOfTag(NAVIGATION_HUD_TARGET_TAG))
    }

    /**
     * No new surface: the HUD is the same height on the route, pending and withheld, so nothing it
     * draws in the withheld state reaches further over the map than it did. The map-chrome fill at
     * 80% is the HUD's existing one (`MapChromeAlphaTest`); nothing new is drawn over the map.
     */
    @Test
    fun `the HUD keeps its height in every route state`() {
        setScreen(ReturnRoute.Ahead(east, 1_500.0))
        val onRoute = boundsOf(NAVIGATION_HUD_TAG)
        listOf(ReturnRoute.Pending, ReturnRoute.Unavailable(canRetry = true), ReturnRoute.Unavailable(canRetry = false)).forEach { state ->
            composeRule.runOnIdle { route = state }
            composeRule.waitForIdle()
            val b = boundsOf(NAVIGATION_HUD_TAG)
            assertEquals("the HUD's height with $state", (onRoute.bottom - onRoute.top).value, (b.bottom - b.top).value, 0.5f)
        }
    }

    /**
     * CLAUDE.md, the `Surface` pitfall: real long-presses on the map just below the HUD, across its
     * width, reach the map with "Try again" offered. A point inside any clickable control (the icon
     * cluster) is skipped, and the number sampled is asserted, so an empty sample cannot pass.
     */
    @Test
    fun `with Try again offered, real long-presses on the map below the HUD reach the map`() {
        setScreen(ReturnRoute.Unavailable(canRetry = true))
        val hud = boundsOf(NAVIGATION_HUD_TAG)
        val controls = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().map {
            with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) }
        }
        var sampled = 0
        for (row in listOf(6.dp, 20.dp)) {
            val y = hud.bottom + row
            for (i in 0 until SAMPLES) {
                val x = hud.left + 8.dp + (hud.right - hud.left - 16.dp) * (i.toFloat() / (SAMPLES - 1))
                if (controls.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) continue
                val before = longPresses
                val p = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
                composeRule.onRoot().performTouchInput { longClick(p) }
                composeRule.waitForIdle()
                assertEquals("a long-press at ($x, $y) must reach the map", before + 1, longPresses)
                sampled++
            }
        }
        assertTrue("at least $MIN_SAMPLED points were sampled, not $sampled", sampled >= MIN_SAMPLED)
        assertEquals("no long-press on the map asked for a new route", 0, retries)
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val MAP_TAG = "return-route-map"
        const val SAMPLES = 8
        const val MIN_SAMPLED = 8
    }
}
