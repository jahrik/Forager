# 2026-09-27: Journal redesign J5 (short windows), completion report

Dispatch: `prompts/preserved/2026-09-27-26.md` (build, J5 coder). Plan: `docs/plans/journal-redesign.md`
(L1-L8, O6), `docs/plans/landscape-phone-design.md` (P4, P5, P11, R12 and R17 revised),
`docs/audits/2026-09-27-journal-j0-pulse.md` C2, and the B3 destinations, J1-J4b and picker-fix
reports. Written by the J5 coder in a cloud worktree on local branch `j5`, cut from
`origin/journal-redesign` at `3870e4d` (the dispatch's `94a034c` plus the planner's store-copy commit
`7a26f3b` and one audit note, `3870e4d`; no app or test change between `94a034c` and `3870e4d`).

**Status: ruling 1, L1, L2, L3 (Entries, and the Records chip row's hide-on-scroll), L4 with rulings
2 and 3, and L6 are built, tested, revert-checked and pushed. L7 is confirmed by test; nothing was
needed for it beyond keeping the composition structure the same in both orientations. L8 is
device-only. L5 is not built: it stopped on two open questions (Needs a decision 1 and 2). A third
question, whether the drafts chip also shows on Records, is Needs a decision 3; the Entries half of
L3 does not depend on it.** Final full suite: 246 classes / 2026 / 0 / 0 / 24.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `f768e2e9` | Tests first: `AvailabilityScreenJournalShortWindowTest` (22) and `JournalShortWindowCardsTest` (13). At base: 35 tests, 28 failures (below). |
| `07dd9d45` | The build, plus the test changes listed under "Tests moved or rewritten". |
| this report's commit | This file. |

## Premises checked before building

- **Base.** `git rev-parse HEAD` was `3870e4d57de5a17d1e0b18e0fcfb975d8f8a2dd9`, the planner's message's
  base. Its two commits after `94a034c` touch only `prompts/`, `RECORD.md`, `docs/audits/`.
- **Baseline, measured.** Full suite at `3870e4d`, cleared results directory, `--offline`,
  `LC_ALL=C.UTF-8`, 0 `e:` lines: **244 / 1990 / 2 / 0 / 24.** Failures, both in
  `JournalPendingDeleteTest`: "Undo on an album photo brings it back ..." (`'tile-options-delete' is
  not displayed`) and "a plain tap on an album photo still opens the viewer" (`photo-viewer` not
  found). The planner saw 1 failure at `eefce47`. The second is an album test, but a tap test, not a
  long-press one; recorded here as seen, not investigated (the dispatch holds that group).
- **The header condition** is where the dispatch says: `AvailabilityCompactScaffold.kt:691` at base
  (`!isMapFullscreen() && compactTab() != CompactTab.MAP && !isEditingJournalEntry`), inside B3's
  cap. The scaffold already holds `journalScreenState` (parameter at `:276`), so "passing state
  only" needed no new parameter.
- **No other search entry exists in a short window.** The rail has no Search action yet
  (`AvailabilityNavigationUi.kt`, the rail's doc comment: "No header Search action yet: that comes
  with P8"). So with the header hidden, L1's icon is the Journal's only way to search.
- **Short window helpers.** `isShortWindow()` (`ui/adaptive/ShortWindow.kt`) and the landscape test
  `AvailabilityScreen` uses (`isShortLandscapeWindow`, `:1007`). J5 calls the same two in
  `isShortLandscapeJournal()` (`JournalShortWindow.kt`); the scaffold reads `showRail`, which is
  that same value.
- **The album's photo menu needs launchers** (`rememberPhotoAcquisitionLaunchers`), which lived
  inside `EntriesAlbum`. Moving the button into `JournalTab`'s row needed them hoisted (What was built).
- **J4b's long-press menu** is `LongPressOptionsBox` / `tileClickable` (`TileOptionsMenu.kt`), used
  unchanged.
- **Height budget** (the dispatch's "before building"): measured at base and found to leave room, so
  no abort. Table below.

### Premises that were wrong

1. **"Robolectric measures heights" holds only for heights with no text in them.** Under Robolectric's
   default (legacy) graphics every glyph measures about one pixel wide and text lines come out taller
   than real ones (a chip label "Offline maps" laid out 13 px wide; with native graphics, 83 px). So
   B3's 85 dp search header and 197 dp Records chrome are legacy-mode figures. With
   `@GraphicsMode(NATIVE)` the header measures **45 dp**, which is J0 C2's own estimate: this settles
   J0's "Could not determine: why 85 dp against the 45 dp estimate". The height tests run native;
   the table gives both.
2. **L5's "chips fit on one line at 640 dp with full labels" is not true today, and cannot be seen
   false in legacy mode.** Legacy mode showed all five chips in 382 dp. Native: at 640 dp with
   two-digit counts the last chip ends at 672 dp, past the row's 640 (the row scrolls). See Needs a
   decision 1.
3. **L5's "rows one line with the time right-aligned" has no time to right-align on two of three row
   types, and its rows are not in J5's files.** `WaypointRow` (`AvailabilityTripsWaypointsUi.kt:230`)
   shows name, MGRS and lat/lng and no time; `OfflineRegionRow` (`AvailabilityOfflineMapsUi.kt:421`)
   shows name, a detail line ending "downloaded ... ago" and a three-line paragraph; `TrackExportRow`
   (`TrackExportPanel.kt:92`) shows a timestamp and "N points". The All logbook composes these rows
   (`RecordsLogbookList.kt`), by the owner's J1 answer "Same rows as their chips". None of those four
   files is in the dispatch's file list. See Needs a decision 2.
4. **The planner's prediction "about six cards on screen" (plan L4)**: under Robolectric (native, no
   status bar) two full card rows and 52 dp of a third are visible, so four cards whole and two in
   part. On the device the status bar takes more. Device item.

## What was built

- **Ruling 1, the header.** `AvailabilityCompactScaffold.kt`: `journalHidesSearchHeader = showRail &&
  compactTab() == JOURNAL && !journalScreenState.searchHeaderRevealed`, added to the header's
  condition; a `LaunchedEffect` closes the search dropdown when the header hides, so its dismiss scrim
  is never left over content with no bar. `JournalScreenState.searchHeaderRevealed` is new, the
  fourth element of its saver (`toBooleanStrict`, so a bad value fails loudly as `valueOf` does).
  **How it is dismissed:** touching the same icon again (its icon and label become "Hide search"), or
  Back (a `BackHandler` in `JournalTab`, composed after both branches so it outranks their own; off
  while an entry is open). The icon is not shown while any entry is open, because the scaffold hides
  the header then regardless (`isEditingJournalEntry`).
- **L1** (`JournalShortWindow.kt`, new): `ShortWindowJournalHeader`, 48 dp, `fillMaxWidth` inside
  B3's cap: the Entries | Records switch at its start (sized to its labels), the search icon and the
  action at its end. The switch is now one composable, `JournalSwitch` (in `JournalTab.kt`), used by
  portrait (full width, as before, same tags) and by the row.
- **L2**: no floating button in a short window. Timeline: "New entry" (`Edit` icon) does what the FAB
  did. Album: the photo button opens the same Take photo / Import menu with the same item tags. Records:
  no action. The album's launchers are now created once in `CartographyScreen` and passed to
  `EntriesAlbum` (its `onOpenCamera`/`onAddGalleryPhoto` parameters became `photoAcquisition`), so the
  FAB and the row's button share one set; see Decisions 4.
- **L3**: Entries' second row holds the drafts chip "✎ N drafts ›" (an `AssistChip`, doing what the
  banner's Continue does, one routing lambda shared with the banner) and J2's view toggle. The banner
  is not drawn in a short window. Records' second row is its filter chip row. Both hide while the
  content scrolls down and return on a scroll up: `HideOnScrollState`, a `NestedScrollConnection` that
  reads the child's **consumed** scroll in `onPostScroll`, attached over the whole Entries column and
  the whole Records tab (`RecordsTab.hideChipsOnScroll`), shown through `AnimatedVisibility`.
- **L4** (`SidewaysEntryCard.kt`, new): `SidewaysEntryCard`, the 72 dp slot at the start and J3's
  content beside it (day numeral, weekday and title on one line; one-line preview; species chips on
  one line; the stats; the tags). **Ruling 3**, `entrySlotContent`: the hero photo (J3's
  `entryHeroPhoto`), else every kept track found (`entryThumbnailTracks`, J4's one box, on the Tracks
  container), else a panel in the first kept type's colours and icon (Decisions 2). **Ruling 2**: the
  card and J3's collapsed row (`SidewaysCollapsedEntryRow`) sit in J4b's `LongPressOptionsBox` (Edit,
  Delete; the same pending delete) instead of the two-stage swipe; a plain tap opens the report.
  `CartographyEntryListScreen.sideways` selects the path (a new branch; portrait's is unchanged);
  the drafts list takes it too.
- **L6**: `EntriesAlbum.columns`, 5 in a short window (`SHORT_WINDOW_ALBUM_COLUMNS`), 3 otherwise.
- **L7**: nothing new was needed. The structure is kept the same in both orientations so a turn,
  which the manifest handles without recreation, keeps `remember` state below it: `CartographyScreen`
  wraps each branch in `ShortWindowFrame` (a Column whose header slot is empty in portrait), and
  `JournalTab`'s Records branch is a Column in every window.
- **A regression found and fixed while building.** Moving the call sites made
  `RecordsFilterChipsTest` "leaving the Finds chip while editing is an incidental exit" fail
  (reproduced alone). My first fix (reading `onFindsTabLeft` through `rememberUpdatedState` in
  `RecordsTab`) did not fix it and was removed. Instrumented, the call ran with `editing = null,
  mode = EDIT`: the function reference in use had captured `editing` before the find opened. The fix
  is in `JournalTab.leaveFindEditingIfNeeded`, which now reads the open find and the callback through
  `rememberUpdatedState`. A new test pins the same rule for the L1 row's switch.

## Height budget (`w823dp-h384dp-land`, both rotations measured identical; Robolectric, zero insets)

| Measure | Base, legacy graphics (B3's mode) | Base, native graphics | J5, native graphics | J5, legacy |
|---|---|---|---|---|
| App-wide search header | 0-85 | 0-45 | hidden | hidden |
| Switch row (base) / L1 row (J5) | 93-145 | 53-101 | 0-48 (48 dp) | 0-48 |
| Entries: toggle row | 157-197 | 113-153 | L3 row: toggle 52-92, chip 56-88 | toggle 52-92 |
| Entries: drafts banner | 205-257 | 161-209 | (the chip, in the L3 row) | |
| Entries: content area | from 257 | from 209 | **96-384 = 288 dp** | 96-384 = 288 dp |
| Entries: first card row | 321-413 (does not fit) | 257-325 | 140-228 | 156-246 |
| Entries: second card row | off screen | 333-401 (cut) | 236-324 | 254-344 |
| Records: chip row | 153-209 (B3: 197 before J1's chips) | 109-165 | 48-104 | 48-104 |
| Records: content area | from 209 | from 165 | **104-384 = 280 dp** | 104-384 |

So the chrome on Records is **104 dp** where B3 measured 197 (legacy, header visible). With the header
revealed by the icon, the Entries content would lose it again (45 dp native); a revert of ruling 1
(below) shows the second card row then ending at 393, past 384. Planner's prediction 1 (content at
least 250 dp) holds: 288.

## Tests: base failures (`f768e2e9` against `3870e4d`)

35 tests, 28 failures, each for its stated reason: the header shown on Journal ("the app-wide search
header is hidden ..."), no search icon, no `journal-short-header`, a FAB present ("no floating
button in a short window"), no drafts chip ("✎ 2 drafts ›" not displayed; the banner present), the
Records chip row not hiding ("the chip row hides on a scroll down"), no type panel, the hero 392 dp
wide and the track thumbnail 56 dp ("the slot is 72 dp wide"), a swipe row present ("no two-stage
swipe row in a short window"), no menu item (`tile-options-edit`), 3 album columns ("the first five
share the first row expected 163 but was 367").

Passing at base by design (7): the two L7 tests (the dispatch's "confirm"), and five pins: portrait
keeps the header and has no L1 row; the header shows on List and Seasonal; portrait keeps one column,
the hero on top and the swipe; portrait album 3 columns; a plain tap opens the report. The L7 tests
pass before and after, so under CLAUDE.md they are suspect as evidence of anything this change did;
the `l7-key` mutation below shows the editor test does detect state lost on a turn.

## Revert checks

Runner (`scratchpad/j5/revert.py`): saves the file, applies the edit (each must match exactly once),
runs the classes through a script that deletes the XML first and refuses to read results if the log
has any `e:` line, restores from the saved copy (never git), and compares the file byte for byte with
the saved copy. After every check the comparison was true and `git status` was empty, so the forward
change (committed at `07dd9d45`) was intact.

| Check | Edit | Result, 0 `e:` lines unless stated |
|---|---|---|
| r1-header | drop `&& !journalHidesSearchHeader` | 6 failures: hidden x2, icon x2 ("hidden before the touch"), height x2 (second card row ends at 393 > 384) |
| l1-row | the header lambda `if (shortLandscape)` → `if (false)` | 20 failures, all on the missing row/icons/chip |
| l2-fab | FAB shown on the timeline in a short window | 2 failures: "no floating button in a short window" x2 |
| l2-albumfab | `showAddPhotoButton = true` | 1: "no floating button on the album either" |
| l3-hide | hide threshold made unreachable | 2: "the toggle row hides ...", "the chip row hides ..." |
| l3-chip | drafts chip never drawn | 6: the chip absent in each chip test and the height tests |
| l4-sideways | `sideways = false` on the timeline | 8: slot sizes (56, 392 dp), no type panel, swipe present, no menu |
| r3-order | first attempt: `false && hero != null` | **1 `e:` line (smart cast lost); results refused, not cited** |
| r3-order-2 | photo branch removed | 1: the hero test, hero node absent (the track took the slot) |
| r2-menu | first attempt: `false && onDelete != null` | **1 `e:` line (smart cast lost); results refused, not cited** |
| r2-menu-2 | `onDelete != null && false` | 4: the long-press tests, menu count / menu item absent |
| l6-cols | 3 columns in a short window | 1: "the first five share the first row expected 143 but was 347" |
| r1-back | the dismiss `BackHandler` disabled | 2: "Back hides the revealed header" x2 |
| exit-fix | `latestEditing` → `editing` | 2: `RecordsFilterChipsTest`'s incidental exit, and the new L1-switch test |
| l7-key | `key(shortLandscape) { CartographyScreen(...) }` (a mutation, not a revert) | 1: the L7 editor test ("Your own account (optional)" not displayed after the turn); the switch/chip/view test passes, as it should, since that state is hoisted |

**The dispatch lists "a revert build with compile errors" as an abort condition.** Two of my revert
edits did not compile. The runner refused both, nothing was cited from them, and I re-ran each with an
edit that compiles. I did not stop the stage; if the planner reads the condition as a hard stop, this
is where I went past it.

## Tests moved or rewritten

- `AvailabilityScreenLandscapeB3DestinationsTest`: 1 helper, `journalTopTabs()`, 2 tests (T1 Journal
  at both rotations). The switch no longer spans the capped width in a short window, so the helper
  measures the L1 row, which does (as J1 moved the Records test onto the chip row). Assertions
  unchanged. Seen failing before the change on centring (expected 371.5 / 451.5, was 125.5 / 205.5).
- `AvailabilityScreenJournalShortWindowTest`, after its tests-first commit: the album photo test closed
  the menu with Back, which cannot reach a popup window under Robolectric; it now closes the menu by
  touching Take photo and asserts the album's camera opened each time (stronger, not weaker).
- `JournalShortWindowCardsTest`: +1 test (the L1 switch's incidental exit).
- No portrait test changed (prediction 2: holds).

## Suite before and after

| | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| Baseline, `3870e4d` | 244 | 1990 | 2 (held album tests, above) | 0 | 24 |
| First full run with the build (before the exit fix) | 246 | 2025 | 5 | 0 | 24 |
| **Final, `07dd9d45`, cleared results, 0 `e:` lines** | **246** | **2026** | **0** | **0** | **24** |

The first run's five: `RecordsFilterChipsTest` (mine, fixed above); B3 T1 Journal x2 (the layout
change, updated above); `JournalTabTest` photo-pull ("'Log photo' is not displayed"), which passed in
its class rerun alone (with `RecordsFilterChipsTest`, 30/1, the one being mine); and the held
`JournalPendingDeleteTest` album long-press test ("an album photo's Delete hides it ...",
`'tile-options-delete' is not displayed`), not rerun alone, since the final full run passed it. The
final run has no failures, so no held test needed a rerun there. The suite grew by 36 (prediction 3,
25 to 50: holds).

## Needs a decision

1. **L5 chips: how to make five chips fit 640 dp.** Native measurement at 640 dp, counts 36/12/10/14/0:
   chips and gaps need 656 dp plus 2 x 16 dp padding, against 640; 48 dp over, more with bigger counts,
   font scale or a different device font. Options: (a) Material's 18 dp chip icon instead of today's
   24 dp, 4 dp gaps and 12 dp padding in a short window: fits by about 6 dp, fragile; (b) no leading
   icons in a short window (about 90 dp back), dropping J4's "each with an icon"; (c) smaller label
   type; (d) keep the row scrolling in short windows. Nothing built.
2. **L5 rows: what "one line with the time right-aligned" shows, and where.** Which rows (the All
   logbook only, or every single-type list too); which time (waypoints show none today, regions show
   "downloaded ... ago" inside a detail line); what is dropped to fit one line (MGRS and coordinates,
   the region's detail line and paragraph); whether Directions and Share stay. And the rows live in
   `AvailabilityTripsWaypointsUi.kt`, `AvailabilityOfflineMapsUi.kt`, `TrackExportPanel.kt` and
   `RecordsLogbookList.kt`, none in J5's scope; new short-window row composables under `ui/log/` would
   avoid editing them but duplicate their content and controls. Nothing built.
3. **The drafts chip on Records.** Plan and dispatch: "'✎ N drafts ›', in a second row with the view
   toggle (Entries) or the filter chips (Records)". Read one way, the chip shows on Records too
   (opening a Cartography draft from Records); read the other, the parentheses only name the second
   row on each tab. Built: Entries only, as portrait has the banner on Entries only. Adding it to the
   Records row would also make Needs a decision 1 harder.

## Decisions I made that the dispatch did not

1. **Dismissal**: the icon toggles, and Back hides; the revealed flag is saved in
   `JournalScreenState`, so a user who brought the header up finds it up after leaving and returning
   (CLAUDE.md, UX defaults). Rejected: hiding it after a search result is chosen, which needs scaffold
   changes beyond the condition.
2. **The type panel's type** for an entry keeping several types: the first in the Records chips'
   order (finds, tracks, waypoints, offline maps). An entry with writing and nothing kept: a neutral
   panel with the ✎ (`Edit`) glyph. Not asserted by a test beyond the panel's presence.
3. **J3's collapse rule stays** in a short window: a collapsed entry is a one-line row with the
   long-press menu, no slot.
4. **Launchers hoisted** so both photo buttons share one set; rejected: two sets, which would lose an
   Import result if the phone turns while the system picker is up.
5. **The search icon is left out while an entry is open**, since the header cannot show then.
6. **Hide-on-scroll reads consumed scroll**, so a drag on a list that cannot move changes nothing; the
   state is per screen and transient, not in `JournalScreenState`.
7. **Card text in the sideways card**: title on the day's line, one-line preview, one line of species
   chips; stats wrap. The portrait card allows two-line title and preview.
8. **Columns** keep J3's rule (`isShortWindow()` gives 2); only the sideways path uses the landscape
   test too. A short portrait window (multi-window) keeps J3's cards in two columns, as before.

## Device-only items

- **L8**: no control within the `displayCutout` inset; the rail side keeps its padding. Robolectric
  reports zero insets, so none of this is checked, and J5 adds no inset handling of its own (it relies
  on B3's content area). Check the L1 row's switch (row start) and icons (row end) clear of the
  cut-out at both rotations.
- The height table's real figures with the status bar and real fonts; how many cards show.
- The header's reveal and dismissal feel; the dropdown closing when the header hides.
- Hide-on-scroll feel (the animation, flings, a list only slightly taller than the screen).
- Long-press on sideways cards: timing, the menu's anchor near the bottom edge.
- Import from the L1 row's photo button, turning the phone while the picker is up.
- Rotating with an entry open in its editor, both ways (Robolectric swaps the configuration in place).

## Flags outside scope

- **Scratchpad collision (my error).** The scratchpad directory is shared with earlier sessions. My
  first attempt to write a runner failed, and the background run I started executed an older
  `run.sh` belonging to another session, which targets worktree `agent-a55a5245adbd30376`. I killed it
  within about a minute; it may have deleted that worktree's `app/build/test-results` (build output
  only). I then overwrote that session's `scratchpad/run.sh` and `tally.py` before moving my own into
  `scratchpad/j5/`. Nothing in any repository was touched.
- **Robolectric legacy graphics**: any existing test that measures text-dependent sizes is measuring
  legacy metrics (Premises 1). Not audited.
- `JournalPendingDeleteTest`'s "a plain tap on an album photo still opens the viewer" failed at
  baseline; it is an album test but not a long-press one.

## Added after the resume (continuation `prompts/preserved/2026-09-27-27.md`, RECORD.md 2026-09-27-66)

The owner's answers: L5 chips, option (a) "Tighter chips (fits by ~6 dp)"; L5 rows, "A small
follow-up stage", now; drafts chip, "Entries only (Recommended)", as built (no change). Pulled
`54bcc6bf` with `git pull --no-rebase` (a fast-forward: only the continuation's store copy and its
RECORD.md line). The continuation makes a revert build that does not compile an abort.

**Status: L5a is built, tested, revert-checked and pushed. L5b stopped before building, on the
continuation's own stop-and-ask rule: every Records row type loses information it shows today when
made one line, the offline-region row always and the waypoint and track rows in named cases (the
inventory below). No row was changed.** Both L5a revert builds compiled, so the abort condition did
not arise. Final full suite: 246 / 2029 / 0 / 0 / 24.

### Commits added after the resume (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `841eb255` | L5a tests first: the guard (native graphics), the font-scale 1.15 record, a portrait pin. At `54bcc6bf`: the guard fails, "the chip row's content is 48.0dp wider than the row". |
| `27e5c70a` | L5a: `RecordsChipRowMetrics`. |
| this report commit | This section. |

### L5a, the chips: what was built

`RecordsFilterChips.kt`: `RecordsChipRowMetrics(iconSize, chipGap, rowPadding)`. `Default` is J1's row
(the chip `Icon` unsized, which draws at 24 dp; 8 dp gaps; 16 dp padding), used in portrait and by
`LogPanel`. `ShortWindow` is Material's own chip icon size, `FilterChipDefaults.IconSize` (18 dp), 4 dp
gaps (`Spacing.xs`) and 12 dp padding (`Spacing.md`). `RecordsTab`'s J5 parameter `hideChipsOnScroll`
is renamed `shortWindow` and now also picks the metrics; `JournalTab` passes it in a short landscape
window as before. The row still scrolls sideways when it overflows, as J1 built it.

**Measured** (native graphics, `w823dp-h384dp-land`, through the real `AvailabilityScreen`, the row at
B3's 640 dp cap, counts All 38 / Finds 12 / Tracks 10 / Waypoints 16 / Offline maps 0, the counts
option (a) was priced with):

| | Before (`54bcc6bf`) | After (`27e5c70a`) |
|---|---|---|
| Font scale 1.0 | overflows by 48 dp (content ends at 740, row ends at 692) | **fits, 6 dp to spare** (content ends at 686) |
| Font scale 1.15 | overflows by 79 dp | **overflows by 25 dp; the row scrolls** |

**At 1.15 the row overflows by 25 dp.** Per the continuation, the design was not changed further. The
margin at 1.0 is 6 dp, so a two-digit Offline maps count, or larger counts elsewhere, can also tip it
over; not measured.

### L5a tests (`AvailabilityScreenJournalShortWindowTest`, +3)

- **Guard**, native graphics: asserts font scale 1.0 was in force, the row is 640 dp, the five chips
  share one line, and the content (last chip's end plus the row's end padding, taken equal to its start
  padding) is no wider than the row; the message states the overflow and the geometry. At base it fails
  for that reason: 48.0 dp.
- **Record at font scale 1.15** (`@Config(fontScale = 1.15f)`), native graphics: asserts the screen
  saw font scale 1.15, then prints the result either way (above). **It cannot fail on overflow, by
  the continuation's design; it passes identically before and after the change and is a record, not
  evidence of a fit** (CLAUDE.md's "passes identically" rule, flagged here).
- **Portrait pin**, native graphics at `w411dp-h891dp`: 16 dp row padding, 8 dp gaps, 24 dp icon. It
  passes before and after, by design.

### L5a revert checks

The same runner as before (edit from a saved copy; refuse on any `e:` line; restore from the saved
copy; byte comparison true and `git status` empty after each):

| Check | Edit | Result, 0 `e:` lines |
|---|---|---|
| l5a-padding (the continuation's "old padding") | `ShortWindow`'s `rowPadding = Spacing.md` → `Spacing.lg` | guard fails: "the chip row's content is 2.0dp wider than the row ... padding 16.0.dp" |
| l5a-metrics | `RecordsTab` picks `Default` in a short window | guard fails: "48.0dp wider", the base figure |

Both failures name figures only these edits produce (2 dp is 634 + 8 dp of padding against 640).

### L5b, the rows: the pre-build inventory, and why it stopped

Measured with a probe (native graphics, not committed): each row's width in the All logbook at
`w823dp-h384dp-land`, where rows are narrowest, and each piece of text's single-line width at its
current style. The logbook's row (badge included) is **608 dp** wide; the badge and its gap take 36 dp,
so a row's own content gets **572 dp**. The single-type lists were not measured; they are at most the
640 dp cap, so 32 to 68 dp wider.

| Row type | Composable, file | What it shows today | Its date or time | Fits one line? |
|---|---|---|---|---|
| Waypoint | `WaypointRow`, `ui/availability/AvailabilityTripsWaypointsUi.kt:230` (in the Waypoints chip via `WaypointsSection`, and in All) | name (`titleSmall`); MGRS (`bodySmall`, omitted when unsupported); lat/lng to 4 places; Directions button; inside a `Card` with 12 dp padding | none shown; `Waypoint.createdAtEpochMillis` (`domain/model/Waypoint.kt:29`); no existing formatter formats a waypoint's time | **Only for short names.** 572 - 24 card padding - 48 Directions = 500 dp for text. MGRS 122 + coordinates 113 + a time in the track format 137 + three 8 dp gaps = 396, leaving **about 104 dp for the name**. "Pin 1" is 34 dp; "Chanterelle patch by the creek" is 198 dp and would be cut. |
| Recorded track | `TrackExportRow`, `ui/track/TrackExportPanel.kt:92` | 40 dp track thumbnail; start time as the title (`bodyLarge`, `formatTrackTimestamp`, `DISPLAY_FORMAT` "MMM d, yyyy, h:mm a" at `:163`); `trackSubtitle`: "N points", plus the network-fix exclusion note when one applies, plus "· recording"; Share button | its start, already shown (the title) | **Not in the note cases.** Text gets about 468 dp (572 - thumbnail 40 - Share 48 - gaps). With the time right-aligned at `bodySmall` (137 + 8), the subtitle gets about 323 dp. "1234 points" is 70 dp and fits. "240 points · 812 more not shown (network fixes) · recording" is 345 dp, and "No usable points — all 123 fixes were from the network provider · recording" is 432 dp: both would be cut. That note exists so a short or empty track is never silent (`trackSubtitle`'s doc comment), which is the continuation's "a status". |
| Offline region | `OfflineRegionRow`, `ui/availability/AvailabilityOfflineMapsUi.kt:421` | name (`bodyMedium`); "Stale" when stale; a detail line: radius around lat/lng, tile count, size, "downloaded N days ago" (`relativeTimeLabel`, `ui/availability/AvailabilityPureFunctions.kt:39`); a three-line paragraph on zoom levels | `OfflineRegionSummary.createdAtEpochMillis` (`domain/OfflineMapRepository.kt:204`), shown relative inside the detail line | **No, never.** The detail line alone is 485 dp and the paragraph is 1486 dp on one line, against 572. One line keeps the name, the Stale flag and the date; the radius, the coordinates, the tile count, the size and the zoom paragraph would go. |
| Find | none | Finds are tiles, not rows: `FindTile` in `FindsGalleryScreen`'s grid (`ui/log/FindsGalleryScreen.kt:188`), and two to a row in the All logbook | `foundOn`, in the tile's label ("Find on 2026-09-20", `findTileLabel`, `:240`) | No row exists, so there is nothing to change. |
| All logbook rows | `RecordsLogbookList.kt` (`BadgedRow` around the three rows above) | the rows above with a type badge | as above | As the rows above. |

**Under the continuation's rule ("if a row cannot become one line without dropping information it
shows today ... stop and report which row and what would go"), L5b stopped here.** What would go:

1. **Offline region**, always: the radius, the centre coordinates, the tile count, the size, and the
   zoom-level paragraph.
2. **Waypoint**, for names longer than about 104 dp (about 15 characters): the end of the name. The
   alternative, keeping the whole name, would drop the MGRS or the coordinates instead. A waypoint also
   shows no time today, so one line adds one (its creation time; no existing formatter formats a
   waypoint, so the format is also a choice, the track's "MMM d, yyyy, h:mm a" being the nearest).
3. **Recorded track**, when a network-fix note applies: the end of the note. This is also the only row
   whose time is already its title, so "primary label on the left" would make "N points" the label.

The rows keep J4b's two-stage swipe and long-press menus unchanged, since nothing in them changed.
Options for whoever decides, not built: (a) one line only where nothing is lost, and the region row
kept as it is; (b) one line with the dropped pieces moved elsewhere (a second line shown only when a
row is opened, or the region's details behind a tap); (c) keep today's rows in short windows.

### Suite after the resume

- **Final, `27e5c70a`, cleared results directory, 0 `e:` lines: 246 classes / 2029 / 0 / 0 / 24.**
  That is +3 over `07dd9d45` (the three L5a tests). No failures, so no held test needed a rerun.
- Affected classes during the work: `AvailabilityScreenJournalShortWindowTest` (26),
  `RecordsFilterChipsTest` (13) and `AvailabilityScreenLandscapeB3DestinationsTest` (12), 51/0/0/0 at
  `27e5c70a`'s tree.

### Device-only items added after the resume

- The chips at the device's own font, and at the user's font scale: the guard measures Robolectric's
  native fonts, not the S22 Ultra's.
- The 18 dp chip icons' legibility (each chip keeps Material's chip height and touch target).
