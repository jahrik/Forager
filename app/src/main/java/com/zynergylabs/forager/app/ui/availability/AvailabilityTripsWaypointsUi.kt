package com.zynergylabs.forager.app.ui.availability

// Split-AvailabilityScreen Stage F: trips and waypoints, moved verbatim out of
// AvailabilityScreen.kt. One block, lines 5116-5343 of the file as of 3bd0efe: TripPlannerSection,
// PlannedTripsList, PlannedTripRow, WaypointsSection, WaypointRow. Same package as Stages A to E,
// so every same-package reference resolves unchanged. Pure move: no signature, name or body
// changed. No widening: TripPlannerSection and WaypointsSection were already internal. No symbol
// left behind is reached from here. Seam F (the wide layout) was released by the owner for this
// split, as recorded in the Understory amendment merged in #130.

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.zynergylabs.forager.app.domain.MgrsConverter
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.MgrsCoordinate
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.LocalDate

/**
 * The Trip Planner drawer section's content: the planned-trips list (see [PlannedTripsList])
 * above the existing rain-driven trip windows, gated on a search the same way [MapTab] is —
 * before any region is chosen there is no rainfall history to plan from, so this says that rather
 * than rendering an empty [TripWindowsCard]. The planned-trips list has no such gate: it lists
 * absolute map points the user placed, independent of any search.
 */
@Composable
internal fun TripPlannerSection(uiState: AvailabilityUiState, onDeletePlannedTrip: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        PlannedTripsList(plannedTrips = uiState.plannedTrips, onDeletePlannedTrip = onDeletePlannedTrip)
        HorizontalDivider()
        if (uiState.hasSearched) {
            TripWindowsCard(uiState = uiState)
        } else {
            Text(
                "Choose a region in search options to see rain-driven trip windows.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * The list of trips the user has placed on the map, sorted by
 * [GetPlannedTripsUseCase][com.zynergylabs.forager.app.domain.GetPlannedTripsUseCase] with any dated today
 * already moved to the front — this composable renders that order, it doesn't recompute it.
 */
@Composable
private fun PlannedTripsList(plannedTrips: List<PlannedTrip>, onDeletePlannedTrip: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Planned Trips", style = MaterialTheme.typography.titleSmall)
        if (plannedTrips.isEmpty()) {
            Text(
                "No trips planned yet. Tap the add button on the map to plan one.",
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            val today = LocalDate.now()
            plannedTrips.forEach { trip ->
                PlannedTripRow(
                    trip = trip,
                    isToday = trip.date == today,
                    onDelete = { onDeletePlannedTrip(trip.id) },
                )
            }
        }
    }
}

/**
 * One planned trip: its user-chosen [PlannedTrip.name] as the primary identifying text (more
 * prominent than the coordinates below it, per the user's own framing — the name is what a
 * person recognizes a trip by, the coordinates are supporting detail), its MGRS grid reference
 * (the format asked for on this screen; see [MgrsConverter]) with decimal degrees kept alongside
 * since some readers still want them and MGRS can't cover every point (see
 * [MgrsCoordinate.Unsupported]), a "Directions" action that hands the location to whatever
 * navigation app is installed (see [launchDirections]), and delete.
 */
@Composable
private fun PlannedTripRow(trip: PlannedTrip, isToday: Boolean, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isToday) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isToday) {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(trip.name, style = MaterialTheme.typography.titleSmall)
                Text(TRIP_WINDOW_DATE_FORMAT.format(trip.date), style = MaterialTheme.typography.bodyMedium)
                when (val mgrs = MgrsConverter.convert(trip.location)) {
                    is MgrsCoordinate.Grid -> Text(mgrs.value, style = MaterialTheme.typography.bodySmall)
                    // No line at all rather than a wrong or truncated one — see MgrsCoordinate's
                    // own doc comment. The decimal-degrees line below still locates the trip.
                    is MgrsCoordinate.Unsupported -> Unit
                }
                Text(
                    "${"%.4f".format(trip.location.lat)}, ${"%.4f".format(trip.location.lng)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = { launchDirections(context, trip) }) {
                Icon(Icons.Filled.Directions, contentDescription = "Directions to ${trip.name}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove planned trip for ${trip.date}")
            }
        }
    }
}

/**
 * The saved-waypoints drawer section — same "list plus delete" shape as [PlannedTripsList], the
 * one existing precedent for a user-placed-point list in this drawer. Unlike planned trips,
 * waypoints have no date to sort by, so they're shown newest-first (the order
 * [com.zynergylabs.forager.app.domain.GetWaypointsUseCase] already returns — see that use case for why).
 *
 * [errorMessage] takes priority over the list, same branch order [TripWindowsCard] uses for
 * [AvailabilityUiState.tripWindowsErrorMessage] — a failed load/add/remove is belief-changing (the
 * list on screen may not be what's actually saved), so it replaces the list rather than sitting
 * beside it.
 *
 * Scrolls itself ([Modifier.verticalScroll]) rather than depending on a scrollable caller — same
 * reasoning as [OfflineMapsPanel]'s own outer `Column`. Journal restructure Stage 1 moved this out
 * of the Tools drawer's `SearchControls`, which supplied that scroll, into [com.zynergylabs.forager.app.ui.log.RecordsTab]'s
 * flat `Column(fillMaxSize())`, which does not — this is now `WaypointsSection`'s only caller.
 */
@Composable
internal fun WaypointsSection(
    waypoints: List<Waypoint>,
    errorMessage: String?,
    onDeleteWaypoint: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** How many Cartography entries currently keep a reference to each waypoint (by id) — Journal Stage 2b's 4b deletion warning, shown in the confirm dialog below. */
    entryReferenceCounts: Map<String, Int> = emptyMap(),
) {
    // Journal Stage 2b, 4b: this section had no delete confirmation at all before — every other
    // per-row delete in this drawer (OfflineRegionsSection, PlannedTripsList) already confirms
    // first, and a deletion warning needs somewhere to show itself. Same pendingDelete-then-dialog
    // shape as OfflineRegionsSection.
    var pendingDeleteWaypoint by remember { mutableStateOf<Waypoint?>(null) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        when {
            errorMessage != null -> Text(
                errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )

            waypoints.isEmpty() -> Text(
                "No waypoints dropped yet. Tap the add button on the map to drop one.",
                style = MaterialTheme.typography.bodySmall,
            )

            else -> waypoints.forEach { waypoint ->
                WaypointRow(waypoint = waypoint, onDelete = { pendingDeleteWaypoint = waypoint })
            }
        }
    }

    pendingDeleteWaypoint?.let { waypoint ->
        WaypointDeleteDialog(
            waypoint = waypoint,
            entryReferenceCounts = entryReferenceCounts,
            onDeleteWaypoint = onDeleteWaypoint,
            onDismiss = { pendingDeleteWaypoint = null },
        )
    }
}

/**
 * [WaypointsSection]'s delete confirmation, extracted unchanged (journal redesign J1, S4) so the
 * Journal's All logbook, which shows the same [WaypointRow], confirms a delete with the same dialog —
 * "deletes stay as they are" in that stage.
 */
@Composable
internal fun WaypointDeleteDialog(
    waypoint: Waypoint,
    entryReferenceCounts: Map<String, Int>,
    onDeleteWaypoint: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val referencingEntryCount = entryReferenceCounts[waypoint.id] ?: 0
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete \"${waypoint.name}\"?") },
        text = {
            Text(
                // No permanence claim — see OfflineRegionsSection's identical dialog for why.
                if (referencingEntryCount > 0) {
                    "This waypoint appears in $referencingEntryCount ${if (referencingEntryCount == 1) "journal entry" else "journal entries"}."
                } else {
                    "Delete this waypoint?"
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDeleteWaypoint(waypoint.id)
                    onDismiss()
                },
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = { onDismiss() }) { Text("Cancel") } },
    )
}

/**
 * One saved waypoint: its user-chosen [Waypoint.name] as the primary identifying text, the same
 * MGRS-plus-decimal-degrees coordinate display [PlannedTripRow] uses, a "Directions" action
 * ([launchDirections]) reusing the exact same `geo:` intent machinery, and delete.
 */
@Composable
internal fun WaypointRow(waypoint: Waypoint, onDelete: () -> Unit) {
    val context = LocalContext.current
    val location = LatLng(waypoint.lat, waypoint.lng)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(waypoint.name, style = MaterialTheme.typography.titleSmall)
                when (val mgrs = MgrsConverter.convert(location)) {
                    is MgrsCoordinate.Grid -> Text(mgrs.value, style = MaterialTheme.typography.bodySmall)
                    // No line at all rather than a wrong or truncated one — see PlannedTripRow's
                    // own use of the same MgrsCoordinate branch for why.
                    is MgrsCoordinate.Unsupported -> Unit
                }
                Text(
                    "${"%.4f".format(waypoint.lat)}, ${"%.4f".format(waypoint.lng)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = { launchDirections(context, waypoint.name, location) }) {
                Icon(Icons.Filled.Directions, contentDescription = "Directions to ${waypoint.name}")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove waypoint ${waypoint.name}")
            }
        }
    }
}
