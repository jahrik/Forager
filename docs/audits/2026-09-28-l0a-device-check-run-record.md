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
