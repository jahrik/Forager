# 2026-09-28: Forager Navigator plan, status audit

The owner asked which parts of `docs/plans/forager-navigator-plan.md` are
unfinished. `docs/plans/README.md` has said since 2026-08-25 that "Phase
1.5/2/3/4: status not re-audited in this pass", and no later record audits
them. This report does. It was written in a planner session at `pre-main`
`352b708` (`origin/main` is `faf2f88`, 18 commits behind). It is read-only.
Nothing was built and nothing was decided. Every file:line is at `352b708`.
App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`
unless they start with `app/`, `docs/`, `server/`, `gradle/` or `README.md`.

## Method, and how far to trust each row

Three read-only sweeps split by phase (1a to 1c with §4 and §5; 1.5 with §6
and §0; 2 to 4 with the look-alike page) read the code with `git grep` and
`git show` against `origin/pre-main`. The planner then did two things:

- **Re-ran eight of the sweeps' claims.** No `AlarmManager`, check-in or
  overdue code in `app/src/main`. No `SiteContext` or `FieldTrip`.
  `returnWalkingTime(` appears only at its definition. No `GpxCodec.decode`
  call in `app/src/main`. `HIGH_ACCURACY` is hard-coded at
  `MainActivity.kt:521`. `f08703a` is an ancestor of `pre-main`, dated
  2026-09-01. No `isIgnoringBatteryOptimizations` or `REQUEST_IGNORE_BATTERY`
  anywhere in `app/src`. No `raster-dem` or `hillshade` except the comment at
  `ui/map/MapChrome.kt:222`.
- **Re-read the cited line ranges** marked † below.

Unmarked rows are the sweeps' reading. The planner did not re-read them.
They are reported as read in code by the sweep, not as re-verified.

## Summary

| Phase | State |
|---|---|
| 1a Field capture | Mostly built. GPX import and battery modes are partial. |
| 1b MapLibre migration | Built, except the pre-built regional extract path. The self-hosted endpoint is the only offline path, and it is live. |
| 1c Converge | Partial. Check-in timer not built. Offline readiness has none of the planned states. |
| §4 conditions | The off-track alert ships without either of its conditions. |
| 1.5 Differentiator, §6 privacy | Not built. Only entry-to-track and entry-to-waypoint links exist, on a different entity. |
| 2 Terrain and hydrography | Not built. |
| 3 Access and land context | Not built. |
| 4 Trip and record | Not built, except the post-trip review, which the Journal reshaped without a recorded decision. |
| Toxic-look-alike page | Not built, never scoped. |

## Phase 1a: field capture

| Item | Status | Evidence |
|---|---|---|
| Foreground location service and notification | Built | `service/TrackRecordingService.kt:56`, `startForeground` at `:243-248`, notification with a Stop action at `:173-180` and `:204-228`. Started from `MainActivity.kt:364-381`. |
| Track sampling and accuracy filtering | Built | `domain/LocationSampler.kt:29-41`, used at `TrackRecordingService.kt:111-120`. Batched writes (20 points or 30 s, `:285-286`). |
| Battery modes | Partial | Three modes at `domain/model/TrackRecordingMode.kt:27-29`. They change only the sampler's storage thresholds. The OS request is fixed at 1 s (`location/AndroidLocationTracker.kt:71-74`). Production hard-codes `HIGH_ACCURACY` (`MainActivity.kt:521` †). No selector in the UI. |
| Track and waypoint schema plus migrations | Built | `data/local/Migrations.kt:101` (`MIGRATION_4_5`), later 12→13, 13→14, 14→15. `ForagerDatabase.kt:159` † is version 15. |
| Track statistics | Built | `domain/ComputeTrackStatisticsUseCase.kt:39`, read by `CartographyViewModel.kt:538,545`. The Records list shows a point count only (`ui/track/TrackExportPanel.kt:130-140`). |
| GPX export | Built | `domain/GpxCodec.kt:68` ← `export/TrackGpxExporter.kt:50` ← `ui/track/TrackExportPanel.kt:164`, reached from `ui/log/RecordsTab.kt:237`. |
| GPX import | Partial, not reachable | Decoder only, `GpxCodec.kt:179`. No caller in `app/src/main` †. `docs/audits/2026-09-07-track-distance-display-pulse.md:142` says the same. |
| Coordinate formats and MGRS | Built, MGRS and decimal degrees only | `domain/MgrsConverter.kt:39`, `ui/availability/AvailabilityPureFunctions.kt:98-106`. The tap toggle is `remember` state and is not saved (`AvailabilityCompactMapUi.kt:577-578`). No DMS or UTM. |
| Compass plumbing | Built | `sensor/AndroidCompassProvider.kt:61`, `AppContainer.kt:136`. |
| Return-to-start bearing | Built | `domain/ComputeReturnToStartUseCase.kt:8-23` (bearing, straight-line distance, elevation difference), called at `TrackRecordingViewModel.kt:662`. |

## Phase 1b: MapLibre migration

| Item | Status | Evidence |
|---|---|---|
| Basemap on MapLibre | Built | `ui/map/SightingsMap.kt`. `MapSlot.kt:322` `SightingsMapSlot` is the default. MapLibre 13.5.0 pinned at `gradle/libs.versions.toml:37`. |
| Overlays re-implemented | Built | GeoJSON layers from `SightingsMap.kt:630`. |
| Dashed connector | Superseded | Re-implemented on MapLibre, then deleted with the foraging-areas feature in `f08703a` † (2026-09-01, "per owner sign-off" in the message). No audit doc records that decision. |
| Pre-built regional extract path | Not built | `domain/OfflineMapRepository.kt` has one `download()` path. No static-extract code. |
| Self-hosted endpoint behind the region picker | Built, and live | Worker URL at `map/OfflineStyle.kt:18`, used by `MapLibreOfflineMapRepository.kt:97-98`, wired at `AppContainer.kt:188`. No feature flag. |
| osmdroid removed | Built | No `org.osmdroid` reference on `pre-main`. |

The plan's owner refinement (2026-08-18, `forager-navigator-plan.md:139-150` †)
was "build both, activate one": the extract path is active, and the endpoint
ships dark until the app can pay for hosting. The build is the reverse. Only
the endpoint exists, and it is what users get. The activation decision is
still an unchecked box (`forager-navigator-plan.md:441` †).

## Phase 1c: converge

| Item | Status | Evidence |
|---|---|---|
| Track breadcrumbs | Built | `SightingsMap.kt:675,735,1013-1018`, fed from `MainActivity.kt:527`. |
| Waypoint markers | Built | `SightingsMap.kt:685-686,736`, fed from `MainActivity.kt:528`. |
| Offline readiness screen | Partial | `AvailabilityOfflineMapsUi.kt:112`. Rows show "Stale" (`:371`) and "Ready to zoom" (`:448`). `ui/availability/OfflineMapStatus.kt:11-20` † is only Idle, Downloading, Succeeded, Failed. No available, partial, not downloaded or not covered state. No extract-versus-pack distinction, since only one path exists. |
| Return-to-vehicle screen | Built as a HUD, compact layout only | `NavigationHud` at `AvailabilityCompactMapUi.kt:1072`, controls at `:971`. The wide layout (`AvailabilityWideLayoutUi.kt`) has neither. |
| Off-track alert | Built, fires only in returning mode | `DetectOffTrackUseCase.kt:15-20` → `TrackRecordingViewModel.kt:662-675` → `alert/AndroidAlertDelivery.kt:41,106-107`. |
| Overdue check-in timer | Not built | No `AlarmManager`, check-in or overdue code †. `README.md:911-912` says the timer "isn't started". |

## Plan §4 and §5 conditions

| Item | Status | Evidence |
|---|---|---|
| Setup-time check that the notification fires, routing to the battery-optimisation exemption | Not built | No battery-exemption API anywhere †. The nearest thing is a one-time warning at trip start (`AlertAudibility.kt:40-55`, called at `TrackRecordingViewModel.kt:199`). It reads ringer, Do Not Disturb and notification state. It does not test-fire and does not route to the exemption. |
| Wording: a local phone reminder, not a monitored service | Not built | Off-track strings at `app/src/main/res/values/strings.xml:15-17` carry no such wording. |
| No ETA on the compass screen | Held in the UI, direction changed since | See "Walking time" below. |
| `ForagingAreaLabels` extended into one navigation-disclaimer source | Not built, base deleted | The file went in `f08703a` †. No disclaimer source exists. |
| Track retention decision | Decided; deletion not reachable | `domain/model/Track.kt:7-12` †: no automatic pruning, "deleted only by explicit user action". `DeleteTrackUseCase` is constructed at `AppContainer.kt:255` and nothing reads it, so a user cannot delete a track. |
| Measured insert cost | Partial | `RoomTrackRepositoryTest.kt:136-160` (Robolectric, in-memory) compares batched and single inserts for 1,000 points and asserts only batched ≤ single. No figure is recorded anywhere, and nothing was measured on a device. |

**Walking time.** Plan §4 cut any time estimate, and
`domain/model/ReturnToStartInfo.kt:4-8` † still cites that cut. Later owner
rulings point the other way: walk-back time "should be implemented first"
(`docs/audits/2026-09-11-sundown-decisions-and-walkback-sequencing.md:26` †),
and the average pace is displayed from the start
(`docs/audits/2026-09-11-sundown-countdown-prebuild-report.md:280` †, recorded
"rather than acted on" at `:274` †). `domain/ReturnWalkingTime.kt` is built
and tested, and it has no production caller †. **That is deliberate:**
"No surface exists yet, on purpose … the alert brings the surface"
(`ReturnWalkingTime.kt:20-22` †). The estimate measures along the walked
track, not a straight line, and the owner accepted it "on the condition that
it is labelled walking time and never padded" (`:17-18` †). So §4 is
superseded in intent by owner rulings, but no record reverses it by name.

## Phase 1.5 and §6 privacy

| Item | Status | Evidence |
|---|---|---|
| `SiteContextSection` (slope, aspect, elevation band, canopy, moisture, disturbance, burn year) | Not built | No match in `app/src/main` †. `MushroomLogEntry` still has seven sections (`domain/model/MushroomLogEntry.kt:42-48` †). |
| Entries linked to track points and waypoints | Partial, different shape | Cartography entries link whole tracks (`data/local/CartographyEntryEntity.kt:70-82`) and waypoints (`:86-97`), written by `CartographyEntryDao.upsertEntryWithRefs` and reachable from `CartographyEntryEditScreen.kt:275-276`. No track-point link exists. A find (`MushroomLogEntry`) links only to an offline region. |
| Capture position vs corrected position | Not built | A find stores one `foundAt` (`MushroomLogEntry.kt:40` †). "Change Location" overwrites it (`JournalTab.kt:316`, `LogPanel.kt:283`). A camera photo's own coordinates (`domain/model/LogPhoto.kt:32-37`) seed `foundAt` only when it is null (`MushroomLogViewModel.kt:866-867`). |
| Sensitive-location controls | Not built | No privacy or sensitivity field on the entry. Related but different: the "Automatically Save Location to Photos" setting (`AvailabilitySettingsUi.kt:497-502`) and GPS-EXIF scrubbing of camera photos. |
| Privacy-safe export (confirmation, whole-bundle rounding, find export without its track, one rounding function) | Not built | The only export is GPX. One tap goes straight to the share sheet (`TrackExportPanel.kt:112-117` †). Coordinates and timestamps are written in full (`GpxCodec.kt:133-135` †). No find or journal export exists (`app/src/main/AndroidManifest.xml:112`, `docs/legal/privacy-policy.md:49`). No rounding function exists. |
| §0 carry-over: replace the README's "Not yet verified" caveats with the real findings | Not done | The caveats are still there (`README.md:679-680` †, `:694-699` †). The plan's own checkbox is open (`forager-navigator-plan.md:431` †). Some outcomes are recorded in a different section, `README.md:1164-1181`. |

**Three positions on tracks leaving the phone, none reconciled.**
- The navigator plan §6 lets tracks be exported, rounded with the rest of
  the bundle and omittable from a find export.
- `docs/plans/journal-trips-and-offline-regions.md:220` † says "Tracks never
  leave the device under any setting". That document is design only (`:9` †).
- The shipped GPX export (`e44e455`, 2026-08-29) sends full-precision tracks
  with no confirmation.

No record chooses between them.

## Phase 2: terrain and hydrography

Nothing is built. There is no hillshade, slope or aspect layer: the map adds
only GeoJSON layers (`SightingsMap.kt:636-704`), and the one mention is a
comment at `MapChrome.kt:222` †. The default Topographical basemap is
OpenTopoMap's raster with its own baked-in hillshading (`ui/map/Basemap.kt:151-163`),
which is not the plan's layer. There is no elevation profile. Gain and loss
are computed (`domain/model/TrackStatistics.kt:16-17`) but nothing outside
`domain/` reads them. Water in the offline style comes from the Protomaps
archive, not NHD (`server/pmtiles-worker/src/offline-style.json:380-413`).
There is no 3DEP → `gdaldem` → PMTiles pipeline in `server/` or `scripts/`.
It exists only as plan text (`docs/plans/maplibre-migration.md:319-372`).

## Phase 3: access and land context

Nothing is built. There is no vector pipeline beyond the manual basemap
`pmtiles extract` recipe (`server/pmtiles-worker/README.md:56-82`). There is
no PAD-US layer, no USFS roads, trails or MVUM, no designations, no layer
cards, no "unavailable for the region" state and no "not covered" readiness
state. `domain/` has no point-in-polygon; `OfflineTileMembership.kt:28` is a
tile-box test for offline regions.

## Phase 4: trip and record

| Item | Status | Evidence |
|---|---|---|
| `FieldTrip` entity (party, emergency contact, checklists, gear, weather snapshot, brief) | Not built | None of the 15 entities at `ForagerDatabase.kt:141-161` is one. `PlannedTrip` is untouched (`domain/model/PlannedTrip.kt:19-24`). |
| Checklists and regulation cards | Not built | No match. |
| Pre-trip weather snapshot | Not built as a snapshot | Live, uncached trip windows and forecast exist (`AvailabilityViewModel.kt:590,623`), keyed to a search region and attached to no trip. |
| Exportable trip brief | Not built | The only share paths are GPX and the crash report. |
| Fire perimeters, LANDFIRE | Not built | No match in app or server. |
| Post-trip review (tracks, waypoints, finds, photos in one record) | Reshaped by the Journal | A day-keyed `DerivedTrip` that stores nothing (`domain/model/DerivedTrip.kt:5-12` †, quoting the owner: "a trip is derived, not stored") feeds the authored `CartographyEntry` (`domain/model/CartographyEntry.kt:5-11,50-73`). Reachable from the Journal tab (`JournalTab.kt:417` → `CartographyScreen`). |

No document says the Journal model replaced the plan's `FieldTrip` or its
post-trip review. `journal-trips-and-offline-regions.md` proposed its own
`RecordedTrip` (`:329-331`) and never mentions `FieldTrip`.
`journal-redesign.md` is UI only. The "one field record" part therefore
exists. Party, emergency contact, checklists, gear, weather snapshot and trip
brief have no successor and no decision closing them.

## Toxic-look-alike reference page

Not built, and never scoped after the plan recorded it as planned and
unscoped (`forager-navigator-plan.md:351-374`). The species index carries no
look-alike or edibility field (`FungiTaxonEntity.kt:18-23`;
`data/species-index/README.md:53`: "No edibility, habitat, range, or season
fields — deliberate").

## Decisions open for the owner

Nothing in the record settles these. Each is a stop-and-ask for whoever
dispatches Navigator work.

1. **`FieldTrip`.** Keep it as planned, fold its remaining fields into the
   Journal, or drop them.
2. **Walking time versus §4's ETA cut.** Record the reversal by name, so the
   plan and `ReturnToStartInfo.kt:4-8` stop contradicting the owner's
   2026-09-11 rulings.
3. **Offline paths.** Build the pre-built extract path the plan made primary,
   or record that the endpoint is the path and take the activation decision
   (`forager-navigator-plan.md:441`) now. Either way, this is a hosting cost
   the commercial release carries.
4. **Tracks leaving the phone.** Pick one of the three positions above.
5. **Off-track alert conditions.** §4 said the alert ships only with the
   test-fire check and the honest wording. It ships without them. Build them,
   or record that the condition was waived.

## Stale in the record

- `docs/plans/README.md:17`, the Navigator row: "Phase 1.5/2/3/4: status not
  re-audited in this pass". This report is that audit.
- `README.md`'s "Not yet verified" section (§0 above).
- `ForagingAreaLabels`, which §4 and §6 build on, no longer exists (`f08703a`).
- The plan's §1 table names osmdroid classes that are deleted. That is
  historical and not wrong as a record of 2026-08-18.

## Incidental findings

- **User-dropped waypoints are not linked to the recording they were
  dropped in.** `Waypoint.kt:12-13` documents `trackId` as "the Track this
  waypoint was dropped while recording". Only the automatic origin and end
  waypoints set it (`TrackRecordingViewModel.kt:284-289`, `:533-538`).
  A dropped waypoint goes through `MainActivity.kt:531` to `addWaypoint`
  (`TrackRecordingViewModel.kt:605-607`) with no `trackId`.
- **`Basemap.kt:73` † claims the coverage limit is "shown next to the
  choice".** `BasemapCoverage` is read only by `BasemapTest.kt:56-73`, and
  `MapModePicker` shows only the label (`MapChrome.kt:179-187`).
- **Two cited documents are not in the repository**, on any branch:
  `amendment-2b-entry-definition.md` (cited at `CartographyEntry.kt:5-11`)
  and `dispatch-return-estimate.md` (cited by the 2026-09-11 reports).
- **A name clash to avoid.**
  `docs/audits/2026-09-11-listed-features-phase0-inventory.md:136,208-209`
  says "Phase 2 … looks built" and "Phase 4 is partly built". Those are the
  store-description dispatch's phases, not this plan's.

## Disclosure

**Confirmed versus inferred.** Every "not built" verdict comes from code
searches that returned nothing, and from reading code, not from documents.
The rows marked † and the eight re-run claims were checked by the planner.
The rest are the sweeps' reading. Hardware claims (the connector read as
dashed, the basemap renders) are document claims, not observed here.

**Could not determine.**
- Whether the owner's 2026-08-18 hardware confirmation in plan §0 referred to
  the osmdroid renderer. `3269c4d` (2026-08-20) says nothing about MapLibre
  had been seen on hardware.
- What `amendment-2b-entry-definition.md` says about trips, since it is not
  in the repository.
- Whether any Navigator item was built on a branch not merged into
  `pre-main`. The Phase 2 to 4 sweep searched every branch for `FieldTrip`
  and the terrain terms and found nothing. The other sweeps read `pre-main`
  only.

**Premises that were wrong.**
- `docs/plans/README.md` says Phase 1a and 1b "landed". That is broadly
  right, but GPX import and battery modes are partial, and 1b's primary
  offline path was never built.
- The planner's chat summary before this report said the walking-time
  estimate is "built and tested but nothing displays it", with no reason
  given. The absence is deliberate and waits on the turnaround alert
  (`ReturnWalkingTime.kt:20-22`). That is corrected above.

**Decided beyond scope.** Nothing. The report was delivered to the owner as
a file outside the repository, to be filed by the working coder.

## For the coder filing this

- Path: `docs/audits/2026-09-28-navigator-plan-status-audit.md`.
- `docs/audits/README.md` is a serialization point. If the append conflicts,
  merge (never rebase) and keep every row. Proposed row:

  | 2026-09-28 | **Forager Navigator plan, status audit: 1a/1b mostly built, 1c partial, 1.5 to 4 not built, five owner decisions open** ([report](2026-09-28-navigator-plan-status-audit.md)). Read-only, at `pre-main` `352b708`. Built: foreground recording, sampling, schema, statistics, GPX export, MGRS, compass, return bearing, MapLibre basemap and overlays, breadcrumbs, waypoints, the off-track alert, the return HUD (compact only). Partial: GPX import (no caller), battery modes (hard-coded), offline readiness (no planned states). Not built: the check-in timer, the pre-built extract path (the endpoint the plan kept dark is the only path and is live), the §4 alert conditions, all of 1.5 and §6, Phases 2 and 3, and Phase 4 apart from the post-trip review the Journal reshaped. Open for the owner: `FieldTrip`, walking time versus §4's ETA cut, offline paths, tracks leaving the phone, and the alert conditions. |

- The Navigator row in `docs/plans/README.md` still says Phases 1.5 to 4
  were "not re-audited". Point it at this report. Whether that is a dated
  addition to the row or a new line is the filing planner's call.
- Nothing in this report changes code, and it asks for no build.