# Map chrome colour pulse (read-only, at `a500e000`)

**Date:** 2026-09-29.

**Recorded by:** the planner, from a read-only pulse's hand-back. It is condensed, with the citations kept.

**How it was read:**
- through git at `a500e000`, with a diff to `f645e8f9` showing no colour change;
- Material3 defaults read as bytecode from `material3-android-1.5.0-alpha26`, marked [M3].

**Paths** are under `app/src/main/java/com/zynergylabs/forager/app/ui/`.

## The navigation bar's colour
`ForagerBottomNav` passes `colorScheme.surfaceContainer` (**sC**) (AvailabilityNavigationUi.kt:147-152, the rail at :209-213).
- **Dark #202020, light #F4EFE2** (theme/Color.kt:135,141).
- On the Maps tab it is at 0.8, as one layer (AvailabilityCompactMapUi.kt:904, 941).
- Its tonal elevation is 0 [M3]. It has no shadow and no border.
- No shared token exists for it: four sites read sC directly.

## What differs
**The warm "B/C" family:** dark Bark #3B2E24, light Cream #EDE3D0 (Color.kt:8-9; map/MapChrome.kt:247,250; duplicated as CompassStripBackgroundColor*, AvailabilityMapControlsUi.kt:95,98). It is used by:
- the search bar (AvailabilitySearchUi.kt:275-280);
- **the search panel** (:413-414);
- the compass strip (AvailabilityMapControlsUi.kt:388-390);
- the navigation HUD (NavigationHud.kt:203-204);
- the taxon and journal-entries chips (AvailabilityMapOverlaysUi.kt:341-345; map/JournalEntriesChip.kt:115-128);
- the legend (map/MapLayersSheet.kt:468-473);
- **the icon-cluster container, bar and pill** (AvailabilityMapIconCluster.kt:579-586; MapChrome.kt:313-326, 470; AvailabilityMapControlsUi.kt:209, 270-285);
- the landscape L (AvailabilityMapIconCluster.kt:567-577);
- the handles (MapChrome.kt:642, 744-745);
- **every map bubble** (map/MapBubble.kt:169-209);
- the AddActionTile (AvailabilityMapControlsUi.kt:611-642);
- the entry map's fullscreen bar (log/CartographyEntryReportScreen.kt:536-559).

**Already sC,** the navigation bar's role:
- the species suggestions and Month menu (AvailabilitySearchUi.kt:1204-1214, 1279-1287);
- the journal-entries menu (JournalEntriesChip.kt:68);
- the wide suggestions (AvailabilityScreen.kt:1610);
- the entry map's overflow menu (CartographyEntryReportScreen.kt:410-415).

**Other roles at 0.8:**
- surfaceContainerLow: the **Tools drawer** (AvailabilityScreen.kt:1933-1937), the Layers sheet (MapLayersSheet.kt:238-247) and the record details sheet (log/RecordDetailsSheet.kt:188-194);
- surfaceContainerHigh: the dialogs (AvailabilityMapOverlaysUi.kt:144-151; AvailabilityNavigationUi.kt:279-291; AvailabilityWideLayoutUi.kt:684-701; CartographyEntryReportScreen.kt:674-689);
- surface: the waypoint-name dialog (AvailabilityMapOverlaysUi.kt:216-224) and the centre-pin row (map/CentrePinLocationPicker.kt:302-316);
- errorContainer: the search notice (AvailabilitySearchUi.kt:625-633);
- inverseSurface: the snackbar (AvailabilityCompactScaffold.kt:646-678).

**Opaque over a map:** "Download this area?" (AvailabilityOfflineMapsUi.kt:256-266). That is against the edge-case ruling "Dialogs … 80% over the map".

## Composition, with every alpha kept
- **The cluster** is a container at 0.6 and children at 0.5 (MapChrome.kt:287-308). If both layers take sC, the bar reads sC@0.8, exactly the navigation bar's colour.
- **Menus stacked on the panel** read 0.96 (the owner's "1 A"). They keep the navigation bar's hue.
- **Sheets and the drawer** sit over a Bark@0.32 scrim, so they read warmer than the unscrimmed navigation bar even with an sC container.
- Shadows under translucent fills darken slightly (JournalEntriesChip.kt:100-104).
- Tonal elevation tints only `surface` [M3], so no translucent chrome is tinted.

## Tests that pin colour
- **MapChromeOverMapTest** compares each container with its M3 role (:108-120, :240-253).
- **MapLayersSheetTest** requires sCL@0.8 (:73-76).
- **JournalEntriesChipTest** follows `MapIconStackButtonColor*` (:128-143).
- **LandscapeLClusterPixelsTest pins the Bark/Cream hue** (:91-110).
- MapChromeAlphaTest pins alphas only.
- **Nothing pins the navigation bar's colour.**

## Could not determine
- The rendered colours on a device.
- The baseline inverseSurface value.
- How PermanentDrawerSheet gets its colour.
- Whether the scrims cover the navigation bar.
- The tablet has no navigation bar to match.
- Whether the search notice (the error colour), the snackbar, dialogs and sheets are in the owner's "pop up".
- Whether 0.8 on the search bar and panel on non-map tabs is intended (AvailabilitySearchUi.kt:276,414).
