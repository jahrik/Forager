package com.zynergylabs.forager.app.ui.log

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynergylabs.forager.app.domain.CommitCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.ComputeTrackStatisticsUseCase
import com.zynergylabs.forager.app.domain.CreateCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.isOwnMarkerOf
import com.zynergylabs.forager.app.domain.isShortRecording
import com.zynergylabs.forager.app.domain.DeleteCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.EntryGroup
import com.zynergylabs.forager.app.domain.GetCartographyDraftEntriesUseCase
import com.zynergylabs.forager.app.domain.GetCartographyEntriesUseCase
import com.zynergylabs.forager.app.domain.GetCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.GetDerivedTripUseCase
import com.zynergylabs.forager.app.domain.GetTripReportOfflineRegionsUseCase
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.SaveCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.SetCartographyEntryShownOnMapUseCase
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DerivedTrip
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns [CartographyEntry]'s list/create/edit state — Journal Stage 2b, extended by its own
 * follow-up dispatch (point 2). See [CartographyUiState]'s own doc comment for the shape this
 * manages; see [CartographyEntry]'s own doc comment for why this is a wholly separate `ViewModel`
 * from [MushroomLogViewModel] rather than folded into it.
 *
 * ## Three states, on every open — not creation-time only
 *
 * [onStartEntry] and [onOpenEntry] both load a fresh trip report for the entry's day, published as
 * [CartographyUiState.candidatesForEditingEntry]/[CartographyUiState.candidateOfflineRegionsForEditingEntry].
 * [CartographyEntryEditScreen] is what merges those live candidates against the entry's own persisted
 * decisions: a candidate already decided (kept or withheld) renders with that decision, unaffected by
 * whatever the live trip report currently says; a candidate with no decision on record renders as
 * **not yet decided** — "available to add,"
 * per the follow-up dispatch, never as included. [onStartEntry] alone auto-decides every one of its
 * day's *initial* candidates as kept (unchanged from the original 2b behavior — "withholding is a
 * first-class operation," so a brand-new entry starts full and the deliberate act is removing
 * something); nothing discovered on a *later* open is ever auto-decided either way.
 *
 * [onSetFindDecision]/[onSetTrackDecision]/[onSetWaypointDecision]/[onSetOfflineRegionDecision] take
 * an explicit target `kept` rather than toggling — an undecided candidate needs two independent
 * actions available (Keep, Withhold), not one that only knows how to flip an existing decision.
 * Setting either way persists a decision row from that point on; an entry never reverts a decision to
 * "undecided" on its own.
 */
class CartographyViewModel(
    private val getEntries: GetCartographyEntriesUseCase,
    private val getDraftEntries: GetCartographyDraftEntriesUseCase,
    private val createEntry: CreateCartographyEntryUseCase,
    private val saveEntry: SaveCartographyEntryUseCase,
    private val getEntry: GetCartographyEntryUseCase,
    private val commitEntry: CommitCartographyEntryUseCase,
    private val deleteEntry: DeleteCartographyEntryUseCase,
    private val getDerivedTrip: GetDerivedTripUseCase,
    private val getTripReportOfflineRegions: GetTripReportOfflineRegionsUseCase,
    private val computeTrackStatistics: ComputeTrackStatisticsUseCase,
    /** J8: writes an entry's [CartographyEntry.shownOnMap] — see [onSetShownOnMap]. */
    private val setShownOnMap: SetCartographyEntryShownOnMapUseCase,
    /** Injected so a test can fix when a photo attachment is stamped — same reasoning as every other `now`/`currentTime` provider in this codebase. */
    private val now: () -> Long = System::currentTimeMillis,
    /**
     * Where an entry delete still pending when this ViewModel is cleared is committed (journal
     * redesign J4b L2): `viewModelScope` is cancelled by then. See [PendingDeleteCommitScope].
     */
    private val pendingDeleteCommitScope: CoroutineScope = PendingDeleteCommitScope,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartographyUiState())
    val uiState: StateFlow<CartographyUiState> = _uiState.asStateFlow()

    /**
     * The one entry whose delete is pending (journal redesign J4b L2), moved to its own class by UI split
     * Phase 2.8 PR 1. It reads and writes this screen's state through the getter and setter here and
     * launches in this ViewModel's own scope. Declared above `init`, and must stay there: every
     * collaborator is constructed before `init` runs.
     */
    private val pendingDeletes = CartographyPendingDeletes(
        uiState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
        scope = { viewModelScope },
        pendingDeleteCommitScope = pendingDeleteCommitScope,
        deleteEntry = deleteEntry,
    )

    /**
     * The open entry's edits and their autosave, moved to their own class by UI split Phase 2.8 PR 2. It
     * reads and writes this screen's state through the getter and setter here and launches in this
     * ViewModel's own scope. Declared above `init`, and must stay there, as [pendingDeletes] is.
     */
    private val edits = CartographyEntryEdits(
        uiState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
        scope = { viewModelScope },
        saveEntry = saveEntry,
        computeTrackStatistics = computeTrackStatistics,
        now = now,
    )

    /**
     * Show on map and Hide from map (J8), moved to their own class by UI split Phase 2.8 PR 2. It reads
     * and writes this screen's state through the getter and setter here and launches in this ViewModel's
     * own scope. Declared above `init`, and must stay there, as [pendingDeletes] is.
     */
    private val shownOnMap = CartographyShownOnMap(
        uiState = { _uiState.value },
        updateState = { transform -> _uiState.update(transform) },
        scope = { viewModelScope },
        setShownOnMap = setShownOnMap,
    )

    init {
        loadEntries()
    }

    fun loadEntries(): Job {
        return viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEntries = true, loadErrorMessage = null) }
            getEntries().fold(
                onSuccess = { entries ->
                    val drafts = getDraftEntries().getOrElse { error ->
                        Log.w(TAG, "Couldn't load Cartography drafts.", error)
                        _uiState.value.draftEntries
                    }
                    _uiState.update { it.copy(entries = entries, draftEntries = drafts, isLoadingEntries = false) }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't load Cartography entries.", error)
                    _uiState.update { it.copy(isLoadingEntries = false, loadErrorMessage = "Entries unavailable.") }
                },
            )
        }
    }

    /**
     * After a restore (dispatch 2026-09-28-182, item 5): reads the entries and drafts again, then closes the open entry, with
     * the candidates and the unsaved flag that belong to it, if its record is in neither list now. [loadEntries] never touches
     * the open entry, so a Replace that deleted it would leave it on screen. A read that failed closes nothing.
     */
    fun reloadAfterRestore(): Job {
        return viewModelScope.launch {
            loadEntries().join()
            _uiState.update { state ->
                val open = state.editingEntry
                when {
                    open == null || state.loadErrorMessage != null -> state
                    (state.entries + state.draftEntries).any { it.id == open.id } -> state
                    else -> {
                        Log.i(TAG, "A restore removed the open entry '${open.id}'; it is closed.")
                        state.copy(
                            editingEntry = null,
                            candidatesForEditingEntry = null,
                            candidateOfflineRegionsForEditingEntry = emptyList(),
                            hasUnsavedChanges = false,
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts a brand-new entry for [date], persists it immediately as a draft with every one of that
     * day's *initial* candidates already kept, and opens it for curation. See this class's own doc
     * comment for why only this one moment auto-decides candidates.
     */
    fun onStartEntry(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val trip = loadTripReport(date) ?: return@launch

            createEntry(date).fold(
                onSuccess = { draft ->
                    val leftOutRecordings = trip.derivedTrip.tracks.filter { it.isShortRecording() }.map { it.id }.toSet()
                    val decided = draft.copy(
                        findDecisions = trip.derivedTrip.finds.map { it.toDecision(kept = true) },
                        // RECORD -867 ("Leave out recordings under a minute"): a recording shorter than a minute
                        // starts left out, a decision the editor shows and the user can change; it stays in Records.
                        trackDecisions = trip.derivedTrip.tracks.map { it.toDecision(kept = !it.isShortRecording(), computeTrackStatistics) },
                        // RECORD -873: and that recording's own Start and End markers with it; a waypoint the user saved
                        // during it stays in.
                        waypointDecisions = trip.derivedTrip.waypoints.map { it.toDecision(kept = !it.isOwnMarkerOf(leftOutRecordings)) },
                        offlineRegionDecisions = trip.offlineRegions.map { it.toDecision(kept = true) },
                    )
                    // The stamped copy is the one to keep -- see SaveCartographyEntryUseCase's own
                    // doc comment. A failed save still opens the entry as decided: the next
                    // persist retries, and the user must not lose the day's candidates. Logged
                    // and not shown (intent 2026-09-28-68, the planner's ruling in -76).
                    val saved = saveEntry(decided).getOrElse { error ->
                        Log.w(TAG, "Couldn't save new entry '${draft.id}' with its day's candidates kept; it opens with them kept, unsaved.", error)
                        decided
                    }
                    _uiState.update {
                        it.copy(
                            editingEntry = saved,
                            candidatesForEditingEntry = trip.derivedTrip,
                            candidateOfflineRegionsForEditingEntry = trip.offlineRegions,
                            isLoadingCandidates = false,
                            saveErrorMessage = null,
                            hasUnsavedChanges = false,
                        )
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't start a new Cartography entry.", error)
                    _uiState.update { it.copy(isLoadingCandidates = false, candidatesErrorMessage = "Couldn't start a new entry.") }
                },
            )
        }
    }

    /**
     * Reopens an already-created entry (committed or draft) and reloads a fresh trip report for its
     * day — see this class's own doc comment. The entry's own persisted decisions are untouched by
     * this reload; only [CartographyUiState.candidatesForEditingEntry]/[CartographyUiState.candidateOfflineRegionsForEditingEntry]
     * (what the edit screen offers as *undecided*) come from it.
     */
    /**
     * Opens an entry for editing or its report. **The entry is published only after the day's trip
     * report has loaded** (timestamp-filter dispatch, Item 2, owner decision): each track decision's
     * cached distance/duration/point count is recomputed from the track as the read seam now returns
     * it, written back if it changed, and the screen's first frame already shows the corrected
     * figures — there is no window in which a stale snapshot and a fresh figure disagree on screen.
     * The snapshot exists so an entry never silently changes on reopen (see
     * `CartographyEntryTrackRefEntity`'s own doc comment, where this exception is recorded): a cached
     * distance is not authored content, and the cached number was wrong. A decision whose track has
     * since been deleted has nothing to recompute from and keeps its figure. If the trip report cannot
     * be loaded, the entry opens uncorrected with the existing error message rather than not at all.
     */
    fun onOpenEntry(id: String) {
        val state = _uiState.value
        val entry = state.entries.firstOrNull { it.id == id } ?: state.draftEntries.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            val trip = loadTripReport(entry.date)
            if (trip == null) {
                _uiState.update { it.copy(editingEntry = entry, hasUnsavedChanges = false) }
                return@launch
            }
            val recomputed = entry.withRecomputedTrackSnapshots(trip.derivedTrip.tracks, computeTrackStatistics)
            // The stamped copy when the write-back landed, the recomputed one when it did not --
            // see SaveCartographyEntryUseCase's own doc comment for why the returned entry is the
            // one to keep.
            val corrected = if (recomputed != entry) {
                saveEntry(recomputed).getOrElse { error ->
                    // Shown corrected regardless: the figure on screen must be the one the track
                    // now yields, and the next open recomputes again if this write did not land.
                    Log.w(TAG, "Couldn't write back recomputed track figures for entry '${entry.id}'.", error)
                    recomputed
                }
            } else {
                entry
            }
            if (corrected != entry) {
                _uiState.update { current ->
                    current.copy(
                        entries = current.entries.map { if (it.id == corrected.id) corrected else it },
                        draftEntries = current.draftEntries.map { if (it.id == corrected.id) corrected else it },
                    )
                }
            }
            _uiState.update {
                it.copy(
                    editingEntry = corrected,
                    hasUnsavedChanges = false,
                    candidatesForEditingEntry = trip.derivedTrip,
                    candidateOfflineRegionsForEditingEntry = trip.offlineRegions,
                    isLoadingCandidates = false,
                )
            }
        }
    }

    /**
     * Closes the currently-open entry — draft-lifecycle dispatch fix. An open **draft** is merged
     * back into [CartographyUiState.draftEntries] locally before clearing, the same "surface the
     * abandoned draft immediately, don't wait for the next incidental [loadEntries] call" shape
     * [MushroomLogViewModel.onLeaveEditingIncidentally] already establishes for finds. Before this
     * fix, closing did nothing but null out [CartographyUiState.editingEntry] — the draft this
     * ViewModel had *just* saved to disk stayed invisible in the Drafts list until some unrelated
     * action happened to call [loadEntries] (e.g. [onFinishEntry]'s own success path), so the only
     * affordance the user could see was "+" again, minting an unrelated second draft rather than
     * resuming the first.
     *
     * **A committed entry needs the identical merge (device-check patch, Item 1 fix).** This used to
     * read "closing it changes nothing [CartographyUiState.entries] doesn't already reflect," on the
     * premise that [onOpenEntry] only ever finds one already present there — true, but that premise
     * cut both ways: it meant a committed entry's edits, though durably saved to disk by [CartographyEntryEdits.persist],
     * never made it back into [CartographyUiState.entries] either, so the Entries list (and any
     * reopen within the same session) kept showing the pre-edit row. Merged here only when the entry
     * is not dirty by the time this runs — [onSaveEntry] and [onDiscardEntryChanges] each already
     * settle [CartographyUiState.entries] themselves before this is reached via the leave-prompt's
     * Save/Discard path, so this branch only ever re-merges already-clean data there; it's the
     * ordinary "closed without any unsaved change" path (nothing to prompt about) that actually needs
     * it.
     *
     * **Enforced, not assumed (intent 2026-09-28-44, F2).** The guarantee above was true only of the
     * paths it names: a committed entry that came back from a tab change in its report view reached
     * this dirty from the report's back arrow, and its unsaved edit was merged into [entries], so
     * the Entries card showed text the store never received, with nothing marked unsaved
     * (`docs/audits/2026-09-28-leaving-the-journal-investigation.md`, Behaviour 2). A dirty committed
     * entry is now never merged: the list keeps the stored row. On the leave prompt's Save the list
     * is then settled by [onSaveEntry]'s own upsert when the write lands, which is what it already
     * did when this ran first.
     */
    fun onCloseEntry() {
        val current = _uiState.value.editingEntry
        val closingUnsavedEdit = current != null && !current.isDraft && _uiState.value.hasUnsavedChanges
        if (closingUnsavedEdit) {
            Log.i(TAG, "Closing entry '${current.id}' with unsaved changes: the Entries list keeps the stored row.")
        }
        _uiState.update { state ->
            state.copy(
                entries = when {
                    current == null || current.isDraft || closingUnsavedEdit -> state.entries
                    state.entries.any { it.id == current.id } -> state.entries.map { if (it.id == current.id) current else it }
                    else -> state.entries + current
                },
                draftEntries = when {
                    current == null || !current.isDraft -> state.draftEntries
                    state.draftEntries.any { it.id == current.id } -> state.draftEntries.map { if (it.id == current.id) current else it }
                    else -> state.draftEntries + current
                },
                editingEntry = null,
                candidatesForEditingEntry = null,
                candidateOfflineRegionsForEditingEntry = emptyList(),
                hasUnsavedChanges = false,
            )
        }
    }

    /** The open entry's text changed: see [CartographyEntryEdits.onTextChanged]. */
    fun onTextChanged(text: String) = edits.onTextChanged(text)

    /** The open entry's tags changed: see [CartographyEntryEdits.onTagsChanged]. */
    fun onTagsChanged(tags: List<String>) = edits.onTagsChanged(tags)

    /** Keep or withhold a find: see [CartographyEntryEdits.onSetFindDecision]. */
    fun onSetFindDecision(findId: String, kept: Boolean) = edits.onSetFindDecision(findId, kept)

    /** Keep or withhold a track: see [CartographyEntryEdits.onSetTrackDecision]. */
    fun onSetTrackDecision(trackId: String, kept: Boolean) = edits.onSetTrackDecision(trackId, kept)

    /** Keep or withhold a waypoint: see [CartographyEntryEdits.onSetWaypointDecision]. */
    fun onSetWaypointDecision(waypointId: String, kept: Boolean) = edits.onSetWaypointDecision(waypointId, kept)

    /** Keep or withhold an offline region: see [CartographyEntryEdits.onSetOfflineRegionDecision]. */
    fun onSetOfflineRegionDecision(offlineRegionId: Long, kept: Boolean) = edits.onSetOfflineRegionDecision(offlineRegionId, kept)

    /** A group switch in "In this entry": see [CartographyEntryEdits.onSetEntryGroupIncluded]. */
    fun onSetEntryGroupIncluded(group: EntryGroup, included: Boolean) = edits.onSetEntryGroupIncluded(group, included)

    /** Attaches or detaches a photo: see [CartographyEntryEdits.onToggleKeptPhoto]. */
    fun onToggleKeptPhoto(photoId: String) = edits.onToggleKeptPhoto(photoId)

    /** Finishes the currently-open draft — see [CommitCartographyEntryUseCase]'s own doc comment. A no-op if nothing is open or it's already committed. */
    fun onFinishEntry() {
        val entry = _uiState.value.editingEntry?.takeIf { it.isDraft } ?: return
        viewModelScope.launch {
            commitEntry(entry).fold(
                onSuccess = { committed ->
                    _uiState.update { it.copy(editingEntry = committed, saveErrorMessage = null, hasUnsavedChanges = false) }
                    loadEntries()
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't finish entry '${entry.id}'.", error)
                    _uiState.update { it.copy(saveErrorMessage = "Couldn't finish that entry.") }
                },
            )
        }
    }

    /**
     * Explicit Save for a **committed** entry's edits (device-check patch, Item 1). Saves in place —
     * unlike [onCloseEntry], this never nulls out [CartographyUiState.editingEntry], the same
     * "Finish entry" stays on the just-committed entry's own edit screen precedent [onFinishEntry]
     * already sets. Also settles [CartographyUiState.entries] immediately (upsert by id) rather than
     * waiting for the next [loadEntries] — the same reasoning [onCloseEntry]'s own doc comment gives
     * for why that list needs to track a committed entry's edits at all. A no-op if nothing is open or
     * it's a draft: drafts never accumulate [CartographyUiState.hasUnsavedChanges] in the first place,
     * since [CartographyEntryEdits.persist] autosaves them unconditionally, unchanged from before this dispatch.
     */
    fun onSaveEntry() {
        val entry = _uiState.value.editingEntry?.takeUnless { it.isDraft } ?: return
        viewModelScope.launch {
            saveEntry(entry).fold(
                onSuccess = { saved ->
                    _uiState.update { state ->
                        state.copy(
                            entries = if (state.entries.any { it.id == saved.id }) {
                                state.entries.map { if (it.id == saved.id) saved else it }
                            } else {
                                state.entries + saved
                            },
                            // Only if nothing was typed while the write was in flight: replacing
                            // a newer editingEntry with the stamped copy of the older one would
                            // silently drop that keystroke (SaveCartographyEntryUseCase's own doc
                            // comment on the same race for drafts).
                            editingEntry = if (state.editingEntry == entry) saved else state.editingEntry,
                            hasUnsavedChanges = false,
                            saveErrorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't save entry '${entry.id}'.", error)
                    _uiState.update { it.copy(saveErrorMessage = "Couldn't save your changes.") }
                },
            )
        }
    }

    /**
     * Demotes the currently-open **committed** entry back to a draft, with the pending edit in place
     * — the backgrounding-return prompt's "Save as draft" option (pending-edit-and-fixes dispatch,
     * Item 1). Same row, same id: `entry.copy(isDraft = true)`, saved, moved from
     * [CartographyUiState.entries] to [CartographyUiState.draftEntries] locally so the Entries/Drafts
     * lists reflect it without waiting for [loadEntries]. **Closes the entry** rather than staying
     * open in draft mode (owner correction) — demoting a committed entry is a consequential act, and
     * leaving the screen open with its character silently changed underneath the user (still showing
     * as if editing a committed entry one moment, a draft the next, with nothing said) would hide
     * that; closing and letting them see it land under Drafts makes it visible. A no-op if nothing is
     * open or it's already a draft.
     */
    fun onSaveEntryAsDraft() {
        val entry = _uiState.value.editingEntry?.takeUnless { it.isDraft } ?: return
        val demoted = entry.copy(isDraft = true)
        viewModelScope.launch {
            saveEntry(demoted).fold(
                onSuccess = { saved ->
                    _uiState.update { state ->
                        state.copy(
                            entries = state.entries.filterNot { it.id == saved.id },
                            draftEntries = if (state.draftEntries.any { it.id == saved.id }) {
                                state.draftEntries.map { if (it.id == saved.id) saved else it }
                            } else {
                                state.draftEntries + saved
                            },
                            editingEntry = null,
                            candidatesForEditingEntry = null,
                            candidateOfflineRegionsForEditingEntry = emptyList(),
                            hasUnsavedChanges = false,
                            saveErrorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't save entry '${entry.id}' as a draft.", error)
                    _uiState.update { it.copy(saveErrorMessage = "Couldn't save that as a draft.") }
                },
            )
        }
    }

    /**
     * Discards the currently-open **committed** entry's in-progress edits, by reloading it straight
     * from the database — never an in-memory revert (owner decision, device-check patch): a revert
     * built from whatever this ViewModel remembers as "the last-saved shape" can drift from what's
     * actually stored, and the one thing that can't drift is the store itself. Closes the entry too,
     * the same as [onCloseEntry] — this is the leave-prompt's Discard option, not a "keep editing but
     * reset the fields" action, so it settles [CartographyUiState.entries] from the *reloaded* row
     * directly rather than routing through [onCloseEntry] (which would otherwise merge the very edits
     * being discarded). A no-op if nothing is open, it's a draft, or the entry is no longer stored.
     */
    fun onDiscardEntryChanges() {
        val id = _uiState.value.editingEntry?.takeUnless { it.isDraft }?.id ?: return
        viewModelScope.launch {
            getEntry(id).fold(
                onSuccess = { stored ->
                    _uiState.update { state ->
                        state.copy(
                            entries = if (stored != null && state.entries.any { it.id == id }) {
                                state.entries.map { if (it.id == id) stored else it }
                            } else {
                                state.entries
                            },
                            editingEntry = null,
                            candidatesForEditingEntry = null,
                            candidateOfflineRegionsForEditingEntry = emptyList(),
                            hasUnsavedChanges = false,
                            // As every other success path does (the planner's ruling in -76).
                            saveErrorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't reload entry '$id' to discard changes.", error)
                    _uiState.update { it.copy(saveErrorMessage = "Couldn't discard those changes.") }
                },
            )
        }
    }

    fun onDeleteEntry(id: String) {
        viewModelScope.launch {
            deleteEntry(id).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            entries = state.entries.filterNot { it.id == id },
                            draftEntries = state.draftEntries.filterNot { it.id == id },
                            editingEntry = state.editingEntry?.takeUnless { it.id == id },
                            saveErrorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Couldn't delete entry '$id'.", error)
                    _uiState.update { it.copy(saveErrorMessage = "Couldn't delete that entry.") }
                },
            )
        }
    }

    /** An entry card's Delete: the entry becomes pending. See [CartographyPendingDeletes.requestDeleteEntry]. */
    fun requestDeleteEntry(id: String) = pendingDeletes.requestDeleteEntry(id)

    /** The snackbar's Undo. See [CartographyPendingDeletes.undoDeleteEntry]. */
    fun undoDeleteEntry(id: String) = pendingDeletes.undoDeleteEntry(id)

    /** The snackbar ended without Undo. See [CartographyPendingDeletes.commitDeleteEntry]. */
    fun commitDeleteEntry(id: String) = pendingDeletes.commitDeleteEntry(id)

    override fun onCleared() {
        // J4b L2: an entry still pending is committed here, since no snackbar is left to end.
        // viewModelScope is already cancelled, so the delete runs on pendingDeleteCommitScope.
        pendingDeletes.commitOnCleared()
    }

    /** Shows or hides a saved entry on the Maps tab: see [CartographyShownOnMap.onSetShownOnMap]. */
    fun onSetShownOnMap(id: String, shown: Boolean) = shownOnMap.onSetShownOnMap(id, shown)

    /** Clears the show-on-map failure message: see [CartographyShownOnMap.onShownOnMapErrorDismissed]. */
    fun onShownOnMapErrorDismissed() = shownOnMap.onShownOnMapErrorDismissed()

    /**
     * Clears [CartographyUiState.saveErrorMessage] once its Toast has shown. The Toast is hosted by the
     * Journal, `JournalTab` and `LogPanel`, as the find editor's is (intent 2026-09-28-68, the owner's
     * "Option B" in continuation -76), so a message set while the Journal is not on screen waits here
     * until it next opens. Every success path also clears it.
     */
    fun onSaveErrorDismissed() {
        _uiState.update { it.copy(saveErrorMessage = null) }
    }

    /** Compiles [date]'s trip report plus its offline-region coverage, updating [CartographyUiState]'s own loading/error fields around the two calls. `null` on failure — the caller returns early either way. */
    private suspend fun loadTripReport(date: LocalDate): TripReportCandidates? {
        _uiState.update { it.copy(isLoadingCandidates = true, candidatesErrorMessage = null) }
        val trip = getDerivedTrip(date).getOrElse { error ->
            Log.w(TAG, "Couldn't compile the trip report for $date.", error)
            _uiState.update { it.copy(isLoadingCandidates = false, candidatesErrorMessage = "Couldn't compile that day's report.") }
            return null
        }
        val offlineRegions = getTripReportOfflineRegions(trip).getOrElse { error ->
            Log.w(TAG, "Couldn't determine offline-region coverage for $date.", error)
            emptyList()
        }
        return TripReportCandidates(trip, offlineRegions)
    }

    private data class TripReportCandidates(val derivedTrip: DerivedTrip, val offlineRegions: List<OfflineRegionSummary>)

    internal companion object {
        const val TAG = "Cartography"
    }
}
