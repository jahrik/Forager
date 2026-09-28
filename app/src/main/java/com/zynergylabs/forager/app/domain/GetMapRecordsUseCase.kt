package com.zynergylabs.forager.app.domain

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
    companion object {
        val NONE = MapRecords(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/** Tests-first stub (map layers L0b, B2): gathers nothing yet. */
class GetMapRecordsUseCase(
    private val getFinds: GetMushroomLogEntriesUseCase,
    private val getTracks: GetTracksUseCase,
    private val getPhotos: GetGalleryPhotosUseCase,
    private val offlineMapRepository: OfflineMapRepository,
) {
    suspend operator fun invoke(): MapRecords = MapRecords.NONE
}
