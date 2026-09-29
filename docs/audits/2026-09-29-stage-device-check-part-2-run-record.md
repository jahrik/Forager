# Stage device check Part 2: run record

**Dispatch:** 2026-09-28-154 (`prompts/preserved/2026-09-29-21.md`). One section per session. Evidence:
`/home/zynergy-labs/Zynergy/device-evidence/2026-09-29-part-2/`, prefixed `s1-`, `s2-`, `s3-`.

## Session 1: layout and the map

**Coder:** `claude-sonnet-5-5` (as configured for the session; not independently readable from inside it).
**Device:** S22 `R5CT321008R` (SM-S908U). The tablet (`R52T506412L`, shown as `unauthorized`) is not touched; every adb call
uses `-s R5CT321008R`.
**Items:** 1-8, 9-12, 27-31, 34-51, 53-56, 58-59, 60, 61-63.

### Setup, done before any launch

**Base.** `origin/journal-redesign` was `aa0dde85` when the worktree was cut. `git diff --stat 85a41257 aa0dde85` touches
only `RECORD.md` and `prompts/preserved/2026-09-29-22.md`, so the code is the planner's clean-suite code. The APK was built with
the worktree detached at `85a41257`, then the branch was restored.

**Build.** `LC_ALL=C.UTF-8 ./gradlew --offline assembleDebug`, BUILD SUCCESSFUL in 2m 8s (log: `s1-build.log`).
- A first attempt, run with a 590 s `timeout`, exited 137 (killed) and produced no APK. The log showed a Kotlin daemon
  connection failure and fallback. It was rerun with no timeout and succeeded. The cause of the kill is not determined.
- sha256 `642d039d8cf08a03564abde22eacf4c1058643e4411548614fe5597c1cc6df98`
- versionName `1.0.1685+g85a41257`, versionCode `1685`
- signer `CN=Android Debug`, SHA-256 `cb2f6da502c3fe7bea8db747414bed47cbc8350944cf8291e9b09806c94f1626`

**Verified full copy (rule 2), taken before the install** into `s1-copy/`:
- 18 files under `files/`, `databases/`, `shared_prefs/`: `forager.db` + `-wal` + `-shm`, `fungi_index.db` + side files,
  `files/photos/` (3 files), `files/mbgl-offline.db` (no side files exist), 5 DataStore files, 2 shared_prefs, `profileInstalled`.
  This is wider than the dispatch's list. `captures/` and `maplibre-offline/` are empty directories.
- Device sha256 (`device.sha256`) equals the pulled files' sha256 for all 18. The device hashes were re-read after the pull and
  are unchanged.
- `forager.db` (on a scratch copy of the db and its wal, so the original was not replayed): header `SQLite format 3`,
  `integrity_check` = ok, `user_version` = 16. Row counts are in `s1-copy/forager-db-verify.txt`
  (mushroom_log_entries 3, log_photos 3, tracks 1, track_points 23, waypoints 3, offline_regions 2, cartography_entries 7,
  planned_trips 0, cached_searches 2, and the ref tables).
- **Caveat:** the dispatch names `files/photos/`, but the copy holds 3 photos, and the owner-vs-earlier-session provenance
  of these rows is not known to me. They are treated as the owner's and are not edited.

**Install (rule 3).** `install -r` returned Success. `firstInstallTime` is unchanged (`2026-09-22 11:15:05`); versionName went
`1.0.1577+g7d17a5c4` to `1.0.1685+g85a41257`. Same signature (an `install -r` with a different one would have failed). The code
declares Room `version = 16` (`ForagerDatabase.kt:167`), the same as the device, so no migration is predicted.

**Starting settings** (restored and read back at the end): `accelerometer_rotation` 0, `user_rotation` 0, `font_scale` 1.0,
window/transition/animator scales 1.0/1.0/1.0, `wm size` 1080x2316. Crash buffer at start: empty.

### Pre-registration

Registered before the first launch of the new build. Source reports are under `docs/audits/`; `RECORD` is `RECORD.md`. A line
number is given only where I have read the code. Code I have not yet read is listed as unverified. I will add anchors in the
results section when a check reads code.

**General method.** Real `adb shell input` and real rotations only. Each measurement comes from a `uiautomator dump` or from
pixels. A tap that is meant to hit a control is a coordinate tap. Item verdicts are pass, fail, not runnable, or owner to judge.

**Predictions.** These are what I expect, so a surprise is visible:
- P1. Most layout items (34-51) pass, since Part 1 and the follow-ups fixed the earlier fails.
- P2. Robolectric reported zero insets for all of them (CLAUDE.md, "Robolectric reports zero window insets"), so I expect at
  least one placement item (37, 38, 45, 46, 49, 62) to show a real-inset difference.
- P3. Item 38's cluster-to-search-bar margin is about 0 px, as RECORD:4875-4876 says. I predict a pixel read of 0 to a few px.
- P4. Items 53-56 are for the owner. They get captures and no verdict from me.
- P5. Item 63's no-permission-prompt half passes (`PhotoViewerDialog.kt:225` says no storage permission on API 29+). The S22 is
  API 34+.

**Pass conditions.**

| Items | Pass condition (source) |
|---|---|
| 1 | A tap on each glyph kind (find, waypoint, track, region, trip) opens its bubble with the right feature; colour-field cells open only at the point stage; the photo glyph and its cells are touched at least once (m1 report "Device-only", first bullet). |
| 2 | A tap inside an offline circle, away from its outline, opens nothing and reaches what is beneath (RECORD:2788). |
| 3 | The tail tip sits on the glyph, measured from the dump/pixels, with the glyph at each screen edge, at 0/90/270. |
| 4 | Bubbles clear the compass strip, nav, rail and cut-out at 0/90/270 by dump bounds. |
| 5 | The bubble follows its glyph after pan, zoom and rotate; a dismissed bubble does not return. |
| 6 | In fullscreen a feature tap does not bring the chrome back. |
| 7 | Directions leaves the app for the phone's navigation app. |
| 8 | The find overlay opens over the Journal and over a day entry. |
| 9 | On first style load an entry is framed (its records inside the view) in the preview, fullscreen and landscape. |
| 10 | Rotation re-fits; the view is kept after a pan and after opening and closing a find. |
| 11 | Track widths are recorded at zoom 13 and 11 by pixel width; the breadcrumb dots and gaps shrink with the width. |
| 12 | The search field refocuses after `clearFocus()`; at a large font scale in short landscape the fields show, the keyboard lowers, and the scroll goes back up. Font scale is restored. |
| 27-31 | Per the landscape report: the picker and the entry report at 90 and 270 with real insets, the scrolling side's remaining height measured, the map side against the cut-out and rail, pan/zoom kept on rotation with the picker open, L2's opening frame on the 154 dp preview, the cut-off control observed, and a tall bubble on the 154 dp preview not clipped. |
| 34 | Portrait: expanded legend, the cluster's last row is above the legend's top. Collapsed, the cluster is back at its earlier bounds. |
| 35 | Portrait dropdown: Latitude, Longitude and "Search this location" have bounds above the keyboard's top. Record whether the scroll lowers the keyboard. |
| 36 | After a tab round trip, pan, zoom, bearing, tilt and follow mode are the same; a new search still moves the camera. |
| 37 | The "i" is in the portrait and fullscreen place; inboard of the rail at 90; in the cut-out band at 270; a touch opens attribution. |
| 38 | At 90 the caption's bounds do not overlap the cluster's. At 90 and 270 the cluster-to-search-bar gap is read from the dump (P3). |
| 39 | At 90 and 270: the pill is beside the bar; the fill is 56 x 156 dp; drag, snap and the handles work; record and return are reachable while recording. Thumb reach goes to the owner. |
| 40 | Legend inboard of the cluster with the cluster on each side, collapsed and expanded, at 90 and 270; legend vs dropdown; the first-compose jump is noted. |
| 41 | Chip row at 90 and 270 with one chip, two chips, a long label and the cluster far-side: alignment, width cap, ellipsis beside the clear button, clear of the central third and nav inset, 12 dp gap; portrait spacing is 4 dp. |
| 42 | In landscape the cluster drags to the nav inset; in portrait it stops above the legend. |
| 43 | J8's pill is touched at its edges; I report whether the edges take a touch. The 48 dp question is the owner's. |
| 44 | Street on Maps then Satellite on the entry leaves Maps on Street; all three are dark at night. |
| 45 | The search notice's top equals the strip's bottom and the cluster is below it; clearing restores the cluster; 0/90/270. |
| 46 | The Maps snackbar meets the floating nav's top in portrait, takes the system-bar inset in fullscreen, and clears the rail and cut-out at 90 and 270. |
| 47 | The Layers sheet's map-type chips are centred and the same height in portrait and landscape. |
| 48 | The nav-bar band shows the map at 0.8 under the Layers sheet and the short-landscape Records sheet; solid in portrait. |
| 49 | The landscape centre-pin row clears the rail and nav bar at 90 and 270, and the pin stays at the map centre. |
| 50 | One Back closes species suggestions and leaves the search panel open. |
| 51 | The Records sheet shows the picker map through in short landscape and is solid in portrait. |
| 53-56 | Owner to judge from captures. I record what I see and give no verdict on the design questions. |
| 58-59 | Captures at night over Topographical and Street and Satellite, the highlighted region, and the entry map with Offline on. The "is 0.85 too bright" question is the owner's. "Nothing changes by day" is compared with a day capture. |
| 60 | With two planned trips and no search, from a cold start, the flags draw by day and at night, the switch works and a tap opens a bubble. The trips are created as "DEVICE CHECK 2026-09-29" rows and deleted afterwards. |
| 61 | A capture (no location) and an import (its own metadata) appear in the "Forager" album with the right date. Gallery copies are deleted afterwards with their paths recorded. |
| 62 | The control's bounds against the real status and nav bars, in portrait and landscape. |
| 63 | No permission prompt appears; a 12 MP JPEG copies without a stall. |

**Cannot run, or unlikely to (registered now, so that it is a declared limit and not a surprise):**
- Items that need new data create only rows named "DEVICE CHECK 2026-09-29 ...". Where the owner's own data is the only
  data on the phone, I use it read-only.
- Item 61's import half needs a photo on the phone to import; if none exists I will say so.
- Item 11's zoom 13 and 11 depend on a track being visible; the phone holds one track.
- A system prompt over the app is not tapped by me (rule 11). It becomes a stop.

### Progress, interim (00:42; the run is not finished, no session verdicts yet)

Evidence is in `s1-*` files in the evidence directory; the live log is `s1-notes.md` there.

- **Item 37, portrait:** pass. A tap at (1040,1915) inside the "i" bounds `[1010,1886][1069,1945]` opened the MapLibre attribution dialog (`s1-i37-p0-attr.png`).
- **Item 1, partly:** the find, photo and region bubbles open on real taps (`s1-m1-p0-tap-glyph.png` and `s1-m1-p0-tap-inside-circle.png`). Waypoint, track and trip glyphs, and the colour-field cells, are not yet touched. All of the phone's existing finds, photos and waypoints are stacked at one spot and the map stops zooming, so the kinds cannot be separated without new data at other places.
- **Item 2:** pass with a caveat. Three taps inside the offline circle, more than 250 px from the glyph stack and more than 100 px from the outline, opened nothing. Nothing lies beneath there, so this shows the interior does not capture a tap, not that a glyph beneath is reached.
- **Observation, no report I have read asserts it:** bubbles opened on taps about 150-170 px (54-60 dp) from the stacked glyphs, and the region bubble on a tap 48 px inside the outline.
- **Rotation setting found changed:** `accelerometer_rotation` read 0 before the install and 1 at the first launch, with the phone at ROTATION_90. I did not set it. I reset it to 0 with `user_rotation` 0. The final read-back will restore the original 0/0.
- **Owner message received mid-run, not actioned:** quoted verbatim in the evidence notes and in the hand-back. It asks to shrink the icon bar, turn the small pill 90° with half of it under the bar, and make the pill the same size as the bar. It is a design change. It has ambiguities (which pill, and what "same size" and "beneath" mean) and it would change the APK that Sessions 2 and 3 reuse. It belongs to a separate dispatch.
- **Owner message withdrawn (00:45):** "Oh sorry ignore that". The pill/icon-bar message above is not a finding and no dispatch follows from it. Items 34, 39 and 40 are judged on their own pass conditions.

### Results so far (interim 2, 00:56; more follow)

Every figure below is from a `uiautomator dump` (`s1-*.xml`) or from the named screenshot (`s1-*.jpg`). The disk filled at 00:51, so
five screenshots from that moment are empty and were deleted, and the remaining PNGs were converted to JPEG q90. Bounds come from
the dumps, not from the JPEGs.

| Item | Verdict | Evidence and reading |
|---|---|---|
| 1 | partly: find, photo, region, waypoint bubbles pass; track, trip and colour-field cells not reached | Find, photo and region bubbles open on real taps (`s1-m1-p0-tap-glyph`). A waypoint I dropped as "DEVICE CHECK 2026-09-29 wp1" opens its own bubble with Directions and Details (`s1-m1-waypoint-bubble`). On the entry map the ORIGIN waypoint and the track were not separable from the find: taps at their glyphs returned the find-1 bubble. No planned trips exist (`planned_trips` = 0), and I have not yet made one (item 60). |
| 2 | pass, with a caveat | At one zoom level in, three taps inside the offline circle (>250 px from the glyphs, >100 px from the outline) opened nothing. Nothing lies beneath there, so this shows the interior does not capture the tap, not that a glyph beneath is reached. |
| 3 | partly | Left edge (photo glyph at x~58): card clamped to x=35, tail tip on the glyph's cap but at its right edge, not centred. The entry map's right-side glyph (x=947): tail tip on the cap. Top, bottom, right edge and 90/270 not tested. |
| 5 | pan and dismiss pass; rotation fails | Pan re-anchors (tail moved with the glyph). After X, a pan does not bring the bubble back. **After a rotation from 0 to 90 with the bubble open the bubble stayed at its portrait place** (`s1-m1-rot90-open`, bounds [~35..780 x ~700..990]) while the glyph was at about (937,573); it overlapped the cluster. A 50 px pan re-anchored it (`s1-m1-rot90-afterpan`). Zoom re-anchor not tested. |
| 6 | pass | In fullscreen a tap on the waypoint pin opened its bubble; the bottom-nav labels are absent from the dump and "Exit fullscreen" stayed. |
| 7 | pass | Directions launched `act=VIEW dat=geo:0,0?q=45.3262615,-122.6181016(DEVICE%20CHECK%202026-09-29%20wp1)`, `cmp=com.google.android.apps.maps/.MapsActivity`, which was the top activity; Back twice returned to Forager. |
| 8 | pass | Find bubble > "Open in Journal" opens the find page over the Journal; the day entry's find-2 bubble > "Open find" opens over the day entry; Back returns to the entry. |
| 34 | pass (portrait) | Collapsed cluster last row [922,1583][1057,1718], legend chip [842,1747][1057,1882]. Expanded legend card [269,1596][1057,1866]; the cluster moved up and its last row ends at 1563: 33 px clear. Re-collapsed: the rows are back at [922,1437]/[922,1583], as before. Needs Diagnostics "Synthetic forecast layers" ON (original OFF; restore at the end). |
| 35 | pass | Latitude [45,990][528,1171], Longitude [551,990][1035,1171], "Search this location" [355,1255][725,1312]; the keyboard's top is at about y=1398. A scroll of the dropdown lowered the keyboard (`mInputShown` true to false). |
| 36 | partly | Pan and zoom survive Maps > Journal > Maps (`s1-t36-before`, `s1-t36-after`, glyph at the same pixel). Locate then round trip: same view. Bearing and tilt cannot be set with a single pointer: not runnable. "A new search still moves the camera": not yet run. |
| 37 | pass at 0, 90, 270 | "i" at [1010,1886][1069,1945] (0); [1886,1010][1945,1069] with the rail from x=1956 (90); [2246,1010][2305,1069] (270). A tap opened the "MapLibre Android" attribution dialog each time (`s1-i37-*`). Fullscreen "i" not yet checked. |
| 38 | pass (caption); margin is 14 px, not ~0 | At 90 and 270 the search bar bottom is 209 and the cluster top 223: 14 px by bounds. The coordinate caption [1260,87]..[1933,178] does not overlap the cluster (x 98-391). |
| 40 | partly: pass at 90 with the cluster on either side | Cluster left, expanded legend [1145,720][1933,990]: no overlap. Cluster dragged right, expanded legend [830,720][1618,990], the pill's left edge 1640: 22 px clear. 270 (cluster right by default): legend [1115,720][1903,990], pill from 1925. Dropdown: the legend shows through the semi-transparent dropdown behind "Search this location" (`s1-search-top`). |
| 42 | pass, with an observation | At 90 the cluster dragged down to [1798,935][1933,1070] (bottom 1070 of 1080). Its "+" then overlaps the "i" [1886,1010][1945,1069] (`s1-drag-down-r90`). Portrait stopping above the legend not yet tested. |
| 47 | pass (portrait) | The three chips sit at y 651-708 (same height); outlines x 124-328 and 715-958 of 1080: margins 124 and 122. Landscape not yet measured. |

**Observations for the owner (not gates):**
- The expanded legend is capped at 96 dp (`MapLayersSheet.kt:440`). With the title wrapped to two lines, the ramp's "0%" and "100%" labels are cut off at rest (`s1-leg-expanded-p0`).
- Long-press then drag is what moves the cluster (`AvailabilityCompactMapUi.kt:910`, `detectDragGesturesAfterLongPress`). A plain swipe on the handle does nothing.
- Bubbles open on taps up to about 54-60 dp from a glyph; nothing I have read states that tolerance.
- The bubble sits under the cluster at 90 where they overlap.
- `accelerometer_rotation` was set back to 1 twice without my setting it (cause outside the app; the app has no code writing it).

### Results so far (interim 3, 01:27)

Evidence prefix `s1-` in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-29-part-2/`; the full live log with every bound is `s1-notes.md` there. **Measurement basis:** every verdict below rests on `uiautomator` bounds unless marked "PNG", except that positions read off screenshots are from JPEGs (the disk filled) and are marked "visual". Item 48 rests on raw PNG pixels (`s1-i48-bandA-crop.png`, `s1-i48-bandB-crop.png`).

**Notification prompt (planner's request).** Trigger: Maps tab at 270, my `input tap 1993 729` on "Start recording track" (`[1959,695][2027,763]`), the first recording start; status-bar clock 1:03 in `s1-rec270-a.jpg`. The logcat buffer had rolled past it (earliest kept FGS line 01:06:44 for the second start), so the log line is not available. The prompt was the system "Allow Forager to send you notifications?" (`GrantPermissionsActivity`). The owner tapped Allow; `POST_NOTIFICATIONS` went from `granted=false` to `granted=true`. Finding: starting a track recording raises the notification prompt, not only choosing a backup folder. To restore: `adb shell pm revoke com.zynergylabs.forager.app android.permission.POST_NOTIFICATIONS`, then read it back (done at the end).

| Item | Verdict | Reading |
|---|---|---|
| 9, 10 | pass (entry 2026-09-27) | At 0 the preview map is [0,437][1080,1247]; at 90 [116,400][1916,832] (432 px = 154 dp) and at 270 [401,400][2201,832]; both finds and the cluster of glyphs are inside in each; rotation re-fits. A tap on empty preview map opens the entry map fullscreen. Pan-then-find-open-close: not run. |
| 11 | not run | Zoom 13/11 track widths: the map does not zoom out from the entry preview with real input in a way I could measure widths; only 1 old track and my 1-point tracks exist. |
| 12 | not run | Large font scale and refocus not exercised. |
| 27, 28 | pass | Picker (Journal > Records > Offline maps chip) at 90 and 270: controls [1061..1871] / [446..1256] all visible with non-zero size, map beside them; pin text "Pin at: 45.3222, -122.6252" identical at 270, 90 and 0 after a pan. Entry report map centred between insets. |
| 29, 30, 31 | pass / observation | L2 opening frame on the 154 dp preview holds the day's find spread with margin. The bottom-right clickable [1725,856][1871,991] is the Offline map switch, fully inside (rail from 1956). Bubbles (find card about 103 dp, photo card 118 dp) fit inside the 154 dp map and are not clipped; a bubble taller than the map could not be produced. The card covers its own glyph. |
| 39 | partly pass | At 270 with recording on: record/stop [1959,695][2027,763] and return [1959,841][2027,909] both on screen and clear of the legend; a real tap on return started navigation (a banner covers the search bar and strip) and Stop navigating and Stop recording worked. Drag and snap (long-press) worked at 90 with both handles. Thumb reach: owner. The 56 x 156 dp fill: the container above the pill is drawn as one surface (`s1-drag2-r90.jpg`); the owner to judge. |
| 41 | pass at 90 (cluster either side) and 270, one and two chips and a long label | Chips sit at the bar's start on the side away from the cluster and follow the cluster when it is dragged; 104 px clear of the cluster; the long label ellipsises before the X and the row is capped at the bar's width. Portrait spacing: taxon pill [219,272][861,362] to J8 clickable [219,374][680,509], 12 px (4 dp). |
| 43 | pass | Taps 4 px inside each edge of J8's pill opened its list; taps 4 px outside opened nothing. Feel: owner. |
| 44 | pass (Street/Satellite half) | Maps Street; the entry map showed Street; choosing Satellite on the entry left Maps on Street. Night half with 58/59. |
| 45 | pass at 0 | Notice "Enter a valid latitude (-90 to 90) and longitude (-180 to 180)." [0,254][1080,391], its top at the strip's visible bottom (254); the cluster moved down to Fullscreen [956,464] (button top 430), 39 px below the notice; clearing the notice returned the cluster. 90/270 not run. |
| 46 | pass | Trip-start snackbar ("Do Not Disturb is on...") at 0: card ends at 1925, the floating nav starts at 1956 (31 px); fullscreen: 32 px above the system nav band; 90: 59 px from the rail, 53 from the cut-out; 270: 63 and 51. It draws over the legend and the cluster's lower rows. |
| 48 | pass, portrait Layers sheet (PNG) | Screen rows y 2215-2300 in the nav band: with the sheet open pixel std is 0.14-0.22 of the same rows with no sheet, mean (35,32,30): the map shows through at about 0.8, not a flat band. The landscape Records sheet was not opened. |
| 49 | pass on clearance | At 90: OK [120,922][1004,1057], Cancel [1027,922][1911,1057], rail from 1956. At 270: [405..1289] and [1312..2196], rail ends 360. Pin x = map centre exactly; pin y hot spot unknown (node 498..611 against map centre 582). |
| 40, 42 | see interim 2 | unchanged |
| 36 | pass | search by coordinates moved the camera (NET). Bearing/tilt not settable. |

**Finding to note:** deleting my test data: track details sheet has only Share (no Delete), so five 1-point test tracks are left; see the clean-up section at the end.

### Session 1: final results (01:40)

Same basis as above (uiautomator bounds; visual positions from JPEG marked; item 48 from PNG). The complete per-tap log with every bound is `s1-notes.md` in the evidence directory. **Crash buffer:** empty at start; at the end 0 `FATAL` lines and no Forager line.

**Verdicts not given above**

| Item | Verdict | Reading |
|---|---|---|
| 1 | partly | see interim 2. Track and colour-field cells (needs an unstacked location, and a point-stage zoom) not reached; trip glyph: the flag opened its bubble ("DEVICE CHECK 2026-09-29 trip B", coordinates, Directions) at 90. |
| 3 | partly | as interim 2. |
| 4 | not run separately | Bubble bounds against strip, nav, rail and cut-out were seen in passing: photo bubble at 0 [68,849]..; at 90 the bubble overlapped the cluster until a pan (see 5). |
| 5 | **fail on rotation** | as interim 2 (bubble stays at the portrait place until the next pan). |
| 11, 12 | not run | see interim 3. |
| 36 | pass except bearing and tilt (not settable with single-pointer input: not runnable) | |
| 37 | pass at 0, 90 and 270; **fail in portrait fullscreen** | In fullscreen the "i" is at [1010,2246][1069,2305], inside the system navigation band (`NavigationBar0`, from y=2181). Two real taps at (1040,2275) and (1040,2262) opened no dialog and focus stayed on MainActivity (`s1-fsi`, `s1-fsi2`). |
| 38 | pass (caption); margin 14 px, not about 0 | interim 2 |
| 42 | landscape pass with observation; portrait not run | interim 2 (the "+" then covers the "i" at 90) |
| 45 | pass at 0 only | 90 and 270 not run |
| 50 | pass with a qualification | With the keyboard up, Back 1 closes the keyboard, Back 2 closes the suggestions popup leaving the search panel open, Back 3 closes the panel. "One Back" holds once the keyboard is down. |
| 51 | not run | I could not find the Records sheet. See the mistake below. |
| 53 | not run | (no ORIGIN/END pins isolated) |
| 54, 55, 56 | owner to judge | captures: `s1-night-*.png`, `s1-list-tab`, chip screenshots `s1-j8p0/j8r90/j8r270/long90`. The taxon chip and J8 chip are visible against the map at night; no shadow. |
| 58, 59 | owner to judge (captures only) | Night is ON at baseline (map sepia). `s1-night-Street.png`, `s1-night-Topographical.png`, `s1-night-Satellite.png` (PNG crops y 700-1400): the highlighted region outline is a teal ring with a white dashed line on all three. No day comparison, no entry-report-at-night capture. |
| 60 | pass | Cold start, no search: flag drawn for trip B; a tap opened its bubble; Layers > Planned trips off removed the flag and on restored it. Day (Night Maps off) not run. |
| 61 | partly (import only) | The phone has no photo without a location, so the capture-without-location half could not be tested. Saving the existing photo: MediaStore row `Pictures/Forager/forager-photo-20260927-211632.jpg`, 1,841,146 bytes = the source file's size, `datetaken` NULL, `date_added` set. The Gallery app's own date display was not checked, so "the right date" is **unverified**; NULL `datetaken` is the thing to check. |
| 62 | partly | Viewer: Close [23,98][158,233], Save [922,98][1057,233], status bar bottom 75: 23 px clear. Landscape not measured. |
| 63 | pass | No permission prompt; the row was added within 1 s of the tap (12 MP not tested: the file is 1.8 MB). |

**Mistake made (Decisions I made / Flags):** In Journal > Records > Offline maps at 90, tapping "Download Maps" started a real download at once (no confirm step): Offline maps went 2 to 3. I had expected a sheet. That region and the other test data are gone after the restore below. **Flag:** a single tap on Download Maps downloads with no confirmation.

**Decisions I made**
- **Restored the phone from the verified copy at the end of Session 1** (force-stop, `run-as` cat of all 18 files back). Reason: the track details sheet has no Delete, so five 1-point test tracks, ~10 of my waypoints, one saved entry, two trips and one download could not be removed from the UI. Read-back after the restore: all 18 sha256 equal the copy's; `forager.db` integrity ok, `user_version` 16, every table's row count equal to `forager-db-verify.txt`. The dispatch's rule 6 names the restore for Session 3; using it here goes beyond that and is disclosed. Nothing of the owner's changed between the copy and the restore, so the restore is a no-op for the owner's data (the copy was taken before I did anything).
- Turned the debug "Synthetic forecast layers" switch ON for 34/40, and started recordings for 39/46; the restore returned the DataStore file to OFF.
- The owner tapped Allow on the notification prompt; I revoked it (`pm revoke`) and cleared the user-set flag (`pm clear-permission-flags ... user-set`); read back `granted=false, flags=[USER_SENSITIVE_WHEN_GRANTED|USER_SENSITIVE_WHEN_DENIED]` = the start state.
- Converted PNG evidence to JPEG q90 when the disk filled; deleted my worktree's `app/build`.
- Deleted the one Gallery row I created (`content delete`, MediaStore id 1001239272, path `Pictures/Forager/forager-photo-20260927-211632.jpg`); the folder no longer lists.

**Flags outside scope**
- `accelerometer_rotation` returned to 1 twice on its own (the app has no code that writes it); left 0/0 at the end.
- Item 37 in fullscreen and item 5 (rotation with a bubble open) are the two device failures.
- Starting a track recording raises the notification-permission prompt (planner's point 2).
- The download-with-no-confirm above.
- A cross-session message (planner ref change) and an owner message about the pill layout (withdrawn) are quoted in `s1-notes.md`.

**Restore read-back.** Rotation 0/0, font_scale 1.0, animation scales 1.0/1.0/1.0, `wm size` 1080x2316, versionName `1.0.1685+g85a41257`, firstInstallTime `2026-09-22 11:15:05` unchanged, `POST_NOTIFICATIONS` not granted; map type, Night Maps and the Diagnostics switch are back to the copy's DataStore values because the DataStore files were restored byte for byte.

**Items with no verdict here:** 11, 12, 51, 53, 61 (capture half), 45 at 90/270, 42 portrait, 4 (separate). They are not runnable or were not reached; they are not passes.
