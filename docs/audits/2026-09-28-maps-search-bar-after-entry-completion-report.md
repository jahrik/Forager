# The Maps search bar after an open day entry: completion report

Intent `2026-09-28-17`, dispatch `prompts/preserved/2026-09-28-17.md`. Branch `journal-redesign`, base
`14cad7e`. The owner reported: "when moving from journal entry map to the main map, the search bar
disappears", and "It's missing in landscape also". The owner's ruling was "Keep entry, fix the bar
(Recommended)". The Maps tab's search bar now shows on Maps with a day entry left open, and the entry
stays open. The full suite is 267 classes / 2193 tests / 0 failures / 0 errors / 24 skipped.

Paths below are under `app/src/main/java/com/zynergylabs/forager/app/`. Line numbers are at base `14cad7e`.

## Verification

- **Base.** After a fetch, `origin/journal-redesign` was at `14cad7e`, the commit carrying the dispatch.
  The worktree `/home/zynergy-labs/Zynergy/forager-wt/search-bar-fix` was cut from it. During the work,
  `ba065ad` landed on the remote (the Layers-sheet terminal and completion report, `RECORD.md` and one
  doc only). It was merged in at `79b4d9e`, with no rebase.
- **Two bars: holds.** The app-wide header is the `SearchEntryBar` at `ui/availability/AvailabilityCompactScaffold.kt:711`.
  It is gated on `compactTab() != CompactTab.MAP`, so it never shows on Maps. The Maps tab's bar is
  passed to `CompactMapTab` as `searchBarSlot` (`:888`), and `CompactMapTab` composes it at
  `ui/availability/AvailabilityCompactMapUi.kt:655`.
- **Where the Maps bar is emptied: holds.** The gate is `searchBarSlot = if (isEditingJournalEntry) { {} } else …`
  (`:888-890`). It uses `isEditingJournalEntry = logUiState.editingEntry != null || cartographyUiState.editingEntry != null`
  (`:401`). Both values are ViewModel state, and neither reads the tab.
- **Why a tab switch does not clear it: holds.** `onBottomNavTabSelected` (`:448-493`) closes an open
  find on leaving Journal (`:457-458`). Nothing in it touches a Cartography entry. Outside
  `ui/log/Cartography*`, the only reference to `CartographyViewModel::onCloseEntry` is its wiring at
  `MainActivity.kt:491`. That wiring reaches `CartographyScreen` through `JournalTab.kt:533`, and the
  screen calls it from `requestLeaveEntry` (`ui/log/CartographyScreen.kt:201-207`).
  `CartographyViewModel.onCloseEntry` is at `ui/log/CartographyViewModel.kt:234`.
- **The failure: reproduced.** The failing tests below reproduce it in both orientations.
- **History: holds.** `1cea45b` is the repository's first commit ("Forager"), and its diff contains
  `searchBarSlot = if (isEditingJournalEntry) {`. It is an ancestor of `2458934`, and the file at
  `2458934` contains the same gate.
- **Landscape sizing is not the cause: holds.** The Maps-tab landscape width and alignment are
  computed at `:762-772` and applied to the bar's wrapper at `:912-917`, inside the slot's non-empty
  branch. Nothing there removes the bar.
- **Not the cause: holds, as far as it was read.**
  - The J5 short-window rule (`journalHidesSearchHeader`, `:705`) requires `compactTab() == CompactTab.JOURNAL`,
    and it gates only the header at `:711`.
  - The entry map's fullscreen is local state, `remember(entry.id)` at `ui/log/CartographyEntryReportScreen.kt:251`.
    It is separate from the main `isMapFullscreen`.
  - I did not re-read L0b or hide-on-scroll. The tests below show the bar returning with only the gate
    changed, so neither is needed to explain the symptom.
- **The find question: answered from the code. A find does not leave the bar empty.** On leaving
  Journal, the tab handler calls `leaveLogEntryEditingOfferingDiscard()` whenever
  `logUiState.editingEntry != null` (`:457-458`). It does not look at the mode.
  - That function (`ui/availability/AvailabilityScreen.kt:1124-1136`) calls `onLeaveLogEntryEditingIncidentally`,
    which is wired to `MushroomLogViewModel::onLeaveEditingIncidentally` (`MainActivity.kt:476`).
  - `onLeaveEditingIncidentally` (`ui/log/MushroomLogViewModel.kt:582`) sets `editingEntry = null` on
    every branch, for any open find, whether in its report view or its editor.
  - So a find in view mode is closed by the tab switch, and `isEditingJournalEntry` is false by the
    time Maps shows. This is not a second route to the symptom, so nothing more was built for it.
  - The find path was traced by reading. No test in this work runs it through the tab switch.
  - Two side effects are listed under Flags.

## The failing test and its message

`5d247a6` added four tests to `AvailabilityScreenBackNavigationTest`. Each opens the fixture's committed
day entry from Journal, in its report view or, separately, in its editor. It then taps Maps with a real
`performTouchInput` click at the nav item's centre, and asserts that the Maps search bar
(`ACTIVE_SEARCH_SUMMARY_TAG`, `"active-search-summary"`) is shown.

- **Two tests use the portrait bottom nav**, at the class's `w360dp-h640dp-xhdpi`. They also assert
  that there is no rail.
- **Two tests use the rail**, at `@Config(qualifiers = "w823dp-h384dp-land")`. They assert that the rail
  exists and that the Maps item lies inside the rail's bounds before touching it.
- **Every test also checks** that Maps is selected after the touch, that the bar is shown before any
  entry opens, and that it is absent while the entry is open on Journal.

Run at base plus these tests: the class ran 31 tests with 4 failures, 0 errors and 0 skipped. The build
log had no compile errors. Each failure is at the final assertion:

```
the Maps search bar (active-search-summary) is shown on the Maps tab with a day entry left open in its report view expected:<1> but was:<0>
the Maps search bar (active-search-summary) is shown on the Maps tab with a day entry left open in its editor expected:<1> but was:<0>
```

The report-view message came from the portrait and the landscape report-view tests, and the editor
message from the two editor tests. All four failures name the missing bar. The earlier assertions
passed, so each failure came after the real touch had selected Maps. The first attempt to build the
tests did not compile (a missing `click` import), and it produced no results that I used.

## What landed

- **`5d247a6`, tests first.** The four failing tests above.
- **`79b4d9e`**, a merge of `origin/journal-redesign` (`ba065ad`, record and docs only) after a push was
  rejected.
- **`35a99ad`, the fix.** One condition in `ui/availability/AvailabilityCompactScaffold.kt`:
  `searchBarSlot = if (isEditingJournalEntry && compactTab() == CompactTab.JOURNAL)`, where it had been
  `if (isEditingJournalEntry)`. The commit also adds a comment on the gate and corrects one stale
  sentence in the comment above it. The entry's state is untouched. The class then ran 31 tests with
  0 failures.
- **`b828624`, the rest of the ruling.** Four pins, one each for the report view and the editor in
  portrait and in short landscape. Each opens the day entry, touches Maps, then touches Journal. It
  asserts that the entry is still open ("Entry options" is shown and Entries home is absent) and that
  the Journal's search header is still hidden for it. They do not depend on the fix. The revert run
  below confirms they pass without it.
- **The existing test** "the top search bar hides while viewing or editing a Cartography entry, and
  reappears once it closes" is unchanged and passes, in the class runs and in the suite.
- **No existing assertion changed.** The diff to the test file adds lines only.

**An observation from the pins.** A day entry left open **in its editor** comes back in its
**report view**, not in the editor. I checked this with a temporary diagnostic line, which was removed
before the commit. It counted zero "Your own account (optional)" fields after returning, in all four
cases. The cause is that `CartographyScreen`'s `mode` is a plain `remember` (`ui/log/CartographyScreen.kt:171`),
so it resets when the Journal tab leaves composition. This behaviour predates this work, and the pins
do not assert either mode. See Flags.

## Revert check

- I saved a copy of `AvailabilityCompactScaffold.kt` to `app/build/scaffold.saved` (identical sha256,
  `fc84f6ed…0867`).
- I reverted the one line to `searchBarSlot = if (isEditingJournalEntry) {`. `git diff` showed only
  that line.
- I ran `AvailabilityScreenBackNavigationTest` from a cleared results directory.
  - The build log shows `:app:compileDebugKotlin` and `:app:compileDebugUnitTestKotlin` executed, with
    0 `e:` lines.
  - The JUnit XML (timestamp `2026-09-28T10:20:04Z`) shows 35 tests, 4 failures, 0 errors and 0
    skipped.
  - The four failures are exactly the four bar tests, with the two messages quoted above.
  - The four pins and the existing hide test passed.
  - Every failure is one that this revert can produce. The pins passing is the prediction for them.
- I restored the file from the saved copy, not from git. Afterwards its sha256 matched the saved copy,
  `git status` was clean, and line 898 reads `searchBarSlot = if (isEditingJournalEntry && compactTab() == CompactTab.JOURNAL) {`.

## Suite

I ran the full `:app:testDebugUnitTest` at `b828624`, with the scaffold restored, from a cleared
`app/build/test-results/testDebugUnitTest`. It ran `--offline` with `LC_ALL=C.UTF-8`, and the build was
successful in 2m 28s, with 0 `e:` lines in the log. The counts, summed from the JUnit XML, are
**267 classes / 2193 tests / 0 failures / 0 errors / 24 skipped**.

- **Against the baselines.** The planner's baseline at `af846cc` is 266 / 2183 / 0 / 0 / 24. The
  Layers-sheet work (`2026-09-28-16`) reported 267 / 2185 / 0 / 0 / 24 at its end, and that work is in
  this base. This work adds 8 tests to an existing class: 2185 + 8 = 2193, with the class count
  unchanged. I did not run a suite at base myself.
- **The held flaky family** (`JournalPendingDeleteTest`'s album long-press, tap and Undo tests, and
  `JournalTabTest`'s photo-pull test) did not fail in this run.
- There was no OOM kill.

## Device-only

For the next build's device check, on the S22:

1. **Portrait.** Open Journal and open a committed day entry in its report view. Tap Maps on the bottom
   nav. The Maps search bar shows at the top, above the compass strip, with no band of bare map where
   it should be. Tap Journal: the same entry is still open.
2. **Portrait, editor.** Repeat step 1 with the entry open in its editor ("Entry options", then
   "Edit entry").
3. **Short landscape**, the rail showing. Repeat steps 1 and 2, tapping Maps on the rail. The Maps
   search bar shows on the punch-hole side, capped short of the centre line.
4. **Where the entry map is showing.** Repeat the route from a day entry whose map is showing,
   including the entry map's own fullscreen, since the owner's words were "from journal entry map to
   the main map".

Robolectric reports zero insets. The bar's position against the real status bar and cut-out is
therefore device-only, as it already was.

## Decisions I made

- **The literal form of the gate.** The dispatch gives the gate as "an entry is open and the Journal
  tab is showing", and I wrote exactly that. The slot composes only inside the `CompactTab.MAP` branch
  of `when (compactTab())` (`:774`, `:782`), so the second half is always false there, and the
  condition always resolves to the bar. Deleting the gate would behave the same. I kept the conjunction
  because the dispatch named it and it matches prediction 2. I documented in the code comment that it
  is always false in that branch. Choosing between the two properly is the planner's call.
- **One touch per nav item.** Each test uses one real touch at the nav item's centre, not samples
  across its bounds. The claim under test is about the bar, and the touch only routes to Maps. The
  dispatch asked for "a coordinate touch". I added checks that the touched item is in the rail in
  landscape and is selected afterwards.
- **Where the tests live.** They are in `AvailabilityScreenBackNavigationTest` itself, with method-level
  `@Config` for landscape. This reuses its fixture, which is the one the existing hide test uses. The
  dispatch allowed this class or a sibling.
- **Two sets of tests.** The pins that the entry is still open back on Journal are separate tests from
  the bar tests, not extra assertions at the end of them. That keeps the pins independent of the fix,
  which the revert check then shows directly. Before committing, I removed a mid-test bar assertion
  from the pins so that they would not depend on the fix.
- **What "the same entry" means in the pins.** The fixture holds one committed entry, so the pins assert
  that an entry is open, not its id.
- **One comment edit.** I corrected the sentence "Empty while isEditingJournalEntry, unconditionally"
  in the slot's existing comment, since the change made it false.
- **No full-suite baseline at base.** I compared against the planner's figures instead: `af846cc`'s
  266/2183/0/0/24, and the Layers-sheet report's 267/2185/0/0/24 at its end. I did this because the
  machine is shared and another suite had been running. The dispatch did not ask for a base run, but
  the environment it cites (`2026-09-28-03`) did.
- **The record.** I did not write any record entry, sweep or store copy, because the dispatch says the
  kit is gone and the planner writes the record. `.claude/` and the checkers are absent at this base.

## Flags outside scope

- **A day entry left open in its editor returns in its report view** (`CartographyScreen.kt:171`, a
  plain `remember`). The ruling's reason was "returning to Journal puts the user back where they were".
  For the editor, it does so only in part. Whether unsaved edits survive that return, and how they show
  in the report view, is unverified.
- **An open find is closed by the tab switch**, even in its report view (`MushroomLogViewModel.kt:582`,
  through `:457-458`). The ruling keeps a day entry across a tab change, but a find is not kept. That is
  inconsistent with the ruling's reason, and the ruling does not say whether it applies to finds.
- **A committed find that is only being viewed** gets a "Saved to Drafts" snackbar with Discard on the
  tab switch. `leaveLogEntryEditingOfferingDiscard` (`AvailabilityScreen.kt:1124-1136`) shows it whenever
  `editingEntry` was non-null, and does not look at the mode or at `isDraft`. I found this by reading
  and have not run it. What Discard then does to a committed find is also unverified.
- **The case from the dispatch's abort list, an entry deleted elsewhere while the user is on Maps,** was
  not exercised and not built for.
