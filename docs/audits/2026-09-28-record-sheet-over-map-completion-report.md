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
