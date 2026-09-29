# Dispatch 2026-09-28-160: the landscape icon cluster as an L — coder report

**Status: in progress (pre-registration pushed before building).** Sections below are appended as work lands; nothing above a "Resumed" heading is rewritten.

**Coder session.** The owner's launch prompt names `/model claude-sonnet-5-5`. The session's configured model id is `claude-sonnet-5-5`; the serving model was not independently read, so that is the configured identifier, not a verified one.

**Base.** `origin/journal-redesign` at `9c337f19` (fetched at the start of the session; it is the commit that carries `prompts/preserved/2026-09-29-26.md`). Worktree `/home/zynergy-labs/Zynergy/forager-wt/landscape-l`, branch `landscape-l`.

**Paths** (all under `app/src/main/java/com/zynergylabs/forager/app/ui/`): CMU = `availability/AvailabilityCompactMapUi.kt`, MCU = `availability/AvailabilityMapControlsUi.kt`, MC = `map/MapChrome.kt`.

## Premises re-verified at `9c337f19`

Read, not inferred:
- The landscape switch is `val landscapeCluster = railPortEdge != null && punchHoleEdge != null`, CMU:409; the branch is `ShortLandscapeClusterRow(...)`, CMU:1096; the composable is at CMU:1535. (Dispatch cites CMU:1095-1105 and :1534-1548: off by one line each; same code.)
- The container `Surface` opens at CMU:1025 (dispatch: 1024), `.testTag(MAP_ICON_CLUSTER_TAG)` at CMU:1041. It carries `color = mapIconClusterContainerColor()` (0.6), `shadowElevation = 2.dp`, a 1 dp border, and the measuring `onGloballyPositioned`.
- The bar is `MapIconBar`, MC:382; its `Column` has `padding(vertical = Spacing.xs)` and `spacedBy(Spacing.xs)`, MC:450-453: 5 × 48 + 4 × 4 + 2 × 4 = 264.
- Each row is `MapBarIconButton`'s 48 dp `Box` with `clickable` (MC:726-731), inside the `Surface`'s 24 dp rounded shape.
- The pill is `ControlPill`, MCU:178, a private composable with a `Column` of two `MapBarIconButton`s, the same padding and spacing, filled at `mapIconClusterChildColor()` (0.5).
- `mapIconBarRowAnchorOffset` hardcodes `Spacing.xs` row pitch, MC:90-95; `ADD_TILE_ANCHOR_OFFSET` is MCU:612; its only production caller for the compact map is CMU:1409. `AvailabilityScreen.kt:334` imports `mapIconBarRowAnchorOffset` (used by the Cartography-entry and wide-tree callers; not touched).
- `MapIconBar` has a second caller, `ui/log/CartographyEntryReportScreen.kt:532`, per the dispatch. Not opened or edited by this work.
- The remembered offsets, side and minimised flag are keyed on `landscapeCluster` at CMU:410-441; unchanged by this work.

## Design used (from the dispatch, unmade decisions listed in "Decisions I made" at the end)

- In landscape only: the container `Surface` becomes a plain `Box` (same padding, same measuring, same tag) that draws nothing and consumes no pointer input; inside it a `Column` holds the bar and, 8 dp beneath, a horizontal pill. Both are aligned to the cluster's outer edge.
- The bar gets a landscape row pitch of 0 and no end padding (240 dp); rendered at the standing 0.8 chrome alpha as one layer.
- The pill gets a horizontal form (record, then return, 48 dp thick) at the same 0.8, and the same zero pitch/padding.
- `mapIconBarRowAnchorOffset` gains a defaulted row-pitch parameter; the landscape AddActionTile anchor passes 0.
- Portrait Column, wide tree, `CartographyEntryReportScreen`, `MapChromeAlphaTest` untouched.

## Pre-registration (written and pushed before any test or code)

All at `w823dp-h384dp-land`, `ROTATION_90` (cluster on the left) and `ROTATION_270` (cluster on the right), `isRecording = true` unless stated.

**Predictions against the base (`9c337f19`), tests-first.** Every new test below fails at base for the reason named; a pass at base is a stop.

| # | Claim | Pass condition | Fails at base because |
|---|---|---|---|
| P1 | L shape | bar 48 × 240 (five contiguous 48 dp rows), gap 8, pill 48 thick × 96, pill outer edge flush with bar outer edge, return inboard of record, cluster 296 tall × 96 wide | base bar is 264 tall, pill is beside the bar, cluster 264 tall |
| P2 | Empty corner and gap reach the map | a real long-press at the point inboard of the bar's mid-height, and one in the 8 dp gap, each increments the map stub's long-press count; a long-press on the bar itself does not (positive control) | base container `Surface` covers both |
| P3 | Nothing drawn | pixels in the corner and the gap equal a reference map pixel (native graphics) | base container fill (0.6) is drawn there |
| P4 | Single-layer 0.8 | bar and pill interior pixels equal `0.8·chrome + 0.2·background` per channel, within 2/255 | base children composite over the container (0.92 over background) |
| P5 | End-row touches | five real touches across each of the fullscreen row and the add row (fractions 0.2/0.8 corners and centre of the row's own 48 dp bounds), all reach it; touches on rows 2 and 3 never reach the map | passes at base for touch routing where the rows already reach; the bar geometry asserted in the same test is what fails — flagged: this one may pass at base on its touch half and is not the discriminating test |
| P6 | Drag, snap, minimise, restore | after a snap the pill stays flush with the bar's outer edge and inboard-extending; 40 dp drag moves the cluster 40 dp; minimise and restore returns to the same place | shape assertion fails at base |
| P7 | Clamps | with the cluster dragged to the bottom, `bottom == mapArea.bottom` and height 296; with a search notice showing and the cluster dragged to the top, `top >= notice bottom` and height 296 | height is 264 at base |
| P8 | Anchor | `mapIconBarRowAnchorOffset(5, rowPitch = 0.dp)` is +96 dp from the bar's centre; the default call is unchanged at +104 dp | the parameter does not exist at base (compile error is expected; recorded, not a stop) |

**The corner-clipping stop (dispatch).** Prediction: five touches at fractions 0.2/0.8 of an end row's 48 dp box lie inside the rounded end. From the 24 dp corner radius on a 48 dp wide bar, the point at fraction (0.2, 0.2) is at distance √2 × 14.4 = 20.4 dp from the arc's centre (inside 24); at (0.1, 0.1) it is 27.0 (outside). So the gate samples are inside by geometry, and the extreme corners of the 48 dp box are outside the drawn shape by geometry. Whether Robolectric's Compose hit-testing follows the rounded clip is undetermined (the map, §6). A separate, non-gating probe of the extreme corners is run once and its result reported here rather than committed as a test.

**Machine.** 2332 MB available at the first check, under the 2.5 GB rule; no Gradle run started until both the memory and the Java-Gradle-process checks pass.
