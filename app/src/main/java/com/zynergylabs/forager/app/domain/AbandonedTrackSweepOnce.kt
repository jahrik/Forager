package com.zynergylabs.forager.app.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs [EndAbandonedTracksUseCase] once in a process, with the process's own start time as its
 * cut-off (dispatch 2026-09-28-400, Amendment 3, Part 3b, the planner's second ruling).
 *
 * ## What launches it, and what was tried first
 *
 * The first recording ViewModel to be created calls [runOnce]. A second ViewModel in the same
 * process (a second Activity, or the screen opened again) calls it too and gets `null`: it has
 * already run.
 *
 * It was first launched from `ForagerApplication.onCreate`. That made the app's database open at
 * every app start, which nothing had done before, and an existing test that replaces the database
 * file then failed in 7 runs of 10, against 0 of 10 with the call removed. So `onCreate` now only
 * notes the start time and hands it to the container, and nothing in it queries the database.
 *
 * ## Why launching it from a screen is safe
 *
 * The cut-off does the work. [processStartedAtEpochMillis] is taken at the top of
 * `ForagerApplication.onCreate`, and only a track that started before it is ever a candidate. A
 * recording that is just starting, one that is just stopping, and anything a second screen is
 * doing all concern a track started in this process, and no such track is a candidate, whenever
 * this runs.
 *
 * ## What that costs
 *
 * A process that starts with no screen (the scheduled backup's worker, a sticky restart of the
 * recording service) does not sweep; the sweep runs when the app is next opened. A track left
 * open while the process lives waits for the next process. Tracks restored from an older backup
 * stay open until the next process.
 *
 * ## Once means once completed
 *
 * A run that could not list the tracks at all has not happened, and the next call tries again.
 * So does a run that was cancelled with its screen. Two calls at once wait their turn: the second
 * finds the first has finished and returns `null`.
 */
class AbandonedTrackSweepOnce(
    private val endAbandonedTracks: EndAbandonedTracksUseCase,
    private val processStartedAtEpochMillis: Long,
    /** Told what each completed run did. `AppContainer` logs it: this is the one place the app changes a stored record without being asked, and a log line is the only trace on a phone. */
    private val onSwept: (AbandonedTracksSweep) -> Unit = {},
) {
    private val mutex = Mutex()
    private var done = false

    /** The sweep's result, or `null` if it has already run in this process. */
    suspend fun runOnce(): Result<AbandonedTracksSweep>? = mutex.withLock {
        if (done) return@withLock null
        endAbandonedTracks(processStartedAtEpochMillis).onSuccess { sweep ->
            done = true
            onSwept(sweep)
        }
    }
}
