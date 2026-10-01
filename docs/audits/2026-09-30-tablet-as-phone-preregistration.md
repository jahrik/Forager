# Tablet as phone: pre-registration (dispatch 2026-09-28-245, amended by -246)

Written and pushed before any production edit. Base: `5bad4f5b` (origin/journal-redesign). Coder: Sonnet 5.5 (`claude-sonnet-5-5`, read from the session's system prompt).

## Premises read at the base

| # | Premise (dispatch) | At the base |
|---|---|---|
| 1 | Four tree-choice sites in `AvailabilityScreen.kt` (~:1187, :1211, :1305, :1882) | Confirmed at `AvailabilityScreen.kt:1187`, `:1211`, `:1305`, `:1882` (all `windowWidthClass == COMPACT \|\| isShortWindow`). |
| 2 | `SHORT_WINDOW_MAX_HEIGHT_DP = 480` at `ShortWindow.kt:14` | Confirmed, `ShortWindow.kt:14`. Premise 2's acceptance of a portrait arrangement at landscape tablets is withdrawn by -246. |
| 3 | J6's cluster stays | Not contradicted; the cluster is the phone's own. |
| 4 | Removal by hand | Followed. |
| 5 | No migration / DataStore / backup change | Not contradicted at the base by any file this change touches. |
| item 2 | `WindowWidthClass` goes "if nothing else reads it"; SeasonalTab's 640 dp cap goes with it | **WRONG.** `AvailabilityResultsUi.kt:245-251` (`SeasonalTab`) reads it, and it is a phone path: a phone in landscape is `w823dp`, so `MEDIUM`, and gets the 640 dp cap. `AvailabilityScreenLandscapeB3DestinationsTest.kt:185` asserts that cap on the phone, and `AvailabilityCompactScaffold.kt:775` uses `READABLE_CONTENT_MAX_WIDTH` itself. Removing it changes a phone outcome (item 4 forbids). **Left in place; reported for the planner.** |
| -246 item 2 | `AvailabilityCompactScaffold.kt` ~:896, :930 are readers | Stale. The scaffold's only readers are `:179` (parameter), `:550` (`showRail`) and `:930` (`railPortEdge = if (showRail) portEdge else null`). |

## Readers of `isShortWindow` / `isShortLandscapeWindow` (amendment item 2), and what each does at w1318dp-h824dp-land after the change

| Reader | Today | After |
|---|---|---|
| `AvailabilityScreen.kt:1187,1211,1305,1882` tree choice | short or COMPACT picks phone tree | gone: the phone tree always |
| `AvailabilityScreen.kt:1218` `isShortLandscapeWindow` | short and landscape | landscape (renamed `isLandscapeWindow`); true at the landscape tablet |
| `AvailabilityCompactScaffold.kt:550` `showRail` | the parameter | rail shown, bottom nav not; L via `:930` (`railPortEdge`) |
| `AvailabilityScreen.kt:1910` drawer direction | short landscape flips the drawer to the port edge | landscape flips it |
| `AvailabilityOfflineMapsUi.kt:297` picker side by side | short and landscape | landscape |
| `JournalShortWindow.kt:64` `isShortLandscapeJournal` (readers `JournalTab.kt:593`, `RecordsTab.kt:380`) | short and landscape | landscape |
| `JournalTab.kt:694` entry columns | short (two columns) | short or landscape (two columns) |
| `CartographyEntryReportScreen.kt:376` map height cap | short: a height budget, 40% of `screenHeightDp` | **unchanged**: its meaning is a height budget, not landscape (its own doc comment, `:747`). At h824 the cap does not apply, as today for any tall window. Reported, not changed. |

## Predictions for the new class `AvailabilityScreenTabletAsPhoneTest` (12 tests)

At the base (nothing built):
- FAIL, the wide tree is drawn: `a portrait tablet renders the phone tree` (no `compact-bottom-nav`), `a 600 to 839 dp wide window renders the phone tree`, `on a portrait tablet a real touch on a bottom nav tab switches the tab`, and every `landscape tablet` test (no rail, no L, tab row and permanent drawer instead).
- PASS: `control - a phone in landscape still shows the rail and the L` (`w823dp-h384dp-land`). It must pass before and after; it exists so the L and rail lookups are shown able to find them.
- After the build: all 12 pass.

Reverting checks (from saved copies, never git): (a) put the width test back at `AvailabilityScreen.kt` (tree choice) and expect the portrait guards to fail on the missing `compact-bottom-nav`; (b) put the landscape gate back to short-and-landscape and expect the landscape-tablet tests to fail on the missing `compact-navigation-rail`.

## Deleted tests

Not predicted per class. After the production deletion the full suite is run, every failure is read, and a class or method is deleted only if its subject is a deleted symbol; each is listed in the completion report with the symbol it covered. A failure whose subject is a phone path stops the work.

(Correction to the section above: the new class `AvailabilityScreenTabletAsPhoneTest` has 10 tests, not 12; the count in the prediction was wrong, the list of what fails at the base is unchanged.)

## Part B: A1 item 1, the L after a fullscreen cycle (pre-registered before the fix)

Read, not run:
- The L's top limit is `topLimitPx = topInset` (`AvailabilityCompactMapUi.kt:641`, landscape branch), where `topInset` is `safeAnimatedTopInset` (`AvailabilityCompactScaffold.kt:387-392`, `:974`): a spring animation of `searchBarHeight`, toward `0.dp` while fullscreen and back to `searchBarHeight` when it ends.
- The cluster re-clamps its displayed offset in the `LaunchedEffect` at `AvailabilityMapIconCluster.kt:495`, keyed on `clusterHeightPx`, `mapContentBoxHeightPx`, `bottomNavHeightPx`, `isFullscreen`, `landscape`, `legendBoundPx`, `currentNoticeBottomPx`. **`topLimitPx` is not a key**; the clamp reads it through `rememberUpdatedState` (`:354`).
- The L's offset memory (`userChosenOffsetPx`) is 0; at rest the limit pushes the displayed L down to it (the centred top, 44 dp in a 384 dp window, is above the search bar's bottom).

Prediction (mechanism): on exit `isFullscreen` flips and the effect runs at once, with the animated limit still near 0, so the clamp of the memory 0 is 0 and the L glides to its centred top; the limit then animates back to `searchBarHeight` but nothing re-runs the effect, so the L stays at the centred top, above the search bar's bottom. This fits every observation in the relay (record `4bd3be31`, "A1 item 1 reproduction"): 247 px at rest and while in fullscreen (the effect ran on entry while the limit was still `searchBarHeight`, and the limit's animation to 0 re-ran nothing), 200 px after exit (the centred position, 47 px up, about 16.7 dp), Back the same as the button, no later re-layout (8 s), and a rotation restoring it (`landscape` and the edges are keys, so the effect re-runs with the settled limit).

Under Robolectric this reproduces through the real entry point (a coordinate touch on the Fullscreen row and on the Exit fullscreen row) because it needs no window insets: the window is 384 dp tall and `searchBarHeight` is a measured text height. Pass condition after the fix, in `LandscapeLFullscreenCycleTest` (3 tests, rotations 90 and 270 and a Back-key exit): after the cycle the L's top is not above the search bar's bottom and equals its rest value within 1 dp. Predicted at the current code: all 3 fail on the "after a fullscreen cycle" assertion with the L's top about 44 dp against a search bar bottom above it. If they do not fail, the mechanism above is wrong and the fix is not written; the next step is data (a frame log of the limit and the target).

Not reproducible under Robolectric, device-only: the 47 px figure itself (it is the S22's density and real insets); whether the map's shift between the captures is related (the relay says the cause was not isolated).
