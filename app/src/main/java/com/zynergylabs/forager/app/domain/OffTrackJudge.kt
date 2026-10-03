package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.TrackPoint

/** How far beyond the path, plus the reading's own reported accuracy, counts as off it. The owner: "About 40 m". Provisional. */
const val OFF_TRACK_LINE_METERS = 40.0

/** How long a walker must be off the path before it alerts. The owner: "About 15 seconds". Provisional. */
const val OFF_TRACK_HOLD_MILLIS = 15_000L

/** How many counting readings, at least, must be off the path before it alerts. The planner's number. Provisional. */
const val OFF_TRACK_MIN_READINGS = 3

/** How long back on the path re-arms the alert. The owner: "back on the path for a little while"; the planner's number. Provisional. */
const val OFF_TRACK_REARM_MILLIS = 10_000L

/** What [OffTrackJudge.next] decided for one reading. */
data class OffTrackVerdict(val isOffTrack: Boolean, val alert: Boolean)

/**
 * "Off track", redefined to fit the walker (dispatch 2026-09-28-425, plan task T21). Work in
 * progress: this is the interface the tests are written against, and it decides nothing yet.
 */
class OffTrackJudge(path: List<TrackPoint>) {
    fun next(reading: TrackPoint): OffTrackVerdict = OffTrackVerdict(isOffTrack = false, alert = false)
}
