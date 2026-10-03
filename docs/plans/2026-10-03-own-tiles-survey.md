# Forager's own map tiles: the survey (stage 2 of the trail-navigation and own-tiles plan)

**Status:** survey, read-only. Nothing built or decided. Filed by the planner on 2026-10-03 (UTC), at `main` `cb035035`.

**Asked for by the owner,** verbatim: "Run the survey. I intend to use an RPI as a server for sending map tiles to the cloudflare worker to send to others as they call them. I have s 8GB RPI5 on 256GB nVME" (`RECORD.md` -461). Plan: `docs/plans/trail-navigation-and-own-tiles.md`, stage 2.

**How it was made, and how far to trust it.** Three agents run by the planner on 2026-10-03: (1) the repository at `origin/main` `cb035035`, read-only, following CLAUDE.md's Known pitfalls; (2) the live Worker's public responses (six small requests and one z5 tile) plus Cloudflare's and the tools' published documentation; (3) licences, styles and elevation data from published sources. **No Cloudflare account was read.** No patents. **The planner has not re-checked the web sources.** Marks: **[C]** read in the cited file or page, or observed on the live server; **[I]** inference. Repository paths are at `cb035035`; app paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

## Summary

1. **Forager's own tile server works, but it is a one-off, not a pipeline.** The archive in R2 is one manual extract of Protomaps' daily build from 2026-08-19, 8.8 GB, continental US [C, `server/pmtiles-worker/README.md:52-90`]. Nothing refreshes it, and the archive carries no data date [C].
2. **It is not ready for a public app's traffic.** It has no rate limiting [C, `src/index.ts`; required by `docs/plans/maplibre-migration.md:280-283`]. Zoom 15 tiles are fetched live from Protomaps' public build host, marked "NOT VERIFIED" in the code [C, `src/index.ts:127-246`]. Tiles fetched from the live server carried no Cloudflare cache status, so every tile probably runs the Worker [C observed; I], and the edge cache may need a custom domain rather than `workers.dev` [C, community source only].
3. **The map labels need work before an online switch.** The labelled style recorded on 2026-08-19 was never committed and cannot be recovered [C, searched all history]. Glyphs come from MapLibre's demo server [C, `BasemapStyles.kt:362`]. The night recolour covers backgrounds, fills and lines only, not text [C, `NightColour.kt:74-78`]. The record of the earlier label crash points at a section that does not contain it [C], and whether a labelled vector map renders online without crashing has never been tested [C].
4. **Trails need our own build, not an extract.** The Protomaps tile schema separates paths from tracks but drops difficulty and visibility (`sac_scale`, `trail_visibility`) [C, Protomaps `Roads.java`]. Showing trails as navigation treats them means building the tiles ourselves with Planetiler and a modified profile, or a separate trails layer from the same data [I]. That is where a build machine, the Pi, comes in.
5. **Cost is small either way.** About $0 to $1 a month at 1,000 users and $5 to $7 at 10,000, with R2's free egress [I, from Cloudflare's published prices and assumed traffic].
6. **The Pi:** comfortably able to build Oregon and Washington (minutes), plausibly the whole US basemap (a few hours, tight on 8 GB), uncertain for a US routing build [I]. As the *serving* origin it adds a home single point of failure, upload use and ISP terms, to save a few dollars a month that R2 already covers; a copy in R2 is needed anyway for when the Pi or home internet is down [I].

## 1. The tile server today [C unless marked]

- **Routes** (`server/pmtiles-worker/src/index.ts`): `/style/offline.json` (bundled 57-layer style, no glyphs or sprite); `/{name}.json` TileJSON with `maxzoom` raised to 15 (`OVERFLOW_MAX_ZOOM`, :306-316, :371); `/{name}/{z}/{x}/{y}.mvt` tiles; 404, 400 and 204 for bad paths, zooms, extensions and empty tiles.
- **Live** (`/us.json`): "Protomaps Basemap" version 4.15.2, bounds −124.85, 24.4, −66.87, 49.6, zooms 0 to 15, layers boundaries, buildings, earth, landcover, landuse, places, pois, roads, water; attribution "© OpenStreetMap"; no build or data date.
- **Caching:** `Cache-Control: public, max-age=86400`, and `caches.default` puts responses in the edge cache (:268, :292). On the live server `/us.json` showed `cf-cache-status: HIT`; a tile showed no cache status [observed].
- **Errors:** only a missing archive is mapped (404); everything else is rethrown, so a 500 [I].
- **The zoom 15 overflow:** z15 tiles are range-read from `build.protomaps.com/<date>.pmtiles` and cached in R2 under `overflow/` (:127-246). The code says "NOT VERIFIED", and Protomaps' tolerance for sustained traffic is unknown (:138-146).
- **No rate limiting.** CORS is `*` (`wrangler.toml:27`). Workers Logs are off for privacy (`wrangler.toml:7-13`).
- **The archive:** bucket `forager-maps`, object `us.pmtiles`, the `20260819.pmtiles` build, 8.8 GB, maxzoom 14 (`README.md:52-54`). Built by hand with `pmtiles extract` and `rclone copyto` (`README.md:56-90`); no script, no schedule, no owner recorded.
- **Deploys:** Cloudflare Workers Builds. The README's production branch (`:28`) is stale; `main` appears to deploy now [I, `docs/audits/2026-09-09-workers-builds-check-uncorrelated.md:13-17`].
- **Routing data:** nothing in the pipeline could produce it.

## 2. What the app would need to switch [C for the citations; I for the list]

- `Basemap.kt`: all three basemaps are raster (:142-172); the class doc records that live use of the Worker was deliberately not adopted, "four times the request volume against a personal-account Cloudflare Worker" (:42-50). A vector Street basemap would be US-only (`UNITED_STATES_ONLY`, :10-17): an owner call.
- `BasemapStyles.kt`: raster style JSON only (`styleJsonFor`, `singleLayerStyleJson`); topo night's Street layer underneath and its 9.5 to 9.7 crossfade were worked out for raster tiles (:69-79, :137, :173) and would be re-derived; `GLYPHS_URL_TEMPLATE` points at `demotiles.maplibre.org` (:362); attribution and credits (:258, :266).
- `SightingsMap.kt`: the cap (:742), style loading (:743-746), the night recolour's gating (:755).
- `NightColour.kt:74-78`: text and halo colours are not recoloured.
- The Worker: a route for a labelled style.
- **Must not change:** `OFFLINE_STYLE_URL` (`OfflineStyle.kt:3-10`), or downloaded regions stop matching.
- **Already in place:** the User-Agent on every MapLibre request (`MapHttpClient.kt:22-36`).
- **Offline downloads** fetch z10 to z15 through the Worker's z/x/y endpoint (`MapLibreOfflineMapRepository.kt:114-118`). MapLibre's PMTiles source cannot be used for offline packs [C, MapLibre's PMTiles example page], but Forager's offline regions go through the Worker's ordinary tile URLs, so that limit does not bite as built [I].

## 3. Styles, schema and licences [C unless marked; re-read the LICENSE files before publishing]

- **OpenStreetMap (ODbL):** credit "OpenStreetMap" linked to its copyright page, collapsible but reachable (OSMF attribution guidelines). Protomaps treats its tilesets as ODbL; treat Forager's the same [I].
- **Protomaps basemaps:** code BSD-3, map design and generated style JSON CC0, tilesets ODbL ("Protomaps © OpenStreetMap"). Fonts Noto Sans (OFL), glyphs built with `maplibre/font-maker`, sprites from MIT-licensed Tangram icons.
- **Builders:** Planetiler (Apache-2.0; its OpenMapTiles profile requires an OpenMapTiles credit), tilemaker. PMTiles spec CC0.
- **Other schemas:** OpenMapTiles and OpenFreeMap styles require a visible CC-BY credit; MapTiler's Outdoor style has no open licence found [I: not usable]. None of the open schemas carries `sac_scale` or `trail_visibility`.
- **The repository's own licence records:** Worker code MIT, adapted from Protomaps (`README.md:8-11`); glyphs today are Open Sans from the demo server with no credit shown (`docs/audits/2026-09-08-data-inventory-for-privacy-policy.md:544`); bundled Noto fonts have no licence recorded. None has been re-checked against the non-commercial, open-source model.
- **The agent's recommended stack** [I]: a Geofabrik extract; Planetiler with a fork of the Protomaps profile adding trail attributes (`sac_scale`, `trail_visibility`, `tracktype`, `surface`, `foot`, `access`, `informal`); PMTiles in R2 behind the Worker; a custom CC0 style with trail layers; self-hosted Noto glyphs; credits for OSM, Protomaps and USGS, with a licence screen.

## 4. Topo: contours and hillshade

- **Elevation data:** USGS 3DEP is public domain, credit requested [C]. The repository already ruled for 3DEP over Terrarium, because of Terrarium's mixed per-source licences (`docs/plans/maplibre-migration.md:319-352`) [C]; `own-map-tiles.md`'s Terrarium candidate conflicts with that.
- **Rendering on Android:** MapLibre draws hillshade from a `raster-dem` source [C]. Contours have to be made ahead of time as vector tiles (the web-only contour plugin does not run on Android) [C/I].
- **Size, Oregon and Washington** [I, arithmetic, not measured]: hillshade to z12 about 0.7 to 2 GB; contours z10 to z14 in the low hundreds of MB. The repository's pipeline proposal: 3DEP, `gdaldem`, PMTiles (`maplibre-migration.md:353-371`).

## 5. Cloudflare costs and limits [C from Cloudflare's published pages; I for the estimates]

- Workers free plan: 100,000 requests a day (errors beyond it, not charges). Paid: $5 a month for 10 million requests, then $0.30 per million.
- R2: $0.015 per GB-month, reads $0.36 per million, **egress free**.
- A cached response still counts as a Worker request; caching saves CPU and R2 reads.
- Estimates, at about 1,000 tiles per user a month: about $0 to $1 a month for 1,000 users (but the free plan's daily cap fails hard on a busy day), about $5 to $7 for 10,000.
- Cloudflare's terms limit serving "a disproportionate percentage of pictures, audio files, or other large files" except through paid services, naming the Developer Platform; small vector tiles do not look like the target, but the terms do not say so [I].

## 6. The Raspberry Pi 5 (8 GB, 256 GB NVMe)

**As the build machine** [C for the published figures; I for the Pi]:
- Planetiler needs RAM of about half the input file and disk of 5 to 10 times it. Oregon (242 MB) plus Washington (347 MB): minutes, trivially. The US (11.3 GB): about 5.7 GB of memory, tight on 8 GB but possible with disk-backed storage, 57 to 113 GB of scratch disk, a few hours. No published Planetiler run on a Pi was found.
- Valhalla and BRouter both have ARM64 builds; neither publishes memory or time figures. Oregon and Washington look comfortable; a US routing build on 8 GB is uncertain.
- Jobs one after another, not at once.

**As the serving origin** [I unless marked]:
- Connected through Cloudflare Tunnel with no open ports [C], reached by the Worker through Workers VPC, which is in beta and free today [C].
- At 10,000 users, about 250 GB a month uploaded from home at a 50% cache-miss rate; peaks well above the 0.8 Mbps average. ISP terms for running a server on a home plan were not researched.
- When the Pi or the home connection is down, only already-cached tiles serve. The robust fallback is a copy in R2, so R2 stays in the picture either way.
- Hardware: a 5 V 5 A supply [C], NVMe boot, and a UPS if it serves users.

**The agent's recommendation:** the Pi builds and uploads to R2 on a schedule; the Worker keeps serving from R2. The Pi as origin only if self-hosting is itself the goal, with R2 as the fallback.

## Premises in `docs/plans/own-map-tiles.md` that are now wrong

1. The label crash's record is cited as "PR #23, `maplibre-migration.md` §7"; that section does not contain it.
2. "A labelled style was generated": it was never committed and cannot be recovered.
3. The night recolour "exists": for backgrounds, fills and lines only.
4. "Serves a continental-US archive from R2": zoom 15 depends live on Protomaps' public host.
5. Terrarium as the elevation candidate conflicts with the earlier ruling for 3DEP.
6. The commercial ruling it is judged against is superseded.

## Decisions this puts to the owner (for the spec, stage 3)

1. **The Pi's role:** build machine with R2 serving (the agent's recommendation), or the serving origin with R2 as fallback (the owner's stated intent).
2. **A custom domain** for the tile server, so Cloudflare's edge cache works properly (about the cost of a domain a year) [I].
3. **Our own build with trail attributes,** rather than extracts of Protomaps' build, so trails can be drawn and routed as navigation treats them.
4. **Ending the zoom 15 dependency** on Protomaps' public host, by building to z15 ourselves.
5. **Coverage:** US only to start, which makes the Street basemap US-only.
6. **Topo:** our own contours and hillshade from 3DEP, or keep OpenTopoMap for now.
7. **Before any online switch,** a device check that a labelled vector map renders online on MapLibre 13.5.0 without the old crash.

## Could not determine

The live archive's actual size and data date today; why tiles show no cache status and whether the edge cache works on `workers.dev`; which branch Cloudflare deploys and whether it matches `main`; Cloudflare plan, usage and the `overflow/` cache's size; the label crash's original evidence (PR #23's description is not in the repository); whether a labelled vector style renders online on 13.5.0; measured sizes for elevation tiles; any Planetiler, Valhalla or BRouter run on a Pi, with figures; Workers VPC pricing after the beta; real tiles per user.
