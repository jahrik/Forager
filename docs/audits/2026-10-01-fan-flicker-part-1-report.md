# Fan-flicker, Part 1 report (dispatch 2026-09-28-369): diagnosis on the S22

Coder: Sonnet 5.5 (`claude-sonnet-5-5`). Branch `fan-flicker`, cut from `origin/journal-redesign` at `b6617c5c`.
Part 2 (the fix) is **not built**: the fix that works is a style-wide setting, which the dispatch makes a stop. Options are at the end.
Evidence: `~/Zynergy/device-evidence/2026-10-01-fan-flicker/` (videos, per-frame rows `*_rows.txt`, `measure.py`, `rec.sh`, build patches).

## Result in one paragraph
The cause is MapLibre's **placement transition** (the symbol fade-in), not out-of-step sources. Turning placement transitions off, with nothing else changed, removed every flicker measure on the S22 at animator 1 and 5, for the open and the fold-back (tests below). Hypothesis A is confirmed by its effect; hypothesis B was **not run** (see "Test B").

> **Correction, amendment 3 (completion report):** the placement fade is the cause of the dimming, the late fade-in and the blinking icons, and removing it fixed those. It is not the cause of every fault: with it off, the four layers still show different pushes in 85 to 88% of animating frames, the circles ahead of the icons (measured on the S22; the owner saw it on the S26). Test B, below, was needed after all.


## Conditions (the same for every recording)
S22 (R5CT321008R), 1440x3088 screen recorded at 720x1544, nominal 120 fps (timestamps about 8.3 ms apart; the recorder repeats frames, so only frames whose fan region changed are counted). Night map style, the same stack at the same zoom: seven members (three photo tiles, a flag, a pin, two mushrooms). One recording per condition (n = 1 each); the open and the fold are the two halves of each recording. Animator scale 1 and 5, restored to 1.0 and read back after each use. Tap on the stack's visible pink cap (map tap only); Back sent only after reading that Forager's window had focus; no swipes.

> **Correction, amendment 3:** the S22 was in 120 Hz mode for these recordings (the owner: "The S22 is currently in 120hz mode" and "It has been during this part of the test"; the planner read the active display mode; the 8.3 ms steps between changed frames in the rows). So those frames are real displayed frames, not recorder repeats, and every "60 Hz" said about the S22 in messages on this dispatch was wrong for these recordings. What the phone was set to on earlier days is not established, and where the dispatch's "60 on the S22" came from is not either.


## Measurement (`measure.py`)
Per unique frame in the fan region (480x520 crop), against two reference frames of the same recording: the folded stack before the tap (background) and the open fan at rest.
- **icon** = saturated pixels (S>120, V>120, not blue) not in the background's own saturated pixels, outside a 40 px disc at the stack; shown as a fraction of the rest frame's count.
- **circ** = pixels more than 25 grey levels darker than the background, S<90, V<200, outside the same disc; fraction of rest.
- **geometry rest** = the first frame after which the circle mask's 98th-percentile radius stays within 3 px (crop scale) of the rest radius.
- **final brightness** = icon >= 0.95 of rest and mean V within 8 of rest, from there to the end of the clip.
- **Flags:** *circle-without-icon* = icon < 0.5 and circ > 0.2; *icon-without-circle* = icon > 0.3 and circ < 0.2; *reversals* = direction changes of icon coverage in steps over 0.2.
Thresholds were checked against stills: base a1 frames 278, 279, 283 (legs and dark discs, no icons) and 284 (all icons) match the contact sheet; test A a5 frame 312 was checked at full resolution (below).

## Reproduction on the base (1.0.2329+gb6617c5c), `ff_base_a1.mp4`, `ff_base_a5.mp4`
| | animator 1 open | animator 1 fold | animator 5 open | animator 5 fold |
|---|---|---|---|---|
| geometry rest | 242 ms after first change | gone at 209 ms | 1058 ms | gone at 909 ms |
| circle-without-icon frames | 8 | 4 | 17 | 12 |
| icon-without-circle frames | 1 | 1 | 6 | 0 |
| reversals | 7 | 4 | 0 | 4 |
| icons at final brightness | 259 ms **after** rest | n/a | before rest (774 ms) | n/a |

What it looks like (a1 open, frames 265 to 306): the stack disappears for a frame (265), photo tiles show with no circles (271 to 276), then icon and circle coverage alternate frame to frame (icon 0.10, 0.74, 0.94, 0.10, 0.99, 1.03, 0.10, 0.10, 0.58 ... frames 278 to 292) while the legs and circles are already spreading; after the fan is at rest (frame 294) the icons stay at 0.10 to 0.16 until about frame 302 and reach full brightness at frame 325, about 260 ms later. In every dimmed frame **the flag stays solid** and the other glyphs vanish.

## Planner's diagnosis, part by part
- **Code facts (file:line, read at `b6617c5c`): confirmed.** `SightingsMap.kt:798-809` pushes on every progress change; `FanOutLayers.kt:79-80` makes four sources, `:92-99` the circle and symbol layers; `fanIconLayerProperties()` at `:105-118` has no ignore-placement, opacity or transition property; `pushFanFrame` at `:286-296` calls `setGeoJson` on each. `grep -rn "TransitionOptions\|\.transition" app/src/main` at `b6617c5c` finds nothing: no style transition is set anywhere, so the SDK default (placement transitions on, 300 ms) applies.
- **A, the symbol fade: confirmed by effect, mechanism inferred.** `Style.transition = TransitionOptions(300, 0, false)` (the third argument is `enablePlacementTransitions`, present in the pinned SDK 13.5.0: `javap` of `TransitionOptions` shows `isEnablePlacementTransitions`) removed the fault (table below). I did not read MapLibre's placement code, so "each `setGeoJson` makes the copies new symbols whose fade restarts" is the planner's account and fits the result; the test shows the fade is necessary for the fault, not which step restarts it.
- **B, sources out of step: not run.** Prediction recorded below. A already removes the alternation with four sources still pushed separately, which is evidence against B being needed to explain it; it is not a test of B.

> **Superseded by amendment 3:** B was tested as probe 2 against probe 3 (completion report). The reasoning here, that A removing the coverage mismatches meant sources out of step did not matter, was wrong: the coverage measure could not see a circle and its icon at different places. The planner recorded the same error for its own call.

- **"Not explained":** which glyphs stay solid. In my base clips the flag stays solid and the photo tiles, pin and mushrooms blink; the planner's S26 clip had the mushrooms solid; -319's S22 clip had the mushrooms and pin dim. **I could not determine why.** I have a guess (the flag moves least and its symbol is matched across tile versions, so keeps its opacity) that I did not test; treat it as unverified.
- **Other candidates not ruled out** (push rate, `symbolSortKey`, -299's per-frame properties): not tested individually. Test A leaves all three in place and the fault is gone, so none is *necessary* for it; I did not test whether any is sufficient on its own.

## Test A: placement transitions off
Prediction: the late fade-in disappears (icons at final brightness at or before rest); if the alternation is the same fade restarting, it disappears too; if it is sources out of step (B), it stays as absent-versus-full.
Change: one line at the start of the fan-draw effect, `style.transition = TransitionOptions(300, 0, false)` (patch: `builds/testA-placement-off.patch`; apk `builds/testA-placement-off.apk`, 1.0.2329+gb6617c5c.dirty, 0 `e:` in `builds/testA-build.log`).
| | animator 1 open | animator 1 fold | animator 5 open | animator 5 fold |
|---|---|---|---|---|
| circle-without-icon | **0** (base 8) | **0** (base 4) | **0** (base 17) | **0** (base 12) |
| icon-without-circle | 0 | 2 (805, 806) | 10 | 10 |
| reversals | **0** (base 7) | 0 (base 4) | 0 | 0 |
| icons at final brightness | **116 ms, before rest at 200 ms** (base 259 ms after) | n/a | 634 ms, before rest (1067 ms) | n/a |
Result: the prediction for A held and the alternation went with it, so B is not needed to explain it.

> **Superseded by amendment 3:** "B is not needed" holds for the coverage mismatches and the alternation, not for the circles running ahead of the icons.

**The icon-without-circle counts are a flaw in the dispatch's pass criterion, not residual flicker.** At animator 5 frame 312 (full-resolution crop checked) the icons are clustered at the stack and the circles are not yet visible: -299 grows each circle's radius from 0 with the progress, so early in the ease a circle exists with a radius of a pixel or two. The criterion "no frame shows an icon without its circle" fails there by design on any build. It needs a floor (for example, a radius under 4 dp does not count as shown); that is the owner's or planner's call, so I applied none.
Not measured: the front glyph at either end, and the icon offset at the fold's last frame and the open's first (-299 and -318); those are Part 2 checks.

## Test B: single source (not run; a deviation from the dispatch's Part 1 step 3)
Not run, on the planner's call (message from session `ec11ee`; the owner is being told and can overturn it). Free disk read 2011 MB when I went to build, under the 2048 MB floor, and I deleted nothing. The planner's reasoning: test A's build still pushed four separate sources and showed 0 circle-without-icon frames and 0 reversals at animator 1 and 5, so sources out of step did not produce the measured fault on the S22. **I agree, for the S22 at 60 Hz**: if re-tiling the sources apart were putting circles and icons out of step, that mismatch would remain with the fade off, and it did not (n = 1 recording per condition). The inference does not reach a contribution too small to see at this sample, or the S26 at 120 Hz, where the planner's own list had the push rate outrunning the re-tiling. Prediction recorded had B run: with placement transitions on, icons would still dim and blink. The patch is kept (`builds/testB-single-source.patch`; the tree was restored from a saved copy, sha256 `81edcba8...` equals the original) so B can be run later.

## The options for Part 2
**Did I find, or look for, a fix confined to the fan's own layers and source pushes? I looked, and did not find one.** What I looked at: `javap` of `SymbolLayer` and `Style` in the pinned SDK 13.5.0 for any per-layer setting that controls the placement fade. The only per-layer transition members are the paint-property transitions (`iconOpacityTransition`, `iconColorTransition`, and so on); I did not test whether any of them affects the placement fade, and I did not read MapLibre's native placement code. The only fade switch I found is the style-wide `TransitionOptions.enablePlacementTransitions`. Options 3 and 4 below are designs I reasoned out, **inferred and untested**; nothing fan-confined has been run on the phone.

What option 1 and 2 change outside the fan, layer by layer (read from the code at `b6617c5c`; the effect on each is **inferred from how the placement fade works, not observed**: test A was recorded on the fan only, and I did not look at the rest of the map with it off):
- The nine marker symbol layers built by `markerSymbolLayer` (`SightingsMap.kt:1177`): search centre, planned trip, waypoint, find, photo, and the three journal halos (waypoints, finds, photos). They would stop fading in and out. A user would see it when markers first appear (the map loads, data changes), when a layer is switched on or off in the Layers sheet, and as markers come into view on pan and zoom: they would pop in instead of fading over about 300 ms. `iconAllowOverlap(true)` is set on them, so no collision fade-out is involved.
- The fan's own icon layer: the intended change.
- The basemap's symbol layers: per the comment at `MapLibreOfflineMapRepository.kt:54-62`, the offline style has no `text-field` layers, so no label fade is lost; whether it has icon-only symbol layers, and which style is live on the device, I did not check.
- The live-location puck's layers: not checked.
- The -265 fade of other markers to 80% while a fan is open is a paint opacity change, a different mechanism, and should not change; not tested.

1. **Placement transitions off for the whole style, once per style load.** Run on the phone: yes (test A), fan only observed; fixes the open and the fold. Outside the fan: the layers above.
2. **The same, on only while a fan opens or folds plus a short tail, back on otherwise.** Run: no. Outside the fan: the same layers, only during those moments; still a map-wide setting; switching it mid-flight may itself re-place symbols (untested); the tail length would be a number to choose.
3. **Push the icons once and animate with per-member layers' `icon-translate`.** Run: no. Outside the fan: nothing, in principle. Cost: N layers per fan; a symbol's first appearance would still fade in over about 300 ms, longer than the 250 ms fan, so "icons at final brightness by the frame the fan rests" would probably still fail.
4. **Draw the icons as raster image sources, one quad per member.** Run: no. Outside the fan: nothing, in principle. Cost: a larger rewrite with a real risk to the look at rest.
I recommend asking the owner about option 1 or 2, since they are the only ones measured or close to it; that is a recommendation, not a decision.

## Premises that were wrong or need a correction
- The pass criterion above (icon-without-circle at near-zero circle radius).
- "The icons fade in after the fan is in place and the photo icons are gone": in my S22 base clip the dimming starts during the spread and the flag does not dim; which glyphs dim varies by clip (above).
- Nothing wrong found in the planner's code facts.

## Four disclosures
- **Confirmed vs inferred.** Confirmed by measurement: the base fault (n = 1 per condition), and that test A removes it. Inferred: the mechanism inside MapLibre (restarting fade), and that the alternation is the same cause (consistent with test A, not isolated). Read in the SDK: that `TransitionOptions` has `enablePlacementTransitions` (javap), not its behaviour.
- **Could not determine.** Why one glyph stays solid; B; what turning the setting off does to the rest of the map; the S26 at 120 Hz.

> **Correction:** the S22 was also in 120 Hz mode (see the note under Conditions). What separates the S22's runs from the owner's S26 clip is the stack (7 members against 12), the style (night against day) and the phone, not the refresh rate.

- **Wrong premises.** See above.
- **Decided beyond scope.** I wrote `measure.py`'s thresholds and flags myself, as the dispatch allowed ("or your own"); I chose animator scale 5 and 1 only as asked; when a command failed with a disk-quota error I deleted three scratch frame caches of my own (about 1 GB each, in `/tmp/ff`); `/tmp` is a separate tmpfs (`df` shows 5633 MB, mounted on `/tmp`), so that deletion freed nothing on `/` and the 2011 MB figure is unaffected by it.

## The phone
Start: 1.0.2306+g19f159fd, user 0 ceDataInode 2259049, `forager.db` sha256 `e1188b00f0da4b713e4ef6d6626b90139247a886460e5bf317d39996c8e2b1db`, animator 1.0; data copied read-only to `data-copy/` (tars with `SHA256`). Installs, in order (`adb install -r --user 0`, own builds only; I did not record the clock time of the first two installs, only the apk's file time and the recordings' file times):
| # | build | apk sha256 (first 16) | apk file time | versionName read back after install | recordings made on it |
|---|---|---|---|---|---|
| 1 | base | `c19d4e3195c92ec6` | 23:42:20 | `1.0.2329+gb6617c5c` | `ff_base_a1` (23:43), `ff_base_a5` (23:47) |
| 2 | test A | `8887687baae5904f` | 23:51:23 | `1.0.2329+gb6617c5c.dirty` | `ff_testA_a1` (23:52), `ff_testA_a5` (23:53) |
| 3 | base again | `c19d4e3195c92ec6` | (same file as #1) | `1.0.2329+gb6617c5c` | none; `lastUpdateTime` 23:54:43, read at 23:56 |
The two builds do **not** share a versionName: test A carries the `.dirty` suffix (the working tree was modified), the base does not. That, read back after each install, and the order of events tie each recording to its build; I did not read the installed apk's hash off the phone after installs 1 and 2 (the planner read it after #3 and it matched `c19d4e31`). The planner's reading of the phone at 06:55Z (base, no `.dirty`) is consistent with install #3; my earlier hand-back said the phone was on test A at the moment I wrote it, before I reinstalled the base.
After each install the inode (2259049) and the db hash (`e1188b00...`) read back unchanged. Settings: animator scale set to 5.0 twice and read back as 1.0 after each. End state: base build installed, db hash unchanged, animator 1.0, app closed (the reinstall stopped it; no Forager process running), recordings deleted from the phone after each pull.
