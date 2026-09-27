# 2026-09-27: map layers L0a (the layer model), stopped before building

Dispatch: `prompts/preserved/2026-09-27-29.md` (build, L0a coder). Plan: `docs/plans/journal-redesign.md`
("Map layering framework" and everything under it, including "L0 design rulings"). Read before building:
`docs/audits/2026-09-27-map-layers-and-forecast-data-pulse.md` (the pulse) and
`docs/audits/2026-09-27-prediction-map-layers-prior-art.md`. Written by the L0a coder in a cloud worktree
on local branch `l0a`, cut from `origin/journal-redesign` at `6d1fced` (the planner's store-copy commit
after `9a81a7e`, which the dispatch allows).

**Status: stopped at the premise check. Nothing was built and no test was written.** Two findings
trigger the dispatch's abort conditions, and a third is an open design question the build cannot go
round:

1. **A visible change in draw order (abort condition).** The z-groups the dispatch asks for
   (colour fields < areas < lines < markers) do not reproduce today's order. Two markers, the
   search centre and the sighting dots, draw **below** every track line today. Putting them in the
   markers group moves them above the breadcrumb and the kept tracks. That is certain, not
   possible, and it shows on the main Maps tab while a track is recording. Details under Needs a
   decision 1.
2. **Stable feature ids cannot be added in the pure builders for four layers without changing
   what their sources receive (open design question).** A4 asks for a stable id on every
   tappable feature, added in the builders. Finds, photos, kept tracks and offline-region circles
   reach `SightingsMap` as bare coordinates with no record id, and the dispatch also says "do not
   change what data each source receives". The two instructions cannot both hold for those four.
   Needs a decision 2.
3. **What a layer's opacity means (open design question).** One number per layer cannot restore
   today's sighting layer, which has two different opacities. Needs a decision 3 gives a lean.

I stopped rather than choosing. Everything below was read at `6d1fced`. Nothing else in the dispatch
is blocked: A5 (attribution as a list) does not depend on any of the three, and A3 depends only on
decisions 1 and 3.

## Commits (pushed to `journal-redesign`)

- This report only. No production or test file changed.

## Premises checked before building

Paths under `app/src/main/java/com/zynergylabs/forager/app/`. The pulse was taken at `3025bab`. Three
commits have touched `ui/map/` since then: `70f4bef`, `72c9e12` and `0086f8f`, the picker fixes F1 to
F3, which added `onUserCameraGesture`. They moved `SightingsMap.kt`'s line numbers by +9 and
`MapSlot.kt`'s by about +21. **Every citation's content still holds.** Only the line numbers are
stale, so I treated this as a shifted premise, not a wrong one.

| Dispatch or pulse citation | At `6d1fced` | Holds? |
|---|---|---|
| Night recolour before overlays, `SightingsMap.kt:459-460` | `:468-469` (`applyOfflineNightRecolour`, then `initializeOverlayLayers`) | yes, shifted |
| Stale order comment, `:614-615` | `:623-624` ("search centre, sightings, planned trips last") | yes, shifted; still stale |
| Click listener, `:312-332` | `:315-335`; queries `SIGHTING_LAYER_ID` only | yes, shifted |
| `initializeOverlayLayers`, pulse `:630-696` | `:639-705` | yes, shifted |
| Ids, pulse `:1137-1159` | `:1154-1176` | yes, shifted |
| `refreshOverlayData`, pulse `:718-741` | `:727-750` | yes, shifted |
| Data effect, pulse `:482-491` | `:491-500` | yes, shifted |
| `MapSlot` compiler ceiling, `MapSlot.kt:14-26` | `:14-26` | yes |
| `MapSlot` has 9 parameters | `MapSlot.kt:284-330`: region, content, renderMode, focusOverride, onLongPress, onTap, onSightingTap, onCameraIdle, modifier (9) | yes |
| `MapRenderMode` / `MapOverlayContent` | `MapSlot.kt:40-159` / `:161-251` | yes, shifted |
| `mapAttributionFor`, `BasemapStyles.kt:130-131` | `:130-131`, one `String`; one production caller (`SightingsMap.kt:604`) and four assertions in `OfflineStyleSwapTest.kt:65-73` | yes |
| Insertion order is the z-order; no `addLayerBelow/Above/At` | `git grep` over `app/src/main` finds none, and no `moveLayer` either | yes |

### Today's draw order, bottom to top (read at `SightingsMap.kt:639-705`)

| # | Layer id | Type | Kind the dispatch would give it |
|---|---|---|---|
| 1 | `offline-region-circles-layer` | fill | area |
| 2 | `offline-region-circles-outline-layer` | line | line (area outline) |
| 3 | `search-center-layer` | symbol | **marker** |
| 4 | `sightings-layer` | circle | **marker** |
| 5 | `breadcrumb-trail-casing-layer` | line | line |
| 6 | `breadcrumb-trail-layer` | line | line |
| 7 | `kept-tracks-casing-layer` | line | line |
| 8 | `kept-tracks-layer` | line | line |
| 9 | `planned-trips-layer` | symbol | marker |
| 10 | `waypoints-layer` | symbol | marker |
| 11 | `find-markers-layer` | symbol | marker |
| 12 | `photo-markers-layer` | symbol | marker |

With the groups in order (areas < lines < markers), layers 3 and 4 must move above layers 5 to 8.
Every other layer keeps its relative position.

### Where the move would show

- The main Maps tab draws both sightings and the breadcrumb: `AvailabilityCompactMapUi.kt:599-601`,
  `AvailabilityWideLayoutUi.kt:251-253`. While recording, wherever the trail or its casing crosses a
  sighting dot or the search reticle, today the line draws over the dot. After the move the dot
  would draw over the line.
- The Cartography entry map (`CartographyEntryReportScreen.kt:355-382`) passes no sightings and
  sets `showSearchCentre = false`, so the move changes nothing there.
- I found no record of the current order being chosen on purpose. The breadcrumb source has come
  after the sighting source since at least `892883c`, and C2's casings (`6a53995`) kept that
  position. No doc or comment in `app/src` or `docs` says the trail should draw over the dots
  (`git grep` for the phrasings). The comment at `:623-624` predates the breadcrumb and does not
  mention it.

### Which features carry an id today (read at `SightingsMap.kt:902-1149`)

| Builder | Input type | Id property today | A real record id available in the input? |
|---|---|---|---|
| `sightingsFeatureCollection` | `List<Sighting>` | `observationId` | yes, already used |
| `plannedTripsFeatureCollection` | `List<PlannedTrip>` | none | yes, `PlannedTrip.id` (`domain/model/PlannedTrip.kt:20`) |
| `waypointsFeatureCollection` | `List<Waypoint>` | none | yes on the main map (`Waypoint.id`, `domain/model/Waypoint.kt:23`). **On the entry map it is fake**: `CartographyEntryReportScreen.kt:358-369` synthesizes `"cartography-map-waypoint-$index"`, and its own comment says nothing reads it |
| `searchCenterFeatureCollection` | `Region` | none | a singleton; a constant id would do |
| `breadcrumbFeatureCollection` | `List<LatLng>` | none | a singleton; a constant id would do |
| `keptTracksFeatureCollection` | `List<List<LatLng>>` | none | **no** |
| `pointsFeatureCollection` (finds, photos) | `List<LatLng>` | none | **no** |
| `offlineRegionCirclesFeatureCollection` | `List<Region>` | none | **no** (`Region` is lat, lng, radius only, `domain/model/Region.kt:4-7`) |

The record ids exist one step upstream. `GetCartographyEntryMapDataUseCase.kt:60-89` has
`decision.trackId`, `decision.findId`, `attachment.photoId` and the offline-region decisions in hand,
and drops them when it maps to `LatLng` and `Region`. `CartographyEntryMapData`
(`GetCartographyEntryMapDataUseCase.kt:105`) has no id fields.

### Opacity today (read at `SightingsMap.kt:645-719, 1073-1084`)

- The sighting circle has `circle-opacity` 0.7 **and** `circle-stroke-opacity` 0.85.
- The offline fill has `fill-opacity` 0.2.
- Lines and symbols set no opacity, so MapLibre's default of 1 applies.

### Premises that were wrong, and predictions

- **"Keeping each layer's exact paint and layout (so nothing visible changes)" together with the
  four z-groups in order.** These cannot both hold (finding 1).
- **"Add [a stable id] in the pure builders where missing" together with "do not change what data
  each source receives".** These cannot both hold for four layers (finding 2).
- **Pulse line numbers.** Stale by +9 in `SightingsMap.kt` after F1 to F3. The content is correct.
- **Predictions 1 to 3.** None can be evaluated, because nothing was built. On prediction 2, the
  layer state would fit in `MapRenderMode` as a defaulted field. But if decision 2 goes to option
  (b), `MapOverlayContent`'s marker fields change.

## What was built

Nothing.

## Tests

None written and none run, the full suite included. At the planner's `3b09e43` the suite was 248
classes / 2052 / 1 / 0 / 24. I did not re-measure it.

## Needs a decision

**1. The search centre and the sighting dots against the track lines.**

- (a) **Strict groups (the dispatch's four groups as written).** The search centre and the
  sightings go into markers, above the tracks. This draw order agrees with the tap order the owner
  ruled ("markers, then lines"), so what a finger wins is what is drawn on top. It is a visible
  change on the main map while recording, it goes on the J7 list as a device item, and the dispatch's
  "nothing visible changes" is relaxed for these two layers only.
- (b) **Keep today's order exactly.** This needs a fifth group, or an exception inside markers,
  that puts these two below lines. The registry would no longer read as the four groups in order.
  Draw order and tap order would also disagree for these two: a dot drawn under a track would still
  win the tap. It already does today, since only the sighting layer is queried.
- Lean, as information and not a choice: (a). It is the convention the prior-art pass records
  (point markers above lines), and it matches the tap ruling. But the dispatch told me to stop on
  exactly this.

**2. Stable ids for finds, photos, kept tracks and offline-region circles, and real ids for the
entry map's waypoints.**

- (a) **Index ids in the builders** (for example `find-3`). This meets "carries an id" in form. The
  id is stable only while the list is unchanged, and nothing downstream can map it back to a record,
  so the generic callback would carry an id no caller can resolve. M1 would have to replace it.
- (b) **Carry record ids through.** Widen the four `MapOverlayContent` fields, `CartographyEntryMapData`
  and `GetCartographyEntryMapDataUseCase` to keep the ids they already hold (for example a small
  `MapMarker(id, LatLng)`), and stop synthesizing waypoint ids in `CartographyEntryReportScreen`. This
  changes a domain use case and a caller, which is outside the dispatch's file scope and against
  "do not change what data each source receives". It is what M1's bubbles and J8 will need anyway.
- (c) **Real ids only where they exist today.** Sightings, planned trips and main-map waypoints get
  real ids; the search centre and the breadcrumb get constant ids. The other four are deferred to M1,
  which needs the record anyway. For those, the generic callback carries no feature id, or does not
  fire.
- No lean from me between (b) and (c). The difference is whether L0a or M1 owns the domain change.

**3. What a layer's opacity is.**

- A **multiplier from 0 to 1 on each layer's own base opacities, default 1**, would keep today's
  values exactly. The sighting fill would stay 0.7 and its ring 0.85, the offline fill 0.2, and lines
  and symbols 1. Each property would get base × multiplier: `circle-opacity` and
  `circle-stroke-opacity`, `fill-opacity`, `line-opacity`, `icon-opacity`.
- An **absolute** value cannot represent the sighting layer's two opacities with one number.
- Lean: the multiplier. I list it because I had already stopped. It would not have been an abort on
  its own.

**4. Smaller points for A4, which I would otherwise have settled as noted.**

- The owner's tap ruling names the offline-map outline, not the fill. My reading: the fill is not
  tappable, and the outline is in the lines group.
- `queryRenderedFeatures` at a single screen point, which is what the listener does today, only hits
  a line where its rendered pixels are. That is 1.5 dp for the offline outline and 6 to 9 dp for the
  tracks, so a finger will often miss them. A box query around the tap point is the usual answer. It
  changes what "the topmost layer under the finger" means for the precedence function's input, so it
  belongs in A4 if the planner wants it, or in M1.

## Decisions I made that the dispatch did not

- I treated the stale line numbers as shifted premises, not wrong ones, because every cited
  behaviour is where the pulse says, 9 lines lower.
- I stopped the whole stage rather than building A5 alone. A5 is independent, but the dispatch lists
  the abort conditions as stop conditions. I left it for the resumed session, so the stage lands in
  one piece.

## Device-only items (for J7, once built)

`SightingsMap` cannot run under Robolectric (native MapLibre; `SightingsMapOverlayDataTest.kt:18-54`
records why). The following would be proven only on a device:

- the actual draw order of the twelve layers, including the move in decision 1 if (a) is chosen;
- visibility through the `visibility` layout property, and opacity through each paint property, with
  no style reload;
- the native hit-test (`queryRenderedFeatures`) returning features from every tappable layer, and
  the sighting bubble unchanged;
- the offline night recolour still leaving overlays untouched after the loop replaces the hand-ordered
  adds;
- the attribution caption's joined text, as rendered in the corner.

## Flags outside scope

- `CartographyEntryReportScreen.kt:358-369` gives the entry map's waypoints synthetic ids with the
  comment "this map has no tap handling". That stops being true once A4 lands, whichever option
  decision 2 takes.

## D58 check

`git grep -i` for the three forbidden phrases over this report and this commit's message: zero hits.
