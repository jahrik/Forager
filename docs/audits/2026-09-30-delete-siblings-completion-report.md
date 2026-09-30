# Delete-siblings: completion report (dispatch 2026-09-28-297)

Branch `delete-siblings`, cut from `9c806ae1` (verified: an ancestor of `origin/journal-redesign` `4f3a02ad`, which adds only `RECORD.md` and `prompts/preserved/2026-09-30-13..15.md`; `-291` is merged in it as `1b1e65cf`). No device, no adb, nothing merged to `journal-redesign`. Dispatch: `prompts/preserved/2026-09-30-14.md`.

## Premises (line numbers are `9c806ae1`'s)

| Premise | Result |
|---|---|
| Photo: `commitDeleteGalleryPhoto` clears `pendingPhotoDelete`; `commitGalleryPhotoDelete` reloads gallery and entries, not `mapRecords.photoMarkers` | **Confirmed.** `MushroomLogViewModel.kt:1154-1157`, `:1166-1183`. |
| Region: `commitDeleteOfflineRegion` clears `pendingOfflineRegionDelete`; `commitOfflineRegionDelete` reloads `offlineRegions` only | **Confirmed.** `AvailabilityViewModel.kt:1389-1392`, `:1400-1405`. |
| -291's mechanism: `onFindDeleted`, wired log VM to `MainActivity` to availability VM | **Confirmed**, read in `0aba4562`. The photo fix follows it. |
| A photo's deletion changes what a find draws | **No.** Find markers are `entry.foundAt` (`GetMapRecordsUseCase.kt:~92`); `DeleteGalleryPhotoUseCase` removes the photo row, its entry cross-references, then the file, and never touches `foundAt`. A camera photo's location was copied into `foundAt` at capture, so the find stays drawn where it was. Nothing to ask. |

## The delete paths

| Path | Takes the pending path? |
|---|---|
| Compact album, tile long-press Delete / accessibility action: `onRequestDeleteGalleryPhoto` (`MainActivity.kt:596`) | **Yes** (`requestDeleteGalleryPhoto`) |
| Drawer gallery, and the album's own trash button (after a dialog): `onDeleteGalleryPhoto` (`MainActivity.kt:557`, `CartographyScreen.kt:566`) | **No. Deletes at once** (`onDeleteGalleryPhoto`, `MushroomLogViewModel.kt:1096`). No Undo, no filter: on `9c806ae1` the photo stays drawn from the snapshot **immediately**. |
| A find's own photos | I found no photo-delete call in `main/` other than the two above. A find's photo detach (`removePhoto`) is not a delete of the photo row. Not traced further. |
| Offline maps list and Records rows: `onDeleteOfflineRegion = viewModel::requestDeleteOfflineRegion` (`MainActivity.kt:516`) | **Yes** (pending) |
| `AvailabilityViewModel.onDeleteOfflineRegion` (`:1433`, delete at once) | **No production caller.** `git grep` in `main/`: only its definition; the UI's `onDeleteOfflineRegion` lambdas are the pending request. Left alone. |
| `onCleared` commits | Not fixed (no map left to draw; same as -291). |

## Mechanism

- **Photos**: `MushroomLogViewModel` takes a new last parameter `onPhotoDeleted: (String) -> Unit = {}`, called on success from **both** `commitGalleryPhotoDelete` and `onDeleteGalleryPhoto`. `MainActivity` wires it to the new `AvailabilityViewModel.onPhotoDeleted(id)`, which removes the marker from `mapRecords.photoMarkers`. -291's shape, unchanged.
- **Regions**: `commitOfflineRegionDelete`'s `onSuccess` calls a private `dropOfflineRegionFromMapRecords(id)` (the record id is `id.toString()`, as `GetMapRecordsUseCase` makes it). The delete and the snapshot are in one ViewModel, so no callback or wiring is needed; a callback would be indirection for nothing.
- Why removal, not reload, and what was rejected: as in -291's report (a reload adds a read that cannot see the row, and an observed `Flow`/`PendingDelete` change is what the dispatch reserves). `mapRecords` is not a `Flow`; `domain/PendingDelete.kt` untouched.

## What landed

| Commit | What |
|---|---|
| `41d6ae94` | Tests first (`SiblingDeletesReappearOnMapTest`, 9). Pushed, but does not compile: see disclosure 2. |
| `9f224373` | Test file renamed its shared fakes (a clash with `FindDeleteReappearsOnMapTest`'s). Pushed **failing for the right reason**: 5 of 9 fail, `photo-a was deleted and is still drawn expected:<[photo-b]> but was:<[photo-a, photo-b]>` (x3) and `region 1 was deleted ... expected:<[2]> but was:<[1, 2]>` (x2); the Undo and failed-delete controls pass. |
| `7e378848` | The fix (3 main files) and the test's `onPhotoDeleted` wiring line. 9/9 and `FindDeleteReappearsOnMapTest` 5/5 pass. |
| `e2e0dbe2` | Merge of `origin/journal-redesign` (dispatch files only; no conflict, README untouched). |
| report commit | This report and the README row. |

Tests (real ViewModel calls, both kinds): commit with nothing tapped; `onMapShown` while pending then commit; Undo; failed delete; plus one for the delete-at-once photo path.

## Revert checks (saved copy in `/tmp`, restored from it, never from git)

Build log `e:` lines: 0 each time. Forward change confirmed afterwards (`git status` clean; `onPhotoDeleted(photo.photo.id)` present twice, `dropOfflineRegionFromMapRecords(region.id)` once).
- **Photo** (both `onPhotoDeleted(...)` calls removed): 3 fail, all `photo-a was deleted and is still drawn ... but was:<[photo-a, photo-b]>`; 9 run.
- **Region** (the one call removed): 2 fail, both `region 1 was deleted and is still drawn ... but was:<[1, 2]>`.
Each failure is one this revert could produce and names the deleted record.

## Suite counts (XML saved to `/tmp/sib-xml` before the later runs)

**Not the full suite: full suite minus `DrawerBackOverJournalTest`** (see disclosure 3): **3185 tests, 0 failures, 0 errors, 24 skipped**, 392 result files. `assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines. The `JournalPendingDeleteTest` album tests and `JournalTabTest` From Album (-296's) passed in this run; I did not look for the `DiagnosticsPanelTest` intermittent.

## The four disclosures

**Confirmed vs inferred.** Confirmed by reading and by running: every premise above, the failing/passing tests, the reverts. Inferred: that a finger-level delete through each UI path reaches these ViewModel calls (the tests start at the ViewModel request/commit calls, not the Composables).

**Could not determine.** (a) Why `DrawerBackOverJournalTest` stalled once in the full run (below). (b) Whether the two `MainActivity` wiring lines are right on a device: no headless test exercises `MainActivity`'s `onPhotoDeleted = { id -> viewModel.onPhotoDeleted(id) }`; remove it and the suite stays green (as -291's residual 4). Checked by grep only. (c) Whether a find's own photos have a delete path I missed (grep only, stated above). -291's residuals apply here unchanged: the one-frame window between the pending slot clearing and the delete finishing, and an in-flight `loadMapRecords` that re-adds the record. Not touched. The test repeats `AvailabilityScreen.kt:887-891`'s expression, as -291's does.

**Premises that were wrong.** None of the dispatch's. Corrections of mine: the dispatch names only the pending path, but the drawer gallery and album trash photo deletes are immediate (no Undo) and had the same gap with no window at all.

**Decided beyond scope.** (1) I fixed `onDeleteGalleryPhoto`'s success path too (the dispatch names the pending commit): the dispatch's rule is "once deleted, never drawn", and those are photo deletes. One line; revert-checked. Remove it if you want the narrower reading. (2) I did not change `AvailabilityViewModel.onDeleteOfflineRegion` (no caller). (3) Build-slot rules: disk was 1975-2017 MB free at three launches (under the dispatch's 2048 MB) before the planner confirmed 3.3 GB; memory was always above 2048 MB and no wrapper or worker of another build was running. Also my first red run was contaminated (I edited source mid-build, and the test file also did not compile); I discarded it and redid the red run from committed state. The `41d6ae94` commit therefore does not compile and the first green-able failing state is `9f224373`.

**The stall (not fixed, not silenced).** The first full run hung 17 min. A thread dump of the test worker (taken before I killed it, a process my own run started) showed the main thread inside `DrawerBackOverJournalTest.touchTools` (`:420`), from the test `portrait, Back with the drawer open over the Entries album view closes the drawer and keeps the album` (`:538`), about 88 s of main-thread CPU. I re-ran with that class excluded by an init script kept in `/tmp` only (never committed) and got the counts above. `DrawerBackOverJournalTest` **alone**: on my branch 6 tests, 0 failures, 15.2 s; on base `9c806ae1` 6 tests, 0 failures, 11.5 s. So alone it does not stall on either; it stalled once inside the full-suite run. My diff touches no drawer, fan, or Back code, but I did not run the full suite on base, so I cannot say whether base stalls in the full run. Needs: a full-suite run on base, or a repeat full run here.

## Device-only list (no device was used)

For each of: drawer gallery delete, album long-press Delete (compact), album trash button, and an Offline maps list / Records row swipe-delete: delete a photo (or region), go back to Maps, wait past Undo (or, for the immediate photo paths, just go back). **Pass:** the record does not come back, and an open fan does not collect it. **Also:** Undo brings it back; a photo delete that fails (hard to cause) leaves it drawn.
