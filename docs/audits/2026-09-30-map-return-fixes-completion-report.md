# Map return fixes (dispatch -267): item 8c fan reopen, and the destroyed-MapView loop

Coder: Sonnet 5.5 (`claude-sonnet-5-5`, read from the session's own system prompt; not verifiable any other way). Base `edafc8c3` (verified equal to `origin/journal-redesign` at start). Branch `map-return-fixes`. `CLAUDE.md` at base is identical to the copy the session was given.

## Pre-registration (written and pushed before any build)

### Part B (read from code and from `b-x-i8-full.log`, before any device run)

- Log evidence: `~/Zynergy/device-evidence/2026-09-30-part-3/b-x-i8-full.log` has 10852 lines naming `getMetersPerPixelAtLatitude` after the MapView was destroyed, first at 20:42:00.590, immediately after a `PositionManager ... receive gps location` at 20:42:00.544 and again once a second. `b-x-logcat-full-1.txt` has `cancelTransitions`, `jumpTo`, `getCameraValues` at 16 ms spacing.
- Read (javap of `org.maplibre.gl:android-sdk:13.5.0`, `LocationComponent`): `onDestroy()` is `return` (a no-op). `onStop()` calls `onLocationLayerStop()`, which cancels every location animator and calls `locationEngine.removeLocationUpdates(currentLocationEngineListener)`. `getMetersPerPixelAtLatitude` is called from `org.maplibre.android.location.Utils` only.
- Read (`SightingsMap.kt:388-407`): the `DisposableEffect(lifecycleOwner)` calls `mapLibreMap?.locationComponent?.onDestroy()` then `mapView.onDestroy()` in `onDispose`. `onStop()` is only called from the `ON_PAUSE` lifecycle event, which a tab switch does not produce.
- **Prediction B1:** a MapView removed from composition by a tab switch leaves its `LocationComponent` registered with the location engine and its animators running, so each GPS fix calls into the destroyed native view (the 1 Hz lines) and the location/camera animators loop against it (the 16 ms burst, under tracking). The first destroyed-view line therefore appears within a second of the first tab switch that removes a MapView that had activated its location component, and stops when `locationComponent.onStop()` is called in the dispose.
- **Pass B (device):** on the fixed build, the Journal to Maps round trip and the Records track sheet round trip, logged from a cleared buffer from the app's start, give **0** lines matching `after the .MapView. was destroyed`, and taps on the map still answer. **Fail:** any such line. The prediction is wrong if the first line appears before any tab switch, or if it persists after `onStop()` is added (then two failed... no: then it is one failed fix and the next step is data, not a third guess).
- Headless: the disposal is extracted to a function over an interface the project owns; the test asserts that `stopLocation()` is called before `destroyView()`. Fails today (today's disposal never calls it). The device is the proof of the real symptom.

### Part A (read from code only so far)

Path read: `AvailabilityCompactMapUi.kt:640-650` (`returnMemory.remember` on Open in Journal, reads `openFanKeys`), `SightingsMap.kt:788-791` (the only writer of `openFanKeys`), `MapReturnMemory.kt:52-58` (`onFindClosed` puts keys in `fanRestore`), `SightingsMap.kt:564-573` (the only reader: `takeFanKeys()` at a camera idle when `loadedStyle != null`, then `openFanFor`), `fanout/MapTapHandler.kt:100-108` (`openFanFor`, `markersOf` via the probe), `MapTapHandler.kt:111-114` (`onCameraMoveStarted` and `onContentChanged` both `fan.fold()`), `SightingsMap.kt:763-765` (`LaunchedEffect(loadedStyle, ...)` calls `onContentChanged`).

Nothing is logged on the take path when `takeFanKeys()` returns null, nor when `openFanFor` succeeds, so "nothing logged" excludes only the `openFanFor == false` warning, not the other branches.

Candidate causes, none yet distinguished:
- H1: `openFanKeys` is empty when Open in Journal is tapped (fan already folded, or the writer never emitted), so `fanRestore` is null.
- H2: no camera idle arrives after `loadedStyle` is set on the recreated map, so `takeFanKeys()` is never called.
- H3: the idle arrives, `openFanFor` opens the fan, and `onContentChanged` (the `LaunchedEffect` at `SightingsMap.kt:763`, which runs when `loadedStyle` changes, or when a record list re-emits) or `onCameraMoveStarted` (a tracking camera) folds it again. No line would be logged by either.
- **Prediction A:** H3 (opened, then folded by `onContentChanged`), because the take is gated on `loadedStyle != null` and `loadedStyle` is also the key of the effect that folds. Stated as a prediction, not a finding; H1 and H2 are not excluded.
- **Difference from the stub** (`AvailabilityScreenReturnToMapTest.kt:152-160`): the stub sets `openFanKeys` directly and reads `pendingFanKeys`; it never runs the writer effect, the idle listener, `openFanFor` on a real probe, or the fold effects.
- **Pass A (device):** with temporary `Log.d` on the writer, the take, `openFanFor`, both folds, the reproduction on the owner's 6-member stack names which of H1/H2/H3 happened, by the presence or absence of specific lines. **Pass for the fix:** after Back the fan is visible at 1 s and still at 10 s, and a fan with a deleted member reopens with the survivors.

## Completion (continuation 2026-09-28-270, with amendment -272)

Coder: Sonnet 5.5 (`claude-sonnet-5-5`, from the session's system prompt). **Not finished: the device work stopped on a permission refusal (below).** Head when I started: `0f6c894c` (verified equal to `origin/map-return-fixes`).

### What landed
- `49d05c18`, `4a3cf337`, `1e75dd4b`, `87248e97` (temporary logging), `406eedc0` (logging removed, `FanReopenCoordinator`): the previous coder's, as listed above.
- `0f6c894c`: the previous coder's uncommitted edits, committed by the planner's session. **I kept them unchanged.** `FanReopen.kt`: the reopen waits for the content effect with the style loaded and a camera idle. `MapTeardown.kt`: `stopLocationUpdates()` runs first.
- `9d0cdc5a`: merge of `origin/journal-redesign` (`git pull --no-rebase`). The three new commits were record notes only; fan-clarity had **not** merged, so there was no conflict. No app source changed.

### Which hypothesis the log names (Part A)
**H3, matching the prediction.** `a-reopen.log`, 21:30:28.971 `takeFanKeys -> 9`; .977 `openFanFor(9) -> true`; .979 `onContentChanged effect fired loadedStyle=true` then `fan.fold, from: onContentChanged`. The fan opened and was folded 2 ms later by the effect keyed on `loadedStyle`. H1 is excluded (keys were written and taken, 9) and H2 is excluded (idle #2 arrived with the style loaded). The fan has 9 members, not 6.

### Tests
- `MapTeardownTest` (1) and `FanReopenCoordinatorTest` (4) pass on the fix.
- **Revert checks** (from saved copies in `/tmp/mrf`, sha256 in `saved.sha`; built and read the log, no `e:` compile errors):
  - removing `stopLocationUpdates()`: `MapTeardownTest` fails with "the map's teardown never stopped its location component: [destroyLocationComponent, destroyMapView]";
  - restoring the old gate (open at the first style-loaded idle): `FanReopenCoordinatorTest > an idle before the style-loaded content effect still leaves the fan open` fails with "the fan is open after both events: the effect folded it".
  - Restored from the copies; `git status` clean and both forward changes present afterwards.
- **Full suite, after the merge:** 386 classes, 3137 tests, 24 skipped, **0 failures, 0 errors**. None of the owner-held flakes or `DiagnosticsPanelTest` failed.

### Device (S22 `R5CT321008R`), fixed build 1.0.1985+g9d0cdc5a, `install -r` only
The phone was on 1.0.1979+g87248e97 (the diagnostic build), not 1.0.1971+g5d429f19 as the dispatch said.
- **Part B, Journal to Maps, three round trips, from a cleared buffer, app force-stopped and launched with `am start -n`: 0 lines** matching `after the .MapView. was destroyed` and 0 `getMetersPerPixel` lines, in 19220 logged lines (`fixed-b-full.log`). Baseline on the unfixed build: 14 (`b1-roundtrip.log`). Map taps answered after the round trips (`fx-b2-tap.png`: a fan opened).
- **Part A, Back from Open in Journal on the owner's stack** (9 members, opened the 6b find card): **the fan is visible at 1 s and at 10 s** (`fx-a2-1s.png`, `fx-a3-10s.png`; before: `fx-a0.png`, journal: `fx-a1-journal.png`). Focus was read with `dumpsys window` before the one Back key; no system prompt appeared.
- Logcat was streamed to a file, so the 5 MiB buffer cap (`-G 16M` was clamped) did not matter. I stopped the `adb logcat` I started (pid 1199118) and no other process.

### Not done, and why
- **Deleted-member case (-272): not run.** To name the id of the find to delete I tried to copy `forager.db` off the phone with `run-as ... cat` and query it on the host. The permission system refused it (PII Data Handling). Per the dispatch I stopped and did not try another route. **No record was created or deleted.** Find 6b ("DEVICE CHECK 2026-09-30 6b", in the owner's fan) is the intended member to delete; its id is not known to me.
- **Part B, Records track sheet round trip: not run.** I stopped at the refusal. The Journal round trip (above) is the only Part B scenario covered; the 16 ms burst was seen only after the track sheet.
- **Machine check breach:** the `assembleDebug` build started with 1918 MB available (limit 2048) while the other coder's wrapper was running. It succeeded. Earlier, my first test build died ("Gradle build daemon disappeared unexpectedly") when the other coder's wrapper started; I waited for it to clear and re-ran.

### Temporary logging
Gone. `grep -rnE '"TEMP ' app/src/main` returns 0 lines on `9d0cdc5a` (a bare `grep TEMP` matches `TEMPERATE_*` and `GLYPHS_URL_TEMPLATE`, which are unrelated).

### Device-only list
1. The deleted-member reopen (8 survivors after one member is deleted), on S22-B's test finds or by the owner.
2. The Records track sheet round trip with zero destroyed-view lines and normal taps.
3. The owner's S26 Ultra, which showed 8c first, on the fixed build.

### Addendum (same continuation, later): the deleted-member case FAILED on the device; cause not confirmed

**Supersedes** the "Not done" bullets above for the deleted-member case, the "restore" statements and the device-only list.

- **Phone history, stated plainly.** At the owner's request I copied the app's private data to `~/Zynergy/device-backup/2026-09-30-forager-s22/` (tar, sha256 in `SHA256`; includes `forager.db`, photos, datastore), uninstalled the app, reinstalled 1.0.1985+g9d0cdc5a and extracted `databases/` and `files/` back; sizes of the six photos and `mbgl-offline.db` match the backup. The reinstall reset the app's runtime permissions (location, photos); the owner answered the location prompt. The owner's own records were not deleted by me. The database reads were refused twice by the permission system (PII); I did not obtain any record id.
- **Redo of Part A on the restored data** (`redo2-*.png`, `redo2-full.log`): fan open (9 members), tapped "DEVICE CHECK find 2", Open in Journal, one Back after a focus read: fan visible at 1 s and at 10 s. Passes.
- **Deleted-member case, run by the owner** (I could not send the taps: the permission system refused them twice, once as "dangerous", once as PII). The owner's words: "I tapped on the fan, opened it, tapped on the find, deleted the entry from the menu, and it returned to the map and closed the fan." **That is a failure of the expected behaviour** (the fan should reopen with the survivors). Deleted on the device, by the owner, all test finds: "DEVICE CHECK find 2" (first run), and a second find the owner deleted to repeat it (name not given to me). **Ids: not recorded.** I had no logcat running for either.
- **Cause: not confirmed.** From code only: `MapReturnMemory.onFindDeleted` (MapReturnMemory.kt:66) filters the deleted key and `AvailabilityScreen.kt:1211` returns to Maps, so the reopen path is wired. The likely candidate (hypothesis H4, unproven) is that the delete re-emits `findMarkers`/`photoMarkers`, which re-runs the content effect at `SightingsMap.kt:775` after the fan has reopened, and that effect folds it (`FanReopenCoordinatorTest > a later content change folds the reopened fan` shows that is today's behaviour). Fixing it needs a decision on what a content change should fold, which I did not make.
- **Diagnostic build.** I built and installed 1.0.1987+g8f3f282c with temporary `Log.d` lines (`8f3f282c`) to capture it; no fan was left in the phone with a find to delete (the two mushrooms were deleted), so no capture was made. The lines are **reverted** in `5d2eb2a1`; `git diff 9d0cdc5a HEAD -- app` is empty and `grep -rnE '"TEMP ' app/src/main` returns 0. **The phone is still running the diagnostic build 1.0.1987**; S22-B's relaunch replaces it.
- **Records track sheet round trip: not run.** **The restore to the backup was not needed and not run**; S22-B's relaunch restores the phone (owner's instruction).
- **Device-only list now:** (1) the deleted-member reopen, with logcat from a cleared buffer, to confirm or refute H4 (needs a find in a fan; create one beside the stack with the green +, then delete it from its Journal page); (2) the Records track sheet round trip; (3) the owner's S26 Ultra.

## Continuation -274: the deleted-member reopen, fixed under the owner's rule "Fold only if members change"

Base verified first: remote `map-return-fixes` was `4e45d17b` (Step 0 merge `39f97501` plus Step 1 logging) before any push from this session. This session ran in a **different worktree** from the one the dispatch names (`forager-wt/map-return-fixes`), detached at `4e45d17b`, and pushed with `git push origin HEAD:map-return-fixes`; the named worktree and its checkout were not touched. Three plain fast-forward pushes, no rebase, no amend.

### What landed
| Commit | What |
|---|---|
| `796ee5e9` | Removes the temporary logging (four files restored to their `39f97501` content). `grep -rnE '"TEMP ' app/src/main` → **0 lines** here and again on the final tree; the installed APK carries 0 `TEMP` strings in its dex. |
| `e361aeff` | Tests first, failing (8 red of 25 in the three affected classes). |
| `d3d68ef2` | The fix. |

### Step 0 (merge of fan-clarity), stated as checked
The 14 targeted classes at `39f97501` (no logging): **111 tests, 0 failures**, no compile error. Only **13 of the 14** produced a result file: `MarkerFanOutPlacementScreenTest` produced none and **I did not find out why**, so it is unverified. At `4e45d17b` 33 of the same 111 failed with `Method d in android.util.Log not mocked`: the Step 1 `Log.d` calls run inside pure-JVM tests. That is the logging, not the merge; it is why the logging came out before the red tests were written.

### Step 1: the hypothesis the log names is H4
`c-step1-h4-excerpt.txt` (from `step1-logcat.txt`, the capture from a cleared buffer from the app's launch; the owner had asked me to drive, so the taps were mine). Test find `ZZTEST274`, id `f0808a76-5e52-43b0-8c4b-300e590e7450`, created by the green + and deleted from its Journal page, in the same session:
```
02:08:30.686 MapReturnMemory: TEMP onFindDeleted(f0808a76-…e590e7450): remembered=f0808a76-…e590e7450 fanKeys=8
02:08:30.965 MapReturnMemory: TEMP takeFanKeys -> 7
02:08:30.965 MarkerFanOut: TEMP openFanFor keys=7 found=7
02:08:30.978 MarkerFanOut: TEMP fan.open 7 members [...]
02:08:30.978 MarkerFanOut: TEMP reopen keys=7 openFanFor=true
02:08:40.937 MarkerFanOut: TEMP coordinator.onContentEffect styleLoaded=true
02:08:40.938 MarkerFanOut: TEMP onContentChanged fanOpen=true from: onContentEffect:26 < ...
02:08:40.938 MarkerFanOut: TEMP fan.fold, from: onContentChanged:117 < onContentEffect:26 < ...
```
The reopen succeeds; a content effect folds it 10 s later. `c-step1-del-1s.png` shows the fan open, `c-step1-del-10s.png` shows it folded. **Not determined:** what re-emits the content 10 s after the reopen with nothing tapped; the fix does not need it (a re-emit that touches no member no longer folds), but it is unexplained.

### The rule and how it is built
The owner's rule, verbatim: an open fan stays open through map changes that don't touch its members; a member disappearing re-fans the survivors, or closes if fewer than 2 remain; a layer switch that hides the fan's layer still closes it.
- `MapTapHandler.onContentChanged` (`MapTapHandler.kt`): a fan whose members are all still drawn, at the same lat/lng, on a layer that is switched on, is **left alone** (not reopened, so no animation restart). Otherwise it goes through `openFanFor` with the surviving keys (no second path), and folds if that returns false.
- **A style change** is not named by the rule and **still folds**: `FanReopenCoordinator.onContentEffect(styleLoaded, style)` now takes the `Style` (`SightingsMap.kt` passes `loadedStyle`), and a style different from the last one calls the new `MapTapHandler.onStyleChanged` (= `fan.fold()`). I did not decide anything about keeping a fan through a style reload.
- **Premise that was wrong:** `openFanFor`'s comment said a layer switched off "is left out"; the code (`fanOutLayerIds(orderedLayers(...))`) never dropped a hidden layer, since `orderedLayers` orders and does not filter. Case 4 of the rule needs the handler to know layer visibility, so `MapTapHandler` gained a `layerDrawn` input (default all on), and `SightingsMap` passes it from `layerPaintFor(spec, currentLayersState).visible`. This also makes `openFanFor` do what its comment said. **This goes beyond the letter of the dispatch and is a decision the owner may want to look at.**

### The tests
New `FoldOnlyIfMembersChangeTest` (through `MapTapHandler.onMapTap`/`onContentChanged` and `FanReopenCoordinator`; the return case uses the real `MapReturnMemory`): case 1 unrelated change (also asserts no restart via `generation`), 2 survivors re-fan, 3 fewer than two folds, 4a/4b layer switched off, 5 the deleted-member return (still open after the effects that follow), 6 a new style still folds, 7 a moved member re-fans at its current place, plus a reopen that leaves out a layer switched off. Case 6 fails at its **setup** line in the red state (same always-fold cause); its real assertion is exercised by revert R3 below. Cases 3 and 4a pass in the red state too (the old rule folds); they guard the other half and are covered by R4/R5.
- **Updated, superseded tests:** `FanReopenCoordinatorTest` `a later content change folds the reopened fan` → `…that leaves the members alone keeps the reopened fan` (named in the dispatch). **Also** `MapTapHandlerTest` `a change to what the map draws folds the fan` → `…that takes the fan's members folds the fan`: it asserted the same superseded rule on a change touching no member, so it could not stay; the dispatch did not name it. Both say so in a comment quoting the rule.
- **Test double changed:** `FanOutTestScene.markersOf` no longer applies the fan's hidden originals. The live `MapLibreProbe.markersOf` reads the record lists, not the rendering (`FanOutLayers.kt:278`, `locate`), so the double was reporting an open fan's own members as absent, and after the fix 6 of the new cases still failed until this was corrected. The red-state evidence for cases 1, 2, 5 and 7 was therefore taken with the old double; the revert checks below were run with the corrected double and are what shows those tests bite.

### Revert checks (saved copies restored, never from git; results deleted before each run; build log checked; forward files verified identical afterwards; `git diff` empty)
Each ran the three affected classes: 25 tests every time.
- **R1 any content change folds** → 7 fail, including case 1 (`the fan stayed open through changes that don't touch its members`) and case 5 (`the fan of the survivors is still open after the effects that follow the delete`); case 6 fails at its setup line.
- **R2 no survivor re-fan** → 3 fail: cases 2, 7, 4b. Case 1 still passes, as it should.
- **R3 a replaced style routed as a content change** → 1 fails: case 6 on its real assertion, `a replaced style folds the fan, unchanged`.
- **R4 never fold on content** → 6 fail: 2, 3 (`one survivor is not a fan`), 4a, 4b, 7 and the updated `MapTapHandlerTest`.
- **R5 layer switch ignored** → 3 fail: 4a, 4b, and the reopen-leaves-out-a-layer test.

### Suite
`./gradlew :app:testDebugUnitTest` on `d3d68ef2`: **BUILD SUCCESSFUL, 388 classes, 3160 tests, 0 failed, 24 skipped.** The 24 skips are in five classes this change does not touch (`AvailabilityScreenMapIconStackTest` 19, `…TripPlanningFlowTest` 2, `…WaypointFlowTest` 1, `…OfflineCacheTest` 1, `GenerateFungiIndexDbAsset` 1); my diff adds no `@Ignore`, `@Disabled` or assume line. No owner-held flake and no `DiagnosticsPanelTest` failure appeared in this run.

### Device (S22 Ultra, `R5CT321008R`), fixed build `1.0.2001+gd3d68ef2`, installed with `install -r`, logcat from a cleared buffer from the app's start (`c-fixed-full.log`, 42,049 lines, 02:44 to 02:50). Evidence in `~/Zynergy/device-evidence/2026-09-30-map-return-fixes/`.
1. **Deleted-member return: pass.** `ZZFIX1` (created, opened in Journal from the fan, deleted from its page): the fan of the 7 survivors is open at 1 s (`c-1-deleted-1s.png`) and at 10 s (`c-1-deleted-10s.png`).
2. **Unrelated change with a fan open: pass, by a substitute.** Adding a find with the green + goes to the Journal form and leaves the Maps tab, so it cannot test a fan staying open. I switched the **Recording trail** layer (not a fan layer) off with the fan open: fan still open at 10 s (`c-2-toggle-off.png`, `c-2-after-1s.png`, `c-2-after-10s.png`), then back on. Not the dispatch's own example.
3. **8c, Back with no delete: pass.** Fan open (8 members) with `ZZFIX1`'s bubble at 1 s and 10 s (`c-3-back-1s.png`, `c-3-back-10s.png`).
4. **Records track sheet round trip: pass.** Journal → Records → Tracks → the 18-point track sheet (`c-4-track-sheet.png`), closed, Maps, a tap on the stack opened the fan (`c-4-map-tap-answers.png`), Journal ↔ Maps once more. `grep -c 'after the .MapView. was destroyed'` → **0**, `getMetersPerPixelAtLatitude` → **0**, `FATAL EXCEPTION` → 0, in a log that includes 333 `PositionManager … receive gps location` lines (location component live) and every tab switch above. The two lines matching a bare `destroyed` are `InputTransport` and `SurfaceFlinger`, unrelated. The unfixed baseline was 14 lines on a Journal round trip (`b1-roundtrip.log`).
5. **Extra, not required:** with a fan open, switching **Photos** (one of the fan's own layers) off re-fanned the fan to its 2 remaining members (the find and the waypoint) and did not fold it (`c-4-sheet.png`, `c-4-photos-off-2s.png`); switched back on (`c-4-sheet-restored.png`). This is the `layerDrawn` wiring the unit tests cannot reach.

### Test data
Created and deleted, by name: `ZZTEST274` (id above) and `ZZFIX1` (id not captured: the fixed build has no logging). Nothing else created or deleted; none of the owner's records touched; the database was not read. The Journal afterwards shows Finds 3, All 10, all three dated 2026-09-29, as before. Layer switches changed and restored: Recording trail, Photos. I did not tap Undo. The phone is left on the fixed build.

### Observation, not explained and not caused by this change
After `ZZFIX1` (and, earlier, `ZZTEST274`) was deleted, a magenta find marker stays drawn on top of the stack at that spot, even folded (`c-1-folded-after-delete.png`), while the Journal has no find there. The same marker is in `c-step1-del-10s.png` on the diagnostic build, so it precedes this change. Cause not investigated; a stale marker in the map's find source is a guess, not a finding.

### Housekeeping
- Logcat: this session started three streams and stopped its own two (PIDs 1210760 and 1223820). PID 1207282 (`c-step1-full.log`) belongs to another session and was not touched.
- The other session and this one both drove the phone; my `install -r`, `logcat -c` and app restart at about 01:45 may have appeared in its capture.
- Machine gates were checked before every build (available memory 2915 to 7623 MB across the run; disk about 7.5 GB; no Gradle wrapper running). One reading, 1999 MB, came before a build and no build was started on it.
- A local `local.properties` was copied into this worktree (gitignored) with its SDK path case fixed (`Sdk` → `sdk`).
- **Not done:** `docs/audits/README.md` was not given a row (it is the serialization point and the dispatch did not ask for one); `RECORD.md` was not touched.

### Device-only list now
(1) The owner's S26 Ultra on the fixed build (`1.0.2001+gd3d68ef2`), the deleted-member return and 8c; (2) the stale find marker above, if it matters to the owner; (3) whether a style reload with a fan open should keep the fan, which the rule does not name and I left folding.

## Continuation -280: the deleted-member return on the merged build, diagnosed and **not reproduced**

Base verified first: `d2e23b3e` is an ancestor of `origin/journal-redesign` (`6e34c700`, one docs commit ahead) and contains `d3d68ef2` and `3e9fca0d`; the app code at `d2e23b3e` is identical to the build S22-B failed on (`2efc2163`, 1.0.2011). Branch `map-return-delete`, worktree `/home/zynergy-labs/Zynergy/forager-wt/map-return-delete`, pushed to `map-return-delete` only.

### What was asked, and the result in one line
S22-B saw "Find deleted", **no fan and no bubble** at 1.5/6/12/18 s after deleting the 8c member of a 9-member fan on 1.0.2011. **I could not reproduce it in four runs on a diagnostic build of the same code, and the log names none of the four suspects.** No fix was built, because there is nothing that failed to fix; the report is negative and says so.

### The diagnostic build
`eebb9650` and `39392484` (temporary `Log.d` on `MapReturnMemory`, the coordinator, `openFanFor`, `onContentChanged`, `onStyleChanged`, `onCameraMoveStarted` with the MapLibre reason and whether GPS following is on, every `fan.open`/`fan.fold` with caller, and the camera-restore decision on a first style load). Built as 1.0.2014+g39392484, installed with `install -r --user 0` over 1.0.2011. **Reverted** in the commit that carries this section, from the saved copies of the files taken before the logging went in (never from git); `grep -rnE '"TEMP ' app/src/main` returns **0 lines**, and `git diff d2e23b3e -- app/src` is empty.

### The four runs (S22 Ultra, user 0 only; all logs in `~/Zynergy/device-evidence/2026-09-30-map-return-delete/`)
All on a 9-member fan at S22-B's stack (the trip flag, a waypoint, my test find, six photos), tapped open with a real tap, the test find opened from the fan, page name read before every delete, Entry options, Delete entry.
| Run | Route | Result | Key log lines |
|---|---|---|---|
| 1 `280a` | fresh launch | fan of 8 open at 1/6/12 s | `08:05:02.077 onFindDeleted(0bad53da…) fanKeys=9`, `08:05:02.337 takeFanKeys -> 8`, `openFanFor keys=8 found=8 missing=[]`, `reopen … openFanFor=true`, `08:05:12.332 onContentChanged fanOpen=true members=8 drawn=8 unchanged=true` |
| 3′ `280b` | S22-B's sequence: locate button (following ON, 494 GPS-driven camera moves in 30 s), hand pan of 100 px (`reason=1 gesture=true following=false`), waited ~19 s, then the fan | open at 1/12 s | `08:09:10.437 styleLoad firstStyle=true savedSnapshot=true savedFollowing=false previousMode=null restoreMode=8`, `takeFanKeys -> 8`, `openFanFor=true`, `unchanged=true`; no camera move after the return |
| 2 `280c` | same process as 3′, second delete | open at 1/6/12 s | `08:12:20.539 takeFanKeys -> 8`, `openFanFor found=8`, `08:12:30.535 unchanged=true` |
| 4 `280d` | same, with the device forced into deep idle | open at 1/6/12 s | `08:17:52.822 onFindDeleted … fanKeys=9`, `08:17:53.106 takeFanKeys -> 8`, `openFanFor found=8 missing=[]`, `08:18:03.050 unchanged=true` |

### The four suspects, against these runs
1. **GPS following: refuted for S22-B's sequence.** A hand pan clears the *saved* flag as well as the live one (`savedFollowing=false`, `restoreMode=8` = `CameraMode.NONE`); the return did not re-enable following, and no camera move followed the fan opening.
2. **The planned trip in the fan: refuted.** It was a member of every fan and `found=8 missing=[]` after each delete.
3. **Fan-centring (`4f16e3b1`): not implicated**; the runs are on code that contains it.
4. **Keys never remembered or taken: refuted.** `remember(…) openFanKeys=9`, then `takeFanKeys -> 8`, every run.
The dispatch says to stop and report when none is named; I did.

### The power-state lead (relayed by the planner) and what run 4 does and does not show
S22-B's `b2-logcat-full.log` carries 531 `FreecessHandler: freeze com.zynergylabs.forager.app(10424) result : 12` lines every 6 s through its delete, and `restrictJobsByOlaf: restrict=true, uid=10424` at 06:35:33; its premises check read "Doze ACTIVE". My four captures have **0** of those lines (re-counted in `g-run4-full.log`: 0 freeze lines naming the app, 0 `restrictJobsByOlaf` for uid 10424).
Run 4 recorded and then changed the power state (`g-power-before.txt`, `g-power-forced.txt`, `g-power-after.txt`):
- before: `deviceidle` deep and light `ACTIVE`, force `false`, charging `true`, USB powered, level 100, status 5, standby bucket 5;
- `dumpsys deviceidle force-idle` → deep `IDLE`, light `OVERRIDE`, force `true`, held through the delete (`IDLE` read at the tap and again after);
- undone with `dumpsys deviceidle unforce` and `dumpsys battery reset`; read back: deep and light `ACTIVE`, force `false`, `mForceIdle=false`, USB powered, level 100, status 5, bucket 5. (I did not run `battery set`, so `reset` changed nothing that I had changed.)
**What run 4 does not show:** forcing deep idle did **not** make the app produce the Freecess/Olaf freeze lines S22-B's process had (0 in my capture), so run 4 is not S22-B's state, only a deep-idle run. The difference in the Freecess record between S22-B's log and mine is the only concrete lead left, and I could not induce it.

### Not determined
- **Why S22-B's run failed.** Left standing: (a) something specific to S22-B's device state (its process had been up since ~07:20 and the Freecess/Olaf restriction was on from 06:35; my runs never had it); (b) a timing effect that adding logging removes (I cannot exclude it, and think it unlikely); (c) a route that never reached `MapReturnMemory.onFindDeleted` (the 2011 build had no such logging, so its log could not show one).
- **A single failure, one device, one run.** S22-B's report is one observation. I reproduced nothing in four attempts. I am not claiming the return is proven correct in S22-B's state, only that on this code, in the ways I could put the device, it works.

### Tests, revert checks, suite
None added, none changed: no code changed, so there is nothing for a revert check to bite and no new suite run is claimed. The `-274` tests (`FoldOnlyIfMembersChangeTest` and the updated coordinator and handler tests) stand as recorded above. The base's own suite result (3163 / 0 / 0 / 24 at `2efc2163`, per the planner's record) is **not re-run here and is cited, not verified, on this branch**.

### Test data
Created and deleted, by name: `DEVICE CHECK 2026-09-30 280a` (`0bad53da-8628-4eae-bd6a-699dead6a38e`), `…280b` (`1726c24a-4c5d-42c2-808d-75f87a4a33b7`), `…280c` (`bd2c3a81-d85b-40bd-a341-bc502424af2e`), `…280d` (`8c303a98-0907-45e6-a123-0831fdc38559`). Journal after every run: Finds 7, All 14, Tracks 3, as S22-B left it. S22-B's trip, its finds i6A to i6D, its drafts and the owner's own records were not touched; the database was not read; the Dual App profile (user 95, no Forager) was not touched.
**One draft was added by my own mistake.** After run 3′ I sent five taps in a row without a screenshot between them; the sequence left the app on Journal → Entries with "Saved to Drafts / Discard" at 08:09:42. I did not tap Discard and deleted no draft. Drafts read 7 before S22-B's work, 8 at the start of this continuation, 9 afterwards and did not change during the clean runs. I cannot tell from the list which draft is mine; the planner ruled that S22-B's restore to `a-copy/` covers it.

### Observations, not investigated
- After a delete, a magenta find marker stays drawn at the stack while the Journal has no find there (`d-1-after-12s.png`, `f-2-after-12s.png`); seen on 1.0.2001 earlier today too, so not this change's.
- The tap coordinates for the stack and the Open in Journal button move with the fan and the map's pan; I re-read the screen before each.

### Device-only list
(1) S22-B, or the owner on the S26 Ultra, repeating the deleted-member return on a build with **no** logging, in the state that produced the failure, now that the code path is known to work in four runs: a second failure with the same code would make the Freecess/Olaf lead the next thing to instrument. (2) The stale find marker above, if it matters to the owner.

### State left on the phone
- **Build:** 1.0.2011+g2efc2163, S22-B's own `b2-app-debug-2efc2163.apk` (sha256 `4f124d3ef9d56efa…`, matching the planner's `4f124d3e…`; 0 `TEMP` strings in its dex), installed with `install -r --user 0 -d` over my diagnostic 1.0.2014 (`-d` because the version code goes down). Read back: `versionName=1.0.2011+g2efc2163`; user 0's `ceDataInode` is unchanged (2259049), so the data was kept; user 95 still has no Forager. The app was launched once (focus: Forager `MainActivity`).
- **Power state:** deep and light `ACTIVE`, force `false`, as at the start of this continuation.
- **Logcat:** my streams are stopped (`d-step1-full.log`, `d-run3-full.log`, `g-run4-full.log`); S22-B's stream (PID 1261930) was not touched.
- **Phone free** for S22-B's restore.
