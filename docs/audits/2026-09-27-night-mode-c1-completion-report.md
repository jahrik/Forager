# Night mode, colour build C1 (basemap): completion report

Date: 2026-09-27 (UTC). Branch `night-mode`, cut from `origin/pre-main` `d0ae612` (the #133 merge).
Dispatch: `prompts/preserved/2026-09-27-01.md`. Record: intent `2026-09-27-24`. The planner's rulings
on the build's one stop are quoted verbatim in that intent (planner log line 2411). Merge into `pre-main` is authorised
without a device check. The owner deferred device checks to beta testing, so **nothing here was run on a
device.**

## What was wrong

Night Maps did nothing. `SightingsMap`'s style-swap effect was keyed on
`(mapLibreMap, basemap, mapPalette, useOfflineTiles)`, with no night key. The value it compared to decide
whether to reload (`AppliedMapStyle(basemap, palette, useOfflineTiles)`) had no night component. A toggle
therefore never relaunched the effect. When the effect did relaunch for another reason, the comparison
said nothing had changed. The night paint itself, desaturation and contrast only, did not invert
anything. The offline style ignored night entirely. On cold launch, the preference was read
asynchronously and nothing ordered it against the map's first style.

## What was built

| Item | Change | Where |
|---|---|---|
| (a) | `AppliedMapStyle` gains `night`, holding **effective night**: `effectiveNight(basemap, nightMode, useOfflineTiles) = nightMode && (useOfflineTiles \|\| basemapTakesNightPaint(basemap))`. The effect's keys gain `nightMode`, and `requested` carries effective night. | `BasemapStyles.kt`, `SightingsMap.kt` |
| (b) | `NIGHT_RASTER_PAINT` is exactly `raster-brightness-min 1`, `raster-brightness-max 0`, `raster-hue-rotate 180` (V1). `basemapTakesNightPaint` is false only for `USGS_IMAGERY_ONLY`, and `styleJsonFor` reads it. | `BasemapStyles.kt` |
| (c) | `AvailabilityUiState.nightModeMapsLoaded` (default false). `loadNightModePreferences` sets it true with the value on success. On failure it logs (unchanged message) and sets it true with `nightModeMaps` false. `requestedMapStyle(...)` returns `null` until loaded. The effect returns on `null` and has `nightModeLoaded` in its keys. | `AvailabilityUiState.kt`, `AvailabilityViewModel.kt`, `BasemapStyles.kt`, `SightingsMap.kt`, `MapSlot.kt`, `AvailabilityScreen.kt` |
| (d) | Offline night by post-load recolour. See the section below. | `NightColour.kt` (new), `SightingsMap.kt` |
| (e) | Stale night comments and docs corrected. See the section below. | as listed |

### The gate's hops (planner ruling, option A)

- `AvailabilityUiState.nightModeMapsLoaded`
- → `AvailabilityScreen.kt:787`: `MapRenderMode(basemap, night = isNightMode, nightModeLoaded = uiState.nightModeMapsLoaded)`
- → `MapRenderMode.nightModeLoaded` (default `true`)
- → both main-map paths:
  - wide: `:1270` → `AvailabilityWideLayoutUi.kt`;
  - compact: `:1322` → `AvailabilityCompactScaffold.kt`, then `mapRenderMode.copy(...)` → `AvailabilityCompactMapUi.kt`.
- → `SightingsMapSlot` (`MapSlot.kt`, `nightModeLoaded = renderMode.nightModeLoaded`)
- → `SightingsMap(nightModeLoaded = ...)` (default `true`).

The other `MapRenderMode` construction sites keep the default `true`, by the ruling:

- `CentrePinLocationPicker.kt:127`, reached from `AvailabilityOfflineMapsUi`, `JournalTab` and `LogPanel`;
- `CartographyEntryReportScreen.kt:377`.

That rests on an **unverified inference**: every such map sits behind user navigation, so the preference
has loaded by the time one is reached. It is a device item below.

The dispatch's premise was that the flag would take one path (AvailabilityScreen → MapRenderMode/MapSlot →
SightingsMap). That was wrong: there are three construction sites. The ruling records it as the planner's
error.

### Offline night (d)

**SDK check (step 2).** I ran `javap` on `classes.jar` from `android-sdk-13.5.0.aar` in the Gradle cache.
All of the following exist:

- `Style`: `getLayers()`, `getLayer(String)`, `getLayerAs(String)` and `getJson()`;
- `Layer`: `setProperties(PropertyValue<?>...)` and `getId()`;
- colour getters, each returning a `PropertyValue<String>`: `BackgroundLayer.getBackgroundColor()`,
  `FillLayer.getFillColor()`, `FillLayer.getFillOutlineColor()` and `LineLayer.getLineColor()`;
- colour setters on `PropertyFactory`: `backgroundColor`, `fillColor`, `fillOutlineColor` and `lineColor`,
  each taking `(int)`, `(String)` and `(Expression)`;
- `PropertyValue`: `isNull`, `isExpression`, `getExpression`, `isValue`, `getValue` and `getColorInt`;
- `Expression`: `toString()`, `toArray()` and `static raw(String)`.

So (d) was buildable, and no deferral applied.

**How it works.**

- In the `setStyle` callback, when `requested.useOfflineTiles && requested.night`,
  `applyOfflineNightRecolour(style)` runs **before** `initializeOverlayLayers`. The overlays' own line and
  fill layers are therefore never recoloured.
- It walks `style.layers`. For each background, fill and line layer, it reads the colour properties
  through the SDK getters: a string value becomes a literal, and an expression becomes its `toString()`
  JSON.
- It hands each property to the pure `offlineNightRecolourOf`, then writes the result back with
  `PropertyFactory` (a `String`, or `Expression.raw(json)`).
- Anything the pure function cannot read is left in its day colour and logged by layer and property.
  One summary line logs the recoloured and left counts.
- Day, or night off, restyles nothing: the style reloads fresh from its URI.

`offlineNightRecolourOf` recolours a literal, and every colour literal inside any expression (planner
ruling 2). It reads these forms:

- `#rgb`, `#rgba`, `#rrggbb` and `#rrggbbaa`;
- `rgb()` and `rgba()` strings;
- `["rgb", ...]` and `["rgba", ...]` arrays with number arguments.

Labels, conditions and zoom stops are left as they are. It leaves, with a reason:

- CSS colour names;
- `hsl()`;
- expressions with no colour literal (data-driven `["get", ...]`);
- text that is not JSON.

Colour names *inside* an expression are not recognised and stay day; the repository's offline style uses
none.

**`server/pmtiles-worker/src/offline-style.json`:**

- 57 layers: 1 background, 15 fill and 41 line.
- Colour properties: `background-color` ×1, `fill-color` ×15 and `line-color` ×41. There is no
  `fill-outline-color`.
- Three colours are expression-valued:
  - `landcover` `fill-color` (`match`);
  - `landuse_park` `fill-color` (`case`);
  - `roads_minor` `line-color` (`interpolate` on zoom).
- The test "every colour property in the repository's offline style is recoloured" asserts that all 57
  are recoloured and none left.

That is a claim about the file as authored. Two things are unverified:

- **The live style's form.** MapLibre Native may hand colours back in a different form: an `rgba()`
  string for literals, and `["rgba", ...]` arrays inside expressions are my expectation, and both are
  handled. Only a device run shows the actual form, and any form not handled appears in the log lines.
- **The deployed worker.** Whether it serves this exact file.

### Stale comments and docs (e)

- `SightingsMap.kt`:
  - `nightMode`'s doc: the basemap sentence only; the marker sentences are C2's;
  - the style-swap block, base `:393` and `:404-406`;
  - three passages that say a night toggle takes the style-swap path, base `:426-427`, `:772-773` and
    `:821-822`. These are true now except over Satellite, and each now says so.
- `BasemapStyles.kt`:
  - the paint's doc, base `:27-37` and `:40-62`, rewritten for V1 in the (b) commit;
  - `mapStyleSourceFor`'s doc, base `:113-120`.
- `MapSlot.kt`: `:72-73`, and `MapRenderMode.night`'s basemap sentence.
- `README.md` `:760-763`. The dispatch said `:764`; the passage sits at `:760-762` at the base.
- `docs/plans/map-redesign.md`: an appended dated note corrects `:382-386` and `:459-467` and records night
  mode as built. No old text was edited.

Marker-palette and halo comments were left for C2, including `MapPalette.kt:349`'s mention of
`raster-saturation`/`raster-contrast`, which is now stale.

## Tests

**Tests-first.** Each commit failed for its stated reason before its implementation:

| Commit | Failure |
|---|---|
| `156f797` (b) | 3 of 12 on assertions, no compile error. "exactly the three V1 properties": `expected:<[raster-brightness-min, raster-brightness-max, raster-hue-rotate]> but was:<[raster-saturation, raster-contrast]>`. "Satellite's night style JSON equals its day style JSON": the night JSON carried a paint block. `OfflineStyleSwapTest` night applied: `OPEN_TOPO_MAP ... expected:<true> but was:<false>` |
| `cdc118a` (b) | Compile: `Unresolved reference 'basemapTakesNightPaint'` |
| `b2b67f7` (a) | Compile, 11 errors: `No parameter with name 'night' found`, `Unresolved reference 'effectiveNight'` |
| `643887e` (c) | Compile, 10 errors: `nightModeLoaded`, `nightModeMapsLoaded`, `requestedMapStyle` unresolved |
| `dfe6ef0` (d) | Compile, 58 errors, all members the commit had not added: `nightColorOf`, `LayerColourProperty`, `StyleColourValue`, `NightRecolour`, `offlineNightRecolourOf`, `NIGHT_RECOLOURED_PROPERTIES` and follow-on inference errors |

**Assertions changed on purpose, each named in its commit.**

- `BasemapNightStyleTest`:
  - The old "desaturates and re-sharpens every basemap, without dimming it" is replaced. Its
    assertions were: no `raster-brightness-max` (old `:58-61`), saturation in [-1, 0) (old `:63-67`),
    and contrast in [-1, 1] (old `:69-70`). The replacement asserts exactly the three V1 keys, with
    values 1, 0 and 180.
  - The old requirement of a night paint on every basemap (old `:53-55`) became "Satellite's night JSON
    equals its day JSON".
- `OfflineStyleSwapTest` `:52-53`: it looks for `raster-hue-rotate`, and expects it only off Satellite.

**New tests (26):**

| Class | Tests | What they check |
|---|---|---|
| `BasemapNightStyleTest` | +2 | Satellite night equals day; only Satellite opts out. The V1 test replaces one existing test. |
| `OfflineStyleSwapTest` | +6 | Night toggle reloads on Topographical and Street; not over Satellite; always over offline; `effectiveNight`'s truth table; no style requested before load; once loaded, requested carries effective night. |
| `AvailabilityViewModelNightModeTest` (new) | 2 | Success: gate closed, then open with the value. Failure: gate open, day, logged, asserted through a recording `ErrorLog`. |
| `AvailabilityScreenSettingsPanelTest` | +3 | Through the real screen: main map told not loaded, then loaded. The offline picker map keeps the default while the screen's flag is false. |
| `NightColourTest` (new) | 4 | The 17 swatch-board §3 pairs within 1/255; black and white swap; alpha kept; HSV hue within 2° for the 12 saturated pairs that don't clamp. |
| `OfflineNightRecolourTest` (new) | 9 | Fixture style with literal hex and `rgba()`, `match`, `case`, `interpolate`, `step`, the `["rgba", ...]` array form, `fill-outline-color`, a data-driven colour, a named colour and a symbol layer. Plus the repository's offline style. |

**The effect's keys are not asserted.** No headless test can reach them. `SightingsMap` constructs a
`MapView`, whose native initialiser Robolectric cannot run, and every screen test substitutes a fake
`MapSlot`. The tests reach the two decisions the keys feed:

- `requestedMapStyle` and `needsStyleReload`, as pure functions;
- the hop into `MapRenderMode`, through the real screen.

`applyOfflineNightRecolour` (the SDK adapter) is not reachable headless for the same reason.

**Hue: HSV, not Oklab (planner ruling 3).** V1 adds one amount to every channel, so HSV hue is exact
wherever nothing clamps. My pre-code Python check gave the same HSV hue to 0.1° for every unclamped pair.
Oklab hue does drift, which is information, not a failure: the swatch board's own table has `#fad5a3`
going from 74.9° to 65.9°, and `#e892a2` from 7.8° to 12.9°.

**Revert checks.** The runner followed CLAUDE.md's rules:

- it saved a copy of the file before editing;
- each edit matched exactly once;
- the results directory was deleted before every run;
- each reverted build log was checked for `e:` lines before any result was read, and every one had
  **0** compile errors, with the test task executed;
- the file was restored from the saved copy, never from git, with its sha256 checked equal;
- the forward text was confirmed present afterwards, and `git status` was clean.

Five classes ran in each check (34 tests).

| Revert | Edit | Failed | Quoted failures |
|---|---|---|---|
| 1, night field | `needsStyleReload = applied?.copy(night = requested.night) != requested` | 2 | "turning Night Maps on or off is a reload on Topographical and Street": `OPEN_TOPO_MAP: day -> night`; "... over the offline style is a reload, whatever the basemap": `USGS_IMAGERY_ONLY offline: day -> night` |
| 2, Satellite skip | `basemapTakesNightPaint(...) = true` | 5 | "only Satellite opts out": `expected:<[OPEN_TOPO_MAP, OSM_STANDARD]> but was:<[USGS_IMAGERY_ONLY, OPEN_TOPO_MAP, OSM_STANDARD]>`; Satellite night equals day (the night JSON differs); night applied: `USGS_IMAGERY_ONLY ... expected:<false> but was:<true>`; "over Satellite is not a reload"; "effective night is the toggle, except Satellite's ..." |
| 3, gate | `if (!nightModeLoaded)` → `if (false)` in `requestedMapStyle` | 1 | "no style is requested before the Night Maps preference has loaded": `USGS_IMAGERY_ONLY night=false offline=false expected null, but was:<AppliedMapStyle(...)>` |
| 3b, gate at the ViewModel (extra) | success path without `nightModeMapsLoaded = true` | 1 | "the gate is closed until the preference read completes, then opens with the stored value": `the read completed` |
| 4, hue-rotate | `night(c) = (255 - c)` | 5 | pairs: `day #A78A41 channel 0: got 88, board says 176`; hue: `#A78A41: day hue 42.9, night hue 222.9`; `match`, `case` and literal recolours differ, e.g. `expected:<rgba([28, 57, 25], 1)> but was:<rgba([45, 16, 48], 1)>` |

Every failure in each revert is one that edit alone could cause. Under revert 4:

- black ↔ white, alpha, `#cccccc`, and the `interpolate`/`step`/`["rgba", ...]` cases still passed. They are
  all greys, for which V1 and `255 - c` agree, so they cannot detect the missing hue term;
- the pair and hue tests are the ones that do.

**Suites.**

- Before, at `0907664` (`app/` identical to `d0ae612`): 217 classes, 1,681 tests, 0 failures, 0 errors,
  24 skipped.
- After, at `8859694`: 220 classes, 1,707 tests, 0 failures, 0 errors, 24 skipped. The log has no `e:`
  line. The results directory was deleted before the run.
- The difference is +3 classes and +26 tests, as tallied above, and skipped is unchanged.
- The per-class counts differ from the baseline only in the six classes this build touched. Every
  other class is identical.

Evidence is kept outside the repository:

- `~/Zynergy/forager-night-baseline/` and `~/Zynergy/forager-night-after/`: gradle.log, JUnit XML,
  per-class counts and HEAD;
- `~/Zynergy/forager-night-work/`: each tests-first and implementation run, and the five revert folders
  with their spec, diff, log, XML and saved copy.

## Device items (none run; deferred to beta by owner ruling)

1. V1 appearance on Topographical and Street.
2. Satellite unchanged at night.
3. Toggling Night Maps on a loaded map restyles it, and toggling over Satellite does not reload.
4. Cold launch with Night Maps on shows night with no day flash, including the launch delay: the map is
   blank until the preference read lands.
5. The offline map's night recolour looks right. Read the `SightingsMap` log lines "Offline night: N
   colour properties recoloured, M left" and any "left in its day colour" lines. They show which form the
   SDK returns colours in, and whether `Expression.toString()` gives parseable JSON for `match`, `case`
   and `interpolate`.
6. Markers are still present after every toggle.
7. (Planner ruling) The centre-pin pickers (offline maps, Journal, Log panel) and the Cartography entry
   map, reached after cold launch with Night Maps on, open in night. This checks the unverified inference
   that the preference has loaded by the time a user navigates to one.

## Flags

- `MapPalette.kt:349` still describes the night paint as `raster-saturation`/`raster-contrast`. That file
  is C2's and was not touched.
- `SightingsMap.nightMode`'s doc still says it "drives the map's own twilight trigger and long-press
  override". That sits among the marker sentences left for C2. It is stale: no twilight trigger or
  long-press override exists anywhere in `ui/map` at `8859694` (grep for "twilight" finds only that
  comment).
- The test "offline on ignores the basemap and night mode, since the store holds one style" is still
  correct about the style *source*. Offline night is now applied after load, not through the source.
- A user who toggles Night Maps before the startup read lands will have the toggle overwritten by the
  read. That is the base's behaviour on success; the failure path now does the same with `false`, as the
  dispatch specified.
