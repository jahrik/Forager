# Fan-flicker completion report (dispatch 2026-09-28-369, amendment 2026-09-28-370)

Coder: Sonnet 5.5 (`claude-sonnet-5-5`). Branch `fan-flicker`. Evidence: `~/Zynergy/device-evidence/2026-10-01-fan-flicker/` (outside the repository, on purpose: it includes recordings that open with the phone's own screen). Part 1 is `2026-10-01-fan-flicker-part-1-report.md`; this report adds Part 2 and does not repeat it.

**Two things are open until the owner answers, and the fix is not to be called done before they are:**
1. **The pass criterion.** With the planner's 4 dp floor the "icon without circle" count is not 0 on the fix build, which the amendment makes a stop. I applied no other floor. See "The criterion" below.
2. **The phone.** The S22 is on the fix build, not the base. Putting the base back is a version-code downgrade (`INSTALL_FAILED_VERSION_DOWNGRADE`, 2329 over 2334); the only way I know is `install -r -d`, which the dispatch does not list, so I did not run it. The planner has asked the owner.

## What landed
- `1ead81ea` on `fan-flicker` (pushed): `PlacementTransitions.kt` (`transitionWithoutPlacementFade`, `disableSymbolFade`), one call in the style-load callback (`SightingsMap.kt:693`, first line of the `setStyle` callback at `:691`), and `PlacementTransitionsTest` (3 cases). 3 files, 72 insertions, 0 deletions. Nothing on the dispatch's "Do not touch" list is in the diff: `FAN_DURATION_MS`, the easing, `FanOutLayers.kt`, the fan's geometry and touch areas are untouched. Before it: `2b5e9f88` (the stub and the test, pushed un-run), `433b7696` (merge of `origin/journal-redesign` at `8ab13f3d`, docs only).
- The fix: `style.transition = TransitionOptions(current.duration, current.delay, false)`: MapLibre's placement (symbol-fade) transition off, duration and delay kept. Applied wherever a style finishes loading, so it holds for every basemap, day and night, online and offline (not shown on a second style by any test here; see below).
- Alternatives rejected, and why: the Part 1 report's options 2, 3 and 4 (untested, or a risk to the look at rest), and test B (not run; test A with four sources already had 0 mismatch frames). The planner and the owner chose option 1.

  > **Superseded by amendment 3:** test B was not needed for what this fix did, but the single source was then tried for the circles-ahead fault and did not close it (Amendment 3 section below).


## Verify-before-building (the amendment's two items and its code list)
- **Transition values before the change, read from a loaded style on the S22:** `duration=300 delay=0 placement=true`, exactly as expected (`builds/fix-with-log-logcat.txt`; one style load logged). The temporary `Log.i` that read them is not in the recorded or committed build: it is in the log-build apk's dex (1 hit for "before: duration") and absent from the final apk (0 hits).
- **One place:** `setStyle` is called once (`SightingsMap.kt:691`, one `MapView` at `:297`). `grep -rn "TransitionOptions\|\.transition\b\|setTransition" app/src/main` found nothing before the fix, so nothing replaces the style's transition afterwards. The inline style JSON (`BasemapStyles.kt:170-216`) has no `"transition"` key; I did not read the offline style's served JSON, and the logged values (300, 0, true) are what it, or the default, gave.
- **Test A's keying:** its edit was in the fan-draw effect, keyed on `loadedStyle`, `mapLibreMap` and `focusedObservationId`, so it ran from the style's load. Confirmed from my own edit.
- **Symbol layers:** `FanOutLayers.kt:99` and `markerSymbolLayer` (`SightingsMap.kt:1177`); no `textField` in code. **A stale comment found, not edited (outside this dispatch):** `BasemapStyles.kt:19-20` says `SightingsMap` adds a `SymbolLayer` with a `text-field` for the numbered foraging-area markers; no such code exists.
- **Basemap styles:** the two inline builders I read, `BasemapStyles.kt:170-216`, are raster only. The offline style is, per the planner, 57 layers with no symbol layer (read from the served document, not from the phone); I did not fetch it.
- **The location dot.** `LocationComponentActivationOptions.Builder` defaults `useSpecializedLocationLayer` to false (`javap -c`, `iconst_0` at its constructor); the app's activation call (`SightingsMap.kt:1513-1514`) does not set it. So the library uses `SymbolLocationLayerRenderer`: symbol layers plus a circle layer for the accuracy ring (`LayerSourceProvider` instantiates `SymbolLayer` and `CircleLayer`; the `LocationIndicatorLayer` path is only taken when the specialised flag is on: inferred from the `LocationLayerController` constructor's branch on that flag, which I read in bytecode and did not observe running). The new setting therefore **reaches the dot's symbol layers**. What it changes about the dot while it moves or turns I did not observe and did not try to produce: that is the owner's check.
- **The dot at rest, by eye:** same on both builds (`dot-base-vs-fix-crop.png`; the stills are post-launch screenshots, `dot-base-still-full.png` from the first map screenshot of this work, `dot-fix-still-full.png` from the fix build). The only difference is the heading wedge, which moved between the stills. My automated dot detector was **not usable**: it fires on blue water at the map's first frame (the river), so it reported the dot at the map's first frame on both clips. The cold-start comparison of the dot is by eye and the stills.

## The fan, before and after (same stack, zoom, night style, same camera as restored; animator 1 and 5; `measure.py` for all)
`measure.py` is the script in the evidence folder. It was changed only before the base recordings were measured with its final form (streaming decode, fold mode); I re-ran base a1 open with the final version and it reproduced the earlier numbers exactly. Counts are against the thresholds in the Part 1 report; n = 1 recording per cell.

| | circle-without-icon | icon-without-circle (raw) | direction reversals | rest / gone | icons at final brightness |
|---|---|---|---|---|---|
| **base a1 open** | 8 | 1 | 7 | rest 242 ms | 259 ms **after** rest |
| **fix a1 open** | **0** | 3 | **0** | rest 233 ms | 175 ms, **58 ms before** rest |
| base a1 fold | 4 | 1 | 4 | gone 209 ms | n/a |
| **fix a1 fold** | **0** | 2 | **0** | gone 175 ms | n/a |
| **base a5 open** | 17 | 6 | 0 | rest 1058 ms | 774 ms, before rest |
| **fix a5 open** | **0** | 9 | **0** | rest 1042 ms | 624 ms, before rest |
| base a5 fold | 12 | 0 | 4 | gone 909 ms | n/a |
| **fix a5 fold** | **0** | 11 | **0** | gone 917 ms | n/a |
Test A (the Part 1 style-wide line, before the fix was a function) gave the same pattern: 0 circle-without-icon and 0 reversals in all four cells; icon-without-circle 0, 2, 10, 10. (`floor.py` counts 9 for test A a5 fold against `measure.py`'s 10: its window ends at 1.2 times the tween, `measure.py`'s at the frame the fan layers are gone.)

**Pass (amendment): 0 circle-without-icon frames, 0 reversals, icons at final brightness by the frame the fan rests: met in all four cells.** The fourth item, "0 icon-without-circle frames with the floor", is not met; see next.

## The criterion (open: the owner decides)
- **The planner's 4 dp floor, as measured.** Drawn radius = 18 dp (`FAN_CIRCLE_DIAMETER_DP` 36 / 2, `FanClarity.kt:31,66`) times the fold progress, the progress from `FastOutSlowInEasing` over 250 ms times the animator scale (`MarkerFanOutState.kt:81`). Cross-checked against the circle mask's outer extent (`floor.py`; the two estimates agree within about 3 dp). Density 600 dpi (`wm density`), so 4 dp is 7.5 px in a 720-wide recording. Every frame flagged "icon without circle" has a circle of **6 to 13 dp** (a1 open 11 to 13; a5 open 6 to 9; folds 9 to 12): above 4 dp. So the floored count equals the raw count and is not 0: fix a1 open 3, a1 fold 2, a5 open 9, a5 fold 11 (base: 1, 1, 6, 0). The amendment makes that a stop. I did not apply a different floor.
- **What the flagged frames look like** (full-resolution crops, fix a1 frames 277 to 283 and a5 frames 305 to 330): the glyph tiles are already full size while the circles have grown to only 6 to 13 dp, so each disc is largely hidden behind its tile; the dark discs come out from behind the glyphs a few frames later. Icon and circle coverage rise together with no reversal; nothing dims, blinks or fades in late. My reading, which the planner shares and the owner must confirm, is that these frames measure -299's growth from nothing, not the fault.
- **The number for the owner's choice:** the first frame at which a disc is visible beside its glyph (`visible.py`; discs = darkened pixels outside the glyphs, the legs and the folded stack's footprint, at least 1500 px in this and the next frame; threshold set by eye on fix a1 frames 279 to 283, where the counts are 246, 581, 1081, 2083 and the eye sees nothing at 279 and 280, a faint halo at 281, clear at 282; a constant artifact of about 1080 px from the stack's removal is why 1000 px was too low). Open: fix a1 **frame 282, 133 ms, model radius 14.6 dp (progress 0.81)**; fix a5 **frame 329, 533 ms, 12.0 dp (0.66)**; base a1 278 (12.2 dp); base a5 321 (9.4 dp); test A a1 285 (8.2 dp); test A a5 327 (11.3 dp). So on the fix a circle shows beside its glyph at about 12 to 15 dp radius, about a glyph tile's half-width. Fold, last frame with a visible disc: fix a1 800 (50 ms, 15.6 dp), fix a5 1222 (250 ms, 15.6 dp); base a1 805 (11.5 dp), base a5 1209 (11.7 dp); test A a1 802 (14.4 dp), test A a5 1196 (16.0 dp). **Caveat for the fold figures:** the fold's start frame is when the metric first moved, which lags the true start (the ease begins slowly), so the fold's model progress and radius read high.
- Options for the owner: (1) keep 4 dp and treat the fix as failing; (2) floor the criterion where a disc can show beside its glyph (about 12 to 15 dp from the numbers above), which the fix meets; (3) accept the raw icon-without-circle counts as -299's growth, as the amendment's own note for the owner already says. My measurements cannot choose between them.

## -299 and -318 on the fix build (a1 recording, full-resolution crops of fix a1 frames 268 to 274 and 812 to 830)
- **-299, no icon jump at the open's first frame or the fold's last:** open: the camera tile is the front glyph in the same place as in the original stack (frames 270 to 272); fold's end: frames 816 to 818 the tile is in front, at 819 the originals return with the puck over the same tile in the same place. No position jump. Viewed at a1 only; a5 only numerically (below).
- **-318, the front glyph does not change at either end:** the camera tile is in front before the open (frame 250), in the first fan frames, in the last fan frames and after the fold. Unchanged.
- **A residual, not caused by this fix and not touched by it:** at the open's first frame there is one frame (animator 1, frame 269) or two (animator 5, frames 265 and 266) with no glyph at the stack: the originals are hidden before the copies are drawn, and only the puck shows. The base has it too (frames 265 and 268). It is a position-neutral gap of one or two frames, outside this dispatch's list; it is the "three blank frames at the open's start" that -319 noted, now shorter. The fold's end has no such gap on the fix.
  > **Correction, Amendment 2 review round (below):** "The fold's end has no such gap on the fix" rested on a few clips and was wrong. A one-frame empty stack at the fold's end also occurs, at a low and varying rate: 1 of 5 runs of the first fix, 2 of 6 of the Amendment 2 build as first handed over. It was fixed in the review round.

## Outside the fan: the cold start (the owner will judge the choice by this)
- **Definition (the planner's):** a cold start is the first launch after an install, read by `pidof com.zynergylabs.forager.app` empty immediately before the recording, the same condition for both clips; a first launch after an install may be slower than an ordinary cold start.
- **Clips:** `ff_base_cold.mp4` (base, 13.9 s) and `ff_fix_cold.mp4` (fix, 13.9 s), each launched with `am start` while `screenrecord` ran; same night style, the camera the app restores. Sheets from the first full-bleed splash frame: `cold-sheet-base-from-splash.png`, `cold-sheet-fix-from-splash.png`.
- **What differs: nothing I can see.** Both: splash, then a blank loading frame, then the map with the marker stack and the dot arriving together. The marker goes from absent to drawn in one step on both (base: 0 to 1662 pink pixels of 1666 at 3821 ms in the clip's time; fix: 0 to 1658 of 1663 at 2904 ms), no fade-in on the base, so there is no fade for the fix to remove at a cold start. The absolute times differ (map at 3305 ms against 2622 ms) and that is a difference between two launches, not evidence about the setting.
- **Limits of that finding:** one clip each, one stack, the pixel counts from `cold_events.py` and not a frame-by-frame eye check, and first launch after an install. It cuts against the Part 1 report's and the amendment's expectation that markers "pop in on load".
- **Unobserved, with the owner:** a layer switched off and on in the Layers sheet, markers coming into view on pan and zoom, and the dot while it moves or turns. I did not try to produce them on the phone.

## Tests
- **Test first.** `PlacementTransitionsTest` on the stub (`transitionWithoutPlacementFade` returning its input), run at 11:39Z: 3 tests, 2 failed, with "placement transitions must be off" (`PlacementTransitionsTest.kt:20`) and the second case's flag assertion (`:29`); the duration and delay assertions passed on the stub, and the third case passes either way (it is not a bite test). Build log: 0 `e:` lines. XML `tests/stub-failing-run.xml` and `tests/stub-failing-run-build.log`.
- **On the fix** (11:41Z): 3/3 pass (`tests/fix-passing-run.xml`).
- **Revert check, from a saved copy** (`/tmp/ff/revert/PlacementTransitions.kt.saved`, sha256 `0a28b811…`): the function body reverted to `current`; `compileDebugKotlin` and `compileDebugUnitTestKotlin` ran, **0 `e:` lines, read before the XML**; the XML is fresh (the old one deleted first, timestamp 11:55:29Z matching the run); 2 of 3 fail with the placement-flag messages, which only this revert can produce. Restored from the saved copy, not from git: sha256 equal, `TransitionOptions(current.duration, current.delay, false)` back at `PlacementTransitions.kt:21`, the call at `SightingsMap.kt:693`, `git status` empty. Evidence `tests/revert-run.xml`, `tests/revert-run-build.log`.
- **What no unit test reaches:** that the `setStyle` callback calls `disableSymbolFade`, that the map then stops fading symbols, and that it holds on a second basemap. A `Style` cannot be built under Robolectric. The recordings are the only evidence for the first two, and the owner's check after a basemap swap and a night switch is the only evidence for the third.
- **Full unit suite, committed tree `1ead81ea`:** BUILD SUCCESSFUL in 5m 11s, 0 `e:` lines; 407 suites, 3278 tests, 24 skipped, **0 failures, 0 errors** (counted from the XML in `tests/full-suite-xml/`, saved before the revert run). No owner-held flake and no `DiagnosticsPanelTest` failure occurred in this run. `assembleDebug`: 0 `e:` lines for both fix builds (`builds/fix-with-log-build.log`, `builds/fix-final-build.log`).

## Provenance of the installed fix apk
It reads `1.0.2334+g2b5e9f88.dirty`, built before the commit. The log-build and the final build share that versionName; they differ by apk hash and `lastUpdateTime` (final: apk `db0e0c8b3a83c78b…`, installed 04:42:00). How I know the tree it was built from is the tree at `1ead81ea`: the last source edit was the log line's removal at 04:41:21; the build and the test run followed it; the apk was copied at 04:41:42 and the commit made at 04:41:43 in the same command, with no edit between (file mtimes, `stat`); `git diff HEAD` is empty and the committed `PlacementTransitions.kt` has no log; and the final apk's dex has no "before: duration" string while the log-build's does. I did not rebuild at `1ead81ea` and compare dex byte for byte, so it is the timestamps plus the string check, not a byte comparison.

## The phone
Start: `1.0.2306+g19f159fd`, user 0 ceDataInode 2259049, `forager.db` sha256 `e1188b00f0da4b713e4ef6d6626b90139247a886460e5bf317d39996c8e2b1db`, animator 1.0; data copied read-only (`data-copy/`). Installs in order (`adb install -r --user 0`, own debug builds only; the three values read back after each, always inode 2259049 and the db hash above):

| # | build | apk sha256 (first 16) | versionName read back |
|---|---|---|---|
| 1 | base `b6617c5c` | `c19d4e3195c92ec6` | `1.0.2329+gb6617c5c` |
| 2 | test A (throwaway) | `8887687baae5904f` | `1.0.2329+gb6617c5c.dirty` |
| 3 | base again | `c19d4e3195c92ec6` | `1.0.2329+gb6617c5c` |
| 4 | fix with the temporary log | `93bead787778e0da…` | `1.0.2334+g2b5e9f88.dirty` |
| 5 | **fix final (current)** | `db0e0c8b3a83c78b…` | `1.0.2334+g2b5e9f88.dirty` (lastUpdateTime 04:42:00) |
| 6 | base again: **refused**, `INSTALL_FAILED_VERSION_DOWNGRADE` | `c19d4e3195c92ec6` | unchanged (no change to the phone) |
- Settings: animator scale set to 5.0 three times (base, test A and fix recordings) and read back as 1.0 after each; nothing else changed. No swipes anywhere; every tap was on the stack after a screenshot, and every Back was sent after reading Forager's window had focus.
- End state **as it stands**: fix build installed (the base restore is open, above), app running (pid 1506, started by my cold-start launch), `forager.db` sha256 and ceDataInode equal to the start, animator 1.0, no recordings left on the phone. The fix apk is kept (`builds/fix-final.apk`, sha256 above).

## The four disclosures
- **Confirmed vs inferred.** Confirmed by measurement (n = 1 per cell): the base fault at animator 1 and 5, open and fold, and that the fix removes the circle-without-icon frames and the reversals and brings the icons to final brightness by the time the fan rests. Confirmed by unit test: the function's output. Inferred: the mechanism inside MapLibre; the fix's effect on every other symbol layer and on the dot while it moves (not observed); that it holds on a second basemap (no test). Read in the SDK by `javap`: the `enablePlacementTransitions` flag, the location component's renderer choice, and the absence of a per-layer fade control (not a reading of MapLibre's native placement code).
- **Could not determine.** Why one glyph stays solid in the base clips (Part 1); the S26 at 120 Hz; what a user sees elsewhere on the map; the first-frame gap's cause (inferred: the originals are hidden a render before the copies are drawn).

  > **Correction:** the S22 was in 120 Hz mode too; see the note under Conditions in the Part 1 report.

- **Premises that were wrong.** The pass criterion (the Part 1 report's flaw, then the 4 dp floor); the amendment's and the Part 1 report's expectation that markers pop in at a cold start (not seen on the base); the comment at `BasemapStyles.kt:19-20`.
- **Decided beyond scope.** The thresholds in `measure.py`, `floor.py` and `visible.py` are mine, set against stills; the 1500 px disc threshold; deleting three scratch frame caches of mine on the `/tmp` tmpfs when it filled (they freed nothing on `/`); one contact sheet that included the pre-launch frames of the base cold start was made, viewed and deleted, at the planner's instruction; and two cold-start sheets regenerated to start at a full-bleed splash frame. The cold-start clips themselves open with the phone's own screen (a personal photo); they were left untrimmed in the local evidence folder, outside the repository, on the planner's instruction.

## For the owner afterwards (device-only, on the merged build)
As the amendment lists them, plus: the first frame or two of an open may show an icon before its circle has grown, and the stack is absent for one frame as the fan starts (both predate this fix); the pass criterion above is yours to settle.

---

# Amendment 2 (continuation -371): the blink at the open's start

**Status: done, with one flagged frame and one case not reachable on the phone (both below). Ready to merge; the planner merges.**

> **Superseded by the review round below:** the commit this section named (`a5185a2f`) was not merged. The planner found a case the first wiring broke (a restart of the effect with a fan up), and the fold's end proved to have the mirror gap. The tree to merge is the one named in the review-round section.


## The owner's words, as theirs
- On the first fix build: "The flickering is better, but now there is a brief moment when the icons blink before fanning out". Then "Go ahead".
- Later: "It's clean on the spread now", and, of the first fix: "I ran it on super slow motion and I verified no flickering beyond the blink occurs." They did not say which phone or how it was slowed.
- Beside the pass criterion (above): "It's clean on the spread now" is the owner's word about what they watched. The planner reads it as support for not making icon-without-circle a pass item. That is the planner's reading, not the owner's ruling, and the criterion is still theirs to settle.

## Diagnosis: confirmed, in two independent records that agree run by run
- **Setup.** A throwaway probe build (kept out of the tree, copies in the evidence folder) logged per rendered frame how many original markers and how many copies the renderer reported at the stack (`queryRenderedFeatures`), while the same run was screen-recorded. Six opens (five at animator 1, one at 5), `amend2/probe-logcat.txt` and `runs/probe_r*.mp4`, `runs/probe5_r1.mp4`.
- **Result.** The log has a rendered frame with copies=0 and originals=0 (1 or 2 frames) in opens 2, 4, 5 and 6 (the animator-5 open), and none in opens 1 and 3. `gap.py` on the recordings of those runs finds one empty screen frame (8, 8, 9 and 7 ms) in exactly those four and none in runs 1 and 3. So the originals' filter takes effect before the copies are drawn, and which comes first varies from run to run (the copies are re-tiled off the main thread), which is why the gap is 0, 1 or 2 frames.
- **How `gap.py` decides** (checked against stills, e.g. a2_r2 frames 267 to 276): a frame is empty when the fan region (the 480x520 crop) holds fewer than 60 glyph-coloured pixels (hue 125 to 179 or 0 to 4, S>110, V>110); the folded stack has about 1800 and the copies about 2500. A gap's time is the clip timestamp of the first non-empty frame after it minus that of its first frame.
- **Not ruled out and not tested:** the planner's other candidates (a visibility or image step landing late; the first push before positions settle; the gap depending on the number of members). The data fits the one mechanism above in every run, but a stack of two or twelve was not tried.

## Gap before and after (S22, 120 Hz mode, animator 1 unless noted; empty frames at the stack, time on screen)
| build | runs with a gap | per run |
|---|---|---|
| first fix, zoomed view (`pre1_r1..4`, `pre2_r1`, a5 `pre5_r1`) | 6 of 6 | 25 ms (2 frames), 9, 9, 8, 8, and 17 ms (2 frames, animator 5) |
| first fix, earlier view (`ff_fix_a1`, `ff_fix_a5`) | 2 of 2 | 33 ms (1 frame); 16 ms (2 frames, animator 5) |
| old order + probes, the restored view the "after" runs share (`probe_r1..5`, `probe5_r1`; `p2_r1..5`, `p2a5_r1`) | 9 of 12 | 8, 8, 9, 7 ms (probe) and 25, 9, 8, 8, 8 ms (p2); 3 runs none |
| **Amendment 2, the same view (`a2_r1..5`, a5 `a2a5_r1`)** | **0 of 6** | **none** (the whole clips scanned, so the fold's end too) |
> *Correction:* "so the fold's end too" is true of what was scanned (the whole clips) but those six runs were a small sample of an intermittent fold-end gap; see the review round.
The length varies (0 to 2 frames, 7 to 33 ms); the 33 ms was the longest, not the typical. Six runs after the fix is a small sample: if the gap showed in two thirds of runs, 0 of 6 would be about a 1 in 700 chance, but it is still six runs on one stack.

## The fix
- `FanOutHideGate` (new, pure, `ui/map/fanout/`): the order. A new fan hides nothing until its copies are drawn; the originals that stay fanned stay hidden; every other original is shown at once; the rest are hidden when `onCopiesDrawn(generation)` is called for the current wait. `onFold` drops a wait; an empty members list shows everything and drops any wait.
- `FanOutRenderSignal.kt` (new): `awaitCopiesRendered` adds an `OnDidFinishRenderingFrameListener` and, on each rendered frame, asks `queryRenderedFeatures` of the fan's icon and dot layers for the ids of the expected copies; it completes when all are reported and removes the listener when it completes or is cancelled. `hideWhenCopiesDrawn` applies the gate's step to the filters. The matching (`expectedCopies`, `copiesDrawn`) is pure.
- `SightingsMap.kt` (the fan effect, +30 -4): the copies are pushed first as before; the hide is the wait above; a fold drops the wait.
- **The signal is not a fixed wait.** It is the renderer's own report, 3 or 4 rendered frames after the push in the six probe opens (28 to 46 ms, the copies 0.2 to 3.7 dp from their originals), and its listener was removed each time (the probe logged `its listener is removed`; it queries nothing once the answer is in or the fan is folded, released or replaced, because the wait is cancelled with its coroutine).
- **Where the signal cannot come, and what happens (logged, none reached in these runs):**
  1. No fanned marker is on screen: the originals are hidden at once, with one info line (`None of the N fanned markers is on screen`); there is nothing to blink.
  2. On screen but not reported: when the fan has been at rest for 3 rendered frames, the originals are hidden anyway and a warning says so (`The renderer did not report the N copies drawn ...`). This is the last resort. With animations off (the owner's "spread out at once") the fan is at rest at once, so this fires about 3 frames after the open; I did not test that case.
  The `MarkerFanOut` tag logged neither line during the Amendment 2 runs (`amend2/final/a2-markerfanout-log.txt`, bounded by a marker line, 06:07 to 06:12).
- **Not done:** the single source and the probe code are not in this tree. They were reverted as new commits (below).

## Commits
`f6a7ed0b` (the wiring, work in progress when written), reverts of `9bc4fec7` (the single source) and `10ccca53` (its stub and failing tests; reverting the first alone would have left seven failing tests and a stub), the removal of `FanGapProbe.kt`, and the merge of `journal-redesign`: the tree is `d5d51ba0`. `FanOutLayers.kt` is byte-identical to `origin/journal-redesign` (0 differing files); `grep` finds 0 probe references; the diff against `journal-redesign` is exactly the five Amendment 2 files (`FanOutHideGate.kt`, `FanOutRenderSignal.kt`, `SightingsMap.kt`, and the two test classes): 470 insertions, 4 deletions. The single source can be re-applied by reverting the two revert commits.

## "No state where a marker shows twice or not at all", case by case
| case | how shown |
|---|---|
| a fan opens | recordings: no empty frame in 6 of 6; the glyph-pixel count goes from the folded level (about 1837) to the copy level (about 2507) in one step and never exceeds it before the spread (`a2_r1..5`), so there is no sign of a frame with the originals beside offset copies (originals and copies at one place would not show in the count); full-resolution crops of `a2_r2` frames 267 to 276 show no ghost |
| a fold, Back during the spread | recordings `q2_r1`, `q2_r2` (Back 0.3 s after the tap): the stack is back (7565 glyph px, a folded stack), no empty frame; unit tests for the gate |
| Back before the signal | **not reachable by adb.** A Back 0.08 s after the tap was not delivered to the fan at all (the fan stayed open until a later Back): the Back handler is composed only once the fan is open, and the two adb calls land in the same input batch. The signal itself arrives about 45 ms after the push, earlier than any Back adb can place after the handler exists. Covered by the gate's tests (a fold or release before the signal leaves the originals shown and the late signal does nothing), not on the phone. This Back behaviour is not new with this change; I did not run it on the base |
| another stack tapped while a fan is open | gate tests only (the first fan's originals return at once, the new ones hide on the signal, a marker in both stays hidden). No second stack was in view; not on the phone |
| a style reload | the effect, and so the gate, is re-created with the style, whose filters hide nothing, and the open fan goes through the same push-then-signal path (gate test "a fan re-drawn on a fresh style"). Not on the phone |
| members off screen / no signal | the two logged branches above; neither reached on the phone |
| reduce motion | not tested |

## Evidence that the first amendment's measures still hold (`measure.py`, the same script)
| run | circle-without-icon | reversals | rest | icons at final brightness |
|---|---|---|---|---|
| a2_r1 open | **1** (frame 284) | 0 | 216 ms | 75 ms before rest |
| a2_r2 / r3 / r4 / r5 open | 0 / 0 / 0 / 0 | 0 | 200 / 216 / 208 / 175 ms | 84 / 75 / 75 / 67 ms before rest |
| a2a5_r1 open (animator 5) | 0 | 0 | 950 ms | 408 ms before rest |
| folds, six runs | 0 | 0 | gone at 158, 142, 150, 158, 174 ms (a1), 484 ms (a5) | n/a |
**The one flagged frame, stated plainly:** `a2_r1` frame 284 is flagged circle-without-icon (icon coverage 0.37, circle 0.22, thresholds 0.5 and 0.2). It sits on a smooth rising edge (icon 0.23, 0.37, 0.74; circle 0.09, 0.11, 0.22, 0.30), nothing reverses or dims, and the early copies lie inside the metric's 40 px hub exclusion disc, which depresses icon coverage there. I read it as the threshold edge and not a blink, but by the dispatch's strict count the pass ("0 circle-without-icon frames") is met in five runs of six and not in one. icon-without-circle (reported, not a pass item here): 1 to 2 frames at animator 1 open, 3 to 6 at the fold, 9 and 6 at animator 5.

## -299 and -318, viewed at animator 1 and 5
Full-resolution crops: `a2_r2` frames 267 to 276 (open start), `a2_r1` frames 626 to 636 (fold end), `a2a5_r1` frames 282 to 296 (open start) and 1092 to 1104 (fold end). The camera tile is the front glyph in the same place before the open, in the copies, in the last fan frames and after the fold; no position jump at either end, at either speed (-299, -318). The location dot is covered by the copies from the first copy frame (-290) and returns about 5 to 6 frames after the copies clear at the fold's end (`a2_r1..3`, measured by blue pixels at the hub); in the first fix's clip it returned 4 frames after. I did not look into that difference.
> *Correction:* "the dot returns about 5 to 6 frames after the copies clear" was a loose reading (first frame past a fixed offset with enough blue pixels). Measured properly in the review round, the dot returns in the same frame the originals do.

## Tests
- **`FanOutHideGateTest` (12):** test first. On the stub (hides at once) 6 of 12 failed, each with a message specific to the order ("no original may be hidden before the signal expected [] but was [FanMember(...)]"; "a1 is in both fans, so its original stays hidden ..."; "the first fan's originals come back as its copies go ..."). The other 6 passed on the stub because it returns nothing from the signal paths: they guard the implementation and do not bite on the stub. Pushed failing (`a96b0686`), then the implementation: 12/12.
- **`FanOutRenderSignalTest` (7):** the pure matching. **These were written with the implementation, not before it**; their bite is shown only by the revert check below.
- **Revert check, from saved copies** (`FanOutHideGate.kt` sha256 `d655e88d…`, `FanOutRenderSignal.kt` `31e6974d…`): `onMembers` made to hide at once, and `copiesDrawn` made to accept any one copy. The compile tasks ran, **0 `e:` lines, read before the XML**; 8 of 19 failed: 6 in the gate (the same set that failed on the stub) and 2 in the signal ("one icon missing" and one more), failures only these edits can produce. Restored from the saved copies (sha256 equal), forward change confirmed present, `git status` empty. Evidence `amend2/final/revert-*.xml`.
- **What no unit test reaches:** that the renderer reports the frames the code asks it about and that the listener is removed (the probe log shows it, 3 or 4 frames per open); that the filters then change on the next frame; the signal on the S26; reduce motion; another stack; a style reload.
- **Full suite, `d5d51ba0`:** BUILD SUCCESSFUL in 4m 17s, 0 `e:` lines; 409 suites, 3297 tests, 24 skipped, **0 failures, 0 errors** (19 more than the first fix's 3278: the 12 and the 7). XML in `amend2/final/full-suite-xml/`, saved before the revert run. `assembleDebug`: 0 `e:` lines.

## The phone
Installs since the first fix, in order (`install -r --user 0`, forward only, no `-d`; the three values read back after each, always inode 2259049 and db `e1188b00…`): the probe build (apk `43eea7fc…`, `1.0.2342+g69e004e9.dirty`), probe 2 (`c3b44af7…`, `1.0.2344+gf6a7ed0b.dirty`), probe 3 (`bc8c7a64…`, `1.0.2345+g10ccca53.dirty`; the single source plus the probe), and **the Amendment 2 build (apk `accbaa72fbcf5c8d…`, `1.0.2352+gd5d51ba0`, `lastUpdateTime` 06:06:43), which is on the phone now**. It is built from a clean tree (no `.dirty`).
- Animator set to 5.0 for one run per batch and read back as 1.0. Settings otherwise unchanged.
- **Deviations from the phone rules, stated:** I ran `adb logcat -c` once (at the start of the probe 3 batch), which clears the phone's log buffer and touches no data; it is not in the dispatch's list and I will not run it again. I wrote one marker line into the log (`log -t FANMARK`) at the planner's suggestion, to bound a read.
- **The owner used the phone while I recorded**, which I found and stopped for: `runs/pre1_r2.mp4` shows, for about 4 seconds, a settings screen sliding over the dimmed map, and the end of `pre1_r4.mp4` is the same kind of overlay; I sent no input then. The map had also been panned and zoomed between my first clips and later ones, so the earlier "before" clips (`ff_*`, `pre*`) are on a different view from the matched "before" and "after" runs (`probe*`, `p2*`, `a2*`), which share the view the app restores after an install. I did not restore the earlier view (a pinch is a swipe). Nothing from the owner's own use of the phone is in a sheet or the repository; the clips are local.
- Re-reads before taps and installs always matched what I left; each run's pre-tap check confirmed the folded stack at the tap point, and stopped run 5 of the first batch and two runs of the interruption batch when it was not there.

## The four disclosures
- **Confirmed vs inferred.** Confirmed: the mechanism of the blink (two independent records agreeing run by run); that the Amendment 2 build shows no empty frame in 6 of 6 runs; the unit tests. Inferred: why the order races (re-tiling off the main thread), and that 0 of 6 will hold in general.
- **Could not determine.** A Back between the push and the signal on the phone; another stack; a style reload; reduce motion; the S26; the late return of the dot.
- **Premises that were wrong.** "The S22 is 60 Hz" (it was 120 Hz mode for these recordings; origin not established); "test B is not needed" (see Amendment 3). (The first fix's report did name the one-or-two-frame gap at the open's start as a residual, correctly; what it did not know was that its length varies from 0 to 2 frames and that the order races.)
- **Decided beyond scope.** The thresholds in `gap.py`; the three-frame give-up and its info and warning lines; adding `onFold` to the gate beyond the dispatch's list (so a fold drops a wait); reverting `10ccca53` as well as `9bc4fec7` (the planner named only the second; the first alone would have left seven failing tests); a sheet that included the owner's settings screen was made, viewed and deleted.

---

# Amendment 3 (continuation -373): the circles run ahead of the icons — STOPPED, measured, not fixed

**Status: a stop, as the amendment defines it ("if one source does not close the gap ... a stop with what you measured"). No fix is in this tree. The single source was built and measured and is set aside (reverted as two new commits); the owner decides the next step.** Nothing about options A, B or C was built.

## The owner's words, as theirs
- "While we're at it, there is a moment where the circles jump ahead of the icons and it is visible during usual animation speed."
- Asked whether the recording was on the S26 and on GitHub's build 2340: "Yes on 2340 on my S26" (build 2340 holds the first fix and nothing of Amendments 2 or 3; a stack of twelve, day style).
- "I noticed the circles grow when expanding. It's acceptable if they fade instead of grow, if the growth is causing uneven physics."
- "The S22 is currently in 120hz mode", and "It has been during this part of the test".

## What was measured, and how
A throwaway probe build (`amend2/probe2-build.apk`, `amend2/probe3-single-source-build.apk`, log files `probe2-logcat.txt` and `probe3-logcat.txt`, `lag.py` to reproduce) tagged every pushed fan feature with a push sequence number, the progress it was pushed at and its member, and on every rendered frame asked `queryRenderedFeatures` of all four layers which push each showed, and for each member the distance on screen between its circle's point and its icon's point. Five opens at animator 1 and one at animator 5 per build, on the S22 (120 Hz mode), the night style, a stack of seven (three photos, two finds, a pin, a flag).

| | four sources (today) | one source (built, set aside) |
|---|---|---|
| animating frames, animator 1 (5 opens) | 289 | 295 |
| frames with the layers on different pushes | 255 (88%) | 252 (85%) |
| frames with every layer on the same push | 34 (12%) | 43 (15%) |
| circle point to icon point, same member: median | 1.35 dp | 0.00 dp |
| frames with the two 3 dp or more apart | 100 (35%) | 72 (24%) |
| largest distance | 35.7 dp | 23.0 dp |
| animator 5 (1 open): out of step / animating | 126 / 144, largest 5.5 dp | 125 / 142, largest 6.1 dp |
The usual pattern with four sources (lag behind the newest push, for circles, icons, legs): circles and legs current with the icons one push behind in 130 frames, icons two behind in 38; with one source: icons one behind in 108, and the legs one push ahead of both circles and icons in 87 (40 before).

## The owner's question: position, size, or both?
**Position.** Where the circle was ahead of the icon, the circle's drawn radius (18 dp times the progress it was pushed at) differed from the radius of the icon's push by a median of 0.09 dp (largest 8.6 dp), while the circle's point differed from the icon's by a median of 3.7 dp (90th percentile 12.8 dp, largest 35.7 dp). That is (a) in the planner's list: the circles are displaced, and would look wrong at any size. The growth is not what makes it look uneven on this evidence, so the condition the owner attached to allowing a fade ("if the growth is causing uneven physics") is not met by these measurements, and no fade was built. At what progress a circle first shows beside its glyph with the growth in place: about 0.66 to 0.81 on the fix builds (12 to 15 dp radius, the first amendment's table).

## Two limits on this result (carried to the owner as written)
1. **The queries are not shown to equal what is drawn.** `queryRenderedFeatures` on a symbol layer may report the previous placement's state, a frame behind the drawn one; if so, the icon lag in the log is overstated by up to one push. For presence the query agreed with the screen run by run (Amendment 2), but not for position. I tried to check the position in the recordings, with the legs' end points against the glyphs, and it failed: the basemap's own white lines and the static accuracy ring swallow the legs, so there is no independent pixel confirmation on the S22. The owner's S26 clip is the visual evidence that the fault is real.
2. **The per-layer lag patterns use the newest push in each layer, and the fan spans several map tiles**, which update separately: a leg crossing into an early tile can make the legs look ahead. The per-member circle-to-icon distance is tile-independent, so it is the figure to rely on.
Also: five opens per build and one at animator 5; one stack of seven on the night style; the S26's stack of twelve in the day style is not what was measured.

## Why the single source does not close it (inferred, not shown)
A GeoJSON source is cut into map tiles and each push re-tiles the ones it touches; the layers of a tile come from the same data, but the symbol layer needs a further step (its images and glyphs are requested from the main thread, then placement) that a circle or line layer does not, so the icons can arrive a push after the circles even from one source. I did not read MapLibre's code; the evidence is only that one source moved the median to 0 and left a quarter of the frames 3 dp or more apart.

## Options, each with its cost; none built, none tested on the phone
- **A. Draw the circle as a symbol too** (an SDF circle bitmap in a second symbol layer, its size and opacity following the progress): the circle and the glyph then go through the same symbol pass. Cost: the circle's antialiasing at rest must be shown to match the native circle (the dispatch's bar is pixel for pixel); the legs, a line layer, would still arrive ahead of both; -299's growth becomes an `icon-size` expression; a fade instead of a grow is the owner's conditional permission. The planner has asked the owner for a throwaway test of this, in a later step of the dispatch file; not started.
- **B. Gate the pushes on the render:** do not push the next frame until all four layers show the last. Cost: fewer pushes (the motion would step more coarsely), and frames mixing two pushes still occur while one lands. At 120 Hz the cost in coarseness is not what I first wrote ("every 2 to 3 frames at 60 Hz"): that premise was wrong, see the correction under Conditions in the Part 1 report.
- **C. Move the fan out of the map's layers** into a Compose overlay positioned each frame from the projection, with no tile pipeline. Cost: the largest change; the dot would be under it by construction (-290 holds), but touch areas, Back, stacking (-318) and the look at rest must all be re-shown, and it is more than "confined to the fan's layers".
- **D. Keep the single source** as a partial improvement (median 0, 24% of frames 3 dp or more apart) and accept the rest. The owner's call; it leaves a fault they have seen on the S26.

## Test B, corrected
The Part 1 report and the planner both said test B was not needed. It was: probe 2 against probe 3 is that test, run late, and it shows a single source helps and is not enough. The notes beside the earlier statements say so.

## What exists in the tree and the evidence
The single-source code and its tests (`FanPushPlanTest`, 7/7 on the implementation, 7/7 failing on the stub) are in the history (`10ccca53`, `9bc4fec7`) and were reverted by two new commits; reverting those reverts re-applies them. The probe code is not in the tree; its copies are in `amend2/saved-copies/`. `legs_vs_icons.py` in the evidence folder is the failed pixel check, kept so that nobody repeats it as written.

---

# Amendment 2, review round: a restart with the fan up, and the fold's end

**Status: done. The planner reviewed `a5185a2f`, found a case I had broken, and the fix turned up the mirror gap at the fold's end; both are fixed on `fan-flicker`. Not yet merged.**

## 1. The planner's case: confirmed, in the code
The fan-draw effect is `LaunchedEffect(loadedStyle, mapLibreMap, focusedObservationId)` (`SightingsMap.kt:800`); the comment at `:791` says tapping a fanned marker keeps the fan up while a bubble opens, which changes `focusedObservationId`, so the effect restarts with the fan open. In `a5185a2f` the gate (`:806`), `hiddenFor` and `wasOpen` were created inside it, so the restart began with a gate that believed nothing was hidden: its first `onMembers` returned an empty hide list (every original shown) and a wait, and the originals came back for the 1 or 2 frames until the signal. On `journal-redesign` (`:795-804`) the same restart called `applyFanOutHiding(style, members)` at once and the originals never came back. A style reload has the same shape: the new style's layers start unfiltered and the hide waited for a signal while the copies were already spread. So: a marker shown twice, which Amendment 2 rules out. The S22's stack has no sighting (three photos, two finds, a pin, a flag), so my runs could not reach it, and the gate's tests used one gate.
Not reproduced on the phone: no member of that stack changes the focused observation (it is a sighting's observation id; `sightingsFeatureCollection` is its consumer), so a tap on a fanned find opens a bubble without restarting the effect. I did not tap a fanned member. It stays with the owner (a sighting in a fan, then a tap on it).

## 2. The fix, and what was rejected
- **Chosen:** (a) the gate is `remember(loadedStyle) { FanOutHideGate() }`, outside the effect (`SightingsMap.kt`), so it outlives a restart and is new only with a new style, whose filters start clear; (b) `onMembers` is idempotent: asked again for the members it is waiting for it returns the same wait (same generation, so the signal for the first ask still counts), and for members it has hidden it changes nothing; (c) `onMembers(members, spread)`: a fan first seen already away from its originals (progress above zero) is hidden at once, as before the amendment, because the wait exists only for the open's first frames, when the copies sit exactly on their originals (-299); (d) `wasOpen` starts true so a restart that finds the fan folding drops a pending wait.
- **Rejected:** hoisting alone (a style reload would still wait with the copies spread; and the new gate is needed there); the spread rule alone (a restart in the first 45 ms of an open would hide before the copies are drawn).
- **Tests, first:** five new `FanOutHideGateTest` cases, pushed with 3 failing on the old gate (`48d117db`): "the wait is the same wait expected:<1> but was:<2>" and "copies are away from their originals, so there is nothing to wait for" (this is the new-gate-on-restart case itself). The other two pass on both (they guard the new code). **What no unit test reaches:** that the gate is in fact remembered across the effect's restart (`remember(loadedStyle)` in `SightingsMap.kt`) and that the effect then does not touch the filters: Compose's keying is not exercised headless. The gate's behaviour when asked twice is.

## 3. The fold's end: found by checking my own claim, and fixed
- **What I found.** The first fix's report, and my earlier hand-back, said the fold's end has no gap. Re-reading `gap.py`'s output in full (for several sets I had printed only its first line), and running more, the tally of runs with a one-frame empty stack, by build (S22, 120 Hz mode, animator 1 and 5):

| build (run set) | runs | open-start gaps | fold-end gaps |
|---|---|---|---|
| first fix (`pre*`, five usable runs) | 5 | 5 | 1 |
| old order + probe, same view (`probe*`, `p2*`) | 12 | 9 | 0 |
| old order + the single source (`p3*`) | 6 | 6 | 0 |
| Amendment 2 as first handed over (`a2*`) | 6 | 0 | 0 |
| Amendment 2 plus the restart fix (`a2b*`) | 6 | 0 | **2** (8 ms each) |
| **final (`a2c*`)** | **11** | **0** | **0** |
- **Cause (inferred; the same race as the open, reversed).** At a release the originals' filter is cleared and the copies are cleared in the same pass; un-hiding an original needs its tile laid out again, clearing the copies is a small source, and either can land first. On `a2b_r2` and `a2b_r4` the glyph count goes copies, then 0, then folded (2444, 0, 1834 and 2366, 0, 1832).
- **The fix.** `FanOutHideStep.reveal` names the originals a release shows again; the effect holds back the clearing push until `awaitOriginalsRendered` reports them (per marker layer, by feature id), then pushes the empty frame. The copies stand exactly on their originals at progress 0, so showing both meanwhile is not seen. Tests first (`reveal` as a stub field: 2 of the 4 new cases failed on it, pushed failing as `71b80429`), then the implementation (`3c82194a`); the pure matching has 4 more cases. The restart fix is `d970c1bc`.
- **Where the signal cannot come:** none of the revealed originals on screen: the copies are cleared at once; the originals not drawn at all (their layer switched off, say): after `REVEAL_FRAMES_BEFORE_GIVING_UP = 6` rendered frames the copies are cleared anyway and a warning says so. Neither fired in the final runs (`MarkerFanOut` log empty). **Not covered:** a fan *replaced* by another (tap on a second stack): its copies go in the same push as the new ones come, so the first fan's markers could blink the same way; this is rare and I did not handle or test it.
- **Where 3 and 6 come from.** `REST_FRAMES_BEFORE_GIVING_UP = 3` (the open) is my number: the signal arrived at the 3rd or 4th rendered frame in the six probe opens, and 3 rendered frames *at rest* is a safety net well after that in an animated open and about the same time in a reduce-motion open. `REVEAL_FRAMES_BEFORE_GIVING_UP = 6` is twice the open's observed 3 to 4, chosen without a measurement of the release (none was taken). Both are last resorts, logged when reached; neither is a tuned wait. If the map stops rendering before they are reached (the app backgrounded), the listener stays attached but is called on no frames, so it costs nothing, the originals stay as they were, and the wait resumes on the first frame after rendering restarts or ends with its caller's cancellation (a fold, a release, a new style).

## 4. The gap runs again, on the final build (`1.0.2360+g3c82194a`, apk `c73fb616d8bd13d2…`)
11 runs (ten at animator 1, one at 5), same restored view: **0 open-start gaps, 0 fold-end gaps**, `MarkerFanOut` warnings none. At the fold's end the glyph count now holds the copy level for about 4 frames and drops straight to the folded level (2478 to 1833; no zero, no bump). The first amendment's measures on `a2c_r1..3` and the animator-5 run: 0 circle-without-icon frames, 0 reversals, icons at final brightness 67 to 409 ms before the fan rests (this time the one flagged frame of the earlier build did not recur). -299 and -318 viewed again at animator 1 and 5 (open start and fold end): the camera tile is the front glyph in the same place at both ends and both speeds, no jump. **Dot:** it returns in the same frame the originals do on every build checked (`a2` and `a2c`); the final build's fold-end settles about 4 frames later than `a2`'s, the copies being held for the originals.

## 5. Tests, revert check, suite
- **`FanOutHideGateTest` 21, `FanOutRenderSignalTest` 11, both passing.**
- **Revert check, from saved copies** (`FanOutHideGate.kt` sha256 `3c487136…`, `FanOutRenderSignal.kt` `ee1e0b12…`): the reveal removed, the spread rule disabled, the idempotence removed, and `originalsDrawn` changed from all to any. The compile tasks ran, **0 `e:` lines, read before the XML**; 7 of 32 failed, each one a case for a mutated behaviour (the two reveal cases, "already spread", "same wait", "restart during a fold", and the two originals' matching cases). Restored from the saved copies (sha256 equal), forward change confirmed present, `git status` empty.
- **Full suite, `3c82194a`:** BUILD SUCCESSFUL in 4m 13s, 0 `e:` lines; 409 suites, 3310 tests, 24 skipped, **0 failures, 0 errors**; XML in `amend2/final3/full-suite-xml/`.

## 6. The early Back, and the phone
- **The Back 80 ms after the tap.** `MarkerFanOutState.kt` is not in this branch's diff, so "the fan's Back handler is composed only while the fan is open" predates Amendment 2. What the early Back did on the phone: nothing visible. The fan then opened normally and stayed open; the focused window, the process and the activity were unchanged (`mCurrentFocus` and `topResumedActivity` still `MainActivity`, same pid), no dialog or navigation appeared. Which handler consumed it I did not determine: several `BackHandler`s exist (the bubble, the search suggestions, the compact map's overlays), and with none enabled the activity would have finished, which it did not.
- **Installs this round** (forward only; three values read back after each, always inode 2259049 and db `e1188b00…`): the previous build `1.0.2352+gd5d51ba0` was replaced by `1.0.2358+gd970c1bc` (apk `2fd54361106d1d12…`, the restart fix) and then by **`1.0.2360+g3c82194a` (apk `c73fb616d8bd13d2…`, installed 06:42:06), which is on the phone now**, built from a clean tree. Animator set to 5.0 for one run per batch and read back as 1.0. No `logcat -c` this round; a marker line was written before each batch and the log bounded by time.
- **Disclosures added:** the owner's phone was in use for part of an earlier batch (described above); a Back at 80 ms and the early interruption runs stopped three of my own runs through the pre-tap check; the "dot returns 5 to 6 frames later" and "no fold-end gap" statements are corrected beside where they stood.
