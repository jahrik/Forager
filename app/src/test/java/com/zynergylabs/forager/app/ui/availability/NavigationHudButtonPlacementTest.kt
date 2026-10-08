package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.TrueHeadingReading
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

import org.junit.Assert.assertFalse

/**
 * RECORD -714 (the owner: "Button moves to second row (Recommended)"): where the navigation display's three-dot button sits,
 * and that real touches across its 36 dp square, corners included, open the quick menu there. At 360 dp the first row cannot
 * give data part B's longest route line its full width beside the button (firstRowHoldsTheButton), so the button ends the
 * second row; at 384 dp (the S22) it stays left of the X. The display is composed afresh between touches, because under
 * Robolectric neither Back nor a touch elsewhere reaches the menu's popup window.
 */
abstract class NavigationHudButtonPlacementTests(private val inSecondRow: Boolean) {
    private val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }
    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val now = 1_791_050_400_000L
    private val start = Waypoint("origin", 45.53, -122.68, null, "Start", "", now - 3_600_000L, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val fix = LocationFix.Update(lat = start.lat + 380.0 / 111_195.08, lng = start.lng, altitude = 120.0, accuracyMeters = 3.79f, timestampEpochMillis = now, provider = FixProvider.GPS)
    private var generation by mutableStateOf(0)

    private fun bounds(tag: String) = composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().single().let { n ->
        with(composeRule.density) { androidx.compose.ui.unit.DpRect(n.boundsInRoot.left.toDp(), n.boundsInRoot.top.toDp(), n.boundsInRoot.right.toDp(), n.boundsInRoot.bottom.toDp()) }
    }

    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `the button sits where the measured fit puts it, and real touches across it open the menu`() {
        composeRule.setContent {
            androidx.compose.runtime.key(generation) {
                NavigationHud(
                    heading = mutableStateOf<TrueHeadingReading>(TrueHeadingReading.Available(11f)),
                    liveFix = fix,
                    target = start,
                    distanceUnit = DistanceUnit.MILES,
                    currentTime = CurrentTimeProvider { now },
                    showDecimalDegrees = false,
                    onToggleCoordinateFormat = {},
                    onExit = {},
                    route = ReturnRoute.Ahead(LatLng(start.lat, start.lng), 390.0),
                    quickSettings = MapQuickSettings(isRecording = true, backBy = null, onBackByChoice = {}, onClearBackBy = {}, sundown = SundownSettings(), offTrackReminder = OffTrackReminderSettings()),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        composeRule.waitForIdle()
        val button = bounds(MAP_QUICK_SETTINGS_BUTTON_TAG)
        val exit = bounds(NAVIGATION_HUD_EXIT_TAG)
        val hud = bounds(NAVIGATION_HUD_TAG)
        assertEquals("36 dp wide: $button", 36f, (button.right - button.left).value, 0.5f)
        assertEquals("36 dp tall: $button", 36f, (button.bottom - button.top).value, 0.5f)
        if (inSecondRow) {
            assertTrue("below the X's row: button $button, X $exit", button.top >= (exit.top + exit.bottom) / 2 + 24.dp - 0.5.dp)
            assertTrue("at the second row's end: button $button in $hud", hud.right - button.right <= 8.5.dp)
            assertEquals("the status line is whole beside the X alone", "≈ 1250 ft straight", composeRule.onAllNodesWithTag(NAVIGATION_HUD_STATUS_TAG, useUnmergedTree = true).fetchSemanticsNodes().single().config[androidx.compose.ui.semantics.SemanticsProperties.Text].joinToString { it.text })
        } else {
            assertTrue("left of the X: button $button, X $exit", button.right <= exit.left + 0.5.dp)
            assertTrue("in the X's row: button $button, X $exit", button.top < exit.bottom && button.bottom > exit.top)
        }
        val inset = 3.dp
        val points = listOf(
            (button.left + button.right) / 2 to (button.top + button.bottom) / 2,
            button.left + inset to button.top + inset,
            button.right - inset to button.top + inset,
            button.left + inset to button.bottom - inset,
            button.right - inset to button.bottom - inset,
        )
        for ((x, y) in points) {
            composeRule.touchAt(x, y)
            assertTrue("a real touch at (${x.value}, ${y.value}) in $button opened the menu", shown(MAP_QUICK_SETTINGS_MENU_TAG))
            composeRule.runOnIdle { generation++ }
            composeRule.waitForIdle()
            assertFalse("a fresh display before the next touch", shown(MAP_QUICK_SETTINGS_MENU_TAG))
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationHudButtonPlacement360Test : NavigationHudButtonPlacementTests(inSecondRow = true)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationHudButtonPlacement384Test : NavigationHudButtonPlacementTests(inSecondRow = false)
