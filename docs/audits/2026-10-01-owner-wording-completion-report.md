# Dispatch 2026-09-28-364 (+ amendment -365): the owner's words stop being called a "rule" or "ruled"

Coder: Sonnet 5.5 (`claude-sonnet-5-5`), from the session's configuration, not checked against the running session.
Base: `origin/journal-redesign` at `9b1079f5` when I started; merged `ad961fa1` (records only) with `git pull --no-rebase`, merge `e0872574`, no conflict. Branch `owner-wording`, worktree `/home/zynergy-labs/Zynergy/forager-wt/trip-flag-size`. Nothing merged to `journal-redesign`.

## Landed
- `5c84592f`: the two "owner's rule" comments (-364).
- `b4f9360a`: nine "owner ruled" comments (amendment -365).
- `e0872574`: merge of `origin/journal-redesign`. Then the build log, this report and the README row.

## Each comment, before and after
**-364 (verified first: `git grep -nIiE "owner'?s rules?\b" -- app/src` showed exactly these two, no third; 0 hits afterwards)**
| Where | Before | After |
|---|---|---|
| `ui/log/CameraOverlay.kt:24` | "...is styled. Owner's rule," | "...is styled. The owner," (the quoted block untouched) |
| `ui/log/JournalTabTest.kt:364` | "The owner's rule, "Follow until you touch it":" | "The owner's words, "Follow until you touch it":" |

**-365 (all nine confirmed at the stated lines at `9b1079f5`; the verb only changed)**
| # | Where | Before | After |
|---|---|---|---|
| 1 | `AndroidManifest.xml:122` | "Withdrawn before the owner ruled:" | "...the owner decided:" |
| 2 | `domain/PathHome.kt:77` | "owner ruled the track-network candidate inside the retrace ruling" | "owner decided ..." |
| 3 | `photo/PhotoExporter.kt:28` | "the owner ruled on 2026-09-29 that" | "the owner decided on 2026-09-29 that" |
| 4 | `ui/availability/AvailabilityCompactScaffold.kt:1046` | "so that the rule reads as the owner ruled it" | "...as the owner decided it" |
| 5 | `ui/map/BasemapStyles.kt:301` | "the owner ruled that on Satellite only the markers switch" | "the owner decided that ..." |
| 6 | `ui/map/fanout/MarkerFanOutState.kt:70` | "the owner ruled the duration and not the curve" | "the owner chose the duration and not the curve" |
| 7 | `ui/map/layers/MapLayers.kt:44` | "The tap precedence the owner ruled (L0 design rulings, 3: ...)" | "...the owner chose (L0 design rulings, 3: ...)" |
| 8 | `ui/map/OfflineStyleSwapTest.kt:127` | "The owner ruled that on Satellite only the markers switch" | "The owner decided that ..." |
| 9 | `ui/availability/AvailabilityScreenMapBubblesTest.kt:483` | "which the owner has ruled is wrong" | "which the owner has said is wrong" |

**#4, "the rule":** read in place (`AvailabilityCompactScaffold.kt:1044-1046`): the comment says a conjunction is written out so that "the rule reads as the owner decided it, not as its consequence". "The rule" is the behaviour the code encodes, not a label for the owner's words, so I left it, as the amendment allows.

## The count
`git diff -U0 origin/journal-redesign -- app` (after the merge): 11 files, 22 changed lines (11 added, 11 removed). I counted by dropping every line whose first non-blank token after the `+`/`-` is `//`, `/*`, `*` or `*/`. Two lines remain: the manifest pair (`AndroidManifest.xml:122`, before and after). I recognised them as comment by reading the file: line 122 lies inside one XML comment that opens with `<!--` on line 78 and closes with `-->` on line 131, with no other delimiter between (a short Python check of the lines). So **0 non-comment lines**. The Kotlin-comment test would also have counted a code line that happens to start with `*`; none of the 20 Kotlin lines is such a line (I read them all above in the diff).

## Check
`:app:assembleDebug :app:compileDebugUnitTestKotlin`: BUILD SUCCESSFUL, 0 `e:` lines, `processDebugMainManifest` ran (log at `data/2026-10-01-owner-wording/build.log`). No test run, no full suite, no revert check: a decision of this dispatch. 2244 MB free and no Gradle process running before the build.

## Four disclosures
- **Confirmed vs inferred:** confirmed by grep, reading and the build. Inferred: that the manifest edit does not affect the merged manifest's semantics (an XML comment; the build processed it).
- **Could not determine:** nothing open.
- **Premises that were wrong:** none. The planner's first grep missed "has ruled", which the amendment already corrected; my grep of `owner (has )?ruled` found the same nine comments plus the test name.
- **Decided beyond scope:** none. Out of scope and left, as the amendment says: the noun "ruling" (about 240 lines), the test name `BackupSettingsScreenTest.kt:358` ("...the default the owner ruled"), and "which nobody ruled" at `AndroidBackupNotifier.kt:43`. I also saw other "ruled" uses that are not the owner's (for example `BackupSchedule.kt:14` "ruled default", `EntryMapFrame.kt:8` "ruled \"Fit all kept records\"", `CameraArrangement.kt:76`, `MapBubble.kt:81` "the planner ruled") and did not touch them; the first two are plausibly the owner's words, so the planner may want to ask.

Nothing to check on a device.
