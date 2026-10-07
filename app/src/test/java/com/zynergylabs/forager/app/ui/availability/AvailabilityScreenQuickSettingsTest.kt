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
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.BackByChoice
import com.zynergylabs.forager.app.domain.BackByShown
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.JournalMenuContainerColor
import com.zynergylabs.forager.app.ui.map.JournalMenuContentColor
import com.zynergylabs.forager.app.ui.map.MAP_CHROME_OVER_MAP_ALPHA
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

/**
 * The map's quick settings on the real [AvailabilityScreen] with the real [AvailabilityViewModel]
 * (dispatch 2026-09-28-645, Amendments 1 and 2, RECORD -646 and -647): the gear at the strip's end and
 * in the navigation display, opened by real touches across its square; the menu's three sections,
 * driven by real touches; its sundown and off-track rows changing the same values Settings shows; Back
 * by's line in the strip's last hour; and map long-presses still reaching the map around the gear
 * (CLAUDE.md, the `Surface` pitfall).
 *
 * Back by's choices are asserted as what the screen hands `MainActivity` ([BackByChoice]); what the
 * recording ViewModel does with one is `TrackRecordingBackByTest`'s. Where the gear sits against the
 * real top inset is device-only (CLAUDE.md: Robolectric reports zero insets).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class AvailabilityScreenQuickSettingsTest {

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
    private val now = 1_791_050_400_000L // 2026-10-03T18:00:00Z
    private val origin = Waypoint("origin", 45.31, -122.634, null, "Start", "", 500L, trackId = "t1", designation = WaypointDesignation.ORIGIN)

    private val utc = TimeZone.getTimeZone("UTC")
    private val clock = object : SundownClock {
        override fun full(epochMillis: Long): String = SimpleDateFormat("h:mm a", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
        override fun short(epochMillis: Long): String = SimpleDateFormat("h:mm", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
    }

    private var recording by mutableStateOf(false)
    private var returning by mutableStateOf(false)
    private var backBy by mutableStateOf<BackByShown?>(null)
    private val choices = mutableListOf<BackByChoice>()
    private var clears = 0
    private var longPresses = 0

    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(modifier.testTag(MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) })
    }

    private lateinit var viewModel: AvailabilityViewModel

    private fun setScreen() {
        viewModel = mapLayersViewModel()
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
                    // The real ViewModel's own Settings callbacks, as MainActivity passes them.
                    onSundownAlertsEnabledChanged = viewModel::onSundownAlertsEnabledChanged,
                    onDarknessMarginChanged = viewModel::onDarknessMarginChanged,
                    onOffTrackReminderChanged = viewModel::onOffTrackReminderChanged,
                    mapSlot = map,
                    isRecording = recording,
                    isReturning = returning,
                    onToggleReturning = { returning = !returning },
                    navigationTarget = if (recording) origin else null,
                    returnRoute = ReturnRoute.Ahead(LatLng(45.32, -122.634), 1_800.0),
                    recordingBackBy = backBy,
                    onBackByChoice = { choices += it },
                    onClearBackBy = { clears++ },
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

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

    private fun touchAt(x: Dp, y: Dp) {
        val at = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput { click(at) }
        composeRule.waitForIdle()
    }

    /** A real touch at the centre of [tag]'s own bounds (a popup's node is reached through the popup's root). */
    private fun touchCentreOf(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput { click(center) }
        composeRule.waitForIdle()
    }

    private fun closeMenu() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        if (shown(MAP_QUICK_SETTINGS_MENU_TAG)) {
            // A popup's own window takes Back; a touch on the map outside it closes it as the owner's path says.
            touchAt(20.dp, 600.dp)
        }
    }

    private fun recordingWithBackBy(at: Long?, publishedAt: Long = now) {
        composeRule.runOnIdle {
            recording = true
            backBy = at?.let { BackByShown("t1", it, publishedAt) }
        }
        composeRule.waitForIdle()
    }

    // ── Where the gear sits (Amendment 2) ──

    /** Q2, "Taller strip (Recommended)": the gear's whole square is inside the strip, at its right end, and nothing hangs over the map. */
    @Test
    fun `portrait, the gear sits at the strip's right end, its whole square inside the strip`() {
        setScreen()
        val strip = bounds(STRIP_TAG)
        val gear = bounds(MAP_QUICK_SETTINGS_GEAR_TAG)
        assertEquals("a finger-sized square: $gear", 48f, (gear.bottom - gear.top).value, 0.5f)
        assertEquals("a finger-sized square: $gear", 48f, (gear.right - gear.left).value, 0.5f)
        assertTrue("inside the strip, top to bottom: $gear in $strip", gear.top >= strip.top - 0.5.dp && gear.bottom <= strip.bottom + 0.5.dp)
        assertTrue("at the strip's right end: $gear in $strip", strip.right - gear.right < 1.dp)
    }

    /** Landscape with the rail on the left: the strip is in the top-left corner, and its inner end is its right end. */
    @Test
    @Config(qualifiers = "w823dp-h384dp-land-xxhdpi")
    fun `landscape, the gear sits at the strip's inner end, inside it`() {
        setScreen()
        val strip = bounds(STRIP_TAG)
        val gear = bounds(MAP_QUICK_SETTINGS_GEAR_TAG)
        val root = composeRule.onAllNodes(isRoot()).onFirst().getUnclippedBoundsInRoot()
        val stripIsLeft = strip.left - root.left < root.right - strip.right
        if (stripIsLeft) {
            assertTrue("rail on the left: the gear at the strip's right, inner, end: $gear in $strip", strip.right - gear.right < 1.dp)
        } else {
            assertTrue("rail on the right: the gear at the strip's left, inner, end: $gear in $strip", gear.left - strip.left < 1.dp)
        }
        assertTrue("inside the strip, top to bottom: $gear in $strip", gear.top >= strip.top - 0.5.dp && gear.bottom <= strip.bottom + 0.5.dp)
    }

    // ── Real touches on the gear ──

    /** A finger is not a point (CLAUDE.md): touches sampled across the gear's square, corners included, each open the menu. */
    @Test
    fun `real touches across the gear's square each open the menu`() {
        setScreen()
        val gear = bounds(MAP_QUICK_SETTINGS_GEAR_TAG)
        val inset = 4.dp
        val points = listOf(
            (gear.left + gear.right) / 2 to (gear.top + gear.bottom) / 2,
            gear.left + inset to gear.top + inset,
            gear.right - inset to gear.top + inset,
            gear.left + inset to gear.bottom - inset,
            gear.right - inset to gear.bottom - inset,
        )
        for ((x, y) in points) {
            touchAt(x, y)
            assertTrue("a real touch at (${x.value}, ${y.value}) in $gear opened the menu", shown(MAP_QUICK_SETTINGS_MENU_TAG))
            closeMenu()
            assertFalse("closed before the next touch", shown(MAP_QUICK_SETTINGS_MENU_TAG))
        }
    }

    /**
     * Around the gear the map still takes long-presses: points in the strip's own row beside the
     * gear, and just under the strip below it, sampled and counted. The gear and the coordinates are
     * controls; everything else in the strip takes no touch.
     */
    @Test
    fun `long-presses around the gear and just under the strip reach the map`() {
        setScreen()
        recordingWithBackBy(now + 30 * minute)
        val strip = bounds(STRIP_TAG)
        val gear = bounds(MAP_QUICK_SETTINGS_GEAR_TAG)
        val controls = composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().map {
            with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) }
        }
        var sampled = 0
        val ys = listOf(strip.top + 3.dp, (strip.top + strip.bottom) / 2, strip.bottom - 3.dp, strip.bottom + 6.dp)
        val xs = listOf(gear.left - 6.dp, gear.left - 30.dp, gear.left - 80.dp, (gear.left + gear.right) / 2, strip.left + 40.dp)
        for (y in ys) {
            for (x in xs) {
                val inGear = x >= gear.left && x <= gear.right && y >= gear.top && y <= gear.bottom
                if (inGear) continue
                if (controls.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) continue
                val before = longPresses
                val p = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
                composeRule.onAllNodes(isRoot()).onFirst().performTouchInput { longClick(p) }
                composeRule.waitForIdle()
                assertEquals("a long-press at (${x.value}, ${y.value}), gear $gear, strip $strip, must reach the map", before + 1, longPresses)
                sampled++
            }
        }
        assertTrue("the row under the gear, just below the strip, is sampled", sampled >= 4)
        assertTrue("at least $MIN_SAMPLED points sampled, not $sampled", sampled >= MIN_SAMPLED)
    }

    // ── The menu ──

    @Test
    fun `the menu is at the map chrome's opacity, its content opaque`() {
        setScreen()
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)
        val node = composeRule.onNodeWithTag(MAP_QUICK_SETTINGS_MENU_TAG).fetchSemanticsNode()
        assertEquals("container alpha", MAP_CHROME_OVER_MAP_ALPHA, node.config[JournalMenuContainerColor].alpha, 0.001f)
        assertEquals("content alpha", 1f, node.config[JournalMenuContentColor].alpha, 0.001f)
    }

    @Test
    fun `not recording, Back by says how to get one and offers no choice`() {
        setScreen()
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)
        assertEquals(BACK_BY_NOT_RECORDING_TEXT, text(QUICK_BACK_BY_NOT_RECORDING_TAG))
        assertFalse(shown(quickBackByHoursTag(1)))
        assertFalse(shown(QUICK_BACK_BY_PICK_TAG))
        assertTrue("Sundown is there without a recording", shown(QUICK_SUNDOWN_ALERTS_TAG))
        assertTrue("and Off-track", shown(QUICK_OFF_TRACK_TAG))
    }

    @Test
    fun `recording, real touches on +1 h, +2 h and +3 h hand those choices on, and the menu stays open`() {
        setScreen()
        recordingWithBackBy(null)
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)
        for (hours in listOf(1, 2, 3)) {
            touchCentreOf(quickBackByHoursTag(hours))
            assertTrue("the menu stays open after +$hours h (Q3)", shown(MAP_QUICK_SETTINGS_MENU_TAG))
        }
        assertEquals(listOf(BackByChoice.HoursFromNow(1), BackByChoice.HoursFromNow(2), BackByChoice.HoursFromNow(3)), choices)
    }

    @Test
    fun `Pick a time opens the clock picker over the map at 80 percent, and Set hands on the picked time`() {
        setScreen()
        recordingWithBackBy(null)
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)
        touchCentreOf(QUICK_BACK_BY_PICK_TAG)
        assertTrue(shown(BACK_BY_TIME_PICKER_TAG))
        val dialog = composeRule.onNodeWithTag(BACK_BY_TIME_PICKER_TAG).fetchSemanticsNode()
        assertEquals("dialog alpha", MAP_CHROME_OVER_MAP_ALPHA, dialog.config[com.zynergylabs.forager.app.ui.map.MapChromeContainerColor].alpha, 0.001f)
        touchCentreOf(BACK_BY_TIME_PICKER_SET_TAG)
        assertFalse(shown(BACK_BY_TIME_PICKER_TAG))
        assertEquals("one picked time, a clock time", 1, choices.size)
        assertTrue(choices.single() is BackByChoice.AtTime)
    }

    /** Q3, "Menu shows it + dot (Recommended)": with a time set, the menu's top line says it, Clear clears, and the gear has its dot. */
    @Test
    fun `with a time set, the menu's top line shows it, Clear clears it, and the gear carries a dot`() {
        setScreen()
        recordingWithBackBy(null)
        assertFalse("no dot with none set", shown(MAP_QUICK_SETTINGS_DOT_TAG))
        recordingWithBackBy(now + 2 * 60 * minute)
        assertTrue("a dot while set", shown(MAP_QUICK_SETTINGS_DOT_TAG))
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)
        assertEquals("Back by ${clock.full(now + 2 * 60 * minute)}", text(QUICK_BACK_BY_SET_TAG))
        touchCentreOf(QUICK_BACK_BY_CLEAR_TAG)
        assertEquals(1, clears)
    }

    /** The sundown and off-track rows are Settings' own values: a real touch changes the ViewModel's state, which Settings reads. */
    @Test
    fun `the Sundown and Off-track rows change the same values Settings shows`() {
        setScreen()
        assertTrue(viewModel.uiState.value.sundownAlertsEnabled)
        assertTrue(viewModel.uiState.value.offTrackReminderEnabled)
        touchCentreOf(MAP_QUICK_SETTINGS_GEAR_TAG)

        touchCentreOf(QUICK_SUNDOWN_ALERTS_TAG)
        assertFalse("Sundown alerts off", viewModel.uiState.value.sundownAlertsEnabled)
        touchCentreOf(quickDarknessMarginTag(90))
        assertEquals("Dark under trees: 1 h 30", 90, viewModel.uiState.value.darknessMarginMinutes)
        touchCentreOf(QUICK_OFF_TRACK_TAG)
        assertFalse("Off-track reminder off", viewModel.uiState.value.offTrackReminderEnabled)
    }

    // ── The line (Amendment 1) ──

    @Test
    fun `the strip shows Back by under the sunset line only in the last hour`() {
        setScreen()
        recordingWithBackBy(now + 61 * minute)
        assertFalse("61 min away: no line", shown(STRIP_BACK_BY_LINE_TAG))
        recordingWithBackBy(now + 60 * minute)
        assertEquals("Back by ${clock.full(now + 60 * minute)}", text(STRIP_BACK_BY_LINE_TAG))
        recordingWithBackBy(now - 5 * minute)
        assertEquals("passed and still set: the line stays", "Back by ${clock.full(now - 5 * minute)}", text(STRIP_BACK_BY_LINE_TAG))
        recordingWithBackBy(null)
        assertFalse("cleared: no line", shown(STRIP_BACK_BY_LINE_TAG))
    }

    /** Q1, "Gear in nav display too (Recommended)": navigating, the strip is hidden; the gear and the line are in the HUD, and a real touch opens the menu. */
    @Test
    fun `navigating, the gear and the line move into the navigation display`() {
        setScreen()
        recordingWithBackBy(now + 20 * minute)
        composeRule.runOnIdle { returning = true }
        composeRule.waitForIdle()
        assertFalse("the strip is hidden", shown(STRIP_TAG))
        assertEquals("Back by ${clock.full(now + 20 * minute)}", text(NAVIGATION_HUD_BACK_BY_LINE_TAG))
        val hud = bounds(NAVIGATION_HUD_TAG)
        val gear = bounds(MAP_QUICK_SETTINGS_GEAR_TAG)
        assertTrue("the gear is in the HUD: $gear in $hud", gear.top >= hud.top && gear.bottom <= hud.bottom && gear.left >= hud.left && gear.right <= hud.right)
        touchAt((gear.left + gear.right) / 2, (gear.top + gear.bottom) / 2)
        assertTrue(shown(MAP_QUICK_SETTINGS_MENU_TAG))
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val MAP_TAG = "quick-settings-map"
        const val STRIP_TAG = "compass-elevation-strip"
        const val MIN_SAMPLED = 10
    }
}
