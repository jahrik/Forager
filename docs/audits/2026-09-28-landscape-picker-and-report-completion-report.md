# Landscape picker and entry report (L1, L2): completion report

**Status: built.** L1 and L2 were built under continuation `2026-09-28-49`, which answered the stop recorded
below. The build is in "Resumed (continuation `2026-09-28-49`)" at the end of this report. The stop sections are kept
as they were written.

**Status at the stop: stopped before any build, on two abort conditions for L1:** an unruled design question and a premise that
turned out wrong. Nothing in `app/` changed. The verification for both bugs is done and recorded below. L2 has no open
question and could go ahead on its own if the planner says so.

**Dispatch:** `prompts/preserved/2026-09-28-47.md`. **Intent:** `2026-09-28-47`, written by the planner. I did not touch
`RECORD.md`. No planner message arrived during the work.
**Base:** `origin/journal-redesign` at `658e7c3`, the commit carrying the dispatch. I checked it against the remote
after `git fetch`. Worktree `forager-wt/landscape-fixes`, branch `landscape-fixes`, created with the dispatch's own
command.
**Kit:** `.claude/kit.json`, `check_record.py` and `check_prompts.py` are not in this tree (the kit was removed by
`c5b2a10` and `e136330`). So there was no `required_sections` list to validate the dispatch against, and I ran no
record steps. The dispatch says the planner owns the record.
**Paths** below are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full. Line numbers are at
`658e7c3`.

## Verification

Both bugs were reproduced headless with a throwaway diagnostic test. It was not committed; it is kept outside the repo
in the session scratchpad. The test drove the real `AvailabilityScreen` at `w823dp-h384dp-land` with a stub map, then
dumped every semantics node's bounds. The build log had 0 `e: ` lines. The window was 823 x 384 dp and the rail sat at
x 743 to 823. Robolectric's default rotation is 0, which `portEdgeFor` maps to Right.

### L1, the Offline Maps picker

- **Cause, confirmed:** the map's height is fixed by its width, and there is no short-window branch.
  - `OfflineMapsPanel` hands the picker `mapAspectRatio = MAP_PICKER_ASPECT_RATIO`
    (`ui/availability/AvailabilityOfflineMapsUi.kt:184`), which is 4:3 (`:269`).
  - The picker applies it as `fillMaxWidth().aspectRatio(4:3)` to the map box (`ui/map/CentrePinLocationPicker.kt:146`).
    In the 640 dp column that makes the map 480 dp tall.
  - The whole panel is one `verticalScroll` column (`AvailabilityOfflineMapsUi.kt:151`), in the `weight(1f)` slot under
    the chip row (`ui/log/RecordsTab.kt:282-283`).
  - OK and Cancel belong to the picker and sit **under** the map (`CentrePinLocationPicker.kt:162-173`). The name, the
    radius slider and Download sit below that (`AvailabilityOfflineMapsUi.kt:188-249`).
- **Seen headless:** the panel's viewport ran from y 104 to 384 (280 dp) and the map from y 208 to the bottom. OK, the
  name, the slider and Download all had zero-size bounds, so they were outside the viewport. Once the panel scrolls so
  that the 480 dp map fills the whole viewport, nothing but the map is left to drag. That matches run-record item 7.
- **Windows affected:**
  - every short landscape window (height under 480 dp, landscape). The content column there is capped at 640 dp, so
    the map is always 480 dp, taller than the window.
  - **Inferred, not run:** any window whose panel viewport is shorter than about three quarters of its width, such as a
    short multi-window window in portrait.
  - Compact portrait (411 dp wide gives a 308 dp map) and the wide layout are not affected.
- **Insets:** none needed. The failure needs no inset. On a device the status bar makes the viewport even shorter.

### L2, the day entry report

- **Cause, confirmed:** the same kind of fixed height, and the overflow is what hides the header.
  - The report is a plain `Column` with no scroll (`ui/log/CartographyEntryReportScreen.kt:347`). Its parent gives it a
    bounded `weight(1f)` slot (`ui/log/CartographyScreen.kt:575-577`).
  - The map box is `fillMaxWidth().aspectRatio(4f / 3f)` (`CartographyEntryReportScreen.kt:400`), so it is 480 dp at
    640 dp wide.
  - Its slot is only what is left under the header row (`:353-383`), 272 dp here. The map measures 480 dp anyway, and
    Compose centres an oversized child on its slot. So the map is drawn about 104 dp **above** its slot. It is drawn
    after the header, so it lands on top of both the header row and the Journal's short-window row.
  - The offline-toggle row (`:507-530`) and the scrolling text column (`:533-538`) get no height at all.
- **Seen headless:**
  - the header's Back and "Entry options" buttons are laid out at y 60 to 100;
  - the map spans y 8 to 384 and covers them;
  - the offline Switch and the report's text have zero-size bounds.

  This is the device's "map covering the whole column, no header row". It also explains the record's "19 px into the
  status bar": the map is drawn above its own slot. That part is inferred; Robolectric has no status bar.
- **Not reproduced: the cut-off control at the bottom right.** The device showed a clickable 52 dp wide with 15 dp of it
  visible. Headless, the offline Switch (52 dp wide) gets zero height instead. That it is the Switch is **unverified**.
- **The header is not hidden by `isMapFullscreen`.** The run record left open whether `:310` was the cause. It is not:
  the header is composed, and the map covers it.
- **Windows affected:** every short landscape window (a 640 dp column gives a 480 dp map). Inferred, not run: any column
  whose height is less than three quarters of its width plus the header row.
- **Insets:** none needed for the failure.

### The planner's predictions

1. "L1's cause is a fixed map height with no short-window branch": **held**.
2. "L2's cause is the same kind of fixed height": **held**. The missing header is that overflow drawn over it, not a
   branch that hides the header.
3. "The suite grows by 6 to 15": **not reached**. Nothing was built.

## Why I stopped (L1)

**1. The ruling's "all reachable without scrolling" cannot hold in this window with every control the stacked layout
has. What to do about that is not ruled.**

- The owner's ruling, as the dispatch quotes it: "radius slider, name, OK/Download on the other, all reachable without
  scrolling".
- The dispatch's build bullet is weaker: "reachable without the map swallowing a drag". It also asks for "whatever else
  the stacked layout has" on the controls side.
- **What fits.** I measured the stacked panel without its map, at 320 dp wide, which is half the 640 dp column.
  Robolectric's text metrics are only approximate, so these numbers are rough.
  - The controls come to about 540 dp:
    - the two instruction lines;
    - "Pin at" and the OK/Cancel row;
    - "Download region";
    - the name;
    - the radius label and slider;
    - the tile estimate;
    - Download;
    - then "Downloaded Maps", the tile budget and the region list.
  - The four named controls alone come to about 350 dp.
  - The controls side gets 280 dp headless, or about 264 dp on the S22 once the status bar is taken off. A scroll hides
    the chip row and gives back about 42 dp (the run record's 306 dp).
  - On the device, with smaller real text, the four named controls stacked are **estimated** at about 312 dp. So even
    they do not fit with the chip row showing.
- **The options I can see.** Each changes something the ruling did not decide.
  - **(a)** The controls side scrolls on its own, beside a full-height map, in the stacked order without the map. Every
    control is reachable by a drag on the controls side, and that drag never reaches the map. This meets the dispatch's
    bullet. It does not meet "without scrolling". The tests would reach Download with a real drag on the controls side
    first.
  - **(b)** As (a), but the controls side is reordered so that OK/Cancel, the radius and slider, and Download come
    first. The instruction lines, "Download region", the tile estimate and the region list go below them. By the device
    estimate that likely fits OK, the slider and Download without scrolling, but not the name while the chip row shows.
    It changes the order of the controls.
  - **(c)** Shorten or drop copy on the controls side, such as the panel's two-sentence instruction. That is new copy
    or a dropped control, which the dispatch's abort list names.
  - **(d)** Give the controls more than half the width, or lay them out in two columns. That is a new layout the ruling
    does not describe.

**2. Which side the map goes on: the landscape plan points both ways.**

- The dispatch says to follow the B2/B3 convention "where the map sits away from the rail".
- The plan has two conventions, and they disagree:
  - **Panels go on the rail side, with the map beyond them.**
    - The search sheet is "anchored to the rail side" (P8, `docs/plans/landscape-phone-design.md:261-270`).
    - The HUD is "on the rail side" (P9, `:272-277`).
    - The tools drawer "opens from the rail side" (P12, `:300-315`).
    - Tiles may draw under the cut-out while controls are padded clear of it (P4, `:220-225`; R17 revised,
      `:608-611`).

    By this reading the map goes on the punch-hole side and the controls go beside the rail.
  - **Controls go on the punch-hole side.** The map's own icon cluster defaults to the punch-hole side, away from the
    rail (P6, `:236-248`; R3). By this reading the controls go away from the rail, and the map goes by it.
- I read the first as closer: it is a panel beside a map, not controls floating over one. But that reading is mine, and
  the dispatch lists "a side the convention does not settle" as a stop.
- In the Records tab the 640 dp column is centred with margins on both sides (run record, B3 6). So neither half
  actually touches the cut-out or the rail. The choice is about direction, not clearance.

**What I need:** a ruling on 1, choosing (a), (b), (c), (d) or something else, and a confirmation of the side in 2. I
also need to know whether L2 should be built now, on its own, while L1 waits.

## Tests-first messages

None. No test was written into the tree. The diagnostic above only measured bounds. It asserted nothing, and it is not
evidence that a failing test would fail for its stated reason.

## What landed, with SHAs

Only this report. No app code, and no test.

## Revert checks

None. Nothing was built.

## Suite

Not run. Nothing in `app/` changed from `658e7c3`, so the planner's baseline at `de8fdd3` (283 / 2303 / 0 / 0 / 24)
still describes the code. I did not re-run it.

## Device-only

Unchanged from the dispatch:

- both screens on the S22 at rotations 90 and 270, with real insets;
- the map's side against the cut-out and the rail.

Also for L2, when it is built: what the bottom-right control cut off on the device actually is.

## D58

`git grep -i` for the three phrases over this report and the commit message, before the push: 0 hits.

## Decisions I made

1. **I stopped the whole dispatch rather than building L2 alone.** L2 does not depend on either open question. But the
   dispatch treats an abort condition as a stop, and it gives no rule for splitting the build. Deciding this properly
   needs the planner's word.
2. **I proceeded without the kit's record steps** (the sweep, an intent, the checkers). `.claude/kit.json` and both
   checkers are not in this tree, and the dispatch says the planner writes the record.
3. **I diagnosed with a throwaway test through `AvailabilityScreen`** instead of reading the layout alone. It was not
   committed. Its source and logs are outside the repo.
4. **I measured at 320 dp for the controls side,** which assumes a half-and-half split. The split itself is not ruled.
   I picked it only to size the question.
5. **I leaned towards the rail-side reading for the map's side** (the map on the punch-hole side). I did not act on it.

## Flags outside scope

- **The find picker has the same shape in a short window.** Its map is `weight(1f)` of what is left
  (`CentrePinLocationPicker.kt:143-144`), so it does not overflow. It is only short: the run record's flag 5,
  128.7 dp.
- **The picker's Cancel does nothing in the offline panel** (`onCancel = {}`, `AvailabilityOfflineMapsUi.kt:180-183`).
  It is visible, and it would stay visible in a side-by-side layout. This is not new.
- **Run-record flag 2's "19 px into the status bar"** is, by my reading, the same overflow as L2, not an inset bug.
  That is inferred; it cannot be checked headless.

## Resumed (continuation `2026-09-28-49`)

Written by the same coder, in the same worktree (`forager-wt/landscape-fixes`, local branch `landscape-fixes`),
pushing to `journal-redesign`. Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless given in full.

**The message** (`prompts/preserved/2026-09-28-49.md`, committed at `aa24247`), verbatim:

> Planner message 2026-09-28-49, part of dispatch 2026-09-28-47. Quote it verbatim in your report. It answers your stop report at `c2ca9d9`. Build L1 and L2.
>
> **Q1, the L1 controls side.** The owner's ruling, verbatim: "Pin OK/Download, rest scrolls (Recommended)". It supersedes "all reachable without scrolling", which cannot hold at 384 dp.
> - The map is full height on one side.
> - On the other side, the actions are pinned at the bottom and always visible: OK, with the Cancel that sits beside it today, and Download.
> - The radius slider, the name and everything else scroll above the pinned actions.
> - A drag on the map pans the map and never reaches the controls.
> - The test must show that OK and Download are displayed and receive real touches, sampled across their bounds, with no scrolling in the test. It must also show that the slider is reachable by scrolling the controls side.
>
> **Q2, the side, ruled by the planner.** The map goes on the punch-hole side, and the controls on the rail side. This matches the landscape search sheet, the HUD and the drawer (P8, P9, P12), which sit by the rail with the map beyond them. Cite those in the code comment. On a rotation between 90 and 270, the sides follow the rail. Test both rotations if the harness can set them; otherwise make it a device item.
>
> **Q3.** Yes, build L2, as dispatched. Record the cap rule and its dp. The cut-off control you could not reproduce stays a device item.
>
> Everything else stands: tests first through the real screens, revert checks, the full suite, and the report. The Cancel that does nothing (`AvailabilityOfflineMapsUi.kt:180-183`) stays a flag, and is not fixed here.

The coordinator's relay of it arrived during my stop hand-back. The committed file above is the text I built to.

### Verification

The verification is the one above, under "Verification". Nothing it depends on moved between `658e7c3` and `aa24247`:
the commits between them touch only `docs/`, `prompts/` and `RECORD.md`.

**Both rotations can be set.** The harness pins the rotation with `ShadowDisplay.setRotation`, as the B3 tests do. Each
rotation test first asserts that the screen read the rotation it pinned. At 90 the rail was measured at the right; at
270, at the left.

### Tests first

The tests were committed at `3fc7bfe` before any production change. They are
`app/src/test/.../ui/availability/LandscapeOfflinePickerTest.kt` (8 tests) and `LandscapeEntryReportTest.kt`
(5 tests). Both drive the real `AvailabilityScreen` at `w823dp-h384dp-land`. `3fc7bfe` was pushed to
`landscape-fixes-wip` only, since it is red by design.

**At base** (`aa24247` plus the tests; results directory cleared; 0 `e: ` lines), each failure had the predicted
reason:

- **OK, Cancel and Download, at 90 and at 270:** "The component with Text … contains 'OK' … is not displayed!" OK is
  below the 480 dp map, outside the viewport.
- **The slider by scrolling, at 90 and at 270:** "could not find any node that satisfies: (TestTag =
  'offline-picker-controls-scroll')". There is no controls side yet.
- **The sides, at 90:** "the map (DpRect(left=52.0.dp, top=208.0.dp, right=692.0.dp, bottom=688.0.dp)) is on the
  punch-hole side, left of the controls (DpRect(left=68.0.dp, top=740.0.dp …". At 270 it is the mirror image, with the
  map at 132 to 772.
- **L2's header:** "the map (DpRect(left=52.0.dp, top=8.0.dp, right=692.0.dp, bottom=488.0.dp)) starts below the header
  row (Back at DpRect(left=72.0.dp, top=60.0.dp …)". The map is drawn over Back.
- **L2's cap:** "the map is 40% of the 384 dp window expected:<153.6> but was:<480.0>".
- **L2's offline switch:** "The component with TestTag = 'cartography-entry-offline-toggle' is not displayed!"

**Checks that passed at base, by design, and are not tests-first:**

- **The drag on the map** pans it and reaches no control. At base the map drag already panned without reaching a
  control. The panel only trapped the user once it had scrolled so far that the map filled the view.
- **The two portrait pins** (the picker stacked with a 4:3 map; the report's 4:3 map).
- **The bubble on the capped map.** A real touch on a glyph opens its bubble, with one opening-frame request.

These four passed before the change and pass after it. By CLAUDE.md that makes them suspect as evidence of this
change. They guard against regressions and prove nothing about the fix.

**One test was changed after base.** The first slider test made three fixed-length drags. In the built layout that
overshot the slider: it ended at -27 dp, seen by instrumenting the test. It now makes short real drags on the
scrolling part until the slider lies wholly inside it, at most eight, and asserts that at least one drag happened. At
base it still fails on the missing tag, before any drag. The revert check below reproduces that failure.

### What landed

`62d1d1d`, "L1 and L2 built". `3fc7bfe` and `62d1d1d` went to `landscape-fixes-wip` first, and to `journal-redesign`
after the suite.

**L1, `ui/availability/AvailabilityOfflineMapsUi.kt`:**

- **The window test:** `isShortWindow()` and `ORIENTATION_LANDSCAPE`, the same two tests B1 to B3 and J5 use.
- **The layout:** `OfflineMapsSideBySide`, a `Row` in two equal halves.
  - The map takes one half, at full height.
  - The other half is a scrolling column above a pinned bottom:
    - **scrolls:** the panel's instruction, the picker's instruction, "Download region", the name, the radius label and
      slider, the tile estimate, the download status, then the divider and "Downloaded Maps";
    - **pinned:** the "Pin at" line with OK and Cancel, then Download.
- **The side:** `controlsFirst` is `currentWindowPortEdge() == ScreenEdge.Left`. So the map is on the punch-hole side
  and the controls by the rail, and the sides follow the rail between 90 and 270. P8, P9 and P12 are cited in the KDoc.
- **Portrait and the wide layout** keep the stacked panel, in the same order as before.
- **One pin across layouts:** the pin state is remembered once in the panel. The map is a `movableContentOf`, placed
  by whichever layout shows, so turning the phone moves the MapView rather than rebuilding it.

**L1, `ui/map/CentrePinLocationPicker.kt`:**

- The picker's four `remember`s moved, unchanged, into a `CentrePinState` holder (`rememberCentrePinState`).
- Its body is now three composables: `CentrePinInstruction`, `CentrePinMap` and `CentrePinConfirmActions`.
- The public `CentrePinLocationPicker` composes them in the same order, so its signature and layout are unchanged.
  `CentrePinLocationPickerTest` and `OfflineMapsPanelPickerTest` pass unchanged.

**L2, `ui/log/CartographyEntryReportScreen.kt`:**

- **The rule:** in a short window (`isShortWindow()`, any orientation), the map is 4:3 by its width but no taller than
  **40% of the window height** (`SHORT_WINDOW_MAP_HEIGHT_FRACTION`). That is **153.6 dp** at 384 dp.
- **Why 40%:** the Journal's 48 dp row and the 64 dp header take 112 dp. A 154 dp map leaves about 118 dp for the
  offline row and the start of the text. Half the window would leave the text about 16 dp.
- **Rejected:** capping the width as well, to keep 4:3.
- **The mechanism:** a layout modifier, `shortWindowEntryMapHeight`, that can never measure taller than its slot. It is
  not `heightIn` on `aspectRatio`, because `aspectRatio`'s unconstrained fallback is the overflow itself.
- **Fullscreen and portrait** keep their branches.
- **The opening frame:** the camera request is unchanged, one per screen instance (checked). The fit it asks for is
  resolved against the preview's size, so on a device the smaller preview opens at a lower zoom, 48 dp in from a 154 dp
  tall view. That zoom is not checkable headless, and it is a device item.

### Revert checks

The runner:

- saves a copy of the file before editing;
- makes a one-line edit, which must match exactly once;
- runs the named classes from a cleared results directory;
- refuses to cite results if the build log has any `e: ` line;
- restores the file from the saved copy, not from git;
- then confirms the tree equals HEAD, so the forward change is still present.

All three builds compiled (0 `e: ` lines), and every restore was confirmed.

1. **L1, the side-by-side layout off** (`if (false && isShortWindow() …`): 6 of 8 failed.
   - The failures are the four tests-first failures, at both rotations, with the base messages word for word.
   - The drag check and the portrait pin passed. Neither could be failed by this edit.
2. **L2, the cap off** (`else if (false && shortWindow)`): 3 of 5 failed.
   - The failures are the header ("top=8.0.dp … Back … top=60.0.dp"), the cap ("expected:<153.6> but was:<480.0>") and
     the switch ("is not displayed!").
   - The bubble check and the portrait pin passed.
3. **The side swapped** (`== ScreenEdge.Right`): 2 of 8 failed, the two side tests only.
   - At 90: "the map (… left=372.0.dp … right=692.0.dp …) is on the punch-hole side, left of the controls (…
     left=68.0.dp …".
   - At 270: the mirror image.
   - Each failure names this edit's own geometry: the map is beside the controls, on the wrong side.

### Suite

At `62d1d1d`, from a cleared results directory, with `LC_ALL=C.UTF-8` and `./gradlew --offline :app:testDebugUnitTest`.
Counts are from the JUnit XML, and the build log had 0 `e: ` lines:

**285 suites, 2316 tests, 0 failures, 0 errors, 24 skipped.** Against the planner's `de8fdd3` baseline
(283 / 2303 / 0 / 0 / 24), that is +2 suites and +13 tests, the two new classes. Prediction 3, "grows by 6 to 15",
**held**. No held flaky test failed in this run.

### Device-only

- **Both screens on the S22 at rotations 90 and 270, with real insets.**
  - The status bar and the cut-out shorten the controls side. Headless it is 280 dp, 172 dp of it pinned.
  - How much of the scrolling part is left on the device is unknown.
- **The map's side against the cut-out and the rail.**
  - Headless, the sides follow the rail at both rotations.
  - On the device, the 640 dp column is centred, so neither half touches the cut-out band.
- **Turning the phone with the picker open.** The pin state is shared and the map is movable content, so the pan and
  zoom should survive, but no test turns the phone mid-test. **Not run.**
- **L2's opening frame on the 154 dp preview:** its zoom, and whether 48 dp of padding leaves enough of the day's
  records in view.
- **L2's cut-off control** from the Part B run: what it was, and whether it now shows.
- **L2's bubbles on a 154 dp preview.** Headless, the bubble opens, but a real bubble taller than the preview could be
  clipped by it. That was not measured.

### D58

`git grep -i`-equivalent counts for the three phrases, over each commit's staged diff and message before each push
(`3fc7bfe`, `62d1d1d` and this report): 0 every time.

### Decisions I made (continuation)

1. **The "Pin at" line is pinned with OK and Cancel.** The ruling names OK, its Cancel and Download. "Pin at" is part of
   the same confirm row today (`CentrePinConfirmRow`), so I kept that row whole rather than splitting it.
2. **The split is half and half.** The ruling does not give one.
3. **Scroll order:** the stacked order, less the map and the pinned actions. The picker's own instruction line sits
   under the panel's instruction, where it was.
4. **The picker was refactored into a state holder and three pieces**, rather than written a second time. The public
   picker's layout and signature are unchanged. It is a change to working code that the find pickers share.
5. **The map is movable content.** This keeps the MapView and its camera across a turn. The dispatch did not ask for
   it; I added it for CLAUDE.md's UX default, that what the user set survives. Not verified by any test.
6. **L2 uses `isShortWindow()` in any orientation.** The dispatch says "a short window". L1 uses short and landscape,
   because the ruling is about landscape.
7. **The cap is 40% of `screenHeightDp`,** with the reasoning above. The dispatch left the rule to me.
8. **The slider test was changed after base,** as described under "Tests first".
9. **The tests-first commit went to `landscape-fixes-wip`,** not `journal-redesign`, because it is red by design. Both
   commits went to `journal-redesign` after the suite passed.

### Flags outside scope (continuation)

- **Two lines of copy are now wrong in landscape.** The panel's instruction says "Pan the map **below**", and "No
  location picked yet — pan the map **above** and tap OK." Side by side, the map is beside them. I did not change
  them, because new copy is unruled.
- **Cancel still does nothing** in the offline panel (`onCancel = {}`), as ruled: a flag, not fixed.
- **Right-to-left layouts:** `Row` follows the layout direction, while `ScreenEdge` is absolute. In an RTL locale the
  map would land by the rail. The app does not appear to support RTL today; I did not check that.
- My first JUnit reader treated an XML element with no children as false and dropped the failure lines. The totals
  line caught it: 2 failures with none listed. It was fixed before any result was cited.
