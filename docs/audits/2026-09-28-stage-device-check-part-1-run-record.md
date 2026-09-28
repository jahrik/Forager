# Stage device check, Part 1 (L0b, the night offline region, track widths, search), on the S22 Ultra: run record

**Status: pre-registration only.** Nothing on the app's screen has been looked at yet. This file is committed and
pushed before any check is run, so the order of prediction and observation can be checked. Results are added in a
later commit, and this section is kept unchanged as the Appendix.

**Date:** 2026-09-28, from 17:14Z.
**Dispatch:** `prompts/preserved/2026-09-28-51.md`; the intent is `2026-09-28-51` in `RECORD.md`, the planner's. The kit
is absent at this base (no `.claude/`, no checkers), so I write no record entry. The phone, evidence, privacy and
settings rules of `2026-09-28-04.md` and `-12.md` apply in full.
**Base:** `origin/journal-redesign` at `26709b1`, the commit carrying the dispatch, confirmed with `git fetch` at the
start. Branch `device-stage-1`, worktree `/home/zynergy-labs/Zynergy/forager-wt/device-stage-1`, cut from it.
**Code citations** are at `26709b1`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in
full.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-stage-check-1/`, outside the repository. No
screenshot, dump, coordinate, note text, place name or photo goes into this file.
**Device:** SM-S908U, serial `R5CT321008R`, the only device attached (`adb devices -l`). `ro.build.id=BP2A.250605.031.A3`.
Unlocked (`isKeyguardShowing=false`). `wm size` 1080 x 2316, `wm density` 450, so 1 dp = 2.8125 px and portrait is
384 x 823.5 dp.

## Install facts (done before this pre-registration; no app screen looked at)

- **Disk before the build:** 12 GB free on `/`; memory 3 GB available.
- **Build:** `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug` in the new worktree: BUILD SUCCESSFUL in 55 s, 0 `e: `
  lines (`03-build.log`). The tree was clean, so the version carries no `.dirty`.
- **APK:** `stage1-debug.apk` (a copy of `app/build/outputs/apk/debug/app-debug.apk`), sha256
  `9bdc0f9c44a6919be56d47712c1c8ece4d5298091c4cc72be397053a56f7d735`; `versionName=1.0.1356+g26709b1c`,
  `versionCode=1356` (`aapt2 dump badging`); signer `CN=Android Debug`, certificate SHA-256
  `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`.
- **Installed before:** `versionName=1.0.1259+g75c050f7.dirty`, `versionCode=1259` (`01-dumpsys-package-start.txt`). Its
  base APK, pulled read-only (`installed-before.apk`, sha256 `4f42b8fe…eb09e92`), has the same certificate SHA-256,
  `cb2f6da5…94f1626`. **No signature mismatch.**
- **Database:** `ForagerDatabase.version = 15` at both `75c050f` and `26709b1` (`data/local/ForagerDatabase.kt:159`), and
  `git diff --stat 75c050f 26709b1` over `app/src/main/java/com/zynergylabs/forager/app/data/`, `app/build.gradle.kts` and
  `gradle/` is empty. No migration is needed.
- **Install:** `adb install -r stage1-debug.apk` at 17:17:37Z: `Success` (`04-install.txt`). No uninstall, no `-d`, no
  data clear.
- **After:** `versionName=1.0.1356+g26709b1c`, `versionCode=1356`, `lastUpdateTime` 10:17:38 phone time (PDT), and user 0's
  `firstInstallTime=2026-09-22 11:15:05`, **unchanged**, with the same `ceDataInode`/`deDataInode`
  (`05-dumpsys-package-after.txt`). User 95 (the Dual App profile the capture record flagged) is still listed
  `installed=true`, as before the install.
- **Crash buffer at the start:** `logcat -b crash -d`, 0 bytes (`02-crash-start.txt`, 17:16Z).
- **Settings at the start** (`06-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
  `navigation_mode=0` (three-button), `ui_night_mode=2` (`cmd uimode night`: yes), location on, `location_mode=3`.
  App: `map_preferences` holds `map.fullscreen=false`, `night_mode.maps=false` (Night Maps off), the offline picker's
  last pick, and **no `map.layer.*` key**, so every layer is at its default; no `debug_diagnostics_preferences` file
  exists, so the Synthetic forecast switch has never been stored (`07-`, `08-`, decoded by `pbprefs.py`).
- **The app was not running** after the install; the launcher had focus.

## Data inventory (read-only DB copy, `db-start`)

Saved entries 2, drafts 5, saved finds 2 (both located), draft finds 1, photos 3 (all located), tracks 1 (23 points; its
bounding box is about 39 x 19 m and its length about 188 m), waypoints 3, offline regions 2 (`DEVICE CHECK`, 1 km, and
`DEVICE CHECK 2026-09-28 B`, 5 km), planned trips **0**, cached searches 2. The L0a entry keeps the track, both finds, all
three waypoints and one region. **No planned trip exists and nothing is recording**, and I may create neither.

# Pre-registration: pass conditions and predictions, written before each check was looked at

Every touch is a real `adb shell input` touch at coordinates from a fresh dump or screenshot. The app is launched only
with `am start -n com.zynergylabs.forager.app/.MainActivity`. If any system or Google prompt appears, I stop without
touching it. Verdicts that rest on my reading of an image say so.

## Check 1: the Layers sheet opens, is 80% opaque, and clears the nav bar

Code: `MapLayersSheet` (`ui/map/MapLayersSheet.kt:204-266`), a `ModalBottomSheet` with `skipPartiallyExpanded = true`, its
own default insets, and `containerColor = BottomSheetDefaults.ContainerColor.copy(alpha = MAP_CHROME_OVER_MAP_ALPHA)`
(`:221`), where `MAP_CHROME_OVER_MAP_ALPHA = 0.8f` (`ui/map/MapChrome.kt:239`). Hosts: the compact cluster's Layers row
(`ui/availability/AvailabilityCompactMapUi.kt:1293`) and the entry map's Layers row, composed only in fullscreen
(`ui/log/CartographyEntryReportScreen.kt:507`, the row hidden while offline tiles are on). Its description is "Layers:
\<mode\> map. Choose the map type and overlays." (`MapLayersSheet.kt:119-120`).
- **Pass (opens):** a real touch on the Layers row opens a sheet whose dump holds "Layers", "Map type" and "Overlays":
  seven switches on the Maps tab (Finds, Photos, Waypoints, Planned trips, Recording trail, Tracks, Offline maps;
  `MAPS_TAB_OVERLAYS`, `:129-137`) and five on the entry map (`ENTRY_MAP_OVERLAYS`, `:144`: no Planned trips and no
  Recording trail). The Maps-tab row is touched at five points across its bounds.
- **Pass (80%):** the sheet's own fill, sampled from a screenshot over a map area of known colour, equals the alpha-0.8
  composite of the container colour over what is behind it (the scrim-darkened map), within a few units per channel.
  A 100%-opaque sheet would read the container colour exactly whatever lies behind it. The container colour is read
  from a patch of the sheet over a flat area and checked for variation over map detail: detail showing through is the
  sign of translucency. This is by my reading of screenshots and pixel samples.
- **Pass (insets):** in portrait, no clickable node of the sheet extends into the navigation-bar frame read from
  `dumpsys window`; at `user_rotation` 1 and 3 (the bar on the right and on the left), no clickable node lies in the bar's
  frame and the sheet's top is below the status bar. Content that does not fit scrolls.
- **Prediction:** it opens from both hosts, reads as translucent at 0.8, and clears the bar in portrait and at both
  rotations (Material's default sheet insets).

## Check 2: toggling each overlay; a hidden layer takes no tap

Code: a switch change sets `visibility` and opacity on the loaded style's own layers in a `LaunchedEffect(loadedStyle,
drawnLayersState)`, with no `setStyle` (`ui/map/SightingsMap.kt:664-673`); `setStyle` runs only when
`needsStyleReload(appliedStyle, requested)` is true (`:563`), which a layer change does not feed. Taps query each tappable
layer (`tappableLayerIds`, `ui/map/layers/TapPrecedence.kt:17-18`: finds, photos, waypoints, kept tracks, the offline
outline) with `queryRenderedFeatures` (`SightingsMap.kt:399-405`); that a layer with `visibility=none` returns no feature
there is MapLibre's behaviour, not this code's, and is exactly what the tap half checks. A feature tap opens its M1 bubble
(`:414` onward).
- **Pass (hide and show):** for each of the seven, switching it off removes its drawn glyphs or lines from the map
  (pixels at the glyph's place change to the ground's), and switching it back on restores them.
- **Pass (no style reload):** the basemap does not blank or re-tile across a toggle. **How:** a burst of screenshots
  through the toggle, and the logcat read with `-d` over the same window; the positive control is the same burst through
  a basemap change, which does reload the style. If the control's burst shows no visible reload either, the check cannot
  discriminate and I will say so rather than call it a pass.
- **Pass (no tap when hidden):** for Finds, Photos, Waypoints, Tracks and Offline maps (the outline), a tap on a drawn
  glyph or line while visible opens its bubble (the control), and the same tap with the layer hidden opens nothing: no
  bubble in the next dump.
- **Data limits, stated now:** there are no planned trips and no recording, so the Planned trips and Recording trail
  switches can be toggled but have nothing to hide; their hide-and-show half is **not observable** and will be recorded as
  such, not as a pass.
- **Prediction:** pass for the five with data; no reload seen on toggles, the control shows one.

## Check 3: the choices persist across `am force-stop`

Code: the ViewModel restores the stored choices at start (`loadMapLayerPreferences`,
`ui/availability/AvailabilityViewModel.kt:175`), from `map_preferences` keys under `map.layer.*`
(`data/repository/DataStoreMapPreferencesRepository.kt`).
- **Pass:** hide two overlays (Finds and Tracks), `am force-stop`, relaunch with `am start`: the sheet shows both off, the
  map draws neither, and `map_preferences` holds their keys. Then both are switched back on and the file read back.
- **Prediction:** pass.

## Check 4: Diagnostics, "Synthetic forecast layers" (debug build)

Code: the toggle is the Diagnostics panel's first row (`app/src/debug/java/com/zynergylabs/forager/app/ui/diagnostics/
DiagnosticsPanel.kt:198` onward), stored in `debug_diagnostics_preferences`. The two fields are `FillLayer`s in
`ZGroup.COLOUR_FIELDS`, the lowest group, below every marker, line and region (`ui/map/layers/MapLayers.kt:206-219`,
`:244`); base fill opacity 0.6 (`:222`); the slider multiplies it, live (`MapLayersSheet.kt:364-383`, and the paint
effect above). A reorder draws at the next style load (`MapLayersSheet.kt:196-197`). Below `MIN_FORECAST_ZOOM = 7.0`
nothing is requested (`ui/map/ForecastCellLayer.kt:25`, `SightingsMap.kt:689-696`). The legend chip is at `BottomEnd`,
inside `controlsPadding`, padded `end = Spacing.sm` and `bottom = renderMode.bottomInset + 32 dp`
(`AvailabilityCompactMapUi.kt:1149-1161`, `LEGEND_ATTRIBUTION_CLEARANCE`, `MapLayersSheet.kt:401`), capped at 96 dp tall
with its contents scrolling (`LEGEND_MAX_HEIGHT`, `:415`, `:451`), and the cluster's clamp stops above its live top.
MapLibre's "i" is at bottom-end (`SightingsMap.kt:513`).
- **Pass (draws, below markers):** with the switch on and the Maps tab at zoom 7 or more, cells in the two ramps
  (chanterelles `#FFD54F` to `#8D2B00`, chicken of the woods `#80DEEA` to `#003B4A`, `ui/map/layers/ColourFields.kt`)
  draw, and every marker and the track draw over them (a marker's pixels are its palette colour, not tinted by a cell).
- **Pass (opacity live):** moving a field's slider changes the cells' drawn colour at once, with the sheet still open.
- **Pass (reorder):** after a reorder, the stacking where the two fields overlap is unchanged until a basemap change, and
  changed after it.
- **Pass (legend):** the chip's bounds lie right of the cluster and do not overlap it, the rail (in landscape) or the nav,
  and lie above the "i" (its node from the dump, or its pixels), in portrait and at `user_rotation` 1 and 3. A touch
  expands it, its height is at most 96 dp (270 px), a swipe inside it scrolls its content without collapsing it, and a
  second touch collapses it.
- **Pass (cluster):** with the cluster dragged to the bottom on its right side, it stops above the chip, collapsed and
  expanded. The L0b report expects that this may **fail** on the real phone once real insets take their room; I will
  record the measured gap.
- **Pass (minimum zoom):** zoomed out below 7, no cell draws.
- **Turned off at the end** and read back (`debug_diagnostics_preferences`, and the switch in the panel).
- **Prediction:** draws and slider pass; the reorder passes at the basemap change; the legend clears the "i", the rail and
  the nav; the cluster-over-expanded-legend clamp is the likely failure (the planner's prediction 3).

## Check 5: the night offline region

Code: `MapPalette.NIGHT.offlineRegion = 0xFF202020` (`ui/theme/MapPalette.kt:111`) at `OFFLINE_REGION_FILL_OPACITY = 0.2f`
(`ui/map/layers/MapLayers.kt:153`); its outline is a dashed line on `PaletteRole.CASING`, `NIGHT.casing = #000000`, 1.5 dp
(`SightingsMap.kt`, `OFFLINE_REGION_CIRCLE_OUTLINE_WIDTH_PX = 1.5f`). The fill is in `ZGroup.AREAS`, below every line and
marker (`MapLayers.kt:244-258`).
- **With** Night Maps on (Settings), the Maps tab on region `DEVICE CHECK 2026-09-28 B`, on Topographical and Street:
  - **(a) Observation, by my reading:** the region reads darker than the ground outside, and ground detail inside stays
    legible. Pixel samples of the same ground colour just inside and just outside the edge are compared: inside should
    be about 0.8 x outside plus 0.2 x `#202020` per channel.
  - **(b) Observation, the one the dispatch singles out:** whether the dashed outline carries the edge over the darkest
    ground. I look for the darkest ground the edge crosses and say whether the dashes can be seen there, by my reading of
    a 2x crop, with the pixel contrast of dash against ground given.
  - **(c) Pass:** a marker inside the region draws in its palette colour, the same as the same kind of marker outside or
    its night palette value, since markers are above the fill.
- **Night Maps is restored** to off and read back.
- **Prediction:** darker with ground legible; the outline weak or invisible over the darkest ground (the planner's
  prediction 4); markers unchanged.

## Check 6: track widths by zoom (T1)

Code: `TRACK_WIDTH_ZOOM_STOPS` = `(11, 0.4)` and `(15, 1.0)` (`ui/map/layers/TrackWidthByZoom.kt:23`), drawn as
`interpolate(linear, zoom, …)` (`SightingsMap.kt`, `lineWidthExpression`). Kept track 6 dp (16.9 px) and casing 9 dp
(25.3 px) at zoom 15 and above; 4.2 and 6.3 dp (11.8 and 17.7 px) at 13; 2.4 and 3.6 dp (6.75 and 10.1 px) at 11 and below;
the casing always 1.5 x the line.
- **The zoom is measured, not assumed:** from the drawn diameter of an offline region's circle (1 km or 5 km radius) in
  px, zoom = log2(40075016 x cos(lat) x d_dp / (512 x 2 x r_m)), or from a known distance between two records.
- **The data limit, stated now:** the only kept track spans about 39 x 19 m. At zoom 11 that is about 1.4 x 0.7 dp, and at
  13 about 6 x 3 dp, so at those zooms the track draws as a blob and a line width cannot be measured across a clean
  segment. At 11 and 13 I will measure the blob's size and compare it with the track's extent plus the modelled casing
  width, against the extent plus the unthinned 9 dp; the difference is 5.4 dp at 11 and 2.7 dp at 13. That is a weaker
  check, said so.
- **Pass:** the line and casing widths measured at 15 or above match 6 and 9 dp within 1 px; the blob sizes at about 13
  and about 11 match the thinned model better than the unthinned one; on Topographical, Street, and at night.
- **Prediction:** pass at 15+; the blob comparison favours the thinned model at 11; at 13 it may not discriminate.

## Check 7: the compact bar with no location set

Code: `activeSearchSummary` (`ui/availability/AvailabilitySearchUi.kt:545-563`): `"Search a location"` when
`uiState.region` is null, joined after the month with `" · "`. The region is session state, set only by a search
(`AvailabilityViewModel.kt:406-407`, `:416-419`, `:792-795`) and not restored at start (`init`, `:159-175`). The install
killed the process, so no region is set at the first launch; I run no search.
- **Pass:** the bar reads exactly "September · Search a location".
- **Prediction:** pass.

## Check 8: the bar's tap, portrait and short landscape

Code (continuations `-40` to `-42` of the T1-T3 report): the tap opens `SearchDropdown` with "Advanced search" and "Enter
coordinates manually" both expanded, once per opening (`AvailabilityCompactScaffold.kt`, the `LaunchedEffect` on
`showSearchDropdown`), and scrolls once to the end (`scrollTo(maxValue)`) where the fields do not fit.
- **Portrait pass:** after one real touch on the bar, "Collapse Advanced search" and "Collapse Enter coordinates
  manually" exist, and Latitude, Longitude and "Search this location" are displayed (bounds on screen, above the keyboard
  if it is up).
- **Short landscape pass** (`user_rotation` 1): after the touch, the three fields are displayed without my scrolling, and a
  drag down inside the dropdown brings "Set on map" back into view. **Recorded:** whether the keyboard is shown after the
  tap and after the scroll (`dumpsys input_method`, `mInputShown`), and whether the field keeps focus.
- **Nothing is typed and no search is run.** The dropdown is left with Back.
- **Prediction:** portrait passes; landscape scrolls to the fields, can be scrolled back, and the keyboard is lowered by
  the programmatic scroll.

**Not run:** the wide layout, since the S22 has no wide window.

## Settings to restore

`user_rotation` 0 and `accelerometer_rotation` 0; `font_scale` 1.0 (not planned to change); Night Maps off; the basemap as
found; the Diagnostics switch off (read back); every overlay switch on (the default, read back from `map_preferences`);
the app left on the Maps tab.

# Resumed (continuation `2026-09-28-55`), written after the relaunch

Everything above this heading is the first coder's, byte for byte as pushed at `5532ff2`: the install facts, the data
inventory, and the pass conditions and predictions written before any check was looked at. I have not edited it. Its
"Status: pre-registration only" line describes that commit; the outcome is in the sections below. Anything I add to a
pass condition is added here, marked as written after the relaunch.

**Who:** a fresh device coder, replacing the first one, which died at 18:03Z on a network outage mid check 4. I started
at 18:37Z. **Record:** the kit is absent at this base, and the planner writes the record (its continuation entry
`2026-09-28-55` is in `RECORD.md` at `367c32f`). I do not touch `RECORD.md`.

## The continuation, verbatim

From `prompts/preserved/2026-09-28-55.md` at `367c32f`, below its "verbatim prompt follows" line:

> **Type:** device (continuation `2026-09-28-55` of dispatch `2026-09-28-51`)
>
> # Why you exist
>
> The device coder running dispatch `2026-09-28-51` died at 18:03Z on a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"), in the middle of check 4. The planner session that ran it died with it. You replace that coder.
>
> **What it left, checked by the planner at relaunch:**
> - Branch `device-stage-1` (worktree `/home/zynergy-labs/Zynergy/forager-wt/device-stage-1`), pushed at `5532ff2`. That commit holds `docs/audits/2026-09-28-stage-device-check-part-1-run-record.md` with the install facts and the pass conditions and predictions it wrote **before** looking. Those stand as written: do not edit them. If you need a new pass condition or prediction, append it and mark it as written after the relaunch.
> - **No verdicts were written.** It reasoned in blocks that were not saved, so its judgements are lost. What survives is its evidence: `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-stage-check-1/`, files `01` to `123`, `snaps.log`, `toggle-times.txt` and its helper scripts (`snap.sh`, `nodes.py`, `toggle.sh`, `dtap.sh`, `diffimg.py`, `alpha.py`).
> - An extract of its transcript: `prior-agent-transcript-extract.md` in that directory, with every command and its truncated output. It is model output. Use it to find which evidence file shows what; cite the evidence files, not the extract.
> - **The phone's state at relaunch** (planner's read, 18:3xZ): awake; Forager `MainActivity` in focus; `user_rotation` 3; `accelerometer_rotation` 0. From its last commands: Diagnostics "Synthetic forecast layers" was switched **on** (step 104), and the Tracks and Finds overlays were toggled during check 2. Read the full state back yourself before you touch anything. The starting values are in `06-settings-start.txt` and `07-datastore-ls-start.txt`, and those are what you restore to.
>
> # What governs
>
> Read `prompts/preserved/2026-09-28-51.md` in full. Every rule in it binds you, especially the standing rules (no `logcat -c`, `am start` not `monkey`, stop at any prompt over the app, evidence outside the repository) and the abort conditions. Quote this message verbatim in the run record, in a "Resumed" section.
>
> # What you do
>
> 1. **Before anything else,** confirm the installed build is still the one the pre-registration records: versionName, versionCode, `lastUpdateTime` and `firstInstallTime` from `dumpsys package`. Also do a crash read (`logcat -d -b crash`) covering the gap since 18:03Z. A mismatch or a new Forager crash is an abort.
> 2. **Checks 1 to 4 already run.** For each item, give a verdict from the saved evidence **only where a named evidence file shows the pass condition on its own.** Where it does not (the judgement was in the lost reasoning, or the evidence is ambiguous), re-run that item and say so. For each verdict, list whether it came from saved evidence or from a re-run, and name the files.
> 3. **Finish check 4:** a reorder applies at the next style load; cells draw nothing below the minimum zoom; then turn the Diagnostics switch **off** and read it back.
> 4. **Run checks 5 to 8** as `-51` sets them out.
> 5. **Restore and read back** every setting `-51` names, the overlay toggles, and rotation (`user_rotation` and `accelerometer_rotation` back to their starting values).
> 6. **Number new evidence from `124` onward.**
> 7. **Write the verdicts into the existing run record,** with the sections `-51` requires. Push `device-stage-1`; the planner merges it.
>
> # Merge
>
> Not authorised.

## A coordinator message received during the run, verbatim

Relayed by the coordinator at about 18:45Z. No planner-log line was given, so none is cited. I was not waiting on the
phone when it arrived, and carried on.

> The owner says: "S22 is ready for your device check." If you stopped or were waiting on the phone (locked, a prompt, anything else), read its state again now and carry on under dispatch 2026-09-28-55. If the reason you stopped still holds, report it instead. If you weren't waiting, carry on as you were. Commit and push the run record as you go.

## Step 1 of the continuation: the build and the crash read across the gap (before any touch)

- **Base:** `git fetch` at 18:36Z: `origin/device-stage-1` at `5532ff2`, the worktree clean and at the same commit.
  `origin/journal-redesign` at `367c32f`, carrying `-55`.
- **Device:** `adb devices -l` lists `R5CT321008R` (SM-S908U) only.
- **Installed build, 18:37:02Z** (`124-dumpsys-package-resume.txt`): `versionName=1.0.1356+g26709b1c`,
  `versionCode=1356`, `lastUpdateTime=2026-09-28 10:17:38` (phone time, PDT), user 0's
  `firstInstallTime=2026-09-22 11:15:05`, `ceDataInode=2495587`, `deDataInode=2494123`. **All identical** to
  `05-dumpsys-package-after.txt`, the pre-registration's install facts. Not an abort.
- **Crash read:**
  - `logcat -d -b crash`: **0 bytes** (`125-crash-resume.txt`, 18:37Z). It was 0 bytes at 17:16Z (`02-`) and at
    17:48Z (`92-`).
  - The main and system buffers (`126-log-resume.txt`, `logcat -d`) run from 12:41Z to 18:37Z, so they cover the gap
    since 18:03Z: **0** `FATAL EXCEPTION` lines. The events buffer starts at 18:05:52Z and holds no `am_crash` or
    `am_anr` line.
  - Forager's process, pid 9626, was started at 17:48:30Z by check 3's relaunch (`126-`: `Start proc 9626`) and was
    still running at 18:37Z (`pidof`). It did not die across the gap.
  - **No new Forager crash.** Not an abort.
- **Phone and app state at 18:37:42Z**, read before any touch (`127-settings-resume.txt`, `128-datastore-ls-resume.txt`,
  `129-map_prefs-resume.pb`, `130-diag-prefs-resume.pb`, decoded by `pbprefs.py` with coordinates redacted):
  - awake (`mWakefulness=Awake`); `isKeyguardShowing=false`; focus `com.zynergylabs.forager.app/.MainActivity`;
  - `user_rotation=3`, `accelerometer_rotation=0`, `font_scale=1.0`, `navigation_mode=0`, `ui_night_mode=2`,
    location on, `location_mode=3`;
  - `map_preferences`: `map.fullscreen=false`, `night_mode.maps=false`, the offline picker's last pick, and the seven
    `map.layer.*.visible` keys, **all true**;
  - `debug_diagnostics_preferences`: `diagnostics.synthetic_forecast=true` (the first coder's step 104).
  - This matches the planner's read at relaunch. **Two differences from the start** (`06-`, `07-`, `08-`): the seven
    `map.layer.*` keys now exist where there were none, and the Diagnostics file now exists. The app can set these
    keys but not remove them, and I may not write to its files, so "restored" for them means their default values
    (overlays true, Diagnostics false), read back. See Settings.

## Additions to the pre-registration, written after the relaunch and before each item was run

These are written at about 19:05Z, after reading the first coder's saved evidence for checks 1 to 4 and before
touching the phone. They add methods and one discriminator. They change no pass condition above.

**Check 2, no style reload: a log discriminator, re-run.** The pre-registration's own rule applies to its visual
half: the control's burst (`35-rec-basemap-street.raw`, a basemap change with the sheet open) shows no blank or
re-tiling (the box's pixel spread stays at 29 to 30 through all 322 frames), so screenshots cannot tell a reload from
none here. Its log half needs a named marker. The marker: in the app's own log (`logcat -d --pid`, read afterwards), a
`SensorManager` `unregisterListener` then `registerListener` (Rotation Vector) within 3 s of the tap. Why it marks a
style load: `SightingsMap`'s `setStyle` callback re-activates the location component on every new style
(`activateLiveLocationIfPermitted`, `ui/map/SightingsMap.kt:616`, run when `trackLiveLocation` is true, as on the Maps
tab), and the component re-registers its compass sensor. That link is my inference from the code, so the controls must
show it. Tile requests (`Mbgl-HttpRequest`) are counted too, but are not decisive: a basemap change fetches a different
source's tiles, and a reload of the same basemap might fetch none.
- **Controls:** two basemap changes with the sheet open, Topographical to Street and back.
- **Toggles:** Finds, Tracks and Offline maps, each switched off and on again, six toggles.
- **Pass:** both controls show the sensor pair, and no toggle window does. If either control lacks the pair, the check
  cannot discriminate and I say so.
- **Prediction:** pass.

**Check 4, "the track draws over the cells": method.** At a zoom where the kept track's line is at least 10 px wide,
the same camera is captured with both colour fields on and then with both fields' switches off in the sheet (then
switched back on). Pass: the track's stroke pixels, and the find, photo and waypoint glyph pixels, are equal in the two
frames within 3 per channel, while the ground beside them differs. Prediction: pass.

**Check 4, the opacity slider: method.** With the sheet open over the map, the chanterelles slider is dragged from 100%
to about 30%, and the screen is captured with the sheet still open. The map shows through the sheet (20% of it, check
1), so a patch of plain sheet fill over a chanterelle cell is compared with two frames taken before the drag (the noise
floor). Pass: the patch changes by more than 3 per channel, against a floor of about 1, with the sheet never closed.
The slider is then dragged back to 100% and `map.layer.<id>.opacity` is read back. Prediction: pass.

**Check 4, the reorder: method.** A place where both fields draw is found first: its colour differs from each field
drawn alone. The chanterelles handle is dragged down one row (so chicken of the woods is on top), the sheet closed, and
the place captured; then a basemap change, and the place captured again. Pass: the place's colour is unchanged (within 3
per channel) before the basemap change and changes after it. If no place with both fields can be found, the check
cannot discriminate and I say so. The order is then restored, with a second basemap change back to Topographical, and
read back. Prediction: pass.

**Check 4, the legend at 270: expanding it safely.** At `user_rotation` 3 the collapsed chip lies over the cluster's
"Start recording track" button (see the verdict), so a touch on the chip is made only left of the cluster's column
(x below 2083 px), never inside it. A touch reaching the record button would create a recording, which this dispatch
forbids. Pass: as the pre-registration's legend condition, with its height at most 270 px.

**Check 4, the minimum zoom: method.** The zoom is measured from the 5 km region's drawn circle, by the
pre-registration's formula. Frames are taken zooming out in small steps across 7. Pass: a frame at a measured zoom
above 7 draws cells (the control), and a frame at a measured zoom below 7 draws none, with no cell tint anywhere in
the map. Prediction: pass.

**Checks 5 to 8** run on the pre-registration's conditions above. One method is fixed now for check 5 (c): markers
inside the region are compared with the same camera with the Offline maps overlay switched off; their pixels must be
equal within 3 per channel. For check 6, the zoom above 15 is measured from the kept track's own bounding box, about
39 x 19 m (the data inventory), and at 13 and 11 from the 1 km region's circle.
## Verdicts, checks 1 to 3

Each verdict says whether it rests on the first coder's saved files, re-read and re-analysed by me, or on a re-run.
"Saved" means I read the named file myself; nothing is taken from the transcript extract. Screenshot readings are mine,
from the Read tool, and are marked "by my reading". Crops I made from saved screenshots are numbered from `131` and
named in the evidence index.

### Check 1: the Layers sheet: **pass** (saved evidence)

- **Opens from the Maps tab.** `13-layers-row-5taps.txt`: five real taps at (935,1125), (960,1150), (990,1178),
  (1020,1205) and (1045,1235), all inside the Layers row's bounds `[922,1111][1057,1246]` (`10-arrival.xml`). Each
  took the dump's "Map type" count from 0 to 1. The full sheet from the centre tap (`11-sheet-portrait.xml`) holds
  "Layers", "Map type", "Overlays" and seven switches: Finds, Photos, Waypoints, Planned trips, Recording trail, Tracks,
  Offline maps. The file records counts per tap, not a full dump per tap.
- **Opens from the entry map in fullscreen.** `25-entry-fullscreen.xml` shows the entry map in fullscreen ("Exit
  fullscreen" in its cluster) with the Layers row at `[922,1185][1057,1320]`. `26-entry-layers-5taps.txt`: five taps
  across that row, each opening the sheet. `27-entry-sheet.xml` holds five switches: Finds, Photos, Waypoints, Tracks,
  Offline maps, with no Planned trips and no Recording trail, as `ENTRY_MAP_OVERLAYS` says. On the way there the
  first coder switched the entry report's own "Offline map" switch on and off again (`22-`, `24-`); it was left off.
- **80% opaque.** I re-ran `alpha.py` on the saved screenshots. It fits each channel of the sheet-open frame as a line
  in the same pixel of the sheet-closed frame.
  - Maps tab (`10-arrival.png` against `11-sheet-portrait.png`): the scrim alone has slope 0.679 (Material's 0.32
    scrim leaves 0.68). Under the sheet the slope is 0.137, rms at most 0.65. So the sheet passes
    0.137 / 0.679 = 0.202 of what is behind it: **opacity 0.798**. Solving the intercepts for the container colour
    gives (27.8, 27.6, 27.0). The container is `SurfaceContainerLowDark`, `#1B1B1B` (27, 27, 27)
    (`ui/theme/Color.kt:140`, via `BottomSheetDefaults.ContainerColor`, `MapLayersSheet.kt:221`), so it is within 1
    unit per channel.
  - Entry map (`25-entry-fullscreen.png` against `27-entry-sheet.png`): scrim 0.681, sheet 0.139, rms 0.31,
    **opacity 0.796**.
  - A fully opaque sheet would give slope 0 under the sheet. These readings could not come from one.
- **Insets.** Frames are from `dumpsys window` (`12-`, `14-`, `17-window-*.txt`):
  - **Portrait:** the navigation bar is `[0,2181][1080,2316]`. The sheet's scroll area is `[0,581][1080,2181]` and
    its last switch row ends at 2113 (`11-sheet-portrait.xml`). The one node inside the bar's frame is "Close
    sheet", `[0,2181][1080,2316]`. That is the modal's scrim, the tap-outside-to-dismiss surface, not a sheet
    control. I read the condition "no clickable node of the sheet" as excluding it (see Decisions).
  - **`user_rotation` 1:** the bar is on the right, `[2181,0][2316,1080]`, and the cut-out on the left,
    `[0,0][75,1080]`. The sheet is `[258,219][2058,1080]`, 1800 px (640 dp) wide and centred, with its rows at
    x 303 to 2013 (`15-sheet-r1.xml`). Its drag handle starts at y 84, the status bar's bottom edge. The rows below
    the fold scroll into view, down to Offline maps (`16-sheet-r1-scrolled.xml`).
  - **`user_rotation` 3:** the bar is on the left, `[0,0][135,1080]`, and the cut-out on the right,
    `[2241,0][2316,1080]`. The sheet has the same bounds and scrolls the same way (`18-`, `19-sheet-r3*.xml`).
  - Only the scrim lies in the bar at either rotation.
- **Prediction:** held.

### Check 2: toggling each overlay

**Hide and show: pass for the five overlays with data (saved evidence, by my reading); Planned trips and Recording
trail not observable.**
- **All-on sequence** (crop `132` from `37-`, `40-` to `45-`, `49-` to `51-`, each on Street at about zoom 19):
  - Finds off removes the find glyph's cap, uncovering the waypoint pin beneath it, and Finds on restores it
    (`40-`, `41-`).
  - Photos off removes both photo glyphs, and on restores them (`42-`, `43-`).
  - Tracks off removes the whole track line, and on restores it (`50-`, `51-`).
  - Waypoints cannot be seen in this sequence: the pin lies under the photo glyphs, so `44-` and `45-` look the same.
- **Isolated hides**, with every other overlay off (crop `131`): Waypoints (`61-` to `65-`), Photos (`67-` to `65-`),
  Finds (`71-` to `73-`) and Tracks (`75-` to `77-`). In each, the glyph or line is present and then gone.
- **Waypoints shown again** (crop `135`): `87-` has everything off; `89a-` has Photos on (the photo glyph); `89b-` has
  Waypoints on, and the pin is back above the photo glyph. The sheet dumps confirm each state.
- **Offline maps** (`133`):
  - At zoom 19 the view is inside a region. Off turns the whole map from the fill's grey to the plain ground, and on
    restores it (`51-` to `53-`: pixel (200,1500) goes (159,156,152), then (242,239,233), then (159,156,152)).
  - Zoomed out, `83-` (Offline maps alone) draws the 5 km circle, its fill and its white dashed outline, and `87-`
    removes them.
- **Planned trips and Recording trail:** each switch went off and on and read back (`46-` to `49-*-sheet-after.xml`).
  No planned trip exists and nothing is recording, so there was nothing to hide. **Not observable**, as the
  pre-registration said.

**No style reload: pass (re-run).**
- **The saved visual evidence cannot discriminate**, by the pre-registration's own rule. The control's burst through a
  basemap change (`35-rec-basemap-street.raw`, re-analysed with `recan.py`) shows no blank: the box's spread stays at
  29 to 30.
- **Re-run**, using the discriminator added after the relaunch (`146-c2r-times.txt`, `146-c2r-*.xml`,
  `147-log-c2r.txt`; the sheet open throughout, the state read back after each step):
  - Topographical to Street at 18:52:05.7Z: the sensor pair followed at 06.128 and 06.146.
  - Street to Topographical at 18:52:50.4Z: the pair at 50.740 and 50.758.
  - Finds, Tracks and Offline maps each off and on, six toggles: **no** sensor line in any window.
  - Tile requests were 0 in all eight windows, so they could not have told a toggle from a basemap change, as
    expected.
- **The first coder's log agrees.** Re-counted by me from `91-log-before-stop.txt` against `toggle-times.txt`: its one
  control (17:32:58Z) shows the pair and 22 tile requests. None of its 30 toggles shows either.
- **Prediction:** held.

**A hidden layer takes no tap: pass for Finds, Photos, Waypoints and Tracks (saved evidence); not discriminating for
Offline maps, whose control failed.**
- **Finds, Photos, Waypoints and Tracks.** Each had every other overlay off. Each has a control: a tap while visible
  opens its bubble, with a "Close" node in the dump:
  - Waypoints: `62-` and `63-`, the bubble titled "DEVICE CHECK waypoint";
  - Photos: `68-`;
  - Finds: `72-`, "DEVICE CHECK find 1";
  - Tracks: `76-`.

  The same points, with the layer hidden, open nothing (no "Close", no "Directions", no "Details" node):
  - Waypoints: `66-wp-hidden-tap-680-695.xml` and `-652-776.xml`;
  - Photos: `70-` and `70b-`;
  - Finds: `74-` and `74b-`;
  - Tracks: `78-` and `78b-`.

  Each hidden state is confirmed in the sheet dump before it (`65-`, `69-`, `73-`, `77-*-sheet-after.xml`).
- **Offline maps.** The control failed:
  - Six taps on the **visible** outline opened nothing: `84-outline-visible.xml`, and `86-outline-*.xml` at five
    more points.
  - Each tap was inside a white dash run: I re-read `83-offline-only.png`, the dash rows at x 400, 480, 560, 600, 660
    and 720 against the tap points.
  - The dashes were in the same place, within 1 px, in `84b-after-outline-tap.png`, taken after the first tap, so the
    map had not moved.
  - The hidden tap (`88-`) also opened nothing, but with no working control it shows nothing either way. **The check
    cannot discriminate for Offline maps.**
  - That a visible region outline takes no tap is a finding in itself. See Flags; I did not investigate it.

### Check 3: the choices persist across `am force-stop`: **pass** (saved evidence)

- **Before the stop:** `90-map_prefs-before-stop.pb` holds `map.layer.find-markers-layer.visible = false` and
  `map.layer.kept-tracks-layer.visible = false`, with the other five true. The crash buffer was empty
  (`92-crash-before-stop.txt`, 0 bytes).
- **The stop:** the phone's system log (`126-log-resume.txt`) shows "Killing 30324:com.zynergylabs.forager.app …
  stop com.zynergylabs.forager.app" at 17:48:28.650Z. Then `Start proc 9626` at 17:48:30.366Z, from the
  `am start`.
- **After the relaunch:**
  - The sheet shows Finds and Tracks off and the other five on (`94-relaunch-sheet.xml`).
  - The map draws no find glyph and no track (crop `136`, by my reading): `93-relaunch` at the start camera, and
    `96-relaunch-locate` zoomed in, where the track would be plain.
  - Switching Tracks on (`97-`) and then Finds on (`98-`) draws each again at the same camera.
  - The stored keys are all true afterwards (`99-map_prefs-all-on.pb`).
- **Prediction:** held.

