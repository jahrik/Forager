# Map markers, colour build C2: completion report

Date: 2026-09-27 (UTC). Branch `marker-palette`, cut from `origin/pre-main` `545258e` (the #134 merge).
Dispatch: `prompts/preserved/2026-09-27-02.md`. Record: intent `2026-09-27-27`. The planner's rulings on
the build's two stops are quoted verbatim in that intent (planner log line 2488). Merge into `pre-main` is
authorised without a device check. The owner deferred device checks to beta, so **nothing here was run on a
device.**

## What was built

| Item | Change | Where |
|---|---|---|
| (a) | `MapPalette` has one role per marker, day and night: `waypoint`, `find`, `plannedTrip`, `photo`, `keptTrack`, `breadcrumb`, `centrePin`, `searchCentre`, `offlineRegion`, `sightingDot`, `sightingDotStroke` (the sighting ring), `sightingDotStrokeSelected` (the selected ring) and `casing`. The find pin no longer shares the offline region's colour, and the photo no longer shares the planned trip's. | `MapPalette.kt`, `SightingsMap.kt` |
| (b) | The palette is `MapPalette.forMode(nightMode)` on every basemap, Satellite included. It is chosen inside `requestedMapStyle`, which a test reaches; the style effect builds the overlays from `requested.palette`. `AppliedMapStyle.night` stays effective night for the basemap paint. | `BasemapStyles.kt`, `SightingsMap.kt` |
| (c) | Each track has a solid casing line directly below it, 1.5dp wider on each side (9dp); the breadcrumb stays dashed. The sighting ring is 1.5dp, and the selected ring is 3dp. The fill stays at 0.7 and the ring at 0.85. The offline region is filled at 0.2 in its role colour, with a dashed casing outline: 1.5dp wide, 6dp dashes, 4dp gaps, butt ends. | `SightingsMap.kt` (`LineLayerSpec`, `trackLayerSpecs`, `offlineRegionOutlineSpec`, `sightingStrokeWidthExpression`) |
| (d) | Each bitmap marker has its own silhouette, cased 1.5dp, drawn into a bitmap with its anchor at the exact centre. The search centre becomes a reticle `SymbolLayer`. | `MarkerGlyphs.kt` (new), `SightingsMap.kt` |
| (e) | The centre pin is `LocationOn` in `MapPalette.forMode(night).centrePin`, over a casing built from `LocationOn`'s own paths. `CentrePin` and `CentrePinLocationPickerOverlay` take `night`, and the three main-map call sites pass `renderMode.night`. | `CentrePinLocationPicker.kt`, `AvailabilityCompactMapUi.kt` `:1148` and `:1166`, `AvailabilityWideLayoutUi.kt` `:322` |
| (f) | Stale marker, palette and halo claims are corrected, and a dated addendum is appended to `map-redesign.md`. | see "Stale comments" below |

### Palette

The dispatch's table is used as given, with one exception. The day sighting-dot fill is `#2B2B2B`, not `#1F1F1F`, by the planner's ruling. The planner's first grey sat 0.090 from the day offline region `#0B0B0B`, under (d)'s 0.10. The glyph board recorded that pair; the swatch board did not. `#2B2B2B` is the planner's pick under the owner's "a near black color". I checked it before writing any code: (d) 0.1395 against the offline region and 0.1404 against the waypoint, the two nearest.

**Roles retired**, with nothing reading them:
- `connector`, whose use is now `keptTrack`;
- `areaMarkerBackground`, now split into `find` and `offlineRegion`;
- `areaMarkerForeground`;
- `NIGHT_WARM` and `NIGHT_INK`;
- `DAY_TILE_REFERENCE` and `NIGHT_TILE_REFERENCE`.

### Silhouettes and anchors

All dimensions are in dp, from the top-left of the fill extent, as on the glyph board (§1), whose §2 shape choices are kept.

| Glyph | Fill extent | Anchor | Parts |
|---|---|---|---|
| Waypoint | 22 × 28 | tip (11, 28) | head circle r 11 at (11, 11); triangle (0, 11) (11, 28) (22, 11); ring in the casing colour, outer r 3.5, inner r 2 |
| Find | 24 × 26 | stem foot (12, 26) | dome: upper half of an ellipse rx 12, ry 14, centred at (12, 14); stem x 8–16, y 13.5–26 |
| Planned trip | 20 × 28 | pole foot (1.5, 28) | pole x 0–3, y 0–28; pennant x 3–20, y 0–12 |
| Photo | 22 × 22 | centre (11, 11) | rounded square r 5; camera in the casing colour: body x 4–18, y 7–17, r 1.5; bump x 8–14, y 5–8; lens a fill-colour disc r 3.2 at (11, 12) |
| Search centre (reticle) | 26 × 26 | centre (13, 13) | ring r 8 at the stroke centre, stroke 2 (7–9); arms 2dp wide, 13dp from the centre each way |

**Reticle dimensions (my choice, as the dispatch asked).**
- Each arm runs 13dp from the centre.
- The ring's outer edge is 9dp, so the arms pass it by 4dp. That is two stroke widths, so they read as crossing the ring rather than touching it.
- The arms also pass the ring's casing (10.5dp) by 2.5dp.
- With its casing the reticle is 29dp across.

**Differences from the glyph board's Python** (also in `MarkerGlyphs.kt`'s doc):
1. **Waypoint.** The board's `pin_poly` joins the head's upper half-circle to the tip. That drops the parts of the lower half-circle outside the triangle. Its §1 text describes today's shape instead (the whole head circle plus the triangle), and that is what is drawn.
2. **Casing.** The board grew each polygon by 1.5dp using a round-joined stroke. Here each part is filled, then stroked 3dp wide with round joins, in the casing colour. The footprint is the same.
3. **Curves.** Arcs and circles are true `Path` arcs, not 120–180-point polygons.
4. **Search centre.** The board's plus sat 3dp inside the ring; the owner's tweak puts the arms beyond it.
5. **Casing drawn in two passes.** A single `FILL_AND_STROKE` pass lost the reticle ring's even-odd hole. The first run of `MarkerGlyphsTest` caught that: the hollow came out in the casing colour. Drawing the casing as a fill pass and a stroke pass fixed it on the first attempt.

**Anchor check.**
- Every bitmap is padded symmetrically round its anchor by the glyph's farthest extent from it, plus the 1.5dp casing, plus 1dp. Both dimensions are even.
- The anchor is therefore the exact centre, and every `SymbolLayer` uses `icon-anchor: center` with no `icon-offset`.
- The canvas is translated by (half-width − anchorX × density, half-height − anchorY × density), so the feature point lands on the centre pixel exactly.

Computed at the test density (3.0) and the S22's (2.8125):

| Glyph | 3.0: bitmap, anchor px | 2.8125: bitmap, anchor px |
|---|---|---|
| Waypoint | 82 × 184, (41, 92) | 76 × 172, (38, 86) |
| Find | 88 × 172, (44, 86) | 82 × 162, (41, 81) |
| Planned trip | 126 × 184, (63, 92) | 120 × 172, (60, 86) |
| Photo | 82 × 82, (41, 41) | 76 × 76, (38, 38) |
| Search centre | 94 × 94, (47, 47) | 88 × 88, (44, 44) |

`MarkerGlyphsTest` checks this at density 3:
- the anchor is `(width / 2, height / 2)` for every glyph, and both dimensions are even;
- the fill colour is drawn just inside the feature at the anchor: 1.5dp above the pin tip, 1dp above the stem foot and the pole foot, and the centre for the photo and the reticle;
- the casing colour is drawn half a casing below each bottom anchor.

That MapLibre then places the image's centre on the coordinate is the SDK's documented `center` behaviour, not something a headless test reaches.

### Centre pin

- The pin keeps `Icons.Filled.LocationOn` at 40dp, with its anchor unchanged: the box padded at the bottom by half its size.
- Behind it sits a second icon, `centrePinCasingVector`. It holds `LocationOn`'s own `VectorPath`s, copied rather than approximated, each filled and stroked in the casing colour. The stroke is 1.8 viewport units, which is 1.5dp on each side at 40dp over 24 units, with round joins.
- The pin is no longer tinted with the theme's `primary`.
- Where each overlay's night comes from: `CompactMapTab` and `MapTab` each take `renderMode: MapRenderMode` and hand that same value to `mapSlot` (compact `:550`, wide `:257`). So each call site passes `renderMode.night`.

## Tests

**Tests-first.** Each commit failed for its stated reason before its implementation:

| Commit | Failure |
|---|---|
| `21b9c46` (a) | Compile: 8 errors, all `Unresolved reference` to the new roles (`find`, `photo`, `keptTrack`, `centrePin`, `offlineRegion`, `casing`) |
| `65ad7b4` (b) | Compile: 5 errors, each `No value passed for parameter 'palette'` (`requestedMapStyle` loses the parameter) |
| `4ffae52` (c) | Compile: 23 errors, all from `sightingStrokeWidthExpression`, `trackLayerSpecs` and `offlineRegionOutlineSpec`, plus the inference errors that follow |
| `3dec2cb` (d) | Compile: 185 errors, all in `MarkerGlyphsTest.kt`: unresolved (d) names (`MarkerGlyph`, `MarkerIcon`, `drawGlyph`, `markerIconImage`, `GlyphImage`, `GlyphPaint`, the `SEARCH_CENTRE_*` constants) and the inference errors after them |
| `93664b6` (e) | Compile: 6 errors (`No parameter with name 'night' found` on the overlay; `centrePinCasingVector` unresolved, and the inference after it) |
| `fe8922f` (e) part 1 | The two screen tests failed on assertions, as intended, with the picker built but not threaded: compact `night expected:<#FF[A656A0]> but was:<#FF[7D0D5B]>`; wide `expected:<#FF[A656A0]> but was:<#FF[7D0D5B]>` |

Each implementation then passed its classes with 0 failures:
- (a): 50 tests;
- (b): 56;
- (c): 54;
- (d): 39, after the one fix above;
- (e): 120, with 19 skipped, which are the existing `@Ignore`s in those classes.

**Expected values changed on purpose, each named in its commit:**
- `OfflineStyleSwapTest`:
  - C1's "turning Night Maps on or off over Satellite is not a reload" became "… over Satellite reloads for the markers, and the basemap stays day". It now goes through `requestedMapStyle`.
  - The `applied()` helper's palette now follows the toggle; it was fixed at `DAY`. So "once loaded, the requested style carries effective night" changed its expected value through the helper. That makes **two** existing tests, not the one the planner predicted.
- `SightingsMapOverlayDataTest` "sightingStrokeColorExpression carries the caller's own palette, not a hardcoded one" (`:186-204`, named as the ruling asked). It used `MapPalette.NIGHT`, but both palettes now carry the same two rings, so `NIGHT` could no longer catch an ignored palette argument. It now uses a palette with deliberately different rings. The role names were kept (`sightingDotStroke`, `sightingDotStrokeSelected`), so nothing there changed because of a rename.
- `MapPaletteTest` was rewritten (below).

**`MapPaletteTest`, rewritten.**
- It measures against the swatch board's §3 ground clusters of 0.5% share or more, Topo and Street: 26 day clusters, and 26 V1-night clusters clustered independently.
- It asserts (a), (c), (d) and (e) in both modes. Each figure is pinned as a ratchet at its measured value, floored to 3 decimals, with the threshold and margin in a comment beside it.
- A threshold a pinned value fails is not asserted, per the ruling.
- (d) covers the 11 fill roles; the sighting ring is a casing and is left out, per the ruling.
- (b) is not asserted: the board showed no casing polarity passes it (§4).
- The pinned figures are in intent `2026-09-27-27`'s Baseline.

The figures below threshold, all recorded:
- night search centre: (a) 0.137 and (d) 0.099;
- night offline region: (d) 0.099;
- night sighting dot: (a) 0.097;
- night selected ring: (a) 0.143, and (c) 1.076 against the grey dot.

A guard test reproduces board figures (waypoint (a) 0.3595, (c) 15.65, hue 300.3°, (e) 4.7°), so a broken calculation cannot pass silently.

**The two removed night contrast tests do not return.** A measured dark reference exists now, but it does not support them:
- against the night ground's P50 luminance, the night fills measure 1.24:1 (centre pin) to 5.85:1 (offline region), not 4.0:1;
- night's weakest, 1.24, is below day's weakest, 1.84 (the selected ring).

Script: `~/Zynergy/forager-c2-work/oldtests.py`.

**New tests (22):**

| Class | Tests | What they check |
|---|---|---|
| `MapPaletteTest` | 10, replacing 7 | owner colours; (a), (c) and (d) in each mode; (e); `forMode`; the calculation guard |
| `OfflineStyleSwapTest` | +1 | the palette follows Night Maps on every basemap, online and offline (the Satellite test is changed, not added) |
| `SightingsMapOverlayDataTest` | +3 | selected ring 3dp; track casings; offline outline |
| `MarkerGlyphsTest` (new, `GraphicsMode.NATIVE`) | 10 | extents and anchors; anchor at the centre; fill at the anchor; casing below bottom anchors; an all-transparent border, so the padding holds the casing; reticle arms past the ring; the waypoint ring; the camera; the find icon is a mushroom; every icon in its own role's colour, day and night |
| `CentrePinLocationPickerTest` | +3 | overlay pin colour and casing, day and night (pixels); the picker's own pin follows `night`; the casing is `LocationOn`'s own outline |
| `AvailabilityScreenMapIconStackTest` | +1 | through the real screen and ViewModel, compact: the add-button picker's pin is day, then night after `onNightModeMapsChanged(true)` |
| `AvailabilityScreenWideWindowLayoutTest` | +1 | through the real screen, wide: with night in the state, the pin is the night colour |

**What only the eye can check** (Robolectric cannot):
- how MapLibre scales and resamples the icon images;
- whether the silhouettes read as distinct at a glance;
- whether a tap lands on the drawn anchor;
- how the 0.7 and 0.85 opacities blend over real ground.

`AvailabilityCompactMapUi.kt:1166` (the "Set on map" picker) has no test of its own; it is the same one-line change as `:1148`. `applyOfflineNightRecolour` and the effect's keys stay unreachable headless, as in C1.

**Revert checks.** The runner follows CLAUDE.md's revert-runner rules:
- it saves a copy of the file outside the repo;
- the edit must match exactly once;
- the results directory is deleted before each run;
- the reverted build log is counted for `e:` lines before any result is read: **0 in every revert**, with the test task executed;
- the file is restored from the saved copy, never from git, and its sha256 checked equal;
- the forward text is confirmed present, and `git status` was clean after each.

Runner: `~/Zynergy/forager-c2-revert.py`. Evidence: `~/Zynergy/forager-c2-work/revert-*`.

| Revert | Edit | Failed | Quoted failures |
|---|---|---|---|
| R1, palette follows night | `palette = MapPalette.forMode(nightMode)` → `MapPalette.DAY` in `requestedMapStyle` | 3 of 15 | "… over Satellite reloads for the markers …" (`AssertionError`, the reload assertion); "once loaded, …": `USGS_IMAGERY_ONLY night=true offline=false expected:<AppliedMapStyle(… palette=MapPalette(waypoint=-4620809 …` (the NIGHT palette); "the marker palette follows Night Maps …": `USGS_IMAGERY_ONLY offline=false night=true expected:<MapPalette(waypoint=-4620809 …` |
| R2, track casing | kept track's casing layer removed from `trackLayerSpecs` | 1 of 29 | `two tracks and two casings expected:<4> but was:<3>` |
| R3, selected ring width | `SIGHTING_DOT_SELECTED_STROKE_WIDTH_PX = 3f` → `1.5f` | 1 of 29 | `expected:<["case", ["get", "selected"], 3.0, 1.5]> but was:<["case", ["get", "selected"], 1.5, 1.5]>` |
| R4, reticle arms | `SEARCH_CENTRE_ARM_DP = 13f` → `9f` (the ring's outer edge) | 2 of 10 | `arm half-length 9.0dp is past the ring's outer edge and its casing`; `SEARCH_CENTRE width expected:<26.0> but was:<18.0>` |
| R5, find is a mushroom | `MarkerIcon.FIND`'s glyph → `MarkerGlyph.WAYPOINT` | 2 of 10 | `FIND glyph expected:<FIND> but was:<WAYPOINT>`; `find icon size expected:<88> but was:<82>` |

Every failure in each revert is one that edit alone could cause.

Two caveats:
- In R4 the arms test stopped at its first assertion, on the constant, so its pixel assertions did not run under the revert.
- In R5 the mushroom test stopped at its size assertion, so its dome and stem pixel assertions did not run either.

Those pixel assertions are known to pass on the forward build. Whether they would fail on their own under the revert is not shown.

**Suites.**
- **Before**, at `5d65bf4` (`app/` identical to `545258e`): 220 classes, 1,707 tests, 0 failures, 0 errors,
  24 skipped.
- **After**, at `c72e9df`: 221 classes, 1,729 tests, 0 failures, 0 errors, 24 skipped. The log has no `e:`
  line, and the results directory was deleted before the run.
- **Difference:** +1 class and +22 tests, as tallied above; skipped is unchanged. That falls within the
  planner's predicted +15 to +40.
- **Per class:** the counts differ from the baseline only in the seven classes this build touched
  (`MapPaletteTest` 7→10, `OfflineStyleSwapTest` 14→15, `SightingsMapOverlayDataTest` 26→29,
  `MarkerGlyphsTest` new with 10, `CentrePinLocationPickerTest` 5→8, `AvailabilityScreenMapIconStackTest`
  103→104, `AvailabilityScreenWideWindowLayoutTest` 7→8). Every other class is identical.
- **Evidence:** `~/Zynergy/forager-c2-baseline/` and `~/Zynergy/forager-c2-after/` (gradle.log, JUnit XML,
  per-class counts, HEAD), and `~/Zynergy/forager-c2-work/` (every tests-first, implementation and revert
  run).

## Stale comments (f)

- **`SightingsMap.kt`:**
  - `nightMode`'s doc: markers follow the flag, and the "twilight trigger and long-press override" text, which no code backed, is replaced;
  - the class doc's planned-trip diamond and its osmdroid colour-constants sentence;
  - the colour-constants comment ("derived from the active ColorScheme"), now "hand-authored per role";
  - `initializeOverlayLayers`' doc;
  - the three passages that said a night toggle skips the style swap over Satellite (in the (b) commit).
- **`MapSlot.kt`:** `MapRenderMode.night`'s marker sentences; the search centre, find and photo descriptions.
- **`MapPalette.kt`:** the class doc, rewritten in the (a) commit, since it described the palette that commit replaced. It includes the pulse's lines about day-only markers, five passes, `NIGHT_WARM`/`NIGHT_INK`, halos and `raster-saturation`/`raster-contrast`.
- **`MapPaletteTest`:** its doc, rewritten with the test.
- **`Color.kt` `:97` and `:112`, comments only (ruling):** `MapPalette` does not derive from the scheme, and the planned trip and search centre no longer use these colours.
- **`README.md`:** the `ui/map/` summary and the overlay-legibility paragraph.
- **`docs/plans/map-redesign.md`:** an appended addendum correcting "What shipped instead" (`:472-477`) without editing it. It also records that `MapPalette` is hand-authored and not scheme-derived, which is the reality R2 was superseded by.

## Device items (none run; deferred to beta by owner ruling)

1. Every marker is legible on Topo and Street, day and night, and on Satellite at night.
2. The silhouettes are distinguishable at a glance.
3. Tap targets land on the drawn anchor. No marker other than the sighting dot has a tap handler today, so this is about where the drawn anchor sits over the coordinate.
4. The selected ring is visible at night: blue on grey measures 1.076:1 in luminance.
5. The casings are not clipped: bitmap padding, and the track casings under the lines.
6. The centre-pin picker is legible, day and night.

## Flags

- Before C2, `waypointPinBitmap` filled its head circle (clockwise) and its tail triangle (counter-clockwise) as **one** path under the default non-zero rule. Where the two overlapped, their windings would cancel, which could have left the overlap unfilled on a device. I did not render the old function, so this is unverified. The new glyphs draw each part separately.
- `docs/plans/contrast_assertions.md` still describes the removed night icon path and `NIGHT_WARM` as things to revive. That is historical, and it was not in (f)'s list, so I did not touch it.
- Historical hardware notes in `README.md` (`:1159-1214`: the dashed connector, "bark brown" dots) describe past device checks and were left as they are.
