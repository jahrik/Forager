# Fan-back-drawer (dispatch 2026-09-28-293): completion report

Branch `fan-back-drawer`, cut from `4a250dc9` (verified: it is the parent of `origin/journal-redesign` `c6c83723`, which adds only the dispatch file and its intent). `origin/journal-redesign` merged with `git pull --no-rebase` at the end (clean, merge commit `1b1730c0`). No device, no adb, nothing merged to a shared branch.

The owner: "When a fan is spread out and I call the tool panel, hitting the back button closes the fan instead of the tool panel, when it's expected to close the tool panel."

## Verified premises (all read at `4a250dc9`)
- **Drawer's Back: confirmed.** `BackHandler(enabled = isDrawerOpen) { isDrawerOpen = false }` at `AvailabilityScreen.kt:1022`, comment `:1018-1021`.
- **Fan's Back: confirmed.** `MarkerFanOutBackHandler` at `MarkerFanOutState.kt:102-107` (before my edit), composed only `if (state.isOpen && !bubbleOpen)`; called at `SightingsMap.kt:950`. No drawer gate. The mechanism (composed after the drawer's handler, so asked first) is inferred from the existing KDoc and from the red run below, which failed on exactly that: the first Back folded the fan and left the drawer open.
- **Bubble's gate: confirmed, with a correction.** `MapBubble.kt:263` is `BackHandler(enabled = backEnabled && tapped != null)`. `AvailabilityCompactMapUi.kt:658` passes `backEnabled = !isDrawerOpen && pendingAction == null && !pickingSearchLocation && !showActionMenu` **to the map's bubble layer**. **Corrected:** `AvailabilityCompactScaffold.kt:1204` (`backEnabled = !isDrawerOpen()`) is **not** a map call site; it feeds the **Journal tab** (`JournalTab`'s `backEnabled`, intent 2026-09-28-28). The map gets its drawer state at `:919` (`mapRenderMode.copy(...)`), where `isDrawerOpen()` is in scope.
- **Every map that can show a fan with a drawer able to open over it.** `SightingsMap` has exactly one production caller, `SightingsMapSlot` (`MapSlot.kt:500`), reached through `MapRenderMode`. Its `mapSlot(...)` callers: 
  1. **Compact Maps tab**, `AvailabilityCompactMapUi.kt:552`, `renderMode` from `AvailabilityCompactScaffold.kt:919`. Drawer: yes. Fixed and tested.
  2. **Journal entry report map**, `CartographyEntryReportScreen.kt:~488` (inside the Journal tab; the drawer opens over any tab). Drawer: yes. Fixed, **not tested** (see below).
  3. `CentrePinLocationPicker.kt:204`: no drawer over it reachable that the picker does not sit under; it passes the default `backEnabled = true`, unchanged. Not reasoned further.
  - **Wide/landscape:** there is no separate wide layout in `AvailabilityScreen`. One `CompactMainScaffold` (`AvailabilityScreen.kt:1295`) serves every window and takes `isLandscapeWindow`; the drawer handler and the map's `renderMode` are the same ones. The landscape case is therefore covered by the same code, but the tests run only at `w384dp-h823dp` (portrait). Landscape is device-only.

## Mechanism chosen, and what was rejected
- `MarkerFanOutBackHandler(state, bubbleOpen, backEnabled = true)`: the handler is still not composed when `!backEnabled`, exactly as it is not composed while `bubbleOpen`. `SightingsMap` gets a `backEnabled` parameter; it arrives through a new `MapRenderMode.backEnabled` (default `true`), set at `AvailabilityCompactScaffold.kt:919` to `!isDrawerOpen()` and at the report screen to its own `backEnabled`.
- **Why `MapRenderMode`, not a `MapSlot` parameter:** `MapSlot` is a typealias lambda whose parameter count already crashed the Compose compiler at 10 (see the doc comment at `MapSlot.kt` above `SightingsMapSlot`); `MapRenderMode` is a data class already `.copy`'d at that exact call site, and already carries other per-call-site flags.
- **Rejected: an always-composed `BackHandler(enabled = ...)`**, per the dispatch and the KDoc at `MarkerFanOutState.kt:89-101`: it would be registered at first composition and lose to every handler composed since.
- **Rejected: closing the fan's handler off by reading drawer state inside `SightingsMap`**: the map would need to know about the drawer; a flag it is handed keeps it ignorant, as the bubble's is.
- KDoc on `MarkerFanOutBackHandler` updated with the drawer exception and the owner's words.

## Decided beyond scope (disclosed)
The entry-report map (surface 2) is fixed too, one line (`backEnabled = backEnabled`). The dispatch's own sentence "every map that can show a fan while a drawer can open over it" and "at each call site" read as covering it; if that was not meant, revert that one hunk. **It has no test.** I did not build a report-screen harness; it is reasoned from code only, and on the real `SightingsMap` the pass-through is unverified by any test (below).

## What the tests reach, and do not
`AvailabilityScreenFanBackDrawerTest` (4 tests) runs the real `AvailabilityScreen`, the real Tools control (a touch on the "Tools" node), a real touch to open the fan, Back through the real activity dispatcher. The map is the same stub `AvailabilityScreenFanBubbleDismissalTest` uses, and it carries **its own copy** of the `SightingsMap` line that hands `backEnabled` to the handler. So what is proven: the screen sets `renderMode.backEnabled` from the drawer, and a handler gated on it yields the right order. **Not proven:** that `SightingsMap` forwards `backEnabled` to `MarkerFanOutBackHandler` (`SightingsMap.kt:952`, read, not tested) or `SightingsMapSlot` forwards it (`MapSlot.kt`, read, not tested). The same limitation the existing test's KDoc states.

## What landed
- `48e3bc20`: plumbing (`backEnabled` on the handler, `SightingsMap`, `MapRenderMode`, `SightingsMapSlot`) **unwired** plus `AvailabilityScreenFanBackDrawerTest`. **Pushed failing.** Run: 2 of 4 failed, `a second Back, with the drawer gone, folds the fan` (first Back had folded the fan: `assertTrue(fan.isOpen)` after it) and `with a fan open and the Tools drawer open, Back closes the drawer and the fan stays open` (`Trip Planner` still displayed: the drawer stayed open). The other two (no drawer: Back folds the fan; bubble over fan: Back order) are guards that pass before and after; the bubble one passes before because the bubble already carries the drawer gate. Flagged: those two are **not** evidence of the fix.
- `7bc8e26f`: the fix (`AvailabilityCompactScaffold.kt:919` `backEnabled = !isDrawerOpen()`; report screen hunk).
- `1b1730c0`: merge of `origin/journal-redesign`.

## Revert check
Saved `AvailabilityCompactScaffold.kt` to `/tmp/scaffold.saved`, edited `backEnabled = !isDrawerOpen()` to `backEnabled = true` (one line), ran the class. **Build log checked: 0 `e:` lines, `compileDebugKotlin` and `compileDebugUnitTestKotlin` both ran** (XML timestamp 18:31:00 against a wall clock of 18:31:18). Result: the same 2 tests failed with messages specific to the edit (`Trip Planner ... is displayed` = the drawer stayed open; `fan.isOpen` false after the first Back). Restored from the saved copy (not from git); confirmed afterwards: `git status` clean and `backEnabled = !isDrawerOpen()` present at the scaffold. The forward change was committed, so both the copy and git agree.

## Suite
Full `:app:testDebugUnitTest`, read from the JUnit XML (copy saved at `/tmp/fbd-xml-full/`, taken before the revert run overwrote it): **3177 tests, 24 skipped, 0 failures, 0 errors**, 391 result files. The 24 skips are in existing files (`AvailabilityScreenOfflineCacheTest`, `...WaypointFlowTest`, `...MapIconStackTest`, `...TripPlanningFlowTest`, `GenerateFungiIndexDbAsset`); I did not touch them. Affected classes: `AvailabilityScreenFanBackDrawerTest` 4/0 failures, `AvailabilityScreenFanBubbleDismissalTest` 10/0, `MarkerFanOutHostTest` 10/0 (timestamps fresh, from this run). No stall, no `DiagnosticsPanelTest` failure seen. `:app:assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines. The -291 coder did not hold a Gradle build at any check (no wrapper or test executor process listed before each run).

## Other Back-order findings (report only, nothing changed)
Registration order decides, not enabling order: a handler composed later is asked first, and an always-composed `BackHandler(enabled = x)` keeps its first-composition slot. The fan's handler is composed when it opens, so it sits **after** every handler already composed. "Fan first" below means a Back with both open closes the fan and leaves the other open. None of these was shown by a test; all are **reasoned from code**.

| Thing | Where | Closes first with a fan open | Basis |
|---|---|---|---|
| bubble | `MapBubble.kt:263`; fan gated by `bubbleOpen` | **bubble**, then fan | shown by `AvailabilityScreenFanBubbleDismissalTest` |
| Tools drawer | `AvailabilityScreen.kt:1022` | **drawer** (this dispatch) | shown by the new test |
| `pendingAction` / `pickingSearchLocation` / `showActionMenu` | `AvailabilityCompactMapUi.kt:428` (always composed, before the fan) | **fan**, then these | reasoned from code |
| search dropdown | `AvailabilityCompactScaffold.kt:423` (always composed) | **fan**, then the dropdown | reasoned from code |
| taxon suggestions list | `AvailabilitySearchUi.kt:974`, composed after its `ExposedDropdownMenuBox` | **fan** if the list composed before the fan opened, otherwise the list if it composed after | reasoned from code; order depends on whether that composable is already in the tree |
| fullscreen | `AvailabilityScreen.kt:1025` (always composed) | **fan**, then fullscreen exits | reasoned from code |
| compact tab to Maps | `:1029` | n/a: the map is not showing on another tab | reasoned from code |
| navigation exit prompt | `:1065`, plus an `AlertDialog` (own window) | the dialog, since a dialog window takes Back before the activity dispatcher | reasoned from code; its own comment at `:1057-1063` says the dialog path is device-only |
| home "tap Back again to exit" | `:1083` | **fan** (exit warning is last) | reasoned from code |
| Layers sheet | `MapLayersSheet.kt:245` `ModalBottomSheet` (own window) | **sheet**, then fan | reasoned from code (a modal sheet takes Back in its own window); not exercised |

Items for the planner to take to the owner (they close the fan first): the action menu and centre-pin pickers, the search dropdown, and fullscreen. The suggestions list is ambiguous by composition timing.

## Device-only
1. Open a fan, open Tools, press Back: the drawer closes and the fan stays open; press Back again and the fan folds.
2. The same in landscape (one scaffold serves both, so it should match, but the tests are portrait only).
3. Open a fan, open a bubble over it, open Tools: Back closes the drawer, then the bubble, then the fan.
4. The Journal entry report map with a fan open: open Tools, Back closes the drawer and the fan stays (fix untested).
5. Not reachable from tests: `SightingsMap`'s own forwarding of `backEnabled` to the handler (`SightingsMap.kt:952`); items 1 and 3 are what exercise it.
