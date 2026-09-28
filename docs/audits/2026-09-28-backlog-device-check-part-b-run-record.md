# Backlog device check, Part B (landscape, the Maps drawer and the pickers), on the S22 Ultra: run record

**Status: pre-registration only.** Nothing below has been run yet. This file is committed and pushed before any item
is looked at, so the order of prediction and observation is checkable. Results are added in a later commit; this
section is kept unchanged as the Appendix.

**Date:** 2026-09-28, from 09:45 UTC.
**Device:** Samsung SM-S908U, serial `R5CT321008R`, the only device attached (`adb devices -l`, 09:45:46Z).
`getprop` at 09:46:16Z: `ro.product.model=SM-S908U`, `ro.build.id=BP2A.250605.031.A3`.
**Build under test:** the installed build, `versionName=1.0.1192+g24589349`, `versionCode=1192`,
`lastUpdateTime=2026-09-27 19:18:29` (`01-dumpsys-package-start.txt`, 09:46:16Z). It matches the dispatch. Nothing
installed; no Gradle, no emulator.
**Crash buffer at the start:** `logcat -b crash -d` read 0 bytes (`02-crash-start.txt`, 09:46:16Z). The app is
process 19584, the same process as in L0a and Part A. The phone was unlocked (`isKeyguardShowing=false`) with
`MainActivity` in focus.
**Settings at the start** (`03-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
`navigation_mode=0` (three-button), `ui_night_mode=2` (`cmd uimode night`: yes), `location_mode=3`, location enabled.
**Dispatch:** `prompts/preserved/2026-09-28-14.md`, with Part A's `2026-09-28-04.md` and `2026-09-28-12.md` applying
except where it differs. The intent is `2026-09-28-14` in `RECORD.md`, the planner's. I do not touch `RECORD.md`.
**Base:** the dispatch names "`origin/journal-redesign` at the commit carrying this file", which is `fe0f0d7`. By the
time the worktree was added (09:45Z), `origin/journal-redesign` had moved on to `af846cc`, the L0b coder's merge
of `fe0f0d7` with its completion report (one file, `docs/audits/2026-09-28-map-layers-l0b-completion-report.md`).
This branch, `device-backlog-b-2026-09-28`, is cut from `fe0f0d7`, the commit the dispatch names (see Decisions).
**Code citations** are at the installed build's commit `2458934`, as in Part A, read with `git show 2458934:…`.
**Evidence:** outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-backlog-b/`, cited
by name with sha256. No screenshot, dump, coordinate, note text, place name or photo is in this file.
**dp to px:** `wm density` 450, so 1 dp = 2.8125 px; `wm size` 1080 x 2316 px, so the landscape window is
823.5 x 384 dp before insets.

# Pre-registration: pass conditions and predictions, written before each item was looked at

The item numbers are those of `docs/audits/2026-09-28-device-backlog-compilation.md`. Abbreviations are that file's.
Every touch is a real `adb shell input` touch at screen coordinates from a fresh dump or screenshot. "Sampled across
its bounds" means at least five touches spread across the control's own bounds, as in Part A.

## Step 1: build, baseline, rotation facts
- **Pass:** `versionName=1.0.1192+g24589349` (read above); `logcat -b crash -d` read at the start and at the end, a
  new Forager line being a stop.
- **Rotation facts to record** at `user_rotation 1` (ROTATION_90) and `3` (ROTATION_270), with
  `accelerometer_rotation 0`: from `dumpsys window displays`, the display rotation, the cut-out rect, the status-bar
  and navigation-bar frames; the window size in dp. **The charger port's physical edge cannot be read from any
  dumpsys**: Android exposes no port position. I will infer it from `portEdgeFor` (`ui/adaptive/PortEdge.kt:29-33`:
  the natural-orientation bottom edge, the window's right at ROTATION_90 and its left at ROTATION_270) and say it is
  inferred. The cut-out's edge is read.
- **Prediction:** the build matches (already read) and the crash buffer stays empty. At ROTATION_90 the cut-out is on
  the left and the 3-button bar on the right; at ROTATION_270 the reverse. The window is 823.5 x 384 dp at both.

## Step 2: the B3 Maps tools drawer, items 1 to 4 (B3d:190-197)
Code: `AvailabilityScreen.kt:1506-1571` at `2458934`. In a short landscape window the drawer's layout direction is
set so that its start edge is the port edge (Rtl when the port is on the right), `gesturesEnabled =
drawerState.isOpen`, and the sheet is `ModalDrawerSheet`. Tools on the rail opens it.
- **Item 1 (ROTATION_90). Pass:** the sheet sits against the right edge of the window, on the rail and 3-button bar
  side; none of its interactive content lies within the navigation-bar frame read in step 1 (each clickable node's
  bounds compared against that frame); its rounded corners are on its left edge, facing the map (by my reading of a
  screenshot crop, said so).
- **Item 2. Pass:** at each rotation, a tap on the scrim (the map area outside the sheet) closes the drawer (no sheet
  nodes in the next dump), and Tools then reopens it. The claim is about touches, so the scrim taps are sampled: five
  close-and-reopen cycles at each rotation, each at a different scrim point spread across the scrim's bounds.
- **Item 3. Pass:** with the drawer open, a horizontal swipe on the sheet towards its own edge (rightward at
  ROTATION_90) closes it; with it closed, a swipe from near the rail's inner edge across the map towards the centre
  does not open it (no sheet nodes after), and the map may pan instead.
- **Item 4. Pass:** with the drawer open at ROTATION_90, `user_rotation 3` leaves a drawer that is either still open,
  now against the left (port) edge with its rounded edge facing right, or closed cleanly with Tools still working;
  no crash, and no sheet stranded half-open or on the wrong edge. I record which. The source asks only that it be
  exercised ("the direction around it changes while open"), so a clean close is recorded, not called a fail.
- **Item 5,** predictive back, is **not run**: it needs gesture navigation, and the navigation mode may not be changed.
- **Prediction:** 1 to 4 pass at ROTATION_90 (the planner's prediction 1), and 2 and 3 at ROTATION_270 too. For 4, the
  drawer stays open and moves to the left edge, since `drawerState` is remembered and only the direction changes.

## Step 3: cut-out clearance and the cap, B3 items 6 and 38, J5 L8 (B3x:210-214, J5:263-266)
Code: `shortLandscapeContentInsets()` (`AvailabilityNavigationUi.kt:238-241`: `statusBars` top, `displayCutout`
horizontal, `ime` bottom) on every tab but Map; the rail takes the `navigationBars` inset on its own side (`:209`);
the content is capped at 640 dp (`READABLE_CONTENT_MAX_WIDTH`, `AvailabilityResultsUi.kt:306`) and centred in what
the rail leaves (`AvailabilityCompactScaffold.kt:653-668`).
- **Destinations:** List, Seasonal, Journal Entries (Timeline and Album), Journal Records (All, and each chip), at
  ROTATION_90 and ROTATION_270. Tools is the drawer (step 2), and Map is excluded by the source.
- **Pass (L8):** no clickable or focusable node's bounds overlap the cut-out band, the strip of the window between the
  cut-out edge and the cut-out inset (from step 1), at either rotation. This includes the L1 row's Entries | Records
  switch (row start), its search icon and its action button (row end). A node is compared by its bounds from a fresh
  dump.
- **Pass (the cap and centring):** the content column is 640 dp wide (1800 px, within 3 px) and its left and right
  margins, measured to the cut-out inset on one side and to the rail's inner edge on the other, are equal within 3 px.
- **Prediction:** pass at both rotations (the planner's prediction 2). The column is 640 dp with about 14 dp either
  side, from 823.5 dp less the rail (80 dp plus the 48 dp bar inset) and the cut-out inset (about 27 dp).

## Step 4: item 7, the landscape picker maps (B3x:215-216), an observation
- **What is recorded:** for the Offline Maps picker (Records, Offline maps), the find's location picker (the draft
  find's form, "Change Location", left with Cancel) and the entry map preview (the L0a entry's report), each map's
  bounds in dp from a dump, and whether it is usable: whether it is on screen whole or needs scrolling to reach, and
  whether OK and Cancel can be reached. By my reading of screenshots, said so.
- **Code:** the offline picker's map is 4:3 by width (`AvailabilityOfflineMapsUi.kt:174-186`, `:269`), so at 640 dp
  wide it is 480 dp tall, taller than the 384 dp window; the find picker's map is `weight(1f)` of what is left
  (`CentrePinLocationPicker.kt:142-148`).
- **Prediction:** the offline picker map does not fit the window (about 640 x 480 dp inside a scrolling panel), so
  its OK row needs a scroll; the find picker map is about 640 x 250 dp and usable; the entry preview reads.

## Step 5: J5 items 39 to 46, the Journal in landscape
- **39 (J5:267; the height table at J5:151-163). Record:** at each rotation, the L1 row, the L3 row and the content
  area in dp, and the first card rows' bounds. **How many cards show:** the Entries list holds two saved entries, the
  L0a card and the blank collapsed row, so it cannot show six. The five drafts' list ("Continue" through the drafts
  chip) draws with the sideways cards too (J5 L4: "the drafts list takes it too"), so the count is made there: whole
  and partial cards, and the number of whole two-card rows the content area holds, from the row pitch. **Pass
  (P:327):** six or more whole cards would show given the cards. **Prediction:** fewer than six whole (the planner's
  prediction 3): two whole rows, four whole cards, and part of a third row, with the status bar taking about 27 dp
  more than Robolectric's zero.
- **40 (J5:268). Record:** the L1 row's search icon reveals the app-wide search header (it appears in the next dump),
  and a second touch on it ("Hide search") and Back each hide it. The dropdown: with the header revealed, its search
  dropdown opened (nothing chosen), then the header hidden by the icon: **pass** if the dropdown's nodes are gone
  with the header (`AvailabilityCompactScaffold.kt:698-702`, the `LaunchedEffect` J5 added). The feel is
  the owner's.
- **41 (J5:269). Record:** on a list only slightly taller than its viewport (the drafts list, or Records All, whichever
  overflows by less), a small scroll down hides the second row (the L3 toggle row or the chip row) and a scroll up
  brings it back, and whether the list can still reach its end. The animation's feel is the owner's.
- **42 and 31 (J5:270, J4b:325). Pass:** a long-press (`motionevent` DOWN, hold 800 ms, UP) on the sideways card or
  tile lowest on screen opens its Edit and Delete menu, and the menu's bounds lie inside the window, above no system
  bar; dismissed with Back, nothing chosen. On a draft card or the L0a card, never on an owner's record.
- **43 (J5:271). Pass:** Album view, the L1 row's photo button opens Take photo and Import; Import opens the system
  picker (package from `dumpsys activity`); with the picker up, `user_rotation` 1 to 3 and back to 1; then the picker
  is cancelled with Back, and the app is back on the Journal's Album view with no crash and nothing imported
  (`files/photos` count unchanged). The `ACCESS_MEDIA_LOCATION` prompt Part A found behind the picker is dismissed
  with Back, choosing neither button, as Part A did.
- **44 (J5:272, P:323). Pass:** with the blank saved entry open in its editor (through its long-press menu's Edit),
  `user_rotation` 1 to 3 and then 3 to 1: the editor is still open after each, on the same entry (same date, blank text,
  "Save" not "Finish entry"), and leaving it with Back gives no prompt. Then the draft find open in its form
  (Records, Finds, its Drafts), the same two rotations: the form stays open with its identification label intact.
  Nothing is typed in either. The focused window's id is read before and after each turn, since rotation is not
  expected to recreate the Activity (J1:126-129). The database is compared before and after (counts and the two rows).
- **45 (J5:398-399). Pass:** on Records at font scale 1.0 and 1.15, landscape, each chip's label is whole on one line,
  and if the row overflows the capped width it scrolls sideways. Widths in dp recorded.
- **46 (J5:400), an observation:** the chip icons' measured size and, by my reading of a 2x crop, whether each is
  legible in the current (dark) theme.
- **Prediction:** 40's dropdown closes with the header; 41 hides and returns; 42's menu is inside the window; 43 and
  44 keep their state; 45 passes at 1.0 and scrolls at 1.15.

## Step 6: J5c items 47, 48, 49 and 53, the details sheet in landscape (J5c:319-328)
Code: `RecordDetailsSheet.kt:162-182`, a `ModalBottomSheet` with `skipPartiallyExpanded = true`, its content a
`verticalScroll` column. Its insets are Material's defaults (material3 `1.5.0-alpha26`; which insets those are is
unverified: I have no source for that version here).
- **The row:** "DEVICE CHECK waypoint" in Records, Waypoints (L0a test data), a tap on its body; and the owner's
  track row, a tap only (the sheet opens; nothing else is done with it).
- **47. Pass:** at both rotations, no clickable node of the sheet lies within the navigation-bar frame or the cut-out
  band from step 1, and the sheet's top is below the status bar.
- **48. Pass:** the sheet's actions (Directions for the waypoint, Share for the track) are below the visible part when
  it opens, and a swipe up inside the sheet brings them on screen (the content scrolls). Neither action is pressed.
- **49. Record:** a downward drag from the drag handle dismisses the sheet (no sheet nodes after), or not. The
  predictive Back animation is **not run** (gesture navigation).
- **53. Pass:** with the sheet open, `user_rotation` 1 to 3: the sheet is still open, on the same record, after the
  turn.
- **Prediction:** pass for 47, 48 and 53; a drag down dismisses (49).

## Step 7: picker items 34, 35 and 36 (PF:299-310, ODR:5-11, emulator-check-attempt.md:18-19)
The find picker is the draft find's form, "Change Location" (`JournalTab.kt:364-375`); it is always left with
**Cancel**, never OK, so the draft find is not changed. Its region is `findLocationPickerRegion(deviceLocation, …)`
(`FindLocationPickerRegion.kt:21-22`), the device's live fix at 1 km, from `AvailabilityUiState.liveFix`, which the
ViewModel collects from `AndroidLocationTracker` (GPS and network, 1 s; `AndroidLocationTracker.kt:71-74`). Each fix
is logged under the tag `ForagerFix` with its provider (`:53-60`), so the logcat, read with `-d`, shows fixes
arriving.
- **The fix-arrival control first. Pass:** with the find picker open, `dumpsys location` shows recent fixes for the
  app's registrations, and `logcat -d -s ForagerFix` shows new lines, with their providers, during the check.
- **35, the first-fix follow. Pass (PF:305-307):** location off, the picker opened, location on; untouched, the
  picker moves to the device when the fix arrives (its "Pin at:" text changes to the new fix, and the map with it).
  **A weakness, stated before looking:** `liveFix` keeps the last accepted fix while location is off (nothing clears
  it; `AvailabilityViewModel.kt:233-244`), and this process has had fixes since 02:18Z. So the picker opens already at
  the device's last fix, and the "move" on the next fix is only GPS jitter on a phone lying still. The check can
  only see that "Pin at:" keeps following new fixes while untouched; it cannot see a move from somewhere else to the
  device. Nothing here can empty `liveFix` except a new process.
- **34, pinch and pan, then wait. `input motionevent` takes one pointer, so adb cannot pinch** (`adb shell input`
  usage: `motionevent <DOWN|UP|MOVE|CANCEL> <x> <y>`). **Substitute:** double taps at the pin to zoom in (each a
  gesture MapLibre reports as `REASON_API_GESTURE`, `SightingsMap.kt:399-403`), then a slow one-finger pan made of
  `motionevent` DOWN, MOVEs and UP with a pause before UP (no fling). The zoom is measured, not assumed: from a pan of
  a known number of px and the change in "Pin at:" longitude, zoom = log2(360 x (pan in dp) / (512 x Δlng)), the Web
  Mercator scale MapLibre uses. **Pass (PF:301-304):** after the pan, over 10 s with fixes arriving (the control
  above), the view does not drop to zoom 13 and does not return to the device: "Pin at:" stays at the panned value,
  and two screenshots 10 s apart show the same map. Coordinates are kept out of this file; only differences are
  given.
- **36, the offline picker after a slow first fix. Pass (PF:308-310):** opened with GPS cold, a pan before the location
  lands, then nothing moves; the radius slider dragged, the map stays on the panned point and re-zooms; OK and
  "Download region" show the panned point. **Whether it can be run:** a cold start can be requested from adb (`cmd
  location providers send-extra-command gps delete_aiding_data`, listed in `cmd location help`). But the offline
  picker's first fix is `getCurrentLocation()`, which races GPS against the network provider
  (`AndroidLocationProvider.kt:23-40`, `:50-52`), so a cold GPS alone does not make that fix slow; and the picker's
  opening centre is the remembered L0a pick, which lies 0.7 m from the L0a waypoint, beside the phone, so a late fix
  would move it by metres at most. I will send the cold-start command once, open the picker, pan at once, and record
  what happens, saying that the check could not have failed on this data unless the map visibly jumps.
- **Prediction:** fixes are arriving and 35 passes as far as it can see (the planner's prediction 4); 34 passes; 36
  shows nothing moving, on data that could not show a failure.

## Step 8: item 37, a download kept while the Offline maps list is reopened, with one force-stop (PF:311-314)
Code: `MapLibreOfflineMapRepository.kt:107-175` at `2458934`: the MapLibre region is created at the start and the Room
row written only on completion; `listRegions` deletes incomplete regions other than this process's in-flight ones
(`:182-216`, F4). `onDownloadOfflineMaps` (`AvailabilityViewModel.kt:1082-1140`) shows `Downloading(n, m)`.
- **The one download:** at the picker's remembered centre, confirmed with OK, a radius chosen so that the estimate
  label reads a small download that still takes long enough to leave and return, and the name
  `DEVICE CHECK 2026-09-28 B`. **Pass, first half:** while it runs, switching from the Offline maps chip to All and
  back shows "Downloading N / M tiles" still counting up (N larger than before the switch).
- **The force-stop,** once, with `am force-stop com.zynergylabs.forager.app` while N < M. Then the app is relaunched
  from the launcher intent (`monkey -p … -c android.intent.category.LAUNCHER 1`) and Records, Offline maps reopened.
  **Pass, second half:** the list shows the L0a region "DEVICE CHECK" and no partial region; the Room
  `offline_regions` table has one row; MapLibre's own offline database (read with `run-as`, copied, never written)
  holds no incomplete region for the new name.
- **Not run:** the half where the region "appears in the list on completion", since the one download allowed is the
  one force-stopped.
- **Prediction:** pass for both halves run; the partial region is gone after the relaunch.

## Owner items, not attempted
Feel and animation (parts of 40, 41 and 49); TalkBack speech; predictive back (5, 49) and anything else needing
gesture navigation (38's gesture-navigation half, P:326); the wide tree; folds.

## Settings to restore
`accelerometer_rotation` 0 and `user_rotation` 0 (read at the start); `font_scale` 1.0; location on; the Maps tab as
found. Each is read back at the end.
