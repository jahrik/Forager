# Small fixes from the 2026-10-07 S22 day check (dispatch 2026-09-28-685)

Branch `small-fixes-1007`, cut from `origin/main` at `b71c1569` (motion Part 3 merged; data parts A to C, motion Parts 1 and 2 and the failure fixes already on it). Dispatch: `prompts/preserved/2026-10-07-15.md`. The owner's words are in RECORD -644, -655, -678, -684 and -689.

**State, after Amendment 1 (RECORD -694) and the build:** see "Amendment 1" and "The build" at the end. The sections up to "Not verified" are the pre-build report, left as written; the pre-build stops are answered by Amendment 1.

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

## Amendment 1 (RECORD -694): what was added

The owner's answers, verbatim:

1. Fix 2: "Stay beside the search bar, '…' (Recommended)".
2. "Drop 'Approaching ·' if needed (Recommended)".
3. Fix 4: "Approve as written (Recommended)".
4. Fix 3: "Yes, cover Do Not Disturb (Recommended)".

### Fix 2: the strip and the display stay beside the search bar

Code: `ui/availability/LandscapeChromeWidth.kt`, applied in `AvailabilityCompactMapUi.kt`.

- **Width.** The landscape strip and display are capped at the room between the rail-side controls edge and the search bar's end.
- **How the bar's end is found.** It is recomputed the way the scaffold places the bar: `min(384 dp, half the window − punch-side inset − 8 dp)`. It is not read back from a measurement. That avoids the regression recorded on `onGloballyPositioned` placement.
- **Text.** Every text in the display and the strip is one line, with no soft wrap, and ends in "…" when it overflows. That covers the target, the distance and its kind, the status, the heading, the altitude, the labels, and the strip's "no fix" line.

### Item 2: "Approaching ·" is dropped when it does not fit

- `NavigationHudReadout.statusShortText` holds "Last fix N s ago". I kept the capital L, the same form the line takes when the walker is not approaching.
- `statusTextThatFits` measures the status line's own width (`BoxWithConstraints` with `rememberTextMeasurer`).

### Item 4: Do Not Disturb

- New owned seam: `DoNotDisturbSource` / `DoNotDisturbFilter` in `domain/AlertDelivery.kt`, with `alert/AndroidDoNotDisturbSource.kt` as the Android side.
- The rules are in `vibrationSkippedByDoNotDisturb`:

| Do Not Disturb mode | Off-track (notification usage) | Sundown (alarm usage) |
|---|---|---|
| Total silence | skipped | skipped |
| Alarms only | skipped | not skipped |
| Priority only | skipped (inferred) | not skipped |

- **Priority only is inferred.** It assumes Android's defaults. The app's own Do Not Disturb exceptions, and whether a channel may bypass Do Not Disturb, are not read: reading them needs notification-policy access, which the app does not ask for.
- Do Not Disturb is checked before the ringer. A reading that fails is logged and treated as saying nothing.
- **Not covered:** a phone-level setting that turns vibration off (Android's vibration and haptics settings). Nothing the app can read shows it.

### Constructor change

`AndroidAlertDelivery` now has a three-argument internal constructor. A test that ends in a trailing lambda, `SundownNotificationTextTest`, therefore still passes the vibration seam. The full constructor takes the ringer and Do Not Disturb explicitly.

## The build (the only one, 2026-10-08)

### How it was run

- Every run was under `systemd-run --user --scope -q -p MemoryMax=5G -p MemorySwapMax=0`.
- Gradle heap 1536m, Kotlin daemon 2g, Java temp `~/.cache/forager-test-tmp`.
- No daemon was running before the first run.
- Each run compiled first, then the Kotlin daemon was stopped, then tests ran.
- `./gradlew --stop` at the end, with no Gradle or Kotlin process left.
- Free disk: 4.4 GB at the start, 4.0 GB at the end.

### First compile

It failed in `SundownNotificationTextTest`. My new last constructor parameter had captured that test's trailing lambda. I fixed it in the production code with the three-argument constructor above, and did not touch the test.

### New tests

All pass, except the font 1.0 case below:

- `SkippedVibrationRecordTest` (8 tests)
- `StartBackNowTest` (4)
- `ImportedLabelFitTest` (3)
- `LandscapeLargeFontTest` (9)
- the additions to `NavigationHudReadoutTest`

`ImportedLabelFitTest` first failed on its own harness: the details sheet is a second window, so the screen has two roots. I fixed the test to read the first root.

### Revert checks

Each one restored the file from a copy saved before the edit, and checked it with `cmp`. Each compile log was checked for errors before any result was read.

| Check | Edit | Result |
|---|---|---|
| Fix 1 | The title loses its weight | Bit at font 1.0 and font 2.0: "every character of the label shows expected:<8> but was:<1>", the S22's one-letter column. |
| Fix 3 | The ringer never skips | Bit: "expected vibration=skipped(phone on silent) but was vibration=done", in 2 tests. |
| Fix 4 | `leaveByHasPassed` is always false | **The first try did not compile:** a smart cast was lost, the case CLAUDE.md describes. Its results were not cited. Redone as `get() = false`, it bit in 3 tests: "…start by 4:54 PM" where "Start back now…" was expected. |
| Item 1 | No width cap | Bit at both rotations at font 2.0. For example, display `left=356 dp` against a search bar ending at 384 dp, and strip `[94, 716]` dp against the bar's `[0, 384]` dp. |
| Item 2 | The short form is never used | Bit: "expected Last fix 45 s ago but was Approaching · last fix 45 s ago". |
| Item 4 | Do Not Disturb ignored | **The first try did not compile:** a type mismatch. Not cited. Redone, it bit in 2 tests: "PRIORITY, NORMAL … expected skipped(Do Not Disturb) but was done", and "expected Do Not Disturb but was null". |

### Measured, with native graphics and the S22's landscape window (823 × 384 dp) under Robolectric

**At font 2.0:**
- The display and the strip are each 332 dp wide and end exactly at the bar.
- The display is 122 dp tall, with and without the cap, so its height is unchanged. It clears the central third by 6 dp.
- The status line shows 6 of its 33 characters, then "…".
- The coordinates in the display and the strip, and the strip's altitude ("404 ft" shows 2 characters), end in "…". At this font, the coordinates show nothing but the "…".

**"Approaching · last fix 45 s ago":**
- At 360 dp portrait and in landscape, font 1.0, it is whole.
- At 360 dp portrait, font 2.0, it reads "Last fix 45 s ago".

**Fix 1:** at 360 dp the label is 54 dp at font 1.0 and 106.5 dp at font 2.0, one line, inside the window. The long title shows 25 of its 31 characters at font 1.0 and 12 at font 2.0, then "…".

### Full suite

4,233 tests, 24 skipped, **2 failures, both from one cause, and not touched:**

1. `AvailabilityScreenNavigationWordsLandscapeTest`, an existing test: "MGRS, navigation-hud-distance-kind <by trail>: not ellipsised". It shows 5 of 8 characters.
2. `LandscapeLargeFontTest`, "font 1,0 the display is whole…", a new test: "font 1.0 <No origin waypoint for this track> is not ellipsised". It shows 30 of 33 characters.

**Cause: the owner's two rules disagree at font 1.0.**

- With no cap, the display already overlapped the search bar at font 1.0.
  - In the 823 dp window it was 360 dp wide from x = 383 dp, against a bar ending at 384 dp: 1 dp of overlap.
  - The existing test's 780 dp window has room for only about 318 dp beside the bar, so the overlap there is about 42 dp.
  - The S22's real cut-out inset puts its overlap at roughly 20 dp. That figure is inferred, not measured.
- So "never wider than the gap to the search bar" changes the display at font 1.0, and that cuts text, against "at font 1.0, nothing changes".

This goes back to the owner. The options are in the coder's message.

## Not verified (after the build)

- Every layout figure above is from Robolectric, which reports no insets. The S22's cut-out and status bar, and so its real gap and its central-third clearance, are device items.
- The Priority-only rule for Do Not Disturb is inferred from Android's defaults.
- A phone-level vibration-off setting is not covered.
- No phone or emulator was used.
