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
 * [EndAbandonedTracksUseCase] on a plain JVM (dispatch 2026-09-28-400, Amendment 3, Part 3b).
 *
 * The owner's path 1, answered "A": `Open Records > a track nothing is recording any more > it
 * shows as a normal finished track, ended at its last recorded point; Delete is offered as for any
 * track`. Records decides both of those from the row's end time, so what is tested here is the
 * one write that gives such a row an end time, and every case in which it must not write.
 *
 * `PROCESS_START` is the moment the application object was created. A row started before it was
 * left open by an earlier process, and nothing in this process is recording it.
 */
class EndAbandonedTracksUseCaseTest {

    private val logged = mutableListOf<String>()
    private var watched: String? = null

    private fun useCase(tracks: SweepTracks) =
        EndAbandonedTracksUseCase(tracks, watchedTrackId = { watched }, errorLog = { _, message, _ -> logged += message })

    private fun open(id: String, startedAt: Long = 1_000L, name: String? = null, originWaypointId: String? = null) =
        Track(id = id, name = name, startedAtEpochMillis = startedAt, endedAtEpochMillis = null, points = emptyList(), originWaypointId = originWaypointId)

    private fun stored(t: Long, kept: Boolean = true) =
        TrackPointRecord(TrackPoint(lat = 45.0, lng = -122.0, altitude = null, accuracyMeters = 5f, timestampEpochMillis = t), kept = kept)

    @Test
    fun `a track left open by an earlier process is ended at its last stored point, and nothing else in its row changes`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("stuck", startedAt = 1_000L, name = "Ridge", originWaypointId = "wp-1"), stored(2_000L), stored(9_000L), stored(5_000L))
        }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf(EndedAbandonedTrack("stuck", endedAtEpochMillis = 9_000L, clampedFromEpochMillis = null)), sweep.ended)
        assertEquals(
            Track(id = "stuck", name = "Ridge", startedAtEpochMillis = 1_000L, endedAtEpochMillis = 9_000L, points = emptyList(), originWaypointId = "wp-1"),
            tracks.row("stuck"),
        )
        assertEquals("no point was removed", 3, tracks.getFullRecord("stuck").getOrThrow().size)
    }

    /** Ruling B: the last STORED point, network fixes included. The end time says when recording stopped, and a fix the map hides is still a moment the phone was recording. */
    @Test
    fun `the last stored point counts even when it is one the screen leaves out as a network fix`() = runTest {
        val tracks = SweepTracks().apply { put(open("stuck"), stored(2_000L), stored(7_000L, kept = false)) }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(7_000L, sweep.ended.single().endedAtEpochMillis)
        assertEquals(7_000L, tracks.row("stuck")?.endedAtEpochMillis)
    }

    /** Ruling A's cut-off: a row started in this process is never a candidate, whatever the timing of the sweep against a Record tap. */
    @Test
    fun `a track started in this process is left open, at the cut-off and after it`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("just-before", startedAt = PROCESS_START - 1), stored(PROCESS_START - 1))
            put(open("at-start", startedAt = PROCESS_START), stored(PROCESS_START + 5))
            put(open("after-start", startedAt = PROCESS_START + 10), stored(PROCESS_START + 20))
        }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf("just-before"), sweep.ended.map { it.trackId })
        assertNull(tracks.row("at-start")?.endedAtEpochMillis)
        assertNull(tracks.row("after-start")?.endedAtEpochMillis)
    }

    /** Ruling A: the row the watch is for is left alone, whether the service has begun it or a Return was only accepted early for it. One comparison covers both. */
    @Test
    fun `the track the watch is for is left open`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("watched"), stored(2_000L))
            put(open("stuck"), stored(3_000L))
        }
        watched = "watched"

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf("stuck"), sweep.ended.map { it.trackId })
        assertNull(tracks.row("watched")?.endedAtEpochMillis)
    }

    /** The watch can become for a track while the sweep is reading: it is asked again just before the write. */
    @Test
    fun `a track the watch takes up while the sweep is reading is left open`() = runTest {
        val tracks = SweepTracks().apply { put(open("stuck"), stored(3_000L)) }
        tracks.beforeFullRecord = { watched = "stuck" }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertTrue(sweep.ended.isEmpty())
        assertNull(tracks.row("stuck")?.endedAtEpochMillis)
    }

    /**
     * The owner's answer for a stuck track with nothing recorded in it, "Option A" (Part 3c): `it
     * shows as a finished track with no points, ended at its start time; you can delete it`.
     * Nothing is deleted. It is reported apart from the tracks ended at a stored point.
     *
     * This test asserted the opposite until Part 3c: that such a row was left exactly as it was
     * and counted as left.
     */
    @Test
    fun `a track with no stored point is ended at its own start time, reported apart, and nothing else in its row changes`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("empty", startedAt = 4_000L, name = "No fix", originWaypointId = "wp-9"))
            put(open("stuck"), stored(3_000L))
        }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf(EndedAbandonedTrack("empty", endedAtEpochMillis = 4_000L, clampedFromEpochMillis = null)), sweep.endedWithNoStoredPoint)
        assertEquals(
            Track(id = "empty", name = "No fix", startedAtEpochMillis = 4_000L, endedAtEpochMillis = 4_000L, points = emptyList(), originWaypointId = "wp-9"),
            tracks.row("empty"),
        )
        assertEquals("the track with a stored point is still reported with the others", listOf("stuck"), sweep.ended.map { it.trackId })
    }

    /** The same two conditions as every other candidate: it started before this process did, and the watch is not for it. */
    @Test
    fun `a track with no stored point is left open when it started in this process, or the watch is for it`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("starting-now", startedAt = PROCESS_START + 5))
            put(open("watched", startedAt = 2_000L))
            put(open("empty", startedAt = 3_000L))
        }
        watched = "watched"

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf("empty"), sweep.endedWithNoStoredPoint.map { it.trackId })
        assertNull(tracks.row("starting-now")?.endedAtEpochMillis)
        assertNull(tracks.row("watched")?.endedAtEpochMillis)
    }

    /** The conditional write applies to these too: one ended by something else in the meantime keeps that end. */
    @Test
    fun `a track with no stored point that something else ended between the read and the write keeps that end`() = runTest {
        val tracks = SweepTracks().apply { put(open("racing", startedAt = 3_000L)) }
        tracks.beforeFullRecord = { tracks.forceEnd("racing", 99_000L) }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(99_000L, tracks.row("racing")?.endedAtEpochMillis)
        assertTrue(sweep.endedWithNoStoredPoint.isEmpty())
    }

    /** Ruling D: the end written is never earlier than the row's start. A point's time is the fix's own and the start is the phone's clock, so they can disagree. */
    @Test
    fun `an end that would be earlier than the start is written as the start, and reported with both times`() = runTest {
        val tracks = SweepTracks().apply { put(open("skewed", startedAt = 10_000L), stored(4_000L), stored(6_000L)) }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(listOf(EndedAbandonedTrack("skewed", endedAtEpochMillis = 10_000L, clampedFromEpochMillis = 6_000L)), sweep.ended)
        assertEquals(10_000L, tracks.row("skewed")?.endedAtEpochMillis)
    }

    @Test
    fun `a track that already has an end time is not touched`() = runTest {
        val finished = open("finished").copy(endedAtEpochMillis = 4_000L)
        val tracks = SweepTracks().apply { put(finished, stored(2_000L), stored(9_000L)) }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertTrue(sweep.ended.isEmpty())
        assertEquals(finished, tracks.row("finished"))
    }

    /**
     * Ruling C: the write is the conditional one. If something else ended the row between the
     * sweep's read and its write (the recording service finishing a stop), the true end stays and
     * the sweep does not count the track as one it ended.
     */
    @Test
    fun `a track ended by something else between the read and the write keeps that end`() = runTest {
        val tracks = SweepTracks().apply { put(open("racing"), stored(3_000L)) }
        tracks.beforeFullRecord = { runCatching { tracks.forceEnd("racing", 99_000L) } }

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(99_000L, tracks.row("racing")?.endedAtEpochMillis)
        assertTrue(sweep.ended.isEmpty())
    }

    @Test
    fun `a track whose points cannot be read is left open, logged and counted, and the others are still ended`() = runTest {
        val tracks = SweepTracks().apply {
            put(open("unreadable"), stored(2_000L))
            put(open("stuck"), stored(3_000L))
        }
        tracks.failFullRecordFor = "unreadable"

        val sweep = useCase(tracks)(PROCESS_START).getOrThrow()

        assertEquals(1, sweep.failed)
        assertNull(tracks.row("unreadable")?.endedAtEpochMillis)
        assertEquals(listOf("stuck"), sweep.ended.map { it.trackId })
        assertEquals(listOf("Couldn't read the stored points of open track 'unreadable'; it is left open."), logged)
    }

    @Test
    fun `a failure to list the tracks is a failure, not an empty sweep`() = runTest {
        val tracks = SweepTracks().apply { failGetAll = true }

        assertTrue(useCase(tracks)(PROCESS_START).isFailure)
    }

    private companion object {
        const val PROCESS_START = 1_000_000L
    }
}

/** A track store that keeps rows and stored points apart, as Room does, and ends a row only through the conditional write. */
private class SweepTracks : TrackRepository {
    private val rows = linkedMapOf<String, Track>()
    private val points = mutableMapOf<String, List<TrackPointRecord>>()
    var failGetAll = false
    var failFullRecordFor: String? = null
    var beforeFullRecord: (() -> Unit)? = null

    fun put(track: Track, vararg stored: TrackPointRecord) {
        rows[track.id] = track
        // Stored order is by timestamp, as TrackDao's query returns it.
        points[track.id] = stored.sortedBy { it.point.timestampEpochMillis }
    }

    fun row(id: String): Track? = rows[id]

    fun forceEnd(id: String, at: Long) {
        rows[id] = rows.getValue(id).copy(endedAtEpochMillis = at)
    }

    override suspend fun getAll(): Result<List<Track>> =
        if (failGetAll) Result.failure(IllegalStateException("the database could not be read")) else Result.success(rows.values.toList())
    override suspend fun getById(id: String): Result<Track?> = Result.success(rows[id])
    override suspend fun getFullRecord(id: String): Result<List<TrackPointRecord>> {
        beforeFullRecord?.invoke()
        return if (id == failFullRecordFor) Result.failure(IllegalStateException("the points could not be read")) else Result.success(points[id].orEmpty())
    }
    override suspend fun getForDay(dayStartInclusiveEpochMillis: Long, dayEndExclusiveEpochMillis: Long): Result<List<Track>> =
        Result.failure(UnsupportedOperationException("getForDay is not part of this test's path"))
    override suspend fun create(track: Track): Result<Unit> = Result.failure(UnsupportedOperationException("create is not part of this test's path"))
    override suspend fun appendPoints(trackId: String, points: List<TrackPoint>): Result<Unit> =
        Result.failure(UnsupportedOperationException("appendPoints is not part of this test's path"))
    override suspend fun end(trackId: String, endedAtEpochMillis: Long): Result<Unit> =
        Result.failure(UnsupportedOperationException("the sweep must use endIfOpen, never the unconditional end"))
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
