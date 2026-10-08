# Small fixes from the 2026-10-07 S22 day check (dispatch 2026-09-28-685)

Branch `small-fixes-1007`, cut from `origin/main` at `b71c1569` (motion Part 3 merged; data parts A to C, motion Parts 1 and 2 and the failure fixes already on it). Dispatch: `prompts/preserved/2026-10-07-15.md`. The owner's words are in RECORD -644, -655, -678, -684 and -689.

**State: code and tests written for fixes 1, 3 and 4. Fix 5 is done and proven. Fix 2 is a proposal only, with no code. Nothing has been compiled or run under Gradle.** Gradle waits for the planner's go. That go also waits on two stops, listed below.

## Premises checked

- **The base.** `origin/main` was `b71c1569`, the motion Part 3 merge, as the dispatch said.
- **Fix 3, the "existing interface".** It exists: `domain/AlertAudibility.kt` (`AlertAudibility`, `RingerMode`). Today it is read only once, at the start of a trip, for the silenced-phone warning. Fix 3 reuses it.
- **Fix 3, where a vibration is recorded.** There are two places:
  - the Return record line, `data/diagnostics/FileReturnRecord.kt`, through `ReturnWatch`;
  - the sundown watch's "partly delivered" error log, `domain/SundownWatch.kt`. The sundown alerts always override silence, so this place never meets the silent case.
- **Fix 4, which alerts can name a time already gone.** Only the leave-by alert can.
  - In `DecideSundownAlertUseCase`, the leave-by moment being due implies the heads-up is due too. So once leave-by has passed, the leave-by alert fires and the heads-up is spent without firing. A heads-up therefore always names a future time.
  - The sunset alert names no start-by time.
  - With the walk back unknown, the text is "The walk back is unknown." and names no time either.
- **Fix 4, a wording note for the owner.** The owner's example begins "The walk back is about 2 min". The current measured text begins "The walk back the way you came is about…", which is the owner's own 2026-10-05 wording. The proposal keeps "the way you came".
- **Fix 2, the cause** (all read from the code):
  - In landscape, the strip and the navigation display are anchored to the rail side (`AvailabilityCompactMapUi.kt:926-932` and `:1068-1075`).
  - Neither one is bounded by the search bar.
  - The strip is as wide as its content.
  - The display is `fillMaxWidth` up to `LANDSCAPE_HUD_MAX_WIDTH` (360 dp).
  - At font 2.0 both grow across the centre line into the bar.
  - The status line is `maxLines = 1` with soft wrap on and no ellipsis (`NavigationHud.kt:353-359`). So it breaks at a word and draws only the first word: "No".
- **Something that does not exist.** Nothing in `main/` defines the central third. Only the tests do (`AvailabilityScreenLandscapeB2Test`, `AvailabilityScreenNavigationWordsLandscapeTest`). No landscape test sets a font scale.

## Fix 1: the "Imported" label

In `ui/log/RecordDetailsSheet.kt`, `DetailsTitle`:

- The title gets `weight(1f, fill = false)`.
- When a word sits beside it, the title is one line with an ellipsis.
- The label and "Stale" are `maxLines = 1` and `softWrap = false`.
- A title with nothing beside it wraps as before. That covers waypoint names, and region names that are not stale.

A region's "Stale" label gets the same fix, because it is the same composable.

Test: `ui/log/ImportedLabelFitTest.kt`.

- It goes through the real `RecordsTab`, then the Tracks chip, then a real touch on the row, then the details.
- It runs at 360 dp with native graphics, at font scale 1.0 and 2.0, using the S22's title "forager-track-2026-09-27-192346".
- It checks that the label is one line, whole, in a box as wide as it needs, and inside the window.
- It checks that the title is one line, ellipsised, and ends before the label.
- It checks that a short title shows whole.

## Fix 3: a skipped buzz recorded as done

How it works:

- `AndroidAlertDelivery` takes an `AlertAudibility`. It is the last constructor parameter, defaulting to `AndroidAlertAudibility`, so `AppContainer` is unchanged.
- After the vibration is issued without error, the delivery reads the ringer.
- `vibrationSkippedBecause(overridesSilence, ringerMode)` in `domain/AlertDelivery.kt` returns "phone on silent" only for an alert that does not override silence while the ringer is on silent.
- The outcome then has `vibrated = false` and `vibrationSkipped = "phone on silent"`.
- The Return record writes `vibration=skipped(phone on silent)`, in the same form as the existing `failed(...)` and `not-posted(...)`.
- The vibration is still issued, with the same usage. The alert's behaviour is unchanged.
- A ringer that cannot be read is logged, and the outcome is left as it was.

Two compatibility notes:

- `vibrationSkipped` defaults to null, so back-by's 4-argument `AlertDeliveryOutcome(...)` calls still compile.
- The `vibrate(...)` call line itself is untouched.

Test: `alert/SkippedVibrationRecordTest.kt`. It walks off track through `ReturnWatch.onFix` into the real delivery and the real `FileReturnRecord`, with the ringer faked behind `AlertAudibility`, and reads back the line that was written. It covers five cases:

- silent: skipped, and still issued once with no override;
- Vibrate and normal: done;
- a sundown alert on silent: not skipped;
- an unreadable ringer;
- a throwing vibration on silent: failed, not skipped.

## Fix 4: "Start back now" (wording is a stop)

How it works:

- `SundownAlertDetail` gains `decidedAtEpochMillis`. It defaults to null, which keeps the old text. `SundownWatch` sets it to the tick's `now`.
- `leaveByHasPassed` is true when the start-by time's clock minute is before the minute the alert was decided in.
- The measured and at-least texts then use the new strings below. The unknown text is unchanged.
- The alert still fires once, at the same moment.

Test: `alert/StartBackNowTest.kt`. It runs through `SundownWatch.tick` on a fake clock into the real `AndroidAlertDelivery` and reads the posted notification. It covers four cases:

- a recording started 5 minutes past leave-by;
- the S22 case: the margin raised from 30 min to 1 h 30 after the heads-up fired;
- a leave-by alert decided within its own minute, which keeps the clock time;
- the minute boundary for measured, at-least and unknown.

## Fix 5: design-token check 3 and typed tweens

`scripts/verify-design-tokens.sh`, check 3. The pattern is now `(^|[^[:alnum:]_])tween(<[^()]*>)?\(` for both greps. It is a plain script, so it was run with no Gradle:

- **Planted in `ui/backup/RestoreLoadingPage.kt`:** `tween<Float>(300)` and `tween<Pair<Float, Float>>(durationMillis = 300)`.
  - The new check names both.
  - HEAD's script, run against the same planted file, named neither.
- **Planted in `ui/motion/MotionTokens.kt`:** `tween<Float>(300)`. The new check names it, so the one allowed exception still excludes only its exact line.
- **Restored:** each planted file was restored from a copy saved before editing, and confirmed with `sha256sum -c`. `git status` showed only the script modified.

**What it finds now** (unplanted): the same five hits as before. There is no typed tween in today's tree.

- `ui/map/fanout/MarkerFanOutState.kt:87`
- `ui/backup/RestoreLoadingPage.kt:94`, `:95`, `:97` and `:100`

Not fixed, as the dispatch said. Not covered either: a direct `TweenSpec(` constructor, or an import alias of `tween`. Neither occurs in `main/` today.

## Stops (sent to the planner before any build)

These are in the coder's final message: fix 2's approach, and fix 4's exact strings.

## Not verified

- Nothing has been compiled. No test has run except the shell check for fix 5.
- No revert checks have run yet. They are planned for fixes 1, 3 and 4, on the go:
  - fix 1: drop the title's weight;
  - fix 3: return `null` from `vibrationSkippedBecause`;
  - fix 4: force `leaveByHasPassed` to false.
- Do Not Disturb, and Android's own "vibrate for notifications" settings, can also drop the off-track buzz. The ringer cannot tell this app either case, so the record still says "done" for them.
- Whether "Approaching · last fix 45 s ago" is cut at 360 dp has not been measured.
