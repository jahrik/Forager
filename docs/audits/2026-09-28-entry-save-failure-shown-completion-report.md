# Entry save failures shown: completion report (dispatch `2026-09-28-68`)

**Status: stopped at verification step 3, before any test or code.** The survey found more than one
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
