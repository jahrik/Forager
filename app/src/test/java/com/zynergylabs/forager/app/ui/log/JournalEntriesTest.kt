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
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.ui.availability.AvailabilityUiState
import com.zynergylabs.forager.app.ui.map.Basemap
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Journal redesign J2 (`prompts/preserved/2026-09-27-18.md`; plan J1, J2, J3, J7): the Entries |
 * Records switch, the Drafts banner, the timeline/album toggle and the floating button, driven
 * through the real [JournalTab] with its Cartography callbacks wired to local state, the way
 * [RecordsFilterChipsTest] wires the finds side.
 *
 * Back goes through the host Activity's real `OnBackPressedDispatcher`, the only thing that settles
 * which `BackHandler` takes a press. Every control a finger taps is touched with
 * `performTouchInput` at several points across its own bounds (CLAUDE.md, "A semantic
 * `performClick` asserts wiring, not routing"). Tags are literals, so each step's tests compile
 * against the base they were written before.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class JournalEntriesTest {

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

    private val openedCartographyIds = mutableListOf<String>()
    private var startedCartographyEntries = 0
    private var albumCameraOpens = 0

    private fun pressBack() {
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
    }

    private fun setScreen(
        cartography: CartographyUiState = CartographyUiState(entries = listOf(ENTRIES_COMMITTED)),
        galleryPhotos: List<GalleryPhoto> = emptyList(),
        galleryPhotoEntryReferenceCounts: Map<String, Int> = emptyMap(),
        journalState: JournalScreenState? = null,
    ) {
        composeRule.setContent {
            var logState by remember { mutableStateOf(MushroomLogUiState()) }
            var cartographyState by remember { mutableStateOf(cartography) }
            val state = journalState ?: rememberJournalScreenState()
            JournalTab(
                uiState = logState,
                onOpenCameraForLogEntry = {},
                onOpenCameraForAlbum = { albumCameraOpens++ },
                onOpenCameraForCartographyEntry = {},
                mapSlot = ENTRIES_STUB_MAP,
                pickerRegion = Region(lat = 45.326, lng = -122.634, radiusKm = 15),
                basemap = Basemap.DEFAULT,
                onOpenEntry = { id -> logState = logState.copy(editingEntry = logState.entries.first { it.id == id }) },
                onCloseEntry = { logState = logState.copy(editingEntry = null) },
                onStartEntry = { location, date ->
                    logState = logState.copy(editingEntry = MushroomLogEntry.draft(id = "new-find", location = location, date = date))
                },
                onEntryChanged = {},
                onStartEditingEntry = {},
                onSaveEntry = {},
                onCancelEditing = {},
                onLeaveEditingIncidentally = {},
                onAddPhoto = {},
                onRemovePhoto = {},
                onPullPhoto = {},
                onDeleteEntry = {},
                onSaveErrorDismissed = {},
                galleryPhotos = galleryPhotos,
                galleryPhotoEntryReferenceCounts = galleryPhotoEntryReferenceCounts,
                cartographyUiState = cartographyState,
                onOpenCartographyEntry = { id ->
                    openedCartographyIds += id
                    cartographyState = cartographyState.copy(
                        editingEntry = (cartographyState.entries + cartographyState.draftEntries).first { it.id == id },
                    )
                },
                onStartCartographyEntry = { date ->
                    startedCartographyEntries++
                    val started = CartographyEntry.draft(id = "started-$startedCartographyEntries", date = date, updatedAtEpochMillis = 0L)
                    cartographyState = cartographyState.copy(editingEntry = started, draftEntries = cartographyState.draftEntries + started)
                },
                onCloseCartographyEntry = { cartographyState = cartographyState.copy(editingEntry = null, hasUnsavedChanges = false) },
                onCartographyTextChanged = {},
                onCartographyTagsChanged = {},
                onSetFindDecision = { _, _ -> },
                onSetTrackDecision = { _, _ -> },
                onSetWaypointDecision = { _, _ -> },
                onSetOfflineRegionDecision = { _, _ -> },
                onToggleKeptPhoto = {},
                onFinishCartographyEntry = {},
                onDeleteCartographyEntry = {},
                getCartographyEntryMapData = { _, _ -> ENTRIES_EMPTY_MAP_DATA },
                getCartographyEntryOfflineRegion = { _, _ -> null },
                getCartographyEntryCurrentLocation = { LocationResult.LocationUnavailable },
                availabilityUiState = AvailabilityUiState(),
                distanceUnit = DistanceUnit.MILES,
                currentTime = CurrentTimeProvider { 0L },
                onOfflineMapLatChanged = {},
                onOfflineMapLngChanged = {},
                onOfflineMapRadiusChanged = {},
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                tracks = emptyList(),
                onTracksOpened = {},
                waypoints = emptyList(),
                waypointsErrorMessage = null,
                onDeleteWaypoint = {},
                journalState = state,
            )
        }
        composeRule.waitForIdle()
    }

    private fun node(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)

    /** One real touch at [fraction] of the node's own width and height. */
    private fun touch(tag: String, fraction: Offset) {
        node(tag).performTouchInput { click(Offset(width * fraction.x, height * fraction.y)) }
        composeRule.waitForIdle()
    }

    // ── T1: the switch ──

    @Test
    fun `the switch shows Entries and Records, Entries selected, and no Cartography label`() {
        setScreen()

        node(SWITCH_ENTRIES).assert(hasText("Entries")).assertIsSelected()
        node(SWITCH_RECORDS).assert(hasText("Records")).assertIsNotSelected()
        composeRule.onNodeWithText("Cartography").assertDoesNotExist()
        // Entries shows Cartography's content (an entry card), not Records' chips.
        composeRule.onNodeWithText(ENTRIES_COMMITTED.date.toString()).assertExists()
        node(RECORDS_CHIP_ROW).assertDoesNotExist()
    }

    @Test
    fun `touching each side of the switch at several points switches between Entries and Records`() {
        setScreen()

        for (point in TOUCH_SAMPLES) {
            touch(SWITCH_RECORDS, point)
            node(SWITCH_RECORDS).assertIsSelected()
            node(SWITCH_ENTRIES).assertIsNotSelected()
            node(RECORDS_CHIP_ROW).assertExists()
            composeRule.onNodeWithText(ENTRIES_COMMITTED.date.toString()).assertDoesNotExist()

            touch(SWITCH_ENTRIES, point)
            node(SWITCH_ENTRIES).assertIsSelected()
            node(SWITCH_RECORDS).assertIsNotSelected()
            node(RECORDS_CHIP_ROW).assertDoesNotExist()
            composeRule.onNodeWithText(ENTRIES_COMMITTED.date.toString()).assertExists()
        }
    }

    @Test
    fun `Back from Records steps to Entries`() {
        setScreen()
        touch(SWITCH_RECORDS, Offset(0.5f, 0.5f))
        node(SWITCH_RECORDS).assertIsSelected()

        pressBack()

        node(SWITCH_ENTRIES).assertIsSelected()
        node(SWITCH_RECORDS).assertIsNotSelected()
        node(RECORDS_CHIP_ROW).assertDoesNotExist()
    }
}

private const val SWITCH_ENTRIES = "journal-switch-entries"
private const val SWITCH_RECORDS = "journal-switch-records"
private const val RECORDS_CHIP_ROW = "records-filter-chip-row"

/** Three touches spread across a control: near its start edge, its centre, near its end edge, at differing heights. */
private val TOUCH_SAMPLES = listOf(Offset(0.12f, 0.3f), Offset(0.5f, 0.5f), Offset(0.88f, 0.7f))

private val ENTRIES_COMMITTED = CartographyEntry.draft(id = "committed-1", date = LocalDate.of(2026, 8, 1), updatedAtEpochMillis = 1_000L)
    .copy(isDraft = false)

private val ENTRIES_EMPTY_MAP_DATA = CartographyEntryMapData(
    trackPolylines = emptyList(),
    findMarkers = emptyList(),
    waypointMarkers = emptyList(),
    photoMarkers = emptyList(),
    offlineRegionCircles = emptyList(),
)

private val ENTRIES_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }
