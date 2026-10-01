# The Layers sheet at the map chrome's standing opacity: completion report

Intent `2026-09-28-16`, dispatch `prompts/preserved/2026-09-28-16.md`. Branch `journal-redesign`, base
`955cc58`. The owner asked, over a screenshot of the sheet: "Can this panel be given 80% opacity like the
rest of the map chrome?" The sheet's container now draws at `MAP_CHROME_OVER_MAP_ALPHA` (0.8). The full
suite is 267 classes / 2185 tests / 0 failures / 0 errors / 24 skipped.

## Verification

- **Base.** `origin/journal-redesign` was at `955cc58` after a fetch, the commit carrying the dispatch.
  The worktree `/home/zynergy-labs/Zynergy/forager-wt/sheet-alpha` was cut from it.
- **Premise 1, the constant: holds.** `MAP_CHROME_OVER_MAP_ALPHA = 0.8f` is at `ui/map/MapChrome.kt:239`.
  Its doc comment reads "The standing opacity for chrome floating over the map — the one value every
  fill here targets."
- **Premise 2, the sheet: holds.** At base the sheet is `ModalBottomSheet(` at `ui/map/MapLayersSheet.kt:197`
  with no `containerColor`, so it takes Material3's opaque default.
- **Premise 3, no other colour source: holds.** The three callers pass no colour and wrap the sheet in no
  theme or colour local: `AvailabilityCompactMapUi.kt:1283`, `AvailabilityWideLayoutUi.kt:322` and
  `CartographyEntryReportScreen.kt:436`. The sheet's colour comes only from the app theme's colour scheme
  (`ForagerTheme`, `Theme.kt:142`). The `Surface` inside `MapLayersSheet.kt` (line 410 at base) is the
  legend chip, not the sheet.
- **Material3's defaults.** Material3 is pinned at `1.5.0-alpha26` (`gradle/libs.versions.toml:19`). No
  sources jar is in the Gradle cache, so I read the defaults from the disassembled `classes.jar` (`javap -c`):
  - `ModalBottomSheet`'s `containerColor` defaults to `BottomSheetDefaults.ContainerColor`.
  - `BottomSheetDefaults.ContainerColor` is `ColorSchemeKeyTokens.SurfaceContainerLow`, which is
    `colorScheme.surfaceContainerLow` (`SheetDefaults.kt:522`).
  - `contentColor` defaults to `contentColorFor(containerColor)`.
  - `scrimColor` defaults to `BottomSheetDefaults.ScrimColor`, which is `colorScheme.scrim.copy(alpha = 0.32f)`
    (`SheetDefaults.kt:529`).
  - `contentColorFor` returns a colour only when it matches a scheme role exactly. Otherwise it falls back
    to `LocalContentColor.current`.

## What landed

- **`05d68c3`, tests first.** `MapLayersSheet` now passes its current defaults explicitly:
  `containerColor = BottomSheetDefaults.ContainerColor` and `contentColor = contentColorFor(BottomSheetDefaults.ContainerColor)`.
  It also exposes two semantics properties for tests:
  - `MapLayersSheetContainerColor`, on the sheet's tagged node, holds the colour passed as `containerColor`.
  - `MapLayersSheetContentColor`, on the content column, holds `LocalContentColor.current` as read inside
    the sheet.

  Nothing drawn changes at this commit. It also adds the new `MapLayersSheetTest` (two tests).
- **`8026a7e`, the change.** One production line:
  `val containerColor = BottomSheetDefaults.ContainerColor.copy(alpha = MAP_CHROME_OVER_MAP_ALPHA)`,
  plus comment lines citing the role and the scrim.
- **Scrim: unchanged.** It is Material3's default, `colorScheme.scrim` at alpha 0.32. The app's `scrim` is
  `Bark` in both themes (`Theme.kt:63` and `:113`). So the map behind reads first through a Bark scrim at
  0.32, then through the sheet's `surfaceContainerLow` at 0.8.
- **Drag handle: unchanged.** It is still the default, `BottomSheetDefaults.DragHandle`.
- **Content colour: unchanged.** It is pinned to the default role's own, `onSurface`. Without the pin,
  `contentColorFor` finds no role for a colour at alpha 0.8 and falls back to `LocalContentColor`.
  Revert R2 below shows that fallback is black in the test.

## Tests

`app/src/test/java/com/zynergylabs/forager/app/ui/map/MapLayersSheetTest.kt` composes the real
`MapLayersSheet` in `ForagerTheme(darkTheme = true)`. Dark is the theme of the owner's screenshot. Each
test reads the colour from the composed sheet's semantics and compares it with the theme's own roles, read
in the same composition:

1. `the sheet's container is its default container role at the map chrome's standing opacity` checks two
   things: alpha equals `MAP_CHROME_OVER_MAP_ALPHA`, and the RGB equals `surfaceContainerLow`.
2. `the sheet's content colour stays the default container role's own, opaque` checks that the content
   colour equals `onSurface`.

**Tests first, at `05d68c3`.** The results directory was cleared first and `compileDebugKotlin` ran with
no `e:` lines. Result: 2 tests, 1 failure. Test 1 failed for the stated reason,
`container alpha expected:<0.8> but was:<1.0>`. Test 2 passed. It is a guard for "change no other
colour": it passes before and after the change by design, and revert R2 is the evidence that it bites.

**Forward, at `8026a7e`:** 2/2 pass.

**Revert checks.** The runner script (in the session scratchpad) works like this for each check:
1. Save a copy of `MapLayersSheet.kt`.
2. Make a one-line edit.
3. Clear the results directory and run the class.
4. Restore from the saved copy, not from git.
5. Confirm the restored file matches the saved copy and has no diff against HEAD.
6. Check the build log for compile errors before reading the XML.

- **R1** reverts `.copy(alpha = MAP_CHROME_OVER_MAP_ALPHA)`.
  - Build: `compileDebugKotlin` ran, no compile errors.
  - Result: 2 tests, 1 failure. Test 1 failed with `container alpha expected:<0.8> but was:<1.0>`, a
    failure this edit produces. Test 2 passed.
  - Restore: identical to the saved copy, 0 diff lines against HEAD.
- **R2** deletes `contentColor = contentColor,` from the `ModalBottomSheet` call.
  - Build: `compileDebugKotlin` ran, no compile errors.
  - Result: 2 tests, 1 failure. Test 2 failed with
    `content colour expected:<Color(0.92941177, 0.8901961, 0.8156863, 1.0 ...)> but was:<Color(0.0, 0.0, 0.0, 1.0 ...)>`.
    That is Cream (`onSurface`, dark theme) against the `LocalContentColor` fallback, a failure this edit
    produces. Test 1 passed.
  - Restore: identical, 0 diff lines against HEAD.
  - My first R2 attempt used a target that matched twice (the legend's `Surface` has the same line). The
    runner's uniqueness assert refused it before writing anything, the tree was confirmed clean, and the
    check was rerun with a unique target.

## Suite

- **At `8026a7e`:** cleared `app/build/test-results/testDebugUnitTest`, then ran `./gradlew --offline
  :app:testDebugUnitTest` with `LC_ALL=C.UTF-8`. Result: BUILD SUCCESSFUL, no compile errors.
- **Counts from the JUnit XML:** **267 classes / 2185 tests / 0 failures / 0 errors / 24 skipped.**
- **Against the baseline at `af846cc` (266 / 2183 / 0 / 0 / 24):** one more class and two more tests,
  which is `MapLayersSheetTest`.
- **The held family:** `ui.log.JournalPendingDeleteTest` and `ui.log.JournalTabTest` passed in this run.
- **Predictions (planner):**
  1. One production line changes: true of `8026a7e`. It depends on `05d68c3`, though, which added the
     explicit parameters, the content-colour pin and the semantics seam (31 lines in
     `MapLayersSheet.kt`). The pin exists because of this change.
  2. The suite grows by 1 or 2: it grew by 2.

## Device-only

None of this has been seen on a real screen (no phone or emulator in this dispatch):
- How the translucent sheet reads over real terrain: Street, Topographical, Satellite, and in night mode.
- Whether text and controls stay legible over busy imagery, with the 0.32 Bark scrim beneath the sheet.
- The light theme, which the tests do not compose.
- Whether translucency shows the sheet's own edge or inset regions differently from Robolectric. Robolectric
  reports zero insets, per CLAUDE.md.

## D58

Before each push I ran a case-insensitive grep for the three phrases D58 forbids, over the diff since
`955cc58` and over every commit message. Both pushes had zero hits. I ran it once more over this report's
commit before its push.

## Decisions I made

- **The seam is a semantics property.** The dispatch allowed a semantics property or a test seam. I
  exposed the colour passed as `containerColor` (the dispatch's "colour the composable was passed"), not a
  pixel read. Existing precedent: `LevelLine.kt:97`, `PhotoViewerDialog.kt:408`.
- **The seam and the explicit defaults went in the tests-first commit.** A seam that did not exist at base
  would have failed to compile rather than failing on alpha. So `05d68c3` is a behaviour-neutral
  production change, and `8026a7e` is the one-line change.
- **The content colour is pinned, and a second test guards it.** The dispatch said "Change no other
  colour" but did not name this mechanism. Without the pin, the sheet's text colour would silently have
  become `LocalContentColor.current`. Pinning it was my reading of that instruction, and it adds a
  production line the dispatch did not list.
- **The test is new, not added to a host test.** The dispatch says "its test", and there was no existing
  test file for `MapLayersSheet`. I created `ui/map/MapLayersSheetTest.kt` and composed the sheet
  directly, in the dark theme only.
- **The source of the Material3 defaults.** With no sources jar, I cited the bytecode's line markers
  (`SheetDefaults.kt:522`, `:529`) from `javap`, not the published source.
- **Record keeping.** The dispatch says the kit is gone and the planner writes the record. I wrote no
  sweep, intent or terminal and did not touch `RECORD.md`. I also skipped the structural check against
  `.claude/kit.json`, which does not exist at this base. My standing instructions assume both, so this
  follows the dispatch over them.

## Flags outside scope

- `MapLayersSheet.kt`'s colour comment says the map "reads through both" the scrim and the sheet. That is
  what the code composes; how it looks is device-only.
- The other map sheets and the legend chip are untouched, per scope. The legend chip already uses
  `MapIconStackButtonColorDark` and `MapIconStackButtonColorLight` at 0.8.
