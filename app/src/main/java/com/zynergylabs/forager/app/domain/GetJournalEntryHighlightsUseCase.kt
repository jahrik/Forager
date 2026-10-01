package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.LatLng
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

/**
 * J8-2 (`prompts/preserved/2026-09-28-52.md`; the owner's "Highlight in place", "Live records" and
 * "Saved entries only"): which of the records the Maps tab draws are kept by an entry shown on the map,
 * and by which entries.
 *
 * - **Saved and shown only.** An entry counts when it is committed and [CartographyEntry.shownOnMap];
 *   a draft never does, whatever its flag says, so the rule holds by construction and not only because
 *   the caller passes committed entries.
 * - **Live geometry.** Every highlight is one of the records the map already draws — [records] (the
 *   saved finds, located photos, ended tracks and offline regions, pending deletes left out) and
 *   [waypoints] (the waypoints the Maps tab draws) — with that record's own geometry. An entry's
 *   snapshots are never read for a position, so a waypoint moved since the entry was written is
 *   highlighted where it is now, and a kept record since deleted, or not drawn today (an unended track),
 *   is simply not highlighted.
 * - **What an entry keeps.** Its kept track, find, waypoint and offline-region decisions (a withheld
 *   one is not kept), and its attached photos (planner, continuation `2026-09-28-53`, Q4: the photos
 *   the entry map draws, not the photos of the finds it keeps).
 *
 * Headless, like [GetMapRecordsUseCase]: plain lists in, plain lists out. Whether a halo is drawn also
 * depends on the Layers sheet's switches, which the map applies (`layerPaintFor`), not this.
 */
class GetJournalEntryHighlightsUseCase {
    operator fun invoke(entries: List<CartographyEntry>, records: MapRecords, waypoints: List<Waypoint>): JournalEntryHighlights {
        val shown = entries.filter { !it.isDraft && it.shownOnMap }
        if (shown.isEmpty()) return JournalEntryHighlights.NONE
        val keepers = LinkedHashMap<HighlightedRecord, MutableList<JournalEntryOnMap>>()
        val shownEntries = shown.map { entry ->
            val named = JournalEntryOnMap(entry.id, entry.date)
            fun keep(kind: HighlightedRecordKind, recordId: String) {
                keepers.getOrPut(HighlightedRecord(kind, recordId)) { mutableListOf() }.add(named)
            }
            entry.trackDecisions.filter { it.kept }.forEach { keep(HighlightedRecordKind.TRACK, it.trackId) }
            entry.findDecisions.filter { it.kept }.forEach { keep(HighlightedRecordKind.FIND, it.findId) }
            entry.waypointDecisions.filter { it.kept }.forEach { keep(HighlightedRecordKind.WAYPOINT, it.waypointId) }
            entry.offlineRegionDecisions.filter { it.kept }.forEach { keep(HighlightedRecordKind.OFFLINE_REGION, it.offlineRegionId.toString()) }
            entry.photos.forEach { keep(HighlightedRecordKind.PHOTO, it.photoId) }
            named
        }
        fun kept(kind: HighlightedRecordKind, recordId: String) = HighlightedRecord(kind, recordId) in keepers

        val trackPolylines = records.trackPolylines.filter { kept(HighlightedRecordKind.TRACK, it.recordId) }
        val findMarkers = records.findMarkers.filter { kept(HighlightedRecordKind.FIND, it.recordId) }
        val photoMarkers = records.photoMarkers.filter { kept(HighlightedRecordKind.PHOTO, it.recordId) }
        val waypointMarkers = waypoints.filter { kept(HighlightedRecordKind.WAYPOINT, it.id) }.map { RecordPoint(it.id, LatLng(it.lat, it.lng)) }
        val offlineRegionCircles = records.offlineRegionCircles.filter { kept(HighlightedRecordKind.OFFLINE_REGION, it.recordId) }

        // Only records the map draws carry entry lines: a kept record not drawn has no bubble to show them in.
        val drawn = trackPolylines.map { HighlightedRecord(HighlightedRecordKind.TRACK, it.recordId) } +
            findMarkers.map { HighlightedRecord(HighlightedRecordKind.FIND, it.recordId) } +
            photoMarkers.map { HighlightedRecord(HighlightedRecordKind.PHOTO, it.recordId) } +
            waypointMarkers.map { HighlightedRecord(HighlightedRecordKind.WAYPOINT, it.recordId) } +
            offlineRegionCircles.map { HighlightedRecord(HighlightedRecordKind.OFFLINE_REGION, it.recordId) }
        return JournalEntryHighlights(
            shownEntries = shownEntries,
            trackPolylines = trackPolylines,
            findMarkers = findMarkers,
            photoMarkers = photoMarkers,
            waypointMarkers = waypointMarkers,
            offlineRegionCircles = offlineRegionCircles,
            keptIn = drawn.toSet().associateWith { keepers.getValue(it).toList() },
        )
    }
}
