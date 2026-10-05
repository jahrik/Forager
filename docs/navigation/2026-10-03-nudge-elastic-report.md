# A small nudge gives and springs back while navigating (dispatch 2026-09-28-463)

**Status: built and pushed on `nudge-elastic`, not merged. Desk step done on the S22** with the owner doing the gestures.
- The nudge, the drag and the spring behaved as built.
- One fault was seen that this dispatch did not cause and does not touch: the map can stay tilted after Stop (below).

**Date:** 2026-10-03 and 04 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-10.md` (from `origin/records-after-160`).
**Base:** `origin/main` at `bdb27f03` (PR #160, -457), checked against the remote; unchanged at the full-suite run.
**The owner's words,** to this session directly: "Yes, start -463", then "Yes, use the phone".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-nudge-elastic/`.

## In plain terms

1. While navigating, a small push of the map with one finger now moves it, but stiffly, half as far as the finger.
2. Let go before a finger's width, and the map springs back to following in a quarter of a second, going slightly past and settling.
3. Drag further, and the map lets go and stops following, and "Return to Route" appears, as before.
4. A pinch is unchanged: it zooms and keeps following.

## Verified before building (reported by message first; the planner accepted it, no stop)

Read from the pinned MapLibre 13.5.0 `classes.jar` by `javap`; no sources are cached.

1. **What can move the map during the give without ending following.**
   - **MapLibre's own pan cannot.** With tracking-gesture management on (-457), the location camera's move listener raises the move detector's threshold to our 48 dp and interrupts the gesture (`LocationCameraController$6.onMoveBegin`). Neither the map nor the app's move listener hears of a nudge. Past the threshold, `onMove` sets mode NONE: today's leave.
   - **An app camera move cannot.** `MapLibreMap`'s camera calls notify the developer-animation listener, which sets mode NONE (`LocationComponent$10.onDeveloperAnimationStarted`). This re-reads -433's finding.
   - **The padding while tracking can.** `LocationComponent.paddingWhileTracking` → `LocationAnimatorCoordinator.feedNewPadding` → `LocationCameraController.setPadding` → `Transform.moveCamera`. That path does not go through `MapLibreMap`, gives no developer-animation notice, and leaves following on.
     - The walker sits at the centre of the padded area, so widening one side by twice the give moves them, and the map with them, by the give.
     - Padding is screen space, so the give holds under the 45° tilt and a compass-facing bearing.
     - Only ever widened, so no side goes negative. The view's own padding is (0, top, 0, 0) (`ui/map/NavigationView.kt`, `applyView`).
   - **The finger** is read from the touch listener the map view already has (`ui/map/SightingsMap.kt`). It still returns `false`, so MapLibre's handling of every touch, and hit-testing, are unchanged.
   - **Padding is refused** in mode NONE and while a mode transition runs. The give is gated on both, so nothing is sent then.
2. **The spring.**
   - MapLibre animates padding linearly only: `playAnimators` always passes a `LinearInterpolator`. So the overshoot is the app's own: a `ValueAnimator` of 250 ms whose frames set the padding at once through Android's overshoot curve, written out as `nudgeSpringOffset`.
   - Position, bearing and padding are separate animators in the coordinator, so tracking keeps moving the walker under the give and the spring.
3. **Constants and the threshold.**
   - Named and provisional, beside `NAVIGATION_NUDGE_THRESHOLD_DP` in `ui/map/NavigationView.kt`:
     - `NAVIGATION_NUDGE_GIVE_RATIO` 0.5;
     - `NAVIGATION_NUDGE_SPRING_MILLIS` 250;
     - `NAVIGATION_NUDGE_OVERSHOOT_TENSION` 2.0, Android's default, which peaks 13% past rest (1.5 would be 8%).
   - The threshold still measures the finger's travel (MapLibre's detector, unchanged), so a real drag means what it did. The map gives at most half the threshold, 24 dp.

## What was built

- **`ui/map/NudgeElastic.kt`** (new):
  - **`NudgeGive`** (pure): the give is half of one finger's travel from where it came down, capped at half the threshold along the finger's direction.
    - A second finger, or not being able to take padding, gives nothing.
    - On release it says what to spring back from, or nothing: nothing was given, or following has ended. A give that ended with a leave keeps its padding until "Return to Route" or Stop sets the view's padding again.
    - A spring cut short by a new touch is continued from where the map is.
  - **`nudgePadding`** (pure): the padding that shows a give.
  - **`nudgeSpringOffset`** (pure): the spring's curve. It ends at exactly zero.
  - **`NudgeCamera`**: the map's side, behind an interface so the driver can be tested without a MapView.
  - **`NudgeElasticDriver`**: takes each MotionEvent.
    - Down starts a give and stops a running spring.
    - Move shows the give.
    - Lift, cancel or a second finger releases it.
    - The spring stops if following ends mid-spring.
    - It logs each spring under `ForagerNavView`, with its size in pixels. No position.
- **`ui/map/NavigationView.kt`:**
  - The three constants.
  - `StartZoomGate.transitioning` and `NavigationModeChange.transitioning`: whether a mode transition of ours is running.
- **`ui/map/SightingsMap.kt`:**
  - Builds the driver once, with a `NudgeCamera` that gives only when all of these hold:
    - the location component is active;
    - the navigation view is following and has not been left;
    - no transition is running;
    - the mode is not NONE.
  - Shows padding with `paddingWhileTracking(padding, 0)`.
  - Feeds the existing touch listener's events to it, and cancels any spring when the map leaves composition.

**The give's moves are classed as the location follower's** by the camera-move classifier: they are not a gesture reason and not marked as the app's. So an open fan stays open through a nudge, as it does while the map follows.

## Tests

**Tests first, pushed failing** (`2aa0f30b`, against a stub that gave nothing; run `t1-tests-first`): 14 tests, 9 failing.
- The 5 that passed: the transition flag, which was real code in that commit; and four tests that expect nothing to be given or sent, which the stub also did. Each is bitten by a revert below (n04, n05, n09, d02b), except the driver's "a drag that left following gets no spring", discussed under the revert table.

New, in two new classes; no existing test file changed:
- **`ui/map/NudgeElasticTest`** (10, pure):
  - half the finger's travel, on both axes;
  - the cap at half the threshold, along the finger's direction;
  - lifted while following springs back from the give;
  - a drag that left following does not spring;
  - a second finger gives nothing and springs back what one gave;
  - nothing with no finger down, or while padding cannot be taken;
  - a spring cut short continues from where the map is;
  - the padding for a give in each direction, and none for no give;
  - the spring's curve: starts at the give, passes rest by 13%, ends at exactly rest;
  - the transition flag.
- **`ui/map/NudgeElasticDriverTest`** (4, Robolectric, through real MotionEvents into the driver):
  - a nudge gives, springs past rest, and settles on the view's padding;
  - a drag that left following gets no spring;
  - a second finger springs back;
  - leaving following mid-spring stops the spring.

**A test of mine that could not fail, found by its revert and fixed.**
- "leaving following mid-spring stops the spring" first set its leave 50 ms into the spring. Its revert (d02, the mid-spring stop removed) did not bite.
- A scratch test (kept in `s1-scratch-frames/`, not committed) showed why: under Robolectric all 18 of the spring's frames run within the first 50 ms idle, so the leave came after the spring had ended.
- The test now ends following after the spring's third frame, however the frames are timed (`4f4ff93a`). Its revert d02b fails "expected:<4> but was:<20>".
- One consequence: the 250 ms itself cannot be checked off the phone. Only the curve and the frame-by-frame decisions are.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD after each check.

All 11 counted below compiled with 0 errors and failed for their own edit; d02, the first run of the mid-spring stop, did not bite and is not counted (see above).

| Check | One edit | Fails |
|---|---|---|
| n01 | the give ratio dropped (full travel) | 6 tests, each with the doubled give (e.g. 20 for 10) |
| n02 | no cap | the cap test (100 for 48) |
| n03 | two fingers give | the second-finger test ("a pinch starting expected null") |
| n04 | springs after a leave | the pure leave test |
| n05 | gives when padding cannot be taken | the leave test; the "nothing given" test ("a mode transition running") |
| n06 | a new give starts from zero | the cut-short test |
| n07 | padding widened once, not twice | the padding test; the driver's give test |
| n08 | no overshoot (a plain ease) | the curve test; the driver's "passes rest" test |
| n09 | the transition flag always false | the transition test |
| d01 | a second finger not released | the driver's second-finger test |
| d02b | the mid-spring stop removed | the mid-spring test (4 expected, 20 sent) |

**The driver's "a drag that left following gets no spring"** is not bitten by n04 alone. With n04 the spring starts, but its first frame finds following gone and sends nothing, so what reaches the map is the same. The decision itself is held by the pure test (n04), and the frame check by d02b.

## Suite

| Run | Tree | Result |
|---|---|---|
| Tests first (`t1-tests-first`) | `2aa0f30b` | 2 classes, 14 tests, 9 failures, as expected |
| Built (`t2-built`) | before `9e5cbfad` | 2 classes, 14 tests, 0 failures |
| Test fixed (`t3-test-fixed`) | before `4f4ff93a` | 2 classes, 14 tests, 0 failures |
| Full suite (`t4-full-suite`) | `4f4ff93a` | 429 classes, 3536 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled:** -457's full suite on its merged tree was 3,522 tests in 427 classes. This branch adds 10 + 4 = 14 in two new classes, which gives 3,536 in 429. `git diff --name-status origin/main..HEAD -- app/src/test` lists only those two files, both added.

## The desk step on the S22

The build was 1.0.2590+g4f4ff93a, installed by adb over 1.0.2578. The owner did the gestures; I read the `ForagerNavView` lines (`device/nav-log.txt`, phone time UTC−7). The S22 runs at 600 dpi (3.75), so the threshold is 180 px, and the largest give is 90 px.

1. **Nudges gave and sprang back, and following held.** Six springs are logged, from gives of 33 to 86 px (about 9 to 23 dp, within the 24 dp cap), with no "tracking changed" between them.
2. **Drags left.** Twice, "tracking changed to 8 … byTheApp=false", MapLibre's own leave, and "Return to Route" brought the view back ("tracking changed to 32 … byTheApp=true").
3. **The pinch:** not shown in the log either way, as a pinch that keeps following writes no line. The owner did not flag it.
4. **What the owner flagged:** "Map stayed tilted after Stop".

### The map stayed tilted after Stop: seen, not caused by this dispatch, not fixed here

- Navigation was stopped three times (18:01:43, :47.97 and 18:02:00.17). Each time the leave's ease to flat and north-up was cancelled within 7 ms ("navigation left, the ease cancelled", tilt 43.6° to 43.9°).
- In all three, the Stop came within 750 ms of a view being applied. That is while the tilt and padding animations that `applyView` starts (`NAVIGATION_VIEW_TRANSITION_MILLIS`) were still running.
- Each was followed by that padding animation's own "view applied" line, after the leave (18:01:43.257, :48.188, 18:02:00.426).
- So, inferred from the timing, not from MapLibre's code: the location camera's still-running animators move the camera after the leave has set mode NONE, which cancels the leave's ease.
- **A nudge took part in only the first of the three.** That one also kept the earlier drag's give in its padding (right 35, bottom 49), which is the give's padding staying after a leave, as designed. The second and third had no nudge.
- This is Stop's ease back, which this dispatch was told not to touch, so it is reported, not changed. Whether a build without -463 does the same was not run: no earlier device record has a Stop in it. It needs its own dispatch: see the fault first with a build from `main`, then fix it, for instance by cancelling the tracking animators before the leave's ease.

## Disclosure

**Confirmed vs inferred.**
- Confirmed by tests: the give, the cap, the release decisions, the padding, the curve, and the driver's handling of real MotionEvents.
- Confirmed on the S22 by the log: gives under the cap, springs, no leave on a nudge, and a leave on a drag.
- Inferred from the bytecode: that the padding path does not end following. The phone agrees, since no "tracking changed" follows a give.
- Inferred from the timing: the cause of the cancelled leave.

**Could not determine.**
- How the spring looks: its 250 ms and overshoot cannot be checked off the phone (Robolectric runs all its frames at once). The owner did not flag it.
- Whether the pinch was tried: it writes no line.
- Whether the tilted Stop happens without -463: not run.

**Premises that were wrong.** None found in the dispatch. My own first mid-spring test was wrong (it could not fail), and is fixed and recorded above.

**Decided beyond scope.**
1. **A second finger springs back what one gave.** The step path's pinch "still zooms and keeps following", and a give left behind under a pinch would have stayed until the next Return.
2. **The give's padding stays after a leave,** until "Return to Route" or Stop sets the view's padding again. Resetting it mid-drag would jump the map 24 dp. The planner accepted this.
3. **A spring cut short by a new touch is continued from where the map is,** not snapped to rest.
4. **The give's moves count as the follower's for the open fan,** so a nudge does not fold it.
5. **The overshoot is the app's own animator,** since MapLibre animates padding linearly only.
