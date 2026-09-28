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
