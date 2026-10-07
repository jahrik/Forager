package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.ComputeTrackStatisticsUseCase
import com.zynergylabs.forager.app.domain.MgrsConverter
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.isOfflineRegionStale
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.MgrsCoordinate
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.formatDistanceKm
import com.zynergylabs.forager.app.domain.model.formatDistanceMeters
import com.zynergylabs.forager.app.domain.networkFixExclusionNote
import com.zynergylabs.forager.app.ui.theme.navigationBarContainerColor
import com.zynergylabs.forager.app.ui.availability.decimalDegreesLabel
import com.zynergylabs.forager.app.ui.availability.launchDirections
import com.zynergylabs.forager.app.ui.availability.offlineRegionSizeLabel
import com.zynergylabs.forager.app.ui.availability.offlineRegionZoomNote
import com.zynergylabs.forager.app.ui.availability.relativeTimeLabel
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.map.MapChromeSheetNavigationBar
import com.zynergylabs.forager.app.ui.map.mapChromeFill
import com.zynergylabs.forager.app.ui.map.mapChromeContentColor
import com.zynergylabs.forager.app.ui.theme.Spacing
import com.zynergylabs.forager.app.ui.track.formatRecordTimestamp
import com.zynergylabs.forager.app.ui.track.canBeDeleted
import com.zynergylabs.forager.app.ui.track.shareTrackGpx
import com.zynergylabs.forager.app.ui.track.trackTitle
import kotlinx.coroutines.launch

/**
 * Which Records row's details sheet is open (journal redesign J5c; the owner: "have them display info
 * upon tapping", form "Bottom sheet of details (Recommended)"). A waypoint, a recorded track or an
 * offline region, by id. Finds are not here: a find tile already opens the find's report.
 *
 * Held by [RecordsTab] in `rememberSaveable` state through [RecordDetailsTargetSaver], so an open
 * sheet survives a rotation (which is not a recreation here, J5 L7) and an Activity recreation.
 * Leaving the Journal tab with the sheet open is not possible: the sheet is modal, so its scrim
 * covers the bottom bar and the rail. Its Navigate (dispatch 2026-09-28-502) closes it before the
 * Maps tab comes up, and Back from that navigation opens it again by request.
 */
internal sealed interface RecordDetailsTarget {
    /**
     * [fromTrackId] (dispatch -616 as amended by -618): the walk whose details this waypoint was opened
     * from, through the "Waypoints on this track" list; Back returns there ([returnsTo]). `null` for a
     * waypoint opened any other way, which Back closes as before.
     */
    data class WaypointDetails(val id: String, val fromTrackId: String? = null) : RecordDetailsTarget
    data class TrackDetails(val id: String) : RecordDetailsTarget
    data class OfflineRegionDetails(val id: Long) : RecordDetailsTarget
}

/**
 * Where Back on this target's sheet goes (dispatch -616 as amended by -618; the owner's "Back retraces the
 * way in"): a waypoint opened from its walk's list returns to that walk's details; every other target has
 * nowhere to return to, and Back closes the sheet.
 */
internal fun RecordDetailsTarget.returnsTo(): RecordDetailsTarget? =
    (this as? RecordDetailsTarget.WaypointDetails)?.fromTrackId?.let { RecordDetailsTarget.TrackDetails(it) }

/**
 * Saves a [RecordDetailsTarget] as one string, `waypoint:<id>`, `track:<id>` or `region:<id>` (and, since dispatch -616,
 * `walk-waypoint:<track id><U+001F><id>` for a waypoint opened from its walk). A
 * string that is none of these fails loudly on restore rather than quietly opening nothing, the
 * same choice [JournalScreenState]'s saver makes with `valueOf` and `toBooleanStrict`.
 */
internal val RecordDetailsTargetSaver: Saver<RecordDetailsTarget?, String> = Saver(
    save = { target ->
        when (target) {
            null -> ""
            // Dispatch -616: a waypoint opened from its walk keeps the walk, so Back still returns there after a recreation.
            is RecordDetailsTarget.WaypointDetails -> target.fromTrackId?.let { "$WALK_WAYPOINT_KEY$it$WALK_WAYPOINT_SEPARATOR${target.id}" }
                ?: "$WAYPOINT_KEY${target.id}"
            is RecordDetailsTarget.TrackDetails -> "$TRACK_KEY${target.id}"
            is RecordDetailsTarget.OfflineRegionDetails -> "$REGION_KEY${target.id}"
        }
    },
    restore = { saved ->
        when {
            saved.isEmpty() -> null
            saved.startsWith(WALK_WAYPOINT_KEY) -> saved.removePrefix(WALK_WAYPOINT_KEY).let { rest ->
                val at = rest.indexOf(WALK_WAYPOINT_SEPARATOR)
                if (at < 0) error("Not a saved record-details target: '$saved'")
                RecordDetailsTarget.WaypointDetails(id = rest.substring(at + 1), fromTrackId = rest.substring(0, at))
            }
            saved.startsWith(WAYPOINT_KEY) -> RecordDetailsTarget.WaypointDetails(saved.removePrefix(WAYPOINT_KEY))
            saved.startsWith(TRACK_KEY) -> RecordDetailsTarget.TrackDetails(saved.removePrefix(TRACK_KEY))
            saved.startsWith(REGION_KEY) -> RecordDetailsTarget.OfflineRegionDetails(saved.removePrefix(REGION_KEY).toLong())
            else -> error("Not a saved record-details target: '$saved'")
        }
    },
)

private const val WAYPOINT_KEY = "waypoint:"
private const val TRACK_KEY = "track:"
private const val REGION_KEY = "region:"
private const val WALK_WAYPOINT_KEY = "walk-waypoint:"

/** Between the walk's id and the waypoint's: a control character, so neither id (a UUID, or anything with colons) can contain it. */
private const val WALK_WAYPOINT_SEPARATOR = '\u001F'

/**
 * What a Records row does on a tap once J5c has given it one: open [onClick], announced to TalkBack
 * as "Details for <name>" ([recordDetailsClickLabel]). One modifier for every row type so the three
 * rows cannot drift apart in how they take the tap.
 *
 * A row inside a J4b [TwoStageSwipeRow] needs nothing more for J4b's rule ("a tap on an open row
 * closes it and does not open it"): while the row is open, that component lays an overlay over its
 * content which takes the tap and closes the row, so this click never sees it. The row's own
 * buttons (Directions, Share) are children of the clickable node and take their own taps first.
 */
internal fun Modifier.opensRecordDetails(name: String, onClick: () -> Unit): Modifier =
    clickable(onClickLabel = recordDetailsClickLabel(name), onClick = onClick)

/** The click label a Records row carries for TalkBack: "Details for Creek pin". */
internal fun recordDetailsClickLabel(name: String): String = "Details for $name"

/**
 * The details sheet for one Records row (journal redesign J5c): a Material 3 [ModalBottomSheet] with
 * the record's full information and its actions. Back and a scrim tap dismiss it (the sheet's own
 * behaviour, through [onDismiss]).
 *
 * **Everything shown is already in memory where the row is** (the dispatch's "no new query, column
 * or migration"): the lists [RecordsTab] was handed, the waypoints' reference counts it was handed,
 * and figures derived from them in the same way their rows or cards already derive them. A target
 * whose record is no longer in those lists (a delete landed, a list reloaded) closes the sheet.
 *
 * **Opened fully expanded** (`skipPartiallyExpanded`), so every field shows without a drag in
 * portrait; in a short landscape window the sheet takes the height it has and its content scrolls.
 *
 * **Waypoint: Navigate, then Directions** (dispatch 2026-09-28-502, plan task T9). The owner's M1 ruling
 * asked for Navigate (the app's own HUD) beside Directions; J5c shipped Directions only, because nothing
 * then navigated to a chosen waypoint. [onNavigateToWaypoint] is that entry point now: the sheet closes
 * and the Maps tab navigates to the waypoint, and Back opens this sheet again (Amendment 1). A host that
 * passes none (an entry map's) offers Directions only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordDetailsSheet(
    target: RecordDetailsTarget,
    waypoints: List<Waypoint>,
    tracks: List<Track>,
    offlineRegions: List<OfflineRegionSummary>,
    /** `RecordsTab`'s `waypointEntryReferenceCounts`. A waypoint with no entry here has no count to show, and the line is left out. */
    waypointEntryReferenceCounts: Map<String, Int>,
    distanceUnit: DistanceUnit,
    nowEpochMillis: Long,
    staleThresholdDays: Int,
    getFullRecord: suspend (String) -> Result<List<TrackPointRecord>>,
    /** The track's Delete: a pending delete with Undo, as the list's swipe asks for (Part 2 follow-ups F1 item 5, owner "Option A"). `null` shows none; a track still recording never shows one. */
    onDeleteTrack: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    /**
     * Whether a map is drawn on screen beneath the sheet (map chrome at 80%, dispatch 2026-09-28-56 as
     * amended by -58; planner message -77): the container is then at `MAP_CHROME_OVER_MAP_ALPHA`, and
     * otherwise solid, unchanged. Each call site says which it is.
     */
    overMap: Boolean = false,
    /** Dispatch 2026-09-28-502: a waypoint's "Navigate", given its id; `null` offers none (an entry map's sheet). */
    onNavigateToWaypoint: ((String) -> Unit)? = null,
    /**
     * Dispatch -616 as amended by -618: shows another record's details in this sheet's place: a waypoint
     * from a walk's "Waypoints on this track" list, and the walk again on Back from it. `null` leaves the
     * list's rows without a tap, and Back closes the sheet.
     */
    onOpenDetails: ((RecordDetailsTarget) -> Unit)? = null,
) {
    // One sheet per target, so a target opened in this one's place gets a sheet of its own, shown fresh:
    // the sheet Back has just hidden is not reused (its state is hidden, and it would stay so).
    key(target) {
        RecordDetailsSheetFor(
            target, waypoints, tracks, offlineRegions, waypointEntryReferenceCounts, distanceUnit, nowEpochMillis,
            staleThresholdDays, getFullRecord, onDeleteTrack, onDismiss, overMap, onNavigateToWaypoint, onOpenDetails,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordDetailsSheetFor(
    target: RecordDetailsTarget,
    waypoints: List<Waypoint>,
    tracks: List<Track>,
    offlineRegions: List<OfflineRegionSummary>,
    waypointEntryReferenceCounts: Map<String, Int>,
    distanceUnit: DistanceUnit,
    nowEpochMillis: Long,
    staleThresholdDays: Int,
    getFullRecord: suspend (String) -> Result<List<TrackPointRecord>>,
    onDeleteTrack: ((String) -> Unit)?,
    onDismiss: () -> Unit,
    overMap: Boolean,
    onNavigateToWaypoint: ((String) -> Unit)?,
    onOpenDetails: ((RecordDetailsTarget) -> Unit)?,
) {
    val waypoint = (target as? RecordDetailsTarget.WaypointDetails)?.let { t -> waypoints.firstOrNull { it.id == t.id } }
    val track = (target as? RecordDetailsTarget.TrackDetails)?.let { t -> tracks.firstOrNull { it.id == t.id } }
    val region = (target as? RecordDetailsTarget.OfflineRegionDetails)?.let { t -> offlineRegions.firstOrNull { it.id == t.id } }
    if (waypoint == null && track == null && region == null) {
        // Not an error: the record left the list the row came from. Nothing to show, so no sheet.
        LaunchedEffect(target) { onDismiss() }
        return
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Material3's own default container role for a sheet (`BottomSheetDefaults.ContainerColor`), at the
    // map chrome's alpha over a map, as the Layers sheet is (`MapLayersSheet`). The content colour is
    // pinned to the role's own for the reason given there: `contentColorFor` matches a colour-scheme
    // role exactly. The scrim stays Material3's default.
    // C1: the navigation bar's colour (was surfaceContainerLow); alpha as it was, 0.8 over a map and solid elsewhere.
    val containerColor = mapChromeFill(navigationBarContainerColor(), overMap)
    val contentColor = contentColorFor(navigationBarContainerColor())
    // Back, a scrim touch or a drag down: back to where this target was opened from, if anywhere.
    val parent = target.returnsTo()
    val dismiss: () -> Unit = if (parent != null && onOpenDetails != null) ({ onOpenDetails(parent) }) else onDismiss
    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag(RECORD_DETAILS_SHEET_TAG).mapChromeContainerColor(containerColor),
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        // Over a map only: the band under the sheet follows the sheet's own container (item 3, -104).
        if (overMap) MapChromeSheetNavigationBar()
        Column(
            modifier = Modifier
                .mapChromeContentColor(LocalContentColor.current)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            RecordDetailsBody(
                waypoint = waypoint,
                track = track,
                region = region,
                waypoints = waypoints,
                tracks = tracks,
                waypointEntryReferenceCounts = waypointEntryReferenceCounts,
                distanceUnit = distanceUnit,
                nowEpochMillis = nowEpochMillis,
                staleThresholdDays = staleThresholdDays,
                getFullRecord = getFullRecord,
                onDeleteTrack = onDeleteTrack,
                onNavigateToWaypoint = onNavigateToWaypoint,
                onOpenDetails = onOpenDetails,
            )
        }
    }
}

/** What the record's details show in [RecordDetailsSheet]. Exactly one of [waypoint], [track], [region] is non-null. */
@Composable
private fun RecordDetailsBody(
    waypoint: Waypoint?,
    track: Track?,
    region: OfflineRegionSummary?,
    waypoints: List<Waypoint>,
    tracks: List<Track>,
    waypointEntryReferenceCounts: Map<String, Int>,
    distanceUnit: DistanceUnit,
    nowEpochMillis: Long,
    staleThresholdDays: Int,
    getFullRecord: suspend (String) -> Result<List<TrackPointRecord>>,
    onDeleteTrack: ((String) -> Unit)?,
    onNavigateToWaypoint: ((String) -> Unit)?,
    onOpenDetails: ((RecordDetailsTarget) -> Unit)?,
) {
    when {
        waypoint != null -> WaypointDetails(waypoint, tracks, waypointEntryReferenceCounts, onNavigateToWaypoint)
        track != null -> TrackDetails(
            track,
            waypoints,
            distanceUnit,
            getFullRecord,
            onDeleteTrack,
            onOpenWaypoint = onOpenDetails?.let { open -> { id: String -> open(RecordDetailsTarget.WaypointDetails(id, fromTrackId = track.id)) } },
        )
        region != null -> OfflineRegionDetails(region, distanceUnit, nowEpochMillis, staleThresholdDays)
    }
}

@Composable
private fun WaypointDetails(waypoint: Waypoint, tracks: List<Track>, referenceCounts: Map<String, Int>, onNavigate: ((String) -> Unit)?) {
    val context = LocalContext.current
    val location = LatLng(waypoint.lat, waypoint.lng)
    DetailsTitle(waypoint.name)
    when (val mgrs = MgrsConverter.convert(location)) {
        is MgrsCoordinate.Grid -> DetailField(FIELD_MGRS, "MGRS", mgrs.value)
        // No line rather than a wrong one, as the row does (MgrsCoordinate's doc comment).
        is MgrsCoordinate.Unsupported -> Unit
    }
    DetailField(FIELD_COORDINATES, "Coordinates", decimalDegreesLabel(waypoint.lat, waypoint.lng))
    DetailField(FIELD_CREATED, "Created", formatRecordTimestamp(waypoint.createdAtEpochMillis))
    waypoint.trackId?.let { trackId ->
        // The track list is loaded at the ViewModel's init; a link to a track not in it is said
        // plainly rather than guessed at.
        val parent = tracks.firstOrNull { it.id == trackId }
        DetailField(FIELD_TRACK, "Track", parent?.let(::trackTitle) ?: "Not loaded")
    }
    referenceCounts[waypoint.id]?.let { count -> DetailField(FIELD_USED_IN, "Used in", journalEntryCountLabel(count)) }
    DetailsActions {
        // Dispatch -502: the app's own navigation first, then the hand-off to a maps app.
        onNavigate?.let { navigate ->
            OutlinedButton(
                onClick = { navigate(waypoint.id) },
                modifier = Modifier.testTag(RECORD_DETAILS_NAVIGATE_TAG),
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Navigate", modifier = Modifier.padding(start = Spacing.sm))
            }
        }
        OutlinedButton(
            onClick = { launchDirections(context, waypoint.name, location) },
            modifier = Modifier.testTag(RECORD_DETAILS_DIRECTIONS_TAG),
        ) {
            Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Directions", modifier = Modifier.padding(start = Spacing.sm))
        }
    }
}

@Composable
private fun TrackDetails(
    track: Track,
    waypoints: List<Waypoint>,
    distanceUnit: DistanceUnit,
    getFullRecord: suspend (String) -> Result<List<TrackPointRecord>>,
    onDeleteTrack: ((String) -> Unit)?,
    onOpenWaypoint: ((String) -> Unit)?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // The same derivation the entry editor's candidate rows use for a live track
    // (CartographyEntryEditScreen's TracksSection), from the points already in memory.
    val stats = ComputeTrackStatisticsUseCase()(track.points)
    DetailsTitle(trackTitle(track))
    // Dispatch -616 as amended by -618: the drawing carries start, end and dropped-waypoint dots, and the
    // list of those waypoints sits under it.
    WalkThumbnail(
        track = track,
        waypoints = waypoints,
        modifier = Modifier.size(TRACK_THUMBNAIL_SIZE).testTag(RECORD_DETAILS_THUMBNAIL_TAG),
    )
    WalkWaypointsSection(waypointsDroppedOn(track, waypoints), onOpenWaypoint)
    DetailField(FIELD_STARTED, "Started", formatRecordTimestamp(track.startedAtEpochMillis))
    DetailField(FIELD_ENDED, "Ended", track.endedAtEpochMillis?.let(::formatRecordTimestamp) ?: "Still recording")
    DetailField(FIELD_DISTANCE, "Distance", formatDistanceMeters(stats.distanceMeters, distanceUnit))
    DetailField(FIELD_DURATION, "Duration", formatTrackDuration(stats.durationMillis))
    DetailField(FIELD_POINTS, "Points", track.points.size.toString())
    networkFixExclusionNote(track)?.let { note ->
        Text(note, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag(RECORD_DETAILS_NOTE_TAG))
    }
    DetailsActions {
        OutlinedButton(
            onClick = { scope.launch { shareTrackGpx(context, track, waypoints, getFullRecord) } },
            modifier = Modifier.testTag(RECORD_DETAILS_SHARE_TAG),
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Share", modifier = Modifier.padding(start = Spacing.sm))
        }
        // Part 2 follow-ups F1 item 5 (owner "Option A"): the same pending delete and Undo as the row's swipe,
        // labelled "Delete" as the swipe's revealed button and its accessibility action are (DELETE_ACTION_LABEL,
        // SwipeToDelete.kt). Never for a track that is still recording. The sheet or pane closes by itself when the
        // track leaves the list it reads (the pending track is hidden at once).
        if (onDeleteTrack != null && track.canBeDeleted) {
            OutlinedButton(
                onClick = { onDeleteTrack(track.id) },
                modifier = Modifier.testTag(RECORD_DETAILS_DELETE_TAG),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(DELETE_ACTION_LABEL, modifier = Modifier.padding(start = Spacing.sm))
            }
        }
    }
}

/**
 * "Waypoints on this track" (dispatch -616 as amended by -618; the owner chose the heading): the
 * waypoints dropped on the walk, oldest first, each row its name and when it was dropped. A tap anywhere on
 * a row opens that waypoint's details in this sheet's place ([onOpen]), and Back returns to the walk. A
 * walk with none shows no section at all, not an empty heading. The rows have no fill of their own: over
 * a map the sheet's container already carries the map chrome's alpha.
 */
@Composable
private fun WalkWaypointsSection(dropped: List<Waypoint>, onOpen: ((String) -> Unit)?) {
    if (dropped.isEmpty()) return
    Text(
        WALK_WAYPOINTS_HEADING,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.sm).testTag(RECORD_DETAILS_WALK_WAYPOINTS_HEADING_TAG),
    )
    dropped.forEach { waypoint ->
        val tap = onOpen?.let { open -> Modifier.opensRecordDetails(waypoint.name) { open(waypoint.id) } }
            ?: Modifier.semantics(mergeDescendants = true) {}
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = WALK_WAYPOINT_ROW_MIN_HEIGHT)
                .then(tap)
                .testTag(recordDetailsWalkWaypointTag(waypoint.id)),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(waypoint.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                formatRecordTimestamp(waypoint.createdAtEpochMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OfflineRegionDetails(region: OfflineRegionSummary, distanceUnit: DistanceUnit, nowEpochMillis: Long, staleThresholdDays: Int) {
    val stale = isOfflineRegionStale(region.createdAtEpochMillis, nowEpochMillis, staleThresholdDays)
    DetailsTitle(region.name, stale = stale)
    DetailField(FIELD_RADIUS, "Radius", formatDistanceKm(region.region.radiusKm, distanceUnit))
    DetailField(FIELD_CENTRE, "Centre", decimalDegreesLabel(region.region.lat, region.region.lng))
    DetailField(FIELD_TILES, "Tiles", region.tileCount.toString())
    DetailField(FIELD_SIZE, "Size", offlineRegionSizeLabel(region))
    DetailField(
        FIELD_DOWNLOADED,
        "Downloaded",
        "${formatRecordTimestamp(region.createdAtEpochMillis)} (${relativeTimeLabel(region.createdAtEpochMillis, nowEpochMillis)})",
    )
    Text(offlineRegionZoomNote(region), style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag(RECORD_DETAILS_ZOOM_TAG))
}

@Composable
private fun DetailsTitle(title: String, stale: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(RECORD_DETAILS_TITLE_TAG))
        if (stale) {
            Text(
                "Stale",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag(RECORD_DETAILS_STALE_TAG),
            )
        }
    }
}

/** One labelled value. Merged for accessibility, so TalkBack reads "Radius, 9 mi" as one item. */
@Composable
private fun DetailField(key: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .testTag(recordDetailsFieldTag(key)),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(FIELD_LABEL_WIDTH),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailsActions(content: @Composable () -> Unit) {
    Row(modifier = Modifier.padding(top = Spacing.sm), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) { content() }
}

/** "2 journal entries", "1 journal entry", "no journal entries": the snackbar's own nouns (J4). */
internal fun journalEntryCountLabel(count: Int): String = when (count) {
    0 -> "no journal entries"
    1 -> "1 journal entry"
    else -> "$count journal entries"
}

/**
 * A track's duration as the entry cards and the entry editor print it ("2h 10m", "48m";
 * `trackSubtitle(distanceMeters, durationMillis, unit)` in `CartographyEntryEditScreen.kt`, whose
 * duration half this repeats because that function returns distance and duration as one string and
 * its file is outside J5c).
 */
internal fun formatTrackDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private val FIELD_LABEL_WIDTH = 112.dp
private val TRACK_THUMBNAIL_SIZE = 96.dp

/** The Material minimum touch target, so a row is easy to hit with a thumb. */
private val WALK_WAYPOINT_ROW_MIN_HEIGHT = 48.dp

internal const val WALK_WAYPOINTS_HEADING = "Waypoints on this track"
internal const val RECORD_DETAILS_WALK_WAYPOINTS_HEADING_TAG = "record-details-walk-waypoints-heading"
internal fun recordDetailsWalkWaypointTag(waypointId: String): String = "record-details-walk-waypoint-$waypointId"

internal const val RECORD_DETAILS_SHEET_TAG = "record-details-sheet"

internal const val RECORD_DETAILS_TITLE_TAG = "record-details-title"
internal const val RECORD_DETAILS_STALE_TAG = "record-details-stale"
internal const val RECORD_DETAILS_NOTE_TAG = "record-details-note"
internal const val RECORD_DETAILS_ZOOM_TAG = "record-details-zoom"
internal const val RECORD_DETAILS_THUMBNAIL_TAG = "record-details-thumbnail"
internal const val RECORD_DETAILS_DIRECTIONS_TAG = "record-details-directions"
internal const val RECORD_DETAILS_NAVIGATE_TAG = "record-details-navigate"
internal const val RECORD_DETAILS_SHARE_TAG = "record-details-share"
internal const val RECORD_DETAILS_DELETE_TAG = "record-details-delete"

internal fun recordDetailsFieldTag(key: String): String = "record-details-field-$key"

internal const val FIELD_MGRS = "mgrs"
internal const val FIELD_COORDINATES = "coordinates"
internal const val FIELD_CREATED = "created"
internal const val FIELD_TRACK = "track"
internal const val FIELD_USED_IN = "used-in"
internal const val FIELD_STARTED = "started"
internal const val FIELD_ENDED = "ended"
internal const val FIELD_DISTANCE = "distance"
internal const val FIELD_DURATION = "duration"
internal const val FIELD_POINTS = "points"
internal const val FIELD_RADIUS = "radius"
internal const val FIELD_CENTRE = "centre"
internal const val FIELD_TILES = "tiles"
internal const val FIELD_SIZE = "size"
internal const val FIELD_DOWNLOADED = "downloaded"
