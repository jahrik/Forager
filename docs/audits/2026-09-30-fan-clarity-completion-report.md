# Fan clarity (dispatch 2026-09-28-265, preserved as prompts/preserved/2026-09-29-58.md): completion report

Status: **pre-registration (written before building).** Sections below the line are filled in as the work lands.

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
