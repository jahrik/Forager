package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.ui.theme.Spacing

/**
 * The Entries/Drafts submenus' shared list — Journal Stage 2b. A grid, the same shape
 * [LogGalleryScreen]'s own gallery uses, not the plain scroll [LogEntryListScreen] uses for finds:
 * Cartography's grid columns are exactly the "more of the same thing at once" responsive knob owner
 * decision #3 calls for (one Cartography implementation, [columns] the only thing that changes
 * between window classes).
 *
 * **No "+" tile since journal redesign J2 (T4, plan J7).** It used to be the first grid cell, the
 * largest thing on screen; starting an entry is now [CartographyScreen]'s floating button, which
 * sits over this grid, so [bottomContentPadding] lets the last row scroll clear of it. [emptyMessage]
 * is what an empty list says (the timeline and the drafts list say different things).
 *
 * **Cards since journal redesign J3 (C1, plan J5):** each month's entries sit under a sticky
 * month header, and each entry is a [CartographyEntryCard] (day numeral and weekday, title, species
 * chips, stats by type) or, with nothing to draw large, a [CollapsedEntryRow]; see that file.
 *
 * A card names its date, its tag chips (if any), and kept-item stats — **never whether it has
 * writing**. Per `amendment-2b-optional-writing.md`: a wordless entry with kept items is complete,
 * not incomplete, so no card here carries an "Incomplete"-style badge (the find tiles' own such badge was removed on 2026-09-13) the way [LogGalleryScreen]'s
 * find tiles do; [MushroomLogEntry] is a different entity with a different completeness question,
 * and none of that framing carries over to entries that never claimed to be structured records.
 */
@Composable
internal fun CartographyEntryListScreen(
    entries: List<CartographyEntry>,
    isLoading: Boolean,
    onOpenEntry: (String) -> Unit,
    /** Shown when there is nothing to list and no load error to show instead. */
    emptyMessage: String,
    /** The user's distance unit, for a card's track stat (J3, C1). */
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
    loadErrorMessage: String? = null,
    /** Grid column count — 2 for compact, more for expanded/tablet. See this composable's own doc comment on owner decision #3. */
    columns: Int = 2,
    /** Space below the last row, so a floating button over the grid does not cover it at the end of the list (J2, T4). */
    bottomContentPadding: Dp = Spacing.lg,
    /** The gallery photos the screen already holds, which a card's hero is resolved against (J3, C2; see [entryHeroPhoto]). */
    galleryPhotos: List<GalleryPhoto> = emptyList(),
    /** The already-loaded recorded tracks, which a card's thumbnail is looked up in by id (J3, C3; see [entryThumbnailTracks]). */
    tracks: List<Track> = emptyList(),
) {
    if (isLoading && entries.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (entries.isEmpty() && loadErrorMessage == null) {
        Text(
            emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.fillMaxWidth().padding(Spacing.lg),
        )
        return
    }

    val months = remember(entries) { groupEntriesByMonth(entries) }
    val photosById = remember(galleryPhotos) { galleryPhotos.associateBy { it.photo.id } }
    val tracksById = remember(tracks) { tracks.associateBy { it.id } }
    Column(modifier = modifier.fillMaxSize()) {
        if (entries.isEmpty() && loadErrorMessage != null) {
            Text(
                loadErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, bottom = bottomContentPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            months.forEachIndexed { run, (month, monthEntries) ->
                // J3, C1 (plan J5): a sticky header per month. The run index keeps keys unique where
                // a month recurs (the drafts list's order is by last update, not date).
                stickyHeader(key = "month-$month-$run", contentType = "month") { EntryMonthHeader(month) }
                items(monthEntries, key = { it.id }) { entry ->
                    val open = { onOpenEntry(entry.id) }
                    val hero = entryHeroPhoto(entry, photosById)
                    if (isCollapsedEntry(entry, hasHero = hero != null)) {
                        CollapsedEntryRow(entry = entry, distanceUnit = distanceUnit, onClick = open)
                    } else {
                        CartographyEntryCard(
                            entry = entry,
                            distanceUnit = distanceUnit,
                            onClick = open,
                            hero = hero?.let { photo -> { EntryHeroPhoto(entry.id, photo) } },
                            thumbnail = entryThumbnailTracks(entry, tracksById).takeIf { it.isNotEmpty() }?.let { found -> { EntryTrackThumbnail(entry.id, found) } },
                        )
                    }
                }
            }
        }
    }
}

/** The test tag of an entry card's two-stage swipe row (J4b L2). */
internal fun entrySwipeTag(entryId: String): String = "entries-swipe-$entryId"
