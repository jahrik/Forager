# Landscape picker and entry report (L1, L2): stopped at verification

**Status: stopped before any build, on two abort conditions for L1:** an unruled design question and a premise that
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
