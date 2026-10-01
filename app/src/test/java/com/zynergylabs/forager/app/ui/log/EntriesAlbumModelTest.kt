package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.LogPhoto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** [groupAlbumByDay], headless: day order, order within a day, the unknown-date group, and the zone. */
class EntriesAlbumModelTest {

    private val zone: ZoneId = ZoneId.of("America/Los_Angeles")

    private fun photo(id: String, at: LocalDateTime?): GalleryPhoto = GalleryPhoto(
        photo = LogPhoto(id = id, relativePath = "photos/$id.jpg", createdAtEpochMillis = at?.atZone(zone)?.toInstant()?.toEpochMilli()),
        referencingEntryIds = emptyList(),
    )

    @Test
    fun `days come newest first, photos keep input order within a day, and unknown dates come last`() {
        val photos = listOf(
            photo("old-1", LocalDateTime.of(2026, 9, 20, 9, 0)),
            photo("new-1", LocalDateTime.of(2026, 9, 26, 8, 0)),
            photo("unknown-1", null),
            photo("new-2", LocalDateTime.of(2026, 9, 26, 7, 0)),
            photo("unknown-2", null),
            photo("old-2", LocalDateTime.of(2026, 9, 20, 18, 0)),
        )

        val days = groupAlbumByDay(photos, zone)

        assertEquals(listOf(LocalDate.of(2026, 9, 26), LocalDate.of(2026, 9, 20), null), days.map { it.date })
        assertEquals(
            listOf(listOf("new-1", "new-2"), listOf("old-1", "old-2"), listOf("unknown-1", "unknown-2")),
            days.map { day -> day.photos.map { it.photo.id } },
        )
    }

    @Test
    fun `a photo falls on its device-local day, not its UTC day`() {
        // 20:00 and 21:00 in Los Angeles on the 26th are already the 27th in UTC.
        val photos = listOf(
            photo("evening", LocalDateTime.of(2026, 9, 26, 20, 0)),
            photo("later", LocalDateTime.of(2026, 9, 26, 21, 0)),
        )

        val days = groupAlbumByDay(photos, zone)

        assertEquals(listOf(LocalDate.of(2026, 9, 26)), days.map { it.date })
        assertEquals(listOf("evening", "later"), days.single().photos.map { it.photo.id })
    }

    @Test
    fun `no photos means no days, and no unknown group when every date is known`() {
        assertEquals(emptyList<AlbumDay>(), groupAlbumByDay(emptyList(), zone))
        assertEquals(
            listOf(LocalDate.of(2026, 9, 26)),
            groupAlbumByDay(listOf(photo("a", LocalDateTime.of(2026, 9, 26, 12, 0))), zone).map { it.date },
        )
    }
}
