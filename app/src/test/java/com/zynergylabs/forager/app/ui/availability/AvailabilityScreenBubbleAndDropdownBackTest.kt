package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performScrollTo
import com.zynergylabs.forager.app.ui.map.MAP_BUBBLE_TAG
import com.zynergylabs.forager.app.ui.map.fanout.memberPositionDp
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.log.MushroomLogUiState
import com.zynergylabs.forager.app.ui.map.CENTRE_PIN_CONFIRM_ROW_TAG
import com.zynergylabs.forager.app.ui.map.MapFeatureTap
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.map.fanout.FanOutTestScene
import com.zynergylabs.forager.app.ui.map.fanout.MapTapHandler
import com.zynergylabs.forager.app.ui.map.fanout.MapTapSinks
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutBackHandler
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutHost
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutState
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-312, items 11 and 12: the Back order between a map bubble, a fan, fullscreen and the
 * search dropdown, through the real [AvailabilityScreen], real touches and the real activity dispatcher.
 *
 * Item 11: a bubble closes before the fan and after anything opened after it (here fullscreen and the
 * dropdown). Item 12: with a fan open and the dropdown open, Back closes the dropdown and the fan stays; the
 * next Back folds the fan. The map is the same stub [AvailabilityScreenFanBackOthersTest] uses.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class AvailabilityScreenBubbleAndDropdownBackTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val fan = MarkerFanOutState()
    private var scene: FanOutTestScene? = null
    private val log = MushroomLogUiState(entries = listOf(BUBBLE_FIND), galleryPhotos = listOf(BUBBLE_PHOTO))

    private val slot: MapSlot = { _, content, renderMode, _, _, onTap, _, _, modifier ->
        val density = LocalDensity.current.density
        val currentOnTap by rememberUpdatedState(onTap)
        val currentOnFeatureTap by rememberUpdatedState(renderMode.onFeatureTap)
        val bubbleShown = content.focusedFeature != null || content.focusedObservationId != null
        val currentBubbleShown by rememberUpdatedState(bubbleShown)
        val probe = remember { FanOutTestScene(density).also { scene = it; it.hidden = { fan.members.map { m -> m.key }.toSet() } } }
        val handler = remember {
            MapTapHandler(
                fan = fan,
                probe = probe,
                drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, MapLayersState.DEFAULT) },
                sinks = object : MapTapSinks {
                    override fun onPlainTap() = currentOnTap()
                    override fun onSightingTap(observationId: Long?, xPx: Float, yPx: Float) = currentOnTap()
                    override fun onFeatureTap(layerId: String, featureId: String, xPx: Float, yPx: Float, at: LatLng) =
                        currentOnFeatureTap(MapFeatureTap(layerId, featureId, Offset(xPx, yPx), 0f, at))
                    override fun onUnidentifiedFeature(layerId: String) = currentOnTap()
                },
                bubbleOpen = { currentBubbleShown },
            )
        }
        MarkerFanOutHost(fan)
        MarkerFanOutBackHandler(fan, bubbleOpen = bubbleShown, backEnabled = renderMode.backEnabled)
        Box(
            modifier
                .testTag(MAP_TAG)
                .pointerInput(Unit) { detectTapGestures { handler.onMapTap(LatLng(0.0, 0.0), it.x, it.y) } },
        )
    }

    private val density get() = composeRule.density.density

    private fun settle() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
    }

    private fun touchMap(xDp: Float, yDp: Float) {
        composeRule.onNodeWithTag(MAP_TAG).performTouchInput { click(Offset(xDp * density, yDp * density)) }
        settle()
    }

    private fun back() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        settle()
    }

    private fun composeFan() {
        val store = OneCellStore()
        val viewModel = mapLayersViewModel(store = store)
        composeRule.setContent { MapLayersTestScreen(viewModel = viewModel, mapSlot = slot, store = store, logUiState = log) }
        composeRule.waitForIdle()
    }

    private fun fanOpen() {
        composeFan()
        val s = checkNotNull(scene)
        s.addAtScreen(MapLayerIds.FINDS, "find-1", SPOT_X * density, SPOT_Y * density)
        s.addAtScreen(MapLayerIds.PHOTOS, "ph-1", SPOT_X * density, SPOT_Y * density)
        touchMap(SPOT_X, SPOT_Y)
        assertTrue("the stack fanned", fan.isOpen)
    }

    private fun shown(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun fullscreenOn() = runCatching { composeRule.onNodeWithContentDescription("Exit fullscreen").assertIsDisplayed() }.isSuccess

    private fun openDropdown() {
        composeRule.onNodeWithTag(ACTIVE_SEARCH_SUMMARY_TAG).performTouchInput { click() }
        settle()
        assertTrue("the search dropdown is open", shown(SEARCH_DROPDOWN_TAG))
    }

    private fun openAddMenu() {
        composeRule.onNodeWithContentDescription("Plan a trip or log a find here").performTouchInput { click() }
        settle()
        assertTrue("the add-action menu is open", shown(ADD_ACTION_TILE_TAG))
    }

    private fun openBubbleOverFan() {
        fanOpen()
        val find = fan.members.single { it.key.featureId == "find-1" }
        val at = memberPositionDp(find, fan.progress)
        touchMap(at.xDp, at.yDp)
        composeRule.onNodeWithTag(MAP_BUBBLE_TAG).assertIsDisplayed()
        assertTrue("the fan is still open under the bubble", fan.isOpen)
    }

    private fun bubbleShown() = shown(MAP_BUBBLE_TAG)

    @Test
    fun `a bubble over a fan, then fullscreen on, Back leaves fullscreen, then closes the bubble, then folds the fan`() {
        openBubbleOverFan()
        composeRule.onNodeWithContentDescription("Fullscreen").performTouchInput { click() }
        settle()
        assertTrue("fullscreen is on", fullscreenOn())

        back()
        assertFalse("the first Back left fullscreen", fullscreenOn())
        assertTrue("the first Back left the bubble", bubbleShown())
        assertTrue("the first Back left the fan", fan.isOpen)

        back()
        assertFalse("the second Back closed the bubble", bubbleShown())
        assertTrue("the second Back left the fan", fan.isOpen)

        back()
        assertFalse("the third Back folded the fan", fan.isOpen)
    }

    @Ignore(BUBBLE_DROPDOWN_REASON)
    @Test
    fun `a bubble over a fan, then the dropdown open, the first Back goes to the dropdown and leaves the bubble and the fan`() {
        openBubbleOverFan()
        openDropdown()

        back()

        // Only the bubble and fan are asserted: whether the dropdown is closed after Back is not observable in this harness,
        // which refocuses the search field when the dropdown closes and so reopens it (see the ignored tests below).
        assertTrue("the first Back left the bubble up: the dropdown, opened after it, took the Back", bubbleShown())
        assertTrue("the first Back left the fan", fan.isOpen)
    }

    @Ignore(DROPDOWN_REFOCUS_REASON)
    @Test
    fun `with a fan open and the dropdown open, Back closes the dropdown and the fan stays, then the next Back folds the fan`() {
        fanOpen()
        openDropdown()

        back()
        assertFalse("the first Back closed the dropdown", shown(SEARCH_DROPDOWN_TAG))
        assertTrue("the first Back left the fan open", fan.isOpen)

        back()
        assertFalse("the second Back folded the fan", fan.isOpen)
    }

    @Ignore(DROPDOWN_REFOCUS_REASON)
    @Test
    fun `with no fan, Back closes the dropdown and it is still closed after the screen settles`() {
        composeFan()
        openDropdown()

        back()
        assertFalse("Back closed the dropdown", shown(SEARCH_DROPDOWN_TAG))

        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertFalse("the dropdown stayed closed", shown(SEARCH_DROPDOWN_TAG))
    }

    private companion object {
        // Red today: the first Back closes the bubble ahead of the dropdown. Adding the dropdown to the bubble's gate makes
        // -293's AvailabilityScreenFanBackDrawerTest bubble test fail through the same refocus-reopen, so it waits for the owner.
        const val BUBBLE_DROPDOWN_REASON = "blocked on an owner ruling: red today (the bubble closes before a dropdown opened after it); the fix is entangled with the dropdown refocus (completion report, Part B)"
        // Dispatch 2026-09-28-312, item 12, stopped for the owner: closing the dropdown runs focusManager.clearFocus(force = true)
        // (AvailabilityCompactScaffold.kt, the LaunchedEffect on showSearchDropdown), which reaches View.clearFocus and
        // View.rootViewRequestFocus; AndroidComposeView.requestFocus then focuses the search field, and onFieldFocused reopens the dropdown.
        // Traced, see the completion report. Whether a device does the same is not known; the fix changes how the dropdown opens.
        const val DROPDOWN_REFOCUS_REASON = "blocked on an owner ruling: closing the dropdown refocuses the search field and reopens it (completion report, Part B)"
        const val MAP_TAG = "bubble-dropdown-back-map"
        const val SPOT_X = 150f
        const val SPOT_Y = 420f
    }
}
