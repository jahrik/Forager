# sundown-line: the sunset line in the strip and the HUD (T3), and Settings > Sundown (T4)

Dispatch 2026-09-28-592 (RECORD -592, `prompts/preserved/2026-10-06-11.md` on `records-after-173`),
with Amendment 1 (RECORD -593), Amendment 2 (RECORD -595) and the no-position ruling (RECORD -596).
Branch `sundown-line`, cut from `origin/main` at `ef269025` (still `origin/main` when this was
written). Not merged; no pull request opened. Laptop coder, no phone, no adb.

The code was built by a coder session that the machine's restart killed after pushing `ea722ff7`.
That session left no report and no recorded red runs. A second session finished the work: the revert
checks, the strip layouts, the full suite and this report. Both are described below, and which
session did what is stated where it matters.

## Commits

| Commit | What |
|---|---|
| `a9ca61fc` | Nine tests that pin when each sundown alert fires (`SundownWatchAlertMomentsTest`). The commit message says they were green on `main` before the watch changed. This session did not re-run them at that commit; see "Not done". |
| `ea722ff7` | The line, the settings, both amendments and -596. |
| `33c6e322` | Strip tests in landscape and fullscreen, and three repairs found when reading `ea722ff7` (see below). |
| `59c4dd85` | The strip tests read the laid-out line, not only its text; the 24-hour clock test can now fail. |
| this report's commit | This report and its two index rows. |

## The uncommitted line the dead session left

The worktree had one uncommitted line: `.clickable {}` on the strip's sundown line at
`AvailabilityMapControlsUi.kt:573`. `git diff` showed this was the only difference from `ea722ff7`:
one file, one insertion. It was a revert probe for the touch test. Restoring the file from git
restored the committed forward change exactly, because there was no uncommitted forward work to lose.
CLAUDE.md's rule against restoring from git protects uncommitted forward work, and there was none.
The same probe was then run again under this session's runner (r01 below).

## What the walker sees

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`.

- **While recording**, the map's top strip has a second line under the compass readout:
  - "Sunset 6:42 PM · start back by 5:32" when the walk back is known.
  - "Sunset 6:42 PM · in 1 h 12 min · dark 7:13" when it is not.
  - "Sunset 6:42 PM · start back was 5:32" once that time has passed.
  - "Sun set 6:42 PM · dark in 21 min" after sunset.
  - "Dark since 7:13 PM" after dark, including a recording started after dark. It never counts down to tomorrow.
- **Hidden** until 2 h 30 min before sunset, or until the start-back time is less than 1 hour away, whichever comes first (-595).
- **Hidden** with no position (-596). "Sunset: finding your position…" never shows.
- **Return or Navigate tapped**: the strip goes, as it already did, and the line is the HUD's last row.
- **Navigating to a waypoint with no recording**: the HUD shows "Sunset 6:42 PM · in 1 h 12 min · dark 7:13". It is computed on screen and never has a start-back time (-593).
- **Settings > Sundown**:
  - A "Sundown alerts" checkbox row, on by default. Off stops the notifications only; the line stays.
  - "Dark under trees" with "Woods get dark before sunset. Alerts allow this much extra.", and radio rows for 30 min / 45 min / 1 h (default) / 1 h 30.
  - Changing the margin moves the start-back time on screen at once.
  - Both settings survive a recreated repository.
- **Times** follow the phone's 12/24-hour setting.
  - The first time on the line uses `DateFormat.getTimeFormat`, as the alerts do (`alert/AndroidAlertDelivery.kt:273`).
  - In 12-hour, later times drop AM/PM ("start back by 5:32"), as the owner wrote them.

## The verify items, answered from the code at `59c4dd85`

1. **Base.** `origin/main` is `ef269025`, the merge-base of `sundown-line`. Nothing has landed since.
2. **One source for the start-back time.**
   - `sundownLeaveByAt` (`domain/SundownLine.kt:66-67`) returns the turnaround minus the walk back, or the turnaround alone when the walk back is unknown.
   - The turnaround is sunset minus the margin (`domain/ComputeSundownCountdownUseCase.kt:67`).
   - The alert decision calls it at `domain/DecideSundownAlertUseCase.kt:72`. The watch's line calls it at `domain/SundownWatch.kt:304`, from the same tick and the same countdown.
   - The screen reads the watch's `shown` StateFlow. The service declares no `android:process` (none in `AndroidManifest.xml`), so the screen and the service share one process. `MainActivity` hands the flow to `TrackRecordingViewModel` (`sundownShown`), which copies it into `TrackRecordingUiState.sundownLine` for its own recording only.
   - One step re-derives instead of reading a tick. `SundownWatch.onMarginChanged` (`:158-162`) recomputes the turnaround as sunset minus the new margin, then calls the same `sundownLeaveByAt`. It decides and delivers nothing. This is the planner's acceptance in -593 ("A display-only watch method that re-derives the start-back time on a margin change with the same function"). It repeats the countdown's `sunset − margin` arithmetic in one line, and the report states that rather than calling it zero second computations.
   - The ViewModel's own unrendered countdown is removed.
3. **When "walk back known" is true.**
   - `SundownWatch.kt:254-257`: a `ReturnWalkingTime.Estimate` gives `About` or `AtLeast`. `Withheld` or no estimate gives `Unknown`.
   - The line has a start-back time exactly when the walk back is not `Unknown` (`:303-304`). The alerts read `walkBack.millisOrNull` from the same value (`:264`).
   - The walk back reads only a GPS fix, so a last known position gives a sunset and no start-back time. This is tested.
   - An "at least" walk back reads "start back by", per -593's "Just "start back by 5:32"".
4. **Civil dusk.**
   - `ComputeSundownCountdownUseCase.kt:55-66` finds the next −6° crossing after sunset.
   - After sunset, the countdown always looks for the *next* sunset. So the line:
     - takes a sunset the watch is holding, with its own dusk. This is the fix in `SundownWatch.kt:227`: the held copy used to keep tomorrow's dusk.
     - or searches backwards for today's sunset (`SunCrossing.previousDescendingCrossing`) and finds its dusk forwards (`civilDuskAfter`, `SundownLine.kt:114-122`).
   - A recording started between sunset and dark reads "Sun set … · dark in …". One started after dark reads "Dark since …".
5. **Strip and HUD.** See "Strip layouts" below.
6. **Settings.**
   - The section sits after Night Mode and before Photo Location (`ui/availability/AvailabilitySettingsUi.kt`, `SettingsContent`).
   - "Sundown alerts" is a checkbox row, the shape of that screen's other on/off settings. The owner said "switch". The planner chose the screen's own control and told the owner (-593).
   - The margin is a radio group, the shape `DistanceUnitSection` and `ThemeModeSection` use.
7. **Help.** There is no Help screen. The owner ruled on the horizon sentence: "Leave it out for now" (-593). It is not in this build.
8. **12/24-hour.** See "What the walker sees".
9. **Stops.**
   - None found that the build crosses.
   - "start back by" with no walk back cannot happen: the start-back time is `null` there and the words switch to the countdown.
   - Alert moments are pinned (see the revert checks below).

## Owner rulings this build follows, quoted

- Placement, -421: "Strip, then HUD (Recommended)".
- -592:
  - "Add "start back by" (Recommended)".
  - "30 min, 45 min, 1 h, 1 h 30 (Recommended)".
  - "Yes, write it (Recommended)".
- -593:
  - The watch change: "Yes, allow it (Recommended)".
  - Navigating without recording: "Sunset and dark only (Recommended)".
  - The horizon sentence: "Leave it out for now".
  - The "at least" walk back: "Just "start back by 5:32"".
- -595, the owner unprompted: "For sundown alerts, it wouldn't be useful in the morning or early noon so while navigating and tracking, those alerts can be clutter on the UI. Maybe have sundown alerts appear ~4 hours prior to sundown." Then: "Option 1, but change it from 4 hours to 2.5 hours before sunset".
- -596: "Yes hide it before a position is known".
- Gradle: "Go ahead and build it." This came to this session from the planner; it was not read at its source.

Two points need the owner's word:

- **Polar night.** The path does not cover it. The build writes "Sun stays down today" (`ui/availability/SundownLineText.kt`) and shows it whatever the window. Polar day, which has no sunset to approach, shows no line.
- **Checkbox, not switch** (above). The planner told the owner; no answer is recorded.

## Strip layouts, and the 80% map-chrome rule

- **Read.**
  - The strip has one call site, `CompactMapTab` (`ui/availability/AvailabilityCompactMapUi.kt:885`).
  - **Portrait**: full width under the search bar (`railPortEdge == null`).
  - **Landscape**: content-width in the top corner on the rail side (`contentWidth = true`, `:900-903`).
  - **Fullscreen**: the same strip. Nothing gates it on fullscreen, and it follows `topInset` up as the search bar slides away.
  - It is removed while navigating, when the HUD (`:994`) takes the line.
- **Placed.**
  - The line is a `Text` in a `Column` under the readout row, inside the strip's existing `Box` (`ui/availability/AvailabilityMapControlsUi.kt:427-575`).
  - In portrait and fullscreen it fills the strip's width. In landscape it is content-width, so the strip grows to fit the line when the line is wider than the readout. That is visible only on a device.
  - In the HUD it is the last row inside the HUD's existing `Box` (`ui/availability/NavigationHud.kt:360-368`).
- **80%.**
  - No new surface. The line draws no background.
  - It sits on the strip's fill (`MapIconStackButtonColorDark`/`Light`, `ui/map/MapChrome.kt:253,265`, both at `MAP_CHROME_OVER_MAP_ALPHA`) or on the HUD's fill (the same colours, `NavigationHud.kt:254-259`).
  - The text colour is opaque, as on the rest of the strip.
- **Touches.** The strip stays a plain `Box` with a background. The `Column` and the `Text` take no pointer input.
- **Tests.** Portrait, fullscreen and landscape each have a test that:
  - finds the line inside the strip;
  - checks it is laid out whole: one line, not ellipsized, every character visible, under native graphics;
  - samples real long-presses across the line's bounds and in a row under the strip, and asserts each one reaches the map.

  Landscape also asserts the strip is content-width.

**Two traps in reading this, both found while doing it:**

- **Legacy graphics measure text as one pixel per character.** The first landscape run reported the line 12.33 dp wide and cut off. That is 37 px for a 37-character line: Robolectric's legacy graphics measure one character as one pixel. Under native graphics it is 216 dp with all 36 characters visible. A content-width line can only be checked under native graphics. Under legacy graphics no cap could cut it off, so the check could not fail there.
- **`hasVisualOverflow` reports a whole line as overflowing.** Under native graphics it said the landscape line overflowed, with every character visible and nothing ellipsized: the paragraph is a fraction of a pixel wider than the whole pixels it is given. The check reads the line's ellipsis and visible end instead.

## Tests

- **Red first.** No red run of `ea722ff7`'s tests is recorded anywhere: the commit carries tests and code together, and the dead session's runs, if any, were not reported.
  - Running those tests against `main`'s code would not compile (`SundownLine`, `SundownShown` and the rest do not exist there), so that would show nothing.
  - Instead, the red half of each behaviour is shown by a revert check against the built code. Each one fails a named test with a message specific to its edit (below).
  - The alert-moment tests in `a9ca61fc` are guards, meant to be green before and after. Their commit message says they were green on `main` first.
- **Added.** 67 `@Test` annotations, 3,618 at `origin/main` to 3,685 here. That count includes the tests in `TrackRecordingSundownTest` that replaced the retired countdown's.
- **Added by this session.** Two strip tests (landscape, fullscreen), the laid-out checks, and a 24-hour clock test that can fail.
- **Fixed by this session.**
  - The 24-hour test used 18:42 UTC, which is 11:42 in this laptop's zone, where "h:mm" and 24-hour agree. The revert of the 24-hour branch first passed it (r18), so it was changed to 18:42 in the JVM's own zone, and the revert then failed it (r18b).
  - Three test strings wrote `${'$'}` where they meant `$`. The settings test's error log recorded the literal text "$message $error", and two failure messages printed names instead of values.
  - `ThemeModeSection`'s KDoc had been left orphaned above the new Sundown block (`AvailabilitySettingsUi.kt`). It is back above its function.
- No `@Ignore` was added, and no assertion was weakened.

## Revert checks

**The runner.** For each revert, the runner:

1. saves every file it edits;
2. applies a one-line (or two-line) revert and prints the diff;
3. deletes the old JUnit XML and runs the classes `*Sundown*` and `*SunCrossing*` (133 tests; 14 for the screen class alone);
4. counts `e:` lines in the build log, and refuses to cite any run that has them;
5. reads only XML written after the run started;
6. restores from the saved copy, never from git, and checks the sha256 against the saved copy;
7. confirms the working tree against `HEAD` is the same as before the run.

Every run below had 0 compile-error lines, a sha256 match, and an unchanged tree, except r19, noted.

| # | Behaviour | Revert | Failed (message) |
|---|---|---|---|
| r01 | Touches on the line reach the map (the dead session's probe) | `.clickable {}` on the line | the portrait, fullscreen and landscape touch tests: "at least 16 points sampled, not 5". The line became a control, so every point on it was skipped. |
| r02 | Same, as a touch consumer | `.pointerInput { detectTapGestures(onLongPress = {}) }` on the line | the same three: "a long-press at (16.0.dp, 71.0.dp) … must reach the map expected:<1> but was:<0>" (landscape at 527 dp) |
| r03 | Window: 2 h 30 min before sunset | 150 → 240 min (the owner's first 4 h) | 7, including "the two thresholds are the owner's numbers" (expected 9000000 but was 14400000) and "hidden at 2 h 31 min … expected:<false> but was:<true>" in the domain, text, screen, HUD and ViewModel tests |
| r04 | Window: start-back under 1 h | the start-back clause replaced by `false` | 4: "shown early when the start-back time is 59 min away" (domain and screen), "a start-back time already passed shows", "hidden outside both windows" (expected "Sunset 6:42 PM · start back by 2:42" but was null) |
| r05 | -596: no position, no line | `FindingPosition` shown, with "Sunset: finding your position…" restored | 4: "expected null, but was:<Sunset: finding your position…>", and both screen "finding your position never renders" tests, and "no position, and polar day, are hidden" |
| r06 | Margin re-derives the line at once (watch) | `onMarginChanged` keeps the old turnaround | the watch test and the end-to-end ViewModel test: "thirty minutes later … expected:<…324737> but was:<…524737>" (30 min apart) |
| r07 | Margin re-derives the line at once (wiring) | Settings' handler does not tell the watch | the end-to-end test (same 30 min message), and the settings test timing out waiting for the watch to be told |
| r08 | Dusk fix: a held sunset keeps its own dusk | the held copy keeps the computed (tomorrow's) dusk | "after sunset on a recording begun before it … dark at today's dusk": dusk 1791166304354 against 1791080015038, a day apart |
| r09 | Alerts off: the line stays | `if (!enabled) return` restored before the line | "with the alerts off … the line stays": ClassCastException, FindingPosition is not BeforeSunset |
| r10 | Arrived: the line stays | the arrived early return restored | "after arriving at the start the line is still published": still BeforeSunset after sunset |
| r11 | Start-back = the leave-by alert's time | the line uses the turnaround without the walk back | 4, including "exactly the alert's own time expected:<…460271> but was:<…660272>" (20 min, the walk back) |
| r12 | No walk back, no start-back time | the `Unknown` check removed | "with no GPS fix … no start-back time" and "the last known position gives the line before any live fix": expected null |
| r13 | Return or Navigate moves the line into the HUD | the HUD gets no line | 4 HUD tests: no node `navigation-hud-sundown-line` |
| r14 | Navigate with no recording: sunset and dark only | the screen-computed line removed | "Navigate with no recording shows sunset and dark only": no node |
| r15 | The margin survives (stored) | Settings' handler does not store the margin | "real touches … survive a recreated repository": expected 45 but was 60; end-to-end "stored expected:<30> but was:<60>" |
| r16 | "Alerts off" survives (stored) | Settings' handler does not store the switch | see below |
| r17 | Alerts off stops the notifications | the alerts-off gate before deciding removed | 7, including `SundownWatchTest` "with the alerts turned off nothing fires", `TrackRecordingServiceSundownTest` "alerts off: nothing on the sundown channel", and four alert-moment tests |
| r18 | 24-hour clock | the short time always "h:mm" | **passed**: the test could not fail in this zone (see Tests); not evidence |
| r18b | same, after the test fix | same | "the phone's clock, 24-hour …": expected 18:42 but was 6:42 |
| r19 | After dark: no countdown to tomorrow | the backward search skipped | **compile errors (4 `e:` lines, a lost smart cast); not cited** |
| r19b | same, rewritten to keep the smart cast | `position != null && false && …` | 5, including "a recording started after dark says dark since today's dusk" and "in the small hours, still dark since the evening's dusk": BeforeSunset, not DarkSince |
| r20e | The line laid out whole (positive control) | a 60 dp width cap on the line | portrait, fullscreen and landscape: "not ellipsized: … visible end 4 of 36, ellipsized true" |

**r16 is only partly evidence.**

- In two of three runs, the settings test timed out first, at its wait for the watch to be told 45 min (`SundownSettingsTest.kt:201`). That wait is about the margin, not the switch, so that failure does not belong to this edit.
- In the third run, with the wait wrapped to print its state, the wait passed and the test failed at the switch's own assertion ("expected:<false> but was:<true>").
- The same test is green forward in 5 of 5 runs.
- Why skipping the switch's writes delays the margin's is not known. It is recorded, not explained.

Earlier landscape runs of r01, r02 and r20 under legacy graphics are superseded by the runs above. The forward change was confirmed present after every check: the sha256 matched, the tree against `HEAD` was unchanged, and the forward suite is below.

## Full suite

- **Before**: not run by this session. Taken from the arrived-alone report (`docs/navigation/2026-10-06-arrived-alone-report.md:142`): **3,823 tests, 0 failures, 0 errors, 24 skipped**. That figure was measured on the arrived-alone branch's merged tree (`b1f888a5`). That tree is the same as `ef269025`'s in `app/`: `git diff 428c6096 ef269025` is empty, and `428c6096` adds only docs to `b1f888a5`.
- **After** (`59c4dd85`, `--rerun`): **FULL_RESULT**.

## Device-only (listed, not run)

- Where the line sits against the real top inset in portrait, landscape and fullscreen. Robolectric reports zero insets (CLAUDE.md).
- In landscape, how wide the strip grows to fit the line, and whether it reaches the search bar on a narrow phone.
- A real sunset on a walk: the line from 2 h 30 min out, "start back was", "Sun set … dark in", "Dark since".
- That the line moves at once on the phone when the margin changes in Settings mid-recording.

## Not done or not verified

- No phone, no adb.
- The full suite on `main` before was not re-run. The count comes from the arrived-alone report, as stated.
- `a9ca61fc`'s claim that the nine alert-moment tests were green on `main` first was not re-run by this session. They are green here, and r17 shows that four of them fail when the alerts-off gate is removed.
- r16's timeout is unexplained (above).
- No recorded red run for the tests in `ea722ff7`. The revert checks stand in for it, as stated under Tests.
