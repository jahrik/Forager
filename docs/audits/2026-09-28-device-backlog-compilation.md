# Journal redesign: the device-only backlog, compiled (read-only, at `f62eb3e`)

**Date:** 2026-09-28. **Read at:** `journal-redesign` `f62eb3e`, clean tree. **By:** a pulse subagent of the planner session on the owner's computer; no device reads. Recorded by the planner from the pulse's hand-back, unedited in substance, as the source list for the backlog device checks (the owner, 2026-09-28: "you can run them at the same time if you want, otherwise you can wait for a good time to do them"). L0a's own device items are not here; they are dispatch `2026-09-28-02`.

**Abbreviations.** `A:` is `docs/audits/2026-09-27-` plus: `landscape-b3-drawer-completion-report.md` (B3d), `landscape-b3-destinations-completion-report.md` (B3x), `journal-jN-completion-report.md` (J1…J5c), `picker-fixes-completion-report.md` (PF), `owner-device-result-find-picker.md` (ODR). R is `RECORD.md`, P is `docs/plans/journal-redesign.md`.

**Context (P:85-93).** In landscape the S22 Ultra window is 823×384 dp, the compact tree. `LogPanel` "serves only the wide tree: tablets and foldables", so items for the wide tree cannot be run on the S22 (inferred).

## B3: Maps tools drawer and landscape destinations (B3d:185-199, B3x:208-216, R:2435)
1. "The sheet on the right at ROTATION_90… clear of the 3-button bar… rounded edge face the map" (B3d:190-192). Maps drawer, rotation 90, 3-button navigation.
2. "Scrim tap closing in both orientations, and the drawer reopening afterwards" (B3d:193).
3. "Swipe-to-close while open, and no swipe-to-open while closed, on a real map" (B3d:194-195).
4. "Rotating between 90 and 270 with the drawer open" (B3d:196-197).
5. "Predictive back on the flipped sheet… none is expected; unverified" (B3d:198-199). Gesture navigation. Missing from the terminal's list at R:2435.
6. "The cap and centring beside the real rail, with real cut-out and navigation-bar insets" (B3x:210-214). Every non-Map destination, both rotations. Duplicated by 38 and P:325-326.
7. "the two picker maps and the Cartography map preview read acceptably at 640 dp" (B3x:215-216). Offline Maps picker, the log entry's location picker (R:2434), the entry map preview, landscape.

## J1 (J1:295-307, 461-467)
8. "The chip row's real widths, one-line labels and sideways scroll at 360 dp and in a short landscape window" (J1:297-300). Records. J5 re-covers the landscape half (45, R:2702).
9. "Chip colours in light, dark and the Understory theme" (J1:301-302).
10. "A real night-mode toggle and a fold with the Journal on Records and a non-default chip" (J1:303-307). Partly invalidated: its "back on Maps" expectation predates J2 T5, which made `compactTab` survive recreation (R:2507, :2534); the app should now return to the Journal (inferred). Fold not possible on the S22 (inferred). Duplicates 17.
11. "The All list at 360 dp with real data: find tiles two to a row, badges…, long region rows wrapping" (J1:463-464). Needs finds with photos and long region names.
12. "Scrolling performance of All with a large log" (J1:465-467). Needs a large log.
13. Back from a single-type chip returning to All. Not in J1's device list: the owner's "We'll try it and see if it works" (R:2487; "added to J7's device check" R:2498; P:476). Robolectric asserts it (J1:164); on the device a usability judgement: chip, Back to All, Back to Entries (P:302).

## J2 (J2:257-264, 558-570)
14. "✎ New entry": "shows in full… touches across its whole width… the card beside it still opens… clears the gesture bar in portrait" (J2:259-261). Portrait only (no floating button in short windows, R:2702). Duplicates P:324.
15. "The banner's one-line fit at 360 dp and with larger font scales" (J2:262). Needs a draft. Portrait.
16. "the corner delete button covers roughly the top-right 40 dp" (J2:263). **Superseded:** F5 removed the button (R:2665, PF:339-342).
17. "A real night-mode toggle and a fold on the Journal, on the album, with the drafts list open" (J2:264). Needs two or more drafts.
18. "📷 Add photo": full label, touches across its width, "Take photo opens the camera and Import opens the system picker", the menu stays on screen, clears the gesture bar (J2:560-566). Album, portrait, camera.
19. "Badges at a 107 dp tile… light and dark photos… TalkBack reads each" (J2:567-568). Needs photos linked to an entry and to a saved find.
20. "A real night-mode toggle and a fold on Seasonal, then a new search: Seasonal should reload" (J2:569-570).

## J3 (J3:340-355)
21. "The cards at 360 dp with real data": long title, chips and stats wrapping, a 140 dp hero photo "may look soft", a long timeline (J3:342-348).
22. "The sticky header over scrolling cards… in light, dark and Understory themes" (J3:349-350). Entries in two or more months.
23. "Thumbnails. Legible at 56 dp and 40 dp on real tracks" (J3:351-352).
24. "The collapsed row's height… at larger font scales" (J3:353-355). Needs an empty entry.

## J4 (J4:260-270)
25. "The swipe itself: threshold feel, the red background and icon, RTL" (J4:262-263). **Superseded** by J4b's two-stage swipe (R:2627); see 30.
26. "TalkBack: the 'Delete' action… the snackbar announced, Undo reachable" (J4:264-265).
27. "The snackbar's position above the bottom navigation… in both orientations, and in the wide tree's drawer" (J4:266-267). Wide half not runnable on the S22.
28. "The region row on a surface other than `surface`" (J4:268). Needs an offline region.
29. "A night-mode toggle mid-snackbar" (J4:269-270).

## J4b (J4b:321-329)
30. "The two-stage swipe's feel… open width, delete threshold, fling… RTL, and a vertical scroll starting on an open row in the Waypoints chip" (J4b:323-324; scroll case :306-308). Inferred from later changes: entry cards swipe only in portrait (short-window cards use a long-press, R:2702); J5c made a tap open the details sheet and a tap on an open row close it (R:2729).
31. "Long-press timing and the menu's anchor position on a tile near the screen's bottom edge" (J4b:325). Duplicates 42.
32. "TalkBack: the Edit/Delete actions on cards, rows, tiles and photos… the long-click labels" (J4b:326-327).
33. "a long-press near the corner trash button" (J4b:328-329). **Superseded** by F5; covered only under Robolectric (PF:347-351).

## Picker and offline-maps fixes (PF:299-314, R:2666)
34. Find picker: "pinch in to about 18, pan, and wait 10 s. Pass: the view does not drop to zoom 13 and does not return to the device" (PF:301-304), "compact and wide". **Partly verified:** the owner observed a pass on Build 1173, head `e36ff86` (ODR:5-9). Open: whether fixes were arriving (ODR:11; trap described at `emulator-check-attempt.md:18-19`); the wide tree; a re-run on the current build (L0a changed `SightingsMap.kt` heavily; no changed line touches the gesture listener, still at `SightingsMap.kt:402`; re-run advisable, inferred).
35. "The first-fix follow… location just enabled… Pass: the picker moves to the device when the fix arrives" (PF:305-307). The positive control 34 needs.
36. "The offline picker after a slow first fix… GPS cold, pan… drag the radius slider" (PF:308-310).
37. "A download kept while the Offline maps list is reopened… force-stop the app mid-download" (PF:311-314). Needs a network download.

## J5 (J5:261-272, 396-400)
38. "L8: no control within the `displayCutout` inset… the L1 row's switch… and icons… at both rotations" (J5:263-266). Duplicates 6. Both navigation modes (P:326).
39. "The height table's real figures…; how many cards show" (J5:267). P:327 expects "Six or more cards"; Robolectric found four whole and two partial (J5:73-75), so six may not hold (inferred).
40. "The header's reveal and dismissal feel; the dropdown closing when the header hides" (J5:268).
41. "Hide-on-scroll feel… a list only slightly taller than the screen" (J5:269).
42. "Long-press on sideways cards: timing, the menu's anchor near the bottom edge" (J5:270).
43. "Import from the L1 row's photo button, turning the phone while the picker is up" (J5:271).
44. "Rotating with an entry open in its editor, both ways" (J5:272). Duplicates P:323.
45. "The chips at the device's own font, and at the user's font scale" (J5:398-399). Robolectric: overflows by 25 dp at font scale 1.15 (R:2702).
46. "The 18 dp chip icons' legibility" (J5:400).

## J5c: tap a waypoint, track or region row (J5c:317-328)
47. "The sheet's insets… the navigation bar and the cut-out in both landscape rotations, and the sheet's top in portrait" (J5c:319-320).
48. "the sheet's actions start below its fold and the content scrolls" in a short landscape window (J5c:321-322).
49. "Drag-to-dismiss and the predictive Back animation" (J5c:323).
50. "TalkBack: 'Details for …' announced on each row…" (J5c:324-325).
51. "The ripple on a waypoint card… and on the badged row in All" (J5c:326).
52. "Directions and Share handing off to real apps" (J5c:327). Directions only; in-app Navigate deferred (P:488).
53. "Rotating with the sheet open" (J5c:328).

The J5c terminal's summary, "the sheet's height and scrim" (R:2731), does not match the report (the scrim was tested under Robolectric, R:2729); use the report's list.

## The plan's own J7 list (P:318-327, P:410)
Three lines with no stage item behind them: "draft Continue" (P:320); "The map's 'Log a find' routing" (P:321); "mid-find-edit" rotation (P:323). "Swipe and Undo" (P:322) now means the two-stage swipe and long-press menus (P:410). J8's map overlays (P:410) are not built. Rotation does not recreate the Activity (J1:126-129), so rotation items test layout reflow, not state restoration (inferred).

## The handoff's "first two" claim
Confirmed as the plan's ordering (P:476; `prompts/preserved/2026-09-28-01.md:23`), with two corrections: pinch-and-pan already has the owner's observed pass (ODR:5-11), so what remains is 35 and the wide tree; and Back-to-All comes from the owner's ruling (R:2487, :2498), not a report's device list.

## Count
B3 7 · J1 6 · J2 7 (1 superseded) · J3 4 · J4 5 (1 superseded) · J4b 4 (1 superseded) · picker 4 (1 partly verified) · J5 9 · J5c 7 · plan J7: 3 lines with no stage item. 53 items: 3 superseded, about 10 duplicated across stages.

## Could not determine
Which build is on the phone (not read; the planner read `1.0.1192+g24589349` at 03:00Z); whether landscape B4 (`docs/plans/landscape-phone-design.md:389`) ever ran (only R:2435, "none run", found); whether the owner's picker pass was compact or wide, or whether fixes were arriving (ODR:11); that the S22 Ultra cannot fold (outside knowledge, not verified in the repo).
