# Offline regions: protect, then fix start-up (dispatch 2026-09-28-106, relaunch 2026-09-28-111)

**Coder:** a Claude Code session configured as `claude-sonnet-5-5` (from the session configuration; not read back from the serving side).
**Base:** `journal-redesign` at `5a7fecd`, verified against the remote by `git pull --no-rebase` before starting. Worktree `forager-wt/offline-safety`, branch `offline-safety`.
**Owner's ruling, verbatim (from the dispatch):** "Option A. Protect then fix."

## Pre-registration (written and pushed before any code)

Paths under `app/src/main/java/com/zynergylabs/forager/app/`. All [read] at `5a7fecd`.

### Premises checked

| Premise | Result |
|---|---|
| `map/MapLibreOfflineMapRepository.kt:190` prunes Room rows missing from the read | holds: `offlineRegionDao.getAll().filter { it.id !in liveIds }.forEach { deleteById }` |
| `:317` null list becomes empty | holds: `offlineRegions?.toList().orEmpty()` in `listOfflineRegionsSuspend` |
| `:211-215` incomplete, not-in-flight region deleted (tiles and row) | holds; decision is `offlineRegionIdsToDelete` (`:296-297`), in-flight set `:105` |
| `map/MapLibreStorage.kt:54-58` redirect before `getInstance` (`:95`) | holds |
| Start-up read `AvailabilityViewModel.kt:922-934`; init call `:167` | holds |
| `SightingsMap.kt:274` is the only pre-`MapView` init | holds (init call is at `:281`, `MapView` at `:282`; the pulse's line numbers are off by a few, the shape holds) |
| Nothing reads `filesDir/maplibre-offline` | holds for `app/src`: the only match is the redirect itself (`MapLibreStorage.kt:57`); `res/` and the manifest have no `maplibre`/`mbgl` match. Older audit docs mention it; they are prose. |

### P3 finding, before building: it needs no migration

What the code can tell about whether a region ever finished, with no schema change:
- `download()` creates the region with placeholder metadata whose `downloadedAtEpochMillis = 0L` (`MapLibreOfflineMapRepository.kt`, `placeholderMetadata`), and overwrites it with a real timestamp only **after** `downloadToCompletionSuspend` returns (`updateMetadataSuspend`).
- The Room row is upserted only after that, so a region that never finished has **no Room row and metadata with `downloadedAtEpochMillis == 0`**.
- The rule I will build: an incomplete, not-in-flight region is deleted only if it has no Room row **and** its metadata parses with `downloadedAtEpochMillis == 0L`. Anything else incomplete (a Room row exists, unparseable metadata, a real timestamp) is kept, not shown, and logged with its id.
- Unverified: that builds before the placeholder existed wrote the same marker (the marker's first appearance in history is `99606a5`; whether any device holds regions from before it is not determinable here). Such a region fails the rule and is kept, which is the safe direction.

### Shape

New file `map/OfflineRegionReconciliation.kt` holds the decisions and `reconcileOfflineRegions(...)`, taking plain data (`LiveRegion`), the DAO, an in-flight set, a delete lambda and a warn lambda, so `listRegions` becomes read, map, reconcile. This is the extraction of each decision into headless code that the dispatch asks for; the wiring test drives `reconcileOfflineRegions` with a fake DAO. Not covered by any JVM test: the thin mapping from `OfflineRegion` to `LiveRegion` and the `OfflineManager` calls themselves.

**Step 1 (tests first):** extract with today's behaviour unchanged, so the new tests run against base behaviour. **Step 2:** the fix.

### Predictions

1. Tests-first, at base behaviour, **fail by assertion** (not compile error) for these reasons:
   - `an empty read deletes no Room row`: base prunes every row.
   - `a partial read keeps the missing row`: base deletes it.
   - `a missing row is logged with its id`: base logs nothing.
   - `an incomplete region with a Room row is not deleted`: base deletes it.
   - `an incomplete region with a real timestamp is not deleted`: base deletes it.
   - `a null read is a failure`: base returns an empty list.
2. Tests that should pass at base (controls, not counted as biting): a complete region is never deleted; a never-finished region (no row, timestamp 0) is deleted; an in-flight region is not deleted.
3. Revert checks (restore from a saved copy, not git; build log read for compile errors first): restoring the prune fails the empty-read and partial-read tests with the messages naming the surviving row; restoring null-to-empty fails the null-read test.
4. F2 puts `MapLibre.getInstance` in `ForagerApplication.onCreate`. `MapLibreStorage.kt:44-49` records an earlier attempt reverted because Robolectric boots the real `ForagerApplication` and MapLibre's native init throws there. My plan, unless that is wrong: the call is wrapped so a failure is logged (never swallowed) and the process continues, so every Robolectric test that boots the application logs one warning and passes. Checked in Part 2.
5. Suite grows by 8 to 15 tests.

### Pass conditions

Part 1: the six tests-first tests fail at step 1 for the stated reasons, pass after step 2, both revert checks fail with edit-specific messages, and the full suite from a cleared results directory has zero failures.
