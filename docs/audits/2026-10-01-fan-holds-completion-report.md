# Fan-holds completion report (dispatch 2026-09-28-380)

Branch `fan-holds`. Code at `951feea7` (tests first at `52d9843f`); `journal-redesign` merged in at `6eb42da9` (docs only, no source
file changed by the merge). Every figure below is read from a file in `~/Zynergy/device-evidence/2026-10-01-fan-holds/` (named in
brackets); the folder is outside the repo and not committed.

## What the owner asked for

"If a small jump will close the fan then we shouldn't let that pass"; "Fan should close on map movement. The user attempting to pan away
means they have to tap to put it away first, which is an extra step"; "Keep the intentional map movements"; "That Back condition is
acceptable and normal. The fix for that is a line in the FAQ" (the FAQ line is not part of this dispatch).

## What landed

- **Camera moves are classified by cause** (`fanout/CameraMoveClassifier.kt:42-47`): a touch gesture is the user's; a move the app marked
  (`markAppMove`, `:32`) because the user asked for it or because a style or restore is re-applying the view is the app's; an unmarked,
  non-gesture move while the location follower is on (`cameraMode != NONE`, `SightingsMap.kt:565`) is the follower's; anything else is
  unknown. The mark is cleared at camera idle (`:37`, called at `SightingsMap.kt:579`).
- **Only the follower's own re-centring leaves an open fan open** (`MapTapHandler.kt:126-127`). Touch, a requested move, and an unknown
  move fold it as before. An unknown move is logged as a warning (`SightingsMap.kt:568`), per the repo's no-silent-fallback rule.
- **Marks** are set before: the restore at style init (`SightingsMap.kt:703`), the restore after a style swap (`:734`), live-location
  activation after a style load (`:747`), an applied camera frame (`:784`), the region/focus camera set (`:795`), the locate effect before
  the mode change (`:964`), and the orientation-reset ease (`:1017`).
- **Touch areas follow the map**: a tap on an open fan is hit-tested against the markers' places now, from `probe.markersOf`, not the
  places stored when the fan opened (`MapTapHandler.kt:76`, `liveMembers()` at `:156`). A member the map cannot locate keeps its stored
  place and a warning says so.
- Not touched: `-369`'s three fixes, fan timing and easing, reduce-motion, fan geometry, what a tap does, every Back order. Diff against
  `693ece8a` for `app/src`: 6 files, 348 insertions, 14 deletions.

## Verification

**Unit tests (18 new: `CameraMoveClassifierTest` 8, `MapTapHandlerFanHoldsTest` 10).**
- On stubs, before the fix: 43 tests ran, 13 failed [`tests/stub-failing-build.log`]; per class 7 of 8 and 6 of 10
  [`tests/stub-failing-*.xml`]. On the fix: 8/8 and 10/10 [`tests/fix-passing-*.xml`].
- Full suite on the fix: 412 classes, 3343 tests, 0 failures, 0 errors, 24 skipped, build successful, 0 compile errors
  [`suite/*.xml`, `suite/fh-suite.log`; tally in `tally.txt`]. The 24 skips are not mine and I did not look at them.
  The suite ran before the merge of `journal-redesign`; the merge changed only `RECORD.md` and a preserved dispatch.

**Revert check (unit), compile log read first** [`revert/r1.log`, `revert/r2.log`: 0 `e:` lines each].
- r1, hit test reads the stored members instead of `liveMembers()`: 3 failures in `MapTapHandlerFanHoldsTest`, each about the moved map
  ("a tap where an icon is now drawn finds it", "a tap where the icon used to be does not find it", "keeps its stored place, and says so")
  [`revert/r1/`].
- r2, the follower branch returns UNKNOWN: 2 failures in `CameraMoveClassifierTest` ("expected LOCATION_FOLLOW but was UNKNOWN")
  [`revert/r2/`].
- Both files restored from copies saved before editing; the hashes after (`292645f94bcc…`, `ba28898633e5…`) equal the hashes before.
- **Not shown by the revert:** the handler test "a location-following move leaves an open fan open" and "a run of follower moves" fail on
  the stubs but did not fail under r2, because r2 changes the classifier and those two tests hand the handler a cause directly. They hold
  the handler; the classifier tests hold the classifier. The wiring between them (`SightingsMap.kt:565-566`) has no unit test; it is a
  Compose/MapLibre listener, and only the device exercises it.

**Device, S22 (portrait, animator 1.0)** — build `1.0.2378+g951feea7` installed over `1.0.2371+g693ece8a`; data inode 2259049 and
`forager.db` sha256 `6357bd01…` unchanged before and after [`builds/`; checked on screen during the run, not saved to a file].
Eleven recordings, plus five more on a deliberately reverted build. State per clip is derived from the clip itself
[`clip_state.py`; `clip_state_output.txt`, `clip_state_revfollow.txt`]; empty-frame gap by the `-369` script [`gap_output.txt`].

| clips | build | following | result |
|---|---|---|---|
| `before_r1..3` | old (`693ece8a`) | off (stack not at screen centre) | fan opens ~2.5 s, holds to Back, folds; no empty frame |
| `after_r1..3` | fix | off | same; no empty frame |
| `follow_r1..5` | fix | on (stack at screen centre after the locate tap; the tap itself I did not see — see below) | opens, holds to Back (~9.8 s), folds; no empty frame |
| `revfollow_r1..5` | fix with follower line reverted (`builds/revert-variant-follower-unknown.apk`, version name ends `.dirty`) | on | **identical to the fix: opens, holds, folds** |

**What the last row means.** The revert variant makes every follower move fold the fan. It did not fold it, and the log
[`logcat-revfollow-*.txt`, window 10:28:11 to 10:29:55 in `revfollow_start.txt`] holds no `SightingsMap` warning, which the variant would
have written for each unclassified move. So during these runs the follower made no camera move at all: the phone was still. **The
following-on device runs therefore do not discriminate the fix from the old behaviour, and are not evidence that the fix works.** They show
only that nothing regressed on a still phone. Showing the real case needs the phone to move (a walk), which this device check cannot do.

**Provenance of `follow_r1..5`.** Made at 10:13 to 10:15, about five minutes before the 10:19:42 reboot, in a part of the session I
cannot recall. They were taken after the phone went to portrait. That following was on is inferred from the stack at screen centre (the
view the locate button sets), not from a read mode. The `revfollow` runs were taken afresh, by me, with the same method.

**-369 measures with following off.** Empty-frame gap on `before_r*` and `after_r*`: none, in all six [`gap_output.txt`]. I did not re-run
`discs_early.py` (disc drop-out); this dispatch changed no drawing code, but I did not measure it.

## Not verified / owner's hand check

- A fan staying open while the follower re-centres under real travel, and an old-build-versus-fix comparison under travel: not shown.
  Unit tests only. Owner's check: open the fan with following on and walk a few steps.
- A fan folding on the user's own pan/zoom/rotate, and on a search result or style change, with the new wiring: unit-tested for the
  handler and classifier; the listener wiring is device-only and not exercised by me (no swipes allowed on the phone).
- Style switch (day/night) with a fan open: marks are set at the restore sites so it folds as before; not run.
- Touch areas after real travel: unit-tested with a panned scene only.
- I could not install the old build to compare (downgrade not authorised), so `before_r*` and the fix runs are on different builds
  but neither exercises a follower move.

## Process notes

- Reboot (power cut) at 10:19:42 wiped `/tmp`, including saved copies for the revert check; the revert check had already run and its
  results were copied into `revert/` at 10:08. The second (device) revert used `git checkout -- <file>` to restore, which is safe only
  because the forward change was committed at `951feea7`; the file hash after matched the hash before.
- The phone sat in landscape during part of the run; I paused rather than tap blind.
- The final installed build is the clean commit: `builds/fan-holds-951feea7-final.apk`, byte-identical (same sha256) to the first
  clean build; version name `1.0.2378+g951feea7`, no `.dirty`.
- Evidence folder: `clip_state.py` and `runs.sh` (copied and edited from `2026-10-01-fan-flicker/runs.sh` to write here).
