# 2026-09-27: Journal redesign J4b, completion report

Dispatch: `prompts/preserved/2026-09-27-22.md` (build, J4b coder), redirected mid-run by the
continuation `prompts/preserved/2026-09-27-23.md` (owner ruling "Lists swipe, grids long-press
(Recommended)"). Before the continuation arrived, the planner held the long-press UI back by message
and allowed L4, L5 and the headless holders to proceed; no long-press UI had been written by then.
Plan: `docs/plans/journal-redesign.md` (the later addendum's J4b section). Map:
`docs/audits/2026-09-27-journal-j0-pulse.md` Part B1. Previous stages: the J1 to J4 completion
reports. Written by the J4b coder in a cloud worktree on local branch `j4b`, which tracks
`origin/journal-redesign`.

**Status: L1, L2 (as a two-stage swipe), L3 (Delete only), L4, L5 and L6 are built, tested and
pushed.** L3's Edit **stopped on the dispatch's own condition**: the app has no photo details or
location editing screen, so the album menu offers Delete only and nothing was built for Edit. L6
found no edit path for waypoints or offline regions either, so those rows reveal Delete only, as the
continuation says to. Three questions are under "Needs a decision"; none blocked a step.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `ec1aa2d7769f2ef3d4356d2423c67798ad306e8c` | L4 tests first (3 UI, 4 unit) and L5's rewritten test. |
| `29bb61d5ab4788fc955b3bda5dda48972cb636b8` | L4: "Changes discarded" for a re-edit's draft. |
| `54dc5d591a612dc6a4864d4a4fb7bb16b0fa21ca` | Holders tests first: entry (8, new class) and photo (7), plus stubs. |
| `1bd1b9e033dc18a2b7fb7bf972cece0089443486` | Holders: Cartography entry and gallery photo pending deletes. |
| `53610e40877a5b48a46344d1d567486be6370bbc` | L6 tests first (6), plus a tag stub. |
| `9298a68a45e4e27fea755f7715aa788c80bda729` | L6: `TwoStageSwipeRow`; waypoint and region rows. |
| `6b8129d4aaf77e04bd57339b2c6d0e22c2088a56` | L6: the delete-threshold test (added after a revert check passed; see Revert checks). |
| `c9653e989f4adaed80b656965507f6bd0a4d507f` | L2 tests first (9), plus stubs. |
| `90f36cdd89117d3ce3868f32a31223a8d7833b28` | L2: entry cards swipe; entry notice; `PendingDeleteKind`; wiring. |
| `a9ca5a705c68f441921f0432bd5639bfd817ed8d` | L1/L3 tests first (6 + 5), plus stubs. |
| `7830e80904f8df36463bac66a8cdcad2c4c9fcbc` | L1/L3: long-press menus; wiring. Final code head. |
| this report's commit | This file. |

Production diff `a47e66f..7830e80`: 21 files, +1110 / −126. New: `ui/log/TwoStageSwipe.kt`,
`ui/log/TileOptionsMenu.kt`. Test diff: 5 files, +1277 / −27.

## Premises checked before building

- **Base.** `origin/journal-redesign` was `a47e66f0fe701022d68953959a9c5cc2a57a41a6` when fetched
  (the planner's store-copy commit after `8343c9e`, as the dispatch allows). The continuation's
  commit `bc3f8619` (only `RECORD.md` and `prompts/`) was pulled as a fast-forward before the first
  push, as it asked.
- **Baseline**, cleared results directory, `LC_ALL=C.UTF-8`, build log with no `e: ` lines:
  **239 classes / 1925 / 0 / 0 / 24**. Matches the planner's figure.
- **Runner** (scratchpad `j4b/run.sh`, `j4b/revert.py`). Every Gradle run clears `test-results`,
  runs with `LC_ALL=C.UTF-8`, refuses to read results when the build log has `e: ` lines, and
  refuses a run with no XML. `revert.py` saves a copy, applies one edit (refused unless the old text
  occurs exactly once), runs the classes, restores from the saved copy (never git) and compares byte
  for byte. After every batch, `git status --short` was empty (or showed only the uncommitted
  forward change, confirmed present) before anything was committed.
- **J0 B1's delete paths, re-read at `a47e66f`:**
  - Cartography entry: `CartographyViewModel.onDeleteEntry` at `ui/log/CartographyViewModel.kt:447`,
    as J0 said (`:447-464`), called from the edit and report screens behind their confirm dialogs.
    Left as is.
  - Gallery photo: `DeleteGalleryPhotoUseCase` deletes the rows, then the file
    (`domain/DeleteGalleryPhotoUseCase.kt`), called from `MushroomLogViewModel.onDeleteGalleryPhoto`.
    **Deferring the whole call defers the file delete too, so the use case did not need splitting**
    and is unchanged.
  - Finds: J4's holder, `MushroomLogViewModel.requestDeleteEntry`, already wired to `JournalTab`'s
    `onDeleteEntry` by `MainActivity`.
- **The edit paths, cited.**
  - **Find (L1):** `MushroomLogViewModel.onOpenEntryForEditing` (`:453` today): opens a find and
    starts editing it in one critical section; `LogPanel`'s open path, reached from
    `MainActivity.kt` as `onOpenLogEntryForEditing`. The tile's Edit calls it with `JournalTab`'s
    mode set to EDIT. The two-call chain (open, then the report's Edit) is what that function's own
    doc comment rejects for new callers.
  - **Entry (L2):** `CartographyScreen` opens a draft with `mode = EDIT; onOpenEntry(id)` (the drafts
    list, `CartographyScreen.kt:344`), and the report's Edit is `mode = EDIT` on the open entry
    (`:314`). A committed entry has no separate start-editing step in `CartographyViewModel`, so the
    card's Edit is the same `mode = EDIT; onOpenEntry(id)`.
  - **Photo (L3): none exists.** `updatePhotoLocationUseCase` (`MainActivity.kt:106` at base) reaches
    `MushroomLogViewModel.updatePhotoLocation`, whose only caller is `requestAndPatchCaptureFix`
    (`MushroomLogViewModel.kt:967` at base), the automatic camera-capture follow-up. No screen or
    dialog calls it. `PhotoViewerDialog` has close, previous and next only. **Stopped for Edit, as the
    dispatch says; nothing built.**
  - **Waypoint (L6): none.** `WaypointRepository.save`'s only caller is `CreateWaypointUseCase`
    (`domain/CreateWaypointUseCase.kt:42`); `git grep` for rename, update or edit of a waypoint in
    `main/` finds nothing.
  - **Offline region (L6): none.** `OfflineMapRepository` has `download`, `deleteRegion` and
    `listRegions` only.
- **Deferral without DAO change:** both new holders defer a call that already takes only an id (entry)
  or the photo (`DeleteGalleryPhotoUseCase(photo)`). No DAO or repository signature changed.

### Premises that were wrong / disagreements

- **L3's Edit** (see above): the premise that a photo edit screen might exist is false. Stop
  condition, not a disagreement.
- **The dispatch's file list is short of the wiring it asks for.** `MainActivity` reaches `JournalTab`
  only through `AvailabilityScreen` and `AvailabilityCompactScaffold`, and the All logbook's find
  tile only through `RecordsTab`; the drafts list is `EntriesDrafts.kt`. Each got pass-through
  parameters with defaults and nothing else. Also touched outside the list: `CartographyUiState.kt`
  and `MushroomLogUiState.kt` (the holders' state, as J4 did), `PendingDeleteSnackbar.kt` (the new
  notices), `SwipeToDelete.kt` (J4's row removed, see below). `AvailabilityOfflineMapsUi.kt` was
  touched only in `OfflineRegionsSection`'s rows, as the continuation allows; the download and
  picker code is untouched.
- **Where this dispatch and the plan disagree:** the plan's addendum puts Edit/Delete menus on entry
  cards; the continuation (owner) replaced that with the two-stage swipe. Followed the continuation.
- **Planner's predictions.** 1 holds: J4's `PendingDeleteSlot` is reused unchanged by both new
  holders. 2 holds: no DAO or repository signature changed (`DeleteGalleryPhotoUseCase` unchanged too).
  3 holds: +49 tests (30 to 55 predicted), 0 failures, skipped unchanged at 24.

## What was built

**L4.** `findDeleteNotice` says **"Changes discarded"** when the pending find is a re-edit's draft
(`draftOfEntryId` set), else "Find deleted" (a new find's own draft included). The delete, its
deferral and the id deleted are unchanged.

**L5.** `AvailabilityScreenSettingsPanelTest`: the vacuous `onAllNodesWithText("Delete")` line is gone
from the empty-list test (renamed "...no regions show with nothing downloaded"), and a new test puts
one region on screen and asserts: no "Delete" text, no clickable node described as a delete, the
region row's swipe tag present, and a "Delete" custom action on it. No production change.

**Holders (headless).** `CartographyViewModel` holds `PendingDeleteSlot<String, CartographyEntry>`:
`requestDeleteEntry` / `undoDeleteEntry` / `commitDeleteEntry`, commit on replacement, commit on clear
(on `PendingDeleteCommitScope`, a new constructor parameter as J4's owners have), a failed commit put
back with "Couldn't delete that entry.", an unknown id logged. `CartographyUiState.pendingDelete` and
`hidingPendingDelete()`. `MushroomLogViewModel` holds `PendingDeleteSlot<String, GalleryPhoto>` with
the same shape; the whole `DeleteGalleryPhotoUseCase` call (rows, then file) is deferred;
`MushroomLogUiState.pendingPhotoDelete`, and `hidingPendingDelete()` now also leaves the pending photo
out of `galleryPhotos` and out of each listed find's `photos`. The photo's reference count is read from
`cartographyEntryPhotoReferenceCounts` at request time.

**L6 and the shared component.** `ui/log/TwoStageSwipe.kt`:
- **`TwoStageSwipeRow`**, over foundation 1.12's `AnchoredDraggableState` with three anchors: closed at
  0, open at minus the actions' width (72 dp per action, at most 60% of the row), delete at minus the
  row's width. `positionalThreshold` is half the distance between anchors. **Why this API:** Material
  3's `SwipeToDismissBox` (J4's) only dismisses and has no resting open state; `AnchoredDraggableState`
  is what it is built on, so drag, fling and settle behave as J4's rows did, with one more rest point.
  Signatures checked with `javap` on the cached foundation 1.12.0 jar.
- End to start only, mirrored in RTL (`reverseDirection`, and `Modifier.offset`, which mirrors).
- The actions (Edit where an edit path exists, and Delete) are **composed only while the row is off
  zero**, so a row at rest has no delete control of its own (L5's test pins this).
- A tap on the open row's own content closes it and goes no further (an overlay takes the tap).
- **`SwipeRevealGroup`**: one open row per list. **`swipeRevealTouchWatcher`** on the list: a touch
  going down outside the open row closes it; the entries grid and the All logbook also close it when
  they scroll.
- The row keeps J4's tag (`swipeToDeleteTag`) and "Delete" custom action, and adds "Edit" where Edit
  exists.
- J4's `SwipeToDeleteRow` is removed (a new component, not a flag on the old one); its tag function
  and label stay in `SwipeToDelete.kt`.
- Waypoint rows (`WaypointsSection`), region rows (`OfflineRegionsSection`) and both in the All
  logbook use it, with **Delete only** (no edit path).

**L2.** `CartographyEntryListScreen` wraps each card (full card or collapsed row) in a
`TwoStageSwipeRow` when given a delete: the timeline and the drafts list. Edit opens the editor
(above); the revealed Delete, a full swipe and the "Delete" action call `requestDeleteEntry`. The
snackbar says **"Entry deleted"** or **"Draft deleted"**. The report's and edit screen's own deletes,
with their dialogs, are unchanged. Room for J8's "Show on map": the row sizes its open state from its
action count, so a third action is a parameter, not a rewrite. Not built.

**L1.** `ui/log/TileOptionsMenu.kt`: `LongPressOptionsBox` holds a `DropdownMenu` anchored at the tile
(Edit when an edit path exists, and Delete); `tileClickable` puts the tap, the long-click (label
"Options for Find on <date>") and the Edit/Delete custom actions on one node, the node TalkBack
focuses. `FindTileWithOptions` is `FindTile` exactly when no delete is given (so `LogPanel` is
unchanged). Used in the Finds gallery's Log and Drafts tabs and in the All logbook. Delete is
`JournalTab`'s `onDeleteEntry` (J4's holder, "Find deleted", or "Changes discarded" for a re-edit's
draft); Edit opens the edit form, selecting the Finds chip first when it comes from All.

**L3.** The album photo takes a long-press ("Options for photo") with a menu of **Delete only**,
calling `requestDeleteGalleryPhoto`. The snackbar reads e.g. **"Photo deleted · used in 1 find and 2
journal entries"**; the row and the file are deleted only when it ends. The tap still opens the viewer.
The drawer's `PhotoGalleryScreen` is untouched (the pending photo is hidden there too, since it reads
the same `hidingPendingDelete()` state).

**Snackbar slot key.** `PendingDeleteNotice.type` is now `PendingDeleteKind` (waypoint, offline
region, find, Cartography entry, gallery photo). J4 keyed on `RecordType`, which has only the four
Records types and also drives the chips and colour roles, so two non-Records values were not added
there.

**Wiring.** `MainActivity` passes `cartographyUiState.hidingPendingDelete()`,
`requestDeleteEntry`, `requestDeleteGalleryPhoto`, and the two new notices; `onOpenLogEntryForEditing`
now also reaches `JournalTab`. `LogPanel` (wide tree) gets no entry swipe, no find menu and no photo
menu; it does get the two-stage rows for waypoints and regions, since it shares those sections.

## Tests: base failures

Each tests-first commit run at its own base, cleared results, build log clean (one compile failure
caught and not read, below).

- **L4/L5 at `ec1aa2d`:** 3 fail for the stated reason (`expected:<[Changes discard]ed> but was:<[Find
  delet]ed>`, and "Changes discarded … is not displayed" twice). 4 pass by construction (the
  unchanged "Find deleted" cases and the null case). The new L5 test passes (it describes current
  behaviour; its proof is the revert check). **First base run of `FindDeleteNoticeTest` failed on the
  wrong reason** (`ExceptionInInitializerError`: `PendingDeleteCommitScope` reads `Dispatchers.Main` in
  the same file); fixed by running the class under Robolectric, then it failed for the right reason.
  One earlier run did not compile (`getOrNull` import); the runner refused to read it.
- **Holders at `54dc5d5`:** 13 of 15 fail (nothing pended, nothing committed, e.g. `expected:<entry-a>
  but was:<null>`); the two unknown-id tests pass by construction.
- **L6 at `53610e4`:** 5 of 6 fail (`records-swipe-waypoints-wp-creek-delete` not displayed, and the
  like); the accessibility test (Delete only, no Edit) passes by construction.
- **L2 at `c9653e9`:** 9 of 9 fail on the missing swipe row (`entries-swipe-entry-a`); the 31 earlier
  tests in the class still pass with the new harness.
- **L1/L3 at `a9ca5a7`:** 9 of 11 fail (no menu; `OnLongClick` not present); the two plain-tap tests
  pass by construction. **The first base run failed the album tests for a harness reason** (the
  harness passed no `galleryPhotos` to `JournalTab`, so no photo tile existed); the harness was fixed
  to pass what the compact scaffold passes, and the second base run is the one cited.

Every forward run passed first time. What the UI tests assert (all in `JournalPendingDeleteTest`: the
real `JournalTab`, real ViewModels over fakes whose call lists every delete assertion reads, a real
`SnackbarHost` fed by the notice builders `MainActivity` uses):
- real `performTouchInput` swipes: short (64 or 110 dp, slow), full (`swipeLeft()`), and slow long
  (45% and 80% of the row);
- revealed buttons touched by coordinate at three points each (each point a fresh attempt: open,
  touch, Undo);
- real `longClick` at three points of two tiles and two photos; menu items touched by coordinate;
- menus with exactly the expected items (clickable nodes under the popup);
- exact snackbar texts; nothing deleted before the timeout (a region's and a photo's checked at half
  the timeout too); exactly one delete with the id after; Undo deletes nothing;
- the Edit route to the editor for that record; custom actions; long-click labels;
- a plain tap still doing today's action; a tap on a revealed card closing it without opening;
  one open row at a time; a touch elsewhere closing.

## Revert checks

All build logs clean except the one noted, every file restored identical by `cmp`, tree clean after
each batch.

| Behaviour | Edit | Observed |
|---|---|---|
| L4 message choice | `draftOfEntryId != null` -> `false` | 3/28: the three Changes-discarded tests |
| L4 message choice, other side | -> `isDraft` | 2/28: the two new-find tests |
| L5, old button back | `OutlinedButton { Text("Delete") }` added to `OfflineRegionRow` | 1/28: found 1 node with text "Delete" |
| L5, trash icon variant | `IconButton` "Delete Molalla Ridge" added | 1/28: found 1 clickable node described "Delete…" |
| L5, custom action gone | `customActions` not set | 1/28: "has a custom accessibility action labelled Delete" |
| Entry commit on timeout / replacement / clear | `entry?.let(...)` off; `displaced?.let(...)` off; `takeAny` -> null | 2/8, 1/8, 1/8 |
| Entry deferral | request commits at once | 7/8 |
| Entry hidden while pending | `hidingPendingDelete` returns `this` | 4/8 |
| Photo commit on timeout / replacement / clear | the same three edits | 2/69, 1/69, 1/69 |
| Photo deferral (row and file) | request commits at once | 6/69 headless; 4/51 UI (e.g. no row or file delete before the snackbar ends) |
| Photo hidden from gallery / from finds | each filter off | 3/69, 1/69 |
| Open anchor | open anchor removed | 5/30: every short-swipe test |
| Delete anchor | delete anchor removed | 12/31: J4's full-swipe tests and the threshold test |
| Delete threshold by position | `0.5f` -> `0.9f` | 6/31, the threshold test at its 80% assertion (`…used in 2 journal entries … is not displayed`) |
| One open at a time | the group collector never closes | 3/40: waypoint, All and entry-card tests |
| Touch elsewhere closes | `onDownInRoot` call replaced by `Unit` | 1/31 |
| Revealed Delete | button's `onDelete` removed | 2/30 |
| Waypoint gains an Edit | `onEdit = {}` in the logbook | 2/30: the Delete-only action test, and the All test (the open width doubled, so a 64 dp swipe no longer opens) |
| Tap on revealed body | overlay off | 1/40 |
| Entry Edit route | timeline Edit sets VIEW | 2/40 |
| Draft Edit route | `onEditDraft = { _ -> }` | 1/40 |
| Entry Edit action | action body drops the call | 1/40 |
| Draft deleted message | always "Entry deleted" | 1/40 |
| Undo (all types) | `ActionPerformed -> onCommit()` | 9/40 after L2; 13/51 after L3, the photo Undo tests among them |
| Find Edit route | editFind sets REPORT | 3/51 |
| All Edit selects Finds | `selectTab` removed | 1/51: Finds chip `Selected = 'false'` |
| Menu Delete | item's call removed | 6/51 |
| Tile custom actions | not set | 2/51 |
| Tile tap | `onClick = {}` | 8/51 (J4's report tests tap tiles too) |
| Album menu is Delete only | `if (hasEdit)` -> `if (true)` | 1/51: an Edit item found |
| Photo message | find count dropped | 1/51 |

**A revert check that did not bite, and what it found.** The first delete-threshold check moved the
delete anchor to three row widths and **every test still passed**. A full `swipeLeft()` flings, and a
fling carries to the next anchor whatever the positional threshold is, so no test pinned the
threshold by position. The slow-swipe test (`6b8129d`) was added for it and fails under the
threshold revert. **One L6 revert did not compile** (a `false &&` in front of a smart cast); the
runner reported the compile error and read nothing, and the check was rerun with a different edit.

## Tests moved or rewritten

| File | Change | Count |
|---|---|---|
| `ui/log/JournalPendingDeleteTest.kt` | **rewritten** (behaviour changed, L4): J4's "deleting from the find's edit form is the same pending delete, of the open draft" asserted "Find deleted"; now "Changes discarded", same deferral and id | 1 test |
| `ui/availability/AvailabilityScreenSettingsPanelTest.kt` | **rewritten** (L5): the vacuous line removed from "…no regions or delete buttons show…", renamed; what it guarded moved to a new test with a region on screen | 1 test changed, 1 added |
| `ui/log/JournalPendingDeleteTest.kt` | harness only: `onStartEntry` wired to the real `onStartNewEntry`; a real `CartographyViewModel`; gallery photos and a recording photo store; the new `JournalTab` parameters | 0 tests |
| `ui/log/MushroomLogViewModelTest.kt` | fake only: records `deletePhotoFromGallery`, can fail it; helper takes `getPhotoEntryReferenceCount` | 0 tests |

**J4's single-stage swipe tests needed no rewrite:** they run a full `swipeLeft()`, and a full swipe
still deletes. They passed unchanged at `9298a68` and at every later run. Nothing disabled, skipped or
weakened.

## Suite before and after

- Before, at `a47e66f`: 239 classes / 1925 / 0 / 0 / 24.
- After, at `7830e80`, cleared results directory, build log clean: **241 classes / 1974 tests /
  0 failures / 0 errors / 24 skipped.** +2 classes (`FindDeleteNoticeTest` 4,
  `CartographyEntryPendingDeleteTest` 8), +49 tests (L4 6, L5 1, holders 15, L6 7, L2 9, L1 6, L3 5).
- **`JournalTabTest`'s photo-pull test did not fail** in the baseline or the final run, so there was
  nothing to rerun alone.

## Needs a decision

1. **Album photos now have two deletes with different behaviour.** The long-press menu's Delete is
   pending with Undo; the tile's corner trash button (`EntriesAlbum.kt`, `AlbumPhotoTile`, unchanged)
   still confirms in a dialog and then deletes rows and file at once. The dispatch says only that the
   tap is unchanged. Options: (a) keep both, as built; (b) remove the corner button, leaving the
   long-press and the accessibility action; (c) route the corner button through the pending holder
   too, without its dialog. Built: (a). The drawer's `PhotoGalleryScreen` keeps its own button and
   dialog in every option.
2. **L3's Edit.** No photo details or location screen exists (Premises). Options: (a) leave the album
   menu Delete-only; (b) commission a photo details/location screen as its own stage, then add Edit.
   Built: (a).
3. **The photo snackbar's wording.** The old dialog warned about both finds and journal entries using
   a photo; the dispatch's example names journal entries only. Built: both, "Photo deleted · used in
   1 find and 2 journal entries", each part left out at zero. The alternative is journal entries
   only, which drops a warning the dialog gave.

## Decisions I made that the dispatch did not

1. **A touch elsewhere closes the open row and still acts** (a tap on another card still opens it).
   Rejected: swallowing that first tap, as some mail apps do; a visible, ordinary card would do nothing
   for no reason the user can see.
2. **One open row per list, not per screen.** Each list has its own group; the Records chips and the
   Entries timeline never show at once.
3. **Scroll closes the open row** in the Entries grid and the All logbook, where the list's scroll
   state is at hand. `WaypointsSection` and `OfflineRegionsSection` close it on a touch elsewhere
   only: a scroll that starts on the open row itself leaves it open there (not device-checked).
4. **Reveal width** 72 dp per action, capped at 60% of the row so the delete anchor stays well past
   the open one on a narrow card (two columns in a short window).
5. **The pending photo is also hidden from finds' own photo lists** (the Finds gallery's cover, the
   find report), not only the album, following J4's "hidden from every Journal list". The open edit
   form's working copy is left alone.
6. **`PendingDeleteKind`** instead of adding entries and photos to `RecordType` (What was built).
7. **`LogPanel` gets no entry swipe, find menu or photo menu** (no callbacks passed; J6 owns the wide
   tree). It does get two-stage waypoint and region rows, since it shares those sections.
8. **A menu with one item** for album photos, since Delete is the only action there is; the
   accessibility action is the same one.
9. **Messages:** "Entry deleted", "Draft deleted", "Changes discarded", and the photo wording above.

## Device-only items

- The two-stage swipe's feel on the S22 Ultra: the open width, the delete threshold, the fling from
  open to delete, RTL, and a vertical scroll starting on an open row in the Waypoints chip.
- Long-press timing and the menu's anchor position on a tile near the screen's bottom edge.
- TalkBack: the Edit/Delete actions on cards, rows, tiles and photos; the menu read out; the long-click
  labels ("Options for …").
- The album tile: a long-press near the corner trash button (the button takes that touch; tests
  sampled points off it).

## Flags outside scope

- **`MainActivity`'s J4b wiring is exercised by no test**, as J4 recorded for its own: no test
  composes `MainActivity`. The UI tests wire the same entry points and notice builders by hand.
- **A pending record stays pending while no snackbar host is composed**, as J4 recorded; this now
  covers entries and photos too (committed on ViewModel clear).
