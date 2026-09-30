# Dispatch 2026-09-28-279, fan-restyle: completion report

Base `2efc2163` (verified: it is the parent of `origin/journal-redesign` at `d2e23b3e`, which holds the dispatch,
`prompts/preserved/2026-09-30-06.md`). Branch `fan-restyle`, worktree `forager-wt/fan-restyle`. No device, no adb.
The session was first opened in the wrong repository (`zynergy-site`); it stopped, and the planner [f2eaef] gave the
Forager path. Nothing was changed in `zynergy-site`.

## What landed

| Commit | What |
|---|---|
| `ac1cc9bb` | Tests first, pushed failing: case 6 rewritten to the owner's ruling, cases 6b to 6e added. |
| `eb9c32a6` | The change. `FanReopenCoordinator.onContentEffect` always calls `MapTapHandler.onContentChanged`; the `lastStyle` / `NO_STYLE_YET` bookkeeping is gone. `MapTapHandler.onStyleChanged` had one caller and is removed. Two stale comments corrected (`MapTapHandler` class doc, the effect's comment in `SightingsMap.kt`). |
| `ec2599df` | Assertion messages on 6b and 6e, so a revert fails with a message of its own (bare `AssertionError` before). |
| `74537fb4` | `git pull --no-rebase` of `origin/journal-redesign` (`d2e23b3e`): no conflict; it brought only the dispatch's own `RECORD.md` and `prompts/preserved/2026-09-30-06.md`. |

Nothing was added to redraw the fan: no re-push, no new path. Re-fanning goes through `openFanFor` (via
`onContentChanged`), as the dispatch requires. `SightingsMap.kt` changed only in a comment.

## Premises, verified (read at `2efc2163`)

- **Where a new style folds the fan: confirmed.** `FanReopen.kt:25-30` compared the style to `lastStyle` and routed a
  replacement to `MapTapHandler.onStyleChanged()` (`MapTapHandler.kt:141`, `= fan.fold()`). Its only caller was that line.
- **The fan's layers come from existing paths: confirmed, nothing missing.**
  - `addFanOutLayers` (`FanOutLayers.kt:72`) is called from `initializeOverlayLayers` (`SightingsMap.kt:1086`), which the
    `setStyle` callback runs (`:696`) before `loadedStyle = style` (`:715`).
  - The fan-draw effect (`:790`, keyed on `loadedStyle`) restarts on a new style; `hiddenFor` starts `null`, so it hides the
    originals again and `pushFanFrame` pushes the frame at once (`snapshotFlow` emits its current value).
  - The circle colour is applied at layer creation from `currentChromeColour` (`:702`) and by the `chromeColour` effect (`:827`).
  - The fade effect (`:818`, keyed on `loadedStyle`) re-applies `fanFadedPaint` for the current `fanOut.isOpen`.
- **"The members are all still drawn" does not depend on the new style having rendered: confirmed.**
  `MapLibreProbe.markersOf` (`FanOutLayers.kt:287`) reads the record lists through `locate` and only projects them; it does not
  call `queryRenderedFeatures`. So `onContentChanged` gives the same answer on a just-loaded style. This was the risk that
  could have needed more than a re-push, and it is not there.
- **The return path (8c) is unaffected: confirmed by test.** `lastStyle` started at `NO_STYLE_YET`, so the first load always
  counted as "replaced" and called `fold()` on a fan that was not yet open. Removing that is a no-op for 8c (6d, 6e).

## Every cause of a style reload found

`SightingsMap.kt:686` (the one `setStyle` call) runs when the effect at `:655`, keyed `mapLibreMap, basemap, useOfflineTiles,
nightMode, nightModeLoaded`, finds `needsStyleReload(appliedStyle, requested)` (`BasemapStyles.kt:214`). `AppliedMapStyle`
(`BasemapStyles.kt:152`) has four fields, so there are four causes:
1. `basemap` changed.
2. `palette` changed: `MapPalette.forMode` of the raw Night Maps toggle, so the toggle reloads on every basemap, Satellite included.
3. `useOfflineTiles` changed.
4. `night` changed: `effectiveNight`, the basemap's own night paint (false on Satellite, so it is not a separate cause there).

Also: the first style of a new `MapView` (a tab switch and back). That is a new composition with a fresh
`FanReopenCoordinator`, so it is the 8c path, not a reload of an open fan. A style that fails to load never reaches the callback.
The rule covers all of them because it is one path; the tests use a new style object, not one of these four by name.

## Tests

- Case 6 (was "a new style still folds"): an open fan survives a replaced style, members all drawn, same `generation`.
  The owner's ruling is quoted in the comment above it.
- 6b: a replaced style that also removes a member re-fans the survivors. 6c: fewer than two folds. 6d: the 8c return after
  Back still reopens. 6e: the return after a delete reopens the survivors, and a night-mode reload then keeps them.
- Red before the change (`/tmp/fan-restyle-red3.log`): 6, 6b, 6e failed; **6c and 6d passed already** and are controls for
  the change, not evidence that it bites. 6d first failed for a wrong setup (the keys were not armed with `onFindClosed`); fixed
  before the red run was pushed.
- Not covered: the `LaunchedEffect`s that redraw the fan on the new style. They are Compose plus native MapLibre, device-only.

## Revert checks

Saved copies of the pre-change `FanReopen.kt` and `MapTapHandler.kt` were put back over the forward files (not from git), and
the forward files restored from a second saved copy afterwards.
- Build log checked before results: 0 `e:` lines; `compileDebugKotlin` and `compileDebugUnitTestKotlin` both ran.
- Reverted, run 2 (after `ec2599df`): 6, 6b and 6e fail with their own messages:
  "a replaced style is not a reason to fold", "a replaced style that removed a member re-fans the survivors, it does not fold",
  "the reopened survivors stay open through a night-mode style reload". 6c, 6d and the 4 `FanReopenCoordinatorTest` cases pass.
  Run 1 (before the messages were named) failed the same three; 6b and 6e then had no message, which is why `ec2599df` exists.
- Forward change confirmed present after each restore (`git status` clean, no `onStyleChanged` left in either file).
- "Drop the re-push": not applicable, the change adds none.

## Suite

`:app:testDebugUnitTest` at `74537fb4`: 388 classes, **3167 tests, 0 failures, 0 errors, 24 skipped**
(`/tmp/fan-restyle-full2.log`, BUILD SUCCESSFUL, 4m06s). The planner's `2efc2163` count was 3163/0/0/24; the difference is my 4 new cases.
The owner-held flakes, `DiagnosticsPanelTest` and `LeavingTheJournalFixesTest` all passed on this run; none failed.

**A first full run was killed and is discarded.** The planner stopped its Gradle daemon and test worker (they looked idle),
and the run ended "Gradle build daemon disappeared". Its results are not cited. Its result files show it had **already stalled**:
`in-progress-results-generic.bin` last changed 07:26:58, 2.7 minutes into a run that was killed after about 40. The last
recorded test was in `LeavingTheJournalFixesTest`; the name at the file's end is cut off and matches, by prefix, only
`F3 a bubble's Open in Journal over a changed kept find leaves it first, ...` (`LeavingTheJournalFixesTest.kt:1128`), so that
is a **candidate for the hung test, inferred, not confirmed**; no thread dump was taken. The second run finished in 4 minutes with
that class green, so the stall did **not** reproduce. Another session's build was running on the machine at the same time, so
contention is possible; it is unmeasured. This is a possible new intermittent for record -278, not a finding about -279.

## Device-only list (S22-B, afterwards)

1. An open fan through a night-mode switch, day to night and night to day: the fan stays open, the circles are recoloured
   to the new theme, the icon fade (80%) is on, the frame is drawn.
2. Through a basemap change (e.g. OpenTopoMap to Satellite and back).
3. Through the offline-tiles switch (cause 3), and any other reload cause (Satellite's toggle reloads via the palette only).
4. A reload with a member removed in between (e.g. a find deleted, then a reload): re-fans the survivors; with fewer than two, folds.
5. 8c after the change: Back from a find opened from a fan reopens the same fan; and the deleted-member return.
6. One thing a green suite cannot show: whether the fan's frame is visible on the new style at the first frame, or only after a
   moment. Robolectric cannot build a `MapView`.

## Decided beyond scope

Removed `MapTapHandler.onStyleChanged` (no remaining caller). The `style` parameter of `onContentEffect` is now unused; it
stays so the map's call site and the tests can still say "a replaced style", with `@Suppress("UNUSED_PARAMETER")`. A
follow-up may drop it if the owner prefers.
