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
