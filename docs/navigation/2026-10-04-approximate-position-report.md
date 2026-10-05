# Approximate position until GPS arrives (dispatch 2026-09-28-510)

**Status: verify-before-building done and reported to the owner; build not started, waiting on the owner's answers to the stop-and-ask below.** This file is committed now so the findings are not only in a session transcript (CLAUDE.md, "Push before you tidy"); the build sections are added when the build happens.

Dispatch: `prompts/preserved/2026-10-04-10.md` on `records-after-168` (429eb1bc when read). The owner's choices it carries: RECORD -508.

**Base.** Branch `approximate-position`, cut from `origin/main` at `d4bd00bf1e7f74acba65d0847b8c71ecb349017a` (PR #168). `origin/main` was fetched and was still `d4bd00bf` before any citation in the dispatch was acted on. App paths below are relative to `app/src/main/java/com/zynergylabs/forager/app/`.

## Verify before building

### 1. The 50 m gate and every consumer of a fix that passes it

**Where:** `domain/LiveFixGate.kt:68` (`LIVE_FIX_MAX_ACCURACY_METERS = 50f`) and `:71-74` (`acceptLiveFix`; a `null` accuracy passes). Applied once, in `ui/availability/AvailabilityViewModel.kt:266-277` (`collectLiveFixes`), which writes the passing fix to `AvailabilityUiState.liveFix` (`ui/availability/AvailabilityUiState.kt:280`). A refused fix is dropped and the previous one held.

**Readers of `liveFix` (every one in `main/`, by grep for `liveFix`, `liveLocation`, `liveAltitudeMeters`):**

| Reader | What it does | Acts on position? |
|---|---|---|
| `ui/availability/NavigationHud.kt:509-644` `navigationReadout` (called `:225`, fed `AvailabilityCompactMapUi.kt:976`) | HUD distance, needle, "Approaching", "Arrived", stale/lost, elevation, coordinates | Display, except "Arrived" (via `arrivedAtStart`) |
| `NavigationHud.kt:440-443` `arrivedAtStart`, called `AvailabilityCompactMapUi.kt:594` | Arrival: the HUD's "Arrived" and the map's ring | **Yes**: arrival |
| `NavigationHud.kt:450-488` `straightLineToTarget` / `nextStraightLine`, called `AvailabilityScreen.kt:997` | T8's dashed line to a waypoint, fading and grey rules | Display (T7/T8 line rules; not to be touched) |
| `AvailabilityScreen.kt:989-993` `millisUntilFreshnessChanges` | Ticks the line's fade | Display |
| `AvailabilityUiState.kt:333,336` `liveLocation` / `liveAltitudeMeters` → `AvailabilityCompactMapUi.kt:871-872` | Compass strip's coordinates and elevation, or "Location services unavailable" (`AvailabilityMapControlsUi.kt:444-457`) | Display |
| `ui/map/TrueHeading.kt:74-93` (via `AvailabilityCompactMapUi.kt:589`, `AvailabilityScreen.kt:1674` → `SightingsMap.kt:332`) | Declination at the fix for true north; `NeedsFix` without one | Display (heading) |
| `MushroomLogViewModelFactory.kt:39` → `ui/log/MushroomLogViewModel.kt:403-419` `freshDeviceLocation` | Stores a new find's location | **Yes**: writes data |
| `AvailabilityCompactScaffold.kt:1152` → `ui/log/FindLocationPickerRegion.kt:21-22` | Where the find-location picker opens | Display (picker's first view) |

**Streams that never read `liveFix`:** the recording service collects `LocationTracker.fixes` itself (`service/TrackRecordingService.kt:138-156`): every raw fix goes to `ReturnWatch.onFix` (`:142`), so to the off-track judge, which drops network readings by the timestamp rule itself (`domain/OffTrackJudge.kt:61,70`); the sampler decides track points (`:143`, `domain/LocationSampler.kt`). `TrackRecordingViewModel` collects its own stream (`ui/track/TrackRecordingViewModel.kt:880-905`): the origin waypoint and `lastGatedFix` pass the recording mode's own gate (`:896`), and `RouteHome` reads `lastGatedFix` (`:855-867`). None of the three reads `AvailabilityUiState`. So a value the availability collector produces **cannot reach** the track, `ReturnWatch`, the off-track judge or `RouteHome` unless code is added to carry it there.

**The map puck does not read `liveFix` at all.** See item 5.

**So the display-only paths an approximate reading may reach:** the map's dot, circle and label; the HUD's distance, needle and status line (not "Arrived"); the compass strip's text. It must not reach `liveFix` itself, since `arrivedAtStart`, the waypoint line and `freshDeviceLocation` all read that field. **The proposed shape:** `liveFix` keeps exactly its present meaning (gated, precise), and the approximate reading is a separate field beside it, read only by the display paths above. Nothing that acts reads the new field.

### 2. Where network fixes arrive, how they are told apart, and the rules this sits beside

- **Arrival:** `location/AndroidLocationTracker.kt:71-75` requests GPS and network, each enabled one, at 1 s; `:89-97` maps every `Location` to `LocationFix.Update`, which **carries no provider** (`domain/LocationTracker.kt:45-53`). The provider is only logged (`:53-61`, tag `ForagerFix`).
- **Telling them apart:** only the timestamp rule `TrackPoint.isNetworkProviderFix()` (`domain/NetworkProviderFix.kt:48`, milliseconds not zero), defined on `TrackPoint`, applied at the track read seam and in `OffTrackJudge`. The live collector does not apply it: it gates on accuracy alone, so a 12 m network fix passes today and becomes `liveFix`.
- **Proposed classification:** "approximate" is **a fix the live gate refuses** (accuracy over 50 m), whatever its provider; no provider or timestamp test. That keeps one rule (the gate's) deciding what is precise, and the satellite-status quick win (RECORD -512), which changes that gate after this, then changes both together.
- **The rules this sits beside:** `domain/NavigationReadout.kt:85-92`: fresh under 30 s, stale 30 s to 5 min (distance dimmed, "Last fix N s ago"), lost from 5 min ("No fix for N min", distance and needle withheld). -502's line (`NavigationHud.kt:479-488`): bright when fresh, faded (0.4) when stale, kept grey once lost, none before the first fix. `LiveFixGate.kt:40-46` records an earlier refusal to let a refused fix through once the held one is stale ("a confident lie ... do not add it here without field data"). -508 is the owner overriding that for a **labelled** display that never decides; the gate itself and the held fix are unchanged. **Open** (stop-and-ask, below): whether the approximate reading also shows once the held precise fix is *lost*, or only when there has been none.

### 3. "Far" for the HUD's "≈"

**Proposed rule:** with an approximate reading, the target is far when its distance is **more than twice the reading's accuracy**, using the existing `APPROACHING_ACCURACY_MULTIPLIER = 2.0` (`domain/NavigationReadout.kt:51`) by reference, not a second constant. Far: "≈ distance" and the needle. Not far: no needle, "Approximate, finding GPS…". Why twice and not once: at a distance equal to the accuracy the bearing can be wrong by up to 90°, at twice by up to 30°; twice is the ratio at which the HUD already hides the needle for a precise fix, so one rule decides the needle for both kinds of reading and the two cannot drift. Provisional, like its source. The owner's words were "inside the circle of uncertainty"; with twice, the needle also hides when the target is just outside the drawn circle. That difference is put to the owner below.

**A defect this exposed in the formatter, not a choice:** `formatDistanceWithAccuracy` (`domain/model/DistanceUnit.kt:117-138`) assumes accuracy at most 50 m. Its coarsest step is 500 m (1,000 ft), so a reading of, say, 600 m accuracy with a target 800 m away finds no step and throws (`first { }`, `:123` and `:130`). It also drops the "≈" at 1 km / a quarter mile and over. An approximate reading needs its own formatter (always "≈", rounded no finer than the accuracy, at every distance). A new function; the existing one is unchanged.

### 4. The last known position without Play services

`android.location.LocationManager.getLastKnownLocation(provider)` per provider (GPS, network, passive), API 1, no Play services; its age is now minus `Location.getTime()`, the same wall-clock basis as `LocationFix.ageMillis` (`domain/LocationTracker.kt:91`). The app never calls it today (grep: no `getLastKnownLocation` in `main/`). MapLibre's puck engine does (item 5). Proposed: a new owned interface in `domain/`, implemented over `LocationManager` in `location/`, read once when the map opens with no live reading; the newest of the providers' answers is shown greyed with its age until any live reading arrives. Never enters `liveFix`. **Not verified:** what the S22 returns for it indoors (device only).

### 5. How the puck and its accuracy circle are drawn today

Read from the code and from the pinned MapLibre artifact (`13.5.0`, `gradle/libs.versions.toml:37`) with `javap -c` and its `res/values/values.xml`:

- **The puck is MapLibre's `LocationComponent`, fed by MapLibre's own engine, not by the app.** `ui/map/SightingsMap.kt:1786-1792`: `activateLocationComponent(... .useDefaultLocationEngine(true) ...)`. In 13.5.0 the default engine is `MapLibreFusedLocationEngineImpl` (`LocationEngineDefault.getDefaultLocationEngine`). The component asks it for high accuracy every 1 s (`LocationComponent` constructor: interval 1000, priority 0). It listens to GPS, and to network too when GPS is the chosen provider (`shouldStartNetworkProvider`). Each fix passes to the puck if `Utils.isBetterLocation` says so: Android's classic rule (2 minutes newer wins, more accurate wins, 200 m tolerance for the same provider). **There is no accuracy ceiling.** On start it seeds the puck from the best last-known location of every provider (`getBestLastLocation`, from `LocationComponent`'s `getLastLocation` call).
- **What it draws by default** (the app overrides none of these; `SightingsMap.kt:1687-1716` sets only tracking gestures, the animation multiplier and the layer position): an accuracy circle at 15% of `#4A90E2` (`maplibre_accuracyAlpha 0.15`), and a stale state that greys the dot after 30 s without an update (`maplibre_enableStaleState true`, `maplibre_staleStateTimeout 30000`, `maplibre_user_icon_stale`).
- **Consequence, inferred, not seen:** indoors on the S22, with only 100 m network fixes arriving, the puck very likely already showed the network position, drawn as a normal blue dot with a faint circle and no label, and the map in follow mode centred on it, while the strip and HUD said "Location services unavailable". The S22 records (RECORD -507, -508) do not say what the puck did. This is the "puck divergence" the location-accuracy pre-build report flagged and queued, never confirmed on a device (`docs/audits/2026-09-06-location-accuracy-prebuild-report.md:207,222`).
- **The 80% rule for the label:** a label over the map is chrome. Fill at `MAP_CHROME_OVER_MAP_ALPHA` (0.8, `ui/map/MapChrome.kt:268`), content opaque, `mapChromeContainerColor` set beside its test tag so `MapChromeAlphaTest`'s pattern can assert it.

**Can the approximate state use the puck, or does it need its own layer?** Either works technically. They differ in what the user can see disagree, and one of them changes what the map's follow (including the navigation view's) is fed. That is a decision, put to the owner below.

## Stop-and-ask, put to the owner in the coder's window

1. **How the approximate dot reaches the map** (item 5): (A) feed the puck from the app's own classified position, (B) an own layer beside MapLibre's puck, or (C) restyle MapLibre's puck in place. Recommended: A.
2. **When the approximate reading shows** (item 2): only before the first precise fix, or also once the held precise fix is lost (5 min and over). Recommended: both.
3. **"Far" for "≈"** (item 3): twice the accuracy (recommended) or the drawn circle itself.
4. **What the strip says while the position is approximate or last-known**, which the dispatch does not cover.

The answers and what was built follow when the build happens.
