package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
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
 * Dispatch 2026-10-12-06 (RECORD -934's "the pill and card touch edge to edge", and the planner's call in RECORD -936: "in
 * landscape the card still touches the bar edge to edge"): navigating in a short landscape window, the search bar's inner
 * edge is the navigation card's inner edge, no gap, at both rotations, and long-presses beside the bar, below it, reach the
 * map. Before this dispatch the bar stopped at its 384 dp cap and left a gap of tens of dp beside the display.
 */
abstract class LandscapeCardMeetsBarTests {
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
    private val start = Waypoint(id = "origin", lat = 45.53, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private var longPresses = 0
    private var rotationSeen: Int? = null
    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag("landscape-card-map").pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }

    private fun setScreen(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            AvailabilityScreen(
                uiState = AvailabilityUiState(liveFix = fix),
                isRecording = true,
                isReturning = true,
                navigationTarget = start,
                returnRoute = ReturnRoute.Ahead(LatLng(45.525, -122.68), 900.0),
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
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeen)
    }

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun longPressAt(x: Dp, y: Dp) {
        val at = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput { down(at); advanceEventTime(700); up() }
        composeRule.waitForIdle()
    }

    private fun inAnyControl(x: Dp, y: Dp): Boolean = composeRule.onAllNodes(androidx.compose.ui.test.hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
        .any { n -> with(composeRule.density) { x.toPx() >= n.boundsInRoot.left && x.toPx() <= n.boundsInRoot.right && y.toPx() >= n.boundsInRoot.top && y.toPx() <= n.boundsInRoot.bottom } }

    private fun assertMeets(rotation: Int) {
        setScreen(rotation)
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val card = bounds(NAVIGATION_HUD_TAG)
        println("MEASURED rotation $rotation: bar $bar, card $card")
        val barOnLeft = rotation == Surface.ROTATION_90
        if (barOnLeft) {
            assertEquals("the bar's right edge is the card's left edge: bar $bar, card $card", card.left.value, bar.right.value, 0.5f)
        } else {
            assertEquals("the bar's left edge is the card's right edge: bar $bar, card $card", card.right.value, bar.left.value, 0.5f)
        }
        assertEquals("the two start at the same top", card.top.value, bar.top.value, 0.5f)
        // Below the bar, beside the taller card, nearer the card than the cluster on the bar's outer side: the map's.
        val x = if (barOnLeft) card.left - 12.dp else card.right + 12.dp
        var sampled = 0
        for (y in listOf(bar.bottom + 4.dp, bar.bottom + 20.dp)) {
            if (y >= card.bottom || inAnyControl(x, y)) continue
            val before = longPresses
            longPressAt(x, y)
            assertEquals("a long-press at (${x.value}, ${y.value}) below the bar, beside the card, reached the map", before + 1, longPresses)
            sampled++
        }
        assertTrue("sampled below the bar: $sampled", sampled >= 1)
    }

    @Test fun `at ROTATION_90 navigating, the bar meets the card edge to edge`() = assertMeets(Surface.ROTATION_90)

    @Test fun `at ROTATION_270 navigating, the bar meets the card edge to edge`() = assertMeets(Surface.ROTATION_270)

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeCardMeetsBar780Test : LandscapeCardMeetsBarTests()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeCardMeetsBar823Test : LandscapeCardMeetsBarTests()
