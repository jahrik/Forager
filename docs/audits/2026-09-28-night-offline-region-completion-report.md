# Night offline region: completion report (2026-09-28)

Intent `2026-09-28-19` (build, `prompts/preserved/2026-09-28-19.md`), with continuations `-20`, `-22`,
`-24`, `-26` (device capture) and `-36` (this one, which governs). Branch `journal-redesign`, worked in
`/home/zynergy-labs/Zynergy/forager-wt/night-region` on local branch `night-region`. The planner writes
the record and the terminal; this report does not touch `RECORD.md`.

**Outcome.** `MapPalette.NIGHT.offlineRegion` is `#202020` (was `#FFFFFF`), at the unchanged 0.2 fill
opacity. The owner picked it from phone shots. Tests first, a compiling revert check and the full suite
are below. What the region looks like on a screen at night is device-only and was not seen by this
build.

Paths below are under `app/src/main/java/com/zynergylabs/forager/app/` (production) or
`app/src/test/java/com/zynergylabs/forager/app/` (tests). Line numbers are at `b3e2f1c` unless another
commit is named.

## This continuation's message, verbatim

From `prompts/preserved/2026-09-28-36.md`, below its "verbatim prompt follows" line:

> You are finishing dispatch 2026-09-28-19. Before starting, read:
> - `2026-09-28-19.md`, `-22`, `-24` and this file (this one governs);
> - the capture record `docs/audits/2026-09-28-night-region-candidates-capture-run-record.md`.
>
> Work in `/home/zynergy-labs/Zynergy/forager-wt/night-region`: run `git pull --no-rebase`, then push to `journal-redesign`. Quote this message verbatim in your report.
>
> **The owner picked `#202020`.** Asked "Is that #202020 at 20%", the answer was yes. The planner then pointed out that it nearly vanishes over the darkest ground; the owner replied, verbatim, "That's my pick."
>
> 1. **Change the colour.** Set `MapPalette.NIGHT.offlineRegion` to `0xFF202020`. Its doc comment records:
>    - the owner's words: "Darker shade (Recommended)", "Not too dark since it can make it harder to read. Find a balance.", "You pick from phone shots (Recommended)", "That's my pick.";
>    - the three candidates;
>    - the capture record.
> 2. **Tests first.** Update the pins that name the old value, in the class's ratchet style, and report each old and new assertion:
>    - `every role holds the owner's colour`;
>    - the 78 as-drawn pins, re-measured for `#202020`;
>    - night (d) for `offlineRegion`, if it changes.
>
>    Seen failing against `#FFFFFF` first, then passing.
> 3. **Revert check**, under CLAUDE.md's runner rules, with the file restored from a saved copy.
> 4. **Full suite**, from a cleared results directory, with counts from the JUnit XML. Another coder (`2026-09-28-34`) may be building on this machine; check `df` first (about 13 GB free). Stop if the machine runs out of disk or memory.
> 5. **Report.** Write `docs/audits/2026-09-28-night-offline-region-completion-report.md` with -19's sections, covering every continuation (-20, -22, -24, -26 and this one).
>    - Under device-only: the region on the S22 at night once this build is installed, especially whether the dashed outline carries the edge over the darkest ground, where the fill nearly vanishes (ΔE 0.001 over `#22201C`).
>
> **Scope:** `ui/theme/MapPalette.kt`, `MapPaletteTest.kt` and the report. Nothing else. Also run D58 before the push. No phone, and no emulator.

A coordinator message reached this session mid-run, after the tests-first run and before its commit.
Verbatim (no planner-log line was given, so none is cited):

> A network outage (EAI_AGAIN) stopped you. The network is back, and the owner said "Try again". Your worktree, `/home/zynergy-labs/Zynergy/forager-wt/night-region`, still has your uncommitted edit to `MapPaletteTest.kt`. Carry on from there under continuation `2026-09-28-36`. Commit and push at your next stopping point.

This session did not observe the outage itself. The tests-first run had completed and its JUnit XML
was on disk, and the worktree held the uncommitted `MapPaletteTest.kt` edit, as the message said. Work
carried on from there, and the edit was committed and pushed as `2d1d783`.

## The chain, and where each part's evidence is

Only the last part (`-36`) was run by this session. The earlier parts are summarised from the record
(`RECORD.md` entries `2026-09-28-19`, `-20`, `-22`, `-24`, `-26`, `-36`), commit `75c050f`'s message and
the capture record. They are **not re-run or re-derived here**, except where a line says so.

| Part | What it asked | What happened (source) |
|---|---|---|
| `-19` build | `NIGHT.offlineRegion` white to black at 0.2; tests first; floors re-run | The coder stopped before any change: `MapPaletteTest`'s solid-mark checks (a) and (c) for night `offlineRegion` fail for any darker fill (record `-22`, Reason). The planner's prediction 2, "no contrast floor breaks", was wrong. |
| `-20` message | Owner: "Not too dark since it can make it harder to read. Find a balance." Pick a dark grey by the planner's rules 2 (label contrast) and 3 (0.02 ΔE) | Rule 2 needs label colours that are baked into the raster tiles; the offline vector style has no label layers (record `-22`, Reason). Stopped. |
| `-22` continuation | Owner: "Test it as drawn (Recommended)", "You pick from phone shots (Recommended)". As-drawn checks with the planner's thresholds (0.02 ΔE, 3:1 outline) | No grey passes either threshold on every cluster, and `#FFFFFF` already failed the outline check (record `-24`, Reason; the coder counted three clusters, not the planner's four, record `-26`, Notes). Stopped with nothing committed. |
| `-24` continuation | Thresholds withdrawn; as-drawn figures pinned, not gated; three candidates `#202020`, `#404040`, `#606060`; APKs built, not pushed | `75c050f`: night `offlineRegion` leaves (a) and (c); 78 as-drawn pins over 26 clusters at `#FFFFFF`; `OFFLINE_REGION_FILL_OPACITY` `private` to `internal` (`ui/map/layers/MapLayers.kt:153`). Coder's suite at `75c050f`: 267 / 2194 / 0 / 0 / 24 (record `-26`). APKs and signing facts in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-night-region-candidates/FACTS.txt` (outside the repository). |
| `-26` device | Install the three in turn on the S22 and capture identical night views | `docs/audits/2026-09-28-night-region-candidates-capture-run-record.md`: every view taken; `606060-B` mis-framed and not retaken; 606060 left installed; Night Maps restored to off. |
| `-36` continuation | The owner picked `#202020`; set it, re-measure the pins, revert check, suite, this report | This report. |

## Verification (this part)

- **Base.** `origin/journal-redesign` was at `be84b78` after `git fetch`, the commit carrying
  `2026-09-28-36.md`. `git pull --no-rebase` in the worktree fast-forwarded `night-region` from
  `75c050f` to `be84b78` with no conflict.
- **Premise: the current value.** `ui/theme/MapPalette.kt:101` at `be84b78`: `offlineRegion =
  0xFFFFFFFF.toInt()`. Confirmed.
- **Premise: the only pins naming the old value are in `MapPaletteTest`.** `git grep` over `app/src`
  for `offlineRegion` outside `MapPaletteTest.kt` finds only the data layer's unrelated
  `offlineRegionId` and the fill layer's read, `palette.offlineRegion` (`ui/map/SightingsMap.kt:877`).
  No other test pins `NIGHT.offlineRegion`. Confirmed.
- **The pin arithmetic.** Before editing, a Python copy of the test's helpers (sRGB composite with
  `roundToInt` half-up, WCAG luminance, Ottosson Oklab, floor to three decimals, the opacity as the
  float `0.2f` widened to double) reproduced all 78 `#FFFFFF` pins at `75c050f` exactly, line for line.
  It was then run for `#202020` to write the new pins. The test itself is the arbiter: it passes at
  `#202020` (below), and the Python figures are not cited as evidence on their own.
- **Night (d) for `offlineRegion` changes**: 0.0994 against `searchCentre` at `#FFFFFF`, 0.3604 against
  `centrePin` at `#202020`. Every other night role's nearest neighbour is unchanged except
  `searchCentre`'s (see Flags).
- **Disk and memory** before each Gradle run: 13 GB free on `/`, about 4 GB memory available. No OOM
  kill. Another coder (`2026-09-28-34`) was building; no daemon was stopped.

## Tests first

Commit `2d1d783`, pushed (with merge `bce4a16`, which took in the other coder's pushes). Only
`MapPaletteTest.kt` changed; `MapPalette.NIGHT.offlineRegion` was still `#FFFFFF`.

Assertions changed, old and new:

1. `every role holds the owner's colour, day and night`: `"offlineRegion" to (0xFF0B0B0B to
   0xFFFFFFFF)` became `"offlineRegion" to (0xFF0B0B0B to 0xFF202020)`.
2. `night offline region as drawn matches its recorded figures over every night ground cluster`: the
   26 `AsDrawnPin` rows in `nightOfflineRegionAsDrawn`, re-measured for `#202020`. Each row's
   `casingOverGround` does not depend on the fill and is unchanged, so 52 figures moved:

   | Ground | ΔE old → new | Casing over region old → new | Casing over ground |
   |---|---|---|---|
   | `#4E6012` | 0.115 → 0.044 | 4.866 → 2.501 | 3.005 |
   | `#627524` | 0.099 → 0.056 | 6.099 → 3.233 | 4.089 |
   | `#334801` | 0.137 → 0.028 | 3.692 → 1.857 | 2.068 |
   | `#798936` | 0.085 → 0.068 | 7.518 → 4.120 | 5.444 |
   | `#879A45` | 0.073 → 0.077 | 8.736 → 4.978 | 6.738 |
   | `#9CA754` | 0.064 → 0.086 | 10.047 → 5.815 | 8.069 |
   | `#59490A` | 0.125 → 0.033 | 4.078 → 2.086 | 2.382 |
   | `#465208` | 0.126 → 0.035 | 4.228 → 2.137 | 2.470 |
   | `#2A2D01` | 0.161 → 0.014 | 2.799 → 1.424 | 1.474 |
   | `#AEBC65` | 0.050 → 0.096 | 11.877 → 7.182 | 10.180 |
   | `#C2D076` | 0.039 → 0.106 | 13.958 → 8.701 | 12.577 |
   | `#666319` | 0.108 → 0.047 | 5.281 → 2.760 | 3.363 |
   | `#323701` | 0.150 → 0.020 | 3.131 → 1.575 | 1.682 |
   | `#4B3A04` | 0.138 → 0.024 | 3.439 → 1.745 | 1.906 |
   | `#A49047` | 0.071 → 0.076 | 8.660 → 4.936 | 6.658 |
   | `#7F752A` | 0.094 → 0.060 | 6.535 → 3.491 | 4.480 |
   | `#020302` | 0.236 → 0.045 | 1.712 → 1.052 | 1.016 |
   | `#BEA964` | 0.055 → 0.091 | 10.890 → 6.457 | 9.048 |
   | `#2E5CFA` | 0.093 → 0.073 | 5.741 → 3.141 | 4.027 |
   | `#446835` | 0.111 → 0.045 | 5.168 → 2.705 | 3.277 |
   | `#5C7D4A` | 0.095 → 0.060 | 6.523 → 3.481 | 4.487 |
   | `#173E48` | 0.144 → 0.021 | 3.335 → 1.681 | 1.819 |
   | `#3C512B` | 0.129 → 0.034 | 4.159 → 2.090 | 2.403 |
   | `#1F342E` | 0.157 → 0.013 | 3.013 → 1.515 | 1.588 |
   | `#22201C` | 0.175 → **0.001** | 2.481 → 1.292 | 1.291 |
   | `#537342` | 0.102 → 0.054 | 5.885 → 3.093 | 3.898 |

   The composite sanity line `assertEquals("#333333", hex(composite(0xFFFFFFFF, 0xFF000000, 0.2)))` is
   a check of the helper with literal white, not of the palette, and was left as it was.
3. `(d) night roles keep their measured separation from each other`, night `offlineRegion` pin:
   `Pin("offlineRegion", a = null, c = null, d = 0.099), // (d) -0.010` became
   `Pin("offlineRegion", a = null, c = null, d = 0.360), // (d) +2.600`.

Comments updated with them: the class doc's shortfall list (the offline region's half of the board's
(d) pair is now history, and the owner's pick is named), the night pin's comment, and the as-drawn
list's KDoc (now `#202020`, with its shortfalls: under 0.02 ΔE on `#2A2D01`, `#1F342E` and `#22201C`;
casing under 3:1 over the composite on 14 of 26 clusters, lowest `#020302` at 1.052).

**Predicted failure at `#FFFFFF`:** these three tests fail, and nothing else in the class; the as-drawn
test with 52 lines (26 ΔE, 26 casing over region, 0 casing over ground).

**Seen** (`MapPaletteTest` alone, results directory cleared, `LC_ALL=C.UTF-8`, build log 0 `e: ` lines,
JUnit XML timestamp 12:39:37Z): 11 tests, 3 failed, 0 errors.

- `every role holds the owner's colour, day and night`:
  `org.junit.ComparisonFailure: NIGHT.offlineRegion expected:<#[202020]> but was:<#[FFFFFF]>`
- `(d) night roles keep their measured separation from each other`:
  `Role separation (d): NIGHT.offlineRegion is 0.0994 from searchCentre, pinned at 0.360`
- `night offline region as drawn …`: `Night offline region as drawn, fill #FFFFFF at
  0.20000000298023224:` then 52 lines, 26 ΔE, 26 casing over region, 0 casing over ground; the first,
  `ground #4E6012 (region #718041): ΔE is 0.1151, pinned at 0.044`.

It matched the prediction exactly.

## What landed

| SHA | What |
|---|---|
| `2d1d783` | Tests first: the pins above, `MapPaletteTest.kt` only |
| `bce4a16` | Merge of `origin/journal-redesign` (the other coder's T1/T2 work) after a rejected push; no conflict |
| `b3e2f1c` | `ui/theme/MapPalette.kt:111`: `offlineRegion = 0xFF202020.toInt()`, with a comment above it (`:101-110`) recording the owner's four answers, the three candidates and the capture record |
| this report's commit | This file |

`MapPaletteTest` at `b3e2f1c` (results cleared, 0 `e: ` lines, XML 12:50:24Z): 11 tests, 0 failed.

No other production value, the opacity, the outline, the day palette or any other role changed.

## Revert check

Runner rules from `CLAUDE.md` ("A reverted-variant check is only evidence if the reverted build actually
ran"):

- Saved a copy of `MapPalette.kt` to `app/build/night-region/MapPalette.saved.kt` before editing.
- One-line edit: `offlineRegion = 0xFF202020.toInt()` back to `0xFFFFFFFF.toInt()` (`git diff --stat`:
  one file, one line).
- Cleared `app/build/test-results/testDebugUnitTest`, ran `MapPaletteTest`.
- **Build log: 0 `e: ` lines.** The XML's timestamp, 12:50:57Z, is after the forward run's 12:50:24Z,
  from a cleared directory, so it is this run's.
- Result: 11 tests, 3 failed, the same three with the same messages as the tests-first run
  (`expected:<#[202020]> but was:<#[FFFFFF]>`; `0.0994 from searchCentre, pinned at 0.360`; 53-line
  as-drawn message starting `ground #4E6012 (region #718041): ΔE is 0.1151, pinned at 0.044`). Each is
  a failure this revert can produce: all three name `#FFFFFF` or its figures.
- Restored from the saved copy (not from git); `cmp` against the copy matched; `git status` clean
  afterwards, so the file equals the committed `b3e2f1c`; `0xFF202020` present once.

## The suite

At `b3e2f1c`, results directory cleared, `LC_ALL=C.UTF-8`, `./gradlew --offline :app:testDebugUnitTest`:
exit 0, build log 0 `e: ` lines. Counts from the JUnit XML, 274 files, timestamps 12:51:15Z to 12:53:47Z:

**274 classes / 2245 tests / 0 failures / 0 errors / 24 skipped.**

That equals the planner's count at `ace13cf` (274 / 2245 / 0 / 0 / 24, record `2026-09-28-33`). The
tree also carries the other coder's `104b17a` (production files only, no tests), so the count is not
expected to move. `MapPaletteTest` changed pins but not its test count (11). The held flaky family
(`JournalPendingDeleteTest`'s album tests, `JournalTabTest`'s photo pull) did not fail this run. No
baseline suite was run for this part; the dispatch did not ask for one.

## Casing against fill, from the code's values

The dispatch `-19` asked whether the dashed outline is still distinguishable from the fill. From the
code: the fill is `#202020` at `OFFLINE_REGION_FILL_OPACITY = 0.2f` (`ui/map/layers/MapLayers.kt:153`,
`:261`); the outline is a line layer on `PaletteRole.CASING` (`:263`), which is `NIGHT.casing =
#000000` (`ui/theme/MapPalette.kt`), drawn at full opacity. The black casing over the composited
region measures 1.052:1 to 8.701:1 across the 26 night clusters (sRGB blend assumed), under 3:1 on
14. Over the darkest cluster `#22201C` the fill moves the ground by 0.001 ΔE, so there the casing is
the only mark of the edge, at 1.292:1 against the composite. These are figures, not a judgement of
visibility, which is device-only.

## Device-only

None of this was seen on a screen by this build. Once a build with `b3e2f1c` is installed on the S22:

- the region on the night Maps tab and the entry map, on Topographical and Street: whether it reads
  darker than its surroundings and whether the ground inside stays legible ("Not too dark since it can
  make it harder to read");
- **especially whether the dashed outline carries the edge over the darkest ground, where the fill
  nearly vanishes (ΔE 0.001 over `#22201C`)**, and over the other near-black clusters (`#1F342E`
  0.013, `#2A2D01` 0.014);
- markers inside the region are unchanged;
- whether MapLibre blends the fill in sRGB, as the test assumes, is unverified; in linear light the
  drawn colours differ from the pins.

The candidate shots of `#202020` (`202020-A-night.png`, `202020-B.png`, `202020-C.png`, outside the
repository) came from APK `candidate-202020.apk`, built from `75c050f` plus the same one-value edit.
The committed build is a later tree (other coders' work has landed), so those shots show the colour,
not this build.

## Decisions I made

- **No record steps.** My agent definition asks for a sweep, intent/continuation entries, a terminal
  and a backup. `.claude/kit.json`, `check_record.py` and `check_prompts.py` do not exist in this tree,
  and the dispatch chain says the planner writes the record. I followed the dispatch and wrote nothing
  in `RECORD.md`.
- **The comment's form.** The dispatch says the value's "doc comment" records the owner's words; the
  palette's roles carry line comments, not KDoc, inside the `NIGHT` constructor call (the sighting dot
  at `:112`). I wrote a line comment above `offlineRegion`, in that style, and left the class KDoc
  alone.
- **The comment's wording.** The owner's first question is paraphrased ("Asked whether to darken it or
  lower its opacity"), not quoted; the four quotes the dispatch listed are verbatim. I added a clause
  that over the darkest ground only the dashed casing marks the edge, and that how well it does is
  unverified on a screen.
- **Updated comments beyond the three named pins,** in `MapPaletteTest.kt` only: the class doc's
  shortfall list and "The owner chooses" sentence, the night pin's comment, and the as-drawn KDoc.
  They named `#FFFFFF` and would have been false otherwise.
- **Left `searchCentre`'s night (d) pin at 0.099** (see Flags). The dispatch named only
  `offlineRegion`'s (d).
- **Left the `#333333` composite sanity line** unchanged, as a check of the helper, not the palette.
- Used a Python copy of the test's helpers to write the new pins, after checking it reproduced all 78
  old ones.
- Took the as-drawn table's "0.02 ΔE" and "3:1" as the reference points for the KDoc's shortfall list,
  because they are the thresholds the class and `-22` used; they gate nothing.
- Logs for the first run went to `/home/zynergy-labs/Zynergy/forager-wt/night-region-logs/` because
  `app/build/` did not exist yet (the planner had deleted it); I moved the log into
  `app/build/night-region/` and removed that directory.

## Flags outside scope

- **`searchCentre`'s night (d) pin is now loose.** It is pinned at 0.099, measured against the old
  `#FFFFFF` offline region. At `#202020` its nearest role is `keptTrack` at 0.1590. The ratchet still
  passes, but it would also pass if `searchCentre` moved to within 0.06 of another role, and its
  comment (`MapPaletteTest.kt`, "Board-recorded shortfall: (a) 0.137 against Topo night #C2D076 and (d)
  0.099 against the offline region") and the class doc's "(d) 0.099" no longer describe the palette.
  Re-pinning it at 0.159 is one line; it was not named in the dispatch.
- **The class doc's luminance paragraph is stale.** `MapPaletteTest.kt`'s "night fills measure 1.24:1
  (centre pin) to 5.85:1 (offline region)" against the night ground's P50 luminance was measured with
  `#FFFFFF`. The P50 figure is not in the test, so I could not re-derive it.
- **The black night casing** is under 3:1 over the plain ground on 11 of 26 clusters with no fill at
  all (record `-24`, Notes). That is the casing's property and is unchanged.
- **The shots are of the candidate APK, not this build** (see Device-only).
- Carried from the capture record: `606060-B` is mis-framed; Forager is installed in the phone's Dual
  App profile (user 95) by `adb install -r`; the phone currently runs `candidate-606060.apk`, not
  `#202020`.
- The other coder's WIP commit `104b17a` ("tests-first stubs for T1 and T2, not yet compiled") came in
  with the merge `bce4a16`. It compiled in this worktree; its test outcomes are in the suite above.
