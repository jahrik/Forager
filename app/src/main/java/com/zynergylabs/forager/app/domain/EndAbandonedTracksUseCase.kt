package com.zynergylabs.forager.app.domain

/**
 * One track the sweep ended. [clampedFromEpochMillis] is the last stored point's time when that
 * was earlier than the track's start and the start was written in its place; `null` otherwise.
 */
data class EndedAbandonedTrack(val trackId: String, val endedAtEpochMillis: Long, val clampedFromEpochMillis: Long?)

/**
 * What one run of [EndAbandonedTracksUseCase] did. [ended] are the tracks ended at their last
 * stored point; [endedWithNoStoredPoint] are the ones with nothing recorded in them, ended at
 * their own start; [failed] counts tracks it could not read or write, each logged.
 */
data class AbandonedTracksSweep(
    val ended: List<EndedAbandonedTrack> = emptyList(),
    val endedWithNoStoredPoint: List<EndedAbandonedTrack> = emptyList(),
    val failed: Int = 0,
)

/**
 * Ends tracks that were left open and that nothing is recording any more (dispatch
 * 2026-09-28-400, Amendment 3, Part 3b; plan task T1).
 *
 * ## What the owner asked for
 *
 * `Open Records > a track nothing is recording any more`, answered "A": **it shows as a normal
 * finished track, ended at its last recorded point; Delete is offered as for any track.** Records
 * reads "finished" from the row's end time and offers Delete from the same field, so the whole of
 * this is one write: an end time on a row that has none.
 *
 * Such rows come from two places: a process killed while recording (nothing resumes a recording,
 * and nothing else ever ends its row), and a fault in earlier builds that could start a second
 * track the recording service never wrote to. Until this, they read "Still recording" for good
 * and the app refused to delete them.
 *
 * ## When it runs
 *
 * Once in a process, when the first recording ViewModel is created. [AbandonedTrackSweepOnce]
 * holds the "once" and the reasons: why it is launched from a screen and not from
 * `ForagerApplication.onCreate`, where it was first put, and what that costs. This class only
 * holds the rule, and is given the process's start time by whoever runs it.
 *
 * Whenever it runs in a process, the cut-off below is what keeps it away from anything that
 * process is doing: a recording just starting, one just stopping, a second screen. Each of those
 * concerns a track started in this process, and no such track is ever a candidate.
 *
 * ## Which rows
 *
 * An open row (no end time) whose **start is earlier than [invoke]'s `processStartedAtEpochMillis`**,
 * and which is **not the track the watch is for**.
 * - The cut-off is what makes the timing of this sweep against a Record tap not matter: a row
 *   started in this process is never a candidate.
 * - The watch's track covers a recording the service has begun and a Return accepted early for a
 *   track about to be recorded. It is asked when the rows are listed and again just before each
 *   write.
 *
 * ## What is written
 *
 * The end time, and nothing else. It is the time of the track's **last stored point**, read
 * through [TrackRepository.getFullRecord], which includes the fixes the screen leaves out as
 * network fixes: the end time says when recording stopped, and a fix the map hides is still a
 * moment the phone was recording (ruling B). **This is not a display consumer.** `getFullRecord`'s
 * own comment keeps display code away from the unfiltered record; this reads one timestamp from it
 * to write a row, and shows nothing.
 *
 * Never earlier than the row's start. A point's time is the fix's own and the start is the phone's
 * clock, so they can disagree; then the start is written, and the result says so with both times
 * (ruling D).
 *
 * The write is [TrackRepository.endIfOpen], which ends a row only if it is still open. If the
 * recording service wrote the true end between this sweep's read and its write, that end stays
 * (ruling C).
 *
 * ## A track with nothing recorded in it
 *
 * The owner's answer (Part 3c, "Option A"): `Open Records > a stuck track with nothing recorded in
 * it > it shows as a finished track with no points, ended at its start time; you can delete it`.
 * So a candidate with no stored point is ended **at its own start time**, through the same
 * conditional write and under the same two conditions as every other candidate. It is reported
 * apart from the others. The other answer on offer was for the app to remove such a track by
 * itself; the owner chose not to have anything deleted without doing it themselves.
 *
 * ## What it does not do
 *
 * Nothing is deleted. No row is removed, no waypoint is touched, and no point is removed from any
 * track. A track it could not read or could not write is left open, logged and counted.
 */
class EndAbandonedTracksUseCase(
    private val trackRepository: TrackRepository,
    /** The track [ReturnWatch] is for right now, or `null`. A function, so it is asked at the moment it matters. */
    private val watchedTrackId: () -> String?,
    private val errorLog: ErrorLog,
) {
    suspend operator fun invoke(processStartedAtEpochMillis: Long): Result<AbandonedTracksSweep> =
        trackRepository.getAll().map { tracks ->
            val ended = mutableListOf<EndedAbandonedTrack>()
            val endedWithNoStoredPoint = mutableListOf<EndedAbandonedTrack>()
            var failed = 0
            val candidates = tracks.filter {
                it.endedAtEpochMillis == null && it.startedAtEpochMillis < processStartedAtEpochMillis && it.id != watchedTrackId()
            }
            for (track in candidates) {
                val stored = trackRepository.getFullRecord(track.id).getOrElse { error ->
                    errorLog.w(TAG, "Couldn't read the stored points of open track '${track.id}'; it is left open.", error)
                    failed++
                    null
                } ?: continue
                // Stored order is by time, but the latest time is what is meant, so it is asked for by value.
                val lastStoredAt = stored.maxOfOrNull { it.point.timestampEpochMillis }
                // Nothing recorded in it: ended at its own start. Otherwise at the last stored point, never before the start.
                val endAt = if (lastStoredAt == null) track.startedAtEpochMillis else maxOf(lastStoredAt, track.startedAtEpochMillis)
                // Asked again here: the reads above suspended, and the watch may have become for this track meanwhile.
                if (track.id == watchedTrackId()) continue
                trackRepository.endIfOpen(track.id, endAt)
                    .onSuccess { wrote ->
                        if (!wrote) return@onSuccess
                        if (lastStoredAt == null) {
                            endedWithNoStoredPoint += EndedAbandonedTrack(track.id, endAt, clampedFromEpochMillis = null)
                        } else {
                            ended += EndedAbandonedTrack(track.id, endAt, clampedFromEpochMillis = lastStoredAt.takeIf { it < track.startedAtEpochMillis })
                        }
                    }
                    .onFailure { error ->
                        errorLog.w(TAG, "Couldn't end open track '${track.id}'; it is left open.", error)
                        failed++
                    }
            }
            AbandonedTracksSweep(ended = ended, endedWithNoStoredPoint = endedWithNoStoredPoint, failed = failed)
        }

    private companion object {
        const val TAG = "EndAbandonedTracks"
    }
}
