# Device check, map layers L0a on the S22 Ultra: run record

**Status: in progress.** This commit holds the pre-registration: each check's pass condition and
prediction, written from the code before the check was run. Verdicts follow in later commits.

**Date:** 2026-09-28, from 03:08 UTC
**Device:** Samsung SM-S908U, serial `R5CT321008R`, Android 16. `ro.product.model=SM-S908U`,
`ro.build.id=BP2A.250605.031.A3` (`getprop`, 03:08:48Z). It was the only device attached
(`adb devices -l`), and every adb call named it with `-s`.
**Build under test:** the installed `versionName=1.0.1192+g24589349`, `versionCode=1192`. Nothing
was installed, and Gradle was not run.
**Dispatch:** `prompts/preserved/2026-09-28-02.md`; intent `2026-09-28-02` in `RECORD.md` (the planner's).
**Base:** `origin/journal-redesign` at `f18af53` (the planner's store-copy commit after `f62eb3e`;
`git diff --stat f62eb3e f18af53` touches only `RECORD.md` and the store copy).
**Evidence:** outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-l0a/`,
cited by file name and sha256. No screenshot, dump, coordinate or place name is in this file.
**dp to px:** `wm density` is 450, so 1 dp = 450 / 160 = 2.8125 px. The screen is 1080 x 2316 px (`wm size`).

## Pre-registration (written before each check was run)

Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

### Check 2: attribution caption text

- **What the code produces.** The caption is
  `attributionCaption(mapCreditsFor(basemap, useOfflineTiles, activeLayerCredits(MAP_LAYER_REGISTRY, layersState)))`
  (`ui/map/SightingsMap.kt:681`). No registry layer sets `credit` (`ui/map/layers/MapLayers.kt:211-252`;
  the default is `null`, `:109`), so `activeLayerCredits` is empty, and `mapCreditsFor` returns the one
  credit of `mapAttributionFor` (`ui/map/BasemapStyles.kt:130-142`). The Maps tab passes no offline
  flag and no layer state (`ui/availability/AvailabilityScreen.kt:840`; defaults `false` and
  `MapLayersState.DEFAULT`, `ui/map/MapSlot.kt:104`, `:174`).
- **Pass condition:** the caption at the map's bottom start reads exactly, with no ` · ` separator:
  - Street (`MapMode.STREET` -> `Basemap.OSM_STANDARD`, `ui/map/MapMode.kt:29`): `© OpenStreetMap contributors` (`ui/map/Basemap.kt:170`);
  - Topographical (`OPEN_TOPO_MAP`, `MapMode.kt:30`): `© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)` (`Basemap.kt:161`);
  - Satellite (`USGS_IMAGERY_ONLY`, `MapMode.kt:31`): `USGS The National Map, orthoimagery — public domain` (`Basemap.kt:147`);
  - an entry map with offline on: `Protomaps © OpenStreetMap` (`map/OfflineStyle.kt:28`).
- **Prediction:** matches on all three basemaps. The entry-map part is **not run**: the phone has no
  Cartography entry (inventory below).
- **Disclosure:** the expected texts above were read from the code before the device was touched.
  The Topographical caption was first seen on screen at 03:11:43Z, in the arrival screenshot, before
  this section was written down.

### Check 3: the sighting bubble on a direct tap, a near miss and a far tap

- **What the code does.** The click listener (`ui/map/SightingsMap.kt:343-395`) queries every
  tappable layer at the tap's screen point; only when nothing tappable is there does it query a
  square box of `TAP_BOX_DP` = 48 dp, so ±24 dp (`ui/map/layers/TapPrecedence.kt:89`,
  `SightingsMap.kt:359-363`). A winner on the sighting layer is looked up by `observationId` and
  goes to `onSightingTap`. On the Maps tab that sets `tappedSighting`, which draws the bubble and
  feeds `focusedObservationId` back to the map (`ui/availability/AvailabilityCompactMapUi.kt:605`,
  `:618-622`). No winner calls `onTap`, which clears `tappedSighting` (`:614-617`). The selected dot's
  ring is `0xFF2196F3` at 3 dp; every other ring is white at 1.5 dp (`SightingsMap.kt:1128-1149`,
  `:1390-1391`; `ui/theme/MapPalette.kt:87-88`). The dot's radius is 9 dp (`SightingsMap.kt:1378`),
  so its drawn edge is 10.5 dp (29.5 px) from its centre with the ring.
- **Pass conditions:**
  - (a) **Direct tap** at a dot's centre: the bubble appears (its texts and a `Close` button in the dump),
    and the tapped dot's ring turns blue. The ring is the map's own rendering of
    `focusedObservationId`, so a blue ring on the tapped dot ties the bubble's sighting to that dot.
  - (b) **Near miss** about 16 dp (45 px) from an isolated dot's centre, outside its drawn edge, with no
    other drawn overlay feature within the ±24 dp (±67.5 px) box: the bubble appears for that dot, and
    its ring turns blue.
  - (c) **Far tap** at least 60 dp (169 px) from every drawn overlay feature: no bubble appears.
- **Sequence:** each case starts with no bubble showing. A bubble is closed with its own `Close` button,
  not with a map tap, so that the far tap is the only map tap with no bubble expected.
- **Prediction:** (a), (b) and (c) all as the code says.

### Check 4: draw order on the Maps tab

- **What the code orders.** `MAP_LAYER_REGISTRY` bottom to top (`ui/map/layers/MapLayers.kt:211-252`):
  offline fill; offline outline, breadcrumb casing, breadcrumb, kept-track casing, kept track;
  search centre, sightings, planned trips, waypoints, finds, photos. `initializeOverlayLayers` adds one
  native layer per spec in that order (`SightingsMap.kt:733-756`). The Maps tab draws sightings,
  planned trips, the breadcrumb, waypoints and the search centre
  (`ui/availability/AvailabilityCompactMapUi.kt:598-606`).
- **Pass condition:** for each pair of those layers seen overlapping on screen, the one drawn on top is
  the one higher in the registry. In particular, a sighting dot or the reticle drawn over the breadcrumb.
- **Prediction:** at least the breadcrumb part is **not run**. The phone is stationary, and the trail is
  about 9 ft long (the return row reads `9 ft`) and lies under the location puck (inventory below).
  I did not start this recording and will neither stop nor discard it.

### Check 5: draw order on an entry map

- **Pass condition:** as check 4, for finds, photos, kept tracks and the offline circle's fill and
  outline on a Cartography entry map (`ui/log/CartographyEntryReportScreen.kt:355-382`).
- **Prediction:** **not run**. `cartography_entries` has 0 rows.

### Check 6: defaults look as before (an observation)

- **What the code sets.** `visibility: visible` and every base opacity, at state opacity 1
  (`SightingsMap.kt:831-841`, `:753`). The base opacities are sighting fill 0.7 and ring 0.85, offline
  fill 0.2, and 1 for every line and icon (`MapLayers.kt:145-158`, `:224`, `:243-246`). Colours are from
  `MapPalette.DAY` with Night Maps off (`ui/theme/MapPalette.kt:74-89`): sighting dot `0xFF2B2B2B` with a
  white ring, search centre `0xFF000000`, waypoint `0xFF350560`, breadcrumb `0xFF650BB1`, casing `0xFFFFFFFF`.
- **What is recorded:** whether any overlay seen looks faded, missing or restyled against those values.
  No earlier screenshot exists, so this cannot be a pass.
- **Prediction:** nothing looks faded or missing.

### Check 7: Night Maps leaves overlays alone

- **Prediction:** **not run**. It needs an entry map with offline tiles, and the phone has no
  Cartography entry and no offline region.
- **The dispatch's pass condition does not match the code.** The dispatch's pass is "the basemap
  changes and every overlay's pixel colours do not". But the overlays are built in
  `MapPalette.forMode(nightMode)` (`ui/map/BasemapStyles.kt:207`, passed to `initializeOverlayLayers`
  at `SightingsMap.kt:528-534`). That is `MapPalette.NIGHT` with Night Maps on
  (`ui/theme/MapPalette.kt:92-109`). So by the code, every overlay's colour does change with the toggle:
  the waypoint goes from `0xFF350560` to `0xFFB97DF7`, the sighting dot from `0xFF2B2B2B` to
  `0xFF8C8C8C`, and the casing from white to black. The claim the code supports is narrower: the
  offline recolour (`applyOfflineNightRecolour`, `SightingsMap.kt:527`, `:1408`) runs before the overlays
  are added, so the overlays show the night palette's own values rather than the V1 transform of their
  day values. This is recorded for the planner and is not resolved here.

## Inventory (read-only, before any check)

To follow.
