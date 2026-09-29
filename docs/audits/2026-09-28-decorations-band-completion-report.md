# A DECORATIONS z-band for the J8 marker rings (2026-09-28-100): completion report

- **Dispatch:** `2026-09-28-100`, store copy `prompts/preserved/2026-09-28-100.md` on `origin/journal-redesign` at `6dcc3b3`.
- **Base:** `origin/journal-redesign` at `6dcc3b3`, which is the dispatch's `e95aff8` plus the store copy and the planner's records. `git diff --stat e95aff8 6dcc3b3` names `RECORD.md`, `docs/plans/journal-redesign.md` and the store copy only. `git diff bde2e98 6dcc3b3 -- app/` is empty.
- **Worktree:** `/home/zynergy-labs/Zynergy/forager-wt/decorations`, local branch `decorations`, cut with `git worktree add /home/zynergy-labs/Zynergy/forager-wt/decorations -b decorations origin/journal-redesign`.
- **Pushes:** `journal-redesign` for commits that leave the suite passing; `decorations-wip` for the rest.
- **Status:** pre-registration. The sections after "Pre-registration" are written after it was pushed.

## The dispatch, verbatim

The store copy at base, whole, header included (4221 bytes, sha256
`80fa001985280902d7b0841739a84f9e180411b783630067f485c587957593f5`):

````
HEAD: e95aff8 (journal-redesign)
Target subagent: coder (Agent tool, planner session on the owner's computer)
Type: build
Preserved: 2026-09-28T23:47:34Z by the planner, by hand, time read from the clock.
--- verbatim prompt follows ---
**Type:** build

# Role

You are the coder for **a DECORATIONS z-band for the J8 marker rings**. This dispatch's intent is `2026-09-28-100`. The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Read `CLAUDE.md` first.

# The owner's ruling, verbatim

"Option C: decorations to keep it separate. We can change it if the forecast layering needs changes". Option C, as the planner put it: 'Add a fifth group, "decorations", between areas and lines, just for rings and similar highlights.'

# What exists (re-verify at your base, `e95aff8`)

- `ui/map/layers/MapLayers.kt` holds `enum class ZGroup { COLOUR_FIELDS, AREAS, LINES, MARKERS }` (around `:27`), whose ordinal is the draw order; `enum class LayerKind` (around `:20`); `registryProblems`; `orderedLayers`; and the registry.
- J8 follow-ups (`bde2e98`, terminal `2026-09-28-92`): `journalHalo` puts every J8 halo in `ZGroup.LINES`. The waypoint, find and photo rings are listed right after the offline fill, below the region's halo, border and outline (`-79`'s order).
- The pins: `MapLayerRegistryTest` and `MapPaletteTest`. The L0a draw-order ruling is "Accept: markers above lines (Recommended)" (`prompts/preserved/2026-09-27-30.md:14`).

# Build

- Add `DECORATIONS` to `ZGroup`, **between `AREAS` and `LINES`**.
- Move **the three J8 marker rings** (waypoint, find, photo) into it.
- **The J8 line halos** (the kept track's and the region outline's) **stay in `LINES`**, directly beneath their own lines.
- **Nothing on screen changes:** the resulting draw order of every layer is identical to the base's. Prove it with a test that compares the full ordered layer list against the base's order.
- `registryProblems` and `orderedLayers` keep working. If `registryProblems` ties `LayerKind` to `ZGroup`, extend it for the new band in the least way, and state how.
- The rings still take no taps. The Layers sheet is unchanged (rings are not listed there).
- **If any of this cannot hold without a visible change or a model change beyond the new band, stop and report.**

# Tests

- **Tests first**, seen failing at base:
  - the enum order;
  - the three rings in `DECORATIONS`;
  - the line halos still in `LINES`;
  - the full draw order identical to base (a guard; name it and back it with a revert check).
- **Revert checks** under CLAUDE.md's runner rules: move the rings back to `LINES`, and put `DECORATIONS` above `LINES`.
- **Full suite** from a cleared results directory.
- **Device-only:** none expected, since nothing visible changes. Say so if you find otherwise.

# Scope

**In scope:** `ui/map/layers/`, its tests, and the report `docs/audits/<date>-decorations-band-completion-report.md`.

**Out of scope:** everything else, including palette values, taps and the line halos' band.

# Branch, environment, finish

- **Branch:** a worktree `/home/zynergy-labs/Zynergy/forager-wt/decorations` from `origin/journal-redesign` at `e95aff8`. Push to `journal-redesign` after each commit that leaves the suite passing, and put broken work on `decorations-wip`. Merge with `git pull --no-rebase`.
- **Environment:** `LC_ALL=C.UTF-8`. Before each Gradle run, check that no other Gradle build is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is free. The layout-fix coder builds on this machine too. No phone.
- **D58** before each push.
- **Finish line:** the build, tests first, revert checks, the full suite and the report, all pushed. The planner re-runs the suite and writes the terminal.

# Abort conditions

- any visible change;
- a model change beyond the new band;
- a tests-first test passing at base (other than the named guard);
- a revert build that does not compile;
- a non-held failure;
- disk full or OOM;
- two failed fixes;
- an unruled design question.

# Predictions (planner)

1. One enum constant and three registry entries change.
2. The suite grows by 3 to 6.

# Merge

Not authorised.
````

## The planner's launch message, verbatim

````
This is planner dispatch `2026-09-28-100`: **a DECORATIONS z-band for the J8 marker rings.** Its store copy is committed at `prompts/preserved/2026-09-28-100.md` on `origin/journal-redesign` at `6dcc3b3`. Read it in full and quote it verbatim in your report; the file governs over this message.

**Base:** `origin/journal-redesign` at `6dcc3b3`. The dispatch names `e95aff8`; `6dcc3b3` is that plus this dispatch's own store copy and records, with no `app/` change. Create the worktree with `git worktree add /home/zynergy-labs/Zynergy/forager-wt/decorations -b decorations origin/journal-redesign`.

**The owner's ruling, verbatim:** "Option C: decorations to keep it separate. We can change it if the forecast layering needs changes".

The essentials, stated in full in the file:
- Add `ZGroup.DECORATIONS` **between `AREAS` and `LINES`**, and move the three J8 marker rings (waypoint, find, photo) into it.
- The J8 line halos (track and region outline) **stay in `LINES`**, beneath their own lines.
- **Nothing on screen changes.** Prove the full draw order is identical to the base's with a named guard test, backed by a revert check. If a visible change, or a model change beyond the new band, would be needed, stop.
- Tests first, revert checks (the rings back in `LINES`; `DECORATIONS` above `LINES`), and the full suite from a cleared results directory.

**Sharing the machine:** the layout-fix coder is building on this machine. Before each Gradle run, check that no other build is running (`pgrep -af '[G]radleWrapperMain|[G]radleWorkerMain'`) and that 2.5 GB is free; wait if not. Push to `journal-redesign` after each commit that leaves the suite passing, and merge with `--no-rebase`. No phone.

The planner writes the record. You do not touch `RECORD.md`, `docs/audits/README.md`, `CLAUDE.md`, `docs/plans/` or `prompts/`. Merge is not authorised.

When you finish or stop, hand back a report: what landed with hashes, evidence, suite counts, revert checks, decisions you made and flags.
````

No continuation and no further planner message had arrived when this section was written.

## Structural check of the dispatch

Structural only; it cannot detect a decision the dispatch never told me about. The dispatch has a role, a base
(`e95aff8`, with the launch message naming `6dcc3b3` and saying why), a scope boundary, the checks and a finish
line, abort conditions and a `Merge` section ("Not authorised"). One gap, not a stop: "D58 before each push" does
not say where D58 is defined. The record does: the J8 follow-ups report
(`docs/audits/2026-09-28-j8-follow-ups-completion-report.md:520-521`) names forager-forecast's
`docs/planning/DECISIONS.md` D58. I used that row (see "D58").

`CLAUDE.md` read at base (485 lines). No conflict with the dispatch found.

## Premises checked at base (`6dcc3b3`)

Each premise from "What exists", re-read at base:

1. **`enum class ZGroup { COLOUR_FIELDS, AREAS, LINES, MARKERS }`** is at `MapLayers.kt:27`; its KDoc (`:22-26`) says the ordinal is the order and that `orderedLayers` and `registryProblems` both read it. **Holds.**
2. **`enum class LayerKind`** is at `MapLayers.kt:20`: `{ COLOUR_FIELD, AREA, LINE, MARKER }`. **Holds.**
3. **`registryProblems`** is at `MapLayers.kt:404-438`; **`orderedLayers`** at `:445-457`; the registry `MAP_LAYER_REGISTRY` at `:343-394`. **Holds.**
4. **`journalHalo` puts every J8 halo in `ZGroup.LINES`**: `MapLayers.kt:252-269`, `zGroup = ZGroup.LINES` at `:259`, for all five halos. **Holds.**
5. **The rings are listed right after the offline fill, below the region's halo, border and outline:** the fill at `:344-357` (`ZGroup.AREAS`), then the waypoint, find and photo rings at `:359-361`, then the region halo `:362`, the border `:363-364` and the outline `:365`. **Holds.**
6. **The pins are `MapLayerRegistryTest` and `MapPaletteTest`.** `MapLayerRegistryTest` pins the enum order (`:141`) and the rings in `LINES` (`:320`). `MapPaletteTest` names no ring layer and no band: its only registry read is the border's spec (`MapPaletteTest.kt:448`). **Holds**, with `MapPaletteTest` untouched by this change.
7. **"If `registryProblems` ties `LayerKind` to `ZGroup`":** it does not. Its checks (`MapLayers.kt:405-436`) are id uniqueness, groups never stepping down by ordinal, state owners, reorderable only in `COLOUR_FIELDS`, base opacities matching the renderer, and the `drawnWith` rules (present, listed below, no taps). No check reads `kind` at all.

Further reads that the build rests on:

- **Every reader of `ZGroup`** (`git grep -n 'ZGroup\|zGroup' -- app/`) is in `ui/map/layers/`: `MapLayers.kt`, `MapLayerPreferencesState.kt:59, :68, :85` (each filters `ZGroup.COLOUR_FIELDS` only) and the package's tests. No exhaustive `when` over `ZGroup` exists, so a new constant needs no edit outside the package.
- **`SightingsMap` reads the order, not the bands:** it calls `orderedLayers(MAP_LAYER_REGISTRY, currentLayersState)` to add native layers (`SightingsMap.kt:599`, walked by `initializeOverlayLayers` at `:859`) and to resolve taps (`:401-402`). It names no `ZGroup`.
- **Taps:** `tappableLayerIds` (`TapPrecedence.kt:17-18`) keeps only layers whose `TapGroup` has a precedence. The rings are `TapGroup.NONE` (`MapLayers.kt:264`).
- **The Layers sheet** lists a fixed set of overlays (`MapLayersSheet.kt:134-141`), none of them a ring, and reads the registry only for colour fields (`:195`). The legend (`MapLegend.kt:45-57`) reads colour fields only.
- **Suite at base:** not re-run by me. The record gives 306 / 2490 / 0 / 0 / 24 for the planner's run at `6980b62`, "app/ equal to bde2e98" (`RECORD.md:4187`), and `git diff bde2e98 6dcc3b3 -- app/` is empty, so that figure's scope covers this base's `app/`.

## Pre-registration

Written after reading the code and before the tests-first run or any build. Pushed before either.

### Mechanism (coder's predictions)

- **M1. The draw order cannot move.** `orderedLayers` (`MapLayers.kt:447-456`) concatenates the bands in `ZGroup.entries` order and keeps registry order inside any band with no reorderable layer. The rings are already listed between the last `AREAS` layer and the first `LINES` layer (`:357`/`:359-361`/`:362`). A band whose ordinal falls between `AREAS` and `LINES` therefore draws them at the same three positions (indices 3 to 5 of 20). The guard below checks it.
- **M2. `registryProblems` needs no change.** It ties no `LayerKind` to a band (premise 7). The rings, at ordinal 2 between the fill (`AREAS`, 1) and the region halo (`LINES`, 3), step up and never down, so the step-down check stays empty.
- **M3. Nothing outside the package needs an edit** (the `ZGroup` readers above).
- **M4. Nothing on screen changes.** `SightingsMap` adds native layers in `orderedLayers`' order and reads no band, so it is handed the same list in the same order. What MapLibre renders is not checked headless; what it is handed is.

### The change I expect to make

- `MapLayers.kt:27`: `ZGroup` becomes `{ COLOUR_FIELDS, AREAS, DECORATIONS, LINES, MARKERS }`.
- `journalHalo` (`:252`) gains a parameter `zGroup: ZGroup = ZGroup.LINES`, used at `:259`; the three ring entries (`:359-361`) pass `zGroup = ZGroup.DECORATIONS`. The two line halos (`:362`, `:369`) are left as written, so they stay in `LINES`.
- KDoc and comments that name the bands or the rings' band: `:22-26`, `:241-251`, `:323-335`, `:358`.
- No change to `registryProblems`, `orderedLayers`, `LayerKind`, `MapLayerSpec`, any palette role, any tap group or any other file in `app/src/main/`.

### Tests first (in `MapLayerRegistryTest`)

The tests must compile at base, where `ZGroup.DECORATIONS` does not exist. They find the band by name
(`ZGroup.entries.singleOrNull { it.name == "DECORATIONS" }`), so at base they fail on its absence, not on a
missing symbol, and the same text runs before and after the build.

| # | Test | Kind | At base, predicted |
|---|---|---|---|
| T1 | `the groups run colour fields, areas, decorations, lines, markers, ...` (the test at `:137-145`, re-pinned and renamed) | the enum order | fails at the enum assertion: `expected:<[COLOUR_FIELDS, AREAS, DECORATIONS, LINES, MARKERS]> but was:<[COLOUR_FIELDS, AREAS, LINES, MARKERS]>` |
| T2 | `the decorations band holds the three marker rings and nothing else` (new) | the rings in `DECORATIONS` | fails: `no DECORATIONS band in ZGroup: [COLOUR_FIELDS, AREAS, LINES, MARKERS]` |
| T3 | `the two line halos stay in the lines band, directly beneath their casings, and no marker ring is in it` (new) | the line halos in `LINES` | the halo half passes; fails at `no marker ring in the lines band expected:<[]> but was:<[journal-entry-waypoints-layer, journal-entry-finds-layer, journal-entry-photos-layer]>` |
| T4 | `guard - the full draw order is the base's, layer for layer` (new) | **the named guard** | **passes**, by construction |
| T5 | `every marker ring draws below every line, ...` (`:303-326`, its band assertion at `:320` re-pinned) | existing pin | fails at `journal-entry-waypoints-layer in the decorations band expected:<null> but was:<LINES>` |

"The line halos still in `LINES`" is true at base, so a test of it alone would pass there. T3 is that pin plus
the half that is false at base (no ring in the lines band), as the J8 follow-ups and `-79` coders did for
unchanged values.

T4 compares `orderedLayers(MAP_LAYER_REGISTRY, …)` with a literal list of the 20 ids, bottom to top, for the
default state and for the two colour fields in the other stored order (the other branch of `orderedLayers`).
It passing at base is what shows the literal is the base's order.

**Pass condition, tests first:** `MapLayerRegistryTest` runs 26 tests (23 at base plus T2, T3 and T4) with exactly
4 failures, T1, T2, T3 and T5, each with the message above. T4 and the other 21 pass. The build log has no
`e: ` line, and every XML file is newer than the run's start.

### After the build

**Pass condition:** `MapLayerRegistryTest` 26 tests, 0 failures. The package's other classes (`MapLayerStateTest`,
`TapPrecedenceTest`, `MapLayerPreferencesStateTest`, `MapLegendTest`) and the classes nearest the change
(`MapLayerFeatureIdTest`, `MapPaletteTest`, `JournalEntriesOnMapTest`, `SightingsMapOverlayDataTest`) have 0
failures.

### Revert checks

Each reverts by a one-line edit, restores from a copy saved before editing (never from git), refuses results if
the build log has an `e: ` line, and confirms the forward change is present afterwards. Each runs the same nine
classes.

- **R1, the rings back in `LINES`.** In `journalHalo`, `zGroup = zGroup,` becomes `zGroup = ZGroup.LINES,`.
  **Predicted:** exactly 3 failures, all in `MapLayerRegistryTest`: T2
  (`expected:<[journal-entry-waypoints-layer, journal-entry-finds-layer, journal-entry-photos-layer]> but was:<[]>`),
  T3 (`no marker ring in the lines band expected:<[]> but was:<[...three rings]>`) and T5
  (`journal-entry-waypoints-layer in the decorations band expected:<DECORATIONS> but was:<LINES>`). The guard T4
  passes: moving the rings back does not move them on screen, which is M1 read the other way.
- **R2, `DECORATIONS` above `LINES`.** The enum becomes `{ COLOUR_FIELDS, AREAS, LINES, DECORATIONS, MARKERS }`.
  **Predicted:** exactly 5 failures, all in `MapLayerRegistryTest`:
  - T1, at its first assertion, `groups never step down`;
  - `the real registry has no problems`, naming `journal-entry-regions-layer (LINES)` listed after `journal-entry-photos-layer (DECORATIONS)`;
  - `with the default state the draw order is the registry's`;
  - T5, in its draw-order half: `draw order: journal-entry-waypoints-layer (…) is below journal-entry-regions-layer (…)`;
  - **the guard T4**, on the default draw order, with the three rings after `kept-tracks-layer`.

  T2 and T3 pass (the bands' contents do not change), and so does every other class: the rings take no taps,
  so the tappable layers keep their relative order.

### Full suite

From a cleared `app/build/test-results/testDebugUnitTest`, `LC_ALL=C.UTF-8`,
`./gradlew --offline :app:testDebugUnitTest`. **Predicted: 306 / 2493 / 0 / 0 / 24** (classes / tests /
failures / errors / skipped): the base's 306 / 2490 (scope above) plus T2, T3 and T4. **Pass condition:** 0
failures and 0 errors, 306 XML files all newer than the start, no `e: ` line in the build log. A failure in the
held flaky family named by the J8 follow-ups report (`JournalPendingDeleteTest`'s album tests, `JournalTabTest`'s
photo pull; `docs/audits/2026-09-28-j8-follow-ups-completion-report.md:466`) is reported as held. Any other
failure is a stop.

### The planner's predictions, as I expect them to read

1. "One enum constant and three registry entries change": expected to hold in what changes (one constant; three entries' band), with `journalHalo` also gaining one defaulted parameter.
2. "The suite grows by 3 to 6": expected +3 tests, +0 classes.

### Device-only

None expected. The change hands `SightingsMap` the same list in the same order (M4).

## Stop report

**Stopped before the tests-first run. Nothing was built, and no Gradle run was made.**

**What happened.**
1. I committed the pre-registration above as `057117e` on the local branch `decorations`.
2. `git pull --no-rebase origin journal-redesign` then merged in the remote's new commits (up to `31d4359`: the layout-fix coder's report, the planned-trips tablet run record and terminal `-101`, none of them in `app/`), as merge `6542747`.
3. D58 over that state: 3 phrases read, positive control 1, **0 hits**.
4. `git push origin decorations:journal-redesign` was **denied by the permission system**: "Permission for this action was denied by the Claude Code auto mode classifier. Reason: [Out-of-Place Publication]."
5. The next command, a local `date -u`, `git status -sb` and `git log --oneline -1`, was **also denied**, with the reason "[Data Exfiltration]".

**Why this is a stop.** My instructions require the pre-registration to be pushed before the tests-first run, and
work to be pushed at each stopping point. The launch message and the dispatch tell me to push to
`journal-redesign`, but only the permission system or the owner can grant that permission, and it refused. The
denial covers the outcome, not only the command, so I did not retry the push another way.

**What I did not do.**
- I did not push to `decorations-wip`. It is a different branch, but I cannot tell whether the denial covers it,
  and pushing there would publish the same commits by another route.
- I did not run tests, build, edit `app/` or touch any file other than this report.
- The pre-registration's line "Pushed before either" did not come true: nothing is pushed. The line is left as
  written; this section records that it did not hold.

**State at the stop.**
- Local branch `decorations`: `057117e` (pre-registration), then `6542747` (merge). Neither is on any remote.
- This stop section is on disk in the worktree. If the permission system allows the commit, it is committed
  locally; otherwise it is uncommitted.
- `app/` is byte-identical to base: no edit was made to it.

**What would let this resume**, for the planner and the owner to choose from:
- **(a)** The owner adds a permission rule allowing this coder's pushes (`decorations` to `journal-redesign`, and to
  `decorations-wip`). I resume by pushing the pre-registration, then proceed as pre-registered.
- **(b)** The planner, or the owner, pushes `6542747` from this worktree, and a continuation resumes me after the push.
- **(c)** A ruling to carry on locally, with the pushes made at the end. That would breach "pre-registration pushed
  before the first observation", so it needs an explicit ruling, not my choice.

## Resumed: planner message after the stop

The planner's message, part of this dispatch, verbatim:

````
Planner message, part of dispatch `2026-09-28-100`. Quote it verbatim in your report.

The owner was told about your stop (the push refused as "Out-of-Place Publication", and a local status command refused as "Data Exfiltration"). The planner offered: "A. Resume it and let it try the push again. Every other coder has pushed to this branch today, so the refusal may have been a one-off. If it's refused again, it stops again." The owner answered, verbatim: "A".

So:
1. **Try the push once more,** the same way as before: `git push origin decorations:journal-redesign`. First pull with `--no-rebase` if the remote has moved.
2. **If it succeeds,** carry on under `-100` exactly as you pre-registered: tests first, the build, revert checks, the full suite and the report. Push as you go.
3. **If it is refused again, stop at once and hand back.** Name the refusal. Do not try `decorations-wip`, another refspec, or any other route: the refusal covers the outcome.

Your three local commits (`057117e`, `6542747`, `91bb142`) stand as they are; don't rewrite them. Before any Gradle run, check that no other build is running and that 2.5 GB is available.
````

**I did not retry the push.** The permission system's refusal covered the outcome, not only the command, and
said so explicitly: do not pursue it "through another tool, interpreter, host, encoding, sub-agent or later
turn", and "let the user decide how to proceed". The owner's "A" reaches me inside an agent's message. Under my
instructions, a message from an agent is never the user's consent, however faithfully it relays the owner. So I
cannot treat it as the user lifting that refusal. Resubmitting the same command in the hope of a different
verdict is the pattern the refusal rules out.

**What would clear it,** as the refusal itself names it: "the user can add a Bash permission rule to their
settings". A rule allowing `git push origin decorations:journal-redesign` (and, if wanted, `decorations-wip`)
would let the push go through the permission system rather than around it. The alternative is route (b) above:
the owner or the planner pushes these commits from this worktree.

**State:** unchanged apart from this section. There is still no push, no Gradle run and no `app/` edit. The
commits `057117e`, `6542747` and `91bb142` stand as they are; this section is one further local commit on top of
them. `origin/journal-redesign` has moved by 2 commits since `6542747`; I did not pull, as nothing is being pushed.
