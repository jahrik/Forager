# Backlog device check, Part B (landscape, the Maps drawer and the pickers), on the S22 Ultra: run record

**Status: complete, with one half-item not run and one question for the planner.** The session ran from 09:45Z to 10:37Z.
Every item has a verdict. The pre-registration was pushed at `e37f8b8` before any item ran. It is kept unchanged as the
Appendix. One deviation from it is recorded: item 37's force-stop half could not be run (see "Needs a decision").

**Date:** 2026-09-28, 09:45Z to 10:37Z.
**Device:** Samsung SM-S908U, serial `R5CT321008R`, the only device attached (`adb devices -l`).
`ro.build.id=BP2A.250605.031.A3`.
**Build under test:** `versionName=1.0.1192+g24589349`, `versionCode=1192`, at the start (`01-dumpsys-package-start.txt`, 09:46Z) and
at the end (`223-version-end.txt`, 10:36Z). Nothing installed; no Gradle, no emulator.
**Dispatch:** `prompts/preserved/2026-09-28-14.md`. Part A's `2026-09-28-04.md` and `2026-09-28-12.md` apply except where it
differs. The intent is `2026-09-28-14` in `RECORD.md`, the planner's; I did not touch `RECORD.md`. No planner message
arrived during the run.
**Base:** `fe0f0d7`, the commit the dispatch names. By the time the worktree was added, `origin/journal-redesign` had
moved to `af846cc`, the L0b coder's merge of `fe0f0d7` with its completion report (one docs file). This branch is cut
from `fe0f0d7` (see Decisions).
**Code citations** are at the installed build's commit `2458934`.
**Evidence:** `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-backlog-b/`, outside the repository, indexed with sha256 in
its own `evidence-index.md` (hashed below). No screenshot, dump, coordinate, note text, place name or photo is in this
file. Where the screenshots show the owner's map area, photos or coordinates, they stay in that directory.
**dp to px:** `wm density` 450, so 1 dp = 2.8125 px. The landscape window is 2316 x 1080 px, 823.5 x 384 dp (the
configuration reads `w823dp h384dp`).
**Navigation mode:** `navigation_mode=0`, three-button, unchanged throughout.

## Outcome

| Item | What | Verdict |
|---|---|---|
| 1 | Build, baseline, rotation facts | **pass**: build as dispatched; crash buffer empty at 09:46Z and 10:36Z; rotation facts below |
| B3 1 | Drawer on the right at ROTATION_90, clear of the bar, rounded edge to the map | **pass** |
| B3 2 | Scrim tap closes, and it reopens, at both rotations | **pass**: 5 of 5 at 90, 6 of 6 at 270 |
| B3 3 | Swipe closes while open; no swipe-to-open while closed | **pass** at both rotations |
| B3 4 | Rotating 90 to 270, and 270 to 90, with the drawer open | **pass**: it stays open and moves to the new port edge |
| B3 5 | Predictive back | **not run**: gesture navigation |
| B3 6, J5 38, L8 | No control in the cut-out inset; the cap and centring | **pass** at both rotations, on every non-Map destination; the column is 640.0 dp, margins 41 and 40 px |
| 7 | Landscape picker maps (observation) | offline picker **not usable in landscape**: once scrolled, the map fills the viewport and its OK row cannot be reached by a drag. Find picker usable but short (640 x 128.7 dp). Entry report: the map covers the whole column with no header |
| 39 | Height table and card count | recorded; **fewer than six whole cards**: 2 whole with full-size cards, 4 whole with the short draft cards |
| 40 | Header reveal and dismissal; dropdown closing with the header (observation) | recorded. The icon reveals and hides it. **Back does not always hide it first**: on a single-type chip the first Back steps the chip to All |
| 41 | Hide-on-scroll on a slightly taller list (observation) | recorded: a 150 px drag hides the chip row and a 150 px drag back returns it |
| 42 | Long-press on a sideways card near the bottom edge | **pass**: menu inside the window; dismissed with Back |
| 31 | Long-press on a tile near the bottom edge | **pass**, on the L0a find tile |
| 43 | Import from the L1 row, turning while the picker is up | **pass** |
| 44 | Rotating with an entry open in its editor, both ways; mid-find-edit | **pass** for both; the rows are unchanged in the database |
| 45 | Chips at font scale 1.0 and 1.15 in landscape | **pass**: fit at 1.0; overflow by about 12 dp at 1.15, and the row scrolls |
| 46 | 18 dp chip icons (observation) | legible, by my reading |
| 47 | Details sheet insets at both rotations | **pass** |
| 48 | Sheet actions below the fold; content scrolls | **pass** for the track sheet; the waypoint sheet's Directions shows without scrolling |
| 49 | Drag-to-dismiss | a drag down from the handle **dismissed** it. The predictive Back animation is **not run** |
| 53 | Rotating with the sheet open | **pass** |
| 34 | Find picker: zoom in, pan, wait 10 s | **pass at zoom 17.1**, the basemap's ceiling. Zoom 18 cannot be reached on Topographical. adb cannot pinch; double taps were used |
| 35 | First-fix follow | **pass, weakly**: the untouched pin moved with the first new fix, by 1e-4 degree. The check cannot show a move from elsewhere (see the item) |
| 36 | Offline picker after a slow first fix | run, but **the precondition was not produced**: the cold-start command returned 0 and GPS fixes kept arriving each second. Nothing moved after the pan; the slider kept the pan and re-zoomed; OK showed the panned point |
| 37 | Download kept while the list is reopened; force-stop mid-download | **completion half pass**. The leave-and-return half and the force-stop half **not run**: the one download allowed finished in about 4 s. No force-stop was sent |
| Owner items | Feel, animation, TalkBack speech, predictive back and gesture navigation, the wide tree, folds | **not attempted** |

**The planner's predictions:**
1. "The drawer passes 1 to 4 at ROTATION_90": **held**.
2. "No control lies within the cut-out inset at either rotation": **held**.
3. "Fewer than six whole cards show in the landscape Entries list": **held**. The Entries list holds only two entries, so the count was made on the drafts list (item 39).
4. "Fixes are arriving, and 35 passes": **held**, with 35's weakness stated.
5. "No crash": **held**. The crash buffer read 0 bytes at the start and the end. The app stayed process 19584 throughout.

## Step 1: build, baseline, rotation facts: **pass**
- **Crash buffer:** 0 bytes at 09:46:16Z (`02-crash-start.txt`) and at 10:36:49Z (`222-crash-end.txt`), read with `-d`.
  `logcat -c` was never run.
- **Rotation facts** (`11-window-displays-r1.txt`, `-r3.txt`, `11-window-r1.txt`, `-r3.txt`), with `accelerometer_rotation 0`:

  | | ROTATION_90 (`user_rotation 1`) | ROTATION_270 (`user_rotation 3`) |
  |---|---|---|
  | Window | 2316 x 1080 px, 823.5 x 384 dp, `mOverlappingWithCutout=false` | the same |
  | Status bar | `[0,0][2316,84]`: 84 px, 29.9 dp | the same |
  | 3-button bar | `[2181,0][2316,1080]`: right edge, 135 px, 48.0 dp | `[0,0][135,1080]`: left edge |
  | Cut-out inset | left, 75 px (26.7 dp); bounding rect `(0,512)-(75,568)` | right, 75 px; bounding rect `(2241,512)-(2316,568)` |
  | Charger port | right edge, **inferred** | left edge, **inferred** |

- **The port's edge is inferred, not read.** Android reports no port position. The inference is `portEdgeFor`
  (`ui/adaptive/PortEdge.kt:29-33`, the natural-orientation bottom edge), and it agrees with the 3-button bar's side
  here. The cut-out's side is read from `displayCutout` (`sideHint=LEFT` at 90, `RIGHT` at 270).

## Step 2: the B3 Maps tools drawer
**Item 1 (ROTATION_90): pass.**
- **Tools** on the rail (the rail is `[1956,84][2316,1080]`: 225 px, 80 dp, of rail, plus the bar's 135 px) opened the sheet
  (`21-drawer-open-r1.xml`, `.png`).
- **The sheet** spans x 1303 to 2316: 1013 px, 360 dp, against the right edge. Its scrim, "Close navigation menu",
  is `[0,0][1303,1080]`.
- **Clear of the bar:** every node inside it ends at x 2181 or less, the 3-button bar's left edge. That covers "Trip Planner",
  its expand icon, the search-options close and Settings. The sheet's own inset padding puts its content beside the bar.
  The sheet's background runs under the bar.
- **Rounded edge:** by my reading of `21-crop-top-edge.png`, the sheet's top-left corner is rounded, facing the map.

**Item 2: pass at both rotations.**
- **At 90** (`drawer_cycle.sh`; `22-r1-scrim*-closed.xml`, `-reopened.xml`):
  - five scrim taps, at (60,150), (650,110), (1250,540), (650,1000) and (300,700), spread across the scrim;
  - each closed the drawer: no scrim node in the next dump;
  - Tools reopened it each time.
  - The map mode read the same after the taps, so no tap reached a map control under the scrim.
- **At 270:**
  - the sheet is `[0,0]` to x 1013, and its scrim is `[1013,0][2316,1080]`;
  - one scrim tap at (1700,600) closed it (`27-r3-closed.xml`);
  - then five cycles, at (1100,150), (1700,110), (2200,300), (1700,1000) and (1400,700): each closed, and each reopened
    (`28-r3-*`).

**Item 3: pass at both rotations.**
- **At 90:**
  - `swipe 1600 600 2250 600 300` (towards the sheet's edge) closed it (`23-r1-swipe-close.xml`);
  - with it closed, `swipe 1940 600 1200 600 300`, from beside the rail across the map, did not open it (`24-…`: no scrim).
  - The map image changed across 81% of a sampled region between `20-maps-r1.png` and `24-…png`. That is consistent with a
    pan, but the two captures are minutes apart, so the swipe alone is not proven to be the cause.
- **At 270:**
  - `swipe 700 600 150 600 300` closed it;
  - `swipe 380 600 1100 600 300` did not open it (`29-r3-*`).

**Item 4: pass, both ways.**
- **90 to 270 with the drawer open** (`25`, `26-r3-after-turn.xml`, `.png`):
  - the drawer is still open, now `[0 … 1013]` on the left (port) edge, with its content from x 135, beside the bar;
  - by my reading of `26-r3-after-turn-small.png`, its rounded edge faces right, to the map;
  - the focused window stayed `42e6367`, so the Activity was not recreated.
- **270 to 90** (`30`, `31-r1-after-turn.xml`): still open, back on the right. A scrim tap then closed it (`32`).

**Item 5:** not run (gesture navigation).

## Step 3: cut-out clearance and the cap (B3 6, J5 38, L8): **pass**
`cutout.py` lists every clickable, focusable, checkable or scrollable node outside the rail, and flags any that overlaps the cut-out band. The band is x 0 to 75 at 90 and x 2241 to 2316 at 270.

| Destination | ROTATION_90 violations | ROTATION_270 violations | Dumps |
|---|---|---|---|
| List | 0 | 0 | `40-list-r1`, `50-list-r3` |
| Seasonal | 0 | 0 | `41`, `51` |
| Journal, Entries, Timeline | 0 | 0 | `42-journal-r1`, `54-timeline-r3` |
| Journal, Entries, Album | 0 | 0 | `43-album-r1`, `53-entries-r3` |
| Journal, Records: All, Finds, Tracks, Waypoints, Offline maps | 0 on each | 0 on each | `44`, `45-r1-chip-*`, `55-r3-chip-*` (each chip's `checked` confirmed) |

- **The L1 row** at 90: the switch is `[161,84][744,219]`, 30 px (10.7 dp) inside the column's start at x 116; its
  icons end at x 1871. At 270, the switch starts at x 446 and the icons end at x 2156. Neither is near the cut-out.
- **The cap and centring:**
  - the content column is `[116 … 1916]` at 90 and `[401 … 2201]` at 270, both 1800 px, **640.0 dp**, on every destination;
  - at 90, the margins are 116 − 75 = 41 px to the cut-out inset and 1956 − 1916 = 40 px to the rail;
  - at 270, they are 401 − 360 = 41 px to the rail and 2241 − 2201 = 40 px to the cut-out inset;
  - so the column is centred in what is left, within 1 px.
- **Not a cut-out finding, noted:** the app-wide search field's node starts at y 74, 10 px above the status bar's bottom
  (y 84), on List at 90 (`40-list-r1.xml`). The same 10 px was in Part A's portrait dumps. It is the text field's node, and
  whether anything drawn sits there was not judged.

## Step 4: item 7, the landscape picker maps (observation)
- **The Offline Maps picker** (Records, Offline maps, at 270; `60` to `64`):
  - **On open:** the map node is `[401,605][2201,1080]`, 169 dp of its 480 dp showing. It is 640 x 480 dp (4:3 by width,
    `AvailabilityOfflineMapsUi.kt:174-186`, `:269`).
  - **Scrolling:** two drags on the instruction text scrolled the panel; the Records chip row hid on the scroll. Then the
    map filled the whole viewport, `[401,219][2201,1080]`, 306 dp tall (`62`, `63-small.png`).
  - **Stuck:** no part of the panel outside the map was left to drag. A vertical drag on the map panned the map (75% of
    its pixels changed), and the panel did not move. **So OK, the name, the radius and "Download Maps" cannot be reached
    by a finger in landscape once the panel is scrolled that far.**
  - **The way out:** Back left the chip for All (`64`).
  - **Not usable in landscape, by this reading.** Items 36 and 37 were therefore run in portrait (see Decisions).
- **The find picker** (the draft find's "Change Location", at 270; `73-findpicker-r3.xml`):
  - its map is `[401,468][2201,830]`, **640 x 128.7 dp**;
  - its "Pin at:" line, OK and Cancel are below it, all on screen;
  - the Records chip row stays above the form, which is part of why the map is short;
  - **usable, but short**, by my reading.
- **The entry map preview** (the L0a entry's report, at 270 and at 90; `170` to `175`):
  - the report came up with its map covering the whole content column, from y 65 (19 px into the status bar) to the
    bottom, 640 x 361 dp showing, with **no header row**: no "Back to Cartography" and no "Entry options" in the dump;
  - a clickable node at the bottom right, `[2010,1037][2156,1080]`, is cut off by the window's bottom, with 15 dp of it showing;
  - by the code, the header row is left out only when `isMapFullscreen` is true (`CartographyEntryReportScreen.kt:310`);
  - I opened it twice with a single tap on the card, and it came up this way both times, and the same at 90;
  - Back returned straight to Entries;
  - **why it opens like this was not investigated** (see Flags).

## Step 5: J5 items 39 to 46, the Journal in landscape
**39: the height table (at 270; `100`, `104`, `105`), and the card count.**

| Measure | Device (px) | Device (dp) | Robolectric, J5 (dp) |
|---|---|---|---|
| Status bar | 0-84 | 0-29.9 | 0 |
| L1 row | 84-219 | 29.9-77.9 (48.0) | 0-48 |
| L3 row (drafts chip, view toggle) | 219-354 | 77.9-125.9 (48.0) | 52-92 toggle |
| Content area | 354-1080 | **258.1 dp** | 288 |
| Month header "SEPTEMBER 2026" | 377-434 | | |
| First card row (the L0a card) | 480-844 | 170.7-300.1 (129.4 tall) | 140-228 |

- **How many cards show:** the Entries list has two entries, a 48 dp collapsed row and the L0a card, both whole.
  - **The drafts list** (Continue, "✎ 5 drafts ›"; `105-drafts-list-r3.xml`, `105-small.png`) has five short draft cards, 249 px
    (88.5 dp) tall on a 272 px pitch. Four are whole, in two rows ending at y 1001, and the fifth shows 56 px of 249.
  - With cards the size of the L0a card (129.4 dp), the 213 dp from the first row's top to the window's bottom holds one whole
    row, which is **2 whole cards**.
  - **Six whole cards (P:327) does not hold on this phone** with either card size. The planner's prediction 3 held.

**40: the header (observation).**
- **Reveal:** on Records, the L1 row's Search icon brought the app-wide header up, `[520,74][2178,209]` at 270, and the L1 row
  moved down to 212-347. The icon became "Hide search" (`130`).
- **The icon hides it:** on Entries, "Hide search" hid the header (`140`).
- **Back hides it, but not always first:**
  - on Entries, one run hid it with the first Back (`143` to `144`);
  - in another, the first Back after a reveal had no visible effect, and the second hid it (`139`, `141`, `142`). I did not find
    why;
  - **on the Finds chip, the first Back stepped the chip to All, with the header still up, and the second Back hid it**
    (`135` to `137`). The J5 report says the header's `BackHandler` "outranks" the Records one (`journal-j5-completion-report.md:85`).
    Here it did not.
- **The dropdown:**
  - with the header revealed on Finds, a tap on the search field opened the dropdown (radius, month, recent searches) and the
    keyboard (`131`);
  - the "Hide search" icon was then covered, and not in the dump, so the icon could not hide the header with the dropdown open;
  - Back 1 closed the keyboard, and Back 2 showed no visible change (the dropdown covers the chips, so a chip change could not
    be seen);
  - after Back 3, the dropdown and the header were both gone, and the chip read **All**, not Finds (`132` to `134`);
  - so the dropdown did close when the header hid, as J5's `LaunchedEffect` intends (`AvailabilityCompactScaffold.kt:701-702`);
  - that one of those Backs stepped the chip under the dropdown is **inferred** from the before and after states.
  - Nothing was chosen in the dropdown, and the search still reads "September · 5 mi".
- **The feel is the owner's.**

**41: hide-on-scroll (observation).**
- **Which list:** no list here is only slightly taller than its viewport. Waypoints ends 7 px short of the window (`112`),
  and the drafts list and Entries have no hiding row. I used the **Finds gallery** (the Log sub-tab). Its tiles in landscape
  are 843 px (300 dp) square, two to a row, so the list overflows by about 113 dp, inferred from the tile width.
- **What happened** (`114` to `116`):
  - a slow 150 px drag up hid the chip row, and the Log | Drafts tabs moved from 376-511 to 219-354;
  - a 150 px drag down brought it back.
- **Seen in passing:** after Back from the scrolled Offline maps panel to All, the chip row stayed hidden with All at its
  top (`64` to `66`). A drag down on a list already at its top consumes nothing, so it did not return the row. It came back
  only after a drag up and then a drag down (`67`). See Flags.

**42 and 31: long-presses near the bottom edge: pass.**
- **42, a sideways card** (`120`, `121`):
  - the lowest draft card, `[446,1024][1290,1080]` (partly off the bottom), was held 800 ms at (868,1052);
  - its Edit and Delete menu opened at `[446,568][761,838]`, inside the window and clear of the bar and the status bar;
  - Back dismissed it, and the drafts list stayed (`122`).
- **31, a tile:**
  - the L0a "DEVICE CHECK find 1" tile in Records, Finds, `[1313,556][2156,…]`, which runs past the bottom edge, was held
    800 ms at (1734,1040);
  - its menu opened at `[1313,263][1628,533]`, inside the window;
  - Back dismissed it (`125`, `126`).
- **Not used:** the album's bottom-row tiles, `[446,874]…` at 270. They are the owner's photos, not test data (see Decisions).

**43: Import with a turn while the picker is up: pass** (`150` to `157`).
- **The route:** Album view, the L1 row's photo button (`[2021,84][2156,219]`), then its menu, "Take photo" and "Import", then
  Import at 10:24:02Z.
- **The picker:** `com.google.android.photopicker/com.android.photopicker.MainActivity` was top-resumed.
- **The turns:** `user_rotation` 3 to 1, then 1 to 3. The picker stayed top-resumed through both. This order is the
  reverse of the pre-registration's 1 to 3 to 1, since the phone was at 270 when the item began.
- **Back, and a permission prompt:**
  - Back cancelled the picker, and behind it was a permission prompt, "Allow Forager to access photos …", with "Allow limited
    access", "Allow all" and "Don't allow";
  - it was dismissed with Back, choosing none;
  - `ACCESS_MEDIA_LOCATION` and `READ_MEDIA_VISUAL_USER_SELECTED` read `granted=false` with the same flags as at the start (the
    two permission blocks' md5s match).
- **Back on the app:** the Album view, three tiles; `files/photos` still holds 3 files and `files/captures` is empty. Nothing
  was imported.

**44: rotating with an entry open in its editor: pass, both entries, both ways.**
- **The draft find**, opened from Records, Finds, Drafts (1) (`70` to `72`):
  - turned 3 to 1 and 1 to 3;
  - the form stayed open after each: "Find on 2026-09-28", "Change Location" and the identification field reading the
    DEVICE CHECK label;
  - the focused window stayed `42e6367`.
- **The blank saved entry,** opened through its row's long-press menu, then Edit (`101` to `103`):
  - the editor shows its date, 2026-09-28, both text fields empty, and no "Finish entry", so it is the committed entry;
  - turned 3 to 1 and 1 to 3, it stayed open with both fields empty, and the window stayed the same;
  - Back left it with no prompt (`104`).
  - The pre-registration named "Save" as a marker; no "Save" is on screen in this editor, so the date, the blank text and
    the missing "Finish entry" were used instead.
- **The database:** nothing was typed in either. The md5 of the draft find's row, the blank entry's row and every
  `cartography_entries` row is the same at `db-start` (10:04Z), `db-after-find`, `db-after-editor` and `db-end` (`rows.sh`).

**45: the chips in landscape at 1.0 and 1.15: pass** (at 270; `160`, `161`, `162`).

| Chip | 1.0 (dp) | 1.15 (dp) |
|---|---|---|
| All 7 | 79.6 | 82.8 |
| Finds 2 | 98.1 | 105.2 |
| Tracks 1 | 104.9 | 113.4 |
| Waypoints 3 | 133.0 | 146.1 |
| Offline maps 1 | 147.9 | 164.3 |

- **At 1.0,** the last chip ends at x 2064, inside the 640 dp column (x 2201), so the row fits.
- **At 1.15,** it ends at x 2200, past the row's end padding. A swipe on the row moved it to end at x 2167 (`162`), so the
  row overflows by about 12 dp and scrolls.
- **By my reading of `161-chips-fs115-r3-crop-chips.png`,** every label and count is whole on one line. Each chip keeps a 48 dp
  touch height.
- Font scale went to 1.15 at 10:25:14Z and back to 1.0 at about 10:26Z, read back `1.0`.

**46: the 18 dp chip icons (observation).**
- The short-window metrics set Material's 18 dp chip icon (`RecordsFilterChips.kt:140`, `FilterChipDefaults.IconSize`).
- The drawn glyphs measure roughly 11 to 17 dp across inside that box, from `160-chips-fs100-r3.png`. It is a crude
  pixel-extent measure.
- **By my reading of the 2x crop `160-chips-x2.png`,** in dark, each icon is legible: the list, the leaf, the track line, the
  pin and the folded map.

## Step 6: J5c items 47, 48, 49 and 53, the details sheet in landscape
**47: pass.**
- **The waypoint sheet** ("DEVICE CHECK waypoint"; `181` at 90, `182` at 270) is `[258 … 2058]`, 640 dp, centred on the window.
  - At 90, it ends 123 px short of the bar (x 2181) and starts 183 px past the cut-out inset (x 75).
  - At 270, it starts 123 px past the bar (x 135) and ends 183 px short of the cut-out inset (x 2241).
  - Its drag-handle box starts at y 314, below the status bar.
- **The track sheet** (at 270; `185`) is taller. Its drag-handle box is `[1091,84][1226,219]`, starting exactly at the status
  bar's bottom, y 84.
- **Noted:** the sheet is centred on the window, not on the content column. So it overlaps the rail by 102 px at both
  rotations (x 258 against the rail's 360 at 270, and x 2058 against 1956 at 90). The rail is under the scrim then.

**48: pass for the track sheet.**
- **The track sheet:** on open, its actions start at y 1043, cut off by the window's bottom (`185`).
- **A slow 350 px drag up** inside the sheet scrolled its content, and "Share" came fully on screen at `[303,900][621,1035]`
  (`186`). Share was not pressed.
- **The waypoint sheet's** "Directions" is on screen when it opens (`[303,900][707,1035]`), since its content is short.

**49:** a drag from the handle, `swipe 1158 381 1158 1060 500`, **dismissed the waypoint sheet** (`183`: no "Close sheet").

**53: pass.** With the waypoint sheet open, `user_rotation` 1 to 3 at 10:29:30Z. The sheet was still open on the same
waypoint, with the same bounds (`182`).

## Step 7: picker items 34, 35 and 36
**The fix-arrival control: pass** (`74-foragerfix-1.txt`, `74-dumpsys-location.txt`, 10:05Z).
- **The log:** `logcat -d -s ForagerFix` shows a GPS fix about every second, at 11 to 12 m accuracy (1,921 gps and 111 network
  lines in the buffer).
- **The gate:** GPS fixes at that accuracy pass the live-fix gate, `LIVE_FIX_MAX_ACCURACY_METERS = 50f` (`LiveFixGate.kt:68`).
  Network fixes at 100 m do not.
- **The service:** `dumpsys location` lists the app's requests at `@+1s0ms` on its providers.

**35: pass, weakly.**
- **The first try went wrong:**
  - location off at 10:06:07Z raised a Google Play services screen, "No location access"
    (`com.google.android.gms/.location.settings.LocationOffWarningActivity`), on top of the app (`76`, `78`);
  - I dismissed it with Back, choosing none of its buttons (`79`).
- **The second try:**
  - location off at 10:08:48Z raised no such screen, so the Back I sent for it left the find form instead;
  - the draft was kept, "Drafts (1)", and no Discard was pressed (`80`, `81`);
  - I reopened the draft and its picker with location still off (`82`, `83`).
- **Location off:** four reads over 13 s gave the same "Pin at:" (`83-pinwatch-locoff.txt`).
- **Location on** at 10:09:52.6Z:
  - the first fixes came at 10:09:54.4Z (network, 15 m) and 10:09:59.2Z (gps);
  - the untouched pin moved by 1 × 1e-4 degree of longitude, about 8 m, by the 10:09:59Z read, and then held for 55 s while fixes
    kept arriving (`84-pinwatch-locon.txt`, `84-foragerfix-after-on.txt`: 61 gps, 5 network).
- **Why this is weak, as pre-registered:** `liveFix` kept the last fix while location was off, so the picker opened at the
  device already. What was seen is an untouched picker following a new fix by GPS jitter. A move from a stale place to the
  device could not be produced in this process.

**34: pass at zoom 17.1, not 18.**
- **adb cannot pinch:** `input motionevent` takes one pointer. Double taps were used instead, sent as two `input tap`
  processes 0.08 to 0.12 s apart.
- **The zoom was measured from a slow `motionevent` pan of 900 px (320 dp)** and the change in "Pin at:" longitude, as
  zoom = log2(360 × 320 / (512 × Δlng)) (`zoomcalc.py`):
  - about 13.1 at the start, which is `zoomForRadiusKm(1)`, 13;
  - 14.12 after one double tap (`87`, `88`);
  - 17.10 after four more (`89`, `90`);
  - two further double taps changed nothing: the map images were pixel-identical (`93`, `94`).
- **Zoom 17 is the ceiling:** the picker draws on the Topographical basemap, OpenTopoMap, whose `maxZoom` is 17
  (`ui/map/Basemap.kt:155`, set with `setMaxZoomPreference`, `SightingsMap.kt:516`). **The source's "about 18" cannot be
  reached on this basemap.** I did not change the map mode (see Decisions).
- **The pan:** at 17.1, a pan of 900 px moved the pin 16 × 1e-4 degree of longitude (`91` to `92`, 10:12:47Z). Its opening point
  lies 125 × 1e-4 degree of longitude away (about 1 km), from all the pans together.
- **10 s later** (`93`, 10:13:00Z) **and 33 s later** (`95`):
  - "Pin at:" is unchanged, and the map area is pixel-identical to the capture just after the pan (0 changed pixels);
  - 14 GPS fixes at 12 to 15 m arrived in the first 13 s, and 82 more after that.
- So the view did not drop to zoom 13 and did not return to the device, with fixes arriving. The picker was left with its
  own **Cancel** (`96`), and the draft find's row is unchanged.

**36: run, but the check could not have failed.**
- **Where:** in **portrait**, because the landscape panel's OK row cannot be reached (item 7).
- **The cold start:** `cmd location providers send-extra-command gps delete_aiding_data` at 10:31:47.8Z returned 0. But GPS
  fixes kept arriving every second at 13 to 14 m, with no gap. **No cold start took effect,** and the offline picker's first
  fix also races the network provider (`AndroidLocationProvider.kt:23-52`).
- **The pan:** the chip was opened, and a slow pan followed 0.6 s later. It moved the pin by −65 and +92 × 1e-4 degree
  (`192`, `194`). For 20 s after, "Pin at:" and the map were unchanged (`195`, `196`: 0 changed pixels).
- **The radius slider** (`197` to `201`):
  - from 1 mi to 3 mi (5 km), the pin was kept and the map did not change (39 px). That radius is in the same zoom band,
    13 for up to 5 km (`SightingsMap.kt:1507-1508`);
  - to 6 mi (about 10 km), the pin was kept and the map re-zoomed (69% of the visible strip changed);
  - back to 3 mi, then OK: "Download region:" equals "Pin at:" exactly.
- **Verdict:** the parts after the fix pass. The slow-first-fix precondition was not produced.

## Step 8: item 37: the completion half **pass**; the reopen half and the force-stop half **not run**
- **The download:** the pick from item 36, 3 mi (5 km), estimated "~248 tiles", named `DEVICE CHECK 2026-09-28 B`
  (`202-named.xml`). "Download Maps" was tapped at 10:35:23Z.
- **It finished at once:** the first status read, at 10:35:26Z, already showed no "Downloading" line, and "No location picked
  yet", the post-success state (`210`, `211`). The Room row's `createdAtEpochMillis` is 10:35:27.5Z, so the download took
  about 4 s.
- **So two halves could not be run:**
  - there was no time to leave the chip and return while it ran;
  - **no force-stop was sent,** since `am force-stop` after completion would test nothing, and the dispatch allows one
    download only.
- **The completion half: pass.**
  - The chip reads "Offline maps 2".
  - The list shows "DEVICE CHECK 2026-09-28 B", "3 mi … — 244 tiles, 3.1 MB — downloaded just now", under the L0a region,
    with a tile budget of 261 / 6000 (`214`).
  - Room `offline_regions` holds id 2 with `radiusKm=5` (`db-after-download`).
  - MapLibre's `mbgl-offline.db` holds region 2 with 244 tiles, beside region 1's 17 (`mbgl-after1/`, read from a copy).
- **Needs a decision:** see below.

## Settings

| Setting | Start (09:46Z, `03-settings-start.txt`) | Changes | End (10:36:49Z, `224-settings-end.txt`) |
|---|---|---|---|
| `system accelerometer_rotation` | `0` | none | `0` |
| `system user_rotation` | `0` | 1, 3 and back, many times; 0 at 10:30Z for items 36 and 37 | `0`; display `ROTATION_0` |
| `system font_scale` | `1.0` | 1.15 at 10:25:14Z, 1.0 at about 10:26Z | `1.0` |
| Location (`cmd location`; `secure location_mode`) | `true`; `3` | off 10:06:07Z, on 10:06:54Z; off 10:08:48Z, on 10:09:52.6Z | `true`; `3` |
| `secure navigation_mode` | `0` | none | `0` |
| Dark mode (`cmd uimode night`; `secure ui_night_mode`) | `yes`; `2` | none | `yes`; `2` |
| GPS aiding data | — | `delete_aiding_data` requested once, 10:31:47.8Z (it had no visible effect) | — |
| App permissions (`ACCESS_MEDIA_LOCATION`, `READ_MEDIA_VISUAL_USER_SELECTED`) | `granted=false`, flags as listed | the prompt was dismissed with Back | the same (`156`) |
| The app's screen | the Maps tab (`10-arrival.xml`) | — | the Maps tab, "Map mode: Topographical … Night mode off." (`221-final.xml`) |
| The Journal's Entries view (app state) | Timeline | Album at item 43 | **Album**, not restored |
| The offline picker's remembered pick (`map_preferences`) | the L0a region | overwritten by the new download (`setLastPickedRegion`, `AvailabilityViewModel.kt:1133`; the file's mtime is 10:35Z) | the new region's centre and radius |

- `/sdcard/blb-ui.xml`, the dump file this run wrote, was removed (`ls`: no such file).
- **The app log** for PID 19584 is saved (`225-logcat-app-pid.txt`). It was read with `-d` only.
  - This session's slice holds 48,753 lines, 42,658 of them the MapLibre `getMetersPerPixelAtLatitude` errors.
  - The other W and E lines are graphics-allocator noise, 34 `Mbgl-LocationComponent`, 32 `Mbgl-HttpRequest` and 17
    `WindowOnBackDispatcher` lines.
  - None of them is a Forager exception.

## Created data
All of it is left in place. Read from `db-end` (10:36Z) against `db-start` (10:04Z):

| What | How | Label |
|---|---|---|
| Offline region, 5 km (shown as "3 mi"), 244 tiles, 3.1 MB; Room `offline_regions` id 2, MapLibre region 2 | item 37's one download, in portrait | `DEVICE CHECK 2026-09-28 B` |

- **No draft find was created:** item 44's mid-find-edit and items 34 and 35 used the existing DEVICE CHECK draft find,
  whose row is unchanged.
- **The side effect in Settings:** the offline picker's remembered pick now points at the new region.
- **Unchanged:** saved entries 2, drafts 5, saved finds 2, draft finds 1, photos 3, tracks 1, waypoints 3,
  `cached_searches` 2.
- **Nothing was deleted.** No Cancel or Discard was pressed on the draft find, and no Delete was chosen in any menu.

## Decisions I made
1. **Cut the branch from `fe0f0d7`, not from the moved `origin/journal-redesign` (`af846cc`).**
   - The dispatch names "the commit carrying this file", and its `git worktree add` line assumed that commit was the tip.
   - When the tip moved, they disagreed. The difference is one L0b docs file and no app code.
   - Deciding this properly needed the planner to say which of the two it meant.
2. **Proceeded without the kit's record steps,** as Part A did. `.claude/kit.json` does not exist at this base, and the
   dispatch says the planner owns `RECORD.md`.
3. **Put the evidence in `…/device-evidence/2026-09-28-backlog-b/`,** by analogy with Part A's directory. The dispatch
   inherits Part A's directory rule but names no Part B directory.
4. **Ran items 36 and 37 in portrait.** The landscape offline panel's OK row cannot be reached by a finger (item 7).
   Neither source item names an orientation.
5. **Ran 34 at zoom 17.1, the basemap's ceiling, rather than switching the map mode to Street** (OSM, `maxZoom` 19) to reach
   18. Changing the map mode was not among the settings I may change. Deciding it properly needs the planner's word on
   whether 34 must run at 18.
6. **The substitute for a pinch:** double taps, as two `input tap` processes about 0.1 s apart. The zoom was measured
   from the pin's change over a pan of known length, which is my method.
7. **Used the existing draft find** for 34, 35 and 44's mid-find-edit, instead of creating the one extra draft find the
   dispatch allows. Opening a new draft has no DB write on leave (`MushroomLogViewModel.kt:582-630`); the row's md5 is
   unchanged.
8. **For 31, used the L0a find tile, not the album's bottom-row tiles,** which are the owner's photos.
9. **For 41, used the Finds gallery,** since no list here is only slightly taller than the window.
10. **For 39, counted cards on the drafts list,** because the Entries list holds two entries.
11. **Dismissed the Play services "No location access" screen and the photo-permission prompt with Back,** choosing none of
    their buttons.
12. **Did not send the one allowed force-stop after the download had finished,** since it would test nothing. See
    "Needs a decision".
13. **The download's size:** 3 mi (5 km), about 248 tiles, chosen to last long enough to leave and return. It did not.
    "Small" was my reading.
14. **Left the Journal's Entries on the Album view** at the end, rather than going back to restore Timeline.

## Flags outside scope
1. **The landscape Offline Maps panel traps the user.** Once scrolled so that its 480 dp map fills the 306 dp viewport, no
   drag reaches OK, the name, the slider or Download. Back is the only way out (item 7).
2. **The entry report opens in landscape with its map covering the column and no header row,** and a control cut off at the
   bottom-right edge. It also draws 19 px into the status bar (item 7). The cause was not investigated; whether it is
   `isMapFullscreen` or something else is **unverified**.
3. **Back with the header revealed on a single-type chip steps the chip before hiding the header,** against the J5 report's
   stated order. On Entries, one run needed two Backs (item 40).
4. **The Records chip row can stay hidden** after Back lands on a list at its top. A drag down cannot bring it back, since
   hide-on-scroll reads consumed scroll. If that list could not scroll at all, it would be **inferred** to stay hidden (item 41).
5. **The find picker's map is 128.7 dp tall in landscape,** partly because the Records chip row stays above the form.
6. **The "Saved to Drafts" snackbar is centred on the window in landscape,** `[242,911][1998,1046]` at 270, so it overlaps
   the rail and its Tools item (`97`). This is J4 item 27's landscape half, which neither part's dispatch ran.
7. **The details sheet overlaps the rail by 102 px** at both rotations, being centred on the window, not the column.
8. **Turning location off raised a Play services "No location access" screen once** (not the second time). A user turning
   location off mid-picker lands in another app's screen.
9. **Inferred from the code, not run:** the live-fix collection registers only providers enabled when it starts
   (`AndroidLocationTracker.kt:71-74`), and it is not restarted while it is still active. So a process that comes to the
   foreground with location off might get no fixes after location is turned on, until it next leaves the foreground. This
   run turned location off and on with the collection already registered, which does not test that case.
10. **The details sheet still reads "Used in: no journal entries"** for the DEVICE CHECK waypoint (`182-small.png`), as Part A
    flagged.
11. **The Map tab's MGRS/coordinates readout node** starts at y 43, inside the status bar's 84 px, at 90 (`20-maps-r1.xml`).
    It is on the Map tab, which step 3 excludes.
12. **The MapLibre error stream continues:** 42,658 `getMetersPerPixelAtLatitude` lines in this session.

## Needs a decision
**Item 37's force-stop half.**
- A 244-tile download finished in about 4 s, so neither "leave and return while it runs" nor "force-stop mid-download" could
  be done. The dispatch allows one download and one force-stop, and the one download is used.
- The options I can see:
  - (a) allow a second, larger download, labelled and left in place or cleaned up by the orphan path. Roughly 2,000 tiles
    or more, within the 6,000 budget (261 used), would likely give 30 s or more (inferred from 244 tiles in about 4 s);
  - (b) accept 37 as completion-only;
  - (c) throttle the network during a download, which needs a setting not on the list.

## Premises that were wrong
- **"Pinch in to about 18"** (PF:301-304, dispatch step 7): the Topographical basemap stops at 17.
- **"36 is not run if a cold GPS start cannot be forced from adb":** the command exists and returns 0, but GPS kept fixing
  every second. And a slow fix would not make the offline picker's first fix slow, since that fix races the network provider.
- **"37: force-stop mid-download"** with "one small offline-region download": a small download here finishes before a
  force-stop can be sent.
- **"How many cards show" in the landscape Entries list:** the list holds two entries.
- **B3x "at 640 dp":** confirmed. The column is 640.0 dp on every destination.

## Evidence index
Every capture, log and database copy is listed with its sha256 in `evidence-index.md` in the evidence directory (328 files).
That file's own sha256 is given below. Derived crops, downscaled copies and `*-query/` working copies are not indexed. The
helpers `snap.sh`, `nodes.py`, `cutout.py`, `drawer_cycle.sh`, `pinwatch.sh`, `pindelta.py`, `pan.sh`, `zoomcalc.py`,
`dbcopy.sh`, `rows.sh` and `dlstat.sh` are in the directory and in the index. `snaps.log` records each capture's UTC window
and hash prefix.

| File | sha256 |
|---|---|
| `evidence-index.md` | `e579e3dcf3eb54594d651250d01e603894df8998ae8aeaef030b49f90642310a` |

---

# Appendix: the pre-registration, unchanged from `e37f8b8`

# Backlog device check, Part B (landscape, the Maps drawer and the pickers), on the S22 Ultra: run record

**Status: pre-registration only.** Nothing below has been run yet. This file is committed and pushed before any item
is looked at, so the order of prediction and observation is checkable. Results are added in a later commit; this
section is kept unchanged as the Appendix.

**Date:** 2026-09-28, from 09:45 UTC.
**Device:** Samsung SM-S908U, serial `R5CT321008R`, the only device attached (`adb devices -l`, 09:45:46Z).
`getprop` at 09:46:16Z: `ro.product.model=SM-S908U`, `ro.build.id=BP2A.250605.031.A3`.
**Build under test:** the installed build, `versionName=1.0.1192+g24589349`, `versionCode=1192`,
`lastUpdateTime=2026-09-27 19:18:29` (`01-dumpsys-package-start.txt`, 09:46:16Z). It matches the dispatch. Nothing
installed; no Gradle, no emulator.
**Crash buffer at the start:** `logcat -b crash -d` read 0 bytes (`02-crash-start.txt`, 09:46:16Z). The app is
process 19584, the same process as in L0a and Part A. The phone was unlocked (`isKeyguardShowing=false`) with
`MainActivity` in focus.
**Settings at the start** (`03-settings-start.txt`): `font_scale=1.0`, `accelerometer_rotation=0`, `user_rotation=0`,
`navigation_mode=0` (three-button), `ui_night_mode=2` (`cmd uimode night`: yes), `location_mode=3`, location enabled.
**Dispatch:** `prompts/preserved/2026-09-28-14.md`, with Part A's `2026-09-28-04.md` and `2026-09-28-12.md` applying
except where it differs. The intent is `2026-09-28-14` in `RECORD.md`, the planner's. I do not touch `RECORD.md`.
**Base:** the dispatch names "`origin/journal-redesign` at the commit carrying this file", which is `fe0f0d7`. By the
time the worktree was added (09:45Z), `origin/journal-redesign` had moved on to `af846cc`, the L0b coder's merge
of `fe0f0d7` with its completion report (one file, `docs/audits/2026-09-28-map-layers-l0b-completion-report.md`).
This branch, `device-backlog-b-2026-09-28`, is cut from `fe0f0d7`, the commit the dispatch names (see Decisions).
**Code citations** are at the installed build's commit `2458934`, as in Part A, read with `git show 2458934:…`.
**Evidence:** outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-backlog-b/`, cited
by name with sha256. No screenshot, dump, coordinate, note text, place name or photo is in this file.
**dp to px:** `wm density` 450, so 1 dp = 2.8125 px; `wm size` 1080 x 2316 px, so the landscape window is
823.5 x 384 dp before insets.

# Pre-registration: pass conditions and predictions, written before each item was looked at

The item numbers are those of `docs/audits/2026-09-28-device-backlog-compilation.md`. Abbreviations are that file's.
Every touch is a real `adb shell input` touch at screen coordinates from a fresh dump or screenshot. "Sampled across
its bounds" means at least five touches spread across the control's own bounds, as in Part A.

## Step 1: build, baseline, rotation facts
- **Pass:** `versionName=1.0.1192+g24589349` (read above); `logcat -b crash -d` read at the start and at the end, a
  new Forager line being a stop.
- **Rotation facts to record** at `user_rotation 1` (ROTATION_90) and `3` (ROTATION_270), with
  `accelerometer_rotation 0`: from `dumpsys window displays`, the display rotation, the cut-out rect, the status-bar
  and navigation-bar frames; the window size in dp. **The charger port's physical edge cannot be read from any
  dumpsys**: Android exposes no port position. I will infer it from `portEdgeFor` (`ui/adaptive/PortEdge.kt:29-33`:
  the natural-orientation bottom edge, the window's right at ROTATION_90 and its left at ROTATION_270) and say it is
  inferred. The cut-out's edge is read.
- **Prediction:** the build matches (already read) and the crash buffer stays empty. At ROTATION_90 the cut-out is on
  the left and the 3-button bar on the right; at ROTATION_270 the reverse. The window is 823.5 x 384 dp at both.

## Step 2: the B3 Maps tools drawer, items 1 to 4 (B3d:190-197)
Code: `AvailabilityScreen.kt:1506-1571` at `2458934`. In a short landscape window the drawer's layout direction is
set so that its start edge is the port edge (Rtl when the port is on the right), `gesturesEnabled =
drawerState.isOpen`, and the sheet is `ModalDrawerSheet`. Tools on the rail opens it.
- **Item 1 (ROTATION_90). Pass:** the sheet sits against the right edge of the window, on the rail and 3-button bar
  side; none of its interactive content lies within the navigation-bar frame read in step 1 (each clickable node's
  bounds compared against that frame); its rounded corners are on its left edge, facing the map (by my reading of a
  screenshot crop, said so).
- **Item 2. Pass:** at each rotation, a tap on the scrim (the map area outside the sheet) closes the drawer (no sheet
  nodes in the next dump), and Tools then reopens it. The claim is about touches, so the scrim taps are sampled: five
  close-and-reopen cycles at each rotation, each at a different scrim point spread across the scrim's bounds.
- **Item 3. Pass:** with the drawer open, a horizontal swipe on the sheet towards its own edge (rightward at
  ROTATION_90) closes it; with it closed, a swipe from near the rail's inner edge across the map towards the centre
  does not open it (no sheet nodes after), and the map may pan instead.
- **Item 4. Pass:** with the drawer open at ROTATION_90, `user_rotation 3` leaves a drawer that is either still open,
  now against the left (port) edge with its rounded edge facing right, or closed cleanly with Tools still working;
  no crash, and no sheet stranded half-open or on the wrong edge. I record which. The source asks only that it be
  exercised ("the direction around it changes while open"), so a clean close is recorded, not called a fail.
- **Item 5,** predictive back, is **not run**: it needs gesture navigation, and the navigation mode may not be changed.
- **Prediction:** 1 to 4 pass at ROTATION_90 (the planner's prediction 1), and 2 and 3 at ROTATION_270 too. For 4, the
  drawer stays open and moves to the left edge, since `drawerState` is remembered and only the direction changes.

## Step 3: cut-out clearance and the cap, B3 items 6 and 38, J5 L8 (B3x:210-214, J5:263-266)
Code: `shortLandscapeContentInsets()` (`AvailabilityNavigationUi.kt:238-241`: `statusBars` top, `displayCutout`
horizontal, `ime` bottom) on every tab but Map; the rail takes the `navigationBars` inset on its own side (`:209`);
the content is capped at 640 dp (`READABLE_CONTENT_MAX_WIDTH`, `AvailabilityResultsUi.kt:306`) and centred in what
the rail leaves (`AvailabilityCompactScaffold.kt:653-668`).
- **Destinations:** List, Seasonal, Journal Entries (Timeline and Album), Journal Records (All, and each chip), at
  ROTATION_90 and ROTATION_270. Tools is the drawer (step 2), and Map is excluded by the source.
- **Pass (L8):** no clickable or focusable node's bounds overlap the cut-out band, the strip of the window between the
  cut-out edge and the cut-out inset (from step 1), at either rotation. This includes the L1 row's Entries | Records
  switch (row start), its search icon and its action button (row end). A node is compared by its bounds from a fresh
  dump.
- **Pass (the cap and centring):** the content column is 640 dp wide (1800 px, within 3 px) and its left and right
  margins, measured to the cut-out inset on one side and to the rail's inner edge on the other, are equal within 3 px.
- **Prediction:** pass at both rotations (the planner's prediction 2). The column is 640 dp with about 14 dp either
  side, from 823.5 dp less the rail (80 dp plus the 48 dp bar inset) and the cut-out inset (about 27 dp).

## Step 4: item 7, the landscape picker maps (B3x:215-216), an observation
- **What is recorded:** for the Offline Maps picker (Records, Offline maps), the find's location picker (the draft
  find's form, "Change Location", left with Cancel) and the entry map preview (the L0a entry's report), each map's
  bounds in dp from a dump, and whether it is usable: whether it is on screen whole or needs scrolling to reach, and
  whether OK and Cancel can be reached. By my reading of screenshots, said so.
- **Code:** the offline picker's map is 4:3 by width (`AvailabilityOfflineMapsUi.kt:174-186`, `:269`), so at 640 dp
  wide it is 480 dp tall, taller than the 384 dp window; the find picker's map is `weight(1f)` of what is left
  (`CentrePinLocationPicker.kt:142-148`).
- **Prediction:** the offline picker map does not fit the window (about 640 x 480 dp inside a scrolling panel), so
  its OK row needs a scroll; the find picker map is about 640 x 250 dp and usable; the entry preview reads.

## Step 5: J5 items 39 to 46, the Journal in landscape
- **39 (J5:267; the height table at J5:151-163). Record:** at each rotation, the L1 row, the L3 row and the content
  area in dp, and the first card rows' bounds. **How many cards show:** the Entries list holds two saved entries, the
  L0a card and the blank collapsed row, so it cannot show six. The five drafts' list ("Continue" through the drafts
  chip) draws with the sideways cards too (J5 L4: "the drafts list takes it too"), so the count is made there: whole
  and partial cards, and the number of whole two-card rows the content area holds, from the row pitch. **Pass
  (P:327):** six or more whole cards would show given the cards. **Prediction:** fewer than six whole (the planner's
  prediction 3): two whole rows, four whole cards, and part of a third row, with the status bar taking about 27 dp
  more than Robolectric's zero.
- **40 (J5:268). Record:** the L1 row's search icon reveals the app-wide search header (it appears in the next dump),
  and a second touch on it ("Hide search") and Back each hide it. The dropdown: with the header revealed, its search
  dropdown opened (nothing chosen), then the header hidden by the icon: **pass** if the dropdown's nodes are gone
  with the header (`AvailabilityCompactScaffold.kt:698-702`, the `LaunchedEffect` J5 added). The feel is
  the owner's.
- **41 (J5:269). Record:** on a list only slightly taller than its viewport (the drafts list, or Records All, whichever
  overflows by less), a small scroll down hides the second row (the L3 toggle row or the chip row) and a scroll up
  brings it back, and whether the list can still reach its end. The animation's feel is the owner's.
- **42 and 31 (J5:270, J4b:325). Pass:** a long-press (`motionevent` DOWN, hold 800 ms, UP) on the sideways card or
  tile lowest on screen opens its Edit and Delete menu, and the menu's bounds lie inside the window, above no system
  bar; dismissed with Back, nothing chosen. On a draft card or the L0a card, never on an owner's record.
- **43 (J5:271). Pass:** Album view, the L1 row's photo button opens Take photo and Import; Import opens the system
  picker (package from `dumpsys activity`); with the picker up, `user_rotation` 1 to 3 and back to 1; then the picker
  is cancelled with Back, and the app is back on the Journal's Album view with no crash and nothing imported
  (`files/photos` count unchanged). The `ACCESS_MEDIA_LOCATION` prompt Part A found behind the picker is dismissed
  with Back, choosing neither button, as Part A did.
- **44 (J5:272, P:323). Pass:** with the blank saved entry open in its editor (through its long-press menu's Edit),
  `user_rotation` 1 to 3 and then 3 to 1: the editor is still open after each, on the same entry (same date, blank text,
  "Save" not "Finish entry"), and leaving it with Back gives no prompt. Then the draft find open in its form
  (Records, Finds, its Drafts), the same two rotations: the form stays open with its identification label intact.
  Nothing is typed in either. The focused window's id is read before and after each turn, since rotation is not
  expected to recreate the Activity (J1:126-129). The database is compared before and after (counts and the two rows).
- **45 (J5:398-399). Pass:** on Records at font scale 1.0 and 1.15, landscape, each chip's label is whole on one line,
  and if the row overflows the capped width it scrolls sideways. Widths in dp recorded.
- **46 (J5:400), an observation:** the chip icons' measured size and, by my reading of a 2x crop, whether each is
  legible in the current (dark) theme.
- **Prediction:** 40's dropdown closes with the header; 41 hides and returns; 42's menu is inside the window; 43 and
  44 keep their state; 45 passes at 1.0 and scrolls at 1.15.

## Step 6: J5c items 47, 48, 49 and 53, the details sheet in landscape (J5c:319-328)
Code: `RecordDetailsSheet.kt:162-182`, a `ModalBottomSheet` with `skipPartiallyExpanded = true`, its content a
`verticalScroll` column. Its insets are Material's defaults (material3 `1.5.0-alpha26`; which insets those are is
unverified: I have no source for that version here).
- **The row:** "DEVICE CHECK waypoint" in Records, Waypoints (L0a test data), a tap on its body; and the owner's
  track row, a tap only (the sheet opens; nothing else is done with it).
- **47. Pass:** at both rotations, no clickable node of the sheet lies within the navigation-bar frame or the cut-out
  band from step 1, and the sheet's top is below the status bar.
- **48. Pass:** the sheet's actions (Directions for the waypoint, Share for the track) are below the visible part when
  it opens, and a swipe up inside the sheet brings them on screen (the content scrolls). Neither action is pressed.
- **49. Record:** a downward drag from the drag handle dismisses the sheet (no sheet nodes after), or not. The
  predictive Back animation is **not run** (gesture navigation).
- **53. Pass:** with the sheet open, `user_rotation` 1 to 3: the sheet is still open, on the same record, after the
  turn.
- **Prediction:** pass for 47, 48 and 53; a drag down dismisses (49).

## Step 7: picker items 34, 35 and 36 (PF:299-310, ODR:5-11, emulator-check-attempt.md:18-19)
The find picker is the draft find's form, "Change Location" (`JournalTab.kt:364-375`); it is always left with
**Cancel**, never OK, so the draft find is not changed. Its region is `findLocationPickerRegion(deviceLocation, …)`
(`FindLocationPickerRegion.kt:21-22`), the device's live fix at 1 km, from `AvailabilityUiState.liveFix`, which the
ViewModel collects from `AndroidLocationTracker` (GPS and network, 1 s; `AndroidLocationTracker.kt:71-74`). Each fix
is logged under the tag `ForagerFix` with its provider (`:53-60`), so the logcat, read with `-d`, shows fixes
arriving.
- **The fix-arrival control first. Pass:** with the find picker open, `dumpsys location` shows recent fixes for the
  app's registrations, and `logcat -d -s ForagerFix` shows new lines, with their providers, during the check.
- **35, the first-fix follow. Pass (PF:305-307):** location off, the picker opened, location on; untouched, the
  picker moves to the device when the fix arrives (its "Pin at:" text changes to the new fix, and the map with it).
  **A weakness, stated before looking:** `liveFix` keeps the last accepted fix while location is off (nothing clears
  it; `AvailabilityViewModel.kt:233-244`), and this process has had fixes since 02:18Z. So the picker opens already at
  the device's last fix, and the "move" on the next fix is only GPS jitter on a phone lying still. The check can
  only see that "Pin at:" keeps following new fixes while untouched; it cannot see a move from somewhere else to the
  device. Nothing here can empty `liveFix` except a new process.
- **34, pinch and pan, then wait. `input motionevent` takes one pointer, so adb cannot pinch** (`adb shell input`
  usage: `motionevent <DOWN|UP|MOVE|CANCEL> <x> <y>`). **Substitute:** double taps at the pin to zoom in (each a
  gesture MapLibre reports as `REASON_API_GESTURE`, `SightingsMap.kt:399-403`), then a slow one-finger pan made of
  `motionevent` DOWN, MOVEs and UP with a pause before UP (no fling). The zoom is measured, not assumed: from a pan of
  a known number of px and the change in "Pin at:" longitude, zoom = log2(360 x (pan in dp) / (512 x Δlng)), the Web
  Mercator scale MapLibre uses. **Pass (PF:301-304):** after the pan, over 10 s with fixes arriving (the control
  above), the view does not drop to zoom 13 and does not return to the device: "Pin at:" stays at the panned value,
  and two screenshots 10 s apart show the same map. Coordinates are kept out of this file; only differences are
  given.
- **36, the offline picker after a slow first fix. Pass (PF:308-310):** opened with GPS cold, a pan before the location
  lands, then nothing moves; the radius slider dragged, the map stays on the panned point and re-zooms; OK and
  "Download region" show the panned point. **Whether it can be run:** a cold start can be requested from adb (`cmd
  location providers send-extra-command gps delete_aiding_data`, listed in `cmd location help`). But the offline
  picker's first fix is `getCurrentLocation()`, which races GPS against the network provider
  (`AndroidLocationProvider.kt:23-40`, `:50-52`), so a cold GPS alone does not make that fix slow; and the picker's
  opening centre is the remembered L0a pick, which lies 0.7 m from the L0a waypoint, beside the phone, so a late fix
  would move it by metres at most. I will send the cold-start command once, open the picker, pan at once, and record
  what happens, saying that the check could not have failed on this data unless the map visibly jumps.
- **Prediction:** fixes are arriving and 35 passes as far as it can see (the planner's prediction 4); 34 passes; 36
  shows nothing moving, on data that could not show a failure.

## Step 8: item 37, a download kept while the Offline maps list is reopened, with one force-stop (PF:311-314)
Code: `MapLibreOfflineMapRepository.kt:107-175` at `2458934`: the MapLibre region is created at the start and the Room
row written only on completion; `listRegions` deletes incomplete regions other than this process's in-flight ones
(`:182-216`, F4). `onDownloadOfflineMaps` (`AvailabilityViewModel.kt:1082-1140`) shows `Downloading(n, m)`.
- **The one download:** at the picker's remembered centre, confirmed with OK, a radius chosen so that the estimate
  label reads a small download that still takes long enough to leave and return, and the name
  `DEVICE CHECK 2026-09-28 B`. **Pass, first half:** while it runs, switching from the Offline maps chip to All and
  back shows "Downloading N / M tiles" still counting up (N larger than before the switch).
- **The force-stop,** once, with `am force-stop com.zynergylabs.forager.app` while N < M. Then the app is relaunched
  from the launcher intent (`monkey -p … -c android.intent.category.LAUNCHER 1`) and Records, Offline maps reopened.
  **Pass, second half:** the list shows the L0a region "DEVICE CHECK" and no partial region; the Room
  `offline_regions` table has one row; MapLibre's own offline database (read with `run-as`, copied, never written)
  holds no incomplete region for the new name.
- **Not run:** the half where the region "appears in the list on completion", since the one download allowed is the
  one force-stopped.
- **Prediction:** pass for both halves run; the partial region is gone after the relaunch.

## Owner items, not attempted
Feel and animation (parts of 40, 41 and 49); TalkBack speech; predictive back (5, 49) and anything else needing
gesture navigation (38's gesture-navigation half, P:326); the wide tree; folds.

## Settings to restore
`accelerometer_rotation` 0 and `user_rotation` 0 (read at the start); `font_scale` 1.0; location on; the Maps tab as
found. Each is read back at the end.
