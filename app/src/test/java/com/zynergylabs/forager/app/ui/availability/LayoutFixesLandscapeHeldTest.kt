package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.MAP_LEGEND_CHIP_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Part 1 layout fixes, the re-measurements planner message `2026-09-28-99` asks for after the landscape
 * reshape: item 7 (J8's chip against the cluster) and items 1 and 2 in landscape (the legend against
 * the cluster, collapsed and expanded), at `ROTATION_90` and `ROTATION_270` in the short landscape
 * window. The report pre-registers which are expected to still collide; those are stop evidence.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LayoutFixesLandscapeHeldTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val map = LayersRecordingMapSlot(LAYOUT_FIXES_DATES)

    private fun setScreen(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        var rotationSeen: Int? = null
        val store = FixedForecastStore(BOTH_FORECAST_GROUPS)
        val viewModel = mapLayersViewModel(store = store)
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            MapLayersTestScreen(viewModel, map.slot, store, cartographyUiState = LAYOUT_FIXES_SHOWN_ENTRY_STATE)
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the screen must see the pinned rotation", rotation, rotationSeen)
    }

    private fun tag(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

    // ── Item 7 ──

    private fun assertChipClear(rotation: Int) {
        setScreen(rotation)
        val chip = tag(JOURNAL_ENTRIES_CHIP_TAG)
        val cluster = tag(MAP_ICON_CLUSTER_TAG)
        assertFalse("J8's chip ${chip.describe()} and the cluster ${cluster.describe()} do not intersect", chip.overlapsRect(cluster))
    }

    @Test fun `T7 at ROTATION_90 J8's chip and the cluster do not intersect`() = assertChipClear(Surface.ROTATION_90)

    @Test fun `T7 at ROTATION_270 J8's chip and the cluster do not intersect`() = assertChipClear(Surface.ROTATION_270)

    // ── Items 1 and 2 in landscape ──

    private fun assertLegendClear(rotation: Int) {
        setScreen(rotation)
        val collapsed = tag(MAP_LEGEND_CHIP_TAG)
        assertFalse("collapsed: the legend ${collapsed.describe()} and the cluster ${tag(MAP_ICON_CLUSTER_TAG).describe()} do not intersect", collapsed.overlapsRect(tag(MAP_ICON_CLUSTER_TAG)))

        // Near the legend's bottom, where it is composed over anything under it.
        composeRule.touchAt((collapsed.left + collapsed.right) / 2, collapsed.bottom - 6.dp)
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the legend expanded", "Hide legend", composeRule.onNodeWithTag(MAP_LEGEND_CHIP_TAG).fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        val expanded = tag(MAP_LEGEND_CHIP_TAG)
        assertFalse("expanded: the legend ${expanded.describe()} and the cluster ${tag(MAP_ICON_CLUSTER_TAG).describe()} do not intersect", expanded.overlapsRect(tag(MAP_ICON_CLUSTER_TAG)))
    }

    @Test fun `T1 at ROTATION_90 the legend, collapsed and expanded, does not intersect the cluster`() = assertLegendClear(Surface.ROTATION_90)

    @Test fun `T1 at ROTATION_270 the legend, collapsed and expanded, does not intersect the cluster`() = assertLegendClear(Surface.ROTATION_270)
}
