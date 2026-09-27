# 2026-09-27: Journal redesign J1, completion report (partial: S4 stopped for decisions)

Dispatch: `prompts/preserved/2026-09-27-16.md` (build, J1 coder). Plan:
`docs/plans/journal-redesign.md` (J4, J6, J10, L7, "Build order" J1, "Rules for
every build stage"). Map: `docs/audits/2026-09-27-journal-j0-pulse.md`. Written
by the J1 coder in a cloud worktree on local branch `j1`, which tracks
`origin/journal-redesign`.

**Status: S1, S2, S3 and S5 are built, tested and pushed. S4 (the All logbook
and type badges) is not built.** It stopped on three design questions the
dispatch and the plan leave open (section "Needs a decision"). Until S4 lands,
the All chip, which is Records' default, shows the chip row and nothing under
it. That is the state of the branch head, and it is not shippable as is.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `a07b05861c1995729e870431766eacadc9c772ee` | S1 tests first: `AvailabilityScreenJournalStateTest` (2 tests). Fail at base, below. |
| `2f8c918234f50639c06efbb4f8f7a4241aac3dd0` | S1: `JournalScreenState` (new), created in `AvailabilityScreen`, passed through `CompactMainScaffold` to `JournalTab`, Records selection handed to `RecordsTab`. |
| `d34cf6079bde1fa9d3b963fc790e1798cd8055e9` | S2: `RecordTypeStyle` (new) and `RecordTypeStyleTest`. |
| `dca3f31ff7bf1c8d263aa8237ea322190129940c` | S3 tests first: `RecordsFilterChipsTest` (9 tests); the S1 test moved from the "Offline Maps" sub-tab to the Offline maps chip. Fail at base, below. |
| `fb1ed5cebf32c27d12c5e9b7a404f46945dd2875` | S3: `RecordsFilterChips.kt` (new), `RecordsTab` on chips, `RecordsSubTab` gains `ALL`; label-driven tests updated in place. Also carries one S5 comment (see S5). |
| `ff26f2972a893082352cf10fad7cb51a7545196b` | S5: two stale comments (the third went in with S3). |
| this report's commit | This file. |

Production diff `3df97d1..ff26f29`: 8 files, 387 insertions, 83 deletions, all
under `ui/log/` and the two `ui/availability/` files the dispatch allows
(planner's prediction 4: holds).

## Premises checked before building

- **Base.** `origin/journal-redesign` was `3df97d15752ebccf07370c873eebfb05a0c7c5f0`
  when fetched. `git diff --stat c3791de 3df97d1` touches only `RECORD.md`,
  `docs/audits/2026-09-27-journal-j0-pulse.md` and
  `prompts/preserved/2026-09-27-16.md`, as the planner's correction says.
  `git diff --stat 352b708 c3791de -- app/` is empty. Branch `j1` was cut from
  `origin/journal-redesign`.
- **Baseline at the base**, from a cleared results directory:
  226 classes / 1782 tests / 0 failures / 0 errors / 24 skipped. Matches the
  planner's figure at `352b708`.
- **J0 citations used by this dispatch, re-read at `3df97d1`; all hold:**
  `JournalTab(` called at `AvailabilityCompactScaffold.kt:913`; `JournalTab.kt:226-243`
  (`mode`, the two picker flags, `selectedTopTab`, `recordsPendingSubTab`, all
  `remember`); `RecordsTab.kt:124` (`selectedTab`, `remember`, default
  WAYPOINTS); `AvailabilityScreen.kt:693` (`compactTab`); `RecordsTab.kt:141-144`
  (`selectTab`, calls `onFindsTabLeft` when leaving FINDS); `JournalTab.kt:260-262`
  and `:476` (`leaveFindEditingIfNeeded`, passed as `onFindsTabLeft`);
  `JournalTab.kt:245-255` and `RecordsTab.kt:126-131` (the EDIT_NEW_FIND latch);
  `RecordsTab.kt:133-139` (open callbacks); `RecordsTab.kt:152` (Back to
  Waypoints); `PhotoViewerDialog.kt:111-113`; the test comments at
  `AvailabilityScreenBackNavigationTest.kt:965` and `LogEntryDetailScreenTest.kt:263`.
  Every J0 part C test call site I touched was at the line J0 gives.
- **No recreation precedent in the repo.** `git grep` over `app/src/test` for
  `recreate()` and `StateRestorationTester` found nothing. I used Compose's
  `StateRestorationTester` (in `ui-test-junit4`, already a test dependency; no
  new artifact). It saves every `rememberSaveable` through the host's real
  `SaveableStateRegistry`, disposes the composition and recomposes from the
  saved values; plain `remember` does not come back. See "Decisions" 1.
- **Offline regions and tracks are loaded eagerly**, so chip counts are right
  without opening their chips: `AvailabilityViewModel.kt:133` calls
  `loadOfflineRegions()` at init, and `TrackRecordingViewModel.kt:180` calls
  `loadTracks()` at init. The open callbacks are refreshes.
- **Dates for S4, cited (the check the dispatch asked for):** waypoints
  `createdAtEpochMillis` (`domain/model/Waypoint.kt:29`); tracks
  `startedAtEpochMillis` (`domain/model/Track.kt:40`); offline regions
  `createdAtEpochMillis` (`domain/OfflineMapRepository.kt:204`, on
  `OfflineRegionSummary`); logged finds `foundOn: LocalDate`
  (`domain/model/MushroomLogEntry.kt:41`), stored as a bare `yyyy-MM-dd` with no
  time and no zone (`data/local/MushroomLogEntryEntity.kt:55` and its doc
  comment). Every type has a date, so the "no date" abort does not fire. Finds
  have **no time of day**, which is question S4-1 below. The device-local-day
  convention for putting epoch-millis records and `foundOn` on the same day
  already exists: `domain/LocalDayRange.kt:48` and its doc comment (owner
  decision #1, "day boundaries are device-local").

### Premises that were wrong

- **Planner's prediction 2** ("the ones that only click 'Records' do not
  change") is wrong for one call site: `AvailabilityScreenWaypointFlowTest`'s
  `openWaypointsTab` helper (J0's `av/AvailabilityScreenWaypointFlow:224`) clicks
  only "Records" and relied on Waypoints being the default; with All the default,
  its 4 tests failed. Updated in place (it is in J0's "Records" list).
- **Predictions 1, 3, 4.** 1 holds (a custom `Saver`; it saves enum names).
  3: the suite grew by 12 tests (1782 to 1794), below the predicted 20 to 45,
  because S4's tests were not written. 4 holds.

## What was built

**S1.** `ui/log/JournalScreenState.kt`: a `@Stable` holder with `topTab` and
`recordsFilter` (the latter exposed also as a `MutableState`,
`recordsFilterState`), a `listSaver` over enum names, and
`rememberJournalScreenState()`. Created in `AvailabilityScreen` right below
`compactTab` and passed as a new last parameter of `CompactMainScaffold` to
`JournalTab`'s new `journalState` parameter; `JournalTab` delegates
`selectedTopTab` to it and passes `recordsFilterState` to `RecordsTab`'s new
`selectedTabState` parameter. A marked comment in the class shows where J2/J3's
Entries view mode and scroll positions go. Transient state is where it was:
`mode`, both picker flags, `recordsPendingSubTab`, `pendingJournalDestination`,
dialogs. `LogPanel.kt` is untouched; it calls `RecordsTab` without the new
parameter and gets `RecordsTab`'s local `remember` default.

**S2.** `ui/log/RecordTypeStyle.kt`: `RecordType` (four values),
`RecordTypeColors(accent, container)`, and `RecordTypeStyle.colors(type)`
reading `MaterialTheme.colorScheme` (plus `colorsIn(scheme, type)`, the same
mapping over an explicit scheme, for the test). Exactly the plan's table.

**S3.** `ui/log/RecordsFilterChips.kt`: `RecordsFilterChipRow`, a
`horizontalScroll` row of `FilterChip`s, each with a leading icon, a label and
a count; single-type chips use `RecordTypeStyle` (container behind the selected
chip, accent on icon and selected label); All uses `FilterChipDefaults`.
`RecordsSubTab` is now `{ ALL, FINDS, RECORDED_TRACKS, WAYPOINTS, OFFLINE_MAPS }`
(chip order; nothing reads the ordinal). `RecordsTab`'s `SecondaryTabRow` is
gone; Back steps to All from any single-type chip under the same
`!findsEditingInProgress` gate; from All, Back falls through to `JournalTab`'s
Records-to-Cartography step, as it did from Waypoints. `onFindsTabLeft`, the
open callbacks and the pending latch are unchanged in code, so leaving Finds
mid-edit still exits incidentally and EDIT_NEW_FIND now selects the Finds chip.
The All branch renders an empty `Box` pending S4.

**S5.** `PhotoViewerDialog.kt`'s comment now says the manifest does handle
orientation/screenSize/screenLayout/keyboardHidden and keeps
`rememberSaveable` for the causes that still recreate (night mode, fold,
locale, font scale, density, process death). The two test comments now call
"Album" a Journal (Cartography) sub-tab. I did not restate J0's "since
2026-09-15": this clone is shallow (`git rev-parse --is-shallow-repository` is
`true`), so the manifest's history is not readable here.

## Tests: base failures

Each tests-first commit was run at its own base from a cleared results
directory, build log checked for `e: ` lines first (none).

**S1 at `3df97d1`** (`a07b058` over the base): 2 tests, 2 failures, both at
`AvailabilityScreenJournalStateTest.kt:78`, `Failed to assert the following:
(Selected = 'true')` on the `Records` tab node after returning (called from
`:90`, the tab-change test, and `:103`, the restore test). Both selections had
reset to Cartography: the stated reason.

**S3 at `d34cf60`** (`dca3f31` over S2): 11 tests (9 new plus the 2 S1 tests
now on chips), 11 failures, every one `Expected exactly '1' node but could not
find any node that satisfies: (TestTag = 'records-chip-…')`: no chips existed.
The first S3 base run did not compile (a missing `assert` import in the new
test); that run's results were not read, the import was added, and the second
run is the one cited.

**All of them pass** at `fb1ed5c` and at the final head.

What the S3 tests assert (`RecordsFilterChipsTest`, through the real
`JournalTab`, Back through the Activity's real `OnBackPressedDispatcher`):

- the row: five chips, left to right in the plan's order, labels and counts
  (All 7 = 1 find + 1 track + 2 waypoints + 3 regions), All selected, the old
  sub-tab labels gone;
- Waypoints, Tracks, Offline maps, Finds each show their old sub-tab's content
  (waypoint delete buttons; `share-track-t1`; "Downloaded Maps" and a region
  name; the finds gallery's "+" tile and a find tile); Tracks and Offline maps
  fire their open callbacks once per entry;
- Back from each single-type chip returns to All, and Back from All reaches
  Cartography;
- EDIT_NEW_FIND lands on the Finds edit form with the Finds chip selected;
- leaving Finds mid-edit calls the incidental exit exactly once; leaving Finds
  with nothing open does not.

Every chip selection that stands for a finger is a `performTouchInput` click,
and the per-type tests sample three points per chip (12 %/30 %, 50 %/50 %,
88 %/70 % of the chip's own width/height), returning to All between samples so
each touch has to do the selecting. Semantic clicks remain only where a test
is navigating, not testing the chip (opening Records, the "+" tile, and the
label-driven tests' updated helpers).

## Revert checks

Runner: saves a copy of the file, applies a one-line edit, runs the named
classes from a cleared results directory, refuses to read results if the build
log has `e: ` lines, then restores from the saved copy and `cmp`s it. After
each, the forward change was confirmed present by `grep` before commit.

| Behaviour | Edit | Predicted | Observed | Build log |
|---|---|---|---|---|
| Hoist | `AvailabilityCompactScaffold.kt`: `journalState = journalScreenState,` commented out (JournalTab falls back to its own in-branch default) | tab-change test fails | tab-change test fails at `:78` (Records not selected); restore test passes (the in-branch `rememberSaveable` is restored) | clean |
| Saver | `JournalScreenState.kt`: `rememberSaveable(saver = …)` to `remember { … }` | restore test fails, tab-change test passes | exactly that: 1/2 fail, `:78` via `:103` | clean |
| Back rule | `RecordsTab.kt`: `selectTab(RecordsSubTab.ALL)` to `selectTab(RecordsSubTab.WAYPOINTS)` | both Back tests fail | `RecordsFilterChipsTest` Back test and `AvailabilityScreenBackNavigationTest` "back on a single-type Records chip steps to All" fail: `records-chip-all` Selected = 'false'; 2/36 | clean |
| Incidental exit | `RecordsTab.kt`: `onFindsTabLeft()` to `Unit` | incidental-exit test fails | `leaving Finds mid-edit calls the incidental exit once expected:<1> but was:<0>`; 1/9 | clean |
| EDIT_NEW_FIND latch (extra) | `JournalTab.kt`: `recordsPendingSubTab = RecordsSubTab.FINDS` to `= null` | EDIT_NEW_FIND test fails | `records-chip-finds` Selected = 'false'; 1/9 | clean |
| Logbook ordering | not run: S4 not built | | | |

`RecordTypeStyleTest` got no revert check (not in the dispatch's list); every
role in its scheme is distinct, so a swapped or wrong role reads back as a
different colour.

## Label-driven test changes (plan rule; J0 part C)

At the S3 build without these updates the full suite had 44 failures
(`s3full`: 229 classes / 1794 tests / 44 failures / 0 errors / 24 skipped), and
every one traced to a J0 part C call site. Each was updated in place in
`fb1ed5c`, to the chip's test tag via `recordsFilterChipTestTag(...)`. Nothing
was disabled, skipped, ignored or weakened; two Back tests were renamed
because the rule they pin changed (Waypoints to All).

| File | Call sites changed | Tests that had failed |
|---|---|---|
| `ui/log/JournalTabTest.kt` | 1 (`:209`, "Logged Finds" in `setScreen`) | 14 |
| `ui/log/LogPanelTest.kt` | 1 (`:169`, "Logged Finds") | 3 |
| `ui/availability/AvailabilityScreenAdaptiveLayoutTest.kt` (class `AvailabilityScreenWideWindowLayoutTest`) | 1 (`:429`, "Logged Finds") | 1 |
| `ui/availability/AvailabilityScreenBackNavigationTest.kt` | 4 (`:517`, `:603`, `:619`, `:917`) | 4 |
| `ui/availability/AvailabilityScreenSettingsPanelTest.kt` | 6 (`:248` and `:253`, the helpers `openOfflineMapsSubTab`/`openRecordedTracksSubTab` updated in place; `:440`; `:453`; `:609`; `:614`) plus one added Waypoints-chip tap in the test at `:601`, before its Waypoints-content assertion | 16 |
| `ui/availability/AvailabilityScreenWaypointFlowTest.kt` | 1 (the `openWaypointsTab` helper, J0's `:224` "Records" click; a Waypoints-chip tap added) | 4 |
| `ui/availability/AvailabilityScreenLandscapeB3DestinationsTest.kt` | 2 (`:138-139`, `recordsSubTabs()` now measures the chip row by its tag; `:166`, All selected instead of "Waypoint Markers") | 2 |
| **Total** | **16** | **44** |

The B3 measurement changed subject: the old helper spanned the first and last
sub-tab (which filled the width); chips size to their labels and scroll, so
their span is no longer the width Records gets. The row (`fillMaxWidth`,
tagged `RECORDS_FILTER_CHIP_ROW_TAG`) is measured instead, and still passes
the 640 dp cap and centring at both rotations. J0 part C entries not touched
("Cartography", "Drafts (1)", "Album", "Entries", and the "Records" clicks
other than WaypointFlow's) still pass.

## Suite before and after

- Before, at `3df97d1`: 226 classes / 1782 tests / 0 failures / 0 errors / 24 skipped.
- After, at `ff26f29` (the final code head; this report adds no code), cleared
  results directory, build log clean: **229 classes / 1794 tests / 0 failures /
  0 errors / 24 skipped.** +3 classes (`AvailabilityScreenJournalStateTest`,
  `RecordTypeStyleTest`, `RecordsFilterChipsTest`), +12 tests (2 + 1 + 9),
  skipped unchanged.

## Needs a decision (why S4 stopped)

The dispatch says to stop on any design question it and the plan leave open.
These three block S4; nothing of S4 was built or guessed.

1. **Where a find goes inside its day.** "Grouped by day, newest first" is
   satisfiable at day level for every type, but within a day waypoints, tracks
   and regions have times and finds have none (`foundOn` is a bare date,
   `MushroomLogEntry.kt:41`). The plan's mockup shows a find at "6:42 PM",
   which the data cannot supply. Options: (a) finds first within their day;
   (b) finds last; (c) order a find by its earliest photo's
   `LogPhoto.createdAtEpochMillis` (`LogPhoto.kt:33`, nullable) when it has
   one, falling back to (a) or (b); (d) a separate "Finds" sub-group inside
   each day. A find row would show no time in any option except (c).
2. **What tapping a find row in All does.** Today a find opens only inside the
   Finds slot (`findsSection`, rendered by the Finds sub-tab). Options:
   (a) select the Finds chip and open the report there; Back then closes the
   report to the Finds gallery, and a second Back returns to All; (b) open the
   report in place over All, so Back returns straight to All (needs the finds
   section rendered outside the Finds chip, a larger change to `JournalTab`'s
   routing); (c) rows in All are not tappable in J1. Waypoint, track and region
   rows have no row tap today (their actions are icon buttons:
   Directions/Delete, Share, Delete), so "does what tapping it does today" is
   nothing for those three. A related sub-question: do All rows carry those
   icon actions, or only in the single-type chips? ("Deletes stay as they are"
   reads to me as "unchanged where they are", not "added to All", but that is a
   reading.)
3. **Drafts.** Are draft finds (`MushroomLogUiState.draftEntries`) in All, and
   in the Finds count? I built the Finds count as committed finds only
   (`uiState.entries.size`, what the Finds gallery's first "Log" tab lists);
   All's count adds that to the other three. If drafts belong in All, the count
   changes with it.

## Decisions I made that the dispatch did not

1. **Recreation test harness:** `StateRestorationTester`, since the repo had no
   `recreate()` precedent. It is a composition-level round trip through the
   real saveable registry, not an Activity recreation; the ViewModel-held state
   is untouched by it, which matches a real recreation.
2. **The Records selection is passed as a `MutableState`**
   (`RecordsTab(selectedTabState = …)`, default local `remember`), so `LogPanel`
   needs no change and keeps local, non-saveable state until J6.
3. **`LogPanel` gets the chips too**, because it shares `RecordsTab`. J0 part C
   and the dispatch's test list include `LogPanelTest:169`, so this looked
   expected, but it is a visible change to the wide tree in a stage that says
   `LogPanel` is J6. `LogPanel` passes no finds count, so its Finds and All
   chips show **no count** rather than a wrong one (`findsCount: Int? = null`).
   Its Back from a single-type chip now also goes to All.
4. **Chip icons:** All `ViewList`, Finds `Eco` (leaf; the icon set has no
   mushroom), Tracks `Timeline`, Waypoints `Place`, Offline maps `Map`. All has
   an icon too ("each with an icon"), though the mockup draws All without one.
5. **Chip labels** "All", "Finds", "Tracks", "Waypoints", "Offline maps" (the
   plan's), with the count as a second text in the label.
6. **Chip colours:** type container behind the selected chip, type accent on
   the icon always and on the label when selected; All keeps Material defaults.
7. **Test tags** `records-chip-{all,finds,tracks,waypoints,offline-maps}` and
   `records-filter-chip-row`, fixed strings rather than enum names.
8. **The dispatch and the plan differ** on one point I followed the dispatch
   on: the plan's J1 build order does not mention Back; the dispatch sets Back
   to All (planner's call). No other disagreement found.

## Device-only items

- The chip row's real widths, one-line labels and sideways scroll at 360 dp
  and in a short landscape window (Robolectric's text measurement in this
  project is known to be implausible, per the comment that stood above the old
  tab row in `RecordsTab.kt` at `3df97d1`).
- Chip colours in light, dark and the Understory theme: the test checks the
  role mapping, not what it looks like.
- A real night-mode toggle and a fold with the Journal on Records and a
  non-default chip: the state should survive (StateRestorationTester stands in
  for this under Robolectric). `compactTab` is plain `remember`, so after such a
  recreation the app is back on Maps; tapping Journal should show the kept tab
  and chip.

## Flags outside scope

- `compactTab` itself does not survive recreation (`AvailabilityScreen.kt:693`,
  plain `remember`), so after a night-mode toggle the user lands on Maps, not
  the Journal. Not asked for here.
- `CompactMainScaffold`'s header comment counts 109 extracted parameters; the
  new one is noted at its declaration as added after that count, rather than
  editing the historical tally.
- The branch head's Records opens on an empty All until S4 is built.
