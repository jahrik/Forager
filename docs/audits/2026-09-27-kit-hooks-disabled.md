# 2026-09-27: the Claude-kit hooks are disabled, then removed from `pre-main`

This note records that the owner turned off the kit's hooks on 2026-09-27, and
why. It describes what the two commits did. It does not judge whether the
change should stay.

## 1. The owner's words

The owner, in the session that wrote this note (2026-09-27), verbatim:

> I disabled the hooks on 2026-09-27, to work around weekly limits in vhilocal
> machine.

"vhilocal" is kept as written. The note reads it as the owner's local machine,
where the planner and coder sessions run. That reading is not confirmed.

## 2. What the commits did

Two owner commits, both made on the evening of 2026-09-26 Pacific time, which
is 2026-09-27 in UTC. Their messages carry different dates for that reason.

1. **`c5b2a10`**, "Disable Claude-kit hooks (owner, 2026-09-27)", at
   2026-09-27T06:32:09Z on branch `disable-kit-hooks`. One line added to
   `.claude/settings.json`: `"disableAllHooks": true`. The hooks and their
   config stay in the tree. It reached `main` through **PR #138**, merged at
   2026-09-27T06:42:45Z as `faf2f88` (parents `76905d4` and `c5b2a10`). PR
   #138's body is the unfilled template. Because `c5b2a10` was cut from
   `pre-main` after PR #137, that merge also carried `pre-main`'s history into
   `main` (326 files).
2. **`e136330`**, "CLAUDE.md and kit removal temp fix (owner, 2026-09-26)", at
   2026-09-27T06:58:44Z, pushed directly onto `pre-main` (its one parent is
   `3896118`, the PR #137 merge; it is not a merge). It deletes 29 files,
   9,351 lines: `.claude/settings.json`, every file under `.claude/hooks/`
   with their tests, `.claude/agents/coder.md` and `pulse.md`,
   `.claude/kit.json` and `kit.lock`, `check_kit.py`, `check_prompts.py`,
   `check_record.py`, `find_dispatches.py`, `launch_session.py`,
   `run_exercise.py`, `session_agents.py` and `update_worktree.py`. It also
   removes the "Roles and gates" section from the end of `CLAUDE.md`.

So the two trunks differ. On `main` the hooks exist and are switched off. On
`pre-main` they are gone. The message calls `e136330` a temp fix. No record
says when or how it would be undone.

## 3. What stops being enforced

Read from the deleted files' purpose as the earlier records describe them, not
tested after the change:

- The role, dispatch, history and device guards no longer run. Nothing
  mechanically keeps the planner read-only, preserves dispatches under
  `prompts/preserved/`, or refuses a merge into a protected branch that has no
  backup.
- `check_record.py`, `check_prompts.py` and `check_kit.py` are not in
  `pre-main`'s tree. `.github/` names none of them, so CI is unaffected. Any
  `RECORD.md` entry written from here on is checked by hand, or recovered with
  `git show 3896118:check_record.py`.

## 4. Not verified

- Whether the weekly limits were actually hit, and on what. That is the
  owner's account.
- Whether any session ran with hooks active after 06:32Z. Nothing in the
  repository records it.
