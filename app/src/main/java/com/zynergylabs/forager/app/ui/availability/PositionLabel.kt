package com.zynergylabs.forager.app.ui.availability

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ShownPosition
import com.zynergylabs.forager.app.ui.map.MapPosition

/** The map's label beside the dot: "Approximate location" or "Last seen …" (dispatch 2026-09-28-510). */
internal const val MAP_POSITION_LABEL_TAG = "map-position-label"

/** The compass strip's text while the position is approximate or last known. */
internal const val COMPASS_STRIP_POSITION_NOTE_TAG = "compass-strip-position-note"

/** The map label's words for an approximate position (the owner, RECORD -508). */
internal const val APPROXIMATE_LOCATION_TEXT = "Approximate location"

/** What the strip and the HUD add while waiting: the walker has nothing to do, GPS takes over by itself. */
internal const val FINDING_GPS_SUFFIX = ", finding GPS…"

/** "45 s", "6 min", "2 h", "3 d": coarse, since the number's job is "how old", not a stopwatch. */
internal fun formatSeenAge(ageMillis: Long): String = ""

/** "Last seen 2 h ago" (the owner's words, RECORD -508). */
internal fun lastSeenText(ageMillis: Long): String = "Last seen ${formatSeenAge(ageMillis)} ago"

/** The words for a position shown in place of GPS: the map label's and the strip's. */
internal data class PositionNote(val position: ShownPosition, val labelText: String, val stripText: String)

/** [PositionNote] for what [mapPosition] shows in place of GPS, or `null`; ticks while a "Last seen" age is shown. */
@Composable
internal fun rememberPositionNote(mapPosition: MapPosition?, currentTime: CurrentTimeProvider): State<PositionNote?> =
    produceState<PositionNote?>(null, mapPosition, currentTime) {}

/**
 * The label under the dot while the position is shown in place of GPS (dispatch 2026-09-28-510).
 */
@Composable
internal fun MapPositionLabel(note: State<PositionNote?>, anchor: State<Offset?>, modifier: Modifier = Modifier) = Unit
