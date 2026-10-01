# Dispatch 2026-09-28-286, glyph-resize: completion report

Base `ecd79009` (verified by `git rev-parse`; `origin/journal-redesign` was `06264376`, which adds only the dispatch file).
Branch `glyph-resize`, worktree `forager-wt/glyph-resize`. No device, no adb.

## What landed

| Commit | What |
|---|---|
| `18205e12` | Tests first, pushed failing: new dimensions and anchors, fan-centring offsets, the FIND journal-halo edge (26 to 25), a "25 dp tall, proportions kept, between the photo and the pin" case, and a guard that WAYPOINT, PHOTO and SEARCH_CENTRE are unchanged. |
| `4148f228` | The mushroom's stem-foot probes in `MarkerGlyphsTest` re-based to the scaled geometry (see "Beyond the dispatch's list"). |
| `38428fe1` | The change, in `MarkerGlyphs.kt` only. |
| `163784cc` | `git pull --no-rebase` of `origin/journal-redesign` (`06264376`): no conflict. |
| `5ed12bed` | `FanOutLayersTest`'s pinned offsets re-based; found by the full suite, not the targeted runs. |

## Mechanism, and why

`MarkerGlyph` takes its design size and anchor as plain constructor arguments and a `scale` (default 1), and exposes
`widthDp`, `heightDp`, `anchorXDp`, `anchorYDp` as the design value times `scale`, so every existing reader sees the drawn size.
`parts()` multiplies FIND's and PLANNED_TRIP's shape coordinates by `scale`; `GLYPH_TARGET_HEIGHT_DP = 25f` is the target and each
scale is that over the design height (25/26 and 25/28), so proportions are kept by construction. The paints are not touched, so the
casing (3 dp stroke, 1.5 dp each side) and the detail strokes keep their width. Why this and not `canvas.scale` or a path
transform: `parts()` is read on its own by the halo and by tests (`fillBounds`), so scaling the coordinates there keeps every
consumer consistent with the reported size in one place, and needs no `Matrix`.

**Exact new sizes** (dp, 2 places): FIND 23.08 x 25.00, anchor (11.54, 25.00); PLANNED_TRIP 17.86 x 25.00, anchor (1.34, 25.00).
Unchanged: WAYPOINT 22 x 28, PHOTO 22 x 22, SEARCH_CENTRE 26 x 26. Fan-centring offsets (from `fanCentringOffsetDp()`): FIND
(0, 12.5) from (0, 13); PLANNED_TRIP (-7.59, 12.5) from (-8.5, 14); the rest unchanged.

## Premises, verified (at `ecd79009`)

- **Dimensions and anchors:** `MarkerGlyphs.kt:34-45`, PLANNED_TRIP (20, 28, 1.5, 28) and FIND (24, 26, 12, 26): confirmed.
- **Shapes:** `parts()` hard-coded dp coordinates, FIND's dome `arcTo(RectF(0,0,24,28), 180, 180)` and stem `rect(8, 13.5, 16, 26)`,
  PLANNED_TRIP a pole `rect(0,0,3,28)` and pennant `rect(3,0,20,12)`: confirmed.
- **Bitmaps** are padded about the anchor from `widthDp`/`heightDp`/anchors (`drawGlyph`, `drawGlyphHalo`), so they follow: confirmed.
- **Fan-centring** follows from the dimensions: confirmed.
- **Every draw site or reader of a glyph's size:** in `main/` only (1) `SightingsMap.kt:1071`, which registers every
  `MarkerIcon`'s image through `markerIconImage`, so the map layer and the fan's copies (same image ids) get the new size;
  (2) `FanOutLayers.kt:173`, which reads `fanCentringOffsetDp()`; (3) inside `MarkerGlyphs.kt`, `drawGlyph`, `drawGlyphHalo` and
  `MarkerIcon`. **The journal halos** (`FIND_JOURNAL_HALO`) are drawn from the same glyph, so the find's halo follows
  (its top edge is now 25 dp above the anchor, not 26). A search of `main/` for a legend, list row, swatch or Layers-sheet use of
  `MarkerGlyph`/`MarkerIcon` found none: there is no other draw site. **No tap target or hit box is derived from a glyph's size:**
  the touch area is `FAN_TOUCH_DP` and the tap box `TAP_BOX_DP`, both constants; the stop condition did not fire.

## Tests

- Red before the change (`/tmp/gr-red.log`): four failures, each expecting the new value and seeing the old (width 23.0769 vs 24;
  offset y 12.5 vs 13; height 25 vs 28; FIND halo "nothing past the halo" read 255). The WAYPOINT/PHOTO guard passed: a control.
- Revert checks, from a saved copy of the forward file (not from git), build log 0 `e:` lines and both Kotlin compile tasks run:
  - **flag at scale 1:** `MarkerGlyphsTest` fails on "PLANNED_TRIP width expected 17.8571 but was 20.0", "the flag's height
    expected 25.0 but was 28.0" and "PLANNED_TRIP fan centring offset x expected -7.5893 but was -8.5"; no FIND failure.
  - **find at scale 1:** fails on "FIND width expected 23.0769 but was 24.0", "the find's height expected 25.0 but was 26.0",
    "FIND fan centring offset y expected 12.5 but was 13.0", the mushroom's rim probe, and the FIND halo edge; no flag failure.
  - `FanOutLayersTest`, same two reverts: "the trip copy's icon-offset x expected -7.5893 but was -8.5", and "the find copy's
    icon-offset y expected 12.5 but was 13.0".
  - Forward change confirmed after each restore (`git status` clean, both `scale = GLYPH_TARGET_HEIGHT_DP / 26f|28f` present).
- **Stroke widths are not scaled:** true by construction (the paints are untouched). No test pins it at pixel level: at density 3 a
  pixel is 0.33 dp, larger than the difference a scaled casing (1.34 dp against 1.5) would make, so a pixel probe could not tell them
  apart. Stated as unverified by test; the device list covers it.

## Beyond the dispatch's list (found while building, disclosed)

- `JournalHaloGlyphTest` pinned the FIND halo edge at -26; re-based to -25 in the first test commit.
- `MarkerGlyphsTest`'s "the find marker is a mushroom anchored at its stem foot" probed the unscaled design offsets from the stem
  foot (its right corner at +3.5 is outside the narrower stem now): re-based to design x 25/26 (`4148f228`). Weak as a guard for the
  shape by itself; the dimension cases are the guard.
- `FanOutLayersTest` ("each copy carries the icon-offset...") pinned (0, 13) and (-8.5, 14) through the pushed frame. The targeted
  runs did not include it; the first full-suite run failed on it, and it was re-based (`5ed12bed`) with per-copy messages.

## Suite

`:app:testDebugUnitTest` at `5ed12bed`: 388 classes, **3170 tests, 0 failures, 0 errors, 24 skipped**, BUILD SUCCESSFUL in 3m59s
(`/tmp/gr-full2.log`). The parent line's last count was 3168; this adds the two new cases. `LeavingTheJournalFixesTest` ran 31 of 31
with no failure and no stall. An earlier full run (`/tmp/gr-full.log`, at `163784cc`) had 3170 tests, 2 failures: `JournalTabTest`
"From Album on the edit form opens the picker and pulls the selected photo into the entry" (an owner-held flake, not touched) and
`FanOutLayersTest` above (mine, fixed). Machine gates were checked before every build (memory 3.7 to 5.8 GB, disk 4.9 to 5.1 GB,
no wrapper or worker).

## Device-only list (S22-B)

1. The flag and the find beside the pin and the photo, on the map and in a fan, by day and by night: the flag and the find read
   between the photo and the pin in size.
2. The flag's pole foot and the mushroom's stem foot sit on their true coordinate (the anchors were scaled with the shape).
3. Both centred in their 36 dp circles in a fan.
4. The outline weight of the flag and the find matches the pin's and the photo's (the casing was not scaled), and the pennant's
   and dome's edges are not clipped.
5. The find's journal halo still rings it evenly.
