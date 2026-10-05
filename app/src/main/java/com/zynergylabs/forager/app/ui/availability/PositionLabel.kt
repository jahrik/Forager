package com.zynergylabs.forager.app.ui.availability

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ShownPosition
import com.zynergylabs.forager.app.domain.ageMillis
import com.zynergylabs.forager.app.domain.inPlaceOfGps
import com.zynergylabs.forager.app.ui.map.MapPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/** The compass strip's text while the position is approximate or last known. */
internal const val COMPASS_STRIP_POSITION_NOTE_TAG = "compass-strip-position-note"

/**
 * The words for an approximate position (the owner, RECORD -508), which the strip reads. Dispatch
 * 2026-09-28-535 (RECORD -534) took away the label that once carried them under the dot: it repeated the strip.
 */
internal const val APPROXIMATE_LOCATION_TEXT = "Approximate location"

/** What the strip and the HUD add while waiting: the walker has nothing to do, GPS takes over by itself. */
internal const val FINDING_GPS_SUFFIX = ", finding GPS…"

/** "45 s", "6 min", "2 h", "3 d": coarse, since the number's job is "how old", not a stopwatch. Never negative. */
internal fun formatSeenAge(ageMillis: Long): String {
    val seconds = (ageMillis / 1_000L).coerceAtLeast(0L)
    return when {
        seconds < 60L -> "$seconds s"
        seconds < 3_600L -> "${seconds / 60L} min"
        seconds < 86_400L -> "${seconds / 3_600L} h"
        else -> "${seconds / 86_400L} d"
    }
}

/** "Last seen 2 h ago" (the owner's words, RECORD -508). */
internal fun lastSeenText(ageMillis: Long): String = "Last seen ${formatSeenAge(ageMillis)} ago"

/** The strip's words for a position shown in place of GPS. */
internal data class PositionNote(val position: ShownPosition, val stripText: String)

/** [PositionNote] for [position] at [nowEpochMillis]; `null` for a position that is not shown in place of GPS. */
internal fun positionNoteFor(position: ShownPosition?, nowEpochMillis: Long): PositionNote? = when (position) {
    is ShownPosition.Approximate -> PositionNote(position, APPROXIMATE_LOCATION_TEXT + FINDING_GPS_SUFFIX)
    is ShownPosition.LastKnown -> PositionNote(position, lastSeenText(position.fix.ageMillis(nowEpochMillis)) + FINDING_GPS_SUFFIX)
    else -> null
}

/**
 * [PositionNote] for what [mapPosition] shows in place of GPS ([inPlaceOfGps]), or `null`: what the strip
 * reads. Recomputed whenever the shown position changes, and once a second while a "Last seen" age is on
 * screen; never otherwise. A State so that only its reader, the strip, recomposes.
 */
@Composable
internal fun rememberPositionNote(mapPosition: MapPosition?, currentTime: CurrentTimeProvider): State<PositionNote?> =
    produceState<PositionNote?>(null, mapPosition, currentTime) {
        val position = mapPosition ?: return@produceState
        snapshotFlow { position.shown.value.inPlaceOfGps(position.liveFix.value) }.collectLatest { inPlace ->
            while (true) {
                value = positionNoteFor(inPlace, currentTime.nowEpochMillis())
                if (inPlace !is ShownPosition.LastKnown) break
                delay(SEEN_AGE_TICK_MILLIS)
            }
        }
    }

/** How often a "Last seen" age is re-read; its finest unit is a second. */
private const val SEEN_AGE_TICK_MILLIS = 1_000L
