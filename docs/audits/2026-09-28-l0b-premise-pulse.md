# L0b premise pulse (read-only, at `f62eb3e`)

**Date:** 2026-09-28. **Read at:** `journal-redesign` `f62eb3e5fd0db174492f099d5dbe5a4d3d31921f`, clean tree. **By:** a pulse subagent of the planner session on the owner's computer; no device reads, nothing run but git. Recorded by the planner from the pulse's hand-back, unedited in substance, so the L0b dispatch (`prompts/preserved/2026-09-28-03.md`) can cite it. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Every line is a claim about `f62eb3e`; re-check before relying on it later.

## 1. The Layers button

- **Compact (portrait, fullscreen, landscape):** row 4 of `MapIconBar`, `Icons.Filled.Layers` (`ui/map/MapChrome.kt:438-450`); content description "Map mode: X. Choose Street, Topographical, or Satellite. Night mode on/off."; inside the cluster in `CompactMapTab` (`ui/availability/AvailabilityCompactMapUi.kt:936-961`). Shown in fullscreen; padded by `controlsPadding` in landscape (`:911`); gone while the cluster is minimised (`:904-905`). In the cluster, not the rail.
- **Medium/expanded:** a separate round `MapModeToggle` at `Alignment.TopEnd` (`ui/availability/AvailabilityWideLayoutUi.kt:139-161, 296-300`).
- **Cartography entry map:** only while fullscreen (`ui/log/CartographyEntryReportScreen.kt:399-433`); hidden while offline tiles are on (`:409`).
- **Centre-pin pickers:** none has it. `MapModePicker` is called only from the three places above.
- **What it opens:** `MapModePicker` (`ui/map/MapChrome.kt:126-193`): not a sheet, dialog or dropdown but a custom popover, a full-size 32% black scrim dismissing on tap (`:145-154`), then an `AnimatedVisibility` panel expanding from an anchor (`:157-166`); a `Surface` holding a `Row` of `FilterChip`s, one per `MapMode` (`:175-189`); a tap applies and closes. Contents: Street, Topographical, Satellite only (`ui/map/MapMode.kt:28-31`). Night Maps is a Settings checkbox (`ui/availability/AvailabilitySettingsUi.kt:367`); the only offline toggle is the Cartography map's fifth row (`CartographyEntryReportScreen.kt:416-423`).
- **Basemap state:** `remember { mutableStateOf(MapMode.DEFAULT) }` (`ui/availability/AvailabilityScreen.kt:828`), not persisted.

## 2. Layer state

- Only on `MapRenderMode`: `layers: MapLayersState = MapLayersState.DEFAULT` (`ui/map/MapSlot.kt:174`) and `onFeatureTap` (`:190`). `MapOverlayContent` holds none.
- `MapLayersState(layers: Map<String, LayerState>, reorderableOrder: List<String>)` (`ui/map/layers/MapLayerState.kt:34-43`); `LayerState(visible, opacity)`, opacity a 0-1 multiplier rejected outside that range in `init` (`:14-22`).
- `MapRenderMode` is constructed at `AvailabilityScreen.kt:840` (main map; compact via `:1386` then `.copy(bottomInset=…)` at `AvailabilityCompactScaffold.kt:794`, wide via `:1333`), `CartographyEntryReportScreen.kt:379-385`, `CentrePinLocationPicker.kt:152`. None passes `layers` or `onFeatureTap`; non-default `MapLayersState` appears only in `MapLayerStateTest` and `MapLayerRegistryTest`.
- Registry flags (`ui/map/layers/MapLayers.kt:211-252`): toggleable are offline fill (`:219`), breadcrumb (`:228`), kept tracks (`:230`), planned trips, waypoints, finds, photos (`:248-251`); not toggleable are the two casings and the offline outline, which follow their owner layer (`:182`), the search centre (`:231`) and sightings (`:239`). `userOpacity` and `userReorderable` are false everywhere (`:167-169, 183-184, 220-221, 240-241`). The colour-field group is empty (`:191-192`).
- Visibility and opacity apply live, keyed on `layersState` (`ui/map/SightingsMap.kt:597-600`); order only at a style load (`:532, 744-755`).

## 3. DataStore

- Interface `domain/MapPreferencesRepository.kt:21-59`, every function `suspend` returning `Result<…>`. Implementation `data/repository/DataStoreMapPreferencesRepository.kt`: per-instance `PreferenceDataStoreFactory.create` (`:31-33`), `runCatchingCancellable` (`data/repository/RunCatchingCancellable.kt:18`); file `map_preferences` (`:76`); keys `offline_map.last_picked_lat`, `offline_map.last_picked_lng`, `offline_map.last_picked_radius_km`, `offline_map.stale_threshold_days`, `night_mode.maps`, `map.fullscreen` (`:77-82`).
- No DI framework: `AppContainer.kt:190` creates it; `MainActivity.kt:76` passes it into `AvailabilityViewModel` (constructor `:82`), its only reader (reads `:836, :855, :870, :961`; writes `:946, :977, :1133`; failures logged through `errorLog.w`).
- No per-layer or overlay-visibility preference exists.

## 4. Diagnostics screen

- `app/src/debug/java/com/zynergylabs/forager/app/ui/diagnostics/DiagnosticsPanel.kt`; Settings row "Diagnostics (debug build)" (`:334`). Gated by a source-set split, not `BuildConfig.DEBUG` (rationale `:61-73`); the release twin composes nothing (`app/src/release/.../DiagnosticsPanel.kt:13-19`).
- Reached in compact through the Tools drawer, Settings (`AvailabilitySettingsUi.kt:580-601`), the row (`:373`), the panel (`:296-300`); in medium/expanded through `DrawerPanel.Diagnostics` (`AvailabilityScreen.kt:1137, 1151-1156`).
- No toggles: a log row with view and share, listings of `photos/` and `captures/` with per-file share (`:160-164`); its doc comment says read-only (`:75-84`).

## 5. Chrome placement

- Attribution caption: inside `SightingsMap` at `Alignment.BottomStart`, padded by `bottomInset` (`SightingsMap.kt:680-689`); portrait `bottomInset` is the nav's height, animating to 0 in fullscreen (`AvailabilityCompactScaffold.kt:573-578`); landscape 0 (`:525`).
- MapLibre's own "i" attribution control moved to `BOTTOM|END` (`SightingsMap.kt:449`); no `setAttributionMargins` or logo call in main.
- Cluster: `CenterStart/CenterEnd` by `isMapIconBarOnLeftSide` (`AvailabilityCompactMapUi.kt:714`), plus drag offset and `controlsPadding` (`:908-912`); vertical drag clamped above the nav's top edge, or the screen bottom in fullscreen (`:750-772`); right side by default in portrait (`MapChrome.kt:486-491`), punch-hole side in landscape (`AvailabilityCompactMapUi.kt:238-242`).
- Bottom nav `Alignment.BottomCenter`, full width, 80% opacity (`:1135-1155`). Rail `CenterStart/CenterEnd` on the charger-port edge (`:1169-1187`), `fillMaxHeight` (`AvailabilityNavigationUi.kt:210`), slides away in fullscreen. Wide layout add button at `BottomEnd` (`AvailabilityWideLayoutUi.kt:314-319`).
- Inferred, not observed: with the rail on the left edge it covers the bottom-start caption; in portrait the bottom-end "i" sits under the nav.

## 6. Forecast data today

No forecast-cell code, no `ForecastSource`, no GeoJSON loading beyond the overlay `GeoJsonSource`s (`SightingsMap.kt:752, 892-900`). Non-spatial only: `GetTodaysForecastUseCase` (Open-Meteo weather, `domain/GetTodaysForecastUseCase.kt:20-26`), `AvailabilityForecast` (`domain/model/AvailabilityForecast.kt:3-22`), `PredictAvailabilityUseCase` (`:17`).

## 7. `onFeatureTap`

`(layerId: String, featureId: String) -> Unit`, default `{ _, _ -> }` at `MapSlot.kt:190` and `SightingsMap.kt:237`, passed through at `MapSlot.kt:415`. No caller passes anything else. A successful feature tap logs nothing; the only log is `Log.w` when the winner has no id (`SightingsMap.kt:378-385`); `onTap` fires afterwards either way (`:389`). No UI or debug path hides a layer. Inferred: a device check cannot observe a feature tap today.

## 8. Robolectric reachability

`AvailabilityScreenMapIconStackTest` (`@Config w360dp-h640dp-xhdpi`, `:142`) drives the real screen with coordinate touches on the layers row, opens the picker and taps "Street" (`:2419-2466`). `AvailabilityScreenSettingsPanelTest:422-444` uses a semantic `performClick`; its capturing map slot records `renderMode` (`:127-139`). The Map mode description is pinned in 4 test classes, including `AvailabilityScreenAdaptiveLayoutTest:169` and `CartographyEntryReportScreenFullscreenTest:165+`. No test composes the real `SightingsMap` or `SightingsMapSlot`; no landscape test drives the button.

## 9. Globally unique names

DataStore files: `app_theme_preferences`, `camera_grid_preferences`, `camera_orientation_preferences`, `map_preferences`, `photo_location_preferences`, `sundown_preferences`, `distance_unit_preferences`. Key namespaces: `app_theme.`, `camera.`, `offline_map.`, `night_mode.`, `map.`, `photo_location.`, `sundown.`, `unit_system.`, `distance_unit.` (legacy). No `SharedPreferences` in main.

## 10. Screenshots

Committed under `docs/audits/img/<date>-<slug>/` or `docs/audits/assets/<date>-<slug>/`; 111 images, about 62.5 MB. Screenshots showing personal data were left out or deleted (`2026-09-22-strip-stack-device-check-run-record.md:81`, `2026-09-26-landscape-capture-record.md:67`); no written rule, only the practice.

## Could not determine

Where MapLibre's "i" and logo land relative to the nav and rail (needs the phone); what the rail contains; whether the cited tests pass at this head.

## Premises the planner's framing got wrong

The picker is not a sheet and holds only basemaps; most of the owner's overlays are never drawn on the main map; the Diagnostics screen has no toggles and its release twin is empty; the corner opposite the attribution is already taken; "the map opens as it was left" does not cover the basemap; layer state is only on `MapRenderMode`, and a stored reorder takes effect only at the next style load. The owner's answers to these are in `docs/plans/journal-redesign.md`, "L0b rulings (owner, 2026-09-28)".
