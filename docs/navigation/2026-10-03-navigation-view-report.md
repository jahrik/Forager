# The map's navigation view (dispatch 2026-09-28-430, plan task T22)

**Status: built and pushed on `navigation-view`, not merged.** PHONE_STATUS_PLACEHOLDER

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-04.md`, read on `records-after-153` at `d0db4618`. Its rulings on the coder's findings are "Rulings on the coder's findings (continuation 2026-09-28-432)", on `records-after-154` at `ec6ae5cb`. One later ruling, (i) on the Journal's picker, came by message and goes into the dispatch file with the next record entry.
**Base:** `origin/main` at `5b856b6a`, checked against the remote. Way-back route Part 2 (-423) was not merged when this was cut, so this branch does not have it. The two touch the HUD's readout signature, so expect a small merge in `NavigationHud.kt` (both add one parameter to `navigationReadout`).
**The owner's go:** to the planner, "Go on T22", to the step path below. To this session directly, asked because this is a new dispatch: "Yes, start T22".

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-navigation-view/`.

## What this is, in plain terms

While you walk back to your start:

- **The map tilts** about 45° and **turns so the way you face is up**. You are a little below the centre, with more of the map ahead of you, and it follows you.
- **If you drag the map** to look around, it stays where you put it and a **"Return to Route"** button appears at the bottom. Tap it and the tilted, facing-up view comes back. The locate button does the same while you're navigating, and so does a tap on "Reset orientation" (that one turns north-up and still follows).
- **If the compass gets stuck,** the navigation panel says "Compass calibrating…" and the app keeps trying for 15 seconds. If the compass recovers, the panel goes back to normal. If not, the map turns north-up and the panel says "Compass unavailable · north up". Facing-up comes back by itself once the compass recovers.
- **When you stop navigating,** the map goes flat and north-up again.

Separately, and everywhere: **the blue location arrow now points to true north**, not magnetic north. Before this it was about 15° off here.

## The step path the owner confirmed

```
Tap Return
> the map tilts about 45°, turns so the way you face is up,
  and places you a little below the centre; it follows you

Drag the map to look around
> it stops following and stays where you put it
> a "Return to Route" button appears
> tap it > the tilted, facing-up view comes back and follows you again

The compass gets stuck
> a notice: "Compass calibrating…"
> the app keeps trying for about 15 seconds
> if it recovers, the notice goes and the view carries on
> if not, the map turns north-up and the notice says so;
  facing-up comes back by itself once the compass recovers

Stop navigating
> the map goes back to flat, north-up, as it was
```

## Verified before building (reported by message first)

1. **How the map follows today** (`ui/map/SightingsMap.kt`, read at `5b856b6a`):
   - **First activation:** `CameraMode.TRACKING` with `RenderMode.COMPASS` on the puck (`:1597-1598`).
   - **Locate:** re-sets `TRACKING` (`:966-976`; the dispatch's `:960-973` had drifted by a few lines).
   - **Gestures:** a pan drops MapLibre to `NONE` by itself; a gesture is told apart by `REASON_API_GESTURE` (`:566-577`, `:1360-1364`).
   - **The camera memory** is written at each idle (`:596-611`) and restored on a new map's first style (`:697-755`, `:1400-1421`).
   - **What MapLibre 13.5.0's `LocationComponent` offers**, read by `javap` from the pinned `android-sdk-13.5.0.aar`:
     - `CameraMode.TRACKING_COMPASS` and `TRACKING_GPS_NORTH`.
     - `setCameraMode(mode, duration, zoom, bearing, tilt, listener)`.
     - `tiltWhileTracking`, `paddingWhileTracking`, `setCompassEngine(CompassEngine)` and `addOnCameraTrackingChangedListener`.
2. **What "stuck" can mean.**
   - `ui/map/TrueHeading.kt` already reduces the compass to Available, NoSensor, NeedsFix and Unreliable.
   - Unreliable is `domain/CompassTrustJudge.kt`'s existing band: it enters above 15° of the sensor's own uncertainty or on a LOW/UNRELIABLE status, and leaves below 12° held for 2 s.
   - "Stuck" is Unreliable, with no new threshold.
   - **Not caught:** a sensor that silently stops reporting. Nothing in the app measures that, and no new threshold was made for it.
3. **What else moves or reads the camera:** the fan's hold (-380), the bubble, locate, reset orientation, the camera memory, and fullscreen. The rulings below cover each.
4. **Where "Return to Route" goes:** bottom centre, above the attribution caption.
5. **The HUD's needle and north arrow.** The needle is device-relative, so it stays right on a turned map. The north arrow, rotated by minus the heading, shows where north is on a facing-up map; nothing else on screen shows north, since the cluster's orientation icon is a static arrow. No doubling, except the label (ruling E).

### A finding: the location puck pointed at magnetic north

MapLibre's own compass engine (`LocationComponentCompassEngine`) has no `GeomagneticField` call. It is `getRotationMatrixFromVector`, then `remapCoordinateSystem`, then `getOrientation`, so it reads magnetic north. Today's puck uses it (`RenderMode.COMPASS`), so it has pointed at magnetic heading, about 15° off in the Pacific Northwest, on a true-north map, while the compass strip and the HUD read true. Used as it was, `TRACKING_COMPASS` would have turned the map to magnetic heading the same way. **This is inferred from the library's bytecode, not measured on a phone.**

## The rulings (continuation 2026-09-28-432, and (i) by message)

- **A.** The app's true-heading engine for the puck everywhere, not only while navigating. The map outside navigation otherwise stays north-up and flat.
- **(i),** on a question this raised: the Journal's centre-pin picker shows a puck too, so it gets the true heading as well. The Cartography report map has no puck and is unchanged.
- **B.** No rotation sensor at all: north-up at once, with the notice, and no retry window.
- **C.** The bubble re-anchors on every camera move while navigating only.
- **D.** "Reset orientation" while navigating is a move away from the view: north-up, still following, "Return to Route" shown. Locate while navigating does what "Return to Route" does.
- **E.** One place, one word: the HUD's heading label carries the notice. No separate map notice.
- **F.** The user's zoom is left as it is.
- **Accepted as proposed:**
  - `TRACKING_COMPASS` with a 45° tilt and top padding.
  - Re-applying the view after a camera-memory restore.
  - "Following broken" held above the tab.
  - The padding recomputed on resize.
  - The pill at the map-chrome 80%, with the snackbars lifted above it.

## What was built

- **`ui/map/NavigationView.kt`** (new):
  - **The figures and words, each named once:** `NAVIGATION_VIEW_TILT_DEGREES = 45.0`; `NAVIGATION_VIEW_TOP_PADDING_FRACTION = 0.25`, which puts the walker an eighth of the map's height below the centre; `NAVIGATION_COMPASS_RETRY_MILLIS = 15_000`; "Compass calibrating…" and "Compass unavailable · north up".
  - **`NavigationFacingJudge`:** stuck means Unreliable, retried 15 s, then north-up, and back the moment the heading is Available. NoSensor is north-up at once. NeedsFix is not a compass fault.
  - **`rememberNavigationFacing`:** judged on every heading change, and on a 250 ms tick so the window ends on time with no new reading.
  - **`NavigationViewRequest`:** facing, following, a restore count and `onLeftView`. Built so T8 can turn it on by the same predicate, `AvailabilityScreen`'s `isNavigating`.
  - **`TrueHeadingCompassEngine`:** MapLibre's `CompassEngine`, fed the app's true heading. While the heading isn't available it hands over nothing, and the last heading stands.
  - **`NavigationModeChange`:** the camera-mode changes, made only through MapLibre's tracking API, because a camera move from the app ends tracking.
    - Facing-up is `TRACKING_COMPASS`.
    - Calibrating is `TRACKING`, with the bearing holding.
    - North-up is `TRACKING_GPS_NORTH`.
    - All three are tilted 45° with the top padding. Leaving navigation is `TRACKING` with bearing 0 and tilt 0 and no padding, or, when not following, one eased camera move to flat and north-up.
    - Its one tracking listener reads any mode change the app did not make, while following, as the user moving away. A drag ends tracking; a rotate gesture ends the compass's hold. A pinch to zoom keeps MapLibre tracking and is not a move away.
  - **`MapCompass` and `LocalMapCompass`:** see "The puck, everywhere".
- **`ui/map/SightingsMap.kt`:**
  - The navigation view, applied while navigating and following: on Return, on a restore request, on a facing change, after a style load or a tab's camera restore, and on a height change. When navigation stops, it leaves the view.
  - "Reset orientation" while navigating and following does north-up, keeping the follow, and reports the move. The map's own first run of that effect is not a tap, and keeps its old behaviour.
  - The bubble re-anchored on every camera move while navigating (ruling C).
  - The app's compass engine installed at each activation (the first one, every style swap, and locate).
- **`ui/map/MapSlot.kt`:** `MapRenderMode.navigationView` and `MapRenderMode.trueHeading`, passed through.
- **`ui/map/TrueHeading.kt`:** an overload taking the fix as a `State`, so a map can make its heading without reading the fix in its composition. The existing function now delegates to it, with the same keys and the same body.
- **`ui/map/MapKeepOut.kt`:** a keep-out id for the pill.
- **`ui/availability/ReturnToRoutePill.kt`** (new): content-width, laid out at least 48 dp tall, with its fill at `MAP_CHROME_OVER_MAP_ALPHA` and its content opaque, in the snackbar's colour roles.
- **`ui/availability/AvailabilityScreen.kt`:**
  - "Following broken" and the restore count, held above the tab with `rememberSaveable` (as the legend's flag is), and cleared when navigation stops.
  - `LocalMapCompass` provided to every map under the screen.
- **`ui/availability/AvailabilityCompactScaffold.kt`:** the four values passed on; the snackbar lifted above the pill while it shows.
- **`ui/availability/AvailabilityCompactMapUi.kt`:**
  - The facing.
  - The request in the map's render mode, while navigating only, plus the true heading always.
  - Locate while navigating routed to "Return to Route".
  - The pill.
  - The facing handed to the HUD.
- **`ui/availability/NavigationHud.kt`:** a `facing` parameter. While calibrating the heading label reads "Compass calibrating…", and once north-up "Compass unavailable · north up" (ruling E). Otherwise it is unchanged.

### The puck, everywhere (ruling A and (i))

- **The Maps tab's map** is handed the one true heading the strip and the HUD read.
- **The other maps** with a puck under `AvailabilityScreen` make their own from `LocalMapCompass`: the compass, declination, and the live fix as a `State`. There are two, both drawn by `CentrePinMap`: the Journal's centre-pin picker and the offline-maps region picker. (The add-action picker over the Maps tab draws no map of its own; it sits over the Maps tab's.)
- **A map makes its heading only while it is composed,** so the compass is not read on a tab with no map.
- **A map outside `AvailabilityScreen`** keeps MapLibre's own engine. There is none today: the three `MapRenderMode` builders are under it, and the Cartography report map has no puck.
- **A deviation from the ruling's wording, said plainly:** the planner ruled "parameters through the edit flow only". This uses one value provided once on the screen instead of new parameters through `JournalTab`, `CentrePinLocationPicker` and `CentrePinMap`. No Journal composable changed. The touched call sites are `AvailabilityScreen.kt` (the provider) and `SightingsMap.kt` (the reader); the picker call sites (`JournalTab.kt:469`, `CentrePinLocationPicker.kt:122` and `:207`, `AvailabilityOfflineMapsUi.kt:176`) are unchanged.

## Tests

New:
- **`ui/map/NavigationViewTest`** (5):
  - 15 s, then north-up, then back on recovery, with a second stretch getting its own window.
  - Recovery inside the window clears it.
  - No sensor is north-up at once; no fix is not a fault.
  - The engine hands over the true heading and nothing while it is not available.
  - The below-centre padding.
- **`ui/availability/AvailabilityScreenNavigationViewTest`** (11), on the real screen with a stand-in map that records its `MapRenderMode` and counts real long-presses:
  - Asked for with Return, facing up and following, and not before or after Stop.
  - Every map is handed the true heading.
  - A move away shows "Return to Route", and real touches at five points across it each bring the view back (one per move away).
  - Long-presses beside the pill reach the map.
  - Locate while navigating, by a real touch.
  - A move away survives a tab round trip, by real touches on List and Maps.
  - Stop clears a move away.
  - The stuck-compass path end to end: "Compass calibrating…", still retried at 14 s, north-up at 16 s with the label, and facing-up again after the trust judge's 2 s of good readings.
  - No compass at all.
  - A snackbar sits above the pill.
  - The pill's fill at 80%.
- **`NavigationHudReadoutTest`:** one test of the two labels.

Existing assertions changed, by ruling E, in `AvailabilityScreenMapIconStackTest`, each noted in the test:
- "with no compass sensor the HUD shows the distance and no bearing text": the heading label "Compass unavailable" → "Compass unavailable · north up".
- "an untrusted heading on the HUD withholds the needle and its text and reads Compass unreliable once": the HUD's heading label "Compass unreliable" → "Compass calibrating…", in four assertions, the node count included. The name is kept. The strip's own "Compass unreliable", outside navigation, is unchanged.

A fault the tests found, fixed before any run was cited:
- **The fault:** the facing was first judged only on its 250 ms tick, so for up to a quarter-second the HUD would have shown "Compass unreliable" before "Compass calibrating…".
- **How it was found:** the untrusted-heading test passed unchanged where ruling E said its label should change. The test had never let the tick run, so it was a check that could not see the data that could fail it.
- **The fix:** the facing is now also judged at once on every heading change, and that test now fails on the old word, as it should.

Nothing skipped, silenced or weakened. The 19 skipped in the touched classes are `AvailabilityScreenMapIconStackTest`'s existing `@Ignore`s.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD (`37402e19`) after each check.

All 16 compiled with 0 errors, and each failed for its own edit. The four whose failure has no message (n08, n11, n12, n13) were traced to their line in the XML's stack trace, each the assertion its edit targets.

| Check | One edit | Fails with |
|---|---|---|
| n01 | the retry window 100× longer | "15 s after it became unreliable expected NORTH_UP but was CALIBRATING"; the screen's label still "calibrating" |
| n02 | no sensor retried | NORTH_UP expected, CALIBRATING (judge and screen) |
| n03 | recovery does not clear the window | "only 14 s into the second stretch" read NORTH_UP |
| n04 | the engine hands over any heading | "[95.0] but was [95.0, 0.0, 0.0, 0.0]" |
| n05 | no "calibrating" label | "Compass calibrating…" was "Compass unreliable" |
| n06 | no "north up" label | "Compass unavailable · north up" was "Compass unavailable" / "Compass unreliable" |
| n07 | the view asked for when not navigating | "not navigating … expected null" |
| n08 | always following | `assertFalse(view()!!.following)`, lines 218 and 281 |
| n09 | no pill | "move 0 shows the pill"; the pill not found (4 tests) |
| n10 | pill's tap not wired | "touch 0 … hid the pill" |
| n11 | locate plain tracking | `assertTrue(view()!!.following)`, line 266 |
| n12 | a tab change resets the move away | `assertTrue(pillShown())`, line 280 |
| n13 | Stop keeps the move away | `assertTrue(view()!!.following)`, line 293 |
| n14 | no true heading handed to the map | "not navigating" (null) |
| n15 | pill solid | alpha 0.8 expected, 1.0 |
| n16 | snackbar not lifted | "the snackbar's bottom 560.0.dp is at or above the pill's top 480.0.dp" |

**Not revert-checked, because nothing headless reaches it:** every `SightingsMap` change. That covers the camera modes, the tilt and padding, the tracking listener's reading of a move away, "Reset orientation" while navigating, the bubble on camera move, the compass engine's installation, and the pickers' heading from `LocalMapCompass`. A real MapView cannot run under Robolectric, so these are the phone's. The stand-in map stands in for the listener's call to `onLeftView`; it does not test it.

## Suite

FULL_SUITE_PLACEHOLDER

## What Robolectric cannot show here, said plainly

- **The map's real tilt and rotation,** and MapLibre's camera modes.
- **The puck's direction.**
- **The compass on a phone.**
- **Real window insets,** so where the pill sits above the nav and caption on a phone.

PHONE_SECTION_PLACEHOLDER
