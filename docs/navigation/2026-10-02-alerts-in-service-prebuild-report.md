# Pre-build report: alerts move into the recording service (dispatch 2026-09-28-400, Part 1)

**Status: a report. Nothing is built. Nothing under `app/src/main` was changed.**

**Date:** 2026-10-02 (UTC).
**Dispatch:** `prompts/preserved/2026-10-02-01.md`, Part 1 only. Plan task T1 of [`2026-10-01-navigator-completion-plan.md`](2026-10-01-navigator-completion-plan.md).
**Base:** `origin/main` at `9c3f103d`, verified against the remote before reading. The dispatch was written against `c1610518`; `git diff --stat c1610518 9c3f103d -- app/` is empty, so every line number the dispatch gives still points at the same code.
**Branch:** `alerts-in-service`.
**What this dispatch added:** two test classes, this report, and one row in each of two indexes.

App paths below are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Test paths are relative to `app/src/test/java/com/zynergylabs/forager/app/`. Each claim is marked **read** (the file was read at the base), **observed** (a test ran and its result was read from the JUnit XML), or **inferred**.

## The short version

1. **All six of the planner's premises are confirmed.** None is wrong. Two are slightly wider than stated, and that is recorded under each.
2. **Premise 4, the one to settle first, is confirmed by a run.** After a swipe-away, pressing Record makes a second track. The service ignores it. In the test, twenty fixes went to the first track and none to the second.
3. **Four things the premises did not say,** found while tracing them. They are under "Flags outside scope" and none is acted on:
   - The second track from premise 4 is never ended. Records would show it as "Still recording" for good, and the app refuses to delete a track that is still recording.
   - As wired today, the off-track check reads three fixes one second apart. A walker would have to cover more than 25 m in two seconds to trip it by walking. This matters for the move, because which stream of fixes feeds the check decides what "three readings" means.
   - The service drops the second start without a log line.
   - After a reopen, Restore from backup is allowed while the service is still writing track points.
4. **The shape I would build** is the one `AlertDelivery.kt`'s header names, with one thing made explicit that the header leaves open: how the screen reads the state back. Details under "Shapes".

## 1. The six premises

### Premise 1. The off-track decision and all its state live in the screen's ViewModel. **Confirmed (read, and observed).**

- The decision and the one `deliver` call: `ui/track/TrackRecordingViewModel.kt:823-834`. The call itself is `:833`. **Read.**
- The rolling window `:177`. The cooldown `:182`, checked at `:850-853`. **Read.**
- The returning flag is in the screen's state (`ui/track/TrackRecordingUiState.kt:43`), set by `startReturn` (`TrackRecordingViewModel.kt:425-434`) and cleared by `stopReturn` (`:437-441`). **Read.**
- The readings come from the ViewModel's own collection of the fix stream (`:526-550`), launched in `viewModelScope`. **Read.**
- **Observed:** `ui/track/TrackRecordingSwipeAwayFaultTest`. With the ViewModel alive, a set of readings delivers exactly one off-track alert. The same readings after the ViewModel is cleared deliver none, and the fix stream has no collector left.

Two places where the premise is wider than the code:
- "All of it is cleared with the Activity (`onCleared` `:855`)". `onCleared` (`:855-874`) cancels the poll and the fix collection (`:856-857`) and commits pending deletes. It does not reset the window, the cooldown or the flag. They are abandoned with the object, not cleared. The effect on the user is the same.
- The start point is not only ViewModel state. The origin waypoint is saved, and the track row points at it (`:567`, `domain/model/Track.kt:43`). What is lost on a swipe-away is the ViewModel's copy of it (`TrackRecordingUiState.kt:62`), not the waypoint.

### Premise 2. The service knows a track id and a mode and nothing else. **Confirmed (read).**

`service/TrackRecordingService.kt`, all 288 lines read. Its fields are a scope, the recording job, a buffer and its lock, and the current track id (`:58-67`). The mode is a local inside `startRecording` (`:106`, `:111`). It collects its own fixes (`:116`), samples them (`:120`) and batches them (`:122-126`, `:133-138`). It has no start point and no returning flag. Its imports (`:3-36`) name neither `AlertDelivery` nor `DetectOffTrackUseCase`.

One thing worth knowing for the shapes: the service already reaches everything it uses through `(application as ForagerApplication).container` (`:110`, `:148`), and `alertDelivery` is built there at process start (`AppContainer.kt:322`). So an alert can be delivered from the service with no new plumbing. `AppContainer.kt:297-299` says there is no separate graph for the service.

### Premise 3. After a swipe-away the service records, and a reopened app does not know. **Confirmed as far as a headless test reaches (read, and observed). The swipe itself is the phone's.**

- `activeTrack` is in-memory (`TrackRecordingViewModel.kt:72-78`). `init` loads waypoints and tracks and nothing else (`:200-203`). `resyncRecordingState` returns at once when `activeTrack` is null (`:403`). **Read.**
- Nothing in `MainActivity` asks whether the service is running. There is no bind and no running-service query anywhere in `app/src/main` (searched). `hasStartedRecordingOnce` is a `remember` (`MainActivity.kt:410`), so a new Activity starts with it false and sends nothing. **Read.**
- The manifest sets no `stopWithTask` on the service (`AndroidManifest.xml:174-177`), and the service does not override `onTaskRemoved` (searched). **Read.** By the platform's documented default the service then carries on when the task is removed. **Inferred; not run on a phone.**
- **Observed:** `service/TrackRecordingServiceSwipeAwayFaultTest`, first test. The real service and a real ViewModel over the real container: two fix listeners while the screen is alive, one after the ViewModel is cleared, the service not stopped, the track row still open. A second ViewModel, built and sent `onEnteredForeground()`, reports `isRecording` false.

**The step no test reaches:** the swipe from recents. It is stood in for by clearing the ViewModel's store with the service left running. Whether the phone really destroys the Activity, keeps the process and leaves the service alone is device-only. See "For the owner afterwards".

### Premise 4. A second recording could then be started over the first. **Confirmed (read, and observed).**

- The record button calls `startRecording` whenever `isRecording` is false (`MainActivity.kt:580-603`). `startRecording` creates a new track row and does not look for an open one (`TrackRecordingViewModel.kt:211-242`, `domain/StartTrackUseCase.kt`). **Read.**
- The new `activeTrack` makes `MainActivity` send `ACTION_START` (`MainActivity.kt:411-427`). The service handles it only when `recordingJob == null` (`TrackRecordingService.kt:82`). Otherwise the `if` falls through with no `else`. **Read.**
- The points keep going to the first track because the collector closed over the first `trackId` (`:106`, `:126`, `:136`), and a stop ends `currentTrackId`, which is still the first (`:143`, `:151`). **Read.**
- **Observed:** the second test in `TrackRecordingServiceSwipeAwayFaultTest`. Twenty fixes through the platform's own listener path: the first track holds twenty points, the second holds none. Then the screen's stop and `ACTION_STOP`: the first track is ended, the second is not.

**The step no test reaches:** `MainActivity`'s `LaunchedEffect` (`:411-438`), which turns the ViewModel's state into the two intents. No test in this repository composes `MainActivity`. The test builds the same intents from the same three constants, as `TrackRecordingServiceTest.startIntent` already does.

### Premise 5. The two sundown alerts have no caller, on purpose. **Confirmed (read).**

`DecideSundownAlertUseCase` appears in `app/src` in two files only: its own (`domain/DecideSundownAlertUseCase.kt:46`) and its test. Nothing in `app/src/main` builds an `Alert` of kind `TURNAROUND` or `SUNSET`; the only `Alert(` in `main` is the off-track one (`TrackRecordingViewModel.kt:833`). The comment is at `TrackRecordingViewModel.kt:486-489`.

### Premise 6. If the process itself is killed, nothing resumes. **Confirmed for the part a test reaches (read, and observed).**

`onStartCommand` acts only on `ACTION_START` and `ACTION_STOP` (`TrackRecordingService.kt:77-96`) and returns `START_STICKY` (`:97`). **Observed:** the third test in `TrackRecordingServiceSwipeAwayFaultTest` calls `onStartCommand(null, …)`: no foreground notification, no fix listener, `START_STICKY` returned, and the service does not stop itself.

**Not reached:** whether and when Android restarts the service after the process is killed, and what it does with a restarted location service that never calls `startForeground`. Out of scope to fix, as the dispatch says.

## 2. The tests that pin today's behaviour

Both classes carry a header that says a green run means the fault is still there. Each test says which assertion should turn red when the fix lands. They are to be replaced then, not deleted.

**`ui/track/TrackRecordingSwipeAwayFaultTest`** (plain JVM, 4 tests)

| Test | What it shows |
|---|---|
| `control - with the screen's ViewModel alive, walking away from the start delivers exactly one off-track alert` | The readings used below do alert when something is listening. Stays green after the fix. |
| `FAULT today - with a return under way and the ViewModel cleared, readings that would alert deliver nothing` | The dispatch's first path, today. Same readings as the control. The fix stream's collector count goes 1 to 0 and no alert is delivered. |
| `FAULT today - a ViewModel built while a track is still open reports not recording, even after it enters the foreground` | Premise 3, the ViewModel's half. |
| `FAULT today - pressing record on the reopened screen creates a second open track row beside the first` | Premise 4, the ViewModel's half. |

**`service/TrackRecordingServiceSwipeAwayFaultTest`** (Robolectric, the real service and container, 3 tests)

| Test | What it shows |
|---|---|
| `FAULT today - the service goes on recording after the ViewModel is cleared, and a reopened ViewModel reports not recording` | Premise 3. |
| `FAULT today - a second recording started over the first is ignored by the service, so its row gets no points and is never ended` | Premise 4. |
| `today - a null intent, which is what a sticky restart delivers after the process is killed, starts nothing` | Premise 6. |

**Entry points used:** `startRecording`, `startReturn`, `stopRecording`, `onEnteredForeground` on the ViewModel; the tracker's fix stream; `ViewModelStore.clear()`; the service's `onStartCommand` through Robolectric's controller. No inner method is called by hand.

**Why the fault test feeds fixes through the tracker and not through `returnToStart`.** `returnToStart` is public. Called on a cleared ViewModel it would still deliver. Nothing in the app does that, so a test that did would be measuring a path with no caller.

**Counts, from the JUnit XML** (results directory cleared before each run; XML and logs in `~/Zynergy/device-evidence/2026-10-02-alerts-in-service/`):

| Run | What | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| 1 | The two new classes alone | 2 | 7 | 0 | 0 | 0 |
| 2 | The full unit suite, once (`:app:testDebugUnitTest --continue`) | 415 | 3393 | 0 | 0 | 24 |
| 3 | The two new classes alone, five more times (`--rerun`) | REPEATS_PLACEHOLDER |

- Runs 1 and 2 were at commit `2717bded`, the commit that adds the two test classes. What came after it is this report and two index rows.
- Both build logs were searched for compile errors before any result was read: none. Every XML timestamp in run 2 falls between 03:19:57Z and 03:24:04Z, inside the run, which started at 03:19:54Z. So these are this run's results and not a previous run's.
- **The 24 skipped are not mine.** They are in five existing classes: `AvailabilityScreenMapIconStackTest` 19, `AvailabilityScreenTripPlanningFlowTest` 2, `AvailabilityScreenOfflineCacheTest` 1, `AvailabilityScreenWaypointFlowTest` 1, `GenerateFungiIndexDbAsset` 1. No test was skipped, silenced or edited by this dispatch.
- **Nothing failed.** The two owner-held intermittent classes both passed in run 2: `MushroomLogViewModelWiringTest` 2 of 2, `DiagnosticsPanelTest` 8 of 8.
- 3393 less my 7 is 3386 tests that were already there. That figure is derived; the suite was not run at the bare base.

**No revert check was run, and why.** This project proves a test bites by reverting the behaviour and reading the failure. These tests pin a fault that is still in `app/src/main`, and Part 1 may not touch `app/src/main`, so there was nothing to revert. What stands in for it is inside the tests: the control test (same readings, one alert), the collector count going from 1 to 0, the listener count going from 2 to 1, and the first track's twenty points beside the second track's zero. Each fault assertion sits next to a reading that shows the path was live.

## 3. Inventory: what has to outlive the Activity

For the first path (`recording, returning > swiped away > the walker moves away > the alert arrives`).

| | State | Where it lives today | Readers under `ui/` |
|---|---|---|---|
| S1 | Which track is recording, and its mode | The ViewModel's `activeTrack` (`TrackRecordingUiState.kt:26`, set `TrackRecordingViewModel.kt:224`). The service also has the id (`TrackRecordingService.kt:67`) and the mode as a local (`:106`). | `MainActivity.kt:411-412` (starts and stops the service), `:579`, `:581` (the record button), `:156` (the backup guard, read at `ui/backup/BackupViewModel.kt:346`, `:376`). Passed down as `isRecording`: `AvailabilityScreen.kt:592`, `:1347`; `AvailabilityCompactScaffold.kt:222`, `:968`; `AvailabilityCompactMapUi.kt:278`, `:737`, `:751`; read at `AvailabilityMapControlsUi.kt:212-215`, `:224`, `:227`. |
| S2 | The returning flag | `TrackRecordingUiState.kt:43`. Written `TrackRecordingViewModel.kt:428`, `:440`, `:286`. The decision reads it at `:823`; the path-home poll at `:508`. | `MainActivity.kt:618`, `:621`. `AvailabilityScreen.kt:639`, and `:793` where it becomes `isNavigating`, read at `:796-797` (which waypoints the map shows), `:1079`, `:1084-1085`, `:1097` (Back), `:1352-1353`. `AvailabilityCompactScaffold.kt:227-228`, `:975-976`. `AvailabilityCompactMapUi.kt:285`, `:293`, `:740`, `:754`, `:773` (the compass strip hides), `:878` (the HUD shows). `AvailabilityMapControlsUi.kt:230` (the button's tint). `AvailabilityPureFunctions.kt:65-66`. |
| S3 | The start point | The origin waypoint in state (`TrackRecordingUiState.kt:62`, set `TrackRecordingViewModel.kt:570`), with the first breadcrumb as a stand-in (`:819-821`). The waypoint is also saved, and the track row points at it (`:567`). | `MainActivity.kt:625` as `navigationTarget`: `AvailabilityScreen.kt:662`, `:796-797`, `:1357`; `AvailabilityCompactScaffold.kt:232`, `:981`; `AvailabilityCompactMapUi.kt:300`, `:882` (the HUD's target). Breadcrumbs: `MainActivity.kt:608` down to `ui/map/SightingsMap.kt:1330` (the line on the map). |
| S4 | The rolling window of distances | `TrackRecordingViewModel.kt:177`. Appended `:824`, read `:825`, cleared `:281`, `:427`, `:438`. | None. Private. |
| S5 | When the last alert fired (the cooldown) | `TrackRecordingViewModel.kt:182`. Set `:828`, read `:850-853`, cleared `:282`, `:439`. | None. Private. |
| S6 | The collection of fixes that drives the decision | `locationJob`, `TrackRecordingViewModel.kt:172`, `:526-550`. Every fix reaches the decision at `:546`, whether or not it passed the accuracy gate. | None directly. |
| S7 | "Off track now" | `TrackRecordingUiState.kt:45`, written `TrackRecordingViewModel.kt:835`. | `MainActivity.kt:619`; `AvailabilityScreen.kt:641`, `:1354`; `AvailabilityCompactScaffold.kt:229`, `:977`; `AvailabilityCompactMapUi.kt:294`, `:741`, `:755`; read at `AvailabilityMapControlsUi.kt:229` (the button turns the error colour). |
| S8 | Distance and bearing to the start | `TrackRecordingUiState.kt:52`, written `TrackRecordingViewModel.kt:835`, `:837`. | `MainActivity.kt:617`; `AvailabilityScreen.kt:637`, `:1351`; `AvailabilityCompactScaffold.kt:226`, `:974`; `AvailabilityCompactMapUi.kt:284`, `:739`, `:753`; read at `AvailabilityMapControlsUi.kt:224` (the button's spoken description). |

**S1 to S6 must live outside the Activity for the first path to work.** S7 and S8 are what the decision produces for the screen. They only matter while a screen exists, but whatever owns the decision is where the screen has to read them from.

**Also in the ViewModel, tied to the same fix collection, and not needed for the first path:** the last accuracy-gated fix (`TrackRecordingViewModel.kt:188`), which seeds the origin waypoint (`:544`), the end waypoint (`:302`), the sundown countdown (`:492`) and path home (`:507`). They stay where they are in every shape below. One consequence is listed under "Flags outside scope": a recording swiped away before its first good fix never gets an origin waypoint.

## 4. Shapes

All three shapes share one piece: **a new plain-Kotlin class that holds S2 to S5 and makes the decision.** It takes a fix, uses the existing `ComputeReturnToStartUseCase` and `DetectOffTrackUseCase` unchanged, applies the existing cooldown, and calls `AlertDelivery`. It has no Android import, so it is testable with no Activity, Service or Compose tree. The existing off-track tests in `TrackRecordingViewModelTest` (`:433` to `:632`) are its specification. The shapes differ in **who holds that object and how the screen reaches it.**

One constraint applies to all three, from the dispatch's "do not touch what off track means": **the object must be fed every raw fix, as the ViewModel's collector feeds the decision today (`TrackRecordingViewModel.kt:546`), not the sampled points the service keeps (`TrackRecordingService.kt:120`).** The service's collector sees the same raw fixes before sampling (`:116-119`), so this is available. Feeding sampled points would change "three readings" from about two seconds to at least ten (`domain/model/TrackRecordingMode.kt`, HIGH_ACCURACY's 5 s interval). That is a change of meaning and is not proposed. See flag 2.

### Shape A. The service holds it; the ViewModel mirrors it (the header's shape, taken literally)

`domain/AlertDelivery.kt:64-66`: "moving navigation ownership (returning state, start point, window, cooldown, and this call) into the service, with the ViewModel as a mirror."

- **Holds:** a field of `TrackRecordingService`, created in `startRecording` and dropped in `stopRecording`.
- **Commands in:** the ViewModel has no `Context` (`TrackRecordingViewModel.kt:206-209`), so "start return" and "stop return" travel as the start and stop do today: ViewModel state, then an effect in `MainActivity`, then an intent. Two new intent actions.
- **State out, the mirror:** the header does not say how. There are two ways. Either the Activity binds to the service (`onBind` returns null today, `TrackRecordingService.kt:69`), or the service writes into something process-wide that the ViewModel reads. The second is Shape B.
- **Touches:** `TrackRecordingService` (two actions, the field, feeding it, a binder), `MainActivity` (send the two intents; bind and unbind), `TrackRecordingViewModel` (the decision leaves; mirrored state arrives from the binding), plus the new class.
- **Costs:**
  - The command hop and the binding both live in `MainActivity`, the one class no test composes. Premise 4's untested step would gain two siblings.
  - Binding connects after a delay, so the mirror has a "not known yet" state the screen must handle.
  - Everything the service holds is tested only through Robolectric's service controller. `TrackRecordingServiceTest`'s header records what that path has cost before.

### Shape B. One object for the process, driven by the service; the ViewModel mirrors it

- **Holds:** `AppContainer`, beside `alertDelivery` (`AppContainer.kt:322`), which is already built there so that delivery never depends on the Activity.
- **Driven by the service:** the service begins it when a recording starts, feeds it each raw fix from the collector it already runs, and ends it when the recording stops. It is alive exactly as long as the foreground service is, which is the thing that keeps the process alive and has the continuous stream.
- **Commands in:** `startReturn` and `stopReturn` on the ViewModel call the object directly. No intent, no effect in `MainActivity`.
- **State out:** the object exposes its state as a flow. The ViewModel copies `isReturning`, `isOffTrack` and the distance to the start into `TrackRecordingUiState`. Every reader in section 3 keeps reading the same fields it reads today.
- **Touches:** the new class; `AppContainer` (one line); `TrackRecordingService` (begin, end, and one call inside the existing collector); `TrackRecordingViewModel` (the decision block `:823-834` and the two fields `:177`, `:182` leave; three fields become copies); `MainActivity`'s factory (`:191-207`, one more argument).
- **Costs:**
  - It is shared, changeable state with two writers on two threads. The service's scope is `Dispatchers.Default` (`TrackRecordingService.kt:58`); the ViewModel is on the main thread. Today's version never had to be thread-safe. This one does.
  - It must never claim a recording the service is not running. So the service ends it in `stopRecording` and in `onDestroy` (`:100`, `:142`), and nothing else may begin it.
  - The one call inside the existing collector sits in the same block as the sampler and the batching, which the dispatch says not to touch. It would be added beside them without changing them. **That reading of "do not touch" is the planner's to confirm before Part 2.**
  - Who begins it is a real choice. If the service does (on `ACTION_START`), it is always backed by a real stream, but there is a short gap after the Record tap in which Return has nothing to act on. If the ViewModel does, there is no gap, but it can exist with no service behind it when the permission check rolls a start back (`MainActivity.kt:428-433`). I would have the service begin it and have the object accept an early "returning" for the track it is about to be given.

### Shape C. A or B, plus the returning flag saved to storage

- **Adds:** the returning flag in Room or DataStore, so it survives the process being killed.
- **Costs:** a Room column means a migration and a new database version, which is a globally unique number (`CLAUDE.md`, "Verify your base branch"). DataStore avoids that but is a flat setting standing in for something that belongs to a track.
- **Why not now:** it only pays off if a killed process resumes its recording. Premise 6 says nothing resumes, and that is out of scope. `CLAUDE.md`: no correction logic without data showing the case.

### Which I would build, and why

**Shape B.**

- It is the header's intent with the mirror made explicit. The header says the service owns it and the ViewModel mirrors it. It does not say how the mirror reads. Each way to read is either a binding or B's shared object. B picks the one with no new Android surface.
- **Where it departs from the header's words:** the header says "into the service". In B the object is held by `AppContainer` and driven by the service. I do not think the header's shape is wrong. Taken literally it is incomplete, and completing it with a binding puts more code in the one class nothing tests.
- The decision and its state are reachable from a plain JVM test, and from a service test with no ViewModel in existence, which is the check plan task T1 asks for.
- Nothing under `ui/` except the ViewModel changes. Every reader in section 3 is untouched.
- It leaves the second path cheap, whichever way the owner decides it.

**What B costs that should be weighed before a go:** the thread-safety above, and the existing tests in section 6 that drive the decision through the ViewModel and would have to be moved.

### How T2 (the two sundown alerts) would attach. Not built.

T2 needs three things: a tick that runs with no fix arriving (under canopy there may be none), a position, and the memory of which alerts have fired (`DecideSundownAlertUseCase` leaves that to its caller, `domain/DecideSundownAlertUseCase.kt:22-23`).

| | Tick | Position | "Already fired" memory |
|---|---|---|---|
| **A** | A new job in the service, beside the flush loop (`TrackRecordingService.kt:133-138`), not inside it | The service's own last kept point (`:112`) | A field of the service |
| **B** | The same new job in the service; it calls the shared object | The last accuracy-gated fix the object saw from its feed | Held by the shared object, or by a sibling object with the same lifetime |
| **C** | As A or B | As A or B | Saved, if "once per recording" has to survive a killed process |

In every shape the tick is a new loop, so `CLAUDE.md`'s rule applies: its tests stop it inside the test body, in a `finally`. The darkness margin would be read once per recording, as the ViewModel does today (`TrackRecordingViewModel.kt:233`), from the preferences repository that is built and passed nowhere (`AppContainer.kt:258`). Both sundown alerts pass `overridesSilence = true`; off-track keeps `false` (`TrackRecordingViewModel.kt:829-833`).

### What a reopened app would need to read for the second path. Listed, not decided.

The second path is with the owner. This is only what each shape would have to supply.

| What the reopened screen needs | A | B |
|---|---|---|
| That a recording is running, and which track | From the binding, once connected | From the shared object, at once |
| Its mode and start time | The service holds only the id (`TrackRecordingService.kt:67`); the mode is a local (`:106`). Both would have to be kept. | Kept by the shared object when the service begins it |
| The returning flag, "off track now", distance to the start | From the binding | From the shared object |
| The start point | Either shape: the saved origin waypoint, through `GetTrackOriginWaypointUseCase`, which has no caller in `app/src/main` today (`AppContainer.kt:333-335`) | Same |
| The breadcrumb line | Either shape: the ViewModel's existing poll, once it knows the track id (`TrackRecordingViewModel.kt:443-462`) | Same |

Things either shape leaves for that decision:
- **An open track row is not proof of a running recording.** A killed process leaves the same row (premise 6). The reopened screen needs a live source, the binding or the shared object, and cannot go by the row alone.
- `hasStartedRecordingOnce` is false in a new Activity (`MainActivity.kt:410`). If the screen adopted a running recording, the existing effect would send `ACTION_START` again. The service would ignore it (`TrackRecordingService.kt:82`), and the flag would then be set so Stop works. Harmless as written, and worth a test.
- The last good fix starts empty in the new ViewModel (`TrackRecordingViewModel.kt:188`), so the end waypoint, the sundown countdown and path home wait for the next one.
- The once-per-recording notices would be able to show a second time (`:197-198`).
- If the origin waypoint was never created before the swipe-away, the new ViewModel would create it from its own first good fix (`:542-545`), which is where the walker is at the reopen, not where they started.

## 5. Test plan for Part 2

**Fail first, in this order:**

1. **The decision class, plain JVM.** Port the existing off-track tests one for one onto the new class, before it decides anything: three readings and 25 m give one delivery; one delivery per event, not per fix; a second after the cooldown; none while not returning; stopping the return resets the cooldown; the delivered `Alert` is `OFF_TRACK` with `overridesSilence = false`. Each fails on a count of 0 against an expected 1 or 2.
2. **Delivery with no screen alive, through the service's own entry point.** Start the service through `onStartCommand`, mark the recording as returning, and construct no ViewModel at all. Send fixes through `ShadowLocationManager`, as `TrackRecordingServiceSwipeAwayFaultTest` now does. Assert on the off-track notification the real `AndroidAlertDelivery` posts (`alert/AndroidAlertDelivery.kt:112`, id 1002 at `:71`), because the container's `alertDelivery` cannot be swapped for a fake. This is the replacement for the fault test, and plan task T1's check. Before the feed is wired it fails with no notification.
3. **The mirror.** A ViewModel test: `startReturn` on the ViewModel marks the shared object as returning; a state change in the object appears in `TrackRecordingUiState`.
4. **The revert check, naming the edit.** Remove the one feed call in the service: test 2 fails with "expected the off-track notification, found none". Under this project's runner rules: the build log checked for compile errors first, the file restored from a saved copy, the forward change confirmed present afterwards.

**Existing tests Part 2 cannot leave as they are.** They drive the decision through the ViewModel, and the decision will no longer be there. Moving them is an edit to existing tests, so it needs the planner's word in the amendment:
- In `ui/track/TrackRecordingViewModelTest.kt`: the tests at `:433`, `:463`, `:494`, `:525`, `:545`, `:573`, `:593` and `:613`.
- The ViewModel's constructor gains an argument. Six existing test files construct it: `TrackRecordingViewModelTest`, `TrackRecordingSundownTest`, `service/TrackRecordingServiceTest`, `ui/log/JournalPendingDeleteTest`, `ui/log/TrackDeleteTest`, `data/repository/NetworkFixExclusionPerConsumerTest`. A default value would spare them, but a default that quietly builds a private decision object per ViewModel is the fault again in test clothing. I would make it a required argument and edit the six.
- The two fault classes from this dispatch are replaced.

**What no headless test can reach:**
- The swipe from recents: Activity destroyed, process kept, service left running.
- The alert felt in a pocket with the screen off after the swipe-away.
- `MainActivity`'s effect that starts and stops the service.
- What the phone's own battery management does to a foreground service whose app has no task.

## 6. Build steps for Part 2

For Shape B. Each is one working session or less. `main` behaves as it does today until step 3 lands.

1. **The decision class and its tests.** One new file in `domain/`, wired to nothing. The ported tests fail first, then pass.
2. **The service drives it.** Held in `AppContainer`; begun, fed and ended by the service. Nothing sets "returning" in production yet, so behaviour is unchanged. The service test (test 2 above) fails first, then passes, with its revert check.
3. **The ViewModel hands over.** `startReturn` and `stopReturn` go to the shared object; the ViewModel's own decision, window and cooldown are removed; the three state fields become copies; the existing tests are moved. Steps 2 and 3 must ship together: after step 3 alone, nothing would deliver.
4. **The device check on the S22.** The dispatch's first path, plus the three things that must not regress: the alert with the screen off and the app still open; Stop from the notification; the record button agreeing with the service after a stop from the shade.
5. **Only if the owner confirms the second path:** the reopened screen adopts a running recording. Its own dispatch.

For Shape A, steps 2 and 3 become: two intent actions and the service-held object; then the binding in `MainActivity` and the ViewModel reading through it. Step 3 would be more than one session.

## 7. Disclosures

### Confirmed versus inferred

**Read at `9c3f103d`:** every file-and-line claim in sections 1, 3 and 4.

**Observed (tests run, results read from the JUnit XML):** the seven tests in section 2.

**Inferred, not run:**
- That a swipe from recents destroys the Activity, clears its ViewModel, keeps the process and leaves the service running. From the manifest, the absence of `onTaskRemoved`, and Android's documented behaviour. The Activity clearing its ViewModel store on a final destroy is `androidx` behaviour; its source was not read here.
- That the readers in section 3 are complete. They come from a search for each field name under `ui/` and in `MainActivity.kt`. A reader that reaches the state under another name would not be in the list. The pass-through lines were read from the search output, and the lines where a value is used were read in the files.
- Flag 2 below.

### Could not determine

- **Whether the first path's premise holds on the S22:** that the service and the process survive the swipe. Samsung's battery management is the unknown. Device-only.
- **Which fixes have produced the off-track alerts the owner has felt.** See flag 2.
- **Whether Android ever restarts this service after killing the process, and what happens next.** Not reachable here and out of scope.
- **Whether the second path's adoption is safe against a recording whose service died while the process lived.** No such state was produced here.

### Premises that were wrong

**None of the six.** Two were wider than the code, recorded under premises 1 and 3: `onCleared` cancels but does not clear, and the start point survives in storage.

The dispatch named premise 4 as the one most likely to be wrong. It was settled first, and it is the best-evidenced of the six.

### Decided beyond scope

Nothing was built or changed beyond the dispatch. Choices I made inside it:
- **New test files, with their own fakes,** and no additions to `TrackRecordingViewModelTest` or `TrackRecordingServiceTest`, so no existing test file was edited.
- **A control test and a premise 6 test** beyond the dispatch's "at least" list.
- **The swipe-away is stood in for by `ViewModelStore.clear()`.**
- **No revert check,** for the reason in section 2.

## Flags outside scope

Reported, not touched. None is proposed for Part 2 unless the owner says so.

1. **The second track from premise 4 can never be ended or deleted from the app.** Once the service stops, nothing writes that row's end time (**observed**). Records shows an open track as "Still recording" (`ui/log/RecordDetailsSheet.kt:295`, `ui/track/TrackExportPanel.kt:209`), and Delete is refused for a track with no end time (`ui/track/TrackExportPanel.kt:224`, `TrackRecordingViewModel.kt:726`) (**read**). A track left open by a killed process (premise 6) is in the same position.
2. **What "three readings" spans today.** The decision is fed every raw fix (`TrackRecordingViewModel.kt:546`). The tracker asks the platform for a fix as often as every second (`location/AndroidLocationTracker.kt:74`, `:103`), and the one recorded walk on the owner's phone shows GPS fixes exactly one second apart (`docs/audits/2026-09-07-fix-log-walk-findings.md:3`, `:122`). The check compares the newest of three readings with the oldest (`domain/DetectOffTrackUseCase.kt:16-20`), so about two seconds apart. To add more than 25 m to the distance from the start in two seconds takes more than 12 m/s. **Inferred:** steady walking away from the start would not trip it, and a network fix landing far from the GPS position could, in either direction of travel. I could not determine which the owner has experienced. The existing tests step 111 m between readings, so they do not show this either way. The dispatch says not to touch what off track means, and I have not. It is here because the move has to choose which stream feeds the check, and that choice is this question.
3. **The service drops a second `ACTION_START` without a log line** (`TrackRecordingService.kt:82`, no `else`). `CLAUDE.md`: no default fallback that is not logged when it fires.
4. **Restore from backup is guarded by the ViewModel's `isRecording`** (`MainActivity.kt:156`, `ui/backup/BackupViewModel.kt:346`, `:376`). After a reopen that reads false while the service is still writing points (premise 3), so a restore would be allowed during a live recording. **Read, not run.**
5. **A recording swiped away before its first good fix never gets an origin waypoint,** because creating it is the ViewModel's job (`TrackRecordingViewModel.kt:542-545`). The return then points at the first breadcrumb, as it does today for a track with no origin (`:819-821`).
6. **The window list is never trimmed** (`TrackRecordingViewModel.kt:824`); only its last three entries are read. At one fix a second it grows by 3,600 numbers an hour of return. Small. Worth not carrying into the new class.
7. **This coder session ran on Opus 5.5, not Sonnet 5.5.** The launch prompt's model line arrived with its command missing, and a session cannot change its own model.

## For the owner afterwards (device-only, today's build, optional)

The dispatch's two checks stand. Two additions from this report, cheapest first:

1. **Record a track, tap Return, swipe Forager away from recents.** Pass: the recording notification stays. Evidence: a screenshot of the shade. This is the premise the whole task rests on.
2. **Walk away from the start for a minute.** Expected today: no alert.
3. **Open Forager again.** Expected today, from the code and the tests: the Maps tab shows no recording, and the button offers Record, not Stop. Evidence: a screenshot.
4. **Do not press Record there** unless you want the stuck track from flag 1. If you do want to see it: press Record, walk, press Stop, then open Records. Expected today: two tracks, the older one with all the points, the newer one with none and reading "Still recording", with no Delete offered. The stuck track cannot be removed from inside the app today.
5. **End the test from the notification's Stop action,** which is the one control that works whatever the screen believes.
