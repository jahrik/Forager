# Dispatch 2026-09-28-299 (fan-fold-snap): completion report

Base `4f3a02ad`, verified an ancestor of `origin/journal-redesign` (`d610d90f`, which adds only the dispatch and its intent). Branch `fan-fold-snap`, pushed; merged with `origin/journal-redesign` by `git pull --no-rebase` (clean, no conflicts; merge `51626b0a`). Not merged to any other branch.

## Premises (read, file:line)
- Original marker: `markerSymbolLayer`, `SightingsMap.kt:1174-1180`, `iconAnchor(CENTER)`, no offset. **Confirmed.**
- Fan copy layer: `FanOutLayers.kt` `symbols()`, `iconAnchor(CENTER)` plus `iconOffset(get(ICON_OFFSET_PROPERTY))`. **Confirmed**; the property was `glyph.fanCentringOffsetDp()` at every progress (was `:176`).
- `release()` clears members and progress (`MarkerFanOutState.kt:59-63`); the host calls it after `animateTo(0f)` (`:81-85`). **Confirmed.** So at progress 0 a copy sat off its original by the full centring offset and snapped when cleared; the same at the first frame of an open.
- The mechanism is inferred from code; **not observed on a device.**

## Jump per glyph (computed from `MarkerGlyph`, pinned by the progress-1 test)
| Glyph | Offset at progress 0 before the fix (dp) |
|---|---|
| WAYPOINT pin | (0, 14) |
| FIND | (0, 12.5) |
| PLANNED_TRIP flag | (-7.589, 12.5) |
| PHOTO | (0, 0) |
| SEARCH_CENTRE | (0, 0) |
| sighting dots | no offset property, never snapped |

## Other last-frame causes (checked; read from code and library behaviour, not measured)
- **Final `animateTo` frame:** `Animatable` with `tween` ends on the target exactly and `FastOutSlowInEasing` returns 1f at fraction 1 (library behaviour, **inferred, not read in source here**). `release()` writes members and progress with no suspension between. `snapshotFlow` may conflate the last animated frame into the release, so the last pushed frame can be a tiny progress rather than 0; with the fix that costs (offset x progress), i.e. well under a pixel. Not a snap.
- **`fanMemberLatLng` round trip** (`FanOutLayers.kt` `fanMemberLatLng`): at progress 0 it is `toScreenLocation` then `fromScreenLocation` of the same float point, sub-pixel error. Not a cause. `member.lat/lng` comes from the probe's `locate`; that it equals the coordinate the original's source feature draws at is **unverified** here.
- **Hiding filter vs copies clearing:** both happen in one `snapshotFlow` iteration (`SightingsMap.kt` ~:798-806). Whether MapLibre applies the filter change and the async GeoJSON update in the same render frame is **device-only, could not determine.** It would show as a one-frame double or missing icon, not a positional snap.
- No other positional cause found.

## The circle
Read: `FanClarity.kt:31-34,65-66` (`FAN_CIRCLE_DIAMETER_DP` = 36, opacity `MAP_CHROME_OVER_MAP_ALPHA`), `FanOutLayers.kt` `fanCircleProperties`/`applyFanCircleStyle`. **Radius 18 dp and opacity are constant at every progress; no fade code exists.** So at the fold's end the circle was fully drawn (and popped off at release, a disc appearing or vanishing at each end). Per the dispatch I stopped and asked. **Owner ruled: fade/shrink the circle.** Built: each circle feature carries `circleScale` = progress; `circleRadius` = 18 dp x that (data-driven expression). Nothing is drawn behind a glyph at rest, nothing pops, full 36 dp when spread. Consequence to see on device: at mid-fold the circle is smaller than the glyph's body and off-centre from it (e.g. progress 0.5: pin body 7 dp off the circle's centre, radius 9 dp).

## What landed
- `c9437d6b` tests first: required `progress` parameter added to `fanFrameCollections` (ignored), plus 4 tests (progress 0, 0.5, 1 offsets across all five glyphs; circles' scale). Pushed failing.
- `4063f0ee` the fix: offset = centring offset x progress; circle scale property and radius expression; KDoc on `fanCentringOffsetDp()` and `fanFrameCollections` quoting the owner. Ring placement, duration, easing untouched; circle's full size (36 dp) unchanged.
- `51626b0a` merge of `origin/journal-redesign`.
- Red run (c9437d6b's `FanOutLayers.kt` swapped in from a saved copy, forward copy restored after): `FanOutLayersTest` 16 tests, 3 failed, each for the predicted reason: `waypoints-layer copy's icon-offset y at progress 0 expected 0.0 but was 14.0`; same at 0.5 (`expected 7.0 but was 14.0`); circles' scale `expected [0.0, 0.0] but was [null, null]`. Progress-1 test stayed green (today's values). Data: `docs/audits/data/2026-09-30-fan-fold-snap/red-FanOutLayersTest.xml`, `red.log`.

## Revert check
From a saved copy (`/tmp/s299/FanOutLayers.kt`, not git): dropped the scaling (`add(offset.xDp); add(offset.yDp)`), stale XML deleted first. Build log: 0 `e:` lines. `FanOutLayersTest` 16 tests, 2 failed: `...waypoints-layer copy's icon-offset y at progress 0 expected 0.0 but was 14.0` and `... at progress 0.5 expected 7.0 but was 14.0`: each names this edit. Progress-1 and circle tests stayed green, as a revert of the offset alone should leave them. Forward change restored from the saved copy; `git status` clean and the scaling line present (grep 1). The circle-scale revert was **not** separately revert-checked: the red run above is its only evidence (it failed for `null` scale, i.e. the property absent).

## Suite (from the JUnit XML, saved at `/tmp/s299/fullxml`, 391 class files; run log `docs/audits/data/2026-09-30-fan-fold-snap/full.log`)
Full `:app:testDebugUnitTest` on the fix, before the merge: **3186 tests, 24 skipped, 0 failures, 0 errors**; BUILD SUCCESSFUL, 4m 4s, no stall, no class excluded. The owner-held flakes did not fail in this run. `assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines. **Not re-run after the merge** (the merge brought -297 and -298 code; the dispatch asked for the pull before hand-back, and the Gradle slot was released).

## Disclosures
- **Confirmed vs inferred:** confirmed by reading: the offset mechanism, the per-glyph numbers, the circle's constancy, and by test: the feature output at progress 0, 0.5, 1. Inferred: that this is the owner's "sometimes", the animator's exact final frame, and the conflation.
- **Could not determine:** whether the filter and source update land in one render frame; whether the data-driven `circleRadius` expression renders as intended on device (`Expression.product(literal, get)`, an SDK call no test reaches); `icon-offset` units on device (irrelevant at 0, matters mid-fold for the pin/find/flag).
- **Premises that were wrong:** none. The planner's offsets and the no-offset dots were right.
- **Decided beyond scope:** nothing beyond the owner's circle ruling; chose shrink (radius x progress) over also fading opacity, and scale = raw progress (easing already applied by the host). Circles under dots scale too, for consistency.

## Device-only (S22; none run here)
1. Fold a fan of pins, finds, flags and photos, slowly if the animator scale allows, and watch the last frame: no icon jumps.
2. The same for the first frame of an open.
3. The circles near the end of a fold and the start of an open: they grow and shrink to nothing, nothing pops; check mid-fold it does not look wrong with the circle smaller than and off-centre from the glyph.
4. Day and night.
