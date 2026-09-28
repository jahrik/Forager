package com.zynergylabs.forager.app.ui.availability

// Split-AvailabilityScreen Stage F: the compact navigation, moved verbatim out of
// AvailabilityScreen.kt. Three blocks, lines of the file as of 3bd0efe: 329-376 (CompactTab,
// CompactTab.icon), 2280-2427 (ForagerBottomNav, ForagerNavigationRail,
// shortLandscapeContentInsets, COMPACT_NAVIGATION_RAIL_TAG, ScreenEdge.horizontalInsetsSide) and
// 4804-4835 (EXIT_NAVIGATION_PROMPT_TAG, EXIT_NAVIGATION_PROMPT_EXIT_TAG,
// EXIT_NAVIGATION_PROMPT_KEEP_TAG, ExitNavigationPrompt). Same package as Stages A to E, so every
// same-package reference resolves unchanged. Pure move: no signature, name or body changed. Seven
// widenings, private -> internal: CompactTab, ForagerBottomNav, ForagerNavigationRail,
// shortLandscapeContentInsets, horizontalInsetsSide and ExitNavigationPrompt, because their callers
// stay in AvailabilityScreen.kt (CompactTab, ForagerBottomNav and ForagerNavigationRail are also
// used by CompactMapTab in AvailabilityCompactMapUi.kt); and CompactTab.icon, widened with its enum
// as the dispatch listed it, though its only callers are the two bars here. No symbol left behind
// is reached from here. Seam F (the wide layout) was released by the owner for this split, as
// recorded in the Understory amendment merged in #130.

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.add
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.map.mapChromeContentColor
import com.zynergylabs.forager.app.ui.map.mapChromeFill
import com.zynergylabs.forager.app.ui.log.ScreenEdge

/**
 * The compact bottom nav's five destinations, in trip order left to right — Pre-trip surfaces
 * (List, Seasonal) then the surface the user is actually in (Maps, a true centre — depends on
 * this being an odd count; a sixth destination would break the centring), then Post-trip and rare
 * (Journal, Tools). [ResultsTab] itself stays a 3-way enum, unchanged, since the medium/expanded
 * window's tab row still switches only between List/Maps/Seasonal, kept in sync with this enum's
 * own `selectedTab` (see [AvailabilityScreen]'s `ForagerBottomNav` call site) whenever the tapped
 * destination is one of those three.
 *
 * **Album is not a destination here any more.** It folds into [JOURNAL] as a third top tab
 * alongside Log/Drafts (see [LogGalleryScreen]'s own doc comment) — Album and log entries share a
 * phase and a frequency (per-find capture at the specimen, Post-trip review at home), so they
 * belong in one destination rather than two separate ones that only existed apart because they
 * were built apart.
 *
 * **[TOOLS] does not behave like the other four.** Tapping it never sets `compactTab` to
 * [TOOLS] — see [ForagerBottomNav]'s own call site — it opens [CompactToolsDrawerContent] as an
 * overlay instead, over whatever tab was already showing, the same drawer the removed MapIconBar
 * search icon used to open (that icon is gone — see `MapIconBar`'s own doc comment — this tab is
 * its replacement entry point, not a new destination alongside it). "Tools" is deliberately a
 * catch-all for things used but not wanted in the immediate way — per-trip and rare items that are
 * not destinations in their own right (trip planner, waypoints, and now Settings
 * too — see that composable's own doc comment). Search itself — basic species/category search,
 * Recent Searches, and Advanced Search — is **not** part of that list any more: it moved out into
 * [SearchDropdown], reached from [ActiveSearchSummary] up top rather than from this drawer, exactly
 * so it would not read as one more Tools entry. That inclusion rule is what the next addition here
 * has to argue against becoming a junk drawer.
 */
internal enum class CompactTab(val label: String) {
    LIST("List"),
    SEASONAL("Seasonal"),
    MAP("Maps"),
    JOURNAL("Journal"),
    TOOLS("Tools"),
}

/** The compact bottom nav's icon per destination — see [ForagerBottomNav]. */
internal fun CompactTab.icon(): ImageVector = when (this) {
    CompactTab.LIST -> Icons.AutoMirrored.Filled.List
    CompactTab.SEASONAL -> Icons.Filled.WbSunny
    CompactTab.MAP -> Icons.Filled.Map
    CompactTab.JOURNAL -> Icons.Filled.MenuBook
    // Same icon Settings itself used before this dispatch folded it into Tools' own drawer — no
    // "tools/build" icon exists in this app's icon set (only the core Material icons module is a
    // dependency, not the extended one a wrench/handyman glyph would need), and Settings is still
    // genuinely part of what this destination opens.
    CompactTab.TOOLS -> Icons.Filled.Settings
}

/**
 * Replaces the compact-only top [SecondaryTabRow] — decision #4 in `docs/plans/map-redesign.md`,
 * back down to [CompactTab]'s **5** destinations as of this dispatch: List, Seasonal, Maps,
 * Journal, Tools. **Corrected twice now**: this comment said "5" before Album was added as its own
 * destination (2026-08-28 correction), then needed correcting again to "6" once Album actually
 * landed, and is "5" again for real now that Album folded into Journal and Settings folded into
 * Tools — re-derived directly against [CompactTab.entries] rather than trusted from either prior
 * count, per this project's own repeated history of exactly this drift.
 *
 * [selectedTab] is [AvailabilityScreen]'s own `compactTab`, with one exception: [CompactTab.TOOLS]
 * never actually becomes the selected `compactTab` (see that entry's own doc comment — tapping it
 * opens a drawer instead), so [isDrawerOpen] stands in for its own highlight specifically. Every
 * other entry highlights the ordinary way.
 *
 * **Two call sites, never both for the same tab at once** (fullscreen-fixes dispatch, Item 1, third
 * design): `compactMainScaffold`'s own `bottomBar` slot renders this, unconditionally opaque, for
 * every tab except Map; the Map tab's own instance lives inside `CompactMapTab`'s own content `Box`
 * instead, `selectedTab` hardcoded to [CompactTab.MAP] there — see that composable's own doc
 * comment for why the nav can't stay in `bottomBar` for that one tab.
 *
 * Its own real rendered height (not a fixed constant) is measured, not guessed — see
 * `compactMainScaffold`'s own `bottomNavHeightPx` doc comment for why a flat Material3 spec value
 * (80dp) undershoots this on a real device by exactly the system navigation-bar inset, invisible
 * under Robolectric (CLAUDE.md's own "Known pitfalls").
 */
@Composable
internal fun ForagerBottomNav(
    selectedTab: CompactTab,
    isDrawerOpen: Boolean,
    onTabSelected: (CompactTab) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Opaque by default — the three docked, `bottomBar`-hosted instances. The Map tab's own
     * overlay instance (inside `CompactMapTab`'s Box, floating over the map) passes 80%, the
     * standing chrome-over-the-map treatment this file's own map-chrome family uses everywhere
     * else — restored on the owner's own call after a screenshot. History, so it isn't flipped a
     * third time without knowing: fullscreen-maps Part 2a first floated it at 80% *while
     * fullscreen*; the fullscreen-fixes dispatch made it slide fully off-screen in fullscreen and
     * went opaque, reasoning it was never both visible and over the map at once; this restores
     * translucency for the non-fullscreen state, where it *is* always over the map.
     */
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    NavigationBar(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    ) {
        CompactTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = if (tab == CompactTab.TOOLS) isDrawerOpen else selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = { Text(tab.label) },
                // Colored entirely from MaterialTheme.colorScheme rather than the fixed Bark/
                // Color.White an earlier revision used — that hardcoding was a real bug, not a
                // style choice: it left this bar the same dark brown regardless of system light/
                // dark theme, while every other surface in the app (and this same bar's own
                // active-tab color, already colorScheme.primary) switched with it.
                // NavigationBarItemDefaults.colors' own defaults already give the unselected/
                // indicator roles sensible theme-following values, so this only overrides
                // selectedIconColor/selectedTextColor to keep the app's own forest green
                // (colorScheme.primary — ForestGreen in light theme, MossGreen in dark, per that
                // theme's own doc comment) as the active-tab accent, matching this file's other
                // hand-picked accents rather than leaving it at M3's default secondary-container
                // tint.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

/**
 * [ForagerBottomNav] turned on its side for a short landscape window — landscape build step B1,
 * `docs/plans/landscape-phone-design.md` P2/P3 and Resolutions R12 and R14. A Material 3
 * [NavigationRail] on the charger-port edge ([portEdge], from `ui/adaptive`'s `portEdgeFor`), next
 * to the system navigation bar, which the capture found on that same edge at both landscape
 * rotations.
 *
 * The same destinations, labels, icons, colours and selection behaviour as [ForagerBottomNav], in
 * the same order top to bottom ([CompactTab.entries]), sharing its `onTabSelected` handler, so a
 * tab switch is one piece of logic in either orientation. [CompactTab.TOOLS] highlights on
 * [isDrawerOpen] exactly as it does there. No header Search action yet: that comes with P8's
 * search sheet in B2 (R14).
 *
 * Two containers, one rail (R12 as revised on the owner's correction). On the Map tab it is an
 * overlay at 80% ([containerColor], the standing opacity for chrome over the map, as
 * [ForagerBottomNav]'s overlay uses), so the map under it never changes size when it hides; on
 * every other tab it is opaque, beside the content. It takes the `navigationBars` inset on its
 * own side only; the top inset comes from the Scaffold padding it sits inside, like the content
 * beside or under it. Robolectric reports zero insets (CLAUDE.md, "Known pitfalls"), so where it
 * sits against the real system bar is a device item (B4).
 */
@Composable
internal fun ForagerNavigationRail(
    selectedTab: CompactTab,
    isDrawerOpen: Boolean,
    onTabSelected: (CompactTab) -> Unit,
    portEdge: ScreenEdge,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    NavigationRail(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        windowInsets = WindowInsets.navigationBars.only(portEdge.horizontalInsetsSide()),
        modifier = modifier.fillMaxHeight().testTag(COMPACT_NAVIGATION_RAIL_TAG),
    ) {
        CompactTab.entries.forEach { tab ->
            NavigationRailItem(
                selected = if (tab == CompactTab.TOOLS) isDrawerOpen else selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = { Text(tab.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

/**
 * The Scaffold content insets for every tab but Map in a short landscape window (landscape B1,
 * P4/P5 and Resolutions R12 and R17 as revised). These tabs have the opaque rail beside them
 * ([ForagerNavigationRail], which takes the `navigationBars` inset on the port side itself), so:
 * at the top, `statusBars`; on the sides, `displayCutout`, so nothing sits in the cut-out band (in
 * practice only the punch-hole side has one); at the bottom nothing for a system bar, since in
 * landscape the 3-button bar is on the port side. The IME still pushes content up, as
 * `safeDrawing` did for these tabs in portrait. The Map tab does not use this: its map is
 * full-bleed and only its controls are padded (compactMainScaffold's `mapControlsPadding`).
 */
@Composable
internal fun shortLandscapeContentInsets(): WindowInsets =
    WindowInsets.statusBars.only(WindowInsetsSides.Top)
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets.ime.only(WindowInsetsSides.Bottom))

/** Tags [ForagerNavigationRail]'s container, so a test can measure the rail's own bounds. */
internal const val COMPACT_NAVIGATION_RAIL_TAG = "compact-navigation-rail"

/** The absolute window side for a left or right [ScreenEdge]; the rail is only ever on one of those. */
internal fun ScreenEdge.horizontalInsetsSide(): WindowInsetsSides =
    if (this == ScreenEdge.Left) WindowInsetsSides.Left else WindowInsetsSides.Right

/** The exit-navigation prompt's buttons, for the back-navigation tests. */
internal const val EXIT_NAVIGATION_PROMPT_TAG = "exit-navigation-prompt"
internal const val EXIT_NAVIGATION_PROMPT_EXIT_TAG = "exit-navigation-prompt-exit"
internal const val EXIT_NAVIGATION_PROMPT_KEEP_TAG = "exit-navigation-prompt-keep"

/**
 * What system back raises while navigating, instead of exiting — see the back chain in
 * [AvailabilityScreen] for why back can only ever raise and dismiss this. Same [AlertDialog]
 * shape as [ThreeWayActionDialog] and the Cartography editor's leave prompt, not a new dialog
 * style. Wording is the owner's: exiting navigation is not stopping the recording, and the text
 * says so, because a user who thinks "Exit" ends the track will keep navigating when they meant
 * to stop, or the reverse. Dismissing by any route — the Keep button, a tap outside, the dialog
 * window's own back — keeps navigating; only the Exit button calls [onExit].
 */
@Composable
internal fun ExitNavigationPrompt(
    onExit: () -> Unit,
    onKeepNavigating: () -> Unit,
    /** Whether a map is drawn on screen beneath the prompt: its container is then at the map chrome's alpha. */
    overMap: Boolean = false,
) {
    val dialogColor = mapChromeFill(AlertDialogDefaults.containerColor, overMap)
    AlertDialog(
        onDismissRequest = onKeepNavigating,
        title = { Text("Exit navigation?") },
        text = { Text("Your track will keep recording.", modifier = Modifier.mapChromeContentColor(LocalContentColor.current)) },
        confirmButton = {
            TextButton(onClick = onExit, modifier = Modifier.testTag(EXIT_NAVIGATION_PROMPT_EXIT_TAG)) { Text("Exit") }
        },
        dismissButton = {
            TextButton(onClick = onKeepNavigating, modifier = Modifier.testTag(EXIT_NAVIGATION_PROMPT_KEEP_TAG)) { Text("Keep navigating") }
        },
        modifier = Modifier.testTag(EXIT_NAVIGATION_PROMPT_TAG).mapChromeContainerColor(dialogColor),
        containerColor = dialogColor,
    )
}
