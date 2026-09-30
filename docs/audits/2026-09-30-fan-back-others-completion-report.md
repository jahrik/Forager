# Fan-back-others (dispatch 2026-09-28-298): completion report

Branch `fan-back-others`, cut from `9c806ae1` (verified an ancestor of `origin/journal-redesign` `4f3a02ad`, which adds only dispatch files and RECORD entries). No device, no adb, nothing merged to a shared branch. The owner: "apply the change to the other Back cases". Rule: with a fan open and another thing open, Back closes that thing and the fan stays; the next Back folds the fan.

**Status of the final checks: see "Suite" — the post-merge suite and `assembleDebug` are marked there as run or not run.**

## Premises (read at `9c806ae1`)
- **-293's mechanism: confirmed.** `MapRenderMode.backEnabled`; `AvailabilityCompactScaffold.kt:921` set it to `!isDrawerOpen()`; it reaches `SightingsMap.kt:952` and `MarkerFanOutBackHandler`, which is not composed while it is false (`MarkerFanOutState.kt`).
- **Handlers and state, confirmed:** action menu, `pendingAction`, `pickingSearchLocation` at `AvailabilityCompactMapUi.kt:428` (state inside `CompactMapTab`; `pickingSearchLocation` is a parameter of it, owned by the scaffold as `pickingSearchLocationOnMap`); search dropdown `AvailabilityCompactScaffold.kt:423` (`showSearchDropdown`, scaffold scope); fullscreen `AvailabilityScreen.kt:1025` (hoisted; `isMapFullscreen()` is in scope at the scaffold's `:921`); suggestions `AvailabilitySearchUi.kt:974`, inside `SearchEntryBar`, so registered at the bar's first composition, before any fan.
- **Correction:** the menu and picker state is not in scope at `:921`. It lives in `CompactMapTab`, whose own `mapSlot` call (`:573`) already does `renderMode.copy(...)`. So the gate is computed in two places, each where its state lives, rather than hoisting the menu state to the scaffold (a far larger change).
- **Can each coexist with a fan?** Read from code, no test for the ones not listed as tested:
  - fullscreen: yes (the toggle is an icon-bar control; tested).
  - add-action menu, Log-a-find/trip picker: yes (icon-bar Add control; tested). The map's long-press is `{}` in the compact tab, so the Add control is the only opener.
  - Set on map picker: yes (dropdown, then "Set on map"; tested).
  - search dropdown: yes (fan opened first, then the search bar). Its scrim blocks map taps, so the reverse order cannot happen. **Excluded by the owner's ruling, below.**
  - taxon suggestions: a fan plus suggestions is reachable only through the dropdown (typing opens it), so I found no path to it with the dropdown closed. Inferred, not tested. **Not covered; same ruling.**

## Items covered, and excluded, with reasons
Covered, each with a test through the real screen, a real touch on the control, and the real dispatcher: fullscreen, the add-action menu, the pin picker opened from the menu, the dropdown's Set on map picker.
Excluded:
- **The search dropdown (and with it the suggestions): the owner ruled "Option A", drop it.** See "The dropdown finding".
- The bubble, the drawer, the Layers sheet, the navigation-exit dialog, the home exit warning: unchanged, as the dispatch said.

## The dropdown finding (harness-only observation)
With a `!showSearchDropdown` term in the gate, four tests went red: my own dropdown test and Set-on-map test, and two of -293's unchanged `AvailabilityScreenFanBackDrawerTest` tests ("a second Back, with the drawer gone, folds the fan", and the bubble-order test). Diagnosed by stack trace, not by guess: after the drawer or the dropdown closes, `AndroidComposeView.requestFocus` hands focus to the search field (`focusSearch` finds it first), and its `onFieldFocused` (`AvailabilityCompactScaffold.kt:838`) sets `showSearchDropdown = true` again, so the gate went false again and the next Back closed the dropdown instead of folding the fan. A no-fan test (fan folded first, then open the dropdown, Back, assert closed) fails the same way, so it is independent of the fan. `LeavingTheJournalFixesTest`'s F4 dropdown tests close the dropdown in their own harness; I did not find why mine differs. Two attempts to work round it in my tests (no clock advance after Back; a semantic click instead of a touch) changed nothing, so I stopped and asked, per CLAUDE.md. The owner's device report on build g4cdf840b (predates -293): "Back dismisses the keyboard, then the search drawer", so on a device the dropdown's own Back works and the first Back just hides the IME. **The refocus-reopen is observed in this Robolectric harness only.** The two workarounds were reverted to what the dispatch asked for (a real touch, the settle clock); they were not needed for the tests that remain.
The removed dropdown test was my own, unlanded; removing it is not silencing an existing test.

## Fullscreen finding
Fan open, enter fullscreen: Back leaves fullscreen and the fan stays open; the next Back folds it (tested, `AvailabilityScreenFanBackOthersTest`). Does leaving fullscreen already fold the fan? By reading, no: only a gesture camera move folds (`SightingsMap.kt:547-553`, `REASON_API_GESTURE`; programmatic moves are `REASON_API_ANIMATION`), and `onContentChanged` folds only when the fan's members change (`MapTapHandler.kt:128-137`). Not shown by a test on a real map (the test map is a stub). Whether a real map resize leaves the fan's screen-space copies sensibly placed is device-only.

## Bubble finding (not changed)
The bubble's gate (`AvailabilityCompactMapUi.kt:658`) is `!isDrawerOpen && pendingAction == null && !pickingSearchLocation && !showActionMenu`: it excludes the drawer and the menu/picker states, **not the dropdown or fullscreen**. Its handler is registered when the bubble layer composes, later than the dropdown's (scaffold) and fullscreen's (screen) handlers, so I expect a bubble to close before both. Reasoned from code, no test. For the planner to take to the owner.

## Journal entry report map (beyond the dispatch's letter, per its instruction to apply the rule where the items exist)
It has fullscreen (`CartographyEntryReportScreen.kt:308`, handler `:359`), and fullscreen can hold a fan. Added `&& !isMapFullscreen` to its `backEnabled` (one line). **No test**: no report-screen harness was built; reasoned from code. It has no add-action menu, dropdown or pickers; its Layers sheet has its own window and is unchanged.

## What landed
- `6b6f46c1`: `AvailabilityScreenFanBackOthersTest` (6 tests). Its message says "pushed failing": **that was written before any red run existed**; it was not compiled or run when pushed.
- `0322fa9a`: the fix (WIP, unbuilt when pushed). `55fae470`: test workarounds (later reverted).
- `bafa0fde`: Option A: dropdown term and test removed, helpers restored.
- `9e260a1b`: the report-map fullscreen line.
- `1dc1dc85`: merge of `origin/journal-redesign` (`git pull --no-rebase`, clean). It brought -299's code (fan-fold-snap) and other dispatches' files.
- This report and its README row (next commit).
Gate now: scaffold `backEnabled = !isDrawerOpen() && !isMapFullscreen()`; `CompactMapTab` adds `&& pendingAction == null && !pickingSearchLocation && !showActionMenu`. `MarkerFanOutBackHandler`'s KDoc names the owner's words and the exclusions. Compose-late design kept.

## Red run
At `6b6f46c1`'s main (gate unwidened): 5 of 6 failed, each at "the first Back closed X" (fullscreen, dropdown, add-action menu, Log a find picker, Set on map picker), the guard passed, 0 `e:` lines, compile ran (XML copy `/tmp/fbo-xml-red`, not committed). The Option A edit removed the dropdown test and restored two helpers; it did not change what the four remaining tests assert, so I did not re-run red for them. The guard passes before and after and is not evidence of the fix.

## Revert checks
Each from a saved copy (`/tmp/scaffold.fwd`, `/tmp/mapui.fwd`), never from git; build log checked each time (0 `e:` lines, `compileDebugUnitTestKotlin` ran, XML timestamp inside the run), one gate term at a time, class `AvailabilityScreenFanBackOthersTest`:
| Term removed | Failed, and only this | Message |
|---|---|---|
| `!isMapFullscreen()` | fullscreen test | "the first Back closed fullscreen" |
| `!showActionMenu` | add-action menu test | "the first Back closed the add-action menu" |
| `pendingAction == null` | Log a find picker test | "the first Back closed the Log a find picker" |
| `!pickingSearchLocation` | Set on map test | "the first Back closed the Set on map picker" |
After each, the forward file was restored from the saved copy and `git diff --quiet` confirmed the tree equalled the commit.

## Suite
Full `:app:testDebugUnitTest` at `bafa0fde`, before the report-map line and the merge: **3187 tests, 24 skipped (existing files), 0 failures, 0 errors**, 392 result files (XML saved at `/tmp/fbo-xml-full`). Affected classes: `FanBackOthersTest` 5/0, `FanBackDrawerTest` 4/0, `FanBubbleDismissalTest` 10/0, `MarkerFanOutHostTest` 10/0, unchanged. The owner-held flakes (`DrawerBackOverJournalTest` 6/0, `DiagnosticsPanelTest` 8/0 in that run) did not fail; no stall. `:app:assembleDebug` at the same commit: BUILD SUCCESSFUL, 0 `e:` lines.
POST-MERGE: see the last section of this file, added when the post-merge run finishes.

## Disclosures
- **Confirmed vs inferred:** confirmed by tests: four covered items and the gate terms. Inferred from code only: suggestions coverage, the bubble's order against the dropdown and fullscreen, fullscreen not folding the fan, the report-map fullscreen line, `SightingsMap`'s pass-through (unchanged since -293).
- **Could not determine:** why `LeavingTheJournalFixesTest`'s harness does not show the refocus-reopen; whether it ever happens on a device.
- **Premises that were wrong:** the menu/picker state is not in scope where `backEnabled` is computed (two places, above); my own premise that the suggestions need no term because the dropdown term covers them died with the dropdown term.
- **Decided beyond scope:** the report-map fullscreen line (the dispatch's "if they exist there, apply"); the one-line test-name and helper choices in the new test class.

## Device-only
Each item opened over an open fan, then Back, then Back again, in **portrait and landscape** (the tests are portrait only; one scaffold serves both):
1. Fullscreen: Back leaves fullscreen, fan stays open; Back folds it. Also watch whether the fan copies sit correctly after the map resizes.
2. Add-action menu (the `+` control): Back closes the menu, fan stays; Back folds.
3. Log a find / trip / waypoint pin picker from the menu: same.
4. Dropdown's Set on map picker: same.
5. Search dropdown over an open fan: **unchanged by ruling**; observe the order (expected: the first Back hides the IME, then closes the dropdown; whether the fan folds before the dropdown is today's behaviour) and whether closing the drawer or dropdown reopens it (harness-only observation so far).
6. A bubble over a fan with fullscreen or the dropdown open (the bubble finding).
7. The Journal entry report map: fan open, fullscreen, Back, Back (untested).
