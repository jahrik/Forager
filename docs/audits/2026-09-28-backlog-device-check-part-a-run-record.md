# Backlog device check, Part A (the Journal in portrait), on the S22 Ultra: run record

**Status: pre-registration.** This section is committed and pushed before any item below is run on the
phone (the baseline reads of item 1 excepted). The results follow in later commits. The pass conditions
and predictions are not edited after this commit, except where a correction is marked.

**Date:** 2026-09-28, from 04:58 UTC. The phone's clock shows UTC-7, so its local date is 2026-09-27
until 07:00Z.
**Device:** Samsung SM-S908U, serial `R5CT321008R`, the only device attached (`adb devices -l`).
`getprop` at 04:58Z: `ro.product.model=SM-S908U`, `ro.build.id=BP2A.250605.031.A3`.
**Build under test:** the installed build, `versionName=1.0.1192+g24589349`, `versionCode=1192`
(`01-dumpsys-package-start.txt`). Nothing installed; no Gradle, no emulator.
**Dispatch:** `prompts/preserved/2026-09-28-04.md`; the intent is `2026-09-28-04` in `RECORD.md` (the planner's).
**Base:** `origin/journal-redesign` at `46186bb` (fetched 04:58Z). This branch, `device-backlog-a-2026-09-28`,
is cut from it.
**Code citations** are at the installed build's commit `2458934`. `git diff --stat 2458934 46186bb -- app/src/main`
touches 17 files, none under `ui/log/`; the one Journal-adjacent file it touches, `ui/availability/AvailabilityScreen.kt`,
is cited at `2458934` (`git show 2458934:…`).
**Evidence:** outside the repository, in `/home/zynergy-labs/Zynergy/device-evidence/2026-09-28-backlog-a/`, cited by
name with sha256 in the Evidence index. No screenshot, dump, coordinate, note text, place name or photo is in this file.
**dp to px:** `wm density` is 450, so 1 dp = 2.8125 px. `wm size` is 1080 x 2316 px, so **the portrait width is
384 dp, not the 360 dp several sources assume.** Nothing emulates 360 dp here.
**Navigation mode:** `settings get secure navigation_mode` = `0`, three-button navigation. Its bar is
`[0,2181][1080,2316]` (135 px, 48 dp); the status bar is `[0,0][1080,75]`; the cut-out is `Rect(512, 0 - 568, 75)`
(`11-dumpsys-window.txt`).

## Planner's launch message (quoted; part of the dispatch)

> **Changes since the dispatch was written.** Each is a planner message and part of the dispatch; quote them in your report.
>
> 1. **Test data already on the phone.** The L0a check left owner-authorised test data in place:
>    - a "DEVICE CHECK waypoint";
>    - two finds, "DEVICE CHECK find 1" (with a photo) and "DEVICE CHECK find 2";
>    - the owner's kept track;
>    - an offline region "DEVICE CHECK";
>    - a committed day entry, dated 2026-09-27, whose text is "DEVICE CHECK 2026-09-28 (L0a)".
>
>    See the "Created data" section of `docs/audits/2026-09-28-l0a-device-check-run-record.md`. You may **use** these as data for your items: read them, open them, and view them in lists and sheets. Deletes stay limited to the entry you create yourself, and always end in Undo. Your inventory counts them.
>
> 2. **Do not run `adb logcat -c`.** The previous coder cleared the crash buffer that way by mistake. Read logs with `-d` only.
>
> 3. **No iNaturalist activity.** Do not sync, upload or share anything to iNaturalist. If the app offers to upload a find, decline it.

The launch message also said: "The L0a device check has finished: terminal `2026-09-28-09` at `46186bb`, with its run
record merged at `8e4fe67`. The phone is free, and you are the only coder on it. The L0b coder is building on this
machine in parallel, so: no Gradle, no emulator, no install." `git ls-remote origin device-l0a-2026-09-28` reads
`ecf429a`, the commit `8e4fe67` merged.

## Inventory (read-only, 04:59Z, before any item)

Read from a byte copy of the app's database (`adb exec-out run-as … cat databases/forager.db`, `-wal`, `-shm`
into `db-start-raw/`, queried only as a second copy in `db-start-query/`). Counts only.

| Data | Count |
|---|---|
| Journal entries, saved (`cartography_entries.isDraft=0`) | 1 (the L0a entry) |
| Journal entries, drafts | 0 |
| Finds (`mushroom_log_entries`), all saved (`isDraft=0`) | 2 |
| Finds with a photo (`log_entry_photos`) | 1 |
| Album photos (`log_photos`) | 3 |
| Album photos linked to a find | 1 (it is also the one photo the L0a entry keeps) |
| Album photos linked to nothing | 2 |
| Tracks | 1 (23 `track_points` rows) |
| Waypoints | 3 |
| Offline regions | 1 |
| Months the entries span | 1 (2026-09) |

All of the finds, the region, one waypoint and the entry are the L0a test data; the track and its two waypoints
are the owner's, kept by L0a.

## Found before running: four conflicts in the dispatch (stop-and-ask; see "Needs a decision")

Reading the code before the items turned up four places where the dispatch cannot be followed as written. The
items they touch are **held**, not run, until the planner rules. Every other item is run.

1. **Item 14's touches create entries.** "✎ New entry" calls `onStartEntry(LocalDate.now())`
   (`ui/log/CartographyScreen.kt:406`, `:497`), which **persists a new draft on every tap**, with every one of
   the day's candidates kept (`ui/log/CartographyViewModel.kt:115-146`). "At least five taps spread across the
   control's own bounds" would therefore make five drafts. The Data section allows one draft and one saved entry,
   and deletes only of those, each ending in Undo.
2. **"Log a find" (P:321) cannot be left with "Save nothing".** The map's route calls `onStartLogEntry`
   (`ui/availability/AvailabilityCompactScaffold.kt:733`), which is `MushroomLogViewModel.onStartNewEntry`: it
   **persists a draft find before the form shows** (`ui/log/MushroomLogViewModel.kt:323-338`). Back is
   `onLeaveEditingIncidentally`, which keeps a brand-new find's draft (`:582-630`); only the form's Cancel deletes
   it (`:531-552`), and a delete of a find is outside the Data section.
3. **Item 24 needs an entry the dispatch's title prevents.** A row collapses only with no hero, **blank text**
   and no kept track (`ui/log/CartographyEntryCard.kt:291-292`). The entry form has no title field (L0a record,
   step 4(e)), so "titled `DEVICE CHECK 2026-09-28`" means typing it into the entry's text, and the entry then
   draws as a card, not a collapsed row. Separately, a new entry made before 07:00Z is dated 2026-09-27 and keeps
   the day's track by default, which also stops it collapsing.
4. **Items 15, 24, 29, 30 and P:320 use entries this run has not made**, and making them depends on 1 and 3.

---

## Pre-registration: pass conditions and predictions, written before each item was looked at

### Item 1: build, baseline, inventory
- **Pass:** `versionName=1.0.1192+g24589349`; `logcat -b crash -d` read at the start and at the end, a new
  Forager line being a stop; the inventory above.
- **Prediction:** pass, crash buffer empty (it was empty at the end of L0a, 04:52Z).

### Item 13: Back from a single-type chip
- **Pass (from the code):** on Records with a single-type chip selected, Back selects All
  (`ui/log/RecordsTab.kt:215-217`, `BackHandler(enabled = selectedTab != ALL …) { selectTab(ALL) }`). From All,
  that handler is off and Back falls to `JournalTab`'s `selectedTopTab = CARTOGRAPHY`, i.e. Entries
  (`ui/log/JournalTab.kt:354-356`; P:302 "Back from Records steps to Entries"). A third Back, from Entries with
  nothing open, falls to the Activity-level handler (not part of this item, not pressed).
- **Observable:** the chip row's `checked`/`selected` state and the Entries | Records switch in a fresh dump after
  each `input keyevent KEYCODE_BACK`.
- **Prediction:** chip → All → Entries, as the planner predicts.

### Item 18: "📷 Add photo" in the album (portrait)
- **Pass (J2:560-566; `ui/log/EntriesAlbum.kt:195-222`):**
  - the button's text reads "Add photo" in full with its icon, not ellipsized (the dump's text node is the whole
    string and its bounds sit inside the button's);
  - each of five taps across the button's own bounds (both ends, the centre, two between) opens the menu with
    "Take photo" and "Import"; each menu is dismissed with Back before the next tap;
  - the menu's bounds lie inside the window `[0,0][1080,2316]`, and above the navigation bar (y < 2181);
  - Take photo opens the app's camera (the in-app camera window), left with Back; Import opens the system photo
    picker (`dumpsys activity` names its package), left with Back. No photo taken or imported;
  - the button's bottom is above the top of the navigation-bar inset, y = 2181 px, and above the bottom
    navigation's top.
- **Prediction:** pass. The button sits inside the Journal content above the bottom navigation
  (`CartographyScreen.kt:520-526` comment), so it clears the system bar by construction.

### Items 8 (portrait half) and 45 (portrait): the Records chip row at font scale 1.0 and 1.15
- **Pass (J1:297-300, J5:398-399; `ui/log/RecordsFilterChips.kt:68-90`, `:111`):** each chip's label is on one
  line and not ellipsized (text nodes read "All", "Finds", "Tracks", "Waypoints", "Offline maps" whole, each
  node's height one line of text); the chips' widths are recorded in dp; where the row's content is wider than
  the screen, a horizontal swipe on the row moves the chips (the last chip's bounds change, and the last chip
  comes fully on screen). Clipping with no scroll is a fail.
- **Prediction:** at 1.0 and 384 dp, the row overflows the screen (five chips with 24 dp icons and counts, 8 dp
  gaps, 16 dp ends) and scrolls; at 1.15 it overflows by more and scrolls. The planner's prediction 2 (scrolls,
  not clips) holds.

### Item 31: long-press on a tile near the bottom edge
- **Pass (J4b:325):** a long-press (`motionevent` DOWN, hold ≥ 600 ms, UP) on an album or find tile whose bounds
  come within one tile height of the bottom of the content area opens the Delete menu, whose bounds lie inside
  the window; dismissed with Back, nothing chosen.
- **Data needed:** a grid long enough that a tile sits near the bottom edge. With 3 album photos (one row) and 2
  finds, no tile can reach there.
- **Prediction:** **not run** for want of data.

### Items 10 and 17: night-mode toggles
- **Item 10 pass (from the code):** `uiMode` is not in the Activity's `configChanges`
  (`AndroidManifest.xml:160`), so a `cmd uimode night` toggle recreates it. `compactTab` is `rememberSaveable`
  (`AvailabilityScreen.kt:741` at `2458934`), and `JournalScreenState` saves the top tab, the Records filter and the
  Entries view (`ui/log/JournalScreenState.kt:85-103`). So after the toggle the app shows the Journal, on Records,
  with the same non-default chip selected. J1's "back on Maps" predates this (R:2507).
- **Prediction:** Journal, Records, the same chip, after each toggle (dark to light and back).
- **Item 17:** needs the drafts list open, which needs two or more drafts (`CartographyScreen.kt:398-404`). The
  Data section allows one draft. **Not run** for want of data; the fold half is not run on any item (the S22 does
  not fold).

### Item 20: Seasonal after a night-mode toggle, then a new search
- **Pass (J2:569-570; `AvailabilityScreen.kt:890-893` at `2458934`, `AvailabilityViewModel.kt:608-633`):** after the
  toggle the app returns on Seasonal (`selectedTab` is `rememberSaveable`, `AvailabilityScreen.kt:726`) with its
  content shown; after a search whose region, month or filter differs, Seasonal shows loading and then a new
  pattern (or an explicit error), not the old one and not a blank.
- **Prediction:** pass.
- **A choice I make here:** the only searches I can make change the owner's current search (the Maps tab's sightings
  and reticle) and upsert a `cached_searches` row (`data/local/CachedSearchDao.kt:28`, `:45`). The dispatch
  allows the network for this item; I will change the month and change it back, and record both searches. See
  Decisions.

### Items 9 and 22: theme observations
- **Item 9 (observation):** in light, dark and Understory, each chip's fill and label colour read from the
  screenshot as hex, selected and unselected, from `RecordTypeStyle` (`ui/log/RecordTypeStyle.kt:43-46`: Finds
  `secondary`/`secondaryContainer`, Tracks `tertiary`/`tertiaryContainer`, Waypoints `primary`/`primaryContainer`,
  Offline maps `onSurfaceVariant`/`surfaceContainerHighest`; All the `FilterChip` defaults). A pair "looks too
  close to read" is judged by my reading of the image, and the contrast ratio is computed.
- **Item 22:** needs entries in two or more months. The phone has one month. **Not run** for want of data.

### Items 11, 19, 21, 23 and 28: real-data observations (sizes from dumps; no content quoted)
- **11 (J1:463-464):** the All list's find tiles two to a row, the type badges' sizes and what they overlap, the
  region row's height and whether a long name wraps (the only region name is short, so the wrap part lacks data).
- **19 (J2:567-568; `EntriesAlbum.kt:296-340`):** on the 3-column album the tile is about (384 − 2×16 − 2×3)/3 ≈
  115 dp here, not 107 dp; badges are 22 dp circles with 14 dp icons, 4 dp from the tile's bottom-start corner.
  The one photo linked to both an entry and a saved find should carry both badges; the other two none.
- **21 (J3:342-348):** the L0a entry's card: day numeral beside the title, the stats row, the 140 dp hero
  (`CartographyEntryCard.kt:332`) and the track thumbnail.
- **23 (J3:351-352):** the card thumbnail 56 dp (`CartographyEntryCard.kt:358`) and the Tracks row thumbnail
  40 dp (`ui/track/TrackExportPanel.kt:193`), measured and judged legible or not by my reading of the image.
- **28 (J4:268):** the region row's own background against the list's.
- **Prediction:** the sizes match the code at this density; nothing overlaps.

### Item 47 (portrait half): the details sheet's top
- **Pass (J5c:319-320; `ui/log/RecordDetailsSheet.kt:162-181`):** a tap on a waypoint, track or region row opens
  the sheet; the sheet's topmost node (its drag handle or its top edge) is at y ≥ 75 px, below the status bar and
  the cut-out; Back dismisses it.
- **Prediction:** pass (Material 3's `ModalBottomSheet` applies the status-bar inset itself; unverified until seen).

### Item 52: Directions and Share from the details sheet
- **Pass (J5c:327):** Directions on the waypoint sheet (`RecordDetailsSheet.kt:206`, `launchDirections`) hands off to
  an installed maps app, and Share on the track sheet (`:243`, `shareTrackGpx`) opens the system share sheet; each
  target's package is read from `dumpsys activity activities`; each is left with Back without acting. Nothing is
  shared to iNaturalist or anywhere.
- **Prediction:** Directions opens Google Maps (`com.google.android.apps.maps`), unverified; Share opens the
  Android share sheet.

### Items 19, 26, 32 and 50: TalkBack, partial (from dumps)
- **What a dump can give:** `uiautomator dump` writes text, `content-desc`, `clickable`, `long-clickable` and
  bounds. It does not write custom accessibility actions or click and long-click labels.
- **Pass:** each named control's content description and clickable/long-clickable flags are recorded; the custom
  actions ("Edit", "Delete", `TwoStageSwipe.kt:196-212`) and the labels ("Options for …", "Details for …",
  `RecordDetailsSheet.kt:113-116`) are recorded as **not readable from a dump**, with the code's values quoted as
  the code's, not as seen.
- **Prediction:** partial.

### Item 12: scrolling performance
- **Needs** 100 or more items in All. All holds 2 + 1 + 3 + 1 = 7. **Not run.**

### Held until the planner rules (see "Found before running")
Item 14, P:320 (draft Continue), P:321 (the map's "Log a find"), item 15, item 24, item 30, item 29, and
therefore any use of the entries in Data (a).
