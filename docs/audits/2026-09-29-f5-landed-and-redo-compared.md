# F5, the landed build and the redo compared (planner, 2026-09-29)

**Asked for.** The owner, verbatim: "Run a diff on the old F5 and new F5 and document the procedural differences into the audit folder."

**What is compared.** Two builds of the same dispatch, F5 (2026-09-28-216): backups without recent searches, and GPX exports cleaned from the cache. The dispatch is `prompts/preserved/2026-09-29-48.md`, and the launch prompt is `prompts/preserved/2026-09-29-50.md`. Both coders worked from those files.

| | Landed F5 | Redo |
|---|---|---|
| Its own commits | `5cffbb10` pre-registration and tests; `dd93caf5` tests-first results and M1; `981bff8d` build; `770fcf6f` report | `76b71b41` pre-registration; `eea15726` tests first; `9b54e5a2` build; `29d009ae` VACUUM dropped; `426786f5` report |
| Where it is | Branch `privacy-fixes`, no longer on the remote. Merged into `journal-redesign` by `947b2d9d`, `e954208b` and `2fa093ef` | Its own branch, head `426786f5`. Not merged anywhere |
| Base | `8f260834`. Its app code is the launch base's: `git diff 445b292b 8f260834 -- app` is empty | `445b292b`, the launch prompt's base |
| Report | `docs/audits/2026-09-29-privacy-fixes-completion-report.md`, the dispatch's name | A report in `docs/audits/` at `426786f5`, under another name, since the landed F5 holds the dispatch's |
| Commit times (PDT) | 13:05 to 13:19 | 13:27 to 13:46 |
| Attribution | The Co-Authored-By trailer on its four commits names the model its session ran on. That is not the model the launch prompt sets | The trailer on its five commits names the model the launch prompt sets |
| Why it exists | The dispatch | The owner's instruction, as the redo reports it: redo F5 on a new branch so the attribution is true |

**How it was compared:**
- **The code:** `git diff 981bff8d 426786f5 -- app`. Both builds start from the same app code. `981bff8d` is the landed build before its merges brought in other sessions' work, so the diff holds only the two F5s' differences.
- **The reports and prompts:** both completion reports, the dispatch and the launch prompt, read in full.
- **D58:** this planner's check was run over each build's own commits, diffs and messages.

Line numbers are at `981bff8d` for the landed build and at `426786f5` for the redo. `K/` is `app/src/main/java/com/zynergylabs/forager/app/`.

## 1. What a user gets: the same

Both builds do what the dispatch asks, in the same way as far as a user can see:
- **Searches out of backups:** a backup empties `cached_searches` on its snapshot copy, straight after the snapshot is taken. The live database is never written. Restore is unchanged, because it never read that table.
- **GPX cleanup:** `.gpx` files in `cacheDir/tracks` last written more than one hour ago are deleted first thing in `write`, and at app start, off the main thread.
- **Nothing sharper than one hour:** both found no share-completion signal (the chooser at `TrackExportPanel.kt:266` returns nothing), so both built the hour.

## 2. Procedure, rule by rule

| Rule (source) | Landed F5 | Redo |
|---|---|---|
| Confirm the model (launch prompt, "First") | Reported as unconfirmed, in the hand-back only. The report keeps model identifiers out of repository files, citing the environment's rule | The report's title, header and body name the model (11 lines), and so do its branch and report names. It says the model is the session's configured one, "not independently confirmed" |
| Read `CLAUDE.md` at the base (launch prompt) | Recorded: no conflict, and the line the prompt disapplies is not there (`grep -c` gives 0) | Not mentioned in the report |
| Quote the dispatch files and every planner message verbatim (launch prompt) | Both files appended in full. The report states that no planner message reached the session | Quotes the owner's "2 A / 3 A", the owner's instruction as relayed, and the planner's cancel message. The dispatch files are not quoted |
| Worktree and branch `privacy-fixes` (dispatch) | Yes | A new worktree and branch, on the owner's instruction as it reports it |
| Base `445b292b` (launch prompt) | Cut at `8f260834`. The report shows the app code is identical | `445b292b` |
| Verify every premise (launch prompt) | A premise table of nine rows, each with file:line and a result | Premises in prose, with file:line |
| The wrong premise: restore already never writes `cached_searches` | Found, and flagged as a wrong premise. The two restore tests became guards, and a mutation at the base (M1, making the table journal data) showed before any build that they can fail | Found. The two restore tests were predicted as guards, and revert checks after the build (R3a, R3b: Replace or Merge copies the table) showed they can fail |
| Pre-register, and push it before building (launch prompt) | Pre-registration and the six tests in one commit, `5cffbb10`, pushed to `privacy-fixes-wip` before any build | Pre-registration alone (`76b71b41`), then the tests (`eea15726`) |
| Tests fail first, for the stated reason (launch prompt) | 71 run, 4 failed, each on its predicted message; 0 compile errors; 4 XML files, none stale | 51 run, 4 failed, each on its predicted message; no compile errors |
| Revert checks from saved copies (launch prompt) | Six (R1 to R6) plus M1, each predicted in advance. Each file was restored byte-identical to its saved copy with the forward change present, and `git status` was clean afterwards. Includes two threshold mutations (any age; two hours) and clearing the live database instead of the copy | Six (R1 twice, R2, R3a, R3b, R4, R5), restored and compared with `cmp` and `git diff`. No threshold mutation, and no live-database check |
| A failed prediction | None | R2: removing the VACUUM was predicted to fail the byte test, and nothing failed. The report records it as a finding |
| "Nothing else changes"; no design decisions (dispatch, launch prompt) | Built only what the dispatch names. Leftover bytes in the file were left as an observation, with options (a) to (c) for the owner | Its build commit `9b54e5a2` added a `VACUUM` the dispatch does not name. It was removed in `29d009ae` once R2's prediction failed |
| The full suite, from a cleared results directory (launch prompt) | 3223 / 0 / 0 / 24 in 395 XML files, none stale, in one run, on the build merged with the then-current `journal-redesign`. That matches its prediction exactly. The planner repeated it at `06b394b9` (-233) | 3145 / 0 / 0 / 24 in 379 XML files, on the second run. The first run had one failure, in `LeavingTheJournalFixesTest`, which then passed three times alone and on the rerun. The count is at the older base, without the work that landed on `journal-redesign` since |
| Push to `journal-redesign`, broken work to `privacy-fixes-wip` (dispatch, launch prompt) | Yes | Its own branch only |
| The report at the dispatch's path (dispatch) | Yes | Another name |
| Device-only list (dispatch) | Eight steps, cheapest first, each with a pass condition and the evidence to keep, two marked as observations | Three items, without pass conditions or evidence |
| Not tested | Seven items | Three items |
| D58 (launch prompt) | Clean over its own commits, and its report states the check | Clean over its own commits. Its report does not mention the check |
| Hand-back | To the previous planner (-232) | To this planner, after the role moved |

## 3. The code, where the two differ

| Area | Landed F5 | Redo |
|---|---|---|
| What the backup clears | Every table `JournalTables.excluded` names, today `cached_searches` alone, in one transaction (`K/data/backup/RoomJournalBackup.kt:110`, `:186-196`) | `cached_searches` by name, in one statement, with no transaction (`RoomJournalBackup.kt:109`, `:184-187`) |
| Documentation of the clearing | The class doc and the `JournalTables.excluded` doc (`JournalTables.kt:95`) both say a backup leaves these rows out and a restore never reads them | Neither is updated. The function's doc (`RoomJournalBackup.kt:180-181`) states that Android's SQLite defaults `secure_delete` on. In the same sentence it says this was probed only under Robolectric, not on a device. The landed F5 treats the same question as open (device item 3) |
| A GPX file that cannot be deleted | `Files.deleteIfExists`, so a failure comes with its reason. Each failure is logged through an injected `ErrorLog` (`K/export/TrackGpxExporter.kt:36`, `:88-90`), on the export path and at start alike | `File.delete()`, which gives no reason. It returns `StaleSweep(found, deleted)` (`TrackGpxExporter.kt:71-78`). App start logs a shortfall (`K/ForagerApplication.kt:116`), but `write` discards the result (`TrackGpxExporter.kt:47`). **So a failed delete during an export is not logged.** That runs against `CLAUDE.md`, "Errors and failure paths": no fallback that is not logged when it fires |
| The age constant | `private const val STALE_EXPORT_AGE_MILLIS` (`TrackGpxExporter.kt:108`) | `const val MAX_EXPORT_AGE_MILLIS`, public (`TrackGpxExporter.kt:89`) |
| The constructor | Gains a defaulted `errorLog` parameter, so no existing call changes | Unchanged |

## 4. The tests, where the two differ

| Case | Landed F5 (6 tests) | Redo (7 tests) |
|---|---|---|
| A backup holds no search rows, and the phone keeps its searches | T1 | Test 1 |
| No search text left in the snapshot's bytes | An observation only, from a throwaway test that was never committed. Search bytes were absent under Robolectric, with `secure_delete` = 1 | A committed test (test 2). Its own report shows it cannot tell `DELETE` alone from `DELETE` plus `VACUUM` under Robolectric (R2) |
| Replace and Merge of an older backup leave the phone's searches | T2, T3, as guards | Tests 3 and 4, as guards, over schema 16 and 17 backups |
| `write` deletes a 61-minute export and keeps a 59-minute one | T4 | Test 5 |
| A Share through the real UI deletes a stale export first | T5, a real touch on the row's Share (`RecordDetailsSheetTest`) | None |
| A non-`.gpx` file in the folder is left alone | None. Listed under "Not tested" | Test 6 |
| App start deletes a stale export | T6 (`ForagerApplicationGpxCacheTest`, a second `onCreate`) | Test 7 (its own start test) |

## 5. For the owner's choice

Both builds give users the same behaviour. They cannot both be merged, because they change the same code. The choice between them is the owner's. The facts that bear on it, from the sections above:
- **The landed F5:**
  - it is on `journal-redesign` with the planner's suite at the current base, and it is Part 3's base;
  - it logs a failed delete on both paths;
  - it has a real-UI test of the Share path;
  - its report documents each rule the launch prompt sets;
  - its trailer is not the model the launch prompt sets.
- **The redo:**
  - its trailer is the model the launch prompt sets;
  - it adds a byte test and a non-`.gpx` guard;
  - a failed delete during an export goes unlogged;
  - it clears without a transaction;
  - it states an unverified platform default in a code comment;
  - it sits at the older base. Taking it would need the landed code removed, a merge onto the current `journal-redesign`, the suite rerun, and Part 3's base moved.
