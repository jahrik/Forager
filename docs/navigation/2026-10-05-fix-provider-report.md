# Every fix carries its true source (dispatch 2026-09-28-527): report

**Status: verification done; the owner answered in RECORD -558. Stage A (`dbaa8efe`): the provider field and the failing tests, not yet run. No Gradle has run.** The dispatch is
`prompts/preserved/2026-10-05-01.md`. It was written against `a9dfdd59`. -516 has merged since
(PR #174), so this was read at `4b72b25c`, where `fix-provider` is cut. App paths are relative to
`app/src/main/java/com/zynergylabs/forager/app/`. Line numbers are as of `4b72b25c`.

## Verify before building

### Citations re-checked

These still hold at `4b72b25c`: `LiveFixGate.kt:71-74`, `AndroidLocationTracker.kt:89-97`
(`toFix()`), `LocationTracker.kt:45-53` (`LocationFix.Update`, with no provider field),
`NetworkProviderFix.kt:48`, `OffTrackJudge.kt:61,70`, and `TrackRecordingViewModel.kt:896` and
`:855-867`. Some of -510's table lines have moved:

| Path | Was (at `d4bd00bf`) | Now |
|---|---|---|
| Live gate in `ui/availability/AvailabilityViewModel.kt` | `:266-277` | `:269-285` |
| `NavigationHud.kt` `arrivedAtStart` | `:440-443` | `:447-450` |
| `straightLineToTarget` and `nextStraightLine` | `:450-488` | `:457-495` |
| `navigationReadout` | `:509` | `:516` |
| `arrivedAtStart` call in `AvailabilityCompactMapUi.kt` | `:594` | `:599` |
| Strip's `liveLocation` and `liveAltitudeMeters` | `:871-872` | `:879-880` |
| `AvailabilityScreen.kt` waypoint line | `:989-997` | `:992-1000` |
| Service collector in `TrackRecordingService.kt` | `:138-156` | `:148-170` |

These have not moved: `MushroomLogViewModelFactory.kt:39`, `MushroomLogViewModel.kt:403-420` and
`AvailabilityCompactScaffold.kt:1152`.

### 1. Every path a live fix takes to something that acts

**Where `liveFix` is set.** It is written in one place only, `AvailabilityViewModel.kt:280`. That
happens when `acceptLiveFix` passes (`:277`). A refused fix goes to `approximateFix` (`:295`). Every
reader of `liveFix`, `liveLocation` and `liveAltitudeMeters` in `main/` was found by grep, and each
one is downstream of that line. So a provider check added at `:277` covers all of them together:

- **Arrival:** `arrivedAtStart`, for "Arrived" and the ring.
- **The waypoint's line:** `nextStraightLine`.
- **A new find's location:** `freshDeviceLocation`.
- **Display:** the HUD readout and the strip.

**Display paths need no change.** The map dot is fed by the app from `shownPosition`
(`domain/ShownPosition.kt:46-59`, `ui/map/MapPosition.kt:51`). True north reads `headingFix`, which
is `liveFix ?: approximateFix ?: lastKnownFix` (`AvailabilityUiState.kt:359`). Once a network fix
goes to `approximateFix` rather than `liveFix`, both pick it up as approximate: the dot shows the
approximate look and the strip shows approximate text.

**What changes for a network fix of 50 m or better.** Today it replaces `liveFix` and can displace a
fresh GPS fix. Afterwards it is held only as `approximateFix`. `shownPosition` already prefers any
precise fix that is not lost (`:52`), so the network fix cannot take over from GPS.

**`TrackRecordingViewModel` has its own stream (`:880-905`).** On that stream:

- **`lastGatedFix`** (`:896-897`): the first-fix rule of `LocationSampler` for the active mode,
  with any provider allowed. It seeds the origin waypoint (`:899`) and `RouteHome` (`:859`). A
  network fix within the mode's ceiling reaches both today. The provider check goes at `:896`.
- **`lastGatedFix` also feeds the recording's sundown countdown** (`updateSundown`, `:825-838`, from
  `40f4c59c`). Neither the dispatch nor -510's table lists this reader. See stop 2.
- **`returnToStart(point)`** (`:902`) takes every fix. It is display only: the return control's
  distance text and its content description (`AvailabilityMapControlsUi.kt:235,566-578`).

**-516's watcher.** `SundownWatch.onFix` (`domain/SundownWatch.kt:131-136`) is called with a raw
`TrackPoint` from the service (`TrackRecordingService.kt:156`). It sets `newestGpsFix` by the
timestamp rule (`:134`). `newestGpsFix` drives the walk back and the "arrived home" stop (`:187`).
The sunset is taken from a fix of any provider, which is the owner's exception and is left alone.

**A second, one-shot path that neither table lists:** `LocationProvider.getCurrentLocation()`
(`location/AndroidLocationProvider.kt`). It asks GPS and the network at once and returns
**whichever answers first**. It applies no accuracy gate. Its `LocationResult.Success` carries no
accuracy and no provider. Its callers:

- **Acts, and writes data:** `MushroomLogViewModel.requestAndPatchCaptureFix` (`:1022-1040`). The
  position is written to the photo row, and for the camera's find path it is promoted to the find's
  `foundAt` (`:1016`). Indoors or at a cold start the network usually answers first: the class's
  own doc comment says so. **So a camera find can be saved at a network position of any accuracy
  today.** See stop 1.
- **Sets the species search region:** `AvailabilityViewModel.useCurrentLocation` (`:449`). That
  search uses a radius in kilometres.
- **Display or camera centring:** `locateMe` (`:488`), the default centre for offline maps
  (`:1034`) and the cartography entry's centring (`CartographyEntryReportScreen.kt:327`).

### 2. The service's stream

The service holds the `LocationFix.Update` at the moment it calls `toTrackPoint()`
(`TrackRecordingService.kt:151`). **Proposal:** the service passes the live provider as an argument
beside the point, and nothing stores it:

- **Calls:** `returnWatch.onFix(candidate, fix.provider)` and `sundownWatch.onFix(candidate,
  fix.provider)`.
- **Sundown watch:** sets `newestGpsFix` only when the provider is GPS.
- **Off-track judge:** gets the reading's provider, so `next(reading, provider)` at `:69-70` judges
  the live reading by its provider. The stored path at `:61` keeps the timestamp rule, because a
  stored point has no provider.
- **No column is added.** The sampler and the stored track are unchanged.

### 3. Unknown providers

The logs were read for this. I counted `provider=` across every `ForagerFix` line in the 56 files
under `~/Zynergy/device-evidence/` that contain the tag. Only `gps` (76,425 lines) and `network`
(4,944 lines) appear. Nothing else appears: no `fused`, `passive` or `null`.

In the code, live updates are requested from `GPS_PROVIDER` and `NETWORK_PROVIDER` only
(`AndroidLocationTracker.kt:71-75`). `PASSIVE_PROVIDER` is read only for the last known position
(`AndroidLastKnownLocationSource.kt:68`), which is display only. So nothing on main receives
`fused` or `passive` as a live fix.

**Proposal:** the planner's default. An unknown provider never acts and may be shown, as
approximate.

### 4. The disagreement check

**Proposal:** tag `ForagerFixRule`, at debug level, in `AndroidLocationTracker.onLocationChanged`
beside the existing `ForagerFix` line. It logs only when provider is GPS and the milliseconds are
not zero, or provider is network and the milliseconds are zero. An unknown provider has nothing to
disagree with, so it is not logged.

During a recording it fires once per collector, as `ForagerFix` does. Each collector registers its
own listener: three do during a recording (`TrackRecordingViewModel.kt:656`). See the counts below.

### 5. The S22's own logs, recounted

**The dispatch's counts are log lines, not fixes. Two things inflate them:**

- **The desk folder holds three overlapping captures.** `logcat.txt` has 1,643 lines, `dot.txt`
  1,317 and `dot2.txt` 1,766. Together that is 4,726, which matches the dispatch. The union of the
  three is 1,766 distinct fixes. Fixes were counted as distinct by provider, accuracy and
  timestamp. The first two files are contained in `dot2.txt`.
- **During a recording each fix is logged once per collector:** up to three times on the walks.

Distinct fixes:

| Capture | GPS fixes | GPS accuracy | GPS ≤ 50 m | Network fixes | Network accuracy | Network ≤ 50 m |
|---|---|---|---|---|---|---|
| Desk (union) | 1,514 | 18.7–267.7 m | 222 | 252 | 15.2–100 m | 5 |
| `2026-10-03-owner-walk` | 648 (607 at 3.79 m) | 3.79–9.9 m | 648 | 81 | 12.5–600 m | 11 |
| `2026-10-03-walk-t21` | 387 (369 at 3.79 m) | 3.79–20.1 m | 387 | 60 | 16.7–500 m | 5 |
| `2026-10-03-walk-zoom` | 700 (639 at 3.79 m) | 3.79–12.7 m | 700 | 93 | 11.6–500 m | 83 |

Walk totals: 1,735 GPS fixes, of which 1,615 report exactly 3.79 m, and 234 network fixes. The
dispatch's line counts were 4,614 and 4,460.

**The timestamp rule against the provider.** The dispatch says it agreed on every fix. That holds
for the desk only: 1,766 of 1,766 distinct fixes. **On the walks it disagreed 4 times in 1,969:**

- three GPS fixes stamped with milliseconds (463, 479 and 602 ms; accuracy 8.8 m, 11.8 m and
  9.9 m);
- one network fix on a whole second (300 m).

The rule is not exact even on the S22. The disagreement log is worth having.

Not read: `2026-10-03-walk-t21/a1-replay-real-watch/log.txt`, a replay rather than a capture.

**For the `LiveFixGate.kt` comment.** Both sentences the dispatch names are wrong on these counts:
"carries no signal on that device's GPS path" and "never rejects a GPS fix". Indoors, GPS reports
18.7–267.7 m, and 1,292 of 1,514 desk GPS fixes are over 50 m. 3.79 m is the outdoor best case.
The limits: one phone, one desk session, three walks.

## Stop-and-ask, for the owner

1. **Camera finds take the first answer from GPS or the network, with no accuracy gate.** This is
   item 1's one-shot path. It is outside the paths the dispatch names, and it is the one place
   besides `freshDeviceLocation` where a find's location is written from a fix. The options:
   - **(a)** Leave it alone in this dispatch, and record it as an uncovered failure against -519
     for a dispatch of its own.
   - **(b)** Bring it in now. The one-shot result would carry its provider, and the camera path
     would refuse a network answer. That reverses the deliberate "race both" choice
     (`AndroidLocationProvider.kt`, `awaitFirstLocation`'s doc comment). It also needs a fallback,
     or under canopy a camera find gets no location.

   **Recommend (a).** The fallback is a design question in its own right.

   The search region (`useCurrentLocation`) is left as is under either option: it searches at
   kilometre scale.
2. **Making `lastGatedFix` GPS-only would blank the recording's sundown countdown indoors.**
   `ComputeSundownCountdownUseCase.kt:42` returns `NoPositionYet` without a position. Today a
   network fix within the mode's ceiling gives the countdown its place. The options:
   - **(a)** Split the field. The origin waypoint and `RouteHome` read a GPS-only gated fix. The
     countdown keeps reading what it reads today: gated by the mode, any provider.
   - **(b)** Make all three GPS-only, and accept "no position yet" indoors.

   **Recommend (a).** The countdown is unchanged, and it matches -516's "sunset from any fix"
   exception. It also means nobody sees a countdown that looks broken indoors.
3. **The provider field's default on `LocationFix.Update`.** It is built at 47 sites in 26 test
   files and at two sites in `main/`. The options:
   - **(a)** No default. The compiler finds every site, and each test fixture says GPS explicitly.
   - **(b)** Default to unknown. That is honest, but every fixture meant as a precise fix silently
     turns approximate, and it surfaces as unrelated test failures.
   - **(c)** Default to GPS. That is a guess, and the dispatch rules guesses out.

   **Recommend (a).** It is a mechanical edit to fixtures only, and no assertion changes.

## Disclosure (so far)

- **Confirmed vs inferred.**
  - Confirmed by reading and grep: every path above, and the counts, from the logs with a script
    (`/tmp/fixcount2.py`, which is not committed).
  - Inferred: that "the network usually answers first" for camera finds. That comes from
    `AndroidLocationProvider`'s own comment, not a measurement.
- **Could not determine.** What the S22 sends from `getLastKnownLocation(PASSIVE_PROVIDER)`. No log
  records it.
- **Premises that were wrong.**
  - The counts are lines, not fixes.
  - "Agreed on 4,726 of 4,726" holds for the desk only. The walks disagree 4 times.
  - The paths missed the one-shot `LocationProvider` path and the countdown's read of
    `lastGatedFix`.
  - **This verification step itself missed a reader.** The end waypoint, made when a recording
    stops, is built from `lastGatedFix` (`TrackRecordingViewModel.kt:594`). It was found while the
    tests were being written. It takes GPS only under the dispatch's rule 2, since it saves a
    position. The planner accepted that, matching the owner's answer for the origin waypoint
    (RECORD -558).
  - The walk folder names are `2026-10-03-walk-t21` and `2026-10-03-walk-zoom`, not
    `-owner-walk-t21` and `-owner-walk-zoom`.
- **Decided beyond scope.** Nothing yet.
