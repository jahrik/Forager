# 2026-09-28: track widths by zoom (T1), the entry map's opening frame (T2), the search-bar copy (T3)

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
