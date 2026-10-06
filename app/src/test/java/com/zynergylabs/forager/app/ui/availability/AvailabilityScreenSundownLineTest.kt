package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeSundownCountdownUseCase
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.LocationTracker
import com.zynergylabs.forager.app.domain.SundownLine
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.SundownCountdown
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import org.robolectric.annotation.Config
import org.robolectric.Shadows

/**
 * The sundown line on the real [AvailabilityScreen] with the real [AvailabilityViewModel] (dispatch
 * 2026-09-28-592, plan task T3; placement RECORD -421, "Strip, then HUD"; Amendments 1 and 2, RECORD
 * -593, -595 and -596): in the strip while recording, in the HUD once Return or Navigate is tapped,
 * sunset and dark only in the HUD when navigating with no recording, hidden until its window opens,
 * and never "finding your position". The recording's line is what `MainActivity` hands the screen
 * from `TrackRecordingUiState.sundownLine`; how that reaches the ViewModel is
 * `TrackRecordingSundownTest`'s.
 *
 * Where the line sits against the real top inset in portrait, landscape and fullscreen is device-only
 * (CLAUDE.md: Robolectric reports zero insets).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class AvailabilityScreenSundownLineTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val minute = 60_000L
    private val creek = Waypoint("wp-1", 45.326, -122.634, null, "Creek pin", "", 1_000L)
    private val origin = Waypoint("origin", 45.31, -122.634, null, "Start", "", 500L, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val morning = 1_791_010_800_000L // 2026-10-03T07:00:00Z
    private val sunset = (ComputeSundownCountdownUseCase()(morning, LatLng(creek.lat, creek.lng), morning, 0L) as SundownCountdown.Known).sunsetAtEpochMillis
    private val dusk = (ComputeSundownCountdownUseCase()(morning, LatLng(creek.lat, creek.lng), morning, 0L) as SundownCountdown.Known).civilDuskAtEpochMillis!!

    private var now = sunset - 70 * minute

    private val utc = TimeZone.getTimeZone("UTC")
    private val clock = object : SundownClock {
        override fun full(epochMillis: Long): String = SimpleDateFormat("h:mm a", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
        override fun short(epochMillis: Long): String = SimpleDateFormat("h:mm", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
    }

    private val fixes = MutableSharedFlow<LocationFix>(replay = 1)
    private val tracker = object : LocationTracker {
        override val fixes: Flow<LocationFix> = this@AvailabilityScreenSundownLineTest.fixes
    }

    private var line by mutableStateOf<SundownLine?>(null)
    private var recording by mutableStateOf(false)
    private var returning by mutableStateOf(false)
    private var longPresses = 0

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }

    private lateinit var viewModel: AvailabilityViewModel

    private fun setScreen() {
        viewModel = mapLayersViewModel(locationTracker = tracker)
        viewModel.onEnteredForeground()
        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            CompositionLocalProvider(LocalSundownClock provides clock) {
                AvailabilityScreen(
                    uiState = state,
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
                    mapSlot = map,
                    waypoints = listOf(creek),
                    waypointsLoaded = true,
                    isRecording = recording,
                    isReturning = returning,
                    onToggleReturning = { returning = !returning },
                    navigationTarget = if (recording) origin else null,
                    returnRoute = ReturnRoute.Ahead(LatLng(45.32, -122.634), 1_800.0),
                    onNavigateToWaypoint = viewModel::onNavigateToWaypoint,
                    onStopWaypointNavigation = viewModel::onStopWaypointNavigation,
                    recordingSundownLine = line,
                    compassProvider = FixedCompass(0f),
                    computeTrueHeading = ComputeTrueHeadingUseCase(NoDeclination),
                    currentTime = CurrentTimeProvider { now },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun text(tag: String): String =
        composeRule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    private fun anyTextContains(words: String) = composeRule.onAllNodesWithText(words, substring = true).fetchSemanticsNodes().isNotEmpty()

    private fun recordingWith(newLine: SundownLine) {
        composeRule.runOnIdle {
            recording = true
            line = newLine
        }
        composeRule.waitForIdle()
    }

    private val startBack = sunset - 70 * minute

    private fun startBackLine(at: Long = startBack - 20 * minute) = SundownLine.BeforeSunset(at, sunset, dusk, startBack)

    private fun expected(at: Long = startBack - 20 * minute) = "Sunset ${clock.full(sunset)} · start back by ${clock.short(startBack)}"

    // ── Strip, then HUD ──

    @Test
    fun `recording, the strip shows the line`() {
        setScreen()
        recordingWith(startBackLine())
        assertEquals(expected(), text(STRIP_SUNDOWN_LINE_TAG))
        assertFalse("no HUD", shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `not recording and not navigating, no line anywhere`() {
        setScreen()
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
        assertFalse(shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `tapping Return moves the line into the HUD, and the strip with it goes`() {
        setScreen()
        recordingWith(startBackLine())
        composeRule.runOnIdle { returning = true }
        composeRule.waitForIdle()
        assertEquals(expected(), text(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `Navigate while recording shows the recording's line in the HUD`() {
        setScreen()
        recordingWith(startBackLine())
        viewModel.onNavigateToWaypoint(creek.id)
        composeRule.waitForIdle()
        assertEquals(expected(), text(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `Navigate with no recording shows sunset and dark only, computed on screen, with no start-back time`() {
        setScreen()
        fixes.tryEmit(LocationFix.Update(creek.lat - 0.005, creek.lng, 50.0, 3.79f, now, provider = FixProvider.GPS))
        composeRule.waitForIdle()
        viewModel.onNavigateToWaypoint(creek.id)
        composeRule.waitForIdle()

        val words = text(NAVIGATION_HUD_SUNDOWN_LINE_TAG)
        // The fix is 550 m south of the creek, which moves sunset by seconds, so the minute is matched loosely.
        assertTrue(words, Regex("Sunset \\d{1,2}:\\d{2} (AM|PM) · in 1 h (9|10) min · dark \\d{1,2}:\\d{2}").matches(words))
        assertFalse("never a start-back time without a recording: $words", words.contains("start back"))
    }

    /** RECORD -596, the owner: "Yes hide it before a position is known". */
    @Test
    fun `navigating with no recording and no position, no line, and finding your position never renders`() {
        setScreen()
        viewModel.onNavigateToWaypoint(creek.id)
        composeRule.waitForIdle()
        assertTrue("the HUD is up", shown(NAVIGATION_HUD_TAG))
        assertFalse(shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
        assertFalse(anyTextContains("finding your position"))
    }

    @Test
    fun `recording with no position yet, no line in the strip, and finding your position never renders`() {
        setScreen()
        recordingWith(SundownLine.FindingPosition)
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
        assertFalse(anyTextContains("finding your position"))
        composeRule.runOnIdle { returning = true }
        composeRule.waitForIdle()
        assertFalse("nor in the HUD", shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
        assertFalse(anyTextContains("finding your position"))
    }

    // ── Every state of the path, in the strip ──

    @Test
    fun `each state of the owner's path reads as confirmed in the strip`() {
        setScreen()
        recordingWith(startBackLine(at = startBack + 5 * minute))
        assertEquals("Sunset ${clock.full(sunset)} · start back was ${clock.short(startBack)}", text(STRIP_SUNDOWN_LINE_TAG))

        recordingWith(SundownLine.BeforeSunset(sunset - 72 * minute, sunset, dusk, null))
        assertEquals("Sunset ${clock.full(sunset)} · in 1 h 12 min · dark ${clock.short(dusk)}", text(STRIP_SUNDOWN_LINE_TAG))

        recordingWith(SundownLine.AfterSunset(dusk - 21 * minute, sunset, dusk))
        assertEquals("Sun set ${clock.full(sunset)} · dark in 21 min", text(STRIP_SUNDOWN_LINE_TAG))

        recordingWith(SundownLine.DarkSince(dusk))
        assertEquals("Dark since ${clock.full(dusk)}", text(STRIP_SUNDOWN_LINE_TAG))
    }

    // ── Amendment 2 (RECORD -595): the window, on screen ──

    @Test
    fun `hidden at 2 h 31 min before sunset, shown at 2 h 29 min`() {
        setScreen()
        recordingWith(SundownLine.BeforeSunset(sunset - 151 * minute, sunset, dusk, null))
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
        recordingWith(SundownLine.BeforeSunset(sunset - 149 * minute, sunset, dusk, null))
        assertEquals("Sunset ${clock.full(sunset)} · in 2 h 29 min · dark ${clock.short(dusk)}", text(STRIP_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `shown early with the start-back time 59 min away, hidden at 61 min with sunset further than 2 h 30 min`() {
        setScreen()
        val early = sunset - 4 * 60 * minute
        recordingWith(SundownLine.BeforeSunset(early - 61 * minute, sunset, dusk, early))
        assertFalse(shown(STRIP_SUNDOWN_LINE_TAG))
        recordingWith(SundownLine.BeforeSunset(early - 59 * minute, sunset, dusk, early))
        assertEquals("Sunset ${clock.full(sunset)} · start back by ${clock.short(early)}", text(STRIP_SUNDOWN_LINE_TAG))
    }

    @Test
    fun `the window holds in the HUD too`() {
        setScreen()
        recordingWith(SundownLine.BeforeSunset(sunset - 151 * minute, sunset, dusk, null))
        composeRule.runOnIdle { returning = true }
        composeRule.waitForIdle()
        assertFalse(shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
        recordingWith(SundownLine.BeforeSunset(sunset - 149 * minute, sunset, dusk, null))
        assertTrue(shown(NAVIGATION_HUD_SUNDOWN_LINE_TAG))
    }

    // ── Touch (CLAUDE.md, the Surface pitfall) ──

    /**
     * Real long-presses at points sampled across the line's own bounds in the strip, and in a row just
     * under the strip, reach the map: the line takes no touches, and the strip around it took none
     * before. The number sampled is asserted, so an empty sample cannot pass.
     */
    @Test
    fun `real long-presses on the line and just below the strip reach the map`() {
        setScreen()
        recordingWith(startBackLine())
        val lineBounds = composeRule.onNodeWithTag(STRIP_SUNDOWN_LINE_TAG).getUnclippedBoundsInRoot()
        val strip = composeRule.onNodeWithTag("compass-elevation-strip").getUnclippedBoundsInRoot()
        assertTrue("the line is inside the strip: $lineBounds in $strip", lineBounds.top >= strip.top && lineBounds.bottom <= strip.bottom + 0.5.dp)

        val controls = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().map {
            with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) }
        }
        var sampled = 0
        val rows = listOf((lineBounds.top + lineBounds.bottom) / 2, lineBounds.top + 2.dp, lineBounds.bottom - 2.dp, strip.bottom + 6.dp)
        for (y in rows) {
            for (i in 0 until SAMPLES) {
                val x = lineBounds.left + 8.dp + (lineBounds.right - lineBounds.left - 16.dp) * (i.toFloat() / (SAMPLES - 1))
                // A clickable control (the strip's coordinates above, the icon cluster below) is not the map; skipped and counted.
                if (controls.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) continue
                val before = longPresses
                val p = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
                composeRule.onRoot().performTouchInput { longClick(p) }
                composeRule.waitForIdle()
                assertEquals("a long-press at ($x, $y), line $lineBounds, must reach the map", before + 1, longPresses)
                sampled++
            }
        }
        assertTrue("at least ${'$'}MIN_SAMPLED points sampled, not ${'$'}sampled", sampled >= MIN_SAMPLED)
        assertTrue("every point on the line itself is sampled (no control sits on it)", sampled >= 3 * SAMPLES)
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val MAP_TAG = "sundown-line-map"
        const val SAMPLES = 5
        const val MIN_SAMPLED = 16
    }
}
