# The way back drawn on the map, arrival at the start, and an exit icon (dispatch 2026-09-28-497, plan task T7)

**Status: built and pushed on `route-drawn`, not merged.** Desk check on the S22 done. The line itself, and its grey passed part, need a walk.

**Date:** 2026-10-04 (UTC).
**Dispatch:** `prompts/preserved/2026-10-04-08.md` (from `origin/records-after-166`), with Amendment 1 (the planner's rulings: B for the passed part; arrival "Twice accuracy, at least 15 m"; the control's icon and label) and Amendment 2 (four existing tests).
**Base:** `origin/main` at `bc364238`, checked against the remote; unchanged at the full-suite run.
**The owner's words,** to this session directly: "Yes, start -497" and "Yes, use the phone"; for the desk check, "Done, as expected" and "It's showing now".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Logs, the screenshot and scripts are in `~/Zynergy/device-evidence/2026-10-04-route-drawn/`. The screenshot shows a grid reference, so it stays there.

## In plain terms

1. **Tap Return:** the way home along the path walked is drawn on the map. The route as it was at Return is drawn grey and dimmed, and the way ahead from where the walker is is drawn bright azure over it. Whatever the walker has already passed therefore shows grey.
2. **If the route can't be calculated,** the last line ahead stays on the map, faded, until a route comes back.
3. **Within the larger of twice the GPS accuracy and 15 m of the start,** the start's pin becomes a ring with a check, the bright line ends, and the navigation panel's big figure reads "Arrived". Navigation stays on until the walker ends it.
4. **While navigating, the Return button is an X in a circle,** labelled "Stop navigating". Tapping it ends navigation as before; the recording carries on.
5. **Ending navigation clears the lines.**

## Verified before building (reported by message first; the planner ruled)

1. **Where the route lives.**
   - `routeHome()` (`domain/RouteHome.kt`) runs in `TrackRecordingViewModel`'s route tick, every 5 s while returning, from the track the 15 s poll last read.
   - Its route runs from the most recent stored point back to the first, then on to the origin waypoint.
   - The walker's assumed place is the first route point at least the hop's length along. No projection onto the nearest point: that is the owner's ruling for the route home.
   - `RouteHome.Ahead` exposed no line. "The part already passed" has no place in that model, so I proposed two options:
     - **B:** keep the route at Return dimmed, with the current route ahead bright over it.
     - **A:** split the line at a distance.
   - The planner ruled B.
2. **The approach rule.**
   - `isApproaching` (`domain/NavigationReadout.kt:41`): straight-line distance to the origin ≤ 2 × the fix's accuracy. On the S22 (constant 3.79 m) that is a fixed 7.6 m.
   - Its comment said the app "does not declare arrival". No record holds that as an owner ruling (searched `RECORD.md` on `records-after-166`), and -497 quotes the owner choosing "Start marker changes shape + HUD says Arrived".
   - The owner then chose "Twice accuracy, at least 15 m" (planner, Amendment 1).
3. **The control.** `ControlPill`'s return button (`ui/availability/AvailabilityMapControlsUi.kt`): `Icons.Filled.Directions`, labelled with the return sentence. `material-icons-extended` is already a dependency, so `Icons.Filled.Cancel` needed nothing new.
4. **The start marker.** All waypoints share one icon (`MarkerIcon.WAYPOINT`), and the origin is drawn only while navigating to it.
5. **The rules.**
   - The lines are map content, not chrome, so the 80% rule does not apply.
   - MapLibre layers take no touches.
   - The live recording's line is the `BREADCRUMB` layer, below `KEPT_TRACKS` in the registry (`ui/map/layers/MapLayers.kt:382-387`), so lines placed above `KEPT_TRACKS` are above it too (the planner's check).

**Two changes of plumbing,** told to the planner before building and accepted:
- **The route layers are outside `MAP_LAYER_REGISTRY`,** as the fan-out's are. `MapLayerRegistryTest` pins the registry's exact layers, count and order, and those tests may not be edited. A navigation line belongs in neither the Layers sheet nor tap routing anyway.
- **The ring is its own image and layer,** not a new `MarkerIcon`, because `MarkerGlyphsTest` pins the exact icon set. While arrived, the origin's pin is left out of the drawn waypoints.

## What was built

- **`domain/RouteHome.kt`:** `Ahead` gains `route` (the whole route) and `routeAhead` (from the walker's assumed place to the end), with defaults.
- **`domain/RouteLine.kt`** (new):
  - `RouteLine(atReturn, ahead, aheadIsCurrent)`;
  - `nextRouteLine`: the first route becomes the line at Return, later ones move only the line ahead, and a withheld route keeps the last line and marks it faded.
- **`domain/NavigationReadout.kt`:**
  - `hasArrived(distance, accuracy)` = distance ≤ max(2 × accuracy, 15 m), 15 m with no accuracy;
  - `ARRIVAL_MIN_RADIUS_METERS` = 15, provisional;
  - a pointer to -497 in `isApproaching`'s comment. `isApproaching` and "Approaching" are unchanged.
- **`ui/track/`:** `TrackRecordingUiState.routeLine`, updated by each route tick and cleared in all four places `routeHome` is cleared:
  - a recording starting;
  - a recording taken up;
  - the recording ending;
  - the route ticks ending.
- **`ui/availability/NavigationHud.kt`:**
  - `arrivedAtStart(fix, start, now)`: a fix that is not lost, within `hasArrived`. It is the one rule the HUD and the map both read.
  - The large figure reads "Arrived" while returning (a route given) and arrived.
- **`ui/availability/AvailabilityMapControlsUi.kt`:** `returnControlIcon` (Cancel while returning) and `returnControlDescription` ("Stop navigating" alone while returning, as the planner ruled; the sentence otherwise).
- **`ui/map/RouteHomeLayers.kt`** (new):
  - **Lines:** three sources and four layers, all above `KEPT_TRACKS`:
    - the line at Return: grey `#8A8A8A`, 5 dp, opacity 0.6;
    - the line ahead's casing, in the palette's casing colour;
    - the line ahead itself: azure `#0288D1`, 6 dp; while withheld, opacity 0.4.
  - **The ring:** above `WAYPOINTS`, a 36 dp white disc with an azure ring and check. It is a static swap with no animation, so Reduce Motion has nothing to suppress.
  - **Night:** lighter azure and grey.
  - **Provisional:** colours, widths and opacities, all to be judged on a walk.
- **The path to the map:** `MainActivity` → `AvailabilityScreen(routeLine)` → `CompactMainScaffold` → `CompactMapTab` (which works out `arrivedAt` and leaves out the origin's pin while arrived) → `MapOverlayContent.route` → `SightingsMap`, which adds the layers at style load and pushes the route when it changes.

## Tests

**Tests first, pushed failing** (`e2ae7680`; run `t1-tests-first`): 16 of the new tests failed against stubs. The ones that passed:
- those expecting nothing: no line yet; no route; not arrived beyond 15 m, on a lost fix, or off a return;
- two through plumbing that was already real code: the line handed to the map, and the long-press;
- the existing ViewModel tests, all passing.

Each passing one is covered by a revert below, except the long-press test (see below).

**Two of my own new tests were wrong at first,** found when the build ran:
1. **The ViewModel test expected 3 route points; there are 4,** since the route ends with the origin waypoint. The expectation was corrected; it had failed earlier on a null.
2. **The screen test read the Return control's label from the unmerged node,** where the icon's description isn't, so its tests-first failure ("null") did not prove the label. It now reads the merged node, and revert r12 bites it.

**New, in five new classes, plus two tests added to `TrackRecordingViewModelTest`** (no existing test there changed):
- **`domain/RouteLineTest`** (5):
  - the route from the most recent point to the start and origin;
  - the line ahead from the walker's assumed place;
  - the line at Return kept;
  - withheld keeps the last line, faded;
  - nothing withheld before a line.
- **`domain/ArrivalTest`** (3): with 3.79 m accuracy, 14.9 and 15 m arrive and 15.1 m does not (approaching at 8 m does not); with 10 m accuracy, 19.9 m arrives and 20.1 m does not; with no accuracy, 15 m.
- **`ui/map/RouteHomeLayersTest`** (4):
  - the line at Return to the dimmed source and the line ahead to the bright one;
  - withheld is faded (0.4);
  - arrived ends the line ahead and puts the ring at the start;
  - no route, or a one-point line, draws nothing.
- **`ui/availability/NavigationHudArrivedTest`** (5):
  - "Arrived" within 15 m while returning;
  - not beyond;
  - not off a return;
  - not on a lost fix;
  - the control's icon and label.
- **`ui/availability/AvailabilityScreenRouteDrawnTest`** (4, through the real screen with a recording stand-in map):
  - the map is handed the line and the start's pin;
  - within 15 m it is handed the ring's place, not the pin, and the HUD reads "Arrived";
  - the control reads "Stop navigating", then its own label, and ending hands the map no route;
  - real long-presses below the HUD reach the map with the route handed to it.
- **`TrackRecordingViewModelTest`** (2 added, through the real entry points and its `runRecordingTest` harness):
  - Return draws the line, a later tick keeps the line at Return, and `stopReturn` removes it;
  - stopping the recording removes it.

### Existing tests changed (Amendment 2)

All four are in `ui/availability/AvailabilityScreenMapIconStackTest.kt`, each with a "Dispatch 2026-09-28-497 changed this" line. The before-file is in the evidence folder.

| Test | Before | After |
|---|---|---|
| "inside the approach threshold the distance appears exactly once and the target column is empty" (:701) | large slot "12 m", counted once | `ARRIVED_TEXT`, counted once. Status "Approaching", empty target column and the existing absences unchanged |
| "recording with a real fix and return-to-vehicle active shows the full sentence via contentDescription" (:1619) | `isReturning = true`, sentence "Return: 180° S · 1.2 km · -45 m" | `isReturning = false`, the same sentence |
| "an off-track fix tints the return-to-vehicle button with the error color's contentDescription state" (:1702) | sentence "Return: 180° S · 500 m · elevation diff. unavailable" | "Stop navigating", still off track and returning |
| "a return distance under a kilometer is shown in meters in the return row's sentence" (:1723) | `isReturning = true`, "Return: 45° NE · 350 m · …" | `isReturning = false`, the same sentence |

**Why these changes, and why they are no weaker:**
- Arrival's radius is never smaller than approaching's, so during a return the HUD's approaching state now always reads "Arrived".
- While navigating, the control's label is "Stop navigating". The sentence is still the control's label whenever the walker is recording and not navigating, which is where the two format tests now check it.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD after each check.

**r15's first edit** (`false && …`) broke a smart cast and did not compile (3 errors). The runner refused to cite it, and r15b replaced it.

All 15 counted below compiled with 0 errors and failed for their own edit.

| Check | One edit | Fails |
|---|---|---|
| r01 | the line ahead is the whole route | the assumed-place test |
| r02 | the line at Return moves with each route | the kept-line test |
| r03 | withheld not faded | the withheld test ("faded") |
| r04 | the line kept after the return ends | the ViewModel return test |
| r05 | the line kept after the recording stops | the ViewModel stop test |
| r06 | no 15 m floor | three arrival tests; the screen's arrival test; the HUD's "Arrived" test |
| r07 | arrival on a lost fix | the lost-fix test |
| r08 | "Arrived" off a return | "never reads Arrived" off a return |
| r09 | the line ahead kept on arrival | the arrival layers test |
| r10 | never faded | the withheld layers test (0.4 against 1.0) |
| r11 | the icon unchanged | the icon test |
| r12 | the label unchanged | the screen's label test; the JVM label test |
| r13 | the start's pin kept on arrival | the screen's arrival test ("the start's pin is left out") |
| r14 | the line not handed to the map | the screen's first test |
| r15b | the map never arrives | the screen's arrival test |

**The long-press test** has no edit to revert. The route is drawn by MapLibre layers, not a Compose surface, so nothing new could intercept a touch. It guards against a future overlay rather than proving this one.

## Suite

| Run | Tree | Result |
|---|---|---|
| Tests first (`t1-tests-first`) | `e2ae7680` | 6 classes, 57 tests, 16 failures, as expected |
| Built (`t2-built`) | before `41cb9130` | 8 classes, 100 tests, 0 failures |
| Full suite before Amendment 2 (`t3-full-suite`) | `41cb9130` | 439 classes, 3581 tests, **4 failures** (the four tests above) |
| The changed class (`t4-changed-tests`) | before `e295fbe1` | 1 class, 104 tests, 0 failures, 19 skipped (its existing `@Ignore`s) |
| Full suite (`t5-full-suite`) | `e295fbe1` | 439 classes, 3581 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled:** `main` at `bc364238` has 3,558 tests in 434 classes (-477's figure). This branch adds 5 + 3 + 4 + 5 + 4 + 2 = 23 tests, in five new classes and one existing one. 3,558 + 23 = 3,581; 434 + 5 = 439.

## The desk check (S22, 1.0.2654+ge295fbe1, the owner's word)

The owner started a recording, tapped Return and then the X, and answered "Done, as expected". A screenshot while navigating (`device/navigating-arrived.png`) shows:
- the navigation panel reading **"Arrived"**, with "Approaching" below it as ruled;
- the Return button as **an X in a circle**;
- the start as **a ring with a check**;
- the recording's dotted line.

**No route line was visible.** At a desk the walker has not moved. The track likely held fewer than two stored points at Return (the service writes points in batches), and a one-point line is not drawn. Inferred.

**So the line itself has not been seen on a phone:** its colours, the grey passed part, the faded withheld line, and the line ending at arrival are device-only, for the owner's next walk.

## Disclosure

**Confirmed vs inferred.**
- Confirmed by tests: the line's data and rules, arrival, the HUD's "Arrived", the control, the clearing, the map hand-off.
- Confirmed on the S22: "Arrived", the X-circle, the ring, and the X ending navigation.
- Inferred: why no line showed at the desk; how the line looks (not yet seen).

**Could not determine.**
- How the colours and opacities read outdoors, by day and at night.
- Whether 15 m is the right floor (provisional).

**Premises that were wrong.**
- **"Never arrived" was a code comment, not an owner ruling.** It was stated as an owner decision in `NavigationReadout.kt`'s comment, but no record holds it.
- **The registry and the icon set can't take new entries** without editing their existing tests. Hence the plumbing changes above.
- **Two of my new tests were wrong at first,** as recorded above.

**Decided beyond scope.**
1. **The colours, widths and opacities,** all provisional.
2. **The line ahead's casing,** for contrast over imagery.
3. **The arrival ring's look.**
4. **The route at Return grey at 0.6 under the bright line,** so the passed part is grey and the rest is covered.
5. **A fanned origin pin** would show its normal image until the fan folds. Noted, not built for.
