# Design: the Journal redesign

Planning doc for the Journal destination — Cartography (entries, drafts, album)
and Records (waypoints, offline maps, tracks, finds) — in portrait and in short
(landscape) windows. It is a design record and a task spec for later coder
dispatches, not a replacement for the repo's `CLAUDE.md`, whose standing
principles govern everything below.

Written 2026-09-26 by the planner session in worktree
`bridge-cse_013tPFtYR838ZpDY4QTeK8QA` and committed by a coder dispatch
(its store copy and record intent name it). **O** decisions are the owner's,
said in that planner session; **J** and **L** decisions are the planner's,
made under O2. **Nothing in this document is built.** Every citation was read
at `pre-main` `545258e`, except where marked as read at `main` `76905d4` with
the file unchanged between the two. Re-verify before relying on any of it
(CLAUDE.md, "A planner's picture of the repository is a claim about the past").

## What this is

The owner, with two screenshots of the Journal (Cartography › Entries, and
Records › Waypoint Markers): "The journal menu is mostly finished, but it feels
plain and vanilla. Can you research mobile interface development and design,
and propose a journal interface that is pleasing, easy to navigate, and
fulfills its purposes."

## Decisions — from the project owner

- **O1. Redesign the Journal.** As quoted above.
- **O2. The planner designs it.** "You're the designer so make the
  suggestions."
- **O3. The proposal is approved.** "Let's go with your proposal." That
  covers J1–J10 below as proposed, including the renaming of the Cartography
  tab.
- **O4. Landscape is in scope.** "Also consider the landscape layout." L1–L8
  below were written in answer. The owner has not yet ruled on them
  separately; they are proposed under O2.
- **O5. Hand-off.** "Commit it to the repo for the other planner to find,
  it'll be added into the other session since it's a long ways from
  finishing."

## Evidence — why it reads as plain

Read in code:

- **Two stacked tab rows.** The Journal's top row is a `SecondaryTabRow`
  (Cartography | Records), `JournalTab.kt:398-414`. Under it sit Cartography's
  Entries/Drafts/Album and Records' four sub-tabs (`RecordsSubTab`,
  `JournalTab.kt:243`). With the bottom bar, that is four levels before any
  content, and seven destinations under one bottom-bar icon. Material 3 permits
  secondary tabs under primary tabs, but here both rows look alike, so neither
  reads as primary.
- **Wrapped labels.** Records' labels ("Waypoint Markers", "Offline Maps",
  "Recorded Tracks", "Logged Finds") wrap to two lines on a phone (owner
  screenshot).
- **The entry card is almost empty.** `CartographyEntryTile`
  (`CartographyEntryListScreen.kt:121-161`) shows:
  - `entry.date.toString()`, an ISO date;
  - `entry.text` only if non-blank;
  - the tags;
  - one summed "N kept items" count.

  Two entries on one day look identical (owner screenshot: two
  "2026-09-26" cards).
- **The data exists but isn't shown.** `CartographyEntry`
  (`domain/model/CartographyEntry.kt:50-135`) already carries:
  - kept finds with `ownIdentification` and `hasPhotos`;
  - kept tracks with `distanceMeters` and `durationMillis`;
  - waypoints with names;
  - offline regions with `radiusKm`;
  - attached `photos`.
- **The "+" tile is the largest thing on screen.** It takes the first grid
  cell at full tile size (`CartographyEntryListScreen.kt:91`, `:97-119`,
  aspect ratio `0.85` at `:163`).
- **The palette goes unused.** It has distinct hues (`ui/theme/Color.kt`),
  and Understory already maps them to roles
  (`understory-design-system.md:230-235`):
  - `primary`: dark `#A8CBA0`, lichen;
  - `secondary`: Mushroom;
  - `tertiary`: TrailBlue.

  The Journal uses almost none of it.

Landscape, read in code and plan:

- A window under 480 dp tall is short (`ui/adaptive/ShortWindow.kt`, landed
  with landscape B1). A short window gets the compact tree
  (`landscape-phone-design.md` P1, R1).
- So a phone held sideways shows `JournalTab`, not `LogPanel`, beside an
  opaque navigation rail on the port side (R12 revised,
  `landscape-phone-design.md:600-603`).
- Its content is capped at 640 dp and centred (P11, `:289-298`). The S22 Ultra
  window there is 823 × 384 dp (`:30`).
- `LogPanel` now serves only the wide tree: tablets and foldables.

Not read (known from screenshots and doc comments only):
`CartographyScreen.kt`, `RecordsTab.kt`, `FindsGalleryScreen.kt`,
`PhotoGalleryScreen.kt`, the body of `LogPanel.kt`.

## Decisions — from the planner, portrait (J)

- **J1. One switch at the top.** A single-choice segmented button,
  **Entries | Records**, replaces the `SecondaryTabRow`. Only the on-screen
  label "Cartography" becomes "Entries"; code names such as
  `JournalTopTab.CARTOGRAPHY` and `CartographyEntry` stay.
- **J2. Drafts become a banner** at the top of Entries: "✎ N unfinished
  entries · Continue ›". With one draft, Continue opens it; with more, it
  opens the drafts list. There is no Drafts tab.
- **J3. Album becomes a view of Entries.** A two-button view toggle
  (☰ timeline, ▦ album) in the toolbar row. The album is grouped by day, in 3
  columns, with 3 dp gaps. A 🔗 badge marks a photo attached to an entry,
  replacing the reference-count text.
- **J4. Records gets filter chips.** One row of filter chips, each with an
  icon and a count:
  - All · Finds · Tracks · Waypoints · Offline maps.
  - The row scrolls sideways when it overflows.
  - **All** is the default: one logbook grouped by day, newest first.
  - A single-type chip shows what that type's tab shows today, including the
    Finds editing flow unchanged.
- **J5. Entry cards show the day.**
  - Sticky month headers.
  - A large day numeral with the weekday, instead of the ISO date.
  - The first line of the entry's text as its title.
  - Kept finds' identifications as species chips.
  - A stats row by type: finds count, track distance and duration, waypoint
    count, offline-map count.
  - A hero photo on top when the entry has one attached (subject to J0).
  - An entry with no photo, text or track collapses to one short row.
- **J6. One colour role per record type,** held in one shared style object and
  used by the chips, record badges, card stats and species chips. These are
  theme roles, not raw `Color.kt` values, so night mode and the Understory
  theme carry through:

  | Type | Role |
  |---|---|
  | Finds | `secondary` / `secondaryContainer` |
  | Tracks | `tertiary` / `tertiaryContainer` |
  | Waypoints | `primary` / `primaryContainer` |
  | Offline maps | `onSurfaceVariant` / `surfaceContainerHighest` |

- **J7. New entry is a floating button.** An extended floating action button
  replaces the "+" tile: "✎ New entry" on the timeline, "📷 Add photo" on the
  album.
- **J8. Delete on Records rows.** Swipe-to-dismiss with an Undo Snackbar, if
  J0 finds deletes reversible. Otherwise delete moves into a ⋮ overflow menu
  with a confirm dialog. Either way, the always-visible trash icon goes.
- **J9. Columns.** One full-width column in compact portrait. The existing
  `columns` parameter (`CartographyEntryListScreen.kt:57-58`) stays the only
  width knob.
- **J10. State survives.** The switch side, the Records chip, the Entries
  view mode and scroll position survive a tab change (CLAUDE.md, UX
  defaults) and a rotation (L7). Persistence across app restarts is not in
  scope.

**Components.** J1–J8 use stable Material 3 components:
`SingleChoiceSegmentedButtonRow`, `FilterChip`, `ExtendedFloatingActionButton`,
`SwipeToDismissBox`. Swapping them for Expressive ones (`ButtonGroup`, the FAB
menu) belongs to Understory step 5, which is gated on Gate G
(`understory-design-system.md:703`), not to this plan.

**Left out on purpose:**
- an all-entries map view: new capability, not a restyle, so a later owner
  question;
- a calendar view: speculative;
- the app-wide search header: untouched.

## Decisions — from the planner, short windows (L)

These apply where `isShortWindow()` is true: a phone on its side. The Journal
then sits beside the opaque rail, in at most 640 dp of width and about 360 dp of
height below the status bar. Height is the scarce axis
(`landscape-phone-design.md` P5).

- **L1. One pinned header row, 48 dp tall:** the Entries | Records switch, a
  search icon, and "✎ New" as an icon button.
- **L2. No floating button in short windows.** "✎ New", or "📷" on the album,
  moves into the L1 row. A 56 dp FAB in about 360 dp of height covers too much
  of the list, and sits where Snackbars dock.
- **L3. Drafts become a chip.** "✎ N drafts ›" sits in a second row with the
  view toggle (Entries) or the filter chips (Records). That row hides on
  scroll down and returns on scroll up.
- **L4. Entry cards turn sideways, two columns.** A 72 dp thumbnail on the
  left (photo, or a tinted type-icon panel), text on the right. The content is
  the same as J5; only the arrangement differs. That gives about six cards on
  screen instead of one.
- **L5. Records chips fit on one line** at 640 dp with full labels. Rows are
  one line, with the time right-aligned.
- **L6. The album grid is 5 columns** at 640 dp.
- **L7. State survives rotation.** Both orientations now render `JournalTab`,
  so no second copy of state is involved. What remains is whether `remember`
  state survives the configuration change, which J0 answers. If it does not,
  the J10 state moves to `rememberSaveable` or a hoisted saver.
- **L8. Insets.** No control within the `displayCutout` inset, and the rail
  side keeps the rail's own padding (`landscape-phone-design.md` P4, R17
  revised). Robolectric reports zero insets, so this is device-only.

**Dropped:** a list-detail layout, with the entry opening in a pane beside the
list. It conflicts with P11's 640 dp single-column cap and with R12's "no new
destinations" tree. It may still suit the wide tree (tablets, J6 stage), as a
future owner question.

## Mockups

Portrait, Entries (default):

```
┌──────────────────────────────────────┐
│ ╭──────────────────────────────────╮ │
│ │ 🔍 (app-wide header, unchanged)  │ │
│ ╰──────────────────────────────────╯ │
│ ╭────────────────┬─────────────────╮ │
│ │ ✓  Entries     │     Records     │ │  J1
│ ╰────────────────┴─────────────────╯ │
│ 3 entries · 5 finds         [☰][▦]  │  J3
│ ╭──────────────────────────────────╮ │
│ │ ✎  1 unfinished entry  Continue ›│ │  J2
│ ╰──────────────────────────────────╯ │
│ SEPTEMBER 2026                       │  J5 sticky header
│ ╭──────────────────────────────────╮ │
│ │▓▓▓▓▓▓▓▓ hero photo ▓▓▓▓▓▓▓▓▓▓▓▓▓▓│ │
│ │  26 │ Chanterelles along the ridge│ │
│ │ SAT │ Wet slope under Doug-fir…   │ │
│ │     │ [C. formosus] [B. edulis]   │ │  secondaryContainer
│ │     │ 🍄3  〰 4.2 km · 2h10m  📍1 │ │  J6 roles
│ ╰──────────────────────────────────╯ │
│ ╭──────────────────────────────────╮ │
│ │  14 │ Scouting Molalla  ╭────────────╮
│ │ MON │ 🗺 1 offline map  │✎ New entry │  J7
│ ╰───────────────────────╰────────────╯ │
├──────────────────────────────────────┤
│  ☰     ☀      🗺    (📖)    ⚙       │
└──────────────────────────────────────┘
```

Portrait, Records:

```
┌──────────────────────────────────────┐
│ ╭────────────────┬─────────────────╮ │
│ │    Entries     │  ✓  Records     │ │
│ ╰────────────────┴─────────────────╯ │
│ [All 17][🍄Finds 12][〰Tracks 3][📍Wa→ │  J4
│ Today   Sat, Sep 26 · 5 records      │
│ │[🍄] Cantharellus formosus      › │ │
│ │     6:42 PM · 3 photos           │ │
│ │[〰] Ridge loop                 ⤓ │ │
│ │     6:10 PM · 4.2 km · 2h 10m    │ │
│ │[📍] Start · 6:10 PM ➦ │ 🗑 Delete │  J8 swiped
│ Mon, Sep 14 · 2 records              │
│ │[🗺] Molalla                    ⋮ │ │
│ ┌──────────────────────────────────┐ │
│ │ Waypoint deleted            UNDO │ │
│ └──────────────────────────────────┘ │
└──────────────────────────────────────┘
```

Portrait, Entries › Album view: a 3-column photo grid under day headers, with a
🔗 badge on attached photos and the FAB reading "📷 Add photo".

Short window (phone on its side, rail on the port side, content ≤ 640 dp):

```
┌────┬───────────────────────────────────────────────────────────┐
│ ☰  │  [✓ Entries │ Records]                          🔍   ✎   │ L1
│ ☀  │  ✎ 1 draft ›                                   [☰][▦]    │ L3
│ 🗺 │ ┌───────┐ 26 SAT Chanterelles…  ┌───────┐ 26 SAT Morning…│
│(📖)│ │ photo │ C. formosus · B. edu  │ ~track│ 〰1.8 km · 48m │ L4
│ ⚙  │ └───────┘ 🍄3 〰4.2 km 📍1       └───────┘ 📍1            │
│    │ ┌───────┐ 14 MON Scouting Mol…  ┌───────┐ 09 WED …       │
│    │ │  🗺   │ 🗺 1 offline map       │ photo │ 🍄2            │
└────┴───────────────────────────────────────────────────────────┘
```

## Build order

Stages run in sequence, never in parallel: each appends to
`docs/audits/README.md` (CLAUDE.md, "a serialization point").

- **J0. Pulse (read-only).** Answer:
  1. The structure of `CartographyScreen.kt`, `RecordsTab.kt`,
     `FindsGalleryScreen.kt`, `PhotoGalleryScreen.kt` and `LogPanel.kt`.
  2. Whether each delete (waypoint, track, offline region, find, gallery
     photo) is reversible. This decides J8.
  3. Whether an entry's first attached photo can reach the list without a
     per-card load. This decides J5's hero.
  4. What a static track thumbnail per card would cost.
  5. Every test that selects Journal tabs by label text ("Cartography",
     "Drafts", "Album", "Waypoint Markers", "Offline Maps", "Recorded Tracks",
     "Logged Finds"), with a count.
  6. The callers of `pendingDestination` and `onFindsTabLeft`, and the three
     `BackHandler`s' conditions in `JournalTab.kt`.
  7. Whether the Activity is recreated on rotation (manifest `configChanges`),
     and so whether `remember` state survives it (L7).
  8. Whether the app-wide search header renders on the Journal tab in a short
     window after landscape B1/B2.
- **J1. Shared state and colour roles, then Records.**
  - Hoist the Journal UI state (J10, L7) and add the J6 style object.
  - Then build J4's chips and the All logbook, and the type badges on rows.
  - The Finds editing flow, its incidental-exit rule and `EDIT_NEW_FIND`
    routing must behave as today.
- **J2. The switch, the Drafts banner, the Album toggle and the floating
  button.** J1, J2, J3 and J7.
  - Back from Records steps to Entries.
  - Back from the album view returns to the timeline before leaving the
    Journal.
  - The FAB gets a coordinate-touch test sampling several points on the cards
    around it (CLAUDE.md, "A semantic `performClick` asserts wiring, not
    routing").
- **J3. Entry cards.** J5 and J9, from data `CartographyEntry` already holds.
  The hero photo is included only if J0 found it cheap. Tests assert the
  rendered text: title, species, stats.
- **J4. Delete.** J8, in the form J0 allows.
- **J5. Short windows.** L1–L8. **After landscape B3 lands**, since B3 owns
  the rail layout and the 640 dp cap for the Journal tabs
  (`landscape-phone-design.md` B3). Robolectric at `w823dp-h384dp-land`.
- **J6. The wide tree (tablets).** Bring `LogPanel` up to J1–J9. This is the
  lowest priority, and the list-detail question is raised with the owner
  first.
- **J7. Device check on the S22 Ultra, both orientations and both landscape
  rotations.**
  - Each screen, the Back path, draft Continue.
  - The map's "Log a find" routing.
  - Swipe and Undo.
  - Rotating on each screen and mid-find-edit, with nothing lost.
  - The FAB clear of the gesture bar in portrait.
  - The cut-out and rail sides in landscape, with 3-button and gesture
    navigation.
  - Six or more cards visible in landscape.

## Sequencing against other work

- **Landscape B2/B3.** These are in progress in another session. J1–J4 touch
  `JournalTab.kt`, `RecordsTab.kt` and `CartographyEntryListScreen.kt`. Check
  B3's diff for overlap before dispatching J1, and run J5 only after B3.
- **Understory step 5.** It may later swap J1/J4/J7's components for
  Expressive ones. This plan does not pre-empt that.

## Rules for every build stage

- **Tests.** A test that selects a tab by label is updated in the same commit
  to the new control, and reported with a count. Nothing is disabled, skipped
  or weakened.
- **Revert checks.** Each new test gets a revert check: restore from a saved
  copy, never from git; read the build log for compile errors before the
  results.
- **Push** at every stopping point.
- **Out of scope for every stage:**
  - the Room schema and migrations;
  - the find edit form and the entry report screen, beyond what routing to
    them needs;
  - the map screens.

## Open questions

1. J8's final form, once J0 answers reversibility.
2. The J5 hero photo and the track thumbnail: included or not, once J0
   prices them.
3. L7's mechanism, once J0 answers the rotation question.
4. The app-wide header in a short window (J0 question 8): if it renders, L1's
   48 dp budget needs to be recounted.
5. **Owner, later:** an all-entries map view; list-detail for the wide tree.

## Prior art

Cited as the planner used them in the planning session; not re-fetched by the
dispatch that committed this file.

- Material 3, Tabs guidelines: secondary tabs only below primary tabs, when
  more than one level is needed.
  https://m3.material.io/components/tabs/guidelines
- Nested tabs work only when the hierarchy is clear and shallow (citing
  Nielsen Norman Group). https://www.designmonks.co/blog/nested-tab-ui
- Day One: Timeline, Photos, Map and Calendar as views of one journal,
  switched from the top of the screen.
  http://help.dayoneapp.com/en/articles/840054-journal-views-in-day-one-for-ios
- The landscape rules this plan defers to: `docs/plans/landscape-phone-design.md`
  (P1, P5, P11, R12 revised, R17 revised) and its own prior-art section.

## Addendum, 2026-09-27: owner rulings in the implementing planner session

- Committed from planner session `bridge-cse_01BcqShzosraMo4pUXkqaqRp` by this dispatch. The originating session's commit dispatch (`2026-09-27-04`, in worktree `bridge-cse_013tPFtYR838ZpDY4QTeK8QA`) never ran.
- O6. L1–L8 approved. The owner chose "Approve L1–L8" when asked.
- O7. Order. The owner chose "Landscape B2, B3, then Journal". Journal stages start after landscape B3; the J0 pulse ran in parallel.
- O8. Committed here. The owner chose "A coder here commits it".
- Citation drift found while committing: none; all ten checked at `cff1309` hold. `JournalTab.kt:398-414` holds (the `SecondaryTabRow` opens at 398 and closes at 414); `:243` holds (the `RecordsSubTab`-typed `recordsPendingSubTab` latch); `CartographyEntryListScreen.kt:57-58` holds (`columns: Int = 2` and its doc comment); `:91` holds (the `AddCartographyEntryTile` item); `:97-119` holds (`AddCartographyEntryTile`); `:121-161` holds (`CartographyEntryTile`); `:163` holds (`ENTRY_TILE_ASPECT_RATIO = 0.85f`); `domain/model/CartographyEntry.kt:50-135` holds (`CartographyEntry` through `PhotoAttachment`); `understory-design-system.md:230-235` holds (the `primary` to `tertiaryContainer` role rows, dark primary `#A8CBA0`). None of these four files changed between `545258e` and `cff1309`.

## Addendum, 2026-09-27 (later): two new features, owner rulings in the cloud planner session

Asked by the owner while J4 was running, verbatim: "Add long press options for tiles to edit/delete them. Have an option for cartography entries to show up on the main map so the journal doesn't need to be opened every time. More than one entry may be active at a time on the main map."

Both are new capability, not restyling, so they are stages of their own and do not reopen J1-J4.

### J4b. Long-press menus on tiles (after J4)

- Tiles: **"Album photos, Find tiles, Entry cards"**. Records rows keep swipe only.
- Each opens a menu with Edit and Delete. Delete uses J4's delayed delete and Undo snackbar.
  - Find tiles (Records → Finds, and in All): Edit opens the find's editor. This supersedes J4's "Keep inside the report": delete stays in the report too, and is also on the tile.
  - Entry cards: Edit opens the entry editor. Delete gets the same delayed delete and Undo, which J4 did not cover for Cartography entries.
  - Album photos: Delete gets delayed delete and Undo, with the file delete deferred until the snackbar ends (J0 B1: a photo's file is gone at once today). What Edit means for a photo is **open**: the photo's details and location screen if one exists; the dispatch must find it and stop if there is none.
- Every menu is reachable by TalkBack, as J4's swipe rows are.

### J8. Journal entries on the main map (after J5)

- Content: **"Kept tracks, Kept waypoints, Kept finds, Offline-map outlines"**: an entry shown on the map draws all four kinds of its kept records.
- Toggle: **"Card menu + map chip (Recommended)"**. "Show on map" / "Hide from map" in the entry's long-press menu (J4b) and in its report. On the main map, a small chip shows how many entries are showing; tapping it lists them to hide one or all. More than one entry may show at once.
- Persistence: **"Yes, keep them (Recommended)"**. Stored with the entry in Room, since it belongs to an entry (CLAUDE.md, Room for data that relates). That is a migration: `ForagerDatabase` is at version 15 on this branch's base; the dispatch must check the version against `pre-main` at dispatch time, not this note (CLAUDE.md, verify globally-unique claims), and the new column lands with its reader in the same change.
- Open, for J8's pre-build pass: how entry overlays are told apart from live map markers and from each other (the J6 colour roles and C2 marker palette apply); whether drafts can be shown; what tapping an overlaid record does; where the chip sits in portrait and in short landscape without breaking the map-surface touch rules (CLAUDE.md, Surface intercepts touches); and whether the map's existing marker layers already draw any of these records.

### Order

J4 (running), then J4b, then J5 (short windows, which will need to place the map chip too if J8 lands first, so J5 stays before J8), then J8, then the single Journal PR. J6 (tablets) and J7 (device check) as the plan lists them; J7 gains the long-press menus and the map overlays.

### M1. Tap a map glyph for a bubble (after J5, before J8)

Asked by the owner, verbatim: "Tap glyphs to show a bubble that contains their info. (Finds, photo, track, waypointz etc). Tapping waypoints offers an option to navigate to them."

What the code does today (read by the planner at `journal-redesign` `b65b775`): only sighting dots respond to a tap. `ui/map/SightingsMap.kt:312-325` queries the tapped point against `SIGHTING_LAYER_ID` alone and opens `ObservationBubble` (`ui/availability/AvailabilityMapOverlaysUi.kt:357`); every other glyph layer (`SightingsMap.kt:1142-1159`: planned trips, waypoints, kept tracks, finds, photos, offline-region circles) is drawn and ignores taps. A waypoint row's "Directions" hands the location to an installed navigation app (`launchDirections`, `ui/availability/AvailabilityTripsWaypointsUi.kt:146`); the app also has its own navigation HUD.

Owner rulings:
- Glyphs: **"Finds and photos, Waypoints, Tracks, Planned trips & offline maps"**: every glyph kind gets a bubble with its info on tap, alongside the sighting bubble that exists.
- Navigate: **"In-app HUD, plus Directions (Recommended)"**: a waypoint's bubble offers Navigate, which starts Forager's own navigation HUD to it, and Directions, the existing hand-off to an installed app.
- Placement: **"Journal branch, before J8 (Recommended)"**: stage M1 on `journal-redesign`, after J5, so J8's entry overlays reuse the same bubbles; it ships in the single Journal PR.

Open, for M1's pre-build pass: what each bubble shows per kind (starting from what that record's row already shows); what tapping the bubble itself does (open the find, photo or track; J8 adds entry records); which glyph wins when several overlap at the tap point; how a bubble interacts with the sighting bubble and with long-press (which drops a point today, `SightingsMap.kt:334`); and the map-surface touch rules (CLAUDE.md), with coordinate-touch tests on the map.

### Order, revised

J4 (running), J4b, J5, **M1**, J8, then the single Journal PR.

### Photo details and location (future stage, not scheduled)

J4b found no photo edit screen, so the album's long-press menu is Delete-only. The owner chose **"Later, own stage (Recommended)"**: a photo details and location screen is a stage of its own, to be specified when scheduled (`updatePhotoLocationUseCase` is wired in `MainActivity` and would be where such a screen writes; J4b traced what reaches it).

### Photo editing: sequenced after the camera work (owner, 2026-09-27)

The owner, verbatim, on the photo details and location stage above: "Photo editing is a large project in itself, but not outside the scope of nature photography, so we can add it, but it will come after the central camera improvements land". So photo editing is in scope as its own project, larger than a details-and-location screen, and it starts only after "the central camera improvements" land. No plan in `docs/plans/` is titled for that camera work at the time of writing; which work the phrase names is to be confirmed with the owner when photo editing is scheduled, not assumed.

### Map layering framework (owner, 2026-09-27; to be specified)

The owner, verbatim: "Prepare the map for layering framework also. We are going to improve the forecast methods with layering based on several conditions, similar to how other prediction maps do."

Planner's placement, pending the owner's confirmation: the framework is built before M1 (glyph bubbles) and J8 (entries on the map), since both add map layers and would otherwise be built outside it and reworked. It is a framework for layers, not the forecast method itself; the forecast conditions and how they combine are a later, separate piece of work. Before specifying it, a read-only pulse maps today's layer composition in `ui/map/SightingsMap.kt` and the forecast data the app already has, and a prior-art pass records how established prediction maps structure layer controls. The owner's decisions on scope follow from those.

#### What `slayer8366/forager-forecast` already fixes for the layer framework (read 2026-09-27 at its `main` `876156b`)

The owner, verbatim: "Look at forager-forecast repo for details. It's pure R&D so what's there is very raw so far". Read by the planner; citations are to that repo. Its own status: planning complete, data audits done, no model fit and nothing published yet.

- **What reaches the app (D55, accepted with edits in D56, target confirmed as this app in D57).** Per forager group and ISO week, weather-cell polygons at 0.1 degree, published nightly as GeoJSON split into 1 degree blocks (one file per group per week per block, under a dated path named in a manifest), beside vector PMTiles, with a 250 m raster later. Cell properties by name: `group`, `week`, `chance` (0 to 1), `uncertainty_low`, `uncertainty_high`, `applicable`, `drivers` (a list of `{label, value}`, top first), `weather_through`, `model_version`. The manifest names groups (with GBIF and iNaturalist ids), the current week, the published ecoregions, the attribution text and the layer paths. The reason (D55): MapLibre's offline packs never download a source added at runtime and have no PMTiles path, so for offline use in a saved region the app fetches the blocks touching its areas and **stores them itself**.
- **What the app promises in return (D55).** The number is called "sighting chance" and nothing else, with a unit test searching the app's copy for forbidden terms; shown only beside its reference class; nothing drawn for an unscored cell, with "no forecast here" in the legend; nothing finer than the weather cell until a later decision allows the 250 m raster, labelled "relative habitat" with no percent; the attribution string shown on the map. Tap a cell: chance, uncertainty and data dates (SPEC acceptance; T11).
- **Standing rule for agents in this repo (D58).** Three phrases the forecast project forbids never reach `slayer8366/Forager` in code, strings, docs or commit messages, other than inside a rule or test that names them as forbidden. The planner checked `journal-redesign` on 2026-09-27: zero hits in files and in commit messages. Every dispatch touching forecast copy repeats this check. (D59: this app's own wording rules are the app's to redo after the integration.)
- **Gate.** Nothing derived from non-commercial-licensed records is published or shipped until the owner rules on commercial use (D22, D29). The framework can be built and tested with synthetic cells before that; no real forecast layer ships until it.
- **"Several conditions."** The model is habitat x trigger x observation (START_HERE), and each cell carries its top `drivers`. Whether the app also shows individual conditions as their own layers, as other prediction maps do, is the owner's call after the prior-art pass.

So the framework must give: layers the app feeds from data it stores (re-added after every style reload, basemap change and night-mode change), explicit z-order between the basemap and the markers, per-layer toggles, a legend with a "no forecast here" state and an attribution slot, and tap-to-query per layer, which is what M1's bubbles and J8's overlays need too.

**Correction to M1 above (2026-09-27).** "Long-press (which drops a point today, `SightingsMap.kt:334`)" is wrong: the long-press listener exists, but every production caller passes `{}`, so a long-press does nothing on any map today (`docs/audits/2026-09-27-map-layers-and-forecast-data-pulse.md`). M1 does not need to avoid a long-press action; it only needs to keep long-press free if a later stage wants it.

#### The owner's rulings on the layer framework (2026-09-27)

Asked after the pulse (`docs/audits/2026-09-27-map-layers-and-forecast-data-pulse.md`) and the prior-art pass (`docs/audits/2026-09-27-prediction-map-layers-prior-art.md`), verbatim answers:

1. Colour fields: **"Several, with opacity"**. Like Gaia and CalTopo: any number of colour layers on at once, each with its own opacity slider, and the user can reorder them. (Not Windy's one-at-a-time.)
2. Conditions: **"Both, like Waldschatzfinder (Recommended)"**. Sighting chance plus each condition (rain, soil temperature, soil moisture and the like) as its own layer, and tapping a cell shows the per-condition breakdown. This needs the forecast project to publish condition grids as well as sighting chance, a change to its D55 contract; that is decided in `slayer8366/forager-forecast`, which this session reads but does not write.
3. Layers menu: **"One sheet, two sections (Recommended)"**. The map's existing Layers button opens one sheet: "Map type" (pick one basemap) and "Overlays" (toggles for finds, waypoints, tracks, planned trips, offline maps, journal entries, and later the forecast layers).
4. Stage scope: **Option 1, "Framework + synthetic layer"**, with the owner's addition: "This will also require an update to the seasonal forecast panel to align with the model, otherwise it will be two sources of info competing." So the framework stage builds a generic layer model (order, visibility, per-layer opacity, legend, multi-credit attribution, tap precedence), moves today's markers onto it as toggleable overlays, adds a stored-data interface for downloaded cells, and proves it with a synthetic forecast layer behind a developer flag; no real forecast data. And the Seasonal tab's forecast panel (today's point-based conditions, trip windows and fruiting-lag figures, `ui/availability/AvailabilityResultsUi.kt`) must be brought into line with the model so the two do not compete; its timing is asked separately.

Stage name and place: **L0, the map layer framework**, after J5 and before M1 and J8, which become its first users. Every L0 dispatch repeats the D58 check for the forecast project's forbidden phrases, and no forecast copy lands without it.

Seasonal panel timing, the owner's answer: **"With the first real forecast (Recommended)"**. The Seasonal tab's forecast panel changes in the same stage that turns on real forecast data (after the commercial-use ruling), so the map and the panel switch to the model together; until then both keep today's figures. L0 does not touch the panel.

### Order, revised again

J4b (done), the picker and offline-maps fix stage (running), J5, **L0**, M1, J8, then the single Journal PR. Photo editing after the central camera improvements. Real forecast data, condition layers and the Seasonal panel's alignment wait on the commercial-use ruling and on the forecast project's D55 change.

### Device check timing (owner, 2026-09-27)

The owner, verbatim: "I'll run a device check after we are finished with this journal project". So every device-only item from B3, J1-J4b, the picker fix stage, J5, L0, M1 and J8 goes into one consolidated J7 checklist, run once at the end on the S22 Ultra. The planner assembles that checklist from each stage's completion report before the single Journal PR; items proven only by Robolectric are not claimed as verified until then. First among them: the find picker's pinch-and-pan (the only check that the new user-gesture signal is wired in the live map), and Back from a Records chip returning to All ("We'll try it and see if it works").

### Flake session after the Journal project (owner, 2026-09-27)

The owner, verbatim: "Sharing the root cause is a finding, it may guide us to the actual flake, so I'll run a separate session to run it down once the journal project is finished." So F6 (the intermittent album long-press menu tests, terminal `2026-09-27-64`) and the older `JournalTabTest` photo-pull flake are investigated together in one dedicated session after the Journal PR. Whether they share a root cause is the first question that session answers; either answer is a finding. Its starting data is in terminal `2026-09-27-64` and in `docs/audits/2026-09-27-picker-fixes-completion-report.md` ("Continuation: F6, held by the owner"): 6 failures in 18 class runs, all album long-press tests, the menu not opened in the two failures with detail. Until then every full-suite result is reported with its actual failure count, not as green.

### J5c. Tap a Records row for its details (owner, 2026-09-27)

After keeping today's rows in short windows ("Keep today's rows (Recommended)"), the owner added, verbatim: "Expanding my answer: have them display info upon tapping", and chose **"Bottom sheet of details (Recommended)"**: tapping a waypoint, recorded-track or offline-map row, in portrait and landscape, opens a bottom sheet with the record's full details and its actions. Scheduled as J5c, after J5 and before L0. The M1 ruling for waypoints (Navigate in-app plus Directions) applies to the sheet's waypoint actions too.

### In-app Navigate deferred (owner, 2026-09-27)

J5c's coder stopped on Navigate: starting the app's own navigation HUD for a waypoint from the Records tab needs a path the app does not have. The owner, verbatim: "We can defer the navigation for another time. It's going to need more work anyway", and for M1's waypoint bubble: "Defer for now too. It will go with a review in current waypoint navigation." So J5c's details sheet and M1's waypoint bubble offer **Directions only** (the existing hand-off to an installed navigation app). In-app Navigate, from both places, waits for a later review of the current waypoint navigation, which will specify the route. This supersedes the Navigate half of the M1 ruling "In-app HUD, plus Directions (Recommended)"; Directions stands.

### L0 design rulings (owner, 2026-09-27)

Asked before dispatching L0, verbatim answers:
1. Persistence: **"Across app restarts (Recommended)"**: which overlays are on, their opacity and their order are stored in DataStore (like Night Maps and fullscreen), so the map opens as it was left.
2. Legend: **"Collapsible chip, bottom corner (Recommended)"**: a small chip naming the active colour layer(s) in the bottom corner opposite the attribution; tap to expand the colour scale and "no forecast here"; clear of the icon cluster and the landscape rail.
3. Tap priority: **"Markers, then lines, then colour (Recommended)"**: a marker wins, then a track line or offline-map outline, then a colour cell only if nothing else is under the finger; the topmost layer wins within each group.
4. The synthetic layer's switch: **"Diagnostics screen (Recommended)"**: a toggle in the existing Diagnostics (debug build) screen, debug builds only, never in release.

L0 runs as two sequential stages: **L0a** (the layer model, explicit z-order, visibility and opacity, tap priority, today's markers moved onto it; no new UI) and **L0b** (the Layers sheet, DataStore persistence, the legend chip, the stored-data interface for forecast cells, the synthetic layer).

### Device checks per stage, on the S22 Ultra (owner, 2026-09-28)

Supersedes "Device check timing (owner, 2026-09-27)" above, which stands as the record of what was decided then. The owner, verbatim: "That ruling is now stale. You have direct access to the S22 ultra now, so use that for device checks". The planner session now has the test phone (SM-S908U, serial `R5CT321008R`) attached over adb, so a stage's device-only items are checked on it when the stage closes, rather than collected for one J7 run at the end. First to run: L0a's device-only list (`docs/audits/2026-09-27-map-layers-l0a-completion-report.md`, "Device-only"). The device-only items already collected from B3, J1-J4b, the picker fix stage, J5 and J5c are not yet checked; when they run is to be confirmed with the owner, not assumed from this ruling.

### L0b rulings (owner, 2026-09-28)

A read-only pulse at `f62eb3e` found five premises in "L0 design rulings" that the code does not match: the Layers button opens `MapModePicker`, a basemap-only popover, not a sheet (`ui/map/MapChrome.kt:126-193`); the Maps tab draws only sightings, planned trips, waypoints and the live-recording breadcrumb, while finds, photos, kept tracks and offline circles are drawn only on a Cartography entry map (`ui/availability/AvailabilityCompactMapUi.kt:598-606`, `ui/log/CartographyEntryReportScreen.kt:373-376`); the bottom corner opposite the attribution caption already holds MapLibre's own attribution button (`ui/map/SightingsMap.kt:449`) and, in the wide layout, the add button; the Diagnostics screen has no toggles and exists only in the debug source set; and L0b adds one colour field, so reordering could not be exercised. Asked, verbatim answers:

1. Overlays: **"Also show all finds etc."**: L0b also draws every find, photo, kept track and offline region on the Maps tab, so each overlay toggle acts on something. This widens L0b beyond the framework into a new Maps-tab feature.
2. First run: **"On by default"**: the new Maps-tab overlays are visible the first time the map opens after the update; the user switches off what they do not want, and the choice persists.
3. Drafts: **"Saved entries only (Recommended)"**: finds, photos and tracks from saved entries only, nothing from a draft until it is saved; offline regions are all drawn.
4. Entry map: **"Same sheet (Recommended)"**: the entry map's Layers button opens the same sheet, listing what that map draws, with one set of stored choices shared by both maps.
5. Legend placement: **"Bottom-right, above the 'i' (Recommended)"**: the ruled corner stands; the chip stacks above MapLibre's attribution button, and above the add button in the wide layout.
6. Synthetic switch: **"Debug-only source (Recommended)"**: the synthetic generator and its switch live only in the debug source set; the release build gets a forecast source that reports "no forecast data" explicitly, so no synthetic code ships in release.
7. Colour fields: **"Two synthetic layers (Recommended)"**: the debug switch provides two synthetic colour layers, so reordering, stacked opacity and a two-entry legend can be checked on the S22.
8. Stage size: **"One stage"**: L0b stays one stage with one device check at the end, not split.

Not asked, and so unchanged: the basemap choice stays session-only (`ui/availability/AvailabilityScreen.kt:828`), since the persistence ruling names overlays, their opacity and their order; and a reorder takes effect at the next style load, as accepted in terminal `2026-09-27-72` (Deviations 4). Open for J8: J8 draws the kept records of selected entries, and L0b now draws all saved entries' records by kind, so how the two relate on the Maps tab is J8's pre-build question. Open for M1: tapping a forecast cell (chance, uncertainty, data dates, from forager-forecast's acceptance) has no stage yet; M1's bubbles are the natural home.

**Ruling 3 clarified (owner, 2026-09-28).** A find is its own record with its own draft state (`MushroomLogEntry.isDraft`), and a Journal day entry keeps or withholds references to finds, tracks, waypoints and regions (`domain/model/CartographyEntry.kt`), so "saved entries only" had two readings. Asked, the owner chose **"Every saved record (Recommended)"**: the Maps tab draws every saved (non-draft) find, every track in Records, every album photo with a location and every offline region, whether or not a Journal entry keeps it. A find withheld from an entry still shows on the Maps tab; per-entry display is J8's. This does not touch the rule that an entry's own map geometry comes only from `GetCartographyEntryMapDataUseCase`: the Maps tab draws records, not entries.

### L0b forecast-facing rulings, after reading forager-forecast (owner, 2026-09-28)

The owner, verbatim: "Look at forager-forecast repo for details on what the layering is for." Read at forager-forecast `876156b` (`docs/audits/2026-09-28-forager-forecast-layering-read.md`): the layers exist to show a calibrated weekly **sighting chance** per forager group per 0.1 degree weather cell, only where the model beats a seasonal calendar, blank where it cannot score, beside its reference class, with chance, uncertainty, top drivers and data dates on tap; condition layers have no data source in the D55 contract yet, and R8 gives a percent only to the chance layer. Asked, verbatim answers:

1. First synthetic layer: **"Real format, 'test data' name (Recommended)"**: drawn and labelled exactly as the real layer will be (percent, reference-class sentence, "no forecast here", week and data dates), but named as test data so the fixed term "sighting chance" is never attached to fake numbers. Debug builds only.
2. Second synthetic layer: **"A second group (Recommended)"**: a second group's forecast in the same format, which is the published data's real shape (one file per group per week), instead of a generic condition. Chanterelles and chicken of the woods, the forecast project's first two groups (D9).
3. Cell tap readout: **"In M1, with the bubbles (Recommended)"**: M1 builds the forecast-cell readout (chance, uncertainty, top drivers, data dates) as one more bubble kind; L0b builds no tap UI.
4. Reference class: **"Use that wording (Recommended)"**: the expanded legend carries, verbatim, "Chance this group is reported in each 11 km cell this week, where anyone is reporting fungi. Compare areas, not spots. A high chance is not a find." (`slayer8366/Forager-app` `0172c33`, `presentation/src/main/kotlin/com/zynergy/forager/presentation/SightingChance.kt:33-35`), until the forecast project fixes its own wording.

Planner's additions from the same read, stated in the L0b release message rather than asked: the cell store is keyed by group, week and 1 degree block; synthetic cells are centred on multiples of 0.1 degree (edges at x.x5, as ERA5-Land's grid gives) and belong to the block holding their centre; some synthetic cells carry `applicable` false and some are omitted, both drawing nothing; the lowest ramp colour must read as clearly different from an empty cell. Open, and not this repository's to settle: condition layers (owner ruling 2 on the layer framework) need a new decision in forager-forecast changing D55; group and week selection has no ruling in either repository.

### Commercial use: the owner's ruling (2026-09-28)

Asked what D29 in forager-forecast is about (its licence gate: nothing derived from CC BY-NC records is published or shipped until the owner rules on commercial use; forager-forecast `docs/planning/DECISIONS.md:39` at `876156b`, applied record by record by D48), the owner said, verbatim: "I'll be selling the app, but that particular feature will be free. What license do you recommend?" The planner's answer, not legal advice: a free feature inside a paid app most likely still counts as commercial under CC BY-NC 4.0 ("not primarily intended for or directed towards commercial advantage or monetary compensation"), so the options were a model built only from CC0 and CC BY records, or a separate non-commercial product. The owner, verbatim: **"Commercial-safe model. I'll get an open-meteo subscription for their service"**.

What this means here:
- The forecast Forager ships is trained and scored only on CC0 and CC BY records: D29's secondary, commercial-safe analysis becomes the shipped model, and the all-licence analysis stays research. The ruling belongs to forager-forecast (answering D29 and D22); the handoff `prompts/preserved/2026-09-28-08.md` carries it there, since this repository's sessions only read that one.
- Attribution the app must carry once real data ships: the GBIF download citations with their DOIs (CC BY), the Copernicus text of forager-forecast D53, and CEC ecoregions (CC BY) if drawn. L0b's multi-credit attribution list is where they go.
- **Open-Meteo:** its free API is non-commercial (forager-forecast `docs/planning/DATA_REGISTER.md:9`, quoting "You may only use the free API services for non-commercial purposes"). The app calls the free hosts today: `api.open-meteo.com` (`data/remote/OpenMeteoClient.kt:14`) and `archive-api.open-meteo.com` (`data/remote/OpenMeteoArchiveClient.kt:18`). With the owner's subscription, the app must move to the subscriber endpoints and key before it is sold. **Not scheduled; needed before release.** Its pre-build pass decides, from Open-Meteo's own documentation at the time (unverified here: the subscriber hosts and how the key is passed), how the key is kept out of the repository and whether it is called from the app or through a proxy the owner runs, since a key inside an APK can be extracted.
- **Open, unverified:** iNaturalist's API terms for a commercial app, and the licences of any observation photos the app displays. To check before release.

### Leaving the Journal: the owner's rulings (2026-09-28)

From `docs/audits/2026-09-28-leaving-the-journal-investigation.md` (13 characterisation tests on branch `leave-journal-investigation`, `a89b240`). Asked, verbatim answers:

1. The Discard data loss (viewing a committed find, then leaving the Journal, shows "Saved to Drafts" whose Discard deletes the committed find, no Undo): **"Snackbar only for real drafts (Recommended)"**: only viewing shows no snackbar; the snackbar appears only when a draft was really saved, and its Discard can only delete that draft, never a committed find.
2. A day entry left in its editor: **"Return to the editor (Recommended)"**: returning to the Journal reopens it in its editor with the unsaved changes; the report view never draws unsaved edits as if saved; leaving the editor by Back with unsaved changes asks Save or Discard.
3. Finds across a tab change: **"Keep finds open too (Recommended)"**: a find open in view or edit is still open on return, as day entries now are; the Maps search bar stays visible (the gate at `AvailabilityCompactScaffold.kt:401` must not hide it for a find open on another tab).
4. Order: **"M1 first"**: M1 is built next; these three fixes follow, then the two landscape bugs from backlog Part B (the Offline Maps picker unusable in landscape; the entry report's layout in landscape).

### M1 rulings (owner and planner, 2026-09-28)

After the M1 premise pulse (`docs/audits/2026-09-28-m1-premise-pulse.md`), the owner's verbatim answers:

1. Tap result: **"Bubble only (Recommended)"**: a glyph tap opens its bubble and nothing else; `onTap` no longer fires after a feature tap, as sighting taps already behave (settles terminal `2026-09-27-72` Deviations 3).
2. Bubble tap: **"Open in place (Recommended)"**: waypoints, tracks and offline maps open the J5c details sheet on the Maps tab; a photo opens the photo viewer in place; a find's bubble has "Open in Journal", which switches to the Journal with that find open.
3. Entry map: **"Yes, same bubbles (Recommended)"**.
4. Non-records: **"Not tappable (Recommended)"**: the search-centre reticle and the live recording trail stop taking taps.

Planner's rulings, not asked, open to change:
- one generic bubble shell, reusing `AnchoredAtScreenPoint`, with one bubble on screen at a time (a single tapped-thing state that includes sightings);
- the tail's tip always lands on the tapped feature: when the clamp moves the bubble, the tail moves with the anchor, not with the bubble;
- the anchor is the tap point, re-projected on camera idle for point features;
- forecast cells become tappable (the forecast acceptance "a tapped cell shows the same numbers as the scoring table") in a group that loses to any marker or line within the box, so the owner's "point, then box" rule stands;
- the cell bubble looks the cell up by re-querying the store by group, week and block, not by parsing the id;
- a waypoint bubble offers Directions only (In-app Navigate deferred);
- a planned trip's bubble shows what its Trip Planner row shows, with Directions, and opens nothing further (there is no trip target);
- bubble content per kind starts from what that record's row or sheet already shows.

### Track widths by zoom and the entry map's opening frame (owner, 2026-09-28)

The owner, verbatim, over Maps-tab and entry-map screenshots: "At some point in zooming out, the tracks get muddied up from the thickness + distance. Can we have the track lines thin out as we zoom out?" and "When opening an entry, the user expects to see their tracks on the map, maybe the last tracks they recorded. But instead they're greeted with a blank map as shown. Can the entry map open to their last recorded track location, zoomed in to where you see in the next photo?"

Asked, verbatim answers:
1. Framing: **"Fit all kept records (Recommended)"**: the entry map opens framed to everything the entry keeps (tracks, finds, photos, waypoints), as close as fits them; offline-region circles are left out of the framing unless the entry keeps nothing else.
2. Timing: **"Right after M1 (Recommended)"**: both are built after M1 lands and before the Leaving-the-Journal fixes, since they touch the same map files M1 is changing.

Revised order: M1 (running); then track widths by zoom and the entry map's opening frame; then the three Leaving-the-Journal fixes; then the two landscape bugs. The widths' zoom stops are the planner's to propose in the dispatch and the owner's to judge on the phone.

### Search bar copy: "Search a location" (owner, 2026-09-28)

The owner, verbatim: "the \"September · no location set\" in the search bar is accurate, but it reads awkward, like an error (especially after the user gives their permission to set their location on the map). Change it to \"September · Search a location\"", and the principle: "while bare accuracy is an easy thing to settle on, since it fulfills the honesty part, we need to go one step beyond that and give the user a spark or motivation to action based on that honesty. Like, sure that data exists, but what can they do about it? How can it be useful? In this case, they can search a location to satisfy the \"no location set\"."

The string is at `ui/availability/AvailabilitySearchUi.kt:497` (the `?: "no location set"` fallback); no test pins it (`git grep` over `app/src/test`, 2026-09-28). Built with the track-width and entry-framing stage right after M1. The principle applies to every empty or missing-state string reviewed from here on, within the forecast project's fixed terms.

### J8 rulings (owner, 2026-09-28)

After the J8 premise pulse (`docs/audits/2026-09-28-j8-premise-pulse.md`), which found that the Maps tab already draws every saved record (L0b), that portrait entry cards swipe rather than long-press, and that the map palette has no spare role, the owner's verbatim answers:

1. Standing out: **"Highlight in place (Recommended)"**: a shown entry's tracks, finds, photos and waypoints are highlighted in place with a halo or outline in one shared "journal entry" colour; everything else is unchanged.
2. Toggle: **"Entry report menu (Recommended)", "Maps-tab chip (Recommended)", "Layers sheet switch"**: "Show on map" / "Hide from map" in the entry report's menu; a Maps-tab chip listing the shown entries, to hide one or all; and a "Journal entries" switch in the Layers sheet's Overlays that turns all shown entries' highlights on or off together. Not the card swipe.
3. Bubble: **"Name entry + Open entry (Recommended)"**: a highlighted record's bubble names the entry or entries it belongs to and offers "Open entry".
4. Drafts: **"Saved entries only (Recommended)"**.
5. Chip: **"Top, by the species chip (Recommended)"**: in the row with the taxon chip, under the compass strip or search bar.
6. Geometry: **"Live records (Recommended)"**: the highlight follows the live waypoint and region positions; a kept record since deleted is simply not highlighted.

Earlier J8 rulings stand: content "Kept tracks, Kept waypoints, Kept finds, Offline-map outlines"; more than one entry may show at once; "Yes, keep them" in Room. Planner's rulings: the migration rebuilds `cartography_entries` in the pattern of 12 to 13 and 14 to 15 (the legacy fixtures declare the entity directly, `Migrations.kt:899-906`); the highlight colour is one new palette role, day and night, proposed by the planner and measured in `MapPaletteTest`, for the owner to judge on the phone.

### Map chrome at 80%, nothing fully obstructs the map (owner, 2026-09-28)

The owner first asked about one sheet, verbatim: "Can you set this card at 80% opacity while over the map? In the places that aren't covering a map, they can stay solid. Same for tracks please". That came with screenshots of the waypoint and track details sheets opened from a map bubble. The owner then gave rulings: **"After J8 (Recommended)"** for the timing and **"Yes, all three (Recommended)"** for including the offline-region sheet. The planner queued it as intent `2026-09-28-56`.

The owner then widened it to a principle, verbatim: "My idea is that nothing should fully obstruct the map view. All map chrome gets 80% opacity as a result."

What the planner takes this to mean, pending the owner's answers on edge cases:
- Every surface drawn over a map is at `MAP_CHROME_OVER_MAP_ALPHA` (0.8, `ui/map/MapChrome.kt:239`). That covers bars, strips, chips, clusters, rails, legends, bubbles, sheets, drawers and menus.
- The same component shown where it covers no map stays solid, as in the owner's first message.
- The icon cluster (`MapChrome.kt:233-236`) and the Layers sheet (`MapLayersSheet.kt:221`) already follow it.

Before the dispatch widens, a read-only pulse inventories every surface drawn over a map (`docs/audits/2026-09-28-map-chrome-inventory-pulse.md` when filed). Edge cases the pulse surfaces, such as dialogs, text fields and drawers, go to the owner before anything is built. Dispatch `2026-09-28-56` stays queued behind J8, and it will be widened by a continuation, not rewritten.

**Edge-case rulings (owner, 2026-09-28).** These were asked after the map-chrome inventory pulse (`docs/audits/2026-09-28-map-chrome-inventory-pulse.md`, read at `76a67f3`). Verbatim answers:
1. Dialogs, pop-up menus and the snackbar over a map: **"80% over the map"**. This was chosen against the planner's recommendation to keep them solid.
2. The Tools drawer: **"80% over Maps (Recommended)"**. It is solid over the other tabs.
3. The accent buttons (the + disc, the record disc, the wide Add button): **"Keep them solid (Recommended)"**.
4. The attribution caption at 0.55: **"Leave it at 55% (Recommended)"**.

Planner's readings, stated to the owner, stand unless the owner overrules them:
- Full-screen destinations the user opens stay opaque: the photo viewer, the find-over-view page and the camera. They are destinations, not chrome.
- Scrims are unchanged.
- The cluster's 0.6 container is unchanged, since it composites to 0.8 by design (`MapChromeAlphaTest`).
- The five `0.8f` literals move onto `MAP_CHROME_OVER_MAP_ALPHA`, with no visible change.

Dispatch `2026-09-28-56` is widened by continuation `2026-09-28-58`, and still waits for J8.

**Accent buttons, superseding edge-case ruling 3 (owner, 2026-09-28).** Ruling 3 above ("Keep them solid (Recommended)") stands as the record of what was decided then. The owner then reversed it, verbatim: "Have the colored buttons be at 80% opacity also". That covers the + disc, the record disc while recording, and the wide Add button.

The + disc sits on the icon bar and the record disc on the control pill. Both are already 0.8 composites, so a disc at 0.8 on top would stack to about 0.96. Asked how the discs should reach 80%, the owner answered "[No preference]". The planner ruled **true 80% overall**: the map shows through each disc as much as through the rest of the chrome. The fill beneath is not stacked under the disc, which follows CLAUDE.md's rule that layered fills composite to 0.8 rather than each carrying it. The wide Add button sits straight on the map, so its own fill at 0.8 is already 0.8 overall. Carried by continuation `2026-09-28-59` of `-56`.

**Correction: the "colored buttons" are the bottom tab bar (owner, 2026-09-28).** This supersedes the "Accent buttons" paragraph above, which stands as the record of the planner's misreading.
- The planner's edge-case question 3 named the + disc, the record disc and the wide Add button, and the owner's "Have the colored buttons be at 80% opacity also" was recorded against those.
- The owner then clarified, verbatim: "The bottom colored buttons", "Leave the map icon bar alone", and, with a screenshot circling the bottom tab bar (List, Seasonal, Maps, Journal, Tools): "These buttons at the bottom must be 80% opacity".

So:
- The accent discs and the wide Add button **stay solid**. Edge-case ruling 3 stands again, and the icon bar and control pill are untouched.
- **The bottom tab bar's buttons go to 80% over the map.**
  - The bar's own container is already `surfaceContainer` at a literal 0.8 (`AvailabilityCompactMapUi.kt:1210`).
  - The selected tab's highlight (the brown pill behind Maps) is Material3's default indicator colour, solid. Only the icon and label colours are overridden (`AvailabilityNavigationUi.kt:167-170`). The highlight goes to 80% over the map.
  - Icons and labels stay opaque, as UX defaults require.
- The planner applies the same to the navigation rail, which stands in for the bar in short landscape. That is the planner's reading, stated to the owner.
- Whether the bar's 0.8 container actually shows the map through on the S22 is a device check. The screenshot cannot settle it.

Carried by continuation `2026-09-28-60`, which supersedes `-59`.
