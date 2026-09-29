package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * Dispatch 2026-09-28-245, Part B: A1 item 1, the landscape L after a fullscreen cycle. S22-A
 * (record -242, rerun at `4bd3be31`): after a real tap into fullscreen and a real tap out, the L
 * moves up about 45 px, so its top lies above the search bar's bottom, which the landscape-L ruling
 * ("never above the search bar's bottom", `docs/audits/2026-09-29-landscape-l-completion-report.md`)
 * forbids. Rotation, or a relaunch, restores it.
 *
 * Both entries and both exits go through the real control (a coordinate touch on the Fullscreen row)
 * and through the Back key. Under Robolectric the insets are zero (CLAUDE.md), so this reproduces
 * only what does not depend on them: the short window's centred L top (44 dp) is above the search
 * bar's bottom (its measured height), which is what makes the limit push it down at rest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class LandscapeLFullscreenCycleTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private class TapMapSlot {
        val slot: MapSlot = { _, _, _, _, _, onTap, _, _, modifier ->
            Box(modifier.testTag(LAYOUT_FIXES_MAP_TAG).pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) })
        }
    }

    private val map = TapMapSlot()

    private fun setScreen(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        var rotationSeen: Int? = null
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            LayoutFixesScreen(uiState = LAYOUT_FIXES_FIX_STATE, mapSlot = map.slot, isRecording = true)
        }
        settle()
        assertEquals("the screen must see the pinned rotation", rotation, rotationSeen)
    }

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
    }

    private fun tag(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun cluster(): DpRect = tag(MAP_ICON_CLUSTER_TAG)

    /** A real touch at the centre of the row described by [description]. */
    private fun realTouchOn(description: String) {
        val b = composeRule.onNodeWithContentDescription(description).getUnclippedBoundsInRoot()
        val at = with(composeRule.density) { Offset(((b.left + b.right) / 2).toPx(), ((b.top + b.bottom) / 2).toPx()) }
        composeRule.onRoot().performTouchInput { click(at) }
        settle()
    }

    private fun assertNotAboveSearchBar(what: String) {
        val searchBottom = tag(SEARCH_ENTRY_BAR_TAG).bottom
        assertTrue(
            "$what: the L's top ${cluster().top.value} is not above the search bar's bottom ${searchBottom.value}",
            cluster().top >= searchBottom - 0.5.dp,
        )
    }

    private fun assertCycleRestoresTheL(rotation: Int, exit: () -> Unit) {
        setScreen(rotation)
        assertNotAboveSearchBar("at rest")
        val restTop = cluster().top.value

        realTouchOn("Fullscreen")
        assertTrue("fullscreen is on: the search bar is gone", composeRule.onAllNodesWithTag(SEARCH_ENTRY_BAR_TAG).fetchSemanticsNodes().isEmpty())
        assertTrue("the exit control shows", composeRule.onAllNodesWithContentDescription("Exit fullscreen").fetchSemanticsNodes().isNotEmpty())
        exit()

        assertTrue("fullscreen is off: the search bar is back", composeRule.onAllNodesWithTag(SEARCH_ENTRY_BAR_TAG).fetchSemanticsNodes().isNotEmpty())
        assertNotAboveSearchBar("after a fullscreen cycle")
        assertEquals("the L is where it was before the cycle", restTop, cluster().top.value, 1f)
    }

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `at ROTATION_90 the L after a real tap into and out of fullscreen is not above the search bar's bottom`() =
        assertCycleRestoresTheL(Surface.ROTATION_90) { realTouchOn("Exit fullscreen") }

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `at ROTATION_270 the L after a real tap into and out of fullscreen is not above the search bar's bottom`() =
        assertCycleRestoresTheL(Surface.ROTATION_270) { realTouchOn("Exit fullscreen") }

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `at ROTATION_90 leaving fullscreen with the Back key does the same`() =
        assertCycleRestoresTheL(Surface.ROTATION_90) {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
            settle()
        }
}
