# F5 completion report: backups without recent searches; GPX exports cleaned from the cache (dispatch 2026-09-28-216)

**Coder session.** The launch prompt asks the coder to confirm the model it runs on. That confirmation, and what could and could not be established, is in the hand-back message to the planner and the owner. Model identifiers are kept out of repository files in this environment.
**Branch:** `privacy-fixes`, worktree `/home/zynergy-labs/Zynergy/forager-wt/privacy-fixes`, cut from `origin/journal-redesign` at `8f260834`. That commit contains the launch base `445b292b` (`git merge-base --is-ancestor`). `git diff --stat 445b292b..8f260834` touches only `RECORD.md`, `docs/plans/journal-redesign.md` and `prompts/preserved/`, so the code is the base's.
**Governing files, read in full:** `prompts/preserved/2026-09-29-48.md` (the dispatch) and `prompts/preserved/2026-09-29-50.md` (the launch prompt), both quoted verbatim at the end of this report. The dispatch's map: `docs/plans/journal-redesign.md:1242-1244` ("After L1, and the chrome colour", items 2 and 3), and `RECORD.md` entry 2026-09-28-212 (L1's findings), whose two relevant notes are quoted in the premise table below. The intent is `RECORD.md` 2026-09-28-216. No planner message has reached this session, so none is quoted.
**`CLAUDE.md` at the base:** no conflict with the dispatch found. The launch prompt disapplies a `CLAUDE.md` line reading "a main session in this repository is the planner". That line is not in `CLAUDE.md` at this base (`grep -c` gives 0), so there is nothing to disapply.

## Pre-registration (written and pushed before building or observing)

### Premises checked at the base (`8f260834`, code identical to `445b292b`)

| Premise (dispatch, or L1's note in -212) | Checked at | Result |
|---|---|---|
| The backup is a whole-database snapshot (`RoomJournalBackup.kt:149-176`) | `RoomJournalBackup.kt:149-174` (`takeSnapshot`): the database file and its WAL are copied under the write lock (`:164-165`), then the copy is folded (`:168-173`). `doBackUp` zips that copy (`:107-108`, `:140`). | holds (the function ends at `:174`, and `:176` starts the restore section) |
| `:162`'s "DELETE … WHERE 0" is a no-op | `RoomJournalBackup.kt:162`: `DELETE FROM cached_searches WHERE 0` runs on the **live** database inside the transaction, and the comment at `:160-161` says it only takes the write lock. `WHERE 0` matches no row. | holds; left unchanged |
| Restore (Replace and Merge) must never write the phone's `cached_searches` | Replace deletes and inserts only `JournalTables.journal` (`RoomJournalBackup.kt:239-242`, `:250-251`). Merge reads only journal specs (`:321`, `:346`, `:362`). `cached_searches` is not in `journal` (`JournalTables.kt:42-92`). | **already true at the base.** See finding 1. |
| `JournalTables.kt:95-97` calls the table "not journal data" | `JournalTables.kt:95-97`: `"cached_searches" to "a rebuildable cache of network results, not journal data"`. The guard at `JournalBackupTest.kt:798-803` (base numbering) requires every schema table to be journal or excluded. | holds |
| Both backups go through this snapshot | `AppContainer.kt:208` builds the one `RoomJournalBackup`. The manual backup calls it at `BackupViewModel.kt:240`, and the scheduled one at `BackupSchedule.kt:127`. | holds: one change covers both |
| Android's own backup does not carry the database | `AndroidManifest.xml:134`, `android:allowBackup="false"` | holds (context only) |
| `TrackGpxExporter` writes into `cacheDir/tracks` (`:78`) and nothing deletes the file | `TrackGpxExporter.kt:78`. `write` (`:46-60`) only does `mkdirs` and `writeText`. `git grep cacheDir` in `app/src/main` finds no delete of `tracks/`. The only other cache user is the backup's scratch folder (`AppContainer.kt:213`). | holds |
| Every export goes through `write` | `TrackExportPanel.kt:264` (in `exportAndShareTrack`, `:254-266`) is the only caller. It is reached through `shareTrackGpx` (`:247-252`) from the row's Share (`TrackExportPanel.kt:187`) and from the details sheet's Share (`RecordDetailsSheet.kt:373`). | holds: a clean-up inside `write` runs before every export |
| A sharper, safe signal than one hour (the dispatch's stop) | `TrackExportPanel.kt:265`: `context.startActivity(Intent.createChooser(shareGpxIntent(context, file), "Share track"))`. There is no result, no chosen-component `IntentSender`, and no callback, so the code has no signal that the receiving app has finished reading. | **no sharper signal.** The planner's one-hour reading stands, and this is not a stop. |
| App start has a place for off-main clean-up | `ForagerApplication.kt:48-56` (`onCreate`). `sweepOrphanedCaptures` (`:97-104`) launches its file walk on `applicationScope` (`Dispatchers.IO`, `:38`). | holds; the new clean-up follows that pattern |

### Findings from the premise check (before building)

1. **Restore already never writes `cached_searches`.** The dispatch asks for "Replace and Merge of an old backup that has searches leave the phone's own searches untouched" as a test pushed failing. That test cannot fail at the base, because Replace and Merge walk `JournalTables.journal` only (the rows above). I predict both restore tests **pass at the base**. They are guards, not tests-first, and a mutation check (M1 below) will show that they can fail. No restore code changes.
2. **"At app start" reaches past the intent's scope line.** `RECORD.md` -216 gives "Scope boundary: data/backup and TrackGpxExporter". App start is `ForagerApplication.onCreate`, so that file changes too, by one new function and its call. The dispatch governs.
3. **The model check** is reported in the hand-back (see the header).

### What will be built, and where

- `RoomJournalBackup.kt`: a new private function `leaveOutExcludedTables(snapshot: File)`, called in `doBackUp` straight after `takeSnapshot(snapshot)` (`:108`). It opens the **scratch copy** read-write and, in one transaction, deletes every row of each table `JournalTables.excluded` names. Today that is `cached_searches` alone. It runs before the copy is integrity-checked, described in the manifest and zipped. The live database is not opened for writing, and `:162` is unchanged.
- `JournalTables.kt:94`: the `excluded` doc comment says a backup leaves these rows out and a restore never reads them. The reason string is unchanged.
- `TrackGpxExporter.kt`: a new function `deleteStaleExports(): Int` deletes the `.gpx` files in its folder that were last modified more than one hour ago, and returns the count. A failed delete is logged through an `ErrorLog` (a new constructor parameter whose default writes to `android.util.Log`, so existing constructions are unchanged), never silently. `write` calls it first.
- `ForagerApplication.kt`: a new private function `deleteStaleGpxExports()`, called from `onCreate` after `sweepOrphanedCaptures`. It runs `TrackGpxExporter.forContext(this).deleteStaleExports()` on `applicationScope` and logs the count at INFO when it is not zero, as the capture sweep does.

### Predictions and pass conditions

The tests are written against the base API, so they compile at the base. Predicted: 0 `e:` lines in the log.

| # | Test (file:line as written) | At the base | Passes when |
|---|---|---|---|
| T1 | `JournalBackupTest.kt:798` "a backup made with searches on the phone holds no cached_searches rows, and the phone keeps its searches" | **fails**: `the backup's snapshot holds no cached_searches rows expected:<0> but was:<2>`, because the snapshot is a byte copy (`RoomJournalBackup.kt:164-165`) and nothing deletes on it | the snapshot has 0 rows, the waypoints are still in it, and the phone's two searches are unchanged column for column |
| T2 | `JournalBackupTest.kt:815` "Replace from an older backup that holds searches…" | **passes** (finding 1) | the phone's searches are unchanged and the backup's waypoint is restored |
| T3 | `JournalBackupTest.kt:830` "Merge of an older backup that holds searches…" | **passes** (finding 1) | the same, with the backup's waypoint merged in beside the phone's |
| T4 | `TrackGpxExporterTest.kt:133` "before it writes, an export deletes the exports more than an hour old…" (61 and 59 minutes) | **fails**: `an export more than an hour old is deleted before the next one is written`, because `write` never deletes (`TrackGpxExporter.kt:46-60`) | the 61-minute export is gone, the 59-minute one is kept, and the new one is written |
| T5 | `RecordDetailsSheetTest.kt:347` "a Share first deletes GPX exports more than an hour old…" (a real touch on the row's Share) | **fails**: `the export more than an hour old is deleted before the new one is written` | the 2-hour export is gone, the 10-minute one is kept, and the folder holds that one and the file just shared |
| T6 | `ForagerApplicationGpxCacheTest.kt:26` "app start deletes GPX exports more than an hour old…" (a second `onCreate` with two exports in the cache) | **fails** after its 5 s poll: `the export more than an hour old is deleted at app start`, because `onCreate` (`ForagerApplication.kt:48-56`) has no GPX clean-up. A crash in the second `onCreate` would be a wrong-reason failure and is checked for. | the 61-minute export is gone, and the 59-minute one is still there 300 ms later |

T2 and T3 each check first that the older backup really holds a `cached_searches` row (`backupHoldingSearches`, `JournalBackupTest.kt:854`), so they cannot pass on a backup that holds none.

### Revert and mutation checks (planned; each restores from a copy saved before its edit, never from git)

| Check | Edit | Predicted failures |
|---|---|---|
| R1 | remove the `leaveOutExcludedTables(snapshot)` call | T1 only: `expected:<0> but was:<2>` |
| R2 | remove the `deleteStaleExports()` call in `write` | T4 and T5 (the stale file still exists); T6 passes |
| R3 | remove the `deleteStaleGpxExports()` call in `onCreate` | T6 only |
| R4 | the age test becomes "any age" (`>= 0`) | T4 and T5 on "within the hour is kept"; T6 probably on the same, subject to the 300 ms wait |
| R5 | the threshold becomes two hours | T4 and T6 (the 61-minute file is kept). T5 is predicted to **pass**: its stale file is set 2 hours old and is a few seconds older by the time of the Share, so a two-hour threshold still deletes it. |
| R6 | clear the **live** database instead of the copy (the same delete, on `database.openHelper.writableDatabase`, before `takeSnapshot`) | T1 on `the phone keeps its searches` (both sides empty) |
| M1 | add `TableSpec("cached_searches", Kind.RECORD, listOf("key"))` to `JournalTables.journal` (a mutation, since the restore has no forward change to revert) | T2 and T3, each on the searches' dump. Replace swaps the phone's two rows for the backup's `key-1`, and Merge adds `key-1`. The table-list guard also fails (`nothing is both`). |

A revert run whose log has an `e:` line, or no `BUILD` line, is refused. After each restore, the file is compared byte for byte with the saved forward copy.

### Full suite (predicted)

On the branch after merging `origin/journal-redesign`, whose tip `f34b73e6` the planner recorded at 3217 / 0 / 0 / 24 (-225): **3223 tests, 0 failures, 24 skipped**, which is 3217 plus the 6 above. That holds only if nothing else lands meanwhile, and anything else that lands is accounted for from the diff. The results directory is cleared first, and the counts come from the JUnit XML, with every file newer than the run's start.

### Observation, not a gate: search bytes left in the archive

`DELETE` removes rows but, unless SQLite's secure-delete is on, can leave their bytes in the file's free space. After the build, I will look (with a throwaway test, not committed) at whether the archive's `forager.db` bytes still contain (a) the key of a search the snapshot clearing deleted, and (b) the key of a search deleted on the live database before the backup, as an eviction does. This runs under Robolectric's SQLite, which may not be built like a phone's. The result is reported either way. It is not a pass condition, and any follow-up is the owner's.

### Push plan

This pre-registration and the tests go to `privacy-fixes-wip` first, as they are broken work (failing tests). The build goes to `journal-redesign` once its tests pass. Before each Gradle run: no Java Gradle process (`pgrep -af '^\S*java .*([G]radleWrapperMain|[G]radleWorkerMain)'` empty), `MemAvailable` at least 2.5 GB, and at least 2048 MB free on disk. D58 is checked on the diff and the commit messages before each push.

## Tests first, at the base (observed after the pre-registration was pushed at `5cffbb10`)

The run was on `5cffbb10` (the base code with the six tests), with the classes `JournalBackupTest`, `TrackGpxExporterTest`, `RecordDetailsSheetTest` and `ForagerApplicationGpxCacheTest`. The results directory was cleared first. The log has 0 `e:` lines and a `BUILD FAILED` line, which comes from the test failures. There are 4 XML files, none older than the run's start. **71 tests, 4 failed, 0 skipped**, exactly the predicted four, each on its predicted message:

- T1: `the backup's snapshot holds no cached_searches rows expected:<0> but was:<2>`
- T4: `an export more than an hour old is deleted before the next one is written`
- T5: `the export more than an hour old is deleted before the new one is written`
- T6: `the export more than an hour old is deleted at app start`. This came after the second `onCreate` ran without an exception, so it is not the wrong-reason failure the pre-registration guarded against.

T2 and T3 **passed at the base**, as predicted (finding 1). They are guards.

**M1**, run at the base and before any build, puts `cached_searches` into `JournalTables.journal` as a record keyed by `key`. It ran `JournalBackupTest` (42 tests) with 0 `e:` lines, 1 XML file and none stale. Result: **4 failed**:
- T2 (Replace): `the phone's searches are exactly as they were, and none of the backup's arrived`. The dump shows the phone's two searches replaced by the backup's single `key=key-1` row.
- T3 (Merge): the same message. The dump holds 3 rows, the phone's two plus the backup's `key-1`.
- the table-list guard: `the journal list, as ruling 3 B names it`.
- T1, still failing, as it does at the base with no fix; M1 cannot touch it.

The file was restored from the copy saved before the edit, is byte-identical to it, and has its exclusion line present. So T2 and T3 do detect a restore that writes `cached_searches`.

## Correction to the pre-registration

The premise table cites the chooser call at `TrackExportPanel.kt:265`. It is at **`:266`**, both at the base (`git show 445b292b:…` gives `:266`) and now. `:265` is the closing brace of the `withContext` block before it. The claim is unchanged. The pre-registration is left as pushed.

## What landed

These are on `privacy-fixes`, pushed to `privacy-fixes-wip` as the work went, and then pushed to `journal-redesign`. The pushed head is in the hand-back.

| Commit | What |
|---|---|
| `5cffbb10` | Pre-registration and the six tests, pushed before any build or run |
| `dd93caf5` | Tests-first results at the base, and M1 |
| `981bff8d` | The build (below) |
| `947b2d9d` | Merge of `origin/journal-redesign` at `b457bc75` (C1 and records). No conflict, and no file shared with F5. |
| this report's commit, and a merge of the later record-only commits | the rest of the report |

The build, with paths under `app/src/main/java/com/zynergylabs/forager/app/` and lines as at `947b2d9d`:
- `data/backup/RoomJournalBackup.kt:110`: `doBackUp` calls `leaveOutExcludedTables(snapshot)` straight after `takeSnapshot(snapshot)` (`:109`). The function (`:178-196`) opens the **scratch copy** read-write and, in one transaction, runs `DELETE FROM` on each table `JournalTables.excluded` names, which today is `cached_searches` alone. The live database is not opened for writing, and the no-op at `:162` is unchanged. The class doc says the same at `:50-51`.
- `data/backup/JournalTables.kt:94-97`: the `excluded` doc says a backup leaves these rows out (the copy only) and a restore never reads them. No restore code changed: Replace and Merge already walk `JournalTables.journal` alone.
- `export/TrackGpxExporter.kt`:
  - `deleteStaleExports()` (`:82-94`) deletes every `.gpx` file in the folder whose last-modified time is more than an hour old (`STALE_EXPORT_AGE_MILLIS`, `:108`), and returns the count. It uses `Files.deleteIfExists`, so a failure comes with its reason, and a failed delete is logged through `errorLog` (`:90`).
  - `write` calls it first (`:58`). `errorLog` is a new constructor parameter (`:36`) whose default writes to `android.util.Log`, so no existing construction changes.
- `ForagerApplication.kt:57`: `onCreate` calls `deleteStaleGpxExports()` (`:113-118`), which runs `TrackGpxExporter.forContext(this).deleteStaleExports()` on `applicationScope` (`Dispatchers.IO`) and logs the count at INFO when it is not zero, as the capture sweep beside it does.

## Evidence

**The build's own classes,** on `981bff8d`: 71 tests, 0 failed, 0 `e:` lines, 4 XML files, none stale. This is the same 71 that ran at the base, where 4 failed.

**Revert checks,** from `/tmp/f5-revert.py`. Each one saved a copy of the file before its edit, refused to cite results unless the log had 0 `e:` lines and a `BUILD` line, read only XML newer than the run's start, and restored from the saved copy, never from git. Each was then confirmed byte-identical to the forward copy, with its forward-change text present. All six had 0 `e:` lines, restored identical, and had the marker present, and `git status --short` was empty after the set. Every failure is one its own edit can produce, and each matches the pre-registered prediction.

| Check | Edit | Result |
|---|---|---|
| R1 | the `leaveOutExcludedTables(snapshot)` call removed | 42 run, **1 failed**: T1, `the backup's snapshot holds no cached_searches rows expected:<0> but was:<2>` |
| R2 | the `deleteStaleExports()` call in `write` removed | 29 run, **2 failed**: T4, `an export more than an hour old is deleted before the next one is written`; T5, `the export more than an hour old is deleted before the new one is written`. T6 passed. |
| R3 | the `deleteStaleGpxExports()` call in `onCreate` removed | 29 run, **1 failed**: T6, `the export more than an hour old is deleted at app start` |
| R4 | the age test replaced by `false`, so any age is deleted | 29 run, **3 failed**, all on the kept file: T4 `an export within the hour is kept: …`, T5 and T6 `the export within the hour is kept` |
| R5 | the threshold set to two hours | 29 run, **2 failed**: T4 and T6 on the 61-minute file. T5 passed, as predicted, because its 2-hour file is a few seconds older than two hours by the time of the Share. |
| R6 | the live database cleared (`DELETE FROM cached_searches` on `database.openHelper.writableDatabase` before the snapshot) instead of the copy | 42 run, **1 failed**: T1, `the phone keeps its searches: the live table is never cleared`, `but was:<{cached_searches=[]}>` |
| M1 | (at the base, above) `cached_searches` made journal data | T2 and T3 failed on the phone's searches, and the table-list guard failed |

**Observation (not a gate): search bytes in the archive.** A throwaway Robolectric test was copied in, run once, and deleted. It was never committed, and `git status` was clean afterwards. It seeded the full journal plus two searches with unique strings, deleted one of them on the live database (as an eviction does), and made a backup with the build in place. Robolectric's SQLite reported `sqlite_version=3.44.3` and **`PRAGMA secure_delete` = 1** on the live connection. The archive's `forager.db` (225,280 bytes):
- did not contain the cleared search's key or label (`false`, `false`);
- did not contain the evicted search's key or label (`false`, `false`);
- did contain a journal string, the positive control (`Chanterelles under the oaks`: `true`).

So under Robolectric no search bytes survive, because secure-delete is on. Whether a phone's SQLite has it on is **not verified here** (device item 3 below).

## Full suite

On `947b2d9d` (F5 merged with `journal-redesign` at `b457bc75`), the machine was idle by the check: no Gradle Java process, 2857 MB available, 11662 MB free on disk. The results directory was cleared first. The counts come from the JUnit XML: **395 files, none older than the run's start; 0 `e:` lines; 3223 tests, 0 failures, 0 errors, 24 skipped**, in 263 s.

That is exactly the prediction. The planner's suite at `fb6bf9ce` was 3217 / 0 / 0 / 24 in 394 files, and `git diff --stat fb6bf9ce b457bc75 -- app/` is empty. F5 adds 6 tests and 1 class: 3217 + 6 = 3223, and 394 + 1 = 395. The commits that landed on `journal-redesign` after `b457bc75` and were merged for the push change nothing under `app/` (`git diff --stat <merge-base> origin/journal-redesign -- app/` is empty), so this run covers the pushed code.

## Not tested

- **The failed-delete path** in `deleteStaleExports` (`TrackGpxExporter.kt:89-91`). No test makes a file undeletable, so the log line has never been seen.
- **A failure inside `leaveOutExcludedTables`**, for example the copy failing to open read-write. By reading, it throws inside `doBackUp`'s `try`, and `attempt` (`RoomJournalBackup.kt:90-100`) logs it and returns a `BackupException`. No test exercises it.
- **Files other than `.gpx`** in `cache/tracks` are left alone. The app writes none, and no test pins this.
- **The exact one-hour boundary.** The tests use 59 and 61 minutes (and 10 and 120 on the Share path) against the real clock, not 60 minutes exactly.
- **The scheduled backup** takes the same code by construction (`BackupSchedule.kt:127` into the same `RoomJournalBackup`), but no test in this dispatch drives it.
- **A cold process start.** The start test drives a second `onCreate` on the app Robolectric already started, because nothing can be put in the cache before Robolectric's own start.
- **A narrow race, by reasoning only:** the start clean-up reads a stale file's time, then an export overwrites that same file (same track) before the clean-up deletes it. It would need a Share within milliseconds of start, of a track whose last export is more than an hour old.

## Device-only list (for the S22-B data/backup/restore session, cheapest first)

Each step has a pass condition and the evidence to keep. Steps 3 and 8 are observations, not gates.

1. **A new backup holds no searches.** With recent searches present, make a manual backup, copy the `.zip` off, unzip it, and run `sqlite3 forager.db "SELECT COUNT(*) FROM cached_searches; SELECT COUNT(*) FROM waypoints;"`. *Pass:* 0 searches, and a non-zero journal count. *Evidence:* the command output.
2. **The phone keeps its searches.** The recent-searches list is the same before and after that backup. *Pass:* same entries, same order. *Evidence:* two screenshots.
3. *(Observation)* **No search bytes in the archive.** Run `strings forager.db | grep -c '<a searched place's latitude as the app stores it, or a filter label only a search uses>'` on the same file. *Record:* the count. 0 means the phone's SQLite overwrote the deleted rows, which Robolectric's does. Anything above 0 goes to the owner (see Flags, item 3).
4. **A pre-F5 backup restores without touching searches.** Take a backup made before F5 that holds searches (Part 2's, if one exists; otherwise note that none exists). Restore it with Merge, then with Replace, on "DEVICE CHECK" data. *Pass:* after each, the recent-searches list is the phone's own, unchanged. *Evidence:* screenshots before and after.
5. **A fresh export is shared and readable.** Share a track, then run `adb shell run-as com.zynergylabs.forager.app ls -l cache/tracks`. *Pass:* the file is listed, and the receiving app opens it. *Evidence:* the `ls -l` output and the receiving app's screen.
6. **A stale export goes before the next one.** More than an hour after step 5 (real time; do not change the clock), share another track and run the `ls -l` again. *Pass:* step 5's file is gone and the new one is there. *Evidence:* both listings with timestamps.
7. **A stale export goes at start.** With an export more than an hour old in `cache/tracks`, force-stop and reopen the app. *Pass:* the file is gone within seconds, and logcat shows `ForagerApplication: Deleted 1 GPX export(s) more than an hour old from the cache.` *Evidence:* the listing and the logcat line.
8. *(Observation)* **A receiving app that reads late.** If any receiving app reads the file more than an hour after the share (a draft left open, say), record which app and what it shows. *Record:* the app and its behaviour.

## Decisions I made

1. **The cleared set is `JournalTables.excluded`,** not `cached_searches` written out by name. I took "Keep its exclusion consistent" literally: a table excluded from restore is also left out of backups. It is the same today, since there is one excluded table. The alternative was to name the one table.
2. **The clearing is its own function, in its own read-write open of the copy,** called after `takeSnapshot`, not folded into `takeSnapshot`'s fold step. That follows `CLAUDE.md`'s "new capability is a new function". The effect is the same.
3. **Only `.gpx` files** in `cache/tracks` are deleted.
4. **"Older than one hour"** is measured from the file's last-modified time against `System.currentTimeMillis()`, and a file is deleted when it is strictly older than 60 minutes. Re-sharing the same track overwrites its file, so the time restarts then.
5. **Failed deletes are logged** through an `ErrorLog` constructor parameter that defaults to `android.util.Log`, rather than calling `Log.w` directly. `TrackGpxExporterTest` is a plain JVM test, where a direct `Log.w` would throw. The start clean-up logs its count at INFO, like the capture sweep.
6. **The start test drives a second `onCreate`**, because files cannot be put in the cache before Robolectric's own start. The alternative, a test `Application` subclass, would need `ForagerApplication` made `open` for tests.
7. **The failing tests and the pre-registration went to `privacy-fixes-wip` first,** as broken work, following the journal-backup dispatch's practice. The finished work went to `journal-redesign`.
8. **`ForagerApplication.kt` changed,** although the intent's scope line names only data/backup and `TrackGpxExporter`. The dispatch's "at app start" needs it, and the dispatch governs.
9. **Model identifiers are kept out of this file.** The environment forbids them in repository artifacts, so the model check is in the hand-back.

## Flags outside scope

1. **A wrong premise:** restore already never wrote `cached_searches`. The dispatch's "Replace and Merge … leave the phone's own searches untouched" could not be pushed failing. Those two tests are guards, and M1 shows they can fail.
2. **The start and the Share clean-up are the planner's one-hour reading.** The code has no sharper signal: the share is `startActivity(createChooser(…))` with no result (`TrackExportPanel.kt:266`). An app that reads the file more than an hour later will find it gone (device item 8).
3. **Bytes as well as rows.** "A backup contains no `cached_searches` rows" holds by test. Whether the deleted rows' **bytes** are gone depends on the SQLite's secure-delete setting. It is on under Robolectric, and unverified on a phone (device item 3). The same applies to searches evicted earlier on the live database, and to any deleted journal record, since the backup is a copy of the file.

   If a phone shows residue, the options are the owner's:
   - (a) `PRAGMA secure_delete = ON` on the copy's connection before the clearing, which covers only the rows cleared there;
   - (b) `VACUUM` on the copy after the clearing, which rewrites the file and drops all free-page residue, a wider change;
   - (c) accept it and word the disclosure to match.

   This matters for the legal-drafts change the dispatch says follows.
4. **The `DELETE FROM cached_searches WHERE 0`** at `RoomJournalBackup.kt:162` reads like clearing but only takes the write lock. It is left unchanged, and the new function's doc says so.
5. **The launch prompt disapplies a `CLAUDE.md` line** ("a main session in this repository is the planner") that is not in `CLAUDE.md` at this base.

## Not merged

Merging to `main` is not authorised and was not done. No device was used.

## Appendix: the governing files, verbatim

### `prompts/preserved/2026-09-29-48.md` (the dispatch)

~~~~markdown
# Dispatch 2026-09-28-216 (F5): backups without recent searches; GPX exports cleaned from the cache

**The owner, verbatim:** "2 A / 3 A". These answer the planner's questions after L1:
- 2: "Backups include your last five searches, with their coordinates… A. Leave searches out of backups."
- 3: "Exported GPX files are never cleaned out of the app's cache. A. Delete the cached copy after sharing."

They are recorded in `docs/plans/journal-redesign.md`, "After L1, and the chrome colour", items 2 and 3, with L1's findings in record -212.

**Base:** `origin/journal-redesign` at the commit named at launch.

**Worktree:** `/home/zynergy-labs/Zynergy/forager-wt/privacy-fixes`, branch `privacy-fixes`. **Work only there.**

## Build
1. **Searches out of backups.** A backup file contains no `cached_searches` rows.
   - The backup is a whole-database snapshot (RoomJournalBackup.kt:149-176 at a0cf46ed's base). Clear the table in the **snapshot copy** before it is zipped. Never clear it on the live database.
   - Note that `:162`'s "DELETE … WHERE 0" is a no-op.
   - **Restore** (Replace and Merge) never writes the phone's `cached_searches`. An older backup that does contain the table is read without restoring it.
   - `JournalTables.kt:95-97` already calls the table "not journal data". Keep its exclusion consistent.
2. **GPX exports cleaned from the cache.** `TrackGpxExporter` writes into `cacheDir/tracks` (TrackGpxExporter.kt:78), and nothing deletes the file.
   - The receiving app may still be reading the shared file when the share sheet closes, so the file cannot be deleted at once.
   - **The planner's reading, told to the owner:** delete exported GPX files older than one hour, at app start and before each new export.
   - **Stop** if the code shows a sharper, safe signal, such as the share's completion, and report it instead.
3. Nothing else changes.

## Tests
- Tests first, pushed failing:
  - a backup made with searches on the phone contains no `cached_searches` rows, and the phone keeps its searches;
  - Replace and Merge of an old backup that has searches leave the phone's own searches untouched;
  - an export older than an hour is removed at start and before the next export, and a fresh one is kept.
- Revert checks from saved copies; the full suite at 0 failures.
- A report at `docs/audits/2026-09-29-privacy-fixes-completion-report.md`, with a device-only list.
- Push to journal-redesign. No device. Merge is not authorised.
- The machine checks as always. Never run `--stop`.
- **The legal docs follow:** once this lands, `legal-drafts` drops its two disclosures. The planner handles that.
~~~~

### `prompts/preserved/2026-09-29-50.md` (the launch prompt)

~~~~markdown
# Coder session: F5, backups without recent searches; GPX cache cleaned (dispatch 2026-09-28-216)

## Your role and rules

The owner opened this session to act as the **coder** for one dispatch. It is not the planner. CLAUDE.md's line that "a main session in this repository is the planner" does not apply here: the owner assigned this session the coder role. The planner is another session, and it writes the record.

**First,** confirm the model you are running on. The owner sets `/model claude-sonnet-5-5`. If you cannot tell, say so in your hand-back.

**The coder's rules**, which bind you with `CLAUDE.md`:
- **You execute the dispatch and make no design decisions.** An ambiguity or gap is a stop: name it, lay out the options, and hand back. Stopping is compliant; guessing is not.
- **Read `CLAUDE.md` at your base first.** Where it conflicts with the dispatch, stop and report the conflict. The dispatch files govern over this prompt. Quote them, and every planner message, verbatim in your report.
- **Verify every premise at your base** before relying on it. A wrong premise is a finding.
- **Never touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`.** Your record is your report in `docs/audits/`: add a new "Resumed" section, and never rewrite what is already there.
- **Pre-register** your predictions and pass conditions, with file:line, and push them before building or observing.
- **Tests fail first, for the stated reason.** A tests-first test passing at base where you predicted failure is a stop.
- **Revert checks:** restore from a copy you saved before editing, never from git. Refuse results when the build log has compile errors. Confirm the forward change is present afterwards.
- **The full suite** runs from a cleared results directory. Counts come from the JUnit XML, with every file newer than the run's start.
- **Two failed fixes on one symptom:** get data, not a third guess.
- **Scope is a wall.** Cite a file and line, or say it is unverified.
- **Push before you tidy.** Merge with `git pull --no-rebase`; never rebase, reset or amend unpushed work.
- **D58:** before each push, check the diff and commit messages for the three phrases in forager-forecast `docs/planning/DECISIONS.md` row D58, Decision column. Never write them anywhere.
- **Sharing the machine:** before each Gradle run, check that no Java Gradle process is running (`pgrep -af '^\S*java .*([G]radleWrapperMain|[G]radleWorkerMain)'`; a looser pattern matches other sessions' shell loops) and that 2.5 GB of memory is available. Wait if either fails. Other coders build here. **Never run `./gradlew --stop`**: it stops every build on the machine.
- **If the permission system refuses a push or command, stop and hand back.** Do not try another route.
- **Work only in your named worktree,** always by absolute path. Never check out or change branches in any other checkout, including the one this session opened in.

## Hand-back

When you finish or stop:
1. Push your report.
2. Message the planner session: use `ListAgents`, find the session named **"# Planner session"**, ref **`[4b12e2]`** (the ref can change; the name is the address), and `SendMessage` it your hand-back. The hand-back gives what landed with hashes, the verification, the evidence (tests first, revert checks, suite counts), what was not tested, the device-only list, and the sections **Decisions I made** and **Flags outside scope**.
3. Also end your turn with the same hand-back, so the owner sees it.

## The dispatch

**What governs:** read in full, on `origin/journal-redesign`:
- **`prompts/preserved/2026-09-29-48.md`**, the dispatch, which governs, with the owner's words verbatim;
- everything it names as your map.

**Worktree:** create `/home/zynergy-labs/Zynergy/forager-wt/privacy-fixes` (branch `privacy-fixes`) from `origin/journal-redesign`. **Work only there.**

**In brief** (the dispatch governs): Backups contain no `cached_searches` rows: cleared in the snapshot copy, never on the live database, and never restored. Exported GPX files older than an hour are deleted at app start and before each new export.

**Base:** **`445b292b`**.

**Other coders build here too:** pull with --no-rebase and merge. Keep to the machine-sharing rule, including at least 2048 MB free on disk. Push to `journal-redesign`; broken work goes on `privacy-fixes-wip`. No device. Merge is not authorised.
~~~~
