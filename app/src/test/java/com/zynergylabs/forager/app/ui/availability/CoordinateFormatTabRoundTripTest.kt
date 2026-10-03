package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDisplay

/**
 * Dispatch 2026-09-28-422 (plan task T18): the coordinate format the user chose on the Maps tab
 * survives leaving the tab and coming back (CLAUDE.md, UX defaults: user-set state that resets on a
 * tab change is a bug). For the session only; across a restart it is not asked for. Driven through
 * the real screen with real touches: the strip's coordinates, then the bottom nav's "List" and
 * "Maps". At the S22's portrait size, as `LayoutFixesPortraitTest`'s camera round trip is.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class CoordinateFormatTabRoundTripTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val map = LayoutFixesMapSlot()

    private val mgrs = coordinatesStripText(LAYOUT_FIXES_FIX_LOCATION, showDecimalDegrees = false)
    private val decimal = coordinatesStripText(LAYOUT_FIXES_FIX_LOCATION, showDecimalDegrees = true)

    private fun touchText(text: String) {
        val b = composeRule.onNodeWithText(text).getUnclippedBoundsInRoot()
        composeRule.touchAt((b.left + b.right) / 2, (b.top + b.bottom) / 2)
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    @Test
    fun `decimal degrees chosen on the Maps tab are still shown after the List tab and back`() {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        composeRule.setContent { LayoutFixesScreen(uiState = LAYOUT_FIXES_FIX_STATE, mapSlot = map.slot) }
        composeRule.waitForIdle()
        assertEquals("precondition: the strip starts on MGRS", 1, composeRule.onAllNodesWithText(mgrs).fetchSemanticsNodes().size)

        touchText(mgrs)
        assertEquals("the touch chose decimal degrees", 1, composeRule.onAllNodesWithText(decimal).fetchSemanticsNodes().size)

        touchText("List")
        assertEquals("the strip left composition with the tab", 0, composeRule.onAllNodesWithText(decimal).fetchSemanticsNodes().size)
        touchText("Maps")

        assertEquals("decimal degrees after the round trip", 1, composeRule.onAllNodesWithText(decimal).fetchSemanticsNodes().size)
        assertEquals("not back on MGRS", 0, composeRule.onAllNodesWithText(mgrs).fetchSemanticsNodes().size)
    }
}
