# s22-fails completion report (dispatch 2026-09-28-318)

Base: `origin/journal-redesign` at `1a43ba5c` when started; merged with its head `60d626b9` before hand-back (`git pull --no-rebase`, no conflicts). Branch `s22-fails`, pushed. Coder ran as Sonnet 5.5 (`claude-sonnet-5-5`, from the session configuration; not read back from the session). No device, no adb, no merge to any other branch.

## Fail 3: the puck is under the markers after a basemap swap

**Reproduction.** Not reproduced on a real map (native). Reproduced at the SDK's own decision, headless: `LiveLocationPuckSwapTest` "premise" test passes on the pre-fix commit `44b85497`, i.e. 13.5.0's `LocationComponentPositionManager.update` returns "unchanged" for the options the component already holds. The swap test failed on `44b85497` with "a swap needs a first placement to make the final one count" (run 1, XML in the evidence below).

**Mechanism.** Observed (javap of the pinned 13.5.0 `classes.jar`): `MapLibreMap.notifyStyleLoaded` calls `Style.onDidFinishLoadingStyle`, then `LocationComponent.onFinishLoadingStyle`, then the app's `OnStyleLoaded` callback. `onFinishLoadingStyle` rebuilds the puck's layers with the options it holds (`layerBelow` = the fan's casing layer, absent from the new style). The app's callback then adds the registry and fan layers, above the puck. `activateLocationComponent` returns early in `initialize` (already initialised) and calls `applyStyle`; `LocationLayerController.applyStyle` removes and re-adds layers only when `update` returns true, which it does not for unchanged options. A fresh `MapView` is not initialised until after the layers exist, which is why leaving Maps and returning looked right.
Inferred, not read: what the native `addLayerBelow` does with a missing layer id (the c3 frames agree with it appending on top). Of the planner's three candidates, only the first, partly, matches; the second (registry layers re-added after the puck) is true as a consequence of the order, and the third (Street's own layers) was not needed.

**Fix.** `puckReplacementOptions` (`SightingsMap.kt`) returns the options with no position when the component is already initialised; `activateLiveLocationIfPermitted` applies them just before activation, so the next application is a change and the SDK re-adds the puck's layers below `FanOutIds.LEGS_CASING_LAYER`, which exists by then. Basemap-independent: it runs on every style load, which Topo, Street, Satellite and the night toggle all share.
**Rejected:** moving the puck's layers by id (SDK-internal names); adding the app's layers through `Style.Builder` so they exist before the SDK re-places the puck (restructures working code).

## Fail 5: the front glyph pops at the fold's end and the open's start

**Reproduction.** Frames 572-618 of `c5-fold-last-frames.png` read: mushroom over camera through 610, camera over mushroom from 614. Headless: `FanIconStackingOrderTest` failed on `44b85497` (no sort key on any copy: `expected {find=18, flag=16, photo=19, pin=17} but was {null...}`; copies sorted `[find, flag, photo, pin]` against the registry's `[flag, pin, find, photo]`).

**Mechanism.** Observed: the copies share one symbol layer (`FanOutLayers.kt`, `icon-allow-overlap` true); the originals draw per registry layer, markers fixed in order (planned trips, waypoints, finds, photos; `MapLayers.kt:406-409`). Inferred from the style spec, not from the native library: with no `symbol-sort-key`, a layer with allow-overlap orders by viewport y, so feature order (the planner's reading) is not what decides it, and at progress 0 the copies tie on y.

**Fix.** Each icon copy carries `sortKey` = its layer's index in the draw order (`fanFrameCollections`' new `drawOrder`, defaulted to the registry, passed live from `SightingsMap.kt`), and the icon layer sets `symbol-sort-key` from it (`PropertyFactory.symbolSortKey(Expression)` exists in 13.5.0, javap). Dots under icons is untouched. A layer missing from the draw order is logged, not hidden.
**Rejected:** ordering the features alone (the y-ordering overrides it).

## What landed

| Commit | What |
|---|---|
| `44b85497` | Failing tests and behaviour-neutral seams |
| `24d1d7f8` | Draft of this report with the seam list |
| `ae910f86` | The two fixes |
| `b156f2c3` | Merge of `origin/journal-redesign` (`60d626b9`) |

Seams in `44b85497` (all neutral there): `puckReplacementOptions` returning null (`SightingsMap.kt`), the hoisted `options` variable in `activateLiveLocationIfPermitted`, defaulted `drawOrder` on `fanFrameCollections`, the `SORT_KEY_PROPERTY` constant, `fanIconLayerProperties()` (the same four properties moved out of a local helper). Test-only: `app/src/test/java/org/maplibre/android/location/PuckPlacementProbe.java`, in the SDK's package because `update` is package-private.

## Revert checks

Each from a copy saved before editing (`/tmp/s22-save`), the build log read for compile errors before the results (`e:` lines: 0 in all three), the files restored from the saved copy (never from git) and confirmed afterwards (`cmp` against the copy, forward lines present, `git status` clean).

| Revert | Tests that failed | Edit-specific? |
|---|---|---|
| A: `puckReplacementOptions` returns null | swap test; "first placement" test; 8 run, 2 failed | yes: only the two puck tests, the stacking class stayed green |
| B: feature `sortKey` not written | per-glyph keys; sorted order; follows-given-order; 8 run, 3 failed | yes: the layer-property test stayed green |
| C: layer `symbol-sort-key` not set | "the icon layer sorts by that property"; 8 run, 1 failed | yes: only that test |

## Suite counts (from the JUnit XML)

- Failing-first run on `44b85497` (4 classes): 27 tests, 6 failed (4 stacking, 2 swap); `LiveLocationPuckOrderTest` 3/3 and `FanOutLayersTest` 16/16 green.
- After the fix, the six named classes: `FanOutLayersTest` 16, `LiveLocationPuckOrderTest` 3, `MarkerFanOutHostTest` 10, `AvailabilityScreenFanBubbleDismissalTest` 10, plus the two new classes 4 and 4: all 0 failures.
- Full suite after the merge: 401 classes, 3246 tests, 0 failures, 0 errors, 24 skipped. `assembleDebug` succeeded, 0 `e:` lines. The 24 skips were not inspected or compared with the base; `grep @Ignore app/src/test` counts 51 occurrences, which I did not reconcile with 24.
- The four unchanged guard classes were not edited.

## The four disclosures

**Confirmed vs inferred.** Confirmed: the call order and the `update` early-outs (javap); the SDK's `update` answer (a test calling the real class); the sort-key property values pushed and set. Inferred: that the native layer honours `symbol-sort-key` over viewport y; that native `addLayerBelow` of a missing id appends on top; that the two applications in one call draw no intermediate frame.
**Could not determine.** Whether either fix works on a real map: no device. Whether the 24 skipped tests are the pre-existing ones.
**Premises that were wrong or partly wrong.** The planner's "feature order" reading of fail 5: it is not what orders an overlapping, allow-overlap layer (inferred from the spec). The dispatch's candidate "the LocationComponent keeps its old layers" is nearer to right than the other two, but the cause is the callback order, not the SDK ignoring options.
**Decided beyond scope.** The `drawOrder` parameter's default (the registry order, so unchanged tests compile); logging a missing draw-order layer; the same-package Java probe. A disk breach: the failing-tests-fix run began with 1966 MB free against the dispatch's 2048 MB floor (I printed the figure and did not gate on it); every later build was gated and ran at 2311 to 2319 MB or more. No harm seen.

## Device-only list (owner)

1. Swap to each basemap (Topo, Street, Satellite) with a folded stack over the puck: the puck is above the markers and below an open fan. Also toggle night mode, which shares the path.
2. Fold and open a mixed fan (pin, find, photo, flag), recorded at animator scale 5: no z-order pop at the fold's last frames or the open's first, and the front glyph is the camera throughout.
3. Not covered anywhere: a fan opened right after a swap still draws its legs over the puck (it did before).

Evidence: JUnit XML kept at `/tmp/s22-xml/{run1,run2,rev-A,rev-B,rev-C,full}` on the coder's machine (not committed); logs `/tmp/s22-run1.log`, `/tmp/s22-run2.log`, `/tmp/s22-rev-*.log`, `/tmp/s22-full.log`.
