package com.zynergylabs.forager.app.ui.availability

// Data part C (dispatch -668), under the owner's "Piece by piece (Recommended)" (RECORD -655): the
// trip windows card, moved out of AvailabilityResultsUi.kt before this part changed it. Same package,
// so its caller (TripPlannerSection) and AvailabilityPureFunctions.kt's TRIP_WINDOW_DATE_FORMAT resolve
// unchanged. The move itself changed no line of it.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.ForagingSelection
import com.zynergylabs.forager.app.domain.ForagingWeatherGuidance
import com.zynergylabs.forager.app.domain.model.UnitSystem
import com.zynergylabs.forager.app.domain.model.formatRainfall
import com.zynergylabs.forager.app.domain.model.formatSoilTemperature
import com.zynergylabs.forager.app.domain.model.TripWindow
import com.zynergylabs.forager.app.domain.model.TripWindowReport
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.format.DateTimeFormatter


internal val TRIP_WINDOW_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

/**
 * Upcoming days that sit inside the stated post-rain lag range, next to the group's general
 * weather pattern.
 *
 * Two owned domain objects meet here and stay visually distinct: [TripWindowReport] is
 * measurements and date arithmetic only (see its own doc comment for why it must never grow a
 * score), and [ForagingWeatherGuidance] is the separately-stated rule of thumb that makes those
 * measurements interesting. The card shows both but never blends them into one sentence.
 *
 * Unlike [ConditionsCard], not gated to the browsed month: the days ahead of today are relevant
 * to planning a trip this week regardless of which month's species ranking is on screen.
 */
@Composable
internal fun TripWindowsCard(uiState: AvailabilityUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("Trip Windows", style = MaterialTheme.typography.titleSmall)

            when {
                uiState.isLoadingTripWindows -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )

                uiState.tripWindowsErrorMessage != null -> Text(
                    uiState.tripWindowsErrorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )

                uiState.tripWindowReport != null -> TripWindowReportContent(uiState.tripWindowReport, uiState.unitSystem)
            }

            HorizontalDivider()
            ForagingWeatherGuidanceSection(uiState.foragingSelection, uiState.unitSystem)
        }
    }
}

@Composable
private fun TripWindowReportContent(report: TripWindowReport, unitSystem: UnitSystem) {
    if (report.windows.isEmpty()) {
        Text(noTripWindowMessage(report, unitSystem), style = MaterialTheme.typography.bodySmall)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        report.windows.forEach { window -> TripWindowRow(window, unitSystem) }
    }
}

@Composable
private fun TripWindowRow(window: TripWindow, unitSystem: UnitSystem) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            "${TRIP_WINDOW_DATE_FORMAT.format(window.startDate)} – ${TRIP_WINDOW_DATE_FORMAT.format(window.endDate)}",
            style = MaterialTheme.typography.bodyMedium,
        )
        val mostRecentRain = window.precedingRainEvents.first()
        Text(
            "${window.daysAfterMostRecentRainAtStart}–${window.daysAfterMostRecentRainAtEnd} days after " +
                "${formatRainfall(mostRecentRain.totalMm, unitSystem, metricDecimals = 0)} of rain ending " +
                TRIP_WINDOW_DATE_FORMAT.format(mostRecentRain.endDate) +
                if (mostRecentRain.isForecast) " (forecast)" else "",
            style = MaterialTheme.typography.bodySmall,
        )
        if (window.precipitationDuringWindowMm > 0.0) {
            Text(
                "${formatRainfall(window.precipitationDuringWindowMm, unitSystem)} more rain forecast during the window",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        window.meanShallowSoilMoistureM3M3?.let { moisture ->
            Text(
                "Shallow soil moisture: ${"%.2f".format(moisture)} m³/m³",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        window.meanSoilTemperatureC?.let { temp ->
            Text("Soil temperature: ${formatSoilTemperature(temp, unitSystem)}", style = MaterialTheme.typography.bodySmall)
        }
        window.evapotranspirationSinceRainMm?.let { et0 ->
            Text(
                "${formatRainfall(et0, unitSystem)} evapotranspiration since the rain",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * The general weather pattern for the current selection, stated as a rule of thumb next to the
 * measurements above it — never combined with them into a score. See
 * [ForagingWeatherGuidance]'s doc comment for the rules this enforces.
 */
@Composable
private fun ForagingWeatherGuidanceSection(selection: ForagingSelection, unitSystem: UnitSystem) {
    val guidance = ForagingWeatherGuidance.forSelection(selection, unitSystem)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        // labelMedium + a muted color, not titleSmall/labelLarge: Material3 sizes titleSmall and
        // labelLarge identically (14sp/500), so this heading and the card's own "Trip Windows"
        // title above it were reading as the same weight despite one being nested inside the
        // other. This is deliberately a step down from the card title, not a second one beside it.
        Text(
            guidance.heading,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        guidance.paragraphs.forEach { paragraph ->
            Text(paragraph, style = MaterialTheme.typography.bodySmall)
        }
        guidance.speciesDataCaveat?.let { caveat ->
            Text(caveat, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
        }
    }
}
