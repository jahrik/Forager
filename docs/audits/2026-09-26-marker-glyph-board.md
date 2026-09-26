# Marker glyph board: every map marker in its own silhouette, in the owner's palette

**Date:** 2026-09-26 (UTC; record IDs dated 2026-09-27, as in the swatch board).
**Dispatch:** `prompts/preserved/2026-09-26-46.md` (Type build), with the planner's ruling at
planner-log line 2231, quoted in `RECORD.md` intent `2026-09-27-18`.
**Branch:** `marker-glyphs`, cut from `origin/pre-main` `3428a92` (merge of #131).
**What this is:** design evidence only. It draws the shapes the dispatch specifies, in the colours the
owner picked, and measures them. It changes no app code, test or colour, and chooses no colour.
`git diff origin/pre-main -- app/` is empty.
**Board:** [`assets/2026-09-26-marker-glyph-board/glyph-board.png`](assets/2026-09-26-marker-glyph-board/glyph-board.png),
2594 x 4196 px, 1,768,836 bytes.

## Summary

- **The board.** It has ten rows: waypoint, find, planned trip, photo, sighting dot (both states),
  search centre, centre-pin picker, offline region, kept track and live breadcrumb. Its columns are
  Topo Day, Topo Night, Street Day and Street Night, over the swatch board's tile crop with its V1
  night transform, and a Silhouette column. The Silhouette column draws every glyph in solid black
  on white, at on-screen size and at 4x, with the anchor marked by a red cross at 4x.
- **Silhouette distinctness misses the 0.6 target. This is reported, and no shape was changed.**
  - Under the headline method (the planner's), 8 of the 15 pairs that count toward the target
    exceed 0.6.
  - The highest pair is **sighting dot / search centre at 0.913**. Next come photo / sighting dot
    at 0.834 and **waypoint / find at 0.819**.
  - Anchor-aligned, 4 of the 15 exceed 0.6: those three, plus photo / search centre at 0.762.
  - The main reason is that IoU scores compact outlines as alike. A disc, a rounded square and a
    ring whose middle is filled by a crosshair are all compact. Section 3 has the full table.
- **Colour vision (Machado 2009).**
  - Under deuteranopia, 1 pair collapses by day (ΔE < 0.10), beside the one pair that already sits
    under 0.10 without simulation. At night 4 pairs collapse, again beside the one already under.
    Protanopia collapses 3 pairs by day and 3 at night.
  - Every collapsed pair differs in shape.
  - Two of them are also above the 0.6 IoU target, both at night: **photo / sighting dot** (ΔE
    0.049 under deuteranopia, IoU 0.834) and **find / sighting dot** (ΔE 0.092 under deuteranopia,
    0.050 under protanopia; IoU 0.749). For those, the silhouettes are close by the dispatch's own
    measure (section 4).
- **Sighting-dot contrast.**
  - The white ring reproduces the swatch board's dot-halo figures exactly, because constraint (b)
    depends only on the ring and the ground.
  - At night it passes 3:1 everywhere except on the lightest night ground, Topo P90: **2.586:1**.
    By day it passes only on Topo P10 (3.383:1).
  - The grey night fill does not enter (b). Where the grey fill does matter is against the ground
    it sits on (1.20–2.41:1 at 0.7 over night grounds). The selected blue ring against the grey
    fill is **1.076:1** at night.
  - Reported, with no colour changed (section 5).

## 1. Shapes, dimensions and anchors

Everything is hand-drawn as Paths/polygons, as Understory keeps marker shapes while `MaterialShapes` is
experimental (`docs/plans/understory-design-system.md:599`). On-screen px = dp x 2.8125 (S22,
`docs/audits/assets/2026-09-26-landscape-capture/r0-window-displays.txt`). Each glyph's "fill extent"
excludes casing; every glyph has a 1.5 dp casing (white by day, black by night) outside it unless
stated. Anchors are measured from the top-left of the fill extent.

| Glyph | Shape | Fill extent, dp | With casing, dp | With casing, px (1x) | Anchor, dp | Anchor, px |
|---|---|---|---|---|---|---|
| Waypoint | Teardrop pin as today's `waypointPinBitmap` (`SightingsMap.kt:1081`): head circle r 11 and a triangle from the head's equator to the tip; hollow ring 7 dp across (outer), 1.5 dp stroke, casing colour, on the head centre | 22 x 28 | 25 x 31 | 70.3 x 87.2 | tip (11, 28) | (30.9, 78.8) |
| Find | Mushroom: dome = upper half-ellipse rx 12, ry 14, flat underside at y 14; stem 8 dp wide, y 13.5–26, square foot | 24 x 26 | 27 x 29 | 75.9 x 81.6 | stem foot (12, 26) | (33.8, 73.1) |
| Planned trip | Flag: pole 3 dp wide (x 0–3, y 0–28); rectangular pennant x 3–20, y 0–12 | 20 x 28 | 23 x 31 | 64.7 x 87.2 | pole foot (1.5, 28) | (4.2, 78.8) |
| Photo | Rounded square, corner r 5 dp; camera in casing colour: body 14 x 10 dp (r 1.5), viewfinder bump 6 x 3 dp, lens a fill-colour disc r 3.2 dp | 22 x 22 | 25 x 25 | 70.3 x 70.3 | centre (11, 11) | (30.9, 30.9) |
| Sighting dot | Circle r 9, fill at 0.7; ring 1.5 dp centred on r (8.25–9.75) at 0.85, white unselected / `#2196F3` selected in every mode; no other casing | 19.5 x 19.5 (ring outer) | same | 54.8 x 54.8 | centre (9.75, 9.75) | (27.4, 27.4) |
| Search centre | Ring 16 dp across at the stroke centre line (r 8), stroke 2 dp; crosshair a plus at the centre, arms 3 dp from centre, 2 dp stroke; no fill; casing around ring and plus | 18 x 18 (ring outer) | 21 x 21 | 59.1 x 59.1 | centre (9, 9) | (25.3, 25.3) |
| Centre-pin picker | Hand-drawn approximation of Material `LocationOn` in a 40 dp box (24-unit grid: head circle (12, 9) r 7, tangents to the tip (12, 22), hole r 2.5); casing outside and a casing ring round the hole; the hole is transparent | 40 x 40 box (drawn 23.3 x 33.3) | drawn 26.35 x 36.35 | box 112.5; drawn 74.1 x 102.2 | box bottom-centre (20, 40) | (56.3, 112.5) |
| Offline region | Fill at 0.2 inside r; dashed outline 1.5 dp centred on r, dash 6 dp, gap 4 dp, butt ends, each dash cased 1.5 dp | area (drawn r 44) | — | — | none (an area) | — |
| Kept track | 6 px solid, round caps, cased 1.5 each side | line | 9 wide | 25.3 wide | none | — |
| Live breadcrumb | 6 px dashes (0.4, 1.0) x width, round caps, each cased 1.5 each side (the swatch board's convention) | line | 9 wide | 25.3 wide | none | — |

4x renders are the same geometry at 11.25 px/dp (for example, waypoint with casing 281 x 349 px).
Colours are the dispatch's table, unchanged: fill day / night, waypoint `#350560` / `#B97DF7`, find
`#DA02AF` / `#F96FAC`, planned trip `#9553A4` / `#FA01DD`, photo `#C1154F` / `#E8046D`, kept track
`#A122F8` / `#EEA7FE`, live breadcrumb `#650BB1` / `#B228F8`, centre-pin picker `#7D0D5B` / `#A656A0`,
search centre `#000000` / `#DEDEDE`, offline region `#0B0B0B` / `#FFFFFF`, sighting dot `#1F1F1F` /
`#8C8C8C`.

**Centre-pin anchor.** `CentrePinLocationPicker.kt:198-201` draws `LocationOn` in a 40 dp box, padded at the
bottom by half its size, so the coordinate is at the box's bottom edge. In this approximation the
drawn tip sits at 22/24 of the box, which is 3.3 dp above that edge. Whether the real `LocationOn`'s tip sits there
was not checked (section 7).

## 2. Choices made in this build

The dispatch left these open ("choose the simplest version") or they were method details. They are the coder's, not
the owner's or the planner's, except where marked.

1. **Flag pennant: rectangular**, flush with the pole's top, 17 x 12 dp. The swallowtail was the alternative.
2. **Mushroom:** a half-ellipse dome with a flat underside and a straight 8 dp stem, both chosen as
   the simplest. The cap is 14 dp tall and the stem 12.5 dp.
3. **Waypoint:** the ring's interior stays the pin's fill colour (a "hollow" ring, not a hole to the
   ground). The pin outline is today's `waypointPinBitmap` geometry, not a tangent teardrop.
4. **Camera:** hand-drawn, approximating Material `PhotoCamera`; the path data was not extracted
   (planner ruling). The body is 14 x 10 dp, the bump 6 x 3 dp, and the lens a fill-colour disc r 3.2 dp.
5. **Search centre:** "16 dp ring" is read as the stroke centre line (r 8, today's radius). The stroke
   is 2 dp (today's). "Short crosshair" is drawn as a plus at the centre with 3 dp arms, inside the
   ring. Ticks outside the ring were the alternative.
6. **Offline region:** dash 6 dp, gap 4 dp, stroke 1.5 dp (today's width). Drawn at r 44 dp.
7. **Sighting dot:** "9 px" and "1.5 px" are MapLibre style units, which are dp. The ring is centred on r,
   as the swatch board drew it (MapLibre actually strokes outside the radius, which would make
   the dot 1.5 dp wider). The ring keeps today's 0.85 opacity, since the dispatch gives none.
8. **Centre-pin picker** is on the board and in the IoU table **by the planner's ruling** (line 2231), as
   a hand-drawn `LocationOn` at 40 dp, in the new colours, with casing.
9. **Scale:** the crop and the glyphs are both drawn at 2.8125 px per dp (a 256 px raster tile at its own
   zoom is 256 dp). The crop uses NEAREST resampling, as the swatch board did at 2x. BILINEAR would be closer to
   MapLibre's default linear raster resampling, but it compresses much worse.
10. **Silhouette** = every painted pixel black, casing included, with transparent holes kept (the
    `LocationOn` hole and the search centre's gap). Fill opacity is ignored, so the sighting dot and
    the offline region become solid. For lines and the region, the 1x drawing is shown at half size
    beside a 4x window (18 x 24 dp) on the line or the rim; they have no anchor cross.
11. **The 1.5 dp casing** is built as the polygon grown by a 3 dp round-joined stroke.

## 3. Silhouette distinctness (IoU)

**Method.** Each point glyph's silhouette is rendered at 40 px/dp, binarised at alpha 128 and cropped to its
bounding box (casing included).
- **Headline (the planner's method, ruling at planner-log line 2231):** each silhouette is scaled uniformly so
  that its larger dimension is 512 px, then placed with its bounding-box centre at a common point.
- **Anchor-aligned:** the same scale, placed with its anchor at the common point.

IoU = |A ∩ B| / |A ∪ B| on the binary masks. Silhouettes are the same day and night (same geometry).

**Checks on the tool.** Each glyph against itself gives 1.000. Disc against rounded square (casing included) gives 0.834,
against 0.8338 computed analytically (π/4 / (1 − (4 − π)·0.26²)).

| Pair | Headline (bbox-centred) | Anchor-aligned | Note |
|---|---:|---:|---|
| Sighting dot / Search centre | **0.913** | **0.913** |  |
| Waypoint / Centre-pin picker | **0.858** | **0.659** | transient, never co-displayed |
| Photo / Sighting dot | **0.834** | **0.834** |  |
| Waypoint / Find | **0.819** | **0.818** |  |
| Photo / Search centre | **0.762** | **0.762** |  |
| Find / Sighting dot | **0.749** | 0.191 |  |
| Find / Centre-pin picker | **0.705** | 0.580 | transient, never co-displayed |
| Find / Search centre | **0.673** | 0.179 |  |
| Waypoint / Sighting dot | **0.658** | 0.176 |  |
| Find / Photo | **0.631** | 0.209 |  |
| Waypoint / Search centre | 0.572 | 0.168 |  |
| Sighting dot / Centre-pin picker | 0.565 | 0.121 | transient, never co-displayed |
| Waypoint / Photo | 0.549 | 0.178 |  |
| Waypoint / Planned trip | 0.521 | 0.441 |  |
| Search centre / Centre-pin picker | 0.487 | 0.119 | transient, never co-displayed |
| Planned trip / Sighting dot | 0.482 | 0.093 |  |
| Planned trip / Photo | 0.480 | 0.088 |  |
| Photo / Centre-pin picker | 0.471 | 0.120 | transient, never co-displayed |
| Find / Planned trip | 0.469 | 0.432 |  |
| Planned trip / Search centre | 0.450 | 0.091 |  |
| Planned trip / Centre-pin picker | 0.440 | 0.362 | transient, never co-displayed |

Bold marks a value above the 0.6 target. The table has 21 pairs; the 6 involving the centre-pin picker are excluded from the target by the ruling,
which leaves 15.

**Against the target: missed.**
- Headline: 8 of 15 pairs exceed 0.6. The highest is **sighting dot / search centre, 0.913**.
- Anchor-aligned: 4 of 15 exceed 0.6 (sighting dot / search centre 0.913, photo / sighting dot 0.834,
  waypoint / find 0.818, photo / search centre 0.762). A bottom anchor against a centre anchor pulls a
  pair apart by half a glyph, so anchor-aligned is low for every mixed-anchor pair.
- No shape was reshaped to pass.

**What drives the numbers.**
- **The centre-anchored trio (dot, search centre, photo) all overlap heavily.** Uniform scaling
  removes their real size difference (19.5, 21 and 25 dp). The search centre's hollow is only the
  gap between the crosshair's casing and the ring's casing (4.5–5.5 dp), so its silhouette is nearly a disc.
  IoU does not see what makes the three differ to the eye: a hollow ring, corners, the dot's
  smaller size.
- **Waypoint / find, 0.819:** the two glyphs have near-equal bounding boxes (25 x 31 and 27 x 29 dp). The pin's
  head and the mushroom's dome coincide over the top half, and the pin's tail and the stem both
  occupy the lower centre.
- **The flag** is the most distinct glyph. Its highest pair is 0.521, against the waypoint.
- **The centre-pin picker against the waypoint is 0.858**: both are teardrop pins with a round
  centre feature. By the ruling this is transient and never co-displayed.

## 4. Colour-vision deficiency

**Method.** Machado, Oliveira and Fernandes (2009), severity 1.0 deuteranopia and protanopia matrices,
applied in linear RGB. These are the swatch board's `colour.simulate`, unchanged.
- Every pair of the ten fills per mode is compared by Oklab ΔE. A pair "collapses" below 0.10, the
  swatch board's (d) threshold and the one its section 6 used.
- The whole board was also simulated for both deficiencies (`cvd-deuteranopia.png` and
  `cvd-protanopia.png`, outside the repo). A side-by-side visual check showed each simulation
  applied. It is not an extra measure.

Pairs under ΔE 0.10 (unsimulated → simulated), with the shapes that remain:

| Mode | Condition | Pair | ΔE | Told apart by shape? |
|---|---|---|---|---|
| Day | none | offline region / sighting dot | 0.090 | yes: dashed area vs dot (both near-black by design) |
| Day | deuteranopia | find / planned trip | 0.146 → 0.059 | yes: mushroom vs flag, IoU 0.469 |
| Day | deuteranopia | offline region / sighting dot | 0.090 → 0.090 | yes: area vs dot |
| Day | protanopia | waypoint / centre-pin picker | 0.169 → 0.069 | weakly: pin vs `LocationOn`, IoU 0.858; transient, never co-displayed |
| Day | protanopia | find / planned trip | 0.146 → 0.042 | yes: IoU 0.469 |
| Day | protanopia | find / live breadcrumb | 0.243 → 0.080 | yes: point vs line |
| Day | protanopia | offline region / sighting dot | 0.090 → 0.090 | yes: area vs dot |
| Night | none | search centre / offline region | 0.099 | yes: crosshair ring vs dashed area |
| Night | deuteranopia | waypoint / planned trip | 0.172 → 0.043 | yes: IoU 0.521 |
| Night | deuteranopia | find / sighting dot | 0.198 → 0.092 | **above target**: IoU 0.749 |
| Night | deuteranopia | photo / sighting dot | 0.243 → 0.049 | **above target**: IoU 0.834 |
| Night | deuteranopia | centre-pin picker / sighting dot | 0.156 → 0.092 | yes: IoU 0.565; transient, never co-displayed |
| Night | deuteranopia | search centre / offline region | 0.099 → 0.099 | yes: ring vs area |
| Night | protanopia | find / sighting dot | 0.198 → 0.050 | **above target**: IoU 0.749 |
| Night | protanopia | planned trip / live breadcrumb | 0.148 → 0.044 | yes: point vs line |
| Night | protanopia | photo / centre-pin picker | 0.150 → 0.084 | yes: IoU 0.471; transient, never co-displayed |
| Night | protanopia | search centre / offline region | 0.099 → 0.099 | yes: ring vs area |

**Pairs told apart by shape alone** under a simulation (collapsed by it, not before):
- Deuteranopia by day: find / planned trip.
- Deuteranopia at night: waypoint / planned trip and the transient centre-pin picker / sighting dot.
  Find / sighting dot and photo / sighting dot also collapse in colour, but their silhouettes are
  over the 0.6 target.
- Protanopia by day: find / planned trip, find / live breadcrumb and the transient waypoint /
  centre-pin picker (silhouette 0.858).
- Protanopia at night: planned trip / live breadcrumb and the transient photo / centre-pin picker.
  Find / sighting dot also collapses in colour, but its silhouette is over the target.

For the night pairs involving the sighting dot, the dot still differs in ways that are not
silhouette: its 0.7 fill, its white ring against the black casing of every other point glyph, and
its smaller real size (19.5 dp, which the normalised IoU removes). None of those was measured here as a
separator.

**The two sighting-dot states share one silhouette.** They differ only by ring colour. White against
`#2196F3` is ΔE 0.381, 0.399 under deuteranopia and 0.336 under protanopia, so blue survives both.
Against the night grey fill, though, the blue ring is ΔE 0.170 (0.170 / 0.156 simulated) and
1.076:1 in luminance (section 5).

## 5. Sighting-dot contrast

The (b) method is the swatch board's: WCAG 2.1 contrast of the ring against the ground's luminance Y at
P10/P50/P90. It uses the swatch board's `ground.json`: all 18 sample tiles per basemap, day pixels and
their V1 transform. The ring is taken as opaque.
- **As a check on the data**, the white-ring column below equals the swatch board's dot-halo column to the
  third decimal on all twelve rows.
- **Supplementary columns** give the fill at 0.7 over the ground against the ring, and against the ground. Each
  composites the fill over a neutral grey of that ground's Y, in sRGB. That is an approximation: the real ground
  is coloured.

Fill Y: day `#1F1F1F` 0.0137, night `#8C8C8C` 0.2623. Ring Y: white 1.0, blue 0.2861.
Opaque fill against ring: day, white 16.483 and blue 5.276. Night, white 3.363 and blue **1.076**.

| Mode | Ground | Y | White ring vs ground (b) | Blue ring vs ground (b) | Fill@0.7 vs white ring | Fill@0.7 vs blue ring | Fill@0.7 vs ground |
|---|---|---:|---:|---:|---:|---:|---:|
| Day | Topo P10 | 0.2604 | 3.383 | 1.083 | 10.368 | 3.319 | 3.065 |
| Day | Topo P50 | 0.5913 | 1.637 | 1.908 | 7.814 | 2.501 | 4.772 |
| Day | Topo P90 | 0.8641 | 1.149 | 2.720 | 6.585 | 2.108 | 5.732 |
| Day | Street P10/P50/P90 | 0.5695 | 1.695 | 1.843 | 7.938 | 2.541 | 4.683 |
| Night | Topo P10 | 0.0434 | 11.242 | 3.598 | 4.674 | 1.496 | 2.405 |
| Night | Topo P50 | 0.1295 | 5.850 | 1.872 | 3.949 | 1.264 | 1.481 |
| Night | Topo P90 | 0.3560 | **2.586** | 1.208 | 3.112 | 1.004 | 1.203 |
| Night | Street P10 | 0.1092 | 6.595 | 2.111 | 4.060 | 1.299 | 1.625 |
| Night | Street P50/P90 | 0.1139 | 6.406 | 2.050 | 4.060 | 1.299 | 1.578 |

**Reading.**
- The white ring passes 3:1 at night on every ground except the lightest, Topo P90 (2.586).
- By day it passes only on Topo P10. This is the same finding as the swatch board, and it does not
  depend on the new fills.
- The new fills change the other columns:
  - The day near-black fill stands well off the ground, at 3.07–5.73:1.
  - The night grey fill at 0.7 is 1.20–2.41:1 against the night ground.
  - The selected blue ring is barely distinct from the grey fill in luminance at night (1.00–1.50:1
    composited). It remains distinct in hue (ΔE 0.17).
- The colours were not changed.

## 6. Predictions against outcome

**Planner (outcome).**
1. *Highest-IoU pair waypoint / find, under 0.6.* **Not met.** Waypoint / find is 0.819, over 0.6.
   It is the fourth-highest pair overall (the third excluding the transient centre-pin pair), and the
   highest among the tall, bottom-anchored glyphs. The highest overall is sighting dot / search
   centre, 0.913.
2. *Under deuteranopia several pairs collapse, all told apart by silhouette.* **Partly met.** Several
   collapse and every one differs in shape. Two night pairs, find / sighting dot and photo /
   sighting dot, have silhouettes above the 0.6 target, so by the dispatch's own measure they are
   not clearly told apart by silhouette.
3. *Night grey under the white ring measures low ground contrast on the lightest night ground.*
   **Met in number:** 2.586 at Topo P90. That figure belongs to the ring, not the grey fill,
   because (b) does not involve the fill (section 5). The grey fill itself is 1.20:1 against that
   ground.

**Coder (mechanism, intent 2026-09-27-18).**
- The highest pair, sighting dot / search centre at 0.85–0.92: **met** (0.913).
- The trio all above 0.6: **met** (0.913, 0.834, 0.762).
- Waypoint / find at 0.55–0.65: **missed** (0.819). I underestimated how much of the pin's tail
  area the stem covers once both are scaled to the same height.
- The flag lowest against everything, under 0.45: **partly**. It is the lowest glyph, but its
  pairs reach 0.521.
- Anchor-aligned leaves the trio unchanged and lowers mixed-anchor pairs: **met**.
- Colour, the three day near-blacks all under ΔE 0.10: **missed**. Only offline region / sighting
  dot is under (0.090); search centre `#000000` against offline region `#0B0B0B` is not.
- Kept against live track and the two dot states as the only same-silhouette cases: the dot states
  are identical as predicted. Kept against live did not collapse in colour, so it never arose.
- Contrast reproducing the swatch dot-halo column exactly, with only night Topo P90 failing: **met**.

## 7. What could not be determined

- **Real `LocationOn` and `PhotoCamera` geometry.** Neither path was extracted (the ruling
  said not to parse bytecode), so both glyphs are approximations. The centre-pin IoU figures and
  the 3.3 dp tip-to-anchor gap in section 1 hold for the approximation only.
- **On-device appearance.** Nothing here was run on the phone. The following are unmeasured:
  MapLibre's actual stroke placement (outside the radius), `icon-image` scaling and resampling, and
  how 0.7 and 0.85 opacities blend over real coloured ground.
- **Whether IoU matches human discrimination** at 55–90 px. IoU ignores holes smaller than the casing gap,
  real size and colour. No perception test was run.
- **The ground crop is one tile.** Contrast uses the 18-tile sample percentiles. The board shows one crop, and
  wetter or more urban ground was not sampled.

## 8. Provenance

Scripts and working files live in `~/Zynergy/forager-glyph-work/`, outside the repo. Host Python 3.14.4
and Pillow 12.1.1, no numpy. sha256:
- `glyphs.py` f9ecf5365627d8375531ab786b90369f3a6e8ba63d1180d66ab6335085b2e656
- `iou.py` 4c2071d0143862d57e63b800949fe27037e4585ab616f17cf4eaf9dd22f184f1
- `board.py` 26d4da174c0a4de1eb5cef25358b2fa437cb71501bb2dd1b0a89200b51254f16
- `measure.py` 6d007a9e9b4f66bdd4043a6411a4b4b58f855c2b69e1c9daad7696daa2823d46
- `glyph-board.png` 90b636a156ec4b970346aca953d85d0d382e288134ab196dc5508dfeccd7f183 (the committed board)
- `cvd-deuteranopia.png` ea7652b309872e2a2900d55d6ac7ee9b73970fbf919b052157ee9a1142056d7d
- `cvd-protanopia.png` f5f9eb7fb42c5be5e0a5ce480423ab83a17396f25914a12fd12e464d862e5a1a
- Reused from `~/Zynergy/forager-swatch-work/`: `colour.py` 8119a0da2b40fa024c5afc046fa612cfb935ce222a00409a0c67183761b9b212,
  `ground.json` 55039d7c07be2449e86ce9d5e3a2051804ab97cbc1fb36cf4270b477b2f63891
- Tiles: `~/Zynergy/forager-tile-sample/{opentopomap,osm}/14/2572_5854.png`.
