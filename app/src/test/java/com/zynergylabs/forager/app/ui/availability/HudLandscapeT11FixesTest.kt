package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.BackByShown
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.MapIconClusterPlacement
import com.zynergylabs.forager.app.domain.SundownLine
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.UnitSystem
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MAP_ICON_BAR_TAG
import com.zynergylabs.forager.app.ui.map.MAP_LAYERS_SHEET_TAG
import com.zynergylabs.forager.app.ui.map.MapOverlayContent
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.map.NAVIGATION_VIEW_TOP_PADDING_FRACTION
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * T11 fixes (dispatch 2026-10-09-02, RECORD -766, the owner's answers confirmed in RECORD -768), through the real
 * [AvailabilityScreen] in the two landscape windows the T11 check measured. Guards, not measurements (the measurements are
 * `HudLandscapeT11MeasurementTest`, re-run for the report's before and after).
 *
 * - **Item 1, text that fits** ("Distance first, short status"): the distance is whole at fonts 1.3 and 2.0, the turn words
 *   give way to the bearing or go; the stale warning is whole, long or short; the grid reference is whole at font 2.0.
 * - **Item 2, height** ("Combine lines, allow the rest"): the sundown line and Back by share one line, Back by whole; "Try
 *   again" sits beside its message without making the display taller; the display, which may now reach into the central
 *   third, never covers the walker's dot or the needle.
 * - **Item 3, the icon bar while navigating** ("Right, landscape only" with the pill's return; "Follow the display"; "Slide
 *   as far as it can"): on the display's side the L slides below the display and the pill moves beside "+", and both go back
 *   to where the user put them; on the other side nothing moves; every bar and pill button is reached by real touches sampled
 *   across it, settled and mid-slide; the map's long-press beside the L still reaches the map.
 *
 * The recording side (recording, Return, the route, the sundown line, Back by) is passed as the screen's own parameters, as
 * every landscape test in this package does. Robolectric reports no insets (CLAUDE.md): on the S22 every top edge is ~30 dp
 * lower, so the fit of the L under the display at font 1.0 with an evening line is a device item (2 dp short there, by the
 * T11 report's arithmetic).
 */
abstract class HudLandscapeT11Fixes {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    @After
    fun clockBack() {
        composeRule.mainClock.autoAdvance = true
    }

    // ── Fixture (the T11 harness's, so the states are the ones the report measured) ──

    private val minute = 60_000L
    private val morning = 1_791_050_400_000L
    private val here = LatLng(45.52, -122.68)
    private val metresPerDegreeLat = 111_195.0
    private val origin = Waypoint(
        id = "origin", lat = here.lat + 380.0 / metresPerDegreeLat, lng = here.lng, altitude = null, name = "Start", note = "",
        createdAtEpochMillis = morning, trackId = "t1", designation = WaypointDesignation.ORIGIN,
    )

    /** Due east: a phone facing 281° turns 169° right, "Sharp right · 169°", data part B's longest turn. */
    private val east = LatLng(here.lat, here.lng + 0.001)

    private val utc = TimeZone.getTimeZone("UTC")
    private val clock = object : SundownClock {
        override fun full(epochMillis: Long): String = SimpleDateFormat("h:mm a", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
        override fun short(epochMillis: Long): String = SimpleDateFormat("h:mm", Locale.US).apply { timeZone = utc }.format(Date(epochMillis))
    }
    private val sunset2258 = morning + (4 * 60 + 58) * minute
    private val recordingLine = SundownLine.BeforeSunset(
        nowEpochMillis = sunset2258 - 48 * minute,
        sunsetAtEpochMillis = sunset2258,
        civilDuskAtEpochMillis = sunset2258 + 28 * minute,
        startBackAtEpochMillis = sunset2258 - 10 * minute,
    )

    private class CountingMap {
        var taps = 0
        var longPresses = 0
        var content: MapOverlayContent? = null
        val slot: MapSlot = { _, content, _, _, _, onTap, _, _, modifier ->
            this.content = content
            Box(modifier.testTag(LAYOUT_FIXES_MAP_TAG).pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }, onTap = { taps++; onTap() }) })
        }
    }

    private val map = CountingMap()
    private var returning by mutableStateOf(false)
    private var route by mutableStateOf<ReturnRoute>(ReturnRoute.Ahead(east, 390.0))
    private var line by mutableStateOf<SundownLine?>(null)
    private var backBy by mutableStateOf<BackByShown?>(null)
    private var now by mutableLongStateOf(morning)
    private var fixAt by mutableLongStateOf(morning)
    private var generation by mutableIntStateOf(0)
    private var railSide = false
    private var recordToggles = 0
    private var returnToggles = 0
    private var locates = 0
    private var retries = 0
    private val placementWrites = mutableListOf<MapIconClusterPlacement>()

    private fun setScreen(rotation: Int, clusterOnRailSide: Boolean, navigating: Boolean = true) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        railSide = clusterOnRailSide
        returning = navigating
        composeRule.setContent {
            val nowAtCompose = now
            val currentTime = remember(nowAtCompose) { CurrentTimeProvider { nowAtCompose } }
            val state = AvailabilityUiState(
                region = Region(lat = here.lat, lng = here.lng, radiusKm = 15),
                liveFix = LocationFix.Update(lat = here.lat, lng = here.lng, altitude = 3_000.0, accuracyMeters = 12.5f, timestampEpochMillis = fixAt, provider = FixProvider.GPS),
                unitSystem = UnitSystem.IMPERIAL,
                mapIconClusterPlacement = if (railSide) MapIconClusterPlacement.DEFAULT.copy(landscapeOnPortSide = true) else MapIconClusterPlacement.DEFAULT,
            )
            CompositionLocalProvider(LocalSundownClock provides clock) {
                // A fresh screen per generation: a touch that opens a popup (the Layers sheet) cannot be dismissed under
                // Robolectric (NavigationHudButtonPlacementTest), so each sampled touch gets a screen of its own.
                key(generation) {
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
                        mapSlot = map.slot,
                        isRecording = true,
                        onToggleRecording = { recordToggles++ },
                        isReturning = returning,
                        // Counted only: the test decides when navigation starts and ends.
                        onToggleReturning = { returnToggles++ },
                        onLocateMe = { locates++ },
                        navigationTarget = origin,
                        returnRoute = route,
                        onRetryRoute = { retries++ },
                        recordingSundownLine = line,
                        recordingBackBy = backBy,
                        onMapIconClusterPlacementChanged = { placementWrites += it },
                        compassProvider = object : CompassProvider {
                            override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(281f, HeadingUncertainty.Estimated(2f), 0L))
                        },
                        computeTrueHeading = ComputeTrueHeadingUseCase(object : DeclinationProvider {
                            override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
                        }),
                        currentTime = currentTime,
                    )
                }
            }
        }
        settle()
    }

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(3_000L)
        composeRule.waitForIdle()
    }

    private fun freshScreen(navigating: Boolean) {
        composeRule.runOnIdle { returning = navigating; generation++ }
        settle()
    }

    // ── Reading the screen ──

    private fun nodes(tag: String): List<SemanticsNode> = composeRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes()

    private fun SemanticsNode.dp(): DpRect = with(composeRule.density) {
        DpRect(boundsInRoot.left.toDp(), boundsInRoot.top.toDp(), boundsInRoot.right.toDp(), boundsInRoot.bottom.toDp())
    }

    private fun tag(tag: String): DpRect = nodes(tag).single().dp()

    private fun described(description: String, substring: Boolean = false): DpRect =
        composeRule.onAllNodes(hasContentDescription(description, substring = substring)).fetchSemanticsNodes().single().dp()

    private fun textOf(tag: String): String? = nodes(tag).firstOrNull()?.config?.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }

    private fun window(): DpRect = composeRule.onAllNodes(isRoot()).fetchSemanticsNodes().first().dp()

    /** Whether [tag]'s text is drawn whole: one line, not ellipsised, every character visible. */
    private fun whole(tag: String): Boolean {
        val node = nodes(tag).single()
        val results = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        val layout = results.single()
        val lines = layout.lineCount
        return lines == 1 && !layout.isLineEllipsized(0) && layout.getLineEnd(0, visibleEnd = true) == layout.layoutInput.text.length
    }

    private fun wholeText(tag: String): String = "<${textOf(tag)}>"

    // ── Item 1: text that fits ──

    private fun assertDistanceWholeOnTheWayHome() {
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false)
        assertEquals("positive control: the route home's longest figure", "1280 ft", textOf(NAVIGATION_HUD_DISTANCE_TAG))
        assertTrue("the distance ${wholeText(NAVIGATION_HUD_DISTANCE_TAG)} is drawn whole", whole(NAVIGATION_HUD_DISTANCE_TAG))
        val turn = textOf(NAVIGATION_HUD_TARGET_TAG)
        assertTrue("the turn words are whole, the bearing alone or gone: <$turn>", turn in setOf("Sharp right · 169°", "169°", ""))
        if (!turn.isNullOrEmpty()) assertTrue("what the turn column shows is whole: <$turn>", whole(NAVIGATION_HUD_TARGET_TAG))
        assertEquals("the needle stays", 1, nodes(NAVIGATION_HUD_NEEDLE_TAG).size)
    }

    @Test @Config(fontScale = 1.3f) fun `item 1 at font 1,3 the distance home is whole and the turn words give way`() = assertDistanceWholeOnTheWayHome()

    @Test @Config(fontScale = 2.0f) fun `item 1 at font 2,0 the distance home is whole and the turn words give way`() = assertDistanceWholeOnTheWayHome()

    private fun assertStaleWarningWhole() {
        now = morning + 45_000L
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false)
        val status = textOf(NAVIGATION_HUD_STATUS_TAG)
        assertTrue("the stale warning in its long or short form: <$status>", status == "Last fix 45 s ago" || status == "45 s old")
        assertTrue("the stale warning <$status> is drawn whole", whole(NAVIGATION_HUD_STATUS_TAG))
    }

    @Test fun `item 1 at font 1,0 the stale warning is whole`() = assertStaleWarningWhole()

    @Test @Config(fontScale = 2.0f) fun `item 1 at font 2,0 the stale warning is whole`() = assertStaleWarningWhole()

    @Test @Config(fontScale = 2.0f)
    fun `item 1 at font 2,0 the grid reference is whole beside the distance`() {
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false)
        assertTrue("positive control: the coordinates are shown", nodes(NAVIGATION_HUD_COORDINATES_TAG).isNotEmpty())
        assertTrue("the grid reference ${wholeText(NAVIGATION_HUD_COORDINATES_TAG)} is whole (-699)", whole(NAVIGATION_HUD_COORDINATES_TAG))
        assertTrue("and the distance ${wholeText(NAVIGATION_HUD_DISTANCE_TAG)} is still whole (distance first)", whole(NAVIGATION_HUD_DISTANCE_TAG))
    }

    // ── Item 2: height ──

    @Test
    fun `item 2 the sundown line and Back by share one line, Back by whole, and the display is no taller for Back by`() {
        line = recordingLine
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false)
        val sundownOnly = tag(NAVIGATION_HUD_TAG)
        composeRule.runOnIdle { backBy = BackByShown(trackId = "t1", backByAtEpochMillis = now + 40 * minute, nowEpochMillis = now) }
        settle()
        val both = tag(NAVIGATION_HUD_TAG)
        val sundown = tag(NAVIGATION_HUD_SUNDOWN_LINE_TAG)
        val back = tag(NAVIGATION_HUD_BACK_BY_LINE_TAG)
        assertTrue("positive control: Back by reads <${textOf(NAVIGATION_HUD_BACK_BY_LINE_TAG)}>", textOf(NAVIGATION_HUD_BACK_BY_LINE_TAG)?.startsWith("Back by") == true)
        assertTrue("one line: the sundown line $sundown and Back by $back overlap vertically", sundown.top < back.bottom && back.top < sundown.bottom)
        assertTrue("Back by follows the sundown line on it: $sundown then $back", back.left >= sundown.right - 0.5.dp)
        assertTrue("Back by ${wholeText(NAVIGATION_HUD_BACK_BY_LINE_TAG)} is whole", whole(NAVIGATION_HUD_BACK_BY_LINE_TAG))
        assertEquals("the display is no taller with Back by: $sundownOnly then $both", (sundownOnly.bottom - sundownOnly.top).value, (both.bottom - both.top).value, 0.5f)
        assertTrue("the evening line is inside the display", tag(NAVIGATION_HUD_EVENING_LINE_TAG).let { it.top >= both.top - 0.5.dp && it.bottom <= both.bottom + 0.5.dp })
    }

    @Test
    fun `item 2 Try again sits beside its message, the display is no taller for it, and real touches across it retry`() {
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false)
        val onRoute = tag(NAVIGATION_HUD_TAG)
        composeRule.runOnIdle { route = ReturnRoute.Unavailable(canRetry = true) }
        settle()
        val hud = tag(NAVIGATION_HUD_TAG)
        val retry = tag(NAVIGATION_HUD_RETRY_TAG)
        val message = tag(NAVIGATION_HUD_DISTANCE_TAG)
        assertTrue("positive control: the message reads <${textOf(NAVIGATION_HUD_DISTANCE_TAG)}>", textOf(NAVIGATION_HUD_DISTANCE_TAG) in setOf(ROUTE_UNAVAILABLE_TEXT, ROUTE_UNAVAILABLE_SHORT_TEXT))
        assertTrue("beside: Try again $retry starts right of the message $message", retry.left >= message.right - 0.5.dp)
        assertTrue("beside: Try again $retry is level with the message $message", retry.top < message.bottom && message.top < retry.bottom)
        assertEquals("the display is no taller for Try again: $onRoute then $hud", (onRoute.bottom - onRoute.top).value, (hud.bottom - hud.top).value, 0.5f)
        assertTrue("still at least 48 dp tall: $retry", retry.bottom - retry.top >= 47.5.dp)
        fractions().forEachIndexed { i, (fx, fy) ->
            composeRule.touchAt(retry.left + (retry.right - retry.left) * fx, retry.top + (retry.bottom - retry.top) * fy)
            assertEquals("touch $i at ($fx, $fy) of $retry reached Try again", i + 1, retries)
        }
    }

    /**
     * The owner's exception (RECORD -766: "Combine lines, allow the rest (Recommended)"): the navigating display may reach into
     * the central third, never over the walker's dot or the needle. The worst case the screen can show: font 2.0, the route
     * withheld with "Try again", the sundown line and Back by. The walker's dot is where the navigation view puts it,
     * [NAVIGATION_VIEW_TOP_PADDING_FRACTION] of the map's height as top padding (the walker an eighth below the centre).
     */
    @Test @Config(fontScale = 2.0f)
    fun `item 2 at its tallest the display stays above the walker's dot and nothing covers the needle`() {
        route = ReturnRoute.Unavailable(canRetry = true)
        line = recordingLine
        backBy = BackByShown(trackId = "t1", backByAtEpochMillis = morning + 40 * minute, nowEpochMillis = morning)
        setScreen(Surface.ROTATION_90, clusterOnRailSide = true)
        val hud = tag(NAVIGATION_HUD_TAG)
        val mapArea = tag(LAYOUT_FIXES_MAP_TAG)
        val height = mapArea.bottom - mapArea.top
        val walkerY = mapArea.top + (height + height * NAVIGATION_VIEW_TOP_PADDING_FRACTION.toFloat()) / 2
        assertTrue("positive control: the evening line and Try again are both up", nodes(NAVIGATION_HUD_EVENING_LINE_TAG).isNotEmpty() && nodes(NAVIGATION_HUD_RETRY_TAG).isNotEmpty())
        assertTrue("the display $hud ends above the walker's dot at y ${walkerY.value} by its radius", hud.bottom <= walkerY - WALKER_DOT_RADIUS)
        val needle = tag(NAVIGATION_HUD_NEEDLE_TAG)
        assertTrue("the needle $needle is inside the display $hud", needle.left >= hud.left && needle.right <= hud.right && needle.top >= hud.top && needle.bottom <= hud.bottom)
        // What is drawn after the display (the rail) or beside it (the search bar, the L) must not reach the needle.
        listOf(COMPACT_NAVIGATION_RAIL_TAG, SEARCH_ENTRY_BAR_TAG, MAP_ICON_CLUSTER_TAG).forEach { other ->
            nodes(other).forEach { assertFalse("$other ${it.dp().describe()} does not cover the needle ${needle.describe()}", it.dp().overlapsRect(needle)) }
        }
    }

    // ── Item 3: the icon bar while navigating ──

    private fun bar(): DpRect = nodes(MAP_ICON_BAR_TAG).single().dp()
    private fun record(): DpRect = tag("control-pill-record")
    private fun returnButton(): DpRect = tag("control-pill-return-to-vehicle")
    private fun add(): DpRect = described("Plan a trip or log a find here")

    /** The L on the display's side: down below the display, the pill beside "+", and back to where the user put it. */
    private fun assertSlideOnTheDisplaysSide(rotation: Int) {
        setScreen(rotation, clusterOnRailSide = true, navigating = false)
        // Where the user put it: dragged down 20 dp from where it opened, which the screen stores.
        val handle = tag("map-icon-bar-minimize-handle")
        composeRule.longPressDrag((handle.left + handle.right) / 2, (handle.top + handle.bottom) / 2, 0.dp, 20.dp)
        val writes = placementWrites.size
        assertTrue("positive control: the drag was stored", writes > 0)
        val barBefore = bar()
        val recordBefore = record()
        val clusterBefore = tag(MAP_ICON_CLUSTER_TAG)
        val onLeft = (barBefore.left + barBefore.right) / 2 < (window().left + window().right) / 2

        composeRule.runOnIdle { returning = true }
        settle()
        val hud = tag(NAVIGATION_HUD_TAG)
        val barDown = bar()
        val addRow = add()
        val rec = record()
        val ret = returnButton()
        assertTrue("positive control: the display is on the L's side ($hud, bar $barDown)", if (onLeft) hud.left < barDown.right else hud.right > barDown.left)
        assertTrue("the bar's top ${barDown.top} is not above the display's bottom ${hud.bottom}", barDown.top >= hud.bottom - 0.5.dp)
        assertEquals("the record button is level with +: $rec, + $addRow", addRow.top.value, rec.top.value, 0.5f)
        assertEquals("the return button is level with +: $ret, + $addRow", addRow.top.value, ret.top.value, 0.5f)
        if (onLeft) {
            assertTrue("the pill is right of the bar: record $rec, return $ret, bar $barDown", minOf(rec.left, ret.left) >= barDown.right - 0.5.dp)
        } else {
            assertTrue("the pill is left of the bar: record $rec, return $ret, bar $barDown", maxOf(rec.right, ret.right) <= barDown.left + 0.5.dp)
        }
        assertEquals("nothing was stored while navigating", writes, placementWrites.size)

        composeRule.runOnIdle { returning = false }
        settle()
        assertEquals("the bar is back where the user put it: $barBefore", barBefore.top.value, bar().top.value, 0.5f)
        assertEquals("the pill is back under it: $recordBefore", recordBefore.top.value, record().top.value, 0.5f)
        assertEquals("the pill is back under it: $recordBefore", recordBefore.left.value, record().left.value, 0.5f)
        assertEquals("the L's box is as it was: $clusterBefore", clusterBefore.bottom.value, tag(MAP_ICON_CLUSTER_TAG).bottom.value, 0.5f)
        assertEquals("nothing was stored by the slide", writes, placementWrites.size)
    }

    @Test fun `item 3 at ROTATION_90 the L on the display's side slides below it and comes back`() = assertSlideOnTheDisplaysSide(Surface.ROTATION_90)

    @Test fun `item 3 at ROTATION_270 the L on the display's side slides below it and comes back`() = assertSlideOnTheDisplaysSide(Surface.ROTATION_270)

    @Test
    fun `item 3 the L on the other side does not move when navigation starts`() {
        setScreen(Surface.ROTATION_90, clusterOnRailSide = false, navigating = false)
        val barBefore = bar()
        val recordBefore = record()
        composeRule.runOnIdle { returning = true }
        settle()
        assertTrue("positive control: navigating", nodes(NAVIGATION_HUD_TAG).isNotEmpty())
        assertEquals("the bar did not move", barBefore.top.value, bar().top.value, 0.5f)
        assertEquals("the pill did not move", recordBefore.top.value, record().top.value, 0.5f)
        assertEquals("the pill did not move", recordBefore.left.value, record().left.value, 0.5f)
    }

    // ── Item 3: touches ──

    private fun fractions() = listOf(0.5f to 0.5f, 0.2f to 0.2f, 0.8f to 0.2f, 0.2f to 0.8f, 0.8f to 0.8f)

    /** A control and how a test knows a touch reached it. */
    private inner class Control(val name: String, val bounds: () -> DpRect, val reached: () -> Boolean)

    private fun controls(onLeft: Boolean): List<Control> {
        var resetBefore = 0
        var locateBefore = 0
        var recordBefore = 0
        var returnBefore = 0
        return listOf(
            Control("Fullscreen", { described("Fullscreen") }) { composeRule.onAllNodes(hasContentDescription("Exit fullscreen")).fetchSemanticsNodes().isNotEmpty() },
            Control("Reset north", { described("Reset orientation to north").also { resetBefore = map.content?.resetOrientationRequestId ?: 0 } }) {
                (map.content?.resetOrientationRequestId ?: 0) == resetBefore + 1
            },
            // The minimise handle owns the locate row's outer 12 dp by design (owner's ruling (c)), so its samples stay inboard.
            Control("Locate", {
                described("Center on my location").let { r -> if (onLeft) DpRect(r.left + 14.dp, r.top, r.right, r.bottom) else DpRect(r.left, r.top, r.right - 14.dp, r.bottom) }.also { locateBefore = locates }
            }) { locates == locateBefore + 1 },
            Control("Layers", { described("Layers:", substring = true) }) { nodes(MAP_LAYERS_SHEET_TAG).isNotEmpty() },
            Control("Add", { add() }) { nodes(ADD_ACTION_TILE_TAG).isNotEmpty() },
            Control("Record", { record().also { recordBefore = recordToggles } }) { recordToggles == recordBefore + 1 },
            Control("Return", { returnButton().also { returnBefore = returnToggles } }) { returnToggles == returnBefore + 1 },
        )
    }

    /**
     * Every bar and pill button, five real touches across each, a fresh screen per touch. [atMidSlide]: the touch lands while
     * the L is moving (the clock held at a frame where the bar and the pill are both between their two places).
     */
    private fun assertEveryButtonTakesTouches(rotation: Int, navigating: Boolean, atMidSlide: Boolean = false) {
        setScreen(rotation, clusterOnRailSide = true, navigating = navigating && !atMidSlide)
        val b = bar()
        val onLeft = (b.left + b.right) / 2 < (window().left + window().right) / 2
        val ends = if (atMidSlide) slideEnds() else null
        var sampled = 0
        for (control in controls(onLeft)) {
            fractions().forEachIndexed { i, (fx, fy) ->
                freshScreen(navigating = navigating && !atMidSlide)
                if (ends != null) holdMidSlide(ends)
                val r = control.bounds()
                composeRule.touchAt(r.left + (r.right - r.left) * fx, r.top + (r.bottom - r.top) * fy)
                composeRule.mainClock.autoAdvance = true
                settle()
                assertTrue("${control.name}, touch $i at ($fx, $fy) of $r (navigating $navigating, mid-slide $atMidSlide) reached it", control.reached())
                sampled++
            }
        }
        assertEquals("every control was touched at every point", 35, sampled)
        assertEquals("no touch fell through to the map", 0, map.taps)
    }

    /** The bar's top and the record button's box, not navigating and settled navigating: the two ends of the slide. */
    private data class SlideEnds(val barUp: DpRect, val barDown: DpRect, val recordUnder: DpRect, val recordBeside: DpRect)

    private fun slideEnds(): SlideEnds {
        freshScreen(navigating = false)
        val barUp = bar()
        val recordUnder = record()
        composeRule.runOnIdle { returning = true }
        settle()
        val ends = SlideEnds(barUp, bar(), recordUnder, record())
        assertTrue("positive control: the slide moves the bar ($ends)", ends.barDown.top - ends.barUp.top > 8.dp)
        return ends
    }

    private fun DpRect.between(a: DpRect, b: DpRect): Boolean {
        fun away(o: DpRect) = maxOf((top - o.top).value, (o.top - top).value, (left - o.left).value, (o.left - left).value) > 4f
        return away(a) && away(b)
    }

    /** From a settled screen not navigating, starts navigation and holds the clock at the first frame where both the bar and the pill are mid-move. */
    private fun holdMidSlide(ends: SlideEnds) {
        composeRule.mainClock.autoAdvance = false
        returning = true
        repeat(MAX_FRAMES) {
            composeRule.mainClock.advanceTimeByFrame()
            if (bar().between(ends.barUp, ends.barDown) && record().between(ends.recordUnder, ends.recordBeside)) return
        }
        throw AssertionError("positive control: no frame in $MAX_FRAMES had the bar and the pill both mid-slide ($ends; last bar ${bar()}, record ${record()})")
    }

    @Test fun `item 3 at ROTATION_90 not navigating every bar and pill button takes real touches across it`() = assertEveryButtonTakesTouches(Surface.ROTATION_90, navigating = false)

    @Test fun `item 3 at ROTATION_90 navigating every bar and pill button takes real touches across it`() = assertEveryButtonTakesTouches(Surface.ROTATION_90, navigating = true)

    @Test fun `item 3 at ROTATION_270 navigating every bar and pill button takes real touches across it`() = assertEveryButtonTakesTouches(Surface.ROTATION_270, navigating = true)

    @Test fun `item 3 at ROTATION_90 mid-slide every bar and pill button takes real touches across it`() = assertEveryButtonTakesTouches(Surface.ROTATION_90, navigating = true, atMidSlide = true)

    /** CLAUDE.md, the `Surface` pitfall: with the pill beside "+", the map beside the L still takes a long-press. */
    @Test
    fun `item 3 navigating the map's long-press beside the L and above the pill still reaches the map`() {
        setScreen(Surface.ROTATION_90, clusterOnRailSide = true)
        val b = bar()
        val rec = record()
        val ret = returnButton()
        val onLeft = (b.left + b.right) / 2 < (window().left + window().right) / 2
        val pill = DpRect(minOf(rec.left, ret.left), minOf(rec.top, ret.top), maxOf(rec.right, ret.right), maxOf(rec.bottom, ret.bottom))
        val x = if (onLeft) b.right + 24.dp else b.left - 24.dp
        val points = listOf(x to (b.top + b.bottom) / 2, x to pill.top - 24.dp, (pill.left + pill.right) / 2 to pill.top - 12.dp)
        points.forEach { (px, py) ->
            val before = map.longPresses
            longPressAt(px, py)
            assertEquals("a long-press at (${px.value}, ${py.value}) beside the L (bar $b, pill $pill) reached the map", before + 1, map.longPresses)
        }
        val before = map.longPresses
        longPressAt((pill.left + pill.right) / 2, (pill.top + pill.bottom) / 2)
        assertEquals("positive control: a long-press on the pill itself does not reach the map", before, map.longPresses)
    }

    private fun longPressAt(x: Dp, y: Dp) {
        val at = with(composeRule.density) { androidx.compose.ui.geometry.Offset(x.toPx(), y.toPx()) }
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput {
            down(at)
            advanceEventTime(700)
            up()
        }
        composeRule.waitForIdle()
    }

    private companion object {
        /** The walker's dot, a margin generous enough for MapLibre's puck and its accuracy ring at walking zoom. */
        val WALKER_DOT_RADIUS = 12.dp
        const val MAX_FRAMES = 120
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HudLandscapeT11FixesAt823Test : HudLandscapeT11Fixes()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HudLandscapeT11FixesAt780Test : HudLandscapeT11Fixes()
