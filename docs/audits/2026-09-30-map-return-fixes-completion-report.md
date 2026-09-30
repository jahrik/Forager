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
