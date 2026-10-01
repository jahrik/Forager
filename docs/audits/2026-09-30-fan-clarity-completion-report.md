# Fan clarity (dispatch 2026-09-28-265, preserved as prompts/preserved/2026-09-29-58.md): completion report

Status: **complete except the device-only list.** The pre-registration is unchanged; the results follow it, from "Results" down.

Model: claude-sonnet-5-5 (read from the session's system prompt, not assumed). Base: `05c88878`, confirmed equal to
`origin/journal-redesign` at start. Branch `fan-clarity`, worktree `/home/zynergy-labs/Zynergy/forager-wt/fan-clarity`.

## Premises checked at base (read, not assumed)

- `FAN_STACK_DP = 32f` at `ui/map/fanout/MarkerFanOut.kt:30`; used by `stackOf` (`:71-72`, strict `<` on both axes).
  `FAN_TOUCH_DP = 48f` at `:19`. The dispatch's "about :30" holds.
- Fan layers: `FanOutLayers.kt:47-56` (ids), `:67-88` (`addFanOutLayers`; halos symbol layer at `:85`, then dots, then icons),
  `:96-101` (`FanFrame` with `halos`), `:119` (`fanFrameCollections`, halos built at the `haloImageFor` call). The dispatch's
  "`FanOutLayers.kt` about :36-37" for the above-every-registry-layer rule is wrong by line: that rule is the doc comment at `:36-37`
  region and the call order in `SightingsMap.kt:1051`; not a defect.
- Marker opacity has **two writers only**: `initializeOverlayLayers` (`SightingsMap.kt:1047`, at style load) and the effect at
  `SightingsMap.kt:793-796` (keyed on `loadedStyle, drawnLayersState`). Nothing else sets `iconOpacity`/`circleOpacity` on registry
  layers (grep of `applyLayerPaint`, `paintProperties`, `iconOpacity`, `circleOpacity` over `main/`). So the fade has nothing to fight
  if it is composed into that one effect rather than written beside it. No stop-and-report for item 2.
- The Layers sheet's opacity is a multiplier (`MapLayerState.kt:4-13`); no marker layer has `userOpacity = true`
  (`MapLayers.kt` `marker(...)` sets `userOpacity = false`; the sighting spec likewise), so at base marker layers always draw at their
  base. The fade is still written as a multiplier on the resolved paint so it stays correct if that changes.
- Chrome colour: `navigationBarContainerColor()` = `MaterialTheme.colorScheme.surfaceContainer` (`theme/NavigationBarColor.kt:24-26`),
  `#F4EFE2` light / `#202020` dark (`theme/Color.kt:135,141`); 80% is `MAP_CHROME_OVER_MAP_ALPHA` (`MapChrome.kt:267`).

## Predictions and pass conditions (for the failing-tests commit, built on stubs that return the pre-change behaviour)

The tests-first commit adds `ui/map/FanClarity.kt` as stubs (fade = identity, circle style = radius 0/opacity 1) and an empty
`FanFrame.circles`, so the tests compile and fail on assertions.

| Test | Expected at stub commit | Why |
|---|---|---|
| `MapTapHandlerStackDistanceTest` 26 dp vertical and horizontal, 30 dp pair, constant 26 | FAIL (4) | distance still 32 (`MarkerFanOut.kt:30`) |
| same class, 25 dp vertical and horizontal, 20 dp | PASS | 25 < 32 |
| `MarkerFanOutGeometryTest` new `just under the stacking distance ...` | FAIL | asserts `FAN_STACK_DP == 26` |
| `MarkerFanOutGeometryTest` rebased chain/25 dp cases, `MapTapHandlerPlacementTest` rebased | PASS | inside 32 as well as 26; they are re-basing, not new claims |
| `AvailabilityScreenFanBubbleDismissalTest` 30 dp and 26 dp real touch | FAIL (2) | they fan at 32 |
| same, 25 dp real touch | PASS | |
| `FanClarityTest` layer-set, 80 percent, sighting, user-opacity-multiplied, circle style | FAIL | stubs |
| `FanClarityTest` `a folded fan gives back ...`, `a layer that does not fade ...`, `no line, fill or colour field fades` | PASS at stub | **guards, not bites**: the stub is the identity, which is also the correct answer for these; each is flagged as passing before and after |
| `FanOutLayersTest` circle tests (three) | FAIL | `circles` is empty at stub |

Revert checks planned (saved copies, build log checked first): (a) `FAN_STACK_DP` back to 32; (b) `fadedWhileFanned` to `false`;
(c) drop `* FAN_FADE_OPACITY`; (d) circle diameter to 32 dp; (e) drop circle features from `fanFrameCollections`.

Device-only (cannot be reached here): see the list at the end of the final report.

---

# Results (continuation 2026-09-28-271)

Model: claude-sonnet-5-5 (from the session's system prompt). Head verified at start: `origin/fan-clarity` = `95b36d76`.
Machine gates before each build: at least 2048 MB available (lowest seen 2233 MB, other coders building), 8.3 GB free disk, no
Gradle wrapper of mine running. One idle Kotlin compile daemon (pid 1165913, from Sep 29 21:31) was not mine and was left alone.
Gradle started its own daemon ("1 busy Daemon could not be reused"). No `--stop`, no kills.

## What landed

| Commit | What |
|---|---|
| `c2cd00d2` | Tests first, stubs returning pre-change behaviour (previous coder) |
| `95b36d76` | Implementation, committed unbuilt by the previous coder; **built and run here, unchanged** |
| merge commit of `origin/journal-redesign` `4c52a1cc` | Docs only (RECORD.md, three prompts); `git diff 95b36d76 HEAD -- app` is empty |

No source edit was needed: `95b36d76` compiled first time (0 `e:` lines) and every targeted test passed. The report edit and the merge are the
only commits of this continuation.

Departures from the pre-registration: none in the checks. One note it did not carry: `95b36d76` deleted two `FanOutLayersTest` cases
(`a halo goes under a copy whose record an entry keeps, and only that one`; `with no entry shown there is no halo`), so the class has
10 tests, not the 12 at the stub commit. They tested the halo, which the circle replaces (owner: "Circle replaces halo").

## Stub commit `c2cd00d2`: predictions against results

Built in place by checking out `c2cd00d2`'s `app/src` (tree clean beforehand, `95b36d76` copy saved to /tmp), then restoring `95b36d76`'s
`app/src` and confirming an empty diff. 0 compile errors. Six classes, **58 tests, 15 failed**.

- FAIL as predicted (15): `MapTapHandlerStackDistanceTest` 26 dp vertical, 26 dp horizontal, 30 dp pair, constant 26 (4);
  `MarkerFanOutGeometryTest` just-under-the-distance (1); `AvailabilityScreenFanBubbleDismissalTest` 26 dp and 30 dp real touch (2);
  `FanClarityTest` layer set, 80 percent, sighting fill and ring, user opacity multiplied, circle style (5);
  `FanOutLayersTest` three circle tests (3).
- PASS as predicted (43): the 25 dp and 20 dp cases, the rebased chain and placement cases, the 25 dp real touch, and the three
  `FanClarityTest` guards (`a folded fan gives back ...`, `a layer that does not fade ...`, `no line, fill or colour field fades`).
  **Those three are guards, not bites**: the identity stub is also the right answer for them, so they pass before and after.
- Prediction table: **exact match, no differences.**

## Forward build `95b36d76`

`BUILD SUCCESSFUL`, 0 compile errors, same six classes: 56 tests, 0 failures.

## Revert checks (each from the saved copy, edit applied by `sed`, edit confirmed applied, 0 compile errors, file restored and `git diff 95b36d76 -- app/src` empty after each)

| | Edit | Failed | Specific to the edit? |
|---|---|---|---|
| (a) | `FAN_STACK_DP` 26 back to 32 | 7: 4 `MapTapHandlerStackDistanceTest`, 1 `MarkerFanOutGeometryTest`, 2 `AvailabilityScreenFanBubbleDismissalTest` | yes: only distance tests; none of `FanClarityTest` or `FanOutLayersTest` |
| (b) | `fadedWhileFanned` to `false` | 4 `FanClarityTest`: layer set, 80 percent, sighting, user opacity | yes: fade tests only; the 3 guards still pass |
| (c) | drop `* FAN_FADE_OPACITY` | 3 `FanClarityTest`: 80 percent, sighting, user opacity | yes: the layer-set test passes, as it should, since which layers fade is untouched |
| (d) | circle diameter 48 to 32 dp | 1 `FanClarityTest`: circle style | yes: alone |
| (e) | drop the `circles +=` line in `fanFrameCollections` | 3 `FanOutLayersTest`: one circle per copy, kept record has a circle and no halo, member with nothing to draw | yes: circle tests only. The "gets no circle either" case fails at `FanOutLayersTest.kt:118`, the positive `assertEquals(1, circles)` for the other member, not its "none" side |

The three guards are not counted as evidence by any check above.

## Full unit suite

`:app:testDebugUnitTest`, 5m 16s, `BUILD SUCCESSFUL`: **3146 tests, 0 failures, 24 skipped**, 0 compile errors. None of the owner-held
flakes (`JournalPendingDeleteTest` album long-press, `JournalTabTest` From Album photo pull) or the intermittent `DiagnosticsPanelTest`
failed. The suite ran on `95b36d76`'s code before the merge; the merge adds no file under `app/`, `data/` or `server/`, so it is the
same code as the merged head. It was **not re-run after the merge**, because there was nothing to re-run it on.

## Merge

`origin/journal-redesign` was `4c52a1cc`: five docs-only commits (-266 to -272). **map-return-fixes has not merged**, so the fold
`LaunchedEffect` conflict the dispatch anticipated near `SightingsMap.kt:761-785` has not arisen. Whoever merges second there resolves it;
my edits in that file are the fan `LaunchedEffect` key list (dropped `journalHighlights`), the paint effect (now a `snapshotFlow` on
`fanOut.isOpen`), and a new chrome-colour effect.

## Not tested here (device-only; a `MapView` cannot be built under Robolectric)

Everything above proves the pure values and rules. That `SightingsMap` applies them on live MapLibre layers is unproven:
1. **The look by day and night:** the circle is the map chrome colour (`navigationBarContainerColor()`, `#F4EFE2` light, `#202020` dark)
   at 80%, 48 dp, under the icon and above the legs, and no white halo behind a fanned copy of a journal-kept record.
2. **The fade and its restore:** every marker icon outside the fan drops to 80% of its own paint (the sighting fill 0.7 becomes 0.56),
   lines and fills unchanged, and on folding each returns to exactly its prior look, including after a Layers-sheet opacity change made
   while a fan is open.
3. **The 30 dp pair:** the owner's screenshot pair, whose 30 dp is the planner's estimate, no longer fans, and a pair the owner can see
   overlapping at about 25 dp still does.
4. **Theme change with a fan open:** the circle recolours (`applyFanCircleStyle`); not exercised anywhere.
5. **Fade ends when the fold starts:** rests on `isOpen` being false from the moment `wantOpen` clears (`MarkerFanOutState.kt:41`), read, not run.
