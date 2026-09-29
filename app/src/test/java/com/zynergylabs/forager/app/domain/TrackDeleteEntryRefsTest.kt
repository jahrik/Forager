package com.zynergylabs.forager.app.domain

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.data.local.ForagerDatabase
import com.zynergylabs.forager.app.data.repository.RoomCartographyEntryRepository
import com.zynergylabs.forager.app.data.repository.RoomMushroomLogRepository
import com.zynergylabs.forager.app.data.repository.RoomTrackRepository
import com.zynergylabs.forager.app.data.repository.RoomWaypointRepository
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.RecordPolyline
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackDecision
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDecision
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Part 2 follow-ups F1 item 5, point 5 of continuation `2026-09-28-191`: a Journal entry that kept a track
 * follows the rule for an entry that kept a since-deleted waypoint.
 *
 * **The rule, found in code (read at `addf7d7a`):** a delete removes the record and nothing else. Neither
 * `RoomWaypointRepository.delete` (`dao.deleteById`) nor `RoomTrackRepository.delete`
 * (`dao.deleteTrackAndPoints`) touches `cartography_entry_*_refs`, and those tables have no `@ForeignKey` "by
 * explicit standing rule" (`CartographyEntryEntity.kt`, the ref entities' doc comments): the ref row survives
 * the delete with its snapshot, and the entry shows the snapshot. So a kept track stays in the entry as its
 * snapshot (name, distance, duration, point count), and its map line is not drawn, since the points went with the
 * track (`GetCartographyEntryMapDataUseCaseTest`, "a kept track deleted from Records draws nothing and does not
 * error"). The two ref tables differ in what the snapshot holds (a waypoint's carries its coordinates, so it can
 * still be drawn; a track's carries no path), but the rule, the ref row surviving untouched, is the same.
 *
 * Real Room, real repositories, the real [DeleteTrackUseCase] and [DeleteWaypointUseCase], and a reload of the
 * entry from the database after the delete: the ref rows' presence is read from the rows, not assumed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TrackDeleteEntryRefsTest {

    private lateinit var database: ForagerDatabase
    private lateinit var trackRepository: RoomTrackRepository
    private lateinit var waypointRepository: RoomWaypointRepository
    private lateinit var entryRepository: RoomCartographyEntryRepository
    private lateinit var mapData: GetCartographyEntryMapDataUseCase

    private val track = Track(
        id = "track-1",
        name = "Ridge Loop",
        startedAtEpochMillis = 1_000L,
        endedAtEpochMillis = 2_000L,
        points = listOf(TrackPoint(lat = 45.20, lng = -122.50, altitude = null, accuracyMeters = null, timestampEpochMillis = 1_000L)),
    )
    private val waypoint = Waypoint(id = "wp-1", lat = 45.21, lng = -122.51, altitude = null, name = "Creek pin", note = "", createdAtEpochMillis = 1_500L)
    private val trackDecision = TrackDecision(trackId = "track-1", name = "Ridge Loop", distanceMeters = 1200.0, durationMillis = 600_000L, pointCount = 42, kept = true)
    private val waypointDecision = WaypointDecision(waypointId = "wp-1", name = "Creek pin", lat = 45.21, lng = -122.51, kept = true)
    private val entry = CartographyEntry.draft(id = "entry-1", date = LocalDate.of(2026, 8, 1), updatedAtEpochMillis = 1_000L)
        .copy(isDraft = false, trackDecisions = listOf(trackDecision), waypointDecisions = listOf(waypointDecision))

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Application>(), ForagerDatabase::class.java).build()
        trackRepository = RoomTrackRepository(database.trackDao())
        waypointRepository = RoomWaypointRepository(database.waypointDao())
        entryRepository = RoomCartographyEntryRepository(database.cartographyEntryDao())
        mapData = GetCartographyEntryMapDataUseCase(trackRepository, RoomMushroomLogRepository(database.mushroomLogDao()))
        trackRepository.create(track).getOrThrow()
        trackRepository.appendPoints(track.id, track.points).getOrThrow()
        waypointRepository.save(waypoint).getOrThrow()
        entryRepository.save(entry).getOrThrow()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `before the delete the entry has the kept track drawn and both counts at one`() = runTest {
        val loaded = entryRepository.getById("entry-1").getOrThrow()!!

        assertEquals(listOf(trackDecision), loaded.trackDecisions)
        assertEquals(listOf(RecordPolyline("track-1", listOf(LatLng(45.20, -122.50)))), mapData(loaded, galleryPhotos = emptyList()).trackPolylines)
        assertEquals(1, entryRepository.countEntriesReferencingTrack("track-1").getOrThrow())
        assertEquals(1, entryRepository.countEntriesReferencingWaypoint("wp-1").getOrThrow())
    }

    @Test
    fun `after the track is deleted the entry still keeps it as its snapshot, and draws no line`() = runTest {
        DeleteTrackUseCase(trackRepository, waypointRepository)("track-1").getOrThrow()

        val loaded = entryRepository.getById("entry-1").getOrThrow()!!

        assertEquals("the ref row and its snapshot survive the delete, unchanged", listOf(trackDecision), loaded.trackDecisions)
        assertEquals("the track itself is gone", null, trackRepository.getById("track-1").getOrThrow())
        assertEquals("its line is not drawn, and nothing fails", emptyList<RecordPolyline>(), mapData(loaded, galleryPhotos = emptyList()).trackPolylines)
    }

    @Test
    fun `the same holds for a waypoint, which is the rule the track follows`() = runTest {
        DeleteWaypointUseCase(waypointRepository)("wp-1").getOrThrow()

        val loaded = entryRepository.getById("entry-1").getOrThrow()!!

        assertEquals("the waypoint's ref row and snapshot survive its delete, unchanged", listOf(waypointDecision), loaded.waypointDecisions)
        assertEquals(null, waypointRepository.getById("wp-1").getOrThrow())
    }

    @Test
    fun `deleting the track leaves the entry's waypoint decision alone too`() = runTest {
        DeleteTrackUseCase(trackRepository, waypointRepository)("track-1").getOrThrow()

        val loaded = entryRepository.getById("entry-1").getOrThrow()!!

        assertEquals(listOf(waypointDecision), loaded.waypointDecisions)
    }
}
