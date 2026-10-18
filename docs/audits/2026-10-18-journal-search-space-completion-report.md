# Journal search room in landscape: completion report (dispatch 2026-10-18-08 Amendment 3)

Hazel (app coder), 2026-10-18, written 22:20Z. RECORD -1320 (the owner: "Option 2"), which supersedes -1318's "screens
without a map behave as today" for the Journal in landscape. Branch `journal-search-space`, cut from origin/pre-main
`8c90626f` (PR #260 merged), as PR #261 into `pre-main`. **Not merged.**

**Status: built, tests updated, the revert check done. The full CI result is in the table below. No Stop was hit:**
- The Offline maps file (`AvailabilityOfflineMapsUi.kt`) is untouched. The reserve is made in the scaffold.
- No test was weakened. One existing claim was replaced, on the ruling (see Tests).

## What landed

| Commit | What |
| --- | --- |
| `598cd918` | The reserve in `AvailabilityCompactScaffold.kt`, the -1244 test's claim replaced, and the new -1320 tests |
| `3b2f46b7` | Test order only: each -1320 reveal test checks the room after the bounds, so a build without the room fails on what moved, not on a missing tag |
| (this commit) | This report and its index row |

- **`journalSearchRoom`** (`AvailabilityCompactScaffold.kt`) is the one place the reserve is decided. It applies when
  the window is landscape (`railBeside`), the tab is Journal, and the map is not fullscreen. Its value is the pill's own
  landscape height, the same value passed to `SearchEntryBar` as `landscapeHeight`. In every other case it is `null`,
  meaning no reserve.
- **How the room is held.** A `Box` wraps the pill's `AnimatedVisibility` with `heightIn(min = room)` and
  `clipToBounds`. The pill slides in and out of it vertically. While the room is held, the expand and shrink are left
  out, so nothing under the room follows the pill. The height is a floor, so a search notice under the pill can still
  add height.
- **The room is there on every Journal screen, including with an entry open** (the planner's ruling on my question).
  The `Box` sits outside the `journalSearchHeld` check, so opening or closing an entry moves nothing either. **The owner
  should see this at the phone check:** an open entry or editor in landscape now has the empty strip at the top as
  well.
- Portrait is unchanged, because the Journal's pill never hides there (`AvailabilityCompactScaffold.kt:697`, `:1073`).
  Other tabs are unchanged.
- Measured under Robolectric (823x384): the room is 36 dp tall, 0 to 36 at the top, 743 dp wide (the rail takes the
  rest). The pill is 0-36 inside it. The Offline maps map is at 52-372 x 84-384 before and after the reveal.

## Tests

All in `AvailabilityScreenJournalShortWindowTest`, through the real `AvailabilityScreen` at `w823dp-h384dp-land`.

- **-1244's slide test: the claim is replaced, not weakened** (the planner's ruling).
  - Its checks that the pill is part way in, part way out, at its place, and gone at the end are unchanged.
  - Its three checks that the row below follows the pill are replaced by "the row has not moved", at the end of the
    reveal, part way in and part way out. These are now equality checks.
  - The replaced assertions are quoted word for word in the test's KDoc.
- **New: the reveal moves nothing below the switch line**, on Entries, All, Finds, Tracks, Waypoints and Offline maps
  at ROTATION_90, and Offline maps at ROTATION_270.
  - Every tagged node from the switch line down keeps the same bounds, within 0.5 dp, at three points: part way
    through the slide (4 frames in), revealed, and hidden again. Between 12 and 20 nodes per screen were compared.
  - On Offline maps the map's top and height don't change, and the pill doesn't overlap it.
  - The pill lies inside the room, clear of the switch line.
- **New: switching screens with the pill hidden doesn't move the switch line.** The line's top and bottom stay the
  same from Entries through All, Finds, Tracks, Waypoints, Offline maps and back to All.
- **New: the room stays while an entry is open.** It keeps the same top and height. The entry being open is a positive
  control.
- The 🔍 icon is touched with real coordinate touches. Chips and the switch are reached by semantic clicks, which only
  get the test to a screen, as the class's own rule allows.

**Filtered CI 39168299870 at `598cd918`:** green, 90 tests, 0 failures, across
`AvailabilityScreenJournalShortWindowTest` (39), `LeavingTheJournalFixesTest` (32), `DrawerBackOverJournalTest` (11)
and `LandscapeOfflinePickerTest` (8).

## Revert check

The edit is one line: `val journalSearchRoom: Dp? = null`, which brings back the resize exactly as it was. I made the
branch from the committed forward head and restored the file from a copy saved before editing. Afterwards `cmp`
confirmed the forward file was intact. In each revert run's log, every compile step reports `BUILD SUCCESSFUL`, and
there are no `e: ` lines and no failed compile task. Only the test task failed.

| Check | Branch / commit | Tests at | CI run | Result |
| --- | --- | --- | --- | --- |
| R4 | `journal-search-space-revert-1` `81522170` | `598cd918` | 39168866696 | 9 of 39 failed; 8 of those fail at the room's tag lookup |
| R4b | `journal-search-space-revert-2` `aa6fad23` | `3b2f46b7` | 39170164095 | 9 of 39 failed, on what moved |

- **R4b:** each of the 7 reveal tests failed on what moved: "…, part way in: the same tagged nodes below the switch
  line expected:<[compact-navigation-rail, …". Without the room, the switch line starts at the top of the window and
  moves down as the pill expands, so the set of nodes at or below it changes. The -1244 test failed with "RECORD -1320:
  revealed, the row has not moved expected:<0.0> but was:<36.0>", which is the 36 dp resize. The entry-open test fails
  at the room lookup, because it measures only the room.
- **The chip-switch test passed under the revert,** as it must. Without any room nothing jumps between chips either.
  It guards against a reserve on some screens only, not against the reserve being removed.
- **R4,** on the earlier test order, failed the same 9 tests. 8 of them failed at the room's tag lookup and never
  reached their bounds checks, so I reordered the checks (`3b2f46b7`) and ran R4b.
- The removed expand and shrink are not covered by a test. Inside the held room, an expand would not move anything
  either. It only changes how the pill is uncovered (a clip growing) compared with a plain slide. The slide is what the
  dispatch describes, so I kept it.

## Full CI

| Run | Head | Result |
| --- | --- | --- |
| 39168872192 (pull_request) | `598cd918` | green: 5,131 tests, 0 failures, 0 errors, 24 skipped (JUnit XML, 690 files) |
| 39170164409 (pull_request) | `3b2f46b7` | 1 failure: `MushroomLogViewModelWiringTest` "a find deleted through the Journal's ViewModel leaves the Maps tab's records". This is the parked intermittent test, row 2 of `docs/process/known-bugs.md`, about 6% of runs. The commit changed six lines of one Journal test file. Reported, not touched. |
| FULL_CI_PENDING | this commit | |

## Not verified

- **On a phone:** the slide, whether the strip reads as intended, and the Offline maps map holding still without a
  stutter. Robolectric has no real insets. Device-only.
- **The room's height against the real pill on a phone.** The room uses the pill's own `landscapeHeight`, and under
  Robolectric the two match at 36 dp. A search notice can still make the pill taller than the room, and then the
  content moves by the notice's height. That only happens in an error state.
- **The landscape tablet:** not tested here. It uses the same scaffold path (`railBeside`).

## Phone steps (S26 Ultra, then the S22 and the tablet)

1. **Landscape, Journal, each screen.** Open Journal → Entries, then Records with each chip (All, Finds, Tracks,
   Waypoints, Offline maps). Pass: an empty strip at the top, the same height on every screen. The switch line stays
   still when you change chips.
2. **🔍 on each screen.** Tap 🔍, then tap it again. Pass: the bar slides down into the strip and back up. Nothing
   below it moves.
3. **Offline maps.** Tap 🔍, then tap it again. Pass: the map doesn't move, resize or stutter.
4. **An entry open.** Open an entry from Entries, then close it. Pass: the strip stays and nothing jumps. Note for the
   owner: the strip is on the open entry too.
5. **Portrait.** Open the Journal in portrait. Pass: unchanged, with the bar always shown and no strip.
