# Motion Part 3: code and tests written, nothing compiled yet

Dispatch 2026-09-28-676 (RECORD intent -676; preserved at `prompts/preserved/2026-10-07-13.md` on branch `records-after-173`).
The owner's choices: RECORD -651, with the night-mode note in -652 and "Use it, then prune" in -655. The scout:
`docs/ui/2026-10-07-motion-scout.md` on branch `motion-scout` (8a40c6d4). Branch `motion-part-3`, cut from `origin/motion-part-2`
at `008ebbab`, the base the dispatch names (the worktree was cut from a fresh fetch, and `git ls-remote` showed the remote at the same commit before any code was written).
Part 2 is not merged; nothing here is on `main`. No PR.

**Status: written, not run.** The dispatch holds Gradle for the planner's go. Nothing on this branch has been compiled, no test
has run, and no revert check has been done. Every claim below about behaviour is what the code is written to do, not something
observed.

Paths are relative to `app/src/main/java/com/zynergylabs/forager/app/ui/` unless they start with `app/` or `docs/`.

## What was built

1. **Journal pages, "Slide in, slide back"** (`motion/PageSlide.kt`). `PageSlide` shows one page at a time: a page at least as
   deep as the current one slides in from the right **over** it, the page beneath staying still; a shallower one is uncovered by
   the current page sliding out to the right, the page beneath in place from the first frame (Back retraces the way in). A
   leaving page takes no touch (Part 1's `leavingTakesNoTouches`), keeps only its leaving marker in the semantics tree (Part 2's
   fix (a)), and has its Back handlers off (Part 2's inert Back owners, now `internal` in `motion/TabCrossfade.kt` and shared).
   Pages with the same key update in place, so typing in an editor never slides. Each page carries what it shows (the open
   find or entry), so a report sliding out after its find has closed still draws. `SlideOverPage` is the same for a page that
   comes in over content that stays composed beneath it. Applied to:
   - **J1, J2**: Entries | Records (`log/JournalTab.kt`). Records slides in over Entries from the switch; Back or the switch slides
     it out to the right.
   - **F1, F2**: the Finds pages (`log/JournalTab.kt`, new `FindsPage`): gallery, report, editor, location picker, From Album.
   - **F3**: the find opened over the view from a map bubble slides in over the view and out again (`SlideOverPage`). While it
     comes in, nothing beneath takes a touch over the whole area (it swallowed every touch before); while it goes, it takes
     none. As it goes it keeps drawing the page it last showed, not the gallery.
   - **J3, J4**: Entries' home, the drafts list, an entry's report and editor, and the spinner before a new entry's editor
     (`log/CartographyScreen.kt`, new `EntriesPage`). The early returns became pages; each branch's Back handler moved into its
     page, so it is composed only while that page shows, as before.
   - **J7**: the editor's "+ Add a photo from the Album" picker is now `CartographyScreen`'s own page, sliding in over the editor.
     Its flag is held by `CartographyScreen` and passed to `CartographyEntryEditScreen` (`pullingPhotoState`, `drawsPhotoPicker =
     false`); the editor on its own keeps its old behaviour by default.
2. **Lists, "Slide and close up"** (`motion/ListMotion.kt`). `rememberListRows` keeps a row that has gone missing where it was,
   marked as leaving, until `ListRowMotion` has faded and shrunk it; the rows below close up as its height goes. If it comes back
   while leaving (Undo), it is the same row turned round. A new row fades and grows in. A lazy list adds `animateItem`'s
   placement glide so a moved row glides to its place. Nothing animates on a list's first rows (opening, or loading into an empty
   screen) or after its `resetKey` changes. A leaving row takes no touch and leaves the semantics tree. Applied to:
   - Entries (J9; `log/CartographyEntryListScreen.kt`, so the drafts list too, which uses it), month headers kept with their rows;
   - Finds tiles (F6; `log/FindsGalleryScreen.kt`), shrinking in place as tiles; switching Log and Drafts is a `resetKey`, so
     it stays instant;
   - Records: the All logbook's timed rows, and a whole day when its last record goes (R4; `log/RecordsLogbookList.kt`), and the
     Tracks list (`track/TrackExportPanel.kt`);
   - waypoints (P2; `availability/AvailabilityTripsWaypointsUi.kt`) and regions (R15; `availability/AvailabilityOfflineMapsUi.kt`);
   - the List tab's results (L4; `availability/AvailabilityResultsUi.kt`).
   Swipe rows are wrapped, not changed: their drag, snap and actions are as before.
3. **Words, "Numbers instant, words fade"**: the Records logbook's day header ("· 1 record" / "· 2 records", R7) through Part 2's
   `WordSwap`. Every other word item is a stop (below).
4. **Night mode, "Fade the colours", fast and smooth** (`theme/ColorSchemeFade.kt`, wired in `theme/Theme.kt`). Every role of the
   colour scheme blends from light to dark or back on the motion scheme's fast effects spring, about a tenth of a second
   (critically damped, stiffness 3800; worked out, not measured). At rest the app is drawn with `LightColors` or `DarkColors`
   themselves. Instant under reduced motion.
5. **Prune** (owner, RECORD -655: "Use it, then prune"), with no production caller after this part, checked by `git grep`
   across the tree:
   - from `motion/MotionTokens.kt`: `markerEntranceSpec`, `MARKER_CLUSTER_STAGGER_STEP_MS`, `selectionPulseSpec`,
     `SELECTION_PULSE_MIN_SCALE`, `SELECTION_PULSE_MAX_SCALE`, `narrativeRevealSpec`, `ROUTE_REVEAL_MS_PER_KM`,
     `routeRecalculationMorphSpec`, `dataLayerOverlaySpec`;
   - `motion/MotionPrecedence.kt` entire (precedence order, degradation tiers, `activeTiers`, the 12-object budget, the clustering
     threshold), with `app/src/test/.../motion/MotionPrecedenceTest.kt`.
   Kept, because something calls them: `feedbackMotionSpec`, `PRESS_BOUNCE_SCALE`, `iconSwapSpec`, `ICON_SWAP_ENTER_SCALE`,
   `panelMotionSpec`, `navigationMotionSpec`, `tabCrossfadeSpec`, `mapPopUpFadeSpec`, `mapPopUpGrowSpec`, `MAP_POPUP_ENTER_SCALE`,
   `navigationViewChromeSpec`, `wordSwapSpec`, `LOCATION_INDICATOR_MOVE_DURATION_MS` (`map/SightingsMap.kt:1730`), and the three new
   ones. `docs/motion-spec.md` marks each removed name where it was cited, with the rule text left as written, and ADR-0002 gains a
   dated amendment rather than an edited table.
   **Found, not pruned (outside the dispatch's two files):** `MotionTreatment`, `ReducedMotionTreatment` and
   `reducedMotionEquivalent` in `motion/ReduceMotion.kt` have no production caller either.

New tokens: `pageSlideSpec` and `listRowSpec` (`defaultEffectsSpec`), and `nightModeFadeSpec(scheme)` (`fastEffectsSpec`, read from
the scheme the theme provides, since it fades the theme's own colours). No `tween` or `spring` at a call site. `docs/motion-spec.md`
§2 and §4 gain the rows; ADR-0002 gains the amendment with the reason the two moving categories sit on a critically damped spec.

## Verified before building, and how

By reading this branch at `008ebbab` unless said otherwise.

- The base: a fresh fetch of `origin/motion-part-2` gave `008ebbab`, and `git ls-remote` agreed before any code was written.
- The helpers the dispatch names exist: `leavingTakesNoTouches` (`motion/LeavingTakesNoTouches.kt`), `WordSwap`
  (`motion/WordSwap.kt`), `clickableWithShapedPress` (`motion/PressFeedback.kt:138`), `BouncingIconButton`
  (`motion/BouncingIconButton.kt:21`), and fix (a) (`motion/TabCrossfade.kt`, `clearAndSetSemantics` on the leaving tab).
- `MotionTokens.kt` had no raw `tween`/`spring` but Part 2's one exception (`navigationViewChromeSpec`), which is untouched.
- Every Journal screen move in the scout was a plain `when` or an early `return` (`log/JournalTab.kt:661`, `:489-586`, `:793-798`;
  `log/CartographyScreen.kt:449-519`; `log/CartographyEntryEditScreen.kt:178-197`), so no screen kept a page composed to slide.
- Each map-hosting page calls the map slot once, and each call is its own `MapView` (Part 2's report; the comment at
  `log/CartographyEntryReportScreen.kt:472-474`). A slide keeps the leaving page composed, so a report with a map sliding out over
  a list keeps one map; Entries with an open entry map sliding under Records on its Offline maps chip holds two for a moment.
- The lists: Entries and Finds are lazy grids with stable keys (`log/CartographyEntryListScreen.kt:147-148` at `008ebbab`,
  `log/FindsGalleryScreen.kt:136`, `:140`), the List tab a keyed `LazyColumn`, and Records, Tracks, waypoints and regions plain
  `Column`s keyed with `key(...)`.
- Material 3's colour scheme: `ColorScheme` in material3 1.5.0-alpha26 is immutable (48 `final long` fields) and provided through
  `staticCompositionLocalOf` (both read from the jar's bytecode with `javap` in this session). Hence a whole-tree recomposition
  per frame of the night fade.
- `ExitTransition.KeepUntilTransitionsFinished` is `internal` in animation 1.12.1 (bytecode, `$animation` suffix), so the covered
  page uses `ExitTransition.None` and relies on AnimatedContent keeping it until the slide-in ends (unverified; see below).
- The remaining production callers of every token, by `git grep` over `app/src/main`, before and after the prune.

## Premises that were wrong or incomplete

- **"Use Part 2's `WordSwap`" for status lines and labels.** Most of the scout's word items sit in or above something tappable.
  `WordSwap` holds the larger of the two lines for the length of its crossfade, so a button whose label changes (F9), a row whose
  height changes (R16, R18), or a button beside a label that changes length (J15's Continue) would hold its old touch area or
  place for that moment. That is a touch-area change, which the dispatch makes a stop; Part 2 left the coordinates' swap (C5)
  instant for the same reason. Only R7 moves nothing.
- **"Prune MotionTokens.kt and MotionPrecedence."** `motion/ReduceMotion.kt` holds three more unused declarations
  (`MotionTreatment` and friends), outside the two files named; reported, not removed.
- **The scout's R4** says the Records lists are plain columns, so the lazy item animation is not available. True; the leaving
  rows are kept by `rememberListRows` instead, which works in both. The consequence: a row that **moves** in a plain column (not
  added, not removed) still jumps. Records, Tracks, waypoints and regions are sorted by time, so this is only seen if a record's
  time changes.
- **The scout's J9** includes the Entries album grid. The album is photos, not entries; not built, listed as a stop.

## Stops for the owner (not decided here)

1. **An arriving page takes touches where it is drawn.** The slide moves a page by placement, not by drawing alone, because two
   Journal pages host a live map, and a hosted `View` follows placement. So for the length of a slide-in, the arriving page's
   controls are where they are drawn, on their way. Settled touch areas are unchanged. The alternative (touches held at the
   settled place, as Part 2's pop-ups do) would leave a map page's map drawn at its settled place while the rest of the page
   slides. Keep, or ask for another way?
2. **Short landscape window: the L1 row slides with each page.** The row holding the Entries | Records switch is drawn inside
   each page (`CartographyScreen`'s `ShortWindowFrame`, Records' column), so in a short window it slides in and out with the
   page, switch included. Keep, or hoist the row above the slide (a structural change to both screens)?
3. **Word items left instant** because a crossfade would move a touch area: F9 (Add Location / Change Location button), J15
   (drafts banner label beside Continue), R16 (a region's Not downloaded / Stale lines inside its tappable row), R18 (No location
   picked yet / Download region, above the slider and buttons). Leave instant, or crossfade and accept the moment?
4. **Word items left instant for other reasons**: Q7 (the search summary is the search field's placeholder; the field's slot is
   one of the scout's deliberately instant items); T6 (the folder name appears, it is not a swap); P4 ("Today" appears, not a
   swap); T8 (the restore caption).
5. **Screen moves not clearly covered by "Journal pages"**, left instant: J5 (timeline / album toggle), J6, J10, F5, F8 (spinner,
   empty and content swaps), E1 (the entry map going fullscreen: a size animation of a live map), E2 and E3 (the entry map and
   offline row arriving late), R1 (the filter chips' list swap), F4 (Log / Drafts), R11 and R12 (one bottom sheet replacing
   another), T1 to T3 (the Tools drawer's pages: "Journal pages" names the Journal), K1 (the camera, a full-screen dialog), V1
   to V5 (the photo viewer, a dialog window).
6. **R2**: a find tapped in the All logbook selects the Finds chip and opens its report together. The chip change is R1 (not
   covered), so the report appears at once on the way in; Back from it slides it out over the Finds gallery. Slide it in too?
7. **Lists not built**: the Entries album grid (J9's album half), find pairs re-pairing in the logbook (R5), the trip planner's
   cards (P1), the find editor's photo row (F7). Which, if any?
8. **Reduced motion** (my calls, under §4's "fade or instant"): pages change at once with no leaving page kept; list rows come
   and go at once; night mode changes at once. The alternative for each is a plain fade.

## Decided beyond the dispatch's words (my calls, open to reversal)

- **Slides and list motion on a critically damped spring** (`defaultEffectsSpec`), not a spatial one: an expressive spatial
  spring overshoots, which on a slide-in bares a strip of the page beneath at the right edge, and on a list bounces every row
  below. Recorded in ADR-0002's amendment with the alternative.
- **Depth decides direction.** Records is "in from" Entries (it is the side Back leaves); a find's pages go gallery, report,
  editor, pickers; Entries' go home, drafts list, report, editor, album picker. Save from an editor back to its report slides the
  editor out, as Back would.
- **The spinner before a new entry's editor and the editor are one page**, so the editor replaces its spinner in place instead of
  sliding twice.
- **Grid tiles** (Finds, Entries in two columns) shrink in place and the grid closes up after them with a glide, rather than
  shrinking in height, which in a grid would leave the row's other tile standing at full height.
- **Night mode**: things that pick their colour from whether night mode is on, not from the scheme (`LocalForagerDarkTheme`: parts
  of the map chrome), the system bars' icons, and the map's basemap reload (G2) all change at the start, as before.

## Overlap with other branches

`data-a-entry` (data part A, unmerged) changes `log/CartographyScreen.kt` (13 lines, inside the entry detail this part turned into
a page), `log/CartographyEntryEditScreen.kt` (235 lines) and `log/JournalTab.kt` (8 lines). This branch's edits to those files were
kept small where they meet (the entry detail keeps its body and names; the editor gains two parameters and three changed lines),
but the second of the two to merge main will have conflicts to resolve by hand. `git diff origin/motion-part-2...origin/data-a-entry`
was read for this; nothing was merged.

## Tests written (none run)

Under `app/src/test/java/com/zynergylabs/forager/app/ui/`. Every touch is a real coordinate touch; every mid-animation read is
with the clock stopped and the state write applied before frames are stepped.

- `log/JournalPageSlideTest.kt`, through the real `JournalTab` under `ForagerTheme`:
  - a find's report slides in over the gallery; mid-slide a real touch on the gallery's "+" tile, still drawn and not yet covered,
    starts no new find;
  - Back slides the report out to the right with the gallery in place, and a touch on the uncovered tile reaches the gallery;
  - Back, just started: a touch on the leaving report, where it is still drawn, goes through it to the gallery's tile beneath;
  - under reduced motion the report replaces the gallery at once;
  - Records slides in over Entries from the switch, and Back slides it out with Entries in place at the left edge;
  - a find opened over the view (the real `VIEW_FIND` request) slides in, and a touch on the still-uncovered Records half of the
    switch does nothing;
  - Back slides that find out with the view beneath in place.
- `availability/ListMotionTest.kt`, through `WaypointsSection` and `CartographyEntryListScreen`, deleting by each row's own Delete
  accessibility action: a deleted waypoint shrinks from its own top while the row below is caught between its two places, and a
  touch on what is left of it (first shown to open it when at rest) opens nothing; Undo while leaving grows it back from where it
  had got to; reduced motion removes it at once with the row below in its place; a deleted Entries card in the lazy list takes no
  touch while leaving and the next card closes up into its place.
- `motion/ListRowsMergeTest.kt` (plain JUnit): where a leaving row is kept, two at once, Undo reusing the same state, a new row
  starting hidden, nothing kept when not animating, a reorder keeping each row's state.
- `theme/NightModeFadeTest.kt`, through `ForagerTheme(darkTheme)`: a frame is drawn part-way between light and dark (by luminance,
  since Compose blends in Oklab), it ends exactly on the dark scheme, it is over within half a second of frames, under reduced
  motion only the two ends are ever drawn, and the blend's last listed role moves with the rest.
- `motion/MotionTokensTest.kt`: the pruned categories' tests removed; the three new ones mapped and critically damped.
- Deleted: `motion/MotionPrecedenceTest.kt`, with the code it tested.

**Not covered by a test:** what the find over the view draws as it leaves (its semantics are cleared, so its last page cannot be
read); Entries' pages (J3, J4) and the album picker (J7), which share `PageSlide` with the tested Finds pages but are not driven
separately; the Finds grid, Records logbook, Tracks list, regions and List tab rows (the same `ListRowMotion` as the two tested);
the R7 header's crossfade (`WordSwap` is tested on its own by Part 2).

**Existing tests at risk, untouched:** tests that read the semantics tree in the same frame as a Journal page change, a list
delete, or a night-mode change with the clock stopped (most settle and are unaffected, as in Part 2); tests reading the Records
day header's bounds (its top padding moved from the text to the `WordSwap` box around it); tests that composed
`CartographyEntryEditScreen` inside `CartographyScreen` and expect the album picker inside the editor's node.

**Revert checks planned** (each from a saved copy, the compile log checked for errors before any result is read, only XML newer
than the run read, and the forward file confirmed restored by checksum):
- R1 `LeavingPageFrame` without `leavingTakesNoTouches`: the "+" tile test fails (a new find started) and the leaving-report test
  fails (the touch is taken by the report).
- R2 the forward slide replaced by `EnterTransition.None`: "the report part-way in" is never seen.
- R3 the back slide replaced by `ExitTransition.None`: "Records part-way out" is never seen.
- R4 `SlideOverPage` without its touch blocker: the Records half of the switch is selected.
- R5 `ListRowMotion` without `leavingTakesNoTouches`: "a leaving row opens nothing" fails, in both list tests.
- R6 `rememberListRows` never animating: "the creek row part-way out" is never seen.
- R7 `mergeListRows` giving a returning row a new state: "Undo turns the same row round" fails.
- R8 `fadingColorScheme` returning the target scheme: "some frame part-way" fails.
- R9 its reduced-motion branch removed: "only the two ends were ever drawn" fails.
- R10 `pageSlideTransform`'s reduced-motion branch removed: "nothing sliding in" fails.

## Unverified because nothing has been compiled

Everything, and in particular:
- that it compiles, including `ContentTransform`'s constructor with named arguments, `animateItem` in the grid and list scopes,
  `run { }` in the logbook's `when`, and the shadowed `editingEntry` parameter in `CartographyScreen`'s entry detail;
- that `ExitTransition.None` keeps a covered page composed and drawn until the slide-in ends (my reading of AnimatedContent, not
  its source in this session; the first test reads the covered page mid-slide, so it will show it);
- that `boundsInRoot` of a leaving page's marker reports its slid position (the tests read it);
- that a `LaunchedEffect` (the `VIEW_FIND` request) runs while the clock is stepped frame by frame (the F3 test depends on it);
- that `AnimatedContent` with a matching key updates a page in place (Part 2's `WordSwap` relies on the same and was green).

## Device checks (S22, after the build)

Each Journal slide in portrait and landscape, light and dark: Entries and Records, a find's pages, an entry's report and editor,
the album picker, the find over the view; the entry report's map sliding (whether a `SurfaceView` map follows the slide and is
clipped at the Journal's edge, or draws over the landscape rail); Entries with an open entry map sliding under Records on Offline
maps (two maps for a moment); the short window's L1 row sliding with its page; list deletes and Undo in Entries, Finds, Records,
Tracks, waypoints and regions, and a List tab results update; the night-mode blend from Settings, whether it is quick and smooth
with the whole app recomposing each frame, and whether the map chrome and system bars changing at the start read as a flash; all
of it again with the phone's animations off.

## Amendment 1 (RECORD -681), applied 2026-10-08, also not compiled

The owner, verbatim: "Same rule everywhere (Recommended)" and, for the short window, "Keep it still (Recommended)". The planner's
calls: R2 slides in as well as out; word items beside tappable controls stay instant; an arriving page taking touches where it is
drawn is accepted; reduced motion stays instant; the three `ReduceMotion.kt` declarations are removed. `origin/main` (bc85fd29,
Parts 1 and 2) was merged in first (131f698c, no conflicts). `data-a-entry` was not yet on `main` and is not merged here.

**Built** (171dd53f, then the tests and this section):
- **Pages are opaque.** `PageSlide` draws every page on the screen's background (`pageColor`, default the Scaffold's
  `colorScheme.background`). Found while applying the rule more widely: the Journal's pages draw no background of their own, so
  without this a page sliding in would have shown the one beneath through it. This applies to everything in the first part too.
- **The short window's L1 row stays still.** `JournalTab` draws the row (switch, search, action) above the pages; Entries hands
  up the action for its last slot through a new `shortWindowHeaderAbove` state (`log/CartographyScreen.kt`), remembered per view
  so it changes only when the view does; Records no longer draws the row. Only the page below slides.
- **R1, R2**: a Records chip's list slides in over All, Back (which steps to All) or the All chip slides it out; chip to chip
  slides in. A find tapped in All brings its report in with the Finds list (`log/RecordsTab.kt`). The chip row stays still.
- **J5**: the album slides in over the timeline; Back or the toggle slides it out. The toolbar stays still
  (`log/CartographyScreen.kt`).
- **F4**: Drafts slides in over Log, and Log slides it out; each tab's grid is its own page with its own rows
  (`log/FindsGalleryScreen.kt`; the `resetKey` from the first part is no longer used there).
- **Lists**: the Entries album (`log/EntriesAlbum.kt`, tiles kept with their day header while they leave), planned trips (P1,
  `availability/AvailabilityTripsWaypointsUi.kt`), the find editor's photos (F7, `log/LogEntryDetailScreen.kt`) and the logbook's
  find tiles (R5, `log/RecordsLogbookList.kt`).
- **Pruned**: `MotionTreatment`, `ReducedMotionTreatment` and `reducedMotionEquivalent` from `motion/ReduceMotion.kt`, with the two
  `ReduceMotionTest` tests that exercised only them.

**New stops** (the rule cannot cover these cleanly):
1. **J6, J10, F5, F8, V4, K2**: a spinner, an empty message or an error giving way to content. No page is opened on top and there
   is no Back to retrace, so "slide in from the right" has nothing to mean. Option: a quick crossfade, or leave instant.
2. **E1** (entry map to fullscreen): the map is one `View` that must not be torn down, so fullscreen cannot be a second page; the
   only motion is growing its container, which re-measures the live map every frame and moves its touch area. Leave instant?
3. **E2, E3** (the entry map and the offline row arriving late): content appearing, not a page. Grow in like a list row, or leave?
4. **R11, R12** (one details sheet replacing another): each is its own `ModalBottomSheet` with Material's motion; a slide between
   them needs one sheet holding both pages, which changes the sheet's height (and touch area) mid-change. Leave as is?
5. **T1 to T3** (the Tools drawer's pages): over the Maps tab the drawer is 80% (`AvailabilityScreen.kt`, `drawerContainerColor`).
   An opaque page breaks "nothing fully obstructs the map"; a page at 80% doubles up over the drawer's own 80% (the rule says layered
   fills composite to 0.8, `MapChromeAlphaTest`); a transparent page shows both pages through each other mid-slide. Slide only
   where the drawer is solid (off Maps), or leave instant everywhere?
6. **K1** (the in-app camera): it is its own full-screen window, tied to status-bar hiding and the rotation that stays the
   system's, with two touch-through bugs on record; a leaving camera window cannot let touches through while it slides away.
7. **V1** (the photo viewer opening): also its own window, with the same limit on its way out. **V2** (next and previous) moves
   sideways between photos, not on top; **V3** (double-tap zoom) and **V5** (Save's state) are not pages. Which, if any?
8. **F7 and R5 still jump at the end**: the removed tile fades and shrinks in place, but the find editor's photos (a `FlowRow`) and
   the logbook's find pairs (a plain column) then reflow at once; neither layout has a placement glide. A glide there needs a
   placement animation this app does not have yet.

**Tests written (not run):**
- `log/JournalPageSlideTest.kt` gains two: a Records chip's list slides in over All with the chip row still and slides out on
  Back with All in place; and in a short window (`w823dp-h384dp-land`) the switch does not move while Records slides in, with
  exactly one switch on screen. Before this amendment the switch was inside each page, so both of those reads would fail.
- **Assertions changed in existing tests:** none. **Tests removed:** `ReduceMotionTest`'s two mapping tests, with the code they
  tested. `MotionTokensTest` was already changed in the first part, for the first prune.
- **Not covered by a test:** J5, F4, the album, trips, the find editor's photos and the find pairs (the same `PageSlide` and
  `ListRowMotion` as the tested ones); the opaque page background (no test reads colour mid-slide).
- **Revert checks added to the plan:** R11, the L1 row drawn back inside the pages (`shortWindowHeaderAbove` ignored): the short
  window test fails on "exactly one switch"; R12, the chip lists without their `PageSlide`: "the Waypoints list part-way in" is
  never seen.

**Existing tests now also at risk, untouched:** the short-window Journal tests (`AvailabilityScreenJournalShortWindowTest`), which
may read the L1 row as part of Entries' or Records' content, or read the Entries action in the row before Entries has handed it up
(it arrives one frame after the view changes, after composition).

