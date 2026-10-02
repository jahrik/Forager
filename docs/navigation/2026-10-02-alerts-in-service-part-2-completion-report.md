# Completion report: the off-track alert moves into the recording service (dispatch 2026-09-28-400, Part 2)

**Status: built and pushed on `alerts-in-service`, not merged. The planner merges. Not run on a phone.**

**Date:** 2026-10-02 (UTC).
**Dispatch:** `prompts/preserved/2026-10-02-01.md`, Amendment 2 (continuation 2026-09-28-403), and its "rulings on the coder's questions" (continuation 2026-09-28-404), read on branch `records-after-147` at `ea4d3d2f`. Plan task T1.
**Base:** `origin/alerts-in-service` at `11d23217` and `origin/main` at `9c3f103d` when work began, both checked against the remote. `main` moved to `f5103d0d` during the work; it was merged in at `943f7892` and changed nothing under `app/`.
**The owner's go for the build,** to this session directly: "Yes, build Part 2". Their decisions, as quoted in the amendment: "Option B", and for the off-track check "Move as is, adjust as necessary."
**The pre-build report this follows:** [`2026-10-02-alerts-in-service-prebuild-report.md`](2026-10-02-alerts-in-service-prebuild-report.md).

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`, test paths to `app/src/test/java/com/zynergylabs/forager/app/`. Every figure below was read from a file in `~/Zynergy/device-evidence/2026-10-02-alerts-in-service/part2/`; the folder for each is named beside it.

## What changed for the user

Recording, returning, and the app swiped away from recents: the off-track alert is now still decided and still delivered, for as long as the recording runs. Before, it stopped for good at the swipe.

What "off track" means is unchanged: three readings, 25 m, a 120 s cooldown, and it does not override a silenced phone. By the pre-build report's flag 2, steady walking alone may not trip that check. That is plan task T21, and was not touched.

Nothing else on screen changes. A reopened app still does not show the recording it left. That is Part 3.

## What landed

| Commit | What |
|---|---|
| `7ae7266a` | Tests first: `ReturnWatchTest` against a class that decides nothing. Failing. |
| `1adfe2cb` | Tests first: the service test and the ViewModel handover tests, with only the plumbing they need to compile. Failing. |
| `c57ee30f` | The build: steps 1, 2 and 3 together. |
| `943f7892` | `main` merged in (`RECORD.md`, the plan and the dispatch file only). |
| this commit | This report and its two index rows. |

**Under `app/src/main`, seven files** (`git diff --stat 11d23217 HEAD`: 400 lines added, 100 removed):

- **`domain/ReturnWatch.kt`, new.** The returning flag, the start point, the window, the cooldown and the one `AlertDelivery.deliver` call. Plain Kotlin, no Android import. It uses `ComputeReturnToStartUseCase` and `DetectOffTrackUseCase` unchanged.
- **`AppContainer.kt`.** Holds the one `ReturnWatch` (`:329`).
- **`service/TrackRecordingService.kt`.** Begins the watch when a recording starts (`:129`). Hands it every raw fix, in one call placed before the sampler (`:139`). Ends it in `stopRecording` (`:169`) and in `onDestroy` (`:118`). Logs a start that is dropped because a recording is running (`:108`, ruling 8). The sampler and the batching are not changed, reordered or wrapped.
- **`ui/track/TrackRecordingViewModel.kt`.** Its decision, window and cooldown are removed. `startReturn` and `stopReturn` call the watch (`:464`, `:486`). `isReturning` and `isOffTrack` are copies of the watch's state (`:217`). It hands the watch the start point whenever that changes (`:233`). Its constructor loses `alertDelivery` and `detectOffTrack` and gains `returnWatch`.
- **`ui/track/TrackRecordingUiState.kt`.** Comments only: the two fields are now described as copies.
- **`domain/AlertDelivery.kt`.** Comment only: the header's "hole" is marked closed for off-track, with the original two paragraphs left as written.
- **`MainActivity.kt`.** The ViewModel factory's arguments, and nothing else (`:191-205`).

Nothing else under `ui/` changed. No composable, no HUD, no notification, no `resyncRecordingState`.

## The planner's rulings, one by one

### Rulings 1 and 2. One call beside the sampler; every raw fix

`TrackRecordingService.kt:139` is the one added line inside the collector. It sits after the fix becomes a point and before the sampler is asked. `TrackRecordingServiceTest` passes unedited apart from the constructor argument (3 tests, 0 failures, `t4-full-suite`).

### Ruling 3. The service begins it; an early Return is kept

`ReturnWatch.startReturn` and `setStartPoint` are accepted for a track before the service has begun, and kept if the service then begins that same track. Held by:
- `ReturnWatchTest`: `a return asked for before the service has begun the watch is kept for that track`, and `an early return for one track is dropped when the service begins a different one`.
- `TrackRecordingSwipeAwayTest`: `Return tapped before the service has begun the watch is not lost`, through the ViewModel's own `startRecording` and `startReturn`.

### Ruling 4. The eight off-track tests, moved one for one

From `ui/track/TrackRecordingViewModelTest.kt` (lines at `11d23217`) to `domain/ReturnWatchTest.kt`. **Every name is unchanged and every assertion is unchanged.** What changed is how the readings arrive: `vm.returnToStart(point)` became `watch.onFix(point)`, and "start a recording, add a breadcrumb at 45.0, poll" became "begin the watch, give it the start at 45.0". The points, the clock values and the expected values are the same.

| Old line | New line | Name | Asserted, before and after |
|---|---|---|---|
| `:433` | `:53` | moving steadily away from the start while returning sets off-track | `isOffTrack` is true |
| `:463` | `:71` | going off-track delivers exactly one alert, not one per fix | 0 deliveries before; `isOffTrack` true; 1 delivery; it equals `Alert(OFF_TRACK, overridesSilence = false)`; still 1 after a fourth fix |
| `:494` | `:93` | a sustained drift alerts again once the cooldown elapses | 1 delivery; still 1 at the cooldown less 1 ms; 2 at the cooldown |
| `:525` | `:114` | staying on track never delivers an alert | 0 deliveries |
| `:545` | `:126` | stopReturn resets the cooldown so a later return attempt can alert immediately | 1 delivery; then 2 after stop-return and a new return at the same clock instant |
| `:573` | `:146` | moving steadily toward the start while returning stays on track | `isOffTrack` is false |
| `:593` | `:158` | moving away from the start does not set off-track unless actively returning | `isOffTrack` is false |
| `:613` | `:170` | stopping the recording clears returning and off-track state | `isOffTrack` true first; then `isReturning` false and `isOffTrack` false |

Two things to read against that table:
- In the last row the act that "stops the recording" was `vm.stopRecording()` and is now `watch.end("track-1")`, which is what the service calls when a recording stops. The assertions did not change.
- `isOffTrack` and `isReturning` were read from the ViewModel's state and are now read from the watch's. That the ViewModel's state follows the watch's is held separately, in `TrackRecordingSwipeAwayTest`.

No assertion had to change, so nothing here was a stop.

### Ruling 5, as widened by the ruling on question 2. The constructor call sites

Each call site loses `detectOffTrack` and `alertDelivery` and gains `returnWatch`. No assertion in these files changed.

| File | The edit |
|---|---|
| `MainActivity.kt:191-205` | Two positional arguments removed, `container.returnWatch` added |
| `service/TrackRecordingServiceTest.kt` | The same three, positional |
| `ui/track/TrackRecordingSundownTest.kt` | Two named arguments removed; `returnWatch = ReturnWatch(…, AlertDelivery { })` added; one import |
| `ui/log/TrackDeleteTest.kt` | The same |
| `ui/log/JournalPendingDeleteTest.kt` | The same |
| `data/repository/NetworkFixExclusionPerConsumerTest.kt` | The same |
| `ui/track/TrackRecordingViewModelTest.kt` | The same, in its `viewModel()` fixture; the eight tests of ruling 4 removed, with a four-line note where they stood |

The planner's condition was checked before editing: no test in these six files, outside the eight that moved, counted deliveries on a fake `AlertDelivery`.

Left behind in `TrackRecordingViewModelTest.kt`, unused now and not removed, because removing them is an edit beyond the ruling: the imports of `Alert` and `AlertKind`, and the constant `OFF_TRACK_ALERT_COOLDOWN_MILLIS`. They compile with a warning at most.

### Ruling 6, as corrected by the planner. The two Part 1 fault classes

Renamed, since they no longer only describe a fault: `TrackRecordingSwipeAwayFaultTest` is now `TrackRecordingSwipeAwayTest`, and `TrackRecordingServiceSwipeAwayFaultTest` is now `TrackRecordingServiceSwipeAwayTest`.

| Part 1 test | Now |
|---|---|
| `control - with the screen's ViewModel alive, …delivers exactly one off-track alert` | **Kept.** |
| `FAULT today - with a return under way and the ViewModel cleared, readings that would alert deliver nothing` | **Turned.** `with a return under way and the ViewModel cleared, walking away from the start still delivers the off-track alert`. The assertion that turned is the last: an empty list then, one off-track alert now. |
| `FAULT today - a ViewModel built while a track is still open reports not recording…` | **Still a pin, Part 3's.** `PART 3 FAULT, still true - a ViewModel built while a recording runs reports not recording and not returning`. |
| `FAULT today - pressing record on the reopened screen creates a second open track row…` | **Still a pin, Part 3's,** with one new fact recorded (below). |
| `FAULT today - the service goes on recording after the ViewModel is cleared, and a reopened ViewModel reports not recording` | **Still a pin, Part 3's.** |
| `FAULT today - a second recording started over the first is ignored by the service…` | **Still a pin, Part 3's,** and it now asserts ruling 8's log line. |
| `today - a null intent…starts nothing` | **Kept,** and it now also asserts the watch was not begun. |

New in those two classes: four ViewModel tests for the handover, and three service tests (two that deliver with the ViewModel gone, one that shows the same walk posts nothing when no return is under way).

### Ruling 7. Thread-safety

**What makes it safe.** Everything `ReturnWatch` holds is read and written inside one lock (`synchronized`, a single private monitor). That includes the check "has the cooldown passed" and the write that closes it, so two fixes arriving together cannot both decide to alert. The state the screen reads is published as a `StateFlow`, from inside the lock, so states appear in the order the changes happened. `AlertDelivery.deliver` is called after the lock is released, so posting a notification never holds the lock.

**The two callers** are the service's collector, on `Dispatchers.Default`, and the ViewModel, on the main thread.

**The tests that exercise two callers at once,** both in `ReturnWatchTest`, on real threads:
- `two threads feeding the same walk away deliver one alert, not two`: two threads released together, 300 rounds.
- `toggling the return from one thread while another feeds fixes never throws`: 20,000 calls on each of two threads.

**Shown to bite:** with the lock removed from `onFix` (revert check `r2-no-lock-onfix`) both tests failed, each with an exception thrown from the corrupted list. They did not fail on a count of two alerts; the list broke first.

**What no test reaches:** the real pairing of those two threads on a phone. The tests use two plain threads, not the service's dispatcher and the main looper.

### Ruling 8. The dropped second start is logged

`TrackRecordingService.kt:108`: `Ignoring a start for track '<new>': already recording track '<current>'.` Asserted, with both ids, in `TrackRecordingServiceSwipeAwayTest`. Revert check `r4-no-log` fails on that assertion.

A start with no track id at all is still dropped without a line. The ruling named the second start only, so that was left.

### Ruling 9. It keeps only the readings it reads

`ReturnWatch` keeps three readings. `ReturnWatchTest`, `keeping only the readings the check reads decides exactly as keeping every reading did`, runs the old arithmetic beside the new: the real `DetectOffTrackUseCase` over a list that is never trimmed, with the same cooldown, against the watch. 200 random walks of 400 readings each, the clock moving so the cooldown opens and closes. "Off track now" and the delivery count are compared after every reading, and each walk must go off track at least once, so no walk compares nothing.

The window size is `DetectOffTrackUseCase`'s private constant. That class was not mine to change, so the number 3 is repeated in `ReturnWatch`. Revert check `r6-keep-two` (keep two readings) fails that test at walk 0, reading 23, and eight others.

## The two design points the planner accepted, and what holds each

1. **The ViewModel hands the start point to the watch whenever it changes; the watch keeps the last one.** The origin waypoint once it exists, the first breadcrumb before that: the same rule the button's distance follows. Nothing is read from storage by the service. Held by `TrackRecordingSwipeAwayTest`, `the watch measures to the first breadcrumb until the origin waypoint exists, then to the origin`. Revert check `r5-no-origin-hand` fails it: 222.4 m where 111.2 m was expected.
2. **The ViewModel copies the watch's state only when the watch is for its own active track.** So a reopened screen still shows no return until Part 3. Held by the first `PART 3 FAULT` test in `TrackRecordingSwipeAwayTest`, where the watch is returning for the first track and the reopened screen shows nothing. Revert check `r3-copy-any-track` fails it.

## The same sum in two places (the ruling on question 1)

The distance on the Return button is still worked out by the ViewModel from its own fixes. The watch works out its own distance for the decision. Both use `ComputeReturnToStartUseCase` and the same start point.

**Can the two disagree in a way a user could see?** By one fix at most. The button's distance comes from the last fix the ViewModel's listener received, and "off track now" from the last three the service's listener received. Both listeners are registered with the platform for the same providers at the same interval. Nothing on screen compares the two: one is the button's spoken description, the other is its colour. A user could not see a disagreement unless the two listeners were delivered different fixes for a long stretch, which I have no evidence of either way. **Inferred, not observed.**

## Tests first, and what each failed with

**`7ae7266a`** holds `ReturnWatchTest` against a class with the right shape that decided nothing. Run alone (`t1-watch-skeleton`): 19 tests, 12 failures, 0 compile errors. The seven that passed all assert that nothing happens. The eight moved tests among the failures:

| Test | Failed with |
|---|---|
| moving steadily away…sets off-track | `AssertionError` (not off track) |
| going off-track delivers exactly one alert… | `AssertionError` (not off track) |
| a sustained drift alerts again… | `expected:<1> but was:<0>` |
| stopReturn resets the cooldown… | `expected:<1> but was:<0>` |
| stopping the recording clears… | `AssertionError` (its precondition, off track, was not reached) |

The other three of the eight (`staying on track never delivers`, `moving steadily toward the start…stays on track`, `moving away…unless actively returning`) assert an absence and passed against the empty class. They are shown to be connected by revert check `r6-keep-two` and by the side-by-side test, not by this run.

**`1adfe2cb`** holds the service tests and the handover tests. The run was made on the working tree that was then committed as `1adfe2cb` (`t2-all-tests-first`): 5 classes, 75 tests, 23 failures, 0 compile errors.

| Class | Tests | Failures |
|---|---|---|
| `ReturnWatchTest` | 19 | 12 (as above) |
| `TrackRecordingServiceSwipeAwayTest` | 6 | 4 |
| `TrackRecordingSwipeAwayTest` | 8 | 7 |
| `TrackRecordingViewModelTest` (unedited apart from the argument, the eight still in it) | 39 | 0 |
| `TrackRecordingServiceTest` | 3 | 0 |

- **The service test with no ViewModel** failed with `the service began the watch for its track expected:<ReturnWatchState(trackId=…)> but was:<ReturnWatchState(trackId=null…)>`. It failed on its first claim, that the service begins the watch, and never reached the notification. The notification message is shown by the revert check below.
- **The service test with the ViewModel cleared** failed with `expected the off-track notification: the return was started on the screen, the screen is gone, and the walker moved away`.
- **The handover test** failed with `the watch is returning the moment the screen's callback returns`.
- **The dropped-start test** failed on `the dropped start is logged…`.

**After the build** (`t3-built-targeted`, the working tree that became `c57ee30f`): the ten affected classes, 146 tests, 0 failures.

## Revert checks

One edit each, from a saved copy and never from git. For every one: the reverted build's log was read for compile errors before any result (0 each time), and after restoring, the working tree was confirmed identical to `c57ee30f`. The runner is `part2/revert.sh`; each folder holds the saved copy, the edit as a diff, the build log and the XML.

| Folder | The edit | What failed, and with what |
|---|---|---|
| `r1-no-feed` | **The one named by the amendment:** the feed call removed from the service (`TrackRecordingService.kt:139`) | 3 of 6 in `TrackRecordingServiceSwipeAwayTest`. `expected the off-track notification after three readings moving away from the start, with no ViewModel alive`; `expected the off-track notification: the return was started on the screen, the screen is gone, and the walker moved away`; and, in the no-return test, `the watch measured them`. |
| `r2-no-lock-onfix` | The lock removed from `onFix` | 2 of 19 in `ReturnWatchTest`: both two-caller tests, each on an exception thrown by a feeder thread |
| `r3-copy-any-track` | The ViewModel copies the watch's state whatever track it is for | 2 of 8 in `TrackRecordingSwipeAwayTest`: both `PART 3 FAULT` tests |
| `r4-no-log` | The log line removed | 1 of 6: `the dropped start is logged…` |
| `r5-no-origin-hand` | The origin waypoint not handed to the watch | 1 of 8: `to the origin at 45.001 expected:<111.2> but was:<222.39…>` |
| `r6-keep-two` | Two readings kept instead of three | 9 of 19 in `ReturnWatchTest`, among them the side-by-side test at `walk 0, reading 23` |

Each failure is one that edit could produce, and none belongs to a different edit.

## Suite counts

From the JUnit XML, results directory cleared first.

| Run | Commit | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| Full unit suite (`t4-full-suite`) | `c57ee30f`, clean tree | 416 | 3411 | 0 | 0 | 24 |
| `assembleDebug` (`t5-assemble`) | `c57ee30f` | 0 `e:` lines, `BUILD SUCCESSFUL` |

- The 24 skipped are the same five existing classes as in Part 1: `AvailabilityScreenMapIconStackTest` 19, `AvailabilityScreenTripPlanningFlowTest` 2, `AvailabilityScreenOfflineCacheTest` 1, `AvailabilityScreenWaypointFlowTest` 1, `GenerateFungiIndexDbAsset` 1. Nothing was skipped, silenced or weakened.
- Both owner-held intermittent classes passed: `MushroomLogViewModelWiringTest` 2 of 2, `DiagnosticsPanelTest` 8 of 8. Nothing failed.
- The count reconciles with Part 1's 3393: less the 7 Part 1 tests, less the 8 moved out of `TrackRecordingViewModelTest`, plus 19 in `ReturnWatchTest`, 8 in `TrackRecordingSwipeAwayTest` and 6 in `TrackRecordingServiceSwipeAwayTest`, is 3411.
- Every XML timestamp in the full run falls between 07:29:13Z and 07:33:21Z, inside the run.
- The suite was run at `c57ee30f`. The merge of `main` after it (`943f7892`) changed no file under `app/` (`git diff --stat c57ee30f 943f7892 -- app/` is empty), so the suite was not run again.

## What no headless test reaches

- **The swipe from recents itself.** Stood in for by clearing the ViewModel's store. The phone check in the pre-build report showed the process, the service and the notification surviving it on the S22, on the release build 2426, which does not contain this change.
- **The alert on a phone with this build.** No test here puts this code on a phone.
- **`AndroidAlertDelivery.deliver` called from a background thread on a real device.** It used to be called on the main thread, from the ViewModel. It is now called on the service's collector thread. Posting a notification and vibrating are calls the platform accepts from any thread, and they worked under Robolectric at SDK 36. **Inferred for a real device, not observed.**
- **`MainActivity`'s effect** that starts and stops the service. No test composes `MainActivity`.
- **The two real threads,** as under ruling 7.
- **Whether fixes still reach the service's listener with the app swiped away, the screen off and the phone unplugged.** If they stop, nothing feeds the watch.

## Disclosures

### Confirmed versus inferred

**Observed** (tests run, results read from the XML): everything in the tables above.

**Read:** every file-and-line claim, at `c57ee30f`.

**Inferred:** the three points marked so above (the two distances, the background-thread delivery, the real threads).

### Could not determine

- Whether the alert arrives on the S22 with the app swiped away. It needs this build on a phone, and the phone carries the release build.
- Whether steady walking trips the check at all (the pre-build report's flag 2). Not this part's question, but it limits what a device check can show.

### Premises that were wrong

- **Amendment 2's step 3** said the distance to the start becomes a copy of the watch's state. Five existing tests outside the eight need the ViewModel to work it out itself. Raised with the planner before building; the planner withdrew that sentence (ruling on question 1).
- **Ruling 6** said each fault test becomes its fixed counterpart. The reopen tests have no fixed behaviour until Part 3. Raised; corrected by the planner.
- **Mine:** my pre-build report's test plan said the service test would fail first "with no notification". Against the empty class it failed one line earlier, on the watch not being begun. The notification message is shown by revert check `r1-no-feed` instead.

### Decided beyond scope

Nothing was built beyond the amendment. Choices I made inside it, each said above or here:
- **The names:** `ReturnWatch`, `ReturnWatchState`, and the two renamed test classes.
- **A reopened screen's Return on a second track is now refused and logged,** where before the move it would have marked itself returning. This follows from the watch belonging to one track. It is in Part 3's territory and I did not design around it; it is pinned in `TrackRecordingSwipeAwayTest` so Part 3 finds it.
- **`ReturnWatchState.returnToStart` exists and nothing under `ui/` reads it.** The decision cannot be tested without the measurement it was made on. It is a field with a reader only in tests.
- **Five revert checks beyond the one the amendment named.**
- **Stale comments I updated** in the files I was changing (`AlertDelivery.kt`, `TrackRecordingUiState.kt`, three places in `TrackRecordingViewModel.kt`), each marked with the dispatch, with the original reasoning left in place where it was a record.

## Flags outside scope

1. **A comment in `MainActivity.kt` is now out of date** (`:322-329`): it says the off-track alert is fed from the ViewModel's collector. It is not, since this change. The amendment allows no edit to `MainActivity` beyond the one argument, so it was left. One sentence, for whoever next edits that file.
2. **If Record is pressed again within the instant before the service has handled the previous stop,** the watch is still begun for the old track and refuses the new track's start point and Return until the service ends the old one and begins the new. Both intents are handled on the main thread in order, so the gap should be a few milliseconds. **Inferred, not measured.** The start point is handed again at the next poll, within 15 s, and when the origin waypoint is created.
3. **The pre-build report's flags 1, 2, 4 and 5 are unchanged by this part:** the stuck second track, what three readings spans, Restore after a reopen, and a recording with no origin waypoint.

## For the owner afterwards (device-only)

As the amendment lists them; not run by me. How this build reaches the S22 is the owner's: the phone carries release build 2426 and a debug build will not install over it.

1. **The alert with the app open and the screen off still arrives.** This is the path that worked before and must not regress. It now runs through different code.
2. **The alert with the app swiped away.** Whatever makes it arrive in step 1 should make it arrive here. Because of flag 2 in the pre-build report, a plain walk may not trip either.
3. **Stop from the notification** ends the recording (expand the notification first; the Stop action is behind its arrow on this phone), and the record button agrees when the app is next looked at.
4. **`adb logcat -s TrackRecordingService`** during a reopen-and-Record shows the new warning line, if the owner chooses to make the stuck track of flag 1.
