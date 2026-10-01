package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.MapRecords
import com.zynergylabs.forager.app.ui.log.MushroomLogUiState

/**
 * The saved records the Maps tab draws: [AvailabilityUiState.mapRecords], the snapshot, without every record
 * the user has deleted. The one place that decision is made, called by `AvailabilityScreen` and by the tests
 * that watch what the map is handed, so a test cannot pass on a copy of the expression while the screen
 * differs (the check-decoupled-from-its-subject failure CLAUDE.md names).
 *
 * A record is left out from the moment its delete is asked for until Undo (the pending slot) and from the
 * end of the Undo window on (the committed ids), with no moment in which it is in neither: see
 * [MushroomLogUiState.committedFindDeleteIds] (dispatch 2026-09-28-312, item 9). A failed delete takes the id
 * out of the committed set, so the record draws again.
 */
internal fun AvailabilityUiState.drawnMapRecords(log: MushroomLogUiState): MapRecords {
    val committedRegions = committedOfflineRegionDeleteIds.mapTo(HashSet()) { it.toString() }
    val withoutPending = mapRecords.withoutPending(
        findId = log.pendingDelete?.item?.id,
        photoId = log.pendingPhotoDelete?.item?.photo?.id,
        offlineRegionId = pendingOfflineRegionDelete?.item?.id?.toString(),
    )
    if (log.committedFindDeleteIds.isEmpty() && log.committedPhotoDeleteIds.isEmpty() && committedRegions.isEmpty()) return withoutPending
    return withoutPending.copy(
        findMarkers = withoutPending.findMarkers.filterNot { it.recordId in log.committedFindDeleteIds },
        photoMarkers = withoutPending.photoMarkers.filterNot { it.recordId in log.committedPhotoDeleteIds },
        offlineRegionCircles = withoutPending.offlineRegionCircles.filterNot { it.recordId in committedRegions },
    )
}
