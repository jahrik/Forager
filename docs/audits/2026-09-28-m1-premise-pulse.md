# M1 premise pulse (read-only, at `21d755a`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `21d755a`. The app tree is identical to `05424b6`. **By:** a pulse subagent of the planner session. The planner recorded this from the pulse's hand-back, condensed but with every citation kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`. Line numbers are at `21d755a`; re-check them before relying on them.

## Tap path

- **`onFeatureTap`.** `(layerId, featureId) -> Unit`, defaulted, at `ui/map/MapSlot.kt:193`, passed on at `:440` and declared at `ui/map/SightingsMap.kt:242`. It has no callers in main or tests.
- **Precedence and tap groups.** Precedence is `tapWinner` / `resolveTap` (`ui/map/layers/TapPrecedence.kt:25-42`): point first, then a 48 dp box. Tap groups (`ui/map/layers/MapLayers.kt:246-287`):
  - colour fields and the offline fill are NONE (`:214`, `:258`);
  - the outline, breadcrumb and kept tracks are LINE;
  - every marker is MARKER, the search centre included.
- **Fixed-id non-records.** `"search-centre"` and `"breadcrumb"` (`SightingsMap.kt:1436, :1439`) are tappable.
- **The click listener** (`SightingsMap.kt:364-414`):
  - a resolved sighting goes to `onSightingTap` only (`:392-393`);
  - any other winner goes to `onFeatureTap`, and then `onTap` always fires (`:398-409`).
- **What `onTap` does on each host:**
  - compact: exits fullscreen and clears the sighting (`AvailabilityCompactMapUi.kt:640-643`);
  - wide: clears the sighting (`AvailabilityWideLayoutUi.kt:283`);
  - entry map: enters fullscreen (`CartographyEntryReportScreen.kt:403`);
  - picker: `{}`.
- **Long-press.** Every caller passes `{}`.

## ObservationBubble

- **Where:** `ui/availability/AvailabilityMapOverlaysUi.kt:357-481`. It is placed by the generic `AnchoredAtScreenPoint` (`:273-325`, which takes a content lambda) at 315° from the tap point, rotated by the map's bearing, capped at 280 dp, and clamped to the map box (`:315-320`).
- **The tail offset.** The run record's roughly 150 px tail offset is the x clamp: the bubble is pushed on screen while the tail is drawn from where it lands. The limitation is named at `:253-257`. Inferred. The run record's case (b) is not reconciled.
- **Dismissal:** its close button, the host's `onTap`, or the iNaturalist action.
- **Reuse:** it takes a `Sighting`, so it is not reusable as it stands. Its shell could be extracted.
- **Live re-projection** exists only for sightings (`SightingsMap.kt:455-460`, keyed on `focusedObservationId`).

## Per kind: `featureId`, content today, lookup

- **Finds.** `MushroomLogEntry.id` (`GetMapRecordsUseCase.kt:95`).
  - The tile (`ui/log/FindsGalleryScreen.kt:244-290`) and the report (`ui/log/LogEntryReportScreen.kt:80-200`, partly read) show it.
  - Lookup: `MushroomLogUiState.entries`.
- **Photos.** `LogPhoto.id` (`:100`).
  - The album tile has an image and badges, no text (`ui/log/EntriesAlbum.kt:240-275`).
  - Lookup: the gallery list. How it reaches the Maps host is unverified.
- **Waypoints.** `Waypoint.id`.
  - The row (`AvailabilityTripsWaypointsUi.kt:242-272`) and the J5c sheet (`ui/log/RecordDetailsSheet.kt:186-213`) show it.
  - Lookup: the Maps host already has `MapOverlayContent.waypoints`. The entry map's waypoints carry placeholder names (`CartographyEntryReportScreen.kt:371-380`).
- **Tracks.** `Track.id`, ended tracks only (`GetMapRecordsUseCase.kt:103-104`).
  - The J5c sheet (`RecordDetailsSheet.kt:216-250`) shows it.
  - Lookup: `trackUiState.tracks`.
- **Planned trips.** `PlannedTrip.id`.
  - The Trip Planner row (`AvailabilityTripsWaypointsUi.kt:110-156`) shows it. There is no sheet.
  - The compact map gets trips only after a search (`AvailabilityCompactMapUi.kt:619`).
- **Offline regions.** `id.toString()` of a `Long` (`GetMapRecordsUseCase.kt:107`); the J5c sheet's target is a `Long` (`RecordDetailsSheet.kt:70`).
  - The J5c sheet (`:253-266`) shows it.
  - The outline takes the tap.
- **Forecast cells.** The model has every D55 property (`domain/ForecastCells.kt:53-71`). The map feature carries only `chance` and `featureId = "${lat},${lng}"` (`ui/map/ForecastCellLayer.kt:78-81`), and `SightingsMap` does not keep the cells (`:647-670`).
  - Lookup: layer, then group through `MapForecastFeed.groupsByLayer`, plus the week, then `ForecastCellStore.cells` (`ForecastCells.kt:227`).
  - Parsing the id back could lose precision (inferred).

## Open targets

- **Find report:** `onOpenEntry` (`MainActivity.kt:469`) plus `JournalTab`'s local `mode` (`JournalTab.kt:438-441`). No code switches `compactTab` (`AvailabilityScreen.kt:754`) to JOURNAL except the nav.
- **Photo viewer:** `PhotoViewerDialog` (`ui/log/PhotoViewerDialog.kt:106-110`) is a dialog, so it can be composed in place (inferred).
- **J5c sheet:** `RecordDetailsSheet` is `internal` and list-driven (`:141-153`). It is composable on Maps if its inputs are threaded (inferred).
- **Planned trip:** no target exists.
- **Directions:** `launchDirections` (`AvailabilityPureFunctions.kt:162-176`).

## Tests and limits

- `SightingsMap` cannot run under Robolectric (`app/src/test/.../ui/map/SightingsMapOverlayDataTest.kt:26-36`).
- Coordinate-touch stub harnesses exist: `AvailabilityScreenLandscapeB2Test:490-505`, `AvailabilityScreenShortLandscapeTest:352, :404`, and `MapLayersUiFixtures.kt:108-132` (`LayersRecordingMapSlot`).
- **Testable headless:** a stub slot firing `onFeatureTap`, then the host lookup, bubble content, actions, dismissal and the `onTap` decision; coordinate touches on bubble bounds; the pure precedence and builders; cell lookup against a fake store.
- **Device-only:** the native hit test and live re-anchoring.
- `MapSlot` is at its nine-parameter ceiling (`MapSlot.kt:353-399`). New callbacks go on `MapRenderMode` (`:159-200`), and new data on `MapOverlayContent` (`:218-320`).
