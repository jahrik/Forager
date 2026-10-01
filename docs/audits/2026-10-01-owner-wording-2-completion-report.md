# Dispatch 2026-09-28-367: owner-wording-2, the last "ruled" phrasings and one test's name

Coder: Sonnet 5.5 (`claude-sonnet-5-5`), from the session's configuration, not checked against the running session.
Base: `origin/journal-redesign` at `46335728` when I started and when I finished (`git pull --no-rebase` found nothing new). Branch `owner-wording-2`, worktree `/home/zynergy-labs/Zynergy/forager-wt/trip-flag-size`. Nothing merged to `journal-redesign`.

## Landed
- `e42c6969`: the edits (six comments, one test rename).
- the next commit: `BackupSchedule.kt`'s citation reworded at the planner's request (no "rulings" label).
- the build log, the two classes' XML (`docs/audits/data/2026-10-01-owner-wording-2/`), this report and the README row.

## Part 2 first: did the owner choose Weekly? Yes.
- `docs/plans/journal-redesign.md:911-920` ("Journal backup and restore: third rulings (owner, 2026-09-29)"): the owner, verbatim, item 8: "weekly to start, with default off, let the user set the frequency from there". The planner's reading under it (:929-ish) is "The default frequency is Weekly."
- `RECORD.md`, record -127's note lists "Weekly is the default frequency" among the coder decisions put to the owner; record -133 ("the owner ruled on -127's stop and its decisions") carries "Weekly is the default frequency, and the schedule stays off by default" in its Changes.
- So the test name ("the default the owner ruled") was right in substance and `BackupSchedule.kt:14` ("has no ruled default") was wrong. The comment is corrected; the test is renamed to "...the default the owner chose".
- Not verified: the original coder dispatch (`prompts/preserved/2026-09-29-11.md`) says "5 C ... off by default" and "2 C: daily, weekly or monthly" but names no default; I did not open the -127 coder report.

## Each line, before and after
| Where | Before | After |
|---|---|---|
| `BackupSchedule.kt:13-15` | "[frequency] has no ruled default, so it starts at [BackupFrequency.WEEKLY]; the user changes it..." | "[frequency] starts at [BackupFrequency.WEEKLY], the owner's choice (the owner, 2026-09-29, item 8: "weekly to start, with default off, let the user set the frequency from there", `docs/plans/journal-redesign.md`); the user changes it..." (quoted words exact) |
| `EntryMapFrame.kt:8` | `...next photo?", ruled "Fit all kept records")` | `...next photo?", chose "Fit all kept records")` |
| `DrawerBackOverJournalTest.kt:165` | "behaviour ruled for after M1" | "behaviour decided for after M1" |
| `CompactToolsDrawerTest.kt:46` | "and ruled on in `docs/audits/...` section 5" | "and decided in ..." |
| `TrackSelfJoin.kt:37` | "the track-network candidate the owner ruled inside the retrace ruling" | "...the owner decided inside the retrace ruling" (same sentence as `PathHome.kt:77`, done in -365) |
| `CameraArrangement.kt:76` | "A fourth arrangement was ruled and then **withdrawn**" | "A fourth arrangement was decided and then **withdrawn**" (the record: `docs/audits/2026-09-19-camera-activity-window-report.md:175`, "The owner has ruled to add a fourth arrangement") |
| `BackupSettingsScreenTest.kt:358` (name) | `` `turning the automatic backup on leaves Weekly selected, the default the owner ruled` `` | `` `... the default the owner chose` `` (name only; body, assertions and `@Test` untouched) |

## Read and left (the dispatch's "every one reported")
- `AndroidBackupNotifier.kt:43` "Importance is DEFAULT, which nobody ruled": says no one decided it; not a decision of the owner's. Left.
- `ScheduledBackup.kt:69` "No constraints are added (none is ruled)": the absence of a decision. Left.
- `FanPlacement.kt:57` "is ruled on instead of fanning by default": a future decision about a layer not yet added. Left.
- Left as the dispatch said: "ruled out" (idiom, about ten lines); `CartographyEntry.kt:40` ("every candidate the user has ruled on", the app's user); `MapBubble.kt:81` ("the planner ruled"); every noun ("ruling", "rulings", "the retrace ruling", document names). I did not re-grep for "ruled" lines the planner's list did not name; the list is the planner's and I did not re-derive it.

## Counts
`git diff -U0 origin/journal-redesign -- app`: 7 files, 18 changed lines (10 added, 8 removed). Counted by dropping lines whose first non-blank token after `+`/`-` is `//`, `/*`, `*` or `*/`. One line remains for each side: the test function's name line, before and after, which is the rename (not a comment, by design). So Part 1 is 16 comment lines with 0 non-comment; Part 2 is the one renamed line.

## Build and classes
`:app:assembleDebug :app:compileDebugUnitTestKotlin` then the two classes, in one run: BUILD SUCCESSFUL, 0 `e:` lines. `BackupSettingsScreenTest.kt` runs under two classes:
- `BackupSettingsScreenPortraitTest`: 27 tests, 0 failures, 0 errors, 0 skipped.
- `BackupSettingsScreenShortLandscapeTest`: 27 tests, 0 failures, 0 errors, 0 skipped.
- Base counts: 27 and 27 in the saved XML of the -358 full suite (`docs/audits/data/2026-10-01-trip-flag-size/full-suite/`); I did not re-run the base. The renamed test appears in both XML files under its new name and passes in each (read from the XML).
- No full suite and no revert check: a rename and comments, as the dispatch decided; CI runs the suite on the merge.

## Four disclosures
- **Confirmed vs inferred:** confirmed by reading the record, the diff, the build and the XML. Inferred: that the three "left" lines describe no owner decision (read in place, not traced further).
- **Could not determine:** nothing open on the Weekly question.
- **Premises that were wrong:** the dispatch's premise that name and `BackupSchedule.kt:14` disagree was right; the comment was the wrong one, not the name.
- **Decided beyond scope:** none. The planner asked for the "rulings" label to be dropped from the new comment; done.

Nothing to check on a device.
