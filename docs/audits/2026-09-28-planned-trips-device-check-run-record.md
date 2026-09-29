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
