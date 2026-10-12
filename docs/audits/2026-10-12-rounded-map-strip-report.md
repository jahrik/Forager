# Rounded, tucked map strip: completion report (dispatch 2026-10-12-06)

RECORD -932 (intent), -934, -935, -936, -937 (the owner's answers). Branch `rounded-map-strip`, rebased onto main `a576c051`
(PR #220) with no conflicts: the two sides touched no file in common, and the rebased patch is byte-identical to the
pre-rebase one (`git diff 921aa9c7 9199b5b1` against `git diff a576c051 30e83e2b`). Not merged.

## What landed

- **Search pill** over the map (`AvailabilitySearchUi.kt`, `pillCorners`): rounded, 2 dp shadow, no divider (a 1 dp spacer keeps
  the bar's 45 dp). Full width (RECORD -934's zero side margin; the planner read the pill and strip as full width too, for
  the owner to confirm from the screenshots). Other tabs unchanged.
- **Tucked card** (`RoundedMapCard.kt`): the strip and the navigation card draw their fill 16 dp up behind the pill, less the
  pill's shape, so nothing is laid twice at 0.8; their own bounds start at the pill's bottom. No pointer input.
- **Navigation card** (`NavigationHud.kt`): three columns (figure + kind over turn + needle; "Alt" over arrow + heading;
  grid reference over a short evening line), the X above the three-dot button in a column at the right edge, then lines
  under the columns only when they say something: the grid reference (when it has left its column), the evening lines,
  the route message with Try again, the status line (everything today's carried, RECORD -935).
- **Fit** (`RoundedMapCardFit.kt`), the owner's order (RECORD -934, -936): kind, turn to degrees, "Alt", "≤ 16 ft", grid
  reference to its own line. Nothing further; an overflow is logged.
- **Turn words** (`NavigationWords.kt`): "sharp right 169°"; `turnShortForm` reads the last word (the " · " parse would have
  silently returned null).
- **Landscape**: the pill and strip meet square with no join line; navigating, the bar now takes the room up to the card.
- **Bottom bar** (Maps tab only) rounds its top corners by a clip, so the corner gaps reach the map (RECORD -937).
- **Map credit line** rounds its top-right corner only (the owner's correction to -937).

## CI

- Final full run `38225602288` at `30e83e2b`: 597 suites, 4,628 tests, **12 failures**, 24 skipped. Main `a576c051`
  (run `38223860061`): 587 suites, 4,573 tests, 0 failures, 24 skipped.
- The 12 failures are all landscape, all the display's height or width against earlier owner rulings, and are left failing
  for the owner (not edited, not ignored): `HudLandscapeT11FixesAt780Test` ×5, `HudLandscapeT11FixesAt823Test` ×3,
  `HudLandscapeT11At780Test` ×2 (positive control: the evening line left by -770), `LandscapeLargeFontTest` ×2. See
  "Stops" below.

## Revert checks

Each ran on its own `revert-check/*` branch built from committed state, never touching the working tree; each build log was
checked for compile errors before its results were read. r1 to r12 ran at `9199b5b1` (before the rebase), r1b, r11b, r13
and r14 at `30e83e2b`.

| Check | Edit | Run | Result |
|---|---|---|---|
| r1 | turn words back to " · " | 38223951325 | did not run (SDK download failed); rerun as r1b |
| r1b | same | 38225621991 | bit: NavigationWordsTest 5, e.g. `expected slight left 10° but was slight left · 10°` |
| r2 | `turnShortForm` back to the " · " parse | 38223978202 | bit: NavigationHudFitTest 2, `expected 169° but was null` |
| r3 | middle column rows swapped back | 38224003291 | bit: arrows test 4/4, `the two arrows share a line (centres 39.0 and 16.0)` |
| r4 | card fill without the pill cut out | 38224029393 | bit in dark only: `the pill's lower band ... not laid twice`; light passed (weak: see below) |
| r5 | pill square | 38224053985 | bit: `long-press at (2.0, 2.0) in the pill's rounded corner reached the map` |
| r6 | three dots above the X | 38224077900 | bit: placement 360/384 and touch test |
| r7 | no grid-reference line step | 38224099469 | bit: fit test 8/12 (grid reference ellipsised) and RoundedMapCardFitTest |
| r8 | no "≤ 16 ft" step | 38224125346 | bit: `then within 16 ft takes its short form` |
| r9 | bottom bar square | 38224151246 | bit: `long-press at (2.0, 702.0) in the bar's rounded corner` |
| r10 | credit line square | 38224174294 | bit: `top-right corner gap shows the map` |
| r11 | landscape bar back to its cap | 38224195024 | **did not bite** at 780/823: the display already filled the room there |
| r11b | same, with a 915 dp class added | 38225646748 | bit at 915 only: `bar's right edge is the card's left edge, expected 475 but was 384` |
| r12 | evening line always in the right column | 38224217166 | bit: fit test 6 (sundown cut), grow test, fit unit test |
| r13 | heading drawn with no fix | 38225670759 | bit: `no heading line` (MapIconStackTest) |
| r14 | no empty status node | 38225693221 | bit: ArrivalHoldTest `navigation-hud-status` missing |

r4 is weak: in light theme a doubled 0.8 layer is within the shadowed-colour tolerance, so only the dark case tells.

## Measured widths (run 38215191193)

Recorded in the hand-back; the fit test (`NavigationCardFitAcrossWidthsTest`) now covers 320/360/384/412 dp at fonts
1.0/1.3/2.0 and passes: nothing in the columns, the grid line or the evening lines is clipped, the arrows share a line, and
the grid reference is whole. The "within 16 ft" case can't be reached on the real screen: arrival (15 m or twice the
accuracy) always covers "within" (distance ≤ accuracy), so "≤ 16 ft" is exercised only by the pure fit test.

## Stops (owner's call)

1. **Landscape height.** With the X and the three dots stacked (76 dp) and the lines under the columns, the landscape card is
   taller: Try again's 48 dp row adds height (94 to 118 dp at 780), against -766's "no taller for Try again"; at font 2.0 it
   reaches 154 dp, past the central third (128 dp, Landscape B2 S10), over Reset north and the walker's dot (T11 item 2,
   -770); at font 1.0 with the evening line it ends 14 dp under the L's lowest top.
2. **Landscape grid reference at font 2.0, 780 dp**: the card is 292 dp wide (the room beside the capped bar, -694); the grid
   line has 236 dp; MGRS needs 239.5 dp, so it ends in "…" (against -699).
3. Portrait "No route" short form beside Try again at narrow widths (RECORD -768's form, now used in portrait too).

## Device items

The pill's top margin and fullscreen slide against the real status bar; the bottom bar's rounded corners against the real
navigation-bar inset (Robolectric reports zero insets); shadow and 80% fill over real tiles, day and night; the mockup match
at the phone's own font; a small lighter rectangle seen at the middle column's lower right in the Robolectric screenshots
(unexplained, not investigated); the credit line over the real map (MapLibre is not drawn under Robolectric, so its test
composes the caption alone).

Screenshots: `~/Zynergy/device-evidence/2026-10-12-nav-strip-mockup/robolectric-30e83e2b/` (outside the repository).
