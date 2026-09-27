# Split AvailabilityScreen.kt, Stage G (split build 2): completion report

**Date:** 2026-09-26 (UTC). **Dispatch:** `prompts/preserved/2026-09-26-47.md`, with two planner
rulings quoted in the intent (planner log lines 2312 and 2327). **Record:** intent `2026-09-27-21`,
sweep merge entry `2026-09-27-20`. **Branch:** `split-scaffold` from `origin/pre-main`
`29277b75f8a1d41f34bbbc1b5b767fc5bbd5031d`. **Extraction commit:** `8a90daa`.

A behaviour-preserving extraction, not a pure move. The body of the `compactMainScaffold` lambda
in `app/src/main/java/com/zynergylabs/forager/app/ui/availability/AvailabilityScreen.kt` (`AS`)
moved into a new file in the same package, `AvailabilityCompactScaffold.kt`, as
`internal @Composable fun CompactMainScaffold(...)`. What the lambda captured became 109 explicit
parameters. `AS` keeps the lambda, which now only calls the new function. No test file changed.

The owner chose this as its own build ("Yes, as a second build", on the option "Build 2: the
scaffold extraction on its own, reviewed and tested separately, still no intended behaviour
change"), and authorised the merge into `pre-main` without a device check.

## Two stops, and the rulings that resolved them

The build stopped twice before its intent. Both rulings are quoted verbatim in intent
`2026-09-27-21`.

1. **The dispatch's hoisting pattern changed behaviour.** It passed each written state as a
   value plus a setter. The fullscreen toggle at `AS:1779-1782` writes `isMapFullscreen` and then
   reads it back for `onMapFullscreenChanged`. With a value parameter, that read returns the
   pre-toggle value, so the saved preference would be inverted. The reads of `compactTab` (`:1476`)
   and `isMapFullscreen` (`:1500`) inside `onBottomNavTabSelected` happen at tap time, and a value
   would fix them at composition. **Ruling (line 2312):** every captured `State` becomes a getter,
   `name: () -> T`, and every read in the body becomes `name()`. The seven written states also get
   a setter, `onNameChange: (T) -> Unit`. `selectedTab`, which is written and never read, gets
   the setter only. Plain values and callbacks pass as values.
2. **`ResultsTab` is private to `AS`** (`:323`), and the body names it in the four `selectedTab`
   writes. **Ruling (line 2327):** widen it to `internal`, as permitted edit (d). An
   `@OptIn(ExperimentalMaterial3Api::class)` was pre-approved only if the compiler forced it. It
   did not.

## Premises checked at the base

Every premise held at `29277b7`, checked after `git fetch`:

- `origin/pre-main`, the main checkout and the planner worktree were all at `29277b7`.
- #132's merge had no `merge` entry, and its Carries are `-17` to `-19`.
- `AS` was 2,229 lines, with:
  - `fun AvailabilityScreen` at `:403`;
  - `drawerSheetContent` at `:1011` and `mainScaffold` at `:1202`;
  - `compactMainScaffold` at `:1303-2139`, 837 lines, body `:1304-2138`;
  - `content = compactMainScaffold` at `:2173`;
  - no `rememberSaveable`, `key(` or `currentCompositeKeyHash`.
- The seven written states are as the pulse found them:
  - `isMapFullscreen` at `:1501` and `:1780`;
  - `compactTab` at `:1504`, `:1726` and `:2120`;
  - `selectedTab` at `:1509-1511` and `:2121`;
  - `isDrawerOpen` at `:1487`;
  - `mapMode` at `:1771`;
  - `pendingJournalDestination` at `:1731` and `:1980`;
  - `logPhotoAcquisitionInFlight` at `:1923`.

## What changed

- **New file** `AvailabilityCompactScaffold.kt`, 1,093 lines:
  - the package line;
  - a "Split-AvailabilityScreen Stage G" header naming every parameter and the seven written
    states;
  - 89 imports: the 87 `AS` imports the code names, plus the two below;
  - the 109-line signature;
  - the 835-line body, with its original indentation.
- **`AS`**, 1,505 lines:
  - `:323` is widened from `private` to `internal`;
  - lines `:1304-2138` are replaced by a 111-line call, `CompactMainScaffold(` plus 109 named
    arguments plus `)`.

  Everything else is byte-identical, including the declaration line `:1303`, the comment above
  it, the closing brace and `content = compactMainScaffold`. `AS`'s imports are unchanged.
- **Call site.** The lambda form, `val compactMainScaffold: @Composable () -> Unit = {
  CompactMainScaffold(...) }`. Inlining the call at `:2173` was the alternative. It would have
  changed that line and removed the declaration, so the lambda form is the one that leaves `AS`'s
  other lines byte-identical. At the call site:
  - each getter is passed as `{ name }`;
  - each setter as `{ name = it }`;
  - each value as `name`.
- **Compiler-forced imports (edit (c)).** The first compile failed on:
  ```
  e: .../AvailabilityCompactScaffold.kt:147:19 Unresolved reference 'FocusManager'.
  e: .../AvailabilityCompactScaffold.kt:148:25 Unresolved reference 'SoftwareKeyboardController'.
  ```
  In `AS` these types are inferred and never named, so `AS` has no import for them. The two
  follow-on errors, `clearFocus` and `hide`, cleared with the imports. No `@OptIn` was forced.
  The only warning in the package is the existing `Icons.Filled.MenuBook` deprecation in
  `AvailabilitySettingsUi.kt:144`.

## Checks

### 1. Body verbatim

The base body (`AS:1304-2138` at `29277b7`, 835 lines) was diffed against `CompactMainScaffold`'s
body (835 lines). **45 lines differ**, all one-for-one, with no line added or removed. A checker
undid each rewrite on the new line (`onXChange(e)` back to `x = e`, and `x()` back to `x`) and
compared the result with the base line. **45 of 45 are (a) edits, 0 are not.** The checker was
shown to fail: a planted `0.dp` to `1.dp` change on one rewritten line was reported as "NOT (a)".
A second scan found no bare reference to any of the eight state names left in the body's code,
comments and named-argument labels excluded.

(The checker's first form reported three setter calls inside `{ … },` lambdas as "NOT (a)". Its
pattern had required the line to end at ` }`, and these end at ` },`. That was a checker bug,
fixed before the result above.)

Rewrites by state (base line numbers):

| State | Reads rewritten to `name()` | Writes rewritten to `onNameChange(x)` |
|---|---|---|
| `isMapFullscreen` | 10: `:1354`, `:1500`, `:1566`, `:1600`, `:1702`, `:1778`, `:1780`, `:1781`, `:1874`, `:1990` | 2: `:1501`, `:1780` |
| `compactTab` | 15: `:1407`, `:1476`, `:1543`, `:1623`, `:1630`, `:1648`, `:1650`, `:1670`, `:1702`, `:1746`, `:2043`, `:2044`, `:2058`, `:2095`, `:2131` | 3: `:1504`, `:1726`, `:2120` |
| `selectedTab` | none (setter only) | 4: `:1509`, `:1510`, `:1511`, `:2121` |
| `isDrawerOpen` | 4: `:1651`, `:1671`, `:1783`, `:2132` | 1: `:1487` |
| `mapMode` | 1: `:1770` | 1: `:1771` |
| `pendingJournalDestination` | 1: `:1979` | 2: `:1731`, `:1980` |
| `logPhotoAcquisitionInFlight` | 1: `:1413` | 1: `:1923` |
| `mapTaxonFilter` (read only) | 1: `:1809` | none |

That is 33 reads and 14 writes on 45 lines. `:1780` and `:1702` each carry two rewrites.

### 2. `AS` minimal

Against the base, `AS` changes in two places only:

- `:323`, `private` becomes `internal`;
- `:1304-2138` becomes the 111-line call.

Lines 1-1303 match the base apart from `:323`. The 91 lines from the base's `:2139` to the end
are identical. Every call line has the form `name = { name }`, `name = { state = it }` or
`name = name`, for 7 getters, 7 setters and 95 values. The arguments are in the signature's
order. The import block is identical to the base.

### 3. Parameter ledger

The parameters are ordered state and values first, then callbacks. Within each group they follow
the order of first appearance in the body, counting code only and not named-argument labels.
Function-typed parameters count as callbacks. There are 109:

- 6 getter+setter pairs (12 parameters);
- 1 getter only, `mapTaxonFilter`;
- 1 setter only, `onSelectedTabChange`;
- 95 values.

No parameter is passed as a plain value where it is backed by a `State`. Fifteen values come from
`val`s in `AvailabilityScreen`, the rule-6 cases:

- `focusManager`, `keyboardController`, `isShortLandscapeWindow`, `portEdge`,
  `logDraftSnackbarHostState`, `distanceUnit`, `mapIconClusterPosition`, `mapRenderMode`,
  `isNightMode`, `mapWaypoints`, `isNavigating`, `basemap`, `leaveLogEntryEditingOfferingDiscard`,
  `onViewSpeciesOnMap`, `onClearMapTaxonFilter`.

None of their declarations is delegated (`by`), a `derivedStateOf` or a collected `State`. Each is
computed once in `AvailabilityScreen`'s own scope, where the base also reads it, so passing the
value keeps the read in the same scope. The lambdas among them read `State` live inside
themselves, as before.

| # | Parameter | Type | Kind | Source in `AvailabilityScreen` | First use in body (base line) |
|---|---|---|---|---|---|
| 1 | `isMapFullscreen` | `() -> Boolean` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:738` | `:1354` |
| 2 | `focusManager` | `FocusManager` | value | `val` `:813` | `:1380` |
| 3 | `keyboardController` | `SoftwareKeyboardController?` | value | `val` `:814` | `:1381` |
| 4 | `compactTab` | `() -> CompactTab` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:688` | `:1407` |
| 5 | `logUiState` | `MushroomLogUiState` | value | parameter `:466` | `:1407` |
| 6 | `logPhotoAcquisitionInFlight` | `() -> Boolean` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:695` | `:1413` |
| 7 | `cartographyUiState` | `CartographyUiState` | value | parameter `:515` | `:1420` |
| 8 | `isDrawerOpen` | `() -> Boolean` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:815` | `:1487` |
| 9 | `isShortLandscapeWindow` | `Boolean` | value | `val` `:954` | `:1542` |
| 10 | `portEdge` | `ScreenEdge` | value | `val` `:956` | `:1565` |
| 11 | `logDraftSnackbarHostState` | `SnackbarHostState` | value | `val` `:966` | `:1606` |
| 12 | `uiState` | `AvailabilityUiState` | value | parameter `:404` | `:1704` |
| 13 | `distanceUnit` | `DistanceUnit` | value | `val` `:790` | `:1705` |
| 14 | `pendingJournalDestination` | `() -> PendingJournalDestination?` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:822` | `:1731` |
| 15 | `currentTime` | `CurrentTimeProvider` | value | parameter `:436` | `:1749` |
| 16 | `mapSlot` | `MapSlot` | value | parameter `:653` | `:1756` |
| 17 | `mapIconClusterPosition` | `MapIconClusterPositionState` | value | `val` `:766` | `:1757` |
| 18 | `mapRenderMode` | `MapRenderMode` | value | `val` `:787` | `:1769` |
| 19 | `mapMode` | `() -> MapMode` | getter (of getter+setter) | `var` by `remember { mutableStateOf }` `:778` | `:1770` |
| 20 | `isNightMode` | `Boolean` | value | `val` `:786` | `:1772` |
| 21 | `isRecording` | `Boolean` | value | parameter `:575` | `:1793` |
| 22 | `startRecordingErrorMessage` | `String?` | value | parameter `:578` | `:1795` |
| 23 | `breadcrumbPoints` | `List<LatLng>` | value | parameter `:596` | `:1796` |
| 24 | `mapWaypoints` | `List<Waypoint>` | value | `val` `:728` | `:1797` |
| 25 | `returnToStart` | `ReturnToStartInfo?` | value | parameter `:616` | `:1799` |
| 26 | `isReturning` | `Boolean` | value | parameter `:618` | `:1800` |
| 27 | `isNavigating` | `Boolean` | value | `val` `:725` | `:1801` |
| 28 | `isOffTrack` | `Boolean` | value | parameter `:620` | `:1802` |
| 29 | `compassProvider` | `CompassProvider` | value | parameter `:626` | `:1804` |
| 30 | `computeTrueHeading` | `ComputeTrueHeadingUseCase` | value | parameter `:634` | `:1805` |
| 31 | `navigationTarget` | `Waypoint?` | value | parameter `:641` | `:1806` |
| 32 | `pathHomeMeters` | `Double?` | value | parameter `:648` | `:1807` |
| 33 | `mapTaxonFilter` | `() -> Long?` | getter | `var` by `remember { mutableStateOf }` `:703` | `:1809` |
| 34 | `basemap` | `Basemap` | value | `val` `:779` | `:1913` |
| 35 | `tracks` | `List<Track>` | value | parameter `:667` | `:1972` |
| 36 | `waypoints` | `List<Waypoint>` | value | parameter `:602` | `:1975` |
| 37 | `waypointsErrorMessage` | `String?` | value | parameter `:604` | `:1976` |
| 38 | `waypointEntryReferenceCounts` | `Map<String, Int>` | value | parameter `:606` | `:1978` |
| 39 | `onLocateMe` | `() -> Unit` | value | parameter `:563` | `:1391` |
| 40 | `onLeaveLogEntryEditingIncidentally` | `() -> Unit` | value | parameter `:502` | `:1406` |
| 41 | `leaveLogEntryEditingOfferingDiscard` | `() -> Unit` | value | `val` `:982` | `:1477` |
| 42 | `onDismissTaxonSuggestions` | `() -> Unit` | value | parameter `:415` | `:1486` |
| 43 | `onIsDrawerOpenChange` | `(Boolean) -> Unit` | setter (of getter+setter) | writes `isDrawerOpen`, `var` by `remember { mutableStateOf }` `:815` | `:1487` |
| 44 | `onIsMapFullscreenChange` | `(Boolean) -> Unit` | setter (of getter+setter) | writes `isMapFullscreen`, `var` by `remember { mutableStateOf }` `:738` | `:1501` |
| 45 | `onMapFullscreenChanged` | `(Boolean) -> Unit` | value | parameter `:460` | `:1502` |
| 46 | `onCompactTabChange` | `(CompactTab) -> Unit` | setter (of getter+setter) | writes `compactTab`, `var` by `remember { mutableStateOf }` `:688` | `:1504` |
| 47 | `onSelectedTabChange` | `(ResultsTab) -> Unit` | setter (setter-only) | writes `selectedTab`, `var` by `remember { mutableStateOf }` `:680` | `:1509` |
| 48 | `onUseCurrentLocation` | `() -> Unit` | value | parameter `:405` | `:1706` |
| 49 | `onTaxonSearchQueryChanged` | `(String) -> Unit` | value | parameter `:413` | `:1710` |
| 50 | `onTaxonSearchResultSelected` | `(TaxonSearchResult) -> Unit` | value | parameter `:414` | `:1711` |
| 51 | `onPendingJournalDestinationChange` | `(PendingJournalDestination?) -> Unit` | setter (of getter+setter) | writes `pendingJournalDestination`, `var` by `remember { mutableStateOf }` `:822` | `:1731` |
| 52 | `onStartLogEntry` | `(LatLng?, LocalDate) -> Unit` | value | parameter `:483` | `:1732` |
| 53 | `onViewSpeciesOnMap` | `(Long) -> Unit` | value | `val` `:709` | `:1751` |
| 54 | `onMapModeChange` | `(MapMode) -> Unit` | setter (of getter+setter) | writes `mapMode`, `var` by `remember { mutableStateOf }` `:778` | `:1771` |
| 55 | `onPlaceTripPin` | `(LatLng, LocalDate, String) -> Unit` | value | parameter `:418` | `:1773` |
| 56 | `onToggleRecording` | `() -> Unit` | value | parameter `:576` | `:1794` |
| 57 | `onDropWaypoint` | `(LatLng, String) -> Unit` | value | parameter `:608` | `:1798` |
| 58 | `onToggleReturning` | `() -> Unit` | value | parameter `:621` | `:1803` |
| 59 | `onClearMapTaxonFilter` | `() -> Unit` | value | `val` `:715` | `:1810` |
| 60 | `onManualLatChanged` | `(String) -> Unit` | value | parameter `:406` | `:1816` |
| 61 | `onManualLngChanged` | `(String) -> Unit` | value | parameter `:407` | `:1817` |
| 62 | `onSearchManualCoordinates` | `() -> Unit` | value | parameter `:408` | `:1818` |
| 63 | `onOpenCamera` | `(InAppCameraTarget) -> Unit` | value | parameter `:475` | `:1907` |
| 64 | `onOpenLogEntry` | `(String) -> Unit` | value | parameter `:484` | `:1915` |
| 65 | `onCloseLogEntry` | `() -> Unit` | value | parameter `:485` | `:1916` |
| 66 | `onLogEntryChanged` | `(MushroomLogEntry) -> Unit` | value | parameter `:486` | `:1918` |
| 67 | `onStartEditingLogEntry` | `() -> Unit` | value | parameter `:488` | `:1919` |
| 68 | `onSaveLogEntry` | `() -> Unit` | value | parameter `:498` | `:1920` |
| 69 | `onCancelLogEntryEditing` | `() -> Unit` | value | parameter `:500` | `:1921` |
| 70 | `onLogPhotoAcquisitionInFlightChange` | `(Boolean) -> Unit` | setter (of getter+setter) | writes `logPhotoAcquisitionInFlight`, `var` by `remember { mutableStateOf }` `:695` | `:1923` |
| 71 | `onAddLogPhoto` | `(PhotoSource) -> Unit` | value | parameter `:505` | `:1924` |
| 72 | `onRemoveLogPhoto` | `(LogPhoto) -> Unit` | value | parameter `:506` | `:1925` |
| 73 | `onPullLogPhoto` | `(LogPhoto) -> Unit` | value | parameter `:507` | `:1926` |
| 74 | `onDeleteLogEntry` | `(String) -> Unit` | value | parameter `:508` | `:1927` |
| 75 | `onSaveLogErrorDismissed` | `() -> Unit` | value | parameter `:513` | `:1928` |
| 76 | `onDeleteGalleryPhoto` | `(GalleryPhoto) -> Unit` | value | parameter `:509` | `:1934` |
| 77 | `onAddGalleryPhoto` | `(PhotoSource) -> Unit` | value | parameter `:511` | `:1935` |
| 78 | `onOpenCartographyEntry` | `(String) -> Unit` | value | parameter `:516` | `:1941` |
| 79 | `onStartCartographyEntry` | `(LocalDate) -> Unit` | value | parameter `:517` | `:1942` |
| 80 | `onCloseCartographyEntry` | `() -> Unit` | value | parameter `:518` | `:1943` |
| 81 | `onCartographyTextChanged` | `(String) -> Unit` | value | parameter `:519` | `:1944` |
| 82 | `onCartographyTagsChanged` | `(List<String>) -> Unit` | value | parameter `:520` | `:1945` |
| 83 | `onSetFindDecision` | `(String, Boolean) -> Unit` | value | parameter `:521` | `:1946` |
| 84 | `onSetTrackDecision` | `(String, Boolean) -> Unit` | value | parameter `:522` | `:1947` |
| 85 | `onSetWaypointDecision` | `(String, Boolean) -> Unit` | value | parameter `:523` | `:1948` |
| 86 | `onSetOfflineRegionDecision` | `(Long, Boolean) -> Unit` | value | parameter `:524` | `:1949` |
| 87 | `onToggleKeptPhoto` | `(String) -> Unit` | value | parameter `:525` | `:1950` |
| 88 | `onAcquirePhotoForCartographyEntry` | `(PhotoSource) -> Unit` | value | parameter `:527` | `:1951` |
| 89 | `onFinishCartographyEntry` | `() -> Unit` | value | parameter `:528` | `:1952` |
| 90 | `onSaveCartographyEntry` | `() -> Unit` | value | parameter `:530` | `:1953` |
| 91 | `onDiscardCartographyEntryChanges` | `() -> Unit` | value | parameter `:532` | `:1954` |
| 92 | `onSaveCartographyEntryAsDraft` | `() -> Unit` | value | parameter `:534` | `:1955` |
| 93 | `onDeleteCartographyEntry` | `(String) -> Unit` | value | parameter `:535` | `:1956` |
| 94 | `getCartographyEntryMapData` | `suspend (CartographyEntry, List<GalleryPhoto>) -> CartographyEntryMapData` | value | parameter `:542` | `:1957` |
| 95 | `getCartographyEntryOfflineRegion` | `suspend (CartographyEntry, List<LatLng>) -> OfflineRegionSummary?` | value | parameter `:551` | `:1958` |
| 96 | `getCartographyEntryCurrentLocation` | `suspend () -> LocationResult` | value | parameter `:557` | `:1959` |
| 97 | `onOfflineMapLatChanged` | `(String) -> Unit` | value | parameter `:438` | `:1965` |
| 98 | `onOfflineMapLngChanged` | `(String) -> Unit` | value | parameter `:439` | `:1966` |
| 99 | `onOfflineMapRadiusChanged` | `(Int) -> Unit` | value | parameter `:440` | `:1967` |
| 100 | `onOfflineMapNameChanged` | `(String) -> Unit` | value | parameter `:441` | `:1968` |
| 101 | `onOfflineMapsOpened` | `() -> Unit` | value | parameter `:442` | `:1969` |
| 102 | `onDownloadOfflineMaps` | `() -> Unit` | value | parameter `:443` | `:1970` |
| 103 | `onDeleteOfflineRegion` | `(Long) -> Unit` | value | parameter `:444` | `:1971` |
| 104 | `onTracksOpened` | `() -> Unit` | value | parameter `:669` | `:1973` |
| 105 | `getFullRecord` | `suspend (String) -> Result<List<TrackPointRecord>>` | value | parameter `:677` | `:1974` |
| 106 | `onDeleteWaypoint` | `(String) -> Unit` | value | parameter `:609` | `:1977` |
| 107 | `onRecentSearchSelected` | `(CachedSearchSummary) -> Unit` | value | parameter `:425` | `:2101` |
| 108 | `onRadiusChanged` | `(Int) -> Unit` | value | parameter `:409` | `:2112` |
| 109 | `onMonthSelected` | `(Int) -> Unit` | value | parameter `:410` | `:2113` |

### 4. Writes wired

| Write | Test through the real screen | Status |
|---|---|---|
| `isMapFullscreen` (fullscreen toggle) | `AvailabilityScreenMapIconStackTest`, "entering and leaving fullscreen writes the preference through, and launch never writes it" (the icon bar's row, by real touch) and the fullscreen layout tests | covered; revert-checked (R1) |
| `compactTab` (tab switch) | `AvailabilityScreenMapIconStackTest`, "all five bottom nav destinations are present, and Tools opens the drawer as an overlay over List rather than switching tabs"; `AvailabilityScreenSeasonalTabTest` | covered; revert-checked (R2) |
| `selectedTab` | none found that observes it | **coverage gap, unverified**, see below |
| `isDrawerOpen` (drawer open) | `AvailabilityScreenMapIconStackTest`, "the Tools tab opens the drawer" | covered by name; not revert-checked |
| `mapMode` (map mode) | `AvailabilityScreenSettingsPanelTest`, "tapping the quick-fire icon opens the map mode picker, and a chip there changes the basemap" | covered by name; not revert-checked |
| `pendingJournalDestination` (journal destination) | none through the screen | **coverage gap** |
| `logPhotoAcquisitionInFlight` (photo acquisition) | none | **coverage gap** |
| The value passed to `onMapFullscreenChanged` (the eighth line, from the first ruling) | the same `AvailabilityScreenMapIconStackTest` test as the first line; it records every value written to the preference store (`[true]`, `[true, false]`, …) | covered; revert-checked (R3) |

**The gaps.**

- **`selectedTab`.** Two kinds of screen test switch compact tabs, and neither observes the
  write:
  - `AvailabilityScreenSeasonalTabTest` and `AvailabilityScreenLayoutTest` pass
    `onSeasonalTabSelected = {}`, a no-op;
  - "the bottom nav's three destinations select the same ResultsTab the old tab row did" asserts
    only whether `map-slot` is present, and that is driven by `compactTab`.

  Several classes pass the real view model's `onSeasonalTabSelected`. I did not establish whether
  any of their assertions depends on the write, and I did not revert-check it. So "no test
  observes `selectedTab`" is unverified, not shown.
- **`pendingJournalDestination`.** It is exercised only by `JournalTabTest`, which composes
  `JournalTab` directly. Both screen-level "Log a find" tests are `@Ignore`d:
  - `AvailabilityScreenMapIconStackTest`, "the add button opens the same plan-or-log chooser…";
  - `AvailabilityScreenTripPlanningFlowTest`, "choosing Log a find calls onStartLogEntry…".
- **`logPhotoAcquisitionInFlight`.** No test was found that backgrounds the app while a find's
  photo acquisition is in flight.

**Revert checks.** The runner followed CLAUDE.md's rules for every check:

- it saved a copy of the file before editing;
- it asserted that the edit matched exactly one line;
- it deleted `app/build/test-results/testDebugUnitTest` before running;
- it refused to cite results if the build log had an `e:` line (none did);
- it restored the file from the saved copy, not from git, and checked its sha256 against the
  pre-edit file;
- it checked `git status` afterwards.

After all three checks the tree was clean, and both files were byte-identical to the committed
forward versions.

- **R1, `isMapFullscreen`'s setter as a no-op.** The call-site argument
  `onIsMapFullscreenChange = { isMapFullscreen = it }` became `{ }`.
  `AvailabilityScreenMapIconStackTest` had 103 tests: 13 failed and 19 were skipped. Every
  failure is a fullscreen test. The persistence test failed with
  `java.lang.AssertionError: expected:<[true]> but was:<[false]>`. Others failed with:
  - `The component with ContentDescription = 'Exit fullscreen' (ignoreCase: false) is not displayed!`;
  - `Failed: assertDoesNotExist.`, in "fullscreen removes the search bar and the compass strip
    moves flush to the top edge, both restored on exit";
  - `expected fullscreen's own drag range to reach below the nav's top (560.0.dp)…`.

  The two "persisted fullscreen preference" launch tests passed, as predicted. They set the state
  through `AS`'s launch effect, not through the setter.
- **R2, `compactTab`'s setter as a no-op.** `onCompactTabChange = { compactTab = it }` became
  `{ }`. Across `AvailabilityScreenMapIconStackTest` and `AvailabilityScreenSeasonalTabTest` (112
  tests), 11 failed. Every failure is a tab switch that did not happen:
  - `Failed: assertDoesNotExist.` on `map-slot` after tapping List, in "all five bottom nav
    destinations…" and "the bottom nav's three destinations…";
  - "the search dropdown starts flush at the top on the List tab…";
  - 8 of the 9 `AvailabilityScreenSeasonalTabTest` tests, for example
    `…contains 'Estimate from 25 observations with a known date, not a guarantee.' … is not displayed!`.

  The ninth, "a loading state shows a progress indicator rather than stale content", passed. Its
  only assertion is `assertDoesNotExist`, which also holds on the Map tab, so it cannot fail when
  the tab does not switch.
- **R3, the read after the write.** `AvailabilityCompactScaffold.kt`'s
  `onMapFullscreenChanged(isMapFullscreen())` became `onMapFullscreenChanged(!isMapFullscreen())`.
  Right after the toggle, `!new` equals the old value, so this is the value captured before the
  write, which the rejected value-parameter form would have passed. Of 103 tests, **exactly 1
  failed**: "entering and leaving fullscreen writes the preference through, and launch never
  writes it", `java.lang.AssertionError: expected:<[true]> but was:<[false]>`. The getter ordering
  is load-bearing, and a test covers it.

### 5. Suite

The full unit suite, `./gradlew --stacktrace testDebugUnitTest`, was run twice. The JUnit XML,
`per-class-counts.tsv`, `summary.tsv` (per test case) and the Gradle logs are kept outside the
repository.

| | Commit | Classes | Tests | Failures | Errors | Skipped | Kept in |
|---|---|---|---|---|---|---|---|
| Before | `36a0fe2` (`app/` identical to `29277b7`) | 217 | 1,681 | 0 | 0 | 24 | `~/Zynergy/forager-scaffold-baseline/` |
| After | `8a90daa` | 217 | 1,681 | 0 | 0 | 24 | `~/Zynergy/forager-scaffold-after/` |

**Differences: 0.** Both `per-class-counts.tsv` and both `summary.tsv` (1,681 test cases each) are
byte-identical.

**Checks that the "after" run is current:**

- The results directory was deleted first.
- The log shows `Task :app:testDebugUnitTest` executed, with no `e:` line.
- The tree was clean at `8a90daa`.
- `AvailabilityCompactScaffoldKt.class` exists in `app/build/tmp/kotlin-classes/debug`.

**What this shows.** Passing the same before and after is what this build predicts, so the suite
is regression evidence only. The body's faithfulness rests on checks 1 and 2, and the wiring on
check 4's reverts.

### 6. Line counts

| File | Before | After |
|---|---|---|
| `AvailabilityScreen.kt` | 2,229 | 1,505 |
| `AvailabilityCompactScaffold.kt` | — | 1,093 |

## Predictions against outcome

- **Planner 1, `AS` at about 1,350-1,450 lines: missed.** It is 1,505. The call puts each of its
  109 arguments on its own line, in the style of the `CompactMapTab` call, which takes 111 lines
  for the call alone. 2,229 - 835 + 111 = 1,505. The coder's prediction of 1,505 was met.
- **Planner 2, 90-110 parameters (as revised by the first ruling): met.** There are 109, the
  figure the capture scan gave before the build.
- **Planner 3, suite identical with no test changes: met.**
- **Coder mechanism prediction:**
  - Met: no `@OptIn`, and the new file came in at about 1,100 lines (1,093).
  - Met: R1, R2 and R3 each failed as predicted.
  - Missed: "imports only" was right, but I had not expected two imports that `AS` itself lacks.

## Not verified

- Behaviour on a device. No device check was run, as dispatched.
- Whether any screen test observes the `selectedTab` write (see check 4).
- `isDrawerOpen` and `mapMode` were not revert-checked. Their tests are named by reading them.
- `assembleDebug` was not run locally. CI runs it.
- Compose group keys change with the extraction, because the body now runs in its own function's
  group. The file has no `rememberSaveable`, so no saved state is keyed on them. Whether a
  composable the body calls in another file uses `rememberSaveable` was not checked.
