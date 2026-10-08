package com.zynergylabs.forager.app.ui.availability

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A calendar date as a person reads it: "Oct 7, 2026" (owner, RECORD -656: "Use 'Oct 7, 2026'
 * everywhere"). Data part C (RECORD -668) uses it on the Seasonal tab and the trip windows; the
 * other screens move to it in their own parts. The planned-trips row and its map bubble still use
 * [TRIP_WINDOW_DATE_FORMAT] ("MMM d"), which belongs to the trip planner's part, not this one.
 */
internal fun displayDate(date: LocalDate): String = DISPLAY_DATE_FORMAT.format(date)

private val DISPLAY_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
