# Back by: build report (dispatch 2026-09-28-645, plan task T15)

Branch `back-by`. The dispatch is `prompts/preserved/2026-10-07-04.md` (RECORD intent -645). Its amendments and the
owner's answers are in RECORD -646 to -649. The hand-over is -687, the build order -689, and the width question -690
(the owner: "Measure first, then decide"). This report covers the second coder's work: the merges, the build, the
tests, the revert checks and the measurements. The first coder's code and tests are at `29074703` and had never been
compiled.

## What is on the branch

These pieces are built and tested. They were written by the first coder and finished here:
- `BackByWatch`, driven by the recording service's 15 s tick. It needs no exact-alarm permission.
- The alert, on its own channel, with "I'm back" and "+30 min" sent straight to the service.
- The three-dot quick menu at the strip's far right, and left of the X in the navigation display.
- The "Back by" line, shown in the last hour.
- Three buzzes for Back by and for the sundown alerts.

## Merges (all of `origin/main`; no unmerged branch)

1. **`cf9c6a2c`, main at `f6fc6c91`** (failure-fixes, motion Parts 1 and 2, data parts A and C). Five files
   conflicted. Every conflict kept both sides:
   - **`AppContainer.kt` imports:** `BackByWatch` sits beside `SettingsResetNotice` and `RecordingHalts`.
   - **`TrackRecordingService.kt` imports:** `NotificationManagerCompat` stays, for the back-by cancel.
     `ContextCompat` goes, because main moved its only use.
   - **`TrackRecordingService.kt` fix path:** main's guarded sundown call stays. The back-by fix call now goes through
     `RecordingWatches.backByOnFix` under `guardWatch`, which is what RECORD -674 asked for.
   - **`TrackRecordingViewModel.kt` init:** both collectors stay. `setBackBy`, `clearBackBy` and `copyBackBy` sit
     beside `onRecordingHalted`.
   - **`NavigationHud.kt`:** the three-dot button stays left of the X, and the X is now main's `BouncingIconButton`.
   - **`AvailabilityCompactMapUi.kt`:** the display keeps `backByLine` and `quickSettings`. Its position modifier is
     main's, which motion Part 2 moved onto the slide.
2. **`4f7631ac`, main at `a71b57ac`** (data part B). This merge had no text conflicts.
3. **`a2d64279`, main at `d223728b`** (motion Part 3 and the small fixes). One conflict, in `AndroidAlertDelivery.kt`:
   - **The vibration seam:** both sides changed it. Back by made it `(Context, Alert)` so the buzz pattern can follow
     the alert's kind. The small fixes kept `(Context, Boolean)` and added the ringer and Do Not Disturb seams after it.
   - **How both are kept:** the main constructor is now private. It takes the ringer, Do Not Disturb and the
     `(Context, Alert)` vibration. The small fixes' five-argument and three-argument test constructors keep their
     `(Context, Boolean)` seam, which is adapted inside, so their tests are unchanged.
   - **Why the parameter order differs:** the private constructor's parameters are ordered apart from the
     five-argument one, because the two would otherwise clash on the JVM once their function types are erased.

## Changes in this build

- **The guard.** `RecordingWatches.backByOnFix`, called under `guardWatch("The back-by watch", …)`.
- **The three-dot button and motion Part 1.** The planner named `BouncingIconButton`. The first run showed that it
  takes away the button's corners:
  - **What happened:** a real touch 3 dp in from the top-left corner did not open the menu.
  - **Why:** `BouncingIconButton` is Material's `IconButton`, which clips itself to a circle, and a clip is part of
    hit testing.
  - **What was built instead:** the button uses Part 1's own pieces, as `MapBarIconButton` does. The `clickable` sits
    on the unclipped 36 dp square, `pressBounce` is on the icon, and `PressHighlight` draws the press as a circle.
  - **Result:** the bounce and the round press are there, and the touch area is still the whole square.
- **The "Back by" line.** On the strip and in the display it now uses the sundown line's pop-up grow and `WordSwap`.
- **Settings writes.** The menu's sundown and off-track rows call the same `AvailabilityViewModel` functions as
  Settings. Those write through `DataStoreSundownPreferencesRepository` and
  `DataStoreOffTrackReminderPreferenceRepository`, which are built on `settingsDataStore`. Back by stores nothing.
- **A skipped buzz, recorded honestly.** Back by overrides silence like the sundown alerts, so the small fixes'
  `vibrationSkipReason` gives the same answers for it:

  | Phone state | What is recorded |
  |---|---|
  | On silent | Not skipped: it plays with alarm usage |
  | Do Not Disturb, alarms only or priority | Not skipped |
  | Do Not Disturb, total silence | Skipped: "Do Not Disturb" |

  `BackByWatch`'s "partly delivered" log used to say "vibration issued" for a skipped buzz. It now says
  "skipped: <reason>", as `SundownWatch` does.

## Tests added or changed

- **`TrackRecordingServiceWatchFailureTest`.** A fourth case: a back-by watch that throws on every fix, through the
  real service. All 20 points are still stored.
- **`NavigationHudQuickSettingsWidthTest`.** It also reads the turn and the distance's kind. It adds data B's longest
  first row ("Sharp right · 169°", "1280 ft by trail"). This test is red; see the width clash below.
- **`AvailabilityScreenQuickSettingsTest`** (back-by's own; it first ran in this build):
  - **Missing import:** the `click` import was added.
  - **Touch test:** it now composes the screen afresh between samples. Under Robolectric, neither Back sent to the
    activity nor a touch on the map's window reaches the menu's popup window, so neither one closed it.
  - **The dot:** it is now read in the unmerged tree, because it sits inside the clickable square.
  - **New test:** held down, the icon dips under a round press and springs back.
- **`BackByWatchTest`.** New: a skipped buzz is logged as skipped, with its reason.
- **`BackByVibrationRecordTest`** (new). The back-by buzz's record on silent, under total silence and under alarms
  only, through the real `deliverReporting`.

## Revert checks: nine, all bite

- **How they ran:** the runner saved a copy of the file and made a one-line edit. It refused to read results if the
  compile log had an `e:` line; none did. It deleted the old JUnit XML and ran the named classes. Then it restored the
  file from the saved copy and confirmed the file matched the forward version.
- **Forward change still present afterwards:** true for all nine.

| Revert | Failures, read from the XML |
|---|---|
| R1 back-by fix call unguarded | 1: "every fix's point is stored although BACK_BY_ON_FIX threw on each expected:<20> but was:<0>" |
| R2 button clipped to a circle | 1: "a real touch at (351.0, 48.0) in DpRect(left=348.0.dp …) opened the menu" (the top-left corner) |
| R3 no press bounce on the icon | 1: the press test, "Key not present: PressBounceScale". The bounce's own semantics go with it, so the test stops before its "dipped" assertion. It still fails only because the bounce is gone. |
| R4 Back by buzzes off-track's two | 1: "Back by vibrates three pulses", expected 6 elements, got 4 |
| R5 sundown alerts buzz two | 1: "the sundown alerts vibrate three pulses", HEADS_UP expected 6, got 4 |
| R6 skipped buzz logged as issued | 1: the new skipped-buzz log test |
| R7 arrival counted without Return | 1: "without Return, being at the start does not end it": "still fires expected:<1> but was:<0>" |
| R8 +30 min counted from the old time | 2: the watch's +30 min test and the service's, through the notification's own intent |
| R9 line shown outside the last hour | 1: "the strip shows Back by … only in the last hour": "61 min away: no line" |

## The width clash (RECORD -690): measured, nothing changed

The layout is unchanged, and neither data B's tests nor the width test were touched.

**How it was measured:** the real `AvailabilityScreen`, data B's own setup and native graphics. Each text node's box
was compared with the width it needs: `maxIntrinsicWidth`, and the characters actually drawn. The baseline is the same
build with the button removed for one run, restored from a saved copy, so it stands in for main's layout. The
measuring test was temporary and is not committed. "Short" means needed width minus the box width.

### 360 dp portrait

**With no button, every line is whole.** With the button:

| Where | Line | Box / needs | Short | Shows |
|---|---|---|---|---|
| Strip (MGRS and decimal) | heading "315° NW" | 32.0 / 50.75 (decimal: 36.5) | **18.75** (decimal 14.25) | "315" (decimal "315°") |
| Strip (MGRS and decimal) | elevation "9843 ft" | 30.5 / 42.75 (decimal: 33.5) | **12.25** (decimal 9.25) | "98" (decimal "984") |
| Display, first row | kind "by trail" | 30.0 / 40.25 | **10.25** | "by " |
| Display, first row | status "≈ 1250 ft straight" | 87.0 / 102.25 | **15.25** | "≈ 1250 ft str" |

- **What stays whole:** "Sharp right · 169°", "1280 ft", the labels "Facing" and "Alt", the coordinates (MGRS and
  decimal) and the sundown line.
- **Where the button sits:** in the strip at x 324 to 360. In the display at x 256 to 292, left of the X.
- **The strip's height:** 18 dp without the button, 36 dp with it.
- **Other states that cut** (from `NavigationHudQuickSettingsWidthTest`): "Unable to calculate route" (shows "Unable
  to calculate r"), "Location services unavailable" (shows "…unava") and "Last seen 23 h ago, finding GPS…" (shows
  "…findi") are cut only with the button.
- **Cut either way:** "No origin waypoint for this track" is cut with or without the button, so the button is not
  the cause there.

### 384 dp portrait (the S22 Ultra)

**Every line is whole, with the button,** on both the strip and the display, in MGRS and decimal. The tightest
margin is 0.17 dp.

### 780 × 360 dp landscape (the S22, as data B measures it)

The small fixes already make facing and altitude give way here, so some lines are cut with no button at all.

| Where | Line | Without the button | With the button |
|---|---|---|---|
| Display | "1280 ft" | whole | **short 8.17**, shows "128" |
| Display | "by trail" | short 4.5, shows "by tr" | **box 0, nothing drawn** |
| Display | "≈ 1250 ft straight" | short 7.5, shows "≈ 1250 ft strai" | **short 55.5**, shows "≈ 125" |
| Display | heading "281° W" (MGRS) | short 1.5 | short 1.5 (same) |
| Display | elevation "9843 ft" (MGRS) | short 0.83 | short 0.83 (same) |
| Strip | heading | short 17.5 (decimal 13.17) | identical |
| Strip | elevation | short 11.5 (decimal 8.5) | identical |

**The landscape strip is identical with and without the button because the button is not there.** At 780 × 360 it is
laid out 0 dp wide at the strip's right edge (x 700). Its clipped bounds are empty, so it cannot be seen or touched.
The strip is still 36 dp tall, which shows the button was measured.

- **Why:** in landscape the strip is content-width. The column of readings is not weighted, so it takes the whole
  capped width the small fixes allow and leaves the button nothing.
- **Wider windows:** at 823 × 384 (the quick-settings landscape test) the button shows, because the cap is wider.
- **Consequence:** on a 780 dp landscape screen today there is **no way into the quick menu from the strip**. While
  navigating, the display's button does show, at x 596 to 632.

## Other existing tests this build breaks: reported, not touched

**`AvailabilityScreenMapIconStackTest`, two tests:**
- "the search dropdown starts below the compass strip, not over it": the dropdown's top is 67 dp, the strip's bottom
  85 dp.
- "the icon bar cannot be dragged above where the search dropdown would start": the bar's top is 71 dp, the strip's
  bottom 85 dp.

**Cause** (read from the code):
- The dropdown's offset (`AvailabilityCompactScaffold.kt`, `searchDropdownTopOffset`) and the icon bar's top limit
  (`AvailabilityCompactMapUi.kt`, `topLimitPx`) both assume the strip is one text line tall (`compassStripClearance`,
  the height of "Mg").
- The button's 36 dp floor makes the strip 18 dp taller than that, so both now reach up over the strip's lower half.

**Same assumption elsewhere, not checked by any failing test (read, not run):** the observation bubble's `minY` and
the taxon and journal chips' top padding also use `compassStripClearance`. They will probably sit under the strip's
lower 18 dp too.

**Not fixed here:** a fix moves other controls (the dropdown, the icon bar's limit, the chips and the bubble). The
dispatch made a change to another control's position a stop.

## Full suite

The whole of `:app:testDebugUnitTest`, read from the JUnit XML (543 files): **4,300 tests, 8 failures, 0 errors, 24
skipped**. No `@Ignore` is added on this branch. The 24 skipped are existing `@Ignore`s; I did not count main's for
comparison.

All 8 failures are the two causes above:
- **The width clash (6):**
  - the four 360 dp tests in `AvailabilityScreenNavigationWordsTest` (data B's);
  - `NavigationHudQuickSettingsWidthTest`;
  - `AvailabilityScreenReturnRouteTest > the whole sentence shows at 360 dp`: "Unable to calculate route" is cut
    beside the button. This is the same figure the width test shows ("Unable to calculate r").
- **The taller strip (2):** the two `AvailabilityScreenMapIconStackTest` tests.

None of the failing tests was touched. `AvailabilityScreenNavigationWordsLandscapeTest` passes. Its assertions do not
cover the landscape strip's missing button or the display's cut "by trail".

## Device-only, unverified

On the S22 (dispatch, "Device-only"):
- set +1 h with the time adjusted so it fires during the check;
- the alert with the phone silenced and the app swiped away;
- both buttons;
- the reminder ending on arrival after Return.

Also device-only:
- the round press and the bounce, as they look on the phone;
- whether an open menu closes on Back and on a map tap on the phone (Robolectric cannot show it);
- what the record says under each Do Not Disturb mode on the real phone.
