# Dispatch 2026-09-28-361: the fan-out's comments stop calling the owner's answers "rules"

Coder: Sonnet 5.5 (`claude-sonnet-5-5`), from the session's configuration, not checked against the running session.
Base: `origin/journal-redesign` at `19f159fd` when I started; it moved to `be04d42d` (a records-only device-note) and I merged it with `git pull --no-rebase` (merge `cfb1d091`, no conflict). Branch `fan-comment-wording`, worktree `/home/zynergy-labs/Zynergy/forager-wt/trip-flag-size`. Nothing merged to `journal-redesign`.

## What landed
- `d6c49445`: the comment edits (6 files).
- `3f17cc73`: compile log, test log and the three classes' XML under `docs/audits/data/2026-10-01-fan-comment-wording/`.
- `cfb1d091`: merge of `origin/journal-redesign`; `c1e79101`: first report and README row.
- Widening, after the owner's "Go ahead" relayed by the planner (the un-numbered -274 "rule" comments): the commit "The -274 comments say the owner's choice, not rule" (hash in `git log`), a second run's XML under `data/.../second-run/`, and this report updated.

## The diff is comments only
`git diff -U0 origin/journal-redesign -- app`, recounted after the widening: **8 files, 49 changed lines** (24 added, 25 removed). Before the widening it was 6 files, 41 lines. I counted by listing every added or removed line and dropping those whose first non-blank token is `//`, `/*`, `*` or `*/`: **0 remain**. That test would call a code line that happens to start with `*` a comment, which no line here is; the changed lines were also read in full (below). No code, test name, constant or assertion changed.

## Each comment, before and after
| Where | Before | After |
|---|---|---|
| `MarkerFanOut.kt:10` (not on the planner's list) | "the owner's rules in" | "the owner's choices in" |
| `MarkerFanOut.kt:32` | "(the owner's rule 3)" | "(the owner's choice, dispatch 2026-09-28-197)" |
| `MarkerFanOut.kt` `FAN_DURATION_MS` doc | "...animation speed to 250ms". It replaces his earlier rule 6, "Give it a .4s animation speed" (400). Only the duration changed: the easing, what the progress drives and the reduce-motion behaviour are as they were." | the dispatch's exact text: "The fan-out, and the fold-back, take this long. The owner, dispatch 2026-09-28-358 amendment -359: "And increase the animation speed to 250ms"." |
| `MapTapHandler.kt:49` | "(the owner's rule 4)" | "(the owner's choice, dispatch 2026-09-28-197)" |
| `MapTapHandler.kt:54-55` | "(a stack, rule 1) ... (rule 2)" | "(a stack) ... (the owner's choice, dispatch 2026-09-28-197)" |
| `MarkerFanOutState.kt:90` | "(the owner's rule 5)" | "(the owner's choice, dispatch 2026-09-28-197)" |
| `MarkerFanOutState.kt:97` | "supersedes rule 5's "fan before bubble"" | "supersedes the earlier "fan before bubble"" (amendment -255 fact kept) |
| `MapTapHandlerTest.kt:15` | "the owner's rules 1 to 5" | "the owner's choices" |
| `MapTapHandlerTest.kt:46`, `:106`, `:143` (not on list) | "Rules 1 and 2: a stack fans out." / "Rule 4: a tap on a fanned marker..." / "Rule 5: the folds." | "A stack fans out." / "A tap on a fanned marker..." / "The folds." |
| `MarkerFanOutGeometryTest.kt:13` | "the owner's rules 1 to 3" | "the owner's choices" |
| `MarkerFanOutGeometryTest.kt:32`, `:67`, `:97` (not on list) | "Rule 2: the ring." / "Rule 3: the spiral." / "Rule 1: what is a stack." | "The ring." / "The spiral." / "What is a stack." |
| `MarkerFanOutHostTest.kt:33-34` | "the owner's rules 5 and 6: "Give it a .4s animation speed", the system's animations off..." | "the owner's choices: "And increase the animation speed to 250ms" (dispatch 2026-09-28-358 amendment -359), the system's animations off..." |
| `MarkerFanOutHostTest.kt:113` (mine, from -359; not on list) | "Rule 6 was 0.4 s; the owner, dispatch -358 amendment -359: ..." | "The owner, dispatch -358 amendment -359: ..." |
| `MarkerFanOutHostTest.kt:202` (not on list) | "Rule 5: Back." | "Back." |
| **-274 widening** `FanReopenCoordinatorTest.kt:63` | "Superseded by the owner's rule, intent 2026-09-28-274: "Fold only if members change"." | "Superseded by the owner's choice, intent 2026-09-28-274: ..." |
| `MapTapHandlerTest.kt:183` | same sentence | same change |
| `FoldOnlyIfMembersChangeTest.kt:17` | "The owner's rule, verbatim: "Fold only if members change"." | "The owner's words, verbatim: ..." |
| `FoldOnlyIfMembersChangeTest.kt:181` | "One rule with "fold only if members change" (-274): a replaced style..." | "The same behaviour as "fold only if members change" (-274): a replaced style..." |

Where a bare label was removed from a section comment I did not add a dispatch cite, because the class comment already cites -197; this is a judgement call within "keep the meaning". No pronoun for the owner is used; the one "his" is gone.

## Checks
- `:app:compileDebugKotlin :app:compileDebugUnitTestKotlin`: BUILD SUCCESSFUL, 0 `e:` lines.
- First run (before the widening): `MarkerFanOutHostTest`, `MapTapHandlerTest`, `MarkerFanOutGeometryTest`: 11, 12 and 18 tests, 0 failures.
- Second run (after the widening, compile and tests in one invocation): 0 `e:` lines, BUILD SUCCESSFUL; those three classes plus `FanReopenCoordinatorTest` (4) and `FoldOnlyIfMembersChangeTest` (13): 58 tests, 0 failures, 0 errors, 0 skipped. Base counts: each file's `@Test` count at `19f159fd` is 11, 12, 18, 4 and 13, the same. (I compared `@Test` annotations in source, not a base test run.)
- **No full suite and no revert check: a decision of this dispatch, not an omission.** Nothing executable changed; CI runs the suite on the merge.
- Final grep `git grep -nE "[Rr]ules? [0-9]|rules [0-9]" -- app/src`, excluding the out-of-scope Understory and `CameraBands` hits, returns nothing.

## Four disclosures
- **Confirmed vs inferred:** confirmed by reading the changed lines, the count, the compile and the three classes. Inferred: that the section-label hits are the -197 numbered answers (they carry the same numbers as the list; I did not re-read `prompts/preserved/2026-09-29-39.md`).
- **Left, reported:** `FanReopenCoordinatorTest.kt:64` "which is the rule the device log showed folding a good reopen" (the old behaviour, not an owner answer); `FoldOnlyIfMembersChangeTest.kt:180` "The owner's ruling" (dispatch -279, the word is "ruling", not "rule"); `CameraOverlay.kt:24` "Owner's rule" and `JournalTabTest.kt:364` "The owner's rule, \"Follow until you touch it\"" (other features, outside the fan-out; I am unsure they are in the owner's intent and left them). `main/` has no -274 "rule" label (`SightingsMap.kt:784` and `MapTapHandler.kt:122-123` quote the words without it). No test NAME contains "rule" (grep of backticked function names under `ui/map`). Non-owner uses of "rule" were also left: `MarkerFanOut.kt:91` (an algorithm), `MapTapHandler.kt:125`, "32 dp rule"/"48 dp rule" in `MapTapHandlerPlacementTest.kt:33` and `MarkerFanOutGeometryTest.kt:102`, `:142`, and the JUnit `@Rule` and `RuleChain` identifiers.
- **Premises that were wrong:** the planner's list was incomplete: it missed `MarkerFanOut.kt:10` and the seven section-label comments above. The list otherwise matched (read at `18c7f301`, still true at `19f159fd`).
- **Decided beyond scope:** none beyond changing the extra numbered hits, which the planner confirmed in scope; the -274 widening was the owner's ("Go ahead", relayed by the planner). In `FoldOnlyIfMembersChangeTest.kt:181` I chose "The same behaviour as" for "One rule with"; say so if another wording is wanted.

Nothing to check on a device.
