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

## Results (appended after building; the sections above are unchanged)

### What landed

Commits on `chrome-follow-ups` (first pushed to `chrome-follow-ups-wip`): `b586eb6` pre-registration (on `journal-redesign`), the tests-first commit, and the forward commit; hashes are in the hand-back. Items 1, 3, 4, 5, 6 and 7 are built; items 2 and 8 are stops (above).

| # | Change | File:line at the forward commit |
|---|---|---|
| 1 | `overMap = selectedTab == OFFLINE_MAPS && isShortLandscapeJournal()` | `ui/log/RecordsTab.kt` (the `RecordDetailsSheet(` call's `overMap`) |
| 4 | `BackHandler(enabled = suggestionsOpen)` composed **after** the `ExposedDropdownMenuBox` | `ui/availability/AvailabilitySearchUi.kt` (end of the species field's `Column`) |
| 7 | `CartographyEntry.keepsHighlightableRecord`; the menu offers Show only with it, Hide whenever shown | `domain/model/CartographyEntry.kt`; `ui/log/CartographyEntryReportScreen.kt:424` |
| 5 | new `rowPadding` on `CentrePinLocationPickerOverlay`, applied to the row only; the Maps tab passes `controlsPadding`, and the nav-bar bottom inset also applies in the rail layout | `ui/map/CentrePinLocationPicker.kt`, `ui/availability/AvailabilityCompactMapUi.kt` (the overlay's two call sites and `centrePinConfirmBottomInset`) |
| 6 | Maps-tab snackbar takes `WindowInsets.navigationBars` bottom padding | `ui/availability/AvailabilityCompactScaffold.kt` (`snackbarHost`) |
| 3 | `MapChromeSheetNavigationBar()` sets `isNavigationBarContrastEnforced = false` on the sheet's dialog window, in both bottom sheets (the Records one only over a map) | `ui/map/MapChrome.kt` (end), `MapLayersSheet.kt`, `RecordDetailsSheet.kt` |

### Tests first, seen failing at base for the stated reason (`a3221b4` plus the tests; `/tmp/cf-base.log`, JUnit XML read)

64 tests ran in the four filters, 7 failed, exactly the predicted set:
- item 1: `MapChromeRecordsPortraitTest`, `MapChromeRecordsWideTest`: "record-details-sheet: container alpha expected:<1.0> but was:<0.8>". `MapChromeRecordsShortLandscapeTest` passed at base and passes after, as pre-registered: it cannot tell base from fix.
- item 4: `MapChromeSuggestionsBackPortraitTest`, `...ShortLandscapeTest`: "one Back closed the suggestions expected:<0> but was:<1>".
- item 7: `JournalEntriesOnMapPortraitTest`, `...ShortLandscapeTest`: "no Show on map expected:<0> but was:<1>".
- item 5: `MapChromePinRowLandscapeTest`: row `[0,320][823,384]` dp against the rail `[743,0][823,384]` dp.
`the report menu still offers Hide from map on a shown entry that keeps nothing` and `...offers Show on map on a saved entry that keeps a record` passed at base and pass after: they pin decisions and were not expected to fail.

### Item 4 took three attempts; the record of how

1. A `BackHandler` above the `ExposedDropdownMenuBox`: still failed.
2. The same inside the box's content: still failed.
Two failed fixes, so no third guess. Data instead (a temporary diagnostic test and temporary `println`s, all removed before the commit; `grep DIAG app/src` shows none): after typing, Back reached none of this screen's handlers (the home handler printed on the same Back before typing, none printed after; the search panel's did not; mine did not), and the ViewModel dismiss was never called. `javap` on `material3-android-1.5.0-alpha26` showed `ExposedDropdownMenuBox` calling `androidx.compose.material3.internal.BackHandler` at the end of its body, after its content. That handler answers with `onExpandedChange(false)`, which this box passes as a no-op. The third variant, composed after the box, is the one that passes. **What I did not establish:** that on the S22 the same ordering is what swallowed Back. The Robolectric finding and the bytecode order agree, and the device symptom (three Backs, nothing) fits, but the device is where it is confirmed. Item 4's fix is therefore Robolectric-verified, device-check advised.

### Revert checks (`/tmp/revert.sh`: saves a copy before editing, restores from that copy, runs the classes, reports compile errors, then confirms the file equals HEAD)

| item | one-line revert | build log compile errors | failures read from the XML | forward present after |
|---|---|---|---|---|
| 1 | drop `&& isShortLandscapeJournal()` | 0 | Portrait and Wide: "container alpha expected:<1.0> but was:<0.8>" | yes |
| 4 | `BackHandler(enabled = false)` | 0 | both: "one Back closed the suggestions expected:<0> but was:<1>" | yes |
| 5 | drop `.padding(rowPadding)` | 0 | "the row DpRect(left=0.0.dp, top=320.0.dp, right=823.0.dp ...) does not lie over the rail DpRect(left=743.0.dp ..." | yes |
| 7 | `(entry.shownOnMap \|\| entry.keepsHighlightableRecord)` becomes `true` | 0 | both: "no Show on map expected:<0> but was:<1>" | yes |

Each failure is one that its own revert could produce. Items 3 and 6 have no test and so no revert check.

### Full suite

`./gradlew :app:testDebugUnitTest` from a cleared `app/build/test-results`, `LC_ALL=C.UTF-8`, at the forward commit's tree: **BUILD SUCCESSFUL in 3m 15s**. From the JUnit XML: **319 result files, none older than the run's start; 2578 tests, 0 failures, 0 errors, 24 skipped.** I authored 6 new test methods (3 in `JournalEntriesOnMapScreenTest`'s compact harness, run in 2 window classes; 2 in `MapChromeSuggestionsBackTests`, run in 2; 1 in `MapChromePinRowLandscapeTest`), which the runner counts as 11 new test runs, and rewrote one existing method (`MapChromeRecordsTests`, run in 3 classes). I did not run the base suite, so the exact growth is **unverified**; the planned-trips report gives 2567 at `b427a66`, a different base, so 2578 minus 11 is not a claim. The dispatch predicted 10 to 20 for the eight items; two are stops and two are device-only, so 11 runs is inside that range for what was built, which is a coincidence rather than a check.

### Device-only, for the S22 (Robolectric reports zero insets; none of these is proved by the green suite)

- **Item 3, a bottom sheet's navigation-bar band.** Hypothesis-driven: the cause was not established in the device check, and I could not establish it here. The change turns off the contrast scrim (`isNavigationBarContrastEnforced`); the other candidate, the window's navigation-bar colour, is ignored for a target of API 35+. **If the band is still flat (20, 19, 18) with the sheet up, the cause is the second one and this change does nothing.** Pass: with the Layers sheet, and the Records sheet from the Offline maps panel in short landscape, the band under the sheet shows the map at 0.8 like the rest of the sheet. In portrait the Records sheet is solid now, so its band should read solid: that is the correct result there, not a failure.
- **Item 5, landscape pin row.** The rail half is tested (bounds); the device check is the system-bar half: the row's ends clear the rail and the navigation bar in both landscape rotations (90 and 270), and the pin still sits at the map's true centre (it must not have moved; `rowPadding` does not touch the pin's frame).
- **Item 6, portrait snackbar.** The snackbar sits above the system navigation bar. **Flag:** on the Maps tab the floating bottom nav is also at the foot, so a snackbar just above the system bar can lie over the floating nav; the ruling says "above the system navigation bar" and I did exactly that. If the owner wants it above the floating nav as well, that is a placement nobody has ruled.
- **Item 4, on the phone:** one Back with the suggestions up closes them and leaves the search panel.
- **Item 1, on the phone (short landscape, Offline maps panel):** the sheet shows the picker map through it; and in portrait it is solid.

### Not tested

The wide tree's Offline maps sheet is asserted solid (I read "only in short landscape" literally, and the wide tree's panel is stacked); if the owner meant the wide tree to keep 0.8, that is the one place my reading could be wrong. Nothing tests `MapChromeSheetNavigationBar`'s window call: under Robolectric its `SideEffect` runs harmlessly. The Maps-tab snackbar's padding is untested by construction.

### Decisions I made

1. **Item 1, wide tree:** solid (literal reading of "only in short landscape").
2. **Item 7, what "highlightable" means:** any kept track, find, waypoint or offline-region decision, or any attached photo, i.e. the domain's own definition of "what an entry keeps" (`GetJournalEntryHighlightsUseCase.kt:44-75`), not a check that the record is drawn today. A photo with no location counts as kept. A different reading (require a drawn record) needs the map's live data at the menu, which the report screen does not have.
3. **Item 7, an entry already shown that keeps nothing** still offers "Hide from map", so a shown entry can always be hidden. No new copy.
4. **Item 5, the bottom inset in the rail layout** now uses the navigation-bar inset outside fullscreen (before, only in fullscreen), because no bottom nav is measured there.
5. **Items 2 and 8 stopped**, per the dispatch's stop rule; nothing was built for either.
6. **Item 3 was built on an unestablished cause**, because the dispatch lists it as device-only and asks for a fix against the documented APIs; the report says so and lists the failing case.

### Flags outside scope

- The species suggestions' `onExpandedChange = {}` is what makes M3's own Back handler a no-op. Anything else in the app that uses `ExposedDropdownMenuBox` with a no-op `onExpandedChange` will swallow Back the same way; I grepped only this file's use.
- `GetJournalEntryHighlightsUseCase` and `CartographyEntry.keepsHighlightableRecord` state "what an entry keeps" twice; a change to one must change the other. I did not refactor the use case (out of scope).
- The run record's flag 10 (the trip dialog at 0.815) and the light-theme gap are not touched.
- A stray Bash `pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'` in this shell matched another session's `until ...` wait loop, so it reported a build when none ran; I confirmed with `ps` that no Gradle worker was running before each build. Another session's Gradle and Kotlin daemons were idle.

---

# Resumed (planner continuation `2026-09-29-12`, record `2026-09-28-129`; a fresh coder)

## Who and what I ran on

- Model: this session is configured for `claude-sonnet-5-5` (my system prompt's model line). I did not verify the serving model.
- Worktree `/home/zynergy-labs/Zynergy/forager-wt/chrome-follow-ups`, branch `chrome-follow-ups`, at `881cfbd` (the first coder's report commit); I pulled `origin/journal-redesign` with `--no-rebase` (fast-forward to `1d513e1`, planner files only in `prompts/` and `RECORD.md`; no `app/` change since `881cfbd`'s merge base).
- `CLAUDE.md` read at the base. Nothing in the dispatch conflicts with it.

## The governing texts, verbatim

`prompts/preserved/2026-09-29-12.md` (the continuation), below its "verbatim message follows" line, at `1d513e1`:

> Planner message `2026-09-29-12` (record `2026-09-28-129`), part of dispatch `2026-09-28-104`. Quote it verbatim in your report. It answers your two stops at `881cfbd` and your flag on item 6.
> 
> **Item 8, the entry map's basemap. The owner, verbatim: "1 B".** As the planner put it: "When an entry map opens, start it on your Maps-tab basemap. You can still change it for that entry, and changing it doesn't affect the Maps tab."
> - Seed `entryMapMode` from the Maps tab's `mapMode` when the entry map opens. There is no write-back, so the comment's state-leak fix stays intact. Update the comment at `CartographyEntryReportScreen.kt:227-234` to say so.
> - Night Maps is already passed (`:481`). **Also find why the device check saw day rendering on the entry map at night.** Report the cause with file:line. If it is a separate bug, fix it only if the fix is confined to passing or honouring `night` on the entry map; otherwise report it.
> - **Tests first:** an entry map opened after the Maps tab is set to a non-default basemap starts on that basemap. Changing it there leaves the Maps tab unchanged.
> 
> **Item 2, the search notice. The owner, verbatim: "Option A".** As the planner put it: "The red notice slides in just below the compass strip. While it's showing, the icon column on the right keeps below it rather than overlapping."
> - On the Maps tab the notice is placed below the compass strip, using the strip's **measured** height, not `compassStripClearance`.
> - While it shows, the cluster's top limit (`minY`) follows the notice's measured bottom, and the cluster moves down if it would overlap.
> - Placement only; no copy.
> - **Tests first,** in portrait and `w823dp-h384dp-land`: a notice is fully below the strip and does not intersect the cluster, and the cluster returns to its limit when the notice clears.
> 
> **Item 6, the planner's ruling on your flag:** on the Maps tab, the snackbar sits above the floating bottom navigation (and above the rail's foot in landscape), not only above the system bar. That is the standard placement. Add a test that it does not intersect the bottom nav.
> 
> Revert checks for each, the full suite on your final tree at 0 failures, push to `journal-redesign` with `--no-rebase`, and a new "Resumed" section. Before each Gradle run, check that no other build is running and that 2.5 GB is available; the photo-export and backup coders build here. Hand back to `[9b334a]`.

The launch prompt this session was opened with (it is also `prompts/preserved/2026-09-29-13.md`), "The dispatch" section:

> You are a **fresh coder** continuing dispatch `2026-09-28-104`. The first coder built six of eight items (`881cfbd`) and stopped on two. That window is now busy with other work, so it is not yours to message.
> **What governs:** read in full, on `origin/journal-redesign`: `prompts/preserved/2026-09-28-104.md` (the dispatch); `prompts/preserved/2026-09-29-09.md` (its launch prompt); **`prompts/preserved/2026-09-29-12.md`** (the continuation you build), which governs. Also read the report ... Quote `-2026-09-29-12` and this prompt verbatim in a new "Resumed" section.
> **Worktree:** `/home/zynergy-labs/Zynergy/forager-wt/chrome-follow-ups` (branch `chrome-follow-ups`). **Work only there.** Pull `journal-redesign` with `--no-rebase` first.
> **Build (from `-2026-09-29-12`):** **Item 8** (the owner, verbatim "1 B"): seed `entryMapMode` from the Maps tab's `mapMode` when the entry map opens, no write-back; update the comment at `CartographyEntryReportScreen.kt:227-234`; find, with file:line, why the entry map rendered in daylight at night, and fix it only if the fix is confined to passing or honouring `night`, otherwise report it. **Item 2** (the owner, verbatim "Option A"): on the Maps tab place the search notice below the compass strip, using the strip's measured height; while the notice shows, the cluster's `minY` follows the notice's measured bottom; placement only, no copy. **Item 6** (the planner's ruling): on the Maps tab the snackbar sits above the floating bottom nav, and above the rail's foot in landscape.
> **Tests first** for each, in portrait and `w823dp-h384dp-land` where relevant, seen failing at base. Then revert checks, the full suite on your final tree at 0 failures, and a push to `journal-redesign`. Broken work goes on `chrome-follow-ups-wip`. No phone. Merge is not authorised.

(The role-and-rules block of the launch prompt, and its hand-back instructions, are those of `prompts/preserved/2026-09-29-09.md`, which is quoted in this report's first section by the first coder.)

## Premises checked at the base

- **Item 8's written decision:** `ui/log/CartographyEntryReportScreen.kt:227-234` and the state at `:299-302` say `entryMapMode` is local and independent of the Maps tab's `mapMode`, fixing a leak that came from *writing back*. The owner's "1 B" seeds it on open with no write-back; the leak fix stays intact.
- **Where the Maps tab's `mapMode` is:** `AvailabilityScreen.kt:894` (`var mapMode ... MapMode.DEFAULT`). It reaches `LogPanel` (`:1416`) and the compact scaffold (`mapMode` lambda, `:1677`) only as `basemap = mapMode.basemap` (`:895`); `JournalTab.kt:139` and `LogPanel.kt:117` take `basemap: Basemap`; `CartographyScreen` and `CartographyEntryReportScreen` take neither. `MapMode` and `Basemap` are one to one (`map/MapMode.kt:28-31`), so the seed needs `mapMode` threaded, not a lookup.
- **Where `night` goes:** `AvailabilityScreen.kt:902` (`isNightMode = uiState.nightModeMaps`) to `LogPanel`/`JournalTab` (`night = isNightMode`, `:1426`, scaffold `:1031`), to `CartographyScreen` (`JournalTab.kt:638`, `LogPanel.kt:470`), to `CartographyEntryReportScreen` (`CartographyScreen.kt:422`), to the map's `MapRenderMode(night = night)` (`CartographyEntryReportScreen.kt:481`). Every hop is a direct pass, and `MapRenderMode.nightModeLoaded` defaults to `true` (`map/MapSlot.kt:33`), so the entry map is never held by the cold-launch gate. The cause of the daylight rendering is therefore **not** established by reading; a test below pins what the code hands the map (see the pre-registration).
- **Item 2's layout:** the notice is composed in the search bar's `Column` (`AvailabilityCompactScaffold.kt:975-1001`, `SearchNotice(uiState, overMap = true)` at `:1014`), which is `CompactMapTab`'s `searchBarSlot` (`AvailabilityCompactMapUi.kt:353`, composed at `:694`); the compass strip is drawn later in the same Box at `topInset` (`:1107-1123`), so the notice's first line lies under it. The strip's own height is not measured anywhere (`compassStripClearance` is a text-line height, `:638-642`). The cluster's top limit is `dropdownTopPx = (topInset + compassStripClearance)` (`:781`), used by `clampMapIconBarVerticalOffset` (`:804-841`), whose upward bound is coerced to at most 0 (`:818-822`), so the clamp cannot move a cluster down to clear anything. That coercion is why "the cluster moves down if it would overlap" needs a positive lower limit while a notice shows.
- **Item 6's cause:** the Maps-tab snackbar takes only `WindowInsets.navigationBars` bottom padding (`AvailabilityCompactScaffold.kt:627-637`); the floating bottom nav is inside `CompactMapTab`'s Box, not the Scaffold's `bottomBar`, so nothing reserves its height for the snackbar. Its measured height is already kept: `bottomNavHeightPx` (`:520`), `bottomNavHeight` (`:544`), zero in the rail layout.

## Pre-registration (written before any build or test run)

New tests are in `MapChromeResumedTest.kt` (new), driving the real `AvailabilityScreen` through `MapChromeTestScreen`; I gave `MapChromeTestScreen` an optional `mapSlotOverride`, and `ForagerBottomNav` a test tag (`COMPACT_BOTTOM_NAV_TAG`), both inert. Robolectric reports zero insets, so item 6's system-bar half and item 2's status-bar half are device-only.

| test | predicted at base | pass condition after |
|---|---|---|
| `MapChromeEntryMapTest`: Maps tab set to Street through the Layers sheet, then Journal: the entry map's `basemap` | **fails**: `OPEN_TOPO_MAP` (`CartographyEntryReportScreen.kt:302` seeds `MapMode.DEFAULT`) | `OSM_STANDARD` |
| same, Maps tab left on its default | passes (guard: the seed changes nothing when the tab is on its default) | passes |
| same, entry map fullscreen, Layers, Satellite: the entry map is `USGS_IMAGERY_ONLY` and the Maps tab is still Street | **passes at base** (the no-write-back guard; it can only fail if the seed is built as a write-back) | passes |
| Night Maps on and loaded: the entry map's render mode has `night == true` | **passes at base** if the plumbing is right (the diagnosis test). If it fails, that is the cause | passes |
| `SearchNoticePortraitTest`: strip and notice | **fails**: the notice starts at the bar's bottom, where the strip is drawn, so the two overlap | notice top at or below the strip's bottom |
| `SearchNoticePortraitTest`: cluster dragged to its top limit, then a notice, then cleared | **fails**: the cluster's top limit is `topInset + clearance` and the notice is full width, so they intersect | cluster top at or below the notice's bottom, and back at its limit when cleared |
| `SearchNoticeLandscapeTest`: notice vs strip (different corners) and vs the cluster; cluster back when cleared | **fails on the cluster**: the cluster's top (60 dp) is above the notice's bottom under the 45 dp bar; the strip half passes | no intersection; cluster returns to 60 dp |
| `SnackbarPortraitTest`: snackbar vs the bottom nav | **fails**: the host is at the Scaffold's bottom edge and the nav is at the same edge | snackbar bottom at or above the nav's top |
| `SnackbarNarrowLandscapeTest` (`w640dp-h360dp-land`): snackbar vs the rail | **predicted to fail** (a 600 dp snackbar centred in 640 dp spans 20 to 620; the rail is 560 to 640); if it passes at base that is a stop | no intersection |
| `SnackbarShortLandscapeTest` (`w823dp-h384dp-land`): the same | passes (a guard: the snackbar's 600 dp cap keeps it off an 80 dp rail at 823 dp) | passes |

Predicted counts: the suite grows by about 11 test methods (4 + 2 + 1 + 1 + 1 + 1 + 1).

### Revert checks planned

Each restores from a copy saved before editing, checks the build log for compile errors first, and confirms the forward change afterwards: the seed (item 8) back to `MapMode.DEFAULT`; the notice spacer (item 2) removed; the cluster's notice bound (item 2) removed; the snackbar's bottom-nav padding (item 6) removed.
