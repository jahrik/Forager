# Own map tiles: Forager serves its own online basemap

**Status: a plan for later, nothing built, nothing dispatched.** Written 2026-09-30 by the planner at the owner's request. The owner, verbatim: "3 for now but 2 intrigues me. Maybe tiling our own maps should be a project", then "Save it for after PR 140. Write it as a plan for later". The decision it comes from is `RECORD.md` 2026-09-28-338.

**When:** after PR #140 (the Journal redesign) leaves draft. **Do not start before then.**

**The update it belongs to** (the owner, 2026-09-30, verbatim): "Next update we will focus on navigation, predictive forecast layers, and trip planning. So map tiling will fit neatly." All three draw on or over the basemap. So the scope choice at step 2 (A or B, and the coverage) is made together with that update's plans, not on its own:
- navigation: the Navigator plan, `docs/navigation/forager-navigator-plan.md`, audited on 2026-09-28;
- forecast layers: forager-forecast, and the map-layers pulse `docs/audits/2026-09-27-map-layers-and-forecast-data-pulse.md`;
- trip planning: the planned trips already drawn on the map.

**This is a claim about the past.** Everything under "What exists" was read by the planner on 2026-09-30, at `journal-redesign` `f2b941dc`, from docs dated August 2026. Per CLAUDE.md ("A planner's picture of the repository is a claim about the past"), the first step below re-reads all of it before anything is decided.

## Why

The online Street basemap (`Basemap.OSM_STANDARD`, `tile.openstreetmap.org`), and since -310 topo night below map zoom 9.5, depend on OpenStreetMap's own tile servers. Their policy (operations.osmfoundation.org/policies/tiles, read 2026-09-30; no date shown) says:
- "there is no SLA or guarantee";
- "Access may be blocked without prior notice";
- "Commercial services, or those that seek donations, should be especially aware that access may be withdrawn at any point";
- "Offline use is not permitted on tile.openstreetmap.org".

Forager will be sold (the owner's commercial ruling, 2026-09-28). A basemap that can go blank without notice is a pre-release risk, of the same kind as the Open-Meteo subscription.

**The alternatives on record:**
1. a commercial tile provider (switch2osm.org lists some);
2. **the app's own tiles** (this plan);
3. keep OSM's servers until release. That is the owner's choice for now.

The owner decides between 1 and 2 before release.

## What exists (read 2026-09-30; verify first)
- **A tile server of Forager's own.** A Cloudflare Worker, `forager-pmtiles.brandonlee1-894.workers.dev`, serves a continental-US Protomaps PMTiles vector archive from R2. Sources:
  - `server/pmtiles-worker/README.md`;
  - `docs/plans/README.md`, rows "Real PMTiles tile-serving infrastructure" and "PMTiles Worker → real app wiring" (PR #24, PR #25);
  - `docs/plans/pmtiles-worker-android-wiring.md`.
- **The offline maps use it today.** `OFFLINE_STYLE_URL` = `…/style/offline.json` (`app/src/main/java/com/zynergylabs/forager/app/map/OfflineStyle.kt:18`). That style is glyph-stripped: 57 layers, no labels, because of a native crash when offline downloads carry glyph layers (PR #23, `maplibre-migration.md` §7).
- **A labelled style was generated and not wired up.** Per `pmtiles-worker-android-wiring.md` (2026-08-19): a 71-layer labelled style, from Protomaps' own `@protomaps/basemaps` package, "for a future live basemap". It was not bundled, because nothing consumed it.
- **The night recolour of the vector style exists.** Colour build C1 (d): `NightColour.kt`, `offlineNightRecolourOf`, `applyOfflineNightRecolour` in `SightingsMap.kt`. It applies the same V1 transform as the raster night paint.
- **No elevation data is served.** No hillshade or raster-DEM source exists in `main/` (-323). -310 measured the AWS Open Data Terrain Tiles (Terrarium) as a candidate: public domain for the US, with a combined attribution block; about 15 MB per 60×60 km offline region up to z12, estimated from four tiles (`docs/audits/data/2026-09-30-topo-night/renders/README.md`).

## Scope choices for the owner (decided at step 2, not now)
- **A. Own Street only.** Swap the online Street basemap, and topo night's low-zoom layer, to the Worker's labelled vector style, day and night. This is mostly wiring.
- **B. Own topo too.** A, plus contours and hillshade from elevation data, so the app no longer needs OpenTopoMap either. OpenTopoMap has the same kind of risk: it publishes no limit, "for bigger projects, please contact us", its render server was downsized on 2026-01-05, and it plans to replace raster tiles (-310's report).
- **Coverage:** today's archive is continental US only. Worldwide would be a larger archive, and a larger hosting cost.

## Steps
1. **A read-only survey, no code** (the `pulse-before-plan` skill). Establish from the live systems and the repo, with file:line and URLs:
   - what the Worker serves today: the archive's date and size, its zoom range and coverage, whether it holds the labelled style, and the Worker's error handling;
   - what it costs today, and what a public app's traffic would cost (Cloudflare plan limits, R2 egress, Worker requests);
   - how the archive is built and refreshed, and who runs that;
   - the licences: OSM data (ODbL, attribution), Protomaps' basemap style and schema, the fonts and glyphs, and any DEM, each against the commercial-safe ruling;
   - the glyph crash (PR #23): whether it touches **online** labelled rendering, or only offline downloads;
   - what `SightingsMap`, `BasemapStyles` and the night recolour would need, as a pre-build report and not a build.
2. **A spec** (`plan-forge:spec`) for the owner: A or B, the coverage, the look by day and at night (the owner's "Don't make me think" and the -310 night rulings apply), and the costs. **The owner rules on scope.**
3. **Dispatches,** only after the spec is approved. Each carries the usual tests-first, revert-check and device-check steps.

## Constraints carried forward
- **Night mode is pure V1 inversion.** No dimming, no per-pixel tuning (the owner, -314, -322). The switch below 9.5 exists because OpenTopoMap's low-zoom tiles invert light (-310).
- **Offline regions keep working throughout.** The owner's data and downloaded regions must survive any style change (the Room/DataStore rules in CLAUDE.md).
- **The attribution stays visible** for every source drawn, whatever the policy of the source.
- **The User-Agent** (`net/AppUserAgent`, -325 and -331) goes on every request to the Worker too.
