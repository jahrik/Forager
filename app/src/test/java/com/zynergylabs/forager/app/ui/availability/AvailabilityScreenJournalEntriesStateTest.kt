package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Journal redesign J2 (`prompts/preserved/2026-09-27-18.md`), driven through the real
 * [AvailabilityScreen] because the bottom nav is what a user leaves the Journal with and what Back
 * finally falls through to:
 *
 * - T3: the Entries view (timeline or album) lives in `JournalScreenState` (plan J10), so it
 *   survives leaving the Journal tab and a saved-instance-state round trip; Back from the album
 *   steps to the timeline before it leaves the Journal.
 *
 * Recreation uses [StateRestorationTester], as `AvailabilityScreenJournalStateTest` (J1) does; see
 * that class for why it stands in for an Activity recreation. Bottom-nav and toggle taps here are
 * semantic clicks: these tests are about where state lives, not about touch routing, which
 * `JournalEntriesTest` covers with coordinate touches.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class AvailabilityScreenJournalEntriesStateTest {

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

    private fun openBottomTab(label: String) {
        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()
    }

    private fun pressBack() {
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
    }

    private fun chooseAlbum() {
        openBottomTab("Journal")
        composeRule.onNodeWithTag(VIEW_ALBUM).performClick()
        composeRule.waitForIdle()
        assertAlbumShowing()
    }

    private fun assertAlbumShowing() {
        composeRule.onNodeWithTag(VIEW_ALBUM).assertIsOn()
        composeRule.onNodeWithTag(VIEW_TIMELINE).assertIsOff()
        composeRule.onNodeWithTag(ALBUM).assertIsDisplayed()
    }

    // ── T3 ──

    @Test
    fun `the album view survives leaving the Journal tab and returning`() {
        composeRule.setContent { entriesStateScreen() }
        chooseAlbum()

        openBottomTab("List")
        openBottomTab("Journal")

        assertAlbumShowing()
    }

    @Test
    fun `the album view survives a saved-instance-state round trip`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { entriesStateScreen() }
        chooseAlbum()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        // Before T5 the bottom tab itself resets to Maps on a restore; tapping Journal again is
        // harmless once it does not.
        openBottomTab("Journal")

        assertAlbumShowing()
    }

    @Test
    fun `Back from the album returns to the timeline, and Back again leaves the Journal`() {
        composeRule.setContent { entriesStateScreen() }
        chooseAlbum()

        pressBack()

        composeRule.onNodeWithTag(VIEW_TIMELINE).assertIsOn()
        composeRule.onNodeWithTag(ALBUM).assertDoesNotExist()
        composeRule.onNodeWithTag(ENTRIES_STATE_MAP_TAG).assertDoesNotExist()

        pressBack()

        composeRule.onNodeWithTag(ENTRIES_STATE_MAP_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(VIEW_TIMELINE).assertDoesNotExist()
    }
}

private const val VIEW_TIMELINE = "entries-view-timeline"
private const val VIEW_ALBUM = "entries-view-album"
private const val ALBUM = "entries-album"
private const val ENTRIES_STATE_MAP_TAG = "entries-state-map-slot"

private val ENTRIES_STATE_STUB_MAP: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier.testTag(ENTRIES_STATE_MAP_TAG)) }

@androidx.compose.runtime.Composable
private fun entriesStateScreen() {
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
        mapSlot = ENTRIES_STATE_STUB_MAP,
    )
}
