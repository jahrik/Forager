# Night-inversion spike on the S22 Ultra, and OpenTopoMap/OSM tile colour measurement

**Date:** 2026-09-26 (UTC, run 19:45–20:10; IDs dated 2026-09-27 on the owner's ruling "Use 2026-09-27 IDs now").
**Dispatch:** `prompts/preserved/2026-09-26-38.md` (Type device), intent `2026-09-27-02` in `RECORD.md`.
**Branch:** `night-evidence`, cut from `origin/landscape-b2` `16f7d4f` (itself one sweep commit on `origin/pre-main` `15e7900`).
**What this is:** evidence only. The spike code is reverted on this branch and nothing under `app/` survives
the merge (`git diff origin/pre-main -- app/` is empty). No colour, palette or design decision is made here.

## Summary

- **MapLibre 13.5.0's raster paint properties can invert the online raster basemaps.** With
  `raster-brightness-min: 1` and `raster-brightness-max: 0`, the rendered map is exactly the per-channel
  RGB inverse of the day map: the median per-channel residual against `255 − day` is **0.0** on both
  Topographical and Street. The earlier report's line "`brightness-min`/`max` clamp/scale, they don't
  invert" (`docs/qc/dispatches/reports/2026-08-28-night-inversion-phase1-report.md:124`) was never tested
  on 13.5.0 and does not hold when min > max.
- **Adding `raster-hue-rotate: 180` (V1) gives a hue-preserving lightness inversion.** Hue shift against
  the day map: median **0°**, P90 **1°** over saturated mid-lightness pixels. HLS lightness is mirrored
  (median `L_day + L_night` = **1.00**). Without hue-rotate (V2), every hue shifts by exactly **180°**:
  forest green turns violet or magenta, and water turns brown.
- **Tile sample (Tillamook State Forest, 36 tiles):** both basemaps are overwhelmingly green, light
  ground. OSM is 87% one woodland colour, `#add19e`. OpenTopoMap is a hillshaded yellow-green ramp:
  93% of its chromatic pixels have Oklab hue 90–150°. Contours, roads and water are each about 2.4% or
  less of pixels (the table below gives the figures).

## 1. Spike

### Method

- Device `R5CT321008R` (SM-S908U) only, portrait, `accelerometer_rotation` 0, `user_rotation` 0 throughout.
- `NIGHT_RASTER_PAINT` (`app/src/main/java/com/zynergylabs/forager/app/ui/map/BasemapStyles.kt:71-75`)
  was the only change, in commits titled "SPIKE — not for merge". Today's saturation and contrast were
  left out of both variants.
  - **V1**, `9ba5cac`: `raster-brightness-min` 1, `raster-brightness-max` 0, `raster-hue-rotate` 180.
    Installed as `1.0.932+g9ba5cacb`.
  - **V2**, `48ba2f8`: `raster-brightness-min` 1, `raster-brightness-max` 0, no hue rotate. Installed as
    `1.0.933+g48ba2f82`.
  - **Baseline**: the day style (Night Maps off) on the installed build `1.0.899+g429edb96` (`429edb9`).
    Its `styleJsonFor` output for day is the same as `pre-main`'s, because only B1's five files differ
    under `app/` between `429edb9` and `15e7900`.
- Each build came from `./gradlew assembleDebug` with 0 compile errors, and was installed with
  `adb install -r <absolute path>`. There was no uninstall and no data clear.
- **Forcing the style.** Turning Night Maps on in Settings did not restyle a loaded map, which confirms
  the known missing key: the style `LaunchedEffect` at `SightingsMap.kt:416` is keyed without
  `nightMode`. The map chrome's content description said "Night mode on" while the tiles stayed in day
  colours.
  - For each variant, the style was forced by switching Topographical → Street → Topographical.
  - Topographical was then captured, and Street was captured after switching to it.
- The view was the same for every capture: the map stayed centred on the device's position, and the HUD
  reads the same grid square to within 1 m. The V2 residual of 0.0 confirms the frames align.
- **Markers in view:** only the location puck. No track or waypoint was in view, and none was created.
- **Where the captures are.** They show the device's location, so they are not in the repository. They
  are in `~/Zynergy/forager-night-spike/`, with `MANIFEST.md` (6 rows, 6 PNGs).
- The comparison script is `~/Zynergy/forager-night-spike/_nav/compare_captures.py`, and its output is
  `_nav/compare_captures.out` beside it.

### What the screenshots show

| Capture | Inverted? | Hues | Legibility (visual reading, not measured) |
|---|---|---|---|
| `baseline-day_topographical.png` | n/a (day) | Pale ground, yellow-green woodland, orange highway, yellow minor roads, light-blue river, brown contours, grey hillshade | Day reference |
| `baseline-day_street.png` | n/a (day) | Off-white ground, green woodland and parks, pink-red motorway, pale-yellow roads, light-blue river | Day reference |
| `v1_topographical.png` | Yes: ground near-black, dark linework pale | Kept. Woodland green to olive, highway orange, minor roads yellow, river dark teal-blue, contours brown-orange | Place names, road numbers and contour labels render pale on dark and are readable. Grey hillshade is inverted as well: shadowed slopes now render lighter than lit ones. Whether that reads as inverted relief was not assessed |
| `v1_street.png` | Yes | Kept. Woodland mid-green, motorway pink-red, minor roads olive-yellow, river dark teal | Place and road labels pale on dark, readable. Pale-yellow minor roads became dark olive, visibly lower contrast against the dark ground than in the day map |
| `v2_topographical.png` | Yes | Rotated 180°. Woodland violet/purple, river rust-brown, highway and minor roads blue, contours light blue | Labels pale on dark, readable. Colour identity of every feature changed |
| `v2_street.png` | Yes | Rotated 180°. Woodland magenta-purple, river brown, motorway teal, minor roads dark navy | Labels readable. Minor roads (dark navy) are low contrast on the dark ground |

Numeric comparison against the day baseline of the same basemap. The map area only (x 0–900,
y 260–1880 of 1080×2316) is used, every 37th pixel, for 39,406 samples per pair. "Saturated mid-L"
means HLS S > 0.25 and 0.15 < L < 0.85 in both images.

| Pair | median \|(255−day)−night\| per channel | median \|day−night\| | HLS L_day+L_night P10/P50/P90 | Hue shift P50/P75/P90 (n) |
|---|---|---|---|---|
| Topographical V1 | 6.0 | 126.0 | 0.94 / 1.00 / 1.00 | 0° / 0° / 1° (12,656) |
| Topographical V2 | 0.0 | 131.7 | 1.00 / 1.00 / 1.00 | 180° / 180° / 180° (14,114) |
| Street V1 | 17.3 | 182.3 | 0.96 / 1.00 / 1.03 | 0° / 0° / 1° (14,264) |
| Street V2 | 0.0 | 182.3 | 1.00 / 1.00 / 1.00 | 180° / 180° / 180° (15,040) |

- **The location puck** is drawn by MapLibre's LocationComponent, not the raster layer, so it is not
  touched by the paint.
  - In `baseline-day_topographical.png` (19:51:01Z) it is blue.
  - In every later capture, including both day Street and every variant, it is red.
  - The change came between the first two captures, which were on the same build and style, after one
    basemap switch. So it is not an effect of either variant. Its cause was not determined.
- **The app's own chrome** is Compose, drawn above the map, and is unchanged by the variants: the search
  bar, the HUD, the control column and the bottom navigation.

## 2. Tile measurement

- **Area and tiles.**
  - Tillamook State Forest, Oregon, around 45.60°N, 123.45°W (public land, not the owner's location).
  - A 3×3 grid at z13 (x 1285–1287, y 2926–2928) and at z14 (x 2572–2574, y 5853–5855).
  - Sources: `a.tile.opentopomap.org` and `tile.openstreetmap.org`.
  - **36 tiles fetched, all HTTP 200**, with no 403 or 429. The User-Agent was
    `Forager-night-evidence-tile-sample/1.0 (Forager Android app research; one-off 36-tile sample; …)`,
    with 1.2 s between requests.
  - The fetch log is `assets/2026-09-27-night-inversion-tile-measurement/fetch-log.tsv` (36 lines).
  - The tiles themselves are in `~/Zynergy/forager-tile-sample/`, not in the repository.
- **Tool.** A script of my own, outside the repository:
  `~/Zynergy/forager-tile-sample/analyse_tiles.py`, sha256 `a7e4e3b9…da53`, run as
  `python3 analyse_tiles.py`. Pure Python and Pillow; there is no numpy on the host.
  - `scripts/measure-night-inversion.py` was not used: its interface does not fit. It hardcodes three
    other locations and z14–19, fetches single tiles, and reports WCAG luminance and HLS hue only, with
    no Oklab, cluster hex or grid input.
  - **Luminance** is WCAG relative luminance, the formula `MapPaletteTest.kt` and that script use, and
    Oklab L.
  - **Bins:** Oklab L in 0.1 steps; C in five bands; hue in 30° steps over chromatic pixels (C ≥ 0.02).
  - **Clusters:** unique colours are merged greedily in Oklab, largest first. A colour joins the first
    cluster whose seed is within ΔE_ok 0.04. Each cluster is reported as its share-weighted mean colour.
  - No tile has alpha < 255.
- **Full output and the complete bin and cluster tables** are
  `assets/2026-09-27-night-inversion-tile-measurement/analysis-output.txt`, `bins.csv` and
  `summary.csv`. They cover z13 and z14 separately as well as pooled.
- **Pixel counts:** 18 tiles × 65,536 = 1,179,648 px per basemap, matching the output.

### Luminance percentiles (z13+z14 pooled)

| Basemap | WCAG rel. luminance P10 / P50 / P90 | Oklab L P10 / P50 / P90 |
|---|---|---|
| OpenTopoMap | 0.260 / 0.591 / 0.864 | 0.632 / 0.835 / 0.947 |
| OSM | 0.570 / 0.570 / 0.570 | 0.821 / 0.821 / 0.821 |

OSM's three percentiles coincide because 87.0% of its pixels are one colour, the woodland fill `#add19e`.
Per zoom (in `analysis-output.txt`), OpenTopoMap z13 is 0.207 / 0.574 / 0.890 and z14 is
0.309 / 0.604 / 0.863. OSM z13 is 0.570 / 0.570 / 0.577.

### Oklab distribution (share of all pixels, pooled)

| Oklab L bin | 0–.5 | .5–.6 | .6–.7 | .7–.8 | .8–.9 | .9–1 |
|---|---|---|---|---|---|---|
| OpenTopoMap | 1.71% | 5.68% | 11.43% | 21.70% | 34.90% | 24.58% |
| OSM | 0.10% | 0.12% | 0.33% | 5.80% | 92.00% | 1.66% |

| Oklab C band | < .02 | .02–.05 | .05–.10 | .10–.15 | ≥ .15 |
|---|---|---|---|---|---|
| OpenTopoMap | 3.12% | 1.74% | 29.80% | 64.18% | 1.17% |
| OSM | 2.16% | 4.49% | 92.80% | 0.51% | 0.04% |

Hue, as a share of chromatic pixels:

- **OpenTopoMap:** 60–90° 4.35%, 90–120° 46.72%, 120–150° 46.79%, 240–270° 1.59%; every other bin is
  under 0.3%.
- **OSM:** 60–90° 1.02%, 90–120° 0.88%, 120–150° 94.44%, 150–180° 0.78%, 180–210° 0.79%,
  210–240° 1.93%; every other bin is at most 0.08%.

### Dominant colour clusters (pooled)

The feature names in the last column are inferred from colour, checked by eye against the centre z14
tiles. They are not read from map data.

**OpenTopoMap** (151 clusters; top 12 cover 87.36%)

| Hex | Share | Oklab L / C / h | rel. lum | Feature (inferred) |
|---|---|---|---|---|
| `#cfe193` | 21.62% | 0.878 / 0.103 / 119 | 0.692 | woodland fill, lit hillshade |
| `#e4f7af` | 14.81% | 0.945 / 0.095 / 120 | 0.861 | woodland fill, brightest hillshade |
| `#bbcd7c` | 14.63% | 0.816 / 0.107 / 119 | 0.557 | woodland, mid shade |
| `#a8ba66` | 11.35% | 0.756 / 0.111 / 119 | 0.444 | woodland, shaded |
| `#96a34f` | 9.66% | 0.687 / 0.110 / 117 | 0.332 | woodland, shaded |
| `#e7d899` | 4.97% | 0.880 / 0.083 / 96 | 0.684 | non-wooded ground, lit |
| `#7d8b35` | 3.59% | 0.607 / 0.112 / 118 | 0.231 | woodland, deep shade |
| `#68751d` | 2.09% | 0.534 / 0.111 / 118 | 0.158 | woodland, deepest shade |
| `#f6f9c5` | 1.48% | 0.967 / 0.067 / 110 | 0.914 | pale ground |
| `#fbfbfa` | 1.13% | 0.988 / 0.001 / 106 | 0.964 | white: road casing, label halo |
| `#f0e7b1` | 1.08% | 0.922 / 0.070 / 100 | 0.789 | pale non-wooded ground |
| `#cbb76b` | 0.94% | 0.780 / 0.099 / 95 | 0.476 | non-wooded ground, shaded |

The largest non-green clusters, below the top-12 cut:

- **Contours:** `#a78a41` 0.61%, `#d2a65f` 0.56% and `#be984a` 0.43%, brown.
- **Water:** `#204ffd` 0.42%, rivers and streams, saturated blue; `#7395cb` 0.28%, blue.
- **Highway:** `#f29529` 0.26%, orange.
- **Greys:** `#e5e9e1`, `#d5d6cd`, `#afb2af` and `#70726e`, each 0.25–0.53%. These are roads, tracks,
  labels and hillshade on non-wooded ground.

**OSM** (82 clusters; top 12 cover 98.75%)

| Hex | Share | Oklab L / C / h | rel. lum | Feature (inferred) |
|---|---|---|---|---|
| `#add19e` | 87.02% | 0.821 / 0.080 / 137 | 0.570 | woodland fill |
| `#99ba86` | 3.50% | 0.751 / 0.081 / 134 | 0.436 | woodland tree symbols |
| `#aed4dd` | 2.34% | 0.845 / 0.042 / 214 | 0.613 | water (river) |
| `#dce2dc` | 1.36% | 0.907 / 0.010 / 146 | 0.748 | pale grey-green: clearings, casings |
| `#c6d9b3` | 1.34% | 0.861 / 0.055 / 129 | 0.649 | lighter green: scrub or edge blend |
| `#fcfcfb` | 0.80% | 0.991 / 0.001 / 106 | 0.973 | white: casings, label halos |
| `#c6dcd3` | 0.58% | 0.876 / 0.026 / 169 | 0.679 | water edge blend |
| `#c2cabd` | 0.47% | 0.829 / 0.020 / 133 | 0.574 | grey-green blend |
| `#a6a55d` | 0.45% | 0.707 / 0.094 / 108 | 0.358 | olive: track dashes over woodland |
| `#fad5a3` | 0.42% | 0.893 / 0.077 / 75 | 0.706 | primary road fill (OR 6), beige-orange |
| `#9bc487` | 0.24% | 0.775 / 0.095 / 136 | 0.482 | green |
| `#acc2a5` | 0.21% | 0.789 / 0.047 / 138 | 0.501 | green-grey |

Smaller non-green clusters: `#d0b890` 0.10% and `#c49d4f` 0.08%, brown track dashes.

**About the sample.** The area is almost entirely forest, crossed by one highway and one river. That is
representative of the app's foraging use, but it under-represents roads, settlements and open water.

- OSM's ground here is woodland green, not the off-white land colour. OSM's background colour barely
  appears: `#f1f0ec` is 0.08%.
- These shares describe this area. They do not describe the basemaps in general.

## 3. Predictions against the result

- **Planner 1**, at least one variant visibly inverts lightness on both basemaps: **met.** Both variants
  do, on both basemaps.
- **Planner 2**, V2 shifts green towards magenta and V1 largely restores hue: **met.**
  - V2: green goes to violet/magenta, and the measured hue shift is exactly 180°.
  - V1: median hue shift 0°, P90 1°.
  - The earlier report's claim of no inversion is contradicted for min > max.
- **Planner 3**, the dominant non-background clusters:
  - OpenTopoMap has green vegetation (dominant) and brown contours with an orange road: **met**, though
    contours and road are each ≤ 0.61%.
  - OSM has green (dominant), a beige-orange road `#fad5a3` and blue water `#aed4dd`: **met**, at small
    shares.
- **Planner 4**, phone restored: **met.** The build and settings are recorded in the `RECORD.md`
  terminal.
- **Coder mechanism** (intent `2026-09-27-02`):
  - Brightness min/max maps channel c to min + (max − min)·c, so V2 = 1 − c, a naive invert: **met**,
    with a residual of 0.0.
  - V1's hue would drift somewhat because of the hue-rotate approximation: **missed.** Hue is preserved
    to 1° at P90. V1 does differ from a naive invert (residual 6.0 / 17.3), as a hue-preserving invert
    should.
  - Labels stay legible: met on visual reading.
  - The prediction that a plain relaunch might render day was not tested, because the style was always
    forced by a basemap switch. That Night Maps' toggle alone does not restyle a loaded map was observed.
  - The prediction that P50 luminance would be above 0.5 on both basemaps: met (0.591 and 0.570).

## 4. What could not be determined

- **The shader order and formula themselves.** My mechanism was stated from memory of MapLibre Native's
  raster shader and was not read from the 13.5.0 AAR. Only its output was measured.
- **Legibility for a user at night, on a dark-adapted eye.** The legibility column is a reading of
  screenshots at a desk, not a measurement. No contrast ratios were computed for labels or thin
  features in the variant captures.
- **Markers.** The only marker in view was the location puck, and no track or waypoint was on screen.
  How the day marker palette (`MapPalette.DAY`, `SightingsMap.kt:216`) reads on an inverted ground was
  not observed.
- **Why the location puck turned from blue to red** between the first and second baseline captures.
- **The offline vector style and satellite.** Neither was tested: the offline style has no raster layer
  (`BasemapStyles.kt:113-119`), and satellite is out of scope.
- **Other areas and zooms.** The tile sample is one forested area at z13/z14. Towns, open water and
  z15–17 were not measured.
- **Whether the inverted hillshade** (V1 Topographical) reads as inverted relief to a user.
