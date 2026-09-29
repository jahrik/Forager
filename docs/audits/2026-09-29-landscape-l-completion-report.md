# Dispatch 2026-09-28-160: the landscape icon cluster as an L — coder report

**Status (rewritten at the resume; the first stop, on the disk, is recorded below and was resolved): STOPPED AGAIN on three conflicts with existing behaviour that are the planner's to decide (see "Resumed", section 6). The L is built and its own tests pass; the full suite is red at 13 tests. Everything is on `landscape-l-wip`; nothing is on `journal-redesign`.** Sections below are appended as work lands; nothing above a "Resumed" heading is rewritten.

**Coder session.** The owner's launch prompt names `/model claude-sonnet-5-5`. The session's configured model id is `claude-sonnet-5-5`; the serving model was not independently read, so that is the configured identifier, not a verified one.

**Base.** `origin/journal-redesign` at `9c337f19` (fetched at the start of the session; it is the commit that carries `prompts/preserved/2026-09-29-26.md`). Worktree `/home/zynergy-labs/Zynergy/forager-wt/landscape-l`, branch `landscape-l`.

**Paths** (all under `app/src/main/java/com/zynergylabs/forager/app/ui/`): CMU = `availability/AvailabilityCompactMapUi.kt`, MCU = `availability/AvailabilityMapControlsUi.kt`, MC = `map/MapChrome.kt`.

## Premises re-verified at `9c337f19`

Read, not inferred:
- The landscape switch is `val landscapeCluster = railPortEdge != null && punchHoleEdge != null`, CMU:409; the branch is `ShortLandscapeClusterRow(...)`, CMU:1096; the composable is at CMU:1535. (Dispatch cites CMU:1095-1105 and :1534-1548: off by one line each; same code.)
- The container `Surface` opens at CMU:1025 (dispatch: 1024), `.testTag(MAP_ICON_CLUSTER_TAG)` at CMU:1041. It carries `color = mapIconClusterContainerColor()` (0.6), `shadowElevation = 2.dp`, a 1 dp border, and the measuring `onGloballyPositioned`.
- The bar is `MapIconBar`, MC:382; its `Column` has `padding(vertical = Spacing.xs)` and `spacedBy(Spacing.xs)`, MC:450-453: 5 × 48 + 4 × 4 + 2 × 4 = 264.
- Each row is `MapBarIconButton`'s 48 dp `Box` with `clickable` (MC:726-731), inside the `Surface`'s 24 dp rounded shape.
- The pill is `ControlPill`, MCU:178, a private composable with a `Column` of two `MapBarIconButton`s, the same padding and spacing, filled at `mapIconClusterChildColor()` (0.5).
- `mapIconBarRowAnchorOffset` hardcodes `Spacing.xs` row pitch, MC:90-95; `ADD_TILE_ANCHOR_OFFSET` is MCU:612; its only production caller for the compact map is CMU:1409. `AvailabilityScreen.kt:334` imports `mapIconBarRowAnchorOffset` (used by the Cartography-entry and wide-tree callers; not touched).
- `MapIconBar` has a second caller, `ui/log/CartographyEntryReportScreen.kt:532`, per the dispatch. Not opened or edited by this work.
- The remembered offsets, side and minimised flag are keyed on `landscapeCluster` at CMU:410-441; unchanged by this work.

## Design used (from the dispatch, unmade decisions listed in "Decisions I made" at the end)

- In landscape only: the container `Surface` becomes a plain `Box` (same padding, same measuring, same tag) that draws nothing and consumes no pointer input; inside it a `Column` holds the bar and, 8 dp beneath, a horizontal pill. Both are aligned to the cluster's outer edge.
- The bar gets a landscape row pitch of 0 and no end padding (240 dp); rendered at the standing 0.8 chrome alpha as one layer.
- The pill gets a horizontal form (record, then return, 48 dp thick) at the same 0.8, and the same zero pitch/padding.
- `mapIconBarRowAnchorOffset` gains a defaulted row-pitch parameter; the landscape AddActionTile anchor passes 0.
- Portrait Column, wide tree, `CartographyEntryReportScreen`, `MapChromeAlphaTest` untouched.

## Pre-registration (written and pushed before any test or code)

All at `w823dp-h384dp-land`, `ROTATION_90` (cluster on the left) and `ROTATION_270` (cluster on the right), `isRecording = true` unless stated.

**Predictions against the base (`9c337f19`), tests-first.** Every new test below fails at base for the reason named; a pass at base is a stop.

| # | Claim | Pass condition | Fails at base because |
|---|---|---|---|
| P1 | L shape | bar 48 × 240 (five contiguous 48 dp rows), gap 8, pill 48 thick × 96, pill outer edge flush with bar outer edge, return inboard of record, cluster 296 tall × 96 wide | base bar is 264 tall, pill is beside the bar, cluster 264 tall |
| P2 | Empty corner and gap reach the map | a real long-press at the point inboard of the bar's mid-height, and one in the 8 dp gap, each increments the map stub's long-press count; a long-press on the bar itself does not (positive control) | base container `Surface` covers both |
| P3 | Nothing drawn | pixels in the corner and the gap equal a reference map pixel (native graphics) | base container fill (0.6) is drawn there |
| P4 | Single-layer 0.8 | bar and pill interior pixels equal `0.8·chrome + 0.2·background` per channel, within 2/255 | base children composite over the container (0.92 over background) |
| P5 | End-row touches | five real touches across each of the fullscreen row and the add row (fractions 0.2/0.8 corners and centre of the row's own 48 dp bounds), all reach it; touches on rows 2 and 3 never reach the map | passes at base for touch routing where the rows already reach; the bar geometry asserted in the same test is what fails — flagged: this one may pass at base on its touch half and is not the discriminating test |
| P6 | Drag, snap, minimise, restore | after a snap the pill stays flush with the bar's outer edge and inboard-extending; 40 dp drag moves the cluster 40 dp; minimise and restore returns to the same place | shape assertion fails at base |
| P7 | Clamps | with the cluster dragged to the bottom, `bottom == mapArea.bottom` and height 296; with a search notice showing and the cluster dragged to the top, `top >= notice bottom` and height 296 | height is 264 at base |
| P8 | Anchor | `mapIconBarRowAnchorOffset(5, rowPitch = 0.dp)` is +96 dp from the bar's centre; the default call is unchanged at +104 dp | the parameter does not exist at base (compile error is expected; recorded, not a stop) |

**The corner-clipping stop (dispatch).** Prediction: five touches at fractions 0.2/0.8 of an end row's 48 dp box lie inside the rounded end. From the 24 dp corner radius on a 48 dp wide bar, the point at fraction (0.2, 0.2) is at distance √2 × 14.4 = 20.4 dp from the arc's centre (inside 24); at (0.1, 0.1) it is 27.0 (outside). So the gate samples are inside by geometry, and the extreme corners of the 48 dp box are outside the drawn shape by geometry. Whether Robolectric's Compose hit-testing follows the rounded clip is undetermined (the map, §6). A separate, non-gating probe of the extreme corners is run once and its result reported here rather than committed as a test.

**Machine.** 2332 MB available at the first check, under the 2.5 GB rule; no Gradle run started until both the memory and the Java-Gradle-process checks pass.

## Stop: no space left on device

**What happened, read from the build logs.**
- First run of the three test classes at the unmodified base (`:app:testDebugUnitTest --tests '*LandscapeLClusterTest' --tests '*LandscapeLClusterPixelsTest' --tests '*LayoutFixesShortLandscapeTest'`, `--offline`): `BUILD FAILED in 1m 2s`, `Could not add entry ':app:bundleDebugClassesToRuntimeJar' to cache executionHistory.bin`, then `Could not receive a message from the daemon`.
- Second run: `BUILD FAILED in 769ms`, `java.io.IOException: No space left on device` while resolving `incomingCatalogForLibs0`.
- `df` at the time: `/dev/nvme0n1p5`, 67 G, 100% used, 0 available; a byte count read a minute apart showed about 120 MB free, and it moves as other sessions build.

**Consequence.** Neither run reached compilation or a test; there are no JUnit results, so nothing in this report is a test result. The predictions in the pre-registration are untested.

**What I did not do, and why.** The space is held by other sessions' and the owner's work, none of it mine to delete: `~/Zynergy/forager-wt` is 11 G across some 30 worktrees, `~/.gradle` 9.8 G, `~/Android` 6.1 G, `~/.android` 4.9 G, `~/Zynergy/device-evidence` 3.8 G, `~/Zynergy/forager-repo-backups` 1.1 G. My own worktree is 165 MB, and clearing it would not free enough for a Gradle build. Removing another worktree's `build/` or the shared Gradle cache would be outside this dispatch's scope ("work only in your named worktree") and could break a build another coder or the device session is running. So this is a stop for the owner or planner to resolve, not something I worked around.

**What is pushed** (branch `landscape-l-wip`; nothing on `journal-redesign`):
- `LandscapeLClusterTest.kt` (geometry, real coordinate touches, corner and gap long-presses with positive controls, bottom and notice clamps), `LandscapeLClusterPixelsTest.kt` (native graphics: nothing drawn in the corner or the gap; bar and pill each one layer at 0.8), and edits to `LayoutFixesShortLandscapeTest.kt` replacing TR1, TR2 and TR4 (and the `bar()` helper's 4 dp), each carrying the owner's ruling verbatim in its comment.
- Not compiled yet. Unverified: that these tests compile, and that they fail at base for the reasons pre-registered.

**To resume.** Free roughly 2 GB (a Gradle build here wrote about 160 MB into the worktree before failing, and the daemon and test workers need more), then: run the three classes at base and confirm the pre-registered failures; implement (container `Surface` becomes a fill-less `Box` in landscape; `MapIconBar` and `ControlPill` gain a landscape row pitch and horizontal form; `mapIconBarRowAnchorOffset` gains a row-pitch parameter); revert checks from saved copies; the full suite from a cleared results directory.

## Decisions I would have to make (not made; for the planner)
1. **The pill's width.** The dispatch fixes its thickness (48) and outer-end flush, not its length. I pre-registered 96 (two 48 dp buttons, no padding or spacing, the same rule the bar's "no spacing" gives), which also makes the record button sit exactly under the bar and puts "half of the pill" under the bar as the owner said. The alternative is 108 (today's 4 dp padding and spacing turned horizontal), which offsets the record button 4 dp from the bar's edge. The tests pin 96.


# Resumed (dispatch 2026-09-28-162 continuation; the owner cleared space)

## 1. What governs, quoted
`prompts/preserved/2026-09-29-28.md` at `origin/journal-redesign` (`3016b39b`), verbatim:

> 1. **The pill** (your open question). The owner, verbatim: "the icons need to stack fully. Make sure that happens and have the pill extend outward like the L".
>    - The pill is **96 dp**: two 48 dp buttons with no padding or spacing.
>    - **Record's 48 dp box is exactly under the bar's 48 dp column.** Its left and right edges equal the bar's, within 0.5 dp in the tests, on both sides.
>    - Return extends outward, inboard of the screen.
>    - Your pre-registered 96 stands. Add an explicit alignment assertion if the shape test does not already pin both edges.
> 2. **The disk.** The owner answered "1 C": **the owner is clearing space. You delete nothing outside your worktree.**
>    - Before each Gradle run, also check `df -m /` shows at least **2048 MB** available, and wait if it does not.
>    - A run that fails with "No space left on device" is not evidence of anything. Re-run it from a cleared results directory once space is back.

The planner's message carrying it said the same; "Decisions I made" below therefore lists no open pill-length question. `LandscapeLClusterTest.assertShape` now pins both of record's edges to the bar's (added before the base run).

Gate applied before every Gradle run from here: no Java Gradle process (`pgrep -af '^\S*java .*([G]radleWrapperMain|[G]radleWorkerMain)'`), at least 2500 MB memory available, at least 2048 MB disk. Several runs waited on other sessions' builds. I did not run `./gradlew --stop`. A daemon left by the first crashed run held a lock on this worktree's `.gradle`, so runs used `--no-daemon`.

## 2. Tests first, at the unmodified base (`9c337f19` plus the tests only)
Run: `:app:testDebugUnitTest --tests '*LandscapeLClusterTest' --tests '*LandscapeLClusterPixelsTest' --tests '*LayoutFixesShortLandscapeTest' --offline --no-daemon`, results directory cleared first; no `e:` lines in the log; read from the JUnit XML: **30 tests, 16 failed, 14 passed.**

Failures, each for the pre-registered reason (message quoted from the XML):
- L1 (both rotations): "the bar is 240 tall ... expected:<240.0> but was:<256.0>" (the base bar has no end padding in `bar()` terms, 256 from row to row).
- L3 (both): "a real long-press in the empty corner inboard of the bar reached the map ... expected:<1> but was:<0>": the container fill takes it.
- L4 bottom clamp and notice floor (both): "the cluster is 296 tall ... but was:<264.0>".
- L5 (both): "the empty corner inboard of the bar shows the map: expected (0.996, 0.969, 1.000), read (0.957, 0.922, 0.890)": the container's 0.6 fill.
- TR1 (both): "the cluster ... is 296 tall ... but was:<264.0>"; TR2 and TR4 (both rotations, the four): "the pill ... is 8 dp below the bar ... expected:<8.0> but was:<-104.0>": beside, not beneath.

Passing at base: TR3, TR5, T5, T9 (unchanged tests, as expected), **and L2 (the real touches on the end rows), which I had flagged as not discriminating at base.** They confirm the touch routing before and after; the shape they sit in is asserted by L1. The L2 middle-row tests passed at base in their first form and were later narrowed (section 5).

## 3. What was built (all under `app/src/main/java/com/zynergylabs/forager/app/ui/`)
- `availability/AvailabilityCompactMapUi.kt`: in landscape (`landscapeCluster`, CMU:409) the container `Surface` is replaced by a plain `Box` that draws nothing and takes no pointer input, still carrying the padding, the measuring and `MAP_ICON_CLUSTER_TAG`; inside it `LandscapeLCluster` (replacing `ShortLandscapeClusterRow`): a `Column` aligned to the cluster's outer edge, the bar, an 8 dp gap, the horizontal pill. The bar and pill lambdas are hoisted above and take their fill and spacing as parameters; the portrait branch is the old `Surface`/`Column` with the same arguments (child fill, `Spacing.xs`). The AddActionTile's y offset uses `ADD_TILE_ANCHOR_OFFSET_LANDSCAPE` in landscape.
- `map/MapChrome.kt`: `MapIconBar(rowSpacing = Spacing.xs)` (default unchanged, so `CartographyEntryReportScreen`'s call is unchanged); `mapIconBarRowAnchorOffset(rowIndexFromTop, rowSpacing = Spacing.xs)`; `MAP_ICON_BAR_LANDSCAPE_ROW_SPACING = 0.dp`; `mapIconChromeFillColor()` (the standing 0.8 fill).
- `availability/AvailabilityMapControlsUi.kt`: `TrailheadControls`/`ControlPill` gain `horizontal`, `onLeftSide`, `fillColor`, `rowSpacing` (defaults reproduce the vertical pill exactly); the horizontal pill is a `Row` whose order mirrors by side so record is always the outer end; `ADD_TILE_ANCHOR_OFFSET_LANDSCAPE`.
- The wide (tablet) tree, `CartographyEntryReportScreen`, `MapChromeAlphaTest` and its constants: not edited.

## 4. Forward runs, and what each caught
- First forward run (12 of 30 failed). One was **a real bug in my first build, caught by L1 at ROTATION_270**: the horizontal pill's `Row` put record first, so on the right side record sat at the inboard end, beside the bar's column, with return under it (message: "the record button's left edge equals the bar's ... expected:<767.0> but was:<719.0>", and TR2/TR4 the same on that side). Fixed by mirroring the Row order.
- The rest were my tests' own errors, fixed in the tests, not the code: L4 notice floor assumed the top reaches the notice's bottom (section 6b); the L5 pixel sample landed on the minimise handle; L4's message text read the "Fullscreen" row while the screen was legitimately in fullscreen; L2's touch fractions on rows 2 and 4 (section 6c).
- Last forward run of the three classes: **30 tests, 2 failed (T9, section 6a), 28 passed.** New: `MapIconBarAnchorTest` (2, the default 104 dp unchanged, the L's 96 dp) and L6 (the add menu's panel 32 dp below the add row's centre; a characterisation read at the implementation, see the test's comment).

## 5. Revert checks (from copies saved to `/tmp/llrev` before each edit; the runner refuses results if the log has an `e:` line and restores from the saved copy; each log had none)
| # | Revert | Failure that this edit alone causes |
|---|---|---|
| R1 | landscape bar back to `Spacing.xs` row spacing | L1 "the bar is 240 tall ... but was:<256.0>", TR1 "the cluster ... is 296 tall ... but was:<320.0>", TR2/TR4 "8 dp below ... but was:<12.0>", TR5 drag 32 not 40 (the clamp) and L6 "expected:<32.0> but was:<24.0>" |
| R2 | horizontal pill order not mirrored | only L1, TR2, TR4 at the right-hand cluster: "the record button's left edge equals the bar's ... expected:<767.0> but was:<719.0>", "the return button ... extends inboard (left) of the record button" |
| R3 | container `Surface` with the 0.6 fill back under the L | L5 "the gap under the bar shows the map: ... read (0.973, 0.945, 0.976)"-family corner/gap pixel failures and L3 "a real long-press in the empty corner ... reached the map ... expected:<1> but was:<0>" |
| R4 | the pill's own fill back to the 0.5 child fill | only L5 "the pill reads as one layer at 0.8 over the map: expected (0.941, 0.906, 0.851), read (0.965, 0.929, 0.906)" |
| R5 | AddActionTile anchor back to the portrait offset | only L6 "the add menu's panel bottom is 32 dp below the add row's centre ... expected:<32.0> but was:<40.0>" |
**Correction to the R3 row, made after re-reading the result files:** the pixel message `the gap under the bar shows the map ... read (0.973, 0.945, 0.976)` belongs to R1 (the spacing revert moves the pill into the gap), not R3. R3's L5 failure is `the empty corner inboard of the bar shows the map: expected (0.996, 0.969, 1.000), read (0.957, 0.922, 0.890)`, the same reading as at base, as it should be with the container fill back.

Every run also carried T9's two known failures (not caused by these edits). After the last revert, `git status` was clean against `af00f9a0` and the anchor line is the forward one (checked by grep), so the forward change was present. `L3`'s reverted result (R3) confirms the positive control: with a fill the corner really is captured.

## 6. Three conflicts with existing behaviour. I have not decided any of them.
**a. T9 fails at both rotations (existing test, not edited).** "the search bar [0.0, 0.0][384.0, 45.0] and the cluster [8.0, 44.0][104.0, 340.0] do not intersect" (and the right-hand mirror). The centred 296 dp L has its top at 44 dp; the search bar's bottom is 45. Cause, read from the code: the top clamp's upward bound is `coerceIn(-fallbackDownwardOffsetPx, 0f)` at CMU:846, so the clamp can pull a cluster up but never push a centred one down, and 296 > 384 - 2 x 45 by 2 dp. At the old 264 it never met the bar. Options for the planner: let the top clamp push down (a change to the clamp's semantics); accept a 1 dp overlap and change T9; or another. Device insets differ, so the S22's real figure is unknown.

**b. The notice floor cannot hold in a 384 dp window (my test corrected, a code fact reported).** With a notice showing, the floor is `min(notice bottom, lowest edge)` (CMU:880-882), so the L's top sits at 88 dp with the notice ending at 137: the L overlaps the notice by 49 dp (at the old 264, by 17 dp). The L4 test pins the code's own contract (top = 88), not a clearance.

**c. The minimise handle steals the outer-lower corner of the compass row and the outer-upper corner of the Layers row: 11 existing tests fail.** `LayoutFixesChipRowLandscapeTest` T7 (all 11, both rotations): "five real touches across the reset button [8.0, 92.0][56.0, 140.0] all reached it expected:<5> but was:<3>" (4 of 5 at ROTATION_270). The handle is a 20 x 72 dp box centred on the bar's mid-height (unchanged, per the dispatch). With rows 48 dp apart it reaches 12 dp into rows 2 and 4; at the old 52 dp pitch it reached 8 dp, just short of the sample at 0.8 of the row's height. My own L2 middle-row test therefore samples the compass and locate rows only at the centre column and the inboard 0.2/0.8 columns, and says why in its comment: that is the finding excluded by name, not hidden. Options: shorten the handle's box to 48 dp; move or narrow it; accept the loss and rewrite T7's sampling. The dispatch says the handle is unchanged, so this is a conflict between two of its own instructions.

## 7. The corner-clipping stop (dispatch), triggered
Scratch probe (real touches, two rotations, not committed; the source is not in the repo), the bottom row (its result is read from the add menu opening, which is unambiguous). At the row's own 48 dp box, fractions across and down:
- reached: (0.03, 0.03), (0.97, 0.03), (0.1, 0.1), (0.9, 0.1), (0.15, 0.15), (0.85, 0.85);
- **lost to the map** (`mapTaps+1`): (0.03, 0.97), (0.97, 0.97), (0.1, 0.9), (0.9, 0.9).
Same result at both rotations. The end of the bar is a 24 dp semicircle (a 24 dp corner radius on a 48 dp wide bar), so the two outer corners of the end row's box lie outside the drawn shape and the shape's clip takes the hit test: (0.9, 0.9) is 27.2 dp from the arc's centre (lost), (0.85, 0.85) is 23.8 (reached). Lost area is the two corner slivers outside the semicircle, about 5% of the box, none of it inside the drawn shape. The top row's probe is not usable (toggling fullscreen moves the chrome mid-probe); by the same geometry it is the mirror image. The gate samples in L2 (0.2/0.8 and the centre) all reach it. At the old 4 dp end padding the same geometry lost points near the corner too, only fewer (arithmetic, not run: (0.1, 0.1) at the old padding is 24.5 dp from the centre, just outside); that comparison is inferred, not measured. Per the dispatch I did not shrink the rows and this is the report of the geometry.

## 8. Full suite
From a cleared results directory, `--continue`, run started at epoch 1790675323; **348 result files, none older than the start; 2821 tests, 13 failed, 0 errors, 24 skipped** (skipped is the existing `@Ignore` count; I did not measure the base's). The 13: T9 x 2 (6a) and `LayoutFixesChipRowLandscapeTest` T7 x 11 (6c). No other class fails, including the portrait and tablet suites, `MapChromeAlphaTest`, and the Cartography entry report's.

## 9. Not tested / device-only
- Not run: the base's full suite (no baseline for the 24 skipped or to say the two T9 and eleven T7 are new by suite count; they pass at base per the tests-first run for T9 and by the old 8 dp overlap arithmetic for T7).
- Robolectric reports zero insets, so the L against the S22's real insets, the cut-out and the rail; thumb reach; and the empty corner on a real screen are device items at 90 and 270. So is whether real fingers land in the lost corner slivers.
- L6's 32 dp is read from the implementation, not derived.

## 10. Decisions I made
- The corrected tests (section 4) and the L2 middle-row sampling (6c) are my test-design choices, disclosed above.
- `LandscapeLCluster` replaces `ShortLandscapeClusterRow` (the old composable is gone, not left dead).
- Pushed to `landscape-l-wip`, not `journal-redesign`, because the branch carries three unresolved conflicts and a red suite.

## 11. Flags outside scope
- `/tmp` is shared: another session overwrote `/tmp/d58check.sh`, so the D58 check is inline (0 hits, diff and commit messages).
- A crashed run's daemon held this worktree's `.gradle` lock; I did not stop it.
- The disk and memory gates delayed runs by hours in total; several waits were on another session's long Gradle process.
