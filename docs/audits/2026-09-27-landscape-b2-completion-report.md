# Landscape build B2: completion report (cloud build step)

Date: 2026-09-27 (UTC). Branch `landscape-b2r`, from base `1c88e61` (intent `2026-09-27-35`) to
head `299a673` (the last code commit; this report's own commit follows it). Specification: intent
`2026-09-27-35` in `RECORD.md`. Continuation: `2026-09-27-36` (`1c9d495`), store copy
`prompts/preserved/2026-09-27-08.md`.

This is the **cloud build** step of the three-step B2 run. It writes no terminal, no backup and
no merge. Per the owner's ruling it also opens no pull request: the PR goes to a fresh coder.
Nothing here was run on a device.

## Summary

- **Built:** S1-S10 as the intent specifies, each tests-first. Every S item has at least one test
  that failed at the base for its stated reason and passes after.
- **Suites:**
  - Base (`2411b5d`): 221 classes, 1729 tests, 0 failures, 24 skipped.
  - After (`299a673`): 224 classes, 1757 tests, 0 failures, 24 skipped.
  - Growth: +3 classes, +28 tests, skipped unchanged. The planner predicted +20 to +45.
- **Revert checks:** all six the intent lists, each seen failing for a reason specific to its own
  edit. Two edits failed to compile, and the runner refused to cite them (see below).
- **Existing test changes:** the only intentional one is S9 (`AvailabilityScreenShortLandscapeTest`
  :239 and :247). Every other existing test passes unchanged.

## Step 0, verbatim

This covers the environment report and what the owner's rulings changed in it.

- **(a)** `hostname` gives `vm`.
- **(b)** `gh pr merge 999999 --merge --repo slayer8366/Forager` gives
  ``PreToolUse:Bash hook error: history_guard: `gh pr merge` denied, (b) form: `--repo` is not allowed.``
  The guards were active.
- **(c)** Three outputs:
  - `gh auth status` gave `/bin/bash: line 1: gh: command not found` (exit 127).
  - `git status -sb` gave `## claude/landscape-b2-cloud-build-3cdfmf` and
    `?? prompts/preserved/2026-09-27-08.md`.
  - `git rev-parse HEAD` gave `1c88e610d47a767822096ea95af509952c0467f2`.
- **(d)** `git push --dry-run origin HEAD:refs/heads/landscape-b2r` gives `Everything up-to-date`.
- **(e)** `ANDROID_HOME` and `ANDROID_SDK_ROOT` were empty. `java -version` gives
  `openjdk version "21.0.10" 2026-01-20` (build 21.0.10+7-Ubuntu-124.04).
  `scripts/setup-android-sdk.sh` ran and ended "Android SDK installed at /opt/android-sdk".

The coder stopped on the branch name and on the missing `gh`. After the owner's ruling (quoted
below):

- **Branch.** The ruling said to create a local `landscape-b2r`. A local `landscape-b2r` already
  existed, tracking `origin/landscape-b2r` at `1c88e61`, so it was checked out rather than created.
  `git checkout landscape-b2r` gave "Switched to branch 'landscape-b2r' / Your branch is up to date
  with 'origin/landscape-b2r'."
- **gh install.** `apt-get install -y gh` installed `gh 2.45.0-1ubuntu0.3` (Ubuntu
  noble-updates/universe).
- **gh auth.** `gh auth status` then gave:

  ```
  github.com
    X Failed to log in to github.com using token (GH_TOKEN)
    - Active account: true
    - The token in GH_TOKEN is invalid.
  ```

  The coder stopped again. The owner ruled on the PR route and on committing the continuation
  (quoted below). `gh` was not used after that.

## Relayed messages, verbatim

Each was relayed by the coordinator (the main session). Planner-log lines are not cited: the log
is on the owner's machine, and this cloud session cannot read it. The continuation `2026-09-27-36`
quotes the first two. The rest are quoted here, because this dispatch limits `RECORD.md` to the
continuation. The local finish step can carry them into the record with their log lines.

1. On the Step 0 stop:
   > Owner ruling on your Step 0 stop: "Install gh. Work on landscape-b2r"
   >
   > These are the owner's exact words, relayed by the main session.
   > - Branch: create a local `landscape-b2r` tracking `origin/landscape-b2r` (both are at 1c88e61), check it out and build there. This is your option (ii).
   > - gh: install the `gh` CLI in this session, then run `gh auth status`. The owner did not rule on auth. If gh cannot authenticate, don't work around it by other routes; stop and report exact output.
   > - Everything else in the dispatch stands unchanged. Resume from Step 1, and record the install and the branch creation in the completion report's Step 0 section.
2. On the gh auth stop:
   > Owner ruling on the gh auth stop: two answers. The main session relays them word for word below.
   > Q1 "How should the coder handle the PR and CI steps?" answered "Allow GitHub MCP tools".
   > Q2 "Should the coder commit and push the store copy plus its continuation entry now?" answered "Yes, commit and push now".
   >
   > What this means for you:
   > 1. Do Step 1 now on `landscape-b2r`: run both checkers, add the continuation entry, commit your store copy, and push. Then continue with Steps 2–6 as the dispatch says.
   > 2. For Step 7, open the PR and read CI through the GitHub MCP tools (create_pull_request, pull_request_read, actions_*) instead of gh. The owner has to add those tools to your role_guard allowlist and agent tool list, and neither you nor the main session may edit `.claude/`. When you reach Step 7, if the tools aren't in your tool list or role_guard refuses them, stop and report the exact refusal. Don't try any other route.
   > 3. You can uninstall gh or leave it installed, but don't use it for Step 7.
3. On the owner's commit `2fae413`:
   > Owner commit on origin/landscape-b2r: the owner has pushed `2fae413` there, and your next push will be rejected until you merge it in.
   >
   > The owner's words, relayed word for word by the main session: "Option 2 is done". Option 2 was the owner editing `.claude/agents/coder.md` line 4 through the GitHub website to add GitHub MCP tools (the planner proposed create_pull_request, pull_request_read, actions_list, actions_get and get_job_logs). The planner has not seen the commit's content. `git ls-remote` shows origin/landscape-b2r at 2fae4133ebaf125a1c011e25bb51a97be7c33c2d, while local is at 1c9d495.
   >
   > What to do:
   > 1. Before your next push, run `git fetch origin landscape-b2r`, then `git merge origin/landscape-b2r`. Use a merge commit: no rebase, no amend, no force. Merging the owner's `.claude/` commit is not you editing `.claude/`; don't edit it yourself.
   > 2. Report what 2fae413 changed (`git show --stat` and the diff to coder.md). If it touches anything besides `coder.md`, or a merge conflict comes up, stop and report.
   > 3. If `check_kit.py` or CI flags drift in coder.md against kit.lock, that is outside this work. Report it and don't touch it.
   > 4. Step 7 stays as already ruled: use the GitHub MCP tools if they are in your tool list. The planner expects they won't be, since your tool list was set when you started. If they aren't, or a call is refused, stop after Step 6 and report the exact result. Don't use gh or any other route.
4. On the Maven 429 stop:
   > Owner ruling on the Step 2 429 stop: "1 I choose B". This is the owner's reply, relayed word for word by the main session.
   >
   > (b) is your option: fill the Gradle cache slowly first (`./gradlew :app:dependencies`, or jars one at a time), then run the base suite. The owner set no cap on attempts. The planner proposed "up to three suite runs"; the owner chose B without it, so don't treat that cap as ruled. If the suite still hits 429s after the pre-fetch, stop and report the counts, as before.
   >
   > On the uncompiled draft: the owner asked for it to be pasted to them so they can keep a copy outside this session and hand it back to you. There's no ruling on committing it. Leave it where it is, don't commit it, and carry on with Step 2 on the clean base.
5. On the draft question:
   > Owner ruling on the draft question: "Start from the draft". These are the owner's words, relayed by the main session. This is your option (i), and it replaces the earlier "leave it uncommitted" instruction.
   >
   > What that means:
   > - Copy the draft from the scratch folder into the tree, compile it, and fix it, including the four weaknesses you listed. Write S1 and S9 tests from scratch as the intent specifies.
   > - The rest of Step 3 is unchanged. Tests-first commits must each be seen failing for the reason the intent states before their implementation, with a push after each. A tests-first commit that passes before its implementation is an abort.
   > - Keep the original draft in the scratch folder, untouched.
   > - Given the JVM crash you saw: check every run's build log for a crash or compile error before citing its JUnit XML (CLAUDE.md revert-runner and stale-results rules).
   > - Your next record entry should quote the relayed messages still owed, with this one among them.
   > - Then Steps 4–6 as dispatched. Stop after Step 6 and hand back with the final pushed head SHA and the completion report's filename. The PR goes to a fresh coder.

## Owner commit merged

`2fae413` (the owner's, made on the GitHub website) changes one line of
`.claude/agents/coder.md`:

- `tools: Read, Grep, Glob, Edit, Write, Bash` became
- `tools: Read, Grep, Glob, Edit, Write, Bash, mcp__github__create_pull_request, mcp__github__pull_request_read, mcp__github__actions_list, mcp__github__actions_get, mcp__github__get_job_logs`

It was merged with `git merge --no-ff`, with no conflict, as `2411b5d`.

After that merge, `check_kit.py` reports:

```
FAIL: 1 vendored file(s) differ from .claude/kit.lock (kit v0.2):
 - .claude/agents/coder.md: sha256 24ed6eb89133e249f2b843879ef13b825ea282f80cac8ef3a2bffbcc86dc0cc4 differs from the lock's 2c10e174d6b59862c058b873617321292fce25630684907740d036b52b33c68c
```

This is not touched, as ruled. No workflow in `.github/workflows/` runs `check_kit.py`.

## The Gradle cache, and one JVM crash

| Run | What it was | 429s | Result |
|---|---|---|---|
| 1 | Base suite, default | 7 | failed at `:app:kspDebugKotlin` |
| 2 | Base suite, `--max-workers=2` | 2 | failed at `:app:compileDebugKotlin` |
| 3 | Base suite, `--max-workers=1` plus Gradle retry/backoff properties | 12 | failed resolving `debugUnitTestRuntimeClasspath` |

These three runs were before the owner's ruling. After it (option (b)):

- **Metadata.** `./gradlew :app:dependencies` gave 0 429s.
- **Jars, one at a time.** A session-local init script (not in the repository) fetched each of
  312 modules from 33 debug, Kotlin and KSP configurations, without their dependencies, pausing
  150 ms between downloads. It took four passes:
  - pass 1: 309 fetched, 3 failed;
  - pass 2: failed in 7 s, because the coder added `--refresh-dependencies` (the coder's error);
  - pass 3: 310 fetched, 2 failed;
  - pass 4: 312 fetched, 0 failed.
- **Base suite, run 4.** No 429s. The test JVM crashed with `SIGSEGV` in the C2 JIT compiler
  (`libjvm.so Node::uncast(bool)`) while compiling
  `android.database.sqlite.SQLiteDatabase::rawQueryWithFactory`, and exited with 134. By then 177
  classes and 1299 tests had passed, with 0 failures.
- **Base suite, run 5.** The same command, unchanged, passed: 221 / 1729 / 0 / 24. This is the base
  cited above. The crash was intermittent (one in two runs).

Every run cited below had its build log checked for `^e: ` (compile errors) and for the JVM's fatal
error banner before its XML was read. The run script deletes the classes' old XML first and refuses
to report if either is present.

## Tests first

The tests-first commit is `6c96a24`, which adds `AvailabilityScreenLandscapeB2Test.kt` with three
classes:

- `AvailabilityScreenLandscapeB2Test`: 26 tests, `w823dp-h384dp-land`.
- `AvailabilityScreenLandscapeB2TurnTest`: 1 test, a portrait window turned to landscape.
- `AvailabilityScreenPortraitStripTnumTest`: 1 test, `w360dp-h640dp-xhdpi`.

It started from the draft the owner kept, sha256
`712000e01d65fe3bf9a8553d46251057099313df9d4e31b0d017c0b87e2d7291`. The original is still in the
scratch folder, unchanged. The four weaknesses the coder listed were fixed:

1. The TurnTest comment said "at ROTATION_90" where the code uses 270. The comment now says 270,
   and why.
2. `isBarItself()` was always false. It is removed, and S8 now counts the bar's and the cluster's
   own Surfaces as controls a sample point must not land in.
3. `turnTo` was unverified, and it turned out not to work. The first two tries left the screen at
   the old rotation (the rail stayed at x=743 after a "turn to 270"). After the second miss the
   coder instrumented instead of guessing:
   - a probe composable under the same provider read `currentWindowPortEdge()` as `Left`, while the
     screen's rail had not moved;
   - `LocalConfiguration` is built with `compositionLocalOf$default` (structural equality), read
     from the compose-ui 1.12.0 bytecode;
   - the base `navigationHidden` is 2 (`NAVIGATIONHIDDEN_YES`), so the copy the second try provided
     compared equal (`equalToBase=true`).

   The fix flips `navigationHidden` relative to the base. `turnTo` now also asserts the rail moved
   to the new port edge, which proves the screen itself saw the turn.
4. S7's single guessed frame (one frame plus 48 ms) became a frame-by-frame scan of up to 1.5 s.
   The map's bounds are asserted equal at every frame.

S1 was written new. S9 is a rewrite of two existing tests (below).

The run at the base (`run-tests-first-base3`) had 22 of 26, 1 of 1 and 1 of 1 failing:

| Item | Test(s) | Failure at the base (message) |
|---|---|---|
| S1 | 90, 270 | "the search bar DpRect(left=0.0.dp, …, right=743.0.dp, …) stays clear of the far half of the map" (at 270: left=80, right=823) |
| S2 | bar 90, 270 | "the bar's width is min(384, centre distance - 8) expected:<384.0> but was:<743.0>" |
| S2 | dropdown 90 / 270 | "dropdown right = bar right expected:<743.0> but was:<823.0>" / "dropdown left = bar left expected:<80.0> but was:<0.0>" |
| S3 | chip 90 | "the chip DpRect(left=334.0.dp, top=129.0.dp, …) is directly under the bar DpRect(… bottom=85.0.dp), not below the strip's band too" |
| S4 | strip 90, 270 | "the strip is at the top of the map, not below the search bar expected:<0.0> but was:<85.0>" |
| S4 | HUD 90, 270 | "the HUD is at the top of the map expected:<0.0> but was:<85.0>" |
| S5 | landscape; portrait | "the strip's heading text uses tabular figures expected:<tnum> but was:<null>" |
| S6 | default 90 | "the cluster DpRect(left=687.0.dp, …) is on the left half of …" |
| S6 | turn 90 to 270 | "at 90 the cluster starts on the left, the punch-hole side" |
| S6 | port-side drag across a turn | "at 270 the port edge is the left; the cluster follows it" |
| S6 | TurnTest | "landscape at 270: the cluster defaults to the punch-hole side, the right" |
| S7 | enter 90 / 270, leave 90 | "the rail must be seen part-way toward the port edge … frames seen: [DpRect(left=743.0.dp, …)]" (the rail vanished in one frame) |
| S8 | beside the bar, 90 | "a long-press at (747.0.dp, 2.0.dp) must reach the map expected:<1> but was:<0>" (next to a full-width bar is the rail) |
| S10 | navigating 90 / 270 | "… these do: [navigation-hud DpRect(left=0.0.dp, top=85.0.dp, right=743.0.dp, bottom=209.0.dp)]" |
| S10 | chip showing, 90 | "… these do: [map-taxon-filter-chip DpRect(left=334.0.dp, top=129.0.dp, right=409.0.dp, bottom=173.0.dp)]" |

**Four tests pass at the base as well as after. They are flagged, per CLAUDE.md, as checks that do
not discriminate on their own:**

- **S10 without navigation, at 90 and at 270.** Before B2 nothing persistent reached the central
  third (the bar ended at y=85, the cluster and rail sat right of x=548). They guard against B2's
  moves introducing an intrusion. The navigating and chip S10 cases do fail at the base.
- **S8 beside the cluster, at 90.** Beside a right-hand cluster at the base is map. It guards the
  Surface rule after S6 moves the cluster. S8 beside the bar does fail at the base.
- **S6's default at 270.** At 270 the punch-hole side is the right, which was already the default.
  This was predicted in the continuation's mechanism prediction (i). The other S6 tests fail.

No S item's tests all pass at the base.

## Implementation commits (each pushed)

| Commit | Items | Result after |
|---|---|---|
| `cc35095` | S1-S3 | S1-S3 and S8 (bar) pass. S1 at 90 still failed only on its cluster assertion (S6's). B1 11/11. |
| `c66bd53` | S4-S5 | S4, S5 (both) and S10 (navigating) pass. B1 11/11. |
| `dc0b3f7` | S6 | S6 (all) and TurnTest pass. B1 11/11; `AvailabilityScreenMapIconStackTest` 104 tests, 0 failures (19 skipped, as before). |
| `baf2ffe` | S7 | 26/26. B1 11/11 and `AvailabilityScreenTurnToShortLandscapeTest` 1/1 unchanged (their 2 s clock advances still see the rail gone). |
| `299a673` | S9 | `AvailabilityScreenShortLandscapeTest` 11/11 |

Where each item lives:

- **S1.** `AvailabilityScreen` computes `punchHoleEdge = punchHoleEdgeFor(landscape,
  currentDisplayRotation())` and passes it through `CompactMainScaffold` to `CompactMapTab`.
  `punchHoleEdgeFor` now has a caller.
- **S2.** In `CompactMainScaffold`, inside the `BoxWithConstraints`:
  `landscapeSearchWidth = min(384dp, maxWidth/2 − punch-hole-side controls padding − 8dp)`, on the
  Map tab of a short landscape window only.
  - The bar's wrapper becomes `padding(mapControlsPadding).fillMaxWidth().wrapContentWidth(side).width(cap)`.
  - The dropdown aligns TopStart or TopEnd, with `padding(mapControlsPadding)` and `width(cap)`.
  - The scrim takes `padding(mapControlsPadding)` on the Map tab.
- **S3.** The chip aligns to the punch-hole side at `topInset + Spacing.sm`, in a column the bar's
  width, `wrapContentWidth(Alignment.Start)`.
- **S4.** The strip mount and the HUD mount align to the top corner on the rail side, with
  `padding(controlsPadding)` and no `topInset`.
  - The strip passes `contentWidth = true`, which drops the Row's `fillMaxWidth` and the inner Row's
    `weight(1f)`, and also the no-fix line's `weight(1f)` (see Decisions).
  - The HUD is `widthIn(max = 360.dp).fillMaxWidth()`.
- **S5.** `labelMedium.copy(fontFeatureSettings = "tnum")` on the heading, elevation and
  coordinate Texts, in both orientations.
- **S6.** `MapIconClusterPositionState` gains `landscapeOnPortSide`,
  `landscapeUserChosenOffsetPx`, `landscapeDisplayedOffsetPx` and `landscapeIsMinimized`.
  - The side goes through a `LandscapeClusterSide` delegate, translated from the current port and
    punch-hole edges.
  - The drag's `pointerInput` is keyed on the two edges, so a turn restarts the gesture block
    rather than leaving it writing through the previous orientation's state.
  - Portrait reads and writes the original fields.
- **S7.** The Map tab's rail is in an `AnimatedVisibility` with `slideIn/OutHorizontally` toward
  the port edge on `MotionTokens.navigationMotionSpec()` (`motionScheme.defaultSpatialSpec`).
- **S9.** Described in the next section.
- **S10.** Tests only.

## S9: the tests B1 would lose

`AvailabilityScreenShortLandscapeTest`, at :239 and :247 as of `1c88e61`: B1's "no control on the
map sits under the rail", at 90 and at 270. B1 relied on the cluster defaulting to the rail's side
at 90. Each test now drags the cluster to the port side first, with a real long-press-then-drag on
its minimise handle.

The changed assertions, named:

- **New in both:** the drag, and "the cluster … was dragged to the port side, the right/left".
- **At 270:** `MAP_ICON_CLUSTER_TAG`'s `assertIsDisplayed` is added. It was at 90 only.
- **Unchanged:** `assertNoControlUnderRail`. The doc comment was rewritten to say why.

## Revert checks

Each check followed the same procedure:

1. Save a copy of the file.
2. Make a one-line edit.
3. Run the affected classes. The script refuses on compile errors or a JVM crash, and deletes the
   classes' old XML first.
4. Restore the file from the saved copy, never from git.
5. Confirm the restored file is byte-identical to HEAD's, and that `git diff --stat` is empty.

Every check below passed step 5.

| Intent's check | Edit | Result (failures specific to this edit) |
|---|---|---|
| Search cap | Removed the bar wrapper's `.width(landscapeSearchWidth)` | 9 fail. S2 "the bar's width … expected:<384.0> but was:<743.0>" (90, 270); S1 "stays clear of the far half"; S8 at (747, 2); S2 dropdown "dropdown right = bar right expected:<743.0> but was:<384.0>" (the dropdown kept its own cap). S4 strip "does not reach the search bar" follows, since a full-width bar reaches the strip. |
| Punch-hole placement | `val punchHoleEdge = portEdge;` in place of `punchHoleEdgeFor(` (the call kept, unused) | 11 fail. S1/S2 "the bar starts at the punch-hole-side (left) edge expected:<0.0> but was:<419.0>" (and at 270, 823 vs 404); S6 defaults on the wrong halves. The chip, and the strip meeting the bar, follow from the bar being on the rail side. |
| Strip content width | `contentWidth = false` at the strip mount | 2 fail: S4 strip "is content-width, well short of the space beside the rail" (strip 0-743 at 90, 80-823 at 270) |
| HUD rail side | The HUD's `.align(rail side)` becomes `.align(Alignment.TopCenter)` | 2 fail: "the HUD ends at the rail's inner edge expected:<743.0> but was:<552.0>" (90) and "starts … expected:<80.0> but was:<272.0>" (270) |
| Cluster per-orientation memory | 1st edit: the side delegate's condition to `if (false)`. **Refused: compile errors** ("Argument type mismatch: actual type is 'ScreenEdge?'" at :372, a lost smart cast). 2nd: `if (false && …)`. **Refused, same error** (the constant folds). 3rd: the landscape getter reads `state.isOnLeftSide` | Third edit: TurnTest "landscape at 270: the cluster defaults to the punch-hole side, the right"; S6 default 90, turn, port-side drag; S1 at 90's cluster assertion. |
| Rail slide keeping map size | (a) Slide reverted: `if (railPortEdge != null && !isFullscreen)` around the `AnimatedVisibility` (B1's instant cut) | 3 fail: S7 "the rail must be seen part-way toward the port edge … frames seen: [DpRect(left=743.0.dp …)]" (and 270, and leaving) |
| | (b) Map size broken: the map slot takes `padding(end = 80.dp)` while the rail shows | 5 fail: S7 "the map at 32ms: right expected:<743.0> but was:<823.0>" (at 16 ms leaving). S1/S2 follow from the narrower map (width expected 363.5). |

For the cluster-memory check, no results from the two refused runs were read. The failures cited
are the third run's, from a build log with no compile errors.

## Measurements and findings

These come from temporary probe tests that were not committed; the file was restored from a saved
copy and `git status` was clean after each.

- **HUD and search bar overlap by 1 dp.** At ROTATION_90 while navigating, the HUD is
  `DpRect(383, 0, 743, 124)` and the bar is `DpRect(0, 0, 384, 85)`. As predicted in the
  continuation's mechanism prediction (ii), they overlap by 1 dp at the top (823 − 80 − 360 = 383
  < 384). The HUD is composed later, so it draws over the bar's last dp. No S item asserts on this.
  It is a finding for the planner, not something the coder changed.
- **The HUD clears the central third by 4 dp.** The HUD is 124 dp tall and the central third
  starts at y=128. Prediction (iii) held, just. A taller HUD (a font scale, a longer status line)
  would reach the centre: a device item.
- **Robolectric text is too narrow to judge the strip's width.** The strip at 90 with a live fix
  is `DpRect(628, 0, 743, 36)`, 115 dp wide. The probe found each Text laid out at about 10 dp wide
  with `hasVisualOverflow=true` ("Compass unavailable" 20×36 px). The portrait strip, which B2 did
  not relayout, shows the same: "Compass unavailable" 20×36 px, overflow true, at xhdpi. So this is
  how text measures under Robolectric here, in both orientations, not a B2 regression (base text
  metrics not measured). The consequence: whether the real strip fits between the bar's end and
  the rail (359 dp at 90) is **device-only**, and the S4 "does not reach the search bar" assertion
  proves nothing about real font widths.
- **A green suite proves nothing about real insets.** Insets are zero under Robolectric (CLAUDE.md),
  so the punch-hole-side controls edge equals the window edge in every test. Where the bar, chip
  and cluster sit against a real cut-out is device-only.

## Device items (none run; deferred to beta)

From the intent's Note (4), with the findings above:

1. Search bar and chip placement at both rotations, against the real cut-out.
2. The dropdown clear of the rail, and the rail tappable while the dropdown is open.
3. The strip's steady width with tnum on the device font, and whether it fits between the bar and
   the rail at both rotations (above).
4. The HUD in the rail corner while navigating: its 1 dp overlap with the bar, and its height
   against the central third at the device's font scale.
5. Cluster memory across physical turns (90 to 270 and back; portrait to landscape and back).
6. The rail slide on entering and leaving fullscreen.
7. No control in the cut-out.

## Decisions I made

- **The no-fix weight.** The intent names the strip's `fillMaxWidth` (MCU:344) and inner-Row
  `weight(1f)` (MCU:392-395). The no-fix line's `weight(1f)` (MCU:376) is also dropped when
  content-width. Otherwise that one line stretches the strip back across the window and the
  intent's "content-width" would not hold with no fix.
- **S3 at 270.** "Aligned to the bar's start" was read as the bar's layout start, its left edge,
  at both rotations. At 270 the chip therefore sits at the bar's left end, not the punch-hole edge.
  The test covers 90 only, where the two readings coincide.
- **The S1 test.** The intent specifies no test of its own for S1. The coder wrote one that
  asserts placement on the edge `punchHoleEdgeFor` names, at both rotations.
- **The dropdown's top offset is unchanged in landscape.** It is still below the bar plus the
  strip's height, although the strip no longer sits there. The intent is silent on it.
- **The second map-size revert, check (b).** It is an injected fault, not a revert, because B2 has
  no line of its own that keeps the map's size (B1's structure does). Check (a) reverts the slide
  itself.
- **The cluster-memory revert edit.** After two refused edits, the third reverts the getter rather
  than the condition.
- **`turnTo`'s configuration flip** uses `navigationHidden`, which nothing on this screen reads.
- **Gradle settings.** `--max-workers=1` throughout, and a session-local pre-fetch init script.
- **Probes and crash files.** They were kept out of the repository.

## Flags outside scope

- `check_kit.py` fails on `.claude/agents/coder.md` against `kit.lock`, since `2fae413`.
- The cloud image lacks `gh` and the Android SDK, and its `GH_TOKEN` is invalid. Maven Central
  rate-limits a cold Gradle cache. Its OpenJDK 21.0.10 crashed once in C2 during the suite.
- The GitHub MCP tools were not in this session's tool list, so no PR was opened. The PR goes to a
  fresh coder, as ruled.
- The planner-log lines for the five relayed messages are for the local finish step to cite.
