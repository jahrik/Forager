# Fan-above-puck (dispatch 2026-09-28-290): completion report

Branch `fan-above-puck`, cut from `35f65c47`; `origin/journal-redesign` (`eb4cf129`) merged with `git pull --no-rebase` (clean; it only adds the dispatch file and its RECORD.md intent). No device, no adb, nothing merged to a shared branch.

## Verified premises (all at `35f65c47`)
- **Confirmed.** `addFanOutLayers` is called at `SightingsMap.kt:1087`, defined at `FanOutLayers.kt:71-97`; `LEGS_CASING_LAYER` is the first layer it adds (`:82`).
- **Confirmed.** `activateLiveLocationIfPermitted` is at `:1455`, called at `:722` and `:896`. Its builder set only `trackingAnimationDurationMultiplier` and no `layerAbove`/`layerBelow`.
- **Ordering at both call sites: confirmed.** `:722` is inside the `setStyle` callback, after `initializeOverlayLayers` (`:696`, which ends in `addFanOutLayers`), so it covers a basemap swap. `:896` uses `loadedStyle`, assigned at `:720`, after the same call. The only other `initializeOverlayLayers` caller is `:696`.
- **Corrected: `LocationComponentOptions` builds under Robolectric.** `LocationIndicatorMotionTest`'s comment (and `SightingsMapOverlayDataTest`'s, which it cites) says it cannot outside a device. For `Style` that is true; for the options it is not. So the seam exists and the test is real.
- **Cause (inferred, not observed on a device):** no position given means the puck is added on top of the style.

## What the pinned MapLibre (13.5.0) does with `layerBelow`
Established with `javap -p -c` on the classes in `android-sdk-13.5.0.aar`; there is no sources jar.
- `Builder.layerBelow(String)` exists.
- `LocationComponentPositionManager.addLayerToMap` uses `layerAbove` if set, else `layerBelow` (`addLayerBelow`), else `addLayer`. A `layerAbove` would therefore win over `layerBelow`; the test asserts none is set.
- **Not every puck layer goes through it.** `SymbolLocationLayerRenderer.addLayers` places only the topmost layer that way (bearing if `bearingOnTop`, else foreground). The foreground/bearing, background and shadow layers, the accuracy layer and the pulsing circle are each added with `addLayerBelow` the previous puck layer. So the whole puck is contiguous, directly under the named layer. (Bytecode reading, not observed.)
- **Named layer missing: not established.** The Java code does not check. The native behaviour is in `libmaplibre.so` and I did not read it. Both call sites have the layer, so it should not arise. One pre-existing window is unchanged in kind: a locate-me tap at `:896` while a `setStyle` is pending uses the previous `Style` object; that could already fail and now also names a layer the new style may not have yet.

## What landed
- `8811961b`: `liveLocationComponentOptions(context)` extracted (no behaviour change) and `LiveLocationPuckOrderTest` (3 tests). Pushed failing: `expected:<fan-out-legs-casing-layer> but was:<null>`; 1 of 3 failed, the `layerAbove == null` and multiplier tests passed, 0 `e:` lines.
- `232fee53`: `.layerBelow(FanOutIds.LEGS_CASING_LAYER)` plus the doc comments (owner quote at the builder; `addFanOutLayers` says the puck is placed below the fan).
- Mechanism choice: `layerBelow` on the existing builder, as suggested, rather than `layerAbove` on the registry's top layer, which would tie the fan to the registry's order and would not move with the fan's own layers. No layer, render mode, camera mode or multiplier changed.

## Revert check
From a copy saved before the edit (`/tmp/SightingsMap.saved`, restored from that copy, not from git). The `layerBelow` line was removed (one-line diff). Build log: 0 `e:` lines. The JUnit XML timestamp is from that run (17:55:46). Result: the same 1 of 3 failed, with `expected:<fan-out-legs-casing-layer> but was:<null>`, a failure this edit can produce. After the restore the line is present (1 match) and `git status` was clean.

## Suite
Full `:app:testDebugUnitTest`, read from the JUnit XML: 389 classes, 3173 tests, 0 failures, 0 errors, 24 skipped. No stall. The run was made on `232fee53` before the journal-redesign merge, which added no code. `assembleDebug`: successful, 0 `e:` lines.

## Device-only (not done)
1. Open a fan over the puck: every fan layer (legs, circles, dots, icons) draws above the puck, by day and by night.
2. With no fan open, the puck still draws above ordinary registry markers and tracks.
3. After a basemap swap, and after leaving Maps and coming back, the order holds.
4. The puck still tracks, rotates with the heading, and its accuracy circle still shows.
