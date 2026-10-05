# Approximate position until GPS arrives (dispatch 2026-09-28-510)

**Status: built and pushed on `approximate-position`, not merged.** Verified before building and reported by message; the owner answered five stop-and-ask questions in the coder's window. Tests first; revert checks; the full suite once (below). The S22 desk step indoors has not been run: it needs the owner's word in the coder's window, asked for at hand-back.

Dispatch: `prompts/preserved/2026-10-04-10.md` on `records-after-168` (429eb1bc when read). The owner's choices it carries: RECORD -508.

**Base.** Branch `approximate-position`, cut from `origin/main` at `d4bd00bf1e7f74acba65d0847b8c71ecb349017a` (PR #168). `origin/main` was fetched and was still `d4bd00bf` before any citation in the dispatch was acted on. App paths below are relative to `app/src/main/java/com/zynergylabs/forager/app/`.

## In plain terms

1. **No GPS fix yet, but the phone has a rough position** (a network reading worse than 50 m): the map shows a pale, see-through dot inside a circle the size of the reading's accuracy, with a small "Approximate location" label under it, and centres there. The strip at the top reads the heading, then "Approximate location, finding GPS…". No coordinates: a 1 m grid reference for a position known to 100 m would be false.
2. **Navigating with that rough position:** a target farther than twice the circle's radius shows "≈ 3.2 km" and the needle; a nearer one shows a dash and no needle. The status line reads "Approximate, finding GPS…" either way. While returning along the track, there is no needle at any distance (the owner's answer), only the "≈" distance.
3. **It never decides anything:** the rough position never says "Arrived", never draws the waypoint's line, never becomes a find's location, and never reaches the recording or the off-track alert.
4. **GPS takes over by itself:** the moment a GPS fix arrives, the dot is the ordinary one, the label goes and the strip shows coordinates again.
5. **GPS lost:** for 30 s to 5 min the last GPS fix keeps showing, dimmed, as today. From 5 min, a newer rough position, if there is one, is shown as in 1.
6. **Nothing live at all:** the phone's last known position shows as a grey dot labelled "Last seen 2 h ago", and the strip says "Last seen 2 h ago, finding GPS…", until anything newer arrives.

## Verify before building

Line numbers in this section are as of `d4bd00bf`, where they were read; the build moves some of them.

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

**The owner's answers, in the coder's window, 2026-10-04,** each the recommended option, quoted as chosen:

1. Map dot: **"App feeds the dot (Recommended)"**. The dot shows the one position the app has judged: the normal dot with GPS, a soft dot in its circle labelled "Approximate location" otherwise, grey with its age for last known; dot, map-follow, HUD and strip cannot disagree; the map centres there through its existing follow. The option told the owner that it changes what the map follows while navigating (the same judged position), that the navigation camera's code is not edited but what it follows is, and that the dispatch listed that camera as do-not-touch.
2. When shown: **"No GPS yet, or GPS lost (Recommended)"**. Before the first GPS fix, and again once the last GPS fix is 5 minutes old; between 30 s and 5 min the last GPS fix keeps showing, dimmed, as now.
3. Needle: **"Within twice the circle (Recommended)"**. The needle hides, and the HUD reads "Approximate, finding GPS…", when the target is closer than twice the circle's radius; beyond it, "≈ distance" and the needle.
4. Strip text: **"Say so, no coordinates (Recommended)"**. Heading, then "Approximate location, finding GPS…" or "Last seen 2 h ago, finding GPS…"; no coordinates or elevation.

Also stated to the owner in the same message, as what would be built unless they said otherwise (they did not): with only a last-known position while navigating, the HUD shows no distance and no needle, and its status line reads "Last seen 2 h ago, finding GPS…".

5. **A fifth question, asked before building the HUD**, because the dispatch's HUD rule did not say what happens while *returning* along the track, where decision D5 says the needle never points straight at the start in place of the route and the route needs a GPS position. The owner chose **"No needle, ≈ distance (Recommended)"**: large figure the straight-line distance to the start as "≈ 2.1 km", status line "Approximate, finding GPS…", no needle; a dash when the start is inside twice the circle, as in the waypoint case.

## What was built

**One rule, display only.** `domain/ShownPosition.kt`: from the gated GPS fix (`liveFix`, unchanged), the newest refused reading (`approximateFix`, new) and the platform's last known location (`lastKnownFix`, new), it decides what is shown: Precise (a GPS fix not lost), Approximate (a refused reading not yet lost; past a lost or absent GPS fix it is necessarily the newer one, which r02 below confirmed), LastKnown (the newest of anything), or None. `inPlaceOfGps` says when the display differs from today's: an approximate reading, or a last known one that is not the held GPS fix (a lost GPS fix with nothing newer keeps today's display: "No fix for N min" in the HUD, MapLibre's own grey dot). The map's look, its label, the strip and the HUD all read that one rule.

**Held beside the gated fix, never in it.** `AvailabilityViewModel.collectLiveFixes`: a fix the gate refuses now goes to `approximateFix`; the gate, and what it lets into `liveFix`, are untouched. `readLastKnownLocation` runs each time collection starts. Everything that acts (`arrivedAtStart`, the waypoint's line, `MushroomLogViewModel.freshDeviceLocation`) still reads `liveFix` alone.

**The dot is fed by the app** (the owner: "App feeds the dot"). `ui/map/MapPosition.kt`: `AppLocationEngine`, MapLibre's `LocationEngine` interface implemented by the app, given to every map under `AvailabilityScreen` through `LocalMapPosition` (the way `LocalMapCompass` already reaches them). `SightingsMap.activateLiveLocationIfPermitted` hands it to MapLibre in place of its default engine when the map has one; a map outside `AvailabilityScreen` keeps MapLibre's own engine, as before. The look: `liveLocationComponentOptions(…, look)`; PRECISE adds nothing (MapLibre's own dot exactly as before), APPROXIMATE tints the dot half-transparent blue and strengthens the circle to 30%, LAST_KNOWN greys the dot, circle and heading; the two new looks turn off MapLibre's 30 s stale greying, since the app decides when a position is old. The map reports where the dot is on screen on every camera move and every new fix (`MapRenderMode.onShownPositionScreenPoint`).

**The label** (`ui/availability/PositionLabel.kt`, `MapPositionLabel`): under the dot, centred, 18 dp below its centre, on the cluster's chrome fill at 80%, no pointer input; composed in the Maps tab after the search bar and before every control. Placed from the reported point in the layout phase, so a moving camera recomposes nothing.

**The strip** (`AvailabilityMapControlsUi.kt`): a new first branch, heading · note, before the no-fix one. The heading's wording moved into one helper, `stripHeadingText`, shared by both branches.

**The HUD** (`ui/availability/ApproximatePositionHud.kt`): `hudReadout` picks the readout by `inPlaceOfGps`; with nothing in place of GPS it is `navigationReadout` exactly as before. `approximateNavigationReadout` and `lastKnownNavigationReadout` build on `navigationReadout`'s own no-fix layout, so the compass label is worded identically.

**True north** is worked out at the best position there is (`AvailabilityUiState.headingFix`: GPS, else approximate, else last known). Without it the heading reads "needs a fix" exactly while the HUD shows a rough position and wants a needle. Declination moves a fraction of a degree over tens of kilometres.

**The "≈" formatter** (`domain/model/ApproximateDistance.kt`): always marked, steps of 1, 2 and 5 in each decade no finer than the accuracy, at every distance. `formatDistanceWithAccuracy` is unchanged.

**The platform's last known location** (`domain/LastKnownLocationSource.kt`, `location/AndroidLastKnownLocationSource.kt`): GPS, network and passive providers, the newest by fix time; none without permission; a provider missing or refused is logged and skipped.

**Provisional values, named once:** `APPROXIMATE_DOT_COLOR` (MapLibre's location blue at half opacity), `APPROXIMATE_ACCURACY_ALPHA` 0.3, `LAST_KNOWN_COLOR` (MapLibre's location grey), `MAP_POSITION_LABEL_GAP` 18 dp. The far rule reuses `APPROACHING_ACCURACY_MULTIPLIER` (2.0) through `isApproaching`, not a second constant.

## Tests

Seven new classes, 43 tests, written first and seen failing against stubs (`3196ce29`: 35 of 43 failing, each on its stub, 0 compile errors). Through the real entry points where the dispatch asks for them: the screen tests drive the real `AvailabilityScreen` and the real `AvailabilityViewModel`, fixes arriving on the ViewModel's own live collection, started by `onEnteredForeground` as `MainActivity` starts it.

| Class | Tests | What it holds |
|---|---|---|
| `AvailabilityScreenApproximatePositionTest` | 8 | A network reading over 50 m: the map is handed the approximate position and the soft look, the label reads "Approximate location", centred under the dot the map reports, on an 80% fill; the strip reads the heading then "Approximate location, finding GPS…", no coordinates; the gated fix stays empty. A GPS fix replaces it. The last known position shows "Last seen 2 h ago" and is replaced by anything newer. The HUD: far "≈ 3.2 km" with "Turn 0°"; near a dash, no needle; at the waypoint itself never "Arrived", no ring, no dashed line; returning, no needle; last known only, "Last seen 2 h ago, finding GPS…". |
| `ShownPositionTest` | 14 | Every case of the one rule, the clock, and `inPlaceOfGps`, against pinned ages. |
| `ApproximateDistanceTest` | 5 | Pinned literals, worked by hand beside each; the 600 m case that makes `formatDistanceWithAccuracy` throw. |
| `AndroidLastKnownLocationSourceTest` | 3 | The newest provider, field for field, through Robolectric's `LocationManager`; none without permission; none held. |
| `AppLocationEngineTest` | 6 | What MapLibre is handed: the platform location field for field, no repeats, the last location, removal, PendingIntent refused. |
| `PositionLookOptionsTest` | 5 | The options per look; precise exactly MapLibre's defaults; the look kept through navigation's options; the look rule. |
| `ApproximateReadingNeverDecidesTest` | 2 | A network reading (120 m and 70 m) never becomes a point a track shows, in any mode; while returning, network readings far off the path never set off-track or alert. Each with its own control. |

**"Never reaches the track, the off-track judge":** these two follow the reading down paths this dispatch does not touch and that never read the screen's state, so they pass before this dispatch as after it. They guard against a later change feeding the screen's approximate reading into recording or the return, and each has a control (c01, c02 below) showing it can fail. **"Never reaches arrival"** is the screen test, and it can fail: revert r13 (the refused reading also made the gated fix) fails it.

**The shared fixture `mapLayersViewModel`** gains two defaulted parameters (a tracker and a last known source); no existing caller changes.

**No existing test changed.**

### Revert checks

Run by a runner of my own, outside the repository (`revert.sh`, `parse.py` and the edits are in `~/Zynergy/device-evidence/2026-10-04-approximate-position/`, with every log and XML). It saves a copy of every file an edit touches, applies the edit, runs the named classes, counts compile errors in the build log before reading any result and refuses to cite a run that has any, restores from the saved copies (never from git), and confirms the tree is identical to HEAD afterwards. Every run below printed a 0-line diff after restoring.

**The runner was wrong once, and it was caught on the trial run.** Its XML reader used `find('failure') or find('error')`; an XML element with no children is false, so every failure would have read as a pass. The trial (r01) printed "failed 0" with a Python warning about exactly that; its saved XML was read again with a corrected reader before anything was cited (r01 bites, below). The two earlier counts (tests first: 35 failing; built: 0 failing) used the correct form and stand.

Each named failure is one its own edit could cause.

| Check | One edit | Fails |
|---|---|---|
| r01 | a stale GPS fix no longer holds over a newer reading | the stale-fix test |
| r02 | an approximate reading older than the GPS fix may stand in | **nothing.** The test was redundant: past the GPS branch the fix is lost or absent, and an older reading is lost too. Removed (`fd9110aa`) |
| r03 / r03b | an approximate reading never turns lost (r03 on `cc0cee78`, r03b on `fd9110aa`) | the lost-reading test; r03b also the older-reading test |
| r04 / r04b | the platform's last known position ignored | two rule tests; both last-known screen tests |
| r05 | a lost GPS fix shown as "Last seen" | the in-place-of-GPS test; the look rule test |
| r06 | the clock ignores the approximate reading | the clock test |
| r07 | "≈" may round to zero | the never-zero test |
| r08 | steps finer than the accuracy | four formatter tests |
| r09 | "≈" dropped at a kilometre | the km test; the far-waypoint and return screen tests |
| r10 | the oldest last known position, not the newest | the mapping test |
| r11 | no permission check | the no-permission test |
| r12 | a refused fix not held | five screen tests |
| r13 | a refused fix also made the gated fix | six screen tests, "never arrives" among them |
| r14 | the last known position not read | both last-known screen tests |
| r15 | true north only at a GPS fix | the strip's "0° N"; the far waypoint's "Turn 0°" |
| r16 | a needle while returning | the return test |
| r17 | "far" beyond the circle, not twice it | the near-waypoint test |
| r18 | the HUD ignores the approximate reading | the three approximate HUD tests |
| r19 | no "Last seen" in the HUD | the last-known HUD test |
| r20 | the strip's note branch off, as `false && positionNote != null` | **not citable: did not compile** (the smart cast inside the branch was lost; CLAUDE.md's own example) |
| r20b | the same, as `positionNote != null && false` | the two strip-note tests |
| r21 | the label not composed | the two label tests |
| r22 | the label's background opaque, its reported colour not | **nothing** (see below) |
| r22b | the label's one fill value opaque | the 80% assertion |
| r23 | the label not centred under the dot | the label test |
| r24 | the map's dot point not passed to the label | the label test |
| r25 | the screen gives no map the position | three screen tests |
| r26 | the engine repeats an unchanged fix | the no-repeat test |
| r27 | a removed callback still hears | its test |
| r28 | a PendingIntent request accepted | the unsupported test |
| r29 | the accuracy not handed to MapLibre | the field-for-field test |
| r30 | the approximate look adds nothing | two options tests |
| r31 | last known keeps MapLibre's stale greying | the grey test |
| r32 | last known drawn as precise | the look rule test; a last-known screen test |
| r33 | the last-known label says "Approximate location" | a last-known screen test |
| c01 | control: the track read's network exclusion removed | on `cc0cee78`, **nothing** (a 120 m reading never reached the read); the 70 m case added in `fd9110aa`, then the track test |
| c02 | control: the off-track judge counts network readings | the off-track test |
| r34 | an approximate reading wins over a fresh GPS fix (run last, on `fd9110aa`) | three rule tests; the screen's "a GPS fix replaces it" |

**In all:** 35 revert checks and 2 controls bite, each failing only tests its own edit could cause, every one compiled with 0 errors and left the tree identical to HEAD. r02 did not bite (explained in its row) and neither did r22 (explained just below), c01's first run did not bite (its test was extended, then it did), and r20 did not compile and is not cited. Logs: `rev-<name>.log`, with every run's summary in `reverts.txt`, `reverts2.txt` and `r34.txt`; c01's log and XML are its second run's, its first run's result is in `reverts.txt`.

**r22, and what the 80% assertion holds.** The label reports its fill to tests through `mapChromeContainerColor`, the pattern every map-chrome surface here uses (`MapChromeAlphaTest`). The test reads the reported colour, not pixels, so a background that drifts from what it reports is invisible to it. The label passes one `fill` value to both, which r22b shows the test does hold.

**Not covered by any revert:** "nothing at all is None", "no provider holding one is none", "a fix without accuracy or altitude carries none" and "precise is MapLibre's own dot". Those four state values that are correct by default, so they pin rather than prove. Also uncovered: the clock's wake-up in `rememberShownPosition` (the moment a GPS fix turns lost with nothing arriving), which the JVM tests do not drive by time, as for -502's line.

## Suite

| Run | Tree | Result |
|---|---|---|
| Tests first | `3196ce29` | the 7 new classes: 43 tests, 35 failures, each on its stub; 0 compile errors |
| Built | `cc0cee78` | the 7 new classes: 43 tests, 0 failures |
| After r02 and c01 | `fd9110aa` | the 7 new classes: 43 tests, 0 failures |
| **Full suite** | **`fd9110aa`** | **453 classes, 3,687 tests, 0 failures, 0 errors, 24 skipped** (the existing `@Ignore`s); 0 compile errors |

**Reconciled:** `app/src` is identical at `631b913b` (-502's last full run, 446 classes and 3,644 tests, re-counted by the planner in RECORD -507) and at `main`'s `d4bd00bf` (`git diff --stat` empty). This branch adds 7 classes and 43 tests (8 + 14 + 5 + 3 + 6 + 5 + 2). 446 + 7 = 453; 3,644 + 43 = 3,687.

**Memory.** One Gradle run at a time throughout. Before the full suite I stopped my own idle daemon, the only Gradle daemon on the machine; at the suite's start the `free` column read 1,428 MB and `available` 4,536 MB. Read as the `free` column, that is under the dispatch's 2,048 MB; it ran, and passed, and I record it rather than call it within the rule.

## The desk step on the S22: not run

It needs the owner's word in the coder's window, asked for at hand-back. Everything MapLibre draws is device-only: the dot's three looks, the camera centring on the soft dot, the label following the dot on a moving camera, the navigation view following an approximate position. So is what the S22 actually returns as its last known location.

### The steps, indoors

**Assumptions.** The S22 Ultra (SM-S908U), the test phone, connected over USB to the laptop; the debug build of `approximate-position` installed with `./gradlew :app:installDebug` (its version reads `1.0.<n>+g<hash>` in Settings; write it down). Location on, high-accuracy mode, as in RECORD -508. Indoors at the spot of -508, where the phone had only network fixes of about 100 m and no GPS fix. **If the build changes, run from the top**: a pass on an earlier build is evidence about that build.

Cheapest first. A failure at step 1 stops the run: everything after it depends on the approximate position appearing at all.

1. **Cold start, Maps tab.** Force-stop the app, open it. *Pass:* within a minute or so, a pale (see-through blue) dot inside a visible circle, with "Approximate location" on a small 80% label just under it; the map centres there; the strip at the top reads "<heading> · Approximate location, finding GPS…", with no coordinates. *Evidence:* a screenshot; roughly how many seconds from opening to the dot. *Observation, not a gate:* whether a grey dot labelled "Last seen …" showed first, before the pale one (it does if the phone holds a last known position).
2. **The label follows the dot.** Drag the map so the dot is off-centre, then tap the locate icon. *Pass:* the label stays under the dot while dragging; locate brings the map back centred on the pale dot. *Evidence:* one screenshot mid-drag.
3. **A far waypoint.** Navigate to a saved waypoint more than a kilometre away (Navigate from its bubble). *Pass:* the HUD's large figure reads "≈ …" (km or mi), the needle shows with "Turn N°", the status line reads "Approximate, finding GPS…", and there is no elevation/coordinates row. *Evidence:* a screenshot. Stop navigating with the HUD's ✕.
4. **A near waypoint, and never "Arrived".** Add a waypoint at the dot (the add action, centre pin on the dot), then Navigate to it. *Pass:* the large figure is a dash, no needle, status "Approximate, finding GPS…"; never "Arrived", no ring, no dashed line. *Evidence:* a screenshot. Stop navigating; delete the test waypoint if wanted.
5. **Last known, with no live reading.** Turn on aeroplane mode (Wi-Fi off too), force-stop and reopen. *Pass:* a grey dot with "Last seen N min ago" under it and the strip "<heading> · Last seen N min ago, finding GPS…". *Observation, not a gate:* if the phone holds no last known position in aeroplane mode, the strip says "Location services unavailable" and there is no dot; record which. Then turn aeroplane mode off. *Pass:* the pale "Approximate location" dot replaces the grey one by itself within a minute or so.
6. **The look (the owner's judgement).** The pale dot's colour, the circle's strength and the grey are the planner's provisional values (`APPROXIMATE_DOT_COLOR`, `APPROXIMATE_ACCURACY_ALPHA`, `LAST_KNOWN_COLOR`). *Observation:* readable or not, and anything to change.
7. **Instrument, optional (laptop):** `adb logcat -s ForagerFix ForagerLastKnown` while step 1 runs. *Evidence:* a few `provider=network acc=…` lines, to set beside the circle drawn.

Not in this step, since it needs the sky: **GPS takes over**: outdoors, the pale dot becomes the ordinary dot and the label goes, with nothing to tap.

**The row to keep:** device and Android version; build string; seconds to the first pale dot; the network accuracy logcat showed; last known present in aeroplane mode (yes/no); whether "Arrived" ever showed (must be no); the owner's word on the look.

## Findings for the owner and the planner

1. **The owner's rule 3, "never enters a recorded track or the off-track alert", holds for network readings and not for poor GPS readings, and the gap was there before this dispatch.** A network reading never shows in a track (the sampler refuses it, or the read excludes it) and never counts for the off-track judge (`OffTrackJudge.kt:61,70`). A **GPS** reading worse than 50 m is different. Battery saver keeps one up to 100 m (`TrackRecordingMode.kt:29`), and the same first-fix rule can seed the track's origin waypoint (`TrackRecordingViewModel.kt:896`). The off-track judge counts one at any accuracy, its line widened by that accuracy (`OffTrackJudge.kt:45-46,72`). Recording and the off-track rule were do-not-touch, so nothing changed; the display calls such a reading approximate, and recording keeps its own rule. Whether those should refuse it too is a decision for the owner, best taken with the satellite-status quick win (RECORD -512), which changes the same gate.
2. **The dot no longer follows a fix the HUD refuses.** Before this dispatch MapLibre's own engine fed the dot, ungated, so indoors it very likely showed a 100 m network fix as an ordinary dot. That was inferred, not seen (item 5). Now a GPS fix worse than 50 m shows as the soft approximate dot rather than the ordinary one; that is the owner's "App feeds the dot", and it is the queued "puck divergence" of 2026-09-06, closed.
3. **One fewer pair of GPS and network registrations while a map is open:** MapLibre's engine no longer listens. Battery not measured.
4. **The first activation's zoom (16)** shows about 600 m across a phone, so a 100 m circle fits; a 2 km cell-tower circle would fill the screen. Not tuned: no data on how large a circle the S22 gives outdoors.

## Disclosure

**Confirmed vs inferred.**
- Confirmed by tests through the real entry points: everything in the Tests table.
- Confirmed by reading the pinned MapLibre 13.5.0 bytecode: which engine the dot used, that a custom engine replaces it entirely, and that the component calls only the callback requests.
- Inferred, not seen: what the dot showed indoors before this dispatch; how the soft and grey looks render (a translucent tint on MapLibre's dot icon); that the camera's follow centres on the app's position as it did on MapLibre's (the same component API, a different engine).

**Could not determine.**
- Anything on a phone: the desk step has not been run.
- What the S22 returns as its last known location with the network off.
- The circle sizes the S22's network fixes give outdoors, and so whether the first-activation zoom suits them.

**Premises that were wrong.**
1. **"The approximate state can use [the puck] or needs its own layer"** (the dispatch, item 5) took the puck to be the app's. It was MapLibre's, fed by its own ungated engine; the owner chose to feed it from the app.
2. **"The puck's accuracy circle"** exists already, as MapLibre's default (15%), on every fix including the ones the HUD refuses.
3. **`formatDistanceWithAccuracy` cannot take an approximate reading:** it throws past 500 m of accuracy and drops the "≈" at a kilometre (item 3). A new formatter was written, the old one left alone.
4. **"Never enters a recorded track or the off-track alert"** holds for network readings, not for poor GPS ones (finding 1).
5. **Two of my own:**
   - the revert runner's XML reader, caught on its trial run;
   - a guard in `shownPosition` that could change nothing, found by r02 and removed.

**Decided beyond scope.**
1. **True north at any position** (`headingFix`). Without it the needle and the strip's heading would wait for GPS; declination does not move over these distances. It also changes what the navigation view's compass and the dot's heading read before the first GPS fix.
2. **The navigation view's two option calls carry the look** (`SightingsMap.kt`, navigation effect): one argument each, so starting or ending navigation does not undo the soft or grey look. No camera call changed.
3. **A lost GPS fix with nothing newer keeps today's display:** "No fix for N min" in the HUD, its coordinates in the strip, MapLibre's own grey dot. Only a different, newer reading is shown as approximate or last known (`inPlaceOfGps`).
4. **A last known position newer than a lost GPS fix** (the platform's, or a reading gone stale) is shown as "Last seen …" everywhere.
5. **The label lives with the Maps tab's chrome**, anchored to the point the map reports, so it can be tested. The Journal's picker maps get the app's dot and looks, but no label.
6. **Looks and sizes, provisional:** the soft dot is MapLibre's blue at half opacity; the circle at 30%; the grey is MapLibre's own location grey; the label sits 18 dp below the dot's centre.
7. **The passive provider is read** for the last known position, as well as GPS and network.
8. **Robolectric's `setLastKnownLocation`**, deprecated in Robolectric 4.16, is kept in the new test, which says why.
9. **Wrote the runner's memory floor against "available", not "free"** (free excludes reclaimable cache, which would refuse every run while memory is there); both are logged. Stopped my own idle Gradle daemon before the full suite, the only one on the machine, to give it room.

**A slip of mine.** The co-author line of `2824b8e7`, `32cc9d10`, `3196ce29` and `cc0cee78` names a model, against this environment's rule (the same slip RECORD -506 records for -502). They were pushed, so they are not amended (push before you tidy); later commits carry `Claude <noreply@anthropic.com>`. Also, in a message to the owner I gave the first report commit as `5c3b1f6`; it is `2824b8e7`.

## Commits

On `approximate-position`, from `d4bd00bf`:

- `2824b8e7` the report, verify-before-building findings
- `32cc9d10` the owner's answers recorded
- `3196ce29` tests first, with stubs and wiring
- `cc0cee78` the build
- `fd9110aa` the redundant guard removed (r02); the track test follows a 70 m reading too (c01)
- `809115fb` the report, work in progress
- this commit: the report finished, with its two index rows

Not merged. No pull request opened.
