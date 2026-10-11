package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.Track

/**
 * A recording shorter than this is left out of a day's new journal entry (RECORD -867, the owner: "Leave out
 * recordings under a minute (Recommended)"). It stays in Records, and the entry's editor still lists it, left out,
 * so it can be included by hand. The owner's Oct 10 entry held one: a recording started and stopped inside a
 * minute, whose Start and End markers led the waypoint table and whose single point blanked Climb.
 */
const val SHORT_RECORDING_MILLIS: Long = 60_000L

/**
 * Whether this track is a recording shorter than [SHORT_RECORDING_MILLIS], start to end as the recording itself
 * marked them. `false` for a track still recording (no end yet, so its length is not known) and for an imported
 * track, which is not a recording and whose times may be only an ordering.
 */
fun Track.isShortRecording(): Boolean {
    val ended = endedAtEpochMillis ?: return false
    if (importedAtEpochMillis != null) return false
    return ended - startedAtEpochMillis < SHORT_RECORDING_MILLIS
}
