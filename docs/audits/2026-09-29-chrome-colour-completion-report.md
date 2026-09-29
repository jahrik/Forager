# C1 completion report: all map chrome takes the navigation bar's colour (dispatch 2026-09-28-210)

**Coder session.** Configured model `claude-sonnet-5-5` (the session's setting; the serving model was not read back from inside the session).
**Branch:** `chrome-colour`, worktree `/home/zynergy-labs/Zynergy/forager-wt/chrome-colour`, cut from `origin/journal-redesign` at `47abc89d` ("C1 base 5eedd921"); `5eedd921` is an ancestor of it (`git merge-base --is-ancestor`).
**Governing files, read in full:** `prompts/preserved/2026-09-29-45.md`; `docs/plans/journal-redesign.md:1228-1245` ("Map chrome: one colour, the navigation bar's"); `docs/audits/2026-09-29-map-chrome-colour-pulse.md`. `CLAUDE.md` at this base: read; its "Nothing fully obstructs the map view" bullet (every surface over a map at `MAP_CHROME_OVER_MAP_ALPHA` 0.8, `MapChromeAlphaTest`) is consistent with the dispatch ("every alpha stays"); no conflict found.

**The owner's words, verbatim** (from the dispatch): "The map chrome isn't aligned. The search panel and map icon bar are the wrong color. Have them be the same color as the bottom app navigation bar. Make sure any other pop up or bubble, or the tool panel, is the same color as the app navigation bar also please". Then: "Option B / That's not a problem. My problem is exactly how I stated: the wrong color. / Opacity for the icon bar is fine as is."

## Pre-registration (written and pushed before any code)

### Premises checked at the base (paths under `app/src/main/java/com/zynergylabs/forager/app/ui/`)
| Premise (pulse at `a500e000`) | Read at `47abc89d` | Result |
|---|---|---|
| The nav bar's colour is `colorScheme.surfaceContainer`; dark `#202020`, light `#F4EFE2` | `availability/AvailabilityNavigationUi.kt:147-152` (bar), `:209-213` (rail); `theme/Color.kt:135,141` (`SurfaceContainerDark`, `SurfaceContainerLight`), `theme/Theme.kt:49,104` | holds |
| On the Maps tab the bar and rail are at 0.8 | `availability/AvailabilityCompactMapUi.kt:904,941` | holds |
| Bark `#3B2E24` / Cream `#EDE3D0` hue, `MapIconStackButtonColorDark/Light` at 0.8 | `map/MapChrome.kt:247,250`; `theme/Color.kt:8-9` | holds |
| Duplicate `CompassStripBackgroundColorDark/Light` | `availability/AvailabilityMapControlsUi.kt:95,98`; read at `AvailabilityMapControlsUi.kt:389`, `AvailabilitySearchUi.kt:414`, `NavigationHud.kt:204` | holds (three readers) |
| Cluster container 0.6 and children 0.5 on Bark/Cream | `map/MapChrome.kt:302,305,313-326` | holds |
| Search bar, panel, strip, HUD, chips, legend, bubbles, handles, AddActionTile, entry-map bar on the Bark family | `AvailabilitySearchUi.kt:276,414`; `AvailabilityMapControlsUi.kt:389,640`; `NavigationHud.kt:204`; `AvailabilityMapOverlaysUi.kt:344`; `map/JournalEntriesChip.kt:115`; `map/MapLayersSheet.kt:470`; `map/MapBubble.kt:169,207`; `map/MapChrome.kt:184,470,642,745,838-844` | holds. **Also on the family and not in the pulse's list: the map-mode picker (`map/MapChrome.kt:184`, tag `map-mode-picker`).** |
| Tools drawer, Layers sheet, details sheet at surfaceContainerLow | `availability/AvailabilityScreen.kt:1933-1937` (`DrawerDefaults.modalContainerColor`), `map/MapLayersSheet.kt:238-247`, `log/RecordDetailsSheet.kt:188-194` (`BottomSheetDefaults.ContainerColor`) | holds |
| Dialogs at surfaceContainerHigh; waypoint dialog and centre-pin row at surface; snackbar at inverseSurface | `AvailabilityNavigationUi.kt:279-291`, `AvailabilityWideLayoutUi.kt:684-713`, `log/CartographyEntryReportScreen.kt:674-689` (`AlertDialogDefaults.containerColor`); `AvailabilityMapOverlaysUi.kt:144-151` (date), `:216-224` (waypoint, `colorScheme.surface`); `map/CentrePinLocationPicker.kt:302-316`; `AvailabilityCompactScaffold.kt:646-678` (`SnackbarDefaults.color`) | holds |
| "Download this area?" is opaque over a map | `availability/AvailabilityOfflineMapsUi.kt:254-268` (no container passed); `pickerMap` is composed in both layouts (`:291`, `:325`) | holds: a map is always drawn beneath it, so `overMap = true` |
| The search notice is errorContainer | `AvailabilitySearchUi.kt:625-633` | holds, unchanged |
| Nothing pins the nav bar's colour | grep of `app/src/test` for the nav bar tags with a colour | holds |
| Tests that pin the old roles | `MapChromeOverMapTest` (roles captured `:47-77`; `assertOverMap`/`assertSolid` `:213-237`), `MapLayersSheetTest.kt:45-76`, `LandscapeLClusterPixelsTest.kt:18-19,91`, `JournalEntriesChipTest.kt:26,128-143`, `MapChromeAlphaTest.kt:18-27` | holds; `MapChromeAlphaTest.kt:26-27` reads `MapIconStackButtonColorDark/Light.alpha`, so **those constants are kept** (redefined, not removed) so no alpha assertion is edited |

### The design this build will follow (from the dispatch's text)
- **The token:** `navigationBarContainerColor()` (`ui/theme`), which returns `MaterialTheme.colorScheme.surfaceContainer`. The bar and the rail take it as their default container; every dialog, sheet, drawer, menu, snackbar and the centre-pin row takes it through `mapChromeFill(token, overMap)`.
- **The Bark-family constants** (`MapIconStackButtonColorDark/Light`, the cluster's container and child) are non-composable values read at about twenty sites. They keep their names and their alpha and are redefined from `SurfaceContainerDark`/`SurfaceContainerLight`, the two values `Theme.kt` gives `surfaceContainer`; a test asserts they equal the scheme's `surfaceContainer` in both themes. `CompassStripBackgroundColor*` is deleted, its three readers taking `MapIconStackButtonColor*`.
- **Test seam:** the Bark-family surfaces expose their container through the existing `MapChromeContainerColor` semantics (as the sheets, dialogs and the journal chip already do). No alpha, content colour, border or shadow changes except where contrast fails.

### Predictions and pass conditions
At the stubs (the token function, the semantics seams on the Bark-family surfaces, the test screen's `darkTheme` parameter; **no colour changed**), each test below is expected to fail as stated; each passes when its surface's container is the token at its own unchanged alpha.

| Test (new class unless named) | Predicted failure at the stubs | Passes when |
|---|---|---|
| `MapChromeColourTokenTest`: constants and cluster layers equal `surfaceContainer` in dark and light; the token literals are `#202020` and `#F4EFE2`; the bar and rail expose their container | `expected:<Color(0.94.., ...)> but was:<Bark/Cream>` for every constant and layer; the bar and rail cases pass at the stub (already the token), so they are guards, held by a revert check | each container is the token, alphas 0.8, 0.6 and 0.5 |
| `MapChromeColourTest` through `AvailabilityScreen`, dark and light: search bar, search panel, compass strip, HUD, taxon chip, journal chip, legend, cluster, bar, pill, handles, bubble and its tail, AddActionTile, map-mode picker, entry map's bar, landscape L, tablet | container colour aside from alpha differs (Bark/Cream, not the token) | container is the token at 0.8 (cluster container 0.6, children 0.5) |
| `MapChromeOverMapTest` (existing, edited to the token; each edit quotes the owner) | role mismatches: sheet, drawer, dialogs, waypoint dialog, centre-pin row, snackbar are today at other roles; the search notice stays errorContainer and passes | as above, and "Download this area?" at 0.8 |
| `MapLayersSheetTest` (existing, edited) | `expected surfaceContainerLow` no longer holds after the change; at the stub it still passes, so it is edited with its change | sheet is the token at 0.8 |
| `LandscapeLClusterPixelsTest` (existing, edited) | pixels are Bark/Cream today | pixels are the token at 0.8 |
| `JournalEntriesChipTest` (existing) | passes at the stub and after, because it compares to `MapIconStackButtonColor*` | unchanged; a check that passes before and after is flagged in the report |
| `ChromeContrastTest`: every text and icon colour drawn on the token, both themes, 4.5:1 text and 3:1 icons | the snackbar's `inverseOnSurface` on the token fails; the rest pass | every pair passes, the snackbar using the nav bar's own content colour |

**Pre-registered expectation about content colours:** on the token, `White` (dark) and `Bark` (light) chrome content, `onSurface`, `onSurfaceVariant` and `primary` are expected to pass; the snackbar (`inverseOnSurface`, `inversePrimary` action) is expected to fail and to be the only surface whose content colour changes. Stated before measuring; the measured ratios go in "Resumed" below.

## Stopped, and resumed

I stopped once on an instruction relayed as the owner's ("Start F4. Stop C1"), with nothing compiled: the WIP was pushed as `5dac3b74` at the planner's instruction (its message: uncompiled, unrun, C1 stopped). The planner then reported the owner's answers, verbatim: "**4 A**" (resume C1 now) and "**5 No, those are map icons not chrome**", and instructed:
1. continue from `5dac3b74`, pull `journal-redesign` with `--no-rebase` first, run the tests at base to confirm the predicted failures, push them, then implement;
2. "Do NOT tie the icon cluster's 0.6/0.5 layers to MAP_CHROME_OVER_MAP_ALPHA; the owner declined that. The icon bar's COLOUR still changes to the token, per the owner's first request. Only opacity stays as it is.";
3. the wide record-details pane "is `surface` and solid. It takes the token colour and STAYS solid (colour only, alpha unchanged). Record it as the pulse's wrong premise.";
4. findings 2 to 5 as proposed: `MapModePicker` follows the constants; `MapIconStackButtonColor*` kept but redefined from the token; the snackbar takes the nav bar's content colours where contrast fails; the Download dialog is always over its map, at 0.8.

(The question about tying all chrome opacity to one setting, and the owner's "5 No", are answered by point 2: every alpha is as it was; nothing was made to follow `MAP_CHROME_OVER_MAP_ALPHA`.)

## What landed

Branch `chrome-colour`, pushed to `chrome-colour-wip` throughout and to `journal-redesign` at the end. Merged with `journal-redesign` twice, `--no-rebase`, clean both times (`57a4333a`, F4's continuation at `958eea6c` included; `a6d01fc9`, the second).

| Commit | What |
|---|---|
| `b8eb6136` | pre-registration |
| `5dac3b74` | WIP as stopped: seams, stub token, tests, uncompiled |
| `2b0af317` | tests first, run at the stubs (168 tests, 108 failed) and two harness fixes |
| `d67f327d` | the colour change, 168 of 168 green |
| `56371729` | pixel test: search bar, strip, icon bar and navigation bar drawn as one colour |
| `f9895000` | pixel tests: search panel, bubble, wide details pane |
| `a6d01fc9` | merge of `journal-redesign` |

What changed, per dispatch item (paths under `app/src/main/java/com/zynergylabs/forager/app/ui/`):
1. **One token:** `theme/NavigationBarColor.kt`, `navigationBarContainerColor()` = `MaterialTheme.colorScheme.surfaceContainer` (dark `#202020`, light `#F4EFE2`). `ForagerBottomNav` and `ForagerNavigationRail` take it as their default container (`availability/AvailabilityNavigationUi.kt`), and the Maps tab's 0.8 copies of it (`AvailabilityCompactMapUi.kt`). `MapIconStackButtonColorDark/Light` (`map/MapChrome.kt`) are **kept under their names and their 0.8** and redefined from `SurfaceContainerDark`/`SurfaceContainerLight`, the two values `Theme.kt` gives `surfaceContainer`, because about twenty sites read them outside a composable and `MapChromeAlphaTest.kt:26-27` pins their alpha; `MapChromeColourTokenTest` asserts they equal the scheme's in both themes. `CompassStripBackgroundColorDark/Light` deleted; its three readers (strip, search panel, HUD) take `MapIconStackButtonColor*`.
2. **Every surface:** through those constants: the search bar and panel, compass strip, navigation HUD, taxon chip, journal-entries chip, legend, the cluster's bar and pill (portrait, tablet, landscape L), the handles' marks, every bubble and its tail, the AddActionTile, the map-mode picker, the entry map's fullscreen bar. The cluster's container and children are `navigationBarContainerColor()` at their own 0.6 and 0.5 (`mapIconClusterContainerColor`, `mapIconClusterChildColor`). Through `mapChromeFill(navigationBarContainerColor(), overMap)`: the Layers sheet, the record details sheet, the Tools drawer (`AvailabilityScreen.kt`), the exit, three-way, entry-delete, trip-date and waypoint-name dialogs, the centre-pin row, the compact snackbar, and the species, Month and entry-overflow menus and the journal chips' menus (already `surfaceContainer`; routed to the token, no visible change). The wide details pane (`log/JournalDetailSlot.kt`) takes the token and stays solid.
3. **Search notice:** unchanged (errorContainer); `MapChromeOverMapTest`'s notice tests pass unedited, and `MapChromeColourTest` asserts it in both themes.
4. **Alpha:** none changed except "Download this area?", which was opaque and is now 0.8 through `mapChromeFill(..., overMap = true)` (`AvailabilityOfflineMapsUi.kt`). Its picker map is always composed beneath it (`:291` and `:325` at the base), so `overMap` is unconditional. No alpha assertion was edited.
5. **Content:** unchanged except the snackbar, whose default content colour `inverseOnSurface` and action colour `inversePrimary` fail contrast on the token (pinned by `ChromeContrastTest`): its text is now the nav bar's own content colour, `onSurfaceVariant`, its action `primary` and its dismiss `onSurfaceVariant`. Measured contrast on the token (WCAG ratio, from `Color.kt` values): dark, chrome content White 16.29:1, `onSurface` 12.80, `onSurfaceVariant` 9.89, `primary` 9.09, `error` 7.39, handle outline (White at 0.7) 8.58; light, chrome content Bark 11.42, `onSurface` 11.42, `onSurfaceVariant` 6.60, `primary` 7.58, `error` 5.07, handle outline (Bark at 0.7) 4.73. All clear 4.5 for text and 3 for icons. **The pre-registered expectation held:** the snackbar's old pair was the only failure; every other content colour passed unchanged.
6. **Tablet:** same token (it has no navigation bar); `MapChromeColourWide*Test` and the wide pane pixel test.

## Evidence

**Tests first**, at the stubs (seams with the old colours, a stub token), from a cleared results directory, `e:` lines 0: `MapChromeColour*`, `ChromeContrastTest`, `MapChromeOverMapTest` and its subclasses, `MapLayersSheetTest`, `LandscapeLClusterPixelsTest`, `JournalEntriesChipTest`, `MapChromeAlphaTest`: **168 tests, 108 failed**, each `container colour aside from alpha expected:<token> but was:<Bark/Cream or the old role>` (or the alpha: `download-confirm-dialog: container alpha expected:<0.8> but was:<1.0>`), for the reason predicted. Two failures were the harness, not the reason, and were fixed and rerun (30 tests, 24 failed, all colour or alpha): the landscape pill's semantic seam sat on a different node from its tag, and the Download button needed a scroll before its click. Passing at the stubs, so **regression guards, not shown to bite by tests-first**: the navigation bar and rail (already the token), the search notice, `ChromeContrastTest` (pure arithmetic; it also pins the snackbar's old pair failing), `JournalEntriesChipTest` (compares to the constants, which followed), `MapChromeAlphaTest`. The two pixel-test classes were written after the implementation; their evidence is the reverts below.

**Revert checks** (`/tmp/cc_revert.py`: saves the file before editing, restores only from that copy, refuses to cite results if the reverted build has an `e:` line or no `BUILD` line, reads only XML newer than the run's start, and checks the restored file is byte-identical and the forward marker present). 20 runs, 0 compile-error lines, every restore identical with the marker present:

| Revert | Result |
|---|---|
| v1 constants back to Bark | 23 failed (`expected 0.125 ... but was 0.231, 0.180, 0.141`), including the pixel and Landscape L pixel tests |
| v2 cluster container | 8 failed |
| v3 cluster child | 10 failed |
| v4 / v5 nav bar / rail default | 2 / 2 failed |
| v6 Layers sheet | 1 failed |
| v7 details sheet | 7 failed |
| v9 drawer | 2 failed |
| v10 exit dialog | 3 failed |
| v11 Download dialog opaque again | 2 failed: `container alpha expected:<0.8> but was:<1.0>` |
| v12 centre-pin row | 1 failed |
| v13 snackbar content | 2 failed (`content colour expected ... 0.812, 0.788, 0.745`) |
| v14 snackbar container | 3 failed |
| v16 search bar fill (fill only) | 2 failed (pixel test) |
| v17 Month menu role | 2 failed |
| **v8, v15 (details pane fill only, search panel fill only): 0 failed** | **an escape.** The semantic tests read the colour the surface is *given*, which a fill-only revert leaves alone. Closed by pixel tests (`f9895000`), then re-reverted: w8 pane 2 failed (`read 0.106` = `surface`), w15 search panel 2 failed, w18 bubble fill 2 failed |

**Full suite**, results cleared first, every file newer than the run's start (0 stale), `e:` lines 0, on `a6d01fc9` (this branch merged with `origin/journal-redesign`): **394 classes / 3217 tests / 0 failed / 24 skipped.** One run; no flake seen.

## Decisions I made

1. **Constants kept, redefined**, rather than removed (`MapChromeAlphaTest` pins their alpha, so removing them would edit an alpha assertion); the token is the composable `navigationBarContainerColor()` and the constants are built from the same two theme values. There are two expressions of one colour, held equal by `MapChromeColourTokenTest`; if someone changes `Theme.kt`'s `surfaceContainer` to a different value, the constants follow because they use the same vals, but a change of the token function alone would not reach them.
2. **Semantic seams** (`mapChromeContainerColor`) added to the Bark-family surfaces, and two tags (`MAP_ICON_BAR_TAG`, `DOWNLOAD_CONFIRM_DIALOG_TAG`), as the existing sheets and dialogs already do. Production code changed for testability.
3. **Snackbar action colour** `primary` and dismiss `onSurfaceVariant`: my choice of which passing colours to use. The action colour is contrast-checked but not visible to any test.
4. **`MapChromeRoles` in `MapChromeOverMapTest` now equals the token** for sheet, surface, dialog, date picker, snackbar, drawer, menu; snackbar content `onSurfaceVariant`. Each edit quotes the owner. `MapLayersSheetTest` and `LandscapeLClusterPixelsTest` likewise. No alpha assertion, and no test outside the dispatch's list, was edited.
5. **The icon bar's pixel check allows a shadow factor** (0.90 to 1): the bar's own 2 dp shadow, unchanged, darkens a translucent fill by about 3% in light and 7% in dark; the check demands the same hue times one neutral factor, so a Bark or Cream bar fails it.
6. The pixel tests' expected pixel is the screen's own background (the stand-in map draws nothing), as `LandscapeLClusterPixelsTest` does.

## Flags outside scope

- **Wrong premise in the pulse:** the wide record-details pane is `colorScheme.surface`, solid (`log/JournalDetailSlot.kt:100` at the base), not surfaceContainerLow. It now takes the token, per the planner.
- **`MapModePicker` (`map/MapChrome.kt:144`) has no caller.** It follows the constants and is not reachable, so untested.
- **Seams versus pixels.** Only these fills are proven by rendered pixels: the search bar, compass strip, icon bar, navigation bar, search panel, bubble, wide details pane (`MapChromeColourPixelsTest`) and the landscape L's bar and pill (`LandscapeLClusterPixelsTest`). The rest (the HUD, chips, legend, AddActionTile, handles, cluster container, pill, sheets, dialogs, drawer, menus, snackbar, centre-pin row, Download dialog) are proven through the semantic seam, which repeats the colour the surface is given: a change to the fill alone would not fail them (revert v8 and v15 showed this for two surfaces before their pixel tests). A snackbar's action colour is not read by any test.
- **The scrims** (Bark at 0.32 under sheets and the drawer) are unchanged, so sheets and the drawer still read warmer than an unscrimmed navigation bar (the pulse's own note). Device-only.
- **Stale comments** still name `CompassStripBackgroundColorDark/Light` (`AvailabilityMapControlsUi.kt:5`, `AvailabilitySearchUi.kt:11`, `theme/Theme.kt:62`); they are history in file headers and a colour-scheme comment, not edited.
- **Machine rule:** one Gradle attempt started with 2415 MB available (under 2.5 GB); it failed at compile and ran no tests. Every later run went through a script that waits for no Gradle worker and 2600 MB.
- **Never compiled at the pause:** the WIP at `5dac3b74` was uncompiled as pushed; the first compile was at the resume.

## Not tested

- Real screen rendering over real terrain, at night, over each basemap.
- The snackbar's action label colour, and any text colour drawn by a shared composable not enumerated in `ChromeContrastTest`'s list (a record type's accent in a details sheet).
- The seam-only surfaces' fills (above).
- The wide `PermanentDrawerSheet` (the wide Tools drawer): not read for its container; the pulse listed it "could not determine", and it was not changed.
- Compact tablet variants other than `w1280dp` and `w840dp`.

## Device-only list (S22 Ultra and the tablet)

1. Over each basemap (topo, aerial), by day and at night, do the search bar and panel, strip, chips, legend, icon bar, bubbles and the bottom navigation bar read as **one colour**? Compare the search panel and the icon bar with the navigation bar, as in the owner's screenshot (`~/Zynergy/device-evidence/2026-09-29-owner-chrome-colours.jpg`).
2. The cluster's bar and pill against its container (0.5 over 0.6) and the shadow: does the bar still read as one piece in light?
3. The Tools drawer, the Layers sheet and the details sheet over their Bark scrims: do they read warmer than the navigation bar?
4. "Download this area?" over the picker's map at 0.8: readable, and the picker's map showing through.
5. A snackbar with an action: the action's colour and the text's legibility on the token.
6. The wide details pane at the token beside the drawer, on the tablet.
7. Landscape L and the rail on the S22: the same colour at the rail and the L.
