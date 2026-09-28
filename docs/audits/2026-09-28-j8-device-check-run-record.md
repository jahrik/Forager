# J8 device check (Journal entries on the main map), on the S22 Ultra: run record

**Status: pre-registration only.** The app has not been launched on this build and no app screen has been looked at.
This file is committed and pushed before the first launch, which is what runs the migration, so the order of
prediction and observation can be checked. Results are added in later commits, and this section is kept unchanged.

**Date:** 2026-09-28, from 20:56Z.
**Dispatch:** `prompts/preserved/2026-09-28-72.md` (sha256 `1afe2f34…ccd88252`), committed at `8dbfd14`; its intent is
`2026-09-28-72` in `RECORD.md`, the planner's. The kit is absent at this base (no `.claude/`, no checkers; the owner
removed it in `e136330`), and the dispatch says the planner writes the record, so I write no record entry. The standing
rules, data rules and abort conditions of `2026-09-28-51.md` apply, and through it the phone, evidence, privacy and
settings rules of `-04` and `-12`.
**Base:** `git fetch` at 20:56Z: `origin/journal-redesign` at `d7cc9f5`, one planner commit past the dispatch's
`8dbfd14` (intent `-74`, the tablet check, and a plan line; `RECORD.md`, `docs/plans/` and one store copy only).
Branch `device-j8`, worktree `/home/zynergy-labs/Zynergy/forager-wt/device-j8`, cut from `d7cc9f5`, upstream unset so
nothing can push to `journal-redesign`.
**The APK is built at `99de6c2`,** J8's terminal, as the dispatch says. `git diff --stat 99de6c2 d7cc9f5` over `app/`,
`build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradle.properties`, `gradlew`, `data/` and `server/` is empty,
so the code is the same at the branch head. **Code citations** are at `99de6c2` (identical under `app/` at `d7cc9f5`).
Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-j8-check/`, outside the repository. No screenshot,
dump, coordinate, note text, place name or photo goes into this file.
**Device:** SM-S908U, serial `R5CT321008R`, named with `-s` on every call. `adb devices -l` also lists a second device,
`R52T506412L` (SM-X800, the tablet, for `-74`); it is never addressed. `ro.build.id=BP2A.250605.031.A3`. Unlocked
(`isKeyguardShowing=false`), awake. `wm size` 1080 x 2316, `wm density` 450, so 1 dp = 2.8125 px.

## The dispatch, verbatim

From `prompts/preserved/2026-09-28-72.md` at `8dbfd14`, below its "verbatim prompt follows" line:

> **Type:** device
>
> # Role
>
> You are the coder for **J8's device check on the S22 Ultra** (`R5CT321008R`; touch no other device). You observe and record. You change no app code. This dispatch's intent is `2026-09-28-72`. The planner writes the record.
>
> Read first:
> - `CLAUDE.md`;
> - `prompts/preserved/2026-09-28-51.md`. Its standing rules, data rules and abort conditions apply in full unless this dispatch changes them.
> - the J8 completion report, `docs/audits/2026-09-28-j8-entries-on-map-completion-report.md`, its device-only list above all;
> - the Part 1 run record, `docs/audits/2026-09-28-stage-device-check-part-1-run-record.md`, for how this phone behaves and for the helper scripts in `device-evidence/2026-09-28-stage-check-1/`.
>
> # Standing rules, restated
>
> - Never run `adb logcat -c`; read with `-d` only.
> - Launch with `am start`, never `monkey`.
> - **If any system or Google prompt appears over the app, stop at once.** Do not tap it, dismiss it or capture it.
> - Evidence stays outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-j8-check/`.
>
> # The database: backup first, because this install migrates the owner's real data
>
> The phone runs a debug build at database version 15, and it holds the owner's own records. J8 brings `MIGRATION_15_16`. **Debug builds chain `fallbackToDestructiveMigration(true)`** (`data/local/ForagerDatabase.kt:202-205`), so a debug build that finds no migration path wipes the database rather than failing. The path is registered, so a failure should throw rather than wipe. The owner's records are still not to be bet on that reading.
>
> 1. **Before installing:**
>    - `am force-stop` the app.
>    - Copy `forager.db` and any `-wal`/`-shm` files out through `run-as` (`exec-out run-as com.zynergylabs.forager.app cat databases/...`) into the evidence directory.
>    - **Verify the copy on this machine:** sha256; the SQLite header; `PRAGMA integrity_check` returning `ok`; `PRAGMA user_version` = 15; and row counts for every table. Record them all.
>    - **If any of that fails, stop.**
> 2. **Build** a debug APK from `origin/journal-redesign` at `99de6c2` (J8's terminal; `app/` is identical at `7b4521f`) in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-j8`, with `./gradlew --offline assembleDebug` and `LC_ALL=C.UTF-8`. Check `df` and free memory first. Another coder may be running Gradle: if under 2.5 GB is available, wait. Record sha256, versionName, versionCode and the certificate digest.
> 3. **Install with `install -r` only.** Never uninstall, clear data or use `-d`. Confirm `firstInstallTime` is unchanged.
> 4. **Launch,** then read crashes (`logcat -d -b crash` and `FATAL EXCEPTION`). **After the migration:**
>    - force-stop and copy the database out again;
>    - confirm `user_version` = 16, `integrity_check` = `ok`, and `cartography_entries` has a `shownOnMap` column that is false on every row;
>    - confirm **every table's row count equals the backup's**.
>
>    **A count mismatch, a crash, or a version other than 16 is an abort. Report it at once, and restore nothing without the owner.**
>
> # Data rule for this check
>
> - Toggle `shownOnMap` **only on entries labelled "DEVICE CHECK"**. Never on the owner's own entries.
> - Every entry you show, you hide again before you finish, and you read it back.
> - Otherwise `-51`'s rules stand: create, edit and delete nothing else, and restore every setting you change.
>
> # The checks, cheapest first
>
> For each check, write the pass condition from the J8 report and the code (file:line) and your prediction before you look. Push those first, then the verdict and the evidence.
>
> 1. **Migration on real data:** as in 4 above.
> 2. **Report menu:** a saved DEVICE CHECK entry's report menu offers "Show on map", placed between Edit and Delete; a draft's does not. Showing it makes the Maps-tab chip appear, reading "1 journal entry on map" in the singular.
> 3. **The highlight:** the entry's kept records are highlighted in place, beneath their own glyphs and lines. Capture Topographical, Street and night, with the other basemaps if present. **The owner judges the colours**: day `#005577`, night `#00DDFF`. Report and capture; do not rule on the look.
> 4. **The chip against real insets:** portrait, 90 and 270. It sits under the compass strip (portrait) or under the search bar (landscape), clear of the system bars, at 0.8 (measure the fill as Part 1 did). Its list shows the entry's date, "Hide" and "Hide all".
> 5. **The landscape chip against the icon cluster.** The owner ruled that this overlap is for this check to judge: "1 A". At 90 and 270, measure the chip's and the cluster's bounds, and whether a real touch where they overlap reaches the chip or the cluster. **Report and capture; do not rule.**
> 6. **Bubble lines:** a highlighted record's bubble shows the entry's date line, "Open entry <date>" to TalkBack. Tapping it opens that entry's report in the Journal. With an entry open in its editor and unsaved, you would get the unsaved-changes prompt; **do not create unsaved edits on the owner's entries**. Test the prompt only on a DEVICE CHECK entry, and Discard it.
> 7. **The Layers "Journal entries" switch** hides all highlights while the chip stays. It persists across `am force-stop` and relaunch. Restore it.
> 8. **The failed-write Toast** ("Changes not applied. Try again."): **not run.** It cannot be forced on the device without changing app code. Record it as not run.
>
> # Report
>
> Write `docs/audits/2026-09-28-j8-device-check-run-record.md` on branch `device-j8`, in the Part 1 record's format. Commit and push after each check. The planner merges it.
>
> Record:
> - the backup's and the migration's figures;
> - the install facts;
> - every verdict;
> - the owner-judged captures, named;
> - the settings and `shownOnMap` values restored and read back;
> - the crash reads;
> - **Decisions I made**;
> - **Flags outside scope**.
>
> # Abort conditions
>
> - a backup that cannot be made or verified;
> - an install needing uninstall, `-d` or a data clear;
> - a signature mismatch;
> - a migration crash, a version other than 16, or any row-count mismatch;
> - a new Forager crash;
> - any prompt over the app;
> - a locked phone;
> - a pass condition you cannot state from the code;
> - any need to touch the owner's own entries.
>
> # Merge
>
> Not authorised.

## The launch message, verbatim

The planner's message that launched me. The store copy above governs over it.

> This is planner dispatch `2026-09-28-72`, **J8's device check on the S22 Ultra** (`R5CT321008R`). Its store copy is committed at `prompts/preserved/2026-09-28-72.md` on `origin/journal-redesign` at `8dbfd14`. Fetch it, read it in full and quote it verbatim in your run record; the file governs over this message.
>
> **The one thing to hold above everything else:** this install migrates the owner's real database. Debug builds chain `fallbackToDestructiveMigration(true)` (`data/local/ForagerDatabase.kt:202-205`).
> - Before installing, force-stop the app, copy `forager.db` and any `-wal`/`-shm` out through `run-as`, and verify the copy on this machine: sha256, the SQLite header, `PRAGMA integrity_check` = ok, `user_version` = 15, and every table's row count.
> - **If any of that fails, stop.**
> - After the migration: `user_version` 16, integrity ok, `shownOnMap` false on every row, and every row count equal to the backup's. Otherwise abort, report at once, and restore nothing without the owner.
>
> Toggle `shownOnMap` **only on entries labelled "DEVICE CHECK"**, never on the owner's own entries. Hide every one again, and read them back.
>
> The standing rules of `-51` apply in full:
> - no `logcat -c`;
> - `am start`, not `monkey`;
> - **stop at once at any system or Google prompt over the app**;
> - evidence outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-j8-check/`.
>
> **Build and push:**
> - Build the APK from `99de6c2` in a new worktree, `/home/zynergy-labs/Zynergy/forager-wt/device-j8`, on branch `device-j8` from `origin/journal-redesign`.
> - Another coder (the save-failure build) runs Gradle on this machine. Check free memory before `assembleDebug`, and wait if under 2.5 GB is available.
> - Push the run record after each check.
> - The planner merges it and writes the record. You do not touch `RECORD.md`. Merge is not authorised.
>
> When you finish or stop, hand back a report: the backup and migration figures, install facts, every verdict, the owner-judged captures by name, what was restored and read back, crash reads, decisions you made, and flags.

## Before the install (no app screen looked at)

### The phone at the start (20:57Z)

- **Installed:** `versionName=1.0.1356+g26709b1c`, `versionCode=1356`, `lastUpdateTime=2026-09-28 10:17:38` (phone time,
  PDT), user 0's `firstInstallTime=2026-09-22 11:15:05`, `ceDataInode=2495587`, `deDataInode=2494123`
  (`01-dumpsys-package-start.txt`). That is Part 1's build, as the dispatch says. Forager was running as pid 9626
  (Part 1's process) with `MainActivity` in focus.
- **Crash buffer:** `logcat -d -b crash`, **0 bytes** (`02-crash-start.txt`).
- **Settings** (`03-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
  `navigation_mode=0` (three-button), `location_mode=3`, `cmd uimode night`: yes. Screen timeout 600000 ms and
  `stay_on_while_plugged_in=15`.
- **App settings** (`05-map_prefs-start.pb`, `06-diag-prefs-start.pb`, decoded by `pbprefs.py`, coordinates redacted):
  `map.fullscreen=false`, `night_mode.maps=false` (Night Maps off), the offline picker's last pick, the seven overlay
  keys and both colour fields' keys `true`, `forecast-chanterelles` opacity 1.0, `map.layer_order` the registry's
  order, `diagnostics.synthetic_forecast=false`. There is no `journal-entry-*` key.
- `databases/` holds `forager.db`, `forager.db-wal` and `forager.db-shm` only (`04-app-files-start.txt`).

### The database backup (20:57Z)

- `am force-stop com.zynergylabs.forager.app` at 20:57:42Z; `pidof` then empty (`07-force-stop-time.txt`).
- `dbcopy.sh db-backup`: the device's own `sha256sum` of each file through `run-as`, then a byte copy of each through
  `exec-out run-as … cat` into `db-backup-raw/`, then the local sha256. **They match**:

  | file | bytes | sha256 (device = local) |
  |---|---|---|
  | `forager.db` | 229376 | `d8c365df9396a7142c8a1a1c0367e7ee4824cf905b0adc800e84751148032f16` |
  | `forager.db-wal` | 469712 | `4151cbe6810fea12ae3f379787077102e3f555a84e893aed0594b70e57860174` |
  | `forager.db-shm` | 32768 | `95544d10b0b47e1c117847ba0dfff85787ecce20cddb8bcbfaec9a006e80b0da` |

  `db-backup-raw/` is never opened by SQLite and is now read-only (`chmod a-w`). Opening a WAL database replays and can
  checkpoint the WAL, so every query runs on a second copy, `db-backup-query/`.
- **Verified on this machine** (`dbverify.sh`, `db-backup-verify.txt`), SQLite 3.50.6:
  - header: `53514c69746520666f726d6174203300` ("SQLite format 3\0"), page size 4096, write/read version 2/2 (WAL);
    the WAL's magic `377f0682`, a valid WAL header;
  - `PRAGMA integrity_check`: **ok**;
  - `PRAGMA user_version`: **15**;
  - row counts for every table in `sqlite_master`:

  | table | rows | table | rows |
  |---|---|---|---|
  | `android_metadata` | 1 | `log_entry_photos` | 1 |
  | `cached_searches` | 2 | `log_photos` | 3 |
  | `cartography_entries` | 7 | `mushroom_log_entries` | 3 |
  | `cartography_entry_find_refs` | 2 | `offline_regions` | 2 |
  | `cartography_entry_offline_region_refs` | 1 | `planned_trips` | 0 |
  | `cartography_entry_photo_refs` | 1 | `room_master_table` | 1 |
  | `cartography_entry_track_refs` | 1 | `sqlite_sequence` | 1 |
  | `cartography_entry_waypoint_refs` | 3 | `track_points` | 23 |
  | | | `tracks` | 1 |
  | | | `waypoints` | 3 |

  18 tables. Beyond the dispatch, `dbdigest.py` records a sha256 over each table's rows (sorted, every column but
  `cartography_entries.shownOnMap`), printing no content (`db-backup-digest.txt`), so a later copy can be compared
  row for row and not only by count.
- **Nothing failed, so no stop.**
- **Re-checked just before the install** (21:03:01Z): the app still not running, and the device's sha256 of all three
  files still equal to the backup's (`12-pre-install-device-sha256.txt`). The backup is the database the install met.

### Data inventory (read from `db-backup-query/`, counts and labels only)

Entries 2 saved and 5 drafts; finds 3 (`mushroom_log_entries`); photos 3; tracks 1 (23 points); waypoints 3; offline
regions 2; planned trips 0; cached searches 2 (`08-entries-inventory.txt`). By label (`text LIKE '%DEVICE CHECK%'`):
- **`6107d76c…`, saved, dated 2026-09-27, labelled** (the L0a entry, "DEVICE CHECK 2026-09-28 (L0a)" in the L0a run
  record). It keeps, every ref `kept=1`, 2 finds, 1 track, 3 waypoints and 1 offline region, and has 1 photo attached.
  **This is the only saved DEVICE CHECK entry, and the only entry whose `shownOnMap` I will change.**
- `6380d39b…`, saved, dated 2026-09-28, blank text, **not labelled** (Part A's item 24 entry, made with blank text on the
  planner's ruling). Not touched.
- Five drafts dated 2026-09-28, each labelled `DEVICE CHECK 2026-09-28` (Part A's item 14). Used only for check 2's draft
  half: opened, its menu read, left with Back, nothing typed.

### Build

- Memory before Gradle: 3367 MB available (`free -m`), above the 2.5 GB floor; a Gradle daemon and a Kotlin daemon from
  other sessions were resident. `/` had 9.6 GB free.
- `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug` in the worktree, detached at `99de6c2` with a clean tree:
  **BUILD SUCCESSFUL in 1m 1s**, exit 0, **0** `e: ` lines (`09-build.log`). The worktree then went back to
  `device-j8`, clean.
- **APK** (`j8-debug.apk`, a copy of `app/build/outputs/apk/debug/app-debug.apk`; `10-apk-facts.txt`): sha256
  `629d32256e1d4352b9159d1166ada66687e94f0985e92813c13fada2dd9807a3`; `versionName=1.0.1389+g99de6c24`,
  `versionCode=1389` (`aapt2 dump badging`), no `.dirty`; signer `CN=Android Debug, O=Android, C=US`, certificate
  SHA-256 `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`.
- **The installed APK**, pulled read-only (`installed-before.apk`, `11-installed-before-cert.txt`): sha256
  `9bdc0f9c…a56f7d735`, which is Part 1's `stage1-debug.apk` byte for byte, `versionCode=1356`, certificate SHA-256
  `cb2f6da5…94f1626`. **Same certificate: no signature mismatch.** 1389 is above 1356, so `-r` needs no `-d`.

### Install

- `adb -s R5CT321008R install -r j8-debug.apk` at 21:03:08Z: `Success` (`13-install.txt`). No uninstall, no `-d`, no data
  clear.
- **After** (`14-dumpsys-package-after.txt`): `versionName=1.0.1389+g99de6c24`, `versionCode=1389`, `lastUpdateTime`
  14:03:09 PDT, user 0's `firstInstallTime=2026-09-22 11:15:05`, **unchanged**, with the same `ceDataInode`/`deDataInode`.
  User 95 is listed as before.
- The app was not running after the install; the launcher had focus.
- The device's sha256 of all three database files after the install still equals the backup's
  (`15-post-install-device-sha256.txt`): the install did not open the database. The migration runs at the first launch.

# Pre-registration: pass conditions and predictions, written before the first launch

Every touch is a real `adb shell input` touch at coordinates from a fresh dump or screenshot. The app is launched only
with `am start -n com.zynergylabs.forager.app/.MainActivity`. If any system or Google prompt appears over the app, I stop
without touching it. Verdicts that rest on my reading of an image say so. Rotations are set with
`settings put system user_rotation` (1 = 90, 3 = 270) with `accelerometer_rotation` 0, as Part 1 did.

## Check 1: the migration on real data

Code: `ForagerDatabase.version = 16` (`data/local/ForagerDatabase.kt:167`); `MIGRATION_15_16` registered
(`:196-200`, the list at `:199`); `fallbackToDestructiveMigration(true)` only when `isDebug` (`:203-205`).
`MIGRATION_15_16` (`data/local/Migrations.kt:952-978`) creates `cartography_entries_new` with `shownOnMap INTEGER NOT
NULL`, copies `id, date, text, tags, isDraft, updatedAtEpochMillis` and writes `0` for `shownOnMap` from every old row,
drops the old table, renames the new one, and recreates `index_cartography_entries_date` and
`index_cartography_entries_isDraft`. No `AUTOINCREMENT`, so `sqlite_sequence` is not touched.
- **Method:** `am start`, then wait until the Maps tab has drawn records (the map records come from Room, so the
  database has been opened), or 30 s. Crash reads: `logcat -d -b crash` and `FATAL EXCEPTION` in `logcat -d`. Then
  `am force-stop`, and `dbcopy.sh db-migrated` and `dbverify.sh db-migrated`, exactly as for the backup.
- **Pass:**
  - the copy's sha256 equals the device's for every file;
  - the header is "SQLite format 3\0";
  - `PRAGMA integrity_check` = `ok`;
  - `PRAGMA user_version` = **16**;
  - `PRAGMA table_info(cartography_entries)` lists `shownOnMap`, `INTEGER`, not null, and
    `SELECT COUNT(*) FROM cartography_entries WHERE shownOnMap <> 0` = **0** of 7 rows;
  - the same 18 tables, each with **exactly the backup's row count** (table above);
  - no crash line and no Forager `FATAL EXCEPTION`.
- **Also recorded, not pass conditions:** both indexes exist; `dbdigest.py` per table against the backup's (every
  table's rows equal, `cartography_entries` compared without `shownOnMap`; `room_master_table` is expected to differ,
  since its identity hash is the schema's).
- **Abort (the dispatch's):** a count mismatch, a crash, or a version other than 16. **Reported at once, and nothing
  restored without the owner.** If the copy reads version 15 with every file byte-identical to the backup, the database
  was not opened and the migration has not run; I report that at once too rather than decide it myself.
- **Prediction:** pass: version 16, integrity ok, 7 of 7 rows `shownOnMap = 0`, every count equal, every digest equal
  but `room_master_table`'s.

## Check 2: the report menu, and the chip's first appearance

Code: the report's "Entry options" menu (`ui/log/CartographyEntryReportScreen.kt:384-407`): "Edit entry" (`:389-393`),
then, only `if (!entry.isDraft && onSetShownOnMap != null)`, "Show on map" or, while shown, "Hide from map" with the map
icon (`:394-400`; labels `ui/map/JournalEntriesOnMap.kt:8`, `:11`), then "Delete entry" (`:401-405`). A saved entry
opens in the report from Entries (`ui/log/CartographyScreen.kt:554`); a draft opens in the editor
(`CartographyScreen.kt:465`, `:474`), whose own "Entry options" menu holds "Delete entry" only
(`ui/log/CartographyEntryEditScreen.kt:225-235`). The write sets `shownOnMap` alone (`data/local/CartographyEntryDao.kt:82-83`).
The chip is composed only while an entry is shown (`ui/availability/AvailabilityCompactMapUi.kt:1089-1090`,
`:1111-1117`) and reads `journalEntriesChipLabel` (`JournalEntriesOnMap.kt:23-24`): "1 journal entry on map" for one.
- **Pass (saved):** on the Journal, Entries, a real touch on the L0a entry's card opens its report (header
  "2026-09-27"); a touch on "Entry options" opens a menu whose dump holds exactly three items, top to bottom "Edit
  entry", "Show on map", "Delete entry", with "Show on map" between the other two by bounds.
- **Pass (draft):** a DEVICE CHECK draft opened from the drafts banner opens in the editor; its "Entry options" menu's
  dump holds "Delete entry" and **no** "Show on map" or "Hide from map". The menu is closed with Back and the draft left
  with Back, nothing typed and nothing chosen. (Opening a draft writes nothing: `CartographyViewModel.onOpenEntry`,
  `ui/log/CartographyViewModel.kt:171-212`, writes only a recomputed track snapshot, and the drafts keep no track.)
- **Pass (show):** "Show on map" touched on the L0a entry's menu; the menu then offers "Hide from map" in its place; on
  the Maps tab a chip's dump text is exactly **"1 journal entry on map"**. Before the touch, the Maps tab has no such
  chip (the control).
- **Prediction:** pass.

## Check 3: the highlight (owner-judged look)

Code: five halo layers, each directly below what it decorates, taking no taps, in `PaletteRole.JOURNAL_ENTRY`
(`ui/map/layers/MapLayers.kt:324` region outline, `:329` kept track, `:351` waypoints, `:353` finds, `:355` photos;
`journalHalo`, `:229-245`). Colours `MapPalette` day `0xFF005577` (`ui/theme/MapPalette.kt:98`) and night `0xFF00DDFF`
(`:127`), at opacity 1 (`MapLayers.kt:179-180`). Line halos are 3 dp wider each side than what they lie under: the
region's outline, and the track's casing, thinning with zoom as the track does (`ui/map/SightingsMap.kt:1467-1489`);
marker halos are the glyph grown by 1.5 + 3 dp (`ui/map/MarkerGlyphs.kt:184-199`). Only the kept records of saved,
shown entries among what the Maps tab draws are highlighted (`ui/availability/AvailabilityScreen.kt:923-925`). Night
Maps puts the markers on the night palette on every basemap, Satellite included (`ui/map/BasemapStyles.kt:162-166`).
- **Pass (in place, beneath):** at one camera framing the kept records, two frames with the Layers sheet's "Journal
  entries" switch on and then off (the sheet closed each time; the camera does not move). In the on frame a ring whose
  unblended middle pixels read the halo colour within 6 per channel surrounds each kept record drawn there; each
  record's own glyph or line pixels are equal in the two frames within 3 per channel (so the halo is under them). The
  records the entry does not keep (offline region "DEVICE CHECK 2026-09-28 B" and the two photos not attached to it)
  get no ring: equal in the two frames within 3 per channel (the negative control). The switch is restored on and read
  back. By pixel comparison, with my reading of the frames stated.
- **Captures for the owner, named in the verdict:** Topographical, Street and Satellite (the Layers sheet's third map
  type, USGS, zoom 15 at most), each by day and with Night Maps on. **The owner judges the colours; I do not rule on the
  look.** Night Maps is restored off and the basemap to Topographical, each read back.
- **Prediction:** pass, rings in `#005577` by day and `#00DDFF` at night; at least one basemap where the owner may find
  the ring hard to read (the planner's prediction 4). I do not judge that.

## Check 4: the chip against real insets, its fill, and its list

Code: the chip sits in a `FlowRow` after the taxon chip (`AvailabilityCompactMapUi.kt:1089-1119`). Portrait:
`TopCenter`, top padding `topInset + compassStripClearance + 8 dp` (`:1104-1108`; the clearance is the strip's
label-line height, `:621-625`). Short landscape, when the punch-hole side and the search bar's width are known:
`TopStart` or `TopEnd` on the punch-hole side, top padding `topInset + 8 dp`, in a column the search bar's width with
the chip at its start (`:1094-1103`). Fill: `MapIconStackButtonColorDark` or `Light`, `Bark #3B2E24` or
`Cream #EDE3D0` at 0.8 (`ui/map/MapChrome.kt:233-239`; `ui/map/JournalEntriesChip.kt:108-123`), content white or Bark,
shadow 4 dp. The list: a `DropdownMenu` at `MenuDefaults.containerColor` x 0.8 (`JournalEntriesChip.kt:68`,
`:133-137`; dark `SurfaceContainerDark #202020`, light `#F4EFE2`, `ui/theme/Color.kt:135`, `:141`), one row per
shown entry with its date and a trailing "Hide", a divider, and "Hide all" (`:140-161`).
- **Pass (place), portrait:** the chip's bounds (dump) lie wholly below the compass strip's bottom edge (its nodes, or
  its pixels if it has none), centred on the window within 3 px, and outside the status bar, navigation bar and
  cut-out frames from `dumpsys window`.
- **Pass (place), 90 and 270:** the chip's top is at or below the search bar's bottom, its start edge within 3 px of the
  bar's start edge, on the punch-hole side, and outside the system-bar and cut-out frames.
- **Pass (fill, 0.8), portrait, 90 and 270:** Part 1's method (`alpha.py`): a frame with the chip, then the same camera
  after the chip is gone, fitted per channel over a box inside the chip clear of its text and edges. Opacity is 1 minus
  the slope; pass is **0.78 to 0.82**, with the container colour solved from the intercept within 3 per channel of
  `Bark` (dark theme) or `Cream` (light). The chip is removed by its own list: **"Hide"** in portrait and at 270,
  **"Hide all"** at 90, each a real touch, so both actions are exercised. Each re-show is "Show on map" from the
  Journal. A camera move between the two frames voids the pair, and I say so.
- **Pass (list):** a real touch on the chip opens a list whose dump holds "2026-09-27", "Hide" and "Hide all", and no
  other row. Recorded beyond the dispatch, from the J8 report's device-only list: the list's own fill, by the same fit
  between the list open and closed with Back (an observation; expected 0.8 of `#202020` in the dark theme).
- **Prediction:** place passes in all three; the fill reads 0.80 within 0.02; the list passes.

## Check 5: the landscape chip against the icon cluster (owner-judged, "1 A")

Code: the chip's landscape place is above (Check 4). The J8 report's finding 1: at the default the cluster is on the
punch-hole side too, and the chip's row is composed after the cluster, so where they overlap the chip is drawn over it
and takes the touch.
- **Measured, at 90 and 270:** the chip's bounds; the cluster's container bounds and each of its controls' bounds
  (dump); their intersection, or the gap if none.
- **The touch:** where they overlap, I first name every cluster control whose bounds contain the overlap. If each of
  them only moves the view (for example "Reset orientation to north", zoom or locate), I make real touches at no fewer
  than three points spread across the overlap, and after each read whether the chip's list opened or the cluster
  control acted (dump, and for a bearing reset the compass). If any of them would create or change data (the record
  button above all, which the data rule forbids), **I do not touch there**, and record it as not run with the control
  named.
- **Captures** at both rotations, named. **Report and capture; I do not rule.**
- **Prediction:** the overlap reproduces at 90 and at 270 (the planner's prediction 3), and a touch in it opens the
  chip's list.

## Check 6: the bubble's entry line, Open entry, and the unsaved-changes prompt

Code: a highlighted record's bubble lists each shown entry keeping it, up to three, as a `TextButton` showing the date
in the report header's form with the content description "Open entry <date>" (`ui/map/MapBubble.kt:455-468`;
`JournalEntriesOnMap.kt:27`, `:30`), only on the Maps tab and only while the switch shows the highlights
(`AvailabilityScreen.kt:1249`). A tap switches the compact tree to the Journal with `VIEW_ENTRY`
(`AvailabilityScreen.kt:1257-1266`), and `CartographyScreen` opens the entry in its report (`:258-274`). If a saved
entry is open in its editor with unsaved changes, it asks "Save your changes?" first (`:265-269`;
`CartographyEntryEditScreen.kt:317-339`, with Save, Discard and Cancel), and Save or Discard then opens the request
(`CartographyScreen.kt:276-285`). Typing in a saved entry's editor writes nothing until Save
(`CartographyViewModel.kt:664-679`); Discard reloads the stored row (`:446-471`); backgrounding while dirty saves
nothing and shows "Welcome back" on return (`CartographyScreen.kt:347-362`).
- **Pass (line):** a real touch on a DEVICE CHECK record the entry keeps (a DEVICE CHECK find, or the DEVICE CHECK
  waypoint) opens its bubble, whose dump holds a node with text "2026-09-27" and content description
  **"Open entry 2026-09-27"**. What TalkBack speaks is the owner's; TalkBack is not turned on.
- **Pass (open):** a real touch on that line puts the Journal in front with the L0a entry's report open (header
  "2026-09-27", "Entry options").
- **Pass (prompt), on the L0a entry only:** its report, "Edit entry", one character typed into "Your own account";
  the Maps tab by its navigation item; the same record's bubble; its date line. The Journal comes up with "Save your
  changes?" (Save, Discard, Cancel). A real touch on **Discard**. Then the L0a entry's report is open, and its text
  node has the length it had before the typing (I compare lengths and a hash, and quote nothing). The typed character
  is never saved: the final database copy's `cartography_entries` digest must equal the backup's.
- If "Welcome back" ever appears I choose "Continue editing" and say so. Nothing else is typed anywhere.
- **Prediction:** pass.

## Check 7: the Layers sheet's "Journal entries" switch

Code: "Journal entries" is the last Maps-tab overlay (`ui/map/MapLayersSheet.kt:133-141`), on layer
`journal-entry-tracks-layer` (`MapLayers.kt:177`), the state owner of the other four halos (`:243`); it persists by
layer id as `map.layer.journal-entry-tracks-layer.visible` (`data/repository/DataStoreMapPreferencesRepository.kt:124-126`).
The chip reads the shown entries whatever the switch (`AvailabilityScreen.kt:923-935`; `AvailabilityCompactMapUi.kt:1089`).
- **Pass (hides, chip stays):** switch off: at one camera, every kept record's ring is gone (its pixels equal, within
  3 per channel, to the same place in a frame taken with the entry hidden or the switch's own off frame from check 3),
  while the chip's dump still reads "1 journal entry on map".
- **Pass (persists):** `am force-stop`, `am start`: the sheet shows "Journal entries" off, `map_preferences` holds
  `map.layer.journal-entry-tracks-layer.visible = false`, no ring is drawn, and the chip is there.
- **Restored:** switched on, the rings back, and the key read back `true`.
- **Prediction:** pass.

## Check 8: the failed-write Toast

**Not run**, as the dispatch says: it cannot be forced on the device without changing app code.

## Settings and data to restore

- `shownOnMap` false on the L0a entry, read back from a final database copy (force-stop, `dbcopy.sh`, `dbverify.sh`):
  `shownOnMap = 0` on all 7 rows, user_version 16, integrity ok, every count equal to the backup's.
- `user_rotation` 0 and `accelerometer_rotation` 0; `font_scale` 1.0 (not planned to change); Night Maps off; basemap
  Topographical; every overlay on, "Journal entries" included; the app left on the Maps tab, in portrait.

# Verdicts

## Check 1: the migration on real data: **pass**

- **Launch:** `am start -n …/.MainActivity` at 21:07:41.8Z (`16-launch.txt`); pid 31050, `MainActivity` in focus. No
  system or Google prompt over the app. By 21:07:50Z the Maps tab had drawn the stored records (finds, photos and both
  offline region circles, `17-first-launch.png`, by my reading), so the database had been opened.
- **Crash reads** at 21:08Z: `logcat -d -b crash` **0 bytes** (`18-crash-after-launch.txt`); `logcat -d`, 331,486 lines
  from 10:13Z, **0** `FATAL EXCEPTION` (`19-log-after-launch.txt`).
- `am force-stop` at 21:08:16Z, `pidof` empty (`20-force-stop-after-launch.txt`). `dbcopy.sh db-migrated`: the device's
  and the local sha256 **match** for all three files (`db-migrated-device-sha256.txt`, `db-migrated-local-sha256.txt`):
  `forager.db` 229376 bytes `e88effc0…61af7c93`, `forager.db-wal` 466944 bytes `79359a6b…3cf0dd00`, `forager.db-shm`
  32768 bytes `fd4c9fda…b8549389eb`. `db-migrated-raw/` is read-only.
- **Verified** (`db-migrated-verify.txt`):
  - header "SQLite format 3\0", page size 4096, WAL mode;
  - `PRAGMA integrity_check`: **ok**;
  - `PRAGMA user_version`: **16**;
  - `cartography_entries` has `shownOnMap`, `INTEGER`, not null (column 6), and `COUNT(*) = 7`, rows with
    `shownOnMap <> 0` **0**, rows with it null 0;
  - **the same 18 tables with exactly the backup's counts**: android_metadata 1, cached_searches 2, cartography_entries
    7, find_refs 2, offline_region_refs 1, photo_refs 1, track_refs 1, waypoint_refs 3, log_entry_photos 1, log_photos 3,
    mushroom_log_entries 3, offline_regions 2, planned_trips 0, room_master_table 1, sqlite_sequence 1, track_points 23,
    tracks 1, waypoints 3.
- **Also recorded:** both indexes, `index_cartography_entries_date` and `index_cartography_entries_isDraft`, exist,
  with the primary key's autoindex. `dbdigest.py` (`db-migrated-digest.txt`) equals the backup's for **17 of 18**
  tables row for row, `cartography_entries` compared without `shownOnMap`. The one that differs is
  `room_master_table`, as predicted: its identity hash went from `59205bd8e5811737f6e5d12fe330140f` to
  `b8e97e83e4139dbf92f791836dde6002`, which are the `identityHash` values of the exported `15.json` and `16.json`.
- **One thing I did not predict:** the migrated copy's WAL has its first 16 header bytes zero (magic, format, page size
  and checkpoint sequence), the rest of it not zero. A WAL with no valid magic is empty to SQLite, so the database is
  what `forager.db` holds, and the digests above were computed from exactly that. It matches SQLite resetting the WAL
  after a checkpoint, but which step on the phone did it (the migration's own transaction, Room, or Android's WAL
  handling) I have not established. It changes no figure above.
- **Prediction:** held in full.

Not an abort: no count mismatch, no crash, version 16.

## Check 2: the report menu, and the chip's first appearance: **pass**

Relaunched with `am start` at 21:09:11Z (pid 31805).
- **Control:** the Maps tab before any entry was shown (`21-c2-maps-before.xml`/`.png`): no node with "journal entr" in
  its text, and no chip under the compass strip.
- **Saved entry:** Journal, Entries (`22-`): a real touch on the L0a entry's card at (300, 1200), away from "New entry",
  opened its report, header "2026-09-27", "Entry options" at `[900,279][1035,414]` (`23-`). A touch there opened a menu
  of exactly three rows (`24-c2-report-menu.xml`): "Edit entry" `[610,437][1035,572]`, **"Show on map"
  `[610,572][1035,707]`**, "Delete entry" `[610,707][1035,842]`. Show on map is between Edit and Delete.
- **Show:** a real touch on "Show on map" at 21:10:02.9Z (`25-`). The menu reopened reads "Edit entry", **"Hide from
  map"**, "Delete entry" (`26-`); it was closed with Back (`27-`).
- **The chip:** on the Maps tab (`28-c2-maps-chip.xml`/`.png`), a clickable node `[310,273][771,408]` whose text is
  exactly **"1 journal entry on map"**. The camera was the same as the control's (the relaunch's opening camera), and
  the DEVICE CHECK region's circle and the markers now carry a dark ring (by my reading; measured in check 3).
- **Draft:** Journal, the drafts banner's "Continue ›" opened "Unfinished entries" with five DEVICE CHECK drafts (`31-`).
  A touch on the first opened it **in the editor** ("Your own account (optional)" field, `32-`). Its "Entry options"
  menu holds one row, **"Delete entry"**, and no "Show on map" or "Hide from map" (`33-c2-draft-menu.xml`: 0 matches).
  Back closed the menu (`34-`), Back left the draft for the list (`35-`) and Back returned to Entries, still "5
  unfinished entries" (`36-`). Nothing was typed or chosen.
- Crash buffer 0 bytes after the check (`37-`); Forager pid 31805 throughout.
- **Prediction:** held.

## A planner message received during the run, verbatim

Relayed by the coordinator at about 21:22Z, after my session was cut off by a network outage in the middle of check 3's
set-up (the last evidence file was `39-c3-sheet`). No planner-log line was given, so none is cited.

> Planner: resume dispatch `2026-09-28-72`. Your turn was cut off by a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"). Nothing is wrong with your work, and your context is intact.
>
> The planner's read at 21:22Z:
> - `origin/device-j8` is at `2f4460d` (checks 1 and 2 pushed);
> - evidence runs up to `39-c3-sheet`;
> - the S22 has versionName 1.0.1389+g99de6c24, Forager in focus, user_rotation 0.
>
> **Before your next action:**
> - re-read the phone's state (focus, rotation, any open sheet or menu, and which DEVICE CHECK entries have `shownOnMap` on);
> - run `git status` in your worktree.
>
> Do not assume your last command completed. Then carry on from check 3. All rules of `-72` stand: DEVICE CHECK entries only; hide every one again and read it back; stop at any prompt over the app. Push after each check. Hand back when you finish or stop.

**The state I read back before touching anything (21:23Z):**
- `git status`: `device-j8` clean and level with `origin/device-j8` at `2f4460d`.
- Phone: awake, `isKeyguardShowing=false`, Forager `MainActivity` in focus, pid 31805 (the check 2 relaunch),
  `user_rotation=0`, `accelerometer_rotation=0`; `versionName=1.0.1389+g99de6c24`, `lastUpdateTime` 14:03:09 PDT,
  `firstInstallTime` 2026-09-22 11:15:05, all unchanged. Crash buffer **0 bytes** (`40-crash-resume.txt`).
- **The Layers sheet was still open** (`41-resume-state.xml` is byte-identical to `39-c3-sheet.xml`), every overlay
  switch on, "Journal entries" on. My last command had completed: it opened the sheet and dumped it. No menu or prompt
  was open. I closed the sheet with Back (`42-`); the chip still read "1 journal entry on map".
- **`shownOnMap`**, read from a database copy after `am force-stop` at 21:23:34Z (`db-resume-*`; the copy matches the
  device, integrity ok, user_version 16): **1 on `6107d76c…` (the L0a DEVICE CHECK entry) only**, 0 on the other six
  rows. `dbdigest.py` equals the post-migration digests for all 18 tables (`shownOnMap` excluded): nothing else was
  written.
- The force-stop was mine, to read the database cleanly. Forager was relaunched with `am start` for check 3.

## Check 3: the highlight: **halos drawn in place and beneath their own glyphs (pass); two halos drawn for waypoints the map does not draw (fail against J8's own rule); marker halos lie over the kept track (not as I pre-registered); the look is the owner's**

Relaunched with `am start` at 21:24:11Z (pid 5297) after the resume read-back. Pairs are the same camera with the
Layers sheet's "Journal entries" switch on, off and on again (`jtoggle.sh`, which opens the sheet from the Layers row,
taps the switch's row, reads its checked state back and closes the sheet with Back; each toggle is in `snaps.log`).
`halo.py` compares an on frame with an off frame over the map left of the cluster; `over-under.py` is Part 1's
`211-over-under.py`, copied unchanged. The zooms are not measured: "about 12" is the relaunch's opening camera, "17" is
five double-tap steps from it (OpenTopoMap's maximum, `ui/map/Basemap.kt`), and Satellite clamps to its maximum, 15.

**(i) Rings in the halo colour round the kept records: pass.**
- About zoom 12, Topographical, day (`43-` on, `44-` off, `45-` on again): **32,188 px** change between on and off,
  **26,159** of them within 6 per channel of `#005577`. They form two blobs: a ring on the 1 km DEVICE CHECK region's
  circle `[319,975][759,1415]` and one round the markers `[485,1102][614,1238]`. On against on-again differs by 498 px,
  all of it in four small blobs at the location puck, which pulses; so the camera did not move.
- Zoom 17, Topographical, day (`47-`, `48-`, `49-`; crops side by side in `50-c3-z17-crops-on-off-on.png`): the track
  carries a ring on each side of its white casing, and the markers carry theirs (by my reading of `50-`).
- The one kept find away from the cluster (135 m east, "DEVICE CHECK find 2" by the L0a record) at zoom 15 carries a ring
  of 2,247 px of `#005577` by day on Satellite (`58b-`) and about 2,205 px of `#00DDFF` at night on Satellite, Street and
  Topographical (`64-`, `65b-`, `66b-`), counted in the same 130 x 120 px box.
- Night, Street, zoom 17 (`68b-` on, `69-` off, `70-` on again): **20,641 px** change, **16,720** within 6 of
  `#00DDFF`.

**(ii) Each halo is beneath its own record: pass. Every record's pixels unchanged, as I pre-registered it: not held.**
- Photo glyph `#C1154F`: **0 of 637** interior pixels change between `48-` (off) and `47-` (on).
- With Finds and Photos switched off to uncover the waypoint pin (`51-` on, `52-` off, `53-` on again; crops in
  `54-c3-z17-wpt-only-crops.png`): waypoint pin `#350560`, **0 of 1,287** interior pixels change.
- **But the kept track's own line is covered in places:** 886 of its 4,733 interior `#A122F8` pixels change in the same
  pair, and **822** of those read `#005577` in the on frame, in `[497,1054][584,1205]`, where the waypoint halos lie.
  Likewise 76 of 210 find-cap pixels change in `47-`/`48-`, where the photo's halo lies over the find. The marker halos
  are in `ZGroup.MARKERS` (`MapLayers.kt:236`, `if (kind == LayerKind.MARKER) ZGroup.MARKERS`), above every line, so a
  marker's halo is under its own marker and over the track and over the markers below it in the stack. The search-centre
  reticle (a black crosshair, drawn with no location set; Part 1's flag 8) is covered the same way (`54-`, middle crop
  against the outer two).
- So my condition "each record's own glyph or line pixels are equal in the two frames" does not hold for the track and
  the find where they overlap other kept markers. The dispatch's words, "beneath their own glyphs and lines", do hold for
  every halo. Which reading J8 intended is not mine to rule.

**(iii) The negative controls.**
- The 5 km region "DEVICE CHECK 2026-09-28 B", which the entry does not keep: **no ring**. No changed pixel lies on its
  outline in `43-`/`44-` (both blobs are on the 1 km circle and the markers). **Pass.**
- The two photos the entry does not keep: **not observable.** From the database copy (`55-c3-record-offsets.txt`,
  offsets in metres only), all three photos lie within about 1 m of each other, two of them at the same point, so the
  kept photo's glyph and halo cover the other two.

**(iv) A finding: halos drawn for two waypoints the map does not draw.**
- In `51-`/`53-` (on) two pin-shaped halos, one about 55 px left of and above the visible pin and one below it over the
  reticle, have **no waypoint glyph over them**. In `52-` (off) only one pin is drawn, and in no frame is there a second
  or third pin (a search for `#350560` finds one stray pixel in `48-` and none in `47-`).
- Their places match the entry's other two kept waypoints. From the database copy, the ORIGIN waypoint lies 8.0 m west
  and 5.8 m north of the DEVICE CHECK waypoint, and the END waypoint 2.4 m east and 10.7 m south. At the measured scale
  those put the pins' heads where the two halos are. That match is my inference.
- The code agrees. The Maps tab draws `mapWaypoints` (`ui/availability/AvailabilityScreen.kt:828-829`), which is
  `mapVisibleWaypoints`: waypoints with no designation only, and an origin only while it is the navigation target
  (`ui/availability/AvailabilityPureFunctions.kt:59-66`). The highlight is computed from the unfiltered `waypoints`
  (`AvailabilityScreen.kt:923-924`). The use case's own doc says its `waypoints` are "the waypoints the Maps tab draws"
  (`domain/GetJournalEntryHighlightsUseCase.kt:50`).
- So on this phone **a halo draws where no record is drawn**, which is what the J8 report quotes the dispatch as ruling
  out: "a kept record that is not drawn today ... is not highlighted"
  (`docs/audits/2026-09-28-j8-entries-on-map-completion-report.md:143-145`). **A fail against that rule, by my reading**,
  and it reaches every entry that keeps a track's ORIGIN or END waypoint. Not investigated further, and nothing
  changed. See Flags.

**Captures for the owner (the colours are the owner's to judge; I do not rule on the look):**

| basemap | day `#005577` | night `#00DDFF` |
|---|---|---|
| Topographical | `43-c3-z12-on.png` (about 12, the region's ring); `47-c3-z17-on.png`, `56-c3-z17-all-on.png` (17) | `66b-c3-topo-night-z15.png` (15); `67-c3-topo-night-z17.png` (17) |
| Street | `57b-c3-street-day.png` (17) | `65b-c3-street-night-z15.png` (15); `68b-c3-street-night-z17.png` (17) |
| Satellite | `58b-c3-satellite-day.png` (15) | `64-c3-satellite-night.png` (15) |

The same cameras with the switch off: `44-`, `48-`, `52-` (Finds and Photos off), `69-`. Crops: `50-`, `54-`. By my
reading only, and for the owner to weigh: the day colour is dark against the dark Satellite ground (`58b-`).

**Restored and read back** (`74-map_prefs-after-c3.pb`, `75-c3-end.xml`): Finds and Photos on (`56a-`, `56-`); basemap
Topographical (`71-`; the Layers row reads "Layers: Topographical map"); Night Maps on at 21:32:58Z (`61-`, `62-`) and
off at 21:36:10Z (`73-`), `night_mode.maps = False`; "Journal entries" on, `map.layer.journal-entry-tracks-layer.visible
= True`. That key did not exist at the start; the app can set it but not remove it, so it is left at its default value,
as Part 1 did for the other overlay keys. Every other overlay key `true`, as at the start. Crash buffer 0 bytes
(`76-`); pid 5297 throughout.

**Prediction:** rings in both colours held. "The negative controls get no ring" held for the region and could not be
observed for the photos. I did not predict the undrawn waypoints' halos or the marker halos over the track.

## Check 4: an addition to the pre-registration, written after reading the portrait bounds and before any touch

In portrait the chip's clickable node is `[310,273][771,408]` (`28-`, `75-`). The compass strip's surface is drawn down
to y 254 (pixels of `75-c3-end.png`: the strip's fill (77,67,59) through row 254, map from row 255), so the chip is
**18 px below the strip's drawn edge**. But the strip's coordinate readout, a clickable text node, has bounds
`[564,163][902,298]` (its 48 dp touch target), which reach **25 px into the chip's bounds** over x 564 to 771. My
condition says "below the compass strip's bottom edge (its nodes, or its pixels if it has none)", and the strip's nodes
and its pixels disagree here. So I add a touch test before looking further:
- **Five real touches** in the overlap `[564,273][771,298]`, spread across it: (580, 280), (630, 293), (668, 285),
  (715, 278), (760, 292). After each, a dump: the chip's list open ("Hide all" present) means the chip took it; the
  readout switching between its MGRS and its decimal-degree form means the strip took it. Each is undone before the
  next: the list closed with Back, or the readout touched again at its own centre (820, 230), outside the chip. Both
  are view-only; neither writes data.
- **Control:** one touch at the chip's centre (540, 340), expected to open the list.
- **Prediction:** the chip takes all five, since the chip's row is composed after the strip (`AvailabilityCompactMapUi.kt`
  places the row after the strip in the same `Box`); by my reading of the code, not established.

## Check 4: the chip against real insets: **place pass (portrait, 90, 270); list pass; fill 0.83 to 0.84 by my measure, not the pre-registered 0.78 to 0.82**

Bounds are from dumps; the drawn edges from screenshot pixels; the system-bar frames from `dumpsys window` (`77-` portrait,
`92-` at 90, `102-` at 270). Each chip node is 135 px (48 dp) tall, its minimum touch target; the pill drawn inside it
is about 92 px (33 dp).

| | chip node | pill drawn | what it sits under | bars and cut-out | verdict |
|---|---|---|---|---|---|
| portrait (`75-`, `83-`) | `[310,273][771,408]` | x 310-770, y 294-385 | strip drawn to y 254: pill 40 px below it; centred (node centre 540.5, window 540) | status and cut-out `[0,0][1080,75]`, nav `[0,2181][1080,2316]`: clear | **pass** |
| 90 (`91-`, `95-`) | `[75,236][536,371]` | x 76-535, y 258-350 | search bar drawn x 75-1135, bottom 211: pill 47 px below; start 76 against the bar's 75 | cut-out `[0,0][75,1080]` left, status `[0,0][2316,84]`, nav `[2181,0][2316,1080]` right: clear | **pass** |
| 270 (`101-`, `103-`) | `[1180,236][1641,371]` | x 1180-1640, y 257-348 | bar drawn x 1181-2240, bottom 211: pill 46 px below; start 1180 against the bar's 1181 | nav `[0,0][135,1080]` left, status, cut-out `[2241,0][2316,1080]` right: clear | **pass** |

The punch-hole side is the left at 90 and the right at 270, and the chip is on it both times, at the bar's start (its left
edge), as `AvailabilityCompactMapUi.kt:1094-1103` places it. The search bar's `EditText` node starts right of its search
icon (x 194 at 90, 1299 at 270), so I took the bar's start from its drawn fill.

**The portrait touch-target band (the addition above).** The coordinate readout's node reaches y 298 and the chip's node
starts at y 273. Five real touches in that band, at (580, 280), (630, 293), (668, 285), (715, 278) and (760, 292)
(`79-c4-touch-2` to `-6`, `snaps.log` 21:44:32Z to 21:44:51Z), reached **neither**: the chip's list did not open and the
readout did not switch form. Each was followed by a touch on the readout at its own centre (820, 230), which switched
it every time (MGRS, decimal, MGRS, and so on in the dumps), so the readout's own control works. The chip's centre
(540, 340; `79-c4-touch-1`) and its pill's top edge (540, 300; `81-`) opened the list. So the 25 px where the two touch
targets overlap belongs to neither control; what took those touches (the map, presumably, since nothing visible
happened) is not established. The readout was left in its MGRS form, as found (`82-`). The pill itself is 40 px clear
of the strip.

**The list: pass, at all three rotations.** A real touch on the chip opens a list of exactly two rows: "2026-09-27"
with a trailing "Hide", and "Hide all" (`84-` portrait, `96-` at 90, `104-` at 270). Both actions work: **"Hide"** in
portrait at 21:46:46Z and at 270 at 21:51:30Z, and **"Hide all"** at 90 at 21:49:58Z, each removed the chip (0 matches
in `86-`, `97-`, `105-`). After each, the report's menu offered "Show on map" again, and "Show on map" re-showed the
entry (21:47:59Z, 21:50:42Z).

**The fill.** Part 1's `alpha.py`, fitting the chip frame against the same camera after the chip was hidden, over two
strips inside the pill above and below its text, clear of the edges:

| | slope a (per channel) | 1 - a | intercept b (R, G, B) | pair |
|---|---|---|---|---|
| portrait, above | 0.167 | 0.833 | 47.0, 37.0, 29.0 | `83-`/`86-` |
| portrait, below | 0.160 | 0.840 | 47.0, 37.0, 29.0 | |
| 90, above | 0.165-0.167 | 0.834 | 46.8, 36.7, 29.1 | `95-`/`97-` |
| 90, below | 0.163 | 0.837 | 46.8, 36.9, 28.8 | |
| 270, above | 0.165-0.166 | 0.834 | 47.1, 37.0, 29.1 | `103-`/`105-` |
| 270, below | 0.161-0.162 | 0.839 | 46.9, 36.9, 29.0 | |

rms 0.67 to 1.25. The camera did not move within any pair: slope 1.000, rms 0 in boxes away from the chip and the
halos (`83-`/`86-`, `95-`/`97-`, and at 270 two boxes clear of the region's ring, which the Hide also removed). The
controls `83-`/`85-` (both with the chip) give slope 1.000 in the chip box.
- **By the pre-registered measure, opacity 1 - a is 0.833 to 0.840, outside 0.78 to 0.82. Not held.** The container
  colour solved that way, b / (1 - a), is (56.4, 44.4, 34.8) in portrait, within 3 of `Bark` (59, 46, 36).
- **What the figures fit better, by my reading:** every intercept equals 0.8 x `Bark` (47.2, 36.8, 28.8) to within 0.4
  per channel, at all three rotations. That is what a fill of exactly 0.8 gives if the ground under the pill reaches it
  at 0.83 of its brightness. The chip has a 4 dp shadow (`JournalEntriesChip.kt:123`), and the ground just outside the
  pill is darkened by it: slope 0.962 in the 8 px above the pill and 0.944 in the 12 px below (`83-`/`86-`), deeper
  below, as a shadow is. So I read it as a fill at 0.8 of `Bark` over the chip's own shadow, which takes about 17% of
  the ground's light first. That is an inference from the fits, not a measurement of the shadow under the pill.
  Whether 83 to 84% of the ground hidden meets "80%" is the owner's.
- **The list's fill (an observation, the J8 report's device-only item):** `84-`/`85-`, `96-`/`95-` and `104-`/`103-`
  (open against closed), a box inside the list clear of its text: slope 0.191 to 0.202, so 0.80 to 0.81, and the
  container colour b / (1 - a) = 31.3 to 32.7 per channel, which is `#202020` (32).

Crash buffer 0 bytes after checks 4 and 5 (`106-`); pid 5297 throughout. `user_rotation` back to 0,
`accelerometer_rotation` 0.

**Prediction:** place and list held. "The fill reads 0.80 within 0.02" did not hold, as measured.
