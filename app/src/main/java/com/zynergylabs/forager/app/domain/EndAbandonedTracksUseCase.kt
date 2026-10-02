package com.zynergylabs.forager.app.domain

/** One track the sweep ended. [clampedFromEpochMillis] is the last stored point's time when that was earlier than the track's start and the start was written in its place; `null` otherwise. */
data class EndedAbandonedTrack(val trackId: String, val endedAtEpochMillis: Long, val clampedFromEpochMillis: Long?)

/** What one run of [EndAbandonedTracksUseCase] did. */
data class AbandonedTracksSweep(
    val ended: List<EndedAbandonedTrack> = emptyList(),
    val leftWithNoStoredPoint: Int = 0,
    val failed: Int = 0,
)

/**
 * SKELETON (dispatch 2026-09-28-400, Amendment 3, Part 3b, tests first): the shape and nothing
 * else. It ends nothing yet, so its tests fail on their assertions and not on a missing symbol.
 */
class EndAbandonedTracksUseCase(
    @Suppress("unused") private val trackRepository: TrackRepository,
    @Suppress("unused") private val watchedTrackId: () -> String?,
    @Suppress("unused") private val errorLog: ErrorLog,
) {
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(processStartedAtEpochMillis: Long): Result<AbandonedTracksSweep> = Result.success(AbandonedTracksSweep())
}
