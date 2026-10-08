package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.unit.DpRect
import com.zynergylabs.forager.app.ui.map.MAP_LAYERS_SHEET_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Item 9 (dispatch `2026-09-28-104`, continuation `2026-09-28-142`; the owner: "have the map street/topo/satellite chips be centered between
 * the panel sides. The height position on the panel is fine as is."): the Layers sheet's map-type chip row is centred between the sheet's
 * sides, and its height is what it was. Through the real [AvailabilityScreen] and a real touch on the cluster's Layers row. The row's bounds
 * are the chips' own, first to last; the sheet's are the sheet node's.
 */
internal abstract class LayersChipsTests(private val baseChipTopDp: Float) {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private val map = BubbleMapSlot(emptyList())
    private val state = MapChromeScreenState()
    private val roles = MapChromeRoles()

    private fun openSheet() {
        composeRule.setContent { MapChromeTestScreen(state, map, roles) }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        composeRule.onNode(hasContentDescription("Layers:", substring = true)).performTouchInput { click(center) }
        composeRule.waitForIdle()
    }

    private fun chip(label: String): DpRect = composeRule.onNodeWithText(label).getUnclippedBoundsInRoot()

    @Test
    fun `the map-type chips are centred between the sheet's sides`() {
        openSheet()
        val sheet = composeRule.onNodeWithTag(MAP_LAYERS_SHEET_TAG).getUnclippedBoundsInRoot()
        val first = chip("Street")
        // The last chip has been Topographical since dispatch 2026-09-28-708 removed Satellite.
        val last = chip("Topographical")

        val rowCentre = (first.left.value + last.right.value) / 2f
        val sheetCentre = (sheet.left.value + sheet.right.value) / 2f
        assertEquals(
            "the chip row [${first.left.value}, ${last.right.value}] is centred on the sheet [${sheet.left.value}, ${sheet.right.value}]",
            sheetCentre,
            rowCentre,
            1f,
        )
    }

    @Test
    fun `guard - the map-type chips' top is where it was`() {
        openSheet()
        assertEquals("the chip row's top", baseChipTopDp, chip("Street").top.value, 0.5f)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
// 275 dp: the chip row's top at base (`a3221b4`'s tree plus the earlier items), read from the base run, not derived.
internal class LayersChipsPortraitTest : LayersChipsTests(baseChipTopDp = 275f)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
// 142 dp, read the same way.
internal class LayersChipsLandscapeTest : LayersChipsTests(baseChipTopDp = 142f)
