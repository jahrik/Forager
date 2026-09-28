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
