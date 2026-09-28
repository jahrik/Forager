# Missing planned-trip glyphs, reproduced on the owner's tablet (SM-X800): run record

**Status: pre-registration only.** No Forager screen has been opened in this run, and no trip exists. This section is
committed and pushed before any observation, so the order of prediction and observation can be checked. Observations
are added in later commits; this section stays unchanged.

**Date:** 2026-09-28, from 23:28Z.
**Dispatch:** `prompts/preserved/2026-09-28-95.md` (sha256 `cfa98c46…12e2730`, 3163 bytes), which applies the method of
`prompts/preserved/2026-09-28-94.md` (sha256 `ba0b8144…fe27680`, 4562 bytes). Both are quoted verbatim in the Appendix.
The intent `2026-09-28-95` and every `RECORD.md` entry are the planner's; I write none.
**Base:** `origin/journal-redesign` at `554449b`, confirmed with `git fetch` at the start (the planner's store-copy
commit for `-95`). Branch `device-trips-tablet`, worktree `/home/zynergy-labs/Zynergy/forager-wt/device-trips-tablet`,
cut from `554449b`.
**Code citations are at `d7cc9f5`**, the installed build's commit (`versionName` `1.0.1416+gd7cc9f5b`), not at the base:
`git diff d7cc9f5 554449b -- app/` is not empty (32 files). Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.
Abbreviations: AWL `ui/availability/AvailabilityWideLayoutUi.kt`, AS `ui/availability/AvailabilityScreen.kt`, AVM
`ui/availability/AvailabilityViewModel.kt`, AMOU `ui/availability/AvailabilityMapOverlaysUi.kt`, ATWU
`ui/availability/AvailabilityTripsWaypointsUi.kt`, SM `ui/map/SightingsMap.kt`, ML `ui/map/layers/MapLayers.kt`, MLS
`ui/map/layers/MapLayerState.kt`, MG `ui/map/MarkerGlyphs.kt`, MB `ui/map/MapBubbles.kt`.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips-tablet/`, outside the repository.
Under `-04`'s privacy rule (carried by `-51` and `-72`, which `-94` applies), no screenshot, dump, coordinate, place name
or photo goes into this file. The trips' coordinates, which `-94` asks me to record, are recorded in the evidence
directory and named here (Decisions I made, 2).
**The planned-trips pulse's filing** is not filed yet (launch message). These predictions are from my own reading of
the code. If its findings arrive, they are recorded in a later section and compared; this one is not edited.

## The launch message, verbatim

> This is planner dispatch `2026-09-28-95`: **reproduce the missing planned-trip glyphs on the owner's tablet.** Its store copy is committed at `prompts/preserved/2026-09-28-95.md` on `origin/journal-redesign` at `554449b`. It builds on `prompts/preserved/2026-09-28-94.md`, the S22 version. Read both in full and quote `-95` verbatim in your run record; the files govern over this message.
>
> **The pulse is not filed yet.** The dispatch names a planned-trips pulse filing, but that pulse is still running. Pre-register from your own reading of the code, with file:line: how saved planned trips reach the wide map (`AvailabilityWideLayoutUi.kt`), any filters on the way, the layer and glyph, and the Layers switch. If the pulse's findings arrive while you work, the planner will forward them.
>
> The essentials, stated in full in the files:
> - **The tablet only:** `-s R52T506412L` on every adb command. **Never touch the S22 `R5CT321008R`.**
> - **No install.** Confirm `1.0.1416+gd7cc9f5b` first.
> - **Stop at any system prompt** without touching it, and hand back naming it; the owner taps through. Leave "Stay Awake", the 5-minute timeout, the `CAMERA` grant and the existing DEVICE CHECK items alone.
> - **Create at most three trips** through the real UI (today, a few days ahead, and past if the dialog allows). The wide route is the Add button, then the three-way dialog, then Plan a trip, then the date dialog. Record their ids.
> - **Check each trip:** whether its glyph draws, in landscape first and then portrait (where the 103.5 dp strip may be too narrow), zoomed in and out, with the Planned trips layer on and off, by day and at night, on tap, and after a tab round trip and a relaunch. The wide map needs a search first: re-run the recent one. **Fix nothing.** Name the cause the evidence supports, marked as read, observed or inferred.
> - **Delete exactly your trips.** Afterwards, read `planned_trips` back from a database copy taken after a force-stop.
> - **Where things go:** evidence in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips-tablet/`. The run record is `docs/audits/2026-09-28-planned-trips-tablet-run-record.md` on `device-trips-tablet`, pushed after each check. The planner merges it; you do not touch `RECORD.md`. Merge is not authorised.
>
> When you finish or stop, hand back a report: the build, the trips created and deleted, every verdict, captures by name, logcat lines, the supported cause, what was restored, decisions you made and flags.

## Structural check of the dispatch

Structure only; this cannot detect a decision I was never told about.
- **Role:** present (`-95` Role). **Base:** the branch is named as "from `origin/journal-redesign`"; the launch message
  names `554449b`. **Scope boundary:** the tablet only, at most three trips, fix nothing, leave the owner's settings and
  the DEVICE CHECK items alone. **Checks and finish line:** `-94`'s checks 1 to 5 as modified by `-95`; the finish line is
  in `RECORD.md`'s intent `-95` ("The run record pushed and trips deleted and read back"). **Abort conditions:** present.
  **Merge:** "Not authorised."
- `-95`'s "Read first" names the pulse's filing, which does not exist yet; the launch message covers that gap.

## The device and its state (read before any Forager screen)

- **Attached** (`00-devices.txt`, 23:28:01Z): `R52T506412L` (`model:SM_X800`, `product:gts8pwifixx`) and `R5CT321008R`
  (the S22; listed, never addressed). Every other adb call names `-s R52T506412L`; the helper scripts name no other serial.
- **Identity** (`00-device-facts.txt`): `ro.product.model=SM-X800`, samsung, `gts8pwifixx`/`gts8pwifi`, fingerprint
  `samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZE1:user/release-keys`, Android 16, SDK 36;
  `wm size` 1752 x 2800 and `wm density` 340, physical, no override (1 dp = 2.125 px). Tablet clock
  `Mon Sep 28 16:28:02 PDT 2026` (UTC−7), so **the tablet's "today" is 2026-09-28** for the whole run unless it passes
  07:00Z.
- **Installed build** (`01-dumpsys-package-start.txt`): `versionName=1.0.1416+gd7cc9f5b`, `versionCode=1416`,
  `firstInstallTime=2026-09-28 04:30:35`, `lastUpdateTime=2026-09-28 14:06:59`, `DEBUGGABLE`. **The expected build.
  Nothing is installed in this run.** `CAMERA` `granted=true` (`USER_SET`, the owner's); location, media location and
  notifications `granted=false`.
- **State:** `isKeyguardShowing=false`, `mWakefulness=Awake`, the launcher focused, no prompt, `ROTATION_90`. Forager not
  running (no pid).
- **Settings** (`03-settings-start.txt`): `accelerometer_rotation=1`, `user_rotation=1`, `font_scale=1.0`,
  `display_density_forced=null`, `navigation_mode=2`, `window_animation_scale=1.0`; `stay_on_while_plugged_in=15` and
  `screen_off_timeout=300000`, **both the owner's, not touched**.
- **Crash buffer** (`02-crash-start.txt`): 19 lines, 2 FATAL EXCEPTIONs (the 08-10 `com.mgoogle` ones), 0 Forager lines;
  sha256 `72bd6b5b5e9a70ea`, byte-identical to the sanity check's reads.
- **Database at the start** (`db-start-*`; the app was not running, so no force-stop was needed): device and local sha256
  match; header valid; `integrity_check` `ok`; `user_version` 16. Rows: `cached_searches` 1, `cartography_entries` 1,
  `cartography_entry_find_refs` 1, `cartography_entry_waypoint_refs` 1, `log_entry_photos` 1, `log_photos` 1,
  `mushroom_log_entries` 1, `waypoints` 1 (the DEVICE CHECK items), `android_metadata` 1, `room_master_table` 1, and
  **`planned_trips` 0**. Every other table 0. Per-table digests in `db-start-digest.txt`.
- **Preferences:** no `files/datastore/` directory exists (`04-app-files-start.txt`), so every DataStore preference is at
  its default. For this run that means the **Planned trips switch is on** (`MapLayersState.DEFAULT`, MLS:14, 38, 41) and
  **Night Maps is off** (`DataStoreMapPreferencesRepository.kt:75`, `?: false`).

## Premises checked

1. **"The tablet takes the wide tree."** The branch is AS:1755 (`COMPACT || isShortWindow` takes the compact tree). The
   sanity check measured wide in both orientations at this build. To be seen again on screen.
2. **"The wide map shows only after a search has run (AWL:228-231)."** At `d7cc9f5` the gate is AWL:229-233,
   `!uiState.hasSearched`, and `hasSearched` is `region != null` (`AvailabilityUiState.kt:310`). Loading and a sightings
   error also replace the map (AWL:235-247). Confirmed from the code.
3. **"The Add button, then the three-way dialog, then Plan a trip, then the date dialog."** A step is missing:
   between "Plan a trip" and the date dialog comes the **centre-pin picker** (AWL:381-398). Its confirm stores the
   camera centre as the trip's location (AWL:385), and only then does the date dialog open (AWL:431-440). **The premise
   is incomplete, not wrong.**
4. **"Past if the dialog allows."** It will not, by the code: `TripDatePickerDialog` marks every date before today
   unselectable (AMOU:122-127), and `SavePlannedTripUseCase` rejects a past date independently (`domain/SavePlannedTripUseCase.kt:27-29`).
   So at most two trips can be created through the real UI. To be observed in the dialog.
5. **"The app names them 'Trip N'."** `defaultTripName(n) = "Trip ${n + 1}"` (`ui/availability/AvailabilityPureFunctions.kt:125`),
   from the trip count in state (AWL:433).
6. **"The planner's own delete"** (`-94`). The Trip Planner is a collapsible section of the drawer's Search panel
   (`ui/availability/AvailabilitySearchUi.kt:713-714`); each row's delete is an `IconButton` described
   "Remove planned trip for <date>" that deletes at once, with no undo (ATWU:151-153, AVM:839-850).
7. **`planned_trips` 0 at the sanity check's copy:** confirmed, and 0 again at my start copy.

## How a saved trip reaches the wide map (read, at `d7cc9f5`)

1. **Created:** the map's Add button (AWL:374-380) opens `ThreeWayActionDialog` (AWL:413-429, 462-481); "Plan a trip"
   sets `pendingAction = PLAN_TRIP` (AWL:415-418); the centre-pin picker's confirm sets `pendingTripLocation =
   cameraCenter` (AWL:385); `TripDatePickerDialog` (AWL:431-440; AMOU:115-159), "Plan trip", calls `onPlaceTripPin`
   (AS:1587 → `MainActivity.kt:436` → AVM:825-837), which saves through `SavePlannedTripUseCase` and on success reloads
   (AVM:828).
2. **Loaded:** `loadPlannedTrips` (AVM:810-822) puts `GetPlannedTripsUseCase`'s result into `uiState.plannedTrips`. That
   use case **filters nothing**: it sorts today's trips first, then by date (`domain/GetPlannedTripsUseCase.kt:20-25`).
   It also runs once at start-up, independent of any search (AVM:159-162). A load failure logs
   `AvailabilityViewModel` "Couldn't load planned trips." (AVM:815) and leaves the list as it was.
3. **Handed to the wide map:** `CombinedResultsPane` (AS:1577-1596) → `MapTab` (AWL:134-150) passes
   `plannedTrips = uiState.plannedTrips` **unfiltered** into `MapOverlayContent` (AWL:281). The only conditions are the
   ones on the map itself: a region (AWL:229-233, 251), not loading (AWL:235), no sightings error (AWL:243). **The taxon
   filter touches sightings only** (AWL:264-268).
   - For contrast, the compact Maps tab passes `if (hasSearched) uiState.plannedTrips else emptyList()`
     (`ui/availability/AvailabilityCompactMapUi.kt:640`). That is `-94`'s tree; noted here, not tested.
4. **Into MapLibre:** `SightingsMapSlot` forwards `content.plannedTrips` (`ui/map/MapSlot.kt:464-468`). `SightingsMap`'s
   data effect is keyed on `plannedTrips` and on `loadedStyle` (SM:623-632), and `refreshOverlayData` sets the
   `planned-trips` source to one GeoJSON point per trip, with no filter (SM:1030, 1530-1535).
5. **The layer and glyph:** `initializeOverlayLayers` registers every `MarkerIcon` image in the current palette (SM:867)
   and adds one native layer per registry entry (SM:870-881). `planned-trips-layer` is a `SymbolLayer` with
   `icon-image` `planned-trip-flag`, `icon-allow-overlap` true and `icon-anchor` center (SM:926-928, 953-958;
   MG:231). The flag is a 3 dp pole and a 17 x 12 dp pennant, 20 x 28 dp, its pole foot on the trip's point (MG:41-42,
   117-120), filled `#9553A4` by day and `#FA01DD` at night (`ui/theme/MapPalette.kt:83, 104`), cased white by day and
   black at night (`MapPalette.kt:95, 125`), at icon opacity 1 (ML:180). It sits above the sighting dots and below the
   waypoint, find and photo glyphs (ML:333-356). No `minzoom`/`maxzoom` is set on it (SM:953-958).
6. **The Layers switch:** "Planned trips" is in `MAPS_TAB_OVERLAYS` (`ui/map/MapLayersSheet.kt:137`), which the wide
   Layers sheet lists (AWL:343-354). Its change goes to `onMapLayerVisibilityChanged` (AVM:501-503) into
   `uiState.mapLayers`, then `drawnMapLayers` (AS:899) into `MapRenderMode.layers` (AS:942-948). `SightingsMap` sets
   each layer's `visibility` from `layerPaintFor` (MLS:64-74) at build (SM:879) and on every change (SM:673-676). No
   style reload is involved.
7. **Tap:** a tap queries the tappable layers under the point, then in a box (SM:400-411); the trip layer's tap group is
   MARKER (ML:194, 350). A hit becomes `MapFeatureTap` (SM:428-436) → `tappedThingOf` (MB:98-101) → the bubble, whose
   content comes from `bubbleSources.plannedTrips` (AS:1224; MB:244-253): the trip's name, date, MGRS, coordinates and a
   "Directions" action (`ui/map/MapBubble.kt:411-417`). A hidden layer is left out of the query (`tappableLayerIds` on
   the drawn state, SM:401-402; the hiding itself is inferred from L0a's report, not re-read).
8. **Night:** Night Maps (Settings, `ui/availability/AvailabilitySettingsUi.kt:427`) reloads the style; the new style
   gets the night palette's images and empty sources (SM:859-882), and the data effect, keyed on `loadedStyle`, refills
   them (SM:623-632).
9. **Rotation:** `MainActivity` handles orientation and size changes itself (`AndroidManifest.xml:160`), and both
   orientations take the wide tree, so the same map view is resized, not rebuilt. **Inferred:** MapLibre keeps the
   camera's centre and zoom across the resize, so the portrait strip shows the middle 103.5 dp of the landscape view.

**Where the code could drop a trip on this tree, if anywhere:** only by the map not being shown (no region, loading,
error), a failed load (a logged warning), the switch off, or the trip lying outside the visible ground. **I found no
date filter and no search filter on the wide path.**

## Predictions and pass conditions, written before observing

The trips: **A**, dated today (2026-09-28), and **B**, dated 2026-09-30 (the owner's future date); **C**, a past date,
is predicted not creatable. Each is placed at the centre pin after a short vertical pan, so it sits clear of the
existing DEVICE CHECK waypoint and find and of the search reticle, and stays within the portrait strip's width
(Decisions I made, 3).

**Measurement.** Each case gets a screenshot and a uiautomator dump (`snap.sh`). A glyph "draws" when I see the flag
in the screenshot at the trip's place, **and** `pix.py` counts pixels of the trip's fill colour (±16 per channel) in the
map's bounds that were absent before the trip was created. `pix.py` is run on a capture taken before any trip, as the
baseline for the basemap's own pixels in those colours. A glyph is "absent" when neither holds.

| # | Case | Prediction | Pass (the glyph draws) when |
|---|---|---|---|
| 0 | Past date in the dialog | **Not selectable**; C is not created (AMOU:125-127) | the past day's cell is disabled in the dump, or a past date cannot be confirmed |
| 1 | Landscape, just after creating A, then B, at the placing zoom | **A and B draw**, a flag with its pole foot at the centre pin's point (AWL:281; SM:1030) | flag seen, and a new cluster of `#9553A4` pixels at the pin's point |
| 1z | Landscape, zoomed in (double-tap) and zoomed out | **Both draw at every zoom** (no minzoom, `icon-allow-overlap` true, SM:955) | the same, at each zoom, while the trip's point is on screen |
| 2 | Landscape, Planned trips **off**, then **on** | **Off: no flag; on: flags back**, with the map not reloading (SM:673-676) | off: 0 trip-colour pixels beyond the baseline; on: the cluster back |
| 3 | Landscape at night (Night Maps on), then day | **Both draw**, in `#FA01DD` at night (`MapPalette.kt:104`) | flag seen and `#FA01DD` pixels at the trip's point |
| 4 | Landscape, tap on each flag | **A bubble opens**: "Trip N", the date, MGRS, decimal degrees, "Directions" (`MapBubble.kt:411-417`) | the bubble's text in the dump names the trip |
| 5a | Landscape, List | Maps is the same pane on the wide tree (AS:1576-1577); **Seasonal and back** rebuilds the map: **both draw** | as 1 |
| 5b | `am force-stop`, `am start`, recent search re-run | **Both draw** (AVM:159-162 reload at start) | as 1 |
| P1 | Portrait (rotation 0), 103.5 dp strip | **A trip whose point lies inside the strip draws.** The strip keeps the centre (inferred, item 9 above), so A and B, placed near the centre's longitude, are predicted **inside and drawn**. Width alone hides only a trip outside the strip, or one under the Layers or Add button | flag seen in the strip, with trip-colour pixels |
| P2 | Portrait, zoom in and out, switch off and on, night, tap | **As in landscape.** The bubble is cut off at the screen's right edge, as the sanity check saw for a waypoint | as 1 to 4 |

**Planner's outcome predictions** (`RECORD.md`, intent `-95`): (1) "The wide map draws trips in landscape; the S26
symptom is specific to the compact tree." (2) "In portrait the 103.5 dp strip may show none, for width alone." My
mechanism prediction agrees with (1) for the tablet. On (2), I predict the strip shows a trip that lies inside it, so
any trip missing in portrait is missing because its point is outside the strip or under a control, not because of a
filter.

**If a glyph is missing** where predicted drawn, I record which conditions hold, capture it, read `logcat -d` for
`AvailabilityViewModel`, `SightingsMap` and MapLibre lines, and do not fix anything.

## Method

- `snap.sh NAME` saves `NAME.xml` (`uiautomator dump`) and `NAME.png` (`screencap`) and logs time, `user_rotation` and
  hashes to `snaps.log`. **It refuses to capture unless Forager owns the focused window**, so a prompt is never captured
  (the sanity check's v2 guard, copied unchanged apart from the evidence path).
- `dbcopy.sh`, `dbverify.sh` and `dbdigest.sh`: copy only with the app stopped; the raw copy is never opened.
- `pix.py PNG x0 y0 x1 y1 [HEX…]`: counts pixels within ±16 per channel of each colour in a box.
- **Touches are real:** `adb -s R52T506412L shell input tap/swipe` at coordinates from a fresh dump. Zoom in by
  double-tap; zoom out by MapLibre's double-tap-and-drag (`input motionevent`), if the timing works over adb. If it does
  not, I say so.
- **Rotation:** `accelerometer_rotation 0` and `user_rotation 0/1`, restored to `1`/`1` and read back.
- **Settings in the app I change and restore:** the Planned trips switch (back on) and Night Maps (back off). Their
  DataStore file does not exist at the start, so restoring leaves a file holding the default values (Decisions I made, 4).
- **Logcat:** `logcat -d` only; never `-c`. Launch with `am start`, never `monkey`.
- **D58:** before each push, `/tmp/trips-tablet/d58.sh` checks the diff since `554449b`, uncommitted files and commit
  messages for the three phrases D58 forbids. It reads them at run time from forager-forecast `origin/main`
  `docs/planning/DECISIONS.md` row D58, so they are never written to disk. Positive control: 1. First run: 0 hits.

## Decisions made before observing (carried into the final "Decisions I made")

1. **Where D58's phrases come from.** Neither `-95` nor `-94` names where D58 is defined. Earlier reports at this base
   (`docs/audits/2026-09-28-j8-follow-ups-completion-report.md:520-521`) name forager-forecast's
   `docs/planning/DECISIONS.md` D58, and I used that.
2. **Trip coordinates go in the evidence directory, not here.** `-94` says "Record each trip's id, date, coordinates and
   name"; `-04`'s rule, which `-94` carries through `-72` and `-51`, keeps coordinates out of the repository. Both are
   met by recording them in `trips-created.txt` in the evidence directory. Deciding which rule governs, if they were
   meant to conflict, would need the planner.
3. **Placement and dates.** A short vertical pan before each placement, so each trip is clear of the existing DEVICE
   CHECK waypoint (placed at the search centre by the sanity check), the find and the reticle, and stays inside the
   portrait strip's width. Dates: today (2026-09-28) and **2026-09-30**, the owner's future date, for "a few days ahead".
4. **Restoring the two in-app settings leaves a DataStore file behind.** No preference file exists at the start;
   switching Planned trips off and on and Night Maps on and off writes the defaults into one. The values read back as
   the start's; the file's existence is new.
5. **Worktree:** a new one at `/home/zynergy-labs/Zynergy/forager-wt/device-trips-tablet`, as the earlier device
   checks did, rather than my session's own worktree, which is on another branch.
6. **No force-stop at the start:** Forager was not running, so the start copy needed none.
7. **An objective glyph measure** (`pix.py`) alongside my reading of each screenshot. The dispatch asks for screenshots
   and dumps only.

## The planned-trips pulse's findings (received after the pre-registration was pushed in `e5cb48a`)

Forwarded by the planner at about 23:4xZ, before any Forager screen was opened. Appended as written; the
pre-registration above is not edited. No planner-log line was given with it.

### The planner's message, verbatim

> The planned-trips pulse is back. Its findings, read at `a0a54f9`, are for your pre-registration. If you have already pushed your predictions, append these as written after the pulse; do not edit what you pushed.
>
> - **Compact tree:** planned trips are handed to the map only after a search has set a region: `AvailabilityCompactMapUi.kt:573` `val hasSearched = uiState.region != null`, and `:641` `plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList()`. Waypoints, finds, photos, tracks and regions are not gated.
> - **Wide tree:** there is no map until a search (`AvailabilityWideLayoutUi.kt:239-243`). After a search it passes `uiState.plannedTrips` with no gate (`:291`). So on your tablet, the pulse predicts trips **do** draw once the searched map is up.
> - **No other filter:** none by date, month, taxon or region radius (DAO `PlannedTripDao.kt:11`, use case `GetPlannedTripsUseCase.kt:20-25`). `SavePlannedTripUseCase.kt:31` refuses a date before today, so your past trip may not be creatable. Record it if so.
> - **The layer:** `planned-trips-layer`, in the MARKERS group, a flag glyph (`MarkerGlyphs.kt:231`), day `#9553A4` and night `#FA01DD`. It has no zoom limit, is on by default, and the switch is wired (`MapLayersSheet.kt:137` to `SightingsMap.kt:968`).
> - **Camera:** it opens at zoom 12 for the default 8 km radius (`SightingsMap.kt:1809-1814`), so a trip may sit outside the opening view. Pan or zoom out before calling one missing.
>
> Carry on. The tablet only, and fix nothing.

### The pulse against the installed build, and against my predictions

- **Its lines are at `a0a54f9`; the tablet runs `d7cc9f5`.** I read each cited line at `a0a54f9` and it says what the
  pulse says. Between `d7cc9f5` and `a0a54f9`, `AvailabilityWideLayoutUi.kt` and `SightingsMap.kt` change only map-chrome
  colours, test tags and the offline region's night border, and `AvailabilityCompactMapUi.kt` 7 lines;
  `SavePlannedTripUseCase.kt`, `GetPlannedTripsUseCase.kt` and `PlannedTripDao.kt` do not change. **So its findings hold
  for the installed build,** at shifted lines: the wide gate AWL:229-233 (pulse :239-243), the hand-over AWL:281 (pulse
  :291), the visibility setter SM:967 (pulse :968), `zoomForRadiusKm` SM:1787-1792 (pulse :1809-1814), the compact gate
  `AvailabilityCompactMapUi.kt:640` (pulse :641).
- **Agreement with my pre-registration:** the same wide path with no gate after the search, no date or other filter, no
  past trip creatable, a flag on by default with no zoom limit and a wired switch. The pulse adds the DAO's unfiltered
  query (`SELECT * FROM planned_trips`, `PlannedTripDao.kt:11`), which I had not read, and the opening zoom, which I had
  read (SM:1787-1792) but not stated.
- **A citation error of mine, found by the pulse's line.** My pre-registration cites the past-date refusal as
  `domain/SavePlannedTripUseCase.kt:27-29`. It is **:31-33** at `d7cc9f5` (and at `a0a54f9`). I had numbered that file
  after removing its import lines. No other citation in the pre-registration was numbered that way.
- **The pulse's camera note** matches my placement plan: trips are placed at the centre pin, so each is in view when
  created; "missing" is judged only with the trip's point on screen.

## Observations

Everything below was written after observing. Captures are in the evidence directory; each has a `.png` and a `.xml`
of the same name unless marked, and `snaps.log` holds each capture's time, rotation and hashes. Every input is logged
with its time in `07-inputs.log`. "Trip pixels" are `pix.py` counts of the trip's fill colour (±16 per channel).

### Corrections and a tooling fault, found before the first check

- **The pulse arrived at about 23:32Z, not "23:4xZ".** The pre-registration was committed at 23:31:55Z and the pulse
  section at 23:32:40Z. The section above is left as pushed.
- **My first capture read a stale dump.** Copying the sanity check's `snap.sh`, my `sed` renamed only the first of the
  two `/sdcard/tab-ui.xml` paths on its line. So `uiautomator dump` wrote a new file and `cat` read the sanity check's
  last dump (45867 bytes, 15:34 PDT, `rotation="0"`). Caught because `10-r1-launch.xml` listed search results that the
  screenshot did not show. That file is renamed `10-r1-launch.STALE-sanity-dump.xml` and cited for nothing. `snap.sh`
  v3 deletes its own dump file first and refuses to read unless the dump reports success; the faulty copy is kept as
  `snap.sh.v1-stale-bug`. Every dump cited below is v3's, and each one's `rotation` attribute and text agree with its
  screenshot where I compared them.
- **One unguarded read.** Opening the three-way dialog for trip B, I read one dump directly rather than through
  `snap.sh`'s focus guard. It showed Forager's own dialog and was not saved. Every other read went through `snap.sh`.

### Setup: launch and the search (landscape, rotation 90)

- Rotation locked (`accelerometer_rotation 0`, `user_rotation 1`) at 23:32:49Z (`05-rotation-log.txt`).
- `am start -W` with the launcher's own action and category at 23:32:5xZ: `Status: ok`, `LaunchState: COLD`, Forager's
  `MainActivity` focused, no prompt (`06-launch.txt`).
- **Wide tree, no region:** the drawer, "September · Search a location", and the map's "Choose a region in search options
  to see mapped sightings." (`10b-r1-launch`). Premise 2 observed.
- Recent searches, the one row "Fungi · September", "cached 1 hour ago" (`11-r1-recent-open`). **Re-run** at 23:33:5xZ;
  it read "cached just now" afterwards and the map came up at **[1532,405][2800,1720], 596.7 x 618.8 dp**, Topographical,
  by day (`12-r1-maps-pane`), the same bounds as the sanity check. The DEVICE CHECK waypoint sits at the map's centre, the
  find below right.
- **Baseline trip pixels** in the map (`13-pix-baseline-r1.txt`): `#9553A4` 2 px (basemap, at the right edge),
  `#FA01DD` 0. **Positive control:** the same count finds the DEVICE CHECK find's `#DA02AF` at 1537 px and the waypoint's
  `#350560` at 1620 px, each where the glyph is.

### Check 0: a past date (observed)

- The date dialog (`16-r1-date-dialog-A`) lists **every September date before the 28th with `enabled=false`**, the 26th
  included, and "Today, Monday, September 28, 2026" selected. A real tap on the 26th's cell changed nothing: the next
  dump is byte-identical (`17-r1-date-dialog-after-past-tap`, dump `ae1a3af994f534b9` both times).
- **Verdict: as predicted. Trip C (past) cannot be created through the real UI.** Two trips were created, not three.

### Trips created

Through the wide route with real taps: a slow vertical pan (1200 ms, no fling), the Add button, "What would you like to
do here?", "Plan a trip", the centre-pin picker's OK, the date dialog, "Plan trip".

| Trip | Name (the default) | Date | Placed at (UTC) | Captures |
|---|---|---|---|---|
| **A** | Trip 1 | 2026-09-28 (today) | 23:35:1xZ | `14-r1-three-way`, `15-r1-centre-pin`, `16-r1-date-dialog-A`, `18-r1-after-A` |
| **B** | Trip 2 | 2026-09-30 | 23:36:2xZ | `20-r1-centre-pin-B`, `21-r1-date-dialog-B`, `22-r1-date-dialog-B-sep30`, `23-r1-after-B` |

- The picker's step between "Plan a trip" and the date dialog was there, as premise 3 said (`15-r1-centre-pin`).
- The Trip Planner lists both: "Today", "Trip 1", "Sep 28", and "Trip 2", "Sep 30", each with an MGRS line and a
  "Remove planned trip for <date>" button (`24-r1-trip-planner`).
- **Ids and coordinates** come from the database copy after the relaunch check (below); the coordinates stay in the
  evidence directory.

### Check 1: landscape, just after creating each trip, at the placing zoom

| Trip | Capture | Trip pixels near its point | Seen | Verdict |
|---|---|---|---|---|
| A | `18-r1-after-A` (crop `18-r1-after-A-crop.png`, png only) | **1229 px**, bounds [2163,1004][2204,1062] = 19.8 x 27.8 dp; 2 px elsewhere in the map | a purple flag, white casing, its pole foot on the map's centre point (2166, 1062) | **draws** |
| B | `23-r1-after-B` | **1223 px** at [2163,1004][2204,1061]; A's flag **1229 px** at [2163,765][2204,823], 238 px north, the pan distance | two flags | **both draw** |

The map-wide count after B is 2452 px, the two flags and nothing else (`19-pix-r1.txt`). The flag's measured size
matches the glyph's 20 x 28 dp (MG:42). **As predicted.** Logcat is read for all checks together at the end.

## Appendix A: `prompts/preserved/2026-09-28-95.md`, verbatim

At `554449b`, whole (sha256 `cfa98c46e5f3a49f8acb8b7b7bac2f5c343a10e3bb0d8ae9b439ecf5e12e2730`):

~~~~markdown
HEAD: 36c2a46 (journal-redesign)
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: device
Preserved: 2026-09-28T23:22:59Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** device

# Role

You are the coder for **reproducing the missing planned-trip glyphs on the owner's tablet** (`R52T506412L`, `SM-X800`). Pass `-s R52T506412L` on every adb command. **Never touch the S22 `R5CT321008R`**; another coder uses it. You observe and record. **You fix nothing and change no app code.** This dispatch's intent is `2026-09-28-95`. The planner writes the record.

# The owner's words, verbatim

- "I noticed there are no day/night icons for planned trips, even though they do record in the planner. The layer is available in the map layers panel though." This came with screenshots from the owner's S26: three trips in the planner (Sep 26 past, Sep 28 today, Sep 30 future) and none on the map.
- "They do not appear at all".
- "Verify it on the S22 ultra", then "And tablet too".

# Read first

- `prompts/preserved/2026-09-28-94.md`, the S22 version of this check. **Its method, checks, data rule and report shape apply here**, with the differences below.
- The planned-trips pulse's filing named in the launch message.
- `docs/audits/2026-09-28-tablet-layout-sanity-run-record.md`, for how this tablet behaves and for its helper scripts.

# Differences from `-94`

- **The tablet takes the wide tree.** Portrait is MEDIUM (the map is a 103.5 dp strip) and landscape is EXPANDED (a 597 dp map).
  - The wide map shows only after a search has run (`AvailabilityWideLayoutUi.kt:228-231`). Re-run the tablet's recent search; that reaches iNaturalist, which is allowed here.
  - Placement on wide goes: the Add button, then the three-way dialog, then Plan a trip, then the date dialog.
  - Check in landscape first, then in portrait, where the strip may be too narrow to show a glyph at all. Record which.
- **No install.** Use the installed `1.0.1416+gd7cc9f5b` and record it.
- **Prompts (owner rule "4 A"):** stop at any system prompt without touching it, and hand back naming it. The owner taps through.
- **Leave alone:** "Stay Awake", the 5-minute timeout and the `CAMERA` grant (all the owner's settings), and the existing DEVICE CHECK items.
- **Data:**
  - create at most three trips (today, a few days ahead, and past if the dialog allows), and record their ids;
  - **delete exactly those at the end**;
  - read back from a database copy after a force-stop that `planned_trips` is at its starting count (0 in the sanity check's database copy).
- **Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips-tablet/`.
- **Report:** `docs/audits/2026-09-28-planned-trips-tablet-run-record.md` on branch `device-trips-tablet` from `origin/journal-redesign`. Push after each check; the planner merges it.

# Abort conditions

- not exactly this tablet, or a different installed build;
- a locked tablet;
- a new crash;
- a trip that cannot be deleted;
- any need to touch the owner's data or the S22.

A prompt pauses the run for the owner.

# Merge

Not authorised.
~~~~

## Appendix B: `prompts/preserved/2026-09-28-94.md`, verbatim

At `554449b`, whole (sha256 `ba0b814494044bf510642f478915f96c42bf492566c1134cf00e9a412fe27680`). `-95` applies its method, checks, data rule and report shape.

~~~~markdown
HEAD: a0a54f9 (journal-redesign) when written. **Queued until the map-chrome device check (-84) frees the S22;** the launch message names the planned-trips pulse's filing.
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: device
Preserved: 2026-09-28T23:22:21Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** device

# Role

You are the coder for **reproducing the missing planned-trip glyphs on the S22 Ultra** (`R5CT321008R`). Pass `-s R5CT321008R` on every adb command, and never touch the tablet `R52T506412L`. You observe and record. **You fix nothing and change no app code.** This dispatch's intent is `2026-09-28-94`. The planner writes the record.

# The owner's words, verbatim

- "I noticed there are no day/night icons for planned trips, even though they do record in the planner. The layer is available in the map layers panel though." This was sent with screenshots from the owner's S26, showing the Maps tab at night with no trip glyph, and the Trip Planner listing three trips:
  - Trip 1, Sep 26 (past);
  - Trip 3, Sep 28 ("Today");
  - Trip 2, Sep 30 (future).
- Asked whether the trips appear when zoomed out or panned to them: "They do not appear at all".
- "Yes from thr S26" (the phone). "Verify it on the S22 ultra".

The planner offered this check as: "the agent would add one "DEVICE CHECK" trip near the phone, see whether it draws by day and at night with the Planned trips layer on and off, then delete it."

Read first:
- `CLAUDE.md`, especially Bug fixing: see the failure before the fix, and confirm it fails for the reason expected;
- the planned-trips pulse's filing named in the launch message. It maps every condition under which a trip is or is not drawn;
- `prompts/preserved/2026-09-28-72.md`, whose device rules apply.

# Standing rules

- Never run `logcat -c`. Launch with `am start`, never `monkey`.
- **Stop at once at any system or Google prompt.**
- Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-planned-trips/`.
- **No install:** use the build already on the phone (from `-84`), and record its versionName.
- Restore every setting you change, and read it back.

# Data

- Through the app's real UI (placement, then the date dialog, then confirm), create **at most three planned trips** near the map's current view:
  - one dated **today**;
  - one dated **a few days ahead**;
  - one **in the past**, if the date dialog allows it.

  That mirrors the owner's three.
- Record each trip's id, date, coordinates and name. The app names them "Trip N"; you cannot label them "DEVICE CHECK", so identify them by id.
- **Delete exactly those trips at the end** through the planner's own delete. Then read back, from a database copy made after a force-stop, that `planned_trips` is back to its starting count (0 in the J8 check's backup).
- Touch nothing else of the owner's.

# Pre-register before creating anything

From the pulse's map and the code (file:line), write down:
- the conditions under which each of your three trips should draw;
- your prediction for each case below.

Push those before creating a trip.

# The checks

For each trip:
1. After creating it, is a glyph drawn at its location? Check at the zoom where it was placed, then zoomed in on it, then zoomed out.
2. With the Layers "Planned trips" switch off and then on.
3. By day (Topographical) and at night (Night Maps on).
4. Does tapping where it sits open its bubble?
5. After switching tabs and back, and after a force-stop and relaunch.

For every case, capture a screenshot and a uiautomator dump. Read `logcat -d` for Forager lines about trips or layers.

**If the glyph is missing,** record exactly which conditions hold. Compare them with the pulse's filters to name the cause the code predicts, and say whether the evidence confirms it. **Do not fix it.**

# Report

Write `docs/audits/2026-09-28-planned-trips-device-check-run-record.md` on branch `device-trips` from `origin/journal-redesign`. Push after each check; the planner merges it. Record:
- the build;
- the trips created and deleted, with ids;
- every verdict;
- the captures;
- the logcat lines;
- the cause the evidence supports, marked as read, observed or inferred;
- what was restored and read back;
- **Decisions I made**;
- **Flags outside scope**.

# Abort conditions

- any prompt over the app;
- a locked phone;
- a new crash;
- a trip that cannot be deleted;
- any need to touch the owner's data or the tablet.

# Merge

Not authorised.
~~~~
