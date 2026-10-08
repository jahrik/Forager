package com.zynergylabs.forager.app.ui.availability

import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.domain.model.TaxonSearchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDisplay

/**
 * RECORD -750 (dispatch 2026-09-28-750), items 1, 2 and 4, through the real [AvailabilityScreen] and the real
 * [AvailabilityViewModel] ([RealSearchScreenRig]), every touch real. Each test starts in portrait and turns the phone to the
 * S22's landscape windows (780 x 360 and 823 x 384 dp) at ROTATION_90 (the bar on the left) or ROTATION_270 (on the right),
 * as the owner did.
 *
 * Item 1, the owner: "The drop down search panel has a gap between the top of the panel and the bottom of the bar. It should
 * sit flush against the bar to look continuous." Item 2: "The drop down search panel does not scroll to the bottom like it
 * does it portrait view"; "If the keyboard is floating keep it to the one side. The bar still should scroll up to reveal the
 * search button at the bottom". Item 4: the bar never drops out of line with the strip, whatever its text.
 *
 * Robolectric has no keyboard, so item 2 injects the keyboard's insets into the screen's own view, as the platform delivers
 * them: docked (a bottom inset, visible) and floating (visible, no inset; a floating keyboard reports none). Whether a real
 * keyboard reports those values, and where a floating one sits, is a device item.
 */
abstract class LandscapeSearchFixesTests(private val portrait: String, private val landscape: String) {

    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(RealSearchScreenRig.touchModeHost()).around(composeRule)

    private val rig = RealSearchScreenRig(composeRule)

    private fun bounds(tag: String): DpRect = composeRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    private fun window(): DpRect = composeRule.onAllNodes(isRoot()).onFirst().getUnclippedBoundsInRoot()
    private fun DpRect.d() = "[%.2f, %.2f][%.2f, %.2f]".format(left.value, top.value, right.value, bottom.value)

    /** The portrait screen, then a turn to [rotation] in the landscape window. */
    private fun setScreenThenTurn(rotation: Int, beforeTurn: () -> Unit = {}) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        composeRule.activityRule.scenario.onActivity { RuntimeEnvironment.setQualifiers(portrait) }
        rig.setScreen()
        assertTrue("positive control: portrait first (window ${window().d()})", window().height > window().width)
        beforeTurn()
        turn(rotation)
    }

    private fun turn(rotation: Int) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.activityRule.scenario.onActivity { RuntimeEnvironment.setQualifiers(landscape) }
        rig.settle()
        val w = window()
        assertTrue("positive control: landscape after the turn (window ${w.d()})", w.width > w.height)
        assertTrue("positive control: the landscape strip is up", shown(STRIP_TAG))
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        if (rotation == Surface.ROTATION_90) {
            assertEquals("positive control: at ROTATION_90 the bar is on the left", w.left.value, bar.left.value, 0.5f)
        } else {
            assertEquals("positive control: at ROTATION_270 the bar is on the right", w.right.value, bar.right.value, 0.5f)
        }
    }

    private fun shown(tag: String) = composeRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun closeDropdown() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        rig.settle()
        check(!rig.dropdownShown()) { "Back did not close the dropdown" }
    }

    // ── Item 1: the panel flush against the bar ──

    private fun assertFlush(rotation: Int) {
        setScreenThenTurn(rotation)
        rig.tapBar()
        assertTrue("positive control: the bar's tap opened the dropdown", rig.dropdownShown())
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val divider = bounds(SEARCH_ENTRY_BAR_DIVIDER_TAG)
        val panel = bounds(SEARCH_DROPDOWN_TAG)
        println("MEASURED -750 item 1 $landscape rotation $rotation: bar ${bar.d()}, divider ${divider.d()}, panel ${panel.d()}, gap ${(panel.top - divider.bottom).value}")
        assertEquals("positive control: the divider is the bar's visible bottom edge", bar.bottom.value, divider.bottom.value, 0.5f)
        assertEquals("the panel's top is the divider's bottom, no gap", divider.bottom.value, panel.top.value, 0.5f)
        assertEquals("the panel keeps the bar's left edge", bar.left.value, panel.left.value, 0.5f)
        assertEquals("the panel keeps the bar's right edge", bar.right.value, panel.right.value, 0.5f)
    }

    @Test fun `item 1 at ROTATION_90 the open panel sits flush against the bar`() = assertFlush(Surface.ROTATION_90)

    @Test fun `item 1 at ROTATION_270 the open panel sits flush against the bar`() = assertFlush(Surface.ROTATION_270)

    // ── Item 2: the keyboard keeps the bottom row in view ──

    /** The keyboard's insets as the platform delivers them, into the screen's own Compose view. */
    private fun keyboard(bottom: Dp, visible: Boolean) {
        composeRule.runOnUiThread {
            val view = composeView(composeRule.activity.window.decorView) ?: error("no AndroidComposeView")
            val px = with(composeRule.density) { bottom.roundToPx() }
            val insets = WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, px))
                .setVisible(WindowInsetsCompat.Type.ime(), visible)
                .build()
            ViewCompat.dispatchApplyWindowInsets(view, insets)
        }
        rig.settle()
    }

    private fun composeView(view: View): View? {
        if (view.javaClass.name.endsWith("AndroidComposeView")) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) composeView(view.getChildAt(i))?.let { return it }
        return null
    }

    private fun openPanelBottomRowHidden(rotation: Int): DpRect {
        setScreenThenTurn(rotation)
        rig.tapBar()
        assertTrue("positive control: the bar's tap opened the dropdown", rig.dropdownShown())
        val panel = bounds(SEARCH_DROPDOWN_TAG)
        val search = bounds(SEARCH_DROPDOWN_SEARCH_TAG)
        assertTrue("positive control: before the keyboard, Search ${search.d()} is below the panel's bottom ${panel.d()}", search.bottom > panel.bottom)
        return panel
    }

    private fun assertDocked(rotation: Int) {
        val before = openPanelBottomRowHidden(rotation)
        val keyboardHeight = 180.dp
        keyboard(keyboardHeight, visible = true)
        val keyboardTop = window().bottom - keyboardHeight
        val panel = bounds(SEARCH_DROPDOWN_TAG)
        val search = bounds(SEARCH_DROPDOWN_SEARCH_TAG)
        val setOnMap = bounds(SEARCH_DROPDOWN_SET_ON_MAP_TAG)
        println("MEASURED -750 item 2 docked $landscape rotation $rotation: keyboard top ${keyboardTop.value}, panel before ${before.d()} after ${panel.d()}, Search ${search.d()}, Set on map ${setOnMap.d()}")
        assertTrue("the panel ends at or above the keyboard's top (${panel.bottom} vs $keyboardTop)", panel.bottom <= keyboardTop + 0.5.dp)
        for ((name, b) in listOf("Search" to search, "Set on map" to setOnMap)) {
            assertTrue("$name ${b.d()} ends above the keyboard's top $keyboardTop", b.bottom <= keyboardTop)
            assertTrue("$name ${b.d()} sits just above the keyboard's top $keyboardTop (within 24 dp)", b.bottom >= keyboardTop - 24.dp)
        }
        assertEquals("the panel did not move sideways (left)", before.left.value, panel.left.value, 0.5f)
        assertEquals("the panel did not move sideways (right)", before.right.value, panel.right.value, 0.5f)
        // And Search takes a real touch there: it runs the current-location search.
        val requests = rig.locationRequests
        rig.touch(composeRule.onNodeWithTag(SEARCH_DROPDOWN_SEARCH_TAG))
        assertEquals("a real touch on Search above the keyboard ran its search", requests + 1, rig.locationRequests)
    }

    @Test fun `item 2 at ROTATION_90 a docked keyboard lifts Search and Set on map just above it`() = assertDocked(Surface.ROTATION_90)

    @Test fun `item 2 at ROTATION_270 a docked keyboard lifts Search and Set on map just above it`() = assertDocked(Surface.ROTATION_270)

    private fun assertFloating(rotation: Int) {
        val before = openPanelBottomRowHidden(rotation)
        keyboard(0.dp, visible = true)
        val panel = bounds(SEARCH_DROPDOWN_TAG)
        val search = bounds(SEARCH_DROPDOWN_SEARCH_TAG)
        println("MEASURED -750 item 2 floating $landscape rotation $rotation: panel before ${before.d()} after ${panel.d()}, Search ${search.d()}")
        assertTrue("Search ${search.d()} is inside the panel ${panel.d()}", search.bottom <= panel.bottom)
        assertTrue("the panel scrolled to its end: Search ${search.d()} within 24 dp of its bottom ${panel.bottom}", search.bottom >= panel.bottom - 24.dp)
        assertEquals("the panel stays on its side (left)", before.left.value, panel.left.value, 0.5f)
        assertEquals("the panel stays on its side (right)", before.right.value, panel.right.value, 0.5f)
        assertEquals("a floating keyboard does not resize the panel", (before.bottom - before.top).value, (panel.bottom - panel.top).value, 0.5f)
    }

    @Test fun `item 2 at ROTATION_90 a floating keyboard scrolls the panel to its end, the panel staying on its side`() = assertFloating(Surface.ROTATION_90)

    @Test fun `item 2 at ROTATION_270 a floating keyboard scrolls the panel to its end, the panel staying on its side`() = assertFloating(Surface.ROTATION_270)

    /** A user drag stops it, as in portrait (#202). */
    @Test
    fun `item 2 after a user drag a keyboard no longer moves the panel`() {
        openPanelBottomRowHidden(Surface.ROTATION_90)
        composeRule.onNodeWithTag(SEARCH_DROPDOWN_TAG).performTouchInput { swipeDown() }
        rig.settle()
        keyboard(0.dp, visible = true)
        val panel = bounds(SEARCH_DROPDOWN_TAG)
        val search = bounds(SEARCH_DROPDOWN_SEARCH_TAG)
        assertTrue("the user's scroll stands: Search ${search.d()} is still below the panel ${panel.d()}", search.bottom > panel.bottom)
    }

    // ── Item 4: the bar stays in line with the strip, whatever its text ──

    private val longSpecies = TaxonSearchResult(
        990_001L,
        "Hygrophoropsis aurantiaca var. pallidissima",
        "pale false chanterelle of the coastal dune pinewoods",
        "variety",
        "Fungi",
        null,
    )

    /** Picks the long species through the bar, as a user would, and closes the dropdown. */
    private fun pickLongSpecies() {
        rig.taxonResults = listOf(longSpecies)
        rig.tapBar()
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG, useUnmergedTree = true).performTextInput("pale")
        rig.settle()
        composeRule.onNodeWithText(longSpecies.commonName!!).performTouchInput { click(center) }
        rig.settle()
        assertEquals("positive control: the long species is selected", longSpecies.toFilter(), rig.viewModel.uiState.value.taxonFilter)
        closeDropdown()
    }

    private fun assertInLine(rotation: Int, withSearch: Boolean) {
        setScreenThenTurn(rotation) {
            if (withSearch) rig.searchCoordinates("45.326", "-122.634")
            pickLongSpecies()
        }
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        println("MEASURED -750 item 4 $landscape rotation $rotation search=$withSearch: bar ${bar.d()}, strip ${strip.d()}")
        assertEquals("the bar's top is the strip's top", strip.top.value, bar.top.value, 0.5f)
        assertEquals("the bar is the strip's height", (strip.bottom - strip.top).value, (bar.bottom - bar.top).value, 0.5f)
        if (rotation == Surface.ROTATION_90) {
            assertEquals("the bar ends where the strip begins", strip.left.value, bar.right.value, 0.5f)
        } else {
            assertEquals("the bar begins where the strip ends", strip.right.value, bar.left.value, 0.5f)
        }
        val (text, layout) = summaryLayout()
        println("MEASURED -750 item 4 summary <$text>: lines ${layout.lineCount}, ellipsised ${layout.isLineEllipsized(0)}")
        assertTrue("positive control: the summary names the long species <$text>", text.startsWith(longSpecies.commonName!!))
        assertEquals("the summary is one line", 1, layout.lineCount)
        assertTrue("the summary, longer than the bar's room, ends in an ellipsis", layout.isLineEllipsized(0))
        if (withSearch) assertTrue("Clear shows with the search", shown(SEARCH_BAR_CLEAR_TAG))
    }

    private fun summaryLayout(): Pair<String, TextLayoutResult> {
        val node = composeRule.onAllNodes(
            hasAnyAncestor(hasTestTag(SEARCH_ENTRY_BAR_TAG)) and androidx.compose.ui.test.SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().single { n -> n.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text }?.contains(" · ") == true }
        val text = node.config[SemanticsProperties.Text].joinToString("") { it.text }
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return text to results.single()
    }

    @Test fun `item 4 at ROTATION_90 a long species keeps the bar in line with the strip, ending in an ellipsis`() = assertInLine(Surface.ROTATION_90, withSearch = false)

    @Test fun `item 4 at ROTATION_270 a long species keeps the bar in line with the strip, ending in an ellipsis`() = assertInLine(Surface.ROTATION_270, withSearch = false)

    @Test fun `item 4 at ROTATION_90 a long species with a search and Clear keeps the bar in line`() = assertInLine(Surface.ROTATION_90, withSearch = true)

    @Test fun `item 4 at ROTATION_270 a long species with a search and Clear keeps the bar in line`() = assertInLine(Surface.ROTATION_270, withSearch = true)

    /**
     * Item 3 with item 4, the owner's own path: a species searched, Clear, then the turn. Clear resets the species, and the bar
     * still meets the strip ("make sure that the panel doesn't switch also, in case I'm wrong").
     */
    @Test
    fun `item 3 a species searched, Clear, then the turn: the selection is the default and the bar meets the strip`() {
        setScreenThenTurn(Surface.ROTATION_90) {
            pickLongSpecies()
            rig.searchCoordinates("45.326", "-122.634")
            rig.touch(composeRule.onNodeWithTag(SEARCH_BAR_CLEAR_TAG))
        }
        assertEquals("Clear put the selection back to the default", TaxonFilter.FUNGI, rig.viewModel.uiState.value.taxonFilter)
        assertFalse("the bar does not name the species", summaryLayout().first.contains(longSpecies.commonName!!))
        val bar = bounds(SEARCH_ENTRY_BAR_TAG)
        val strip = bounds(STRIP_TAG)
        assertEquals("the bar's top is the strip's top", strip.top.value, bar.top.value, 0.5f)
        assertEquals("the bar ends where the strip begins", strip.left.value, bar.right.value, 0.5f)
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xxhdpi")
class LandscapeSearchFixes780Test : LandscapeSearchFixesTests("w360dp-h780dp-xxhdpi", "w780dp-h360dp-land-xxhdpi")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class LandscapeSearchFixes823Test : LandscapeSearchFixesTests("w384dp-h823dp-xxhdpi", "w823dp-h384dp-land-xxhdpi")
