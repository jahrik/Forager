# Known bugs

The list the QA philosophy's A6 reads: "Known bugs are listed and judged before release, never found first by users". Each promotion into main names the bugs it ships with, from this list. Started 2026-10-16 (RECORD -1138), in the form the owner chose: "Bugs: open / resolved / hold".

## The fields

- **Status** (the owner's three):
  - **Open:** confirmed, not fixed.
  - **Hold:** confirmed, parked on purpose. The reason is given.
  - **Resolved:** fixed. Says where the fix was confirmed, and in which commit or release.
- **Where:**
  - **Live:** in the Play build people have.
  - **New:** only on pre-main so far.

  The QA philosophy's shipping rule reads this field (question 19, -1097): a listed bug already in the live app may ship, but a new serious one fails a promotion.
- **Serious:** yes or no. Serious means a crash, lost records, a wrong position, direction or time, or anything that reads as identifying a mushroom. These are the fail cases in section 4.
- **Seen:** where, when, and by whom. Give a RECORD ID wherever there is one.

Experiments on dev are not listed here. They reach this list only when they are carried onto pre-main.

## The list

| # | What the user meets | Status | Where | Serious | Seen |
|---|---|---|---|---|---|
| 1 | The website's delete-data page still lists "any crash reports" among what uninstalling deletes. Release builds keep none since -930, so the line is untrue. | Open | Live (website) | No | Noted by the planner, RECORD -1066; in the 2026-10-15 tidying inventory |
| 2 | Not a user-facing bug: one automated test, `MushroomLogViewModelWiringTest`, fails intermittently, on about 6% of CI runs, in a group the owner parked. It is listed so that a red run isn't mistaken for a new bug. | Hold (owner: until development cools) | Tests only | No | -348, -349; most recently -1093 |

**Still to add:** the owner's own triage list of bugs found in the field and in closed testing. It isn't held in a file this planner could read. The owner is asked to supply it, or to confirm the list starts from these two.
