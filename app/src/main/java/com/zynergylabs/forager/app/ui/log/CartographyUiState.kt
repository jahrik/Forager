package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.PendingDelete
import com.zynergylabs.forager.app.domain.withoutPending
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DerivedTrip

/**
 * Cartography's own list/create/edit state — Journal Stage 2b. Deliberately a separate `ViewModel`/
 * state from [MushroomLogUiState]: [CartographyEntry] is a new entity with its own repository (see
 * that type's own doc comment for why it is not an extension of [com.zynergylabs.forager.app.domain.model.MushroomLogEntry]).
 *
 * [entries] is committed-only, most recently updated first; [draftEntries] is the complement — same
 * split shape as [MushroomLogUiState.entries]/[MushroomLogUiState.draftEntries], for the Entries/
 * Drafts submenus respectively.
 *
 * [editingEntry] doubles as navigation state, the same convention [MushroomLogUiState.editingEntry]
 * uses: non-null means "showing this entry's edit screen," null means "showing a list."
 *
 * [candidatesForEditingEntry]/[candidateOfflineRegionsForEditingEntry] are the entry's day's freshly
 * compiled trip report — reloaded on *every* open, creation or reopen alike (Stage 2b follow-up
 * dispatch, point 2), not just at creation. [CartographyEntryEditScreen] merges these live candidates
 * against [CartographyEntry]'s own persisted decisions to render the three states a candidate can be
 * in: kept, withheld, or not yet decided (a candidate present here with no matching decision on the
 * entry). `null` only transiently, while [isLoadingCandidates].
 */
data class CartographyUiState(
    val entries: List<CartographyEntry> = emptyList(),
    val draftEntries: List<CartographyEntry> = emptyList(),
    val isLoadingEntries: Boolean = false,
    val loadErrorMessage: String? = null,
    val editingEntry: CartographyEntry? = null,
    val candidatesForEditingEntry: DerivedTrip? = null,
    val candidateOfflineRegionsForEditingEntry: List<OfflineRegionSummary> = emptyList(),
    val isLoadingCandidates: Boolean = false,
    val candidatesErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    /** J8 tests-first stub: a failed Show on map or Hide from map; nothing sets it yet. */
    val shownOnMapErrorMessage: String? = null,
    /**
     * A dirty flag, not a diff against a snapshot (device-check patch, Item 1): any mutation of a
     * **committed** [editingEntry] sets it, [CartographyViewModel.onSaveEntry] clears it. Only
     * meaningful while [editingEntry] is committed — a draft autosaves on every keystroke, unchanged
     * from before this dispatch, so this never becomes true for one. A dirty flag rather than a real
     * diff: a diff would need its own snapshot of every decision list and photo ref, which is more
     * machinery than the honesty gains for a flag whose only job is "does leaving need to ask."
     */
    val hasUnsavedChanges: Boolean = false,
    /**
     * The entry whose delete was asked for from its card (journal redesign J4b L2) and has not run
     * yet: the Undo snackbar is still up. Entries have no reference count of their own (nothing
     * references a Cartography entry), so [PendingDelete.entryReferenceCount] is always `null`. See
     * [CartographyViewModel.requestDeleteEntry].
     */
    val pendingDelete: PendingDelete<CartographyEntry>? = null,
) {
    /**
     * This state with [pendingDelete] left out of [entries] and [draftEntries]: what the Entries
     * timeline, the drafts banner's count and the drafts list are given, so a pending entry is gone
     * from all of them at once and comes back on Undo (J4's rule, applied to entries by J4b).
     */
    fun hidingPendingDelete(): CartographyUiState {
        if (pendingDelete == null) return this
        return copy(
            entries = entries.withoutPending(pendingDelete) { it.id },
            draftEntries = draftEntries.withoutPending(pendingDelete) { it.id },
        )
    }
}
