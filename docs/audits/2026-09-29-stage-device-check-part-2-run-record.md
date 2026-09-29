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
