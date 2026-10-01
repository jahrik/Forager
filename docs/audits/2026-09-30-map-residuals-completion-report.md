# map-residuals completion report (dispatch 2026-09-28-312)

Dispatch file: `prompts/preserved/2026-09-30-20.md`. Branch `map-residuals`, pushed. No device, no adb, no merge into any other branch.

**Two coder sessions did this work, on two models.**
- **The earlier session** (transcript `706b8f15-a065-56d6-a46a-67ea2d07af30`) started at base `1a43ba5c`, the remote head then. Its commits are signed Sonnet 5.5; I did not read its session record. It built Part A and the fullscreen half of item 11, ran a full suite, and lost its API connection at 2026-10-01T01:48Z before reporting anything.
- **The resumed session** (this report's author) ran as **Opus 5.5**: `get_session` read `session_context.model` and `last_served_model` as `claude-opus-5-5`. The owner, relayed by the planner: "It'll stay on Opus for now. Go". Every commit from `38452a6d` on is this session's.

**Base.** `origin/journal-redesign` at `1a43ba5c` when the earlier session started; merged with its head `f8f3b9f5` at `520dd863` (`git pull --no-rebase`, no conflicts). Fetched again before the final suite: still `f8f3b9f5`, "Already up to date".

**What the resumed session found on arrival, and what it did not redo.** The launch prompt assumed a fresh start. The worktree and branch already existed, with `520dd863` unpushed and all run evidence in `/tmp` and one transcript. That evidence was copied into `docs/audits/data/2026-09-30-map-residuals/` unchanged (`38452a6d`; the folder's `README.md` says which files). Where this report cites a run the earlier session made, it says "earlier session". Everything else was run by the resumed session.

## Part A: deletes (items 9 and 10)

### Premises

| Premise | Result |
| --- | --- |
| The flash window, for finds, photos and regions | **Confirmed by a red run** (earlier session, the fix's six files put back to `1df2ee45`, 0 `e:` lines): "find-a is drawn in the commit window expected:<[find-b]> but was:<[find-a, find-b]>", and the same for `photo-a` and region `1`. |
| The in-flight load | **Confirmed by the same red run**: "find-a came back when the late load finished expected:<[find-b]> but was:<[find-a, find-b]>", and the same for `photo-a` and region `1`. |
| The hook lines are covered by no test | **Not re-run at base.** Taken from the -291 and -297 reports. Read: at base both lines sat inside `MainActivity`'s `viewModels` initializer, and no test launches `MainActivity` (the two tests that name it read the manifest and an `Intent`). |
| The cited lines | Read at `1a43ba5c`: `MushroomLogViewModel.kt:730`, `:743`, `:758`, `:1109`, `:1182`, `MainActivity.kt:138` and `:140`, `PendingDelete.kt:62-63`, `AvailabilityScreen.kt:887-891` are all what the dispatch says. |

### Mechanism (the earlier session's; reviewed line by line by the resumed session)

Each ViewModel keeps a set of **committed-delete ids** in its UI state: `committedFindDeleteIds` and `committedPhotoDeleteIds` (`MushroomLogUiState.kt:79`, `:81`), `committedOfflineRegionDeleteIds` (`AvailabilityUiState.kt:182`).
- An id goes in when its record leaves the Undo window, in the state update **before** the one that clears the pending marker (`commitDeleteEntry`, `MushroomLogViewModel.kt:737`; `commitDeleteGalleryPhoto`, `:1178`; `commitDeleteOfflineRegion`, `AvailabilityViewModel.kt:1422`). The record is never in neither.
- It **stays after the delete succeeds**, which is what closes the late load: the stale snapshot goes into `mapRecords`, and the filter still leaves the record out.
- It comes out when the delete **fails**, so the record draws again.
- A restore empties all three sets (`reloadAfterRestore`, both ViewModels), since a restore can bring a deleted record back.
- One filter, `AvailabilityUiState.drawnMapRecords` (`DrawnMapRecords.kt`), replaces the screen's old expression at `AvailabilityScreen.kt:887`. Outside the two ViewModels that write them, it is the only code that reads the three sets (`git grep`), so no Journal list sees them.
- `domain/PendingDelete.kt` is unchanged. `mapRecords` is still a snapshot, not a `Flow`.

**Alternatives rejected** (the earlier session's reasons, from its messages to the planner):
- Removing the record from `mapRecords` before the slot clears: does not close the late load.
- A load guard (a tombstone or a sequence number in `loadMapRecords`): needs start and failure hooks across both ViewModels.
- A "committing" state in `PendingDeleteSlot`: would change what J4's lists read.

**The wiring seam.** The `MushroomLogViewModel(...)` construction moved, unchanged, out of `MainActivity` into `createMushroomLogViewModel` (`MushroomLogViewModelFactory.kt`; the hook lines are now `:54` and `:56`). `MainActivity.kt:102` calls it; `MushroomLogViewModelWiringTest` calls the same function with the real `AppContainer`. Rejected: launching `MainActivity` under Robolectric (never done in this suite; MapLibre and the camera). No dependency-injection change.

### What landed

| Commit | What |
| --- | --- |
| `1df2ee45` | Tests first, and the seam (earlier session) |
| `1ea640d5` | The fix (earlier session) |
| `171c5901` | "Undo draws a photo again", "Undo draws a region again" (the owner: "build them") |
| `055e265c` | The late-load test for the album's delete-at-once path |

### Revert checks

Every check below, unless marked "earlier session": one exact edit applied to a copy-saved file, the results folder deleted first, the build log read for `e:` lines (0 in every one), the file restored from the saved copy and confirmed equal to `HEAD` with a clean tree. Each prediction was written before its run and each was met.

| Check | Edit | Red, and only this |
| --- | --- | --- |
| whole fix (earlier session) | six files back to `1df2ee45` | 6 of 10: the three "commit window" and three "late load" tests, messages above |
| R16 | `onFindDeleted` line removed (`MushroomLogViewModelFactory.kt:54`) | "the snapshot still holds the deleted find: onFindDeleted is not wired expected:<[]> but was:<[wired-find]>" |
| R17 | `onPhotoDeleted` line removed (`:56`) | "the snapshot still holds the deleted photo: onPhotoDeleted is not wired expected:<[]> but was:<[wired-photo]>" |
| R3 | the failure branch's `committedFindDeleteIds - entry.id` removed | "a find whose delete fails is drawn again": "expected:<[find-a, find-b]> but was:<[find-b]>" |
| R4 | the same for photos | "expected:<[photo-a, photo-b]> but was:<[photo-b]>" |
| R5 | the same for regions | "expected:<[1, 2]> but was:<[2]>" |
| R11b | the committed id on the delete-at-once photo path removed | "photo-a, deleted at once, came back when the late load finished expected:<[photo-b]> but was:<[photo-a, photo-b]>" |
| R6, R9, R10 | **outside the fix**: each Undo no longer clears its pending marker | its own "Undo draws a ... again" test |

R16 and R17 are the dispatch's "that line's removal", on the line each hook became. The earlier session ran the same two; only its second log survived.

**The three Undo tests guard behaviour the fix did not change.** No line of the fix runs on an Undo path, and the whole fix reverted left the find one green. R6, R9 and R10 show each is connected to its Undo.

**A gap found by counting, and closed.** The dispatch cites both photo delete paths (`:1109` and `:1182`). The earlier tests covered the pending one only. With the fix's line on the delete-at-once path removed, 150 tests in five classes stayed green (R11). `055e265c` adds the test; R11b shows it bites.

## Part B: Back order (items 11 and 12)

### Premises

| Premise | Result |
| --- | --- |
| The bubble closes before fullscreen and before the dropdown | **Confirmed by test, both red at base.** Fullscreen: "the first Back left fullscreen" (earlier session, pre-fix). Dropdown: "the first Back closed the dropdown [seen: bubble=false dropdown=true fan=true]". |
| With the dropdown in the fan's gate, two of -293's tests fail through a refocus | **Confirmed, and made exact.** It is not the dropdown's close in those two tests. It is the **Tools drawer's** close, which also clears focus, that opens a dropdown (below). |
| On the S22 it does not happen (-305) | Not re-checked; no device. Consistent with what was read: a finger puts the window in touch mode, where the hand-back does not happen. |
| Nobody knows why F4's harness differs | Answered below, part read, part inferred. |

### The focus finding

**Read** (bytecode of the jars the tests run on, and source):
- `View.clearFocus` (SDK 36, `android-all-instrumented` 16): `refocus = sAlwaysAssignFocus || !isInTouchMode()`. `clearFocusInternal` calls `rootViewRequestFocus` only when `refocus` is true. So **out of touch mode, clearing focus hands focus to the first focusable view, inside the same call.**
- Robolectric 4.16.1: a window starts **out of touch mode** unless real graphics are on (`ShadowWindowManagerGlobal`), and the flag is rebuilt for every test.
- The app clears focus itself in two places that matter: the dropdown's close (`AvailabilityCompactScaffold.kt`, the effect on `showSearchDropdown`) and the Tools drawer's close (`AvailabilityScreen.kt`, the effect on `isDrawerOpen`).
- The search field opens the dropdown on any focus gain (`AvailabilitySearchUi.kt:246`).
- MapLibre 13.5.0's `MapView` calls `setFocusable(true)` and `setFocusableInTouchMode(true)`. `SightingsMap.kt` does not change that.

**Observed** (runs):
- The trace (earlier session; `earlier-session-run-outputs.txt`, `part-b-focus-trace`): `onFieldFocused` is called inside the scaffold's `clearFocus` call, one stack: `clearOwnerFocus`, `View.clearFocus`, `clearFocusInternal`, `rootViewRequestFocus`, `AndroidComposeView.requestFocus`, `focusSearch`, `dispatchFocusCallbacks`, `onFieldFocused`. Nothing is posted.
- Out of touch mode, with a map stub that takes no focus: Back closes the dropdown and it reopens at once; closing the Tools drawer opens the dropdown (probe: `dropdown=false`, drawer open `false`, after Back closed the drawer `true`).
- **Who gets the handed-back focus decides everything.** With a stub map that is `clickable` (`BubbleMapSlot`), the same test **passed with no fix**: the focus went to the map. This was a missed prediction of mine, and how the point was found.
- In touch mode none of it happens.

**Inferred, not checkable headless:**
- On a device on the Maps tab the handed-back focus may land on the real map view, in which case a keyboard user never saw the reopen there, and the fix changes nothing visible on that tab. On the other tabs the bar is the first thing on screen and the reopen is more likely.
- **Why F4's harness differs:** F4's stub map holds a `Button` (read, `LeavingTheJournalFixesTest.kt:1171-1175`), which is focusable, so on its Maps tab the handed-back focus has somewhere else to go. F4's own comment records that on its Journal tab, which has no map, Back left the dropdown open. Both fit. The stubs of -293, -298 and `FanBubbleDismissal` take no focus (`pointerInput` only, read). The earlier session's explanation, that the drawer had already taken focus off the field, was not checked.

### The owner's rulings, in order, as the planner relayed them

1. "Option B then": the field ignores a focus gain only while the app's own close is clearing focus, with no timer.
2. "Option B": confirmed again with the finding above and the alternative below in front of the owner.
3. "Yes dropdown should go away first when hitting back. It does not undo my Option A, it fulfills it. I never said to have the fan close before what's drawn on top": the dropdown goes into the fan's and the bubble's Back gates. -303's Option A held the term out only while the harness reopened the dropdown.
4. "Option A", of this dispatch's later three: extend the same flag to the Tools drawer's close.

**The alternative not taken:** making the stub maps focusable, to match the real `MapView`, test-only. It would have kept the gate tests and -293's green with no production change. It would not have covered a tab with no map, where the field does get the focus back, and it would have meant editing fixtures that -293's, -298's and `FanBubbleDismissal`'s unchanged tests use.

### What was built

- **Item 11, fullscreen** (`436efabd`, earlier session): `!isFullscreen` in the bubble's gate (`AvailabilityCompactMapUi.kt:667`).
- **The rule: a focus handed back by the app's own `clearFocus` never opens the search dropdown** (`15a7e8bb`, then `59d7cece`). One holder, `appClearFocusInProgress`, made in `AvailabilityScreen` (`AvailabilityScreen.kt:960`) and passed to the scaffold, is up only while the drawer's or the dropdown's `clearFocus(force = true)` runs. The scaffold's two `onFieldFocused` (the Maps tab's bar and the other tabs') ignore a focus gain while it is up. A focus the user gives the field opens the dropdown as before. `AvailabilitySearchUi.kt` is untouched; its own `clearFocus` on scroll, and `ReturnPromptState`'s, are outside the rule and the holder's comment says why.
- **Rejected:** not clearing focus on close. The field would stay focused, a second tap would gain no focus, and the dropdown would not open again.
- **Items 11 and 12, the dropdown** (`59d7cece`): `!showSearchDropdown` in the scaffold's `renderMode.backEnabled` (`AvailabilityCompactScaffold.kt:941`). The bubble's gate reads that same value, as the fan's handler already did, so there is no new parameter. The comments at both gates and on `MarkerFanOutBackHandler` say what is true now.
- **No production change for the harness's sake.** Touch mode in the test class is set by the test.

### Tests, and the mode each runs in

| Class | Mode | Tests |
| --- | --- | --- |
| `AvailabilityScreenBubbleAndDropdownBackTest` | **touch mode**, set by `Instrumentation.setInTouchMode(true)` before the window is added, with a positive control that the decor view reports it | bubble, then fullscreen (item 11); bubble, then dropdown: dropdown, bubble, fan (item 11); fan, then dropdown: dropdown, then fan (item 12); no fan: Back closes the dropdown and it stays closed; after a Back close a touch on the field opens it again |
| `AvailabilityScreenDropdownCloseOutOfTouchModeTest` | **default, out of touch mode**, with a positive control, and a map stub that takes no focus | Back closes the dropdown and it stays closed, on the Maps tab and on the Journal tab (the scaffold's two call sites); closing the Tools drawer does not open the dropdown |

All real touches on the real screen, Back through the real `OnBackPressedDispatcher`. The fan and bubble tests do **not** run out of touch mode; -293's and -298's unchanged tests do, and cover the drawer and fullscreen orders there.

**Unchanged and green:** -293's `AvailabilityScreenFanBackDrawerTest`, -298's `AvailabilityScreenFanBackOthersTest`, `AvailabilityScreenFanBubbleDismissalTest`, `MarkerFanOutHostTest`, `LeavingTheJournalFixesTest`. `git diff 1a43ba5c HEAD` is empty for all five files.

### The path to it, including what went red

- The earlier session left three of its own tests `@Ignore`d. No `@Ignore` is left: the guard runs in touch mode, and the two gate tests were taken out at `e1c2754b`, then brought back red at `957e3c5e` when the owner ruled on the gates. The bubble one has its first full-order form again.
- Tests were pushed red before each fix: `c988405c` (the dropdown's own close), `957e3c5e` (the gates), `d3e021e9` (the drawer's close).
- **`9f8bfd8c` is a stopped work-in-progress, by instruction.** With the gates in and only the dropdown's close covered, two of -293's unchanged tests were red: "the second Back folded the fan", and "Failed: assertDoesNotExist ... (TestTag = 'map-bubble')". They were not edited. The probe found the drawer's close; the owner ruled; `59d7cece` ends that state.

### Revert checks

Same method as Part A. 0 `e:` lines in every one, each prediction written first and met.

| Check | Edit | Red, and only this |
| --- | --- | --- |
| R1 | `!isFullscreen` out of the bubble's gate | "the first Back left fullscreen" |
| R2 | the dropdown BackHandler's body emptied | the touch-mode guard: "Back closed the dropdown" |
| R2b | `setInTouchMode(true)` removed | every test in the class, on the positive control |
| R7a2 | the flag not read at the Maps tab's `onFieldFocused` | four: my Maps-tab test, my drawer test, and -293's two, each with its own message |
| R7b2 | the flag not read at the other tabs' `onFieldFocused` | my Journal-tab test |
| R15 | the flag not raised at the dropdown's close | my Maps-tab and Journal-tab tests |
| R12 | the flag not raised at the drawer's close | three: my drawer test ("closing the Tools drawer did not open the dropdown") and -293's two |
| R13 | the dropdown term out of the scaffold's gate | **both** gate tests: "[seen: dropdown=true fan=false]" and "[seen: bubble=false dropdown=true fan=true]" |
| R14 | the bubble's gate not reading the scaffold's value | the bubble test only |
| R8 | the flag never lowered (run on the first form of the fix) | every test that opens the dropdown, at its own "the dropdown opened" control |

Two things these do not show:
- **R13 cannot fail the fan test alone.** The bubble's gate reads the same value, by design, so taking the term out turns both red. R14 isolates the bubble's.
- **The reopen test's own last assertion has never been red.** A flag that is never lowered is up from the first composition, so the dropdown never opens at all and the test fails earlier. No natural one-line edit leaves the flag up only after a close.

## Suite counts (from the JUnit XML)

SUITE_COUNTS_PLACEHOLDER

## The four disclosures

**Confirmed versus inferred.** Split in the focus finding above. Elsewhere: the earlier session's whole-fix red run is cited from its saved XML and log, which I read; I did not re-run it, because the screen file has since changed. MapLibre's region ids are `AUTOINCREMENT` (read from the pinned library's schema string); find and photo ids are UUIDs (read).

**Could not determine.**
- Where the handed-back focus lands on a real device, on any tab.
- Whether a keyboard or D-pad user ever saw the reopen, or the dropdown opening when Tools closes.
- Whether the earlier session's wiring test, which polls the real database with `Thread.sleep` for up to 10 seconds, can time out on a slow CI machine. It passed in every run here.

**Premises that were wrong, or decayed.**
- The launch prompt's: a fresh start. The branch, the worktree and an unpushed merge already existed.
- The earlier session's stop message: "Why F4's harness differs: there the Tools drawer has already taken focus off the field". Not shown; the focusable stub is the reading the runs support.
- The planner's, corrected by the planner: that the gate change reverses -303's Option A. The owner: it fulfils it.
- Mine: that the new out-of-touch-mode test would be red with any stub map. It was green with a focusable one.
- A comment now out of date, in a file I may not edit: `LeavingTheJournalFixesTest.kt`, F4's Journal test, says Back with no drawer leaves the dropdown open on the Journal. It no longer does, out of touch mode. The test does not assert it, so it stays green.

**Decided beyond the dispatch's letter.**
- The second out-of-touch-mode test, on the Journal tab, so both `onFieldFocused` call sites are covered.
- The delete-at-once photo test, on a path the dispatch cites.
- The bubble's gate reading `renderMode.backEnabled` in place of a new parameter.
- Unused helpers and imports removed from the earlier session's Part B test file while I was rewriting it.
- R16 and R17 re-run first-hand; the flag's revert checks re-run on the final code.
- Not done: the scaffold file's header lists the parameters "this extraction created"; the new `appClearFocusInProgress` parameter is not added to it, since that list is a record of that build.

**Left as they are, for the owner.**
- **Out of touch mode, after a Back close the field is focused and the dropdown is shut, and a touch on the field does not reopen it**, because no focus is gained (probe, `option-b-probe-output.txt`). The owner was told before ruling. In touch mode a touch reopens it (tested).
- **The album's delete-at-once path still draws the photo until its delete finishes.** There is no pending marker on that path, and the committed id goes in on success. It never comes back afterwards. The rule says "from the moment the user deletes"; this path meets it only once the delete has finished.
- **The taxon suggestions list is not in the Back gates.**
- **Committed ids are kept for the ViewModel's life.** If MapLibre's offline database were ever reset while the app runs, a new region could be given an id still in the set and would not be drawn until a restart. Inferred from the schema; not seen.
- `MainActivity` passes its own `AvailabilityViewModel` to `createMushroomLogViewModel`; no test can see which instance it passes.
- One permission refusal: this session's first push of `520dd863` was refused ("Out-of-Place Publication"). I stopped and handed back; the owner said "Push it".
- One loss: the earlier session's first wiring-revert log was overwritten by its second. R16 replaces it.

## Device-only list (owner, for -319)

1. Delete a find, a photo and a region, and watch the moment Undo ends: no flash.
2. A bubble over a fan, then fullscreen: Back leaves fullscreen, then closes the bubble, then folds the fan.
3. A bubble over a fan, then the dropdown, keyboard up and down: Back hides the keyboard, closes the dropdown, then the bubble, then the fan.
4. A fan, then the dropdown, keyboard up and down: Back hides the keyboard, closes the dropdown and the fan stays; Back again folds the fan.
5. With a hardware keyboard or a D-pad, on the Maps tab and on the Journal tab: open the dropdown, leave touch mode with a key, press Back or Esc. Where does focus go, and does the dropdown stay closed?
6. The same keyboard: after that close, how is the dropdown opened again? In the harness a tap on the still-focused field does not.
7. The same keyboard: open Tools, close it with Back or Esc. Does the search dropdown open?

## Evidence

`docs/audits/data/2026-09-30-map-residuals/`: every log and XML set named above, each revert's edit as a diff, the two runner scripts, the probe outputs, and the earlier session's material with its own `README.md`.
