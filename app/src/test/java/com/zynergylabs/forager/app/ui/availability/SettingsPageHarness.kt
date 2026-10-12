package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.ui.map.MapSlot
import org.junit.rules.ExternalResource
import org.robolectric.Shadows

/**
 * The Settings page reached the way a person reaches it: [AvailabilityScreen], then "Tools", then
 * "Settings" (the same two taps `AvailabilityScreenSettingsPanelTest.openSettings` makes). The map
 * is a stub, as in that class: composing the real one starts the map library.
 */
internal object SettingsPageHarness {

    /** Robolectric needs the host activity declared before the compose rule launches it. */
    fun declareHostActivity(): ExternalResource = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    private val stubMap: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

    fun openSettings(composeRule: AndroidComposeTestRule<*, ComponentActivity>) {
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(region = Region(lat = 45.0, lng = -122.0, radiusKm = 15)),
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
                mapSlot = stubMap,
            )
        }
        composeRule.onNodeWithText("Tools").performClick()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForIdle()
    }

    /** One of Settings' own rows: the check that the page showing is Settings. */
    const val SETTINGS_CONTROL_ROW = "Night Maps"
}
