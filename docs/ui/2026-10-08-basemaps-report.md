# Basemaps: Satellite removed, the basemap remembered across restarts

Dispatch 2026-09-28-708 (RECORD intent -708; preserved at `prompts/preserved/2026-10-08-01.md`). Branch `basemaps`, cut from
`origin/main` at `d223728b` (fetched before cutting; `origin/main` was still `d223728b` when this report was written). No PR.

**Status: written, not run.** The dispatch holds Gradle for the planner's go. Nothing on this branch has been compiled, no test
has run, and no revert check has been done. Every claim below about behaviour is what the code is written to do, not something
observed.

Paths are relative to `app/src/main/java/com/zynergylabs/forager/app/` unless they start with `app/` or `docs/`.

**Revisit:** Satellite is to be revisited once the forecast's habitat layers are on the map (the owner's "revisit later"). The
same line is in `ui/map/Basemap.kt`'s class doc and `ui/map/MapMode.kt`'s.

## Premises checked

| Premise in the dispatch | Found |
|---|---|
| `ui/map/Basemap.kt` `USGS_IMAGERY_ONLY`, its style, night exemption, attribution, Layers sheet and picker entries | Confirmed. Style and exemption in `ui/map/BasemapStyles.kt` (`basemapTakesNightPaint`, `effectiveNight`); the sheet (`ui/map/MapLayersSheet.kt`) and the picker (`ui/map/MapChrome.kt`, `MapModePicker`) both iterate `MapMode.entries`, so removing `MapMode.SATELLITE` removes both chips. |
| `domain/MapLayerPreferencesRepository.kt:27` says the basemap "stays session-only" | Confirmed, lines 27-28. |
| "Anyone with Satellite stored (the live map, an entry map's seeded basemap, an offline region's style if any)" | **Wrong: no basemap name was stored anywhere.** The live map's mode was `remember { mutableStateOf(MapMode.DEFAULT) }` in `ui/availability/AvailabilityScreen.kt` (reset every launch); the entry map's is `remember(entry.id) { mutableStateOf(initialMapMode) }` seeded from the live map, never stored (`ui/log/CartographyEntryReportScreen.kt:325`); `OfflineRegionEntity` has no style or basemap column (`data/local/OfflineRegionEntity.kt:30-37`), downloads use the one offline vector style, and the offline picker pins `Basemap.OPEN_TOPO_MAP` (`ui/availability/AvailabilityOfflineMapsUi.kt:178`); the journal backup is Room tables and photos (`data/backup/`, no preference or basemap read), and `android:allowBackup="false"` (`AndroidManifest.xml:134`). So no phone can hold Satellite today. The unknown-key path below is still built and tested (by seeding the key), because the new store is the first place a removed name could ever sit. |
| Stops: a stored basemap name in Room or a backup format needing a migration; any change to offline region downloads | Neither arises: nothing in Room or the backup holds a basemap, and the offline download code is untouched. |

## What was built

1. **Satellite removed.** `Basemap.USGS_IMAGERY_ONLY` and `MapMode.SATELLITE` deleted, with: `basemapTakesNightPaint` and
   `effectiveNight` (their only purpose was Satellite staying day; every basemap now takes the night paint and the requested
   basemap night is the toggle), `BasemapCoverage` and `Basemap.coverage` (see "Decided beyond scope"), and the Satellite
   asides in comments that described current behaviour (`SightingsMap.kt`, `MapSlot.kt`, `NavigationView.kt`,
   `theme/MapPalette.kt`, `MapLayersSheet.kt`, `AvailabilitySettingsUi.kt`). Comments that record history (the entry map's
   state leak, the owner's chip-centring quote) are left as they were. The night handover from night topo to Street is not
   touched: `styleJsonFor`'s Topographical night branch lost only a condition that was always true for Topographical.
2. **Basemap persisted** in DataStore, `map_preferences`, key `map.basemap`, beside the layer keys:
   - `domain/BasemapPreferenceRepository.kt` (new): `getBasemapKey(): Result<String?>`, `setBasemapKey(key)`. A sibling of
     `MapLayerPreferencesRepository` rather than two more methods on it, so its fakes do not grow. Plain strings, so the domain
     names no map type.
   - `DataStoreMapPreferencesRepository` implements it on the same instance (DataStore refuses a second one on the file);
     `AppContainer.basemapPreferenceRepository` hands that instance out; `MainActivity` passes it and wires
     `onMapModeSelected`.
   - `MapMode.storageKey` (`"street"`, `"topographic"`) is what is stored, not the enum name; `MapMode.forStoredKey` reads it,
     `null` for anything else. `MapMode.REPLACEMENT_FOR_UNKNOWN` is Topographical (the owner: "Topo (Recommended)").
   - `AvailabilityViewModel.loadBasemapPreference` at start: nothing stored opens on Topographical (as before); a stored
     unknown key (Satellite's, or anything else) opens on Topographical, logs one line, and stores Topographical so the line does
     not repeat; a failed read logs and opens on Topographical without writing. Every outcome sets `AvailabilityUiState.mapMode`,
     so it cannot hold the map blank. A pick made before the read lands wins. `onMapModeSelected` shows the pick and stores it; a
     failed write is logged.
3. **No first frame on the default.** `AvailabilityUiState.mapMode` is `null` until the read is in, and `AvailabilityScreen`
   now hands the map `nightModeLoaded = nightModeMapsLoaded && mapMode is known`. `SightingsMap` loads no style while that is
   false (`requestedMapStyle` returns `null`, unchanged), so a phone that left Street never gets a Topographical style first.
   The field keeps its name; `MapRenderMode.nightModeLoaded`'s doc says it now waits for both.
4. **Docs.** `MapLayerPreferencesRepository`'s comment and the journal-redesign note (`docs/plans/journal-redesign.md`, a
   dated superseding paragraph after the L0b sentence, which stays) quote the owner. `Basemap.kt` and `MapMode.kt` record the
   removal, the revisit, and the persistence.

## Decided beyond scope (for the planner to confirm or reverse)

**Planner's ruling, 2026-10-08: all five accepted as built** (coverage removed; USGS script kept as the revisit's record; `chosenMapMode` stays with its reason in the comment; the gate's field name stays with its comment updated; an unknown key writes Topographical back and a failed read writes nothing). No code changed for it: each was already in that state.

1. **`BasemapCoverage` and `Basemap.coverage` removed.** No production code ever read them (only `BasemapTest` and
   `MapModeTest`); `UNITED_STATES_ONLY` existed for USGS, and with it gone every value would be `WORLDWIDE` with a `null` note.
   I read that as code whose only purpose was Satellite. Reversible from git if the revisit wants it back.
2. **`scripts/verify-usgs-basemap.sh` kept.** It only checks USGS services, so it is Satellite-only by purpose, but it is a
   script rather than app code and `Basemap.kt` cites it as the record for whoever revisits Satellite. Say if it should go.
3. **The screen keeps a local `chosenMapMode` beside `uiState.mapMode`.** The map draws `chosenMapMode ?: uiState.mapMode ?:
   DEFAULT`. In the app the two agree from the first pick on. It is kept because the many test hosts that drive
   `AvailabilityScreen` without a ViewModel (`MapChromeTestScreen`, the settings-panel and icon-stack hosts) would otherwise
   get a picker that changes nothing; this keeps them as they were rather than wiring a callback into each. The alternative is
   one source of truth (the ViewModel) and wiring every host; I chose the smaller change.
4. **The gate reuses `nightModeLoaded`** rather than renaming it to something like "style inputs loaded" across
   `SightingsMap`, `requestedMapStyle`, `MapSlot` and their tests. Named as churn in its doc comment.
5. **Unknown key: write Topographical back; failed read: write nothing.** The first stops the log line firing every launch;
   the second avoids overwriting a value that may be fine.
6. **`effectiveNight` deleted rather than reduced to `nightMode`.** `AppliedMapStyle.night` is now the toggle directly.

## Tests

New:
- `app/src/test/.../ui/availability/AvailabilityScreenBasemapPersistenceTest.kt` (Robolectric, real screen, real ViewModel,
  real `map_preferences` file, real touches): the Layers sheet offers only Street and Topographical; a stored `"satellite"`
  opens on Topographical (every render mode released to the map is Topographical, the Layers row says "Topographical"), the
  log line is the expected one, and the file then holds `"topographic"` read with no repository in between; Street picked in
  the sheet survives a restart (the first repository released, a fresh repository and ViewModel over the same file), with the
  second launch's read held open so the test sees that no render mode is released before it lands, then that every released
  one is Street.
- `app/src/test/.../ui/availability/AvailabilityViewModelBasemapTest.kt` (plain JVM): nothing stored; stored Street; stored
  Satellite (logged, `"topographic"` written); failed read (logged, nothing written, gate released); a pick stored under its
  key; a pick before the read lands is not overwritten; a failed write logged and the pick holds.
- `MapModeTest`: the modes are Street and Topographical; each mode's stored key, pinned literally, reads back; unknown keys
  (`"satellite"`, `"SATELLITE"`, `""`) read as none and the replacement is Topographical.

Fixture change: `MapLayersUiFixtures.kt` gains `basemapPreferences` on `mapLayersViewModel` (default: nothing stored, which is
the old behaviour), wires `onMapModeSelected` in `MapLayersTestScreen`, and adds `InMemoryBasemapPreference`.

Night handover from night topo to Street: covered by the unchanged tests in `TopoNightStreetSwitchTest` ("night topo is the
Street layer under the topo layer, overlapping 9_5 to 9_7, V1 on both", "which layers the style draws on each side of the
crossfade", "Street's night style JSON is byte-identical to before", "day style JSON is byte-identical to before for every
basemap", the caption tests). Byte-identical pins mean the edit to `styleJsonFor` cannot have moved those documents unnoticed.

### Every changed assertion, with its reason

| File | Was | Now | Reason |
|---|---|---|---|
| `ui/map/MapModeTest.kt` | `assertEquals(USGS_IMAGERY_ONLY, SATELLITE.basemap)` | removed | Satellite removed. |
| `ui/map/MapModeTest.kt` | "only Satellite resolves to a USGS basemap", "only Satellite is US-only coverage" | replaced by "the modes are Street and Topographical, in that order" (+ the two key tests, new) | Subject removed; what remains to pin is the set offered. |
| `ui/map/BasemapTest.kt` | four coverage tests: one basemap works outside the US; OSM Standard unrestricted; a limited basemap carries a note; USGS iff US-only | replaced by "no basemap is a US-only USGS service" (no `USGS` credit, no `nationalmap.gov` template) | `BasemapCoverage` removed (beyond scope, item 1). |
| `ui/map/BasemapNightStyleTest.kt` | "Satellite's night style JSON equals its day style JSON"; "only Satellite opts out of the night paint" | replaced by "every basemap's own layer takes the V1 paint at night" | The opt-out went with Satellite; pins that no basemap stays day. |
| `ui/map/BasemapStyleTest.kt` | "the USGS source declares ArcGIS row-column order"; "USGS Imagery's declared maxzoom is lower than the OSM standard map's" | removed | Both about USGS only; the x-y template test and the per-basemap maxzoom test stay. |
| `ui/map/TopoNightStreetSwitchTest.kt` | Satellite line in "day style JSON is byte-identical"; "Satellite's night style JSON is byte-identical to its day JSON"; `SATELLITE_DAY` | removed | Satellite removed; Topo and Street pins unchanged. |
| `ui/map/NavigationViewTest.kt` | two `USGS_IMAGERY_ONLY` lines (18 navigating, 15 not) | removed | Satellite removed; Topographical (17 to 18) still shows the cap raised, OSM (19) still shows it not lowered. |
| `ui/map/OfflineStyleSwapTest.kt` | night carries hue-rotate `== (basemap != USGS)` | `assertTrue` for every basemap | No exemption left. |
| `ui/map/OfflineStyleSwapTest.kt` | USGS credit line | removed | Satellite removed. |
| `ui/map/OfflineStyleSwapTest.kt` | helper `applied(...)` used `effectiveNight(...)` | `night = nightMode`, written out | `effectiveNight` removed; the helper is also the expectation for "once loaded…", so it should not call production code. |
| `ui/map/OfflineStyleSwapTest.kt` | "turning Night Maps on or off over Satellite reloads for the markers, and the basemap stays day" | removed | Satellite removed. |
| `ui/map/OfflineStyleSwapTest.kt` | "effective night is the toggle, except Satellite's…" | replaced by "the requested basemap night is the Night Maps toggle on every basemap, online and offline" (through `requestedMapStyle`) | `effectiveNight` removed. Test names "…Satellite included…" and "…carries effective night" renamed, assertions unchanged. |
| `ui/availability/AvailabilityScreenSettingsPanelTest.kt` | Satellite chip count 1, tap Satellite, basemap `USGS_IMAGERY_ONLY` | Satellite count 0, tap Topographical, basemap `OPEN_TOPO_MAP` | Satellite removed; still proves the sheet stays open and a second chip applies. |
| `ui/availability/AvailabilityScreenSettingsPanelTest.kt` | "the main map is told once the Night Maps preference has loaded": state `nightModeMapsLoaded = true` | state also `mapMode = MapMode.DEFAULT`; assertion unchanged | The gate now also waits for the stored basemap. |
| `ui/availability/MapChromeResumedTest.kt` | entry map picks Satellite, asserts `USGS_IMAGERY_ONLY` | picks Topographical, asserts `OPEN_TOPO_MAP` | Satellite removed; the entry map starts on Street, so Topographical is its other choice. The Maps tab still on Street is unchanged. |
| `ui/availability/MapChromeChipsTest.kt` | last chip "Satellite" | last chip "Topographical" | Satellite removed; the centring claim is the same. |
| `ui/log/CartographyEntryReportScreenFullscreenTest.kt` | Street, Topographical, Satellite displayed | Street and Topographical displayed; Satellite count 0 | Satellite removed. |

No test was `@Ignore`d, skipped or weakened beyond the removals above, each of which tested removed code.

## Revert checks planned for the go (not run)

Each restores from a copy saved before the edit, checks the build log for compile errors before reading results, and confirms
the forward change is back afterwards (CLAUDE.md, Testing).

1. `AvailabilityScreen`: drop `&& (uiState.mapMode != null || chosenMapMode != null)` from the gate. Expected: the restart
   test fails "nothing may draw while the stored basemap is unread" with a released Topographical render mode.
2. `AvailabilityViewModel.loadBasemapPreference`: drop `storeBasemap(replacement)`. Expected:
   `AvailabilityViewModelBasemapTest` "a stored Satellite…" fails `expected [topographic] but was []`. (The screen test would
   time out in `waitUntil`, a less specific failure, so the VM test is the one to cite.)
3. Same function: drop the `if (it.mapMode == null)` guard. Expected: "a pick made before the read lands…" fails expected
   `STREET` but was `TOPOGRAPHIC`.
4. `AvailabilityScreen`: drop `onMapModeSelected(it)` from `onMapModeChange`. Expected: the restart test times out waiting for
   `"street"` in the file.

## Unverified

- Everything: not compiled, no test run.
- That the held-read restart test sees composition before the read lands is how it is written (the read is held by a
  `CompletableDeferred`); not observed.
- On a phone: that the first frame after launch is MapLibre's blank and then the stored basemap, never the default. The
  Robolectric test reaches the render mode handed to the map, not `SightingsMap`'s style effect (its MapView needs a native
  initialiser). Device item: set Street, swipe the app away, reopen, watch the first tiles.
- That the blank before the read is short on a phone. A DataStore first read is the same one the Night Maps gate already
  waits on, and both reads start together, so it should add little; not measured.
