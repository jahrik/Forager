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

---

# Addendum, same dispatch: the planner's review, the mark with no move, the instrumented run (supersedes where stated)

Code now at `c170cf54` (tests first `d6068c5e`, fix `669adb2b`, second flaw tests first `7c68d989`, fix `c170cf54`). Evidence in
`~/Zynergy/device-evidence/2026-10-01-fan-holds/hole/` unless a path says otherwise. The text above is left as written; this section
corrects it where it says so.

## Premises that were wrong (the planner's and mine)

1. **A mark with no move.** The planner accepted the marking design in Part 1 without asking what happens to a mark whose move never
   starts, and so did I; my own class comment named the case ("lasts until the next idle") and accepted it as "the old behaviour". The old
   behaviour is the fault the owner asked to fix ("If a small jump will close the fan then we shouldn't let that pass"). A mark set for a
   frame that could not be applied, a camera already in place, or a locate tap with no fix would have waited for the follower's next move and
   been taken for the app's. Fixed in `669adb2b`.
2. **"Following on" in `follow_r1..5` and `revfollow_r1..5` was an inference that this run shows to be unsupported.** The report above says
   following was "apparently on" because the stack sat at screen centre. The instrumented build reads the camera mode: a fresh launch gives
   mode 8 (none), the stack sits at the same centred place, and only the locate tap makes it 24 (tracking) [`probe-launch-1046.txt`,
   `probe-all-final.txt`]. So the stack at centre says nothing about the mode, and those ten clips were most likely taken in mode 8, which
   is why the reverted variant held the fan there too and logged nothing. I cannot prove the mode for those clips; I can say the inference
   was wrong as a method. They remain what the report above said: nothing regressed on a still phone.
3. **"A still phone gives the follower nothing to move" (said above) is false.** With the mode at 24 the follower moved the camera 7288 times
   in two minutes, about 60 a second, on a phone lying still [`probe-watchB-window.txt`].

## The finding that shaped the fix (read from bytecode, not run)

`javap -c` of SDK 13.5.0 (`android-sdk-13.5.0/jars/classes.jar` in the Gradle transforms cache): `Transform.moveCamera`, `easeCamera` and
`animateCamera` each call `CameraChangeDispatcher.onCameraMoveStarted`, which stores the reason and queues a message
(`CameraChangeHandler.scheduleMessage`, a no-argument `Handler`, so the main looper, no delay). The app's listener runs on a later looper
turn, not inside the camera call. `scheduleMessage` also removes a pending message and queues a new one at the back. So "clear the mark when
the call returns" would clear it before the listener runs, and a single hop after the mark could run before the message that belongs to it.
The classifier therefore has three mark states (none, armed, in flight) and a settle step that hops twice through the looper; an armed mark
still there then had no move and is dropped. No time limit. The cost, in the class comment: a site that moves the camera more than two
looper turns after marking would be classed as the follower's.

## A second flaw, found by the instrumented launch log

The first fix (`669adb2b`) let any settle step drop the current armed mark. At launch, with the main thread busy, mark 1's second hop ran
after its move had gone idle and mark 3 had been set; it dropped mark 3, and mark 3's own move then started unmarked and was classed
`UNKNOWN` (`probe-launch-1046.txt`, 10:46:31.375 to .465, on the `669adb2b` instrumented build). With following on it would have been taken
as the follower's. `c170cf54` numbers each mark and a settle step may drop only the latest mark's own. On the `c170cf54` build the same
launch shows the stale steps ignored (`settle: for=1 current=3 mark was IN_FLIGHT`, `for=2 current=3`) and no `UNKNOWN` [`probe-all-final.txt`,
11:06:46]. The planner's review found the first hole by reading; this one only the instrumented log showed. The instrumented build is why.

## Device evidence that sees the follower move (instrumented build, throwaway, not in the tree)

The instrumentation (`instrumentation-c170cf54.patch`, sha256 `e9b22a23e4b28de9…`; apk `instrumented-c170cf54.apk`, `a80cf09904af926e…`)
logs under the tag `FanProbe` every mark (with its site), every move start (reason, following, camera mode, mark state, cause), every idle,
every settle step, every fan open (with the camera mode) and fold (with its caller). It crashed once on first install: my probe read the
camera mode before the location component was activated (`LocationComponentNotInitializedException`, crash buffer); guarded and rebuilt.
It is not in the repository.

- **Locate tap with a fan open** [`probe-locA.txt`, `runs/locA*`]: fan opened in mode 8 (11:07:27). Locate tapped at 11:07:33.390: mark
  `site=locate-tap`, 24 ms later `move-started following=true mode=24 markBefore=ARMED cause=APP_REQUESTED`, and `fan FOLD wasOpen=true`
  with caller `onCameraMoveStarted`. **The locate tap folds an open fan**, as a move the user asked for should. The transition into following
  starts inside the call and does not wait for a fix. After it, bursts of `LOCATION_FOLLOW` moves with the fan already folded.
- **Two-minute watch, fan open, mode 24** [`probe-watchB-window.txt`, `runs/watchB.mp4`, `watchB_open.png`, `watchB_end.png`]: fan opened
  11:08:20.319 (`cameraMode=24`). Between 11:08:19 and 11:10:26 there were 7288 `move-started`, every one `following=true mode=24
  markBefore=NONE cause=LOCATION_FOLLOW`, and one fan line in the window, the open. The fan was open in the screenshots at +3 s and at
  +2 min and closed only on Back at 11:11:04.9 (`fold` caller `MarkerFanOutBackHandler`).
- **The check that could fail** [`probe-revwatch-window.txt`, `runs/revwatch*`]: the same run on an instrumented variant with the follower
  branch returning `UNKNOWN` (`instrumentation-c170cf54-revertvariant.patch`, `instrumented-c170cf54-revertvariant.apk`, `9d68b3c115413733…`),
  mode 24 after a locate tap: fan `OPEN` 11:12:15.118, `FOLD wasOpen=true` at 11:12:15.218 from `onCameraMoveStarted`, 2080 moves classed
  `UNKNOWN`, 2076 `SightingsMap` warnings in the window. **So this device run can tell the fix from the old behaviour, and the fix holds
  where the old behaviour folds in 100 ms.** (The variant's warning count, 2076, is below its move count, 2080, by 4: the first moves in the
  window were not warned; I did not look into why.)
- **Which mark sites the runs reached** (`probe-all-final.txt`, `c170cf54` builds): `activate-after-style` (2 marks, move classed
  `APP_REQUESTED`), `region-focus` (4 marks, `APP_REQUESTED`), `locate-tap` (2 marks, `APP_REQUESTED`). **Not reached:** `restore-init`,
  `restore-styleload`, `frame` (`applyCameraFrame`) and `orientation-reset`. Their marks are tested only in the classifier's unit tests; whether
  each makes its move inside the two-turn window is unproved. The live-location activation site (`SightingsMap.kt`, `activate-after-style`),
  the one the planner named, was reached twice and its move started inside the window both times.
- Warnings on the fix builds: no `SightingsMap` "not a touch, not marked" warning on the `c170cf54` fix build in any run.

## The owner's own check, as theirs, on an earlier build

The owner, to the planner: "Device tested myself, map moves with fan moving off screen and not closing." That was on build 951feea7, before
the mark's lifetime changed, and is evidence that a follower's real move leaves the fan open. It is not evidence for the changes in
`669adb2b` or `c170cf54`, which change which moves count as the app's. Their screenshot of the walk (dot centred, fan open, partly off the
right edge): `~/Zynergy/device-evidence/planner-fan-flicker-2026-10-01/owner-s22-fan-holds-walk-1037.png`.

## Verification of the changes in this addendum

- Tests: `CameraMoveClassifierTest` 13 (5 new), `MapTapHandlerFanHoldsTest` 13 (3 new), new `FakeLooper` test helper. Stubs failed 4 of 37
  (`stub-failing-build.log`); the stale-settle test failed 1 of 26 on `669adb2b`'s code (`stub2/`); on the fix 13/13, 13/13, `MapTapHandlerTest`
  12/12, `FoldOnlyIfMembersChangeTest` 13/13, `MapTapHandlerReopenFanTest` 5/5 (`fix-build.log`, `fix/`, `fix2-build.log`, `fix2/`), 0 compile errors
  each.
- Revert checks, compile log first, 0 `e:` lines each, files restored from git (safe: the changes are committed) and hash-checked:
  settle step disabled (`669adb2b`'s change) fails 4 tests (`revert-settle.log`, `revert-settle/`); generation guard removed (`c170cf54`'s change)
  fails the 1 stale-settle test, specific message "mark 2's move is the app's whichever settle step runs first" (`revert-generation.log`,
  `revert-generation/`).
- Full suite on `c170cf54`: 412 classes, 3351 tests, 0 failures, 0 errors, 24 skipped (`suite-c170cf54.log`, `suite/`, `suite_tally_c170cf54.txt`).
  An earlier full run on `669adb2b` (3350 tests, 0 failures) is `suite-669adb2b.log`; the `951feea7` run in the folder above was 3343.
- The Gradle transforms cache was deleted by the owner during this work; the first build after regenerated it (the suite took one normal run).

## End state

Phone: clean `1.0.2385+gc170cf54` (no `.dirty`), `builds/fan-holds-c170cf54-final.apk`, ceDataInode 2259049 unchanged, `forager.db` sha256
`6357bd01…` unchanged, animator 1.0, portrait, Forager in focus. The camera mode after launch is none until the locate button is tapped.

## Not verified

- Walking: the owner's check is on `951feea7`; the owner repeats it on `c170cf54`. A phone lying still already gives the follower at ~60 moves
  a second once the mode is tracking, so the walk adds position changes, not the first follower move.
- Four mark sites unreached (above).
- A pan/zoom/rotate by finger, a search result and a style switch with a fan open on the final code: classifier and handler unit tests only.
- Touch areas after the map has moved by real travel; the tap on a fanned icon after the map moved on the phone (the owner has not tried it).
