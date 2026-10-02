package com.zynergylabs.forager.app.domain

/**
 * SKELETON (dispatch 2026-09-28-400, Amendment 3, Part 3b, second ruling, tests first): the shape
 * and nothing else. It runs nothing yet.
 */
class AbandonedTrackSweepOnce(
    @Suppress("unused") private val endAbandonedTracks: EndAbandonedTracksUseCase,
    @Suppress("unused") private val processStartedAtEpochMillis: Long,
) {
    suspend fun runOnce(): Result<AbandonedTracksSweep>? = null
}
