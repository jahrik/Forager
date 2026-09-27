# 2026-09-27: Journal redesign J3, completion report

Dispatch: `prompts/preserved/2026-09-27-20.md` (build, J3 coder). Plan: `docs/plans/journal-redesign.md`
(J5, J6, J9, "Build order" J3, "Rules for every build stage"). Map:
`docs/audits/2026-09-27-journal-j0-pulse.md` (Part B and the owner's rulings). Previous stages:
`2026-09-27-journal-j1-completion-report.md`, `2026-09-27-journal-j2-completion-report.md`. Written by
the J3 coder in a cloud worktree on local branch `j3`, which tracks `origin/journal-redesign`.

**Status: C1 to C6 are built, tested and pushed.** One question needs the owner: what an Entries card
should draw when its entry keeps **two or more tracks** (see "Needs a decision"). For now such a card
shows no thumbnail, and its track stat is the sum. Everything else follows the dispatch.

## Commits (all pushed to `journal-redesign`)

| SHA | What |
|---|---|
| `8331afc3b13d908a36475ada332ccf012bc1bba6` | C1 tests first (11, new `JournalEntryCardsTest`). |
| `0bbfe8737f852cf2faf18dc53764908832b34949` | C1: `CartographyEntryCard.kt` (new), list rewritten, `distanceUnit` passed; label-driven tests updated. |
| `2f7a93bb5327aeebc855d97d341521356971e100` | C2 tests first (3). |
| `b2701024ed360d410f8548481ff649b79b52914f` | C2: hero photo. |
| `2342406bc180de2ccd1baf98131750d74cbcd8d4` | C3 tests first (9 headless in new `TrackThumbnailProjectionTest`, 5 in `JournalEntryCardsTest`), plus a projection stub. |
| `08af83a93cf1b965ad4e763e581217d47f28c918` | C3: `TrackThumbnail.kt`, on Recorded Tracks rows and Entries cards. |
| `0b213576c50855771d3c4537d1d114e0d20a4dd4` | C4 tests first (1). |
| `26746275cdfda775b330e8624ba42b6a363b56b8` | C4: one column in compact portrait. |
| `4ad959f70be2761322c6c52869c47a8ebab87f21` | C5 tests first (1). |
| `6eb7e2ad2d7dffc29a920b22b331cadec92266ee` | C5: the find badge leaves out draft finds. |
| `c592fdb1da5e83be23edbd8ff9eebeaacd6c1861` | C6 tests first (3, new `PhotoEntryReferenceCountTest`), plus a behaviour-preserving extraction. |
| `4aaf5f46045204c14076860681d113d5044308cd` | C6: the fallback is logged. Final code head. |
| this report's commit | This file. |

Production diff `558249c..4aaf5f4`: 9 files, all under `ui/log/`, plus `ui/track/TrackExportPanel.kt`
and `MainActivity.kt`. `AvailabilityScreen.kt`, `AvailabilityCompactScaffold.kt`, `LogPanel.kt` and
`RecordsTab.kt` are untouched. One file outside the dispatch's list, `ui/log/EntriesDrafts.kt`: see
"Decisions" 2.

## Premises checked before building

- **Base.** `origin/journal-redesign` was `558249cdb7b18226513a1cf841b0a1206b77f0c9` when fetched,
  the planner's store-copy commit after `558f090`, as the dispatch allows.
- **Baseline at the base.** Cleared results directory, `LC_ALL=C.UTF-8`, build log with no `e: `
  lines. It came from the C1 tests-first run, which is the base plus the new test file: 234 classes /
  1847 tests / 11 failures / 0 errors / 24 skipped. All 11 failures are in the new class, so every
  other class stands at **233 / 1836 / 0 / 0 / 24**, the planner's figure. An earlier run at the bare
  base read 233 / 1836 / 0 / 0 / 24 too. It went through a runner script left in the shared
  scratchpad by an earlier session, which I did not read before it ran, so I do not cite it as
  evidence.
- **Runner** (scratchpad `j3/run.sh`, `j3/revert.sh`).
  - Every Gradle run clears `test-results` first, runs with `LC_ALL=C.UTF-8`, refuses to read results
    if the build log has `e: ` lines, and refuses a run with no XML.
  - `revert.sh` saves a copy of the file and applies a one-line edit, aborting unless the old text
    matches exactly once. It runs the classes, restores the file from the saved copy (never from
    git) and `cmp`s the two.
  - After each revert, `grep` confirmed the forward line was present before the commit.
  - Two runs did not compile (a missing `getOrNull` import in the C2 tests; a colon in a C3 test
    name). The runner refused both, and the fixed rerun is the one cited. One revert edit matched
    twice and was refused before anything ran; I widened its context and reran it.
- **Citations.**
  - `CartographyEntryListScreen.kt:55`: `columns: Int = 2`. Holds.
  - `CartographyEntry.date` is a `LocalDate` (`domain/model/CartographyEntry.kt:52`). Holds.
  - `TrackExportRow` is at `ui/track/TrackExportPanel.kt:90`. Holds.
  - `TrackRecordingUiState.tracks` is at `ui/track/TrackRecordingUiState.kt:80`. Holds.
  - `MainActivity.kt:104` is the `getPhotoEntryReferenceCount = { … getOrDefault(0) }` lambda. Holds.
  - `CartographyScreen` receives `galleryPhotos` (`CartographyScreen.kt:87` at base). Holds.
- **Where a kept find's identification lives:** `FindDecision.ownIdentification: String?`
  (`domain/model/CartographyEntry.kt:97`). The field exists, so the "holds none" abort does not
  fire. It is nullable per find; see "Decisions" 4.
- **Formatter reused:** `trackSubtitle(distanceMeters, durationMillis, distanceUnit)`
  (`ui/log/CartographyEntryEditScreen.kt:660`). It is the one formatter behind the edit and report
  screens' track rows, and it builds on `formatDistanceMeters` (`domain/model/DistanceUnit.kt:71`).
  Its doc comment warns against `formatDistanceKm`. So the "no formatter" abort does not fire.
- **How the track list reaches the compact tree** (J0 B3 confirmed): `MainActivity.kt:544`
  `tracks = trackUiState.tracks`, then `AvailabilityScreen` parameter `:674`, then
  `CompactMainScaffold(… tracks = tracks …)` `:1378`, then scaffold parameter
  `AvailabilityCompactScaffold.kt:190`, then `JournalTab(… tracks = tracks …)` `:987`, then
  `JournalTab`'s `tracks` parameter (`JournalTab.kt:204` at base), which it already handed to
  `RecordsTab`. The shortest way to `CartographyScreen` was **one new parameter on
  `CartographyScreen`**, passed from `JournalTab`. No ViewModel, scaffold or `AvailabilityScreen`
  change.
- **Draft find ids for C5:** `MushroomLogUiState.draftEntries` (`ui/log/MushroomLogUiState.kt:42`),
  already `JournalTab`'s `uiState`.
- **Logging pattern for C6:** the `ErrorLog` seam (`domain/ErrorLog.kt:25`), with the real one at
  `MainActivity.kt:51` (`androidErrorLog`, backed by `Log.w`). The pattern it follows is right beside
  the line: `autoSaveLocationToPhotos = { …getOrElse { error -> androidErrorLog.w("PhotoLocation", …,
  error); false } }` (`MainActivity.kt:112-117` at base), which logs and then returns a fallback. The
  pattern does not make a fallback value wrong; it requires the fallback to be logged. So 0 stays, and
  the "stop and ask" branch did not fire. See "Flags" for what 0 hides.
- **Entry order,** which the month grouping relies on: `GetCartographyEntriesUseCase.kt:31` sorts by
  date, newest first. Drafts are sorted by `updatedAtEpochMillis` (`GetCartographyDraftEntriesUseCase.kt:10`).
- **Sticky headers in a grid:** `LazyGridScope.stickyHeader` exists in the resolved
  `foundation-android` 1.12.0 (read with `javap` from the Gradle cache). No new dependency.

### Premises that were wrong

- None of the dispatch's cited lines or types failed.
- **Planner's predictions.**
  - 1 holds: no new dependency.
  - 2 holds: one new parameter carries the tracks, on `CartographyScreen`, and there is no ViewModel
    change. `CartographyEntryListScreen` and `DraftsListScreen` each gained one too.
  - 3 holds: C6's production change is 8 lines in the function body and wiring, with the rest doc
    comment.
  - 4 holds: +33 tests, 0 failures, skipped unchanged at 24.

## What was built

**C1, the cards** (`ui/log/CartographyEntryCard.kt`, new; `CartographyEntryListScreen.kt`).
- **Sticky month headers:** `stickyHeader` per run of same-month entries, reading "SEPTEMBER 2026".
- **Card layout.** A large day numeral and a capitalised short weekday ("26", "SAT", device locale)
  replace the ISO date. Next to them:
  - the first line of the text as the title, with the rest as a two-line preview;
  - species chips from kept finds' `ownIdentification`, each species once, in the Finds
    `RecordTypeStyle` colours;
  - a stats row by type, each in its `RecordTypeStyle` colours with the Records chips' icons. Finds
    read "3 finds" or "1 find". Tracks show kept tracks' summed `distanceMeters` and `durationMillis`
    through `trackSubtitle` ("4.2 km · 2h 10m", or "2.6 mi · 2h 10m" in miles). Waypoints read "1
    waypoint". Offline maps read "2 offline maps". A type with nothing kept has no stat;
  - the tags, as before.
- **Kept only.** Every figure reads kept decisions only; a withheld decision is not part of the
  entry. That is how the old tile's count worked too.
- **Collapse.** An entry with no hero, no text and no kept track collapses to a short row with its
  day, weekday and stats on one line, or "Nothing kept".
- **No fixed shape.** The old 0.85 aspect ratio is gone; cards size to their content.
- Pure helpers (`entryTitleAndBody`, `entrySpecies`, `entryStats`, `isCollapsedEntry`,
  `groupEntriesByMonth`) hold the rules without Compose.

**C2, the hero.** `entryHeroPhoto`:
- sorts `CartographyEntry.photos` by `attachedAtEpochMillis`;
- looks each up in the gallery photos `CartographyScreen` already receives;
- takes the first that resolves, drawn with `DecodedPhoto` at full width and 140 dp tall, on top of
  the card.

None resolves, no hero. The collapse rule reads the resolved hero, so an entry whose only photos were
deleted collapses. No query, column or migration.

**C3, the thumbnail** (`ui/log/TrackThumbnail.kt`, new).
- **Projection.** `projectTrackToBox` is pure. It projects by bounding box and one linear scale for
  both axes, centred, north up, with longitude scaled by the cosine of the middle latitude.
- **Drawing.** `TrackThumbnail` is a Compose `Canvas` `drawPath` in the Tracks accent colour. The
  path is remembered, keyed on track id, point count and box size. Fewer than two points composes
  nothing.
- **Rows.** On Recorded Tracks rows, a 40 dp thumbnail sits in front of the text, from the row's own
  `track.points`. The All logbook reuses `TrackExportRow`, so its track rows get it too.
- **Cards.** On Entries cards it is a 56 dp thumbnail at the card's end. The card looks up its one
  kept track by id in `tracks`, passed from `JournalTab` to `CartographyScreen` to the list. A track
  not found shows no thumbnail. There is no database read per card.

**C4.** `JournalTab` passes `columns = 1` to `CartographyScreen` in compact portrait, and today's 2 in
a short window (see "Decisions" 1). `LogPanel`'s `columns = 3` is unchanged.

**C5.** `EntriesAlbum` takes `draftFindIds`. The find badge shows when
`referencingEntryIds.any { it !in draftFindIds }`. `JournalTab` passes
`uiState.draftEntries`' ids through `CartographyScreen`. No query. The delete dialog's count is
unchanged.

**C6.** `MainActivity.kt:104` now calls `photoEntryReferenceCountOrZero(id, …::forPhoto,
androidErrorLog)`. That function is new, top-level and `internal` in `MainActivity.kt`, so the rule
can be tested without the Activity. It returns `getOrElse { errorLog.w("PhotoReferenceCount",
"Couldn't count journal entries keeping photo $id; showing 0.", error); 0 }`. The value shown is
unchanged.

## Tests: base failures

Each tests-first commit was run at its own base (the previous commit plus the new tests), from a
cleared results directory, with the build log clean.

- **C1 at `558249c`.** 11 of 11 fail: `entry-card-*`, `entry-row-*` and `entries-month-*` are absent,
  and the card text ("3 finds", "C. formosus", …) is not found.
- **C2 at `0bbfe87`.** 3 of 3 fail:
  - `entry-hero-hero-p-mid` is absent;
  - `entry-card-photo-only` is absent, because a wordless photo entry collapsed.
- **C3 at `b270102`.** 13 of 14 fail:
  - the 8 projection tests fail on "point count expected:<n> but was:<0>" (the stub);
  - the 5 card and row tests fail on an absent `entry-track-thumbnail-with-track-a` or
    `track-thumbnail-tr-a`;
  - **the 14th, "fewer than two points project to nothing", passes against the stub by
    construction.** It is an absence check, and a revert check covers it (below).
  - Each card or row absence test carries a positive control in the same test, which is what fails at
    base.
- **C4 at `08af83a`.** 1 of 1 fails: "card 0 ends at the list's end padding expected:<344.0> but
  was:<176.0>".
- **C5 at `2674627`.** 1 of 1 fails: `entries-album-badge-find-p-draft` exists.
- **C6 at `6eb7e2a`,** with the extraction in place and still unlogged. The failure test fails with
  "one log call: [] expected:<1> but was:<0>". **The two success-path tests pass at base by
  construction**, since nothing logged yet. A revert check covers them.

**Test corrections made at forward runs, not code changes.** The first forward runs of C1 and C2
failed for reasons the predictions did not include. In each case I fixed the check (CLAUDE.md, "a
failure that doesn't match the prediction means the test is wrong"):

- **C1.** Text lookups inside a card used the merged tree, where a clickable `Card` swallows its
  children's text nodes. They now use `useUnmergedTree = true`.
- **C1.** The sticky-header test assumed one column, which C1 alone does not give. It now has 28
  entries and accepts "scrolled away or moved up under the header".
- **C2.** Hero tag lookups had the same merged-tree problem. They now use the unmerged tree, including
  the zero-count checks, so those cannot pass vacuously. The Compose error message itself suggested
  `useUnmergedTree`.
- These went in with each implementation commit. Every corrected line sits after the assertion that
  failed at base, and I did not re-run the corrected files at base.

**What the tests assert.** They go through the real `JournalTab`, at `w360dp-h640dp`. Back goes
through the Activity's `OnBackPressedDispatcher`.

- **Card text.** The day "26" and "SAT"; the exact title, with the second line not part of it; no ISO
  date.
- **Species chips.** "C. formosus" appears once for two finds, "B. edulis" is shown, and the withheld
  "A. muscaria" is absent.
- **Stats.** "3 finds", "4.2 km · 2h 10m", "1 waypoint", "2 offline maps"; "2.6 mi · 2h 10m" in miles;
  singulars; no stat for a type with nothing kept. The old "kept item" text is gone.
- **Month headers.** "SEPTEMBER 2026" and "AUGUST 2026" in order, each above its cards. The header
  stays in place while 28 cards scroll under it.
- **Collapsed rows.** They show day, weekday and stats, at 64 dp or less and shorter than a full card.
  An empty entry says "Nothing kept".
- **Hero.** The earliest existing photo is chosen and a deleted earlier one skipped. The hero sits at
  the card's top, above its text. No photo means no hero. A photo entry is a card, and a deleted-photo
  entry collapses.
- **Card thumbnails.** Present with two or more points and inside the card. Absent with one point, for
  a track not in the list, and for a withheld track.
- **Row thumbnails.** Present at 2 points; absent at 1 and at 0, with the row itself present.
- **Columns.** Each card spans the list's width inside its 16 dp padding, and each sits below the one
  before.
- **Find badge.** A draft-only photo has no find badge; a saved-find photo and a both photo have one.
- **C6.** A failure returns 0 and logs once, with the photo id, "0" and the cause. A success, whether
  3 or 0, logs nothing.
- **Touches.** A card and a collapsed row are each touched with `performTouchInput` at three points
  (12%/30%, 50%/50%, 88%/70% of their own bounds), and each touch opens that entry. The Records
  switch, the Tracks chip and the album toggle are centre touches, because they are navigation, not
  under test. The Recorded Tracks row has no row tap (only its Share button), so there is no row
  touch test.

## Revert checks

Every build log was clean, every file was restored identical by `cmp`, and every forward line was
`grep`-confirmed before its commit. Each failure below names this edit's own case.

| Behaviour | Edit | Predicted | Observed |
|---|---|---|---|
| Collapse | `if (isCollapsedEntry(entry, hasHero = false)) {` → `if (false) {` | the 3 collapse tests fail | exactly those 3: `entry-row-empty` not displayed; `entry-row-bare` not found (×2); 3/11 |
| Hero: earliest | drop `.sortedBy { it.attachedAtEpochMillis }` | the earliest test fails | `entry-hero-hero-p-mid` not displayed; 1/14 |
| Hero: skip deleted | `minByOrNull { … }?.let { photosById[…] }` (earliest, no fall-through) | the earliest test and the no-photo test's positive control fail | exactly those 2; 2/14 |
| Projection: cosine | `lngScale = cos(…)` → `1.0` | the cosine test fails | `y of point 0 expected:<100.0> but was:<75.0>`; 1/9 |
| Projection: two points (covers the test that passed at base) | `if (points.size < 2)` → `if (points.isEmpty())` | the fewer-than-two test fails | `expected:<[]> but was:<[ThumbnailPoint(x=50.0, y=50.0)]>`; 1/28 |
| Thumbnail composes nothing below two points | the same edit in `TrackThumbnail` | the card and row one-point tests fail | `track-thumbnail-tr-b` found; `entry-track-thumbnail-with-track-b` found; 2/19 |
| Thumbnail not-found path | `tracksById[id]` → `tracksById[id] ?: tracksById.values.firstOrNull()` | the not-found test fails | `entry-track-thumbnail-with-track-b` found; 1/19 |
| One column | `COMPACT_PORTRAIT_ENTRY_COLUMNS = 1` → `2` | the columns test fails | `expected:<344.0> but was:<176.0>`; 1/20 |
| C5 draft exclusion | `.any { it !in draftFindIds }` → `.any { it !in draftFindIds \|\| true }` | the badge test fails | `entries-album-badge-find-p-draft` found; 1/46 |
| C5 wiring (extra) | `JournalTab` passes `draftFindIds = emptySet()` | the badge test fails | the same; 1/21 |
| C6 log | the `errorLog.w(…)` line → `error.hashCode()` | the failure test fails | `one log call: [] expected:<1> but was:<0>`; 1/3 |
| C6 silent on success (covers the tests that passed at base) | log on every call | all 3 fail | both success tests show one `always` entry; the failure test shows 2 calls; 3/3 |

## Label-driven test changes

Cards no longer carry the ISO date that older tests used to find them. Each call site was repointed in
C1's commit (`0bbfe87`) to the card's own tag (`entry-card-<id>` or `entry-row-<id>`), with every
assertion unchanged. Nothing was disabled, skipped or weakened.

| File | Call sites | Tests that had failed |
|---|---|---|
| `ui/log/JournalEntriesTest.kt` | 16 (`onNodeWithText(x.date.toString())` 15, `onAllNodesWithText` 1) | 9 |
| `ui/log/CartographyScreenTest.kt` | 7 (`"2026-08-01"`) | 11 |
| `ui/availability/AvailabilityScreenBackNavigationTest.kt` | 14 (13 clicks, 1 absence check at `:889`) | 11 |
| **Total** | **37** | **31** |

- **The one absence check** (`AvailabilityScreenBackNavigationTest:889`) still passed, but only
  because the date string is gone. It was repointed so it keeps its meaning, "the entry left Entries"
  (CLAUDE.md, "a check that passes identically before and after").
- **One fixture changed.** `JournalEntriesTest`'s `FAB_ENTRIES` went from 12 to 30 entries, and its
  comment says why. Those wordless entries now collapse to short rows, and 12 rows no longer filled the
  screen, so the FAB test's own setup check ("a card lies under the button") failed. Its assertions
  stand.
- **Tallies.** The CartographyScreen and JournalEntries counts come from the first forward run of
  those classes (28 failures there, 8 of them in the new class). The BackNavigation count comes from
  the C1 full run, which had 11 failures, all in that class.

## Suite before and after

- **Before,** at `558249c`: 233 classes / 1836 / 0 / 0 / 24 (from the C1 base run, as above).
- **After,** at `4aaf5f4`, cleared results directory, build log clean: **236 classes / 1869 tests /
  0 failures / 0 errors / 24 skipped.**
- **Difference:** +3 classes (`JournalEntryCardsTest` 21, `TrackThumbnailProjectionTest` 9,
  `PhotoEntryReferenceCountTest` 3), +33 tests, skipped unchanged.
- **`JournalTabTest`'s photo-pull test** did not fail in any run this session.

## Needs a decision

1. **An entry that keeps two or more tracks.**
   - The dispatch says "Entries cards with a kept track", and the data allows several.
   - **Built for now:**
     - no thumbnail when two or more tracks are kept (`entryThumbnailTrack`, `singleOrNull`);
     - the stats row sums their distances and durations into one track figure.
   - I read "a stats row by type" as one figure per type, as the three count types are.
   - **Thumbnail options:**
     - (a) keep none;
     - (b) draw the first kept track (stored order is undefined: `getTrackRefs` has no `ORDER BY`);
     - (c) draw the longest;
     - (d) draw them all in one box on a shared projection, which is a small change to
       `projectTrackToBox`'s input.
   - **Stat options:** keep the sum, or show one figure per track.
   - Nothing tests the multi-track case, so whichever is chosen lands with its own test.

## Decisions I made that the dispatch did not

1. **Short windows keep two columns.**
   - The dispatch scopes C4 to "compact portrait" and puts short windows (J5) out of scope.
   - So `JournalTab` passes 1, or 2 when `isShortWindow()`. Landscape stays as it was until J5, which
     plans sideways two-column cards (L4).
   - The alternative was 1 everywhere, which would change landscape now. Nothing tests the landscape
     column count.
2. **`EntriesDrafts.kt` was edited** (it is not in the dispatch's file list).
   - `DraftsListScreen` hosts the same `CartographyEntryListScreen`. The cards need the user's distance
     unit, so it gained a `distanceUnit` parameter.
   - It also gained `galleryPhotos` and `tracks`, so draft cards show hero and thumbnail the same way,
     and a draft with a photo does not collapse.
   - The alternative was a default unit, which would print a false unit on draft cards.
3. **Month headers by consecutive run.** The timeline is date-ordered, so each month is one header.
   The drafts list is ordered by last update, so a month can recur there with a second header.
4. **A kept find with no identification gets no chip** and still counts in "N finds". The entry
   report shows no identification for it either (`CartographyEntryReportScreen.kt:500`). Species are
   deduplicated, which matches the plan's mockup of 3 finds shown as 2 chips.
5. **Title and preview.** The title is the first non-blank line. The following lines form a two-line
   preview under it, as in the mockup.
6. **Tags stay on the full card**, which showed them before; the plan's J5 list does not mention them.
   They are left off the collapsed row to keep it one line.
7. **"No photo" in the collapse rule means no hero resolves.** An entry whose photos were all deleted
   collapses, since it has nothing more to show.
8. **The thumbnail's `remember` key adds the point count** to the dispatch's track id and box size. A
   track still recording gains points under one id, and would otherwise draw a stale path.
9. **The projection keeps shape:** one scale, centred, with a cosine-of-latitude correction on
   longitude (at 45° N, longitude would otherwise be stretched about 1.4×). A track crossing the
   antimeridian is not handled, and says so in the doc comment.
10. **Sizes:** hero 140 dp tall; card thumbnail 56 dp; row thumbnail 40 dp; stroke 2 dp.
11. **C6's function** lives top-level in `MainActivity.kt` (in scope), not in `domain/`: it is wiring,
    and a test can call it without an Activity. Its log tag is `PhotoReferenceCount`.
12. **`LogPanel` (the wide tree) changes and gaps.**
    - It shares `CartographyScreen`, so it gets the new cards and heroes.
    - It passes no `tracks` and no `draftFindIds`. So its cards draw no track thumbnail, and its
      album's find badge still counts draft finds, until J6.
    - This follows J1 and J2's precedent. `LogPanel.kt` is not edited.
13. **The dispatch and the plan:** no disagreement found. The dispatch adds C3 to C6 (owner rulings),
    which the plan lacks.

## Device-only items

- **The cards at 360 dp with real data:**
  - the day numeral beside a long title;
  - chips and stats wrapping;
  - a 140 dp hero decoded at `inSampleSize = 4` (`DecodedPhoto`), which may look soft full-width on a
    high-density screen. The hero is not measured here;
  - scrolling a long timeline, where each hero decodes on its own (`DecodedPhoto` has no shared cache,
    J0 B2).
- **The sticky header over scrolling cards:** its surface background covering cards cleanly, in light,
  dark and Understory themes.
- **Thumbnails.** Legible at 56 dp and 40 dp on real tracks. The Tracks accent (`tertiary`) against
  the card's surface in all themes.
- **Robolectric's text measurement** in this project is known to be implausible (J1 and J2 reports).
  The collapsed row's height check (64 dp or less) holds under Robolectric; check it on the device at
  larger font scales.

## Flags outside scope

- **What a failed count's 0 hides (C6).** It still hides the delete dialog's "appears in N journal
  entries" warning, so a photo kept by entries can be deleted without that warning when the count
  fails. It is now logged, not fixed. The owner may want a failed count to show as "unknown" instead;
  the pattern did not require that, so I did not stop.
- **Two sibling fallbacks stay silent.** `MainActivity.kt:75` (`getOfflineRegionReferenceCount`) and
  `:179` (`getWaypointReferenceCount`) do the same unlogged `getOrDefault(0)`. The dispatch named only
  `:104`.
- **The find badge fails open.** If the draft finds list failed to load, `draftFindIds` is empty and
  draft-only photos would show the find badge again.
- **The timeline's scroll position** still resets when an entry is opened and closed (J2 flagged
  this; J10's scroll positions are not in this dispatch).
