package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.Waypoint
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [buildRecordsLogbook]'s grouping and order, headless — journal redesign J1, S4. The zone is fixed
 * and the times are chosen so a naive UTC grouping would put two records on the wrong day, so a
 * grouping that ignored the zone fails here rather than passing by coincidence.
 */
class RecordsLogbookTest {

    private val zone = ZoneId.of("America/Los_Angeles")
    private fun at(date: LocalDate, hour: Int): Long = date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private val d1 = LocalDate.of(2026, 9, 26)
    private val d2 = LocalDate.of(2026, 9, 25)

    private fun find(id: String, date: LocalDate) = MushroomLogEntry.draft(id = id, location = null, date = date).copy(isDraft = false)
    private fun waypoint(id: String, millis: Long) = Waypoint(id = id, lat = 0.0, lng = 0.0, altitude = null, name = id, note = "", createdAtEpochMillis = millis)
    private fun track(id: String, millis: Long) = Track(id = id, name = null, startedAtEpochMillis = millis, endedAtEpochMillis = null, points = emptyList())
    private fun region(id: Long, millis: Long) = OfflineRegionSummary(id = id, name = "r$id", region = Region(0.0, 0.0, 1), minZoom = 0.0, maxZoom = 1.0, tileCount = 1, sizeBytes = 1, createdAtEpochMillis = millis)

    private fun ids(day: LogbookDay): List<String> = day.finds.map { it.id } + day.timed.map {
        when (it) {
            is TimedRecord.TrackRecord -> it.track.id
            is TimedRecord.WaypointRecord -> it.waypoint.id
            is TimedRecord.OfflineRegionRecord -> "r${it.region.id}"
        }
    }

    @Test
    fun `days newest first, finds first in their day in input order, then timed records newest first, grouped in the given zone`() {
        val days = buildRecordsLogbook(
            finds = listOf(find("F2", d1), find("Fold", d2), find("F1", d1)),
            // 20:00 and 21:00 local on d2 are 03:00/04:00 UTC on d1: a UTC grouping would move them.
            tracks = listOf(track("T-late", at(d2, 21)), track("T1", at(d1, 15))),
            waypoints = listOf(waypoint("W-eve", at(d2, 20)), waypoint("W1", at(d1, 9))),
            offlineRegions = listOf(region(7, at(d1, 12))),
            zone = zone,
        )

        assertEquals(listOf(d1, d2), days.map { it.date })
        assertEquals(listOf("F2", "F1", "T1", "r7", "W1"), ids(days[0]))
        assertEquals(listOf("Fold", "T-late", "W-eve"), ids(days[1]))
        assertEquals(listOf(5, 3), days.map { it.recordCount })
    }

    @Test
    fun `timed records on the same millisecond keep tracks, waypoints, regions order`() {
        val t = at(d1, 10)
        val days = buildRecordsLogbook(emptyList(), listOf(track("T", t)), listOf(waypoint("W", t)), listOf(region(1, t)), zone)
        assertEquals(listOf("T", "W", "r1"), ids(days.single()))
    }

    @Test
    fun `nothing in, nothing out`() {
        assertEquals(emptyList<LogbookDay>(), buildRecordsLogbook(emptyList(), emptyList(), emptyList(), emptyList(), zone))
    }
}
