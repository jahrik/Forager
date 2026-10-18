package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.DpRect
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.log.RecordsSubTab
import com.zynergylabs.forager.app.ui.log.recordsFilterChipTestTag
import com.zynergylabs.forager.app.ui.map.PAN_RECORDING_MAP_TAG
import com.zynergylabs.forager.app.ui.map.PanRecordingMapSlot
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
 * L1 (dispatch `prompts/preserved/2026-09-28-47.md`, continuation `2026-09-28-49`): the Offline Maps
 * picker in a short landscape window. Owner's rulings: "Side by side (Recommended)" and "Pin OK/Download,
 * rest scrolls (Recommended)"; the planner's: the map on the punch-hole side, the controls by the rail
 * (P8, P9, P12).
 *
 * Driven through the real [AvailabilityScreen] (Journal, Records, the Offline maps chip, by semantic
 * clicks: navigation, not a claim about touch), with its state held here and the map a
 * [PanRecordingMapSlot] that a real drag pans. Every claim about a control being reached is a real
 * touch at window coordinates, sampled at five points across the control's own bounds, and no test
 * scrolls before touching OK or Download. Each rotation test first proves the screen saw the rotation.
 * Robolectric reports zero insets, so the status bar and cut-out are device items.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class LandscapeOfflinePickerTest {

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

    private val uiState = mutableStateOf(AvailabilityUiState())
    private val map = PanRecordingMapSlot(panTo = L1_PANNED)
    private var latChanges = 0
    private var downloads = 0
    private var rotationSeenByScreen: Int? = null

    private fun setScreen(rotation: Int?) {
        if (rotation != null) Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            rotationSeenByScreen = LocalView.current.display?.rotation
            AvailabilityScreen(
                uiState = uiState.value,
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
                // As AvailabilityViewModel does: the confirmed pick becomes the two text fields.
                onOfflineMapLatChanged = { latChanges++; uiState.value = uiState.value.copy(offlineMapLatText = it) },
                onOfflineMapLngChanged = { uiState.value = uiState.value.copy(offlineMapLngText = it) },
                onOfflineMapRadiusChanged = { uiState.value = uiState.value.copy(offlineMapRadiusKm = it, offlineMapRadiusTouched = true) },
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = { downloads++ },
                onDeleteOfflineRegion = {},
                onNightModeMapsChanged = {},
                onThemeModeChanged = {},
                mapSlot = map.slot,
            )
        }
        composeRule.waitForIdle()
        if (rotation != null) {
            assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeenByScreen)
        }
        composeRule.onNodeWithText("Journal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Records").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(recordsFilterChipTestTag(RecordsSubTab.OFFLINE_MAPS)).performClick()
        composeRule.waitForIdle()
    }

    private fun bounds(node: SemanticsNodeInteraction): DpRect = node.getUnclippedBoundsInRoot()
    private fun root(): DpRect = composeRule.onRoot().getUnclippedBoundsInRoot()
    private fun ok() = composeRule.onNodeWithText("OK")
    private fun cancel() = composeRule.onNodeWithText("Cancel")
    private fun download() = composeRule.onNodeWithText("Download Maps")
    private fun slider() = composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
    private fun mapNode() = composeRule.onNodeWithTag(PAN_RECORDING_MAP_TAG)
    private fun rail(): DpRect = composeRule.onNodeWithTag(COMPACT_NAVIGATION_RAIL_TAG).getUnclippedBoundsInRoot()

    private val fractions = listOf(0.5f to 0.5f, 0.15f to 0.2f, 0.85f to 0.2f, 0.15f to 0.8f, 0.85f to 0.8f)

    /** A real touch at a point of [rect], given as fractions of its width and height. */
    private fun touch(rect: DpRect, fx: Float, fy: Float) {
        val x = rect.left + (rect.right - rect.left) * fx
        val y = rect.top + (rect.bottom - rect.top) * fy
        composeRule.onRoot().performTouchInput { click(Offset(x.value * density, y.value * density)) }
        composeRule.waitForIdle()
    }

    private fun assertWhollyInWindow(what: String, rect: DpRect) {
        val window = root()
        assertTrue(
            "$what $rect lies wholly inside the window $window",
            rect.left >= window.left && rect.right <= window.right && rect.top >= window.top && rect.bottom <= window.bottom,
        )
    }

    // ── Tests first: OK, Cancel and Download are pinned, shown and reached by real touches, with no scrolling ──

    private fun checkPinnedActions(rotation: Int) {
        setScreen(rotation)
        ok().assertIsDisplayed()
        cancel().assertIsDisplayed()
        download().assertIsDisplayed()
        val okRect = bounds(ok())
        assertWhollyInWindow("OK", okRect)
        assertWhollyInWindow("Cancel", bounds(cancel()))

        for ((fx, fy) in fractions) touch(okRect, fx, fy)
        assertEquals("every sampled touch on OK confirmed the pick", fractions.size, latChanges)

        // OK confirmed a point, so Download is enabled now.
        val downloadRect = bounds(download())
        assertWhollyInWindow("Download", downloadRect)
        // Part 2 follow-ups F1 item 4: a touch on Download Maps now opens the confirmation instead of
        // starting the download, so "reached it" is the confirmation appearing; Cancel closes it again.
        for ((fx, fy) in fractions) {
            touch(downloadRect, fx, fy)
            composeRule.onNodeWithText("Download this area?").assertIsDisplayed()
            composeRule.onNode(hasText("Cancel") and hasAnyAncestor(isDialog())).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Download this area?").assertDoesNotExist()
        }
        assertEquals("no touch on Download Maps started a download: the confirmation comes first", 0, downloads)
    }

    @Test fun `L1 at ROTATION_90 OK, Cancel and Download are shown and take real touches across their bounds without scrolling`() =
        checkPinnedActions(Surface.ROTATION_90)

    @Test fun `L1 at ROTATION_270 OK, Cancel and Download are shown and take real touches across their bounds without scrolling`() =
        checkPinnedActions(Surface.ROTATION_270)

    // ── The slider is in view on open, then takes real touches ──
    //
    // RECORD -1253 (the owner: "C: OK, Cancel and Download on one line", so that "the optional name box at top with the radius
    // slider shown", RECORD -1243, holds): the slider is meant to be in view when the panel opens. This asserted the opposite
    // case, that the slider was reached by scrolling: real drags on the controls side until it lay wholly inside the scrolling
    // part, and `assertTrue("the controls side was scrolled by a real drag ($drags)", drags > 0)`. The touches across the
    // slider's bounds below are unchanged.

    private fun checkSliderByScrolling(rotation: Int) {
        setScreen(rotation)
        val mapRect = bounds(mapNode())
        val area = bounds(composeRule.onNodeWithTag(L1_CONTROLS_SCROLL_TAG))
        val opened = bounds(slider())
        assertTrue("the slider $opened lies wholly in the scrolling part $area on open, with no drag", opened.top >= area.top && opened.bottom <= area.bottom)
        slider().assertIsDisplayed()
        val sliderRect = bounds(slider())
        assertWhollyInWindow("the slider", sliderRect)
        assertTrue("the slider is on the controls side, off the map ($sliderRect, map $mapRect)", sliderRect.left >= mapRect.right || sliderRect.right <= mapRect.left)

        var previous = uiState.value.offlineMapRadiusKm
        var moved = 0
        for ((fx, fy) in listOf(0.1f to 0.5f, 0.3f to 0.3f, 0.5f to 0.7f, 0.7f to 0.3f, 0.9f to 0.5f)) {
            touch(sliderRect, fx, fy)
            val now = uiState.value.offlineMapRadiusKm
            assertTrue("a touch at $fx across the slider moved the radius past $previous (now $now)", now > previous || (fx == 0.1f && now != previous))
            previous = now
            moved++
        }
        assertEquals(5, moved)
    }

    @Test fun `RECORD -1253 at ROTATION_90 the slider is in view on open and takes real touches across its bounds`() =
        checkSliderByScrolling(Surface.ROTATION_90)

    @Test fun `RECORD -1253 at ROTATION_270 the slider is in view on open and takes real touches across its bounds`() =
        checkSliderByScrolling(Surface.ROTATION_270)

    // ── RECORD -1253: OK, Cancel and Download on one line, each hit where it is drawn ──

    @Test
    fun `RECORD -1253 OK, Cancel and Download share one line and each takes real touches where it is drawn`() {
        setScreen(Surface.ROTATION_90)
        val okRect = bounds(ok())
        val cancelRect = bounds(cancel())
        val downloadRect = bounds(download())
        for ((name, r) in listOf("Cancel" to cancelRect, "Download" to downloadRect)) {
            assertEquals("$name is on OK's line (centre)", (okRect.top + okRect.bottom).value / 2, (r.top + r.bottom).value / 2, 1f)
        }
        assertTrue("OK $okRect, then Cancel $cancelRect, side by side", okRect.right <= cancelRect.left)
        assertTrue("Cancel $cancelRect, then Download $downloadRect, side by side", cancelRect.right <= downloadRect.left)
        assertWhollyInWindow("Download", downloadRect)
        // Cancel first, while nothing is picked: touches across it confirm nothing and open nothing.
        for ((fx, fy) in fractions) touch(cancelRect, fx, fy)
        assertEquals("no touch on Cancel reached OK", 0, latChanges)
        composeRule.onNodeWithText("Download this area?").assertDoesNotExist()
        // OK, across its bounds: each confirms the pick.
        for ((fx, fy) in fractions) touch(okRect, fx, fy)
        assertEquals("every sampled touch on OK confirmed the pick", fractions.size, latChanges)
        // Download, enabled now, across its bounds: each opens the confirmation, closed again by its Cancel.
        for ((fx, fy) in fractions) {
            touch(downloadRect, fx, fy)
            composeRule.onNodeWithText("Download this area?").assertIsDisplayed()
            composeRule.onNode(hasText("Cancel") and hasAnyAncestor(isDialog())).performClick()
            composeRule.waitForIdle()
        }
        assertEquals("the touches on OK all confirmed the pick, none reached it from Download", fractions.size, latChanges)
        assertEquals("no download started without the confirmation", 0, downloads)
    }

    // ── Tests first: side by side, the map full height on the punch-hole side, the controls by the rail ──

    private fun checkSides(rotation: Int) {
        setScreen(rotation)
        val mapRect = bounds(mapNode())
        val okRect = bounds(ok())
        val railRect = rail()
        val window = root()
        val railOnRight = railRect.left > (window.left + window.right) / 2
        // RECORD -956 (the owner: "One should look exactly line the other"), and the planner's answer 3 (the picker's controls on the right): the rail
        // on the right at both rotations. Was `assertEquals("ROTATION_90 has the rail on the right, 270 on the left",
        // rotation == Surface.ROTATION_90, railOnRight)`.
        assertTrue("the rail is on the right at rotation $rotation", railOnRight)
        if (railOnRight) {
            assertTrue("the map ($mapRect) is on the punch-hole side, left of the controls ($okRect)", mapRect.right <= okRect.left)
        } else {
            assertTrue("the map ($mapRect) is on the punch-hole side, right of the controls ($okRect)", mapRect.left >= okRect.right)
        }
        assertEquals("the map runs to the bottom of the window", window.bottom.value, mapRect.bottom.value, 0.5f)
        assertEquals(
            "the map starts where the controls side does, under the chip row",
            bounds(composeRule.onNodeWithTag(com.zynergylabs.forager.app.ui.log.RECORDS_FILTER_CHIP_ROW_TAG)).bottom.value,
            mapRect.top.value,
            0.5f,
        )
    }

    @Test fun `L1 at ROTATION_90 the map is full height on the punch-hole side and the controls by the rail`() = checkSides(Surface.ROTATION_90)

    @Test fun `L1 at ROTATION_270 the map is full height on the punch-hole side and the controls by the rail`() = checkSides(Surface.ROTATION_270)

    // ── Check (not tests-first): a drag on the map pans it and reaches no control ──

    @Test
    fun `L1 a drag on the map pans it and reaches no control`() {
        setScreen(Surface.ROTATION_90)
        val radiusBefore = uiState.value.offlineMapRadiusKm
        val textBefore = bounds(composeRule.onNodeWithText(OFFLINE_INSTRUCTION, substring = true))

        mapNode().performTouchInput { swipe(center, center - Offset(0f, 150f), 400) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(l1PinText(L1_PANNED)).assertExists()
        assertEquals("the drag moved no control's value", radiusBefore, uiState.value.offlineMapRadiusKm)
        assertEquals("the drag confirmed nothing", 0, latChanges)
        assertEquals("the drag downloaded nothing", 0, downloads)
        assertEquals(
            "the drag did not scroll the controls side",
            textBefore,
            bounds(composeRule.onNodeWithText(OFFLINE_INSTRUCTION, substring = true)),
        )
    }

    // ── Check (a pin, passes before and after by design): portrait is unchanged ──

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `L1 portrait keeps the stacked picker, the map 4 by 3 across the width and OK below it`() {
        setScreen(rotation = null)
        val mapRect = bounds(mapNode())
        val window = root()
        assertEquals("the map spans the width", (window.right - window.left).value, (mapRect.right - mapRect.left).value, 0.5f)
        assertEquals("the map is 4:3", (mapRect.right - mapRect.left).value * 3f / 4f, (mapRect.bottom - mapRect.top).value, 0.5f)
        assertTrue("OK is below the map", bounds(ok()).top >= mapRect.bottom)
        assertTrue("Download is below OK", bounds(download()).top >= bounds(ok()).bottom)
    }
}

/** The production tag on the controls side's scrolling part (a literal, so this compiles before it exists). */
private const val L1_CONTROLS_SCROLL_TAG = "offline-picker-controls-scroll"
private const val OFFLINE_INSTRUCTION = "Offline downloads cover the continental United States"
private val L1_PANNED = LatLng(45.7, -122.9)
private fun l1PinText(at: LatLng) = "Pin at: ${"%.4f".format(at.lat)}, ${"%.4f".format(at.lng)}"
