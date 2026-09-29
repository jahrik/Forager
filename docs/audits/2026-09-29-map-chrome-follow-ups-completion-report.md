# Map-chrome device check's follow-ups: completion report (dispatch `2026-09-28-104`)

Coder session, worktree `/home/zynergy-labs/Zynergy/forager-wt/chrome-follow-ups`, branch `chrome-follow-ups`.
Written by the coder; the planner writes the record. This file grows in sections; nothing already in it is rewritten.

**Model.** The session is configured for `claude-sonnet-5-5` (the system prompt names it). I cannot read the serving
model from inside the session, so I do not claim it.

## Governing text, verbatim

The owner's ruling, quoted in `-104` and in the launch prompt: "1 A 2 A".
- **1 A:** the Records details sheet opened from the Offline maps panel is at `MAP_CHROME_OVER_MAP_ALPHA` **only in short landscape**, where the picker map is beside it. It is **solid in portrait**, where it lies over the region list. This supersedes `-77`'s Q1 (b).
- **2 A:** batch the device check's small layout flags into one stage.

`-104`'s stop rule, verbatim: "**The stop rule for every item:** a fix that moves chrome to a place no ruling gave, adds copy, or needs a design choice not ruled above is a stop, with options reported."
Its abort conditions include "an unruled placement, copy or design choice; a deliberate fixed entry map (item 8); a tests-first test passing at base; a revert build that does not compile; a non-held failure; two failed fixes on one item; a refused push."
The launch prompt's item 8: "**First check** whether a comment, report or ruling says it is fixed on purpose. If one does, stop."

## Base and premises, verified before building

- **Base.** `origin/journal-redesign` at `a3221b4` (fetched 2026-09-29T02:3xZ), which is later than the `62e6bf5` the launch prompt names. `CLAUDE.md` is unchanged between the two (`git diff 62e6bf5 HEAD -- CLAUDE.md` is empty). The worktree was cut from `a3221b4`.
- **Item 1: the call site can tell.** `RecordsTab.kt:330` passes `overMap = selectedTab == RecordsSubTab.OFFLINE_MAPS`. The panel itself decides its layout at `AvailabilityOfflineMapsUi.kt:264` with `isShortWindow() && LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE`, and `log/JournalShortWindow.kt:63-64` already exposes that exact test as `isShortLandscapeJournal()` in the call site's own package. So the call site can tell, by the panel's own test. Not a stop.
- **Item 8: a comment says the entry map's basemap is independent on purpose.** `ui/log/CartographyEntryReportScreen.kt:227-234`: "[entryMapMode] is local to this screen, independent of `AvailabilityScreen`'s own `mapMode` — a confirmed, real state leak this dispatch fixes: before this, `basemap` threaded straight from the live Maps screen, so switching this entry's own preview to Satellite silently changed the live map too. Defaults to [MapMode.DEFAULT] (Topographical), reset per [entry]". The state is `:302`, applied at `:480` and `:529`. That is a written decision against the ruling's "it follows both". **Stop for item 8**; options below. `docs/audits/README.md` (2026-09-27 row, Night mode C1) and `2026-09-27-night-mode-c1-completion-report.md:40-45` separately say the entry map "keeps the default" for the `nightModeLoaded` gate "by the planner's ruling" resting on "an unverified inference"; that concerns the load gate, not the entry map's basemap or night value, and I did not read it as a ruling that the entry map ignores Night Maps. `night = night` is passed at `CartographyEntryReportScreen.kt:481`.
- **Item 7's convention for hiding versus disabling menu items** (cited, as asked). `CartographyEntryReportScreen.kt:198-200` ("the same 'absent, not disabled-and-visible' treatment") and `:231-234` ("hidden, not disabled ... an absent control reads as a feature not yet built rather than a limitation the app has"); the menu already hides the item for a draft or a caller with no callback (`:424`). The convention is hide.
- **What "highlightable" means.** `domain/GetJournalEntryHighlightsUseCase.kt:44-75`: an entry keeps its kept track, find, waypoint and offline-region decisions (`kept == true`) and its attached photos. I use that definition rather than a new one.
- **Item 6's cause.** `AvailabilityCompactScaffold.kt:647-656`: on the Maps tab the Scaffold's `contentWindowInsets` drop the bottom side and `bottomBar` composes nothing (`:665`), so the `snackbarHost` (`:613`) has no bottom bar to sit above and no inset padding of its own.
- **Item 5's cause.** `CentrePinLocationPicker.kt:288-300` aligns the row `BottomCenter`, `fillMaxWidth()`, padded only by `bottomInset`. The Maps tab passes `Modifier.fillMaxSize()` and `bottomInset` from `AvailabilityCompactMapUi.kt:1409-1412`, which is the measured bottom-nav height (zero in the rail layout, `AvailabilityCompactScaffold.kt:539-541`). The rail's width and the cut-out are in `controlsPadding` (`AvailabilityCompactScaffold.kt:557-566`), which the row does not get.
- **Item 3's cause is not established.** The run record (`2026-09-28-map-chrome-device-check-run-record.md`, check 2, the sheet paragraph) says the band is flat (20, 19, 18) and "What draws it (the sheet window's navigation-bar background, or Android's contrast scrim for three-button navigation) I did not establish." Two `ModalBottomSheet` call sites: `ui/map/MapLayersSheet.kt:243` and `ui/log/RecordDetailsSheet.kt:181`. `log/CameraWindowChrome.kt:22-60` records that for a target of API 35+ `Window.setStatusBarColor` "cannot be changed", so a window-colour fix may not exist on this target; `isNavigationBarContrastEnforced` is the documented switch for the contrast scrim.
- **Item 2 has two unruled halves**; see the pre-registration below.

## Pre-registration (written before any build or test run; pushed before building)

Predictions are mine. Each test goes through the real screen. "Fails at base" means: the assertion named fails on `a3221b4`, for the reason named.

| # | Item | Test (real entry point) | Predicted at base | Pass condition after |
|---|---|---|---|---|
| 1 | Records sheet from Offline maps | `MapChromeOverMapTest.kt` `MapChromeRecordsTests`: the existing Offline maps case, now expecting 0.8 in `MapChromeRecordsShortLandscapeTest` (`w823dp-h384dp-land`) and **solid** in `MapChromeRecordsPortraitTest` (`w384dp-h823dp`) and `MapChromeRecordsWideTest` (`w840dp-h1024dp`, not short, panel stacked) | portrait and wide **fail**: `RECORD_DETAILS_SHEET_TAG: container alpha` expected 1.0, was 0.8 (`RecordsTab.kt:330`). Short landscape passes both before and after: a check that cannot distinguish base from fix, so its value is only as the other half of the pair | portrait and wide solid, short landscape 0.8 |
| 4 | Back closes the suggestions | new `MapChromeFollowUpsTest`: type `zzz` into the Maps tab's search field, the suggestions open, one system Back | menu still present after one Back (`AvailabilitySearchUi.kt:1183` has no `BackHandler`; `ExposedDropdownMenu`'s popup is not focusable) | menu gone after one Back, and the search panel behind it is still there |
| 7 | "Show on map" only when something is kept | `JournalEntriesOnMapScreenTest.kt` compact harness: an entry with nothing kept, menu open | "Show on map" **present** (`CartographyEntryReportScreen.kt:424` gates only on draft and callback) | absent; present on an entry keeping a find; "Hide from map" still offered on a shown entry |
| 5 | Landscape pin row clears the rail | `MapChromeFollowUpsTest`, short landscape: `+` then Trip, then compare the row's bounds with the rail's | row's bounds overlap the rail's (`CentrePinLocationPicker.kt:288-300`); Robolectric does measure the rail's own width, so this half is visible. The nav-bar half is not | no overlap, row inside the map's own controls frame; nav-bar half **device-only** |
| 6 | Portrait snackbar above the system bar | none possible | n/a: Robolectric reports zero navigation-bar inset | **device-only**; the change is `WindowInsets.navigationBars` bottom padding on the Maps-tab snackbar |
| 3 | Sheet's nav-bar band | none possible | n/a | **device-only**, cause unestablished; a hypothesis-driven change, see below |
| 2 | Search notice | not built | n/a | **stop**, see below |
| 8 | Entry map follows basemap and Night Maps | not built | n/a | **stop**, see below |

Predictions on counts: the suite grows by 3 to 6 tests (the dispatch predicted 10 to 20 for eight items; two are stops and three are device-only). Item 4's `BackHandler` ordering against the scaffold's own `BackHandler(enabled = showSearchDropdown && !isDrawerOpen())` at `AvailabilityCompactScaffold.kt:397` is the one place I expect a surprise; the test will say.

## Stops

**Item 2, the search notice.** Two halves are unruled.
(a) *Below the strip.* The notice sits in the search bar's own `Column` (`AvailabilityCompactScaffold.kt:975-1001`), and the compass strip is drawn at `topInset` = the bar's height alone (`AvailabilityCompactMapUi.kt:1107-1123`), so any notice taller than zero starts under the strip. Getting it below needs a placement for it and the strip's real height, which the code does not carry: `compassStripClearance` is one text line's height (`:638-642`), not the strip's box.
(b) *Clear of the cluster.* The cluster's top limit is `minY = topInset + compassStripClearance` (`:713`) whether or not a notice is up, and the cluster is user-draggable, side-switching and minimisable. "Clear" could mean the notice avoids the cluster's column, or the cluster is held below the notice while it shows. Those are different placements and neither is ruled.
Options: (i) notice below the strip, full width, and the cluster's `minY` follows the notice's measured bottom while it shows; (ii) notice below the strip, bounded to leave the cluster's column free (text wraps to more lines); (iii) the notice replaces the strip's row while it shows. I would pick none without the planner.

**Item 8, the entry map follows the basemap and Night Maps.** Written decision to the contrary at `CartographyEntryReportScreen.kt:227-234` (above). The dispatch says stop. Options: (i) leave as is, and record that the entry map's basemap is per-entry by design, changing the dispatch's ruling; (ii) seed `entryMapMode` from the Maps tab's `mapMode` on open, keeping the entry's own picker and not writing back (the leak the comment fixed was write-back), which needs the planner to say the seed is wanted; (iii) follow Night Maps only, leaving the basemap per-entry. Whether Night Maps already reaches the entry map is separate: `night = night` is passed (`:481`); I have not established why the device check saw day rendering in all six combinations and did not look, since the item stops.
