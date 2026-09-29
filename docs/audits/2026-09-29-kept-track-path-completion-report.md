# F3 completion report: a kept track keeps its path (dispatch 2026-09-28-195)

**Coder session.** Configured model `claude-sonnet-5-5` (the session's own setting; the serving model was not read back from inside the session).
**Branch:** `kept-track-path`, worktree `/home/zynergy-labs/Zynergy/forager-wt/kept-track-path`, cut from `origin/journal-redesign` at `34c98256` (which contains the dispatch's base `ce30ac4c`; verified with `git merge-base --is-ancestor`).
**Governing files, read in full:** `prompts/preserved/2026-09-29-37.md`; `docs/plans/journal-redesign.md:1189-1205` ("A kept track keeps its path", "Kept track paths: F3's design"); `docs/audits/2026-09-29-kept-track-path-pulse.md`. `CLAUDE.md` at this base differs from the copy the session opened with only by one added bullet (map chrome at 80%) and the trailing "Roles and gates" section; no conflict with the dispatch found.

## Pre-registration (written and pushed before any code)

### Premises checked at the base
| Premise (pulse, at `c43e2e9d`) | Checked at `34c98256` | Result |
|---|---|---|
| DB is at version 16 | `ForagerDatabase.kt:167` (`version = 16`), `:228` (`SCHEMA_VERSION = 16`) | holds |
| Nothing claims 17 | 135 `refs/remotes/origin/*` and 44 local `refs/heads/*` searched: no `17.json` under `app/schemas` on any remote ref, no `MIGRATION_16_*` in `app/src/main/java` on any ref, and no `version = 17` in `data/local` on any local head; 27 remote branches declare `version = 16`, none 17 | holds |
| Refs are (entryId, trackId), no @ForeignKey, index on trackId | `CartographyEntryEntity.kt:73-77` | holds |
| Track snapshot holds no path | `CartographyEntryTrackRefEntity` fields at `CartographyEntryEntity.kt:78-86` | holds |
| Read-seam filter is in `RoomTrackRepository.toDomain` only | `RoomTrackRepository.kt:87-100` (`excludeNetworkProviderFixes`) | holds |
| Delete removes points then row in one transaction | `TrackDao.kt:70-74` | holds |
| `DeleteTrackUseCase` copies nothing today | `domain/DeleteTrackUseCase.kt:19-25` (detach waypoints, then delete) | holds |
| Entry map draws kept tracks only from live points | `GetCartographyEntryMapDataUseCase.kt:53-56` | holds |
| Card thumbnail reads live tracks | `CartographyEntryCard.kt:345-346` | holds |
| Bubble for a tapped track reads live tracks | `MapBubbles.kt:258-269`, waypoint fallback at `:245-247` | holds |
| Merge drops a track ref whose track is missing | `JournalTables.kt:60-63` (`needs = tracks`), `RoomJournalBackup.kt:361-384` | holds |
| Bubble stats come from points | `MapBubbles.kt:259` recomputes distance and duration from `track.points` | **wrong for a saved path**: a saved path has no timestamps, so duration cannot be recomputed; the decision snapshot already holds distance and duration. See "Decisions I made". |

### Predictions and pass conditions
Stubs (signatures, an empty `MIGRATION_16_17`, a codec that throws `NotImplementedError`, a delete that copies nothing) go in first so the tests compile, then the tests run and are expected to fail as follows.

| Test | Predicted failure at the stubs | Passes when |
|---|---|---|
| `TrackPathCodecTest` (new, pure) | `NotImplementedError` from the stub codec, every case | round trip is exact for many points, one point, empty; malformed length is an explicit failure |
| `SchemaMigrationTest` 16→17 and the chain to 17 (`SchemaMigrationTest.kt:121-136`, `:218`) | Room validation: `Migration didn't properly handle: cartography_entry_track_paths` (the empty stub creates no table) | table and index exist as `17.json` declares, every seeded value survives |
| `DeleteTrackUseCaseTest` (new cases) | assertion: the fake path repository recorded no copy | copy is called with the track's read-seam-filtered points before `delete`; a failed copy leaves the track undeleted; a missing track copies nothing and still deletes |
| `TrackDeleteEntryRefsTest` (real Room; kept, withheld, draft, two entries, entry delete) | assertion: `cartography_entry_track_paths` has 0 rows after the delete | one row per ref row, kept or withheld, draft or not; the entry's delete removes them |
| `GetCartographyEntryMapDataUseCaseTest` (saved line; live wins; withheld draws nothing; no saved path draws nothing) | assertion: saved-line case returns no polyline | saved line drawn only for a kept decision whose track is gone |
| bubble (`MapBubbles`) and card thumbnail tests | assertion: no track content / no thumbnail for a deleted track | fall back as the waypoint bubble does |
| `JournalBackupTest`: Merge and Replace with a deleted track | assertion: Merge drops the track ref and its path row (`rowsDropped` 1) | ref and its path survive both modes |
| `JournalBackupTest` table guard (`JournalBackupTest.kt:703-713`) | list mismatch: schema has a table the journal list lacks | the new table is `OWNED`, owner `cartography_entries`, no `needs` |

Expected edits to existing tests, each because of the dispatch's rulings or the version bump, none weakening a claim (quoted where the edit is made below): `TrackDeleteEntryRefsTest`, `GetCartographyEntryMapDataUseCaseTest.kt:114`, the two `16` pins at `JournalBackupTest.kt:79` and `:711`, `:651` (a "newer than the app" backup must now be 18, not 17), `:683` and `JOURNAL_TABLE_NAMES` (a v15 backup has no path table), `JournalBackupTestSupport.kt:123` (`SCHEMA`), `SchemaMigrationTest.kt:218`.
