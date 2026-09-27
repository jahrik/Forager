package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.isOfflineRegionStale
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.availability.AvailabilityUiState
import com.zynergylabs.forager.app.ui.availability.OfflineRegionDeleteDialog
import com.zynergylabs.forager.app.ui.availability.OfflineRegionRow
import com.zynergylabs.forager.app.ui.availability.WaypointRow
import com.zynergylabs.forager.app.ui.theme.Spacing
import com.zynergylabs.forager.app.ui.track.TrackExportRow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Records' **All** view — journal redesign J1, S4 (plan J4; the owner's answers in
 * `prompts/preserved/2026-09-27-17.md`): every record in one list, grouped by day, days newest first,
 * that day's finds first and then its timed records newest first ([buildRecordsLogbook] owns the
 * order).
 *
 * **The rows are the chips' own rows** (owner's answer 2, "Same rows as their chips"): [FindTile],
 * [TrackExportRow], [WaypointRow] and [OfflineRegionRow], each exactly as its single-type chip shows
 * it, controls included, with a [RecordTypeBadge] in front. Finds sit two to a row, as the Finds
 * gallery's own two-column grid shows them. Deletes confirm through the same dialogs the chips use
 * ([WaypointDeleteDialog], [OfflineRegionDeleteDialog]); nothing about deleting changes in J1.
 * Journal redesign J4 replaced that for waypoints: a waypoint's whole badged row is a
 * [SwipeToDeleteRow], as in its own chip, and [onDeleteWaypoint] asks for a pending delete with Undo.
 *
 * **Tapping a find** calls [onOpenFind], which `RecordsTab` turns into "select the Finds chip and open
 * that find's report there", so Back goes report, then the Finds gallery, then All. Waypoint, track
 * and region rows have no row tap, as today.
 *
 * [finds] `null` means the caller has no finds list to give (`LogPanel`, out of scope until J6): the
 * logbook then says so in a line of its own rather than presenting the timed records as everything.
 *
 * A scrolling `Column`, like the single-type lists it gathers (`WaypointsSection`, `TrackExportList`),
 * not a lazy list: those lists already compose every row, and this one is their sum.
 */
@Composable
internal fun RecordsLogbookList(
    finds: List<MushroomLogEntry>?,
    tracks: List<Track>,
    waypoints: List<Waypoint>,
    availabilityUiState: AvailabilityUiState,
    distanceUnit: DistanceUnit,
    currentTime: CurrentTimeProvider,
    getFullRecord: suspend (String) -> Result<List<TrackPointRecord>>,
    onDeleteWaypoint: (String) -> Unit,
    onDeleteOfflineRegion: (Long) -> Unit,
    onOpenFind: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteRegion by remember { mutableStateOf<OfflineRegionSummary?>(null) }
    val days = buildRecordsLogbook(
        finds = finds.orEmpty(),
        tracks = tracks,
        waypoints = waypoints,
        offlineRegions = availabilityUiState.offlineRegions,
        zone = ZoneId.systemDefault(),
    )
    val now = currentTime.nowEpochMillis()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (finds == null) {
            Text(
                "Finds are not listed here. See the Finds chip.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (days.isEmpty()) {
            Text("No records yet.", style = MaterialTheme.typography.bodyMedium)
        }
        days.forEach { day ->
            LogbookDayHeader(day)
            day.finds.chunked(FIND_COLUMNS).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    pair.forEach { find ->
                        Box(modifier = Modifier.weight(1f).testTag(logbookRowTag(RecordType.FINDS, find.id))) {
                            FindTile(entry = find, onClick = { onOpenFind(find.id) })
                            RecordTypeBadge(
                                type = RecordType.FINDS,
                                recordId = find.id,
                                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.xs),
                            )
                        }
                    }
                    repeat(FIND_COLUMNS - pair.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
            day.timed.forEach { record ->
                when (record) {
                    is TimedRecord.TrackRecord -> BadgedRow(RecordType.TRACKS, record.track.id) {
                        TrackExportRow(track = record.track, waypoints = waypoints, getFullRecord = getFullRecord)
                    }
                    is TimedRecord.WaypointRecord -> key(RecordType.WAYPOINTS, record.waypoint.id) {
                        SwipeToDeleteRow(
                            testTag = swipeToDeleteTag(RecordType.WAYPOINTS, record.waypoint.id),
                            onDelete = { onDeleteWaypoint(record.waypoint.id) },
                        ) {
                            BadgedRow(RecordType.WAYPOINTS, record.waypoint.id) {
                                WaypointRow(waypoint = record.waypoint)
                            }
                        }
                    }
                    is TimedRecord.OfflineRegionRecord -> BadgedRow(RecordType.OFFLINE_MAPS, record.region.id.toString()) {
                        OfflineRegionRow(
                            region = record.region,
                            isStale = isOfflineRegionStale(record.region.createdAtEpochMillis, now, availabilityUiState.offlineStaleThresholdDays),
                            distanceUnit = distanceUnit,
                            nowEpochMillis = now,
                            onDelete = { pendingDeleteRegion = record.region },
                        )
                    }
                }
            }
        }
    }

    pendingDeleteRegion?.let { region ->
        OfflineRegionDeleteDialog(
            region = region,
            entryReferenceCounts = availabilityUiState.offlineRegionEntryReferenceCounts,
            onDeleteOfflineRegion = onDeleteOfflineRegion,
            onDismiss = { pendingDeleteRegion = null },
        )
    }
}

@Composable
private fun LogbookDayHeader(day: LogbookDay) {
    val count = day.recordCount
    Text(
        "${DAY_HEADER_FORMAT.format(day.date)} · $count ${if (count == 1) "record" else "records"}",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = Spacing.sm)
            .testTag(logbookDayTag(day.date)),
    )
}

/** A timed record's own row with its type badge in front. */
@Composable
private fun BadgedRow(type: RecordType, recordId: String, row: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag(logbookRowTag(type, recordId)),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordTypeBadge(type = type, recordId = recordId)
        Box(modifier = Modifier.weight(1f)) { row() }
    }
}

/**
 * A record's type, as a small disc in [RecordTypeStyle]'s colours (plan J6): the type's container
 * behind its chip icon in the type's accent. Announced as the type name.
 */
@Composable
internal fun RecordTypeBadge(type: RecordType, recordId: String, modifier: Modifier = Modifier) {
    val colors = RecordTypeStyle.colors(type)
    Box(
        modifier = modifier
            .size(BADGE_SIZE)
            .clip(CircleShape)
            .background(colors.container)
            .semantics { contentDescription = type.displayName() }
            .testTag(recordTypeBadgeTag(type, recordId)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(type.filter().chipIcon(), contentDescription = null, tint = colors.accent, modifier = Modifier.size(BADGE_ICON_SIZE))
    }
}

private fun RecordType.filter(): RecordsSubTab = when (this) {
    RecordType.FINDS -> RecordsSubTab.FINDS
    RecordType.TRACKS -> RecordsSubTab.RECORDED_TRACKS
    RecordType.WAYPOINTS -> RecordsSubTab.WAYPOINTS
    RecordType.OFFLINE_MAPS -> RecordsSubTab.OFFLINE_MAPS
}

private fun RecordType.displayName(): String = when (this) {
    RecordType.FINDS -> "Find"
    RecordType.TRACKS -> "Track"
    RecordType.WAYPOINTS -> "Waypoint"
    RecordType.OFFLINE_MAPS -> "Offline map"
}

internal fun RecordType.tagName(): String = when (this) {
    RecordType.FINDS -> "finds"
    RecordType.TRACKS -> "tracks"
    RecordType.WAYPOINTS -> "waypoints"
    RecordType.OFFLINE_MAPS -> "offline-maps"
}

internal fun logbookDayTag(date: LocalDate): String = "records-logbook-day-$date"
internal fun logbookRowTag(type: RecordType, recordId: String): String = "records-logbook-row-${type.tagName()}-$recordId"
internal fun recordTypeBadgeTag(type: RecordType, recordId: String): String = "records-badge-${type.tagName()}-$recordId"

/** The Finds gallery's own column count on compact (`FindsGalleryScreen`'s `columns` default). */
private const val FIND_COLUMNS = 2
private val BADGE_SIZE = 28.dp
private val BADGE_ICON_SIZE = 16.dp
private val DAY_HEADER_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
