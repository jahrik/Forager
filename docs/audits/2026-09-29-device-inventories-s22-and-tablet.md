# Device inventories for the S22 session and the tablet session (read-only, at `52f12f93`)

**Recorded by:** the planner, from a read-only helper's hand-back, for dispatch 2026-09-28-227 (device check Part 3). It is condensed, with the citations kept. Line numbers are at `52f12f93`.

**Citations** refer to reports under `docs/audits/`:
- L = landscape-l completion
- P2inv = part-2 device inventory
- RR = part-2 run record
- F1 = part-2 follow-ups map
- BK = journal-backup completion
- F3 = kept-track-path
- F4 = marker-fanout
- C1 = chrome-colour
- J6 = j6 completion
- LJ = 2026-09-28 leaving-the-journal fixes
- -NNN = a RECORD.md entry

**Markers:** (DATA) creates or deletes data, or restores; (NET) needs the network; (OWNER) is judged by the owner.

**Sundown is out of scope.** F5's items are added when its report exists; at this commit it does not.

## A. The S22 (portrait, 90 and 270)

### A1. The landscape L at 90 and 270, with Part 2's deferred 38, 39, 40 and 42
1. The L against the real insets, cut-out and rail. Its top is at or below the real search bar's bottom. Record the real search-bar and strip heights. [L:144, :166, :254]
2. The empty corner and the 8 dp gap draw nothing, and a real long-press there reaches the map. (OWNER for the look) [L:144]
3. Real fingers across each button's full 48 dp square, corners included: all five bar rows, both pill buttons, and the old lost slivers.
   - A point 2 dp outside a square reaches the map, including 6 dp from the physical edge.
   - The handle's 20×48 box takes no touch from the compass or Layers rows.
   [L:144, :173, :175-177, :257]
4. A search notice with the L on the notice's side. The notice is inset by 8 + the L's width + 8, the bounds are disjoint, the text wraps readably, and the L does not move. [L:169-172, :255; RR:199]
5. An open SearchDropdown over the L: record what covers what. [L:214, :256]
6. P2 38: at 90 the coordinate caption clears the L. Read the L-to-search-bar margin, which was 14 px before the L. [P2inv:87; RR:144]
7. P2 39, for the L:
   - drag, snap and both handles;
   - record and return reachable while recording;
   - thumb reach. (OWNER)
   (DATA: recording) [P2inv:88-94; RR:169]
8. P2 40: the legend inboard of the L, on each side, collapsed and expanded. The legend against the dropdown, and the first-compose jump. [P2inv:95; RR:145]
9. P2 42: the L drags down to the nav inset; before the L, its "+" covered the "i" at 90. Portrait still stops above the legend. [P2inv:100; RR:146, :198]
10. C1: the rail and the L read as one colour. (OWNER) [C1:142]

### A2. Bubbles and stacked markers
11. **Re-check of fail 5** (F1 1): rotate 0→90→0 with a photo bubble and a find bubble open. Each stays on its glyph and clears the cluster or L. Also re-anchor on zoom. [F1:165; RR:136, :193]
12. P2 4: bubbles clear the real strip, nav, rail and cut-out at 0, 90 and 270. [P2inv:32; RR:192]
13. P2 3: the tail tip on the glyph at the top, bottom and right edges, and at 90 and 270. [RR:135]
14. P2 1: hit tests on track cells and colour-field cells. [RR:190]
15. F4 1-3:
    - two photos at one spot fan in a ring in 0.4 s, with legs, and the map does not move;
    - with a photo over a find, a tap on the find opens its bubble;
    - nine or more make a spiral with no overlaps.
    (DATA) [F4:105]
16. F4 and -208: sighting dots never fan. Records over dots fan and the dots stay put. A dot tap is unchanged. (NET) [F4:129, :137]
17. F4 5-7:
    - the fan folds on a map tap, a pan, a pinch and Back, and Back closes it before a bubble, fullscreen or the drawer;
    - at separating zoom there is no fan;
    - with animations removed, it appears already spread.
    [F4:105]
18. F4 8, 9 and 12:
    - fanned copies keep their halos;
    - leg and copy colours at night (OWNER);
    - a stack under a thin track line.
    [F4:105]
19. F4 10 and -208, at 0, 90 and 270 with the real bars:
    - a fan near each edge, and beside the cluster or L, the legend, the chip row, the nav and the rail. Every marker stays on screen and clear;
    - drag and snap the cluster, then fan beside it, to check the keep-out bounds follow the drag;
    - with the HUD up;
    - a very large stack.
    (DATA) [F4:105, :202, :213-214]
20. Observe: the entry map and the centre-pin picker have no keep-out registry. [F4:215]

### A3. Chrome, the "i", tracks and downloads
21. **Re-check of fail 37** (F1 2): portrait fullscreen, with 3-button and gesture navigation. The "i" sits above the nav bar and a tap opens attribution. Also look at MapLibre's logo band. [F1:166, :116; RR:196]
22. F1 3: track widths at zoom 12, 14, 16 and 18, the casing and halo, and the breadcrumb dots at low zoom. This replaces P2 11. (OWNER) [F1:167; RR:165]
23. F1 4 and C1: "Download this area?":
    - its wording, and the radius in the units setting;
    - Cancel and Download, at 0, 90 and 270;
    - readable at 0.8 over the picker map.
    (DATA, NET for Download) [F1:168; C1:139]
24. C1: over topo and aerial, day and night, the search bar and panel, strip, chips, legend, icon bar, bubbles and bottom nav read as one colour, against the owner's screenshot. Also:
    - the bar and pill against the container, in light;
    - the drawer, Layers and details sheets over their scrims;
    - a snackbar with an action.
    (OWNER, NET) [C1:136-138, :140]
25. P2 51: the Offline maps Records sheet in short landscape and in portrait. [P2inv:109; RR:201]
26. P2 12: refocus after clearFocus(), and the auto-scroll at a large font scale in short landscape. [P2inv:42; RR:166]
27. P2 53: rings on ORIGIN and END. (DATA) [P2inv:117; RR:202]
28. P2 52, the rest: "In <find>", ">3 entries", and switched off. (DATA) [RR:346]
29. Session 1's partly-run halves:
    - 10: pan, then open and close a find;
    - 36: bearing and tilt (OWNER);
    - 44: the entry map at night;
    - 47: the Layers chips in landscape;
    - 48: the landscape Records sheet's nav band;
    - 58 and 59: by day, and the entry report at night;
    - 60: by day.
    [RR:147-205]

### A4. Journal flows at 90 and 270
30. P2 14 and 15. (DATA) [P2inv:46-47]
31. P2 17: the withheld-waypoint half at 0, 90 and 270, and the new-entry half at 90 and 270. (DATA) [RR:317]
32. P2 18 with F1 8, at 0, 90 and 270. (DATA) [F1:172; RR:318]
    - A dirty editor, then a night-mode toggle: the editor is there, with **no** "Welcome back".
    - Background the app with a dirty editor and return: the prompt appears.
33. P2 22-26. Item 25 uses the camera; see A5. (DATA) [P2inv:59-63]
34. P2 33's other states: the editor, clean and dirty; the report; the Finds chip; the album; Settings over an entry; the drafts list; entry map fullscreen; the pull-photo picker. [RR:333, :349]
35. P2 32 from the entry map's fullscreen. [RR:349]

### A5. Camera and Gallery
36. F1 9, **unfixed, investigate:** the camera round trip in landscape. Read `user_rotation` and the display rotation before, during and after. (DATA) [F1:93-99, :173; RR:329, :361]
37. F1 7: save a photo with a time, then read `datetaken` and the Gallery app's date. Also P2 61's capture half and 62 in landscape. (DATA) [F1:171; RR:206-207]

### A6. Destructive and restore items, last. The session then restores from its verified copy.
*Inferred, not re-read on the device:* the phone is at 1.0.1685 with user_version 16, so the install is F3's migration check itself.

38. F3: install over v16. The app reads user_version 17, integrity ok, and row counts equal. (DATA) [F3:141]
39. F1 5, track delete. (DATA) [F1:169]
    - A swipe in Tracks and in All, Undo, and the timeout.
    - Waypoints detached.
    - Delete on the details sheet.
    - A recording track offers none.
    - An entry that kept the track still lists it.
40. F3: keep a track in a finished entry, then delete the track. (DATA) [F3:138-139, :142]
    - The entry map draws the saved line, and the card thumbnail stays.
    - The bubble shows name, distance and duration, with no date and no Details.
    - The same with a draft. A withheld track draws nothing.
    - Delete the entry, and no path data remains.
41. F3: Replace and Merge onto a phone without the track. The entry still draws the line. (DATA) [F3:140]
42. F2 4 and -187 flag 1: a restore onto the same phone (Replace, then Merge twice) makes no duplicate or "Not downloaded" regions. Then a Replace from a backup lacking one of the phone's regions: what happens to its tiles? (DATA, NET) [BK:657, :664]
43. F2 5 and -187 flag 2: a Replace with a find's report open closes it. With a track, waypoint, trip or region detail open, is it stale? (DATA) [BK:658, :663]
44. F2 1: turning the schedule on writes no zip at once. (DATA; the timing method is the OWNER's call) [BK:654, :647]
45. F2 2: after more than 5 scheduled files, the oldest are gone and manual ones are untouched. (DATA; OWNER's call) [BK:655]
46. F2 3: the permission is asked on the first turn-on only. After declining, the next-launch notice shows once, and the recording prompt is unchanged. The owner taps the prompts. (DATA) [BK:656]
47. F2 6: Try again with the schedule off lands a zip. With the folder gone, "didn't finish", and its tap opens Backup. (DATA) [BK:659]
48. Replace over an existing backup file. (DATA) [RR:452, :512]
49. A second "Download again". (DATA, NET) [RR:456]
50. A notification tap from a cold start. [RR:475]
51. The restore page's pulse and fade, on video. (OWNER) [RR:460]
52. The schedule under Doze. (OWNER's call) [RR:474]

## B. The owner's tablet (SM-X800, wide tree in both orientations)

### B1. Layout sizes
1. Each pane's real width in portrait and landscape: the Journal list, the detail pane, the species list, the map, and an entry's own map. Compare them with J6's rulings and the 480 dp rule, with screenshots. (OWNER) The -80 baseline was drawer 360, map 103.5 in portrait and 596.7 in landscape, and report map 360×270.1.
2. J6a: Entries 1 column (about 328 dp), Finds 2, Album 3. The Records chips scroll at the real font scale. [J6:103, :163]
3. J6b: the List | Maps tabs at 824 dp portrait, with the map 464 dp across, real touches, and the bar's Layers row. Also a resized window of 840-1200 dp. [J6:167]
4. The photo viewer measurement, which the owner deferred. (OWNER) [plan:756]

### B2. The Journal
5. The detail pane's insets: status bar, nav bar, end edge, and the keyboard over an editor. [J6:100]
6. The pane over the results map: the state on close, flicker with two maps, and the keyboard dismissed. [J6:101]
7. A real Back gesture through the whole order. [J6:102]
8. Album long-press Delete with Undo; the snackbar must be visible while a detail is open. (DATA) [J6:104]
9. The record details pane: its content, Directions and Share. [J6:105]
10. Rotation with a detail open; the pull-photo picker's Camera and Import; predictive back. [J6:97]
11. LJ F1 steps 2 and 3 on wide. (DATA) [LJ:267]
12. LJ F3 step 5: the bar's "+", then "Log a find", with a changed find open. (DATA) [LJ:465]
13. The wide search-panel tap: Advanced search opens expanded, and a choice inside is not overwritten. [-38]

### B3. The tablet map
14. Rotation with Maps showing, with a bubble open, and in fullscreen. [J6:167, :233; F1:165]
15. The map before any search, with planned trips. (DATA if there are no trips) [J6:167; -134]
16. F1 6: the taxon chip and "1 journal entry on map" clear the cluster, at rest and dragged to the top. At 824×1318, 1318×824 and 1280×900 resized. (NET) [F1:170; J6:230]
17. A long-press and drag on the map beside the cluster, and how it feels. (OWNER) [J6:233]
18. Fullscreen with the real bars: the drawer and search bar are hidden, and the map reaches the edges. A persisted fullscreen at launch. [J6:233]
19. The HUD and strip heights over a real map, and the coordinate toggle. (DATA: navigating) [J6:233]
20. The cluster with the legend expanded. [J6:233]
21. The cluster on the right against the attribution, and where the tablet's fullscreen "i" sits. [J6:233; F1:117, :180]
22. Observe: a touch at the locate row's outer edge minimises the cluster. [J6:224]
23. Track delete on the details pane; none while recording. (DATA) [F1:169]
24. A fan near each edge and control, and a rotation with a fan open. (DATA) [F4:105, :202]
25. C1 on wide: the details pane at the token beside the drawer, and the chrome as one colour. The PermanentDrawerSheet's colour was never read. (OWNER, NET) [C1:141, :131]

### B4. Backup on the tablet
26. A restore onto the tablet as a "new phone": the time and disk use, the "Not downloaded" rows, and the wide Backup section. (DATA, NET) [BK:127, :352]

## Not placed, because each needs an owner decision
- **Forced-failure checks** need a hook no build has: P2 57's save-failure Toasts, and 72's failed backup write.
- **The cloud-provider write** was skipped on purpose.
- **The capture review** of 54-56, 58 and 59 is deferred to 2026-09-30.
- **F2 1 and 2 need either a device clock change or real elapsed time.** Which the owner allows is unrecorded.
