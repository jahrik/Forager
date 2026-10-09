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

## PENDING

- The fix (step 2), its option chosen by the planner, and the revert check.
- The record (step 3), the walk logger (step 4): built and pushed, not yet run.
- Suite counts, the S22 checks (step 5), the four disclosure sections.
