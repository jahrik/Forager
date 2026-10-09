# Battery saver: built, and measured on the S22 (dispatch 2026-09-28-767, plan T17)

RECORD intent -767; the owner's answers -765, -766, -768. Branch `battery-saver`, cut from `origin/main` at
`c81b326d`. Written by the coder, 2026-10-09 (UTC).

## What the owner chose

- T17: "A choice of mode that also changes how often the phone is asked for a fix. Measure on the S22 first; no
  saving is claimed without it."
- "A Battery saver switch (Recommended)", in the strip's quick menu, off by default (-766).
- After the verify step (-768): "Slow everything, every 5 s (Recommended)"; "Accept up to 4 s (Recommended)";
  "Quick menu, logger off (Recommended)", with the wording "Battery saver" / "Checks your position every 5
  seconds instead of every second. Alerts can come a few seconds later."

## What the verify step found (before any code)

Every claim read in the worktree at `c81b326d`.

1. **One place sets the rate.** `location/AndroidLocationTracker.kt` asked GPS and network each every 1 s, no
   minimum distance, plain `LocationManager` (no priority setting: the provider is the priority).
2. **Each collector registers separately, and the fastest wins.** The recording service, the recording screen's
   ViewModel and the map's ViewModel each collect the tracker's cold flow, so each makes its own platform
   registration; the platform serves a provider at the fastest interval asked. The recording screen's
   registration lasts the whole recording, screen off included (`TrackRecordingViewModel.onLeftForeground`'s
   doc comment). A saver rate in the service alone would have changed nothing on a walk. Hence the owner's
   "Slow everything": the one tracker follows the switch.
3. **What is kept is separate.** Recording samples at `TrackRecordingMode.HIGH_ACCURACY` (5 s, 5 m, 30 m). The
   enum's `BATTERY_SAVER` value changes what is kept and the accuracy limit, not the request rate; not used.
4. **Effects of a 5 s rate** (reported to the planner, accepted by the owner):
   - off-track alert: needs at least 3 GPS readings over at least 15 s, so worst case from leaving the path to
     the alert goes from about 16 s to about 20 s (up to about 4 s later); re-arm about 11 s to about 15 s;
   - arrival noticed up to 5 s later (about 6 m at walking pace); "Approaching" is a 100 m zone, cosmetic;
   - back-by and sundown: on the service's own 15 s timers, unchanged; a fix every 5 s stays FRESH (stale at
     30 s), so nothing greys between fixes;
   - recorded track: kept points may fall from about 5 s to about 10 s apart, because fix times jitter around
     the 5 s sampling floor; distance on winding paths slightly short, by an amount not measured;
   - navigation needle: bearing and distance update every 5 s (about 4 degrees per update at 100 m out).

## What changed

- `domain/BatterySaver.kt`: the repository interface (`enabled` as a `StateFlow`), `FIX_INTERVAL_MILLIS` (1 s),
  `BATTERY_SAVER_FIX_INTERVAL_MILLIS` (5 s).
- `data/repository/DataStoreBatterySaverPreferenceRepository.kt`: DataStore, file `battery_saver_preferences`,
  off by default, per-instance factory as the other settings.
- `location/AndroidLocationTracker.kt`: follows the switch; a change re-registers at once (mid-recording
  included); still ends after one `PermissionDenied` with a switch flow that never completes. Each registration
  logs its interval under `ForagerFixRate`.
- `AppContainer`: the repository, handed to the tracker. `TrackRecordingService`: reads the stored value when a
  recording starts, so a recording with no screen asks at the stored rate.
- Map ViewModel, state, screen, `MainActivity`: load and store the switch; a failed store puts the switch back.
- `MapQuickSettings.kt`: a "Battery saver" checkbox row, last, under a divider, with its one line. A checkbox
  like the Off-track and Sundown rows above it, not a toggle switch.

## Tests and revert checks

New tests, through the real entry points:

- `AndroidLocationTrackerBatterySaverTest` (4): the interval each provider is registered at, as Robolectric's
  `ShadowLocationManager.getLocationRequests` holds it: off 1 s, on 5 s, on then off during one collection
  (re-registered each time, one request per provider, removed at the end), and no permission still ends the
  flow after one `PermissionDenied`.
- `TrackRecordingServiceBatterySaverTest` (2): the real service and container, no Activity; the value stored
  by another instance before the service starts (as across a restart): on gives a GPS request at 5 s; off
  (the control) at 1 s.
- `DataStoreBatterySaverPreferenceRepositoryTest` (3): off on a fresh install; on survives a recreated
  instance; back off survives too.
- `AvailabilityScreenQuickSettingsTest`, one new test: the real screen and ViewModel; the row off by default,
  the label and its line present, and real touches at 5%, 30%, 60% and 95% of the row's width each flipping it
  and each change stored, in order.

Targeted run: 34 tests in the five classes, all pass (`AndroidLocationTrackerTest`'s 11 included).

Revert checks. Each run edits a saved copy's file by one change, runs the named classes, refuses to read results
if the build log has a compile error (none had one), restores from the saved copy and confirms it byte-identical;
`git status` was clean after each.

| # | Revert | Failed, as predicted | Message |
|---|---|---|---|
| R1 | tracker always asks at the off interval | tracker on, tracker mid-collection, service on (3 of 6) | `expected:<[5000]> but was:<[1000]>` |
| R2 | service never reads the stored value | service on (1 of 6) | `expected:<[5000]> but was:<[1000]>` |
| R3 | the flow no longer ends on `PermissionDenied` | the new no-permission test (1 of 15) | `UncompletedCoroutinesError: After waiting for 1m` |
| R4 | the row's checkbox does nothing | quick-menu test | `a touch at 0.05 of the row's width expected:<true> but was:<false>` |
| R5 | the ViewModel does not store | quick-menu test | `each change stored, in order expected:<[true, false, true, false]> but was:<[]>` |
| R6 | the repository does not write DataStore | repository restart, service on (2 of 5) | `expected:<true> but was:<false>` |

What the reverts also show. R3: the 11 existing `AndroidLocationTrackerTest` tests all passed with the
completion removed, because they build the tracker with a switch flow that completes; only the new test, with a
live switch, holds the first-launch contract. R4 failed at the first sample (the checkbox itself, at 5%), so it
says nothing about the other three points under that revert; the forward run shows all four reach the row. The
two "off" tests (service control, repository back-off) cannot fail under any revert here, since off is also the
default; they are controls, not guards.

Full suite (with `assembleDebug`, one Gradle run): 586 classes, 4,520 tests, 0 failures, 0 errors, 24 skipped;
no `@Ignore` added by this branch. Not compared against a `main` run, so whether 24 skipped matches `main` is
read from the diff (no skip added), not measured.

## Measurement on the S22

MEASUREMENT_PLACEHOLDER
