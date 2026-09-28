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
editor (`30-`), and the Maps item at 22:44:0xZ brought up the Maps tab with **no snackbar** (`31-c1c-snackbar-B`, a
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
