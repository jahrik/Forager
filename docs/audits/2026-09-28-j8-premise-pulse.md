# J8 premise pulse (read-only, at `26709b1`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `26709b1`, `origin/pre-main` `352b708`, through git objects.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with every citation kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`.

## Room

- **Schema version.** `ForagerDatabase.version = 15` on both branches (`data/local/ForagerDatabase.kt:159`). The latest migration is `MIGRATION_14_15` (`data/local/Migrations.kt:908`). None of the 106 remote branches declares a version above 15 or a `MIGRATION_15_*`.
- **`CartographyEntryEntity`.** It has the columns `id, date, text, tags, isDraft, updatedAtEpochMillis`, with indexes on `date` and `isDraft` (`data/local/CartographyEntryEntity.kt:19-32`). There are five ref tables with snapshots and `kept`. The photo table has no `kept` (`CartographyEntryDao.kt:161-162`).
- **Where a new column goes.** In the entity (`:23-32`), `domain/model/CartographyEntry.kt`, and the mappers at `data/repository/RoomCartographyEntryRepository.kt:73,88`.
- **Hazard.** Three legacy test fixtures declare `CartographyEntryEntity::class` directly: `TrackOriginWaypointMigrationTest.kt:149` (v12), `WaypointDesignationMigrationTest.kt:101` (v13) and `TrackPointSpeedMigrationTest.kt:109` (v14). That is why 12 to 13 and 14 to 15 rebuilt their tables instead of using ADD COLUMN (`Migrations.kt:899-906`). A 15 to 16 ADD COLUMN on `cartography_entries` would hit the same problem (inferred).

## Overlap with L0b

- **What the Maps tab draws.** `GetMapRecordsUseCase` returns non-draft located finds, located photos, ended tracks only, and every region (`domain/GetMapRecordsUseCase.kt:94-108`). It returns no waypoints; the Maps tab draws live waypoints (`MainActivity.kt:553`). These go into the same `MapOverlayContent` fields and layers the entry map uses (`ui/availability/AvailabilityCompactMapUi.kt:648-651`, `AvailabilityWideLayoutUi.kt:285-288`, `ui/log/CartographyEntryReportScreen.kt:420-431`).
- **So a record that is both saved and kept by an entry draws once, with no entry marking** (`ui/map/layers/MapLayers.kt:263-290`).
- **How the entry map's geometry differs** (`domain/GetCartographyEntryMapDataUseCase.kt`):
  - tracks are fetched by id, with no ended filter (`:69-72`);
  - waypoints and regions come from the entry's snapshots (`:85, :95-97`);
  - photos are the attached ones (`:88-93`);
  - it handles one entry per call (`:68`).
- **Palette.** There are ten `PaletteRole` entries, with no spare role (`MapLayers.kt:55-66`; `ui/theme/MapPalette.kt:47-117`). The plan's "J6 colour roles" (`ui/log/RecordTypeStyle.kt:40-47`) are Compose roles with no photo; MapLibre cannot read them. That was a premise correction.
- **Registry.** Four z-groups and four tap groups, with `registryProblems` enforcing them (`MapLayers.kt:27, 43-48, 299-324`). There is no highlight mechanism: `focusedFeature` only re-anchors bubbles (`ui/map/SightingsMap.kt:493-497`).

## Toggle surfaces

- **Portrait entry cards have no long-press menu.** They use `TwoStageSwipeRow` with Edit and Delete (`ui/log/CartographyEntryListScreen.kt:161-168`, "Lists swipe, grids long-press"). Long-press exists only on sideways cards in short windows (`:137-146, 209-214`). Both menus are fixed to two items (`ui/log/TileOptionsMenu.kt:61-114`; `ui/log/TwoStageSwipe.kt:168-175, 238-245`). This was a premise correction.
- **The report's own menu** is a `DropdownMenu` with Edit and Delete (`CartographyEntryReportScreen.kt:374-389`). Adding an item there is straightforward.
- **The Layers sheet's Overlays list** has seven items (`ui/map/MapLayersSheet.kt:129-137`). "Journal entries", which layer ruling 3 lists, is not built.
- **Where things sit today:**
  - the legend chip at BottomEnd, above the "i" (`AvailabilityCompactMapUi.kt:1151-1161`), with the cluster clamping above it (`:786-793`);
  - the caption at BottomStart (`SightingsMap.kt:803-811`);
  - the taxon chip at TopCenter (`AvailabilityCompactMapUi.kt:1080-1100`);
  - the status strip at the rail-side top in landscape (`:1055-1062`);
  - the rail (`:1236-1253`);
  - on the wide layout, the legend and the add button at BottomEnd (`AvailabilityWideLayoutUi.kt:341-346, 362`).

## Bubbles and taps

- **Bubbles are chosen per layer and id** (`ui/map/MapBubbles.kt:205-260`). A kept record's bubble is the same as any other record's. No bubble names an entry or opens one (`MapBubble.kt:135-156`), although the photo bubble shows a count (`:282`).
- **Tap routing is by M1:** marker, then line, then colour. The bubble actions are Open in Journal / Open find, the viewer, Directions, and details.

## Drafts

Committed entries and drafts are read separately (`domain/CartographyEntryRepository.kt:11-15`). L0b excludes draft finds (`domain/GetMushroomLogEntriesUseCase.kt:24`). Nothing gates J8 on drafts.

## Unique claims

`MapSlot` has 9 parameters, which is its ceiling (`ui/map/MapSlot.kt:398-444`; the doc comments' 7 and 8 are stale, `:31, :39-41`). `MapRenderMode` and `MapOverlayContent` have 12 fields each. New fields go on those, not on `MapSlot`.
