# F4: stacked markers fan out on tap (dispatch 2026-09-28-197): completion report

**Status: in progress.** This first section is the pre-registration, pushed before any code or test is written. Later sections are appended as "Resumed" sections; nothing above them is rewritten.

**Model:** the session is configured for `claude-sonnet-5-5`; whether that is what served each turn I cannot read from inside the session.

**Base.** The dispatch names `c3cde3d7`. At the start `origin/journal-redesign` was `34c98256`, two commits on (`75875dae` "F4 base c3cde3d7", `34c98256` the F1 terminal record); `c3cde3d7` is an ancestor of it (`git merge-base --is-ancestor`, true). The diff `c3cde3d7..34c98256` is `RECORD.md` and `prompts/preserved/2026-09-29-40.md` only, so nothing this dispatch touches moved. The worktree was cut from `34c98256`.

**What governs, quoted.** From `prompts/preserved/2026-09-29-39.md`: "Stop and report, rather than choose: the ring's radius and the spiral's spacing, if they cannot be derived from the markers' own touch size, which is at least 48 dp; anything the fanned-out markers would cover, or be covered by: the icon cluster, a legend, the chip row; a stack mixing kinds (a photo and a find) that the existing tap priority would resolve differently from a fan-out." From the plan (`docs/plans/journal-redesign.md`, "Fan-out: the behaviour"): the owner, verbatim, "Confirm 1 to 5 / 6 Give it a .4s animation speed. / 7 confirm".

## Premises checked at the base

| Premise | Result |
|---|---|
| A tap reaches the top glyph only; PHOTOS above FINDS; marker layers not reorderable | Read. `TapPrecedence.kt:25-35` (`tapWinner`), `:47-53` (`resolveTap`); `MapLayers.kt:226` (`marker(...)` sets `userReorderable = false`, as the F1 report says); registry order at the end of `MAP_LAYER_REGISTRY` (`:358`): sightings, planned trips, waypoints, finds, photos. |
| The map click listener is the only entry for a map tap | Read: `SightingsMap.kt:442` (`map.addOnMapClickListener`), queries at `:456` and `:460`, outcome `when` after. |
| Marker symbol layers draw every icon even when overlapping | Read: `markerSymbolLayer` sets `iconAllowOverlap(true)` (`SightingsMap.kt:1033`). So a stack is drawn, not culled, and `queryRenderedFeatures` can return all of it. |
| A `MapView` cannot be built under Robolectric | Stated at `OfflineStyleSwapTest.kt:187` and repeated in the F1 report; not re-tested by me. It is why the tap logic has to be reachable without one. |
| Back: `MapBubbleLayer` registers `BackHandler(enabled = backEnabled && tapped != null)` at `MapBubble.kt:253`, unconditionally composed | Read. A `BackHandler` composed later takes priority, so a fan-out handler composed **only while the fan is open** sits above every handler already on screen. That ordering is my inference from the dispatcher's documented last-registered-first behaviour, and is asserted by a test (below), not just assumed. |
| Camera listeners | Read: `addOnCameraMoveStartedListener` at `SightingsMap.kt:508`, whose reason is the only user/programmatic discriminator. |

## Design, derived rather than chosen

Everything numeric comes from the 48 dp touch size (`TAP_BOX_DP`, `TapPrecedence.kt`), so the first stop condition does not fire.

- **A touch area** is a 48 dp square centred on the marker's own coordinate. Two markers are a stack when both |dx| and |dy| between their coordinates are under 48 dp at the current zoom. A stack is the tapped marker plus every marker overlapping *it* (not a transitive chain).
- **Ring, up to 8:** radius `max(48, 48 / (2 sin(pi/n)))` dp, first marker at 12 o'clock, clockwise. Adjacent centres are then at least 48 dp apart, and the ring clears its own centre by a touch size.
- **Spiral, more than 8:** an Archimedean spiral from radius 48 dp growing 48 dp per turn; each marker is the first point along it that is at least 48 dp from every marker already placed. No fudge factor.
- **Order:** the layer drawn on top first (photos, finds, waypoints, planned trips, sightings), then feature id.
- **Motion:** 400 ms (`FAN_DURATION_MS`), the same for fold-back. Animator scale 0, read as `isReduceMotionEnabled` (`ui/motion/ReduceMotion.kt`), snaps to the end.
- **Folds on:** a tap on anything but a fanned marker (then the tap goes on as it would have), any camera move starting (user or programmatic, since the copies are placed in screen space and a moving camera would strand them), Back, and any change to the map's content or style.

## The three stop conditions, read

1. **Ring radius and spiral spacing:** derivable from the touch size, as above. Not a stop.
2. **What the fan covers or is covered by:** the fan is drawn in the map's own layers, which sit under every Compose overlay. A stack near the icon cluster, a legend or the chip row is therefore covered by it, and a covered fanned marker cannot be touched. I add no edge avoidance, because choosing one (shifting the ring, capping the spiral) is a design decision the owner has not made; it is reported, not decided. Screen edges clip in the same way.
3. **A stack mixing kinds:** rule 1 says "markers" and names no kind, so a photo over a find fans both, and each opens its own bubble. The existing priority (photo wins) is what made the find unreachable; the fan-out resolves it by design, so I read this as the motivating case and not as a conflict. **Unverified:** whether the owner meant sightings (the circle-layer dots, `TapGroup.MARKER` like the rest) to fan too. I follow the registry (every `TapGroup.MARKER` layer), and list it under Decisions.

## Predictions and pass conditions (before anything is built)

Tests are in three new classes, all Robolectric-headless, none needing a `MapView`.

| Class | Prediction at the stubs (fails, for the stated reason) | Pass condition after |
|---|---|---|
| `MarkerFanOutGeometryTest` | Stubs return the tapped marker alone and all-zero offsets: every stack, ring and spiral test fails on "expected N members / distance >= 48 but was 0". | Ring n=2..8 and spiral n=9,12,30,60: every pair of positions at least 48 dp apart, equal radius on the ring, first at 12 o'clock; stack at two zooms differs. |
| `MapTapHandlerTest` | The stub handler reports every tap as a plain tap: the fan tests fail on "fan not open", the each-marker tests on "no feature tap". The lone-marker and tap-elsewhere tests are controls that **should pass at the stubs** (existing behaviour). | A stack tap opens a fan and calls no sink; a tap on each fanned marker calls `onFeatureTap` with that marker's layer and id; each fold trigger empties the fan. |
| `MarkerFanOutHostTest` | The stub host never animates: timing tests fail on "progress expected > 0 at 200 ms", reduced-motion on "expected 1.0"; the Back-order test fails on the fan handler never running. | Progress is strictly between 0 and 1 at 200 ms, 1.0 at 400 ms (and under 1 at 399), the same reversed for fold; 1.0 at once with animator scale 0; Back folds the fan and does not reach an earlier `BackHandler`, the second Back does. |

**What the tests cannot reach, stated now.** MapLibre's GL rendering (the fan layers, the hidden originals, the legs), `queryRenderedFeatures`, the projection, and the listener wiring in `SightingsMap` are device-only. The handler under test is the class the click listener delegates to, given a fake `MapProbe` with a real Web-Mercator projection; the fake stands in for the SDK, so a mistake in the SDK-facing adapter is invisible to these tests.
