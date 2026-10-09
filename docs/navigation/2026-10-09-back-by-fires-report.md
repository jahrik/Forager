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

`scripts/verify-policy-permissions.sh`: all checks passed, so the manifest gained a receiver and no permission.

## PENDING

- Full suite (asked of the planner, disk).
- S22: the launch check, and the desk check including screen off past the time, with the two record files read
  afterwards.
- The four disclosure sections and the index row.
