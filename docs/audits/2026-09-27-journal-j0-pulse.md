# 2026-09-27: Journal redesign J0 pulse, re-run in the cloud

J0 (`docs/plans/journal-redesign.md`, "Build order") ran once on 2026-09-27 at
`545258e`, but its answers stayed in that planner's session (RECORD.md
dispatch-note `2026-09-27-34`, "not relayed to the record"). This is a re-run
against `pre-main` at `352b708` (after landscape B3, #139), by three read-only
pulses in parallel under `prompts/preserved/2026-09-27-15.md`. Each read the
working tree at `e871179`, which differs from `352b708` only in `RECORD.md` and
the store copy (`git diff --stat 352b708 e871179`), so the citations below hold
at `352b708`. No pulse edited a file or ran a build. The planner condensed each
hand-back; claims marked "inferred" were marked so by the pulse.

Paths are under `app/src/main/java/com/zynergylabs/forager/app/` unless stated.

## Part A: structure, routing and state

### A1. The five screens (J0 question 1)

Callers found with `git grep -n -E '\b(CartographyScreen|RecordsTab|FindsGalleryScreen|PhotoGalleryScreen|LogPanel|JournalTab)\('`
over `app/src`, declarations dropped.

- **`ui/log/CartographyScreen.kt`** (340 lines). One public composable,
  `CartographyScreen` (:75); `private enum CartographyTab { ENTRIES, DRAFTS, ALBUM }`
  (:337). Owns `mode` (`remember`, :117), `confirmingLeaveEntry` (:135),
  `selectedTab` (`remember`, :281), and three `rememberSaveable` flags
  (:175, :176, :202); its own `BackHandler(enabled = editingEntry != null)` (:153).
  Receives everything else through `CartographyUiState`; `editingEntry` is its
  navigation state (:137). Renders the edit or report screen when an entry is
  open (:223, :256, returning at :271), else a `SecondaryTabRow` of Entries,
  Drafts, Album (:284-292): Entries and Drafts are `CartographyEntryListScreen`
  (:304, :314), Album is `PhotoGalleryScreen` (:322). Called by
  `JournalTab.kt:417` and `LogPanel.kt:366`; test `CartographyScreenTest.kt:73`.
  *Inferred:* `selectedTab` sits after the early returns, so opening an entry
  and backing out lands on Entries, even when it was opened from Drafts.
- **`ui/log/RecordsTab.kt`** (258 lines). `RecordsTab` (:78);
  `enum RecordsSubTab { WAYPOINTS, OFFLINE_MAPS, RECORDED_TRACKS, FINDS }` (:258).
  Owns `selectedTab` (`remember`, default WAYPOINTS, :124), a one-shot
  `pendingSubTab` latch (:126-131), open callbacks for Offline Maps and Tracks
  (:133-139), and a `BackHandler` (:152). Finds content arrives as a slot
  (:98). Sub-tabs render `WaypointsSection`
  (`ui/availability/AvailabilityTripsWaypointsUi.kt:173`), `OfflineMapsPanel`
  (`ui/availability/AvailabilityOfflineMapsUi.kt:112`), `TrackExportList`
  (`ui/track/TrackExportPanel.kt:58`) and the finds slot (:248). Called by
  `JournalTab.kt:452` and `LogPanel.kt:404`; no direct test.
- **`ui/log/FindsGalleryScreen.kt`** (236 lines). `FindsGalleryScreen` (:64);
  `private enum FindsGalleryTab { LOG, DRAFTS }` (:137). Only state:
  `selectedTab` (`remember`, :88). Cover photo is `entry.photos.firstOrNull()`
  drawn with `DecodedPhoto` (:192-194). Called by `JournalTab.kt:371` and
  `LogPanel.kt:321`; 9 calls in `FindsGalleryScreenTest.kt`.
- **`ui/log/PhotoGalleryScreen.kt`** (253 lines). `PhotoGalleryScreen` (:78).
  Owns photo-acquisition launchers (:92), `viewingPhotoId`
  (`rememberSaveable`, :96) and a per-tile `confirmingDelete` (:169). Called by
  `CartographyScreen.kt:322` (Album) and `ui/availability/AvailabilityScreen.kt:1198`
  (the medium/expanded drawer, without reference counts); 11 calls in
  `PhotoGalleryScreenTest.kt`.
- **`ui/log/LogPanel.kt`** (455 lines). `LogPanel` (:100), the wide-tree
  counterpart of `JournalTab`: a "Mushroom Log" header (:439), a
  Cartography/Records tab row (:347), four `remember` flags (:213, :216, :219,
  :225), a `pendingDestination` latch (:227) and two `BackHandler`s (:258,
  :268). No `mode`: an open find always shows the editor (:72-73). Called by
  `AvailabilityScreen.kt:1113`; test `LogPanelTest.kt:63`.
- **`JournalTab`** (`ui/log/JournalTab.kt`) owns `mode` (:226), the two picker
  flags (:232, :236), `selectedTopTab` (:238, default CARTOGRAPHY) and
  `recordsPendingSubTab` (:243), all `remember`, and is called only from
  `ui/availability/AvailabilityCompactScaffold.kt:913` inside
  `when (compactTab)`. *Inferred:* leaving the Journal bottom tab discards all
  of that state and everything nested under it.

### A2. Routing and Back (J0 question 6)

`git grep -n -E 'pendingDestination|onFindsTabLeft|pendingJournalDestination|PendingJournalDestination' -- app/src`,
comments excluded.

- `PendingJournalDestination { EDIT_NEW_FIND }` (`JournalTab.kt:515-518`) is
  held at `AvailabilityScreen.kt:830` (`remember`). Set by the wide tree's
  "Log a find here" (`AvailabilityScreen.kt:1268`) and the compact one
  (`AvailabilityCompactScaffold.kt:707`, after switching to the Journal tab at
  :702). Passed to `LogPanel` (`AvailabilityScreen.kt:1189`) and `JournalTab`
  (`AvailabilityCompactScaffold.kt:987`); cleared at `AvailabilityScreen.kt:1190`
  and `AvailabilityCompactScaffold.kt:988`. Consumed at `JournalTab.kt:245-255`
  (Records, EDIT, Finds) and `LogPanel.kt:227-236` (the same without `mode`),
  both handing off to `RecordsTab`'s latch (:126-131). Tests:
  `JournalTabTest.kt:80, :83, :198, :207, :334`.
- `onFindsTabLeft` is declared at `RecordsTab.kt:99` and invoked only at :142,
  when leaving FINDS. Passed as `::leaveFindEditingIfNeeded` from
  `JournalTab.kt:476` (acts only in EDIT mode, :260-262) and `LogPanel.kt:428`
  (no mode check, :241-243). No test references it.
- `JournalTab.kt` has exactly two `BackHandler`s:
  1. :281-289, enabled when a find is open or a picker is up; closes the
     pickers first, then leaves an EDIT-mode find incidentally, else closes the
     find.
  2. :299-301, enabled on Records when (1) is not; returns to Cartography.

  Related: `RecordsTab.kt:152` returns to Waypoints from any other sub-tab;
  `CartographyScreen.kt:153` asks before leaving an open entry.
  *Could not determine:* whether a find can be open while the Cartography top
  tab shows, which would let handler (1) take Back there.

### A3. Rotation and where state lives (J0 question 7)

- Confirmed: `AndroidManifest.xml:160` handles
  `orientation|screenSize|screenLayout|keyboardHidden`, with its reasoning at
  :140-156, pinned by `app/src/test/.../MainActivityConfigChangesTest.kt:32-36`.
  A plain rotation does not recreate the Activity. A night-mode toggle, a
  smallest-width change (fold, some multi-window resizes), locale, font scale
  or density change, and process death still do (standard Android behaviour,
  not device-checked).
- A phone stays in the compact tree in both orientations
  (`AvailabilityScreen.kt:1437`, `isShortWindow` at :961). *Inferred:* a
  tablet or foldable crossing that line swaps `JournalTab` for `LogPanel` and
  loses local Journal state without any recreation.
- Plain `remember`: `compactTab` (`AvailabilityScreen.kt:693`), `drawerPanel`
  (:799), `pendingJournalDestination` (:830); every Journal tab and sub-tab
  selection listed in A1; the edit and report screens' local flags (keyed on
  `entry.id`); the Records sub-tabs' pending-delete dialogs
  (`AvailabilityOfflineMapsUi.kt:340`, `AvailabilityTripsWaypointsUi.kt:185`).
- `rememberSaveable`: `CartographyScreen.kt:175, :176, :202`;
  `PhotoGalleryScreen.kt:96`; `LogEntryDetailScreen.kt:237`;
  `LogEntryReportScreen.kt:90`; `PhotoAcquisitionLaunchers.kt:88`;
  `PhotoViewerDialog.kt:116`. No `rememberSaveable` anywhere in
  `ui/availability`.
- ViewModel-held (survives recreation, not process death): the open entry
  (`MushroomLogUiState.kt:45`, `CartographyUiState.kt:31, :45`) and the lists.
  No `SavedStateHandle` in either Journal ViewModel (the only hits are comments
  in `MapPreferencesRepository.kt:15` and `InAppCameraViewModel.kt:27`).
  *Inferred:* after process death no entry is open and an in-progress draft
  shows only under Drafts.
- **Stale comment found:** `ui/log/PhotoViewerDialog.kt:111-113` says the
  Activity declares no `configChanges`, so a rotation recreates it. That has
  been false since the 2026-09-15 manifest change; the `rememberSaveable` it
  justifies is still right for the other causes.

## Part C: tests by label, and the short-window header

### C1. Tests that select a Journal tab by label (J0 question 5)

Searches: `git grep -n` over `app/src/test app/src/androidTest` for (1) the
seven exact literals, (2) any string literal containing one, and (3) "Records",
"Entries", "Log" and "Journal". Coverage: `git ls-files` lists 229 test files
(228 unit, 1 androidTest), and `git grep -l '.'` and `find` both see 229. Two
helpers were read: `AvailabilityScreenLandscapeB3DestinationsTest.kt:93, :110`
and `AvailabilityScreenSettingsPanelTest.kt:241-254` (`openOfflineMapsSubTab`
with 9 callers, `openRecordedTracksSubTab` with 5). Test paths below are under
`app/src/test/java/com/zynergylabs/forager/app/`; `av/` is `ui/availability/`,
`log/` is `ui/log/`, and each `Test.kt` suffix is dropped.

**Selects by label (`performClick`), 16 call sites:**
- "Logged Finds" 5: `av/AvailabilityScreenAdaptiveLayout:429`,
  `av/AvailabilityScreenBackNavigation:517, :917`, `log/JournalTab:209`,
  `log/LogPanel:169`.
- "Recorded Tracks" 3: `av/AvailabilityScreenBackNavigation:603`,
  `av/AvailabilityScreenSettingsPanel:253` (helper, 5 tests), `:614`.
- "Offline Maps" 2: `av/AvailabilityScreenSettingsPanel:248` (helper, 9 tests),
  `:609`.
- "Album" 3, each the Cartography sub-tab after a "Journal" click:
  `av/AvailabilityScreenBackNavigation:977`, `av/AvailabilityScreenInAppCamera:286, :316`.
- "Drafts" 0 as an exact literal; all 3 selections use "Drafts (1)", because
  the tab text carries a count (`CartographyScreen.kt:289`,
  `FindsGalleryScreen.kt:103`): `av/AvailabilityScreenBackNavigation:873`,
  `log/CartographyScreen:197`, `log/FindsGalleryScreen:108` (the last is the
  finds gallery's own Drafts tab).
- "Cartography" 0; "Waypoint Markers" 0.

**Locates by label to assert or measure (breaks on a rename), 16:**
"Cartography" 6 (`av/AvailabilityScreenBackNavigation:553, :586`;
`av/AvailabilityScreenLandscapeB3Destinations:132, :157`;
`av/AvailabilityScreenShortLandscape:468, :474`); "Waypoint Markers" 3
(`av/AvailabilityScreenBackNavigation:619`;
`av/AvailabilityScreenLandscapeB3Destinations:138, :166`); "Logged Finds" 1
(`…LandscapeB3Destinations:139`); "Offline Maps" 1
(`av/AvailabilityScreenSettingsPanel:440`); "Recorded Tracks" 1 (`…:453`);
"Drafts (1)" 3 (`av/AvailabilityScreenBackNavigation:554, :587`;
`log/CartographyScreen:347`); "Album" 1 (`log/FindsGalleryScreen:156`).

**Per file, selects plus locates, 32 in all:** BackNavigation 10,
SettingsPanel 6, LandscapeB3Destinations 5, InAppCamera 2, ShortLandscape 2,
CartographyScreen 2, FindsGalleryScreen 2, AdaptiveLayout 1, JournalTab 1,
LogPanel 1.

**Labels the plan's list leaves out:** "Records" (top tab), 10 click sites
(`av/AvailabilityScreenAdaptiveLayout:428`,
`av/AvailabilityScreenBackNavigation:516, :602, :618, :916`,
`av/AvailabilityScreenLandscapeB3Destinations:164`,
`av/AvailabilityScreenSettingsPanel:243` (helper),
`av/AvailabilityScreenWaypointFlow:224`, `log/JournalTab:208`,
`log/LogPanel:168`) and one measurement (`…LandscapeB3Destinations:133`).
"Entries" (Cartography sub-tab), 1 click (`av/AvailabilityScreenBackNavigation:879`),
13 display checks and 1 absence check (`log/JournalTab:341`). "Log" (the finds
gallery's first tab), 0.

**Label strings used for other things** (snackbar, content descriptions such
as "New Cartography entry" and "Back to Cartography", the "From Album" button,
assertion messages, comments) were listed by the pulse and are not tab
selections. Two test comments are stale: `av/AvailabilityScreenBackNavigation:965`
and `log/LogEntryDetailScreen:263` call "Album" a bottom-nav label, which
`CompactTab` (`ui/availability/AvailabilityNavigationUi.kt:80-86`) no longer
has.

*Could not determine:* a test that builds a label at runtime would evade the
string-literal search; none was looked for.

### C2. The search header in a short landscape window (J0 question 8)

**Yes, it renders on the Journal tab, inside B3's 640 dp cap, unless an entry
is being edited.** From `ui/availability/AvailabilityCompactScaffold.kt`:
`railBeside` is true on Journal (:503-504); the capped Column (:649-653) holds
`if (!isMapFullscreen() && compactTab() != CompactTab.MAP && !isEditingJournalEntry) { SearchEntryBar(...); SearchNotice(uiState) }`
(:686), with `isEditingJournalEntry` at :381; the Journal content takes the
remaining height (`weight(1f)`, :726).

Height: built from a measurement, not a fixed dp (`AvailabilitySearchUi.kt:199-296`):
4 dp padding, a field of twice the measured height of "Mg" in `labelMedium`
(:213-218), a 4 dp spacer, a divider, 4 dp padding. *Estimate, inferred:* about
45 dp at default font scale, growing with it; `SearchNotice` (:521-545) adds a
strip of roughly 30-35 dp only when an error or a denied location permission is
set. The cap limits width only.

The B3 destinations coder measured, under Robolectric at `w823dp-h384dp-land`
on Records, the bar ending at 85 dp, the Journal tab row at 141 dp and the
Records sub-tab row at 197 dp, leaving about 187 dp
(`2026-09-27-landscape-b3-destinations-completion-report.md:200-204`). No test
asserts these. *Could not determine:* why 85 dp against the 45 dp estimate (a
notice strip set in that test's state, or Robolectric's font metrics); a run
would settle it. The plan's L1 48 dp budget (open question 4) needs recounting
either way, since the header does render.
