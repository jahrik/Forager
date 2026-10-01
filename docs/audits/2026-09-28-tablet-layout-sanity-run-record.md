# Tablet layout sanity check before J6, on the owner's tablet (SM-X800): run record

**Status: pre-registration only.** No Forager screen has been looked at on the tablet yet, and the new build is not
installed. This file is committed and pushed before any measurement, so the order of prediction and observation can be
checked. Measurements are added in later commits; this section stays unchanged.

**Date:** 2026-09-28, from 21:00Z.
**Dispatch:** `prompts/preserved/2026-09-28-74.md` (quoted verbatim in the Appendix). Launched by the planner's
continuation `2026-09-28-75` (quoted verbatim below). The intent `2026-09-28-74` and every `RECORD.md` entry are the
planner's. I write none (see Decisions I made, 1).
**Base:** `origin/journal-redesign` at `d7cc9f5`, confirmed with `git fetch` at the start. The branch head was
`ab7a653`, the planner's launch note, which changes only `RECORD.md`. `git diff d7cc9f5 99de6c2 -- app/` is empty, so
`app/` is J8's terminal. Branch `device-tablet`, worktree `/home/zynergy-labs/Zynergy/forager-wt/device-tablet`, cut
from `d7cc9f5`.
**Code citations** are at `d7cc9f5`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in
full. The pulse cites `14ea159`, and some lines have moved since: `PERMANENT_DRAWER_WIDTH` is AWL:90 here (pulse: :88).
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`, outside the repository. No
screenshot, dump, coordinate, note text, place name or photo goes into this file (`-04`'s privacy rule, carried by
`-51`).

## The device (read before the install; no Forager screen looked at)

- **Attached devices** (`adb devices -l`, at the start): `R52T506412L` (`model:SM_X800`, `product:gts8pwifixx`) and
  `R5CT321008R` (`model:SM_S908U`, the S22). Exactly one non-S22 device, as the launch message said. Every adb call in
  this run names `-s R52T506412L`, and my helper scripts are checked to contain no other serial.
- **Identity** (`00-device-facts.txt`): `ro.product.model=SM-X800`, `ro.product.manufacturer=samsung`,
  `ro.product.name=gts8pwifixx`, `ro.product.device=gts8pwifi` (a Galaxy Tab S8+, Wi-Fi). Build
  `ro.build.fingerprint=samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZE1:user/release-keys`,
  Android 16, SDK 36.
- **Display:** `wm size` 1752 x 2800 (physical, no override); `wm density` 340 (physical, no override;
  `display_density_forced=null`). So 1 dp = 2.125 px, and the natural (portrait) display is **824.5 x 1317.6 dp**.
- **At the start** the tablet was at rotation 90 (landscape, `mCurrentRotation=ROTATION_90`, 2800 x 1752), with the
  display configuration `sw824dp w1318dp h824dp`, `night` (system dark mode on). Insets at 90: `statusBars` 64 px at the
  top (30.1 dp), `navigationBars` 32 px at the bottom (15.1 dp), no `displayCutout` source, and no left or right inset
  (the side sources are `systemGestures` only).
- **Unlocked:** `isKeyguardShowing=false`, `mWakefulness=Awake`. The launcher had focus, and no prompt was showing.
- **Clock:** the tablet's `date` read `Mon Sep 28 14:04:53 PDT 2026` against the host's 21:04:53Z (UTC−7,
  `America/Los_Angeles`).
- **Settings at the start** (`03-settings-start.txt`): `user_rotation=1`, `accelerometer_rotation=1` (auto-rotate on),
  `font_scale=1.0`, `display_density_forced=null`, `navigation_mode=2` (gestures), `stay_on_while_plugged_in=15`,
  `screen_off_timeout=15000`, `window_animation_scale=1.0`.
- **"Stay Awake" is the owner's setting.** The planner's note below says the owner turned it on, before or during this
  run. `stay_on_while_plugged_in=15` was already set when I first read it (21:03Z). I leave it as it is and do not list it
  for restore.
- **Crash buffer at the start** (`02-crash-start.txt`): 19 lines, 2 FATAL EXCEPTIONs, both from
  `com.mgoogle.android.gms:persistent`, dated 08-10. **0 lines name Forager.**

## Install facts (before the install)

- **Installed package** (`01-dumpsys-package-start.txt`): `versionName=1.0.1279+gce8ddbef`, `versionCode=1279`,
  `pkgFlags=[ DEBUGGABLE HAS_CODE ALLOW_CLEAR_USER_DATA ]`, `firstInstallTime=2026-09-28 04:30:35` and
  `lastUpdateTime=2026-09-28 04:30:35` (tablet time, PDT: 11:30:35Z), installer `com.google.android.packageinstaller`.
  Every runtime permission is `granted=false` (location, camera, notifications, media). The commit `ce8ddbef` is in no
  local clone and on no remote ref, so its database version comes from the backup, below.
- **Installed certificate:** its base APK, pulled read-only (`installed-before.apk`, sha256
  `12058170472d291e6fb61278ae322ba6352ea7fb0ad25240c50cf41ba661406e`), signer `CN=Android Debug, O=Android, C=US`,
  certificate SHA-256 `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626` (`04-installed-before-cert.txt`).
- **Memory and disk before the build** (`05-build-start.txt`): 3003 MB available, 8.9 GB free on `/`. A Gradle daemon and
  a Kotlin daemon from other sessions were already running. During the build, available memory fell to 2080 MB.
- **Build:** `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug` in the new worktree. `exit=0`, 40 tasks executed, 0 `e: `
  or `error:` lines, ended 21:03:19Z (`05-build.log`).
- **APK** (`06-apk-facts.txt`): `tablet-debug.apk`, a copy of `app/build/outputs/apk/debug/app-debug.apk`, sha256
  `0da6c533e2a9a96341bfb17e91ab60f53fec47a82ed47f5df76b662f199ec27c`; `versionName=1.0.1416+gd7cc9f5b`,
  `versionCode=1416`; signer `CN=Android Debug`, certificate SHA-256
  `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`. **Same certificate as installed: no mismatch.**
  1416 is above 1279, so `install -r` needs no `-d`.

## Database backup (owner "2 B"; before the install)

- The app was running in the background (pid 15584). `am force-stop` at 21:03:47Z; pid gone afterwards
  (`07-force-stop-time.txt`).
- `run-as` works (the package is debuggable). On the tablet, `databases/` held `forager.db` (4096 B),
  `forager.db-wal` (226632 B) and `forager.db-shm` (32768 B), all dated 04:30 PDT.
- **Copied** with `exec-out run-as … cat` into `db-backup-raw/` and never opened. A second copy in `db-backup-query/` is
  the one read (`dbcopy.sh`, `08-db-backup-copy.txt`). **The device-side and local sha256 match for all three files:**
  - `forager.db` `94772575633b6f43be37fdc3bc98e81d25f0b53e7098b5f21d547a0f415a894c`;
  - `forager.db-wal` `5a098d4647f8b99c35750a686aa10accd4638e0bba08fa0b689fabe99037cbb6`;
  - `forager.db-shm` `548fa158008c9f708eecd06f18e2071c9a6071dd296bc35161b10d4dd5760f6b`.
- **Verified on this machine** (`dbverify.sh`, `db-backup-verify.txt`): the header is `SQLite format 3\0`, page size 4096;
  the WAL magic is `377f0682`; `PRAGMA integrity_check` is `ok`; **`PRAGMA user_version` is 15**; `journal_mode` is `wal`.
- **Row counts** (every table in `sqlite_master`): `android_metadata` 1, `room_master_table` 1, and **0 in every other
  table**: `cached_searches`, `cartography_entries`, the five `cartography_entry_*_refs`, `log_entry_photos`,
  `log_photos`, `mushroom_log_entries`, `offline_regions`, `planned_trips`, `sqlite_sequence`, `track_points`, `tracks`
  and `waypoints`. Per-table digests are in `db-backup-digest.txt`. **The Forager database on this tablet holds no
  records.**
- **Other app files** (`09-app-files-start.txt`): `files/captures/` is empty; `files/maplibre-offline/` is empty;
  `mbgl-offline.db` is 65536 B; there is no `files/datastore/`. `shared_prefs/` holds only
  `MapboxSharedPreferences.xml` and `android.app.ActivityThread.IDS.xml`.
- `user_version` 15 is below 16, so the install migrates (`MIGRATION_15_16`, J8). Step 6 checks it afterwards.

## Predictions, written before measuring

From the display figures above, the code at `d7cc9f5` and the pulse's section 6. **Nothing here has been seen on the
tablet.**

**Which tree.** `currentWindowWidthClass()` reads `LocalConfiguration.screenWidthDp` (`ui/adaptive/WindowWidthClass.kt:39-46`)
and `isShortWindow()` reads `screenHeightDp < 480` (`ui/adaptive/ShortWindow.kt:14, 25`). The branch is
`if (windowWidthClass == WindowWidthClass.COMPACT || isShortWindow)` at AS:1755 at this base (pulse: AS:1690).
- **Portrait (rotation 0):** expected configuration `w824dp h1318dp`. 824 is MEDIUM (600 to 839), and 1318 is not short.
  **Wide tree.**
- **Landscape (rotation 90):** configuration `w1318dp h824dp` (read at the start, before the app ran). EXPANDED, not
  short. **Wide tree.**
- **Rotation 270:** the same as 90. No cutout source was listed at 90, and the navigation bar is at the bottom. I read the
  insets at 270 and measure it in full only if a bar or cut-out differs by side.

**Pane widths.** Compose rounds each dp size to whole px, so 360 dp is 765 px and the 1 dp `VerticalDivider` (AWL:133)
is 2 px.

| Pane | Source | Portrait, predicted | Landscape (90), predicted |
|---|---|---|---|
| Drawer (Journal and the other panels) | `PermanentDrawerSheet` at `Modifier.width(PERMANENT_DRAWER_WIDTH)`, AS:1840, AWL:90 | 360 dp (765 px), full height | 360 dp (765 px), full height |
| Species list | `COMBINED_PANE_LIST_WIDTH`, AWL:131, 155 | 360 dp (765 px) | 360 dp (765 px) |
| Divider | `VerticalDivider()`, AWL:133 | 1 dp (2 px) | 1 dp (2 px) |
| Map | `Modifier.weight(1f)`, AWL:149 | 1752 − 765 − 2 − 765 = **220 px = 103.5 dp** | 2800 − 1532 = **1268 px = 596.7 dp** |
| Pulse's formula for the map, W − 721 dp | pulse section 6 | 824.5 − 721 = 103.5 dp | 1317.6 − 721 = 596.6 dp |

- **Heights** are not in the pulse. Structurally, the drawer spans the full window height, with the bars padded inside it
  (`DrawerDefaults.windowInsets`, inferred from M3, not read in this version's source). The list and map span from below
  the search bar, summary and the List | Maps | Seasonal row to above the navigation bar (`Scaffold` padding, AS:1521-1527).
  I predict no numbers for them.
- **Chrome on a 103.5 dp map (portrait).** The Layers button is 48 dp at `TopEnd` with 8 dp padding (AWL:337-341), so it
  spans map-x 47.5 to 95.5 dp. The chip row is a `FlowRow` at `TopCenter`, padded 8 dp (AWL:320-336), so at most 87.5 dp
  wide. **Prediction:** any chip there is squeezed below its natural width and overlaps the Layers button. The add button
  (AWL:374-380) and the legend chip (AWL:357-366) take the bottom-right corner.
- **Map usable in portrait?** **Prediction:** a glyph inside the strip opens its bubble (the tap handling is the same as
  on any width), but the bubble is wider than the strip and is clipped at the map's edges. This is inferred; the pulse says
  nothing about it.
- **The Journal in the drawer:** 360 dp wide in both orientations. The Entries list is 360 dp.
- **An entry's report map:** `Modifier.fillMaxWidth().aspectRatio(4f / 3f)` on a tall window
  (`ui/log/CartographyEntryReportScreen.kt:421-430`), with no horizontal padding between the drawer and the report
  (`ui/log/LogPanel.kt:421-444`). **Prediction: 360 x 270 dp (765 x 574 px)** in both orientations.
- **A record's details sheet:** a `ModalBottomSheet` with no `sheetMaxWidth` (`ui/log/RecordDetailsSheet.kt:163-167`).
  M3's default maximum is 640 dp (`BottomSheetDefaults.SheetMaxWidth`; from M3's API as I know it, **not read** in
  1.5.0-alpha26's source, which the Gradle cache holds only as an `.aar`), centred in the whole window, over a scrim.
  **Prediction:**
  - portrait: 640 dp (1360 px) wide, at x = 196 to 1556 px (92 to 732 dp). It covers the drawer from 92 dp rightward, the
    whole list and the first 11 dp of the map;
  - landscape: 640 dp, at x = 720 to 2080 px (339 to 979 dp). It covers the drawer's last 21 dp, the whole list and
    258 dp of the map.
  It draws over the drawer, list and map alike, not inside the drawer.
- **The album:** in the drawer, 360 dp, with `EXPANDED_GRID_COLUMNS` columns (`ui/log/LogPanel.kt:394, 480`; the pulse
  gives 3 columns, about 104 dp a cell, inferred).
- **A photo in the viewer:** `PhotoViewerDialog` is a `Dialog` with `usePlatformDefaultWidth = false` and
  `decorFitsSystemWindows = false` (`ui/log/PhotoViewerDialog.kt:131`). **Prediction: the whole window**, 824.5 x 1317.6 dp
  in portrait and 1317.6 x 824.5 dp in landscape.
- **The old "Photo Gallery" panel:** a drawer panel, so 360 dp.

**A premise refined.** The dispatch says a species search is needed for the wide map to show (AWL:228-231). The gate is
`!uiState.hasSearched` at AWL:230, and `hasSearched` is `region != null` (`ui/availability/AvailabilityUiState.kt:310`).
What the map needs is a region chosen in search options. The message reads "Choose a region in search options to see
mapped sightings." (AWL:231).

## Method

- **Captures:** `snap.sh NAME` saves `NAME.xml` (`uiautomator dump`) and `NAME.png` (`screencap`), and logs the UTC time,
  `user_rotation` and both hashes in `snaps.log`. **It refuses to capture when the focused window is not Forager's**, so a
  prompt is never captured (owner "4 A").
- **dp from dump bounds:** width = (right − left) / 2.125, and likewise for height.
- **Touches are real:** `adb -s R52T506412L shell input tap` at coordinates from a fresh dump.
- **Rotation:** `settings put system accelerometer_rotation 0` and `user_rotation 0/1/3`, restored at the end to
  `accelerometer_rotation=1` and `user_rotation=1` and read back. No other system setting is changed. Density, font scale
  and "Stay Awake" are never touched.
- **The planner's note** (quoted below) arrived before any Forager screen was looked at.

## The launch message, verbatim

> This is planner dispatch `2026-09-28-74`, **the tablet layout sanity check before J6**, launched by `RECORD.md` entry `2026-09-28-75`. Its store copy is committed at `prompts/preserved/2026-09-28-74.md` on `origin/journal-redesign`. Fetch it, read it in full and quote it verbatim in your run record; the file governs over this message.
>
> **Base:** `origin/journal-redesign` at `d7cc9f5`. Its `app/` is identical to `99de6c2`, the J8 terminal. Build in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-tablet`, on branch `device-tablet` from that base.
>
> **Devices at launch (the planner's `adb devices -l` read):**
> - `R52T506412L`, model `SM_X800`, product `gts8pwifixx`: **this is the tablet, your device.** Confirm with `getprop` yourself.
> - `R5CT321008R`, model `SM_S908U`: **the S22. Another coder is running J8's device check on it. Never touch it.** Pass `-s R52T506412L` on every adb command.
> - If `adb devices` shows anything different when you start, stop.
>
> **What the owner ruled, and what it means for you:**
> - "2 B": Forager is already on the tablet. Install over it and keep its data. First read the installed package, compare signing certificates (stop on a mismatch), and **back up and verify the database through `run-as`** (stop if you can't). `install -r` only. If it migrates, verify version 16, integrity and equal row counts.
> - "3 A": create a few items labelled "DEVICE CHECK 2026-09-28 T" and leave them. Never open, edit or delete the owner's own records on the tablet.
> - "4 A": at any system, Google or first-run prompt, stop at once without touching it. Hand back to the planner, naming the prompt; the owner taps through, and you are resumed.
>
> **The measurements:** in each orientation, measure every wide-tree pane in dp from dump bounds. Write your predictions from the J6 pulse's arithmetic before measuring. Give a predicted-against-measured table, with captures. **Do not rule on "how much of an issue"; the owner judges.** Change no device-wide display settings. Restore rotation and anything else you change.
>
> **Sharing the machine:** the save-failure coder runs Gradle, and J8's device coder builds an APK. Check `df` and free memory before `assembleDebug`, and wait while under 2.5 GB is available.
>
> **Output:** evidence goes outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`. The run record is `docs/audits/2026-09-28-tablet-layout-sanity-run-record.md` on `device-tablet`, pushed as you go. The planner merges it and writes the record; you do not touch `RECORD.md`. Merge is not authorised.
>
> When you finish or stop (including for a prompt), hand back a report: device facts, install and backup figures, created items, the measurement table, captures by name, what was restored, crash reads, decisions you made and flags.

## The planner's note during the run, verbatim

Received at about 21:02Z, after the device reads had started and before the install. No planner-log line was given
with it.

> Planner note for the tablet check (2026-09-28-74), no reply needed: the owner has turned on "Stay Awake" (Developer options) on the tablet, so it won't sleep. The owner set it, so leave it as it is: don't turn it off and don't list it as something to restore. Record it in the run record as the owner's setting, made before or during your run. Carry on as you were.

## Install, migration and launch (steps 5 to 7)

- **Install:** `adb -s R52T506412L install -r tablet-debug.apk`, 21:06:55Z to 21:06:59Z: `Performing Streamed Install`,
  `Success` (`10-install.txt`). No uninstall, no `-d`, no data clear.
- **After** (`11-dumpsys-package-after.txt`): `versionName=1.0.1416+gd7cc9f5b`, `versionCode=1416`,
  `lastUpdateTime=2026-09-28 14:06:59` (PDT). **`firstInstallTime=2026-09-28 04:30:35`, unchanged**, with the same
  `ceDataInode=536500` and `deDataInode=548428`. Only user 0 holds the package.
- **First launch:** `am start -W -n com.zynergylabs.forager.app/.MainActivity` at 21:07:13Z, `Status: ok`,
  `LaunchState: COLD` (`12-launch.txt`). Focus went to Forager's `MainActivity`, and it was Forager's only window: no
  system, Google or first-run prompt.
- **Crash read after launch** (`14-crash-after-launch.txt`): 19 lines, the same two 08-10 `com.mgoogle.android.gms`
  entries as at the start, **0 Forager lines**. The only "Migration" lines in the log are Samsung's own
  (`MigrationParser`, `[#CMH#]`).
- **Migration check:** `am force-stop`, then the same copy and verify as the backup (`15-db-after-copy.txt`,
  `db-after-verify.txt`, `db-after-digest.txt`):
  - device and local sha256 match: `forager.db` `921e74f5…d7e69`, `-wal` `92b12ed6…fb405`, `-shm` `fd4c9fda…389eb`;
  - **`user_version` 16**, **`integrity_check` `ok`**, the header valid;
  - **every table's row count equals the backup's** (all 0, apart from `android_metadata` 1 and `room_master_table` 1);
  - `cartography_entries` gained `shownOnMap INTEGER NOT NULL` (`PRAGMA table_info`);
  - every per-table digest is identical except `room_master_table`, whose one row is Room's schema identity hash and
    changes with the version.
  - The WAL header now reads zeros (the WAL was reset after a checkpoint), so I also opened `forager.db` alone
    (`db-after-mainonly/`): `user_version` 16, `integrity_check` `ok`, 18 tables, the same as the backup.
- **Second launch** after the check: 21:08:14Z, `COLD`, Forager's window only. Rotation was locked at the same time:
  `accelerometer_rotation 0`, `user_rotation 1` (`16-rotation-log.txt`).
- **App window at 90** (`17-window-r1.txt`): `Requested w=2800 h=1752`, `mBounds` and `mAppBounds` `(0,0,2800,1752)`,
  `fullscreen`, configuration `sw824dp w1318dp h824dp`. So 1317.6 x 824.5 dp, as predicted.

## Created items (owner "3 A"), all left on the tablet

All through the app's own UI, with real taps, at rotation 90 (`19-created-items.txt`):

1. **A search region**, which the wide map needs (AWL:230). In the drawer's Search panel, under "Advanced search", I
   typed a latitude and longitude and tapped "Search this location". The location is a public national-park visitor
   centre, chosen by me (Decisions I made, 3). The value is in `18-search-location.txt`, not here. The search reported
   293 historical iNaturalist observations of Fungi within 5 mi for September. This writes a search-cache row, which
   the closing inventory counts.
2. **A waypoint**, 21:10:40Z: map "+", "Drop a waypoint", the centre pin, OK, then the name "DEVICE CHECK 2026-09-28 T"
   (the default "Waypoint 1" replaced), "Drop waypoint".
3. **A find**, saved 21:12:42Z: map "+" (after a short pan, so it is not on the waypoint), "Log a find", OK. The find
   editor opened in the drawer. Identification "DEVICE CHECK 2026-09-28 T", Description Notes
   "DEVICE CHECK 2026-09-28 T", then Save. It is listed under Records, Finds, Log.
   - One slip: my first tap for the notes field hit the on-screen keyboard's "a" key, so the identification briefly read
     "DEVICE CHECK 2026-09-28 TaDEVICE CHECK 2026-09-28 T". I cleared it and retyped it before saving. It was never
     saved that way.
4. **A journal entry**, committed 21:14:33Z: Cartography, "New entry", "Your own account" set to
   "DEVICE CHECK 2026-09-28 T". The day's find and waypoint were offered as candidates, both kept by default, and I left
   both kept (Decisions I made, 4). Then "Finish entry", which commits the draft and stays on the edit screen
   (`ui/log/CartographyEntryEditScreen.kt:81-86`). The entries list shows it, with "1 find" and "1 waypoint".

No photo was created: the dispatch's list names none, and Forager on this tablet holds none (see "Not yet measured").

The Forager database held no records before this run, so none of the owner's records were there to open, edit or delete.

## Measurements at rotation 90 so far (landscape; wide tree)

dp = px / 2.125. Captures are in the evidence directory, named below.

| Pane | Predicted | Measured (dump bounds) | Capture |
|---|---|---|---|
| Tree | wide | **wide** (`PermanentDrawerSheet` at the left, the main scaffold beside it) | `20-launch-r1`, `24-r1-maps-pane` |
| Drawer | 360 dp (765 px), full height | **[0,0][765,1752]: 360.0 x 824.5 dp** | `24-r1-maps-pane` |
| Main scaffold (search, summary, tabs, results) | the rest | [765,0][2800,1752]: 957.6 x 824.5 dp | `24-r1-maps-pane` |
| Species list | 360 dp (765 px) | **765 to 1530 px: 360.0 dp** (inferred from the drawer's right edge and the map's left edge; the list's scrolling column is [799,525][1496,1720], 16 dp inside each edge), from y 405 to 1720 | `24-r1-maps-pane` |
| Divider | 1 dp (2 px) | 1530 to 1532 px, 2 px (inferred from the same edges) | `24-r1-maps-pane` |
| Map | 1268 px = 596.7 dp | **[1532,405][2800,1720]: 596.7 x 618.8 dp** | `24-r1-maps-pane` |
| Map's Layers button | 48 dp at the top end, 8 dp in | [2681,422][2783,524]: 48.0 dp, 8.0 dp from the top and right | `24-r1-maps-pane` |
| Map's add button | bottom end | [2681,1601][2783,1703]: 48.0 dp, 8.0 dp from the bottom and right | `24-r1-maps-pane` |
| Entries list (Journal, Cartography) | 360 dp | **360 dp column; the one entry card is [34,464][255,753]: 104.0 x 136.0 dp**, a 3-column grid. Its title text gets [162,481][238,583] (35.8 dp wide) and draws as "DEV / IC…"; its "1 find" and "1 waypoint" counts get 24 px (11.3 dp) each | `40-r1-entries-list` |
| Entry report map | 360 x 270 dp (765 x 574 px) | **[0,404][765,978]: 360.0 x 270.1 dp** | `41-r1-entry-report` |

Also seen, not measured further: in the drawer's Records tab, the chip row (All, Finds, Tracks, Waypoints) is wider than
360 dp, and its "Waypoints" chip is cut at the drawer's edge, [668,277][765,379] (`31-r1-find-editor`, `34-r1-find-saved`).
It scrolls sideways (`HorizontalScrollView`, `scr=t`).

## Interruption: the owner operated the tablet (21:23Z), and the planner's resumes

- **21:15:3xZ.** I opened the entry's options menu (for J8's chip) and ran `snap.sh`. It **refused**, because focus was on
  a window titled "Pop-Up Window". My network connection then dropped (EAI_AGAIN), and the planner resumed me (message 1
  below).
- **On resuming** I read `dumpsys window`. The focused window was `Pop-Up Window`, `package=com.zynergylabs.forager.app`,
  `mOwnerUid=10352`, `ty=APPLICATION_SUB_PANEL`: Forager's own `DropdownMenu` ("Edit entry", "Show on map",
  "Delete entry", `42-r1-entry-menu.xml`). The worktree was clean and level with `origin/device-tablet` (`9c4926f`).
  Following the planner, I dismissed it with one Back. Focus returned to `MainActivity`.
  - `snap.sh` now checks the focused window's owning package (`package=` in `dumpsys window windows`), not its title. So
    Forager's own popups pass, and any other package's window is still refused. The old version is kept as `snap.sh.v1`.
- **Straight after**, a capture was refused again: focus was on `com.android.settings/.homepage.SettingsHomepageActivity`.
  I sent no further input and read the events log (`44-events-foreign-input.txt`, times PDT + 7 h = UTC):
  - Settings resumed at 21:23:16Z (Connections, then sub-pages), back to Forager at 21:23:20Z;
  - Settings again from 21:23:25Z to 21:23:33Z;
  - Recents at 21:23:34Z and 21:23:42Z;
  - Forager brought back from Recents at 21:23:52Z.

  None of it was my input. I stopped driving. Then the planner relayed the owner's words (message 2 below): the owner had
  set the screen timeout.
- **The owner's setting:** `screen_off_timeout` is now **300000** (5 minutes); it was 15000 at the start. **It is the
  owner's change. I leave it and do not restore it.** "Stay Awake" (`stay_on_while_plugged_in=15`) is unchanged, and
  also the owner's.
- **State on resuming** (21:24:36Z): unlocked (`isKeyguardShowing=false`), `mWakefulness=Awake`, Forager's
  `MainActivity` focused. `accelerometer_rotation=0`, `user_rotation=1` (mine), `font_scale=1.0`,
  `display_density_forced=null`, `wm size` and `wm density` physical only.
- **Forager did not crash across it:** the same pid, 23432, before and after. The crash buffer is unchanged at 19 lines
  with 0 Forager lines (`45-crash-at-stop.txt`). The main log's two FATAL EXCEPTIONs are the 08-10 `com.mgoogle` ones.
- **A second `MainActivity` in the task.** Task 406 now holds two instances: `158172503` (the one I used, STOPPED) and
  `5263770` (RESUMED, started at 21:23:38Z by `realStartActivityLocked` after `bringingFoundTaskToFront`). What launched it
  during the owner's navigation is not in the log I read. Recorded under Flags; not investigated.

### The planner's message 1, verbatim

> Planner: resume dispatch `2026-09-28-74`. Your turn was cut off by a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"). Nothing is wrong with your work, and your context is intact.
>
> Your last note said a capture was refused because focus was on a window titled "Pop-Up Window", and that you would confirm its owner before treating it as Forager's. Carry on from there:
> - Re-read the tablet's state (`dumpsys window`: the focused window and its owning package). Re-read your worktree's git status.
> - **If that window belongs to anything other than Forager** (a system, Samsung or Google prompt), it is a prompt under owner rule "4 A". Stop without touching it, and hand back naming it, so the owner can tap through.
> - **If it is Forager's own popup** (a `DropdownMenu`), dismiss it the way a user would and carry on.
>
> Use `-s R52T506412L` only; never touch the S22. Leave "Stay Awake" as the owner set it. Push as you go, and hand back when you finish or stop.

### The planner's message 2, verbatim

> Planner: the owner says, verbatim, "Resume tablet check. I set display settings to dim at 5 minutes".
> - The screen timeout is the owner's setting. Leave it, don't restore it, and record it in the run record as the owner's change. "Stay Awake" also stays as the owner set it.
> - If you stopped because the tablet dimmed or locked, re-read its state now. If it is unlocked and awake, carry on under `2026-09-28-74` from where you were.
> - If the tablet is locked (a lock screen, not just dimmed), stop and hand back without trying to unlock it.
> - If any prompt is showing that isn't Forager's, stop and name it.
>
> Use `-s R52T506412L` only. Push as you go, and hand back when you finish or stop.

Neither message gave a planner-log line.

### Evidence hashes so far (sha256, first 16 hex)

`10-install.txt` 9deba4c560a7f785; `11-dumpsys-package-after.txt` d7a11bee60db11b3; `12-launch.txt` 09a8b5fbc761408c;
`14-crash-after-launch.txt` 72bd6b5b5e9a70ea; `15-db-after-copy.txt` 7b690f4232026b11; `db-after-verify.txt`
4a4e5f427946cc02; `db-after-digest.txt` 9aef3b3a9f2ab7c7; `17-window-r1.txt` 1d1efa6019d250a4; `20-launch-r1.png`
e5bfb7b162fd1b82; `24-r1-maps-pane.png` f20973a961b1bae3 (`.xml` 9c753a698c72267c); `29-r1-waypoint-dropped.png`
732bace92841b6cc; `34-r1-find-saved.png` 10af6bc35d9de98d; `40-r1-entries-list.png` 699d7e0348d221cc (`.xml`
c4115f895da8f88b); `41-r1-entry-report.png` 6e0872d9535229a7 (`.xml` de8739ba1ebc1b8c); `42-r1-entry-menu.xml`
103116d04aeb5274; `44-events-foreign-input.txt` fe8bdd5295e126fd; `45-crash-at-stop.txt` 72bd6b5b5e9a70ea. Every
capture's time, rotation and hashes are in `snaps.log`.

## Status at the end of the run

**Measured in both orientations**, with 270 checked. Everything is restored and read back, and the created items are
left in place. The pre-registration above is unchanged. One thing is **not measured**: a photo in the viewer (see
"Not measured"). **I do not rule on "how much of an issue".** The owner judges from the table and the captures.

## After the resume: rotation 90 continued

- The new front `MainActivity` (see the Interruption) started with no region: "September · Search a location" and the
  map's "Choose a region…" message (`46-r1-resume`). I reloaded the one row under "Recent searches" ("Fungi · September",
  "cached 17 minutes ago"). The map came back at the same bounds, `[1532,405][2800,1720]` (`48-r1-maps-pane-2`).
  `cached_searches` still holds one row at the end, so the reload wrote no new row.
- **J8's chip:** Mushroom Log, the entry's report, "Entry options". Forager's own menu passed `snap.sh` v2
  (`49-r1-entry-menu-2`). Then "Show on map" (`50-r1-j8-chip`).
- **The taxon chip:** "View on Map" on the first species row, West Coast Reishi (`51-r1-both-chips`).
- **Glyph tap:** my find's glyph (`52-r1-glyph-bubble`); closed with its Close button.
- **Details sheet:** Records, All, the waypoint's row (`53-r1-records-all`, `54-r1-details-sheet`); closed with Back.
- **Album and Photo Gallery:** Album view (`55-r1-album`); Timeline view selected again after it; then the drawer's
  "Photo Gallery" row (`56-r1-photo-gallery`).

## Rotation 270

- `user_rotation 3` at 21:29Z; `mCurrentRotation=ROTATION_270` (`57-window-r3.txt`).
- The insets are the same as at 90: `statusBars` `[0,0][2800,64]` and `navigationBars` `[0,1720][2800,1752]`, no
  `displayCutout`. The window is `Requested w=2800 h=1752`, `w1318dp h824dp`.
- **The bars and cut-out do not differ by side,** so under the dispatch I did not measure 270 in full. One capture:
  the map `[1532,405][2800,1720]`, both chips and the Layers and add buttons at the same bounds as at 90
  (`58-r3-maps-pane`).

## Portrait (rotation 0)

- `user_rotation 0` at 21:29Z; `mCurrentRotation=ROTATION_0`. The window is `Requested w=1752 h=2800`, fullscreen,
  configuration **`sw824dp w824dp h1318dp`** (`59-window-r0.txt`). `statusBars` `[0,0][1752,64]`, `navigationBars`
  `[0,2768][1752,2800]`.
- **After rotating** (`60-r0-maps-pane`, and `61-r0-maps-pane-b` 5 s later), the map strip's basemap and glyphs looked
  blocky and pixelated in my reading of the images. The native-resolution crop `61-r0-map-crop.png` shows it, against
  `51-r1-map-crop.png` at 90, which is crisp.
- **A cold launch in portrait** (`am force-stop`, then `am start` at 21:30:56Z, `COLD`, one window) gives the same
  (`63-r0-maps-pane-cold`, crop `63-r0-map-crop.png`). So it is not left over from the rotation. The report map at 360 dp
  in the same orientation renders crisply (`66-r0-report-map-crop.png`). This is my reading of the images; the cause is
  not determined (Flags, 1).
- The region was reloaded from "Recent searches" after the cold launch. The taxon filter did not survive the relaunch.
  Its portrait measurement comes from `60-r0` and `61-r0`, before the relaunch.
- **Glyph tap in the strip:** my waypoint pin, at about (1642, 1545) px. The bubble opened, and it is **cut off at the
  screen's right edge** (`64-r0-glyph-tap`, crop `64-r0-bubble-crop.png`). I dismissed it with Back, because its Close
  button was off screen.
- The Journal's entries list (`65-r0-entries-list`), the report (`66-r0-entry-report`), Records (`67-r0-records-all`),
  the details sheet (`68-r0-details-sheet`), the album (`69-r0-album`) and the Photo Gallery panel
  (`70-r0-photo-gallery`) were all taken in portrait.

## Predicted against measured

dp = px / 2.125, from dump bounds. "Inferred" marks a figure taken from neighbouring nodes, not a node of its own.

| Pane | Portrait, predicted | Portrait, measured | Landscape (90), predicted | Landscape (90), measured |
|---|---|---|---|---|
| Tree | wide (MEDIUM, 824 dp; tall) | **wide** | wide (EXPANDED, 1318 dp; tall) | **wide** |
| App window | 824.5 x 1317.6 dp | 1752 x 2800 px, `w824dp h1318dp` | 1317.6 x 824.5 dp | 2800 x 1752 px, `w1318dp h824dp` |
| Drawer | 360 dp | **[0,0][765,2800]: 360.0 x 1317.6 dp** | 360 dp | **[0,0][765,1752]: 360.0 x 824.5 dp** |
| Species list | 360 dp | **360.0 dp** (765 to 1530 px, inferred), 16 dp padding inside | 360 dp | **360.0 dp** (inferred, the same) |
| Divider | 2 px | 1530 to 1532 px (inferred) | 2 px | 1530 to 1532 px (inferred) |
| **Map** | **220 px = 103.5 dp** (pulse's W − 721: 103.5) | **[1532,405][1752,2768]: 103.5 x 1112.0 dp** | **1268 px = 596.7 dp** (pulse: 596.6) | **[1532,405][2800,1720]: 596.7 x 618.8 dp** |
| Map usable (glyph tap) | the bubble opens, clipped (inferred) | **the bubble opens, cut at the screen's right edge**: [1558,1199][1752,1526], 91.3 of 280 dp visible, Close off screen; the basemap looks blocky (my reading) | opens | **opens whole**: [1976,826][2571,1149], 280.0 x 152.0 dp, with Close |
| Taxon chip | squeezed to ≤ 87.5 dp, under or over the Layers button | **[1549,422][1735,542]: 87.5 x 56.5 dp**, text in three lines; the Layers button [1633,422][1735,524] covers its right 48 dp. The clear control's `Button` reports [0,0][0,0], and no "Show all species" node appears | top centre | **[1735,422][2229,491]: 232.5 x 32.5 dp**, clear target [2145,407][2247,509] |
| J8's chip | squeezed the same | **[1549,551][1735,687]: 87.5 x 64.0 dp**, wrapped onto a second row, text in three lines. Alone after the cold launch: [1549,422][1735,558] | top centre, beside the taxon chip | **[2246,422][2598,524]: 165.6 x 48.0 dp**; the row [1735 to 2598] is centred on the map; 39 dp clear of the Layers button |
| Layers button | 48 dp, top end, 8 dp in | [1633,422][1735,524]: 48.0 dp, 8 dp in | the same | [2681,422][2783,524]: 48.0 dp, 8 dp in |
| Add button | bottom end | [1633,2649][1735,2751]; the attribution text wraps to five lines, [1545,2594][1739,2764], beside and under it | bottom end | [2681,1601][2783,1703]; the attribution is one line |
| Journal (entries list) | 360 dp | **360 dp**; the entry card is [34,464][255,753]: **104.0 x 136.0 dp** in a 3-column grid; title "DEV / IC…" | 360 dp | the same: **104.0 x 136.0 dp** card |
| Entry report map | 360 x 270 dp | **[0,404][765,978]: 360.0 x 270.1 dp** | 360 x 270 dp | **[0,404][765,978]: 360.0 x 270.1 dp** |
| Details sheet | 640 dp, x 92 to 732 dp, over the drawer, list and 11 dp of map | **[196,2191][1556,2768]: 640.0 x 271.5 dp, x 92.2 to 732.2 dp**, full-window scrim; covers the drawer from 92 dp, the list and 11.3 dp of the map | 640 dp, x 339 to 979 dp | **[720,1143][2080,1720]: 640.0 x 271.5 dp, x 338.8 to 978.8 dp**; covers the drawer's last 21 dp, the list and 258 dp of the map |
| Album | 360 dp | **[0,370][765,2768]: 360 dp**, empty ("No photos yet…") | 360 dp | **[0,370][765,1720]: 360 dp**, empty |
| Photo in the viewer | the whole window | **not measured** (no photo) | the whole window | **not measured** (no photo) |
| Old "Photo Gallery" panel | 360 dp | **[0,64][765,2768]: 360 dp**, empty | 360 dp | **[0,64][765,1720]: 360 dp**, empty |

At 270: the same bounds as at 90 for the map and the chips (`58-r3-maps-pane`).

**Pulse prediction check** (the planner's outcome prediction 2 in intent `-74`): every measured width equals the
arithmetic to the pixel, in both orientations.

## Captures, per screen

All in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`, each with a `.png` and `.xml` of the same
name unless marked. Times, rotation and hashes are in `snaps.log`.

| Screen | Portrait (0) | Landscape (90) | 270 |
|---|---|---|---|
| 1. Maps and List pane after a search | `60-r0-maps-pane`, `61-r0-maps-pane-b`, `63-r0-maps-pane-cold` | `24-r1-maps-pane`, `48-r1-maps-pane-2` | `58-r3-maps-pane` |
| 1. Both chips | `60-r0-maps-pane`, `61-r0-maps-pane-b` | `51-r1-both-chips` (J8 alone: `50-r1-j8-chip`) | `58-r3-maps-pane` |
| 1. Glyph tap and bubble | `64-r0-glyph-tap` (crop `64-r0-bubble-crop.png`) | `52-r1-glyph-bubble` | — |
| 1. Map rendering crops (png only) | `61-r0-map-crop.png`, `63-r0-map-crop.png` | `51-r1-map-crop.png` | — |
| 2. Journal entries list | `65-r0-entries-list` | `40-r1-entries-list` | — |
| 2. Entry report and its map | `66-r0-entry-report` (crop `66-r0-report-map-crop.png`) | `41-r1-entry-report` | — |
| 3. Records | `67-r0-records-all` | `53-r1-records-all` | — |
| 3. Details sheet | `68-r0-details-sheet` | `54-r1-details-sheet` | — |
| 4. Album | `69-r0-album` | `55-r1-album` | — |
| 5. Old Photo Gallery panel | `70-r0-photo-gallery` | `56-r1-photo-gallery` | — |

Hashes (sha256, first 16 hex) of the captures cited above that the earlier table did not list: `50-r1-j8-chip.png`
60924efb211084ed; `51-r1-both-chips.png` 12b12840bc41a6d9 (`.xml` a0d1aa24ad0c22ea); `52-r1-glyph-bubble.png`
82cf4f41c51fd691; `54-r1-details-sheet.png` 836e69758b0db60b (`.xml` fb1130662c04e6d5); `55-r1-album.png`
e86df4a99b78279a; `56-r1-photo-gallery.png` 6f5ef6467c2c4c94; `58-r3-maps-pane.png` 6a4b3de449b57531; `60-r0-maps-pane.png`
22f9bee70238d292 (`.xml` 142fba453db82e3a); `61-r0-maps-pane-b.png` ad8a17e3778a17a5; `61-r0-map-crop.png`
28fae750fa7edeba; `51-r1-map-crop.png` e3a26c000611baeb; `63-r0-maps-pane-cold.png` 8314d5f49b003dfd; `63-r0-map-crop.png`
6df3f3dd2bd1fadf; `64-r0-glyph-tap.png` 63d88a62d9d00b09 (`.xml` 610a36bccbb2ce6a); `64-r0-bubble-crop.png`
d3da4119f0c9a2c2; `65-r0-entries-list.png` 660aa4e20a27451b; `66-r0-entry-report.png` 30f4baa407e7681a (`.xml`
fb2f6e1ce4ba1396); `66-r0-report-map-crop.png` 6fdf1f09c4659a1d; `67-r0-records-all.png` 39e37d2a37f8c923;
`68-r0-details-sheet.png` 800e15c484a88cfa (`.xml` 8a49471e3d3b8ee0); `69-r0-album.png` 8e2058585330756e;
`70-r0-photo-gallery.png` 341d2c61dfe7eaeb; `72-r0-after-hide.png` 48effb11d176bb64; `snaps.log` f3a0678ad6f5ad57.

## Not measured: a photo in the viewer (item 4's second half)

Forager on this tablet holds no photo: the album and the Photo Gallery panel are both empty, and `log_photos` is 0.
The dispatch's test-data list names no photo. Making one would need one of these, and none is covered by the dispatch or
the owner's "3 A":
- **Camera**, on a DEVICE CHECK find: the camera permission is not granted, so a system prompt appears (the owner taps
  through under "4 A"), and the picture is whatever the tablet's camera sees.
- **Import or "From Album"**: the system photo picker, choosing one of the owner's own photos.
- Pushing a made-up image into the tablet's shared storage and importing that: a write outside Forager.

So it is recorded as **not measured**. The code prediction stands unobserved: the whole window
(`ui/log/PhotoViewerDialog.kt:131`). It goes to the planner as a question.

## Restored and read back

`73-settings-end.txt`, 21:35:13Z, against `03-settings-start.txt`:

| Setting | Start | Changed by me to | End (read back) |
|---|---|---|---|
| `accelerometer_rotation` | 1 | 0 (locked) | **1** |
| `user_rotation` | 1 | 1, 3, 0 | **1**; display at `ROTATION_90` |
| `font_scale` | 1.0 | not touched | 1.0 |
| `display_density_forced` | null | not touched | null; `wm size` and `wm density` physical only |
| `navigation_mode` | 2 | not touched | 2 |
| `window_animation_scale` | 1.0 | not touched | 1.0 |
| `stay_on_while_plugged_in` ("Stay Awake") | 15 | **not touched; the owner's** | 15 |
| `screen_off_timeout` | 15000 | **not touched; the owner changed it** | 300000 (the owner's; not restored, as ruled) |

App state:
- **My entry's "Show on map" was turned off again** (21:34:5xZ). The chip disappeared (`72-r0-after-hide`), the menu reads
  "Show on map" again, and the database has `shownOnMap=0` on the entry.
- The taxon filter ended with the cold relaunch; no chip remains.
- The Cartography view is back on Timeline, and the drawer is back on the search options.
- Forager is force-stopped. At the start it was running in the background (pid 15584), with the launcher in front. At
  the end the launcher is in front and Forager is not running.

## Crash reads

- **Start** (`02-crash-start.txt`), **after launch** (`14-crash-after-launch.txt`), **at the interruption**
  (`45-crash-at-stop.txt`), **before the cold relaunch** (`62-crash-before-relaunch.txt`) and **at the end**
  (`74-crash-end.txt`, sha256 72bd6b5b5e9a70ea, byte-identical to the others): 19 lines each, the same two 08-10
  `com.mgoogle.android.gms:persistent` FATAL EXCEPTIONs, **0 Forager lines**.
- The main log has no FATAL EXCEPTION naming Forager.
- Forager's pid was stable across each stretch: 23055, then 23432 through the interruption, then the cold relaunch.
- `logcat -c` was never run.

## Closing inventory (database, read-only copy after the final `am force-stop`)

`db-end-verify.txt`, `db-end-digest.txt`: copy matches the device, `integrity_check` `ok`, `user_version` 16. Rows:

| Table | Rows | What |
|---|---|---|
| `cartography_entries` | 1 | my entry: `isDraft=0`, `shownOnMap=0`, text "DEVICE CHECK 2026-09-28 T" |
| `cartography_entry_find_refs` | 1 | the find kept in it |
| `cartography_entry_waypoint_refs` | 1 | the waypoint kept in it |
| `mushroom_log_entries` | 1 | my find: `ownIdentification` and `entryNotes` "DEVICE CHECK 2026-09-28 T"; `syncStateKind=DRAFT` |
| `waypoints` | 1 | my waypoint, named "DEVICE CHECK 2026-09-28 T" |
| `cached_searches` | 1 | the one search (the recent-search reloads added none) |
| `android_metadata`, `room_master_table` | 1 each | as before |
| every other table | 0 | as before |

## Decisions I made

1. **No record entries; structure validated against no `kit.json`.** My standing instructions call for a sweep, an
   intent in `RECORD.md`, and a check of the dispatch's sections against `.claude/kit.json`. At `d7cc9f5` there is no
   `kit.json` and no checker (the owner removed them in `e136330`). `-74` says "The planner writes the record", and the
   launch message says "you do not touch `RECORD.md`". So I wrote no record entry and treated the required-section list
   as empty, as the Part 1 coder did.
   - The main checkout (`faf2f88`) still has a `kit.json` whose `device` type requires "Base and state",
     "Scope boundary", "Closed decisions", "Prediction", "Finish line and abort conditions", "Checks", "Out of scope" and
     "Device items". `-74` has none of those headings.
   - Deciding which applies properly needs an owner ruling. I followed the dispatch and its base.
2. **The capture guard.** `snap.sh` refuses to capture unless Forager owns the focused window. It first checked the window
   title, and I changed it to the owning package after Forager's own `DropdownMenu` ("Pop-Up Window") was refused. The
   old version is kept.
3. **The search location.** I typed coordinates for a public national-park visitor centre rather than using "Use current
   location", which would need the location permission and so a prompt. The place is my choice; its value stays out of
   the repository.
4. **The entry keeps the find as well as the waypoint.** The editor offered both, kept by default. The dispatch names
   only the waypoint ("if it can be kept"), and I left the default.
5. **Labels:** the find carries the label in both its identification and its notes, the waypoint in its name, and the
   entry in its text.
6. **Showing my entry on the map** to see J8's chip, then hiding it again and reading it back. The chip exists only
   while an entry is shown; `-72`'s rule was the model. Following the planner, I first dismissed the menu with Back and
   then reopened it.
7. **The taxon chip** came from "View on Map" on the first species row (West Coast Reishi).
8. **A short pan before logging the find,** so it would not sit on the waypoint.
9. **A cold relaunch in portrait** (`am force-stop`, `am start`), which the dispatch did not ask for. It was to tell a
   rotation leftover from the narrow strip's own rendering. It also cleared the second `MainActivity` and the taxon
   filter.
10. **270 checked, not measured in full.** I read "if the tablet's cut-out or bars differ by side" as the insets
    reported by `dumpsys window`, which match 90's.
11. **Measurement choices:**
    - the species list's and divider's widths are inferred from the drawer's right edge and the map's left edge (the
      list has no node of its own in the dump);
    - the glyph tests used my own find (90) and waypoint (portrait);
    - in portrait I closed the bubble with Back, because its Close was off screen.
12. **The rendering reading** ("blocky") is my reading of images, stated as such.
13. **The photo viewer not measured,** and no photo created (above). Left for the planner rather than decided.
14. **App UI state tidied** (Timeline view, the drawer on search options). This was not in any restore list. Forager
    left stopped rather than running in the background as found.
15. **After the owner's first interruption I stopped driving** until the planner's second message, rather than going on
    with the first. The first had been written before the owner's Settings activity.

## Flags outside scope

Recorded, not investigated. None is ruled on here.

1. **In portrait the 103.5 dp map strip renders blocky** (my reading of `61-r0-map-crop.png` and `63-r0-map-crop.png`),
   both after a rotation and after a cold launch. The 360 dp report map in the same orientation is crisp. Cause not
   determined.
2. **In portrait a map bubble is cut off at the screen's right edge,** with its Close button off screen (`64-r0`).
3. **In portrait the Layers button covers the taxon chip's right 48 dp,** and the chip's clear control does not appear
   in the dump (its `Button` reports `[0,0][0,0]`). Whether a finger can clear the filter in portrait was **not
   touch-tested**.
4. **In portrait the attribution text wraps to five lines** beside and under the add button.
5. **The Journal's entries grid in the 360 dp drawer** gives a 104 dp card, whose title reads "DEV / IC…" and whose
   counts get 11 dp each. This matches the pulse's J9 row ("3 in a 360 dp drawer").
6. **The Records chip row is wider than the drawer;** "Waypoints" is cut at its edge (it scrolls).
7. **A second `MainActivity` instance** appeared in Forager's task at 21:23:38Z, during the owner's navigation. The log
   I read does not say what launched it.
8. The installed build's commit `ce8ddbef` is in no local clone and on no remote ref.
9. The find reads `syncStateKind=DRAFT` in the database while the app lists it under Finds, Log. I take that field to be
   its iNaturalist upload state (unverified).
10. `-12`'s "no iNaturalist activity" is overridden here by `-74`, which allows a search. The search did reach
    iNaturalist (it reported 293 observations).
11. The standing-instruction conflict in Decisions 1.

## Appendix: the dispatch, verbatim

`prompts/preserved/2026-09-28-74.md` at `d7cc9f5`, whole (sha256 `40d67458bc7710a837cd8599f50ac500d1467447f8f5a45c11dcc638b459d337`):

~~~~markdown
HEAD: 8dbfd14 (journal-redesign) when written. **Queued until the owner says the tablet is connected;** the base at launch is stated in the launch message.
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: device
Preserved: 2026-09-28T20:53:37Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** device

# Role

You are the coder for **the tablet layout sanity check before J6**. You observe, measure and record. You change no app code. This dispatch's intent is `2026-09-28-74`. The planner writes the record.

# The owner's words, verbatim

- "for J6, perform a device check for a sanity test to see how much of a issue the layout sizes are"
- "Do a separate check before J6 / I'll connect the tablet for the check before J6. Do not start J6 without that sanity check"
- To the planner's tablet questions: "2 B" (Forager is already on the tablet: install over it and keep its data); "3 A" (create a few items labelled "DEVICE CHECK" inside Forager and leave them); "4 A" (at any prompt, stop, and the owner taps through).

Read first:
- `CLAUDE.md`;
- `docs/audits/2026-09-28-j6-premise-pulse.md`, your map of the wide layout and the source of the predictions below;
- `docs/plans/journal-redesign.md`, "J6 rulings" and what follows it;
- `prompts/preserved/2026-09-28-51.md` and `-72.md`, whose standing rules apply unless this dispatch changes them.

# The device

- **The S22 (`R5CT321008R`) may be in use by another coder. Never touch it.** Pass `-s <serial>` on every adb command.
- The tablet is the one device in `adb devices` that is not `R5CT321008R`. **If there is more than one such device, or none, stop.**
- Read and record:
  - `ro.product.model`, `ro.product.manufacturer` and `ro.build.fingerprint`;
  - the Android version, `wm size` and `wm density`;
  - for each orientation, the app window's size in dp (`dumpsys window`).
- Log it by model and build, not by serial alone.

# Standing rules

- Never run `logcat -c`.
- Launch with `am start`, never `monkey`.
- Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`.
- **Prompts (owner "4 A"):** if any system, Google or first-run prompt appears, stop at once. Do not tap it, dismiss it or capture it. Hand back to the planner saying which prompt appeared. The owner taps through, and the planner resumes you.

# Install over the existing Forager, keeping its data (owner "2 B")

1. Read the installed package: `versionName`, `versionCode`, `firstInstallTime`, `lastUpdateTime`, whether it is debuggable, and its signing certificate digest.
2. **Build** a debug APK from `origin/journal-redesign` at the base the launch message names, in a new worktree `/home/zynergy-labs/Zynergy/forager-wt/device-tablet` (branch `device-tablet`), with `--offline assembleDebug` and `LC_ALL=C.UTF-8`. Check `df` and memory first, and wait if under 2.5 GB is free.
3. **If the installed certificate differs from the debug APK's, stop.** `install -r` would fail, and uninstalling is forbidden.
4. **Back up the database first, as `-72` does:**
   - force-stop the app;
   - copy `forager.db` and its `-wal`/`-shm` files through `run-as`;
   - verify on this machine: sha256, the SQLite header, `integrity_check` ok, `user_version`, and every table's row count.
   - **If `run-as` is not available (not debuggable), or the backup does not verify, stop.**
5. `install -r` only; never uninstall, clear data or use `-d`. Confirm `firstInstallTime` is unchanged.
6. If `user_version` was below 16, the install migrates. Confirm afterwards: version 16, integrity ok, and every table's row count equal to the backup's. **A mismatch or a crash is an abort.** Report it, and restore nothing without the owner.
7. Read crashes after launch.

# Test data (owner "3 A")

- Create only what the measurements need, **each item labelled "DEVICE CHECK 2026-09-28 T"**:
  - one saved journal entry with a line of text and, if it can be kept, a waypoint;
  - one find;
  - one waypoint.
- Record exactly what you created, and leave it on the tablet.
- **Do not open, edit or delete the owner's own records on the tablet.**
- A species search is needed for the wide map to show (`AvailabilityWideLayoutUi.kt:228-231`). Running one is allowed; it writes a search cache row, which you record.

# Predictions to write before measuring (from the pulse, section 6)

- The wide tree is taken when the window is at least 600 dp wide and at least 480 dp tall (`AvailabilityScreen.kt:1690`, `WindowWidthClass.kt`, `ShortWindow.kt`). Predict which tree each orientation takes, from the dp sizes you read.
- On the wide tree:
  - the drawer (the Journal) is 360 dp (`AvailabilityWideLayoutUi.kt:88`), and the species list beside it is 360 dp;
  - the map gets the rest: about 119 dp at a width of 840, and nothing at 721 or less. That is the pulse's arithmetic, never run.

  Write your predicted widths for this tablet before looking.

# Measurements, in each orientation (portrait, and landscape at rotation 90; also 270 if the tablet's cut-out or bars differ by side)

Take a screenshot and a uiautomator dump of each screen, and convert bounds from px to dp with the density you read:
1. The Maps/List pane after a search: the drawer's, list's and map's widths and heights, and whether the map is usable at all (a glyph tap opens its bubble). Also where J8's chip and the taxon chip sit.
2. The Journal in the drawer: its width, the Entries list, and an entry's report with its map, giving the report map's size.
3. Records, and a record's details sheet: where the sheet draws relative to the drawer and the map, and its width.
4. The album and a photo in the viewer.
5. The old "Photo Gallery" panel (owner: to be removed in J6), for the record only.

Then a table: predicted against measured for every pane. **Do not rule on "how much of an issue".** Present the measurements and captures. The owner judges.

# Restore

Restore every setting you change (rotation, and any display setting), and read it back. The test items stay (owner "3 A"). **Change no device-wide display settings** (density, font size) at all.

# Report

Write `docs/audits/2026-09-28-tablet-layout-sanity-run-record.md` on branch `device-tablet`. Commit and push as you go; the planner merges it. Record:
- the device facts;
- the install and backup figures;
- the created items;
- the prediction and measurement table;
- captures named per screen;
- what was restored;
- crash reads;
- **Decisions I made**;
- **Flags outside scope**.

# Abort conditions

- more or fewer than one non-S22 device;
- a signature mismatch;
- a backup that cannot be made or verified;
- a migration mismatch or crash;
- a new crash;
- a locked tablet;
- any need to touch the owner's records or the S22.

A prompt pauses the run for the owner rather than ending it.

# Merge

Not authorised.
~~~~

## Photo viewer (continuation, 2026-09-28-86)

**Status of this section: pre-registration.** Written and pushed before any Forager screen was opened in this run and
before the photo was taken. Measurements are appended in later commits; this part stays unchanged.

**Date:** 2026-09-28, from 22:25Z.
**Dispatch:** `prompts/preserved/2026-09-28-86.md` at `20ada3d` (sha256
`e859e47750cd75ebaca96ac26d76e83b3ed25f1c3e40edc2b29218a65c5d84cb`, 3176 bytes), quoted verbatim at the end of this
section. The intent `2026-09-28-86` and every `RECORD.md` entry are the planner's; I write none (Decisions I made, 1,
below).
**Base:** `origin/journal-redesign` at `20ada3d`, confirmed with `git fetch` at the start. Branch `device-tablet-2`,
worktree `/home/zynergy-labs/Zynergy/forager-wt/device-tablet-2`, cut from `20ada3d`.
**Code citations** are at `d7cc9f5`, the installed build's commit. `ui/log/PhotoViewerDialog.kt`, `EntriesAlbum.kt`,
`LogEntryReportScreen.kt`, `PhotoAcquisitionLaunchers.kt` and `location/AndroidLocationProvider.kt` are identical at
`20ada3d`; `ui/map/MapBubble.kt` differs by 2 added lines, not in the viewer's call.
**Evidence:** the same directory as above, numbered from 75. The privacy rule above holds: no screenshot, dump,
coordinate or photo goes into this file. The photo shows whatever the tablet's camera faces.

### The launch message, verbatim

> This is planner dispatch `2026-09-28-86`, **the tablet photo-viewer measurement**. Its store copy is committed at `prompts/preserved/2026-09-28-86.md` on `origin/journal-redesign` at `20ada3d`. Fetch it, read it in full and quote it verbatim; the file governs over this message.
>
> The owner is at the tablet now and chose, verbatim: "Option A is my choice for the photo viewer question". That means taking one photo with the app's camera on the find labelled "DEVICE CHECK 2026-09-28 T".
>
> - **The tablet only:** `-s R52T506412L` (`SM-X800`) on every adb command. **Never touch the S22 `R5CT321008R`**; another coder is running a device check on it.
> - **No install.** Confirm the installed build is `1.0.1416+gd7cc9f5b` first.
> - **At the camera permission prompt, or any system prompt, stop at once without touching it.** Hand back naming it. The owner will tap through, and you will be resumed. Expect this.
> - Create only the one photo. Touch nothing of the owner's. Leave "Stay Awake" and the owner's screen timeout alone.
> - Write your prediction first: the viewer takes the whole window (`PhotoViewerDialog.kt:131`). Then open it from the album, the find and the map. Measure its bounds in dp in portrait and at 90, and capture it.
> - Append the section to `docs/audits/2026-09-28-tablet-layout-sanity-run-record.md` on a new branch `device-tablet-2` from `origin/journal-redesign`, and push. Evidence goes in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`. The planner merges it; you do not touch `RECORD.md`.
>
> Hand back when you finish or stop.

### The planner's relay during the run, verbatim

Received at about 22:3xZ, after the device reads below and before this section was first pushed. I had not stopped, so
under its last bullet I carried on. No planner-log line was given with it.

> Planner relay under dispatch `2026-09-28-86`. The owner says, verbatim: "The tablet is ready for the device run".
>
> - If you stopped at the camera permission prompt, or any other prompt, re-read the tablet's state now with `dumpsys window` (the focused window and its owner).
> - If the prompt is gone and Forager is in front, carry on. The owner has handled it.
> - If a prompt is still showing, don't touch it. Hand back naming it.
> - If you had not stopped, carry on as you were.
>
> `-s R52T506412L` only. Hand back when you finish or stop.

### The device (read before any Forager screen)

- **Attached** (`75-devices.txt`, 22:25:44Z): `R52T506412L` (`model:SM_X800`, `product:gts8pwifixx`) and
  `R5CT321008R` (the S22, listed only; never addressed). Every other adb call in this run names `-s R52T506412L`.
- **Identity** (`75-device-facts.txt`): `SM-X800`, samsung, `gts8pwifixx`, fingerprint
  `samsung/gts8pwifixx/gts8pwifi:16/BP2A.250605.031.A3/X800XXSBEZE1:user/release-keys`, Android 16, SDK 36;
  `wm size` 1752 x 2800 and `wm density` 340, both physical with no override. 1 dp = 2.125 px, as in the first run.
- **Installed build** (`76-dumpsys-package-start.txt`): `versionName=1.0.1416+gd7cc9f5b`, `versionCode=1416`,
  `firstInstallTime=2026-09-28 04:30:35`, `lastUpdateTime=2026-09-28 14:06:59`: the first run's install, unchanged.
  **The expected build; nothing installed in this run.** `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`,
  `ACCESS_MEDIA_LOCATION` and `POST_NOTIFICATIONS` are all `granted=false`. Forager was not running.
- **State** (`77-state-start.txt`, 22:25:56Z): `isKeyguardShowing=false`, `mWakefulness=Awake`, the launcher focused, no
  prompt showing, `ROTATION_90`. Settings: `accelerometer_rotation=1`, `user_rotation=1`, `font_scale=1.0`,
  `display_density_forced=null`, `navigation_mode=2`, `window_animation_scale=1.0`; `stay_on_while_plugged_in=15` and
  `screen_off_timeout=300000`, both the owner's, left alone.
- **Crash buffer at the start** (`78-crash-start.txt`): 19 lines, the two 08-10 `com.mgoogle.android.gms` FATAL
  EXCEPTIONs, 0 Forager lines; byte-identical to the first run's `74-crash-end.txt` (sha256 72bd6b5b5e9a70ea).
- **Database before any change** (`79-db-p2start-copy.txt`, `db-p2start-verify.txt`, `db-p2start-digest.txt`; read-only
  copy through `run-as`, the app not running, so no force-stop was needed): device and local sha256 match,
  `integrity_check` `ok`, `user_version` 16. Rows: `cartography_entries` 1, `cartography_entry_find_refs` 1,
  `cartography_entry_waypoint_refs` 1, `mushroom_log_entries` 1, `waypoints` 1, `cached_searches` 1,
  **`log_photos` 0, `log_entry_photos` 0**. Every per-table digest equals the first run's closing `db-end-digest.txt`.
- **App files** (`80-app-files-start.txt`): `files/captures/` empty; no photo directory.

### Predictions, written before the photo and before measuring

1. **The viewer takes the whole window** in both orientations. `PhotoViewerDialog` is a Compose `Dialog` with
   `DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)` (`ui/log/PhotoViewerDialog.kt:131`),
   whose content is a black `Box` with `Modifier.fillMaxSize()` (`:133-141`). Predicted bounds of the viewer's root:
   - portrait: **[0,0][1752,2800], 824.5 x 1317.6 dp**;
   - landscape (90): **[0,0][2800,1752], 1317.6 x 824.5 dp**.

   So it covers the drawer, the species list and the map entirely, in its own window above the activity's.
2. **The same bounds from every route.** All three routes compose the same function: the album
   (`ui/log/EntriesAlbum.kt:181`), the find's report (`ui/log/LogEntryReportScreen.kt:199`) and a photo bubble's
   "View photo" (`ui/map/MapBubble.kt:327`, the action at `:391`). The host's size cannot reach a `Dialog` that is its
   own window.
3. **The close control** sits inside `WindowInsets.safeDrawing` (`:148`), at the top start, 8 dp in (`Spacing.sm`), a
   48 dp `IconButton`: predicted [17,81][119,183] px in both orientations (the 64 px status bar plus 8 dp; no left inset
   at 0 or 90). With one photo there is no previous/next row (`:155`).
4. **One system prompt:** the camera permission request, since `CAMERA` is not granted
   (`ui/log/PhotoAcquisitionLaunchers.kt:121-128`). This is the planner's prediction 2 in the intent; the run stops
   there for the owner.
5. **The photo will not be located, so the map route will not be reachable.** A camera capture's coordinate comes only
   from a live fix (`ui/log/MushroomLogViewModel.kt:809, 959-976`), and `AndroidLocationProvider.getCurrentLocation()`
   returns `PermissionDenied` before asking for one when neither location permission is granted
   (`location/AndroidLocationProvider.kt:24`). With no coordinate the photo has no marker on the Photos layer
   (`domain/GetMapRecordsUseCase.kt:97`, a `mapNotNull`), and a "View photo" bubble exists only for a photo marker.
   The find's own bubble shows a cover thumbnail but has no viewer action (`MapBubble.kt:371-379`). **Predicted: no
   map route; recorded as not reachable, not measured.**
6. **What the photo creates** (`domain/AddPhotoToLogEntryUseCase.kt`; the re-edit draft and Save,
   `MushroomLogViewModel.kt:453-529`, `domain/CommitDraftEntryUseCase.kt`): one `log_photos` row with null latitude and
   longitude, and one `log_entry_photos` row pointing the find at it. `mushroom_log_entries` is back to 1 row with its
   digest unchanged, because the committed row is the draft, a field-for-field copy of the find. One image file in
   app storage; `files/captures/` empty again afterwards.

### Corrections to the pre-registration above

Written after measuring; the pre-registration is left as pushed in `eda1583`.
- The planner's relay arrived at about **22:26Z**, not "22:3xZ": the pre-registration was committed at 22:27:16Z.
- The find bubble's lines are `MapBubble.kt:371-380`, not `:371-379`.

### The camera permission prompt: answered by the owner before my first input

- **I saw no prompt.** My tap on the find's Camera button (22:28:30Z, `88-camera-tap.txt`) opened the in-app camera
  straight away, and focus stayed on Forager's `MainActivity`. `CAMERA` then read `granted=true` with `USER_SET`; at
  22:25Z it had been `granted=false` (`76-dumpsys-package-start.txt`).
- **The events log** (`89-events-since-2224.txt`, `89b-events-all-since-2225.txt`; tablet time is UTC−7) shows what
  happened in between, none of it my input:
  - 22:26:42Z: Forager was started from the launcher (task 409, a new process).
  - 22:26:47Z: Forager requested `CAMERA`, and `GrantPermissionsActivity` opened.
  - 22:26:49Z: it was answered with one touch and closed.
  - 22:26:59Z: Forager's task was removed from Recents (`am_kill … remove task`).
  - 22:27:01Z: Forager was started again from the launcher (task 410).

  The owner handled the prompt before I touched Forager. This matches the relay's "If the prompt is gone and Forager is
  in front, carry on".
- **No photo came from the owner's session:** `files/` had no `photos/` directory at 22:28Z
  (`90-app-files-now.txt`), and the camera I opened read "No photos yet".
- **Two `MainActivity` instances.** My `am start -n` at 22:27:35Z added a second `MainActivity` (105319010) on top of
  the owner's launcher-started one (78494893) in task 410 (`96-tasks.txt`). I used the top one throughout. The final
  force-stop ended both. See Flags.

### What was created (read from the database copy after the final force-stop)

All at rotation 90, through the app's own UI with real taps:
1. The drawer's Mushroom Log, then Records, then Finds, then the find ("Find on 2026-09-28"). In the drawer this opens
   the find's edit form (`LogPanel` has no separate report step, `MushroomLogViewModel.kt:440-451`), which starts a
   re-edit draft.
2. Camera (22:28:30Z) opened the in-app camera (`91-r1-camera-open`). **One press of "Take photo" at 22:29:31Z**, and
   the count read "1 photo taken" (`92-r1-after-shutter`). Back closed the camera, and the form showed one "Log photo"
   thumbnail (`93-r1-editor-with-photo`).
3. Save (22:29:53Z), then the back arrow. The Finds list shows the find with the photo as its cover (`95-r1-finds-after`).

**Rows** (`124-created-rows.txt`, `db-p2end-verify.txt`, `db-p2end-digest.txt`; the copy matches the device's sha256;
`integrity_check` `ok`; `user_version` 16):
- **`log_photos`, 1 row (was 0):** id `63fa4ada-afa4-4571-8b89-02cb4ab9838b`, `relativePath`
  `photos/63fa4ada-afa4-4571-8b89-02cb4ab9838b.jpg`, `createdAtEpochMillis` 1790634571890 (22:29:31Z, the shutter
  press), **`latitude` and `longitude` NULL**.
- **`log_entry_photos`, 1 row (was 0):** `entryId` `dfcf2b13-3e9a-404a-b874-f687f5374def` (the find, `isDraft=0`),
  `photoId` `63fa4ada-…`. A join confirms that the reference points at the find and at the photo.
- **`mushroom_log_entries`:** 1 row, **its digest unchanged** from the start copy. No draft row remains.
- **The file:** `files/photos/63fa4ada-afa4-4571-8b89-02cb4ab9838b.jpg`, 1114083 bytes (device sha256
  932c86edd8d3d5eb…). `files/captures/` is empty again (`126-app-files-end.txt`).
- **Also changed, not created:** the `cached_searches` row. My reload of the recent search (Decisions 5) re-fetched it.
  `fetchedAtEpochMillis` and `lastAccessedAtEpochMillis` moved to 22:30:53Z. Every other column, `entriesJson`
  included, is unchanged (`125-cached-search-diff.txt`). It is still 1 row.
- **Every other table's digest** equals the start copy's.

### Measurements

dp = px / 2.125, from dump bounds. Before each viewer opened, the drawer [0,0][765,H], the species list (765 to 1530 px)
and the map were on screen behind it. The map was at [1532,405][2800,1720] at 90 and [1532,405][1752,2768] at 0
(`101-r1-album`, `106-r1-find-editor`, `112-r0-find-editor`, `116-r0-album`), the same bounds as the first run.

| | Portrait (0), predicted | Portrait (0), measured | Landscape (90), predicted | Landscape (90), measured |
|---|---|---|---|---|
| Viewer from the **album** (`EntriesAlbum.kt:181`) | [0,0][1752,2800], 824.5 x 1317.6 dp | **window frame [0,0][1752,2800]; root and "Full-screen photo" node [0,0][1752,2800]: 824.5 x 1317.6 dp** (`117-r0-viewer-album`, `118-r0-window-viewer-album.txt`) | [0,0][2800,1752], 1317.6 x 824.5 dp | **window frame [0,0][2800,1752]; root and photo node [0,0][2800,1752]: 1317.6 x 824.5 dp** (`102-r1-viewer-album`, `103-r1-window-viewer-album.txt`) |
| Viewer from the **find** (its edit form's thumbnail, `LogEntryDetailScreen.kt:288`) | the same | **the same, [0,0][1752,2800]** (`113-r0-viewer-find`, `114-r0-window-viewer-find.txt`); its dump is byte-identical to the album's at 0 | the same | **the same, [0,0][2800,1752]** (`107-r1-viewer-find`, `108-r1-window-viewer-find.txt`); its dump is byte-identical to the album's at 90 |
| Viewer from a **map bubble** | not reachable (unlocated photo) | **not reachable**: `latitude`/`longitude` NULL, so no photo marker. Not exercised on the device | not reachable | **not reachable**, the same |
| Close control | [17,81][119,183] px | **[18,82][120,184]: 48.0 dp**, 8.5 dp from the left, 38.6 dp from the top (the 30.1 dp status bar plus 8.5 dp) | [17,81][119,183] px | **[18,82][120,184]**, the same |
| Previous/next row | none (one photo) | none | none | none |

- **The window.** At every open the viewer is its own window: `ty=APPLICATION`, `(fillxfill)`, token the activity
  (105319010), `Requested w/h` equal to the display, and `frame` equal to `display`. It sits above the activity's
  window, and on screen it is the whole display.
- **Does it cover the drawer, the list and the map? Yes, entirely, in both orientations, from both routes.** The window
  frame contains all three panes' bounds. In each capture only the photo, fitted on black, the Close control and the
  system status bar and gesture handle are visible. None of the drawer, the list or the map shows through (my reading
  of `102`, `107`, `113` and `117`). A uiautomator dump lists only the focused window, so the dumps alone would not show
  what lies underneath. The window frame and the screenshots are the evidence for coverage.
- **Status bar.** The viewer draws behind the system bars rather than hiding them: the status bar's clock and icons stay
  visible over the black at the top (my reading of the captures). This is consistent with `decorFitsSystemWindows = false`
  and with the controls padded inside `safeDrawing` (`PhotoViewerDialog.kt:131, 148`).

**Predicted against observed:**
1. **Whole window:** confirmed, 4 of 4 opens.
2. **The same from every route:** confirmed for the album and the find. Each orientation's two dumps are byte-identical
   (`c15e75925243590e` at 90, `fd04c233fa6f6ff7` at 0).
3. **The close control:** 1 px off at each edge (predicted 17 and 81, measured 18 and 82). The size, 48 dp, is as
   predicted.
4. **One camera prompt:** the events log shows exactly one `CAMERA` request. It came in the owner's own session before
   mine, and I saw none.
5. **Unlocated photo, no map route:** confirmed from the database. The map route was not tried on the device.
6. **The rows:** confirmed as predicted. The cache row's timestamps were not predicted; they come from my reload
   (Decisions 5).

### Captures and evidence (all in the evidence directory; times, rotation and hashes are in `snaps.log`)

| Screen | Portrait (0) | Landscape (90) |
|---|---|---|
| Viewer from the album | `117-r0-viewer-album` (png 764a3b3bffdcfab8) | `102-r1-viewer-album` (png ccf40dcde1d2d582) |
| Viewer from the find | `113-r0-viewer-find` (png 50e073494bebc26b) | `107-r1-viewer-find` (png d55c1755169f4035) |
| The album before opening | `116-r0-album` | `101-r1-album` |
| The find's form before opening | `112-r0-find-editor` (xml only) | `106-r1-find-editor` (xml only) |
| Window frames | `114-r0-window-viewer-find.txt` a853ce086496cbcf, `118-r0-window-viewer-album.txt` 8d53c45190366412 | `103-r1-window-viewer-album.txt` a9cbc000533176e2, `108-r1-window-viewer-find.txt` 262b17e2147c3964 |

Also: `91-r1-camera-open`, `92-r1-after-shutter` (png b9e70b9bfe1322bb), `93-r1-editor-with-photo`, `94-r1-saved`,
`95-r1-finds-after` (png c5ada0a5f64969ad), `98-r1-maps-pane` (png 3783a40649580403), `111-r0-finds`.
Text evidence: `124-created-rows.txt` 0cadc8f84ef31e38, `125-cached-search-diff.txt` d69bcf7736c2d7ec,
`126-app-files-end.txt` aca76121b9fb8826, `db-p2end-verify.txt` c3296ca6bf9486b3, `db-p2end-digest.txt`
71be0526ce57c7c6, `81-rotation-log.txt` 9d6b5fa56e4fbf5c, `89b-events-all-since-2225.txt` 1227e8bc96120fcd,
`96-tasks.txt` 7b4055d2d39b685c. Sha256 prefixes of every file from 75 on are in `130-hashes.txt`.

### A stray input of mine

At 22:30:52Z my command that reloaded the recent search also sent one **Back** key, by a scripting error. I did not mean
to send it. Afterwards the screen showed the reloaded list and map with no dialog (`98-r1-maps-pane`). No activity was
finished or created (`99-events-after-stray-back.txt` holds no `wm_*_activity` line, and `99-tasks-after-stray-back.txt`
shows the same two instances with the same one resumed). I found no effect of it, and I cannot say what, if anything,
consumed it.

### Restored and read back (`129-settings-end.txt`, 22:35:38Z, against `77-state-start.txt`)

| Setting | Start | Changed by me to | End (read back) |
|---|---|---|---|
| `accelerometer_rotation` | 1 | 0 (locked) | **1** |
| `user_rotation` | 1 | 1, then 0 | **1**; display at `ROTATION_90` |
| `font_scale`, `display_density_forced`, `navigation_mode`, `window_animation_scale`, `wm size`/`density` | 1.0, null, 2, 1.0, physical | not touched | the same |
| `stay_on_while_plugged_in` ("Stay Awake") | 15 | **not touched; the owner's** | 15 |
| `screen_off_timeout` | 300000 | **not touched; the owner's** | 300000 |
| `CAMERA` permission | not granted | **not touched; granted by the owner at 22:26:49Z** | granted (left as the owner set it) |

App state:
- The Cartography view is back on Timeline (`119-r0-timeline`), and the drawer is back on the search options
  (`121-r0-search-options`).
- Forager is force-stopped (22:34:54Z, pid 31081 before, none after). It was not running at my first reads (22:25Z).
  The owner then started it. It is stopped now so the database could be copied (Decisions 9). The launcher is in front,
  and the tablet is unlocked.
- **Left in place (owner "3 A"):** the photo, its row, its reference and its file, with the other DEVICE CHECK items.

### Crash reads

- **Start** (`78-crash-start.txt`), **before the force-stop** (`120-crash-before-stop.txt`) and **at the end**
  (`127-crash-end.txt`): 19 lines each, byte-identical to the first run's `74-crash-end.txt` (sha256 72bd6b5b5e9a70ea).
  They hold the two 08-10 `com.mgoogle` FATAL EXCEPTIONs and **0 Forager lines**.
- **Main log since 22:25Z** (`128-main-since-2225.txt`, 56097 lines): 0 `FATAL EXCEPTION` lines, and no `am_crash` or
  `am_anr` event. Forager's pid 31081 stayed stable from the owner's 22:27:01Z start until my force-stop.
- `logcat -c` was never run.

### Decisions I made (this continuation)

1. **No record entries; no required-section check against `kit.json`.** The same conflict as Decisions 1 of the first
   run, still present:
   - my standing instructions ask for a sweep, an intent and a structural check against `.claude/kit.json`;
   - at `20ada3d` there is no `kit.json` and no checker (the owner removed them in `e136330`);
   - `-86` says "The planner writes the record", and the launch message says "you do not touch `RECORD.md`".

   I followed the dispatch and its base. The checkout my session started in (`faf2f88`) still has a `kit.json` whose
   `device` type requires "Base and state", "Scope boundary", "Closed decisions", "Prediction", "Finish line and abort
   conditions", "Checks", "Out of scope" and "Device items". `-86` has none of those headings. Deciding which applies
   properly needs an owner ruling.
2. **The worktree.** `device-tablet-2` is in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-tablet-2`,
   rather than in my session's own worktree, which is on another branch.
3. **Carrying on with no prompt.** There was no prompt for me to stop at, because the owner had already answered it
   (above). I carried on under the relay.
4. **How the photo reached the find.** The drawer's edit form, Camera, one shutter press, Back to close the camera, then
   **Save**.
   - Cancel would have deleted the draft together with its photo reference (`MushroomLogViewModel.kt:531`), leaving
     the photo in the album but not on the find.
   - The back arrow alone would have left a draft behind.
5. **Reloading the recent search** ("Fungi · September", the first run's DEVICE CHECK search), so that the list and the
   map were drawn behind the viewer and the coverage could be observed. The dispatch did not ask for it. It re-fetched,
   and the cache row's two timestamps changed.
6. **"The find" route is the edit form's thumbnail**, because a find tapped in the drawer opens for editing. I left the
   form each time with the back arrow. An unchanged re-edit draft is deleted on that exit (`MushroomLogViewModel.kt:582`),
   and the end copy holds no draft.
7. **The map route was not tried on the device.** The photo has no coordinate, so there is no photo marker to tap. I
   did not grant location or change the camera's "Save location" chip, which would have been needed to make one.
8. **Closing the viewer** with its own Close control each time, not Back.
9. **The force-stop at the end,** needed for a consistent database copy. It also ended the instance the owner had
   started.
10. **Putting the photo's and the find's ids and the photo's file name in this file.** They are random UUIDs, and I read
    the privacy rule (no screenshot, dump, coordinate, note text, place name or photo) as not covering them.
11. **Leaving the `CAMERA` grant in place.** It is the owner's.

### Flags outside scope (this continuation)

Recorded, not investigated; none is ruled on here.

1. **`am start -n` stacks a second `MainActivity`** on a launcher-started task: 105319010 on top of 78494893 in task
   410. My intent carried no action; the owner's carried `MAIN`. That a mismatch with the task's root intent is the
   cause is inferred, not verified. The same mismatch may explain the first run's flag 7, but that is unverified.
2. **The Timeline/Album choice did not survive.** I chose Album at 90. After a switch to Records, a rotation and a
   return, Cartography showed Timeline again (`115-r0-album`). Which of the two reset it is not determined. CLAUDE.md's
   UX defaults treat an unrequested reset of user-set UI state as a bug unless stated. I do not rule on it.
3. **Tapping a recent search re-fetches from iNaturalist** rather than showing the cache: the row read "cached just now"
   afterwards, and `fetchedAtEpochMillis` moved. The first run saw no new row, which is also true here. What changes is
   the row's timestamps.
4. **The camera's "Save location: On" chip** shows On while no location permission is granted. The photo was saved
   with no coordinate, and nothing on screen said so. The code documents this as intended
   (`MushroomLogViewModel.kt:935-944`).
5. **The viewer leaves the status bar visible** over the photo (above). Whether that is wanted is the owner's call.
6. The standing-instruction conflict in Decisions 1.

### The dispatch, verbatim

`prompts/preserved/2026-09-28-86.md` at `20ada3d`, whole (sha256
`e859e47750cd75ebaca96ac26d76e83b3ed25f1c3e40edc2b29218a65c5d84cb`):

~~~~markdown
HEAD: ed37751 (journal-redesign)
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: device
Preserved: 2026-09-28T22:21:24Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** device

# Role

You are the coder for **the tablet photo-viewer measurement**, the one item the tablet sanity check could not measure (terminal `2026-09-28-80`). This dispatch's intent is `2026-09-28-86`. The planner writes the record.

# The owner's ruling, verbatim

"Okay I'm near the tablet. Option A is my choice for the photo viewer question".

Option A, as the planner put it: "Take a camera photo on the DEVICE CHECK find. You'd tap through the camera permission prompt."

# The device

- Only the tablet, `R52T506412L` (`SM-X800`). Pass `-s R52T506412L` on every adb command.
- **Never touch the S22 `R5CT321008R`**, which another coder is using.
- Leave "Stay Awake" and the 5-minute screen timeout as the owner set them.

# Rules

- Never run `logcat -c`.
- Launch with `am start`, never `monkey`.
- Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-tablet-sanity/`, continuing its numbering.
- **Prompts (owner rule "4 A"):** at the camera permission prompt, or any other system prompt, stop at once without touching it. Hand back naming it; the owner taps through, and the planner resumes you.
- **No install.** Use the build already on the tablet, `1.0.1416+gd7cc9f5b`. The photo viewer is a full-screen destination that no later stage changed. Confirm the installed version first.

# What to do

1. Open the find labelled "DEVICE CHECK 2026-09-28 T" and add one photo to it with the app's own camera. It shows whatever the tablet faces; the owner was told.
   - Change nothing else on the find.
   - Touch nothing of the owner's.
   - Record exactly what was created: the photo row and the find's photo reference.
2. Before measuring, write your prediction: the viewer takes the whole window (`ui/log/PhotoViewerDialog.kt:131`, the J6 pulse).
3. Open the photo in the viewer from:
   - the album;
   - the find;
   - a map bubble, if the photo is located.

   In portrait and in landscape at 90, measure the viewer's bounds in dp from the dump. Say whether it covers the drawer, the list and the map, and capture it.
4. Leave the photo on the tablet with the other DEVICE CHECK items (owner "3 A"). Restore rotation and read it back.

# Report

Append a section "Photo viewer (continuation, 2026-09-28-86)" to `docs/audits/2026-09-28-tablet-layout-sanity-run-record.md`. Work on a new branch `device-tablet-2` from `origin/journal-redesign`; the record is already merged there. Quote this dispatch verbatim, and push. The planner merges it. Record:
- the device facts;
- what was created;
- the prediction and measurements;
- the captures;
- what was restored;
- the crash reads;
- **Decisions I made**;
- **Flags outside scope**.

# Abort conditions

- not exactly the one tablet, or a different installed build;
- a locked tablet;
- a new crash;
- any need to touch the owner's records or the S22.

A prompt pauses the run for the owner.

# Merge

Not authorised.
~~~~
