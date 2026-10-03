# The map zooms to a set level when navigation starts (dispatch 2026-09-28-440, T22 follow-up)

**Status: built and pushed on `navigation-zoom`, not merged. No phone step:** the camera itself is for the owner's next walk.

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-06.md`, with Amendment 1 (the zoom cap).
**Base:** `origin/main` at `bb0b559e`, checked against the remote. `main` at `09e43bd3` (PR #158, docs only) was merged in at `1ee92281`.
**The owner's go,** to this session directly: "Yes, start -440".
- **On the cap,** the owner gave two answers minutes apart: "Take 17 on topo (Recommended)" in this window, and "Zoom in past the cap" in the planner's.
- Asked here which stands, they chose "Zoom past the cap". That is Amendment 1, and it is what is built.

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-navigation-zoom/`.

## What this is, in plain terms

1. **Tap Return:** as the map tilts and turns, it now also zooms in to street level, about two blocks ahead. On every map, the topo and satellite maps included: for those, the map enlarges its closest tiles, so the view looks softer, and nothing extra is downloaded.
2. **Pinch:** the map keeps your zoom and keeps following.
3. **Drag, then "Return to Route":** following comes back at your zoom, not the set one.
4. **Stop:** flat and north-up, and back within the map's usual zoom limit.
5. **A new Return** starts at the set zoom again.

## Verified before building (reported by message first)

1. **Where the zoom belongs.**
   - `applyView` (`ui/map/NavigationView.kt:277`) has one caller, the navigation effect in `ui/map/SightingsMap.kt` (`:1037`). That effect re-runs on Return, every facing change, "Return to Route", a style load or a tab's camera restore, and a height change. So the zoom cannot simply live inside it.
   - A first-apply flag on the map side would not do either: a tab change builds a new map, which would zoom again.
   - Accepted: a "start zoom pending" flag held by the screen above the tab, cleared when the map reports the zoom applied.
2. **"About two blocks ahead" as a zoom.**
   - The working is for the S22's map (about 797 dp tall) at 45.4° N. MapLibre's default 36.87° field of view gives a focal length of 0.5 × 797 / tan 18.43° = 1195 dp. With the 45° tilt and the quarter-height padding, the walker sits at 62.5% of the height.
   - Ground ahead = 0.7071 × (tan(45° + α) − 1) × 1195 × metres per dp:

| Zoom | To the HUD's lower edge (about 130 dp from the top) | To the map's top |
|---|---|---|
| 17 | about 316 m | about 500 m |
| 18 | about 158 m | about 250 m |
| 18.5 | about 112 m | about 180 m |

   - `NAVIGATION_VIEW_ZOOM = 18.0`, provisional. Arithmetic, not measured on the phone.
3. **MapLibre (pinned 13.5.0, from the bytecode).**
   - `LocationComponent.zoomWhileTracking` exists, and is refused while a mode transition runs, as tilt and padding are. So it goes in the same transition listener.
   - `LocationCameraController` registers move, rotate and fling listeners and no scale listener, so a pinch keeps tracking (inferred; device-only).
   - The tracking modes keep the camera's zoom.
4. **What else zooms on Return.** Only the first activation's ease to 16 (`SightingsMap.kt:1679`). As -433 recorded, it is an app camera move that ends tracking, so Return starts from not following, the mode transition runs, and the start zoom lands after it. No conflict.

**The finding that became Amendment 1:**
- The map caps its zoom at each basemap's operating limit (`SightingsMap.kt:742`, `setMaxZoomPreference(basemap.maxZoom)`): Street 19, Topo 17, Satellite 15.
- Zoom 18 would have been clamped on Topo, the owner's map, to about four blocks ahead.

## What was built

- **`ui/map/NavigationView.kt`:**
  - **`NAVIGATION_VIEW_ZOOM = 18.0`,** with the working.
  - **`navigationMaxZoom(basemap, navigating)`:** the basemap's own limit, raised to 18 while navigating (a higher limit is kept).
  - **`NavigationViewRequest`** gains `zoomOnStart` and `onStartZoomApplied`.
  - **`applyView`** applies `zoomWhileTracking(startZoom)` in the mode change's transition listener, after the tilt and padding. It reports the zoom applied when the zoom animation ends.
  - **`leaveNavigation`** eases back flat, north-up, and within the basemap's own cap. It restores that cap when the ease ends, because setting it first would jump.
  - **The camera log line** now records the zoom too.
- **`ui/map/SightingsMap.kt`:**
  - While navigating, the effect sets the camera's cap to `navigationMaxZoom(basemap, true)` on every run, since a style load puts the basemap's own cap back.
  - It passes the start zoom to `applyView` while one is pending, and leaving hands `leaveNavigation` the basemap's own cap.
  - `zoomOnStart` is one of the effect's keys.
- **`ui/availability/AvailabilityScreen.kt`:**
  - `navigationZoomPending` is held with `rememberSaveable` above the tab. It is set when navigation starts, cleared when the map reports the zoom applied, and cleared when navigation stops.
  - The start is detected against a saved "was navigating" flag, so a configuration change mid-navigation does not read as a new start.
- **`AvailabilityCompactScaffold.kt` and `AvailabilityCompactMapUi.kt`:** pass the flag and the callback into the request.

**The tile sources' own limit does not move, and this is how it is known.** The style's raster sources take their `maxzoom` from `Basemap.maxZoom` in `styleJsonFor` (`ui/map/BasemapStyles.kt`). That function takes no navigation input, and the existing `BasemapStyleTest` "the style's declared maxzoom matches Basemap's own operating limit" holds every basemap to it. Only the camera's limit (`setMaxZoomPreference`) is raised. MapLibre therefore never requests a tile beyond the source's `maxzoom`, and enlarges the deepest one. How that looks is device-only.

**One edge, said plainly.** If the walker drags the map away within the moment before the start zoom is applied, the zoom is still pending, and is applied when "Return to Route" brings the view back.

## Tests

**Tests first, pushed failing** (`eeb81afd`, against a stub that never asked for the zoom and never raised the cap): 3 of the new tests failed (`t1-tests-first`). The three that expect no zoom request (facing change, "Return to Route", tab change) passed trivially; each is bitten by revert z03 below.

New:
- **`NavigationViewTest`:** while navigating, the camera may reach 18 on every basemap, and not otherwise (Topo 17 → 18, Satellite 15 → 18, Street stays 19).
- **`AvailabilityScreenNavigationViewTest`** (6):
  - Starting navigation asks once for the set zoom, and not again once the map has applied it.
  - A facing change does not ask again.
  - "Return to Route" does not ask.
  - A new navigation after Stop asks again.
  - A configuration change mid-navigation does not ask again (`StateRestorationTester`).
  - A tab round trip does not ask again.
- **The harness change:** `setScreen` takes an optional `StateRestorationTester`. No existing test changed.

**Device-only:** the camera calls themselves (`zoomWhileTracking`, the raised cap, Stop's ease back within it), how the enlarged tiles look, and whether a pinch keeps following.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never git.
- It reads the build log for compile errors first.
- It confirms the tree is identical to HEAD (`6d7197f6`) after each check.

All 6 compiled with 0 errors and failed for their own edit.

| Check | One edit | Fails with |
|---|---|---|
| z01 | the cap not raised while navigating | 18.0 expected, 17.0 |
| z02 | not asked on start | "Return asks for the start zoom"; the new-navigation test |
| z03 | never cleared when applied | the facing, "Return to Route", tab, configuration and "applied once" tests (5) |
| z04 | the "was navigating" flag not saved | the configuration-change test |
| z05 | the flag not passed to the map's request | "Return asks for the start zoom"; the new-navigation test |
| z06 | asked whenever navigating, not only on a start | the configuration-change test |

## Suite

| Run | Tree | Result |
|---|---|---|
| Tests first (`t1-tests-first`) | `eeb81afd` | 22 tests, 3 failures, as expected |
| Touched classes (`t2-built`) | before the commit | 4 classes, 135 tests, 0 failures, 19 skipped (the icon-stack class's existing `@Ignore`s) |
| Full suite (`t3-full-suite`) | `6d7197f6` | 426 classes, 3517 tests, 0 failures, 0 errors, 24 skipped |

**Reconciled, counted per file against `origin/main`.** The base's 3,510 tests in 426 classes, plus `AvailabilityScreenNavigationViewTest` 11 → 17 and `NavigationViewTest` 5 → 6: 3,517. The later merge of `09e43bd3` brought docs only.

## For the owner's next walk

1. Tap Return: the map zooms to about two blocks ahead as it tilts. On Topo, slightly soft up close.
2. Pinch: the zoom stays, and the map keeps following.
3. Drag, then "Return to Route": following comes back at the pinched zoom.
4. Stop: flat, north-up, and back within the map's usual limit.
5. Return again: the set zoom again.

The `ForagerNavView` log lines now include the zoom, so each step can be read back afterwards.
