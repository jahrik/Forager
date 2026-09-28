# 2026-09-28: J8, Journal entries on the main map (stopped at verification)

**Status: stopped at verification, before any code or test.** The version check and the pulse's claims hold.
But the build needs files the scope does not list, and it raises four questions the rulings do not answer.
Two of those questions involve copy or behaviour the dispatch names as stops. Each is below with the options I
can see. Nothing under `app/` has changed.

Dispatch: `prompts/preserved/2026-09-28-52.md`, intent `2026-09-28-52` (written by the planner). Authority: the
plan's "J8. Journal entries on the main map" and "J8 rulings (owner, 2026-09-28)". Written by the J8 coder in
worktree `forager-wt/j8`, local branch `j8`, cut from `origin/journal-redesign` at `864b1af`. Paths are under
`app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Line numbers are at `864b1af`.

## Base

- After `git fetch`, `origin/journal-redesign` was `864b1af`, the commit carrying the dispatch. **Confirmed.**
- The kit is absent at this base: there is no `.claude/` directory and no checkers. As the dispatch says, I made
  no record entry.

## Verification before building

### Schema version and collisions

- `ForagerDatabase.version = 15` on `journal-redesign` (`data/local/ForagerDatabase.kt:159`) and on
  `origin/pre-main` at `352b708` (same line). **Confirmed.**
- The latest migration is `MIGRATION_14_15` (`data/local/Migrations.kt:908`).
- **I scanned all 108 remote branches after `git fetch --all`.** None declares a version above 15. For each branch
  I read `ForagerDatabase.kt` at the current package path, and at whatever path the file has on the 45 older-layout
  branches. On those 45 the highest version is 15. No branch has a `Migration(15` or a `MIGRATION_15_` anywhere
  (`git grep` per branch). **No collision.** The dispatch asks for this check again at the final push; there
  was none.

### The pulse's claims (`docs/audits/2026-09-28-j8-premise-pulse.md`)

| Claim | At `864b1af` |
|---|---|
| `cartography_entries` columns `id, date, text, tags, isDraft, updatedAtEpochMillis`, indexes on `date` and `isDraft` (`CartographyEntryEntity.kt:19-32`) | **Confirmed** (`data/local/CartographyEntryEntity.kt:20-32`); `15.json` matches |
| New column goes in the entity, `CartographyEntry.kt`, and the mappers (`RoomCartographyEntryRepository.kt:73,88`) | **Confirmed**: `toDomain` at `:68`, `toEntity` at `:83` |
| Legacy fixtures declare `CartographyEntryEntity::class` directly: `TrackOriginWaypointMigrationTest.kt:149`, `WaypointDesignationMigrationTest.kt:101`, `TrackPointSpeedMigrationTest.kt:109` | **Confirmed** (`git grep`) |
| 12 to 13 and 14 to 15 rebuild instead of ADD COLUMN, for that reason (`Migrations.kt:899-906`) | **Confirmed** |
| `GetMapRecordsUseCase` returns non-draft located finds, located photos, ended tracks and every region, and no waypoints (`:94-108`) | **Confirmed** (`domain/GetMapRecordsUseCase.kt:94-109`) |
| The Maps tab draws live waypoints (`MainActivity.kt:553`) | **Confirmed** (`trackUiState.visibleWaypoints`) |
| Ten `PaletteRole`s, none spare (`MapLayers.kt:55-66`) | **Confirmed** |
| Four z-groups and four tap groups; `registryProblems` enforces them; nothing highlights today | **Confirmed** (`ui/map/layers/MapLayers.kt:27, 43-48, 299-324`) |
| The report's menu is a `DropdownMenu` with Edit and Delete (`CartographyEntryReportScreen.kt:374-389`) | **Confirmed**: the menu opens at `:378`, with its items "Edit entry" at `:380` and "Delete entry" at `:385` |
| The Layers sheet's Overlays list has seven items (`MapLayersSheet.kt:129-137`) | **Confirmed** (`MAPS_TAB_OVERLAYS`) |
| The taxon chip at TopCenter (`AvailabilityCompactMapUi.kt:1080-1100`) | **Confirmed** (`:1079-1098`), and under the search bar on the punch-hole side in landscape |
| `MapSlot` at 9 parameters; new fields go on `MapRenderMode`/`MapOverlayContent` | **Confirmed** (`ui/map/MapSlot.kt:398-444`). **No tenth parameter is needed**: the highlight data goes on `MapOverlayContent`, and the switch state already travels on `MapRenderMode.layers` |
| Layer choices persist by layer id | **Confirmed**: `DataStoreMapPreferencesRepository.setLayerVisible` stores any id (`data/repository/DataStoreMapPreferencesRepository.kt:124-126`). A registry layer marked `userToggleable` therefore persists like the other switches with no new key |

Two more facts the build depends on, which the pulse did not list:

- The Maps-tab host already receives the committed entries. `AvailabilityScreen` takes `cartographyUiState`
  (`ui/availability/AvailabilityScreen.kt:540`), and `MainActivity` passes `cartographyUiState.hidingPendingDelete()`
  (`MainActivity.kt:488`). So an entry in its Undo window is already absent, and the chip and highlight can read
  the shown entries from there. Drafts are in a separate list (`draftEntries`), so "saved only" holds by
  construction.
- **The only writer of a Cartography entry is `CartographyViewModel`.** It is constructed in `MainActivity.kt:136-146`
  from `AppContainer` use cases. That is the scope finding below.

None of the three stop conditions in "Verification before building" holds. The stop is for the reasons below.

## Needs a decision

### S1. The toggle can only be written through files the scope does not list

"Show on map", "Hide from map", the chip's "Hide" and "Hide all" all write `shownOnMap`. Today an entry is
written only through `CartographyViewModel` (`ui/log/CartographyViewModel.kt`), which is built from `AppContainer`
use cases in `MainActivity.kt:136-146`, and whose callbacks reach `AvailabilityScreen` through
`MainActivity.kt:488-519`. The report screen's only caller is `ui/log/CartographyScreen.kt:355`. So the build
needs these files:

- `ui/log/CartographyViewModel.kt`: one handler. It writes the field for one entry and updates `entries` and the
  open entry in memory, so the chip and the report's menu item change at once.
- `AppContainer.kt`: wiring that write into the ViewModel, either as a use case or as the repository method.
- `MainActivity.kt`: passing the handler to `AvailabilityScreen`, beside the other Cartography callbacks.
- `ui/log/CartographyScreen.kt`: threading the report menu's callback to `CartographyEntryReportScreen`.
- `ui/availability/AvailabilityCompactScaffold.kt`: threading the chip's data and callbacks from
  `AvailabilityScreen` to the compact Maps tab. M1 and L0b both needed this file widened. I cannot tell whether
  "the Maps hosts" includes it.
- `ui/log/JournalTab.kt` and `ui/log/LogPanel.kt`: I read "the Journal routes for 'Open entry'" as including
  these two. Please confirm.

Options:
- (a) Widen the scope to these files, for threading plus the one ViewModel handler and its wiring, with no other
  behaviour change in them.
- (b) Something else.

Lean: (a).

### Q1. "Open entry" when the Journal is already showing something

M1's find route (`AvailabilityScreen.kt:1184-1212`, `ui/log/JournalTab.kt:339-362, 707-713`) shows the find in an
overlay (`FindOverView`) over whatever the Journal shows. The top tab and the Records chip do not change, and
Back returns to the view underneath. For finds, a find already open is left first (F3). An entry differs in two
ways. First, its report lives under the Entries top tab (`CartographyScreen`), not under Records. Second, the
Journal can already have a day entry open, in its editor with unsaved changes. For that case the
Leaving-the-Journal ruling 2 says the entry "comes back in its editor", and Back from the editor asks Save or
Discard. `CartographyViewModel.onOpenEntry` (`:168`) replaces the open entry and clears `hasUnsavedChanges`, so
calling it with an unsaved editor open would silently drop that edit.

Options:
- (a) **An overlay, like `FindOverView`.** The entry's report shows over whatever the Journal shows, with its own
  entry state rather than the ViewModel's open entry. Back returns to what was under it, the open editor
  included. Still open: what the overlay's own menu does. "Edit entry" would put a second entry into the one open
  slot. It could first apply the rule in (b), or be left out of the overlay.
- (b) **Switch to the Entries top tab and open it there,** with the Records chip untouched. An entry already open
  and clean is closed first. One open in its editor with unsaved changes gets the existing Save or Discard prompt
  first, then the new entry opens. This changes the user's top tab.
- (c) Something else.

On the wide layout I would follow the owner's Q3 answer for finds ("Open drawer to the find") and open the drawer's
`LogPanel` to the entry, in whichever form (a) or (b) takes. No lean between (a) and (b). (a) is closer to M1's
route, and (b) is closer to how the Journal opens entries today.

### Q2. The bubble's entry lines and actions: two copy questions

The dispatch: "list each one. Where there are more than three, show a count, and 'Open entry' opens a list. Any
copy beyond what is named here is a stop."

- **The count's text is not named.** Two strings already exist:
  - "Kept in N journal entries", M1's photo line (`ui/map/MapBubbles.kt`, `photoAttachmentLine`);
  - "N journal entries", `journalEntryCountLabel` (`ui/log/RecordDetailsSheet.kt:309-313`).
  Or other text.
- **Two or three entries.** Is there one "Open entry" per listed entry, or one "Open entry" that opens the same
  list as for four or more? The dispatch says a list opens only past three, which suggests the former. Three
  buttons labelled "Open entry" would read the same to TalkBack.
- **The list's form.** I would make it a menu from the button, one row per entry, named as in Q3, where tapping a
  row opens that entry. No title, so no new copy. Please confirm, or name a title.

### Q3. Which form of "the entry's date as the Journal shows it"

The Journal shows an entry's date in two forms:
- the report header, `entry.date.toString()`, for example "2026-09-12" (`ui/log/CartographyEntryReportScreen.kt:372`);
- the card, a day numeral and a weekday ("12", "SAT") under a month header ("SEPTEMBER 2026")
  (`ui/log/CartographyEntryCard.kt:191-234`).

The dispatch does not say how the chip's list names an entry either. Lean: the report header's form in both
places, since it is the only one that is a whole date on its own.

### Q4. Two readings I would state rather than choose silently (lean given; confirm or overrule)

- **A record whose own overlay is switched off.** Suppose Tracks is off in the Layers sheet and "Journal entries"
  is on. Either a shown entry's track draws its halo alone, or it draws nothing. Lean: nothing. The dispatch says
  "a kept record that is not drawn today ... is not highlighted", and a halo with no line under it highlights
  nothing. This needs each halo layer to follow both the "Journal entries" switch and its record's own switch.
  One registry field would carry that, checked by `registryProblems`.
- **Which photos are the entry's "located photos".** Lean: the attached photos (`CartographyEntry.photos`) that have
  a location and are drawn on the Maps tab. These are what the entry map draws
  (`domain/GetCartographyEntryMapDataUseCase.kt:88-93`), and they do not include the photos of kept finds.

## What I would build once these are answered (mechanism, not built)

These are stated so the next round is quick. They are mine and open to change:
- **Room.**
  - `MIGRATION_15_16` rebuilds `cartography_entries` with `shownOnMap INTEGER NOT NULL`. The rebuild uses an
    explicit source column list and writes `0` for every existing row, and recreates both indexes.
  - There is no SQL `DEFAULT` clause, as in the other rebuilds. The dispatch's "default false" I read as the domain
    default plus the migrated value. The alternative is `@ColumnInfo(defaultValue = "0")` with `DEFAULT 0` in the
    rebuilt table.
  - One DAO `UPDATE` sets the field for one id, so a toggle never rewrites the entry's other fields.
  - Every existing migration test's chain gains `MIGRATION_15_16`, and `SchemaMigrationTest` gains 15 to 16 and
    the chain to 16. `16.json` is exported.
- **The use case** (headless). It takes the committed entries with `shownOnMap`, the records the Maps tab draws
  (`MapRecords`, pending deletes already out) and the live waypoints. It returns the kept records among them, each
  with the ids of the entries keeping it. It never reads an entry's snapshot coordinates, so a record deleted or
  not drawn is simply absent. Withheld decisions and drafts cannot reach it.
- **Layers.** Five halo layers, each directly below its record's layer, with `TapGroup.NONE`:
  - kept tracks: a wider line under the track casing;
  - offline regions: a wider line under the dashed outline;
  - waypoints, finds and photos: a glyph-shaped halo bitmap drawn under each marker.
  Each layer has its own GeoJSON source rather than one filtered source, so a symbol layer can never be fed a
  line. That makes planner prediction 3's "one new source" wrong by design. One halo layer is the "Journal
  entries" switch's state owner, listed in `MAPS_TAB_OVERLAYS`, and the other four follow it.
- **Palette.** One new role, `JOURNAL_ENTRY`, day and night, with values measured in `MapPaletteTest`'s ratchet
  style.

## What landed

This report only. No production or test file.

## Tests

None written and none run, so there is no tests-first run and no revert check.

## Suites

Not run. Nothing is built. The machine had 11 GB free on `/` and 2 GB of memory available (`free -g`: 11 total,
8 used).

## Predictions

- 1 (version 15 on both branches, no collision): **held.**
- 2 (no tenth `MapSlot` parameter): **held** on the design; nothing is built.
- 3 (one new source, four or five layers): five layers by the design above, but five sources, not one.
- 4 (suite +40 to +90): not measured.

## Device-only

Nothing is built. The dispatch's list stands for the build:
- the highlight's look over each basemap and at night;
- the chip against real insets;
- the migration on the S22's real data.

## Decisions I made

- **I followed the dispatch over my agent definition on the record,** as the M1 and L0b coders did. My definition
  asks for a sweep, an intent, a terminal and the kit's checkers. The kit is absent at this base, and the
  dispatch says the planner writes the record. I touched nothing in `RECORD.md` or `prompts/`.
- **I did not validate the dispatch's sections against a kit config,** because none exists at the base.
- **I stopped the whole stage** rather than building J8-1 alone. The column must land with its reader in the same
  change (CLAUDE.md), and its readers are the chip, the report menu and the use case, all of which S1 and Q1 to Q4
  shape.
- **I wrote this stop as the named completion report and pushed it,** as the M1 and L0b coders did, so the
  findings are not only in a hand-back.
- **The collision scan's method.** I read each branch's `ForagerDatabase.kt` at whatever path it has, and grepped
  for a 15-to-anything migration.
- **Mechanisms and leans.** Everything under "What I would build" and every lean above is mine, stated as
  information and not built.

## Flags outside scope

- The pulse's premise list did not name `CartographyViewModel.kt`, `AppContainer.kt`, `MainActivity.kt` or
  `CartographyScreen.kt`, and the dispatch's scope follows it. S1 is the result.
- A worktree `forager-wt/j8` and a local branch `j8` now exist, created as the dispatch says.

## D58

I ran `git grep -i` for the three phrases named in `prompts/preserved/2026-09-28-03.md` over this report and this
commit's message. The result is in the commit.

## Resumed (continuations `2026-09-28-54` and `2026-09-28-62`): stopped before the build, on the chip's fill

**Status: stopped after the verification and the tests-first re-run, before building anything.** I am the third J8 coder,
relaunched by continuation `2026-09-28-62`. The first coder wrote the stop section above and then died on a network
outage at 18:09Z ("API Error: Can't reach the API server (EAI_AGAIN)"). Continuation `-54` launched a replacement. That second
coder was stopped at about 19:10Z when the owner said "Stop everything". It left commit `ebefbc9` (tests first, stubs in
main) on `origin/j8-wip` and uncommitted work, and it sent no hand-back. `-62` launched me to replace it.

I stop on one question where `-62` and CLAUDE.md say different things: the chip's fill (Q-A). I also ask the planner to
confirm one reading of the tests-first abort (Q-B). The only thing pushed to `journal-redesign` is this report. The
stopped coder's work is preserved, unchanged, on `origin/j8-wip`.

Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

### First act

I committed the stopped coder's working tree untouched as `9cde9b3` ("WIP: stopped J8 coder's uncommitted work,
unverified (preserved as found)"). It holds 18 files, with 454 insertions and 41 deletions and no untracked files. I
pushed it to `origin/j8-wip` (`ebefbc9..9cde9b3`) before reading, editing, stashing or resetting anything.

### Base

- After `git fetch --all`, `origin/journal-redesign` was `48633ee`, the commit carrying `-62`'s store copy.
  **Confirmed.**
- Its `app/` is byte-identical to `864b1af`, where the stop section read its citations, and to `367c32f`, the parent of
  `ebefbc9`. `git diff --stat` over `app/` is empty for both. The base has moved by planner commits only.
- The kit is still absent at this base (`e136330`), so, as above, I made no record entry.

### Verification (re-run)

- `ForagerDatabase.version = 15` at `data/local/ForagerDatabase.kt:159`, on `origin/journal-redesign` (`48633ee`) and
  on `origin/pre-main` (`352b708`). **Confirmed.**
- **Collision scan.** I scanned all 110 remote-tracking branches on both remotes (`origin` and `fresh`). One branch is
  above 15 or declares a `MIGRATION_15_*`: `origin/j8-wip` at `9cde9b3`, which is this stage's own work (the stubs in
  `ebefbc9` and the stopped coder's migration). No other branch is. **No collision.** There is no final push to
  re-check at, since I stopped.
- **Pulse claims.** `app/` has not changed since `864b1af`, so the stop section's table still holds at this base. I
  re-read the claims at `48633ee` and all are **confirmed**:
  - the entity's columns (`data/local/CartographyEntryEntity.kt:26-31`);
  - the mappers (`data/repository/RoomCartographyEntryRepository.kt:68`, `:83`);
  - the three legacy fixtures (`TrackOriginWaypointMigrationTest.kt:149`, `TrackPointSpeedMigrationTest.kt:109`,
    `WaypointDesignationMigrationTest.kt:101`);
  - the rebuild precedents (`data/local/Migrations.kt:794`, `:908`);
  - `MapSlot`'s nine parameters (`ui/map/MapSlot.kt:398-444`);
  - the report menu (`ui/log/CartographyEntryReportScreen.kt:378-389`);
  - the seven overlays (`ui/map/MapLayersSheet.kt:129-137`).

None of the verification stops holds.

### Tests first, re-established

**How I established "base".** `-62` says base means the code before the stubs. I built that tree in the J8 worktree:
1. I detached at `ebefbc9`.
2. I deleted the four files the stubs add: `GetJournalEntryHighlightsUseCase.kt`, `SetCartographyEntryShownOnMapUseCase.kt`,
   `JournalEntriesOnMap.kt` and `16.json`.
3. I checked out `app/src/main` and `app/schemas` from `367c32f`.

After that, `git diff --stat 367c32f` over those two paths was empty, and `app/src/test` was `ebefbc9`'s. Afterwards I
restored `ebefbc9` with `git checkout ebefbc9 -- app`, and `git status` was clean. Every run below cleared the results
directory first, and my runner refuses to read results when the build log has compile errors
(`/tmp/claude-1000/j8r/run.sh`; logs copied to `app/build/j8r/logs/` in the worktree).

**Run 1: `ebefbc9`'s tests against base main.** `:app:compileDebugUnitTestKotlin` fails with 344 `e:` lines. All of
them are in 24 test files and none is in main. Each error is a J8 piece that does not exist at base; the rest are
type-inference errors that follow from those.

| Test file | What base lacks (the distinct unresolved references) |
|---|---|
| `CartographyEntryShownOnMapMigrationTest` (new) | `MIGRATION_15_16`, `setShownOnMap`, `shownOnMap` |
| `SchemaMigrationTest` and 11 migration tests' chains | `MIGRATION_15_16` |
| `GetJournalEntryHighlightsUseCaseTest` (new) | `GetJournalEntryHighlightsUseCase`, `JournalEntryOnMap`, `JournalEntryHighlights`, `HighlightedRecord`, `HighlightedRecordKind`, `shownOnMap` |
| `CartographyViewModelTest` | `SetCartographyEntryShownOnMapUseCase`, `onSetShownOnMap`, `shownOnMap` |
| `JournalEntriesOnMapTest` (new) | `journalEntriesChipLabel`, `journalEntryDateLabel`, `keptInEntriesLines`, `KeptInEntriesLines`, `EntryLine`, `keptIn`, `journalEntriesKeeping`, `JOURNAL_ENTRIES_SWITCH_LAYER_ID`, the five `JOURNAL_ENTRY_*` layer ids |
| `JournalHaloGlyphTest` (new) | `drawGlyphHalo`, `JOURNAL_HALO_WIDTH_DP` |
| `MapLayerRegistryTest` | the five `JOURNAL_ENTRY_*` ids, `JOURNAL_ENTRIES_SWITCH_LAYER_ID`, `PaletteRole.JOURNAL_ENTRY`, `drawnWith` |
| `MapPaletteTest` | `journalEntry` (`:234`, in the `fills` map that every (a), (c), (d) and (e) check reads) |
| five `CartographyViewModel` construction sites and two fake repositories (adaptations) | `SetCartographyEntryShownOnMapUseCase`, the `setShownOnMap` parameter and override |

So no tests-first test can pass at base. A compile failure is not a per-test reason, though, so I added runs 2 and 3.

**Run 2: the one tests-first file that compiles against base.** `AvailabilityScreenMapLayersTest.kt` changed only
strings. I set the 24 non-compiling files to their `367c32f` versions, or removed the four new ones, so that this file
was the only difference from base. Then I ran it: 3 classes, 19 tests, **2 failures, both of them the changed tests**.
Each failed with `performScrollTo() failed ... could not find any node that satisfies: (Text ... contains 'Journal
entries')`. The other 17 passed.

**Run 3: at `ebefbc9` (the stubs).** 22 classes, 114 tests, 48 failures, 0 compile-error lines. I checked the log
before reading the results.
- **All 24 new tests fail,** each at an assertion or exception that names the missing behaviour:
  - the migration's two: Room's "Migration didn't properly handle: cartography_entries", and "J8 tests-first stub:
    setShownOnMap is not built";
  - `SchemaMigrationTest`'s 15 to 16;
  - the use case's five: `but was:<[]>` on the shown entries or the geometry;
  - the ViewModel's three: `expected:<true> but was:<false>`;
  - `JournalEntriesOnMapTest`'s nine: empty chip text and date, `null` entry lines, an empty `keptIn`, "no registry
    layer journal-entry-regions-layer", Offline maps still last in the sheet, and "stored visibility for
    journal-entry-tracks-layer: no such layer";
  - the halo glyph's two: "halo has a real size (2x2)" and "the halo draws its colour there expected:<-16755337> but
    was:<0>";
  - the registry's two new tests: "only the halos decorate another layer ... but was:<{}>", and `registryProblems`
    returning `[]` for a decoration of a missing layer.
- **17 existing tests had their expectations changed. 16 of them fail and one passes.**
  - **They fail:**
    - `SchemaMigrationTest`'s 4 to 16;
    - the two Layers-sheet tests;
    - six `MapLayerRegistryTest` tests: the id set, the order, the kinds and sources, the tap groups, the state owners
      and the flags;
    - seven `MapPaletteTest` checks: the hex pin (`DAY.journalEntry expected:<#[005577]> but
      was:<#[FFFFFF]>`), (a) day and night, (c) day and night, (e) ("achromatic in a mode") and (d) night.
  - **(d) night fails, but not on the new pin.** The black placeholder is 0.2435 from the night offline-region fill,
    which is under that role's own pin of 0.360. It is not `journalEntry`'s own (d) pin that fires.
  - **(d) day passes** (`MapPaletteTest.kt:285`). The white placeholder is more than 0.161 from every day role, so the
    new row at `:103` is met. The stopped coder's commit message says this, and it planned a revert check for that row.
- `MapLayerRegistryTest`'s "the only change from the order before L0a" was edited only to subtract the halos, so that
  it tolerates them. It passes at the stubs. It is not a tests-first test.
- **Adaptations, not tests first.** Eleven migration tests' chains gained `MIGRATION_15_16`. Eight fail at the stubs,
  on Room's validation. Three pass: `TrackOriginWaypointMigrationTest`, `TrackPointSpeedMigrationTest` and
  `WaypointDesignationMigrationTest`. Their legacy fixtures declare `CartographyEntryEntity` directly, so they already
  have the column. That is the hazard the pulse named, and it is why the migration rebuilds the table. The five
  constructions and two fakes only compile against the new signatures.

**The stubs, reviewed.** Each stub in `ebefbc9`'s main declares a name with no behaviour:
- `MIGRATION_15_16` migrates nothing;
- `setShownOnMap` returns a failure;
- the use case returns `NONE`;
- `onSetShownOnMap` does nothing;
- `drawGlyphHalo` returns an empty 2x2 image;
- the chip text, the date label and the entry lines are empty or `null`;
- the palette's `journalEntry` is each mode's casing colour;
- the halo and source ids and `drawnWith` exist, with no registry entry.

Three things go beyond a name:
- the entity field and its two mapper lines;
- the version bump to 16, with its no-op migration;
- `openEntryAccessibilityLabel`, which is implemented.

None of these makes a tests-first test pass. The mappers are reached only through the migration tests, which fail on the
no-op migration.

**My reading of the abort.** At base as `-62` defines it, no tests-first test passes. 24 files fail to compile, each on
a missing J8 piece, and the one file that compiles fails for its stated reason. So I read `-52`'s abort ("a tests-first
test passing at base") as not met. At the stubs, the finer reason shows for every tests-first test except the (d) day
row, which the placeholder happens to satisfy. That is Q-B.

### The stopped coder's uncommitted work (`9cde9b3`)

**What it is.**
- J8-1:
  - the DAO's one-column `UPDATE`, which returns the rows changed;
  - `MIGRATION_15_16` as a rebuild, with an explicit column list, `0` for every row and both indexes recreated;
  - the repository's `setShownOnMap`, a failure when no row changed.
- J8-2:
  - the use case: saved and shown entries only, live geometry, and entry lines only for records that are drawn;
  - five halo layers in the registry, each directly below its record, with `TapGroup.NONE`, following the switch's
    layer;
  - `drawnWith` in `layerPaintFor` and in `registryProblems`;
  - the halo sources fed in `SightingsMap`, with two line-halo specs and three halo marker images;
  - `MapOverlayContent.journalHighlights`, a new field on the bundle and not a `MapSlot` parameter;
  - the palette at day `#005577` and night `#00DDFF`.
- J8-3: the ViewModel handler, which refuses a draft; "Journal entries" last in the Maps tab's overlays and absent from
  the entry map's.
- J8-4: the chip text, the report-header date form, the date lines or the "Kept in N journal entries" count line, and
  `keptIn` on the five bubble kinds.
- Tests: two new tests in `JournalEntriesOnMapTest` (what each halo source receives; the line-halo widths), one in
  `JournalHaloGlyphTest` (the halo icons), and `MarkerGlyphsTest` narrowed to non-halo icons.

**What it lacks.**
- The hosts that compute the highlights and pass them, `journalEntriesKeeping` and `onOpenEntry`.
- The chip.
- The report menu's item and its threading.
- The bubble's entry-line UI and its list.
- The Open-entry route with the save prompt, and its tests first (`-53`, Q1).
- The UI tests, the revert checks and the full suite.

**What it does.** I did not build on it. I ran 29 classes at `9cde9b3`: the tests-first classes, plus every class that
reads the registry, the palette, the glyphs or the overlays list. It compiles, with 0 compile-error lines. Of 181 tests,
**2 fail**:
1. `JournalHaloGlyphTest`, "beyond the marker's casing, and only there, the halo shows a ring of its own colour", fails
   with `WAYPOINT: the halo draws its colour there expected:<-16755337> but was:<0>`. That is the same message as at the
   stubs. So `drawGlyphHalo` as written leaves the probe point transparent. The probe is 0, -28 dp from the tip, moved
   outward by the casing plus half the halo width. I did not find out whether the drawing or the probe is wrong.
2. `MapLayerStateTest`, "a hidden layer's visible flag is false and no other layer changes", is an existing test. With
   Finds off, the finds halo is now hidden too (`journal-entry-finds-layer ... visible=false`). That is `-53`'s Q4
   reading working: "A record whose own overlay is switched off gets no highlight". The test's "no other layer changes"
   does not allow for it. It would need restating so that the record's own halo goes with it and nothing else changes,
   recorded as an intended change to an existing test and not as a weakened one.

The other 179 pass, including all the palette pins.

**What I kept, and why.** I kept all of it, unchanged, on `origin/j8-wip` as the base for the build. It compiles. It
follows the mechanism in the stop section, which `-53` accepted, and it turns every tests-first test green except the
halo ring. Nothing in it needs undoing. Four points need attention when the build resumes:
- the halo-ring failure (above);
- the `MapLayerStateTest` restatement (above);
- the three test additions were written with the implementation, so they are not tests first, and each needs a revert
  check;
- a failed toggle shows the existing text "Couldn't save your changes." (`ui/log/CartographyViewModel.kt:636`, the same
  string as at `:388` and `:664`). It is existing copy used in a new place, and whether that counts as new copy is for
  the planner.

## Needs a decision

### Q-A. The chip's fill: `-62` and CLAUDE.md disagree

- `-62`: "Do not make J8's chip 80%, and do not change any fill for it. Build the chip as `-52` and `-53` say. `-56`
  will bring it in line afterwards."
- CLAUDE.md at the base (`:245`, the owner's rule): "New map chrome starts at 80%, and an opaque surface over a map is a
  bug unless the owner has stated an exception for that case."
- `-52` and `-53` put the chip "in the row with the taxon chip" and bound it to its content. Neither names a fill.
- The taxon chip beside it is already at 80%. `TaxonMapFilterChip` fills with `MapIconStackButtonColorDark` and
  `MapIconStackButtonColorLight` (`ui/availability/AvailabilityMapOverlaysUi.kt:291-296`). Those are `Bark` and `Cream`
  at `MAP_CHROME_OVER_MAP_ALPHA` (`ui/map/MapChrome.kt:233-239`). The inventory pulse lists it that way (its row "Taxon
  chip", "0.8 constant").
- `-58` lists J8's new chip and its list "if they are not already at 80%", so it allows either.
- The owner's "After J8 (Recommended)" (plan, "Map chrome at 80%") sets when the 80% work happens. I found no owner
  ruling that J8's new chrome should be opaque in the meantime.

Building the chip like its neighbour makes it 80%, against `-62`'s words. Building it at anything other than 80% puts an
opaque surface over the map, against CLAUDE.md, and needs a fill that nobody has named. Options:
- (a) The taxon chip's own fill and content colours, at 80%, so the row reads as one. `-56` then finds it already at
  80%.
- (b) The same colours solid (`Bark` or `Cream` at full alpha), as `-62` says literally, until `-56` lands. The row then
  has one 80% chip and one solid chip.
- (c) A Material3 chip's default colours, for example `AssistChip`'s transparent container with an outline. How that
  reads over a map is unknown.
- (d) Something else.

In every option, the chip's list (a menu) keeps Material3's default menu fill, which the owner's edge-case ruling 1
leaves to `-56`. Lean: (a). It meets the purpose `-62` states for its sentence (`-56` brings the chip in line) and
breaks no other written rule.

### Q-B. Confirm the tests-first reading for the (d) day row

At base as `-62` defines it, the row fails, because it does not compile (`MapPaletteTest.kt:234`, `journalEntry`). At
the stubs it passes, because the white placeholder is far from every day role. Options:
- (a) Accept it, with a revert check on that row as the evidence that it can fail: a day `journalEntry` inside 0.161 of
  the sighting dot, the role its pin names.
- (b) Treat it as `-52`'s abort.
- (c) Something else.

Lean: (a).

## What landed

- `9cde9b3` on `origin/j8-wip`: the stopped coder's work, preserved as found. `ebefbc9` is under it, unchanged.
- This report, on `journal-redesign`.
- Nothing under `app/` changed on `journal-redesign`.

## Tests and suites

Four runs, each described above: run 1 (compile at base), run 2 (3 classes, 19 tests, 2 failures), run 3 (22 classes,
114 tests, 48 failures) and the WIP run (29 classes, 181 tests, 2 failures). There was no full suite and no revert
check. Before every Gradle run `/` had 11 GB free and about 3.8 GB of memory was available. A Gradle daemon and a Kotlin
daemon shared across worktrees held about 5 GB.

## Not tested

Nothing was built, so none of the following was exercised: the hosts, the chip, the report menu, the bubble's entry
lines, the Open-entry route, the Layers switch on a real screen, and any of the UI tests `-52` lists. The WIP's
behaviour beyond the 29 classes is unknown.

## Predictions

- 1 (version 15, no collision): **held** at this base.
- 2 (no tenth `MapSlot` parameter): **held** so far. The WIP adds a field to `MapOverlayContent` only.
- 3: five layers and five sources, recorded by `-53` as wrong by design.
- 4 (suite +40 to +90): not measured.

## Device-only

Unchanged from `-52`:
- the highlight's look over each basemap and at night;
- the chip against real insets;
- the migration on the S22's real data.

## Decisions I made

- **The record.** I followed the dispatch over my agent definition, as the stop section did. The kit is absent at the
  base (`e136330`), and `-52` says the planner writes the record.
- **Order.** I committed and pushed the WIP before reading the store copies or validating anything, as `-62` orders.
  That came before my agent definition's own first step, validating the dispatch.
- **How I built base, and run 2.** The tree for run 1 is described above. Run 2 set the other 24 test files to base so
  that one file could run. Both methods are mine.
- **Classification.** I sorted the tests into new, changed-expectation and adaptation. The count that meets or misses
  the abort rests on that split.
- **The abort for (d) day.** I read it as not met, on `-62`'s definition of base, and I ask Q-B rather than build on
  that reading.
- **Stopping the whole stage on Q-A**, rather than building the parts that do not depend on the chip's fill.
- **What to keep.** I kept all of the WIP on `j8-wip`, built nothing on it, and discarded nothing.
- **Where this report goes.** It is a report-only commit on `journal-redesign`, made with a temporary index and no
  checkout, so the suite there is unchanged. `j8-wip` stays at `9cde9b3`.
- **The collision scan's scope.** I scanned every remote-tracking branch on both remotes, and counted `j8-wip` as this
  stage's own work.
- **Leans.** The leans in Q-A and Q-B are mine, given as information.
- **My own runner.** I used my own runner rather than the previous coders' copies. I read their `revert.py` but did not
  use it.

## Flags outside scope

- `-62`'s "Do not make J8's chip 80%" may rest on a picture of the tree in which the taxon chip is not at 80%. The
  inventory pulse, filed before `-62`, lists it at the 0.8 constant.
- `ebefbc9`'s commit message counts "22 classes, 113 tests, 48 failures". My run 3 counts 114 tests over the same 22
  classes. The failures match; the one-test difference is in which classes the two runs selected, and I did not trace
  it.
- The previous coders' runner copies stay in `app/build/j8/` and `/tmp/claude-1000/j8/`. I left them untouched.

## D58

I ran `git grep -i` for the three phrases named in `prompts/preserved/2026-09-28-03.md` over this report and this
commit's message before the push. There were zero hits.

## Verbatim: continuation `2026-09-28-53`

> Planner message 2026-09-28-53, part of dispatch 2026-09-28-52. Quote it verbatim in your report. It answers S1 and Q1 to Q4 of your stop at `c9217b3`. **Build J8.**
>
> **S1: the scope widens, by the planner.**
> - New files in scope:
>   - `CartographyViewModel.kt`, for one handler that writes `shownOnMap` and updates the in-memory entry;
>   - `AppContainer.kt` and `MainActivity.kt`, for wiring that handler;
>   - `CartographyScreen.kt`, for threading the report menu's callback;
>   - `AvailabilityCompactScaffold.kt`, `JournalTab.kt` and `LogPanel.kt`, for threading and the Open-entry route.
> - No other behaviour change in any of these files.
> - The gap was the planner's: the scope followed the pulse's file list.
>
> **Q1: the owner's ruling, verbatim: "Open in Journal, prompt first (Recommended)".**
> - "Open entry" switches to the Journal and opens that entry in its report view.
> - If another entry is open with unsaved changes, the existing "Save your changes?" prompt (`CartographyEntryEditScreen.kt:317-342`) comes first, and the new entry opens only after Save or Discard. An unchanged open entry just closes.
> - On the wide layout, it opens the drawer the same way.
> - Do not change the saved Records chip or the top tab beyond what opening the entry requires.
> - Tests first:
>   - opening while an entry has unsaved edits shows the prompt, and Discard or Save behaves as it does today;
>   - opening while an unchanged entry is open simply opens the new one;
>   - no unsaved edit is ever dropped silently.
>
> **Q2: the owner's ruling, verbatim: "Tap a date line (Recommended)".**
> - In a highlighted record's bubble, each keeping entry is its own line showing its date. Tapping the line opens that entry under Q1's rule.
> - With more than three entries, a single line "Kept in N journal entries" replaces the date lines, reusing the photo bubble's existing wording and its plural handling. Tapping it opens an untitled list of those entries' dates, and each list item opens its entry.
> - There is no separate "Open entry" button. The date lines are the action, each with the accessibility label "Open entry <date>".
>
> **Q3, by the planner:** use the report header's date form (`2026-09-12`, `CartographyEntryReportScreen.kt:372`) in the bubble and in the chip's list.
>
> **Q4, by the planner:** both of your readings are accepted.
> - A record whose own overlay is switched off gets no highlight.
> - "Located photos" means the entry's attached photos, as the entry map draws them.
>
> Your five-layer, five-source mechanism is accepted. Prediction 3 will be recorded as wrong by design.
>
> Everything else stands: tests first, the migration tests, revert checks, the full suite, the collision re-check at the final push, and the report ("Resumed" section).

## Verbatim: continuation `2026-09-28-54`

> **Type:** build (continuation `2026-09-28-54` of dispatch `2026-09-28-52`)
>
> # Why you exist
>
> The J8 coder that was building dispatch `2026-09-28-52` died at 18:09Z on a network outage ("API Error: Can't reach the API server (EAI_AGAIN)"). The planner session that ran it died with it. You replace that coder. Nothing was wrong with its work; it simply stopped.
>
> **What it left, checked by the planner at relaunch:**
> - Worktree `/home/zynergy-labs/Zynergy/forager-wt/j8`, branch `j8`, at `3656e21`, **clean**: no code or test had been written. Its only commit is the stop report `c9217b3` (`docs/audits/2026-09-28-j8-entries-on-map-completion-report.md`).
> - Runner copies it made in `app/build/j8/` (`run.sh`, `revert.py`, `d58.sh`, copied from `forager-wt/leave-fixes/app/build/lf/`). Before you trust `revert.py`, confirm it restores from a saved copy (never git) and refuses results when the build log has compile errors (CLAUDE.md, Testing).
> - Palette search scripts in `/tmp/claude-1000/j8/` (`pal.py`, `pair.py`). Its unverified pick was day `#005577`, night `#00DDFF`. Treat that as a lead, not a result: measure your own values in `MapPaletteTest` as `-52` requires.
> - An extract of its transcript at `/tmp/claude-1000/j8/prior-coder-transcript-extract.md` (its commands, truncated outputs and its few text lines; its reasoning was not saved). It is model output: re-check anything in it against the code before you rely on it.
>
> # What governs
>
> Read these in full, in this order, and quote `-53` and this message verbatim in your report's "Resumed" section:
> 1. `prompts/preserved/2026-09-28-52.md`: the dispatch. Every rule, test, scope line, abort condition and finish line in it binds you.
> 2. `prompts/preserved/2026-09-28-53.md`: the planner's continuation. It widens the scope (S1) and carries the owner's rulings on Open entry (Q1) and the bubble's date lines (Q2), plus the planner's Q3 and Q4. **It governs where it differs from `-52`.**
> 3. The stop report at `c9217b3`. It is your predecessor's verification; your own verification still runs (below).
>
> # What changes for you
>
> - **Re-run the verification.** The base has moved by planner commits only since `c9217b3`, but CLAUDE.md's rule applies: re-check `ForagerDatabase.version` (15) on `journal-redesign` and `origin/pre-main`, and re-check that no remote branch declares 16 or a `MIGRATION_15_*`. Confirm the pulse claims your build rests on at your base, with file:line. A stop condition from `-52` is still a stop.
> - **Then build J8** as `-52` plus `-53` say: tests first, each seen failing at base; J8-1 to J8-4; revert checks; the full suite from a cleared results directory; and the collision re-check at your final push.
> - **Append your report** to the existing `docs/audits/2026-09-28-j8-entries-on-map-completion-report.md`. Do not rewrite the stop section above it. Add a "Resumed" section that names this relaunch (`2026-09-28-54`) and says that the first coder died on a network outage.
> - **Phone:** none. A device coder is on the S22 at the same time.
> - **The record:** not yours. Same as `-52`.
>
> # Merge
>
> Not authorised.

## Verbatim: continuation `2026-09-28-62` (the store copy, which governs)

> **Type:** build (continuation `2026-09-28-62` of dispatch `2026-09-28-52`)
>
> # Why you exist
>
> The J8 coder relaunched by `-54` was stopped at about 19:10Z when the owner said "Stop everything". Nothing was wrong with its work. The owner has since told the planner to relaunch it ("A": relaunch both). You replace it.
>
> **What it left, as the planner read it at relaunch:**
> - Worktree `/home/zynergy-labs/Zynergy/forager-wt/j8`, branch `j8`. Commit `ebefbc9`, "J8 tests first … stubs in main, failing tests", is pushed to `origin/j8-wip` only, not to `journal-redesign`.
> - **Uncommitted work on top of `ebefbc9`:** 18 files changed, 454 insertions and 41 deletions, in `data/local`, `data/repository`, `domain`, `ui/log/CartographyViewModel.kt`, `ui/map` and `ui/theme/MapPalette.kt`, plus three test files. It is unreviewed and may not compile.
> - It sent no hand-back, so its verdicts on its own tests-first runs are unknown.
>
> # First, before anything else
>
> Commit the working tree exactly as it is, with a message saying it is the stopped coder's uncommitted work, unverified. Push it to `j8-wip`. Do not edit, stash, reset or discard any of it first ("Push before you tidy", CLAUDE.md). Then read it and decide what to keep. Say in your report what you kept and why.
>
> # What governs
>
> In full, in this order: `prompts/preserved/2026-09-28-52.md`, then `-53`, then `-54`. `-54`'s instructions apply to you in full: the verification re-run, the build, the report as a "Resumed" section appended to the existing completion report, no phone, and no record. Quote `-53`, `-54` and this message verbatim in the report.
>
> # What you must re-establish, not take on trust
>
> - **Tests first.** Confirm that every tests-first test in `ebefbc9` fails at base for its stated reason, by running it yourself against base `main` code. The stubs make "base" mean the code before the stubs: say how you established it. If a stated reason cannot be shown, that is `-52`'s abort condition.
> - **Verification.** `ForagerDatabase.version` is 15 on `journal-redesign` and `origin/pre-main`, and no remote branch declares 16 or a `MIGRATION_15_*`. Re-check this at your final push as well.
>
> # Not yours
>
> The queued map-chrome dispatch `-56` (amended by `-58`) starts after J8. Do not make J8's chip 80%, and do not change any fill for it. Build the chip as `-52` and `-53` say. `-56` will bring it in line afterwards.
>
> # Merge
>
> Not authorised.

The launch message also carried a planner's note, marked "not part of the store copy". Verbatim:

> Planner's note (not part of the store copy): the only other agent on this machine is the device coder on the phone, which does not build. Check `df` and free memory before each Gradle run. Commit and push at every natural stopping point, so another stop or outage costs nothing. When you finish or stop, hand back a report as the dispatch requires: what landed with hashes, verification, evidence, suite counts, and what was not tested.
