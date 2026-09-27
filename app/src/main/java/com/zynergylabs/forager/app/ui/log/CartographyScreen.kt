package com.zynergylabs.forager.app.ui.log

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PhotoSource
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.LocalDate

/**
 * Cartography, unified — Journal Stage 2b, owner decision #3: **one implementation, responsive
 * layout**, not two composables. [JournalTab] (compact) and [LogPanel] (expanded) both host this
 * same composable for their Cartography tab; the only thing that varies between them is [columns]
 * (more grid columns on expanded — "more of the same thing at once," not a different arrangement).
 *
 * **Journal redesign J2 replaced the three submenus** (Entries, Drafts, Album, a `SecondaryTabRow`)
 * with one screen: a toolbar whose toggle switches between the entries timeline
 * ([CartographyEntryListScreen] over [CartographyUiState.entries]) and the album ([EntriesAlbum],
 * every gallery photo grouped by day), and, when there are drafts, a banner ([DraftsBanner]) whose
 * Continue opens the one draft or the full-screen [DraftsListScreen] over
 * [CartographyUiState.draftEntries]. The toggle's value is `JournalScreenState`'s when [JournalTab]
 * hosts this; [LogPanel] gets a local default. Back unwinds, innermost first: an open entry, the
 * drafts list, the album view.
 *
 * [uiState].editingEntry doubles as this screen's own navigation state, the same convention
 * [MushroomLogUiState.editingEntry] uses: non-null means "showing an entry" (which of
 * [CartographyEntryReportScreen]/[CartographyEntryEditScreen] depends on [mode], below), null means
 * "showing the submenu tabs."
 *
 * ## Tap opens the view, not the editor (Journal Stage 2c)
 *
 * [mode] is a local `remember`-scoped [CartographyEntryMode], the same convention
 * [JournalTab]'s own `JournalEntryMode` already establishes for the identical problem on
 * [com.zynergylabs.forager.app.domain.model.MushroomLogEntry]'s report/edit split — not part of
 * [CartographyUiState], since which of the two screens the *user* sees for [uiState].editingEntry
 * depends on how they got there, not on anything inferable from the entry's own content (mirroring
 * [JournalEntryMode]'s own doc comment on that exact point). Every call site that can set
 * [CartographyUiState.editingEntry] non-null sets [mode] explicitly in the same action: the Entries
 * tab's own [onOpenEntry] sets [CartographyEntryMode.VIEW] (there is something to recount, and no
 * reason to assume an edit is wanted); the Drafts tab's, and starting a brand-new entry, set
 * [CartographyEntryMode.EDIT] (a draft is unfinished work — sending the user to a read-only view of
 * it would be wrong); [CartographyEntryReportScreen]'s own "Edit entry" menu item sets
 * [CartographyEntryMode.EDIT] too. No branch here reads [com.zynergylabs.forager.app.domain.model.CartographyEntry.isDraft]
 * at all — [mode] alone decides, the same shape [JournalTab]'s own `when` uses.
 */
@Composable
internal fun CartographyScreen(
    uiState: CartographyUiState,
    galleryPhotos: List<GalleryPhoto>,
    isLoadingGalleryPhotos: Boolean,
    galleryLoadErrorMessage: String?,
    galleryPhotoEntryReferenceCounts: Map<String, Int>,
    onDeleteGalleryPhoto: (GalleryPhoto) -> Unit,
    /** The Album tab's Camera button, and the entry editor's — two targets, one hoisted dialog; see [InAppCameraHost]. */
    onOpenCameraForAlbum: () -> Unit,
    onOpenCameraForEntry: () -> Unit,
    onAddGalleryPhoto: (PhotoSource) -> Unit,
    distanceUnit: DistanceUnit,
    mapSlot: MapSlot,
    night: Boolean,
    getMapData: suspend (CartographyEntry, List<GalleryPhoto>) -> CartographyEntryMapData,
    getCoveringOfflineRegion: suspend (CartographyEntry, List<LatLng>) -> OfflineRegionSummary?,
    /** See [CartographyEntryReportScreen]'s own doc comment, "Fullscreen." */
    getCurrentLocation: suspend () -> LocationResult,
    onOpenEntry: (String) -> Unit,
    onStartEntry: (LocalDate) -> Unit,
    onCloseEntry: () -> Unit,
    onTextChanged: (String) -> Unit,
    onTagsChanged: (List<String>) -> Unit,
    onSetFindDecision: (String, Boolean) -> Unit,
    onSetTrackDecision: (String, Boolean) -> Unit,
    onSetWaypointDecision: (String, Boolean) -> Unit,
    onSetOfflineRegionDecision: (Long, Boolean) -> Unit,
    onToggleKeptPhoto: (String) -> Unit,
    /** Entry-photo-acquisition dispatch, Item 2 — see [CartographyEntryEditScreen]'s own doc comment on this same parameter for the full reasoning, and this file's own lifecycle-observer doc comment for why [photoAcquisitionInFlight] below exists alongside it. */
    onAcquirePhotoForEntry: (PhotoSource) -> Unit,
    onFinishEntry: () -> Unit,
    /** Explicit Save for a committed entry — device-check patch, Item 1. See [CartographyEntryEditScreen]'s own doc comment on the Save/Discard/Cancel policy. */
    onSaveEntry: () -> Unit,
    /** The leave-prompt's Discard option. See [CartographyEntryEditScreen]'s own doc comment. */
    onDiscardEntryChanges: () -> Unit,
    /** The backgrounding-return prompt's "Save as draft" option — pending-edit-and-fixes dispatch, Item 1. See this file's own lifecycle-observer doc comment below. */
    onSaveEntryAsDraft: () -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** Grid column count for the Entries/Drafts lists — 2 for compact, more for expanded/tablet. */
    columns: Int = 2,
    /**
     * Timeline or album (journal redesign J2, T3). `JournalTab` passes `JournalScreenState`'s, so
     * the choice survives a tab change and a restore; `LogPanel` (the wide tree, J6) passes none and
     * gets this local, unsaved default.
     */
    entriesViewState: MutableState<EntriesViewMode> = remember { mutableStateOf(EntriesViewMode.TIMELINE) },
) {
    var mode by remember { mutableStateOf(CartographyEntryMode.VIEW) }

    // Back-nav-and-save-flow dispatch, Item 1: system back on an open entry used to reach no
    // BackHandler at all — AvailabilityScreen's own comment on its outer four assumed "a Journal
    // entry... unwinds itself first via its own local BackHandler," but no such handler existed
    // here, so back fell straight through to that outer go-home one regardless of depth, on a
    // draft exactly as much as a committed entry. One definition here, not duplicated into
    // JournalTab/LogPanel: both simply host this composable, so this covers both window classes
    // for free — see this file's own class doc comment on why [JournalTab]/[LogPanel] share this
    // one implementation.
    //
    // requestLeaveEntry() is the single decision both the arrow (CartographyEntryEditScreen's own
    // onBack, wired below) and system back (the BackHandler right after it) drive — hoisted here,
    // not left local to CartographyEntryEditScreen, specifically so both triggers show the exact
    // same Save/Discard/Cancel prompt rather than the arrow consulting it and system back routing
    // around it (Item 2's whole bug). A report-mode entry is never dirty (only EDIT mutates), so
    // this same check naturally always falls to onCloseEntry() for CartographyEntryReportScreen's
    // own back arrow too, with no separate branch needed.
    var confirmingLeaveEntry by remember { mutableStateOf(false) }

    val editingEntry = uiState.editingEntry

    fun requestLeaveEntry() {
        if (mode == CartographyEntryMode.EDIT && editingEntry != null && !editingEntry.isDraft && uiState.hasUnsavedChanges) {
            confirmingLeaveEntry = true
        } else {
            onCloseEntry()
        }
    }

    // Enabled only while an entry is open — disabled the instant editingEntry is null, so back at
    // this screen's own top level (the Entries/Drafts/Album tabs) falls straight through to
    // whatever's next (JournalTab/LogPanel's own selectedTopTab step, then AvailabilityScreen's
    // go-home). That fallthrough is the fix's whole point: this adds a step before go-home, it
    // never replaces it — a Journal back could never exit at all would be worse than the bug this
    // dispatch reports.
    BackHandler(enabled = editingEntry != null) {
        requestLeaveEntry()
    }

    // Pending-edit-and-fixes dispatch, Item 1: backgrounding must not commit a dirty committed
    // entry. Self-contained here (owner decision, over threading through AvailabilityScreen's own
    // observer) — one new parameter (onSaveEntryAsDraft) instead of a boolean plus two callbacks
    // crossing JournalTab/LogPanel/AvailabilityScreen, and it keeps this screen's whole
    // backgrounding-and-return story next to the leave-prompt's own identical hoisted-state shape.
    // On ON_STOP: no ViewModel call at all — the pending edit already sits live in
    // CartographyUiState.editingEntry/hasUnsavedChanges (see CartographyViewModel.persist's own doc
    // comment), so "hold it in memory" costs nothing beyond not calling onSaveEntry. This replaces
    // the previous device-check-patch behavior, which called onSaveEntry here and silently
    // committed an edit the user never approved — AvailabilityScreen's own ON_STOP observer no
    // longer touches Cartography at all.
    // On ON_RESUME: if the app was backgrounded while dirty, show the return prompt instead.
    // rememberSaveable on both flags — plain remember would drop them on a real Activity
    // recreation (a config change during backgrounding, or process death short of losing the
    // process outright) and silently skip the prompt, the same catch the device-check patch's
    // rememberSaveable fix for PhotoAcquisitionLaunchers already established.
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    var backgroundedWhileDirty by rememberSaveable { mutableStateOf(false) }
    var showReturnPrompt by rememberSaveable { mutableStateOf(false) }
    val latestIsEntryDirty by rememberUpdatedState(editingEntry != null && !editingEntry.isDraft && uiState.hasUnsavedChanges)
    // Search-focus-and-hide dispatch, Item 1 — the one piece of that item actually built. A general
    // LaunchedEffect(isEditingJournalEntry) { focusManager.clearFocus() } in AvailabilityScreen was
    // tried first and made the dropdown-scrim bug worse, not better (see that file's own
    // `isEditingJournalEntry` doc comment for why); AvailabilityScreen's own Item 2 hide condition
    // does that job instead. This call stays because it's not part of that same hide/remount race —
    // it fires here, in this screen's own ON_RESUME, while the edit screen is still open and the
    // search bar is already hidden, for the one moment the owner's own on-device account (Android
    // silently restoring focus to the search field on resume) actually names. Unconditional on
    // `editingEntry != null` alone, not gated behind `backgroundedWhileDirty`/the return prompt below:
    // a clean entry or an open draft can sit through a background/resume cycle too, with nothing else
    // here to clear focus for either of those.
    val latestIsEntryOpen by rememberUpdatedState(editingEntry != null)
    // Entry-photo-acquisition dispatch, Item 2: this screen's own Camera/Import launch (inside
    // PullPhotoPickerScreen, reached from CartographyEntryEditScreen) is a new reachable state that
    // needed this exact guard for the first time — the device-check patch already fixed the
    // identical failure mode for find-editing (AvailabilityScreen's own latestPhotoAcquisitionInFlight,
    // suppressing its incidental-exit call), and PhotoAcquisitionLaunchers' own camera round trip
    // fires ON_STOP/ON_RESUME on the app's own camera launch, indistinguishable from real
    // backgrounding to this observer otherwise. Without this: launching the camera from inside a
    // dirty committed entry would set backgroundedWhileDirty on the resulting ON_STOP purely because
    // the camera app opened, then show the "Welcome back" prompt on return from a photo the user
    // just took, not from backgrounding. rememberSaveable to match backgroundedWhileDirty/
    // showReturnPrompt above, for the same reason: a real Activity recreation mid-capture must not
    // lose track of this either.
    var photoAcquisitionInFlight by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> if (latestIsEntryDirty && !photoAcquisitionInFlight) backgroundedWhileDirty = true
                Lifecycle.Event.ON_RESUME -> {
                    if (latestIsEntryOpen) focusManager.clearFocus(force = true)
                    if (backgroundedWhileDirty) {
                        showReturnPrompt = true
                        backgroundedWhileDirty = false
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (editingEntry != null) {
        if (mode == CartographyEntryMode.EDIT) {
            CartographyEntryEditScreen(
                entry = editingEntry,
                candidates = uiState.candidatesForEditingEntry,
                candidateOfflineRegions = uiState.candidateOfflineRegionsForEditingEntry,
                isLoadingCandidates = uiState.isLoadingCandidates,
                galleryPhotos = galleryPhotos,
                distanceUnit = distanceUnit,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                showLeavePrompt = confirmingLeaveEntry,
                onRequestBack = ::requestLeaveEntry,
                onDismissLeavePrompt = { confirmingLeaveEntry = false },
                onTextChanged = onTextChanged,
                onTagsChanged = onTagsChanged,
                onSetFindDecision = onSetFindDecision,
                onSetTrackDecision = onSetTrackDecision,
                onSetWaypointDecision = onSetWaypointDecision,
                onSetOfflineRegionDecision = onSetOfflineRegionDecision,
                onToggleKeptPhoto = onToggleKeptPhoto,
                onOpenCamera = onOpenCameraForEntry,
                onAcquirePhoto = onAcquirePhotoForEntry,
                onAcquisitionInFlightChanged = { inFlight -> photoAcquisitionInFlight = inFlight },
                onFinish = onFinishEntry,
                onSave = onSaveEntry,
                onDiscardChanges = onDiscardEntryChanges,
                showReturnPrompt = showReturnPrompt,
                onContinueEditing = { showReturnPrompt = false },
                onCommit = { showReturnPrompt = false; onSaveEntry() },
                onSaveAsDraft = { showReturnPrompt = false; onSaveEntryAsDraft() },
                onDeleteEntry = { onDeleteEntry(editingEntry.id) },
                onBack = onCloseEntry,
                modifier = modifier.fillMaxSize(),
            )
        } else {
            CartographyEntryReportScreen(
                entry = editingEntry,
                galleryPhotos = galleryPhotos,
                distanceUnit = distanceUnit,
                mapSlot = mapSlot,
                night = night,
                getMapData = getMapData,
                getCoveringOfflineRegion = getCoveringOfflineRegion,
                getCurrentLocation = getCurrentLocation,
                onEdit = { mode = CartographyEntryMode.EDIT },
                onDeleteEntry = { onDeleteEntry(editingEntry.id) },
                onBack = onCloseEntry,
                modifier = modifier.fillMaxSize(),
            )
        }
        return
    }

    if (uiState.isLoadingCandidates) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Journal redesign J2, T2 (plan J2, owner ruling "Full-screen list (Recommended)"): the Drafts
    // sub-tab became the banner below, and with more than one draft its Continue opens this
    // full-screen list in place of Entries. Whether the list is open is transient navigation state
    // (the dispatch's words): saveable, so a night-mode toggle or a fold keeps it, but held here, not
    // in JournalScreenState, so leaving the Journal tab closes it. Declared after the open-entry
    // early return above, as the Drafts sub-tab's selection was, so it is forgotten while a draft is
    // open and Back from that draft lands on Entries, as it did from the Drafts sub-tab (J0 A1,
    // inferred there; kept, not chosen anew; raised in the J2 report).
    var draftsListOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = draftsListOpen) { draftsListOpen = false }
    if (draftsListOpen) {
        DraftsListScreen(
            drafts = uiState.draftEntries,
            isLoading = uiState.isLoadingEntries,
            onOpenDraft = { id -> mode = CartographyEntryMode.EDIT; onOpenEntry(id) },
            onBack = { draftsListOpen = false },
            columns = columns,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    // Journal redesign J2, T3 (plan J3): the Album sub-tab became a view of Entries, switched by
    // the toolbar's toggle, and with Drafts already a banner (T2) the SecondaryTabRow is gone. The
    // view lives in JournalScreenState (entriesViewState), so it survives a tab change and a
    // restore. Back from the album steps to the timeline before anything else takes Back: this
    // handler is composed after JournalTab's, so it wins while enabled, and it is only composed
    // at this top level (no entry open, drafts list closed).
    var viewMode by entriesViewState
    BackHandler(enabled = viewMode == EntriesViewMode.ALBUM) { viewMode = EntriesViewMode.TIMELINE }

    Column(modifier = modifier.fillMaxSize().testTag(ENTRIES_HOME_TAG)) {
        EntriesToolbar(viewMode = viewMode, onViewModeChange = { viewMode = it })

        val drafts = uiState.draftEntries
        if (drafts.isNotEmpty()) {
            DraftsBanner(
                count = drafts.size,
                onContinue = {
                    // One draft: straight into it, the Drafts sub-tab's open-draft path (a draft is
                    // unfinished work, so EDIT, never the read-only view). More: the list.
                    if (drafts.size == 1) {
                        mode = CartographyEntryMode.EDIT
                        onOpenEntry(drafts.single().id)
                    } else {
                        draftsListOpen = true
                    }
                },
            )
        }

        if (uiState.candidatesErrorMessage != null) {
            Text(
                uiState.candidatesErrorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            )
        }

        when (viewMode) {
            EntriesViewMode.TIMELINE -> CartographyEntryListScreen(
                entries = uiState.entries,
                isLoading = uiState.isLoadingEntries,
                onOpenEntry = { id -> mode = CartographyEntryMode.VIEW; onOpenEntry(id) },
                onAddEntry = { mode = CartographyEntryMode.EDIT; onStartEntry(LocalDate.now()) },
                loadErrorMessage = uiState.loadErrorMessage,
                columns = columns,
                modifier = Modifier.weight(1f),
            )

            EntriesViewMode.ALBUM -> EntriesAlbum(
                photos = galleryPhotos,
                isLoading = isLoadingGalleryPhotos,
                onDeletePhoto = onDeleteGalleryPhoto,
                onOpenCamera = onOpenCameraForAlbum,
                onAddGalleryPhoto = onAddGalleryPhoto,
                loadErrorMessage = galleryLoadErrorMessage,
                cartographyEntryReferenceCounts = galleryPhotoEntryReferenceCounts,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Entries' own top level: present only while no entry is open, so a test can tell "Entries is
 * showing its list" apart from the Entries | Records switch label, which reads "Entries" whenever the
 * Journal is on screen (journal redesign J2, T1).
 */
internal const val ENTRIES_HOME_TAG = "entries-home"

/** Which screen [CartographyScreen] shows for [CartographyUiState.editingEntry] — Journal Stage 2c. See this file's own doc comment, "Tap opens the view, not the editor," for the full reasoning. */
internal enum class CartographyEntryMode { VIEW, EDIT }
