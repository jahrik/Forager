# Tablets as a big phone: completion report (dispatch 2026-09-28-245, amended by -246)

Coder: Sonnet 5.5 (`claude-sonnet-5-5`), read from the session's system prompt; not verifiable any other way. Base `5bad4f5b` (origin/journal-redesign, confirmed after `git pull --no-rebase`), branch `tablet-as-phone`. Pre-registration: `docs/audits/2026-09-30-tablet-as-phone-preregistration.md` (pushed at `ea0439ca`, Part B at `6afd818a`, before the code it predicts). `j6-rnd` was not touched. Nothing was merged into `journal-redesign`. No device, no `adb`.

## What landed

| Commit | What |
|---|---|
| `ea0439ca` | Pre-registration and the failing guard tests (`AvailabilityScreenTabletAsPhoneTest`). |
| `25a839ca` | WIP push (not compiled): phone tree at every size, tablet tree and tests removed by hand. |
| `8f8708b9` | Compiles; landscape gate; tablet-only search/settings code and stale comments removed; 10 new tests pass. |
| `6afd818a` | Part B pre-registration and the failing `LandscapeLFullscreenCycleTest`. |
| `291e682a` | Part B fix. |
| `d7110b6d` | `AvailabilityScreenInAppCameraTest`: the two 700 dp tests reworked. |
| `59e5e76d` | The guard's tap tests strengthened after the base run showed they passed at the base. |

Part B is in its own commits, after Part A's.

## Full suite

`./gradlew :app:testDebugUnitTest --offline --continue` on `59e5e76d`: 376 classes, **3082 tests, 0 failures, 0 errors, 24 skipped**, `BUILD SUCCESSFUL`, no compile errors in the log. The 24 skipped are the suite's existing `@Ignore`s (the planner's figure at `24b1aac0` was also 24); I did not add or remove one. Both owner-held flakes (`JournalPendingDeleteTest` album corner long-press, `JournalTabTest` From Album photo pull) passed in this run and were not touched. I did not measure a full-suite count at `5bad4f5b`, so I cannot state a before-and-after difference in total; the per-file counts below are `@Test` lines at `5bad4f5b` against `HEAD`, and an abstract test class inherited by several subclasses runs more executions than it has `@Test` lines.

An earlier full run, before the camera test was reworked, had exactly one failure, `AvailabilityScreenInAppCameraTest` "the camera survives the width-class flip a rotation causes", predicted in the dispatch.

## Premise checks (at `5bad4f5b`)

- Confirmed: the four tree-choice sites, `AvailabilityScreen.kt:1187, :1211, :1305, :1882` (all `windowWidthClass == COMPACT || isShortWindow`); `ShortWindow.kt:14` `SHORT_WINDOW_MAX_HEIGHT_DP = 480`; the shared cluster stays; removal by hand.
- **Wrong: "`WindowWidthClass` goes if nothing else reads it; SeasonalTab's 640 dp cap goes with it."** `AvailabilityResultsUi.kt:245-251` (`SeasonalTab`) reads it on the phone path. A phone in landscape is `w823dp`, so `MEDIUM`, and gets the 640 dp cap. `AvailabilityScreenLandscapeB3DestinationsTest.kt:185` asserts that cap on the phone, and `AvailabilityCompactScaffold.kt:775` uses `READABLE_CONTENT_MAX_WIDTH` itself. Removing it changes a phone outcome, which Part A item 4 forbids. **Left in place: `WindowWidthClass.kt`, `currentWindowWidthClass`, the cap.** It is the planner's call whether the phone's landscape cap goes (it would change a phone test's outcome).
- Stale in the amendment: `AvailabilityCompactScaffold.kt` ~:896 and ~:930 as readers. The scaffold's readers are `:179` (parameter), `:550` (`showRail`) and `:930` (`railPortEdge = if (showRail) portEdge else null`).
- Not verified at the base: premise 5 (no migration, DataStore key, backup format) was not contradicted by any file touched; I did not grep for one.

## Readers of `isShortWindow` / `isShortLandscapeWindow` (amendment item 2), at w1318dp-h824dp-land

| Reader | Before | Now |
|---|---|---|
| `AvailabilityScreen.kt` four tree sites | picked the tree | gone; the phone tree always |
| `AvailabilityScreen.kt:1111` `isLandscapeWindow` (was `isShortLandscapeWindow`, short and landscape) | short and landscape | landscape (`isLandscapeWindow()` in `ShortWindow.kt`); true |
| `AvailabilityCompactScaffold.kt:550` `showRail`, `:930` the L's `railPortEdge` | the parameter | rail, no bottom nav, the L |
| `AvailabilityScreen.kt:1523` drawer direction | flips to the port edge in short landscape | flips in landscape |
| `AvailabilityOfflineMapsUi.kt:297` side-by-side | short and landscape | landscape |
| `JournalShortWindow.kt:61-64` `isLandscapeJournal` (was `isShortLandscapeJournal`; readers `JournalTab.kt:593`, `RecordsTab.kt` overMap) | short and landscape | landscape |
| `JournalTab.kt:694` entry columns | short: 2 | short or landscape (`isShortOrLandscapeWindow()`): 2 |
| `CartographyEntryReportScreen.kt:376` map cap | short: 40% of `screenHeightDp` | **unchanged.** It is a height budget (its own doc, `:747`), which the amendment says to report rather than change. At h824 it does not apply. |

Also read, not in the amendment's list: `AvailabilityCompactScaffold.kt:~807` `journalHidesSearchHeader = showRail && Journal && !revealed`. Its comment says "height is the scarce axis there", so it may be a height budget too. It follows `showRail`, so at a landscape tablet the Journal hides the app search header until its icon reveals it. I did not change it; it is a flag.

## Deleted (with the check that no phone code calls it)

Method: after each deletion, `git grep` of the symbol in `app/src/main` outside comments returns nothing, and `/tmp` scripts (top-level and member-level) list any name whose reference count went from more than one at `ea0439ca`'s base to one or none (empty at the end).

Files: `ui/availability/AvailabilityWideLayoutUi.kt` (`CombinedResultsPane`, `WideMapControls`, private `MapTab`, private `ThreeWayActionDialog`, `PERMANENT_DRAWER_WIDTH`, `COMBINED_PANE_*`, `WIDE_LIST_PANE_TAG`, `THREE_WAY_ACTION_DIALOG_TAG`); `ui/log/LogPanel.kt`; `ui/log/JournalDetailSlot.kt` (`JournalDetailSlot`, `JournalDetailLayer`, `JournalDetailPriority`, `JournalDetail`, `JournalDetailPane`, `JOURNAL_DETAIL_PANE_TAG`).
In files kept: `AvailabilityScreen.kt` (`mainScaffold`, `drawerSheetContent`, `DrawerPanel` and `drawerPanel`, `expandAdvancedSearchRequested`, `journalDetailSlot`, `windowWidthClass` reads, `wideResultsMapShown`, `usesCompactTree`, the `PermanentNavigationDrawer` branch); `AvailabilitySearchUi.kt` (`ActiveSearchSummary`, `AvailabilitySearchTopBar`, `RegionControls`, `WIDE_SEARCH_SUMMARY_TAG`, and from `SearchControls` the parameters `includeAdvancedSearch`, `includeRecentSearches`, `expandAdvancedSearchRequested`, `onAdvancedSearchExpandConsumed`, `distanceUnit`, `currentTime`, `onUseCurrentLocation`, `onManualLatChanged`, `onManualLngChanged`, `onSearchManualCoordinates`, `onRadiusChanged`, `onMonthSelected`, `onRecentSearchSelected`, which only the tablet call passed a live value to); `AvailabilitySettingsUi.kt` (`MushroomLogEntryRow`, `SettingsHeader`; `CompactToolsDrawerContent`'s `currentTime`, now unused); `RecordDetailsSheet.kt` (`RecordDetailsPane`, `RECORD_DETAILS_PANE_TAG`); the `detailSlot` parameter and non-null branches in `CartographyScreen.kt`, `JournalTab.kt`, `RecordsTab.kt`. **The null branches are the phone's path and were kept as they were** (`RecordsTab` sheet path, `CartographyScreen` in-place entry, `JournalTab` find overlay).

Tests deleted, with the symbol each covered (`@Test` lines, base to now): `WideJournalTest` 29 (LogPanel and the detail pane), `WideMapControlsTest` 17 (`WideMapControls`), `WideMapTabsTest` 8 (the tablet's map tabs), `WideChipRowClusterGuardTest` 2, `LogPanelTest` 6 (`LogPanel`), `JournalDetailPaneTouchTest` 1, `MapChromeOverMapTest` 11 (`MapChromeWideTest`, plus 4 inherited runs of `MapChromeRecordsWideTest`; the records class lost its `wide` parameter), `AvailabilityScreenAdaptiveLayoutTest` 7 (the wide window's drawer, tabs, three-way dialog, night pin, Discard snackbar), `TrackDeleteTest` 3 (the three pane tests), `AvailabilityScreenMapLayersTest` 3 (the wide Layers class), `LeavingTheJournalFixesTest` 3 (F1 and F3 wide), `AvailabilitySearchSummaryCopyTest` 3 (the wide summary), `MapChromeColourTest` 2 (the wide colour classes, x2 dark/light), `AvailabilityScreenMapBubblesTest` 2 (the wide bubble class), `JournalEntriesOnMapScreenTest` 2 (the wide class), `MapChromeColourPixelsTest` 1 (the pane pixel class, dark and light), `RecordDetailsSheetTest` 1 (the wide pane), `EntrySaveFailureShownTest` the wide subclass (11 inherited executions, no `@Test` line of its own).
Kept: `RecordsChipRowScrollTest` (shared chips at w1500), the Seasonal cap test in `AvailabilityScreenAdaptiveLayoutTest` (it now runs on the phone tree at w840, and the cap is kept).
Moved onto the phone path: `LogPanelTest`'s "Change Location on an already-located entry" is now in `JournalTabTest` (no phone test covered it; the other five `LogPanelTest` cases have named twins in `JournalTabTest` and `JournalPendingDeleteTest`). `MarkerFanOutPlacementTabletTest` (w1318 mdpi) was not deleted: it now runs the phone tree's landscape layout and passes.
**Reworked, not deleted:** `AvailabilityScreenInAppCameraTest`'s two 700 dp tests (`:233`, `:257` in the base). They no longer swap trees, so the "flip happened" assertion (`Tools` count 0) became "nothing swapped" (count 1); they still assert that a width change with the camera open leaves it open, that its photo routes, and that Back closes it. Renamed "survives a width change" and "changing the width back keeps the camera too". Reason: the width-change-with-camera-open claim is still meaningful; only the mechanism (a tree swap) is gone.

## New tests

`AvailabilityScreenTabletAsPhoneTest`, 10: a portrait tablet (w824dp-h1318dp) and a 600-839 dp width (w673dp-h841dp) draw the phone tree (bottom nav, no rail, compact map, no permanent drawer); a real touch on the bottom nav's own "List" item switches the tab; at w1318dp-h824dp-land the rail, no bottom nav, no permanent drawer; the L is drawn (bar 48 by 240, cluster 296); a real touch on the rail's "List" item switches the tab; real long-presses along the rail's inner edge and mid-map reach the map (sampled, at least 4 points); the L's top never above the search bar's bottom at rest and dragged to the top, at ROTATION_90 and ROTATION_270; and a control (the phone in landscape at w823dp-h384dp) so the L and rail lookups are shown able to find them.
`LandscapeLFullscreenCycleTest`, 3 (Part B).

### Revert checks (all from saved copies in `/tmp`, never from git; each build log checked: 0 compile errors, and the result file's time matched the run)

1. **Base production, my tests** (`git checkout ea0439ca -- app/src/main` after saving the forward tree, restored from the saved copy, `git status` clean after): 9 of 10 fail, the control passes. Messages: `The component with TestTag = 'compact-bottom-nav' is not displayed!` (both portrait guards); `The component with TestTag = 'compact-navigation-rail' is not displayed!`; `the L's bar is 240 tall: five 48 dp rows expected:<240.0> but was:<256.0>` (the portrait cluster of the wide tree); `Failed to assert the following: (Selected = 'true') ... Expected exactly '1' node` (both tap tests, after they were strengthened). **A first version of the two tap tests passed at the base** (`List` matched the wide tree's own tab row); this run showed it, and they now find the item inside `compact-bottom-nav` / `compact-navigation-rail` and assert the touch point lies in it (`59e5e76d`). Also a first run of my own prediction was wrong on that test (predicted fail, observed pass), recorded in the commit.
2. **Landscape gate back to short-and-landscape** (`isLandscapeWindow()` `&& screenHeightDp < 480`, `ShortWindow.kt`): the 6 landscape-tablet tests fail and nothing else does, with `compact-navigation-rail is not displayed!`, `... 240 tall ... but was <256.0>` and `dragged to the top the L rests at the search bar's bottom 45.0 expected:<45.0> but was:<61.0>`.
3. **Part B, the limit back to the animated `topInset`**: `LandscapeLFullscreenCycleTest` 3 of 3 fail with `after a fullscreen cycle: the L's top 44.0 is not above the search bar's bottom 45.0`; forward restored, 3 of 3 pass and `LandscapeLRulingsTest` (18) still passes.

## Part B: A1 item 1

Diagnosis (file:line, pre-registered before the fix): `topLimitPx` is the animated `topInset` (`AvailabilityCompactScaffold.kt:387-392, :974`; `AvailabilityCompactMapUi.kt:641`), and the cluster's re-clamp `LaunchedEffect` (`AvailabilityMapIconCluster.kt:495`) is keyed on `isFullscreen` and others but **not on the limit**. On leaving fullscreen it re-clamped against the animation's first frame (near 0) and the L settled at its centred top, 44 dp against a 45 dp search bar bottom in the 384 dp window. Reproduced under Robolectric through real touches on the Fullscreen and Exit fullscreen rows (and the Back key) with the exact predicted message before any fix. It fits every observation in S22-A's relay (`4bd3be31`): stays at 247 px in fullscreen, 200 px after exit, same by Back, nothing after 8 s, a rotation restores it.
Fix: `CompactMapTab` takes `searchBarBottom: Dp = topInset` and the landscape L's limit reads it; the scaffold passes the settled `searchBarHeight`. Portrait's limit is untouched. The fullscreen behaviour is the same as before (the L stays put while the bar is hidden).
Not reproducible headless: the 47 px figure (density and insets), and whether the map's shift between S22-A's captures is related (the relay says it was not isolated). Not verified on a device.

## Decisions I made (the planner may overrule any)

1. Left `WindowWidthClass` and SeasonalTab's 640 dp cap in place (wrong premise; the cap is a phone rule). See above.
2. `isLandscapeJournal()` (the Journal's sideways header, second row, cards, album columns) follows landscape, not short. The amendment listed only the Journal's columns; the rest of J5 is the same gate and its own comment calls it "the same two tests B1-B3 use". Its comment says height is the scarce axis, so this is the reader most likely to want a different answer on a tall tablet.
3. `CartographyEntryReportScreen.kt:376` left as short (a height budget).
4. Part B's fix keeps the L still while the bar is hidden (option: key the effect on the limit, which would centre the L in fullscreen at 384 dp and glide it back on exit). Chosen because it changes no visible fullscreen behaviour. Rejected: the key, for that change.
5. Removed the tablet-only parameters of `SearchControls` and `CompactToolsDrawerContent.currentTime`, and `RegionControls`, which became unreachable with the tablet call; the phone call passes what remains. No phone test called `SearchControls` directly.
6. Kept `MarkerFanOutPlacementTabletTest` (passes on the phone tree).
7. Kept the `SeasonalTab`-cap test in `AvailabilityScreenAdaptiveLayoutTest` (runs on the phone tree).

## Not done, and why

- The 640 dp cap and `WindowWidthClass` (wrong premise).
- **Unused parameter `onReopenTaxonSuggestions`** on `AvailabilityScreen`: nothing in the phone tree reads it now (only the tablet's summary tap did), but it is a required parameter, so removing it edits every screen test and `MainActivity`, and the ViewModel function behind it. Left, flagged.
- Historical KDoc that names removed things in files I did not edit (about 14: `CartographyEntryListScreen`, `CartographyViewModel`, `EntriesAlbum`, `FindsGalleryScreen`, `JournalScreenState`, `LogEntryDetailScreen`, `MushroomLogViewModel`, `PhotoViewerDialog`, `InAppCameraHost`, `CrashLogPanel`, `AvailabilityMapOverlaysUi`, `AvailabilityTripsWaypointsUi`, `CentrePinLocationPicker`, `MapChrome`, `Theme`). The dispatch asks for comments corrected in files I edit, which I did; these are dead links, not behaviour. Records and J6's report are not edited.
- The MapIconCluster's nullable `landscapeBar` / `landscapePill` fallback (`?: bar`) is now always taken with a non-null value from its one caller; the tablet was the caller that passed null. Left, since removing it edits the phone's cluster wiring, which the dispatch says to stop and report rather than do.

## Flags outside scope

- `AvailabilityCompactScaffold.kt:~807` (`journalHidesSearchHeader`): see above.
- Machine checks: the first `testDebugUnitTest` run (the 10 new tests) started with 1992 MB available against the then 2.5 GB limit; the planner's later 2048 MB rule applied to the rest, and every later run started above it. One base-state experiment was begun at 1944 MB and undone (tree restored from the saved copy, `git status` clean) rather than run; it was redone later above the limit. No harm resulted.

## Device-only list

1. **S22 recheck of A1 item 1**: enter fullscreen by tap and leave by tap and by Back, at 90 and 270; the Fullscreen icon must return to its rest y (247 px in S22-A's record), the L's top not above the search panel's bottom. Robolectric has zero insets, so the px figure and the inset-dependent part are device-only.
2. **Tablet smoke check (owner)**: a portrait tablet shows the phone's layout stretched (bottom nav, map, Journal); a landscape tablet shows the rail and the L.
3. **The L's size and position on a real tablet in landscape.** It was sized for a phone (296 dp tall on the rail); whether it looks right on a large screen is the owner's call.
4. On a landscape tablet the Journal hides the app search header until its icon reveals it (the height-scarce rule, now landscape); confirm that is wanted.
5. The Seasonal tab on a landscape tablet is capped at 640 dp beside the rail (the cap kept); confirm.
