# Map launch fixes: completion report (dispatch 2026-10-18-04)

Hazel (app coder), 2026-10-18, written 14:51Z. RECORD -1272 (decision), -1273 (intent), -1278 (the owner's
answers). Branch `map-launch-fixes`, PR #257 into `pre-main`, **not merged**. This session continued the work
from pushed head `b3ca6a56` after the previous Hazel session was stopped by mistake (-1281). That session wrote
all the code and the six revert branches. This session read CI, checked the reverts and wrote this report.

**Status: not done. Full CI at `b3ca6a56` is red, with one failure, an existing test that pins the behaviour the
owner's answer (B) removed. Rewriting it is a test change that the dispatch lists as a stop ("a test would have to
be weakened"), so it is untouched and the decision goes to the planner (Questions, below).**

## What landed

Measured at `b3ca6a56`. Its base is `e61e248a`, which is `origin/pre-main` (checked 14:51Z). The local head matched
`origin/map-launch-fixes` before any work.

| Commit | What it does |
|---|---|
| `a0005a33` | Tests over stubs, first version (no demo-host style, OpenTopoMap's full credit, the client installed after MapLibre initialises). Its CI run 39133618709 failed on a test compile error. |
| `fc48f5f3` | Fixes that compile error (trailing lambda on `getInstance`). |
| `e4159ee0` | Installs the map's HTTP client inside `MapLibreInitializer`, straight after the SDK call succeeds. |
| `c0303ea6` | Tests over stubs for the owner's answers (-1278): no glyphs, raster names kept, full credit tap-to-reveal, an install failure fails initialisation, no Open Sans credit. |
| `b3ca6a56` | The build of all three answers. |
| (this report) | This file and its index row. |

**Why the two earlier runs failed.** Both were filtered `workflow_dispatch` runs over the new tests, run on purpose
to see them fail before the code was written:
- 39134222373 at `fc48f5f3`: 19 tests, 4 failed;
- 39134774158 at `c0303ea6`: 29 tests, 6 failed.

Every failure was one of the new tests, failing for its own reason (JUnit XML read, e.g. `expected:<[sdk, client]> but
was:<[sdk]>`). They were not regressions, and `b3ca6a56` is the build they were waiting for. 39133618709 at `a0005a33`
was a compile error, fixed by `fc48f5f3`.

## Verification of the premise "b3ca6a56 builds all three"

Read from the diff (`git diff origin/pre-main...b3ca6a56`).

- **(A) holds.** `"glyphs"` is gone from both style builders, `nightStyleJsonWithBasemapBelow` and
  `singleLayerStyleJson` (`BasemapStyles.kt`). `GLYPHS_URL_TEMPLATE` and `AREA_MARKER_FONT_STACK` are deleted. The
  Open Sans entry is removed from `app/src/main/assets/credits/software.json`.

  No layer in `app/src/main` uses `text-field` or a `SymbolLayer` (grep). The only mentions are comments about the
  offline style. The tile URLs are unchanged, so the names printed in the raster tiles stay.

  The answer said to "update" the software.json credit note. The branch deletes the entry instead, because nothing
  is left for it to credit. I read that as the same thing; say so if not.
- **(B) holds.** `installHttpClient` runs inside `MapLibreInitializer.initialize`'s try, after `getInstance`
  (`MapLibreInitializer.kt:40-41`). A throw there is logged, rethrown and not remembered, so the next call retries
  both. `ForagerApplication` no longer installs the client separately, and the "default User-Agent" fallback log is
  gone. The real lambda is `installMapHttpClient()` (`MapLibreStorage.kt:29`).

  All three callers go through `initializeMapLibre`:
  - `ForagerApplication.kt:79`;
  - `SightingsMap.kt:325`;
  - `MapLibreOfflineMapRepository.kt:230`.
- **(C) holds.** `Basemap.fullAttribution` defaults to `attribution`. OpenTopoMap's is "© OpenStreetMap contributors,
  SRTM | © OpenTopoMap (CC-BY-SA)". Both builders write `fullAttribution` into each source's `attribution`, which is
  what MapLibre's tap-to-reveal dialog lists.

  The caption is still built from `attribution` through `mapCreditsFor`/`attributionCaption` (`SightingsMap.kt:1471`).
  MapLibre's attribution button is not turned off anywhere (no `isAttributionEnabled` write in `app/src/main`). It is
  placed at bottom-end or bottom-start (`SightingsMap.kt:791`, `:1302-1308`).

**A premise in (B) that I believe is wrong (inferred, not observed).** -1278 says an install failure leaves "the map
blank as on any start-up failure". `SightingsMap.kt:322-327` calls `initializeMapLibre(context)` inside `remember {}`,
and `MapLibreInitializer` rethrows. There is no catch around that call in `SightingsMap.kt` (grep), and no test
composes the real `SightingsMap`.

So my reading is that a failed initialisation, now including a failed install, throws during composition when the map
screen opens. That is more likely an app crash than a blank map. This is not new: a `getInstance` failure already took
that path on pre-main, and the branch adds the install as a second way into it. Nothing has been run to confirm it. A
failure after a successful `getInstance` has no known cause today; the only failure mechanism found, the client loaded
before MapLibre is ready, can no longer happen. This needs the owner's attention, not a pick from me (Questions).

## Evidence

### Full CI at `b3ca6a56`: run 39136088411 (pull_request, attempt 2), red

- Lint (debug): success.
- Release compile and package: success.
- Build, test, publish APK: failure.
- JUnit XML from the run's `unit-test-report` artifact, 675 files, timestamps 14:39:31Z to 14:48:50Z: **5,064 tests, 1
  failure, 0 errors, 24 skipped.**
- The one failure: `MapUserAgentTest > starting the application installs the client MapLibre will use, carrying the
  app's User-Agent`, with the message `java.lang.AssertionError: ForagerApplication.onCreate installed no HTTP client into MapLibre`.

**Why it fails.** That test, from dispatch 2026-09-28-325, reads `HttpRequestImpl.client` after Robolectric boots
`ForagerApplication`. Under Robolectric `MapLibre.getInstance` always fails, because the native library cannot load.

- **Before:** the app installed the client anyway, and the test passed. So under Robolectric the install did *not*
  throw after a failed `getInstance`. That is an observation, not an explanation.
- **Now:** answer (B) installs it only after a successful initialisation, so nothing is installed and the test fails.

The failure is caused by this change and says exactly what (B) asked for. The test's claim, "application start
installs the client", is still meant to hold on a device, where initialisation succeeds. It can no longer be reached
under Robolectric. It is not edited, skipped or weakened.

### Revert checks

Six revert branches, each one commit on top of `b3ca6a56` changing one line in one main-source file (`git diff
b3ca6a56 origin/map-launch-fixes-revert-N`, read for each). Each run was a `workflow_dispatch` filtered to the five
affected classes:
- `MapLibreInitializerTest`;
- `BasemapStyleTest`;
- `BasemapTest`;
- `SoftwareCreditsCatalogTest`;
- `TopoNightStreetSwitchTest`.

For every run:
- the log has **zero Kotlin `e:` lines**;
- the release compile job succeeded;
- the unit test task compiled and ran 41 tests;
- the JUnit XML is from that run's own artifact, with timestamps inside the run (14:36Z to 14:42Z).

None of them restores from git into the forward branch. They are separate branches, and `origin/map-launch-fixes` is
still `b3ca6a56`, with the forward diff above present (re-checked 14:51Z).

| Revert | Edit | Run | Result | Failures, each one this edit can cause |
|---|---|---|---|---|
| R1 | `"glyphs"` demo URL restored in `singleLayerStyleJson` only | 39136131212 | 41 / 4 failed | `no style declares a glyphs URL` ("OPEN_TOPO_MAP (night=false) still declares glyphs: …demotiles…"); `no style … asks MapLibre's demo server` ("still names the demo host"); `TopoNightStreetSwitchTest` day and Street-night byte-identical snapshots (diff shows the glyphs line). Topo night uses the other builder, so it does not fail, which is consistent. |
| R2 | single-layer source attribution back to the caption line | 39136135460 | 41 / 4 failed | `OpenTopoMap's tap-to-reveal credit is its full credit` (night=false, short form found); `every basemap's style JSON carries its full attribution string` (OPEN_TOPO_MAP); `TopoNightStreetSwitchTest` night "the topo source is the day document's, unchanged" (day now short, night full) and day snapshot. |
| R3 | `runCatching { installHttpClient() }` (failure swallowed) | 39136139883 | 41 / 1 failed | `a client that cannot be installed fails the initialisation` ("expected java.lang.IllegalStateException to be thrown, but nothing was thrown"). |
| R4 | `installHttpClient()` call removed | 39136144795 | 41 / 3 failed | `the map's HTTP client is installed straight after MapLibre initialises` (`expected:<[sdk, client]> but was:<[sdk]>`); `no HTTP client is installed while MapLibre has failed … and it is on the next success` (`expected:<1> but was:<0>`); `a client that cannot be installed fails the initialisation` (nothing thrown). |
| R5 | Open Sans entry restored in software.json | 39136149384 | 41 / 1 failed | `no map label font is credited, now the map fetches none` ("still credited: […]", `SoftwareCreditsCatalogTest.kt:55`). |
| R6 | OpenTopoMap's `fullAttribution` line removed | 39136152957 | 41 / 3 failed | `OpenTopoMap's tap-to-reveal credit is its full credit`; `BasemapTest > OpenTopoMap's caption keeps the short credit and its full credit is the long form`; `TopoNightStreetSwitchTest` day snapshot. All show the short form where the full one is expected. |

All six are valid, and none needed re-doing.

### A test that cannot fail

`MapLibreInitializerTest > application start never falls back to MapLibre's default User-Agent` passed in run
39134222373 at `fc48f5f3`. At that commit `ForagerApplication` still had `installMapHttpClientAtStart` and its
"default User-Agent" log (`fc48f5f3:ForagerApplication.kt:48,94`).

So it passes the same before and after the change. The `MapUserAgentTest` result above explains why: under Robolectric
the old install never threw, so the fallback log never fired. It is not evidence for (B). The evidence for (B) is the
three initialiser tests that R3 and R4 broke, plus the `MapUserAgentTest` failure. That failure is the real entry point
showing, at application start, that no client goes in after a failed initialisation. The test is left as written; the
planner decides whether it stays.

## Not tested, said plainly

- **Anything on a device.** The basemaps loading, the caption, the tap-to-reveal dialog, no font requests, and what a
  failed initialisation does on screen. The device check below covers the first four. The fifth cannot be triggered
  on a device without an instrumented build.
- **Whether the full credit fits MapLibre's attribution dialog in S22 portrait.** That is a platform dialog, not ours.
- **No APK from `b3ca6a56`.** The test step failed, so the APK step did not produce one. The revert runs' APKs are
  reverted builds and must not be used for the device check.
- **The cause of the `HttpRequestImpl` static-initialiser failure** is the previous session's `javap` reading, and I
  have not re-read it. The Robolectric observation (install succeeded after a failed `getInstance`) does not settle it
  either way, because Robolectric's MapLibre is not the device's.

## Device check (S22, owner)

**Do not start until CI is green on PR #257's head** and its debug APK is installed. If the head changes after a step
has passed, start again from step 1: a pass on an older build tells you nothing about the new one.

You need the debug APK from the green run, the S22 with a network connection, and a map region you know.

1. **Street loads.** Open the Map tab and choose Street in Layers.
   - Pass: map pictures fill the screen within a few seconds, with place and road names readable in them.
   - Write down: pass or fail, and a description (no screenshots on GitHub).
   - **If this fails, stop.** Nothing after it can be read.
2. **Topo loads.** Switch to Topo.
   - Pass: contour lines and shading draw, with place and trail names in them.
   - Also zoom out past the night-mode switch point (map zoom about 9.5) in night mode. Pass: it crossfades to
     Street, as before.
3. **The caption, portrait.** On Topo, read the always-visible credit line at the bottom of the map.
   - Pass: it reads "© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)" on one line.
   - Write down the exact text and whether it wraps.
4. **The full credit, portrait.** Tap the small "i" at the map's bottom corner (MapLibre's attribution button).
   - Pass: the list includes "© OpenStreetMap contributors, SRTM | © OpenTopoMap (CC-BY-SA)".
   - Write down the exact text, and whether it shows whole or cut off.
   - In Topo night mode, the list may also show Street's credit. That is expected, because both maps are drawn.
5. **Street's credit.** On Street, the caption and the "i" list both read "© OpenStreetMap contributors".
6. **Landscape (observation, not a gate).** Repeat 3 and 4 in landscape. Write down what you see. The owner's
   ruling covers portrait; this is for the record.
7. **No font requests (needs a computer).** This one is optional and done last. With the phone on USB and the debug
   build running, open Android Studio's App Inspection, then Network Inspector, and pan both maps for about a minute.
   - Pass: no request to `demotiles.maplibre.org`, and none with `/font/` or `.pbf` in its path.
   - Tile requests to `tile.openstreetmap.org` and `tile.opentopomap.org` are expected.
   - If Network Inspector shows no map traffic at all, that is a limit of the tool, not a pass. Write "no instrument".
   - The style files are already checked by unit tests (`BasemapStyleTest`); this step is only about the phone.

**The row to write:** S22 Ultra (SM-S908U), Android version, build version name, and steps 1 to 7 with the exact texts
from 3 to 5, wrap or no wrap, the step 6 observations, and step 7's result or "no instrument".

## What RECORD -1281 (Cloudflare serves the maps) will touch

That is a later dispatch and none of it was started here. On this branch it would affect:
- `Basemap.kt:128-129` and `:141-142`, the OpenTopoMap and OpenStreetMap tile URLs and credits;
- `fullAttribution`, if the tiles or style change;
- `TopoNightStreetSwitchTest`'s byte-identical snapshots, which pin today's URLs and credits.

**Fonts.** Today's maps are raster-only, so (A)'s "no glyphs" is safe. A vector basemap served from Cloudflare (for
example Protomaps) draws its labels from fonts, so it will need a `glyphs` URL back. `BasemapStyleTest`'s new tests
forbid the demo host and any `glyphs` key, so that dispatch must change the second test on purpose: glyphs from a host
the project serves.

**The User-Agent work (B) stays useful.** It also identifies the app to its own server, but the reason it was urgent,
OpenStreetMap's tile policy, would no longer apply to the basemap.

## Scope notes

- The dispatch's scope boundary excludes "the audits index". The planner's continuation instructions for this session
  asked for this report with an index row. I followed the later instruction, and the row is the only index change.
- Commits use the dispatch's trailer, without a model name.

## Questions for the owner (through the planner)

1. **The `MapUserAgentTest` failure.** Its application-start test asserts what (B) removed, as Robolectric sees it.
   Options:
   - (a) Replace it with two tests:
     - application start under Robolectric, where initialisation fails, installs **no** client. That is the real
       entry point for (B), and it would have failed on pre-main.
     - `installMapHttpClient()` called directly puts a client carrying the app's User-Agent into MapLibre.
   - (b) Keep the test's claim and find a way to make MapLibre's initialisation succeed under Robolectric. No route is
     known.
   - (c) Something else the planner prefers.

   Each option changes an existing assertion, which is why I stopped. Option (a) also lets the weak "never falls back"
   test go.
2. **"Map blank" versus a likely crash** when initialisation fails on a device (inferred above, not observed). Is
   that acceptable as the behaviour for an install failure, or should the map screen handle a failed initialisation?
   The second would be a behaviour choice with its own dispatch.
