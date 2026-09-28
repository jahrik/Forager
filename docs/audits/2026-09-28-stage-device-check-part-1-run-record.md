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
