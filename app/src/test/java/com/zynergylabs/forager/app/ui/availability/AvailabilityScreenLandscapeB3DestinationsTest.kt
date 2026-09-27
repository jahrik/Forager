package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.AvailabilityEntry
import com.zynergylabs.forager.app.domain.model.AvailabilityForecast
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
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
import org.robolectric.shadows.ShadowDisplay

/**
 * Landscape build B3, P11 (`docs/plans/landscape-phone-design.md`, as corrected by R7 and R9;
 * dispatch `prompts/preserved/2026-09-27-14.md`): in a short landscape window, on every tab but
 * Map, the content beside the opaque rail is capped at `READABLE_CONTENT_MAX_WIDTH` (640 dp) and
 * centred in the width the rail leaves.
 *
 * "The tab content" is measured per destination by an element that fills that destination's own
 * width, so what is measured is the width the destination was handed, not a caption that happens
 * to wrap short: List's species card (inside the tab's own 16 dp side padding), the Journal's top
 * tab row (Cartography's left edge to Records' right edge), the Records sub-tab row (Waypoint
 * Markers' left edge to Logged Finds' right edge), and Seasonal's own tagged column.
 *
 * Everything is driven through the real [AvailabilityScreen]; each rotation-specific test first
 * proves the screen read the pinned rotation, so the two port edges are really both sampled.
 * Robolectric reports zero insets, so "the area beside the rail" here is the window less the rail;
 * on a device it is also less the cut-out and system-bar insets, which is a device item (B4).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class AvailabilityScreenLandscapeB3DestinationsTest {

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

    private var rotationSeenByScreen: Int? = null

    private fun setScreen(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            rotationSeenByScreen = LocalView.current.display?.rotation
            b3Screen(uiState = B3_LIST_STATE, mapSlot = B3_STUB_MAP)
        }
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeenByScreen)
    }

    private fun bounds(text: String): DpRect = composeRule.onNodeWithText(text).getUnclippedBoundsInRoot()
    private fun taggedBounds(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun railBounds(): DpRect = taggedBounds(COMPACT_NAVIGATION_RAIL_TAG)
    private fun rootBounds(): DpRect = composeRule.onRoot().getUnclippedBoundsInRoot()

    /** The width the rail leaves: the window less the rail, on whichever side the rail is. */
    private fun areaBesideRail(): DpRect {
        val root = rootBounds()
        val rail = railBounds()
        return if (rail.centreX() > root.centreX()) {
            DpRect(root.left, root.top, rail.left, root.bottom)
        } else {
            DpRect(rail.right, root.top, root.right, root.bottom)
        }
    }

    /** A rail item selected by a semantic click — navigation to the destination under test, not a claim about touch. */
    private fun openTab(label: String) {
        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(label).assertIsSelected()
    }

    private fun assertCappedAndCentred(what: String, content: DpRect, allowance: Dp = 0.dp) {
        val area = areaBesideRail()
        assertTrue(
            "$what is at most $READABLE_WIDTH wide (plus $allowance of its own side padding): " +
                "it is ${content.width}, in an area ${area.width} wide beside the rail",
            content.width.value <= (READABLE_WIDTH + allowance).value + 0.5f,
        )
        assertEquals(
            "$what is centred in the area beside the rail ($area): content $content",
            area.centreX(), content.centreX(), 1f,
        )
    }

    private fun listContent(): DpRect = taggedBounds(SPECIES_ROW_TAG)

    /**
     * Journal redesign J5 (L1): in a short window the Entries | Records switch sits at the start of
     * the Journal's pinned 48 dp row, sized to its labels, so its segments no longer span the width
     * the Journal was handed. The row fills that width, so it is what is measured, as the Records
     * chip row is below. (Until J5 the switch's two segments spanned it and were measured.)
     */
    private fun journalTopTabs(): DpRect = taggedBounds(com.zynergylabs.forager.app.ui.log.SHORT_HEADER_TAG)

    /**
     * Journal redesign J1 (S3): the Records filter chip row, which replaced the sub-tab row. The
     * chips themselves size to their labels and scroll, so their outer edges no longer span the
     * width Records was handed; the row fills that width (`fillMaxWidth`), so it is what is measured.
     */
    private fun recordsSubTabs(): DpRect = taggedBounds(com.zynergylabs.forager.app.ui.log.RECORDS_FILTER_CHIP_ROW_TAG)

    // ── T1: List, Journal (its default top tab), Records ──

    private fun checkList(rotation: Int) {
        setScreen(rotation)
        openTab("List")
        composeRule.onNodeWithText("artist's bracket").assertIsDisplayed()
        // The species card sits inside ListTab's own 16 dp side padding, so the capped content is
        // the card plus 32 dp; the card itself is at most 640 - 32.
        assertCappedAndCentred("List's species card", listContent(), allowance = (-32).dp)
    }

    private fun checkJournal(rotation: Int) {
        setScreen(rotation)
        openTab("Journal")
        composeRule.onNodeWithTag(com.zynergylabs.forager.app.ui.log.journalSwitchTestTag(com.zynergylabs.forager.app.ui.log.JournalTopTab.CARTOGRAPHY)).assertIsSelected()
        assertCappedAndCentred("the Journal's top tab row", journalTopTabs())
    }

    private fun checkRecords(rotation: Int) {
        setScreen(rotation)
        openTab("Journal")
        composeRule.onNodeWithText("Records").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(com.zynergylabs.forager.app.ui.log.recordsFilterChipTestTag(com.zynergylabs.forager.app.ui.log.RecordsSubTab.ALL)).assertIsSelected()
        assertCappedAndCentred("the Records filter chip row", recordsSubTabs())
    }

    @Test fun `T1 at ROTATION_90 List is capped at 640 and centred beside the rail`() = checkList(Surface.ROTATION_90)
    @Test fun `T1 at ROTATION_270 List is capped at 640 and centred beside the rail`() = checkList(Surface.ROTATION_270)
    @Test fun `T1 at ROTATION_90 Journal is capped at 640 and centred beside the rail`() = checkJournal(Surface.ROTATION_90)
    @Test fun `T1 at ROTATION_270 Journal is capped at 640 and centred beside the rail`() = checkJournal(Surface.ROTATION_270)
    @Test fun `T1 at ROTATION_90 Records is capped at 640 and centred beside the rail`() = checkRecords(Surface.ROTATION_90)
    @Test fun `T1 at ROTATION_270 Records is capped at 640 and centred beside the rail`() = checkRecords(Surface.ROTATION_270)

    // ── T2: Seasonal, which caps itself already; guards that the two caps compose to 640, not less ──

    private fun checkSeasonal(rotation: Int) {
        setScreen(rotation)
        openTab("Seasonal")
        val content = taggedBounds(SEASONAL_CONTENT_TAG)
        assertCappedAndCentred("Seasonal's column", content)
        assertEquals("the two caps compose to 640, not less", READABLE_WIDTH.value, content.width.value, 0.5f)
    }

    @Test fun `T2 at ROTATION_90 Seasonal is 640 wide and centred beside the rail`() = checkSeasonal(Surface.ROTATION_90)
    @Test fun `T2 at ROTATION_270 Seasonal is 640 wide and centred beside the rail`() = checkSeasonal(Surface.ROTATION_270)

    // ── The search bar above the tab content: in the capped column too (coder's reading, see the completion report) ──

    @Test
    fun `the search bar above the tab content is capped and centred with it`() {
        setScreen(Surface.ROTATION_90)
        openTab("List")
        assertCappedAndCentred("the search bar", taggedBounds(SEARCH_ENTRY_BAR_TAG))
    }

    // ── T4: the rail stays tappable beside the capped content ──

    /**
     * Real touches at five points across a rail item's own bounds, each from the capped List tab,
     * each switching to Seasonal. The item's bounds are checked to lie in the rail first, so the
     * points sampled are the rail's, not a same-labelled control elsewhere.
     */
    private fun checkRailTappable(rotation: Int) {
        setScreen(rotation)
        val rail = railBounds()
        val fractions = listOf(0.5f to 0.5f, 0.2f to 0.2f, 0.8f to 0.2f, 0.2f to 0.8f, 0.8f to 0.8f)
        var tapped = 0
        for ((fx, fy) in fractions) {
            openTab("List")
            composeRule.onNodeWithTag(SPECIES_ROW_TAG).assertIsDisplayed()
            val item = bounds("Seasonal")
            assertTrue("the Seasonal item $item lies in the rail $rail", item.left >= rail.left && item.right <= rail.right)
            val point = Offset(
                (item.left + item.width * fx).value,
                (item.top + (item.bottom - item.top) * fy).value,
            )
            composeRule.onRoot().performTouchInput { click(Offset(point.x * density, point.y * density)) }
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Seasonal").assertIsSelected()
            composeRule.onNodeWithTag(SEASONAL_CONTENT_TAG).assertIsDisplayed()
            tapped++
        }
        assertEquals("every sampled point was tapped", fractions.size, tapped)
    }

    @Test fun `T4 at ROTATION_90 real touches across a rail item switch tab beside capped content`() = checkRailTappable(Surface.ROTATION_90)
    @Test fun `T4 at ROTATION_270 real touches across a rail item switch tab beside capped content`() = checkRailTappable(Surface.ROTATION_270)


    // ── T3: portrait is unchanged. A pin: it passes before and after B3 by design ──

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `T3 portrait List content fills the width, as today`() {
        composeRule.setContent { b3Screen(uiState = B3_LIST_STATE, mapSlot = B3_STUB_MAP) }
        composeRule.waitForIdle()
        assertTrue("portrait has no rail", composeRule.onAllNodesWithTag(COMPACT_NAVIGATION_RAIL_TAG).fetchSemanticsNodes().isEmpty())
        openTab("List")

        val root = rootBounds()
        val card = listContent()
        assertEquals("the card starts at the tab's 16 dp padding", (root.left + 16.dp).value, card.left.value, 0.5f)
        assertEquals("the card ends at the tab's 16 dp padding", (root.right - 16.dp).value, card.right.value, 0.5f)
    }
}

// ── shared fixtures (file-private; named apart from AvailabilityScreenShortLandscapeTest's) ──

private val READABLE_WIDTH = 640.dp
private const val SPECIES_ROW_TAG = "species-row"

private fun DpRect.centreX(): Float = ((left + right) / 2).value

private val B3_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

private val B3_REGION = Region(lat = 45.326, lng = -122.634, radiusKm = 15)

private val B3_LIST_STATE = AvailabilityUiState(
    region = B3_REGION,
    sightings = listOf(
        Sighting(
            observationId = 1L,
            taxonId = 48473L,
            scientificName = "Ganoderma applanatum",
            commonName = "artist's bracket",
            lat = B3_REGION.lat,
            lng = B3_REGION.lng,
            observedOn = LocalDate.of(2025, 8, 14),
            photoUrl = null,
        ),
    ),
    forecast = AvailabilityForecast(
        region = B3_REGION,
        month = 8,
        filter = TaxonFilter.FUNGI,
        entries = listOf(
            AvailabilityEntry(
                species = SpeciesObservationCount(
                    taxonId = 48473L,
                    scientificName = "Ganoderma applanatum",
                    commonName = "artist's bracket",
                    rank = "species",
                    observationCount = 14,
                    photoUrl = null,
                    wikipediaUrl = null,
                ),
                relativeLikelihood = 1.0f,
            ),
        ),
    ),
    selectedMonth = LocalDate.now().monthValue,
)

@androidx.compose.runtime.Composable
private fun b3Screen(uiState: AvailabilityUiState, mapSlot: MapSlot) {
    AvailabilityScreen(
        uiState = uiState,
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
        mapSlot = mapSlot,
    )
}
