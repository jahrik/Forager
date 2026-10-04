# A quick flick ended following: MapLibre's fling, then the drag line (dispatch 2026-09-28-477, fault, with Amendment 1)

**Status: fixed and pushed on `nudge-fling`, not merged. Seen on the S22 before and after,** with the owner flicking.

**Date:** 2026-10-04 (UTC).
**Dispatch:** `prompts/preserved/2026-10-04-03.md` (from `origin/records-after-163`), with Amendment 1 (the line rises to 72 dp).
**Base:** `origin/main` at `6c85e436` (PR #163), checked against the remote; unchanged at the full-suite run.
**The owner's words,** in this window:
- "Yes, start -477", then "Yes, use the phone" for each phone step.
- On how a fast flick a little past a finger's width should behave: first "Keep as is (Recommended)"; then, asking to see the options again, "Raise the line for all drags".
- On how far: "1.5 finger widths, 72 dp".
- In the planner's window, the owner confirmed the step path ("Confirmed").

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Phone logs are in `~/Zynergy/device-evidence/2026-10-04-nudge-fling/`, phone time UTC−7. APKs are on the owner's USB drive under `apks/`.

## In plain terms

1. **Two causes, one after the other.**
   - First, MapLibre treats a quick flick as a "fling" when the finger lets go, and a fling always ended following, however short the flick. Fixed: a flick while following now springs back like a nudge.
   - Then the owner's quick flicks still sometimes let go. They had travelled past a finger's width (the old line, 48 dp) before the finger lifted.
2. **The owner moved that line to 1.5 finger widths (72 dp).** A nudge or flick under it springs back, and the map can give up to 36 dp. A drag past it lets go and shows "Return to Route". A pinch zooms and keeps following.

## Part A: seen first

**The library, read from the pinned 13.5.0 bytecode:**
- `MapGestureDetector$StandardGestureListener.onFling` runs when the finger lifts. It needs scroll gestures on, `UiSettings.isFlingVelocityAnimationEnabled`, and a speed of at least `UiSettings.getFlingThreshold()` (1,000 by default, in pixels per second divided by the pixel ratio, so 1,000 dp/s). It never asks whether the move passed the tracking threshold.
- It then notifies the fling listeners. The location camera's (`LocationCameraController$8.onFling`) calls `setCameraMode(8)`, NONE, unconditionally.
- **MapLibre has no option for this on the location component.** Its only fling controls are the map-wide `UiSettings` ones.

**On the S22**, with log lines added (they stay: each fling reported while navigating with the camera mode, and each release with the mode and the finger's net travel):
- Build 1.0.2620, `device-before/`.
- A first round of ordinary flicks: no fling. They were not fast enough.
- A second round of sharp flicks:
  - **45 releases while following; 10 were reported as a fling, and every one ended following 1 to 2 ms later.** The other 35 sprang back.
  - Flung releases travelled 24 to 61 dp; releases that sprang back, 0 to 31 dp.
  - 12 releases came after a drag had already left following; 9 of them carried on as fling momentum, as they should.

Quoted (phone time):

```
21:43:46.124 touch released: mode=32, travel=39 dp
21:43:46.124 fling reported: mode=32
21:43:46.125 tracking changed to 8: expected=32, byTheApp=false, navigating=true
21:43:46.147 nudge: following ended mid-spring; the spring stops
```

## Part B: the fix

**`ui/map/NudgeFlingGuard.kt`** (new), fed by the map view's existing touch listener (`ui/map/SightingsMap.kt`):
- That listener runs before MapLibre's own handling and still returns `false`.
- At a release while navigating and still following (the location camera not NONE), MapLibre's fling is switched off for that one release, then switched back on at the next touch.
- A drag that has already left following releases with the fling on, so its momentum is as before.
- Outside navigation, the fling setting is never touched.

**After, build 1.0.2621** (`device-after/`):
- **0 flings reported while following, out of 136 releases while following.**
- **The owner still saw a flick let go.** All 12 leaves were MapLibre's drag threshold ending following during the move, 1 to 20 ms before the finger lifted, with travel of 56 to 91 dp, past the 48 dp line. They were followed by a fling, now only momentum.

So the fling fault was fixed, and what remained was the line itself.

## Amendment 1: the line rises to 72 dp

- **The change:** `NAVIGATION_NUDGE_THRESHOLD_DP` 48 → 72 (`ui/map/NavigationView.kt`).
- **What follows from it:**
  - MapLibre's tracking move threshold, set from the constant in `liveLocationComponentOptions`;
  - the nudge's give cap, half the threshold, now 36 dp.
- The confirmed step path (planner, `RECORD.md` -482):
  1. A nudge or quick flick under 72 dp springs back and keeps following, with a give of up to 36 dp.
  2. A drag past 72 dp leaves and shows "Return to Route".
  3. A pinch still zooms and keeps following.

**On the S22, build 1.0.2622** (`device-72/`). The owner answered "As expected".
- **Releases:** 86 in all, 70 of them while following. **0 flings reported while following.**
- **17 releases between 48 and 72 dp sprang back and kept following.** Under the old line these would have let go.
- **Leaves:** all 12 came from drags whose release travel was 89 to 211 dp.
- **Seven flicks of 73 to 87 dp also kept following.** The logged travel is the finger's net movement from touch to release. MapLibre measures its threshold from where its own move detection begins, after a first small movement, so its line sits slightly past the nominal value. The 48 dp build showed the same: flicks up to 61 dp kept following. Inferred from the counts; not measured further.
- **Releases of 159 to 328 dp kept following.** All came after 22:55:22, the pinch step at the end, so they are very likely pinch releases, which have their own 400 dp line. The log has no pointer count, so this is inferred.

## Tests

New, in two new classes; no existing test file changed:
- **`ui/map/NudgeFlingGuardTest`** (4, Robolectric, through real MotionEvents into the guard):
  - a release while still following has no fling, and the next touch has it back;
  - a release after a drag left following keeps its fling;
  - outside navigation the fling is never touched;
  - a flick after a guarded release is guarded again, and a drag after it has its fling back.
- **`ui/map/NudgeThresholdTest`** (3, pure):
  - the line is 72 dp;
  - the give follows it, up to 36 dp;
  - a flick between the old and the new line gives half its travel, uncapped.

**Device-only:** MapLibre's fling and threshold themselves; a MapView cannot run under Robolectric. The desk steps above are the evidence for them.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD after each check.

All 5 compiled with 0 errors and failed for their own edit.

| Check | One edit | Fails |
|---|---|---|
| f01 | the release while following not guarded | the guarded-release test ("off for this release"); the repeat test |
| f02 | the fling not switched back on at the next touch | the guarded-release test ("on again before MapLibre sees the next touch"); the repeat test |
| f03 | the switch written even when it was never switched off | the drag test ("never touched"); the outside-navigation test; the repeat test |
| f04 | every release guarded, following or not | the drag test; the outside-navigation test; the repeat test |
| g01 | the line back to 48 dp | all three threshold tests (48 for 72; a 24 dp cap for 36) |

`NavigationGestureOptionsTest` passes under g01. It checks that MapLibre's option equals the constant, whatever its value, so it covers the wiring, not the value.

## Suite

| Run | Tree | Result |
|---|---|---|
| Guard and nudge classes (`t1-built`) | before `90f15944` | 3 classes, 18 tests, 0 failures |
| With the 72 dp tests (`t3-amendment`) | before `0891b7de` | 5 classes, 23 tests, 0 failures |
| Full suite (`t4-full-suite`) | `0891b7de` | 434 classes, 3558 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled:** `main` at `6c85e436` had 3,551 tests in 432 classes (-470's report). This branch adds 4 + 3 = 7 tests in two new classes, which gives 3,558 in 434. `git diff --name-status origin/main..HEAD -- app/src/test` lists only those two files, both added.

## Disclosure

**Confirmed vs inferred.**
- Confirmed:
  - the fling mechanism, from the bytecode and from the log (10 of 10 flung releases ended following);
  - the fix (0 flings while following, in 136 and then 70 releases);
  - the 72 dp line on the S22 (17 releases between 48 and 72 dp kept following; every leave was a drag past 89 dp);
  - the owner's "As expected".
- Inferred:
  - why releases slightly past the nominal line still follow;
  - that the 159 to 328 dp releases were pinches.

**Could not determine.** The exact distance at which MapLibre lets go. The log records the net travel at release, not the moment MapLibre's detector began measuring.

**Premises that were wrong.**
- The dispatch's description assumed one cause. There were two: MapLibre's fling (fixed) and the 48 dp line (raised by the owner's choice, Amendment 1).
- I first recorded the owner's answer as "Keep as is". The owner then asked to see the options again and chose "Raise the line for all drags". The later answer is the one built, through the planner's Amendment 1.

**Decided beyond scope.**
1. **The fling is switched off per release,** not for the whole navigation. A real drag past the line keeps its momentum.
2. **The Part A log lines stay.** They log only while navigating, on each release and fling.
