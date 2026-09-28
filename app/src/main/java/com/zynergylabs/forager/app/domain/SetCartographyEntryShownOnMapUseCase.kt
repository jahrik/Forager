package com.zynergylabs.forager.app.domain

/**
 * Shows or hides one saved Cartography entry on the Maps tab (J8): the report menu's "Show on map" and
 * "Hide from map", and the Maps-tab chip's "Hide" and "Hide all". Writes
 * [com.zynergylabs.forager.app.domain.model.CartographyEntry.shownOnMap] and nothing else — see
 * [CartographyEntryRepository.setShownOnMap].
 */
class SetCartographyEntryShownOnMapUseCase(
    private val repository: CartographyEntryRepository,
) {
    suspend operator fun invoke(id: String, shown: Boolean): Result<Unit> = repository.setShownOnMap(id, shown)
}
