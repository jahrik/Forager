# 2026-09-27: gaps left open because the weekly limit was hit

This note lists record and verification work that was due after PR #137
merged and was left undone. The owner ruled on each one in the session that
wrote this note, on branch `claude/docs-pr137-loose-ends-jnv47u`:

> Leave all those and record them as gaps left by the weekly limit being hit.

The cause is the owner's account. The same limit is the reason given in
`2026-09-27-kit-hooks-disabled.md`. Nothing below has been done. Each item is
a gap, not a finding that the work is unnecessary.

## The gaps

1. **No merge entry for PR #138 into `main`.** `faf2f88` (parents `76905d4`
   and `c5b2a10`, merged 2026-09-27T06:42:45Z) is newer than `76905d4`
   (#119, merge entry `2026-09-26-24`). Under
   `2026-09-26-recordkeeping-protocol-shift.md` §2 every merge into `main`
   after `af12a69` gets one. No pre-merge backup of `main` at `76905d4` is on
   record either. Whether one exists outside the repository is unknown.
2. **The B2 index row still reads "not yet merged".** The
   `2026-09-27-landscape-b2-completion-report.md` row in `README.md` was
   written before the merge. No follow-up row records that #137 merged
   into `pre-main` as `3896118`. Merge entry `2026-09-27-42` records it in
   `RECORD.md`.
3. **`Merged-by` on merge entry `2026-09-27-42` is inferred.** It reads
   `coder preserved/2026-09-27-11.md`, taken from terminal `2026-09-27-41`'s
   plan. GitHub's `merged_by` is the account the owner and the coders share,
   and the backup `INDEX.md` line on the owner's machine was not read. If the
   owner merged #137, the correct value is `owner`.
4. **The nine B2 device items have not been run.** They are listed in
   terminal `2026-09-27-41`, Observed (7). The owner ruled they are
   owner-run after the merge.
5. **Terminal `2026-09-27-41`'s hand-back checks are unrecorded.** They are
   the CI run on `37fa01d`, the merge-parent check, and the two
   checkout-update dry runs. It is also unrecorded whether the checkouts were
   updated. Merge entry `2026-09-27-42`'s Notes already name these. The
   merge parents alone were re-read there from git, as `47638bb` and
   `37fa01d`.

## Closing a gap

Close each gap with its own later record: a merge entry, an index row, a
correcting note, or a device-check report. Don't edit this note or the
entries it names.
