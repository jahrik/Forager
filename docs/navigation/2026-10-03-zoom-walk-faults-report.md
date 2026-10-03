# The start zoom never landed, and a pinch left the navigation view (dispatch 2026-09-28-457, with Amendment 1's nudge protection)

**Status: fixed and pushed on `zoom-walk-faults`, not merged. Each failure was seen on the S22 before it was fixed, and the fix was seen there after.**

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-09.md` on `origin/records-after-159`, with Amendment 1 (order, and Part C).
**Base:** `origin/main` at `cb035035` (PR #159, -440), checked against the remote; `main` had not moved by the full suite.
**The owner's words** in the coder's window:
- "Yes, start -457".
- "Yes, use the phone" for the first desk run, and "Yes, go ahead" for the second.
- For their own gestures on the phone, "Done" and then "As expected".
- Part C's step path was confirmed in the planner's window: "Confirmed 1 2 and 3".

Figures and log extracts are in `~/Zynergy/device-evidence/2026-10-03-zoom-walk-faults/`. The log lines quoted below carry no position.

## In plain terms

- **Tap Return:** the map now zooms in to street level, about two blocks ahead, as it tilts and turns.
- **A pinch zooms and keeps following.**
- **A small accidental nudge,** under about a fingertip's width, is ignored and the map keeps following.
- **Only a deliberate drag** leaves the view and shows "Return to Route".

## Part A: the start zoom

**Seen before fixing.** The owner's walk log (`2026-10-03-walk-zoom/logcat-all.txt`) logs this at every start:

> `22:03:46.107 E Mbgl-LocationComponent: LocationComponent#zoomWhileTracking method call is ignored because the camera mode is transitioning`

The same lines appear for tilt and padding, and the zoom then stays at the walker's own (17.62, 16.18, 16.85, 14.91).

Log lines were added (`aadef55b`: each view apply's mode and pending zoom, the zoom callback's outcome, and every tracking-mode change), and the S22 at a desk gave the sequence in full (build `1.0.2577+gaadef55b`):

```
apply view: FACING_UP, start zoom none, mode now 8, asking 32
tracking changed to 32: expected=32, byTheApp=true, navigating=true
apply view: FACING_UP, start zoom 18.0, mode now 32, asking 32
E LocationComponent#zoomWhileTracking method call is ignored because the camera mode is transitioning
start zoom cancelled: mode=32, zoom=12.00, …
apply view: FACING_UP, start zoom none, mode now 32, asking 32
view applied, FACING_UP: mode=32, zoom=12.00, bearing=294.8, tilt=45.0, padding=0, 747, 0, 0
```

**The cause.** Candidates 1 and 2 together:
- The map's navigation effect first ran with no start zoom. The pending flag is set by `AvailabilityScreen`'s `LaunchedEffect` after that composition, so this run started the transition with a listener that carried no zoom.
- The flag's change re-ran the effect within milliseconds and re-sent the same camera mode. For the same mode, MapLibre's `setCameraMode` calls the transition listener at once (13.5.0, offsets 0–21). So the zoom, tilt and padding were issued mid-transition and refused.
- The refusal's `onCancel` cleared the pending flag, so the zoom was never tried again, and the original transition finished without it.

**The fix:** `StartZoomGate` (`ui/map/NavigationView.kt`), used by `NavigationModeChange.applyView`.
- A request for the mode a transition of ours is already heading to waits for that transition instead of re-sending it.
- The wanted start zoom is held, and handed out only by the listener of the transition actually in flight, when it really finishes.
- A superseded listener hands out nothing; a cancelled transition keeps the zoom for the next.
- Stop drops it.

**Seen after fixing** (build `1.0.2578+gde369d9c`):

```
apply view: FACING_UP, start zoom none, mode now 8, asking 32
apply view: FACING_UP, start zoom 18.0, mode now 32, asking 32
apply view: a transition to 32 is in flight; waiting for it
start zoom requested: 18.0
start zoom finished: mode=32, zoom=18.00, bearing=339.1, tilt=45.0, padding=0, 739, 0, 0
```

No MapLibre refusal follows.

## Parts B and C: a pinch, and a nudge

**Seen before fixing:**
- On the first desk run the owner pinched, nudged and dragged while the log ran (`device/owner-gestures-log.txt`); the computer cannot inject a two-finger pinch without root (`sendevent`: "Permission denied").
- Every gesture ended following by the library, not by the app. For example, a second finger down (Android's action 261) at 15:32:47.632 was followed by "tracking changed to 8: expected=32, byTheApp=false" at 15:32:47.747.
- One-finger moves (action 2) did the same within about 40 ms. So the pinch's drift and the smallest nudge both tripped MapLibre's move gesture.
- -440's report had inferred from the bytecode (no scale listener) that a pinch would keep tracking. **That inference was wrong:** the move detector fires on the pinch's two fingers.

**The fix, one change for both parts:**
- MapLibre's `LocationComponentOptions.trackingGesturesManagement` is off by default in 13.5.0. With it on, while tracking, `LocationCameraController.adjustGesturesThresholds` sets the move detector's threshold to `trackingInitialMoveThreshold`, and a multi-finger threshold `trackingMultiFingerMoveThreshold` (defaults 25 dp and 400 dp).
- `liveLocationComponentOptions(context, navigating = true)` (`ui/map/SightingsMap.kt`) turns it on, with `NAVIGATION_NUDGE_THRESHOLD_DP = 48` (a fingertip; provisional) and `NAVIGATION_MULTI_FINGER_MOVE_THRESHOLD_DP = 400` (MapLibre's own, stated).
- The navigation effect applies those options at the start of navigation, and the ordinary ones when navigation stops, through `LocationComponent.applyStyle`, which hands them to the camera controller (`initializeOptions`).
- Every activation (a style load, locate) applies the ordinary options and resets the flag, so the effect applies the navigating ones again.
- **Outside navigation nothing changes.**

**Seen after fixing** (the owner's gestures on `1.0.2578+gde369d9c`, `device2/gestures-log.txt`; the owner: "As expected"):
- Second fingers down at 15:48:21.887 and 15:48:22.903 (two pinches): no tracking change.
- One-finger moves from 15:48:24.4 to 15:48:25.9 (the nudge): no tracking change.
- From 15:48:26.365, the drags: "tracking changed to 8 … byTheApp=false", then "Return to Route" taps bring it back to 32.
- Stop at 15:49:00: flat.

## Tests

Reachable off the phone:
- **`NavigationViewTest`** (3 new):
  - The start zoom waits for the transition in flight and lands when it finishes (the exact sequence the phone logged).
  - A superseded transition hands out nothing and the newer one gets the zoom.
  - A cancelled transition keeps the zoom, and Stop drops it.
- **`NavigationGestureOptionsTest`** (new, 2): while navigating, the options turn management on with both thresholds in pixels; outside navigation, management is off as before.

**Not reachable off the phone,** so the desk steps above are the evidence:
- MapLibre's transition timing and its refusals.
- What a finger does under the thresholds.
- `applyStyle` switching the options at runtime.

No existing test changed, skipped or silenced.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD (`de369d9c`) after each check.

All 5 compiled with 0 errors and failed for their own edit.

| Check | One edit | Fails with |
|---|---|---|
| y01 | the same mode re-sent mid-transition | "the second request waits expected:<null> but was:<2>" |
| y02 | a superseded listener hands out the zoom | expected null, was 18.0 |
| y03 | a cancelled transition drops the zoom | expected 18.0, was null |
| y04 | gesture management off while navigating | the options test |
| y05 | MapLibre's 25 dp nudge, not 48 | "expected:<144.0> but was:<75.0>" (px at xxhdpi) |

## Suite

| Run | Tree | Result |
|---|---|---|
| Touched classes (`t1-built`) | before the commit | 5 classes, 35 tests, 0 failures |
| Full suite (`t2-full-suite`) | `de369d9c` | 427 classes, 3522 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled, counted per file against `cb035035`.** The base's 3,517 tests in 426 classes, plus `NavigationViewTest` 6 → 9 and the new `NavigationGestureOptionsTest` (2): 3,522 tests, 427 classes. The 24 skipped are as before.

## Disclosure

**Confirmed (on the phone, from log lines):**
- The cause of the missing zoom (the sequence above), and that it now lands at 18.00.
- That a pinch and the smallest nudge ended tracking by the library before the fix.
- That after the fix pinches and a small nudge keep following and drags still leave.

**Inferred:**
- That the dismissal before the fix came from MapLibre's move detector. That is read from its source and the timing; the log shows the mode change, not the detector.
- That 48 dp suits "a fingertip". The owner's one test said "As expected"; a walk is the real test.

**Could not determine:** how a nudge between 25 and 48 dp felt to the owner against one over 48 dp. The owner made one nudge; its distance on screen is not in the log.

**Premises that were wrong:** -440's report inferred that a pinch keeps tracking. It does not without the gesture management.

**Decided beyond scope:**
- The diagnostic log lines stay (the dispatch allowed it): every tracking change while navigating, and the start zoom's outcome.
- The drawn tiles' look at 18 on Topo was not re-examined.
