# Leaving the Journal: the fixes (completion report, stopped at F3)

Intent `2026-09-28-44`, dispatch `prompts/preserved/2026-09-28-44.md`. Worktree `forager-wt/leave-fixes`, branch
`leave-fixes`, pushed to `journal-redesign`. Authority: `docs/plans/journal-redesign.md`, "Leaving the Journal: the
owner's rulings (2026-09-28)". The kit is gone at this base, so I wrote no `RECORD.md` entry. The planner writes the
terminal.

**Status.** F1, F2 and F4 are built and pushed, each with its tests first, its revert checks and a full suite.
**F3 is not built.** I stopped on it: removing the tab handler's close makes two routes reachable that open another
find over the one now kept open, and no ruling says what happens to the kept one (see "F3: the question"). Nothing of
F3 is in the tree.

Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

## Base

- `origin/journal-redesign` was `18cc8bb` after `git fetch`, the commit carrying the dispatch. **Confirmed.**
- `git diff --stat 9cf3ffa 18cc8bb -- app` is empty, so the app tree is the one the planner's suite ran on.
- Resources: 13 GB free on `/` at the start, 12 GB at the end. About 3 GB of memory available throughout. No OOM, and
  I stopped no daemon.

## Verification before building

### The 13 characterisation tests at base

I copied `LeavingTheJournalInvestigationTest.kt` from `a89b240` into the worktree and ran it against `18cc8bb`:
**13 tests, 0 failures, 0 `e: ` lines.** Every one of them still describes today's behaviour. No difference to name.
The file was then removed and never committed, since it pins the old behaviour.

### The investigation's code paths, at `18cc8bb`

Every path is still there and behaves as the investigation described. Only the line numbers have moved, from the M1,
track-width and search-copy stages that landed since.

| Investigation | At `18cc8bb` | Same code? |
|---|---|---|
| Tab handler, `AvailabilityCompactScaffold.kt:457-459` | `:471-473` | yes |
| Search-bar gate, `isEditingJournalEntry`, `:401` | `:415`; used at `:725` (header off the Maps tab) and `:913` (the Maps tab's slot) | yes |
| Backgrounding observer, `:388`, `:431` | `:402` (`latestIsJournalEditing`), `:444-446` (the `ON_STOP` call) | yes |
| Search dropdown handler, `:353` (drawer report) | `:367` | yes |
| The wrapper, `AvailabilityScreen.kt:1124-1136` | `:1170-1183` | yes |
| The wide `LogPanel`'s use of it, `:1262` | `:1311` | yes |
| `onDiscardLogDraft = mushroomLogViewModel::onDeleteEntry`, `MainActivity.kt:477` | `:477` | yes |
| `JournalTab`'s `mode`, `:270` | `:289` | yes |
| `leaveFindEditingIfNeeded`, `:323-325` | `:357-359` | yes |
| `MushroomLogViewModel.onOpenEntry` `:386-394`, `onLeaveEditingIncidentally` `:582-640` (the `!current.isDraft` branch `:614-615`), `onDeleteEntry` `:760-781` | the same lines (`:615` for the branch) | yes |
| `CartographyScreen`'s `mode`, `:171` | `:180`; `requestLeaveEntry` `:210-216`; the branch `:309`; Edit `:356` | yes |
| `CartographyViewModel.onCloseEntry` `:234-254`, doc `:225-232` | `:234-254`, doc `:222-232` | yes |
| Map tab handler, `AvailabilityCompactMapUi.kt:483` (drawer report) | `:497` | yes |

**One premise that did not need a fix.** F3 says the Maps search bar must not be hidden for a find open on the Journal,
and describes this as extending `35a99ad`'s gate to finds. At base that gate already covers finds:
`isEditingJournalEntry` includes `logUiState.editingEntry` (`:415`), and the Maps tab's slot is empty only when it
holds **and** the Journal tab shows (`:913`). The M1 report recorded the same (its F3). So that bullet of F3 needs a
test, not a code change. F3 is not built, so neither was written.

## F1: "Saved to Drafts" only for a real draft

**The owner's ruling:** "Snackbar only for real drafts (Recommended)".

**What was wrong.** The wrapper (`AvailabilityScreen.kt:1170-1183` at base) captured whatever entry was open, ran the
leave, and offered "Saved to Drafts" with Discard for that id. Discard is `onDeleteEntry`. So:
- after only viewing a committed find, Discard deleted the committed find, with no Undo (investigation, Behaviour 1);
- after an unchanged editor, the snackbar said "Saved to Drafts" when the leave had just deleted the draft copy.

The tests-first run found **a second path** to the same loss. A new find is left by Back, so the snackbar is offered.
The user then reopens it from Drafts and saves it while the snackbar is still up. It is committed under the same id
(`CommitDraftEntryUseCase` keeps a new draft's own id), and the stale Discard then deleted the committed find.

**The fix** (`b5359cd`):
- `leaveKeepsDraft(left, entries)` and `isUnchangedReEdit(current, entries)`, two top-level functions in
  `ui/log/MushroomLogViewModel.kt`. `onLeaveEditingIncidentally` now calls `isUnchangedReEdit` instead of its inline
  copy of the same comparison, so the ViewModel and the wrapper share one definition of what the leave keeps.
- The wrapper offers the snackbar only when `leaveKeepsDraft` holds for the entry being left. Otherwise, nothing.
- Discard deletes the id only while it is still in `draftEntries` when tapped. Otherwise it logs a warning and
  deletes nothing.
- The ViewModel's behaviour is unchanged. The wide `LogPanel` shares the wrapper, so it is covered by the same code.

**Planner prediction 2 ("a mode check in the leave path, not a ViewModel change"): half right.** It is in the leave
path, and the ViewModel's behaviour did not change. But it is not a mode check: the rule is whether the leave keeps a
draft. A mode check would still offer the snackbar after an unchanged editor. `MushroomLogViewModel.kt` did change:
the comparison moved out into a shared function.

## F2: the day-entry editor survives the round trip

**The owner's ruling:** "Return to the editor (Recommended)".

**The fix** (`b91a543`):
- `CartographyScreen`'s `mode` is now a parameter, `entryModeState`. It defaults to the old local `remember`, for
  `LogPanel` and for tests.
- `AvailabilityScreen` holds it in a `rememberSaveable`, beside `journalScreenState`, and passes it through
  `CompactMainScaffold` (new parameter `cartographyEntryModeState`) and `JournalTab` (a parameter of the same name).
- An entry left in its editor therefore comes back in its editor. That holds for a committed entry with typed text, a
  committed entry with a changed keep or withhold choice, and a new draft ("Finish entry" is shown).
- **The Save or Discard prompt is the existing one**, not new copy: `CartographyEntryEditScreen`'s `showLeavePrompt`
  dialog ("Save your changes?", with Save, Discard and Cancel; `CartographyEntryEditScreen.kt:317-342`), raised by
  `CartographyScreen.requestLeaveEntry` (`:210-216` at base). Once the editor comes back, Back from it raises that
  prompt, and the tests take both answers.
- **`onCloseEntry`** no longer merges a dirty committed entry into `entries`, which is what its doc comment already
  said it did. It logs the close at info level, and its doc comment now says why.
  - On the prompt's Save, `onCloseEntry` runs right after `onSaveEntry` launches its write (`onSave(); onBack()`). The
    list is then settled by `onSaveEntry`'s own upsert when the write lands, so the info line also fires on an
    ordinary prompt Save on a device.

## F4: Back with the drawer open, over the dropdown and the Map tab

**The check.** A probe at `b91a543`, run and deleted uncommitted, did this for each state:
1. opened the state;
2. touched Tools for real, at the centre of its nav item;
3. pressed Back twice through the Activity's dispatcher;
4. printed the drawer and the state after each step.

Its output is in `app/build/lf/probe1.xml` (not committed).

| State | Tools opens the drawer over it? | First Back at base |
|---|---|---|
| Search dropdown, Maps, portrait | yes | closed the dropdown; the drawer stayed open |
| Search dropdown, Maps, short landscape (rail) | yes | the same |
| Search dropdown, Journal, portrait | yes | the same |
| "Set on map" (`pickingSearchLocation`) | yes | closed the picker; the drawer stayed open |
| The Map tab's Log a find picker (`pendingAction`), portrait | yes | the same |
| The add-action menu (`showActionMenu`), short landscape | yes: the menu's scrim leaves the rail clear | closed the menu; the drawer stayed open |
| The add-action menu, portrait | **no**: the menu's scrim covers the bottom nav, so the touch dismisses the menu | (no drawer) |

So both handlers can be enabled under the open drawer. **Planner prediction 3 holds.**

The Tools tap does not close the dropdown. The tab handler only dismisses taxon suggestions and opens the drawer
(`AvailabilityCompactScaffold.kt:482-490` at base). The dropdown's scrim is padded off the nav on purpose (`:1129-1136`).

**The fix** (`0b4fc9c`), the drawer fix's `backEnabled` pattern:
- the dropdown's handler: `BackHandler(enabled = showSearchDropdown && !isDrawerOpen())`;
- the Map tab's handler: `BackHandler(enabled = !isDrawerOpen && (pendingAction != null || pickingSearchLocation || showActionMenu))`.

Each state stays up under the drawer. The next Back, with the drawer closed, unwinds it as before; the tests check
that, except on the Journal (see Findings).

## The tests

All are in `app/src/test/java/com/zynergylabs/forager/app/ui/availability/LeavingTheJournalFixesTest.kt`, 21 tests,
through the real screen:
- the real `AvailabilityScreen`;
- the real `MushroomLogViewModel` and `CartographyViewModel`;
- an in-memory `ForagerDatabase` and a real `FilePhotoStore`.

The fixture is the investigation's, copied from `a89b240` with its private stubs renamed `LeaveFix*`. The
investigation branch was not merged. Tab changes and Tools are real touches at the nav item's centre. Back goes
through the Activity's dispatcher.

### Tests first, with the failure at base

The F1 tests were run at `18cc8bb`. The F2 tests were run at `b5359cd` and the F4 tests at `b91a543`. Those are the base
plus the earlier fixes, which touch none of these paths.

| Test | Failure at base |
|---|---|
| F1 viewing a committed find then leaving for Maps … | `no "Saved to Drafts" snackbar, since no draft was saved expected:<0> but was:<1>` |
| F1 short landscape, … leaving on the rail … | the same |
| F1 viewing a committed find then opening the Tools drawer … | the same |
| F1 a committed find opened in its editor and left by Back unchanged … | the same |
| F1 wide, … the drawer's editor and left by Back unchanged … (`w1280dp-h900dp`) | the same |
| F1 a new find left by Back offers Discard, and once that find is saved the Discard deletes nothing | `Discard deleted no committed find expected:<[find-1, find-new]> but was:<[find-1]>` |
| F2 a committed day entry's editor left with typed text comes back in its editor … | `the day entry's editor shows (its account field) expected:<1> but was:<0>` |
| F2 after that return, Back … asks Save or Discard, and Discard leaves Entries showing the stored text | the same |
| F2 after that return, the leave prompt's Save stores the typed text | the same |
| F2 … a waypoint withheld comes back in its editor … | the same |
| F2 a new day entry, a draft, left in its editor comes back in its editor | the same |
| F2 closing a day entry with unsaved changes leaves the Entries list and card on the stored text | `expected:<T[he original account].> but was:<T[yped on the trail, not saved].>` |
| F4 (six tests: the six "yes" states above) | `drawer open = true, <state> held = false expected:<drawer [closed, held]> but was:<drawer [open, gone]>` |

**Three tests pass at base, by design, and are not tests-first:**
- **F1, the changed-editor guards, compact and wide.** Leaving a changed editor already showed the snackbar, and
  Discard already deleted only the draft; the dispatch asks for exactly this to be tested on the database. They guard
  that F1 does not withhold the snackbar from a real draft. Revert check R1d shows they bite.
- **F4, the portrait add-action menu.** A proof that the menu cannot be open under the drawer in portrait.

**Characterisation tests ported, with their assertions inverted:**
- the investigation's withheld-waypoint test, now "comes back in its editor with the waypoint still withheld";
- its new-draft test, now "comes back in its editor".

None was brought in unchanged. The F1 tests restate its Behaviour 1 cases as the new behaviour, with new assertions.

**Test corrections, both made before the fix's commit and both in the commit message:**
- **F2's helper.** It asserted "no Entry options menu" as a marker of the report view, but the editor has that menu
  too (`CartographyEntryEditScreen.kt:227`). I dropped that half. The half that failed at base, the editable account
  field, stays. The first F2 test adds a check that the typed text is drawn only inside the editable field.
- **F4's Journal dropdown test.** It claims only the first Back. Its second-Back half cannot hold on the Journal at
  base or after the fix (see Findings).

## Revert checks

**The runner** is `app/build/lf/revert.py`, with its specs `f1-checks.json`, `f2-checks.json` and `f4-checks.json`
(not committed). For each check it:
1. saves a copy of the file;
2. applies one edit, which must match exactly once;
3. clears the results;
4. runs `LeavingTheJournalFixesTest` with `--offline` and `LC_ALL=C.UTF-8`;
5. refuses the results on any `e: ` line;
6. requires the exact predicted failing set, each message containing its fragment;
7. restores the file from the saved copy, never from git, compares it byte for byte, and prints `git status`.

**Every check compiled** (0 `e: ` lines in each log) and **every file was restored byte-identical.** The F2 and F4
checks ran with their forward change still uncommitted. Afterwards the working tree listed the same modified files,
and each forward line was still present (grep count 1).

| Check | Edit | Failed | Verdict |
|---|---|---|---|
| R1a | the wrapper offers for any open entry (`keptDraftId = left?.id`) | 5 of 8: the five no-snackbar tests | confirmed |
| R1b | Discard ignores whether the id is still a draft (`if (true)`) | 1 of 8: the saved-new-find test | confirmed |
| R1c | `leaveKeepsDraft` ignores unchanged re-edits (`left.isDraft`) | 2 of 8: the two unchanged-editor tests | confirmed |
| R1d | `leaveKeepsDraft` is `false` | 3 of 8: the two changed-editor guards and the new-find test | confirmed |
| R2a | `CartographyScreen`'s mode back to a local `remember` | 5 of 14: the five round-trip tests | confirmed |
| R2b | the scaffold does not pass `cartographyEntryModeState` | the same 5 of 14 | confirmed |
| R2c | `onCloseEntry` merges a dirty entry again | 1 of 14: the close test | confirmed |
| R4a | the dropdown's handler without `!isDrawerOpen()` | 3 of 21: the three dropdown tests | confirmed |
| R4b | the Map tab's handler without `!isDrawerOpen` | 3 of 21: Set on map, the Log a find picker, the landscape menu | confirmed |

**Why each failure belongs to its own edit.**
- R1b fails only the one test whose Discard is tapped after a save.
- R1c fails only the unchanged-editor cases. The viewed-find cases are not drafts, so they still show nothing.
- R2c fails only the one test that closes a dirty entry.
- R4a and R4b split the six F4 tests exactly by which handler owns each state.

## Suites

All runs were from a cleared results directory, with `--offline` and `LC_ALL=C.UTF-8`, and 0 `e: ` lines. Counts are
read from the JUnit XML.

| Run | Tree | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| Baseline | `18cc8bb`, in a temporary detached worktree (since removed) | 282 | 2269 | 0 | 0 | 24 |
| After F1 | `b5359cd` | 283 | 2277 | 0 | 0 | 24 |
| After F2 | `b91a543` | 283 | 2283 | 0 | 0 | 24 |
| After F4 | `0b4fc9c` | 283 | 2290 | 0 | 0 | 24 |

**Class by class, baseline against `0b4fc9c`:**
- the one new class is `LeavingTheJournalFixesTest`, 21 / 0 / 0 / 0;
- no class is missing;
- no other class's counts changed.

**The held family** passed on the final run: `JournalPendingDeleteTest` 52 / 0 / 0 / 0 and `JournalTabTest`
17 / 0 / 0 / 0. One green run says nothing about their flake rate.

**Planner prediction 4 (the suite grows by 20 to 45):** +21, with F3 unbuilt.

## F3: the question

**F3 as dispatched:**
- the tab handler no longer closes a find (`AvailabilityCompactScaffold.kt:471-473`);
- the find's own mode and M1's `FindOverView` survive the return, as F2 did for day entries (`JournalTab.kt:289`,
  `:319`);
- backgrounding no longer closes it (`:402`, `:444-446`).

All of that is mechanical, and I can build it.

**What it opens.** Today, leaving the Journal always closes a find, so every route that opens a find starts with none
open on the compact layout. With F3, a find can be open (viewed, or in its editor with a draft) while the user is on
Maps. Two Maps routes then open another find over it:
- "Log a find": `onLogFindHere` (`AvailabilityCompactScaffold.kt:748`) calls `onStartLogEntry`, and
  `MushroomLogViewModel.onStartNewEntry` replaces `editingEntry` with a new draft.
- A find bubble's "Open in Journal": `onOpenFind` (`AvailabilityScreen.kt:1119`) calls `onOpenLogEntry`, which
  replaces `editingEntry`.

Neither route runs the leave on the find it replaces. No committed data is lost either way, but the replaced find's
draft ends up in one of two states:
- **a changed re-edit or a new find:** its row stays stored, but it is missing from the in-memory Drafts list until
  the next `loadEntries`;
- **an unchanged re-edit:** its copy stays stored as a duplicate draft, which the leave would have deleted.

**The two precedents disagree:**
- M1's entry-map "Open find" leaves the open find first (`JournalTab.kt:546`, `leaveFindEditingIfNeeded()`).
- The wide layout's "Log a find" replaces without a leave (`AvailabilityScreen.kt:1458`). On that layout this already
  happens at base, because `LogPanel` stays composed.

**Options:**
- **(a)** Before either compact route opens its find, leave the open one through the F1 wrapper, as the entry map's
  "Open find" does. The unchanged copy is deleted, a changed draft lands in Drafts, and "Saved to Drafts" is offered
  only for a real draft.
- **(b)** The same leave, but silent: the raw callback, no snackbar.
- **(c)** No leave, as the wide layout does today.
- **(d)** (a) or (b), and the wide layout's two routes too. That goes beyond F3's text, though `AvailabilityScreen.kt`
  is in scope.

**Lean:** (a), for the compact routes. I have not chosen, and nothing of F3 is built.

## Findings

- **On the Journal, Back does not close the search dropdown**, with no drawer involved. A probe showed this at
  `b91a543`, and the same probe with the F4 edits reverted to `HEAD` (restored from saved copies afterwards) showed the
  same: after Back the dropdown was still shown, and the Maps tab was not selected. On Maps, Back closes it. I did not
  find which step keeps it open. I checked no Journal handler that could be enabled on Entries. The finder cannot tell
  "never closed" from "closed and reopened". This is outside F4's question. Reported, not fixed.
- **Short landscape, the Log a find picker:** a real touch at the Tools item's centre closed the picker and opened no
  drawer. Something of the picker sits over the rail there. I did not look into it.

## Device-only

For the next device check, on the S22 in portrait and in short landscape at 90 and 270. Robolectric sent Back through
`onBackPressedDispatcher`: no real key, no gesture, no predictive back.

1. **F1.** Open a committed find (report), then each of Maps, Tools, and home plus return. Expected: no "Saved to
   Drafts" anywhere, and the find is still in the gallery.
2. **F1.** Open a find's editor and change nothing, then Back. Expected: no snackbar. Change a field, then Back.
   Expected: "Saved to Drafts". Tap Discard. Expected: the change is gone and the find is intact, with its photos.
3. **F1.** Start a new find, type, Back, then within 4 s open it from Drafts and Save, then tap Discard. Expected: the
   saved find stays.
4. **F1 on a medium or expanded window** (the wide drawer), steps 2 and 3. Not available on the S22 in portrait. A
   tablet or a freeform window would do.
5. **F2.** Open a day entry, Edit, type, then Maps and back. Expected: the editor, with the text. Back. Expected:
   "Save your changes?"; take Discard once and Save once.
6. **F2.** The same with a waypoint withheld, and with a new entry (expected: "Finish entry" on return).
7. **F2.** Leave a dirty editor, toggle night mode (an Activity recreation), and return. Expected: the editor. The
   saveable mode covers this, but no test ran it.
8. **F4.** For each of the dropdown (Maps, Journal), "Set on map", the Log a find picker, and in landscape the
   add-action menu: open it, tap Tools, press Back. Expected: only the drawer closes. On Maps, a second Back closes
   the state.
9. **Journal dropdown and Back** (Findings): with no drawer, does Back close it on the phone?

## D58

`app/build/lf/d58.sh` checks, before each push:
- `git diff 18cc8bb`;
- the staged and untracked files;
- every commit message since `18cc8bb`;
- the pending message file.

It uses the three phrases from `prompts/preserved/2026-09-28-03.md`. The result was 0 each time, and it is checked
again for this commit.

## Decisions I made

- **Record steps.** My agent definition asks for a sweep, an intent and a terminal. The kit is absent at this base
  (`e136330`), and the dispatch says the planner writes the record. I followed the dispatch and touched nothing in
  `RECORD.md`, `prompts/` or `docs/audits/README.md`.
- **No structural validation** of the dispatch: no `.claude/kit.json` exists at this base.
- **The order F1, F2, F4, then stopping at F3.** I built the three fixes the question does not touch, rather than
  stopping everything when I found it. Deciding this properly needed a ruling on whether an abort condition met in one
  fix stops the others.
- **F1's rule is "the leave keeps a draft"**, which is the ViewModel's own rule, shared, rather than the investigation's
  O1.1 (`isDraft` alone), which would still offer the snackbar after an unchanged editor. With it, a brand-new find
  left untouched still gets "Saved to Drafts", because its draft row really is kept. I read ruling 1 ("only when a
  draft was really saved") as covering that case. The dispatch's "leaving an editor with no change shows no snackbar"
  I read as the committed find's editor.
- **The Discard-time guard** (the id must still be a draft) comes from the tests-first run, not from the dispatch. It
  follows ruling 1's "Discard can only ever delete that draft". When it refuses, it logs a warning.
- **The UI decides, from the ViewModel's own function,** rather than making the leave report its result, which would
  mean changing the callback type `MainActivity` passes (out of scope). The cost: in the rare case where a photo
  attach changes the open find between the tap and the ViewModel's locked read, the two could disagree. The Discard
  guard keeps that from deleting a committed find.
- **Where F2's mode lives:** in `AvailabilityScreen`, as a `rememberSaveable` beside `journalScreenState`, not in
  `JournalScreenState` (not in scope, and its doc comment names `JournalTab`'s mode as deliberately transient).
  Choosing saveable over plain `remember` is my extension to Activity recreation.
- **`onCloseEntry` logs at info level**, not as a warning, since the prompt's Save reaches it dirty on a device as a
  matter of course.
- **The `onCloseEntry` test calls the ViewModel directly**, after typing through the real editor, because once F2's
  mode fix is in no known screen route reaches it with a dirty entry. That is an exception to "the real entry point".
- **The F1 tests leave the editors by system Back, not by a tab change,** so that they still hold once F3 stops the tab
  change leaving a find. Backgrounding with a viewed find already shows no snackbar at base (the observer calls the raw
  callback), so no tests-first test was possible there. I left it to F3's backgrounding test, which is not built.
- **Wide coverage for F1** is the editor cases at `w1280dp-h900dp`, M1's wide window. A committed find in its report on
  the wide layout (M1's overlay) is not tested; it goes through the same wrapper.
- **The two test corrections** above, and the three by-design passing tests.
- **The baseline run**, in a temporary detached worktree, removed with `--force` afterwards.
- **The probes** were run and deleted uncommitted; their output is quoted here.
- **Comments in production code**, beside each change.

## Flags outside scope

- **`JournalScreenState`'s doc comment** ("What is not here, on purpose": `JournalTab`'s `mode`) now reads against F2
  in spirit. The day-entry mode is hoisted beside it, not into it. I did not edit that file.
- **The wide `LogPanel`** gets F1 (the shared wrapper) but not F2. Its `CartographyScreen` keeps the local default, so a
  drawer-panel change there would still reset a day entry's mode. That is unverified; plan stage J6 covers `LogPanel`.
- **The Journal search dropdown's Back** (Findings).
- **The short-landscape picker under the rail** (Findings).
- **The investigation's unverified item:** whether a day entry that kept a find is affected when that find is deleted.
  F1 closes the path by which a committed find was deleted, so this was not examined.
