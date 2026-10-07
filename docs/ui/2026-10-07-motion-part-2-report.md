# Motion Part 2: code and tests written, nothing compiled yet

Dispatch 2026-09-28-666 (RECORD intent -666; preserved at `prompts/preserved/2026-10-07-10.md` on branch `records-after-173`).
The owner's choices: RECORD -651, with Part 1's amendments -657 and -659. The scout: `docs/ui/2026-10-07-motion-scout.md` on
branch `motion-scout` (8a40c6d4). Branch `motion-part-2`, cut from `origin/motion-part-1` at `cdf0875b`, the base the dispatch
names (checked against the remote before starting). Part 1 is not merged; nothing here is on `main`. No PR.

**Status: written, not run.** The dispatch holds Gradle for the planner's go. Nothing on this branch has been compiled, no test
has run, and no revert check has been done. Every claim below about behaviour is what the code is written to do, not something
observed. Part 1's own code, which this builds on (`LeavingTakesNoTouches` above all), is also uncompiled.

## What was built

Paths are relative to `app/src/main/java/com/zynergylabs/forager/app/ui/`.

1. **Tabs (S1, S2), "Quick crossfade".** `motion/TabCrossfade.kt`: `TabCrossfade`, an `AnimatedContent` with a fade and no size
   transform, wraps the scaffold's tab `when` (`availability/AvailabilityCompactScaffold.kt:1004`). Every route between tabs
   changes `compactTab`, so View on Map, Navigate, Back to Maps and the rest all fade. The outgoing tab is held at the constraints
   and screen position it had (`holdWhileLeaving`) and takes no touch (`leavingTakesNoTouches`) until it has gone, so the live map
   is **not measured again** when the bottom bar or the landscape rail comes back as Maps leaves. Instant under reduced motion.
2. **Bottom bar (S3, S4), "Fade with the tab".** `TabChromeFade` (same file) on the Scaffold's solid bar (`:835`) and on the
   landscape rail beside the content (`:864`, `:1544`). It fades on the tab's spec; the Maps tab's own 80% bar is inside that tab
   and fades with it, so the fill passes between solid and 80%. Its room in the layout still comes and goes at once: leaving for
   Maps it reports no height (no width for the rail) and is drawn where it was, taking no touch. Turning the phone plays no fade
   (keyed on the window's shape and the port edge). The stale comment the dispatch names at `:590` is at `:601` on this base and
   now says the rail slides in fullscreen.
3. **Navigation start and stop (N1, N2), "Move with the map".** `availability/AvailabilityCompactMapUi.kt:900-1080`: the strip
   and the navigation display are each in an `AnimatedVisibility`, clipped at their own top edge, sliding up and out or down and in
   by their own height, with a fade, on `MotionTokens.navigationViewChromeSpec`. A leaving panel takes no touch and gives up its
   keep-out (`TOP_STRIP`, which both register) and the strip its measured height at once. Fade only under reduced motion.
4. **Pop-ups over the map, "Fade and grow".** `motion/MapPopUp.kt`: `MapPopUp` fades on `mapPopUpFadeSpec` and grows from a pivot
   on `mapPopUpGrowSpec`, from `MAP_POPUP_ENTER_SCALE` (0.85, chosen). **The grow is drawn only** (`drawGrow`), so a pop-up is laid
   out, and takes touches, at its settled size from its first frame; AnimatedVisibility's own `scaleIn` would have moved its touch
   area with the drawing. Fade only under reduced motion. Applied to:
   - the bubble (M5), from its glyph (`map/MapBubble.kt:288`), every map host's bubble, since the layer is shared;
   - the centre pin and its OK/Cancel row (M9), the pin from its tip and the row from its bottom edge
     (`CentrePinLocationPickerPopUp`, `map/CentrePinLocationPicker.kt:336-350`, used by the Maps tab at
     `AvailabilityCompactMapUi.kt:1331` for both of its uses; the other screens' pickers keep the plain overlay);
   - Return to Route (N3), from its bottom centre (`:1113`);
   - each chip (M1), from its top centre (`:1029`, `:1032`), the row staying composed until its last chip has gone;
   - the legend (M2), from its bottom-end corner (`:1151`);
   - the strip's sundown line (C1), which grows its strip by layout (`AvailabilityMapControlsUi.kt:590`), since the strip's own
     background has to grow with it. The strip takes no touch outside its coordinates, so this moves no touch.
   The Back by line is not on this branch and is left to a later merge, as the dispatch says.
5. **Taps on a leaving pop-up, "Let taps through at once".** Every pop-up above, the strip and the HUD when they leave, the
   outgoing tab and the leaving bar or rail use Part 1's `leavingTakesNoTouches`. Each leaving piece also drops its fan keep-out at
   once.
6. **Words that change, "Numbers instant, words fade".** `motion/WordSwap.kt`: `WordSwap` crossfades a line on `wordSwapSpec`
   when its words change and draws it at once when only its numbers change (`wordsOf` reduces every number to one mark and keys
   the crossfade on the rest). Applied to the strip's heading, elevation, position note and sundown line, and the HUD's heading,
   target, distance, status and sundown lines.
7. **Snackbar heights (S6).** `glidingSnackbarBottom` (`AvailabilityCompactScaffold.kt:708`, `:1583`): on the Maps tab the
   snackbar's bottom offset (the nav's height, or the system bar's in fullscreen and the rail layout, plus the Return to Route
   lift) glides on `navigationMotionSpec`, the attribution insets' spec. A tab change makes it jump, because the Scaffold's own
   placement changes at once then. Instant under reduced motion.

New tokens (`motion/MotionTokens.kt`): `tabCrossfadeSpec` (fastEffects), `mapPopUpFadeSpec` (defaultEffects), `mapPopUpGrowSpec`
(defaultSpatial), `MAP_POPUP_ENTER_SCALE`, `navigationViewChromeSpec` (slowSpatial), `wordSwapSpec` (fastEffects). No `tween` or
`spring` at a call site. `docs/motion-spec.md` §2 and §4 gain the matching rows.

## Verified before building, and how

By reading this branch at `cdf0875b`, unless said otherwise:

- `origin/motion-part-1` was `cdf0875b` when the worktree was cut; `back-by` (29074703) is not an ancestor of `origin/main`
  (`git merge-base --is-ancestor`), so no Back by line exists to animate here.
- The map must not re-measure: the scaffold says so of the search bar's slide and the attribution insets
  (`AvailabilityCompactScaffold.kt:1098-1109` and `:978-988` as of `cdf0875b`) and the HUD says so of itself
  (`NavigationHud.kt:113`).
- The tab switch was a plain `when (compactTab())` (`AvailabilityCompactScaffold.kt:960` at `cdf0875b`), and every app-made tab
  move goes through `compactTab`.
- The solid bar lives in the Scaffold's `bottomBar` slot and the Maps bar inside the Maps tab (`:811`, and
  `AvailabilityCompactMapUi.kt:1118-1141`); the Scaffold's content padding depends on that slot's reported height (the comment at
  `:799-808`). This is why the leaving bar reports no height rather than fading in its slot.
- The strip and the HUD both register `MapKeepOutIds.TOP_STRIP` (`AvailabilityCompactMapUi.kt:913`, `:1028` at `cdf0875b`), and
  `mapKeepOut` removes its id on dispose (`map/MapKeepOut.kt:45-51`): with both composed during a crossover, the leaving one's
  dispose would erase the other's entry. Hence a leaving panel drops its keep-out at the start of its exit.
- The compass strip is a plain `Box`, not a `Surface`, and takes no touch outside its coordinates (`AvailabilityMapControlsUi.kt`,
  the comment before its `Box`), so growing it by layout moves no touch.
- The navigation view's camera changes take 750 ms (`map/NavigationView.kt:316`), and `scripts/verify-design-tokens.sh` check 3
  fails on any `tween(` under `ui/`, motion included, with ADR-0002 behind it.
- The map is a MapLibre `MapView` made with no options (`map/SightingsMap.kt:316`). That its default render surface is a
  `SurfaceView` is **inferred** from MapLibre's defaults, not read from the jar in this session.

## Premises that were wrong or incomplete

- **"Fix the stale comment at `:590`."** The comment is at `:593` on this base (Part 1 moved it), now `:601`. It was stale as the
  scout said: the Maps rail slides (`AvailabilityCompactMapUi.kt:1156-1160` at `cdf0875b`).
- **"Timed to the map's tilt" and "use the tokens, no raw tween".** These cannot both hold exactly: the tilt is a 750 ms MapLibre
  duration, and a spring has no duration. Built on the scheme's slowest spatial spring as the nearest (stop 1).
- **The tab crossfade and a deliberately instant item.** The scout's list of things that stay instant includes the search field
  "and its slot on Maps", which "appears and vanishes on purpose" (an animated exit leaves the field focusable). A crossfade keeps
  the outgoing Maps tab composed, search bar included. Handled by emptying the Maps tab's search slot the moment Maps starts to
  leave, so that bar still goes at once while the rest of the tab fades (`AvailabilityCompactScaffold.kt:1164-1169`). Reported
  because it is a judgement inside a recorded decision, not a free choice.
- **The scout's M5 code reference** (`map/MapBubble.kt:279-341`) is the shared bubble layer, so the bubble's fade and grow applies
  to every map host's bubble (the entry report's fullscreen map and the tablet's map too), not only the Maps tab's.

## Stops for the owner (not decided here)

1. **Navigation timing.** The strip and the HUD move on the motion scheme's slowest spatial spring, which approximates but does
   not equal the map's 750 ms tilt. Keep the spring (judged on the S22), or allow one timed animation for this case, which means
   an exception to ADR-0002 and to design-token check 3?
2. **The HUD's own sundown line (scout N6).** Not in the owner's pop-up list ("the strip's sundown and back-by lines"), so it still
   appears and goes at once (its words do crossfade). Fade and grow it as the strip's does?
3. **The strip swapping whole between readout, "Location services unavailable" and the position note (scout C2).** Left
   instant: it is a change of layout, not of words. Crossfade it with the words, or leave?
4. **The coordinates' MGRS/decimal swap on tap (C5).** Left instant: wrapping its text would change its tap box for the length of
   a crossfade (a touch-area change). Leave?
5. **Back during a tab fade.** The outgoing tab's Back handlers (the Journal's, the bubble's and the fan's) stay composed for the
   length of the fade (about a fast effects spring). A Back pressed in that moment could be taken by the leaving tab. Leave it, or
   switch the leaving tab's handlers off?
6. **Two maps for a moment.** Going from Maps to a Journal page with its own entry map, both maps are composed for the length of
   the fade, each its own `MapView`. The cost is the S22's (RECORD -651 already says the S22 decides whether the map is kept
   through the fade); if it is too high, the fallback is to drop the Maps tab at once while the rest still fades.
7. **A forecast cell's bubble (scout M7).** It shows nothing until its cell is read; if the read outlasts the grow, that bubble
   still appears at once. Not in the owner's list; leave?
8. **During a grow, a touch lands where the control settles, not where it is drawn.** This keeps every touch area exactly as
   before (the dispatch: any touch-area change is a stop), but for the first part of a grow the drawn control is slightly smaller
   than its touch box. The alternative, touch following the drawing, is a touch-area change. Confirm the drawn-only grow.

## Decided beyond the dispatch's words (my calls, open to reversal)

- The strip and the HUD **fade as they slide**, since a clipped slide alone leaves them cut by their clip edge for a moment.
- **Reduced motion:** the tab change and the bar's fade are instant (no tab is kept); pop-ups, the strip's line and the navigation
  panels fade without growing or sliding; the snackbar jumps; word swaps keep their crossfade, as Part 1's icon swap does.
- The chips **each** fade and grow, not the row as one, so the second chip appearing beside the first also grows.
- The legend keeps the icon cluster clear of it until it has gone, so the cluster does not glide over a legend still fading.
- The snackbar offset uses the system bar's raw inset in fullscreen and the rail layout, as the attribution button already does,
  where it was a `windowInsetsPadding`. These differ only if something above has consumed that inset: device-only either way.

## Tests written (none run)

Under `app/src/test/java/com/zynergylabs/forager/app/ui/`, every touch a real coordinate touch:

- `availability/MapPopUpMotionTest.kt`, through `AvailabilityScreen`:
  - Return to Route made to leave by a real tap on it, caught mid-leave: a long-press at each of five points across where it was
    reaches the map and does not fire it again; mid-grow, touches at five points across its settled bounds reach it; under reduced
    motion it does not grow and still lets the long-press through as it leaves.
  - The bubble closed by its close button, caught mid-leave: a long-press on its card reaches the map; mid-grow, its close button
    takes a real touch where it settles; no grow under reduced motion.
  - The centre pin's row cancelled, caught mid-leave: a long-press on its OK reaches the map and opens no dialog; mid-grow, OK takes
    a real touch where it settles and opens the trip's date dialog.
  - Starting navigation, caught mid-change: the display is above where it settles (sliding down) and a long-press on the leaving
    strip reaches the map; under reduced motion the display is already in place.
  - The snackbar caught mid-glide between its height without and with Return to Route; under reduced motion already there.
- `availability/TabCrossfadeTest.kt`, with a map stand-in that records every set of constraints it is measured with: leaving Maps,
  the map is measured with nothing but its own constraints while it fades and the solid bar is part way in; mid-fade, a touch on the
  List tab's bare background does not reach the leaving map; coming to Maps, the map has one size from first measure to last while
  the solid bar fades out; under reduced motion the change is instant.
- `availability/StripSundownLineGrowTest.kt`: the strip caught growing and shrinking between its two heights; at once under
  reduced motion.
- `motion/WordSwapTest.kt`: `wordsOf` on real lines; a number-only change shows one new line at once; a word change shows both
  lines mid-crossfade, then the new one.
- `motion/MotionTokensTest.kt`: the five new categories map to their scheme specs; effects ones are critically damped.

Reduced motion in these tests is the **transition scale alone at 0**, so Compose's own animations still run (they follow the
animator scale) and what changes is the app's reading of the setting.

**Not covered by a test:** the landscape rail's fade and the outgoing tab kept in place when the rail appears (the placement shift
in `holdWhileLeaving`); the chips' and legend's tap-through (the same `MapPopUp` as the three tested, not driven separately); the
HUD's word swaps (the strip's are not driven either; `WordSwap` is tested on its own).

**Existing tests at risk, untouched:** any test that asserts something has gone, or counts nodes by tag or text, in the same frame
as the change that removes it, without advancing the clock: tab changes, Return to Route, the bubble, the centre pin's row, the
strip and the HUD now leave over a few frames, and while a word crossfades or a tab fades there are two nodes with the same tag or
text. Tests that settle (most of the ones read here, for example `AvailabilityScreenNavigationViewTest`'s `settle()`, 1 s) should
be unaffected, provided `slowSpatialSpec` settles inside a second, which is not checked.

**Revert checks planned** (each from a saved copy, the build log checked for compile errors first, and the forward change
confirmed present after): drop `leavingTakesNoTouches` from `MapPopUp` (the three mid-leave long-press tests must fail on the
map's count); replace `drawGrow` with a graphics-layer scale (the mid-grow corner touches must fail); remove `holdWhileLeaving`
from `TabCrossfade` (the held-map test must fail on a new set of constraints); put the leaving bar back in its slot with its
height (the coming-to-Maps test must fail on two sizes); key `WordSwap` on the whole text (the number-only test must fail on two
lines); make the strip's line appear without `expandVertically` (the grow test must fail on "caught growing").

## Unverified because nothing has been compiled

Everything, and in particular: that it compiles (`ColumnScope`/`RowScope` overloads of `AnimatedVisibility` resolving as meant;
`if … else { }.clipToBounds()` applying to the whole `if`, as the file's existing `}.mapKeepOut(...)` relies on); that
`AnimatedContent` with a `contentKey` updates a line in place without a crossfade; that `holdWhileLeaving` keeps the map from
being measured (`AnimatedContent` passing its constraints through); that the Scaffold accepts a zero-height bottom bar with the
same content padding as an empty slot on the Maps tab; that the long-press in the tests fires on the main clock with the
auto-advance off; Part 1's `leavingTakesNoTouches` itself; and every look and feel item below.

## Device checks (S22, after the build)

The tab crossfade to and from Maps in portrait and landscape, and its cost with the map kept through the fade (RECORD -651:
"checked on the S22 before it is kept"), including whether a `SurfaceView`-backed map fades at all under a Compose alpha; the bar's
fill passing between solid and 80%; the strip and the HUD against the map's tilt on Return and on Stop; each pop-up's fade and
grow, in light and dark, and a tap on each as it leaves; word crossfades on the strip while turning the phone (a compass point
changing near a boundary could crossfade repeatedly); the snackbar's glide on fullscreen and on Return to Route; all of it again
with the phone's animations turned off.

## Amendment 1 (RECORD -672), applied 2026-10-07, also not compiled

The owner's answers to the stops above, and what changed for each. Still code and tests only.

1. **Stop 1, "Allow one exception".** `MotionTokens.navigationViewChromeSpec` is now a tween of exactly
   `NAVIGATION_VIEW_TRANSITION_MILLIS`, read from `map/NavigationView.kt` (made `internal` there; not copied), with Material's
   standard easing (chosen: MapLibre's camera curve was not read). It carries both the slide and the fade of the strip and the
   navigation display, so they are one timed animation; under reduced motion they still fade alone on the pop-ups' fade. Recorded
   in `docs/motion-spec.md` §2 and in ADR-0002's new amendment section, with the reason and the rejected alternative.
   `scripts/verify-design-tokens.sh` check 3 allows that one line, by file and exact text, with a reason comment.
   `MotionTokensTest` holds every other category to "no TweenSpec" and this one to the constant's duration.
   **Check 3 shown still to fail** (run as a plain script, no Gradle), each time with the planted line named in its output: a tween
   in another file; a second tween in `MotionTokens.kt`; the allowed text copied to a second line of `MotionTokens.kt`. Each plant
   was undone from a saved copy, and `MotionTokens.kt` was confirmed byte-identical to its saved copy by checksum. Unplanted,
   check 3 lists the same 8 hits as at `cdf0875b` and not the allowed line.
   **Found on the way (not fixed):** the first plants, written `tween<Float>(300)`, passed check 3. Its pattern `tween(` does not
   match a tween written with explicit type arguments, so the check is blind to that form. Reported for the hygiene sweep's
   "blind checks" list rather than widened here. Its 3 false positives on `metersBetween(` remain as before.
2. **N6.** The navigation display's sundown line fades and grows like the strip's (`availability/NavigationHud.kt`, by layout,
   fade alone under reduced motion). Test: `availability/HudSundownLineGrowTest.kt`.
3. **C2.** The strip's swap between the readout, "Location services unavailable" and the position note crossfades
   (`AnimatedContent` keyed on which of the three, on the word swap's spec; the outgoing one keeps what it showed and takes no
   touch). The width the three used to take by weight is on the crossfade's box. Test: a new case in
   `availability/StripSundownLineGrowTest.kt` (both on screen mid-change, then the no-fix line alone).
4. **C5.** The coordinate tap stays instant. No change.
5. **Back during a tab fade.** From its first leaving frame the leaving tab is handed Back dispatchers nothing presses, so its
   handlers stop. activity-compose 1.13's `BackHandler` uses the navigation-event owner when there is one and the
   OnBackPressed owner otherwise (read from its bytecode, `BackHandlerKt`), so both are provided (`motion/TabCrossfade.kt`).
   `navigationevent-compose` is an API dependency of activity-compose 1.13.0 (its Gradle module file), so it is on the compile
   classpath. Test: in `MapPopUpMotionTest`, a bubble open on Maps, List tapped, Back pressed mid-fade: the screen goes back to
   Maps (the arriving side's Back). Before the change the leaving bubble's handler would have taken it and the screen stayed on
   List.
6. **M7.** What a tapped thing shows is resolved before the pop-up (`map/MapBubble.kt`), and the bubble is shown only once there
   is content, so a late forecast cell fades and grows when it arrives. **Not tested:** driving a slow forecast read through the
   real feed was beyond what I could set up without running anything.
7. **The drawn-only grow:** confirmed. No change.
8. **Two maps:** kept as built. The fallback is one constant, `KEEP_MAP_THROUGH_TAB_FADE` (`AvailabilityCompactScaffold.kt`).
   Set to `false`, it drops the leaving Maps tab at once while the arriving tab and the bar still fade.
