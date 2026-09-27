# 2026-09-27: Landscape B3, drawer (P12): completion report

Dispatch: `prompts/preserved/2026-09-27-13.md` (the drawer coder). Scope: P12 as
corrected by R2 (`docs/plans/landscape-phone-design.md`), with the owner's
rulings in `docs/audits/2026-09-27-landscape-b3-prebuild-report.md` section 5.
Branch `landscape-b3`. Base `9784367` (the dispatch names `acba675`; `9784367`
is `acba675` plus docs-only commits to `RECORD.md`, the pre-build report and
`prompts/preserved/`, checked with `git diff --stat acba675 9784367`).

**Two things need a decision** (section 7): the isDrawerOpen sync added beyond
the dispatch's one-line D1, and the RTL reading of D2.

## 1. Commits

| SHA | What |
|---|---|
| `177a2ddad676928c4a6f67ea76c85081a36c6ddb` | `CompactToolsDrawerTest`, tests first, failing at base |
| `2b9897f` (merge) | `git pull --no-rebase`; brought in P11's `82bf5f1`, `b268c75` (only `AvailabilityCompactScaffold.kt`, `AvailabilityResultsUi.kt`, their test) |
| `96c05179c4d1ee1c4d552e46b9377acf8111f6e6` | D1 and D2, and the comments D1 made false |
| `62909f2` then merge `064248c` | Test comment only; merge brought in P11's report (`docs/` only) |
| this report's commit | this file |

Files changed by this dispatch: `AvailabilityScreen.kt`, `AvailabilitySettingsUi.kt`
(comment only), `CompactToolsDrawerTest.kt` (new), this report. Nothing else.

## 2. Premises checked before building

- **Line numbers.** `AvailabilityScreen.kt:1435-1441` was the
  `ModalNavigationDrawer` with `gesturesEnabled = false` at `:1441` and the
  comment at `:1437-1440`; `currentWindowPortEdge()` at `:961`; the close-bar
  comment at `:1028-1030`; the `DrawerHeader` doc at
  `AvailabilitySettingsUi.kt:100`. All as the dispatch said.
- **M3 behaviour.** Read from the `material3-android-1.5.0-alpha26` sources jar
  (dl.google.com, `commonMain/androidx/compose/material3/NavigationDrawer.kt`):
  `onDismissRequest` closes only `if (gesturesEnabled && ...)` (`:384-388`),
  the drag is `anchoredDraggable(..., enabled = gesturesEnabled, reverseDirection = isRtl)`
  (`:373-381`), the sheet is placed with `Modifier.offset {}` (layout-direction
  aware) at the start edge. Matches the pre-build report.
- **Helpers.** `isShortWindow()` (`ui/adaptive/ShortWindow.kt:25`) and R15's
  orientation test are already combined as `isShortLandscapeWindow`
  (`AvailabilityScreen.kt:959-960` at base); `portEdge` at `:961`. Reused, nothing new.
- **A premise that was incomplete: "D1 is a one-line change."** The scrim calls
  `drawerState.close()` itself. The screen's authority for "should the drawer
  be open" is `isDrawerOpen` (`AvailabilityScreen.kt:800-820` at base), which only the
  app's own callers change. See section 3, D1.

## 3. What was built

**D1.** `gesturesEnabled = drawerState.isOpen`. Seen on its own (the one-line
change and nothing else, log `d1only`): the first scrim tap closed the sheet,
and the next Tools tap did not reopen it, in all three scrim tests: "the
drawer opens from Tools (sheet DpRect(left=Dp.Unspecified, ...))". The sheet
was visibly shut with `isDrawerOpen` still true, so Tools set a flag that was
already true and Back would still route to the close-drawer handler
(`BackHandler(enabled = isDrawerOpen)`, `:871` after this change). So a `LaunchedEffect(drawerState)`
in the drawer block now watches
`drawerState.currentValue == Closed && !drawerState.isAnimationRunning` and
sets `isDrawerOpen = false` when that becomes true while the flag is true.
Only the change is acted on, so a just-requested open (flag true, drawer still
closed and idle) emits nothing. This also covers M3's other self-closes
(swipe-to-close, the semantics `dismiss` action, Escape), which were already
out of sync before B3 but unreachable with gestures off (inferred from the M3
source; only the scrim path is tested).

**D2.** Around the `ModalNavigationDrawer`, in a short landscape window,
`LocalLayoutDirection` is the direction whose start edge is the port edge
(Right -> `Rtl`, Left -> `Ltr`); elsewhere the ambient direction. The ambient
direction is restored inside `ModalDrawerSheet` (around
`CompactToolsDrawerContent`) and around `compactMainScaffold`. The restore is
inside the sheet, not around it, so the sheet's own `isRtl` reads (rounded
edge, start-side inset padding, predictive-back transform origin in
`DrawerSheet`, M3 `:857-898`) follow the edge the sheet is actually on. For
the app's LTR locale this is exactly the dispatch's "flip at 90, unchanged at
270". It differs from the dispatch's wording in RTL; see section 7.

**Comments.** The `gesturesEnabled` comment now says why the flag is
`isOpen` and what alpha26 gates on it; the close-bar comment (`:1031-1033`)
and `DrawerHeader`'s doc (`AvailabilitySettingsUi.kt:100-103`) no longer say
gestures are off. The close effect's "scrim tap" (`:836`) was left: with the
sync it is now true.

**Stop condition checked.** The flip did not mirror the drawer content or the
map once restored: T4's content-direction assertion passes, and
`AvailabilityScreenShortLandscapeTest` 11/0 and `AvailabilityScreenLandscapeB2Test`
26/0 pass with the flip in place (both drive `ROTATION_90`, where it applies).
R-e below shows those two classes do catch a missing content restore.

## 4. Tests, seen failing at base then passing

`CompactToolsDrawerTest`, 13 tests. Per-method `@Config` qualifiers: portrait
`w384dp-h823dp-port` (the S22 Ultra portrait window; at 360 dp the 360 dp sheet
would leave no scrim to tap), `w823dp-h384dp-land`, and RTL variants
`ar-ldrtl-...`. Every rotation test asserts the screen saw the rotation. Scrim
taps are `performTouchInput` clicks at 3 x 3 points across the scrim region
beside the sheet, reopening via Tools before each; drags are `swipe` from 2 dp
inside each edge, 300 dp, at 60% height.

At base `9784367` (log `base`, compile clean): 13 tests, 6 failures.

| Test | Base | Reason at base | After (`96c0517`) |
|---|---|---|---|
| T1 portrait scrim | FAIL | "a scrim tap at (364.0.dp, 4.0.dp): the sheet has left the window, but it is at (0..360)" | PASS |
| T2 landscape 90 scrim | FAIL | same, sheet at 0..360 | PASS |
| T2 landscape 270 scrim | FAIL | same | PASS |
| T3 portrait drags | pass | by design | PASS |
| T3 landscape 90 drags | pass | by design | PASS |
| T3 landscape 270 drags | pass | by design | PASS |
| T4 landscape 90 sheet right, reads LTR | FAIL | "touches the window's right edge expected:<823.0> but was:<360.0>" | PASS |
| T4 landscape 270 sheet left, reads LTR | pass | no flip needed at 270 | PASS |
| T5 portrait sheet left, reads LTR | pass | by design | PASS |
| 90: rail still right after a scrim close | FAIL | scrim tap did not close | PASS |
| RTL landscape 90 sheet right, reads RTL | pass | ambient RTL already starts right | PASS |
| RTL landscape 270 sheet left, reads RTL | FAIL | "touches the window's left edge expected:<0.0> but was:<463.0>" | PASS |
| RTL portrait sheet right, reads RTL | pass | by design | PASS |

The last four are beyond the dispatch's T1-T5. The RTL ones test the section 7
reading and should be changed with it if the ruling goes the other way.

## 5. Revert checks

Runner: `run.sh` restores `AvailabilityScreen.kt` from a copy saved before the
first edit (sha1 `d0bd68bc...`), never from git; after every revert the sha1
matched the copy again and `git status` was clean. Each log checked for `^e: `
lines first: **0 compile errors in every run.** Every failure message below
names a state the base run could not have produced (for example, sheet at
463..823 under R-a, which needs the flip), so none is a stale result.

| Revert | Edit | Predicted | Observed |
|---|---|---|---|
| R-a | `gesturesEnabled = false` | T1, T2 fail | 4 fail: T1, T2 at 90 and 270 ("the sheet has left the window, but it is at (463..823)" at 90, (0..360) portrait and 270), and the 90 rail-after-close test (same cause). Matches. |
| R-b | `gesturesEnabled = true` | T3 fails | 3 fail, all three T3: portrait and 270 "a horizontal drag ... from the left edge", 90 "... from the right edge" (the flipped side). Matches. |
| R-c | flip removed (`if (false)`) | T4 at 90 fails | 3 fail: T4 at 90 ("expected:<823.0> but was:<360.0>"), RTL 270 ("expected:<0.0> but was:<463.0>"), and the 90 rail-after-close test, whose tap at x = 40 dp lands on the left-edge sheet. Matches; the last two were predicted before the run, but that prediction is not recorded outside this session. |
| R-d | no restore inside the sheet | T4 content assertion fails | 2 fail: T4 at 90 "left to right: the label sits in the row's left half (right gap 48.0.dp > left gap 303.0.dp)", RTL 270 the mirror. Matches. |
| R-e (extra) | no restore around the screen content | the map-side tests at 90 fail | 14 fail, all at `ROTATION_90`: `ShortLandscapeTest` 4 (rail at 0..80 not the right), `LandscapeB2Test` 9 (search bar, cluster, HUD, strip mirrored), `CompactToolsDrawerTest` 1 (rail's Tools at 0..80). Matches. |
| R-s (extra) | sync removed (`if (false && ...)`) | T1, T2 fail on the reopen | 3 fail: T1, T2 at 90 and 270, "the drawer opens from Tools (sheet ... Dp.Unspecified ...)". `BackNavigationTest` 27/0: no existing test would have caught the desync. Matches. |

One weakness found by R-d: of the two content-direction assertions, "room for
the leading icon before the label" passed under R-d (a 303 dp gap is also at
least 40 dp); only "the label sits in the start half" discriminates.

## 6. Counts

Affected classes, before (setup dispatch, at `9784367`) and after (full suite at `064248c`):

| Class | Before | After |
|---|---|---|
| `CompactToolsDrawerTest` | did not exist | 13/0/0/0 |
| `AvailabilityScreenShortLandscapeTest` | 11/0/0/0 | 11/0/0/0 |
| `AvailabilityScreenLandscapeB2Test` | 26/0/0/0 | 26/0/0/0 |
| `AvailabilityScreenBackNavigationTest` | not recorded by setup | 27/0/0/0 |

Full unit suite at `064248cac6363e664f257b8fceb4454d25386353` (includes P11's
work): **226 classes, 1782 tests, 0 failures, 0 errors, 24 skipped**, 143 s,
compile clean. Against the setup baseline (224 / 1757 / 24 skipped):
+2 classes, +25 tests = 13 here + 12 in P11's
`AvailabilityScreenLandscapeB3DestinationsTest`. No existing test changed
(prediction 3 held). Prediction 4 ("8 to 16 tests") held at 13.

## 7. Decided beyond the dispatch (needs the planner)

1. **The isDrawerOpen sync (D1).** The dispatch predicted a one-line change.
   The one line alone closes the sheet but leaves the drawer unable to reopen
   (section 3, seen in the `d1only` run and again under R-s). I added a
   settled-closed watcher in the drawer block rather than stop, because
   without it ruling 1 ships a drawer that jams after its first scrim tap. The
   alternative is the owner's call: for example, a custom scrim that sets
   `isDrawerOpen = false` itself, which would leave `gesturesEnabled = false`
   but departs from the ruling. Please confirm or overrule.
2. **RTL reading of D2.** The dispatch says "provide the opposite of the
   ambient direction" when the port edge is right, and leaves 270 unchanged.
   In an RTL locale (reachable: `android:supportsRtl="true"`,
   `AndroidManifest.xml:137`, and the system locale sets the direction even
   with no translations) that puts the drawer on the left at 90 and on the
   right at 270, away from the rail both times, and at 90 it moves the drawer
   off the rail side it already had. I implemented the physical mapping
   (port right -> `Rtl`, port left -> `Ltr`, ambient restored inside), which
   meets P12's "opens from the rail side" in both locales and is identical to
   the dispatch for LTR. If the literal reading is wanted, it is one
   expression (`drawerDirection`) and the two RTL landscape tests.
3. **Four extra tests** (section 4) and two extra revert checks (R-e, R-s).
4. **Portrait window for the tests** is `w384dp-h823dp-port`, not the
   `w360dp-h640dp` most availability tests use: at 360 dp the sheet fills the
   window and there is no scrim to tap.

## 8. Device-only (for B4)

Robolectric reports zero insets and runs the drawer animation on the test
clock, so none of this is shown here:

- The sheet on the right at `ROTATION_90`: its start-side inset padding should
  now land on the right, clear of the 3-button bar there, and its rounded edge
  face the map. Inferred from `DrawerSheet`'s `isRtl` reads, not run.
- Scrim tap closing in both orientations, and the drawer reopening afterwards.
- Swipe-to-close while open, and no swipe-to-open while closed, on a real map
  that pans under a horizontal drag.
- Rotating between 90 and 270 with the drawer open (the direction around it
  changes while open).
- Predictive back on the flipped sheet (the sheet uses the no-state
  `ModalDrawerSheet` overload, so none is expected; unverified).

## 9. Flags outside scope

- **I ran the other coder's runner script by mistake.** A shared scratchpad
  already held a `run.sh` belonging to the P11 coder (it `cd`s into
  `agent-ac538866c2d202e98`). My own heredoc writing a `run.sh` there was
  refused by the sandbox, and the next command executed theirs with the filter
  `CompactToolsDrawerTest`, which does not exist in their tree. It deleted
  their `app/build/test-results/testDebugUnitTest` directory and overwrote
  their `scratchpad/base.log` (at about 08:00). The Gradle build failed with
  no tests run, so no source or git state of theirs was touched. If they cited
  `base.log` or those XML files after 08:00, they should re-run. My own
  scripts live in `scratchpad/drawer/`.
- The `Dp.Unspecified` sheet bounds seen after a close suggest M3 stops
  placing the drawer's layout once closed; the test counts an unplaced sheet as
  off screen deliberately (comment on `sheetIsOnScreen`). Not investigated
  further.
