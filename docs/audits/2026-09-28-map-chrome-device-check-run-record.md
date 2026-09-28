# Map-chrome device check, on the S22 Ultra: run record

**Status: pre-registration only.** Forager has not been launched on this build and no app screen has been looked at.
This part is committed and pushed before the first launch, so the order of prediction and observation can be checked.
Results are added in later commits, and this part is kept unchanged.

**Date:** 2026-09-28, from 22:20Z.
**Dispatch:** `prompts/preserved/2026-09-28-84.md` (sha256 `3b3f1f3d…a323d2ce6c`), committed at `ed37751`; its intent is
`2026-09-28-84` in `RECORD.md`, the planner's. The kit is absent at this base (no `.claude/`, no checkers; the owner
removed it in `e136330`), and the dispatch says the planner writes the record, so I write no record entry and do not
touch `RECORD.md`. The standing rules, data rules and abort conditions of `-51` and `-72` apply, and through `-51` the
phone, evidence, privacy and settings rules of `-04` and `-12`.
**Base:** `git fetch` at 22:20Z: `origin/journal-redesign` at `20ada3d`, one planner commit past the dispatch's `ed37751`
(intent `-86`, the tablet photo-viewer measurement; `RECORD.md` and one store copy only). Branch `device-chrome`,
worktree `/home/zynergy-labs/Zynergy/forager-wt/device-chrome`, cut from `20ada3d`, upstream unset so nothing can push
to `journal-redesign`.
**The APK is built at `b358a4a`,** as the dispatch says. `git diff --stat b358a4a 20ada3d` over `app/`,
`build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradle.properties`, `gradlew`, `data/` and `server/` is empty.
**Code citations** are at `b358a4a` (identical under `app/` at `20ada3d`). Paths are under
`app/src/main/java/com/zynergylabs/forager/app/` unless given in full.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-map-chrome-check/`, outside the repository. No
screenshot, dump, map coordinate, note text, place name or photo goes into this file. Screen positions below are
pixels on the phone's screen.
**Device:** SM-S908U, serial `R5CT321008R`, named with `-s` on every call. `adb devices -l` also lists
`R52T506412L` (SM-X800, the tablet, intent `-86`); it is never addressed. `ro.build.id=BP2A.250605.031.A3`. Unlocked
(`isKeyguardShowing=false`), awake. `wm size` 1080 x 2316, `wm density` 450, so 1 dp = 2.8125 px.

## The dispatch, verbatim

From `prompts/preserved/2026-09-28-84.md` at `ed37751`, below its "verbatim prompt follows" line:

> **Type:** device
>
> # Role
>
> You are the coder for **the map-chrome device check on the S22 Ultra** (`R5CT321008R`). The tablet `R52T506412L` is also attached: **never touch it.** Pass `-s R5CT321008R` on every adb command. You observe and record, and change no app code. This dispatch's intent is `2026-09-28-84`. The planner writes the record.
>
> Read first:
> - `CLAUDE.md`;
> - `prompts/preserved/2026-09-28-51.md` and `-72.md`, whose standing rules, data rules and abort conditions apply;
> - the map-chrome completion report, `docs/audits/2026-09-28-record-sheet-over-map-completion-report.md`, especially "Resumed and built" and its device-only list;
> - terminal `2026-09-28-83` in `RECORD.md`;
> - the J8 device check's run record, for how it measured a fill's alpha on this phone.
>
> # Standing rules
>
> - Never run `logcat -c`.
> - Launch with `am start`, never `monkey`.
> - **Stop at once at any system or Google prompt over the app.**
> - Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-map-chrome-check/`.
> - **Create, edit and delete nothing.** Every dialog you open, you cancel. Never confirm a delete, a trip, a waypoint or a find. Restore every setting and read it back.
>
> # Build and install
>
> 1. Before the install, back up and verify the database exactly as `-72` did: force-stop, copy out through `run-as`, then check sha256, header, integrity, `user_version` (16 expected) and every table's row count. **If this fails, stop.**
> 2. Build a debug APK from `origin/journal-redesign` at `b358a4a` (the map-chrome and save-failure stages; `ForagerDatabase.version` is still 16) in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-chrome` (branch `device-chrome`). Before `assembleDebug`, check that no other Gradle build is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is free. Wait if either fails.
> 3. `install -r` only, with the same certificate. `firstInstallTime` unchanged. Confirm afterwards that `user_version` is still 16 and every row count is unchanged.
>
> # The checks, cheapest first
>
> For each surface:
> - write the pass condition from the report and the code (file:line) and your prediction before you look;
> - measure whether the map shows through it, and its composite alpha, the way the J8 check did (the surface over a distinctive map region, against the map alone);
> - capture it for the owner.
>
> **The one question these checks answer is whether the map shows through on a real screen.** The owner judges the look.
>
> 1. **The three surfaces outside the map's Box, flagged first by the report:** the search dropdown, the Tools drawer and the compact snackbar, all over the Maps tab. The comment at `AvailabilityCompactMapUi.kt:326-341` says a translucent surface in that position once shipped opaque. For the snackbar, use one that the app shows without any data change. If none can be raised without creating, editing or deleting, record it as not run.
> 2. **Separate windows over the Maps map:**
>    - the details sheet from a waypoint, a track and an offline region's bubble;
>    - the trip-date dialog and the waypoint-name dialog, both reached from placement and **cancelled**;
>    - the species suggestions (type a species name on the Maps tab) and the Month menu.
> 3. **In-tree surfaces:** the centre-pin OK/Cancel row (**cancel it**), the search notice if it can be raised without a data change, the bottom nav and rail, the compass strip, the taxon chip and J8's chip. J8's chip read 0.833 to 0.840 in `-72`; that is a known item, so re-measure it but do not rule on it.
> 4. **The Journal:**
>    - the Records sheet from Offline maps (0.8) and from All (solid);
>    - an entry with a map: its overflow menu and its delete dialog, **cancelled**, both at 0.8;
>    - an entry without a map, if one exists, where both stay solid.
> 5. **Legibility captures** of each surface over Topographical, Street and Satellite, by day and at night, **for the owner to judge.**
> 6. **Rotations:** repeat 1 and 3 at 90 and 270, as far as the surfaces exist there (the rail replaces the nav).
> 7. **Not run:**
>    - the exit-navigation prompt and the HUD, which need an active return;
>    - the three-way dialog and the wide Layers button, which are wide-only, for the tablet;
>    - the save-failure Toast, which needs a forced write failure.
>
> # Report
>
> Write `docs/audits/2026-09-28-map-chrome-device-check-run-record.md` on `device-chrome`, in the `-72` record's format. Push after each check; the planner merges it. Record:
> - the backup;
> - the install facts;
> - every verdict with its measured alpha;
> - the owner-judged captures by name;
> - what was restored and read back;
> - the crash reads;
> - **Decisions I made**;
> - **Flags outside scope**.
>
> # Abort conditions
>
> - a backup that cannot be verified;
> - an install needing uninstall, `-d` or a data clear;
> - a signature mismatch;
> - a database change after the install;
> - a new Forager crash;
> - any prompt over the app;
> - a locked phone;
> - any need to create, edit or delete data;
> - touching the tablet.
>
> # Merge
>
> Not authorised.

## The launch message, verbatim

The planner's message that launched me. The store copy above governs over it.

> This is planner dispatch `2026-09-28-84`, **the map-chrome device check on the S22 Ultra** (`R5CT321008R`). Its store copy is committed at `prompts/preserved/2026-09-28-84.md` on `origin/journal-redesign` at `ed37751`. Fetch it, read it in full and quote it verbatim in your run record; the file governs over this message.
>
> The essentials, stated in full in the file:
> - **Two devices are attached.** Pass `-s R5CT321008R` on every adb command, and never touch the tablet `R52T506412L`.
> - **Back up and verify the database before installing**, exactly as `-72` did. If that fails, stop.
> - **Build** `b358a4a` in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-chrome` (branch `device-chrome`). Before `assembleDebug`, check that no other Gradle build is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is free. A night-outline build coder shares the machine.
> - **Install** with `install -r` only. Afterwards, `user_version` is 16 and every count is unchanged.
> - **Create, edit and delete nothing.** Cancel every dialog, and never confirm a delete, trip, waypoint or find. Stop at once at any system or Google prompt.
> - **The question:** does the map show through each surface on a real screen? Measure the composite alpha, taking the dropdown, the Tools drawer and the snackbar first. Capture legibility over every basemap by day and night for the owner to judge.
> - **Push the run record after each check.** The planner merges it and writes the record. You do not touch `RECORD.md`. Merge is not authorised.
>
> When you finish or stop, hand back a report: backup and install figures, every verdict with its alpha, the captures by name, what was restored and read back, crash reads, decisions you made and flags.

## Before the install (no app screen looked at)

### The phone at the start (22:23Z)

- **Installed:** `versionName=1.0.1389+g99de6c24`, `versionCode=1389`, `lastUpdateTime=2026-09-28 14:03:09` (phone time,
  PDT), user 0's `firstInstallTime=2026-09-22 11:15:05`, `ceDataInode=2495587`, `deDataInode=2494123`
  (`01-dumpsys-package-start.txt`). That is J8's build, as `-72` left it. Forager was running as pid 17613 (J8's final
  relaunch) with `MainActivity` in focus.
- **Crash buffer:** `logcat -d -b crash`, **0 bytes** (`02-crash-start.txt`).
- **System settings** (`03-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
  `navigation_mode=0` (three-button), `location_mode=3`, `cmd uimode night`: yes (so the app composes its dark theme),
  `screen_off_timeout=600000`, `stay_on_while_plugged_in=15`, the three animation scales 1.0.
- **App settings** (`05-*-start.pb`, decoded by `pbprefs.py`, map coordinates redacted): `map.fullscreen=false`,
  `night_mode.maps=false` (Night Maps off), the offline picker's last pick, every overlay key `true` including
  `journal-entry-tracks-layer`, `forecast-chanterelles` opacity 1.0, `map.layer_order` the registry's order;
  `diagnostics.synthetic_forecast=false`; camera grid `GridLevel`, lock-to-portrait false; photo location auto-save
  true. `databases/` holds `forager.db`, `-wal` and `-shm` only (`04-app-files-start.txt`).

### The database backup (22:24Z)

`-72`'s method, with its scripts copied into this evidence directory (paths changed only): `dbcopy.sh`, `dbverify.sh`,
and `dbdigest.py`, which here hashes **every** column, `shownOnMap` included, since this check changes nothing.
- `am force-stop com.zynergylabs.forager.app` at 22:24:17Z; `pidof` then empty (`06-force-stop-time.txt`).
- `dbcopy.sh db-backup`: the device's own `sha256sum` of each file through `run-as`, a byte copy of each through
  `exec-out run-as … cat` into `db-backup-raw/` (then made read-only), and the local sha256. **They match:**

  | file | bytes | sha256 (device = local) |
  |---|---|---|
  | `forager.db` | 229376 | `e88effc0cdcde55dd96cae939b27a9d7b539c2a377c375a51cdac8ad61af7c93` |
  | `forager.db-wal` | 466944 | `55db76e43aed8653c39f1182b09cc8edac8166eac8a510a1313f770a31ea95c3` |
  | `forager.db-shm` | 32768 | `3ddec32f0a49f74fd92e2877ccb4ff265174f02327e5cc82825814622ea89459` |

  `forager.db` and the WAL are byte for byte J8's end copy (`-72`'s restore section); the SHM differs, as it does after
  any launch. Every query runs on a second copy, `db-backup-query/`.
- **Verified on this machine** (`db-backup-verify.txt`), SQLite 3.50.6:
  - header `53514c69746520666f726d6174203300` ("SQLite format 3\0"), page size 4096, write/read version 2/2 (WAL);
    WAL magic `377f0682`, a valid WAL header;
  - `PRAGMA integrity_check`: **ok**;
  - `PRAGMA user_version`: **16**;
  - row counts, all 18 tables: android_metadata 1, cached_searches 2, cartography_entries 7,
    cartography_entry_find_refs 2, cartography_entry_offline_region_refs 1, cartography_entry_photo_refs 1,
    cartography_entry_track_refs 1, cartography_entry_waypoint_refs 3, log_entry_photos 1, log_photos 3,
    mushroom_log_entries 3, offline_regions 2, planned_trips 0, room_master_table 1, sqlite_sequence 1, track_points 23,
    tracks 1, waypoints 3.
  - Per-table row digests, every column (`db-backup-digest.txt`). Computed `-72`'s way (without `shownOnMap`), they
    equal J8's end digests for all 18 tables (`db-backup-digest-j8form.txt`): nothing was written between J8's end and
    this backup.
- **Nothing failed, so no stop.**
- **Re-checked just before the install** (22:28:43Z): the app not running, the phone unlocked, and the device's sha256 of
  all three files equal to the backup's (`11-pre-install-device-sha256.txt`).

### Data inventory (from `db-backup-query/`, ids, flags and counts only; `07-entries-inventory.txt`)

- **Entries:** 2 saved, 5 drafts.
  - `6107d76c…`, saved, dated 2026-09-27, labelled DEVICE CHECK (the L0a entry), with **8 kept records**: it has a map
    in its report. `shownOnMap` 0.
  - `6380d39b…`, saved, dated 2026-09-28, blank text, **0 kept records** (Part A's item 24 entry): no map in its report.
  - Five drafts, each labelled DEVICE CHECK, no kept records.
- **Records:** waypoints 3 (ORIGIN and END of the one track, which the map does not draw, and one DEVICE CHECK
  waypoint); tracks 1 (unnamed, 23 points); offline regions 2 (both DEVICE CHECK: the 1 km one and "B", 5 km); finds 3,
  of which **one is a draft** (`1024c923…`, `isDraft` 1, `draftOfEntryId` null: the P:321 "Log a find" draft of `-12`);
  photos 3; planned trips 0; cached searches 2.

### Build

- Before Gradle (22:24Z): `pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'` empty (exit 1); 3398 MB available
  (`free -m`), above the 2.5 GB floor; `/` 7.6 GB free. A Gradle daemon and a Kotlin daemon from other sessions were
  resident, idle.
- `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug` in the worktree, detached at `b358a4a` with a clean tree, 22:25:02Z
  to 22:26:06Z: **BUILD SUCCESSFUL in 1m 3s**, exit 0, **0** `e: ` lines (`08-build.log`). The worktree then went back
  to `device-chrome`, clean.
- **APK** (`chrome-debug.apk`, a copy of `app/build/outputs/apk/debug/app-debug.apk`; `09-apk-facts.txt`, build-tools
  37.0.0): sha256 `db8a355c6af5316a1d747d3e875b5430dc8bbbdd48ab7df2c650c64112eb2a69`;
  `versionName=1.0.1457+gb358a4aa`, `versionCode=1457`, no `.dirty`; signer `CN=Android Debug, O=Android, C=US`,
  certificate SHA-256 `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`.
- **The installed APK**, pulled read-only (`installed-before.apk`, `10-installed-before-cert.txt`): sha256
  `629d3225…2dd9807a3`, which is J8's `j8-debug.apk`, `versionCode=1389`, certificate SHA-256 `cb2f6da5…94f1626`.
  **Same certificate: no signature mismatch.** 1457 is above 1389, so `-r` needs no `-d`.
- `ForagerDatabase.version = 16` at `b358a4a` (`data/local/ForagerDatabase.kt:167`), and `git diff --stat 99de6c2
  b358a4a` over `data/` and `app/schemas/` is empty: no migration runs.

### Install

- `adb -s R5CT321008R install -r chrome-debug.apk` at 22:28:48Z: `Success` (`12-install.txt`). No uninstall, no `-d`,
  no data clear.
- **After** (`13-dumpsys-package-after.txt`): `versionName=1.0.1457+gb358a4aa`, `versionCode=1457`, `lastUpdateTime`
  15:28:49 PDT, user 0's `firstInstallTime=2026-09-22 11:15:05`, **unchanged**, with the same `ceDataInode` 2495587 and
  `deDataInode` 2494123. User 95 is listed as before.
- The app was not running after the install. The device's sha256 of all three database files still equals the backup's
  (`14-post-install-device-sha256.txt`). `user_version` and the counts after the first launch are read in check 0 below.

# Pre-registration: pass conditions and predictions, written before the first launch

Every touch is a real `adb shell input` touch at screen coordinates from a fresh dump or screenshot. The app is launched
only with `am start -n com.zynergylabs.forager.app/.MainActivity`. If any system or Google prompt appears over the app, I
stop without touching it. Verdicts that rest on my reading of an image say so. Rotations are set with
`settings put system user_rotation` (1 = 90, 3 = 270) with `accelerometer_rotation` 0, as `-72` did. Dialogs are
dismissed with **Back**, which runs each dialog's own `onDismissRequest` (the same state change as its Cancel button,
cited per surface below), so no touch ever lands beside a confirm button.

## The measurement, for every surface

`-72`'s method (`alpha.py`, copied unchanged): two frames at the same camera, **A** with the surface absent (the map
alone) and **B** with it present. Each channel of B is fitted as `B = a·A + b` over a box inside the surface's fill,
clear of its text, icons and edges, sampled every second pixel. The box is chosen from the dump's bounds and the frame
and is stated with each figure.
- **Composite opacity** `1 − a`: the share of the map's light that everything above it at that pixel hides (the fill,
  any shadow under it, any scrim, any panel it is stacked on). This is the figure the dispatch asks for.
- For a surface in a window over a scrim (sheets, dialogs, the drawer), a second box in the scrim alone gives `s`, and
  the **fill's own alpha** is `1 − a/s` (the Stage Part 1 record's method for the Layers sheet, which read 0.798 on this
  phone). For a popup stacked on the 0.8 search panel, the same with the panel-alone frame in place of the scrim.
- **Container colour** from the intercept: `b / (1 − a)` for a single fill; `(b − (a/s)·b_s) / (1 − a/s)` for a fill
  over a scrim whose own intercept is `b_s`.
- **A pair is void** if a control box on the map, away from all chrome and outside any scrim, fits worse than slope
  1.000 ± 0.01 with rms ≤ 1.5 (the camera moved), and I say so. **A box is unreadable** if its ground has a spread
  (standard deviation in A) under 8 per channel, since no slope can be read off a flat ground, and I say so.

**The verdict** answers the dispatch's one question:
- **"shows the map through"** when `a` (or `a/s`) is at least 0.10 and the fit tracks the ground (rms ≤ 3);
- **"opaque"** when `a` (or `a/s`) is at most 0.02;
- **"at 0.8"** additionally when the fill's own alpha is 0.78 to 0.82, `-72`'s band. Outside that band I report the
  figure and do not rule, because `-72` read J8's chip at 0.833 to 0.840 over its own 4 dp shadow, and several surfaces
  here carry a shadow.

The dark theme is the one composed (`uimode night` yes). Its roles: `surface` `#1B1B1B` (`ui/theme/Theme.kt:97`,
`ui/theme/Color.kt:17`), `surfaceContainerLow` `#1B1B1B`, `surfaceContainer` `#202020`, `surfaceContainerHigh`
`#2B2B2B` (`Theme.kt:103-105`, `Color.kt:140-142`), `errorContainer` `#6B2222` (`Theme.kt:92`, `Color.kt:122`),
`scrim` Bark `#3B2E24` (`Theme.kt:113`). `inverseSurface` is not set in `DarkColors`, so it is Material3's baseline (not read at this version).
Material3's default scrim alpha for sheets and drawers (0.32) is Material3's, not read here.

## Check 0: the first launch, the database and crashes

- **Method:** `am start`, then wait until the Maps tab has drawn the stored records (the records come from Room, so the
  database is open), or 30 s. Crash reads. Then `am force-stop`, `dbcopy.sh db-launch`, `dbverify.sh db-launch`,
  `dbdigest.py`.
- **Pass:** the copy matches the device; header valid; integrity ok; **`user_version` 16**; the same 18 tables with the
  backup's counts; every table's digest equal to the backup's (nothing written); no crash line, no Forager `FATAL
  EXCEPTION`.
- **Abort:** a version other than 16, a count or digest mismatch, a crash. Reported at once; nothing restored without
  the owner.
- The same database read (`dbcopy.sh`, digests against the backup's) follows every check below that could have written
  (the snackbar path above all), and ends the run.
- **Prediction:** pass.

## Check 1: the three surfaces outside the map's Box, over the Maps tab

`AvailabilityCompactMapUi.kt:326-341`: a translucent bar composed "one level further out" than the map's own Box
"shipped fully opaque on a real device despite an identical `Surface`/color/alpha". The map is MapLibre's default view
(no `textureMode` anywhere in `app/src/main`, so a `SurfaceView`); why placement would change blending is not something
the code says.

**1a. The search dropdown.** Composed in the scaffold's own Box (`AvailabilityCompactScaffold.kt:1162-1211`), outside
`CompactMapTab` (`:808`), whose Box holds the map. Fill: a `Box.background` of `CompassStripBackgroundColorDark`, Bark
at 0.8 (`AvailabilitySearchUi.kt:405-411`; `AvailabilityMapControlsUi.kt:92`). Its dismiss scrim is a bare tap
interceptor with no fill (`AvailabilityCompactScaffold.kt:1102-1153`).
- **Raise:** one touch on the search bar's text opens it with the keyboard up; in portrait the panel lies under the
  keyboard and one Back lowers the keyboard with the panel left open (Stage Part 1, check 8). A is the frame before the
  touch. Nothing is typed, no row is chosen. Closed with Back.
- **Predicted:** `a` 0.20 (composite 0.80), intercept 0.8 × Bark = (47.2, 36.8, 28.8).

**1b. The Tools drawer.** `ModalDrawerSheet` in the `ModalNavigationDrawer` at `AvailabilityScreen.kt:1819-1845`;
`drawerColor = mapChromeFill(DrawerDefaults.modalContainerColor, compactTab == CompactTab.MAP)` (`:1839`), the
`surfaceContainerLow` role (Material3's, not read), `#1B1B1B`. Material3's drawer scrim over everything else.
- **Raise:** the bottom nav's "Tools" item. A is the frame before. No control inside is touched. Closed with Back.
- **Predicted:** `s` 0.68, `a` 0.136, fill 0.80, composite 0.864; container (27, 27, 27).

**1c. The compact snackbar.** `SnackbarHost(logDraftSnackbarHostState)` in the Scaffold's `snackbarHost` slot
(`AvailabilityCompactScaffold.kt:592-609`), `snackbarColor = mapChromeFill(SnackbarDefaults.color, compactTab() ==
CompactTab.MAP)` (`:597`). No scrim.
- **Which snackbar.** The host shows four kinds (`AvailabilityScreen.kt:1185-1233`): pending-delete Undo (needs a
  delete), the trip-start and network-fix notices (need a recording), and **"Saved to Drafts"** (`:1211-1233`),
  offered when leaving a find's editor keeps a draft (`leaveKeepsDraft`, `ui/log/MushroomLogViewModel.kt:1200-1201`).
  The phone holds one draft find (`1024c923…`, `draftOfEntryId` null). Opening a draft is `onOpenEntry` (`:386-394`,
  "Creates nothing"); leaving by the bottom nav runs `onLeaveEditingIncidentally` (`:582-633`), whose branch for a new
  find's draft only updates UI state; the find form writes only on a field change (`ui/log/LogEntryDetailScreen.kt:189`,
  `:213`; `ui/log/JournalTab.kt:467` on a location pick). So by the code this path writes nothing.
- **Raise:** Journal, open the draft find (the path is read from the dumps on the phone; if opening it needs anything
  beyond a touch on its row, **not run**), then the bottom nav's Maps item. The snackbar's **Discard is never touched**;
  it times out (`SnackbarDuration.Short`). B is the frame with the snackbar; A the same camera after it has gone. Then a
  database copy: **every digest must equal the backup's; any difference is an abort.**
- **Predicted:** raised with no write; `a` 0.20; the container colour whatever Material3's `inverseSurface` is, read
  from the intercept.

**Prediction for check 1 (mine):** all three show the map through at 0.8. I cannot derive from the code why the bar once
shipped opaque, so this is a weak prediction; the planner predicted the opposite for at least one of them.

## Check 2: separate windows over the Maps map

**2a. The details sheet from a bubble:** a waypoint (the DEVICE CHECK waypoint), the track, and the 1 km DEVICE CHECK
offline region. `RecordDetailsSheet` (`ui/log/RecordDetailsSheet.kt:174-186`): `ModalBottomSheet`,
`mapChromeFill(BottomSheetDefaults.ContainerColor, overMap)` (`:179`), `overMap = true` from the bubble
(`ui/map/MapBubble.kt:312-325`); Material3's default scrim. Raised by a real touch on the record, then the bubble's
"Details" (`MapBubble.kt:402`, `:411`, `:430`). "Directions" and "Share" are never touched. Closed with Back.
- **Predicted:** `s` 0.68, `a` 0.136, fill 0.80 (the Layers sheet's container and scrim, 0.798 in Part 1).

**2b. The trip-date dialog.** `TripDatePickerDialog` (`ui/availability/AvailabilityMapOverlaysUi.kt:121-178`): the
dialog's container `DatePickerDefaults.colors().containerColor` at 0.8 (`:143-145`), the picker inside clear (`:146`).
Raised by the + disc, "Plan a trip", then the centre pin's "OK", which only sets `pendingTripLocation`
(`AvailabilityCompactMapUi.kt:1337-1345`). Back runs `onDismiss` (`:1379`), as "Cancel" does (`AvailabilityMapOverlaysUi.kt:164`);
"Plan trip" is never touched. The dialog window's dim is the platform's (not read), measured as `s`.
- **Predicted:** fill 0.80; container `#2B2B2B` if the default is `surfaceContainerHigh` (`-83`'s R13 message read
  (0.169, 0.169, 0.169), which is 43).

**2c. The waypoint-name dialog.** `WaypointNameDialog` (`AvailabilityMapOverlaysUi.kt:203-245`): `surface` at 0.8
(`:215`), `shadowElevation = 4.dp` (`:220`). Raised by +, "Drop waypoint", "OK" (sets `pendingWaypointLocation` only,
`AvailabilityCompactMapUi.kt:1342`). Back runs `onDismiss` (`:1390`), as its "Cancel" does (`AvailabilityMapOverlaysUi.kt:241`); the confirm is never
touched. If the field takes
focus and raises the keyboard, the first Back lowers it and the second closes the dialog.
- **Predicted:** fill 0.80 at its centre; up to about 0.84 if its shadow reaches under the fill, as J8's chip did.

**2d. The species suggestions.** `ExposedDropdownMenu` (`AvailabilitySearchUi.kt:1157-1164`) at
`mapChromeFill(MenuDefaults.containerColor, suggestionsOverMap)`; the Maps bar passes `overMap = true`
(`AvailabilityCompactScaffold.kt:976`, through `AvailabilitySearchUi.kt:297`). The source is the local fungi index
(`AppContainer.kt:166`; `data/repository/LocalFungiIndexRepository.kt:26-41`): a read, no network, no write. Raised by
typing a species name into the Maps bar with `input text`; no suggestion is chosen. Closed with Backs.
- **Predicted:** against the panel alone, its own alpha 0.80; composite against the map alone about 0.96 where it lies
  on the panel (the owner's "1 A" stacking), 0.80 where it overhangs the map.

**2e. The Month menu.** `ExposedDropdownMenu` (`AvailabilitySearchUi.kt:1221-1229`), `overMap = compactTab() ==
CompactTab.MAP` (`AvailabilityCompactScaffold.kt:1240`). Raised from the panel's Month field; no month is chosen;
closed with Back.
- **Predicted:** as 2d.

## Check 3: in-tree surfaces

**3a. The centre-pin OK/Cancel row.** `CentrePinLocationPickerOverlay` (`ui/map/CentrePinLocationPicker.kt:292-305`):
`surface` at 0.8, `shadowElevation = 4.dp`. Raised by +, "Plan a trip". **Cancelled** with its "Cancel" (`:391`,
`onCancel` sets `pendingAction = null`, `AvailabilityCompactMapUi.kt:1347`), a real touch at the button's centre from
a fresh dump; A is the frame after the cancel, at the same camera.
- **Predicted:** fill 0.80, up to about 0.84 by its shadow.

**3b. The search notice.** `SearchNotice` (`AvailabilitySearchUi.kt:589-612`): `errorContainer` at 0.8 when `overMap`,
which the Maps tab's call inside the map's Box passes (`AvailabilityCompactScaffold.kt:978`). **Raised without a data
change** by "Search this location" with both coordinate fields empty: `searchManualCoordinates`
(`AvailabilityViewModel.kt:398-404`) sets the validation message and returns before any search. **The dump must show
Latitude and Longitude empty before the touch; if either holds text, not run.** The message clears only when a search
starts (`:413`), so it is cleared by `am force-stop` and `am start` (UI state only).
- **Predicted:** `a` 0.20, container (107, 34, 34).

**3c. The bottom nav.** `ForagerBottomNav` with `surfaceContainer` at 0.8 (`AvailabilityCompactMapUi.kt:1227-1231`),
in the map's Box. It slides away in fullscreen (`:1222`) while the map keeps its size, so **A is the fullscreen frame**
(the cluster's Fullscreen control; `map.fullscreen` is persisted, restored to false and read back).
- **Predicted:** `a` 0.20, container (32, 32, 32).

**3d. The rail** (at 90 and 270, check 6): the same fill (`:1263-1268`), the same fullscreen reference.

**3e. The compass strip.** `AvailabilityMapControlsUi.kt:327`, Bark at 0.8. It is not composed only while navigating
(`AvailabilityCompactMapUi.kt:1047`), which is out of bounds, so there is no frame without it. In fullscreen it moves up
into the search bar's band (`topInset`, `:1072`), which may uncover its portrait band. **If the fullscreen dump
shows nothing over that band, A is the fullscreen frame; if not, the strip is not measurable, and I say so.**
- **Predicted:** `a` 0.20, intercept (47.2, 36.8, 28.8).

**3f. The taxon chip.** Composed only with a taxon filter (`AvailabilityCompactMapUi.kt:1091`, `:1112`), which needs a
completed species search (`:577-588`). A search fetches from the network and caches in Room (`cached_searches`,
`data/repository/RoomSearchCacheRepository.kt`). **Not run: it needs a data change.**

**3g. J8's chip.** Composed only while an entry is shown on the map (`AvailabilityCompactMapUi.kt:1091`, `:1114`), and
showing one writes `cartography_entries.shownOnMap`. `-72`'s data rule allowed that toggle on DEVICE CHECK entries;
`-84` says "Create, edit and delete nothing" and lists "any need to create, edit or delete data" as an abort
condition. **Not run: it needs a data edit this dispatch forbids.** Raised as a question in the hand-back.

## Check 4: the Journal

**4a. The Records sheet from Offline maps:** the same `RecordDetailsSheet`, `overMap = selectedTab ==
RecordsSubTab.OFFLINE_MAPS` (`ui/log/RecordsTab.kt:330`). A real touch on a downloaded-region row (a tap, never a swipe
or long press); closed with Back. Measured over the panel's picker map where the sheet overlaps it; if it does not
overlap the map at all in portrait, the figure is over the list, and I say so.
- **Predicted:** fill 0.80.

**4b. From All:** the same sheet, solid. A row of a waypoint, track or region.
- **Predicted:** opaque, `a/s` at most 0.02, container `#1B1B1B`.

**4c. The entry with a map** (`6107d76c…`): its overflow menu (`ui/log/CartographyEntryReportScreen.kt:406-414`,
`MenuDefaults.containerColor` at 0.8 when `entryMapShown`, `:379`) and its delete dialog (`:662-676`,
`AlertDialogDefaults.containerColor` at 0.8). The menu is opened from "Entry options" and closed with Back; the dialog is
opened by the menu's "Delete entry" (which only sets `confirmingDelete`, `:432-434`) and **dismissed with Back**
(`onDismissRequest`, `:665`), never "Delete". "Show on map" and "Edit entry" are never touched. Each is measured where
it lies over the preview map; where it does not overlap the map, I say so and report what can be read.
- **Predicted:** both at 0.8 over the map.

**4d. The entry without a map** (`6380d39b…`): the same menu and dialog, **solid**.
- **Predicted:** opaque over the report's own content.

## Check 5: legibility captures, for the owner

Each surface above that can be raised, over Topographical, Street and Satellite (the Layers sheet's map types), each with
Night Maps off ("by day") and on ("at night"). Captures are named in the verdict. **The owner judges the look; I do not
rule on it.** The chrome's fills follow the app theme, not Night Maps (`AvailabilityMapControlsUi.kt:94`, the Light
colour's comment), so only the map beneath changes. Night Maps and the basemap are restored and read back.

## Check 6: rotations

Checks 1 and 3 again at `user_rotation` 1 (90) and 3 (270), as far as each surface exists there; the rail replaces the
nav. Restored to 0 and read back.

## Check 7: not run, as the dispatch says

The exit-navigation prompt and the HUD (need an active return); the three-way dialog and the wide Layers button (wide
only, for the tablet); the save-failure Toast (needs a forced write failure).

## Settings to restore

`user_rotation` 0, `accelerometer_rotation` 0; Night Maps off; basemap Topographical; `map.fullscreen` false; every
overlay as found; `font_scale` 1.0 (not planned to change); the app left in focus on the Maps tab in portrait, as found.
The final database read (above) closes the run.

## Check 1c, an addition to the pre-registration, written after its first attempt (22:44Z) and before its second

**The premise was wrong.** Switching tabs no longer leaves an open find: since intent `-44`'s F3, the bottom nav's
handler keeps the find open on the Journal (`AvailabilityCompactScaffold.kt:457-464`, the comment on
`onBottomNavTabSelected`). I missed that comment when I wrote the path above. On the phone, the draft find opened in its
editor (`30-`), and the Maps item at 22:44:05Z brought up the Maps tab with **no snackbar** (`31-c1c-snackbar-B`, a
screenshot 0.9 s after the touch and a dump after it; `32-` 7 s later). The draft find is still open on the Journal.

**The second path.** The editor's own exits still call the snackbar wrapper: its back arrow is `onBack =
onLeaveEditingIncidentally` (`ui/log/JournalTab.kt:506`), and the Journal's BackHandler runs the same
(`JournalTab.kt:436`, `:439`), which is `leaveLogEntryEditingOfferingDiscard` (`AvailabilityCompactScaffold.kt:1005`;
`AvailabilityScreen.kt:1212-1231`). That is the same `onLeaveEditingIncidentally` branch as above (UI state only), so by
the code it writes nothing either. The snackbar then shows over the Journal, where it is solid; its host is the compact
Scaffold's, shared by every tab, and its colour follows `compactTab()` (`:597`).
- **Raise:** Journal; one system Back in the find's editor; then, at once, the Maps item. **B** is a screenshot as soon
  as the Maps tab shows with the snackbar; **A** the same camera after it has timed out. No other touch until it has
  gone: after the tab change the snackbar may sit over the nav, and its **Discard** must not be touched.
- **Pass:** the snackbar is in B over the Maps map, and the fit as pre-registered. Then the database read: every digest
  equal to the backup's, or abort.
- If this snackbar does not reach the Maps tab either, **1c is not run** (no other snackbar is available without a data
  change), and I say so.

# Verdicts

## Check 0: the first launch, the database and crashes: **pass**

- `am start` at 22:40:44.8Z (`15-launch.txt`); pid 25721, `MainActivity` in focus; no system or Google prompt. By
  22:40:53Z the Maps tab had drawn the stored records (both DEVICE CHECK region circles and the markers, `16-first-launch`,
  by my reading), so the database had been opened.
- Crash reads: `logcat -d -b crash` **0 bytes** (`17-`); `logcat -d`, 319,588 lines, **0** `FATAL EXCEPTION` (`18-`).
- `am force-stop` at 22:41:09Z; `dbcopy.sh db-launch`: copy matches the device. `db-launch-verify.txt`: header valid,
  WAL magic valid, **integrity ok, `user_version` 16**, the same 18 tables with **the backup's counts**, and
  `db-launch-digest.txt` **equal to the backup's for all 18 tables**: the launch wrote nothing.
- **Prediction:** held.

## Check 1: the three surfaces outside the map's Box: **all three show the map through, at 0.8**

Relaunched at 22:41:18.9Z (pid 26049), the opening camera about zoom 12 on Topographical, Night Maps off. Every figure
below is `alpha.py` over the named frames; "composite" is `1 − a`.

**1a. The search dropdown: shows the map through, at 0.8. Pass.**
- A `20-c1-maps-A` (22:41:27Z); a real touch on the bar's text at (500, 132) at 22:41:36Z opened the panel with the
  keyboard up (`21-`, `mInputShown=true`); one Back lowered the keyboard and left the panel open (`22-c1-dropdown-B`,
  `mInputShown=false`). The panel's scroll area is `[0,250][1080,1787]`. Nothing typed, no row chosen.
- Fits, A = `20-`, B = `22-`:

  | box (inside the panel, clear of text) | a (R, G, B) | composite | intercept b | rms | ground spread |
  |---|---|---|---|---|---|
  | `[430,1010][940,1130]` (right of "Recent searches") | 0.200, 0.200, 0.201 | **0.800** | 47.0, 37.0, 28.9 | 0.28 | 37-39 |
  | `[450,465][880,610]` (right of the radius label) | 0.194, 0.195, 0.194 | 0.805 | 47.7, 37.8, 30.2 | 1.5-2.9 | 40-49 |
  | `[60,1715][880,1780]` (below "Search this location") | 0.193 | 0.807 | 47.8, 38.0, 30.3 | 1.3-2.5 | 31-42 |
  | control, map below the panel, `[300,1800][1000,1895]` | 1.000 | — | 0 | 0.00 | 23-34 |

  The container solved from the clean box, b / (1 − a), is (58.8, 46.3, 36.2): **Bark (59, 46, 36)** at 0.8. The other
  two boxes read 0.805 and 0.807 with a larger rms; something small in them (the slider's track shadow or the region
  circle's dashes) is my guess, not established.
- By my reading of `22-` the map's roads, river and contours read clearly through the panel.
- One Back closed it (22:42:18Z, `23-`); the camera had not moved (`20-` against `23-`, slope 1.000, rms 0).

**1b. The Tools drawer: shows the map through; fill 0.80, composite 0.863. Pass.**
- A `23-`; the nav's "Tools" at (981, 2068) at 22:42:30Z; B `24-c1-drawer-B`. The sheet is `[0,0][1013,2316]` (360 dp),
  the scrim takes the rest, `x ≥ 1013`. Nothing inside was touched; Back closed it (`25-`, camera unchanged: slope 1.000,
  rms 0 against `23-`).
- Fits, A = `23-`, B = `24-`:

  | box | a (R, G, B) | b | rms |
  |---|---|---|---|
  | scrim alone `[1020,400][1076,900]` | **s** = 0.677, 0.679, 0.676 | 19.2, 14.9, 12.3 | 0.33 |
  | scrim alone `[1020,1200][1076,1850]` | 0.677, 0.679, 0.676 | 19.2, 14.9, 12.4 | 0.35 |
  | sheet `[50,400][850,900]` | 0.137, 0.136, 0.137 | 26.0, 25.1, 24.0 | 0.27-0.30 |
  | sheet `[50,1250][850,1800]` | 0.137, 0.137, 0.137 | 26.0, 25.0, 24.0 | 0.28 |

  The scrim is Bark at 0.32 as predicted: s 0.68, and b_s = 0.32 × Bark (18.9, 14.7, 11.5). **Fill alpha 1 − a/s =
  0.797 to 0.800; composite 1 − a = 0.863 to 0.864.** The container solved over the scrim is (27.7, 27.7, 27.0), which is
  `#1B1B1B`. There is no map box outside the drawer and its scrim, so the camera check is `23-` against `25-`.

**1c. The compact snackbar: shows the map through, composite 0.80 to 0.82 over the map. Pass, by the second path.**
- **First path, 22:43Z to 22:44Z: no snackbar**, as the addition above records (`26-` to `32-`). Its premise was wrong.
- **Second path** (22:45:27Z to 22:45:53Z): Journal (the draft find still open in its editor, `33-`, a dump identical to
  `30-`); **one Back** at 22:45:38.7Z; the Maps item at 22:45:39.3Z. `34-c1c-snackbar-B` (screenshot 0.9 s after the
  touch): **"Saved to Drafts" with "Discard" over the Maps tab**, at the bottom of the window, `y` 2147 to 2282, over
  the lower part of the app's nav and under the system navigation bar's buttons (the system bar is a separate window
  above the app). `35-` (7 s later): gone, the camera unchanged (`[50,300][850,900]`, slope 1.000, rms 0). Discard was
  never touched.
  - There its ground is the nav's own 0.8 fill over the map, so the ground's spread is only 4 to 8: under the 8 I
    pre-registered, so **not a reading by my own rule**. For the record: `[100,2152][800,2182]` fits a = 0.199, 0.198,
    0.201, rms 0.41.
- **Third run, for a readable ground** (22:47Z to 22:48Z): relaunched (pid 27726), the same path to the editor
  (`38-` to `42-`), Back at 22:48:01.3Z, Maps at 22:48:01.8Z, then **Fullscreen** at (989, 740) at 22:48:02.4Z, so the
  nav slid away while the snackbar showed. `43-c1c3-snackbar-fs-B` 1 s later: the snackbar straight over the map (the
  attribution caption and the map's lines visible through it, by my reading). `44-c1c3-fs-A` after it had gone,
  fullscreen, the same camera (`[50,300][850,900]` slope 1.000, rms 0).

  | box (inside the snackbar, clear of its text, "Discard" and the system bar's buttons) | a (R, G, B) | composite | b | rms | spread |
  |---|---|---|---|---|---|
  | `[420,2152][800,2205]` | 0.195, 0.196, 0.186 | **0.804 to 0.814** | 181.6, 176.4, 185.3 | 2.0-2.1 | 21-31 |
  | `[580,2152][800,2255]` | 0.181, 0.179, 0.187 | 0.813 to 0.821 | 185.6, 180.9, 186.5 | 2.1-2.2 | 29-35 |
  | `[40,2152][76,2276]` | 0.181, 0.184, 0.184 | 0.816 to 0.819 | 183.9, 178.6, 185.7 | 2.3-2.7 | 64-78 |

  The container solved from the first box is (225.6, 219.4, 227.6), within 5 per channel of Material3's baseline dark
  `inverseSurface` `#E6E0E9` (230, 224, 233), which I take from Material3's tokens, not read at this version. The
  slightly higher composite and rms than the panels', and the colour a little darker than the token, are consistent
  with the snackbar's own shadow under its fill, as J8's chip showed; that is an inference.
- **The database** after the second path (force-stop 22:46:47Z, `db-c1c-*`) and after the third and the fullscreen
  exit (22:49Z, `db-c1end-*`): integrity ok, `user_version` 16, **every digest equal to the backup's**. Neither path
  wrote anything. `map.fullscreen` read back `False` after the exit (`46-`).
- Crash buffer 0 bytes (`36-`, `47-`).

**Prediction for check 1:** mine, "all three show the map through at 0.8", **held**; the planner's "at least one renders
opaque" did not. The snackbar's first path, which I predicted would raise it, did not.

**Captures, Topographical by day (for check 5):** `22-c1-dropdown-B`, `24-c1-drawer-B`, `34-c1c-snackbar-B` (over the
nav band) and `43-c1c3-snackbar-fs-B` (over the map).

## Check 2: separate windows over the Maps map: **every one raised shows the map through, fills 0.80; the offline region's sheet could not be raised**

Relaunched at 22:49:55Z (pid 28812), then pid 31819 after a relaunch at 23:01Z (to clear the typed query). Topographical,
Night Maps off.

**2a. The details sheet from a bubble.**
- **The offline region: not raised.** A real touch on the 1 km DEVICE CHECK region's dashed outline, (330, 1195), twice,
  and on region B's outline, (355, 358), each opened **no bubble**. The app logged, each time, `W MapBubble: No
  OFFLINE_REGION 1 in the host's lists; its bubble was not shown.` (and `… 2 …` for B). So the outline tap resolves to
  the region (`ui/map/MapBubbles.kt:78`), but the bubble looks it up in `visibleOfflineRegions`
  (`AvailabilityScreen.kt:1251`; `MapBubbles.kt:266`), which does not hold it. The Records chips agree: All counts 6
  (2 finds, 3 waypoints, 1 track) and lists no region (`27-`). **Not run; the map-chrome question for this sheet is
  answered by the other two, which are the same composable.** Why the list lacks both regions I did not establish; see
  Flags.
- **The track** (22:52Z to 22:53Z): five double-taps at (540, 1200) to about zoom 17 (`54-`), a real touch on the
  track's line at (606, 1212) opened its bubble (`55-`), and "Details" at (150, 1098) the sheet (`56-c2-track-sheet-B`,
  its drag handle at y 1104). Back closed it (`57-`, the bubble gone too). Camera unchanged (`54-` against `57-`,
  slope 1.000, rms 0). "Share" was not touched.
- **The DEVICE CHECK waypoint** (22:54Z to 22:56Z): its pin lay under the find and photo glyphs, so the Layers sheet's
  Finds and Photos were switched off (`ltoggle.sh`, `58-`, `59-`, each read back unchecked) to uncover it, as `-72`
  did. A touch on the pin at (553, 1000) opened its bubble (`61-`), "Details" at (390, 886) the sheet
  (`62-c2-wpt-sheet-B`). "Directions" was not touched. Back closed it (`63-`); camera unchanged (`60-` against `63-`).
  Finds and Photos were switched on again (`64-`, `65-`, read back checked), and `map_preferences` decodes equal to the
  start's (`66-`).

  | sheet | box | a (R, G, B) | b | rms | fill 1 − a/s | composite 1 − a |
  |---|---|---|---|---|---|---|
  | scrim, both | `[50,300][850,900]` | **s** = 0.679 | 18.9, 15.0, 11.9 | 0.22 | — | — |
  | track | `[300,1290][880,1560]` | 0.138, 0.138, 0.138 | 25.9, 24.9, 23.9 | 0.26 | **0.797** | 0.862 |
  | track | `[700,1180][1035,1260]` | 0.137 | 26.2, 24.9, 24.0 | 0.27 | 0.798 | 0.863 |
  | track | `[600,1960][1035,2170]` | 0.134, 0.138, 0.135 | 26.4, 25.2, 24.3 | 0.20 | 0.797 to 0.803 | 0.862 to 0.866 |
  | waypoint | `[60,1500][1020,1545]` | 0.136, 0.138, 0.138 | 26.3, 24.9, 23.9 | 0.23 | 0.797 to 0.800 | 0.862 to 0.864 |
  | waypoint | `[700,1990][1035,2170]` | 0.134, 0.139, 0.136 | 26.4, 25.1, 24.3 | 0.22 | 0.795 to 0.803 | 0.861 to 0.866 |

  The scrim is Bark at 0.32 (s 0.679, b = 0.32 × Bark). The container solved over it is (27.7, 27.4, 27.0), `#1B1B1B`.
  **Both sheets show the map through at 0.8. Pass.** Other boxes that crossed the sheet's text gave rms 16 to 51 and
  are not cited.
- **An observation, not a pass condition:** the bottom 135 px of the window, `y` 2181 to 2316, the navigation bar's
  inset, where the dump shows only the sheet's "Close sheet" dismiss node, is **flat**: (20, 19, 18) to (21, 20, 19),
  standard deviation 0.7 per channel in `56-`, where the frame without the sheet has a spread of 21. No map shows
  through that band while the sheet is up. What draws it (the sheet window's navigation-bar background, or Android's
  contrast scrim for three-button navigation) I did not establish. See Flags.

**2b. The trip-date dialog: shows the map through, fill 0.80. Pass.** At the same zoom-17 camera: the + disc (989, 1324)
at 22:56:28Z; "Trip" (470, 1326) in its tile (`68-`; "Find" is `[577,1259][751,1394]` and was not touched); the pin's
row (`69-`); its "OK" (287, 1865), which only sets the pending location; the dialog (`70-c2b-trip-dialog-B`), "Trip
name" prefilled, today selected. **Back** dismissed it at 22:57Z (`71-`: no dialog, no pin). "Plan trip" was not
touched.

| box | a (R, G, B) | b | rms |
|---|---|---|---|
| dim alone, `[60,262][1020,322]` | **s** = 0.399 | 0.1 | 0.29 |
| dialog, `[400,560][1000,640]` (right of "Select date") | 0.074 | 34.5 | 0.16 |
| dialog, `[620,1620][880,1710]` (the empty cells after the 30th) | 0.079 | 34.1 | 0.15 |
| dialog, `[60,1720][880,1790]` (above the buttons) | 0.079, 0.079, 0.081 | 34.0 | 0.21 |

The window's dim is black at 0.60 (s 0.399, b 0). **Fill 1 − a/s = 0.797 to 0.815; composite 1 − a = 0.92.** The
container over the dim is 42.5 per channel, `#2B2B2B`, so the picker's own cleared container adds nothing, as `-83`'s
R13 says. The first box reads 0.815, higher than the other two; I did not establish why.

**2c. The waypoint-name dialog: shows the map through, fill 0.80. Pass.** + (989, 1324); "Waypoint" (910, 1326); "OK"
(287, 1865); the dialog (`74-c2c-wpt-dialog-B`, "Waypoint 2" prefilled, no keyboard). **Back** dismissed it (`75-`).
"Drop waypoint" was not touched.

| box | a (R, G, B) | b | rms |
|---|---|---|---|
| dim alone, `[50,300][850,800]` and `[50,1500][850,1850]` | **s** = 0.397 to 0.399 | 0 | 0.25-0.28 |
| dialog, `[520,905][1020,975]` (right of its title) | 0.079, 0.079, 0.077 | 22.1 | 0.17-0.21 |
| dialog, `[60,1200][480,1380]` | 0.078 | 22.3 | 0.38 |
| dialog, `[60,1335][1020,1385]` | 0.076, 0.077, 0.077 | 22.1 | 0.85 |

**Fill 0.801 to 0.809; composite 0.92.** Container 27.6, `#1B1B1B`. Its 4 dp shadow does not reach under these boxes.
Camera unchanged across 2b and 2c (`71-` against `75-`).

**2d. The species suggestions: over the panel, the popup's own fill 0.80 and a composite of about 0.97; over the bare
map, 0.805. Pass.** The bar (500, 132) opened the panel with the keyboard (`76-`); `input text chanterelle` (23:00Z)
raised the suggestions from the local index (`77-`, keyboard up); one Back lowered the keyboard and **the popup
stayed and grew** to 15 rows, down over the panel's end, the bare map below it, the caption and the nav (`78-`).
Nothing was chosen.
- Against the panel alone (`76-` against `77-`, both with the keyboard): `[760,640][1040,760]` a 0.197 to 0.201, b 26.0,
  rms 0.3; `[640,905][1040,995]` a 0.190 to 0.196, rms 0.3. **Own fill 0.80 to 0.81**, container 32.6, `#202020`.
- Against the map alone (`75-`): the same boxes a 0.025 to 0.035, **composite 0.965 to 0.975**; `[660,1110][1040,1160]`
  a 0.032 to 0.040, rms 0.4 to 1.2, composite 0.960 to 0.968. The owner's "1 A" stacking predicts 0.96.
- **Overhang on the bare map** (`78-` against `75-`), `[700,1795][1040,1845]`: a 0.195, b 26.4, rms 0.21, **composite
  0.805**, container `#202020`.
- **Closing it took an outside touch.** Three Backs (23:00:30Z to 23:00:38Z) left the popup and the panel open, with nothing visibly
  changed (`79-`). A touch at (60, 1830), left of the popup and below the panel, on the panel's dismiss scrim and above
  the caption, closed both (`80-`). The query stayed in the bar, so the app was force-stopped and relaunched.

**2e. The Month menu: own fill 0.80 over the panel; 0.800 over the bare map. Pass.** Relaunched (pid 31819); the map
alone (`82-`); the bar, one Back (keyboard down), the panel alone (`83-`); a touch on the Month field (540, 876)
opened the twelve months over the panel and past it onto the map and the nav (`84-c2e-month-B`). One Back closed the
menu (`85-`, equal to `83-` in the menu's box), still "September"; one more closed the panel (`86-`, camera equal to
`82-`).
- Against the panel (`83-`): `[300,1040][880,1110]`, `[300,1480][880,1540]` a 0.198 to 0.200, b 26.0, rms 0.3: **own
  fill 0.80**, `#202020`.
- Over the bare map, `[300,1800][880,1890]` (`82-` and `83-` agree there): **a 0.200, composite 0.800.**
- Over the panel against the map alone, a 0.04 to 0.06 with rms 5 to 6: the panel's own text beneath breaks the fit,
  so I cite only that it is near the stacked 0.96.

The database after check 2 (force-stop 23:02Z, `db-c2-*`): integrity ok, `user_version` 16, **every digest equal to the
backup's**; `map_preferences` decodes equal to the start's (`89-`). Crash buffer 0 bytes, 0 `FATAL EXCEPTION` (`87-`).

**Prediction:** held for every surface raised: fills 0.797 to 0.815, the stacked popups about 0.96 to 0.97. Not
predicted: the region's bubble not opening, the sheet's opaque navigation-bar band, and the suggestions ignoring Back.

**Captures, Topographical by day, for check 5:** `56-` (track sheet), `62-` (waypoint sheet), `70-` (trip dialog),
`74-` (waypoint dialog), `77-` and `78-` (suggestions), `84-` (Month).

## Check 3: in-tree surfaces: **all measured at 0.80; the search notice's first line sits under the compass strip; both chips not run**

Relaunched at 23:04:05Z (pid 32618), the opening camera, Topographical, Night Maps off.

**3c. The bottom nav and 3e. the compass strip: 0.80 each. Pass.** `90-c3-normal-B`, then the cluster's Fullscreen at
(989, 740): `91-c3-fullscreen-A`. In fullscreen the nav and the search bar are gone and the strip has moved up to
`y` 78 to 124, so the strip's normal band (`[0,204][1080,255]`) and the nav's (`[0,1956][1080,2316]`) are bare map there,
except the caption, which drops to the window's foot. The map did not move (`[50,400][850,900]`, slope 1.000, rms 0).

| surface | box | a (R, G, B) | b | rms | composite |
|---|---|---|---|---|---|
| strip | `[920,208][1075,252]` (right of the readout) | 0.200 | 46.9, 37.0, 29.0 | 0.29 | **0.800** |
| strip | `[125,208][222,252]` (between the arrow and the heading) | 0.201 | 46.8, 36.8, 28.8 | 0.27 | 0.799 |
| nav | `[0,1962][420,1995]`, `[660,1962][1080,1995]` (above the icons) | 0.200 to 0.201 | 25.8 to 26.0 | 0.27-0.29 | **0.80** |
| nav | `[0,2145][1080,2178]` (below the labels) | 0.199 to 0.200 | 25.9 to 26.1 | 0.29 | 0.80 |
| nav, in the system bar's band | `[270,2190][470,2245]` | 0.200 to 0.201 | 25.9 to 26.0 | 0.27 | 0.80 |
| search bar (not asked; in the same pair) | `[860,135][1050,185]` | 0.199 to 0.200 | 47.0, 37.0, 29.1 | 0.3 | 0.80 |

Containers: the strip and the bar 0.8 × Bark exactly; the nav 32.4, `#202020` (`surfaceContainer`). The exit from
fullscreen (23:04:54Z, `92-`) restored the same frame (`90-` against `92-` over the nav band and the map, slope 1.000,
rms 0). The pre-registered fullscreen reference was usable for the strip because nothing covers its band there.

**3a. The centre-pin OK/Cancel row: 0.80 inside, 0.81 to 0.84 at its edges. Pass.** + (989, 1324) at 23:05:08.7Z,
"Trip" (470, 1326), the pin and its row (`94-c3a-pinrow-B`, row drawn `y` 1775 to 1956, buttons `[45,1798][528,1933]`
OK and `[551,1798][1035,1933]` Cancel). **Cancel** by a real touch at its centre (793, 1865) at 23:05:25.5Z: the pin
and row gone (`95-c3a-A`), the camera unchanged.

| box | a (R, G, B) | b | rms |
|---|---|---|---|
| inside the outlined Cancel button, `[600,1830][700,1850]` | 0.200, 0.201, 0.201 | 22.0 | 0.22 |
| below the buttons, `[60,1936][1020,1954]` | 0.200 | 22.0 | 0.28 |
| above the buttons, `[60,1778][1020,1796]` (the row's top edge) | 0.188, 0.189, 0.189 | 22.2 | 1.0 |
| the row's ends, `[0,1800][42,1930]`, `[1040,1800][1078,1930]` | 0.162 to 0.187 | 22 to 26 | 2.3-3.0 |

**Fill 0.80** inside (container 27.5, `#1B1B1B`). Near its edges the composite rises to 0.81 to 0.84 with a larger
rms, which fits its 4 dp shadow reaching under the fill there; that is an inference.

**3b. The search notice: 0.80. Pass on the measure, with a legibility finding.** At the opening camera (`96-`, the map
alone): the bar, one Back for the keyboard, the panel (`97-c3b-panel`): **both coordinate fields read empty in the dump**
(`text=""` on `[45,1381][528,1562]` and `[551,1381][1035,1562]`). "Search this location" by a real touch at (540, 1675)
at 23:06:10.3Z: the panel closed and the notice showed, "Enter a valid latitude (-90 to 90) and longitude (-180 to
180).", `[0,203][1080,340]` (`98-c3b-notice-B`). No search ran.
- `[300,290][1060,336]` and `[140,290][1060,300]` (below its first line, clear of "180)."): a 0.200, b (86.0, 27.0,
  27.0), rms 0.28. **Composite 0.80**, container (107.5, 33.8, 33.8), `#6B2222` (`errorContainer`).
- **The notice starts at the search bar's foot (`y` 203) and the compass strip, `[0,204][1080,255]`, is drawn over it.**
  Its first line of text lies under the strip's own readout (crop `99-c3b-notice-crop.png`). Where the two overlap, the
  notice passes 0.197 to 0.200 of the strip-over-map ground (`[960,208][1075,252]`, rms 0.3), so the map there is
  under two 0.8 fills, about 0.96 (inferred by multiplication; there is no frame of that band without the strip). **By
  my reading of `98-` and `99-`, the first line is hard to read.** The owner's to judge; see Flags.
- Cleared by `am force-stop` and `am start` at 23:06:52.8Z (`102-`: no notice).

**3f. The taxon chip: not run**, as pre-registered: it needs a completed species search, which fetches from the network
and writes `cached_searches`.

**3g. J8's chip: not run**, as pre-registered: showing an entry writes `cartography_entries.shownOnMap`, and this
dispatch forbids any data edit. `-72`'s 0.833 to 0.840 stands unmeasured here. **A question for the planner** (hand-back).

The database after check 3 (`db-c3-*`): integrity ok, `user_version` 16, every digest equal to the backup's;
`map_preferences` decodes equal to the start's (`101-`, so `map.fullscreen` is back to false). Crash buffer 0 bytes
(`100-`).

**Prediction:** the nav, strip, row and notice at 0.80: held; the row's shadow shows only at its edges. Not predicted:
the notice under the strip.

**Captures, Topographical by day:** `90-` (nav, strip and bar), `94-` (centre-pin row), `98-` and `99-` (notice).

## Check 4: the Journal: **every surface as built: 0.8 where the code says map, solid where it says none; the Offline maps sheet in portrait covers no map**

Same process (pid 32618 after the 23:06:52.8Z relaunch). Journal, Records.

**4a. The Records sheet from Offline maps: fill 0.80, but over the list, not the picker map.** The Records chip row was
scrolled sideways by a drag on it and the "Offline maps" chip touched at (837, 463) (`104-`, `105-`). **The chip read
"Offline maps 2"**, where the All count before had held no region (see 2a). The panel shows the picker map
(`[0,815][1080,1625]`), then the pin row, name, radius, "Download Maps", and the downloaded rows. I scrolled the panel
by two drags that started on plain text ("No location picked yet …"), never on the map, the slider, "Download Maps" or a
row (`106-`, `107-`). With the "DEVICE CHECK" row in view (`[45,1533][1035,1907]`), the picker map was scrolled off
the top. The rows lie about 900 px below the map's foot, and the sheet rises only to `y` about 1100, so **in portrait
the sheet cannot lie over the picker map** (from the bounds). A touch on the row's title at (180, 1561) opened
the sheet (`108-c4a-sheet-B`); Back closed it (`109-`, the list unchanged).
- Scrim `[50,600][1030,1000]`: s 0.681 to 0.682, b (18.6, 14.6, 11.7). Sheet `[520,1440][1040,1520]` and
  `[560,1610][1040,1680]`: a 0.135 to 0.137, rms 0.06 to 0.12 over the list's own text: **fill 0.799 to 0.802**,
  composite 0.863 to 0.865. (`[620,1262][1040,1330]` has a flat ground, spread 9, and reads 0.122; not cited.)
- **Verdict: the fill is 0.8 as built (`RecordsTab.kt:330`), and it shows the list through, not the map.** By my reading
  of `108-`, the list's text behind the sheet's text makes both hard to read. The owner's to judge; see Flags. The
  landscape layout, where the report's S1 infers the sheet does cover the picker map, is not in this dispatch's
  rotation list and was not run.

**4b. From All: opaque. Pass.** The chips scrolled back, "All" (165, 463) (`111-`: All now lists region "B" first,
confirming the regions had loaded). A touch on region B's title at (400, 695) opened the sheet (`112-c4b-sheet-B`); Back
closed it (`113-`). Scrim s 0.680 to 0.681; in the sheet, `[520,1440][1040,1520]` and `[600,1700][1040,1760]`, over list
text with a spread of 42 to 55: **a 0.000, every pixel (27, 27, 27)**, `#1B1B1B`.

**4c. The entry with a map** (`6107d76c…`, the L0a DEVICE CHECK entry; Entries, a touch at (300, 1200) at
23:12:32Z; its report with the preview map `[0,437][1080,1247]`, `115-` and `116-` equal).
- **Overflow menu: 0.80 over the map. Pass.** "Entry options" (967, 346) opened "Edit entry", "Show on map", "Delete
  entry" at `[611,415][1036,865]`, over the map's top-right corner (`117-c4c-menu-B`). `[640,545][1020,600]`: a 0.201,
  b 25.9, rms 0.3, **composite 0.80**, container `#202020`; `[640,680][1020,738]` a 0.195 to 0.197 (flat-ish ground,
  spread 9 to 15). Back closed it (`118-`, equal to `116-`). "Show on map" and "Edit entry" were not touched.
- **Delete dialog: 0.80 over the map. Pass.** The menu again, "Delete entry" at (860, 790) at 23:13:36.9Z; "Delete this
  entry?" (`120-c4c-delete-dialog-B`), its top half over the map. **Back** at 23:14:12.9Z dismissed it (`121-`, equal to
  `118-`); the entry is still there (`122-`). Dim over the map s 0.402; in the dialog over the map `[720,880][960,960]`
  a 0.082, rms 0.1, and `[160,815][980,870]` a 0.081 to 0.082: **fill 0.796**, composite 0.92, container 42.4,
  `#2B2B2B`. Over the report below the map, `[480,1380][940,1430]`, a 0.078: the same fill.

**4d. The entry without a map** (`6380d39b…`, "Nothing kept"; its report has no map, and the dump no `SurfaceView`).
- **Overflow menu: opaque. Pass.** Over the report's explanatory text (`[45,437][1035,722]`, ground spread 54 to 64),
  `[640,545][1020,600]` and `[640,440][1020,470]`: **a 0.000, every pixel (32, 32, 32)**, `#202020` (`124-`).
- **Delete dialog: opaque. Pass, read from flat ground.** The dialog (`126-`) lies below the text, over the report's
  plain background, so no slope can be read. Instead: the ground there is (27, 27, 27) without the dialog and (11, 11,
  11) under its dim; a 0.8 fill would show (36.6); **the dialog's pixels are all (43, 43, 43)**, `#2B2B2B` solid.
  Back dismissed it (`127-`).

## Check 2a, resumed after check 4: the offline region's details sheet: **fill 0.80. Pass.**

Back on the Maps tab at 23:16:04Z (the same process, after the Offline maps panel had reloaded the region list): the
same touch on the 1 km DEVICE CHECK region's outline at (330, 1195) now opened its bubble ("DEVICE CHECK", "Details";
`129-`), and "Details" (150, 1081) the sheet (`130-c2a-region-sheet-B`). Back closed it (`131-`, camera equal to
`128-`).
- Scrim `[50,300][850,900]` s 0.679; sheet `[620,1262][1040,1330]`, `[520,1440][1040,1520]`, `[560,1610][1040,1680]`:
  a 0.136 to 0.139, rms 0.3: **fill 0.795 to 0.800**, composite 0.862, container `#1B1B1B`.
- So the earlier failure was the region list, not the sheet: at this launch the list the bubble reads was empty until
  the Offline maps panel reloaded it (`AvailabilityViewModel.kt:897-905`, `:921-935`). Why the start-up load
  (`:167`) left it empty I did not establish. See Flags.

The database after check 4 (`db-c4-*`): integrity ok, `user_version` 16, **every digest equal to the backup's**;
`map_preferences` decodes equal to the start's (`133-`), so opening the Offline maps panel stored nothing. Crash buffer
0 bytes, 0 `FATAL EXCEPTION` (`132-`).

**Prediction:** held, except that 4a's sheet covers the list, not the map, in portrait, which the pre-registration
allowed for.

**Captures:** `108-` (Records sheet from Offline maps), `112-` (from All), `117-` and `120-` (entry with a map), `124-`
and `126-` (entry without a map), `130-` (region sheet over the Maps map).

## Check 5: legibility captures, for the owner: **82 captures across the six combinations; the look is the owner's**

"By day" is Night Maps off and "at night" Night Maps on (the app's own map setting, Tools, Settings, "Night Maps"), as
`-72` read the same words; the app theme stayed dark throughout (`uimode night` yes, the app's Night Mode on "System
Default"). The basemap is the Layers sheet's map type. **The basemap is not persisted**: a relaunch always opened on
Topographical (tested at 23:27Z: Street set, force-stop, relaunch, "Layers: Topographical map"). Night Maps is persisted
(`night_mode.maps`).

**Method.** One script, `pass.sh`, run once per combination on a fresh process at the Maps tab's opening camera, touching
only the coordinates checks 1 to 4 had used. Before every touch that sits near a data-changing control (the + tile's
"Find", the pin's "OK", "Delete entry", "Search this location", the draft tile) it reads a fresh dump and stops if the
expected text is not at the expected bounds. After every capture it checks that the focused window belongs to Forager
and that the phone is unlocked. Dialogs and sheets are closed with Back; the popups with Back or the outside touch of
check 2d. `setmap.sh` and `setnight.sh` change the map type and Night Maps through the app's own UI, each read back.
- The first night run stopped itself at 23:22:17.9Z on its focus check, because the entry menu's popup is a separate
  window titled "Pop-Up Window" (owner Forager). The check was corrected to read the focused window's owning package; I
  closed the menu with Back, relaunched, and ran the combination again from the start. That is the only stop in 286
  logged steps. No system or Google prompt appeared.

**The captures** (`c5-<basemap>-<day|night>-NN-<surface>.png`; hashes in `c5-captures-sha256.txt`; contact sheets
`c5-sheet-*.png`). Each combination has the same 13 surfaces, plus the Layers sheet itself where the basemap was changed
through it:

| NN | surface | NN | surface |
|---|---|---|---|
| 00 | Layers sheet (Street and Satellite only) | 07 | trip-date dialog |
| 01 | Maps tab: bottom nav, compass strip, search bar | 08 | waypoint-name dialog |
| 02 | search dropdown | 09 | species suggestions (keyboard down, overhanging the map) |
| 03 | Month menu | 10 | search notice |
| 04 | Tools drawer | 11 | compact snackbar ("Saved to Drafts", over the nav band) |
| 05 | offline region's details sheet | 12 | entry overflow menu over the entry map |
| 06 | centre-pin OK/Cancel row | 13 | entry delete dialog over the entry map |

| | by day | at night |
|---|---|---|
| Topographical | `c5-topo-day-01` to `-13` | `c5-topo-night-01` to `-13` |
| Street | `c5-street-day-00` to `-13` | `c5-street-night-00` to `-13` |
| Satellite | `c5-satellite-day-00` to `-13` | `c5-satellite-night-00` to `-13` |

The Topographical-by-day frames of checks 1 to 4 (`22-`, `24-`, `34-`/`43-`, `56-`, `62-`, `70-`, `74-`, `77-`/`78-`, `84-`,
`90-`, `94-`, `98-`/`99-`, `108-`, `112-`, `117-`, `120-`, `130-`) are the measured ones; the `c5-topo-day` set repeats
them at one camera for comparison with the other five.

**What I noticed, for the owner to weigh, by my reading of the frames only (no rulings):**
- **The entry map follows neither the basemap nor Night Maps.** In all six combinations the entry report's preview map
  (`-12`, `-13`) is the same Topographical-by-day rendering, so those two surfaces were only ever seen over that one map.
- **The search notice's first line** sits under the compass strip in all six (`-10`), as in check 3b.
- **The Offline maps sheet** lies over the list, not a map, in portrait (check 4a), so its legibility does not change
  with the basemap and it is not in the per-combination set; `108-` is its capture.
- Over the stacked panels the popups read nearly solid, as the owner's "1 A" expects; where they overhang the map
  (`-09`, the last rows), the map shows through.
- Satellite by day and Topographical at night are the darkest grounds; the Bark and `#202020` fills over them read as
  near-solid dark panels in the thumbnails. Whether that is enough separation is the owner's call.

**Restored and read back after check 5:** a force-stop; `map_preferences` decodes equal to the start's (`151-`, so
Night Maps is off again); the next launch opens on Topographical. The database (`db-c5-*`): integrity ok,
`user_version` 16, **every digest equal to the backup's**, after the snackbar path's six more runs. Crash buffer 0
bytes, 0 `FATAL EXCEPTION` (`150-`).
