package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import androidx.compose.runtime.snapshots.Snapshot
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.TrueHeadingReading
import com.zynergylabs.forager.app.ui.theme.ForagerTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Motion Part 2, Amendment 1 (RECORD -672), item 2 (scout N6): the navigation display's sundown line fades and grows like the
 * strip's. The display is caught growing and shrinking between its two heights; under reduced motion (the transition scale alone
 * at 0, so Compose's own animations still run) it takes the line's height at once.
 *
 * Nothing here has been run: written with the code, before the build.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class HudSundownLineGrowTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val resolver get() = ApplicationProvider.getApplicationContext<Application>().contentResolver

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 50.0, accuracyMeters = 8f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.53, lng = -122.68, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)

    private var line by mutableStateOf<String?>(null)

    @After
    fun restore() {
        composeRule.mainClock.autoAdvance = true
        Settings.Global.putFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    }

    private fun show() {
        composeRule.setContent {
            ForagerTheme {
                val heading = remember { mutableStateOf<TrueHeadingReading>(TrueHeadingReading.Available(80f)) }
                NavigationHud(
                    heading = heading,
                    liveFix = fix,
                    target = start,
                    distanceUnit = DistanceUnit.MILES,
                    currentTime = CurrentTimeProvider { t + 1_000L },
                    showDecimalDegrees = false,
                    onToggleCoordinateFormat = {},
                    onExit = {},
                    modifier = Modifier.fillMaxWidth(),
                    sundownLine = line,
                )
            }
        }
        settle()
    }

    private fun settle() {
        composeRule.mainClock.autoAdvance = true
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
    }

    private fun hudHeight(): Dp = composeRule.onNodeWithTag(NAVIGATION_HUD_TAG).getUnclippedBoundsInRoot().let { it.bottom - it.top }

    private fun changeLine(next: String?) {
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnUiThread { line = next; Snapshot.sendApplyNotifications() }
        repeat(3) { composeRule.mainClock.advanceTimeByFrame() }
    }

    @Test
    fun `the display grows to hold the line when it opens, and shrinks when it closes`() {
        show()
        val short = hudHeight()
        changeLine(SUNDOWN)
        val growing = hudHeight()
        assertTrue("the line is drawn", composeRule.onAllNodesWithTag(NAVIGATION_HUD_SUNDOWN_LINE_TAG).fetchSemanticsNodes().isNotEmpty())
        settle()
        val tall = hudHeight()
        assertTrue("taller with the line: $short to $tall", tall > short + 4.dp)
        assertTrue("caught growing: $growing between $short and $tall", growing > short + 0.5.dp && growing < tall - 0.5.dp)

        changeLine(null)
        val shrinking = hudHeight()
        assertTrue("caught shrinking: $shrinking between $short and $tall", shrinking > short + 0.5.dp && shrinking < tall - 0.5.dp)
        settle()
        assertEquals("back to its height without the line", short.value, hudHeight().value, 0.5f)
    }

    @Test
    fun `under reduced motion the display takes the line's height at once`() {
        Settings.Global.putFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 0f)
        show()
        changeLine(SUNDOWN)
        val first = hudHeight()
        settle()
        assertEquals("no grow under reduced motion", hudHeight().value, first.value, 0.5f)
    }

    private companion object {
        const val SUNDOWN = "Sunset 7:42 PM · start back by 7:12 PM"
    }
}
