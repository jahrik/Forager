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
