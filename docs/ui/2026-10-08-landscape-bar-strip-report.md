# Landscape: the search bar and the strip meet at the centre

Dispatch 2026-09-28-729 (RECORD intent -729; preserved at `prompts/preserved/2026-10-08-03.md`). Branch `landscape-bar-strip`,
cut from `origin/main` at `f8739856` (PR #202 merged; fetched and confirmed before cutting). No PR. Paths are relative to
`app/src/main/java/com/zynergylabs/forager/app/ui/availability/` unless they start with `app/` or `docs/`.

**Status: built and run. Full suite 4,432 tests, 24 skipped, 6 failures, all six existing tests that this change breaks,
left untouched and listed below with their reasons.** Two of them (the large-font strip) are a real conflict between this
ruling and RECORD -699, and need the owner.

## Premises checked

1. **"Left half / right half" holds at ROTATION_90 only.** The bar follows the punch-hole and the strip the rail
   (`punchHoleEdgeFor`, `portEdge`); at ROTATION_270 both swap. Built mirrored. The three-dot button stays at the strip's own
   right end in both rotations (RECORD -649), so at 270 it sits at the join, beside the line. Tested at both rotations.
2. **Wrong premise: meeting the bar gives the strip no more room.** Before, the strip was already capped at the room beside
   the bar (RECORD -694), which reached to the bar's end (8 dp short of the centre at 780 dp; at 823 dp the bar stopped at
   its 384 dp cap, 27.5 dp short). Now the bar ends at the centre, so the strip is *narrower*: 310 dp at 780 (was 318) and 331.5 dp at 823 (was 359), font 1.0. The heading that 14 sp
   dropped (the dispatch's "Why") is still dropped; see Measurements.
3. **The 384 dp cap and the 8 dp gap no longer apply while not navigating.** Item 1 says the bar ends exactly at the centre;
   at 823 dp the cap would have stopped it at 384. Removed for that state only.

## What changed

- `AvailabilityCompactScaffold.kt`: in a short landscape window on the Maps tab, not navigating, `landscapeSearchWidth` is
  `maxWidth / 2 - punch-hole inset` (ends at the centre). `mapSearchBarHeight` is the strip's measured height there (else the
  bar's own `searchBarHeight`, 45 dp); everything the scaffold places by the bar's bottom on the Maps tab follows it: the
  map's top inset (chips, bubble), the L's top limit, the dropdown and its scrim. While navigating, the old capped width and
  the bar's own height (the owner: "Strip only").
- `AvailabilitySearchUi.kt`: `SearchEntryBar(landscapeHeight)`: when set, the bar is exactly that tall, the field row centred
  above its divider. `null` everywhere else (portrait, other tabs, navigating, before the strip is measured).
- `AvailabilityCompactMapUi.kt`: the landscape strip takes the rail-side half (`landscapeStripHalf`), fills it
  (`contentWidth = false`), is held at least as tall as the bar's field plus divider (`landscapeBarContentFloor`, 33 dp at
  font 1.0), and reports its height up (`onLandscapeStripHeightMeasured`). The 1 dp line (`LANDSCAPE_BAR_STRIP_LINE_TAG`),
  `mapIconStackBorderColor()` (the divider's colour pair), centred on the window, as tall as the two; only over their join,
  no pointer input, not in fullscreen or navigating.
- `LandscapeChromeWidth.kt`: `Modifier.landscapeStripHalf`, the strip's width worked out (not read back), as the room left
  after the bar's own rounded pixels, so an odd window width neither overlaps nor gaps them.
- `AvailabilityMapControlsUi.kt`: the strip's Box centres its row vertically (only visible when held taller, at large fonts).
- `AvailabilityMapIconCluster.kt`: the cluster's re-fit is also keyed on the top limit, **in landscape only**. Found by the
  first full suite: the L kept the clamp of the first frame (45 dp, before the strip was measured), so it did not follow
  the bar's new bottom, and a later notice moved it 1 dp (LandscapeLRulingsTest B1, SearchNoticeLandscapeTest). Portrait's
  limit animates with fullscreen and is deliberately not a key (CompactMapTab's `searchBarBottom` comment); unchanged.

Portrait: no code path changes (every change is gated on the landscape window or `state.landscape`); the portrait tests in
the full suite pass.

## Measurements (native graphics, xxhdpi, font 1.0; `LandscapeBarStripJoinTest` prints them)

| Window, rotation | Bar | Strip | Line | Height |
|---|---|---|---|---|
| 780 x 360, 90 | 0 to 390 | 390 to 700 (rail 700) | 389.67 to 390.67 | 36 / 36 |
| 780 x 360, 270 | 390 to 780 | 80 to 390 | 389.67 to 390.67 | 36 / 36 |
| 823 x 384, 90 | 0 to 411.67 | 411.67 to 743 | 411 to 412 | 36 / 36 |
| 823 x 384, 270 | 411.33 to 823 | 80 to 411.33 | 411 to 412 | 36 / 36 |

The bar is 36 dp, down from 45; the strip 36 dp (its button's floor). The three-dot button is 36 x 36 at the strip's right end.

**What the strip shows whole** (heading 315° NW, altitude 3,000 m = 9843 ft, Imperial default): at both windows, both
rotations, both formats: **altitude and coordinates; the heading is dropped, and the Alt label.** MGRS "10T ER 24991 40768",
decimal "45.5200, -122.6800". Every value shown is whole (not ellipsised, every character visible).

Why, from a probe of the 14 sp widths (removed after use): heading 58.3 dp, "9843 ft" 48.3, "Alt" + gap 23.0, a separator
20.3, MGRS 140.3, decimal 132.0. Room for the readouts is the strip less its button (36), padding (16) and needle (18): 240 dp
at 780, 261.5 at 823. Adding the heading needs 307.9 (MGRS) / 299.6 (decimal): short by 68 / 60 dp at 780 and 46 / 38 dp at
823. Before this change the room was 248 dp at 780 and 289 at 823, and the same readouts showed (heading dropped at both).

## Tests (`app/src/test/.../ui/availability/LandscapeBarStripJoinTest.kt`, 16 tests)

Real `AvailabilityScreen`, 780 x 360 and 823 x 384 (xxhdpi, native graphics), ROTATION_90 and 270, each proving the screen
saw the rotation:
- the join: the bar's inner edge, the strip's inner edge and the line's centre each equal the map's centre (±0.5 dp; positive
  control that the map's centre is the window's); bar at the punch-hole edge, strip at the rail's inner edge; equal tops and
  heights; strip ≥ 36 dp; line 1 dp wide, from the bar's top to its bottom;
- five real coordinate touches across the three-dot button each open the menu (screen composed afresh between touches);
- long-presses around the join reach the map: 10 points just below it at both rotations, plus 3 on the strip beside the join
  at 90 (13 sampled); at 270 those 3 land on the button and are skipped, listed in the output, and the count is asserted;
- the readouts shown are one line and whole in MGRS, then in decimal after a real touch on the coordinates (positive
  control: the text changed).

## Revert checks

Runner: saves the file, applies a one-line edit, runs the named classes in a fresh capped scope, refuses results on any `e: `
line (none), reads only JUnit XML written after the run started, restores from the saved copy (never git) and checks it
byte-identical. Run on the final code; `git status` clean after; each forward line confirmed present by grep.

| # | Edit | Class | Failed | Message |
|---|---|---|---|---|
| R1a | bar back to the capped width | LandscapeBarStripJoin780/823 | 2/8 each | "the bar's right edge is the centre expected 390.0 but was 382.0" (823: 384.0; 270: 398.0 / 439.0) |
| R1b | strip back to the room beside the bar | same | 2/8 each | "the strip's left edge is the centre expected 390.0 but was 382.0" (823: 384.0) |
| R2 | bar keeps its own height | same | 2/8 each | "the bar is the strip's height expected 36.0 but was 45.0" |
| R3 | no line | same | 2/8 each | "could not find any node ... 'landscape-bar-strip-line'" |
| R4 | cluster not re-fitted on the limit | LandscapeLRulingsTest, SearchNoticeLandscapeTest | 2/18, 1/1 | "the L did not move ... expected 45.0 but was 44.0"; "the cluster returned to where it was expected 47.0 but was 44.0" |

## Existing tests this breaks (not touched)

- `AvailabilityScreenLandscapeB2Test` S2 at 90 and 270: assert the bar's width is `min(384, centre - 8)` and ends 8 dp short
  of the centre. Item 1 replaces that (412 at 823 dp). The ruling they encode (Landscape B2 S2) is what -729 reverses.
- `AvailabilityScreenLandscapeB2Test` S1 at 90 and 270: assert the bar "stays clear of the far half" with the same 8 dp gap.
  Same reason.
- **`LandscapeLargeFontTest` "font 2,0 ... the strip stays beside the search bar on one line", at 90 and 270: the
  coordinates are ellipsised, "10T ER 24991 40768" shows 15 of 18 characters.** At font 2.0 the rail is 107 dp, so the
  strip's half is 304 dp; before it was 332 (the bar stopped at 384). RECORD -699 (the owner: "Coordinates take priority
  (Recommended)") keeps them whole. This is a real conflict between the two rulings at font 2.0 on the 823 dp window, not a
  harness artefact. Options for the owner: accept the cut at font 2.0; let the join leave the centre when the coordinates
  need it; or trim the strip's padding or needle there. Not decided here.

## Decided beyond scope (each reversible)

1. **Large fonts.** The bar's field (twice the labelMedium line) plus divider is 33 dp at font 1.0, under the strip's 36, so
   the bar follows the strip. At font 2.0 it is 65 dp, taller than the strip's row; rather than cut the field (a control
   losing its touch area, a stop), the strip is held at that height and the bar follows it. So at large fonts the strip grows
   to the bar, not the reverse.
2. **The bar's divider is kept** as its bottom edge in landscape, so a 1 dp line runs under the bar's half only. Not asked to
   remove it.
3. **Under legacy (non-native) graphics** Robolectric measures "Mg" far taller, so in those test classes the landscape bar
   and strip are 73 dp. A harness effect, shown in the B2 failures' bounds; the native-graphics tests above are the measure.
4. **Starting or stopping navigation:** the bar's width switches at once while the strip slides out (or in) at half width,
   so for the slide (the map tilt's length) the leaving strip overlaps the bar's centre end. Not tested; device step 3.
5. In fullscreen the line is not drawn and the strip keeps its half.

## Device only (S22, cheap first)

1. Landscape, Maps tab, with a fix, both rotations. Pass: bar and strip meet at the centre with one thin line between them,
   the same height, no band of fill hanging below either. Evidence: a screenshot (kept off GitHub).
2. Against the real cut-out and status bar: the bar starts at the cut-out side's controls edge and still ends at the centre.
3. Start and stop navigation: the strip slides out and in; note whether the brief overlap with the bar's end shows.
4. Tap the search bar: the dropdown opens under the bar at the bar's width, Clear works, and the bottom row lifts above the
   keyboard (Robolectric reports no keyboard).

## Gradle

Every run checked `systemctl --user is-active t6b-night` (inactive each time, not touched) and free disk (3.6 GB at the
start, 3.3 GB at the end): `systemd-run --user --scope -p MemoryMax=5G -p MemorySwapMax=0`, `--no-daemon`, Gradle
`-Xmx1536m`, Kotlin daemon 2g, Java temp `~/.cache/forager-test-tmp`, `./gradlew --stop` and the Kotlin daemon stopped after
each scope. No Gradle process left at the end.

## Commits

`dc6047d8` (the layout and its tests), `73e62bb0` (pixel rounding; the L re-fits on the limit), and this report.
