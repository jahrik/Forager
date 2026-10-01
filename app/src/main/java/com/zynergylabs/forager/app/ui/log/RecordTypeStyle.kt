package com.zynergylabs.forager.app.ui.log

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** The four kinds of record the Journal's Records destination holds. */
internal enum class RecordType { FINDS, TRACKS, WAYPOINTS, OFFLINE_MAPS }

/**
 * One record type's two colours: [accent] for its icon and label, [container] for the surface
 * behind them (a selected chip, a row's type badge).
 */
@Immutable
internal data class RecordTypeColors(val accent: Color, val container: Color)

/**
 * One colour role per record type, in one place — journal redesign J1, S2 (plan J6 in
 * `docs/plans/journal-redesign.md`). Every Journal surface that marks a record's type (the Records
 * filter chips and the type badges in this stage; card stats and species chips in J3) reads its
 * colours from here, so a type is the same colour everywhere it appears.
 *
 * The mapping is the plan's table exactly:
 *
 * | Type | accent | container |
 * |---|---|---|
 * | Finds | `secondary` | `secondaryContainer` |
 * | Tracks | `tertiary` | `tertiaryContainer` |
 * | Waypoints | `primary` | `primaryContainer` |
 * | Offline maps | `onSurfaceVariant` | `surfaceContainerHighest` |
 *
 * **Theme roles, never `Color.kt` values.** Everything is read from the [ColorScheme] in force
 * ([MaterialTheme.colorScheme] via [colors]), so night mode and the Understory theme carry through
 * without this object knowing about either. [colorsIn] is the same mapping over an explicit scheme,
 * so the table can be checked without a composition.
 */
internal object RecordTypeStyle {

    fun colorsIn(scheme: ColorScheme, type: RecordType): RecordTypeColors = when (type) {
        RecordType.FINDS -> RecordTypeColors(accent = scheme.secondary, container = scheme.secondaryContainer)
        RecordType.TRACKS -> RecordTypeColors(accent = scheme.tertiary, container = scheme.tertiaryContainer)
        RecordType.WAYPOINTS -> RecordTypeColors(accent = scheme.primary, container = scheme.primaryContainer)
        RecordType.OFFLINE_MAPS -> RecordTypeColors(accent = scheme.onSurfaceVariant, container = scheme.surfaceContainerHighest)
    }

    @Composable
    @ReadOnlyComposable
    fun colors(type: RecordType): RecordTypeColors = colorsIn(MaterialTheme.colorScheme, type)
}
