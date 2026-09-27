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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.FindDecision
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.OfflineRegionDecision
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackDecision
import com.zynergylabs.forager.app.domain.model.WaypointDecision
import com.zynergylabs.forager.app.domain.model.Region
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
 * Journal redesign J3 (`prompts/preserved/2026-09-27-20.md`; plan J5, J9): the Entries timeline's
 * cards, driven through the real [JournalTab] with its Cartography callbacks wired to local state,
 * the way [JournalEntriesTest] does.
 *
 * Cards are checked by their **rendered text** (the plan's rule): title, day numeral and weekday,
 * species chip text, each stat's text with its unit. Every card or row a finger taps is touched with
 * `performTouchInput` at several points across its own bounds (CLAUDE.md, "A semantic
 * `performClick` asserts wiring, not routing"). Tags are literals, so each step's tests compile
 * against the base they were written before.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class JournalEntryCardsTest {

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

    private fun setScreen(
        entries: List<CartographyEntry>,
        galleryPhotos: List<GalleryPhoto> = emptyList(),
        tracks: List<Track> = emptyList(),
        logState: MushroomLogUiState = MushroomLogUiState(),
        distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    ) {
        composeRule.setContent {
            var cartographyState by remember { mutableStateOf(CartographyUiState(entries = entries)) }
            JournalTab(
                uiState = logState,
                onOpenCameraForLogEntry = {},
                onOpenCameraForAlbum = {},
                onOpenCameraForCartographyEntry = {},
                mapSlot = CARDS_STUB_MAP,
                pickerRegion = Region(lat = 45.326, lng = -122.634, radiusKm = 15),
                basemap = Basemap.DEFAULT,
                onOpenEntry = {},
                onCloseEntry = {},
                onStartEntry = { _, _ -> },
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
                cartographyUiState = cartographyState,
                onOpenCartographyEntry = { id ->
                    openedCartographyIds += id
                    cartographyState = cartographyState.copy(editingEntry = cartographyState.entries.first { it.id == id })
                },
                onStartCartographyEntry = {},
                onCloseCartographyEntry = { cartographyState = cartographyState.copy(editingEntry = null) },
                onCartographyTextChanged = {},
                onCartographyTagsChanged = {},
                onSetFindDecision = { _, _ -> },
                onSetTrackDecision = { _, _ -> },
                onSetWaypointDecision = { _, _ -> },
                onSetOfflineRegionDecision = { _, _ -> },
                onToggleKeptPhoto = {},
                onFinishCartographyEntry = {},
                onDeleteCartographyEntry = {},
                getCartographyEntryMapData = { _, _ -> CARDS_EMPTY_MAP_DATA },
                getCartographyEntryOfflineRegion = { _, _ -> null },
                getCartographyEntryCurrentLocation = { LocationResult.LocationUnavailable },
                availabilityUiState = AvailabilityUiState(),
                distanceUnit = distanceUnit,
                currentTime = CurrentTimeProvider { 0L },
                onOfflineMapLatChanged = {},
                onOfflineMapLngChanged = {},
                onOfflineMapRadiusChanged = {},
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                tracks = tracks,
                onTracksOpened = {},
                waypoints = emptyList(),
                waypointsErrorMessage = null,
                onDeleteWaypoint = {},
            )
        }
        composeRule.waitForIdle()
    }

    private fun node(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)

    private fun pressBack() {
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
    }

    /**
     * A node showing exactly [text] inside the card or row tagged [cardTag]. The unmerged tree: a
     * clickable `Card` merges its children's text into its own node, so in the merged tree no child
     * text node has the card as an ancestor.
     */
    private fun textIn(cardTag: String, text: String): SemanticsNodeInteraction =
        composeRule.onNode(hasText(text) and hasAnyAncestor(hasTestTag(cardTag)), useUnmergedTree = true)

    private fun scrollListTo(tag: String) {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasTestTag(tag))
        composeRule.waitForIdle()
    }

    // ── C1: the cards (plan J5) ──

    @Test
    fun `a card shows the day numeral and weekday, the first line as its title, and no ISO date`() {
        setScreen(listOf(FULL_ENTRY))

        node(cardTag(FULL_ENTRY.id)).assertIsDisplayed()
        textIn(cardTag(FULL_ENTRY.id), "26").assertIsDisplayed()
        textIn(cardTag(FULL_ENTRY.id), "SAT").assertIsDisplayed()
        textIn(cardTag(FULL_ENTRY.id), "Chanterelles along the ridge").assertIsDisplayed()
        // The rest of the writing is not part of the title.
        composeRule.onNodeWithText("Chanterelles along the ridge\nWet slope under Doug-fir").assertDoesNotExist()
        composeRule.onNodeWithText("2026-09-26", substring = true).assertDoesNotExist()
    }

    @Test
    fun `a card shows kept finds' identifications as species chips, each species once, withheld finds left out`() {
        setScreen(listOf(FULL_ENTRY))

        composeRule.onAllNodes(hasText("C. formosus") and hasAnyAncestor(hasTestTag(cardTag(FULL_ENTRY.id))), useUnmergedTree = true).assertCountEquals(1)
        textIn(cardTag(FULL_ENTRY.id), "B. edulis").assertIsDisplayed()
        // Withheld: not part of the entry, so no chip.
        composeRule.onNodeWithText("A. muscaria").assertDoesNotExist()
    }

    @Test
    fun `a card's stats row shows each kept type's figure with its unit, in kilometres`() {
        setScreen(listOf(FULL_ENTRY), distanceUnit = DistanceUnit.KILOMETERS)

        val card = cardTag(FULL_ENTRY.id)
        textIn(card, "3 finds").assertIsDisplayed()
        // 4200 m and 2 h 10 min: the app's own track formatter (trackSubtitle), kept tracks only.
        textIn(card, "4.2 km · 2h 10m").assertIsDisplayed()
        textIn(card, "1 waypoint").assertIsDisplayed()
        textIn(card, "2 offline maps").assertIsDisplayed()
        // The old one-number summary is gone.
        composeRule.onNodeWithText("kept item", substring = true).assertDoesNotExist()
    }

    @Test
    fun `a card's track stat follows the user's distance unit`() {
        setScreen(listOf(FULL_ENTRY), distanceUnit = DistanceUnit.MILES)

        // 4200 m = 2.61 mi.
        textIn(cardTag(FULL_ENTRY.id), "2.6 mi · 2h 10m").assertIsDisplayed()
        composeRule.onNodeWithText("4.2 km", substring = true).assertDoesNotExist()
    }

    @Test
    fun `singular stats read 1 find, 1 waypoint, 1 offline map, and a type with nothing kept shows no stat`() {
        setScreen(listOf(SINGLES_ENTRY))

        val card = cardTag(SINGLES_ENTRY.id)
        textIn(card, "1 find").assertIsDisplayed()
        textIn(card, "1 offline map").assertIsDisplayed()
        composeRule.onNodeWithText("waypoint", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText(" km", substring = true).assertDoesNotExist()
    }

    @Test
    fun `entries sit under month headers, newest month first`() {
        setScreen(listOf(FULL_ENTRY, AUGUST_ENTRY))

        node(monthTag("2026-09")).assertIsDisplayed()
        node(monthTag("2026-09")).assert(hasText("SEPTEMBER 2026"))
        scrollListTo(monthTag("2026-08"))
        node(monthTag("2026-08")).assertIsDisplayed().assert(hasText("AUGUST 2026"))

        val sep = node(monthTag("2026-09")).getUnclippedBoundsInRoot()
        val aug = node(monthTag("2026-08")).getUnclippedBoundsInRoot()
        val fullCard = node(cardTag(FULL_ENTRY.id)).getUnclippedBoundsInRoot()
        val augCard = node(cardTag(AUGUST_ENTRY.id)).getUnclippedBoundsInRoot()
        assertTrue("September's card comes after its header", fullCard.top >= sep.bottom)
        assertTrue("August's header comes after September's card", aug.top >= fullCard.bottom)
        assertTrue("August's card comes after its header", augCard.top >= aug.bottom)
    }

    @Test
    fun `the month header stays pinned at the top while that month's cards scroll under it`() {
        setScreen(MANY_SEPTEMBER)

        val headerBefore = node(monthTag("2026-09")).getUnclippedBoundsInRoot()
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(MANY_SEPTEMBER.size)
        composeRule.waitForIdle()

        // The first card has scrolled away (gone, or moved up under the header); the header has not.
        val firstGone = composeRule.onAllNodes(hasTestTag(cardTag(MANY_SEPTEMBER.first().id))).fetchSemanticsNodes().isEmpty() ||
            node(cardTag(MANY_SEPTEMBER.first().id)).getUnclippedBoundsInRoot().top < headerBefore.top
        assertTrue("the list scrolled", firstGone)
        node(monthTag("2026-09")).assertIsDisplayed()
        val headerAfter = node(monthTag("2026-09")).getUnclippedBoundsInRoot()
        val lastCard = node(cardTag(MANY_SEPTEMBER.last().id)).getUnclippedBoundsInRoot()
        assertTrue("the header has not scrolled up out of place (${headerBefore.top} -> ${headerAfter.top})", headerAfter.top <= headerBefore.top + 1.dp && headerAfter.top >= 0.dp)
        assertTrue("the header sits above the cards now showing", headerAfter.bottom <= lastCard.top)
    }

    @Test
    fun `an entry with no photo, no text and no track collapses to one short row with its day and stats`() {
        setScreen(listOf(FULL_ENTRY, BARE_ENTRY))
        scrollListTo(rowTag(BARE_ENTRY.id))

        node(rowTag(BARE_ENTRY.id)).assertIsDisplayed()
        node(cardTag(BARE_ENTRY.id)).assertDoesNotExist()
        textIn(rowTag(BARE_ENTRY.id), "14").assertIsDisplayed()
        textIn(rowTag(BARE_ENTRY.id), "MON").assertIsDisplayed()
        textIn(rowTag(BARE_ENTRY.id), "1 offline map").assertIsDisplayed()
        val row = node(rowTag(BARE_ENTRY.id)).getUnclippedBoundsInRoot()
        val card = node(cardTag(FULL_ENTRY.id)).getUnclippedBoundsInRoot()
        assertTrue("the short row is one line tall (${row.bottom - row.top})", row.bottom - row.top <= 64.dp)
        assertTrue("and shorter than a full card", (row.bottom - row.top) < (card.bottom - card.top))
        // The full card, beside it, is not collapsed.
        node(rowTag(FULL_ENTRY.id)).assertDoesNotExist()
    }

    @Test
    fun `an entry with nothing kept and nothing written collapses and says so`() {
        setScreen(listOf(EMPTY_ENTRY))

        node(rowTag(EMPTY_ENTRY.id)).assertIsDisplayed()
        textIn(rowTag(EMPTY_ENTRY.id), "Nothing kept").assertIsDisplayed()
    }

    @Test
    fun `touching a card at several points opens that entry`() {
        setScreen(listOf(FULL_ENTRY, AUGUST_ENTRY))

        for ((i, point) in TOUCH_SAMPLES.withIndex()) {
            node(cardTag(FULL_ENTRY.id)).performTouchInput { click(Offset(width * point.x, height * point.y)) }
            composeRule.waitForIdle()
            assertEquals(List(i + 1) { FULL_ENTRY.id }, openedCartographyIds)
            node(ENTRIES_HOME).assertDoesNotExist()
            pressBack()
            node(ENTRIES_HOME).assertIsDisplayed()
        }
    }

    @Test
    fun `touching a collapsed row at several points opens that entry`() {
        setScreen(listOf(BARE_ENTRY, EMPTY_ENTRY))

        for ((i, point) in TOUCH_SAMPLES.withIndex()) {
            node(rowTag(BARE_ENTRY.id)).performTouchInput { click(Offset(width * point.x, height * point.y)) }
            composeRule.waitForIdle()
            assertEquals(List(i + 1) { BARE_ENTRY.id }, openedCartographyIds)
            node(ENTRIES_HOME).assertDoesNotExist()
            pressBack()
            node(ENTRIES_HOME).assertIsDisplayed()
        }
    }
}

// Literals, not the production constants, so these compile against the base they were written before.
private const val ENTRIES_HOME = "entries-home"
private fun cardTag(entryId: String): String = "entry-card-$entryId"
private fun rowTag(entryId: String): String = "entry-row-$entryId"
private fun monthTag(yearMonth: String): String = "entries-month-$yearMonth"

/** Three touches spread across a control: near its start edge, its centre, near its end edge, at differing heights. */
private val TOUCH_SAMPLES = listOf(Offset(0.12f, 0.3f), Offset(0.5f, 0.5f), Offset(0.88f, 0.7f))

private fun committed(id: String, date: LocalDate, updatedAt: Long = 0L): CartographyEntry =
    CartographyEntry.draft(id = id, date = date, updatedAtEpochMillis = updatedAt).copy(isDraft = false)

private fun find(id: String, identification: String?, kept: Boolean = true) =
    FindDecision(findId = id, foundOn = LocalDate.of(2026, 9, 26), ownIdentification = identification, hasPhotos = false, kept = kept)

private fun trackDecision(id: String, meters: Double, millis: Long, kept: Boolean = true, points: Int = 10) =
    TrackDecision(trackId = id, name = null, distanceMeters = meters, durationMillis = millis, pointCount = points, kept = kept)

private fun waypoint(id: String, kept: Boolean = true) = WaypointDecision(waypointId = id, name = "Pin $id", lat = 45.0, lng = -122.0, kept = kept)

private fun region(id: Long, kept: Boolean = true) =
    OfflineRegionDecision(offlineRegionId = id, name = "Region $id", lat = 45.0, lng = -122.0, radiusKm = 5, kept = kept)

/**
 * Saturday 2026-09-26: two lines of writing, three kept finds over two species and one withheld, one
 * kept track (4200 m, 2 h 10 min) and one withheld, one kept waypoint and one withheld, two kept
 * offline maps.
 */
private val FULL_ENTRY: CartographyEntry = committed("full", LocalDate.of(2026, 9, 26)).copy(
    text = "Chanterelles along the ridge\nWet slope under Doug-fir",
    findDecisions = listOf(
        find("f1", "C. formosus"),
        find("f2", "B. edulis"),
        find("f3", "C. formosus"),
        find("f4", "A. muscaria", kept = false),
    ),
    trackDecisions = listOf(
        trackDecision("t-kept", meters = 4_200.0, millis = (2 * 60 + 10) * 60_000L),
        trackDecision("t-withheld", meters = 9_000.0, millis = 60 * 60_000L, kept = false),
    ),
    waypointDecisions = listOf(waypoint("w1"), waypoint("w2", kept = false)),
    offlineRegionDecisions = listOf(region(1), region(2)),
)

/** One of each countable type, no track, some writing (so it does not collapse). */
private val SINGLES_ENTRY: CartographyEntry = committed("singles", LocalDate.of(2026, 9, 20)).copy(
    text = "Quick loop",
    findDecisions = listOf(find("s1", null)),
    waypointDecisions = listOf(waypoint("sw", kept = false)),
    offlineRegionDecisions = listOf(region(7)),
)

/** Monday 2026-09-14: no text, no photo, no track; one kept offline map. */
private val BARE_ENTRY: CartographyEntry = committed("bare", LocalDate.of(2026, 9, 14)).copy(
    offlineRegionDecisions = listOf(region(3)),
)

/** Nothing kept, nothing written. */
private val EMPTY_ENTRY: CartographyEntry = committed("empty", LocalDate.of(2026, 9, 10))

private val AUGUST_ENTRY: CartographyEntry = committed("august", LocalDate.of(2026, 8, 30)).copy(text = "Late summer scouting")

/** Twenty-eight September entries with writing, newest first: more than the screen holds. */
private val MANY_SEPTEMBER: List<CartographyEntry> = (28 downTo 1).map { day ->
    committed("sep-$day", LocalDate.of(2026, 9, day)).copy(text = "Walk on the $day")
}

private val CARDS_EMPTY_MAP_DATA = CartographyEntryMapData(
    trackPolylines = emptyList(),
    findMarkers = emptyList(),
    waypointMarkers = emptyList(),
    photoMarkers = emptyList(),
    offlineRegionCircles = emptyList(),
)

private val CARDS_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }
