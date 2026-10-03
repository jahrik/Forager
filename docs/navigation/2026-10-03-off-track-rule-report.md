# "Off track" redefined to fit the walker (dispatch 2026-09-28-425, plan task T21)

**Status: built and pushed on `off-track-rule`, not merged.** The owner's next walk is the real test.

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-03.md`. The planner's rulings on the coder's findings came by message: question 2 ("truly once") and the accepted design. Question 1 was answered by the owner in the coder's window.
**Base:** `origin/main` at `f1b53aca`, checked against the remote. `main` (`4f9f344b`, with T22) was merged in with `git pull --no-rebase` before the full suite (`9f779b29`). It merged without conflicts.
**The owner's go,** to this session directly: "Yes, start -425", and, for the copy, "Yes, copy the track".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-off-track-rule/`. **The walk's positions stay there.** This repository is public and they are real places, so only times, distances and counts are filed here.

## What this is, in plain terms

"Off track" used to mean "your distance to the start went up by 25 m over three readings". On the owner's walk that fired five times while they walked home, four of them falsely, each just after a rough network location reading.

Now it means: **you are more than about 40 m from the path you walked out, for about 15 seconds.**
- A GPS reading that says it is imprecise gets more room.
- Network readings don't count at all.
- You get **one** alert when you go off, and another only after you have been back on the path for 10 seconds.

Replayed over the owner's own walk, the new rule alerts **once**, at the start of their deliberate detour, and not for the rest of the way home.

## Verified before building (reported by message first)

1. **Today's rule and who reads it** (at `f1b53aca`):
   - **The rule:** `domain/DetectOffTrackUseCase.kt`, the last 3 distances to the start, off when the last minus the first is over 25 m.
   - **The window:** `domain/ReturnWatch.kt` `onFix` (`:195-220`) fed it every raw fix, network included. The 120 s cooldown was at `:274`, the one `deliver()` at `:218`.
   - **Its one reader:** `ReturnWatchState.isOffTrack` → `TrackRecordingViewModel.kt:300` → `TrackRecordingUiState.isOffTrack` → `MainActivity.kt:626` → the control pill's colour (`AvailabilityMapControlsUi.kt:229`).
2. **"The track as it stood when Return was tapped."**
   - The watch had no track. The service keeps points through its sampler (`service/TrackRecordingService.kt:143`) and buffers unflushed ones (`:72`).
   - Reading the store at Return would miss up to 30 s, the same lag as in T5.
   - **Accepted:** the service hands the watch each kept point. Edge, now refined: the path is the start, then the kept points, so a Return before any point is kept is measured from the start alone, and only no start at all decides nothing.
   - **"GPS only"** uses the existing timestamp rule (`domain/NetworkProviderFix.kt:48`), because a fix carries no provider. Checked on the walk's own log, which prints provider and time for 729 fixes: all 81 network fixes have sub-second stamps, and so do 2 of 648 GPS fixes, which therefore don't count.
3. **The replay:** below.
4. **Where RouteHome's "off the route" and this rule can disagree** (reported, not reconciled):
   - RouteHome's is the hop from the walker to the most recent stored point (past 50 m, back under 45 m); this one is the distance to the path at Return (40 m plus accuracy, held 15 s).
   - **(a)** Between 40 and 50 m beside the path: an alert while the HUD still shows a route.
   - **(b)** A detour after Return is recorded and becomes RouteHome's route home, while this rule calls it off the path. The owner's walk is this case.
   - **(c)** On the outbound path but more than 50 m from the stored tail (the flush lag, rejected fixes): "Unable to calculate route" and no alert.
   - **(d)** RouteHome reads the accuracy-gated fix, this rule the timestamp test, so a good-accuracy network reading counts for one and not the other.

## The replay of the owner's walk

**What there was:**
- **The stored track,** copied once, read-only, from the S22's debug database. Track `e3872320`: 220 points, 07:46:38Z to 08:10:11Z, every one second-aligned (GPS), accuracy at most 14.8 m. The median gap is 5 s; one gap is 203 s.
- **The log.** Its GPS fixes run at most 9.9 m accuracy. Its 81 network fixes run 12.5 m to 600 m (median 400 m), and carry no position.

**When Return was tapped** is not in the log. The screen-on lines leave two windows: 07:52:40–07:55:11Z, around the farthest point at 07:54:34Z, and 07:59:50–08:00:41Z. The owner first said "Halfway through the walk". Asked which, they chose "A: at the turnaround".

| Rule | Return at the turnaround (the owner's answer) |
|---|---|
| Today's, on the stored points alone | **0 alerts** |
| The new one, by a Python replay | **1 alert, 07:56:07Z** |
| The new one, through the real `ReturnWatch` and `OffTrackJudge` | **1 alert, 07:56:07Z**: a path of 55 kept points behind the start, 165 readings after it |

**What the numbers show:**
- **The detour.** After the turnaround the walker went off the outbound path: 45 m at 07:55:50, a peak of 175 m at 08:00:36. That was five seconds before the first real alert, which the owner remembers as their one deliberate divergence. The new rule's single alert is that detour's start.
- **The way home.** From 08:05 on, the walker stays within 0–17 m of the path, with one 38 m bump at 08:07:35, under the 43.8 m line. That stretch is the owner's "the other side of the street", and the new rule is silent through it.
- **The old rule** gives no alert on what was stored, so its five real alerts needed readings that were never stored: the network fixes the sampler rejects.

**What the replay cannot show:**
- Today's five alerts can't be reproduced, because the network fixes' positions aren't in the log. That they caused them is a strong correlation, not a proof.
- The real rule reads raw fixes about once a second; the replay read the stored points, about every 5 s.
- The 08:00 Return reading (B) would have given no alert, with its 38 m bump just under the line. That reading is set aside on the owner's answer.

The real-judge replay ran as a throwaway test that read the evidence folder's export. It was deleted before any commit, and its output is in `t3-replay-real-judge/`.

## What was built

- **`domain/OffTrackJudge.kt`** (new), pure:
  - **The figures, each named once:** `OFF_TRACK_LINE_METERS = 40.0`, `OFF_TRACK_HOLD_MILLIS = 15_000`, `OFF_TRACK_MIN_READINGS = 3`, `OFF_TRACK_REARM_MILLIS = 10_000`. All provisional.
  - **The distance** is to the nearest point of the path's line, not its nearest recorded point.
  - **Network readings are skipped.** The line widens by the reading's accuracy, with no ceiling: the walk has no poor GPS to choose one from.
  - **Off for 15 s and 3 readings** gives one alert, and `isOffTrack` turns on. **Back on for 10 s** re-arms it and clears `isOffTrack`. A reading back on restarts the 15 s.
  - **An empty path decides nothing.**
  - Time is the readings' own, so a replay runs it as the phone did.
- **`domain/ReturnWatch.kt`:**
  - `onKeptPoint` (new) keeps the stored track.
  - `startReturn` snapshots it as the path.
  - The judge is made at the return's first measurable fix, with the start in front.
  - Each fix is the judge's reading, and the alert is delivered on the reading that goes off.
  - **Gone:** the three-reading window, the 120 s cooldown (replaced by once per stray, ruling Q2), the clock parameter and `keptReadingCount`.
  - **Kept as they were:** the lock, the begin/end/early-Return handling, the delivery with `overridesSilence = false`, and `isOffTrack`.
- **`service/TrackRecordingService.kt`:** one call, `returnWatch.onKeptPoint(candidate)`, beside the sampler's accept. The batching is unchanged.
- **`AppContainer.kt`:** `ReturnWatch(computeReturnToStart, alertDelivery)`.
- **`domain/DetectOffTrackUseCase.kt`:** deleted (retired), with its own test file.
- **Comments that named it,** now naming `OffTrackJudge`: `TrackRecordingUiState.kt`, `TrackRecordingViewModel.kt`, `AvailabilityScreen.kt` and `AndroidManifest.xml`.

## Tests

**Tests first, pushed failing** (`2319ca33`): `OffTrackJudgeTest` against a stub that never decided "off". Six failed (`t1-tests-first`). Three passed trivially, because they expect no alert: the street, walking back, and the empty path. Each of those has a revert check below.

New:
- **`OffTrackJudgeTest`** (9):
  - Going off by more than 40 m for 15 s alerts once, at 15 s.
  - The other side of a street never alerts.
  - A far network reading neither counts nor counts as back on.
  - Poor GPS accuracy widens the line.
  - One alert per stray, re-armed after 10 s back on.
  - Walking back toward the start along the path never alerts.
  - An empty path decides nothing.
  - Three readings are needed.
  - A reading back on restarts the 15 s.
- **`ReturnWatchTest`:**
  - Back on for 10 s re-arms the alert.
  - The path is the points kept before Return, and a detour kept after it stays off the path.
  - A far network reading among GPS readings on the path does not alert.
  - Kept points before the service has begun the watch are not kept.
- **`TrackRecordingServiceSwipeAwayTest`:** the points the real service keeps before Return are the path (20 of 20).

Existing tests changed by the new rule (the dispatch: "the off-track tests that encode today's rule change because their rule changes"). The file before is saved as `ReturnWatchTest.before.kt`.
- **`ReturnWatchTest`, retimed with the same names and claims.** Each walked away three readings 1 s apart and expected off track or an alert; each now walks away four readings 5 s apart (15 s), and the claims are the same:
  - "moving steadily away … sets off-track"
  - "going off-track delivers exactly one alert, not one per fix" (its last line was "the cooldown keeps it"; it is now "once per stray")
  - "stopping the recording clears returning and off-track state"
  - "moving away from the start does not set off-track unless actively returning"
  - "a return asked for before the service has begun the watch is kept for that track"
  - "a fix before the service has begun the watch is not a reading"
  - "with no start point there is nothing to measure against"
  - "ending the recording leaves nothing behind for the next one" (its cooldown note is now kept points and the stray)
- **`ReturnWatchTest`, given a walk-out path** (kept points), because under the new rule on-track means on the path:
  - "staying on track never delivers an alert" and "moving steadily toward the start while returning stays on track". Before, the readings walked from 333 m out back toward the start with no path. They now walk back along a kept walk-out of the same span, and the same assertions hold.
- **`ReturnWatchTest`, replaced:**
  - "a sustained drift alerts again once the cooldown elapses" (2 alerts after 120 s) → "a sustained drift alerts once, and not again however long it lasts" (1 alert over ten minutes).
  - "stopReturn resets the cooldown so a later return attempt can alert immediately" → "stopReturn ends the stray, so a later return attempt can alert again" (2 alerts, same as before).
  - "keeping only the readings the check reads decides exactly as keeping every reading did" (the watch against `DetectOffTrackUseCase`, and `keptReadingCount == 3`) → "the watch decides exactly as the judge does, over the same path and readings" (the watch against a bare `OffTrackJudge`, over 200 random walks of 400 readings, network ones included).
  - "two threads feeding the same walk away deliver one alert, not two": still 1 alert, retimed. Its `keptReadingCount == 3` assertion is gone with the counter; the claim was the deliveries.
  - "toggling the return … never throws": `keptReadingCount == 0` becomes `pathPointCount == 0`.
- **`TrackRecordingSwipeAwayTest`**, "Return on the screen marks the watch as returning, and the watch's off-track shows on the screen": three readings at 7, 12 and 17 s became four, adding 22 s, for the 15 s hold. Its assertions are unchanged.
- **`DetectOffTrackUseCaseTest`** (6 tests of the retired rule): deleted with the class.
- **Six fixtures** build a `ReturnWatch`, and each dropped the retired rule (and, in two, the cooldown clock) from the constructor. No assertion in them changed. They are in `NetworkFixExclusionPerConsumerTest`, `TrackDeleteTest`, `TrackRecordingSwipeAwayTest`, `TrackRecordingViewModelTest`, `TrackRecordingSundownTest` and `JournalPendingDeleteTest`.

Nothing skipped or silenced.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD (`ba99e817`) after each check.

All 14 compiled with 0 errors and failed for their own edit (`reverts-out.txt`).

| Check | One edit | Fails with |
|---|---|---|
| o01 | the line not widened by accuracy | "a GPS reading reporting poor accuracy widens the line": 0 expected, 1 |
| o02 | no 15 s hold | alert "at 15" expected, at reading 2; "restarts the 15 s" |
| o03 | one reading enough | "two readings 20 s apart are not enough" |
| o04 | not once per stray | 16 alerts where 1; 121 over a sustained drift; 100 from two threads |
| o05 | re-armed at once | "not re-armed after 5 s back on": 0 expected, 1 |
| o06 | network readings count | "the network readings on the path did not count as back on": 1 expected, 0 |
| o07 | the line at 10 m | "walking on the other side of a street never alerts": 0 expected, 1 |
| o08 | distance to the start only, not the path | walking back along the path, the street, staying on track (10 tests) |
| o09 | an empty path decides | "an empty path decides nothing": 0 expected, 1 |
| o10 | the path live, not snapshotted at Return | "still the path at Return": 7 expected, 11 |
| o11 | the service keeps nothing for the watch | the service's path: 20 expected, 0; the swipe-away notification not posted |
| o12 | no start in front of the path | every walk away with no kept points: 0 alerts (9 tests) |
| o13 | an alert on every off reading | 121 where 1; the watch against the judge, "deliveries so far" |
| o14 | kept points kept before begun | "kept points before the service has begun the watch are not kept": 0 expected, 7 |

## Suite

| Run | Tree | Result |
|---|---|---|
| Tests first, against the stub (`t1-tests-first`) | `2319ca33` | 9 tests, 6 failures, as expected |
| Touched classes (`t2-built`) | before the service test | 10 classes, 176 tests, 0 failures |
| The walk through the real judge (`t3-replay-real-judge`) | `ba99e817` plus the throwaway test | 1 alert at 07:56:07Z |
| Full suite (`t4-full-suite`) | `9f779b29`, with `main` merged | 425 classes, 3509 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled, counted per file against `origin/main`.** `main`'s 3,501 tests (the navigation view's suite), minus `DetectOffTrackUseCaseTest`'s 6, plus `OffTrackJudgeTest`'s 9, `ReturnWatchTest` 21 → 25, and `TrackRecordingServiceSwipeAwayTest` 8 → 9: 3,509. Classes: one deleted, one added, so 425. The 24 skipped are as before.

## Not done, and why

- **The owner's next walk** is the test of the 40 m, 15 s and 10 s figures.
- **A sensor or provider that stops reporting** is not caught, as before.
- **RouteHome and this rule can disagree,** as listed under item 4; not reconciled, by the dispatch.
