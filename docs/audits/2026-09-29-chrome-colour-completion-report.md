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
