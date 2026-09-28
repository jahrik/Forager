package com.zynergylabs.forager.app.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.JournalEntryOnMap
import com.zynergylabs.forager.app.ui.theme.Spacing

/** The Maps-tab chip (J8-3). */
internal const val JOURNAL_ENTRIES_CHIP_TAG = "journal-entries-chip"

/** The chip's list of shown entries. */
internal const val JOURNAL_ENTRIES_LIST_TAG = "journal-entries-list"

/** One shown entry's row in the chip's list, whose tap hides that entry. */
internal fun journalEntriesListRowTag(entryId: String) = "journal-entries-list-row-$entryId"

/** The chip list's "Hide all". */
internal const val JOURNAL_ENTRIES_HIDE_ALL_TAG = "journal-entries-hide-all"

/** The chip's container colour, read by tests from the composed chip's semantics. */
internal val JournalEntriesChipContainerColor = SemanticsPropertyKey<Color>("JournalEntriesChipContainerColor")
private var SemanticsPropertyReceiver.journalEntriesChipContainerColor by JournalEntriesChipContainerColor

/** The chip's content colour, as its text is drawn in. */
internal val JournalEntriesChipContentColor = SemanticsPropertyKey<Color>("JournalEntriesChipContentColor")
private var SemanticsPropertyReceiver.journalEntriesChipContentColor by JournalEntriesChipContentColor

/** A J8 menu's container colour: the chip's list, and a bubble's list of keeping entries. */
internal val JournalMenuContainerColor = SemanticsPropertyKey<Color>("JournalMenuContainerColor")
private var SemanticsPropertyReceiver.journalMenuContainerColor by JournalMenuContainerColor

/** A J8 menu's content colour, as its rows' text is drawn in. */
internal val JournalMenuContentColor = SemanticsPropertyKey<Color>("JournalMenuContentColor")
private var SemanticsPropertyReceiver.journalMenuContentColor by JournalMenuContentColor

/** J8 tests-first stub: Material3's default menu container, opaque. */
@Composable
internal fun journalMenuContainerColor(): Color = MenuDefaults.containerColor

/** J8 tests-first stub: not pinned, so the ambient content colour. */
@Composable
internal fun journalMenuContentColor(): Color = LocalContentColor.current

/** Marks a J8 menu's content with its colours, for tests. */
internal fun Modifier.journalMenuColours(container: Color, content: Color): Modifier =
    semantics {
        journalMenuContainerColor = container
        journalMenuContentColor = content
    }

/**
 * J8 tests-first stub: the chip with Material3's default surface colours, not yet placed on any map.
 * Its text is [journalEntriesChipLabel]; a tap lists [entries] by date, each row hiding its entry, with
 * "Hide all" last.
 */
@Composable
internal fun JournalEntriesMapChip(
    entries: List<JournalEntryOnMap>,
    onHide: (String) -> Unit,
    onHideAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val container = MaterialTheme.colorScheme.surface
    val content = MaterialTheme.colorScheme.onSurface
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .testTag(JOURNAL_ENTRIES_CHIP_TAG)
                .semantics {
                    journalEntriesChipContainerColor = container
                    journalEntriesChipContentColor = content
                },
            shape = RoundedCornerShape(percent = 50),
            color = container,
            contentColor = content,
            shadowElevation = 4.dp,
        ) {
            Text(
                journalEntriesChipLabel(entries.size),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            )
        }
        val menuContainer = journalMenuContainerColor()
        val menuContent = journalMenuContentColor()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = menuContainer,
            modifier = Modifier.testTag(JOURNAL_ENTRIES_LIST_TAG).journalMenuColours(menuContainer, menuContent),
        ) {
            CompositionLocalProvider(LocalContentColor provides menuContent) {
                entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(journalEntryDateLabel(entry.date)) },
                        trailingIcon = { Text(HIDE_JOURNAL_ENTRY_LABEL, color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            if (entries.size == 1) expanded = false
                            onHide(entry.entryId)
                        },
                        colors = MenuDefaults.itemColors(textColor = menuContent),
                        modifier = Modifier.testTag(journalEntriesListRowTag(entry.entryId)),
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(HIDE_ALL_JOURNAL_ENTRIES_LABEL) },
                    onClick = {
                        expanded = false
                        onHideAll()
                    },
                    colors = MenuDefaults.itemColors(textColor = menuContent),
                    modifier = Modifier.testTag(JOURNAL_ENTRIES_HIDE_ALL_TAG),
                )
            }
        }
    }
}
