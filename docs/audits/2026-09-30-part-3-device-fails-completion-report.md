# Part 3 device fails: completion report (dispatch 2026-09-29-57, records -252, -255, -256)

**Coder model:** Sonnet 5.5 (`claude-sonnet-5-5`), read from the system prompt only.
**Base:** `7aa8b7f5` (journal-redesign after the tablet-as-phone merge `01e95038`), verified with `git cat-file -t` and against `origin/journal-redesign` at start (`git rev-parse origin/journal-redesign` = `7aa8b7f5`). `CLAUDE.md` at the base is identical to the one loaded into the session.
**Branch / worktree:** `device-fails` at `/home/zynergy-labs/Zynergy/forager-wt/device-fails`.
**Machine rule for this session (owner):** memory gate 2048 MB available (not 2.5 GB).

## Pre-registration (written and pushed before any code edit)

Triage by reading the code at the base (nothing run yet). Items marked STOP are not built; the reason is in the item's section (filled in below as the work lands).

| Item | Plan | Prediction |
|---|---|---|
| 1 record button over the "i" | STOP: no rule says which of the L's bottom limit and the attribution wins | not built |
| 2 bubble re-anchors low after rotation | STOP at diagnosis: depends on MapLibre's projection (`SightingsMap.kt:406-441`) | not built |
| 3 bubble opens under the L / rail | build if a Robolectric test can show it with the L's and the rail's own measured bounds | before: the bubble's bounds overlap the L (`MapBubble.kt:132-148` clamps only to the box and `minY`); after: they do not |
| 4 bubble at the top covers its glyph | STOP: M1 records "no tail when the clamp puts the card over the point" (`2026-09-28-m1-glyph-bubbles-completion-report.md:283-285`, `:511`) as the design; no rule says flip-below | not built |
| 5 photo glyph on a thin track | STOP at diagnosis: markers-first is already the rule (`layers/TapPrecedence.kt:20-24`), the failure is "nothing opened", which the handler does not produce in a headless scene | not built |
| 6 stack distance 32 dp | build. `stackOf` `ui/map/fanout/MarkerFanOut.kt:58-64` gets its own constant | 35 dp vertical: no fan before and after is 20 dp fans; 31 fans / 32 does not on both axes: before, 31 fans, 32 fans (48 threshold) so the 32 case fails before the change |
| 7 one layer at a time | build. `MapTapHandler.onMapTap` (`ui/map/fanout/MapTapHandler.kt:63-80`) folds the fan on any tap that is not on a member; `MarkerFanOutBackHandler` (`MarkerFanOutState.kt:96-100`) is composed after the bubble's own handler so it is asked first | before: the first empty tap folds the fan as well as closing the bubble; Back folds the fan first. After: bubble only, then fan |
| 8 Back returns to the map | STOP: `CompactMapTab` leaves composition on a tab switch (`AvailabilityCompactScaffold.kt:889`, `:1082`), so `tapped` (`AvailabilityCompactMapUi.kt:365`) and `SightingsMap`'s `fanOut` (`SightingsMap.kt:341`) are lost; dispatch says stop and name what would be hoisted | not built |

Pass conditions for 6 and 7: the new tests fail at the base for the stated reason and pass after; each revert of the one edit fails with the message specific to it; full unit suite 0 failures apart from the two owner-held flakes.
