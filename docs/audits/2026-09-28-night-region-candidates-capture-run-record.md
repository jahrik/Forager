# Night offline-region candidates: capture run record (2026-09-28), STOPPED PARTWAY (twice)

**Current status (after the fourth session, at the end): stopped at 11:56Z with a question for the planner. Under
the planner's relaxed View C rule, it is unclear whether 404040's View C "matches" at its shift. No phone
action was taken in that session. 404040 is installed; Night Maps is still on.**

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

## Resumed session (third coder session, 11:33Z to 11:52Z)

**The prompt pause.** The session above stopped at 11:25Z on the Google account-recovery prompt. The
owner dealt with it on the phone. The planner's message, part of this dispatch, quoted the owner verbatim:
"The prompt is handled now. Continue as you were". It also restated three rules: never `logcat -c`;
stop at once on any system or Google prompt; launch with `am start`, never `monkey`. It told this
session to take 202020's View C, then install 404040 and 606060, and take A-night, B and C on each,
accepting a frame only if it is pixel-identical outside the circle to 202020's frame for that view. The
planner-log line for that message was not given to this session, so it is not cited.

**The `logcat -c` incident.** At 11:21Z the previous session ran `logcat -c`, clearing the default
buffers including the crash buffer. That broke the rule that logs are read with `-d` only. The effect is
described under "Crash reads" above. This session read logs only with `logcat -b crash -d`.

**State at resume (11:33Z), read only:** Forager `MainActivity` top resumed; versionName
`1.0.1259+g75c050f7.dirty`; `lastUpdateTime` 2026-09-28 03:46:50; `firstInstallTime` 2026-09-22 11:15:05;
rotation 0/0; awake, no keyguard; crash buffer 0 lines; Maps tab on Topographical, no prompt
(`logs/50-resume-state.png`).

### 202020, View C

- `frame.sh` then gave a View A frame that was pixel-identical to `202020-A-night.png` in all four
  `cmp.py` regions (`logs/51-frame-202020-resume.png`).
- `viewc.sh` from it (`logs/52-viewc-202020-resume.png`) was **not** identical to the pre-pause
  clean View C capture `logs/46-viewc-202020-a.png`. It was shifted by exactly 3 px vertically (`offs.py`:
  1108 differing pixels at that shift, 321 435 at zero shift).
- A full rerun, `frame.sh` then `viewc.sh` (`logs/53-viewc-202020-r1.png`), matched `52` at zero shift.
  Its only differences were along the dashed edge (rows about 832-884) and in the status bar.
- The header's grid reference had moved by about 2 m since capture 46. The digits are not recorded here
  because they locate the owner.
- `202020-C.png` was taken from the r1 state and is identical to the r1 snap (0 differing pixels). It is
  View C's reference.

### 404040

- sha256 `92d5cd8ce8260530136e64f6a7c9cfa2cdd1eda3af65b8508e9a9eeb1ebe35a8`, which matches `FACTS.txt`.
- `adb install -r` at 11:39:26Z: "Performing Streamed Install / Success" (`logs/60-install-404040.txt`).
- `dumpsys package` (`logs/61-package-404040.txt`): versionCode 1259, versionName
  `1.0.1259+g75c050f7.dirty`, `lastUpdateTime` 2026-09-28 04:39:27 (phone time, PDT), `firstInstallTime`
  2026-09-22 11:15:05 (**unchanged**), signatures `[d59f30b8]`.
- Crash read: 0 lines (`logs/62-crash-404040.txt`).
- Launched with `am start -n com.zynergylabs.forager.app/.MainActivity`. Rotation 0/0 afterwards. It
  opened on the Maps tab, Topographical.
- **View A-night:** `frame.sh`, first run. `404040-A-night.png` reads 0 differing outside the circle
  against `202020-A-night.png`.
- **View C:** three runs of `frame.sh` + `viewc.sh` (`logs/65-viewc-404040-r1.png`, `66-viewc-404040-r2.png`,
  `66-viewc-404040-r3.png`). Each View A frame read 0 differing outside the circle. Each View C frame
  landed at the **same (3, 3) px offset** from `202020-C.png` (1246 differing pixels at that shift, 86 525
  at zero shift, 101 507 outside the circle). **Not accepted; no 404040-C shot was taken.**
- The header's grid reference sat about 2 m from its value at 202020-C. A read-only poll every 20 s from
  11:45:38Z to 11:51:13Z (16 reads) showed it unchanged throughout.
- **View B:** not taken (stopped first).

**Why View C does not reproduce (inference, unconfirmed).** `frame.sh` step 4 centres the camera on the
device's fix. At View A's zoom (about 10.6, derived) a 2 m move is well under a pixel, so View A stays
pixel-identical. `viewc.sh` then zooms four levels (16x), which turns it into about 3 px. The offset
matched the fix changes seen (the 202020 pre- and post-pause runs, then 404040) and held steady while the
fix held steady. That fits the explanation but has not been tested by moving the fix. Nothing points to
the candidate builds themselves: the 202020 frame shifted too, with no install in between.

**Why this is a stop.** The planner's rule is to redo until the frame is pixel-identical. While the fix
stays put, redoing does not converge: three runs gave one stable offset. That meets the dispatch's abort
condition "framing that cannot be reproduced across candidates", for View C. Views A and B are not
affected.

### Checker used for "outside the circle"

`logs/outside.py` (sha256 `3c3546d1…842`) counts pixels that differ by more than 24 in any channel.
- Views A and B: over the map area (0,300)-(915,1880), minus a disc of radius 415 px about (439,1254),
  the circle measured earlier.
- View C: rows 300-820, above the dashed edge.

`cmp.py`'s left strip (x 0-40) takes in the circle's leftmost rim at x≈29-31, where the candidate fill
blends in: 9 such pixels showed up for 404040. That is why this checker was added.

Controls, both of which bite:
- `202020-A-day` against `202020-A-night`: 855 050 differing.
- `46-viewc-202020-a` (the 3-px-off frame) against `202020-C`: 89 079 differing.

`logs/offs.py` (`ab1d8313…630`) finds the best integer shift in [-8,8]².

### Shots added this session (`shots/SHA256SUMS`)

| File | sha256 | Framing |
|---|---|---|
| `202020-C.png` | `bf903058a98b38b378dd629c290b02d76d9ba6e88cfe01affa1f9d9c3f8e37c3` | frame.sh + viewc.sh (run r1); matches run `52` apart from the dashed edge |
| `404040-A-night.png` | `daaf64179dd5d2d3afa7f9ceb1e49089de4f7caf7355702c7791161f45fea502` | frame.sh; 0 differing outside the circle against `202020-A-night` |

`logs/snaps.log` holds two "SHOT 202020-C" lines. The 11:25:17Z line is the invalid prompt capture
described above. The 11:39:09Z line is the real shot.

### Not done

404040-B and 404040-C; everything on 606060; restoring Night Maps. 606060 is not installed.

### State left on the phone (11:52Z)

| Item | State |
|---|---|
| Installed | 404040 (`lastUpdateTime` 04:39:27 PDT) |
| Night Maps | **on** (start value off), not restored |
| Basemap | Topographical |
| Map | at View C of 404040 run r3 |
| Rotation | 0/0 |
| Crash buffer | 0 lines at 11:51Z |
| Top activity | Forager `MainActivity` |

### Decisions I made (resumed session)

- Treated the pre-pause capture `46` as the thing to check the first post-pause View C against. After it
  failed, I made the two agreeing post-pause runs the reference and took `202020-C.png` from them. With
  no `202020-C` shot yet, the planner's rule did not name a reference for View C.
- Accepted differences along the dashed edge line as "outside the circle" noise. The edge is the circle's
  own boundary.
- Wrote `outside.py`, with the disc radius 415 about (439,1254) and View C's cut-off at row 820, and
  `offs.py`. I used them as the acceptance test in place of `cmp.py`'s regions, which take in the rim.
- Took View C before View B on 404040, to save a basemap round-trip. The order of views is not stated.
- Polled the fix for about 6 minutes before calling the abort condition met. The length was my choice.
- Judged the View C mismatch to meet the abort condition "framing that cannot be reproduced across
  candidates", and stopped all phone work, including 404040-B, which would have reproduced. The
  alternative was to go on with A and B and leave C for later.
- Left Night Maps on and 404040 installed, rather than restoring settings on a stop, since step 4 of the
  continuation names restoring as the last step and the capture is unfinished.
- Left the header's grid digits out of this record as identifying.

### Flags outside scope

- Three ways View C could be made independent of the fix, all needing a planner or owner call:
  - drop `frame.sh`'s final locate and pan to a fixed point instead (202020 would need reinstalling,
    and the install order and exception do not cover that);
  - accept a stated few-pixel offset for View C;
  - wait for the fix to return.
- The shift is invisible at View A's zoom, so the earlier A/B "pixel-identical" results say nothing about
  sub-pixel camera agreement. A check at one zoom cannot see sub-pixel drift that another zoom magnifies.

## Fourth coder session (11:53Z to 11:57Z): stopped on a question, no phone action

**Planner message, part of this dispatch, quoted verbatim.** The planner-log line for it was not given to this
session, so it is not cited.

> **Planner message, part of the dispatch. Quote it verbatim in the run record.** The previous coder stopped on "framing that cannot be reproduced across candidates": 404040's View C sat at a steady (3,3) px offset from `202020-C`, which it attributed to about 2 m of GPS drift, magnified at View C's zoom. The planner is relaxing its own abort rule, because these shots exist only for the owner to compare fill colour by eye.
>
> - **A frame is accepted if** `offs.py` finds its best whole-pixel shift against the 202020 reference for that view is **at most 5 px in each axis**, and, at that shift, it matches outside the circle as `outside.py` measures.
> - **Record each accepted shot's shift** in the run record.
> - **Do not reinstall 202020.** Do not chase the fix, and do not wait for it to drift back.
> - A larger offset, or a mismatch that remains after the shift, is still an abort. So is any prompt appearing over the app.
>
> The standing rules still apply: never `adb logcat -c`, launch with `am start` only (never `monkey`), and `install -r` only.
>
> **Phone state at 11:52Z:**
> - installed: 404040;
> - Night Maps: **on** (its start value was off);
> - basemap: Topographical;
> - rotation: 0/0.
>
> **Continue:**
> 1. Take 404040 View C again under the rule above; its runs `65`/`66` may already qualify. Then take 404040 View B.
> 2. Install 606060 with the same checks as before: sha256 against `FACTS.txt`, `firstInstallTime` unchanged, and a crash read. Then take views A-night, B and C.
> 3. Restore Night Maps to **off** and the basemap to Topographical, then read both back.
> 4. Leave 606060 installed.
> 5. Update the run record and push it to your branch.
>
> **Final message:** list the shot paths in the order 202020, 404040, 606060, grouped by view (A-night, B, C, plus 202020's A-day). Give no judgement of which looks best. You are the only coder on the phone.

### Applying the rule to the existing 404040 View C runs (offline, no phone action)

`outside.py` takes no shift, so the rule's "at that shift" cannot be run with it unchanged. I wrote
`logs/outside_shift.py` (sha256 `81c7d7d28fbadbba8b1c71bfb432f24d9640a5ae58e1791f128cb934ca9f35d6`). It keeps
`outside.py`'s regions and its >24 threshold and compares ref(x,y) with new(x+dx,y+dy), the convention `offs.py`
reports. Pixels whose shifted position falls off the image count as differing. Self-check: `202020-C` against
itself at (0,0) gives 0. At (0,0) it gives 101 507 against `66-viewc-404040-r3`, the same as `outside.py`.

Reference `shots/202020-C.png`, View C region (rows 300-820):

| Run | `offs.py` full map area, best | `offs.py` rows 300-820, best | `outside_shift.py` C at best shift |
|---|---|---|---|
| `65-viewc-404040-r1` | (3,3), 4018 differing | (3,3), 1246 | **1919** of 475 800 |
| `66-viewc-404040-r2` | (3,3), 3782 | (3,3), 1246 | **1919** |
| `66-viewc-404040-r3` | (3,3), 3571 | (3,3), 1246 | **1919** |

The shift is within the rule's 5 px in each axis. At that shift, 1919 pixels still differ outside the circle.

**Where the 1919 sit** (a diff map, looked at, kept only in the session scratchpad): 1070 are in columns 0-24,
mostly the left-edge handle overlay, which is fixed on screen and so is misaligned by any x-shift of the map. The
rest are thin fringes along roads and building outlines, spread over the whole region. That is consistent with
the true offset not being a whole number of pixels, but this is inferred, not measured.

**Control, same fill colour, known whole-pixel offset:** `46-viewc-202020-a` against `202020-C` gives
`offs.py` best (0,+3), and `outside_shift.py` at (0,+3) leaves **66** differing. (That run's first attempt used
(0,-3) and gave 121 709. It was a sign error on my side: `offs.py` had been run with 46 as the reference. It is
recorded here so the figure is not misread later.)

**Why this is a stop, not an accept or an abort.** The rule accepts a frame that "matches outside the circle as
`outside.py` measures" at its shift, and calls "a mismatch that remains after the shift" an abort. `outside.py`
gives a count, not a verdict. Every acceptance in this record so far has meant 0 differing. At a nonzero shift,
0 cannot be reached, because fixed overlays move against the map. The rule does not say what count is a match, so
choosing a tolerance would be deciding it myself. Options I can see:
1. accept with a stated tolerance (1919 of 475 800 is 0.40%; the same-colour control is 66, 0.014%);
2. accept only if the residual is shown to be overlay and sub-pixel fringes, for example by masking the fixed
   overlays, and state the count after masking;
3. treat 1919 as a remaining mismatch, which is an abort under the rule.

Phone work after this is unaffected by the answer only for View B (the View A frames still read 0 at (0,0):
`64-frameA-404040-r1` against `66-frameA-404040-r3`, and `66-frameA-404040-r3` against `202020-A-night`, both 0
of 904 723). I did not take 404040-B either: the planner's order is C and then B, and an abort on C would stop B.

### Phone state (read only, 11:55:58Z)

| Item | State |
|---|---|
| Top resumed | Forager `MainActivity` |
| Installed | 404040, versionName `1.0.1259+g75c050f7.dirty`, `lastUpdateTime` 2026-09-28 04:39:27 (PDT) |
| `firstInstallTime` (user 0) | 2026-09-22 11:15:05, unchanged |
| Night Maps | on (not read this session; unchanged since the planner's 11:52Z state, since nothing was touched) |
| Rotation | 0 / 0 |
| Keyguard | not showing |
| Crash buffer (`logcat -b crash -d`) | 0 lines |

No touch, install, launch or setting change was made this session.

### Decisions I made (fourth session)

- Wrote `outside_shift.py` to apply `outside.py` "at that shift". The shift convention, and counting pixels
  shifted off the image as differing, are mine.
- Ran `offs.py` over View C's rows 300-820 as well as its default full map area. The rule does not say which
  region `offs.py` uses. Both gave (3,3).
- Treated "matches" as undetermined for a nonzero count, and stopped to ask instead of picking a tolerance.
- Did not take 404040-B before the answer, though it would likely pass under the rule.

### Flags outside scope

- **Dual App profile.** `pm list users` shows user 95, `DUAL_APP`. In `logs/00-package-start.txt` Forager is
  `installed=false` for user 95. In `logs/11-package-202020.txt` (after the first `install -r`) and
  `logs/61-package-404040.txt` it is `installed=true` (stopped, never launched). So `adb install -r` without
  `--user` has also installed the candidate builds into Samsung's Dual App profile. Nothing was done about it.
  Removing it would be an uninstall, which this dispatch forbids. The owner or planner decides.
- The rule's "matches" will run into the same question on 606060's View C, and on any View A or B that shifts.
