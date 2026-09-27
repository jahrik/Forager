# Marker swatch board: candidate day and V1-night colours for every map marker

**Date:** 2026-09-26 (UTC; IDs dated 2026-09-27 as in the night-inversion findings).
**Dispatches:** `prompts/preserved/2026-09-26-39.md` (Type build; the spec) and
`prompts/preserved/2026-09-26-40.md` (continuation, with two owner rulings). `RECORD.md` intent
`2026-09-27-05`, continuation `2026-09-27-06`.
**Branch:** `marker-swatches`, cut from `origin/pre-main` `9ea36d6` (merge of #128).
**What this is:** design evidence only. It proposes candidates by the dispatch's method and measures them.
It does not pick the palette; the owner picks from the board. No app, test or `MapPalette` change;
`git diff origin/pre-main -- app/` is empty.
**Board:** [`assets/2026-09-27-marker-swatch-board/swatch-board.png`](assets/2026-09-27-marker-swatch-board/swatch-board.png),
1362 x 3074 px, 1,380,801 bytes.

## Summary

- **V1 is the night transform, by the owner's choice.** Plain inversion (V2) makes blue-violet the
  dominant colour of the ground. V1 holds hue, so blue stays at about the day map's share: **1.84%** of pixels on Topo
  and **2.32%** on Street, against **21.92%** and **1.18%** for V2 in the dispatch's 200–280° band. V2's
  violet actually sits just above that band, at 280–320°. Section 1 has the figures and the sources.
- **The board.** Every role has a rank-1 candidate drawn over the same real tile crop in all four columns
  (Topo Day, Topo Night, Street Day, Street Night), with today's colour drawn small beside it.
- **Every candidate fails (b), and no fill can fix it.** (b) measures the casing against the ground's
  P10/P50/P90 luminance, so it depends on the casing alone. A white casing on the light day ground reaches
  1.15:1 at Topo P90. A black casing on the V1 night ground reaches 1.87:1 at Topo P10. That is the
  direction's casing polarity failing, and no choice of fill changes it (section 3). The constraint is not relaxed here.
- **User marks, excluding (b):** all six have rank-1 candidates in the purple–magenta band that pass (a),
  (c), (d) and (e) in both modes, on both basemaps at once. The smallest margin is +0.367, for the live
  breadcrumb at night.
- **System marks do not pass easily.** Search centre and offline region pass by day but fail (d) at night:
  two light neutrals and a light-grey ring crowd one another. The selected ring fails (a) in both modes and
  (c) by day. The night sighting dot fails (a) at 0.093 against a threshold of 0.15: a dark fill sits among the
  dark V1 forest clusters.
- **Colour vision:** under deuteranopia, find pin / planned trip collapse by day (0.146 → 0.059), and
  waypoint / planned trip at night (0.172 → 0.043). Protanopia adds more pairs (section 6).

## 1. Night transform: V1, and why not V2

**The owner's choice (planner session, quoted in the continuation dispatch):** "I like the V1 arrangement
better. The blue is pretty but that can be an eyesore at night", "The blue in V2 that is", and "Blue can
be in night palette, but not the dominant color like V2".

**Reason recorded with the choice:** plain inversion (V2, `raster-brightness-min` 1 and `-max` 0 with no hue
rotate) turns the whole ground to blue-violet, which the owner finds an eyesore at night. V1 adds
`raster-hue-rotate` 180, which restores each hue after the lightness inversion, so blue does not dominate the night
ground, by construction. The spike measured V1's hue drift at P90 1°
(`2026-09-27-night-inversion-spike-and-tile-measurement.md`, Summary).

**What the ruling constrains.** The ruling concerns the basemap: blue must not dominate the night ground.
Marker candidates may be blue at night, and no blue constraint on markers is applied here. A planner
relay briefly proposed a marker rule "(f): no blue at night". The owner never gave it, and it was withdrawn
before the continuation dispatch. It is recorded here as proposed and withdrawn, and applied nowhere. The
candidate pools below follow the planner's direction (user marks in Oklab hue 300–360° and 0–10°), which
was written before the ruling and is unchanged by it. No pool was widened or narrowed for blue.

**Measured blue-family share of the ground.** Blue family is Oklab hue in [200°, 280°) among chromatic pixels
(C ≥ 0.02, the findings doc's achromatic cut, `analyse_tiles.py` `CHROMA_MIN`). Shares are given both of all pixels
and of chromatic pixels.

*Source used for the figures: the transformed sample tiles.* All 18 tiles per basemap (1,179,648 px each), day
pixels put through V1 (the cited shader formula, section 2) and through V2 = 255 − c per channel (the spike
measured V2 at median residual 0.0 against that formula).

| Basemap | Mode | Blue-family px | Share of all px | Share of chromatic px |
|---|---|---:|---:|---:|
| Topo | Day | 21,319 | 1.81% | 1.87% |
| Topo | **V1 night** | 21,688 | **1.84%** | 1.90% |
| Topo | V2 night | 258,629 | **21.92%** | 22.59% |
| Street | Day | 27,210 | 2.31% | 2.36% |
| Street | **V1 night** | 27,381 | **2.32%** | 2.37% |
| Street | V2 night | 13,968 | **1.18%** | 1.21% |

*Cross-check: the spike captures* (`baseline-day_*`, `v1_*` and `v2_*` PNGs, map box x 0–900, y 260–1880,
1,458,000 px each). These frames show a different, wetter area than the tile sample, and include labels, the
location puck and UI chrome.

| Basemap | Mode | Share of all px | Share of chromatic px |
|---|---|---:|---:|
| Topo | Day | 10.14% | 23.22% |
| Topo | V1 night | 10.26% | 22.86% |
| Topo | V2 night | 11.67% | 25.93% |
| Street | Day | 8.95% | 17.37% |
| Street | V1 night | 9.15% | 17.37% |
| Street | V2 night | 5.53% | 10.46% |

The captures' blue is mostly light water: `#A3DCE8` is 5.39% of the Topo day frame and `#AAD3DF`
is 5.50% of the Street day frame. V1 keeps it at the day share on both sources, which is what the ruling
asks of the ground. How large that share is depends on how much water is in view, and the forest-heavy tile sample
under-represents water.

**Where V2's colour actually goes.** Oklab hue of V2's chromatic pixels (tiles, 20° bins): Topo
280–300° 73.1%, 260–280° 21.4%; Street 300–320° 92.6%. So V2's "blue-violet" sits mostly *above* the
200–280° band, and the band's V2 figures (21.92%, 1.18%) understate what the owner saw. With the band
extended to 300°, Topo V2 would be 95.67% of chromatic pixels. The band is the dispatch's and is not
changed here. For comparison, V1's top bins are 120–140° (Topo 50.9%, Street 93.7%), the day map's greens.

## 2. Method

**Tools.** Host Python 3.14.4 with Pillow 12.1.1, no numpy. Scripts live in `~/Zynergy/forager-swatch-work/`,
outside the repo (sha256 in section 9).

**V1 night transform: cited, not fitted.** MapLibre Native at tag `android-v13.5.0` (tag object `efbde81`,
commit `a666f02633c1d03bd793dc214cbb2dbcacf8d74e`; the app pins `maplibre = "13.5.0"`,
`gradle/libs.versions.toml:37`), from two files: `shaders/raster.fragment.glsl` and
`src/mbgl/renderer/layers/raster_layer_tweaker.cpp`. Local copies were checked byte-for-byte against the tag with
`gh api`. The tweaker computes `spinWeights(spin) = ((2cos+1)/3, (−√3·sin − cos + 1)/3, (√3·sin − cos + 1)/3)`.
The shader applies `rgb' = (dot(rgb, w.xyz), dot(rgb, w.zxy), dot(rgb, w.yzx))`. Saturation (factor 0) and contrast
(factor 1) are identities at their defaults (`saturationFactor(0) = 0`, `contrastFactor(0) = 1`). The output is
`mix(brightness_min, brightness_max, rgb')`, which is 1 − rgb' for V1, on gamma-encoded values, clamped by the
framebuffer. At 180°, V1 night = clamp(c + 1 − 2·mean(r,g,b)) per channel.
**Residual** against the spike captures (predicting `v1_*` from `baseline-day_*`, map box, every 37th pixel,
39,406 samples per basemap): mean per-channel |error| **0.63** (0.25% of full scale) on Topo and
**0.52** (0.20%) on Street. The median is 0.00, P99 is 1.00, and 0.60% / 0.58% of samples exceed 5%. The naive
255 − c gives 10.27% / 8.34%. That is far under the 5% abort threshold.

**Colour maths.** Oklab: Björn Ottosson, "A perceptual color space for image processing" (2020),
<https://bottosson.github.io/posts/oklab/>, the same constants as `analyse_tiles.py`. ΔE is Euclidean
distance in Oklab. WCAG 2.1 relative luminance and contrast ratio:
<https://www.w3.org/TR/WCAG21/#dfn-relative-luminance>, contrast = (Y_hi + 0.05)/(Y_lo + 0.05).
Colour-vision deficiency: Machado, Oliveira and Fernandes, "A Physiologically-based Model for Simulation of
Color Vision Deficiency", IEEE TVCG 15(6), 2009, using the severity 1.0 deuteranopia and protanopia matrices from the
authors' published table, applied in linear RGB (the values were checked against the table).

**Ground references.** All 18 tiles per basemap, z13 and z14 pooled. Clustering is the findings doc's method,
unchanged: unique colours are merged greedily in Oklab, largest first, joining the first cluster whose seed is within
ΔE 0.04. Each cluster is reported as its share-weighted mean sRGB. Night = V1 of every day pixel,
clustered independently. Features (contours, roads, water) are the nearest day cluster to the named hex,
with night = V1 of the same member pixels. The sample's primary road is beige-orange `#fad5a3`. The
pink-red road colours the direction names are not in the sample, so they are taken from
openstreetmap-carto v6.1.0 `style/road-colors-generated.mss`: `@motorway-fill #e892a2` and
`@trunk-fill #f9b29c`, each also through V1.

**Constraints** (the dispatch's, unrelaxed). Margins are relative: value/threshold − 1, and (30 − Δh)/30 for (e).
A margin below 0 fails.
- (a) ΔE ≥ 0.15 from every ground cluster of 0.5% share or more, over both basemaps of the mode.
- (b) contrast of the casing against ground Y at P10, P50 and P90 ≥ 3:1, both basemaps.
- (c) contrast of the fill against its own casing ≥ 3:1.
- (d) ΔE ≥ 0.10 between every pair of roles within a mode.
- (e) Oklab hue difference between a role's day and night fills ≤ 30°. Not applicable when either fill is
  achromatic (C < 0.02).
- (k), which is not one of (a)–(e): the direction's "keep them clear of OSM night's pink-red primary
  roads", for user marks only. ΔE ≥ 0.15 (the (a) threshold) from the carto motorway and trunk colours and
  the sample's primary road, each in the mode's form.

**Choices the method left open.** The stopped coder made these, and this build reran them unchanged. They are
listed with their alternatives; none was chosen by the owner or the planner.
1. **Casing** is `#FFFFFF` by day and `#000000` by night, the extremes of the direction's "white or near-white by
   day, dark by night". A near-white or near-black casing only lowers contrast, so it cannot rescue (b).
2. **The sighting dot's halo stays white in both modes** ("stays dark with a white halo"). The alternative is a
   dark halo at night like every other casing, which (c) would then fail against a dark fill.
3. **The selected ring is modelled as today's**: a colour that replaces the dot's halo, so the ring is its
   own casing, (c) is checked against the dot fill, and it must be ΔE ≥ 0.10 from the white halo it replaces.
   The alternative is a separate ring outside the halo, with its own casing.
4. **One colour per role per mode, used on both basemaps**, so (a) and (b) take the worst case over Topo and
   Street. The alternative, per-basemap colours, would widen the margins but give each role four colours.
5. **Pools.** User marks and the centre pin: Oklab h 300–370° in 5° steps, L 0.20–0.95 in 0.025 steps and C 0.04–0.42 in 0.02 steps,
   in gamut. The centre pin also gets neutrals. System marks: neutrals, L 0–1 in 0.025 steps. Sighting dot: dark, h 30–100°,
   L 0.15–0.45, C 0.01–0.08, plus today's `#3B2E24`.
6. **Today's day sighting dot is kept** (`#3B2E24`), since it passes (a), (c), (d) and (e) ("keep today's value if it
   passes"). The night dot is searched, because today's value fails (a) at night.
7. **Ranking and search.** Candidates are ranked by worst-case margin over (a)–(e) and (k). (b) is the same for every
   fill in a role and mode, so where it is the worst case for all of them, ties are broken on the rest. The search is
   joint coordinate ascent over (role, mode) from 40 seeded random starts, followed by an exhaustive pass over the
   three neutral system roles together. Rank 1 is the joint optimum. Ranks 2 and 3 are the next best for that role with
   every other role held at rank 1, each at least ΔE 0.04 from the ranks above it. This build reran all 40 seeds
   from scratch, and the result was byte-identical to the stopped coder's cached one.
8. **"Current" at night is today's colour**, because markers always use `MapPalette.DAY`
   (`SightingsMap.kt:216`). The one exception is the centre-pin picker, whose night current is theme dark `primary` `#A8CBA0`.
9. **Board rendering.** One crop, used in every cell: OpenTopoMap and OSM tile z14/2572/5854, tile px (0,84)–(144,196),
   at 2 display px per tile px. It holds forest, contours (Topo), OR 6 / Wilson River Highway and the
   Wilson River. Shapes follow `SightingsMap.kt` at `9ea36d6`:
   - pin 22×28 dp and diamond 22 dp (`waypointPinBitmap`, `plannedTripDiamondBitmap`, lines 1059–1105);
   - tracks 6 px, solid or dashed (0.4, 1.0), round caps;
   - sighting dot r 9 px, 0.7 fill, 1.5 px stroke at 0.85;
   - search centre r 8 px with a 2 px stroke;
   - offline region at 0.2 fill with a 1.5 px outline;
   - centre pin `Icons.Filled.LocationOn` 40 dp.

   Candidate casing is 1.5 px each side (today's halo width), which is a choice. Today's colour is drawn small, in
   today's styling, with no casing.

**Centre-pin picker: flagged.** The candidates move it off theme `primary` (today `#2E5339` light, `#A8CBA0`
dark, `CentrePinLocationPicker.kt:200`) onto map-palette colours, as the direction asks. That change
belongs to the owner.

## 3. Ground references

**Topo Day**: 18 tiles, 1,179,648 px, 3,480 unique colours, 151 clusters. Luminance Y P10 / P50 / P90: 0.2604 / 0.5913 / 0.8641. The 19 clusters at 0.5% or more cover 91.87% (sum of the listed shares).

| Cluster | Share | Oklab L | C | h (deg) | Y |
|---|---:|---:|---:|---:|---:|
| `#CFE193` | 21.62% | 0.878 | 0.103 | 119.1 | 0.692 |
| `#E4F7AF` | 14.81% | 0.945 | 0.095 | 120.2 | 0.861 |
| `#BBCD7C` | 14.63% | 0.816 | 0.107 | 119.1 | 0.557 |
| `#A8BA66` | 11.35% | 0.756 | 0.111 | 119.1 | 0.444 |
| `#96A34F` | 9.66% | 0.687 | 0.110 | 116.7 | 0.332 |
| `#E7D899` | 4.97% | 0.880 | 0.083 | 96.4 | 0.684 |
| `#7D8B35` | 3.59% | 0.607 | 0.112 | 117.7 | 0.231 |
| `#68751D` | 2.09% | 0.534 | 0.111 | 117.7 | 0.158 |
| `#F6F9C5` | 1.48% | 0.967 | 0.067 | 110.3 | 0.914 |
| `#FBFBFA` | 1.13% | 0.988 | 0.001 | 106.4 | 0.964 |
| `#F0E7B1` | 1.08% | 0.922 | 0.070 | 99.7 | 0.789 |
| `#CBB76B` | 0.94% | 0.780 | 0.099 | 95.1 | 0.476 |
| `#DDC682` | 0.93% | 0.831 | 0.091 | 91.5 | 0.574 |
| `#8A7833` | 0.70% | 0.575 | 0.091 | 94.7 | 0.191 |
| `#B1A961` | 0.66% | 0.725 | 0.094 | 102.9 | 0.386 |
| `#A78A41` | 0.61% | 0.645 | 0.098 | 87.7 | 0.268 |
| `#D2A65F` | 0.56% | 0.751 | 0.103 | 78.1 | 0.418 |
| `#E5E9E1` | 0.53% | 0.929 | 0.011 | 128.6 | 0.804 |
| `#918E47` | 0.52% | 0.634 | 0.093 | 106.6 | 0.258 |
| **sum** | **91.87%** | | | | |

**Topo Night (V1)**: 18 tiles, 1,179,648 px, 3,468 unique colours, 138 clusters. Luminance Y P10 / P50 / P90: 0.0434 / 0.1295 / 0.3560. The 19 clusters at 0.5% or more cover 90.85% (sum of the listed shares).

| Cluster | Share | Oklab L | C | h (deg) | Y |
|---|---:|---:|---:|---:|---:|
| `#4E6012` | 17.38% | 0.458 | 0.102 | 122.2 | 0.100 |
| `#627524` | 14.81% | 0.530 | 0.108 | 121.6 | 0.154 |
| `#334801` | 13.95% | 0.370 | 0.094 | 126.3 | 0.053 |
| `#798936` | 10.51% | 0.599 | 0.110 | 119.0 | 0.222 |
| `#879A45` | 5.90% | 0.653 | 0.113 | 120.1 | 0.287 |
| `#9CA754` | 5.42% | 0.701 | 0.109 | 115.5 | 0.353 |
| `#59490A` | 3.86% | 0.410 | 0.079 | 93.4 | 0.069 |
| `#465208` | 3.05% | 0.414 | 0.093 | 119.2 | 0.074 |
| `#2A2D01` | 2.57% | 0.285 | 0.063 | 113.7 | 0.024 |
| `#AEBC65` | 2.22% | 0.765 | 0.113 | 116.7 | 0.459 |
| `#C2D076` | 2.09% | 0.827 | 0.116 | 116.2 | 0.579 |
| `#666319` | 1.71% | 0.488 | 0.093 | 107.1 | 0.118 |
| `#323701` | 1.62% | 0.321 | 0.072 | 115.3 | 0.034 |
| `#4B3A04` | 1.37% | 0.357 | 0.070 | 89.3 | 0.045 |
| `#A49047` | 1.28% | 0.656 | 0.096 | 94.3 | 0.283 |
| `#7F752A` | 1.06% | 0.556 | 0.096 | 101.7 | 0.174 |
| `#020302` | 0.88% | 0.093 | 0.007 | 145.0 | 0.001 |
| `#BEA964` | 0.63% | 0.738 | 0.092 | 93.0 | 0.402 |
| `#2E5CFA` | 0.55% | 0.549 | 0.240 | 265.8 | 0.151 |
| **sum** | **90.85%** | | | | |

**Street Day**: 18 tiles, 1,179,648 px, 3,250 unique colours, 82 clusters. Luminance Y P10 / P50 / P90: 0.5695 / 0.5695 / 0.5695. The 7 clusters at 0.5% or more cover 96.94% (sum of the listed shares).

| Cluster | Share | Oklab L | C | h (deg) | Y |
|---|---:|---:|---:|---:|---:|
| `#ADD19E` | 87.02% | 0.821 | 0.080 | 136.6 | 0.570 |
| `#99BA86` | 3.50% | 0.751 | 0.081 | 134.4 | 0.436 |
| `#AED4DD` | 2.34% | 0.845 | 0.042 | 213.7 | 0.613 |
| `#DCE2DC` | 1.36% | 0.907 | 0.010 | 145.5 | 0.748 |
| `#C6D9B3` | 1.34% | 0.861 | 0.055 | 129.2 | 0.649 |
| `#FCFCFB` | 0.80% | 0.991 | 0.001 | 106.4 | 0.973 |
| `#C6DCD3` | 0.58% | 0.876 | 0.026 | 169.2 | 0.679 |
| **sum** | **96.94%** | | | | |

**Street Night (V1)**: 18 tiles, 1,179,648 px, 3,246 unique colours, 101 clusters. Luminance Y P10 / P50 / P90: 0.1092 / 0.1139 / 0.1139. The 7 clusters at 0.5% or more cover 95.73% (sum of the listed shares).

| Cluster | Share | Oklab L | C | h (deg) | Y |
|---|---:|---:|---:|---:|---:|
| `#446835` | 85.99% | 0.477 | 0.088 | 137.1 | 0.114 |
| `#5C7D4A` | 3.50% | 0.551 | 0.084 | 135.2 | 0.174 |
| `#173E48` | 2.14% | 0.341 | 0.047 | 217.3 | 0.041 |
| `#3C512B` | 1.81% | 0.407 | 0.065 | 132.3 | 0.070 |
| `#1F342E` | 0.99% | 0.305 | 0.029 | 174.4 | 0.029 |
| `#22201C` | 0.71% | 0.244 | 0.008 | 84.6 | 0.015 |
| `#537342` | 0.59% | 0.518 | 0.082 | 135.4 | 0.145 |
| **sum** | **95.73%** | | | | |

**Topo features** (named colour, nearest day cluster; night = V1 of the same member pixels)

| Feature | Named | Day cluster | Share | Day L / C / h | Night | Night L / C / h |
|---|---|---|---:|---|---|---|
| contour | `#a78a41` | `#A78A41` | 0.61% | 0.645 / 0.098 / 87.7 | `#B09249` | 0.672 / 0.099 / 87.2 |
| contour | `#d2a65f` | `#D2A65F` | 0.56% | 0.751 / 0.103 / 78.1 | `#976C24` | 0.562 / 0.102 / 76.3 |
| contour | `#be984a` | `#BE984A` | 0.43% | 0.699 / 0.106 / 83.3 | `#A78234` | 0.627 / 0.105 / 83.1 |
| water | `#204ffd` | `#204FFD` | 0.42% | 0.526 / 0.260 / 265.3 | `#2D5BFE` | 0.550 / 0.246 / 265.8 |
| water | `#7395cb` | `#7395CB` | 0.28% | 0.666 / 0.089 / 259.4 | `#3B5D92` | 0.478 / 0.095 / 259.0 |
| highway | `#f29529` | `#F29529` | 0.26% | 0.750 / 0.158 / 63.6 | `#D17408` | 0.650 / 0.152 / 59.1 |
| road/label grey | `#e5e9e1` | `#E5E9E1` | 0.53% | 0.929 / 0.011 / 128.6 | `#1B1E16` | 0.229 / 0.016 / 124.3 |
| road/label grey | `#d5d6cd` | `#D5D6CD` | 0.48% | 0.873 / 0.012 / 111.8 | `#2F3027` | 0.305 / 0.016 / 112.5 |
| road/label grey | `#afb2af` | `#AFB2AF` | 0.25% | 0.760 / 0.005 / 145.5 | `#4E524E` | 0.434 / 0.008 / 145.4 |
| road/label grey | `#70726e` | `#70726E` | 0.25% | 0.549 / 0.007 / 128.6 | `#8F918D` | 0.654 / 0.006 / 128.6 |

**Street features** (named colour, nearest day cluster; night = V1 of the same member pixels)

| Feature | Named | Day cluster | Share | Day L / C / h | Night | Night L / C / h |
|---|---|---|---:|---|---|---|
| water | `#aed4dd` | `#AED4DD` | 2.34% | 0.845 / 0.042 / 213.7 | `#183E47` | 0.341 / 0.045 / 215.7 |
| primary road | `#fad5a3` | `#FAD5A3` | 0.42% | 0.893 / 0.077 / 74.9 | `#573201` | 0.355 / 0.078 / 65.9 |
| track | `#d0b890` | `#D0B890` | 0.10% | 0.793 / 0.060 / 80.3 | `#6A5229` | 0.454 / 0.066 / 79.3 |
| track | `#c49d4f` | `#C49D4F` | 0.08% | 0.716 / 0.107 / 82.9 | `#A37C2E` | 0.610 / 0.106 / 81.5 |
| track | `#a6a55d` | `#A6A55D` | 0.45% | 0.707 / 0.094 / 107.9 | `#8B8941` | 0.616 / 0.095 / 107.4 |
| motorway (carto v6.1.0, not in sample) | `#e892a2` | `#E892A2` | 0.00% | 0.752 / 0.105 / 7.8 | `#7F2939` | 0.415 / 0.119 / 12.9 |
| trunk (carto v6.1.0, not in sample) | `#f9b29c` | `#F9B29C` | 0.00% | 0.825 / 0.089 / 37.9 | `#732C16` | 0.392 / 0.106 / 37.4 |

## 4. Casing contrast, constraint (b)

(b) depends only on the casing and the ground, so it is the same for every fill of a role in a mode. The selected
ring, whose colour is its own casing, is the exception, and its (b) is in its table.

**Day**: casing `#FFFFFF` (every role but the sighting dot and the ring); sighting-dot halo `#FFFFFF`.

| Ground percentile | Y | contrast, casing | margin | contrast, dot halo | margin |
|---|---:|---:|---:|---:|---:|
| Topo P10 | 0.2604 | 3.383 | +0.128 | 3.383 | +0.128 |
| Topo P50 | 0.5913 | 1.637 | -0.454 | 1.637 | -0.454 |
| Topo P90 | 0.8641 | 1.149 | -0.617 | 1.149 | -0.617 |
| Street P10 | 0.5695 | 1.695 | -0.435 | 1.695 | -0.435 |
| Street P50 | 0.5695 | 1.695 | -0.435 | 1.695 | -0.435 |
| Street P90 | 0.5695 | 1.695 | -0.435 | 1.695 | -0.435 |

**Night**: casing `#000000` (every role but the sighting dot and the ring); sighting-dot halo `#FFFFFF`.

| Ground percentile | Y | contrast, casing | margin | contrast, dot halo | margin |
|---|---:|---:|---:|---:|---:|
| Topo P10 | 0.0434 | 1.868 | -0.377 | 11.242 | +2.747 |
| Topo P50 | 0.1295 | 3.590 | +0.197 | 5.850 | +0.950 |
| Topo P90 | 0.3560 | 8.120 | +1.707 | 2.586 | -0.138 |
| Street P10 | 0.1092 | 3.184 | +0.061 | 6.595 | +1.198 |
| Street P50 | 0.1139 | 3.278 | +0.093 | 6.406 | +1.135 |
| Street P90 | 0.1139 | 3.278 | +0.093 | 6.406 | +1.135 |

By day a white casing reaches 3:1 only against Topo P10. At night a black casing fails against Topo P10,
because V1 turns the lightest day pixels near black. The white sighting-dot halo passes at night everywhere except
Topo P90. **No casing polarity passes (b) at every percentile in either mode.** A white casing by day would need
ground Y ≤ 0.30; a black casing at night would need Y ≥ 0.10. That is a finding for the owner, not something this
build resolves.

## 5. Candidates per role and mode

Each row: fill; Oklab L / C / h; casing; then each constraint's raw value with its margin in brackets.
- (a) is the smallest ΔE to any ground cluster of 0.5% or more, and which cluster it is.
- (b) is the smallest casing contrast over the six percentiles.
- (c) is contrast against the casing (or, for the ring, against the dot fill).
- (d) is the smallest ΔE to another role's rank-1 fill.
- (e) is the hue difference against the other mode's rank-1 fill.
- (k) is the smallest ΔE to the three road references.

"today" is the current colour scored under the same model. Recompute a margin from its row: (a) and (k) are
ΔE/0.15 − 1, (b) and (c) are contrast/3 − 1, (d) is ΔE/0.10 − 1, and (e) is (30 − Δh)/30.

#### Waypoint pin

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#E0A030` | 0.749 / 0.142 / 76.5 | `#FFFFFF` | 0.039 Topo `#D2A65F` (-0.741) | 1.15 (-0.617) | 2.27 vs `#FFFFFF` (-0.242) | 0.287 selected_ring (+1.872) | 131.6 vs `#B97DF7` (-3.386) | 0.118 trunk (-0.211) | a, b, c, e, k |
| day | 1 | `#350560` | 0.276 / 0.140 / 300.3 | `#FFFFFF` | 0.359 Topo `#68751D` (+1.397) | 1.15 (-0.617) | 15.65 vs `#FFFFFF` (+4.216) | 0.158 sighting_dot (+0.582) | 4.7 vs `#B97DF7` (+0.845) | 0.496 motorway (+2.305) | b |
| day | 2 | `#360247` | 0.250 / 0.119 / 315.4 | `#FFFFFF` | 0.364 Topo `#68751D` (+1.424) | 1.15 (-0.617) | 16.71 vs `#FFFFFF` (+4.571) | 0.142 sighting_dot (+0.422) | 10.5 vs `#B97DF7` (+0.651) | 0.512 motorway (+2.411) | b |
| day | 3 | `#26004A` | 0.225 / 0.121 / 299.4 | `#FFFFFF` | 0.386 Topo `#68751D` (+1.574) | 1.15 (-0.617) | 17.78 vs `#FFFFFF` (+4.926) | 0.142 offline_region (+0.422) | 5.5 vs `#B97DF7` (+0.816) | 0.542 motorway (+2.615) | b |
| night | today | `#E0A030` | 0.749 / 0.142 / 76.5 | `#000000` | 0.061 Topo `#BEA964` (-0.595) | 1.87 (-0.377) | 9.24 vs `#000000` (+2.080) | 0.160 selected_ring (+0.601) | 136.2 vs `#350560` (-3.541) | 0.362 motorway (+1.414) | a, b, e |
| night | 1 | `#B97DF7` | 0.701 / 0.180 / 304.9 | `#000000` | 0.215 Topo `#2E5CFA` (+0.431) | 1.87 (-0.377) | 7.34 vs `#000000` (+1.446) | 0.137 kept_track (+0.370) | 4.7 vs `#350560` (+0.845) | 0.335 motorway (+1.234) | b |
| night | 2 | `#AC89E8` | 0.701 / 0.139 / 299.8 | `#000000` | 0.211 Topo `#2E5CFA` (+0.407) | 1.87 (-0.377) | 7.50 vs `#000000` (+1.499) | 0.134 kept_track (+0.341) | 0.5 vs `#350560` (+0.984) | 0.325 motorway (+1.165) | b |
| night | 3 | `#B28DCD` | 0.701 / 0.100 / 310.3 | `#000000` | 0.186 Topo `#BEA964` (+0.237) | 1.87 (-0.377) | 7.58 vs `#000000` (+1.528) | 0.131 find_pin (+0.312) | 10.1 vs `#350560` (+0.664) | 0.308 motorway (+1.053) | b |

#### Kept track

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#C97B3D` | 0.655 / 0.124 / 56.6 | `#FFFFFF` | 0.065 Topo `#A78A41` (-0.566) | 1.15 (-0.617) | 3.29 vs `#FFFFFF` (+0.096) | 0.195 photo (+0.953) | 96.6 vs `#EEA7FE` (-2.219) | 0.137 motorway (-0.089) | a, b, e, k |
| day | 1 | `#A122F8` | 0.574 / 0.280 / 305.0 | `#FFFFFF` | 0.362 Topo `#8A7833` (+1.411) | 1.15 (-0.617) | 5.12 vs `#FFFFFF` (+0.707) | 0.151 planned_trip (+0.507) | 15.1 vs `#EEA7FE` (+0.497) | 0.307 motorway (+1.044) | b |
| day | 2 | `#B742F9` | 0.626 / 0.260 / 310.0 | `#FFFFFF` | 0.340 Topo `#A78A41` (+1.266) | 1.15 (-0.617) | 4.07 vs `#FFFFFF` (+0.356) | 0.137 find_pin (+0.374) | 10.1 vs `#EEA7FE` (+0.663) | 0.256 motorway (+0.706) | b |
| day | 3 | `#C715FF` | 0.625 / 0.300 / 314.9 | `#FFFFFF` | 0.375 Topo `#A78A41` (+1.499) | 1.15 (-0.617) | 4.22 vs `#FFFFFF` (+0.406) | 0.131 find_pin (+0.308) | 5.2 vs `#EEA7FE` (+0.826) | 0.282 motorway (+0.878) | b |
| night | today | `#C97B3D` | 0.655 / 0.124 / 56.6 | `#000000` | 0.076 Topo `#A49047` (-0.495) | 1.87 (-0.377) | 6.38 vs `#000000` (+1.128) | 0.177 find_pin (+0.772) | 111.7 vs `#A122F8` (-2.722) | 0.257 motorway (+0.711) | a, b, e |
| night | 1 | `#EEA7FE` | 0.825 / 0.140 / 320.1 | `#000000` | 0.231 Topo `#BEA964` (+0.540) | 1.87 (-0.377) | 11.59 vs `#000000` (+2.865) | 0.137 waypoint (+0.370) | 15.1 vs `#A122F8` (+0.497) | 0.427 motorway (+1.844) | b |
| night | 2 | `#F791FD` | 0.800 / 0.181 / 325.1 | `#000000` | 0.256 Topo `#BEA964` (+0.708) | 1.87 (-0.377) | 10.42 vs `#000000` (+2.474) | 0.118 waypoint (+0.177) | 20.1 vs `#A122F8` (+0.330) | 0.408 motorway (+1.721) | b |
| night | 3 | `#D0AAFC` | 0.801 / 0.120 / 305.1 | `#000000` | 0.213 Topo `#BEA964` (+0.423) | 1.87 (-0.377) | 10.84 vs `#000000` (+2.613) | 0.117 waypoint (+0.166) | 0.1 vs `#A122F8` (+0.996) | 0.408 motorway (+1.722) | b |

#### Live breadcrumb

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#2979FF` | 0.606 / 0.214 / 260.4 | `#FFFFFF` | 0.302 Topo `#918E47` (+1.013) | 1.15 (-0.617) | 3.98 vs `#FFFFFF` (+0.327) | 0.195 planned_trip (+0.952) | 49.6 vs `#B228F8` (-0.652) | 0.303 motorway (+1.019) | b, e |
| day | 1 | `#650BB1` | 0.425 / 0.220 / 299.9 | `#FFFFFF` | 0.340 Topo `#8A7833` (+1.267) | 1.15 (-0.617) | 9.39 vs `#FFFFFF` (+2.130) | 0.157 centre_pin (+0.573) | 10.0 vs `#B228F8` (+0.667) | 0.386 motorway (+1.574) | b |
| day | 2 | `#60289F` | 0.424 / 0.180 / 300.1 | `#FFFFFF` | 0.305 Topo `#8A7833` (+1.035) | 1.15 (-0.617) | 9.12 vs `#FFFFFF` (+2.041) | 0.133 centre_pin (+0.327) | 9.8 vs `#B228F8` (+0.672) | 0.370 motorway (+1.465) | b |
| day | 3 | `#7A0DAD` | 0.450 / 0.220 / 310.0 | `#FFFFFF` | 0.324 Topo `#8A7833` (+1.161) | 1.15 (-0.617) | 8.44 vs `#FFFFFF` (+1.813) | 0.132 planned_trip (+0.322) | 0.1 vs `#B228F8` (+0.998) | 0.355 motorway (+1.365) | b |
| night | today | `#2979FF` | 0.606 / 0.214 / 260.4 | `#000000` | 0.066 Topo `#2E5CFA` (-0.557) | 1.87 (-0.377) | 5.27 vs `#000000` (+0.758) | 0.180 waypoint (+0.797) | 39.6 vs `#650BB1` (-0.319) | 0.341 motorway (+1.270) | a, b, e |
| night | 1 | `#B228F8` | 0.599 / 0.280 / 309.9 | `#000000` | 0.205 Topo `#2E5CFA` (+0.367) | 1.87 (-0.377) | 4.55 vs `#000000` (+0.517) | 0.144 waypoint (+0.440) | 10.0 vs `#650BB1` (+0.667) | 0.310 motorway (+1.068) | b |
| night | 2 | `#AF0ACC` | 0.551 / 0.260 / 320.1 | `#000000` | 0.229 Topo `#2E5CFA` (+0.526) | 1.87 (-0.377) | 3.74 vs `#000000` (+0.246) | 0.126 centre_pin (+0.256) | 20.2 vs `#650BB1` (+0.328) | 0.251 motorway (+0.672) | b |
| night | 3 | `#9F1FDF` | 0.550 / 0.261 / 309.9 | `#000000` | 0.189 Topo `#2E5CFA` (+0.258) | 1.87 (-0.377) | 3.74 vs `#000000` (+0.245) | 0.139 centre_pin (+0.390) | 9.9 vs `#650BB1` (+0.669) | 0.269 motorway (+0.791) | b |

#### Find pin

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#2E5339` | 0.406 / 0.061 / 152.4 | `#FFFFFF` | 0.146 Topo `#68751D` (-0.029) | 1.15 (-0.617) | 8.70 vs `#FFFFFF` (+1.901) | 0.112 selected_ring (+0.116) | 157.6 vs `#F96FAC` (-4.252) | 0.381 motorway (+1.538) | a, b, e |
| day | 1 | `#DA02AF` | 0.600 / 0.260 / 340.1 | `#FFFFFF` | 0.308 Topo `#A78A41` (+1.053) | 1.15 (-0.617) | 4.56 vs `#FFFFFF` (+0.519) | 0.146 planned_trip (+0.456) | 14.8 vs `#F96FAC` (+0.508) | 0.231 motorway (+0.539) | b |
| day | 2 | `#ED039D` | 0.624 / 0.260 / 350.0 | `#FFFFFF` | 0.291 Topo `#A78A41` (+0.937) | 1.15 (-0.617) | 4.12 vs `#FFFFFF` (+0.372) | 0.141 photo (+0.407) | 4.9 vs `#F96FAC` (+0.837) | 0.207 motorway (+0.378) | b |
| day | 3 | `#EB19D0` | 0.649 / 0.280 / 335.0 | `#FFFFFF` | 0.330 Topo `#A78A41` (+1.202) | 1.15 (-0.617) | 3.76 vs `#FFFFFF` (+0.254) | 0.163 kept_track (+0.631) | 19.8 vs `#F96FAC` (+0.338) | 0.224 motorway (+0.494) | b |
| night | today | `#2E5339` | 0.406 / 0.061 / 152.4 | `#000000` | 0.022 Street `#3C512B` (-0.851) | 1.87 (-0.377) | 2.41 vs `#000000` (-0.196) | 0.163 sighting_dot (+0.626) | 172.3 vs `#DA02AF` (-4.744) | 0.109 primary road (-0.272) | a, b, c, e, k |
| night | 1 | `#F96FAC` | 0.725 / 0.179 / 354.8 | `#000000` | 0.213 Topo `#BEA964` (+0.423) | 1.87 (-0.377) | 7.90 vs `#000000` (+1.632) | 0.144 kept_track (+0.436) | 14.8 vs `#DA02AF` (+0.508) | 0.319 motorway (+1.127) | b |
| night | 2 | `#DE77AB` | 0.700 / 0.141 / 349.8 | `#000000` | 0.189 Topo `#BEA964` (+0.260) | 1.87 (-0.377) | 7.33 vs `#000000` (+1.444) | 0.128 waypoint (+0.275) | 9.7 vs `#DA02AF` (+0.677) | 0.291 motorway (+0.937) | b |
| night | 3 | `#E969B8` | 0.700 / 0.181 / 344.8 | `#000000` | 0.230 Topo `#BEA964` (+0.535) | 1.87 (-0.377) | 7.18 vs `#000000` (+1.392) | 0.123 waypoint (+0.230) | 4.7 vs `#DA02AF` (+0.843) | 0.300 motorway (+1.000) | b |

#### Planned trip

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#3B6EA5` | 0.528 / 0.103 / 251.9 | `#FFFFFF` | 0.196 Topo `#8A7833` (+0.308) | 1.15 (-0.617) | 5.30 vs `#FFFFFF` (+0.768) | 0.107 selected_ring (+0.071) | 83.1 vs `#FA01DD` (-1.771) | 0.285 motorway (+0.900) | b, e |
| day | 1 | `#9553A4` | 0.551 / 0.140 / 320.0 | `#FFFFFF` | 0.216 Topo `#8A7833` (+0.438) | 1.15 (-0.617) | 5.18 vs `#FFFFFF` (+0.728) | 0.146 find_pin (+0.456) | 15.1 vs `#FA01DD` (+0.498) | 0.226 motorway (+0.509) | b |
| day | 2 | `#9B4095` | 0.525 / 0.160 / 330.2 | `#FFFFFF` | 0.231 Topo `#8A7833` (+0.537) | 1.15 (-0.617) | 5.90 vs `#FFFFFF` (+0.966) | 0.129 photo (+0.288) | 4.8 vs `#FA01DD` (+0.839) | 0.248 motorway (+0.657) | b |
| day | 3 | `#A35EC1` | 0.600 / 0.160 / 314.7 | `#FFFFFF` | 0.239 Topo `#8A7833` (+0.590) | 1.15 (-0.617) | 4.26 vs `#FFFFFF` (+0.420) | 0.128 kept_track (+0.277) | 20.3 vs `#FA01DD` (+0.323) | 0.199 motorway (+0.326) | b |
| night | today | `#3B6EA5` | 0.528 / 0.103 / 251.9 | `#000000` | 0.143 Topo `#2E5CFA` (-0.044) | 1.87 (-0.377) | 3.96 vs `#000000` (+0.320) | 0.164 centre_pin (+0.638) | 68.1 vs `#9553A4` (-1.269) | 0.224 motorway (+0.494) | a, b, e |
| night | 1 | `#FA01DD` | 0.675 / 0.300 / 335.0 | `#000000` | 0.335 Topo `#2E5CFA` (+1.235) | 1.87 (-0.377) | 6.11 vs `#000000` (+1.038) | 0.148 live_breadcrumb (+0.483) | 15.1 vs `#9553A4` (+0.498) | 0.340 motorway (+1.267) | b |
| night | 2 | `#F63DEE` | 0.700 / 0.280 / 330.0 | `#000000` | 0.317 Topo `#2E5CFA` (+1.110) | 1.87 (-0.377) | 6.82 vs `#000000` (+1.274) | 0.140 waypoint (+0.395) | 10.0 vs `#9553A4` (+0.665) | 0.353 motorway (+1.356) | b |
| night | 3 | `#F208C3` | 0.650 / 0.280 / 340.0 | `#000000` | 0.331 Topo `#2E5CFA` (+1.208) | 1.87 (-0.377) | 5.60 vs `#000000` (+0.866) | 0.129 photo (+0.293) | 20.0 vs `#9553A4` (+0.333) | 0.304 motorway (+1.024) | b |

#### Photo

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#3B6EA5` | 0.528 / 0.103 / 251.9 | `#FFFFFF` | 0.196 Topo `#8A7833` (+0.308) | 1.15 (-0.617) | 5.30 vs `#FFFFFF` (+0.768) | 0.107 selected_ring (+0.071) | 113.0 vs `#E8046D` (-2.767) | 0.285 motorway (+0.900) | b, e |
| day | 1 | `#C1154F` | 0.525 / 0.200 / 10.2 | `#FFFFFF` | 0.218 Topo `#8A7833` (+0.452) | 1.15 (-0.617) | 6.02 vs `#FFFFFF` (+1.007) | 0.152 find_pin (+0.524) | 5.3 vs `#E8046D` (+0.825) | 0.246 motorway (+0.641) | b |
| day | 2 | `#D32E5D` | 0.575 / 0.200 / 10.2 | `#FFFFFF` | 0.212 Topo `#8A7833` (+0.413) | 1.15 (-0.617) | 4.86 vs `#FFFFFF` (+0.621) | 0.135 find_pin (+0.349) | 5.3 vs `#E8046D` (+0.825) | 0.201 motorway (+0.338) | b |
| day | 3 | `#F41B66` | 0.625 / 0.240 / 10.0 | `#FFFFFF` | 0.240 Topo `#A78A41` (+0.601) | 1.15 (-0.617) | 4.04 vs `#FFFFFF` (+0.347) | 0.133 find_pin (+0.328) | 5.1 vs `#E8046D` (+0.831) | 0.185 motorway (+0.234) | b |
| night | today | `#3B6EA5` | 0.528 / 0.103 / 251.9 | `#000000` | 0.143 Topo `#2E5CFA` (-0.044) | 1.87 (-0.377) | 3.96 vs `#000000` (+0.320) | 0.164 centre_pin (+0.638) | 118.3 vs `#C1154F` (-2.942) | 0.224 motorway (+0.494) | a, b, e |
| night | 1 | `#E8046D` | 0.600 / 0.240 / 4.9 | `#000000` | 0.263 Topo `#A49047` (+0.756) | 1.87 (-0.377) | 4.67 vs `#000000` (+0.556) | 0.144 find_pin (+0.436) | 5.3 vs `#C1154F` (+0.825) | 0.222 motorway (+0.481) | b |
| night | 2 | `#DC3864` | 0.600 / 0.200 / 10.2 | `#000000` | 0.220 Topo `#A49047` (+0.467) | 1.87 (-0.377) | 4.79 vs `#000000` (+0.598) | 0.132 centre_pin (+0.320) | 0.0 vs `#C1154F` (+0.998) | 0.203 motorway (+0.351) | b |
| night | 3 | `#E21287` | 0.600 / 0.240 / 355.1 | `#000000` | 0.278 Topo `#A49047` (+0.853) | 1.87 (-0.377) | 4.67 vs `#000000` (+0.557) | 0.129 centre_pin (+0.293) | 15.0 vs `#C1154F` (+0.499) | 0.228 motorway (+0.516) | b |

#### Centre-pin picker

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#2E5339` | 0.406 / 0.061 / 152.4 | `#FFFFFF` | 0.146 Topo `#68751D` (-0.029) | 1.15 (-0.617) | 8.70 vs `#FFFFFF` (+1.901) | 0.112 selected_ring (+0.116) | 177.6 vs `#A656A0` (-4.922) | n/a | a, b, e |
| day | 1 | `#7D0D5B` | 0.399 / 0.160 / 344.8 | `#FFFFFF` | 0.274 Topo `#8A7833` (+0.823) | 1.15 (-0.617) | 10.09 vs `#FFFFFF` (+2.364) | 0.154 photo (+0.536) | 14.7 vs `#A656A0` (+0.510) | n/a | b |
| day | 2 | `#6F285C` | 0.400 / 0.120 / 339.5 | `#FFFFFF` | 0.250 Topo `#8A7833` (+0.669) | 1.15 (-0.617) | 9.84 vs `#FFFFFF` (+2.280) | 0.146 sighting_dot (+0.465) | 9.5 vs `#A656A0` (+0.685) | n/a | b |
| day | 3 | `#6A0644` | 0.349 / 0.140 / 350.2 | `#FFFFFF` | 0.291 Topo `#68751D` (+0.942) | 1.15 (-0.617) | 12.19 vs `#FFFFFF` (+3.064) | 0.138 sighting_dot (+0.377) | 20.2 vs `#A656A0` (+0.328) | n/a | b |
| night | today | `#A8CBA0` | 0.805 / 0.070 / 139.8 | `#000000` | 0.062 Topo `#C2D076` (-0.583) | 1.87 (-0.377) | 11.71 vs `#000000` (+2.905) | 0.073 selected_ring (-0.274) | 155.1 vs `#7D0D5B` (-4.169) | n/a | a, b, d, e |
| night | 1 | `#A656A0` | 0.575 / 0.141 / 330.1 | `#000000` | 0.218 Topo `#7F752A` (+0.453) | 1.87 (-0.377) | 4.46 vs `#000000` (+0.487) | 0.149 waypoint (+0.488) | 14.7 vs `#7D0D5B` (+0.510) | n/a | b |
| night | 2 | `#A840A2` | 0.550 / 0.179 / 330.0 | `#000000` | 0.229 Topo `#2E5CFA` (+0.525) | 1.87 (-0.377) | 3.92 vs `#000000` (+0.307) | 0.137 live_breadcrumb (+0.366) | 14.7 vs `#7D0D5B` (+0.509) | n/a | b |
| night | 3 | `#A84B84` | 0.550 / 0.139 / 344.9 | `#000000` | 0.202 Topo `#7F752A` (+0.344) | 1.87 (-0.377) | 4.00 vs `#000000` (+0.335) | 0.129 photo (+0.287) | 0.2 vs `#7D0D5B` (+0.994) | n/a | b |

#### Search centre

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#B33B3B` | 0.526 / 0.156 / 24.2 | `#FFFFFF` | 0.160 Topo `#8A7833` (+0.067) | 1.15 (-0.617) | 5.82 vs `#FFFFFF` (+0.941) | 0.062 photo (-0.381) | n/a (achromatic) | n/a | b, d |
| day | 1 | `#000000` | 0.000 / 0.000 / 0.0 | `#FFFFFF` | 0.545 Topo `#68751D` (+2.637) | 1.15 (-0.617) | 21.00 vs `#FFFFFF` (+6.000) | 0.150 offline_region (+0.496) | n/a (achromatic) | n/a | b |
| day | 2 | `#484848` | 0.402 / 0.000 / 89.9 | `#FFFFFF` | 0.173 Topo `#68751D` (+0.151) | 1.15 (-0.617) | 9.15 vs `#FFFFFF` (+2.049) | 0.093 sighting_dot (-0.074) | n/a (achromatic) | n/a | b, d |
| day | 3 | `#010101` | 0.067 / 0.000 / 89.9 | `#FFFFFF` | 0.480 Topo `#68751D` (+2.200) | 1.15 (-0.617) | 20.87 vs `#FFFFFF` (+5.958) | 0.082 offline_region (-0.176) | n/a (achromatic) | n/a | b, d |
| night | today | `#B33B3B` | 0.526 / 0.156 / 24.2 | `#000000` | 0.167 Topo `#7F752A` (+0.114) | 1.87 (-0.377) | 3.60 vs `#000000` (+0.202) | 0.129 photo (+0.292) | n/a (achromatic) | n/a | b |
| night | 1 | `#DEDEDE` | 0.901 / 0.000 / 89.9 | `#000000` | 0.137 Topo `#C2D076` (-0.086) | 1.87 (-0.377) | 15.61 vs `#000000` (+4.203) | 0.077 selected_ring (-0.228) | n/a (achromatic) | n/a | a, b, d |
| night | 2 | `#878787` | 0.623 / 0.000 / 89.9 | `#000000` | 0.102 Topo `#A49047` (-0.323) | 1.87 (-0.377) | 5.85 vs `#000000` (+0.949) | 0.149 centre_pin (+0.495) | n/a (achromatic) | n/a | a, b |
| night | 3 | `#9E9E9E` | 0.699 / 0.000 / 89.9 | `#000000` | 0.100 Topo `#BEA964` (-0.334) | 1.87 (-0.377) | 7.84 vs `#000000` (+1.613) | 0.124 selected_ring (+0.242) | n/a (achromatic) | n/a | a, b |

#### Offline region

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#2E5339` | 0.406 / 0.061 / 152.4 | `#FFFFFF` | 0.146 Topo `#68751D` (-0.029) | 1.15 (-0.617) | 8.70 vs `#FFFFFF` (+1.901) | 0.112 selected_ring (+0.116) | n/a (achromatic) | n/a | a, b |
| day | 1 | `#0B0B0B` | 0.150 / 0.000 / 89.9 | `#FFFFFF` | 0.400 Topo `#68751D` (+1.668) | 1.15 (-0.617) | 19.68 vs `#FFFFFF` (+5.561) | 0.150 search_centre (+0.496) | n/a (achromatic) | n/a | b |
| day | 2 | `#161616` | 0.200 / 0.000 / 89.9 | `#FFFFFF` | 0.352 Topo `#68751D` (+1.346) | 1.15 (-0.617) | 18.10 vs `#FFFFFF` (+5.032) | 0.115 sighting_dot (+0.154) | n/a (achromatic) | n/a | b |
| day | 3 | `#030303` | 0.097 / 0.000 / 89.9 | `#FFFFFF` | 0.451 Topo `#68751D` (+2.007) | 1.15 (-0.617) | 20.62 vs `#FFFFFF` (+5.875) | 0.097 search_centre (-0.031) | n/a (achromatic) | n/a | b, d |
| night | today | `#2E5339` | 0.406 / 0.061 / 152.4 | `#000000` | 0.022 Street `#3C512B` (-0.851) | 1.87 (-0.377) | 2.41 vs `#000000` (-0.196) | 0.163 sighting_dot (+0.626) | n/a (achromatic) | n/a | a, b, c |
| night | 1 | `#FFFFFF` | 1.000 / 0.000 / 89.9 | `#000000` | 0.208 Topo `#C2D076` (+0.387) | 1.87 (-0.377) | 21.00 vs `#000000` (+6.000) | 0.099 search_centre (-0.006) | n/a (achromatic) | n/a | b, d |
| night | 2 | `#878787` | 0.623 / 0.000 / 89.9 | `#000000` | 0.102 Topo `#A49047` (-0.323) | 1.87 (-0.377) | 5.85 vs `#000000` (+0.949) | 0.149 centre_pin (+0.495) | n/a (achromatic) | n/a | a, b |
| night | 3 | `#9E9E9E` | 0.699 / 0.000 / 89.9 | `#000000` | 0.100 Topo `#BEA964` (-0.334) | 1.87 (-0.377) | 7.84 vs `#000000` (+1.613) | 0.124 selected_ring (+0.242) | n/a (achromatic) | n/a | a, b |

#### Sighting dot

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#3B2E24` | 0.313 / 0.026 / 59.2 | `#FFFFFF` | 0.243 Topo `#68751D` (+0.619) | 1.15 (-0.617) | 13.11 vs `#FFFFFF` (+3.369) | 0.158 waypoint (+0.582) | 29.1 vs `#4F1B14` (+0.030) | n/a | b |
| day | 1 | `#3B2E24` | 0.313 / 0.026 / 59.2 | `#FFFFFF` | 0.243 Topo `#68751D` (+0.619) | 1.15 (-0.617) | 13.11 vs `#FFFFFF` (+3.369) | 0.158 waypoint (+0.582) | 29.1 vs `#4F1B14` (+0.030) | n/a | b |
| day | 2 | `#390603` | 0.225 / 0.080 / 29.9 | `#FFFFFF` | 0.337 Topo `#68751D` (+1.244) | 1.15 (-0.617) | 17.48 vs `#FFFFFF` (+4.827) | 0.110 offline_region (+0.103) | 0.2 vs `#4F1B14` (+0.994) | n/a | b |
| day | 3 | `#321A16` | 0.250 / 0.039 / 30.2 | `#FFFFFF` | 0.307 Topo `#68751D` (+1.049) | 1.15 (-0.617) | 16.22 vs `#FFFFFF` (+4.406) | 0.107 offline_region (+0.074) | 0.1 vs `#4F1B14` (+0.996) | n/a | b |
| night | today | `#3B2E24` | 0.313 / 0.026 / 59.2 | `#FFFFFF` | 0.047 Street `#1F342E` (-0.687) | 2.59 (-0.138) | 13.11 vs `#FFFFFF` (+3.369) | 0.299 centre_pin (+1.990) | 0.0 vs `#3B2E24` (+1.000) | n/a | a, b |
| night | 1 | `#4F1B14` | 0.301 / 0.080 / 30.1 | `#FFFFFF` | 0.093 Topo `#4B3A04` (-0.378) | 2.59 (-0.138) | 14.01 vs `#FFFFFF` (+3.669) | 0.300 centre_pin (+2.000) | 29.1 vs `#3B2E24` (+0.030) | n/a | a, b |
| night | 2 | `#7C433A` | 0.450 / 0.081 / 30.0 | `#FFFFFF` | 0.093 Topo `#59490A` (-0.381) | 2.59 (-0.138) | 7.72 vs `#FFFFFF` (+1.573) | 0.175 centre_pin (+0.750) | 29.2 vs `#3B2E24` (+0.027) | n/a | a, b |
| night | 3 | `#250302` | 0.174 / 0.061 / 28.7 | `#FFFFFF` | 0.090 Street `#22201C` (-0.398) | 2.59 (-0.138) | 19.22 vs `#FFFFFF` (+5.407) | 0.419 centre_pin (+3.192) | 30.5 vs `#3B2E24` (-0.017) | n/a | a, b, e |

#### Selected ring

| Mode | Rank | Fill | Oklab L / C / h | Casing | (a) min dE, worst cluster | (b) min contrast | (c) contrast vs | (d) min dE, nearest role | (e) dh vs other mode | (k) min dE, worst | Fails |
|---|---|---|---|---|---|---|---|---|---|---|---|
| day | today | `#2196F3` | 0.658 / 0.169 / 248.8 | `#2196F3` | 0.232 Street `#AED4DD` (+0.546) | 1.08 (-0.639) | 4.20 vs `#3B2E24` (+0.398) | 0.210 planned_trip (+1.105) | n/a (achromatic) | n/a | b |
| day | 1 | `#636363` | 0.500 / 0.000 / 89.9 | `#636363` | 0.116 Topo `#68751D` (-0.227) | 1.78 (-0.408) | 2.18 vs `#3B2E24` (-0.273) | 0.149 planned_trip (+0.492) | n/a (achromatic) | n/a | a, b, c |
| day | 2 | `#979797` | 0.676 / 0.000 / 89.9 | `#979797` | 0.103 Topo `#918E47` (-0.315) | 1.16 (-0.614) | 4.49 vs `#3B2E24` (+0.496) | 0.188 planned_trip (+0.879) | n/a (achromatic) | n/a | a, b |
| day | 3 | `#717171` | 0.549 / 0.000 / 89.9 | `#717171` | 0.095 Topo `#8A7833` (-0.366) | 1.44 (-0.519) | 2.69 vs `#3B2E24` (-0.105) | 0.140 planned_trip (+0.401) | n/a (achromatic) | n/a | a, b, c |
| night | today | `#2196F3` | 0.658 / 0.169 / 248.8 | `#2196F3` | 0.143 Topo `#2E5CFA` (-0.045) | 1.21 (-0.597) | 4.48 vs `#4F1B14` (+0.494) | 0.170 waypoint (+0.698) | n/a (achromatic) | n/a | a, b |
| night | 1 | `#C5C5C5` | 0.823 / 0.000 / 89.9 | `#C5C5C5` | 0.116 Topo `#C2D076` (-0.228) | 1.50 (-0.501) | 8.12 vs `#4F1B14` (+1.705) | 0.077 search_centre (-0.228) | n/a (achromatic) | n/a | a, b, d |
| night | 2 | `#878787` | 0.623 / 0.000 / 89.9 | `#878787` | 0.102 Topo `#A49047` (-0.323) | 1.39 (-0.537) | 3.90 vs `#4F1B14` (+0.300) | 0.149 centre_pin (+0.495) | n/a (achromatic) | n/a | a, b |
| night | 3 | `#9E9E9E` | 0.699 / 0.000 / 89.9 | `#9E9E9E` | 0.100 Topo `#BEA964` (-0.334) | 1.04 (-0.655) | 5.23 vs `#4F1B14` (+0.743) | 0.180 waypoint (+0.798) | n/a (achromatic) | n/a | a, b |

**Roles that cannot meet every constraint** (rank 1; (b) fails for all, section 4):
- **Search centre, night** `#DEDEDE`: fails (a) at 0.137 against Topo night `#C2D076`, where 0.15 is needed (−0.087), and
  (d) at 0.077 against the selected ring (−0.228). A light neutral sits near V1's lightest green and near the
  light-grey ring.
- **Offline region, night** `#FFFFFF`: fails (d) at 0.099 against the search centre (−0.006).
- **Sighting dot, night** `#4F1B14`: fails (a) at 0.093 against Topo night `#4B3A04` (−0.378). Every dark fill
  lies among V1's dark forest and contour clusters. The best (a) over all 506 colours in the dark pool is 0.093.
- **Selected ring, day** `#636363`: fails (a) at 0.116 (−0.227) and (c) at 2.18 against the dot (−0.273). None
  of the pool's 39 neutrals passes both (a) and (c) against today's dot `#3B2E24`. At night 4 of the 39 do. The rank-1
  ring is not one of them: the search ranks by the worst margin over all the constraints, (d) included,
  and why those four rank lower was not traced.
- **Selected ring, night** `#C5C5C5`: fails (a) at 0.116 against Topo night `#C2D076` (−0.228) and (d)
  at 0.077 against the search centre.

Today's colours, scored the same way, fail far more: waypoint (a, c, e, k by day), kept track (a, e, k), find
pin at night (a, c, e, k), and so on, as the "today" rows show.

**Per-cluster (a) for the rank-1 fills.** The dispatch asks for (a) per cluster. The tables above give each
candidate's minimum and which cluster it is; the matrix below gives every cluster for the rank-1 fills.

**Day**, rank-1 fills, Oklab dE to each ground cluster of 0.5% or more (threshold 0.15; below it in bold)

| Cluster | Waypoint pin `#350560` | Kept track `#A122F8` | Live breadcrumb `#650BB1` | Find pin `#DA02AF` | Planned trip `#9553A4` | Photo `#C1154F` | Centre-pin picker `#7D0D5B` | Search centre `#000000` | Offline region `#0B0B0B` | Sighting dot `#3B2E24` | Selected ring `#636363` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Topo `#CFE193` 21.62% | 0.649 | 0.488 | 0.556 | 0.442 | 0.405 | 0.434 | 0.537 | 0.884 | 0.736 | 0.573 | 0.392 |
| Topo `#E4F7AF` 14.81% | 0.709 | 0.527 | 0.608 | 0.483 | 0.457 | 0.488 | 0.595 | 0.950 | 0.801 | 0.638 | 0.455 |
| Topo `#BBCD7C` 14.63% | 0.594 | 0.456 | 0.510 | 0.409 | 0.359 | 0.387 | 0.484 | 0.823 | 0.675 | 0.513 | 0.334 |
| Topo `#A8BA66` 11.35% | 0.542 | 0.431 | 0.468 | 0.385 | 0.321 | 0.347 | 0.436 | 0.764 | 0.617 | 0.455 | 0.280 |
| Topo `#96A34F` 9.66% | 0.481 | 0.405 | 0.421 | 0.359 | 0.280 | 0.301 | 0.379 | 0.696 | 0.548 | 0.387 | 0.217 |
| Topo `#E7D899` 4.97% | 0.642 | 0.468 | 0.543 | 0.414 | 0.389 | 0.413 | 0.522 | 0.884 | 0.735 | 0.571 | 0.389 |
| Topo `#7D8B35` 3.59% | 0.416 | 0.393 | 0.379 | 0.351 | 0.254 | 0.270 | 0.325 | 0.618 | 0.471 | 0.311 | 0.155 |
| Topo `#68751D` 2.09% | 0.359 | 0.392 | 0.348 | 0.356 | 0.247 | 0.256 | 0.283 | 0.545 | 0.400 | 0.243 | **0.116** |
| Topo `#F6F9C5` 1.48% | 0.721 | 0.523 | 0.613 | 0.479 | 0.462 | 0.495 | 0.604 | 0.969 | 0.820 | 0.657 | 0.472 |
| Topo `#FBFBFA` 1.13% | 0.725 | 0.500 | 0.605 | 0.467 | 0.459 | 0.504 | 0.610 | 0.988 | 0.838 | 0.676 | 0.488 |
| Topo `#F0E7B1` 1.08% | 0.678 | 0.490 | 0.574 | 0.441 | 0.421 | 0.450 | 0.560 | 0.925 | 0.776 | 0.612 | 0.428 |
| Topo `#CBB76B` 0.94% | 0.555 | 0.422 | 0.473 | 0.363 | 0.318 | 0.334 | 0.437 | 0.786 | 0.638 | 0.474 | 0.297 |
| Topo `#DDC682` 0.93% | 0.598 | 0.441 | 0.506 | 0.382 | 0.350 | 0.369 | 0.478 | 0.836 | 0.687 | 0.523 | 0.343 |
| Topo `#8A7833` 0.70% | 0.374 | 0.362 | 0.340 | 0.310 | 0.216 | 0.218 | 0.274 | 0.582 | 0.435 | 0.272 | **0.118** |
| Topo `#B1A961` 0.66% | 0.505 | 0.398 | 0.432 | 0.344 | 0.282 | 0.301 | 0.394 | 0.732 | 0.584 | 0.420 | 0.244 |
| Topo `#A78A41` 0.61% | 0.434 | 0.370 | 0.379 | 0.308 | 0.235 | 0.236 | 0.321 | 0.653 | 0.505 | 0.342 | 0.176 |
| Topo `#D2A65F` 0.56% | 0.526 | 0.399 | 0.446 | 0.329 | 0.289 | 0.293 | 0.402 | 0.758 | 0.610 | 0.445 | 0.271 |
| Topo `#E5E9E1` 0.53% | 0.670 | 0.459 | 0.554 | 0.425 | 0.407 | 0.453 | 0.556 | 0.929 | 0.779 | 0.617 | 0.429 |
| Topo `#918E47` 0.52% | 0.426 | 0.374 | 0.375 | 0.326 | 0.239 | 0.255 | 0.324 | 0.640 | 0.493 | 0.330 | 0.163 |
| Street `#ADD19E` 87.02% | 0.587 | 0.435 | 0.495 | 0.401 | 0.348 | 0.391 | 0.482 | 0.825 | 0.676 | 0.515 | 0.331 |
| Street `#99BA86` 3.50% | 0.523 | 0.401 | 0.442 | 0.367 | 0.297 | 0.340 | 0.422 | 0.755 | 0.607 | 0.445 | 0.264 |
| Street `#AED4DD` 2.34% | 0.587 | 0.393 | 0.475 | 0.378 | 0.334 | 0.400 | 0.485 | 0.847 | 0.697 | 0.537 | 0.348 |
| Street `#DCE2DC` 1.36% | 0.648 | 0.441 | 0.533 | 0.408 | 0.386 | 0.434 | 0.535 | 0.907 | 0.757 | 0.594 | 0.407 |
| Street `#C6D9B3` 1.34% | 0.616 | 0.441 | 0.515 | 0.404 | 0.366 | 0.408 | 0.506 | 0.863 | 0.714 | 0.551 | 0.365 |
| Street `#FCFCFB` 0.80% | 0.728 | 0.502 | 0.608 | 0.470 | 0.462 | 0.507 | 0.613 | 0.991 | 0.841 | 0.679 | 0.491 |
| Street `#C6DCD3` 0.58% | 0.620 | 0.425 | 0.510 | 0.397 | 0.363 | 0.417 | 0.511 | 0.876 | 0.727 | 0.565 | 0.377 |

**Night**, rank-1 fills, Oklab dE to each ground cluster of 0.5% or more (threshold 0.15; below it in bold)

| Cluster | Waypoint pin `#B97DF7` | Kept track `#EEA7FE` | Live breadcrumb `#B228F8` | Find pin `#F96FAC` | Planned trip `#FA01DD` | Photo `#E8046D` | Centre-pin picker `#A656A0` | Search centre `#DEDEDE` | Offline region `#FFFFFF` | Sighting dot `#4F1B14` | Selected ring `#C5C5C5` |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Topo `#4E6012` 17.38% | 0.372 | 0.438 | 0.406 | 0.368 | 0.446 | 0.332 | 0.264 | 0.454 | 0.551 | 0.205 | 0.379 |
| Topo `#627524` 14.81% | 0.335 | 0.384 | 0.393 | 0.324 | 0.420 | 0.312 | 0.246 | 0.386 | 0.482 | 0.266 | 0.313 |
| Topo `#334801` 13.95% | 0.429 | 0.511 | 0.438 | 0.435 | 0.491 | 0.377 | 0.308 | 0.539 | 0.637 | **0.147** | 0.463 |
| Topo `#798936` 10.51% | 0.307 | 0.334 | 0.388 | 0.287 | 0.401 | 0.302 | 0.244 | 0.321 | 0.415 | 0.327 | 0.250 |
| Topo `#879A45` 5.90% | 0.297 | 0.303 | 0.396 | 0.272 | 0.399 | 0.310 | 0.258 | 0.273 | 0.365 | 0.378 | 0.205 |
| Topo `#9CA754` 5.42% | 0.287 | 0.273 | 0.399 | 0.254 | 0.391 | 0.313 | 0.270 | 0.227 | 0.318 | 0.420 | 0.163 |
| Topo `#59490A` 3.86% | 0.384 | 0.462 | 0.395 | 0.376 | 0.434 | 0.314 | 0.256 | 0.496 | 0.595 | **0.137** | 0.420 |
| Topo `#465208` 3.05% | 0.396 | 0.471 | 0.415 | 0.395 | 0.461 | 0.345 | 0.278 | 0.496 | 0.594 | 0.166 | 0.420 |
| Topo `#2A2D01` 2.57% | 0.481 | 0.576 | 0.464 | 0.491 | 0.524 | 0.413 | 0.350 | 0.619 | 0.718 | **0.098** | 0.542 |
| Topo `#AEBC65` 2.22% | 0.299 | 0.255 | 0.424 | 0.261 | 0.405 | 0.343 | 0.309 | 0.176 | 0.261 | 0.483 | **0.127** |
| Topo `#C2D076` 2.09% | 0.321 | 0.250 | 0.454 | 0.279 | 0.425 | 0.378 | 0.352 | **0.137** | 0.208 | 0.543 | **0.116** |
| Topo `#666319` 1.71% | 0.344 | 0.405 | 0.384 | 0.331 | 0.413 | 0.297 | 0.235 | 0.423 | 0.520 | 0.215 | 0.348 |
| Topo `#323701` 1.62% | 0.455 | 0.545 | 0.447 | 0.462 | 0.504 | 0.390 | 0.326 | 0.584 | 0.683 | **0.105** | 0.508 |
| Topo `#4B3A04` 1.37% | 0.419 | 0.506 | 0.414 | 0.417 | 0.462 | 0.343 | 0.287 | 0.548 | 0.647 | **0.093** | 0.471 |
| Topo `#A49047` 1.28% | 0.271 | 0.276 | 0.367 | 0.228 | 0.357 | 0.263 | 0.226 | 0.263 | 0.357 | 0.367 | 0.193 |
| Topo `#7F752A` 1.06% | 0.307 | 0.349 | 0.369 | 0.282 | 0.384 | 0.272 | 0.218 | 0.357 | 0.454 | 0.275 | 0.284 |
| Topo `#020302` 0.88% | 0.636 | 0.747 | 0.582 | 0.658 | 0.658 | 0.563 | 0.504 | 0.808 | 0.907 | 0.224 | 0.731 |
| Topo `#BEA964` 0.63% | 0.265 | 0.231 | 0.384 | 0.213 | 0.358 | 0.289 | 0.263 | 0.187 | 0.278 | 0.446 | **0.126** |
| Topo `#2E5CFA` 0.55% | 0.215 | 0.338 | 0.205 | 0.345 | 0.335 | 0.368 | 0.221 | 0.426 | 0.511 | 0.383 | 0.365 |
| Street `#446835` 85.99% | 0.348 | 0.416 | 0.387 | 0.355 | 0.433 | 0.330 | 0.248 | 0.433 | 0.531 | 0.221 | 0.358 |
| Street `#5C7D4A` 3.50% | 0.303 | 0.354 | 0.367 | 0.304 | 0.400 | 0.305 | 0.225 | 0.360 | 0.457 | 0.282 | 0.285 |
| Street `#173E48` 2.14% | 0.405 | 0.509 | 0.386 | 0.441 | 0.466 | 0.382 | 0.287 | 0.562 | 0.661 | **0.133** | 0.485 |
| Street `#3C512B` 1.81% | 0.382 | 0.466 | 0.395 | 0.393 | 0.450 | 0.343 | 0.265 | 0.498 | 0.597 | 0.155 | 0.421 |
| Street `#1F342E` 0.99% | 0.444 | 0.546 | 0.421 | 0.469 | 0.494 | 0.399 | 0.318 | 0.596 | 0.696 | **0.105** | 0.519 |
| Street `#22201C` 0.71% | 0.493 | 0.599 | 0.456 | 0.513 | 0.527 | 0.428 | 0.361 | 0.656 | 0.756 | **0.095** | 0.579 |
| Street `#537342` 0.59% | 0.319 | 0.379 | 0.371 | 0.323 | 0.410 | 0.311 | 0.229 | 0.392 | 0.489 | 0.252 | 0.316 |

## 6. Colour-vision deficiency (report only)

These use Machado 2009 at severity 1.0, applied to the rank-1 fills. The pairs listed fall under ΔE 0.10 after simulation,
with their unsimulated ΔE in brackets. The neutral pairs are already under 0.10 before simulation, because they
fail (d) (section 5).

- **Deuteranopia, day**: find_pin / planned_trip 0.059 (from 0.146), planned_trip / selected_ring 0.096 (from 0.149), photo / selected_ring 0.066 (from 0.202).
- **Deuteranopia, night**: waypoint / planned_trip 0.043 (from 0.172), kept_track / selected_ring 0.083 (from 0.140), find_pin / selected_ring 0.092 (from 0.205), search_centre / offline_region 0.099 (from 0.099), search_centre / selected_ring 0.077 (from 0.077).
- **Protanopia, day**: waypoint / centre_pin 0.069 (from 0.169), live_breadcrumb / find_pin 0.080 (from 0.243), find_pin / planned_trip 0.042 (from 0.146), photo / selected_ring 0.079 (from 0.202).
- **Protanopia, night**: live_breadcrumb / planned_trip 0.044 (from 0.148), photo / centre_pin 0.084 (from 0.150), search_centre / offline_region 0.099 (from 0.099), search_centre / selected_ring 0.077 (from 0.077).

User-mark pairs under simulation (rank-1 fills; dE normal -> simulated):

| Pair | deuteranopia day | deuteranopia night | protanopia day | protanopia night |
|---|---:|---:|---:|---:|
| Waypoint pin / Kept track | 0.330 -> 0.311 | 0.137 -> 0.146 | 0.330 -> 0.312 | 0.137 -> 0.130 |
| Waypoint pin / Live breadcrumb | 0.169 -> 0.164 | 0.144 -> 0.120 | 0.169 -> 0.169 | 0.144 -> 0.120 |
| Waypoint pin / Find pin | 0.369 -> 0.329 | 0.153 -> 0.149 | 0.369 -> 0.222 | 0.153 -> 0.118 |
| Waypoint pin / Planned trip | 0.279 -> 0.271 | **0.172 -> 0.043** | 0.279 -> 0.241 | 0.172 -> 0.112 |
| Waypoint pin / Photo | 0.320 -> 0.310 | 0.239 -> 0.200 | 0.320 -> 0.180 | 0.239 -> 0.242 |
| Kept track / Live breadcrumb | 0.162 -> 0.148 | 0.268 -> 0.266 | 0.162 -> 0.144 | 0.268 -> 0.250 |
| Kept track / Find pin | 0.166 -> 0.154 | 0.144 -> 0.121 | 0.166 -> 0.114 | 0.144 -> 0.155 |
| Kept track / Planned trip | 0.151 -> 0.137 | 0.226 -> 0.148 | 0.151 -> 0.135 | 0.226 -> 0.240 |
| Kept track / Photo | 0.272 -> 0.271 | 0.284 -> 0.241 | 0.272 -> 0.273 | 0.284 -> 0.326 |
| Live breadcrumb / Find pin | 0.243 -> 0.208 | 0.235 -> 0.251 | **0.243 -> 0.080** | 0.235 -> 0.193 |
| Live breadcrumb / Planned trip | 0.161 -> 0.152 | 0.148 -> 0.136 | 0.161 -> 0.117 | **0.148 -> 0.044** |
| Live breadcrumb / Photo | 0.262 -> 0.256 | 0.242 -> 0.250 | 0.262 -> 0.182 | 0.242 -> 0.222 |
| Find pin / Planned trip | **0.146 -> 0.059** | 0.153 -> 0.118 | **0.146 -> 0.042** | 0.153 -> 0.160 |
| Find pin / Photo | 0.152 -> 0.137 | 0.144 -> 0.125 | 0.152 -> 0.160 | 0.144 -> 0.173 |
| Planned trip / Photo | 0.156 -> 0.137 | 0.169 -> 0.159 | 0.156 -> 0.144 | 0.169 -> 0.183 |

Among user marks, the collapsed pairs are find pin / planned trip (both deficiencies, by day), waypoint /
planned trip (deuteranopia, night), live breadcrumb / find pin (protanopia, day) and live breadcrumb /
planned trip (protanopia, night). Each of these pairs differs in shape: pin against diamond, line against pin, line against
diamond. Shape and lightness are what separate them, as the direction intends. This is reported, not scored.

## 7. Predictions against outcome

**Planner** (dispatch `2026-09-26-39`):
1. *Every user-mark role has at least one candidate in the purple–magenta band that passes (a)–(e) on Topo
   in both modes.* **Not met, because of (b) alone.** Every candidate fails (b) (section 4). Excluding (b),
   all six user marks pass (a), (c), (d) and (e) in both modes, on both basemaps at once.
2. *Street Night's pink-red roads are the closest ground to the magenta end, so some roles pass there with
   the smallest margin, or fail.* **Not met.** The pink-red references are not in the sample (carto source colours). The
   carto motorway is the nearest of the three road references for every user mark in both modes, but (k) is
   never a rank-1 candidate's smallest margin and never fails. Every rank-1 fill's nearest ground cluster
   is on Topo, not Street.
3. *The neutral system marks pass easily.* **Not met.** By day, search centre (black) and offline region
   (near-black) pass everything but (b), with margins of +0.496. At night both fail (d), search centre also fails (a), and the
   selected ring fails (a) in both modes (section 5).
4. *At least one pair of user marks collapses under deuteranopia simulation; lightness and shape separate
   them.* **Met:** find pin / planned trip by day, waypoint / planned trip at night.
5. *(continuation) The blue-family share of the ground is under 10% in V1 night and over 40% in V2, for both
   basemaps.* **V1 met, V2 not met.** Tiles: V1 1.84% and 2.32%; V2 21.92% (Topo) and 1.18% (Street). On the
   captures, V1 is 10.26% (Topo) and 9.15% (Street) of all pixels, which is water in view, the same as by day. V2's colour
   sits at 280–320°, above the band (section 1).

**Coder, mechanism** (intent `2026-09-27-05` and continuation `2026-09-27-06`):
- The V1 formula as predicted, `clamp(c + 1 − 2m)`: **met**, from the cited source, with a residual of 0.25% / 0.20%.
- (i) (b) fails structurally for the directed casing polarity, independent of the fill: **met.**
- (ii) (a) and (e) pass easily in the 300–10° band: **met** for user marks. The smallest (a) is 0.205 (live breadcrumb, night).
- (iii) (c) and (d) bind among user marks, with (d) tightest between the dark system marks and the sighting dot: **partly met.**
  (d) is the binding margin for 7 of the 12 user-mark rank-1 cells, but the tightest (d) is between light
  neutrals at night, not dark ones by day. (a) binds four cells, (e) binds one, and (c) binds none.
- (iv) Magenta-band user marks at similar lightness collapse under deuteranopia or protanopia: **met** (section 6).
- (v) OSM night's primary road is farther from magenta than the pink-red: **met.** For every user mark the sample's
  primary road is farther than the carto motorway.
- (continuation) V1's blue share is about the day share, under 10% on the tiles: **met.** V2's is substantial on Topo, and on Street
  well under 40%, possibly under 10%, because V2's violet falls outside 200–280°: **met** (21.92%; 1.18%).

## 8. What could not be determined

- **The sample is forest-heavy.** Tillamook State Forest, 36 tiles: roads, towns, open water and
  settlements are under-represented. Ground clusters under 0.5% (most roads, all water on OSM, and Topo's blue
  water at 0.42% by day) enter (a) only where they cross 0.5%: Topo night water `#2E5CFA` is 0.55%. Candidates
  may sit closer to urban or waterside ground than any figure here shows.
- **Pink-red roads are not measured.** The motorway and trunk colours are taken from the carto style source, not from
  the tiles the app loads.
- **On-device appearance** is not tested. The board is a rendering of the model, not a capture. Anti-aliasing,
  display gamma, the night dimming of other UI, and MapLibre's marker blending are not modelled.
- **Casing polarity.** Whether the owner prefers to relax (b), to change the casing rule (for example a
  two-tone or ground-adaptive casing), or to accept a percentile shortfall is not decided here.
- **The blue-share figures** depend on the source: tiles give V1 about 2%, and the captures give about 10% of all pixels,
  because of the water in view. Neither is a measure of the areas the owner actually uses at night.
- **The model's open choices** (section 2) were made by a coder, not by the owner or the planner. A different
  choice on any of them changes the candidates.

## 9. Provenance

- **Reused from the stopped coder** of intent `2026-09-27-05` (files in `~/Zynergy/forager-swatch-work/`,
  written before the stop): `colour.py`, `ground.py`, `candidates.py`, `board.py`, `v1_check.py`, and the
  MapLibre and Machado source copies in `src/`. This build did not take their outputs on trust:
  - `v1_check.py` was rerun, with identical output;
  - `ground.py` was rerun, with identical `ground.json` and output;
  - `candidates.py` was rerun from scratch with its seed cache moved aside, and gave a byte-identical
    `candidates.json` and identical pick;
  - `board.py` was rerun and gave a byte-identical PNG;
  - the shader copies were checked byte-for-byte against the upstream tag.
- **Added by this build:** `blue_share.py` (section 1), a hue histogram (inline, output in `blue_share_hue.out`),
  and `tables.py` (the tables in this doc).
- **sha256:** colour.py `8119a0da…b212`, ground.py `045b8025…4a83`, candidates.py `f4726cff…7d56`,
  board.py `d0016f84…b26e`, blue_share.py `2c6715cd…b94f`, tables.py `60ffed26…bf42`, v1_check.py
  `59828d23…7493`, swatch-board.png `e2efd691…e85`.
- Nothing from those directories is committed except `swatch-board.png`.
