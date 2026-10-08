package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.zynergylabs.forager.app.domain.model.Region
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * RECORD -723: a "Clear" text button at the right end of the search bar, shown only while a search is showing. One
 * tap removes the observations from the map, clears the bar's summary back to "Search a location", and leaves recent
 * searches untouched. Through the real screen and the real ViewModel ([RealSearchScreenRig]); every touch is real.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class SearchBarClearTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(RealSearchScreenRig.touchModeHost()).around(composeRule)

    private val rig = RealSearchScreenRig(composeRule)

    private val regionA = Region(45.326, -122.634, 8)

    private fun clearShown() = composeRule.onAllNodesWithTag(SEARCH_BAR_CLEAR_TAG).fetchSemanticsNodes().isNotEmpty()

    private fun summaryReadsSearchALocation() =
        composeRule.onAllNodesWithText("Search a location", substring = true, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `with nothing searched there is no Clear`() {
        rig.setScreen()
        assertTrue("positive control: the summary reads Search a location", summaryReadsSearchALocation())
        composeRule.onAllNodesWithTag(SEARCH_BAR_CLEAR_TAG).assertCountEquals(0)
    }

    /** Samples across Clear's own bounds, as fractions of its width and height: a finger is not a point. */
    private val touchSamples = listOf(0.5f to 0.5f, 0.15f to 0.25f, 0.85f to 0.25f, 0.15f to 0.75f, 0.85f to 0.75f)

    @Test
    fun `a real touch anywhere on Clear removes the search from the map and the bar and keeps the recent searches`() {
        rig.setScreen()
        touchSamples.forEach { (fx, fy) ->
            rig.searchCoordinates("45.326", "-122.634")
            assertTrue("Clear shows while a search is showing", clearShown())
            assertEquals("positive control: the search is on the map", listOf(RealSearchScreenRig.sightingAt(regionA)), rig.mapSightings)
            assertFalse("positive control: the summary names the search, not Search a location", summaryReadsSearchALocation())
            val recentBefore = rig.viewModel.uiState.value.recentSearches
            assertTrue("positive control: the search was saved to the recent searches", recentBefore.any { it.region == regionA })

            rig.touch(composeRule.onNodeWithTag(SEARCH_BAR_CLEAR_TAG), fx, fy)

            val state = rig.viewModel.uiState.value
            assertEquals("a touch at ($fx, $fy) of Clear left nothing on the map", emptyList<Any>(), rig.mapSightings)
            assertNull("the search's region is gone", state.region)
            assertNull("its ranked list is gone", state.forecast)
            assertTrue("the summary reads Search a location again", summaryReadsSearchALocation())
            assertEquals("the recent searches are untouched", recentBefore, state.recentSearches)
            assertFalse("Clear is gone with the search", clearShown())
            assertFalse("Clear opened no dropdown", rig.dropdownShown())
        }
    }

    @Test
    fun `a sightings fetch still running when Clear is touched does not put the search back`() {
        rig.setScreen()
        val gate = CompletableDeferred<Unit>()
        rig.sightingsGate = gate
        rig.searchCoordinates("45.326", "-122.634")
        assertEquals("positive control: the map's fetch started", 1, rig.sightingsFetches.size)
        assertEquals("positive control: and has not finished", emptyList<Any>(), rig.mapSightings)

        rig.touch(composeRule.onNodeWithTag(SEARCH_BAR_CLEAR_TAG))
        gate.complete(Unit)
        rig.settle()

        assertEquals("the late fetch put nothing on the map", emptyList<Any>(), rig.mapSightings)
        assertNull("the region stays cleared", rig.viewModel.uiState.value.region)
    }
}
