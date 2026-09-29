# Part 1's layout fixes (`-78`, amended by `-88`): stop report

**Status: stopped during verification, before any test or build.** No code, test or build file has changed. This file
is the only change on the branch. The stop is the abort condition "an unruled placement": five of the nine items
(1, 7, 9, the landscape half of 2, and 6) cannot be fixed without moving the legend, the cluster, J8's chip, the search
bar or the attribution caption to a place no ruling gives. Three more need files outside the named scope (4, 5 in
landscape, 8). The options are below. I have not chosen between them.

**Date:** 2026-09-28. **Dispatch:** `prompts/preserved/2026-09-28-78.md` (sha256 `6c28caf7…c3d2`), amended by
`prompts/preserved/2026-09-28-88.md` (sha256 `a4647a92…f13e`), launched by `RECORD.md` entry `2026-09-28-93`.
**Code citations** are at `a0a54f9`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in
full.

## Base and worktree

- `git fetch`: `origin/journal-redesign` is at `a0a54f9`, the base the launch message names.
- `git diff bde2e98 a0a54f9 -- app/` is empty (0 lines), so `app/` here is the J8 follow-ups build, as the launch
  message says.
- Worktree `/home/zynergy-labs/Zynergy/forager-wt/layout-fixes`, branch `layout-fixes`, created with the launch
  message's own command. No `layout-fixes` worktree or branch existed before.
- No Gradle run was made, so there are no suite figures of mine. The planner's figure at this base is
  306 / 2490 / 0 / 0 / 24, and nothing here changes `app/`.

## The governing texts, verbatim

### Dispatch `2026-09-28-78`

From `prompts/preserved/2026-09-28-78.md` at `a0a54f9`, below its "verbatim prompt follows" line:

> **Type:** build
>
> # Role
>
> You are the coder for **Part 1's layout fixes**. This dispatch's intent is `2026-09-28-78`. The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Read `CLAUDE.md` first, especially the Surface pitfall, the Robolectric-insets pitfall, UX defaults and Testing.
>
> # The owner's ruling, verbatim
>
> "2 A fix all", to the planner's list of six items from stage device check Part 1 (`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md`, terminal `2026-09-28-71`).
>
> # The six items (re-verify each at your base with file:line, and cite the run record's evidence)
>
> 1. **The legend chip over the record button at rotation 270,** collapsed or expanded (check 4d).
> 2. **The icon cluster over the expanded legend:** a 122 px overlap. The clamp's downward limit floors at the centred position (`AvailabilityCompactMapUi.kt:795-797`, the device coder's reading) (check 4e).
> 3. **The portrait search dropdown under the keyboard:** Latitude, Longitude and "Search this location" sit under the keyboard, the dropdown does not resize for it, and a programmatic scroll does not lower the keyboard (check 8).
> 4. **A tab round trip resets the Maps camera** to the location at about zoom 12. CLAUDE.md's UX defaults say user-set state survives navigating away and back within a session. The position and zoom the user left must survive a tab round trip. This is **not** about surviving a restart, which is a separate per-case decision and is not asked.
> 5. **MapLibre's "i" sits under the system nav bar** in portrait and at 90.
> 6. **The attribution strip crosses the cluster's bottom row at 90** (seen by eye, not measured).
>
> # Verification before building
>
> For each item:
> - the cause, at file:line;
> - whether Robolectric can see it. Items 3, 5 and 6 likely depend on real insets (IME, nav bar, cut-out), which Robolectric reports as zero (CLAUDE.md). Say so for each;
> - what fixing it changes.
>
> **Stop and report options** where a fix would:
> - move the legend or the cluster to a place the L0b rulings did not give (the legend: "bottom corner opposite the attribution; clear of the icon cluster");
> - change how the cluster drags or snaps;
> - change what the attribution shows.
>
> Do not choose between placements.
>
> # Build
>
> - Fix each item by the least change that holds its ruling.
> - Items 1 and 2: the legend and cluster stay clear of each other at every rotation, collapsed or expanded.
> - Item 3: the dropdown's fields reach the area above the keyboard.
> - Item 4: the camera survives a tab round trip.
> - Items 5 and 6: the "i" and the strip clear the real system bars and the cluster.
> - **No new copy.**
>
> # Tests
>
> - **Tests first**, each seen failing at base for its stated reason, through the real screen:
>   - items 1 and 2 at rotations 0, 90 and 270 (pin the rotation as the `-47` landscape tests did), with the legend collapsed and expanded, asserting the bounds do not intersect;
>   - item 4, a tab round trip keeping position and zoom.
> - Items 3, 5 and 6: write a Robolectric test where one can see the behaviour. Where it cannot, **say so and list the item device-only**. A green Robolectric run is not evidence for an inset-dependent fix (CLAUDE.md).
> - **Revert checks** under CLAUDE.md's runner rules, one per fixed item that has a test.
> - **Full suite** from a cleared results directory, against the planner's figure at your base.
> - **Device-only:** list each item's check on the S22 at 0, 90 and 270, for the planner's next device check.
>
> # Scope
>
> **In scope:**
> - the Maps host files: `AvailabilityCompactMapUi.kt`, `AvailabilityCompactScaffold.kt`, `AvailabilityScreen.kt`, `AvailabilitySearchUi.kt`;
> - `ui/map/SightingsMap.kt` for the camera and attribution margins, and `ui/map/MapLayersSheet.kt` for the legend;
> - the camera state holder, wherever it lives;
> - tests;
> - the report `docs/audits/<date>-part-1-layout-fixes-completion-report.md`.
>
> **Out of scope:** the night outline (`-79`), map-chrome alpha, J6, and the other Part 1 flags (the puck, the reticle, the Diagnostics refresh, the log flood, the outline tap, the Back press).
>
> # Branch, environment, finish
>
> - **Branch:** a worktree from `origin/journal-redesign` at the base the launch message names. Push to `journal-redesign` after each commit that leaves the suite passing, and put broken work on `layout-fixes-wip`. Merge with `--no-rebase`.
> - **Environment:** `LC_ALL=C.UTF-8`. Before each Gradle run, check that no other Gradle build is running and that 2.5 GB is free. No phone.
> - **D58** before each push.
> - **Finish line:** verification, the build, tests first, revert checks, the full suite and the report, all pushed. The planner re-runs the suite and writes the terminal.
>
> # Abort conditions
>
> - an unruled placement;
> - a tests-first test passing at base;
> - a revert build that does not compile;
> - a non-held failure;
> - new copy;
> - disk full or OOM;
> - two failed fixes on one item;
> - an unruled design question.
>
> # Predictions (planner)
>
> 1. Items 1 and 2 share one cause in the legend and cluster clamp.
> 2. Item 4 is a camera state held in composition that resets when the map leaves composition.
> 3. Items 3, 5 and 6 are device-only.
>
> # Merge
>
> Not authorised.

### Continuation `2026-09-28-88`

From `prompts/preserved/2026-09-28-88.md` at `a0a54f9`, below its "verbatim prompt follows" line:

> **Type:** build (continuation `2026-09-28-88` of dispatch `2026-09-28-78`, written before `-78` launched)
>
> This **adds three items** to `-78`'s six. The owner's ruling, verbatim: "2 A", to the planner's option A: "Fix it in the layout-fix stage. I'd add two small overlaps it found as well." Quote this continuation verbatim in the report.
>
> The evidence is J8's device check (`docs/audits/2026-09-28-j8-device-check-run-record.md`, terminal `2026-09-28-81`). Re-verify every file:line at your base.
>
> 7. **At rotation 90, J8's chip covers the cluster's "Reset orientation to north".** Five of five real touches in the overlap opened the chip's list (captures `91`, `95`, `107`). At 270 they are 442 px apart (`101`, `103`, `108`). The chip and the cluster must not overlap at any rotation.
> 8. **In portrait, a 25 px band where the chip's touch area overlaps the coordinate readout's reaches neither control.** Five touches did nothing (check 4). Every point there must reach exactly one control.
> 9. **In landscape, the cluster's top row lies over the search bar's ends.** This predates J8 (flag 6). They must not overlap.
>
> **`-78`'s stop rule applies to all three.** A fix that moves the chip, the cluster or the search bar to a place no ruling gave is a stop, with options reported. J8's chip placement was ruled "Top, by the species chip".
>
> **Tests first:**
> - with rotations pinned as `-78` says, the bounds of the chip, the cluster's rows and the search bar do not intersect at 0, 90 and 270;
> - a real coordinate touch across the old dead band reaches exactly one control (CLAUDE.md: several touches across the bounds, not one at the centre).
>
> Where real insets decide, list the item device-only.
>
> Everything else in `-78` stands.

### The launch message

> This is the launch of planner dispatch `2026-09-28-78`, amended by continuation `2026-09-28-88`: **Part 1's layout fixes, nine items.**
>
> **What governs, in full, in this order:**
> 1. `prompts/preserved/2026-09-28-78.md`;
> 2. `prompts/preserved/2026-09-28-88.md`, which adds items 7 to 9;
> 3. `RECORD.md` entry `2026-09-28-93`, the launch note.
>
> Quote `-78`, `-88` and this message verbatim in your report, `docs/audits/<date>-part-1-layout-fixes-completion-report.md`.
>
> **Base and branch:**
> - **Base:** `origin/journal-redesign` at `a0a54f9`. `app/` there is identical to `bde2e98`, the J8 follow-ups build; the planner's suite is `306 / 2490 / 0 / 0 / 24`.
> - **Worktree:** `git worktree add /home/zynergy-labs/Zynergy/forager-wt/layout-fixes -b layout-fixes origin/journal-redesign`.
> - **Push:** to `journal-redesign` after each commit that leaves the suite passing. Put broken work on `layout-fixes-wip`. Merge with `git pull --no-rebase`, never rebase.
>
> **The nine items**, stated in full in the files. The evidence is the Part 1 run record and the J8 device-check run record, both in `docs/audits/`.
> 1. The legend chip over the record button at 270.
> 2. The cluster over the expanded legend: a 122 px overlap, with the clamp at `AvailabilityCompactMapUi.kt:795-797` in the Part 1 coder's reading.
> 3. The portrait search dropdown under the keyboard.
> 4. A tab round trip resetting the Maps camera. It must survive within a session, per CLAUDE.md's UX defaults; surviving a restart is not asked.
> 5. MapLibre's "i" under the nav bar.
> 6. The attribution strip across the cluster at 90.
> 7. J8's chip over the cluster's "Reset orientation to north" at 90.
> 8. The portrait dead touch band between the chip and the coordinate readout.
> 9. The cluster's top row over the search bar's ends in landscape.
>
> **Since the dispatches were written:**
> - `-79` added the region's night border.
> - J8 follow-ups removed both chips' shadow and moved the rings into the lines band.
>
> Re-verify every file:line at your base.
>
> **The rules that bind you hardest:**
> - **Stop and report options** where a fix would move the legend, the cluster, the chip or the search bar to a place no ruling gave.
> - Items that depend on real insets (the keyboard, the nav bar, the cut-out) are **device-only**. Robolectric reports zero insets, so a green test there is not evidence. Say so for each.
> - Tests first through the real screen, with rotations pinned at 0, 90 and 270. For the dead band, use several real touches across the bounds, not one at the centre.
> - A revert check for each fixed item that has a test. The full suite from a cleared results directory. No new copy.
>
> **Sharing the machine:** a device coder is using the S22, so you get no phone and no emulator. Before each Gradle run, check that none is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is free. Commit and push at every natural stopping point.
>
> The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.
>
> When you finish or stop, hand back a report: what landed with hashes, verification per item (whether Robolectric can see it), evidence, suite counts, revert checks, what was not tested, the device-only list, decisions you made and flags.

### `RECORD.md` entry `2026-09-28-93` (the launch note), at `a0a54f9`

> **Kind:** continuation
> **ID:** 2026-09-28-93
> **Timestamp:** 2026-09-28T23:18:43Z
> **Continues:** 2026-09-28-78
> **Dispatch-file:** none (launch note; -78 as amended by -88 governs)
> **Reason:** J8 follow-ups closed (terminal -92), so -78 (Part 1's layout fixes) is next in the build line.
> **Changes:** -78 and -88 launch at the base named in the launch message, in forager-wt/layout-fixes, branch layout-fixes.
> - J8 follow-ups removed the chips' shadow and moved the rings, and -79 added the region border. Re-verify every file:line.
> - -78's placement stop rule applies to all nine items.
> - The 11-shadows flag from -92 is not in scope.
> **Notes:** Written by the planner by hand.

## Structural check of the dispatch

`-78` with `-88` names a role (coder, "Part 1's layout fixes"), a base (in the launch message: `a0a54f9`), a scope
boundary (in and out of scope), checks and a finish line, abort conditions, and a `Merge` section ("Not authorised").
Nothing structural is missing. This is a structural check only. It cannot show that a needed decision was never made,
and the sections below show that several were not.

## Why I stopped

`-78`'s abort conditions include "an unruled placement" and "an unruled design question". Its stop rule, extended to
all nine items by `-88` and by `-93`, is "Stop and report options where a fix would move the legend, the cluster, the
chip or the search bar to a place no ruling gave", and "Do not choose between placements."

The verification the dispatch asks for before building found that:
- **Items 1, 7 and 9, and item 2 in landscape, have no fix that keeps every ruled placement.** They share one cause (the
  next section). Every fix I can see moves the legend, the cluster, J8's chip or the search bar.
- **Item 6** moves the attribution caption or the cluster. The caption is not in the stop rule's list, but any fix
  chooses a new caption position, and the same cause as the items above is behind it.
- **Items 4, 5 (in landscape) and 8** each need a file the scope does not name.
- **Items 4 and 5** also carry a design question each that no ruling answers.

That leaves item 2 in portrait, item 3 and item 5 in portrait outside fullscreen as buildable within the rulings and the
scope, by my reading. I did not build them. I read the abort list as stopping the dispatch, not a single item, so the
planner can narrow the work or get rulings first. The other reading, stop per item and build the rest, is recorded
under Decisions.

## The shared landscape cause (items 1, 2 in landscape, 6, 7 and 9)

**The icon cluster is taller than a short landscape window's map area, so it fills that area from top to bottom and
cannot move out of anything's way.**

- **The cluster's content is 380 dp tall.** It holds MapIconBar and ControlPill in one Column with an 8 dp gap
  (`ui/availability/AvailabilityCompactMapUi.kt:979-1027`, `CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR = Spacing.sm`,
  `:1407`).
  - MapIconBar has five 48 dp rows (`ui/map/MapChrome.kt:723`), 4 dp gaps and 4 dp padding at top and bottom
    (`:437-477`): 264 dp.
  - ControlPill has two rows (`ui/availability/AvailabilityMapControlsUi.kt:178-227`): 108 dp.
  - The container is padded 8 dp on every side (`MAP_ICON_BAR_EDGE_INSET`, `AvailabilityCompactMapUi.kt:971`).
- **The S22's landscape window is 1080 px, 384 dp.** The Map tab reserves only the top inset in a short landscape
  window (`AvailabilityCompactScaffold.kt:626-630`). Below an 84 px status bar that leaves about 996 px, 354 dp.
- **The cluster is centred** (`AvailabilityCompactMapUi.kt:752`, `:956`), and a Box child is measured within its
  parent. My reading is that the container is laid out at the map area's full height and centred, which puts its first
  row's top at 84 + 22.5 + 11.25 = 117.75 px.
  - The J8 device check read that row at 118 px, at both rotations: "Fullscreen" at `[98,118][233,253]` and
    `[2083,118][2218,253]` (`docs/audits/2026-09-28-j8-device-check-run-record.md:678-682`).
  - The match is my inference from the code and those figures, not a measurement of the container. If it holds,
    ControlPill is squeezed and its "Return to vehicle" row is mostly clipped at the bottom. See Flags.
- **The drag clamp has no room.** The clamp (`AvailabilityCompactMapUi.kt:794-817`) floors the downward limit at the
  centred position (`:801`, `coerceAtLeast(0f)`) and caps the upward limit at 0 (`:812`). With a cluster taller than
  its area both limits are 0, so the cluster stays where it is.
- **Everything else on the cluster's side collides with it:**
  - on the punch-hole side, which is the cluster's landscape default (`LandscapeClusterSide`, `:171-182`, and
    `landscapeOnPortSide = false`, `:160`; landscape design P6, `docs/plans/landscape-phone-design.md:236-240`,
    with R3 and R4 at `:529-534`), the search bar and J8's chip sit at the top (`AvailabilityCompactScaffold.kt:949-956`;
    `AvailabilityCompactMapUi.kt:1095-1104`);
  - the legend chip sits at the bottom-end corner whatever the rotation (`AvailabilityCompactMapUi.kt:1177`);
  - the attribution caption sits at the bottom-start corner (`ui/map/SightingsMap.kt:811`).
- **Robolectric sees this geometry.** It is not inset-dependent at its core: under Robolectric's
  `w823dp-h384dp-land` (the `-47` tests' qualifier, `AvailabilityScreenLandscapeB2Test.kt:80`) the map area is the
  whole 384 dp, and the cluster's container (380 + 16 dp) still fills it. The exact pixel positions on the phone depend
  on the status bar, so the device figures differ from Robolectric's. That is my arithmetic; no test was run.

## Verification, item by item

Each item gives the cause at `a0a54f9`, whether Robolectric can see it, what a fix would change, and its status.

### 1. The legend chip over the record button at 270

- **Evidence:** Part 1 run record, check 4 (d): at 270 the collapsed chip `[2003,871][2218,1006]` lies over the
  cluster's column x 2083-2218, and the dump has no "Start recording track" node. Expanded (`[1430,720][2218,990]`),
  it cuts the "Plan a trip" row and covers the record row
  (`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md:700`, `:706-710`; re-run `:805-809`).
- **Cause:**
  - The chip is aligned `BottomEnd` at every rotation (`AvailabilityCompactMapUi.kt:1170-1184`, the alignment at
    `:1177`).
  - At 270 the punch-hole side is the right, which is the cluster's default side, so both sit on the same edge.
  - Q4's clamp bound (`:790-792`) applies there, but it cannot act, for two reasons:
    - the floor at `:801` keeps the downward limit at the centred position;
    - even without the floor, the upward limit wins at `:816` (`maxOf(maxUpwardOffsetPx, maxDownwardOffsetPx)`), and a
      cluster the height of the window has nowhere to go (the shared cause above).
- **Robolectric:** can see it (`w823dp-h384dp-land`, `ROTATION_270` pinned as `-47` does; bounds of the legend
  against the cluster's rows).
- **A fix changes:** where the legend or the cluster sits in landscape, or the cluster's shape. **Stop, options below.**

### 2. The cluster over the expanded legend (122 px, portrait)

- **Evidence:** Part 1 run record, check 4 (e): the expanded legend's top at 1596, the cluster's last row ending at
  1718, whether or not the cluster was dragged (`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md:721-727`).
- **Cause (portrait):**
  - The clamp's downward limit is `(bottomBoundPx - (H + clusterH) / 2).coerceAtLeast(0f)`
    (`AvailabilityCompactMapUi.kt:800-801`).
  - When the legend's top, less `Spacing.sm`, is above the centred cluster's bottom, the limit floors at 0 and the
    cluster stays centred, over the legend.
  - `-78` cites `:795-797`, and the Part 1 coder read `:785-811` at `26709b1`. At `a0a54f9` the clamp is `:794-817`,
    the floor `:801`, the upper end `:816`.
- **Cause (landscape):** item 1's.
- **Robolectric:**
  - It can see the mechanism.
  - The S22 figure depends on real insets. On the phone the status bar moves the centred cluster down, and the nav's
    measured height includes the system navigation bar (360 px, 128 dp), which lifts the legend.
  - At Robolectric's `w384dp-h823dp`, with zero insets and assuming an 80 dp nav, my arithmetic has the centred cluster
    clearing the expanded legend by about 6 dp. A test must therefore use a viewport where the overlap exists (for
    example `w360dp-h640dp-xhdpi`, used elsewhere in the suite) to see the floor fail for the stated reason.
  - This is arithmetic, not a run.
- **A fix changes (portrait):**
  - The downward limit could go above the centred position when the legend is the binding bound, still under the
    upward (dropdown) bound.
  - That is Q4 as ruled ("the cluster's downward clamp stops above the chip … The clamp uses the chip's current height,
    so the cluster moves up when the legend expands and back down when it collapses",
    `prompts/preserved/2026-09-28-07.md:11-13`), so I read it as within the ruling.
  - It does change the drag range while the legend is expanded. `-78`'s stop list includes "change how the cluster
    drags or snaps", so the planner may want to confirm this reading.
  - On the S22, by my arithmetic, the cluster would rise about 157 px and end about 34 px above the expanded legend.
  - The nav bound's own floor would be left as it is: nothing asks for a change there.
- **Status:** portrait buildable within Q4, by my reading. Landscape is item 1's stop.

### 3. The portrait search dropdown under the keyboard

- **Evidence:** Part 1 run record, check 8: with the keyboard up (inset frame `[0,1396][1080,2316]`), Latitude,
  Longitude and "Search this location" lie below its top edge
  (`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md:1071-1080`).
- **Cause:**
  - The portrait dropdown's height is capped at `maxHeight - searchDropdownTopOffset - bottomNavHeight`
    (`AvailabilityCompactScaffold.kt:1202-1208`). Nothing there subtracts the keyboard.
  - The Map tab's `contentWindowInsets` reserves only the top and the sides in portrait (`:634`), so the keyboard does
    not shrink the space either.
  - So the dropdown is laid out as if no keyboard were up. Its one-shot scroll to the end
    (`AvailabilitySearchUi.kt:500-510`) finds nothing to scroll in portrait, as its own comment says (`:426-427`).
- **A second finding, bearing on the fix:**
  - Capping the height by the keyboard alone would not bring the fields into view.
  - The scroll runs one frame after the section expands, before the keyboard's inset has grown. So the viewport shrinks
    after the scroll, leaving the fields scrolled off the bottom of the shorter dropdown rather than under the keyboard.
  - The scroll would have to run, or run again, once the viewport has settled. That is my reading of the timing, not
    observed.
- **Also seen:** the programmatic scroll does not lower the keyboard (`:1091-1096` of the run record, in landscape).
  - The effect at `AvailabilitySearchUi.kt:420-422` clears focus only if a recomposition sees `isScrollInProgress`
    true.
  - My reading, unverified: an instant `scrollTo` sets it and clears it within one frame, so no recomposition sees it.
  - `-78` records it as part of item 3, but its build line asks only that "the dropdown's fields reach the area above
    the keyboard".
- **Robolectric:** cannot see it. The IME inset is zero there (CLAUDE.md). A test could inject a synthetic IME inset,
  but that tests the wiring against a value I chose, not the phone. **Device-only.**
- **A fix changes:** the dropdown's height cap (the larger of the nav band and the IME inset) in
  `AvailabilityCompactScaffold.kt`, and when the one-shot scroll lands, in `AvailabilitySearchUi.kt`. Both files are in
  scope, and neither is a placement.
- **Status:** buildable within scope, device-only. When the scroll runs is a mechanism choice I would report as a
  decision.

### 4. A tab round trip resets the Maps camera

- **Evidence:** Part 1 run record, flag 6: "Returning to the Maps tab from another tab resets the camera to the location
  at about zoom 12" (`:1212-1214`), and J8's flag 8 (`docs/audits/2026-09-28-j8-device-check-run-record.md:892-895`).
- **Cause:**
  - The camera is the `MapView`'s, and `SightingsMap` creates the `MapView` in a `remember`
    (`ui/map/SightingsMap.kt:267-276`) inside `CompactMapTab`. Only the `CompactTab.MAP` branch composes it
    (`AvailabilityCompactScaffold.kt:808`), so a tab change disposes the `MapView` (`SightingsMap.kt:355-375`), and the
    camera with it.
  - On return, a new `MapView` has `lastAppliedCameraTarget` null (`:348`). Its first data effect moves the camera to
    the display region at `zoomForRadiusKm(region.radiusKm)` (`:655-664`).
  - Before a search, that region is the located fix at `JOURNAL_PICKER_DEFAULT_REGION`'s 15 km
    (`AvailabilityCompactMapUi.kt:554-557`, `AvailabilityOfflineMapsUi.kt:404-405`), and `zoomForRadiusKm(15)` is 12.0
    (`SightingsMap.kt:1809-1814`). That matches the device's "about zoom 12".
  - The planner's prediction 2 holds, as read.
- **What a fix needs that the scope does not give:**
  - A holder above the tab switch, beside `rememberMapIconClusterPositionState()` (`AvailabilityScreen.kt:877`), is in
    scope ("the camera state holder, wherever it lives").
  - Filling it needs the camera's zoom, not only its target. `MapSlot`'s `onCameraIdle` reports a `LatLng` only
    (`ui/map/MapSlot.kt:449`).
  - Restoring it needs a new input reaching `SightingsMap` through `MapRenderMode` and `SightingsMapSlot`, both in
    `ui/map/MapSlot.kt`, which the scope does not name.
  - The existing `MapRenderMode.cameraRequest` (`MapSlot.kt:218`) cannot carry it as it stands. It is skipped while GPS
    tracking owns the camera (`SightingsMap.kt:1078-1082`), and the location component is activated in the same style
    callback that precedes the data effect (`:614`), so the effect meets an active tracker.
- **Design questions no ruling answers:**
  - **What the memory holds.** `-78` asks for "position and zoom". Bearing and tilt are also set by the user, and
    CLAUDE.md's UX default would keep them. Not asked.
  - **Tracking.** Whether live-location tracking, if it was on when the user left, resumes on return, or the camera
    stays where it was.
  - **The zoom-in on activation.** Whether the first-activation zoom-in (`SightingsMap.kt:1207-1209`) is skipped on a
    return.
- **Robolectric:** can see the wiring through the real screen. A round trip hands the saved camera back to a stub map,
  given a stub that reports camera idles with a zoom. MapLibre applying it is device-only.
- **Status:** stop on scope (`MapSlot.kt`) and on the design questions.

### 5. MapLibre's "i" under the nav bar

- **Evidence:** Part 1 run record, flag 3: in portrait the "i" at `[1010,2246][1069,2305]` lies inside the navigation
  bar's frame `[0,2181][1080,2316]`, and at 90 under the bar on the right (`:1206-1207`, and the check 4 (d) table at
  `:696-700`).
- **Cause:**
  - `SightingsMap` sets the "i" to bottom-end (`ui/map/SightingsMap.kt:516`) and never sets its margins, so it sits at
    the `MapView`'s corner with MapLibre's defaults.
  - The `MapView` is full-bleed under the app's nav and the system bar.
  - The caption beside it is padded by `bottomInset` (`:812`), the nav's measured height outside fullscreen
    (`AvailabilityCompactScaffold.kt:827`, `:585-590`). The "i" is not.
- **In portrait outside fullscreen:**
  - `SightingsMap` already has `bottomInset`, so the "i"'s bottom margin could follow it.
  - The legend's clearance above the "i" (`LEGEND_ATTRIBUTION_CLEARANCE`, 32 dp, `ui/map/MapLayersSheet.kt:418`)
    already assumes the "i" sits at `bottomInset`.
  - In scope.
- **At 90 in landscape:**
  - The "i" is at bottom-right, under the overlaid rail and the system bar on the port side. `bottomInset` is 0 there
    (`AvailabilityCompactScaffold.kt:537`).
  - Clearing it needs an end inset: the rail's measured width, or in fullscreen the navigation-bar inset, as
    `mapControlsPadding` computes (`:550-562`).
  - `SightingsMap` has no such input. Reaching it needs `MapRenderMode` and `SightingsMapSlot` in `ui/map/MapSlot.kt`,
    outside the scope.
  - Reading the system insets inside `SightingsMap` would clear the system bar but leave the "i" under the rail, which
    is an app overlay.
- **In fullscreen:**
  - `bottomInset` animates to 0 (`AvailabilityCompactScaffold.kt:585-590`). A code comment says the owner reversed an
    earlier version that kept the caption above the system bar (`:570-575`). That reversal is not recorded with the
    owner's words in `docs/audits/`, so I do not treat it as a closed decision.
  - Whether the "i" in fullscreen follows the caption to the true bottom edge, under the three-button bar, or clears
    the system bar as `-78`'s "clear the real system bars" says, is a question.
- **Robolectric:** cannot see it. `SightingsMap` is the native `MapView` and every screen test replaces it with a stub
  (for example `AvailabilityScreenInAppCameraTest.kt:115`), and the insets are zero. **Device-only.**
- **Status:** portrait outside fullscreen buildable within scope. Landscape is a stop on scope (`MapSlot.kt`), and
  fullscreen a question.

### 6. The attribution strip across the cluster at 90

- **Evidence:** Part 1 run record, flag 4, by eye, not measured (`:1208-1209`).
- **Cause:**
  - The caption is aligned `BottomStart` in `SightingsMap`'s Box and padded only at the bottom, by `bottomInset`
    (`ui/map/SightingsMap.kt:806-815`). `bottomInset` is 0 in a short landscape window
    (`AvailabilityCompactScaffold.kt:537`).
  - At 90 the cluster is on the start side and reaches to 22.5 px above the bottom (the shared cause).
  - The cluster is composed after the map (`AvailabilityCompactMapUi.kt:637-678` against `:951`), so it is drawn over
    the caption.
- **Also, from the code, not observed:** at 270 the caption's start is the left, which is the port side there, so the
  caption would lie under the system navigation bar (left, `[0,0][135,1080]` in the Part 1 record) and the rail. See
  Flags.
- **Robolectric:** cannot see it. The caption is drawn inside `SightingsMap`, which no screen test composes.
  **Device-only.**
- **A fix changes:** the caption's position (a start inset past the cluster and the controls padding), or the cluster
  (the shared cause). Either way a position is chosen. **Stop, options below.**

### 7. J8's chip over "Reset orientation to north" at 90

- **Evidence:** J8 check 5: at 90 the chip's node `[75,236][536,371]` covers the reset button `[98,264][233,399]`, and
  five of five real touches in the overlap opened the chip's list. At 270 they are 442 px apart
  (`docs/audits/2026-09-28-j8-device-check-run-record.md:672-707`).
- **Cause:**
  - In landscape the chip row sits under the search bar, in a column the bar's width, on the punch-hole side, "aligned
    to the bar's start" (`AvailabilityCompactMapUi.kt:1095-1104`).
  - At 90 the bar's start is the left edge, the cluster's side, so the chip meets the cluster's second row (the shared
    cause).
  - The row is composed after the cluster (`:1090-1121` against `:951-1038`), so it takes the touch.
  - At 270 the bar's start is its inboard end, away from the cluster.
- **Robolectric:** can see it (`w823dp-h384dp-land`, `ROTATION_90`; the chip's bounds against the cluster's rows).
- **A fix changes:** where the chip sits in its row, where the search bar and chip start, or the cluster. J8's ruling
  is "Top, by the species chip". The alignment to the bar's start is landscape B2's S3; I did not trace whether S3 was
  the owner's ruling or a planner resolution. **Stop, options below.**

### 8. The portrait dead band between the chip and the coordinate readout

- **Evidence:** J8 check 4: the chip's node `[310,273][771,408]` and the readout's `[564,163][902,298]` overlap by
  25 px. Five real touches there reached neither control; the chip's centre and the pill's top edge opened the list,
  and the readout's centre switched its form
  (`docs/audits/2026-09-28-j8-device-check-run-record.md:590-605`, `:623-631`).
- **Cause, as far as I could establish it:**
  - Both touch areas reach the band only by Compose's minimum-touch-target expansion:
    - The chip is a Material3 `Surface(onClick)` (`ui/map/JournalEntriesChip.kt:112-127`). Its 33 dp pill is centred
      in a 48 dp layout, and its clickable is the pill, so the band above the pill (y 273 to 294) is expansion.
    - The readout is a plain `Text(...).clickable(...)` (`ui/availability/AvailabilityMapControlsUi.kt:440-448`)
      about one text line tall. Its 48 dp area reaches about 44 px below the strip's drawn edge at 254.
  - The map under both is an `AndroidView`, a direct hit everywhere.
  - Partly read from the pinned `compose-ui` 1.12.0 `HitTestResult` bytecode: a direct hit is recorded at distance -1
    and `hit()` removes the near hits recorded after it, and `hasHit()` is true only for a direct hit, so near hits
    alone do not end the search. I did not read the traversal loop.
  - So my reading is that the map's direct hit beats both expanded hits. That fits the device result ("neither"; "the
    map, presumably", J8 record `:629-630`), but it is not established.
- **Consequence for the fix:**
  - If that reading holds, moving the chip row down clear of the readout's area would not meet "every point there must
    reach exactly one control". The band would then lie only in the readout's expansion, which also loses to the map.
  - The band reaches one control only if it lies inside a control's own bounds. That means the chip's clickable
    covering its whole 48 dp layout (`ui/map/JournalEntriesChip.kt`), or the readout's clickable having a real 48 dp
    layout (`ui/availability/AvailabilityMapControlsUi.kt`, which would also make the strip taller).
  - Neither file is in `-78`'s scope. The host (`AvailabilityCompactMapUi.kt`) cannot reach the chip's clickable.
- **Robolectric:**
  - Can see it only with a stub map that takes pointer input the way the real `AndroidView` does. The existing stubs
    are plain `Box`es with no pointer input (for example `AvailabilityScreenInAppCameraTest.kt:115`).
  - With a plain stub, my expectation is that the chip would take the band at base. The tests-first test would then pass
    at base, which is itself an abort condition. That is a prediction, not a run.
  - The geometry is dp-based (`topInset` and the clearance are text measurements, `AvailabilityCompactScaffold.kt:352`,
    `AvailabilityCompactMapUi.kt:622-626`), so it does not depend on insets.
- **Status:** stop on scope (`JournalEntriesChip.kt` or `AvailabilityMapControlsUi.kt`).

### 9. The cluster's top row over the search bar's ends in landscape

- **Evidence:** J8's flag 6: over the search icon at 90 (crop `107-`) and over the bar's right end at 270 (`101-`)
  (`docs/audits/2026-09-28-j8-device-check-run-record.md:888-889`).
- **Cause:**
  - The search bar sits at the top on the punch-hole side, at its capped width (`AvailabilityCompactScaffold.kt:949-956`,
    width `:789-799`). Its punch-hole end is at the cluster's edge at both rotations.
  - The cluster fills the height, so its first row is at y 118 against the bar's drawn bottom at 211 (the shared cause).
  - The bar is composed before the cluster (`AvailabilityCompactMapUi.kt:678` against `:951`), so the cluster is drawn
    over it and takes the touch there.
- **Robolectric:** can see it (`w823dp-h384dp-land`, both rotations; the bar's bounds against the cluster's first row).
- **A fix changes:** where the search bar starts, or the cluster. **Stop, options below.**

## Options (for the planner and the owner; I have not chosen)

**For the shared landscape cause (items 1, 2 in landscape, 6, 7, 9).** Each needs a ruling.

- **A. Make the cluster fit in landscape.**
  - Change its shape in a short landscape window so that it fits between the search bar and the legend. For example,
    ControlPill beside MapIconBar as a second column, or fewer rows.
  - This removes the collisions at the source and would also end the clipping in Flags.
  - Landscape design P6 says "Drag, snap and minimise are unchanged". Shape is not named there, but it is a new cluster
    layout.
- **B. Keep the cluster, and move the other chrome off its column when they share an edge.**
  - The search bar and J8's chip start inboard of the cluster (7, 9). The legend sits inboard of the cluster at the
    bottom (1). The caption starts past it (6).
  - Every one of these is a new position. The legend would no longer be over the "i" as ruling 5 has it ("Bottom-right,
    above the 'i'", `docs/plans/journal-redesign.md:512`), and the bar would no longer sit at the punch-hole edge as B2
    placed it.
- **C. Move the cluster's landscape default to the port side.**
  - Contradicts P6 and R3 ("its default side is the punch-hole side").
  - At 90 the port side is where the legend and the "i" are; at 270 it is where the strip (S4, top corner on the rail
    side) and the caption are. It moves the collisions rather than removing them.
- **D. For item 1 only, move the legend to the bottom corner away from the cluster in landscape.**
  - Contradicts ruling 5 and L0 ruling 2 ("the bottom corner opposite the attribution",
    `docs/plans/journal-redesign.md:494`), since the caption is at bottom-start.
- **E. For item 7 only, align J8's chip to the bar's inboard end when the cluster shares the bar's side.**
  - Keeps "Top, by the species chip" but changes S3's "aligned to the bar's start". It does not help item 9.

**Item 4:** widen the scope to `ui/map/MapSlot.kt` (a zoom-carrying camera report out of the map and a saved camera
into it, on `MapRenderMode`), and rule the design questions: bearing and tilt kept or reset, tracking resumed or not,
and the activation zoom-in skipped on a return.

**Item 5:**
- widen the scope to `ui/map/MapSlot.kt` for an end inset in landscape;
- rule whether the "i" follows the caption to the true bottom edge in fullscreen, or clears the system bar.

Without the scope, only portrait outside fullscreen can be fixed.

**Item 8:** widen the scope to one of `ui/map/JournalEntriesChip.kt` (the chip's clickable covers its whole 48 dp
layout) or `ui/availability/AvailabilityMapControlsUi.kt` (the readout gets a real 48 dp layout, and the strip grows).
The taxon chip shares the chip row and, by the same reading, has the same margins (see Flags).

## What could be built now, if the planner narrows the work

By my reading these need no new ruling and stay in scope. Each would be pre-registered and seen failing before it is
built.

- **Item 2, portrait.**
  - The change: a legend-bound downward limit that may go above the centred position (`AvailabilityCompactMapUi.kt:798-816`).
  - Its test: a real-screen test with the legend expanded, at a viewport where the centred cluster overlaps it,
    asserting that the cluster's bounds do not intersect the legend's. It should fail at base on the floor at `:801`.
  - A revert check: restore the floor.
- **Item 3.**
  - The change: the dropdown's cap by the IME inset, and the one-shot scroll landing once the viewport has settled.
  - Device-only, with no Robolectric evidence possible beyond a synthetic-inset wiring test, if the planner wants one.
- **Item 5, portrait outside fullscreen.**
  - The change: the "i"'s bottom margin from `bottomInset`, in `SightingsMap`.
  - Device-only, with no Robolectric test possible (native `MapView`).

## Premises re-verified

The launch message asked for every file:line to be re-verified. The ones that moved:

| cited | where | at `a0a54f9` |
|---|---|---|
| the clamp, `AvailabilityCompactMapUi.kt:795-797` | `-78` item 2, launch message | `:794-817`; the floor `:801`; the upper end `:816` |
| the clamp, `:785-811` at `26709b1` | Part 1 record `:726` | as above |
| the legend chip, `:1149-1161` (corrected from `:1150-1161`) | Part 1 record `:709`, `:1231` | `:1170-1184` |
| the "i", `SightingsMap.kt:513` | Part 1 record `:129` | `:516` |
| the chip's `FlowRow`, `:1089-1119` | J8 record `:324` | `:1090-1121` |
| the landscape chip place, `:1094-1103` | J8 record `:620` | `:1095-1104` |
| the portrait chip padding, `:1104-1108` | J8 record `:325` | `:1105-1110` |
| the strip clearance, `:621-625` | J8 record `:326` | `:622-626` |

`-78`'s own description of item 2 ("The clamp's downward limit floors at the centred position") is right about the
mechanism. `-88`'s descriptions of items 7 to 9 match the J8 record. J8 follow-ups' removal of the chips' shadow does
not bear on any of the nine. `-79`'s night border does not either.

## The planner's predictions, against the code

1. "Items 1 and 2 share one cause in the legend and cluster clamp." **In part.**
   - Item 2 in portrait is the clamp's floor (`:801`).
   - Item 1 is the landscape geometry. The floor also stops Q4's bound there, but removing it would not fix item 1,
     since the upward limit wins at `:816` and the cluster fills the height.
   - The same landscape geometry is behind 6, 7 and 9, which the prediction did not cover.
2. "Item 4 is a camera state held in composition that resets when the map leaves composition." **Holds, as read.**
   The camera is the `MapView`'s, and the `MapView` is in a `remember` that leaves with the tab.
3. "Items 3, 5 and 6 are device-only." **Holds.**
   - 3 and 5 are device-only by construction (the IME and system-bar insets, and the native `MapView` for 5).
   - 6 is device-only because the caption is inside `SightingsMap`, which no screen test composes. Its geometric cause
     is not inset-dependent.

## D58

Before this push I ran a case-insensitive `grep` for the three phrases D58 forbids
(`/tmp/layout-fixes-probe/d58.sh`, outside the repository). It is the J8 follow-ups coder's script, with its path
changed. I confirmed that its three phrases are the three D58 names, in forager-forecast
`origin/main:docs/planning/DECISIONS.md:10`. It covers `git diff a0a54f9`, every commit message since `a0a54f9`,
named files and every file in `app/`, and it carries a positive control. Run with this file named, before its
commit: diff 0, messages 0, named files 0, files in `app/` with a hit 0, positive control 1. The run after the commit,
covering the commit message, is in the hand-back.

## Not done, and not tested

- **No test was written and no build or suite was run.** There is no tests-first evidence, no revert check and no
  suite count of mine. No Gradle process was started.
- **Nothing was checked on a device.** No phone or emulator was touched, as the launch message said.
- The Compose hit-test reading for item 8 is partial: `HitTestResult` only, not the traversal loop.
- The landscape cluster's measured height is inferred from the code and the J8 dump's row positions, not measured.
- The Robolectric figures for item 2 (about 6 dp of clearance at `w384dp-h823dp`) are arithmetic, not a run.

## Device-only list, for whatever is built

- **Item 3:** in portrait, after one touch on the bar with the keyboard up, Latitude, Longitude and "Search this
  location" lie above the keyboard's inset frame (`dumpsys window`), without the user's scrolling. Also recorded:
  whether the keyboard is lowered.
- **Item 5:** in portrait outside fullscreen, the "i"'s node lies above the app's nav and outside the system bar's
  frame, and a touch on it opens MapLibre's attribution dialog.
  - At 90 and 270, the same against the rail and the system bar, if the landscape half is built.
  - In fullscreen, whichever way the question is ruled.
- **Item 6:** at 90 and 270, the caption's pixels are not covered by the cluster, the rail or the system bar, if a
  ruling allows a fix.
- **Item 2, as a device confirmation:** in portrait with the legend expanded, the cluster's last row ends above the
  legend's top, at 0 only.
- **Items 1, 7, 9, if fixed:** the bounds at 0, 90 and 270 on the S22. Robolectric sees the geometry, but the phone's
  status bar and cut-out change the numbers.
- **Item 4:** on the S22, pan and zoom away, change tab and return: the camera is where it was left (and at the
  bearing, if ruled so).

## Decisions I made

1. **I stopped the dispatch at verification, rather than building the three items that need no ruling** (2 in
   portrait, 3, and 5 in portrait outside fullscreen) and stopping only on the rest. I read "an unruled placement" in
   the abort list as stopping the dispatch. The per-item reading ("Stop and report options where a fix would…") was
   also open. Deciding it properly needed the planner's word on whether the abort list is per item.
2. **I counted item 6 as a placement stop**, though the stop rule's lists name the legend, the cluster, the chip and
   the search bar and not the caption. Any fix picks a new caption position, "Do not choose between placements"
   applies, and the cause is the same as items 1, 7 and 9.
3. **I read "the camera state holder, wherever it lives" as not covering `ui/map/MapSlot.kt`,** and asked instead.
4. **I read Q4 as covering a cluster that rises above centre when the legend expands** (item 2 in portrait), and noted
   that this changes the drag range while the legend is open. The planner may read "change how the cluster drags" as
   covering it.
5. **I treated the fullscreen caption reversal as not a closed decision,** since it is recorded only in a code comment
   (`AvailabilityCompactScaffold.kt:570-575`), not with the owner's words.
6. **D58:** the dispatch names D58 without saying where it is defined. I used the J8 follow-ups coder's phrase list
   after checking it against forager-forecast `origin/main`'s D58 row.
7. **Beyond the dispatch:** I read `compose-ui` 1.12.0's `HitTestResult` bytecode to inform item 8's options.
8. **I pushed this stop report to `journal-redesign`,** not to `layout-fixes-wip`. It changes no code, so it cannot
   change the suite, and the launch message sends commits that leave the suite passing there.
9. **I created the worktree at the launch message's path,** not in the session's own default worktree.
10. **Scratch files** (the quote extraction, the D58 script and the bytecode extract) are in `/tmp/layout-fixes-probe`,
    outside the repository.

## Flags outside scope

1. **In a short landscape window the icon cluster does not fit** (inferred; see the shared cause). If the container is
   laid out at the map area's height, ControlPill is squeezed. On the S22 its "Return to vehicle" row would show about
   17 px of 135, and under Robolectric's `w823dp-h384dp-land` about 36 of 48 dp. The J8 dumps still list the node, which
   does not show whether it is visible.
2. **Every sub-48 dp control over the map may have dead touch margins,** not only the overlap in item 8. This is by my
   partial reading of the hit test: an expanded-touch hit loses to the map's direct hit.
   - The taxon chip shares the chip's row and shape.
   - The readout's area below the strip is expansion too.
   - The J8 check touched only the overlap and the pill, so the rest is untested.
3. **The first-activation zoom-in to 16 appears not to hold.**
   - Opening and returning land at 12.0, `zoomForRadiusKm(15)`, not at `FIRST_ACTIVATION_ZOOM` 16
     (`SightingsMap.kt:1207-1209`, `:1218`).
   - One explanation, unverified: MapLibre drops tracking on an API camera ease, after which the region move at
     `:655-664` runs at zoom 12.
4. **At 270 the attribution caption is probably under the system navigation bar and the rail.** It is bottom-start
   with no start padding (`SightingsMap.kt:806-815`), and at 270 the left is the port side. This is from the code; no
   check has looked.
5. **The dropdown's programmatic scroll does not lower the keyboard,** which the code comment at
   `AvailabilitySearchUi.kt:431-433` says it does. My reading, unverified: an instant `scrollTo` never shows
   `isScrollInProgress` to a recomposition.
6. **The upward clamp limit is bounded by the downward fallback distance** (`AvailabilityCompactMapUi.kt:811-812`,
   `coerceIn(-fallbackDownwardOffsetPx, 0f)`). That ties how high the cluster may be dragged to how far down it could
   go. It does not bind in item 2's case by my arithmetic. Noted, not investigated.


# Resumed (planner messages `2026-09-28-98` and `2026-09-28-99`)

Everything above this heading is as pushed at `b0fb443` (merged at `70cdaba`), and I have not edited it. The stop it
records was answered by two planner messages. This section is written after merging `e95aff8`, before any test is
written or run, and is pushed before the first Gradle run.

## The planner's messages, verbatim

### `2026-09-28-98`, from `prompts/preserved/2026-09-28-98.md` at `e95aff8` (sha256 `ce66b27b…d46e`), below its "verbatim message follows" line

> Planner message `2026-09-28-98`, part of dispatch `2026-09-28-78` as amended by `-88`. Quote it verbatim in your report. It answers your stop at `b0fb443`.
>
> **Build items 2 (portrait), 3, 4, 5 and 8 now.** Items 1, 2 (landscape), 6, 7 and 9 wait for the owner's ruling on the landscape cluster. The planner has put your options A to E to the owner. Do not build them yet.
>
> **Your Decision 1** is accepted: the stop covered the whole dispatch. From here, each item is built or held on its own.
>
> **Item 2 (portrait): build it.** Your Decision 4 is confirmed by the planner. The owner's Q4 ruling ("the cluster moves up when the legend expands") covers the cluster rising above centre while the legend is expanded. That is not "changing how the cluster drags"; it is the ruled response to the legend. Use a viewport short enough for the test to fail at base, and say which.
>
> **Item 3: build it**, including the timing problem you found. The fields must end up visible above the keyboard, not scrolled off the bottom. Mark it device-only.
>
> **Item 4, the camera surviving a tab round trip: build it.** Planner's rulings under CLAUDE.md's UX defaults ("What the user has set survives navigating away and back within a session"):
> - The scope widens to `ui/map/MapSlot.kt`, and to `MapRenderMode` and `SightingsMapSlot` for carrying the camera.
> - On return, restore exactly the camera the user left: target, zoom, bearing and tilt. Restore the tracking mode they left too: following if they were following, not following if they had panned away.
> - Do not run the first-activation zoom-in or the region move on return.
> - Within a session only; nothing persists across a restart.
> - Tests first with a stub map for the wiring. MapLibre applying the camera is device-only.
>
> **Item 5, MapLibre's "i": build portrait and landscape.**
> - The scope widens to `ui/map/MapSlot.kt` for the end inset.
> - Planner's ruling for fullscreen: the "i" follows the attribution caption's own edge treatment. It belongs with the caption.
>
> Mark it device-only.
>
> **Item 8, the portrait dead band: build it.**
> - The scope widens to `ui/map/JournalEntriesChip.kt` and `AvailabilityMapControlsUi.kt` (the readout).
> - Every point in the band must reach exactly one control, without moving either control. Adjust touch targets only.
> - Tests first with a stub map that takes pointer input, so the test can fail at base, and several real touches across the band.
>
> **Held for the owner:** items 1, 2 in landscape, 6, 7 and 9.
>
> **Everything else in `-78` and `-88` stands:** tests first, revert checks per item, the full suite, the device-only list, no new copy, and a "Resumed" section in your report.
>
> **Sharing the machine:** the planned-trips fix (`-97`) may launch while you work. It changes `AvailabilityCompactMapUi.kt:641` and its comment only. Merge its pushes with `--no-rebase`, keeping both sides' intent. Before each Gradle run, check that no other Gradle build is running and that 2.5 GB is free.

### `2026-09-28-99`, from `prompts/preserved/2026-09-28-99.md` at `e95aff8` (sha256 `f5784119…abc`), below its "verbatim message follows" line

> Planner message `2026-09-28-99`, part of dispatch `2026-09-28-78`, as amended by `-88` and `-98`. Quote it verbatim in your report. It releases the held items: 1, 2 in landscape, 6, 7 and 9.
>
> **The owner's ruling, verbatim:** "For icon column in short landscape: option A". Option A, as the planner put it to the owner: "Reshape the column in landscape. For example, put the record and return pill beside the icon bar instead of below it, so the column is about 264 dp tall and fits with room to spare. Nothing else moves."
>
> **Build:**
> - **In short landscape only,** lay the ControlPill beside the MapIconBar instead of below it. The cluster's height becomes the icon bar's (about 264 dp), not 380 dp.
> - **Portrait is unchanged.** Nothing else moves: the search bar, J8's chip, the legend, the caption, the rail and the cluster's side and drag all stay as ruled.
> - **Details are yours to propose and state:** which side of the bar the pill sits on, its vertical alignment, and the gap between them.
>   - **The planner's lean:** the pill on the bar's inboard side (towards the screen's centre), bottom-aligned to the bar. The bar's edge then stays at the screen edge where it is today, and the record button stays near the bottom where a thumb finds it.
>   - If you choose otherwise, say why.
> - **The drag and snap** must work on the new shape, with both handles. If the reshaped cluster needs a handle or drag change beyond its new size, stop and report.
> - **Then re-measure items 1, 2 (landscape), 6, 7 and 9** against the new height, at rotations 90 and 270, using `-78`'s bounds tests.
>   - If an item still collides because of room (the map area is about 354 dp, so about 90 dp is spare), **stop on that item and report its geometry.** Do not move the other chrome.
>   - Item 7 (J8's chip over "Reset orientation to north") may still overlap on width. If it does, report it, with your option E, rather than choosing.
>
> **Tests first** at `w823dp-h384dp-land`, both rotations pinned:
> - the cluster fits the map area;
> - none of the chip, search bar, legend or caption intersects it, whether the legend is collapsed or expanded;
> - the pill's controls are still reachable by real coordinate touches, sampled across their bounds.
>
> Add revert checks, the full suite, and the device items: the reshaped cluster on the S22 at 90 and 270, with thumb reach judged by the owner.
>
> **Everything else in `-78`, `-88` and `-98` stands.**

### The coordinator's relay of `-98`, as delivered to me

> This is planner message `2026-09-28-98`, part of dispatch `2026-09-28-78` as amended by `-88`. The full text is at `prompts/preserved/2026-09-28-98.md` on `origin/journal-redesign` at `2f3c21a`. Fetch it, read it in full and quote it verbatim; the file governs over this summary.
>
> **Build now:**
> - **Item 2, portrait only.** The Q4 ruling covers the cluster rising above centre when the legend expands. Use a viewport short enough to fail at base.
> - **Item 3,** including your scroll-timing finding. Device-only.
> - **Item 4.** Scope widens to `MapSlot.kt`, `MapRenderMode` and `SightingsMapSlot`. On return, restore target, zoom, bearing, tilt and the tracking mode the user left, with no first-activation zoom-in or region move. Within a session only. Test the wiring with a stub map.
> - **Item 5, portrait and landscape.** Scope widens to `MapSlot.kt`. In fullscreen the "i" follows the caption's own edge treatment. Device-only.
> - **Item 8.** Scope widens to `JournalEntriesChip.kt` and `AvailabilityMapControlsUi.kt`, touch targets only. Every point in the band reaches exactly one control. Use a stub map that takes pointer input, and several real touches across the band.
>
> **Held for the owner's landscape-cluster ruling:** items 1, 2 in landscape, 6, 7 and 9.
>
> **Everything else in `-78` and `-88` stands:** tests first, a revert check per item, the full suite, the device-only list, and a "Resumed" section.
>
> **Sharing the machine:** the planned-trips fix (`-97`) may run alongside you, changing only `AvailabilityCompactMapUi.kt:641` and its comment. Merge its pushes with `--no-rebase`. Before Gradle, check that no other build is running and that 2.5 GB is free. Hand back when you finish or stop.
>
> Address this before completing your current task.

### The coordinator's relay of `-99`, as delivered to me

> This is planner message `2026-09-28-99`, part of dispatch `2026-09-28-78` as amended by `-88` and `-98`. The full text is at `prompts/preserved/2026-09-28-99.md` on `origin/journal-redesign` at `e95aff8`. Fetch it, read it in full and quote it verbatim; the file governs.
>
> **The owner's ruling, verbatim:** "For icon column in short landscape: option A". That means reshaping the cluster in short landscape only: the ControlPill sits beside the MapIconBar instead of below it, so the cluster is about 264 dp tall. Portrait is unchanged, and nothing else moves.
>
> - **Placement details are yours to propose and state.** The planner's lean: the pill on the bar's inboard side, bottom-aligned.
> - **Drag and snap** must work on the new shape. A handle change beyond its size is a stop.
> - **Then re-measure items 1, 2 (landscape), 6, 7 and 9** at 90 and 270. An item that still collides for lack of room is a stop, reported with its geometry. For item 7, report the width overlap with option E; don't choose.
> - **Tests first** at `w823dp-h384dp-land`, both rotations: the cluster fits; the chip, bar, legend and caption don't intersect it; real touches across the pill's controls reach them.
> - **Carry on with `-98`'s items** as you were. Hand back when you finish or stop.
>
> Address this before completing your current task.

The two relays each end with a line the harness adds ("Address this before completing your current task."). I quote
them as delivered. The files govern.

## What I read these as

- **Built now:** items 2 (portrait), 3, 4, 5 (portrait and landscape) and 8 (`-98`). The landscape reshape follows the
  owner's ruling "For icon column in short landscape: option A" (`-99`).
- **Re-measured after the reshape:** items 1, 2 (landscape), 6, 7 and 9, at 90 and 270. An item that still collides
  for lack of room is a stop on that item, reported with its geometry. For item 7, a width overlap is reported with
  option E.
- **Scope widened by the planner:**
  - `ui/map/MapSlot.kt` (`MapRenderMode`, `SightingsMapSlot`), for items 4 and 5;
  - `ui/map/JournalEntriesChip.kt` and `ui/availability/AvailabilityMapControlsUi.kt`, touch targets only, for item 8.
- These widenings and the item 4 and 5 design answers are planner rulings on questions I raised in the stop report,
  not owner rulings. I read them as the planner "ruling on a question you raised". See Decisions.

## The design I will build (stated before building, as `-99` asks for the reshape)

**Reshape (option A), short landscape only.**
- The cluster container's content becomes a Row of MapIconBar and ControlPill (inside `TrailheadControls`) instead of
  the Column (`AvailabilityCompactMapUi.kt:979-1027`). Portrait keeps the Column untouched.
- **The pill's side is the bar's inboard side,** towards the screen's centre, the planner's lean. The bar keeps the
  screen edge, so the minimise handle, which straddles the container's outer edge at the bar's mid-height, is
  unchanged. On the left side the order is bar then pill; on the right, pill then bar.
- **Vertical alignment:** bottom, the planner's lean. The record button stays low, where a thumb reaches.
- **Gap:** `CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR` (`Spacing.sm`, 8 dp, `:1407`), the same gap portrait has between
  them.
- **Size:** the container is 264 dp tall (the bar's height) and 104 dp wide (48 + 8 + 48).
- **A consequence I am stating, not choosing:** the container stays one Surface with one fill and border
  (`:962-977`), as the portrait gap is today (`:1395-1406`). So the space above the bottom-aligned pill, 56 dp wide by
  156 dp tall, is container fill and takes touches instead of the map. The owner can judge it on the S22.
- **Drag, snap and the handles are unchanged.** They read the container's measured height (`:465`, `:974-976`) and
  the bar's measured centre (`:382`, `:1004-1006`). With the bar in a Row, `boundsInParent` is still relative to the
  container's content, so both keep their meaning. If a test shows a handle or drag needs more than that, I stop.

**Item 2 (portrait).**
- Q4's legend bound may lift the cluster above centre (`clampMapIconBarVerticalOffset`, `:794-817`).
- When the legend's bound, less `Spacing.sm`, is above the centred cluster's bottom, the downward limit becomes that
  bound's (negative) offset instead of 0 (`:801`).
- The upward limit is widened only as far as that lift needs, and never past the dropdown's top. Where the two
  conflict, the dropdown bound wins, as at `:816`.
- The nav bound's floor and every case without a legend are unchanged.

**Item 3.**
- The portrait dropdown's cap subtracts the larger of the nav band and the IME's bottom inset, on the Maps tab only
  (`AvailabilityCompactScaffold.kt:1202-1208`). Other tabs already shrink with the IME through `safeDrawing` (`:634`
  against `:636`).
- In `SearchDropdown`, the one-shot scroll to the end (`AvailabilitySearchUi.kt:500-510`) keeps the end in view each
  time the viewport's size changes (`ScrollState.viewportSize`), until the user drags the dropdown
  (`ScrollState.interactionSource`, a `DragInteraction.Start`) or it closes.
- Content growing inside the viewport (a section the user opens) does not re-scroll, because that changes the maximum
  and not the viewport.
- The one-shot is still armed once per opening, as `-42` ruled.

**Item 4.**
- A plain class `MapCameraMemory`, whose `saved` field is deliberately not Compose state, so a camera idle recomposes
  nothing. It holds a `MapCameraSnapshot`: target, zoom, bearing, tilt, `following` (the location component tracking),
  and the region target the map last moved to (`lastAppliedCameraTarget`, `SightingsMap.kt:348`).
- It is remembered in `AvailabilityScreen` beside the cluster's holder (`:877`), threaded through `CompactMainScaffold`
  to `CompactMapTab`, and handed to the map on `MapRenderMode.cameraMemory`. `SightingsMapSlot` forwards it.
- `SightingsMap` writes the snapshot on every camera idle (`:465-505`).
- On a new `MapView`'s first style load, if a snapshot exists, `SightingsMap` does three things:
  - sets the camera to it;
  - sets `lastAppliedCameraTarget` to the snapshot's, so an unchanged target makes no region move (`:655-664`), while
    a new search still moves the camera;
  - activates the location component with the saved mode (TRACKING if following, NONE if not), which skips the
    first-activation zoom-in (`:1207-1209`).
- Session only; nothing is stored. The restore decision is a pure function, `cameraRestoreFor`, in `SightingsMap.kt`.

**Item 5.**
- `SightingsMap` sets MapLibre's attribution margins to its defaults plus, at the bottom, `bottomInset` (the caption's
  own, already animated to 0 in fullscreen, `AvailabilityCompactScaffold.kt:585-590`), and at the "i"'s end side a new
  `MapRenderMode.attributionEndInset`.
- The scaffold sets that end inset to the rail's measured width when the rail is on the "i"'s end side (the right in
  a left-to-right layout) outside fullscreen, and 0 otherwise, animated like the bottom inset.
- In fullscreen both are 0: the caption's own edge treatment, per `-98`.
- **At 270 the end side is the punch-hole side.** The end inset is then 0 and the "i" stays where it is, in the
  cut-out band. Padding it inboard of the cut-out would put it under the cluster's column, against "clear the
  cluster". See Decisions.
- The margin arithmetic is a pure function, `attributionMarginsPx`.

**Item 8.**
- In `JournalEntriesMapChip` the chip's clickable moves outside its minimum-interactive box, so the whole 48 dp box
  is the chip's own bounds: a direct hit, which wins over the map (`JournalEntriesChip.kt:112-127`).
- The pill is drawn exactly as before, with its ripple clipped to the pill. The tagged node's bounds (the 48 dp box)
  are unchanged.
- The readout is not changed. Every point of the band is inside the chip's box, so the chip alone takes it.

## Pre-registration: tests, pass conditions and predictions

Every screen test drives the real `AvailabilityScreen` with real coordinate touches. Rotation is pinned with
`ShadowDisplay.setRotation`, and the test asserts that the screen saw it, as `AvailabilityScreenLandscapeB2Test.kt:103-122`
does. "Base" below means `e95aff8` plus an inert API added in the tests-first commit so the tests compile:
- `MapCameraMemory` and `MapCameraSnapshot`;
- `MapRenderMode.cameraMemory` (default null) and `MapRenderMode.attributionEndInset` (default 0 dp);
- `cameraRestoreFor`, returning null (no restore, today's behaviour);
- `attributionMarginsPx`, returning the defaults unchanged.

None of it is read by production code until the build.

**Geometry assumed for the predictions** (Robolectric, zero insets):
- MapIconBar is 264 dp and ControlPill 108 dp (`MapChrome.kt:437-477`, `AvailabilityMapControlsUi.kt:178-227`), so
  the portrait cluster is 380 dp.
- The nav is 80 dp (`NavigationBar`, `AvailabilityNavigationUi.kt:149`).
- The legend's bottom is `bottomInset + 32 dp`. Collapsed it is about 36 dp tall; expanded it is capped at 96 dp
  (`MapLayersSheet.kt:418`, `:432`, `:460-478`).
- The search bar is `searchBarHeight`, about 45 dp (`AvailabilityCompactScaffold.kt:352`). The dropdown's top is
  about 61 dp.

### A. `LayoutFixesLegendPortraitTest`, `w384dp-h740dp-xxhdpi`, `ROTATION_0` (item 2)

- **T2.** With the forecast fields on, the legend collapsed and the cluster at its default place, the cluster's
  bounds do not intersect the legend chip's. Then, after a real touch expands the legend, the same holds. Then, after
  a touch collapses it, the cluster is back where it started, within 1 dp.
- **Why `h740dp`:** by the geometry above, the centred cluster ends at about 560 dp and the expanded legend starts at
  about 532 dp, so it overlaps at base. The collapsed legend starts at about 592 dp, so it clears at base. The lift
  the fix needs, about 36 dp, fits under the dropdown's top.
- **Predicted at base:** the collapsed half passes; the expanded half **fails**, with the cluster's bottom about
  28 dp below the legend's top. The cause is the floor at `:801`.

### B. `LayoutFixesPortraitTest`, `w384dp-h823dp-xxhdpi` (the S22's portrait size), `ROTATION_0` (items 4 and 8)

- **T4.** On the Maps tab the map stub is handed `renderMode.cameraMemory`. The test writes a snapshot into it
  (target, zoom 15.5, bearing 30, tilt 10, not following), then makes real touches on the nav's "List" and then
  "Maps". After the round trip, the map is handed a memory holding that same snapshot.
  - **Predicted at base: fails**, "expected the snapshot but was null": nothing hands the map a memory, and none
    survives the tab change.
- **T8 (six tests, one touch each).** A saved entry is shown on the map (so J8's chip shows) and a live fix is set (so
  the coordinate readout shows). The map stub takes pointer input and counts taps, as the real `AndroidView` does.
  - **The band** is the chip's tagged bounds intersected with the readout's touch area: the readout text's own bounds,
    extended to 48 dp tall about its centre, which is Compose's minimum touch target.
  - A guard in each test: the band must be at least 2 dp in each direction, or the test fails and says so.
  - One real touch per test, at six points spread across the band: x at 1/6, 1/2 and 5/6 of its width, and y at 1 dp
    below its top and 1 dp above its bottom.
  - **Pass:** exactly one of these happens, and the map's tap count does not change:
    - the chip's list opened ("Hide all" is present);
    - the readout switched to its decimal form.
  - **Predicted at base: all six fail, with the map taking the tap and neither control reacting.** This rests on my
    partial reading of `HitTestResult` (the map's direct hit beats both expanded hits). If any of the six passes at
    base, that prediction is wrong and I stop.

### C. `LayoutFixesShortLandscapeTest`, `w823dp-h384dp-land`, `ROTATION_90` and `ROTATION_270` (reshape, items 9 and 5)

Each test below runs at both rotations.
- **TR1, the cluster fits.** The cluster's height is at most the MapIconBar rows' span plus 8 dp plus 1 dp, and at
  most the map's height. The "Return to vehicle" button is 48 dp tall (within 0.5) and lies inside the cluster's
  bounds.
  - **Predicted at base: fails.** The cluster is about 368 dp, squeezed to the window, and the return button about
    36 dp.
- **TR2, the pill sits beside the bar, inboard and bottom-aligned.**
  - At 90 (cluster on the left) the pill's left edge is at or right of the bar rows' right edge; at 270, its right edge
    is at or left of their left edge.
  - The pill's bottom equals the bar's bottom (the add row's bottom plus 4 dp), within 1 dp.
  - **Predicted at base: fails** (the pill is below the bar).
- **TR3, guard: the pill's controls are reachable.** With recording on, so the return button is enabled, five real
  touches spread across each of the record and return buttons' bounds each reach it (their callbacks count 5 each).
  - **Predicted at base: passes.** The squeezed return button still takes touches within its own bounds.
  - A guard, backed by a revert check that covers the pill with a touch-consuming layer.
- **TR4, the snap keeps the shape.** A long-press drag of 200 dp towards the far edge on the minimise handle snaps the
  cluster to the other side, and the pill is then inboard of the bar there.
  - **Predicted at base: fails** (the pill is below the bar).
- **TR5, the drag works on the new shape.** A long-press drag of 40 dp down on the minimise handle moves the cluster
  down by 40 dp (within 1). A touch on the minimise handle then minimises the cluster, and a touch on the restore handle
  brings it back.
  - **Predicted at base: fails on the move.** The squeezed cluster has about 8 dp of room.
- **T9, item 9.** The search bar's bounds and the cluster's do not intersect.
  - **Predicted at base: fails**: the cluster's top is at about 8 dp, the bar's bottom at about 45 dp.
  - **After the build, predicted to pass under Robolectric,** with the cluster's top at about 60 dp.
  - **On the S22 the margin is about 0 px by my arithmetic:** a 354 dp map area puts the cluster's top at about
    210.7 px against the bar's drawn bottom at 211 px. So item 9 on the phone is device-only.
- **T5, item 5 wiring.** The map is handed `attributionEndInset` equal to the rail's measured width at 90, 0 at 270,
  and 0 at 90 in fullscreen (after a real touch on "Fullscreen").
  - **Predicted at base:** fails at 90 outside fullscreen (0 against about 80 dp). The 270 and fullscreen halves pass;
    they are guards.

### D. `LayoutFixesLandscapeHeldTest`, the same window, both rotations (items 7 and 1 and 2 in landscape)

These are the re-measurements `-99` asks for. The ones predicted to still fail after the build are stop evidence. They
stay on `layout-fixes-wip`, not in `journal-redesign`'s suite, and their figures go into this report.
- **T7.** J8's chip does not intersect the cluster.
  - **Predicted:** fails at 90 at base and **still after the build**. The chip spans about 53-101 dp in y from x 0,
    and the cluster about 60-324 dp from x 8, so the chip overlaps the cluster's first row across the bar's width. That
    is item 7's stop, with option E.
  - Passes at 270 (a guard).
- **T1.** Neither the collapsed nor the expanded legend intersects the cluster.
  - **Predicted:** passes at 90 (a guard). Fails at 270 at base and **still after the build**. The collapsed legend
    starts at about 316 dp against the cluster's bottom at about 324 dp. There is no room to lift it: the dropdown's
    top at about 61 dp is below the centred cluster's top at 60 dp. That is item 1's and item 2 (landscape)'s stop.

### E. `SearchDropdownKeyboardTest`, a component test of `SearchDropdown` in a Box whose height the test sets (item 3)

- **T3a.** The dropdown opens with the one-shot request at a height where everything fits (800 dp), then the height
  drops to 360 dp, as a rising keyboard would shrink it. "Latitude", "Longitude" and "Search this location" are then
  displayed.
  - **Predicted at base: fails.** The one-shot ran while everything fitted and does not run again, so the fields are
    scrolled off the bottom.
- **T3b, guard.** After the pin, the user's real drag down brings "Set on map" into view. A later height change, 360
  to 420 and back to 360 dp, leaves "Set on map" displayed.
  - **Predicted at base: passes** (nothing re-scrolls at base).
  - Backed by a revert check that removes the drag disarm.
- **The IME cap itself cannot be seen by Robolectric**, since the IME inset is zero. It is device-only, and no test
  claims it.

### F. `LayoutFixesMapHelpersTest`, plain JVM (items 4 and 5)

- **T4u.** `cameraRestoreFor`:
  - on a new map (no previous camera mode) with a snapshot that was following, it restores its target, zoom, bearing,
    tilt and applied target with TRACKING;
  - with one not following, the same with NONE;
  - with no snapshot, or with a previous camera mode (a style swap on a live map), no restore.
  - **Predicted at base: the two restore cases fail** (null); the two no-restore cases pass.
- **T5u.** `attributionMarginsPx`: in a left-to-right layout the end inset goes to the right margin; in a right-to-left
  layout, to the left; the bottom inset is added to the bottom margin.
  - **Predicted at base: fails** (the defaults come back unchanged).

### Revert checks planned (one per built item with a test)

Each check restores from a copy saved before editing, checks the build log for compile errors, and confirms the forward
change afterwards.

| check | revert | should fail |
|---|---|---|
| R2 | the floor at `:801` restored | T2 (expanded half) |
| R3a | the viewport re-pin removed (one-shot only) | T3a |
| R3b | the drag disarm removed | T3b |
| R4 | `CompactMapTab` hands the map a memory it remembers itself, not the hoisted one | T4 |
| R4u | `cameraRestoreFor` back to null | T4u |
| R5 | the scaffold's end inset back to 0 | T5 at 90 |
| R5u | `attributionMarginsPx` back to the defaults | T5u |
| R8 | the chip's clickable back inside its minimum-interactive box | T8 |
| RA | landscape back to the Column | TR1, TR2, TR4, TR5, T9 |
| RA-g | a touch-consuming layer over the pill | TR3 |

### Device-only, by construction

- the IME cap and when the scroll lands (item 3);
- the attribution margins MapLibre applies (item 5);
- the camera MapLibre restores and the tracking mode (item 4);
- the caption against the reshaped cluster (item 6: the caption is inside `SightingsMap`, which no screen test
  composes);
- every figure that depends on the S22's status bar, nav bar and cut-out (items 1, 7 and 9 margins).

## Additions to the pre-registration, written after the first base run and before the second

The first tests-first run (35 tests from a cleared results directory, 0 compile errors, 6 fresh XML files) matched
every prediction but one. T8 failed at its guard: "the band must exist: chip [176.3, 77.0][208.0, 109.0], readout's
touch area [219.0, 30.0][225.3, 78.0]".
- **Why:** under Robolectric's default (legacy) graphics mode text measures almost no width. The readout's
  coordinates are 6.3 dp wide and J8's chip 32 dp, so the two do not overlap across the screen and no band exists.
- **The same run showed text-sized geometry away from the phone's.** The search bar measured 85 dp (`[0.0, 0.0][384.0,
  85.0]`), against my prediction of about 45 dp and the S22's 126.6 px (45 dp). The collapsed legend measured 33 by
  52 dp.
- By CLAUDE.md, a failure that does not match its prediction means the test is wrong, so these are corrected before
  anything is built.

**Corrections, before the re-run:**
1. **Native graphics.** T8, T9, T7 and T1 run under `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, as
   `AvailabilityScreenMapIconStackTest.kt:1077` already does. Their geometry depends on text size, and native text
   is the closer match to the phone.
   - T2, TR1 to TR5 and T5 stay in the default mode. Their geometry is icons, rows and the nav and rail, and T2's
     figures matched the prediction exactly: the cluster ending at 560.0 dp against the expanded legend's top at
     532.0 dp.
2. **T8's band redefined to the device's dead band.** On the S22 the five dead touches lay between the chip's
   expanded touch area and its pill's top: y 278 to 293 px, against the pill drawn from 294 px
   (`docs/audits/2026-09-28-j8-device-check-run-record.md:615`, `:623-626`). The chip's own layout is its pill (the
   first run measured it 32 dp tall), and its 48 dp is Compose's touch expansion, as is the readout's.
   - So the band is where the two expanded touch areas overlap, outside both controls' own bounds. Each control's
     touch area is its bounds extended to 48 dp tall about its centre.
   - The band runs across x from the larger of the two left edges to the smaller of the two right edges. Down the
     screen it runs from the larger of the chip's touch top and the readout's text bottom, to the smaller of the
     readout's touch bottom and the chip's own top.
   - The six points are unchanged in form: x at 1/6, 1/2 and 5/6, y 1 dp inside its top and its bottom, or its middle
     if it is under 3 dp tall.
   - **Predicted at base: all six reach neither control, and the map takes them.**
3. **Updated predictions after the build,** from native text sizes I have not yet measured. The search bar should be
   near the phone's 45 dp:
   - **T9** should pass, with the cluster's top at about 60 dp;
   - **T7** at 90 should still fail, because the chip starts about 53 dp down, above the cluster's top at 60 dp, and
     overlaps the bar's column;
   - **T1** at 270 should still fail, because the collapsed legend starts about 316 dp down, above the cluster's
     bottom at about 324 dp.
   - If native measurement gives a different search bar height, the numbers in the run decide, and I will say so.

The first run's figures, kept: TR1 cluster 368 dp against bar 264 dp; TR5 moved 8.0 dp of 40; T5 at 90 0.0 against
80.0; T4 `null`; T7 at 90 chip `[1.0, 93.0][48.0, 145.0]` against cluster `[8.0, 8.0][56.0, 376.0]`; T1 at 270 legend
`[782.0, 300.0][815.0, 352.0]` against cluster `[767.0, 8.0][815.0, 376.0]`; T9 bar bottom 85.0 against cluster top
8.0. T3b, TR3, T5 at 270, T7 at 270, T1 at 90 and T4u's two no-restore cases passed, as predicted. T5u's "no insets"
case also passed; I added it as a guard and did not pre-register it.

## The build (commit `d959fb9`, pushed to `layout-fixes-wip`)

Every citation below is at `0511272`, the build with the held test file moved out. Only `JournalEntriesChip.kt` has
changed since in the files cited: item 8 was backed out.
- **Item 2 (portrait).** Q4's legend bound lifts the cluster above centre (`AvailabilityCompactMapUi.kt:824-841`).
  - `legendLiftPx` is the legend's bound less the centred cluster's bottom, when the legend is the lowest edge and
    that difference is negative. The upward limit reaches as far as the lift needs, never past the dropdown's top.
  - The nav's floor, and every state without a legend, is unchanged.
  - Not gated to portrait. In short landscape at the S22's size the dropdown bound wins, so it changes nothing there:
    R2 below left all 14 landscape tests unchanged.
- **Option A, the reshape.** The cluster's contents are two local composables, `clusterBar` and `clusterPill`
  (`:1005-1056`).
  - A short landscape window lays them out in `ShortLandscapeClusterRow` (`:1057-1058`, the function at `:1449-1477`):
    a Row, bottom-aligned, gap `CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR`, with the pill on the bar's inboard side.
  - Portrait keeps the Column (`:1059-1066`).
  - Under Robolectric the cluster is now `[8, 60][112, 324]` at 90 and `[711, 60][815, 324]` at 270: 264 by 104 dp.
- **Item 3.**
  - The Maps tab's portrait dropdown cap subtracts the larger of the nav band and the IME's bottom inset
    (`AvailabilityCompactScaffold.kt:1184-1191`, `:1237`).
  - `SearchDropdown` keeps the end in view on each viewport size change after the one-shot scroll, until a
    `DragInteraction.Start` (`AvailabilitySearchUi.kt:439-457`, armed at `:532`).
- **Item 4.**
  - `MapCameraMemory` and `MapCameraSnapshot` (`ui/map/MapCameraMemory.kt`), remembered in `AvailabilityScreen`
    (`:882`) and threaded through `CompactMainScaffold` (`:192`, `:833`) and `CompactMapTab` (`:232`) to
    `MapRenderMode.cameraMemory` (`MapSlot.kt:218`, forwarded at `:510`).
  - `SightingsMap` saves the camera on each idle once its first style has loaded (`:488-504`).
  - On a new map's first style load it restores the camera with `cameraRestoreFor` (`:625-626`, the function at
    `:1173-1184`), sets `lastAppliedCameraTarget` (`:654`), and activates with the saved mode (`:664`).
- **Item 5.**
  - `SightingsMap` reads MapLibre's default attribution margins once (`:548-551`) and applies
    `attributionMarginsPx` of `bottomInset` and `attributionEndInset` (`:795-811`, the function at `:1201-1208`).
  - The scaffold sets the end inset to the rail's measured width when the port edge is the "i"'s end side outside
    fullscreen, and 0 otherwise, animated (`AvailabilityCompactScaffold.kt:596-611`, handed over at `:849`).
- **Item 8, backed out at `484ea22`** (see the next section). At `d959fb9`, J8's chip had an outer `Box` with the click
  outside `minimumInteractiveComponentSize()`, and inside it the same pill `Surface` with its ripple clipped to the pill
  (`JournalEntriesChip.kt:124-156` at `d959fb9`, the click at `:141-142`).
- **No new copy.** No user-visible string was added or changed.

## T8's band, changed after the build (recorded here as a change, not hidden)

- The build moves J8's chip's click outside the minimum-interactive box. The chip's tagged bounds are those of its
  merged clickable node, so they became the whole 48 dp box, and T8's band (measured from those bounds) would have
  vanished.
- So T8 now takes the pill from the chip's own text plus the pill's padding (`Spacing.md` across, `Spacing.sm` down),
  which the fix does not move. It also has a guard that this pill lies inside the chip's tagged bounds.
- R8 below re-ran T8 against the reverted chip: the band came out `[241.3, 69.0][270.0, 77.0]`, byte for byte the
  second base run's, and all six touches failed as there. The change did not move what T8 measures.

## Tests after the build

Six classes, 35 tests, from a cleared results directory, 0 compile errors, 6 fresh XML files: **33 pass, 2 fail**.
The two failures are the held re-measurements pre-registered to fail:
- **T7 at 90:** J8's chip `[0.0, 53.0][158.0, 101.0]` against the cluster `[8.0, 60.0][112.0, 324.0]`.
- **T1 at 270:** the collapsed legend `[739.0, 316.0][815.0, 352.0]` against the cluster `[711.0, 60.0][815.0, 324.0]`.

## Revert checks

Runner: `/tmp/layout-fixes-probe/revert.py`. Each check:
- saves the forward file;
- applies one replacement, asserted to occur once;
- runs the named classes from a cleared results directory;
- refuses the results if the build log has any `e: ` line;
- restores the file from the saved copy (never git) and checks it byte for byte;
- lists every failure from the XML.

Two first attempts were **refused for compile errors** and redone with a different edit:
- R2's `if (false && legendLiftPx != null)` lost a smart cast (2 errors);
- R4u's `if (true) return null` did the same (6 errors).

That is the runner rule working.

| check | revert | classes run | failures (all, from the XML) | forward file restored |
|---|---|---|---|---|
| R2 | `legendLiftPx` never set (`?.takeIf { false }`) | legend portrait, short landscape | T2 only: "expanded: the cluster [328.0, 180.0][376.0, 560.0] is clear of the legend [192.0, 532.0][376.0, 628.0]", the base figures | yes |
| R3a | the keep-in-view never armed | dropdown | T3a only: "'Latitude' ... is not displayed" | yes |
| R3b | no disarm on the user's drag | dropdown | T3b only: "'Set on map' ... is not displayed", at the assertion after the viewport change (line 108) | yes |
| R4 | the scaffold no longer passes `mapCameraMemory`, so `CompactMapTab` remembers its own | portrait | T4 only: expected the snapshot | yes |
| R4u | `cameraRestoreFor` returns null for any snapshot | helpers | the two restore cases of T4u only | yes |
| R5 | the end inset's target 0 dp | short landscape | T5 at 90 only: "expected:<80.0> but was:<0.0>" | yes |
| R5u | `attributionMarginsPx` returns the defaults | helpers | the two inset cases of T5u only | yes |
| R8 | the chip's click back inside its minimum box | portrait | T8 1 to 6: each "chip's list open: false, readout switched: false, map taps +1" | yes |
| RA | landscape back to the Column (`if (false)`) | short landscape | TR1, TR2, TR4, TR5 and T9, at both rotations, with the base messages (cluster 368 dp, moved 8.0 of 40, and so on) | yes |
| RA-g | a touch-consuming layer over the pill | short landscape | TR3 at both rotations only: "expected:<5> but was:<0>" | yes |

After the last check, `git status` was clean and each forward line was present (`grep`).

## The re-measured items after the reshape (`-99`)

The pre-registered bounds tests, under Robolectric (native text), and the S22's figures by my arithmetic from the
reshaped cluster. The S22 figures take the 1080 px landscape window, the 84 px status bar (a 996 px map area) and the
J8 and Part 1 run records' bar and chip positions. They are not measured.

- **Item 9, the search bar: clear.**
  - Robolectric: the bar ends at 45.0 dp and the cluster starts at 60.0 dp (T9 passes at both rotations; RA fails it).
  - S22 by arithmetic: the cluster's top at 210.75 px against the bar's bottom at 210.6 px. They do not intersect,
    but there is no gap. **Device item.**
- **Item 6, the caption at 90: clear at the default position, by arithmetic only.**
  - The cluster now ends at about 953 px, and the caption (one `labelSmall` line and 2 dp of padding each way, about
    56 px) sits at about 1024 to 1080 px.
  - The caption is not composed under Robolectric, so this is **device-only**.
  - A cluster dragged to its lowest point (126.75 px of room) reaches the bottom and the caption again.
- **Item 7, J8's chip at 90: still collides, on width. Stop, with option E reported.**
  - Robolectric: chip `[0.0, 53.0][158.0, 101.0]` against cluster `[8.0, 60.0][112.0, 324.0]`, overlapping over x 8
    to 112 and y 60 to 101. That is the Fullscreen row, the top of the reset row, and the container fill beside them
    above the pill.
  - S22 by arithmetic: chip box x 75 to 536 and y 233 to 368 px, against the cluster at x 97.5 to 390 and from
    210.75 px. The whole cluster width overlaps, 292.5 px.
  - There is no room below: the chip's box ends at 368 px, so clearing it needs about 180 px downward, and the clamp
    allows 126.75 px.
  - **Option E** (the chip aligned to the bar's inboard end when the cluster is on the bar's side) would put the chip at
    x 674 to 1135 px in the bar's 75 to 1135 px, clear of the cluster by 284 px, by the same arithmetic. I have not
    chosen it.
  - At 270 the chip is clear (T7 passes).
- **Items 1 and 2 in landscape, the legend at 270: still collide, for lack of room. Stop.**
  - Robolectric: the collapsed legend `[739.0, 316.0][815.0, 352.0]` against the cluster `[711.0, 60.0][815.0, 324.0]`,
    overlapping by 8.0 dp. Expanded (96 dp) it is worse.
  - S22 by arithmetic: the collapsed legend's layout from about 889 px against the cluster's bottom at 953 px, an
    overlap of about 64 px. Expanded (270 px tall, from about 720 px), about 233 px.
  - Q4's clamp cannot lift the cluster. Its top (210.75 px) is already above the dropdown's top (about 255.6 px), and
    the clamp never pushes a cluster below that bound's reach, so the upward room is 0. Lifting it 87 px or more would
    take it into the search bar.
  - At 90 the legend and cluster are on opposite sides and clear (T1 passes). A user who drags the cluster to the rail
    side at 90 meets the same collision.
  - These tests live in `LayoutFixesLandscapeHeldTest.kt`, on `layout-fixes-wip` at `d959fb9`, and are not in
    `journal-redesign`'s suite (`0511272` removes the file).

## The first full suite, and item 8 stopped

**The first full suite, at `0511272`** (all six items built):
- cleared results directory; start 00:21:11Z, after waiting for the decorations coder's Gradle build to finish;
- 0 compile errors; 311 XML files, 0 stale.
- **311 / 2521 / 2 / 0 / 24.** The planner's base is 306 / 2490 / 0 / 0 / 24, plus my 5 files and 31 tests.

**The two failures are J8's own tests, broken by my item 8 change:**
1. **`MapChipsOverMapTest`, "the journal entries chip has no shadow under its 0_8 fill":**
   - Failed at `MapChipsOverMapTest.kt:85`: "read on the chip's own node, the one with the Surface's fill:
     [testTag, semantics, clickable, minimumInteractiveComponentSize]".
   - The test reads the modifiers of the node carrying the chip's tag, which it documents as "also carries the
     Surface's background" (`:40-41`). My change put the tag on a new outer box, off the Surface.
2. **`JournalEntriesOnMapShortLandscapeTest`, "touches all around the chip reach the map, and a touch on the chip does
   not":**
   - Failed at its own guard, `JournalEntriesOnMapScreenTest.kt:491`: "(-6.0.dp, 77.0.dp) is on the map".
   - Under Robolectric's default graphics the pill is 32 dp wide and sat centred in the 48 dp box, from x 8. My change
     made the tagged bounds the box, from x 0, so "6 dp left of the chip" fell off the screen.
   - The portrait variant passed only because its "around the chip" points moved out with the tagged bounds.

**Why this is a stop, not a third fix:**
- J8's chip is documented and tested to take no touch outside the pill: "nothing here takes a touch outside the pill
  itself (CLAUDE.md, the Surface pitfall)" (`ui/map/JournalEntriesChip.kt:96`, at base and again now). The J8
  test touches 6 dp left of, right of, below and at the two lower corners of the pill, and asserts the map takes each.
- Item 8 needs part of that same margin, the strip above the pill where the readout's margin overlaps it, to be a
  control's.
- **Keeping the tag on the Surface and making the rest of the box the chip's** would pass `MapChipsOverMapTest`. It
  would still fail J8's test by my reading of the geometry: the point 6 dp below the pill lies inside the 48 dp box.
  That is reasoning, not a run.
- **Giving the readout the band** needs a real 48 dp touch box on the readout. As a layout change, that grows the strip
  over the chip, which `-98` excludes ("without moving either control"). Compose's own expansion is what loses to the
  map today.
- So every fix I can see either changes J8's tested design (the margin around the pill is the map's) or changes J8's
  test. Both are closed to me. The failures are not held, and "a non-held failure" is an abort condition.
- `-98` made each item "built or held on its own", so I stop item 8 alone. The chip went back to its base content at
  `484ea22`, and T8 moved to `LayoutFixesDeadBandHeldTest.kt` on `layout-fixes-wip`.
- In a targeted run after the backout, both J8 tests pass again, and all six T8 touches fail with the base message
  ("chip's list open: false, readout switched: false, map taps +1", band `[241.3, 69.0][270.0, 77.0]`). I did not touch
  either J8 test.

**Options for item 8** (not chosen):
- **A. The chip takes its whole 48 dp box** (built at `d959fb9`, R8-checked).
  - This needs a ruling that the chip's touch box is the chip's, which changes J8's "nothing outside the pill".
  - It also needs J8's two tests re-anchored: the "around the chip" points taken from the touch box, and the shadow
    check read on the Surface's node.
  - This matches Material's own convention: a chip's 48 dp target is the chip's.
- **B. Only the strip above the pill becomes the chip's** (an asymmetric target, from the pill's top up to the box's
  top).
  - J8's tested sides (left, right, below) stay the map's.
  - It covers the band exactly, but it is contrived: why only the top?
  - Not built, not tested.
- **C. The readout takes the band:** a real 48 dp touch box on the readout. That is a layout change that makes the
  strip taller, and is excluded by "without moving either control" unless ruled otherwise.
- **D. Leave it:** accept the 25 px band as the map's, as J8 designed. The device check found five touches there
  "did nothing" visible, because the map took them.

## The second full suite, at `2e7c72f` (the build without item 8, merged with `-100`)

- Merged `origin/journal-redesign` at `da82158`, which carries the decorations band (`-100`). It touches
  `ui/map/layers/MapLayers.kt` and `MapLayerRegistryTest.kt`, none of my files.
- Cleared results directory; start 00:28:59Z, with no other Gradle build running and 5124 MB available.
- 0 compile errors, BUILD SUCCESSFUL; 311 XML files, 0 stale.
- **311 / 2518 / 0 / 0 / 24.**
- **The reconciliation:** `-100`'s own suite was 306 / 2493 / 0 / 0 / 24 (its commit `0fd3944`). Mine adds 5 XML files
  (`LayoutFixesLegendPortraitTest`, `LayoutFixesPortraitTest`, `LayoutFixesShortLandscapeTest`,
  `SearchDropdownKeyboardTest`, `LayoutFixesMapHelpersTest`) and 25 tests, and all 25 passed:
  - T2 and T4, one each;
  - TR1 to TR5, T9 and T5, 14 in all;
  - T3a and T3b;
  - seven helper cases.

  306 + 5 = 311 and 2493 + 25 = 2518.
- The held tests (T7 and T1, 4 tests; T8, 6 tests) are not in this suite. They are on `layout-fixes-wip`.

## Device-only list, for the planner's next device check on the S22 (at 0, 90 and 270 unless stated)

1. **Item 2 (portrait, 0):**
   - with the forecast fields on and the legend expanded, the cluster's last row ends above the legend's top (Part 1
     measured a 122 px overlap);
   - collapsed, it is back at its place;
   - the cluster still stops above the nav without the legend.
2. **Item 3 (portrait, 0):** after one touch on the search bar, with the keyboard up, Latitude, Longitude and "Search
   this location" lie above the keyboard's inset frame (`dumpsys window`), without the user scrolling.
   - A drag in the dropdown then scrolls it freely.
   - Also recorded: whether the programmatic scroll lowers the keyboard (the flag below).
   - Short landscape, where the keyboard floats on the S22, as before.
3. **Item 4:**
   - Pan, zoom, rotate and tilt the Maps tab. Go to List (or Journal) and back: the camera is where it was left,
     including bearing and tilt, and not following.
   - With the puck followed (locate-me, untouched): the round trip keeps following at the zoom left, with no zoom-in
     to 16 and no jump to the region at zoom 12.
   - A new search made elsewhere still moves the camera to it.
   - After a restart the map opens as before (session only).
4. **Item 5:**
   - portrait, outside fullscreen: the "i" is above the app's nav and outside the system bar's frame, and a touch opens
     MapLibre's attribution;
   - in fullscreen, at the true bottom edge, with the caption;
   - at 90 inboard of the rail, and in fullscreen at the edge;
   - at 270, where it was, in the cut-out band, clear of the cluster and the legend.
5. **Item 6 (90):** the caption's pixels are clear of the reshaped cluster at its default position.
6. **Item 8: not built** (stopped, above). If a ruling brings it back, five or more real touches across the old band
   (y 273 to 294 px in J8's record) each reach exactly one control.
7. **Item 9 (90 and 270):** the reshaped cluster's top against the search bar's bottom. By arithmetic they touch with
   no gap, so pixels should be read.
8. **The reshape (90 and 270), with thumb reach judged by the owner:**
   - the pill beside the bar, inboard and bottom-aligned;
   - the container's fill above the pill, 56 by 156 dp, which takes touches;
   - the drag, the snap and both handles on the new shape;
   - record and return reachable, including while recording.
9. **Held, for a ruling, not fixed:** item 7 at 90 (option E), items 1 and 2 in landscape at 270, and item 8.

## Not tested

- Everything under "Device-only", on a device: no phone or emulator was used.
- MapLibre applying the camera, the tracking mode and the attribution margins; only the pure decisions and the wiring
  are tested.
- The IME cap: Robolectric reports no keyboard.
- The caption against the cluster: `SightingsMap` is not composed under Robolectric.
- A right-to-left layout on a real screen (only `attributionMarginsPx`'s arithmetic is tested for it).
- The wide layout and the Cartography entry maps, which pass no camera memory and no end inset (by reading, unchanged).

## Where each item stands (at the push to `journal-redesign`)

| item | status | Robolectric evidence | device-only |
|---|---|---|---|
| 1 legend over record at 270 | **stopped**: no room after the reshape (overlap 8 dp under Robolectric, about 64 px on the S22 by arithmetic) | T1 at 270 fails (held, wip) | re-measure after a ruling |
| 2 portrait | **built** | T2, R2 | the S22 overlap |
| 2 landscape | **stopped**, with item 1 | T1 (held, wip) | as item 1 |
| 3 dropdown and keyboard | **built** | T3a, T3b, R3a, R3b (a viewport shrunk by the test) | the IME cap and the timing |
| 4 camera round trip | **built** | T4, T4u, R4, R4u (wiring and the decision) | MapLibre's restore and the tracking mode |
| 5 the "i" | **built** (portrait and landscape; fullscreen at the edge) | T5, T5u, R5, R5u (wiring and arithmetic) | all placement |
| 6 caption at 90 | **cleared by the reshape at the default position, by arithmetic**; no code of its own | none possible | yes |
| 7 chip over reset at 90 | **stopped**: still overlaps on width; option E reported | T7 at 90 fails (held, wip) | after a ruling |
| 8 dead band | **stopped**: the fix breaks J8's own tests (above) | T8 fails at base, passes under option A (held, wip) | after a ruling |
| 9 cluster over search bar | **cleared by the reshape** | T9, RA | the S22 margin is about 0 px by arithmetic |
| reshape (option A) | **built** | TR1 to TR5, RA, RA-g | thumb reach, the fill above the pill |

## Decisions I made (this Resumed section)

1. **The reshape's details** (`-99` left them to me): the pill on the bar's inboard side, bottom-aligned, with the
   portrait gap. These are the planner's lean, taken as given. The container's fill stays one surface, so the
   56 by 156 dp space above the pill takes touches; the owner can judge it on the phone.
2. **Item 5 at 270:** the "i" stays in the cut-out band on the punch-hole side, because padding it inboard would put it
   under the cluster. `-98` said "portrait and landscape" without naming a side.
3. **Item 5's end side follows the layout direction** (right in left-to-right). A right-to-left layout is untested on
   screen.
4. **Item 3:** the IME cap only on the Maps tab's portrait dropdown. The other tabs already shrink with the IME, and
   the landscape dropdown is on a floating keyboard on the S22. The disarm is on the user's drag, not on any touch.
5. **Item 4:**
   - The snapshot also carries `lastAppliedCameraTarget`, so a new search after a round trip still moves the camera.
   - The restore runs only on a new `MapView`'s first style.
   - The snapshot is written only after that first style has loaded.
   - The memory is not Compose state.
6. **Item 2's lift is not gated to portrait.** In short landscape at the S22's size the dropdown bound wins, so it
   changes nothing there. R2 left all 14 landscape tests unchanged, which supports that.
7. **Item 8's first fix chose the chip, not the readout,** before I found J8's tests pinning the chip's margin. The
   choice of control was mine. The backout undoes it.
8. **Tests-first corrections after the first base run:** native graphics for the text-sized tests, and T8's band
   redefined to the device's dead band. Both were committed and pushed before the second base run (`86d073e`).
9. **T8's band reference changed after the build** (from the tagged bounds to the chip's text), since the fix moved the
   tagged bounds. R8 showed T8 still measures the same band.
10. **Two revert edits were redone** after the runner refused them for compile errors (R2, R4u).
11. **The held tests are kept out of `journal-redesign`'s suite and on `layout-fixes-wip`** (`d959fb9` for T7 and T1,
    `484ea22` for T8), following the J8 follow-ups' handling of failing tests-first commits.
12. **I backed out item 8 rather than stopping the whole dispatch,** under `-98`'s "each item is built or held on its
    own". The other items' fixes do not depend on it.
13. **`T5u with no insets the defaults stand`** was added as a guard without pre-registration. It was said so in the
    additions.

## Flags outside scope (this Resumed section)

1. **J8's tests pin the pill's margin as the map's** (`JournalEntriesOnMapScreenTest.kt:469-504`), which is the
   mechanism behind the dead band. Any item 8 ruling meets them.
2. **The programmatic scroll does not lower the keyboard** (flag 5 of the stop report). This build keeps the fields in
   view anyway. The comment at `AvailabilitySearchUi.kt:431-433`, which says it does, is left as it was.
3. **With the reshape, the space above the pill is container fill,** 56 by 156 dp, at the 0.8 chrome alpha, and takes
   touches (the portrait gap already does, as its doc comment says at `AvailabilityCompactMapUi.kt:1441-1447`).
4. **Item 9 on the S22 has about 0 px of margin** by arithmetic. A status bar or font scale a few pixels different
   would reopen it.
5. **The decorations band (`-100`) landed while I worked** and was merged at `2e7c72f`. It touches `MapLayers.kt` and
   its registry test, none of my files.
6. **Robolectric's default graphics mode measures text at near-zero width.** A test whose geometry depends on text
   (the search bar's height, chip widths) needs `@GraphicsMode(NATIVE)`. The first base run showed it: the search bar
   85 dp against 45.

---

# Resumed again (planner message `2026-09-28-109`)

## Who and what I ran on

- Model: this session is configured for `claude-sonnet-5-5`, the model ID in my system prompt. I did not verify the serving model
  beyond that line, so the serving model may differ.
- Worktree `/home/zynergy-labs/Zynergy/forager-wt/layout-fixes`, branch `layout-fixes`. It was clean at `08e9f13`; I pulled
  `origin/journal-redesign` with `--no-rebase` (fast-forward) to `9b59a4d`. The pull brought only planner-side files
  (`RECORD.md`, `prompts/`, `docs/`), no source.
- `CLAUDE.md` was read at the base. The coder's rules in the launch prompt and `CLAUDE.md` do not conflict with `-109`.

## The dispatch texts read, in order

`prompts/preserved/2026-09-28-78.md`, `-88.md`, `-98.md`, `-99.md` and `-109.md` (the last at `c2c9e6b`, first 16 hex of its sha256
`c0788bd79fae188e`), and `RECORD.md` entries `2026-09-28-108` and `-110`. `-78`, `-88`, `-98` and `-99` are quoted verbatim
above in this report. **`-109` verbatim, below its "verbatim message follows" line:**

> Planner message `2026-09-28-109`, part of dispatch `2026-09-28-78`, as amended by `-88`, `-98` and `-99`. Quote it verbatim in your report. It rules on your held items 1, 2 (landscape), 7 and 8.
> 
> **The owner's answers, verbatim: "1 A / 2 A / 3 D".** As the planner put them:
> - "1. The chip over the reset button at 90°. A. Move the chip to the other end of the search bar, away from the icon bar."
> - "2. The legend over the icon bar at 270°. A. At 270°, put the legend just inside the icon bar, beside it towards the centre, instead of tucked into the corner."
> - "3. The strip between the chip and the coordinates. D. Leave it as map."
> 
> **Item 7: build it (your option E, extended by the planner).**
> - In a short landscape window, **the chip row** (the taxon chip and J8's chip together) aligns to the end of the search bar **away from the cluster's current side**, and follows the cluster when it is dragged or snapped to the other side.
> - It is the row, not J8's chip alone, because J8's chip was ruled "Top, by the species chip". That is the planner's reading of the owner's "the chip", stated to the owner.
> - Portrait is unchanged.
> - **Tests first** at `w823dp-h384dp-land`, 90 and 270, with the cluster on each side: the chip row does not intersect the cluster. Real touches across the reset button reach it.
> 
> **Items 1 and 2 (landscape): build them.**
> - When the cluster is on the legend's side (at 270 by default, or wherever it is snapped), the legend sits **just inboard of the cluster**: beside it towards the centre, bottom-aligned, with the portrait gap, collapsed or expanded. Otherwise it stays in its corner as today.
> - Q4's rule (the cluster moves up for the expanded legend) still applies where there is room.
> - **Tests first:** the legend, collapsed and expanded, does not intersect the cluster at 90 and 270 with the cluster on each side. Real touches across the legend's chip reach it.
> 
> **Item 8: no change.** The owner ruled the band stays map. J8's chip design (touches only on its pill, `JournalEntriesChip.kt:96`) and its tests stand. Record the ruling in your report. Your held T8 tests stay off the suite, on `layout-fixes-wip`.
> 
> **Everything else stands:** revert checks per item, the full suite, the device-only list (add the chip row's and the legend's placement at 90 and 270), no new copy, and a "Resumed" section.
> 
> **Sharing the machine:** the offline-safety coder builds on this machine. Before each Gradle run, check that no other build is running and that 2.5 GB is available.

`RECORD.md` `-110`, the relaunch note, verbatim in its Reason: "the owner, verbatim: \"Switch coders to Sonnet 5.5\", then \"Stop whatever
coder that isn't Sonnet 5.5 and switch them\". The planner stopped the -78 coder. Its worktree forager-wt/layout-fixes was clean at
08e9f13, with -109 not yet started." Its Changes: "A fresh coder on Sonnet 5.5 continues from 08e9f13 and builds -109 (the chip row
away from the cluster; the legend inboard at 270; item 8 unchanged), then the stage's finish line."

## Premises checked at the base (`9b59a4d`)

- **The chip row's placement** is `AvailabilityCompactMapUi.kt:1131-1160`: in short landscape a `FlowRow` aligned to `punchHoleEdge`,
  `width(landscapeSearchWidth)`, `wrapContentWidth(Alignment.Start)`. So today the row is always at the bar's start. Confirmed.
- **The legend's placement** is `AvailabilityCompactMapUi.kt:1210-1225`: `Alignment.BottomEnd`, `padding(controlsPadding)`,
  `padding(end = Spacing.sm, bottom = renderMode.bottomInset + LEGEND_ATTRIBUTION_CLEARANCE)`. Confirmed. The cluster is on the legend's side
  exactly when `!isMapIconBarOnLeftSide` (the same test as `legendBoundPx`, `:799`).
- **Q4's clamp** (`:794-841`) applies its legend bound only when `!isMapIconBarOnLeftSide`. Not changed by this work (see Decisions).
- **An existing test asserts the old chip placement:** `AvailabilityScreenLandscapeB2Test.kt:271-283` (S3 at ROTATION_90) asserts
  "the chip starts at the bar's start" (`:281`). At 90 the cluster's default is the left, so `-109` moves the chip row to the bar's right end
  and that assertion must change. That is the ruling overriding an older assertion, not a silenced test; I will change only that one assertion
  and record it.
- **`-109` says the held T8 tests "stay off the suite, on `layout-fixes-wip`".** `origin/layout-fixes-wip` is at `08e9f13`, the same commit as
  `layout-fixes`, and neither has T8 at its tip. T8 is in `LayoutFixesPortraitTest.kt` at `d959fb9`, which `origin/layout-fixes-wip` contains
  in its history (`git grep -c T8 d959fb9 -- .../LayoutFixesPortraitTest.kt` = 6). So the premise holds in history, not at the branch tip. Not a blocker.
- **No caller check needed for the changed paths:** both are inline in `CompactMapTab`, reached from `AvailabilityScreen` (the tests drive the real screen).

## The design I will build (stated before building)

**Item 7.** In the landscape branch only (`punchHoleEdge != null && landscapeSearchWidth != null`), `wrapContentWidth(...)` takes an *absolute*
alignment: `AbsoluteAlignment.Right` when the cluster is on the left (`isMapIconBarOnLeftSide`), `AbsoluteAlignment.Left` when it is on the right.
The cluster's side is an absolute left/right, so the row's alignment is absolute too (the existing `TopStart`/`TopEnd` on `punchHoleEdge` is
unchanged). It follows the cluster because `isMapIconBarOnLeftSide` is state. Portrait's branch is not touched.

**Items 1 and 2 (landscape).** In short landscape with the cluster on the legend's side, the legend's end padding grows from `Spacing.sm` by
`MAP_ICON_BAR_EDGE_INSET` + the cluster container's width + `Spacing.sm` (the portrait gap between the legend and the cluster,
`legendClusterGapPx`, `:798`). The width is the cluster container's measured width, read in the same `onGloballyPositioned` that measures its
height (`:1049-1052`), kept as the last measured value so a minimised cluster leaves the legend where it was. Vertical placement is unchanged
(bottom-aligned). Otherwise the legend is exactly as today.

**Item 8.** No code change.

## Pre-registration: tests, pass conditions and predictions

All Robolectric, `w823dp-h384dp-land`, `@GraphicsMode(NATIVE)` (text measured, as Decision 6 above), rotation pinned with
`ShadowDisplay.setRotation` and asserted seen by the screen. Cluster moved by the real long-press drag of its minimise handle. Every touch
is a coordinate touch. New file
`app/src/test/java/com/zynergylabs/forager/app/ui/availability/LayoutFixesLandscapeRulingsTest.kt`; `LayoutFixesTestSupport.kt` gains a
`content` field on `LayoutFixesMapSlot` (test support only).

### `LayoutFixesChipRowLandscapeTest` (item 7): the real screen with the taxon chip (via "View on Map" from the List tab) and J8's chip

Each test asserts, in this order: (a) the chip row (union of the two chips' bounds) does not intersect the cluster; (b) the row is at the bar's
end away from the cluster (within 1 dp) and inside the bar's width; (c) five real touches across the reset button's bounds all reach it
(`resetOrientationRequestId` up by 5 on the map's overlay content).

| test | predicted at base |
|---|---|
| T7 at 90, cluster left (default) | **fails at (a)**: the row `[0, 53][158, 101]` against the cluster `[8, 60][112, 324]`, the figures the earlier report recorded |
| T7 at 90, cluster snapped right | passes (a guard: the row is already at the bar's left end, away from the right) |
| T7 at 270, cluster right (default) | passes (a guard: the row is at the bar's left end, away from the right) |
| T7 at 270, cluster snapped left | **fails at (b)**: the row is at the bar's left end, expected at its right end |
| T7 at 90, the row moves when the cluster is dragged across | **fails**: `chipRow().left` does not decrease |

### `LayoutFixesLegendLandscapeTest` (items 1 and 2 landscape): the real screen with the legend showing (as the earlier held test)

For each rotation, with the cluster on the far side (the legend's corner position is read there) and then on the legend's side, with the legend
collapsed and expanded: the legend does not intersect the cluster; its bottom equals the corner's bottom (within 1 dp); on the legend's side its
right edge is 8 dp left of the cluster's left edge (within 1 dp); four real touches at different points of the chip's bounds each flip it.

| test | predicted at base |
|---|---|
| T1 at 90 | **fails** in the second phase (cluster snapped right), collapsed: the legend overlaps the cluster; the first phase (cluster left) passes |
| T1 at 270 | **fails** in the first phase (cluster on its default right), collapsed: the figures the earlier report recorded, legend `[739, 316][815, 352]` against the cluster `[711, 60][815, 324]` |

Any test predicted to fail that passes at base is a stop.

### Revert checks planned

Each restores from a copy saved before editing (not git), checks the build log for compile errors before reading results, and confirms the
forward change afterwards.

| check | revert | should fail |
|---|---|---|
| R7 | the chip row's `wrapContentWidth` back to `Alignment.Start` | the T7 tests, at (a) at 90-left and (b) at 270-snapped, with a message naming the bar's end |
| R1 | the legend's end padding back to `Spacing.sm` | the T1 tests with the legend beside the cluster |

### Device-only, by construction

The chip row's and the legend's placement at 90 and 270 on the S22 (real insets, cut-out, the rail's measured width); where the legend's left
end reaches relative to the search dropdown; and thumb reach.

## What happened (the `-109` build)

### Base run of the new tests (before any source edit), `1ba17a8`, 0 compile errors, fresh XML

- **Legend (`LayoutFixesLegendLandscapeTest`), both fail as predicted, for the stated reason:**
  - 270: the collapsed legend `[739.0, 316.0][815.0, 352.0]` against the cluster `[711.0, 60.0][815.0, 324.0]`;
  - 90 (second phase, cluster snapped right): the legend `[659.0, 316.0][735.0, 352.0]` against the cluster `[631.0, 60.0][735.0, 324.0]`.
- **Chip row (`LayoutFixesChipRowLandscapeTest`), 4 of 5 fail, and one prediction was wrong (a finding, below):**
  - 90 default: the row `[0, 53][382, 93]` against the cluster `[8, 60][112, 324]`;
  - **270 default, predicted to pass, fails:** the row `[439, 53][821, 93]` against the cluster `[711, 60][815, 324]`;
  - 270 snapped left: the row ends at 821 against the bar's right end 823 (expected 823);
  - 90 drag-across: the row's left stays at 0;
  - 90 snapped right passes (the guard).

### Item 7 stopped: the two chips together are as wide as the search bar

**Finding.** With the taxon chip and J8's chip both showing, the chip row is 382 dp wide in a 384 dp search bar (`[0, 53][382, 93]` in the bar `[439, 0][823, 45]`'s width at 270; the same width at 90). Aligning a 382 dp row to either end of a 384 dp bar puts it over the 104 dp cluster whichever end it takes, because the cluster's column is 264 dp tall from y 60 and the row is at y 53 to 93. So `-109`'s item 7 ("the chip row aligns to the end of the search bar away from the cluster ... the chip row does not intersect the cluster") cannot hold by alignment alone in this window with both chips present. The earlier report's 158 dp figure (`[0, 53][158, 101]`) was J8's chip alone; I predicted from that, and the prediction was wrong.

The label I used is "artist's bracket (12)" (as the B2 tests do). Label length varies in the real app, so the row's width varies; I have not measured any other label.

**Options, not chosen (each is a design decision):**
- **A. Alignment plus a width cap.** Align the row to the away end and cap its width at the bar's width minus the cluster's width and a gap (about 272 dp here), so the `FlowRow` wraps the second chip onto a second line. Both chips then fit beside the cluster; the row is two lines tall.
- **B. Alignment only** (what `-109` literally says). It clears the cluster only when the chips together are narrower than about 272 dp (J8's chip alone, 158 dp, qualifies). With both chips it does not.
- **C. The chips leave the bar's column in landscape:** e.g. the row moves below the cluster's top row region or to the other side entirely. That moves chrome to a place no ruling gave.

Nothing for item 7 is in `journal-redesign`. The tests are `LayoutFixesChipRowLandscapeTest` in `LayoutFixesChipRowHeldTest.kt` on `layout-fixes-wip` at `749630d`.

### Items 1 and 2 (landscape) built

`AvailabilityCompactMapUi.kt`: the cluster container's width is measured with its height (`mapIconClusterWidthPx`, kept when minimised). In short landscape with the cluster on the legend's side (`landscapeCluster && !isMapIconBarOnLeftSide`, the same test as the Q4 bound), the legend's end padding is `MAP_ICON_BAR_EDGE_INSET` + the measured width + `Spacing.sm` instead of `Spacing.sm`. Vertical placement and portrait are unchanged.

- **Test:** `LayoutFixesLegendLandscapeTest`, 2 tests (90 and 270), each walking both cluster sides with the legend collapsed and expanded: no intersection; bottom equals the corner's bottom (1 dp); on the legend's side the right edge is 8 dp left of the cluster's left edge (1 dp); four real touches across the chip's bounds each flip it. Passes after the build.
- **Revert check R1:** the end padding back to `Spacing.sm`, restored from a copy saved before editing (`/tmp/lf/orig/`, not git). Build log 0 compile errors; fresh XML; both T1 tests fail with the base run's own messages (270: legend `[739.0, 316.0][815.0, 352.0]` against cluster `[711.0, 60.0][815.0, 324.0]`, which only the padding revert could produce). The forward change was confirmed present afterwards (`legendEndPadding` in the file; diff 16 insertions, 1 deletion).
- **Guards run alongside:** `LayoutFixesShortLandscapeTest` and `AvailabilityScreenMapLayersShortLandscapeTest` passed with the build (18 tests in the three classes, 0 failures).

### Item 8, the ruling recorded

The owner, verbatim, in `-109`: "3. The strip between the chip and the coordinates. D. Leave it as map." No code change. J8's chip design (touches only on its pill, `JournalEntriesChip.kt:96`) and its tests stand. My T8 tests remain in history at `d959fb9` (reachable from `origin/layout-fixes-wip`), off the suite.

### The full suite

At the build, from a cleared results directory, `LC_ALL=C.UTF-8`, `./gradlew --offline :app:testDebugUnitTest`, 0 compile errors, 312 files all newer than the run's start: **312 / 2520 / 0 / 0 / 24** (files / tests / failures / errors / skipped), read from the JUnit XML. The planner's figure at `08e9f13` was 311 / 2518 / 0 / 0 / 24; the difference is exactly my one new class, 2 tests.

## Device-only list (`-109`), for the S22 at 90 and 270

- The legend's placement beside the cluster: its right edge 8 dp inboard of the cluster's, bottom-aligned, collapsed and expanded, with the cluster on each side. Depends on the real cut-out, rail width and nav inset.
- The legend expanded against the search dropdown (the legend is 96 dp tall; the map area is about 354 dp).
- The one-frame move of the legend when the screen first composes (the width is unmeasured until the first layout).
- Item 7, once ruled: the chip row's placement at 90 and 270 on the S22.

## Decisions I made (the `-109` build)

7. **"Just inboard" is measured from the cluster's container**, using its measured width, not a constant. Alternative rejected: a constant from the 104 dp figure, which would drift if the pill changes.
8. **A minimised cluster keeps the legend inboard** (the width is the last measured one). Alternative: the legend returns to the corner while the cluster is minimised. I kept it stable so the legend does not jump on minimise/restore; `-109` says the legend is inboard when "the cluster is on the legend's side", which a minimised cluster still is.
9. **Q4's clamp is untouched.** `-109` says it "still applies where there is room". With the legend beside the cluster the bound no longer serves a purpose in landscape: `legendBoundPx` (`:799`) still limits how far down the cluster can be dragged when it is on the legend's side. I did not remove it; see the flag.
10. **I held item 7 whole** (no alignment code shipped) rather than build alignment alone, because alignment alone does not meet the ruling's own test with both chips present.

## Flags outside scope (the `-109` build)

- **Q4's legend bound in landscape.** In short landscape, with the legend beside the cluster, the clamp at `AvailabilityCompactMapUi.kt:799-841` still limits the cluster's downward drag to the legend's top. That limit protected a legend that sat below the cluster; it now protects nothing. It predates this change (the cluster was already held near centre at 270 by the same bound). The planner may want to rule whether landscape drops it.
- **My prediction for the chip row at 270 was wrong** (above). The earlier report's "at 270 the chip is clear" was measured with J8's chip alone.
- **`RECORD.md` is not mine to write;** the planner records this.

---

# Resumed a third time (planner message `2026-09-29-04`, record `2026-09-28-117`)

## The message, verbatim

The cross-session message as delivered to me, then the file that governs it (`prompts/preserved/2026-09-29-04.md` at `e0baece`, first 16 hex of
its sha256 `6e2789617993fe8b`), below its "verbatim message follows" line. I compared them: the file's text is what follows.

> Planner message `2026-09-29-04` (record `2026-09-28-117`), part of dispatch `2026-09-28-78`. Quote it verbatim in your report. It answers your stop on item 7 and your flag on Q4's legend bound.
> 
> **The owner, verbatim: "1 A 2 A".** As the planner put them:
> - "1. The chip row in landscape. A. Line it up at the end away from the icon bar, and cap its width at the space left beside the bar, about 272 dp. When both chips are showing they wrap onto two lines instead of reaching under the bar."
> - "2. A leftover limit on dragging the icon bar. A. Drop that limit in landscape, and keep it in portrait."
> 
> **Item 7: build it.**
> - In short landscape the chip row (taxon chip and J8's chip) aligns to the search bar's end away from the cluster's current side.
> - Its width is capped at the bar's width minus the cluster's measured width and the gap, so the FlowRow wraps to two lines when both chips will not fit.
> - It follows the cluster when the cluster is dragged or snapped to the other side. Portrait is unchanged.
> - Use your held `LayoutFixesChipRowHeldTest`, extended, as tests first. It must fail at base for the stated reason.
> - Also test a long chip label, not only "artist's bracket (12)".
> - Real touches reach the reset button, and each chip.
> 
> **Q4's legend bound: drop it in short landscape, where the legend now sits beside the cluster.** Keep it in portrait. Tests first: in landscape the cluster can be dragged down past where the legend's bound stopped it, and in portrait the bound still holds.
> 
> **Before you build:** your last `git pull --no-rebase` brought in the offline-safety coder's `app/` changes, and you did not re-run the suite on the merged tree. The planner is re-running it now. Pull again with `--no-rebase` before starting.
> 
> **Everything else stands:** revert checks per item, the full suite from a cleared results directory on your final tree, the device-only list, and a new "Resumed" section. Before each Gradle run, check that no other build is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is available; the planner's suite may be running. Hand back as before, to the planner session ref `[9b334a]`.

The relay: "Planner continuation 2026-09-29-04 (record 2026-09-28-117) for dispatch 2026-09-28-78: build item 7 with a width cap, and drop Q4's
legend bound in short landscape. ... The owner, verbatim: "1 A 2 A". ..." (it points at the file above as the governing text).

## State at the start

- I pulled `origin/journal-redesign` with `--no-rebase`: fast-forward to `e0baece`, which contains my `7d17a5c` and the offline-safety `app/`
  changes. The suite I reported ran before that merge; I re-run it on the final tree below.
- `CLAUDE.md`, the dispatch files, and this message do not conflict.

## The design I will build (stated before building)

**Item 7.** In the landscape branch of the chip row's modifier (`AvailabilityCompactMapUi.kt`, the `punchHoleEdge != null && landscapeSearchWidth != null`
arm): `width(landscapeSearchWidth).wrapContentWidth(<absolute alignment>).widthIn(max = cap)`, with the alignment `AbsoluteAlignment.Right` when the cluster is
on the left and `AbsoluteAlignment.Left` when it is on the right, and `cap = landscapeSearchWidth - (MAP_ICON_BAR_EDGE_INSET + measured cluster width + Spacing.sm)`
(384 - 104 - 16 = 264 dp in the test window). The cap is applied whichever side the cluster is on, as the message states it. Portrait's arm is untouched.

**Q4.** `legendBoundPx` (`:799`) also requires `!landscapeCluster`, so in short landscape the legend adds no bound to the cluster's drag; portrait keeps it.

## Pre-registration: tests and predictions

- **`LayoutFixesChipRowLandscapeTest`** (held file, extended), `w823dp-h384dp-land`, `@GraphicsMode(NATIVE)`, rotation pinned:
  - each configuration asserts, in order: the row does not intersect the cluster; it keeps at least 7.5 dp from it; it is at most 264 dp wide
    (+0.5); it is at the bar's end away from the cluster; it lies in the bar's width; five real touches across the reset button all reach it;
  - configurations: 90 and 270 on the default side, 90 snapped right, 270 snapped left, and a long label ("Chicken of the woods, sulphur shelf, the bright orange bracket fungus of oak") at 90, 270 and 270 snapped left;
  - both chips wrap onto two lines at 90 (J8's chip below the taxon chip);
  - a real touch on J8's chip opens its list, and a real touch on the taxon chip's clear button clears the filter, at 90 and 270;
  - the row moves when the cluster is dragged across.
  - **Predicted at base: fail** on the cases where the row overlaps or is over-wide (all the default-side cases, the long-label cases, the wrap test, 270 snapped
    by the alignment); the snapped-right case at 90 fails on the width cap (382 against 264) though it clears the cluster; the touch-reach tests for the two
    chips pass at base (guards: J8's chip and the clear button are reachable today).
- **`LayoutFixesLegendLandscapeTest`, three `Q4` tests** (270 collapsed, 270 expanded, 90 with the cluster snapped right): a 30 dp drag down moves the cluster 30 dp
  (1 dp), its bottom ends below the legend's top, and the legend and cluster still do not intersect. **Predicted at base: all three fail** (the bound holds the cluster:
  the legend's top less 8 dp is above the cluster's bottom).
- **`LayoutFixesLegendPortraitTest`, one `Q4` test** (`w384dp-h740dp`): a 400 dp drag down stops the cluster at or above the legend's top. **Predicted at base: passes** (a
  guard for "keep it in portrait"), backed by a revert check that drops the bound in portrait as well.

### Revert checks planned

| check | revert | should fail |
|---|---|---|
| R7 | the chip row's modifier back to `width(bar).wrapContentWidth(Alignment.Start)` | the chip-row tests, with the cap or alignment message |
| R7c | the `widthIn(max = cap)` removed only | the width and wrap tests |
| RQ4 | the `!landscapeCluster` condition removed | the three landscape Q4 tests |
| RQ4p | the bound dropped in portrait too | the portrait Q4 guard |

## What happened (the `-117` build)

### Base run of the extended tests, before any source edit (`base2`; 0 compile errors; fresh XML)

20 tests: 8 pass, 12 fail, each for its stated reason. Chip row (9 of 14 fail): the default-side and long-label cases fail on overlap (e.g. 90: row `[0, 53][382, 93]` against cluster `[8, 60][112, 324]`; 270 long label: row `[439, 53][823, 137]` against `[711, 60][815, 324]`); the snapped cases fail on the 264 dp cap (382 wide); the wrap test fails because the chips sit side by side (taxon `[0, 53][216, 85]`, J8's `[224, 61][382, 93]`); the drag-across test fails (row's left stays at 0). The reach guards for J8's chip and the taxon chip's clear button pass at base, as predicted. The three landscape Q4 tests fail: "a 30 dp drag down moved the cluster 30 dp ... but was 0.0". The portrait Q4 guard passes at base.

### Built

`AvailabilityCompactMapUi.kt`:
- **Item 7:** the chip row's landscape modifier is `width(bar).wrapContentWidth(AbsoluteAlignment.Right if the cluster is on the left, else Left).widthIn(max = bar - (MAP_ICON_BAR_EDGE_INSET + measured cluster width + Spacing.sm))`, 264 dp in the test window. Portrait untouched.
- **Q4:** `legendBoundPx` now also requires `!landscapeCluster`.
- **Tests after the build:** all new tests pass (the six affected classes: 69 pass, 2 fail, the 2 being existing B2 tests below).

### Revert checks (each from a copy saved before editing, `/tmp/lf/orig/`; 0 compile errors each; fresh XML; each failure below is one that revert could produce)

| check | revert | result |
|---|---|---|
| R7c | the `widthIn` cap removed | 8 of the 3 classes' 20 tests fail: the width and wrap tests and the overlap tests (row `[2, 53][384, 93]` against the cluster at 90; "at most 264 dp wide" at the snapped cases); the wrap test shows the chips side by side (`[2, 53][218, 85]`, `[226, 61][384, 93]`) |
| R7 | cap removed and alignment back to `Alignment.Start` | 9 fail, adding the drag-across test ("the row moved to the bar's left end ... was at 0.0") |
| RQ4 | the `!landscapeCluster` condition removed | the three landscape Q4 tests fail ("moved the cluster 30 dp ... but was 0.0") |
| RQ4p | the bound dropped everywhere | the portrait Q4 guard fails ("dragged far down: the cluster `[328, 280][376, 660]` stops clear of the legend `[349, 592][376, 628]`") and so does the older T2 (portrait, expanded) |

The forward file was compared byte for byte with the saved forward copy after the four runs: identical.

### The full suite, and a stop: S10

From a cleared results directory at `b8d467b` (the tree after `e0baece`, so with the offline-safety changes merged in), 0 compile errors, 314 files all newer than the run's start: **2548 tests, 2523 pass, 1 fail, 24 skipped.** (The XML was overwritten by the revert runs that followed; the figures are from my read of it before they ran, and Gradle's own line, "2548 tests completed, 1 failed, 24 skipped", agrees.)

The one failure is **`AvailabilityScreenLandscapeB2Test` `S10 at ROTATION_90 with the filter chip showing, the central third stays clear`:** "nothing persistent may intersect the central third DpRect(274.3, 128, 548.7, 256); these do: [map-taxon-filter-chip DpRect(309, 93, 384, 137)]".

- **Cause:** the chip row is now at the bar's right end at 90. That test's fixture runs under Robolectric's default graphics mode, which measures text at near-zero width (Decision 6 in the first Resumed section): the search bar is 85 dp tall there, so the chip sits at y 93 to 137 and, at the right end, reaches into the central third (x 274 to 549, y 128 to 256). Before, it was at the left, x 0 to 75.
- **Experiment (not committed; the file was restored from a copy saved before editing):** the same test under `@GraphicsMode(NATIVE)` passes with the chip row as built.
- **What remains real:** under native text the standard label leaves the row above y 128, but a long label wraps to `[.., 53][.., 137]` (measured in my long-label tests), so a long label can still reach y 137 at x 120 to 384 at 90, which is inside the central third's x range (274 to 549) and its top edge (128). S10 does not test that.
- **I did not touch S10.** Changing its graphics mode is a change to a test I was not dispatched to change; the owner's or planner's ruling on S10 comes first. `-78`'s abort conditions include "a non-held failure".
- **Options, not chosen:** (a) run S10's chip test under native text, as a fixture-fidelity fix; (b) also cap the chip row's height (or its label) so it stays above the central third; (c) rule that the row may enter the central third when a long label wraps; (d) other.

### S3 changed, as pre-registered

`S3 at ROTATION_90 ...` asserted "the chip starts at the bar's start" (`AvailabilityScreenLandscapeB2Test.kt:281`). The owner's ruling puts the row at the bar's right end when the cluster is on the left, which is 90's default. I changed that one assertion to `bar.right == chip.right` and renamed the test. Every other assertion in it is unchanged. The "at 270" S3 test does not exist.

### Where it is

Everything is on `layout-fixes-wip` at `b8d467b`. **Nothing from this continuation is on `journal-redesign`** (it stays at `e0baece` plus whatever the planner has added): the suite has one red test.

## Device-only list (`-117`), S22 at 90 and 270

- The chip row's placement at the bar's away end and its width cap, with the taxon chip alone, with both chips, and with a long label (two or three lines).
- The row against the central third with a long label (see S10).
- The cluster dragged down past where the legend's bound stopped it, in landscape on the legend's side, collapsed and expanded, and its lower limit against the real navigation inset.
- Portrait: the cluster still stops above the legend when dragged far down (unchanged; device confirmation of Q4 in portrait).

## Decisions I made (the `-117` build)

11. **The cap includes the cluster's edge inset:** bar - (8 + measured width + 8) = 264 dp, not the message's "about 272". With 272 the row's inner edge would touch the cluster's (0 gap); the message says "the gap".
12. **The cap applies whichever side the cluster is on,** as the message reads. With the cluster on the far side, the row still wraps at 264 though nothing is beside it. A cap only on the bar's side would keep one line there. Flag for the planner.
13. **S3's assertion changed** (pre-registered); **S10 not touched.**
14. **The wrap test and the long-label tests** use "Chicken of the woods, sulphur shelf, the bright orange bracket fungus of oak" (78 characters). I measured only that label and "artist's bracket (12)".

## Flags outside scope (the `-117` build)

- **S10's fixture measures text at near-zero width** (the same cause as Decision 6): every B2 test that depends on text size without `@GraphicsMode(NATIVE)` measures a different layout from the phone's. Not checked beyond S3 and S10.
- **The relay and the file agree.** No conflict with `CLAUDE.md`.
