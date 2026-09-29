package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Part 1 layout fixes, item 3 (Part 1's device check, check 8; planner message `2026-09-28-98`): after the
 * search bar's tap opens the manual coordinates and the dropdown scrolls to its end, the keyboard coming
 * up shrinks the dropdown's viewport after that scroll. The fields must end up in view above it, not
 * scrolled off the bottom.
 *
 * Robolectric reports no keyboard, so the keyboard's own inset cannot be seen here: the viewport is shrunk
 * by the test, as the keyboard shrinks it on the phone. Whether the dropdown's cap follows the real
 * keyboard is a device item. A component test, not the real screen, for that reason: the screen's height
 * cannot change under Robolectric without a keyboard.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class SearchDropdownKeyboardTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private var viewport by mutableStateOf(800.dp)
    private var expandRequested by mutableStateOf(true)

    private fun setDropdown() {
        composeRule.setContent {
            Box(Modifier.height(viewport)) {
                SearchDropdown(
                    uiState = AvailabilityUiState(),
                    distanceUnit = DistanceUnit.KILOMETERS,
                    onUseCurrentLocation = {},
                    onRecentSearchSelected = {},
                    currentTime = CurrentTimeProvider { 0L },
                    onManualLatChanged = {},
                    onManualLngChanged = {},
                    onSearchManualCoordinates = {},
                    onRadiusChanged = {},
                    onMonthSelected = {},
                    onSetOnMap = {},
                    expandManualCoordinatesRequested = expandRequested,
                    onManualCoordinatesExpandConsumed = { expandRequested = false },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    private fun shrinkTo(heightDp: Int) {
        viewport = heightDp.dp
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    @Test
    fun `T3a when the viewport shrinks after the scroll to the end, the coordinates stay in view`() {
        setDropdown()
        composeRule.onNodeWithText("Search this location").assertIsDisplayed()
        composeRule.onNodeWithText("Set on map").assertIsDisplayed()

        shrinkTo(360)

        composeRule.onNodeWithText("Latitude").assertIsDisplayed()
        composeRule.onNodeWithText("Longitude").assertIsDisplayed()
        composeRule.onNodeWithText("Search this location").assertIsDisplayed()
    }

    @Test
    fun `T3b guard once the user drags the dropdown back up, a later viewport change leaves it where they put it`() {
        setDropdown()
        shrinkTo(360)
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_TAG).performTouchInput { swipeDown(startY = top + 40f, endY = bottom - 40f, durationMillis = 400) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_TAG).performTouchInput { swipeDown(startY = top + 40f, endY = bottom - 40f, durationMillis = 400) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Set on map").assertIsDisplayed()

        shrinkTo(420)
        shrinkTo(360)

        composeRule.onNodeWithText("Set on map").assertIsDisplayed()
        composeRule.onNodeWithText("Search this location").assertIsNotDisplayed()
    }
}
