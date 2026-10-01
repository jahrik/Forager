# Fan-flicker, Part 1 report (dispatch 2026-09-28-369): diagnosis on the S22

Coder: Sonnet 5.5 (`claude-sonnet-5-5`). Branch `fan-flicker`, cut from `origin/journal-redesign` at `b6617c5c`.
Part 2 (the fix) is **not built**: the fix that works is a style-wide setting, which the dispatch makes a stop. Options are at the end.
Evidence: `~/Zynergy/device-evidence/2026-10-01-fan-flicker/` (videos, per-frame rows `*_rows.txt`, `measure.py`, `rec.sh`, build patches).

## Result in one paragraph
The cause is MapLibre's **placement transition** (the symbol fade-in), not out-of-step sources. Turning placement transitions off, with nothing else changed, removed every flicker measure on the S22 at animator 1 and 5, for the open and the fold-back (tests below). Hypothesis A is confirmed by its effect; hypothesis B was **not run** (see "Test B").

## Conditions (the same for every recording)
S22 (R5CT321008R), 1440x3088 screen recorded at 720x1544, nominal 120 fps (timestamps about 8.3 ms apart; the recorder repeats frames, so only frames whose fan region changed are counted). Night map style, the same stack at the same zoom: seven members (three photo tiles, a flag, a pin, two mushrooms). One recording per condition (n = 1 each); the open and the fold are the two halves of each recording. Animator scale 1 and 5, restored to 1.0 and read back after each use. Tap on the stack's visible pink cap (map tap only); Back sent only after reading that Forager's window had focus; no swipes.

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
**The icon-without-circle counts are a flaw in the dispatch's pass criterion, not residual flicker.** At animator 5 frame 312 (full-resolution crop checked) the icons are clustered at the stack and the circles are not yet visible: -299 grows each circle's radius from 0 with the progress, so early in the ease a circle exists with a radius of a pixel or two. The criterion "no frame shows an icon without its circle" fails there by design on any build. It needs a floor (for example, a radius under 4 dp does not count as shown); that is the owner's or planner's call, so I applied none.
Not measured: the front glyph at either end, and the icon offset at the fold's last frame and the open's first (-299 and -318); those are Part 2 checks.

## Test B: single source (not run)
Prediction if it had run: circles and icons in one source (circle layer filtered by `has(circleScale)`, symbol layer by `has(image)`), placement transitions left on, would still show the icons dimming and blinking, because the symbol fade is untouched; the only change would be that icon and circle updates land together. Not built: free disk read 2011 MB when I went to build, under the dispatch's 2048 MB floor, and I deleted nothing to make room. Patch saved (`builds/testB-single-source.patch`, tree restored from a saved copy, sha256 `81edcba8...` equals the original). Waiting on the planner whether it is wanted.

## Fix: what it would change, and why Part 2 stops
The measured fix is style-wide: `enablePlacementTransitions = false` turns the symbol fade off for **every** symbol layer on the map (the registry's marker icons and any labels), not only the fan's. Part 2 allows only a fix confined to the fan's own layers, sources and push, and names a style-wide transition or fade setting as a stop. So I have not built it. Options, each with what it costs:
1. **Placement transitions off for the whole style, once per style load.** One line; measured (above) to fix the open and the fold. Cost: every other symbol also stops fading, so the map's markers and labels appear and disappear instantly on zoom, pan and filter changes, and the fan-fade of other markers (-265, a paint opacity change, not a placement fade) is a different mechanism and should be unaffected. I have **not** looked at what the rest of the map does with it off; that needs the owner's eye.
2. **The same setting, switched on only while a fan is opening or folding plus a short tail, and back on otherwise.** Cost: the rest of the map pops during those moments only; it is still a map-wide setting and untested (switching it mid-flight may itself re-place symbols); the tail length would be a number to choose.
3. **Keep the fan's symbols from being re-placed: push the icons once and animate with per-member layers' `icon-translate`.** Fan-only in principle. Cost: untested; N layers per fan; and the first appearance of a symbol still fades in over about 300 ms, longer than the 250 ms fan, so "icons at final brightness by the frame the fan rests" would probably still fail without something else.
4. **Draw the icons as raster image sources, one quad per member.** No placement, so no fade. Cost: a larger rewrite with a real risk to the look at rest; untested.
My recommendation is to ask the owner about option 1 or 2, because they are the only ones with a measured or near-measured result; that is a recommendation, not a decision, and I have made none.

## Premises that were wrong or need a correction
- The pass criterion above (icon-without-circle at near-zero circle radius).
- "The icons fade in after the fan is in place and the photo icons are gone": in my S22 base clip the dimming starts during the spread and the flag does not dim; which glyphs dim varies by clip (above).
- Nothing wrong found in the planner's code facts.

## Four disclosures
- **Confirmed vs inferred.** Confirmed by measurement: the base fault (n = 1 per condition), and that test A removes it. Inferred: the mechanism inside MapLibre (restarting fade), and that the alternation is the same cause (consistent with test A, not isolated). Read in the SDK: that `TransitionOptions` has `enablePlacementTransitions` (javap), not its behaviour.
- **Could not determine.** Why one glyph stays solid; B; what turning the setting off does to the rest of the map; the S26 at 120 Hz.
- **Wrong premises.** See above.
- **Decided beyond scope.** I wrote `measure.py`'s thresholds and flags myself, as the dispatch allowed ("or your own"); I chose animator scale 5 and 1 only as asked; I deleted three of my own scratch caches (1 GB each in `/tmp/ff`) when the disk quota stopped my commands.

## The phone
Start: 1.0.2306+g19f159fd, user 0 ceDataInode 2259049, `forager.db` sha256 `e1188b00f0da4b713e4ef6d6626b90139247a886460e5bf317d39996c8e2b1db`, animator 1.0; data copied read-only to `data-copy/` (tars with `SHA256`). Installs (`adb install -r --user 0`, own builds only): base 1.0.2329+gb6617c5c, then test A 1.0.2329+gb6617c5c.dirty, then the base again. After each install the three values read back: inode 2259049, db hash `e1188b00...`, the version above. Settings: animator scale set to 5.0 twice and read back as 1.0 after each. End state (now): base build installed, db hash unchanged, animator 1.0, app closed (the reinstall stopped it; no Forager process running). Recordings deleted from the phone after each pull.
