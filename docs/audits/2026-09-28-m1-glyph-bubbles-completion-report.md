# 2026-09-28: M1, tap a map glyph for a bubble (stopped before building)

**Status: stopped at verification, before any code or test.** The premises the build depends on hold, but the
build needs files the scope does not list, and three questions the rulings do not answer. Each is below with the
options I can see. Nothing under `app/` has changed.

Dispatch: `prompts/preserved/2026-09-28-29.md`, intent `2026-09-28-29` (written by the planner). Authority: the
plan's "M1 rulings (owner and planner, 2026-09-28)". Written by the M1 coder in worktree `forager-wt/m1`, local
branch `m1`, cut from `origin/journal-redesign` at `789c602`. Paths are under
`app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

## Base

- `origin/journal-redesign` was `789c602` after `git fetch`, the commit carrying the dispatch. `git diff --stat
  21d755a 789c602` touches only `RECORD.md`, the pulse, the plan and two store copies, so the app tree is the
  pulse's. **Confirmed.**
- While I read, the drawer-Back fix (intent `2026-09-28-28`) landed at `42d9d3d`. I merged it into `m1` with
  `git pull --no-rebase`. It adds a `backEnabled` flag to the Journal's screens, including
  `ui/log/CartographyEntryReportScreen.kt:238-239`, and comments in `AvailabilityScreen.kt` and
  `AvailabilityCompactScaffold.kt`. Line numbers below are at `789c602` unless marked.
- The kit is absent at this base (no `.claude/`, no checkers). I made no record entry, as the dispatch says.

## The pulse's claims, checked

| Claim (pulse) | At `789c602` |
|---|---|
| `onFeatureTap` is `(layerId, featureId)`, defaulted, `MapSlot.kt:193`, passed on `:440`, declared `SightingsMap.kt:242`; no callers | **Confirmed.** The only other mention is a comment at `ui/log/CartographyEntryReportScreen.kt:368`. |
| Precedence `TapPrecedence.kt:25-42`, point then 48 dp box | **Confirmed.** `resolveTap` is `tapWinner(point) ?: tapWinner(box())` (`:41-42`). |
| Colour fields and offline fill are `NONE` (`MapLayers.kt:214, :258`); outline, breadcrumb, kept tracks `LINE`; every marker, search centre included, `MARKER` | **Confirmed** (`MapLayers.kt:214, 258, 261-265`; the search centre is `marker(...)` at `:266`). |
| Fixed ids `"search-centre"`, `"breadcrumb"` (`SightingsMap.kt:1436, 1439`) | **Confirmed.** |
| Click listener `SightingsMap.kt:364-414`: sighting to `onSightingTap` only (`:392-393`); any other winner to `onFeatureTap`, then `onTap` always (`:398-409`) | **Confirmed.** |
| `onTap` per host: compact `AvailabilityCompactMapUi.kt:640-643`; wide `AvailabilityWideLayoutUi.kt:283`; entry map `CartographyEntryReportScreen.kt:403`; picker `{}` | **Confirmed** (picker: `ui/map/CentrePinLocationPicker.kt:155`). |
| Long-press: every caller passes `{}` | **Confirmed** at all four `mapSlot(` calls. |
| `ObservationBubble` `AvailabilityMapOverlaysUi.kt:357-481` on `AnchoredAtScreenPoint` `:273-325`, 315°, 280 dp, clamp `:315-320`, limitation `:253-257` | **Confirmed.** The tail is drawn from where the card lands, so a clamp moves the tip off the anchor. |
| Live re-projection only for sightings, `SightingsMap.kt:455-460` | **Confirmed.** |
| Finds `MushroomLogEntry.id`, photos `LogPhoto.id`, ended tracks only, regions `id.toString()` (`GetMapRecordsUseCase.kt:95, 100, 103-104, 107`) | **Confirmed.** |
| J5c sheet: waypoint `:186-213`, track `:216-250`, region `:253-266`, region target a `Long` `:70`, `internal` and list-driven `:141-153` | **Confirmed.** Its inputs: the three lists, the waypoint reference counts, the distance unit, now, the stale threshold, `getFullRecord`. |
| Entry map waypoints carry placeholder names (`CartographyEntryReportScreen.kt:371-380`) | **Confirmed.** The real names **can be resolved** without a new read: `entry.waypointDecisions` carries each kept waypoint's `waypointId` and `name` (`domain/model/CartographyEntry.kt:113-119`). |
| Compact map gets trips only after a search (`AvailabilityCompactMapUi.kt:619`) | **Confirmed.** |
| Cell model has every D55 property (`domain/ForecastCells.kt:60-71`); the feature carries only `chance` and `featureId = "lat,lng"` (`ui/map/ForecastCellLayer.kt:78-81`); `SightingsMap` keeps no cells (`:647-670`); lookup through `groupsByLayer`, the week and `ForecastCellStore.cells` (`ForecastCells.kt:227`) | **Confirmed.** |
| Find report: `onOpenEntry` (`MainActivity.kt:469`) plus `JournalTab`'s local `mode`; only the nav switches `compactTab` (`AvailabilityScreen.kt:754`) | **Confirmed.** `JournalTab`'s `mode` is `remember { REPORT }` (`ui/log/JournalTab.kt:270`), so it is `REPORT` whenever the Journal tab is newly composed. |
| `PhotoViewerDialog` is a dialog (`ui/log/PhotoViewerDialog.kt:106-110`) | **Confirmed.** |
| `launchDirections` (`AvailabilityPureFunctions.kt:162-176`) | **Confirmed** (`:162-174`, plus the trip overload at `:177`). |
| `SightingsMap` cannot run under Robolectric (`SightingsMapOverlayDataTest.kt:26-36`) | **Confirmed.** |
| `MapSlot` at nine parameters (`MapSlot.kt:353-399`); new callbacks on `MapRenderMode`, new data on `MapOverlayContent` | **Confirmed.** No tenth parameter is needed for any part of the build. |

**Every kind can be looked up from its `featureId` on the Maps tab**, from lists `AvailabilityScreen` already
holds: finds in `logUiState.entries`, photos in `logUiState.galleryPhotos`, waypoints and tracks in its
`waypoints` and `tracks` parameters (`MainActivity.kt:553, 569`), trips in `uiState.plannedTrips`, regions in
`uiState.offlineRegions` (`AvailabilityUiState.kt:167`). **On the entry map, every kind can be looked up for its
bubble** from the entry's own snapshots (`entry.findDecisions`, `trackDecisions`, `waypointDecisions`,
`offlineRegionDecisions`) and `galleryPhotos`; but the snapshots do not carry a track's date, a region's size or
staleness, or a find's cover photo, and they are not what the J5c sheet takes (see Q1).

## Findings (not stops on their own)

**F1. The feature tap carries no position.** `onFeatureTap` is `(layerId, featureId)` (`MapSlot.kt:193`), and the
listener calls it without the screen point it already has (`SightingsMap.kt:374, 401`). A bubble needs a screen
anchor and the bearing, as `onSightingTap` gets them (`:393`), and the cell lookup needs the tapped geographic
point to choose the block. What I would build: widen that `MapRenderMode` field's type to one value carrying the
layer, the id, the screen point, the bearing and the tapped point. It is still no tenth `MapSlot` parameter. The
stub slots in the tests would fire it with those values, not as a bare `(layerId, id)`.

**F2. Re-anchoring can be built headless up to the native boundary.** `SightingsMap` holds every point list it
draws (trips, waypoints, finds, photos). So a focused-feature value on `MapOverlayContent`, generalising
`focusedObservationId`, can be re-projected on camera idle by a pure lookup, with the same dismissal rule
`focusedObservationId` has (`MapSlot.kt:252-266`).

**F3. The compact find route fits in scope, and the Maps search bar is safe.** "Open in Journal" can set
`journalScreenState.topTab = RECORDS` and `recordsFilter = FINDS` (both hoisted in `AvailabilityScreen.kt:760`),
call `onOpenLogEntry(id)`, then set `compactTab = JOURNAL`. `JournalTab` is then composed afresh with `mode =
REPORT`, so the find opens in its report. The gate at `AvailabilityCompactScaffold.kt:401` hides the bar only on
the non-Maps tabs (`:711`). The Maps tab's own slot is gated on "an entry is open **and** the Journal tab shows"
(`:898` at `42d9d3d`), so returning to Maps with the find still open keeps the Maps search bar. One side effect:
this overwrites the user's saved Records filter with Finds.

**F4. Back.** The drawer fix turns the Journal's handlers off while the drawer is open (`backEnabled`). For the
bubble I would add a handler on each map host with `enabled = bubble != null && !isDrawerOpen` on the compact tab
(which already takes `isDrawerOpen`), and `backEnabled && bubble != null` on the entry map. On the compact tab it
is registered after `AvailabilityScreen`'s own handlers, so it wins over the exit and fullscreen handlers while a
bubble shows, and the drawer's handler wins when the drawer is open.

## Needs a decision

**Q1. The entry map (B4 and B5) cannot be reached through files in scope.** `CartographyEntryReportScreen` is
called only from `ui/log/CartographyScreen.kt:334`, which is called from `ui/log/JournalTab.kt` (compact) and
`ui/log/LogPanel.kt` (wide). None of the three is in scope. `CartographyScreen` has `tracks` (for its cards) but
not the waypoints, the offline regions, the reference counts, the stale threshold, `getFullRecord` or the finds,
so the J5c sheet's inputs and a find's route cannot be threaded to the entry map without them.
- (a) Widen the scope to `CartographyScreen.kt`, `JournalTab.kt` and `LogPanel.kt` for parameter threading only,
  as the planner did for L0b's F4.
- (b) Entry-map bubbles built from the entry's own snapshots (name, MGRS and Directions for a waypoint; name,
  distance and duration for a track; name and radius for a region; the find's date and identification; the photo
  viewer from `galleryPhotos`), with no details action and no "Open in Journal" there. This falls short of B3's
  content (a track's date, a region's size and staleness) and of B4.
- (c) Something else.
- Lean: (a).

**Q2. Extracting the bubble shell touches `ui/availability/AvailabilityMapOverlaysUi.kt`, which is not in scope.**
`ObservationBubble` and `AnchoredAtScreenPoint` live there (`:273-325, :357-481`), and the tail fix is in
`AnchoredAtScreenPoint`.
- (a) Widen the scope to that file, so the two composables move to the new bubble file and are removed there.
- (b) Build the shell and the tail fix in the new bubble file under `ui/map/`, move both hosts onto it, and leave
  the two old composables in place with no caller, flagged, as L0b left `MapModePicker`.
- Lean: (a). (b) leaves a second copy of the sighting bubble's content in the tree.

**Q3. "Open in Journal" on the wide layout.** The rulings describe it only as switching `compactTab`, which a
medium or expanded window does not show. There, the Journal is the drawer's `LogPanel` (`AvailabilityScreen.kt`,
`DrawerPanel.Log`), whose top tab is local state (`ui/log/LogPanel.kt:227`). `PendingJournalDestination` has only
`EDIT_NEW_FIND` (`ui/log/JournalTab.kt:701-704`).
- (a) Open the drawer on `DrawerPanel.Log` with the find open in its report. This needs a new
  `PendingJournalDestination` case consumed in `LogPanel.kt` (and `JournalTab.kt`), both out of scope.
- (b) No "Open in Journal" on the wide layout; the find bubble there has no action.
- (c) Something else.

**Q4. "Open in Journal" on the entry map.** The entry map is already inside the Journal (an open day entry's
report). Options:
- (a) Leave the day entry and open the find's report in Records, Finds, on the same tab. This needs Q1 (a).
- (b) No action on the entry map's find bubble.
- (c) Something else, for example opening the find over the entry.

**Q5. Does a colour-field cell found only by the box stage win?** The ruling is that a cell "loses to any marker
or line within the box", so the point-then-box rule stands. It does not say whether a cell that lies only in the
box, not under the point, can win. Near the edge of the forecast area that is a tap on an empty "no forecast
here" cell reading out a neighbour.
- (a) Cells only from the point stage: a cell wins when it is under the finger and no marker or line is at the
  point or in the box.
- (b) Cells from either stage, point first.
- Lean: (a).

## What landed

This report only. No production or test file.

## Tests

None written and none run, so no tests-first run and no revert check.

## Suites

Not run. Nothing is built, and the machine had 2 GB available (`free -g`: 11 GB total, 8 GB used) beside other
sessions' daemons, which the dispatch says not to stop.

## Device-only

Nothing built. The dispatch's list stands for the build: the native hit test returning `featureId` for each
kind; the offline circle's interior falling through (from L0a); the tail on the tapped glyph at the screen edges;
bubbles against real insets; re-anchoring on pan.

## Decisions I made

- **I followed the dispatch over my agent definition on the record.** My definition asks for a sweep, an intent,
  a terminal and the kit's checkers. The kit is absent at this base, and the dispatch says the planner writes the
  record. I touched nothing in `RECORD.md` or `prompts/`.
- **I did not validate the dispatch's sections against a kit config**, since none exists at the base.
- **I stopped the whole stage** rather than building B1 to B3 and the Maps-tab hosts, which have no open question.
  Q1 to Q4 decide what B4 and B5 look like on two of the three hosts, and Q2 decides which file the shell lives in,
  which every host touches.
- **I wrote this stop as the dispatch's named completion report and pushed it**, as the L0b coder did at its stop,
  so the findings are not only in a hand-back.
- **I skipped a baseline suite**, for the reasons under Suites.
- **F1, F2 and F4 are my mechanisms**, stated as what I would build, not built.

## Flags outside scope

- The pulse's premise list did not name `AvailabilityMapOverlaysUi.kt`, `CartographyScreen.kt`, `JournalTab.kt` or
  `LogPanel.kt`, and the dispatch's scope follows it. Q1 to Q3 are the result.

## D58

`git grep -i` for the three phrases named in `prompts/preserved/2026-09-28-03.md`, over this report and this
commit's message: see the commit.
