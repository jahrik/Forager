package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.model.TrackPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Journal redesign J3, C3: [projectTrackToBox], the track thumbnail's projection, tested headless
 * (plain JVM, no Compose, no Robolectric): points in, box coordinates out.
 */
class TrackThumbnailProjectionTest {

    private fun pt(lat: Double, lng: Double) = TrackPoint(lat = lat, lng = lng, altitude = null, accuracyMeters = null, timestampEpochMillis = 0L)

    private fun assertPoints(expected: List<Pair<Float, Float>>, actual: List<ThumbnailPoint>, tolerance: Float = 0.01f) {
        assertEquals("point count", expected.size, actual.size)
        expected.zip(actual).forEachIndexed { i, (e, a) ->
            assertEquals("x of point $i", e.first, a.x, tolerance)
            assertEquals("y of point $i", e.second, a.y, tolerance)
        }
    }

    @Test
    fun `two points on a diagonal at the equator fill a square box corner to corner, north up`() {
        val out = projectTrackToBox(listOf(pt(0.0, 0.0), pt(0.01, 0.01)), width = 100f, height = 100f)

        // South-west point at bottom-left, north-east at top-right. cos(0.005 deg) differs from 1 by
        // under 4e-9, far inside the tolerance.
        assertPoints(listOf(0f to 100f, 100f to 0f), out)
    }

    @Test
    fun `the inset keeps every point that far in from the edges`() {
        val out = projectTrackToBox(listOf(pt(0.0, 0.0), pt(0.01, 0.01)), width = 100f, height = 100f, inset = 10f)

        assertPoints(listOf(10f to 90f, 90f to 10f), out)
    }

    @Test
    fun `one scale for both axes keeps a tall track tall, centred across the box`() {
        // Twice as tall as wide: scale by height, and sit centred horizontally.
        val out = projectTrackToBox(listOf(pt(0.0, 0.0), pt(0.02, 0.01)), width = 100f, height = 100f)

        assertPoints(listOf(25f to 100f, 75f to 0f), out, tolerance = 0.05f)
    }

    @Test
    fun `longitude is scaled by the cosine of the middle latitude`() {
        // At 60 N a degree of longitude is half a degree of latitude, so 0.02 deg of longitude by
        // 0.01 deg of latitude is square and fills the box. Without the cosine it would be twice as
        // wide as tall and fill only half the height.
        val out = projectTrackToBox(listOf(pt(60.0, 0.0), pt(60.01, 0.02)), width = 100f, height = 100f)

        assertPoints(listOf(0f to 100f, 100f to 0f), out, tolerance = 0.2f)
    }

    @Test
    fun `a track is projected in order, every point`() {
        val out = projectTrackToBox(listOf(pt(0.0, 0.0), pt(0.01, 0.0), pt(0.01, 0.01), pt(0.0, 0.01)), width = 100f, height = 100f)

        assertPoints(listOf(0f to 100f, 0f to 0f, 100f to 0f, 100f to 100f), out)
    }

    @Test
    fun `fewer than two points project to nothing`() {
        assertEquals(emptyList<ThumbnailPoint>(), projectTrackToBox(listOf(pt(45.0, -122.0)), width = 100f, height = 100f))
        assertEquals(emptyList<ThumbnailPoint>(), projectTrackToBox(emptyList(), width = 100f, height = 100f))
    }

    @Test
    fun `a track with a zero-width bounding box sits on the box's vertical centre line`() {
        // Every point on one meridian: no width to scale, so x is the centre; y still spans the box.
        val out = projectTrackToBox(listOf(pt(0.0, 5.0), pt(0.005, 5.0), pt(0.01, 5.0)), width = 100f, height = 100f)

        assertPoints(listOf(50f to 100f, 50f to 50f, 50f to 0f), out)
    }

    @Test
    fun `a track at one spot repeated sits at the box's centre`() {
        val out = projectTrackToBox(listOf(pt(45.0, -122.0), pt(45.0, -122.0), pt(45.0, -122.0)), width = 100f, height = 60f)

        assertPoints(listOf(50f to 30f, 50f to 30f, 50f to 30f), out)
    }

    @Test
    fun `a zero-width target box gives finite points on its centre line`() {
        val out = projectTrackToBox(listOf(pt(0.0, 0.0), pt(0.01, 0.01)), width = 0f, height = 100f, inset = 2f)

        assertEquals(2, out.size)
        out.forEach { p -> assertTrue("finite: $p", p.x.isFinite() && p.y.isFinite()) }
        assertPoints(listOf(0f to 50f, 0f to 50f), out)
    }
}
