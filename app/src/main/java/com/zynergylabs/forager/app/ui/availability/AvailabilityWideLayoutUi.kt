package com.zynergylabs.forager.app.ui.availability

// Split-AvailabilityScreen Stage F: the wide layout (seam F), moved verbatim out of
// AvailabilityScreen.kt. Five blocks, lines of the file as of 3bd0efe: 2429-2435
// (PERMANENT_DRAWER_WIDTH), 2437-2491 (CombinedResultsPane, COMBINED_PANE_LIST_WIDTH), 2958-2988
// (MapModeToggle), 3089-3315 (MapTab) and 4837-4864 (ThreeWayActionDialog). Same package as Stages
// A to E, so every same-package reference resolves unchanged. Pure move: no signature, name or body
// changed. Two widenings, private -> internal: PERMANENT_DRAWER_WIDTH and CombinedResultsPane,
// whose callers stay in AvailabilityScreen.kt. MapTab composes the overlays in
// AvailabilityMapOverlaysUi.kt; no symbol left behind is reached from here. Seam F (the wide
// layout) was released by the owner for this split, as recorded in the Understory amendment merged
// in #130. Stage D left CombinedResultsPane behind because it composes MapTab, which was then held.

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.CentrePinLocationPicker
import com.zynergylabs.forager.app.ui.map.CentrePinLocationPickerOverlay
import com.zynergylabs.forager.app.ui.map.MIN_TOUCH_TARGET
import com.zynergylabs.forager.app.ui.map.MapFloatingIconButton
import com.zynergylabs.forager.app.ui.map.MapMode
import com.zynergylabs.forager.app.ui.map.MAPS_TAB_OVERLAYS
import com.zynergylabs.forager.app.ui.map.MapLayersControls
import com.zynergylabs.forager.app.ui.map.MapLayersSheet
import com.zynergylabs.forager.app.ui.map.MapLegendChip
import com.zynergylabs.forager.app.ui.map.layersButtonDescription
import com.zynergylabs.forager.app.ui.map.layers.COLOUR_FIELDS
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.mapLegendFor
import com.zynergylabs.forager.app.ui.map.MapOverlayContent
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.map.MapRenderMode
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.LocalDate

/**
 * Width of the always-visible drawer panel on medium+ windows — see [PermanentNavigationDrawer]'s
 * call site in [AvailabilityScreen]. 360dp is M3's standard navigation-drawer width and keeps this
 * panel's text (species chips, radius slider, trip list) around a comfortable line length rather
 * than stretching to fill whatever the window happens to be.
 */
internal val PERMANENT_DRAWER_WIDTH = 360.dp

/**
 * List and Map shown together rather than tab-switched — the M3 "reveal" pattern for medium+
 * windows (see [AvailabilityScreen]'s call site). [ListTab] keeps a fixed, readable width so it
 * doesn't stretch as the window grows; [MapTab] takes the rest, same as it does full-bleed at
 * compact width.
 */
@Composable
internal fun CombinedResultsPane(
    uiState: AvailabilityUiState,
    currentTime: CurrentTimeProvider,
    distanceUnit: DistanceUnit,
    mapSlot: MapSlot,
    renderMode: MapRenderMode,
    mapMode: MapMode,
    onMapModeSelected: (MapMode) -> Unit,
    onPlaceTripPin: (LatLng, LocalDate, String) -> Unit,
    onLogFindHere: (LatLng) -> Unit,
    breadcrumbPoints: List<LatLng>,
    waypoints: List<Waypoint>,
    onDropWaypoint: (LatLng, String) -> Unit,
    /** See [AvailabilityScreen]'s own `mapTaxonFilter`/`onViewSpeciesOnMap` — threaded to both tabs here. */
    taxonFilter: Long?,
    onClearTaxonFilter: () -> Unit,
    onViewOnMap: (Long) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Map layers L0b: the Layers sheet's choices and callbacks, the legend and the saved records the
     * Maps tab draws (see [MapLayersControls]). Defaulted so other callers are unchanged.
     */
    mapLayers: MapLayersControls = MapLayersControls(),
) {
    Row(modifier = modifier.fillMaxHeight()) {
        ListTab(
            uiState = uiState,
            currentTime = currentTime,
            distanceUnit = distanceUnit,
            onViewOnMap = onViewOnMap,
            modifier = Modifier.width(COMBINED_PANE_LIST_WIDTH).fillMaxHeight(),
        )
        VerticalDivider()
        MapTab(
            uiState = uiState,
            mapSlot = mapSlot,
            renderMode = renderMode,
            mapMode = mapMode,
            onMapModeSelected = onMapModeSelected,
            onPlaceTripPin = onPlaceTripPin,
            onLogFindHere = onLogFindHere,
            breadcrumbPoints = breadcrumbPoints,
            waypoints = waypoints,
            onDropWaypoint = onDropWaypoint,
            taxonFilter = taxonFilter,
            onClearTaxonFilter = onClearTaxonFilter,
            mapLayers = mapLayers,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

/** Same readable-width reasoning as [PERMANENT_DRAWER_WIDTH]; see [CombinedResultsPane]. */
private val COMBINED_PANE_LIST_WIDTH = 360.dp

/**
 * The quick-fire icon overlaid on the map's own top-right corner — not the app bar, not Settings —
 * because which [MapMode] the map is in is a during-the-walk decision made often, unlike anything
 * that used to live in Settings (deleted; see [MapMode]'s own doc comment for what superseded it).
 * A tap opens the Layers sheet (map layers L0b, B1; `MapLayersSheet`), which replaced the basemap-only
 * picker; a picker rather than instantly cycling modes, since there are three to
 * choose from instead of two — "toggle the two" stopped being the whole rule once Satellite joined
 * Street/Topographical.
 */
@Composable
private fun MapModeToggle(mapMode: MapMode, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // size(MIN_TOUCH_TARGET) rather than sizing this off the icon-plus-padding it draws: the icon
    // is 24dp and Spacing.sm padding is 8dp a side, which wrapped to a 40dp circle — under M3's
    // 48x48dp minimum touch target, a real miss found by auditing this file's tap targets against
    // that rule, not a hypothetical one.
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = modifier.size(MIN_TOUCH_TARGET),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Layers,
                contentDescription = layersButtonDescription(mapMode),
            )
        }
    }
}

/**
 * Owns the location-placing flow: [MapSlot] only reports where the camera currently is (see its
 * own doc comment on [MapSlot.onCameraIdle]), so the pending action and what it turns into both
 * live here, next to the only place that location can come from. The three-way menu's own button
 * offers three outcomes — plan a trip ([TripDatePickerDialog], turning it into a saved
 * [PlannedTrip]), log a find ([onLogFindHere], which opens the mushroom log's drawer destination
 * — see `docs/plans/mushroom-log.md`'s Navigation section for why this reuses the same entry point
 * rather than adding a second one), or drop a waypoint ([WaypointNameDialog]) — so
 * [ThreeWayActionDialog] asks which before any of the three runs, and only *then* does
 * [CentrePinLocationPicker] ask where: a button press carries no location the way a long-press
 * gesture used to, so which comes first had to invert. [defaultTripName] is computed from the trip
 * count already in state, here rather than inside the dialog, so the dialog stays a dumb presenter
 * of whatever default it's handed.
 */
@Composable
private fun MapTab(
    uiState: AvailabilityUiState,
    mapSlot: MapSlot,
    renderMode: MapRenderMode,
    mapMode: MapMode,
    onMapModeSelected: (MapMode) -> Unit,
    onPlaceTripPin: (LatLng, LocalDate, String) -> Unit,
    onLogFindHere: (LatLng) -> Unit,
    breadcrumbPoints: List<LatLng>,
    waypoints: List<Waypoint>,
    onDropWaypoint: (LatLng, String) -> Unit,
    /** See [AvailabilityScreen]'s own `mapTaxonFilter` doc comment — "View on Map" from a List-tab row. */
    taxonFilter: Long?,
    onClearTaxonFilter: () -> Unit,
    mapLayers: MapLayersControls,
    modifier: Modifier = Modifier,
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingMapAction?>(null) }
    var pendingTripLocation by remember { mutableStateOf<LatLng?>(null) }
    var pendingWaypointLocation by remember { mutableStateOf<LatLng?>(null) }

    when {
        !uiState.hasSearched -> MapMessage(
            "Choose a region in search options to see mapped sightings.",
            modifier = modifier,
        )

        uiState.isLoadingSightings -> Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }

        uiState.sightingsErrorMessage != null -> MapMessage(
            uiState.sightingsErrorMessage,
            modifier = modifier,
            color = MaterialTheme.colorScheme.error,
        )

        else -> {
            val region = uiState.region
            if (region != null) {
                var cameraCenter by remember(region) { mutableStateOf(LatLng(region.lat, region.lng)) }
                var tappedSighting by remember { mutableStateOf<Sighting?>(null) }
                var tappedSightingScreenPosition by remember { mutableStateOf(Offset.Zero) }
                var tappedSightingBearingDeg by remember { mutableStateOf(0f) }
                val context = LocalContext.current
                Column(modifier = modifier.fillMaxWidth()) {
                    // "View on Map" from a List-tab row: limits the map to one species' sightings
                    // rather than every mapped one. Filtered against uiState.sightings itself
                    // (what the map actually draws), not against uiState.forecast's own
                    // observationCount — the two can legitimately disagree (the forecast is a
                    // separate historical query; the map only shows what actually loaded for this
                    // region), so the count in mapTaxonFilterLabel below is what's really on screen.
                    val filteredSightings = if (taxonFilter != null) {
                        uiState.sightings.filter { it.taxonId == taxonFilter }
                    } else {
                        uiState.sightings
                    }
                    val mapTaxonFilterLabel = taxonFilter?.let { id ->
                        val name = uiState.forecast?.entries?.firstOrNull { it.species.taxonId == id }?.species
                            ?.let { it.commonName ?: it.scientificName }
                            ?: filteredSightings.firstOrNull()?.let { it.commonName ?: it.scientificName }
                            ?: "this species"
                        "$name (${filteredSightings.size})"
                    }
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        mapSlot(
                            region,
                            MapOverlayContent(
                                sightings = filteredSightings,
                                plannedTrips = uiState.plannedTrips,
                                breadcrumbPoints = breadcrumbPoints,
                                waypoints = waypoints,
                                focusedObservationId = tappedSighting?.observationId,
                                // Map layers L0b, B2: every saved record, as on the compact Maps tab.
                                keptTrackPolylines = mapLayers.records.trackPolylines,
                                findMarkers = mapLayers.records.findMarkers,
                                photoMarkers = mapLayers.records.photoMarkers,
                                offlineRegionCircles = mapLayers.records.offlineRegionCircles,
                            ),
                            renderMode,
                            null,
                            {},
                            // A plain tap elsewhere on the map is what dismisses the bubble below —
                            // see ObservationBubble's own doc comment for why this replaced a modal
                            // AlertDialog. Harmless to clear when nothing is showing.
                            { tappedSighting = null },
                            { sighting, screenPosition, bearingDeg ->
                                tappedSighting = sighting
                                tappedSightingScreenPosition = screenPosition
                                tappedSightingBearingDeg = bearingDeg
                            },
                            { location -> cameraCenter = location },
                            Modifier.fillMaxSize(),
                        )
                        tappedSighting?.let { sighting ->
                            AnchoredAtScreenPoint(
                                anchorPx = tappedSightingScreenPosition,
                                bearingDeg = tappedSightingBearingDeg,
                                modifier = Modifier.fillMaxSize(),
                            ) { arrowAngleDeg ->
                                ObservationBubble(
                                    sighting = sighting,
                                    onViewOnINaturalist = {
                                        launchINaturalistObservation(context, sighting.observationId)
                                        tappedSighting = null
                                    },
                                    onDismiss = { tappedSighting = null },
                                    arrowAngleDeg = arrowAngleDeg,
                                )
                            }
                        }
                        mapTaxonFilterLabel?.let { label ->
                            TaxonMapFilterChip(
                                label = label,
                                onClear = onClearTaxonFilter,
                                modifier = Modifier.align(Alignment.TopCenter).padding(Spacing.sm),
                            )
                        }
                        MapModeToggle(
                            mapMode = mapMode,
                            onClick = { showLayersSheet = true },
                            modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.sm),
                        )
                        if (showLayersSheet) {
                            MapLayersSheet(
                                mapMode = mapMode,
                                onMapModeSelected = onMapModeSelected,
                                overlays = MAPS_TAB_OVERLAYS,
                                colourFields = mapLayers.listedColourFields,
                                state = mapLayers.stored,
                                onVisibilityChanged = mapLayers.onVisibilityChanged,
                                onOpacityChanged = mapLayers.onOpacityChanged,
                                onColourFieldMoved = mapLayers.onColourFieldMoved,
                                onDismiss = { showLayersSheet = false },
                            )
                        }
                        // Map layers L0b, B4 (owner's ruling 5): the legend chip in the bottom-right
                        // corner, stacked above the add button below, its right edge on the button's.
                        mapLegendFor(renderMode.layers, MAP_LAYER_REGISTRY, COLOUR_FIELDS, mapLayers.cellsShown)?.let { legend ->
                            MapLegendChip(
                                legend = legend,
                                expanded = mapLayers.legendExpanded,
                                onExpandedChange = mapLayers.onLegendExpandedChange,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = Spacing.sm, bottom = Spacing.sm + MIN_TOUCH_TARGET + Spacing.sm),
                            )
                        }
                        // MEDIUM/EXPANDED's own trigger for the three-way menu — CompactMapTab has
                        // MapIconBar's own add row to repurpose for this; this window class has no
                        // icon bar at all, so this is new here rather than reused. Same icon, same
                        // content description, same corner as the compact bar's own add row, for
                        // the same button to mean the same thing on every layout that has one — see
                        // this file's doc comment on why medium/expanded needed a button added
                        // rather than an existing one converted.
                        MapFloatingIconButton(
                            icon = Icons.Filled.Add,
                            contentDescription = "Plan a trip or log a find here",
                            onClick = { showActionMenu = true },
                            filled = true,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.sm),
                        )
                        if (pendingAction != null) {
                            CentrePinLocationPickerOverlay(
                                onConfirm = {
                                    when (pendingAction) {
                                        PendingMapAction.PLAN_TRIP -> pendingTripLocation = cameraCenter
                                        PendingMapAction.LOG_FIND -> onLogFindHere(cameraCenter)
                                        PendingMapAction.DROP_WAYPOINT -> pendingWaypointLocation = cameraCenter
                                        null -> Unit
                                    }
                                    pendingAction = null
                                },
                                onCancel = { pendingAction = null },
                                modifier = Modifier.fillMaxSize(),
                                // The map's own night (the renderMode handed to mapSlot above), so
                                // the pin follows Night Maps (colour build C2 (e)).
                                night = renderMode.night,
                            )
                        }
                    }
                }
            }
        }
    }

    // CentrePinLocationPickerOverlay is a plain overlay, not a real Dialog — unlike
    // ThreeWayActionDialog below (an AlertDialog, which already handles system back for free) —
    // so unlike this composable's menu, its own picker phase needs an explicit BackHandler or
    // system back would fall straight through it.
    BackHandler(enabled = pendingAction != null) {
        pendingAction = null
    }

    if (showActionMenu) {
        ThreeWayActionDialog(
            onPlanTrip = {
                showActionMenu = false
                pendingAction = PendingMapAction.PLAN_TRIP
            },
            onLogFind = {
                showActionMenu = false
                pendingAction = PendingMapAction.LOG_FIND
            },
            onDropWaypoint = {
                showActionMenu = false
                pendingAction = PendingMapAction.DROP_WAYPOINT
            },
            onDismiss = { showActionMenu = false },
        )
    }

    pendingTripLocation?.let { location ->
        TripDatePickerDialog(
            defaultName = defaultTripName(uiState.plannedTrips.size),
            onConfirm = { date, name ->
                onPlaceTripPin(location, date, name)
                pendingTripLocation = null
            },
            onDismiss = { pendingTripLocation = null },
        )
    }

    pendingWaypointLocation?.let { location ->
        WaypointNameDialog(
            defaultName = defaultWaypointName(waypoints.size),
            onConfirm = { name ->
                onDropWaypoint(location, name)
                pendingWaypointLocation = null
            },
            onDismiss = { pendingWaypointLocation = null },
        )
    }
}

/**
 * What the three-way menu button means — asked before any of [TripDatePickerDialog],
 * `onLogFindHere`, or [WaypointNameDialog] run, and before [CentrePinLocationPicker] even shows;
 * see [MapTab]'s own doc comment. Named for what it asks, not for the gesture that used to trigger
 * it — nothing here is long-press-specific any more, see decision 5 in
 * `docs/plans/pr26-rework.md`'s Workstream L.
 */
@Composable
private fun ThreeWayActionDialog(
    onPlanTrip: () -> Unit,
    onLogFind: () -> Unit,
    onDropWaypoint: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What would you like to do here?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TextButton(onClick = onPlanTrip, modifier = Modifier.fillMaxWidth()) { Text("Plan a trip") }
                TextButton(onClick = onLogFind, modifier = Modifier.fillMaxWidth()) { Text("Log a find") }
                TextButton(onClick = onDropWaypoint, modifier = Modifier.fillMaxWidth()) { Text("Drop a waypoint") }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
