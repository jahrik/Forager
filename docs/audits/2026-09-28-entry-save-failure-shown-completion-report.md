# Entry save failures shown: completion report (dispatch `2026-09-28-68`)

**Status (updated on resumption): built by continuation `2026-09-28-76`; see "Resumed" at the end.** The sections before it are the stop report as filed at `ea60d72`, unchanged.

**Status at the stop: stopped at verification step 3, before any test or code.** The survey found more than one
established way the Journal surfaces a failed save or delete. The dispatch's abort list includes "no single
established pattern (stop and report the options)", so the options are set out below for the owner, and
none has been chosen. Nothing in `app/` changed. This report is the only file this dispatch has
committed.

- **Intent:** `2026-09-28-68`, launched by continuation `2026-09-28-73` (both written by the planner).
- **Base:** `origin/journal-redesign` at `8dbfd14`. `git diff 99de6c2 8dbfd14 -- app` is empty.
- **Worktree:** `/home/zynergy-labs/Zynergy/forager-wt/save-failure`, branch `save-failure`, created
  with the command in the launch message.
- **Every file:line below is at `8dbfd14`**, read in that worktree. Paths are under
  `app/src/main/java/com/zynergylabs/forager/app/` unless they say otherwise.

## Verification 1: every write and read of Cartography's `saveErrorMessage`

The field is `CartographyUiState.saveErrorMessage` (`ui/log/CartographyUiState.kt:38`).

**Set to a message: 7 sites, 5 distinct strings.** Every one is an existing literal.

| Line (`ui/log/CartographyViewModel.kt`) | Failure | Text |
|---|---|---|
| `:348` | `onFinishEntry` (`:338`), `commitEntry` fails | "Couldn't finish that entry." |
| `:388` | `onSaveEntry` (`:364`), the editor's Save for a committed entry | "Couldn't save your changes." |
| `:430` | `onSaveEntryAsDraft` (`:406`) | "Couldn't save that as a draft." |
| `:467` | `onDiscardEntryChanges` (`:446`), reload from store fails | "Couldn't discard those changes." |
| `:488` | `onDeleteEntry` (`:473`), the report's and editor's own Delete | "Couldn't delete that entry." |
| `:573` | `commitEntryDelete` (`:554`), a pending delete's real delete fails | "Couldn't delete that entry." |
| `:672` | `persist` (`:664`), a draft's autosave fails | "Couldn't save your changes." |

**Cleared (set to `null`): 7 success paths and one dismiss function.**
- The success paths are at `:140` (`onStartEntry`), `:343` (Finish), `:382` (Save), `:424` (Save as draft),
  `:482` (Delete), `:566` (pending delete committed) and `:669` (draft autosave).
- `onSaveErrorDismissed()` at `:650-652` clears it. **It has no caller in `app/src/main`.** `git grep`
  finds `onSaveErrorDismissed` wired only for `MushroomLogViewModel` (`MainActivity.kt:487`).
- `onDiscardEntryChanges`'s success path (`:450-464`) does not clear it.

**Reads: none displays it.**
- `git grep -n saveErrorMessage -- app/src/main` has two reads that could display, at
  `ui/log/JournalTab.kt:300-301` and `ui/log/LogPanel.kt:232-233`. Both read `uiState`, which is
  `MushroomLogUiState` in both files (`JournalTab.kt:130`, `LogPanel.kt:108`). Neither reads
  Cartography's field.
- The only other main hit, `ui/availability/AvailabilityScreen.kt:539`, is a doc comment on the find
  log's parameter.
- Test reads are state-level only: `CartographyViewModelTest.kt:713`, `:735` and
  `CartographyEntryPendingDeleteTest.kt:229`.

So the J8 coder's claim holds for display: nothing shows the message. It is wrong on two details.
- **It is cleared**, on every success path above. What nothing calls is the dismiss function.
- **The second "Couldn't save your changes." is at `:672`**, not `:664`. The line is the same at
  `5874699` (checked with `git show`).

## Verification 2: how the app surfaces comparable failures today

**Pattern A: a Toast, and the field cleared as soon as it has shown.**
1. **The find editor's save failure** (`MushroomLogUiState.saveErrorMessage`).
   - It is set with a literal by `MushroomLogViewModel`: save `:486`, `:518`, `:1010`; discard draft
     `:547`; delete `:722`, `:773`; the photo failures `:815` to `:1149`.
   - It is shown by `LaunchedEffect(uiState.saveErrorMessage)` in two hosts:
     - `ui/log/JournalTab.kt:300-305`, the compact tree's Journal tab. It is composed only in the
       `CompactTab.JOURNAL` branch, `AvailabilityCompactScaffold.kt:959`.
     - `ui/log/LogPanel.kt:232-237`, the drawer's Log panel. It is composed only for
       `DrawerPanel.Log`, `AvailabilityScreen.kt:1381-1382`.
   - It is cleared right after the Toast by `MushroomLogViewModel.onSaveErrorDismissed`
     (`MushroomLogViewModel.kt:1163`), and on the next successful write in the ViewModel.
   - `docs/error-presentation-spec.md` (the per-field table and "`saveErrorMessage` — Toast,
     provisionally") names this treatment. It calls it provisional, with "an inline banner with an
     explicit dismiss control" as the alternative. `docs/audits/2026-08-22-error-presentation-handoff.md:82-83`
     records "dismiss" as "displayed".
2. **J8's failed `shownOnMap` write** (`CartographyUiState.shownOnMapErrorMessage`,
   "Changes not applied. Try again.").
   - It is shown by `LaunchedEffect` in `ui/availability/AvailabilityScreen.kt:1273-1278`, **on every
     tab**, not only in the Journal.
   - It is cleared by `CartographyViewModel.onShownOnMapErrorDismissed` (`CartographyViewModel.kt:646`,
     wired at `MainActivity.kt:520`) right after the Toast. Its success path does not clear it.
   - Its comment (`AvailabilityScreen.kt:1270-1272`) calls it "a Toast like the Journal's own save
     failures".

**Pattern B: inline text on the screen, in the error colour, with no dismiss control.**
3. **Cartography's own failed entry creation**, `CartographyUiState.candidatesErrorMessage`.
   - "Couldn't start a new entry." (`CartographyViewModel.kt:147`) is set when `createEntry` fails.
     `CreateCartographyEntryUseCase` is a `repository.save` of the new draft
     (`domain/CreateCartographyEntryUseCase.kt:21-23`), so this is a failed write of an entry.
     "Couldn't compile that day's report." (`:686`) is set on the same field.
   - It is shown as inline `Text` in `colorScheme.error` at the top of the Entries home,
     `ui/log/CartographyScreen.kt:536-543`.
   - It is cleared when the next trip report starts loading (`CartographyViewModel.kt:683`).
4. **Waypoint save and delete failures**, `TrackRecordingUiState.waypointsErrorMessage`.
   - The texts are "Couldn't save waypoint." (`ui/track/TrackRecordingViewModel.kt:622`) and
     "Couldn't delete waypoint." (`:686`, `:698`).
   - The only display is inline `Text` in `colorScheme.error` in the Journal's Records, Waypoints
     list, `ui/availability/AvailabilityTripsWaypointsUi.kt:201-205` (via `ui/log/RecordsTab.kt:274-276`).
     While it is set, it replaces the list.

**Pattern C: inline text in the neutral colour.**
5. **Offline-region delete failures**, `AvailabilityUiState.offlineRegionsErrorMessage`.
   - The text is "Couldn't delete that region." (`ui/availability/AvailabilityViewModel.kt:1336`, `:1362`).
   - It is shown as inline `Text` with no colour argument in the Records, Offline maps panel,
     `ui/availability/AvailabilityOfflineMapsUi.kt:502-504`.

**Checked and not a failure surface.**
- The shared Snackbar host (`AvailabilityScreen.kt:1159-1206`) carries the pending-delete Undo,
  "Saved to Drafts", and two recording notices. It does not carry a save or delete failure.
- Neither `CartographyEntryEditScreen.kt` nor `CartographyEntryReportScreen.kt` has any error surface
  (`grep -i error`).

## Verification 3: not one pattern, so a stop

For a failed **save** made from an **editor**, every surfaced case is Pattern A: the find editor, and
J8's report-menu write. For a failed save or delete **in the Journal** there are three patterns.
- **A.** Finds, album photos and J8's write.
- **B.** Cartography's own failed creation of an entry, and waypoints.
- **C.** Offline regions.

Pattern A also has **two hosts with different reach**. JournalTab and LogPanel show only while the
Journal is composed. `AvailabilityScreen` shows on every tab.

Reading "this area" as "editor saves" would give one pattern, and reading it as "the Journal" would give
three. That reading is itself the choice the dispatch reserves, and Pattern B is used by a failed entry
write inside Cartography itself. So I stopped.

## Options for the owner (none chosen)

Each keeps the existing strings verbatim and adds no copy.
- **Option 1: Toast, hosted like the find editor's.**
  - A `LaunchedEffect` on `cartographyUiState.saveErrorMessage` in `JournalTab` and `LogPanel`, beside
    the find's. It would call `CartographyViewModel.onSaveErrorDismissed` (`:650`, already present)
    after the Toast, which needs that callback threaded from `MainActivity`.
  - It clears once shown, and on the next successful write (already in the ViewModel).
  - A failure that lands while the Journal is not composed waits, and shows when the Journal next
    shows. For example, the backgrounding save that `persist`'s doc comment names
    (`CartographyViewModel.kt:653-663`), or a pending delete committed after leaving the tab.
- **Option 2: Toast, hosted like J8's.**
  - One `LaunchedEffect` in `AvailabilityScreen` beside `:1273`, shown on whichever tab is up.
  - It clears once shown, and on the next successful write.
  - One host covers both window trees.
- **Option 3: inline text on the editor and report screens, like `candidatesErrorMessage`.**
  - Error-coloured `Text` in `CartographyEntryEditScreen`, and the report if it should show there.
  - It clears on the next successful write only. There is no dismiss control, as in Pattern B, and it
    stays up until something succeeds.
  - Note that `onDiscardEntryChanges`'s success does not clear it (`:450-464`).
  - Where it goes on the screen is a layout decision that has not been ruled.
- **The spec's named alternative** is an inline banner with an explicit dismiss control
  (`docs/error-presentation-spec.md`). A dismiss control needs a label or content description, which
  would be **new copy**, and this dispatch forbids new copy. It cannot be built here without a ruling on
  that text.

**For every option: one field carries all seven failures.** Displaying the field shows all seven texts
in the table above, not only "Couldn't save your changes.". Displaying only some of them would need a
split field or a filter, which is a design question. I have assumed they all show, as the dispatch's
"every write … what failures set it and what text" suggests, but that is not ruled.

## Verification 4: when the message clears

This depends on the option chosen.
- **Options 1 and 2** clear it right after the one-shot Toast, and on the next successful write. The
  success paths already do the second.
- **Option 3** clears it only on the next successful write.
- **None clears it on leaving the editor** today. `onCloseEntry` (`CartographyViewModel.kt:246-270`)
  does not touch the field.

## Predictions

- **Planner prediction 1** ("set in two or three places and read by nothing that displays it"):
  - The count is a **miss**: 7 set sites, 5 texts.
  - "Read by nothing that displays it" **holds**.
- **Planner prediction 2** (suite growth): **not reached**.

## Checks not run

- **No tests, no build, no Gradle run, no suite.** The stop came before the tests-first step. The tests
  depend on the pattern chosen, because they assert that the message is "shown the way the pattern
  shows" and cleared by its rule.
- **No revert check.** Nothing was built.
- **Device-only:** nothing was run. The message as shown on the S22 remains a device item for the build.

## Record

- **`RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` and `prompts/` were not touched**, as
  the dispatch says.
- The coder agent definition I was launched with still describes the kit's sweep and intent protocol.
  The base has no kit: `e136330` (owner) removed `.claude/`, `check_record.py` and `check_prompts.py`.
  I followed the dispatch.

## D58

Before the push I ran `git grep -i` for the three phrases named in `prompts/preserved/2026-09-28-03.md`.
It covered this report, `git diff 8dbfd14`, and every commit message since `8dbfd14`. The result is
recorded in the hand-back.

## Verbatim: dispatch `2026-09-28-68`

The store copy `prompts/preserved/2026-09-28-68.md` at `8dbfd14`, whole, header included:

````text
HEAD: 99de6c2 (journal-redesign) when written. **Queued behind the map-chrome build (-56, -58):** the base at launch is stated in the launch message.
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: build
Preserved: 2026-09-28T20:33:38Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** build

# Role

You are the coder for **showing entry save failures**. This dispatch's intent is `2026-09-28-68`. The kit is gone and the planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Read `CLAUDE.md` first, especially "Errors and failure paths" and Testing.

# The owner's ruling

The planner asked the owner verbatim: "Entry save failures are never shown. This bug is older than J8. When saving a Journal entry fails, the app records the error but never displays it, so the failure is silent. A fix it as a small stage of its own." The owner answered verbatim: "2 A".

# What is claimed (the J8 coder's report, not verified by the planner)

- Cartography's `saveErrorMessage` is never displayed or cleared anywhere.
- The existing string "Couldn't save your changes." is used at `ui/log/CartographyViewModel.kt:388` and `:664` (at `5874699`).
- J8 added a separate field for a failed `shownOnMap` write, shown as a Toast by `AvailabilityScreen` with the owner's wording "Changes not applied. Try again.". Do not touch it.

# Verification before building (file:line at your base)

1. Every write of `saveErrorMessage` (what failures set it and what text) and every read. Confirm that no read displays it.
2. How the app surfaces comparable failures today: J8's Toast, the find editor's save failure, and any other save or delete failure in the Journal. List each with file:line.
3. **If there is one established pattern for a failed save in this area, use it. If there are several or none, stop and report the options for the owner.** Do not choose.
4. When the message should clear: after it is shown once, on a retry, on leaving the editor. Follow the chosen pattern's own behaviour and state it.

# Build

- A failed entry save shows its existing string, verbatim, the way the chosen pattern shows failures.
- Clear it by that pattern's own rule.
- **No new copy.** If a failure that sets `saveErrorMessage` has no existing string, that is a stop.
- Fix nothing else. An error path found elsewhere unshown is a flag, not a fix.

# Tests

- **Tests first**, seen failing at base for the stated reason, through the real entry point: the editor's Save (and Finish, if it shares the field) with a repository that fails. Assert that the exact string is shown, and that it clears as the pattern says. Cover portrait and `w823dp-h384dp-land`.
- **Revert check** under CLAUDE.md's runner rules.
- **Full suite** from a cleared results directory. The baseline is the planner's figure at your base.
- **Device-only, listed not run:** the message as shown on the S22.

# Scope

**In scope:**
- `ui/log/CartographyViewModel.kt` and `CartographyUiState.kt`;
- the editor and report screens, and the host that displays the chosen pattern;
- tests;
- the completion report `docs/audits/<date>-entry-save-failure-shown-completion-report.md`.

**Out of scope:** J8's `shownOnMap` failure message, other ViewModels' errors, and the record and docs above.

# Branch, environment, finish

- **Branch:** a worktree cut from `origin/journal-redesign` at the base the launch message names. Push to `journal-redesign` after each commit that leaves the suite passing, and put broken work on `save-failure-wip`. Merge with `git pull --no-rebase`, never rebase.
- **Environment:** `LC_ALL=C.UTF-8`. Check `df` and memory before building. No phone.
- **D58** before each push.
- **Finish line:** verification, the build, tests first, the revert check, the full suite and the report, all pushed. The planner re-runs the suite and writes the terminal.

# Abort conditions

- no single established pattern (stop and report the options);
- a failure with no existing string;
- a tests-first test passing at base;
- a revert build that does not compile;
- a non-held failure;
- new copy;
- disk full or OOM;
- two failed fixes;
- an unruled design question.

# Predictions (planner)

1. `saveErrorMessage` is set in two or three places and read by nothing that displays it.
2. The suite grows by 3 to 8.

# Merge

Not authorised.
````

## Verbatim: the launch message

````text
This is the launch of planner dispatch `2026-09-28-68`: **entry save failures shown.** Its store copy is committed at `prompts/preserved/2026-09-28-68.md` on `origin/journal-redesign`. Read it in full and quote it verbatim in your completion report; the file governs. Also read `RECORD.md` entry `2026-09-28-73`, the launch note.

- **Base:** `origin/journal-redesign` at `8dbfd14`. Its `app/` is identical to `99de6c2`, the J8 terminal; the planner's suite there is `293 / 2374 / 0 / 0 / 24`.
- **Worktree:** `git worktree add /home/zynergy-labs/Zynergy/forager-wt/save-failure -b save-failure origin/journal-redesign`. Confirm HEAD is `8dbfd14`, or a later commit that changes only docs, records or prompts.
- **Push:** to `journal-redesign` after each commit that leaves the suite passing. Put broken work on `save-failure-wip`. Merge with `git pull --no-rebase`, never rebase.

Key points of `-68`, which the file states in full:
- **Verify before building.** Find every write and read of `saveErrorMessage`, and list how the app surfaces comparable failures today, with file:line.
- **If there is not exactly one established pattern for a failed save in this area, stop and report the options for the owner.** Do not choose.
- The existing string, verbatim. **No new copy.**
- Leave J8's "Changes not applied. Try again." as it is.
- Tests first through the editor's real Save with a failing repository, in portrait and `w823dp-h384dp-land`.
- A revert check, the full suite from a cleared results directory, and the report.

**Sharing the machine:**
- A device coder (J8's device check) builds one APK and then uses the phone. You get **no phone and no emulator**.
- Check `df` and free memory before each Gradle run, and wait if under 2.5 GB is available.
- A paused map-chrome stage may resume later and edit other files. Merge with `--no-rebase` if it pushes.

The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.

When you finish or stop, hand back a report: what landed with hashes, verification, evidence, suite counts, the revert check, what was not tested, device-only items, decisions you made and flags.
````

## Decisions I made

- **The stop.** I read the survey as "several patterns", because Pattern B is used for a failed entry
  write in Cartography and Pattern A has two hosts with different reach. Reading "this area" as editor
  saves only would have given one pattern (A), which would still have left its host to choose. The
  dispatch reserves that reading.
- **Record protocol.** I did not do the sweep or write an intent, because the dispatch says the
  planner writes the record and the kit is removed at the base. My agent definition says otherwise.
- **Structural validation.** It was not run against `.claude/kit.json`'s required sections, because that
  file does not exist at the base. The launching worktree's copy (at `faf2f88`) would list sections
  that `-68` does not carry under those names, for example "Base and state", "Closed decisions",
  "Checks" and "Device items".
- **The report's name and push.** I used the completion-report path the dispatch names for a
  stop-at-verification report, and pushed it to `journal-redesign` as a docs-only commit. The J8 coder
  pushed its own stop report the same way.

## Flags outside scope

- **Silent, unlogged fallback.** `onStartEntry`'s `saveEntry(decided).getOrElse { decided }`
  (`CartographyViewModel.kt:133`) drops a failed save of the new entry's decisions without a log or a
  message. That is against CLAUDE.md, "Errors and failure paths" ("no default fallback that isn't
  logged when it fires"). Not touched.
- **`onDiscardEntryChanges`'s success path does not clear `saveErrorMessage`** (`:450-464`). A stale
  message could outlive a successful Discard. This bears on Option 3, and on Options 1 and 2 only if the
  host is not composed when it is set.
- **The spec says all strings go in `strings.xml`** (`docs/error-presentation-spec.md`, Wording rules).
  Every failure string here is a Kotlin literal. Not in scope.

## Resumed (continuation `2026-09-28-76`): built

**Status: built and verified.** Continuation `2026-09-28-76` answered the stop above. The owner chose
option 1 ("Option B": a Toast hosted in the Journal, like the find editor's) and kept every
message. The planner ruled that a successful Discard clears the message, and that `onStartEntry`'s
dropped failure is logged only. Everything in `-76` is built:
- the tests were written first and seen failing for their stated reasons;
- six revert checks were confirmed;
- the full suite ran from a cleared results directory;
- the work is pushed.

An API outage ("EAI_AGAIN") cut the session once, after the threading edit. The planner's resume
message is quoted below. The edit was checked whole with `git status` and `git diff --stat`, then
committed to `save-failure-wip` and pushed before anything else was done.

### Commits

| Commit | What | Where pushed |
|---|---|---|
| `9e2cfde` | The dismiss callback threaded from `MainActivity` to `JournalTab` and `LogPanel`. It is inert: parameters only, nothing reads it. | `save-failure-wip` |
| `ac8f45e` | Tests first: `EntrySaveFailureShownTest.kt` (3 classes, 31 tests) and one `CartographyViewModelTest` test. Failing on purpose. | `save-failure-wip` |
| `fa5aa9e` | The build: the two Toast hosts, the Discard clear, and the log line. | `save-failure-wip`, then `journal-redesign` |
| this report's commit | This section. | `journal-redesign` |

### What changed (file:line at `fa5aa9e`, under `app/src/main/java/com/zynergylabs/forager/app/`)

- **Hosting.** `ui/log/JournalTab.kt:317-322` (compact tree) and `ui/log/LogPanel.kt:246-251` (wide tree)
  each add a `LaunchedEffect(cartographyUiState.saveErrorMessage)`.
  - It shows the message as `Toast.LENGTH_SHORT`, then calls `onCartographySaveErrorDismissed()`.
  - It sits beside the find editor's own effect and takes the same form.
- **Threading.** The dismiss callback runs from `MainActivity.kt:517`
  (`cartographyViewModel::onSaveErrorDismissed`) to the Journal hosts:
  - `ui/availability/AvailabilityScreen.kt:583` (parameter), then `:1442` (to `LogPanel`) and `:1730` (to
    the compact scaffold);
  - `ui/availability/AvailabilityCompactScaffold.kt:265`, then `:1014` (to `JournalTab`);
  - `JournalTab.kt:207` and `LogPanel.kt:191`.

  Nothing else in those files changed.
- **Messages.** All seven existing messages, verbatim, set at `ui/log/CartographyViewModel.kt:352`,
  `:392`, `:434`, `:473`, `:494`, `:579` and `:684` (they were `:348` to `:672` at `8dbfd14`).
  `git diff 8dbfd14 fa5aa9e -- app/src/main` adds and removes no user-facing string. The only new
  string literal is the log line below.
- **Discard.** `onDiscardEntryChanges`'s success path now sets `saveErrorMessage = null`
  (`CartographyViewModel.kt:466-467`).
- **`onStartEntry`.** The failure is now logged with
  `Log.w(TAG, "Couldn't save new entry '${draft.id}' with its day's candidates kept; it opens with them kept, unsaved.", error)`
  (`CartographyViewModel.kt:134-137`). The entry still opens with the candidates kept, and nothing is shown.
- **Doc comment.** `onSaveErrorDismissed` gained one (`CartographyViewModel.kt:656-662`).

### When the message clears (the find editor's rule, now the entries' too)

- **Right after its Toast has shown.** The host calls `onSaveErrorDismissed`. A `Toast` has no dismiss
  callback, so "dismissed" means "shown", as `docs/audits/2026-08-22-error-presentation-handoff.md:83`
  records for the find.
- **On the next successful write.** This covers the seven success paths that already cleared it, plus
  Discard now.
- **Not on leaving the editor.** `onCloseEntry` does not touch it, as before. A message set while the
  Journal is not composed stays in the ViewModel and shows the next time `JournalTab` or `LogPanel`
  composes.

### Tests (`app/src/test/java/com/zynergylabs/forager/app/ui/availability/EntrySaveFailureShownTest.kt`)

- **Harness.**
  - The real `AvailabilityScreen` and the real `CartographyViewModel`, over an in-memory `ForagerDatabase`.
  - The database sits behind `SaveFailureRefusingRepository`, a Room delegate that refuses saves,
    deletes or reads on demand. It records every refusal, and can hold a save until released (a slow
    disk).
  - `MainActivity`'s day-entry wiring is reproduced, including the new callback and the entry's
    `pendingDeleteNotices`.
  - Every action is the user's own, a real touch at the control's centre.
  - Each test first asserts the exact refused call its action made, so a missing Toast cannot come
    from an action that never ran.
- **Windows.**
  - Portrait `w384dp-h823dp-xxhdpi` and short landscape `w823dp-h384dp-land-xxhdpi` go through `JournalTab`.
  - The wide window `w1280dp-h900dp-mdpi` goes through the drawer's `LogPanel`.
- **Every window (9 tests each):**
  1. The editor's Save, then its confirmation: "Couldn't save your changes."
  2. Typing in a draft (autosave): "Couldn't save your changes."
  3. Finish entry: "Couldn't finish that entry."
  4. The return prompt's Save as draft, after a real ON_STOP and ON_RESUME: "Couldn't save that as a draft."
  5. The leave prompt's Discard, with the stored entry unreadable: "Couldn't discard those changes."
  6. The report's Delete, then its confirmation: "Couldn't delete that entry."
  7. The same refusal twice shows twice. This is the user-visible effect of clearing.
  8. A Save still in flight when the user leaves the Journal is refused off screen. Nothing shows,
     the message waits, and it shows once the Journal reopens.
  9. `onStartEntry`'s refused second save is logged: one `WARN` with the refusal's own throwable,
     naming the entry. No Toast, no message, and the new entry still opens.
  - Tests 1 to 6 each assert exactly one Toast, with exactly the text, and the message `null` after it.
- **Compact windows only (2 tests each):** a card's Delete refused when its Undo snackbar ends ("Couldn't
  delete that entry.", the `:579` path).
  - One test ends the snackbar with the Journal showing. The other ends it on the Maps tab, and the
    message shows once the Journal reopens.
  - Card delete is portrait's full swipe and the short window's long-press menu.
  - The wide `LogPanel` has no card delete: `AvailabilityScreen` does not give it
    `onRequestDeleteCartographyEntry`. So the `:579` path is covered in the compact tree only.
- **`CartographyViewModelTest`, one test:** a successful Discard clears a pending message. This is
  tested at ViewModel level, through its public entry points, on purpose.
  - On screen, the Journal's Toast clears the message the moment it shows.
  - The leave prompt that offers Discard is only on screen while the Journal is.
  - So no screen path can reach a successful Discard with a message still pending.
- **Count:** 32 new tests (11 + 11 + 9 + 1). The planner predicted the suite would grow by 3 to 8. That
  was for `-68`'s narrower scope, before `-76` widened it to seven actions, three windows, off-screen
  and log tests.

### Tests first: seen failing for the stated reason

State at the time: the base plus `9e2cfde`'s inert parameters, needed so the tests compile.
- **Run `tf1`:** 4 classes, 54 tests, 32 failed, 0 compile errors.
  - 28 failed for the stated reason:
    - "Toasts shown expected:<1> but was:<0>" (`<2>` for the twice test);
    - "one log line for the refused save … expected:<1> but was:<0>";
    - "and the message is cleared expected:<null> but was:<Couldn't save your changes.>".
  - **The 4 compact card-delete tests failed for a different reason:** "the delete reached the store
    and was refused expected:<[delete entry-saved]> but was:<[]>". The failure did not match the
    prediction, so the test was at fault. My harness had not reproduced `MainActivity`'s
    `pendingDeleteNotices` (`MainActivity.kt:580-606` at `fa5aa9e`), so no Undo snackbar ran and no delete was
    ever made.
- **The fix, in the harness only:** the notice now comes from `cartographyEntryDeleteNotice`, as
  `MainActivity` builds it.
- **Run `tf2`, the two compact classes:** 22 of 22 failed. All 22, including the 4, failed for the
  stated reason.
- **The wide class and the ViewModel test** were not re-run after the fix. Their `tf1` failures were all
  the stated reason. The fix adds a notice built from `pendingDelete`, which is `null` throughout the
  wide tests (they make no card delete), so the list stays empty as before.
- With `fa5aa9e`: run `impl1`, 4 classes, 54 tests, 0 failed.

### Revert checks

Runner: `/tmp/claude-1000/save-failure/revert/revert.py`. For each check it:
- saves a copy of the file and applies one edit whose old text occurs exactly once;
- clears the results and runs the named classes;
- refuses the results if the build log has any `e: ` line;
- reads the XML, keyed by class and test, and counts stale XML;
- restores the file from the saved copy and compares it byte for byte;
- then checks that `git diff HEAD` is empty, so the forward change, committed at `fa5aa9e`, is still
  what the tree holds.

All six checks built with 0 compile-error lines, 0 stale XML files, a byte-identical restore and an
empty `git diff HEAD`. Each one's failures are exactly its predicted set, with messages specific to
its own edit.

| Check | One-line edit | Classes run | Result |
|---|---|---|---|
| R1 | `JournalTab`: `saveErrorMessage?.let` becomes `?.takeIf { false }?.let` | 3 screen classes | CONFIRMED, 20 of 31: every compact Toast test, "Toasts shown expected:<1> but was:<0>" (twice test: `<2>` … `<0>`). The wide tests and the log tests pass. |
| R2 | The same edit in `LogPanel` | 3 screen classes | CONFIRMED, 8 of 31: the wide Toast tests only. |
| R3 | `JournalTab`: `onCartographySaveErrorDismissed()` becomes `Unit` | 2 compact classes | CONFIRMED, 20 of 22: "cleared once shown expected null, but was:<…>", with each message's own text. The twice test fails with "Toasts shown expected:<2> but was:<1>". |
| R4 | `onDiscardEntryChanges`: the `saveErrorMessage = null` line removed | `CartographyViewModelTest` and 3 screen classes | CONFIRMED, 1 of 54: the Discard test, "expected:<null> but was:<Couldn't save your changes.>". |
| R5 | `onStartEntry`: the `Log.w` line removed | 3 screen classes | CONFIRMED, 3 of 31: the three log tests, "expected:<1> but was:<0>". |
| R6 | `LogPanel`: `onCartographySaveErrorDismissed()` becomes `Unit` | wide class | CONFIRMED, 8 of 9: as R3, for the wide tree. |

`-76` asked for at least one check on the hosting and one on the Discard clear. R1 and R2 check the
hosting and R4 the Discard clear. R3, R5 and R6 are extra.

### Full suite

- **Run `full1`, at `fa5aa9e`:** `./gradlew --offline :app:testDebugUnitTest`, from a results directory
  cleared just before. **296 classes / 2406 tests / 0 failures / 0 errors / 24 skipped.**
- **The build log:** 0 `e: ` lines. `:app:testDebugUnitTest` executed; it was not taken from the build
  cache or left up to date. The XML is fresh: the directory was cleared first, and the summed suite
  time is 165 s.
- **Against the planner's base figure** (`293 / 2374 / 0 / 0 / 24`): +3 classes and +32 tests, which are
  the three new window classes and the 32 new tests. The skipped count is unchanged at 24.
- **Merge afterwards:** `3e4ee3e` merged the remote's `fda3158`, `9c7d0a5` and `de22474` (records,
  plans and prompts only). `git diff fa5aa9e 3e4ee3e -- app` is empty, so the suite ran on the same
  `app/` that is pushed.

### Not tested

- **What the phone shows.** `ShadowToast` records that `show()` was called with the text. It says
  nothing about whether the Toast is visible, how long it stays, or whether it can be read on the phone.
- **A real slow disk.** The off-screen case uses a save held open by the test. How long a real write
  can take, and how often it happens after the user has left the Journal, is not known.
- **A Toast raised while the app is in the background.** A failure can land during ON_STOP, for
  example a pending delete committed when the ViewModel is cleared (`onCleared` logs its own failure
  and sets no message). In that case the Toast would come from a stopped activity. Not exercised.
- **The wide tree's card delete.** It does not exist, so its failure cannot be shown there (the pulse's
  gap table, J6's scope).

### Device-only, listed not run

The Toast for each of the seven messages on the S22, in portrait and in landscape at 90 and 270:
legible, on screen long enough to read, and not hidden by the keyboard or the icon cluster.
Also on the S22: a failure raised off screen (a card's Delete, then Maps before the Undo snackbar
ends, on a store that refuses) showing when the Journal opens. A refused write cannot be forced on the
device without code, so this needs a debug hook or a full-disk setup, which is for the owner to decide.

### D58

The three phrases were checked before each push, over the diff since `8dbfd14`, the staged and
untracked files, this report and every commit message since `8dbfd14`. Every push had zero hits.

### Verbatim: planner message `2026-09-28-76` (store copy at `fda3158`)

````text
HEAD: c6e736d (journal-redesign)
Target subagent: the save-failure coder (SendMessage), continuing dispatch 2026-09-28-68 after its stop at ea60d72
Type: continuation
Preserved: 2026-09-28T21:06:34Z by the planner, by hand, time read from the clock.
--- verbatim message follows ---
Planner message `2026-09-28-76`, part of dispatch `2026-09-28-68`. Quote it verbatim in your report. It answers your stop at `ea60d72`. **Build it.**

**The owner's rulings, verbatim:**
- "For the failures, keep the messages as they are. They are specific to the action which is a feature."
- "Option B for your question." Option B, as the planner put it to the owner: "The same pop-up, but only while the Journal is open. A failure that happens elsewhere waits until you next open the Journal." That is your option 1: a Toast hosted like the find editor's.

**What to build:**
- **All seven failures show**, each with its existing message verbatim: `CartographyViewModel.kt:348`, `:388`, `:430`, `:467`, `:488`, `:573` and `:672` at `8dbfd14`. No wording changes.
- **Host** the Toast in `JournalTab` (compact) and `LogPanel` (wide), the way the find editor's is hosted (`JournalTab.kt:300-305`, `LogPanel.kt:232-237`).
- **Dismissal:** call the existing `onSaveErrorDismissed` (`:650-652`) once it has shown, threading the callback from `MainActivity`. The message clears once shown or on the next successful write. A failure raised while the Journal is not on screen shows when the Journal next opens.
- **Planner's ruling:** `onDiscardEntryChanges`'s success path (`:450-464`) clears `saveErrorMessage`, as every other success path does. A stale message must not survive a successful Discard. Record it in the report.
- **Planner's ruling:** log the dropped failure in `onStartEntry` (`CartographyViewModel.kt:133`, `saveEntry(decided).getOrElse { decided }`), following CLAUDE.md's rule that a fallback is logged when it fires. Log only: no UI and no message. The planner told the owner, who did not object.

**Scope** widens to `JournalTab.kt`, `LogPanel.kt` and `MainActivity.kt`, plus any host in between that the callback must pass through. It is for threading and hosting only, with no other behaviour change.

**Tests first**, each seen failing at base for its stated reason, through the real entry points with a failing repository:
- each of the seven actions shows its own message, exactly (a parametrised test is fine);
- the message clears after it has shown;
- a failure raised while the Journal is off screen shows when the Journal next opens;
- Discard success clears a pending message;
- the `onStartEntry` failure is logged and shows nothing.

Cover portrait, `w823dp-h384dp-land` and a wide window for `LogPanel`.

**Everything else in `-68` stands:** a revert check (at least one on the hosting, one on the Discard clear), the full suite from a cleared results directory, D58, the report (append a "Resumed" section to your stop report), and no merge.
````

### Verbatim: the planner's summary of `-76`, as received

````text
This is planner message `2026-09-28-76`, part of dispatch `2026-09-28-68`. **Build it.** The full text is at `prompts/preserved/2026-09-28-76.md` on `origin/journal-redesign` at `fda3158`. Fetch it, read it in full and quote it verbatim; the file governs over this summary.

- **Owner, verbatim:** "For the failures, keep the messages as they are. They are specific to the action which is a feature." All seven messages show, each verbatim.
- **Owner, "Option B":** your option 1, a Toast hosted in `JournalTab` and `LogPanel` like the find editor's, calling `onSaveErrorDismissed` (threaded from `MainActivity`). It clears once shown or on the next successful write. A failure raised off-screen shows when the Journal next opens.
- **Planner's rulings:**
  - a successful Discard clears the message;
  - the dropped failure in `onStartEntry` (`:133`) gets a log line only, with no UI.
- **Scope** widens to `JournalTab`, `LogPanel`, `MainActivity` and the hosts between them, for threading only.
- **Tests first** through the real entry points, in portrait, `w823dp-h384dp-land` and a wide window. Then the revert checks, the full suite and a "Resumed" section in your report.

Check memory before Gradle: two device coders are on this machine. Hand back when you finish or stop.
````

### Verbatim: the planner's resume message after the outage

````text
Planner: resume continuation `2026-09-28-76` of `2026-09-28-68`. Your turn was cut off by a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"). Nothing is wrong with your work, and your context is intact. Your last note was "Now the inert threading (stub) edits in main."

Before your next action:
- run `git status` and `git diff --stat` in your worktree, and check that your last edit is present and whole;
- do not assume your last command completed.

If you have uncommitted work you would not want to lose to another outage, commit it to `save-failure-wip` and push it before continuing ("Push before you tidy"). Then carry on. Before any Gradle run, check that no other Gradle build is running (the map-chrome coder shares the machine) and that 2.5 GB is free. Hand back when you finish or stop.
````

### Decisions I made (resumed)

- **The new parameter's default is `{}`.** `JournalTab`, `LogPanel` and `AvailabilityScreen` take
  `onCartographySaveErrorDismissed: () -> Unit = {}`, so the 7 test files that call `JournalTab` or
  `LogPanel` directly needed no change. This follows the sibling Cartography parameters and J8's
  `onCartographyShownOnMapErrorDismissed`. The cost: a caller that forgets it gets a Toast whose
  message never clears. The one production path passes it, and R3 and R6 catch its loss. The compact
  scaffold, which has only one caller, takes it with no default.
- **Tests first had to compile first.** `9e2cfde` threads inert parameters, which the J8 coder also did
  ("tests first, stubs in main"). So the failing run was at the base plus parameters, not at `8dbfd14`
  itself.
- **Test design.** The planner did not specify how to test, so I chose:
  - one new class file with three window classes, and explicit test methods rather than a
    parametrised runner;
  - the drafts' autosave, typed twice, as the "clears after it has shown" case;
  - a held-open save as the off-screen case in all three windows, plus the card delete on the Maps tab
    in the compact tree;
  - the wide window at `w1280dp-h900dp-mdpi`, as J8 used.
- **The Discard check is at ViewModel level**, for the reason given above.
- **The log line's wording** is mine: a developer log, not user-facing copy.
- **I added a doc comment** to `CartographyViewModel.onSaveErrorDismissed`.
- **Revert checks R3, R5 and R6** go beyond the two `-76` asked for.
- **I fixed my own harness after `tf1`** (the missing `pendingDeleteNotices`) and re-ran only the compact
  classes to see the four fail for the stated reason.

### Flags outside scope (resumed)

- **The wide tree's `LogPanel` has no card delete for entries**, so the `:579` failure path cannot arise
  there. This is a known gap (J6), recorded here as the reason that path is tested in the compact tree only.
- **`CartographyViewModel.onCleared`** (`:591-601` at `fa5aa9e`) commits a pending delete after the
  ViewModel is gone. A failure there is logged only and cannot be shown. Unchanged; noted because it
  is the one entry-delete failure no Toast covers.
- **The scaffold's header comment**, which lists "every parameter this extraction created"
  (the comment opening at `AvailabilityCompactScaffold.kt:20`), does not name the new parameter. Later additions such as
  `onRequestDeleteCartographyEntry` are not named there either, so I left it alone.
