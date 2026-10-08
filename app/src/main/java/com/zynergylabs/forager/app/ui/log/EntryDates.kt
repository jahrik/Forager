package com.zynergylabs.forager.app.ui.log

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A date on the journal entry's report and editor, written "Oct 7, 2026" (the owner in RECORD -656:
 * "Use 'Oct 7, 2026' everywhere"; dispatch 2026-09-28-667 applies it to this part's screens, and data
 * part D to the rest). The phone's language names the month.
 */
internal fun formatEntryDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
