# J6 premise pulse: the wide Journal (read-only, at `14ea159`)

**Date:** 2026-09-28. **Read at:** `origin/journal-redesign` `14ea159`. Nothing was built, run or read from a device.

**Recorded by:** the planner, from a pulse subagent's hand-back. It is condensed, with every citation kept. Paths are under `app/src/main/java/com/zynergylabs/forager/app/`. Abbreviations:
- AS = `ui/availability/AvailabilityScreen.kt`
- ACS = `ui/availability/AvailabilityCompactScaffold.kt`
- AWL = `ui/availability/AvailabilityWideLayoutUi.kt`
- LP = `ui/log/LogPanel.kt`
- JT = `ui/log/JournalTab.kt`
- CS = `ui/log/CartographyScreen.kt`
- RT = `ui/log/RecordsTab.kt`
- CELS = `ui/log/CartographyEntryListScreen.kt`
- EA = `ui/log/EntriesAlbum.kt`
- FGS = `ui/log/FindsGalleryScreen.kt`
- RLL = `ui/log/RecordsLogbookList.kt`
- JSS = `ui/log/JournalScreenState.kt`

**The base moved during the read, a planner error.** Partway through, the planner fast-forwarded the shared checkout `forager-wt/journal-redesign` from `14ea159` to `5874699` to run J8's suite there. The pulse noticed from the reflog and re-checked every citation at `14ea159` through `git show`. Where J8 changes an answer, it cites `823d802` and says so. The rule for next time: a planner does not move a checkout that an agent is reading. It gives the agent its own checkout, or reads through `git show`.

## 1. How the wide tree is chosen

- Width classes: COMPACT is under 600 dp, MEDIUM is 600 to 839 dp, EXPANDED is 840 dp and up (`ui/adaptive/WindowWidthClass.kt:24-25, 39-46`).
- A window is short when its height is under 480 dp (`ui/adaptive/ShortWindow.kt:14, 25`).
- The one branch is `if (windowWidthClass == COMPACT || isShortWindow)` at AS:1690. The compact tree is AS:1690-1767; otherwise the wide `PermanentNavigationDrawer` is AS:1768-1792. The same test is repeated at AS:1105 and AS:1190.
- **The S22 always takes the compact tree.** It is 384 dp wide in portrait and 384 dp tall in landscape (`docs/plans/landscape-phone-design.md:82, 85-86`, from a capture). Reaching the wide tree needs a window at least 600 dp wide and 480 dp tall *(inferred: the S22 would need a density change or a freeform window; unverified)*.
- **Robolectric qualifiers that reach the wide tree:** only `w840dp-h1024dp-mdpi` and `w1280dp-h900dp-mdpi`, both EXPANDED. No test covers MEDIUM with a tall enough window.

## 2. The Journal on the wide tree

**Where it lives.**
- `LogPanel` is one of six `DrawerPanel`s (AS:380-394), switched by a plain-`remember` `drawerPanel` (AS:928), inside a 360 dp `PermanentDrawerSheet` that is always on screen (AS:1773-1790; `PERMANENT_DRAWER_WIDTH` at AWL:88).
- It has one caller, AS:1325.
- **Ways in:** the Search panel's "Mushroom Log" row (AS:1280), "Log a find" (AS:1496-1506), and a bubble's Open in Journal (AS:1204-1218).
- **Ways out:** the back row (LP:409, 540-554; AS:1355), the drawer icon (AS:1442-1454), and a tap on the search summary (AS:1472-1483).

**What it shows.**
- A "Mushroom Log" header (LP:552), then a `SecondaryTabRow` "Cartography | Records" (LP:411-427).
- Under it, the shared `CartographyScreen` (LP:430-469) or `RecordsTab` (LP:471-500).
- M1's find overlay can sit over both (LP:504-535).

**How things open.**
- **An entry:** its report or editor replaces the list in the drawer (CS:318-376).
- **A find:** the grid (LP:363-381) goes straight to the editor (LP:345-360, 374). There is no report step and no "+" tile. The All logbook lists no finds.
- **A record:** the J5c `ModalBottomSheet` (RT:313-328). Where it draws relative to the drawer is unverified.
- **A photo:** the full-screen `PhotoViewerDialog` (EA:179-182). A separate old "Photo Gallery" drawer panel also exists (AS:1281, 1413-1426; `ui/log/PhotoGalleryScreen.kt:142, 162, 214`).

**What sits beside it.** The main scaffold (AS:1437-1541):
- a search bar, summary and notice;
- a **List | Maps | Seasonal** row (AS:1486-1494);
- `CombinedResultsPane`: a 360 dp species list, a divider and the map at `weight(1f)` (AWL:129-153). The map shows a message until a search has run (AWL:228-231).

**List-detail inside the Journal:** none. Every open replaces the list in the same 360 dp column.

## 3. Gap table, compact against wide (J-numbers are the plan's decisions)

| Item | Compact | Wide at `14ea159` | Reused or ported |
|---|---|---|---|
| J1 Entries/Records switch | `JournalSwitch` (JT:583-591, 733-746); Back from Records goes to Entries (JT:418-420) | **Missing**: "Cartography / Records" tab row (LP:411-427); Back from Records goes to Cartography (LP:310-312) | `JournalSwitch` can be reused |
| J2 drafts banner | CS:465-471, 431-438, 397-417 | Present (same `CartographyScreen`) | Reused |
| J3 album | CS:466, 510-523; view state in `JournalScreenState` (JT:633) | Present, but its view state is local (CS:143) and the find badge counts draft finds (`draftFindIds` not passed, CS:151, EA:267). The old Photo Gallery panel is still there | Reused; not wired |
| J4 chips, All, badges | RT:231-245, 249-272, with finds and `onOpenFind` from JT:683-697 | Chips present, but no `finds` passed, so Finds and All have no count and All says finds are elsewhere (RT:123, 235; RLL:124-129). Chip state is local (RT:166) | Reused; not wired |
| Find open | Report then editor (JT:463-497); "+" tile (JT:506-509); 2 columns (FGS:87) | Straight to the editor, no "+", 3 columns (LP:345-380) | **Ported**: LP:316-384 is a parallel copy |
| J5 cards | Track thumbnail (CELS:130-172; JT:637) | **No track thumbnail**; tracks not passed (CS:149) | Reused; not wired |
| J6 colour roles | `RecordTypeStyle.kt:40-51` and its users | Present wherever shared pieces draw | Reused |
| J7 FAB | CS:525-538; EA:170-176 | Present in the 360 dp drawer; no wide touch test | Reused |
| J8 delete on rows | Swipe with Undo (RLL:62-64) | Present; Undo snackbar docks in the drawer (AS:1784-1787); untested wide | Reused |
| J9 columns | 1 portrait, 2 short (JT:632, 765, 768) | 3 in a 360 dp drawer (LP:466, 380, 557) | J6 chooses the value |
| J10 state survives | Hoisted `JournalScreenState` (AS:767; JSS:43-103) plus modes (AS:773-778) | **None reaches LogPanel** (AS:1325-1410); all state is local (LP:235-253, RT:166, CS:143, 189); `drawerPanel` is plain `remember` (AS:928) | Needs threading |
| J4b find tiles | Long-press Edit/Delete (JT:512-515, 690-692) | None | Not wired |
| J4b entry cards | Two-stage swipe (CELS:72-80, 161-169); long-press only when short | None (`onRequestDeleteEntry` not passed, CS:158) | Not wired |
| J4b album photos | Long-press Delete with Undo (EA:102-109, 254-262) | **No delete in the album** (EA:231-237; `LogPanelTest.kt:282`); deleted from the Photo Gallery panel instead | Not wired |
| J5c details | RT:313-328 | Present, tested at w840 (`RecordDetailsSheetTest.kt:494-500`) | Reused |
| M1 bubbles and Open in Journal | AS:1213-1214; JT:357-361, 707-715; JT:551-573 | Present: AS:1216-1217; LP:270-275, 504-535; LP:386-405 | **Ported** copies |
| Leaving fix F1 | AS:1160-1180 | Same (AS:1349) | Shared |
| Leaving fix F2 | Day-entry mode hoisted (AS:773) | **Not applied** (CS:189) | Needs threading |
| Leaving fix F3 | ACS:446-453; AS:777-778, 1209, 1499 | Routes leave first; no `drawerPanel` write calls a leave. After a panel switch a kept find would be out of sight *(inferred)* | Needs threading and decisions |
| J8 stage (wide) | — | Absent at the base. At `823d802`: highlight (AWL:292), chip at the map's top centre (AWL:320-335), `VIEW_ENTRY` into LogPanel (LP:216, 254, 285-288, 483-485), Show/Hide (LP:220) | Built by J8 |

## 4. Shared state

Not shared. `journalScreenState` and the three modes go only to `CompactMainScaffold` (AS:1680-1683) and on to `JournalTab` (ACS:1037, 1042-1044). The `LogPanel` call passes none of them, and JSS:36-37 says so.

The two trees do share the ViewModel state, `pendingJournalDestination` and `pendingJournalFindId` (AS:962-964), the snackbar host (AS:1133) and the leave wrapper (AS:1160).

*(Inferred)* LogPanel's `leaveFindEditingIfNeeded` reads `editing` directly (LP:283-285) and is passed by reference (LP:496). J5 fixed this same stale-capture pattern in JournalTab (JT:370-380).

## 5. Tests on the wide tree (counted, not run)

- `LogPanelTest.kt`: 6 tests, no qualifier; it hosts LogPanel directly.
- `AvailabilityScreenWideWindowLayoutTest` (`AvailabilityScreenAdaptiveLayoutTest.kt:184-185`, w840): 8 tests, 1 of them driving LogPanel (:371). Its name says "medium window", but w840 is EXPANDED.
- `RecordDetailsSheetTest.kt`: 1 wide test (:494-500).
- `AvailabilityScreenMapBubblesWideTest` (w1280): 2 tests.
- `LeavingTheJournalFixesTest.kt`: 3 wide tests.
- Added by J8 at `823d802`: `JournalEntriesOnMapWideTest`, 2 tests at w1280.
- No MEDIUM-and-tall qualifier is used anywhere.

## 6. Facts for the list-detail question

**Pairs, and what the wide tree does with each today:**
- entries and report/editor: replaces, in 360 dp (CS:318-376), with the report's own map inside that width;
- drafts: replaces;
- records and details: a modal sheet;
- finds and a find: the editor replaces, with a report only through M1's overlay;
- album and a photo: a full-screen dialog.

**Widths:**
- The Journal is 360 dp at every wide width (AWL:88).
- Beside it: a 360 dp list, a divider, then the map *(arithmetic, inferred)*. The map is 119 dp at 840 dp (M1's report agrees), 559 dp at 1280 dp, about 118 dp at 839 dp, and nothing at 721 dp or less. At 600 dp the list is squeezed to 240 dp. None of this is run.
- A 3-column cell in the drawer is about 104 dp *(inferred)*.

**Existing side-by-side screens:**
- `CombinedResultsPane`, the species list beside the map. AS:1512-1515 calls it M3's "reveal" pattern.
- The permanent drawer beside the results.
- `OfflineMapsSideBySide`, on the compact tree in short landscape only (`AvailabilityOfflineMapsUi.kt:264-275, 341`).
- None of them is a Journal list beside a Journal detail.

**Library:** there is no Material 3 adaptive dependency. `WindowWidthClass.kt:30-36` records why.

## 7. Premises the code does not match

1. "J8 is being built on `j8`" was stale by the time it was read. J8 had landed (`823d802`), and it touches more than the files named.
2. "The plan says the S22 has no wide window": the plan implied it at `14ea159` (:85-93) and said it only from `823d802`:671. The code agrees.
3. "J6 brings LogPanel up to J1–J9" leaves out J10, which the wide tree also lacks. CLAUDE.md's UX defaults already require state to survive.
4. The plan's J4b "entry cards" long-press: portrait cards swipe, and long-press exists only in a short window (CELS:72-87, 137-172).
5. Stale comments:
   - LP:60-63 says it is a `ModalNavigationDrawer`; it is in the `PermanentNavigationDrawer`.
   - LP:556 says the drawer is "wider"; it is 360 dp.
   - AS:1001-1004 says `isDrawerOpen` stays false on wide, but AS:1217 and AS:1501 set it true. *(Inferred)* Back then resets the panel to Search (AS:973, 1009-1011). If the Journal was reached from its row instead, Back at its top level falls to the double-back exit (AS:1078).
   - `WindowWidthClass.kt:17` says MEDIUM covers phones in landscape; since B1 those are short windows.

## Could not determine

- **Devices:** whether the S22 can reach a 600×480 dp window, and the owner's tablet's model and window class.
- **Drawing:** where the J5c sheet and `PhotoViewerDialog` draw relative to the drawer; whether `PermanentDrawerSheet` takes any inset out of its 360 dp; real widths from 600 to 720 dp.
- **Untested behaviour:** the inferred Back and F3 gaps and the stale capture have no test. No test was run.
