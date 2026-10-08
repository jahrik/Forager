# Search dropdown reordered: code and tests written, nothing compiled yet

Dispatch 2026-09-28-697 (RECORD intent -697; preserved at `prompts/preserved/2026-10-07-17.md`), with the owner's addition relayed
by the planner: "Search on the right, set on map on the left". Branch `search-order`, cut from `origin/main` at `b71c1569` after a
fresh fetch. No PR.

**Status: written, not run.** The dispatch holds Gradle for the planner's go. Nothing on this branch has been compiled, no test has
run, and no revert check has been done. Every claim below about behaviour is what the code is written to do, not something observed.

Paths are relative to `app/src/main/java/com/zynergylabs/forager/app/` unless they start with `app/` or `docs/`.

## Premises checked

| Premise in the dispatch | Found | Where |
|---|---|---|
| The dropdown body is about `AvailabilitySearchUi.kt:405-505` | True | `ui/availability/AvailabilitySearchUi.kt:405-505` at `b71c1569` |
| "Search this location" and "Use current location" are today's two search buttons | True | same file, `:433-437` and `:498-503` at `b71c1569` |
| `coordinatesKeptInView` is the scroll-to-bottom-on-open | True, with a second part: `scrollToCoordinatesPending` (`:385`, `:473-481`) does the one scroll, `coordinatesKeptInView` (`:395-404`) keeps the end in view after it | same file |
| The coordinates are "Prefilled with the current position, as today" | **False.** Nothing fills the fields when the dropdown opens. They start empty (`ui/availability/AvailabilityUiState.kt:37-38`) and are written only by a successful "Use current location" (`ui/availability/AvailabilityViewModel.kt:485-486`), Set on map's OK (`ui/availability/AvailabilityCompactScaffold.kt:1135-1136` at `b71c1569`) and a recent search (`AvailabilityViewModel.kt:902-903`). So they hold the last of those, or nothing. | as cited |

The false premise is what the stop below is about.

## Stop: what Search does about the current position

The two paths differ, which the dispatch named as a stop.

**"Use current location"** (`MainActivity.kt:502-509`, `:281-283`; `ui/availability/AvailabilityViewModel.kt:475-498`): asks for
location permission; granted, it takes one live fix, writes it into both fields and searches there; denied, it sets
`locationPermissionDenied` and the banner reads "Location permission was denied. Open search options and enter coordinates
manually."; no fix, the banner reads "Couldn't determine your location. Enter coordinates manually instead."

**"Search this location"** (`MainActivity.kt:522`; `AvailabilityViewModel.kt:463-473`): parses the two fields; blank or out of
range, the banner reads "Enter a valid latitude (-90 to 90) and longitude (-180 to 180)." and nothing is searched; valid, it
searches there.

As written on this branch, **Search runs the second path only**, which is what the dispatch's words say ("Search searches the
coordinates in the fields"). That leaves the dropdown with no way to search where the user stands: on a first launch the fields are
blank and Search shows the error; after any search they hold that search's place. The map's locate-me button only pans the map
(`AvailabilityViewModel.kt` `locateMe`, documented as touching no search state). `SearchDropdown` keeps its `onUseCurrentLocation`
parameter, still wired by its caller and drawn by no control, so any of the options below is a small change. The options, for the
owner:

- **A. As built.** Search searches the fields as they stand. Blank fields show the validation error. The current position is reached
  only by Set on map. Simplest; it asks the user to know their coordinates or find themselves on the map.
- **B. Blank fields search the current position.** Search with both fields blank runs the "Use current location" path (permission,
  one fix, fill, search); with anything typed, the coordinate path. After any search the fields are no longer blank, so getting back
  to "where I am" means clearing both fields, which nothing on screen says.
- **C. Prefill on open from the live position.** Each time the dropdown opens, the fields are filled from the position the compass
  strip already shows (`AvailabilityUiState.liveFix`, `ui/availability/AvailabilityUiState.kt:286`), so "left as prefilled, that is
  the current position", as the dispatch describes. Typing over them stands until the dropdown closes. With no live position (no
  permission yet, or no fix) the fields would fall back to B's path or stay as they are; which one is a further choice. Cost: the
  fields no longer show the last search's place on reopening (the bar's summary and Recent searches still do).

C matches the owner's words most closely but is new behaviour, not "as today", so it is not built.

**Copy, not changed:** both banners above say "enter coordinates manually", which was also the name of the removed fold. They still
read as an instruction, and the coordinates are now the dropdown's first row. Flagged, not edited.

## What was built

Commits `0777e671` (code) and `fdcc63ab` (tests), pushed to `origin/search-order`.

`ui/availability/AvailabilitySearchUi.kt`, `SearchDropdown`, top to bottom:

1. Latitude and Longitude, side by side, no heading, no fold. Same fields, same callbacks, same validation (in the ViewModel,
   unchanged). Test tags added: `SEARCH_DROPDOWN_LATITUDE_TAG`, `SEARCH_DROPDOWN_LONGITUDE_TAG`.
2. Month (`MonthSelector`, unchanged).
3. "Search radius: …" and its slider (unchanged).
4. Recent searches, in its own fold (unchanged), after a divider.
5. After a divider, one row: **Set on map** (outlined, left; `SEARCH_DROPDOWN_SET_ON_MAP_TAG`) and **Search** (filled, magnifier
   icon, right; `SEARCH_DROPDOWN_SEARCH_TAG`), each half the width, where "Search this location" was. Set on map calls the same
   `onSetOnMap`; Search calls the same `onSearchManualCoordinates` "Search this location" called, which the caller wraps to close the
   dropdown first, as before.

Removed: "Use current location", "Search this location", the "Advanced search" and "Enter coordinates manually" folds, the scroll to
the end on open and the keep-in-view after it (`scrollToCoordinatesPending`, `coordinatesKeptInView`), the
`expandManualCoordinatesRequested`/`onManualCoordinatesExpandConsumed` parameters, and their arming in
`AvailabilityCompactScaffold.kt` (it was armed on each opening). `CollapsibleSection` lost its `expandRequested`/
`onExpandRequestConsumed` pair, whose only caller was those two folds (checked by grep across `app/src`); its log-package caller
never passed it. Unused imports went with them.

Kept: the scroll itself (`verticalScroll`, the panel can still be taller than its room) and its "a scroll lowers the keyboard"
effect; the bar's tap opening the dropdown; the closing panel taking no touch; Back; the 80% fill (the `Box` and its background are
untouched).

## Tests

New, `app/src/test/.../ui/availability/`:

- `AvailabilityScreenLayoutTest` (runs on three configurations: small dense phone, the same at 2x font, large phone):
  - `the search dropdown runs coordinates, Month, Search radius, Recent searches, then Set on map left of Search`: by laid-out
    bounds; Latitude and Longitude share a row; each group's top below the one before; Set on map and Search share a row with Set on
    map's right edge at or left of Search's left edge; "Set on map" once; "Use current location" and "Search this location" absent.
  - `real touches across the Search button run the coordinate search and close the dropdown`: five real touches at screen
    coordinates (centre and four points towards the corners), each on a freshly opened dropdown; each must call
    `onSearchManualCoordinates` once more and close the dropdown; `onUseCurrentLocation` never called.
  - `real touches across Set on map close the dropdown and open the centre-pin picker`: five sampled real touches; each closes the
    dropdown and brings up OK and Cancel; no search runs; Cancel between samples.
- `SearchDropdownSearchesTheFieldsTest` (real screen, field state held as the ViewModel holds it): Search with the fields left as
  they are records their values; typing over them and touching Search records the typed values. Its doc comment says plainly that
  the "left as they are" case starts from fields holding coordinates and is **not** evidence that Search reaches the current position.

Every changed assertion, with its reason:

| File | Was | Now | Why |
|---|---|---|---|
| `AvailabilitySearchSummaryCopyTest` helper (portrait and short-landscape tests) | "Use current location" and "Set on map" displayed (portrait) or existing (landscape, scrolled out); "Collapse Advanced search" and "Collapse Enter coordinates manually" exist; Latitude, Longitude, "Search this location" displayed | Latitude and Longitude displayed; Latitude's top exactly the panel's 16 dp padding below the panel's top (±0.5 dp), so not scrolled; Set on map and Search exist; the four removed labels absent | The labels and folds are removed; the landscape case used to assert the scroll the dispatch removes, and now asserts there is none |
| same, `in portrait the user's own collapse of the manual coordinates stands…` | the user's collapse of "Enter coordinates manually" stands | **deleted** | The fold it collapsed no longer exists; there is nothing left for it to test. Reported here as a removed test, not a silenced one |
| `SearchDropdownKeyboardTest` T3a | after a 360 dp shrink, Latitude, Longitude and "Search this location" displayed | Search and Set on map displayed at 800 dp; after the shrink, Latitude and Longitude displayed | "Search this location" is gone and the bottom row is no longer kept in view; the claim kept is the fields staying in view |
| same, T3b | after the user's drag, a later viewport change leaves the scroll alone ("Set on map" displayed, "Search this location" not) | Latitude 16 dp below the panel's top at 800, 360, 420 and 360 dp | The programmatic scroll it guarded is removed; the claim now is that nothing but the user scrolls the dropdown |
| `SearchDropdownClosingTapThroughTest` | touches "Use current location" (`onUseCurrentLocation` count) | touches Search (`onSearchManualCoordinates` count); added: the touch point is inside the closing panel when the touch lands | The button is gone. Search is now at the panel's bottom rather than its top, so the added assertion stops the mid-close test passing on a point the shrinking panel had already left |
| `AvailabilityScreenLayoutTest` Test 5 | "Collapse Advanced search", "Collapse Enter coordinates manually" exist; "Search this location" displayed | Latitude and Longitude displayed; the two fold labels absent | Folds and button removed |
| same, radius/month/location-row test | "Set on map", "Use current location", radius, Month reachable; "Advanced search" displayed and expanded; each button once | replaced by the order test above | It asserted the old structure; the order test asserts the new one |
| same, `the drawer's Use current location button calls onUseCurrentLocation` | the button calls the callback | replaced by the Search real-touch test | The button is removed |
| `searchAReferenceRegion` in `AvailabilityScreenBackNavigationTest`, `…TripPlanningFlowTest`, `…ConditionsMonthTest`, `…WaypointFlowTest`, `…MapIconStackTest` | clicks the "Search this location" text | clicks `SEARCH_DROPDOWN_SEARCH_TAG` | Navigation only; no assertion changed. These still drive the real ViewModel's coordinate search through the new button |
| `AvailabilityScreenMapIconStackTest`, the `@Ignore`d Set on map real-touch test | tapped "Advanced search" before touching Set on map | that tap removed, with a comment | The fold is gone. The `@Ignore` itself is untouched |

## Not verified, and what the go should run

- **Nothing compiled.** Kotlin errors are possible; the revert runner must read the compile log before any result.
- **The suite.** On the go: the classes above plus every class that opens the dropdown (`AvailabilityScreenAdaptiveLayoutTest`,
  `…BubbleAndDropdownBackTest`, `…DropdownCloseOutOfTouchModeTest`, `…FanBackOthersTest`, `…LandscapeB2Test`,
  `…OfflineCacheTest`, `LeavingTheJournalFixesTest`, `MapChromeColourTest`, `MapChromeColourPixelsTest`, `MapChromeOverMapTest`),
  then the full suite. `MapChromeColourPixelsTest` samples the bare map 200 dp above the screen's foot; the panel is expected to be
  no taller than before (two fold headers and one button fewer), but that is not measured.
- **Revert checks, planned, not run.** Each restores from a saved copy, reads the compile log first, and confirms the forward change
  is back afterwards: (1) swap the bottom row's two buttons: the order test must fail on "Set on map … is left of Search";
  (2) wire Search to `onUseCurrentLocation`: the Search real-touch test and `SearchDropdownSearchesTheFieldsTest` must fail on their
  counts; (3) restore the scroll to the end on open: `SearchDropdownKeyboardTest` T3b and the short-landscape summary test must fail
  on the 16 dp offset; (4) move the coordinates row below Month: the order test must fail on "Month … is below the coordinates".
- **The closing tap-through on Search** depends on the shrinking panel still covering the bottom row two frames into the close.
  The new containment assertion will say if it does not; if so the test needs a different target, which is a finding to report.
- **Device only:** with the keyboard up in portrait, whether the bottom row is reachable (the panel's cap follows the keyboard,
  `AvailabilityCompactScaffold.kt`, Robolectric shows no keyboard); how the reordered panel looks at the 80% fill.
- **The stop above is unresolved.** Until the owner chooses, the dropdown cannot search the current position.
