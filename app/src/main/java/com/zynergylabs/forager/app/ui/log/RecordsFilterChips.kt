package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.zynergylabs.forager.app.ui.theme.Spacing

/**
 * How many records each Records filter chip counts. [finds] is `null` when the caller has no count
 * to give (see `RecordsTab`'s `findsCount`); [all] is then `null` too, since a total missing one type
 * would be a wrong number shown as a right one.
 */
internal data class RecordsFilterCounts(
    val finds: Int?,
    val tracks: Int,
    val waypoints: Int,
    val offlineMaps: Int,
) {
    val all: Int? get() = finds?.let { it + tracks + waypoints + offlineMaps }

    fun of(filter: RecordsSubTab): Int? = when (filter) {
        RecordsSubTab.ALL -> all
        RecordsSubTab.FINDS -> finds
        RecordsSubTab.RECORDED_TRACKS -> tracks
        RecordsSubTab.WAYPOINTS -> waypoints
        RecordsSubTab.OFFLINE_MAPS -> offlineMaps
    }
}

/**
 * Records' filter chips — journal redesign J1, S3 (plan J4): All · Finds · Tracks · Waypoints ·
 * Offline maps, each with an icon and a count, in one row that scrolls sideways when it overflows.
 * Single choice: exactly one chip is selected, and tapping the selected one keeps it selected (a
 * filter row with nothing selected has no meaning here; All is the "no filter" choice).
 *
 * A single-type chip takes its record type's colours from [RecordTypeStyle] (plan J6): the type's
 * container behind a selected chip, its accent on the icon (always) and on the selected label. All
 * has no record type and keeps [FilterChipDefaults]' own colours.
 */
@Composable
internal fun RecordsFilterChipRow(
    selected: RecordsSubTab,
    counts: RecordsFilterCounts,
    onSelect: (RecordsSubTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(RECORDS_FILTER_CHIP_ROW_TAG)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecordsSubTab.entries.forEach { filter ->
            RecordsFilterChip(
                filter = filter,
                count = counts.of(filter),
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun RecordsFilterChip(filter: RecordsSubTab, count: Int?, selected: Boolean, onClick: () -> Unit) {
    val type = filter.recordType()
    val colors = if (type != null) {
        val typeColors = RecordTypeStyle.colors(type)
        FilterChipDefaults.filterChipColors(
            iconColor = typeColors.accent,
            selectedContainerColor = typeColors.container,
            selectedLabelColor = typeColors.accent,
            selectedLeadingIconColor = typeColors.accent,
        )
    } else {
        FilterChipDefaults.filterChipColors()
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Text(filter.chipLabel(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (count != null) Text(count.toString(), style = MaterialTheme.typography.labelMedium)
            }
        },
        leadingIcon = { Icon(filter.chipIcon(), contentDescription = null) },
        colors = colors,
        modifier = Modifier.testTag(recordsFilterChipTestTag(filter)),
    )
}

/** The record type a single-type chip filters to; `null` for [RecordsSubTab.ALL]. */
internal fun RecordsSubTab.recordType(): RecordType? = when (this) {
    RecordsSubTab.ALL -> null
    RecordsSubTab.FINDS -> RecordType.FINDS
    RecordsSubTab.RECORDED_TRACKS -> RecordType.TRACKS
    RecordsSubTab.WAYPOINTS -> RecordType.WAYPOINTS
    RecordsSubTab.OFFLINE_MAPS -> RecordType.OFFLINE_MAPS
}

private fun RecordsSubTab.chipLabel(): String = when (this) {
    RecordsSubTab.ALL -> "All"
    RecordsSubTab.FINDS -> "Finds"
    RecordsSubTab.RECORDED_TRACKS -> "Tracks"
    RecordsSubTab.WAYPOINTS -> "Waypoints"
    RecordsSubTab.OFFLINE_MAPS -> "Offline maps"
}

/**
 * The chip icons. The plan's mockup draws them as emoji (🍄 〰 📍 🗺); these are the nearest glyphs
 * in the Material icon set the app already depends on (`material-icons-extended`), which has no
 * mushroom: a leaf for finds, a line graph for tracks, a pin for waypoints, a map for offline maps,
 * a list for All.
 */
internal fun RecordsSubTab.chipIcon(): ImageVector = when (this) {
    RecordsSubTab.ALL -> Icons.AutoMirrored.Filled.ViewList
    RecordsSubTab.FINDS -> Icons.Filled.Eco
    RecordsSubTab.RECORDED_TRACKS -> Icons.Filled.Timeline
    RecordsSubTab.WAYPOINTS -> Icons.Filled.Place
    RecordsSubTab.OFFLINE_MAPS -> Icons.Filled.Map
}

/** The chip row itself: it fills the width Records is given, so a layout test can measure that width through it. */
internal const val RECORDS_FILTER_CHIP_ROW_TAG = "records-filter-chip-row"

/** Test tag of each filter chip — stable strings, not enum names, so a rename of [RecordsSubTab] does not move them. */
internal fun recordsFilterChipTestTag(filter: RecordsSubTab): String = when (filter) {
    RecordsSubTab.ALL -> "records-chip-all"
    RecordsSubTab.FINDS -> "records-chip-finds"
    RecordsSubTab.RECORDED_TRACKS -> "records-chip-tracks"
    RecordsSubTab.WAYPOINTS -> "records-chip-waypoints"
    RecordsSubTab.OFFLINE_MAPS -> "records-chip-offline-maps"
}
