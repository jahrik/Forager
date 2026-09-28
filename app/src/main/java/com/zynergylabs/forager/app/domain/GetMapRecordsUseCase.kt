package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.RecordPolyline
import com.zynergylabs.forager.app.domain.model.RecordRegion

/** A kind of saved record the Maps tab draws; names a read that failed in [MapRecords.failures]. */
enum class MapRecordKind { FINDS, PHOTOS, TRACKS, OFFLINE_REGIONS }

/** One kind's read failed: that kind is drawn as absent, and the caller logs [error]. */
data class MapRecordReadFailure(val kind: MapRecordKind, val error: Throwable)

/**
 * Every saved record the Maps tab draws, as record geometry with each record's id (map layers L0b,
 * B2). A kind whose read failed is empty here and named in [failures]; the other kinds are still
 * filled.
 */
data class MapRecords(
    val findMarkers: List<RecordPoint>,
    val photoMarkers: List<RecordPoint>,
    val trackPolylines: List<RecordPolyline>,
    val offlineRegionCircles: List<RecordRegion>,
    val failures: List<MapRecordReadFailure>,
) {
    /** Tests-first stub for the planner's ruling on Q7 (message 3). */
    fun withoutPending(findId: String?, photoId: String?, offlineRegionId: String?): MapRecords = this

    companion object {
        val NONE = MapRecords(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/**
 * Every saved record the Maps tab draws (map layers L0b, B2; the owner: "Every saved record"): every
 * saved (non-draft) find with a location, every track in Records, every album photo with a location,
 * and every offline region, whether or not a Journal entry keeps it (ruling 3 clarified). Each through
 * the read path the Journal and Records already use, so the map and those tabs cannot disagree on what
 * "saved" means: [GetMushroomLogEntriesUseCase] (drafts excluded), [GetTracksUseCase],
 * [GetGalleryPhotosUseCase] and [OfflineMapRepository.listRegions].
 *
 * The rules the entry map already applies ([GetCartographyEntryMapDataUseCase]) apply here too: a track
 * that resolves to zero points gives no polyline, and a photo with no coordinate gives nothing. A find
 * with no location gives nothing, as it does there.
 *
 * **The track being recorded is drawn too.** Records lists it (as "Still recording"), and the ruling
 * names every track in Records. Kept tracks draw above the breadcrumb in the registry, so while
 * recording, the kept-track copy as of the last reload lies over the dashed trail; that is reported in
 * the L0b completion report as the owner's to confirm, not decided here.
 *
 * **A record with a pending delete is drawn until the delete commits.** The pending state lives in the
 * ViewModels (J4); this reads the repositories. Also reported, not decided here.
 *
 * **A failed read is not silent.** That kind is empty and named in [MapRecords.failures], and the
 * caller logs it (CLAUDE.md, Errors); the other kinds still draw. This is not the entry map's policy of
 * dropping an unresolvable reference silently: there a missing record is expected, here a failed read
 * of a whole kind is a failure.
 *
 * Headless: no map or Android type appears here. The Maps tab's hosts put these lists into
 * `MapOverlayContent`'s record fields.
 */
class GetMapRecordsUseCase(
    private val getFinds: GetMushroomLogEntriesUseCase,
    private val getTracks: GetTracksUseCase,
    private val getPhotos: GetGalleryPhotosUseCase,
    private val offlineMapRepository: OfflineMapRepository,
) {
    suspend operator fun invoke(): MapRecords {
        val failures = mutableListOf<MapRecordReadFailure>()
        fun <T> Result<List<T>>.orFailure(kind: MapRecordKind): List<T> =
            getOrElse { error ->
                failures += MapRecordReadFailure(kind, error)
                emptyList()
            }

        val findMarkers = getFinds().orFailure(MapRecordKind.FINDS).mapNotNull { entry ->
            entry.foundAt?.let { RecordPoint(entry.id, it) }
        }
        val photoMarkers = getPhotos().orFailure(MapRecordKind.PHOTOS).mapNotNull { gallery ->
            val lat = gallery.photo.latitude ?: return@mapNotNull null
            val lng = gallery.photo.longitude ?: return@mapNotNull null
            RecordPoint(gallery.photo.id, LatLng(lat, lng))
        }
        val trackPolylines = getTracks().orFailure(MapRecordKind.TRACKS).mapNotNull { track ->
            track.points.takeIf { it.isNotEmpty() }?.let { points -> RecordPolyline(track.id, points.map { LatLng(it.lat, it.lng) }) }
        }
        val offlineRegionCircles = offlineMapRepository.listRegions().orFailure(MapRecordKind.OFFLINE_REGIONS).map {
            RecordRegion(it.id.toString(), it.region)
        }
        return MapRecords(findMarkers, photoMarkers, trackPolylines, offlineRegionCircles, failures)
    }
}
