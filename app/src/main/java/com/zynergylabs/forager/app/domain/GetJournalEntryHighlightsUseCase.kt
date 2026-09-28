package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.RecordPolyline
import com.zynergylabs.forager.app.domain.model.RecordRegion
import com.zynergylabs.forager.app.domain.model.Waypoint
import java.time.LocalDate

/** One saved entry shown on the map, as the chip's list and a bubble's entry lines name it: by its [date]. */
data class JournalEntryOnMap(val entryId: String, val date: LocalDate)

/** The kinds of record a shown entry highlights (J8: kept tracks, finds, waypoints, offline regions, and its attached photos). */
enum class HighlightedRecordKind { TRACK, FIND, PHOTO, WAYPOINT, OFFLINE_REGION }

/** One highlighted record, by kind and the id its map feature carries. */
data class HighlightedRecord(val kind: HighlightedRecordKind, val recordId: String)

/**
 * What the Maps tab highlights for the shown entries (J8), as record geometry with each record's id,
 * plus the shown entries themselves and, per highlighted record, the shown entries that keep it.
 */
data class JournalEntryHighlights(
    /** Every saved entry shown on the map, in the order the Journal lists them. */
    val shownEntries: List<JournalEntryOnMap>,
    val trackPolylines: List<RecordPolyline>,
    val findMarkers: List<RecordPoint>,
    val photoMarkers: List<RecordPoint>,
    val waypointMarkers: List<RecordPoint>,
    val offlineRegionCircles: List<RecordRegion>,
    /** For each highlighted record, the shown entries that keep it, in [shownEntries]'s order. */
    val keptIn: Map<HighlightedRecord, List<JournalEntryOnMap>>,
) {
    companion object {
        val NONE = JournalEntryHighlights(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyMap())
    }
}

/** J8 tests-first stub: highlights nothing until the use case is built. */
class GetJournalEntryHighlightsUseCase {
    operator fun invoke(entries: List<CartographyEntry>, records: MapRecords, waypoints: List<Waypoint>): JournalEntryHighlights =
        JournalEntryHighlights.NONE
}
