# Missing planned-trip glyphs, reproduced on the S22 Ultra (compact tree): run record

**Status: pre-registration only.** In this run no Forager screen has been captured or looked at and no trip exists: the
app was force-stopped at 00:13:59Z for the start copy of the database and has not been launched since. This part is
committed and pushed before the first launch, so the order of prediction and observation can be checked. Observations
are added in later commits; this part stays unchanged.

**Date:** 2026-09-29 UTC from 00:09Z (2026-09-28 on the phone's clock, PDT).
**Dispatch:** `prompts/preserved/2026-09-28-94.md` (sha256 `ba0b8144…fe27680`, 4562 bytes), quoted verbatim below with
the launch message. Intent `2026-09-28-94` and continuation `-103` in `RECORD.md` are the planner's. The kit is absent
at this base (no `.claude/`, no checkers; the owner removed them in `e136330`), and the dispatch says the planner writes
the record, so I write no record entry and do not touch `RECORD.md` (Decisions I made, 1).
**Base:** `git fetch` at 00:09Z: `origin/journal-redesign` at `ae0b90f`, the launch note `-103`, as the launch message
says. Branch `device-trips`, cut from `ae0b90f` in my session's worktree, upstream unset so nothing can push to
`journal-redesign`.
**Code citations are at `b358a4a`**, the installed build's commit (`versionName` `1.0.1457+gb358a4aa`), unless marked.
`git diff b358a4a ae0b90f -- app/src/main` touches 7 files (map chrome, palette, layers, bubbles, `SightingsMap`);
`AvailabilityCompactMapUi.kt`, `AvailabilityViewModel.kt` and the trip use cases are not among them. Paths are under
`app/src/main/java/com/zynergylabs/forager/app/`. Abbreviations: ACMU `ui/availability/AvailabilityCompactMapUi.kt`,
AVM `ui/availability/AvailabilityViewModel.kt`, AUS `ui/availability/AvailabilityUiState.kt`, AS
`ui/availability/AvailabilityScreen.kt`, ATWU `ui/availability/AvailabilityTripsWaypointsUi.kt`, AMOU
`ui/availability/AvailabilityMapOverlaysUi.kt`, SM `ui/map/SightingsMap.kt`, ML `ui/map/layers/MapLayers.kt`, MG
`ui/map/MarkerGlyphs.kt`, MP `ui/theme/MapPalette.kt`.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips/`, outside the repository. Under
`-04`'s privacy rule (carried by `-51` and `-72`, which `-94` applies), no screenshot, dump, map coordinate, place name
or photo goes into this file. The trips' coordinates, which `-94` asks me to record, go in `trips-created.txt` in the
evidence directory and are named here (Decisions I made, 2). Screen positions below are pixels on the phone's screen.

## Structural check of the dispatch

Structure only; this cannot detect a decision I was never told about. There is no `.claude/kit.json` at this base to
check section names against (Decisions I made, 1).
- **Role:** present. **Base:** "branch `device-trips` from `origin/journal-redesign`"; the launch message names the
  store copy's commit `ae0b90f`. **Scope boundary:** the S22 only, never the tablet; at most three trips, deleted at the
  end; fix nothing; restore settings; touch nothing else of the owner's. **Prediction:** "Pre-register before creating
  anything" asks for mine; the planner's are in intent `-94`. **Checks:** "The checks", 1 to 5. **Finish line:** in
  intent `-94` ("The run record pushed with verdicts, captures, logcat, the supported cause, the trips deleted and read
  back"). **Abort conditions:** present. **Merge:** "Not authorised."
- Every file it names exists at `ae0b90f`: the pulse's filing (`docs/audits/2026-09-28-planned-trips-pulse.md`, named
  by the launch message) and `prompts/preserved/2026-09-28-72.md`.

## The device and its state (read before any Forager screen)

- **Attached** (`00-devices.txt`, 00:09:58Z): `R5CT321008R` (`model:SM_S908U`) and `R52T506412L` (`model:SM_X800`, the
  tablet; listed, never addressed). Every adb call names `-s R5CT321008R`, and every helper script names no other serial.
- **Identity** (`00-device-facts.txt`): SM-S908U, samsung, fingerprint
  `samsung/b0qsqw/b0q:16/BP2A.250605.031.A3/S908USQSAGZH3:user/release-keys`, Android 16, SDK 36; `wm size` 1080 x 2316,
  `wm density` 450 (1 dp = 2.8125 px). Phone clock `Mon Sep 28 17:10:05 PDT 2026`: **the phone's "today" is 2026-09-28**
  until 07:00Z.
- **Installed build** (`01-dumpsys-package-start.txt`): `versionName=1.0.1457+gb358a4aa`, `versionCode=1457`,
  `lastUpdateTime=2026-09-28 15:28:49`, `firstInstallTime=2026-09-22 11:15:05`, `ceDataInode=2495587`,
  `deDataInode=2494123`, `DEBUGGABLE`. **The build `-84` installed, as the launch message says. Nothing is installed in
  this run.** Permissions: `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` `granted=true` (`USER_SET`), `CAMERA`
  `granted=true`, `POST_NOTIFICATIONS` and `ACCESS_MEDIA_LOCATION` `granted=false`.
- **State:** `isKeyguardShowing=false` (a secure lock is set, so a timeout would be a stop), `mWakefulness=Awake`,
  Forager's `MainActivity` focused (pid 30284, `-84`'s final relaunch).
- **Settings** (`03-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
  `navigation_mode=0`, `location_mode=3`, `screen_off_timeout=600000`, `stay_on_while_plugged_in=15`, the three
  animation scales 1.0, `display_density_forced=450`, `cmd uimode night`: yes. Equal to `-84`'s end reads.
- **App settings** (`05-*-start.pb`, decoded by `pbprefs.py`, map coordinates redacted): all five DataStore files
  byte-identical to `-84`'s end copies. `night_mode.maps=false` (Night Maps off), `map.fullscreen=false`,
  **`map.layer.planned-trips-layer.visible=true`**, every other overlay `true`.
- **Crash buffer** (`02-crash-start.txt`): **0 bytes**.
- **Database at the start** (`db-start-*`, after `am force-stop` at 00:13:59Z, pid 30284 before and none after,
  `06-force-stop-start.txt`): device and local sha256 match (`forager.db` `e88effc0…61af7c93`, `-wal` `55db76e4…31ea95c3`,
  `-shm` `3ddec32f…ea89459`, byte-identical to `-84`'s end copy); header valid, WAL magic valid; `integrity_check` `ok`;
  `user_version` 16; 18 tables: android_metadata 1, cached_searches 2, cartography_entries 7,
  cartography_entry_find_refs 2, cartography_entry_offline_region_refs 1, cartography_entry_photo_refs 1,
  cartography_entry_track_refs 1, cartography_entry_waypoint_refs 3, log_entry_photos 1, log_photos 3,
  mushroom_log_entries 3, offline_regions 2, **planned_trips 0**, room_master_table 1, sqlite_sequence 1,
  track_points 23, tracks 1, waypoints 3. Per-table digests over every column (`db-start-digest.txt`) equal `-84`'s end
  digests for all 18 tables.
- **The two cached searches** share one region (one distinct lat/lng), radius 8 km, "Fungi", months 8 and 9. The August
  row was created by `-04`'s item 20 (a device check); the September row predates it and was refreshed by it
  (`docs/audits/2026-09-28-backlog-device-check-part-a-run-record.md:212-218, 585-586`).

## Premises checked

1. **"The compact Maps tab hands planned trips to the map only after a search has set a region"** (the pulse, at
   `a0a54f9`). At `b358a4a` the same lines: `val hasSearched = uiState.region != null` (ACMU:573) and
   `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()` (ACMU:641). Confirmed.
2. **The S22 takes the compact tree.** 1080 px / 2.8125 = 384 dp wide in portrait, so `WindowWidthClass.COMPACT`
   (AS:1785). Portrait is kept for the whole run (Decisions I made, 3).
3. **"`region` is null until a search runs."** It defaults to null (AUS:30). It is set by manual coordinates
   (AVM:406-407), "Use current location" (AVM:416-423) and a recent search (AVM:792-806). **The map's locate-me button
   does not set it** (`locateMe`, AVM:449-460, whose comment says it touches neither `region` nor any search state).
   Nothing in AVM sets `region` back to null (read, by the `region =` lines in the file). So it is null after every cold
   start (inferred from its being ViewModel state).
4. **"Past dates cannot be created"** (the tablet's terminal `-101`). `TripDatePickerDialog` makes only today and later
   selectable (AMOU:128-133), and `SavePlannedTripUseCase` requires `!date.isBefore(today())` (`domain/SavePlannedTripUseCase.kt:31-33`).
   So at most two trips can be created through the real UI. To be seen in the dialog.
5. **"Use current location, if it raises no permission prompt, or a recent search"** (launch message). Location is
   granted, but "Use current location" runs a live search at the phone's position, which writes a **new**
   `cached_searches` row and may evict one (`data/local/CachedSearchDao.kt:38-48`). Re-running a recent search upserts
   its existing row (the tablet saw only that row's timestamps change). I use the recent search, and the August one,
   which a device check made (Decisions I made, 4).
6. **The trips are named "Trip N"**: `defaultTripName(uiState.plannedTrips.size)` (ACMU:1374).
7. **"Delete through the planner's own delete."** The Trip Planner is a collapsible section of the Tools drawer's search
   panel (`ui/availability/AvailabilitySearchUi.kt:739-740`); each row's delete is an `IconButton` described "Remove
   planned trip for <date>" (ATWU:151-153) that calls `onDeletePlannedTrip` (AVM:839-850), with no undo.

## How a saved trip reaches the compact map (read, at `b358a4a`)

1. **Created:** the cluster's add button opens the `AddActionTile` (ACMU:993-1000, 1279-1293); "Plan a trip" sets
   `pendingAction = PLAN_TRIP` (:1281-1284); the centre pin's OK stores `pendingTripLocation = cameraCenter` (:1337-1340);
   `TripDatePickerDialog` (:1372-1380) calls `onPlaceTripPin` → AVM:825-837, which saves and on success reloads
   (`loadPlannedTrips`, AVM:810-822).
2. **Loaded:** `uiState.plannedTrips` is loaded at `init` (AVM:159-162, "Independent of any search") and after each save
   or delete. `GetPlannedTripsUseCase` sorts and filters nothing (`domain/GetPlannedTripsUseCase.kt:20-25`). A failed load
   logs `AvailabilityViewModel` "Couldn't load planned trips." (AVM:815).
3. **Shown in the Trip Planner, ungated:** `PlannedTripsList(plannedTrips = uiState.plannedTrips, …)` (ATWU:60). Below
   it, **"Choose a region in search options to see rain-driven trip windows."** renders exactly when
   `!uiState.hasSearched` (ATWU:61-68), and `hasSearched` is `region != null` (AUS:310): the same predicate as the map's
   gate. **So that line in a dump is a readable proxy for the gate's state.**
4. **Handed to the map, gated:** ACMU:641, as premise 1. The map itself shows before any search, on a display region
   that is the search region, else the located position, else a fixed fallback (ACMU:554-557). Waypoints (:643) and
   finds, photos, tracks and offline regions (:651-654) are not gated. The whole map is replaced by a spinner while
   `isLoadingSightings` (:531-538) and by a message on a sightings error (:540-544).
5. **Into MapLibre:** `MapSlot` forwards the list (`ui/map/MapSlot.kt:468`); `SightingsMap`'s data effect is keyed on
   `plannedTrips` and `loadedStyle` (SM:623-632) and sets the `planned-trips` source to one point per trip, whatever the
   list holds, an empty list included (SM:1030, 1530-1539).
6. **The layer and glyph:** `planned-trips-layer`, MARKERS, toggleable (ML:350); a `SymbolLayer` with
   `icon-allow-overlap` true and no min or max zoom (SM:953-958); image `planned-trip-flag` (MG:231), a 20 x 28 dp flag
   (MG:41-42), **56 x 79 px on this phone**; fill `#9553A4` by day and `#FA01DD` at night (MP:83, 104), casing white by
   day and black at night (MP:95, 125).
7. **The switch:** "Planned trips" in the Layers sheet sets `visibility` on the loaded style's layer, no style reload
   (SM:673-676, 984-991).
8. **Tap:** the tappable layers are queried under the point, then in a box (SM:400-411). A trip hit opens a bubble from
   `bubbleSources.plannedTrips` (AS:1250, **ungated**), showing the name, date, MGRS, decimal degrees and "Directions"
   (`ui/map/MapBubble.kt:413-419`). With the source empty, nothing is rendered there to hit.
9. **Night:** Night Maps reloads the style, which rebuilds the images and empty sources; the data effect refills them
   (SM:623-632, 860-881).
10. **Camera after a search:** the camera moves to the search region at `zoomForRadiusKm` (8 km gives zoom 12,
    SM:1787-1792) unless the live-location puck is tracking (SM:640-662); a pan breaks tracking.

**Where the code drops a trip on this tree:** the gate at ACMU:641 whenever `region` is null, the switch off, a failed
load (logged), the map replaced (loading or sightings error), or the trip off screen. Nothing else that I found.

## Predictions and pass conditions, written before observing

**Trips:** **A** dated today (2026-09-28); **B** dated 2026-09-30, the owner's future date, as "a few days ahead"
(Decisions I made, 5); **C**, a past date, predicted not creatable. Each is placed at the centre pin after a slow pan
of about 250 px, so it sits clear of the location puck and of the other trip.

**Measurement.** Each case gets a screenshot and a uiautomator dump (`snap.sh`, which refuses to capture unless Forager
owns the focused window, and never reads a stale dump). `pix.py` counts pixels within ±16 per channel of the trip's fill
colour in the map, and `flags.py` groups them into clusters. A baseline count is taken before the first trip.
- **Draws:** I see a flag at the trip's point in the screenshot, **and** a cluster of the trip's fill colour of at
  least 500 px, absent from the baseline, sits at that point.
- **Absent:** no flag seen, **and** no cluster of 20 px or more of the trip's fill colour within 150 px of the point.
- **The trip's point on screen** is the centre pin's point at placing (read from the screenshot). After a double-tap
  zoom at that point, or a quick-zoom anchored there, it stays at that point: MapLibre zooms about the gesture's point.
  That is inferred, not read, and it is checked in phase 2, where the flag is visible, with the same gestures.
- **The gate's state** is read from the Trip Planner's "Choose a region…" line in a dump of the Tools drawer
  (How it reaches the map, 3), at the start of each phase.

### Phase 1: a cold launch, no search run (the case the launch message asks for first)

| # | Case | Prediction | Because |
|---|---|---|---|
| 1-0 | The date dialog, a past day | **Not selectable**; C not created | AMOU:131-133 |
| 1-1 | A, then B, at the placing zoom | **No flag at either point.** The Trip Planner lists both, with the "Choose a region…" line | ACMU:641 (region null); ATWU:60-68 |
| 1-1z | Zoomed in (double-tap) and out (quick-zoom) at each point | **No flag at any zoom** | the source is empty (SM:1030) |
| 1-2 | Planned trips switch off, then on | **No flag in either state** | the switch sets visibility of an empty layer (SM:673-676) |
| 1-3 | Night Maps on, then off | **No flag** (`#FA01DD` 0 px near either point; `#9553A4` 0 px) | the refilled source is still empty (SM:623-632) |
| 1-4 | A real tap at each point | **No bubble** | nothing rendered to hit (SM:400-411) |
| 1-5a | Journal tab and back | **No flag** | `region` still null |
| 1-5b | `am force-stop`, `am start` | **No flag**; the "Choose a region…" line | `region` null at start (AUS:30) |

### Phase 2: after a search (the recent search "Fungi · August" re-run)

| # | Case | Prediction | Because |
|---|---|---|---|
| 2-S | The search runs; the Trip Planner | The "Choose a region…" line **gone** | AVM:792-806 sets `region` before the fetch |
| 2-1 | Pan or zoom so both points are on screen | **Both flags draw**, 56 x 79 px, pole foot at the point, `#9553A4` | ACMU:641 now passes the list |
| 2-1z | Zoomed in and out | **Both draw at every zoom** while on screen | no min/max zoom (SM:953-958) |
| 2-2 | Switch off, then on | **Off: no flag; on: both back** at the same points | SM:673-676 |
| 2-3 | Night Maps on, then off | **Both draw in `#FA01DD`**, then `#9553A4` again | MP:104; SM:623-632 |
| 2-4 | A real tap on each flag | **A bubble**: "Trip 1"/"Trip 2", "Sep 28"/"Sep 30", MGRS, decimal degrees, "Directions" | MapBubble.kt:413-419 |
| 2-5a | Journal tab and back | **Both still draw** | `region` survives in the ViewModel |
| 2-5b | `am force-stop`, `am start` | **No flag again**, the "Choose a region…" line back; after the search is re-run, **both draw** | `region` null at every start |

**If the search fails** (no network, and the row's cache is used or not), `region` is still set (AVM:792-806), so the
gate opens; a sightings error would replace the whole map (ACMU:540-544), and that case is recorded as such, not as
a missing flag.

**What would contradict the gate as the cause:** a flag drawn in phase 1; or no flag in phase 2 with the point on
screen, the switch on and the "Choose a region…" line gone; or a flag after 2-5b's relaunch before the search is
re-run.

**The planner's outcome predictions** (intent `-94`): (1) "The glyph fails to draw for at least one of the three on the
current build, reproducing the owner's S26 report." I predict **both** trips fail to draw before any search and both
draw after one. (2) "The cause is a filter or a missing feed on the compact Maps tab, not the layer switch." I predict
the same, naming ACMU:641.

**Logcat.** No log line is written when trips load or draw successfully (read: the only trip log lines are the failure
warnings at AVM:815, 830 and 844, and SM's layer and tap warnings at SM:440, 875 and 987). So I expect **no** Forager
line about trips, and an empty crash buffer. Absence there is not evidence either way for the gate.

## Method

- **Touches are real:** `adb -s R5CT321008R shell input tap/swipe`, at coordinates from a fresh dump or screenshot.
  Zoom in: a double-tap (`dtap.sh`, two taps 0.1 s apart, `-84`'s method). Zoom out: MapLibre's quick-zoom, a tap and
  then a drag in one adb shell (`qzout.sh`); if the timing fails over adb, I say so. Every input is logged with its time
  in `07-inputs.log`.
- **Launch** with `am start -n com.zynergylabs.forager.app/.MainActivity`, never `monkey`. **Logcat** with `-d` only,
  never `-c`: `logcat -d -b crash` after each phase, and `logcat -d` for the Forager pid's lines at the end.
- **Settings I change in the app and restore:** the Planned trips switch (back on) and Night Maps (back off), read back
  from the dump and from `map_preferences`. No system setting is planned to change; rotation stays at 0.
- **Database:** `dbcopy.sh`, `dbverify.sh` and `dbdigest.py` (copied from the tablet and map-chrome checks, serial and
  paths changed), always after a force-stop; the raw copy is never opened. After the relaunch check (the trips' ids),
  and at the end, after the deletes.
- **Stop at once** at any system or Google prompt, a locked phone, a new crash, or a trip that cannot be deleted.

## Decisions made before observing (carried into the final "Decisions I made")

1. **No record entries.** My standing instructions call for a sweep, an intent and a structural check against
   `.claude/kit.json`. At this base there is no `.claude/` and no checker (`e136330`); the dispatch says the planner
   writes the record, the launch message says I do not touch `RECORD.md`, and `-04` says the kit's record steps do not
   apply. The main checkout's `kit.json` (`faf2f88`) lists `device` sections this dispatch does not have by those names
   (Base and state, Scope boundary, Closed decisions, Prediction, Finish line and abort conditions, Checks, Out of scope,
   Device items). I followed the dispatch and the base, as the `-51`, `-72`, `-84` and `-95` coders did. Which applies
   is the owner's to rule.
2. **Trip coordinates go in the evidence directory, not here** (`-94` asks for them; `-04`'s rule keeps coordinates out
   of the repository), as the tablet coder did.
3. **Portrait only.** The dispatch names no rotation; the phone was found at rotation 0 and the compact tree is the
   question.
4. **The search is the recent "Fungi · August" re-run,** not "Use current location": the latter writes a new cache row
   at the phone's real position and may evict one; the August row was made by a device check (`-04` item 20). The
   re-run still changes that row's timestamps (and perhaps its cached results); it is read back and reported.
5. **Dates:** today, and 2026-09-30 (the owner's future date) for "a few days ahead".
6. **An objective glyph measure** (`pix.py`, `flags.py`) beside my reading of each screenshot; the dispatch asks for
   screenshots and dumps only.
7. **No D58 phrase check.** The tablet coder ran one before each push; my attempt to set it up was refused by the
   permission system, and I did not pursue it. Flagged.

## Observations

Everything below was written after observing. Captures are in the evidence directory; each has a `.png` and a `.xml` of
the same name unless marked, and `snaps.log` holds each capture's UTC time, rotation and hashes. Every input is logged
with its time in `07-inputs.log`. "Trip pixels" are `pix.py` counts of the trip's fill colour (±16 per channel) in the
map box `[0,250][1080,1950]` (below the compass strip, above the attribution line).

### Phase 1 setup: a cold launch, no search

- `am start -W` at 00:19:1xZ: `Status: ok`, `LaunchState: COLD`, pid 4821, Forager's `MainActivity` focused, no prompt
  (`10-launch.txt`).
- **The compact Maps tab, before any search** (`11-p1-launch`): the bar reads "September · Search a location"; the map
  node spans `[0,75][1080,2316]`, so its centre is (540, 1196); the existing DEVICE CHECK find, waypoint and 1 km offline
  region circle are drawn at the centre, with the location puck beside them. **Waypoints, finds and offline regions draw
  before a search** (ACMU:643, 651-654). The compass strip read "Location services unavailable" at this first capture
  and a heading, elevation and MGRS line from the next capture on (flag 2).
- **Baseline trip pixels** (`12-pix-baseline-p1.txt`): `#9553A4` 0 px, `#FA01DD` 0 px.
- **The gate's state before any trip** (`13-p1-tools`, `14-p1-trip-planner-empty`): the Tools drawer's Trip Planner reads
  "No trips planned yet. …" and **"Choose a region in search options to see rain-driven trip windows."**: `region` is
  null.

### Check 1-0: a past date (observed)

- In the date dialog for trip A (`18-p1-date-dialog-A`), every September day before the 28th is `enabled=false`, the 26th
  included; "Today, Monday, September 28, 2026" is `checked=true`.
- A real tap on the 26th's cell (944, 1522) changed nothing: the next dump is byte-identical (`19-p1-date-dialog-after-past-tap`,
  dump `62e3418a869e663e` both times).
- **Verdict: as predicted. Trip C (past) cannot be created through the real UI.** Two trips were created, not three.

### Trips created

The compact route with real taps: a slow pan (`swipe` 400,1450 → 400,1200 over 1200 ms), the cluster's add button
(990, 1325), "Trip" in the add tile (483, 1326), the centre pin's OK (287, 1865), the date dialog, "Plan trip" (914, 1836).

| Trip | Name (the default) | Date | Saved (UTC) | Captures |
|---|---|---|---|---|
| **A** | Trip 1 | 2026-09-28 (today) | 00:21:2xZ | `15-p1-after-pan-A`, `16-p1-add-menu-A`, `17-p1-centre-pin-A` (crop `17-p1-centre-pin-A-crop.png`), `18-p1-date-dialog-A`, `20-p1-after-A` |
| **B** | Trip 2 | 2026-09-30 | 00:22:2xZ | `21-p1-after-pan-B`, `22-p1-add-menu-B`, `23-p1-centre-pin-B`, `24-p1-date-dialog-B`, `25-p1-date-dialog-B-sep30`, `26-p1-after-B` |

- B's dialog offered the default name "Trip 2" (`24-`), so `plannedTrips` held A by then (ACMU:1374).
- **The Trip Planner lists both** (`28-p1-trip-planner-AB`): "Today", "Trip 1", "Sep 28", and "Trip 2", "Sep 30", each with
  an MGRS and a decimal-degrees line, "Directions to Trip N" and "Remove planned trip for 2026-09-28" / "…2026-09-30";
  and below them **"Choose a region in search options to see rain-driven trip windows."** So both trips are in
  `uiState.plannedTrips` while `region` is null. The coordinates are in `trips-created.txt` in the evidence directory; the
  ids come from the database copy after check 1-5b.
- **Where the points are on screen.** Each trip is stored at `cameraCenter`, the camera's target (ACMU:1340), which is
  the map view's centre, (540, 1196). The centre pin's drawn tip is at about (540, 1216) (`17-…-crop`, my reading), 20 px
  lower. The DEVICE CHECK find's `#DA02AF` cluster moved up by **exactly 236 px** at each pan (`[507,1122]` → `[507,886]` →
  `[507,650]`), so after B's placing, **A's point is at (540, 960) and B's at (540, 1196).** The two trips are 0.0101° of
  latitude apart (evidence file), about 1.12 km for 236 px, which is zoom 12 at this latitude (inferred from the Web
  Mercator scale).

### Check 1-1: each trip just after creating it, at the placing zoom, no search

| After | Capture | Trip pixels in the map | Seen | Verdict |
|---|---|---|---|---|
| A | `20-p1-after-A` (crop `20-p1-after-A-crop.png`) | `#9553A4` **0 px**, `#FA01DD` 0 px | ground only at (540, 1196) | **absent** |
| B | `26-p1-after-B` | `#9553A4` **1 px**, at (509, 742), 218 px from A's point and 454 px from B's; `#FA01DD` 0 px (`27-pix-p1-after-B.txt`) | ground only at both points | **both absent** |

**Verdict: as predicted.** With both trips saved and listed, and `region` null, no flag is drawn at either point.

### Check 1-1z: zoomed in and out on each trip, no search

**How the points were followed.** With no flag drawn, each trip's point is carried from its placing position through
each gesture: a pan is measured by `shift.py` (a map patch matched between two captures; it reproduces the known 236 px
pans exactly, as a control), and a zoom by the size of the 1 km DEVICE CHECK offline region's dashed circle (radius
210 px at the placing zoom; my reading of each screenshot, about ±10%). A double-tap zooms about its point (seen for A:
the circle's edge 58 px above A at +1 and about 116 px at +2), **except while the camera tracks the location puck**, when
it zoomed about the puck (`37-`, inferred from MapLibre's tracking mode, not read). So the points below are exact where
marked and estimates (±30 px) elsewhere; the trip-pixel count covers the whole map box either way.

| Capture | Gesture | Zoom vs placing (circle) | A's point | B's point | Trip pixels | Verdict |
|---|---|---|---|---|---|---|
| `30-p1-A-zoom-in-1` | double-tap on A | +1 | (540, 960) exact | (540, 1432) | 0 / 0 | **absent** |
| `31-p1-A-zoom-in-2` (crop `31-…-crop.png`) | double-tap on A | +2 | (540, 960) exact | off screen | 0 / 0 | **absent** |
| `32-p1-zoom-out-1` | quick-zoom v1 (**panned 301 px instead**) | +2 | (540, 659) | off screen | 0 / 0 | **absent** |
| `33-p1-zoom-out-2` | quick-zoom v2 (**zoomed out**) | about +1.2 | ≈ (537, 879) | ≈ (537, 1429) | 0 / 0 | **both absent** |
| `34-p1-zoom-out-3` | quick-zoom v2 | about −0.33 | ≈ (537, 1087) | ≈ (537, 1275) | 0 / 0 | **both absent** |
| `35-p1-zoom-out-4` | quick-zoom v2 (a pan and possibly a zoom; the matcher's fit was poor) | not determined | not determined | not determined | 0 / 0 | not used |
| `36-p1-locate-me` | "Center on my location" | about −0.33 | ≈ (540, 1372) | ≈ (540, 1561) | 0 / 0 | **both absent** |
| `37-p1-B-zoom-in-1` | double-tap on B (zoomed about the puck) | about +0.67 | ≈ (540, 1617) | below the map box | 0 / 0 | **absent** |
| `38-p1-pan-to-B` | pan 382 px (measured) | about +0.67 | ≈ (540, 1235) | ≈ (540, 1610) | 0 / 0 | **both absent** |
| `39-p1-B-zoom-in-2` (map `39-…-map.png`) | double-tap on B | about +1.7 | ≈ (540, 860) | (540, 1610) | 0 / 0 | **both absent** |

"Trip pixels" is `#9553A4` / `#FA01DD` in the whole map box.

- **Verdict: as predicted.** No flag at any zoom tried, from about −0.33 to +2 levels about the placing zoom, with each
  point on screen in at least one capture at each direction of zoom. The DEVICE CHECK find, waypoint, offline regions and
  the location puck drew in every capture where their ground was on screen.
- **Not reached:** a zoom-out of more than about a third of a level. MapLibre's quick-zoom over adb is unreliable: of four
  attempts, one zoomed out 0.8 levels, one 1.5 levels, and two panned (`32-`, `35-`). The first form (`input tap` then
  `motionevent`s) panned; the second (all `motionevent`s) zoomed twice and panned once. `input` calls take about 47 ms on
  this phone (timed), so the double-tap window alone does not explain it; not investigated further.
- **"Center on my location"** recentred on the puck without changing the zoom and without setting `region` (the next
  captures still show "Search a location"), as AVM:449-460 reads.

### Check 1-5a: a tab round trip, no search (run before 1-2 to 1-4; Decisions I made, 8)

- Journal (`40-p1-journal`, dump only: the Journal shows the owner's entries): **the dump holds no MapLibre node**, so the
  map was taken down. Maps again (`41-p1-maps-after-journal`): the map was rebuilt **at the launch view**: `shift.py`
  against `11-p1-launch` gives dx 0, dy 0 (score 1.03), and against `26-p1-after-B` dy +472 = 2 × 236. So at the placing
  zoom, **A's point is at (540, 1432) and B's at (540, 1668), exactly**, both on screen.
- Trip pixels 0 / 0; no flag seen at either point. The "Search a location" bar is unchanged.
- **Verdict: as predicted, both absent.** This view is the reference for checks 1-2 to 1-4.

### Check 1-2: the Layers "Planned trips" switch off, then on, no search

`ltoggle.sh` (copied from `-84`): opens the Layers sheet from the cluster, taps the "Planned trips" row's clickable node,
reads its `checked` state back from a second dump, closes the sheet with Back, then captures.

| State | Capture | Row `checked` | Trip pixels | Map against `41-` | Verdict |
|---|---|---|---|---|---|
| off (00:31:07Z) | `42-p1-trips-off` | `true` → **`false`** | 0 / 0 | identical (dx 0, dy 0, score 0.00) | **absent** |
| on (00:31:36Z) | `43-p1-trips-on` | `false` → **`true`** | 0 / 0 | identical (score 0.00) | **absent** |

- `map_preferences` reads `map.layer.planned-trips-layer.visible = True` afterwards.
- **Verdict: as predicted.** The switch changes nothing on screen, because there is nothing in the layer to show or hide.

### Check 1-3: by day and at night, no search

`setnight.sh` (copied from `-84`): Tools, Settings, the Night Maps row, read back from the dump and from `map_preferences`.

| State | Capture | `#FA01DD` | `#9553A4` | Control | Verdict |
|---|---|---|---|---|---|
| night (checkbox `true`, `night_mode.maps = True`) | `44-p1-night` | **0 px** | 0 px | the find in its night colour `#F96FAC`, 2860 px at `[507,1123][600,1195]`, its own place: the style reloaded in the night palette | **both absent** |
| day again (`false`, `night_mode.maps = False`) | `45-p1-day-again` | 0 px | **0 px** | the find in `#DA02AF`, 2862 px at the same bounds; map against `41-` dx 0, dy 0 | **both absent** |

- **Verdict: as predicted.** Night Maps is back off and read back.

### Check 1-4: a real tap where each trip sits, no search

| Touch | Capture | Bubble in the dump | Verdict |
|---|---|---|---|
| A's point (540, 1432) | `46-p1-tap-A-540-1432` | none (dump identical to `45-`, `5db193b3…`) | **no bubble** |
| where A's pennant would be (565, 1372) | `47-p1-tap-A-565-1372` | none (identical) | **no bubble** |
| B's point (540, 1668) | `48-p1-tap-B-540-1668` | none (identical) | **no bubble** |
| where B's pennant would be (565, 1608) | `49-p1-tap-B-565-1608` | none (identical) | **no bubble** |
| **control:** the DEVICE CHECK find's glyph (540, 1140) | `50-p1-tap-find-control` | "DEVICE CHECK find 1", "Find on 2026-09-27", "Open in Journal", "Close" | the find's bubble opens |

- The control bubble was closed with Back (`51-p1-bubble-closed`, dump `5db193b3…` again). Nothing in it was touched.
- **Verdict: as predicted.** The tap path works in this state (the control), and at the trips' points there is nothing
  to hit. The pennant positions are where the tablet's flags drew relative to their points (above and to the right of
  the pole foot), not measured on this phone yet.

### Check 1-5b: a force-stop and relaunch, no search; the trips' ids

- **Crash buffer before the stop** (`52-crash-p1.txt`): 0 bytes.
- `am force-stop` at 00:34:42Z, pid 4821 before, none after (`53-force-stop-p1.txt`).
- **Database copy** (`db-p1-*`): device and local sha256 match (`forager.db` unchanged, `e88effc0…61af7c93`; the WAL and
  SHM changed); `integrity_check` `ok`; `user_version` 16; **`planned_trips` 2**, `cached_searches` 2. Against the start's
  per-table digests **only `planned_trips` differs**; every other table is unchanged (`db-p1-digest.txt`).

**The trips created** (coordinates in `trips-created.txt` in the evidence directory):

| Trip | id | name | date |
|---|---|---|---|
| **A** | `5deff079-1dba-440e-b947-46e53b0e18d3` | Trip 1 | 2026-09-28 |
| **B** | `47b2784e-6ba8-46cd-96fc-f76bee22ce15` | Trip 2 | 2026-09-30 |

- **Relaunch:** `am start -W` at 00:34:5xZ, `Status: ok`, `LaunchState: COLD` (`54-relaunch-p1.txt`). The map opened at the
  launch view again (`55-p1-relaunch`; `shift.py` against `11-p1-launch` dx 0, dy 0, score 1.02), so **A's point is at
  (540, 1432) and B's at (540, 1668)**. Trip pixels 0 / 0; no flag seen. The bar reads "September · Search a location".
- **The Trip Planner after the relaunch** (`57-p1-relaunch-trip-planner`): "Today", "Trip 1", "Sep 28" and "Trip 2",
  "Sep 30", loaded at start-up (AVM:159-162), and **"Choose a region in search options to see rain-driven trip
  windows."**: `region` is null again.
- **Verdict: as predicted, both absent.**

### Phase 1 in one line

With no search run, **both trips are saved (database), loaded (the Trip Planner lists them) and never drawn**: 0 trip
pixels in every capture of the map, from `20-` to `55-`, at the placing zoom, zoomed in to +2 and out to about −0.33, with
the switch off and on, by night and by day, after a tab round trip and after a relaunch, and no bubble where they sit;
while the find, waypoint, photo and offline regions draw and a tap on the find opens its bubble. **`region` is null
throughout:** the bar reads "September · Search a location" in every one of those captures, and that text is what the
bar shows exactly when `region` is null (`val where = uiState.region?.let { … } ?: "Search a location"`,
`ui/availability/AvailabilitySearchUi.kt:565`); the Trip Planner's "Choose a region…" line, the same predicate (ATWU:62),
was read at three points (before the trips, after them, after the relaunch). That is the condition under which ACMU:641
hands the map an empty list. (Corrected in the commit after `a46c992`, which said the Trip Planner line was read "in
every one of those states"; it was read at three.)

### Phase 2, check 2-S: the search (the recent "Fungi · August" re-run)

- A touch on the bar's text (500, 132) opened the dropdown (`60-p2-search-open`), and a touch on "Recent searches"
  expanded it (`61-p2-recent-open`): "Fungi · September" and "Fungi · August", each "cached 19 hours ago", with the same
  region (its coordinates are in the dumps, not here).
- **A stray touch on the keyboard (a deviation).** The Samsung keyboard was up from the first touch; I read the dumps,
  which do not include the keyboard's window, and not the screenshots, so my touch meant for the August row, at
  (215, 1465), landed on the keyboard's toolbar about 70 px from its Galaxy AI button (`61-p2-recent-open-crop.png`), and
  the keyboard showed its writing-assist panel ("Spelling and grammar", "Writing style", "Composer";
  `63-p2-after-search`, whose dump is identical to `61-`'s: nothing in Forager changed). **No dialog, consent or sign-in
  appeared.** I judged the panel part of the keyboard, not a system prompt, did not touch it, and lowered it with the
  system Back key (`64-p2-after-back-1.png`, screenshot only; `dumpsys input_method` `mInputShown=false`), as `-84` lowered
  the keyboard (Decisions I made, 9; flag 3).
- With the keyboard down, a touch on the August card's centre (300, 1520) at 00:38:5xZ ran the search. After 7 s
  (`66-p2-after-search`) **the bar reads "August · 5 mi"**: `region` is set (`AvailabilitySearchUi.kt:565`). The Trip
  Planner (`68-p2-trip-planner`) lists both trips and, **in place of the "Choose a region…" line, rain-driven trip windows**
  ("7–9 days after 1.2 in of rain ending Sep 25", …): `hasSearched` is true (ATWU:62-63).
- **The camera did not move:** `66-` against `55-p1-relaunch` dx 0, dy 0 (score 0.13). The puck was tracking since the
  relaunch, so the region move was skipped (SM:640-662, as read). So the trips' points are where phase 1 put them.
- **Verdict: as predicted** (the gate's state changed; no sightings error; no spinner left on screen).

### Check 2-1: after the search, at the placing zoom

| Trip | Capture | Trip pixels (`#9553A4`) | Cluster bounds | Predicted point | Seen | Verdict |
|---|---|---|---|---|---|---|
| A | `66-p2-after-search` (crop `66-p2-after-search-crop.png`) | **2175 px** | `[536,1354][591,1431]` = 19.9 x 27.7 dp | (540, 1432) | a purple flag, white casing, its pole foot at the point | **draws** |
| B | `66-p2-after-search` | **2175 px** | `[536,1590][591,1667]` = 19.9 x 27.7 dp | (540, 1668) | the same | **draws** |

- The map-wide count is 4350 px in exactly these two clusters and nothing else (baseline 0). The clusters' size is the
  glyph's 20 x 28 dp (MG:41-42); their bottom edges are 1 px above the pre-registered points, and their 236 px separation
  is the placing pan.
- Sighting dots also appeared with the search (grey discs across the map; ACMU:576-580 gates them the same way).
- **This is the reproduction:** the same two trips, at the same camera, in the same process, draw nothing for 20
  captures while `region` is null, and draw as soon as a search sets it, with nothing else changed.

## Appendix: the dispatch and the launch message, verbatim

### `prompts/preserved/2026-09-28-94.md` at `ae0b90f`, the whole file

> HEAD: a0a54f9 (journal-redesign) when written. **Queued until the map-chrome device check (-84) frees the S22;** the launch message names the planned-trips pulse's filing.
> Target subagent: coder (Agent tool, planner session on the owner's computer)
> Type: device
> Preserved: 2026-09-28T23:22:21Z by the planner, by hand, time read from the clock.
> --- verbatim prompt follows ---
> **Type:** device
>
> # Role
>
> You are the coder for **reproducing the missing planned-trip glyphs on the S22 Ultra** (`R5CT321008R`). Pass `-s R5CT321008R` on every adb command, and never touch the tablet `R52T506412L`. You observe and record. **You fix nothing and change no app code.** This dispatch's intent is `2026-09-28-94`. The planner writes the record.
>
> # The owner's words, verbatim
>
> - "I noticed there are no day/night icons for planned trips, even though they do record in the planner. The layer is available in the map layers panel though." This was sent with screenshots from the owner's S26, showing the Maps tab at night with no trip glyph, and the Trip Planner listing three trips:
>   - Trip 1, Sep 26 (past);
>   - Trip 3, Sep 28 ("Today");
>   - Trip 2, Sep 30 (future).
> - Asked whether the trips appear when zoomed out or panned to them: "They do not appear at all".
> - "Yes from thr S26" (the phone). "Verify it on the S22 ultra".
>
> The planner offered this check as: "the agent would add one "DEVICE CHECK" trip near the phone, see whether it draws by day and at night with the Planned trips layer on and off, then delete it."
>
> Read first:
> - `CLAUDE.md`, especially Bug fixing: see the failure before the fix, and confirm it fails for the reason expected;
> - the planned-trips pulse's filing named in the launch message. It maps every condition under which a trip is or is not drawn;
> - `prompts/preserved/2026-09-28-72.md`, whose device rules apply.
>
> # Standing rules
>
> - Never run `logcat -c`. Launch with `am start`, never `monkey`.
> - **Stop at once at any system or Google prompt.**
> - Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips/`.
> - **No install:** use the build already on the phone (from `-84`), and record its versionName.
> - Restore every setting you change, and read it back.
>
> # Data
>
> - Through the app's real UI (placement, then the date dialog, then confirm), create **at most three planned trips** near the map's current view:
>   - one dated **today**;
>   - one dated **a few days ahead**;
>   - one **in the past**, if the date dialog allows it.
>
>   That mirrors the owner's three.
> - Record each trip's id, date, coordinates and name. The app names them "Trip N"; you cannot label them "DEVICE CHECK", so identify them by id.
> - **Delete exactly those trips at the end** through the planner's own delete. Then read back, from a database copy made after a force-stop, that `planned_trips` is back to its starting count (0 in the J8 check's backup).
> - Touch nothing else of the owner's.
>
> # Pre-register before creating anything
>
> From the pulse's map and the code (file:line), write down:
> - the conditions under which each of your three trips should draw;
> - your prediction for each case below.
>
> Push those before creating a trip.
>
> # The checks
>
> For each trip:
> 1. After creating it, is a glyph drawn at its location? Check at the zoom where it was placed, then zoomed in on it, then zoomed out.
> 2. With the Layers "Planned trips" switch off and then on.
> 3. By day (Topographical) and at night (Night Maps on).
> 4. Does tapping where it sits open its bubble?
> 5. After switching tabs and back, and after a force-stop and relaunch.
>
> For every case, capture a screenshot and a uiautomator dump. Read `logcat -d` for Forager lines about trips or layers.
>
> **If the glyph is missing,** record exactly which conditions hold. Compare them with the pulse's filters to name the cause the code predicts, and say whether the evidence confirms it. **Do not fix it.**
>
> # Report
>
> Write `docs/audits/2026-09-28-planned-trips-device-check-run-record.md` on branch `device-trips` from `origin/journal-redesign`. Push after each check; the planner merges it. Record:
> - the build;
> - the trips created and deleted, with ids;
> - every verdict;
> - the captures;
> - the logcat lines;
> - the cause the evidence supports, marked as read, observed or inferred;
> - what was restored and read back;
> - **Decisions I made**;
> - **Flags outside scope**.
>
> # Abort conditions
>
> - any prompt over the app;
> - a locked phone;
> - a new crash;
> - a trip that cannot be deleted;
> - any need to touch the owner's data or the tablet.
>
> # Merge
>
> Not authorised.

### The launch message

The planner's message that launched me. The store copy above governs over it.

> This is planner dispatch `2026-09-28-94`: **reproduce the missing planned-trip glyphs on the S22 Ultra** (`R5CT321008R`). It is launched by `RECORD.md` entry `2026-09-28-103`. Its store copy is committed at `prompts/preserved/2026-09-28-94.md` on `origin/journal-redesign` at `ae0b90f`. Read it in full and quote it verbatim in your run record; the file governs over this message.
>
> **Read before pre-registering:**
> - **The pulse filing** the dispatch refers to: `docs/audits/2026-09-28-planned-trips-pulse.md`. Its central finding is that the compact Maps tab hands planned trips to the map only after a search has set a region (`AvailabilityCompactMapUi.kt:573`, `:641`).
> - **The tablet result** (terminal `2026-09-28-101`, `docs/audits/2026-09-28-planned-trips-tablet-run-record.md`): on the wide tree, after a search, trips drew in every case. Past dates cannot be created (the dialog disables them, and `SavePlannedTripUseCase.kt:31-33`).
>
> **So on the compact tree, the case to observe first:** with trips saved, after a **cold launch with no search run**, are the flags absent? Then run a search (for example "Use current location", if it raises no permission prompt, or a recent search), and pan or zoom to the trips. Do the flags appear? That is the reproduction the fix (`-97`) waits on.
>
> The essentials, stated in full in the file:
> - **Devices:** the S22 only, `-s R5CT321008R` on every adb call. Never touch the tablet `R52T506412L`.
> - **Build:** no install. The installed build should be `1.0.1457+gb358a4aa`; record it.
> - **Trips:** create at most three through the real UI (today, and a few days ahead; past is not possible), and record their ids. **Delete exactly those at the end**, then read `planned_trips` back from a database copy taken after a force-stop.
> - **Checks:** for each case, zoom, the Layers switch on and off, day and night, tap, a tab round trip, and a relaunch. **Fix nothing.**
> - **Stop at once at any system prompt.** Restore settings and read them back.
> - **Where things go:** evidence in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips/`. The run record is `docs/audits/2026-09-28-planned-trips-device-check-run-record.md` on a new branch `device-trips`, from `origin/journal-redesign`, pushed after each check. The planner merges it; you do not touch `RECORD.md`. Merge is not authorised.
>
> When you finish or stop, hand back a report: the build, the trips created and deleted, every verdict, captures by name, logcat lines, the supported cause (marked read, observed or inferred), what was restored, decisions you made and flags.

## Resumed (dispatch `2026-09-28-94`, continued by `RECORD.md` `2026-09-28-112`): pre-registration

**Written before any Forager screen was captured in this resumed run.** Observations are added in later commits; this
part stays unchanged. The section above is the earlier coder's and is not rewritten.

**Who and where.** A fresh device coder in worktree `/home/zynergy-labs/Zynergy/forager-wt/device-trips`, branch
`device-trips`, at `f35e815` (`origin/device-trips`, verified equal at 01:0xZ). The launch prompt says the session should
be on `/model claude-sonnet-5-5`; **the session's configuration names `claude-sonnet-5-5`, but I cannot read which model
served the turns (the `get_session` tool is not available to me), so the serving model is unverified.** No install, no app code
changed, no `RECORD.md`, index, `CLAUDE.md`, `docs/plans/` or `prompts/` touched. The dispatch (`prompts/preserved/2026-09-28-94.md`
on `origin/journal-redesign`, read in full) is quoted verbatim in the appendix above; `RECORD.md` `-103` and `-112` are quoted
in the handoff, not re-quoted here beyond: `-112` "A fresh device coder on Sonnet 5.5 re-reads the phone's state first, then
finishes -94's remaining checks where they add evidence. It **deletes exactly trips A and B** through the planner and reads
back planned_trips at its starting count. It restores settings and writes the verdicts into the existing run record."

**Phone state read first** (`100-resumed-state.txt`, 01:06:42Z, 2026-09-28 18:06 PDT on the phone): `R5CT321008R` and the tablet
listed, only the S22 addressed; `user_rotation` 0, `accelerometer_rotation` 0, `font_scale` 1.0, `screen_off_timeout` 600000,
`navigation_mode` 0, `location_mode` 3, `display_density_forced` 450, `cmd uimode night`: yes (the phone's own setting, as at the start
of the run above); Forager's `MainActivity` focused, `isKeyguardShowing=false`; `versionName` `1.0.1457+gb358a4aa`, `versionCode` 1457,
`lastUpdateTime` 2026-09-28 15:28:49; **pid 12298** (the earlier run's last read was pid 4821, so the app has been restarted since
phase 2 and `region` may be null again: read before use); crash buffer 0 bytes; 1080x2316 at 450 dpi (compact tree).

**What phases 1 and 2 already give, and what this run adds.** Phase 1 covered every check with no search (0 trip pixels in 20+
captures). Phase 2 covered only check 2-1 (both flags draw at their points after a recent search). This run adds the
with-search cases the dispatch's checks 2, 3, 4 and 5 still lack: **2-2 switch, 2-3 night, 2-4 tap, 2-5b relaunch.**
**Skipped, and why** (they add nothing beyond phases 1 and 2 for the cause): 2-1z (zoom with the flags visible: phase 1 showed
the zoom gestures are unreliable over adb, and no-min/max-zoom is read at SM:953-958) and 2-5a (a tab round trip with a search:
phase 1's round trip rebuilt the map at the launch view with `region` unchanged; the with-search version tests the ViewModel
keeping `region`, which is neither what the reproduction turns on nor needed to name the cause). Both stay open in the table.

### Predictions and pass conditions (same measure as above: `pix.py` count of `#9553A4` by day and `#FA01DD` at night within ±16 per
channel, the flag a 19.9 x 27.7 dp cluster, `snap.sh` for every capture)

Precondition for every row: `region` set, read from the bar ("August · 5 mi" rather than "Search a location") and from the Trip
Planner's "Choose a region…" line being absent. If the app is found with `region` null I re-run the "Fungi · August" recent
search first (the same route as phase 2) and record it. Trip points are read from the screenshot, not assumed.

| # | Case | Prediction | Because |
|---|---|---|---|
| R-0 | State on first capture | `region` null (the app restarted, pid 12298 not 4821) and no flag; after the search re-run, both flags draw | ACMU:573, 641; AUS:30 |
| 2-2 | Planned trips switch off, then on | **Off: 0 px of `#9553A4`, no flag; on: both flags back** at the same screen points | SM:673-676, 984-991 |
| 2-3 | Night Maps on, then off | **Night: both flags in `#FA01DD` (about 2175 px each, same cluster size) and 0 px of `#9553A4`; day again: `#9553A4` back, 0 px `#FA01DD`** | MP:83, 104; SM:623-632, 860-881 |
| 2-4 | A real tap on each flag (at the flag body, a few px above the pole foot) | **A bubble** with "Trip 1"/"Trip 2", "Sep 28"/"Sep 30", an MGRS, decimal degrees, "Directions" | MapBubble.kt:413-419; SM:400-411 |
| 2-5b | `am force-stop`, `am start -W` | **No flag; the "Choose a region…" line back**; after the "Fungi · August" re-run, **both draw** | `region` null at every start (AUS:30) |

**What would contradict the supported cause** (`region` null gating the list at ACMU:641): a flag drawn with `region` null; no
flag with `region` set, the switch on and the points on screen; a flag after the relaunch before the search is re-run.

**Delete and read-back (step 3).** Only through the Trip Planner's "Remove planned trip for <date>" buttons (ATWU:151-153), for the
two rows named "Trip 1" (2026-09-28) and "Trip 2" (2026-09-30), whose ids are `5deff079-1dba-440e-b947-46e53b0e18d3` and
`47b2784e-6ba8-46cd-96fc-f76bee22ce15` (`trips-created.txt`, from a database copy). If the planner lists any other trip, or these
two are not the only rows, I stop. Pass: after `am force-stop`, a database copy (`dbcopy.sh`, `dbverify.sh`, `dbdigest.py`, never
opening the raw copy) shows `planned_trips` 0 rows, `integrity_check` ok, and every other table's digest equal to the start's
(`db-start-digest.txt`), except `cached_searches` timestamps, which the search re-run changes (compared and reported).

**Settings restore (step 4).** I expect to change only the Planned trips switch (back to `true`) and Night Maps (back to `false`),
each read back from the dump and from `map_preferences`; no system setting is planned to change (rotation stays 0/0). The
phone's `cmd uimode night` is the phone's own state and I do not touch it.

**D58.** Before each push I check the diff and commit messages for the three phrases in forager-forecast `docs/planning/DECISIONS.md`
row D58 (Decision column); I do not write them anywhere. Reading that row is the check's input; if I cannot read it, I say so.

## Resumed: observations, step 2 (checks with the search run)

Written after observing. Captures are numbered from `101-` in the evidence directory (`snaps.log`, `07-inputs.log`); "trip pixels"
are `pix.py` counts in the map box `[0,250][1080,1950]`, clusters from `flags.py`. All on the S22, build `1.0.1457+gb358a4aa`.

**R-0, first capture (`101-r-first`, 01:07:34Z): my prediction was wrong.** I predicted `region` null (the app "restarted", pid 12298
not 4821). It was **set**: the bar reads "August · 5 mi", and both flags drew (4350 px of `#9553A4`, two clusters of 2175 px,
19.9 x 27.7 dp, at `[590,1118][645,1195]` and `[590,1403][645,1480]`). The premise was wrong, not the app: **pid 12298 is the earlier
run's own phase-1 relaunch** (`ps` start 17:34:53 PDT = 00:34:53Z; the first logcat lines for it are at 17:34:55), so the phase 2
search ran in it and `region` was still set. The camera had been moved about 54 px right and 237 px up from phase 2's view (the
flags' x is 590 not 536), so the points below are read from these captures, not carried over. The A point is the bottom-left of the
upper cluster.

| # | Case | Capture | Trip pixels | Verdict |
|---|---|---|---|---|
| R-0 | first state, `region` set | `101-r-first` | `#9553A4` 4350, `#FA01DD` 0 | both **draw** (prediction on `region` wrong; draw predicted after a search) |
| 2-2 | switch **off** (01:08:09Z; row `checked` true → false) | `104-r-trips-off` | 0 / 0 | **absent**, as predicted |
| 2-2 | switch **on** (01:08:25Z; false → true) | `106-r-trips-on` | 4350 / 0, same clusters as `101-` | **both back**, as predicted |
| 2-3 | Night Maps **on** (`setnight.sh`, `night_mode.maps = True`) | `110-r-night` | `#9553A4` 0, **`#FA01DD` 4350** | **both draw in the night colour**, two clusters of 2175 px at the same bounds, as predicted |
| 2-3 | day again (`night_mode.maps = False`) | `113-r-day-again` | 4350 / 0 | **both back in `#9553A4`**, as predicted |

`map_preferences` after the toggles: `map.layer.planned-trips-layer.visible = True`, `night_mode.maps = False`
(`109-prefs-after-toggle.txt`, `setnight.sh`'s own read).

**2-4, a real tap on each flag (day).** `input tap` at (600, 1160) (A's flag body, above its foot at y 1195) and (600, 1445) (B's, foot 1480):

| Touch | Capture | Bubble in the dump | Verdict |
|---|---|---|---|
| A (600, 1160) | `116-r-tap-A` | "Trip 1", "Sep 28", an MGRS, decimal degrees, "Directions" | **bubble opens**, as predicted |
| B (600, 1445) | `116-r-tap-B` | "Trip 2", "Sep 30", an MGRS, decimal degrees, "Directions" | **bubble opens**, as predicted |

The MGRS and decimal-degree lines equal the Trip Planner's and `trips-created.txt`'s for each trip (`117-tap-*-texts.txt`; the
values are not copied here, under `-04`'s coordinate rule). Each bubble was closed with Back; the dump after (`118-r-after-bubbles`)
is byte-identical in hash (`c6c7de3f…`) to `101-r-first`'s, so nothing else changed.

**2-5b, force-stop and relaunch.** Crash buffer 0 bytes before (`119-`). `am force-stop` at 01:09:58Z, pid 12298 before, none after
(`120-`); `am start -W` `Status: ok`, `LaunchState: COLD` (`121-`).
- `122-r-relaunch` (01:10:06Z): the bar reads **"September · Search a location"** (`region` null); trip pixels **0 / 0**. As predicted.
- The Trip Planner (`125-r-trip-planner`, opened from Tools; it starts collapsed) lists "Trip 1"/"Sep 28" and "Trip 2"/"Sep 30" and
  **"Choose a region in search options to see rain-driven trip windows."** (the same predicate as the map's gate, ATWU:61-68).
- The keyboard came up on the first touch of the bar (`127-`); this time I read the screenshot, lowered it with Back
  (`mInputShown=false`), opened "Recent searches" (`128-`), and touched the "Fungi · August" card at (500, 1265) (01:11:0xZ).
  No prompt appeared.
- `129-r-after-search` (01:11:17Z, 8 s after): bar "August · 5 mi"; **both flags draw**: `#9553A4` 4350 px, clusters
  `[536,1354][591,1431]` and `[536,1590][591,1667]`, **the same bounds phase 2 recorded** (`66-p2-after-search`), 2175 px each.
  **Verdict: as predicted: absent at every cold start until a search runs, then both draw.**

**Logcat** (`133-logcat-full.txt`, `logcat -d`, never cleared; crash buffer 0 bytes at `132-`). **No Forager line about planned trips**
(no "Couldn't load planned trips."; the string "planned" does not occur), as predicted. Two lines outside the question:
1. At each cold start, `AvailabilityViewModel: Couldn't read offline regions.` with a `MapLibreConfigurationException` ("Using
   MapView requires calling MapLibre.getInstance…"), from `loadOfflineRegions` at `AvailabilityViewModel.kt:922-923` via the
   ViewModel's `init` (:167), seen for pid 12298 (17:34:55) and pid 18843 (18:09:59). That is the offline-regions start-up path
   `-112`'s planner note says a pulse is reading; I add only that it is present at both cold starts I can see.
2. `Mbgl-NativeMapView: You're calling getMetersPerPixelAtLatitude after the MapView was destroyed` **2466 times** across pids 4821,
   12298 and 18843 (first 17:32:50, last 18:11:33). Not trips-related; I did not investigate it (flag).

**Skipped, as pre-registered:** 2-1z and 2-5a.

## Resumed: step 3, the deletes, and step 4, the restore

**Deleted through the Trip Planner** (Tools drawer → Trip Planner → each row's "Remove planned trip for <date>" button, real taps,
`07-inputs.log`). Before (`141-d-planner-before`): **exactly two rows**, "Trip 1" (2026-09-28) and "Trip 2" (2026-09-30), matching the ids
`5deff079-1dba-440e-b947-46e53b0e18d3` and `47b2784e-6ba8-46cd-96fc-f76bee22ce15` above by name, date and the coordinates in `trips-created.txt`
(the UI shows no id; identification is by name, date and coordinates, inferred). Remove on 2026-09-28 at 01:12:3xZ (`142-`: only Trip 2 left); remove on
2026-09-30 at 01:12:4xZ (`143-`: "No trips planned yet."). No confirmation dialog appeared; no other row was touched. The map afterwards
(`144-d-map-after-delete`) has 0 trip pixels; the find, waypoint and regions were untouched.

**Read-back, from a database copy after a force-stop** (`146-force-stop-end.txt`: pid 18843 before, none after, 01:13:02Z; `db-end-*`): device
and local sha256 match for all three files; **`forager.db` is byte-identical to the start copy** (`e88effc0…61af7c93`); header valid, WAL magic valid;
**`integrity_check` ok; `user_version` 16; `planned_trips` 0** (the starting count, `db-start-verify.txt`); the other 17 tables' row counts equal the start's.
Per-table digests over every column (`db-end-digest.txt` against `db-start-digest.txt`, `diff`): **17 of 18 identical; only `cached_searches` differs**
(2 rows both times; the timestamps and possibly cached results the "Fungi · August" re-runs rewrote, as pre-registered and as phase 2 reported).
I did not open the raw copy.

**Settings restored and read back** (`151-final-read.txt`, 01:13:25Z; `152-datastore-compare.txt`): the two things I changed in the app, the Planned trips switch
(`map.layer.planned-trips-layer.visible = True`) and Night Maps (`night_mode.maps = False`), read back from `map_preferences`; **all five DataStore files
are byte-identical (`cmp`) to the start copies** (`05-*-start.pb`). System: `user_rotation` 0, `accelerometer_rotation` 0, `font_scale` 1.0, `screen_off_timeout` 600000,
`navigation_mode` 0, `location_mode` 3, `display_density_forced` 450, `cmd uimode night` yes: all as read at 01:06Z and at the run's start; none was changed. Forager
relaunched with `am start` (COLD) and in focus, on the Maps tab, crash buffer 0 bytes. `region` is null again on that cold start (the Trip Planner line was not re-read; inferred, AUS:30).

## Resumed: verdicts

Phases 1 and 2 (above) plus this run give, for the two trips on the compact tree (S22, `1.0.1457+gb358a4aa`):

| Condition | Result | Evidence |
|---|---|---|
| Trips saved and listed, **`region` null** (cold start, no search) | **never drawn**, at any zoom tried, switch on/off, day/night, after a tab trip and a relaunch; no bubble | phase 1: 0 trip pixels in every capture `20-`…`55-`; resumed `122-r-relaunch` |
| Same trips, **after a recent search sets `region`** | **both draw** at their points, 2175 px each, 19.9 x 27.7 dp | `66-p2-after-search`; `101-r-first`; `129-r-after-search` (same bounds as `66-`) |
| Switch off / on, with `region` set | gone / both back | `104-`, `106-` |
| Day / night, with `region` set | `#9553A4` / `#FA01DD`, same clusters | `113-`, `110-` |
| Tap on each flag, `region` set | bubble with name, date, MGRS, decimal degrees, "Directions" | `116-r-tap-A`, `-B` |
| Force-stop and relaunch | absent, bar "Search a location"; **both draw again after the search is re-run** | `122-`, `129-` |

**Predictions:** every pre-registered row held **except R-0** (I predicted `region` null on first capture; it was set, because the pid I took for a restart was the earlier run's own
relaunch). That was my wrong premise, not a behaviour of the app; the gate itself behaved as predicted at every point where I could read it.

**The cause the evidence supports.** The compact Maps tab hands the map an empty trip list whenever `region` is null: `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()`
(ACMU:641, `hasSearched = uiState.region != null` at :573; **read** at `b358a4a`). **Observed:** on the S22, with two trips in the database and in the Trip Planner, no flag is drawn while the bar reads
"Search a location" (`region` null), across the zoom, switch, day/night, tab and relaunch cases; the flags draw when a search sets `region`, with the camera, the trips, the layer switch and Night Maps
unchanged (phase 2 and `129-`, the same clusters twice); they vanish again at every cold start until a search is re-run. **Inferred:** that the owner's S26 report ("They do not appear at all") is this
gate, as they had three trips listed and did not report running a search (I have not seen the S26 or their state). What the evidence does **not** cover: the wide tree (the tablet's terminal `-101` did, after a
search), a trip with the switch on before any search under a different route to set `region` (manual coordinates, "Use current location"), and whether the gate was intended.

## Resumed: Decisions I made

1. **Skipped 2-1z and 2-5a**, pre-registered before observing, with the reasons above. The dispatch's step 2 lists "the layer switch, day and night, tap, and relaunch, with the search run", which I ran.
2. **Re-ran the "Fungi · August" recent search** (as phase 2 did) rather than "Use current location", for the reason in Decisions above (5): it rewrites one existing `cached_searches` row instead of adding one. The digest shows only that table changed.
3. **Read screenshots for the search step**, after the keyboard was up (the earlier run's stray touch); lowered the keyboard with Back, as `-84` and the earlier run did. No prompt appeared.
4. **Identified the trips to delete by name, date and coordinates**, since the planner shows no id; there were exactly two rows.
5. **Used `am start` at the end** so Forager is in focus as at the handoff. The prior state (Forager focused) is what the dispatch describes; the app was in a cold start with `region` null.
6. **Ran the D58 check** as a grep over the diff and the commit message before each push (the phrases were read from forager-forecast `origin/d55-artifact-contract`'s `DECISIONS.md` row D58 with `git show`, no checkout); none found. The phrases are not written in this file.
7. **No record entry, no `RECORD.md`, index, `CLAUDE.md`, `docs/plans/` or `prompts/` touched**; merge not done.

## Resumed: Flags outside scope

1. **`AvailabilityViewModel: Couldn't read offline regions.`** (a `MapLibreConfigurationException`, `loadOfflineRegions`, `AvailabilityViewModel.kt:922-923` via `init` :167) at every cold start I can see in logcat (pids 12298, 18843). This is the path the planner's pulse is reading.
2. **`Mbgl-NativeMapView: … getMetersPerPixelAtLatitude after the MapView was destroyed`, 2466 lines** across three processes (17:32:50 to 18:11:33), including while the Maps tab was on screen. Unexplained; not investigated.
3. **A uiautomator dump cannot show the flags:** the dump hash of the map with the flags drawn (`101-`) equals the hash with them deleted (`144-`); only the screenshot differs. Any future check of this feature that reads dumps only would pass in both states (`snaps.log`).
4. **The Trip Planner starts collapsed each time the drawer opens**; a first tap in this run did not expand it (`140-`), the second did (`141-`). Not investigated (possibly the tap landed before the drawer settled).
5. **The model that served this session is unverified** (see the pre-registration).
6. **Screenshots and dumps of this run contain the owner's real map area and the trips' coordinates**; they stay outside the repository.
