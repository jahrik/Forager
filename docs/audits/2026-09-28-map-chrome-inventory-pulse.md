# Map chrome inventory pulse (read-only, at `76a67f3`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `76a67f3` (clean worktree, `git rev-parse HEAD` matched the remote). Nothing was built, no tests ran and no adb command ran.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with every citation kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

**Why:** the owner's principle, verbatim: "My idea is that nothing should fully obstruct the map view. All map chrome gets 80% opacity as a result." (now in CLAUDE.md, UX defaults, and in `docs/plans/journal-redesign.md`, "Map chrome at 80%, nothing fully obstructs the map"). Dispatch `2026-09-28-56` is queued behind J8 and is to be widened from this inventory.

**Tags:** *(comment)* means the claim comes from a code comment. *(inferred)* means the pulse's reasoning. *(library default, not read)* means Material3 behaviour the pulse did not open. Material3 is `1.5.0-alpha26` (`gradle/libs.versions.toml:19`).

**Alpha sources.** "0.8 constant" is `MAP_CHROME_OVER_MAP_ALPHA` (`ui/map/MapChrome.kt:239`, used at `:233` and `:236`). "0.8 literal" is a separate `0.8f` that does not follow the constant. "0.8 composite" is a 0.5 child over a 0.6 container (`MapChromeAlphaTest.kt:17-20`). "Opaque" means a `0xFF…` colour; every theme colour named here is opaque (`Color.kt`).

## Premises that were wrong

1. The day-entry **editor has no map**. `CartographyEntryEditScreen` takes no `mapSlot`, and `CartographyScreen.kt:321-353` passes none. The only map in a Journal edit flow is the find editor's location picker (`JournalTab.kt:428`, `LogPanel.kt:318`).
2. **Trip planning, "Set on map" and the HUD have no map of their own.** They draw over the Maps tab's map. The Tools drawer's Trip Planner has no `mapSlot` (`AvailabilityTripsWaypointsUi.kt`).
3. **`ModalNavigationDrawer` renders in-tree, not in a window** *(inferred)*, from `docs/audits/2026-09-27-landscape-b3-drawer-completion-report.md:33-38` and the comment at `AvailabilitySearchUi.kt:316-318`.

## The four map surfaces

There is one `MapView` constructor (`ui/map/SightingsMap.kt:272`, in an `AndroidView` at `:752`), behind `SightingsMapSlot` (`ui/map/MapSlot.kt:457`), and there are four `mapSlot(...)` calls:

| # | Surface | Map call | Reached from |
|---|---|---|---|
| M1 | Compact Maps tab: portrait, short landscape, fullscreen | `AvailabilityCompactMapUi.kt:634` | `AvailabilityCompactScaffold.kt:780`; short windows take the compact tree (`AvailabilityScreen.kt:1690`) |
| M2 | Wide Maps tab | `AvailabilityWideLayoutUi.kt:275` | `AvailabilityScreen.kt:1517`; a map only after a search (`:228`, `:249`) |
| M3 | Journal entry map: report preview and fullscreen | `CartographyEntryReportScreen.kt:418` | modifier switches at `:405-413`; `CartographyScreen.kt:355` via `JournalTab.kt:594` and `LogPanel.kt:430` |
| M4 | Centre-pin picker maps | `CentrePinLocationPicker.kt:200` | find picker (`JournalTab.kt:428`, `LogPanel.kt:318`); offline-region picker (`AvailabilityOfflineMapsUi.kt:172`, `:301`, `:377`, via `RecordsTab.kt:282`) |

Overlays on M1 with no map of their own: placement (`AvailabilityCompactMapUi.kt:1316`; wide `AvailabilityWideLayoutUi.kt:365`), "Set on map" (`AvailabilityCompactMapUi.kt:1337`, from `AvailabilityCompactScaffold.kt:1197-1202`), the HUD (`AvailabilityCompactMapUi.kt:1113`).

## What is drawn over them

### Already at 80%

| Item | Code | Alpha | Also where no map |
|---|---|---|---|
| Search bar (text field) | `AvailabilitySearchUi.kt:205`, `Surface` `:261`, fill `:262` | 0.8 constant | yes, on other tabs (`AvailabilityCompactScaffold.kt:706`), same 0.8 |
| Bubble card and tail | `ui/map/MapBubble.kt:154`, `Surface` `:194`, fill `:163`, tail `:189` | 0.8 constant | no |
| Icon bar in the cluster | `MapChrome.kt:343`, `Surface` `:403`, fill `:405` via `AvailabilityCompactMapUi.kt:996` | 0.8 composite (0.5 over the 0.6 container at `AvailabilityCompactMapUi.kt:957`, `:962`, `MapChrome.kt:267-274`) | no; the container alone reads 0.6 where uncovered, as intended *(comment, `MapChrome.kt:250-253`)* |
| Control pill | `AvailabilityMapControlsUi.kt:177`, `Surface` `:188`, fill `:191` | 0.8 composite | no |
| Minimise and restore handles | `MapChrome.kt:471`, `:552`; `:612`, `:646-647` (border 0.7, `:652`) | 0.8 constant | no |
| Compass/elevation strip | `AvailabilityMapControlsUi.kt:297`, `:325-328`, colours `:91`, `:94` | **0.8 literal** | no |
| Taxon chip | `AvailabilityMapOverlaysUi.kt:291`, `:293`, `:296` | 0.8 constant | no (also M2) |
| Navigation HUD | `NavigationHud.kt:171`, `:203-206` | **0.8 literal** (strip colours) | no |
| Legend chip | `ui/map/MapLayersSheet.kt:435`, `:443`, `:445` | 0.8 constant | no (also M2) |
| Bottom nav over the map | `AvailabilityNavigationUi.kt:127`; `AvailabilityCompactMapUi.kt:1210` | **0.8 literal** | yes, opaque on other tabs (`AvailabilityNavigationUi.kt:142`, `AvailabilityCompactScaffold.kt:624`) |
| Rail over the map (short landscape) | `AvailabilityNavigationUi.kt:198`; `AvailabilityCompactMapUi.kt:1247` | **0.8 literal** | yes, opaque (`AvailabilityCompactScaffold.kt:644`, `:1211`) |
| Add-menu tile | `AvailabilityMapControlsUi.kt:573`, `:577` | 0.8 constant | no |
| Search dropdown (text fields) | `AvailabilitySearchUi.kt:366`, `:396-399` | **0.8 literal** | yes, over other tabs |
| Entry-map fullscreen icon bar | `CartographyEntryReportScreen.kt:478`, default fill `MapChrome.kt:405` | 0.8 constant, one layer | no |
| Layers sheet (window) | `MapLayersSheet.kt:215-232` | 0.8 constant | no |

### Not at 80% today

| Item | Code | Alpha | Over a map where | Also where no map |
|---|---|---|---|---|
| Record details sheet (window) | `ui/log/RecordDetailsSheet.kt:141`, `:163-167` | opaque default | M1 to M3 via the bubble (`MapBubble.kt:305`) | Journal Records (`RecordsTab.kt:316`) |
| Centre-pin confirm row (OK/Cancel) | `CentrePinLocationPicker.kt:265`, `:289-297`, `:295` | opaque (`surface`) | M1, M2 (`AvailabilityWideLayoutUi.kt:365`) | no |
| Search notice banner | `AvailabilitySearchUi.kt:579`, `:590` | opaque (`errorContainer`, `Color.kt:121-122`) | M1 (`AvailabilityCompactScaffold.kt:947`), when there is a message | yes (`:721`; wide `AvailabilityScreen.kt:1484`, above the map) |
| Wide Layers button | `AvailabilityWideLayoutUi.kt:165`, `:173`, `:175` | opaque (`surface`) | M2 | no |
| Wide Add button | `AvailabilityWideLayoutUi.kt:357`, `:361`; `MapChrome.kt:739` | opaque accent (the 0.8 branch `:740-741` has no caller) | M2 | no |
| Add-row disc and record disc | `MapChrome.kt:389-397`, `:693-698`; `AvailabilityMapControlsUi.kt:207`; `MapIconBarAccent.kt:34-37` | opaque accent | M1 | no |
| Attribution caption | `ui/map/SightingsMap.kt:803-812`, `:810` | 0.55 literal | M1 to M4 | no |
| Add-menu scrim | `AvailabilityMapControlsUi.kt:544-554`, `:548` | 0.32 literal | M1 | no |
| Tools drawer and scrim | `AvailabilityScreen.kt:1724`, sheet `:1739` | defaults *(library default, not read)* | over any tab, M1 included | yes |
| Dialogs: trip date (text field), waypoint name (text field, opaque `surface`, `:182`, `:189`), three-way action, exit navigation, entry delete | M1 `:1352`, `:1363`; `AvailabilityScreen.kt:1061`; M2 `AvailabilityWideLayoutUi.kt:397`, `:415`, `:426`, `:451`; M3 `CartographyEntryReportScreen.kt:616` | defaults or opaque | partly cover the map | — |
| Popups: taxon suggestions, Month menu, entry overflow menu | `AvailabilitySearchUi.kt:1118`, `:1165`; `CartographyEntryReportScreen.kt:378` | defaults *(not read)* | M1, M1, M3; overlap not determined | — |
| Snackbar | host `AvailabilityCompactScaffold.kt:581`; wide `AvailabilityScreen.kt:1784-1787` | default *(not read)* | over the map *(comment, `AvailabilityScreen.kt:1137-1140`)* | yes |
| Photo viewer (window, full screen) | `PhotoViewerDialog.kt:129-140`, from `MapBubble.kt:320` | opaque black | fully covers M1 to M3 | yes (four sites with no map) |
| Find-over-view page | `JournalTab.kt:712`, `LogPanel.kt:517` | opaque, by design *(comment, `:707-709`)* | fully covers M3 | — |

Separate windows (sheets, dialogs, popups) take Material3's default scrim where none is passed. For the `ModalBottomSheet` default this is `colorScheme.scrim` at 0.32, as read in `docs/audits/2026-09-28-layers-sheet-alpha-completion-report.md:28,46-48`, not re-derived.

## Tests that pin these colours

- `MapChromeAlphaTest.kt:17-20`, `:23-28` and `:32-36` pin the composite, the 0.6 and 0.5 fills, and the icon-stack colours.
- `MapLayersSheetTest.kt:70-77` and `:80-87` pin the Layers sheet's container and content colours.
- Nothing else pins them. The `0.8f` hits elsewhere in tests are touch fractions, and no screenshot tooling is configured.
- `AvailabilityScreenMapLayersTest.kt:94` assumes the Layers sheet is its own window.

## Layout-dependent or unknown

- **Translucency on a device.** The comment at `AvailabilityCompactMapUi.kt:326-339` says a translucent `Surface` composed one level outside the map's `Box` "shipped fully opaque on a real device". The search dropdown and the drawer sit in that position. The Layers sheet's 80% has never been seen on a device (its report `:111-118`). The app configures no `MapView` surface type.
- **Details sheet from the Records tab.** It covers a map (the offline-region picker, M4) only if that map is on screen when a region row is tapped.
- **Search dropdown.** It can cover the offline picker on Journal, Records. It cannot open over an entry map, because the search bar is hidden while an entry is open (`AvailabilityCompactScaffold.kt:416`, `:705`) *(inferred)*.
- **Not determined:** where the snackbar and the popups land relative to the map; whether a map is ever under the in-app camera (`AvailabilityScreen.kt:1808`); Material3's default fills for the drawer, dialogs, menus, snackbar, chips and buttons; and what `tonalElevation` does to a non-surface colour.

## Other findings

- **`MapModePicker` is dead code.** It is at `MapChrome.kt:127`, with a 0.8 panel and a 0.32 scrim (`:148`). Nothing calls it in main or in tests; three files only import it.
- **Stale comments that say "opaque" where the code is 0.8:** `AvailabilitySearchUi.kt:330`, `AvailabilityCompactScaffold.kt:316`, `AvailabilityScreenMapIconStackTest.kt:2181`, `AvailabilityCompactMapUi.kt:707`, and `MapChrome.kt:294`, `:454-455` and `:716-718`.
- **Five 0.8 literals** do not follow the constant: the strip, the HUD, the dropdown, the nav and the rail. Changing the constant alone would miss them.
