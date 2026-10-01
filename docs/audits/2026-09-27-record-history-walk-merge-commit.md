# 2026-09-27: RECORD.md history walk fails on one merge commit (planner error)

`check_record.py` (recovered from `git show 3896118:check_record.py`, since the kit was removed from `pre-main` by `e136330`) now reports **FAIL** on its history walk:

> commit 27938bc4b022aa8d6bd3f7ee3a00fded6ec9cdb7 ... touching RECORD.md has 2 parents; merge commits are out of scope for this walk

**Cause.** The planner wrote terminal `2026-09-27-64` while a merge of plan notes from `claude/docs-pr137-loose-ends-jnv47u` into `journal-redesign` was stopped on a conflict in `docs/plans/journal-redesign.md`, and then committed the conflict resolution and the terminal together as the merge commit `27938bc`. The walk checks each single-parent commit that changes `RECORD.md` for a byte-prefix extension; it does not evaluate merge commits, so it fails on any merge that changes `RECORD.md`. Earlier merges into `journal-redesign` (`8343c9e`, the addendum merges) did not touch `RECORD.md` and pass.

**Verification that the record itself is intact.** `RECORD.md` at `27938bc` begins byte for byte with `RECORD.md` at its first parent `eefce47` (658,758 bytes, extended to 662,803 by terminal `2026-09-27-64` only). The second parent's copy (`466034c`, the docs branch, 623,215 bytes) is an older prefix and contributes nothing. So the walk's rule, that each transition only appends, holds for this commit when read against the first parent.

**Not done.** The commit is not rewritten: it is pushed, and history is not rebased, amended or force-pushed here (CLAUDE.md, push before you tidy). The checker is not changed to accept merges. Every later run of the recovered checker will show this one FAIL; this note is the explanation, and any other history-walk failure is a new finding.

**Rule from here.** A `RECORD.md` entry is never written or committed during a merge. Merge first (plan and doc files only), commit the merge, then append the entry in its own single-parent commit.
