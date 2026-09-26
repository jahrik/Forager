# Split AvailabilityScreen.kt, Stage F (split build 1): completion report

**Date:** 2026-09-26 (UTC). **Dispatch:** `prompts/preserved/2026-09-26-45.md`. **Record:**
intent `2026-09-27-15`, sweep merge entry `2026-09-27-14`. **Branch:** `split-move` from
`origin/pre-main` `3bd0efe05332b07384df36ef025cd0787a50cd60`. **Move commit:** `c511e26`.

A pure move. Declarations were moved verbatim out of
`app/src/main/java/com/zynergylabs/forager/app/ui/availability/AvailabilityScreen.kt` (`AS`) into
seven new files in the same package. The only other changes are the 32 listed `private` →
`internal` widenings, each new file's imports, and a "Split-AvailabilityScreen Stage F" header in
each new file. No test file changed. Seam F (the wide layout) is included because the owner
released it ("Release F"), as recorded in the Understory amendment merged in #130.

## Premises checked at the base

Every premise the dispatch stated held at `3bd0efe`, checked after `git fetch`:

- **Base and checkouts.** `origin/pre-main`, the main checkout (on `pre-main`) and the planner
  worktree were all at `3bd0efe`, and `git diff --stat 9ea36d6 3bd0efe -- app` was empty.
- **The file.** `AS` was 5,674 lines. It had:
  - no `@file:` annotation, no `@Preview` and no `R.*`;
  - no `CompositionLocal` defined (`CompositionLocalProvider` is used at `:4653`);
  - `BuildConfig` at `:2507` (imported at `:198`);
  - `@OptIn(ExperimentalMaterial3Api::class)` at `:450` and `:5021`;
  - no `rememberSaveable`, `key(` or `currentCompositeKeyHash`.

  `git grep AvailabilityScreenKt` found nothing.
- **The record.** #130's merge had no `merge` entry, and its Carries are `-08` to `-13`
  (`git diff e5ecb91 3bd0efe -- RECORD.md`). `2026-09-27-14` was the next free ID.
- **The ranges.** Every range was re-verified at `3bd0efe`:
  - Every line outside the listed ranges is blank (26 lines), and no line falls in two ranges.
  - A bracket-depth scan with comments and strings stripped puts every range's first line and
    the line after its last at top level.
  - So no range splits a declaration, and every top-level declaration is placed.
- **The shared range `:4998-5006`.** It holds two declarations, each with its own KDoc:
  - `ADD_TILE_ANCHOR_OFFSET` at `:4998-5003` (KDoc `:4998-5002`, declaration `:5003`), which went
    to F3;
  - `MAP_MODE_PICKER_COMPACT_ANCHOR_OFFSET` at `:5005-5006` (KDoc `:5005`), which went to F2.

  Line `:5004` is blank.

## What moved

Line ranges are those of `AS` at `3bd0efe` and include KDoc.

| File | Lines | Ranges | Widened (`private` → `internal`) |
|---|---|---|---|
| F1 `AvailabilityNavigationUi.kt` | 281 | `:329-376`, `:2280-2427`, `:4804-4835` | `CompactTab`, `CompactTab.icon`, `ForagerBottomNav`, `ForagerNavigationRail`, `shortLandscapeContentInsets`, `ScreenEdge.horizontalInsetsSide`, `ExitNavigationPrompt` |
| F2 `AvailabilityCompactMapUi.kt` | 1,218 | `:3324-3354`, `:3356-4420`, `:4422-4434`, `:4442-4443`, `:5005-5006` | `MapIconClusterPositionState`, `rememberMapIconClusterPositionState`, `CompactMapTab` |
| F3 `AvailabilityMapControlsUi.kt` | 598 | `:3317-3321`, `:4436-4440`, `:4445-4802`, `:4866-4996`, `:4998-5003` | `TrailheadControls`, `CompassElevationStrip`, `AddActionTile`, `ADD_TILE_ANCHOR_OFFSET` |
| F4 `AvailabilityMapOverlaysUi.kt` | 532 | `:3076-3087`, `:5008-5114`, `:5345-5673` | `PendingMapAction`, `TripDatePickerDialog`, `WaypointNameDialog`, `AnchoredAtScreenPoint`, `ObservationBubble`, `TaxonMapFilterChip`, `MapMessage` |
| F5 `AvailabilityWideLayoutUi.kt` | 418 | `:2429-2435`, `:2437-2491`, `:2958-2988`, `:3089-3315`, `:4837-4864` | `PERMANENT_DRAWER_WIDTH`, `CombinedResultsPane` |
| F6 `AvailabilitySettingsUi.kt` | 618 | `:2493-2956`, `:2990-3074` | `BuildIdentityFooter`, `DrawerHeader`, `MushroomLogEntryRow`, `SettingsEntryRow`, `PhotoGalleryEntryRow`, `SettingsHeader`, `PhotoGalleryHeader`, `SettingsContent`, `CompactToolsDrawerContent` |
| F7 `AvailabilityTripsWaypointsUi.kt` | 275 | `:5116-5343` | none |

`AS` keeps `:1-327` and `:378-2278` and goes from 5,674 to **2,229** lines.

**Forced widenings: none.** Every widening is on the dispatch's lists, and the compiler forced
no other. Before the move, a reference scan of `AS` with comments and strings stripped found
the private declarations used across the new file boundaries. That set is exactly the lists
minus two entries:

- `CompactTab.icon`, whose only callers are the two bars in F1;
- `MapIconClusterPositionState`, which is the return type of the widened remember function.

No private declaration that stays in `AS` (`ResultsTab`, `DrawerPanel`,
`DOUBLE_BACK_EXIT_WINDOW_MS`) is referenced from moved code except in comments.

**Imports.** Each new file carries the imports of `AS` whose simple name its code uses, or its
KDoc links as `[Name]`.

- An import whose only matches in a file are named arguments (`contentDescription = null`) was
  left out.
- The first compile reported one of those as needed: `e:
  ui/availability/AvailabilitySettingsUi.kt:122:26 Unresolved reference 'contentDescription'`
  (a semantics assignment, not a named argument). It was added to F6 only. That is the only
  compile fix, and it is an import, not a widening.
- The second compile was clean.
- The compiler reports no unused import in `AS`, and no ktlint, detekt or spotless is configured.
  So all 319 of its imports stay, as the dispatch directs.

**Compiler warnings.** In the package's main sources, before and after, the only warnings are
the same two `Icons.Filled.MenuBook` deprecations. Before the move they were at `AS:370` and
`AS:2568`; after it they are at `AvailabilityNavigationUi.kt:93` and
`AvailabilitySettingsUi.kt:144`. `AvailabilitySearchUi.kt`'s two `contentPadding` deprecations
are unchanged.

**`@OptIn`.** Both declaration-level `@OptIn(ExperimentalMaterial3Api::class)` annotations moved
with their declarations:

- `:450` is `AvailabilityScreen`, which stays in `AS`;
- `:5021` is `TripDatePickerDialog`, which went to F4.

## Checks

### 1. Moved, not changed

Command:

`git diff --color-moved=zebra --color-moved-ws=allow-indentation-change origin/pre-main..HEAD -- app/src/main`

It was run with `--color=always` and explicit colours for the moved and plain slots, and every
`+` and `-` line was classified by colour.

- **`AS`:** 3,445 lines removed. Of those, 3,412 are coloured moved, and the 33 that are not are
  the 32 `private` declaration lines of the widened declarations plus one blank line.
- **New files:** every added line that is not coloured moved is one of these:
  - a `package` line (7);
  - a header comment line (83);
  - an import (393);
  - a blank separator line (15);
  - an `internal` declaration line of a listed widening (32).

No other line is unmoved. No import line was removed from `AS`.

### 2. The same declarations

Command:

`git grep -h -e '^private ' -e '^internal ' -e '^fun ' -e '^val ' -e '^const ' -e '^enum ' -e '^class ' -e '^typealias ' -e '^@Composable' <rev> -- app/src/main/java/com/zynergylabs/forager/app/ui/availability | sort`

It was run at `origin/pre-main` and at `HEAD`.

- Both sides have 230 lines.
- `diff` shows 32 removed lines and 32 added lines.
- Every removed `private X` pairs with an added `internal X`, and the 32 are the listed widenings.
- Apart from those, the sets are equal.

### 3. Stage E's method

**`AS` after equals `AS` before with exactly the moved ranges removed.** Command:

`diff <(git show origin/pre-main:$AS | sed -e '328,376d' -e '2279,$d') <(git show HEAD:$AS)`

It produced no output, so the files are identical. The deleted spans hold only the moved ranges
and blank lines. The blank at `:377` is kept as the separator. Imports are unchanged:
`:1-327`, which is the package line, 319 imports and `ResultsTab`, is byte-identical.

**Each moved block is byte-identical to its source.** For each new file, the body after the
imports was taken with `tail -n +<last import + 2>`. `sed -E 's/^internal ((const val|val|fun|enum class|class) (CompactTab\.|ScreenEdge\.)?(<listed names>)\b)/private \1/'`
turned the listed declarations back to `private`, and `cmp` compared the result with the source
ranges. The source ranges were extracted by `git show origin/pre-main:$AS | sed -n 'a,bp'` and
joined by one blank line.

All seven files are byte-identical:

| File | Result |
|---|---|
| `AvailabilityNavigationUi.kt` | byte-identical to `329,376 2280,2427 4804,4835` |
| `AvailabilityCompactMapUi.kt` | byte-identical to `3324,3354 3356,4420 4422,4434 4442,4443 5005,5006` |
| `AvailabilityMapControlsUi.kt` | byte-identical to `3317,3321 4436,4440 4445,4802 4866,4996 4998,5003` |
| `AvailabilityMapOverlaysUi.kt` | byte-identical to `3076,3087 5008,5114 5345,5673` |
| `AvailabilityWideLayoutUi.kt` | byte-identical to `2429,2435 2437,2491 2958,2988 3089,3315 4837,4864` |
| `AvailabilitySettingsUi.kt` | byte-identical to `2493,2956 2990,3074` |
| `AvailabilityTripsWaypointsUi.kt` | byte-identical to `5116,5343` |

**Does the check bite?** It was run twice more with deliberate mistakes:

- F5 without reverting its two widenings reports `DIFFERS (4 differing lines)`.
- F7 against `5116,5342`, one line short, reports `DIFFERS (1 differing lines)`.

So the check can fail on both a keyword and a boundary.

### 4. Suite

The full unit suite (`./gradlew --stacktrace testDebugUnitTest`) was run twice. The JUnit XML and
the Gradle logs are kept outside the repository.

| | Commit | Classes | Tests | Failures | Errors | Skipped | Log |
|---|---|---|---|---|---|---|---|
| Before | `28dc7eb` (`app/` identical to `3bd0efe`) | 217 | 1,681 | 0 | 0 | 24 | `~/Zynergy/forager-split-baseline/` |
| After | `c511e26` | 217 | 1,681 | 0 | 0 | 24 | `~/Zynergy/forager-split-after/` |

**Differences: 0**, compared class by class (tests, failures, errors, skipped) and test case by
test case (status). The files `per-class-counts.tsv` are byte-identical.

The "before" result matches the dispatch's figure from
`docs/audits/2026-09-26-landscape-b1-completion-report.md:97-99`.

**Checks that the "after" run is current:**

- `app/build/test-results/testDebugUnitTest` was deleted before the run.
- The log shows `Task :app:testDebugUnitTest` executed and no `e:` line.
- The tree was clean at `c511e26`.
- `AvailabilityNavigationUiKt.class` and `AvailabilityWideLayoutUiKt.class` exist in
  `app/build/tmp/kotlin-classes/debug`.

**What this check shows.** A suite that passes the same before and after is what this dispatch
predicts, so here it is regression evidence only. It does not show that the move is verbatim.
That rests on checks 1 to 3.

### 5. Line counts

| File | Lines |
|---|---|
| `AvailabilityScreen.kt` | 2,229 (was 5,674) |
| `AvailabilityNavigationUi.kt` | 281 |
| `AvailabilityCompactMapUi.kt` | 1,218 |
| `AvailabilityMapControlsUi.kt` | 598 |
| `AvailabilityMapOverlaysUi.kt` | 532 |
| `AvailabilityWideLayoutUi.kt` | 418 |
| `AvailabilitySettingsUi.kt` | 618 |
| `AvailabilityTripsWaypointsUi.kt` | 275 |

F2 is over the ~1,200 target, which the planner accepted. The dispatch estimated it at about
1,300 lines.

## Predictions against outcome

**Planner:**

1. **Met.** The move compiles with only the listed widenings. None was forced.
2. **Met.** `AS` has 2,229 lines, inside the predicted 2,100 to 2,250.
3. **Met.** The suite is identical per class: 217 classes, 1,681 tests, 0 failures, 24 skipped.
4. **Met.** No test file changed: `git diff --stat origin/pre-main..HEAD -- app/src/test` is
   empty.

**Coder (intent `2026-09-27-15`):**

- **Zero forced widenings: met.**
- **`AS` at 2,229 lines with all 319 imports: met.**
- **Misses in method, not in outcome.** The import scan went wrong three times before the final
  compile, and each was caught a different way:
  - Its first form over-included named arguments. I caught this by reading a generated file's
    imports.
  - Its first refinement over-excluded type annotations (`: Color = …`). I caught this by
    reading the scan's excluded list.
  - Its final form under-included one semantics assignment. The compiler caught this.

  All three were fixed before commit `c511e26`.

## Not verified

- **Compose group keys and file-facade classes.** Moving a composable changes both its
  Compose group keys and the JVM file-facade class that holds it: `AvailabilityScreenKt` becomes
  seven classes. No test or production code names a facade class. Nothing in the moved code or
  `AS` uses `rememberSaveable`, so no saved-state key is carried across builds through these
  functions. Whether a composable *called from* moved code (in another file) uses
  `rememberSaveable` was not checked, and any such key would change only across an app update,
  not within a build.
- **Device.** No device run was made, per the dispatch.
- **`assembleDebug`.** It was not run locally. CI runs it.
