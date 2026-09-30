# Find-delete-reappears: completion report (dispatch 2026-09-28-291)

Branch `find-delete-reappears`, cut from `eb4cf129` (verified: an ancestor of `origin/journal-redesign` `ba428b1c`, which adds only the dispatch and its intent). No device, no adb, nothing merged to `journal-redesign`.

## Premises, verified against `eb4cf129` (line numbers are that tree's; the fix shifts later ones)

| Premise | Result |
|---|---|
| Map records are a snapshot: `loadMapRecords()` reads `getMapRecords()` once into `uiState.mapRecords` | **Confirmed.** `AvailabilityViewModel.kt:492-498`. |
| Only callers are `onMapShown()` and `reloadAfterRestore` | **Confirmed.** `git grep loadMapRecords` in `main/`: `onMapShown` (`:480`) and `reloadAfterRestore` (`:953`). Nothing reloads it on a find delete. |
| A pending find delete is hidden by a filter, not removed | **Confirmed.** `AvailabilityScreen.kt:887-891` calls `GetMapRecordsUseCase.kt:36` `withoutPending(findId = logUiState.pendingDelete?.item?.id, ...)`. |
| The delete commits when the snackbar ends; committing clears `pendingDelete` and does not reload the map | **Confirmed.** `MainActivity.kt:678-682` passes `mushroomLogViewModel::commitDeleteEntry`; `MushroomLogViewModel.commitDeleteEntry` (`:716-719`) sets `pendingDelete = findDeletes.pending` (now `null`) then starts the delete; nothing touches `mapRecords`. |
| The fan's hit-test (`queryRenderedFeatures`, `fanout/MapTapHandler.kt`) then collects the drawn find | **Not read beyond the file header** (`MapTapHandler.kt:14` says `SightingsMap` implements it over `queryRenderedFeatures`). Unverified, and not needed: the tests stop at what the map is handed. |

**Corrected, and stronger than the planner's reading:** the snapshot need not be taken while the delete is pending. Test 1 below never re-shows the tab: the snapshot from the first show already holds the find, so *any* find deleted after the tab was last shown is drawn again the moment its Undo window ends. The owner's path (back from the find page runs `onMapShown` while the row is still in the database) is one way to hit it, not the only way.

## Did the failing test reproduce the owner's bug on the timeout alone? Yes.

`a find deleted while the map was re-shown is not drawn after the Undo window ends, with nothing tapped`: request the delete, `onMapShown`, commit, and nothing else. On `eb4cf129` (commit `dd4a3d57`) it fails with `find-a was deleted and is still drawn expected:<[find-b]> but was:<[find-a, find-b]>`. The planner's inference that "highlighting another find" was coincidental with the timeout stands: no tap is needed, and no tap exists in the test. **What this does not show:** that the owner's screenshots were a timeout and not a tap. It shows the timeout alone is sufficient; a tap would only matter if it made the snackbar end.

## Siblings (reported, not fixed, as instructed). Both have the same gap, by reading only; I did not write a test for either.

- **Photo** (`photoId` on the same `withoutPending` call): `commitDeleteGalleryPhoto` (`MushroomLogViewModel.kt:1154`) clears `pendingPhotoDelete`, then `commitGalleryPhotoDelete` (`:1166`) reloads `loadGalleryPhotos()` and `loadEntries()` on success. Neither touches `AvailabilityViewModel.mapRecords`, so `photoMarkers` keeps the photo and draws it once the filter stops.
- **Offline region** (`offlineRegionId`): `commitDeleteOfflineRegion` (`AvailabilityViewModel.kt:1389`) clears `pendingOfflineRegionDelete`, then `commitOfflineRegionDelete` (`:1400`) calls `loadOfflineRegions()` on success (`:1405`). That refreshes `offlineRegions`, not `mapRecords.offlineRegionCircles`. Same gap.

## Mechanism chosen

`MushroomLogViewModel` takes a new last constructor parameter `onFindDeleted: (String) -> Unit = {}` and calls it from `commitFindDelete`'s `onSuccess`, i.e. after `deleteEntry` has returned success. `MainActivity` wires it (`onFindDeleted = { id -> viewModel.onFindDeleted(id) }`, the lazy-lambda shape `currentFix` already uses). `AvailabilityViewModel.onFindDeleted(id)` removes the find from `uiState.mapRecords.findMarkers`. It is on the path every find-delete commit takes (timeout, a second delete displacing the first, `commitDeleteEntry`), and not on failure, where the find is still saved and stays drawn.

Why removal, not a reload: the database row is already gone when the hook runs, so there is nothing to wait for and no read that could still see it (the dispatch's "after the delete, not concurrently"). A reload would add a read and no correctness.

Alternatives rejected:
- **Reload on commit from `MainActivity`/the screen** (`onCommit` calling both): runs when `pendingDelete` clears, *before* the delete finishes (`commitFindDelete` launches it after), so the reload can re-read the row: the race the dispatch names.
- **`mapRecords` as an observed `Flow`**, or **changing `PendingDelete`**: the two things the dispatch says to stop and ask before; not needed.
- **Keep the filter until the delete finishes**: means changing the pending-delete design.
- **A tombstone set filtered inside `loadMapRecords`**: covers the race below, but needs clearing on restore and Undo semantics; speculative without data.

## Known residuals (unverified on a device)

1. **A window of one DB delete** between `pendingDelete` clearing (`commitDeleteEntry`) and the delete finishing still draws the find from the snapshot. Not an "after anything else the user taps" bug, but it is a flash if the map re-renders in that window; closing it means holding the pending id until the delete finishes, which is the `PendingDelete` design the dispatch reserves.
2. **An in-flight `loadMapRecords` that read the row before the delete and finishes after `onFindDeleted`** would put it back. Needs the tab shown within milliseconds of the delete finishing; not constructed in a test.
3. **`onCleared`'s commit** (`MushroomLogViewModel.kt` `onCleared`) does not call the hook: the activity-scoped availability ViewModel is cleared together with it.
4. The wiring line in `MainActivity.kt:138` is not exercised by any headless test; the test wires the same two ViewModels itself. Remove that line and the suite stays green. It is a one-line lambda, checked by grep, not by a run.
5. The test computes "what the map is handed" by repeating `AvailabilityScreen.kt:887-891`'s expression (it lives in a composable). If that line changes, the test does not follow.

## What landed

| Commit | What |
|---|---|
| `dd4a3d57` | Tests first: `FindDeleteReappearsOnMapTest` (5). Three fail on `eb4cf129` for the predicted reason; the Undo and failed-delete controls pass. Pushed failing. |
| `0aba4562` | The fix (3 main files) and the test's wiring. |
| `0870669d` | Merge of `origin/journal-redesign` (brings in `fan-above-puck`). Clean; the README was not in conflict. |
| this report's commit | Report and README row. |

## Revert check (from a saved copy)

Saved `MushroomLogViewModel.kt` to `/tmp`, commented out the one line `onFindDeleted(entry.id)`, ran `FindDeleteReappearsOnMapTest`. Build log: **0 `e:` lines**, result XML timestamp `18:08:23Z` (fresh, this run). Three failed, each naming the deleted find: `find-a was deleted and is still drawn expected:<[find-b]> but was:<[find-a, find-b]>` (x2) and `... (find-b is the one pending) expected:<[]> but was:<[find-a]>`; the Undo and failed-delete tests passed. Restored from the saved copy, not from git; `git status` clean and `git diff HEAD` empty afterwards, `onFindDeleted` present in `MushroomLogViewModel.kt` (3) and `MainActivity.kt` (1).

Undo and failed-delete tests pass before and after the fix by design (they guard the behaviour the fix must not break); they are controls, not evidence the fix works.

## Suite (read from the JUnit XML, after the merge, at `0870669d`)

- Full `:app:testDebugUnitTest`: **3178 tests, 24 skipped, 0 failures, 0 errors**, 390 classes; 0 `e:` lines. Skips are the pre-existing 24 (the `fan-circle-36` row records 3168/0/0/24 before this and `fan-above-puck`). No owner-held flake or `DiagnosticsPanelTest` failure appeared; no stall, so no thread dump.
- `LeavingTheJournalFixesTest`: 31 tests, 0 failures.
- `:app:assembleDebug`: succeeded, 0 `e:` lines.

## Device-only (the suite cannot show these)

1. The owner's path: open a fan, open a find, delete it, go back to the map, wait past Undo without touching anything. The find does not come back, and a new fan over the spot does not collect it.
2. The same path, then tap another find. The deleted find still does not come back.
3. The same path with Undo: the find comes back on the map and in the fan.
4. Delete a find from the Journal tab, then show Maps. It is not drawn.
5. Watch the moment the snackbar ends for a one-frame reappearance (residual 1).
