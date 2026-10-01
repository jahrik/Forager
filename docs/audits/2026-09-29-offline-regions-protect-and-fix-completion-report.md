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

---

# Completion report (same session, after the pre-registration above)

## What landed

| Commit | What |
|---|---|
| `9b59a4d` | pre-registration (above), pushed to `journal-redesign` before building |
| `26632d6` → `offline-safety-wip` | Part 1 step 1: decisions extracted at base behaviour, tests written; **8 of 11 fail** |
| `a649ac3` → `journal-redesign` | Part 1: P1, P2, P3 |
| `9b667c0` → `offline-safety-wip` | Part 2 step 1: initialiser stub at base behaviour; **3 of 3 tests fail** |
| `d9cf4fd` → `journal-redesign` | Part 2: F1, F2 |

**P1** `map/OfflineRegionReconciliation.kt` `reconcileOfflineRegions`: a Room row missing from the read is kept, not shown, and logged at `Log.w` with its id every time. No path but `deleteRegion` removes a row except P3's. **P2** `regionListOrFailure`: null list throws `IOException`, called from `listOfflineRegionsSuspend` and logged there. **P3** `incompleteRegionDecision`: as pre-registered, no schema change, no migration. **F1** `MapLibreStorage.kt` rewritten without the redirect. **F2** `MapLibreInitializer` (once, retry after failure, logs), called from `ForagerApplication.onCreate` (`initializeMapLibreAtStart`); `offlineManager()` and `SightingsMap` still call the idempotent `initializeMapLibre`. Comments corrected in `MapLibreStorage.kt`, `AvailabilityViewModel.kt` (`onOfflineMapsOpened` doc) and `SightingsMap.kt`.

## Evidence

- **Tests first, Part 1 (base behaviour):** 8 of 11 failed by assertion, each for the pre-registered reason (rows deleted by empty/partial read: "row 2 survives a read that lacks it expected:[1, 2] but was:[1]", "both rows survive an empty read expected:[1, 2] but was:[]"; nothing logged; incomplete region with a row / a real timestamp / unreadable metadata deleted; null read: "expected java.io.IOException to be thrown, but nothing was thrown"). The 3 controls passed. One more failed than pre-registered by name (unreadable metadata), for the same reason as the timestamp case. The build log had no compile errors.
- **Revert checks, Part 1** (saved copy, build log checked first: 0 compile errors each; forward file's sha256 prefix `01618ea7ef949634` identical before and after): restoring the prune failed exactly the empty-read and partial-read tests with those messages; restoring null-to-empty failed exactly the null-read test.
- **Tests first, Part 2:** 3 of 3 failed by assertion (called 3 times not once; nothing logged; no application-start log).
- **Revert checks, Part 2** (0 compile errors; hashes `d7627553edaa`, `dbbaa6a15a72` identical after): removing the `ForagerApplication` call failed only the application-start test ("no MapLibre initialisation was attempted at application start: []"); removing the once-only guard failed the two once/retry tests ("expected:<1> but was:<3>", "expected:<2> but was:<3>"). A first attempt at these produced `UnsatisfiedLinkError` failures for all three tests, which is a failure the reverts could not have caused, so I did not cite it (see Decisions).
- **Full suite** after Part 1: 311 files, 2526 tests, 24 skipped, 0 failures, 0 errors, 0 files older than the run's start. After Part 2: 312 files, **2529 tests, 24 skipped, 0 failures, 0 errors**, 0 stale. Net change against base, from those counts: +11 (Part 1: +11 new, −3 removed; Part 2: +3). The 24 skips are not mine; I did not inspect them.

## Not tested

- The mapping from a real `OfflineRegion` to `LiveRegion`, `OfflineManager` itself, and `MapLibre.getInstance`: none can run on the JVM. `listRegions` is covered up to that mapping only.
- That the `downloadedAtEpochMillis == 0` marker is what every device's unfinished regions carry (builds before `99606a5` unverified). Such a region is kept, not deleted.
- That MapLibre initialises at application start on a device without the redirect failing; the application-start test only observes that an attempt was made and logged.

## Device-only (S22, after this lands)

Cold start logs no "Couldn't read offline regions"; regions show in Records and bubbles at once; `files/mbgl-offline.db` still holds the same regions and tiles; every Room row is kept; a log line `MapLibre initialised.` (tag `MapLibreStorage`) appears once per process; `Room region row N has no MapLibre region` warnings, if any, name real mismatches.

## Decisions I made

1. **Extraction shape:** a `LiveRegion` data class and one `reconcileOfflineRegions` function taking the DAO, so the wiring is testable with a fake DAO, rather than only pure predicates. The dispatch asked for decisions extracted and wiring tested as far as a fake allows.
2. **Deleted `OfflineRegionIdsToDeleteTest.kt`** (3 tests) because its subject, `offlineRegionIdsToDelete`, was replaced by `incompleteRegionDecision`. Its three cases (in flight kept, complete kept, orphan deleted) are covered by the new tests, the orphan case now only under P3's rule. Not a silenced test, but a test removal; say so if you disagree.
3. **`MapLibreInitializer` catches `Throwable`** (logs, rethrows) and `ForagerApplication` catches `Exception` and `LinkageError` and logs. Under Robolectric the SDK throws `UnsatisfiedLinkError`, an `Error`; without the `LinkageError` catch every Robolectric test that boots the application crashed (my pre-registered prediction 4 named the risk). Consequence: a device whose native library will not load now starts and fails when a map is opened, instead of crashing at launch. This is a behaviour choice the owner may want to rule on.
4. **Kept the log tag `MapLibreStorage`** for the initialiser, so nothing that greps logs for it changes.

## Flags outside scope

- **Rule slip:** the first Part 2 Gradle runs started while another agent's build (`forager-wt/layout-fixes`) was running; `pgrep` showed it and I ran anyway. No OOM or interference showed in my results, but the rule says wait. I waited on every later run.
- `ui/availability/AvailabilityOfflineMapsUi.kt:19-20` still says `initializeMapLibre` "is called only from SightingsMap and MapLibreOfflineMapRepository"; it is now also called from `ForagerApplication`. Out of scope; not changed.
- Audit docs (`2026-09-07-offline-style-swap-*`, `2026-09-08-data-inventory-for-privacy-policy.md:433`, `2026-09-08-backup-exclusion-rules-completion-report.md:284`) name `filesDir/maplibre-offline` as the store; they are wrong per the device listings. Not edited. The backup-exclusion report at `:284` implies exclusion rules may have been written for that path; `app/src/main/res` has no `maplibre`/`mbgl` match, so I found none, but whether the live `files/mbgl-offline.db` is covered by backup rules was not checked.
- The pulse's `SightingsMap.kt:274/275` are `:281/282` at this base.
- Merge not done, as instructed.
