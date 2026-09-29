package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.model.AvailabilityEntry
import com.zynergylabs.forager.app.domain.model.AvailabilityForecast
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.MAP_LEGEND_CHIP_TAG
import java.time.LocalDate
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
 * Items 1 and 2 in landscape (planner message `2026-09-28-109`): when the cluster is on the legend's side
 * the legend sits just inboard of it, bottom-aligned, with the portrait gap (8 dp), collapsed or
 * expanded; otherwise it stays in its corner. Same window, both rotations pinned, the cluster moved by
 * the real drag of its handle, the legend's chip touched by real touches across its bounds.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LayoutFixesLegendLandscapeTest {

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
            MapLayersTestScreen(viewModel, map.slot, store)
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the screen must see the pinned rotation", rotation, rotationSeen)
    }

    private fun tag(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun cluster(): DpRect = tag(MAP_ICON_CLUSTER_TAG)
    private fun legend(): DpRect = tag(MAP_LEGEND_CHIP_TAG)
    private fun legendExpanded(): Boolean =
        composeRule.onNodeWithTag(MAP_LEGEND_CHIP_TAG).fetchSemanticsNode().config[SemanticsActions.OnClick].label == "Hide legend"

    private fun snapClusterAcross(dx: Dp) {
        val handle = tag("map-icon-bar-minimize-handle")
        composeRule.longPressDrag((handle.left + handle.right) / 2, (handle.top + handle.bottom) / 2, dx, 0.dp)
    }

    /** Real touches at fractions of the legend's current bounds; each flips it between collapsed and expanded. */
    private fun touchLegendAcrossItsBounds() {
        var expected = legendExpanded()
        listOf(0.2f to 0.5f, 0.8f to 0.5f, 0.5f to 0.8f, 0.5f to 0.2f).forEach { (fx, fy) ->
            val at = legend()
            composeRule.touchAt(at.left + (at.right - at.left) * fx, at.top + (at.bottom - at.top) * fy)
            composeRule.mainClock.advanceTimeBy(2_000)
            composeRule.waitForIdle()
            expected = !expected
            assertEquals("a real touch at ($fx, $fy) of the legend ${at.describe()} reached it", expected, legendExpanded())
        }
    }

    /**
     * With the cluster on [clusterOnRight], the legend, collapsed and then expanded, is clear of the cluster; when
     * it is on the legend's side (the right) the legend is [8 dp] inboard of it and bottom-aligned with where it
     * sits in its corner. The corner's bottom is read with the cluster on the other side.
     */
    private fun assertLegendAgainstCluster(clusterOnRight: Boolean, cornerBottom: Dp) {
        val cluster = cluster()
        val collapsed = legend()
        assertFalse("collapsed: the legend ${collapsed.describe()} and the cluster ${cluster.describe()} do not intersect", collapsed.overlapsRect(cluster))
        assertEquals("collapsed: the legend is bottom-aligned at the corner's bottom", cornerBottom.value, collapsed.bottom.value, 1f)
        if (clusterOnRight) {
            assertEquals("collapsed: the legend's right edge is 8 dp inboard of the cluster ${cluster.describe()} (legend ${collapsed.describe()})", cluster.left.value - 8f, collapsed.right.value, 1f)
        }
        touchLegendAcrossItsBounds()
        // Four flips leave it as it started, collapsed: expand it with one more touch.
        val at = legend()
        composeRule.touchAt((at.left + at.right) / 2, (at.top + at.bottom) / 2)
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("the legend expanded", legendExpanded())
        val expanded = legend()
        assertFalse("expanded: the legend ${expanded.describe()} and the cluster ${cluster().describe()} do not intersect", expanded.overlapsRect(cluster()))
        assertEquals("expanded: the legend is bottom-aligned at the corner's bottom", cornerBottom.value, expanded.bottom.value, 1f)
        if (clusterOnRight) {
            assertEquals("expanded: the legend's right edge is 8 dp inboard of the cluster ${cluster().describe()} (legend ${expanded.describe()})", cluster().left.value - 8f, expanded.right.value, 1f)
        }
    }

    /** Reads the corner's bottom, with the cluster on the left (the far side from the legend), then the cluster on the right. */
    private fun run(rotation: Int, defaultClusterOnRight: Boolean) {
        setScreen(rotation)
        val towardsLeft = (-500).dp
        val towardsRight = 500.dp
        if (defaultClusterOnRight) snapClusterAcross(towardsLeft)
        val cornerBottom = legend().bottom
        assertLegendAgainstCluster(clusterOnRight = false, cornerBottom = cornerBottom)
        // Collapse it again before moving the cluster across.
        val at = legend()
        composeRule.touchAt((at.left + at.right) / 2, (at.top + at.bottom) / 2)
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertFalse(legendExpanded())
        snapClusterAcross(towardsRight)
        assertLegendAgainstCluster(clusterOnRight = true, cornerBottom = cornerBottom)
    }

    @Test
    fun `T1 at ROTATION_90 the legend is clear of the cluster on either side, inboard of it on the legend's side`() = run(Surface.ROTATION_90, defaultClusterOnRight = false)

    @Test
    fun `T1 at ROTATION_270 the legend is clear of the cluster on either side, inboard of it on the legend's side`() = run(Surface.ROTATION_270, defaultClusterOnRight = true)
}
