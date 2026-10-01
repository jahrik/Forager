package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOffset
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What counts as "a fan's copies are drawn" (dispatch 2026-09-28-369, amendment -371): the pure matching only. That the map then
 * reports the right frames to it, and that the listener is removed afterwards, is device-only (a `MapView` cannot be built under
 * Robolectric), so the instrumented run's frame counts and the recordings are the evidence for that part.
 */
class FanOutRenderSignalTest {

    private fun member(layer: String, id: String) = FanMember(FanKey(layer, id), 45.0, -122.0, 0f, 0f, FanOffset(0f, -48f))

    private val findImage = markerIconForLayer(MapLayerIds.FINDS)!!.imageId
    private val photoImage = markerIconForLayer(MapLayerIds.PHOTOS)!!.imageId

    @Test
    fun `an icon copy is expected by its image and feature id`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.FINDS, "7")))

        assertEquals(setOf("$findImage:7"), expected.iconIds)
        assertTrue(expected.dotIds.isEmpty())
    }

    @Test
    fun `two markers with the same feature id on different layers are two copies`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.FINDS, "7"), member(MapLayerIds.PHOTOS, "7")))

        assertEquals(setOf("$findImage:7", "$photoImage:7"), expected.iconIds)
    }

    @Test
    fun `a sighting is expected as a dot by its observation id, and one without a number is only counted`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.SIGHTINGS, "42"), member(MapLayerIds.SIGHTINGS, "not-a-number")))

        assertEquals(setOf("42"), expected.dotIds)
        assertEquals(1, expected.unnumberedDots)
        assertTrue(expected.iconIds.isEmpty())
    }

    @Test
    fun `nothing is drawn until every expected copy has been reported`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.FINDS, "7"), member(MapLayerIds.PHOTOS, "8"), member(MapLayerIds.SIGHTINGS, "42")))

        assertFalse("one icon missing", copiesDrawn(expected, setOf("$findImage:7"), setOf("42"), 0))
        assertFalse("the dot missing", copiesDrawn(expected, setOf("$findImage:7", "$photoImage:8"), emptySet(), 0))
        assertTrue(copiesDrawn(expected, setOf("$findImage:7", "$photoImage:8"), setOf("42"), 0))
    }

    @Test
    fun `an unrelated rendered copy does not stand in for a missing one`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.FINDS, "7"), member(MapLayerIds.FINDS, "8")))

        assertFalse(copiesDrawn(expected, setOf("$findImage:7", "$findImage:99"), emptySet(), 0))
    }

    @Test
    fun `a fan with no copies is drawn at once`() {
        assertTrue(expectedCopies(emptyList()).isEmpty)
        assertTrue(copiesDrawn(expectedCopies(emptyList()), emptySet(), emptySet(), 0))
    }

    @Test
    fun `unnumbered dots are counted against what the renderer reports`() {
        val expected = expectedCopies(listOf(member(MapLayerIds.SIGHTINGS, "x"), member(MapLayerIds.SIGHTINGS, "y")))

        assertFalse(copiesDrawn(expected, emptySet(), emptySet(), 1))
        assertTrue(copiesDrawn(expected, emptySet(), emptySet(), 2))
    }

    // --- the fold's end: the originals that must be drawn again before the copies are cleared

    @Test
    fun `the originals of a fan are expected per layer by their feature ids`() {
        val expected = expectedOriginals(listOf(member(MapLayerIds.FINDS, "7"), member(MapLayerIds.FINDS, "8"), member(MapLayerIds.PHOTOS, "7")))

        assertEquals(mapOf(MapLayerIds.FINDS to setOf("7", "8"), MapLayerIds.PHOTOS to setOf("7")), expected)
    }

    @Test
    fun `a sighting's original is expected by its numeric observation id and one without is left out`() {
        val expected = expectedOriginals(listOf(member(MapLayerIds.SIGHTINGS, "42"), member(MapLayerIds.SIGHTINGS, "x")))

        assertEquals(mapOf(MapLayerIds.SIGHTINGS to setOf("42")), expected)
    }

    @Test
    fun `the copies are not cleared until every original is reported on its own layer`() {
        val expected = expectedOriginals(listOf(member(MapLayerIds.FINDS, "7"), member(MapLayerIds.PHOTOS, "7")))

        assertFalse("the photo is missing", originalsDrawn(expected, mapOf(MapLayerIds.FINDS to setOf("7"))))
        assertFalse("a find with the same id on the wrong layer does not stand in", originalsDrawn(expected, mapOf(MapLayerIds.FINDS to setOf("7"), MapLayerIds.PHOTOS to emptySet())))
        assertTrue(originalsDrawn(expected, mapOf(MapLayerIds.FINDS to setOf("7"), MapLayerIds.PHOTOS to setOf("7", "9"))))
    }

    @Test
    fun `nothing to reveal is drawn at once`() {
        assertTrue(originalsDrawn(expectedOriginals(emptyList()), emptyMap()))
    }
}
