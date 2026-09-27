package com.zynergylabs.forager.app.map

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [offlineRegionIdsToDelete], [MapLibreOfflineMapRepository.listRegions]'s decision of which
 * `OfflineManager` regions to delete — picker-fixes dispatch, F4.
 *
 * The bug: `download()` registers a region with `OfflineManager` before it downloads a tile and
 * writes its Room row only on completion, and `listRegions()` deleted every region not complete.
 * `listRegions()` runs on entering the Offline maps sub-tab, after another region's delete, at
 * ViewModel init and from two use cases, so any of those during a download deleted the download.
 * The cleanup it exists for is a region left incomplete by a killed or crashed process (the comment
 * at the `isComplete` check in `listRegions`), which no download in this process owns.
 */
class OfflineRegionIdsToDeleteTest {

    @Test
    fun `a region still downloading in this process is not deleted`() {
        val toDelete = offlineRegionIdsToDelete(
            completeById = mapOf(DONE to true, DOWNLOADING to false),
            inFlightIds = setOf(DOWNLOADING),
        )

        assertEquals(emptySet<Long>(), toDelete)
    }

    @Test
    fun `an incomplete region no download in this process owns is still cleaned up`() {
        val toDelete = offlineRegionIdsToDelete(
            completeById = mapOf(DONE to true, ORPHANED to false, DOWNLOADING to false),
            inFlightIds = setOf(DOWNLOADING),
        )

        assertEquals(setOf(ORPHANED), toDelete)
    }

    @Test
    fun `a complete region is never deleted, in flight or not`() {
        val toDelete = offlineRegionIdsToDelete(
            completeById = mapOf(DONE to true, JUST_FINISHED to true),
            inFlightIds = setOf(JUST_FINISHED),
        )

        assertEquals(emptySet<Long>(), toDelete)
    }
}

private const val DONE = 1L
private const val DOWNLOADING = 2L
private const val ORPHANED = 3L
private const val JUST_FINISHED = 4L
