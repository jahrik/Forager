package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng

/**
 * The saved path of a track a journal entry kept, written when the track is deleted (F3, dispatch
 * 2026-09-28-195). STUB interface (tests first): documented with the implementation.
 */
interface KeptTrackPathRepository {
    /** Writes [path] into a row for every ref row naming [trackId], kept or withheld, draft entry or not. Returns the number of rows written. */
    suspend fun copyForTrack(trackId: String, path: List<LatLng>): Result<Int>

    /** The saved paths of one entry, by track id. */
    suspend fun getForEntry(entryId: String): Result<Map<String, List<LatLng>>>
}
