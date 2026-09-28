# 2026-09-28: track widths by zoom (T1), the entry map's opening frame (T2), the search-bar copy (T3)

**Status (continuation `2026-09-28-35`): built. See the last section, "Resumed".** T1, T2 and T3 are built and
pushed. They went tests first (20 failing at the stubs, each for its stated reason), then through 16 revert checks,
all of which compiled and were restored from saved copies, and all of which are confirmed (R02 after one fragment
correction, stated below). The full suite at `da5ad1b` is 280 / 2265 / 0 / 0 / 24. The sections before "Resumed" are the
stop report as it stood at `3d3d3c3`, left as written.

**Status: stopped at verification, before writing any code or test.** T2 relies on a premise that a standing
owner ruling contradicts: that an entry keeping only offline regions has a map to frame. The dispatch lists both
"a wrong premise" and "an unruled design question" as abort conditions. Details are in Q1. T1 and T3 have no
open question. I held them back as well because a met abort condition stops the dispatch (see Decisions). Nothing
under `app/` has changed.

Dispatch: `prompts/preserved/2026-09-28-34.md`, intent `2026-09-28-34` (written by the planner). Written by the coder in
worktree `forager-wt/tracks-frame`, local branch `tracks-frame`, cut from `origin/journal-redesign` at `2e02c01`.
Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Line numbers are at `2e02c01`.

## Base

- After `git fetch`, `origin/journal-redesign` was `2e02c01`, the commit that carries the dispatch. **Confirmed.**
- The kit is absent at this base (no `.claude/`, no checkers). I made no record entry, as the dispatch says.
- Disk: 1.3 GB free at the start and 1.2 GB after the worktree was created. No Gradle run was made.

## Verification

### T1, today's line widths

All four track line layers are built by `trackLayerSpecs()` (`ui/map/SightingsMap.kt:1282-1311`) as
`LineLayerSpec`s (`:1264-1275`). One function turns a spec into a layer, `lineLayerFor`, which passes the width as a
constant: `PropertyFactory.lineWidth(spec.widthDp)` (`:1335`).

| Layer | Width | Where |
|---|---|---|
| Breadcrumb | 6 dp | `BREADCRUMB_STROKE_WIDTH_PX = 6f` (`:1581`), used at `:1293` |
| Breadcrumb casing | 9 dp (6 + 2 × 1.5) | `casingFor` (`:1283-1288`), `CASING_WIDTH_DP = 1.5f` (`:1481`) |
| Kept tracks | 6 dp | `KEPT_TRACK_STROKE_WIDTH_PX = 6f` (`:1585`), used at `:1301` |
| Kept-track casing | 9 dp | `casingFor`, as above |
| Offline outline | 1.5 dp, dashed | `OFFLINE_REGION_CIRCLE_OUTLINE_WIDTH_PX = 1.5f` (`:1586`), `offlineRegionOutlineSpec` (`:1319-1330`) |

- **Planned trips are not lines.** Each trip is a `Point` feature (`plannedTripsFeatureCollection`, `:1357` onward),
  drawn as a marker. T1 would therefore change four line layers, and **prediction 2 holds** on reading.
- No other `LineLayer` or `lineWidth` exists in `main/` (`grep`).
- Today's casing-to-line ratio is 9 / 6 = 1.5. Keeping that ratio at every zoom makes the casing's edge 1.5 dp a side
  at z15 and 0.6 dp a side at z11 (casing 3.6 dp over a 2.4 dp line). That is the dispatch's wording, applied
  exactly. I note it only because the casing is additive today (`+ 2 * CASING_WIDTH_DP`).
- `line-dasharray` is in multiples of the line width (`:1270`), so the breadcrumb's dots and gaps would shrink with
  the width. This is a device-only look.
- **An existing test pins today's widths:** `SightingsMapOverlayDataTest` "each track has a solid casing line
  directly below it, wider by the casing on each side, in the casing colour"
  (`app/src/test/java/com/zynergylabs/forager/app/ui/map/SightingsMapOverlayDataTest.kt:235-259`, asserting 6f at `:248`
  and 9f at `:254`). The offline outline's test (`:268-279`, 1.5f) would stay as it is.
- Expressions can be compared headless with `Expression.equals`, as `SightingsMapOverlayDataTest.kt:187-192` and
  `ForecastCellLayerTest.kt:53-57` already do, so the width expression can be tested first as the dispatch asks.

### T2, the entry map's camera today

- **The camera is not fixed and not last-known, so prediction 1 misses.** The screen computes
  `mapRegion = GeoDistance.boundingRegion(resolvedMapData.allPoints)` (`ui/log/CartographyEntryReportScreen.kt:371`)
  and passes it as `MapSlot`'s `region` (`:384`). `SightingsMap` moves the camera to that region's centre at
  `zoomForRadiusKm(region.radiusKm)` (`ui/map/SightingsMap.kt:629-640`). It does this only when `(region, focusOverride)`
  differs from the last target it applied (`shouldMoveCameraToTarget`, `:1034-1038`), so in practice it moves once
  per entry.
- `boundingRegion` (`domain/GeoDistance.kt:132-142`) takes the box midpoint as the centre. Its radius is the farthest
  point's distance from that centre, rounded to whole km and clamped to 1 to 50 km.
- `zoomForRadiusKm` (`ui/map/SightingsMap.kt:1614-1619`) returns 13.0 for up to 5 km, 12.0 for up to 15 km, 10.5 for
  up to 30 km, and 9.0 above that.
- `allPoints` (`domain/GetCartographyEntryMapDataUseCase.kt`, on `CartographyEntryMapData`) is the drawable points
  **plus every kept offline region's centre**.
- The live-location camera does not interfere: `trackLiveLocation = false` on this map (`CartographyEntryReportScreen.kt`,
  `MapRenderMode(...)`), and `SightingsMap` gates both activations on it (`:599`, `:711`).
- **Why it can open away from the content.** This is inferred from the code. I have not seen the owner's screenshot,
  so I cannot say which cause produced it.
  - A kept region's centre is part of the frame, so a region downloaded somewhere else pulls the centre toward it.
    It also inflates the radius, and so zooms the map out.
  - The zoom is a four-step heuristic with a ceiling of 13.0, so a short track opens at z13 whatever its size. At z13
    a walk of a few hundred metres is a few dp across, which matches "a blank map".
  - Above a 30 km radius the zoom is 9.0. By my own arithmetic (about 216 m per dp at z9, latitude 45), a 4:3 preview
    about 270 dp tall shows roughly ±29 km vertically, so points up to 50 km from the centre can fall outside it.
    That arithmetic is unverified.
- **The case with nothing to frame today.** The map section is absent, with no map frame at all, whenever
  `CartographyEntryMapData.isEmpty` is true (`CartographyEntryReportScreen.kt:371-372`, `takeUnless { it.isEmpty }`).
  `isEmpty` is `drawablePoints.isEmpty()`, and `drawablePoints` **excludes offline regions**.
- **Fullscreen and the preview share one `mapSlot` call** (`:383`), whose enclosing `Box` only changes its `Modifier`
  (`:373-379`). The `MapView` and its camera therefore survive the switch.
- The M1 find overlay leaves the day entry composed underneath it (M1 report, "Entry map, 'Open find'"), so a
  once-per-`MapView` guard would survive an overlay round-trip. I have inferred this from that report and not run it.

### T3, the search-bar copy

- **`ui/availability/AvailabilitySearchUi.kt:497` is confirmed:** `?: "no location set"` in `activeSearchSummary`
  (`:492-505`). The month and the `" · "` join are at `:493` and `:504`.
- **`activeSearchSummary` has two readers,** and tapping each does something different.
  - **Compact bar,** `SearchEntryBar`'s `restingPlaceholder` (`:280`). The tap focuses the species field
    (`ACTIVE_SEARCH_SUMMARY_TAG`, `:278`). Focusing it calls `onFieldFocused`, which opens `SearchDropdown` (doc
    comment at `:174-176`) with location, radius and month. **This does open the search, including location.**
  - **Wide layout,** `ActiveSearchSummary` (`:135-161`), called at `ui/availability/AvailabilityScreen.kt:1426`. The
    tap calls `onReopenTaxonSuggestions` (`:141`). That function (`ui/availability/AvailabilityViewModel.kt:385-388`)
    only re-runs the last species query, and **does nothing when nothing has been searched yet**. That is exactly
    when "no location set" shows. **On the wide layout, tapping "Search a location" would not open the search.** As
    the dispatch directs, I report this and build no new behaviour.
- **Other strings that say "no location" in the search or location UI.** These are flags, and none would change:
  - `ui/availability/AvailabilityOfflineMapsUi.kt:198`: "No location picked yet — pan the map above and tap OK."
    (offline-region picker);
  - `app/src/main/res/values/strings.xml:18`: `log_entry_no_location`, "No location set." (a find's report, not the
    search UI; tests pin it at `JournalTabTest.kt:312`, `LogEntryDetailScreenTest.kt:104` and
    `LogEntryReportScreenTest.kt:148, 195`).
- **No test pins the old text.** `git grep -n "no location set" -- app/src/test` returns nothing, which confirms the
  dispatch's 2026-09-28 check.

## Needs a decision

**Q1. Does an entry that keeps only offline regions now get a map?**

The dispatch's T2 says "offline-region circles count toward the bounds only when the entry keeps nothing else", and
it lists "regions only" as a test case. The plan's owner answer reads "offline-region circles are left out of the
framing unless the entry keeps nothing else." Both assume a regions-only entry has a map to frame.

It does not. The owner's plate-pulse ruling on item 4, "a green circle with nothing in it is not a day"
(`docs/audits/2026-09-07-cartography-plate-renderer-pulse.md:567-571`), is applied to this screen by
`CartographyEntryMapData.isEmpty`, which excludes regions, and by the gate at `CartographyEntryReportScreen.kt:371`.
Under that gate the "regions only" rule can never run, because the map never shows. The dispatch's "an entry with no
geometry at all keeps today's behaviour" does not settle it either: a region circle is geometry, and today it gets no
map.

The options I can see:
- **(a)** Keep the gate, so a regions-only entry still gets no map. Build the pure framing with its regions-only
  branch as specified and tested, and record that branch as unreachable from the screen today. The cost is a tested
  rule with no production caller, which is CLAUDE.md's "check reachability" family.
- **(b)** Reverse item 4 for this screen. A regions-only entry gets a map framed on its circles. This widens the scope
  to `domain/GetCartographyEntryMapDataUseCase.kt` (`isEmpty`, or a new property for the gate), and possibly to the
  offline-coverage check that reads `drawablePoints`. It also changes an owner ruling.
- **(c)** Drop the regions-only rule. With the gate kept, regions never count toward the bounds, since the map only
  shows when something else is kept. The pure function then has no regions branch, and the "regions only" test
  asserts "nothing to frame".
- **Lean:** (c) or (a), since both keep the owner's standing ruling. (c) leaves no unreachable branch. Choosing
  between them, or choosing (b), is the owner's or the planner's decision, not mine.

## Mechanism I would build for T2 (stated, not built; for review with Q1)

- **The pure frame.** A new file `domain/EntryMapFrame.kt` (a name I chose) takes the entry map's data and returns
  one of three values:
  - `Fit(bounds, paddingDp = 48, maxZoom = 17)`;
  - `Point(at, zoom = 16)`, when every point is the same location, which is how I would read "a single point";
  - `null`, when there is nothing to frame.
  Circles would enter the bounds through `GeoDistance.boundingBox(centre, radiusKm)` (`domain/GeoDistance.kt:69`), and
  only if Q1 keeps them.
- **The camera request.** A new defaulted field on `MapRenderMode`, carrying an id and a frame (no tenth `MapSlot`
  parameter). `SightingsMap` applies a request once per id, after the existing region move in the same effect, so the
  frame wins on open. A `Fit` goes through MapLibre's `getCameraForLatLngBounds` with 48 dp of padding in px, and its
  zoom is then capped at 17.
  - The fit-zoom arithmetic is native, so it is device-only.
  - The alternative is a pure Web-Mercator fit, which needs the viewport size passed down.
- **"Not re-sent after the overlay."** The screen would create the request once per entry
  (`remember(entry.id)`). The UI test's stub slot would record each distinct request id it receives, and would
  assert one id before and after an M1 "Open find" round-trip.
- **Fullscreen and the preview.** One request, applied at the preview's size. Entering fullscreen does not re-fit,
  because "the frame is applied once, on open".
- **Landscape.** An Activity recreation would build a new `MapView` and could re-fit. Whether this Activity is
  recreated on rotation is something I have not checked.

## What landed

This report only. No production or test file changed.

## Tests-first messages

None. No test was written and none was run.

## Revert checks

None.

## Suite

Not run. Nothing is built, and the disk has 1.2 GB free.

## Device-only

Nothing is built. The dispatch's list stands for the build:
- the widths across zooms on Topographical and Street and at night;
- the entry opening framed on the S22, in the preview, in fullscreen and in landscape;
- the new copy.

To that list I would add the breadcrumb's dots shrinking with the width (T1 above).

## Decisions I made

- **I followed the dispatch over my agent definition on the record.** My definition asks for a sweep, an intent and
  the kit's checkers. The kit is absent at this base, and the dispatch says the planner writes the record. I touched
  nothing in `RECORD.md` or `prompts/`.
- **I did not validate the dispatch's sections against a kit config**, because none exists at this base.
- **I stopped all three items**, not only T2. T1 and T3 have no open question and do not depend on Q1. I read "a met
  abort condition is a stop" as applying to the dispatch. The planner may prefer to release T1 and T3 now.
- **I wrote the stop as the dispatch's named completion report and pushed it**, following the M1 and L0b
  precedent, so the findings exist outside a hand-back.
- **I treated the regions-only conflict as an abort** and did not pick option (a), which would have let me build
  everything as written.
- **The T2 mechanism above is mine**, stated as what I would build: the file name, the reading of "single point",
  the id-carrying request, and MapLibre's fit rather than a pure one.

## Flags outside scope

- **The wide layout's search summary is not a call to action today** (T3 above). Tapping it with no prior search does
  nothing, so the new copy promises something that tap does not do there.
- `ActiveSearchSummary`'s KDoc says the compact tab no longer uses it (`AvailabilitySearchUi.kt:163-169`), but
  `AvailabilityScreen.kt:1426` still uses it on the wide layout. I have not changed it.
- The disk is at 99% (1.2 GB free). A full suite plus revert runs in a fresh worktree may not fit.

## D58

`git grep -i` for the three phrases in `prompts/preserved/2026-09-28-03.md`, run over this report and this commit's
message before the push, returned 0 hits.

## Resumed (continuation `2026-09-28-35`)

Written by the same coder, in the same worktree (`forager-wt/tracks-frame`, local branch `tracks-frame`), pushing to
`journal-redesign`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Line
numbers are at `99eba9d`.

**The continuation** (`prompts/preserved/2026-09-28-35.md`, committed at `e535ef2`), verbatim:

> This is planner message 2026-09-28-35, part of dispatch 2026-09-28-34. Quote it verbatim in your report. It answers the stop you pushed at `3d3d3c3`. Build T1, T2 and T3 with the changes below.
>
> - **Q1: option (c), by the planner.** Your stop is right. The owner's standing plate-pulse ruling, "a green circle with nothing in it is not a day" (`docs/audits/2026-09-07-cartography-plate-renderer-pulse.md:567-571`), means a regions-only entry has no map (`CartographyEntryReportScreen.kt:371`). The dispatch's regions clause was the planner's error. The gate stays. The frame covers kept tracks, finds, located photos and waypoints only, and regions never count towards it. The "regions only" test asserts that there is nothing to frame, and the "regions plus a find" test asserts that the frame is the find alone. This keeps the owner's "Fit all kept records" and the standing ruling together.
> - **T2's mechanism:** your proposal is accepted:
>   - a pure helper in `domain/EntryMapFrame.kt`;
>   - a one-shot camera request with an id on `MapRenderMode`, applied through `getCameraForLatLngBounds` and capped at z17;
>   - "a single point" meaning that all points are in the same place.
>
>   It replaces the old `boundingRegion`/`zoomForRadiusKm` path for the entry map only.
> - **T3, wide layout: the owner's ruling,** verbatim "Make the tap open search (Recommended)". On the wide layout, tapping the search summary opens the search panel even before any search has run, so "Search a location" is true on both layouts. Scope widens to `AvailabilityScreen.kt` at the wide summary's tap (`:1426`) and to `AvailabilityViewModel.kt` where needed. Include a coordinate-touch test on the wide summary. Also fix the stale `ActiveSearchSummary` KDoc (`AvailabilitySearchUi.kt:163-169`).
> - **T1:** as written. Pinned test `SightingsMapOverlayDataTest:235-259` is updated and reported.
> - **Disk:** the owner freed space, and there is now about 13 GB free. Keep checking `df` before each run.
>
> Everything else in the dispatch stands.

The coordinator also relayed that a network outage (EAI_AGAIN) had stopped me mid-work, and the owner's "Try again".
The uncommitted stubs were intact, and I pushed them straight away as `104b17a`, before the tests existed.

### Base

- I pulled with `git pull --no-rebase` to `e535ef2`. Later pushes merged the planner's commits three times
  (`39f258b`, `403a86f`, `da5ad1b`).
- One of those merges brought in another stage's app change: the night offline-region fill, `ui/theme/MapPalette.kt`
  and `MapPaletteTest.kt` (`b3e2f1c`, intent `2026-09-28-19`). It does not overlap any file I touched. That stage's own
  suite at `b3e2f1c` was 274 / 2245 / 0 / 0 / 24, the same as the dispatch's baseline at `ace13cf`. `git diff ace13cf
  e535ef2 -- app` is empty, so the app tree I started from is the baseline's.
- Disk had 13 GB free before every Gradle run (`df -h /`, printed by the run script).

### Commits (pushed to `journal-redesign`)

| Commit | What |
|---|---|
| `104b17a` | WIP stubs for T1 and T2, pushed after the outage and not compiled on their own (they compiled with `f9e06b0`) |
| `f9e06b0` | Tests first: 6 new classes and 1 new test in an existing class, 20 failing, plus the `WIDE_SEARCH_SUMMARY_TAG` stub |
| `99eba9d` | The build: T1, T2 and T3, and the pinned test update |
| this commit | This section |

### Tests first (`f9e06b0`, run `tf1`)

8 classes, 64 tests, **20 failures**, 0 errors, 0 `e: ` lines. Every new test failed. Every other test in those
classes passed. Each failure message, as read from the JUnit XML:

- `TrackWidthByZoomTest`, 6 of 6:
  - the stops: `expected:<[ZoomWidthStop(zoom=11.0, fractionOfFullWidth=0.4), ZoomWidthStop(zoom=15.0,
    fractionOfFullWidth=1.0)]> but was:<[]>`;
  - `breadcrumb has zoom stops`;
  - `breadcrumb at zoom 5 expected:<2.4> but was:<6.0>`;
  - `the widths do change with zoom. Actual: 6.0`;
  - `expected:<["interpolate", ["linear"], ["zoom"], 11.0, 2.4, 15.0, 6.0]> but was:<6.0>`;
  - `the kept track thins out`.
- `EntryMapFrameTest`, 6 of 6: each is `expected:<Fit(…)>` or `expected:<SinglePoint(…)> but was:<null>`.
- `MapCameraRequestTest`, 3 of 3: `never applied`; a bare `AssertionError` (the first `assertTrue`); `expected:<17.0>
  but was:<19.3>`.
- `CartographyEntryReportScreenMapTest`, the new test: `expected:<Fit(bounds=GeoBoundingBox(north=45.25, south=45.2,
  east=-122.47, west=-122.52), paddingDp=48, maxZoom=17.0)> but was:<null>`.
- `CartographyEntryMapOpeningFrameTest`: `one request on open expected:<1> but was:<0>`.
- `CompactSearchBarCopyTest` and `WideSearchSummaryTest`'s copy test: `… contains 'September · Search a location'
  (ignoreCase: false) is not displayed!`.
- `WideSearchSummaryTest`'s touch test: `the Settings panel has closed after a touch at 368.0.dp expected:<0> but
  was:<1>`. The first point is 8 dp inside the summary's left edge, which sits past the 360 dp permanent drawer.

**Absence tests carry positive halves**, as M1 did. "Regions only", "nothing kept", "the outline stays constant", "the
same id is not reapplied" and "GPS tracking applies nothing" each hold at a stub that does nothing. So each one first
asserts a case that the stub fails: the same regions with a waypoint, one located photo, the kept track thinning, a
new id, and the same request with no tracking.

### What landed (`99eba9d`)

**T1, track widths by zoom.**
- **The stops**, defined once as data beside the layer specs: `TRACK_WIDTH_ZOOM_STOPS` (`ui/map/layers/TrackWidthByZoom.kt:23`),
  `ZoomWidthStop(11f, 0.4f)` and `ZoomWidthStop(15f, 1f)`.
  - At zoom 15 and above: breadcrumb and kept tracks 6 dp, their casings 9 dp.
  - At zoom 11 and below: 2.4 dp and 3.6 dp.
  - Linear in between; at zoom 13, for example, 4.2 dp and 6.3 dp.
  - A tweak is one edit to that list.
- **The spec.** `LineLayerSpec` gains `widthByZoom` (`ui/map/SightingsMap.kt`, `null` by default). Both tracks set it
  (`:1411`, `:1420`). `casingFor` copies its track, so each casing carries the same stops and keeps the 9 : 6 ratio at
  every zoom. That is 0.6 dp a side at zoom 11, not `CASING_WIDTH_DP`, as the dispatch's wording gives.
- **The layer.** `lineLayerFor` draws `lineWidthExpression(spec)` (`:1452`), which is `interpolate(linear, zoom, stop(11,
  …), stop(15, …))` (`:1377`), or the constant when a spec has no stops. The offline outline is unchanged at 1.5 dp.
- `lineWidthStops` (`:1354`) is the pure stop list.
- `lineWidthAtZoom` (`:1362`) is a headless model of MapLibre's linear `interpolate`, used by the tests to read widths at
  any zoom. It has no production caller. What it models (clamping outside the stops) is my reading of MapLibre's
  documented behaviour; I have not run it against the renderer.
- **Planned trips are points** (`plannedTripsFeatureCollection`), so four line layers change. **Prediction 2 held.**
- **The pinned test, by name:** `SightingsMapOverlayDataTest` "each track has a solid casing line directly below it,
  wider by the casing on each side, in the casing colour" became "… wider by the casing on each side at full width,
  thinning with it, in the casing colour".
  - Its 6 dp and 9 dp now read "at full width".
  - It gained two assertions: the track carries `TRACK_WIDTH_ZOOM_STOPS`, and its casing carries the same stops.
  - The offline outline's test is unchanged.

**T2, the entry map's opening frame.**
- **`entryMapFrame`** (`domain/EntryMapFrame.kt:48`) works on `CartographyEntryMapData.drawablePoints` only: kept
  tracks' points, finds, located photos and waypoints. **Regions never count** (Q1 (c)). It returns:
  - `null` when there is nothing, where the screen still shows no map, as before;
  - `SinglePoint(at, zoom = 16)` when every point is the same place;
  - otherwise `Fit(bounds, paddingDp = 48, maxZoom = 17)`, the bounds being the plain min/max box.
- **`MapRenderMode.cameraRequest`** (`ui/map/MapSlot.kt:210`) carries a `MapCameraRequest(id, frame)`. `SightingsMapSlot`
  passes it on. This is **no tenth `MapSlot` parameter**, so that abort condition was not met.
- **`SightingsMap`** keeps `lastAppliedCameraRequestId` with its `MapView` (`:350`). In the camera effect (`:645`), a
  request whose id is new (`shouldApplyCameraRequest`, `:1065`) is applied by `applyCameraFrame` (`:1082`):
  - a `SinglePoint` goes to its point at 16;
  - a `Fit` goes through `getCameraForLatLngBounds(bounds, padding px ×4)`, with the zoom capped by `cappedFrameZoom`
    (`:1072`).
  - The target is then marked applied, so the region move does not follow and undo it.
  - If MapLibre returns no camera (the method is `@Nullable`) or rejects the bounds, it logs a `Log.w` and falls back
    to the old region move.
- **The screen** (`ui/log/CartographyEntryReportScreen.kt`) builds one request per screen instance (`:392`). Its id is
  a token remembered per entry (`:290`). The screen passes it at `:429`.
  - The fullscreen switch, a bubble and an M1 find overlay all keep the same screen, so the same request, which is
    applied once.
  - The region is still passed, because `MapSlot` needs one. It no longer sets the opening camera, but a locate-me pan
    still zooms by its radius, as before.
- **Fullscreen and the preview** share one request. It is applied at whatever size the `MapView` has when the style
  first loads, which on opening is the 4:3 preview. Entering fullscreen does not re-fit.
- **Landscape:** whether a rotation recreates this screen, which would mean a new token and a re-fit, is unverified.
  It is a device item.

**T3, the search copy and the wide tap.**
- **The copy.** The no-region fallback is `"Search a location"` (`ui/availability/AvailabilitySearchUi.kt:508`; the
  line moved from `:497` because of the added comment). The month prefix and the `" · "` separator are unchanged.
- **The wide tap.** `ActiveSearchSummary`'s tap (`AvailabilityScreen.kt`, the `mainScaffold` call, around `:1433`) sets
  `drawerPanel = DrawerPanel.Search` and then calls `onReopenTaxonSuggestions()`, as before. The parameter is renamed
  `onClick`, and the summary carries `WIDE_SEARCH_SUMMARY_TAG` (`:144`).
- **`AvailabilityViewModel.kt` is not changed.** Nothing there was needed.
- **KDocs:**
  - `ActiveSearchSummary`'s now describes the new tap;
  - `SearchEntryBar`'s (`:163-169`) now says the summary was replaced on compact only and remains on medium/expanded.
- **Other "no location" strings** (flags only, unchanged): `AvailabilityOfflineMapsUi.kt:198`, and
  `res/values/strings.xml:18` `log_entry_no_location`.

**Tests, by name** (all new except the three noted):
- `ui/map/TrackWidthByZoomTest` (6): the stops; each line and casing at 11 and 15; hold outside and linear inside;
  the 1.5 ratio at every half zoom from 8 to 18; the expression; the outline constant.
- `domain/EntryMapFrameTest` (6), the dispatch's six cases: tracks only; mixed kinds; single point (one, and three
  records at one place); regions only (nothing); regions plus a find (the find alone, and two finds fitted); nothing
  (and an empty track).
- `ui/map/MapCameraRequestTest` (3): applied once per id; no request or GPS tracking applies nothing; zoom cap.
- `CartographyEntryReportScreenMapTest` "the entry map asks to open framed on its kept track and find, never its kept
  region": through the real screen with a capturing slot.
- `CartographyEntryMapOpeningFrameTest` (in `AvailabilityScreenMapBubblesTest.kt`), through the compact Journal tab
  with a day entry open and a stub slot.
  - The slot receives one request, framed on the kept find and waypoint.
  - The test then enters fullscreen by a real touch, opens the find's bubble and "Open find", and presses Back.
  - After that, the slot has been recomposed (the count grew) and holds the same request, with no new one.
- `CompactSearchBarCopyTest` (1) and `WideSearchSummaryTest` (2), through the real `AvailabilityScreen`.
  - Both pin "September · Search a location" before any search.
  - The wide test makes a real coordinate touch at three points across the summary's bounds (8 dp in from the left,
    the centre, 8 dp in from the right). Each touch starts from the Settings panel and must bring back the search
    panel ("Advanced search" shown, "Back to search options" gone). All three must reach the summary (its callback
    count is 3).
- **Confirmed: no existing test pinned "no location set"** (`git grep -n "no location set" -- app/src/test`, nothing).

### Revert checks

- **The runner** is `app/build/tf/revert.py`, with specs from `make_checks.py` in `revert/checks.json`. For each
  check it:
  1. checks the file equals HEAD's blob;
  2. saves a copy;
  3. applies one edit, which must match exactly once;
  4. runs the 8 affected classes through `run.sh`, which clears the results and refuses to read them on any `e: `
     line;
  5. requires the exact predicted failing set, each failure's message containing its fragment;
  6. restores from the saved copy, never from git;
  7. checks the file's sha256 against HEAD's blob again.
- The forward change was committed (`99eba9d`) before any check ran, so the "restore" cannot discard it.
- **All 16 compiled (0 `e: ` lines in every log), every file was restored to HEAD's blob, and `git status` was clean
  after each run.**
- Output: `revert/run1.out`, `report-all.json`, and `revert-R*.log`.

| Check | Edit | Failed | Message, as read | Verdict |
|---|---|---|---|---|
| R01 | zoom-11 stop at 100% | 5 of 64 | `breadcrumb width at zoom 11 expected:<2.4> but was:<6.0>`; the expression `but was:<[… 11.0, 6.0, 15.0, 6.0]>` | Confirmed |
| R02 | casings without stops | 4 of 64 | `breadcrumb casing has zoom stops`; `… at zoom 8.0 expected:<1.5> but was:<3.7499998>`; the pinned test's `casing thins out with its track … but was:<null>` | Confirmed on re-run (below) |
| R03 | the interpolation's input not the zoom | 1 of 64 | `but was:<["interpolate", ["linear"], 0.0, 11.0, 2.4, 15.0, 6.0]>` | Confirmed |
| R04 | evaluator flat between stops | 1 of 64 | `breadcrumb at zoom 13, halfway expected:<4.2> but was:<2.4>` | Confirmed |
| R05 | regions counted (`allPoints`) | 3 of 64 | `expected null, but was:<SinglePoint(at=LatLng(lat=47.0, lng=-120.0)…`; two `but was:<Fit(…north=47.0…` | Confirmed |
| R06 | one place fitted, not centred | 4 of 64 | zero-area `Fit`s where `SinglePoint`s were expected | Confirmed |
| R07 | padding 24 | 3 of 64 | `paddingDp=24` in the pure, screen and UI tests | Confirmed |
| R08 | zoom cap 20 | 3 of 64 | `maxZoom=20.0`, the same three | Confirmed |
| R09 | single point at 15 | 1 of 64 | `but was:<SinglePoint(…zoom=15.0)>` | Confirmed |
| R10 | id ignored | 1 of 64 | `already applied` | Confirmed |
| R11 | applied under GPS tracking | 1 of 64 | bare `AssertionError` on the tracking assert | Confirmed |
| R12 | zoom uncapped | 1 of 64 | `expected:<17.0> but was:<19.3>` | Confirmed |
| R13 | token new on every recomposition | 1 of 64 | `no new request after the round trip expected:<[MapCameraRequest(id=entry-map-entry-1-b6e6…` | Confirmed |
| R14 | the screen sends no request | 2 of 64 | the screen test `but was:<null>`; `one request on open expected:<1> but was:<0>` | Confirmed |
| R15 | the old copy | 2 of 64 | `'September · Search a location' … is not displayed` ×2 | Confirmed |
| R16 | the wide tap does not switch the panel | 1 of 64 | `the Settings panel has closed after a touch at 368.0.dp expected:<0> but was:<1>` | Confirmed |

- **R02, stated plainly.** The first run failed exactly the predicted 4 tests, for the edit's reason, but my fragment
  said `but was:<3.75>`, and 9 / 2.4 in float arithmetic prints `3.7499998`. I corrected the fragment after reading
  the result and re-ran R02 alone (`revert/run2-R02.out`, `report-R02.json`): compiled, 4 failures, all fragments
  matching, restored, clean.
- **Each failure is one its own edit could cause.** Two I read closely:
  - R13's request id changed between open and the round trip, while the one-request-on-open assertion still passed.
    So the test catches a token lost on recomposition, not just a missing one.
  - R03's message shows the interpolation's input replaced and the stops intact.
- **Not covered by any revert check:**
  - `lineLayerFor` drawing the expression, and `applyCameraFrame`: both native, so device-only;
  - the region move's suppression after a frame, which is inside `SightingsMap`'s effect and cannot run under
    Robolectric;
  - `onReopenTaxonSuggestions` still firing on the wide tap. It is counted in the wide test, but it is also the
    original behaviour, so no edit was needed to show that the count bites.

### Suite

- **Full suite at `da5ad1b`** (the pushed head: `99eba9d` merged with the planner's docs), run with `LC_ALL=C.UTF-8`
  from a cleared results directory, counts from the JUnit XML, 0 `e: ` lines: **280 classes / 2265 tests / 0 failures
  / 0 errors / 24 skipped.** Class times sum to 131 s. The held family (`JournalPendingDeleteTest`'s album tests,
  `JournalTabTest`'s photo pull) passed.
- **Against the baseline** (the planner's 274 / 2245 / 0 / 0 / 24 at `ace13cf`, whose app tree is this base's, and the
  night-region stage's same count at `b3e2f1c`): +6 classes and +20 tests. That is exactly this build's additions:
  - `TrackWidthByZoomTest` 6, `EntryMapFrameTest` 6, `MapCameraRequestTest` 3, `CartographyEntryMapOpeningFrameTest`
    1, `CompactSearchBarCopyTest` 1 and `WideSearchSummaryTest` 2, which are six new classes and 19 tests;
  - one new test in `CartographyEntryReportScreenMapTest`.
  I did not run a baseline of my own at base.
- **CI on `da5ad1b`** (run 36425442901): 2265 tests, **2 failed**, 24 skipped. Both failures are the held family:
  - `JournalPendingDeleteTest` "a long-press anywhere on an album photo opens a menu of exactly Delete"
    (`AssertionError at JournalPendingDeleteTest.kt:1263`);
  - "an album photo has no corner delete control, and a long-press at that corner opens the Delete menu" (`:1349`).

  Re-run locally, class alone, at `ceeb829`: 1 class / 52 tests / 0 failures / 0 errors / 0 skipped. I did not
  investigate or change either, as the dispatch says. CI on `ceeb829` was still running when I wrote this. Earlier
  runs on this branch, including the WIP commit's merge `39f258b`, were cancelled by later pushes, so `104b17a` has no
  CI result of its own.
- **Predictions:**
  - 1 (a fixed or last-known camera) **missed**: the camera was the bounding region's centre at a zoom from its
    radius (verification above);
  - 2 (four line layers, casing ratio kept) **held**;
  - 3 (+15 to +35 tests) **held** at +20.

### Device-only

- **The widths' look across zooms,** on Topographical, Street and at night. This includes the breadcrumb's dots and
  gaps shrinking with its width, and whether 40% at zoom 11 reads well.
- **An entry opening framed on the S22,** in the preview, in fullscreen and in landscape.
  - Whether `getCameraForLatLngBounds` gives the right fit at the preview's measured size when the style first loads.
  - Whether rotation recreates the screen and so re-fits.
  - That the view stands after panning, then opening and closing a find.
- **The new copy on both layouts,** and on the wide layout the tap bringing the drawer back to its search panel.

### Decisions I made

- **The WIP push.** After the outage I pushed uncompiled stubs (`104b17a`) to the shared branch, so CI may have run on a
  commit I had not compiled. I have not checked CI.
- **The absence-test halves,** folded as M1 did (above), rather than stubs that throw.
- **Names:**
  - `EntryMapFrame.SinglePoint` (the mechanism said "Point");
  - `ZoomWidthStop`, `TRACK_WIDTH_ZOOM_STOPS`, `TrackWidthByZoom.kt`;
  - `MapCameraRequest(id: String, …)`;
  - `WIDE_SEARCH_SUMMARY_TAG`;
  - `ActiveSearchSummary`'s parameter renamed `onClick`.
- **The request id is a per-screen-instance token** (`entry-map-<entry id>-<UUID>`), not the entry's id. This lets the
  UI test tell a lost screen from a kept one (R13). The accepted mechanism said "an id", not which.
- **Stops live on the spec** (`LineLayerSpec.widthByZoom`), and the casing inherits them through `copy`, rather than a
  separate casing ratio.
- **`lineWidthAtZoom`,** a headless model of MapLibre's interpolation, exists for the tests only.
- **The frame replaces the region move on open.** The region move is suppressed for that target, not run first and
  then overridden.
- **The fallback when MapLibre gives no camera** is a logged region move.
- **The wide tap keeps `onReopenTaxonSuggestions()`** after switching the panel. The ruling says the tap opens search;
  I kept the old species-reopen as well rather than drop it.
- **No `AvailabilityViewModel.kt` change** was needed.
- **The antimeridian** is not handled: min/max bounds, as `boundingRegion` already does.
- **The wide test's "search panel open" marker** is the "Advanced search" header with the Settings back arrow gone.
- **The report shape:** a status line on top and this "Resumed" section, with the stop sections left as written, as M1
  did.

### Flags outside scope

- **Stale KDocs in `domain/GetCartographyEntryMapDataUseCase.kt` and `domain/GeoDistance.kt`.**
  `CartographyEntryMapData.allPoints` still says it is "what `GeoDistance.boundingRegion` fits the camera to", and
  `boundingRegion`'s KDoc calls itself the entry map's camera framing. Both are only partly true now: they give the
  region, but no longer the opening camera. Neither file is in scope beyond a pure helper, so I have not changed them.
- **On the wide layout the drawer's location controls sit inside the collapsed "Advanced search" section**
  (`CollapsibleSection`, collapsed by default). The tap brings the search panel back, but searching a location takes
  one more tap to expand it. Nothing was built for this.
- **When the wide drawer already shows its search panel,** the tap has nothing further to open. The panel is already
  on screen.
- `ActiveSearchSummary`'s and `SearchEntryBar`'s KDocs are fixed. The KDoc of `SearchDropdown`'s neighbour at
  `AvailabilitySearchUi.kt` ("see [ActiveSearchSummary]'s own doc comment on why that quick panel is gone") still reads
  as if the summary were gone; I left it.

### D58

Before every push I ran `git grep -i`-equivalent counts for the three phrases over the staged diff and over each
commit message, `104b17a`, `f9e06b0`, `99eba9d` and this commit. Every count was 0.

## Continuation `2026-09-28-38`: the wide summary opens "Advanced search" expanded

**The message** (`prompts/preserved/2026-09-28-38.md`, committed at `163cae1`), verbatim:

> Planner message 2026-09-28-38, part of dispatch 2026-09-28-34. Quote it verbatim in your report.
>
> The owner ruled on your flag. On the wide layout, the location controls sit inside the collapsed "Advanced search" section, so searching a location takes one more tap after the summary tap. The planner asked: "Should the wide tap open straight to the location controls?" The owner answered, verbatim: "Yes it should. Good application of my principle. Proceed with that change".
>
> **Build.**
> - On the wide layout, tapping the search summary opens the search panel with "Advanced search" **expanded**, so the location controls can be seen and used without another tap.
> - Opening the panel any other way stays as it is today.
> - If the expanded state is stored anywhere, the tap sets it; it must not overwrite a choice the user made while they are inside the panel (CLAUDE.md UX defaults).
> - If "the location controls" and "Advanced search" are not the same thing in code, stop and report.
>
> **Tests.**
> - Tests first: extend your wide coordinate-touch test so it asserts that a location control is displayed after the tap. It must fail at your current head for that reason.
> - One revert check under the runner rules.
> - The full suite, from a cleared results directory.
> - Update the completion report with a short section.
>
> **Scope:** the wide search panel's expand state, reached from `AvailabilityScreen.kt` and `AvailabilitySearchUi.kt`, plus tests. Nothing else.
>
> Push to `journal-redesign` as before. Note that the planner is running a suite in its own worktree at the same time: check `df`, and stop if a build is OOM-killed.

**The stop condition does not apply.** In the medium/expanded drawer, the location controls are direct children of
the "Advanced search" section: "Use current location", Latitude and Longitude, "Search this location" and the radius
(`RegionControls` inside `SearchControls`' "Advanced search" `CollapsibleSection`, `AvailabilitySearchUi.kt`). There is
no further collapsible between them and that header. The section also holds the month selector.

**Where the expanded state lives.** It was stored nowhere outside the section: a local `remember` in
`CollapsibleSection`. That state is dropped whenever the drawer leaves its search panel, which is why the Settings back
arrow always opens the panel collapsed.

**What landed** (`16b3baa` tests first, `6cf0e9b` build):
- A one-shot request:
  - `AvailabilityScreen` holds `expandAdvancedSearchRequested`.
  - Only the summary's tap sets it, beside `drawerPanel = DrawerPanel.Search`.
  - The drawer's `SearchControls` passes it to the "Advanced search" section's new `expandRequested` parameter.
  - The section expands once in a `LaunchedEffect` and calls `onExpandRequestConsumed`, which clears the request.
- The expanded state stays the section's own. So a collapse the user makes afterwards stands, and every other way
  into the panel (the Settings back arrow, the app bar's drawer icon, a first open) passes no request and opens it
  collapsed, as before.
- The compact dropdown's call and the other `SearchControls` caller (`AvailabilitySettingsUi.kt:605`) pass nothing,
  so the defaults leave them unchanged.
- `ActiveSearchSummary`'s KDoc now says the tap opens the panel with the section expanded.

**Tests first** (`16b3baa`, run `tf38`, `WideSearchSummaryTest` at `d64b7ca` plus the test file): 3 tests,
**2 failures**, 0 `e: ` lines.
- The extended coordinate-touch test failed because "Use current location" was not displayed after the touch. It now
  also asserts "Latitude".
- A new test failed at the same assertion: "the summary's tap expands Advanced search, the user's own collapse
  stands, and the back arrow opens it collapsed as before".
- The copy test passed.
- This version of the test library words a missing node as "is not displayed!", as it did for the copy in `tf1`.

**The build** (`6cf0e9b`, run `b38`): `WideSearchSummaryTest`, `CompactSearchBarCopyTest`,
`AvailabilityScreenWideWindowLayoutTest` and `CompactToolsDrawerTest` ran 25 tests with 0 failures.

**The revert check, R17.** I removed `expandAdvancedSearchRequested = true` from the tap, using the same runner over
the same 8 classes. It compiled with 0 `e: ` lines. It failed exactly the 2 predicted tests (65 tests in the run), both
with `'Use current location' (ignoreCase: false) is not displayed`. The file was restored from its saved copy to
HEAD's blob, `git status` was clean afterwards, and the forward line was still present. **Confirmed.**
- Not covered by a revert check: the request being consumed, so that the back arrow still opens the panel collapsed.
  The new test asserts it, but I ran only the one check the message asked for.

**Suite** at `6cf0e9b`, from a cleared results directory, `LC_ALL=C.UTF-8`, counts from the JUnit XML, 0 `e: ` lines:
**280 / 2266 / 0 / 0 / 24**. That is one test more than at `da5ad1b` (the new test) and no new class. The planner's
suite was running in parallel. `df` showed 13 GB free before each run, available memory was 3 GB at the lowest
reading, and nothing was OOM-killed.

**Device-only:** the tap opening the drawer's search panel with its location controls showing, on a wide window.

**Decisions I made**
- **The mechanism.** A one-shot request consumed by the section, not the expanded state hoisted to the screen.
  Hoisting it would have kept the section open across a trip to Settings and back, and that would have changed another
  way into the panel. The message asks for that to stay as it is today.
- **The summary tap re-expands the section every time**, even if the user collapsed it earlier. I read the tap as a
  new request to search, not as overwriting a choice made inside the panel.
- **Only the one revert check was run**, as asked.
- **The test's markers** are "Use current location" and "Latitude".

**Flags outside scope**
- Today, "Advanced search" and the other drawer sections forget a user's expand whenever the drawer leaves its search
  panel (the section's local `remember` inside the panel's `when` branch). CLAUDE.md's UX default ("what the user has
  set survives navigating away and back") would call that a bug. I left it as it is, because the message keeps every
  other way into the panel unchanged.
