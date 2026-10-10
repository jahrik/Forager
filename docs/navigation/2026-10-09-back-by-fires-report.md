# Back by did not fire on the L3 walk: reproduction, record, and the walk logger mid-recording (dispatch 2026-09-28-796)

RECORD intent -796; the walk and the owner's answers -793 to -795; Back by's rulings -645 and -646. Branch
`back-by-fires`, cut from `origin/main` at `6373e2fe` (the base the dispatch names; verified equal to
`origin/main` at the start). Written by the coder, 2026-10-09 (UTC). **Draft, in progress**: sections marked
PENDING are filled as the work lands.

## What the walk showed (from the record, not re-observed)

Back by set on the S22 at about 13:10 with "Pick a time" (the owner: "showed 1:55"); Return at 13:48;
off-track at 13:57:48; return ended by the walker at about 14:19. The owner: "No back-by alert felt or seen".
The planner's read of the S22's `dumpsys usagestats` for 2026-10-09 (not repeated here): interruptions for
`track_recording` at 13:09:45 and `off_track_alert_v2` at 13:57:48, and **no `back_by_alert` interruption**.
So nothing was posted at all; the question is why no post, not why a post was silent.

## Reproduction (step 1)

Run 1, targeted (`TrackRecordingServiceBackByLoopTest`, `AvailabilityScreenQuickSettingsTest`): compiled
clean, 18 tests, 2 failures.

| Case | Test | Result |
|---|---|---|
| (a) a future time set after the service began, reached through its 15 s loop | `a future time set after the service has begun fires through the service's own loop` | passed |
| (b) the same with a Return in progress, 2 km from the start | `with a Return in progress and the walker far from the start, the time still fires through the loop` | passed |
| (c) do the screen and the service share one watch | read, not tested: `MainActivity.kt:63` (`container` is the application's), `:234` (`backBy = container.backByWatch`); the service reads `(application as ForagerApplication).container.backByWatch` (`TrackRecordingService.kt:184, 275`) | same instance |
| the suspected mechanism, modelled | `a GPS fix after the time brings the alert without waiting for the 15 s timer` | **failed**: "no back-by alert within 3 s of a GPS fix 1 ms past the time: the alert waits for the 15 s timer, not the fix" |
| an afternoon time picked on the dial reaches the ViewModel as 13, not 1 | `Pick a time, an afternoon hour picked …` | failed on the harness (the minutes dial was not reached); PENDING |

**What the failing case shows, and what it does not.** Back by is evaluated only by the service's own
coroutine loop, `delay(BACK_BY_TICK_MILLIS)` (`TrackRecordingService.kt:259-268`, `:531`); a fix is stored
(`BackByWatch.onFix`, `BackByWatch.kt:105-108`) and never triggers an evaluation. The recording holds no wake
lock (the only one in the app is the debug walk logger's, `WalkLogger.kt`, which did not start on this walk,
RECORD -795). A coroutine `delay` on `Dispatchers.Default` is measured on the monotonic clock, which does not
advance while the CPU is suspended, and nothing wakes the CPU for it. **Inferred, not observed:** with the
screen off, the S22 suspended, and the timer did not reach a tick past 13:55 before the walk ended. Supporting
evidence from an earlier measurement on the same phone (`2026-10-09-battery-saver-report.md:126-139`): a
screen-off recording spent about half its time in light and half in full Doze, and in one run the platform
stopped the app's GPS 5 minutes in. Not yet confirmed for the walk itself; PENDING the S22 reads.

**A correction to run 1, found in run 2.** Run 1's failing fix test was not evidence. It sent its second
fix in the same wall-clock second as the first, and Robolectric's location shadow drops a fix that comes
less than the registration's 1 s interval after the last one, so no fix after the time reached the service
at all. Run 2 found this when the fix path was built and the test stayed red. The record the test now prints
showed no fix-woken evaluation. The test now spaces its simulated fixes a second apart. The claim that Back by
ignored fixes still holds, read from the code (`BackByWatch.onFix` returned `Unit`, `BackByWatch.kt:105-108`
at `6373e2fe`). That claim was proven by revert R1 below, not by run 1.

**The owner's answers (RECORD -797):** the menu read "1:55 PM"; the strip line "Didn't look". The picker test
now passes: a picked 1:55 on a clock opened in the afternoon reaches `BackByChoice.AtTime(13, 55)`. So the
time was this afternoon's, not tomorrow's.

**The S22's own reading (the planner's, read-only, cited as consistent with a stalled timer, not proof):** the
screen was off from 13:48:37 to 13:57:10, across 13:55. It woke briefly at 13:57:10, 13:57:58, 14:01:27 and
14:12:39. There was no `back_by_alert` post. batterystats was reset at 14:54, when the phone was plugged in,
so it holds no history of the walk.

## The fix (step 2): the owner's option B (RECORD -797: "Check on GPS + wake-up alarm (Recommended)")

For Back by, and the same for the sundown alerts (the owner: "Yes, same fix (Recommended)"):

1. **A fix checks.** `BackByWatch.onFix` returns `true` once the time has come and its alert has not
   been given. The service then evaluates at once (`TrackRecordingService`'s collector, `EvaluationTrigger.FIX`).
   `SundownWatch.onFix` returns `true` once the next alert's due time has passed, as well as on the first fix
   as before. It takes that due time as used, so each due time triggers one evaluation, and the evaluation
   works out the next due time.
2. **A wake-up alarm.** `AndroidWakeUpAlarms` (`alert/AndroidWakeUpAlarms.kt`):
   - It uses `AlarmManager.setAndAllowWhileIdle(RTC_WAKEUP, …)`, which is inexact and needs no permission.
     `setExactAndAllowWhileIdle` needs `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`, which -646 chose not to ask
     for. The inexact alarm may come minutes late in deep Doze.
   - Delivery goes to a non-exported `WakeUpAlarmReceiver`, not to the service. The platform keeps the phone
     awake while a receiver handles its alarm, and through `goAsync` until it finishes (here at most 8 s). The
     app holds no wake lock of its own (release has no `WAKE_LOCK`), so a service coroutine woken by the alarm
     could go back to sleep before it posted anything.
   - Back by asks for the alarm on every set and "+30 min". It cancels it on Clear, I'm back, arrival after
     Return, Stop and the service's destruction.
   - Sundown sets the alarm, after each decision, for the next alert not yet given: the heads-up 30 min before
     the leave-by time, the leave-by time, or sunset. It asks again only when that time moves by more than a
     minute. It cancels the alarm when all three are given, the alerts are off, the walker has arrived, or the
     recording ends.
   - Each alert still fires once, and nothing about wording changed.
3. **The restart path.** A process death ends the recording, as before. An alarm left behind then starts a
   fresh process. Its receiver finds nothing watched, records `alarm-delivered nothing-set`, and does nothing.

## Observability (step 3)

- `files/back-by-record.log` (`FileBackByRecord`) writes one line per event:
  - started, and set (with the time, the track, and accepted or refused with the track being watched);
  - set-ignored with no recording on the screen (the `:315` no-op, which now also logs a warning);
  - +30 min, and each evaluation (due or not; by timer, fix or alarm);
  - fired, with its delivery result;
  - ended, by arrival, I'm back, Clear, Stop or service destroyed;
  - alarm-scheduled, alarm-cancelled and alarm-delivered.
- `files/sundown-record.log` (`FileSundownRecord`) writes the same kinds of line for the sundown alerts.
- No positions are written in either file. The service test checks every line for coordinates.

## The walk logger (step 4)

The Diagnostics switch now tells the logger when it changes (`WalkLogger.onSwitchChanged`, debug source set).
Switched on during a recording, the log starts for that recording at once. Switched off, it stops with
`END reason=switched-off`. Each recording start writes "Walk logger switch at recording start: on|off (track …)"
to diagnostics.log. The row's second line now reads "…Takes effect at once, also during a recording."

## Tests and revert checks

Targeted run 2 (after fixing two test compile errors and the fix spacing above): 17 classes, 110 tests,
0 failures, read from the JUnit XML.

Every revert check followed the same procedure. The runner refuses results when the build log has compile
errors. It restores the file from a copy saved before editing, never from git. `git status` was clean against
the pushed head afterwards.

| | Revert | Failed with |
|---|---|---|
| R1 | Back by fix check off (`onFix` … `&& false`) | "no back-by alert within 3 s of a GPS fix 2 ms past the time…"; "at the time" |
| R2 | Back by alarm delivery evaluates nothing | "no back-by alert within 3 s of the wake-up alarm, delivered 6 ms past the time"; "fired by the alarm… expected:<[…]> but was:<[]>" |
| R3 | Back by alarm never scheduled | "a wake-up for Back by: []" (and the alarm-path test and record story, both needing the alarm) |
| R4 | sundown fix check off | "past the due time, a fix asks" |
| R5 | sundown alarm never scheduled | "List is empty" in four tests: the list of `AlarmScheduled` records, read by the shared setup. This message is generic but names that list |
| R6 | sundown alarm delivery evaluates nothing | "expected:<[HEADS_UP]> but was:<[]>" |
| R7 | Diagnostics switch does not tell the logger | "switched on mid-recording, the log starts for that recording" |

**No local full suite.** The planner ruled it out: root had 1.7 GB free, and the last full run here dipped to 0.94 GB. The full suite runs in CI on this branch's pull request. CI is not triggered by a push to this branch (`.github/workflows/ci.yml` runs on pushes to main and pre-main, and on pull requests), and this dispatch said no PR. So the full suite stays **unrun** until a PR is opened.

`assembleDebug` built at `54274939` (1 min 30 s; free space 1.7 GB before, 1.4 GB after).

`scripts/verify-policy-permissions.sh`: all checks passed, so the manifest gained a receiver and no permission.

## Confirmed vs inferred

- **Confirmed by reading code at `6373e2fe`:**
  - Back by was evaluated only by the service's 15 s coroutine timer (`TrackRecordingService.kt:259-268`), and a fix only stored a position (`BackByWatch.kt:105-108`).
  - The screen and the service share one watch (`MainActivity.kt:63, :234`).
  - The recording held no wake lock in release.
  - Sundown had the same timer-only shape, apart from the first fix (`SundownWatch.kt:182-188`).
- **Confirmed by test, with revert checks:** the fix path and the alarm path, for Back by and for sundown, and the walk logger following its switch.
- **Confirmed on the S22:** Back by fires with the screen on (Case A rerun). Arrival after Return ends Back by and cancels its alarm, and the same holds for sundown (Case A, first run). The time picked on the dial is the afternoon's.
- **Inferred, not proven:** that on the L3 walk the timer stalled while the S22 slept. This rests on the screen being off from 13:48:37 to 13:57:10, no `back_by_alert` post, an earlier measurement of Doze on this phone, and the code. batterystats from the walk was reset, so it cannot confirm it.

## Could not determine

- Whether the S22 was in light or deep Doze at 13:55 on the walk (no history survived).
- How late the inexact alarm comes in light or deep Doze on this phone: Cases B and C did not run validly (see PENDING), so the screen-off firing is unconfirmed on the device.
- Whether the full suite passes: it was not run locally, by the planner's ruling, and no PR was opened.

## Premises that were wrong

- The dispatch's desk step, "start a recording, set Back by…, tap Return, wait. It must fire", cannot fire at a desk. Arrival after Return ends Back by (-646), and a phone at the start has arrived. Case A's first run showed exactly that.
- Run 1's red fix test was taken as showing the fault. It showed a harness artefact: two fixes in the same second, with the second dropped by Robolectric. Corrected in run 2 and proven by R1.
- The dispatch said the S22's walk logger "starts when switched on mid-recording" needed a change. It did: the switch was read only at the start (`WalkLogger.kt:66`), as -795 found.

## Decided beyond scope

- The per-evaluation line in both records, so a device check can see whether the timer ran. Not in the dispatch's list.
- The alarm is delivered to a broadcast receiver rather than to the service, because the platform keeps the phone awake for a receiver and the app has no wake lock.
- Sundown re-asks for the alarm only when its due time moves by more than a minute (`ALARM_RESCHEDULE_SLACK_MILLIS`), so a walk-back estimate that changes slightly each tick does not reschedule every 15 s.
- The Diagnostics row's second line now reads "…Takes effect at once, also during a recording." (debug only).
- The sundown record file `sundown-record.log`, a sibling of the Back by record. The owner asked for "the same logging" without naming a file.

## PENDING

- S22 launch check: **passed** (`scripts/s22-launch-check.sh`). Install -r, 1.0.3139+g54274939, status=verify, cold launch, process alive after 8 s, crash buffer empty. Output at `~/Zynergy/device-evidence/2026-10-09-back-by/launch-check.txt` (outside the repository).
- **S22 desk check, Case A, first run (with Return): Back by did not fire, as -646 rules. Recorded as a pass for the arrival path.**
  I drove the screen over adb after the owner removed the screen lock. The steps:
  - Record at 17:45:29 PDT.
  - Back by > Pick a time > 5:50 PM. The menu read "Back by 5:50 PM", and the record shows the time set (accepted) and its alarm scheduled.
  - Return at 17:47:20.
  The next evaluation, at 17:47:29, found the phone at the start after Return, which is arrival. The record shows `ended reason=arrival` and `alarm-cancelled`. The sundown record shows the same rule at work: leave-by fired on the first evaluation, with the notification posted and the vibration done. Its alarm was set for sunset at 6:35:35 PM and cancelled on arrival at 17:47:30.
  **The dispatch's desk step ("tap Return, wait. It must fire") cannot fire at a desk.** Arrival after Return ends Back by by ruling, so every desk case after that ran without Return. "Fires during a Return away from the start" is therefore covered **only by the JVM test** (case (b), 2 km out), not on the phone.
- **Case A, rerun (no Return, screen on): PASS.** "Stop navigating" at 17:53:19 ended the return and kept the recording running. Back by was set for 6:00 PM at 17:54:04, accepted, with the alarm scheduled. At 18:00:00.021 the timer evaluated it as due, and at 18:00:00.031 it fired with `notification=posted vibration=done`. The shade showed "You planned to be back by 6:00 PM" with "I'm back" and "+30 min". "I'm back", tapped over adb, gave `ended reason=im-back` and `alarm-cancelled`.
- Disclosed: my first UI dump printed the map strip's grid-reference readout once to my session transcript. Nothing was committed, and the dump file was deleted. Later dumps hide anything position-like.
- **Cases B and C (screen off, then forced deep Doze): NOT RUN validly. No result for either.** A script drove both over Wi-Fi adb once the S22 was unplugged at 19:49:40 PDT.
  - Case B's Record tap at 19:50:08 started no recording: the service count read 0. The app was brought to the front (`START u0 … MainActivity`, logcat 19:50:01). Why the tap did not start a recording is not determined.
  - The Back by step that followed set nothing. The newest line in `back-by-record.log` is still Case A rerun's `ended reason=im-back` at 01:03:17Z (18:03 PDT). The script's "record:" lines at 19:50:22 and 20:10:15 re-printed Case A's 17:54 `set` line, and that is not evidence of a new one.
  - So B (screen off 19:50:24 to 20:10) and C (forced deep idle 20:10:49 to 20:30:00, then unforced) ran with no recording and no Back by. Neither says anything about the alarm in Doze.
  - The script fault: it logged the service count and carried on instead of stopping on 0, and its record read did not filter to lines after the set. Evidence outside the repository: `~/Zynergy/device-evidence/2026-10-09-back-by/caseBC.log` and `batterystats-history.txt`. The latter is from runs with no recording, so it is not useful here.
  - **Still open:** how late the allow-while-idle alarm comes on this phone in light and deep Doze. Firing with the screen off is covered by the JVM alarm-path tests (R2, R3) and is not confirmed on the device.
