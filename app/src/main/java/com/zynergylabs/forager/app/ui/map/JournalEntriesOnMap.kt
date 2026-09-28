package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.JournalEntryOnMap
import java.time.LocalDate

/** The report menu's item for an entry not shown on the map (J8, the dispatch's copy). */
const val SHOW_ON_MAP_LABEL = "Show on map"

/** The report menu's item for an entry shown on the map (J8, the dispatch's copy). */
const val HIDE_FROM_MAP_LABEL = "Hide from map"

/** The Layers sheet's switch for every highlight at once (J8, the dispatch's copy). */
const val JOURNAL_ENTRIES_OVERLAY_LABEL = "Journal entries"

/** The chip list's action for one entry (J8, the dispatch's copy). */
const val HIDE_JOURNAL_ENTRY_LABEL = "Hide"

/** The chip list's action for every entry (J8, the dispatch's copy). */
const val HIDE_ALL_JOURNAL_ENTRIES_LABEL = "Hide all"

/** J8 tests-first stub: no chip text yet. */
fun journalEntriesChipLabel(count: Int): String = ""

/** J8 tests-first stub: no date form yet. */
fun journalEntryDateLabel(date: LocalDate): String = ""

/** A bubble date line's accessibility label (owner's Q2 ruling, "Tap a date line"). */
fun openEntryAccessibilityLabel(date: String): String = "Open entry $date"

/** One keeping entry as a bubble line: its id, its date and the line's accessibility label. */
data class EntryLine(val entryId: String, val date: String, val accessibilityLabel: String)

/** What a highlighted record's bubble says about the shown entries that keep it. */
sealed interface KeptInEntriesLines {
    /** Three or fewer: one tappable date line each. */
    data class Dates(val lines: List<EntryLine>) : KeptInEntriesLines

    /** More than three: one tappable count line that opens an untitled list of [entries]. */
    data class Count(val label: String, val entries: List<EntryLine>) : KeptInEntriesLines
}

/** J8 tests-first stub: no entry lines yet. */
fun keptInEntriesLines(entries: List<JournalEntryOnMap>): KeptInEntriesLines? = null
