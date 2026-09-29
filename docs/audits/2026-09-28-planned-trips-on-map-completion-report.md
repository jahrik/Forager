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

## What landed

| Commit | What | Suite at that commit |
|---|---|---|
| `cede861` | pre-registration (above) | not run (docs only) |
| `c985e3f` | tests first: `AvailabilityScreenPlannedTripsBeforeSearchTest.kt` (two classes, four tests), `BubbleMapSlot` reads `content.plannedTrips`, `MapLayersTestScreen(uiStateTransform)` | **3 fail at base**, as predicted (below). Pushed to `trips-on-map-wip` first, since it was red |
| `b427a66` | the build: `AvailabilityCompactMapUi.kt` hands the map `uiState.plannedTrips` (one line), and the comment above `hasSearched` corrected (`:583-587`) | **green** (below); pushed to `journal-redesign` |

`journal-redesign` therefore contains `c985e3f` (red on its own) followed by `b427a66` (green): the branch tip passes, the middle commit does not. I kept the commits separate so the tests-first evidence is in history,
and pushed the red one to `trips-on-map-wip` alone before making the change. (Not rebased or squashed, per CLAUDE.md.)

The code change, in full: `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList(),` became `plannedTrips = uiState.plannedTrips,` (`AvailabilityCompactMapUi.kt`, now at :657), and the comment that said "Sightings/planned trips are
only real once a search has actually run" now says sightings are, and that saved trips are the user's own records handed over whether or not a search has run. `hasSearched` stays: the sightings gate (`!hasSearched -> emptyList()`,
:594 at my base's line numbering plus the comment growth) still uses it. **Nothing else in `main/` changed** (`git diff d79f15c HEAD --stat`: one `main` file). The wide tree, the entry map and sightings are untouched.

## Tests first, seen failing at base for the stated reason

Run at base with the gate still in place (`/tmp/t97-base.log`, `testDebugUnitTest` on the three classes; **0 compile errors** in the log; every result file newer than the run's start; **18 tests, 3 failed**):

| Test | At base | Message (JUnit XML) |
|---|---|---|
| `…PlannedTripsBeforeSearchPortraitTest` "saved trips reach the map and their glyph is drawn with no search run" | **failed** | "the map slot is handed the saved trip expected:<[PlannedTrip(id=trip-1, name=Saddle loop, …)]> but was:<[]>" |
| `…PlannedTripsBeforeSearchLandscapeTest` (same) | **failed** | same message |
| `AvailabilityScreenMapBubblesTest` "a planned trip's bubble shows its row's lines and its Directions" | **failed** | "Failed to inject touch input. Reason: Expected exactly '1' node but could not find any node that satisfies: (TestTag = 'stub-glyph-trip-1')" |
| the two "sightings still wait for a search…" tests | **passed** (predicted: a guard) | n/a |

The failures are for the stated reason, and not for a loading one: the tests' two precondition asserts (`region` null; `uiState.plannedTrips == [BUBBLE_TRIP]`) passed before the failing line, so the trip was loaded and only the hand-over to the map was empty.
The bubble test's body is unchanged; it fails because the stub now draws the glyph only for a trip the screen handed it (before, the glyph was a fixed list entry, which is why it passed at base).

## Revert checks

Runner rules applied: the forward file was saved (`/tmp/t97-forward-copy.kt`, sha256 prefix `ebf9f946ef7bad23`) **before** each edit and restored **from that copy, never from git**; the build log was checked for compile errors before the results were read
(**0** both times, and `compileDebugKotlin` ran); every result file was checked newer than the run's start; after each restore the file's hash equals the saved copy's and `git status` shows a clean tree, so the forward change is present.

1. **The dispatch's revert: the gate put back** (`plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()`). 18 tests, **3 failed, exactly the three above, with the same messages** ("…but was:<[]>" x2; missing `stub-glyph-trip-1`). Each failure is one this edit produces
   (an empty list handed to the map), none belongs to another edit.
2. **Not asked for, added because the guard passes at base:** the sightings gate removed (`!hasSearched -> emptyList()` deleted). 4 tests, **2 failed**: both "sightings still wait for a search…" ("with no region the map is handed no sightings expected:<[]> but was:<[Sighting(observationId=501, …)]>"),
   and both trip tests passed. So the guard is connected to the sightings gate, and the trip tests to the trips gate, and neither tests the other's.

## The full suite

From a cleared results directory (`rm -rf app/build/test-results`), on the forward code at `b427a66`'s tree, `./gradlew :app:testDebugUnitTest`, `LC_ALL=C.UTF-8`: **BUILD SUCCESSFUL in 3m 13s**. Counts from the JUnit XML: **316 result files, none older than the run's start; 2567 tests, 0 failures, 0 errors, 24 skipped.**
- **Growth:** +4 tests (two classes x two). The count before my change I did **not measure** (I did not run the suite at base); 2563 is inferred from 2567 minus the four I added, so the planner's "grows by 3 to 6" holds by construction, not by a base count.
- **The 24 skipped** are not mine and I did not investigate them (the repo has 51 `@Ignore` occurrences in `src/test`, by `git grep`; how they map to 24 skipped tests is not determined).

## Predictions, checked

1. **"One line and one comment change":** held for `main/` (one code line, and the comment). The tests needed more than the dispatch listed (below).
2. **"The suite grows by 3 to 6":** +4.
3. **My own table (pre-registration):** every row held (3 fail at base with the predicted messages; guards pass at base; after the change all pass; the reverts bite on their own edit).

## Decisions I made

1. **`MapLayersTestScreen` gained an optional `uiStateTransform`** (test fixture, default identity), because sightings held with no region cannot be reached through the real ViewModel (a search sets both), and a guard needs that state. The dispatch lists "the tests above" as in scope; the fixture is test-only.
2. **`BubbleMapSlot` now draws a `PLANNED_TRIPS` glyph only for a trip in `content.plannedTrips`** (the fix for the test that cannot fail). This changes a shared stub used by other test classes; I checked `git grep` for `trip-1` and `PLANNED_TRIPS` in `src/test` first (only `AvailabilityScreenMapBubblesTest.kt:398` taps a trip glyph),
   and the whole suite passed. The alternative (a new stub for that one test) would leave the shared stub able to draw a glyph the screen never handed it.
3. **A new test file with an abstract contract class and two subclasses** (one `@Config` each) so portrait and `w823dp-h384dp-land` run the same two tests.
4. **The added second revert (sightings gate)**, because the guard passes at base by design and would otherwise have no evidence of biting.
5. **Two commits before the change**, the red tests pushed to `trips-on-map-wip` and then, with the green change on top, to `journal-redesign`, as the dispatch says ("Push ... after each commit that leaves the suite passing, and put broken work on `trips-on-map-wip`").
6. **D58:** a grep over each diff and commit message for the three phrases (read from forager-forecast `origin/d55-artifact-contract`'s `DECISIONS.md` row D58 with `git show`); none found; not written here.

## Flags outside scope

1. **The dispatch's line numbers are stale** (`:573`/`:641`/`:571-572` at `a0a54f9`; `:586`/`:654`/`:583-584` at `d79f15c`), as its own "re-verify" note anticipated; the text was identical.
2. **The reproduction record is on `device-trips`, not `journal-redesign`** (`50c9ae5`, pending the planner's merge), so a reader at `journal-redesign` cannot yet open the file the dispatch names.
3. **Not fixed, out of scope, from that device run:** `AvailabilityViewModel: Couldn't read offline regions.` at every cold start (`AvailabilityViewModel.kt:922-923`), and 2466 `Mbgl-NativeMapView … after the MapView was destroyed` lines; a uiautomator dump cannot show the trip flags (its hash was identical drawn and undrawn).
4. **`AvailabilityTripsWaypointsUi.kt:62`** still shows the Trip Windows text only when `hasSearched` and the wide tree still shows no map before a search: both correct under the dispatch's scope, named so they are not read as missed.
5. **The serving model is unverified** (see the top).

## Device-only, listed, not run (no phone in this dispatch)

- Trips drawn on the S22 before any search, by day (`#9553A4`) and at night (`#FA01DD`), with the Planned trips switch on and off, from a cold start with two saved trips; and a tap on a flag opening its bubble before any search.
- Robolectric runs a **stub map**: no MapLibre pixel is drawn in any test here, so nothing in this report shows a flag on screen. It shows the screen hands the map the trips (the point the real device run named as the cause); the map's own drawing of them was already shown on the device once `region` was set.
