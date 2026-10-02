package com.zynergylabs.forager.app

import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-400, Amendment 3, Part 3b, through the real entry point,
 * [ForagerApplication.onCreate], as `ForagerApplicationGpxCacheTest` does for the clean-up beside
 * it. Robolectric has already run `onCreate` once from the manifest before the body starts, on an
 * empty database. The body writes track rows and then starts the app again with the same
 * `onCreate` a new process runs, which builds a new container over the same database file. The
 * sweep runs off the main thread, so the check polls.
 *
 * **The first `onCreate` launched a sweep too,** and it may still be running when the body starts.
 * So every open row here is started after that first app start: the first sweep's cut-off leaves
 * it alone whatever its timing, and only the app start under test can end it. An earlier version
 * of these tests dated the rows an hour back; the first sweep could then reach them first, and a
 * revert check failed for a reason that was not its edit. That is how this was found.
 *
 * The owner's path 1, "A": a track nothing is recording any more becomes a normal finished track,
 * ended at its last recorded point. Records reads "finished" and offers Delete from the row's end
 * time, so the end time is what is asserted.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ForagerApplicationAbandonedTracksTest {

    private fun point(t: Long) = TrackPoint(lat = 45.0, lng = -122.0, altitude = null, accuracyMeters = 5f, timestampEpochMillis = t)

    private fun ForagerApplication.endOf(trackId: String): Long? = runBlocking { container.trackRepository.getById(trackId) }.getOrThrow()?.endedAtEpochMillis

    @Test
    fun `app start ends a track an earlier process left open, at its last stored point, and leaves the others as they are`() {
        val app = ApplicationProvider.getApplicationContext<ForagerApplication>()
        Thread.sleep(5) // strictly after the first app start, so that start's own sweep cannot touch these rows
        val before = System.currentTimeMillis()
        runBlocking {
            val tracks = app.container.trackRepository
            // Left open by an earlier process, with stored points: the one to end.
            tracks.create(Track(id = "stuck", name = null, startedAtEpochMillis = before, endedAtEpochMillis = null, points = emptyList())).getOrThrow()
            tracks.appendPoints("stuck", listOf(point(before + 1), point(before + 2))).getOrThrow()
            // Left open with no stored point: not built, left exactly as it is.
            tracks.create(Track(id = "empty", name = null, startedAtEpochMillis = before, endedAtEpochMillis = null, points = emptyList())).getOrThrow()
            // Started "in this process": its start is later than the moment the app object is created below.
            tracks.create(Track(id = "new", name = null, startedAtEpochMillis = before + 3_600_000L, endedAtEpochMillis = null, points = emptyList())).getOrThrow()
            tracks.appendPoints("new", listOf(point(before + 3_660_000L))).getOrThrow()
            // Already finished: its end must not move.
            tracks.create(Track(id = "finished", name = null, startedAtEpochMillis = before, endedAtEpochMillis = before + 1, points = emptyList())).getOrThrow()
            tracks.appendPoints("finished", listOf(point(before + 2))).getOrThrow()
        }
        Thread.sleep(5) // the app start below is strictly later than the rows' start

        app.onCreate()

        val deadline = System.currentTimeMillis() + 5_000
        while (app.endOf("stuck") == null && System.currentTimeMillis() < deadline) Thread.sleep(20)
        assertEquals("the track left open by an earlier process is ended at its last stored point", before + 2, app.endOf("stuck"))
        // The pass that ended it may still be walking the rows; give it time to reach the others.
        Thread.sleep(300)
        assertNull("a track with no stored point is left open", app.endOf("empty"))
        assertNull("a track started in this process is left open", app.endOf("new"))
        assertEquals("a finished track keeps its end", before + 1, app.endOf("finished"))
    }

    /**
     * The container's own wiring: the sweep it builds asks the container's own watch which track
     * to leave alone. Not through `onCreate`, because the watch cannot be begun between the app
     * object being created and its sweep being launched without a hook in production code; this
     * calls the same use-case instance `onCreate` calls, over the real database.
     */
    @Test
    fun `the container's sweep leaves open the track the container's watch is for`() {
        val app = ApplicationProvider.getApplicationContext<ForagerApplication>()
        Thread.sleep(5) // strictly after the app start, so that start's own sweep cannot touch these rows
        val before = System.currentTimeMillis()
        runBlocking {
            val tracks = app.container.trackRepository
            tracks.create(Track(id = "watched", name = null, startedAtEpochMillis = before, endedAtEpochMillis = null, points = emptyList())).getOrThrow()
            tracks.appendPoints("watched", listOf(point(before + 1))).getOrThrow()
            tracks.create(Track(id = "stuck", name = null, startedAtEpochMillis = before, endedAtEpochMillis = null, points = emptyList())).getOrThrow()
            tracks.appendPoints("stuck", listOf(point(before + 1))).getOrThrow()
        }
        app.container.returnWatch.begin("watched", TrackRecordingMode.HIGH_ACCURACY)
        try {
            // A later process's start: both rows are from "before" it.
            val sweep = runBlocking { app.container.endAbandonedTracksUseCase(before + 1_000L) }.getOrThrow()

            assertEquals(listOf("stuck"), sweep.ended.map { it.trackId })
            assertEquals(before + 1, app.endOf("stuck"))
            assertNull("the watch's track is left open", app.endOf("watched"))
        } finally {
            app.container.returnWatch.end(null)
        }
    }
}
