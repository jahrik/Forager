# Why the ORIGIN waypoint was not drawn on the Maps tab (read-only trace, 2026-09-28)

**Prompted by:** the L0a device check's stop (`docs/audits/2026-09-28-l0a-device-check-run-record.md`, dispatch `2026-09-28-02`). On the Maps tab, the phone's one stored waypoint was not drawn. It was the ORIGIN of a track recording that was still open.

**Read at:** `9a81a7e` (before L0a) and `f62eb3e` (after L0a), through git objects only, with no device reads.

**By:** a pulse subagent of the planner session. The planner recorded it from the pulse's hand-back, unedited in substance. Lines are at `f62eb3e` unless marked `@9a81a7e`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

## Finding: filtered out by design, and unchanged by L0a

The device coder's cited path was correct but incomplete:
- `GetWaypointsUseCase.kt:9`
- `WaypointDao.kt:10`
- `TrackRecordingViewModel.kt:190/576`
- `MainActivity.kt:545`

After `MainActivity.kt:545`, the list passes through a filter the coder did not cite:
- `ui/availability/AvailabilityScreen.kt:775`: `val isNavigating = isReturning`
- `:778-779`: `mapWaypoints = mapVisibleWaypoints(waypoints, isNavigating, target = navigationTarget)`
- `AvailabilityPureFunctions.kt:65-66`: `waypoints.filter { it.designation == null || (isNavigating && target != null && it.id == target.id) }`

So the Maps tab draws an ORIGIN waypoint only while the Return leg is active and that waypoint is the navigation target. This is deliberate, and three places record it:
- `AvailabilityPureFunctions.kt:58-63`;
- `WaypointDesignation.kt:12-14` ("neither auto waypoint draws while not navigating");
- `MapVisibleWaypointsTest.kt:17-19, 26-29`.

L0a changed nothing on this path. `ui/availability/` is byte-identical at both commits. There is also no diff over `ui/track`, `MainActivity.kt`, `data/` or `GetWaypointsUseCase.kt`.

## The layer itself

- **Ids unchanged:** `waypoints-layer` and source `waypoints`, at both commits (`MapLayers.kt:127, 140`; `@9a81a7e:1163-1164`).
- **Symbol settings unchanged:** it is a symbol layer with `iconImage("waypoint-pin")`, `iconAllowOverlap(true)` and a centre anchor at both commits (`SightingsMap.kt:818-823`; `@9a81a7e:712-719`).
- **What L0a added:** `visibility` and `iconOpacity` from `layerPaintFor` (`:753, :831-834`). By default the layer is visible at multiplier 1, and nothing in main passes a non-default state (`MapSlot.kt:174`).
- **Image registration:** the image is re-registered on every style load at both commits (`:741`; `@9a81a7e:641`).
- **Order:** the search centre is below the waypoints both before and after L0a. L0a moved it above the lines only (`MapLayers.kt:199-203`).
- **Collision:** every bitmap symbol layer allows overlap, so inferred: collision cannot hide the pin.

## Consequences for the device check

- **The stop was a wrong premise in the dispatch, not a regression.** The planner's dispatch named the path from the coder's reading and did not trace it to the map.
- **An ordinary waypoint** (one with no `designation`) is the positive control: it must draw.
- **The pixel search was not a valid test of absence.** `#350560` is the day colour only; the night colour is `#B97DF7` (`MapPalette.kt:75, 93`).
- **Inferred, not observed:** if the process restarts mid-recording, the navigation target is null, and the ORIGIN stays hidden even while returning (`MainActivity.kt:558`; `TrackRecordingViewModel.kt:216, :275, :559`).
