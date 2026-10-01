# Planned trips on the Maps tab: premise pulse (read-only, at `a0a54f9`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `a0a54f9`, through `git show`. The ref moved to `554449b` during the read, but only records and store copies changed. No device reads, no builds, no tests.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with every citation kept. `P` = `app/src/main/java/com/zynergylabs/forager/app/`. Labels: [R] read in code, [O] observed by running git, [I] inferred.

**Why:** the owner, verbatim: "I noticed there are no day/night icons for planned trips, even though they do record in the planner. The layer is available in the map layers panel though." Then "They do not appear at all" (zoomed out, on the S26).

## The cause the code predicts

- **The compact Maps tab withholds planned trips until a search has set a region** [R]:
  - `P/ui/availability/AvailabilityCompactMapUi.kt:573`: `val hasSearched = uiState.region != null`;
  - `:641`: `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()`.
  - Its comment (`:571-572`) reads: "Sightings/planned trips are only real once a search has actually run".
- **`region` is null until a search runs.** It defaults to null (`AvailabilityUiState.kt:30`) and is set only by manual coordinates, "use current location" or a recent search (`AvailabilityViewModel.kt:406-407, 416-419, 792-795`). Nothing resets it [R]. So it is null after every cold start [I].
- **The owner's screenshot fits.** The Trip Planner line "Choose a region in search options to see rain-driven trip windows." renders only when `region == null` (`AvailabilityTripsWaypointsUi.kt:62-69`), the same check as the gate [R].
- **Only trips and sightings are gated.** On the same tab, waypoints (`:643`) and finds, photos, tracks and offline regions (`:651-654`) are not; sightings are (`:578`) [R].
- **The code's comments disagree with the gate.** `AvailabilityUiState.kt:88-93` and `AvailabilityTripsWaypointsUi.kt:54-55` call trips "independent of any region search" [R].

## The rest of the path

- **No other filter** by date, month, taxon or radius [R]:
  - DAO `PlannedTripDao.kt:11` (no WHERE clause);
  - repository `RoomPlannedTripRepository.kt:15-16`;
  - use case `GetPlannedTripsUseCase.kt:20-25` (sorts, today first; keeps past trips).
- **On save,** `SavePlannedTripUseCase.kt:31` refuses dates before today [R].
- **ViewModel:** loads once in `init` (`:162`) and on save or delete (`:828`, `:842`); `:810-822` [R].
- **To the map:** `MapSlot.kt:256, 468`, then `SightingsMap.kt:623-632, 1031, 1552-1561`, one point per trip [R].
- **Other blanks:** a loading spinner (`:532-538`), a sightings error (`:540-544`), no style before the Night Maps preference loads (`SightingsMap.kt:555-565`), a style failure (`:549-554`) [R].

## The layer and the switch

- **The layer:** `planned-trips-layer` with source `planned-trips` (`MapLayers.kt:140, 162`), registered at `:390`: MARKERS group, symbol, tappable, toggleable [R].
- **The glyph:** `MarkerIcon.PLANNED_TRIP`, "planned-trip-flag", a 20×28 dp flag (`MarkerGlyphs.kt:41-42, 117-120, 231, 246`). Day `#9553A4` on white, night `#FA01DD` on black (`MapPalette.kt:93, 105, 116, 137`).
- **Day and night:** the same code path in both; only the palette differs. There is no sun or moon icon anywhere [R].
- **The switch** is wired: `MapLayersSheet.kt:137, 272-278`, then `AvailabilityScreen.kt:952`, `MainActivity.kt:456` and `AvailabilityViewModel.kt:501-506`, stored in DataStore, and applied at `SightingsMap.kt:673-676, 968` [R]. It is on by default (`MapLayerState.kt:14`).
- **The switch shows the stored choice, not whether data reached the layer** [R].

## Layouts

- **Wide:** there is no map before a search (`AvailabilityWideLayoutUi.kt:239-243`). After one, trips pass ungated (`:291`) [R].
- **The entry map** never passes trips, and its sheet omits the switch (`CartographyEntryReportScreen.kt:464-478`; `MapLayersSheet.kt:149-152`) [R].
- **Bubbles** read the ungated `uiState.plannedTrips` (`AvailabilityScreen.kt:1253`) [R].

## Tests

- **No test checks that a trip reaches the compact map** [O].
- `AvailabilityScreenMapBubblesTest.kt:394-406` draws a trip glyph from a fixed stub list with no search run. The real screen hands the map an empty list there, so that test would pass with or without the gate [I]. This is CLAUDE.md's "a check that passes because it never saw the data that could fail it".

## History [O]

- `feef868` (2026-08-17): the map first takes `plannedTrips`, and the compact tab shows no map before a search.
- `7692527` (2026-08-18, "auto-locate on open"): the compact map appears before a search, and this commit **adds the gate**. Merged in PR #17 and PR #18.
- The gate is unchanged since, and present at `a0a54f9`.
- The "Planned trips" switch arrived in `93da312` (L0b, 2026-09-27). So the owner's build, which shows the switch, has the gate [I].

## Other findings

- **After a search the trips may still be off screen.** The camera opens at zoom 12 for the default 8 km radius (`SightingsMap.kt:1809-1814`). The owner's trips are about 4.6, 10.3 and 12.5 km from their position (MGRS arithmetic) [I].

## Could not determine

- The owner's build and stored switch state, and whether both screenshots came from one session with no search between them.
- The S22's trip count (not read).
- The on-screen span at zoom 12.
- Whether any cited test passes (none were run).
