package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.zynergylabs.forager.app.ui.theme.Spacing
import org.junit.Assert.assertEquals
import androidx.compose.ui.unit.Dp
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
 * Part 1 layout fixes, item 3 (Part 1's device check, check 8; planner message `2026-09-28-98`): the keyboard
 * coming up shrinks the dropdown's viewport, and the coordinate fields must stay in view above it.
 *
 * Until dispatch 2026-09-28-697 the coordinates were the dropdown's last row: the bar's tap scrolled the
 * dropdown to its end and kept it there as the viewport changed, and T3a/T3b tested that keep-in-view and
 * its stop on a user drag. That dispatch put the coordinates first and removed the scroll (the owner's
 * reorder; the dispatch: "Remove the scroll-to-bottom-on-open"), so these tests now hold the new shape:
 * the fields are in view at the top whatever the viewport, and nothing but the user scrolls the dropdown.
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

    /** How far the Latitude field's top sits below the dropdown's top: the panel's own padding when it is not scrolled. */
    private fun latitudeOffsetFromTop(): Dp {
        val panelTop = composeRule.onNodeWithTag(SEARCH_DROPDOWN_TAG).getUnclippedBoundsInRoot().top
        val latitudeTop = composeRule.onNodeWithTag(SEARCH_DROPDOWN_LATITUDE_TAG).getUnclippedBoundsInRoot().top
        return latitudeTop - panelTop
    }

    @Test
    fun `T3a when the viewport shrinks, the coordinates stay in view at the top`() {
        setDropdown()
        composeRule.onNodeWithText("Latitude").assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_SEARCH_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_SET_ON_MAP_TAG).assertIsDisplayed()

        shrinkTo(360)

        composeRule.onNodeWithText("Latitude").assertIsDisplayed()
        composeRule.onNodeWithText("Longitude").assertIsDisplayed()
        // RECORD -700: the button that searches them sits under them, so it stays in view with them.
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_SEARCH_COORDINATES_TAG).assertIsDisplayed()
    }

    @Test
    fun `T3b the dropdown opens at its top and no viewport change scrolls it`() {
        setDropdown()
        assertEquals("opened at its top", Spacing.lg.value, latitudeOffsetFromTop().value, 0.5f)

        shrinkTo(360)
        assertEquals("still at its top after the viewport shrank", Spacing.lg.value, latitudeOffsetFromTop().value, 0.5f)

        shrinkTo(420)
        shrinkTo(360)
        assertEquals("still at its top after the viewport changed twice more", Spacing.lg.value, latitudeOffsetFromTop().value, 0.5f)
    }
}
