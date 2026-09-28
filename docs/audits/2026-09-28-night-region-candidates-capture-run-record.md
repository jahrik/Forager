# Night offline-region candidates: capture run record (2026-09-28), STOPPED PARTWAY

Dispatch `prompts/preserved/2026-09-28-26.md` (continuation `2026-09-28-26` of intent `2026-09-28-19`).
Phone SM-S908U, serial `R5CT321008R`. The raw evidence and every screenshot are outside the repository, in
`/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-night-region-candidates/` (`shots/` holds
deliverables, `logs/` holds working captures and scripts). This record names files and hashes only.
Nothing here judges which candidate looks best.

**Status: stopped after 202020's Views A (day), A (night) and B.** While View C was being captured, a
Google Play services account-security prompt (`com.google.android.gms/.octarine.ui.OctarineActivity`)
came up over the app, asking the phone's owner to confirm an account recovery. It was left unanswered and
undismissed: answering it is the owner's alone, and every further step needs it gone. Its content is
not recorded here because it is personal to the owner. See "Stop" below.

## Restart

This is the second coder session on this continuation. The first stopped early because the permission
checker returned no verdicts, and no abort condition was met. The planner re-verified the phone
read-only before this session began: `versionName=1.0.1259+g75c050f7.dirty`, `firstInstallTime`
unchanged at 2026-09-22 11:15:05, `accelerometer_rotation 0`, `user_rotation 0`, an empty crash buffer
and an empty `shots/`. This session re-read the same values at its start and they matched.

**The monkey rotation incident (first session).** Launching the app with `monkey` toggled system
auto-rotate on this phone. The first session restored it. This session read
`accelerometer_rotation 0` and `user_rotation 0` at its start, at 11:03Z, and again at the stop. It
launched nothing with `monkey`.

## Install facts (from the first session, reused)

- The sha256 of all three APKs matched `FACTS.txt`.
- `ForagerDatabase.version` is 15 at both `2458934` and `75c050f`, and
  `git diff --stat 2458934 75c050f -- app/src/main/java/com/zynergylabs/forager/app/data/local/` is empty.
- 202020 was installed with `adb install -r` at 10:46:48Z (phone time 03:46:50 PDT). `dumpsys package`
  afterwards: versionCode 1259, versionName `1.0.1259+g75c050f7.dirty`, `lastUpdateTime` 2026-09-28
  03:46:50, `firstInstallTime` 2026-09-22 11:15:05 (unchanged). The crash read was empty
  (`logs/12-crash-202020.txt`, 0 bytes).
- 404040 and 606060: **not installed** (stopped first).

## Settings

| Setting | Start | Now | Restored? |
|---|---|---|---|
| Night Maps (`night_mode.maps`, DataStore) | false | **true** | **No.** Restoring it means dismissing the account prompt |
| Basemap (session-only, per `MapLayerPreferencesRepository.kt:27`) | Topographical | Topographical | Yes (read back from the Layers description) |
| `map.fullscreen` | false | false | Unchanged |
| System `accelerometer_rotation` / `user_rotation` | 0 / 0 | 0 / 0 | Unchanged |

All Layers overlays were on, Offline maps included, and were not touched.

## Framing recipe (scripts in `logs/`)

`frame.sh` (sha256 `d597b543…4854`) runs these steps from the Maps tab in portrait:
1. Ten quick-zoom-outs, each a double-tap-and-drag 1500 px up, which reach MapLibre's minimum-zoom clamp.
2. Tap "Center on my location" (970,1033).
3. Ten double taps at (540,1195).
4. "Center on my location" again.
5. Added this session: a slow one-finger pan 180 px left. DOWN at (720,1500), 18 MOVEs of 10 px,
   a 0.8 s hold, then UP. This brings the circle's right edge out from under the icon bar.

At the result, the region's circle (outer dashed edge measured at x≈32 to 846, top y≈847 on the
1080×2316 screen) is wholly on screen, with ground visible outside it. From its 5 km radius, density
2.8125 and MapLibre's 512-px world, the zoom works out to about 10.6. This is derived, not read from
the app.

`frame-instr.sh` (`55519365…f440`) makes the same touches and adds a capture after each step.

**View C** (`viewc.sh`, `a6fb38fc…2c8f`): from the View A frame, four double taps at the circle's top
edge point (439,847), each +1 zoom, for about z14.6. Tracking is off after the pan, so each zoom
anchors at the tap point and the edge stays near y≈850. View C is on Topographical.

**Basemap switch** (`basemap.sh`, `37392dc5…8cf8`): open Layers, tap the map type, close the sheet with
Back, then read back the Layers description.

**Reproducibility checks.** `cmp.py` compares four regions outside the circle: (0,300)-(900,760),
(0,1700)-(900,1880), (0,760)-(40,1700) and (860,300)-(915,1880). A pixel counts as differing when any
channel differs by more than 24.
- Day, run 1 against run 2: 0 differing pixels in every region. Run 2's own clamp capture differs from
  its final frame over 97-99% of each region, so run 2 did move the map.
- Night, runs c and d: 0 differing pixels, at both the clamp and the final frame.
- Street, runs a and b: 0 differing pixels.
- Day against night: the colours differ, so this used edge-map cross-correlation (`edgeshift.py`). The
  night frame peaks at 0.51, about 2 px from (0,0). An identical image scores 0.514 at one step off,
  so the two align to within this test's resolution. A mis-framed night run scores a flat 0.21.
- View C: the first two captures that ran cleanly (`46-viewc-202020-a.png`, and a later rerun from
  the View C state) are pixel-identical.

**Failures seen, and what was learned.**
1. The first night run after turning Night Maps on ended at maximum zoom. The two runs that followed
   were correct. The locate handler (`SightingsMap.kt:683-686` at `75c050f`) activates the location
   component on first use and eases to `FIRST_ACTIVATION_ZOOM = 16.0` (`:1094-1096,1106`), which
   matches that result. Style swaps are meant to restore the previous camera mode without zooming
   (`:532-568`), so why the component was not activated is **unconfirmed**.
2. Six zoom-outs fell short of the clamp from maximum zoom: that run's clamp capture differed from
   the others. The recipe now uses ten. At the clamp, extra zoom-outs change nothing (the clamp
   captures are identical).
3. One run (c2) also ended at maximum zoom, with no style change before it. Two instrumented reruns,
   one of them from the same starting state, did not reproduce this, and logcat showed nothing that
   would tell a good run from a bad one. **The cause is undetermined.** Mitigation: a frame is
   accepted only when it is pixel-identical, outside the circle, to the 202020 reference for its view.
   A failed run is redone.
4. Picking Street moved the camera to a closer zoom, so View B re-runs the recipe after the switch.

## Shots taken (all 202020; `shots/SHA256SUMS`)

| File | sha256 | Framing |
|---|---|---|
| `202020-A-day.png` | `8c13cc78bc61294d0f266368c076b3789d6573c43252bad8ecb79afc07c389d7` | frame.sh; identical to run 1 |
| `202020-A-night.png` | `7cf318d95464ed327ce24f98d77ec30990fe9478d6500993c8f36784c29b4026` | frame.sh (night run d); identical to run c |
| `202020-B.png` | `746bf66462ba2cacab617f12f4c882e15dad80ac26d2129a42e5a45014cad80c` | Street, sheet closed; frame.sh twice, identical |

Not taken: 202020-C, and every 404040 and 606060 view. One capture made as `202020-C.png` turned
out to be the account prompt, not the map. It was moved to
`logs/INVALID-202020-C-attempt-prompt-overlay.png` and its line removed from `SHA256SUMS`, so it
cannot be shown as a candidate. That file shows the owner's account details and must stay out of any
repository.

## Crash reads

The crash buffer read 0 lines at the start, after each shot and at the stop. At 11:21Z,
`logcat -c` (default buffers) was run to scope an instrumented run's log, and it also clears the crash
buffer. That buffer had read 0 lines just before, after the 202020-A-night and 202020-B shots (exact times not logged). Any crash after 11:21Z would
still show in later reads, and there was none.

## Stop

At 11:25Z the foreground became `com.google.android.gms/.octarine.ui.OctarineActivity`, an
account-recovery confirmation prompt with "No, don't allow" [64,1230][531,1335] and "Yes, it's me"
[548,1230][1015,1335]. It appeared unprompted, during View C's scripted double taps at (439,847). Those
eight taps landed on the prompt's text, above its buttons. A read-only check afterwards found the
prompt still showing with both buttons present, so no choice was made. Nothing further was done on the
phone. Continuing needs the owner to deal with the prompt themselves, and a fresh dispatch or go-ahead.

## Decisions I made

- Added the planned 180 px pan as the slow-motionevent form described above (the start point, step
  size and hold were mine).
- Changed step 1 from six to ten zoom-outs, after seeing six fall short from maximum zoom.
- Used "accept only a frame pixel-identical outside the circle to the reference, else redo" in place
  of a diagnosed fix for intermittent failure 3. Whether that counts as "reproducible framing" under
  the abort condition is the planner's call.
- View C on Topographical. The dispatch names no basemap for C.
- View C's edge point: the top of the circle, at four double taps (about z14.6; five would be about
  z15.6). No finds or waypoints were visible near the edge. I read that from the image; the only
  markers sit near the centre, about 5 km away, out of any z15 edge view.
- Shot names `202020-A-day`, `202020-A-night`, `202020-B` and `202020-C`.
- Moved the invalid capture out of `shots/`.
- Stopped on the account prompt, which is not a listed abort condition.
- Ran `logcat -c` for instrumentation, which cleared the crash buffer (see Crash reads).

## Flags outside scope

- The account-recovery prompt itself is for the owner, urgently. Nothing about it was investigated.
- Picking a basemap in the Layers sheet moved the camera to a closer zoom (observed once, cause not
  investigated). This may be the recenter the `:522-531` comment describes as fixed.
- `input motionevent` quick-zoom drags logged `GestureDetector: handleMessage LONG_PRESS` (20 in one
  run). No visible effect; not investigated.
