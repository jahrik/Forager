# Dispatch 2026-09-28-358 (+ amendment -359): the planned-trip flag at 23 dp, the fan in 250 ms

Coder: Sonnet 5.5 (`claude-sonnet-5-5`), taken from the session's configuration, not checked against the running session.
Base: `origin/journal-redesign` at `9a854d48`, read from the remote at start; `git pull --no-rebase origin journal-redesign` before hand-back said "Already up to date". Branch `trip-flag-size`, worktree `/home/zynergy-labs/Zynergy/forager-wt/trip-flag-size`. Nothing merged.

## What landed (all pushed)
| Commit | What |
|---|---|
| `42c3fa06` | Flag tests first: size, anchor, fan offset at full and half progress; guard that FIND, WAYPOINT, PHOTO are unchanged |
| `a78b13ff` | Flag drawn 23 dp tall; doc comments updated |
| `4511c95e` | Fan-duration tests first (`MarkerFanOutHostTest` re-based 400 -> 250; fold-back part-way test added; `MapTapHandlerTest` comment) |
| `40b441cc` | `FAN_DURATION_MS` 400 -> 250, comment quotes the owner and says it replaces rule 6 |
| `c6a2d2b2` | `FanOutLayersTest.kt:79` re-based (see wrong premise below) |
| `9190d13a`, `89cbfe43`, `0bcca100` | Affected-class XML, revert XML and logs, full-suite XML under `docs/audits/data/2026-10-01-trip-flag-size/` (the affected-tip XML is in `9190d13a`'s parent); this report and the README row |

**Important:** nothing was built or run at the two tests-first commits, so they were never seen failing as pushed. The revert checks below are the evidence that the tests can fail.

## Verified premises (file:line at the base)
- Flag size: `PLANNED_TRIP(20f, 28f, 1.5f, 28f, scale = GLYPH_TARGET_HEIGHT_DP / 28f)`, `MarkerGlyphs.kt` enum (was about :59). Confirmed. `GLYPH_TARGET_HEIGHT_DP = 25f` and FIND reads it. Confirmed.
- Shape in `parts()`: pole `rect(0, 0, 3s, 28s)`, pennant `rect(3s, 0, 20s, 12s)`; both use the enum's `scale`, so no change was needed there. Confirmed.
- `fanCentringOffsetDp()` = anchor - extent/2; flag offset went (-7.59, 12.5) -> (-6.98, 11.5), as the planner expected.
- `FAN_DURATION_MS = 400` at `MarkerFanOut.kt:36`, one production reader `MarkerFanOutState.kt:81` (`tween(FAN_DURATION_MS, FastOutSlowInEasing)`), doc at :68. Confirmed. grep of `main/` for the constant, and for 400 near fan/duration/tween/delay/anim, found no other reader.
- **Hit boxes:** a grep of `main/` for `.widthDp/.heightDp/.anchorXDp/.anchorYDp` found them read only in `MarkerGlyphs.kt` (bitmap layout) and via `fanCentringOffsetDp` in `FanOutLayers.kt:213`. The fan's tap box is `FAN_TOUCH_DP` (48 dp) and the map queries rendered features. No tap target is computed from the flag's size in code. NOT verified: how the live map's point query treats a smaller image (device-only).

## Wrong premise (corrected, not a stop)
The dispatch said the flag's numbers are pinned in `MarkerGlyphsTest` and `FanOutLayersTest` at :206 and :219 "and no others". There was a third pin: `FanOutLayersTest.kt:79` (`"trip" to (-7.5893f to 12.5f)`, test "each copy carries the icon-offset that centres its glyph's body"). It failed on the first run (`the trip copy's icon-offset x expected:<-7.5893> but was:<-6.982143>`). I re-based it in the same way as the others: same test, same meaning, new number. I judged that within the dispatch's authorisation (a test pinning the flag's numbers); flagging it here in case the planner reads it otherwise.

## Mechanism and exact numbers
New constant `PLANNED_TRIP_HEIGHT_DP = 23f` in `MarkerGlyphs.kt`; `PLANNED_TRIP` uses `scale = PLANNED_TRIP_HEIGHT_DP / 28f`. Why: it is the same mechanism FIND already uses (a per-glyph scale from a height constant), it keeps the shared `GLYPH_TARGET_HEIGHT_DP` for FIND, and it adds no conditional to working code. Casing and detail strokes are not scaled.
- Size 16.43 x 23 (16.4286 x 23); anchor (1.23, 23) (1.2321, 23); fan offset (-6.98, 11.5) at full, (-3.49, 5.75) at half. Before: 17.86 x 25, (1.34, 25), (-7.59, 12.5).
- Corner clearance in the 36 dp circle (radius 18), farthest corner = s * sqrt(10^2 + 14^2) + 1.5 casing: before 16.86 dp -> **1.14 dp** left; after 15.63 dp -> **2.37 dp** left (pin: 2.5 at top and tip, from the dispatch, not re-derived). Computed by hand from the real scale, not measured on a device.
- Comments updated: "Flag and find size" note, enum entry, `GLYPH_TARGET_HEIGHT_DP` doc, `fanCentringOffsetDp` doc (old numbers kept as "as it was then"), `FanOutLayers.kt:173`. Glyph board not edited.

## Amendment -359: what each number was and is (`MarkerFanOutHostTest`)
- `OWNER_DURATION_MS` 400 -> 250. Constant test 400 -> 250 (name now "250 ms").
- "fan-out takes 0.4 s" -> 0.25 s: `ran` in 250..266 ms (was 400..416).
- "part way at 200 ms, not done a frame before 400" -> "part way at 125 ms, not done a frame before 250, and done at 250": advance 125 (was 200), then `OWNER_DURATION_MS - 125 - 2*FRAME_MS`, then three more frames and progress must be exactly 1. The "done" assertion is new.
- Fold-back "takes the same 0.4 s" -> 0.25 s; a new fold-back part-way/not-done/done test mirrors the fan-out one.
- `repeat(400)` in `framesToReach` is a 400-frame cap, not a duration; left as it was.
- `MapTapHandlerTest.kt:18` comment "in 400 ms" -> "in 250 ms". No test outside `MarkerFanOutHostTest` depends on the duration (grep of `app/src/test` and `androidTest`).

## Revert checks (each from a saved copy in /tmp, never from git; build log checked; forward change re-checked)
Predictions were given to the planner before the runs. Build logs: 0 `e:` lines each. After each, `git diff --stat` was empty and the constant read 23f / 250 again.
1. **Flag back at 25** (`PLANNED_TRIP_HEIGHT_DP = 25f`), `MarkerGlyphsTest` + `FanOutLayersTest`: 6 failures, all naming the flag, e.g. `PLANNED_TRIP width expected:<16.4286> but was:<17.857143>`, `the flag's height, was 25 expected:<23.0> but was:<25.0>`, `the trip copy's icon-offset x expected:<-6.9821> but was:<-7.589286>`, and the half-progress `-3.49107` vs `-3.794643`. The find/waypoint/photo guard and the find-25 test stayed green. Saved: `data/.../revert-flag-25/`.
2. **Duration back at 400**, `MarkerGlyphsTest` + `MarkerFanOutHostTest`: 5 failures, all `MarkerFanOutHostTest`: `expected:<250> but was:<400>`, `the fan-out ran 400 ms, not 250`, `the fold-back ran 400 ms, not 250`, and the two part-way tests at `done by about 266 ms` (progress 0.928 and 0.072). `MarkerGlyphsTest` 15/15 green. Saved: `data/.../revert-duration-400/`.
Not checked: that each flag test fails on a smaller change than a full revert; the revert is the one-line edit the dispatch asked for.

## Suites
- Affected classes at the tip (`MarkerGlyphsTest` 15, `FanOutLayersTest` 16, `MarkerFanOutHostTest` 11, `MapTapHandlerTest` 12): 54 tests, 0 failures. First run had 1 failure (the :79 pin above).
- **Full unit suite at the tip:** 406 classes, 3275 tests, 0 failures, 0 errors, 24 skipped (skips not examined; I added none). BUILD SUCCESSFUL, 4m11s; 0 `e:` lines. No flake, `DiagnosticsPanelTest` failure or stall occurred, so no thread dump. XML: `data/.../full-suite/`.
- `assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines.
- Run order note: the full suite ran at the tip after the revert checks, with the tree confirmed restored.

## Four disclosures
- **Confirmed vs inferred:** confirmed by reading and by test: sizes, anchors, offsets, duration, which tests fail on each revert. Inferred: corner clearances (arithmetic, not rendered); that no hit box depends on glyph size (code grep only).
- **Could not determine:** how the flag looks in its circle on a real display; whether 23 dp reads as "very slightly" smaller to the owner; the 24 skipped tests' reasons.
- **Premises that were wrong:** "no other tests pin the flag's numbers" (a third pin at `FanOutLayersTest.kt:79`).
- **Decided beyond scope:** re-basing `FanOutLayersTest.kt:79`; adding a fold-back timing test and a "done at 250" assertion (the amendment asked for both directions); updating the old offset quote in the `fanCentringOffsetDp` doc (it quotes the owner's -299 words, so I kept the old numbers and marked them as then-values).

## Device-only (owner)
1. The flag in its 36 dp circle, by day and by night: room at its corners (expect about 2.4 dp, about the pin's).
2. The flag beside the pin, the find and the photo on the map: sizes read as a family.
3. The pole foot sits on its true coordinate (anchor is now (1.23, 23)).
4. The fan opens and folds visibly quicker (250 ms).
5. No icon jump at the fold's last frame or the open's first (-299's check, at the new speed).
6. The open's first frames: -319 saw the stack gone and only the bare puck for three 60 fps frames at 400 ms; at 250 ms it is about two frames' worth of the whole animation, so look specifically.
