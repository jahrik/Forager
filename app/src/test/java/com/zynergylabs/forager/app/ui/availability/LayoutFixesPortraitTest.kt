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
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_HIDE_ALL_TAG
import com.zynergylabs.forager.app.ui.map.MAP_LEGEND_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.MapCameraSnapshot
import com.zynergylabs.forager.app.ui.map.journalEntriesChipLabel
import com.zynergylabs.forager.app.ui.theme.Spacing
import kotlin.math.abs
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
 * Part 1 layout fixes, item 2 in portrait (dispatch `2026-09-28-78`, planner message `-98`): with the
 * legend expanded the icon cluster rises above it, under the owner's Q4 ruling ("the cluster moves up
 * when the legend expands and back down when it collapses"). The window is `h740dp`, short enough that
 * the centred cluster overlaps the expanded legend (the S22's own overlap depends on its status and
 * navigation bars, which Robolectric reports as zero), and tall enough that the cluster can rise clear
 * of it under the dropdown's top. Rotation is pinned at 0.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h740dp-xxhdpi")
class LayoutFixesLegendPortraitTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val map = LayersRecordingMapSlot(LAYOUT_FIXES_DATES)

    @Test
    fun `T2 with the legend expanded the centred cluster rises clear of it, and collapsed it returns`() {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        var rotationSeen: Int? = null
        val store = FixedForecastStore(BOTH_FORECAST_GROUPS)
        val viewModel = mapLayersViewModel(store = store)
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            MapLayersTestScreen(viewModel, map.slot, store)
        }
        composeRule.waitForIdle()
        assertEquals("the screen must see the pinned rotation", Surface.ROTATION_0, rotationSeen)

        fun cluster(): DpRect = composeRule.onNodeWithTag(MAP_ICON_CLUSTER_TAG).getUnclippedBoundsInRoot()
        fun legend(): DpRect = composeRule.onNodeWithTag(MAP_LEGEND_CHIP_TAG).getUnclippedBoundsInRoot()

        val start = cluster()
        assertFalse("collapsed: the cluster ${start.describe()} is clear of the legend ${legend().describe()}", start.overlapsRect(legend()))

        composeRule.touchAt((legend().left + legend().right) / 2, (legend().top + legend().bottom) / 2)
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the legend expanded", "Hide legend", composeRule.onNodeWithTag(MAP_LEGEND_CHIP_TAG).fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsActions.OnClick].label)
        val raised = cluster()
        assertFalse("expanded: the cluster ${raised.describe()} is clear of the legend ${legend().describe()}", raised.overlapsRect(legend()))

        composeRule.touchAt((legend().left + legend().right) / 2, legend().top + 12.dp)
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("collapsed again: the cluster is back where it started (${cluster().describe()} against ${start.describe()})", abs((cluster().top - start.top).value) <= 1f)
    }
}

/**
 * Part 1 layout fixes, items 4 and 8, at the S22's portrait size (`w384dp-h823dp`), rotation pinned at 0.
 *
 * Item 4 (planner message `-98`): the camera the user left on the Maps tab survives a tab round trip.
 * The map stub stands in for `SightingsMap`, which writes the camera it settles on into the memory it is
 * handed; whether MapLibre then restores it is a device item.
 *
 * Item 8 (J8's device check, check 4): where J8's chip's touch area overlaps the coordinate readout's,
 * every point reaches exactly one control. The map stub takes pointer input over its whole bounds, as the
 * real map's `AndroidView` does, so a touch neither control claims lands on the map and is counted.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class LayoutFixesPortraitTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val map = LayoutFixesMapSlot()

    private fun setScreen(showEntry: Boolean) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        var rotationSeen: Int? = null
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            LayoutFixesScreen(
                uiState = LAYOUT_FIXES_FIX_STATE,
                mapSlot = map.slot,
                cartographyUiState = if (showEntry) LAYOUT_FIXES_SHOWN_ENTRY_STATE else com.zynergylabs.forager.app.ui.log.CartographyUiState(),
            )
        }
        composeRule.waitForIdle()
        assertEquals("the screen must see the pinned rotation", Surface.ROTATION_0, rotationSeen)
    }

    private fun touchNav(label: String) {
        val item = composeRule.onNodeWithText(label).getUnclippedBoundsInRoot()
        composeRule.touchAt((item.left + item.right) / 2, (item.top + item.bottom) / 2)
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    // ── Item 4 ──

    @Test
    fun `T4 the camera the user left on the Maps tab survives a tab round trip`() {
        setScreen(showEntry = false)
        val left = MapCameraSnapshot(
            target = LatLng(45.3512, -122.6011),
            zoom = 15.5,
            bearing = 30.0,
            tilt = 10.0,
            following = false,
            appliedTarget = LAYOUT_FIXES_REGION to null,
        )
        map.renderMode?.cameraMemory?.saved = left

        touchNav("List")
        assertTrue("the map left composition with the tab", composeRule.onAllNodesWithTag(LAYOUT_FIXES_MAP_TAG).fetchSemanticsNodes().isEmpty())
        touchNav("Maps")

        assertEquals("the camera the user left is what the map is handed back", left, map.renderMode?.cameraMemory?.saved)
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
        setScreen(showEntry = true)
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
