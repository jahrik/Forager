# The coordinate format survives a tab change (dispatch 2026-09-28-422, plan task T18)

**Status: built and pushed on `coordinate-format`, not merged.** No phone, as the dispatch says.

**Date:** 2026-10-03 (UTC).
**Base:** `origin/main`. The branch was cut at `5b856b6a` with the test alone. `main` (`f1b53aca`, with -423) was merged in with `git pull --no-rebase` before the test was run, so the failing run and the fix are both on current `main`.
**The owner's go,** to this session directly: "Yes, build it".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-coordinate-format/`.

## What this is, in plain terms

Tapping the coordinates on the map switches them between MGRS and decimal degrees. Until now the choice went back to MGRS whenever you left the Maps tab and came back. Now it stays as you set it for as long as the app is open. A restart still starts on MGRS: keeping it across restarts was not asked for.

## What was wrong, read before fixing

- **Where the choice was held:** `ui/availability/AvailabilityCompactMapUi.kt:532` held it as `var showDecimalDegrees by remember { mutableStateOf(false) }`, inside `CompactMapTab`.
- **Why it reset:** `CompactMapTab` is composed only while the Maps tab is selected (`AvailabilityCompactScaffold.kt:913`, `CompactTab.MAP ->`), so changing tab takes it out of composition, and a `remember` goes with it.
- **Who reads it:** the compass strip (`:778-779`) and the HUD (`:886-887`). The comment beside it said so: "Still resets when this tab unmounts, as it did before."

## The fix, and why it survives

The choice is now held in `AvailabilityScreen` with `rememberSaveable`, next to the map legend's flag, which is held there for the same reason (`AvailabilityScreen.kt`, "held here, above the tab, so it survives leaving the Maps tab and coming back"). It reaches the Maps tab through `AvailabilityCompactScaffold`, and the strip and the HUD read it as before.

**Why it survives:** `AvailabilityScreen` stays composed across a tab change; only the tab's own content is swapped. State held there is not dropped when the Maps tab leaves.

**Why not `rememberSaveable` inside the tab:**
- `rememberSaveable` survives a configuration change or process recreation through the saved-state registry, but not the composable itself leaving composition. A tab that is swapped out discards its saveable state unless a `SaveableStateHolder` keeps it, and the scaffold uses none.
- It is `rememberSaveable` rather than `remember` above the tab so that a rotation keeps it too, as the legend's flag does.

**Not touched:** the formatting itself, MGRS conversion, and every other control.

## Evidence

- **The test, through the real entry points:** `CoordinateFormatTabRoundTripTest`, at the S22's portrait size.
  - It uses real touches: on the strip's coordinates, then the bottom nav's "List", then "Maps".
  - It reads the strip's text before and after.
  - It also asserts that the strip really left composition with the tab, so the test reaches the case that failed.
- **Pushed before the fix** (`91b1735c`), and run on current `main` before the fix (`f1-test-first`): it failed with "decimal degrees after the round trip expected:<1> but was:<0>". Its three preconditions passed (MGRS at first, decimal after the touch, the strip gone on List), so it failed for the reason the bug gives.
- **With the fix** (`f2-fixed`): it passes, alongside `AvailabilityScreenMapIconStackTest`, whose existing test "a coordinate format chosen in the HUD is the format the strip shows after leaving navigation" still holds. 2 classes, 105 tests, 0 failures, 19 skipped (that class's existing `@Ignore`s).
- **Revert check** (`r1-reset-on-tab-change`, by `revert.sh`: from a saved copy, the build log read first, the tree confirmed identical to HEAD `41f43b9a` after):
  - The one edit resets the format whenever the tab changes, which is the old behaviour in one line.
  - It compiled with 0 errors and failed with the same message as the run before the fix.
- **Full suite:** on `41f43b9a` from a cleared results directory (`f3-full-suite`), 424 classes, 3485 tests, 0 failures, 0 errors, 24 skipped. Reconciled: `main`'s 3,484 tests in 423 classes (way-back route Part 2's suite) plus this one test in one new class; the 24 skipped as before.

Nothing skipped, silenced or weakened. No existing test was edited.
