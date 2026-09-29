# Journal backup and restore: completion report (dispatch `2026-09-28-127`)

Coder session, worktree `/home/zynergy-labs/Zynergy/forager-wt/journal-backup`, branch `journal-backup`.
Written by the coder; the planner writes the record. This file grows in sections; nothing already in it is rewritten.

**Model.** The session is configured for `claude-sonnet-5-5` (the system prompt names it). I cannot read the serving model from inside the session, so I do not claim it.

## Governing text, verbatim

The owner's rulings, as the dispatch quotes them: "on-device backup for journal entries"; "3 B" (entries plus everything they refer to: finds, photos, tracks, waypoints and offline-region details; map tiles are not included, they are re-downloaded); "4 A" (a file saved where the user chooses (SAF), which survives uninstall); "5 C scheduled set to off by default, must be turned on by user"; "6 A - restore to app"; "1 C ask to replace or merge"; "2 C" (daily, weekly or monthly); "4 A" (the controls go in Tools, then Settings); "A, approve the rest" (the copy).

The dispatch's stop rule and abort conditions, verbatim: "**Verification before building (file:line), and stop on any unruled choice**"; "**Abort conditions:** any path that could lose live data on failure; new copy; an unruled choice (offline-region visibility, retention); a tests-first test passing at base; a revert build that does not compile; a non-held failure; two failed fixes; a refused push." Item 6: "**Stop and report options** for how a restored region becomes visible (or re-downloadable). Do not choose." Item 7: how old scheduled backups are kept or pruned "is **unruled: stop and report options**, unless it is one file per run with nothing deleted."

The approved copy is used exactly and nothing else is added: "Back up now", "Automatic backup", "How often" (Daily, Weekly, Monthly), "Backup folder" / "Choose folder", "Restore from backup", the section title "Backup"; the messages "Backup saved.", "Couldn't save the backup.", "Automatic backup is off until you choose a folder.", "Restore complete.", "Couldn't restore that backup."; the prompt title "Restore this backup?", the three body paragraphs verbatim, and the buttons Replace, Merge, Cancel.

## Base and premises, verified before building

- **Base.** `origin/journal-redesign` at `bf41dcb` (fetched 2026-09-29), which contains `881cfbd` (`git merge-base --is-ancestor` true). `CLAUDE.md` is unchanged between `881cfbd` and the base (`git diff 881cfbd HEAD -- CLAUDE.md` is empty). Worktree cut from `bf41dcb`.
- **The premise pulse is a claim about the past**, so I re-read what I rely on at this base: the schema is `app/schemas/.../16.json` (15 entities), `ForagerDatabase.kt:149-169` (entities), `:130-147, :196-205` (`create`: migrations `3_4` to `15_16` registered, destructive fallback debug-only). The database is `forager.db` (`ForagerDatabase.kt`, `create`). Photos are `filesDir/photos/<uuid>.jpg` (`photo/FilePhotoStore.kt:100,120,191`); `log_photos.relativePath` is relative to `filesDir` (`:22-23`).
- **WorkManager is not a dependency** (`git grep androidx.work` in `*.kts` and `*.toml` is empty; `~/.gradle` has no `androidx.work`). Maven metadata reports `androidx.work:work-runtime` 2.12.0 as the latest release (network reachable). I will add it pinned in `gradle/libs.versions.toml`, and `work-testing` for the worker test. **Stated here as the dispatch asks.**
- **`VACUUM INTO` is not available on the app's whole range.** `minSdk` 26 (`app/build.gradle.kts:226`); Android's bundled SQLite is older than 3.27 (which added `VACUUM INTO`) below API 30 [platform knowledge; not checkable in this repo]. So `VACUUM INTO` is out.
- **Room** is 2.8.4 (`gradle/libs.versions.toml:39`), DataStore 1.2.1 (`:50`).
- **No `@ForeignKey`** in `ForagerDatabase` (CLAUDE.md, Room-for-relations pitfall; `LogPhotoEntity.kt:33-38` per the pulse), so references are plain columns and the backup has to know them itself. The full column and key list is in the schema JSON; the ones that matter: `track_points.id` is **auto-generated** (a per-phone counter), `offline_regions.id` is **MapLibre's own region id** (`OfflineRegionEntity.kt:13-17,30` per the pulse), the five `cartography_entry_*_refs` tables and `log_entry_photos` have composite keys, and there are four nullable link columns (`waypoints.trackId`, `tracks.originWaypointId`, `mushroom_log_entries.offlineRegionId`, `mushroom_log_entries.draftOfEntryId`).

## Choices the dispatch asks me to state, made now

**1. The snapshot: hold the write lock, copy the database and its WAL, fold the copy.** A plain file copy under WAL can be torn (a checkpoint mid-copy), as the manifest warns (`AndroidManifest.xml:91-95`). So: a `wal_checkpoint(TRUNCATE)` first (best effort, shrinks the WAL), then inside a Room write transaction (which takes SQLite's single write lock, so no writer can commit and nothing can auto-checkpoint) copy `forager.db` and `forager.db-wal` to a scratch directory, then open the **copy** with plain `SQLiteDatabase`, checkpoint it and set `journal_mode=DELETE`, leaving one self-contained file. Readers keep running throughout. This is "a checkpoint, then a copy" made safe under concurrent writes; I did not use `VACUUM INTO` (above). Live data is only read.
**2. The archive.** A zip: `manifest.json` first, then `forager.db`, then `photos/<name>` for every `log_photos` row whose file exists. The manifest: `formatVersion` (1), `appVersionCode`, `schemaVersion` (the snapshot's `PRAGMA user_version`), `createdAtEpochMillis`, and for every file its path, byte size and SHA-256. **Settings (DataStore) are not included**, per ruling 3 B, which names journal records and what they refer to and nothing else. The snapshot is the whole database; restore reads only the journal tables (below).
**3. What "journal data" is.** Twelve tables: `mushroom_log_entries`, `log_photos`, `log_entry_photos`, `tracks`, `track_points`, `waypoints`, `offline_regions`, `cartography_entries` and its five `_refs` tables. **Not** `planned_trips` and `cached_searches`: ruling 3 B does not list them and the premise pulse calls them "not journal data". A test asserts every table in the schema is in one list or the other, so a future table cannot fall through silently (a check that enumerates, with what each enumerated thing means stated here, per CLAUDE.md's last Testing entry).
**4. Restoring across versions.** Manifest `schemaVersion` above `ForagerDatabase`'s version, or a snapshot whose own `user_version` disagrees with the manifest: refused, reason logged, the user sees "Couldn't restore that backup." An older one: the extracted copy is opened by Room with the registered migrations on **a scratch file, never the live database**, by a builder with **no destructive fallback** (the debug fallback would silently give an empty database, the opposite of a restore). Room's own identity-hash check is the proof the migrated copy matches the schema.
**5. Replace is one SQL transaction on the live database plus a reversible photo step.** Order: extract to scratch, verify every hash, `PRAGMA integrity_check` on the raw snapshot, migrate the scratch copy, `integrity_check` again. Only then: copy the backup's photos into `filesDir/photos` (a name that already exists with different bytes is moved aside first), then one Room transaction deletes the twelve tables' rows and inserts the backup's. Any failure rolls the transaction back and puts the moved-aside files back and removes the copied-in ones, so the phone is as it was. Only after commit are the old photo files that no restored row references deleted. The database is **not** swapped as a file: the app's Room instance stays open, so no restart is needed and live queries update. The alternative (close the database, swap files, restart the process) leaves a window with no database, which is why I did not take it.
**6. Merge, and the reference rule (stated as the dispatch asks).** Insert a record when its id is absent on the phone; an id already present is skipped and **the phone's copy wins whole**. A record inserted by the merge gets its dependent rows; a skipped record gets none of the backup's (so the phone's copy is not altered):
- *Independent records* (`waypoints`, `tracks`, `offline_regions`, `log_photos`, `mushroom_log_entries`, `cartography_entries`): inserted if the id is absent.
- *Owned rows* (`track_points`, `log_entry_photos`, the five `_refs` tables): inserted only if their **owner** (the track, the find, the entry) was inserted by this merge, **and**, for a row that names another record, that record exists on the phone once the merge's inserts are done. Otherwise the row is dropped and counted. `track_points` are inserted **without their id**, because the id is a per-phone counter and would collide.
- *Nullable link columns* on an inserted row (`trackId`, `originWaypointId`, `offlineRegionId`, `draftOfEntryId`): set to NULL when the target does not exist on the phone after the inserts, which is the rule the app already applies when a target is deleted (`WaypointEntity`/`TrackEntity` doc comments, owner decisions on both columns' meaning).
- Photo files are copied only for `log_photos` rows this merge inserted.
A consequence to know: an entry's "kept" snapshot row whose target record was deleted on the source phone is also dropped by a merge, because the rule is "no dangling row". Replace copies such rows verbatim.
**7. The schedule.** `ACTION_OPEN_DOCUMENT_TREE`, with `takePersistableUriPermission`; a DataStore file for {enabled, frequency, folder}, per CLAUDE.md's Room/DataStore rule (flat settings); WorkManager periodic work (24 h, 7 d, 30 d); **off by default**; turning it on with no folder leaves it off and shows "Automatic backup is off until you choose a folder." **Retention is one file per run and nothing is deleted**, which is the dispatch's own carve-out, so retention is **not** a stop. The consequence, for the owner: the folder grows without bound until the user deletes files. A run with an unreadable folder (permission revoked) fails, is logged and leaves the setting alone: no silent switch-off, no silent success.

## Stops

**Item 6, offline regions: report, not built.** Restore copies the `offline_regions` rows like every other journal table (ruling 3 B names "offline-region details"), and the entry-side snapshots (`cartography_entry_offline_region_refs`, `mushroom_log_entries.offlineRegionId`) come with them. What is **not** decided, and is not built here, is how a restored region becomes visible or re-downloadable. What I found:
- Today the Offline maps list is built from MapLibre's own store (`map/MapLibreOfflineMapRepository.kt`, `listRegions`) and reconciled against Room (`map/OfflineRegionReconciliation.kt:27`). Per `-106`'s report, a Room row with no MapLibre region is **kept and not shown**. So a restored row is invisible.
- **A second problem the dispatch did not name: the key.** `offline_regions.id` is MapLibre's native region id, a counter on each phone. A **Merge** treats "same id" as "same record", so a different region with the same small number on the two phones would be skipped as a duplicate, and its refs would point at the phone's unrelated region. **Replace** has no such problem.
Options, none chosen: (a) restore rows and leave them hidden (what happens today, so a restored region is unrecoverable from the UI); (b) list restored rows in Offline maps as "not downloaded", with a re-download from the stored centre, radius and zoom (new screens and new copy); (c) do not restore region rows, only the entries' own snapshots; (d) restore and re-download automatically (network and data use without asking); and for Merge, (e) give restored regions a new id on insert and rewrite the refs, or (f) leave regions out of Merge. I ship the generic id rule for regions (matches the dispatch's Merge rule verbatim) and say plainly it has the collision above.

## Pre-registration: tests and predictions (written before any code; pushed first)

Tests are Robolectric with a real Room database in a temp file and real files, through the service's public API and the real Settings screen. **Tests-first stubs:** the classes exist with every operation returning `Result.failure(UnsupportedOperationException(...))` ("unsupported, explicit", CLAUDE.md), so a test fails by assertion on the result and not by compile error. Predicted failure at that base, all by "expected success but was `UnsupportedOperationException`" unless stated:

| # | Test | Predicted at base |
|---|---|---|
| 1 | Round trip: seed phone A (every journal table has rows, four photo files), `backUp`, `restore(REPLACE)` into an empty phone B: every journal table's rows equal cell-for-cell, photo files equal byte-for-byte | fails: backup unsupported |
| 2 | Replace over a phone with its own data: the phone's rows are gone, the backup's are in; the phone's photo files that no restored row references are gone | fails |
| 3 | Merge: an id on both sides keeps the phone's copy (a changed field survives), an id only in the backup arrives, no owned row points at a missing record, `track_points` of an inserted track arrive with fresh ids, photo files arrive only for inserted rows | fails |
| 4 | Corrupt zip (truncated), and a zip with one flipped byte in a photo: `restore` fails, the live tables and photo files are byte-identical to before, and no scratch files remain | fails on "expected a failure with the approved reason": the stub also fails, so this one **may pass at base for the wrong reason** and I will check the failure's message names the cause, not "unsupported" |
| 5 | A rollback test: an insert made to fail mid-transaction (a spec entry naming a missing column) leaves the phone's tables and files unchanged | fails |
| 6 | Older backup: a real version-15 database built from `app/schemas/.../15.json` restores through the migration (`shownOnMap` present and false); newer: manifest `schemaVersion` 17 is refused with the reason logged | fails |
| 7 | Every table in the schema is classified journal or excluded | passes at base only if the classification exists; written against the stub list, so it fails until the list is filled |
| 8 | The schedule: off by default; `setEnabled(true)` with no folder stays off and yields the approved message; with a folder it turns on; the worker writes one file per run and deletes nothing | fails |
| 9 | The Settings screen (real `AvailabilityScreen`, Tools then Settings): the Backup section shows the approved labels; "Back up now" with a chosen file writes a zip and shows "Backup saved."; a failing backup shows "Couldn't save the backup."; the Automatic switch with no folder stays off and shows its message; "Restore from backup" shows the prompt with the title, the three paragraphs and Replace, Merge, Cancel, and Cancel changes nothing | fails: no such section |

Predicted counts: 15 to 30 new tests. The suite is expected to stay green apart from those. **Revert checks planned** (saved copy, compile errors checked, forward change confirmed after): drop the write-lock around the snapshot copy (test 1 stays green single-threaded, so that one needs its own concurrent-writer test, added); drop the hash comparison (test 4b must fail naming the mismatch); skip the rollback of moved-aside photos (test 5); null-out the "owner inserted" condition in Merge (test 3); remove the migration step (test 6). If a mechanism has no test that bites when reverted, I will say so and not claim it.

**Device-only, listed, not run** (as the dispatch says): a real backup to a chosen folder and a restore on the S22 (after a backup of the phone's own data); the schedule firing (WorkManager's timing is the OS's); a restore onto the tablet as the "new phone"; whether the system file picker's "Save" and "Open" behave with a cloud provider; and the API 26 to 29 snapshot path (Robolectric runs one SQLite, not the old ones).

---

# Results (appended after building; the sections above are unchanged)

## What landed

Commits on `journal-backup` (tests first on `journal-backup-wip`, `68fede12`; forward `fbdaf0c7`; merge `de2ae432` brought in the photo-export work `edb74209`, which had landed on the remote meanwhile). Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

- **Backup and restore:** `data/backup/RoomJournalBackup.kt` (snapshot, verify, Replace, Merge), `BackupArchive.kt` (format, staging, hashes), `PhotoFileJournal.kt` (the undoable photo step), `JournalTables.kt` (the table knowledge), behind the domain interface `domain/JournalBackup.kt`.
- **Schedule:** `domain/BackupSchedule.kt` (`RunScheduledBackupUseCase`, `backupFileName`), `data/repository/DataStoreBackupSchedulePreferences.kt`, `data/backup/ScheduledBackup.kt` (worker and WorkManager job), `data/backup/ContentResolverBackupFiles.kt` (SAF).
- **UI:** `ui/backup/BackupViewModel.kt`, `ui/backup/BackupSection.kt`, threaded into Tools, then Settings through `ui/availability/AvailabilitySettingsUi.kt` and `AvailabilityScreen.kt`; wiring in `AppContainer.kt`, `ForagerApplication.kt`, `MainActivity.kt`.
- **Build:** `ForagerDatabase.create` gained a `name` parameter; new `openForRestore` (no destructive fallback in any build); `SCHEMA_VERSION` constant (guarded by a test against the schema files and the database). `gradle/libs.versions.toml` and `app/build.gradle.kts`: **WorkManager was not a dependency; I added `androidx.work:work-runtime` 2.12.0 and `work-testing` 2.12.0, pinned**, from the `<release>` on Google's Maven read 2026-09-29.

## Verified premises, and one the dispatch did not have

- Every claim in the pre-registration held except the count: the journal tables are **thirteen** (six records, seven owned), not twelve as one sentence there says.
- **The screens do not observe the database.** No DAO returns a `Flow` (`grep Flow<` over `data/local/*Dao.kt` is empty); the ViewModels read once and hold (`CartographyViewModel.kt:90-94`, `MushroomLogViewModel.kt:247`, `TrackRecordingViewModel.kt:572,599`). A restore that wrote the database and stopped would leave every open screen showing the old data until the next launch. So `BackupViewModel` takes an `afterRestore` callback, and `MainActivity` passes the public loaders that exist: `CartographyViewModel.loadEntries`, `MushroomLogViewModel.loadEntries` and `loadGalleryPhotos`, `TrackRecordingViewModel.loadTracks` and `loadWaypoints`. **Not reloaded:** `AvailabilityViewModel`'s offline-region list (its loader is private, `AvailabilityViewModel.kt:923`), the Maps tab's records and highlights (loaded when the map is shown), and anything else I did not find. **Which screens refresh is a design question I settled the smallest way; see Decisions.**
- **A backup's zip is staged in full before anything is checked against the live phone**, so a restore needs room for the extracted archive as well as the final files; a nearly full phone fails at staging and touches nothing. There is no free-space check.

## Tests first, seen failing at base (`68fede12`, the stubs), and what passed anyway

69 tests ran: **63 failed, 6 passed.** Failure reasons read from the JUnit XML: 16 `UnsupportedOperationException: journal backup: not built`, 2 `...restore: not built`, 2 `backup files: not built`, 2 `backup schedule preferences: not built`, 22 `performScrollTo() failed` (no Backup section on the real screen), and the rest assertions on state the stub never sets (`expected:<AUTOMATIC_NEEDS_FOLDER> but was:<null>`, `expected:<Success> but was:<Failure>`, `List is empty` from a scheduler that recorded nothing).

The 6 that passed at base, each read:
1. `the default file name is forager-backup, the date, dot zip`: `backupFileName` is pure and was already real. A control, not a biting test.
2. `the automatic backup is off by default ... approved words`: the stub's defaults are the defaults; a control.
3. `an enabled setting with no folder is never scheduled`: passed because the stub scheduler did nothing. **It bit later** (revert `schedfolder`).
4. `a run with the setting off, or with no folder, writes nothing`, 5. `a folder that cannot be written is a failure`, and 6. `Cancel closes the prompt`: each passed **for the wrong reason**, because the stub fails everything or does nothing. I strengthened all three before building (they now assert a `BackupException` naming "off", "folder" and the prompt being up first) and they fail at the stub with the stub's `UnsupportedOperationException` or a null prompt.

## Revert checks (`/tmp/revert2.sh`: saves a copy before editing, restores from that copy, refuses results if the build log has a compile error, confirms the file equals its committed forward version)

All 14 builds had **0 compile errors**, every failure named its own edit, and every file was restored and confirmed.

| mechanism | one-line revert | the failure that named it |
|---|---|---|
| write lock during the snapshot copy | `runInTransaction` replaced by a plain block | `a writer started during the copy must still be waiting expected:<false> but was:<true>` |
| SHA-256 comparison | `if (false) throw` | `a changed photo: expected a failure, got Success(...)` |
| `integrity_check` | `if (false) throw` | `a damaged snapshot with a matching hash: expected a failure, got Success(...)` (Room's own open did **not** catch it, so this check is not redundant) |
| photo rollback on failure | `photos.rollback()` removed | `the phone's photo files are unchanged expected:<{photos/own-p.jpg=...}> but was:<{p...` (two tests) |
| Merge: only rows of an owner this merge inserted | condition removed | `UNIQUE constraint failed: log_entry_photos...` and `only the phone's own ref row for e1 ... expected:<1> but was:<2>` |
| migration on the scratch copy | `migrateOnScratchCopy` removed | `NOT NULL constraint failed: cartography_entries.shownOnMap` (two tests) |
| newer-than-app refusal | check disabled | `the reason is logged` (the restore was still refused, by the manifest-versus-database mismatch, but not for the reason the test names; that log assertion is what bites) |
| Merge drops a row that would dangle | `if (false)` | `gone-w had no record to point at expected:<0> but was:<1>` |
| old photo files removed after Replace | loop removed | `the phone's own photo file is gone` |
| unlisted or outside-the-folder entry refused | `?: continue` | `an entry named ../../evil.txt: expected a failure, got Success(...)` |
| automatic backup needs a folder (ViewModel) | `if (false)` | three tests, incl. both real-screen ones: `ToggleableState = 'Off'` |
| `afterRestore` after a successful restore | call removed | `after a restore that worked expected:<1> but was:<0>` |
| scheduler needs a folder | folder test removed | `expected null, but was:<WorkInfo{... state=ENQUEUED` |
| file opened truncating | `"wt"` to `"w"` | `expected:<2> but was:<100>` |

**Not revert-checked:** the checkpoint before the snapshot (best effort by design; a revert cannot fail a test), `PhotoFileJournal`'s same-bytes skip, and the approved copy strings (asserted as literals, so a reworded one fails, but I did not reword one to see).

## Full suite

`./gradlew :app:testDebugUnitTest` from a cleared `app/build/test-results`, `LC_ALL=C.UTF-8`, on the merged tree `de2ae432` (my forward commit merged with the photo-export work): **BUILD SUCCESSFUL in 3m 24s**. From the JUnit XML: **327 result files, none older than the run's start; 2659 tests, 0 failures, 0 errors, 24 skipped.** A previous full run on my tree before the photo-export merge: 2648 tests, 0 failures. The growth I authored is **70 tests in 7 new classes** (`JournalBackupTest` 20, `ScheduledBackupTest` 11, `ContentResolverBackupFilesTest` 2, `BackupViewModelTest` 15, `BackupSettingsScreenPortraitTest` 11, `BackupSettingsScreenShortLandscapeTest` 11); the photo-export tests are not mine. Above the pre-registered 15 to 30 because the screen tests run in two window shapes.

## Device-only, listed and not run

- A real backup to a folder and a restore on the S22, after a backup of the phone's own data.
- The **schedule firing** (WorkManager's timing, Doze and battery optimisation, are the operating system's), and a scheduled run's file appearing in the chosen folder.
- A restore onto the tablet as the "new phone", including a photo-heavy journal's time and disk use.
- The system pickers on the phone: the create-file picker offering `forager-backup-<date>.zip`, the folder picker, the open-file picker, with a cloud provider as the destination; `takePersistableUriPermission` and `DocumentsContract.createDocument` (`ContentResolverBackupFiles.kt`), which Robolectric has no provider for.
- The **API 26 to 29 snapshot path**: Robolectric runs one SQLite; the write-lock-and-copy design was chosen because `VACUUM INTO` is missing there, and nothing here ran an old one.
- What the Backup section looks like in the Tools drawer and the wide layout (`BackupSection` is tested in the compact drawer in two window shapes, not in the wide permanent drawer's call site).

## Decisions I made

1. **What "journal data" is:** thirteen tables; `planned_trips` and `cached_searches` are **not** restored (and, since the snapshot is the whole database, they are in the file but ignored). Consequence: a new phone does not get the old phone's planned trips. Alternative: include `planned_trips`. Ruling 3 B lists neither; the premise pulse calls them not journal data.
2. **Replace and Merge write through one Room transaction on the live database; the database file is never swapped and the app is not restarted.** Alternative: close, swap files, restart the process.
3. **The frequency default is Weekly** (the switch is off regardless). No default is ruled and the control needs a selected state.
4. **The chosen folder is shown by its own name** under "Backup folder" (data from the picker, not new copy). Without it the user cannot tell which folder is set.
5. **`RestoreReport`/`BackupReport` counts exist but are not shown**: no approved copy carries them, so the user sees only the five messages.
6. **A photo row whose file is missing on disk is left out of the archive, counted, logged, and the backup is still "Backup saved."** There is no approved copy for "saved, but N photos were missing", so the UI cannot say so. That presents a partial result as success (CLAUDE.md, Errors); the alternative is to fail the whole backup, which loses everything for one missing thumbnail. **Needs the owner's ruling.**
7. **Post-restore refresh** (above): the five loaders. Alternatives: restart the process, or make every ViewModel observe.
8. **Merge counts a dependent row of a skipped owner as neither inserted nor dropped**, and drops (counts, logs) a dependent row whose target is on neither phone. So an entry's "kept" snapshot of a since-deleted record is lost by a Merge but kept by a Replace.
9. **WorkManager job has no constraints** (none ruled): it runs on battery and on any network state, since it only writes a local file.

## Flags outside scope

- **WorkManager adds permissions to the merged manifest:** `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`, `ACCESS_NETWORK_STATE` (its own aar manifest declares them, plus `FOREGROUND_SERVICE`, which the app already declares); read from `work-runtime-2.12.0.aar` and the merged debug manifest. The privacy policy and store data-safety text that list permissions need to know. I did not remove them (`tools:node="remove"`), which would be a design choice.
- **A scheduled run that fails mid-write leaves a partial `forager-backup-<date>.zip` in the folder**, and `BackupFiles` has no delete by design. Restore refuses it (hash or zip error), but the folder holds a useless file. Retention is "one file per run, nothing deleted", so nothing prunes it, and a full disk fills the folder without bound.
- **A manual backup that fails after the file is created** leaves an empty or partial document the user chose the name of.
- **Restoring while a track is recording**: Replace deletes the twelve-plus tables' rows including the active track's points while the recorder is writing to them. Nothing blocks it. Unruled; needs a decision.
- **Restored offline regions: the stop above, plus** the ViewModel does not reload them.
- **The two legal documents** that say "Nothing is left behind" (`delete-data.md:24-28`, `privacy-policy.md:169-170`) are now false for a user who saves a backup to shared storage; the planner drafts them with the owner (out of scope).
- **`allowBackup=false`** is unchanged (`AndroidManifest.xml:79-100`); nothing here touches it.
- `RoomJournalBackupHooks` puts three test seams in production code (named for what they stop at). They default to nothing.
- A `pgrep` for other builds matched other sessions' shell loops; I checked `Gradle Test Executor` processes and free memory before every build, and waited twice for the photo-export coder's run (it sat idle for several minutes with no CPU before finishing).
