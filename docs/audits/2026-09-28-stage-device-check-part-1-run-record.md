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


# Resumed again (continuation `2026-09-28-63`), written after the second relaunch

Everything above this heading is as pushed at `5ec7a63`, byte for byte: the first coder's pre-registration, and the
second coder's Resumed section, its additions and its verdicts for checks 1 to 3. I have not edited any of it.

**Who:** a third device coder, replacing the second, which the owner stopped at about 19:10Z ("Stop everything") part
way through check 4. It sent no hand-back, and wrote no check 4 verdict. I started at 19:23Z. **Record:** the kit is
absent at this base, and the planner writes the record (continuation entry `2026-09-28-63` in `RECORD.md` at
`48633ee`). I do not touch `RECORD.md`.

## The continuation `-63`, verbatim

From `prompts/preserved/2026-09-28-63.md` at `48633ee` (sha256
`415c8550aad4b0a5eca663f18993e416a8d5c52d8abdab9caa9153dc8a66a2b2`), below its "verbatim prompt follows" line:

> **Type:** device (continuation `2026-09-28-63` of dispatch `2026-09-28-51`)
>
> # Why you exist
>
> The device coder relaunched by `-55` was stopped at about 19:10Z when the owner said "Stop everything". It was part-way through check 4. The owner has since told the planner to relaunch it ("A": relaunch both). You replace it.
>
> **What it left, as the planner read it at relaunch:**
> - Branch `device-stage-1`, pushed at `5ec7a63`:
>   - `e99ea0c`, the Resumed section (build unchanged, no crash across the gap);
>   - `01158c6`, pass-condition additions written before its re-runs;
>   - `5ec7a63`, verdicts for checks 1 to 3. It recorded 1 as a pass, 2 as a pass with the Offline maps tap half not discriminating, and 3 as a pass.
>
>   Those stand as written. Do not edit them.
> - Evidence `124` to `200` in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-stage-check-1/`. The last files (`192` to `200`, "qz") look like check 4's minimum-zoom item. That is the planner's inference from the file names, not a finding.
> - It sent no hand-back. Any verdict it reached after `5ec7a63` is not written.
> - **The phone at relaunch** (the planner's read): awake; Forager `MainActivity` in focus; `user_rotation` 0; `accelerometer_rotation` 0. The Diagnostics switch and the overlays are unknown. Read the full state back yourself before touching anything, and compare it with `06-settings-start.txt` and `07-datastore-ls-start.txt`.
>
> # What governs
>
> In full: `prompts/preserved/2026-09-28-51.md`, then `-55`. Every rule and abort condition in them binds you. Quote `-55` and this message verbatim in a second "Resumed" section of the run record.
>
> # What you do
>
> 1. Confirm the install is unchanged, and read crashes (`logcat -d -b crash`) across the gap since 19:07Z. A mismatch or a new Forager crash is an abort.
> 2. **Check 4.** Give verdicts from the saved evidence (`124` to `200`) only where a named file shows the pass condition on its own. Otherwise re-run the item. Then finish check 4, turn the Diagnostics switch **off**, and read it back.
> 3. **Run checks 5 to 8** as `-51` sets them out.
> 4. **Restore and read back** everything `-51` and `-55` name.
> 5. **Number new evidence from `201`.**
> 6. **Commit and push the run record after each check,** not only at the end.
>
> # Merge
>
> Not authorised.

## The continuation `-55`, verbatim, as `-63` requires

From `prompts/preserved/2026-09-28-55.md` at `48633ee` (sha256
`0b9dd8cac1fedfe207f4cba68d2f6f09f337157a04e4d6cf4f798ddf730608a1`; the file is unchanged since `367c32f`), below its
"verbatim prompt follows" line. It is identical to the quote in the first Resumed section (a `diff` of the two gives
nothing).

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

## The planner's note that came with `-63`, verbatim

It came with the launch message and is marked there as not part of the store copy. No planner-log line was given, so
none is cited.

> Planner's note (not part of the store copy): a J8 build coder runs Gradle on this machine at the same time. It does not use the phone, and you need no build. The owner said earlier "S22 is ready for your device check". If any prompt appears over the app, stop at once, as `-51` says. When you finish or stop, hand back a report as the dispatch requires.

## Step 1 of `-63`: the build and the crash read across the gap (before any touch)

- **Base:** `git fetch` at 19:23Z: `origin/device-stage-1` at `5ec7a63`, the worktree clean and at the same commit.
  `origin/journal-redesign` at `48633ee`, carrying `-63`. The app code in the worktree is `26709b1`'s (`git diff --stat
  26709b1 HEAD -- app/` is empty), so code citations stay at `26709b1`.
- **Device:** `adb devices -l` lists `R5CT321008R` (SM_S908U) only.
- **Installed build, 19:24:12Z** (`201-dumpsys-package-resume2.txt`): `versionName=1.0.1356+g26709b1c`,
  `versionCode=1356`, `lastUpdateTime=2026-09-28 10:17:38` (PDT), user 0's `firstInstallTime=2026-09-22 11:15:05`,
  `ceDataInode=2495587`, `deDataInode=2494123`; user 95 unchanged. **All identical** to `124-` and `05-`. Not an abort.
- **Crash read:**
  - `logcat -d -b crash`: **0 bytes** (`202-crash-resume2.txt`, 19:24:18Z).
  - `logcat -d` (`203-log-resume2.txt`): the system buffer runs from 13:25:39Z and the main buffer from 18:55:46Z, both
    to 19:24:21Z, so both cover the gap since 19:07Z. **0** `FATAL EXCEPTION` lines. Its `AndroidRuntime` lines are
    `uiautomator` shell processes (uid 2000), not the app. The events buffer (`204-events-resume2.txt`) starts at
    18:53:31Z and holds no `am_crash` or `am_anr` line.
  - Forager's process is still pid 9626, started at 17:48:30Z (check 3's relaunch), still running at 19:24Z (`pidof`);
    `203-` has no later `Start proc` or `Killing` line for the app.
  - **No new Forager crash.** Not an abort.
- **Phone and app state at 19:24:36Z**, read before any touch (`205-settings-resume2.txt`,
  `206-datastore-ls-resume2.txt`, `207-map_prefs-resume2.pb`, `208-diag-prefs-resume2.pb`, decoded by `pbprefs.py`
  with coordinates redacted; the screen as `209-resume2-state`):
  - awake (`mWakefulness=Awake`); `isKeyguardShowing=false`; focus `com.zynergylabs.forager.app/.MainActivity`; no
    window over it; the keyboard not shown;
  - `user_rotation=0`, `accelerometer_rotation=0`, `font_scale=1.0`, `navigation_mode=0`, `ui_night_mode=2`, location
    on, `location_mode=3`: **all equal to the start** (`06-`);
  - `map_preferences`: `map.fullscreen=false`, `night_mode.maps=false`, the offline picker's last pick; the seven
    overlay `visible` keys all true; both forecast fields' `visible` keys true;
    `map.layer.forecast-chanterelles-layer.opacity = 1.0`; `map.layer_order =
    forecast-chicken-of-the-woods-layer,forecast-chanterelles-layer`. The stored order is bottom to top
    (`moveColourField`, `ui/map/layers/MapLayerPreferencesState.kt:62-73`), and this one equals the registry's own
    (`COLOUR_FIELDS`, `ui/map/layers/ColourFields.kt:42`, chicken of the woods first, so chanterelles on top), which is
    what an empty store gives (`restoreMapLayersState`, `:16-18`, `orderedLayers`, `ui/map/layers/MapLayers.kt:331-343`);
  - `debug_diagnostics_preferences`: `diagnostics.synthetic_forecast=true`. **The switch is still on.**
  - The screen: the Maps tab in portrait on Topographical ("Layers: Topographical map."), cells drawn, the legend chip
    collapsed ("2 layers"), the compact bar reading "September · Search a location".
  - **Differences from the start** (`06-`, `07-`, `08-`): the Diagnostics switch (on, to be turned off); the stored
    keys that did not exist at the start (the overlay and forecast keys, the opacity and the order), each at its
    default value. The app can set these keys but not remove them, and I may not write to its files, so, as the
    second coder said, "restored" for them means their default values, read back.

## Check 4: what the saved evidence shows, read before any touch

The second coder ran check 4 from `137` to `200` and wrote no verdict. I re-read its files myself; nothing below is
taken from its reasoning, which is lost, or from the transcript extract, except where one line says so. New scripts
and crops are numbered from `210`: `210-rows.py` (each Layers-sheet row's checked state, from the clickable row node
that carries it), `211-over-under.py`, `212-shift.py`, `213-fit.py`, `214-circle-zoom.py`, `215-patch.py` and
`216-swap-predict.py`, each described in its own header. Two of my own reads, `203-log-resume2.txt` (`logcat -d`) and
`204-events-resume2.txt` (the events buffer), cover the second coder's window. They are logs of that window, not
re-runs, and I say where a verdict leans on them.

**One tool of the second coder's is unreliable, found now.** `190-zoom.py`'s cell-period estimate reads `148` as zoom
7.20. `148` shows the 1 km region's circle about 420 px across and a single cell edge on each axis, so the period is
wider than the screen and the autocorrelation locked onto the street grid. `214-circle-zoom.py` on the same frame (the 1
km circle's white dashes, the glyphs masked) gives **12.00**, rms 1.0 px. I use `190-zoom.py`'s cell estimate only
where the cells are small and I have checked its edge columns by eye (`209`: 73 px, edges at x 237, 309, 383, 455, so
about 7.5).

### (a) Both fields draw, below the markers: **pass for the markers and the track; the location puck draws under the cells** (saved evidence)

- **Both ramps draw at zoom 7 or more.** `148` (zoom 12.00, above) with both switches on: a teal cell top left, mean
  (71, 149, 151), and orange cells below, mean (172, 107, 63). By my reading, with those samples.
- **Markers and the track over the cells.** Two pairs, each the same camera with both field switches on and then off
  (the sheet dumps `152-sheet-before`/`-after` and `156-`/`158-sheet` show only the two field switches changing, read
  with `210-rows.py`). `212-shift.py` puts both pairs at offset (0, 0), so the camera did not move. `211-over-under.py`
  takes each glyph colour's interior pixels in the fields-off frame and compares them with the fields-on frame:
  - `153` against `151` (every overlay on, Street-level zoom inside one cell): the track `#A122F8`, 4606 px, max
    difference **0**; the photo glyph, 1625 px, **0**; the find glyph, 1328 px, **0**. The ground within 30 px of them
    differs by a median of 88, and 93% of it by more than 3.
  - `157` against `159` (Finds and Photos off, so the waypoint pin is not covered): the track, 5201 px, **0**; the
    waypoint pin, 1698 px, **0**; its black crosshair, **0**. The ground: median 88.
- **The location puck is under the cells.** In the same pair its blue bearing triangle is (51, 96, 148) with the fields
  off and (105, 69, 61) with them on. `213-fit.py` fits the cells' compositing on the ground around it (per channel,
  on = k x off + c, over 95,692 px) and predicts that a pixel of the triangle's colour lying **under** the cells would
  read (102, 66, 63). It reads (105, 69, 61), within 3 per channel. The same fit predicts (151, 36, 112) for the track
  if it were under, and the track reads its own `#A122F8`. The puck is MapLibre's `LocationComponent`, activated with
  no `layerBelow` or `layerAbove` (`activateLiveLocationIfPermitted`, `ui/map/SightingsMap.kt:1164-1189`), and it is
  not in `MAP_LAYER_REGISTRY`, so the registry's promise that the fields sit "below every other layer"
  (`ui/map/layers/MapLayers.kt:224-228`) does not reach it. Whether it counts as a "marker" in this check is not mine to
  rule. See Decisions and Flags.

### (b) The opacity slider is live: **pass** (saved evidence)

- The drag at 18:57:33Z (`snaps.log`) did not move the slider: `163` still reads 100%, and its dump is byte-identical
  to `162`'s. The tap at 18:58:02.664Z took it to **27%** (`165-slider-after-tap.xml`), and `165` was captured 1.07 s
  later, with the sheet open.
- `215-patch.py`, four patches of plain sheet fill between the rows, each against `162-slider-before-1`: `162-…-2` and
  `163`, **0.0** per channel (the floor); `165` (27%), **+6 G and +8.5 B** in every patch, the map behind the sheet
  losing orange; `166` (still 27%), the same; `169` (back to 100%), **0.0 to 0.3**. The camera is unchanged
  throughout, since `169` returns to the floor.
- The sheet never closed: every dump from `162` to `169` is the sheet, and `204-` shows the sheet's window (`62cfd9`)
  holding focus without a break from 18:57:14.05Z to 19:00:18.56Z, across all of them. That second part leans on my log
  read; the verdict stands on `165` alone, the change seen with the sheet open 1.07 s after the tap.
- Stored: `167-map_prefs-after-slider.pb` `map.layer.forecast-chanterelles-layer.opacity = 0.2699…`,
  `170-…-slider-restored.pb` `1.0`. The slider was moved by a tap, not a drag.

### (c) A reorder applies at the next style load: **pass** (saved evidence)

- Sheet order read with `210-rows.py`: `179` chanterelles on top (the default), `180` chicken of the woods on top after
  the drag at 19:02:33.743Z, `184` the same after the basemap changes and back on Topographical. `181-` stores
  `map.layer_order = forecast-chanterelles-layer,forecast-chicken-of-the-woods-layer` (bottom to top, so chicken of the
  woods on top).
- `183-blocks.py` (the second coder's) classes 40 x 40 px blocks from `175` (both), `176` (chanterelles alone) and
  `177` (chicken of the woods alone), and compares a frame with `175`:
  - `178` (both, again): 415 both-blocks, max **0.2**;
  - `182`, after the reorder with the sheet closed, before any basemap change: max **1.0**, none over 3. **Unchanged.**
  - `185`, after Street at 19:03:16.977Z and Topographical at 19:03:20.555Z (`snaps.log`): **all 415** both-blocks
    changed, median **72.4**; the single-field blocks max 0.6. **Changed.**
- It is the swapped stacking, not just a different one. With each fill at 0.6 (`MapLayers.kt:222`), a pixel under both
  fields satisfies E = A - 0.6 x (Xc - Xk) whatever the ground, A being `175` and Xc, Xk the single-field frames.
  `216-swap-predict.py` over the both-blocks: `185` matches within 3 per channel at **91.3%** of 10,375 px (median error
  0.6); `182` at 0.3% (median 72.6).
- The two basemap changes were style loads: `203-` shows Forager (pid 9626) re-registering its compass sensor at
  19:03:17.336Z and 19:03:20.935Z, the marker the second coder established (18:52 controls, check 2), and none at the
  reorder drag. This leans on my log read.
- **Restored:** `187` chanterelles on top again; `189-` stores `forecast-chicken-of-the-woods-layer,
  forecast-chanterelles-layer`, the registry's own order. `188` equals `175` in every block (max 0.8). `snaps.log`
  records no basemap change between `187` and `188`, but `203-` shows two more style loads at 19:03:51.113Z and
  19:03:54.697Z, inside the sheet's window of 19:03:45.5Z to 19:03:58.6Z (`204-`), so `188` followed a style load. No
  style load since 19:03:54.7Z is in `203-`, so the map draws the default order now.

### (d) The legend chip: **pass in portrait and at 90; fail at 270** (saved evidence), with the swipe at 90 and 270 to re-run

Bounds from the dumps (clickable node; its scroll area in brackets); system bars from check 1's `dumpsys window`
reads (`12-`, `14-`, `17-`); the "i" is MapLibre's "Attribution icon" node.

| | chip collapsed | expanded | cluster (rows) | rail or nav | "i" |
|---|---|---|---|---|---|
| portrait (`144`, `111`) | `[842,1747][1057,1882]` | `[269,1596][1057,1866]` | x 922-1057, y 673-1718 | app nav y 1956-2181 | `[1010,2246][1069,2305]` |
| 90 (`120`, `121`) | `[1718,871][1933,1006]` | `[1145,720][1933,990]` | x 98-233 (left) | rail x 1956-2181 | `[2246,1010][2305,1069]` |
| 270 (`123`, `140`, `141`) | `[2003,871][2218,1006]` | `[1430,720][2218,990]` | x 2083-2218 (right) | rail x 135-360 | `[2246,1010][2305,1069]` |

- **Portrait, collapsed:** below the cluster's last row by 29 px, above the nav, above the "i". Clear. (It sits below
  the cluster, not right of it: in portrait the cluster is on the chip's own edge.)
- **90:** right of the cluster, 23 px left of the rail, above the "i" (1006 against 1010). Clear, collapsed and
  expanded.
- **270: fails.** The collapsed chip lies across the cluster's column. `123` and `140` have **no "Start recording
  track" node** (at 90 it is `[132,916][200,970]`), and my crop of `123` shows the chip drawn over the record button,
  whose white dot shows through it. Expanded (`141`), the legend cuts the "Plan a trip" row at y 720 and covers the
  record row. The chip is placed `BottomEnd` whatever the rotation (`AvailabilityCompactMapUi.kt:1150-1161`), and at 270
  the cluster is on the right, the side the chip is on.
- **Expand, cap, collapse:** a touch expands it in all three (`111`, `121`, `141`), to exactly **270 px** (96 dp) in
  each, the scroll area `scrollable=true`; a second touch collapses it (`118`, `122`, `143`).
- **Scroll:** portrait only. `115` to `117`, three swipes inside the expanded legend, move its content up (the
  chanterelles label from y 1698 to 1596 and off, then chicken of the woods, then the explanatory note), with the same
  270 px bounds, still expanded. At 90 and 270 no swipe was made. **Re-run** (below).
- The portrait "i" is under the system navigation bar (`[0,2181][1080,2316]`) and at 90 under the bar on the right; see
  Flags. The chip clears it either way.

### (e) The cluster stops above the chip: **expanded, fail** (saved evidence); **collapsed, to re-run**

- **Expanded, portrait:** `114` (the cluster dragged low, then the legend expanded) and `111` (not dragged) both put
  the cluster at its centred position, top row 673, and its last row at `[922,1583][1057,1596]`: 13 px of a 135 px row
  showing, and no "Return to vehicle" node in either dump. The expanded legend's top is 1596, so the **overlap is
  122 px** (1718 - 1596). The code accounts for it: the clamp's lowest edge is the chip's top less `Spacing.sm`, but
  the downward limit is floored at the centred position (`maxDownwardOffsetPx … coerceAtLeast(0f)`, and the clamp's
  upper end `maxOf(maxUpwardOffsetPx, maxDownwardOffsetPx)`, `AvailabilityCompactMapUi.kt:785-811`), so a chip that
  leaves no room below the centred cluster is overlapped rather than cleared. That reading of the code is mine.
- **At 270** the cluster, at the position `123` shows, is already overlapped by the collapsed chip (see (d)).
- **Collapsed, portrait:** `113` has the cluster 12 px lower than `112`, its last row ending at 1730 against the chip's
  top at 1747: **17 px**. That this was the lowest the cluster could go rests on the drag's length (y 1032 to 2150),
  which only the transcript extract records. A file does not show it on its own, so **re-run**.

### (f) Below the minimum zoom, cells draw nothing: **not shown by the saved evidence; re-run**

`191` to `199` all draw cells. Their cells are about 150 px (`191`) down to about 75 px (`199`) across, zoom about 8.5
down to 7.5; none is below 7. The second coder's last quick zoom (`199`) is where the owner's stop found it.

## Additions for the check 4 re-runs, written before they were run (after the second relaunch)

**(d) Scroll at 90 and 270.** At `user_rotation` 1, then 3: a touch expands the chip; a swipe up inside the expanded
legend; a dump; a second touch collapses it. At 270 every touch on the legend is made left of x 2083, never over the
cluster's column, as the second coder's rule says (a touch reaching the record button would create a recording). Pass:
after the swipe, the legend is still expanded (270 px, `scrollable=true`) and its first text node has moved up.
Prediction: pass at both (the chip's code does not depend on rotation).

**(e) Collapsed.** In portrait, with the chip collapsed, the cluster's drag handle ("Hide map controls") is dragged
with `input motionevent` from its centre to y 2150, as the second coder did. Pass: the cluster's last row ends above
the chip's top. The gap is recorded. The cluster is then dragged back up to its centred position (top row 673) and
read back. Prediction: pass, about 17 px, as `113`.

**(f) Minimum zoom: method.** Pan so the 5 km region is on the map, then quick-zoom out (`197-qzoom2.sh`, a one-finger
double-tap-and-drag) in small steps across 7, measuring each frame's zoom from the 5 km circle with
`214-circle-zoom.py`. "Draws nothing" is measured, not read by eye: at a frame measured below 7, the same camera with
both field switches off in the Layers sheet must equal it over the map (`211`/`212`-style: offset (0, 0), and the map
area outside the chip, the credit line and the sheet's own trace equal within 3 per channel). The control is the same
on/off comparison at a frame measured above 7, which must differ over the cells. The switches are put back on after
each pair and read back. Code: below `MIN_FORECAST_ZOOM` the effect requests no block and sets each field's source
empty at the next camera idle (`ui/map/SightingsMap.kt:684-705`); the legend chip stays (`ForecastCellLayer.kt:15-24`).
Prediction: pass.

**(f), one method detail added at 19:42Z, before any (f) frame.** At zoom 7.5 the 5 km circle is about 50 px in radius
and the find and photo glyphs cover its upper half, their white outlines indistinguishable from its white dashes. For
the (f) frames only, Finds, Photos and Waypoints are switched off in the Layers sheet (Offline maps stays on: the circle
is the ruler), and a 22 px disc at the circle's centre (the 1 km region's own small circle) is left out of the fit.
They are switched back on after (f) and read back. The quick zoom is made on open map away from the circle, so its
first tap lands on no glyph.

**(f), the zoom ruler replaced, at 19:50Z, before any (f) frame was taken.** The method above cannot measure a zoom
below 7 on this phone. The search-centre reticle is drawn at the same centre as the 5 km circle and is not switchable
(`MarkerGlyphs.kt:47`, ring 8 dp at its stroke's centre, 10.5 dp to the edge of its casing, arms to 13 dp plus
casing; `MapLayers.kt:272-278`, the sightings and search centre `userToggleable = false`). At this latitude the 5 km
circle's radius is 32.7 px at zoom 7.0, 30.5 px at 6.9 and 23.1 px at 6.5, against the reticle's 29.5 px ring casing,
so below 7 the circle is inside the reticle. The two regions' centres are 1012 m apart, too close to be a ruler. The
replacement, which changes no pass condition:
- **The control frame** C (cells drawn) is measured from the cell grid, whose 0.1-degree period gives the zoom at any
  latitude (`ForecastCellLayer.kt:35-36`; edge columns checked by eye), and from the 5 km circle's diagonal quarters
  where it clears the reticle, as a cross-check.
- **The below-7 frame** B is measured against C by registration: C and B each with both field switches off (C_off,
  B_off, so no cell tint differs between them), anchored at the reticle's centre (the centroid of its pure-black
  pixels, a fixed geographic point in both), and a one-parameter search over the scale s that best maps C_off onto
  B_off, by mean absolute difference over the map area with the reticle, chip, cluster and credit masked. z_B = z_C +
  log2(s). The same search on C_off against a second capture of C_off at the same camera must return s = 1 (the
  control on the ruler itself).
- **Draws nothing** is then B against B_off, as written above, and the control is C against C_off.

## Check 4: the re-runs, and the verdict

### Corrections to my own lines above

- (a) says `151`/`153` are at "Street-level zoom". The basemap there is **Topographical** (`152-sheet-before`); I meant a
  close zoom, inside one cell.
- The (f) ruler paragraph cites `MapLayers.kt:272-278` for the search centre. The search centre is `:270`
  (`toggleable = false`); `:272-278` is the sightings layer.
- The (f) method detail says "at 19:42Z, before any (f) frame", and the ruler paragraph "at 19:50Z, before any (f)
  frame was taken". The commits are `f840b00` at 19:42:23Z and `2ef2d25` at **19:45:24Z**. `230` (the pan, 19:41:51Z)
  came before both, and `232` (Finds, Photos and Waypoints off, 19:42:44Z) between them. Both are set-up frames at zoom
  7.51. Every zoom step (`236` onward, from 19:45:59Z) came after both.

### (d) The swipe at 90 and 270: **pass** (re-run)

- **90**, from 19:39:48Z (`221-` to `224-`): `221` matches `120` (chip `[1718,871][1933,1006]`). A tap at (1825, 938)
  expanded it to `[1145,720][1933,990]`, 270 px (`222`). A swipe inside it, (1500, 960) to (1500, 760), moved its
  content up ("0%" from y 959 to 756, the chanterelles label off the top, the chicken of the woods label into view),
  still 270 px and `scrollable=true` (`223`). A tap at (1540, 855) collapsed it (`224`).
- **270**, from 19:40:33Z (`225-` to `228-`): `225` matches `123`, again with no "Start recording track" node. A tap
  at (2040, 938), left of the cluster's column, expanded it to `[1430,720][2218,990]` (`226`; the "Plan a trip" node
  gone too). A swipe at x 1700 moved its content up ("0%" from 959 to 752), still expanded (`227`). A tap at
  (1700, 855) collapsed it (`228`), whose dump still reads "Return to vehicle — start recording first": no recording
  was started.
- `user_rotation` read back 1, then 3, after each dump, and 0 after the return (19:41:30Z, `229`).

### (e) Collapsed: **pass** (re-run)

In portrait, chip collapsed, cluster centred (`217`: rows 673 to 1718). The drag on "Hide map controls" from (1052,
1032) to (1052, 2150), 1118 px, at 19:38:52Z moved the cluster **12 px**: rows 685 to 1730 (`218`), against the chip's
top at 1747, a gap of **17 px** between the clickable bounds, the same as `113`. In my crop
(`219-crop-cluster-chip.png`) the cluster's drawn outline ends clearly above the chip's. A 12 px drag back up
(19:39:27Z) returned it to rows 673 to 1718 (`220`), its centred position.

### (f) Below the minimum zoom, cells draw nothing: **pass** (re-run)

- Set-up: markers off (`232-markers-off-sheet-after.xml`: Finds, Photos, Waypoints off, Offline maps and both fields
  on). The zoom steps are `197-qzoom2.sh` at (300, 1400), open map; no bubble opened at any step (no "Close" node).
- **Cell-grid zooms**, from `233-grid.py`'s period fitted to its strongest edges (it now also reports that fit, as
  its raw autocorrelation locked onto a multiple of the period for `238` and `239`): `232` **7.510**, `236` 7.407,
  `237` 7.301, `238` 7.198, `239` **7.096**; each fit's residual is 3 px over seven gaps.
- **The ruler checked before use.** `247-register.py` on `240` against `241` (the same camera twice) returns s =
  1.000 with a sharp minimum (0.00 against 12.6 one step either side). On pairs the grid measures independently, it
  agrees to 0.002: `232` to `239` gives log2(s) = -0.4150 (grid: -0.414), `236` to `238` gives -0.2076 (grid: -0.209).
- **The control, C = `239`, zoom 7.096.** Fields on against fields off at the same camera (`240-C-off`, the sheet dump
  showing only the two field switches changed): **96.7%** of 1,279,141 map pixels differ by more than 3, median 82.
  Off against a second off capture (`241`): max **0**. On against on again (`242`): max **0**. The cells draw, and
  come back exactly.
- **B = `244`**, after one step of dy -120 (19:48:25Z). No cell is visible, and the basemap has changed raster level.
  The legend chip still shows "2 layers", as the code says it should below the limit (`ForecastCellLayer.kt:15-24`).
  Fields on (`244`) against fields off at the same camera (`245-B-off`): **max 0** over the map (`243-onoff.py`, share
  over 3: 0.0000). On again (`246`): max 0.
- **B's zoom:** `245` (B off) against `240` (C off), anchored at the reticle's centre: s = **0.866** (mean difference
  8.45 against a curve median of 34.0 over 0.40 to 1.00; flat within 0.3 from 0.864 to 0.868), so zoom **6.888**,
  within about 0.01.
- So cells draw at 7.10 and draw nothing at 6.89, with the bracket straddling 7 by 0.10 and 0.11.
- Restored: Finds, Photos and Waypoints on (`248-markers-on-sheet-after.xml`); `249-map_prefs-after-f.pb` has every
  overlay and both fields `true`, the chanterelles opacity 1.0 and the order at the registry's.

### The Diagnostics switch: **off, read back**

- Tools, Settings, then "Diagnostics (debug build)" (`251-` to `254-`, each target found by its text in a fresh dump).
  `254` shows "Synthetic forecast layers" checked. A tap on its row at 19:52:08.9Z turned it off: `255` shows the row
  unchecked, and `257-diag-prefs-off.pb` stores `diagnostics.synthetic_forecast = False` (`256-`: the file rewritten at
  12:52 PDT).
- Back on the Maps tab with the drawer closed (`260`), the chip "2 layers" and the credit "· Synthetic test data" were
  **still shown**. The tab asks the store which fields have data only when it comes into view (`onMapShown`,
  `ui/availability/AvailabilityViewModel.kt:470-493`), and the Tools drawer opens over it, so it had not. After List,
  then Maps (`261`, `262`), both were gone: the credit reads "© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)" and no
  legend node is in the dump. See Flags.
- Crash buffer after check 4: 0 bytes (`263-`, 19:53:49Z); Forager still pid 9626.

### Check 4, in one place

| item | verdict | from |
|---|---|---|
| (a) fields draw, below the markers | pass for every registry marker and the track; the MapLibre location puck draws **under** the cells | saved: `148`, `151`-`153`, `156`-`159` |
| (b) opacity slider live | pass | saved: `162`-`170` |
| (c) reorder at the next style load | pass | saved: `175`-`189`; my log read `203` for the style loads |
| (d) legend | pass in portrait and at 90; **fail at 270**: the chip, collapsed or expanded, lies over the cluster's record button | saved: `111`-`123`, `140`-`144`; re-run `221`-`228` (swipe) |
| (e) cluster stops above the chip | collapsed: pass, 17 px (re-run `217`-`220`); **expanded: fail**, 122 px overlap (saved `111`, `114`) | both |
| (f) below the minimum zoom, nothing | pass: draws at 7.10, nothing at 6.89 | re-run `230`-`249` |
| switch off, read back | done | `254`-`257`, `262` |

**Prediction:** "draws and slider pass; the reorder passes at the basemap change; the legend clears the 'i', the rail
and the nav; the cluster-over-expanded-legend clamp is the likely failure". **Held**, except that nothing predicted the
legend lying over the cluster at 270, or the puck under the cells.

## Check 5: methods, written before it was run (after the second relaunch)

The pre-registration's conditions stand, with the second coder's addition for (c). The code premises hold at
`26709b1`: night fill `#202020` (`ui/theme/MapPalette.kt:111`) at 0.2 (`MapLayers.kt:153`); the outline dashed in
`MapPalette.casing` (`SightingsMap.kt:1430-1444`), `#000000` at night (`MapPalette.kt:116`) and `#FFFFFF` by day
(`:89`), 1.5 dp wide, 6 dp dashes and 4 dp gaps (`:1703-1707`). At night the Topographical and Street rasters take the
V1 paint, lightness inverted and hue kept (`BasemapStyles.kt:27-70`), so the darkest night ground is where the day map
is lightest.

- **Set-up:** Night Maps on in Settings, read back (`night_mode.maps = true`). The Maps tab at a zoom where most of the
  5 km circle is on screen with the markers at its centre. Topographical first, then Street.
- **(a) darker, ground legible:** the same camera with the Offline maps overlay on and then off. Over the region's
  interior (markers, reticle, dashes and chrome masked), a per-channel line fit, on = k x off + c. The fill predicts
  k = 0.8 and c = 0.2 x 32 = 6.4. Legibility is my reading of a 2x crop, backed by the ground's local contrast inside
  (the standard deviation of luminance over 9 x 9 px windows) with the overlay on, as a share of the same with it off:
  the fill predicts 0.8. The pre-registration's inside/outside sample is also given.
- **(b) the dashes over the darkest ground:** the circle fitted to the dash pixels (the pixels that go dark when the
  overlay goes on, on the circle). The ground along the circle is read from the overlay-off frame, and the darkest arc
  it crosses is found. There, and on the lightest arc for comparison, the dash's luminance against the ground beside it
  is given as a contrast ratio, with a 2x crop and my reading of whether the dashes can be seen.
- **(c) markers:** each marker's interior pixels (5 px in from any edge of its colour) in the overlay-on frame against
  the overlay-off frame, equal within 3 per channel.
- **Restore:** Offline maps on, basemap back to Topographical, Night Maps off, each read back.
- **Prediction** (the pre-registration's): darker with ground legible; the outline weak or invisible over the darkest
  ground; markers unchanged.

## Check 5: the night offline region: **(c) pass; (a) and (b) observations as below**

Run from 19:55Z. Night Maps on at 19:56:11Z (`266`: the checkbox checked; `267-` `night_mode.maps = True`). Returning to
the Maps tab had put the camera back on the location at about zoom 12 (`262`, before Night Maps; see Flags), so two
quick zooms out on open map brought the whole 5 km circle on screen with the markers inside it (`270`).

- **Frames, Topographical:** `271` overlay on, `272` Offline maps off (only that switch changed,
  `272-…-sheet-after.xml`), `273` on again: `271` against `273` max 0 over the map. **Street:** `280` (the basemap
  switched in the sheet, `280-…-sheet-after.xml`), `281` off, `282` on again: max 0. The markers sit in exactly the same
  pixels in `271` and `280` (4635 glyph px, the same centroid and bounds), so the camera did not move between them.
- **The circle:** `275-region-edge.py` fits the region's edge from where the overlay changes pixels (`274-` is that
  mask): centre (600.5, 1255.7), radius **426.6 px**, rms 1.7 px over 131 rays, about zoom 10.7. Only one circle
  shows: the 1 km region lies inside it, round the markers. On Street the same fit is poor (rms 15.4), because more of
  the night ground there is near `#1E1E1E`, where the fill changes nothing; with the camera shown unchanged, Street's
  measurements use the Topographical fit.

**(a) Darker, with the ground legible.**
- **The fill is exactly the designed one.** `276-fill-fit.py` over the interior (the markers, the 1 km region, the
  dash band and the chrome masked): Topographical **k 0.800, c 6.00** in every channel, rms 0.28, over 91,480 px;
  Street k 0.794 to 0.801, c 5.82 to 6.25, rms 0.26 to 0.29. Predicted 0.8 and 6.4.
- **The ground stays legible, by the numbers:** its local contrast inside is **0.800** of the same ground with the
  overlay off (median over 2263 windows on Topographical, 1720 on Street; p10 to p90 0.794 to 0.807). By my reading of
  the 2x crops (`278-`, `283-`), the ground's detail inside reads the same as outside.
- **The pre-registration's inside/outside samples** agree: Topographical, 9 same-ground pairs, inside (28.3, 32.6,
  18.4) against 0.8 x outside + 6.4 = (28.5, 32.7, 18.5); Street, 58 pairs, (48.1, 69.8, 39.6) against (48.6, 70.2,
  40.0), outside (52.7, 79.8, 42.0).
- **But "reads darker" holds only for part of the ground.** With c = 6, ground whose brightest channel is under 30 is
  made *lighter* by the fill, by at most 6 levels. That is **23%** of the region's interior on Topographical and 25% on
  Street at this camera (`277-dash.py`). By my reading of the whole frames (`271`, `280`), the region does **not**
  read as a distinctly darker area at this zoom: I cannot see the fill's edge as a step in tone anywhere, only the
  dashes where they show.

**(b) Does the dashed outline carry the edge over the darkest ground?** `277-dash.py`, per 10-degree bin of the circle
(26 bins clear of the chrome), gives the dash pixels' mean relative luminance against the brighter side's ground:
- **Topographical:** over the darkest ground the edge crosses (the lower arc, 70 to 120 degrees, ground outside
  relative luminance 0.006 to 0.023), **1.08:1 to 1.21:1**. Over the lightest (230 to 300 degrees), 2.07:1 to 2.37:1.
- **Street:** darkest, **1.28:1 to 1.45:1**; lightest, up to 2.65:1.
- **By my reading** of the 2x crops, overlay on beside overlay off (`278-` and `279-` Topographical, `283-` Street):
  where the dashes cross green vegetation or grey road lines they read plainly. Across the near-black patches between,
  they are black on near-black and I can barely make them out. Over the lightest arc they read plainly. **So no: over
  the darkest ground the outline does not carry the edge,** and at this zoom nothing else does. This is what the palette
  comment says was told to the owner when `#202020` was picked ("Over that ground only the dashed casing marks the
  edge; how well it does is unverified on a screen", `MapPalette.kt:101-110`). This is the screen answer.

**(c) Markers inside the region: pass.** `211-over-under.py`, overlay off against on, interior pixels: photo glyph
1625 px, find glyph 1521 px, their black casing 427 px, max difference **0** on both basemaps (the ground beside them
differs, median 9 and 7). The reticle's 2 dp stroke is too thin for the 5 x 5 interior test at this zoom. **The
location puck is under both region fills:** its blue (74, 144, 226) reads (58, 103, 156) with the overlay on, against
0.64 x v + 10.8 = (58, 103, 155) for two stacked 0.2 fills (it is inside the 1 km region too), on both basemaps. That
is the same as check 4 (a); it is not a registry marker.

**Restored and read back:** Topographical (`284-…-sheet-after.xml`), Offline maps on, Night Maps off at 20:03:36Z
(`287`: unchecked; `288-` `night_mode.maps = False`). Crash buffer 0 bytes (`290-`, 20:03:52Z); Forager still pid 9626.

**Prediction:** "darker with ground legible; the outline weak or invisible over the darkest ground; markers unchanged".
The outline and markers **held**. "Darker" held only in part: the fill is exactly as designed, but it lightens the
darkest quarter of the ground and does not read as a darker area at this zoom.

## Check 6: methods, written before it was run (after the second relaunch)

The pre-registration's pass condition and data limit stand, as does the second coder's addition on measuring the
zoom. The code premises hold at `26709b1`: the kept track is 6 dp (`KEPT_TRACK_STROKE_WIDTH_PX = 6f`,
`SightingsMap.kt:1702`, used as `widthDp`, `:1417`), its casing the same line plus 1.5 dp a side, 9 dp
(`casingFor`, `:1398-1401`; `CASING_WIDTH_DP`, `:1598`), both through `lineWidthExpression` (`:1377-1384`) over
`TRACK_WIDTH_ZOOM_STOPS` (`ui/map/layers/TrackWidthByZoom.kt:23-26`). The casing is `MapPalette.casing`, white by day
and black at night.

- **Set-up:** Finds, Photos and Waypoints off for the whole check, since their glyphs sit on the track; Tracks and
  Offline maps on. Restored and read back at the end.
- **Widths, from a Tracks on/off pair at one camera** (the Layers sheet, only that switch changed): across a straight
  run of the track, profiles perpendicular to it. The drawn width is the sum over the profile of each pixel's coverage,
  (on - off) over (casing - off) projected per pixel, 1 where the pixel is the line's own colour; the line width is the
  sum of each pixel's share of line colour against casing colour. Both are sub-pixel, and neither depends on the ground
  beside the track except through the off frame.
- **At 15 and above** the widths do not depend on zoom, so the zoom only has to be shown to be 15 or more: from the
  track's drawn extent against its bounding box in metres (from the read-only DB copy, not printed), which gives a
  lower bound. Pass: 16.9 +/- 1 px and 25.3 +/- 1 px.
- **At about 13 and 11**, the zoom from the 1 km circle (`214-circle-zoom.py`), and the track's drawn footprint (the
  on/off difference) against the two models: its bounding box in px, from the DB, plus the thinned casing, or plus the
  unthinned 9 dp.
- **Topographical, Street, and night** (Night Maps on, Topographical), each at 15 and above. The blob comparison at 13
  and 11 is made on Topographical by day only. That is my scoping of the pre-registration's "on Topographical, Street,
  and at night", which I read as applying to the width check, where the three differ in the casing's contrast; the
  blob's size does not depend on the basemap.
- **Prediction** (the pre-registration's): pass at 15 and above; at 11 the blob favours the thinned model; at 13 it may
  not discriminate.

## Check 6: track widths by zoom: **pass at 15 and above on Topographical, Street and at night; 13 and 11 not observable**

Run from 20:06Z. Finds, Photos and Waypoints off (`291-…-sheet-after.xml`). Double taps at the track brought the camera
to the map's maximum zoom: two more at 20:07:59Z changed nothing (`293` has the same 9615 track pixels as `292`).

**The zoom at the width frames: 17.0.** From the DB copy the track spans 19.3 m east-west and 38.4 m north-south. Its
line-colour extent, 144 x 274 px, less one line width, matches zoom 17.0 on both axes; `296-widths.py`'s centreline fit
gives 17.085 (mean distance of line pixels to the fitted centreline 4.10 px, about what a 17 px line gives). Either way
it is well above 15, which is all the width check needs.

**The widths, by a raw pixel cut** (the primary evidence; no tool involved). Row y 1290 crosses a straight run of the
track at x about 624, whose direction is within 1.6 degrees of vertical (cos 0.9996). With the Tracks overlay on
against off at the same camera:
- **Drawn (casing) width:** the casing's edge pixels cover 0.48 (x 610) and 0.87 (x 635) of their pixel, with 24 full
  pixels between: **25.35 px**, against 25.31 (9 dp).
- **Line width:** the line-to-casing mixes are 0.258 (x 614) and 0.647 (x 631), with 16 full pixels between:
  **16.91 px**, against 16.88 (6 dp).
- **Day and night give the same pixels.** At night the casing is black and the line (238, 167, 254). The coverage of
  each edge pixel is the same by day and at night to 0.01 (x 610: 0.48 and 0.49; x 635: 0.867 and 0.874; x 614: 0.258
  both). The drawn geometry does not change with the palette.

**The widths, by `296-widths.py`** (third version; see below), perpendicular profiles over the run's middle, keeping
only profiles more than 50 px from the reticle, more than 26 px from any other leg of the track, with the ground
beside them the same in both frames:
- **Topographical** (`298-c6-topo-on-c`/`297-c6-topo-off-a`): 5 profiles, line **16.89 to 16.90 px**, drawn **25.34 to
  25.36 px**.
- **Street** (`301-c6-street-on-b`/`302-c6-street-off-c`): the same 5, the same figures. They are identical to
  Topographical's by construction: the measure is each pixel's coverage, which does not depend on the ground, and the
  track's pixels are the same.
- **Night** (Night Maps on at 20:25:07Z, `306`/`307-` read back; `308-c6-night-on-b`/`309-c6-night-off-b`): 2
  profiles (fewer pass the contrast floor on dark ground), line **16.90 px**, drawn **25.36 px**.
- **Thin, said so:** the clean profiles all lie on one run of the track about 25 px long. With the other-leg rule
  off, 39 profiles pass on two runs. The same run gives 16.88 to 16.94 and 25.31 to 25.42 over 19 profiles. The other,
  near the track's southern end and within 26 px of another leg, reads wider (line 17.2 to 18.5, drawn 25.7 to 27.6),
  falling steadily along its length, which is that leg's casing entering the profile.
- **The tool was corrected twice, each time from a specific pixel, before these figures.** Version 1 read drawn
  54.6 px: it counted as track any pixel whose share of line colour against white exceeded 0.5, and the blue-grey
  ground inside the location accuracy disc scores 0.63. Version 2 read drawn 26.0 px at night only: at the casing's
  outer edge a half-covered black pixel, (14, 18, 22), lies 7 levels from the black end of the black-to-line segment
  and was counted as fully covered. Version 3 explains each changed pixel either as casing over its own ground or as line
  over casing, whichever fits. The raw cut above does not depend on any of this.
- **The ground frames also needed care:** the location puck, its compass triangle and its accuracy disc draw under
  the track (checks 4 and 5) and change from frame to frame, so the first on/off pair (`293`/`294`) had a different
  disc under the track. Pairs were taken in runs (`297-`, `298-`, `302-`, `303-`, `309-`, `310-`) and the best ranked
  by `300-nearband.py`; each profile is also checked on its own ground.

**At about 13 and 11: not observable.** The search-centre reticle is drawn on the track (markers above lines) and does
not move with the location puck (the puck wandered round it between frames). At these zooms it covers the track:
- **13.54** (1 km circle, rms 1.3 px, `317`): 33 track-coloured pixels on the screen. With Tracks toggled (`318`,
  `319`), the only changes near the reticle are slivers inside the ring's quarters and the puck's own movement
  (`320-crop-c6-z13-on-off-diff.png`). The blob the check needs measures 31 x 43 px thinned or 37 x 49 unthinned here,
  so its edge falls under the ring and its casing, 15.5 to 29.5 px from the centre.
- **11.99** (rms 1.0, `321`): no track pixel; toggling changes 6 pixels near the reticle (`322`, `323`,
  `324-crop-…`).
- **10.94** (rms 1.1, `325`): toggling changes **0** pixels within 50 px of the reticle (`326`, `327`).
- The pre-registration expected the blob check to be weak at these zooms. With this data it cannot be made at all: not
  a pass, not a fail. With these zooms by day, the check's scoping to day for 13 and 11 made no difference.

**Restored and read back:** Topographical (`304`), Night Maps off at 20:35:04Z (`314`, `315-`), Finds, Photos and
Waypoints on (`328-…-sheet-after.xml`); `329-` has every overlay and both fields true, the order and opacity at their
defaults, `night_mode.maps = False`. Crash buffer 0 bytes (`330-`, 20:39:51Z); Forager still pid 9626.

**Prediction:** "pass at 15+; the blob comparison favours the thinned model at 11; at 13 it may not discriminate". The
first part **held**. The blob could not be seen at either zoom, which the prediction did not foresee: the reticle, not
the track's size, is the limit.
