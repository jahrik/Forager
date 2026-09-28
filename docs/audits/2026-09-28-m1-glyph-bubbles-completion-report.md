# 2026-09-28: M1, tap a map glyph for a bubble (built after continuation `2026-09-28-30`)

**Status (continuation `2026-09-28-30`): see the last section, "Resumed".** B1 to B5 are built and pushed, with
tests first, 30 revert checks (all compiled, 29 confirmed, 1 message-format mismatch on the predicted failures) and
the full suite at `a5ebc27`: 274 classes / 2245 tests / 0 failures / 0 errors / 24 skipped. The sections before
"Resumed" are the stop report as it stood at `4b6b7e5`, left as written.

**Status at `4b6b7e5` (the stop): stopped at verification, before any code or test.** The premises the build depends on hold, but the
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

## Resumed (continuation `2026-09-28-30`)

Written by the same coder, in the same worktree (`forager-wt/m1`, local branch `m1`), pushing to
`journal-redesign`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

**The continuation** (`prompts/preserved/2026-09-28-30.md`, committed at `f41f3ec`), verbatim:

> Planner message, part of dispatch 2026-09-28-29 (continuation 2026-09-28-30). Quote it verbatim in your report. It answers Q1 to Q5 and F1 to F4 of your stop report (`4b6b7e5`). Build B1 to B5 as written, with the changes below.
>
> **The owner's rulings (verbatim answers):**
> - **Q3, wide layout:** "Open drawer to the find (Recommended)". On the wide layout, "Open in Journal" opens the drawer's `LogPanel` with that find in its report. This adds the destination case you described, in `LogPanel.kt` and `JournalTab.kt`.
> - **Q4, entry map:** "Open find, Back returns (Recommended)". On an entry's map, the find bubble's button is "Open find". It opens that find's report, and Back returns to the day entry exactly as it was: same entry, same view, same scroll.
>
> **The planner's rulings:**
> - **Q1:** (a). The scope widens to `ui/log/CartographyScreen.kt`, `ui/log/JournalTab.kt` and `ui/log/LogPanel.kt`, for parameter threading, plus the two find-open destinations above. No other behaviour changes in them.
> - **Q2:** (a). The scope widens to `ui/availability/AvailabilityMapOverlaysUi.kt`, to extract the shell and fix the tail there.
> - **Q5:** (a). A cell counts only when it is under the finger, at the point stage. It never wins through the box. Test it at the edge of a cell area.
> - **F1:** accepted. `MapRenderMode.onFeatureTap` carries the screen point, the bearing and the map position. Update the stub slots.
> - **F2 and F4:** accepted.
> - **F3:** the compact route stands, **but it must not overwrite the user's saved Records filter.** This follows CLAUDE.md "UX defaults": a user-set filter survives. Open the find without changing the saved chip, or put the chip back when the user leaves the find. If neither is possible in scope, stop and report.
>
> Everything else in the dispatch stands: tests first, coordinate-touch UI tests, revert checks with their abort rule, the full suite, the completion report, and D58. Update the report's stop section into a "Resumed" section. Do not rewrite it.

The coordinator's relay summarised it; the committed file governs, and this report follows the file.

### Base

- I ran `git pull --no-rebase` to `f41f3ec` (docs and record only since `4b6b7e5`) and later merged the planner's
  pushes with `--no-rebase` three times (`4cec22f`, and the merges inside `a5ebc27`'s history). Nothing the
  planner pushed touched `app/`.
- The kit is still absent, so I made no record entry.

### Commits (pushed to `journal-redesign`)

| Commit | What |
|---|---|
| `b9ad4c6` | Tests first: stubs in main, 4 classes, 56 tests, 28 failures |
| `2a54029` | B1 and B3, the pure half: `resolveTap`, the tap groups, `mapTapOutcome`, `MapBubbles.kt`, `nextFindOverView` |
| `ea3da69` | B1 to B5, the UI half: `SightingsMap`, the shell, the three hosts, the find routes |
| `7e645ef` | The UI tests and the fixture additions |
| this commit | This section |

### Tests first (`b9ad4c6`)

- **Run:** 4 classes (`TapPrecedenceTest`, `MapLayerRegistryTest`, `MapBubblesTest`, `FindOverViewTest`), 56
  tests, **28 failures**, 0 `e: ` lines (`app/build/m1/tf2.log`). Every new or re-pinned test failed, each at an
  assertion naming what was missing: the old tap groups, `Plain` from the `mapTapOutcome` stub, `null` from the
  content stubs, the placement stub's tip at `(0, 0)`.
- **Deviation: absence tests restructured before the commit.** The first run (`tf1`, 61 tests, 24 failures) had 9
  new tests passing at base. Every one of them asserted something absent:
  - "a cell only in the box never wins" held because cells were untappable at base;
  - "a record gone from its list has no bubble" held because the stubs returned `null`.

  Before committing I folded each into a test with a positive half that fails first. For example, each cell
  precedence test now first shows the cell winning alone. I made no weakening, and the test count fell from 61 to 56 by the merges. This is the
  "tests-first test passing at base" abort read the way the planner accepted for L0b: the absence half cannot fail
  before its feature exists. Revert checks R01, R06 and R12 show those halves bite.
- **Test corrections after the build**, both in `MapBubblesTest`'s unclamped placement test:
  - the test compared the placed bubble, which includes the 24 px tail margin, rather than the card;
  - it claimed the whole card sits left of the anchor, which a wide card at 135 degrees does not (the tail leaves
    its bottom edge). It now claims the card's centre is.

  Both were my test being wrong about the geometry the sighting bubble always had.

**Existing tests changed, by name** (the rulings change them):
- `TapPrecedenceTest`:
  - "every marker and line is queried, never a casing or the offline fill" became "every record marker, line and
    colour field is queried, never a casing, the offline fill, the search centre or the recording trail". The
    search centre and breadcrumb left the list, and both colour fields joined it.
  - "within the lines the layer drawn on top wins" now compares kept tracks with the offline outline (the
    breadcrumb takes no taps).
  - "hits only on untappable or unknown layers win nothing" gained the search centre and breadcrumb.
  - "a colour field wins only when no marker or line is hit" uses kept tracks instead of the breadcrumb.
  - "with nothing tappable under the point the box decides" uses kept tracks instead of the breadcrumb.
  - Flag: the second, fourth and fifth pass both before and after the build. They are re-pins, not new
    coverage, and the new tests carry the claims.
- `MapLayerRegistryTest` "markers and lines are tappable, casings, the offline fill and the colour fields are
  not" became "record markers, lines and colour fields are tappable, casings, the offline fill, the search centre
  and the recording trail are not": colour fields `COLOUR_FIELD`, breadcrumb and search centre `NONE`.
- **Prediction 3 (sighting-bubble tests unchanged apart from the tail anchor):** every sighting-bubble test in
  `AvailabilityScreenMapIconStackTest` (tap, iNaturalist, close, pan, dismiss, bearing 0 and 90) is `@Ignore`d at
  base for a harness issue (`docs/audits/2026-08-31-*`). So they were compiled, not run, and are **no evidence**
  either way. I changed none of them. Their stub slots still compile against the unchanged `onSightingTap`.

### What landed, per item

**B1, tap routing.**
- `resolveTap` (`ui/map/layers/TapPrecedence.kt`) holds cells back. It resolves the point's markers and lines,
  then the box's markers and lines, then the cells under the finger. A cell never wins through the box (Q5 (a)).
- `mapTapOutcome` decides what fires:
  - a sighting goes to `onSightingTap`, as before;
  - a feature with an id goes to `onFeatureTap` **only**;
  - a feature with no id is logged and taken as a plain tap (my decision, below);
  - nothing tappable is a plain `onTap`.
- `SightingsMap`'s click listener dispatches on that outcome (`ui/map/SightingsMap.kt`, the listener).
- Registry (`ui/map/layers/MapLayers.kt`): the colour fields are `COLOUR_FIELD`, the breadcrumb `NONE`, the search
  centre `NONE`, each with its ruling in a comment.
- **F1:** `MapRenderMode.onFeatureTap` is now `(MapFeatureTap) -> Unit`, carrying the layer, id, screen point,
  bearing and map position (`ui/map/MapSlot.kt`). There is no tenth `MapSlot` parameter (prediction 2 held).

**B2, one shell, one bubble.**
- `ui/map/MapBubble.kt` holds three things:
  - `AnchoredAtScreenPoint`, moved from `AvailabilityMapOverlaysUi.kt`. It places the bubble through the pure
    `bubblePlacement` and hands the shell the tip in its own coordinates as a `State`, written at placement.
  - `MapBubbleShell`, the card extracted from `ObservationBubble`. It draws the tail from the card's edge to that
    tip, so the tip stays on the anchor when the clamp moves the card (no tail when the clamp puts the card over
    the point).
  - `MapBubbleLayer`, one bubble per host with its targets.
- `ObservationBubble` is now one kind of the shell. Its content and tags (`observation-bubble`, `-close`,
  `-view-on-inaturalist`, `-accuracy`) are unchanged.
- **Host state is one `TappedMapThing`** (`ui/map/MapBubbles.kt`), a sighting or a feature, on each of the
  compact tab, the wide tab and the entry map.
- **F2:** `MapOverlayContent.focusedFeature` names a point feature's bubble. On camera idle `SightingsMap` re-finds
  the glyph in the lists it draws (`focusedFeaturePosition`) and re-fires `onFeatureTap` at its projected point.
  Lines, regions and cells keep the screen tap point.
- **Dismissal:**
  - the close button;
  - a plain `onTap` (empty map);
  - Back, through `MapBubbleLayer`'s own `BackHandler`;
  - opening any target.
- **F4, Back precedence:**
  - compact: `enabled = !isDrawerOpen && pendingAction == null && !pickingSearchLocation && !showActionMenu`;
  - wide: off while its add menu or picker is up;
  - entry map: `backEnabled && !showLayersSheet`. It is composed after the entry screen's fullscreen handler, so
    Back closes the bubble before it leaves fullscreen.

**B3, content per kind** (`mapBubbleContentFor`, pure). Each kind starts from what its row, sheet or card
already shows:

| Kind | Title | Lines | Actions |
|---|---|---|---|
| Find | identification, else "Find on <date>" | "Find on <date>" when titled by the identification; cover photo, 56 dp | "Open in Journal" (Maps), "Open find" (entry map) |
| Photo | its timestamp, or "Date unknown" | what it is in (see Decisions); thumbnail, 56 dp | "View photo" |
| Waypoint | name | MGRS (none when unsupported) | "Directions", "Details" |
| Track | `trackTitle` | start time; "<distance> · <duration>" | "Details" |
| Planned trip | name | "MMM d" date, MGRS, decimal degrees (the Trip Planner row's lines) | "Directions" |
| Offline region | name, and "Stale" when stale | "<radius> · <size>" | "Details" |
| Forecast cell | the layer's own name | "37%"; "Uncertainty 21% to 58%"; one line per driver; "Week of …, weather to …"; the reference class verbatim (`LEGEND_REFERENCE_CLASS`) | none |

- The forecast cell is **re-read from the store** (`lookUpForecastCell`): the group comes from the feed's
  `groupsByLayer` and the week from the feed. The blocks are those touched by half a cell around the tapped point,
  so a tap at 45.97 reaches the cell centred on 46.0 in block 46. The cell is the applicable one whose feature id
  equals the tapped id. The id is compared, never parsed. `forecastCellFeatureId` is now the one writer of that
  id, and `ForecastCellLayer.kt` uses it.
- The fixed term for the real number appears nowhere; the layer's own name ("Test forecast: …") is the title.
- A record not found closes the bubble with a `Log.w`. The entry map's waypoints are named from the entry's
  snapshot (`entryMapWaypoints`). A kept waypoint no longer in Records still gets a bubble from that snapshot, with
  no Details (`WaypointContent.hasDetails`).

**B4, targets in place.**
- **Details:** Details opens J5c's `RecordDetailsSheet` inside `MapBubbleLayer`, with its inputs from
  `MapRecordSources` (waypoints, tracks, visible offline regions, reference counts, unit, now, stale threshold,
  `getFullRecord`), on all three hosts.
- **Photo:** "View photo" opens `PhotoViewerDialog` in place.
- **Directions:** Directions calls `launchDirections`.
- **The find routes.** None of them changes the top tab or the Records chip:
  - **Compact, "Open in Journal"** (F3, ruled not to overwrite the chip). `AvailabilityScreen` sets
    `PendingJournalDestination.VIEW_FIND` with the id, calls `onOpenLogEntry(id)` and switches `compactTab` to
    JOURNAL. `JournalTab` consumes it: its `FindOverView` shows the find's report in an opaque `Surface` over
    whatever the Journal was showing. Back, or the report's own back arrow, closes the find and leaves that view
    as it was.
    - **Checked against the search-bar gate** (`AvailabilityCompactScaffold.kt:401`; this item's line numbers are at `42d9d3d`, before M1 added 4 lines above them). The gate hides the header
      only on the non-Maps tabs while a find is open (`:711`), which is the Journal's existing behaviour for any
      open find. The Maps tab's own bar is gated on "an entry is open and the Journal is showing" (`:898`), so it
      is unaffected.
  - **Wide, "Open in Journal"** (Q3). It opens the drawer on `DrawerPanel.Log`; `LogPanel` consumes `VIEW_FIND`
    the same way. `LogPanel` had no report step, so its overlay shows `LogEntryReportScreen`, whose Edit goes on to
    `onOpenEntryForEditing` and the edit form.
  - **Entry map, "Open find"** (Q4). `JournalTab` and `LogPanel` open the find in the same overlay. The day
    entry's `CartographyScreen` and report stay composed under it, so Back returns to the same entry, view, scroll
    and fullscreen state. The overlay's own `BackHandler` is composed after the entry's, so it wins over the entry's
    fullscreen and close handlers.

**B5, hosts.** Bubbles are on the compact Maps tab, the wide Maps tab and the entry map (compact through
`JournalTab`, wide through `LogPanel`). The centre-pin pickers pass no `onFeatureTap`, so no bubble.

**Threading (Q1 (a)).**
- `AvailabilityScreen` builds `MapRecordSources` for the Maps tab.
- `AvailabilityCompactScaffold` passes it and the find id on.
- `AvailabilityWideLayoutUi.kt` threads it to `MapTab`.
- `JournalTab` and `LogPanel` build the entry map's sources from what they already receive, and hand them through
  `CartographyScreen` to `CartographyEntryReportScreen`.
- `LogPanel` gains `onOpenEntryForReport` (passed `onOpenLogEntry`).

### UI tests (`7e645ef`), through the real screens

`app/src/test/java/com/zynergylabs/forager/app/ui/availability/AvailabilityScreenMapBubblesTest.kt`. The stub slot
(`BubbleMapSlot`) draws 24 dp glyph squares that fire `renderMode.onFeatureTap` from a real touch, counts plain map
taps, and simulates a camera idle for the focused feature.

- **Compact portrait `w384dp-h823dp` (14 tests):**
  - a glyph tap shows the waypoint bubble and is no plain tap;
  - **six real touches across the bubble** (the title's left, centre and right, and the card's padding at top,
    middle and bottom) reach no map;
  - an empty-map touch dismisses the bubble; so do the close button and Back;
  - one bubble at a time;
  - Details for a waypoint, a track and a stale region;
  - Directions for a waypoint and a trip (the `geo:` intent);
  - View photo;
  - the cell bubble's numbers equal the stored cell's, and the store is re-read;
  - re-anchoring on idle, and no revival after a close;
  - "Open in Journal" with the Waypoints chip set beforehand: the find opens over it, and after Back the chip is
    still Waypoints.
- **Short landscape `w823dp-h384dp-land`:** touches across the bubble, then an empty-map dismissal.
- **Wide:** touches, dismissal, and "Open in Journal" to the drawer. **I moved these to `w1280dp-h900dp`:** at
  L0b's `w840dp` the permanent drawer and list take 720 dp and leave the map 119 dp wide, too narrow for a bubble.
- **Entry map, through the compact Journal tab with a day entry open:**
  - a waypoint gone from Records is named from the snapshot, with no Details;
  - "Open find" from the entry's fullscreen map opens the find over it, and after Back the entry is still
    fullscreen.
- **Test corrections while making them pass:**
  - the find report's text is "Your own identification: …", not the bare name;
  - after Back the track's sheet was still there: observed, and consistent with the limit `AvailabilityScreen`'s
    exit-prompt comment records (Robolectric's `onBackPressed` reaches the Activity, not a dialog window), so I split the track and region sheet test into two
    instead of closing one sheet by Back;
  - "Old gate" is matched inside the bubble, since the entry's own text lists it too.

**A check that cannot fail, flagged:** "…and is not a plain map tap" asserts that the stub's glyph click did not
reach the stub's map clickable. That is the stub's own wiring, not production. Production's "a feature tap is not
a plain tap" is `mapTapOutcome`, pinned by `TapPrecedenceTest` and revert check R05. `SightingsMap`'s listener that
reads it cannot run under Robolectric.

### Revert checks

- **The runner** is `app/build/m1/revert.py`, the L0b runner re-rooted. For each check it saves a copy, applies one
  edit that must match once, and runs through `run.sh`, which clears results and refuses to read them on any
  `e: ` line. It requires the exact predicted failing set, each message containing its fragment. It restores from
  the saved copy, never from git, and checks the file's sha256 against `HEAD`'s blob.
- **Deviation:** R19 needed an import (`androidx.compose.ui.zIndex`), which the runner inserts after the package
  line beside the one-line edit.
- **Spec change before running:** R23's compact half was dropped, because on reading the handler order I found the
  Journal's existing find handler would also take that Back. See Findings.
- **The specs and outputs:** `revert/checks.json` from `make_checks.py`, output `revert/run1.out`, report
  `revert/report-checks.json`, logs `revert-R*.log`.

**All 30 compiled (0 `e: ` lines) and every file was restored to `HEAD`'s blob; `git status` was clean after.**

| Check | Edit | Failed | Message, as read | Verdict |
|---|---|---|---|---|
| R01 | cells fall back to the box | 1 of 20 | `expected null, but was:<TapHit(layerId=forecast-chanterelles-layer…` | Confirmed |
| R02 | cells not held back at the point | 2 of 20 | `expected:<TapHit(layerId=kept-tracks-layer…`, `…waypoints-layer…` | Confirmed |
| R03 | breadcrumb `LINE` | 3 of 39 | registry `breadcrumb-trail-layer=LINE`; list and hits name the breadcrumb | Confirmed |
| R04 | search centre `MARKER` | 3 of 39 | the same three, naming `search-center-layer` | Confirmed |
| R05 | feature tap is `Plain` | 1 of 20 | `expected:<OnFeature(…wp-1)> but was:<Plain>` | Confirmed |
| R06 | colour fields `NONE` | 7 of 39 | the registry, the list, and the five cell tests' first halves | Confirmed |
| R07 | waypoint layer has no kind | 2 of 16 | `expected:<WAYPOINT> but was:<null>`; the tapped thing `null` | Confirmed |
| R08 | every kind focused | 1 of 16 | `expected null, but was:<FocusedMapFeature(layerId=kept-tracks-layer…` | Confirmed |
| R09 | find title ignores identification | 1 of 16 | `title=Find on 2026-09-12` | Confirmed |
| R10 | journal count dropped | 1 of 16 | `attachedTo=In Chanterelle)` | Confirmed |
| R11 | never stale | 1 of 16 | `expected:<true> but was:<false>` | Confirmed |
| R12 | cell not matched by id | 1 of 16 | returned the 0.9 neighbour | Confirmed |
| R13 | blocks stop at the finger's latitude | 1 of 16 | the edge cell `but was:<null>` | Confirmed |
| R14 | chance not a percent | 1 of 16 | `chance=0.37` | Confirmed |
| R15 | tail from the unclamped card | 1 of 16 | `the tip Offset(218.0, 900.0) lands on the anchor Offset(20.0, 900.0)` | Confirmed |
| R16 | overlay shows for any find | 1 of 2 | `but was:<FindOverView(findId=find-1, shown=true)>` | Confirmed |
| R17 | overlay never leaves | 1 of 2 | `expected null, but was:<FindOverView(…shown=true)>` | Confirmed |
| R18 | empty-map tap keeps the bubble | 1 of 1 | `expected:<0> but was:<1>` | Confirmed |
| R19 | bubble layer under the map (`zIndex(-1f)`) | 3 of 3 | `none of the six touches on the bubble reached the map expected:<0> but was:<6>`, and the landscape and wide equivalents | Confirmed |
| R20 | bubble Back off | 1 of 1 | `expected:<0> but was:<1>` | Confirmed |
| R21 | `VIEW_FIND` sets the Finds chip | 1 of 1 | `Failed to assert … (Selected = 'true') … Tag: 'records-chip-waypoints'` | Confirmed |
| R22 | compact `VIEW_FIND` shows nothing | 1 of 1 | `journal-find-over-view` not displayed | Confirmed |
| R23 | overlay Back off (entry map) | 1 of 1 | `expected:<0> but was:<1>` | Confirmed |
| R24 | wide `VIEW_FIND` shows nothing | 1 of 1 | `journal-find-over-view` not displayed | Confirmed |
| R25 | entry waypoints keep the placeholder | 1 of 1 | no bubble node with "Old gate" | Confirmed |
| R26 | compact passes no focused feature | 1 of 1 | `the bubble moved down with its glyph (267.66666.dp to 267.66666.dp)` | Confirmed |
| R27 | cell bubble finds no group | 1 of 1 | `map-bubble` not displayed | Confirmed |
| R28 | Details opens nothing | 3 of 3 | `record-details-sheet` not displayed, three times | Confirmed |
| R29 | View photo opens nothing | 1 of 1 | `photo-viewer` not displayed | Confirmed |
| R30 | Directions launches nothing | 2 of 2 | `expected:<android.intent.action.VIEW> but was:<null>`; `ComparisonFailure: expected:<[geo:…(Saddle%20loop)]> but was:<[null]>` | **Mismatch, fragment only** |

R30 failed exactly the two predicted tests, both because no intent started, which is this edit's effect. The
mismatch is my fragment: `but was:<null>`, where JUnit's `ComparisonFailure` prints `<[null]>` for the string
comparison. I did not re-run it with a corrected fragment after reading the result.

**Each failure is one its own edit could cause.** Two I read closely:
- R15's tip is off by exactly the clamp: an anchor at x = 20 and a tip at x = 218;
- R26's bubble did not move at all.

**Not covered by any revert check:**
- `SightingsMap`'s listener and idle re-projection (native, device-only);
- the tail drawing itself (draw phase; the geometry is R15's);
- the photo attachment's find half;
- the entry map's "tap on empty map only dismisses" rule.

### Findings

- **The card's own `pointerInput` is redundant.** The pinned Material3 `Surface` already takes pointer input:
  `javap -c` on `SurfaceKt` from `material3-android` `1.5.0-alpha26` shows a `pointerInput` call. So removing the
  shell's `detectTapGestures {}` would not let touches through, and I did not use it as a revert. R19 (the layer
  under the map) is what shows the coordinate-touch tests bite. I kept the modifier, as the sighting bubble had it.
- **The overlay's Back handler matters only over the entry.** Over Records, the Journal's existing find
  `BackHandler`, registered earlier, is still the latest enabled one (`RecordsTab`'s is off while a find is open),
  so it closes the find. Over a day entry, the entry's fullscreen and close handlers are registered later and would
  win, so the overlay's own handler is what makes Back return to the entry (R23).

### Suites

- **Full suite at `a5ebc27`** (the pushed head, with the UI tests), from a cleared results directory, counts from
  the JUnit XML, 0 `e: ` lines: **274 classes / 2245 tests / 0 failures / 0 errors / 24 skipped**. The class times
  sum to 137 s. The held family (`JournalPendingDeleteTest` album tests, `JournalTabTest` photo pull) passed.
- After `ea3da69`, before the UI tests: 270 / 2225 / 0 / 0 / 24.
- **Baseline: not run. The disk is full.** A throwaway worktree at `4b6b7e5` failed to check out: `No space left on
  device`, with 72 MB free on `/` (67 GB, 64 GB used). I removed that partial worktree and nothing else. Derived,
  not measured:
  - M1 adds 45 tests: `MapBubblesTest` 16, `FindOverViewTest` 2, the UI file 19 across 4 classes, and
    `TapPrecedenceTest` 12 to 20.
  - It adds 6 classes.
  - So the base would be 268 / 2200.
- **Prediction 4 (+50 to +110):** +45 by that count, **below the range**.
- **Predictions 1 and 2:** 1 (no Room migration) held; 2 (no tenth `MapSlot` parameter) held.
- **Prediction 3:** no evidence, since the tests are `@Ignore`d (above).

### Device-only

- The native hit test returning each kind's `featureId`, and the colour fields' cells at the point stage only.
- The offline circle's interior falling through to what is under it (from L0a).
- The tail's tip on the tapped glyph at the screen edges, drawn from the clamped card.
- Bubbles against real insets: the compass strip clearance, the nav, the rail, and the landscape cut-out.
- Re-anchoring on pan, zoom and rotate: `focusedFeature` re-projection at camera idle, and no revival after
  dismissal.
- A feature tap no longer restoring the fullscreen chrome (ruling 1), felt on the phone.
- Directions handing off to the phone's own navigation app.
- The find overlay over the Journal and over a day entry, on the S22, in both window classes.

### Decisions I made

- **A winner with no id** is logged and taken as a plain tap (`MapTapOutcome.UnidentifiedFeature`). The
  alternatives were no action, or opening nothing and firing nothing.
- **The bubble shell's home.** The shell and `AnchoredAtScreenPoint` moved to `ui/map/MapBubble.kt`, and
  `ObservationBubble` stayed in `AvailabilityMapOverlaysUi.kt` as a kind of it. I read Q2 (a)'s "extract the shell
  and fix the tail there" as leave to edit that file for the extraction, not as a ruling that the shell stays in it.
- **The tail's mechanism:** the tip is written into a `State` at placement and read at draw.
- **No tail when the clamp puts the card over the point.**
- **Lines, regions and cells** keep the screen tap point, not the tapped map position re-projected. This is the
  literal reading of "lines, regions and cells keep the tap point".
- **The copy:**
  - "View photo", "Details", "Directions";
  - "Date unknown";
  - the photo line: "In <find>" or "In N finds", "Kept in N journal entries", "Not in a find or a journal entry";
  - "Uncertainty X% to Y%";
  - "label: value" for drivers;
  - a track's "<distance> · <duration>" and a region's "<radius> · <size>";
  - the find title rule.
- **The sizes:** 56 dp thumbnails and `TextButton` actions.
- **Directions dismisses the bubble,** as a target opened.
- **On the entry map, an empty-map tap while a bubble shows** only dismisses the bubble; the next one enters
  fullscreen.
- **The route id beside the enum.** `VIEW_FIND` is an enum case with the id carried beside it
  (`pendingJournalFindId`), because `EDIT_NEW_FIND` is referenced by tests as an enum.
- **The find overlay mechanism for F3, Q3 and Q4:** one overlay, so no saved state is touched. The planner offered
  "or put the chip back"; I chose not changing it. Under it, the Finds slot draws nothing while the overlay is up.
- **`LogPanel`'s overlay report** and its `onOpenEntryForReport` parameter.
- **The kept waypoint no longer in Records** gets a snapshot bubble without Details (`snapshotWaypoints`,
  `hasDetails`).
- **The Back gates** on each host (F4, above).
- **The bubble state is plain `remember`,** not saved across a tab change: a bubble is transient, not a place the
  user chose to be. CLAUDE.md's UX default could be read otherwise.
- **The test harnesses:**
  - the wide test window;
  - splitting the sheet test;
  - folding the absence tests;
  - the R19 import;
  - dropping R23's compact half before running.
- **I skipped the baseline** (the disk is full).

### Flags outside scope

- **The disk is full** (72 MB free on `/`). Other sessions' builds, and the planner's re-run, may fail for it. I
  deleted nothing but my own failed worktree.
- `MapModePicker` and the stale KDoc that L0b flagged are unchanged. `AvailabilityMapOverlaysUi.kt`'s header still
  lists the constants it moved; I added a line saying where they went.
- `JournalTab.kt` and `LogPanel.kt`: the new `Box` wrapping their `Column` is not re-indented, to keep the diff to
  what changed.
- The sighting-bubble tests in `AvailabilityScreenMapIconStackTest` remain `@Ignore`d, so the sighting bubble on the
  new shell has no running UI test.
- The dispatch's "Leaving the Journal" fixes are untouched. A find opened over the Journal and then left by a tab
  change goes through today's incidental-exit path, which that later stage owns.

### D58

`app/build/m1/d58.sh` checks the diff since `789c602`, the commit messages since then, and each pending message
file. Result before every push: 0, 0, 0. This commit is included.
