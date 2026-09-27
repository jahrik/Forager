# 2026-09-27: Picker and offline-maps fixes, completion report

Dispatch: `prompts/preserved/2026-09-27-24.md` (build, picker and offline-maps fix stage).
Specification: `docs/audits/2026-09-27-offline-picker-recenter-diagnosis.md`, including its
correction and the owner's rulings. Written by the coder in a cloud worktree on local branch
`fixes`, which tracks `origin/journal-redesign`.

**Status: F1, F2, F3 and F4 are built, tested, revert-checked and pushed. F5 is not built. It
stopped on an open design question:** removing the corner button also removes the only delete from
the wide tree's Journal album, and the dispatch does not say what should happen there (see Needs a
decision, item 1). Final suite: 244 classes / 1988 / 1 / 0 / 24. The one failure is the
known-intermittent `JournalTabTest` photo-pull test, and its class passed 17/0 when rerun alone.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `70f4bef2d54a85a7c886746ba554bc191b06fb76` | F1 signal: `MapRenderMode.onUserCameraGesture`, wired in `SightingsMap`; `UserCameraGestureTest` (2). No behaviour change. |
| `faffd231b01953039aae57dd0e18eaa2b6770ecb` | F1 tests first: `JournalTabTest` 3, `LogPanelTest` 2; `PanRecordingMapSlot` stub. |
| `ab52a032aaf30d1744901ba9debb9fd242d6ece2` | F2/F3 tests first: `OfflineMapsPanelPickerTest` 4. |
| `72c9e12e898470f6ede910b9137c5202cc81a740` | F1 fix, which is also F2's fix: the picker follows its region until touched. |
| `0086f8fa48585e25db535d1e5a252d4b1e05a80d` | F3 fix: after a touch, a radius-only change keeps the panned centre. |
| `cd5f0bfefaea7fdda4421fc2fe448d29e76003d6` | F4 refactor: `offlineRegionIdsToDelete`. The rule and the result are unchanged; the call order changed (below). |
| `fa08a7c9b27bfeebd400d74a886bb71669aa5779` | F4 tests first: `OfflineRegionIdsToDeleteTest` 3. |
| `53ecbff37d15223fab29c87ce713bebbdb030eeb` | F4 fix: in-flight ids tracked; the list reload skips them. Final code head. |
| this report's commit | This file. |

Production diff `3025bab..53ecbff`: 4 files, `MapLibreOfflineMapRepository.kt`,
`CentrePinLocationPicker.kt`, `MapSlot.kt`, `SightingsMap.kt`. Test diff: 6 files, 3 of them new
test classes and 1 a new shared stub. Nothing outside the dispatch's file list was touched.
`JournalTab.kt`, `LogPanel.kt` and `AvailabilityOfflineMapsUi.kt` did not need to change, because all
three call sites share `CentrePinLocationPicker`.

## Premises checked before building

- **Base.** `origin/journal-redesign` was `3025babcae1bb77904c94e986532fd8b43045900` when fetched. That
  is the planner's store-copy commit after `c5ad2e7`, as the dispatch allows. Its diff from `c5ad2e7`
  touches only `RECORD.md` and `prompts/`.
- **Baseline**, cleared results directory, `LC_ALL=C.UTF-8`, `--offline`, build log with no `e: `
  lines: **241 classes / 1974 / 0 / 0 / 24**. This matches the planner's figure.
- **Runner** (scratchpad `pf/run.sh`, `pf/revert.py`, `pf/reverts.sh`). Every run clears
  `test-results`, refuses to read results when the build log has `e: ` lines, and refuses a run with
  no XML. It refused one run, which is not cited: the first F1 base run did not compile because the
  `swipe` import was missing. `revert.py` refuses an edit whose old text does not occur exactly once.
  It saves a copy before editing, restores from that copy (never from git) and compares byte for
  byte. `git status --short` was empty after the revert batch.
- **The diagnosis's find-picker chain, re-read at this base:**
  - `JournalTab.kt:356` and `LogPanel.kt:278` pass `region = findLocationPickerRegion(deviceLocation, …)`.
  - `FindLocationPickerRegion.kt:21-22` builds a new `Region(fix, 1 km)` per fix.
  - `CentrePinLocationPicker.kt:113` (at base) was `var cameraCenter by remember(region)`.
  - `SightingsMap.kt`'s data and camera effect is keyed on `region` and moves the camera when it is
    not tracking (`shouldMoveCameraToTarget`). Holds.
  - I did not re-read the `liveFix` rate and `acceptLiveFix` links (`AndroidLocationTracker`,
    `LiveFixGate`). The tests do not depend on them.
- **Offline picker:** `AvailabilityOfflineMapsUi.kt:129` (`defaultCenter`), `:146-150`
  (`pickerRegion` from the slider's `offlineMapRadiusKm`) and `:161-173` (the picker call) match the
  diagnosis.
- **F4:** `download()` creates the region at `:109` and writes Room at `:120-131` (at base).
  `listRegions()` deleted every region not complete (at base, `:172-176`). Holds.
- **Gesture signal: none reached the picker.** At base, `CentrePinLocationPicker` passed
  `MapRenderMode(basemap, night)` and `{ location -> cameraCenter = location }` as `onCameraIdle`.
  `SightingsMap` registered no camera-move-started or move listener. The `onCameraIdle` doc comment in
  `MapSlot.kt` itself says it fires for "a programmatic jump" too.
- **MapLibre reasons, checked with `javap -c` on the pinned 13.5.0 `classes.jar`:**
  - `REASON_API_GESTURE = 1`, `REASON_DEVELOPER_ANIMATION = 2`, `REASON_API_ANIMATION = 3`.
  - Every `onCameraMoveStarted(1)` is dispatched from a `MapGestureDetector` listener (tap, move,
    scale, rotate, shove, and `MapGestureDetector$3`).
  - `Transform` (API camera moves) and `MapView$3` dispatch 3. No class in the `location` package
    calls `onCameraMoveStarted`.
  - Conclusion: the first activation's `easeCamera` and the tracking camera go through `Transform` and
    report 3. That second link, for tracking, is inferred from the grep, not traced call by call.

### Premises that were wrong or imprecise

- **The ViewModel-init citation.** The dispatch cites `AvailabilityViewModel.kt:133` and "its
  comments" for the orphan-cleanup intent. At this base the init call is `loadOfflineRegions()` at
  `:145`. Its doc comment (`:787`) says "once at startup, and again every time `onOfflineMapsOpened`
  fires" and says nothing about orphans. The orphan intent is written in the repository itself, at
  the `isComplete` check in `listRegions`: "A region OfflineManager still has on file but never
  finished — e.g. the process was killed mid-download … A restart bypasses that catch block
  entirely". F4 keeps that behaviour. The rest of the chain holds.
- **F5 and "the only delete there".** The dispatch says the wide tree's drawer `PhotoGalleryScreen` is
  unchanged. It does not mention that the wide tree also shows `EntriesAlbum`:
  `AvailabilityScreen.kt:1160` calls `LogPanel`, and `LogPanel.kt:366` calls `CartographyScreen`
  without `onRequestDeleteGalleryPhoto`. `CartographyScreen`'s `EntriesToolbar` toggle (`:368`) then
  reaches `EntriesAlbum` with `onRequestDeletePhoto = null`, so the corner button is that album's only
  delete. See Needs a decision, item 1.
- **"Tests that drove the corner button move to long-press, with a count."** No test drives the
  album's corner button. Every `"Delete this photo"` test is in `PhotoGalleryScreenTest` and composes
  `PhotoGalleryScreen`. The count would have been 0.

## What was built

**F1 signal (`MapSlot.kt`, `SightingsMap.kt`).**
- `MapRenderMode.onUserCameraGesture: () -> Unit = {}` is new.
- `SightingsMap` registers `addOnCameraMoveStartedListener` and calls it only when
  `isUserCameraGesture(reason)`, which is `reason == REASON_API_GESTURE`.
- **Why a `MapRenderMode` field:** the Compose compiler crash at 10 declared parameters, recorded on
  `MapOverlayContent`; `MapSlot` is at 9. The convention stated on every `MapRenderMode` field puts
  additions there.
- **Rejected alternatives:**
  - a 10th `MapSlot` parameter (the crash, and it would change every implementation);
  - widening `onCameraIdle` to carry a flag (every implementation changes: 12 main files and 28 test
    files declare a `MapSlot`);
  - `addOnMoveListener`, which reports pans only, not pinch, rotate or double-tap. The owner's rule is
    "until you touch it".
- The default `{}` is non-capturing, so the main map's `MapRenderMode` (`AvailabilityScreen.kt:840`,
  which does not pass it) is unchanged.

**F1 and F2 (`CentrePinLocationPicker`).**
- The picker keeps its own `mapRegion`, `cameraCenter` and `touched`.
- Until `onUserCameraGesture` fires, a new caller region replaces both the region handed to the map and
  the pin, as `remember(region)` did before.
- After it fires, a caller region with a new centre changes neither.
- This is in the shared component, so the find picker in both trees and the offline picker all get the
  rule from one commit. F2's rule ("M1 gets the same rule", recorded as consistency, owner: "Offline
  maps doesn't do this currently, but it may as well echo the same behavior") is that same commit.
- F2's test was therefore committed before the F1 fix, so that it could be seen failing.

**F3 (`centrePinMapRegion`, a pure function in the same file).**
- After a touch, a caller change of radius alone hands the map `Region(panned point, new radius)`. The
  pin and OK keep the panned point, and the zoom follows the radius as it did before.
- A change of centre and radius together is ignored after a touch. This is the find picker's first fix
  after an early pan, which swaps the 15 km search region for the 1 km device region. Following it
  would re-zoom to 13, which is exactly what F1 forbids.
- Before any touch, the caller's region applies whole (today's behaviour).
- See Decisions, item 1, for why this is inferred from the region change rather than taken from a new
  parameter.

**F4 (`MapLibreOfflineMapRepository`).**
- `offlineRegionIdsToDelete(completeById, inFlightIds)` returns the incomplete ids minus the in-flight
  ids.
- `download()` adds the new region's id to a `ConcurrentHashMap` key set from `OfflineManager`'s
  `onCreate` callback itself. It removes the id in a `finally` that covers the create call, the
  download and its failure-delete.
- `listRegions()` passes a snapshot of that set. Incomplete regions are still never listed.
- The set is in memory, so a region a killed process left incomplete is in no set and is still
  deleted. `AppContainer.kt:188` holds one repository per process.
- The refactor commit changed one thing observably: all statuses are read before any delete, instead of
  interleaved per region. The deleted set and the returned list are the same.
- **How the repository is tested today:** it is not tested directly. `OfflineManager`/`OfflineRegion`
  cannot be constructed off a device. `MapLibreOfflineRegionMetadataTest` tests its other pure piece
  (the metadata bytes). `AvailabilityViewModelOfflineMapsTest` uses a fake repository, and its doc
  comment at `:81-83` leaves `OfflineManager` behaviour to this class. F4 follows the metadata test's
  pattern.

**F5: not built** (see Needs a decision, item 1).

## Tests: base failures

Every tests-first commit was run at its own base, with cleared results and a clean build log.

- **F1 at `faffd23`** (signal present, picker unchanged): 3 of 5 fail, each on the snap-back itself.
  - `JournalTabTest` "after a pan…": `Did not expect any node … 'Pin at: 45.6100, -122.7100'` (the
    second fix).
  - `LogPanelTest`, same test: the same message.
  - `JournalTabTest` "a pan made before the first device fix…": `'Pin at: 45.6000, -122.7000'` (the
    first fix).
  - The failing line in each is the check after the fix (stack traces: `JournalTabTest.kt:362/408`,
    `LogPanelTest.kt:242` in that run), not the check after the swipe. So the swipe had registered and
    the pan had set the pin before the fix moved it.
  - The two "before any pan" tests pass: they describe today's behaviour, and R1b below shows they
    bite.
- **F2/F3 at `ab52a03`:** 2 of 4 fail for the stated reason. F2 after-pan fails with the pin at the
  late device centre (`45.6000, -122.7000`). F3 after-pan fails with the pin reset to the built-in
  centre (`39.8283, -98.5795`).
  - **The first F3 run failed for a wrong reason and is not cited:** the slider's `swipeRight()` did
    not move it (`the slider must actually have moved. Actual: 8`). That also meant the before-pan
    slider test had passed without the slider moving: a check that could not fail. Both slider tests
    now assert the radius changed, and the drag is an explicit swipe from 10% to 90% of the slider's
    width. On the second run the before-pan tests pass with the slider moved (8 to 20 km).
- **After the F1 fix (`72c9e12`):** F1 and F2 pass. F3 after-pan fails only on the region
  (`expected:<Region(lat=45.7, lng=-122.9, radiusKm=20)> but was:<Region(lat=39.8283, lng=-98.5795,
  radiusKm=8)>`), which F3's commit fixes.
- **F4 at `fa08a7c`:** 2 of 3 fail with the in-flight id deleted (`expected:<[]> but was:<[2]>`;
  `expected:<[3]> but was:<[3, 2]>`). The complete-region test passes by construction.

What the UI tests use:
- the real `JournalTab`, `LogPanel` and `OfflineMapsPanel`, with the device fix or `AvailabilityUiState`
  held in state;
- a real `performTouchInput { swipe }` on `PanRecordingMapSlot`, which fires `onUserCameraGesture` on
  drag start and `onCameraIdle(P)` on drag end, and records every distinct region it is handed;
- the real radius `Slider`, moved by a real swipe;
- the real OK button, checked through "Found at …" or the `onRegionPicked` value.

The assertions are on the pin text, on the recorded region list (unchanged after the pan, or an exact
`Region`), and on what OK saved.

## Revert checks

All build logs were clean (the runner found no `e: ` lines), every file was restored identical by
`cmp`, and the tree was clean afterwards. Each failure below is one that its edit can produce.

| Behaviour | Edit | Observed |
|---|---|---|
| **F1: touch recorded** (the dispatch's "F1's revert") | `remember { { touched = true } }` -> `remember { { } }` | 5/34: F1 after-pan in both trees and the early-pan test, all showing the new fix; F2 after-pan (`45.6000, -122.7000`); F3 after-pan (`39.8283, -98.5795`) |
| F1 other half: follow before touch | `mutableStateOf(false)` -> `true` for `touched` | 3/34: the three "before any pan" late-centre tests (`Pin at: 45.6000, -122.7000` not found) |
| Gesture filter | `==` -> `!=` in `isUserCameraGesture` | 2/2 |
| F3: radius-only keeps the panned centre | branch returns `shown` | 1/34: `expected Region(45.7, -122.9, 20) but was Region(39.8283, -98.5795, 8)` |
| F3: centre is the panned point, not the caller's | branch uses `caller.lat/lng` | 1/34: `… but was Region(39.8283, -98.5795, 20)` |
| F3 must not fire on a centre-and-radius change | the centre-equality guard dropped | 1/34: the early-pan test, a region `(45.7, -122.9, 1)` reached the map |
| F4: in-flight not deleted | `- inFlightIds` removed | 2/3: `was:<[2]>`, `was:<[3, 2]>` |
| F4: orphan still cleaned | the function returns `emptySet()` | 1/3: `expected:<[3]> but was:<[]>` |

**Not revert-checkable headless:** `SightingsMap`'s listener registration, and the repository adding
and removing ids (native `OfflineManager`). Both are listed as device items.

## Tests moved or rewritten

| File | Change | Count |
|---|---|---|
| `ui/log/JournalTabTest.kt` | harness only: `deviceLocation` held in state and passed; `setScreen` takes a `mapSlot` (default unchanged) | 0 tests changed, 3 added |
| `ui/log/LogPanelTest.kt` | the same harness change | 0 tests changed, 2 added |

Nothing was disabled, skipped or weakened. No main-map test changed.

## Suite before and after

- Before, at `3025bab`: 241 classes / 1974 / 0 / 0 / 24.
- After, at `53ecbff`, cleared results directory, build log clean: **244 classes / 1988 tests /
  1 failure / 0 errors / 24 skipped.**
  - +3 classes: `UserCameraGestureTest` 2, `OfflineMapsPanelPickerTest` 4, `OfflineRegionIdsToDeleteTest` 3.
  - +14 tests: the 9 in those classes, plus `JournalTabTest` 3 and `LogPanelTest` 2.
- **The one failure is `JournalTabTest`'s known-intermittent photo-pull test.** "From Album on the edit
  form opens the picker and pulls the selected photo into the entry" failed with `The component with
  ContentDescription = 'Log photo' … is not displayed!`.
  - Rerun alone, as the dispatch says: `JournalTabTest` 17/0/0.
  - It also passed in every other `JournalTabTest` run in this session: 9 runs (two base runs, two
    forward runs, five revert runs), counted from each run's failure list.
  - It was not touched. This work did not change the photo-pull path (a `git diff` of `3025bab..HEAD`
    shows no change to it). That is shown by the diff, not by a demonstration.

## Planner's predictions

1. **Holds.** F1 needed a gesture signal. It went on `MapRenderMode`, reached through `MapSlot`
   (see What was built for why not a new `MapSlot` parameter).
2. **Holds.** F4 is testable only through the extracted pure function.
3. **Holds for tests and for the main map's camera.** No main-map test changed. The main map's
   behaviour changed only by one registered listener that calls a no-op.
4. **Holds.** +14 tests (12 to 30 predicted), 0 new failures, skipped unchanged at 24. The one red is
   the known intermittent, outside this work.

## Needs a decision

1. **F5: the wide tree's Journal album would lose its only delete.** The compact tree passes the
   long-press Delete (`MainActivity.kt:508` → `AvailabilityCompactScaffold` → `JournalTab` →
   `CartographyScreen` → `EntriesAlbum.onRequestDeletePhoto`). The wide tree's `LogPanel.kt:366` does
   not pass it, so there the album photo is tap-only apart from the corner button, as J4b recorded
   ("`LogPanel` gets no … photo menu"). Options:
   - (a) Remove the corner button only where the long-press Delete is wired (`onRequestDeletePhoto !=
     null`). The compact album gets exactly what was asked; the wide album keeps its button until J6.
   - (b) Remove it everywhere. The wide album then has no delete of its own; its photos stay deletable
     from the wide drawer's `PhotoGalleryScreen`.
   - (c) Wire the long-press Delete into `LogPanel` too, then remove the button everywhere. `LogPanel`
     beyond the find-picker call is outside this dispatch's scope.

   Also affected in every option: `EntriesAlbum.onDeletePhoto` and its dialog use become unused in
   (b) and (c), and removing that parameter would touch `CartographyScreen` (outside scope). Nothing
   is built. The work is small once decided, and it needs its own tests-first commit (a "no corner
   delete" assertion, which fails today, and long-press Delete still working).
2. **The residual F4 window.** A `listRegions()` whose status reads land between MapLibre creating a
   region natively and delivering `onCreate` on the main thread could still see the new region
   unmarked, and delete it. Marking inside `onCreate`, not after the coroutine resumes, narrows the gap
   to MapLibre's own callback delivery. Closing it fully would need a "create pending" state that makes
   every incomplete region un-deletable while a create is in progress. I did not build that: no data
   shows the case, per CLAUDE.md. Options: leave it, or build that state.

## Decisions I made that the dispatch did not

1. **F3's "radius-only change" is inferred from the region change** (same centre, new radius), not
   given by a new parameter. The F1 rule and the F3 rule conflict in one case: the find picker's first
   fix after an early pan changes centre and radius together. Telling the two apart by what changed
   keeps every call site as it was.
   - Rejected: a `followRadiusAfterTouch`-style flag, a conditional threaded into a shared component
     for one caller.
   - Rejected: freezing the region after a touch outright and letting F3 not re-zoom. The dispatch
     allows that ("the zoom may follow"), but it would drop the slider's only visual feedback on the
     picker.
2. **What counts as "touching"** is any gesture MapLibre reports as `REASON_API_GESTURE`: a pan, and
   also a pinch, rotate, tilt, fling or double-tap zoom. The owner's rule is "until you touch it", and
   a pinch also ends tracking.
3. **The in-composition state writes** in `CentrePinLocationPicker`. A new caller region is applied in
   the same composition pass rather than in a `LaunchedEffect`, so the map is never handed one frame of
   a stale region. The writes converge in one pass.
4. **Test additions beyond the dispatch's list:**
   - the early-pan case ("a pan made before the first device fix is kept"), because it is where F1 and
     F3 meet;
   - a complete-region F4 test;
   - `UserCameraGestureTest`, which pins the reason constants to the values `javap` printed.

## Device-only items (not verified; Robolectric composes no `SightingsMap`)

1. **The find picker on the S22 Ultra**, compact and wide. Open Add Location with location on, pinch in
   to about 18, pan, and wait 10 s. Pass: the view does not drop to zoom 13 and does not return to the
   device. OK saves the panned point (the entry's "Found at"). This is also the only check that
   `SightingsMap`'s listener is wired and that a real pan reports `REASON_API_GESTURE`.
2. **The first-fix follow.** Open the find picker with location just enabled (no fix yet), and do not
   touch. Pass: the picker moves to the device when the fix arrives. It is the inferred "tracking
   reports 3" link that keeps this from counting as a touch.
3. **The offline picker after a slow first fix.** Open Offline maps with GPS cold, pan before the
   location lands, and wait. Pass: nothing moves. Then drag the radius slider. Pass: the map stays on
   the panned point and re-zooms; OK and "Download region" show the panned point.
4. **A download kept while the Offline maps list is reopened.** Start a download, leave Offline maps and
   return (or switch chips) while it runs. Pass: "Downloading N / M tiles" continues, and the region
   appears in the list on completion. Then force-stop the app mid-download and reopen. Pass: the
   orphan is gone and the list is correct.

## Flags outside scope

- **The offline tile estimate uses the caller's centre.** `estimateServedOfflineTileCount(pickerRegion)`
  (`AvailabilityOfflineMapsUi.kt`) still reads the panel's own region, not the panned point. Tile count
  varies with latitude, so the estimate can differ slightly from what the panned region would download
  before OK is tapped. After OK it reads the picked point. Not changed.
- **The picker map's search-centre reticle** (`showSearchCentre` default `true`) draws at the region
  handed to the map. After a touch that is no longer where the pin is, which was already the case
  during any pan before this work. Not changed.
