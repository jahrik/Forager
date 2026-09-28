package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * The search bar's copy before any search (owner, 2026-09-28: "Change it to 'September · Search a
 * location'", so the line gives "a spark or motivation to action"), and, on the medium/expanded
 * layout, the owner's ruling that the summary's tap opens the search panel ("Make the tap open
 * search (Recommended)", planner message 2026-09-28-35) even before any search has run.
 */
private fun declareHostActivity() = object : ExternalResource() {
    override fun before() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
    }
}

/** Nothing searched yet: no region, no species query; September, so the month is fixed. */
private val UNSEARCHED_SEPTEMBER = AvailabilityUiState(region = null, selectedMonth = 9)

private const val SEPTEMBER_SEARCH_A_LOCATION = "September · Search a location"

private val SummaryStubMapSlot: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier.testTag("map-slot")) }

private fun ComposeContentTestRule.setUnsearchedScreen(onReopenTaxonSuggestions: () -> Unit = {}) {
    setContent {
        AvailabilityScreen(
            uiState = UNSEARCHED_SEPTEMBER,
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
            onReopenTaxonSuggestions = onReopenTaxonSuggestions,
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
            mapSlot = SummaryStubMapSlot,
        )
    }
    waitForIdle()
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class CompactSearchBarCopyTest {

    private val composeRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity()).around(composeRule)

    @Test
    fun `before any search the compact search bar reads September, Search a location`() {
        composeRule.setUnsearchedScreen()

        composeRule.onNodeWithText(SEPTEMBER_SEARCH_A_LOCATION, useUnmergedTree = true).assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w840dp-h1024dp-mdpi")
class WideSearchSummaryTest {

    private val composeRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity()).around(composeRule)

    private fun touchAt(x: Dp, y: Dp) {
        val at = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
        composeRule.onRoot().performTouchInput { click(at) }
        composeRule.waitForIdle()
    }

    private fun openSettingsPanel() {
        composeRule.onNodeWithText("Settings").performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Back to search options").assertIsDisplayed()
    }

    @Test
    fun `before any search the wide summary reads September, Search a location`() {
        composeRule.setUnsearchedScreen()

        composeRule.onNodeWithText(SEPTEMBER_SEARCH_A_LOCATION, useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Real touches at three points across the summary's own bounds, each from the Settings panel:
     * each one brings the drawer back to its search panel, with nothing searched yet. The last
     * species query is still reopened on the same tap, as before.
     */
    @Test
    fun `a real touch anywhere across the wide summary opens the search panel before any search`() {
        var reopened = 0
        composeRule.setUnsearchedScreen(onReopenTaxonSuggestions = { reopened++ })
        val summary = composeRule.onNodeWithTag(WIDE_SEARCH_SUMMARY_TAG).getUnclippedBoundsInRoot()
        val midY = (summary.top + summary.bottom) / 2

        val points = listOf(summary.left + 8.dp, (summary.left + summary.right) / 2, summary.right - 8.dp)
        for (x in points) {
            openSettingsPanel()

            touchAt(x, midY)

            assertEquals("the Settings panel has closed after a touch at $x", 0, composeRule.onAllNodesWithContentDescription("Back to search options").fetchSemanticsNodes().size)
            composeRule.onNodeWithText("Advanced search").assertIsDisplayed()
            // Continuation 2026-09-28-38 (owner: "Yes it should"): the location controls show at once.
            composeRule.onNodeWithText("Use current location").assertIsDisplayed()
            composeRule.onNodeWithText("Latitude").assertIsDisplayed()
        }
        assertEquals("each touch reached the summary", points.size, reopened)
    }

    /**
     * Continuation 2026-09-28-38: only the summary's tap expands "Advanced search". A collapse the
     * user makes inside the panel afterwards stands, and the other way back into the panel, the
     * Settings back arrow, opens it collapsed as it always has.
     */
    @Test
    fun `the summary's tap expands Advanced search, the user's own collapse stands, and the back arrow opens it collapsed as before`() {
        composeRule.setUnsearchedScreen()
        val summary = composeRule.onNodeWithTag(WIDE_SEARCH_SUMMARY_TAG).getUnclippedBoundsInRoot()
        openSettingsPanel()

        touchAt((summary.left + summary.right) / 2, (summary.top + summary.bottom) / 2)
        composeRule.onNodeWithText("Use current location").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Collapse Advanced search").performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Expand Advanced search").assertIsDisplayed()
        assertEquals("collapsed by the user", 0, composeRule.onAllNodesWithText("Use current location").fetchSemanticsNodes().size)

        openSettingsPanel()
        composeRule.onNodeWithContentDescription("Back to search options").performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Expand Advanced search").assertIsDisplayed()
        assertEquals("the back arrow opens it collapsed", 0, composeRule.onAllNodesWithText("Use current location").fetchSemanticsNodes().size)
    }
}
