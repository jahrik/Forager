# Planned trips drawn on the compact Maps tab before any search: completion report (dispatch `2026-09-28-97`)

**Status: pre-registration only.** Nothing has been built or run when this part was pushed; it stays unchanged. Later sections
are added below it.

**Role and model.** Device-free build coder in worktree `/home/zynergy-labs/Zynergy/forager-wt/trips-on-map`, branch `trips-on-map`,
cut from `origin/journal-redesign`. The session is configured `claude-sonnet-5-5`; **which model served the turns I cannot read**, so
it is unverified. I write no `RECORD.md`, index, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.

**The dispatch** (`prompts/preserved/2026-09-28-97.md` on `origin/journal-redesign`, read in full) governs. Its build, verbatim:
"On the compact Maps tab, hand the map `uiState.plannedTrips` whether or not a search has run. Correct the comment at `:571-572` so it
no longer says trips are only real once a search has run. Nothing else changes: not the sightings gate, not the wide tree (which has no
map before a search; that is J6's), and not the entry map." The owner's ruling: "Option A for the fix" (planner: "Always draw saved trips,
like waypoints and finds, whether or not a search has run.").

## Premises checked at my base (`d79f15c`)

1. **Base.** `origin/journal-redesign` at `d79f15c`; `341566a` is an ancestor (verified with `merge-base --is-ancestor`), so the layout fixes are in.
2. **The gate has moved; the dispatch's line numbers are stale (a finding).** `AvailabilityCompactMapUi.kt` changed by 112 insertions and 9
   deletions since `a0a54f9`. At my base: `val hasSearched = uiState.region != null` is at **:586**, the comment above it at **:583-584**
   (not :571-572), `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()` at **:654** (not :641). The text is the same.
   `hasSearched` is **also** used at :591 for the sightings gate (`!hasSearched -> emptyList()`), so the local stays.
3. **The reproduction.** I ran it myself as the `-94` resumed coder on the S22: `docs/audits/2026-09-28-planned-trips-device-check-run-record.md`
   on branch `device-trips` (`50c9ae5`; **not yet merged to `journal-redesign`**, the planner merges it). With trips saved and `region` null, 0 trip
   pixels in every capture; after a search both flags draw. Same cause as the dispatch names.
4. **Other gates on planned trips.** `git grep hasSearched` over `app/src/main`: `AvailabilityUiState.kt:310` (the property),
   `AvailabilityResultsUi.kt:188,276`, `AvailabilityWideLayoutUi.kt:240` (the wide tree's own "no map before a search" message),
   `AvailabilityTripsWaypointsUi.kt:62` (the trip-windows text), and the two compact-map lines. None other gates trips on the compact map (read).
5. **Why the bubble test cannot fail (read).** `AvailabilityScreenMapBubblesTest.kt:283` builds `BubbleMapSlot(glyphsAt(60.dp, 380.dp))`, whose glyph list
   includes a `PLANNED_TRIPS` glyph `trip-1` (:217) drawn unconditionally by the stub (:141-153); the test at :395 taps it and reads the bubble, which comes from
   `bubbleSources.plannedTrips` (ungated per the run record). No search is run in that test (the fixture VM has none, `MapLayersUiFixtures.kt:142`), so at base
   the map slot is handed `emptyList()` and the test still passes.

## Plan and predictions, written before any edit

**Tests (test files and the test fixture only), through the real `AvailabilityScreen` on the compact tree:**
- `BubbleMapSlot` (test stub, `AvailabilityScreenMapBubblesTest.kt`): draw a `PLANNED_TRIPS` glyph only when `content.plannedTrips` holds its id, so the stub reads what the
  screen passes. Only one test taps a trip glyph (grep for `trip-1`/`PLANNED_TRIPS` in `src/test`), so no other test's touches move.
- New file `AvailabilityScreenPlannedTripsBeforeSearchTest.kt`, two classes, portrait (`w384dp-h823dp-xxhdpi`) and `w823dp-h384dp-land-xxhdpi`, each with:
  (a) **trips before a search:** VM with `BUBBLE_TRIP` saved, no search; the map slot's `content.plannedTrips == listOf(BUBBLE_TRIP)` and the stub draws `stub-glyph-trip-1`;
  (b) **sightings still wait:** the real screen fed a `uiState` with a sighting and `region == null` hands the map no sightings; with `region` set (control) it hands the sighting.
  That needs an optional `uiStateTransform` on `MapLayersTestScreen` (test fixture, `MapLayersUiFixtures.kt`), default identity.
- The existing test `a planned trip's bubble shows its row's lines and its Directions` keeps its body; it now fails when the screen hands no trips.

**Predictions at base (before the build change):**
| Test | Predicted at base | Failure message I expect |
|---|---|---|
| (a) portrait, (a) landscape | **fail** | `plannedTrips` expected `[PlannedTrip(id=trip-1, …)]` but was `[]`; and/or no node with tag `stub-glyph-trip-1` |
| the fixed bubble test | **fail** | "No node found" for `stub-glyph-trip-1` (from `touchCentreOf`) |
| (b) portrait, (b) landscape | **pass at base** (a guard: it passes before and after the change by design, so it is not evidence for the change; its bite is shown by a second revert of the sightings gate, `!hasSearched -> emptyList()`) | after that revert: the sighting appears in `content.sightings` with `region == null` |
| all other tests | pass | |

**After the one-line change (`:654`) and the comment:** all of the above pass. **Revert check (dispatch):** put the gate back; (a) x2 and the bubble test fail naming the empty list /
missing glyph; (b) still passes. **Predicted suite growth: +4** (2 classes x 2 tests; the planner predicted 3 to 6). The revert runner refuses results if the build log has a compile error, and restores
from a copy saved before editing, never from git (CLAUDE.md, Testing).

**What is device-only and not run here:** trips drawn on the S22 before any search, by day and at night, with the switch on and off. Robolectric reports zero window insets and runs a stub map,
so no map pixel is drawn in this run.
