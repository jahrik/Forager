package com.zynergylabs.forager.app.ui.log

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.EntryReport
import com.zynergylabs.forager.app.domain.EntryReportWaypoint
import com.zynergylabs.forager.app.domain.HeightProfile
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.TrackDecision
import com.zynergylabs.forager.app.domain.model.UnitSystem
import com.zynergylabs.forager.app.domain.model.formatDistanceMeters
import com.zynergylabs.forager.app.domain.model.formatTimeSpan
import com.zynergylabs.forager.app.domain.model.formatWholeLength
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.util.Date
import java.util.Locale

/**
 * The entry report's labelled tiles (dispatch 2026-09-28-667, data part A; the owner in RECORD -656:
 * "Summary tiles + height profile (Recommended)"): Distance, Time out, Climb and Finds, each a label
 * over its value with its unit. Two to a row, so a value such as "1 h 12 min" has room on a phone.
 * The track tiles show only when a track is included; Finds shows whenever a track or a find is.
 */
@Composable
internal fun EntrySummaryTiles(report: EntryReport, distanceUnit: DistanceUnit, modifier: Modifier = Modifier) {
    LabelledTiles(tiles = entrySummaryTiles(report, distanceUnit), tagOf = ::entryTileTag, modifier = modifier)
}

/**
 * Labelled tiles, two to a row, each a label over its value: the entry report's ([EntrySummaryTiles]) and, since dispatch
 * 2026-09-28-677 (data part B), the track sheet's, laid out by this one function so the two read alike. [tagOf] gives each
 * tile's test tag from its label. Nothing is drawn for no tiles.
 */
@Composable
internal fun LabelledTiles(
    tiles: List<Pair<String, String>>,
    tagOf: (String) -> String,
    modifier: Modifier = Modifier,
    /**
     * Whether the tiles are drawn over a map, inside a container already at the map chrome's alpha (the track sheet opened
     * from a map). Then they are outlined, with no fill: any fill on that container would composite past 0.8 (CLAUDE.md,
     * "Nothing fully obstructs the map view"). Off a map they are filled, as the entry report's always were.
     */
    overMap: Boolean = false,
) {
    if (tiles.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        tiles.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { (label, value) ->
                    val fill = if (overMap) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = fill,
                        border = if (overMap) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                        // The fill a test reads to tell outlined from filled (MapChromeOverMapTest), beside the tag.
                        modifier = Modifier.weight(1f).testTag(tagOf(label)).mapChromeContainerColor(fill).semantics(mergeDescendants = true) {},
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(value, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** The tiles as label and value, in order. Separate from the layout so a test can read every figure exactly. */
internal fun entrySummaryTiles(report: EntryReport, distanceUnit: DistanceUnit): List<Pair<String, String>> = buildList {
    if (report.trackCount > 0) {
        add(TILE_DISTANCE to formatDistanceMeters(report.distanceMeters, distanceUnit))
        add(TILE_TIME_OUT to formatTimeSpan(report.durationMillis))
        add(TILE_CLIMB to (report.climbMeters?.let { formatWholeLength(it, UnitSystem.forDistanceUnit(distanceUnit)) } ?: CLIMB_NOT_RECORDED))
    }
    if (report.trackCount > 0 || report.findCount > 0) add(TILE_FINDS to report.findCount.toString())
}

/**
 * The small height profile, drawn with Compose [Canvas] (the app has no chart library), with its
 * highest and lowest heights at the left and the distance it spans along the bottom, all as text, so a
 * test reads the labels and a screen reader the summary. Its look is phone-only: Robolectric does not
 * render a Canvas. When it can not be drawn honestly it is one plain line saying why, and with no track
 * included it is nothing at all.
 */
@Composable
internal fun EntryHeightProfile(profile: HeightProfile, distanceUnit: DistanceUnit, modifier: Modifier = Modifier) {
    when (profile) {
        HeightProfile.NoTrack -> Unit
        HeightProfile.PointsUnavailable -> ProfileLine(PROFILE_POINTS_UNAVAILABLE_LINE, modifier)
        is HeightProfile.TooFewHeights -> ProfileLine(PROFILE_TOO_FEW_HEIGHTS_LINE, modifier)
        is HeightProfile.Drawn -> {
            val unitSystem = UnitSystem.forDistanceUnit(distanceUnit)
            val highest = formatWholeLength(profile.highestMeters, unitSystem)
            val lowest = formatWholeLength(profile.lowestMeters, unitSystem)
            val length = formatDistanceMeters(profile.lengthMeters, distanceUnit)
            val lineColor = MaterialTheme.colorScheme.primary
            val baseColor = MaterialTheme.colorScheme.outlineVariant
            Column(modifier = modifier.fillMaxWidth().testTag(ENTRY_HEIGHT_PROFILE_TAG), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(PROFILE_TITLE, style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth().height(PROFILE_HEIGHT_DP.dp)) {
                    Column(
                        modifier = Modifier.width(PROFILE_AXIS_WIDTH_DP.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(highest, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(lowest, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { contentDescription = "Height profile: lowest $lowest, highest $highest, over $length" },
                    ) {
                        val span = (profile.highestMeters - profile.lowestMeters).takeIf { it > 0.0 } ?: 1.0
                        val lengthMeters = profile.lengthMeters.takeIf { it > 0.0 } ?: 1.0
                        drawLine(baseColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        profile.segments.forEach { segment ->
                            val path = Path()
                            segment.forEachIndexed { index, sample ->
                                val x = (sample.distanceMeters / lengthMeters * size.width).toFloat()
                                val y = (size.height - (sample.altitudeMeters - profile.lowestMeters) / span * size.height).toFloat()
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(start = PROFILE_AXIS_WIDTH_DP.dp)) {
                    Text(PROFILE_START_LABEL, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text(length, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun ProfileLine(text: String, modifier: Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.testTag(ENTRY_HEIGHT_PROFILE_LINE_TAG),
    )
}

/**
 * The included waypoints as a short table: name, the time its record was made (the phone's 12- or
 * 24-hour clock), and the distance walked from the start of its track to it. A tap on a row shows its
 * coordinates under it, and a second tap hides them (the owner: "coordinates behind a tap"). A figure
 * the app does not have is a dash, never a guess (see [EntryReportWaypoint]).
 *
 * Which rows are open ([openRows], by waypoint id) is hoisted to `AvailabilityScreen`, as the editor
 * panel's open groups are, so it survives leaving the report and coming back within a session (the
 * planner's call in RECORD -671, under CLAUDE.md's UX defaults).
 */
@Composable
internal fun EntryWaypointTable(
    waypoints: List<EntryReportWaypoint>,
    distanceUnit: DistanceUnit,
    openRows: Set<String>,
    onToggleRow: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (waypoints.isEmpty()) return
    val timeFormat = DateFormat.getTimeFormat(LocalContext.current)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text("Waypoints", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(WAYPOINT_COLUMN_NAME, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            Text(WAYPOINT_COLUMN_TIME, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(WAYPOINT_TIME_WIDTH_DP.dp))
            Text(WAYPOINT_COLUMN_FROM_START, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End, modifier = Modifier.width(WAYPOINT_DISTANCE_WIDTH_DP.dp))
        }
        waypoints.forEach { waypoint ->
            val isOpen = waypoint.id in openRows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = if (isOpen) "Hide coordinates" else "Show coordinates") { onToggleRow(waypoint.id) }
                    .padding(vertical = Spacing.xs)
                    .testTag(entryWaypointRowTag(waypoint.id)),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(waypoint.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        waypoint.timeEpochMillis?.let { timeFormat.format(Date(it)) } ?: MISSING_FIGURE,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(WAYPOINT_TIME_WIDTH_DP.dp),
                    )
                    Text(
                        waypoint.distanceFromStartMeters?.let { formatDistanceMeters(it, distanceUnit) } ?: MISSING_FIGURE,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(WAYPOINT_DISTANCE_WIDTH_DP.dp),
                    )
                }
                if (isOpen) {
                    Box(modifier = Modifier.padding(top = Spacing.xs)) {
                        Text(
                            waypointCoordinatesLine(waypoint.lat, waypoint.lng),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag(entryWaypointCoordinatesTag(waypoint.id)),
                        )
                    }
                }
            }
        }
    }
}

/**
 * The included tracks under the table, one name row each (the owner, RECORD -671: "Keep Finds & maps,
 * fold tracks (Recommended)"): no distance or time line, since the tiles carry those. A tap on a track
 * still in Records ([liveTrackIds]) opens its details ([onOpenTrack]). One known to be deleted from Records
 * has nothing to open, so its row says so instead of doing nothing. [dayLoaded] `false` means the day's
 * records did not load, so a track missing from [liveTrackIds] is not known to be gone and gets no note.
 * A track the read seam left empty keeps its note, as before.
 */
@Composable
internal fun EntryTrackRows(
    tracks: List<TrackDecision>,
    liveTrackIds: Set<String>,
    dayLoaded: Boolean,
    onOpenTrack: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text("Tracks", style = MaterialTheme.typography.titleSmall)
        tracks.forEach { track ->
            val live = track.trackId in liveTrackIds
            val note = trackExclusionSuffix(liveTrack = null, snapshotPointCount = track.pointCount).removePrefix(" · ").takeIf { it.isNotEmpty() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (live) Modifier.clickable(onClickLabel = "Open track") { onOpenTrack(track.trackId) } else Modifier)
                    .padding(vertical = Spacing.xs)
                    .testTag(entryTrackRowTag(track.trackId)),
            ) {
                Text(track.name ?: "Recorded track", style = MaterialTheme.typography.bodyMedium)
                listOfNotNull(note, TRACK_NOT_IN_RECORDS_LINE.takeIf { !live && dayLoaded }).forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Latitude then longitude, four decimals (about 11 m), as the report showed before, now labelled. */
internal fun waypointCoordinatesLine(lat: Double, lng: Double): String =
    "Coordinates: ${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)}"

internal const val TILE_DISTANCE = "Distance"
internal const val TILE_TIME_OUT = "Time out"
internal const val TILE_CLIMB = "Climb"
internal const val TILE_FINDS = "Finds"
internal const val CLIMB_NOT_RECORDED = "Not recorded"
internal const val PROFILE_TITLE = "Height"
internal const val PROFILE_START_LABEL = "Start"
internal const val PROFILE_TOO_FEW_HEIGHTS_LINE = "No height profile: the phone recorded too few heights on this walk."
internal const val PROFILE_POINTS_UNAVAILABLE_LINE = "No height profile: this walk's recorded points couldn't be read."
internal const val WAYPOINT_COLUMN_NAME = "Name"
internal const val WAYPOINT_COLUMN_TIME = "Time"
internal const val WAYPOINT_COLUMN_FROM_START = "From start"
internal const val MISSING_FIGURE = "—"
internal const val TRACK_NOT_IN_RECORDS_LINE = "No longer in Records"

private const val PROFILE_HEIGHT_DP = 96
private const val PROFILE_AXIS_WIDTH_DP = 64
private const val WAYPOINT_TIME_WIDTH_DP = 76
private const val WAYPOINT_DISTANCE_WIDTH_DP = 72

internal fun entryTileTag(label: String) = "entry-tile-$label"
internal const val ENTRY_HEIGHT_PROFILE_TAG = "entry-height-profile"
internal const val ENTRY_HEIGHT_PROFILE_LINE_TAG = "entry-height-profile-line"
internal fun entryTrackRowTag(id: String) = "entry-track-row-$id"
internal fun entryWaypointRowTag(id: String) = "entry-waypoint-row-$id"
internal fun entryWaypointCoordinatesTag(id: String) = "entry-waypoint-coordinates-$id"
