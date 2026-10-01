# Planner handoff: stopping point, 2026-09-29T00:46:06Z

**Written by:** the planner session that resumed stalled session `session_01FvDtK5LcShCXshLP7V54Bt`.

**Why:** the owner, verbatim: "Hang on. Set a place to stop, push everything and save it." Then "Stop them". Every agent is stopped. Every worktree was checked clean, with nothing unpushed. `journal-redesign` is at `908cabf` when this was written.

## Stages

### Closed today

Each closed stage has a terminal in `RECORD.md`. The last planner suite is **311 / 2518 / 0 / 0 / 24 at `08e9f13`**.

| Stage | Terminal |
|---|---|
| J8 | `-66` |
| Stage device check Part 1 | `-71` |
| Tablet sanity check | `-80` |
| J8 device check | `-81` |
| Save failures shown | `-82` |
| Map chrome at 80% | `-83` |
| Tablet photo viewer | `-89` |
| Night outline border | `-91` |
| J8 follow-ups | `-92` |
| Tablet planned trips | `-101` |
| Map-chrome device check | `-102` |
| Decorations band | `-107` |

### Open, stopped by the owner

1. **`-78`, the Part 1 layout fixes.**
   - Built and on `journal-redesign`: items 2 (portrait), 3, 4 and 5, and the owner's short-landscape reshape (the pill beside the bar). Items 6 and 9 are cleared.
   - **Not built:** `-109`, the owner's "1 A / 2 A / 3 D": the chip row goes to the search bar's end away from the cluster, the legend sits inboard of the cluster at 270, and item 8 is unchanged.
   - Worktree `forager-wt/layout-fixes` is clean at `08e9f13`. Progress note `-108`; the relaunch note is `-110`.
2. **`-106`, offline regions: protect, then fix** (owner: "Option A. Protect then fix.").
   - **Nothing is built.** Worktree `forager-wt/offline-safety` is clean, and the relaunch note is `-111`.
   - Data safety: today's start-up error deletes nothing. But `MapLibreOfflineMapRepository.kt:190` would prune every region row on an empty read, and the obvious fix would trigger that (`docs/audits/2026-09-28-offline-regions-startup-pulse.md`).
   - **Do not fix the start-up error without Part 1's protection first.**
3. **`-94`, planned trips on the S22.**
   - **Reproduced,** on `device-trips` at `f35e815`:
     - with no search, the flags are absent and `region` is null;
     - after a recent search, both flags draw.
   - **Two test trips are still on the S22:** A "Trip 1" 2026-09-28 and B "Trip 2" 2026-09-30. Their ids are in the run record and `device-evidence/2026-09-28-planned-trips/trips-created.txt`.
   - Still to do: delete them through the Trip Planner, read `planned_trips` back as 0, restore settings, write the verdicts, merge `device-trips`, and write the terminal. Relaunch note `-112`.

### Queued

- **`-97`: planned trips drawn before a search** (owner: "Option A for the fix"). Unblocked by `-94`'s reproduction. It waits behind `-78`, because both edit `AvailabilityCompactMapUi.kt`.
- **`-104`: the map-chrome device check's follow-ups** (owner "1 A 2 A"). It waits behind `-78`.
- **J6, the tablet layout.** The rulings are in the plan's "J6 rulings" section: list-detail, Photo Gallery removed, the narrow map fixed in J6. It runs alongside stage device check Part 2 (not yet written). The tablet sanity check is done, so J6 may be dispatched.
- **Stage device check Part 2 on the S22.** Not yet written. It should also cover every stage's device-only list since, the chips' re-measure (J8 follow-ups removed their shadow), and the owner-judged looks: the J8 colours, the night outline border, and the reshape's thumb reach.
- **Then the single Journal PR.**

## Waiting on the owner

1. **Coder model.** The owner asked for Sonnet 5.5. The Agent tool's "sonnet" choice resolved to `claude-sonnet-5` (read from the agents' logs), which is not 5.5. The planner had said "Sonnet 5.5" without checking. Options put to the owner:
   - **A:** `model: claude-sonnet-5-5` in `.claude/agents/coder.md` (it takes effect at session start);
   - **B:** the default subagent model in `/config`.
2. **PR #141,** the agent instructions and CLAUDE.md's "Roles and gates", open for the owner to merge.
   - `journal-redesign` deletes `.claude/` (`e136330`), so the Journal PR's merge will need a decision on whether `.claude/` stays.

## Devices

- **The S22** (`R5CT321008R`): build `1.0.1457+gb358a4aa`, Forager in focus, `user_rotation` 0, `accelerometer_rotation` 0 (read at handoff). Trips A and B are still present.
- **The tablet** (`R52T506412L`, SM-X800): build `1.0.1416+gd7cc9f5b`. DEVICE CHECK items and one photo are left there on purpose (owner "3 A"). "Stay Awake", the 5-minute timeout and the CAMERA grant are the owner's settings; leave them.

## For whoever resumes

- **Every dispatch names its own worktree.** `-94` did not, and its coder created a branch in the planner's own checkout. That made the `coder` and `pulse` agent types unavailable for the rest of the session. The planner's checkout is back on its own branch.
- In the session that wrote this, the coder type was missing, so coders ran as general-purpose agents with the coder's rules in the prompt.
- **Never run a suite in a checkout an agent is reading.**
- Wait for builds with the pattern `'[G]radleWrapperMain|[G]radleWorkerMain'`. The brackets stop the pattern matching itself.
- **A push refused by the permission system is the owner's decision.** It is never retried through another agent or route.
