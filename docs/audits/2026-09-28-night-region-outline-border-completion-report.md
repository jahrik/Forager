# Night offline-region outline border: completion report (2026-09-28)

Intent `2026-09-28-79` (build, `prompts/preserved/2026-09-28-79.md`), launched by continuation
`2026-09-28-85`. Worktree `/home/zynergy-labs/Zynergy/forager-wt/night-outline`, local branch `night-outline`,
pushed to `journal-redesign`; the tests-first commit, which leaves the suite failing, went to `night-outline-wip`
first. The planner writes the record and the terminal. This report does not touch `RECORD.md`.

**Outcome.** At night the offline region's dashed outline now has a solid white border beneath it, in the
track casing's pattern: the outline's own line, 1.5 dp wider on each side (4.5 dp in all), white at line
opacity 0.85. The black dashes are unchanged and run along its middle. By day the border's colour is fully
transparent, so the day map does not change. The fill and its opacity did not change. Tests first, two
revert checks and the full suite are below. How it looks on a screen is device-only, and no screen saw it
in this build. **The owner judges it on the phone.**

Paths below are under `app/src/main/java/com/zynergylabs/forager/app/` (production) or
`app/src/test/java/com/zynergylabs/forager/app/` (tests). Line numbers are at `16e9d0d` unless another
commit is named.

## The palette figures (proposed values)

- **Border:** `#FFFFFF`, line opacity **0.85**, **4.5 dp** wide (1.5 dp, `CASING_WIDTH_DP`, each side of the
  1.5 dp dashes), solid, butt caps, constant width at every zoom.
- **Dashes:** unchanged: `NIGHT.casing` `#000000`, 1.5 dp, 6 dp dash and 4 dp gap.
- **By day:** the border's colour is `0x00FFFFFF` (alpha 0), so it draws nothing.
- **Target, dispatch:** at least 3:1 against the darkest night ground in Part 1's evidence. It is asserted as
  a bar in `MapPaletteTest` over two samples I read from Part 1's frames (see "Part 1's darkest ground"
  below):

  | Part 1 ground | Region composite | Border outside the edge | Border inside the edge | Dash on border |
  |---|---|---|---|---|
  | `#0E0D0A` (darkest 10-degree bin, mean) | `#12110E` | 14.026:1 | 13.636:1 | 15.157:1 |
  | `#010101` (darkest single pixel) | `#070707` | 14.787:1 | 14.409:1 | 14.877:1 |

  The bars pass at 3:1. The figures in this table come from the Python copy of the test's helpers (see
  "How the pins were made"). The test asserts them only as at least 3:1.
- **Every night ground cluster** (`MapPaletteTest.nightGround`, the swatch board's 26), pinned as ratchets
  (`nightOfflineBorderPins`). Each figure is WCAG contrast, floored to three decimals. "Outside" is the
  border composited over the plain ground outside the edge, against that ground. "Inside" is the same over
  the region's composite (`#202020` at 0.2) inside the edge, against that composite. "Dash" is the black
  dash against the border, on whichever side is weaker:

| Ground | Region (fill composite) | Border outside, over ground | Border inside, over region | Dash on border (weaker side) |
|---|---|---|---|---|
| `#4E6012` | `#455315` | `#E4E7DB`, 5.573 | `#E3E5DC`, 6.599 | 16.507 |
| `#627524` | `#556423` | `#E7EADE`, 4.211 | `#E6E8DE`, 5.245 | 16.962 |
| `#334801` | `#2F4007` | `#E0E4D9`, 7.866 | `#E0E2DA`, 8.647 | 16.060 |
| `#798936` | `#677432` | `#EBEDE1`, 3.257 | `#E8EAE0`, 4.192 | 17.276 |
| `#879A45` | `#72823E` | `#EDF0E3`, 2.697 | `#EAECE2`, 3.534 | 17.594 |
| `#9CA754` | `#838C4A` | `#F0F2E5`, 2.297 | `#ECEEE4`, 3.081 | 17.916 |
| `#59490A` | `#4E410E` | `#E6E4DA`, 6.915 | `#E4E3DB`, 7.816 | 16.309 |
| `#465208` | `#3E480D` | `#E3E5DA`, 6.672 | `#E2E4DB`, 7.650 | 16.354 |
| `#2A2D01` | `#282A07` | `#DFE0D9`, 10.718 | `#DFDFDA`, 11.025 | 15.705 |
| `#AEBC65` | `#929D57` | `#F3F5E8`, 1.869 | `#EFF0E6`, 2.544 | 18.276 |
| `#C2D076` | `#A2AD65` | `#F6F8EA`, 1.553 | `#F1F3E8`, 2.151 | 18.725 |
| `#666319` | `#58561A` | `#E8E8DD`, 5.059 | `#E6E6DD`, 6.058 | 16.727 |
| `#323701` | `#2E3207` | `#E0E1D9`, 9.474 | `#E0E0DA`, 10.056 | 15.844 |
| `#4B3A04` | `#42350A` | `#E4E1D9`, 8.431 | `#E3E1DA`, 9.195 | 16.048 |
| `#A49047` | `#8A7A3F` | `#F1EEE3`, 2.715 | `#EDEBE2`, 3.561 | 17.582 |
| `#7F752A` | `#6C6428` | `#ECEADF`, 3.883 | `#E9E8DF`, 4.890 | 17.072 |
| `#020302` | `#080908` | `#D9D9D9`, 14.636 | `#DADADA`, 14.267 | 14.877 |
| `#BEA964` | `#9E8E56` | `#F5F2E8`, 2.071 | `#F0EEE6`, 2.799 | 18.077 |
| `#2E5CFA` | `#2B50CE` | `#E0E7FE`, 4.228 | `#DFE5F8`, 5.316 | 16.700 |
| `#446835` | `#3D5A31` | `#E3E8E1`, 5.155 | `#E2E6E0`, 6.146 | 16.628 |
| `#5C7D4A` | `#506A42` | `#E7ECE4`, 3.903 | `#E5E9E3`, 4.910 | 17.096 |
| `#173E48` | `#193840` | `#DCE2E4`, 8.818 | `#DDE1E2`, 9.483 | 15.942 |
| `#3C512B` | `#364729` | `#E2E5DF`, 6.866 | `#E1E3DF`, 7.776 | 16.254 |
| `#1F342E` | `#1F302B` | `#DDE1E0`, 10.020 | `#DDE0DF`, 10.424 | 15.802 |
| `#22201C` | `#22201D` | `#DEDEDD`, 12.078 | `#DEDEDD`, 12.069 | 15.598 |
| `#537342` | `#49623B` | `#E5EAE3`, 4.414 | `#E4E7E2`, 5.439 | 16.827 |

  - The border is under 3:1 against its ground on the six lightest clusters (`#879A45`, `#9CA754`,
    `#AEBC65`, `#C2D076`, `#A49047` and `#BEA964`; lowest `#C2D076`, 1.553 outside). There the black dashes
    carry the edge on their own: the existing as-drawn pins put the casing over the plain ground at 6.658:1
    to 12.577:1 on those clusters (`nightOfflineRegionAsDrawn`).
  - Over `#22201C`, where the fill all but vanishes (ΔE 0.001), the border is 12.078 outside and 12.069 inside.
  - The dash is 14.877:1 or more against the border on every cluster.
- **Other opacities, for the owner's judgement** (Python copy, not in the test). The only change is the one
  constant, `OFFLINE_REGION_BORDER_OPACITY` (`ui/map/layers/MapLayers.kt:200`). Its pins would be re-measured
  the same way:

| Line opacity | Border over `#0E0D0A` (out / in) | Border over `#010101` (out / in) | Dash on border, lowest of all 28 grounds | Border's own colour over `#010101` |
|---|---|---|---|---|
| 0.35 | 3.179 / 3.194 | 3.026 / 3.106 | 3.044 | `#5A5A5A` |
| 0.50 | 5.347 / 5.336 | 5.285 / 5.313 | 5.317 | `#808080` |
| 0.70 | 9.598 / 9.510 | 9.955 / 9.824 | 10.015 | `#B3B3B3` |
| 0.85 (proposed) | 14.026 / 13.636 | 14.787 / 14.409 | 14.877 | `#D9D9D9` |
| 1.00 | 19.433 / 18.880 | 20.873 / 20.144 | 21.000 | `#FFFFFF` |

  0.35 is the lowest opacity that clears 3:1 on both sides over both Part 1 samples. 0.85 is proposed because
  the night map's one other white stroke, the sighting ring, is drawn at 0.85 (`SIGHTING_DOT_STROKE_OPACITY`,
  `MapLayers.kt:209`). It reads as near white (`#D9D9D9` over black), and it is not pure white against a dark
  map. This is a proposal only. Brightness at night is the owner's call on the phone.

## The dispatch, verbatim

The store copy `prompts/preserved/2026-09-28-79.md` at base, whole, header included (4220 bytes, sha256
`f6f944415b69fac3f72390e66f46943f9649855690d80fc7a5e66eb3d6a7976d`):

````text
HEAD: fda3158 (journal-redesign) when written. **Queued in the build line after -78;** the base at launch is stated in the launch message.
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: build
Preserved: 2026-09-28T21:13:53Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** build

# Role

You are the coder for **the night offline-region outline with a white border**. This dispatch's intent is `2026-09-28-79`. The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Read `CLAUDE.md` first.

# The owner's ruling, verbatim

"3 A rework the outline only. The fill color and opacity is fine as is. The outline should have a white border". That answers the planner's report of Part 1's check 5:
- the night fill lightens the darkest quarter of the ground;
- the dashed outline does not carry the edge over the darkest ground, at 1.08 to 1.45 to 1 (checks 5a and 5b, `docs/audits/2026-09-28-stage-device-check-part-1-run-record.md`).

# What changes

- **Night only.** That is the planner's reading, since the question was the night region; stated to the owner. The offline region's dashed outline gets a **white border**: a white line beneath and around the dashes, like the track casing, so the edge carries over the darkest ground.
- **The fill's colour and opacity do not change** (the owner). Neither does the day outline.
- **Values are yours to propose:** the border's width, its opacity (white at some alpha), and whether the dashes' own colour or width changes to sit on it.
  - Measure them in `MapPaletteTest` in its ratchet style.
  - The target is at least 3 to 1 against the darkest night ground sampled in Part 1's evidence, and the dashes still read as dashes against the border.
  - State the figures. **The owner judges on the phone.**
- The border is part of the same outline layer group. It takes no tap if the outline takes none; Part 1 flagged that a visible outline takes no tap, and that is not yours to change. It keeps the registry's order and `registryProblems` passing.

# Verification before building

At file:line: the night region's fill and outline specs (see `docs/audits/2026-09-28-night-offline-region-completion-report.md`), how night mode selects them, how the track casing is drawn (the pattern to follow), and `MapPaletteTest`'s existing pins for the region.

# Tests

- **Tests first**, seen failing at base:
  - the night outline has a border layer in white at your proposed width and opacity;
  - the day outline is unchanged;
  - the fill is unchanged (pinned);
  - the palette figures against the darkest night ground.
- **Revert check** under CLAUDE.md's runner rules.
- **Full suite** from a cleared results directory.
- **Device-only:** the outline on the S22 at night over the darkest ground in `DEVICE CHECK 2026-09-28 B`, for the owner to judge.

# Scope

**In scope:** the offline-region layer specs in `ui/map/` (`SightingsMap.kt`, `layers/`), `ui/theme/MapPalette.kt`, tests, and the report `docs/audits/<date>-night-region-outline-border-completion-report.md`.

**Out of scope:** the fill, the day outline, taps, and everything else.

# Branch, environment, finish

- **Branch:** a worktree from `origin/journal-redesign` at the base the launch message names. Push to `journal-redesign` after each commit that leaves the suite passing, and put broken work on `night-outline-wip`. Merge with `--no-rebase`.
- **Environment:** `LC_ALL=C.UTF-8`. Before each Gradle run, check that no other Gradle build is running and that 2.5 GB is free. No phone.
- **D58** before each push.
- **Finish line:** the build, tests first, the revert check, the full suite and the report, all pushed. The planner re-runs the suite and writes the terminal.

# Abort conditions

- a change to the fill or the day outline being needed;
- a tests-first test passing at base;
- a revert build that does not compile;
- a non-held failure;
- disk full or OOM;
- two failed fixes;
- an unruled design question.

# Predictions (planner)

1. One new line layer and one palette role or constant.
2. The suite grows by 3 to 6.

# Merge

Not authorised.
````

The launch message's key points match the file. It names base `ed37751`, the worktree command and the push
rules, and adds the Gradle and disk checks and "Merge is not authorised". Launch note `2026-09-28-85`
(`RECORD.md`) and the plan (`docs/plans/journal-redesign.md:729-754`) carry the owner's confirmation, verbatim:
"Yes the night outline only". No planner message reached this session after launch.

## Verification before building

- **Base.** After `git fetch`, `origin/journal-redesign` was at `ed37751`, as the launch message says.
  `git worktree add ... -b night-outline origin/journal-redesign` then came up at **`20ada3d`**. The planner
  had pushed intent `2026-09-28-86` (`RECORD.md` and `prompts/preserved/2026-09-28-86.md` only) between my
  fetch and the worktree add. `git diff --stat ed37751 20ada3d` touches only those two files, and
  `git diff b358a4a ed37751 -- app/` is empty, so `app/` here is `b358a4a`'s, the tree the planner's suite
  (304 / 2477 / 0 / 0 / 24) was run on. Launch note `-85` names `5049dfc`. `5049dfc..ed37751` is also
  `RECORD.md` and a store copy only. Confirmed.
- **The night fill.** `MapPalette.NIGHT.offlineRegion = 0xFF202020` (`ui/theme/MapPalette.kt:132`), drawn by
  the fill layer `FillLayer(...).withProperties(PropertyFactory.fillColor(palette.offlineRegion))`
  (`ui/map/SightingsMap.kt:898-899`), at `OFFLINE_REGION_FILL_OPACITY = 0.2f` (`ui/map/layers/MapLayers.kt:188`)
  through the registry's base opacity (`MapLayers.kt:333-346`). Confirmed, unchanged.
- **The outline.** `offlineRegionOutlineSpec()` (`SightingsMap.kt:1449`): colour `MapPalette::casing`,
  `OFFLINE_REGION_CIRCLE_OUTLINE_WIDTH_PX = 1.5f` (`:1781`), dash 6 dp and gap 4 dp (`:1784-1785`), butt caps.
  `NIGHT.casing = #000000` (`MapPalette.kt:137`) and `DAY.casing = #FFFFFF` (`:105`). Registry entry
  `MapLayers.kt:350`: `PaletteRole.CASING`, `TapGroup.LINE`, following the fill. Confirmed, unchanged.
- **How night mode selects them.** `requestedMapStyle` picks `palette = MapPalette.forMode(nightMode)`
  (`ui/map/BasemapStyles.kt:207`; `forMode`, `MapPalette.kt:145`). The style effect passes
  `palette = requested.palette` and `drawOrder = orderedLayers(MAP_LAYER_REGISTRY, ...)` to
  `initializeOverlayLayers` (`SightingsMap.kt:598-599`, `:859`). That function builds every registry layer
  per style load, a line layer through `lineSpecForLayer(spec.id)` and `lineLayerFor` (`:895`, `:1527`), and
  its colour is `PropertyFactory.lineColor(spec.colour(palette))` (`:1529`). A Night Maps toggle reloads the
  style, so the colours are rebuilt from the palette. The palette is the only per-mode input the overlay
  layers take. Satellite stays day, but the marks, this border included, switch palettes
  (`SightingsMap.kt:205-209`).
- **The track casing (the pattern).** `trackLayerSpecs()` (`SightingsMap.kt:1410`) builds each casing with
  `casingFor`: `track.copy(layerId, colour = MapPalette::casing, widthDp = track.widthDp + 2 * CASING_WIDTH_DP,
  dashPattern = null)` (`:1411-1416`), with `CASING_WIDTH_DP = 1.5f` (`:1676`). It keeps the track's caps and
  zoom stops. The registry puts each casing directly below its track, with `TapGroup.NONE` and the track as
  state owner (`MapLayers.kt:351`, `:355`). The kept tracks' J8 halo sits below the casing. Confirmed.
- **`MapPaletteTest`'s existing pins for the region.** These are `every role holds the owner's colour`
  (`"offlineRegion" to (0xFF0B0B0B to 0xFF202020)`), the night (d) pin `Pin("offlineRegion", a = null,
  c = null, d = 0.360)`, and the 26 as-drawn rows in `nightOfflineRegionAsDrawn` with their test. Also
  `MapLayerRegistryTest`'s base-opacity pin of the fill at 0.2. All passed at base and are unchanged.
- **MapLibre carries a colour's alpha.** `javap -c` on `org.maplibre.gl:android-sdk:13.5.0` (the pinned
  version, `gradle/libs.versions.toml:37`) shows `PropertyFactory.lineColor(int)` calling
  `ColorUtils.colorToRgbaString(int)`, which formats `"rgba(%d, %d, %d, %s)"` with the alpha
  `((color >> 24) & 255) / 255`. So the day value `0x00FFFFFF` reaches the style as `rgba(255, 255, 255, 0)`.
  What the renderer draws for it is device-only.
- **Taps.** `tappableLayerIds` keeps only layers whose `TapGroup` has a precedence (`ui/map/layers/TapPrecedence.kt:17`),
  so a `TapGroup.NONE` layer is never queried.

## Part 1's darkest ground

Part 1's check 5 (b) reports the darkest ground the edge crosses as relative luminance 0.006 to 0.023
(Topographical, 70 to 120 degrees), and the dashes over it at 1.08:1 to 1.21:1
(`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md`, check 5). It gives luminances, not colours,
so I read colours from its saved overlay-off frames (read-only), using its own circle fit (centre (600.5,
1255.7), radius 426.6 px), with this script, kept at `/tmp/night-outline/part1_ground.py`, outside the
repository:

````text
#!/usr/bin/env python3
# Read-only on Part 1's evidence: per 10-degree bin of the fitted edge circle, the mean sRGB colour and mean
# relative luminance of the plain ground just outside the edge (R+7..R+13) in the overlay-off frame, and the
# darkest single pixel in the band R-13..R+13 of the overlay-off frame (no overlay drawn there).
import sys, math
from PIL import Image
off=Image.open(sys.argv[1]).convert('RGB').load()
cx,cy,R=map(float,sys.argv[2:5])
def lin(v): v/=255; return v/12.92 if v<=0.04045 else ((v+0.055)/1.055)**2.4
L=lambda p: 0.2126*lin(p[0])+0.7152*lin(p[1])+0.0722*lin(p[2])
ok=lambda x,y: 0<=x<905 and 300<=y<1730
rows=[]; darkest=None
for b in range(0,360,10):
    gout=[];band=[];bad=False
    for a10 in range(b*10,(b+10)*10):
        t=math.radians(a10/10)
        for r in range(int(R)-13,int(R)+14):
            x=int(round(cx+r*math.cos(t))); y=int(round(cy+r*math.sin(t)))
            if not ok(x,y): bad=True; break
            d=r-R
            band.append(off[x,y])
            if 7<=d<=13: gout.append(off[x,y])
        if bad: break
    if bad: continue
    mean=tuple(sum(p[i] for p in gout)/len(gout) for i in range(3))
    Lm=sum(L(p) for p in gout)/len(gout)
    dk=min(band,key=L)
    rows.append((b,mean,Lm,dk,L(dk)))
    if darkest is None or L(dk)<darkest[1]: darkest=(dk,L(dk),b)
rows.sort(key=lambda r:r[2])
for r in rows[:5]:
    print(f"bin {r[0]:3d}: outside mean rgb ({r[1][0]:.1f},{r[1][1]:.1f},{r[1][2]:.1f}) #%02X%02X%02X  L {r[2]:.4f}; darkest band px {r[3]} L {r[4]:.5f}"%tuple(round(c) for c in r[1]))
print(f"{len(rows)} bins; darkest band pixel overall {darkest}")
````

Run as `part1_ground.py <frame> 600.5 1255.7 426.6` on
`/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-stage-check-1/272-topo-night-off.png` and
`281-street-night-off.png`:

- Topographical: the darkest bin is 70 to 80 degrees, mean outside `(14.3, 12.8, 9.7)`, **`#0E0D0A`**, L 0.0061
  (Part 1's own "0.006" for this arc). Its darkest band pixel is `(1, 1, 1)`. The next bins are 110 degrees
  (`#10100D`, L 0.0101) and 100 degrees (`#1D2013`, L 0.0232).
- Street: the darkest bin is 70 to 80 degrees, `#16130D`, L 0.0070. The darkest band pixel on Street is also
  `(1, 1, 1)`, first found in the 110-degree bin.
- The darkest single pixel on either basemap: **`#010101`**.

`MapPaletteTest.part1DarkestNightGround` is `#0E0D0A` and `#010101`. `#010101` is darker than the darkest
swatch-board cluster, `#020302`, so the bar covers the strictest ground either source has.

## Tests first

Commit **`7d507f0`**, pushed to `night-outline-wip`. It holds the tests, plus one production line:
`MapLayerIds.OFFLINE_REGION_BORDER = "offline-region-circles-border-layer"`, a string constant nothing read at
that commit, so the tests compiled and failed on the border's absence, not on a missing symbol. The tests
reach the border's colour through `spec.paletteRole!!.colourOf(palette)` and its width through
`lineSpecForLayer`. Neither needed a new symbol.

New tests (4):
1. `SightingsMapOverlayDataTest`: `at night the offline outline has a solid white border beneath it, the casing
   width wider on each side`. Covers the source, opaque `#FFFFFFFF` at night, 1.5 + 2 × `CASING_WIDTH_DP` =
   4.5 dp, solid, no zoom stops, and the outline's caps.
2. `SightingsMapOverlayDataTest`: `by day the offline outline is unchanged and its border draws nothing`.
   Covers the border's day alpha 0, and the day outline `#FFFFFFFF`, 1.5 dp, dash 6 and gap 4, butt caps,
   constant width.
3. `MapLayerRegistryTest`: `the offline outline's night border sits directly below the outline and above the
   region's halo, takes no taps and follows the fill`. Covers LINES group, `TapGroup.NONE`, absent from
   `tappableLayerIds`, state owner the fill, not toggleable, no opacity slider, not reorderable, not a
   decoration, `LINE` 0.85, hidden with the fill, white at night and alpha 0 by day.
4. `MapPaletteTest`: `the night outline border holds 3 to 1 over Part 1's darkest night ground, and its figures
   over every night ground cluster`. It first pins the fill (`#202020`, 0.2), then asserts the bars (border
   both sides and dash, 3:1, over Part 1's two samples), then the 78 ratchets.

Re-pinned in `MapLayerRegistryTest`, with the border added (7): the id set and count (19 to 20), the full
order, kind, renderer and source, tap groups, base opacities (the border 0.85, every other line still 1), state
owners, and the J8 region halo's "directly below" (from the outline to the border). One more test was changed
without being re-pinned: `the only change from the order before L0a ...` now also subtracts the border's id.
**It passes at base by construction**, since subtracting an id that is not there changes nothing. It is an
existing guard adapted so that it keeps testing what it tested, not a tests-first test.

The dispatch's items "the day outline is unchanged" and "the fill is unchanged (pinned)" pin values that do
not change. A pure pin of an unchanged value passes at base by construction. So each is written inside a test
that also needs the border (tests 2 and 4), and those failed at base on its absence. The existing pins of both
passed at base, as they must.

**Predicted at base:** 11 failures. Tests 1 to 4 and the 7 re-pins each fail because no border layer exists.
Tests 1 to 4 would fail with their own "no border ..." messages, and the re-pins on their changed
expectations. Everything else in the three classes passes, including the adapted guard.

**Seen** (`MapPaletteTest`, `MapLayerRegistryTest`, `SightingsMapOverlayDataTest`; results directory cleared;
`LC_ALL=C.UTF-8`; build log 0 `e: ` lines; XML timestamps 22:34:00Z to 22:34:01Z): **65 tests, 11 failed, 0
errors**.
- `SightingsMapOverlayDataTest` (31, 2 failed): tests 1 and 2, each `java.lang.AssertionError: no border line
  is built under the offline outline`.
- `MapLayerRegistryTest` (22, 8 failed):
  - test 3: `no border layer under the offline outline in the registry`;
  - `base opacities ...`: `java.util.NoSuchElementException: Collection contains no element matching the
    predicate.` (the class's `spec(id)` helper finding no border);
  - the five list and map re-pins, each `expected:<...>` naming `offline-region-circles-border-layer`;
  - the halo re-pin: `journal-entry-regions-layer is directly below offline-region-circles-border-layer
    expected:<-2> but was:<3>`.
- `MapPaletteTest` (12, 1 failed): test 4, `no border layer under the offline outline in the registry`.

It matched the prediction, including the adapted guard passing.

## What landed

| SHA | Branch | What |
|---|---|---|
| `7d507f0` | `night-outline-wip` | Tests first (above), with the unread id constant |
| `16e9d0d` | `night-outline-wip`, then `journal-redesign` | The build |
| `9c43c0b` | `journal-redesign` | Merge of `origin/journal-redesign` (`git pull --no-rebase`, no conflict; records and docs only) |
| this report's commit | `journal-redesign` | This file |

The build (`16e9d0d`):
- `ui/theme/MapPalette.kt`: new field `offlineRegionBorder` (`:86`, with its KDoc), `DAY` `0x00FFFFFF`
  (`:110`), `NIGHT` `0xFFFFFFFF` (`:142`); the class doc's role list names it (`:13-15`).
- `ui/map/layers/MapLayers.kt`: `PaletteRole.OFFLINE_REGION_BORDER` (`:69`); `OFFLINE_REGION_BORDER_OPACITY =
  0.85f` (`:200`, KDoc `:190-199`); the registry entry (`:348-349`) between the region's halo (`:347`) and the
  outline (`:350`), `TapGroup.NONE`, following the fill, line opacity 0.85; the registry KDoc and the J8
  paragraph updated to name it.
- `ui/map/SightingsMap.kt`: `offlineRegionBorderSpec()` (`:1472`), built as `casingFor` builds a casing, from
  `offlineRegionOutlineSpec()`; it is in `lineSpecForLayer`'s list (`:946`), so `initializeOverlayLayers`
  builds it from the registry walk with no new call site.

Not changed: the fill's colour and opacity, the outline's colour, width, dash and caps, the day palette's
existing roles, the halo's own spec, tap routing, and every file outside the three above and the three test
classes.

## Revert checks

Runner: `/tmp/night-outline/revert.sh`, outside the repository. For each check it saves a copy of the file
before editing, makes a one-line edit and shows `git diff --stat` (one file, one line). It clears
`app/build/test-results/testDebugUnitTest` and runs `MapPaletteTest`, `MapLayerRegistryTest`,
`SightingsMapOverlayDataTest` and `MapLayerFeatureIdTest`. **It counts `e: ` lines in the build log and
refuses to cite results if there are any.** It checks every XML timestamp is after the run's start. It restores
the file from the saved copy, never from git, and `cmp`s it. After each check I confirmed `git status` clean,
so the file equals the committed `16e9d0d`, and the forward value is present once.

**R1, the border at night (the dispatch's revert check).** `MapPalette.kt:142` `offlineRegionBorder =
0xFFFFFFFF.toInt()` to `0x00FFFFFF`, so there is no visible border at night: the night map as at base.
- Predicted: 3 failures, each an "opaque white at night" assertion seeing `#00FFFFFF`.
- Seen (started 22:37:12Z, XML 22:37:25Z, 0 `e: ` lines): 78 tests, **3 failed**:
  - `SightingsMapOverlayDataTest` test 1: `opaque white at night expected:<#[FF]FFFFFF> but was:<#[00]FFFFFF>`;
  - `MapLayerRegistryTest` test 3: the same;
  - `MapPaletteTest` test 4: `the border is opaque white at night expected:<#[FF]FFFFFF> but was:<#[00]FFFFFF>`.
- Each names the night value this edit sets, so it is this revert's failure. `MapLayerFeatureIdTest` passed:
  the line's colour and the role's colour still agree, as they should.
- Restored: `cmp` equal; `git status` clean; `0xFFFFFFFF.toInt()` present once.

**R4, the border's opacity (added: shows the bar and the ratchets can fail on the arithmetic).**
`MapLayers.kt:200` `0.85f` to `0.3f`. Test 4's bar had only been seen failing on the border's absence, never
on a contrast figure.
- Predicted by the Python copy: 3 failures. Two are `MapLayerRegistryTest` opacity pins. The third is
  `MapPaletteTest` test 4 with 84 lines: 6 bars (all three figures under 3:1 on both Part 1 samples, lowest
  2.4693) and all 78 ratchets. The first bar line is `Part 1 ground #0E0D0A: border outside is 2.6418:1`, and
  the first ratchet is `#4E6012` outside at 2.0289.
- Seen (started 22:37:33Z, XML 22:37:40Z to 22:37:41Z, 0 `e: ` lines): 78 tests, **3 failed**:
  - `base opacities ...` and test 3: each `expected:<[BaseOpacity(property=LINE, base=0.85)]> but
    was:<[BaseOpacity(property=LINE, base=0.3)]>`;
  - test 4: `Night outline border, #FFFFFF at 0.30000001192092896:`, then 84 lines, 6 starting `Part 1 ground`
    and 78 starting `ground`. The first is `Part 1 ground #0E0D0A: border outside is 2.6418:1, target 3:1`. The
    first ratchet is `ground #4E6012: border outside is 2.0289:1, pinned at 5.573`, and the last `ground
    #537342: dash on border is 6.2296:1, pinned at 16.827`.
- Every line names 0.3 or its figures. Restored: `cmp` equal; `git status` clean; `0.85f` present once.

## The suite

At `16e9d0d`, after `pgrep` found no other Gradle build and `df` showed 6.9 GB free on `/` (about 2.7 GB of
memory available). Results directory cleared, `LC_ALL=C.UTF-8`,
`./gradlew --offline :app:testDebugUnitTest`, started 22:37:58Z: exit 0, `BUILD SUCCESSFUL in 3m 5s`, build log
0 `e: ` lines. Counts are from the JUnit XML, 304 files, timestamps 22:38:08Z to 22:41:02Z, all after the start:

**304 classes / 2481 tests / 0 failures / 0 errors / 24 skipped.**

That is the planner's 304 / 2477 / 0 / 0 / 24 at `b358a4a` (whose `app/` this base has), plus the four new
tests. No classes were added. The held flaky family (`JournalPendingDeleteTest`'s album tests,
`JournalTabTest`'s photo pull) did not fail this run. No baseline suite was run here. The planner's count
stands in for it, and the dispatch did not ask for one.

The push merged `origin/journal-redesign` (`git pull --no-rebase`, merge `9c43c0b`). It brought only
`RECORD.md`, the plan, a run record and two store copies. `git diff 16e9d0d 9c43c0b -- app/` is empty, so the
suite's result stands for the pushed tree.

## How the pins were made

A Python copy of `MapPaletteTest`'s helpers (sRGB composite with `roundToInt` half up, WCAG luminance, Ottosson
Oklab, floor to three decimals, opacities as `Float` widened to double), at `/tmp/night-outline/pins.py`,
outside the repository. Before it wrote any pin, it reproduced all 78 existing `nightOfflineRegionAsDrawn`
figures exactly (26 rows, 0 mismatches). The test is the arbiter: all 78 border pins pass at `16e9d0d`. The
Part 1 and alternative-opacity figures above are the Python copy's alone. R4's first ratchet value, 2.0289,
matched the copy's prediction to four decimals.

## Predictions

**Planner's.**
1. "One new line layer and one palette role or constant." **Held, with two constants besides.** There is
   one line layer (`offline-region-circles-border-layer`) and one palette role (`offlineRegionBorder` /
   `PaletteRole.OFFLINE_REGION_BORDER`). There are also two constants: the layer id, and
   `OFFLINE_REGION_BORDER_OPACITY`.
2. "The suite grows by 3 to 6." **Held:** +4 tests (2481 against 2477), no new class.

**Mechanism (coder's).** It was written here after reading the code and before the build. It is verified by
tests only where it says so.
- The border reaches the map with no new call site. `initializeOverlayLayers` walks the registry and asks
  `lineSpecForLayer` for each line layer, so a registry entry and a spec in that list are enough. This is
  verified headlessly by `MapLayerFeatureIdTest`'s "every line layer in the registry is a line the map builds,
  on the same source, in the role's colour", which passes with the border in the registry. What the device
  draws is unverified.
- It shares the outline's source, so `refreshOverlayData`'s one push to `offline-region-circles` feeds it, and
  `initializeOverlayLayers` adds that source once (`addedSources`). This was read, not tested.
- At night the white border shows through the dashes' gaps, so the edge becomes a continuous light band with
  black dashes along its middle. By day, `rgba(255, 255, 255, 0)` draws nothing. Both are device-only.
- Taps are unchanged, because `TapGroup.NONE` keeps it out of `tappableLayerIds`. This is verified by test 3
  and `TapPrecedenceTest` passing.
- It hides with the Offline maps switch, since `layerPaintFor` reads the fill's state. This is verified by
  test 3.

## Device-only

None of this was seen on a screen by this build. There was no phone and no emulator. Once a build with
`16e9d0d` is on the S22:

1. **The dispatch's item:** the outline at night over the darkest ground of region `DEVICE CHECK 2026-09-28 B`
   (Part 1's check 5 camera, Topographical and Street). Does the edge now carry over the near-black arc? Do
   the dashes still read as dashes on the white? **Is 0.85 too bright at night?** The owner judges. Other
   opacities are in the table above and need a one-line change.
2. By day, nothing new at any region edge. This rests on MapLibre drawing `rgba(255, 255, 255, 0)` as nothing.
3. Over Satellite at night the border is drawn too (the marks switch palettes there). No Satellite ground is
   in the pins.
4. **A region highlighted by a journal entry shown on the map, at night.** The region's J8 halo (7.5 dp,
   `#00DDFF`) now lies under the 4.5 dp border, so it shows 1.5 dp beyond the border on each side instead of
   3 dp beyond the dashes. Through the dashes' gaps the border's white shows, not the halo's cyan. By day it
   is unchanged. See Decisions.
5. The entry report's map with its Offline map switch on at night. It is the same map composable, so the
   border should draw there too. Unverified.
6. MapLibre's blend space. The figures assume an sRGB blend, as the class's `composite` says. In linear light
   the drawn greys differ.
7. Taps. Part 1 found that a visible outline takes no tap (`84-`, `86-`). That is unchanged and not this dispatch's
   to change. The border adds no tap target.

## D58

Before each push I ran a case-insensitive `grep` for the three phrases D58 forbids
(`/tmp/night-outline/d58.sh`, outside the repository). It covered `git diff 20ada3d`, every commit message
since `20ada3d`, and the files under `app/` and this report. A positive control fed one phrase through the
same `grep` and counted 1. Result at each push: 0 hits before `7d507f0`'s push, 0 before `16e9d0d`'s, and 0 before the build's push
to `journal-redesign` (over the diff, the messages and `app/`). Before this report's push, I ran the same
`grep` over this file and got 0 hits.

## Decisions I made

- **No record steps.** My agent definition asks me to sweep, write an intent and write a terminal.
  `.claude/kit.json`, `check_record.py` and `check_prompts.py` do not exist at this base (`e136330`, owner,
  removed the kit). `RECORD.md:2404` records the owner's "Also RECORD.md by hand". The dispatch says the
  planner writes the record. I followed the dispatch and wrote nothing in `RECORD.md`. Structural validation
  against `kit.json` was not possible at this base. Against `main`'s `kit.json` the dispatch does not use the
  section names "Base and state", "Scope boundary", "Closed decisions", "Finish line and abort conditions",
  "Checks", "Out of scope" or "Device items", though it carries their content. I did not stop on that.
- **Night only, by a palette colour that is transparent by day.** The alternative was a night-only flag on
  `MapLayerSpec` with a mode-aware draw order. I chose the palette because the palette is the only per-mode
  input the overlay layers take (`SightingsMap.kt:598`), because the planner's prediction named a palette
  role, and because the registry stays mode-independent. The rejected option would have changed the registry
  model and `orderedLayers`' inputs. The cost is that the day value carries an opacity (alpha 0), where the
  registry is otherwise the one place a layer's opacity is written. This is recorded on the field's KDoc.
  What was needed to decide it properly: nothing in the dispatch said how "night only" is to be built.
- **Placement: halo, border, outline.** This is the kept tracks' order (halo, casing, track) and J8's rule
  that a halo sits "below that record's casing where it has one". The alternative, border below the halo,
  would leave J8's night look exactly as it was, but a highlighted region's halo would then cover the border
  completely. My choice changes a highlighted region's night halo (device item 4) and re-pins J8's "directly
  below" expectation for the region halo. The owner can rule the other way. That is a one-line move in the
  registry and one pin.
- **Width 4.5 dp**, `CASING_WIDTH_DP` each side, reused rather than a new constant, because the dispatch said
  "like the track casing".
- **Opacity 0.85**, matching the sighting ring. It is a proposal, with alternatives in the table.
- **White is opaque in the palette; the opacity is in the registry**, not in the colour's alpha at night.
- **The dashes are unchanged.** The dispatch allowed changing them. At 0.85 they are 14.877:1 or more on the
  border. The dashes use the shared `CASING` role, so changing them at night alone would need a new role.
- **`TapGroup.NONE`.** I read "It takes no tap if the outline takes none" together with "taps" being out of
  scope and the casing pattern: the border adds no tap target. The registry's outline is `TapGroup.LINE`. On
  the phone it takes no tap (Part 1). Neither changed.
- **Caps:** the outline's butt caps, copied as `casingFor` copies a track's caps.
- **A production constant in the tests-first commit** (`MapLayerIds.OFFLINE_REGION_BORDER`, unread there).
- **"Day outline unchanged" and "fill unchanged" folded into tests that need the border**, and one adapted
  guard that passes at base by construction (see Tests first).
- **Part 1's darkest ground defined as two samples I measured** (`#0E0D0A`, `#010101`) with my own script,
  since the run record gives luminances only. The dash bar (3:1) is my reading of "the dashes still read as
  dashes".
- **Ratchets (not worse)** for the 78 border pins, per "its ratchet style". The class's as-drawn table uses
  exact matching instead. The fill is pinned exactly, in the same test, so a fill change cannot pass silently.
- **A second revert check (R4)**, beyond the one asked, because test 4's bar had never been seen failing on a
  contrast figure.
- **The figures quoted in production KDoc are pinned ones only.** The Part 1 exact figures are in this report,
  labelled as the Python copy's.
- **Report content:** the Part 1 sampling script is quoted here in full, since it lives outside the
  repository.

## Flags outside scope

- **The base moved between fetch and worktree** (`ed37751` to `20ada3d`, the planner's `-86` intent). `app/` is
  identical. The launch note `-85` names a third commit, `5049dfc`, also identical in `app/`.
- **Part 1's "a visible outline takes no tap"** (its flag 10) stands. The registry lists the outline as
  `TapGroup.LINE`. Not touched.
- **Long KDoc lines.** Two lines my edits lengthened run past the files' usual wrap: `MapPalette.kt:15`, and
  the J8 paragraph in `MapLayers.kt` (`:320`). They are cosmetic. They were left rather than change
  source after the suite ran.
- **Carried from the night-region report, not re-checked here:** `searchCentre`'s night (d) pin is loose at
  0.099, and `MapPaletteTest`'s class doc still quotes "5.85:1 (offline region)" from the `#FFFFFF` fill.
- **`-70` (J8 follow-ups) will reorder layers near this one.** The plan's ruling "Marker rings move below
  every line" (`docs/plans/journal-redesign.md`, "J8 device check: the owner's rulings", pulled in by the
  merge) reorders the J8 halos against the lines. The region's halo now sits under this border, and
  `MapLayerRegistryTest` pins that. Whoever builds `-70` will meet the pin, and the question in device item 4.
- The Part 1 samples come from one camera on one region. `#010101` is effectively black, so the bar is the
  strictest ground either source has, but whether other areas are darker in some other channel mix is not
  known.
