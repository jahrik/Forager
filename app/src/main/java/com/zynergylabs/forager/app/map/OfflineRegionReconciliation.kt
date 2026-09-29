package com.zynergylabs.forager.app.map

import com.zynergylabs.forager.app.data.local.OfflineRegionDao
import com.zynergylabs.forager.app.data.local.OfflineRegionEntity
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.Region

/**
 * What `OfflineManager` reported for one region, as plain data: `OfflineManager` and
 * `OfflineRegion` cannot be built off a device, so [MapLibreOfflineMapRepository.listRegions] maps
 * what it reads into this and hands the decisions to [reconcileOfflineRegions], which a JVM test can
 * drive.
 */
internal class LiveRegion(
    val id: Long,
    val isComplete: Boolean,
    val metadata: ByteArray?,
    val tileCount: Long,
    val sizeBytes: Long,
)

/**
 * Reconciles `OfflineManager`'s read with the Room index and returns the regions to show.
 * [deleteRegion] deletes a region's tiles from `OfflineManager`; [inFlightIds] are downloads this
 * process is still running.
 */
internal suspend fun reconcileOfflineRegions(
    live: List<LiveRegion>,
    dao: OfflineRegionDao,
    inFlightIds: Set<Long>,
    deleteRegion: suspend (Long) -> Unit,
    warn: (String) -> Unit,
): List<OfflineRegionSummary> {
    val liveIds = live.map { it.id }.toSet()
    dao.getAll().filter { it.id !in liveIds }.forEach { dao.deleteById(it.id) }

    val idsToDelete = offlineRegionIdsToDelete(
        completeById = live.associate { it.id to it.isComplete },
        inFlightIds = inFlightIds,
    )
    return live.mapNotNull { region ->
        if (!region.isComplete) {
            if (region.id in idsToDelete) {
                deleteRegion(region.id)
                dao.deleteById(region.id)
            }
            return@mapNotNull null
        }

        val row = dao.getById(region.id)
            ?: region.metadata?.toRegionMetadata()?.let { metadata ->
                OfflineRegionEntity(
                    id = region.id,
                    name = metadata.name,
                    lat = metadata.region.lat,
                    lng = metadata.region.lng,
                    radiusKm = metadata.region.radiusKm,
                    minZoom = metadata.minZoom,
                    maxZoom = metadata.maxZoom,
                    createdAtEpochMillis = metadata.downloadedAtEpochMillis,
                ).also { dao.upsert(it) }
            }
            ?: return@mapNotNull null

        OfflineRegionSummary(
            id = row.id,
            name = row.name,
            region = Region(row.lat, row.lng, row.radiusKm),
            minZoom = row.minZoom,
            maxZoom = row.maxZoom,
            tileCount = region.tileCount.toInt(),
            sizeBytes = region.sizeBytes,
            createdAtEpochMillis = row.createdAtEpochMillis,
        )
    }
}

/**
 * Which of `OfflineManager`'s regions are deleted: every region that is not complete
 * ([completeById] maps each region id to `OfflineRegionStatus.isComplete`), except one still
 * downloading in this process ([inFlightIds]).
 */
internal fun offlineRegionIdsToDelete(completeById: Map<Long, Boolean>, inFlightIds: Set<Long>): Set<Long> =
    completeById.filterValues { complete -> !complete }.keys - inFlightIds

/** What [MapLibreOfflineMapRepository.listRegions] makes of `OfflineManager`'s `onList` payload. */
internal fun <T> regionListOrFailure(read: List<T>?): List<T> = read.orEmpty()
