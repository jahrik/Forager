package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.zynergylabs.forager.app.domain.AbsentForecastCellStore
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.domain.model.TaxonSearchResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FUNGI_HEADING = "Rain and fungi: the general pattern"

/** The two texts dispatch 2026-09-28-695 removed, as they read on screen before it. */
private const val REMOVED_NO_GUIDANCE_HEADING = "No weather guidance for this selection"
private const val REMOVED_SPECIES_NOTE = "No species-specific data is available for"

private val FLY_AGARIC = TaxonSearchResult(
    taxonId = 48715,
    scientificName = "Amanita muscaria",
    commonName = "Fly Agaric",
    rank = "species",
    iconicTaxonName = "Fungi",
    photoUrl = null,
)

/** A species in a group Forager has written no weather guidance for. */
private val LADYBIRD = TaxonSearchResult(
    taxonId = 48484,
    scientificName = "Harmonia axyridis",
    commonName = "Asian Lady Beetle",
    rank = "species",
    iconicTaxonName = "Insecta",
    photoUrl = null,
)

/**
 * Dispatch 2026-09-28-695: the Trip Windows card's guidance text, through the real [AvailabilityScreen] over the real
 * [AvailabilityViewModel] (`mapLayersViewModel`, `MapLayersTestScreen`), read in the Tools drawer's Trip Planner
 * section a user opens to see it.
 *
 * The species is picked by calling [AvailabilityViewModel.onTaxonSearchResultSelected], the callback a suggestion
 * row's tap calls, and not by typing and tapping a row: the shared fixture's species search returns no results, so
 * there is no row to tap. The region is searched the same way, through the callbacks the coordinate boxes and the
 * "Search this location" button call. Both are the screen's own entry points; what is read back is rendered text.
 *
 * The trip-window weather fetch fails in this fixture, so the card shows its error line above the guidance. That line
 * is what each test holds as proof the card itself rendered, so an absent guidance block is the card's choice and not
 * a card that never composed.
 *
 * A species reopened from a recent search is reopened through [AvailabilityViewModel.onRecentSearchSelected], the
 * callback a recent-search row's tap calls, with the entry the ViewModel itself loaded from the (in-memory) store
 * after the species had been searched by name (amendment 1, RECORD -696: the group is saved with the search).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class TripWindowsGuidanceTextTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private fun setScreenWithARegionSearched(): AvailabilityViewModel {
        val viewModel = mapLayersViewModel()
        composeRule.setContent {
            MapLayersTestScreen(viewModel = viewModel, mapSlot = LayersRecordingMapSlot().slot, store = AbsentForecastCellStore)
        }
        composeRule.waitForIdle()
        // The same reference coordinates AvailabilityScreenConditionsMonthTest types; not anyone's real place.
        composeRule.runOnIdle {
            viewModel.onManualLatChanged("45.326")
            viewModel.onManualLngChanged("-122.634")
            viewModel.searchManualCoordinates()
        }
        composeRule.waitForIdle()
        return viewModel
    }

    private fun pickByName(viewModel: AvailabilityViewModel, result: TaxonSearchResult) {
        composeRule.runOnIdle { viewModel.onTaxonSearchResultSelected(result) }
        composeRule.waitForIdle()
    }

    private fun openTripPlanner() {
        composeRule.onNodeWithText("Tools").performClick()
        composeRule.onNodeWithText("Trip Planner").performClick()
        composeRule.waitForIdle()
    }

    /** The card rendered: its title and its (failed) measurements line. */
    private fun assertTripWindowsCardShown() {
        composeRule.onNodeWithText("Trip Windows").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Couldn't load trip-window weather.").performScrollTo().assertIsDisplayed()
    }

    private fun assertNoRemovedText() {
        composeRule.onAllNodesWithText(REMOVED_NO_GUIDANCE_HEADING).assertCountEquals(0)
        composeRule.onAllNodesWithText(REMOVED_SPECIES_NOTE, substring = true).assertCountEquals(0)
    }

    @Test
    fun `a fungus species picked by name shows the fungi pattern and no species note`() {
        val viewModel = setScreenWithARegionSearched()
        pickByName(viewModel, FLY_AGARIC)
        assertEquals(
            "the precondition: the species is the selection",
            TaxonFilter.SpecificTaxon(taxonId = 48715, label = "Fly Agaric"),
            viewModel.uiState.value.taxonFilter,
        )

        openTripPlanner()

        assertTripWindowsCardShown()
        composeRule.onNodeWithText(FUNGI_HEADING).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("one to three weeks after sustained rain", substring = true).performScrollTo().assertIsDisplayed()
        assertNoRemovedText()
    }

    /**
     * The Fungi category, which is what the screen starts on (`ForagingSelection.forChip(TaxonFilter.FUNGI)`), shows
     * the same pattern and no note. No picker offers a species as a chip at this base, so this is the chip case.
     */
    @Test
    fun `the fungi category shows the fungi pattern and no species note`() {
        val viewModel = setScreenWithARegionSearched()
        assertEquals("the precondition: the default selection", TaxonFilter.FUNGI, viewModel.uiState.value.taxonFilter)

        openTripPlanner()

        assertTripWindowsCardShown()
        composeRule.onNodeWithText(FUNGI_HEADING).performScrollTo().assertIsDisplayed()
        assertNoRemovedText()
    }

    /**
     * Amendment 1 to -695 (RECORD -696; the owner, "Save it with the search"): Fly Agaric searched by name, then
     * another species picked so the selection is no longer Fly Agaric's, then Fly Agaric reopened from the recent
     * searches. Before the amendment this reopened with no group and so no guidance.
     */
    @Test
    fun `a fungus species reopened from a recent search shows the fungi pattern and no species note`() {
        val viewModel = setScreenWithARegionSearched()
        pickByName(viewModel, FLY_AGARIC)
        pickByName(viewModel, LADYBIRD)
        val flyAgaric = TaxonFilter.SpecificTaxon(taxonId = 48715, label = "Fly Agaric")
        val recent = composeRule.runOnIdle { viewModel.uiState.value.recentSearches.single { it.filter == flyAgaric } }

        composeRule.runOnIdle { viewModel.onRecentSearchSelected(recent) }
        composeRule.waitForIdle()
        assertEquals("the precondition: the reopened species is the selection", flyAgaric, viewModel.uiState.value.taxonFilter)

        openTripPlanner()

        assertTripWindowsCardShown()
        composeRule.onNodeWithText(FUNGI_HEADING).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("one to three weeks after sustained rain", substring = true).performScrollTo().assertIsDisplayed()
        assertNoRemovedText()
    }

    /** A selection whose group has no written guidance: the card ends at its measurements, with no heading at all. */
    @Test
    fun `a species in a group with no written guidance shows no guidance block at all`() {
        val viewModel = setScreenWithARegionSearched()
        pickByName(viewModel, LADYBIRD)
        assertEquals(
            "the precondition: the species is the selection",
            TaxonFilter.SpecificTaxon(taxonId = 48484, label = "Asian Lady Beetle"),
            viewModel.uiState.value.taxonFilter,
        )

        openTripPlanner()

        assertTripWindowsCardShown()
        composeRule.onAllNodesWithText(FUNGI_HEADING).assertCountEquals(0)
        composeRule.onAllNodesWithText("Rain and plants: no pattern to offer").assertCountEquals(0)
        assertNoRemovedText()
    }
}
