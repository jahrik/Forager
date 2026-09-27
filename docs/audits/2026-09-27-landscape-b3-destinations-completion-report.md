# 2026-09-27: Landscape B3, destinations (P11), completion report

Dispatch: `prompts/preserved/2026-09-27-14.md` (build, destinations coder). Plan:
P11, R7, R9 and R12 (revised) in `docs/plans/landscape-phone-design.md`, and
`docs/audits/2026-09-27-landscape-b3-prebuild-report.md`. Written by the
destinations coder in a cloud worktree on local branch `b3-destinations`,
which tracks `origin/landscape-b3`.

## Commits (all pushed to `landscape-b3`)

| SHA | What |
|---|---|
| `82bf5f1d91b6d0cadbd80b7adfe557b9bf9c7fde` | Tests first: `AvailabilityScreenLandscapeB3DestinationsTest`. At base: 12 tests, 7 failures (below). |
| `b268c75a5eb023100b6020d3067c0a994e831e19` | The cap: `AvailabilityCompactScaffold.kt` (railBeside path only) and `READABLE_CONTENT_MAX_WIDTH` made `internal` in place. |
| this report's commit | This file. |

Main code: 2 files, 22 insertions, 3 deletions, most of them comments
(planner's prediction 1, under 40 lines: holds).

## Premises checked before building

- **Base.** The dispatch header says `acba675`. The planner's message names
  `97843679e5039972ba2a1ada301051cab04427f8`. `git diff --stat acba675 9784367`
  touches only `RECORD.md`, the pre-build report and three `prompts/preserved/`
  files, no code, so the two agree for this work. My branch was cut at
  `9784367` (confirmed with `git rev-parse HEAD`).
- **The rail beside the content** is where the dispatch says:
  `railBeside` at `AvailabilityCompactScaffold.kt:502-503`, the Row at `:627`
  (both at base). Confirmed.
- **The constant.** `READABLE_CONTENT_MAX_WIDTH` was `private` at
  `AvailabilityResultsUi.kt:303`. Confirmed. Now `internal`, same place.
- **Seasonal already caps** at non-COMPACT widths
  (`AvailabilityResultsUi.kt:245-256`): `widthIn(max = 640).fillMaxWidth()`,
  then `.padding(horizontal = Spacing.lg)` *inside*, and the tag
  `SEASONAL_CONTENT_TAG` sits between them. So the tagged column is 640 wide,
  and the two caps compose to `min(640, 640) = 640`. T2 measures exactly
  640.0 dp at both rotations after the change. Confirmed.

### The destinations the non-Map branch can show, from the code

None of them caps or centres itself except Seasonal. A grep for `widthIn(max`
and `READABLE_CONTENT` in `ui/log/`, `ui/track/` and the Records panels found
nothing. "Scrolls" below means **a vertical scroll container was read in the
code**. No destination's scrolling was exercised by a test at
`w823dp-h384dp-land`. See "Not verified".

| Destination | Composable, file | Caps or centres? | Scroll container (read) |
|---|---|---|---|
| List | `ListTab`, `AvailabilityResultsUi.kt:97` | No. `fillMaxWidth` + 16 dp side padding | `LazyColumn` in `ResultsSection` (only when a forecast exists; loading, empty and not-searched states are a spinner or one `Text`) |
| Seasonal | `SeasonalTab`, `AvailabilityResultsUi.kt:238` | Yes, 640 dp, `TopCenter` | `verticalScroll`, `:257` |
| Journal shell | `JournalTab`, `ui/log/JournalTab.kt:115` | No. Top `SecondaryTabRow`, default tab Cartography (`:238`) | n/a (shell) |
| Cartography: Entries / Drafts | `CartographyScreen` (`CartographyScreen.kt:75`) → `CartographyEntryListScreen` | No | `LazyVerticalGrid`, `GridCells.Fixed(columns)`, `CartographyEntryListScreen.kt:84` |
| Cartography: Album | `PhotoGalleryScreen` | No | `LazyVerticalGrid`, `Fixed(2)`, `PhotoGalleryScreen.kt:136` |
| Cartography entry view | `CartographyEntryReportScreen` (holds a map preview) | No | `verticalScroll`, `CartographyEntryReportScreen.kt:466` |
| Cartography entry edit | `CartographyEntryEditScreen` | No | `verticalScroll`, `CartographyEntryEditScreen.kt:241` |
| Records shell | `RecordsTab`, `ui/log/RecordsTab.kt:78` | No. Four-tab `SecondaryTabRow`, default Waypoints | n/a (shell) |
| Records: Waypoints | `WaypointsSection`, `AvailabilityTripsWaypointsUi.kt` | No | `verticalScroll`, `:188` |
| Records: Offline Maps | `OfflineMapsPanel`, `AvailabilityOfflineMapsUi.kt` (a picker map at a fixed aspect ratio) | No | `verticalScroll`, `:138` |
| Records: Recorded Tracks | `TrackExportList`, `ui/track/TrackExportPanel.kt` | No | `verticalScroll`, `:81` |
| Records: Logged Finds gallery | `FindsGalleryScreen` | No | `LazyVerticalGrid`, `Fixed(2)`, `FindsGalleryScreen.kt:115` |
| Log entry view | `LogEntryReportScreen` | No | `verticalScroll`, `LogEntryReportScreen.kt:153` |
| Log entry edit | `LogEntryDetailScreen` | No | `verticalScroll`, `LogEntryDetailScreen.kt:169` |
| Log entry edit → Add Location | `CentrePinLocationPicker` (map takes the height left over) | No | none. It is a map, not a list |
| Log entry edit → From Album | `PullPhotoPickerScreen` | No | `LazyVerticalGrid`, `Fixed(2)`, `PullPhotoPickerScreen.kt:95` |

Settings and Album-in-the-drawer are not in this list (R7). They live in the
tools drawer, which is not part of this Column.

**Full-width by design?** I found no recorded decision that any of these must
fill the width. The ones the dispatch's examples point at are: the photo grids
(Album, Finds gallery, Pull Photo, Cartography entries), the Offline Maps picker
map, the entry-location picker map, and the Cartography entry's map preview.
The grids' recorded design is a fixed column count per host ("[columns] is the
only thing that varies between hosts (2 compact / 3 expanded", `FindsGalleryScreen.kt:50`;
the same in `CartographyScreen`'s doc comment), not "fill the width". P11 itself names
Album/Gallery and log entry view and edit as capped. So I did not stop. **If the
owner reads either picker map as full-width by design, that is the one place I
would expect a ruling.** Capped, each loses about 51 dp on each side at
823 dp. See "Needs a decision".

## What was built

`AvailabilityCompactScaffold.kt`: the Row's content `Column` (the one beside
the rail) keeps `.weight(1f).fillMaxHeight()` and, only when `railBeside`, adds
`.wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = READABLE_CONTENT_MAX_WIDTH).fillMaxWidth()`.
Otherwise it adds `Modifier`, so portrait, non-short windows and the Map tab
get the identical chain as before. There is no new container and no change to
any destination. No scroll container was added.

## Tests: each seen failing at its base for its stated reason, then passing

Class `AvailabilityScreenLandscapeB3DestinationsTest` (`w823dp-h384dp-land`,
T3 overridden per method to `w411dp-h891dp`). It drives the real
`AvailabilityScreen`, and each landscape test first asserts that the screen
read the pinned rotation. Twelve tests:

- **T1** ×6: List, Journal (Cartography default) and Records, each at
  `ROTATION_90` and `ROTATION_270`. Width is at most 640 dp, and the centre is
  within 1 dp of the centre of the area beside the rail. What is measured:
  List's `species-row` card, which sits inside the tab's 16 dp padding, so it
  is asserted at most 608 dp; the Journal top tab row (Cartography's left edge
  to Records' right edge); the Records sub-tab row (Waypoint Markers' left edge
  to Logged Finds' right edge).
  - **At base (`82bf5f1`): 6/6 fail on width.** Messages: "List's species
    card … it is 711.0.dp, in an area 743.0.dp wide beside the rail"; "the
    Journal's top tab row … 742.0.dp"; "the Records sub-tab row … 740.0.dp".
    Each appears at both rotations. This matches the dispatch's "about 743 dp".
  - After `b268c75`: 6/6 pass.
- **T2** ×2: Seasonal at both rotations. Capped, centred, and **exactly**
  640.0 dp (±0.5), so a composition that shrank it below 640 fails. It passes
  at base and after, as the dispatch expects. It does bite under R-b (below).
- **Search bar** ×1, my addition: at `ROTATION_90` on List, the search bar is
  capped and centred with the content. At base it fails: "the search bar …
  743.0.dp". It passes after. See "Decided beyond the dispatch".
- **T3** ×1: portrait `w411dp-h891dp`. No rail, and List's card runs from
  16 dp to width−16 dp. This is a pin: it passes before and after by design.
- **T4** ×2: at each rotation, from the capped List tab, a real
  `performTouchInput` click at five points across the Seasonal rail item's own
  bounds (centre and four points at 20%/80%). The item's bounds are first
  asserted to lie inside the rail. Each click is asserted to select Seasonal,
  and the tally is asserted to be 5. Passes at base and after (a guard).

Runner: a scratchpad script deletes `TEST-*.xml` before each run, counts
`^e: ` lines in the Gradle log, and reads the XML only after that.

## Revert checks

Before each edit the forward file was saved to the scratchpad, and afterwards
it was restored from that copy (never from git). After both, `git status` was
clean, `git diff HEAD~1 --stat` still showed the forward change, and a grep
found `if (railBeside) {`, `wrapContentWidth(Alignment.CenterHorizontally)` and
`widthIn(max = READABLE_CONTENT_MAX_WIDTH)` at `AvailabilityCompactScaffold.kt:649-652`.

- **R-a: remove the scaffold cap** (`if (railBeside) {` → `if (false) {`,
  one line). Prediction: T1 fails on every destination. Build log: 0 `e:`
  lines, and `compileDebugKotlin` ran. **Observed:** 12 tests, 7 failures. They
  are T1 ×6 plus the search bar, each with the same width messages as at base
  (711 / 742 / 740 / 743 dp in 743 dp). T2, T3 and T4 pass, as they should
  with only the scaffold cap gone (Seasonal caps itself). Every failure is one
  this edit produces. Not stale.
- **R-b: keep the cap, drop the centring**
  (`wrapContentWidth(Alignment.CenterHorizontally)` → `Alignment.Start`). The
  prediction was that T1's centre assertion fails and its width assertion
  passes. Build log: 0 `e:` lines, and `compileDebugKotlin` ran.
  **Observed:** 12 tests, 9 failures, all on the centre assertion (the width
  assertion runs first and passed). T1 ×6: expected centre 371.5, was 320.0
  at `ROTATION_90`; expected 451.5, was 400.0 at `ROTATION_270`. Content
  measured 608 or 640 dp wide, starting at the area's left edge. The search
  bar fails the same way. **Beyond the prediction: T2 ×2 failed too** (Seasonal:
  expected 371.5, was 320.0, and 451.5 against 400.0). This edit can produce
  that: Seasonal's own centring now happens inside a 640 dp column that is
  itself left-aligned. So it is a real consequence of this edit, not a stale
  run. The 51.5 dp offset is (743 − 640) / 2, specific to this edit.

## Affected test classes, before and after

| Class | Before (setup dispatch, `9784367`) | After (`2b9897f`, full suite) |
|---|---|---|
| `AvailabilityScreenShortLandscapeTest` | 11/0/0/0 | 11/0/0/0 |
| `AvailabilityScreenLandscapeB2Test` | 26/0/0/0 | 26/0/0/0 |
| `AvailabilityScreenLandscapeB3DestinationsTest` | did not exist | 12/0/0/0 |

(tests/failures/errors/skipped.) No existing test changed (prediction 3: holds).
The affected classes grew by 12 (prediction 4, 8 to 16: holds). T1 failed at
base on every destination it covers (prediction 2: holds).

**Full suite, once, at `2b9897f`.** That is my `b268c75` plus the drawer
coder's tests-first commit `177a2dd` and their merge. Result: **226 classes /
1782 tests / 6 failures / 0 errors / 24 skipped**, 2 m 32 s. All 6 failures
are in `CompactToolsDrawerTest` (13 tests), the drawer coder's tests-first
class, which is failing at its base by design (sheet side, scrim-tap close).
Without that class: 225 / 1769 / 0 / 0 / 24, which is the planner's baseline
(224 / 1757 / 0 / 0 / 24) plus this class's 12. No test outside those two new
classes failed.

## Decided beyond the dispatch

1. **The search bar is inside the cap.** The dispatch's first paragraph says
   "the content beside the opaque rail (… the Row at `:627`) is capped". The
   Row's non-rail child is the `Column` holding `SearchEntryBar`, its notice,
   and the tab `Box` (with the search dropdown and its scrim). Capping that
   Column is one modifier, and it keeps the bar, its dropdown and the content
   in one 640 dp column. The alternative was to cap only the tab `Box` and
   leave the bar at 743 dp. A separate test pins this choice, so if the planner
   rules the other way, that test and three modifier lines move to the `Box`.
2. **The cap is a modifier chain on the existing Column** instead of a new
   wrapping `Box`. This leaves the portrait hierarchy byte-for-byte the same
   (the non-rail branch adds `Modifier`), and it made the R-b revert a
   one-token edit.
3. T3 lives in the same class as a method-level `@Config(qualifiers =
   "w411dp-h891dp")`, so all four tests are in the one named class.

## Not verified

- **Scrolling at `w823dp-h384dp-land`** was read from the code (table above),
  not exercised. No test scrolls any destination. The dispatch asked for a
  check that each destination scrolls. What I can state is that each has a
  vertical scroll container, except the entry-location picker map, which is a
  map, and List's non-forecast states, which are one line of text.
- **Height, not width, is the scarce axis here.** Measured under Robolectric
  on Records: the search bar ends at 85 dp and the Journal tab row at 141 dp,
  and the Records sub-tab row ends at 197 dp of 384 dp. That leaves about
  187 dp for the Records sub-tab's own content before any real system-bar
  inset. The entry-location picker map gets whatever is left after that and
  its own instruction and button rows. This is outside P11's width scope and
  was not measured on a device.

## Device-only (B4)

- The cap and centring beside the real rail, with real cut-out and
  navigation-bar insets. `shortLandscapeContentInsets()` is zero under
  Robolectric, so "the area beside the rail" was window − rail here. On a
  device it is also less the insets, and the content should centre in what is
  left.
- Whether the two picker maps and the Cartography map preview read acceptably
  at 640 dp (visual).

## Needs a decision

- **Picker maps, possibly full-width.** Neither has a recorded full-width
  design, so they are capped with everything else. If the owner wants the
  Offline Maps picker or the entry-location picker to span the full width
  beside the rail, that is a per-destination exception. The dispatch forbids
  threading it into a destination, so it would need its own ruling.
- **Search bar in or out of the cap** (item 1 above). Built "in".

## Flags outside scope

- The drawer coder's `CompactToolsDrawerTest` has 6 failures at `2b9897f`, by
  design (tests first). Not touched.
- The vertical space consumed by stacked chrome in Records (above) may deserve
  its own look in B4 or the Journal redesign.
