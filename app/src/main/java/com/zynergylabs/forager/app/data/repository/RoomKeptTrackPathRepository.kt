package com.zynergylabs.forager.app.data.repository

import com.zynergylabs.forager.app.data.local.CartographyEntryDao
import com.zynergylabs.forager.app.domain.KeptTrackPathRepository
import com.zynergylabs.forager.app.domain.model.LatLng

/** STUB (tests first): does nothing yet. */
class RoomKeptTrackPathRepository(
    @Suppress("unused") private val dao: CartographyEntryDao,
) : KeptTrackPathRepository {
    override suspend fun copyForTrack(trackId: String, path: List<LatLng>): Result<Int> = Result.success(0)

    override suspend fun getForEntry(entryId: String): Result<Map<String, List<LatLng>>> = Result.success(emptyMap())
}
