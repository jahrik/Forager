# Offline regions at start-up: can a start-up failure delete a saved region? (pulse, read-only, at `ae0b90f`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `ae0b90f`, through `git show`. No adb calls. Device facts come from other agents' evidence files, read locally.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with every citation kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`. Labels: [read] code at `ae0b90f`, [observed] another agent's evidence file, [inferred] reasoning.

**Why:** two device checks flagged this area.
- The tablet logs `Couldn't read offline regions.` with `MapLibreConfigurationException` at every start-up (terminal `2026-09-28-101`).
- On the S22 the region list is empty after every launch until the Offline maps panel opens (terminal `2026-09-28-102`, flag 1). That coder also read that `listRegions` can delete at start-up.

## First: a branch that deletes on an empty read

**`map/MapLibreOfflineMapRepository.kt:190`:** `offlineRegionDao.getAll().filter { it.id !in liveIds }.forEach { offlineRegionDao.deleteById(it.id) }`
- Every Room row missing from a *successful* `listOfflineRegions` read is deleted, **including an empty read**. A `null` from MapLibre becomes an empty list (`:317`) [read].
- It deletes the Room index row (name, centre, radius, created-at), not the tiles. A row comes back only if MapLibre lists the region again, from its metadata (`:219-234`) [read].
- There is no plausibility check. `OfflineRegionEntity.kt:21-22` calls the Room table "the source of truth", but `:190` treats the live read as authoritative [read].

**The observed start-up exception does not reach it.** The exception is thrown in `offlineManager()` (`:261`) before `:183` returns, and every delete comes after `:183` [read, and the S22's stack].

**The hazard is the obvious fix** [inferred from the pieces below]:
- The exception stops MapLibre's storage redirect to `filesDir/maplibre-offline` (`map/MapLibreStorage.kt:57`) from ever taking effect.
- The live store is the SDK default, `files/mbgl-offline.db`: 23,224,320 B on the S22 and 65,536 B on the tablet [observed: `device-evidence/2026-09-28-map-chrome-check/04-app-files-start.txt:24-25`; `device-evidence/2026-09-28-tablet-sanity/09-app-files-start.txt:6-7,15-18`]. On the tablet, `files/maplibre-offline/` is empty [observed].
- `MapLibreStorage.kt:49-51` says the redirect "does not migrate any region already downloaded" [read].
- So a change that makes the redirect succeed (for example calling `MapLibre.getInstance` before `FileSource.setResourcesCachePath`) would point MapLibre at an empty store. The first successful `listRegions` would then **delete every Room row** at `:190`, leaving the tiles unseen in `files/mbgl-offline.db`.

## The start-up path (Q1)

- **The S22's trace at every cold start** [observed: `device-evidence/2026-09-28-map-chrome-check/204-log-end.txt:79700-79746` and repeats]:
  1. `MainActivity.kt:293`
  2. the lazy ViewModel (`:58-61`)
  3. `AvailabilityViewModel.<init>` `:167`
  4. `loadOfflineRegions` `:922-923`
  5. `listRegions` `:183`, then `offlineManager` `:261`
  6. `initializeMapLibre` (`MapLibreStorage.kt:94`), then `ensureMapLibreStorageOutsideCache` `:58`
  7. `FileSource.setResourcesCachePath`, then `MapLibre.getApplicationContext`, then `validateMapLibre`, which throws.
- **`MapLibre.getInstance`** has one call site, `MapLibreStorage.kt:95`, after `:94`. The only `MapView` construction is `SightingsMap.kt:275`, after `:274` [read].
- **The flag.** `:54` sets `hasRedirected` before `:58`, so later calls in the process skip to `getInstance` and succeed [read]. The first successful `getInstance` is the process's second `initializeMapLibre` [inferred].
- **Caught.** By `runCatchingCancellable` (`data/repository/RunCatchingCancellable.kt:18-24`), then logged at `AvailabilityViewModel.kt:931-934` [read].
- **State afterwards.** `offlineRegions` stays `emptyList()` (`AvailabilityUiState.kt:167`). `offlineRegionsErrorMessage` is set (`:933`), but shown only by the Offline maps panel (`AvailabilityOfflineMapsUi.kt:255`) [read].
- **The fallback to the default path is never logged.** The redirect's `onError` (`MapLibreStorage.kt:67-69`) never fires [read]; there are 0 "Couldn't redirect" lines [observed]. That falls short of CLAUDE.md's "Errors and failure paths".

## `listRegions`, every branch (Q2, `:182-249`)

1. `offlineManager()` throws (the observed case): no delete, failure [read].
2. `listOfflineRegions` `onError` (`:320-321`): `IOException`, no delete [read].
3. `onList(null)`: becomes an empty list, and **`:190` deletes every Room row**, returning success [read]. Whether the SDK ever sends null is not determined.
4. An empty array while the regions exist elsewhere (a store switch): **`:190` deletes every Room row** [read; the trigger is inferred].
5. A partial list: `:190` deletes the missing ids' rows [read].
6. A `getStatus` failure after `:190` (`:197`, `:389-401`): returns failure with the `:190` deletes already committed [read].
7. `!status.isComplete` and not in flight (`:211-215`, `offlineRegionIdsToDelete` `:296-297`): **deletes the MapLibre region (tiles) at `:213` and the Room row at `:214`** [read]. It is meant for killed downloads, and the in-flight set is per process (`:105`, `:94-99`). Whether a completed region can later report `isComplete=false` is native behaviour, not determined.
8. The delete in branch 7 fails: throws, and the row is kept [read].
9. A complete region with no Room row: rebuilt from metadata (`:219-234`), or hidden if there is no metadata (`:235-236`) [read].

## Why the list fills later (Q3)

- Opening Offline maps runs `RecordsTab.kt:188-193`, then `onOfflineMapsOpened` (`AvailabilityViewModel.kt:897-905`), then `loadOfflineRegions` [read]. It succeeds because it is the process's second init [inferred].
- The doc at `:885-890` blames "native store still finishing its own initialization"; the trace shows the exception instead [inferred].

## Consequences before the panel opens (Q4)

- **Missing regions:**
  - bubbles miss them (`MapBubbles.kt:270`, `MapBubble.kt:286` logs) [read; observed];
  - the Records chip count is 0 (`RecordsTab.kt:239`) and the All logbook lists no regions (`RecordsLogbookList.kt:105`), with no error shown [read].
- **Unaffected:**
  - the map's region layers use their own read (`GetMapRecordsUseCase.kt:106`), which succeeds [read; observed];
  - J8 highlights (`GetJournalEntryHighlightsUseCase.kt:84`);
  - the entry report's offline-tiles toggle (`GetCartographyEntryOfflineRegionUseCase.kt:39`);
  - the trip report (`GetTripReportOfflineRegionsUseCase.kt:26`) [read].

## Tests (Q5)

- `OfflineRegionIdsToDeleteTest.kt:19-47` covers only the pure incomplete-region decision [read].
- **Nothing tests** the `:190` prune, `:213-214`, the rebuild, `:317`, `initializeMapLibre` or the redirect. `OfflineManager` cannot be built off a device (`:290-294`), and `androidTest` holds only a camera test [read].
- `AvailabilityViewModelOfflineMapsTest.kt:283-292` pins today's behaviour through a fake [read].
- **No test would catch a deletion from a failed or empty read.** A prune followed by a rebuild writes back the same values, so "digests equal" on a device cannot rule one out [inferred].

## History (Q6)

- `1726b90` (2026-08-23): the redirect, with the flag set before the call.
- `66d53b8` (2026-08-24): the `:190` prune, the incomplete-region delete, and the init-time `loadOfflineRegions()`.
- `5f3fdba` (2026-09-02): `initializeMapLibre`.
- Carried in the seed `1cea45b` (2026-09-09).
- F4's in-flight exemption: `cd5f0bf` and `53ecbff` (2026-09-27).
- The SDK has been pinned at 13.5.0 since `60bb499` (2026-08-18).
- The S26 very likely hits the same exception at every cold start [inferred]. No S26 evidence either way on pruning.

## Premises that were wrong

- "listRegions can delete at start-up": the start-up call throws before any delete. Deletes run on the first *successful* read in each process.
- `MapLibreStorage.kt:10-19` says "the default path is the cache dir". The live database is at `files/mbgl-offline.db` [observed].
- `MapLibreStorage.kt:24-25` says it "guarantees the redirect precedes getInstance()". That ordering is what makes the redirect throw.
- Three audits say regions live in `filesDir/maplibre-offline`: `2026-09-07-offline-style-swap-prebuild-report.md:117`, `2026-09-08-data-inventory-for-privacy-policy.md:433` and `2026-09-07-cartography-plate-renderer-pulse.md:279`. The device listings contradict them.
- **A latent crash** [inferred]: if a path ever composed `SightingsMap` before any `listRegions` in a process, `SightingsMap.kt:274` would throw during composition. Deferring the start-up read would expose this.

## Could not determine

- Whether MapLibre ever sends `null` or a partial list, and whether a complete region can report incomplete.
- Whether a successful redirect moves or persists the store.
- The S22's `files/maplibre-offline/` contents.
- Anything on the S26.
- The builds of 2026-08-23 to 2026-08-24.
- How `MapLibreStorage.kt:14-15`'s "confirmed on hardware" squares with the observed location.
