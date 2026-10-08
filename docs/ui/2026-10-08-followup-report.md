# Follow-up: the bar after a turn, New find in the landscape header, the night blend, the Maps-return blank

Dispatch 2026-09-28-755 (RECORD intent -755; preserved at `prompts/preserved/2026-10-08-07.md`), from RECORD -752, -753 and -754.
Branch `followup-1008`. It was cut from `origin/landscape-search-fixes` (eb4932ca), as the coordinator instructed, and fast-forwarded
to `origin/main` 34fd5deb once PR #206 merged (no merge commit was needed). No PR. Paths are relative to
`app/src/main/java/com/zynergylabs/forager/app/ui/` unless they start with `app/` or `docs/`.

**Status:** items 1, 2 and 3 are built, tested and revert-checked. Item 4's cause was found on the S22, and it is not ours to fix
without a decision: logging only (see item 4). The full suite and the S22 launch check results are below.

## 1. The bar after a turn into landscape (RECORD -754)

**Premises that were wrong.** The dispatch read the owner's first screenshot as the bar growing *when the dropdown opens*. That
screenshot came from build 1.0.3097 (c0ec942a, before #206). Under Robolectric (780 and 823, both rotations, after a turn from
portrait, short and long summary, open, and open with a docked keyboard injected), the open bar kept its closed bounds every time,
for example 780, ROTATION_90: bar [0,0][429.33,36], strip [429.33,0][700,36], panel [0,36][429.33,360]. By reading code only
(c0ec942a against 34fd5deb), nothing in the bar's size reads the dropdown's open state at either commit. #206 changed only the
panel: its landscape top offset (the 36 dp gap became 0) and its height cap with a keyboard. Not checked on a phone. The owner
then reproduced the real fault on 1.0.3103 (34fd5deb), S26, ROTATION_90: **after a turn from portrait the bar's surface was taller
than the strip and reached the window's centre, past the join**, with the dropdown closed.

**Cause (reproduced headless first).** The landscape compass strip reports its measured height and width to the scaffold from
`LaunchedEffect(landscapeStripHeightPx)` and `LaunchedEffect(landscapeStripWidthPx)` (`AvailabilityCompactMapUi.kt`,
`CompactMapTopStrip`), which run only when the number changes. The scaffold zeroes its own copy whenever the bar stops meeting the
strip (`AvailabilityCompactScaffold.kt`, `LaunchedEffect(barMeetsStrip)`), but the strip's own measures were zeroed only on
navigation, not in portrait. On a second turn into landscape the strip measured the same numbers as before, nothing was reported,
and the bar fell back to its own height (45 dp) and the half width it uses before the strip is measured. That is the screenshot:
on the S26 the window's half lies past the join. A first turn, which is what #206's tests made, is fine.

**Fix.** `CompactMapTopStrip`: out of landscape (`railPortEdge == null`) the strip's landscape measures go to 0, as they already do
when navigating, so the next landscape measure is a change and is reported.

**Tests** (`LandscapeSearchFixesTest`, both windows): `754 ... after a second turn into landscape the bar meets the strip, closed
and open` (ROTATION_90 and 270), which turns portrait, landscape, portrait, landscape. The bar is the strip's height, ends at the
join, and is where the first turn put it, and opening the dropdown leaves it there with the panel flush beneath. **Red before
the fix**, 4 of 4: "the bar is the strip's height after the second turn expected:<36.0> but was:<45.0>". Bar measured
[0,0][390,45] at 780, ROTATION_90. Also `754 ... the open bar stays put` (short and long summary, both rotations): these pass with
and without the fix, so they guard the open state and prove nothing about the cause.

**Not checked on a phone:** the owner's S26 on this build.

## 2. New find in the landscape header (RECORD -753)

"Yes, match Entries (Recommended)". In a short landscape window, Finds puts "New find" in the Journal's L1 row as an icon button
at the row's end (`log/JournalShortWindow.kt`, `ShortWindowNewFindButton`, the Finds leaf, label "New find"), the same way
Entries puts "New entry" there, and draws no floating button. The grid then needs no floating-button clearance. It shows while
Records' Finds chip shows its gallery on the Log tab, as the floating button does: not on Drafts, not on other chips, and not
while a find is open. `FindsGalleryScreen` gains `newFindInHeader`, which tells `JournalTab` whether the button belongs on screen.
`JournalTab` builds the row's action from that, and only the gallery in Records' slot writes it. Portrait is unchanged.

**Tests** (`log/JournalShortWindowCardsTest`, w823dp-h384dp-land): five real touches across the row's button each start a new
find and open its form, and Back returns to the grid with the button back; there is no floating button; the button sits in the
row, at its end. Log only, not on Drafts or Records' default chip. Portrait keeps the floating button. **Existing test changed:**
`in a short window, the L1 switch leaving Records mid-find-edit is an incidental exit` opened the find by `FINDS_FAB_TAG`, which no
longer exists in a short window. It now uses the row's button, with the same assertions.

## 3. The night blend (RECORD -752)

**Cause.** Two candidates are ruled out by measurement. *Not an sRGB dip:* Compose's `lerp(Color, Color)` blends in Oklab. Under
the old code, the drawn luminance ran 0.939, 0.481, 0.153, 0.054, ... 0.011, monotone, never below the dark end (revert R3's
capture). *Recomposition of the whole tree:* confirmed. The old blend handed the theme a new `ColorScheme` on every frame, and
Material's scheme is a static composition local, so every frame recomposed everything under the theme. R3 recorded a composition
per frame (0.98, 0.73, 0.43, 0.26 ... background). **Inferred, not measured:** on the S22 that per-frame cost, together with the
map restyling for night at the same moment, ate the roughly 0.1 s spring, so only one middle frame was drawn. Frame times on the
phone were not measured.

**Fix** (`theme/ColorSchemeFade.kt`, `NightModeBlend`; `theme/Theme.kt`). The change now recomposes once. At the moment night
mode changes, the screen as last drawn is kept as a picture (`GraphicsLayer.toImageBitmap` of a layer recorded every frame), the
scheme switches at once beneath it, and the picture fades out on the same fast spring. The fade is drawing only. The picture is
held for the switch's frame and the next one, so the one heavy frame is spent under it. Under reduced motion the change is
drawn at once, in the same frame. If the picture fails or takes over 250 ms, the change is made at once and logged under
`ForagerNightBlend`. **Rejected:** keeping the per-frame scheme blend (the scheme local is Material's and static, so no screen
can opt out of the recomposition), and a longer blend (more frames of the same cost, and "ceremonial", against -652). The map is
a SurfaceView, which the picture cannot hold, so over the map only the chrome fades and the live map restyles as before.

**Tests** (`theme/NightModeFadeTest`, rewritten, NATIVE graphics, a 240 x 320 dp mdpi window, read back as whole-window
captures): a positive control that the capture reads the light background; at most two compositions over 30 frames, each at one
end; the drawn centre pixel per frame (measured luminance 0.939 x4, then 0.521, 0.181, 0.065, 0.030, 0.019, 0.014, 0.011),
never past either end, at least three frames part-way, monotone, dark within half a second; reduced motion with only the two ends
and dark from the first frame. Robolectric draws only when something reads the window, so the test captures once before the
change, standing in for the frame a phone would have drawn. **Device item:** how it looks on the S22.

## 4. The Maps-return blank (RECORD -752)

**Headless:** `availability/MapsReturnTest`, portrait and landscape. Returning from List by a real nav touch, the map slot is
measured once, at its settled size (1080 x 2340 px portrait, 2340 x 1080 landscape), never at zero, and handed the camera memory
it left, holding the remembered camera from its first measure. This passes with no change, which rules out the screen's side and
proves no fix.

**On the S22** (build 1.0.3107+g5c693fe5, List to Maps 3 times and Journal to Maps once, logcat `ForagerMapsComeback`, four
screenrecords in `~/Zynergy/device-evidence/2026-10-08/`, not in the repo):
- Every return builds a new MapLibre `MapView`. For about 200 ms the map area is the bare app background and the heading reads
  "—" (the compass reading restarts with the tab: `rememberTrueHeading`'s initial `NeedsFix`). Then come one or two black frames
  with only the puck, then tiles.
- The new map's first frames are at MapLibre's default camera (zoom 0.64 at 0,0) with no style. Our restore lands within about
  0 to 20 ms of them ("restore before the style"), the style loads about 17 ms after that, and every later frame is at the
  remembered camera (zoom 12, the right target). View and render size are 1440 x 2988 from the first frame.
- **No magnified frame was seen** in the four recordings. Unverified guess: MapLibre drawing a cached lower-zoom tile scaled up
  while the right tiles load.

**Cause:** the blank is the `MapView` being rebuilt on every return, inside MapLibre. Our camera restore and the screen's sizing
are right. The fixes are either to cover a returning map with a picture of the one that left until the new one renders fully
(B), or to keep one `MapView` alive across tab changes (C). Per the coordinator, that is a stop: the cause was reported and B or C was offered (B, a picture over the returning map, recommended as the smaller), and **no fix is built here**. Item 4 waits on the owner's choice.

**Logging kept** (`map/SightingsMap.kt`, tag `ForagerMapsComeback`): a new map's first 8 rendered frames and its two camera
restores, about 10 lines per return to Maps, nothing otherwise.

## Revert checks

The runner saves each file to a copy, applies the edit, runs only the named classes in a fresh capped Gradle run, refuses results
if the build log has an `e: ` line, reads only JUnit XML written after the run started, and restores from the saved copy (never
from git), confirming the restore is byte-identical. All four compiled, all four restored identical, and `git status` was clean
afterwards.

| # | Edit | Failed | Message |
|---|---|---|---|
| R1 | `JournalTab`: Records' row action always null | 3 of 16 | "could not find any node that satisfies: (TestTag = 'journal-short-new-find')"; "positive control: on Finds' Log" |
| R2 | `FindsGalleryScreen`: floating button in every window | 1 of 16 | "no floating New find button in a short window" |
| R3 | `ColorSchemeFade.kt` and `Theme.kt` as at c0ec942a | 1 of 4 | "every composition is at one end or the other, never a scheme part-way: [... 0.729 ..., 0.431 ...]" |
| R4 | `CompactMapTopStrip`: no reset out of landscape | 4 of 36 | "the bar is the strip's height after the second turn expected:<36.0> but was:<45.0>" |

R3's other three tests pass under the old code. The blend test cannot see frame cost under Robolectric (the old code also drew
smooth frames there), so the composition count is the test that holds the cause. `MapsReturnTest` and the open-state item 1
tests have no revert: there is no fix behind them.

## Full suite

Run 22:48 to 22:57 UTC on the code at `5c693fe5` (every later commit adds only this report and its index row). The results directory was emptied first and only XML written after the start was counted: **4,502 tests in 581 classes, 24 skipped, 0 failures, 0 errors** (Gradle: BUILD SUCCESSFUL). That is 17 more than #206's 4,485: item 1's 12 (6 in each window class: 4 open-state, 2 second-turn), item 2's 3, item 3's net 0 (4 rewritten), and item 4's 2 (MapsReturnTest, portrait and landscape), counted from each class's XML. Two test cases were changed: `JournalShortWindowCardsTest`'s incidental-exit test (item 2), and `NightModeFadeTest`, rewritten (item 3).

## S22 launch check

`scripts/s22-launch-check.sh` on the S22 (SM-S908U, R5CT321008R), the debug APK built from `5c693fe5` (installed versionName `1.0.3107+g5c693fe5`): install Success, `compile -m verify -f` Success, dexopt `status=verify`, cold launch, **PASS**, process alive after 8 s, crash buffer empty. The same install was then used, as the coordinator asked, for item 4's returns to Maps (screenrecords and logcat only; nothing uninstalled or cleared). The phone was left on that build.

## Device only

1. Item 1: S26 and S22 landscape, both rotations, after portrait and back more than once: the bar is the strip's height and ends
   at the join, closed and open.
2. Item 2: Journal > Records > Finds in landscape: "New find" at the row's end. Touching it opens a new find.
3. Item 3: Night mode on and off on the S22: a quick, smooth blend with no grey middle frame. The fade holds for about two frames
   before it starts. Over the map only the chrome fades.
4. Item 4: depends on the decision.

## Gradle

- Gradle started only after `systemctl --user is-active t6b-night` read `inactive` (22:22:32 UTC). PAUSE was touched by me at
  22:16:55 UTC as instructed; I did not remove it or restart T6b.
- Every test run: `systemd-run --user --scope -q -p MemoryMax=5G -p MemorySwapMax=0`, `--no-daemon`, Gradle `-Xmx1536m`,
  Kotlin daemon 2g, Java temp `~/.cache/forager-test-tmp`, then `./gradlew --stop` and the Kotlin daemon stopped.
- The first run (22:23 UTC) ended with exit 143 about a minute into testing and wrote no results. There was no OOM in the
  kernel log, and the cause was not found. The rerun without the night-fade class passed, and the night-fade test window was
  shrunk to 240 x 320 mdpi (its per-frame captures were full xxhdpi windows). Nothing has recurred since.
