# Stop sometimes left the map tilted (dispatch 2026-09-28-470, fault)

**Status: fixed and pushed on `stop-tilt`, not merged. Seen on the S22 before and after,** with the owner tapping.

**Date:** 2026-10-04 (UTC).
**Dispatch:** `prompts/preserved/2026-10-04-02.md` (from `origin/records-after-161`).
**Base:** `origin/main` at `7c072bfc` (PR #162, -451 and -463), checked against the remote; unchanged at the full-suite run.
**The owner's words,** to this session directly:
- "-470 first (Recommended)" when asked to order it against -446, for which they had just said "Yes, start -446";
- "Yes, use the phone".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Phone logs are in `~/Zynergy/device-evidence/2026-10-04-stop-tilt/`. Their times are the phone's own (UTC−7).

## In plain terms

1. **What happened.** Stop, tapped within a second or two of Return, sometimes left the map tilted. Return starts a short animation that moves the walker below the centre. MapLibre does not stop that animation when following ends, and it kept moving the map, which interrupted Stop's ease back to flat.
2. **The fix.** Stop now ends that animation first. Every Stop on the fixed build ended flat and north-up, including Stops in the middle of a nudge's bounce.
3. **The nudge (-463) did not cause it.** It happens on a build from before the nudge.

## Part A: seen first

### 1. On a build without -463

`main` at `bdb27f03` (1.0.2587), with no new log lines, so only the existing "navigation left" lines (`device-pre463/`).
- **First round:** the owner tapped Stop once the map had settled. Every Stop eased flat. Each came after the view's padding animation had finished.
- **Second round:** Stop about a second after Return.
- **Altogether:** 17 leaves; 11 logged "navigation left, the ease cancelled", leaving tilts of 9.6° to 41.5°.

**-463 does not cause it; it was already there.** Whether -463 adds to it was not separately measured: the nudge's give and spring set padding through the same location-camera path, and once tracking has ended they are refused.

### 2. On today's main, with log lines added

The lines stay in the code, logging only:
- At each leave: what of ours is in flight (the view's padding animation, the start zoom, a mode transition, the nudge spring).
- Every camera move that starts within 1.5 s of the leave, with its reason.
- From the second round on: the camera's tilt, zoom, padding and mode on every frame within 1 s of the leave.

**The first round (`device-instrumented/`, 1.0.2609) could not name the mover. Two things in it were not usable:**
1. The move that starts 11 to 15 ms after the leave reports reason 3. That is MapLibre's reason for every programmatic move, the leave's own ease included.
2. My count of padding animations in flight went to −1, −2, −3 and −4. MapLibre can call both a callback's `onCancel` and its `onFinish` for one animation: the pre-463 log already shows "start zoom cancelled" and "start zoom finished" in the same millisecond. So the count was wrong, and none of its readings are cited here. It was replaced by a token per animation, cleared only by the latest animation's first callback (`a10c6046`).

**The second round (`device-instrumented2/`, 1.0.2611) named it.**
- Seven leaves. All five Stops made with the view's padding animation in flight were cancelled (tilts 12.7° to 35.5°). Both made without one eased flat.
- The start zoom, a mode transition and the nudge spring were in flight at none of the seven.
- The frames of the first cancelled leave, quoted (phone time):

```
13.049 leaving navigation: was following=true, padding animation in flight=true, start zoom in flight=false, transition in flight=false
13.054 camera during the leave's ease, 6 ms in: mode=8, zoom=17.00, bearing=-0.0, tilt=15.5, padding=0, 0, 0, 0
13.056 camera during the leave's ease, 8 ms in: mode=8, zoom=17.00, bearing=-0.0, tilt=15.5, padding=0, 282, 0, 0
13.057 navigation left, the ease cancelled: mode=24, zoom=17.00, bearing=-0.0, tilt=15.5, padding=0, 282, 0, 0
13.524 view applied, FACING_UP: mode=24, zoom=17.00, bearing=-0.0, tilt=15.5, padding=0, 282, 0, 0
```

What the frames show:
- The ease's first frame set the padding to 0, as the leave asks.
- Two milliseconds later the padding was back at 282, the view's padding animation part-way, with the camera already not following (mode 8).
- The ease was cancelled within the next millisecond.
- The padding animation then reported its own finish at 13.524, after the leave.

**The mover is the view's padding animation**, started by `applyView` (`ui/map/NavigationView.kt`). This agrees with the 13.5.0 bytecode, read before the phone step:
- `LocationComponent$8.onCameraTrackingChanged` cancels the zoom and tilt animations only, not the padding.
- `LocationCameraController.getAnimationListeners` attaches the padding listener in every mode.
- `LocationCameraController.setPadding` skips only during a transition. Otherwise it calls `Transform.moveCamera`, which cancels the camera's running ease.

**The candidates the dispatch named:**
- **The tracking tilt:** cancelled by MapLibre when tracking ends (bytecode). It was not the mover: the tilt did not change between the frames above.
- **The start zoom:** not in flight at any of the seven leaves. The zoom did not change between the frames.
- **The nudge spring:** not running at any of them.
- **MapLibre's own transition:** not in flight at any of them.

## Part B: the fix

`NavigationModeChange.leaveNavigation` (`ui/map/NavigationView.kt`) calls `LocationComponent.cancelPaddingWhileTrackingAnimation()` before it ends tracking and eases flat (`c9641436`).
- **How it stops the competing animation:** the public method cancels the location animator coordinator's padding animator (`LocationAnimatorCoordinator.cancelPaddingAnimation`, bytecode), so nothing sets the padding during the ease.
- **Nothing else changes.** The zoom and tilt animations are left to MapLibre's own cancel on the tracking change, as before. The leave's log line now reads what was in flight before this cancel clears it.

### After, on the S22

`device-fix/`, 1.0.2612.

**30 leaves:**
- 18 ended "navigation left" at tilt 0.0 and bearing 0.0. Nine of them had the padding animation in flight at the leave, five of those with the start zoom still running too.
- The other 12 logged "the ease cancelled" at mode 32. Each was a new Return tapped before the ease ended: each comes 0 to 12 ms after an "apply view … asking 32" line, so the ease gave way to navigation starting again. The last leave of that burst of quick Return/Stop taps ended flat.

**Stop mid-nudge:** six Stops were tapped during a nudge's spring. All six ended flat.
- "following ended mid-spring" is logged a few milliseconds before each leave, so the spring stopped itself as -463 built it.

**The owner:** "Flat every time", for both rounds.

**The facing change** the dispatch named was not reached: no calibrating or north-up switch happened at the desk. Turning on the spot turns the compass bearing, which does not re-apply the view. That case runs the same padding animation from `applyView`, which this fix cancels, but it was not seen on the phone.

## Tests

- **No new test.** The leave's ordering needs a real `MapLibreMap` and `LocationComponent`, which cannot run under Robolectric. As the dispatch allows, the desk step is the evidence.
- **No existing test changed.**

**The revert check is the device pair, not a suite run.** The builds before and after differ by the one cancel call and the log line's reading of what was in flight (`git diff a10c6046 c9641436`).
- Before: every leave with the padding animation in flight was cancelled (5 of 5).
- After: none of the nine leaves with it in flight was cancelled.
- A suite revert would pass identically either way, since nothing off the phone reaches it, so none was run.

## Suite

| Run | Tree | Result |
|---|---|---|
| Full suite (`t1-full-suite`) | `c9641436` | 432 classes, 3551 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled:** -457's 3,522, plus -451's 15 and -463's 14 on `main`, is 3,551. This branch adds none.

## Seen on the way, not changed

**A quick nudge can end following.** Twice in the last round (19:35:52.727 and :57.077), MapLibre ended tracking 1 ms after a nudge's release ("tracking changed to 8 … byTheApp=false"), with no drag past the threshold.
- Inferred, not confirmed: the location camera's fling listener ends tracking, so a nudge let go with speed reads as a fling.
- This is -457 and -463 ground, not -470's. Reported for the planner.
- The owner did not flag it.

## Disclosure

**Confirmed vs inferred.**
- Confirmed on the S22:
  - the fault on a build without -463;
  - which animation cancels the ease (the frames above, and five of five against two of two);
  - that the fix ends every leave flat (18 of 18 that were not superseded by a new Return; 0 of 9 with the animation in flight cancelled).
- Confirmed from the bytecode: why MapLibre leaves the padding animation running.
- Inferred: that a facing change's re-apply is covered (not seen), and the fling explanation above.

**Could not determine.**
- Whether -463 adds to the fault, beyond not causing it: it was not separated.
- The facing-change case on the phone.

**Premises that were wrong.**
- The dispatch's candidates included "the tracking tilt and padding animators still running". Only the padding one is: MapLibre cancels the tilt animation itself when tracking ends.
- My own first padding count was wrong and is not cited.

**Decided beyond scope.**
1. **The log lines stay,** as the dispatch allowed. They write only on a leave and in the second after it.
2. **Only the padding animation is cancelled,** not the zoom and tilt ones as well. Those are already cancelled by MapLibre, and the evidence named only the padding.
