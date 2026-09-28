package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.TRACK_WIDTH_ZOOM_STOPS
import com.zynergylabs.forager.app.ui.map.layers.ZoomWidthStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.android.style.expressions.Expression

/**
 * Track widths by zoom (owner, 2026-09-28: "Can we have the track lines thin out as we zoom out?").
 * The stops are the planner's proposal in dispatch 2026-09-28-34: today's width at zoom 15 and above,
 * 40% of it at zoom 11 and below, linear in between, with each casing keeping today's ratio to its
 * line. The expected numbers below are that proposal's arithmetic on today's widths (a 6 dp line, a
 * 9 dp casing), written out, not read back from the stop data.
 */
class TrackWidthByZoomTest {

    private val specs = trackLayerSpecs().associateBy { it.layerId }
    private val breadcrumb = specs.getValue(MapLayerIds.BREADCRUMB)
    private val breadcrumbCasing = specs.getValue(MapLayerIds.BREADCRUMB_CASING)
    private val keptTrack = specs.getValue(MapLayerIds.KEPT_TRACKS)
    private val keptTrackCasing = specs.getValue(MapLayerIds.KEPT_TRACKS_CASING)

    @Test
    fun `the stops are 40 percent of the full width at zoom 11 and the full width at zoom 15`() {
        assertEquals(listOf(ZoomWidthStop(11f, 0.4f), ZoomWidthStop(15f, 1f)), TRACK_WIDTH_ZOOM_STOPS)
    }

    @Test
    fun `each track line is 2,4 dp at zoom 11 and 6 dp at zoom 15, and each casing 3,6 dp and 9 dp`() {
        for ((name, spec, low, full) in listOf(
            Quad("breadcrumb", breadcrumb, 2.4f, 6f),
            Quad("breadcrumb casing", breadcrumbCasing, 3.6f, 9f),
            Quad("kept track", keptTrack, 2.4f, 6f),
            Quad("kept-track casing", keptTrackCasing, 3.6f, 9f),
        )) {
            val stops = lineWidthStops(spec)
            assertNotNull("$name has zoom stops", stops)
            assertEquals("$name has two stops", 2, stops!!.size)
            assertEquals("$name first stop's zoom", 11f, stops[0].first, 0f)
            assertEquals("$name width at zoom 11", low, stops[0].second, 1e-4f)
            assertEquals("$name last stop's zoom", 15f, stops[1].first, 0f)
            assertEquals("$name width at zoom 15", full, stops[1].second, 1e-4f)
        }
    }

    @Test
    fun `below zoom 11 and above zoom 15 the width holds, and in between it is linear`() {
        for ((name, spec, low, full) in listOf(
            Quad("breadcrumb", breadcrumb, 2.4f, 6f),
            Quad("kept-track casing", keptTrackCasing, 3.6f, 9f),
        )) {
            assertEquals("$name at zoom 5", low, lineWidthAtZoom(spec, 5f), 1e-4f)
            assertEquals("$name at zoom 11", low, lineWidthAtZoom(spec, 11f), 1e-4f)
            assertEquals("$name at zoom 13, halfway", (low + full) / 2, lineWidthAtZoom(spec, 13f), 1e-4f)
            assertEquals("$name at zoom 14", low + (full - low) * 0.75f, lineWidthAtZoom(spec, 14f), 1e-4f)
            assertEquals("$name at zoom 15", full, lineWidthAtZoom(spec, 15f), 1e-4f)
            assertEquals("$name at zoom 20", full, lineWidthAtZoom(spec, 20f), 1e-4f)
        }
    }

    @Test
    fun `each casing keeps today's 1,5 ratio to its line at every zoom`() {
        for ((line, casing) in listOf(breadcrumb to breadcrumbCasing, keptTrack to keptTrackCasing)) {
            var zoom = 8f
            while (zoom <= 18f) {
                assertEquals("${casing.layerId} over ${line.layerId} at zoom $zoom", 1.5f, lineWidthAtZoom(casing, zoom) / lineWidthAtZoom(line, zoom), 1e-4f)
                zoom += 0.5f
            }
        }
        assertNotEquals("the widths do change with zoom", lineWidthAtZoom(keptTrack, 11f), lineWidthAtZoom(keptTrack, 15f))
    }

    @Test
    fun `a track's line-width is a linear zoom interpolation from its stops`() {
        val expected = Expression.interpolate(
            Expression.linear(),
            Expression.zoom(),
            Expression.stop(11f, 2.4f),
            Expression.stop(15f, 6f),
        )
        assertEquals(expected, lineWidthExpression(keptTrack))
        assertEquals(expected, lineWidthExpression(breadcrumb))
    }

    @Test
    fun `the offline outline stays a constant 1,5 dp while the tracks thin out`() {
        val outline = offlineRegionOutlineSpec()
        assertNotNull("the kept track thins out", lineWidthStops(keptTrack))
        assertNull("the offline outline has no stops", lineWidthStops(outline))
        assertEquals(Expression.literal(1.5f), lineWidthExpression(outline))
        assertEquals(1.5f, lineWidthAtZoom(outline, 11f), 0f)
        assertEquals(1.5f, lineWidthAtZoom(outline, 15f), 0f)
    }

    private data class Quad(val name: String, val spec: LineLayerSpec, val low: Float, val full: Float)
}
