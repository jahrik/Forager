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

---

# Continued: dispatch 2026-10-12-08 (RECORD -945 to -952)

Base `origin/rounded-map-strip` at `4b8e5c6c`, confirmed before starting. Commits `b58f6fd5`, `1f483db5`, `9718c3c5`, `c3894087`,
`33cdb917`, all pushed. Not merged; the PR stays a draft. No Gradle on the laptop: CI only.

## What landed

- **The strip stays while navigating (-948, -950)**, in both orientations. Its line drops the north arrow and heading, leaving
  height · grid reference · ⋮ (`StripNavigatingReadout` in `AvailabilityMapControlsUi.kt`). While navigating it does not read the
  heading, so the navigation info is still the only place reading it at sensor rate. The strip's own sundown and Back by lines are not
  shown while navigating.
- **Portrait: one card.** The navigation info is drawn inside the strip's card, under a 1 dp line (`CompactMapPortraitNavigationInfo`,
  through the strip's `navigationInfo` slot): one fill, one shadow, no second tuck. It grows down from under the line on the
  navigation view's timing; under reduced motion it only fades. The chips are now composed before the strip, so while navigating the
  card covers them, as the old display did.
- **Landscape: the card hangs under the strip.** RECORD -951 restores main's sizes (-729, -732: `landscapeStripFit`, with the bar taking
  the rest of the width), replacing -940's short-side width.
  - The bar meets the strip whether navigating or not, so nothing resizes when navigation starts.
  - The card sits at the strip's measured bottom, at its exact width, tucked under it, and slides down from under it.
  - The tuck's cut-out now follows the real lower corners of the piece above (`mapCardSurface(upperCorners = …)`).
  - The card has its own keep-out id, `MapKeepOutIds.NAVIGATION_CARD`.
- **The navigation info (-945, -950)** (`NavigationInfo` in `NavigationHud.kt`):
  - Left part, centred: the distance in headlineSmall bold (24 sp, twice the 12 sp text); "ahead 1° ▲ ▲ 134° SE" on one line, with the
    two arrows 4 dp apart; then the kind word on its own line.
  - A divider at the taller part's full height. It stays when there is no evening stack (-951).
  - Right part: the evening stack, centred, with the X at the right edge.
  - No Alt, grid reference or ⋮ in the card.
  - The status line no longer carries the kind word. It leaves out the position statuses (no fix, approximate, last seen), which the
    strip shows once (-951 (a), `statusIsPositionStatus`).
- **Fit** (`navigationInfoFit`): the turn words shrink to degrees, then the distance takes "≤ 16 ft". Nothing else is trimmed. On
  overflow the left part stays whole and the evening pieces wrap; nothing is cut.
- **The ⋮ sits above the X in portrait** (-951 item 5): it is on the strip's line, and the X is below the line.
- **Lighter pill (-945, -951):** `MapPillColorLight` = `surfaceContainerLowest` (#FFFFFF) and `MapPillColorDark` =
  `surfaceContainerHighest` (#373737), both at 0.8. They apply to the Maps-tab pill and to the landscape strip. The card and the
  portrait strip keep the chrome token. Contrast of the pill's text and icon:

  | | Day | Night |
  |---|---|---|
  | Opaque | 13.1:1 | 11.9:1 |
  | Worst case at 0.8 | 8.2:1 | 6.4:1 |

  The spinner (`primary`) is 3.6:1 at its worst case.
- **Pill floor (-951 item 8):** `MAP_PILL_MIN_HEIGHT` is 40 dp, enforced in the pill and in the scaffold's `mapPillHeight`. At font
  0.85 it measures 40 dp; without the floor it was 35.3 dp. The comment that said 41 dp now says 40.
- **The strip's own evening lines (-951 item 4):** one whole line where it fits; otherwise the pieces stack at their " · " joins. Never
  "…".
- **RECORD -952:**
  - The landscape rail rounds its two corners facing the map, clipped like the bottom bar's.
  - The credit strip is a pill, with all four corners rounded.
- **Removed as dead by this change:** `NAVIGATION_HUD_EVENING_LINE_TAG`, `NAVIGATION_HUD_ELEVATION_TAG`,
  `NAVIGATION_HUD_COORDINATES_TAG`, the three-column fit, `mapCardWidth` and `rowsBottomInRootPx`.
- **Left dead for the tidiness release (-951 item 10):** `besideLandscapeSearchBar`, `railSideRoomBesideSearchBar`,
  `LANDSCAPE_HUD_MAX_WIDTH` and `firstRowFit`. `landscapeStripFit` is in use again.

## The chip row and the lighter patch

- **Chip row:** `LayoutFixesChipRowLandscapeTest` passes unchanged once main's widths are back. -940's 384 dp strip was the cause.
- **Lighter patch (-951 item 11):** the probe was re-run on the new card (412 dp portrait, run 38257949553), then removed. The same
  lighter island is drawn: x 587-649 px, y 351-372 px. Its red is 245 or more against 166-169 in the shadow's darker band under the 0.8
  fill. It is the shadow renderer's output, not an element, and stays a device item.

## Tests changed: old and new assertion, and the ruling

| Test | Old | New | Ruling |
|---|---|---|---|
| MapIconStackTest: strip absent / leaving | strip count 0 | strip 1, no strip heading, "95° E" once | -950 |
| MapIconStackTest: no fix | NO_FIX on the card's status | on the strip; card status "" | -951 (a) |
| MapIconStackTest: elevation, grid, toggle, format (4) | card's elevation/coordinates tags | strip's tags | -950 |
| ApproximatePositionTest (5 asserts) | position status on the card; strip absent | strip note once; card status "" | -951 (a) |
| QuickSettingsTest, navigating | strip hidden; ⋮ in the HUD under the X | strip shown; ⋮ on it, above the X; strip's Back by absent | -947, -951 |
| LayoutTest | strip `assertDoesNotExist` ×2 | `assertIsDisplayed` | -950 |
| NoFacingLabelTest, navigating | `[Alt]` in the card | none in the card; `[Alt]` on the strip | -950 |
| SundownLineTest, Return | "and the strip with it goes" | strip shown, its own line absent | -950 |
| NavigationWordsTest, portrait (3) | Alt, elevation and grid on the card; kind beside the figure or leading the status | strip's tags; "by trail" under the turn line | -945, -950 |
| NavigationWordsLandscapeTest | `hud.bottom <= thirdTop` | above the walker's dot by its radius; needle inside the card and uncovered | -766 via -951 |
| LandscapeLargeFontTest :185 (via :219, :261) | `hud.bottom <= thirdTop` | same rule as the row above; grid reference whole, on the strip | -766 via -951, -950 |
| B2Test S4, navigating ×2 | strip hidden; HUD at the map top; ≤ 360 dp wide | strip at the map top; card's top = strip's bottom; same edges | -946, -950, -951 |
| B2Test S4, strip ×2, and LandscapeBarStripJoinTest | -940's short-side widths | main's content width, restored | -951 |
| LandscapeBarStripJoinTest, join ×2 | main's 1 dp join line | no line | -934 |
| LandscapeCardMeetsBarTest ×2 | bar meets card at the top; short-side width | strip and bar identical navigating and not; card's edges = strip's, both rotations | -950, -951 |
| T11FixesTest, item 1 (font 2.0) | card's grid reference whole | strip's grid reference whole | -950 |
| T11FixesTest :375, :418 | `NAVIGATION_HUD_EVENING_LINE_TAG`, never drawn | `NAVIGATION_HUD_SUNDOWN_LINE_TAG` | -941 |
| T11 harness, state 01 | NO_FIX on the card's status | on the strip | -951 (a) |
| RoundedMapCardTouchTest | X above ⋮; card's grid tag | ⋮ above the line, X below; strip's grid tag | -951 item 5, -950 |
| ButtonPlacementTest | ⋮ under the X | no ⋮ in the card; X at its top right; real touches stop navigating | -947, -950 |
| QuickSettingsWidthTest | text clipped with the ⋮ vs without | figure, kind and turn whole; status whole or one line ending in "…" | -947 |
| KindInStatusTest | "By trail" leading the status line | "by trail" on its own line; status "" | -945 |
| ArrowsSideBySideTest | 1 divider, 13 dp between the arrows | 0 dividers, 4 dp | -950 |
| FitAcrossWidthsTest | grid reference drawn; elevation and Alt whole; a landscape pass | those removed; card height printed | -950, -951 |
| RoundedMapCardFitTest | three-column fit (11 tests) | two-part fit (8 tests) | -945, -950 |
| HudSundownLineGrowTest | a two-piece line | five pieces plus Back by (two pieces no longer outgrow the left part) | -945 |
| MapPopUpMotionTest | the strip leaves; the display slides in | strip stays; card grows (sampled 6 frames in); leaving X takes no touch | -948 |
| MapChromeColourTest: pill; landscape strip | chrome token at 0.8 | pill role at 0.8 | -945, -946, -951 |
| MapChromeColourPixelsTest | pill and its lower band = chrome token | pill role; pill at least 2% lighter than the strip | -945 |
| MapCreditCaptionTouchTest | top-right rounded, the other three square | all four corner gaps show the map; long-presses in all four | -952 |

**New tests:**
- `MapTopCardScreenTest`, in portrait and in landscape at 90 and 270:
  - nothing on screen repeats;
  - five real touches each across the X, the grid toggle, the ⋮ and Try again;
  - long-presses at the card's corner gaps, just below it and beside it;
  - portrait only: one card, with no shadow seam at the line;
  - landscape only: long-presses in the rail's corner gaps.
- `NavigationInfoLayoutTest` and `StripEveningLineStackTest`.
- New assertions added to `MapChromeColourTokenTest` and `SearchBarStripSeamTest`.

No test was ignored, skipped or loosened, and none was deleted without a ruling.

## Revert checks

Twenty, each run as below:
- On its own `revert-check/rms08-*` branch, built from committed state with `git commit-tree`, so the working tree was never touched.
- The build log was checked for compile errors before reading any results: every one had 0.
- Results were read from that run's own artifact.
- Afterwards the forward change was confirmed still in HEAD, and the branch was deleted.

r01-r16 ran at `c3894087`; r17-r20 at `33cdb917`.

| Check | Edit | Run | Failure it produced |
|---|---|---|---|
| r01 | the strip's line keeps its heading | 38259677067 | "the heading reads once expected 1 but was 2" |
| r02 | the strip is removed while navigating | 38259693182 | 23 failures, e.g. no `compass-strip-coordinates` |
| r03 | the info drawn as a second tucked card | 38259710140 | "no shadow cast at the line … 0.9109 against 0.9383" |
| r04 | the pill back to the chrome token | 38259727177 | pill colour wrong (2 classes) |
| r05 | the landscape strip back to the chrome token | 38259742956 | strip colour wrong, light and dark |
| r06 | no pill floor | 38259758973 | "the pill is at least 40 dp: 35.33" |
| r07 | the card shows position statuses | 38259774100 | 7 failures: "leaves it out … but was Approximate, finding GPS…" |
| r08 | no kind line | 38259791894 | 5 failures: no `navigation-hud-distance-kind` |
| r09 | the divider not full height | 38259808211 | 3 failures: divider 0 dp tall |
| r10 | the distance back to titleMedium | 38259824691 | "16.0 sp against 12.0 sp" |
| r11 | left part aligned to the start | 38259841550 | "figure 42.7 and turn line 76.7 share a centre" |
| r12 | arrows 16 dp apart | 38259855933 | "expected 4.0 but was 16.0" |
| r13 | no ⋮ on the strip while navigating | 38259871465 | "one three-dot button expected 1 but was 0" |
| r14 | the card 360 dp wide | 38259888908 | 4 failures: card edges are not the strip's |
| r15 | the bar stops meeting the strip while navigating | 38259903020 | "and so does the search bar … 411.3 …" |
| r16 | the strip's line never stacks | 38259919082 | "three pieces … but was []" |
| r17 | the info only fades in | 38261935689 | "the card … is shorter than it settles" |
| r18 | the rail's corners square | 38261877578 | rail-corner long-press, both rotations |
| r19 | the credit strip back to top-right only | 38261892496 | "bottom-right corner gap shows the map" |
| r20 | a leaving stack's width not held | 38261907942 | "caught shrinking" (frames reach 823 dp) |

Two limits on what these checks cover:
- The credit strip's long-press test cannot fail on the corner shape, because the caption takes no touches anywhere. Only its pixel test
  catches a wrong shape (r19).
- In landscape the rail's top corner lies within reach of the strip's ⋮, so the rail test samples the bottom corner only.

## CI

- Full run `38261622357` at `33cdb917`: 602 suites, 4,655 tests, **8 failures**, 24 skipped.
- Main `a576c051` (`38223860061`): 587 suites, 4,573 tests, 0 failures, 24 skipped.

## Stops: landscape height, for the owner (numbers only, nothing trimmed)

The 8 failures are in `HudLandscapeT11FixesAt780Test` and `…At823Test`, four each, against -766 and -770. In landscape the card now
starts under the 36 dp strip.

1. **-766, "no taller for Try again":** the card is 76 dp on the route and 124 dp with "Unable to calculate route" plus Try again's
   48 dp row. At 4b8e5c6c it was 90 → 116.
2. **-770 at font 1.0** (route withheld, evening lines): the card ends at 160 dp. The L's lowest top is 120 dp at 780 and 144 dp at 823,
   so the L cannot slide below the card.
3. **-770 at font 2.0** (route withheld): the card ends at 203 dp even without the evening stack. Reset north's top is at 168 dp (780)
   and 192 dp (823).
4. **The walker's dot, at the tallest state** (font 2.0): the card ends at 315 dp, against limits of 213 dp (780) and 228 dp (823). The
   evening pieces wrap at overflow.

Other measurements:
- **Landscape, 823 dp, no target:** at font 1.0 the card is 36-112 dp, ending 16 dp above the central third. At font 2.0 it is 38-156 dp,
  28 dp into the central third and 72 dp above the walker's dot, which -766 as -951 applies it allows.
- **Portrait navigation info** (`NavigationCardFitAcrossWidthsTest`, at 320 to 412 dp):

  | State | Font 1.0 | Font 1.3 | Font 2.0 |
  |---|---|---|---|
  | On the route | 76 dp | 84 dp | 118 dp |
  | With Try again | 124 dp | 132 dp | 166 dp |
  | Owner's evening example | 76 dp | 90 dp (132 dp at 320) | 198-230 dp; **422 dp at 320 dp** |

  At 320 dp and font 2.0 the pieces wrap heavily. No text is cut in any of the 132 states.

## Device items

1. **Portrait, navigating:** the card extends down as one card. Check the lighter pill and the tuck by day and at night; the bigger
   distance; the arrows side by side; "by trail"; text fitting at the phone's own font; how easy the ⋮ and the X are to hit; and
   long-presses at the corner gaps and just below the card.
2. **Landscape, navigating:** the strip neither moves nor resizes, and the card slides down at its exact width. Whether Reset north and
   the walker's dot stay clear at the phone's own font: CI says not at font 2.0 with Try again.
3. **Ending navigation:** the heading returns to the strip's line.
4. **Not navigating:** the strip's sundown line stacks only when it does not fit.
5. **A route that cannot be found:** Try again appears, then goes once a route is found.
6. **Against the real system bars and map** (Robolectric reports zero insets):
   - the pill's 40 dp;
   - the bottom bar's and the rail's rounded corners;
   - the credit pill over the real map;
   - the lighter patch;
   - the landscape pill's width beside the strip, with a real cut-out inset.

Screenshots: `~/Zynergy/device-evidence/2026-10-12-nav-strip-mockup/robolectric-33cdb917/` (outside the repository).
