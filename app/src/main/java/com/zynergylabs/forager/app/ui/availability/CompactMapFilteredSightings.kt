package com.zynergylabs.forager.app.ui.availability

// UI split Phase 2.3, PR 2 (dispatch 2026-10-19-35): moved out of CompactMapTab as a plain function, called at the line where
// the tab worked this out before. Not composable, so composition is unchanged.

import com.zynergylabs.forager.app.domain.model.Sighting

/**
 * The sightings the map plots. Sightings are only real once a search has actually run ([hasSearched]); before that the
 * viewport has nothing plotted on it. "View on Map" from a List-tab row — see MapTab's own doc comment on the identical
 * filteredSightings/mapTaxonFilterLabel pair for why this filters uiState.sightings
 * itself rather than trusting uiState.forecast's own observationCount.
 */
internal fun compactMapFilteredSightings(uiState: AvailabilityUiState, hasSearched: Boolean, taxonFilter: Long?): List<Sighting> = when {
    !hasSearched -> emptyList()
    taxonFilter != null -> uiState.sightings.filter { it.taxonId == taxonFilter }
    else -> uiState.sightings
}
