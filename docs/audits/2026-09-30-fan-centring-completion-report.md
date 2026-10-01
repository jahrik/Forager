# Fan centring (dispatch 2026-09-28-275) — completion report

Branch `fan-centring`, pushed; not merged. Dispatch base `df35226f`; `origin/journal-redesign` (`96d3831c`, with map-return-fixes) merged in at `43ec7757` with no conflicts.

## Verified premises
- **Anchors** confirmed, `MarkerGlyphs.kt` enum: WAYPOINT 22×28 @ (11,28); FIND 24×26 @ (12,26); PLANNED_TRIP 20×28 @ (1.5,28); PHOTO 22×22 @ (11,11); SEARCH_CENTRE 26×26 @ (13,13).
- **Bitmaps** padded symmetrically about the anchor (`drawGlyph`, `drawGlyphHalo`), so `ICON_ANCHOR_CENTER` puts the anchor on the point. Confirmed.
- **Fan copies**: the icons layer used `ICON_ANCHOR_CENTER` with no offset, image per feature, circle at the same point. Confirmed.
- **Kinds the fan can hold** (`fanOutLayerIds`, `FanPlacement.kt:60`): tap-group MARKER layers except sighting dots: waypoint, find, planned trip, photo (photo already centred). Sighting dots are excluded from fans by rule ("1 A") and are circle-layer points, centred anyway; `fanFrameCollections` still handles them. The search-centre reticle (`TapGroup.NONE`) and journal halos are not fanned; the reticle is centre-anchored regardless. No other off-centre anchor.
- **Tap targets**: `fanMemberAt` / `memberPositionDp` use the member's coordinate-derived position, not the drawn icon, so nothing moves. No stop condition hit.

## Correction to the dispatch's figures (sign)
MapLibre's `icon-offset` moves the image from its anchor, positive right/down. The dispatch's (0,-14) etc. are the vector anchor → body centre. The image must move the opposite way, so the offset the copies apply is `(anchorX − w/2, anchorY − h/2)`: PHOTO (0,0), WAYPOINT **(0,+14)**, FIND **(0,+13)**, PLANNED_TRIP **(−8.5,+14)**. Magnitudes are as given; signs flipped. Tests pin these.

## What landed
- `7d41515e` tests first + compiling stub, pushed failing (2 failed: offset function; copies' property).
- `28bfe095` implementation:
  - `MarkerGlyph.fanCentringOffsetDp()` (`MarkerGlyphs.kt`), the one pure place.
  - **Mechanism**: a per-feature `iconOffset` `[x,y]` property on each icon copy, read by a data-driven `iconOffset(Expression.get(...))` on the fan's icons layer. Chosen because one shared layer and source stay as they are and the offset is testable through `fanFrameCollections`; per-kind layers would multiply layers and filters.
  - Circles, legs, hit-testing, registry layers, fold/fade untouched. A guard test pins circle and leg positions.
- `43ec7757` merge of `origin/journal-redesign`.

## Revert checks (saved copies, build logs checked: no compile errors)
1. Offset dropped from copies → `FanOutLayersTest` "each copy carries the icon-offset…" fails at its `assertNotNull` ("the … copy carries no iconOffset property"); glyph test still passes. Restored.
2. Sign swapped in the function → both the glyph test and the copies test fail on the value comparisons. Restored.
Forward change re-run green after each (`BUILD SUCCESSFUL`, 23 tests in the two classes).

## Suite
Full unit suite after merge: 3163 tests, 1 failed, 24 skipped. The failure was `LeavingTheJournalFixesTest` "F1 viewing a committed find…" (`IllegalArgumentException` at `LeavingTheJournalFixesTest.kt:476`), **not on the allowed-flake list**; it passed on its own rerun (`BUILD SUCCESSFUL`). Unrelated to this change (no map code); treat as a new intermittent for the owner to rule on. The listed owner-held flakes did not fail this run.

## Not verified / device-only (S22-B)
- **Unit of `icon-offset`**: I am inferring dp (the images are registered at the display density), which no test can confirm. If the body is off by the density factor, that is the cause. First check.
- **Data-driven array `iconOffset`** through the native parser (`Expression.get` of an array property) is unexercised under Robolectric.
1. Fanned waypoint, find and planned trip each centred in their circle, by day and by night.
2. Legs meet the circle centres.
3. Originals unmoved when the fan folds.
