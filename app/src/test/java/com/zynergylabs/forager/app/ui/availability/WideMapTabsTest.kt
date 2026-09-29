package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.AvailabilityEntry
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.AvailabilityForecast
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.ui.log.CartographyUiState
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.MapOverlayContent
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
import org.robolectric.annotation.GraphicsMode

/**
 * J6b, the tablet map (dispatch 2026-09-28-152, items 11-13; the owner's rulings in
 * `docs/plans/journal-redesign.md`, "J6 design rulings (owner, 2026-09-29)", 2 and 6.6): the wide
 * results pane, through the real [AvailabilityScreen].
 *
 * - **Item 11.** One constant, a minimum map width of 480 dp. When List beside the map would leave the
 *   map narrower than that, List and Maps are real tabs, each across the whole right side; otherwise
 *   they stay side by side. The boundary is pinned from both sides: the drawer (360) plus the list (360)
 *   plus the divider (1) plus 480 is a 1,201 dp window.
 * - **Item 12.** The chip row over the map clears the Layers button at every width.
 * - **Item 13.** The map is drawn before any search, with the planned trips.
 *
 * The map is a stub that records what it is handed, so "the planned trips are drawn" is read from the
 * data the map receives, not from a proxy for it. Robolectric draws no real map, so nothing here says
 * anything about the map's own rendering or gestures; that is on the device-only list. Written before
 * the build and pushed: each test named "FAILS AT BASE" fails at the base for the reason its message
 * gives; each named "GUARD" already holds and is pinned.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w824dp-h1318dp-mdpi")
class WideMapTabsTest {

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

    private var capturedContent: MapOverlayContent? = null
    private var capturedRegion: Region? = null

    private val capturingMapSlot: MapSlot = { region, content, _, _, _, _, _, _, modifier ->
        capturedRegion = region
        capturedContent = content
        Box(modifier.testTag(MAP_SLOT))
    }

    private fun setScreen(uiState: AvailabilityUiState, cartographyUiState: CartographyUiState = CartographyUiState()) {
        composeRule.setContent {
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
                cartographyUiState = cartographyUiState,
                mapSlot = capturingMapSlot,
            )
        }
        composeRule.waitForIdle()
    }

    private fun mapShowing() = composeRule.onAllNodesWithTag(MAP_SLOT).fetchSemanticsNodes().isNotEmpty()
    private fun listShowing() = composeRule.onAllNodesWithText("View on Map").fetchSemanticsNodes().isNotEmpty()
    private fun mapBounds() = composeRule.onNodeWithTag(MAP_SLOT).getUnclippedBoundsInRoot()
    private fun listBounds() = composeRule.onNodeWithText("View on Map").getUnclippedBoundsInRoot()

    private fun assertWidth(what: String, actual: Dp, expected: Dp) =
        assertTrue("$what is $actual wide, expected about $expected", actual in (expected - 2.dp)..(expected + 2.dp))

    // ── Item 11: the narrow map ──

    @Test
    fun `FAILS AT BASE in portrait the map would be about 100 dp beside the list, so List and Maps are tabs across the right side`() {
        setScreen(SEARCHED_STATE)
        composeRule.onNodeWithText("Maps").assertIsSelected()

        assertTrue("Maps shows the map", mapShowing())
        assertWidth("the map on the Maps tab", mapBounds().width, 464.dp)
        assertEquals("and not the list beside it", false, listShowing())

        composeRule.onNodeWithText("List").performClick()
        composeRule.waitForIdle()

        assertTrue("List shows the list", listShowing())
        assertWidth("the list on the List tab", composeRule.onNodeWithTag(LIST_PANE).getUnclippedBoundsInRoot().width, 464.dp)
        assertEquals("and not the map beside it", false, mapShowing())
    }

    @Test
    @Config(qualifiers = "w1318dp-h824dp-mdpi")
    fun `GUARD in landscape the map is 596 dp beside a 360 dp list, so both stay side by side`() {
        setScreen(SEARCHED_STATE)

        assertTrue("the map is showing", mapShowing())
        assertTrue("the list is showing", listShowing())
        assertWidth("the map", mapBounds().width, 596.dp)
        // The list's width, from the map's left edge: the 360 dp drawer, the list, a 1 dp divider.
        assertWidth("the list", mapBounds().left - 361.dp, 360.dp)
    }

    @Test
    @Config(qualifiers = "w1201dp-h900dp-mdpi")
    fun `GUARD at 1201 dp the map is exactly 480 dp, not narrower, so both stay side by side`() {
        setScreen(SEARCHED_STATE)

        assertTrue("the list is showing", listShowing())
        assertTrue("the map is showing", mapShowing())
        assertWidth("the map", mapBounds().width, 480.dp)
    }

    @Test
    @Config(qualifiers = "w1200dp-h900dp-mdpi")
    fun `FAILS AT BASE at 1200 dp the map would be 479 dp, one under the minimum, so they are tabs`() {
        setScreen(SEARCHED_STATE)

        assertTrue("the map is showing", mapShowing())
        assertEquals("the list is not beside it", false, listShowing())
        assertWidth("the map across the right side", mapBounds().width, 840.dp)
    }

    @Test
    @Config(qualifiers = "w840dp-h1024dp-mdpi")
    fun `FAILS AT BASE at 840 dp the map would be 119 dp, so they are tabs`() {
        setScreen(SEARCHED_STATE)

        assertEquals("the list is not beside the map", false, listShowing())
        assertWidth("the map across the right side", mapBounds().width, 480.dp)
    }

    @Test
    fun `FAILS AT BASE View on Map from the list tab lands on the map, filtered, across the right side`() {
        setScreen(SEARCHED_STATE)
        composeRule.onNodeWithText("List").performClick()
        composeRule.waitForIdle()
        assertTrue("on the list tab the list is showing", listShowing())

        composeRule.onNodeWithText("View on Map").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Maps").assertIsSelected()
        composeRule.onNodeWithText("Showing: Pacific Golden Chanterelle (1)").assertExists()
        assertEquals(listOf(MATCHING_SIGHTING), capturedContent?.sightings)
        assertEquals("the list is gone: the map has the right side", false, listShowing())
        assertWidth("the map", mapBounds().width, 464.dp)
    }

    // ── Item 12: the chip row against the Layers button ──

    /**
     * The row the refresh pulse measured: the taxon chip ("Showing: ...", from View on Map) and J8's "N journal
     * entries on map" chip, side by side at the map's top centre. Either chip's right edge must be left of the
     * Layers button's left edge; a centred row of both is about 406 dp, so it runs under the button on any map
     * narrower than about 406 + 2 x 64 dp.
     */
    private fun assertChipsClearOfLayers(where: String) {
        composeRule.onNodeWithText("List").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("View on Map").performClick()
        composeRule.waitForIdle()
        val layers = composeRule.onNodeWithTag(WIDE_LAYERS_BUTTON_TAG).getUnclippedBoundsInRoot()
        for ((name, tag) in listOf("taxon chip" to TAXON_CHIP, "journal entries chip" to JOURNAL_ENTRIES_CHIP_TAG)) {
            val chip = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertTrue("$where: the $name ends at ${chip.right}, the Layers button starts at ${layers.left}", chip.right <= layers.left)
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `FAILS AT BASE the chip row clears the Layers button on the tabbed map`() {
        setScreen(SEARCHED_STATE, cartographyUiState = ONE_SHOWN_ENTRY)
        assertChipsClearOfLayers("portrait, the map across 464 dp")
    }

    /**
     * **Native graphics, and why.** Robolectric's default graphics mode measures text at close to no width, so
     * the chips were 84 dp and 47 dp wide there and nothing could overlap; a first version of these tests
     * (a taxon chip alone, then a long species name to stress it) passed at base for that reason, and a
     * revert of the inset alone left every test green. `@GraphicsMode(NATIVE)`, as the project's own
     * chip-fit guard uses, gives real text widths: the taxon chip 277 dp, "1 journal entry on map" 158 dp,
     * 443 dp on one line with the gap.
     *
     * Centred on a map W wide, with no inset, that row ends at (W + 443) / 2 + the row's 8 dp margin:
     * 1240 on the 596 dp map (Layers starts at 1262: clear, so a guard), 1182 on the 480 dp map (Layers
     * starts at 1145) and 816 on the tabbed 464 dp map (Layers starts at 768). The inset is what the last two
     * need: it narrows the row's room so it wraps to two lines clear of the button. The tabbed test also fails
     * at base for another reason (its map is 103 dp wide there), so the proof that the *inset* is what fixes
     * it is a revert of the inset alone.
     */
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(qualifiers = "w1318dp-h824dp-mdpi")
    fun `GUARD the chip row clears the Layers button on the 596 dp side by side map`() {
        setScreen(SEARCHED_STATE, cartographyUiState = ONE_SHOWN_ENTRY)
        assertChipsClearOfLayers("landscape, the map beside the list at 596 dp")
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(qualifiers = "w1201dp-h900dp-mdpi")
    fun `FAILS AT BASE the chip row clears the Layers button on the narrowest side by side map, 480 dp`() {
        setScreen(SEARCHED_STATE, cartographyUiState = ONE_SHOWN_ENTRY)
        assertChipsClearOfLayers("1201 dp, the map beside the list at exactly 480 dp")
    }

    // ── Item 13: a map before any search ──

    @Test
    @Config(qualifiers = "w1318dp-h824dp-mdpi")
    fun `FAILS AT BASE before any search the map is drawn with the planned trips`() {
        setScreen(AvailabilityUiState(plannedTrips = listOf(TRIP)))

        // The map's own message, exactly: the list beside it has its own "Choose a region to see ..." line, which a
        // substring match on "Choose a region" would take for this one (a first version did, and read the list).
        assertEquals("no 'choose a region' message stands in for the map", false, composeRule.onAllNodesWithText(MAP_NO_SEARCH_MESSAGE).fetchSemanticsNodes().isNotEmpty())
        assertTrue("the map is showing", mapShowing())
        assertEquals("the planned trips are handed to the map", listOf(TRIP), capturedContent?.plannedTrips)
        assertEquals("no sightings are plotted before a search", emptyList<Sighting>(), capturedContent?.sightings)
    }

    @Test
    fun `FAILS AT BASE before any search on the tabbed layout the Maps tab shows the map with the planned trips`() {
        setScreen(AvailabilityUiState(plannedTrips = listOf(TRIP)))

        composeRule.onNodeWithText("Maps").assertIsSelected()
        assertTrue("the map is showing", mapShowing())
        assertEquals(listOf(TRIP), capturedContent?.plannedTrips)
    }

    private companion object {
        const val MAP_SLOT = "map-slot"
        const val LIST_PANE = "wide-list-pane"
        const val TAXON_CHIP = "map-taxon-filter-chip"
        const val MAP_NO_SEARCH_MESSAGE = "Choose a region in search options to see mapped sightings."

        val REGION = Region(lat = 45.326, lng = -122.634, radiusKm = 15)
        val TRIP = PlannedTrip(id = "trip-1", name = "Creek loop", location = LatLng(45.33, -122.63), date = LocalDate.of(2026, 10, 3))

        fun species(common: String) = SpeciesObservationCount(taxonId = 47348, scientificName = "Cantharellus formosus", commonName = common, rank = "species", observationCount = 42, photoUrl = null, wikipediaUrl = null)

        val MATCHING_SIGHTING = Sighting(observationId = 1, taxonId = 47348, scientificName = "Cantharellus formosus", commonName = "Pacific Golden Chanterelle", lat = 45.33, lng = -122.64, observedOn = LocalDate.of(2026, 8, 1), photoUrl = null)
        val OTHER_SIGHTING = Sighting(observationId = 2, taxonId = 99999, scientificName = "Amanita muscaria", commonName = "Fly Agaric", lat = 45.34, lng = -122.65, observedOn = LocalDate.of(2026, 8, 2), photoUrl = null)

        val SEARCHED_STATE = AvailabilityUiState(
            region = REGION,
            forecast = AvailabilityForecast(region = REGION, month = 8, filter = TaxonFilter.FUNGI, entries = listOf(AvailabilityEntry(species = species("Pacific Golden Chanterelle"), relativeLikelihood = 1f))),
            sightings = listOf(MATCHING_SIGHTING, OTHER_SIGHTING),
        )

        val ONE_SHOWN_ENTRY = CartographyUiState(
            entries = listOf(CartographyEntry.draft(id = "entry-1", date = LocalDate.of(2026, 9, 12), updatedAtEpochMillis = 1_000L).copy(isDraft = false, text = "A walk.", shownOnMap = true)),
        )
    }
}
