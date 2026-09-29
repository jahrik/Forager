# Offline regions on the S22 after the protect-and-fix build (intent 2026-09-28-120)

**Coder:** a Claude Code session configured as `claude-sonnet-5-5` (session configuration; not read back from the serving side).
**Worktree/branch:** `forager-wt/device-offline`, `device-offline`, cut from `origin/journal-redesign` at `cc9a30b`. **APK built at `7d17a5c`** as the dispatch says, by a detached checkout in this worktree. `git diff 7d17a5c cc9a30b` over everything except `docs/`, `prompts/` and `RECORD.md` is empty, so the source is the same either way. (`versionName` carries the commit sha and `versionCode` the commit count, so building at the branch tip would have produced a different APK.)
**Device:** the S22, `R5CT321008R`, only. The tablet is not touched.
**Governing text, verbatim from the dispatch (`prompts/preserved/2026-09-29-06.md`, intent 2026-09-28-120):** "Create, edit and delete nothing. Restore every setting you touch, and read it back." and "Any row missing is an abort. Report it at once and restore nothing without the owner."

## State before the install (read, not inferred)

| Item | Value |
|---|---|
| Installed | `versionName=1.0.1457+gb358a4aa`, `versionCode=1457`, `firstInstallTime=2026-09-22 11:15:05`, `lastUpdateTime=2026-09-28 15:28:49` |
| New APK | `app-debug-7d17a5c.apk`, sha256 `dac26e11b23d16ef6019379e744f5ef2c9ece4eb79aaff9ac5a5f3a83d4555da`, `versionName=1.0.1577+g7d17a5c4`, `versionCode=1577` |
| Certificate | new APK and the installed base.apk both: `CN=Android Debug`, SHA-256 `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`. **Match.** |
| Build | `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug`, exit 0, 0 compile errors, after the Gradle and memory check |

### Backup (taken with the app force-stopped, through `run-as`; evidence outside the repository)

Every file's sha256 on this machine equals the device's:

| File | Bytes | sha256 |
|---|---|---|
| `databases/forager.db` | 229376 | `e88effc0cdcde55dd96cae939b27a9d7b539c2a377c375a51cdac8ad61af7c93` |
| `databases/forager.db-wal` | 466944 | `0ca51dbf6511f985d75a47672c9aeec31076b03f60c4edd78e932f114e77cbac` |
| `databases/forager.db-shm` | 32768 | `b042a2fdb808c1b9d6211aa10adc14c86d5c3ac9a093b9a36aa595e4f53d4450` |
| `files/mbgl-offline.db` | 23674880 | `c0d0d87d9763bbce87afcafb26f2c1fec2a72e4067724b91241a971ea37dad6b` |

No side files for `mbgl-offline.db` exist on the device; `files/maplibre-offline/` is an empty directory. Analysis ran on a scratch copy with the three `forager.db` files together (so the WAL is applied), leaving the backup untouched.

- **`forager.db`:** SQLite 3 header; `integrity_check` = ok; `user_version` = 16. Row counts: `android_metadata` 1, `cached_searches` 2, `cartography_entries` 7, `cartography_entry_find_refs` 2, `cartography_entry_offline_region_refs` 1, `cartography_entry_photo_refs` 1, `cartography_entry_track_refs` 1, `cartography_entry_waypoint_refs` 3, `log_entry_photos` 1, `log_photos` 3, `mushroom_log_entries` 3, `offline_regions` 2, `planned_trips` 0, `room_master_table` 1, `track_points` 23, `tracks` 1, `waypoints` 3.
- **`offline_regions` rows:** id 1 "DEVICE CHECK" (45.3262696, -122.6338591, 1 km, zoom 10 to 15, created 1790569326998, `isEntryCapture` 0); id 2 "DEVICE CHECK 2026-09-28 B" (45.3198262471555, -122.624714326657, 5 km, zoom 10 to 15, created 1790591727547, 0). These are the device checks' own regions.
- **`mbgl-offline.db`:** SQLite 3 header; `integrity_check` = ok. `regions` 2, `resources` 2, `tiles` 967, `region_resources` 4, `region_tiles` 261. Region ids 1 and 2, whose metadata names and coordinates equal the two Room rows.

## Pre-registration (pushed before the install and before the first launch)

Citations are [read] at `7d17a5c`. The planner's outcome predictions are quoted from -120; the mechanism prediction is "not authored" in the dispatch and I add none.

**Predictions (planner, verbatim):** "1. No region-read error on any cold start, and one init line per process. 2. Regions show from launch. 3. Every row kept, with no P1 warning, because Room and MapLibre agree. 4. Offline tiles still render from files/mbgl-offline.db."

**Pass conditions, per check:**

1. **Cold start, three times** (`am force-stop`, `am start`, `logcat -d`, no `logcat -c`; the log is one ring buffer, so each start's lines are separated by process id):
   - no line containing `Couldn't read offline regions.` (`ui/availability/AvailabilityViewModel.kt:934`);
   - **exactly one** `MapLibre initialised.` per process, tag `MapLibreStorage` (`map/MapLibreInitializer.kt:31`; the tag is `map/MapLibreStorage.kt:24`);
   - no `MapLibre initialisation failed` (`map/MapLibreInitializer.kt:27`) and no `MapLibreConfigurationException`;
   - no `FATAL EXCEPTION`.
   - Note: `initializeMapLibreAtStart` (`ForagerApplication.kt:41`, `:53-55`) is the only start-up caller. A later `initializeMapLibre` from `offlineManager()` or `SightingsMap` returns early once initialised, so it must not add a second line.
2. **Regions from launch**, without opening Offline maps first: the Records chip count (`ui/log/RecordsTab.kt:320` passes `visibleOfflineRegions`) includes the 2 regions; the All logbook lists them; a region's bubble on the Maps map opens its card, and `MapBubble` logs no `No offline_region … in the host's lists` (`ui/map/MapBubble.kt:286`, from `MapBubbles.kt:270`, tag `MapBubble`).
3. **Nothing lost**, after force-stop and a fresh copy of both databases: `offline_regions` has ids 1 and 2 with every column identical to the backup's rows above; every other table's count equals the backup's; `mbgl-offline.db` has `regions` = 2, `integrity_check` ok, and I record its `tiles`/`region_tiles` counts against 967/261. The file may differ in bytes (SQLite touches it on open, and MapLibre keeps an ambient cache), so byte equality is not a pass condition; the counts and rows are. **Any missing row or region is an abort, reported at once, and I restore nothing.**
4. **Offline tiles still work:** a region's map opened with Wi-Fi and data left as they are (no network setting changed) and the tiles captured on screen. Caveat recorded in advance: with the network still up, a screenshot of tiles cannot by itself prove they came from the store rather than the network; I will say what the evidence does and does not show.
5. **Logs:** any `Room region row N has no MapLibre region in this read; kept, not shown.` (`map/OfflineRegionReconciliation.kt:41`), with its id, is recorded. Prediction 3 says there are none, because the backup's Room ids (1, 2) equal MapLibre's (1, 2).

**Abort conditions (verbatim in the dispatch):** an unverifiable backup; uninstall, `-d` or a clear; a signature mismatch; any region missing afterwards; a new crash; any prompt over the app; a locked phone; touching the tablet. None has occurred at the time of writing.
