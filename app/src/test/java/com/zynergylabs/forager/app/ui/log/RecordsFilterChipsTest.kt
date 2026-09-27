package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.availability.AvailabilityUiState
import com.zynergylabs.forager.app.ui.map.Basemap
import com.zynergylabs.forager.app.ui.map.MapSlot
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
 * Journal redesign J1, S3 (`prompts/preserved/2026-09-27-16.md`; plan J4): Records' four-tab
 * `SecondaryTabRow` becomes one scrolling row of filter chips, All · Finds · Tracks · Waypoints ·
 * Offline maps, each with a count, All the default.
 *
 * Driven through the real [JournalTab], with its callbacks wired to local state the way
 * [JournalTabTest] does. Back goes through the host Activity's real `OnBackPressedDispatcher`, the
 * only thing that can settle which `BackHandler` takes a press. Chip selection that stands for "a
 * finger chose this chip" is a coordinate touch at several points across the chip's own bounds
 * (CLAUDE.md, "A semantic `performClick` asserts wiring, not routing"); a semantic click is used only
 * where a test is getting somewhere, not testing the chip (opening Records, the "+" tile).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class RecordsFilterChipsTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private var offlineMapsOpenedCount = 0
    private var tracksOpenedCount = 0
    private var incidentalExitCount = 0

    private fun pressBack() {
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
    }

    private fun setScreen(
        initial: MushroomLogUiState = MushroomLogUiState(entries = listOf(CHIPS_FIND)),
        pendingDestination: PendingJournalDestination? = null,
        openRecords: Boolean = true,
    ) {
        composeRule.setContent {
            var uiState by remember { mutableStateOf(initial) }
            var pending by remember { mutableStateOf(pendingDestination) }
            JournalTab(
                uiState = uiState,
                onOpenCameraForLogEntry = {},
                onOpenCameraForAlbum = {},
                onOpenCameraForCartographyEntry = {},
                mapSlot = CHIPS_STUB_MAP,
                pickerRegion = Region(lat = 45.326, lng = -122.634, radiusKm = 15),
                basemap = Basemap.DEFAULT,
                onOpenEntry = { id -> uiState = uiState.copy(editingEntry = uiState.entries.first { it.id == id }) },
                onCloseEntry = { uiState = uiState.copy(editingEntry = null) },
                onStartEntry = { location, date ->
                    uiState = uiState.copy(editingEntry = MushroomLogEntry.draft(id = "new-entry", location = location, date = date))
                },
                onEntryChanged = { updated -> uiState = uiState.copy(editingEntry = updated) },
                onStartEditingEntry = {},
                onSaveEntry = {},
                onCancelEditing = { uiState = uiState.copy(editingEntry = null) },
                onLeaveEditingIncidentally = {
                    incidentalExitCount++
                    uiState = uiState.copy(editingEntry = null)
                },
                onAddPhoto = {},
                onRemovePhoto = {},
                onPullPhoto = {},
                onDeleteEntry = {},
                onSaveErrorDismissed = {},
                cartographyUiState = CartographyUiState(),
                onOpenCartographyEntry = {},
                onStartCartographyEntry = {},
                onCloseCartographyEntry = {},
                onCartographyTextChanged = {},
                onCartographyTagsChanged = {},
                onSetFindDecision = { _, _ -> },
                onSetTrackDecision = { _, _ -> },
                onSetWaypointDecision = { _, _ -> },
                onSetOfflineRegionDecision = { _, _ -> },
                onToggleKeptPhoto = {},
                onFinishCartographyEntry = {},
                onDeleteCartographyEntry = {},
                getCartographyEntryMapData = { _, _ -> CHIPS_EMPTY_MAP_DATA },
                getCartographyEntryOfflineRegion = { _, _ -> null },
                getCartographyEntryCurrentLocation = { LocationResult.LocationUnavailable },
                availabilityUiState = AvailabilityUiState(offlineRegions = CHIPS_REGIONS),
                distanceUnit = DistanceUnit.MILES,
                currentTime = CurrentTimeProvider { 0L },
                onOfflineMapLatChanged = {},
                onOfflineMapLngChanged = {},
                onOfflineMapRadiusChanged = {},
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = { offlineMapsOpenedCount++ },
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                tracks = CHIPS_TRACKS,
                onTracksOpened = { tracksOpenedCount++ },
                waypoints = CHIPS_WAYPOINTS,
                waypointsErrorMessage = null,
                onDeleteWaypoint = {},
                pendingDestination = pending,
                onPendingDestinationConsumed = { pending = null },
            )
        }
        composeRule.waitForIdle()
        if (openRecords) {
            composeRule.onNodeWithText("Records").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun chip(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)

    /** One real touch at [fraction] of the chip's own width and height, after scrolling it into view. */
    private fun touchChip(tag: String, fraction: Offset) {
        chip(tag).performScrollTo()
        composeRule.waitForIdle()
        chip(tag).performTouchInput { click(Offset(width * fraction.x, height * fraction.y)) }
        composeRule.waitForIdle()
    }

    /** Selects [tag] by a real touch at each sample point in turn, going back to All between samples so each touch has to do the selecting. */
    private fun touchChipAcrossItsBounds(tag: String, afterEachTouch: () -> Unit = {}) {
        for (point in TOUCH_SAMPLES) {
            touchChip(ALL_CHIP, Offset(0.5f, 0.5f))
            chip(ALL_CHIP).assertIsSelected()
            touchChip(tag, point)
            chip(tag).assertIsSelected()
            chip(ALL_CHIP).assertIsNotSelected()
            afterEachTouch()
        }
    }

    // ── The row ──

    @Test
    fun `the chip row shows All, Finds, Tracks, Waypoints and Offline maps in that order, with counts matching the data, All selected`() {
        setScreen()

        val expected = listOf(
            Triple(ALL_CHIP, "All", "7"),
            Triple(FINDS_CHIP, "Finds", "1"),
            Triple(TRACKS_CHIP, "Tracks", "1"),
            Triple(WAYPOINTS_CHIP, "Waypoints", "2"),
            Triple(OFFLINE_MAPS_CHIP, "Offline maps", "3"),
        )
        for ((tag, label, count) in expected) {
            chip(tag).assert(hasText(label)).assert(hasText(count))
        }
        chip(ALL_CHIP).assertIsSelected()
        for ((tag, _, _) in expected.drop(1)) chip(tag).assertIsNotSelected()

        val lefts = expected.map { (tag, _, _) -> chip(tag).getUnclippedBoundsInRoot().left.value }
        assertEquals("chips are laid out left to right in display order", lefts.sorted(), lefts)
        // The old sub-tab labels are gone.
        composeRule.onNodeWithText("Waypoint Markers").assertDoesNotExist()
        composeRule.onNodeWithText("Logged Finds").assertDoesNotExist()
    }

    // ── Each single-type chip shows what its sub-tab showed ──

    @Test
    fun `touching Waypoints shows the waypoint list`() {
        setScreen()
        touchChipAcrossItsBounds(WAYPOINTS_CHIP) {
            composeRule.onNodeWithContentDescription("Remove waypoint Alpha").assertExists()
            composeRule.onNodeWithContentDescription("Remove waypoint Bravo").assertExists()
        }
    }

    @Test
    fun `touching Tracks shows the recorded-track list and fires the tracks-opened callback`() {
        setScreen()
        val before = tracksOpenedCount
        touchChipAcrossItsBounds(TRACKS_CHIP) {
            composeRule.onNodeWithTag("share-track-t1").assertExists()
        }
        assertEquals("one tracks-opened call per entry into Tracks", before + TOUCH_SAMPLES.size, tracksOpenedCount)
    }

    @Test
    fun `touching Offline maps shows the offline-maps panel and fires the offline-maps-opened callback`() {
        setScreen()
        val before = offlineMapsOpenedCount
        touchChipAcrossItsBounds(OFFLINE_MAPS_CHIP) {
            composeRule.onNodeWithText("Downloaded Maps").assertExists()
            composeRule.onNodeWithText("Ridge").assertExists()
        }
        assertEquals("one offline-maps-opened call per entry into Offline maps", before + TOUCH_SAMPLES.size, offlineMapsOpenedCount)
    }

    @Test
    fun `touching Finds shows the finds gallery`() {
        setScreen()
        touchChipAcrossItsBounds(FINDS_CHIP) {
            composeRule.onNodeWithContentDescription("New log entry").assertExists()
            composeRule.onNodeWithText("Find on ${CHIPS_FIND.foundOn}").assertExists()
        }
    }

    // ── Back ──

    @Test
    fun `Back from a single-type chip returns to All, and Back from All steps to Cartography`() {
        setScreen()
        for (tag in listOf(FINDS_CHIP, TRACKS_CHIP, WAYPOINTS_CHIP, OFFLINE_MAPS_CHIP)) {
            touchChip(tag, Offset(0.5f, 0.5f))
            chip(tag).assertIsSelected()

            pressBack()

            chip(ALL_CHIP).assertIsSelected()
            chip(tag).assertIsNotSelected()
            composeRule.onNodeWithText("Records").assertIsSelected()
        }

        pressBack()

        composeRule.onNodeWithText("Cartography").assertIsSelected()
    }

    // ── The Finds editing flow, unchanged ──

    @Test
    fun `EDIT_NEW_FIND lands on Finds editing with the Finds chip selected`() {
        val newFind = MushroomLogEntry.draft(id = "new-find", location = LatLng(45.5, -122.5), date = LocalDate.of(2026, 8, 1))
        setScreen(
            MushroomLogUiState(entries = listOf(CHIPS_FIND), editingEntry = newFind),
            pendingDestination = PendingJournalDestination.EDIT_NEW_FIND,
            openRecords = false,
        )

        composeRule.onNodeWithText("Records").assertIsSelected()
        chip(FINDS_CHIP).assertIsSelected()
        composeRule.onNodeWithText("Photos").assertIsDisplayed()
        composeRule.onNodeWithText("Found at 45.5000, -122.5000").assertIsDisplayed()
    }

    @Test
    fun `leaving the Finds chip while editing is an incidental exit`() {
        setScreen()
        touchChip(FINDS_CHIP, Offset(0.5f, 0.5f))
        composeRule.onNodeWithContentDescription("New log entry").performClick()
        composeRule.onNodeWithText("Photos").assertIsDisplayed()
        assertEquals(0, incidentalExitCount)

        touchChip(WAYPOINTS_CHIP, Offset(0.5f, 0.5f))

        assertEquals("leaving Finds mid-edit calls the incidental exit once", 1, incidentalExitCount)
        chip(WAYPOINTS_CHIP).assertIsSelected()
        composeRule.onNodeWithText("Photos").assertDoesNotExist()
    }

    @Test
    fun `leaving the Finds chip with nothing being edited is not an incidental exit`() {
        setScreen()
        touchChip(FINDS_CHIP, Offset(0.5f, 0.5f))
        touchChip(WAYPOINTS_CHIP, Offset(0.5f, 0.5f))
        assertTrue(incidentalExitCount == 0)
    }
}

private const val ALL_CHIP = "records-chip-all"
private const val FINDS_CHIP = "records-chip-finds"
private const val TRACKS_CHIP = "records-chip-tracks"
private const val WAYPOINTS_CHIP = "records-chip-waypoints"
private const val OFFLINE_MAPS_CHIP = "records-chip-offline-maps"

/** Three touches spread across a chip: near its start edge, its centre, near its end edge, at differing heights. */
private val TOUCH_SAMPLES = listOf(Offset(0.12f, 0.3f), Offset(0.5f, 0.5f), Offset(0.88f, 0.7f))

private val CHIPS_FIND = MushroomLogEntry.draft(id = "find-1", location = null, date = LocalDate.of(2026, 9, 20)).copy(isDraft = false)

private val CHIPS_WAYPOINTS = listOf(
    Waypoint(id = "w1", lat = 45.5, lng = -122.6, altitude = null, name = "Alpha", note = "", createdAtEpochMillis = 1_758_000_000_000L),
    Waypoint(id = "w2", lat = 45.6, lng = -122.7, altitude = null, name = "Bravo", note = "", createdAtEpochMillis = 1_758_100_000_000L),
)

private val CHIPS_TRACKS = listOf(
    Track(id = "t1", name = null, startedAtEpochMillis = 1_758_200_000_000L, endedAtEpochMillis = 1_758_203_600_000L, points = emptyList()),
)

private val CHIPS_REGIONS = listOf(
    OfflineRegionSummary(id = 1L, name = "Ridge", region = Region(45.5, -122.6, 5), minZoom = 10.0, maxZoom = 15.0, tileCount = 10, sizeBytes = 1000L, createdAtEpochMillis = 1_758_300_000_000L),
    OfflineRegionSummary(id = 2L, name = "Creek", region = Region(45.4, -122.5, 5), minZoom = 10.0, maxZoom = 15.0, tileCount = 10, sizeBytes = 1000L, createdAtEpochMillis = 1_758_400_000_000L),
    OfflineRegionSummary(id = 3L, name = "Saddle", region = Region(45.3, -122.4, 5), minZoom = 10.0, maxZoom = 15.0, tileCount = 10, sizeBytes = 1000L, createdAtEpochMillis = 1_758_500_000_000L),
)

private val CHIPS_EMPTY_MAP_DATA = CartographyEntryMapData(
    trackPolylines = emptyList(),
    findMarkers = emptyList(),
    waypointMarkers = emptyList(),
    photoMarkers = emptyList(),
    offlineRegionCircles = emptyList(),
)

private val CHIPS_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }
