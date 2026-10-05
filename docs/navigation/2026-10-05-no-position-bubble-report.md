# No bubble under the dot: report (dispatch 2026-09-28-535)

Dispatch: `prompts/preserved/2026-10-05-03.md` on `records-after-172`. Owner's ruling: RECORD -534. Built on branch `no-position-bubble` from `origin/main` at `a9dfdd59` (I confirmed `main` was still there before starting). Not merged.

## In plain terms

The "Approximate location" and "Last seen 2 h ago" bubbles under the dot are gone. The owner, verbatim: "the bubble message is repeating what the strip says. One has to go, and my vote is for the bubble message."

- **No GPS, Maps tab:** a soft dot in its circle, with no bubble. The strip reads "37° NE · Approximate location, finding GPS…".
- **Navigating:** the HUD replaces the strip, as before, and reads "Approximate, finding GPS…".
- **Nothing live:** a grey dot, with no bubble. The strip reads "Last seen 2 h ago, finding GPS…".

The dot's looks, colours and circle are unchanged, and so are the strip's and the HUD's words.

## Verify before building (reported to the owner by message before building)

**1. What existed only for the bubble.** I grepped `app/src` for every name involved, at `a9dfdd59`.

Removed:
- **`ui/availability/PositionLabel.kt`:**
  - `MapPositionLabel`, `MAP_POSITION_LABEL_TAG`, `MAP_POSITION_LABEL_GAP` and `MAP_POSITION_LABEL_CORNER`;
  - `PositionNote.labelText`, which had no reader except the bubble.
- **`AvailabilityCompactMapUi.kt`:** the `positionAnchor` state (`:596`), the screen-point hookup (`:681`) and the bubble's call (`:727`).
- **The dot's screen point:**
  - `MapRenderMode.onShownPositionScreenPoint` (`MapSlot.kt:273-278`) and its pass-through (`:580`);
  - in `SightingsMap.kt`: the parameter (`:304-305`), `currentOnShownPositionScreenPoint`, `currentTrackLiveLocationForPosition`, `reportShownPositionScreenPoint` (`:351-366`), and its three calls (`:671`, `:700`, `:1093`).
  - Its only reader was `positionAnchor`. No other map passed the callback: the Journal pickers and the cartography map used the `{}` default.

Kept, because something else reads them:
- `APPROXIMATE_LOCATION_TEXT`, `FINDING_GPS_SUFFIX`, `lastSeenText` and `formatSeenAge`: the strip uses them, and the HUD uses them at `ApproximatePositionHud.kt:108`.
- `rememberPositionNote` and `PositionNote.stripText`: the strip (`AvailabilityMapControlsUi.kt:470`).
- The fix-into-the-dot `LaunchedEffect` in `SightingsMap.kt`: it still passes each fix to the dot. Only its report line went.

**2. The strip covers every state where the bubble showed.** The bubble was drawn in every state, including while navigating.
- The strip (`AvailabilityCompactMapUi.kt:881`) is a direct child of the map's `Box`. The only thing that hides it is `!isNavigating`.
- Fullscreen hides only the bottom nav (`:1110`) and the rail (`:1147`), not the strip.
- In landscape the strip sits in the rail-side corner (`:893-899`); in portrait it is full width.
- `NavigationHud` (`:987`) is gated on exactly `isNavigating`, so the strip and the HUD cover every state between them.
- `CompactMapTab` is the only place the strip is drawn (`AvailabilityCompactScaffold.kt:928`). No other map screen had the bubble.

So no state shows the dot with neither the strip nor the HUD. This was not a stop.

**3. The tests that asserted the bubble** (all in `AvailabilityScreenApproximatePositionTest`; the r-numbers are -510's revert rows):
- **The network-reading test** (r21, r22b, r23, r24): the bubble's text checks became "no bubble". The checks that it is centred under the dot and has an 80% fill were removed, because they tested only the bubble's placement.
- **The GPS-fix test:** the "label goes" check was removed, because its tag no longer exists. The strip-note check already covers that case.
- **The last-known test** (r21, r33): "Last seen 2 h ago" became "no bubble". After the newer reading arrives, the old label-text check became "still no bubble", and a new check reads the strip's "Approximate location, finding GPS…".
- **The far-waypoint HUD test and the last-known HUD test:** each gained "no bubble". The far-waypoint test also gained "no strip", since the HUD carries the words.
- **The fake map:** it no longer reports the dot's point, since the callback is gone.

## What was built

- **"No bubble" in the tests** means no node on screen reads *exactly* the bubble's words (`assertNoBubble`). The strip and the HUD carry the same words with ", finding GPS…" after them, so only the bubble could ever match exactly.
- **The app code:** the removals listed in item 1, plus a comment where the bubble's call was, quoting the owner. Comments that named the label were updated in `PositionLabel.kt`, `SightingsMap.kt` and `MapPosition.kt:102`.

## Tests

All runs used `:app:testDebugUnitTest`, on the owner's "go" in this window. Each run's log was checked for compile errors before its JUnit XML was read. The logs are outside the repository, in `/tmp/535/`.

| Run | Build | Compile errors | Result |
|---|---|---|---|
| Tests first, `red.log` | `c7e04dde` (the new tests, app code unchanged from `a9dfdd59`) | 0 | 8 tests, **4 failed**, as predicted: each failed with `no bubble reading "…" (dispatch -535)` |
| The fix, `green.log` | `3ca5b138` | 0 | 8 tests, 0 failures |
| Revert, `rev-bubble.log` | `3ca5b138`, with its five changed app files replaced by their `a9dfdd59` versions (the bubble fully back) | 0 | 8 tests, **4 failed**: the same four tests, the same four messages |

The four that failed, and their messages:
- the network-reading test: `no bubble reading "Approximate location"`;
- the last-known test: `no bubble reading "Last seen 2 h ago"`;
- the far-waypoint HUD test: `no bubble reading "Approximate location"`;
- the last-known HUD test: `no bubble reading "Last seen 2 h ago"`.

The two HUD failures are real. The bubble was composed while navigating too, which the dispatch's step path did not mention.

The revert's restore came from copies saved before the edit, not from git. Afterwards, `git status` was clean, `git diff 3ca5b138` was empty, and `PositionLabel.kt` had 0 references to `MapPositionLabel`.

## Suite

SUITE_PLACEHOLDER

## Not tested, and the phone

There is no phone step of its own, as the dispatch says: the change removes a drawn element, and the tests read the composed tree. **The look is to be checked at the next S22 session after this merges.** That check covers the soft dot with no bubble, and the grey dot with no bubble, which is still unconfirmed on a phone (RECORD -534).

## Disclosure

**Confirmed vs inferred.**
- **Confirmed by tests through the real `AvailabilityScreen`:**
  - no bubble for an approximate position or a last known one, on the map and while navigating;
  - the strip's notes unchanged;
  - the HUD's notes unchanged.
- **Confirmed by grep:** that nothing else read the dot's screen point.
- **Inferred, not seen:** that the phone draws nothing under the dot. A real MapView cannot run under Robolectric.

**Could not determine.**
- The look on a phone (left for the next S22 session).

**Premises that were wrong.**
- None in the dispatch's citations. Every line it named was where it said, at `a9dfdd59`.
- One omission: the step path describes the HUD while navigating, but not that the bubble was also drawn then. It was (the two HUD tests failed on `a9dfdd59`), so it was removed there too, as the owner's "Remove both" covers.

**Decided beyond scope.**
1. **`PositionLabel.kt` keeps its name** though it no longer holds a label. Renaming it is cosmetic and would have widened the diff.
2. **The fix-into-the-dot `LaunchedEffect` keeps `mapLibreMap` as a key.** The key was there for the report, but removing it would change when the collection restarts, so it stays, with a comment saying why.
3. **`PositionLookOptionsTest`'s test name still says "the label"**. It is a test name, and renaming it is cosmetic.

## Commits

On `no-position-bubble`, from `a9dfdd59`:

- `c7e04dde`: tests first (the bubble tests become absence tests)
- `3ca5b138`: the build
- the report and its two index rows (this commit)
