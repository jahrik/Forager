package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.FindDecision
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.WaypointDecision
import com.zynergylabs.forager.app.ui.log.CARTOGRAPHY_MAP_TEST_TAG
import com.zynergylabs.forager.app.ui.log.CartographyUiState
import com.zynergylabs.forager.app.ui.log.OFFLINE_TOGGLE_TEST_TAG
import com.zynergylabs.forager.app.ui.map.MAP_BUBBLE_TAG
import com.zynergylabs.forager.app.ui.map.MapCameraRequest
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import java.time.LocalDate
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
 * L2 (dispatch `prompts/preserved/2026-09-28-47.md`): the day entry report in a short window. Its map
 * was 4:3 by the column's width (480 dp at 640), taller than the window, and was drawn over its own
 * header row, with the offline-map row squeezed to nothing. In a short window the map is now capped
 * (see `CartographyEntryReportScreen`'s `SHORT_WINDOW_MAP_HEIGHT_FRACTION`), so the header row and the
 * offline switch show, and they take real touches.
 *
 * Driven through the real [AvailabilityScreen], Journal tab, with a committed entry open, a kept find
 * and waypoint that the map frames, and a covering offline region so the offline switch shows (the
 * control the device run found cut off is **not** known to be this switch; that stays a device item).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class LandscapeEntryReportTest {

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

    private val entry = CartographyEntry.draft(id = "entry-l2", date = L2_DAY, updatedAtEpochMillis = 1_000L).copy(
        isDraft = false,
        text = "A wet morning on the ridge.",
        findDecisions = listOf(FindDecision("find-1", L2_DAY, "Golden chanterelle", hasPhotos = false, kept = true)),
        waypointDecisions = listOf(WaypointDecision("wp-1", "Old gate", 45.4, -122.5, kept = true)),
    )
    private val mapData = CartographyEntryMapData(
        trackPolylines = emptyList(),
        findMarkers = listOf(RecordPoint("find-1", LatLng(45.51, -122.61))),
        waypointMarkers = listOf(RecordPoint("wp-1", LatLng(45.4, -122.5))),
        photoMarkers = emptyList(),
        offlineRegionCircles = emptyList(),
    )
    private val region = OfflineRegionSummary(
        id = 7L, name = "Ridge", region = Region(45.45, -122.55, 10), minZoom = 0.0, maxZoom = 15.0,
        tileCount = 100, sizeBytes = 1_000_000L, createdAtEpochMillis = 0L,
    )
    private var editing by mutableStateOf<CartographyEntry?>(entry)
    private var closes = 0

    private val bubbles = BubbleMapSlot(listOf(StubGlyph(MapLayerIds.WAYPOINTS, "wp-1", 60.dp, 60.dp, LatLng(45.4, -122.5))))
    private val requests = mutableListOf<MapCameraRequest?>()
    private val recordingSlot: MapSlot = { region, content, renderMode, focusOverride, onLongPress, onTap, onSightingTap, onCameraIdle, modifier ->
        requests += renderMode.cameraRequest
        bubbles.slot(region, content, renderMode, focusOverride, onLongPress, onTap, onSightingTap, onCameraIdle, modifier)
    }

    private fun setScreen() {
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(),
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
                mapSlot = recordingSlot,
                cartographyUiState = CartographyUiState(entries = listOf(entry), editingEntry = editing),
                onCloseCartographyEntry = { closes++; editing = null },
                getCartographyEntryMapData = { _, _ -> mapData },
                getCartographyEntryOfflineRegion = { _, _ -> region },
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Journal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("A wet morning on the ridge.").assertExists()
    }

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun root(): DpRect = composeRule.onRoot().getUnclippedBoundsInRoot()
    private fun back() = composeRule.onNodeWithContentDescription("Back to Cartography")
    private fun options() = composeRule.onNodeWithContentDescription("Entry options")

    private fun touch(rect: DpRect, fx: Float, fy: Float) {
        val x = rect.left + (rect.right - rect.left) * fx
        val y = rect.top + (rect.bottom - rect.top) * fy
        composeRule.onRoot().performTouchInput { click(Offset(x.value * density, y.value * density)) }
        composeRule.waitForIdle()
    }

    private val fractions = listOf(0.5f to 0.5f, 0.2f to 0.2f, 0.8f to 0.2f, 0.2f to 0.8f, 0.8f to 0.8f)

    // ── Tests first ──

    @Test
    fun `L2 the header row shows above the map, not under it, and Back takes real touches across its bounds`() {
        setScreen()
        back().assertIsDisplayed()
        options().assertIsDisplayed()
        val backRect = back().getUnclippedBoundsInRoot()
        val mapRect = bounds(CARTOGRAPHY_MAP_TEST_TAG)
        assertTrue("the map ($mapRect) starts below the header row (Back at $backRect)", mapRect.top >= backRect.bottom)

        // Each touch closes the entry; reopen it for the next.
        for ((fx, fy) in fractions) {
            touch(backRect, fx, fy)
            composeRule.runOnIdle { editing = entry }
            composeRule.waitForIdle()
        }
        assertEquals("every sampled touch on Back reached it", fractions.size, closes)
    }

    @Test
    fun `L2 the offline switch below the map is shown whole in the window and takes real touches`() {
        setScreen()
        val switch = composeRule.onNodeWithTag(OFFLINE_TOGGLE_TEST_TAG)
        switch.assertIsDisplayed()
        val rect = switch.getUnclippedBoundsInRoot()
        val window = root()
        assertTrue("the switch $rect lies wholly inside the window $window", rect.top >= window.top && rect.bottom <= window.bottom)
        assertTrue("the switch $rect is below the map ${bounds(CARTOGRAPHY_MAP_TEST_TAG)}", rect.top >= bounds(CARTOGRAPHY_MAP_TEST_TAG).bottom)
        switch.assertIsOff()
        touch(rect, 0.5f, 0.5f)
        switch.assertIsOn()
        touch(rect, 0.2f, 0.3f)
        switch.assertIsOff()
        touch(rect, 0.8f, 0.7f)
        switch.assertIsOn()
    }

    @Test
    fun `L2 the map is capped at the stated fraction of the window height`() {
        setScreen()
        val mapRect = bounds(CARTOGRAPHY_MAP_TEST_TAG)
        assertEquals("the map is 40% of the 384 dp window", 384f * 0.4f, (mapRect.bottom - mapRect.top).value, 0.5f)
    }

    // ── Checks: M1's bubbles and the opening frame still work on the capped map ──

    @Test
    fun `L2 a real touch on a glyph in the capped map opens its bubble, and the map asked for its opening frame once`() {
        setScreen()
        val mapRect = bounds("map-slot")
        touch(DpRect(mapRect.left + 48.dp, mapRect.top + 48.dp, mapRect.left + 72.dp, mapRect.top + 72.dp), 0.5f, 0.5f)
        composeRule.onNodeWithTag(MAP_BUBBLE_TAG).assertIsDisplayed()
        assertEquals("the glyph, not the map, took the touch", 0, bubbles.taps)
        assertEquals("one opening request", 1, requests.filterNotNull().distinct().size)
    }

    // ── Check (a pin, passes before and after by design): portrait is unchanged ──

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `L2 portrait keeps the 4 by 3 map across the width`() {
        setScreen()
        val mapRect = bounds(CARTOGRAPHY_MAP_TEST_TAG)
        val window = root()
        assertEquals("the map spans the width", (window.right - window.left).value, (mapRect.right - mapRect.left).value, 0.5f)
        assertEquals("the map is 4:3", (mapRect.right - mapRect.left).value * 3f / 4f, (mapRect.bottom - mapRect.top).value, 0.5f)
        assertTrue("the map starts below Back", mapRect.top >= back().getUnclippedBoundsInRoot().bottom)
    }
}

private val L2_DAY = LocalDate.of(2026, 9, 20)
