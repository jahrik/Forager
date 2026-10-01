# Back closes the Tools drawer over the Journal: completion report

Intent `2026-09-28-28`, dispatch `prompts/preserved/2026-09-28-28.md`. Base `origin/journal-redesign` at
`05424b6`, checked against the remote before starting. Worktree `forager-wt/drawer-back`, branch
`drawer-back`, pushed to `journal-redesign`. The kit is gone at this base, so I wrote no `RECORD.md`
entry. The planner writes the terminal.

The owner's report, verbatim: "when the tool drawer is open while you're viewing an opened journal
entry, hitting the back button navigates the journal behind the tool drawer and not the drawer itself.
The intended action is to close the drawer while it's open."

## Verification

### The mechanism

The planner's hypothesis about how this happens is **confirmed**. One correction to how it was worded:
what decides is the order in which handlers were *registered* with the dispatcher, and not the order in
which they are composed on a given frame. A `BackHandler` registers once, when it first enters
composition.

- `AvailabilityScreen`'s handlers register when the screen first composes. That includes the drawer's
  handler, `BackHandler(enabled = isDrawerOpen)` (`AvailabilityScreen.kt:982` at base).
- The Journal's handlers register later: when the Journal tab, or a branch inside it, enters
  composition.
- So whenever one of the Journal's handlers is enabled, it takes Back ahead of the drawer.

Each of revert checks rv2 to rv5 (below) turns exactly one Journal handler back on, and in each case
that handler takes Back from the drawer.

### Every enabled handler while the drawer is open (file:line at `05424b6`)

- **Top level.** `AvailabilityScreen.kt`:
  - `:982`, the drawer's handler. On while the drawer is open.
  - `:985`, `:989`, `:1025` and `:1051`. All four have `!isDrawerOpen` in their condition, so they are
    off while the drawer is open.
- **The drawer's own content.**
  - `AvailabilitySettingsUi.kt:581`, the Settings panel (`showSettings`).
  - `:279` and `:282`, the crash-logs and diagnostics panels inside it.
- **The compact scaffold.** `AvailabilityCompactScaffold.kt:353`, the search dropdown. This is not a
  Journal handler.
- **The Journal.** Nine handlers:
  - `JournalTab.kt:345`: a find's back stack.
  - `JournalTab.kt:362`: Records steps back to Entries.
  - `JournalTab.kt:624`: the short-landscape search header.
  - `RecordsTab.kt:215`: a single-type chip steps back to All.
  - `CartographyScreen.kt:215`: an open day entry. The hypothesis cited `:201-205`, which is
    `requestLeaveEntry`, the function this handler calls.
  - `CartographyScreen.kt:373`: the drafts list.
  - `CartographyScreen.kt:403`: the album view steps back to the timeline.
  - `CartographyEntryReportScreen.kt:297`: the entry map in fullscreen.
  - `CartographyEntryEditScreen.kt:188`: the pull-photo picker.

### Which handler takes Back today, state by state

I checked this with a probe class at base. It ran as 6 tests, 0 failures, and was not committed. Each
probe opened the drawer with a real touch on Tools, pressed Back through the Activity's dispatcher, and
printed the state.

- **Day entry, report view: affected.** `CartographyScreen.kt:215` takes Back.
  - Printed: `after Back: editing=null drawerOpen=true entriesHome=1`.
  - The entry closes and the drawer stays open.
- **Day entry, editor (no change made): affected.** `CartographyScreen.kt:215` takes Back again.
  - Printed: `editing=null drawerOpen=true editorField=0`.
- **Find, report view and editor: not open when the drawer is.** Tapping Tools closes the find itself.
  - The tab handler (`AvailabilityCompactScaffold.kt:457-459`) treats Tools as leaving the Journal. Its
    comment at `:455-456` says so.
  - Printed: `find-report after Tools: editing=null backToLog=0 savedToDrafts=true drawerOpen=true`.
    The find editor printed the same.
  - Back then goes to `RecordsTab.kt:215`, and the drawer stays open.
  - Printed: `drawerOpen=true recordsSelected=true`. The Finds chip is no longer selected.
  - **So the planner's prediction 1 is wrong for finds.** A find cannot be open while the drawer is. On
    the finds side, what the bug affects is Records and its chip.
- **J5c details sheet: not reachable, inferred and not run.** It is a `ModalBottomSheet`
  (`RecordDetailsSheet.kt:163`). According to its doc comment (`:64-65`), its scrim covers the bottom
  bar and the rail, so Tools cannot be tapped while it is up.
- **Album photo viewer: not reachable, inferred and not run.** The viewer is `PhotoViewerDialog`
  (`EntriesAlbum.kt:181` calls `PhotoViewerDialog.kt:129`). It is a `Dialog` in its own window over the
  nav bars (its doc comment, `:77-90`). The album view behind it *is* affected: with the viewer closed,
  `CartographyScreen.kt:403` takes Back. The failing tests below show this.
- **The wide layout's drawer: not affected.** It is a `PermanentNavigationDrawer`
  (`AvailabilityScreen.kt:1656`). It never closes, so there is no drawer for Back to close. The Journal
  there is `LogPanel`, inside that drawer.
  - `isDrawerOpen` has one writer on that width, though: see Flags.

## The failing tests and their messages

`app/src/test/java/com/zynergylabs/forager/app/ui/availability/DrawerBackOverJournalTest.kt`, 6 tests.

**The fixture** is `LeavingTheJournalInvestigationTest`'s from `leave-journal-investigation` at
`a89b240`, copied:

- the real `AvailabilityScreen`;
- the real `MushroomLogViewModel` and `CartographyViewModel`;
- an in-memory `ForagerDatabase`.

The private stubs are renamed `DrawerBack*`, so the two files can sit in one package.

**How each test runs:**

1. It opens the state.
2. It opens the drawer with a real touch at the centre of the Tools nav item: the bottom bar in
   portrait, the rail in `w823dp-h384dp-land`.
3. It checks the drawer is open ("Trip Planner" is displayed, as in
   `AvailabilityScreenBackNavigationTest:363-367`), and checks the state is still there behind it.
4. It presses Back through `composeRule.activity.onBackPressedDispatcher`.
5. It asserts two things in one message: the drawer is closed, and the state is unchanged.

The two day-entry checks then press Back again. That second press must close the entry, so it shows the
Journal's handlers are working again once the drawer is closed.

**Results at `ea19a5b`:** 6 tests, 6 failures. Each failed at the Back under test, so every precondition
had passed.

| Test | Message at `ea19a5b` |
|---|---|
| portrait, Back with the drawer open over a day entry's report view closes the drawer and keeps the report | `after Back with the drawer open: expected the drawer closed and the day entry open in its report view held; drawer open = true, Journal state held = false` |
| portrait, … over a day entry's editor closes the drawer and keeps the editor | `… the day entry open in its editor held; drawer open = true, Journal state held = false` |
| short landscape, … from the rail over a day entry's report view … | `… the day entry open in its report view held; drawer open = true, Journal state held = false` |
| short landscape, … from the rail over a day entry's editor … | `… the day entry open in its editor held; drawer open = true, Journal state held = false` |
| portrait, … over the Finds chip on Records closes the drawer and keeps the Finds chip | `… Records with the Finds chip selected held; drawer open = true, Journal state held = false` |
| portrait, … over the Entries album view closes the drawer and keeps the album | `… the Entries album view held; drawer open = true, Journal state held = false` |

**Why every failure is the stated one:** the drawer stayed open, and the Journal state changed.

## What landed

| SHA | What |
|---|---|
| `ea19a5b` | Tests first: `DrawerBackOverJournalTest`. 6 tests, 6 failing (above). |
| `a65176a` | Merge of the planner's docs (`789c602`: M1 intent and pulse), after my push was rejected. No conflict. |
| `42d9d3d` | The fix. |
| this report's commit | This file. |

**The fix: one flag, `backEnabled`.**

- It is a new `JournalTab` parameter, defaulting to `true`.
- `CompactMainScaffold` passes `!isDrawerOpen()`
  (`AvailabilityCompactScaffold.kt`, at the `JournalTab` call).
- It is threaded to all nine Journal handlers:
  - `JournalTab` (3);
  - `RecordsTab` (1);
  - `CartographyScreen` (3);
  - `CartographyEntryReportScreen` (1);
  - `CartographyEntryEditScreen` (1).
- Each of those handlers is now `enabled = backEnabled && <its old condition>`.
- I also added a comment above the drawer's handler in `AvailabilityScreen.kt`.
- The production diff is 7 files, +42 / −9.

**Why this mechanism** (turn the Journal's handlers off, instead of outranking them): a drawer handler
that outranks them would have to register after them, for example by composing it only while the drawer
is open. It would then also outrank the drawer's own Settings step (`AvailabilitySettingsUi.kt:581`).
That step registers with the drawer's content, before the Journal's handlers. With it outranked, Back
from the drawer's Settings panel would close the whole drawer. That would change what the drawer does,
which is out of scope, and it is the design question listed as an abort condition.

Turning the Journal off leaves every one of the drawer's own handlers exactly as it is. A probe with the
fix confirmed this:

- The drawer's Settings panel was open over a day entry.
- The first Back returned to the Tools panel, and the entry was intact:
  `after Back 1: tripPlanner=true entry=day-1`.
- The second Back closed the drawer, and the entry was still intact:
  `after Back 2: tripPlanner=false entry=day-1`.

I did not probe the Settings panel at base.

**Unchanged:**

- **Back with the drawer closed.** The flag is `true` whenever the drawer is closed.
- **The wide tree.** `LogPanel` passes nothing, so it gets the default.
- **The Journal hosted on its own in tests.** Same reason.

**Planner predictions:**

1. **Held for the day entry, in both views. Wrong for finds** (see Verification).
2. **Held.** One enabled flag, threaded to the Journal's handlers. The dispatch's `enabled` idea, with
   no new mechanism.
3. **Held.** No existing test file changed.

**After the fix, the new class and the regression classes:**

| | |
|---|---|
| Classes run | `DrawerBackOverJournalTest`, `AvailabilityScreenBackNavigationTest`, `AvailabilityScreenJournalShortWindowTest` (the J5 short-window Back tests), `CompactToolsDrawerTest`, `AvailabilityScreenJournalEntriesStateTest`, `RecordsFilterChipsTest`, `JournalEntriesTest`, `JournalShortWindowCardsTest`, `RecordDetailsSheetTest` |
| Result | 9 classes, 159 tests, 0 failures, 0 errors, 0 skipped |
| Compile errors in the log | none |

## The revert check

**The runner** is `app/build/drawer-back/revert.py` with `run.sh` and `tally.py`, kept in this
worktree's build directory. For each check it did this:

1. Saved a copy of the file.
2. Applied one edit, and would have refused the edit unless its text occurred exactly once.
3. Cleared `test-results`.
4. Ran `DrawerBackOverJournalTest` with `--offline` and `LC_ALL=C.UTF-8`.
5. Would have refused the results if the log had any `e: ` line.
6. Restored the file from the saved copy, never from git, and compared it byte for byte.
7. Printed `git status --short`.

**Every check compiled:** 0 `e: ` lines in every log. **Every file was restored byte-identical**, and
`git status` was clean each time.

**The forward change was still there afterwards.** HEAD was `42d9d3d`, and `backEnabled` was still
present in all six files.

| Check | Edit | Failed | Why these failures belong to this edit |
|---|---|---|---|
| rv1 | scaffold: `backEnabled = !isDrawerOpen()` becomes `true` | all 6, each with `drawer open = true, Journal state held = false` | It turns every Journal handler back on. |
| rv2 | `CartographyScreen` album handler: drop `backEnabled &&` | 1: the album test only | Only the album-to-timeline step is back on. |
| rv3 | `RecordsTab` chip handler: drop `backEnabled &&` | 1: the Finds chip test only | Only the chip-to-All step is back on. |
| rv4 | `CartographyScreen` open-entry handler: drop `backEnabled &&` | 4: the two portrait and two landscape day-entry tests | Only the entry handler is back on. |
| rv5 | `JournalTab` Records-to-Entries handler: drop `backEnabled &&` | 1: the Finds chip test only | `RecordsTab`'s handler is still off, so `JournalTab`'s takes Back and steps Records to Entries. |

**What the revert checks cannot show:** five threaded handlers have no test that needs them, so no
revert check covers them:

- `JournalTab.kt:345`, a find's back stack. A find closes when Tools is tapped.
- `JournalTab.kt:624`, the short-landscape search header.
- `CartographyScreen.kt:373`, the drafts list.
- `CartographyEntryReportScreen.kt:297`, the entry map in fullscreen.
- `CartographyEntryEditScreen.kt:188`, the pull-photo picker.

Each got the same one-token edit as the handlers that are tested.

A side note from a later probe, not a revert check: its first build failed to compile, because of
private stubs declared twice across two test files. The runner refused those results. I renamed the
stubs and ran the probe again.

## The suite

Both runs started from a cleared results directory and used `--offline` and `LC_ALL=C.UTF-8`, with 0
`e: ` lines. Counts are read from the JUnit XML.

| Run | Tree | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| Baseline | `05424b6`, in a temporary detached worktree (since removed) | 267 | 2194 | 0 | 0 | 24 |
| After | `42d9d3d` | 268 | 2200 | 0 | 0 | 24 |

**Class by class:**

- the only new class is `DrawerBackOverJournalTest`, at 6 / 0 / 0 / 0;
- no class is missing;
- no class's counts changed.

**The held flaky family** passed on the after-run:

- `JournalPendingDeleteTest`: 52 / 0 / 0 / 0;
- `JournalTabTest`: 17 / 0 / 0 / 0.

A single green run says nothing about how often they flake.

**Memory:** about 3 GB was available throughout. There was no OOM, and I killed no daemon.

## Device-only

For the next device check. It needs the S22 in portrait and in short landscape at both rotations, 90 and
270, because the rail and the drawer sit on opposite sides at those two rotations.

**Why these are device-only:** Robolectric sent Back through `onBackPressedDispatcher`. It did not send
a real key or gesture. Predictive back and the gesture-navigation back swipe were not exercised.

1. **Day entry, report view.** Open a day entry, tap Tools, press Back.
   - Expected: the drawer closes and the report stays.
   - Press Back again. Expected: the entry closes.
2. **Day entry, editor.** Same steps from the day entry's editor, with no change made. Then again with
   an unsaved change.
   - The change case was not tested headless. Expected: the first Back only closes the drawer; the
     second Back raises the leave prompt.
3. **Records, Finds chip.** On Records with the Finds chip selected, tap Tools, press Back.
   - Expected: the drawer closes and the Finds chip stays selected.
4. **Entries album view.** Tap Tools, press Back.
   - Expected: the drawer closes and the album stays.
5. **The drawer's Settings panel over an open entry.**
   - Press Back once. Expected: back to the Tools panel.
   - Press Back again. Expected: the drawer closes, and the entry is untouched.
6. **The states not tested headless.** For each, open it, tap Tools, press Back, and expect only the
   drawer to close:
   - the entry map in fullscreen;
   - the drafts list;
   - the short-landscape search header when it is brought up;
   - the day entry editor's pull-photo picker.
7. **In each case above, with the drawer closed,** Back does what it did before.

## Decisions I made

- **I went ahead although prediction 1 was wrong for finds.** My judgement was that this did not change
  the fix, so the abort condition was not met.
  - Instead of the dispatch's find tests ("the entry is still open"), I tested what Back reaches on the
    finds side: the Finds chip on Records. The dispatch's version cannot pass at base, and it would not
    pass after the fix either, without the out-of-scope "finds staying open" change.
  - Deciding this properly needed a planner ruling on what the find cases should assert.
  - The tests deliberately do not pin "Tools closes a find", so M1's Leaving-the-Journal work is not
    tied to it.
- **I added tests the dispatch did not list:** the Entries album view, and short landscape for both
  day-entry views.
- **Which handlers get the flag.** All nine Journal handlers, following step 3's "every Journal
  handler". Five of them are untested (see the revert section).
  - I did not thread the flag into the search-dropdown handler (`AvailabilityCompactScaffold.kt:353`)
    or the Map tab's handler. They are not Journal handlers. See Flags.
- **The mechanism and its name.** Turning the handlers off rather than outranking them, for the reason
  given above. The name is `backEnabled`.
  - A consequence I chose knowingly: over the Journal, Back from the drawer's Settings panel now steps
    back to the Tools panel, as the drawer's own handler does. That is also what the drawer does over
    Maps; I infer this from the registration order and did not run it.
  - I did not probe what that press did over the Journal at base. I infer the Journal took it.
- **How "the drawer is closed" is detected:** "Trip Planner" not displayed, the proxy
  `AvailabilityScreenBackNavigationTest` already uses. Each open is checked first with
  `assertIsDisplayed`.
- **A second Back in the day-entry tests**, to show the Journal's handlers come back once the drawer is
  closed.
- **Tools is touched at its centre only.** This is setup for the tests, not what they claim, so I did
  not sample several points on the target.
- **Probe classes, deleted.** Three were run and deleted uncommitted: the base behaviour probe, the
  Settings-panel probe, and its first build, which did not compile. Their output is quoted in this
  report.
- **The baseline run.** I ran it in a temporary detached worktree and then removed that worktree with
  `--force`, deleting its build output. The dispatch did not ask for a baseline; `2026-09-28-03`'s
  environment section does.
- **The fixture is copied, not shared** with `LeavingTheJournalInvestigationTest`. That class is not on
  `journal-redesign`.
- **Kit record steps not done.** My agent definition's sweep, intent and terminal steps assume a kit
  that is gone at this base. I followed the dispatch instead: the planner writes the record.
- **Two comments in production code:**
  - above the drawer's handler in `AvailabilityScreen.kt`;
  - at the scaffold's `JournalTab` call.

## Flags outside scope

- **Opening the drawer over a viewed committed find offers the Behaviour 1 Discard.** With a find open,
  tapping Tools closes it and shows "Saved to Drafts" with Discard. The probe printed
  `savedToDrafts=true`.
  - Per `docs/audits/2026-09-28-leaving-the-journal-investigation.md`, Behaviour 1, that Discard
    deletes the committed find with no Undo.
  - So the data-loss path is reachable just by opening Tools, not only by changing tab. This belongs to
    the Leaving-the-Journal rulings after M1.
- **The same class of bug may exist on other tabs.** These handlers are registered after the drawer's:
  - the search dropdown's (`AvailabilityCompactScaffold.kt:353`);
  - the Map tab's (`AvailabilityCompactMapUi.kt:483`), for its pending action, location picking and
    action menu;
  - `CompactSettingsTab`'s, if that tab is shown outside the drawer.

  Each would take Back if enabled while the drawer is open. Whether any of those states can coexist
  with an open drawer is unverified.
- **The wide scaffold sets `isDrawerOpen = true`.** Its `onLogFindHere` does this
  (`AvailabilityScreen.kt:1393` at base), although the comment at `:978-981` says the flag stays false
  on that width. This turns the drawer's handler on with no drawer to close. The effect is unverified.
- **The comment at `AvailabilityScreen.kt:961-967`** says nested UI "unwinds itself first". That no
  longer holds for the Journal while the drawer is open. I added a note beside the drawer's handler and
  left that comment itself unedited.
