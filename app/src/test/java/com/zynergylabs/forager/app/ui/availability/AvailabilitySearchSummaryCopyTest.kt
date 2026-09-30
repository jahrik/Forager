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

/**
 * Continuations 2026-09-28-39 and -40: what a real touch on the compact search bar shows. The
 * dropdown opens with "Set on map" and "Use current location" at its top level, and, since the
 * owner's "Also open manual coordinates" (-40), with "Advanced search" and "Enter coordinates
 * manually" both expanded, so Latitude, Longitude and "Search this location" show at once.
 */
private fun ComposeContentTestRule.assertCompactBarTapShowsLocationControls(topButtonsInView: Boolean = true) {
    setUnsearchedScreen()
    onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performTouchInput { click(center) }
    waitForIdle()

    onNodeWithTag(SEARCH_DROPDOWN_TAG).assertIsDisplayed()
    if (topButtonsInView) {
        onNodeWithText("Use current location").assertIsDisplayed()
        onNodeWithText("Set on map").assertIsDisplayed()
    } else {
        // Short landscape (owner, continuation 2026-09-28-41, "Expand and auto-scroll"): the
        // dropdown has scrolled them out of view to show the coordinates; they are still in it.
        onNodeWithText("Use current location").assertExists()
        onNodeWithText("Set on map").assertExists()
    }
    onNodeWithContentDescription("Collapse Advanced search").assertExists()
    onNodeWithContentDescription("Collapse Enter coordinates manually").assertExists()
    onNodeWithText("Latitude").assertIsDisplayed()
    onNodeWithText("Longitude").assertIsDisplayed()
    onNodeWithText("Search this location").assertIsDisplayed()
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class CompactSearchBarLocationControlsTest {

    private val composeRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity()).around(composeRule)

    @Test
    fun `in portrait a real touch on the compact search bar shows Set on map, Use current location and the manual coordinates at once`() {
        composeRule.assertCompactBarTapShowsLocationControls()
    }

    /** Continuation 2026-09-28-40: after the tap's one-time expand, a collapse the user makes stands. */
    @Test
    fun `in portrait the user's own collapse of the manual coordinates stands after the tap expanded them`() {
        composeRule.assertCompactBarTapShowsLocationControls()

        composeRule.onNodeWithContentDescription("Collapse Enter coordinates manually").performTouchInput { click(center) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Expand Enter coordinates manually").assertIsDisplayed()
        assertEquals("collapsed by the user", 0, composeRule.onAllNodesWithText("Latitude").fetchSemanticsNodes().size)
        composeRule.onNodeWithContentDescription("Collapse Advanced search").assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class CompactSearchBarLocationControlsShortLandscapeTest {

    private val composeRule = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity()).around(composeRule)

    @Test
    fun `in short landscape a real touch on the compact search bar scrolls the dropdown once to show the manual coordinates at once`() {
        composeRule.assertCompactBarTapShowsLocationControls(topButtonsInView = false)
    }
}
