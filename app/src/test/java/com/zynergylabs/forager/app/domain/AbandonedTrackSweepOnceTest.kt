package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [AbandonedTrackSweepOnce] on a plain JVM: the sweep runs once in a process, with the process's
 * own start time as its cut-off (dispatch 2026-09-28-400, Amendment 3, Part 3b, second ruling).
 * The rule itself is `EndAbandonedTracksUseCaseTest`'s.
 */
class AbandonedTrackSweepOnceTest {

    private val tracks = CountingTracks()
    private fun once(processStartedAt: Long = 1_000L) =
        AbandonedTrackSweepOnce(EndAbandonedTracksUseCase(tracks, { null }, { _, _, _ -> }), processStartedAt)

    @Test
    fun `the first call sweeps with the process's start time as the cut-off, and the second does nothing at all`() = runTest {
        tracks.put("from-before", startedAt = 500L, lastPointAt = 700L)
        tracks.put("from-this-process", startedAt = 1_500L, lastPointAt = 1_700L)
        val once = once(processStartedAt = 1_000L)

        val first = once.runOnce()

        assertEquals(listOf("from-before"), first!!.getOrThrow().ended.map { it.trackId })
        assertNull("a row started in this process is not a candidate", tracks.endOf("from-this-process"))
        val readsAfterFirst = tracks.listReads

        tracks.put("left-later", startedAt = 600L, lastPointAt = 800L)
        val second = once.runOnce()

        assertNull("a second call in the same process reports that it did not run", second)
        assertEquals("and reads nothing", readsAfterFirst, tracks.listReads)
        assertNull("so a row that would otherwise qualify is left for the next process", tracks.endOf("left-later"))
    }

    /** A sweep that could not list the tracks has not happened. The next screen to be created tries again. */
    @Test
    fun `a sweep that failed to list the tracks is tried again by the next call`() = runTest {
        tracks.put("from-before", startedAt = 500L, lastPointAt = 700L)
        val once = once()
        tracks.failList = true

        assertTrue(once.runOnce()!!.isFailure)

        tracks.failList = false
        assertEquals(listOf("from-before"), once.runOnce()!!.getOrThrow().ended.map { it.trackId })
        assertNull(once.runOnce())
    }
}

private class CountingTracks : TrackRepository {
    private val rows = linkedMapOf<String, Track>()
    private val lastPoint = mutableMapOf<String, Long>()
    var listReads = 0
    var failList = false

    fun put(id: String, startedAt: Long, lastPointAt: Long) {
        rows[id] = Track(id = id, name = null, startedAtEpochMillis = startedAt, endedAtEpochMillis = null, points = emptyList())
        lastPoint[id] = lastPointAt
    }

    fun endOf(id: String): Long? = rows[id]?.endedAtEpochMillis

    override suspend fun getAll(): Result<List<Track>> {
        listReads++
        return if (failList) Result.failure(IllegalStateException("the database could not be read")) else Result.success(rows.values.toList())
    }
    override suspend fun getById(id: String): Result<Track?> = Result.success(rows[id])
    override suspend fun getFullRecord(id: String): Result<List<TrackPointRecord>> =
        Result.success(listOfNotNull(lastPoint[id]?.let { TrackPointRecord(TrackPoint(45.0, -122.0, null, 5f, it), kept = true) }))
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Track>> =
        Result.failure(UnsupportedOperationException("getForDay is not part of this test's path"))
    override suspend fun create(track: Track): Result<Unit> = Result.failure(UnsupportedOperationException("create is not part of this test's path"))
    override suspend fun appendPoints(trackId: String, points: List<TrackPoint>): Result<Unit> =
        Result.failure(UnsupportedOperationException("appendPoints is not part of this test's path"))
    override suspend fun end(trackId: String, endedAtEpochMillis: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException("the sweep must use endIfOpen"))
    override suspend fun endIfOpen(trackId: String, endedAtEpochMillis: Long): Result<Boolean> {
        val row = rows[trackId] ?: return Result.success(false)
        if (row.endedAtEpochMillis != null) return Result.success(false)
        rows[trackId] = row.copy(endedAtEpochMillis = endedAtEpochMillis)
        return Result.success(true)
    }
    override suspend fun setOriginWaypoint(trackId: String, waypointId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("setOriginWaypoint is not part of this test's path"))
    override suspend fun delete(id: String): Result<Unit> = Result.failure(UnsupportedOperationException("delete is not part of this test's path"))
}
