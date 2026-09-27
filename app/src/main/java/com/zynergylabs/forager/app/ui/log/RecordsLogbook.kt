package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.Waypoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * A record in the All logbook that carries a time: everything but a find.
 * [epochMillis] is what it is ordered by within its day.
 */
internal sealed interface TimedRecord {
    val epochMillis: Long

    data class TrackRecord(val track: Track) : TimedRecord {
        override val epochMillis: Long get() = track.startedAtEpochMillis
    }

    data class WaypointRecord(val waypoint: Waypoint) : TimedRecord {
        override val epochMillis: Long get() = waypoint.createdAtEpochMillis
    }

    data class OfflineRegionRecord(val region: OfflineRegionSummary) : TimedRecord {
        override val epochMillis: Long get() = region.createdAtEpochMillis
    }
}

/** One day of the All logbook: that day's finds, then its timed records. */
internal data class LogbookDay(
    val date: LocalDate,
    val finds: List<MushroomLogEntry>,
    val timed: List<TimedRecord>,
) {
    val recordCount: Int get() = finds.size + timed.size
}

/**
 * The All logbook's grouping and order — journal redesign J1, S4 (plan J4; the owner's answers in
 * `prompts/preserved/2026-09-27-17.md`). Pure Kotlin, no Compose, so it is unit-testable headless
 * (CLAUDE.md, Architecture).
 *
 * - **Days newest first.**
 * - **Within a day, finds first**, then the timed records newest first. A find carries
 *   [MushroomLogEntry.foundOn], a calendar date with no time of day (`MushroomLogEntry.kt:41`), so it
 *   cannot be placed among timed records by time; the owner chose "Finds first in the day".
 * - **Stable among equals.** Several finds on one day keep the order they arrive in ([finds] is
 *   `MushroomLogUiState.entries`, the Finds gallery's own order), and timed records with the same
 *   millisecond keep the order tracks, then waypoints, then regions, each in its input order. Both
 *   sorts are Kotlin's stable `sortedBy*`.
 * - **Days are device-local.** An epoch-millis record falls on its calendar date in [zone], and a
 *   find on its stored `foundOn` as-is — the same convention `LocalDayRange` states for every
 *   day-scoped read in this app (owner decision #1, "day boundaries are device-local").
 *
 * [finds] should be committed finds only: the owner's answer 3 ("Committed finds only"), the same
 * list the Finds chip counts.
 */
internal fun buildRecordsLogbook(
    finds: List<MushroomLogEntry>,
    tracks: List<Track>,
    waypoints: List<Waypoint>,
    offlineRegions: List<OfflineRegionSummary>,
    zone: ZoneId,
): List<LogbookDay> {
    val timed: List<TimedRecord> =
        tracks.map { TimedRecord.TrackRecord(it) } +
            waypoints.map { TimedRecord.WaypointRecord(it) } +
            offlineRegions.map { TimedRecord.OfflineRegionRecord(it) }
    val timedByDay = timed.groupBy { Instant.ofEpochMilli(it.epochMillis).atZone(zone).toLocalDate() }
    val findsByDay = finds.groupBy { it.foundOn }
    return (timedByDay.keys + findsByDay.keys)
        .sortedDescending()
        .map { date ->
            LogbookDay(
                date = date,
                finds = findsByDay[date].orEmpty(),
                timed = timedByDay[date].orEmpty().sortedByDescending { it.epochMillis },
            )
        }
}
