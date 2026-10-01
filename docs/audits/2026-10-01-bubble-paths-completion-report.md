# Bubble-paths completion report (dispatch 2026-09-28-387)

Branch `bubble-paths`, cut from `origin/journal-redesign` at `21582025` (not from `fan-tap`, which is withdrawn). Part A was handed back first and is merged
(`3f33903d`, merged as `303226ef`). The rest is at `64be86f8`. Every figure is read from a file in
`~/Zynergy/device-evidence/2026-10-01-bubble-paths/` (named in brackets, `tests/` unless a path says otherwise); the folder is outside the repo.

## The two paths, step by step, and the test that holds each step

**Part A (the owner, "1 yes").** `Fan A open, X's bubble over it > tap stack B > X's bubble closes, fan A closes, fan B opens`, and with no fan open
the bubble closes and the fan opens.

| Step | Held by |
|---|---|
| fan A open with X's bubble over it (as at the base: a tap on a fanned icon shows its bubble over the open fan, not changed) | `AvailabilityScreenFanBubbleDismissalTest`, helper `fanWithFindBubbleOpen` (unchanged) |
| tap on stack B | `MapTapHandlerBubbleStackTest` "fan A and a bubble over it, a tap on stack B ..." (handler, one tap); `AvailabilityScreenFanBubbleDismissalTest` "with a fan and a bubble over it, a real touch on another stack ..." (real screen, real touch) |
| X's bubble closes | the handler's one event is `closeBubble`, not `plain`; on the real screen the bubble node does not exist after the touch |
| fan A closes, fan B opens | `fan.members` are fan B's and `fan.isOpen` after the one tap (same two tests) |
| no fan open, bubble showing, tap on a stack | `MapTapHandlerBubbleStackTest` "no fan and a bubble showing ..."; the real-touch twin in the dismissal class |
| unchanged: single marker (its outcome replaces the bubble, no extra close), another icon of the open fan (its outcome, fan stays), empty map over the fan (bubble closes, fan stays, the next folds it), a stack with no bubble | the four other tests of `MapTapHandlerBubbleStackTest`, which pass before and after |

**Part B (the owner, "2 yes", "build it now").** `Fan open, X's bubble over it > tap "kept in <entry>" > journal entry opens > Back > map, with the fan and
X's bubble as they were > Back > bubble closes > Back > fan closes`.

| Step | Held by |
|---|---|
| the line's tap remembers the bubble and the open fan | `MapReturnMemoryTest` (all of the entry group); the screen tests below reach it by a real touch on `mapBubbleEntryLineTag` |
| journal entry opens | existing J8 tests, unchanged and passing (`JournalEntriesOnMapScreenTest`: "a highlighted record's bubble names its entry ... opens that entry's report") |
| Back lands on the Maps tab with X's bubble showing | `JournalEntriesOnMapScreenTest`, portrait and short landscape: find, waypoint (compact tests) and photo (`FollowUpsTest`) |
| and with the fan's members | "with a fan open under the bubble, the same Back brings the fan's members back with it" (`pendingFanKeys` equals the published keys) |
| next Back closes the bubble, on the Maps tab | "and the next Back closes that bubble, on the Maps tab" |
| next Back folds the fan | not in this path's tests: it is the existing `AvailabilityScreenFanBubbleDismissalTest` ("Back closes only the bubble and a second Back folds the fan"), unchanged |
| the Back after that reaches the exit prompt | not driven (the dispatch says never to send it on the phone; the exit handler is `AvailabilityScreen.kt:1098-1104` and is untouched) |

**The owner's whole path as one test: not possible in this harness.** The stub map has no fan of its own (the fan lives inside `SightingsMap`, which cannot be composed under
Robolectric); the fan's keys are published to the return memory by the test, as `SightingsMap` does (`SightingsMap.kt`, the `openFanKeys` writer). The steps are held
as above, by different tests; the fan's real reopening on the Maps tab (`FanReopen.kt:45`, `openFanFor`) is held by its existing tests and device-only.

## "Today", with file:line

- **Part A**, at the base `MapTapHandler.kt:72-97` (read, then pinned by test): fan A folds (`:94`), fan B opens and the handler returns at `:95` before any sink runs, so the
  bubble stays, now over fan B. Pinned: `tests/a-today/` (`a-today-build.log`): the new-behaviour tests fail on the base with `expected:<[plain]> but was:<[]>`, and
  the assertions that fan B opened passed.
- **Part B**, `~/Zynergy/device-evidence/2026-10-01-fan-tap/kept-in-back-today.md`, confirmed unchanged at this base (`git diff --stat 9cf1cfca..HEAD -- app/src/main` was empty at
  the start): `AvailabilityScreen.kt:1275-1281` (at the base) called `mapReturnMemory.forget()`; Back from the report landed on the Journal's Entries list (`CartographyScreen.kt:296`); a
  second Back went to the Maps tab with nothing open.

## What landed

| Commit | What |
|---|---|
| `6a2e9bda`, `47fea3ec` | Part A tests first, pushed failing |
| `3f33903d` | Part A. `MapTapHandler.kt:79` remembers `bubbleOpen()`; `:103` sends `sinks.onCloseBubble()` when a stack opens with a bubble up. New `MapTapSinks.onCloseBubble`, `MapRenderMode.onCloseBubble` (`MapSlot.kt`), forwarded by `SightingsMap.kt` and set in `AvailabilityCompactMapUi.kt:388` to `{ tapped = null }` |
| `34d2a6ea`, `07656693` | the planner's gap: the journal entry report's own map (`CartographyEntryReportScreen.kt`, a third `mapSlot` caller) answers `onCloseBubble` too. The picker (`CentrePinLocationPicker.kt:204`) draws no records and cannot reach the case |
| `9f9ab273` | Part B tests first, pushed failing: 16 |
| `64be86f8` | Part B. `MapReturnMemory.kt:82-125` (entry path: `rememberEntryOpen`, `onEntryClosed`, `takeEntryBubble`); `AvailabilityCompactMapUi.kt:382,664` (the wrapper and the restore); `AvailabilityScreen.kt:1227,1285,1340`; `AvailabilityCompactScaffold.kt:212,1222`; `JournalTab.kt:268,700`; `CartographyScreen.kt:245,291-297` |

**Why Part A does not use the plain tap.** The plain tap's sink is `SightingsMap.kt:523` (`onPlainTap() = currentOnTap()`); the compact tab's `onTap` is
`AvailabilityCompactMapUi.kt:602`: `if (isFullscreen) onToggleFullscreen()` then `tapped = null`. It also leaves fullscreen, which a stack tap does not do. On the entry
report's map it ENTERS fullscreen when no bubble is up. So the bubble is closed by its own sink.

## Part B: the planner's readings, each case and what it does

1. **Every kind that carries the line, from a fan or not.** The saved request is the bubble itself (`TappedMapThing`: kind, layer, feature, map position, anchor, bearing), not a
   find's id. Memory level, all five kinds (find, photo, waypoint, track, offline region): `MapReturnMemoryTest` "closing the remembered entry from its report hands back its
   bubble, for every kind that carries the line". Real screen: find and waypoint in portrait and short landscape, photo in portrait. **Track and region bubbles are not driven at
   screen level** (the stub map draws only point glyphs); they are held at memory level only.
2. **An entry opened from the Journal's own list is unaffected:** no request is made; "an entry opened from the Journal's own list still goes back to the Entries list" (both
   windows).
3. **Other ways out**, each against the find's side:
   - *another tab:* `AvailabilityScreen.kt:1217` forgets (now also the entry request, `MapReturnMemory.forget`). Test "leaving the entry for another tab forgets the way back to the map". Same as the find.
   - *editing:* leaving from the editor returns nothing and forgets (`onEntryClosed(..., fromReport = false)`), as editing a find forgets its origin. Test "an entry opened for editing is left to the Entries list ..." (both windows) and the memory test.
   - *the unsaved-changes prompt (J8-4, prompt first):* unchanged; the existing tests "opening an entry while another has unsaved edits asks first, and Discard / Save / Cancel ..." pass unchanged in `b-suite`.
   - *another entry swapped in while one is open:* the Journal closes the open entry and opens the requested one (`CartographyScreen.kt:270`). That close must not clear the new request, so a different entry closing leaves the request alone (memory test). The find side clears on a mismatch; **this is a difference**, and the reason is that the find side has no such swap.
   - *the entry deleted from its report:* **not exercised.** By the code the safety net (`CartographyScreen.kt:291-297`) reports the entry gone with `fromReport = true`, so it would return to the map with the bubble. The find's counterpart (`onFindDeleted`, `AvailabilityScreen.kt:1233`) returns to the map without its bubble because the find IS the bubble's record; here the bubble's record is untouched by deleting an entry. I did not drive a delete (the delete flow has a pending-delete layer) and cannot say what happens on a real screen.
   - *the forward detour* (entry, then its map's "Open find", Back, Back): the entry's request is its own field, so a find closing cannot clear it (memory test "a find closing does not clear the entry request"). **Not driven at screen level** (the harness's log state is static, so a find cannot be opened over the entry); the claim is the memory's, and a walk through this on the phone is the owner's.
4. **X gone when the user comes back:** `takeEntryBubble` asks `mapBubbleContentFor` (`MapBubbles.kt:228`); null means no bubble and one line logged. Test "a record that is gone brings back no bubble, and says so". The fan follows `openFanFor`'s own rule (existing tests).
5. **The camera is not touched.**

## The restored bubble's place (the planner's point) and a consequence of -380

- The find's return rebuilds the bubble with the anchor saved at "Open in Journal" (`MapReturnMemory.takeBubble`); the entry's does the same with the anchor saved at the line's tap.
- At every camera idle `reanchorFocusedBubble` (`SightingsMap.kt:454`, called at `:621`) moves a **point** bubble to its record's own place (`focusedFeaturePosition`, `MapBubbles.kt:119`), which is the marker in its stack, not the fanned copy displaced from it. So after the first idle a point bubble over an open fan points at the hub, not at its fanned icon. That is true of the find's return today and of this one. **Reported as a finding; neither fixed nor copied silently.**
- (b), a fan open with a bubble over it and following on: from the code, the idle after each follower move re-anchors the same way. The camera idled after every follower move in the -380 watch (`2026-10-01-fan-holds/hole/probe-watchB-window.txt`, 7288 moves in two minutes). So the bubble would sit at the hub, not at its icon. **Not run**, and not fixable here; for the owner.
- (a), on the phone, the return from a find's page with the fan: **not run.** It needs "Open in Journal", which this dispatch's phone list does not name. **Superseded by the addendum at the end of this report:** the planner authorised it afterwards and it was run.

## Tests changed (before and after)

No assertion of an existing test was changed. Four screen tests got one added line each in their stub sinks because `MapTapSinks` gained a method:
`AvailabilityScreenBubbleAndDropdownBackTest`, `AvailabilityScreenFanBackDrawerTest`, `AvailabilityScreenFanBackOthersTest`, `AvailabilityScreenFanBubbleDismissalTest`:
`override fun onCloseBubble() = currentOnCloseBubble()` and a `currentOnCloseBubble` read of `renderMode.onCloseBubble`. `FanOutTestScene`'s `RecordingSinks` gained the same, recording
`closeBubble`. Tests added: `MapTapHandlerBubbleStackTest` (6), two real-touch tests in the dismissal class, one in `CartographyEntryReportScreenMapTest`, nine entry tests in `MapReturnMemoryTest`
(one of them, "another entry closing ...", was first written to forget the request and was corrected before it was pushed), and in `JournalEntriesOnMapScreenTest` eight compact tests (each runs in portrait and in short landscape) plus one photo test.

## Evidence

- **Tests first, failing:** Part A `a-today/` (2 of 6), `a-stub/` (4 of 18); entry map `a2-stub/` (1 of 17); Part B `b-mem-stub/` (7 of 17) and `b-screen-stub/` (9 of 62); logs beside each, 0 compile errors.
- **Revert checks** (compile log read first, 0 `e:` lines in each log; files restored from git because every change was committed first; hashes before and after equal):
  Part A: handler call disabled, 4 fail (`a-revert-handler/`); CompactMapTab wiring set to `{}`, 2 real-touch tests fail and the 6 handler tests pass (`a-revert-wiring/`).
  Entry map hook set to `{}`, 1 fails (`a2-revert/`).
  Part B: (1) AvailabilityScreen no longer switches to the Maps tab, 9 fail; (2) the bubble no longer remembers the entry, 9 fail; (3) an editor close also returns, 3 fail (`b-revert-3-editor-returns/`); (4) a different entry closing clears the request, 1 fails; (5) the existence check removed, 1 fails; (6) `forget()` back in `onOpenEntry`, 9 fail. **One run was void and redone:** the first form of (2) did not compile (dropping a null check broke a smart cast; `b-revert-2-no-remember.log` was overwritten by the redo), and its "results" were absent, not failures.
- **Suites** (XML saved before any revert): Part A `a-suite_tally.txt`: 413 classes, 3359 tests, 0 failures, 0 errors, 24 skipped. Part B (includes the entry map and Part A) `b-suite_tally.txt`: 413 classes, 3386 tests, 0 failures, 0 errors, 24 skipped. The 24 skips are not mine and were not examined. `assembleDebug`: `builds/final-build.log`, 0 `e:` lines.
- **Merge:** `git merge origin/journal-redesign` reported "Already up to date" at `64be86f8`; no `docs/audits/README.md` row dropped.

## The phone

S22 `R5CT321008R`, installed `1.0.2401+g64be86f8` over `1.0.2385+gc170cf54` (forward). `runs/phone_final.txt`: ROTATION_0, Forager in focus, `ceDataInode` 2259049, `forager.db` sha256 `6357bd01…` (unchanged), animator 1.0, the only mp4 on the phone is `c5-fold.mp4` (not mine). `runs/bpB.mp4`, `bpB_times.txt`, screenshots `bp0-7.png`, dumps `bp_dump_*.xml`.

- **Part A on the phone: not run.** Only one stack is in view; there is no second stack beside the open fan, and I did not arrange one.
- **Part B on the phone: not run.** The stack in view was opened (28 members), and three fanned icons were tapped in turn, each bubble read from a `uiautomator dump`: a find ("Find on 2026-10-01", "Open in Journal"), a photo ("Not in a find or a journal entry", "View photo") and a waypoint ("Waypoint 10", "Directions", "Details"). None carries a "kept in" line, so there was no line to tap. Not arranged (the Layers sheet is not allowed). The owner's run.
- Seen on the phone, as at the base: a tap on a fanned icon shows its bubble over the open fan; the first Back closed the bubble and the fan stayed (glyph pixels 131,827 against 6,591 for the folded stack); the second Back folded it (6,591). No Back reached the exit prompt.

## The four disclosures

**Confirmed against inferred.** Confirmed by test: Part A end to end (handler and real touches), the entry map's hook, and the entry return for find, waypoint and photo (real screen), all five kinds (memory), the editor, tab and swap cases. Inferred from code only: track and region bubbles on a real screen, the entry deletion case, the forward "Open find" detour at screen level, the real `MapView`'s tap path and the fan's real reopening, the bubble's place after the first idle.

**Could not determine.** What a deleted entry does on a real screen; where the restored bubble points after the first idle on a device; whether the stub's `Back` ordering matches a real device for the three-window cases.

**Premises that were wrong.** (1) Part A first closed the bubble with the plain tap; it also leaves fullscreen (and on the entry map enters it), which the planner asked me to check. (2) My first memory test said a different entry closing forgets the request, as a find does; the Journal's swap of the open entry made that wrong, caught before it was pushed. (3) The dispatch listed three `MapRenderMode` builders; the entry report's was not wired by Part A.

**Decided beyond scope.** The entry's request is kept in its own field rather than threaded through the find's, and `CartographyScreen` reports an entry gone by any route (an effect) as well as by Back (synchronously), so a request cannot go stale after a Save at the leave prompt; the dispatch named the files but not that shape.

---

# Addendum, written after the report: phone case (a), run on `1.0.2401+g64be86f8`

This supersedes the report's line "(a), on the phone ... **not run**". The planner authorised the run after the hand-back ("open the stack, tap a fanned find, and in its bubble tap 'Open in Journal' ... on the
find's page, Back and nothing else ... fix nothing"). Evidence: `~/Zynergy/device-evidence/2026-10-01-bubble-paths/runs/bpA_result.md` (with `bpA.mp4`, `bpA_times.txt`, `bpA_a*.png`, `bpA_dump_*.xml`).

**What was done (12:28 to 12:31).** Reads before every tap: ROTATION_0, Forager in focus, version, `ceDataInode` 2259049, `forager.db` sha256 `6357bd01…`, animator 1.0. Tapped the stack, tapped a fanned find
(bubble: "1 more", "Find on 2026-10-01", "Open in Journal"), tapped "Open in Journal" at the centre of the bounds a `uiautomator dump` gave (`[120,1082][483,1143]`), pressed Back once on the find's page. The map came back with
the fan and the bubble. Then Back: the bubble closed and the fan stayed (glyph pixels 131,868). Then Back: the fan folded (6,592). No Back reached the exit prompt.

**What was measured** (device pixels, 3.75 px per dp; screenshots `bpA_a2.png`, `bpA_a4_early.png`, `bpA_a4_late.png`). The "1 more" text row of the bubble, as a stand-in for the card's place: 908 before leaving
(the bubble anchored at the finger, on the fanned icon); 815 in the first frame after Back (about 0.15 s, the map still blank); 1239 settled (about 3 s after Back). Settled against before leaving: 331 px, 88 dp lower.

**The inference and its limit.** The bubble's tail is not drawn in any frame, so the pointer was **not read**; it is inferred from the card's position. If the card is unclamped in y, the settled pointer is at
y = 1264 + 331 = 1595 px, and the fan's legs converge (the hub) at about y 1592 px in the settled frame: under 1 dp apart. The fanned icon is about 92 dp above that. The pointer's x cannot be recovered (the card is
clamped at the left edge). So, settled, the bubble points at the middle of the fan and not at the icon it was opened from, which is what `reanchorFocusedBubble` does at each camera idle
(`SightingsMap.kt:454`, called at `:621`, position from `focusedFeaturePosition`, `MapBubbles.kt:119`).

**Not explained.** In the first frame after Back the card was 93 px (25 dp) *above* where it stood before leaving the map. A transient layout before the map has its size, or a saved anchor that differs from the one at the
tap, would each do it; I did not determine which.

**Not run, still.** The same with the map following the location (the dispatch does not allow the locate button); from the code the idle after each follower move re-anchors the bubble the same way. The owner's decision on
this finding: "Ship with known issue and next release will carry the fix to live."

