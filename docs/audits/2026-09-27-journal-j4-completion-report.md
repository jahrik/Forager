# 2026-09-27: Journal redesign J4, completion report

Dispatch: `prompts/preserved/2026-09-27-21.md` (build, J4 coder). Plan: `docs/plans/journal-redesign.md`
(J8, "Build order" J4, "Rules for every build stage"). Map: `docs/audits/2026-09-27-journal-j0-pulse.md`
(Part B1 and the owner's rulings). Previous stages: the J1, J2 and J3 completion reports. Written by
the J4 coder in a cloud worktree on local branch `j4`, which tracks `origin/journal-redesign`.

**Status: D1 to D6 are built, tested and pushed.** Nothing stopped on an abort condition. Two small
questions for the owner are listed under "Needs a decision"; neither blocked a step.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `8dfecf1925ce06011fe1bed05b6dee038f2a3a08` | D1 tests first (24) plus stubs. |
| `29efb67da26b648d4c94a2ad1923e9d3d5de3358` | D1: `PendingDeleteSlot` and the three ViewModels' pending holders. |
| `f579dee9da0d7c54ef6d589faa33e4cb93ab27b9` | D2 tests first (9, new `JournalPendingDeleteTest`) plus stubs. |
| `74360dc521d45b15c578bc20649b5c7315a456c6` | D2: waypoint swipe, Undo snackbar, trash icon and dialog gone; two tests rewritten. |
| `bc46c0e80c814c0e89c27a488777cef1b471b2f0` | D3 tests first (7) plus stub. |
| `9cf854077b1b7059be9b3d1533e6aeddb5a973e2` | D3: region swipe, deferred tile delete, Delete button and dialog gone. |
| `144a77b5dd8941bb4efb89c2e9df71a1fec62e7d` | D4 tests first (6) plus stub. |
| `4f43b76af0218cb0dfa5cdde2d54e2bb54942763` | D4: the find report's Delete goes through the holder. |
| `3df7296e3105b5749df0ed944e598523019c5a0f` | D5 tests first (6) plus stub. |
| `47bb3be155e9ae48d2ae0c4ea5c51736512d5f05` | D5: multi-track thumbnail and summed stat with count. |
| `4545ac599623a050b9e0d008187ee1d3cf0e704f` | D6 tests first (4) plus a behaviour-preserving extraction. |
| `deab96e9d216495f4140b59b0cf7a4405cb42c7c` | D6: both fallbacks logged. Final code head. |
| this report's commit | This file. |

Production diff `b65b775..deab96e`: 18 files, +944 / −208. New: `domain/PendingDelete.kt`,
`ui/log/PendingDeleteSnackbar.kt`, `ui/log/SwipeToDelete.kt`.

## Premises checked before building

- **Base.** `origin/journal-redesign` was `b65b7758b4f7d7afc4693cede73500c53b0e6481` when fetched, the
  planner's store-copy commit after `19f5216`, as the dispatch allows. Branch `j4` cut from it.
- **Baseline**, cleared results directory, `LC_ALL=C.UTF-8`, build log with no `e: ` lines:
  **236 classes / 1869 / 0 / 0 / 24**. Matches the planner's figure.
- **Runner** (scratchpad `j4/run.sh`, `j4/revert.sh`). Every Gradle run clears `test-results`, runs
  with `LC_ALL=C.UTF-8`, refuses to read results if the build log has `e: ` lines and refuses a run
  with no XML. `revert.sh` saves a copy, applies a one-line edit (refused unless the old text matches
  exactly once), runs the classes, restores from the saved copy (never git) and `cmp`s. After every
  revert the tree was checked clean (`git status --short` empty) before going on.
- **J0 B1's delete paths, re-read at `b65b775`. All are where J0 says, in the files it names;** line
  numbers moved because J1 S4 extracted the dialogs:
  - Waypoint: trash `IconButton` in `WaypointRow` (`AvailabilityTripsWaypointsUi.kt:270` at base, as
    J0 said), confirm now in the extracted `WaypointDeleteDialog`. Wired `MainActivity.kt`
    `onDeleteWaypoint = trackRecordingViewModel::removeWaypoint`.
  - Offline region: `OutlinedButton("Delete")` in `OfflineRegionRow`, confirm in
    `OfflineRegionDeleteDialog`. The real delete is `AvailabilityViewModel.onDeleteOfflineRegion` ->
    `MapLibreOfflineMapRepository.deleteRegion` (tiles, then row, `:145-148`).
  - Finds: `LogEntryReportScreen.kt:137-143` ("Delete entry" menu item) and
    `LogEntryDetailScreen.kt:159-160` ("Delete this entry"), both calling `onDeleteEntry`, wired to
    `MushroomLogViewModel::onDeleteEntry`, no confirm.
  - Tracks: no delete in the UI (`git grep` of callers). None added.
  - Callers: `onDeleteWaypoint`/`removeWaypoint` and `onDeleteOfflineRegion` have no callers outside
    the Records surfaces (`JournalTab`, `LogPanel` through `RecordsTab`); the map deletes neither.
- **A region deleted while downloading (the dispatch's check).** A downloading region is never a row,
  so it cannot be swiped: `AvailabilityUiState.offlineRegions` is only ever set from
  `OfflineMapRepository.listRegions` (`AvailabilityViewModel.loadOfflineRegions`), which lists
  **complete** regions only and deletes incomplete ones (`MapLibreOfflineMapRepository.kt:172-176`);
  a download in progress shows only as the status bar (`OfflineDownloadStatusContent`), and the list
  reloads after it succeeds (`onDownloadOfflineMaps` -> `loadOfflineRegions`). Today's delete has no
  path to an in-progress region either. So no new behaviour was needed and the abort did not fire.
  See "Flags" for a hazard this reading turned up.
- **Delete calls deferrable without DAO changes:** each ViewModel already calls a use case or
  repository method taking only the id; the holder defers that call. No DAO or repository signature
  changed.

### Premises that were wrong / disagreements

- **J0's ruling 1 vs this dispatch.** J0's recorded ruling puts swipe on "waypoint, find and
  offline-region rows"; the dispatch's later ruling keeps finds' delete inside the report with no swipe
  on find tiles. I followed the dispatch (it wins). The plan's J8 ("Undo if reversible, otherwise ⋮ and
  a dialog") is superseded by the owner's ruling.
- **Planner's predictions.** 1 holds: no DAO or repository signature changed. 2 holds: the waypoint and
  region confirm-dialog tests were rewritten, not moved (two tests, below). 3 holds: +56 tests (30 to 60
  predicted), 0 failures, skipped unchanged at 24.

## What was built

**D1, the holder.** `domain/PendingDelete.kt`: `PendingDelete<T>(item, entryReferenceCount, token)`
and `PendingDeleteSlot<K, T>` (pure, one record per type): `pend` returns the record it displaces for
the caller to commit; `undo` and `commit` act only on the named key and return it once; `takeAny` for
clear; `withoutPending` hides it from a list. Each owning ViewModel holds one slot and gains three entry
points: `TrackRecordingViewModel.requestRemoveWaypoint/undoRemoveWaypoint/commitRemoveWaypoint`,
`AvailabilityViewModel.requestDeleteOfflineRegion/undo…/commit…`,
`MushroomLogViewModel.requestDeleteEntry/undoDeleteEntry/commitDeleteEntry`.
- **Hidden while pending:** `TrackRecordingUiState.visibleWaypoints`,
  `AvailabilityUiState.visibleOfflineRegions`, `MushroomLogUiState.hidingPendingDelete()`.
- **Commit:** the unchanged delete call. The record leaves its list at once (no flash back between the
  snackbar closing and the delete landing); a failed delete puts it back where it was and sets the
  error each ViewModel already used ("Couldn't delete waypoint." / "Couldn't delete that region." /
  "Couldn't delete that entry.").
- **Replacement:** a second pend of the same type commits the first immediately.
- **Clear:** each ViewModel's `onCleared` commits what is pending on `PendingDeleteCommitScope`
  (process lifetime, `Main.immediate`, `SupervisorJob`), because `viewModelScope` is cancelled by then.
- **Process death before commit:** the record is not deleted — the safe direction.
- **Finds:** `requestDeleteEntry` runs inside `editingEntryMutex`, like every other write to
  `editingEntry`, and closes the report or form; a never-left draft goes to `draftEntries` on Undo.
- **Unknown id:** logged, nothing pended.

**D2, waypoints.** `ui/log/SwipeToDelete.kt`: `SwipeToDeleteRow` over `SwipeToDismissBox`
(material3 1.5.0-alpha26's `onDismiss` API, checked with `javap`), end to start only, error-container
background and trash icon only while swiping that way, surface colour behind the content, a semantics
custom action "Delete" on the tagged node. `WaypointRow` lost its trash icon; `WaypointsSection` and
the All logbook wrap each waypoint in a `key(id)` `SwipeToDeleteRow`; `WaypointDeleteDialog` is gone.
`ui/log/PendingDeleteSnackbar.kt`: `pendingDeleteMessage`, `waypointDeleteNotice`, and
`PendingDeleteSnackbarEffects`, which shows each notice in **`AvailabilityScreen`'s one snackbar host**
(the host "Saved to Drafts" already uses, so it docks in both window classes and outlives a Journal tab
change): `SnackbarDuration.Long`, action "Undo"; `ActionPerformed` -> undo, `Dismissed` -> commit; a new
delete snackbar dismisses an older delete snackbar. `MainActivity` passes `visibleWaypoints`, wires the
swipe to `requestRemoveWaypoint` and passes the notices.

**D3, regions.** `OfflineRegionRow` lost its "Delete" button; `OfflineRegionsSection` and the All
logbook wrap each region; `OfflineRegionDeleteDialog` is gone. Rows, the Offline maps chip count and
All read `visibleOfflineRegions`; the tile-budget line still counts every region (see Decisions 3).
Notice "Offline map deleted · used in N journal entries".

**D4, finds.** `findDeleteNotice` ("Find deleted"). `MainActivity` passes
`logUiState.hidingPendingDelete()` and wires `onDeleteLogEntry` to `requestDeleteEntry`. The two
screens are unchanged (they still call `onDeleteEntry`). "Saved to Drafts" -> Discard stays immediate.

**D5.** `projectTracksToBox` projects several tracks on one shared bounding box
(`projectTrackToBox` is now that function with one track, same arithmetic); `TracksThumbnail` draws
each as its own subpath; `entryThumbnailTracks` returns every kept track found; `entryStats` reads
"2 tracks · 5.4 km · 2h 10m" for two or more, unchanged for one.

**D6.** `MainActivity.kt:75` and `:179` now call `offlineRegionEntryReferenceCountOrZero` /
`waypointEntryReferenceCountOrZero`, which log through `androidErrorLog` (tags
`OfflineRegionReferenceCount`, `WaypointReferenceCount`) and return 0 as before.

**What the map shows:** a pending waypoint is **hidden on the map too** (`MainActivity` passes
`visibleWaypoints` as the screen's one `waypoints`, which feeds the map). Offline regions are not drawn
on the map at all (the only readers of `offlineRegions` in `main/` are the Journal surfaces and the
ViewModel's own download gate, by `git grep`), so nothing changes there.

## Tests: base failures

Each tests-first commit run at its own base, cleared results, build log clean.

- **D1 at `8dfecf1`** (4 classes, 127 tests): all 24 new fail, on the stubs ("expected:<[wp-creek]> but
  was:<[]>", "Required value was null", positive controls, etc.). One failed on setup instead
  ("List is empty": an unchanged re-edit draft is discarded on leaving); I fixed the test (edit before
  leaving) and re-ran the class: it then failed for its stated reason (the editor not closed).
- **D2 at `f579dee`**: 9/9 fail, `could not find … TestTag = 'records-swipe-waypoints-…'`.
- **D3 at `bc46c0e`**: 16 run, the 7 new fail on the missing region swipe tag; the 9 D2 tests pass.
- **D4 at `144a77b`**: 22 run, 5 fail on the missing "Find deleted" snackbar / Undo. The harness already
  wires `requestDeleteEntry` (built in D1), so what D4 adds is the notice and the `MainActivity` wiring.
  **"find tiles have no swipe" passes at base by construction** (an absence check; its positive control
  is the tile existing).
- **D5 at `3df7296`**: 6/6 new fail (projection "point count expected:<2> but was:<0>", card thumbnail
  absent, stat text absent).
- **D6 at `4545ac5`**: the two failure tests fail ("one log call: [] expected:<1>"); **the two success
  tests pass at base by construction**; revert checks rv15/rv16 cover them.

Every forward run passed first time. What the UI tests assert (`JournalPendingDeleteTest`, 22): the real
`JournalTab` rows, real ViewModels over fake repositories (every delete assertion reads the fake's own
call list), a real `SnackbarHost` and `PendingDeleteSnackbarEffects` fed by the same notice builders
`MainActivity` uses. Real `swipeLeft` gestures on two row positions per type; snackbar text exact with
2, 1 and no references; Undo restores and nothing is deleted after the timeout; the timeout (the host's
own `Long`, reached by advancing the test clock) deletes exactly once with the id; the region's delete
is not called at half the timeout and is called once after; a second swipe commits the first (same type,
and waypoint then region); swipe start-to-end does nothing; the custom "Delete" action; chip counts and
All; the report's and edit form's Delete; no swipe on find tiles. The Undo button is asserted to have a
click action (TalkBack reaches it as a button).

## Revert checks

All build logs clean, every file restored identical by `cmp`, tree clean after each.

| Behaviour | Edit | Observed |
|---|---|---|
| Commit on timeout (holder) | `commit()` returns null | 4/127: the slot's and the three ViewModels' commit tests, e.g. `expected:<[wp-creek]> but was:<[]>` |
| Commit on replacement | `pend()` returns null | 4/127: the slot's displacement test and the three replacement tests |
| Hidden while pending | `withoutPending` returns the list | 11/127: requested, undo positive controls, replacement, open-draft |
| Commit on clear | `AvailabilityViewModel.onCleared` skips `takeAny` | 1/19: `expected:<[1]> but was:<[]>` |
| Region tile delete deferred | request commits at once | ViewModel 5/19 (e.g. budget `[1, 2]` was `[2]`, commit `[1, 1]`); UI 6/16 (`not before the snackbar ends expected:<[]> but was:<[7]>`) |
| Undo deletes nothing (UI) | `ActionPerformed -> onCommit()` | 2/9: the row is not back after Undo |
| Commit on timeout (UI) | `Dismissed -> Unit` | 3/9: timeout, custom action and replacement show no delete |
| Custom action | the action no longer calls `onDelete` | 1/9: no snackbar after the action |
| Finds hidden while pending | `hidingPendingDelete()` returns `this` | 2/22: the tile and the All row still there |
| Multi-track scaling | shared box from the first track only | 1/36: `x of point 0 expected:<0.0> but was:<50.0>` |
| Multi-track count label | label off | 1/24: the "2 tracks · …" test |
| Waypoint fallback logged | log line removed | 1/4 |
| Region fallback logged | log line removed | 1/4 |
| Silent on success (waypoint / region) | log on every call | 2/4 each: success tests see an "always" entry |

## Tests moved or rewritten because a control is gone

| File | Change | Count |
|---|---|---|
| `ui/log/RecordsFilterChipsTest.kt` | moved: "Remove waypoint Alpha/Bravo/Creek pin" content descriptions to the swipe row's tag, assertions unchanged | 3 call sites |
| `ui/log/RecordsFilterChipsTest.kt` | added: the region's swipe tag beside the All row-contents check (its "Delete" button was part of "each row's own controls") | 1 |
| `ui/log/RecordsFilterChipsTest.kt` | **rewritten** (behaviour changed): "deleting from All goes through the same confirmation dialogs as the chips" is now "deleting a waypoint or a region from All is a swipe, with no dialog" | 1 test |
| `ui/availability/AvailabilityScreenWaypointFlowTest.kt` | **rewritten**: "every Waypoints drawer control is reachable, and delete calls onDeleteWaypoint with its id" is now "…a swipe asks for the delete and shows Undo in the screen's snackbar host" (its harness gained a pending notice) | 1 test |
| `ui/log/JournalPendingDeleteTest.kt` (my own, D3) | two All-count assertions 2/1 -> 4/3 when regions joined the harness | 2 |

Nothing disabled, skipped or weakened. **Not observed:** I updated the D2 files before running the
full suite, so how many tests failed without the updates was not measured; an attempt to re-run the old
versions (restoring them with `git checkout`) was refused by the session's permission check and not
retried. The counts above are call sites and tests changed, not failures observed.

## Suite before and after

- Before, at `b65b775`: 236 classes / 1869 / 0 / 0 / 24.
- After, at `deab96e`, cleared results directory, build log clean: **239 classes / 1925 tests /
  0 failures / 0 errors / 24 skipped.** +3 classes (`PendingDeleteSlotTest` 8,
  `JournalPendingDeleteTest` 22, `WaypointAndRegionReferenceCountTest` 4), +56 tests (D1 24, D2 9, D3 7,
  D4 6, D5 6, D6 4); skipped unchanged.
- An intermediate full run at D2 (`d2full`) was 238 / 1902 / 0 / 0 / 24.
- **`JournalTabTest`'s photo-pull test did not fail** in the baseline or either full run, so there was
  nothing to re-run alone.

## Needs a decision

1. **The edit form's Delete on a re-edited find.** When a committed find is open in its edit form, the
   form is editing a separate draft row, and "Delete this entry" deletes that draft (the committed find
   stays). That is today's behaviour and J4 keeps it, but the new snackbar says "Find deleted", which
   reads as if the find itself went. Options: (a) leave it; (b) a different message for a draft of a
   committed find ("Changes discarded"); (c) make that button delete the committed find too (a
   behaviour change). Built: (a).
2. **Swipe-away.** The planner's commit list includes the snackbar being swiped away. Material 3's
   default `SnackbarHost`, which this app uses, has no swipe-to-dismiss, and I did not add one (new
   capability on a shared host). Any non-Undo ending commits, so it would work if one is added.

## Decisions I made that the dispatch did not

1. **The snackbar lives in `AvailabilityScreen`'s existing host**, not a new Journal-only one, so it
   docks where "Saved to Drafts" does in both window classes and keeps running across a tab change.
   This meant editing `AvailabilityScreen.kt` (one parameter and one call), under "the Journal's
   snackbar host wiring". Rejected: a host inside `JournalTab`, which a tab change would dispose
   mid-snackbar, leaving the record pending until the ViewModel is cleared.
2. **Duration `Long`** (about 10 s, lengthened further by Material 3 when an accessibility service asks
   for longer timeouts). "Saved to Drafts" uses `Short`; Undo on a delete gets the longer time.
3. **The tile budget counts a pending region** (its tiles are on disk until the delete runs), matching
   the download gate, which reads the same full list. Rejected: freeing the budget at once, which would
   let a download start that Undo then pushes over the limit.
4. **Other snackbars are not dismissed** by a delete snackbar: only an older delete snackbar is. A
   delete that arrives while "Saved to Drafts" shows waits its turn, staying pending that much longer.
5. **The map hides a pending waypoint** (the screen has one `waypoints` list); showing it until commit
   would have needed a second list threaded through the scaffold.
6. **Messages:** "Waypoint deleted", "Offline map deleted" (the chip's noun), "Find deleted", with
   "· used in 1 journal entry" / "· used in N journal entries". Finds never carry a count: none exists
   for finds (`GetEntryReferenceCountUseCase` doc comment), and adding one is a DAO change.
7. **Reference count at pend time** is the loaded map's value, missing counting as zero, as the old
   dialogs and the UI state's own doc comments do.
8. **Multi-track, one kept track not loaded:** the tracks that are found still draw (the stat still
   sums the stored decisions). Rejected: no thumbnail unless all are found.
9. **Stat format** is the app's `trackSubtitle` ("2h 10m"), not the ruling's example spacing
   ("2 h 10 m").
10. **`PendingDeleteNotice` is a public class with internal members**, because `AvailabilityScreen` is
    public.
11. **`waypointEntryReferenceCounts` stays on `RecordsTab`/`JournalTab`/`LogPanel` but is no longer
    read** (`@Suppress("UNUSED_PARAMETER")` on `RecordsTab`, documented): removing it touches the
    scaffold and `LogPanel`, outside J4's files. `WaypointsSection` and `RecordsLogbookList` dropped
    their copies.
12. **The wide tree (`LogPanel`) gets the same swipe rows and snackbars**, since it shares `RecordsTab`
    and `AvailabilityScreen`'s host is in its drawer sheet. Not tested in the wide tree.

## Device-only items

- The swipe itself on the S22 Ultra: threshold feel, the red background and icon, RTL mirroring
  (not tested under Robolectric).
- TalkBack: the "Delete" action listed on a row, the snackbar announced, Undo reachable within the
  extended timeout.
- The snackbar's position above the bottom navigation and the FAB-free Records screen in both
  orientations, and in the wide tree's drawer.
- The region row on a surface other than `surface` (the content background behind a swiped row).
- A night-mode toggle mid-snackbar: the pending record should survive in its ViewModel and the snackbar
  show again (reasoned from the code, not run).

## Flags outside scope

- **A download in progress may be deleted by opening Offline maps** (inferred, not run):
  `onOfflineMapsOpened` -> `loadOfflineRegions` -> `listRegions`, which deletes any region whose status
  is not complete (`MapLibreOfflineMapRepository.kt:172-176`). If MapLibre lists a region while it is
  downloading (likely, since it is created before the download starts, `:109`), entering the Offline
  maps chip mid-download would delete it. Pre-existing; J4 does not touch it.
- **`AvailabilityScreenSettingsPanelTest`'s "…no regions or delete buttons show with nothing
  downloaded" (`:636`, `onAllNodesWithText("Delete").assertCountEquals(0)`) now passes vacuously**:
  no "Delete" text exists anywhere any more. It still checks "No regions downloaded yet." Left as is.
- **`MainActivity`'s J4 wiring is not exercised by any test**: no test composes `MainActivity`. The UI
  tests wire the same builders and entry points by hand.
- **A pending record stays pending while no snackbar host is composed** (the host drives the timeout);
  it commits when the host returns and the snackbar ends, or when its ViewModel is cleared.
