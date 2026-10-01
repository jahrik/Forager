# Map chrome at 80% over a map: stopped at verification

Intent `2026-09-28-56`, as amended by continuation `2026-09-28-58` and launched by the planner's message
(launch note `2026-09-28-67`). Dispatch files `prompts/preserved/2026-09-28-56.md` and
`prompts/preserved/2026-09-28-58.md`, quoted in full at the end of this report with the launch message.

**Outcome: stopped at verification.** Nothing under `app/` changed, no Gradle run was made, and no test was
written. Two abort conditions are met:
- `-56`'s "a call site the verification cannot classify": the Records call site of the details sheet
  (`RecordsTab.kt:316`) covers a map in some layouts and not in others, and cannot tell which it is in.
  This is `-56` verification rule 3, "stop and report. Do not choose."
- `-56`'s "an unruled design question": the species suggestions and the Month menu open over other chrome
  that is itself translucent over the map, so "80% over the map" can stack. Nothing rules how a popup over
  80% chrome should be filled.

Every other item is classified below, with its file and line at base, the Material3 parameter (confirmed in
`1.5.0-alpha26`) and how its call site knows. The questions are in "Questions for the planner".

All paths below are under `app/src/main/java/com/zynergylabs/forager/app/` unless they say otherwise. Line
numbers are at `e253b50`.

## Base

- `git fetch`, then `origin/journal-redesign` was at `e253b50`. The worktree
  `/home/zynergy-labs/Zynergy/forager-wt/map-chrome` was cut from it on branch `map-chrome`, and HEAD was
  `e253b50`.
- `git diff --stat 99de6c2 e253b50 -- app` is empty. The only changes from `99de6c2` to `e253b50` are
  `RECORD.md`, `docs/plans/journal-redesign.md` and `prompts/preserved/2026-09-28-68.md`.
- At launch: 9.8 GB free on `/` and about 3 GB of memory available (`free -g`).
- Material3 is `1.5.0-alpha26` (`gradle/libs.versions.toml:19`). There is no sources jar, so I read the
  signatures with `javap` from the `classes.jar` in the cached `material3.aar`.

## Material3 parameters at 1.5.0-alpha26

Read with `javap` from `material3-android-1.5.0-alpha26` `classes.jar`. Each exists.

| Composable | Parameter for the fill | Signature read |
|---|---|---|
| `ModalBottomSheet` | `containerColor` | used today by `MapLayersSheet.kt:247` |
| `Surface` | `color` (with `contentColor`) | used throughout the app |
| `AlertDialog` | `containerColor` | `AndroidAlertDialog_androidKt.AlertDialog-Oix01E0(..., Shape, long containerColor, long, long, long, float, DialogProperties, ...)` |
| `DatePickerDialog` | `colors: DatePickerColors` (its `containerColor`) | `DatePickerDialog_androidKt.DatePickerDialog-GmEhDVc(..., Shape, float, DatePickerColors, DialogProperties, ...)` |
| `Dialog` + `Surface` (waypoint name) | the `Surface`'s `color` | app code |
| `DropdownMenu` | `containerColor` | `AndroidMenu_androidKt.DropdownMenu-IlH_yew(..., Shape, long containerColor, float, float, BorderStroke, ...)` |
| `ExposedDropdownMenu` | `containerColor` | `ExposedDropdownMenuBoxScope.ExposedDropdownMenu-vNxi1II(boolean, Function0, Modifier, ScrollState, boolean, Shape, long containerColor, float, float, BorderStroke, ...)` |
| `Snackbar` | `containerColor` (via a custom `SnackbarHost` content) | `SnackbarKt.Snackbar-sDKtq54(SnackbarData, Modifier, boolean, Shape, long containerColor, long, long, long, long, ...)` |
| `ModalDrawerSheet` | `drawerContainerColor`, `drawerContentColor` | `NavigationDrawerKt.ModalDrawerSheet-afqeVBk(Modifier, Shape, long, long, float, WindowInsets, ...)` |

Also read: `BottomSheetDefaults.SheetMaxWidth` is 640 dp (the static initialiser stores `sipush 640` into
`SheetMaxWidth`). That the modal sheet uses it by default and centres itself is library behaviour I did not
read.

## Verification, item by item

"Always" means every call site of the item is over a map by construction. "Can tell" means the call site
already holds the state that decides it.

### Classified

| Item | At base | How the call site knows | Classification |
|---|---|---|---|
| Details sheet from a map bubble | `RecordDetailsSheet` `ui/log/RecordDetailsSheet.kt:141`, `ModalBottomSheet` `:163-167`; call site `ui/map/MapBubble.kt:312` inside `MapBubbleLayer` (`:239`) | `MapBubbleLayer` is composed only inside a map `Box`: `AvailabilityCompactMapUi.kt:687` (M1), `AvailabilityWideLayoutUi.kt:306` (M2), `ui/log/CartographyEntryReportScreen.kt:485` (M3) | Always, under the reading "its screen shows a map beneath". See Q2 for the geometric reading. |
| Centre-pin confirm row | `CentrePinLocationPickerOverlay` `ui/map/CentrePinLocationPicker.kt:265`, `Surface` `:289-298`, `color = surface` `:295` | Its three callers are inside map `Box`es: `AvailabilityCompactMapUi.kt:1336` and `:1357` (M1), `AvailabilityWideLayoutUi.kt:382` (M2). The file's doc comment says it is for "a `Box` that already has its own `mapSlot(...)` call" (`:64-72`). The picker with its own map (`CentrePinLocationPicker`, `:75`) puts its row below the map, not over it (`:129`). | Always |
| Search notice over the Maps tab's map | `SearchNotice` `ui/availability/AvailabilitySearchUi.kt:579`, `Surface(color = errorContainer)` `:590` | The Maps-tab call site is `AvailabilityCompactScaffold.kt:951`, inside the `searchBarSlot`, which `CompactMapTab` composes only inside its map `Box` (`AvailabilityCompactMapUi.kt:677`). The other call sites, `AvailabilityCompactScaffold.kt:725` (other tabs) and `AvailabilityScreen.kt:1544` (wide, above the map), stay unchanged, as `-58` says. | Can tell (by call site) |
| Wide Layers button | `MapModeToggle` `AvailabilityWideLayoutUi.kt:167`, `Surface` `:172`, `color = surface` `:175`, `tonalElevation = 3.dp` | One caller, `:337`, inside the M2 map `Box` | Always |
| Trip date dialog | `TripDatePickerDialog` `AvailabilityMapOverlaysUi.kt:115`, `DatePickerDialog` `:130` | Two callers, both after a centre-pin pick on a map: `AvailabilityCompactMapUi.kt:1372` (M1) and `AvailabilityWideLayoutUi.kt:432` (M2) | Always |
| Waypoint name dialog | `WaypointNameDialog` `AvailabilityMapOverlaysUi.kt:168`, `Dialog` `:178`, `Surface` `:179`, `color = surface` `:182` | Two callers: `AvailabilityCompactMapUi.kt:1383` (M1), `AvailabilityWideLayoutUi.kt:443` (M2) | Always |
| Three-way action dialog | `ThreeWayActionDialog` `AvailabilityWideLayoutUi.kt:462`, `AlertDialog` `:468` | One caller, `:414`, in M2's `MapTab`, raised from the Add button over the map | Always |
| Exit-navigation prompt | `ExitNavigationPrompt` `AvailabilityNavigationUi.kt:265`, `AlertDialog` `:269` | Raised only by the Back handler at `AvailabilityScreen.kt:1078`, gated on `compactTab == CompactTab.MAP`, not fullscreen, drawer closed, navigating; shown at `:1087` | Can tell on compact. The wide case is Q5. |
| Entry delete dialog | `CartographyEntryReportScreen.kt:632-641`, `AlertDialog` `:633` | Raised from the overflow menu (`:404`), which exists only outside fullscreen (`:372`). The screen shows its preview map exactly when `resolvedMapData != null && mapRegion != null` (`:411-421`); an entry with no kept records has no map. | Can tell |
| Entry overflow menu | `DropdownMenu` `CartographyEntryReportScreen.kt:388`, anchored to the header's More button (`:384-387`) | The header row (`:372-408`) sits directly above the preview map (`:421`), so a menu dropping from its right-hand button lands on the map's top-right corner when there is a map. The same `:411-421` condition says whether there is. The screen's outer `Column` (`:366`) carries no scroll of its own. | Can tell. Landing position is inferred from the layout, not measured. |
| Snackbar over the Maps tab | Compact host `AvailabilityCompactScaffold.kt:585`, `SnackbarHost(logDraftSnackbarHostState)` with the default `Snackbar` | The compact scaffold holds `compactTab()`. The wide host (`AvailabilityScreen.kt:1849`) is in the permanent drawer, beside the map, and stays unchanged. | Can tell. Q4 covers the Maps tab with no map. |
| Tools drawer sheet over Maps | `ModalDrawerSheet { … }` `AvailabilityScreen.kt:1804`, in the `ModalNavigationDrawer` at `:1789` | `AvailabilityScreen` holds `compactTab`. The drawer's scrim covers the bar and the rail, so the tab cannot change while it is open (Material3's drawer scrim, not read). | Can tell. Q4 applies here too. |
| J8's chip and lists | Chip: `ui/map/JournalEntriesChip.kt:108-124`, fill `MapIconStackButtonColorDark/Light` (`ui/map/MapChrome.kt:233`, `:236`, Bark/Cream at `MAP_CHROME_OVER_MAP_ALPHA`), content White/Bark. Chip list: `:131-137`, `containerColor = journalMenuContainerColor()` = `MenuDefaults.containerColor.copy(alpha = MAP_CHROME_OVER_MAP_ALPHA)` (`:68`), content `contentColorFor(MenuDefaults.containerColor)` (`:76`), provided at `:139`. Bubble count list: `ui/map/MapBubble.kt:480-486`, the same two functions. | — | **Holds. Already at 0.8 with opaque content; nothing to change.** |

### Not classifiable: the stops

**S1. The details sheet from a Records row (`ui/log/RecordsTab.kt:316`).** `-56` expected this call site to
cover no map. At this base it can:
- **Compact, Offline maps sub-tab.** `OfflineMapsPanel` (`ui/availability/AvailabilityOfflineMapsUi.kt:133`)
  puts the region picker's own map (M4) and the downloaded-region rows in one panel. A row's tap opens this
  sheet (`RecordsTab.kt:294`, rows at `AvailabilityOfflineMapsUi.kt:525`).
  - In a short landscape window (`w823dp-h384dp-land`), the panel is side by side (`:264-274`, `:341-395`):
    the map takes half the width at full height, and the rows are in the other half. A modal sheet at most
    640 dp wide, centred in an 823 dp window, spans about 91 to 732 dp, so it would overlap the map's half
    whichever side it is on. *(Inferred from the code and the 640 dp constant, not measured.)*
  - In portrait the panel scrolls as one column (from `:284`), with the map near the top and the rows below.
    Whether the sheet overlaps the map depends on the scroll position and the sheet's height. *(Inferred.)*
- **Compact, other sub-tabs** (All, Tracks, Waypoints): no map on screen.
- **Wide.** The Journal is `LogPanel` in the `PermanentDrawerSheet` (360 dp, `AvailabilityScreen.kt:1838-1855`),
  **beside** the results pane, never over it. `LogPanel` hosts the same `RecordsTab` (`ui/log/LogPanel.kt:488`).
  The results pane is the list (360 dp) and then the map (`AvailabilityWideLayoutUi.kt:125-151`), so the map
  starts at about 721 dp. A sheet centred in the window reaches past that when the window is wider than about
  802 dp. So a Records sheet on a wide window covers part of the Maps results map whenever that map is
  showing: after a search, not loading and without an error (`:229-248`), on the List or Map tab
  (`AvailabilityScreen.kt:1576-1577`). *(Inferred; the divider's 1 dp and the sheet's centring are library
  defaults I did not read.)*
- **Entry maps.** It cannot open over an entry map: the Records and Entries top tabs are exclusive in the
  Journal (`JournalTab.kt:675`).

`RecordsTab` is handed nothing that says which of these it is in. It knows its own sub-tab, but not the portrait
scroll position, nor what the wide results pane shows.

**S2. The species suggestions** (`ExposedDropdownMenu`, `AvailabilitySearchUi.kt:1118`, in
`SpeciesSearchControls` `:1004`).
- **Compact, every tab.** The field is in `SearchEntryBar` (`:205`), whose focus opens the search dropdown
  (`onFieldFocused = { showSearchDropdown = true }`, `AvailabilityCompactScaffold.kt:723` and `:949`). So the
  suggestions usually drop over the dropdown panel, which already draws at 0.8 on every tab (the 0.8 literal
  pair at `AvailabilityMapControlsUi.kt:91`/`:94`, used at `AvailabilitySearchUi.kt:397`). Suggestions longer
  than the panel run past it onto the map, on the Maps tab. The panel's scrim tap (`AvailabilityCompactScaffold.kt:1123`)
  closes the panel without clearing focus, so the suggestions can also sit straight on the map.
  *(Inferred from the code, not exercised.)*
- **Other compact tabs.** The panel and the suggestions can sit over the Journal's Offline maps picker map,
  which is directly under the header in portrait.
- **Wide.** The field is in `AvailabilitySearchTopBar` (`:943`) across the whole results pane, and the popup
  matches the field's width (Material3's `matchAnchorWidth` default, not read). So it spans the list and the map (M2) when a map shows.

A fill at 0.8 over the panel's 0.8 composites to 0.96 there. CLAUDE.md's UX default says "Layered fills
composite to that value; they do not each carry it", which a separate popup window cannot do over one part and
not another. That is a design question nothing rules.

**S3. The Month menu** (`MonthSelector` `AvailabilitySearchUi.kt:1150`, `ExposedDropdownMenu` `:1165`).
- **Compact.** It exists only in the search dropdown (`:471`). The compact Tools drawer passes
  `includeAdvancedSearch = false` (`AvailabilitySettingsUi.kt:605-613`). So it opens over the 0.8 panel,
  and its twelve rows can run past the panel's bottom onto the map. This is the same stacking question as S2.
- **Wide.** It is in the permanent drawer's Advanced search (`AvailabilitySearchUi.kt:709`, via
  `AvailabilityScreen.kt:1303`). It is anchor-width inside the 360 dp drawer, beside the map, not over it.
  It stays unchanged.

## Questions for the planner

**Q1. The Records details sheet (S1).** Options I can see:
- (a) Solid everywhere, as `-56` expected ("opened from a Records row in the Journal, each of the three has
  alpha 1.0"). The picker and wide overlaps are recorded as exceptions for the owner.
- (b) 0.8 on the Offline maps sub-tab only, the one Records panel that owns a map. `RecordsTab` already knows
  its sub-tab. The wide overlap with the results map stays solid.
- (c) (b), plus the wide case: `LogPanel` is told whether the results pane is showing a map. That needs
  threading through `AvailabilityScreen` → `LogPanel` → `RecordsTab`, and `LogPanel.kt` is not in `-58`'s
  scope list.
- (d) Decide at open time from the measured bounds of the sheet and of any map on screen. That is a new
  mechanism, and I would not propose it without data.

**Q2. What "covers a map" means.** `-58` says dialogs go to 80% when "their screen shows a map beneath
them". Read geometrically instead, the answer changes for:
- the bubble's details sheet on wide windows 721 to 802 dp, where the centred sheet stops short of the
  map pane;
- the same sheet opened from an entry map in the 360 dp wide drawer, which a window wider than about 1360 dp
  would not overlap;
- the same sheet over the entry report's preview map in portrait, depending on the sheet's height;
- the entry delete dialog, centred below the preview map.

I classified the table above under "the screen shows a map". Please confirm, or rule the geometric reading.

**Q3. Popups over 80% chrome (S2, S3).** Options:
- (a) The popup at 0.8 wherever it is over the Maps tab. It stacks to 0.96 where it lies on the panel.
- (b) The popup solid where it opens from the search dropdown, since it lies mostly on 80% chrome.
- (c) The popup at 0.8, and the panel treated as the layer beneath it. That is a new mechanism.

For the wide species suggestions, `mainScaffold` can tell whether M2 shows a map, but the popup spans both the
list and the map. The Q2 reading decides it.

**Q4. The Maps tab without a map.** On compact, the Maps tab shows a spinner while sightings load and a
message on a sightings error instead of the map (`AvailabilityCompactMapUi.kt:531-543`). Should the snackbar,
the drawer and the exit prompt follow the tab ("over the Maps tab", as `-58` words them) or whether the map is
composed? Both are available at their call sites.

**Q5. The exit prompt on the wide layout.** Navigation can only start on compact (the control pill,
`AvailabilityMapControlsUi.kt`). The prompt's handler (`AvailabilityScreen.kt:1078`) reads compact-only state,
so a window that crosses into the wide tree while navigating (a foldable, a resized window) can raise the
prompt over the wide layout, where M2 may or may not show a map. Solid there, or follow M2?

## Consolidation items, re-verified at base

- **The 0.8 literals.** There are four literal sites, not five:
  - `CompassStripBackgroundColorDark`/`Light` (`AvailabilityMapControlsUi.kt:91`, `:94`) serve three
    surfaces: the compass strip (`:326`), the HUD (`NavigationHud.kt:204`) and the search dropdown
    (`AvailabilitySearchUi.kt:397`).
  - The bottom bar over the map is `AvailabilityCompactMapUi.kt:1230` (the pulse cited `:1210`).
  - The rail over the map is `:1267`.

  Routing the four through `MAP_CHROME_OVER_MAP_ALPHA` covers the pulse's five surfaces.
- **Stale "opaque" comments** at base: `AvailabilitySearchUi.kt:330`, `AvailabilityCompactScaffold.kt:320`
  (pulse `:316`), `app/src/test/.../AvailabilityScreenMapIconStackTest.kt:2181`, `AvailabilityCompactMapUi.kt:711`
  (pulse `:707`), `ui/map/MapChrome.kt:294`, `:454-455` and `:716-718`.
- **`MapModePicker` is dead code.** It is at `ui/map/MapChrome.kt:127`. `grep` finds no call in `app/src/main`
  or `app/src/test`, only doc comments and imports. Flagged, not deleted.
- **The device translucency comment** is at `AvailabilityCompactMapUi.kt:326-341` at base (the pulse cited
  `:326-339`).

## Build notes for when it resumes

These are not questions. They are what the build would have to handle.
- **Content colour.** `contentColorFor` matches only an exact scheme role, so every item whose fill gains an
  alpha needs its content colour pinned to the unaltered role's, as `MapLayersSheet.kt:242` does. That covers
  the `Surface`s, the `AlertDialog`s (title and text content colours), the menus and the snackbar.
- **The wide Layers button** has `tonalElevation = 3.dp` on `color = surface` (`AvailabilityWideLayoutUi.kt:172-178`).
  The theme sets no `surfaceTint` (`ui/theme/Theme.kt`), so it is Material's default. A `surface.copy(alpha)`
  fill may no longer match `colorScheme.surface`, so the elevation tint may stop applying and the colour
  would change by more than its alpha. The fill that keeps "the same colour aside from alpha" is the tinted
  surface at 0.8. This is inferred: I did not read `Surface`'s tonal-elevation code at this version.
- **The trip date dialog.** `DatePicker` takes its own `colors`, so the same `DatePickerColors` would go to
  the dialog and the picker. Whether `DatePicker` paints its own container is not read.
- **Semantics seams.** None of these items exposes its colour to tests today. The Layers sheet's pattern is
  `MapLayersSheet.kt:246` and `:250-253`.

## Not done

- No build, no tests (tests first included), no revert checks, no full suite.
- None of the layout claims above was measured. Each geometric claim is marked as inferred. A Robolectric
  bounds check through the real entry points would turn S1's and Q2's geometry into evidence. I did not run
  one, because the stop does not depend on it: the question is what the call site can tell, and that is
  answered by the code.
- I did not read `prompts/preserved/2026-09-28-59.md` or `-60.md`. I did see their `RECORD.md` entries and
  the plan's withdrawn paragraphs while reading the record and the plan for context. I did not act on them.

## Device-only, for after the build (listed, not run)

In `-58`'s order, the surfaces outside the map's `Box` first (the comment at `AvailabilityCompactMapUi.kt:326-341`):
1. The search dropdown (already 0.8 today) and the Tools drawer over Maps: does the map show through them on
   the S22?
2. The snackbar over the Maps tab, and the dialogs and sheets, which are separate windows.
3. The details sheet, the confirm row, the search notice, the wide Layers button and the menus: legibility
   over each basemap and at night. The owner judges.

## Predictions

- `-56` 1, "Two call sites, and only the map bubble's covers a map on the compact layout": two call sites,
  **held** (`MapBubble.kt:312`, `RecordsTab.kt:316`). The second half **did not hold**: the Records call site's
  Offline maps sub-tab covers the picker map in short landscape, which is the compact tree (inferred, S1).
- `-56` 2 and `-58` 2 (suite growth): not measured, since nothing was built.
- `-58` 1, "Every item's call site can tell whether it covers a map, except possibly the popups": **did not
  hold**. The popups are unclassifiable as predicted (S2, S3), and so is the details sheet's Records call site
  (S1).
- `-58` 3, "The Material3 API for the snackbar or a menu container needs a check at the pinned version":
  checked. `Snackbar`, `DropdownMenu` and `ExposedDropdownMenu` all take a `containerColor` at `1.5.0-alpha26`.

## D58

Before the push I ran a case-insensitive grep for the three phrases D58 forbids over this report and this
commit's message. There were zero hits.

## Decisions I made

- **The record.** I followed the dispatch over my agent definition. The kit is absent at this base (no
  `.claude/kit.json`, `check_record.py` or `check_prompts.py` at `e253b50`), and `-56` says the planner writes
  the record. I wrote no sweep, intent or terminal and did not touch `RECORD.md`. For the same reason I did
  no structural section check against a `kit.json`. The one in my launch worktree's older checkout
  (`faf2f88`) names sections that `-56` does not use literally.
- **Stopping the whole stage**, rather than building the classified items and leaving S1 to S3 open. I read
  `-56`'s abort condition and `-58`'s "is a stop" as stopping the build.
- **The reading "the screen shows a map beneath"** for the classified table. It is `-58`'s wording for
  dialogs, which I applied to the sheet, the menus and the snackbar. Q2 asks for it to be confirmed.
- **Where the report goes.** This file, the `-56` report name, as a report-only commit on `journal-redesign`.
  It changes nothing under `app/`, so the planner's suite at `99de6c2` stands for it.
- **No measurement.** I inferred geometry from code and constants rather than running Robolectric bounds
  checks.

## Flags outside scope

- **The search dropdown is at 0.8 on every compact tab** (`AvailabilitySearchUi.kt:392-399`), including tabs
  with no map. That differs from "solid where it covers no map". `-58` only asks to route its literal onto the
  constant, so it would stay that way.
- **The pulse's line citations have drifted since J8.** Examples: the nav literal `:1210` → `:1230`, the
  scaffold comment `:316` → `:320`, and `MapBubble.kt:305` → `:312`.
- **The wide Layers button's tint question** (build notes) also applies to any other `Surface` on `surface`
  with tonal elevation that later goes translucent.

# Resumed and built (planner message 2026-09-28-77)

The planner answered the stop with message `2026-09-28-77` (`prompts/preserved/2026-09-28-77.md` at `9c7d0a5`). A
coordinator message summarised it, and a second one resumed the stage after a network outage. All three are quoted
verbatim at the end of this report. The stage is built, tested first, revert-checked and suite-checked, and it is
pushed to `journal-redesign` at `6e36122`.

## What landed

On `map-chrome`, first parent, pushed to `record-sheet-alpha-wip` at each step. `journal-redesign` received `89f9d15`
and then `6e36122`, each after a full suite on that commit.

| Commit | What |
|---|---|
| `b019080` | **Tests first.** Every surface passes its Material3 default colour explicitly, pins its content colour, and exposes both through `MapChromeContainerColor` and `MapChromeContentColor` (`ui/map/MapChrome.kt`), with test tags where it had none. Nothing drawn changes. Also adds `MapChromeOverMapTest.kt`. |
| `a5f2786` | Forward change, part 1: `mapChromeFill(role, overMap)` in `MapChrome.kt`, `RecordDetailsSheet`'s `overMap` and its two call sites, the centre-pin confirm row, and the search notice and the two search menus' parameters. |
| `53c670e` | Test fix: the Q4 case goes through a snackbar, not the drawer. See "Tests first". |
| `852dac9` | Forward change, part 2: every remaining call site, the four `0.8f` literals moved onto `MAP_CHROME_OVER_MAP_ALPHA`, and the stale "opaque" comments corrected. |
| `89f9d15` | `git pull --no-rebase` of `journal-redesign` (the save-failure stage `-68`/`-76`). It merged cleanly. Full suite, then pushed to `journal-redesign`. |
| `d2a9cff` | A seam and two tests for the date picker's own clear container. See "Added after the forward change". |
| `5f69550` | The three menus' content colour is now read inside them, on their first row. Before this, the value was passed in, which a test could not fail. Semantics only. |
| `6e36122` | `git pull --no-rebase` of `journal-redesign`. The incoming commits were records, runs and plans, with no `app/` change. Full suite, then pushed to `journal-redesign`. |

## What each surface does now

"0.8" means its default role at `MAP_CHROME_OVER_MAP_ALPHA`, through `mapChromeFill`, with the content colour pinned to
the unaltered role's own.

| Surface | 0.8 when | Solid when | Role |
|---|---|---|---|
| Details sheet from a bubble (`MapBubble.kt`) | always: M1, M2 and M3 | never | `BottomSheetDefaults.ContainerColor` |
| Details sheet from Records (`RecordsTab.kt`) | the Offline maps sub-tab (-77 Q1 (b)), compact and in the wide drawer | All, Tracks, Waypoints | same |
| Centre-pin confirm row | always (it sits only in a map's own Box) | never | `surface` |
| Search notice | the Maps tab's own (`overMap = true` at its call site in the map's Box) | other tabs, wide | `errorContainer` |
| Wide Layers button | always | never | `surface`, alpha only (see below) |
| Trip date dialog | always. The picker inside has a clear container, so the dialog's one fill composites to 0.8 | never | `DatePickerDefaults.colors().containerColor` |
| Waypoint name dialog | always | never | `surface` |
| Three-way action dialog | always | never | `AlertDialogDefaults.containerColor` |
| Exit-navigation prompt | compact: the Maps tab. Wide: the results map drawn (-77 Q5) | wide with no map drawn | same |
| Entry delete dialog | the entry has a map | no map | same |
| Entry overflow menu | the entry has a map | no map | `MenuDefaults.containerColor` |
| Species suggestions | compact: the Maps tab's bar. Wide: the results map drawn | other compact tabs; wide before a search | same |
| Month menu | the search panel opened on the Maps tab | other tabs; the wide drawer (beside the map, unchanged) | same |
| Snackbar (compact) | the Maps tab, following the tab (-77 Q4) | other tabs | `SnackbarDefaults.color` |
| Tools drawer | over the Maps tab, following the tab | other tabs | `DrawerDefaults.modalContainerColor` |
| J8's chip, its list and the bubble's count list | unchanged, already 0.8 | — | — |

The wide rule is `wideResultsMapShown` (`AvailabilityScreen.kt`): List or Map selected, a searched region, not
loading, no error. Those are exactly the branches in which `MapTab` draws its map.

The exit prompt's composition moved below the window-class declarations so it can read them. Nothing else about it
changed.

These are unchanged, as `-58` says: the accent discs and the wide Add button, the 0.55 caption, every scrim, the
cluster's 0.6 container, and the full-screen destinations. `MapModePicker` stays, flagged.

**The wide Layers button's elevation tint drops.** This is computed, not measured on a screen. Material3 applies the
tonal-elevation tint only when the fill equals `colorScheme.surface` (`ColorSchemeKt.applyTonalElevation`, read with
`javap`). The formula is surfaceTint at `(4.5 ln(e + 1) + 2) / 100`, which is 0.0824 at 3 dp, composited over surface.
The app sets no `surfaceTint`, so it is `primary`.
- Dark: the button was `#272926` and is now `#1B1B1B` at 0.8.
- Light: it was `#E9EAE4` and is now `#FAF8F3` at 0.8.

As -77 says, this is reported and not compensated.

## Tests

`app/src/test/java/com/zynergylabs/forager/app/ui/availability/MapChromeOverMapTest.kt` has eight classes and 71
tests. Each test drives the real `AvailabilityScreen` in `ForagerTheme(darkTheme = true)`, and compares against the
roles read in the same composition.
- `MapChromeCompactTests` has 19 methods, run in `w384dp-h823dp-xxhdpi` and `w823dp-h384dp-land-xxhdpi`.
- `MapChromeEntryReportTests` has 5, run in the same two windows.
- `MapChromeRecordsTests` has 4, run in portrait, short landscape and `w840dp-h1024dp-mdpi`.
- `MapChromeWideTest` has 11, at `w1280dp-h900dp-mdpi`.

Entry points:
- a real touch on a stub glyph and the bubble's Details;
- the + disc and its chooser, with OK;
- typing in the search field;
- the Month field;
- a Records row;
- the report's overflow button and Delete entry;
- Back while navigating.

Tabs, the Records switch and chips are semantic clicks, as setup. Over a map, a test asserts three things: the
container's alpha is 0.8, its RGB is the role's, and the content colour inside equals the role's content colour and is
opaque. Elsewhere, a test asserts that the container is the role, solid.

### Tests first

The first run was at `b019080` from a cleared results directory, with no compile errors: 68 tests, 43 failures. I had
predicted 43 failures, on "container alpha expected:<0.8> but was:<1.0>", and 25 guards passing by construction.
- 41 failed for that reason.
- 2 failed for a reason I had not predicted, "the Tools drawer over the Maps tab while sightings load", once in each
  window. The failure was "could not find any node … 'Tools'". While sightings load, the Maps tab's bottom bar and rail
  are absent, because they are composed inside the map's Box. So the drawer cannot be opened there. The test was
  wrong, not the build.
  - I replaced it with a snackbar raised while sightings load, in `53c670e`.
  - I re-ran it at `b019080` with the corrected test file (the file saved to a copy first, the tree restored after,
    `git status` clean). Both failed on "compact-snackbar: container alpha expected:<0.8> but was:<1.0>".
- The 25 guards passed, as predicted: the solid-elsewhere cases on the List tab, entries without a map, the Records
  sub-tabs other than Offline maps, and wide with no map. A guard can only pass by finding its surface, because the
  helper requires exactly one node that carries the key. The revert checks below make the guards evidence.

### Added after the forward change

Neither of these is a tests-first result.
- **The picker's clear container** (`d2a9cff`): one compact method, run in both windows, and one wide method. Their
  evidence is R13.
- **The menus' inside read** (`5f69550`): this made their content checks able to fail. The evidence is R15.

### Forward runs

- `852dac9`: the eight classes, plus `MapChromeAlphaTest` and `MapLayersSheetTest`, 87 of 87. The filter also matched
  `AvailabilityScreenMapLayersSheetTest`.
- `d2a9cff`: three classes, 49 of 49.
- `5f69550`: five classes, 59 of 59.

### Revert checks (runner rules)

My runner (`/tmp/claude-1000/mc/revert.py`) does the following for each check:
- saves a copy of the file and makes one edit;
- clears the results directory and runs the eight classes;
- refuses the results on any compile-error line, or on XML older than the run's start;
- restores the file from the saved copy, never from git;
- confirms the file is byte-identical to the copy and the tree is clean;
- confirms only when the failing set equals the prediction and each message carries the predicted fragment.

Every check below was confirmed, and every restore was identical with a clean tree. Each check ran 71 tests. Colour
values are rounded to three places from the JUnit messages, and the names in brackets are mine.

| Check | Mechanism | Edit | Failed (as predicted) | Message |
|---|---|---|---|---|
| R1 | window sheet | bubble call site `overMap = true` → `false` | 9: the three kinds × 2 windows, entry map × 2, wide | "record-details-sheet: container alpha expected:<0.8> but was:<1.0>" |
| R2 | window sheet (guards) | Records `overMap = selectedTab == OFFLINE_MAPS` → `true` | 9: the three solid sub-tab cases × 3 windows | "…expected:<1.0> but was:<0.8>" |
| R3 | sheet content pin | `contentColor = contentColor,` removed | **0, as predicted**. See below. | — |
| R4 | dialog | three-way `overMap = true` → `false` | 1 | "three-way-action-dialog: container alpha expected:<0.8> but was:<1.0>" |
| R5 | dialog, wide rule | the exit prompt's wide branch → `true` | 1: wide with no map | "exit-navigation-prompt: container alpha expected:<1.0> but was:<0.8>" |
| R6 | popup (guard) | Month's `overMap = compactTab() == MAP` → `true` | 2: List tab × 2 | "month-menu: … expected:<1.0> but was:<0.8>" |
| R7 | popup | the Maps bar's `overMap = true` → `false` | 2: suggestions on Maps × 2 | "taxon-suggestions-menu: container alpha expected:<0.8> but was:<1.0>" |
| R8 | drawer | `compactTab == MAP` → `!=` | 4: Maps and List × 2 | "tools-drawer-sheet: container alpha expected:" |
| R9 | drawer content pin | `drawerContentColor = drawerContentColor,` removed | 2 | "tools-drawer-sheet: content colour expected:<Color(0.929, 0.890, 0.816, 1.0)> but was:<Color(0.0, 0.0, 0.0, 1.0)>" (Cream; black, the default outside any Scaffold) |
| R10 | in-tree surface | confirm row `overMap = true` → `false` | 3: compact × 2, wide | "centre-pin-confirm-row: container alpha expected:<0.8> but was:<1.0>" |
| R11 | snackbar (guard) | `compactTab() == MAP` → `true` | 2: List tab × 2 | "compact-snackbar: … expected:<1.0> but was:<0.8>" |
| R12 | entry map rule (guards) | `entryMapShown = …` → `true` | 4: menu and dialog without a map × 2 | "…container alpha expected:<1.0> but was:<0.8>" |
| R13 | picker clear | `colors(containerColor = Color.Transparent)` → `colors()` | 3 | "trip-date-picker: container expected:<Color(0,0,0,0)> but was:<Color(0.169, 0.169, 0.169, 1.0)>" |
| R14 | in-tree surface | `SearchNotice(uiState, overMap = true)` → `SearchNotice(uiState)` | 2 | "search-notice: container alpha expected:<0.8> but was:<1.0>" |
| R15 | menu content | the suggestions' provider → `LocalContentColor.current` | 2 | "taxon-suggestions-menu: content colour expected:<Color(0.929, 0.890, 0.816, 1.0)> but was:<Color(1.0, 1.0, 1.0, 1.0)>" (Cream, the role's; White, the bar's) |
| R16 | notice content pin | `contentColor = noticeContentColor,` removed | 2 | "search-notice: content colour expected:<Color(0.961, 0.863, 0.863, 1.0)> but was:<Color(0.929, 0.890, 0.816, 1.0)>" (`onErrorContainer`; the Scaffold's Cream) |

**R3, and which content checks can fail.** R3's build recompiled (`compileDebugKotlin` executed) and 71 of 71 passed.
- Every call site of the details sheet sits inside a `Scaffold`, whose content colour is `onBackground`.
- `onBackground` equals `onSurface` in both of the app's schemes: Cream in dark, Bark in light (`Theme.kt`).
- So without the pin, the fallback is the same colour, and the sheet's content assertion cannot fail in this app.
- The same holds for the centre-pin row, the waypoint dialog, the date dialog and the entry menu. Each sits under a
  `Scaffold`'s content colour.
- The alert dialogs and the snackbar are given explicit content roles by Material3, so they have no pin to lose.
- The content checks that can fail are the drawer's (R9), the species suggestions' and the Month menu's (R15; they
  sit under the bar's White or Bark), and the notice's (R16).
- The pins stay, as the Layers sheet's does. They are correct, and a change of scheme would make them matter.

## Suites

Each suite ran from a cleared results directory with `./gradlew --offline :app:testDebugUnitTest`. Each had no compile
errors, and no XML was older than the run's start.
- `852dac9`: **301 / 2442 / 0 / 0 / 24**.
- `89f9d15` (after merging the save-failure stage): **304 / 2474 / 0 / 0 / 24**.
- `6e36122` (final): **304 / 2477 / 0 / 0 / 24**. This is the J8 baseline, 293 / 2374, plus:
  - this stage: 8 classes and 71 tests;
  - the save-failure stage, merged in: 3 classes, 31 `EntrySaveFailureShown*` tests, and 1 more `CartographyViewModelTest`.
- The held family, `JournalPendingDeleteTest` and `JournalTabTest`, passed in every suite.
- Before every Gradle run, `/tmp/claude-1000/mc/free.sh` checked that no other `GradleWorkerMain` or wrapper was
  running and that at least 2.5 GB was available. Once the machine was busy with the save-failure build, and I waited
  for it.

## Predictions

- `-58` 2, "the suite grows by 25 to 50": **did not hold**. It grew by 71 tests, because the dispatch asked for
  portrait and short landscape across every compact item, and three windows for Records.
- `-56` 2, "6 to 12": superseded by `-58`.

## Not tested

- **The light theme.** Every test composes the dark theme. The roles are read from the theme, but no light run exists.
- **The wide exit prompt through a real window-class change.** The test composes the wide window with navigation
  already on and presses Back.
- **Whether any fill shows the map on a real screen.** Robolectric reports the colour a surface was given, not what
  the device composites.
- **The date picker's other colours over a map**, such as the selected day and the headline. The test checks only its
  container.
- **The scrims.** They are unchanged and not asserted.

## Device-only (listed, not run: no phone in this stage)

1. **Surfaces outside the map's Box first**, for the comment at `AvailabilityCompactMapUi.kt:326-341`: the search
   dropdown, the Tools drawer over Maps, and the compact snackbar, which the `Scaffold` hosts outside the map's Box.
   Does the map show through each on the S22?
2. **The separate windows over the map**: the details sheet, the three dialogs, the exit prompt and delete dialog, the
   three menus. Does translucency reach the map?
3. **Legibility** of each surface over Street, Topographical and Satellite, and at night. The owner judges.
4. **The wide Layers button** without its tint, on a tablet.

## Decisions I made

- **The Q3 menus on the Journal tab.** -77 says the two menus are 0.8 "when they open over the Maps tab" and solid
  "on tabs with no map". The Journal's Records → Offline maps panel is a tab with a map beneath the search panel, and
  neither phrase names it. I built the first phrase literally: 0.8 only on the Maps tab, so on that Journal panel the
  menus stay solid.
- **The wide species suggestions** at 0.8 when the results map is drawn. This is my Q3 note, read under the Q2 ruling.
- **The exit prompt's wide rule** is "the map is drawn" (`wideResultsMapShown`), which is -77 Q5's wording. The
  compact tree follows the tab, which is Q4.
- **The date picker's own container is clear** inside the dialog, so that the dialog composites to 0.8. I did this
  under CLAUDE.md's layered-fill rule, since the owner's stacking exception names only the two menus.
- **Moving the exit prompt's composition** below the window-class declarations, rather than duplicating
  `currentWindowWidthClass()` there.
- **The test design**:
  - one file of abstract classes run per window;
  - the dark theme only;
  - static screen state for the wide suggestions, where the top bar's field has no tag, and typing for the compact
    ones;
  - replacing the unreachable drawer-while-loading test with a snackbar.
- **Guards passing at base.** The abort condition "a tests-first test passing at base" I read as covering tests
  predicted to fail, because the dispatch itself asks for solid-elsewhere assertions, which pass at base by
  construction. J8 and the Layers stage did the same. They are backed by R2, R5, R6, R8, R11 and R12.
- **Tightening the menus' content seam after the fact** (`5f69550`), once I saw it could not fail.
- **Stating which content checks can fail (R3)** rather than removing the ones that cannot.
- **The record.** As in the stop: no record entries. I used the dispatch's WIP branch for unverified commits, and
  pushed to `journal-redesign` only after a full suite.

## Flags outside scope

- **While sightings load, the compact Maps tab has no bottom bar or rail.** They live inside the map's Box, so Tools
  and the tabs cannot be reached until loading ends (first tests-first run). This was not examined further.
- **The J8 bubble's and chip's menus** already take their content colour as passed, not as read. This is J8's seam.
- **`MapModePicker`** (`ui/map/MapChrome.kt:127` at base) is still dead code.
- **The search dropdown** is still at 0.8 on every tab, left as -77 says.
- **The sheet's content assertion cannot fail in this app's schemes** (R3). The same applies to four other surfaces'
  content assertions.

## D58

Before each push I ran a case-insensitive grep for the three phrases D58 forbids over the whole diff since `de22474`
and every commit message. There were zero hits each time, and again over this section before its push.

---

## The governing texts, verbatim

### `prompts/preserved/2026-09-28-56.md` (the dispatch), whole file at `e253b50`

````text
HEAD: 367c32f (journal-redesign) when written. **Queued:** the base at launch is the planner's J8 terminal commit, stated in the launch message.
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: build
Preserved: 2026-09-28T18:41:31Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** build

# Role

You are the coder for **the record details sheet at 80% over the map**. This dispatch's intent is `2026-09-28-56`. The kit is gone and the planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`.

Read `CLAUDE.md` first. It binds you, especially the Testing section and "a check that passes because it never saw the data that could fail it".

# The owner's request and rulings, verbatim

- 2026-09-28, sent at 18:04Z and 18:05Z and resent at about 18:40Z with two screenshots: "Can you set this card at 80% opacity while over the map? In the places that aren't covering a map, they can stay solid. Same for tracks please". The screenshots are outside the repository at `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-owner-sheet-opacity/`. `owner-waypoint-sheet-over-map.jpg` shows the Waypoint 3 details sheet and `owner-track-sheet-over-map.jpg` a track's details sheet, both opened over the Maps tab.
- Asked when to build it: "After J8 (Recommended)".
- Asked whether the offline-region sheet goes to 80% over the map too: "Yes, all three (Recommended)". Waypoint, track and offline-region details are all at 80% when the sheet covers a map, and solid when it does not.

# What exists (planner's read at `367c32f`, before J8: re-check it at your base)

- `RecordDetailsSheet` (`ui/log/RecordDetailsSheet.kt:141-183`) is one `ModalBottomSheet` for all three kinds, with the default container colour.
- It has two call sites:
  - `ui/map/MapBubble.kt:305`, from a map bubble's details action. This one covers the map.
  - `ui/log/RecordsTab.kt:316`, from a Records row in the Journal. The planner believes it does not cover a map on the compact layout. **Verify this.**
- The precedent is the Layers sheet (`ui/map/MapLayersSheet.kt:214-232`):
  - `BottomSheetDefaults.ContainerColor.copy(alpha = MAP_CHROME_OVER_MAP_ALPHA)`;
  - the content colour pinned to `contentColorFor(BottomSheetDefaults.ContainerColor)`, with the reason in its comment;
  - Material3's default scrim;
  - the container colour exposed through a semantics key, which `MapLayersSheetTest.kt:69-77` reads.
- `MAP_CHROME_OVER_MAP_ALPHA` is 0.8 (`ui/map/MapChrome.kt:239`).

# Verification before building

At your base, with file:line:
1. List every call site of `RecordDetailsSheet`. J8 may have added some.
2. For each call site, on the compact layout and on the wide layout (`w823dp-h384dp-land` and a wide window), state whether the sheet covers a map.
   - Check whether the Journal on the wide layout sits beside or over a map, so that a sheet opened from the Records tab would cover it.
   - Check whether any call site can open over an entry map.
3. **If a call site covers a map in one layout and not in another, and the call site cannot tell which it is in, stop and report.** Do not choose.

# Build

- Give `RecordDetailsSheet` one way to be told it is over a map. Follow the Layers sheet exactly for the container colour, the content colour and the scrim.
- When it is told that, the container is at `MAP_CHROME_OVER_MAP_ALPHA`. When it is not, the sheet is unchanged.
- Every call site that covers a map says so. Every other call site is unchanged in behaviour.
- Expose the container colour to tests the way the Layers sheet does.
- **No other change** to the sheet's content, layout, dismissal or copy. New copy of any kind is a stop.

# Tests

- **Tests first**, each seen failing at base for its stated reason. Drive every test through its real entry point, not by calling the sheet directly:
  - opened from a map bubble on the Maps tab (use the M1 bubble harness in `AvailabilityScreenMapBubblesTest` / `MapLayersUiFixtures.kt`), the waypoint, track and offline-region sheets each have a container alpha of 0.8 and the default container role's colour aside from alpha;
  - opened from a Records row in the Journal, each of the three has alpha 1.0.
- Cover portrait and `w823dp-h384dp-land`.
- The content colour stays opaque in both cases.
- **Revert check,** to CLAUDE.md's runner rules. Revert the map call site's "over a map" to false. Predict that exactly the over-map cases fail, with a message naming the alpha. Confirm the revert compiled, and that the forward change is present again afterwards.
- **Full suite** from a cleared results directory. The baseline is the planner's terminal figure for J8.
- **Device-only, listed not run:** the sheet's legibility over each basemap and at night on the S22. The owner judges it.

# Scope

**In scope:**
- `ui/log/RecordDetailsSheet.kt`;
- the call sites that cover a map (expected: `ui/map/MapBubble.kt`);
- tests;
- the completion report `docs/audits/<date>-record-sheet-over-map-completion-report.md`, with the usual sections.

**Out of scope:** everything else, including the Layers sheet, the bubbles' own cards, and the record and docs above.

# Branch, environment, finish

- **Branch:** a worktree cut from `origin/journal-redesign` at the base the launch message names. Push to `journal-redesign` after each commit that leaves the suite passing, and put broken work on `record-sheet-alpha-wip`. Merge with `git pull --no-rebase`, never rebase.
- **Environment:** `LC_ALL=C.UTF-8`. Check `df` and free memory before building. No phone.
- **D58** before each push.
- **Finish line:** the build, tests first, the revert check, the full suite and the report, all pushed. The planner re-runs the suite and writes the terminal.

# Abort conditions

- a call site the verification cannot classify;
- a tests-first test passing at base;
- a revert build that does not compile;
- a non-held failure;
- new copy;
- disk full or OOM;
- two failed fixes;
- an unruled design question.

# Predictions (planner)

1. Two call sites, and only the map bubble's covers a map on the compact layout.
2. The suite grows by 6 to 12.

# Merge

Not authorised.
````

### `prompts/preserved/2026-09-28-58.md` (the continuation), whole file at `e253b50`

````text
HEAD: 35f05c9 (journal-redesign) when written. **Queued with -56:** the base at launch is the planner's J8 terminal commit, stated in the launch message.
Target subagent: the coder for dispatch 2026-09-28-56, given with it at launch
Type: continuation
Preserved: 2026-09-28T19:06:20Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** build (continuation `2026-09-28-58` of dispatch `2026-09-28-56`, written before `-56` launched)

Read `-56` first. This continuation **widens it** from the record details sheet to all map chrome. Where the two differ, this governs. Quote it verbatim in your report.

# Why

After `-56` was written, the owner ruled a principle, verbatim: "My idea is that nothing should fully obstruct the map view. All map chrome gets 80% opacity as a result." It is now in `CLAUDE.md`, UX defaults, and in `docs/plans/journal-redesign.md`, "Map chrome at 80%, nothing fully obstructs the map", with the owner's edge-case rulings. The inventory you start from is `docs/audits/2026-09-28-map-chrome-inventory-pulse.md`, read at `76a67f3`, before J8. **Re-verify every row at your base**, because J8 has landed since.

# The owner's edge-case rulings, verbatim

1. Dialogs, pop-up menus and the snackbar over a map: "80% over the map".
2. The Tools drawer: "80% over Maps (Recommended)". It stays solid over the other tabs.
3. The accent buttons (the + disc, the record disc while recording, the wide Add button): "Keep them solid (Recommended)".
4. The attribution caption: "Leave it at 55% (Recommended)".

# What goes to MAP_CHROME_OVER_MAP_ALPHA when it covers a map

Each item stays solid (unchanged) where it covers no map.
- **Record details sheet**, as in `-56`: the waypoint, track and offline-region sheets, from the bubble.
- **Centre-pin confirm row** (M1, M2).
- **Search notice banner**, where it shows over the Maps tab's map. It is unchanged on other tabs, and on the wide layout, where it sits above the map.
- **Wide Layers button** (M2).
- **Dialogs** whose screen shows a map beneath them:
  - trip date (M1, M2);
  - waypoint name (M1, M2);
  - the three-way action dialog (M2);
  - the exit-navigation prompt;
  - the entry delete dialog (M3).
- **Pop-up menus** that open over a map: the species suggestions, the Month menu and the entry overflow menu. The pulse could not tell where each lands relative to the map, so find out and classify each one.
- **Snackbar**, over the Maps tab.
- **Tools drawer sheet**, when it opens over the Maps tab.
- **J8's new chip and its list**, if they are not already at 80%.

In every case the content colour stays opaque, as on the Layers sheet (`MapLayersSheet.kt:225`/`:231`). Text fields inside must stay legible.

# What is not changed

- the accent discs and the wide Add button;
- the attribution caption (0.55);
- every scrim, including the add-menu scrim, sheet and dialog scrims and the drawer scrim;
- the cluster's 0.6 container;
- full-screen destinations: the photo viewer, the find-over-view page and the camera;
- `MapModePicker`, which is dead code. Flag it; do not delete it.

# Consolidation, no visible change

- Route the five `0.8f` literals through `MAP_CHROME_OVER_MAP_ALPHA`: the compass strip, the HUD, the search dropdown, the bottom nav and the rail over the map. The pulse cites each.
- Correct the stale "opaque" comments at the lines the pulse lists.

# Verification before building, added to -56's

- For every item above, give file:line at your base, the Material3 parameter you will set (confirm it exists in `1.5.0-alpha26`), and how the call site knows whether it covers a map.
- **An item whose call site cannot tell whether it covers a map is a stop.** Report it; do not choose.

# Tests, added to -56's

- **Tests first**, each seen failing at base. Drive each through its real entry point. Assert the container alpha over the map (0.8) and elsewhere (unchanged), and assert the content colour stays opaque. Expose colours through semantics as the Layers sheet does.
- Cover portrait and `w823dp-h384dp-land`, plus a wide window for M2.
- **Revert checks:** at least one per mechanism (window sheet, dialog, popup, drawer, in-tree surface), under CLAUDE.md's runner rules.
- **Device-only, listed not run:** whether each surface shows the map through it on the S22. The comment at `AvailabilityCompactMapUi.kt:326-339` reports a translucent surface outside the map's `Box` shipping opaque on a real device. The search dropdown and the drawer sit in that position. List them first.

# Scope, widened

`-56`'s scope plus:
- the call sites and composables of the items above;
- `ui/map/MapChrome.kt`;
- `AvailabilityMapControlsUi.kt`, `NavigationHud.kt`, `AvailabilitySearchUi.kt` and `AvailabilityNavigationUi.kt`;
- `AvailabilityCompactMapUi.kt`, `AvailabilityCompactScaffold.kt`, `AvailabilityWideLayoutUi.kt` and `AvailabilityScreen.kt`;
- `CentrePinLocationPicker.kt` and `CartographyEntryReportScreen.kt`.

No behaviour change beyond fill colours, apart from threading whether a surface covers a map. New copy is a stop.

# Predictions (planner), replacing -56's

1. Every item's call site can tell whether it covers a map, except possibly the popups.
2. The suite grows by 25 to 50.
3. The Material3 API for the snackbar or a menu container needs a check at the pinned version.

Everything else in `-56` stands, including the abort conditions, the finish line and "Merge: not authorised".
````

### The planner's launch message

````text
This is the launch of planner dispatch `2026-09-28-56`, as amended by continuation `2026-09-28-58`: **map chrome at 80% where it covers a map, solid elsewhere.**

# What governs, in full, in this order

1. `prompts/preserved/2026-09-28-56.md`: the dispatch (the record details sheet at 80% over the map).
2. `prompts/preserved/2026-09-28-58.md`: the continuation that widens `-56` to all map chrome, with the owner's four edge-case rulings verbatim. **It governs where it differs from `-56`.**
3. `RECORD.md` entry `2026-09-28-67`: the launch note. It sets the base, and it says J8's chip and its lists are expected to be at 0.8 already.

**Do not read or act on `prompts/preserved/2026-09-28-59.md` or `-60.md`.** The owner withdrew both ("Let's restart from this point and forget everything beyond it:"; `RECORD.md` entry `2026-09-28-61`). Under `-58`, the accent buttons (the + disc, the record disc, the wide Add button) **stay solid**, and the attribution caption stays at 0.55.

Quote `-56`, `-58` and this message verbatim in your completion report.

# Base, branch, worktree

- **Base:** `origin/journal-redesign` at `e253b50`. Its `app/` is identical to `99de6c2`, the J8 terminal commit; the planner's suite there is `293 / 2374 / 0 / 0 / 24`.
- **Worktree:** `git worktree add /home/zynergy-labs/Zynergy/forager-wt/map-chrome -b map-chrome origin/journal-redesign`. Confirm that HEAD is `e253b50` or a later commit that changes only docs, records or prompts.
- **Push:** to `journal-redesign` after each commit that leaves the suite passing. Put broken work on `record-sheet-alpha-wip`. Merge with `git pull --no-rebase`, never rebase.

# Things to know at this base

- The inventory you start from, `docs/audits/2026-09-28-map-chrome-inventory-pulse.md`, was read at `76a67f3`, **before J8 landed**. J8 touched `AvailabilityScreen.kt`, `AvailabilityWideLayoutUi.kt`, `AvailabilityCompactMapUi.kt`, `MapBubble.kt`, `MapLayersSheet.kt` and more. Re-verify every row at your base, as `-58` requires.
- `-58`'s J8 item says "if they are not already at 80%". The J8 terminal (`RECORD.md` `2026-09-28-66`) records that the chip uses the taxon chip's 0.8 colour source, and that its list and the bubble's count list are at `MAP_CHROME_OVER_MAP_ALPHA` with opaque text. Verify this, and change nothing there if it holds.
- `-58`'s stop rule holds: an item whose call site cannot tell whether it covers a map is a stop, not a choice.

# Sharing the machine

- The phone is in use by a device coder (Part 1). **No phone, no emulator.**
- The J6 premise pulse has finished, so you are the only build.
- Check `df` and free memory before each Gradle run. About 10 GB of disk and 3.5 to 4 GB of memory were free at launch.
- Commit and push at every natural stopping point.

The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.

When you finish or stop, hand back a report as the dispatch requires: what landed with hashes, verification, evidence, suite counts, revert checks, what was not tested, device-only items, decisions you made and flags.
````

### `prompts/preserved/2026-09-28-77.md` (planner message -77), whole file at `9c7d0a5`

````text
HEAD: fda3158 (journal-redesign)
Target subagent: the map-chrome coder (SendMessage), continuing dispatch 2026-09-28-56 (as amended by -58) after its stop at dc09e53
Type: continuation
Preserved: 2026-09-28T21:13:53Z by the planner, by hand, time read from the clock.
--- verbatim message follows ---
Planner message `2026-09-28-77`, part of dispatch `2026-09-28-56` as amended by `-58`. Quote it verbatim in your report. It answers your stop at `dc09e53` (S1 to S3, Q1 to Q5). **Build it.**

**Q3, S2 and S3, the popups over the search panel. The owner's ruling, verbatim: "1 A".** Option A, as put to the owner: "Leave them stacked. It's the simplest. They look almost solid over the panel and 80% where they overhang the map."
- The species suggestions (`AvailabilitySearchUi.kt:1118`) and the Month menu (`:1165`) each get `MAP_CHROME_OVER_MAP_ALPHA` on their own when they open over the Maps tab, and they stack over the 0.8 panel.
- On tabs with no map they stay solid.
- This is the owner's exception to CLAUDE.md's composite line, for these two menus only.

**Q2, what "covers a map" means. Planner's ruling, stated to the owner and not overruled:** a map is drawn on screen beneath the surface. This is your classifying reading. The owner's "In the places that aren't covering a map, they can stay solid" allows solid there; it does not require it. So you do not measure bounds.

**Q1 and S1, the Records details sheet. Planner's ruling, stated and not overruled:** your option (b).
- 0.8 when it opens from the Offline maps sub-tab, whose picker map is in that panel.
- Solid from the other sub-tabs.
- The wide case, option (c), is **left to J6**, which rebuilds the wide Journal as list-detail (owner's J6 ruling "1 A"). Do not thread `LogPanel`.

**Q4, the Maps tab with no map drawn yet** (loading or error). Planner's ruling, stated and not overruled: surfaces **follow the tab**, 0.8 on the Maps tab.

**Q5, the exit prompt on a wide window.** Planner's ruling, stated and not overruled: it follows the same rule, 0.8 when a map is on screen.

**Your flags:**
- The search dropdown at 0.8 on every compact tab is out of scope; leave it.
- The wide Layers button: change alpha only. If a translucent colour drops its tonal-elevation tint, report it rather than compensating.
- Leave `MapModePicker` flagged, not deleted.

**Everything else in `-56` and `-58` stands:** tests first through the real entry points, revert checks per mechanism, the full suite, the device-only list, and the report (append a "Resumed" section to your stop report). The accent buttons and the 0.55 caption stay as `-58` says.

**Sharing the machine.** Before each Gradle run, check that no other Gradle build is running from another worktree (`pgrep -af GradleWorkerMain`, `pgrep -af 'gradlew'`) and that at least 2.5 GB is available. Wait if either fails. The save-failure coder and two device coders are on this machine.
````

### The coordinator's message relaying -77

````text
This is planner message `2026-09-28-77`, part of dispatch `2026-09-28-56` as amended by `-58`. **Build it.** The full text is at `prompts/preserved/2026-09-28-77.md` on `origin/journal-redesign` at `9c7d0a5`. Fetch it, read it in full and quote it verbatim; the file governs over this summary.

- **Q3, S2, S3 (owner, verbatim "1 A"):** the species suggestions and the Month menu are each at `MAP_CHROME_OVER_MAP_ALPHA` on the Maps tab, stacking over the 0.8 search panel. On tabs with no map they stay solid. This is the owner's exception for these two menus only.
- **Q2 (planner):** "covers a map" means a map is drawn on screen beneath the surface. Do not measure bounds.
- **Q1, S1 (planner):** option (b). The Records details sheet is at 0.8 from the Offline maps sub-tab and solid from the others. The wide case is left to J6; do not thread `LogPanel`.
- **Q4 (planner):** follow the tab.
- **Q5 (planner):** the same rule as everywhere else.
- **Flags:** leave the search dropdown as it is. On the wide Layers button, change alpha only, and report if the elevation tint drops. `MapModePicker` stays flagged.
- **Everything else in `-56` and `-58` stands:** tests first through real entry points, revert checks per mechanism, the full suite, the device-only list, and a "Resumed" section in your report.

**Before every Gradle run:** check that no other Gradle build is running from another worktree (`pgrep -af GradleWorkerMain`), and that at least 2.5 GB is available. Wait if either fails. The save-failure coder is building now. Hand back when you finish or stop.
````

### The coordinator's message resuming -77 after the outage

````text
Planner: resume continuation `2026-09-28-77` of `2026-09-28-56`. Your turn was cut off by a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"). Nothing is wrong with your work, and your context is intact.

Before your next action:
- run `git status` and `git diff --stat` in your worktree, and check that your last edit is present and whole;
- do not assume your last command completed.

If you have uncommitted work, commit it to `record-sheet-alpha-wip` and push it before continuing. Then carry on under `-77`. Before any Gradle run, check that no other Gradle build is running (the save-failure coder shares the machine) and that 2.5 GB is free. Hand back when you finish or stop.
````
