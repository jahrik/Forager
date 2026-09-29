# J6 completion report (dispatch 2026-09-28-152)

## J6a: the Journal — STOPPED before building (dispatch item 8)

**Base:** `j6` at `2486950e`, merged with `origin/journal-redesign` (`git pull --no-rebase`, head `aa0dde85`). Model: configured `claude-sonnet-5-5`; not independently confirmed by a tool.

**The stop.** Dispatch item 8: "The 'Mushroom Log' header follows the phone. If the phone Journal has no equivalent header text, **stop** and report what the phone shows, rather than choose words."

**What the phone shows** (read in the working tree at `aa0dde85`):
- `JournalTab.kt:616-624`: the top of the phone Journal is the `JournalSwitch` (labels "Entries" / "Records", `JournalTab.kt:799-800`) and nothing above it. There is no "Mushroom Log" text on the phone Journal.
- The only "Mushroom Log" strings in `main/` are `LogPanel.kt:587` (wide) and `AvailabilitySettingsUi.kt:153` (a settings label, not a Journal header).
- The wide header is not only text: `LogPanel.kt:576-590` is a clickable 48 dp row with a back arrow ("Back to search options") calling `onBackToSearch` (`LogPanel.kt:439`). It is the wide tree's only on-screen route from the Journal panel to the Search panel.

**Options (planner/owner to choose):**
1. Remove the header; the switch is the top row, as on the phone. Back to Search is then only the system Back (ruling 5 step 6). Loses the visible back affordance.
2. Keep the back-arrow row but drop the words "Mushroom Log" (icon only, with its content description). Unruled: it adds a wide-only control.
3. Give the header wording the owner chooses.

**Not built.** No tests written, no code changed, no suite run. Nothing else in J6a or J6b was started, because item 8 is unruled and replaces the header row that the switch and the list-detail branch sit in.

### Decisions I made
- None on design. I stopped.

### Flags outside scope
- The session prompt held two dispatches (J6, and device Part 2 Session 1, 2026-09-28-154). I took J6 and did not start the device one; a session can only hold one coder role, and the device dispatch names a different worktree and only Session 1 may run Gradle.
- Device-only list: not yet reached.

---

## Resumed: J6a, the Journal (after the header ruling)

**Rulings received.** Planner message (2026-09-29, relayed by the session then named `[9b334a]`, now `[4b12e2]`): "the owner has ruled on your item-8 header stop. Resume J6a." The governing file is `prompts/preserved/2026-09-29-25.md` (pushed at `db7fce7d`). The owner, verbatim: "4: Only apply on landscape phone mode. Never on portrait or tablet mode. / The rest I'll take what's recommended." The ruling on the stop:

> Keep the back-arrow row, and label it **"Journal"**, the phone's name for this tab in its bottom bar. It replaces "Mushroom Log" at LogPanel.kt:587. The Entries / Records `JournalSwitch` sits below it, replacing the "Cartography / Records" tab row. The row still returns to the Search panel, as today (onBackToSearch). Nothing else changes: `AvailabilitySettingsUi.kt:153`'s "Mushroom Log" settings label is out of scope.

And for J6b, item 14: "use the phone's **portrait** arrangement, never the landscape L. If the only reusable composable carries the landscape layout inside it, **stop** and report."

**Base.** `j6` at `a607d2d1` (merge of `origin/journal-redesign` `db7fce7d` into `2a88511c`). Model: configured `claude-sonnet-5-5`; not independently confirmed by a tool.

### Pre-registration (pushed before any production code changes)

**Design, and where it lands** (line numbers are at the base):
1. **New `ui/log/JournalDetailSlot.kt`**: a slot holding layers; a composable registers a layer (content, priority) and removes it when it leaves composition. `AvailabilityScreen`'s wide branch (`content = mainScaffold`, `AvailabilityScreen.kt:1957`) draws the slot's top layer over `mainScaffold` in a `JournalDetailPane` (opaque Surface, tagged `journal-detail-pane`, its own system-bar insets), and clears `mainScaffold`'s semantics while it covers it, so nothing beneath is reachable by touch or by TalkBack.
2. **`LogPanel` becomes a "Journal" back row above `JournalTab`** (`LogPanel.kt:437-537` is replaced), so the wide Journal has one implementation with the phone's: `JournalSwitch`, `JournalScreenState`, the report step and "+" tile, finds and counts, `draftFindIds`, tracks, the J4b delete paths, F2/F3, the `rememberUpdatedState` capture fix (`JournalTab.kt:410-414`). `JournalTab` gains defaulted parameters only (`detailSlot`, and column counts), so every compact call is unchanged.
3. **Detail layers:** the open day entry (`CartographyScreen.kt:383-443`, no longer an early return when a slot is given), the open find (`JournalTab.kt` `findsSection`, split into a list and a detail), record details (`RecordsTab.kt:315-336`, as a pane instead of `RecordDetailsSheet`'s modal sheet when a slot is given), and the pickers (part of the find detail). Priority: record details over find over entry.
4. **Back:** one handler at the top of `LogPanel` (registered before everything under it, so every deeper handler outranks it) takes the Journal to the Search panel; `AvailabilityScreen`'s three wide-tree writes of `isDrawerOpen = true` for the Journal (`:1319-1320`, `:1341-1342`, `:1643-1644`) are removed, so the compact drawer's reset is no longer the wide Journal's Back.
5. **Photo Gallery panel removed:** `DrawerPanel.PhotoGallery`, its row, its branch (`AvailabilityScreen.kt:1554-1567`) and `PhotoGalleryScreen`'s only call site; the album gets the long-press Delete through `onRequestDeleteGalleryPhoto`.

**Tests, written first** (`WideJournalTest`, 25 tests, at 824 x 1318 dp; `RecordsChipRowScrollTest`, 3): pushed to `j6-wip` at `775403d1`.
- **Fail at base, for the reason each names:** 22 of `WideJournalTest`'s 25. Read from the JUnit XML after a run whose build log has 0 compile errors.
- **Guards that pass at base** (behaviour that already holds, pinned): the album's three columns, "Back from a day entry's report closes the detail", the drafts list in the left column, and all three chip-row tests.
- **Item 7 is a finding, not a build:** the chip row already scrolls only when its chips overflow (`RecordsFilterChips.kt:75`, `horizontalScroll` on a row that fits has a zero scroll range). Its three tests pass at base and are guards; nothing is built for item 7 beyond keeping them green. Read against the ruling: at the 328 dp content width the five chips need 414 dp (range 94 at a 320 dp window), so the row does scroll there.
- **Corrections made to the tests before this commit,** each found by reading the first run's XML against the test's own claim: nine tests had failed only because the `journal-switch` tag did not exist yet; "Back reaches the Search panel" passed at base because it looked for the text "Mushroom Log", which the base's Journal header also says; "Finds draws two columns" passed at base because three columns also sit side by side; the chip-row guards read 94 for a row that fits because Robolectric's window is 320 dp wide.

**Predictions for the build:** all 22 flip to pass; the 6 guards stay passing; no compile error in any build log I cite.

**Expected changes to existing tests** (the intended behaviour change, each recorded as such, no assertion weakened): the wide header assertions that read the base's header text `"Mushroom Log"` (`AvailabilityScreenMapBubblesTest.kt:608`, `JournalEntriesOnMapScreenTest.kt:734`, `EntrySaveFailureShownTest.kt:600,606`), the `"Cartography"` tab click (`LogPanelTest.kt:289`), and the two Photo Gallery row tests (`AvailabilityScreenAdaptiveLayoutTest.kt:259`, `AvailabilityScreenShortLandscapeTest.kt:199-205`). The Search panel's own "Mushroom Log" row is unchanged and is what the other tests tap.

**Machine.** The disk was at 100% (0 bytes free, other sessions' builds) when I tried to commit. I freed only this worktree's own `app/build` (116 MB) and pointed `app/build` at `/tmp` (tmpfs, ignored by git) so the next builds have room. No other worktree's files were touched.
