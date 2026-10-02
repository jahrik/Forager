# Device check: the off-track alert's move and the reopened app, on the S22 (dispatch 2026-09-28-400, Amendment 4)

**Status: run to the end on 2026-10-02, 13:21Z to 13:27Z. Every gate passed. One observation confirmed a reading: tapping the recording notification stacks a second copy of the screen.**

**Dispatch:** `prompts/preserved/2026-10-02-01.md`, Amendment 4 (continuation 2026-09-28-413), read on branch `records-after-149` at `ca8e8c84`.
**The owner's go:** to the planner, "Phone is ready for the check"; and to this session directly, asked with the uninstall and the data wipe named, "Yes, uninstall and run".

## Predictions, pushed before step 1

Written before any phone command of this check.

- **Step 5 (Forager opened again after the swipe):** the screen offers "Stop recording track" and the return HUD is on screen.
- **Step 6 (Stop on that screen):** the service is no longer listed and the recording notification is gone, and no "Ignoring a start for track" line was logged for the repeated start.
- **Step 8 (after a stop from the notification, Forager opened):** the screen offers "Start recording track".
- **Step 9 (a stuck track, then the app opened):** the sweep's line is the "nothing recorded in them" one, not the "left open by an earlier process" one. The service writes its points every 30 seconds or every 20 points, and the process is stopped after 20 seconds, so by my reading nothing has been stored. The Maps tab offers "Start recording track", and the newest track's row in Records does not read "recording".

## What was checked, in plain terms

Parts 2 and 3 of this dispatch on real hardware, for everything no headless test reaches. **Not the off-track alert itself:** the phone sat on a desk on USB power and nothing walked.

## The row, for the project to keep

| | |
|---|---|
| Device | S22 Ultra, SM-S908U, serial `R5CT321008R` |
| Android / One UI | 16 / 8.0 |
| Build | debug, `1.0.2485+g7f45b99c`, versionCode 2485, from `alerts-in-service` at `7f45b99c` (its `app/` is identical to `51b7ab32`, the Part 3c build) |
| APK sha256 | `9bf9e88fd7e26514ae799847d56dbcab6d2cbb2b31a39e8b4ea03bf2722b5457` |
| Power | USB |
| Reopened screen showed Stop | **yes** |
| HUD on reopen | **yes**, with the start marker drawn |
| Stop on the reopened screen ended the service | **yes** |
| "Ignoring a start" logged | **no** |
| `MainActivity` records after the notification tap | **2** |
| Screen agreed after a stop from the notification | **yes** |
| Stuck track ended at the next open | **yes**: "Ended 1 track(s) left open with nothing recorded in them, each at its own start time." |

## Predictions against what happened

All four held. Step 5: Stop and the HUD. Step 6: service and notification gone, no "Ignoring a start" line. Step 8: "Start recording track". Step 9: the "nothing recorded in them" line, Record offered, and the newest track's row not reading "recording". The planner's predictions were the same except at step 9, where it allowed either line.

## Steps

Evidence is in `~/Zynergy/device-evidence/2026-10-02-alerts-in-service/device-check/`. Each state reading is four files with the step's name: `-state.txt` (time, rotation, awake, keyguard, pid), `-services.txt`, `-notifications.txt`, `-activities.txt`. The rotation read upright before every tap, the swipe and each key press. Every control was found by its text or description in a fresh dump.

| Step | What was done | Result | Evidence |
|---|---|---|---|
| 0 | Read-only baseline, 13:21:19Z | Release `1.0.2426+ge0239399` installed, pid 5455, upright, awake, no keyguard, USB, no service listed | `step0-baseline.txt`, `step0-*` |
| 1 | `adb uninstall`, then `adb install -g` of the debug APK, 13:21:32Z | **Pass.** Both "Success". Installed versionName `1.0.2485+g7f45b99c`; `firstInstallTime` 06:21:34 on the phone's clock, which is 13:21:34Z | `step1-commands.txt`, `step1-package.txt` |
| 2 | Launcher intent, 13:21:42Z; stopped and told the planner | **Gate met with nothing for the owner to do:** no first-run screen and no prompt; Maps tab with "Start recording track". Continued on the planner's word. | `step2.png`, `step2.xml`, `step2-*` |
| 3 | One tap on "Start recording track", 13:22:34Z | **Pass.** Service foreground, `foregroundId=1001`; notification id 1001 posted. **P1 = 456.** No dialog. The "Do Not Disturb is on" notice showed as a passing message, as expected. | `step3-tap.txt`, `step3.*`, `step3-*` |
| 4 | One tap on Return, 13:22:59Z (the HUD showed, "within 37 ft"). `KEYCODE_APP_SWITCH`; one swipe up on Forager's card, bounds `[265,388][1174,2265]` from the recents dump, 13:23:12Z; Home 13:23:16Z | The task (`t258`) is gone from `dumpsys activity activities` and from `dumpsys activity recents`. At 13:23:20Z: pid 456, service foreground, notification posted. **The same as Amendment 1's result; no difference.** | `step4-return-tap.txt`, `step4-returning.*`, `step4-recents.*`, `step4-swipe.txt`, `step4-after-swipe.png`, `step4-home.png`, `step4-*`, `step4-recents-dump.txt` |
| 5 | Launcher intent, 13:23:35Z. **The main path.** | **Pass.** The dump has "Stop recording track", "Stop navigating", "within 38 ft" and "Approaching": the return HUD is on screen. pid 456, a new task (`t259`). Observation: the start marker is drawn beside the position dot. No breadcrumb line is visible; the phone had not moved, so there was no line to see. | `step5-launch.txt`, `step5.png`, `step5.xml`, `step5-*` |
| 6 | One tap on "Stop recording track", 13:24:00Z | **Pass.** At 13:24:07Z the service is not listed and the notification is gone; the screen offers "Start recording track". **No "Ignoring a start for track" line, and no line at all from `TrackRecordingService`, `TrackRecordingViewModel` or `EndAbandonedTracks` since the process started.** See the note on the log below. | `step6-tap.txt`, `step6.*`, `step6-*`, `step7-logcat-pid456.txt` |
| 7 | "Start recording track" 13:24:29Z; Home; shade expanded; one tap on the body of Forager's notification (its title text), 13:24:42Z | **Observation: 2.** The task holds two `MainActivity` records: `Hist #1` (new, on top) and `Hist #0` (the one already there), `sz=2`. The new screen shows "Stop recording track": it took the recording up. | `step7-record-tap.txt`, `step7-shade.*`, `step7-body-tap.txt`, `step7-after-tap.*`, `step7-after-tap-activities.txt` |
| 8 | Home; shade expanded; Forager's notification expanded (one tap on its own Expand button); one tap on "Stop recording", 13:25:37Z; shade collapsed; launcher intent 13:25:41Z | **Pass.** Service gone and notification gone at 13:25:40Z. The screen offers "Start recording track". It is the **top** one of the two, `Hist #1`, the copy the notification tap made. | `step8.txt`, `step8-shade.*`, `step8-expanded.*`, `step8-after-stop-*`, `step8-reopened.*` |
| 9 | "Start recording track" 13:26:05Z; 20 seconds; `am force-stop`, once, 13:26:25Z; launcher intent 13:26:28Z | The process and the service were gone after the force-stop. **Pass:** the new process (pid 4343) logged, at 06:26:30 on the phone's clock, `Ended 1 track(s) left open with nothing recorded in them, each at its own start time.` The Maps tab offers "Start recording track". | `step9-record-tap.txt`, `step9-recording-*`, `step9-force-stop.txt`, `step9-after-force-stop-*`, `step9-reopened.*`, `step9-logcat-pid4343.txt` |
| 9, Records | "Journal", then "Records", then "Maps" | The newest track's row reads its start time and "0 points". It does not read "recording". Tracks: 3. | `step9-journal.*`, `step9-records.*` |

**End state, 13:27:14Z:** the debug build installed; Forager in front on the Maps tab offering "Start recording track"; no service; no Forager notification; shade closed; upright; one `MainActivity` (`t260`); pid 4343. The release build is not on the phone.

## Tracks this check left on the phone

Three, all ended. By start time on the phone's clock:
- **6:22 AM** (13:22:34Z). Stopped on the reopened screen at 13:24:00Z.
- **6:24 AM** (13:24:29Z). Stopped from the notification at 13:25:37Z. Records shows "1 point".
- **6:26 AM** (13:26:05Z). Force-stopped after 20 seconds; ended by the sweep at its own start time. Records shows "0 points". It has a Start waypoint, made by the screen from the first good fix before the force-stop.

Records also holds four waypoints from these three recordings: three Start markers and one End marker. The owner decides whether any of it stays.

## One finding, and it is the one the check went looking for

**Tapping the recording notification stacks a second copy of the screen.** With Forager's task alive behind the home screen, a tap on the notification's body created a second `MainActivity` on top of the first, in the same task. This confirms the reading in Part 3b's findings, which was inferred then.

What followed on this run was correct: the new copy took the running recording up and showed Stop; the stop from the notification was seen by it; and the once-per-process holder is what keeps the second copy's ViewModel from running the sweep again. What was not looked at: the copy underneath. Reaching it needs Back, which the amendment does not allow.

Not fixed here. It is on the planner's list for the owner.

## Notes on the evidence

- **The log.** My first read at step 6 used `logcat -d -s` with tag filters and returned no lines at all (`step6-logcat.txt`, 0 lines). A read that returns nothing cannot show that a line is absent. I read again by the app's pid (`step7-logcat-pid456.txt`, 4,060 lines from 06:21:43 on the phone's clock, the process's start), with a positive control: 441 lines from the app's own `ForagerFix` tag are in it. In that capture there is no "Ignoring a start" line and no line from the three tags. Step 6's log result rests on that second read, which also covers the repeated start at step 7.
- **Screenshots and dumps** from this check show the map around the phone and, in Records, positions. They stay in the local evidence folder. None is committed, and no position is written in this report.
- **The old dump file was removed before every new dump,** so a failed dump could not pass as a fresh one.

## What this check cannot show

- **The off-track alert,** with the app open or swiped away. Nothing walked.
- **An unplugged phone, or one in a pocket with the screen off.** Everything here is on USB with the screen awake.
- **Anything longer than a few seconds after the swipe.** Amendment 1 held it for a minute; this run did not wait.
- **A stuck track that has stored points.** The one made here had none, as predicted: the service writes every 30 seconds or 20 points, and the process was stopped after 20 seconds. The "ended at its last stored point" path was not exercised on the phone.
- **The copy of the screen underneath** after the notification tap.
- **A first run with prompts.** `install -g` granted the permissions, so no prompt appeared.
- **The Record refusal's sentence.** The screen never offered Record while a recording was running, so there was no way to press it.

## Disclosures

**Confirmed by observation:** every row of the steps table, from saved `dumpsys` output, screen dumps and the log capture, each with its time.

**Inferred:**
- That the reopened screen is a new Activity with a new ViewModel. What was observed is a new task id, the same pid, and a screen showing the recording. The ViewModel cannot be seen from outside the app.
- That no point was stored for the 6:26 track because the process was stopped before the service's first write. What was observed is the log line and "0 points".

**Could not determine:**
- What the copy of the screen underneath showed during steps 7 and 8.
- Whether the breadcrumb line is drawn on a reopened screen. The phone did not move.

**Premises that were wrong:**
- **The amendment's step 2** expected first-run screens for the owner to walk through. There were none.
- **Mine:** the tag-filtered log read, which returned nothing and proved nothing until it was replaced.

**Decided beyond scope:** nothing. The build was made at `7f45b99c`, not `2b9546cb` as the planner's message named: the head had moved by a merge of `main`'s records and by the predictions file, with `app/` unchanged. The notification's Expand button was tapped at step 8, which Amendment 4 allows by name.
