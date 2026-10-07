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

/**
 * The navigation display's text columns with the quick-settings gear in its top row (dispatch
 * 2026-09-28-645, Amendment 2; the planner: the gear left of the exit, and "check the narrowed column at
 * 360 dp with 'Unable to calculate route' and the longest status strings"). On a 360 dp-wide window,
 * each state's large figure and status line are laid out whole, with the gear and, for the record,
 * without it, so a clip the gear did not cause is told apart from one it did.
 *
 * Native graphics, as `AvailabilityScreenSundownLineTest` reads its line: Robolectric's legacy text
 * metrics measure a character as one pixel wide, so nothing could be cut off there. The real phone's
 * font and its font-scale setting are device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NavigationHudGearWidthTest {

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

    private fun fixAt(metresNorth: Double, at: Long = now) =
        LocationFix.Update(lat = start.lat + metresNorth / 111_195.08, lng = start.lng, altitude = 120.0, accuracyMeters = 3.79f, timestampEpochMillis = at, provider = FixProvider.GPS)

    private class Case(
        val name: String,
        val liveFix: LocationFix.Update?,
        val target: Waypoint?,
        val route: ReturnRoute?,
        val approximateFix: LocationFix.Update? = null,
        val lastKnownFix: LocationFix.Update? = null,
    )

    private val cases = listOf(
        Case("Unable to calculate route", fixAt(1_500.0), start, ReturnRoute.Unavailable(canRetry = true)),
        Case("Straight line, a long distance", fixAt(15_000.0), start, ReturnRoute.Ahead(LatLng(start.lat, start.lng), 18_000.0)),
        Case("No origin waypoint for this track", fixAt(1_500.0), null, null),
        Case("Location services unavailable", null, start, ReturnRoute.Pending),
        Case("Approaching · last fix 4 min ago", fixAt(50.0, at = now - 4 * 60_000L - 30_000L), start, ReturnRoute.Ahead(LatLng(start.lat, start.lng), 60.0)),
        Case("Last seen 23 h ago, finding GPS…", null, start, ReturnRoute.Pending, lastKnownFix = fixAt(1_500.0, at = now - 23 * 3_600_000L)),
        Case("Approximate, finding GPS…", null, start, ReturnRoute.Pending, approximateFix = fixAt(1_500.0).copy(accuracyMeters = 60f, provider = FixProvider.NETWORK)),
    )

    private val quickSettings = MapQuickSettings(
        isRecording = true,
        backBy = null,
        onBackByChoice = {},
        onClearBackBy = {},
        sundown = SundownSettings(),
        offTrackReminder = OffTrackReminderSettings(),
    )

    private var case by mutableStateOf<Case?>(null)
    private var withGear by mutableStateOf(true)

    private fun layoutOf(tag: String): TextLayoutResult? {
        val nodes = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
        if (nodes.isEmpty()) return null
        val results = mutableListOf<TextLayoutResult>()
        nodes.single().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    /** `null` when [tag]'s text is laid out whole on its one line; else what was cut. */
    private fun clipped(tag: String): String? {
        val layout = layoutOf(tag) ?: return null
        val words = layout.layoutInput.text.text
        if (words.isEmpty()) return null
        val visibleEnd = layout.getLineEnd(0, visibleEnd = true)
        val whole = visibleEnd == words.length && !layout.isLineEllipsized(0) && layout.lineCount == 1
        return if (whole) null else "'$words' shows ${words.take(visibleEnd)}"
    }

    private fun measure(gear: Boolean): Map<String, List<String>> {
        val found = mutableMapOf<String, List<String>>()
        for (c in cases) {
            composeRule.runOnIdle {
                case = c
                withGear = gear
            }
            composeRule.waitForIdle()
            found[c.name] = listOfNotNull(clipped(NAVIGATION_HUD_DISTANCE_TAG), clipped(NAVIGATION_HUD_STATUS_TAG))
        }
        return found
    }

    @Test
    fun `at 360 dp the narrowed column still lays out every large figure and status line whole`() {
        composeRule.setContent {
            val c = case ?: return@setContent
            NavigationHud(
                heading = mutableStateOf<TrueHeadingReading>(TrueHeadingReading.Available(10f)),
                liveFix = c.liveFix,
                approximateFix = c.approximateFix,
                lastKnownFix = c.lastKnownFix,
                target = c.target,
                distanceUnit = DistanceUnit.MILES,
                currentTime = CurrentTimeProvider { now },
                showDecimalDegrees = false,
                onToggleCoordinateFormat = {},
                onExit = {},
                route = c.route,
                quickSettings = if (withGear) quickSettings else null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val without = measure(gear = false)
        val with = measure(gear = true)
        assertEquals("every case measured", cases.size, with.size)
        val causedByGear = with.filter { (name, cut) -> cut.isNotEmpty() && without[name].orEmpty().isEmpty() }
        val clippedBoth = with.filter { (name, cut) -> cut.isNotEmpty() && without[name].orEmpty().isNotEmpty() }
        assertTrue(
            "clipped with the gear and not without it: $causedByGear; clipped either way (not the gear's): $clippedBoth",
            causedByGear.isEmpty() && clippedBoth.isEmpty(),
        )
    }
}
