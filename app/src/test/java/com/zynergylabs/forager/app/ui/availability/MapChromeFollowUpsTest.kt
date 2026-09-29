package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.DpRect
import com.zynergylabs.forager.app.ui.map.CENTRE_PIN_CONFIRM_ROW_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The map-chrome device check's follow-ups (dispatch 2026-09-28-104), the ones Robolectric can see.
 *
 * Every test drives the real [AvailabilityScreen] through [MapChromeTestScreen], as [MapChromeOverMapTest]
 * does. What is **not** here, because Robolectric reports zero window insets (CLAUDE.md, "Known pitfalls"):
 * the portrait snackbar above the system navigation bar (item 6), a bottom sheet's navigation-bar band
 * (item 3), and the row's clearance of the system bar (item 5's other half). Those are device items.
 */
private typealias FollowUpRule = AndroidComposeTestRule<androidx.test.ext.junit.rules.ActivityScenarioRule<ComponentActivity>, ComponentActivity>

private fun FollowUpRule.back() {
    runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }
    waitForIdle()
}

private fun DpRect.intersects(other: DpRect): Boolean =
    left < other.right && other.left < right && top < other.bottom && other.top < bottom

internal abstract class MapChromeFollowUpsTests {

    protected val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    protected val map = BubbleMapSlot(emptyList())
    protected val state = MapChromeScreenState()
    protected val roles = MapChromeRoles()

    protected fun setScreen() {
        composeRule.setContent { MapChromeTestScreen(state, map, roles) }
        composeRule.waitForIdle()
    }
}

/** Item 4: Back closes the species suggestions first. Portrait and short landscape both use the compact tree. */
internal abstract class MapChromeSuggestionsBackTests : MapChromeFollowUpsTests() {

    @Test
    fun `Back closes the species suggestions typed on the Maps tab, and only them`() {
        setScreen()
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performTextInput("zzz")
        composeRule.waitForIdle()
        assertEquals("the suggestions opened", 1, composeRule.onAllNodesWithTag(TAXON_SUGGESTIONS_MENU_TAG).fetchSemanticsNodes().size)

        composeRule.back()

        assertEquals("one Back closed the suggestions", 0, composeRule.onAllNodesWithTag(TAXON_SUGGESTIONS_MENU_TAG).fetchSemanticsNodes().size)
        assertFalse("the ViewModel's own dismiss ran, so the list does not reopen", state.ui.taxonSearchHasNoResults)
        assertEquals("the query they were for is untouched", "zzz", state.ui.taxonSearchQuery)
    }

    @Test
    fun `with no suggestions open Back is not taken by them`() {
        setScreen()

        composeRule.back()

        assertEquals(0, composeRule.onAllNodesWithTag(TAXON_SUGGESTIONS_MENU_TAG).fetchSemanticsNodes().size)
        assertEquals("", state.ui.taxonSearchQuery)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
internal class MapChromeSuggestionsBackPortraitTest : MapChromeSuggestionsBackTests()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
internal class MapChromeSuggestionsBackShortLandscapeTest : MapChromeSuggestionsBackTests()

/** Item 5, the half Robolectric can see: in a short landscape window the pin's OK/Cancel row does not lie over the rail. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi")
internal class MapChromePinRowLandscapeTest : MapChromeFollowUpsTests() {

    @Test
    fun `the centre-pin OK and Cancel row clears the navigation rail`() {
        setScreen()
        composeRule.onNodeWithContentDescription("Plan a trip or log a find here").performTouchInput { click(center) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Trip").performTouchInput { click(center) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(CENTRE_PIN_CONFIRM_ROW_TAG).assertIsDisplayed()
        val row = composeRule.onNodeWithTag(CENTRE_PIN_CONFIRM_ROW_TAG).getBoundsInRoot()
        val rail = composeRule.onNodeWithTag(COMPACT_NAVIGATION_RAIL_TAG).getBoundsInRoot()
        assertTrue("the rail has a width to clear ($rail)", rail.right - rail.left > androidx.compose.ui.unit.Dp(1f))
        assertFalse("the row $row does not lie over the rail $rail", row.intersects(rail))
    }
}
