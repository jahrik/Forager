# Evidence for dispatch 2026-09-28-312 (map-residuals)

Every run recorded here was made by the **earlier** -312 coder session (transcript
`706b8f15-a065-56d6-a46a-67ea2d07af30`, Sonnet 5.5), which lost its API connection at
2026-10-01T01:48Z before it reported. The files were in `/tmp` and in that session's
transcript only. A resumed session copied them here at 2026-10-01T02:09Z without re-running
anything. Nothing in this folder was produced by the resumed session.

| File | What it is |
| --- | --- |
| `full-suite-and-assemble.log` | `/tmp/full.log`: `./gradlew :app:testDebugUnitTest :app:assembleDebug --continue` on `520dd863` (the merge of `journal-redesign` `f8f3b9f5`). Started 01:39:32Z, ends `BUILD SUCCESSFUL in 5m 16s`, `done 0`, 0 `e:` lines. |
| `full-suite-xml.tgz` | The 404 JUnit XML files of that run, all stamped 01:44:48Z: 3262 tests, 0 failures, 0 errors, 27 skipped. The results folder had been deleted before the run started. |
| `part-a-red-and-green.tgz` | Part A. `map-res-red.log` and `map-res-xml-red/`: the six fix files put back to `1df2ee45` (tests and seam, no fix), 0 `e:` lines, 6 of 10 `MapRecordsDeletedNotDrawnTest` tests red. `map-res-green.log` and `map-res-xml-green/`: the fix restored from a saved copy, 17 classes, 0 failures. |
| `wiring-revert-onPhotoDeleted.log` | `/tmp/rv.log` of the second wiring revert (the `onPhotoDeleted` line removed). The first revert's log (`onFindDeleted` removed) was overwritten by this one; its output survives only in `earlier-session-run-outputs.txt`. |
| `part-b-runs-b1-b9.tgz` | Part B's nine Gradle logs, `b1.log` to `b9.log`. All have 0 `e:` lines. The XML each run produced was overwritten by the next run and is not kept. |
| `earlier-session-run-outputs.txt` | The commands that session ran and the output each returned, copied verbatim from its transcript: the red and green runs, both wiring reverts, the focus diagnostics and the focus stack trace (`part-b-focus-trace`). This is the only surviving copy of the trace. |

Not kept: `/tmp/map-res-saved/` (the saved copies of the six fix files; they equal the
committed files at `1ea640d5`), and the first compile-failed diagnostic run (01:32:44Z,
one `e:` line, `Unresolved reference 'getOrNull'`, in the session's own probe code).
