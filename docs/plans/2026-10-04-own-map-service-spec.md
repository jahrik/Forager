# Spec: Forager's own map service and trail navigation (stage 3)

**Status: draft for the owner to rule on.** Written by the planner on 2026-10-04 (UTC) at `main` `f4e6726a`. Plan: `docs/plans/trail-navigation-and-own-tiles.md`. Inputs: the own-tiles survey, the trail research, the engine spike (with its addendum), the Pi origin report, `map-style/`, and `RECORD.md` -442 to -490.

## Goal

A walker sees Forager's own map, downloaded per area, and navigates along real trails and roads to where they are going or back, guided along their own track when they leave the paths and alerted when they take a wrong turn.

## Scope

- Forager's own map build (map tiles plus trail details) and BRouter routing data, from one OpenStreetMap extract per area, built on the Pi.
- Serving: the Worker at `tiles.zynergy-labs.com` in the zynergy-labs account, the Pi as origin behind Access, R2 as fallback.
- Forager's own Street style in the app, online and in offline regions, by day and at night.
- One download per offline region: map tiles and routing data together.
- Routes along the network to the start and to a waypoint (T7's line becomes this route).
- Off the paths: the walker's own track, the step path of -445; a wrong-turn alert (-447).

## Out of scope

Topo (contours, hillshade; -466: OpenTopoMap stays for now); coverage beyond the US; agency trails merged into routes (-445: reference layer only, later); the old account's service, which keeps serving the app unchanged until the switch (-481).

## Decisions

- OpenStreetMap is the routable base; agency trails a separate reference layer; NPS and Washington RCO data left out (-445).
- BRouter routes: it stayed on trails where Valhalla did not, and is far smaller (-484).
- Trails and roads get the same treatment (-447).
- The Pi serves, behind the Worker, guarded by an Access service token; Cloudflare is the backup (-466, -479).
- zynergy-labs.com is the launch account; the old archive is not copied (-481).
- Forager builds its own tiles with trail attributes, to z15 (-466, 3 and 4).
- The style starts from Protomaps' CC0 design; night is a pure inversion of day (-314, -322).

## Requirements

- R1. One extract per area yields map tiles with trail attributes (`sac_scale`, `trail_visibility`, `informal`, track or path) and BRouter data, in one run on the Pi. Check: both outputs present, dated to the same extract.
- R2. Tiles reach the app only through `tiles.zynergy-labs.com`; with the Pi stopped, the Worker serves from R2. Check: the same tile with the Pi up and down.
- R3. The Worker limits request rates and caches at the edge. Check: cache status on a repeated tile; a burst refused.
- R4. The app draws Forager's style with labels online, in offline regions and at night. Check: the S22, three zooms, aeroplane mode, night mode.
- R5. A region download is one action that stores map and routes, and its size is shown before it starts. Check: a region routes in aeroplane mode.
- R6. A route to the start or a waypoint follows trails and roads where they exist. Check: the three spike routes on the phone.
- R7. Leaving all paths switches quietly to the walker's own track; a wrong turn onto another path alerts once. Check: a walk with both.

## Constraints

`OFFLINE_STYLE_URL` and existing offline regions keep working until migrated; map chrome over the map at 80%; positions never enter the repository; every version pinned; the User-Agent on every request.

## Acceptance

R1 to R7 on the S22, on a town walk and a forest walk, with the Return record (-451) to read them by.

## Unverified

That labels draw offline on 13.5.0 (dispatch -490 is measuring it). That a US build fits the Pi's 8 GB. That one extract's map and routing data stay in step through refreshes.

## Open questions

1. **Coverage first:** Oregon and Washington (builds in minutes on the Pi), or the whole US (hours, tight on memory)?
2. **Region size:** a ceiling for one download (the spike's 60 × 60 km region: about 0.6 MB routing plus the map tiles).
3. **Telling which path the walker is on:** Forager's own matching against the routing data, or Valhalla's matcher alongside BRouter (about 8 MB more in the app, more data)?
4. **T7:** wait for routes along the network, or ship first along the walked track?
5. **Refresh:** how often the Pi rebuilds (weekly is the planner's suggestion).
