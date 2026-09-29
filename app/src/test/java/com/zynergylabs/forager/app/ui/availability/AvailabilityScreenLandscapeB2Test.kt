package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.content.res.Configuration
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.AvailabilityEntry
import com.zynergylabs.forager.app.domain.model.AvailabilityForecast
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.SpeciesObservationCount
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.ui.adaptive.punchHoleEdgeFor
import com.zynergylabs.forager.app.ui.log.ScreenEdge
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDisplay

/**
 * Landscape build B2 (intent 2026-09-27-35, S1-S10): the short-landscape Map tab's layout. The
 * window is the S22 Ultra's landscape window, `w823dp h384dp`. At `ROTATION_90` the charger port,
 * and so the rail, is on the right and the punch-hole on the left; at `ROTATION_270` the reverse
 * (`PortEdge.kt`).
 *
 * Every test drives the real [AvailabilityScreen] and asserts measured bounds. Every
 * rotation-specific test first proves the screen read the pinned rotation, as
 * [AvailabilityScreenShortLandscapeTest] does, so the two rotations cannot both run at
 * `ROTATION_0`. Insets are zero under Robolectric (CLAUDE.md, "Known pitfalls"), so the cut-out
 * padding on the punch-hole side is zero here: the punch-hole-side controls edge is the window's
 * edge. Where the controls sit against a real cut-out is a device item.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class AvailabilityScreenLandscapeB2Test {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val map = B2RecordingMapSlot()

    private var rotationSeenByScreen: Int? = null

    /** Bumped to hand the screen a new [Configuration] object, so it re-reads the display's rotation (a turn from 90 to 270). */
    private var configurationTick by mutableStateOf(0)

    private fun setScreen(rotation: Int, uiState: AvailabilityUiState = B2_SEARCHED_STATE, isReturning: Boolean = false) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            val base = LocalConfiguration.current
            // LocalConfiguration compares by equality, so a copy equal to base does not reach the
            // screen: it did not re-read the rotation (seen: the rail stayed on the right after a
            // turn to 270). navigationHidden is flipped relative to base on odd ticks (base is
            // NAVIGATIONHIDDEN_YES under Robolectric, measured), so each tick's configuration
            // differs from the last; nothing on this screen reads navigationHidden.
            val flipped = if (base.navigationHidden == Configuration.NAVIGATIONHIDDEN_YES) Configuration.NAVIGATIONHIDDEN_NO else Configuration.NAVIGATIONHIDDEN_YES
            val configuration = if (configurationTick == 0) base else Configuration(base).apply {
                navigationHidden = if (configurationTick % 2 == 1) flipped else base.navigationHidden
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                rotationSeenByScreen = LocalView.current.display?.rotation
                B2Screen(uiState = uiState, mapSlot = map.slot, isReturning = isReturning)
            }
        }
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeenByScreen)
    }

    /**
     * Turns the phone from one landscape rotation to the other, in the same composition. A fresh
     * [Configuration] object is provided so the screen's own `currentDisplayRotation()` re-reads
     * the display. That the *screen* (not just this test's lambda) saw the turn is proved by the
     * rail, which B1 already puts on the port edge: at 90 on the right, at 270 on the left.
     */
    private fun turnTo(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        configurationTick++
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the turn to $rotation", rotation, rotationSeenByScreen)
        val railSide = rail()
        if (rotation == Surface.ROTATION_270) {
            assertEquals("after the turn to 270 the screen put the rail on the left", root().left.value, railSide.left.value, 0.5f)
        } else {
            assertEquals("after the turn to $rotation the screen put the rail on the right", root().right.value, railSide.right.value, 0.5f)
        }
    }

    // ── S1: the punch-hole edge comes from punchHoleEdgeFor ──

    /**
     * The punch-hole-side controls (the search bar, and the cluster's default) sit on the edge
     * [punchHoleEdgeFor] names for this rotation, and the search bar does not also reach the
     * opposite edge — a full-width bar touches both, which is how this fails before S1/S2.
     */
    private fun assertPunchHoleSide(rotation: Int) {
        setScreen(rotation)
        val punchHole = punchHoleEdgeFor(windowIsLandscape = true, displayRotation = rotation)
        val bar = searchBar()
        val c = cluster()
        val r = root()
        val m = mapBounds()
        val farEdgeClear = 8.dp
        when (punchHole) {
            ScreenEdge.Left -> {
                assertEquals("the search bar is on the punch-hole edge, the left", r.left.value, bar.left.value, 0.5f)
                assertTrue("the search bar $bar stays clear of the far half of the map $m", bar.right <= (m.left + m.right) / 2 - farEdgeClear + 0.5.dp)
                assertTrue("the cluster $c defaults to the punch-hole half, the left", c.centreXv() < r.centreXv())
            }
            ScreenEdge.Right -> {
                assertEquals("the search bar is on the punch-hole edge, the right", r.right.value, bar.right.value, 0.5f)
                assertTrue("the search bar $bar stays clear of the far half of the map $m", bar.left >= (m.left + m.right) / 2 + farEdgeClear - 0.5.dp)
                assertTrue("the cluster $c defaults to the punch-hole half, the right", c.centreXv() > r.centreXv())
            }
            else -> throw AssertionError("a landscape window's punch-hole edge is left or right, not $punchHole")
        }
    }

    @Test
    fun `S1 at ROTATION_90 the punch-hole-side controls are on punchHoleEdgeFor's edge`() = assertPunchHoleSide(Surface.ROTATION_90)

    @Test
    fun `S1 at ROTATION_270 the punch-hole-side controls are on punchHoleEdgeFor's edge`() = assertPunchHoleSide(Surface.ROTATION_270)

    private fun tagBounds(tag: String): DpRect = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
    private fun root(): DpRect = composeRule.onRoot().getUnclippedBoundsInRoot()
    private fun mapBounds(): DpRect = tagBounds(B2_MAP_SLOT_TAG)
    private fun rail(): DpRect = tagBounds(COMPACT_NAVIGATION_RAIL_TAG)
    private fun searchBar(): DpRect = tagBounds(SEARCH_ENTRY_BAR_TAG)
    private fun cluster(): DpRect = tagBounds(MAP_ICON_CLUSTER_TAG)
    private fun railExists(): Boolean = composeRule.onAllNodesWithTag(COMPACT_NAVIGATION_RAIL_TAG).fetchSemanticsNodes().isNotEmpty()

    /** The search bar's expected width: min(384dp, distance from the punch-hole-side controls edge to the map's centre - 8dp). */
    private fun expectedSearchWidth(punchHoleControlsEdge: Dp): Float {
        val m = mapBounds()
        val centre = (m.left + m.right) / 2
        val distance = if (punchHoleControlsEdge <= centre) centre - punchHoleControlsEdge else punchHoleControlsEdge - centre
        return minOf(384f, (distance - 8.dp).value)
    }

    // ── S2: search bar ──

    @Test
    fun `S2 at ROTATION_90 the search bar sits at the top on the punch-hole side, left, capped short of the centre line`() {
        setScreen(Surface.ROTATION_90)
        val bar = searchBar()
        val m = mapBounds()
        val centre = (m.left + m.right) / 2

        assertEquals("the bar starts at the punch-hole-side (left) edge", root().left.value, bar.left.value, 0.5f)
        assertEquals("the bar is at the top of the map", m.top.value, bar.top.value, 0.5f)
        assertEquals("the bar's width is min(384, centre distance - 8)", expectedSearchWidth(root().left), bar.width.value, 0.5f)
        assertTrue("the bar ends before the centre line: right ${bar.right} vs centre $centre", bar.right <= centre - 8.dp + 0.5.dp)
    }

    @Test
    fun `S2 at ROTATION_270 the search bar sits at the top on the punch-hole side, right, capped short of the centre line`() {
        setScreen(Surface.ROTATION_270)
        val bar = searchBar()
        val m = mapBounds()
        val centre = (m.left + m.right) / 2

        assertEquals("the bar ends at the punch-hole-side (right) edge", root().right.value, bar.right.value, 0.5f)
        assertEquals("the bar is at the top of the map", m.top.value, bar.top.value, 0.5f)
        assertEquals("the bar's width is min(384, centre distance - 8)", expectedSearchWidth(root().right), bar.width.value, 0.5f)
        assertTrue("the bar starts after the centre line: left ${bar.left} vs centre $centre", bar.left >= centre + 8.dp - 0.5.dp)
    }

    @Test
    fun `S2 at ROTATION_90 the open dropdown takes the bar's width and side, and its scrim leaves the rail clear`() {
        setScreen(Surface.ROTATION_90)
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        val bar = searchBar()
        val dropdown = tagBounds(SEARCH_DROPDOWN_TAG)
        val scrim = tagBounds(SEARCH_DROPDOWN_SCRIM_TAG)
        val rail = rail()

        assertEquals("dropdown left = bar left", bar.left.value, dropdown.left.value, 0.5f)
        assertEquals("dropdown right = bar right", bar.right.value, dropdown.right.value, 0.5f)
        assertTrue("the scrim $scrim must not cover the rail $rail", !scrim.intersects(rail))
    }

    @Test
    fun `S2 at ROTATION_270 the open dropdown takes the bar's width and side, and its scrim leaves the rail clear`() {
        setScreen(Surface.ROTATION_270)
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        val bar = searchBar()
        val dropdown = tagBounds(SEARCH_DROPDOWN_TAG)
        val scrim = tagBounds(SEARCH_DROPDOWN_SCRIM_TAG)
        val rail = rail()

        assertEquals("dropdown left = bar left", bar.left.value, dropdown.left.value, 0.5f)
        assertEquals("dropdown right = bar right", bar.right.value, dropdown.right.value, 0.5f)
        assertTrue("the scrim $scrim must not cover the rail $rail", !scrim.intersects(rail))
    }

    // ── S3: filter chip ──

    private fun showTaxonFilterChip() {
        // "View on Map" from a List-tab row, the chip's real entry point.
        composeRule.onNodeWithText("List").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("artist's bracket").performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(B2_TAXON_CHIP_TAG).assertIsDisplayed()
    }

    @Test
    fun `S3 at ROTATION_90 the filter chip sits directly under the search bar, at its end away from the cluster, within its width`() {
        setScreen(Surface.ROTATION_90, B2_SEARCHED_STATE.copy(forecast = B2_FORECAST, selectedMonth = LocalDate.now().monthValue))
        showTaxonFilterChip()
        val bar = searchBar()
        val chip = tagBounds(B2_TAXON_CHIP_TAG)

        assertTrue("the chip $chip is below the bar $bar", chip.top >= bar.bottom - 0.5.dp)
        assertTrue("the chip $chip is directly under the bar $bar, not below the strip's band too", chip.top <= bar.bottom + 16.dp)
        // Part 1 layout fixes, item 7 (planner message 2026-09-29-04): at 90 the cluster's default is the left, so the
        // chip row aligns to the bar's right end, away from it. It began at the bar's start before this ruling.
        assertEquals("the chip ends at the bar's end, away from the cluster on the left", bar.right.value, chip.right.value, 0.5f)
        assertTrue("the chip $chip lies within the bar's width $bar", chip.left >= bar.left - 0.5.dp && chip.right <= bar.right + 0.5.dp)
    }

    // ── S4: strip and HUD in the top corner on the rail side ──

    @Test
    fun `S4 at ROTATION_90 the compass strip is content-width in the top corner on the rail side, not below the search bar`() {
        setScreen(Surface.ROTATION_90, B2_FIX_STATE)
        val strip = tagBounds(B2_STRIP_TAG)
        val rail = rail()
        val bar = searchBar()
        val m = mapBounds()

        assertEquals("the strip ends at the rail's inner edge", rail.left.value, strip.right.value, 0.5f)
        assertEquals("the strip is at the top of the map, not below the search bar", m.top.value, strip.top.value, 0.5f)
        assertTrue("the strip $strip is content-width, well short of the space beside the rail", strip.width < (rail.left - m.left) / 2)
        assertTrue("the strip $strip does not reach the search bar $bar", strip.left >= bar.right)
    }

    @Test
    fun `S4 at ROTATION_270 the compass strip is content-width in the top corner on the rail side, not below the search bar`() {
        setScreen(Surface.ROTATION_270, B2_FIX_STATE)
        val strip = tagBounds(B2_STRIP_TAG)
        val rail = rail()
        val bar = searchBar()
        val m = mapBounds()

        assertEquals("the strip starts at the rail's inner edge", rail.right.value, strip.left.value, 0.5f)
        assertEquals("the strip is at the top of the map, not below the search bar", m.top.value, strip.top.value, 0.5f)
        assertTrue("the strip $strip is content-width, well short of the space beside the rail", strip.width < (m.right - rail.right) / 2)
        assertTrue("the strip $strip does not reach the search bar $bar", strip.right <= bar.left)
    }

    @Test
    fun `S4 at ROTATION_90 the navigation HUD is in the top corner on the rail side, at most 360dp wide`() {
        setScreen(Surface.ROTATION_90, B2_FIX_STATE, isReturning = true)
        val hud = tagBounds(NAVIGATION_HUD_TAG)
        val rail = rail()
        val m = mapBounds()

        assertTrue("the strip stays hidden while navigating", composeRule.onAllNodesWithTag(B2_STRIP_TAG).fetchSemanticsNodes().isEmpty())
        assertEquals("the HUD ends at the rail's inner edge", rail.left.value, hud.right.value, 0.5f)
        assertEquals("the HUD is at the top of the map", m.top.value, hud.top.value, 0.5f)
        assertTrue("the HUD $hud is at most 360dp wide", hud.width <= 360.dp + 0.5.dp)
    }

    @Test
    fun `S4 at ROTATION_270 the navigation HUD is in the top corner on the rail side, at most 360dp wide`() {
        setScreen(Surface.ROTATION_270, B2_FIX_STATE, isReturning = true)
        val hud = tagBounds(NAVIGATION_HUD_TAG)
        val rail = rail()
        val m = mapBounds()

        assertEquals("the HUD starts at the rail's inner edge", rail.right.value, hud.left.value, 0.5f)
        assertEquals("the HUD is at the top of the map", m.top.value, hud.top.value, 0.5f)
        assertTrue("the HUD $hud is at most 360dp wide", hud.width <= 360.dp + 0.5.dp)
    }

    // ── S5: tabular figures (landscape half; the portrait half is AvailabilityScreenPortraitStripTnumTest) ──

    @Test
    fun `S5 in landscape the strip's heading, elevation and coordinate text use tabular figures`() {
        setScreen(Surface.ROTATION_90, B2_FIX_STATE)
        assertStripTextUsesTabularFigures(composeRule)
    }

    // ── S6: cluster side, per orientation ──

    @Test
    fun `S6 at ROTATION_90 the cluster defaults to the punch-hole side, left`() {
        setScreen(Surface.ROTATION_90)
        val c = cluster()
        val r = root()
        assertTrue("the cluster $c is on the left half of $r", (c.left + c.right) / 2 < (r.left + r.right) / 2)
    }

    @Test
    fun `S6 at ROTATION_270 the cluster defaults to the punch-hole side, right`() {
        setScreen(Surface.ROTATION_270)
        val c = cluster()
        val r = root()
        assertTrue("the cluster $c is on the right half of $r", (c.left + c.right) / 2 > (r.left + r.right) / 2)
    }

    @Test
    fun `S6 turning from ROTATION_90 to ROTATION_270 keeps the cluster on the same device edge, the punch-hole edge`() {
        setScreen(Surface.ROTATION_90)
        assertTrue("at 90 the cluster starts on the left, the punch-hole side", cluster().centreXv() < root().centreXv())

        turnTo(Surface.ROTATION_270)

        assertTrue("at 270 the punch-hole edge is the right; the cluster follows it", cluster().centreXv() > root().centreXv())
    }

    @Test
    fun `S6 a cluster dragged to the port side stays on the port edge across a turn from ROTATION_90 to ROTATION_270`() {
        setScreen(Surface.ROTATION_90)
        dragMinimizeHandle(dxDp = 400.dp)
        assertTrue("at 90 the cluster was dragged to the right, the port side", cluster().centreXv() > root().centreXv())

        turnTo(Surface.ROTATION_270)

        assertTrue("at 270 the port edge is the left; the cluster follows it", cluster().centreXv() < root().centreXv())
    }

    private fun dragMinimizeHandle(dxDp: Dp) {
        val b = tagBounds(B2_MINIMIZE_HANDLE_TAG)
        val start = with(composeRule.density) { Offset(((b.left + b.right) / 2).toPx(), ((b.top + b.bottom) / 2).toPx()) }
        val delta = with(composeRule.density) { Offset(dxDp.toPx(), 0f) }
        composeRule.onRoot().performTouchInput {
            down(start)
            advanceEventTime(600)
            moveTo(start + delta)
            advanceEventTime(50)
            up()
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
    }

    // ── S7: the rail slides toward the port edge on entering fullscreen ──

    /**
     * Steps the clock one frame at a time after [trigger], up to [B2_SLIDE_SCAN_MS], and returns
     * the rail's bounds at every frame the rail is in the tree, with the map's bounds checked equal
     * to [mapAtRest] at every frame (the map's size never changes, S7). Scanning frames, rather than
     * sampling one guessed instant, is what lets "mid-slide" be observed whatever the spring's pace.
     */
    private fun railFramesAfter(mapAtRest: DpRect, trigger: () -> Unit): List<DpRect> {
        composeRule.mainClock.autoAdvance = false
        trigger()
        val frames = mutableListOf<DpRect>()
        var elapsed = 0L
        while (elapsed < B2_SLIDE_SCAN_MS) {
            composeRule.mainClock.advanceTimeByFrame()
            elapsed += 16
            if (railExists()) frames += rail()
            assertSameBounds("the map at ${elapsed}ms", mapAtRest, mapBounds())
        }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        return frames
    }

    @Test
    fun `S7 entering fullscreen at ROTATION_90 slides the rail toward the port edge, and the map's size never changes`() {
        setScreen(Surface.ROTATION_90)
        val railBefore = rail()
        val mapBefore = mapBounds()

        val frames = railFramesAfter(mapBefore) { composeRule.onNodeWithContentDescription("Fullscreen").performClick() }

        val midSlide = frames.filter { it.left > railBefore.left + 0.5.dp && it.left < root().right }
        assertTrue("the rail must be seen part-way toward the port edge (right) on some frame; frames seen: $frames", midSlide.isNotEmpty())
        assertTrue("after the slide the rail is gone", !railExists())
        assertSameBounds("after the slide", mapBefore, mapBounds())
    }

    @Test
    fun `S7 entering fullscreen at ROTATION_270 slides the rail toward the port edge, left`() {
        setScreen(Surface.ROTATION_270)
        val railBefore = rail()
        val mapBefore = mapBounds()

        val frames = railFramesAfter(mapBefore) { composeRule.onNodeWithContentDescription("Fullscreen").performClick() }

        val midSlide = frames.filter { it.right < railBefore.right - 0.5.dp && it.right > root().left }
        assertTrue("the rail must be seen part-way toward the port edge (left) on some frame; frames seen: $frames", midSlide.isNotEmpty())
        assertTrue("after the slide the rail is gone", !railExists())
    }

    @Test
    fun `S7 leaving fullscreen at ROTATION_90 slides the rail back in from the port edge`() {
        setScreen(Surface.ROTATION_90)
        val railBefore = rail()
        val mapBefore = mapBounds()
        composeRule.onNodeWithContentDescription("Fullscreen").performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("the rail is gone in fullscreen", !railExists())

        val frames = railFramesAfter(mapBefore) { composeRule.onNodeWithContentDescription("Exit fullscreen").performClick() }

        val midSlide = frames.filter { it.left > railBefore.left + 0.5.dp }
        assertTrue("the rail must be seen right of its resting place, sliding in, on some frame; frames seen: $frames", midSlide.isNotEmpty())
        assertSameBounds("the rail back at rest", railBefore, rail())
    }

    // ── S8: the Surface rule — real long-presses beside the search bar and the cluster reach the map ──

    @Test
    fun `S8 at ROTATION_90 long-presses beside the search bar, along its height, reach the map`() {
        setScreen(Surface.ROTATION_90)
        val bar = searchBar()
        assertLongPressesReachMap(xs = listOf(bar.right + 4.dp, bar.right + 24.dp), top = bar.top + 2.dp, bottom = bar.bottom - 2.dp)
    }

    @Test
    fun `S8 at ROTATION_90 long-presses beside the cluster, along its inner edge, reach the map`() {
        setScreen(Surface.ROTATION_90)
        val c = cluster()
        val inner = if (c.centreXv() < root().centreXv()) c.right + 12.dp else c.left - 12.dp
        assertLongPressesReachMap(xs = listOf(inner), top = c.top + 4.dp, bottom = c.bottom - 4.dp)
    }

    /**
     * Real long-presses at [B2_SAMPLE_COUNT] heights between [top] and [bottom] at each x in [xs]. A
     * point inside any control is skipped, and the number sampled is asserted, so an empty sample
     * cannot pass.
     */
    private fun assertLongPressesReachMap(xs: List<Dp>, top: Dp, bottom: Dp) {
        val controls = controlBounds()
        var sampled = 0
        for (x in xs) {
            for (i in 0 until B2_SAMPLE_COUNT) {
                val y = top + (bottom - top) * (i.toFloat() / (B2_SAMPLE_COUNT - 1))
                if (controls.any { x >= it.left && x <= it.right && y >= it.top && y <= it.bottom }) continue
                val before = map.longPresses
                val p = with(composeRule.density) { Offset(x.toPx(), y.toPx()) }
                composeRule.onRoot().performTouchInput { longClick(p) }
                composeRule.waitForIdle()
                assertEquals("a long-press at ($x, $y) must reach the map", before + 1, map.longPresses)
                sampled++
            }
        }
        assertTrue("at least $B2_MIN_SAMPLED points were sampled, not $sampled", sampled >= B2_MIN_SAMPLED)
    }

    /**
     * What a sample point must not land in to count as "the map": every clickable node, plus the
     * search bar's and the cluster's own Surfaces (a Surface intercepts across its whole bounds, so
     * a point on either is theirs, not the map's). A point *beside* them is what S8 samples.
     */
    private fun controlBounds(): List<DpRect> {
        val clickable = composeRule.onAllNodes(androidx.compose.ui.test.hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes()
            .map { with(composeRule.density) { DpRect(it.boundsInRoot.left.toDp(), it.boundsInRoot.top.toDp(), it.boundsInRoot.right.toDp(), it.boundsInRoot.bottom.toDp()) } }
        return clickable + searchBar() + cluster()
    }

    // ── S10: the centre stays clear ──

    @Test
    fun `S10 at ROTATION_90 no persistent chrome intersects the window's central third`() {
        setScreen(Surface.ROTATION_90, B2_FIX_STATE)
        assertCentreClear(listOf(SEARCH_ENTRY_BAR_TAG, B2_STRIP_TAG, MAP_ICON_CLUSTER_TAG, COMPACT_NAVIGATION_RAIL_TAG))
    }

    @Test
    fun `S10 at ROTATION_270 no persistent chrome intersects the window's central third`() {
        setScreen(Surface.ROTATION_270, B2_FIX_STATE)
        assertCentreClear(listOf(SEARCH_ENTRY_BAR_TAG, B2_STRIP_TAG, MAP_ICON_CLUSTER_TAG, COMPACT_NAVIGATION_RAIL_TAG))
    }

    @Test
    fun `S10 at ROTATION_90 while navigating, the HUD and the rest leave the central third clear`() {
        setScreen(Surface.ROTATION_90, B2_FIX_STATE, isReturning = true)
        assertCentreClear(listOf(SEARCH_ENTRY_BAR_TAG, NAVIGATION_HUD_TAG, MAP_ICON_CLUSTER_TAG, COMPACT_NAVIGATION_RAIL_TAG))
    }

    @Test
    fun `S10 at ROTATION_270 while navigating, the HUD and the rest leave the central third clear`() {
        setScreen(Surface.ROTATION_270, B2_FIX_STATE, isReturning = true)
        assertCentreClear(listOf(SEARCH_ENTRY_BAR_TAG, NAVIGATION_HUD_TAG, MAP_ICON_CLUSTER_TAG, COMPACT_NAVIGATION_RAIL_TAG))
    }

    // Planner message 2026-09-29-05 (the owner's "Option B"): a harness correction, not a weaker assertion. The default
    // graphics mode measures text at near-zero width, which put the chip row at a different place from the phone's;
    // the central-third assertion below is unchanged.
    @Test
    @org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
    fun `S10 at ROTATION_90 with the filter chip showing, the central third stays clear`() {
        setScreen(Surface.ROTATION_90, B2_SEARCHED_STATE.copy(forecast = B2_FORECAST, selectedMonth = LocalDate.now().monthValue))
        showTaxonFilterChip()
        assertCentreClear(listOf(SEARCH_ENTRY_BAR_TAG, B2_TAXON_CHIP_TAG, MAP_ICON_CLUSTER_TAG, COMPACT_NAVIGATION_RAIL_TAG))
    }

    /** Each tag must be present (so an absent node cannot pass) and must not intersect x 1/3-2/3, y 1/3-2/3 of the window. */
    private fun assertCentreClear(tags: List<String>) {
        val r = root()
        val centre = DpRect(
            left = r.left + r.width / 3, top = r.top + r.height / 3,
            right = r.left + r.width * 2 / 3, bottom = r.top + r.height * 2 / 3,
        )
        val offenders = tags.mapNotNull { tag ->
            val nodes = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes()
            assertTrue("$tag must be on screen for this check to mean anything", nodes.isNotEmpty())
            val b = tagBounds(tag)
            if (b.intersects(centre)) "$tag $b" else null
        }
        assertTrue("nothing persistent may intersect the central third $centre; these do: $offenders", offenders.isEmpty())
    }

    private fun assertSameBounds(label: String, expected: DpRect, actual: DpRect) {
        assertEquals("$label: left", expected.left.value, actual.left.value, 0.01f)
        assertEquals("$label: top", expected.top.value, actual.top.value, 0.01f)
        assertEquals("$label: right", expected.right.value, actual.right.value, 0.01f)
        assertEquals("$label: bottom", expected.bottom.value, actual.bottom.value, 0.01f)
    }
}

/**
 * S6's per-orientation memory: portrait keeps today's cluster state exactly, and landscape has its
 * own. The window is turned by handing the same composition a landscape [Configuration], as
 * [AvailabilityScreenTurnToShortLandscapeTest] does; the host window stays portrait-shaped, so this
 * asserts on which half of the window the cluster is in, not on landscape geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-port")
class AvailabilityScreenLandscapeB2TurnTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val map = B2RecordingMapSlot()

    private fun cluster(): DpRect = composeRule.onNodeWithTag(MAP_ICON_CLUSTER_TAG).getUnclippedBoundsInRoot()
    private fun root(): DpRect = composeRule.onRoot().getUnclippedBoundsInRoot()

    @Test
    fun `S6 the portrait cluster side and the landscape cluster side are remembered separately`() {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        var landscape by mutableStateOf(false)
        composeRule.setContent {
            val base = LocalConfiguration.current
            val configuration = if (!landscape) base else Configuration(base).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
                screenWidthDp = 823
                screenHeightDp = 384
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                B2Screen(uiState = B2_SEARCHED_STATE, mapSlot = map.slot, isReturning = false)
            }
        }
        composeRule.waitForIdle()
        assertTrue("portrait: the cluster starts on the right", cluster().centreXv() > root().centreXv())

        // Portrait: drag it to the left.
        val b = composeRule.onNodeWithTag(B2_MINIMIZE_HANDLE_TAG).getUnclippedBoundsInRoot()
        val start = with(composeRule.density) { Offset(((b.left + b.right) / 2).toPx(), ((b.top + b.bottom) / 2).toPx()) }
        val delta = with(composeRule.density) { Offset((-300).dp.toPx(), 0f) }
        composeRule.onRoot().performTouchInput {
            down(start)
            advanceEventTime(600)
            moveTo(start + delta)
            advanceEventTime(50)
            up()
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("portrait: dragged to the left", cluster().centreXv() < root().centreXv())

        // Turn to landscape at ROTATION_270: the port (and rail) is on the left, so the punch-hole
        // side, the cluster's landscape default, is the right — landscape's own default, not the
        // portrait drag to the left carried over. 270 is used because there the two differ.
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_270)
        landscape = true
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("landscape at 270: the cluster defaults to the punch-hole side, the right", cluster().centreXv() > root().centreXv())

        // Back to portrait: the portrait drag is still remembered.
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        landscape = false
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertTrue("portrait again: still on the left, where the user put it", cluster().centreXv() < root().centreXv())
    }
}

/** S5's portrait half: tabular figures apply to the strip in both orientations. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class AvailabilityScreenPortraitStripTnumTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val map = B2RecordingMapSlot()

    @Test
    fun `S5 in portrait the strip's heading, elevation and coordinate text use tabular figures`() {
        composeRule.setContent { B2Screen(uiState = B2_FIX_STATE, mapSlot = map.slot, isReturning = false) }
        composeRule.waitForIdle()
        assertStripTextUsesTabularFigures(composeRule)
    }
}

// ── shared fixtures ──

/**
 * The strip's heading (by its tag), elevation and coordinate text (by their exact strings), each
 * read through the text's own laid-out style ([SemanticsActions.GetTextLayoutResult]), not a proxy.
 */
private fun assertStripTextUsesTabularFigures(rule: SemanticsNodeInteractionsProvider) {
    val nodes = mapOf(
        "heading" to rule.onNodeWithTag(COMPASS_STRIP_HEADING_TAG, useUnmergedTree = true),
        "elevation" to rule.onNodeWithText(B2_ELEVATION_TEXT, useUnmergedTree = true),
        "coordinates" to rule.onNodeWithText(coordinatesStripText(B2_FIX_LOCATION, false), useUnmergedTree = true),
    )
    nodes.forEach { (name, node) ->
        val results = mutableListOf<TextLayoutResult>()
        val action = node.fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)
        assertTrue("the strip's $name text exposes its layout", action?.action?.invoke(results) == true)
        assertEquals("the strip's $name text uses tabular figures", "tnum", results.single().layoutInput.style.fontFeatureSettings)
    }
}

private const val B2_MAP_SLOT_TAG = "b2-map-slot"
private const val B2_STRIP_TAG = "compass-elevation-strip"
private const val B2_TAXON_CHIP_TAG = "map-taxon-filter-chip"
private const val B2_MINIMIZE_HANDLE_TAG = "map-icon-bar-minimize-handle"
private const val B2_SAMPLE_COUNT = 8
private const val B2_SLIDE_SCAN_MS = 1_500L
private const val B2_MIN_SAMPLED = 4

private fun DpRect.centreXv(): Float = ((left + right) / 2).value

private fun DpRect.isInside(outer: DpRect): Boolean =
    left >= outer.left && top >= outer.top && right <= outer.right && bottom <= outer.bottom

private fun DpRect.intersects(other: DpRect): Boolean =
    left < other.right && other.left < right && top < other.bottom && other.top < bottom

/** Stands in for the real map; counts real long-presses on the map's own bounds. */
private class B2RecordingMapSlot {
    var longPresses by mutableStateOf(0)

    val slot: MapSlot = { _, _, _, _, _, _, _, _, modifier ->
        Box(
            modifier
                .testTag(B2_MAP_SLOT_TAG)
                .pointerInput(Unit) { detectTapGestures(onLongPress = { longPresses++ }) },
        )
    }
}

@Composable
private fun B2Screen(uiState: AvailabilityUiState, mapSlot: MapSlot, isReturning: Boolean) {
    AvailabilityScreen(
        uiState = uiState,
        isRecording = isReturning,
        isReturning = isReturning,
        onUseCurrentLocation = {},
        onManualLatChanged = {},
        onManualLngChanged = {},
        onSearchManualCoordinates = {},
        onRadiusChanged = {},
        onMonthSelected = {},
        onMapTabSelected = {},
        onSeasonalTabSelected = {},
        onTaxonSearchQueryChanged = {},
        onTaxonSearchResultSelected = {},
        onDismissTaxonSuggestions = {},
        onReopenTaxonSuggestions = {},
        onPlaceTripPin = { _, _, _ -> },
        onDeletePlannedTrip = {},
        onRecentSearchSelected = {},
        onOfflineMapLatChanged = {},
        onOfflineMapLngChanged = {},
        onOfflineMapRadiusChanged = {},
        onOfflineMapNameChanged = {},
        onOfflineMapsOpened = {},
        onDownloadOfflineMaps = {},
        onDeleteOfflineRegion = {},
        onNightModeMapsChanged = {},
        onThemeModeChanged = {},
        mapSlot = mapSlot,
    )
}

private val B2_REGION = Region(lat = 45.326, lng = -122.634, radiusKm = 15)

private val B2_FIX_LOCATION = LatLng(45.3301, -122.6402)

private const val B2_ELEVATION_TEXT = "123 m"

private fun b2Sighting(index: Int) = Sighting(
    observationId = index.toLong(),
    taxonId = 48473L,
    scientificName = "Ganoderma applanatum",
    commonName = "artist's bracket",
    lat = B2_REGION.lat + index * 0.001,
    lng = B2_REGION.lng + index * 0.001,
    observedOn = LocalDate.of(2025, 8, 14),
    photoUrl = null,
)

private val B2_SEARCHED_STATE = AvailabilityUiState(
    region = B2_REGION,
    sightings = List(12) { b2Sighting(it) },
)

/** A live fix, so the strip shows heading, elevation and coordinates rather than its no-fix line. */
private val B2_FIX_STATE = B2_SEARCHED_STATE.copy(
    liveFix = LocationFix.Update(
        lat = B2_FIX_LOCATION.lat,
        lng = B2_FIX_LOCATION.lng,
        altitude = 123.0,
        accuracyMeters = 5f,
        timestampEpochMillis = System.currentTimeMillis(),
    ),
)

private val B2_FORECAST = AvailabilityForecast(
    region = B2_REGION,
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
