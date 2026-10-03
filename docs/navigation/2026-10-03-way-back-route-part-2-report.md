# Way-back route, Part 2: the HUD follows the route (dispatch 2026-09-28-423, plan task T6)

**Status: built and pushed on `way-back-route-2`, not merged. Phone step not yet run** (it waits for the owner's word in the coder's window). The route drawn on the map, and the start's glyph changing form on approach, are T7.

**Date:** 2026-10-03 (UTC).
**Dispatch:** `prompts/preserved/2026-10-03-02.md`, read on branch `records-after-153`. The planner's rulings on the coder's three questions came by message: question 1 (i), question 3 (a), and question 2 put to the owner twice (below).
**Base:** `origin/main` at `5b856b6a`, with pull request #153 (`domain/RouteHome.kt`, T5) on it, checked against the remote.
**The owner's go:** to this session directly, "Build only, ask before phone". On "Try again": first "Same line, tappable", then, shown the measurements below, "Own line under it (Recommended)".
**Builds on:** [`2026-09-11-way-back-route-decisions.md`](2026-09-11-way-back-route-decisions.md) (D1 to D6) and [Part 1](2026-10-02-way-back-route-part-1-report.md).

App paths are relative to `app/src/main/java/com/zynergylabs/forager/app/`. Figures are read from files in `~/Zynergy/device-evidence/2026-10-03-way-back-route-2/`.

## What this is, in plain terms

While you walk back to your start, the navigation panel now follows the path you walked:

- The **arrow** points at a spot about 25 m ahead along your path, not straight at the start.
- The **large figure** is the distance home along the path.
- The **line under it** says "Straight line …", the straight-line distance to the start, as the large figure said before.
- In the **first moments** after Return, before there is a route, the large figure is a dash.
- When the **route can't be worked out**, the large figure says "Unable to calculate route" and there is no arrow. If trying again could help (you are off the path), a "⟳ Try again" line appears under it. If it can't (the track has no points), nothing is offered.
- The route is **recomputed every 5 seconds**.

## Verified before building (reported by message first, as the dispatch asked)

1. **Where the HUD's parts are composed and fed**, read at `5b856b6a`:
   - The HUD is `ui/availability/NavigationHud.kt:174`, its values from the pure `navigationReadout` (`:331`).
   - Needle: bearing from the live fix to the target (`:365`), relative to the heading (`:386`).
   - Large figure: the straight line, accuracy-aware (`:366`, `:373`).
   - Status line: "Path home …" only when the fix is fresh and not approaching (`:411-415`).
   - `pathHomeMeters` came from `TrackRecordingUiState.pathHome?.totalMeters` (`MainActivity.kt:632`), computed by the 15 s poll (`ui/track/TrackRecordingViewModel.kt` `beginPolling` `:724`, `updatePathHome` `:789-797`). Its only reader was that status text.
2. **How the 5 s job is stopped in its tests**: its own job in `viewModelScope`, cancelled with the recording (`clearRecordingState`) and when the return ends. Both ViewModel harnesses (`TrackRecordingViewModelTest.runRecordingTest`, `TrackRecordingSwipeAwayTest`) stop every recording inside the body, in a `finally`, so the job is cancelled there with the poll. Its tests advance virtual time in 5 s steps, never `advanceUntilIdle`.
3. **The first seconds after Return**: the tick's first search runs at once, on the track the poll last read and the last accuracy-gated fix. When there has been no gated fix yet, there is no route: a dash.

## The planner's and the owner's rulings

- **Question 1 (i), one route search per tick.**
  - `RouteHome.Ahead` now carries the `PathHome` it already built. That is a new field, not a change to RouteHome's rules or numbers.
  - The route tick is the one place path home comes from. `TrackRecordingUiState.pathHome` is now read from `routeHome`, so the 15 s poll runs no search of its own.
  - **Accepted change:** with the walker in the far band, `pathHome` is now `null` where before it was filled. Nothing reads it there.
  - **Said plainly:** `pathHome` now has no production reader at all, since the HUD reads `routeHome` itself. It is kept as a derived property so the existing path-home ViewModel test still reads it, unedited and passing.
- **Question 3 (a):** a dash in the large slot until the first route result. "Unable" would claim a failure that has not happened.
- **Question 2, "Try again", twice:**
  - The owner first chose the same line, tappable.
  - Built that way and measured with real text widths, "Try again" fitted whole but the owner's sentence did not. It showed 12 of its 25 characters at 360 dp ("Unable to ca… · ⟳ Try again"), 16 at 384 dp (the S22) and 20 at 412 dp. Alone, the sentence fits at all three.
  - Shown those figures, the owner chose "Own line under it (Recommended)", which is what is built.
- **"Returning means route mode"**, the planner's ruling on a point the coder raised:
  - `AvailabilityScreen`'s `returnRoute` is not nullable; it defaults to `ReturnRoute.Pending`. No caller can show a return in the old straight-line mode by leaving it out.
  - The straight-line mode stays in the HUD for navigating to a waypoint (D2). It is held by a test named for that mode, and nothing passes it that way today.
- **"Approaching" is left as it is.** It still owns the status line inside the approach threshold (T7 moves it onto the start's glyph), and is still measured against the start itself.

## What was built

- **`domain/RouteHome.kt`**: `Ahead.pathHome`, and the doc's "Who calls it" now names its caller.
- **`ui/track/TrackRecordingViewModel.kt`**:
  - **The route tick:** `beginRouteTicks`, `endRouteTicks` and `updateRouteHome`, plus `ROUTE_TICK_MILLIS = 5_000L` (D4). One search at once, then one every 5 s.
  - **What feeds it:** the track the 15 s poll last read (`lastPolledTrack`) and the last accuracy-gated fix. The hop band is carried from the last result, a withheld one included, as `RouteHome.Withheld.hopBand` asks.
  - **`retryRoute()`** for "Try again": one search, at once.
  - **The tick follows the screen's returning flag** (`copyFromWatch` starts and ends it), whichever way the return reached the screen.
    - The first version started it only from this screen's own Return. That would have left a return taken up after a reopen (Recording > swipe away > open again) with a dash for good.
    - Caught before building, and covered by a test.
  - **The search is a constructor parameter** (`findRouteHome`, `::routeHome` by default), so a test can count searches.
- **`ui/track/TrackRecordingUiState.kt`**: `routeHome` replaces `pathHome` as state; `pathHome` is derived from it.
- **`ui/availability/NavigationHud.kt`**:
  - **`ReturnRoute`** (`Pending`, `Ahead`, `Unavailable(canRetry)`) and `returnRouteOf(RouteHome?)`. Off the route offers "Try again"; no usable points does not.
  - **The readout:**
    - The needle aims at the lookahead.
    - The large slot holds the route figure in plain formatting (a sum over stored points, not one fix's radius), the dash, or the owner's sentence.
    - The status line's empty state carries "Straight line …" with the accuracy-aware formatting the straight line always had. It yields to "Approaching", a stale fix and a lost fix, as "Path home" did.
    - A pending or withheld route has no needle. That is a fifth withholding term, ranked last, below the four the readout already ordered.
  - **`RouteRetryRow`:**
    - "⟳ Try again" on its own row under the message, laid out at least 48 dp tall, in the primary colour, underlined, behind a refresh icon.
    - Announced as a button whose action is "Try again".
    - Drawn only when recomputing could change the answer.
- **The plumbing:** `AvailabilityScreen` → `AvailabilityCompactScaffold` → `AvailabilityCompactMapUi` → the HUD, with `onRetryRoute`. In `MainActivity`, `returnRoute = returnRouteOf(trackUiState.routeHome)` replaces `pathHomeMeters`.

## Measured, not assumed

- **The HUD's height:**
  - It is the same on the route, pending, and withheld with no usable points.
  - With "Try again" it is 40 dp taller (80 to 120 dp at 360 dp), not the row's full 48 dp: the top row was already 48 dp tall for the exit button.
  - No new surface is drawn over the map; the HUD's own 80% fill is unchanged.
- **The sentence:** "Unable to calculate route" shows whole, all 25 characters and not ellipsised, at 360, 384 and 412 dp, with and without "Try again" (about 184 dp needed, 184 to 185 dp given).
- **A measuring trap, recorded:** without Robolectric's native graphics, text measured a few dp wide (a heading label at 3.5 dp). The screen test runs with `@GraphicsMode(NATIVE)`, as `AvailabilityScreenLandscapeB2Test` does.
  - `TextLayoutResult.didOverflowWidth` reads true for any text laid out with `softWrap = false`, even with the box wider than the text. The width test checks the box against the text's intrinsic width, and the ellipsis, instead.
- **Unmeasured:** the S26 Ultra's width in dp.

## Tests

**A deviation, recorded as the planner accepted it:** the tests were written alongside the code, not pushed failing first. Each new behaviour has a revert check instead (below), each failing for its own reason.

New tests:
- **`NavigationHudReadoutTest`:**
  - The needle aims at the lookahead.
  - Pending.
  - Withheld off the route, with "Try again".
  - Withheld for no usable points, with none.
  - A lost fix with every route.
  - A stale fix de-emphasises the route figure.
  - `returnRouteOf` over real `routeHome` searches.
  - The straight-line mode, named for navigating to a waypoint.
- **`AvailabilityScreenReturnRouteTest`**, new, on the real screen:
  - On the route: the route figure, the straight line and the turn to the lookahead.
  - Pending.
  - Withheld.
  - The HUD one row taller only with "Try again".
  - "Try again" by real touches at five points across its row, with a touch on the message above not reaching it.
  - One button, labelled "Try again", at least 48 dp tall.
  - No "Try again", and no tap target, for no usable points.
  - The whole sentence at three widths.
  - Real long-presses on the map below the HUD, across its width, reaching the map, in the taller state and the usual one.
- **`TrackRecordingViewModelTest`:**
  - One search at Return and one every 5 s, none in the poll, none after the return stops, and path home the same object as the search's.
  - "Try again" searches at once, and does nothing when not returning.
  - Off the route and back through fixes, with the hop band carried between ticks.
- **`TrackRecordingSwipeAwayTest`:** a return taken up by a reopened screen gets the route without a second Return tap.

Existing tests changed, each named in the dispatch or by the planner's ruling:
- **`NavigationHudReadoutTest`,** the two path-home tests, which the dispatch names:
  - "path home fills the status line's empty state…" asserted "Path home 0.9 mi" (and km, ft, m) in the status line and "0.7 mi" in the large slot. Now "the route figure fills the large slot…", asserting the same figures in the large slot and "Straight line 0.7 mi" / "1.1 km" in the status line.
  - "path home yields to approaching, to a stale fix and to a lost fix" asserted those three messages with a path-home figure. Now "the straight line yields to…", with the same three messages over a route, and "" with no route.
  - The helper's seventh parameter went from `pathHomeMeters` to `route`.
- **`AvailabilityScreenMapIconStackTest`,** by the "returning means route mode" ruling:
  - Its return tests are fed a route as production feeds one: 1,500 m along the path, with the lookahead due north like the origin, so the turns it pins are unchanged.
  - Its `setScreen` gained `returnRoute` (default `Pending`).
  - Each changed assertion, before → after:
    - "the HUD shows the straight-line distance to the origin in the display unit and the turn to it": large slot "1.1 km" → "1.5 km", plus "Straight line 1.1 km" in the status line. Renamed "while returning the HUD shows the route distance and the straight line in the display unit, and the turn".
    - "an untrusted heading on the HUD…": large slot "1.1 km" → "1.5 km".
    - "with no compass sensor…": "1.1 km" → "1.5 km".
    - "inside the approach threshold…": "within 13 m", counted once → "12 m" (the route from 10 m away), counted once, and "within 13 m" counted zero.
    - "a fix worse than 50 m does not reach the HUD…": large slot "1.1 km" twice → status line "Straight line 1.1 km" twice; the "≈ 600 m" count now matches as a substring.
- Nothing skipped, silenced or weakened. The 19 skipped in the touched classes are `AvailabilityScreenMapIconStackTest`'s existing `@Ignore`s (19 counted in the file); none was added or removed.

### Revert checks

Run by `revert.sh`:
- It edits from a saved copy, never from git.
- It reads the build log for compile errors before citing any result.
- It confirms the tree is identical to HEAD (`07d458af`) after each check.

Each of the 17 compiled with 0 errors, and each failure names its own edit (`reverts-out.txt`, and an XML folder per check).

| Check | One edit | Fails with |
|---|---|---|
| r01 | needle at the start, not the lookahead | "Turn 45°" was "Turn 315°" (readout, screen) |
| r02 | large slot shows the straight line | "1.5 km" was "1.1 km"; "0.9 mi" was "0.7 mi"; "12 m" was "within 13 m" (7 tests) |
| r03 | status line empty | "Straight line …" was "" (8 tests) |
| r04 | pending shows the straight line | "—" was "0.7 mi" |
| r05 | withheld shows the straight line | "Unable to calculate route" was "0.7 mi"; whole-sentence tests read 6 characters |
| r06 | needle drawn while pending or withheld | target text "Turn 315°", arrow 315.0 where null |
| r07 | "Try again" offered for no usable points | a retry node where none; HUD 120 dp where 80 |
| r08 | "Try again" row not drawn | the retry node not found (3 tests) |
| r09 | row not held to 48 dp | "the row is 20.0.dp tall" |
| r10 | row's click not wired | "touch 0 … must reach Try again expected:<1> but was:<0>" |
| r11 | mapping always offers a retry | `Unavailable(canRetry=false)` was `true` |
| r12 | the poll searches as well | "one search at Return expected:<1> but was:<2>" |
| r13 | tick every 15 s, not 5 s | searches 1 where 2; the taken-up return still `null` at 5 s; the band test's cast |
| r14 | "Try again" does nothing | "at once … expected:<2> but was:<1>" |
| r15 | hop band not carried from a withheld result | 48 m read as a route (ClassCastException at the cast to `Withheld`) |
| r16 | tick started only by this screen's Return (two edits) | "the reopened screen has a route home, not null" |
| r17 | `pathHome` a copy, not the search's own | "path home is the route search's own figure expected same" |

The revert checks have one gap: `MainActivity`'s one line, `returnRoute = returnRouteOf(trackUiState.routeHome)`, has no test. Neither did the path-home line it replaces.

## Suite

| Run | Tree | Result |
|---|---|---|
| Touched classes (`t2-built`) | `07d458af` | 6 classes, 212 tests, 0 failures, 19 skipped (the icon-stack class's existing `@Ignore`s) |
| Full unit suite (`t3-full-suite`) | `07d458af`, clean tree | 423 classes, 3484 tests, 0 failures, 0 errors, 24 skipped |
| `assembleDebug` (`t4-assemble`) | `07d458af` | BUILD SUCCESSFUL, `app-debug.apk` built |

**Reconciled with Part 1's suite, not assumed.**
- Part 1 had 422 classes and 3,461 tests at `840e8626`.
- This adds one class (`AvailabilityScreenReturnRouteTest`) and 23 `@Test`s, counted per file against `origin/main`:
  - `AvailabilityScreenReturnRouteTest` 0 → 11
  - `NavigationHudReadoutTest` 22 → 30
  - `TrackRecordingViewModelTest` 31 → 34
  - `TrackRecordingSwipeAwayTest` 20 → 21
  - `AvailabilityScreenMapIconStackTest` 104 → 104
- 3,461 + 23 = 3,484. The 24 skipped match Part 1's 24.

## Not done, and why

- **The phone step.** It waits for the owner's word in the coder's window.
- **The needle following a path** needs a walk, and that is the owner's.
- **Unchanged:**
  - The two cases Part 1 kept visible for a walk: the lookahead behind a walker who rounded a bend inside the lag, and a walker beside the route pointed up the path.
  - Part 1's finding 3: the off-track alert and the route's "off" can disagree. That is T21.
- **Runs every 5 s now, not every 15 s:** the route search on the main dispatcher. It was measured at about 19 ms on a desktop JVM at the four-hour cap; there is no device figure.
