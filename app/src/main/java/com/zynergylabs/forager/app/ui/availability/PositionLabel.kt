package com.zynergylabs.forager.app.ui.availability

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ShownPosition
import com.zynergylabs.forager.app.domain.ageMillis
import com.zynergylabs.forager.app.domain.inPlaceOfGps
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorDark
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorLight
import com.zynergylabs.forager.app.ui.map.MapPosition
import com.zynergylabs.forager.app.ui.map.mapChromeContainerColor
import com.zynergylabs.forager.app.ui.theme.Bark
import com.zynergylabs.forager.app.ui.theme.LocalForagerDarkTheme
import com.zynergylabs.forager.app.ui.theme.Spacing
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/** The map's label beside the dot: "Approximate location" or "Last seen …" (dispatch 2026-09-28-510). */
internal const val MAP_POSITION_LABEL_TAG = "map-position-label"

/** The compass strip's text while the position is approximate or last known. */
internal const val COMPASS_STRIP_POSITION_NOTE_TAG = "compass-strip-position-note"

/** The map label's words for an approximate position (the owner, RECORD -508). */
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

/** The words for a position shown in place of GPS: the map label's and the strip's. */
internal data class PositionNote(val position: ShownPosition, val labelText: String, val stripText: String)

/** [PositionNote] for [position] at [nowEpochMillis]; `null` for a position that is not shown in place of GPS. */
internal fun positionNoteFor(position: ShownPosition?, nowEpochMillis: Long): PositionNote? = when (position) {
    is ShownPosition.Approximate -> PositionNote(position, APPROXIMATE_LOCATION_TEXT, APPROXIMATE_LOCATION_TEXT + FINDING_GPS_SUFFIX)
    is ShownPosition.LastKnown -> lastSeenText(position.fix.ageMillis(nowEpochMillis)).let { PositionNote(position, it, it + FINDING_GPS_SUFFIX) }
    else -> null
}

/**
 * [PositionNote] for what [mapPosition] shows in place of GPS ([inPlaceOfGps]), or `null`: the one value
 * the map's label and the strip read, so the two say the same thing. Recomputed whenever the shown
 * position changes, and once a second while a "Last seen" age is on screen; never otherwise. A State so
 * that only its readers, the label and the strip, recompose.
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

/**
 * The label under the dot while the position is shown in place of GPS (dispatch 2026-09-28-510; the
 * owner: a soft dot "labelled 'Approximate location'", and the last known position "marked old").
 *
 * Centred under the dot at [anchor], the point the map reports in its own pixels on every camera move,
 * [MAP_POSITION_LABEL_GAP] below the dot's centre so it does not cover the dot. The anchor is read in
 * placement only, so a moving camera re-places the label without recomposing anything. Not placed while
 * the map has reported no dot.
 *
 * Map chrome at 80% (CLAUDE.md, "Nothing fully obstructs the map view"): the fill is the cluster's own
 * chrome colour, which carries [com.zynergylabs.forager.app.ui.map.MAP_CHROME_OVER_MAP_ALPHA], and the
 * text is opaque. A plain background with no pointer input, not a Surface, so it takes no touch: a tap on
 * it reaches the map, or the search bar beneath it (CLAUDE.md, the Surface pitfall).
 */
@Composable
internal fun MapPositionLabel(note: State<PositionNote?>, anchor: State<Offset?>, modifier: Modifier = Modifier) {
    val current = note.value ?: return
    val isDarkTheme = LocalForagerDarkTheme.current
    val fill = if (isDarkTheme) MapIconStackButtonColorDark else MapIconStackButtonColorLight
    CompositionLocalProvider(LocalContentColor provides if (isDarkTheme) Color.White else Bark) {
        Text(
            text = current.labelText,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                    layout(placeable.width, placeable.height) {
                        val at = anchor.value ?: return@layout
                        placeable.place(at.x.roundToInt() - placeable.width / 2, at.y.roundToInt() + MAP_POSITION_LABEL_GAP.roundToPx())
                    }
                }
                .background(fill, RoundedCornerShape(MAP_POSITION_LABEL_CORNER))
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                .testTag(MAP_POSITION_LABEL_TAG)
                .mapChromeContainerColor(fill),
        )
    }
}

/** From the dot's centre to the label's top: MapLibre's dot is about 22 dp across at full size, so this clears it. **Provisional.** */
private val MAP_POSITION_LABEL_GAP = 18.dp

private val MAP_POSITION_LABEL_CORNER = 6.dp

/** How often a "Last seen" age is re-read; its finest unit is a second. */
private const val SEEN_AGE_TICK_MILLIS = 1_000L
