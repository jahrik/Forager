package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-697: the search dropdown's Search button searches the coordinates in its fields.
 * Left as the fields already hold them, it searches those; typed over, it searches what was typed.
 *
 * The screen is the real [AvailabilityScreen]; the coordinate fields' state is held here the way the
 * ViewModel holds it (`onManualLatChanged`/`onManualLngChanged` write it, the fields read it back), and
 * `onSearchManualCoordinates` records the fields as they stand when it is called — what the ViewModel's
 * `searchManualCoordinates` reads. That function's own parsing and validation are unchanged by this
 * dispatch; the screen tests that drive the real ViewModel through this button (the `searchAReferenceRegion`
 * helpers, e.g. [AvailabilityScreenConditionsMonthTest]) carry the search the rest of the way.
 *
 * **What this does not show.** The dispatch's premise was that the fields are "prefilled with the
 * current position, as today". They are not: nothing fills them when the dropdown opens. They hold what
 * the last "Use current location", Set on map or recent search wrote, and are empty before any of those
 * (`AvailabilityUiState.manualLatText`'s default). The "prefilled" case below starts from fields holding
 * coordinates, as a past "Use current location" would have left them; it is not evidence that Search
 * reaches the current position. That is the dispatch's stop, reported to the planner.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class SearchDropdownSearchesTheFieldsTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private var state by mutableStateOf(AvailabilityUiState())
    private val searched = mutableListOf<Pair<String, String>>()

    private val mapSlot: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier.testTag(LAYOUT_FIXES_MAP_TAG)) }

    private fun setScreen(initial: AvailabilityUiState) {
        state = initial
        composeRule.setContent { Screen() }
        composeRule.waitForIdle()
    }

    private fun openDropdown() {
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG, useUnmergedTree = true).performTouchInput { click(center) }
        composeRule.waitForIdle()
    }

    /** A real touch at the Search button's centre, at screen coordinates. */
    private fun touchSearch() {
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_SEARCH_TAG).performScrollTo()
        composeRule.waitForIdle()
        val at: Offset = composeRule.onNodeWithTag(SEARCH_DROPDOWN_SEARCH_TAG).fetchSemanticsNode().boundsInRoot.center
        composeRule.onAllNodes(isRoot()).onFirst().performTouchInput { click(at) }
        composeRule.waitForIdle()
    }

    @Test
    fun `Search with the fields left as they are searches the coordinates they hold`() {
        setScreen(AvailabilityUiState(manualLatText = "45.5231", manualLngText = "-122.6765"))
        openDropdown()

        touchSearch()

        assertEquals(listOf("45.5231" to "-122.6765"), searched)
    }

    @Test
    fun `Search after typing over the fields searches what was typed`() {
        setScreen(AvailabilityUiState(manualLatText = "45.5231", manualLngText = "-122.6765"))
        openDropdown()

        composeRule.onNodeWithTag(SEARCH_DROPDOWN_LATITUDE_TAG).performTextReplacement("44.0521")
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_LONGITUDE_TAG).performTextReplacement("-123.0868")
        touchSearch()

        assertEquals(listOf("44.0521" to "-123.0868"), searched)
    }

    @Composable
    private fun Screen() {
        AvailabilityScreen(
            uiState = state,
            onUseCurrentLocation = {},
            onManualLatChanged = { state = state.copy(manualLatText = it) },
            onManualLngChanged = { state = state.copy(manualLngText = it) },
            onSearchManualCoordinates = { searched += state.manualLatText to state.manualLngText },
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
}
