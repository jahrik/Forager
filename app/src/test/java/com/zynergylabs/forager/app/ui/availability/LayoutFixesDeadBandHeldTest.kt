package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_HIDE_ALL_TAG
import com.zynergylabs.forager.app.ui.map.journalEntriesChipLabel
import com.zynergylabs.forager.app.ui.theme.Spacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Part 1 layout fixes, item 8 (J8's device check, check 4), HELD: where J8's chip's touch area overlaps
 * the coordinate readout's, every point reaches exactly one control. The map stub takes pointer input
 * over its whole bounds, as the real map's `AndroidView` does, so a touch neither control claims lands on
 * the map and is counted. At the S22's portrait size, rotation pinned at 0.
 *
 * The fix these tests were written for (the chip's click over its whole 48 dp box, `d959fb9`) broke two
 * of J8's own tests, which pin that the pill's margin belongs to the map; item 8 is stopped for a ruling
 * and these tests are kept on `layout-fixes-wip` only. See the Part 1 layout-fix report.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class LayoutFixesDeadBandHeldTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val map = LayoutFixesMapSlot()

    private fun setScreen() {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        var rotationSeen: Int? = null
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            LayoutFixesScreen(uiState = LAYOUT_FIXES_FIX_STATE, mapSlot = map.slot, cartographyUiState = LAYOUT_FIXES_SHOWN_ENTRY_STATE)
        }
        composeRule.waitForIdle()
        assertEquals("the screen must see the pinned rotation", Surface.ROTATION_0, rotationSeen)
    }

    // ── Item 8 ──

    private fun decimalShown(): Boolean =
        composeRule.onAllNodesWithText(coordinatesStripText(LAYOUT_FIXES_FIX_LOCATION, true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun listOpen(): Boolean = composeRule.onAllNodesWithTag(JOURNAL_ENTRIES_HIDE_ALL_TAG).fetchSemanticsNodes().isNotEmpty()

    /**
     * One real touch at ([fx], [fy]) of the band, as fractions of its width and height, and exactly one
     * control reacts: the chip's list opens, or the readout switches to its decimal form, and the map
     * does not take it.
     */
    private fun assertBandTouchReachesOneControl(fx: Float, fy: Float) {
        setScreen()
        val chipBounds = composeRule.onNodeWithTag(JOURNAL_ENTRIES_CHIP_TAG).getUnclippedBoundsInRoot()
        // The pill as drawn, from the chip's own text and the pill's padding around it (Spacing.md
        // across, Spacing.sm down), a reference the fix to the chip's touch area does not move. At
        // base it is the chip's tagged bounds; after the fix the tagged bounds are the whole 48 dp box.
        val chipText = composeRule.onNodeWithText(journalEntriesChipLabel(1), useUnmergedTree = true).getUnclippedBoundsInRoot()
        val chip = DpRect(chipText.left - Spacing.md, chipText.top - Spacing.sm, chipText.right + Spacing.md, chipText.bottom + Spacing.sm)
        assertTrue(
            "the pill ${chip.describe()} lies inside the chip's own bounds ${chipBounds.describe()}",
            chip.left >= chipBounds.left - 0.5.dp && chip.top >= chipBounds.top - 0.5.dp && chip.right <= chipBounds.right + 0.5.dp && chip.bottom <= chipBounds.bottom + 0.5.dp,
        )
        val readoutText = composeRule.onNodeWithText(coordinatesStripText(LAYOUT_FIXES_FIX_LOCATION, false), useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        // Each control's touch area: its own bounds, extended to Compose's 48 dp minimum touch target
        // about its centre where it is shorter.
        fun touchArea(r: DpRect): DpRect {
            val centreY = (r.top + r.bottom) / 2
            return DpRect(r.left, minOf(r.top, centreY - 24.dp), r.right, maxOf(r.bottom, centreY + 24.dp))
        }
        val chipTouch = touchArea(chip)
        val readoutTouch = touchArea(readoutText)
        // The device's dead band: where the two touch areas overlap, outside both controls' own bounds
        // (below the readout's text and above the chip's pill).
        val band = DpRect(
            maxOf(chip.left, readoutText.left),
            maxOf(chipTouch.top, readoutText.bottom),
            minOf(chip.right, readoutText.right),
            minOf(readoutTouch.bottom, chip.top),
        )
        assertTrue(
            "the band must exist: chip ${chip.describe()} (touch ${chipTouch.describe()}), readout ${readoutText.describe()} " +
                "(touch ${readoutTouch.describe()}), band ${band.describe()}",
            band.right - band.left >= 2.dp && band.bottom - band.top >= 2.dp,
        )
        assertFalse("before the touch the list is closed", listOpen())
        assertFalse("before the touch the readout is in its MGRS form", decimalShown())

        val x = band.left + (band.right - band.left) * fx
        val y = when {
            band.bottom - band.top < 3.dp -> (band.top + band.bottom) / 2
            fy <= 0f -> band.top + 1.dp
            fy >= 1f -> band.bottom - 1.dp
            else -> band.top + (band.bottom - band.top) * fy
        }
        val tapsBefore = map.taps
        composeRule.touchAt(x, y)

        val chipTook = listOpen()
        val readoutTook = decimalShown()
        val mapTook = map.taps - tapsBefore
        assertTrue(
            "the touch at (${x.value}, ${y.value}) in the band ${band.describe()} reached exactly one control " +
                "(chip's list open: $chipTook, readout switched: $readoutTook, map taps +$mapTook)",
            (chipTook != readoutTook) && mapTook == 0,
        )
    }

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 1 of 6, left and top, reaches exactly one control`() = assertBandTouchReachesOneControl(1f / 6, 0f)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 2 of 6, middle and top, reaches exactly one control`() = assertBandTouchReachesOneControl(0.5f, 0f)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 3 of 6, right and top, reaches exactly one control`() = assertBandTouchReachesOneControl(5f / 6, 0f)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 4 of 6, left and bottom, reaches exactly one control`() = assertBandTouchReachesOneControl(1f / 6, 1f)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 5 of 6, middle and bottom, reaches exactly one control`() = assertBandTouchReachesOneControl(0.5f, 1f)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE) fun `T8 band touch 6 of 6, right and bottom, reaches exactly one control`() = assertBandTouchReachesOneControl(5f / 6, 1f)
}
