# 2026-09-27: Landscape B3, pre-build report

B3 is next in the owner's order: "Landscape B2, B3, then Journal"
(`docs/plans/journal-redesign.md:382`, O7). Its scope is P11 (the other
destinations) and P12 (the tools drawer) in `docs/plans/landscape-phone-design.md`,
as corrected by R2, R7 and R9 there. This report was written in a cloud
session at `pre-main` `e136330` plus two docs commits. Nothing was built. It
is read-only.

## 1. Confirmed

- **A scrim tap cannot close the drawer today.** The
  `material3-android-1.5.0-alpha26` sources jar
  (`androidx/compose/material3/NavigationDrawer.kt`, from dl.google.com)
  gates the scrim's dismiss on the flag: `onDismissRequest` runs
  `drawerState.close()` only `if (gesturesEnabled && ...)`. The app passes
  `gesturesEnabled = false` (`AvailabilityScreen.kt:1441`). This settles open
  question 5, which had left it unverified.
- **The comment beside that flag is wrong for this version.**
  `AvailabilityScreen.kt:1439-1440` says "Swipe-to-close still works —
  Material3 enables the drag whenever the drawer is open regardless of this
  flag". In alpha26 the drag is `anchoredDraggable(..., enabled =
  gesturesEnabled)`, so swipe-to-close is off too. With the flag off, the only
  ways out are Back and the close control.
- **The drawer follows layout direction.** The same function reads
  `LocalLayoutDirection` and passes `reverseDirection = isRtl`. No edge or
  anchor parameter exists, which matches B1's report (`2026-09-26-landscape-b1-completion-report.md:123-129`).
  So "opens from the rail side" at `ROTATION_90`, where the rail is on the
  right, has no built-in route. The candidate is flipping the layout
  direction around the drawer and restoring it inside. That is inferred from
  the source and not run.
- **The rail beside the content exists** (B1, `AvailabilityCompactScaffold.kt:502-503`,
  the Row at `:627`).
- **Seasonal is already capped at 640 dp** in a short landscape window,
  because it caps whenever the width class is not COMPACT
  (`AvailabilityResultsUi.kt:245-256`), and an 823 dp window is not COMPACT.
  `READABLE_CONTENT_MAX_WIDTH` is still `private` (`:303`), as R9 expected.

## 2. Could not determine without building

- Which of List, the Journal tabs and Records sub-tabs, entry view and edit,
  and Cartography already cap or centre their content in `w823dp-h384dp-land`.
  They were not read one by one.
- Whether the layout-direction flip opens the drawer on the right without
  also mirroring its contents or the map behind it.

## 3. Decisions B3 needs

1. **How the scrim fix avoids swipe-to-open.** R2 requires a scrim tap to
   close the drawer without re-enabling swipe-to-open over the map. The
   smallest fix is `gesturesEnabled = drawerState.isOpen`. That turns on the
   scrim and swipe-to-close while the drawer is open and keeps swipe-to-open
   off while it is closed. It changes portrait too, which P12 intends.
2. **Opening from the rail side at `ROTATION_90`.** Options: the
   layout-direction flip; a start-edge drawer at both rotations, which departs
   from P12; or a custom sheet.
3. **Branch.** This session is bound to `claude/docs-pr137-loose-ends-jnv47u`,
   a docs branch. B3 on it would mix app code with record commits.
4. **Recordkeeping.** `e136330` removed the kit, the checkers and the dispatch
   store, so B3 has no hook-preserved dispatch. Whether it still gets a
   `RECORD.md` intent and terminal, written by hand, is the owner's call.

## 4. Device-only, for B4

Drawer side and scrim at both rotations; content caps beside the real rail
and cut-out. A cloud session cannot run these.
