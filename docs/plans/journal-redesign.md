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

### Photo editing: sequenced after the camera work (owner, 2026-09-27)

The owner, verbatim, on the photo details and location stage above: "Photo editing is a large project in itself, but not outside the scope of nature photography, so we can add it, but it will come after the central camera improvements land". So photo editing is in scope as its own project, larger than a details-and-location screen, and it starts only after "the central camera improvements" land. No plan in `docs/plans/` is titled for that camera work at the time of writing; which work the phrase names is to be confirmed with the owner when photo editing is scheduled, not assumed.

### Map layering framework (owner, 2026-09-27; to be specified)

The owner, verbatim: "Prepare the map for layering framework also. We are going to improve the forecast methods with layering based on several conditions, similar to how other prediction maps do."

Planner's placement, pending the owner's confirmation: the framework is built before M1 (glyph bubbles) and J8 (entries on the map), since both add map layers and would otherwise be built outside it and reworked. It is a framework for layers, not the forecast method itself; the forecast conditions and how they combine are a later, separate piece of work. Before specifying it, a read-only pulse maps today's layer composition in `ui/map/SightingsMap.kt` and the forecast data the app already has, and a prior-art pass records how established prediction maps structure layer controls. The owner's decisions on scope follow from those.
