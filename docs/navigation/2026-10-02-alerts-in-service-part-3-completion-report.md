# Completion report: the reopened app takes up the recording it left (dispatch 2026-09-28-400, Part 3)

**Status: Part 3a (steps 1 to 5) is built and pushed on `alerts-in-service`, not merged. The planner merges. Not run on a phone. Part 3b (step 6) is not started; it waits for the planner's word.**

**Date:** 2026-10-02 (UTC).
**Dispatch:** `prompts/preserved/2026-10-02-01.md`, Amendment 3 (continuation 2026-09-28-406) and "Amendment 3, a ruling on the coder's stop" (continuation 2026-09-28-407), read on branch `records-after-148` at `9b66504a` and `5f70d6e0`.
**Base:** `origin/alerts-in-service` at `0f6485a4`. `main` was `f5103d0d` when work began, already contained in the branch. Pull request #148 merged during the work (`main` at `da9437fa`, then later); `main` was merged in again at `cdcb5e4e` and moved only `RECORD.md` and the dispatch file.
**The owner's go:** given to this session for Part 2 with later amendments to this dispatch included ("Yes, build Part 2", chosen with that stated). The owner's answers for Part 3, as quoted in the amendment: "Sounds good" to the main path; "1 A", "2 A", "3 I'll go with your recommendation".
**Follows:** [the pre-build report](2026-10-02-alerts-in-service-prebuild-report.md) and [the Part 2 completion report](2026-10-02-alerts-in-service-part-2-completion-report.md).

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`, test paths to `app/src/test/java/com/zynergylabs/forager/app/`. Every figure was read from a file in `~/Zynergy/device-evidence/2026-10-02-alerts-in-service/part3a/`; the folder is named beside it.

# Part 3a: steps 1 to 5

## What changed for the user

- **Record, swipe Forager away, open it again:** the Maps tab now shows the recording still running. It offers Stop, shows the breadcrumbs, and shows the return HUD if a return was under way. Stop on that screen ends the recording.
- **Record pressed while another recording is running** starts nothing and says: "Forager is already recording a track. Stop it before starting another."
- **A recording swiped away before the phone had a good fix** gets its start marker at the beginning of the line when the app is reopened, not where the phone is then.
- If the recording ended while the app was away, the screen offers Record, as before.

## The paths, step by step, with the test that holds each

`Swipe` is `ui/track/TrackRecordingSwipeAwayTest` (plain JVM, the real ViewModel and the real `ReturnWatch`). `Service` is `service/TrackRecordingServiceSwipeAwayTest` (Robolectric, the real service and container). `Watch` is `domain/ReturnWatchTest`.

### The main path

`Recording > swipe the app away > open Forager again > the screen shows the recording still running, and the return HUD if you were heading back`

| Step | Held by |
|---|---|
| The watch says a recording is running, for which track, in which mode | `Watch`: `the watch says it is begun, for which track and in which mode, only between begin and end` |
| A Return accepted early is not a running recording | `Watch`: `a return accepted before the service has begun does not make the watch begun` |
| The reopened screen offers Stop, with the track's start time and mode | `Swipe`: `Recording, swipe the app away, open Forager again - the screen shows the recording still running, and the return that was under way` |
| It shows the breadcrumbs, and goes on reading new ones | the same test |
| It shows the return that was under way, pointing at the start marker made before the swipe | the same test |
| The return is still live: a walk away alerts, and the reopened screen shows off track | the same test |
| No trip-start warning on a take-up | the same test: the first screen, on a silenced phone, had the warning; the reopened one has none |
| The same with the real service: the reopened ViewModel takes up the track the service is recording | `Service`: `the service goes on recording after the ViewModel is cleared, a reopened ViewModel takes the recording up, and its Stop ends it` |
| The reopened Activity's repeated start says nothing (step 3) | the same test, and `Service`: `a repeated start for the track already being recorded says nothing and changes nothing` |
| Stop on the reopened screen ends the recording | the same `Service` test: the track row is ended and the service stops itself |
| The recording ended while the app was away: nothing is taken up, even with the row still open | `Swipe`: `the recording ended while the app was away - nothing is taken up, even with the track row still open` |
| Taken up at the next foreground if it could not be read at creation | `Swipe`: `a recording that could not be read when the screen was created is taken up when the app next comes to the foreground` |
| Restore from backup is refused after a take-up (the pre-build report's flag 4) | `Swipe`: `after the reopened screen takes up the recording, a restore from backup is refused as during any recording` |

### Path 3. Record pressed while another recording is running

| Step | Held by |
|---|---|
| Nothing starts, no track row is made, the owner's sentence is shown | `Swipe`: `Record pressed while another recording is running starts nothing and says so in the owner's words` |
| The same with the real service and database: one row, and the screen then takes up the running recording | `Service`: `Record on a reopened screen while the service is recording starts nothing, makes no second track, and says so` |
| A start for a different track that reaches the service anyway is still dropped with the warning, and the points still go to the first | `Service`: `a start for a different track while one is being recorded is dropped with a warning, and every point still goes to the first` |

### Path 2. A recording swiped away before a start marker was made

| Step | Held by |
|---|---|
| The marker is placed at the track's first recorded point | `Swipe`: `a recording taken up without a start marker gets one at its first recorded point, not at the fix after the reopen` |
| A good fix after the reopen does not move it and makes no second marker | the same test |
| The watch measures to the same start the screen shows | the same test |
| No recorded point yet: no marker until one exists, and then at that point; a fix in between makes none | `Swipe`: `a recording taken up with no recorded point yet gets its start marker when the first point is recorded, at that point` |
| A recording that is not taken up is untouched: its marker still comes from the first good fix | The existing `TrackRecordingViewModelTest` origin tests, unedited |

## What landed

| Commit | What |
|---|---|
| `0ce04e2a` | Tests first, failing, with only the plumbing they need to compile. |
| `335b101f` | The build, steps 1 to 5. |
| `cdcb5e4e` | `main` merged in (`RECORD.md` and the dispatch file only). |
| this commit | This report and its two index rows. |

**Under `app/src/main`, five files** (`git diff --stat 0f6485a4 HEAD`: 231 lines added, 17 removed):

- **`domain/ReturnWatch.kt`.** Its state gains `isBegun` and `mode`. `begin` takes the mode.
- **`service/TrackRecordingService.kt`.** Passes the mode when it begins the watch (`:132`). The warning for a dropped start now applies only to a start for a different track (`:103`).
- **`ui/track/TrackRecordingViewModel.kt`.** `takeUpRunningRecording` (`:412`), called at creation (`:242`), at the foreground (`:534`) and when Record is refused (`:301`). The refusal in `startRecording` (`:292`). `settleTakenUpOrigin` (`:473`). The first-good-fix marker is not made for a taken-up recording (`:771`). Two new required constructor arguments.
- **`MainActivity.kt`.** The factory passes the two arguments (`:206-207`), and the stale comment is corrected (`:331`). The start-and-stop effect is not changed.
- **`res/values/strings.xml`.** `track_recording_already_recording`, the owner's sentence.

No Room column, no migration, no database version. No composable changed. `resyncRecordingState` is unchanged; `onEnteredForeground` gained one call before it.

## How it works, in short

- **What it goes by.** The watch's state says whether the recording service has begun it. That is true only between the service starting and stopping a recording. A screen with no recording of its own, opened while that is true, takes the recording up. It never goes by an open track row.
- **What is taken up.** The track and mode from the watch. The start time and breadcrumbs from the row. The returning and off-track flags, copied from the watch as for any recording. The start marker, looked up through `GetTrackOriginWaypointUseCase`, which now has its first production caller. Then the screen's own poll and fix collection start, as they do after a Record tap.
- **A row that cannot be read takes up nothing,** and is logged. It is tried again at the next foreground.
- **The start marker rule.** For a taken-up recording, the marker is the one the track already has. If it has none, one is made at the first recorded point, on take-up or on the first poll that finds a point. The first-good-fix rule is switched off for a taken-up recording, which is what keeps the marker from landing where the phone is at the reopen.

## The ruling on my stop (option B)

Both new constructor arguments are required, with no default. `MainActivity`'s factory passes `container.getTrackOriginWaypointUseCase` and `getString(R.string.track_recording_already_recording)`.

**Every edited call site, each gaining the two arguments and nothing else:**

| File | The edit |
|---|---|
| `MainActivity.kt:206-207` | Two named arguments |
| `service/TrackRecordingServiceTest.kt` | The container's use case, and the sentence as a literal |
| `ui/track/TrackRecordingSundownTest.kt` | `GetTrackOriginWaypointUseCase` over its own fakes, and the sentence; one import |
| `ui/log/TrackDeleteTest.kt` | The same |
| `ui/log/JournalPendingDeleteTest.kt` | The same |
| `data/repository/NetworkFixExclusionPerConsumerTest.kt` | The same |
| `ui/track/TrackRecordingViewModelTest.kt` | The same, in its `viewModel()` fixture |

No assertion in those files changed.

## Existing tests changed, before and after

All of these are tests written in Parts 1 and 2 of this same dispatch.

| Test | Before | After | Why |
|---|---|---|---|
| `Watch`: every call to `begin` | `begin("track-1")` | `begin("track-1", MODE)` | Step 1: the service gives the mode |
| `Watch`: `an early return for one track is dropped when the service begins a different one` | Expected `ReturnWatchState(trackId = "track-2")` | The same, with `isBegun = true` and the mode | Step 1: the state now says it is begun |
| `Swipe`: `Return tapped before the service has begun the watch is not lost` | Expected `ReturnWatchState(trackId = "track-1", isReturning = true)` | The same, with `isBegun = true` and the mode | Step 1 |
| `Service`: `with no ViewModel in existence, …` | Expected `ReturnWatchState(trackId = trackId)` after the start | The same, with `isBegun = true` and `HIGH_ACCURACY` | Step 1 |
| `Swipe`: `PART 3 FAULT, still true - a ViewModel built while a recording runs reports not recording and not returning` | `isRecording` false, `isReturning` false, no active track, no start marker | **Replaced** by the main-path test: all four are now the running recording's | Step 2 |
| `Swipe`: `PART 3 FAULT, still true - pressing record on the reopened screen creates a second open track row, and its Return is refused` | Two open rows | **Replaced** by the path 3 test: one row, and the owner's sentence | Step 4 |
| `Service`: `PART 3 FAULT, still true - the service goes on recording…a reopened ViewModel reports not recording` | The reopened ViewModel's `isRecording` false | **Replaced:** it takes the recording up, and its Stop ends it | Step 2 |
| `Service`: `PART 3 FAULT, still true - a second recording started over the first is dropped by the service with a warning, so its row gets no points and is never ended` | The second row was made by a reopened ViewModel | **Replaced by two.** The app can no longer make that row, which is the path 3 test. The service's own behaviour for a start that names a different track (the warning, twenty points to the first track, none to the second) is kept as its own test, with the second row made by hand. | Step 4 |

Also, as the amendment asked: the unused `AlertKind` import and the unused constant were removed from `TrackRecordingViewModelTest`. The `Alert` import the Part 2 report also called unused is in fact used by that file's fake delivery, so it stays. That was my error in the Part 2 report.

## Tests first, and what each failed with

**`0ce04e2a`** holds them failing. The run was on the working tree that was then committed as `0ce04e2a` (`t1-tests-first`): 10 classes, 155 tests, 13 failures, 0 compile errors.

| Class | Tests | Failures |
|---|---|---|
| `ReturnWatchTest` | 21 | 2 |
| `TrackRecordingSwipeAwayTest` | 13 | 7 |
| `TrackRecordingServiceSwipeAwayTest` | 8 | 4 |
| `TrackRecordingViewModelTest` and the other six | | 0 |

| Test | Failed with |
|---|---|
| the watch says it is begun… | expected `isBegun=true, mode=BATTERY_SAVER`, was `isBegun=false, mode=null` |
| an early return…is dropped when the service begins a different one | the same two fields |
| Recording, swipe the app away, open Forager again… | `the record button reads this: it offers Stop` |
| a recording that could not be read…is taken up when the app next comes to the foreground | `the failed read is logged, naming the track: []` |
| after the reopened screen takes up the recording, a restore…is refused | `AssertionError` (the restore was allowed) |
| Record pressed while another recording is running… | expected the owner's sentence, was `null` |
| a recording taken up without a start marker… | `a start marker was expected at the first recorded point` |
| a recording taken up with no recorded point yet… | `AssertionError` (not recording) |
| Return tapped before the service has begun… | the two new state fields |
| the service goes on recording…a reopened ViewModel takes the recording up… | `the reopened screen took up the recording the service is making expected:<…> but was:<null>` |
| a repeated start for the track already being recorded says nothing… | expected no warning, was `Ignoring a start for track 'X': already recording track 'X'.` |
| with no ViewModel in existence… | the two new state fields |
| Record on a reopened screen while the service is recording… | expected the owner's sentence, was `null` |

Two new tests passed against the unbuilt code, and are shown connected by revert checks instead: `the recording ended while the app was away - nothing is taken up…` (nothing was ever taken up then), and `a start for a different track…` (that behaviour was Part 2's).

**After the build** (`t2-built-targeted`, the working tree that became `335b101f`): 12 classes, 206 tests, 0 failures.

## Revert checks

One edit each, from a saved copy and never from git. For every one the reverted build's log was read for compile errors first (0 each time), and after restoring, the working tree was confirmed identical to `335b101f`.

| Folder | The edit | What failed |
|---|---|---|
| `a1-never-begun` | The watch never says it is begun (step 1) | 9 of 34: the two `Watch` tests, and every take-up, refusal and marker test |
| `a2-no-takeup-at-init` | No take-up when the ViewModel is created (step 2) | 5: the main path with `the record button reads this: it offers Stop`, the restore test, both marker tests, the foreground test |
| `a3-no-takeup-at-foreground` | No take-up at the foreground (step 2) | 1: the foreground test, `expected:<track-1> but was:<null>` |
| `a4-warn-on-same-track` | The service warns for a repeated start again (step 3) | 2 of 8 in `Service`: both tests that send the repeated start |
| `a5-no-refusal` | The refusal removed (step 4) | 1: expected the owner's sentence, was `null` |
| `a6-marker-from-fix` | The first-good-fix rule left on for a taken-up recording (step 5) | 1: `the fix received after the reopen never makes the marker` |
| `a7-marker-last-point` | The marker made at the last recorded point (step 5) | 1: `expected:<45.0> but was:<45.001>` |

Each failure is one that edit could produce.

**Not shown by a revert:** "no trip-start warning on a take-up". The take-up never writes that field, so there is no line to revert. The main-path test asserts it against a first screen that did have the warning.

## Suite counts

From the JUnit XML, results directory cleared first.

| Run | Commit | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| Full unit suite (`t3-full-suite`) | `335b101f`, clean tree | 416 | 3420 | 0 | 0 | 24 |
| `assembleDebug` (`t4-assemble`) | `335b101f` | 0 `e:` lines, `BUILD SUCCESSFUL` |

- The 24 skipped are the same five existing classes as in Parts 1 and 2. Nothing was skipped, silenced or weakened.
- Both owner-held intermittent classes passed: `MushroomLogViewModelWiringTest` 2 of 2, `DiagnosticsPanelTest` 8 of 8. Nothing failed.
- The count reconciles with Part 2's 3411: plus 2 in `ReturnWatchTest` (19 to 21), 5 in `TrackRecordingSwipeAwayTest` (8 to 13) and 2 in `TrackRecordingServiceSwipeAwayTest` (6 to 8) is 3420.
- The merge of `main` after the run (`cdcb5e4e`) changed no file under `app/`, so the suite was not run again.

## What no headless test reaches

- **`MainActivity`'s start-and-stop effect on a reopened screen.** By reading (`MainActivity.kt`, the effect on `activeTrack`): when the take-up sets `activeTrack`, the effect sends the start again, which the service now ignores silently, and sets the flag that lets a later Stop send the stop. The tests send those two intents by hand. No test composes `MainActivity`.
- **The message on screen.** The sentence reaches `startRecordingErrorMessage`, the surface that already carries a failed start. That the toast shows is the existing composable's, not exercised here.
- **The real swipe and reopen on a phone with this build.** The release build on the S22 does not contain this change.
- **The HUD drawing itself.** The tests assert the two fields it reads.

## Reported, as the amendment asked

**The network-fixes notice can show a second time for the same recording.** Its "already shown" flag is the ViewModel's own and starts false in a new ViewModel (`TrackRecordingViewModel.kt`, `networkFixesNoticeShown`). After a take-up, the first poll that finds the track mostly network fixes raises it again. **Read, not run. Not designed around.**

**Another moment a take-up is needed, which I added:** when Record is refused. The sentence tells the user to stop the running recording, so the screen should then be showing it. Without this, a screen whose first take-up failed would show the sentence and still offer Record.

## Disclosures

### Confirmed versus inferred

**Observed:** every row of the tables above, from the JUnit XML.
**Read:** every file-and-line claim, at `335b101f`.
**Inferred:** what `MainActivity`'s effect does on a reopened screen.

### Could not determine

- Whether the reopened screen looks right on the S22. It needs this build on a phone.
- Whether a take-up ever meets a row it cannot read on a real device. The test forces it.

### Premises that were wrong

- **The amendment's steps 2 and 4 could not be built without `MainActivity`:** the ViewModel had neither the lookup nor a way to read a string. Stopped and asked; the planner allowed two factory arguments, both required.
- **Mine, in the Part 2 report:** I listed the `Alert` import in `TrackRecordingViewModelTest` as unused. It is used. Only `AlertKind` and the constant were unused, and only those were removed.
- **Mine, in my own order for Part 3:** I said the watch needed to carry the mode and "whether a service is recording". I did not say it also has to tell an early Return from a running recording. It does, and step 1's second test holds it.

### Decided beyond scope

Nothing was built beyond the amendment. Choices I made inside it:
- **Take-up also on a refused Record,** as above.
- **A taken-up recording's start marker is named with the time of its first recorded point,** not the time of the reopen. A marker made from the first good fix is still named with the time that fix arrived, as before. Without this the marker would read "Start · 11:02 PM" for a walk that began at 10:19.
- **"First recorded point" is the first of the track's points as the screen reads them,** the same point Return already falls back to. The read excludes fixes it classes as network fixes, so on a track whose very first stored fix was one of those, the marker is at the first point the map draws.
- **The take-up does not look at the row's end time.** The watch is the live source. A row marked ended while the watch is still begun would be a contradiction I have not seen and did not design for.
- **Seven revert checks.**

## Flags outside scope

1. **If location permission was taken away while the app was swiped away,** the reopened screen takes the recording up, and `MainActivity`'s effect then finds no permission, reports it and rolls the screen back, without sending a stop (its flag was never set). The service is left as it was. **By reading, not run.** It is the existing effect's behaviour meeting a new state; the effect is not mine to change.
2. **On a taken-up recording, Stop pressed before the next good fix makes no end marker,** because the last good fix is this ViewModel's own and starts empty. The same known gap as a stop from the notification.
3. **The pre-build report's flag 1 can no longer be made by reopening and pressing Record.** Tracks already stuck that way, and tracks left open by a killed process, are Part 3b.
4. **The pre-build report's flag 2** (what three readings spans) is unchanged; plan task T21.

## For the owner afterwards (device-only)

As the amendment lists them; not run by me. On a test build:
1. **Record, tap Return, swipe Forager away, open it again.** The screen shows Stop, the breadcrumbs and the return HUD. Stop on the screen ends the recording, and the notification goes.
2. **The same, swiped away within a few seconds of Record,** before a start marker appears. On reopening, the start marker is at the beginning of the line, not where the phone is.
3. **Record cannot start a second recording.** If Record is ever offered while one is running, pressing it shows the sentence and starts nothing.

# Part 3b: step 6

Not started. Appended here at the second hand-back.

# Part 3a, follow-up, 2026-10-02 (UTC): the window after Stop

Appended after the planner's review of Part 3a. Nothing above this heading is changed; where this corrects it, this is the later word. Evidence in the same folder, `part3a/`.

## The fault, found by the planner by reading, and confirmed by a run

`stopRecording` clears the screen's recording at once. The service is stopped a moment later, when `MainActivity`'s effect has sent the stop and the service has handled it, and only then does the service end the watch. In between, the watch is still begun for the track the screen has just stopped, and the screen has no active track. That is exactly the state Part 3a's refusal and take-up act on.

**What the build at `186bef5a` did, observed** (`t5-stop-window-tests-first`, 16 tests, 2 failures):

| Entry | Expected | What happened |
|---|---|---|
| `stopRecording()`, then `startRecording()` with the watch still begun for that track | A new recording on a new track, nothing refused, as before Part 3a | The stopped recording was taken back up: `expected:<track-[2]> but was:<track-[1]>` |
| `stopRecording()`, then `onEnteredForeground()` in the same window | The screen stays stopped | It took the stopped recording back up: `the screen stays stopped` failed |

So the mechanism is as the planner described it, with one detail different from the description. Record in that window was refused and the sentence was set, but the take-up that the refusal starts then cleared the sentence again in the same pass. In the test the user-facing sentence was `null` by the time it was read. On a phone it may show briefly or not at all; I could not determine which. Either way the outcome was wrong: the screen went back to recording the track it had just stopped.

**What would have followed on a phone, by reading and not run:** with the recording back on screen, the effect sends a start for that track. The service handles the earlier stop first, ends the row, then handles the start with nothing recording and begins again into a row that already has an end time.

## The fix

`ui/track/TrackRecordingViewModel.kt`, and nothing else. The ViewModel remembers the track it has itself stopped. While the watch is still begun for that track, the Record refusal does not apply to it and the take-up leaves it alone. The memory is dropped as soon as the watch is seen to have moved on: ended, or begun for another track. So every other running recording is still refused against and still taken up.

This is the planner's "smallest" as proposed. I did not find a smaller or safer one. Nothing in `MainActivity`'s effect or the service changed.

## Tests

In `TrackRecordingSwipeAwayTest`, through `stopRecording`, `startRecording` and `onEnteredForeground`:

| Test | Holds |
|---|---|
| `Stop then Record at once, before the service has handled the stop, starts a new recording and refuses nothing` | A new track; no sentence; nothing logged |
| `Stop, then the app comes to the foreground before the service has handled the stop - the stopped recording is not taken back up` | The screen stays stopped and reads no fixes |
| `after its own stopped recording has ended, Record is still refused while a different recording is running` | The memory is only of the screen's own stop: a different running recording still gets the owner's sentence |

## What landed

| Commit | What |
|---|---|
| `301d56a4` | The three tests, pushed failing: 2 of 16 fail as in the table above. The third passed then and passes now; it guards the fix from being too wide. |
| `f19d2d53` | The fix. |
| this commit | This section. |

## Evidence

| Run | Commit | Result |
|---|---|---|
| Affected classes (`t6-stop-window-built`) | the tree that became `f19d2d53` | 12 classes, 209 tests, 0 failures |
| Revert check `a8-forget-own-stop`: the one line that remembers the stopped track removed | `f19d2d53`, restored from a saved copy, 0 compile errors in the reverted build, tree confirmed identical afterwards | 2 of 16 fail, the same two tests with the same two messages as before the fix |
| Full unit suite (`t7-full-suite-after-follow-up`) | `f19d2d53`, clean tree | 416 classes, 3423 tests, 0 failures, 0 errors, 24 skipped |
| `assembleDebug` (`t8-assemble`) | `f19d2d53` | 0 `e:` lines |

3420 plus the 3 new tests is 3423. Both owner-held intermittent classes passed. The 24 skipped are the same five classes.

## Disclosures for the follow-up

**Confirmed by observation:** the two failures before the fix, the revert check, and the counts.

**Inferred, not run:** what the service would have done on a phone after the stopped recording was taken back up.

**Could not determine:** whether the sentence was visible on a phone during that window.

**What the fix rests on:** the stop reaching the service. If it never did, this same screen would go on leaving that recording alone until the watch changed. A new screen, after a swipe-away, has no such memory and takes it up. Not tested; I know of no route by which the stop is not sent after a Stop on a screen that started or took up the recording.

**Not closed by this, and unchanged from the Part 2 report's flag 2:** after Stop then Record at once, the new recording's Return and start point are refused by the watch until the service has ended the old track and begun the new one. A few milliseconds by my reading; the start point is handed again at the next poll.

**Premises that were wrong:** mine. Part 3a's tests never stopped a recording while the watch was still begun and then acted on the same screen, so the window was never exercised. The check never saw the state that could fail it.

**Decided beyond scope:** nothing.

## On the flag about location permission, the planner's reading added

The flag above ("if location permission was taken away while the app was swiped away…") stands as written. The planner's reading, which I agree with as likely: Android kills an app's process when a runtime permission is revoked, so the service would not be running and the watch would not be begun, and the state the flag describes would not arise. **Inferred by both of us, not run.**
