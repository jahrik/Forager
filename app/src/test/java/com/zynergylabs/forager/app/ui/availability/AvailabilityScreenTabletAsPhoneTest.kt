package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.model.AvailabilityEntry
import com.zynergylabs.forager.app.domain.model.AvailabilityForecast
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.time.LocalDate
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
 * Tablets as a big phone (dispatch `2026-09-28-245`, as amended by `-246`): a window of any width
 * renders the phone's tree, and a window that is in landscape, tall or not, gets the phone's
 * landscape layout (the rail, the L). The owner's words: "We did the rotation work on the phone so
 * it should carry across to the tablet"; and "the phone layout will be the thing to expand".
 *
 * Pre-registered before the build (`docs/audits/2026-09-30-tablet-as-phone-preregistration.md`): at
 * the base these fail, because at these sizes the screen draws the tablet tree (a permanent drawer,
 * a tab row, no bottom nav, no rail, no L). The phone's own classes are the check that the phone
 * did not change.
 *
 * Sizes: `w824dp-h1318dp` a portrait tablet, `w673dp-h841dp` a 600-839 dp width, `w1318dp-h824dp-land`
 * a landscape tablet. The last is tall (824 dp), so it is not "short": the rail there is what the
 * amendment adds.
 *
 * Insets are zero under Robolectric (CLAUDE.md), so nothing here reads an inset value; the L's
 * position on a real tablet is a device item.
 */
@RunWith(RobolectricTestRunner::class)
class AvailabilityScreenTabletAsPhoneTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    /** A map stand-in taking pointer input over its whole bounds as the real `AndroidView` does; counts long-presses. */
    private class LongPressMapSlot {
        var longPresses by mutableStateOf(0)
        val slot: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
            Box(
                modifier
                    .testTag(LAYOUT_FIXES_MAP_TAG)
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) },
            )
        }
    }

    private val map = LongPressMapSlot()

    private fun setScreen(rotation: Int? = null, uiState: AvailabilityUiState = LAYOUT_FIXES_FIX_STATE, isRecording: Boolean = false) {
        if (rotation != null) Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        var rotationSeen: Int? = null
        composeRule.setContent {
            rotationSeen = LocalView.current.display?.rotation
            LayoutFixesScreen(uiState = uiState, mapSlot = map.slot, isRecording = isRecording)
        }
        settle()
        if (rotation != null) assertEquals("the screen must see the pinned rotation", rotation, rotationSeen)
    }

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
    }

    private fun tag(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun exists(tag: String): Boolean = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    private fun labelBounds(text: String): DpRect = composeRule.onNodeWithText(text).getUnclippedBoundsInRoot()
    private fun cluster(): DpRect = tag(MAP_ICON_CLUSTER_TAG)

    private fun DpRect.centre(): Pair<Dp, Dp> = (left + right) / 2 to (top + bottom) / 2

    private fun realTapOnLabel(text: String) {
        val (x, y) = labelBounds(text).centre()
        composeRule.onRoot().performTouchInput { click(with(composeRule.density) { Offset(x.toPx(), y.toPx()) }) }
        composeRule.waitForIdle()
    }

    /** What the wide tree drew and the phone tree does not: the permanent drawer's Trip Planner and Photo Gallery entries. */
    private fun assertNoPermanentDrawer() {
        composeRule.onNodeWithText("Trip Planner").assertIsNotDisplayed()
        assertTrue(
            "no Photo Gallery entry: the permanent drawer is gone",
            composeRule.onAllNodesWithText("Photo Gallery").fetchSemanticsNodes().isEmpty(),
        )
    }

    // ── The phone tree at tablet sizes ──

    private fun assertPortraitPhoneTree() {
        setScreen()
        composeRule.onNodeWithTag(COMPACT_BOTTOM_NAV_TAG).assertIsDisplayed()
        assertTrue("a portrait window has no rail", !exists(RAIL_TAG))
        composeRule.onNodeWithTag(MAP_ICON_CLUSTER_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LAYOUT_FIXES_MAP_TAG).assertIsDisplayed()
        assertNoPermanentDrawer()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w824dp-h1318dp")
    fun `a portrait tablet renders the phone tree`() = assertPortraitPhoneTree()

    @Test
    @Config(sdk = [36], qualifiers = "w673dp-h841dp")
    fun `a 600 to 839 dp wide window renders the phone tree`() = assertPortraitPhoneTree()

    @Test
    @Config(sdk = [36], qualifiers = "w824dp-h1318dp")
    fun `on a portrait tablet a real touch on a bottom nav tab switches the tab`() {
        setScreen(uiState = LAYOUT_FIXES_FIX_STATE.copy(forecast = TABLET_FORECAST, selectedMonth = LocalDate.now().monthValue))
        composeRule.onNodeWithText("Maps").assertIsSelected()

        realTapOnLabel("List")

        composeRule.onNodeWithText("List").assertIsSelected()
        assertTrue("the map is gone with the Maps tab", !exists(LAYOUT_FIXES_MAP_TAG))
    }

    // ── The phone's landscape layout at a landscape tablet ──

    private fun assertLandscapePhoneTree() {
        setScreen(Surface.ROTATION_90)
        composeRule.onNodeWithTag(RAIL_TAG).assertIsDisplayed()
        assertTrue("a landscape window has the rail, not the bottom nav", !exists(COMPACT_BOTTOM_NAV_TAG))
        composeRule.onNodeWithTag(MAP_ICON_CLUSTER_TAG).assertIsDisplayed()
        assertNoPermanentDrawer()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `a landscape tablet renders the phone tree with the rail and no bottom nav`() = assertLandscapePhoneTree()

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `at a landscape tablet the L is drawn`() {
        setScreen(Surface.ROTATION_90, isRecording = true)
        val bar = DpRect(
            composeRule.onNodeWithContentDescription("Fullscreen").getUnclippedBoundsInRoot().left,
            composeRule.onNodeWithContentDescription("Fullscreen").getUnclippedBoundsInRoot().top,
            composeRule.onNodeWithContentDescription("Plan a trip or log a find here").getUnclippedBoundsInRoot().right,
            composeRule.onNodeWithContentDescription("Plan a trip or log a find here").getUnclippedBoundsInRoot().bottom,
        )
        assertEquals("the L's bar is 48 wide", 48f, (bar.right - bar.left).value, 0.5f)
        assertEquals("the L's bar is 240 tall: five 48 dp rows", 240f, (bar.bottom - bar.top).value, 0.5f)
        assertEquals("the L is 296 tall with its pill", 296f, (cluster().bottom - cluster().top).value, 0.5f)
        composeRule.onNodeWithTag("control-pill").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `at a landscape tablet a real touch on a rail item switches the tab`() {
        setScreen(Surface.ROTATION_90, LAYOUT_FIXES_FIX_STATE.copy(forecast = TABLET_FORECAST, selectedMonth = LocalDate.now().monthValue))
        composeRule.onNodeWithText("Maps").assertIsSelected()

        realTapOnLabel("List")

        composeRule.onNodeWithText("List").assertIsSelected()
        assertTrue("the map is gone with the Maps tab", !exists(LAYOUT_FIXES_MAP_TAG))
        composeRule.onNodeWithTag(RAIL_TAG).assertIsDisplayed()
    }

    private fun assertLongPressesReachMapAlong(x: Dp) {
        val mapArea = tag(LAYOUT_FIXES_MAP_TAG)
        val searchBottom = tag(SEARCH_ENTRY_BAR_TAG).bottom
        val controls = buildList {
            add(cluster())
            composeRule.onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().forEach { node ->
                with(composeRule.density) {
                    val b = node.boundsInRoot
                    add(DpRect(b.left.toDp(), b.top.toDp(), b.right.toDp(), b.bottom.toDp()))
                }
            }
        }
        val top = maxOf(mapArea.top, searchBottom) + 4.dp
        val bottom = mapArea.bottom - 4.dp
        var sampled = 0
        for (i in 0 until 8) {
            val y = top + (bottom - top) * (i / 7f)
            if (controls.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) continue
            val before = map.longPresses
            val at = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
            composeRule.onRoot().performTouchInput { longClick(at) }
            composeRule.waitForIdle()
            assertEquals("a long-press at ($x, $y) must reach the map", before + 1, map.longPresses)
            sampled++
        }
        assertTrue("at least 4 points were sampled, not $sampled", sampled >= 4)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `at a landscape tablet long-presses along the rail's inner edge and mid-map reach the map`() {
        setScreen(Surface.ROTATION_90)
        val rail = tag(RAIL_TAG)
        assertLongPressesReachMapAlong(rail.left - 4.dp)
        assertLongPressesReachMapAlong(tag(LAYOUT_FIXES_MAP_TAG).let { (it.left + it.right) / 2 })
    }

    @Test
    @Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
    fun `control - a phone in landscape still shows the rail and the L, so the sample can see them`() {
        setScreen(Surface.ROTATION_90)
        composeRule.onNodeWithTag(RAIL_TAG).assertIsDisplayed()
        assertEquals("the L is 296 tall", 296f, (cluster().bottom - cluster().top).value, 0.5f)
    }

    private fun assertLTopNeverAboveSearchBar(rotation: Int) {
        setScreen(rotation, isRecording = true)
        val searchBottom = tag(SEARCH_ENTRY_BAR_TAG).bottom
        assertTrue(
            "at rest the L's top ${cluster().top.value} is not above the search bar's bottom ${searchBottom.value}",
            cluster().top >= searchBottom - 0.5.dp,
        )
        val handle = tag("map-icon-bar-minimize-handle")
        val (hx, hy) = handle.centre()
        composeRule.longPressDrag(hx, hy, 0.dp, (-800).dp)
        assertEquals(
            "dragged to the top the L rests at the search bar's bottom ${searchBottom.value}",
            searchBottom.value,
            cluster().top.value,
            1f,
        )
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `at a landscape tablet at ROTATION_90 the L's top is never above the search bar's bottom`() =
        assertLTopNeverAboveSearchBar(Surface.ROTATION_90)

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [36], qualifiers = "w1318dp-h824dp-land")
    fun `at a landscape tablet at ROTATION_270 the L's top is never above the search bar's bottom`() =
        assertLTopNeverAboveSearchBar(Surface.ROTATION_270)
}

/** The rail's own container tag, a literal so this file states the contract rather than borrowing it. */
private const val RAIL_TAG = "compact-navigation-rail"

private val TABLET_FORECAST = AvailabilityForecast(
    region = LAYOUT_FIXES_REGION,
    month = 8,
    filter = TaxonFilter.FUNGI,
    entries = listOf(
        AvailabilityEntry(
            species = SpeciesObservationCount(
                taxonId = 48473L,
                scientificName = "Ganoderma applanatum",
                commonName = "artist's bracket",
                rank = "species",
                observationCount = 14,
                photoUrl = null,
                wikipediaUrl = null,
            ),
            relativeLikelihood = 1.0f,
        ),
    ),
)
