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
