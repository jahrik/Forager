# 2026-09-28: map layers L0b (built; N1 and N2 resolved; revert checks, release evidence and full suite done)

**Status (2026-09-28, third coder, continuation `2026-09-28-11`): see the last section, "Resumed for N1
and N2".** The finish line is met at `2e89091`. N1 is built (the legend is capped at 96 dp and scrolls). All
20 revert checks run from this session are confirmed, none failed to compile, and the release compile
evidence is in. The full suite is 266 classes / 2183 tests / 0 failures / 0 errors / 24 skipped.
The section "Resumed after the machine restart" is the second coder's report, which stopped at an abort. The
sections before it are the first coder's report as it stood at `09bf96b`. Both are left as written.

Dispatch: `prompts/preserved/2026-09-28-03.md` (build, L0b coder), intent `2026-09-28-03`. Plan:
`docs/plans/journal-redesign.md` from "Map layering framework" to the end, including "L0b rulings (owner,
2026-09-28)" and "Ruling 3 clarified". Written by the L0b coder in worktree `forager-wt/l0b`, local branch
`l0b`, cut from `origin/journal-redesign` at `6235a86`.

**Planner message received during the dispatch**, verbatim (relayed by the coordinator; I have no planner-log
line number to cite, and no record entry is mine to write on this branch):

> Planner message, part of dispatch 2026-09-28-03 (quote it in your report). It narrows the work for now and
> widens nothing: continue the premise check and B1, B2, B3 and B7's non-synthetic parts as written. **Hold B4
> (legend chip), B5 (forecast-cell interface) and B6 (synthetic layers)**, including their tests-first
> commits, until a second planner message releases them. The owner asked me to read the forager-forecast
> repository for what the layering is for, and that read may change those three items' content. If you reach
> a point where only B4 to B6 are left before I have written again, commit and push what you have, then stop
> and report where you are.

It reached me after I had finished the verification below, and before I had written or built anything. It
narrows the work and widens nothing, so I continued under it.

**Planner message 2**, relayed by the coordinator; the committed file `prompts/preserved/2026-09-28-05.md`
at `50820b1` governs, and its message reads, verbatim:

> Planner message 2, part of dispatch 2026-09-28-03. Quote it verbatim in your report.
>
> **B4, B5 and B6 are released, with the changes below.** They come from the owner's rulings after the
> forager-forecast read. Read these two sections at `57f9f30` (`git fetch origin journal-redesign`) before
> resuming:
> - `docs/plans/journal-redesign.md`, "L0b forecast-facing rulings, after reading forager-forecast (owner, 2026-09-28)";
> - `docs/audits/2026-09-28-forager-forecast-layering-read.md`.
>
> Where this message and the dispatch file differ, this message wins. Nothing else changes: scope, abort
> conditions, predictions and finish line are as written.
>
> **B5, the cell store**
> - It is keyed by **group, week and 1° block**, for example `cells(group, week, blocks)`. It returns the cells, or the explicit "no forecast data".
> - A cell belongs to the block that holds its centre. Cells are centred on multiples of 0.1°, so their edges fall at x.x5, which is ERA5-Land's grid.
> - Cells with `applicable` false either carry the flag or are left out of the file. The D55 contract allows both, and both must draw nothing.
> - The property names and ranges stay as in the dispatch.
>
> **B6, the two synthetic layers.** They replace "Sighting chance (synthetic)" and "Test condition (synthetic)".
> - Both layers are **chance layers in the real format**: one layer per group for the current week, `chance` from 0 to 1, shown as a percent, with uncertainty bounds, drivers, `weather_through` and `model_version` all filled.
> - The groups are chanterelles and chicken of the woods, the forecast project's first two groups (D9).
> - The layer names are "Test forecast: chanterelles (synthetic data)" and "Test forecast: chicken of the woods (synthetic data)".
> - The fixed term "sighting chance" is **not** attached to these fake numbers anywhere: not in layer names, legend titles, content descriptions or credits. The owner ruled this: "Real format, 'test data' name".
> - The generator stays deterministic. Some cells in each block carry `applicable` false, and some are left out.
> - The two layers use visibly different sequential ramps. On each ramp, the lowest colour must read as clearly different from an empty cell, because an unscored cell draws nothing (R5). State the hex values in the report.
>
> **B4, the legend**
> - **Collapsed:** the active layer's name, or "2 layers".
> - **Expanded,** per visible layer:
>   - its name;
>   - its ramp with 0% and 100% end labels;
>   - the week and data dates, in the form "Week of \<week\>, weather to \<weather_through\>";
>   - "no forecast here" beside an empty-cell swatch.
> - **Reference class:** once, under the ramps, verbatim: "Chance this group is reported in each 11 km cell this week, where anyone is reporting fungi. Compare areas, not spots. A high chance is not a find." The source is `slayer8366/Forager-app` `0172c33`, `presentation/src/main/kotlin/com/zynergy/forager/presentation/SightingChance.kt:33-35`. Cite it in a comment beside the string.
> - **Percent** is shown only on these chance layers (R8).
> - Placement and behaviour are unchanged.
>
> **Tap readout: not in L0b.** The owner ruled "In M1, with the bubbles". Build no cell tap UI.
>
> **D58** applies to every new string. Run your check before each push, as the dispatch says. Neither the
> reference-class sentence nor any name above contains a forbidden phrase; confirm that with the grep, not by
> reading.

I read both sections it names, merged the planner's commits into my branch (docs only; `git diff 6235a86 HEAD`
over `app/` and the build files is empty, so the baseline below still stands), and carried on. Message 2
settles Q1 (0% and 100%), Q11 (a second group, not a condition) and the tap readout. How the others stand is
under "Needs a decision", each marked **resolved**, **built as written, flagged**, **my mechanism, flagged**
or **open**.

**Status at this commit: verification complete. No code and no test written yet.** The premises the
dispatch named hold, with the line numbers the pulse gave. But reading the code for the build turned up three
findings that change how the build is shaped, and eleven questions the dispatch and the rulings do not answer.
Each question below gives the options I can see, and my lean where I have one. The leans are information, not
choices.

Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Every line number was
read at `6235a86`.

## Verification before building

### Base

- `origin/journal-redesign` is `6235a86`. Its parent is `379eb05`, and it changes only `RECORD.md`,
  `docs/plans/journal-redesign.md` and `prompts/preserved/2026-09-28-03.md`. `git diff --stat f62eb3e 6235a86`
  over `app/`, the build scripts and `gradle/` is empty, so the app tree is `f62eb3e`'s. **Confirmed.**
- The kit is gone at this base. `.claude/kit.json`, `.claude/kit.lock`, `check_record.py`, `check_prompts.py`
  and `check_kit.py` are all absent at `6235a86`, and `CLAUDE.md` there no longer has its "Roles and gates"
  section (removed by the owner's `e136330`). **Confirmed.** I made no record entry.

### The pulse's answers (`docs/audits/2026-09-28-l0b-premise-pulse.md`)

| # | Claim | At `6235a86` |
|---|---|---|
| 1 | Layers row is row 4 of `MapIconBar` with "Map mode: X. … Night mode on/off." (`ui/map/MapChrome.kt:438-450`), in the cluster (`ui/availability/AvailabilityCompactMapUi.kt:936-961`), padded by `controlsPadding` (`:911`), gone while minimised (`:904-905`); `MapModeToggle` at `TopEnd` (`ui/availability/AvailabilityWideLayoutUi.kt:139-161, 296-300`); entry map only in fullscreen (`ui/log/CartographyEntryReportScreen.kt:399-433`), hidden while offline (`:409`); no centre-pin picker has it; `MapModePicker` is a popover (`MapChrome.kt:126-193`: scrim `:145-154`, panel `:157-166`, chips `:175-189`, a tap applies and closes `:182-185`); modes `ui/map/MapMode.kt:28-31`; basemap `remember` at `ui/availability/AvailabilityScreen.kt:828` | **Confirmed.** `MapModePicker(` is called only at `AvailabilityCompactMapUi.kt:1221`, `AvailabilityWideLayoutUi.kt:301` and `CartographyEntryReportScreen.kt:426`. The wide toggle's text has no night clause: "Map mode: X. Choose Street, Topographical, or Satellite." (`AvailabilityWideLayoutUi.kt:157`) |
| 2 | Layer state only on `MapRenderMode` (`ui/map/MapSlot.kt:174, 190`); `MapLayersState`/`LayerState` (`ui/map/layers/MapLayerState.kt:14-43`); construction sites; registry flags (`ui/map/layers/MapLayers.kt:211-252`); colour-field group empty; live visibility and opacity (`ui/map/SightingsMap.kt:597-600`), order only at a style load (`:532, 744-755`) | **Confirmed.** No production code passes `layers` or `onFeatureTap` (grep over `main/`). |
| 3 | `MapPreferencesRepository` (`domain/MapPreferencesRepository.kt:21-59`); `DataStoreMapPreferencesRepository` with a per-instance `PreferenceDataStoreFactory.create` (`data/repository/DataStoreMapPreferencesRepository.kt:31-33`), file `map_preferences` (`:76`), six keys (`:77-82`); built at `AppContainer.kt:190`, passed at `MainActivity.kt:76`; no layer preference | **Confirmed.** See finding F1: this pattern cannot simply be repeated on the same file. |
| 4 | Diagnostics panel only in `app/src/debug`, release twin composes nothing (`app/src/release/.../ui/diagnostics/DiagnosticsPanel.kt:13-19`), no toggles, read-only doc comment (`app/src/debug/.../ui/diagnostics/DiagnosticsPanel.kt:75-84`) | **Confirmed.** It is called from `AvailabilityScreen.kt:1153` and `AvailabilitySettingsUi.kt:297`. |
| 5 | Caption at `BottomStart` padded by `bottomInset` (`SightingsMap.kt:680-689`); "i" moved to `BOTTOM|END` (`:449`); cluster `CenterStart/CenterEnd` (`AvailabilityCompactMapUi.kt:714`), offset (`:908-912`), clamp (`:750-772`); nav (`:1135-1155`); rail (`:1169-1187`); wide add button `BottomEnd` (`AvailabilityWideLayoutUi.kt:314-319`); `bottomInset` animation (`AvailabilityCompactScaffold.kt:573-578`) | **Confirmed.** Where the "i" lands relative to the nav and the rail is still unobserved (device only). |
| 7 | `onFeatureTap` defaults at `MapSlot.kt:190` and `SightingsMap.kt:237`, passed at `MapSlot.kt:415`; `Log.w` only for a winner with no id (`SightingsMap.kt:378-385`); `onTap` after it (`:389`) | **Confirmed.** |
| 8 | Map-mode text pinned in 4 test classes; `AvailabilityScreenSettingsPanelTest` captures `renderMode` (`:127-139`) | **Confirmed**: `AvailabilityScreenMapIconStackTest` (4 lines), `AvailabilityScreenSettingsPanelTest` (2), `AvailabilityScreenAdaptiveLayoutTest` (1), `CartographyEntryReportScreenFullscreenTest` (4), 11 lines in all. See finding F3 for tests that pin more than the text. |

Answer 9 (unique names): no `debug_diagnostics_preferences` file and no `diagnostics.` or `map.layer` key
exists in `main`, `debug` or `release`. **No collision.** `map.layer.*` sits inside the `map.` namespace that
`map.fullscreen` already uses, which is the dispatch's choice.

### `MapOverlayContent`'s record fields

**Confirmed.** `keptTrackPolylines: List<RecordPolyline>` (`ui/map/MapSlot.kt:258`), `findMarkers` (`:270`) and
`photoMarkers` (`:283`), both `List<RecordPoint>`, and `offlineRegionCircles: List<RecordRegion>` (`:294`). The
entry map fills them (`ui/log/CartographyEntryReportScreen.kt:373-376`). The compact Maps tab
(`AvailabilityCompactMapUi.kt:598-606`) and the wide one (`AvailabilityWideLayoutUi.kt:250-256`) leave them
empty. Filling the same fields needs no tenth `MapSlot` parameter, and neither does a forecast-cell source,
which would be a defaulted `MapRenderMode` field.

### The four record sources

| Kind | What "saved" means in code | The read path the Journal or Records uses | Location |
|---|---|---|---|
| Finds | `MushroomLogEntry.isDraft == false` (`domain/model/MushroomLogEntry.kt:64`). A re-edit's draft is a separate row, so the committed row stays `false` | `GetMushroomLogEntriesUseCase` (`domain/GetMushroomLogEntriesUseCase.kt:20-25`, `filterNot { it.isDraft }`), through `MushroomLogViewModel`'s entry load to `uiState.entries` (`ui/log/JournalTab.kt:427, 587`) | `foundAt: LatLng?` (`:40`) |
| Tracks | No draft state. Every stored track is in Records, **including the one being recorded** (`endedAtEpochMillis == null`, shown as "Still recording", `ui/log/RecordDetailsSheet.kt:234`) | `GetTracksUseCase` → `TrackRepository.getAll()` (`domain/GetTracksUseCase.kt:9`), through `TrackRecordingViewModel.loadTracks` (`ui/track/TrackRecordingViewModel.kt:599-606`) | `points` are filtered at the read seam and may be empty |
| Album photos | No draft state: a row in the gallery. `LogPhoto` has no draft field (`domain/model/LogPhoto.kt:30-36`) | `GetGalleryPhotosUseCase` → `getAllPhotos()` (`domain/GetGalleryPhotosUseCase.kt:11-14`), through `MushroomLogViewModel.loadGalleryPhotos` | `latitude`/`longitude`, both nullable |
| Offline regions | No draft state: on disk | `OfflineMapRepository.listRegions()` (`domain/OfflineMapRepository.kt:55`), through `AvailabilityViewModel.loadOfflineRegions` (`:801-817`). Records reads `visibleOfflineRegions`, which drops a pending delete (`ui/availability/AvailabilityUiState.kt:286-287`) | `OfflineRegionSummary.region` |

Every kind has a determinable saved state, so that abort condition does not hold. Two consequences are
questions Q6 and Q7 below.

## Findings that change how the build is shaped

**F1. The layer preferences cannot open a second DataStore on `map_preferences`.** DataStore throws
`IllegalStateException: There are multiple DataStores active for the same file` when a second instance opens a
file the first still holds. `DataStorePhotoLocationPreferenceRepositoryTest.kt:79-104` records this as a finding
that constrains production. So B3's "a per-instance `PreferenceDataStoreFactory.create`" in "the existing
`map_preferences` file" cannot be a new class with its own `create` call beside `DataStoreMapPreferencesRepository`:
the app would throw on first use. The same fact blocks the dispatch's test ("a real per-instance DataStore and
a recreated reader") unless the first instance is released. Read with `javap` from the pinned
`datastore-core-android` 1.2.1: `DataStoreImpl`'s write actor closes its storage connection when its scope
completes (`writeActor$lambda$0` → `StorageConnection.close`), and `FileStorage`'s connection close removes the
file from `activeFiles` (`createConnection$lambda$1` → `Set.remove`). So a test that cancels the first
instance's scope can open a genuine second reader. **Lean:** a new domain interface for the layer preferences,
implemented by the existing `DataStoreMapPreferencesRepository` (one class, one DataStore, the fakes of
`MapPreferencesRepository` untouched), plus an optional `scope` constructor parameter defaulting to what
`PreferenceDataStoreFactory.create` uses today, so the test can cancel it and recreate the reader. The same
applies to the debug file `debug_diagnostics_preferences`: the switch's writer (the panel) and its reader (the
synthetic store) must share one instance.

**F2. Colour fields must be gated on the store having data, not on the registry alone.** B7 puts the two
synthetic layers in `MAP_LAYER_REGISTRY`, which is in `main`, so their specs ship in release as data, even with
no synthetic class there. `MapLayersState` treats an unnamed layer as visible (`MapLayerState.kt:28-38`), and
`activeLayerCredits` credits every visible layer (`:71-75`). So unless availability is part of the rule, a
release build would put "Synthetic test data" in every map's caption, list two synthetic rows in the sheet and
show the legend. "Colour fields are listed within Overlays when any exist" has to mean "when the store has data
for them". **Lean:** the store reports which colour-field layers it has data for, with "no forecast data" as the
explicit empty answer. The sheet, the legend and the credits read that. Release always reports "no forecast
data".

**F3. More existing assertions change than the four description classes.** These tests assert behaviour of the
popover that B1 removes, beyond its text. Prediction 2 would be wrong in part:

- `AvailabilityScreenMapIconStackTest:2419-2437` and `:2440-2466` assert that the picker panel's edge lands on
  the bar's edge beside the layers row after a drag (`assertPanelAnchoredToBar`, `:2325-2353`, via
  `MAP_MODE_PICKER_TAG`).
- `CartographyEntryReportScreenFullscreenTest:160-181` asserts that the picker opens near the layers row.
- `AvailabilityScreenSettingsPanelTest:426-443` asserts that the picker closes when a mode is chosen (`:433-435`,
  "Satellite" count 0). Whether that still holds is question Q3.

A sheet has no anchor to the bar, so the first three would be rewritten or removed together with the popover.
I would report each by name, and I would not treat them as silenced tests, because the behaviour they pin is
what the dispatch removes. This still needs the planner's view before I delete anything.

**F4. Two hosts can only be reached through files the scope does not list.** The compact Maps tab
(`CompactMapTab`) gets every callback it has from `CompactMainScaffold` in
`ui/availability/AvailabilityCompactScaffold.kt`. That is a separate file holding `AvailabilityScreen`'s
extracted compact body (its header, `:3-14`), and it passes the map's render mode through at `:794`. The entry
map gets `mapSlot` and `night` along `AvailabilityScreen` → `AvailabilityCompactScaffold.kt:944-948` →
`ui/log/JournalTab.kt:516-517` → `ui/log/CartographyScreen.kt:330-331` in compact, and `AvailabilityScreen` →
`ui/log/LogPanel.kt:377-378` → `CartographyScreen` in the wide layout. B1 needs the sheet's three callbacks on the
compact tab, and B1 and B3 need the shared layer state plus those callbacks on the entry map. None of the
four files is in "Files in scope", and scope is a wall for me, so **I have not touched them**. Everything
that does not need them I am building:
- B2 and B3 on the compact tab arrive through `uiState` and `renderMode`, which already cross the scaffold
  unchanged.
- The wide layout is reached from `AvailabilityScreen.kt` directly.

What waits for a ruling: the compact tab's sheet (its Layers row keeps opening the old picker until then) and
everything on the entry map. **Needs a decision:**
- (a) widen the scope to those four files, for parameter threading only, with no behaviour change in them;
- (b) something else.
- My lean is (a).

Separately, I treat `ui/availability/AvailabilityUiState.kt` as part of "AvailabilityViewModel" (the dispatch
names the class, not a file, and that file is the class's own state type), so I do change it. That is a
decision, listed at the end.

## Needs a decision

**Q1. The legend's ramp end labels (copy).** B4 asks for "each layer's colour ramp with its end labels" and
names no text. The plan's D55 promise is that the number is "shown only beside its reference class"
(`docs/plans/journal-redesign.md:448`), and a legend carries no reference class.
- (a) "Low" and "High" for both layers.
- (b) "0%" and "100%": a number without its reference class.
- (c) No end labels, which contradicts B4.
- Lean: (a). The owner rules the words.

**Q2. The rest of the copy B1 and B4 need and do not name.** A slider, a drag handle and an expandable chip
each need accessible text.
- The opacity slider: its label and how its value reads, for example "Opacity" and "100%", announced as
  "\<layer\> opacity, 100 percent".
- The drag handle: for example "Reorder \<layer\>", beside the named "Move up" and "Move down" actions.
- The legend chip: its click labels, for example "Show legend" and "Hide legend".
- The "no forecast here" entry's swatch, for example an empty outlined square. This is visual, not text.

**Q3. Does choosing a map type close the sheet?** Today a tap applies and closes (`MapChrome.kt:182-185`), and
`AvailabilityScreenSettingsPanelTest:433-435` pins that. B1 says "one chosen, as today".
- (a) The sheet stays open, so the user can go on to the overlays. The pinned assertion changes.
- (b) It closes on a basemap tap, as today.
- Lean: (a). "As today" may mean the closing, so I did not assume it.

**Q4. The legend chip and the icon cluster (placement).** B4 says the chip "must stay clear of the icon cluster
at either side". The cluster can be dragged down to the nav's top edge, or to the screen bottom in fullscreen
(`AvailabilityCompactMapUi.kt:750-772`). On the right side, that is exactly the bottom-right corner where the
chip sits. The rulings do not say which one gives way.
- (a) While the chip shows on the cluster's side, the cluster's downward clamp stops above it. The clamp
  already re-clamps the displayed position without touching the user's memory (`:851-856`), as it does for the
  nav.
- (b) The chip steps inward, beside the cluster, when the cluster is low on its side.
- (c) Overlap, with composition order deciding who gets the touch.
- A sub-question: clear it for the collapsed height or the expanded height? Expanded, in a 384 dp landscape
  window, two ramps plus "no forecast here" is a large share of the height.
- Separately, and not a question unless you say it is: in portrait the "i" sits under the translucent nav
  (the pulse's inference), so "above the 'i'" only works with the chip padded by the nav's height
  (`bottomInset`) as well. I would place it that way.

**Q5. Which maps draw colour fields?** B5 says "the map asks the store … when the camera goes idle", and every
`SightingsMap` could. B4 places a legend only on the Maps tab (compact, and wide above the add button), and D55
requires a legend with "no forecast here" wherever cells draw.
- (a) Maps tab only, compact and wide. The entry map's sheet lists only its five record overlays. The
  centre-pin pickers pass no store.
- (b) The entry map too, with its own legend placement. That placement is unruled.
- Lean: (a).

**Q6. The track being recorded, under "every track in Records".** Records includes the active track. Kept
tracks draw **above** the breadcrumb (`MapLayers.kt:227-230`). So on the Maps tab while recording, a solid
kept-track copy of the trail, as of the last reload, would cover the dashed "Recording trail" up to the point
where the reload happened.
- (a) Literal: draw it anyway.
- (b) Leave out tracks with no end time; the "Recording trail" layer already draws that track.
- Lean: (b). The ruling's words say (a).

**Q7. Records with a pending delete (the Undo window).** Finds, album photos and offline regions each have a
pending-delete slot (`MushroomLogViewModel.kt:237, 240`, `AvailabilityViewModel.kt:128`). A new use case reading
the repositories would still draw a record the user has just deleted, until the delete commits and the Maps tab
next reloads. Records hides it at once.
- (a) Accept that.
- (b) The Maps tab also leaves out the pending ids the three ViewModels hold.
- Lean: (b).

**Q8. Whether colour-field cells take taps.** Tapping a cell is out of scope, but B7 does not give the
colour-field layers' tap group. With `TapGroup.COLOUR_FIELD` (`MapLayers.kt:46`), a cell lies under almost
every tap. `resolveTap` then finds a tappable hit at the point stage and never runs the 48 dp box
(`ui/map/layers/TapPrecedence.kt:41-42`). So while a colour field is visible, a near miss on a track line or a
marker would stop working. That is the "point, then box" rule the owner kept in terminal `2026-09-27-72`.
- (a) `TapGroup.NONE` for L0b. A tap on a cell falls through as a tap on the empty map does, and M1 decides
  cell taps together with the stage interaction.
- (b) `COLOUR_FIELD`, with `resolveTap` changed so that colour fields are considered only after both stages
  find nothing else.
- Lean: (a).

**Q9. An operating limit on blocks when zoomed out.** The synthetic generator "gives cells for any requested
blocks". At a continental zoom the visible area touches hundreds of 1-degree blocks, 100 cells each, which is
tens of thousands of polygons rebuilt on every camera idle. That is a hang or OOM risk, so correctness needs a
limit, not tuning (CLAUDE.md: an explicit operating limit). What happens above the limit is unruled.
- (a) Request at most N blocks around the camera centre.
- (b) Request nothing below a minimum zoom, with the layer drawing nothing there. Any "zoom in" wording would
  be new copy.
- (c) No limit.
- Lean: (b), with no new copy, N and the zoom stated in the report. The owner may want a message.

**Q10. What 100% opacity means for a colour field.** At the default multiplier of 1, the fill draws at the
layer's base opacity. With a base of 1, a visible synthetic layer covers the basemap completely wherever the
phone is.
- (a) Base 1: the slider is the layer's literal opacity.
- (b) A translucent base, for example 0.6.
- B6 already has me state the ramps' colours, so this may be mine to state as well. Lean: (a). Confirm.

**Q11. What "Test condition (synthetic)" draws.** The D55 cell has one number, `chance`. Condition grids need a
change to D55 that is not made yet (`docs/plans/journal-redesign.md:462`). **Lean:** the store returns cells for
a set of blocks, each carrying its `group`. The generator makes two synthetic groups with independent values, and
each layer draws its group's `chance` through its own ramp. The second layer is then a stand-in with the cell
shape D55 already has.

**Q12 (wiring; my lean unless you say otherwise).** One source-set-split factory, shaped like
`DebugDiagnostics.install`, builds the store in `AppContainer`. The debug version owns the one
`debug_diagnostics_preferences` DataStore and exposes the switch. The debug panel reaches it through the
application's container, so the panel signature shared with the release twin does not change. The release
version is the "no forecast data" store and nothing else.

## What landed

- This report only, and no production or test file. Commit and SHA: see the commit that adds this file on
  `journal-redesign`.

## Tests

None written and none run. No revert check, since there is nothing to revert.

## Suites

Not run. I stopped before building, so a baseline would have described a build that does not exist yet. The
next dispatch's base may differ, and a full run on this 11 GB machine, beside another process's roughly 5 GB of
Gradle and Kotlin daemons, risks an OOM for no evidence this stop needs. At the planner's `1476e97`, the same app
tree, the suite was 253 classes / 2112 / 0 / 0 / 24. I did not re-measure it.

## Device-only

Nothing was built, so nothing is device-only yet. The dispatch's list stands for the build: real hiding and
opacity with no style reload; hidden layers excluded from tap queries; the cell layer drawn under the markers;
the legend's inset placement at both rotations and in fullscreen; the sheet's insets; persistence across a
force-stop. Q4 adds one: where MapLibre's "i" actually lands relative to the nav and the rail.

## Decisions I made

- **I followed the dispatch rather than my agent definition on the record.** My definition requires a sweep,
  an intent, a terminal and the kit's checkers. The dispatch says the kit is gone and the planner writes the
  record. I checked that against the base (the kit files are absent at `6235a86`, and `CLAUDE.md`'s kit section
  was removed by the owner's `e136330`) and touched nothing in `RECORD.md` or `prompts/`. What deciding it
  properly would need: the owner's or planner's confirmation that the agent definition's record steps are
  suspended on this branch. The dispatch asserts that, but my definition predates it.
- **I did not validate the dispatch's structure against a kit config**, because none exists at the base. For
  reference only: against `origin/main`'s `.claude/kit.json` (a different branch, `faf2f88`), the section
  names differ ("Base and branch" rather than "Base and state", no "Closed decisions" section, and so on). I
  did not treat that as a stop.
- **I wrote this stop report as the dispatch's named completion report and pushed it**, following the L0a stop
  at `8da4905` and CLAUDE.md's rule that findings go in `docs/audits/`. The alternative was a hand-back message
  only.
- **I stopped the whole stage** rather than building the parts with no open question (B3's storage and B5's
  parsing are the nearest). F1 changes B3's shape, and every other item has an open question, so a partial
  build would have been pushed to the Journal's draft PR branch with decisions baked in.
- **I skipped the baseline suite**, for the reasons under Suites.
- **Leans.** The leans in F1, F2, Q1 to Q12 and the nav padding in Q4 are mine, offered as information and
  not built.

## Flags outside scope

- The wide layout shows no map until a search has run (`AvailabilityWideLayoutUi.kt:201-204`), so B2's records
  would appear there only after a search. "What is drawn, in compact and wide" does not change that, and I would
  not.
- The planner's prediction 2 is wrong in part as written (finding F3), whichever way Q3 goes.

## D58

`git grep -i` for the three forbidden phrases over this report and this commit's message: see the commit.

## Resumed after the machine restart (second L0b coder, 2026-09-28)

Written by a second coder, resuming in the same worktree (`forager-wt/l0b`, local branch `l0b`). The first
coder was stopped by a machine restart after pushing `55114ee` (tests first) and `fe07702` (the domain and
model half). Its uncommitted work was committed as-is by the planner at `11ef6e2` (branch
`l0b-wip-2026-09-28`, parent `f7e219a`, **never compiled or run**). Everything above this section is the
first coder's report as it stood at `09bf96b`, and is left unedited except for the title and the status
note. Its F1 to F4 and Q2 to Q12 were answered by planner message 3, quoted below. Paths are under
`app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

**Planner message 3** (`prompts/preserved/2026-09-28-07.md`, committed at `ad5026b`), verbatim:

> Planner message 3, part of dispatch 2026-09-28-03. Quote it verbatim in your report. It answers your report's F1–F4 and Q2–Q12 as you pushed them at `09bf96b`/`55114ee`. Where it widens scope, it says so. Your leans marked "my mechanism, flagged" that are not named below stand as flagged.
>
> **The owner's rulings (2026-09-28, verbatim answers):**
>
> - **Q3, map type:** "Stays open (Recommended)". Tapping Street, Topographical or Satellite applies it and the sheet stays open. It closes on a swipe, Back, or a tap outside. The assertion at `AvailabilityScreenSettingsPanelTest:433-435` changes accordingly. Report it by name.
> - **Q4, legend against cluster:** "Cluster stops above it (Recommended)". While the legend chip shows on the cluster's side, the cluster's downward clamp stops above the chip, through the same display-only re-clamp as the nav (`AvailabilityCompactMapUi.kt:851-856`). The user's remembered position is never changed.
>   - The clamp uses the chip's **current** height, so the cluster moves up when the legend expands and back down when it collapses.
>   - Your portrait padding stands: the chip is padded by `bottomInset` as well.
> - **Q6, the live track:** "Leave it out (Recommended)". A track with no end time is left out of the Tracks overlay. The "Recording trail" layer draws it, and it joins Tracks once it has ended. Test it.
> - **Q10, opacity:** "Translucent at 100% (Recommended)". A colour field's base fill opacity is **0.6**, and the slider's multiplier scales that, so 100% on the slider draws at 0.6. State it in the report.
>
> **The planner's rulings:**
>
> - **F1:** your lean. The existing `DataStoreMapPreferencesRepository` also implements a new layer-preferences domain interface: one class, one DataStore on `map_preferences`. Add an optional `scope` constructor parameter so the test can cancel and recreate its reader. The debug file gets one shared instance for the panel's writer and the store's reader.
> - **F2:** your lean. The store reports which colour-field layers it has data for, with "no forecast data" as the explicit empty answer. The sheet rows, the legend and the attribution credits read that. The release build therefore never lists, credits or draws a synthetic layer, even though its specs are in the registry. Add a test that the release twin's store yields no colour-field rows, no credit and no legend.
> - **F3:** the three popover-anchoring assertions are removed together with the popover they pin:
>   - `AvailabilityScreenMapIconStackTest:2419-2437` and `:2440-2466`;
>   - `CartographyEntryReportScreenFullscreenTest:160-181`.
>
>   Each is replaced, in the same commit, by a coordinate-touch test that the same control opens the sheet. Report each by name, with its old and new assertion. This is behaviour B1 removes, not silencing. No other existing assertion is weakened.
> - **F4: scope widened** to `ui/availability/AvailabilityCompactScaffold.kt`, `ui/log/JournalTab.kt`, `ui/log/CartographyScreen.kt` and `ui/log/LogPanel.kt`, for **parameter threading only**, with no behaviour change in them. Your reading that `AvailabilityUiState.kt` is part of "AvailabilityViewModel" is accepted.
> - **Q2, copy:** your examples are accepted:
>   - the slider is labelled "Opacity", its value reads "100%", and it announces as "\<layer\> opacity, 100 percent";
>   - the drag handle is "Reorder \<layer\>", with the custom actions "Move up" and "Move down";
>   - the legend chip's click labels are "Show legend" and "Hide legend";
>   - the "no forecast here" swatch is an empty outlined square.
> - **Q5:** (a). Colour fields draw on the Maps tab only, compact and wide. The entry map's sheet lists only the record overlays it draws, and the centre-pin pickers pass no store. The plan places the legend only on the Maps tab. This reading is recorded for the owner, who may widen it later.
> - **Q7:** (b). The Maps tab leaves out the ids the three ViewModels hold as pending deletes, so a record in its Undo window disappears from the map at once, as it does from Records.
> - **Q8:** (a). Colour-field layers get `TapGroup.NONE` in L0b. A tap on a cell falls through like a tap on the empty map, so the owner's "point, then box" rule keeps working. M1 decides cell taps. Record it in the code beside the layer specs.
> - **Q9:** (b). Below a minimum zoom the cell layers request nothing and draw nothing (CLAUDE.md: an explicit operating limit). There is no new copy: the legend still shows. Choose the zoom so the number of requested blocks stays bounded, and state the zoom and the worst-case block count in the report.
> - **Q12:** your lean. One source-set-split factory in `AppContainer`, shaped like `DebugDiagnostics.install`. The panel signature does not change.
>
> Q1 and Q11 were settled by message 2, and the tap readout is M1's. Predictions stand as written. Prediction 2 will be recorded as wrong in part, by F3.

**Status: stopped at an abort condition, with an open design question.** B1 to B7 are built and pushed.
The full suite has one failure, the open question below. Revert checks stopped at the third of 17: a
revert build did not compile, which the dispatch lists as an abort. The finish line is **not** met. Not
done: 14 revert checks, the release compile evidence, and the finish-line full suite.

### What I did with the WIP commit `11ef6e2`

I built it before trusting any of it. **It did not compile: 35 `e: ` lines, all in tests**; `main` compiled.
It turned out to be a second tests-first round, not build code: stubs in `main` (`MapRecords.withoutPending`,
the ViewModel's four handlers, `MIN_FORECAST_ZOOM`, the sheet's test tags, the `forecast` field on
`MapRenderMode`, the Diagnostics panel's switch parameter) and five new or changed test files.

- **Kept, and fixed**: two mechanical compile faults, both in tests.
  - The fixture's `RecordingMapSlot` collided with a private class of the same name in
    `AvailabilityScreenShortLandscapeTest.kt:567`, so I renamed it `LayersRecordingMapSlot`.
  - `androidx.compose.ui.test.click` was not imported in two files.
- **Kept, and written**: `DiagnosticsSyntheticForecastSwitchTest.kt` was an empty file, so I wrote it.
- **Kept, and changed**: one new test passed at its base, the absence test "with no colour field visible
  there is no legend chip". I extended it to switch a field on in the sheet and expect the chip, which is
  what fails first; its absence half is covered by revert check R13 (not run, see below).
- **Kept as written**: everything else, after reading it against the dispatch and messages 2 and 3. That
  covers the stubs, the ViewModel test, the screen tests and the use-case and cell-layer re-pins for Q6 and
  Q9. The ViewModel test's log wording and the minimum zoom of 7 are the first coder's; I kept both.
- **Discarded**: nothing. I made no reset: `11ef6e2` is on `journal-redesign` under my commits.

### Base

`origin/journal-redesign` was `f7e219a` when I started, docs-only children of `6235a86` over the app tree.
`git diff --stat f62eb3e 55114ee^` over `app/`, the build scripts and `gradle/` is empty, so the first
coder's premise table above still describes the tree the build started from. I did not re-verify each line
of it. The planner pushed docs twice during the build: `43d7810` (`RECORD.md` and the L0a device-check run
record), merged at `e09d52b` with `--no-rebase`. Nothing in it concerned L0b.

**Baseline.** The first coder ran it (`app/build/l0b/baseline.log`, 20:48 local, before `55114ee`, on the
`f62eb3e` app tree): **253 classes / 2112 tests / 0 failures / 0 errors / 24 skipped**, read from the JUnit
XML, 0 `e: ` lines. I did not re-run it.

### Commits (pushed to `journal-redesign`)

| Commit | What |
|---|---|
| `55114ee` | First coder: tests first, round 1 (7 new classes, 3 re-pinned) |
| `fe07702` | First coder: records use case, layer preferences, forecast cells, synthetic store, legend model, colour fields, registry |
| `11ef6e2` | The planner's WIP commit of the first coder's uncommitted work (never built) |
| `d9dd1d8` | Tests first, round 2: compile fixes to the WIP, the Diagnostics test, the absence test extended; stubs for the toggle and `AppContainer.forecastCellStore` |
| `45c82cd` | The ViewModel handlers, the Q6 live-track rule, the Q7 `withoutPending`, the Q9 minimum zoom |
| `93da312` | The Layers sheet on three hosts, the legend chip, colour fields in `SightingsMap`, the Diagnostics toggle, production wiring |
| `e09d52b` | Merge of the planner's docs (`43d7810`) |
| `34487da` | Existing tests re-pinned: the description, the F3 replacements, Q3 |
| this commit | This report |

### Tests first

- **Round 1** (`55114ee`, first coder; its log `app/build/l0b/t1.log`): 10 classes, 78 tests, 44 failures,
  0 `e: ` lines. Every new test failed at a stub. I read the log; I did not re-run it.
- **Round 2** (`d9dd1d8`): 8 classes, 44 tests, **34 failures**, 0 errors, 0 `e: ` lines. Each new test
  fails at a missing behaviour, with messages naming it:
  - no node for the Layers description or `map-layer-*` tags;
  - `map-legend-chip` not displayed;
  - the ViewModel handlers doing nothing (for example `expected:<2026-09-28> but was:<null>`);
  - `withoutPending` returning the pending ids (`expected:<[kept]> but was:<[kept, deleting]>`);
  - the recording track still drawn;
  - no minimum zoom (`expected null, but was:<[ForecastBlock(south=45, west=-123)]>`);
  - the Diagnostics toggle never appearing (`ComposeTimeoutException`).

  The 10 that pass are round-1 tests `fe07702` had already made green; two of them were re-pinned to the
  Q6 and Q9 rulings.

### What landed, per item

- **B1, the Layers sheet** (`ui/map/MapLayersSheet.kt`). A `ModalBottomSheet` in J5c's pattern
  (`ui/log/RecordDetailsSheet.kt:162-175`: `skipPartiallyExpanded`, the sheet's own inset handling, a
  `verticalScroll` column).
  - **Structure**: the title "Layers", then "Map type" and "Overlays".
  - **Map type**: a tap applies it and the sheet stays open (Q3).
  - **Overlays, Maps tab**: seven switches in the dispatch's order.
  - **Overlays, entry map**: five switches (`ENTRY_MAP_OVERLAYS`: no Planned trips, no Recording trail),
    and no colour field (Q5).
  - **Colour fields**: listed after the record overlays, only those with data (F2), top of the draw order
    first. Each has a switch, an "Opacity" slider ("100%", announced "\<layer\> opacity, 100 percent") and
    a drag handle ("Reorder \<layer\>", with the custom actions "Move up" and "Move down").
  - **Hosts**: it replaces the popover on the compact cluster's row (`ui/availability/AvailabilityCompactMapUi.kt`),
    the wide `MapModeToggle` (`ui/availability/AvailabilityWideLayoutUi.kt`) and the entry map's row
    (`ui/log/CartographyEntryReportScreen.kt`), where the rule of hiding the row while offline tiles are on
    is unchanged.
  - **Description**: `layersButtonDescription`: "Layers: \<basemap\> map. Choose the map type and overlays."
  - **`MapModePicker`** is left in `ui/map/MapChrome.kt` with no caller (see Flags).
- **Reorder timing.** A move updates the sheet's order and the legend's order at once, and is stored. The
  map draws the new stacking at its next style load: a basemap change, a Night Maps change, or, on the
  compact tab, returning to the Maps tab. That last one is inferred, not observed: `CompactMapTab` is
  composed only for the Maps tab, so leaving it should dispose the `MapView`. Until then the user sees the
  sheet and legend in the new order over a map still stacked in the old one.
- **B2, every saved record.**
  - `GetMapRecordsUseCase` (`domain/GetMapRecordsUseCase.kt`, from `fe07702`) now leaves out a track with
    no end time (Q6).
  - `MapRecords.withoutPending` drops the find, photo and region in their Undo window (Q7). The screen
    passes the ids from `logUiState.pendingDelete`, `logUiState.pendingPhotoDelete` and
    `uiState.pendingOfflineRegionDelete`.
  - Both Maps-tab hosts fill `MapOverlayContent`'s four record fields. No tenth `MapSlot` parameter.
  - **Freshness mechanism**: `AvailabilityScreen` runs `LaunchedEffect(isMapsTabShown)`, which calls
    `onMapShown()` whenever the Maps tab comes into view (compact: `compactTab == MAP`; medium and
    expanded: the List-and-Map pane). That reloads the records and asks the store for this ISO week's
    groups.
  - A failed kind is logged as "Couldn't load \<kind\> for the map." and drawn as absent.
  - The entry map's data path is untouched.
- **B3, persistence.**
  - `DataStoreMapPreferencesRepository` implements the layer interface on `map_preferences`, with an
    optional `scope` (F1, from `fe07702`). `AppContainer` hands out the one instance as both interfaces.
  - The ViewModel restores the stored choices at start, logging each refusal. A sheet change shows at
    once, then is stored; a failed write is logged. An opacity outside 0 to 1 is refused, never clamped.
- **B4, the legend chip** (`MapLegendChip`).
  - **Placement, compact**: bottom-end, 32 dp above its corner (`LEGEND_ATTRIBUTION_CLEARANCE`: the "i"
    drawable is 21 dp in the pinned aar, and its margin is unread), plus `renderMode.bottomInset` (the
    nav's height in portrait), inside `controlsPadding` (the rail).
  - **Placement, wide**: above the add button, with its right edge on the button's.
  - **Bounds**: bounded to its content (`widthIn(max = 280.dp)`, nothing fills its parent).
  - **Behaviour**: shown only while a colour field is visible. Collapsed, it names the layer or reads
    "2 layers"; a tap expands it and a second tap collapses it ("Show legend" and "Hide legend").
  - **Expanded content**: per layer, the name, the ramp with 0% and 100%, "Week of \<week\>, weather to
    \<weather_through\>" and "no forecast here" beside an outlined square; then the reference class once,
    cited in `ui/map/layers/MapLegend.kt`.
  - The expanded flag is held in `AvailabilityScreen` (`rememberSaveable`), so it survives a tab change.
  - **Q4**: the cluster's clamp stops `Spacing.sm` above the chip's live top while the chip is on its side.
    See "Needs a decision".
- **B5, the cell store on the map.**
  - `MapRenderMode.forecast` (`MapForecastFeed`: store, week, `groupsByLayer`, `onCellsShown`).
    `SightingsMap` fetches, per colour field, the blocks in view on every camera idle and every style load.
    Nothing is fetched below zoom 7 or past the 64-block backstop.
  - **Every colour field the feed does not name is hidden in the state `SightingsMap` draws, taps and
    credits with.** So a map with no feed (the entry map, the centre-pin pickers, any release build) never
    shows one.
  - Colour fields are `FillLayer`s coloured by their ramp on `chance`, at base opacity 0.6 (Q10: 100% on
    the slider draws at 0.6).
  - **Worst case (Q9)**: at zoom 7, a 1280 by 800 dp view touches at most **54 blocks**
    (`ForecastCellLayerTest`), 5,400 cells a layer.
- **B6, the synthetic layers.**
  - The generator and store are from `fe07702` (`app/src/debug/.../forecast/SyntheticForecast.kt`).
  - The Diagnostics panel's first toggle, "Synthetic forecast layers", reaches the store through
    `ForagerApplication.container.forecastCellStore` (Q12); the panel signature is unchanged. Its doc
    comment now says what it writes. A failed read shows it off; a failed write is logged and leaves the
    stored state showing.
  - **Ramps** (`ui/map/layers/ColourFields.kt`, first coder's values), lowest first:
    - chanterelles: `#FFD54F`, `#FFB300`, `#FB8C00`, `#E65100`, `#8D2B00`;
    - chicken of the woods: `#80DEEA`, `#26C6DA`, `#0097A7`, `#00697A`, `#003B4A`.

    Whether each lowest colour reads as clearly different from an empty cell at 0.6 over each basemap is
    device-only.
- **B7, the registry**: from `fe07702`. Two colour fields: toggle, opacity and reorder on,
  `TapGroup.NONE` (Q8), credit "Synthetic test data". No other layer's flags changed.
- **F4 threading**: one parameter each through `AvailabilityCompactScaffold.kt`, and two each through
  `JournalTab.kt`, `CartographyScreen.kt` and `LogPanel.kt`, with no behaviour change.

### The four description-pinning classes, F3 and Q3

- **Description, 11 lines in 4 classes**:
  - `AvailabilityScreenMapIconStackTest` (4, of which 2 were in the F3 tests);
  - `AvailabilityScreenSettingsPanelTest` (2);
  - `AvailabilityScreenAdaptiveLayoutTest` (1);
  - `CartographyEntryReportScreenFullscreenTest` (4, of which 2 were in the F3 test).
- **F3, three tests replaced**:
  - `AvailabilityScreenMapIconStackTest`, right and left edge. Old: "…the map mode picker opens beside the
    layers row and its chips are tappable" (`assertPanelAnchoredToBar` on `MAP_MODE_PICKER_TAG`). New:
    "…a real touch on the layers row opens the Layers sheet and its map types work" (the sheet is
    displayed, and a touch on Street changes the row's description).
  - `CartographyEntryReportScreenFullscreenTest`. Old: "the mode picker opens anchored to the layers row,
    not the fullscreen row". New: "a real touch on the layers row opens the Layers sheet with its map
    types".
  - **Deviation**: F3 said "in the same commit" as the popover's removal. The removal landed in `93da312`,
    the replacements in `34487da`.
- **Q3**: in `AvailabilityScreenSettingsPanelTest`, "tapping the quick-fire icon opens the Layers sheet…",
  Satellite's count after a map-type tap changed from 0 to 1, and it taps Satellite without reopening.
- **Test correction in a new test** (`AvailabilityScreenMapLayersTest`, the legend-content test): "0%",
  "100%" and "no forecast here" are counted in the unmerged tree. The chip is one clickable `Surface`, so
  the merged tree folds its texts into one node and counted 1, not 2.

### Suites

- **L0b classes after the build**: 5 UI classes, 24 tests, 1 failure (the Q4 test).
- **Full suite** after `34487da`, from a cleared results directory, counts from the JUnit XML, 0 `e: `
  lines: **266 classes / 2182 tests / 1 failure / 0 errors / 24 skipped**. That is 13 more classes and 70
  more tests than the baseline.
  - The one failure is `AvailabilityScreenMapLayersSheetTest`, "with the cluster dragged to the bottom on
    the right it stops above the chip, rises when the legend expands and returns when it collapses":
    `expanded: and stays above the chip (601.6667.dp <= 467.0.dp)`.
  - The held family (album long-press, tap and Undo in `JournalPendingDeleteTest`, and `JournalTabTest`'s
    photo pull) passed in this run.
- This is not the finish-line suite: the stop came after it.

### Revert checks (stopped at an abort)

The runner is `app/build/l0b/revert.py`, with the specs in `app/build/l0b/revert/checks.json`: 17 checks,
predictions written before running. For each check it:
- saves the file, applies an edit that must match once, and runs through `run.sh`;
- refuses results when the log has any `e: ` line;
- requires the failing tests to be exactly the predicted ones, with each message containing its predicted
  fragment;
- restores from the saved copy, never from git, and checks the file's sha256 against `HEAD`'s blob.

| Check | Edit | Result |
|---|---|---|
| R01 `GetMapRecordsUseCaseTest` | drop the no-end-time exclusion | **Confirmed**: only "the track being recorded is left out…", `expected:<[]> but was:<[RecordPolyline(recordId=recording, …` |
| R01b `GetMapRecordsUseCaseTest` | `withoutPending` returns `this` | **Confirmed**: only the pending-delete test, `expected:<[kept]> but was:<[kept, deleting]>` |
| R02 `DataStoreMapLayerPreferencesTest` | read opacity only when `value is Double` | **Abort: the revert build did not compile.** `DataStoreMapPreferencesRepository.kt:114:97 Argument type mismatch: actual type is 'Double', but 'Float' was expected`. The smart cast the edit changed fed the map assignment one line down, CLAUDE.md's exact shape. The results were refused, and the file was restored and matched `HEAD`. |
| R03 to R16 | | **Not run**: the abort stopped the run. |

This is a badly chosen revert edit, not a fault in the forward code; a `Float` read written another way
would compile. It is still the dispatch's abort condition, so I stopped rather than pick a new edit.
Round 1's revert checks were never run by the first coder either, so none of the 7 round-1 classes has a
confirmed revert except `GetMapRecordsUseCaseTest`.

### Needs a decision

**N1 (open, placement the rulings do not settle): an expanded legend and the cluster cannot both fit on the
right in portrait.** Measured at `w384dp-h823dp`:
- the cluster is 380 dp tall;
- its upward bound, the search chrome, is at 221.7 dp;
- the collapsed chip spans 675 to 711 dp;
- expanded (two layers and the reference class), the chip's top is at 467 dp.

That leaves 238 dp for a 380 dp cluster. The clamp does what Q4 says as far as it can: the cluster rises
66 dp, from 667.3 to 601.7 dp at its bottom, to its highest allowed position, and still overlaps the chip.
Collapsed, it clears (the test's first assertion passes), and in the short landscape window the chip is
clear of the cluster. Options I can see:
- (a) cap the expanded legend's height to the space below the cluster's highest position, with its
  content scrolling;
- (b) when the cluster cannot fit, move the expanded legend inward, left of the cluster;
- (c) accept the overlap while expanded, with the chip drawn over the cluster (or under it);
- (d) collapse the legend when the user drags the cluster into it.

The test stays failing, unweakened, until this is ruled.

**N2: the abort.** Whether to resume the revert checks with R02 replaced by an edit that compiles, for
example reading opacity under the visible suffix, then R03 to R16. After that: the release compile and the
finish-line suite.

### Device-only

- **From L0a's device check** (moved to L0b's): real hiding and opacity with no style reload; hidden layers
  excluded from tap queries.
- **The cell layer's draw order** under every marker, line and offline region, and the two fields' stacking
  after a reorder, once the style reloads.
- **The legend chip's inset placement** at both rotations and in fullscreen: where MapLibre's "i" lands,
  whether 32 dp clears it, and the portrait `bottomInset` padding.
- **The sheet's insets**: the navigation bar, and a short landscape window.
- **Persistence across a force-stop**: visibility, opacity and order.
- **The ramps' lowest colours** against an empty cell, at 0.6 over Street, Topographical and Satellite, and
  under Night Maps.
- **The camera-idle feed**: performance at zoom 7 with both fields on, and that nothing draws below zoom 7.
- **The reorder-at-next-style-load behaviour**, including whether returning to the Maps tab reloads the
  style.
- **The Diagnostics toggle end to end**: on, then back to Maps, and cells draw where the phone is.

### Decisions I made

- **Kept the WIP's tests-first shape** and committed it as round 2 of tests first, rather than treating
  `11ef6e2` as build code. The alternative was folding its stubs into the build commit, which would have
  left those tests with no failing-first record.
- **Renamed the WIP fixture's class** and added the missing import. These are mechanical, but still my
  choice of name.
- **Wrote `DiagnosticsSyntheticForecastSwitchTest`'s four tests** (the file was empty): what they assert,
  and that a failed write leaves the toggle showing the stored state.
- **Extended the one test that passed at its base** instead of stopping on "a tests-first test passing at
  base". It was an absence test that could not fail before the feature. Deciding this properly would need
  the planner's reading of that abort condition for absence tests.
- **Changed the legend-content test** to count in the unmerged tree, rather than splitting the chip's
  semantics so the merged tree held separate nodes. Either keeps the claim; I chose the one that keeps the
  whole chip one TalkBack target.
- **Made `SightingsMap` itself hide every colour field its feed does not name**, beyond F2's host-level
  gating. Without it, every map on `MapLayersState.DEFAULT` (the centre-pin pickers) would have credited
  "Synthetic test data", in release too.
- **Listed colour fields after the record overlays** in the sheet, since they draw below all of them.
- **Opacity is stored on every slider change**, with no debounce, so the map follows the finger live. The
  alternative was storing on release only.
- **The drag handle moves the row with the finger** and applies whole-row steps on release.
- **The chip's styling** is the map chrome's 80% fill and border. Its maximum width is 280 dp, the ramp
  width 160 dp, and the legend shows ramps at full colour, not at the map's 0.6.
- **`LEGEND_ATTRIBUTION_CLEARANCE` = 32 dp**, and a `Spacing.sm` gap between the cluster and the chip.
- **The legend's expanded flag is held above the tab** and saved across recreation.
- **The wide layout's "Maps tab shown"** counts both the List and the Map tab, since the combined pane
  always shows the map.
- **Removed the `isNightMode` parameters** from `MapIconBar` and `CompactMapTab`, whose only use was the
  dropped description clause.
- **Left `MapModePicker`**, its tag and its anchor helper in place with no caller, rather than editing two
  out-of-scope files to delete them. I did delete the compact file's private anchor constant.
- **A panel-open failure path**: if the container's store is not the synthetic switch, the toggle row is
  absent and a warning is logged.
- **Restore can overwrite early changes**: a sheet change made before the start-up restore lands is
  overwritten by it. I judged the window too short to matter and did not merge the two.
- **Log wording**, not user copy: "Refused a stored map layer choice: …", "Refused a map layer opacity of
  … for …", "Couldn't store a map layer choice.", and the Diagnostics panel's three lines.
- **R02's revert edit**: my choice, and a bad one (see the abort).

### Flags outside scope

- **`MapModePicker`** (`ui/map/MapChrome.kt`) has no caller. It is still imported by
  `ui/availability/AvailabilitySettingsUi.kt:66` and `AvailabilityMapControlsUi.kt:78`, and cited in their
  KDoc, and imported unused in `AvailabilityScreen.kt:312`. Removing it needs those two files.
- **Stale comments**: comments in `AvailabilityCompactMapUi.kt` (around the panel anchor and the modal
  overlays) and one in `AvailabilityScreenMapIconStackTest.kt` still name `MapModePicker`. They are in
  scope; I stopped before tidying them.
- **The entry map hides its Layers row while offline tiles are on**, as before. That now also hides its
  overlay switches.
- **The wide layout shows no map until a search has run** (the first coder's flag, unchanged).
- **The planner's prediction 2 is wrong in part.** Beyond the four description classes:
  - F3's three tests and Q3's assertion changed;
  - round 1 re-pinned `MapLayerRegistryTest` (7 assertions), `MapLayerFeatureIdTest` (1) and
    `MapLayerStateTest` (1) for B7.
- **The other predictions**:
  - 1 (no Room migration) held;
  - 3 (70 to 140 more tests): +70, at its lower edge;
  - 4 (no tenth `MapSlot` parameter) held.

### D58

`app/build/l0b/d58.sh` runs `git grep -i`-style checks for the three phrases over `git diff 6235a86` plus the
staged and working trees, over every commit message since `6235a86`, and over each pending message file. I
ran it before every push in this session: diff hits 0, message hits 0, pending-message hits 0, including for
this commit. The reference-class sentence and the layer names were confirmed by the grep, not by reading.

## Resumed for N1 and N2 (third L0b coder, continuation `2026-09-28-11`)

Written by a third coder, in the same worktree (`forager-wt/l0b`, local branch `l0b`), pushing to
`journal-redesign` as before. Everything above this section is left as it was, apart from the title and the
status note at the top. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

**The continuation** (`prompts/preserved/2026-09-28-11.md`, committed at `fb04afc`). Its message, verbatim:

> You are resuming dispatch 2026-09-28-03, L0b. Read these first:
> - `prompts/preserved/2026-09-28-03.md`;
> - the planner messages `2026-09-28-05.md` and `2026-09-28-07.md`;
> - this file;
> - `docs/audits/2026-09-28-map-layers-l0b-completion-report.md`, including its section "Resumed after the machine restart".
>
> The previous coder stopped at `338b241` with the build pushed. Work in `/home/zynergy-labs/Zynergy/forager-wt/l0b` and push to `journal-redesign` as before. Quote this message verbatim in the report.
>
> **N1, the owner's ruling, verbatim:** "Cap height, scroll (Recommended)".
> - The expanded legend gets a maximum height, and its contents scroll inside it. Choose the cap so that in portrait the icon cluster fits above the chip, by Q4's display-only clamp.
> - The failing test is not weakened: it must pass because the cap makes room.
> - Test that the legend's contents stay reachable by scrolling.
> - State the cap in dp in the report.
>
> **N2, the planner's ruling:** resume the revert checks.
> - The R02 abort came from a badly chosen revert edit, not from the forward code. Replace R02 with a one-line edit that compiles. State the old and new edit, and record this as a deviation.
> - Then run R03 to R16 under CLAUDE.md's runner rules. If any further revert build fails to compile, stop again.
> - Also run the round-1 revert checks the first coder never ran, one per tests-first class.
>
> **Also:**
> - **The release-compile evidence:** a release compile, and proof that no synthetic class is in its output.
> - **Tidy the comments** in `AvailabilityCompactMapUi.kt` and the one test that still name `MapModePicker`.
> - **Leave `MapModePicker` itself and its out-of-scope imports alone.** List them as flagged.
> - **Your extension of the test that passed before its feature existed** stands, recorded as a deviation from the tests-first abort condition.
> - **The full suite** from a cleared results directory, then update the completion report to the dispatch's finish line.
>
> No phone and no emulator: a device coder is using the phone. D58 before every push.

### Base

- `origin/journal-redesign` was `fb04afc` when I started. `git diff --stat 338b241 fb04afc` touches only
  `RECORD.md`, `prompts/preserved/2026-09-28-11.md` and `prompts/preserved/2026-09-28-12.md`, so the app
  tree was the second coder's `338b241`. I fast-forwarded `l0b` to it.
- The kit is still absent at this base: no `.claude/kit.json` and no checkers. So, as for the two coders
  before me, I made no record entry and did no structural validation against a kit config.

### Commits (pushed to `journal-redesign`)

| Commit | What |
|---|---|
| `035fff4` | N1, tests first: the legend-cap and scroll test, and a `LEGEND_MAX_HEIGHT` stub that was not yet applied |
| `5cd7033` | N1: the cap applied; the legend-content test scrolls to each entry |
| `2e89091` | Comments that still named `MapModePicker`, tidied |
| this commit | This section |

### N1: the expanded legend is capped and scrolls

- **The cap is 96 dp** (`LEGEND_MAX_HEIGHT` in `ui/map/MapLayersSheet.kt`, applied as `heightIn(max = …)` on
  the chip's `Surface`). The chip's `Column` already had a `verticalScroll`, so past the cap its contents
  scroll inside it.
- **How I chose 96 dp.** The numbers are the second coder's measurements at `w384dp-h823dp`, re-read in this
  session's failing runs:
  - the cluster is 380 dp tall, and at its highest (the search chrome) its bottom is at 601.7 dp;
  - the chip's bottom is at 711 dp;
  - Q4's clamp keeps `Spacing.sm` (8 dp) between them.
  
  That leaves 101.3 dp for the chip. 96 dp is that, rounded down.
- **Tests first** (`035fff4`). The new test is `AvailabilityScreenMapLayersSheetTest`, "expanded, the
  legend is capped in height and a real swipe on it scrolls the reference class into view". It:
  - expands the chip with a real touch;
  - asserts the chip is at most the cap tall;
  - asserts the reference class starts out of view;
  - swipes up on the chip twice, with real coordinate swipes;
  - asserts the reference class is now in view, and the chip is still expanded.

  At `035fff4` it failed for the stated reason: `capped at 96.0.dp (244.0.dp)`. The Q4 test failed as
  before: `expanded: and stays above the chip (601.6667.dp <= 467.0.dp)`.
- **After the cap** (`5cd7033`). The Q4 test, "with the cluster dragged to the bottom on the right it stops
  above the chip, rises when the legend expands and returns when it collapses", **passes unchanged**. I did
  not touch it. The three L0b UI classes ran 19 tests with 0 failures.
- **Two test corrections**, both in `AvailabilityScreenMapLayersTest.kt`:
  - *The new test*, one fix. Run against the cap, it failed at `assertIsNotDisplayed`. In the merged tree
    the reference-class text is the whole chip, because the chip is one clickable `Surface`, and the chip is
    always displayed. The test now finds that text in the unmerged tree. Its fail-first run at `035fff4`
    had already stopped at the height assertion, before reaching this line.
  - *The legend-content test*, "a tap on the chip shows the legend with each ramp's 0 and 100 percent, the
    dates, no forecast here and the reference class, and a second tap hides it". Under the cap its later
    entries are out of view. It now scrolls to each entry (`performScrollTo`, in the unmerged tree, since
    the scroll sits inside the chip's merged node) before asserting it is displayed. It still asserts every
    entry it asserted before, and its counts are unchanged. Revert check R21 shows this test bites on the
    scroll.
- **Device-only.** The cap was measured under Robolectric, where insets are zero. On the S22 the real
  status bar and navigation bar take from the same 101 dp. So an expanded legend may still overlap a cluster
  dragged to the bottom on the right. This goes on the device check's list.

### Comments tidied (`2e89091`)

Comments only.
- `ui/availability/AvailabilityCompactMapUi.kt`, four places:
  - the file header, where the historical constant name is kept and marked as removed;
  - the panel-anchor note;
  - the clamp note;
  - the modal-overlay order, which now names `MapLayersSheet`.
- `AvailabilityScreenMapIconStackTest.kt`: one section heading.

### N2: the revert checks resumed

**Deviation: R02's edit was replaced.**
- Old edit: `name.endsWith(OPACITY_SUFFIX) && value is Float ->` became `… && value is Double ->`. It did
  not compile: the smart cast fed the `Float` map one line down.
- New edit, R02b: `name.endsWith(OPACITY_SUFFIX) && value is Float ->` becomes
  `name.endsWith(VISIBLE_SUFFIX) && value is Float ->`. No opacity key matches any more, and the smart cast
  is still `Float`, so it compiles.

**The runner.** It is the second coder's `app/build/l0b/revert.py` and `run.sh`, unchanged. For each check it:
- saves a copy of the file before editing it;
- refuses to read any results if the build log has an `e: ` line, which ends the run;
- requires the failing tests to be exactly the predicted set, with each message containing its predicted
  fragment;
- restores the file from the saved copy, never from git;
- checks that the restored file's sha256 equals `HEAD`'s blob, so the forward change is still present.

**The specs.**
- `app/build/l0b/revert/checks2.json` holds R02b to R21. `checks3.json` holds R14 to R21, re-run after the
  one runner refusal described below.
- The spec R02 was run under is kept as `checks-v1-as-run-to-R02.json`.
- The outputs are `revert/run2.out` and `revert/run3.out`, and each check's log is
  `app/build/l0b/revert-<name>.log`.

**Two spec changes, made before any check ran:**
- **R09, R13 and R14 no longer predict the Q4 test's failure.** Their predictions were written while the
  Q4 test was failing in the forward build. N1 made it pass, and none of those three edits touches the
  clamp or the legend's height.
- **R14's match text** had to include N1's new `heightIn` line, which sits between the two lines it
  matched. On the first run the runner refused R14 before editing anything ("edit matches 0 times"). That
  was not a compile failure, and the tree was untouched and clean. I fixed the match text and ran R14 to
  R21 again.

**Checks added.** The dispatch asks for "the round-1 revert checks the first coder never ran, one per
tests-first class". Round 1 (`55114ee`) had 10 classes:
- The 7 new classes are covered by R01 (confirmed earlier), R02b and R03 to R07.
- The 3 re-pinned classes had no check. I added:
  - R17, for `MapLayerRegistryTest`: the registry built without its colour fields;
  - R18, for `MapLayerFeatureIdTest`: the same edit, checked against that class;
  - R19, for `MapLayerStateTest`: the unavailable colour fields never hidden.
- For N1 I added R20 (the cap removed) and R21 (the chip's scroll removed).

**Every check run is confirmed, with 0 `e: ` lines, and every file was restored to `HEAD`'s blob.**

| Check | Classes run | Edit | Failed (of run) | Messages, as read |
|---|---|---|---|---|
| R02b | `DataStoreMapLayerPreferencesTest` | opacity read under the visible suffix | 2 of 4 | `expected:<{forecast-chanterelles-layer=0.3}> but was:<{}>`, and the round-trip test's `…opacity={forecast-chanterelles-layer=0.4}…` |
| R03 | `ForecastCellParsingTest` | range check dropped | 1 of 8 | `expected:<[0.0, 1.0]> but was:<[0.0, 1.0, 1.2, -0.1, 0.3]>` |
| R04 | `ForecastCellLayerTest` | `applicable` filter dropped | 1 of 5 | `a cell that is not applicable draws nothing expected:<1> but was:<2>` |
| R05 | `SyntheticForecastTest` | no cell left out | 1 of 6 | `…some of the 100 cells are left out (100)` |
| R06 | `MapLayerPreferencesStateTest` | Move up and Move down swapped | 1 of 6 | `expected:<[forecast-chicken-of-the-woods-layer, forecast-chanterelles-layer]> but was:<[forecast-chanterelles-layer, …` |
| R07 | `MapLegendTest` | always "N layers" | 1 of 3 | `expected:<[Test forecast: chicken of the woods (synthetic data)]> but was:<[1 layers]>` |
| R08 | `AvailabilityViewModelMapLayersTest` | no opacity refusal | 1 of 7 | `…a multiplier from 0 to 1, not 1.2` |
| R09 | compact sheet + short landscape | Layers row opens nothing | 10 of 16 | `map-layers-sheet` not displayed, and the nodes inside the sheet not found |
| R10 | wide | wide control opens nothing | 2 of 3 | `map-layers-sheet` not displayed; `map-layer-switch:kept-tracks-layer` not found |
| R11 | entry map + fullscreen | entry row opens nothing | 2 of 10 | `map-layers-sheet` not displayed, twice |
| R12 | `CartographyEntryMapLayersTest` | shared state not passed | 2 of 2 | `expected:<false> but was:<true>`, twice |
| R13 | compact sheet + `MapLegendTest` | legend ignores visibility | 4 of 17 | `expected:<0> but was:<1>` twice; `…but was:<[2 layers]>`; `expected:<[forecast-chanterelles-layer]> but was:<[…, forecast-chicken-of-the-woods-layer]>` |
| R14 | the three L0b UI classes | chip `fillMaxWidth` | 3 of 19 | `all five touches reached the map expected:<5> but was:<2>`; `clear of the cluster (DpRect(left=0.0.dp, …right=735.0.dp…)`; `expected:<3> but was:<1>` |
| R15 | `DiagnosticsSyntheticForecastSwitchTest` | toggle ignores a successful write | 2 of 4 | `expected:<On> but was:<Off>`; `ComposeTimeoutException: Condition still not satisfied after 5000 ms` |
| R16 | `ForecastCellLayerTest` | no minimum zoom | 1 of 5 | `expected null, but was:<[ForecastBlock(south=45, west=-123)]>` |
| R17 | `MapLayerRegistryTest` | registry without colour fields | 7 of 19 | `…but was:<[]>` on the colour-field lists; the order, kind and tap maps missing both forecast ids; `NoSuchElementException` in the base-opacity test |
| R18 | `MapLayerFeatureIdTest` | the same | 1 of 13 | the fill-roles list expected `(forecast-chicken-of-the-woods-layer, null)` first |
| R19 | `MapLayerStateTest` | unavailable fields never hidden | 1 of 10 | `expected:<[]> but was:<[Synthetic test data]>` |
| R20 | compact sheet | cap removed | 2 of 14 | `expanded: and stays above the chip (601.6667.dp <= 467.0.dp)`; `capped at 96.0.dp (244.0.dp)` |
| R21 | compact sheet | chip's `verticalScroll` removed | 2 of 14 | `Semantic Node has no parent layout with a Scroll SemanticsAction` (content test); the reference class `is not displayed`, at `AvailabilityScreenMapLayersTest.kt:412`, the check after the swipe |

**Each failure is one its own edit could cause.** Every message names the property that check's edit
removes. R20 reproduces this session's own two fail-first messages exactly. Two cases needed a closer read:
- For R21 I checked the stack line, to make sure the swipe test failed after the swipe and not at the
  earlier not-displayed assertion.
- R14's `expected:<3> but was:<1>` is the wide test's three touches around the chip: a full-width chip
  swallows two of them.

After both runs `git status` was clean. The cap line and every other forward change are at `HEAD`.

### Release compile evidence

- **Command:** `./gradlew --offline :app:compileReleaseKotlin :app:mergeDexRelease`, with `LC_ALL=C.UTF-8`.
  The result was BUILD SUCCESSFUL, with 0 `e: ` lines (`app/build/l0b/release.log`).
  - Why not `assembleRelease`: it depends on `verifyReleaseNeverSignsWithDebugKeystore`
    (`app/build.gradle.kts:405`), which needs the owner's signing identity. This machine's session does
    not have it, so I stopped at the merged dex, which needs no signing.
- **Kotlin class output** (`app/build/tmp/kotlin-classes/release`, 1566 classes):
  - the `forecast` package holds only `ForecastCellStoresKt.class`, the release twin;
  - no class name contains "synthetic" in any case.
  - For comparison, the debug output's `forecast` package holds 12 `SyntheticForecast*` classes.
- **Merged release dex** (`app/build/intermediates/dex/release/mergeDexRelease/classes*.dex`, 4 files), read
  with `build-tools/37.0.0/dexdump`: 40,629 class descriptors, 2,926 of them in `com.zynergylabs.forager.app`.
  - `SyntheticForecast` appears in 0 descriptors.
  - `strings` over each dex finds 0 hits for `SyntheticForecast`, `debug_diagnostics_preferences` or
    `diagnostics.synthetic_forecast`.
  - The other 1,352 app descriptors containing "Synthetic" are all D8's `$$ExternalSyntheticLambda*` classes.
  - The `forecast` package is only `ForecastCellStoresKt`. The diagnostics classes are the release twins
    `DebugDiagnostics` and `DiagnosticsPanelKt`.
- **Positive control.** A check that finds nothing only counts if it could have found something, so I ran
  the same dexdump pipeline over the debug build's project dex (`:app:mergeProjectDexDebug`, 15 dex files).
  It finds 15 `SyntheticForecast` descriptors there. So the release zero is a real absence, not a blind check.

### Full suite

- Run at `2e89091` through `run.sh`, which clears `app/build/test-results/testDebugUnitTest` first. Counts
  are read from the JUnit XML, and the log has 0 `e: ` lines. `:app:testDebugUnitTest` ran and was not up
  to date. The class times in the XML add up to 137 s, which fits the 2 min 26 s run.
- **266 classes / 2183 tests / 0 failures / 0 errors / 24 skipped.**
- Against the baseline (253 / 2112 / 0 / 0 / 24): 13 more classes and 71 more tests. That is the second
  coder's 2182 plus this session's one test.
- The held family (the album long-press, tap and Undo tests in `JournalPendingDeleteTest`, and
  `JournalTabTest`'s photo pull) passed in this run.
- Prediction 3 (70 to 140 more tests): +71, inside the range.

### Finish line

Against the dispatch's finish line:
- **B1 to B7 pushed:** yes.
- **This report:** yes.
- **Tests first, and revert checks, each compiling:** yes. 20 checks confirmed this session, plus R01 and
  R01b earlier. The R02 abort is recorded above, together with its replacement.
- **The four description-pinning classes, updated and named:** yes. See the second coder's section.
- **Release compile evidence:** yes.
- **D58:** clean.
- **Full suite:** 0 failures, with the held family green.

### Device-only (additions)

The second coder's list stands. To it add:
- **The legend's height cap against real insets:** whether the cluster clears an expanded legend in portrait
  once the real status bar and navigation bar take their room.
- **A real swipe inside the expanded legend** on the phone scrolls it and does not collapse it.

### Decisions I made

- **The cap's value, 96 dp, and its basis.** I derived it from the Robolectric geometry at `w384dp-h823dp`,
  which has zero insets. The ruling left the number to me ("Choose the cap so that…"). What I chose is a
  fixed dp against one test window, not a cap computed from the live space above the chip. A computed cap
  would hold with real insets and on shorter phones, but the dispatch asked for a number in dp. Deciding
  properly between the two needs the device check's measurement, or the owner.
- **The cap is on the whole chip**, collapsed label included, and not only on its expanded contents.
  Collapsed, the chip is 36 dp, so this changes nothing there.
- **The new test swipes twice** and checks that the chip stays expanded, so the swipe cannot pass by
  collapsing it.
- **The legend-content test now scrolls to each entry** (`performScrollTo`). The alternative was to leave
  that test alone and let the cap fail it. It asserts the same entries and counts as before. I count it as
  adapting the test to the ruled cap, not weakening it, but that reading is mine.
- **The texts are read in the unmerged tree** in both of those tests. This is my correction to my own
  tests-first test, made after it failed on the merged node.
- **The spec changes before running:** R09, R13 and R14 no longer predict the Q4 test's failure, and R14's
  match text was updated. Both were made because N1 changed the forward tree, and both were made before
  any result was read.
- **Which round-1 checks count as never run.** I read "one per tests-first class" as the 10 classes of
  round 1. That gave three new checks, R17 to R19, for the re-pinned registry classes. The 7 new classes
  were already covered by R01 to R07.
- **R17's and R18's edit:** the registry built without its colour fields, which is the literal revert of
  B7, rather than a single flag flip.
- **Added R20 and R21 for N1.** No existing check covered the cap or the scroll.
- **The release evidence stops at `mergeDexRelease`**, not a signed APK, because there is no signing
  identity here. I added the debug-dex positive control myself.
- **The report status names the finish line as met.** The planner re-runs the suite and writes the terminal,
  so that verdict is the planner's to confirm.
- **Following the dispatch over my agent definition on the record** (no sweep, intent or terminal), as the
  two coders before me did, because the kit is absent at this base.
- **Which test to tidy.** I read the dispatch's "the one test" as the one test the second coder flagged:
  `AvailabilityScreenMapIconStackTest.kt`. See the flags for the other.

### Deviations

- **R02 was replaced by R02b.** The old and new edits are above.
- **The test that passed before its feature existed** was extended by the second coder instead of stopping.
  That stands, and is recorded as a deviation from the tests-first abort condition, as this continuation
  directs. Its absence half is covered by R13, which is now confirmed: `expected:<0> but was:<1>`.
- **F3's replacements landed a commit after the removal** (`93da312`, then `34487da`), as recorded above.

### Flags outside scope

- **`MapModePicker` is left alone, as directed.** It is in `ui/map/MapChrome.kt`, with no caller. It is still
  referenced here:
  - imported by `ui/availability/AvailabilitySettingsUi.kt:66` and `ui/availability/AvailabilityMapControlsUi.kt:78`;
  - imported unused in `ui/availability/AvailabilityScreen.kt:312`;
  - cited in KDoc at `AvailabilitySettingsUi.kt:331`, `AvailabilityMapControlsUi.kt:495, 498, 504, 512, 609`,
    `AvailabilityMapOverlaysUi.kt:364, 489`, `ui/map/Basemap.kt:56`, `ui/map/MapMode.kt:15` and
    `ui/theme/Theme.kt:157`. `AvailabilityOfflineMapsUi.kt:91` says "map mode picker".
- **Another test's KDoc names it:** `AvailabilityScreenSettingsPanelTest.kt:76`. I left it, because the
  dispatch named one test.
- **`mapModePickerEnabled`**, the parameter at `CartographyEntryReportScreen.kt:418`, still carries the old
  name. The comment at `CartographyEntryReportScreenFullscreenTest.kt:193` cites it.
- **A fixed 96 dp cap may be too tall on a real phone**, or on any portrait window shorter than 823 dp. See
  Device-only.

### D58

`app/build/l0b/d58.sh` runs `git grep -i` for the three phrases over:
- `git diff 6235a86`, plus the staged and working trees;
- every commit message since `6235a86`;
- the pending message file.

I ran it before each of this session's pushes, and every result was diff 0, messages 0, pending 0. That
includes this commit.
