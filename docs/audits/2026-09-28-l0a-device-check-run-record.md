# Device check, map layers L0a on the S22 Ultra: run record

**Status: stopped at an abort condition.** A stored waypoint that the code says the Maps tab draws is
not drawn there (see "Stop" below). Checks 1 to 3 were finished before the stop. Check 4 was stopped
before it finished, and checks 5 and 7 have no data on this phone. The settings and the crash log were
still read back at the end.

**Date:** 2026-09-28, 03:08 to 03:29 UTC. The phone's clock showed UTC-7.
**Device:** Samsung SM-S908U, serial `R5CT321008R`, Android 16. `getprop` at 03:08:48Z read
`ro.product.model=SM-S908U` and `ro.build.id=BP2A.250605.031.A3`. It was the only device attached
(`adb devices -l`), and every adb call named it with `-s`.
**Build under test:** the build already installed, `versionName=1.0.1192+g24589349`, `versionCode=1192`.
Nothing was installed, and neither Gradle nor an emulator was run.
**Dispatch:** `prompts/preserved/2026-09-28-02.md`; the intent is `2026-09-28-02` in `RECORD.md` (the planner's).
**Base:** `origin/journal-redesign` at `f18af53`, the planner's store-copy commit after `f62eb3e`.
`git diff --stat f62eb3e f18af53` touches only `RECORD.md` and the store copy.
**Pre-registration:** each check's pass condition and prediction were committed at `3a87422`, before
that check was run. They are repeated below unchanged, except where a correction is marked.
**Evidence:** all of it is outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-l0a/`.
Each file is cited by name, with its sha256 in the Evidence index at the end. No screenshot, dump,
coordinate or place name is in this file. Where a verdict rests on my reading of an image, it says so.
**dp to px:** `wm density` is 450, so 1 dp = 450 / 160 = 2.8125 px. The screen is 1080 x 2316 px (`wm size`).
**Owner:** not at the phone for the run.

## Outcome

| Check | What | Verdict |
|---|---|---|
| 1 | Build and baseline | **pass**: build `1.0.1192+g24589349`; the crash buffer was empty at the start and at the end |
| 2 | Attribution caption text | **pass** on the Maps tab: Street, Topographical and Satellite each byte-equal to the code. Entry map with offline tiles: **not run**, because the phone has no Cartography entry and no offline region |
| 3 | Sighting bubble: direct tap, near miss, far tap | **pass** on all three |
| 4 | Draw order on the Maps tab | **not run**: stopped at the abort condition. Until the stop, no two registry layers were seen overlapping outside the location puck, and the breadcrumb part had nothing it could overlap (see check 4) |
| 5 | Draw order on an entry map | **not run**: `cartography_entries` has 0 rows |
| 6 | Defaults look as before (an observation) | Sighting dots and the reticle draw at the code's opacities (measured). **The ORIGIN waypoint pin is missing: this is the stop** |
| 7 | Night Maps leaves overlays alone | **not run**: no entry map and no offline tiles. **The dispatch's pass condition also conflicts with the code** (see check 7) |
| — | Hidden layers excluded from queries; state-driven visibility and opacity | **not runnable at this build**, as the dispatch records. Moved to L0b's device check; not attempted |
| — | `featureId` from the hit test; a tap inside an offline circle falls through | **not runnable at this build**, as the dispatch records. Moved to M1; not attempted |

- No new Forager crash: `logcat -b crash` was empty at 03:09:02Z and at 03:29:24Z.
- Every setting the run changed is back at its starting value (see Settings).
- **Planner's predictions:**
  - 1 held: the caption matched on all three basemaps.
  - 2 held: the bubble opened on the direct tap and the near miss, and not on the far tap.
  - 3 held: check 5 is not run for want of data, and check 4 did not finish.
  - 4 cannot be evaluated: check 7 was not run.

## Stop: the ORIGIN waypoint is not drawn on the Maps tab

**The abort condition met:** "a result that suggests L0a changed something visible it was not ruled to".
As the dispatch instructs, this section describes the result and cites the code. It does not look for
the cause.

**What the data and the code say should be drawn.** The phone stores one waypoint, with designation
`ORIGIN`, belonging to the open track (`waypoints`: 1 row). Its route to the Maps tab:
- `GetWaypointsUseCase` returns every stored waypoint (`domain/GetWaypointsUseCase.kt`).
- `TrackRecordingViewModel` loads that list at `init` and again after the origin waypoint is saved
  (`ui/track/TrackRecordingViewModel.kt:190`, `:561`, `:571-576`).
- `MainActivity` passes `trackUiState.visibleWaypoints` (`MainActivity.kt:545`), which is the list minus
  any pending delete (`ui/track/TrackRecordingUiState.kt:130`).
- The Maps tab passes that list to `MapOverlayContent.waypoints` (`ui/availability/AvailabilityCompactMapUi.kt:602`).
- `waypoints-layer` is in the registry's markers group, above the search centre and the sightings
  (`ui/map/layers/MapLayers.kt:249`). It is drawn as a bitmap pin (`ui/map/SightingsMap.kt:798`).
- The pin is a 22 x 28 dp teardrop in `0xFF350560` with a white casing, and its tip is on the coordinate
  (`ui/map/MarkerGlyphs.kt:36`, `:185`; `ui/theme/MapPalette.kt:75`). So its head rises about 79 px above
  the point.

**Where it should be.** From the database copy, as distances only:
- The waypoint is 0.0 m from the track's first point and 9.9 m from the search centre.
- The reticle marks the search centre. It was on screen throughout, at about (539,1195) px.
- The camera's zoom was not read. `zoomForRadiusKm` gives z12 for this search's 8 km radius
  (`SightingsMap.kt:1507-1512`); at z12, 9.9 m is about 2 px, and even at z14 it would be about 8 px. So the pin's tip should be within a few px of the reticle's centre, with its head
  rising well clear of the location puck's white halo (about 26 px in radius).

**What was on screen.**
- **By eye:** no pin appears in the screenshots I viewed whole (`10-app-foreground.png`, `11-menu-dismissed.png`,
  `22-street.png`), or in the crops around the reticle (`11-crop-puck-x4.png` on Topographical,
  `22-crop-puck-x4.png` on Street, `35-crop-puck-x2.png` on the restored Topographical). Those show only
  the reticle and the puck.
- **By pixel search:** across five Maps-tab screenshots (`11-menu-dismissed.png`, `22-street.png`,
  `24-satellite.png`, `26-topo-restored.png`, `35-after-far-tap.png`), the map area (x < 920, y 75 to 1950 px) has
  **0 pixels** within 12 per channel of `#350560`.
  - The same search does find a colour that is there. The reticle, a bitmap marker from the same
    pipeline, renders its fill at exactly `#000000` (40 to 54 exact pixels per arm, check 6).
  - So a drawn pin would have shown as hundreds of pixels near its fill colour.
- **On both sides of the puck order:** the pin is absent both when the puck draws over the overlays
  (Topographical, at arrival) and after a style reload, when the overlays draw over the puck. That second
  state held on Street and on the restored Topographical, where the reticle's crosshair covers the puck's
  blue dot (`22-crop-puck-x4.png`, `35-crop-puck-x2.png`; see Flags).
- **In the log:** the app's log (`40-logcat-app-pid.txt`, 02:50 to 03:27Z) has no `SightingsMap` line at
  all. That window includes the three style loads at 03:17 to 03:18Z. So neither of the map's own warnings
  for a layer it cannot build or cannot find was logged (`SightingsMap.kt:749`, `:852`).

**Why this suggests L0a, and what is not known.**
- L0a rebuilt how every overlay layer is constructed and ordered (`e3591aa`) and wrote a `featureId` into
  each builder, the waypoint builder included (`b0ce1be`).
- Whether this pin was drawn on a build before L0a, and whether the ViewModel's list held the waypoint
  in this session, is **not known**. Nothing on screen shows either, and finding out is looking for the cause.
- Two further code reads were made before stopping, to be sure the expectation above was the code's and
  not mine: `GetWaypointsUseCase`, and the callers of `loadWaypoints`. Both are cited above.

**The breadcrumb, for comparison, is not evidence of the same thing.**
- By the network-fix rule (`domain/NetworkProviderFix.kt:75-78`), the open track's kept points are 1, 3,
  5 and 7: a line 3.0 m long, all within 3.0 m of the waypoint.
- It is not seen either. But a line that short, at about z12, may be simplified away by the source, or
  hidden under the reticle and the puck.
- That is **unverified**, so its absence is recorded and not counted towards the stop.

## Inventory (read-only, before any check)

**How it was read.**
- `sqlite3` is not on the phone. The database was copied byte for byte with
  `adb exec-out run-as com.zynergylabs.forager.app cat databases/forager.db` (and `-wal`, `-shm`) into the
  evidence directory, and queried there with the host's `sqlite3`.
- The first copy was checkpointed by the host `sqlite3` when opened. Only the copy changed: the phone's
  file listing was the same before and after. So the raw bytes were pulled again into `db-raw/`, hashed,
  and only a second copy (`db-query-copy/`) was queried.
- Only counts, timestamps and distances were read out; no coordinate was printed or recorded.

| Data | On the phone | Drawn where |
|---|---|---|
| Sightings | one cached search (30 species in its list); many dots on the Maps tab, not counted | Maps tab: dark dots with white rings, spread over the screen, several in overlapping clusters |
| Search centre | the cached search's centre | Maps tab: the reticle at about (539,1195) px, where the location puck also sits |
| Planned trips | 0 rows | none |
| Waypoints | 1 row, `ORIGIN`, on the open track | **not drawn** (see Stop) |
| Breadcrumb | 1 open track: 5 points at the start and 7 at the end, of which 3 and then 4 were kept by the network-fix rule | not seen (3.0 m long; see Stop) |
| Cartography entries | 0 rows (and 0 rows in every `cartography_entry_*_refs` table) | none, so no entry map exists |
| Offline regions | 0 rows | none |
| Finds, photos | `mushroom_log_entries` 0, `log_photos` 2 (album photos, not on any map) | none |

**Found on arrival, not in the dispatch.**
- **A track recording was already running.** `TrackRecordingService` was a foreground service created
  about 47 minutes before 03:10Z. The track row has `endedAtEpochMillis` NULL, started 02:23:46Z, and
  its origin waypoint was saved at 02:23:47Z. I did not start it and did not touch it: stopping or
  discarding it would change data this dispatch did not create. It was still running at 03:29.
- **The Maps tab's add menu (Trip / Find / Waypoint) was open**, over its scrim (`10-app-foreground.png`,
  `.xml`). I closed it by tapping its scrim at (200,1700) px. The scrim is `clickable(onClick = onDismiss)`
  (`ui/availability/AvailabilityMapControlsUi.kt`, `AddActionTile`), and it consumes the tap.

---

## Check 1: build and baseline

- **Pass condition:** `dumpsys package` shows `versionName=1.0.1192+g24589349`. The app tree matches the
  base. `logcat -b crash` is read at the start and at the end, and a new Forager line is a stop.
- **Evidence:**
  - `01-dumpsys-package-start.txt` (03:09Z): `versionCode=1192`, `versionName=1.0.1192+g24589349`,
    `lastUpdateTime=2026-09-27 19:18:29` (phone clock, so 02:18:29Z), `firstInstallTime=2026-09-22 11:15:05`,
    flags `DEBUGGABLE HAS_CODE ALLOW_CLEAR_USER_DATA`.
  - `git diff --stat 2458934 f62eb3e -- app/` is empty, and so is the same diff to `f18af53`. `b0ce1be` is
    an ancestor of `2458934`, and no commit in `b0ce1be..2458934` touches `app/`.
  - `02-crash-start.txt` (03:09:02Z): empty, so there was no Forager line at the start.
  - `90-crash-end.txt` (03:29:24Z): empty.
  - `91-dumpsys-package-end.txt`: the same `versionName`, `lastUpdateTime` and `firstInstallTime`. The
    process was the same at the start and the end (PID 19584).
- **Verdict: pass.**

## Check 2: attribution caption text

- **Pass condition (pre-registered):** the caption at the map's bottom start reads exactly the following,
  with no ` · ` separator:
  - Street: `© OpenStreetMap contributors` (`ui/map/Basemap.kt:170`);
  - Topographical: `© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)` (`:161`);
  - Satellite: `USGS The National Map, orthoimagery — public domain` (`:147`);
  - an entry map with offline on: `Protomaps © OpenStreetMap` (`map/OfflineStyle.kt:28`).

  The reasons:
  - The caption is `attributionCaption(mapCreditsFor(…, activeLayerCredits(…)))` (`ui/map/SightingsMap.kt:681`).
  - No registry layer sets `credit` (`ui/map/layers/MapLayers.kt:211-252`; the default is `null`, `:109`).
  - The Maps tab passes no offline flag and no layer state (`ui/availability/AvailabilityScreen.kt:840`;
    `ui/map/MapSlot.kt:104`, `:174`).
  - `MapMode` maps to `Basemap` at `ui/map/MapMode.kt:29-31`.
- **Prediction:** a match on all three basemaps. The entry-map part is not run.
- **Disclosure:** the expected texts were read from the code before the phone was touched. The
  Topographical caption was first seen at 03:11:43Z, in the arrival screenshot, before the pass condition
  was written down.
- **Evidence:** in each dump, the caption is one `TextView` at bounds starting `[17,1905]`, the bottom start
  of the map, above the bottom bar at y=1956. Each text was compared with the literal read from `Basemap.kt`
  at the line cited, as strings in Python:

  | Basemap | Dump (UTC) | Text in the dump | Equal to the code |
  |---|---|---|---|
  | Topographical (arrival) | `11-menu-dismissed.xml` (03:12:31) | `© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)` | yes |
  | Street | `22-street.xml` (03:17:00) | `© OpenStreetMap contributors` | yes |
  | Satellite | `24-satellite.xml` (03:18:05) | `USGS The National Map, orthoimagery — public domain` | yes |
  | Topographical (restored) | `26-topo-restored.xml` (03:18:46) | `© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)` | yes |

  - Non-ASCII characters were compared too: `©` is U+00A9 in both, and the dash is U+2014 in both.
  - The basemap was changed through the map-mode picker, with real taps at chip centres from a fresh dump:
    Street `[200,1112][406,1247]`, Satellite `[789,1112][1035,1247]`, Topographical `[417,1112][778,1247]`.
    The map-mode button's description confirmed each switch.
- **Verdict: pass** on the Maps tab. The entry-map-with-offline part is **not run**: no Cartography entry
  and no offline region.

## Check 3: the sighting bubble on a direct tap, a near miss and a far tap

- **Pass conditions (pre-registered):**
  - (a) A **direct tap** at a dot's centre: the bubble appears (its texts and a `Close` button in the dump),
    and the tapped dot's ring turns blue. The ring is the map's own rendering of `focusedObservationId`.
  - (b) A **near miss** about 16 dp (45 px) from an isolated dot's centre, outside its drawn edge, with no
    other drawn overlay feature within the ±24 dp (±67.5 px) box: the bubble appears for that dot, and its
    ring turns blue.
  - (c) A **far tap** at least 60 dp (169 px) from every drawn overlay feature: no bubble appears.

  From the code:
  - The listener queries the tap point first, then a `TAP_BOX_DP` = 48 dp box (`ui/map/SightingsMap.kt:343-395`,
    `:359-363`; `ui/map/layers/TapPrecedence.kt:89`).
  - A sighting winner sets `tappedSighting` (`ui/availability/AvailabilityCompactMapUi.kt:618-622`), and
    `onTap` clears it (`:614-617`).
  - The selected ring is `0xFF2196F3` at 3 dp; the others are white at 1.5 dp (`SightingsMap.kt:1128-1149`,
    `:1390-1391`; `ui/theme/MapPalette.kt:87-88`).
  - The dot's radius is 9 dp (`SightingsMap.kt:1378`), so its drawn edge is 10.5 dp (29.5 px) from its centre.

  (Pre-registered line numbers `:611-614`, `:616-620` and `:752` were corrected to `:614-617`, `:618-622`
  and `:753` before the commit.)
- **Prediction:** all three as the code says.
- **How the dots were located.** Native features are not in the dump.
  - I picked candidates by eye from the screenshot, then refined each centre by a template search: the
    point in ±18 px that maximises the mean luminance of the white ring (26.5 to 28.5 px) minus that of the
    interior (within 18 px).
  - Each centre was re-checked on the fresh screenshot taken just before its tap.
- **Sequence.** Each case started with no bubble showing. Each bubble was closed with its own `Close`
  button, a real tap at the centre of that button's clickable bounds in the dump.

**(a) Direct tap: pass.**
- Dot centre (430,589) px. Its nearest other dots are about 145 px away.
- Before (`30-before-direct-tap.png`/`.xml`, 03:19:58): no bubble node; the ring was white at 26 to 28 px,
  with ground beyond it.
- Tap `input tap 430 589` at 03:20:03.
- After (`31-after-direct-tap.xml`, 03:20:05): the bubble reads `Onion Earthball`, `Scleroderma cepa`,
  `Sep 1, 2026`, `±5 m accuracy`, `View on iNaturalist`, with `Close` at `[756,342][801,387]`.
- **The ring on the tapped dot is blue** from 26 to 32 px: 12 to 14 of 16 samples per radius, for example
  `#3FA2F0`, `#3A9DEA`. `0x2196F3` at the ring's 0.85 opacity over a light ground predicts about `#40A3F2`.
- An untapped dot, (302,894), kept its white ring (0/16 blue).
- The bubble was judged from `31-crop-bubble.png` by eye, and the ring by the samples.

**(b) Near miss: pass.**
- Dot centre (302,894) px, over river-blue ground.
- Tap point (302,939): 45 px, which is 16.0 dp, straight below the centre, and 15.5 px outside the dot's
  drawn edge.
- A template scan of the box around the tap, widened by a dot's drawn radius (±97 px), found one disc: this
  dot, at (301,893). So nothing else drawn lay within the ±24 dp box.
- Before: `32-bubble-closed.png`/`.xml` (03:21:57): no bubble node.
- Tap `input tap 302 939` at 03:22:23.
- After (`33-after-near-miss.xml`, 03:22:25): the bubble reads `Inkcaps`, `Coprinopsis`, `Sep 17, 2021`,
  `Accuracy not reported`, `View on iNaturalist`, with `Close`. It is a different sighting from (a)'s.
- The bubble's translucent card covers the top of this dot. The uncovered lower arc (50° to 130°, at 27, 30
  and 32 px) went from white `#F1FAFC` at 27 px, with river `#A3DCE8` at 30 and 32 px before the tap, to
  **blue in all 15 samples** after it (`#2C88CD` to `#3196E2`). The 30 and 32 px samples are reached only by
  the widened selected ring.
- Which of the two query stages matched is not observable. That the point query missed rests on the tap
  being 15.5 px outside the drawn edge.

**(c) Far tap: pass.**
- Tap point (579,1736) px.
- A ±200 px window around it has no disc-like candidate. Its crop (`34-crop-fartap-region.png`) shows only
  basemap, judged by eye. The nearest overlay features, the reticle and the puck, are about 541 px away.
- Before: `34-bubble2-closed.png`/`.xml` (03:23:21): no bubble node.
- Tap `input tap 579 1736` at 03:24:04.
- After (`35-after-far-tap.png`/`.xml`, 03:24:06): no bubble node. The dump is byte-identical to the one
  before (sha256 `14e9b227…` for both), and the map area from y 300 to 1900 px is pixel-identical.
- **Limit, stated:** (c) alone would also pass with the box query removed. It is (b) that exercises the box.

**Flag from this check:** in both bubbles, the tail's tip lands about 150 px to the right of the tapped
dot, at its height. See Flags.

## Check 4: draw order on the Maps tab

- **Pass condition (pre-registered):** for each pair of the Maps tab's layers seen overlapping on screen,
  the one on top is the one higher in `MAP_LAYER_REGISTRY` (`ui/map/layers/MapLayers.kt:211-252`; added
  in that order, `ui/map/SightingsMap.kt:733-756`). The Maps tab draws sightings, planned trips, the
  breadcrumb, waypoints and the search centre (`ui/availability/AvailabilityCompactMapUi.kt:598-606`).
- **Prediction:** at least the breadcrumb part is not run.
- **Verdict: not run.** It was stopped at the abort condition before it finished. What had been seen by then:
  - **Sightings over sightings** overlap in several clusters, but that is one layer, not a pair.
  - **Reticle and sighting dots:** no dot overlaps the reticle. The nearest is 120 px above it.
  - **Reticle, waypoint and breadcrumb:** by the data these coincide within a few px at the reticle. But the
    waypoint pin is not drawn (the stop), and the 3.0 m breadcrumb is not seen. So no pair among them could
    be read.
  - **The ruled change** (dots and reticle over track lines) **cannot be seen on this phone**. The only track
    line is 3.0 m long, under the reticle and the puck, and no dot is within reach of it. The phone was
    stationary.
  - The recording was not mine. I did not start another, and I did not stop or discard this one.
  - **Planned trips:** none.

## Check 5: draw order on an entry map

- **Pass condition (pre-registered):** as check 4, for finds, photos, kept tracks and the offline circle's
  fill and outline on a Cartography entry map (`ui/log/CartographyEntryReportScreen.kt:355-382`).
- **Prediction:** not run.
- **Verdict: not run.** The missing data: any Cartography entry (`cartography_entries` 0 rows), and so any
  entry map with finds, photos, kept tracks or an offline region (`offline_regions` 0 rows). The dispatch
  allows no other way to create them.

## Check 6: defaults look as before (an observation, not a gate)

- **What the code sets (pre-registered):**
  - `visibility: visible` and every base opacity at state opacity 1 (`SightingsMap.kt:831-841`, `:753`).
  - Sighting fill 0.7 and ring 0.85, the offline fill 0.2, and 1 for every line and icon
    (`MapLayers.kt:145-158`, `:224`, `:243-246`).
  - The colours are `MapPalette.DAY` with Night Maps off (`ui/theme/MapPalette.kt:74-89`).
- **Prediction:** nothing looks faded or missing.
- **What was seen.** This is from the Topographical screenshot `35-after-far-tap.png`, with Night Maps off.
  - **Sighting dot fill: at 0.7.**
    - The near-missed dot sits over uniform river ground, `#A3DCE8`.
    - Where both its interior (at 22 px) and the ground beyond its ring (at 33 px) are river, the interior
      reads `#4F6063` at 15° to 90° and `#4E5F63` at 105°. `0x2B2B2B` at 0.7 over `#A3DCE8` predicts `#4F6064`.
    - The per-channel opacity estimate at 15° to 90° is 0.700, 0.701 and 0.704.
    - Had the 0.7 been applied twice (0.49), the interior would be `#68858B`.
  - **Sighting dot ring: at 0.85.** It reads `#F1FAFC` at 27 px, exactly the prediction for white at 0.85
    over `#A3DCE8`. The estimate is 0.848 to 0.870 across channels, from 8-bit rounding.
  - **The selected ring:** consistent with `0x2196F3` at 0.85 (check 3).
  - **The search-centre reticle: at opacity 1.**
    - Each of its four arms has pixels at exactly `#000000`: 50, 54, 40 and 50 of about 320 per window, on
      Topographical.
    - The counts are identical on Street (`22-street.png`), where a 200 x 200 px patch of Street ground
      has 0 exact-black pixels.
    - An icon opacity below 1 over a non-black ground could not render exact black.
  - **Missing: the ORIGIN waypoint pin.** This is the stop (see above).
  - **Not seen:** the breadcrumb, for a reason that may be expected (see Stop).
  - **Not present to judge:** planned trips, finds, photos, kept tracks and the offline fill and outline.
- **Verdict:** an observation, so not a pass. The dots and the reticle show nothing faded or restyled
  against the code's values. One overlay the data says is there is missing.

## Check 7: Night Maps leaves overlays alone

- **Pass condition (the dispatch's):** with offline tiles on an entry map, Night Maps off and on changes the
  basemap and no overlay's pixel colours.
- **Prediction (pre-registered):** not run.
- **Verdict: not run.** There is no entry map (0 Cartography entries) and no offline tiles (0 offline
  regions). Night Maps was not toggled.
- **The dispatch's pass condition conflicts with the code** (pre-registered, not resolved here).
  - The overlays are built in `MapPalette.forMode(nightMode)` (`ui/map/BasemapStyles.kt:207`, passed at
    `SightingsMap.kt:528-534`). That is `MapPalette.NIGHT` with Night Maps on (`ui/theme/MapPalette.kt:92-109`).
  - So by the code, every overlay's colour changes with the toggle. For example, the waypoint goes from
    `0xFF350560` to `0xFFB97DF7`, the sighting dot from `0xFF2B2B2B` to `0xFF8C8C8C`, and the casing from
    white to black.
  - What the code supports is narrower. The offline recolour runs before the overlays are added
    (`SightingsMap.kt:527`, `:1408`), so the overlays show the night palette's own values, not the V1
    transform of their day values.
  - Run as written, this check would fail on correct behaviour. It needs its pass condition restated before
    it is run.

---

## Settings

| Setting | Start (read) | Changes | End (read) |
|---|---|---|---|
| `settings system accelerometer_rotation` | `0` (03:10Z) | none | `0` (03:29Z) |
| `settings system user_rotation` | `0` (03:10Z) | none | `0` (03:29Z) |
| `settings system font_scale` | `1.0` (03:10Z) | none | `1.0` (03:29Z) |
| `settings secure ui_night_mode` (system dark mode) | `2`, dark (`cmd uimode night`: yes) | none | `2` (03:29Z) |
| Maps tab map mode (session state, `ui/availability/AvailabilityScreen.kt:828`) | Topographical (`11-menu-dismissed.xml`, 03:12:31) | Street 03:16:57, Satellite 03:18:01, Topographical 03:18:42 | **Topographical** (`50-final.xml`, 03:29:09: "Map mode: Topographical … Night mode off.") |
| Night Maps (`night_mode.maps`) | `false`: `map_preferences.preferences_pb` ends `08 00`; the dump reads "Night mode off" | none | `false`: the file is byte-identical to the start (sha256 `1974b87e…` both); the dump reads "Night mode off" |
| Map fullscreen (`map.fullscreen`) | `false` (same file); the dump shows the "Fullscreen" button | none | `false` (same file, identical); "Fullscreen" button in `50-final.xml` |
| Maps tab add menu (not a setting; recorded because I changed it) | open, over its scrim, at 03:11:43 | closed by me through its scrim at 03:12:29 | closed; not reopened |
| Track recording (not a setting; not mine) | running since about 02:23:46Z | none | running (`TrackRecordingService` foreground, 03:29Z) |

## App data during the run

- **Nothing was created, edited or deleted by this run.**
- Between the start and end copies, the database differs only in `track_points`, which went from 5 to 7.
  The two new rows belong to the open track (03:16:53Z and 03:16:59Z): the recording's own writes.
- Every other table's count is unchanged: `cartography_entries` 0, `offline_regions` 0, `planned_trips` 0,
  `waypoints` 1, `tracks` 1, `cached_searches` 1, `log_photos` 2, `mushroom_log_entries` 0. The cached
  search's `lastAccessedAtEpochMillis` is unchanged.
- `/sdcard/l0a-ui.xml`, written by `uiautomator dump`, was removed at the end.

## Evidence index

All files are in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-l0a/`. They show or hold real
locations and stay outside the repository.

| File | sha256 |
|---|---|
| `01-dumpsys-package-start.txt` | `8b8381b1aee5e5e702c646d18ad6a1416829e323c021f6252b2cf0a93667dd9c` |
| `02-crash-start.txt` (empty) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `10-app-foreground.png` | `bc6a7197e74fb2ae33540bcb7d33f1c71e7b8354fe7c9363438e0a3dcfbe25a8` |
| `10-app-foreground.xml` | `29438b7e544efff016e752ea4ee566d6d898a9c036c02a8ee3e4b04e6794023a` |
| `11-menu-dismissed.png` | `069374268447e03988a5d0f0893fb6ce66730c196c3e66c8f0d6d57eab83ca01` |
| `11-menu-dismissed.xml` | `acc21323c6463d76f5cb7eec71c308e3939286077ba1666394f2397d016024bd` |
| `11-crop-puck-x4.png` | `04b8f70c58189bfea1d4a83745fb26fb393e2e7e381174d3d36be259e876b33b` |
| `26-crop-puck-x2.png`, `35-crop-puck-x2.png` (identical) | `ae6346477683bfc13bf51e6fd9815518355991c09ae275d917dbdb783afe0f8e` |
| `20-before-picker.xml` | `dd1d995fdeb82f9a9842eceb1f3d168ec07fe013e73539a66f536a6e257835bc` |
| `21-picker-open.png` | `dee6cacbd8ecfa66e7036065d622f12e6e1b174ef51fa89bc135801995cb0b30` |
| `21-picker-open.xml` | `04a462be56e44613ca297461c1ad580271e8b10e9100411df2901922f19619b1` |
| `22-street.png` | `d612d68d08d9651f7d241f5e8c124f1879a454888a1b3dce335c4190cb49aee9` |
| `22-street.xml` | `220c1afb57b75d30a000960bb099ea89eb71a3894a5533434586d908ec150820` |
| `22-crop-puck-x4.png` | `1f7e01a4fb7b4253d817fd440ef4b5381fd9033abb72a451db4eba027f8e15db` |
| `23-picker-open-2.xml` | `5b240646392d2c59a19f963a71d64051bdc15138db5981ef8af88097248e2c4c` |
| `24-satellite.png` | `f63eb5bc5d7f45fc2c49b10c35411f516561fa3291015fdc226c86f2e66d1742` |
| `24-satellite.xml` | `6d17ee0d20320b096c4d0dabd84ed9ca522c5f142bdfdd4011ea3a66fa9e490c` |
| `25-picker-open-3.xml` | `dc11bd83178638e935f2dd22aee93a4f7532146fb0f8786fd84b5f7b60b7f9df` |
| `26-topo-restored.png` | `e6a0ebd0f91f4a1cb4029e42b5d6a600c6130127a7555ba2ab0556e84b03da50` |
| `26-topo-restored.xml`, `30-before-direct-tap.xml` (identical) | `a01cb8069237d8e6ed553763259c531d3c242f6ac41ee89c284a5d3ff376a0c9` |
| `30-before-direct-tap.png` | `b947e229588ae5d71fe671bfec321d41c66449eee48ebcdb5a9e963c6a9eeb33` |
| `31-after-direct-tap.png` | `d2b0f885686dbf4ac60173804b9420fa4615807fd34de6c374f3a3b90a447e33` |
| `31-after-direct-tap.xml` | `251dfe3ca6a9d521d30ce3179d38a3da5664f41821d16bbfabcf7e4e476a7a0d` |
| `31-crop-bubble.png` | `776b270c2a82a42cd4e9fc74a0af3b70b8bdee83429d88d163c44b7903764333` |
| `31-crop-tail-x4.png` | `645650520acf9a22ac9991224a6570d6de85eaa628814c0667eabe3cf055a23d` |
| `32-bubble-closed.png` | `a16d453813190c7ab72b2a6dcd44ca51c655930d27b78f5182ba0e9f40f8a81d` |
| `32-bubble-closed.xml` | `ee7322394f55d6caffde141f4209724a5807aa04a7267e827c34347e5d93488f` |
| `33-after-near-miss.png` | `c8d2e6ff28194764cf39201e2ba028e78697a7cb90dc4b8ebcb0238dacb516cc` |
| `33-after-near-miss.xml` | `9df69cc395c565d0d70b7beac631f44ee334b3c08347a197d7fc970d1b0ee111` |
| `33-crop-bubble.png` | `98820154e1b5cf51ca74b6877a5d3e33f9ff15921b17b0506afc32c5e569d912` |
| `34-bubble2-closed.png` | `7b2efd1965bdc020fcbe63df0afa4c46e2628776bc36268af98c680f606ecd4b` |
| `34-bubble2-closed.xml`, `35-after-far-tap.xml` (identical) | `14e9b227cbb1f5e56fe6537947ec7b66e97ea90a4c4e9d0b68585586b784fa54` |
| `34-crop-fartap-region.png` | `05eb7bc107697c70b7be9e2933f4af439e993173a3a3453973dc8a94060a92a6` |
| `35-after-far-tap.png` | `dd9dc59e15efab7f597243e5c0f262ac09fc35f3c24e0789e5d59856426cb956` |
| `40-logcat-app-pid.txt` | `13016c964bddf5a156274b7696cbd77123db8b6053e0996c446c83c5607ea992` |
| `50-final.xml` | `d49da4465fed0721e77000ae9852d2148190654c46741dcbb824bf554471cda6` |
| `90-crash-end.txt` (empty) | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `91-dumpsys-package-end.txt` | `1f8dad2a920fe8be0ebd77e884da042992afe5b58e45819595f416ac11cc5a51` |
| `db-raw/forager.db` (start; unchanged at the end) | `94772575633b6f43be37fdc3bc98e81d25f0b53e7098b5f21d547a0f415a894c` |
| `db-raw/forager.db-wal` (start) | `b610981cf396c44387fa2532b18f41430382fb61608619c382a86ecfadaa170f` |
| `db-raw/forager.db-shm` (start) | `1daebff8a19185f81bb4c82d3052b5c8ac528428883bfb1ef5e1d05dc260bd93` |
| `db-end-raw/forager.db-wal` (end) | `61e82d8aa7a28296b323ec2b4a357176b3d8d5eca09b25d64ee946955a3c65d5` |
| `prefs-start/map_preferences.preferences_pb`, `prefs-end/…` (identical) | `1974b87e8fa152a27d23ef36932d97970a602f5d6c2799190194247fbeb6cecc` |

`db-query-copy/` and `db-end-query/` are working copies that `sqlite3` checkpointed, so their hashes are not
evidence. `snaps.log` records each capture's UTC window and hash.

---

## Decisions I made

1. **Proceeded without the kit's structural validation.** My agent definition validates a dispatch against
   `.claude/kit.json`'s `required_sections`. That file does not exist at the base (`f18af53`); owner commit
   `e136330` removed it, and it is an ancestor. Measured against `main`'s `kit.json` (`faf2f88`), this
   dispatch does not use the device section names "Base and state", "Scope boundary", "Closed decisions",
   "Prediction", "Finish line and abort conditions", "Checks", "Out of scope" or "Device items", though it
   has sections covering most of them. I took the dispatch's statement that the kit does not apply at this
   base, after verifying it. To decide this properly: a ruling on whether the required-sections check binds
   dispatches on `journal-redesign`.
2. **Read the database through a byte copy on the host.** The dispatch named `sqlite3` on the phone (absent)
   or the app's UI. A `run-as cat` copy is a read, but it is a third method, and it puts a full copy of the
   app's data, real locations included, in the evidence directory.
3. **Left the running recording alone, and used no recording of my own.** The dispatch allowed one recording,
   started and discarded by me. It did not foresee one already running. Stopping it could discard someone
   else's track. Who started it, and whether it may be stopped, is for the owner.
4. **Closed the add menu that was open on arrival**, through its scrim, and did not reopen it at the end.
5. **Chose the observables for check 3:** the selected ring as the tie between a bubble and its dot; closing
   each bubble with its own `Close` button rather than a map tap; and which dots and tap points to use.
6. **Judged two anomalies not to meet the abort condition** (the puck's order, the bubble's tail; see Flags),
   after `git show` found no L0a commit touching the lines involved. That was a judgment on the abort
   condition. Both are reported.
7. **Judged the missing waypoint pin to meet the abort condition, and stopped the checks.** Before stopping,
   I read `GetWaypointsUseCase` and the callers of `loadWaypoints`, to be sure the expectation was the code's.
   That is close to the line "do not look for the cause"; I did no more than that. After the stop I still
   did the end-of-run reads (settings, crash log, package, database counts), because the finish line needs
   them. No further check was run.
8. **Wrote the pass conditions and predictions into a pushed commit (`3a87422`) before running any of
   checks 2 to 7**, so their order is checkable.
9. **Treated species names and observation dates in bubbles as not place names**, and so recorded them.
10. **Removed `/sdcard/l0a-ui.xml`**, the dump file my run created on shared storage, not the app's.
11. **Kept the database copies and the app log in the evidence directory.** They hold real locations; whether
    to keep them is the owner's call.

## Flags outside scope

1. **The location puck's order against the overlays changes after a style reload.**
   - At arrival on Topographical, the puck's halo and blue dot drew over the reticle (`11-crop-puck-x4.png`).
   - After switching to Street, the reticle's crosshair drew over the puck's blue dot (`22-crop-puck-x4.png`).
   - After returning to Topographical, the reticle still drew over the puck (`26-crop-puck-x2.png`, which is
     identical to `35-crop-puck-x2.png`; judged by eye). So from the first style reload on, the overlays
     drew over the puck, on either basemap.
   - No L0a commit adds or removes a line calling `activateLiveLocationIfPermitted`, `setStyle` or the
     `LocationComponent` (`git show 193400f 337e5b5 e3591aa b0ce1be`). Not investigated.
2. **The bubble's tail does not point at the tapped dot.** In both bubbles, the tail's tip lands about 150 px
   right of the dot, at its height, pointing down-right (`31-crop-tail-x4.png`, `33-crop-bubble.png`). The
   anchor `SightingsMap` passes, `Offset(screenPoint.x, screenPoint.y)`, is textually identical before and
   after `e3591aa`, and no L0a commit touches `ui/availability/`. Not investigated.
3. **A steady stream of MapLibre errors.** The app's process logged 5,645
   `E/Mbgl-NativeMapView: You're calling getMetersPerPixelAtLatitude after the MapView was destroyed`
   between 02:50 and 03:27Z, about 2.5 a second. They fill the log buffer, which is why it reaches back only
   to 02:50Z (`40-logcat-app-pid.txt`).
4. **Someone was using the phone before this run.** A recording was started at about 02:23:46Z and was still
   running, and the add menu had been left open. Neither is in the dispatch. Unknown who.
5. **The breadcrumb, 3.0 m long, is not visible** at the Maps tab's zoom. It may be simplified away by the
   source or hidden under the reticle and the puck. Unverified.

## Needs a decision

1. **The stop.** The ORIGIN waypoint is not drawn on the Maps tab. Whether this is L0a, or older, or a state
   of this session, needs an investigation this dispatch forbids. One cheap discriminator: whether the pin
   draws on this phone on a build from before `e3591aa`. That needs an install, which this dispatch forbids.
2. **Check 7's pass condition** needs restating before it is run (see check 7).
3. **The running recording:** leave it, or have someone stop it.
4. **Checks 5 and 7, and check 2's offline part, need a Cartography entry** with finds, photos, a kept track
   and an offline region on this phone, and none exists. This dispatch may not create one.

---

# Continuation 2026-09-28-06

**Status: pre-registration.** This section is committed and pushed before step 1 is run. The results
follow in later commits. The pass conditions and predictions below are not edited after this commit,
except where a correction is marked.

**Dispatch:** `prompts/preserved/2026-09-28-06.md`, continuation `2026-09-28-06` of intent `2026-09-28-02`
(both the planner's, in `RECORD.md` at `bc64d37`).
**Base:** `origin/journal-redesign` at `bc64d37`, merged into this branch as a fast-forward from `b1e08da`.
`git diff --stat 2458934 bc64d37 -- app/` is empty, so the app tree is still the one in the installed build.
**Device:** SM-S908U, serial `R5CT321008R`, `ro.build.id=BP2A.250605.031.A3`, the only device attached
at 03:57:34Z. `wm density` 450 (1 dp = 2.8125 px), `wm size` 1080 x 2316.
**Clock:** the machine and the phone read UTC; the phone's local time is UTC-7, so its local date is
2026-09-27 until 07:00Z. The app's "today" is `LocalDate.now()` (`ui/log/CartographyViewModel.kt:115`,
`ui/log/MushroomLogViewModel.kt:323`), so today's Journal day in the app is 2026-09-27 until then.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-l0a/`, numbered from 60 on. Nothing
identifying goes into this file.

Paths below are under `app/src/main/java/com/zynergylabs/forager/app/`; lines are at `bc64d37`.

## Outcome (updated at the end of the continuation, 04:52Z; added here, above the pre-registration, which is unchanged)

**Status: complete.** Every check has a verdict. The run was **resumed after a machine restart** at 04:35Z (see "Resumed after
a machine restart" under Results). The phone itself did not restart.

| Check | What | Verdict |
|---|---|---|
| 1 | Build and baseline | **pass.** Build `1.0.1192+g24589349` at 03:08Z, 04:01Z, 04:35Z (after the restart) and 04:52Z. The crash buffer was empty at each read. **Caveat:** I cleared it with `logcat -c` at 04:44:28Z, so the end read covers only 04:44:28Z onwards. The gap is covered by an unchanged PID only (see Settings (continuation)) |
| 2 | Attribution caption text | **pass.** The Maps tab: Street, Topographical and Satellite (first run). The entry map: offline on, `Protomaps © OpenStreetMap`; offline off, the Topographical credit (step 8) |
| 3 | Sighting bubble: direct tap, near miss, far tap | **pass** on all three (first run) |
| 4 | Draw order on the Maps tab | **pass** for waypoint over search centre, the one pair that overlaps (step 6). **Not run:** waypoint and sighting dot, and dots and reticle (no overlap); the ruled change, dots and reticle over track lines (no line on the Maps tab after the stop) |
| 5 | Draw order on the entry map | **pass** for every pair seen overlapping (step 7): fill < outline; fill < track; fill < markers; track casing < track; track < waypoints; waypoints < finds; finds < photos. At the framing (zoom 13 by code) and at zoom 17 (measured). **Not run:** the outline against the track or any marker, which never meet |
| 6 | Defaults look as before (an observation) | First run: sighting dots and reticle at the code's opacities. Continuation: on the entry map, every overlay is drawn, icons read their exact hex, and line widths, dashes and fill opacity match the code (step 9). Nothing faded or restyled. Not a pass: nothing earlier to compare with |
| 7 | Night Maps, restated by `2026-09-28-06` | **pass** (step 10). With night on, the offline basemap is recoloured (57 properties logged; ground `#B7B4B0` → `#504D49`, the V1 value within 1). Each overlay reads its DAY hex with night off and its NIGHT hex with night on: waypoint, find and photo glyphs, the kept-track line, the casing. The V1-of-DAY value never appears. Restored and read back |
| 8 | The positive control: an ordinary waypoint draws on the Maps tab | **pass** (step 5): `#350560`, 1,853 px exact, head centred 17 dp above the placed point |
| Step 2 | The Return leg, while recording | **pass:** not active, which accounts for the hidden ORIGIN |
| — | Hidden layers excluded from queries; state-driven visibility and opacity | **not runnable at this build**; moved to L0b. Not attempted |
| — | `featureId` from the hit test; offline-circle fall-through | **not runnable at this build**; moved to M1. Not attempted |

- **The first run's stop** (the ORIGIN pin not drawn) is resolved as the dispatch says: the filter is by design, and the
  positive control passes.
- **Planner's predictions for `2026-09-28-06`:** 1 held (step 2). 2 held (check 8). 3 held (check 5). 4 held (step 8). 5 held (check 7).
- **Test data:** all created as authorised, and left on the phone (see "Created data"). The stop in step 3 also created an
  END waypoint by itself, as the pre-registration predicted from the code.
- **Settings:** restored and read back (see "Settings (continuation)"). The one change not undone is `logcat -c`, which
  cannot be undone.

## Pre-registration

### Step 1: baseline
- **Pass condition:** `dumpsys package` shows `versionName=1.0.1192+g24589349`; `logcat -b crash -d` is read
  now and at the end, and a Forager line that is not in the start read is a stop.
- **Prediction:** the same build, and an empty crash buffer.

### Step 2: the Return leg, while the recording is still open
- **From the code:**
  - `isNavigating = isReturning` (`ui/availability/AvailabilityScreen.kt:775`).
  - The compass strip is composed only while `!isNavigating` (`ui/availability/AvailabilityCompactMapUi.kt:1001`).
  - The navigation HUD is composed only while `isNavigating` (`:1071`). Its exit icon carries
    `contentDescription = "Stop navigating"` (`ui/availability/NavigationHud.kt:272`).
- **Pass condition (Return not active):** on the Maps tab, with nothing changed:
  - a fresh dump has no node described "Stop navigating";
  - the dump has the compass strip's heading text (a `°` reading or "Compass unavailable");
  - the screenshot shows the strip under the search bar and no HUD panel.
- **Stop:** if "Stop navigating" is present, the Return leg is active. I stop and report. I do not turn Return on or off.
- **Prediction:** not active.

### Step 3: stopping the owner's recording
- **From the code:**
  - The map's record control reads "Stop recording track" while recording (`ui/availability/AvailabilityMapControlsUi.kt:204`).
  - Tapping it calls `trackRecordingViewModel.stopRecording()` directly (`MainActivity.kt:513-515`). No
    dialog sits in between, and I found no keep-or-discard choice anywhere on this path.
  - The service flushes the buffer and calls `endTrackUseCase`, which only sets `endedAt`
    (`service/TrackRecordingService.kt:142-157`, `domain/EndTrackUseCase.kt:8-9`). So the stop keeps the track.
  - **The stop also creates data by itself.** `stopRecording()` saves an END-designated waypoint from the last
    gated fix, if there is one (`ui/track/TrackRecordingViewModel.kt:289-306`). `mapVisibleWaypoints` never
    draws it on the Maps tab (`ui/availability/AvailabilityPureFunctions.kt:65-66`).
- **The dispatch's premise:** it says "its keep choice". By the code, there is no choice, and the stop keeps.
  The abort condition "the stop offering only discard" is therefore not expected. The same goes for any choice
  offering discard, which I would not take.
- **Prediction:**
  - no dialog;
  - the control changes to "Start recording track";
  - the track row gets `endedAt`;
  - one END waypoint row is added.

### Step 4: creating the test data (actions, not checks)
- **(a) Waypoint:**
  - Maps tab add menu, then "Waypoint" (`ui/availability/AvailabilityMapControlsUi.kt:587`).
  - The centre-pin picker places it at the camera centre (`ui/availability/AvailabilityCompactMapUi.kt:1252-1258`).
  - Then "Name this waypoint", confirmed with "Drop waypoint" (`ui/availability/AvailabilityMapOverlaysUi.kt:184`, `:197`).
  - Placement: the pin's tip on the search-centre reticle, so the pin overlaps it (step 5, step 6).
- **(b) Finds:**
  - Maps tab add menu, then "Find"; the centre pin sets the find's location (`ui/availability/AvailabilityCompactMapUi.kt:1256`).
  - The find form's Save commits the draft (`ui/log/MushroomLogViewModel.kt:497-523`).
  - Find 1 gets one photo from the app's own camera. A camera capture takes its coordinate from a live fix,
    not from EXIF (`photo/FilePhotoStore.kt:133-137`).
  - Placement: close to the waypoint, but offset so that each glyph is partly visible.
- **(c) Track:** the one kept in step 3.
- **(d) Offline region:**
  - The picker opens on the device's current fix (`ui/availability/AvailabilityViewModel.kt:777-784`).
  - The radius slider's minimum is `Region.MIN_RADIUS_KM` = 1 (`ui/availability/AvailabilityOfflineMapsUi.kt:212-218`,
    `domain/model/Region.kt:10`). The form has a "Name (optional)" field (`:206`).
  - A successful download also saves the picked region as the picker's remembered last region
    (`ui/availability/AvailabilityViewModel.kt:1130`), a `map_preferences` write that I cannot undo through the UI.
    It is recorded under Settings.
- **(e) Journal entry:**
  - Starting an entry keeps every one of the day's initial candidates: finds, tracks, waypoints and offline
    regions (`ui/log/CartographyViewModel.kt:115-146`). Photos are kept one by one (`:310`).
  - "Finish entry" commits the draft (`:322-337`; `ui/log/CartographyEntryEditScreen.kt:285`).
  - The form has no title field, only "Your own account (optional)" and "Tags" (`ui/log/CartographyEntryEditScreen.kt:248`, `:260`).
  - The entry map draws only the entry's kept items, and only photos attached to the entry itself
    (`domain/GetCartographyEntryMapDataUseCase.kt`, `entry.photos`).

### Step 5: check 8, the positive control (an ordinary waypoint draws on the Maps tab)
- **From the code:**
  - A waypoint with `designation == null` passes `mapVisibleWaypoints` (`ui/availability/AvailabilityPureFunctions.kt:66`),
    reaches the compact map as `mapWaypoints` (`ui/availability/AvailabilityScreen.kt:778-779`, `:1392`;
    `ui/availability/AvailabilityCompactScaffold.kt:822`), and is drawn on `waypoints-layer` (`ui/map/layers/MapLayers.kt:249`).
  - The glyph is a teardrop 22 x 28 dp, anchored at its tip on the coordinate (`ui/map/MarkerGlyphs.kt:36`). Its head
    is a circle of radius 11 dp, whose centre is 17 dp above the tip (`:91-92`), and it carries a ring in the casing
    colour of radius 2 to 3.5 dp (`:103`).
  - Colours: fill `#350560` with a white casing when Night Maps is off, and `#B97DF7` with a black casing when it is
    on (`ui/theme/MapPalette.kt:75`, `:89`, `:93`, `:106`; chosen by `MapPalette.forMode`, `:109`).
- **Pass condition:** a pin of that shape whose tip is at the point the app placed it. Night Maps' state is read
  from Settings. The fill pixels in its head, between the ring and the casing, read that state's hex exactly or
  within 2 per channel.
- **Fail and stop:** no such pin.
- **Prediction:** drawn, in `#350560` (Night Maps was off at the end of the first run).

### Step 6: check 4, draw order on the Maps tab
- **Pass condition (the first run's):** every pair of layers seen overlapping on screen stacks as
  `MAP_LAYER_REGISTRY` orders them (`ui/map/layers/MapLayers.kt:211-252`, bottom to top). On the Maps tab, the relevant
  order is breadcrumb < search centre < sighting dots < planned trips < waypoints.
- **The test:** in an overlap, the pixels show the upper layer's own colour. For the waypoint over the reticle, the
  pin's fill covers the reticle's black arm and ring where the two meet.
- **Not run:**
  - a pair that never overlaps on screen.
  - The ruled change (dots and reticle above track lines) needs a line on the Maps tab. The breadcrumb ends with
    the stop in step 3, and kept tracks are not drawn there at this build (`ui/availability/AvailabilityCompactMapUi.kt:598-606`).
    So that pair is expected to be not run.
- **Prediction:** waypoint over reticle, as the registry says. The ruled-change pair is not run.

### Step 7: check 5, draw order on the entry map
- **Pass condition:** every overlapping pair on the entry map stacks as the registry orders them. The relevant order
  is offline fill < offline outline < kept-track casing < kept track < waypoints < finds < photos
  (`ui/map/layers/MapLayers.kt:212-251`; the entry map's overlays are at `ui/log/CartographyEntryReportScreen.kt:355-386`).
  The same pixel test as step 6 is used. A pair that never overlaps is not run.
- **The zoom:**
  - The map frames the entry's points with `GeoDistance.boundingRegion`, whose radius is at least 1 km
    (`domain/GeoDistance.kt:132-141`). For a radius under 5 km, `zoomForRadiusKm` gives 13 (`ui/map/SightingsMap.kt:586`, `:1507-1512`).
  - Each double-tap zooms in by one level (the MapLibre default; the zoom is inferred, not read).
  - I will say which was used.
- **Prediction:**
  - at least one marker pair overlaps and stacks as the registry says;
  - the markers draw over the offline fill;
  - the outline may never meet a marker, because the circle's centre is at the markers, so that pair may be not run.

### Step 8: check 2, offline part
- **Pass condition:**
  - on the entry map with offline tiles on, the caption reads exactly `Protomaps © OpenStreetMap`
    (`map/OfflineStyle.kt:28`, chosen by `ui/map/BasemapStyles.kt:130-131`, drawn at `ui/map/SightingsMap.kt:681`), with
    no ` · `, because no registry layer has a credit;
  - with offline off, the caption reads the entry map's basemap credit (Topographical by default, `ui/map/MapMode.kt:36`).
- **Also from the code:** the offline control appears only when a kept region covers at least one of the entry's
  points (`ui/log/CartographyEntryReportScreen.kt:437-458`, `domain/GetCartographyEntryOfflineRegionUseCase.kt`).
- **Prediction:** a match.

### Step 9: check 6, an observation on the entry map
- **What the code sets:** `visibility: visible`, the offline fill at 0.2, and lines and icons at 1
  (`ui/map/layers/MapLayers.kt:145-149`, `:224`). The line widths are: kept track 6 dp with a 9 dp casing, and the
  offline outline 1.5 dp dashed 6/4 dp (`ui/map/SightingsMap.kt:1374`, `:1478-1483`).
- **The observation:** whether any overlay looks faded, missing or restyled against those values. Icon interiors
  should read their role's hex exactly. This is not a pass.

### Step 10: check 7, restated by the dispatch
- **From the code:**
  - Overlays take `MapPalette.forMode(nightMode)` (`ui/map/BasemapStyles.kt:207`).
  - The offline style is recoloured before the overlays are added, only when offline tiles and night are both on
    (`ui/map/SightingsMap.kt:527`). The recolour logs `Offline night: N colour properties recoloured, M left in day colours.` (`:1442`).
  - Its transform is V1: `night = clamp(c + 255 - 2 * mean(r, g, b))` per channel (`ui/map/NightColour.kt:39-47`).
- **Pass condition:** on the entry map with offline tiles on, Night Maps off and then on, both of the following:
  - **Basemap:** night on logs the recolour line with N > 0. A flat ground sample moves from its day value to that
    value's V1 transform, within 2 per channel.
  - **Overlays:** each sampled overlay pixel reads its role's DAY hex with night off and its NIGHT hex with night
    on, within 2 per channel. At least two glyphs and one line or outline are sampled.
    - Waypoint `#350560` / `#B97DF7`, find `#DA02AF` / `#F96FAC`, photo `#C1154F` / `#E8046D`, kept track
      `#A122F8` / `#EEA7FE`, outline (casing) `#FFFFFF` / `#000000` (`ui/theme/MapPalette.kt:74-107`).
    - **The discriminator:** an overlay passed through V1 would read differently. For example, the waypoint's
      `#350560` becomes `#CD9DF8` under V1, not `#B97DF7`.
- **Restore:** Night Maps goes back to its starting value, and I read it back.
- **Prediction:** pass.

### Planner's predictions, to be scored
1. Step 2 passes. 2. The ordinary waypoint draws. 3. At least one overlapping pair on the entry map stacks as the
registry says. 4. The offline caption matches the code. 5. The restated check 7 passes.

## Results (written as the run goes; the Outcome table comes last)

### Step 1: baseline: **pass**
- `60-dumpsys-package-start.txt` (04:01:16Z): `versionCode=1192`, `versionName=1.0.1192+g24589349`,
  `lastUpdateTime=2026-09-27 19:18:29`. This is the same as the first run.
- `61-crash-start.txt` (04:01:16Z): empty.
- The screen was awake, with no keyguard (`isKeyguardShowing=false`). `MainActivity` had focus. The process was
  PID 19584, the same as in the first run. `TrackRecordingService` had been in the foreground for 1 h 37 min.

### Step 2: the Return leg: **pass** (not active)
- `62-step2-maps-recording.png` / `.xml` (04:01:58Z), on the Maps tab, with nothing touched before them.
- The dump has **no** "Stop navigating" node. It has the strip's heading text `267° W`, the "Stop recording
  track" control, and a return control described with a bearing and a distance.
- By my reading of the screenshot, the strip sits under the search bar, and no HUD panel is on screen.
- The Return leg is not active, which accounts for the hidden ORIGIN (`ui/availability/AvailabilityPureFunctions.kt:65-66`).
  The pin was not drawn in this screenshot either.

### Step 3: the owner's recording, stopped and kept: **done, as predicted**
- **The tap:** a real tap at (990,1498) at 04:02:25Z, the centre of the "Stop recording track" node `[956,1471][1024,1525]`.
- **After (`63-after-stop.png` / `.xml`, 04:02:28Z):**
  - no dialog and no snackbar;
  - the control reads "Start recording track";
  - the return control reads "Return to vehicle — start recording first";
  - `TrackRecordingService` is no longer listed.
- **No keep choice was offered, because none exists** (see the pre-registration). The stop kept the track.
- **The database copy after the stop (`db-c2-afterstop-raw/`):**
  - the track has `endedAt` set, 5918.9 s after its start;
  - `track_points` has 23 rows;
  - **one END waypoint was added** by the stop, "End · Sep 27, 9:02 PM", linked to the track;
  - the ORIGIN waypoint is unchanged.
- **What the app shows** (Journal, Records):
  - The Tracks chip read 0 until it was opened, because it reloads only on opening (`ui/log/RecordsTab.kt:189`).
  - After opening, the row (`66-records-tracks.xml`) reads "Sep 27, 2026, 7:23 PM", "18 points".
  - Its details sheet (`67-track-details.xml`) reads: Started Sep 27, 2026, 7:23 PM; Ended Sep 27, 2026,
    9:02 PM; Distance 379 ft; **Duration 1h 27m**; **Points 18**. See Flags.
  - The sheet was closed with Back.

### Step 4(a): the waypoint "DEVICE CHECK waypoint": **created**
- **The path:**
  - Maps tab add button (tap 990,1325), then "Waypoint" (898,1326): `71-add-menu`, `72-waypoint-picker`.
  - The centre-pin picker was confirmed with OK (286,1865) **without panning**. The confirmed point is `cameraCenter`.
  - The MapView spans [0,75][1080,2316], so its centre is (540,1195.5), where the reticle has been drawn since
    the first run. The picker's own pin tip sits about 6.7 dp below that centre (`ui/map/CentrePinLocationPicker.kt:261`),
    and by my reading of `72-crop-picker-x2.png` it lies just below the reticle's centre.
  - The dialog's default name "Waypoint 1" was cleared and "DEVICE CHECK waypoint" typed. The field read back
    exactly that (`74-waypoint-named.xml`). "Drop waypoint" was tapped at 04:06:55Z.
- **The first two frames after it are dimmed** (`75-waypoint-dropped.png` at 04:06:57Z, `76-check8-pin.png` at 04:07:58Z,
  `77-maps-recheck.png` at 04:10:14Z):
  - The map area, and the caption over it, read 0.839 times their values in `63-after-stop.png` (median over 3,833
    ground samples). The white casing reads `#D6D6D6`, and the pin's fill `#2C0451` (= `#350560` x 0.839).
  - The bottom nav's selected pill and its text are unchanged (`#6B421A`, `#CFC9BE`), and so is the status bar.
    So the dim lies over the map and under the app's chrome.
  - It is not a dim layer in the window manager (both SurfaceFlinger dim layers report "hidden") and not the screen
    timeout (600 s).
  - It cleared on switching to Journal and back (`78-maps-after-tab-roundtrip.png`, 04:10:31Z, ratio 1.000).
  - **No colour below is read from a dimmed frame.** The cause was not looked for (see Flags).

### Step 5: check 8, the positive control: **pass**
- **Evidence:** `78-maps-after-tab-roundtrip.png` / `.xml` (04:10:31Z), with Night Maps off (the map-mode description
  reads "Night mode off"; `night_mode.maps` is `false` in `prefs-c2-start/`). The crop is `78-crop-pin-x4.png`.
- **The pin is drawn:**
  - Within 2 per channel of `#350560`: **2,003 pixels, 1,853 of them exact**. They span x 509 to 569 px (60 px,
    about 21 dp, where the glyph's head is 22 dp) and y 1118 to 1166 px.
  - Its casing and the ring on its head read `#FFFFFF`, the DAY casing.
  - No pixel is within 12 of the night hex `#B97DF7`.
  - Before the waypoint existed, `62` and `63` had 0 pixels within 12 of `#350560` in the map area.
- **Where:** the head's centre is at about (539,1147) px. That is 17 dp (47.8 px) above (540,1195), the reticle's
  centre and the camera centre the picker confirmed, which is where the glyph puts the head over its tip
  (`ui/map/MarkerGlyphs.kt:36`, `:91-92`).
- **What I could not see:** the tip itself. It is under the location puck, a `#4A90E2` dot with a white halo that draws
  over the pin's lower triangle and the reticle's centre. See check 4.
- So the ordinary waypoint draws, and ORIGIN's absence is the filter, not the layer.

### Step 6: check 4, draw order on the Maps tab: **pass** for the one pair that overlaps; the rest **not run**
- **Waypoint over search centre: stacks as the registry says** (`waypoints-layer` above `search-center-layer`,
  `ui/map/layers/MapLayers.kt:231`, `:249`).
  - In `63-after-stop.png`, before the waypoint, the reticle's upper arm at x 538 to 542 px is exactly `#000000`
    from y 1160 to 1170, and its ring is black at y 1172 to 1176.
  - In `78`, those same pixels are the pin's fill, `#33055C` to `#2C0450`. So the pin covers the arm and the ring's
    top, and nothing of the reticle shows through it.
  - The fill darkens slightly towards the puck, from `#35055F` at y 1158 to `#2C0450` at y 1170. I read that as the
    puck's shadow lying over the pin; **unverified**.
- **Waypoint and sighting dot: not run.** The nearest dot's ring ends at about y 1102 px, and the pin's casing begins at
  1114, with basemap between. They do not overlap.
- **Sighting dots and reticle: not run.** No dot overlaps the reticle.
- **The ruled change (dots and reticle above track lines): not run.** The Maps tab has no line after the stop:
  - the breadcrumb ended with the recording;
  - kept tracks are not drawn on the Maps tab at this build (`ui/availability/AvailabilityCompactMapUi.kt:598-606`);
  - there are no planned trips.
- **Not a registry layer, recorded for the first run's flag 1: the location puck.**
  - In `63` (before any tab change this run), the reticle's arm shows at the top of the puck's halo (y 1178, `#000000`),
    so the reticle drew over the puck.
  - In `78` (after the Journal round trip), the halo is white there, and the puck's blue dot covers the reticle's
    centre and the pin's tip. So the puck drew over both.
  - The order changed with the tab round trip.

### Resumed after a machine restart (04:35Z)
- **What happened.** The previous coder on this continuation was stopped by a restart of the machine,
  not of the phone. Its last recorded result is step 6 above (`4b368ab`). Its evidence runs on to
  `112-offline-download-9.xml` (04:23:11Z, `snaps.log`), so it had gone further than it recorded.
  This part is written by the coder who resumed the run.
- **The phone did not restart.** Uptime was 542,553 s (about 6.3 days) at 04:35:04Z. `MainActivity` still
  had focus, in the same process (PID 19584) as both earlier runs. There was no keyguard, and the screen was awake.
- **The new baseline:**
  - `120-dumpsys-package-resume.txt` (04:35:38Z): `versionName=1.0.1192+g24589349`, `versionCode=1192`,
    `lastUpdateTime=2026-09-27 19:18:29`, as before.
  - `121-crash-resume.txt` (04:35:38Z): empty.
- **Lost on the machine side.** `112-offline-download-6.xml` to `-9.xml` are 0 bytes on disk, although `snaps.log`
  lists each with the same non-empty sha256 as `-1` to `-5` (`e131a139…`). I read that as writes lost in the
  restart before they were flushed; **unverified**. The first five, and the fresh `122-resume-screen.xml`, hash to that
  same value, so nothing on screen changed in that time. From here on, the snapshot helper runs `sync` before
  it logs a hash.
- **The base.** The branch was level with `origin/device-l0a-2026-09-28` at `4b368ab`. `origin/journal-redesign`
  is now at `f7e219a`, which carries L0b app code (`fe07702`). I did **not** merge it, so that the app tree in this
  branch, and the line citations above (at `bc64d37`), stay those of the installed build. See Decisions.
- **The inventory before creating anything** (`db-c3-resume-raw/`, read with `run-as cat`; the UI in `122`, `123`):
  - The two finds were already saved, and find 1 already had its photo. The offline region had already been
    downloaded. They are recorded below as 4(b) and 4(d), from the previous coder's evidence and this read. None was
    created a second time.
  - Rows since the step 3 read (`db-c2-afterstop-raw/`): `mushroom_log_entries` 0 → 2, `log_photos` 2 → 3,
    `log_entry_photos` 0 → 1, `offline_regions` 0 → 1, and `waypoints` 2 → 3 (the step 4(a) waypoint).
  - `cartography_entries` is still 0, so step 4(e) had not been started. `track_points` is still 23, and `tracks` 1.
  - No partial or duplicate item was found. `files/captures/` is empty, and `files/photos/` holds three files:
    the two from before the first run and the new one. The photo was listed, not pulled.

### Step 4(b): the two finds: **created** (by the previous coder; recorded on resume)
- **Find 1, "DEVICE CHECK find 1":**
  - The path: Maps add menu, then "Find"; the picker (`81-find-picker.xml`, 04:12:55Z); the form (`82-find-form.xml`).
  - "DEVICE CHECK find 1" went into both "Your own identification (optional)" and "Description Notes" (`91-find1-ready.xml`).
    **The species or placeholder** is that text, typed as the own identification. No species was picked.
  - **The photo:** the form's "Camera" opened the app's camera (`92-camera-open.xml`). One shutter gave "1 photo taken"
    (`93-after-shutter.xml`). The form then showed a "Log photo" thumbnail (`94-find1-with-photo.xml`).
  - "Save" was tapped. The saved view reads "Your own identification: DEVICE CHECK find 1", with the photo, and the
    Finds chip went from 0 to 1 (`96-find1-saved.xml`, 04:17:52Z).
  - The database has the row with `isDraft=0`, and one `log_entry_photos` link to the new `log_photos` row.
    `syncStateKind` is `DRAFT`, which is the iNaturalist upload state, not a draft entry.
- **Find 2, "DEVICE CHECK find 2":** the same path, with the picker panned before OK (`100-find2-panned.png`), and no photo.
  Saved at 04:19:50Z (`105-find2-saved.xml`, Finds chip 2). `isDraft=0`.
- **Where they sit, as offsets from the waypoint (no coordinates):**
  - find 1 is on the waypoint's own point (0.0 m);
  - find 2 is 135 m east;
  - find 1's photo is 0.7 m from the waypoint, taken from a live fix;
  - the kept track's 23 points are 0.4 to 26.8 m from it.

### Step 4(d): the offline region "DEVICE CHECK": **created** (by the previous coder; recorded on resume)
- **The path:**
  - the Journal's Records tab, then the "Offline maps" chip (`106`, `107-offline-panel.xml`);
  - the radius slider at its minimum (`110-offline-radius-min.xml`), and the name "DEVICE CHECK" typed (`111-offline-named.xml`);
  - "Download Maps", at about 04:22:06Z (the row's `createdAtEpochMillis`).
- **Finished.**
  - By 04:22:12Z (`112-offline-download-1.xml`), the chip read "Offline maps 1" and the list held "DEVICE CHECK", with
    no progress indicator. That list shows completed regions only, and the status line renders nothing on success
    (`ui/availability/AvailabilityOfflineMapsUi.kt:292-303`, `:388-410`).
  - It was re-read on resume (`123-offline-list-scrolled.xml`, 04:36Z): "1 mi around ‹coordinates› — **17 tiles, 0.3 MB** —
    downloaded 14 minutes ago".
  - The database row: `radiusKm=1`, `minZoom=10.0`, `maxZoom=15.0`, `isEntryCapture=0`. Its centre is 0.7 m from the waypoint.
- **The radius label reads "1 mi" for 1 km,** because `formatDistanceKm` rounds `1 × 0.621` to a whole mile
  (`domain/model/DistanceUnit.kt:58-61`). See Flags.
- **The side effect predicted in the pre-registration happened:**
  - `map_preferences.preferences_pb` went from 45 to 170 bytes. It gained `offline_map.last_picked_lat`, `_lng` and `_radius_km`.
  - Its first 45 bytes, holding `map.fullscreen` and `night_mode.maps`, both `false`, are unchanged (`prefs-c3-resume/`).

### Step 4(c): the track: the one kept in step 3
The entry in (e) kept it, so no second track was recorded.

### Step 4(e): the Journal day entry: **created** (04:39 to 04:42Z)
- **The path:** Journal, Entries (`124-entries-tab.xml`: "No entries yet"), then "New entry" (tap 881,1832, 04:39:06Z).
  - This persists a draft with every one of the day's candidates kept (`ui/log/CartographyViewModel.kt:115-146`).
  - The form's date reads **2026-09-27**, the phone's local date (UTC-7), as the pre-registration expected.
- **The name.** The form has no name or title field. "DEVICE CHECK 2026-09-28 (L0a)" went into its only free-text
  field, "Your own account (optional)" (`126-entry-named.xml`, read back exactly). See Decisions.
- **Kept by the app on start, all left kept** (`125-entry-new.xml`, `130-entry-scrolled.xml`, each row offering "Withhold"):
  - finds "DEVICE CHECK find 1" and "DEVICE CHECK find 2";
  - track "Recorded track", "379 ft · 1h 27m";
  - waypoints "Start · Sep 27, 7:23 PM" (ORIGIN), "End · Sep 27, 9:02 PM" (END) and "DEVICE CHECK waypoint";
  - offline region "DEVICE CHECK".
  - The track's own Start and End waypoints were kept by the app's default, and I did not withhold them. See Decisions.
- **The photo.** Photos are not kept on start; each is attached by a toggle (`:310`).
  - "Add a photo from the Album" showed three unlabelled tiles (`128-photo-picker.png`), in the order of an unordered
    `SELECT * FROM log_photos` (`data/local/MushroomLogDao.kt:43`).
  - By my reading of the image, all three show the same desk scene, so the image could not tell them apart. I predicted
    that the third was the new photo, from its place in the row scan, and tapped it (287,1275, 04:41:01Z).
  - **Verified in the database** (`db-c3-photo-query/`): `cartography_entry_photo_refs` holds exactly one row, for
    `5206066b…`, find 1's photo. The owner's two older photos were not attached.
- **Finished.** "Finish entry" was tapped (540,1798, 04:41:44Z). The button became "Save" (`132-entry-finished.xml`), which
  is the committed-entry form (`ui/log/CartographyEntryEditScreen.kt:285`, `:296`).
- **The database** (`db-c3-entry-raw/`): one `cartography_entries` row, `date=2026-09-27`,
  `text=DEVICE CHECK 2026-09-28 (L0a)`, `isDraft=0`. The refs: finds 2, track 1, waypoints 3, offline region 1, photo 1.

### Step 7: check 5, draw order on the entry map: **pass** for every pair seen overlapping; the outline's pairs **not run**
- **The screen.** The entry's report (`134-entry-report.png` / `.xml`, 04:42Z) has its map at `[0,437][1080,1247]`, and an
  "Offline map" switch at `[889,1278][1035,1413]`, shown because the kept region covers the entry's points.
- **The zoom:**
  - First, the app's own framing: 13 by the code (`ui/map/SightingsMap.kt:1507-1512`); inferred, not read.
  - Then five double taps near the cluster (04:46:06 to 04:46:57Z, `136`, `137`) and one one-finger drag (`138-entry-panned.png`,
    04:47:18Z). Each double tap was two `input tap` calls 0.15 s apart, and none opened fullscreen.
  - The final view's scale is 0.149 m/px. That is measured from the ORIGIN and END pin tips, 132 px apart for their stored
    19.7 m separation, which gives **zoom 17.0**, assuming MapLibre's 512-unit world at zoom 0 (**unverified**). So one of
    the five double taps did not zoom.
- **The test** (as pre-registered). In an overlap, the upper layer's own hex shows and the lower one's does not. Against the
  offline fill (0.2 of `#0B0B0B`, `ui/map/layers/MapLayers.kt:149`, `:224`), a layer drawn under it would read
  `0.8 × hex + 0.2 × #0B0B0B` instead of its hex. That blend was counted for every role and was 0 in every frame.
- **The pairs**, bottom to top in the registry (`MapLayers.kt:212-251`):

  | Pair (lower < upper) | Frame | What the pixels show | Verdict |
  |---|---|---|---|
  | offline fill < offline outline | `135` (framing, offline on) | On the fitted ring (centre (513,840), r 418 px), 3,340 px read exact `#FFFFFF`: 1,261 in the inner half (−3 to −1 px) and 1,123 in the outer half (+1 to +3). 99 read `#CECECE`, the dimmed white. The dashes' inner halves are not dimmed | **pass** |
  | offline fill < kept track | `138` (zoom 17; the whole view lies inside the 1 km circle) | `#A122F8`: 6,964 exact; its under-fill blend `#831DC9`: 0 | **pass** |
  | offline fill < waypoints, finds, photos | `134`, `135`, `138` | Each reads its exact hex (for example, in `138`: waypoint 4,261, find 1,740, photo 2,523), and each under-fill blend reads 0 | **pass** |
  | kept-track casing < kept track | `138`, rows 850 and 865 | `W[593-596] TRK[597-612] W[614-617]`: the casing shows only as a border. The 6 dp core is the track's own hex | **pass** |
  | kept track < waypoints | `138`, row 768, through the ORIGIN pin's tip | `TRK[472-473] W[475-477] WPT[479-480] W[482-484] TRK[486-495]`, and the track is unbroken one row below (row 775, `TRK[473-495]`). The pin and its casing cut the track | **pass** |
  | waypoints < finds | `138`, columns x 530 and 540 | `TRK[709-726] W[728-730] WPT[732] W[734-736] FND[738-770]`. "DEVICE CHECK waypoint", on find 1's own point, shows only a 1 px edge above the cap, and no `#350560` pixel inside the find's glyph, where its 22 dp head lies | **pass** |
  | finds < photos | `134` (framing), in the box where find 1's extent and the photo's intersect (x 483-542, y 809-839) | `#C1154F` 1,272 px and white 450, and **no** `#DA02AF` pixel. In `138`, columns 520 to 550: `FND[738-770] W PHO[776-837]` | **pass** |
  | offline outline < kept track, waypoints, finds, photos | — | The outline is 1 km from all of them and never meets them on screen | **not run** |
- By my reading of `138-crop-cluster-x3.png`: both pins' tips draw over the track, find 1's cap covers the waypoint under it,
  and the photo covers find 1. This matches the profiles above.
- The breadcrumb, the search centre, sightings and planned trips are not drawn on the entry map
  (`ui/log/CartographyEntryReportScreen.kt:355-386`; `showSearchCentre = false`).

### Step 8: check 2, offline part: **pass**
- **Offline off** (`134-entry-report.xml`): the caption reads `© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)`. That is byte-equal to
  the Topographical `attribution` (`ui/map/Basemap.kt:161`), the entry map's default basemap.
- **Offline on** (the switch tapped at (962,1345), 04:44:28Z; `135-entry-offline-on.xml`, switch `checked="true"`): the caption reads
  `Protomaps © OpenStreetMap`. That is byte-equal to `OFFLINE_STYLE_ATTRIBUTION` (`map/OfflineStyle.kt:28`), with no ` · `,
  as `mapCreditsFor` gives with no layer credits (`ui/map/BasemapStyles.kt:130-140`).
- The same caption stayed through the zoom and both Night Maps states (`138`, `143`, `147`).

### Step 9: check 6, an observation on the entry map (not a gate)
Nothing looked faded, missing or restyled against the code's paint values:
- **Every overlay the entry keeps is drawn:** the fill, the dashed outline, find 1, find 2 (at the framing), the photo, the
  three waypoints, and the kept track with its casing.
- **Icon interiors read their role's DAY hex exactly,** as the counts in step 7 show, so `icon-opacity 1` holds.
- **The kept track's core** is 16 px across a near-vertical run (rows 850 and 865), which is 6 dp at 2.8125 px/dp. Its
  casing spans 25 px (593 to 617), which is 9 dp. Both are as `SightingsMap.kt:1179`, `:1194` give them, at `line-opacity 1`.
- **The outline dashes** measure 16.8 px on and 11.3 px off, which is 6.0 dp and 4.0 dp, over 20 dashes on the left of the ring
  in `135` (`SightingsMap.kt:1216-1219`).
- **The fill:** the basemap ground outside the circle is `#E2DFDA`, and inside it `#B7B4B0`. `0.8 × #E2DFDA + 0.2 × #0B0B0B`
  is `#B7B5B1`, within 1 per channel, so the fill opacity is 0.2 (`MapLayers.kt:149`).
- There is no earlier screenshot of this map, so this is not a before-and-after comparison.

### Step 10: check 7, restated: **pass**
- **The setup.** The entry map, with offline tiles on, at zoom 17 (the view of `138`).
  - Night Maps off: `138-entry-panned.png` (04:47:18Z).
  - Night Maps on: `143-entry-night-on.png` (04:50:20Z). It was turned on through Tools, then Settings, then "Night Maps"
    (tap 113,1148, 04:50:17Z; `141`: `checked="true"`; `night_mode.maps` read `08 01`).
  - Night Maps off again: `147-entry-night-off-again.png` (04:51:42Z).
  - The Settings drawer lies over the entry, so the map and its zoom stayed put.
- **The basemap:**
  - `144-logcat-since-0444.txt` line 37188 reads: `21:50:18.185 … I SightingsMap: Offline night: 57 colour properties recoloured, 0 left in day colours.`
    So N = 57 > 0, logged 1 s after the toggle.
  - The flat ground patch (x 50-150, y 887-987 px, 10,000 px, inside the fill) reads `#B7B4B0` in every pixel with night off,
    and `#504D49` in every pixel with night on.
  - V1 of the day sample is `#4F4C48` (`ui/map/NightColour.kt:39-47`), and `#504D49` is within 1 per channel of it. It is
    exactly `0.8 × V1(#E2DFDA) + 0.2 × #FFFFFF`, the recoloured ground under the night fill (`ui/theme/MapPalette.kt:101`; the day fill is `:83`).
- **The overlays** (the pixel counts are for the map area, y 437-1190):

  | Role | Expected DAY | Measured, night off (`138`) | Expected NIGHT | Measured, night on (`143`) | V1 of DAY, if recoloured (`143`) |
  |---|---|---|---|---|---|
  | waypoint (glyph) | `#350560` | `#350560`, 4,261 exact | `#B97DF7` | `#B97DF7`, 4,267 exact; `#350560` 0 | `#CD9DF8`: 0 |
  | find (glyph) | `#DA02AF` | `#DA02AF`, 1,740 exact | `#F96FAC` | `#F96FAC`, 1,739 exact; `#DA02AF` 0 | `#D200A7`: 0 |
  | photo (glyph) | `#C1154F` | `#C1154F`, 2,523 exact | `#E8046D` | `#E8046D`, 2,523 exact; `#C1154F` 0 | `#FD518B`: 0 |
  | kept track (line) | `#A122F8` | `#A122F8`, 6,964 exact | `#EEA7FE` | `#EEA7FE`, 6,937 exact; `#A122F8` 0 | `#7900D0`: 0 |
  | casing (glyph and track outline) | `#FFFFFF` | `#FFFFFF`, 6,326 exact (x 61-621: the casings, and white in the caption area) | `#000000` | `#000000`, 6,316 exact, x 445-621, y 688-984: the cluster only | — |

- Each role's pixel extents are the same in `138` and `143`, to within 1 px.
- With night off again (`147`), every count is identical to `138`.
- So the overlays draw their palette's own NIGHT colours, and not the basemap's V1 recolour of their DAY colours.
- **Restored:** Night Maps was unchecked at 04:51:39Z (`146`: `checked="false"`). `map_preferences.preferences_pb` is then
  byte-identical to its read at resume (sha256 `17127fa1…`, both).

## Created data (all still on the phone, as authorised)

| Item | Created | Where the app shows it |
|---|---|---|
| Waypoint "DEVICE CHECK waypoint" | 04:06:55Z, previous coder, step 4(a) | the Maps tab (step 5); Journal, Records, Waypoints (3); the entry's Waypoints list and map |
| Find "DEVICE CHECK find 1", own identification and notes "DEVICE CHECK find 1", with one camera photo | 04:17:52Z, previous coder, step 4(b) | Journal, Records, Finds; the entry's Finds list and map. The photo is in the entry's Photos, on its map, and in the Album |
| Find "DEVICE CHECK find 2", own identification and notes "DEVICE CHECK find 2", no photo | 04:19:50Z, previous coder, step 4(b) | Journal, Records, Finds; the entry's Finds list and map (at the framing) |
| The camera photo (`log_photos` row `5206066b…`, 1,841,146 bytes in `files/photos/`) | 04:16:32Z (`createdAtEpochMillis`), previous coder | as above. **Not pulled off the phone** |
| Offline region "DEVICE CHECK", 1 km ("1 mi"), 17 tiles, 0.3 MB | about 04:22:06Z, previous coder, step 4(d) | Journal, Records, Offline maps; the entry's Offline Regions list and map circle |
| Track (the owner's recording, stopped and kept in step 3; 18 points in the app, 23 rows) | stopped 04:02:25Z | Journal, Records, Tracks; the entry's Tracks list and map |
| END waypoint "End · Sep 27, 9:02 PM", created by the stop itself, not asked for | 04:02:25Z, step 3 | Records, Waypoints; the entry's Waypoints list and map |
| Journal entry, `date=2026-09-27`, text "DEVICE CHECK 2026-09-28 (L0a)", committed; it keeps the two finds, the track, three waypoints (ORIGIN, END, DEVICE CHECK), the region and the photo | 04:41:44Z, this coder, step 4(e) | Journal, Entries, SEPTEMBER 2026, day 27 (`133-entries-list.xml`) |
| `map_preferences` keys `offline_map.last_picked_lat`, `_lng` and `_radius_km`, a side effect of the download | 04:22Z | not shown. It is where the offline picker would reopen |

- Nothing else was created, and nothing existing was edited or deleted.
- The end database copy (`db-c3-end-raw/`, 04:52:22Z) is byte-identical to the copy taken just after the entry was
  finished (`db-c3-entry-raw/`). Its counts: `cartography_entries` 1; finds refs 2, track refs 1, waypoint refs 3,
  region refs 1, photo refs 1; `mushroom_log_entries` 2; `log_photos` 3; `log_entry_photos` 1; `offline_regions` 1;
  `waypoints` 3; `tracks` 1; `track_points` 23; `planned_trips` 0; `cached_searches` 1.

## Settings (continuation)

| Setting | Start (read) | Changes | End (read) |
|---|---|---|---|
| `settings system accelerometer_rotation` / `user_rotation` / `font_scale`, `secure ui_night_mode` | `0` / `0` / `1.0` / `2` (first run, 03:10Z) | none | `0` / `0` / `1.0` / `2` (04:52Z) |
| Night Maps (`night_mode.maps`) | `false` (`prefs-c2-start/`; `140-settings.xml` unchecked) | on at 04:50:17Z, off at 04:51:39Z | `false`: `146-settings-nightmaps-off.xml` unchecked; its dump is byte-identical to `140`; `prefs-c3-end/` is identical to `prefs-c3-resume/`; the Maps tab reads "Night mode off" (`148-maps-final.xml`) |
| Map fullscreen (`map.fullscreen`) | `false` | none | `false` (same file); the "Fullscreen" button is in `148` |
| Maps tab map mode (session) | Topographical | none | Topographical (`148`: "Map mode: Topographical … Night mode off.") |
| The offline picker's remembered region (`offline_map.last_picked_*`) | absent | added by the download in 4(d) | **present.** It cannot be removed through the UI; see Created data |
| Entry map: offline switch, zoom (session state, `remember(entry.id)`, `CartographyEntryReportScreen.kt:238`) | off, the framing | on and zoomed, for steps 7 to 10 | left as they were when I went to the Maps tab. They are not persisted |
| Track recording (the owner's) | running | stopped and kept in step 3, as authorised | stopped; "Start recording track" (`148`) |
| **The phone's log buffers** | the default set holds main, system, crash and kernel (`logcat -g`) | **cleared by me at 04:44:28Z with `logcat -c`.** A mistake: see Decisions and Flags | cannot be restored |

- `/sdcard/l0a-ui.xml` was removed at the end (`ls`: no such file).
- **The crash log at the end** (`150-crash-end-c3.txt`, 04:52:22Z) is empty, but it covers only 04:44:28Z onwards, because of the clear.
  - For 04:35:38Z to 04:44:28Z the crash buffer itself is lost. It had read empty at 04:35:38Z.
  - What covers that gap is the app's PID: 19584 at 04:35Z, 04:44Z and 04:52Z, the same process as in the first run. A fatal
    Forager crash would have ended it. That is inference, not a crash-log read.
  - The build is unchanged at the end (`151-dumpsys-package-end-c3.txt`: `versionName=1.0.1192+g24589349`).

## Decisions I made (resumed part)
1. **I did not merge `origin/journal-redesign` again.** The dispatch says to merge it first. The previous coder did, at
   `bc64d37`, and the planner's resume message asked only for `origin/device-l0a-2026-09-28`, which was level.
   `journal-redesign` is now at `f7e219a` and carries L0b app code (`fe07702`). Merging it would have left this branch's app
   tree unlike the installed build, and the citations out of date. The planner should decide whether the branch needs it
   before merging.
2. **I followed the dispatch, not my agent definition, on the record.** No sweep, intent or terminal, and no `RECORD.md`.
   The dispatch says the kit is gone and the planner writes the record, and `.claude/kit.json` and the checkers do not exist
   at this base. That resolves a conflict between two instructions, which is the planner's to confirm.
3. **The entry's name.** The form has no name or title field, so "DEVICE CHECK 2026-09-28 (L0a)" went into "Your own account
   (optional)", its only free-text field. The entry's date is the phone's local 2026-09-27. I kept the dispatch's text
   unchanged rather than writing the local date into it.
4. **The track's own ORIGIN and END waypoints stayed kept** in the entry, as the app kept them on start. Withholding them was
   possible. I took "keeping everything from (a) to (d)" as not requiring it, and the kept track is (c).
5. **Which photo to attach.** The three Album tiles could not be told apart by eye. I tapped the third on a prediction from
   the row scan order, which the code does not fix (`MushroomLogDao.kt:43`, no `ORDER BY`), then confirmed the attached id in
   the database. Had it been wrong, I would have had to remove it from my own entry.
6. **Methods:**
   - how the double taps were made (two concurrent `input tap` calls) and how far to zoom (about 17);
   - the under-fill blend as the discriminator for every pair with the fill;
   - a grid-searched circle for the outline test;
   - the ground patch inside the fill, with the fill blend modelled, for check 7's basemap half;
   - the kept track as check 7's line (the outline was off screen at zoom 17).
7. **The layout.** The updated Outcome table goes at the opening of this section, ahead of the pre-registration, which is
   not edited. The first run's text, including its Status line, is left as it was written.
8. **Not decided, done by mistake:** `adb logcat -c` at 04:44:28Z. I meant to isolate the recolour log line, and did not check
   which buffers it clears. It cleared the phone's main, system and crash buffers, which are not the run's to clear, and it
   weakens the end crash read (see Settings). The right way was `logcat -T <time>`, which reads from a time without
   deleting anything.

## Flags outside scope (continuation)
1. **The radius label rounds 1 km to "1 mi".** `formatDistanceKm` rounds `radiusKm × 0.621` to a whole mile
   (`domain/model/DistanceUnit.kt:58-61`), so the minimum 1 km region reads "1 mi" in the picker, the region row and the
   entry, where it is about 0.6 mi. Not investigated.
2. **The Records track details against the database** (the previous coder's step 3 "See Flags"):
   - the sheet reads "Points 18", but `track_points` holds 23 rows for the track;
   - it reads "Duration 1h 27m", but `endedAt − startedAt` is 5,918.9 s, about 1 h 39 min.
   - Perhaps points excluded at the read seam, and a duration measured between kept points (`domain/isNetworkProviderFix`
     is named in `GetCartographyEntryMapDataUseCase.kt`). **Unverified; not looked into.**
3. **A dim over the map after the waypoint was dropped** (the previous coder's step 4(a) "See Flags"): 0.839 times the map's
   values, under the app's chrome, cleared by a tab round trip. Cause not looked for.
4. **The location puck's order against the reticle changed** after a tab round trip (step 6). It is not a registry layer.
5. **The test finds are iNaturalist-unsynced** (`syncStateKind=DRAFT`). If the owner later syncs, two "DEVICE CHECK" finds,
   one with a photo, could be uploaded. Not checked whether any sync is automatic.
6. **Evidence file problems:**
   - `112-offline-download-6.xml` to `-9.xml` are 0 bytes on disk, although `snaps.log` lists a non-empty hash for each: lost
     in the machine restart (inferred).
   - The previous coder's numbering reused 90 and 91 (`90-find1-notes-fixed2.*`, `91-find1-ready.*`) beside the first run's
     `90-crash-end.txt` and `91-dumpsys-package-end.txt`. The names differ, so nothing was overwritten.
7. **The phone's log buffers were cleared by this run** (Decisions 8). Anyone reading the phone's logcat for times before
   04:44:28Z will find nothing.
8. **The Journal's "today" is the phone's local date,** so an entry made before 07:00Z on 2026-09-28 is dated 2026-09-27.
   That is why the entry's date and its name differ.

## Evidence index (continuation)
All files are in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-l0a/`. They show or hold real locations and stay
outside the repository. The first run's files are indexed above. `90-crash-end.txt` and `91-dumpsys-package-end.txt` are the
first run's, and are not repeated here.

| File | sha256 |
|---|---|
| `60-dumpsys-package-start.txt` | `87753366640a22eb039046c0e10dea6f5a3eeb4e2e380c3e4d7eaed5e9a4549f` |
| `61-crash-start.txt` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `62-step2-maps-recording.png` | `0ae8b1b09753039c8180488b6e1f92755942546b77c28d6a1d6f2d8098e7dd33` |
| `62-step2-maps-recording.xml` | `4d2ddfb52de7f37392d7109f056beea1dfcee8a057c13cc4f5b5834382bf17ff` |
| `63-after-stop.png` | `350b23ed7c165a3442a99037637ef00f776e6a0ab38556002617b1bb2af9e53d` |
| `63-after-stop.xml` | `c6edb85a1a4ada71204f8ea6b7be917f49a87d09daa674d0c5772d582031a40e` |
| `64-journal.png` | `f133db675fb6462349cfd1f68c9c2013510949f62332708dae3c0859be0ddeeb` |
| `64-journal.xml` | `c6edb85a1a4ada71204f8ea6b7be917f49a87d09daa674d0c5772d582031a40e` |
| `65-journal.png` | `3fc696099ddbd5723ad4d48be333b6f453eed588dee1c02edcd92229b65578f0` |
| `65-journal.xml` | `998494ca9e25e8d1879cc2272356a12e17b936dbb488d97f6dda0f241d730b4d` |
| `66-records-tracks.png` | `417621c415260aa8496396c1678fd6dc1b19f44bbf59dc51489c6e5336cc0f6f` |
| `66-records-tracks.xml` | `777a822e5adebd1293b37a528286dbf97ccd2062f45f586469cc34b55c681eea` |
| `67-track-details.png` | `e0bc566ca6f42f3193da63a3a52cf84d96d7f9bba8ce43cedd5244ba438b261e` |
| `67-track-details.xml` | `192c0a41f84c36d11984cd3cffa2e342bfcb8d0c538f42518b3ad608db71b5f0` |
| `68-sheet-closed.xml` | `777a822e5adebd1293b37a528286dbf97ccd2062f45f586469cc34b55c681eea` |
| `70-maps.xml` | `c6edb85a1a4ada71204f8ea6b7be917f49a87d09daa674d0c5772d582031a40e` |
| `71-add-menu.png` | `15563b213f39ccc876207c3379ea5ab01eab33564a4991de4a24143f5a667556` |
| `71-add-menu.xml` | `29438b7e544efff016e752ea4ee566d6d898a9c036c02a8ee3e4b04e6794023a` |
| `72-crop-picker-x2.png` | `56d4ea3eff3779d824d81a5b85dec0ba9353eafcfefa42417c5763f11a710673` |
| `72-waypoint-picker.png` | `6843eca6d4c47fa6b589a30dea9731c60bafab21d9cf07d14fa87dbf523f7842` |
| `72-waypoint-picker.xml` | `7ff6b3a112509af0a830a38eb4e824317b322ac6378f83ec47a0079c12c4f617` |
| `73-waypoint-name.png` | `6c22f280255d4d93eb8e1625d9387d4733e1adf237ff5ddf2f07b9b56d099077` |
| `73-waypoint-name.xml` | `340193c85d97eff84ff36190dcd20d46be970f1380fac6a49992fd975e598a2b` |
| `74-waypoint-named.png` | `1da0f3634bcdc7256cb52cc0fa54b1b16fa20192c3d55476f0f4478db57fde22` |
| `74-waypoint-named.xml` | `c8ddd51e3b0268e94e860eda5cd4cc4876f67565dcb4a99a1a97edda2b15d190` |
| `75-crop-pin-x4.png` | `399d431b89c6c0a054b5a026899a9f856987e41216374fd34e981b79d21e258d` |
| `75-waypoint-dropped.png` | `297fe9d1bf143f8bfe806f0415e3de773b73237fcd9c2c948b43c8128e382ad5` |
| `75-waypoint-dropped.xml` | `6044bfdc98a10c4dad9581a11df9bc3667513f10f117eb1de0357d45b6fed672` |
| `76-check8-pin.png` | `15aa41a042d88f869f331bfcb02ac821b94e923e0f6e7e6288f505e310947e92` |
| `76-check8-pin.xml` | `a21fff49510e90af095e2ddd1829726f2c15b535a21b4217681e079c4ba19665` |
| `77-maps-recheck.png` | `38c81b7ec438d9d1d17368f79ad15344d8a598c4660ef06e44e52e9c74228d99` |
| `78-crop-pin-x4.png` | `d3fd01528b88f975efe98b7728a37e0c20f9fbeac38b00d1c6f2fe4fabee7982` |
| `78-maps-after-tab-roundtrip.png` | `9fb96b9bc7c2b9368b90daee6b8730958bc2c3492e603febf8a37b9e52e43f02` |
| `78-maps-after-tab-roundtrip.xml` | `d16e43898309931094c003f3ec6b19d0e5369e677276f3c46a0bd5d49274e4de` |
| `80-add-menu-2.xml` | `29438b7e544efff016e752ea4ee566d6d898a9c036c02a8ee3e4b04e6794023a` |
| `81-find-picker.png` | `aa6982da9ee1b8f4a66dc3861d92776dafbe0126d6d4f17f019b875ee159b639` |
| `81-find-picker.xml` | `2aa34f8a9716b399a21b9b83971cf74c034a5ff84ab9f54ab00f72516caaa10c` |
| `82-find-form.png` | `4727a4a5890fca106381f6b9f924b9384abf66009783b573202e1ee492cc22de` |
| `82-find-form.xml` | `a17fb120bcc6fee56410ad4b8c1175c3dbd09e181d1c66f7e610e5a88babc8e0` |
| `83-find1-ident.xml` | `41e4a398c4ce6357450ab5810d5e3cf86f47d40556840813554e79ed1b6f5be2` |
| `84-find1-notes.png` | `17cca16257d025301cc327ebe4bf57d479d4e0856cff99f536e423f7548ab320` |
| `84-find1-notes.xml` | `d0ab5a34e4a7ef087a1ceb1c8d4e326b80a9be2aacd80ffbc84a69300b3039fd` |
| `85-find1-ident-fixed.xml` | `41e4a398c4ce6357450ab5810d5e3cf86f47d40556840813554e79ed1b6f5be2` |
| `86-find1-kb-closed.png` | `a2f20b33a89c78bd453434399d51f32b116c425efe35b2572e3c1f99cc2956e1` |
| `86-find1-kb-closed.xml` | `41e4a398c4ce6357450ab5810d5e3cf86f47d40556840813554e79ed1b6f5be2` |
| `87-find1-notes.png` | `e8fcd436a87ad25ba663b7100558913f01fd379b64b97f9968a2d0c68c7dd055` |
| `87-find1-notes.xml` | `d1f7558cb7d2cc9ca29d84b456048d45d8252d6cf1424c3e7c1470f874848ddf` |
| `88-find1-before-camera.png` | `90cb9b631b13dbe803df8e99c7d57cb80c946efe70f145930d3c3440852215cb` |
| `88-find1-before-camera.xml` | `1d8f655ad6e1816b45d7f10618ce522299dbaf8d93a0b6e2613eaad514536684` |
| `89-find1-notes-fixed.png` | `f104a66dc5db64a75b7b50dbdb4525cff8ea9f1dc9379553937f7851f9db4d4c` |
| `89-find1-notes-fixed.xml` | `0aa8de99416a51df125eaab621e085cdc1a569e1b31d410a9895f4159488445a` |
| `90-find1-notes-fixed2.png` | `79cf5fb4430796e44dbe8ca7286937dd30c97dfe288db08d24c6cf9e6b8d27a1` |
| `90-find1-notes-fixed2.xml` | `d1f7558cb7d2cc9ca29d84b456048d45d8252d6cf1424c3e7c1470f874848ddf` |
| `91-find1-ready.png` | `7922931d83765407d6d575205f8b8eab5831969c8cacc8df49dd3ee9f6b1d597` |
| `91-find1-ready.xml` | `e37acba38797b1bbe259228093e88532d80e4e9386853163abff608eb933dff8` |
| `92-camera-open.png` | `274d32954feb24beb3e1370aa72953f3abd53059742544900ee16da2a742d360` |
| `92-camera-open.xml` | `b577d5a174b52e530567ba17306f4817fc710880746bdfff259a58e673f4916a` |
| `93-after-shutter.xml` | `8c654ae7cf33dd0c8d8b8b1d49e34122e4794d6d737e599697199c6dd1015376` |
| `94-find1-with-photo.xml` | `57d4ad6a513d5d4d34843100af7c7d38bebce0ab58693b5e2aedcec2e4de9da2` |
| `95-find1-portrait.xml` | `abc6b74c4f05d94af03e819b9c23a61c83a45256d16ef688040f81c2a5e80c05` |
| `96-find1-saved.xml` | `1cfc13d1f7eb3a2d71b9df323890a6e50713dedecb8d6dc85b48b55b9c7b69f6` |
| `97-maps-before-find2.xml` | `f0f7f00c7fbdf625990f16fa5c4d18df2fa82bcc9ed1874eeabcb24b0471231f` |
| `98-add-menu-3.xml` | `29438b7e544efff016e752ea4ee566d6d898a9c036c02a8ee3e4b04e6794023a` |
| `99-find2-picker.png` | `7bb10460ab69913b0e427e3b434a51a5bac959e4bbdcb4fa1f357d21d80c5b42` |
| `99-find2-picker.xml` | `7b11b02faff5516227ee97ada08698bb60d0256b9ff92dfa81defa2b68c0a7dd` |
| `100-find2-panned.png` | `13ce8ac4a388723ebb40b41975938630bd6e05cd9ddb7225ce63796b1717bd03` |
| `101-find2-form.xml` | `e4b69d6f948943b03884cf000dfc14cd118876aa6c7dfd2b8fbff95e5d086a9d` |
| `102-find2-ident.xml` | `5448062f27311f27905b770c64ea8b7e7dffe364ec19946b0cb56054b0119501` |
| `103-find2-notes.xml` | `36ec8f5f8a84feaf7171d4f7fae86a0de9b62758fc1670b2fb9672502dfc4175` |
| `104-find2-ready.xml` | `c49a8e4c4566366c0cca955887a7bc6cc4a6c8f75d79073d7f990e1417e1d7ee` |
| `105-find2-saved.xml` | `55216de2f37337ba6a5e66a3e2709a3548b082bf4c1f595fe044b0d64f5f36b7` |
| `106-chips-scrolled.xml` | `5bc8d4017c2ae700e71af0d9ef37e6567da43a636690eb3413ad85450ae33b35` |
| `107-offline-panel.png` | `0dda92190f49e21d95e08489ccca616fbccab4286c75ad264adb41022c6be319` |
| `107-offline-panel.xml` | `977bfe8334a4289fe91d3167cfba672672cd82ff980e5557467168bdbf2af5ed` |
| `108-offline-form.xml` | `b04b151b87f2c3aae5d237acebf5c433e8b2197b29c3c9162e16e890224a21ab` |
| `109-crop-slider.png` | `d999ebc4ba01819ca87ebb644aa342a91113b4eb7e6aa7dbff263fcd0c18a274` |
| `109-offline-radius.png` | `65fcecc2f7efeed521329f8336169539e62bab09b56975ec520553f996944834` |
| `109-offline-radius.xml` | `b04b151b87f2c3aae5d237acebf5c433e8b2197b29c3c9162e16e890224a21ab` |
| `110-offline-radius-min.png` | `39ab199c7646b9af8adfe08f001a879fd9c51f99065132e0ff049af1834f315a` |
| `110-offline-radius-min.xml` | `8f569e3da950e670f61d29d26ff0909e4e9ffba77182bed5357b6ddcf20c8ff8` |
| `111-offline-named.xml` | `946c5497ca49cb44746d373b806a294aef6e5c9fbd367ef60ea37cc84c9aff70` |
| `112-offline-download-1.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `112-offline-download-2.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `112-offline-download-3.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `112-offline-download-4.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `112-offline-download-5.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `112-offline-download-6.xml` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `112-offline-download-7.xml` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `112-offline-download-8.xml` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `112-offline-download-9.xml` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `120-dumpsys-package-resume.txt` | `55539ed39e04f0249203eb7258f063c0e0caad37d2d8cc73c4adb3b171dc4ddb` |
| `121-crash-resume.txt` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `122-resume-screen.png` | `8828f6644d6d89aef1dfbd9160a2046c3a796ac6163533b081348790d56feecf` |
| `122-resume-screen.xml` | `e131a13989ae147a1b401e47713e4d5bd9e98b4308520830319ca8a6f0ba7912` |
| `123-offline-list-scrolled.png` | `3646806e27310cd2211960eb3f4bbcf05e196b33c3c6fe784708ef423de3ef65` |
| `123-offline-list-scrolled.xml` | `6f639d5573cfa272579082547edde3e3ab51969d6fdf774fc11ac3f57060658d` |
| `124-entries-tab.png` | `4c07039d778831ed93f04b8eae3fd6b8c6445dc2c1f8ec776834aff40496efc9` |
| `124-entries-tab.xml` | `ec350fafef11141585bb64c79cd67898ee86d406b074ed027ddaf54c44a70168` |
| `125-entry-new.png` | `82a1a9c1c49de7e408d0076d2f73e7a825c528c215177f4f1ed071747ad2b6f2` |
| `125-entry-new.xml` | `1e526a8845499dbca2a1794f2b2fee282d475332860ad461c5295b25377b4191` |
| `126-entry-named.png` | `92f8e045d7b2c2bc7058a4dfa3ce1febb3c9f85a15fc5142f7de9163f8fb06e9` |
| `126-entry-named.xml` | `7094a068d967e962c6b2e22900a85342340b610afc4f1612aebb383a15836397` |
| `127-entry-kb-closed.png` | `c5766427f7b8f11546545efadb9967d2a6484f39d40c7e4a99b09ace7d4c214e` |
| `127-entry-kb-closed.xml` | `7094a068d967e962c6b2e22900a85342340b610afc4f1612aebb383a15836397` |
| `128-photo-picker.png` | `75460a91475fa9d278c533f20080014dfc49f6f4afd1123acd7bb9301aa586d3` |
| `128-photo-picker.xml` | `44cf0275b34428a8e18d5cefeaba082764ab4dff4e275b10c4b83926873a7d05` |
| `129-entry-photo-added.png` | `895ea87eb20508fce8df323a80124f7aa9dda1b0d655f20a9017de87742fd857` |
| `129-entry-photo-added.xml` | `836381f566b0fd4b185a7e90c352aebc1b384c4a67d0ea008e4f7db0771da660` |
| `130-entry-scrolled.png` | `615f4e43bf815e62448c0576202786032fea964ac662079b8a1b3054f00ac781` |
| `130-entry-scrolled.xml` | `87047848335d8c41978b3a304db0986b8f4e7cd65dff0027a481cb7095383682` |
| `131-entry-bottom.png` | `3ce4f427b3fbb84546f0de930ca7dd8b30a4bafd5b712861edde043b29b8e579` |
| `131-entry-bottom.xml` | `5727471c8a176b30b40ad94c5391f6ff550dc7ee15fdad0817289167be60ee5f` |
| `132-entry-finished.png` | `056e0cc86f29c038e13e0122d828cefc2feab081a457e7f2e49870820a7bae99` |
| `132-entry-finished.xml` | `29c3129891aed1e3ab4dd2a1570cd3e6546fb5e1cf471107ff9363d10b67d1ca` |
| `133-entries-list.png` | `fd72bd4e838798422e1f9cdb6f513cb661ce7887a6f5303975610cc89b0f38e0` |
| `133-entries-list.xml` | `a113e284b338f0a56dcb6d786a1abbc9b8f1bbb9504a8dab664f30eb7ab23da1` |
| `134-entry-report.png` | `99037f3d8fa043e64e059f8cef1b9f9409edc50da94f7d4ae27dbb0b23aa23b8` |
| `134-entry-report.xml` | `eef9254400b2081fc2ea2d5b13840e4f05835bb000cb94220b7c16eb6c18b264` |
| `135-entry-offline-on.png` | `6238692d29c8e9deef2433b32c9dd1c907cbf56c336d56c77300959ffb6f4380` |
| `135-entry-offline-on.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `136-entry-dtap1.png` | `798d43437b3cf338f1b92af80333463523025ddf9d01372619a3e177e0fa14b5` |
| `136-entry-dtap1.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `137-entry-dtap5.png` | `a1762fec03566340fa84391c37272475e326d582f6a782a203011cadacb05328` |
| `137-entry-dtap5.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `138-crop-cluster-x3.png` | `66635b37e81e3f9210d4837170659bd323cf320539bcbdaf11a30da9e25cabeb` |
| `138-entry-panned.png` | `f2b84d9611c2b67acd980798701376ba58b6aa833a709925bb2b42ebfb9ed3fa` |
| `138-entry-panned.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `139-tools.png` | `2c3c04d89885e628736a13f3dd58c1208049cfee2e6c7f59e5028859a6240bea` |
| `139-tools.xml` | `41fb762bae523e558c9a0c6cb0b5e73913ffbf9daacdda95cf1c0d188fda9075` |
| `140-settings.png` | `9f1c0956ae61c40b4377b14988fbabfaee86c3703759528dcc9604b369e21a80` |
| `140-settings.xml` | `53cbb94f709ab52015f6b8dbf2eab0c8ebfa21fbda289c2f184838482c9cec3c` |
| `141-settings-nightmaps-on.xml` | `5e09480a188e128979c625d14acf0e05bdeba991dccbc1f9750de5fddba9b980` |
| `142-drawer-closed.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `143-entry-night-on.png` | `0dfa430fd6ef1e4215254bba393b778a5a635361f1411bb7ee6ee968cf1c9656` |
| `143-entry-night-on.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `144-logcat-since-0444.txt` | `617a44d3f8224cd52b20e5129bdcdd63f4a8db1b4dcc85cd912ec76bebbd361c` |
| `145-settings-2.xml` | `5e09480a188e128979c625d14acf0e05bdeba991dccbc1f9750de5fddba9b980` |
| `146-settings-nightmaps-off.xml` | `53cbb94f709ab52015f6b8dbf2eab0c8ebfa21fbda289c2f184838482c9cec3c` |
| `147-entry-night-off-again.png` | `f2576f117d1fa50981de987e7fe25fbf63295a47aa267f15dd6e8aa3e0416f7b` |
| `147-entry-night-off-again.xml` | `cfbaf2b6800fc70f2f6391b4da8618bce5cd49896ae927fe71399ffbb12e7e1f` |
| `148-maps-final.png` | `8049e30164769dffe765a1ab07234bde6d05ad3d5be207d4aad66a7e195b9bd6` |
| `148-maps-final.xml` | `2edc27e4718de1fe0318c21a4c12ce558c4006bee0d7afbb4873121d2b392dc1` |
| `150-crash-end-c3.txt` | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` |
| `151-dumpsys-package-end-c3.txt` | `55108046233d4ebeec8ece914949550bb1f099ee1aa153a1300ea6921de9be0b` |
| `prefs-c2-start/camera_grid_preferences.preferences_pb` | `24b68c2ca7f4d89670aca701dce54732474102c3bab4d5dc82e69a95c23a1a91` |
| `prefs-c2-start/camera_orientation_preferences.preferences_pb` | `534e5373fbfcdfadf22ea42cb110d8897721c33d5a01b5982b8d673523fc1a30` |
| `prefs-c2-start/map_preferences.preferences_pb` | `1974b87e8fa152a27d23ef36932d97970a602f5d6c2799190194247fbeb6cecc` |
| `prefs-c2-start/photo_location_preferences.preferences_pb` | `10ffcfd37b998d56d264dc329e80b0cb336a25d0b952115aeeef75d012955593` |
| `db-c2-start-raw/forager.db` | `50f1e45eccdd5e7edaeedf2be9e1dd7437c110071c5df53e55e221a228d2a0f3` |
| `db-c2-start-raw/forager.db-shm` | `fe014b5fd04a12b88213ec8631609e5d2374f4662539c247d60fbb0b5d3e4bed` |
| `db-c2-start-raw/forager.db-wal` | `560d30dffc033ca0c686a4055d34441b10a2b46314424cfc04e739902f3b6eae` |
| `db-c2-afterstop-raw/forager.db` | `50f1e45eccdd5e7edaeedf2be9e1dd7437c110071c5df53e55e221a228d2a0f3` |
| `db-c2-afterstop-raw/forager.db-shm` | `617bf593b875d3e70a15fc0a549b204207ed700acd24c04f724565a6099b0430` |
| `db-c2-afterstop-raw/forager.db-wal` | `3b4bb4796ea6ed9c907558c018175ee029774d5087b9deda15fccd88c029fca3` |
| `db-c3-resume-raw/forager.db` | `a49b7dd613ab4cc85dfc18255585116e4e3b1ba442ab485e1bdff5943694947c` |
| `db-c3-resume-raw/forager.db-shm` | `8e3938fdb51d2df16db9308381a5e81ed549025b09d092066325b07d1d87fcee` |
| `db-c3-resume-raw/forager.db-wal` | `7523ed59fdeeb07bd33aa0acbea9d01a452baf6b894c9063ca2f2583dd36bb70` |
| `prefs-c3-resume/camera_grid_preferences.preferences_pb` | `24b68c2ca7f4d89670aca701dce54732474102c3bab4d5dc82e69a95c23a1a91` |
| `prefs-c3-resume/camera_orientation_preferences.preferences_pb` | `534e5373fbfcdfadf22ea42cb110d8897721c33d5a01b5982b8d673523fc1a30` |
| `prefs-c3-resume/map_preferences.preferences_pb` | `17127fa1a9ac05cbb242ab4233a0f6d054cecbd3fc6f2516ebca3a13d33af137` |
| `prefs-c3-resume/photo_location_preferences.preferences_pb` | `10ffcfd37b998d56d264dc329e80b0cb336a25d0b952115aeeef75d012955593` |
| `db-c3-entry-raw/forager.db` | `fe5d658f20a266183d92b391bd4c18e9b540a8384da49715e4d3d1f40f28c715` |
| `db-c3-entry-raw/forager.db-shm` | `aef8d7281136dcace2a31ffaabab6d3da2261ff1d8e953982854458c195c0f7e` |
| `db-c3-entry-raw/forager.db-wal` | `0778bf5245c0c6acede3ed544617fce3295a1ee320d6a7366bb601a6e6420cff` |
| `db-c3-end-raw/forager.db` | `fe5d658f20a266183d92b391bd4c18e9b540a8384da49715e4d3d1f40f28c715` |
| `db-c3-end-raw/forager.db-shm` | `aef8d7281136dcace2a31ffaabab6d3da2261ff1d8e953982854458c195c0f7e` |
| `db-c3-end-raw/forager.db-wal` | `0778bf5245c0c6acede3ed544617fce3295a1ee320d6a7366bb601a6e6420cff` |
| `prefs-c3-end/camera_grid_preferences.preferences_pb` | `24b68c2ca7f4d89670aca701dce54732474102c3bab4d5dc82e69a95c23a1a91` |
| `prefs-c3-end/camera_orientation_preferences.preferences_pb` | `534e5373fbfcdfadf22ea42cb110d8897721c33d5a01b5982b8d673523fc1a30` |
| `prefs-c3-end/map_preferences.preferences_pb` | `17127fa1a9ac05cbb242ab4233a0f6d054cecbd3fc6f2516ebca3a13d33af137` |
| `prefs-c3-end/photo_location_preferences.preferences_pb` | `10ffcfd37b998d56d264dc329e80b0cb336a25d0b952115aeeef75d012955593` |
| `snaps.log` (at the end of this run) | `4afd8f05e8fd87a8b5303d06f61188ea4fb2ea2026df194fd03511c1f22bf99e` |
