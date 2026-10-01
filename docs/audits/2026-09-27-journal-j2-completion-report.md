# 2026-09-27: Journal redesign J2, completion report

Dispatch: `prompts/preserved/2026-09-27-18.md` (build, J2 coder). Plan:
`docs/plans/journal-redesign.md` (J1, J2, J3, J7, J10, "Build order" J2, "Rules for every build
stage"). Map: `docs/audits/2026-09-27-journal-j0-pulse.md`. Previous stage:
`docs/audits/2026-09-27-journal-j1-completion-report.md`. Written by the J2 coder in a cloud worktree
on local branch `j2`, tracking `origin/journal-redesign`.

**Status after the second coder (added): T1 to T5 are all built, tested and pushed, including
T3's badges and T4's album button, built after the owner answered the three questions
(`prompts/preserved/2026-09-27-19.md`); T5's side effect on `selectedTab` is fixed too. See the
section "Added by the second coder" at the end. The paragraph below is the status as first
written.**

**Status: T1, T2, T5 are built, tested and pushed. T3 is built except the 🔗 badge. T4 is built
for the timeline only ("✎ New entry"); the album's "📷 Add photo" button is not built.** Both gaps
are stopped on questions the dispatch and the plan leave open, one of them a wrong premise (section
"Needs a decision"). What's on the branch head works as described below and passes the full suite.
The album shows no badge and keeps its Camera/Import row where the floating button would go.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `281c7821d9a9c500859bdf66956bdaa427f3d029` | T1 tests first: `JournalEntriesTest` (3). |
| `b26043dda6a38454ff3fb049d023282d9b8f5f84` | T1: the Entries \| Records segmented switch; `ENTRIES_HOME_TAG`; label-driven tests updated. |
| `24f2b83ec40efa5a4f8fbda60f657161fd0c1be9` | T2 tests first (7). |
| `0be35d2c416eb209e2bada085f7884a5874aab13` | T2: `EntriesDrafts.kt` (banner, full-screen list); Drafts sub-tab gone; label-driven tests updated. |
| `301787497a0c6dd101515d70b66eafdf3a19fe70` | T3 tests first (4 in `JournalEntriesTest`, 3 in the new `AvailabilityScreenJournalEntriesStateTest`). |
| `da2447376a777e9d818a783b2a774c7e38a82f1e` | T3: `EntriesToolbar.kt`, `EntriesAlbum.kt`, `EntriesAlbumModel.kt` (+ `EntriesAlbumModelTest`, 3), view mode in `JournalScreenState`; sub-tab row gone; delete dialog extracted; label-driven tests updated. |
| `95f6f7761f8f217d40c1c4dc893626441e350033` | T5 tests first (2). |
| `83a36fffb3aee14b99503d4995919ee8df1e3773` | T5: `compactTab` is `rememberSaveable`. |
| `e63260ce65064c7c8f3f0e8f5bebe75fe42b1e9e` | T4 tests first, timeline half (3). |
| `aa0ac15f052aabcdbd9b815a753c89e4982077f2` | T4, timeline half: the "New entry" `ExtendedFloatingActionButton`; the "+" tile gone; tests that tapped the tile repointed. |
| `4791f24e34e3b3f7c6159e26f8202283f5580c99` | Indentation only, in `CartographyScreen`'s content `Box`. Final code head. |
| this report's commit | This file. |

Production diff `8615044..4791f24`: 10 files, +653 / −148, all under `ui/log/` plus
`ui/availability/AvailabilityScreen.kt` (T5). `AvailabilityCompactScaffold.kt`, `LogPanel.kt` and
`RecordsTab.kt` are untouched.

## Premises checked before building

- **Base.** `origin/journal-redesign` was `861504481796c3f2f228caf546ac22dabb67dfdf` when
  fetched. `git diff --stat 0ba8877 8615044` lists only `RECORD.md` and
  `prompts/preserved/2026-09-27-18.md`, as the planner said.
- **Baseline at the base**, from a cleared results directory, build log clean: **230 classes /
  1801 / 0 / 0 / 24**. Matches the planner's figure.
- **Citations re-read at `8615044`:**
  - `JournalTab.kt:406-425`: the `SecondaryTabRow` is 406-422 and the `when` starts at 424. Holds.
  - The BackHandler 2 condition, Records → Cartography: holds.
  - `CartographyScreen.kt:284-292`: the Entries/Drafts/Album `SecondaryTabRow`. Holds.
  - `CartographyEntryListScreen` with `draftEntries`: holds.
  - `CartographyEntryListScreen.kt:91, :99`: the "+" tile item and `AddCartographyEntryTile`. Holds.
  - `PhotoGalleryScreen`'s drawer caller: holds. It is at `AvailabilityScreen.kt:1205` today, called without reference counts.
  - `AvailabilityScreen.kt:694`: `compactTab`, plain `remember`. Holds.
- **Callers of what changed** (`git grep`):
  - `CartographyScreen(` is called by `JournalTab` and `LogPanel.kt:366`. See Decisions 1.
  - `CartographyEntryListScreen(` is called only by `CartographyScreen` and, since T2, `DraftsListScreen`. No test calls it directly.
  - `PhotoGalleryScreen(` has two callers before the change, `CartographyScreen` and the drawer. After it, only the drawer.

### Premises that were wrong

1. **"A 🔗 badge … replacing the reference-count text (`PhotoGalleryScreen.kt:148`,
   `cartographyEntryReferenceCounts`)."** No reference-count text is visible on the tile. Line 148
   passes `cartographyEntryCount` into `GalleryPhotoTile`, which uses it only inside the **delete
   confirmation dialog** ("This photo appears in N journal entries", `PhotoGalleryScreen.kt:225-232`
   at `8615044`). The tile itself shows the photo, a delete button and a date line (`:186-204`). The
   plan lists `PhotoGalleryScreen.kt` among the files it did not read, which fits. The badge was
   not built. See "Needs a decision" 1.
2. **Planner's prediction 2** ("every test locating … changes; the Finds gallery's tab tests do
   not"): the second half holds, since `FindsGalleryScreenTest` passed untouched at every step. The
   first half undercounts. Removing the "+" tile (T4) broke 16 tests across 6 call sites that find
   it by its content description, "New Cartography entry". J0 part C lists that string among labels
   used "for other things", not tab selections. See "Label-driven test changes" and Decisions 7.
3. **J0's "Entries" count of 15** is right, but at T1 those checks did not fail the way you would
   expect. They failed as ambiguous matches ("Expected at most 1 node but found 2"), because the
   switch now also reads "Entries". Once T3 removes the sub-tab, the same checks would have
   **passed again for the wrong reason**: the switch label is on screen whenever the Journal is,
   including behind an open entry. So each one now asserts a tag, `ENTRIES_HOME_TAG`, which exists
   only while Entries shows its own top level. That keeps the check's meaning instead of letting it
   pass by coincidence (CLAUDE.md, "a check that passes identically before and after").

Predictions 1, 3 and 4:
- **1 holds.** `CartographyScreen`'s `SecondaryTabRow` is gone entirely.
- **3 holds.** `compactTab` is an enum and saves through the default saver, no custom `Saver`.
- **4 holds.** The suite grew by 25 tests (20 to 40 predicted), with 0 failures and skipped unchanged.

## What was built

**T1, the switch.** `JournalTab`'s top row is now a `SingleChoiceSegmentedButtonRow` of two
`SegmentedButton`s over `JournalTopTab.entries`.
- **Labels.** They come from a new `JournalTopTab.switchLabel`: CARTOGRAPHY reads "Entries", RECORDS reads "Records". Code names are unchanged.
- **State and Back.** Selection is still `journalState.topTab`. Choosing Entries still calls `leaveFindEditingIfNeeded()`. The Records → Entries `BackHandler` is untouched.
- **Tags.** `journal-switch`, `journal-switch-entries` and `journal-switch-records`.
- **`ENTRIES_HOME_TAG`** marks Entries' own top level.

**T2, the Drafts banner** (`ui/log/EntriesDrafts.kt`, new).
- **The banner.** `DraftsBanner` reads "✎ N unfinished entries · Continue ›", singular "entry" for one (`draftsBannerLabel`). It shows only when `draftEntries` is non-empty.
- **Continue with one draft** opens it in EDIT mode, the old Drafts tab's open path.
- **Continue with more than one** sets `draftsListOpen`, and `CartographyScreen` then renders `DraftsListScreen` in place of Entries: a back arrow ("Back to Entries"), the title "Unfinished entries", and `CartographyEntryListScreen` over `draftEntries`. Picking a card opens that draft in EDIT mode.
- **Back** from the list, by `BackHandler` or the arrow, returns to Entries.
- **List-open state** is a local `rememberSaveable` in `CartographyScreen`, not in `JournalScreenState`. It survives a recreation and closes on a tab change: the dispatch's "transient" choice.

**T3, the album as a view of Entries.**
- **Toggle** (`EntriesToolbar.kt`). A right-aligned row of two `FilledIconToggleButton`s: timeline (`ViewList`, "Timeline view") and album (`GridView`, "Album view"). Touching the one already on does nothing, so exactly one is always on.
- **State.** `EntriesViewMode { TIMELINE, ALBUM }` is a new `JournalScreenState` field, `entriesViewState`, and the third element of its `Saver` (by name). `JournalTab` passes it to `CartographyScreen`'s new `entriesViewState` parameter. `LogPanel` passes nothing and gets a local unsaved default.
- **Back.** `BackHandler(enabled = album)` steps back to the timeline. It is composed only at Entries' top level.
- **Album** (`EntriesAlbum.kt`, new):
  - Every gallery photo, grouped by `groupAlbumByDay` (`EntriesAlbumModel.kt`, pure Kotlin): newest day first, input order within a day, unknown dates in one last group labelled "Date unknown", device-local days.
  - Day headers like "Sat, Sep 26, 2026", the Records logbook's format.
  - `LazyVerticalGrid` with 3 fixed columns and `spacedBy(3.dp)` both ways. Tiles are square, with a tap to open the viewer (which steps through photos in the order shown) and the delete button.
  - Delete confirms through `GalleryPhotoDeleteDialog`, extracted unchanged from `PhotoGalleryScreen`'s tile so both surfaces share one dialog.
  - The Camera/Import row stays at the top of the album; see "Needs a decision" 2.
- **`PhotoGalleryScreen` itself is unchanged apart from that extraction.** It is now only the drawer's (`DrawerPanel.PhotoGallery`), which keeps its current look, as the dispatch allows. `PhotoGalleryScreenTest` passes untouched.

**T4, timeline half.**
- **The button.** An `ExtendedFloatingActionButton`, "✎ New entry" (the `Edit` icon plus the text), sits bottom-end over the timeline and calls today's new-entry action (EDIT mode, `onStartEntry(LocalDate.now())`). Tag `entries-fab`.
- **The tile is gone.** The "+" tile, `AddCartographyEntryTile`, and `CartographyEntryListScreen`'s `onAddEntry` parameter are removed.
- **Clearance.** The list takes `bottomContentPadding`; the timeline passes `FAB_CLEARANCE = 88.dp` (56 dp button, 16 dp margin, 16 dp air).
- **Empty states.** The list takes `emptyMessage`: "No entries yet. Use New entry to start one." on the timeline, and the old "No drafts…" text in the drafts list.
- **Which overload, and why.** A probe of Robolectric's semantics tree showed that material3 1.5.0-alpha26's `(icon, text)` overload wraps the label in `clearAndSetSemantics`. The button's merged semantics, which is what TalkBack reads, then carries no label. The content-lambda overload exposes the text, so that is the one used (recorded at the call site).
- **No floating button on the album** yet.

**T5.** `compactTab` is `rememberSaveable { mutableStateOf(CompactTab.MAP) }`. Nothing else about
tab selection changed; see "Flags" for `selectedTab`.

## Tests: base failures

Each tests-first commit was run at its own base from a cleared results directory. The build log was
checked for `e: ` lines before any results were read.

- **T1 at `8615044`.** 3 of 3 fail, each `could not find any node … TestTag = 'journal-switch-entries'` (or `-records`).
- **T2 at `b26043d`.** 6 of 7 fail on `entries-drafts-banner` or `entries-drafts-continue` not existing.
  - **The seventh, "with no drafts there is no banner", passes at base.** It is an absence check, and the base has no banner at all, so it cannot fail there. A revert check proves it instead (below).
- **T3 at `0be35d2`.** 7 of 7 fail on `entries-view-timeline` or `-album` not existing. The first attempt did not compile (a `DpRect.width` extension import). The runner refused to read that run's results; the arithmetic was written out and the second run is the one cited.
- **T5 at `da24473`.** 2 of 2 fail: the Journal or List bottom item is `Selected = 'false'` after `StateRestorationTester`'s round trip.
- **T4 at `83a36ff`.** 3 of 3 fail on `entries-fab` not existing.
- **Forward.** The first forward T4 run failed one test for a reason the prediction did not include: the FAB had no text in its merged semantics. That was diagnosed with the probe above; the fix is the overload change, not a test change.

What the tests assert (`JournalEntriesTest` 17, `AvailabilityScreenJournalEntriesStateTest` 5,
`EntriesAlbumModelTest` 3). Everything goes through the real `JournalTab` or `AvailabilityScreen`,
and Back goes through the Activity's real `OnBackPressedDispatcher`.

- **Switch.** Labels, default selection, no "Cartography". Coordinate touches at three points on each side (12%/30%, 50%/50%, 88%/70% of the control's own bounds) switch the content. Back from Records selects Entries.
- **Banner.**
  - Exact wording for 1 and for 3 drafts; no banner without drafts.
  - Continue touched at three points: with one draft it opens that draft in the editor each time; with three it opens the list (no draft opened, the committed entry absent), and Back returns to Entries.
  - The list's back arrow touched at three points returns to Entries.
  - A card in the list touched at three points opens that draft in the editor.
- **Toggle and album.**
  - Timeline is on by default; there is no "Album" text and exactly one "Entries" node.
  - Touching album and then timeline at three points each switches the content.
  - Back from the album gives the timeline.
  - Album layout: day order (Sep 26, Sep 20, unknown) with header text; three photos share a row in input order, 3 dp apart; the fourth wraps under the first, 3 dp below; tiles are square.
- **State** (AvailabilityScreen level).
  - The album survives List → Journal.
  - The album survives `emulateSavedInstanceStateRestore()`.
  - Back from the album gives the timeline, and a second Back reaches the map (the Journal is left).
  - The Journal and List bottom tabs survive a restore without being tapped again.
- **Floating button.**
  - "New entry" is shown and the "+" tile's content description is gone.
  - The test first confirms that at least one card really lies under the FAB. Then the FAB, touched at three points, starts a new entry each time and never opens that card.
  - Scrolled to the end, the last card and the one beside it end above the FAB's top, and each takes touches at three points.
- **`EntriesAlbumModelTest`.** Headless, `America/Los_Angeles`: day and within-day order, unknown last, 20:00 and 21:00 local (the next day in UTC) staying on the local day, and the empty cases.

**Taps that are semantic clicks, not coordinate touches:** navigation that isn't under test (bottom-nav taps, and the toggle and switch taps in the state class, which is about where state lives), and the label-driven updates in older tests.

## Revert checks

The runner (scratchpad `revert.sh`, `run.sh`):
- saves a copy of the file and applies a one-line edit (it aborts unless the edit matches exactly once);
- runs the named classes from a cleared results directory;
- refuses to read results if the build log has `e: ` lines;
- restores from the saved copy and `cmp`s the file against it.

After each check, `grep` confirmed the forward line was present before the commit. Every build log was clean.

| Behaviour | Edit | Predicted | Observed |
|---|---|---|---|
| Switch's Back step | `JournalTab.kt`: Records BackHandler body `selectedTopTab = CARTOGRAPHY` → `Unit` | both Back-to-Entries tests fail | `JournalEntriesTest` "Back from Records steps to Entries" and `RecordsFilterChipsTest` "…Back from All steps to Cartography": `journal-switch-entries` Selected = 'false'; 2/16 |
| Banner routing, one vs many | `CartographyScreen.kt`: `if (drafts.size == 1) {` → `if (false) {` | one-draft Continue tests fail | `expected:<[draft-a]> but was:<[]>`; `CartographyScreenTest` banner test: editor not displayed; 2/24 |
| No banner without drafts (extra) | `if (drafts.isNotEmpty()) {` → `if (true) {` | the absence test fails | `found '1' node … 'entries-drafts-banner'`; 1/10 |
| Drafts list Back (extra) | `BackHandler(enabled = draftsListOpen)` → `enabled = false` | several-drafts test fails | `entries-drafts-list` still exists after Back; 1/10 |
| Toggle saveable, as the dispatch words it | `JournalScreenState.kt`: `rememberSaveable(saver = …)` → `remember { JournalScreenState() }` | restore test fails, tab-change test passes | J2 album restore test fails (album ToggleableState ≠ On) and J1's restore test fails; both tab-change tests pass; 2/5 |
| Toggle saveable, that field only (extra) | restore `EntriesViewMode.valueOf(saved[2])` → `EntriesViewMode.TIMELINE` | only the J2 restore test fails | exactly that; 1/5 |
| Back from the album | `BackHandler(enabled = viewMode == ALBUM)` → `enabled = false` | both album-Back tests fail | timeline not On; the AvailabilityScreen one also finds no toggle (Back left the Journal); 2/17 |
| Album day order (extra) | `EntriesAlbumModel.kt`: `.toSortedMap(compareByDescending { it })` → `.toSortedMap()` | model test and layout test fail | `expected:<[2026-09-26, 2026-09-20, null]> but was:<[2026-09-20, 2026-09-26, null]>`; "the older day comes after the newer day's photos"; 2/17 |
| FAB action (timeline) | FAB `onClick = { …onStartEntry(…) }` → `onClick = { }` | FAB touch test fails | `expected:<1> but was:<0>`, plus the 2 `CartographyScreenTest` new-entry tests (editor not shown); 3/31 |
| FAB clearance (extra) | `bottomContentPadding = FAB_CLEARANCE` → `Spacing.lg` | end-of-list test fails | `fab-12 ends above the floating button … card bottom=624.0.dp, fab top=568.0.dp`; 1/17 |
| `compactTab` saveable | `rememberSaveable` → `remember` | both T5 tests fail | both: bottom item Selected = 'false'; 2/5 |
| FAB action on the album | not run: not built | | |
| 🔗 badge | not run: not built | | |

## Label-driven test changes (plan rule; J0 part C)

Each change was made in the same commit as the change that broke it. Nothing was disabled, skipped,
ignored or weakened. The replacement for each is noted.

| File | T1 | T2 | T3 | T4 | Total | What replaced it |
|---|---|---|---|---|---|---|
| `ui/availability/AvailabilityScreenBackNavigationTest.kt` | 12 (2 "Cartography", 9 "Entries" checks, 1 "Entries" click) | 3 ("Drafts (1)": 2 checks, 1 click) | 1 ("Album") | 3 ("New Cartography entry") | 19 | switch tag selected; `ENTRIES_HOME_TAG`; banner text / Continue; album toggle; FAB tag |
| `ui/log/CartographyScreenTest.kt` | 0 | 2 ("Drafts (1)") | 4 ("Entries") | 2 ("New Cartography entry") | 8 | Continue tag / banner text; `ENTRIES_HOME_TAG`; FAB tag |
| `ui/availability/AvailabilityScreenInAppCameraTest.kt` | 0 | 0 | 2 ("Album") | 1 ("New Cartography entry", the `openEditorCamera` helper, 11 tests) | 3 | album toggle; FAB tag |
| `ui/availability/AvailabilityScreenLandscapeB3DestinationsTest.kt` | 2 ("Cartography": bounds, selected) | | | | 2 | the two switch segments' span; switch tag selected |
| `ui/availability/AvailabilityScreenShortLandscapeTest.kt` (class `AvailabilityScreenTurnToShortLandscapeTest`) | 2 ("Cartography") | | | | 2 | switch tag displayed |
| `ui/log/JournalTabTest.kt` | 1 ("Entries" absence) | | | | 1 | `ENTRIES_HOME_TAG` absence |
| `ui/log/RecordsFilterChipsTest.kt` | 1 ("Cartography" selected, added in J1) | | | | 1 | switch tag selected |
| **Total** | **18** | **5** | **7** | **6** | **36** | |

**Reading the counts:**
- **Against J0:** "Cartography" 7 (J0's 6, plus J1's `RecordsFilterChipsTest:285`); "Entries" 15 (J0's count); Cartography "Drafts (1)" 5; "Album" 3.
- **Not in J0's list:** the 6 "New Cartography entry" sites. See Decisions 7.
- **Where each broke:** the T1 full run had 16 failures, all at these call sites. T2 had 5, T3 had 7, and T4 had 16 tests across 6 call sites.
- **Test renames.** Three tests whose names described the removed control were renamed (BackNavigation 2 and 1, CartographyScreen 1). Their assertions were kept or replaced one-for-one.
- **Untouched and still passing:** `FindsGalleryScreenTest` (its own "Drafts (1)" and "Log" tabs) and `PhotoGalleryScreenTest`.

## Suite before and after

- **Before**, at `8615044`: 230 classes / 1801 / 0 / 0 / 24.
- **After**, at `4791f24` (the final code head; this report adds no code), cleared results directory, build log clean: **233 classes / 1826 tests / 0 failures / 0 errors / 24 skipped.**
- **Difference:** +3 classes (`JournalEntriesTest` 17, `AvailabilityScreenJournalEntriesStateTest` 5, `EntriesAlbumModelTest` 3) and +25 tests. Skipped unchanged.

## Needs a decision (why T3's badge and T4's album half stopped)

1. **The 🔗 badge: wrong premise, and "attached to an entry" is ambiguous.**
   - There is no visible reference-count text on an album tile to replace (Premises 1). The only count is in the delete dialog, and deletes are J4's.
   - A photo can be referenced by two different things: finds (`GalleryPhoto.referencingEntryIds`, a join over `log_entry_photos`; the dialog calls these "entries") and Cartography entries (`cartographyEntryReferenceCounts`; the dialog calls these "journal entries").
   - Options:
     - (a) the badge marks a photo kept by at least one **Cartography** entry;
     - (b) it marks a photo attached to at least one **find**;
     - (c) either;
     - (d) two different badges.
   - Also: does the delete dialog's count text stay as it is? (My reading is yes, since deletes are J4's and nothing visible is replaced.)
   - Once the owner picks one, the badge is about a 10-line change in `AlbumPhotoTile` plus its two tests.
2. **What "📷 Add photo" does on the album.** The dispatch says "today's add-photo action from the album", but today there are two: **Camera** (in-app camera) and **Import** (system picker).
   - Options:
     - (a) the button opens the camera, and Import stays as a button in the album;
     - (b) it opens a small menu or sheet with Camera and Import, and the Camera/Import row goes;
     - (c) it is Import, and Camera stays.
   - A sub-question: does the Camera/Import row go once the button exists? Today's tests `AvailabilityScreenInAppCameraTest` (the Album's Camera button) depend on the answer.
   - Until then the album keeps its row and has no floating button.
3. **Where Back from a draft opened from the drafts list lands.** Kept as today's Drafts-tab behaviour: **Entries**, not the list. The list-open flag is declared after the open-entry early return, as `selectedTab` was, so it is forgotten. The alternative is to return to the list while drafts remain (the usual list → detail → back shape). That is a one-line move of the declaration. No test pins either way: `JournalEntriesTest`'s pick test re-opens the list only when it is not showing. Not a blocker; please confirm or reverse.

## Decisions I made that the dispatch did not

1. **`LogPanel` (the wide tree) gets the new Entries shape too:** switch-less, but with the banner, list, toggle, album and FAB. It shares `CartographyScreen`, and planner's prediction 1 (the tab row "removed entirely") implies it. `LogPanel.kt` is not edited: it passes no `entriesViewState`, so its toggle is local and unsaved until J6. This is the same shape as J1's decision 3 for `RecordsTab`. Its drawer album (`PhotoGalleryScreen`) keeps its old look.
2. **The banner shows in both views** (timeline and album), under the toolbar, because the dispatch says "at the top of Entries" and the toggle row is part of Entries.
3. **The list-open state** is a local `rememberSaveable`: it survives a recreation and closes on a tab change. The dispatch said either was acceptable.
4. **Banner colour** is `surfaceContainerHigh`, a neutral surface, not a J6 record-type role (drafts are not a record type). "Continue ›" is a `TextButton`, and only that is the touch target, not the whole banner.
5. **Album order:** newest day first, input order within a day, unknown dates last. Headers use J1's logbook format. The per-tile date line is dropped, since the day header carries it.
6. **Toggle component:** two `FilledIconToggleButton`s (a checkbox role with on/off) rather than a second segmented row, so the view toggle does not read like the Entries | Records switch.
7. **The six "New Cartography entry" call sites were updated, not left red.** The dispatch says a failing test outside J0's list is "reported, not touched". I read that as protecting unrelated tests: these tests drive the exact control T4 removes, and J0's list was scoped to tab labels because that is what J0 was asked. Only the locator changed (to the FAB's tag), and every assertion stands. If the planner reads the rule strictly, reverting that part of `aa0ac15` leaves 16 red tests.
8. **The FAB uses the content-lambda overload** (see What was built).
9. **The dispatch and the plan:** no disagreement found. The dispatch adds T5 and the full-screen drafts list (owner rulings), which the plan does not have.

## Device-only items

- **The FAB's real width and label.** Robolectric measures text implausibly here: the FAB rendered 80 dp wide with a 5 to 6 dp label. So the coordinate touches cover the button's Robolectric bounds, not its device bounds.
  - On the S22 Ultra, check that "✎ New entry" shows in full, that touches across its whole width start an entry, and that the card beside it still opens.
  - The button sits inside `CartographyScreen`'s bounds, above the bottom bar, so no system inset is involved (CLAUDE.md, insets pitfall). Still confirm it clears the gesture bar in portrait.
- **The banner's one-line fit** at 360 dp and with larger font scales.
- **Album tiles at 3 columns** (about 107 dp): the corner delete button covers roughly the top-right 40 dp. Check it does not steal taps meant for the photo.
- **A real night-mode toggle and a fold** on the Journal, on the album, with the drafts list open: the tab, view and list should all survive. `StateRestorationTester` stands in for this under Robolectric.

## Flags outside scope

- **`selectedTab` (`ResultsTab`) is still plain `remember` beside the now-saveable `compactTab`** (`AvailabilityScreen.kt`, commented there). After a recreation on List or Seasonal the two disagree until the next tab tap. The inferred consequence: `LaunchedEffect(selectedTab, …)` fires `onMapTabSelected` rather than `onSeasonalTabSelected` for a restored Seasonal tab. After a configuration-change recreation the ViewModel still holds the data; after process death Seasonal would not reload until its keys change. Not device-checked. Fix, if wanted: make `selectedTab` saveable too (the dispatch said nothing else about tab selection changes).
- **The timeline's scroll position resets** whenever an entry is opened and closed (the grid's state lives inside the list, which the open entry disposes). Plan J10's scroll positions are not in this dispatch.
- **The runner's first T4 full run produced no JUnit XML.** Gradle's HTML report failed on a test name containing an em-dash under the container's POSIX locale, and the XML was not written. The tally read "0 classes", which is what caught it; nothing else in the output said so. The runner now sets `LC_ALL=C.UTF-8` and the tally flags an empty read as "not evidence". Every result cited above was read after that fix, or from runs whose failing tests had ASCII names and whose XML was present (each tally shows its class count).
- **`JournalScreenState.Saver` now expects three elements.** A two-element bundle from a J1 build would fail in `saved[2]`. J1 was never released (one PR at the end), so no such bundle exists outside development installs.

## Added by the second coder (continuation `prompts/preserved/2026-09-27-19.md`)

Everything above this section was written by the first J2 coder and is left as written, apart from
the status note added at the top. That includes "Needs a decision", which the owner has now answered,
and its flag on `selectedTab`, which this section closes. I am a fresh session: the first coder's
session was lost to a container restart. I worked in a cloud worktree on local branch `j2b`, cut
from `origin/journal-redesign`.

### Base and baseline

- **Base.** `origin/journal-redesign` was `68726475d6167fed4677ac888cac37f911c5de87` when fetched,
  the planner's commit after `a31c3e3`, as the dispatch says.
- **Baseline at the base.** Cleared results directory, UTF-8 locale, build log clean:
  **233 classes / 1826 / 0 / 0 / 24.** This matches the first coder's final figure, which the planner
  had not re-run.
- **Runner** (scratchpad `run.sh`, `revert.sh`). Every Gradle run:
  - runs with `LC_ALL=C.UTF-8`, the first coder's finding, which the planner ruled on;
  - starts from a cleared `test-results` directory;
  - refuses to read results if the build log has `e: ` lines;
  - refuses a run with no XML.

  `revert.sh` saves a copy of the file and applies a one-line edit, aborting unless the edit matches
  exactly once. It then runs `run.sh`, restores the file from the saved copy (never from git) and
  `cmp`s it against the copy. After each revert check, `grep` confirmed the forward line was present
  before the commit.
  - **One compile failure, caught.** The first album-button run at its base did not compile: the
    runner reported `e: … Unresolved reference 'performClick'` and read nothing. I added the import,
    and the second run is the one cited.
  - **One edit was silently not applied.** A shell `python3` heredoc edit was refused by the
    sandbox, so the first forward badge run had the content description on the inner icon, not on
    the badge. The test failed in exactly the way that predicts (the tagged node carried no
    description). The edit was re-applied with the editor, and the second forward run is the one
    cited.

### Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `fd842c5332a16c1808df5890adc3f149bb3aa031` | Badge tests first (2, `JournalEntriesTest`). |
| `befa39d08c0048b9b2f9f2b27dc429a2a5f053d7` | T3: the album's two badges. |
| `2ec0f1fc26bb27f6f692536963277634dc635c81` | Album button tests first (4, `JournalEntriesTest`, plus the harness's Activity Result recorder). |
| `38e9028dc15409ba4ee038477c71d50070cd294f` | T4, album half: "Add photo" with a Take photo / Import menu; the album's Camera/Import row goes; 2 `AvailabilityScreenInAppCameraTest` tests moved to it. |
| `8d374bc6fb6b11aa12e330f4e5db1529add84c28` | Drafts-list Back tests first (2, `JournalEntriesTest`; the harness's Finish now commits the draft). |
| `c77704dad1d9aef4ed19199ba8eb2aeeee5ef7fd` | T2: Back from a draft opened out of the list returns to the list. |
| `bf369f146d0ba7fa2b2b20e3e8f5aa5df39df692` | `selectedTab` tests first (2, `AvailabilityScreenJournalEntriesStateTest`). |
| `ed9ac8a50be5db0614ba35e5ff9d8a6335a49966` | `selectedTab` is `rememberSaveable`. Final code head. |
| this report's commit | This section, plus one stale test-section comment in `JournalEntriesTest` (comment only). |

- **Production diff `6872647..ed9ac8a`:** 3 files, +202 / −41: `ui/log/EntriesAlbum.kt`,
  `ui/log/CartographyScreen.kt`, `ui/availability/AvailabilityScreen.kt`.
- **Test diff:** 3 files, +347 / −9.
- **Untouched:** `PhotoGalleryScreen.kt`, `LogPanel.kt` and `AvailabilityCompactScaffold.kt`.

### Premises checked before building

- **Answer 1's premise.** The planner said `PhotoGalleryScreen.kt:148` puts no reference count on
  the tile. That holds today, and the same is true of the album tile that replaced it for Entries:
  `EntriesAlbum.kt`'s `AlbumPhotoTile` passes `cartographyEntryCount` only to
  `GalleryPhotoDeleteDialog`. Nothing visible was replaced. The dialog's count text is unchanged.
- **Where each attachment fact comes from.** I traced both from the code. Neither needed a new
  query, so the abort condition did not fire.
  - **Journal (Cartography) entries: `cartographyEntryReferenceCounts`.** The chain:
    - built from `MushroomLogUiState.cartographyEntryPhotoReferenceCounts` (`MushroomLogUiState.kt:52`);
    - filled in `MushroomLogViewModel.loadGalleryPhotos` (`MushroomLogViewModel.kt:288-289`), one
      `getPhotoEntryReferenceCount` per photo;
    - wired at `MainActivity.kt:104` to `GetEntryReferenceCountUseCase.forPhoto`, then
      `CartographyEntryDao.countEntriesReferencingPhoto` (`CartographyEntryDao.kt:163-171`).

    That query counts **committed entries only** (`isDraft = 0`). The counts reach the album
    through `AvailabilityCompactScaffold.kt:952`, then `JournalTab.kt:446`, then `CartographyScreen`,
    then `EntriesAlbum`.
  - **Finds: `GalleryPhoto.referencingEntryIds`** (`domain/model/GalleryPhoto.kt:19`). It is built
    by `RoomMushroomLogRepository.getAllPhotos` (`RoomMushroomLogRepository.kt:74-87`) as a join
    over `log_entry_photos` (`LogEntryPhotoCrossRef`, `LogPhotoEntity.kt:48-56`). It already
    reaches the album on every photo it is given (`galleryPhotos`). A draft find is a standalone row
    (`MushroomLogEntryEntity.kt:153-162`) that carries its own cross-refs (`MushroomLogDao.kt:49-50`,
    the read `commitDraft` repoints). So this list **includes draft finds**. See "Needs a decision"
    below.
- **Callers.** `EntriesAlbum(` has one caller, `CartographyScreen.kt`. `CartographyScreen(` has two:
  `JournalTab` and `LogPanel.kt:366`. So `LogPanel`'s album gets the badges and the button too, and
  `LogPanel` passes the reference counts (`LogPanel.kt:371`). This follows the planner's ruling that
  `LogPanel` shares the new Entries layout until J6.
- **The `selectedTab` question, from the code.** The first coder's inference is **confirmed, and
  the consequence is wider** than it said.
  - `selectedTab` (`AvailabilityScreen.kt:687` at base) was plain `remember`, beside the saveable
    `compactTab` (`:702`).
  - **Readers:** the `LaunchedEffect(selectedTab, region, selectedMonth, taxonFilter)` at `:860-863`
    calls `onMapTabSelected` or `onSeasonalTabSelected`; the wide tab row reads it at `:1268-1272`;
    the wide content at `:1295`.
  - **Writers:** `onSelectedTabChange` is called only from a bottom-nav tap
    (`AvailabilityCompactScaffold.kt:477-479`), from `:1152` (set on map), and from
    `onViewSpeciesOnMap` and the go-home Back (`AvailabilityScreen.kt:732, :897`). Nothing re-syncs
    it after a restore.
  - **The compact Seasonal content reads `compactTab`** (`AvailabilityCompactScaffold.kt:919`).
    - So after any recreation on Seasonal (night mode, fold, process death), the screen shows
      Seasonal while the effect asks for the map's sightings.
    - Worse, **a new search on that screen did not reload Seasonal** until a tab tap. This does not
      need process death: `onSeasonalTabSelected` (`AvailabilityViewModel.kt:476-500`) is the only
      loader and was never called.
  - The two tests below show both at the base.

### Premises that were wrong

- **None of the continuation's cited facts failed.**
- **The first coder's `selectedTab` flag was right but narrower than the effect.** It said "after
  process death Seasonal would not reload". In fact, after any recreation, a changed search does not
  reload Seasonal (above).

### What was built

- **T3, the two badges** (`EntriesAlbum.kt`, `AlbumAttachmentBadges`). They sit at the tile's
  bottom-start corner, side by side, 22 dp discs:
  - **Journal entry:** a link icon (the plan's 🔗) on `surfaceContainerHighest`. Content
    description "Attached to a journal entry". Shown when the entry count is above 0.
  - **Find:** the Finds chip's `Eco` icon in its J6 role (`RecordTypeStyle`, Finds:
    `secondary` on `secondaryContainer`). Content description "Attached to a find". Shown when
    `referencingEntryIds` is non-empty.
  - Each badge's description sits on the badge's own node, so TalkBack announces it as one item.
    Badges take no touches. The delete dialog is unchanged.
- **T4, the album's button** (`EntriesAlbum.kt`, `AddPhotoButton`):
  - **The button.** An `ExtendedFloatingActionButton`, 📷 (`AddAPhoto`) plus "Add photo", bottom-end,
    with the same tag as the timeline's (`entries-fab`), since one or the other is on screen at a
    time. It uses the content-lambda overload, for the first coder's semantics reason.
  - **The menu.** Touching the button opens a `DropdownMenu` anchored to it, with "Take photo"
    (`launchCamera`) and "Import" (`launchGallery`): the same `rememberPhotoAcquisitionLaunchers`
    actions the row called.
  - **The row is gone.** The album's Camera/Import row was removed, and the empty text now reads
    "No photos yet. Use Add photo to take or import one."
  - **Clearance.** The grid takes `FAB_CLEARANCE` (88 dp) of bottom padding, as the timeline does.
    `FAB_CLEARANCE` went `private` → `internal` in `CartographyScreen.kt`.
  - `CartographyScreen`'s comment on the missing album button is updated.
- **T2, Back from a list-opened draft** (`CartographyScreen.kt`):
  - `draftsListOpen` (still `rememberSaveable`) is declared above the open-entry early return, so
    it outlives the open draft. Its `BackHandler` stays below the return, so while a draft is open
    the entry's own handler is the only one enabled.
  - Back from the draft shows the list, and Back again shows Entries. A draft opened from the banner
    (one draft) never sets the flag, so it still closes onto Entries.
  - The existing test "with one draft, touching Continue … opens that draft" still asserts
    `ENTRIES_HOME` after Back, and passes.
  - **Empty case.** When the last draft is finished, a `LaunchedEffect` drops the flag, and the
    screen lands on Entries. See "Decisions" 2.
- **`selectedTab`** is `rememberSaveable` (enum, default saver), one line plus comments.
  `compactTab`'s comment is updated. Nothing else about tab selection changed.

### Tests: base failures

Each tests-first commit was run at its own base (the previous commit plus the new tests), from a
cleared results directory, with the build log clean.

- **Badges at `68726475` plus `fd842c5`.** 2 of 2 fail on
  `could not find any node … TestTag = 'entries-album-badge-entry-in-entry'`. The class ran 19
  tests; the other 17 passed.
- **Album button at `befa39d` plus `2ec0f1f`.** 4 of 4 fail, each on `entries-fab` absent from the
  album (bounds, touch injection, or "not displayed"). The class ran 23.
- **Drafts-list Back at `38e9028` plus `8d374bc`.** 2 of 2 fail with `entries-drafts-list` not
  displayed. The XML puts them at `JournalEntriesTest.kt:411` (after Back) and `:437` (after the
  first Finish), the lines the prediction names, not at the list's opening.
- **`selectedTab` at `c77704d` plus `bf369f1`.** 2 of 2 fail:
  - `the restored composition asks for Seasonal expected:<1> but was:<0>`;
  - `a new search on the Seasonal tab reloads Seasonal expected:<1> but was:<0>`.

  The class ran 7.

**What the new tests assert.** They go through the real `JournalTab`, or `AvailabilityScreen` for
the tab state. Back goes through the Activity's `OnBackPressedDispatcher`. Touches use the three
points 12%/30%, 50%/50% and 88%/70% of the target's own bounds.

- **Badges.**
  - Four photos: none, entry only, find only, both. Each badge is present or absent exactly as
    expected, lies inside its own tile, and the two on one tile do not overlap.
  - The content descriptions are on the badge nodes, and each description appears exactly twice.
- **Album button.**
  - The album shows "Add photo". There is no "New entry", "Camera" or "Import" text, and the menu is
    closed.
  - Thirty photos are used, so the test first confirms a tile lies under the button. The button,
    touched at three points, opens the menu each time and never the viewer. "Take photo", touched at
    three points, opens the album camera once each (CAMERA granted) and launches no picker.
  - "Import", touched at three points, launches `PickMultipleVisualMedia` once each (recorded
    through a test `ActivityResultRegistryOwner`, the real launch path) and opens no camera.
  - Scrolled to the end, each tile of the last row ends above the button's top. A touch at three
    points on each opens the viewer on that photo (counter "28 / 30" and so on). The viewer's close
    is a semantic click (navigation, not under test).
- **Drafts-list Back.**
  - With three drafts, a card touched at three points opens it; Back shows the list, not Entries.
    After the loop, Back shows Entries with the banner.
  - With two drafts, finishing one returns to the list showing the other. Finishing the last lands
    on Entries with no banner. A new entry started from the button and closed then lands on Entries,
    not on a reappearing list. "Finish entry" is a semantic click: the claim is where the screen
    lands.
  - The new-entry step was added in the implementation commit (`c77704d`), after the base run.
    That test had already failed at its base at an earlier line.
- **`selectedTab`.**
  - Restored on Seasonal, the bottom nav still shows Seasonal, and the restore calls
    `onSeasonalTabSelected` once and `onMapTabSelected` never.
  - Then a region change calls `onSeasonalTabSelected` once and `onMapTabSelected` never.

### Revert checks

Every build log was clean; every file restored identical by `cmp`; forward lines `grep`-confirmed
before each commit.

| Behaviour | Edit | Predicted | Observed |
|---|---|---|---|
| Find badge | `attachedToFind = …isNotEmpty(),` → `attachedToFind = false,` | both badge tests fail | `could not find … 'entries-album-badge-find-in-find'` in both; 2/19 |
| Entry badge (the "neither" side) | `attachedToEntry = cartographyEntryCount > 0,` → `= true,` | both fail on extra entry badges | `found '1' node … 'entries-album-badge-entry-in-find'`; `Expected '2' nodes but found '4'` for "Attached to a journal entry"; 2/19 |
| Button opens the menu | `onClick = { menuOpen = true },` → `onClick = { },` | both menu tests fail | `entries-fab-menu-take-photo` / `-import` not displayed; 2/23 |
| Take photo's action | `{ menuOpen = false; onTakePhoto() }` → `{ menuOpen = false }` | the Take photo test and both moved InAppCamera tests fail | `expected:<1> but was:<0>`; `expected:<[ALBUM]> but was:<[]>`; "the control: the touch reached the button expected:<[ALBUM]> but was:<[]>"; 3/36 |
| Import's action | `{ menuOpen = false; onImport() }` → `{ menuOpen = false }` | the Import test fails | `expected:<1> but was:<0>`; 1/23 |
| Album clearance | `bottom = FAB_CLEARANCE),` → `bottom = Spacing.lg),` | end-of-album test fails | `m28 ends above the floating button … tile bottom=624.0.dp, fab top=568.0.dp`; 1/23 |
| Back to the list | list's `onOpenDraft` gains `draftsListOpen = false;` (the old forgetting behaviour) | both drafts-list tests fail | `entries-drafts-list` not displayed in both; 2/25 |
| Flag dropped when no drafts remain | `if (draftsListOpen && drafts.isEmpty()) draftsListOpen = false` → `Unit` | the Finish test fails at its new-draft step | `found '1' node … 'entries-drafts-list'` (the list reappeared); 1/25 |
| List hidden when empty (extra) | `val showDraftsList = draftsListOpen && drafts.isNotEmpty()` → `= draftsListOpen` | **passes** (the flag reset above hides it one frame later) | 0/25, as predicted. This guard only prevents a one-frame empty list. **No test covers it on its own.** |
| `selectedTab` saveable | `rememberSaveable` → `remember` | both new tests fail | the two messages above; 2/7 |

### Label-driven and replaced-control test changes

- **Moved to the new control, every assertion unchanged, in `38e9028`.** These are
  `AvailabilityScreenInAppCameraTest`, **2 tests at 2 call sites**, both of which tapped the album's
  "Camera" button:
  - "the Album's Camera button opens the camera for the Album" (`:317` at base): now the button,
    then Take photo, both semantic clicks as before.
  - "with the camera open, a real touch where the Camera button sits …" (`:289` at base). The point
    under test is now the button's centre, the album control that stays on screen. The positive
    control touches it at root coordinates and then touches Take photo, and the camera opens. The
    claim touches the same root point again under the camera. The three `assertEquals` are
    unchanged.
  - Test names are unchanged.
- **The first coder's `aa0ac15` moves stand, by the planner's ruling.** 16 tests at 6 call sites
  that found the removed "+" tile by "New Cartography entry" were moved to the button's tag, with
  every assertion unchanged (listed in the table above).
- **No other test changed.** `PhotoGalleryScreenTest` passed untouched: the drawer's gallery keeps
  its own Camera/Import row and no badges. So did the other "Camera"/"Import" literals the grep
  found (`CartographyScreenTest:372-373`, `JournalTabTest:371-372`,
  `AvailabilityScreenBackNavigationTest:577, :583`), which belong to the entry editors' photo
  picker, not the album.

### Suite before and after

- **Before**, at `6872647`: 233 classes / 1826 / 0 / 0 / 24.
- **After**, at `ed9ac8a` plus the comment-only test edit in this report's commit, cleared results
  directory, build log clean: **233 classes / 1836 tests / 0 failures / 0 errors / 24 skipped.**
  That is +10 tests (2 + 4 + 2 + 2), no new class, and skipped unchanged.
- **One mid-work failure outside this work.** In the full run with the album button, before the two
  InAppCamera tests were moved, one unrelated test failed besides those two:
  - The failure: `JournalTabTest` "From Album on the edit form opens the picker and pulls the
    selected photo into the entry", with `ContentDescription = 'Log photo' … is not displayed`.
  - That test drives the **find** editor's picker, which this work does not touch.
  - It passed when its class ran alone straight after (14/14), and in the final full run.
  - This is consistent with the intermittent `JournalTabTest` photo-pull failure CLAUDE.md
    describes. It was not touched and is reported as one sighting: a container `./gradlew` full
    run on `38e9028`'s production tree.

### Decisions I made that the continuation did not

1. **Badge look.**
   - The entry badge keeps the plan's 🔗 as a link icon, on a neutral surface. The find badge takes
     the Finds chip's icon and J6 role, so the two are distinct by shape and by colour.
   - Both are corner discs at bottom-start, since the delete button holds top-end.
   - The descriptions are "Attached to a journal entry" and "Attached to a find". "Journal entries"
     is the delete dialog's own word for Cartography entries.
2. **"Back to the list" when no drafts remain.** The option as the first coder posed it read
   "return to the list while drafts remain". I applied that literally: once the last draft is
   finished, the list closes onto Entries rather than showing an empty "No drafts" page, and the
   flag is dropped so a later new draft does not reopen it. If the owner meant the list always,
   it is a two-line removal plus one test step.
3. **The menu is a `DropdownMenu` anchored to the button,** the stable Material 3 menu. The plan
   leaves the Expressive FAB menu to Understory step 5. The menu items have leading icons (camera,
   photo library).
4. **Test harness.** `JournalEntriesTest` now wraps the screen in a recording
   `ActivityResultRegistryOwner`. It records launches and never returns results. Its Finish callback
   now commits the open draft. No earlier test in the class used either.

### Needs a decision

1. **The find badge counts draft finds.** `referencingEntryIds` comes from `log_entry_photos`, which
   holds draft finds' rows. So a photo attached only to an unfinished find shows the find badge,
   while the entry badge counts committed Cartography entries only (`isDraft = 0`). The delete
   dialog's "N entries" count has always read the find side the same way. I built with the data as
   it is, per "don't add a query". The options:
   - (a) keep it as is;
   - (b) exclude drafts in memory, by passing the draft finds' ids (`MushroomLogUiState.draftEntries`,
     already loaded) to the album. That is no query, but it is a new parameter through
     `CartographyScreen`;
   - (c) change the repository join, which is a query change.

### Device-only items (added)

- **The album button and its menu on the S22 Ultra.** Robolectric again measured the button about
  81 dp wide. On the device, check:
  - that "📷 Add photo" shows in full;
  - that touches across its width open the menu;
  - that Take photo opens the camera and Import opens the system picker;
  - that the menu does not open off screen;
  - that the button clears the gesture bar in portrait.
- **Badges at a 107 dp tile.** Check that the two badges read clearly over light and dark photos, in
  light, dark and Understory themes, and that TalkBack reads each.
- **A real night-mode toggle and a fold on Seasonal**, then a new search: Seasonal should reload.
  `StateRestorationTester` stands in for this.

### Flags outside scope

- **`MainActivity.kt:104` hides count failures.** It turns a failed Cartography reference count into
  0 with `getOrDefault(0)`, with no log. A failed read therefore shows no entry badge (and no warning
  in the delete dialog) without a trace. CLAUDE.md says a fallback that fires must be logged. This
  predates J2 and was not touched.
- **`AvailabilityScreenInAppCameraTest`'s moved test keeps its old name** ("…where the Camera
  button sits…"). Its comment says the point is now the floating button. I left the rename for
  whoever owns that class's naming.
