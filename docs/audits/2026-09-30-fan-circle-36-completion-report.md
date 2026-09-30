# Dispatch 2026-09-28-284, fan-circle-36: completion report

Base `bc9dc591` (verified: `git rev-parse bc9dc591` matches; `origin/journal-redesign` was `3f9c13b6`, which adds only the
dispatch file). Branch `fan-circle-36`, worktree `forager-wt/fan-circle-36`. No device, no adb.

## What landed

| Commit | What |
|---|---|
| `bce3a33c` | Tests first, pushed failing against 48: `FanClarityTest` re-based to 36 dp / 18 dp radius, with messages naming the diameter; one new case pins `FAN_TOUCH_DP` at 48. |
| `510121c6` | `FAN_CIRCLE_DIAMETER_DP` 48f to 36f (`FanClarity.kt:31` now); both doc comments updated, quoting the owner and naming this dispatch. Nothing else. |
| `48ce83c9` | `git pull --no-rebase` of `origin/journal-redesign` (`3f9c13b6`): no conflict, only `prompts/preserved/2026-09-30-08.md`. |

## Premises, verified (at `bc9dc591`)

- **The size is one constant: confirmed.** `FAN_CIRCLE_DIAMETER_DP = 48f` at `FanClarity.kt:25`; doc comments at `:24` and
  `:57` said 48 dp (the second read "48 dp across at 80%").
- **The touch area and spacing come from `FAN_TOUCH_DP`, not the circle: confirmed.** `MarkerFanOut.kt:21`, used at `:93`,
  `:101`, `:114`, `:130` (and `:144`).
- **Every reader of `FAN_CIRCLE_DIAMETER_DP`:** exactly two in `app/src`: `FanClarity.kt:60` (`fanCircleStyle`'s radius, half of it)
  and `FanClarityTest.kt:100`. The radius reaches only `PropertyFactory.circleRadius` (`FanOutLayers.kt:101`). **Nothing derives a
  tap target or the spacing from the circle size**, so the stop condition did not fire.
- **Tests naming 48 for the circle:** only `FanClarityTest` (`:93` name, `:96` radius 24, `:100`). No other test.

## Red, green, revert

- Red (`/tmp/fc36-red.log`): one failure, "the circle's radius is half of its 36 dp diameter expected:<18.0> but was:<24.0>".
  The touch-area case passed already: it is a control (`FAN_TOUCH_DP` is unchanged), not evidence the change bites.
- Green after the change: `FanClarityTest` 9 tests, 0 failures.
- Revert check, from a copy saved before editing (not from git): 48 put back, build log 0 `e:` lines, `compileDebugKotlin` and
  `compileDebugUnitTestKotlin` ran; `FanClarityTest` failed with the same message naming the diameter; forward file restored from
  a second copy, `git status` clean and `FAN_CIRCLE_DIAMETER_DP = 36f` present afterwards.

## Suite

`:app:testDebugUnitTest` at `48ce83c9` (nothing new on `origin/journal-redesign` since, still `3f9c13b6`): 388 classes,
**3168 tests, 0 failures, 0 errors, 24 skipped**, BUILD SUCCESSFUL in 4m43s (`/tmp/fc36-full.log`, run 09:14 to 09:19).
The parent line's last count was -279's 3167; this change adds one case (the touch-area pin), so 3168 is the expected figure.
`LeavingTheJournalFixesTest` ran all 31 cases, none failed or skipped, and the run did not stall (the results file kept
advancing until the end); `DiagnosticsPanelTest` and the owner-held flakes did not fail on this run.

**How it got here, disclosed.** The first attempt was held back by the memory gate: 2048 MB available was never reached from
08:44 to 09:13 (1213 to 1547 MB), because three idle daemons of other sessions held about 6.7 GB (PIDs 1283744, 1284065,
1294752). They were not touched by this session; the planner stopped them with the owner's "Yes, stop them", after which
7782 MB was available, 5268 MB of disk was free and no wrapper or worker ran, and the suite was run. One full-suite launch had
been started at 1547 MB by mistake (a chained command ran past the gate check); its wrapper (PID 1297960, started by this
session) was stopped within seconds, its log discarded, and it produced no results.

## Device-only list (S22-B)

1. A 9-member fan by day and by night: the 36 dp circles do not crowd each other.
2. Every glyph (pin, flag, find, camera) inside its circle with its outline unclipped (the pin and flag, about 31 dp with casing,
   are the tight ones: about 2.5 dp around).
3. The touch area is unchanged: a finger 48 dp from a member's centre still hits that member, and a tap in the gap between
   two circles still does what it did (the gap is now wider than the circles' own).
