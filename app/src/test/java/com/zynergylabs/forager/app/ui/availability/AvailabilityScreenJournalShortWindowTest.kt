package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.FindDecision
import com.zynergylabs.forager.app.ui.log.CartographyUiState
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
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * Journal redesign J5, short windows (`prompts/preserved/2026-09-27-26.md`; plan L1-L3 and the
 * owner's ruling 1, "Hide header; icon reveals it (Recommended)"), driven through the real
 * [AvailabilityScreen] at `w823dp-h384dp-land`, both rotations where the rail's side matters.
 *
 * - The app-wide search header is hidden on the Journal tab in a short window, stays on other tabs
 *   and in portrait, and L1's search icon brings it up.
 * - L1: one pinned 48 dp row holding the Entries | Records switch, the search icon and "New".
 * - L2: no floating button; "New" (timeline) and the photo button (album) sit in the L1 row.
 * - L3: the drafts chip in a second row with the view toggle, hiding on a scroll down and
 *   returning on a scroll up.
 * - The height budget (the dispatch's "before building"), measured and asserted to leave two card
 *   rows.
 *
 * Every control a finger taps is touched with `performTouchInput` at several points across its own
 * bounds (CLAUDE.md, "A semantic `performClick` asserts wiring, not routing"); a rail item or the
 * Records switch used only to *get to* a screen may be a semantic click. Tags are literals, so these
 * compile against the base they were written before.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land")
class AvailabilityScreenJournalShortWindowTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private var rotationSeenByScreen: Int? = null
    private val startedEntries = mutableListOf<LocalDate>()
    private val openedEntries = mutableListOf<String>()
    private val cameraTargets = mutableListOf<com.zynergylabs.forager.app.ui.log.InAppCameraTarget>()

    private fun setScreen(rotation: Int, entries: List<CartographyEntry> = MANY_ENTRIES, drafts: List<CartographyEntry> = emptyList()) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(rotation)
        composeRule.setContent {
            rotationSeenByScreen = LocalView.current.display?.rotation
            var cartography by androidx.compose.runtime.remember { mutableStateOf(CartographyUiState(entries = entries, draftEntries = drafts)) }
            shortWindowScreen(
                cartography = cartography,
                onStart = { date -> startedEntries += date },
                onOpen = { id ->
                    openedEntries += id
                    cartography = cartography.copy(editingEntry = (cartography.entries + cartography.draftEntries).first { it.id == id })
                },
                onClose = { cartography = cartography.copy(editingEntry = null) },
                onOpenCamera = { target -> cameraTargets += target },
            )
        }
        composeRule.waitForIdle()
        assertEquals("the screen must actually see the rotation this test is about", rotation, rotationSeenByScreen)
    }

    private fun openJournal() {
        composeRule.onNodeWithText("Journal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Journal").assertIsSelected()
    }

    private fun node(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)
    private fun bounds(tag: String): DpRect = node(tag).getUnclippedBoundsInRoot()
    private fun exists(tag: String): Boolean = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    /** A real touch at a fraction of [tag]'s own bounds. */
    private fun touch(tag: String, fraction: Offset) {
        val r = bounds(tag)
        val x = (r.left + (r.right - r.left) * fraction.x).value
        val y = (r.top + (r.bottom - r.top) * fraction.y).value
        composeRule.onRoot().performTouchInput { click(Offset(x * density, y * density)) }
        composeRule.waitForIdle()
    }

    // ── Ruling 1: the app-wide search header ──

    private fun checkHeaderHiddenOnJournal(rotation: Int) {
        setScreen(rotation)
        openJournal()
        assertTrue("the app-wide search header is hidden on the Journal tab in a short window", !exists(SEARCH_ENTRY_BAR_TAG))
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        assertTrue("and on Records", !exists(SEARCH_ENTRY_BAR_TAG))
    }

    @Test fun `ruling 1 at ROTATION_90 the search header is hidden on the Journal tab in a short window`() = checkHeaderHiddenOnJournal(Surface.ROTATION_90)
    @Test fun `ruling 1 at ROTATION_270 the search header is hidden on the Journal tab in a short window`() = checkHeaderHiddenOnJournal(Surface.ROTATION_270)

    @Test
    fun `ruling 1 the search header still shows on List and Seasonal in a short window`() {
        setScreen(Surface.ROTATION_90)
        composeRule.onNodeWithText("List").performClick()
        composeRule.waitForIdle()
        node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
        openJournal()
        composeRule.onNodeWithText("Seasonal").performClick()
        composeRule.waitForIdle()
        node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
    }

    private fun checkSearchIconReveals(rotation: Int) {
        setScreen(rotation)
        openJournal()
        var revealed = 0
        for (fraction in TOUCH_SAMPLES) {
            assertTrue("hidden before the touch", !exists(SEARCH_ENTRY_BAR_TAG))
            touch(SEARCH_ICON, fraction)
            node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
            revealed++
            // Dismissed by the same icon.
            touch(SEARCH_ICON, Offset(0.5f, 0.5f))
            assertTrue("the icon touched again hides the header", !exists(SEARCH_ENTRY_BAR_TAG))
        }
        assertEquals("every sampled point revealed the header", TOUCH_SAMPLES.size, revealed)
        // Dismissed by Back too.
        touch(SEARCH_ICON, Offset(0.5f, 0.5f))
        node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
        assertTrue("Back hides the revealed header", !exists(SEARCH_ENTRY_BAR_TAG))
        composeRule.onNodeWithText("Journal").assertIsSelected()
    }

    @Test fun `ruling 1 at ROTATION_90 real touches on the search icon reveal the header, and the icon or Back hides it`() = checkSearchIconReveals(Surface.ROTATION_90)
    @Test fun `ruling 1 at ROTATION_270 real touches on the search icon reveal the header, and the icon or Back hides it`() = checkSearchIconReveals(Surface.ROTATION_270)

    @Test
    fun `ruling 1 on Records the search icon reveals the header too`() {
        setScreen(Surface.ROTATION_90)
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        touch(SEARCH_ICON, Offset(0.3f, 0.6f))
        node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `ruling 1 portrait keeps the search header on the Journal tab, and has no L1 row`() {
        setScreen(Surface.ROTATION_0)
        openJournal()
        node(SEARCH_ENTRY_BAR_TAG).assertIsDisplayed()
        assertTrue("portrait has no short-window header row", !exists(SHORT_HEADER))
        assertTrue("portrait has no search icon of its own", !exists(SEARCH_ICON))
        // Portrait's own chrome is unchanged: the floating button and the full-width switch.
        node(ENTRIES_FAB).assertIsDisplayed()
        node(SWITCH).assertIsDisplayed()
    }

    // ── L1: one pinned 48 dp header row ──

    private fun checkHeaderRow(rotation: Int) {
        setScreen(rotation)
        openJournal()
        val row = bounds(SHORT_HEADER)
        assertEquals("the L1 row is 48 dp tall", 48f, row.height.value, 0.5f)
        for (tag in listOf(SWITCH, SEARCH_ICON, NEW_ICON)) {
            val r = bounds(tag)
            assertTrue("$tag $r sits inside the L1 row $row", r.top >= row.top - 0.5.dp && r.bottom <= row.bottom + 0.5.dp && r.left >= row.left - 0.5.dp && r.right <= row.right + 0.5.dp)
        }
        assertTrue("the switch is at the row's start, the icons after it", bounds(SWITCH).right <= bounds(SEARCH_ICON).left)
        // The switch's two segments take real touches across their bounds.
        for (fraction in TOUCH_SAMPLES) {
            touch(SWITCH_RECORDS, fraction)
            node(SWITCH_RECORDS).assertIsSelected()
            touch(SWITCH_ENTRIES, fraction)
            node(SWITCH_ENTRIES).assertIsSelected()
        }
    }

    @Test fun `L1 at ROTATION_90 one 48 dp row holds the switch, the search icon and New`() = checkHeaderRow(Surface.ROTATION_90)
    @Test fun `L1 at ROTATION_270 one 48 dp row holds the switch, the search icon and New`() = checkHeaderRow(Surface.ROTATION_270)

    @Test
    fun `L1 the header row stays pinned while the entries scroll`() {
        setScreen(Surface.ROTATION_90)
        openJournal()
        val before = bounds(SHORT_HEADER)
        composeRule.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        val after = bounds(SHORT_HEADER)
        assertEquals("the header row did not move", before.top.value, after.top.value, 0.5f)
        node(SHORT_HEADER).assertIsDisplayed()
    }

    @Test
    fun `L1 on Records the row holds the switch and the search icon, and no New`() {
        setScreen(Surface.ROTATION_270)
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        node(SHORT_HEADER).assertIsDisplayed()
        node(SEARCH_ICON).assertIsDisplayed()
        assertTrue("Records has no New (portrait's Records has no floating button to move)", !exists(NEW_ICON))
    }

    // ── L2: no floating button; New and the photo button in the L1 row ──

    private fun checkNewInRow(rotation: Int) {
        setScreen(rotation)
        openJournal()
        assertTrue("no floating button in a short window", !exists(ENTRIES_FAB))
        var started = 0
        for (fraction in TOUCH_SAMPLES) {
            touch(NEW_ICON, fraction)
            started++
            assertEquals("each touch on New starts one entry today", started, startedEntries.size)
            assertEquals(LocalDate.now(), startedEntries.last())
        }
    }

    @Test fun `L2 at ROTATION_90 no floating button, and real touches on New in the row start an entry`() = checkNewInRow(Surface.ROTATION_90)
    @Test fun `L2 at ROTATION_270 no floating button, and real touches on New in the row start an entry`() = checkNewInRow(Surface.ROTATION_270)

    @Test
    fun `L2 on the album the row's photo button opens Take photo and Import, and there is no floating button`() {
        setScreen(Surface.ROTATION_90)
        openJournal()
        node(VIEW_ALBUM).performClick()
        composeRule.waitForIdle()
        assertTrue("no floating button on the album either", !exists(ENTRIES_FAB))
        assertTrue("the album's row has no New", !exists(NEW_ICON))
        Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>()).grantPermissions(android.Manifest.permission.CAMERA)
        for ((i, fraction) in TOUCH_SAMPLES.withIndex()) {
            touch(PHOTO_ICON, fraction)
            node(MENU_TAKE_PHOTO).assertIsDisplayed()
            node(MENU_IMPORT).assertIsDisplayed()
            assertEquals("the button alone opens no camera", i, cameraTargets.size)
            // Take photo closes the menu and opens the album's camera (Back cannot reach a popup
            // window under Robolectric, so the menu is closed by using it).
            node(MENU_TAKE_PHOTO).performTouchInput { click(Offset(width * fraction.x, height * fraction.y)) }
            composeRule.waitForIdle()
            assertEquals(i + 1, cameraTargets.size)
            assertEquals(com.zynergylabs.forager.app.ui.log.InAppCameraTarget.ALBUM, cameraTargets.last())
            assertTrue("the menu closed", !exists(MENU_TAKE_PHOTO))
        }
        val row = bounds(SHORT_HEADER)
        val photo = bounds(PHOTO_ICON)
        assertTrue("the photo button $photo is in the L1 row $row", photo.top >= row.top && photo.bottom <= row.bottom)
    }

    // ── L3: the drafts chip in the second row, which hides on scroll ──

    @Test
    fun `L3 the drafts chip sits in the second row with the view toggle, and the banner is gone`() {
        setScreen(Surface.ROTATION_90, drafts = TWO_DRAFTS)
        openJournal()
        assertTrue("no drafts banner in a short window", !exists(DRAFTS_BANNER))
        composeRule.onNodeWithText("✎ 2 drafts ›").assertIsDisplayed()
        val chip = bounds(DRAFTS_CHIP)
        val toggle = bounds(VIEW_TIMELINE)
        val header = bounds(SHORT_HEADER)
        assertEquals("the chip and the toggle share one row", (chip.top + chip.bottom).value / 2, (toggle.top + toggle.bottom).value / 2, 2f)
        assertTrue("that row is below the L1 row", chip.top >= header.bottom - 0.5.dp)
    }

    @Test
    fun `L3 with several drafts, real touches on the chip open the full-screen list`() {
        var opened = 0
        for (fraction in TOUCH_SAMPLES) {
            setScreenOnce(drafts = TWO_DRAFTS)
            touch(DRAFTS_CHIP, fraction)
            node(DRAFTS_LIST).assertIsDisplayed()
            opened++
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
            composeRule.waitForIdle()
            node(DRAFTS_CHIP).assertIsDisplayed()
        }
        assertEquals(TOUCH_SAMPLES.size, opened)
    }

    @Test
    fun `L3 with one draft, a real touch on the chip opens that draft`() {
        setScreen(Surface.ROTATION_270, drafts = listOf(TWO_DRAFTS.first()))
        openJournal()
        composeRule.onNodeWithText("✎ 1 draft ›").assertIsDisplayed()
        touch(DRAFTS_CHIP, Offset(0.8f, 0.4f))
        assertEquals(listOf(TWO_DRAFTS.first().id), openedEntries)
    }

    private var screenSet = false
    private fun setScreenOnce(drafts: List<CartographyEntry>) {
        if (!screenSet) {
            setScreen(Surface.ROTATION_90, drafts = drafts)
            openJournal()
            screenSet = true
        }
    }

    @Test
    fun `L3 the second row hides on a real scroll down and returns on a scroll up`() {
        setScreen(Surface.ROTATION_90, drafts = TWO_DRAFTS)
        openJournal()
        node(VIEW_TIMELINE).assertIsDisplayed()
        node(DRAFTS_CHIP).assertIsDisplayed()
        val grid = composeRule.onNode(hasScrollToIndexAction())
        grid.performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertTrue("the toggle row hides on a scroll down", !exists(VIEW_TIMELINE))
        assertTrue("with the drafts chip", !exists(DRAFTS_CHIP))
        node(SHORT_HEADER).assertIsDisplayed()
        grid.performTouchInput { swipeDown() }
        composeRule.waitForIdle()
        node(VIEW_TIMELINE).assertIsDisplayed()
        node(DRAFTS_CHIP).assertIsDisplayed()
    }

    @Test
    fun `L3 on Records the filter chip row hides on a real scroll down and returns on a scroll up`() {
        setScreen(Surface.ROTATION_270)
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        node(CHIP_ROW).assertIsDisplayed()
        // The All logbook, with more rows than fit: sixteen waypoints on sixteen days.
        val list = composeRule.onNode(hasScrollAction() and hasAnyDescendant(hasText("Pin 1")))
        list.performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertTrue("the chip row hides on a scroll down", !exists(CHIP_ROW))
        node(SHORT_HEADER).assertIsDisplayed()
        list.performTouchInput { swipeDown() }
        composeRule.waitForIdle()
        node(CHIP_ROW).assertIsDisplayed()
        assertTrue("the chip row is the second row, below L1", bounds(CHIP_ROW).top >= bounds(SHORT_HEADER).bottom - 0.5.dp)
    }

    // ── The height budget ──

    private fun checkHeightBudget(rotation: Int) {
        setScreen(rotation, drafts = TWO_DRAFTS)
        openJournal()
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val l1 = bounds(SHORT_HEADER)
        val l3 = bounds(DRAFTS_CHIP).let { chip -> DpRect(chip.left, minOf(chip.top, bounds(VIEW_TIMELINE).top), chip.right, maxOf(chip.bottom, bounds(VIEW_TIMELINE).bottom)) }
        val grid = bounds(ENTRIES_GRID)
        val content = grid.bottom - grid.top
        println("J5-HEIGHT rotation=$rotation entries window=${root.height} l1=${l1.height} (${l1.top}..${l1.bottom}) l3=${l3.height} (${l3.top}..${l3.bottom}) content=$content (${grid.top}..${grid.bottom})")
        val first = bounds(cardTag(MANY_ENTRIES[0].id))
        val third = bounds(cardTag(MANY_ENTRIES[2].id))
        assertTrue("two card rows fit in the content area: the second row ($third) ends by ${grid.bottom}", third.bottom <= grid.bottom + 0.5.dp)
        assertTrue("the first card row starts in the content area", first.top >= grid.top - 0.5.dp)
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        val rl1 = bounds(SHORT_HEADER)
        val chips = bounds(CHIP_ROW)
        val list = bounds(RECORDS_ALL_LIST)
        println("J5-HEIGHT rotation=$rotation records l1=${rl1.height} chips=${chips.height} (${chips.top}..${chips.bottom}) content=${list.bottom - list.top} (${list.top}..${list.bottom})")
        assertTrue("Records chrome (L1 + chip row) is under B3's 197 dp", chips.bottom < 197.dp)
    }

    /**
     * Native graphics: under Robolectric's default (legacy) graphics every glyph measures about one
     * pixel wide and text lines come out taller than real ones, so a height built from text is not a
     * real height there (found while building this stage; see the completion report). Native graphics
     * lays text out with real fonts. Still not the device's font or insets: the device figure is a
     * device item.
     */
    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `height budget at ROTATION_90 the header hidden leaves two card rows on Entries`() = checkHeightBudget(Surface.ROTATION_90)

    @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `height budget at ROTATION_270 the header hidden leaves two card rows on Entries`() = checkHeightBudget(Surface.ROTATION_270)

    // ── L5a: the Records chips fit one line at 640 dp (continuation 2026-09-27-27, owner: "Tighter chips") ──

    private var fontScaleSeenByScreen: Float? = null

    /**
     * Records' All chip row through the real screen, with two-digit counts on All (38), Finds (12),
     * Tracks (10) and Waypoints (16) and none on Offline maps: the counts the owner's option (a) was
     * measured with in the J5 report (native graphics, "fits by about 6 dp").
     */
    private fun openRecordsWithCounts() {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_90)
        composeRule.setContent {
            fontScaleSeenByScreen = androidx.compose.ui.platform.LocalDensity.current.fontScale
            shortWindowScreen(
                cartography = CartographyUiState(),
                onStart = {},
                onOpen = {},
                onClose = {},
                logState = com.zynergylabs.forager.app.ui.log.MushroomLogUiState(entries = (1..12).map { committedFind("find-$it") }),
                tracks = (1..10).map { recordedTrack("track-$it") },
            )
        }
        composeRule.waitForIdle()
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("38").assertExists()
    }

    /**
     * How far the chip row's content runs past the row: the last chip's end plus the row's own end
     * padding (taken equal to its start padding, the first chip's inset), less the row's end. Zero or
     * less means every chip, and the padding after the last, is on screen without scrolling.
     */
    private fun chipRowOverflow(): Pair<Float, String> {
        val row = bounds(CHIP_ROW)
        val first = bounds(CHIP_ALL)
        val last = bounds(CHIP_OFFLINE)
        val padding = first.left - row.left
        val contentEnd = last.right + padding
        val overflow = (contentEnd - row.right).value
        return overflow to "row ${row.left}..${row.right} (${row.right - row.left}), chips ${first.left}..${last.right}, padding $padding, content ends at $contentEnd"
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `L5a guard - at 640 dp the five Records chips, icons and counts, fit one line without scrolling`() {
        openRecordsWithCounts()
        assertEquals("this case is at the default font scale", 1f, fontScaleSeenByScreen)
        val row = bounds(CHIP_ROW)
        assertEquals("the chip row is B3's capped 640 dp", 640f, (row.right - row.left).value, 0.5f)
        val chips = listOf(CHIP_ALL, "records-chip-finds", "records-chip-tracks", CHIP_WAYPOINTS, CHIP_OFFLINE).map { bounds(it) }
        chips.forEach { assertEquals("one line", chips.first().top.value, it.top.value, 0.5f) }
        val (overflow, detail) = chipRowOverflow()
        println("J5-L5A fontScale=1.0 overflow=$overflow $detail")
        assertTrue("the chip row's content is ${overflow}dp wider than the row: $detail", overflow <= 0f)
    }

    /**
     * The same row at font scale 1.15. A record, not a guard, by the continuation's design: it
     * states the result either way, and the owner chose not to change the design further if it
     * overflows. It asserts only that the case ran at the scale it names.
     */
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(fontScale = 1.15f)
    fun `L5a record - at font scale 1_15 the chip row's fit is measured and stated`() {
        openRecordsWithCounts()
        assertEquals("this case runs at font scale 1.15", 1.15f, fontScaleSeenByScreen ?: 0f, 0.001f)
        val (overflow, detail) = chipRowOverflow()
        val result = if (overflow <= 0f) "fits, ${-overflow}dp to spare" else "OVERFLOWS by ${overflow}dp; the row scrolls"
        println("J5-L5A fontScale=1.15 $result: $detail")
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `L5a portrait chips are unchanged - 16 dp row padding, 8 dp gaps, 24 dp icons`() {
        setScreen(Surface.ROTATION_0)
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        val row = bounds(CHIP_ROW)
        val all = bounds(CHIP_ALL)
        val finds = bounds("records-chip-finds")
        assertEquals("row padding", 16f, (all.left - row.left).value, 0.5f)
        assertEquals("gap between chips", 8f, (finds.left - all.right).value, 0.5f)
        val icon = composeRule.onNode(hasTestTag(CHIP_ALL), useUnmergedTree = true).onChildren()[0].getUnclippedBoundsInRoot()
        assertEquals("leading icon", 24f, (icon.right - icon.left).value, 0.5f)
    }

    // ── L7: state survives turning between portrait and a short landscape window ──

    /**
     * The manifest handles orientation (`configChanges`, J0 A3), so a rotation is a configuration
     * change inside one composition, not a recreation: this swaps [LocalConfiguration] in place, as
     * `AvailabilityScreenShortLandscapeTest` does, and the rotation with it.
     */
    private var landscape by mutableStateOf(false)

    private fun setTurnableScreen(drafts: List<CartographyEntry> = emptyList()) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_0)
        composeRule.setContent {
            val base = androidx.compose.ui.platform.LocalConfiguration.current
            val configuration = if (!landscape) {
                base
            } else {
                android.content.res.Configuration(base).apply {
                    orientation = android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    screenWidthDp = 823
                    screenHeightDp = 384
                }
            }
            var cartography by androidx.compose.runtime.remember { mutableStateOf(CartographyUiState(entries = MANY_ENTRIES, draftEntries = drafts)) }
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalConfiguration provides configuration) {
                shortWindowScreen(
                    cartography = cartography,
                    onStart = { date -> startedEntries += date },
                    onOpen = { id -> cartography = cartography.copy(editingEntry = (cartography.entries + cartography.draftEntries).first { it.id == id }) },
                    onClose = { cartography = cartography.copy(editingEntry = null) },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun turn(toLandscape: Boolean) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(if (toLandscape) Surface.ROTATION_90 else Surface.ROTATION_0)
        landscape = toLandscape
        composeRule.waitForIdle()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `L7 the Journal's switch, Records chip and Entries view survive turning to short landscape and back`() {
        setTurnableScreen()
        openJournal()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        node(CHIP_WAYPOINTS).performClick()
        composeRule.waitForIdle()

        turn(toLandscape = true)
        node(COMPACT_NAVIGATION_RAIL_TAG).assertIsDisplayed()
        node(SWITCH_RECORDS).assertIsSelected()
        node(CHIP_WAYPOINTS).assertIsSelected()

        node(SWITCH_ENTRIES).performClick()
        composeRule.waitForIdle()
        node(VIEW_ALBUM).performClick()
        composeRule.waitForIdle()

        turn(toLandscape = false)
        assertTrue("portrait again: no rail", !exists(COMPACT_NAVIGATION_RAIL_TAG))
        node(SWITCH_ENTRIES).assertIsSelected()
        node(VIEW_ALBUM).assertIsOn()
        node(SWITCH_RECORDS).performClick()
        composeRule.waitForIdle()
        node(CHIP_WAYPOINTS).assertIsSelected()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `L7 an entry open in its editor stays open in its editor across the turn, both ways`() {
        setTurnableScreen(drafts = listOf(TWO_DRAFTS.first()))
        openJournal()
        node(DRAFTS_CONTINUE).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Your own account (optional)").assertIsDisplayed()

        turn(toLandscape = true)
        node(COMPACT_NAVIGATION_RAIL_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Your own account (optional)").assertIsDisplayed()

        turn(toLandscape = false)
        composeRule.onNodeWithText("Your own account (optional)").assertIsDisplayed()
    }
}

// ── fixtures ──

private const val SEARCH_ICON = "journal-short-search"
private const val SHORT_HEADER = "journal-short-header"
private const val NEW_ICON = "journal-short-new"
private const val PHOTO_ICON = "journal-short-photo"
private const val SWITCH = "journal-switch"
private const val SWITCH_ENTRIES = "journal-switch-entries"
private const val SWITCH_RECORDS = "journal-switch-records"
private const val ENTRIES_FAB = "entries-fab"
private const val VIEW_ALBUM = "entries-view-album"
private const val VIEW_TIMELINE = "entries-view-timeline"
private const val MENU_TAKE_PHOTO = "entries-fab-menu-take-photo"
private const val MENU_IMPORT = "entries-fab-menu-import"
private const val DRAFTS_BANNER = "entries-drafts-banner"
private const val DRAFTS_CHIP = "entries-drafts-chip"
private const val DRAFTS_LIST = "entries-drafts-list"
private const val DRAFTS_CONTINUE = "entries-drafts-continue"
private const val CHIP_WAYPOINTS = "records-chip-waypoints"
private const val CHIP_ALL = "records-chip-all"
private const val CHIP_OFFLINE = "records-chip-offline-maps"

private fun committedFind(id: String): com.zynergylabs.forager.app.domain.model.MushroomLogEntry =
    com.zynergylabs.forager.app.domain.model.MushroomLogEntry.draft(id = id, location = null, date = LocalDate.of(2026, 9, 20)).copy(isDraft = false)

private fun recordedTrack(id: String): com.zynergylabs.forager.app.domain.model.Track = com.zynergylabs.forager.app.domain.model.Track(
    id = id,
    name = null,
    startedAtEpochMillis = 1_758_200_000_000L,
    endedAtEpochMillis = 1_758_203_600_000L,
    points = emptyList(),
)
private const val CHIP_ROW = "records-filter-chip-row"
private const val ENTRIES_GRID = "entries-grid"
private const val RECORDS_ALL_LIST = "records-logbook-list"
private fun cardTag(id: String) = "entry-card-$id"

/** Three touches spread across a control: near its start edge, its centre, near its end edge, at differing heights. */
private val TOUCH_SAMPLES = listOf(Offset(0.15f, 0.3f), Offset(0.5f, 0.5f), Offset(0.85f, 0.7f))

private fun committed(id: String, date: LocalDate): CartographyEntry =
    CartographyEntry.draft(id = id, date = date, updatedAtEpochMillis = 0L).copy(isDraft = false)

/** Twenty entries with writing and a kept find, newest first: more than a short window holds. */
private val MANY_ENTRIES: List<CartographyEntry> = (20 downTo 1).map { day ->
    committed("sep-$day", LocalDate.of(2026, 9, day)).copy(
        text = "Walk on the $day\nsecond line",
        findDecisions = listOf(FindDecision(findId = "f$day", foundOn = LocalDate.of(2026, 9, day), ownIdentification = "C. formosus", hasPhotos = false, kept = true)),
    )
}

private val TWO_DRAFTS: List<CartographyEntry> = listOf(
    CartographyEntry.draft(id = "draft-a", date = LocalDate.of(2026, 9, 25), updatedAtEpochMillis = 2L),
    CartographyEntry.draft(id = "draft-b", date = LocalDate.of(2026, 9, 24), updatedAtEpochMillis = 1L),
)

private val SHORT_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

/** Enough waypoints that the All logbook scrolls in a short window. */
private val MANY_WAYPOINTS = (1..16).map { i ->
    com.zynergylabs.forager.app.domain.model.Waypoint(
        id = "w$i", lat = 45.0, lng = -122.0, altitude = null, name = "Pin $i", note = "",
        createdAtEpochMillis = 1_790_000_000_000L - i * 86_400_000L,
    )
}

@androidx.compose.runtime.Composable
private fun shortWindowScreen(
    cartography: CartographyUiState,
    onStart: (LocalDate) -> Unit,
    onOpen: (String) -> Unit,
    onClose: () -> Unit,
    onOpenCamera: (com.zynergylabs.forager.app.ui.log.InAppCameraTarget) -> Unit = {},
    logState: com.zynergylabs.forager.app.ui.log.MushroomLogUiState = com.zynergylabs.forager.app.ui.log.MushroomLogUiState(),
    tracks: List<com.zynergylabs.forager.app.domain.model.Track> = emptyList(),
) {
    AvailabilityScreen(
        uiState = AvailabilityUiState(),
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
        mapSlot = SHORT_STUB_MAP,
        cartographyUiState = cartography,
        onStartCartographyEntry = onStart,
        onOpenCartographyEntry = onOpen,
        onCloseCartographyEntry = onClose,
        waypoints = MANY_WAYPOINTS,
        onOpenCamera = onOpenCamera,
        logUiState = logState,
        tracks = tracks,
    )
}
