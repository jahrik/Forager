# 2026-09-27: map layers L0a (the layer model), stopped before building

Dispatch: `prompts/preserved/2026-09-27-29.md` (build, L0a coder). Plan: `docs/plans/journal-redesign.md`
("Map layering framework" and everything under it, including "L0 design rulings"). Read before building:
`docs/audits/2026-09-27-map-layers-and-forecast-data-pulse.md` (the pulse) and
`docs/audits/2026-09-27-prediction-map-layers-prior-art.md`. Written by the L0a coder in a cloud worktree
on local branch `l0a`, cut from `origin/journal-redesign` at `6d1fced` (the planner's store-copy commit
after `9a81a7e`, which the dispatch allows).

**Status (superseded 2026-09-27: built after the owner's rulings, see the last section): stopped at the premise check. Nothing was built and no test was written.** Two findings
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

## Built after the owner's rulings (second L0a coder, 2026-09-27)

Dispatch: `prompts/preserved/2026-09-27-30.md` (the rulings) over `2026-09-27-29.md`. Branch cut from
`origin/journal-redesign` at `488d361` as local `l0a-2` (a local `l0a` already existed in the first
coder's worktree); every push to `journal-redesign`. The premise table above was re-read at `488d361`
and still holds with its shifted line numbers. `/opt/android-sdk` and the Gradle cache were present;
every run was `--offline`.

**Paused note.** The planner's pause request arrived after every step below had finished: all work,
the ten revert checks and the full suite are done and pushed. Nothing is left uncommitted. Written
briefly at the planner's request.

### Commits

| SHA | What |
|---|---|
| `193400f6` | Tests first (A1, A3, A4, A5) with stubs: 4 classes / 46 tests / 36 failures. The 10 that passed are invariant guards a stub cannot break (for example, an empty registry has no problems). Revert checks cover them. |
| `337e5b53` | `ui/map/layers/`: the registry, `registryProblems`, `orderedLayers`, `layerPaintFor`, `activeLayerCredits`, `tapWinner`/`resolveTap`; `mapCreditsFor`/`attributionCaption`. All 36 pass. |
| `e3591aa1` | `SightingsMap` builds its layers from the registry, applies state without a style reload, and uses the new tap listener. Record ids are carried through the use case, `CartographyEntryMapData` and `MapOverlayContent`, and the entry map uses real waypoint ids. At this commit, feature-id tests were written first: 7 failed with `expected:<[ids]> but was:<[null...]>`. |
| `b0ce1bea` | The builders write `featureId`. The 7 failures pass. |

### What was built, per ruling

1. **Draw order.** `MAP_LAYER_REGISTRY` (`ui/map/layers/MapLayers.kt`) lists the layers bottom to top: offline fill (areas); outline, breadcrumb casing and line, kept-track casing and line (lines); search centre, sightings, planned trips, waypoints, finds, photos (markers). The colour-field group is empty. The only change from before is that the search centre and the sighting dots now draw above the lines. This is recorded in the registry's doc and in `initializeOverlayLayers`, and a test pins that nothing else moved. The offline night recolour still runs before the loop, and the stale order comment is replaced.
2. **Ids.** New `domain/model/RecordGeometry.kt` (`RecordPoint`, `RecordPolyline`, `RecordRegion`). The use case keeps `trackId`, `findId`, `photoId` and `waypointId`, plus `offlineRegionId` as decimal text, so every layer's feature id is text. No Room or DAO change was needed. The search centre and the breadcrumb have fixed ids (`search-centre`, `breadcrumb`). The id travels in a string property `featureId`, not the GeoJSON feature id, because a property is what has been seen to round-trip on hardware (sightings' `observationId`). Sightings are unchanged.
3. **Opacity.** `LayerState.opacity` is a multiplier from 0 to 1 on each base opacity. It refuses values outside that range at construction rather than clamping them. Base opacities live in the registry (0.7 and 0.85 for sightings, 0.2 for the offline fill, 1 everywhere else), and `SightingsMap` builds each layer with them. A casing follows its track's state, and the offline outline follows the fill's.
4. **Taps.** The outline is tappable and the fill is not (`TapGroup.NONE`), and neither are the casings. The listener queries each tappable layer separately, because a returned Feature does not name its layer. The box is **48 dp square (±24 dp)**, the Android/Material minimum touch target. It is queried **only when nothing tappable lies exactly under the tap point**. I added the point-first stage so that a near miss inside the box cannot beat a marker the finger is directly on. See Needs a decision.
- A3 plumbing: `MapRenderMode.layers` (default = today) and `MapRenderMode.onFeatureTap(layerId, featureId)` (default no-op). There is no new `MapSlot` parameter; it stays at 9. A `LaunchedEffect(loadedStyle, layersState)` sets `visibility` and opacity through `setProperties`, with no `setStyle`.
- A5: the caption is `attributionCaption(mapCreditsFor(basemap, offline, activeLayerCredits(...)))`, joined with " · ". With a single credit the output is unchanged (pinned for every basemap and the offline style).

### Tests

- **Full suite** (cleared results dir, at `b0ce1bea`): **253 classes / 2112 / 1 / 0 / 24**. That is the base (248 / 2052) plus 5 classes and 60 tests. The one failure is in the held family: `JournalPendingDeleteTest` "Undo on an album photo brings it back and deletes neither row nor file", `The component with TestTag = 'tile-options-delete' is not displayed!`. Its class rerun alone: 52 / 0 / 0 / 0. I did not touch it.
- **Existing tests changed** (all because of ruling 2's new types; no assertion weakened): `SightingsMapOverlayDataTest` (the kept-track, point and offline-circle builder inputs are wrapped), `GetCartographyEntryMapDataUseCaseTest` (its expected values now include the record ids, so it asserts the ids), `CartographyEntryMapDataTest` (the helper wraps its inputs), `CartographyEntryReportScreenMapTest` (inputs wrapped, plus one new test for real waypoint ids), `CartographyEntryReportScreenFullscreenTest` and `NetworkFixExclusionPerConsumerTest` (types only).
- **Revert checks** (`app/build/l0a/revert.py`: it restores from a saved copy, refuses to read results if the build log has a compile error, and I confirmed the tree was clean afterwards). All 10 compiled (0 `e:` lines), and each failed with a message specific to its own edit:
  - Topmost-in-group becomes bottom-most: waypoints-over-sightings and kept-over-breadcrumb fail.
  - Box-first instead of point-first: "what lies under the point decides" fails (the photo wins).
  - Multiplier dropped: 4 failures.
  - State owner ignored: the casing and outline tests fail.
  - Search centre moved back below the lines: 5 registry failures.
  - User order dropped: the colour-field order test fails.
  - Layer credits dropped: 3 failures.
  - Point builder id removed: the find and photo id test fails.
  - Use case find id set to "": that test fails.
  - Entry-map waypoint id set to a constant: the new screen test fails.
- **Predictions:**
  - 1 (no existing test changes): wrong, because ruling 2 widened the scope.
  - 2: the state fits in `MapRenderMode`, but `MapOverlayContent`'s four marker fields changed type (ruling 2).
  - 3 (25 to 50 new tests): 60.

### Needs a decision

- **Point first, then the box.** My addition to ruling 4, for the reason given there. A literal box-only query would let a waypoint 20 dp away beat a sighting dot directly under the finger (topmost within the group). Revert if the owner wants box-only.
- **Tap behaviour changes that follow from the rulings**, and are not draw-order changes:
  - a tap on a planned trip, waypoint, find or photo drawn over a sighting dot now goes to that marker (no bubble; `onFeatureTap` has no UI yet);
  - a tap that misses every pixel but has a sighting dot within 24 dp now opens its bubble, anchored at the tap point as before, and re-anchored on the next camera idle.
- **`onTap` still fires after `onFeatureTap`**, so the fullscreen map's "tap to restore chrome" is unchanged. M1 may want otherwise.
- **Provisional registry flags for L0b.** Toggleable: finds, photos, waypoints, planned trips, breadcrumb, kept tracks and the offline fill; not sightings or the search centre, which are absent from the owner's overlay list. Opacity slider: colour fields only. Reordering: colour fields only. A change to the stored reorder list takes effect at the next style load; the group is empty in L0a.

### Device-only (for J7)

`SightingsMap` cannot run under Robolectric, so these are device-only:

- the twelve layers' actual draw order, especially the dots and the reticle over a recording trail;
- that explicit `visibility: visible`, `line-opacity 1` and `icon-opacity 1` look identical to before (the MapLibre spec defaults; not observed);
- that `queryRenderedFeatures` returns the `featureId` property, and that the RectF box query works;
- that hidden layers are excluded from queries (unverified);
- that a tap inside an offline circle away from its outline falls through;
- that the sighting bubble is unchanged for direct taps;
- that the night recolour still leaves the overlays alone;
- the caption text.

### D58

`git grep -i` over every commit's diff and message for the three forbidden phrases: zero hits.
