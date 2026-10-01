package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.zynergylabs.forager.app.ui.log.MushroomLogUiState
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-312, item 12, the owner's option (b): the search dropdown, closed by Back, stays closed
 * when the window is **out of touch mode**, which is a hardware keyboard or a D-pad on a device and is this
 * harness's default.
 *
 * Out of touch mode `View.clearFocus` hands focus to the first focusable view, and the scaffold clears focus
 * when the dropdown closes (to hide the keyboard), so the search field got focus straight back and its focus
 * callback reopened the dropdown: Back could never close it. [AvailabilityScreenBubbleAndDropdownBackTest] holds
 * the touch-mode half, where focus is not handed back.
 *
 * Through the real [AvailabilityScreen], a real touch on the field and the real activity dispatcher.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class AvailabilityScreenDropdownCloseOutOfTouchModeTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    /**
     * A map that takes no focus, so the search field is the first focusable on the screen and is what the handed-back
     * focus lands on. With a focusable map stub (`BubbleMapSlot`'s is `clickable`) the focus goes to the map, nothing
     * reopens, and this class's test passes with or without the fix: seen, dispatch 2026-09-28-312's report, Part B.
     */
    private val slot: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier.testTag(MAP_TAG)) }

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    private fun back() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        settle()
    }

    private fun dropdownShown() = composeRule.onAllNodesWithTag(SEARCH_DROPDOWN_TAG).fetchSemanticsNodes().isNotEmpty()

    private fun showMapsTab() {
        val store = OneCellStore()
        val viewModel = mapLayersViewModel(store = store)
        composeRule.setContent { MapLayersTestScreen(viewModel = viewModel, mapSlot = slot, store = store, logUiState = MushroomLogUiState()) }
        composeRule.waitForIdle()
        assertFalse(
            "positive control: the window is out of touch mode, the case this class is about",
            composeRule.activity.window.decorView.isInTouchMode,
        )
    }

    private fun openDropdownByTouch() {
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performTouchInput { click() }
        settle()
        assertTrue("positive control: a touch on the field opened the dropdown", dropdownShown())
    }

    @Test
    fun `out of touch mode, Back closes the dropdown and it is still closed after the screen settles`() {
        showMapsTab()
        openDropdownByTouch()

        back()
        assertFalse("Back closed the dropdown, and the focus handed back to the field did not reopen it", dropdownShown())

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertFalse("the dropdown stayed closed", dropdownShown())
    }

    private companion object {
        const val MAP_TAG = "dropdown-close-map"
    }
}
