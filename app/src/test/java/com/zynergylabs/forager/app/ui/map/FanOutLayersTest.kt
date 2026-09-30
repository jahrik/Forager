package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.JournalEntryHighlights
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOffset
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What the fan-out pushes into the map's sources (dispatch 2026-09-28-197): the copies keep their own
 * icon, a journal halo follows a kept record, a dot keeps its selection ring, and every copy has a leg
 * to its true position. These are the pure builders; that the map then draws them, and hides the
 * originals, is device-only (a `MapView` cannot be built under Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FanOutLayersTest {

    private fun member(layerId: String, id: String, lat: Double = 45.0, lng: Double = -122.0) =
        FanMember(FanKey(layerId, id), lat, lng, 0f, 0f, FanOffset(0f, -48f))

    private val movedTo = LatLng(45.001, -122.002)

    private fun frame(members: List<FanMember>, focused: Long? = null) =
        fanFrameCollections(members, { movedTo }, focused)

    @Test
    fun `a copy keeps its own icon and is drawn where it now is`() {
        val f = frame(
            listOf(
                member(MapLayerIds.FINDS, "find"),
                member(MapLayerIds.PHOTOS, "photo"),
                member(MapLayerIds.WAYPOINTS, "waypoint"),
                member(MapLayerIds.PLANNED_TRIPS, "trip"),
            ),
        )
        val byId = f.icons.features().orEmpty().associate { it.getStringProperty("featureId") to it.getStringProperty("image") }
        assertEquals(
            mapOf("find" to "find-mushroom", "photo" to "photo-square", "waypoint" to "waypoint-pin", "trip" to "planned-trip-flag"),
            byId,
        )
        f.icons.features().orEmpty().forEach {
            val at = it.geometry() as Point
            assertEquals(movedTo.lat, at.latitude(), 1e-9)
            assertEquals(movedTo.lng, at.longitude(), 1e-9)
        }
    }

    @Test
    fun `each copy carries the icon-offset that centres its glyph's body on its circle`() {
        val f = frame(
            listOf(
                member(MapLayerIds.FINDS, "find"),
                member(MapLayerIds.PHOTOS, "photo"),
                member(MapLayerIds.WAYPOINTS, "waypoint"),
                member(MapLayerIds.PLANNED_TRIPS, "trip"),
            ),
        )
        val offsets = f.icons.features().orEmpty().associate { feature ->
            val id = feature.getStringProperty("featureId")
            val raw = feature.getProperty(FanOutIds.ICON_OFFSET_PROPERTY)
            assertNotNull("the $id copy carries no ${FanOutIds.ICON_OFFSET_PROPERTY} property", raw)
            val xy = raw.asJsonArray
            id to (xy[0].asFloat to xy[1].asFloat)
        }
        // The find and the flag are drawn 25 dp tall (dispatch 2026-09-28-286), so their offsets are (0, 12.5) and (-7.5893, 12.5).
        val expected = mapOf("find" to (0f to 12.5f), "photo" to (0f to 0f), "waypoint" to (0f to 14f), "trip" to (-7.5893f to 12.5f))
        assertEquals(expected.keys, offsets.keys)
        for ((id, xy) in expected) {
            assertEquals("the $id copy's icon-offset x", xy.first, offsets.getValue(id).first, 1e-3f)
            assertEquals("the $id copy's icon-offset y", xy.second, offsets.getValue(id).second, 1e-3f)
        }
    }

    @Test
    fun `centring a copy moves neither its circle nor its leg`() {
        val f = frame(listOf(member(MapLayerIds.WAYPOINTS, "w", lat = 45.0, lng = -122.0)))
        val circle = f.circles.features().orEmpty().single().geometry() as Point
        assertEquals(movedTo.lat, circle.latitude(), 1e-9)
        assertEquals(movedTo.lng, circle.longitude(), 1e-9)
        val leg = (f.legs.features().orEmpty().single().geometry() as LineString).coordinates()
        assertEquals(listOf(Point.fromLngLat(-122.0, 45.0), Point.fromLngLat(movedTo.lng, movedTo.lat)), leg)
    }

    @Test
    fun `every copy has a leg from its true position to where it is`() {
        val f = frame(listOf(member(MapLayerIds.FINDS, "a", lat = 45.0, lng = -122.0), member(MapLayerIds.PHOTOS, "b", lat = 45.5, lng = -121.5)))
        val legs = f.legs.features().orEmpty().map { (it.geometry() as LineString).coordinates() }
        assertEquals(2, legs.size)
        assertEquals(listOf(Point.fromLngLat(-122.0, 45.0), Point.fromLngLat(movedTo.lng, movedTo.lat)), legs[0])
        assertEquals(listOf(Point.fromLngLat(-121.5, 45.5), Point.fromLngLat(movedTo.lng, movedTo.lat)), legs[1])
    }

    @Test
    fun `a sighting is a dot carrying its observation id, and the focused one is selected`() {
        val f = frame(listOf(member(MapLayerIds.SIGHTINGS, "9001"), member(MapLayerIds.SIGHTINGS, "9002")), focused = 9002L)
        val dots = f.dots.features().orEmpty()
        assertEquals(setOf(9001L, 9002L), dots.map { it.getNumberProperty("observationId").toLong() }.toSet())
        assertEquals(mapOf(9001L to false, 9002L to true), dots.associate { it.getNumberProperty("observationId").toLong() to it.getBooleanProperty("selected") })
        assertTrue("a dot is not an icon", f.icons.features().orEmpty().isEmpty())
    }

    @Test
    fun `a member whose layer has no icon of ours is left out entirely, leg included`() {
        val f = frame(listOf(member(MapLayerIds.KEPT_TRACKS, "track"), member(MapLayerIds.FINDS, "find")))
        assertEquals(1, f.icons.features().orEmpty().size)
        assertEquals("no leg to a marker that is not drawn", 1, f.legs.features().orEmpty().size)
    }

    // Dispatch 2026-09-28-265, item 3: a background circle under every fanned copy, replacing the halo.

    @Test
    fun `every fanned copy, icon or dot, has one circle centred where it now is`() {
        val f = frame(
            listOf(
                member(MapLayerIds.FINDS, "find"),
                member(MapLayerIds.PHOTOS, "photo"),
                member(MapLayerIds.SIGHTINGS, "9001"),
            ),
        )
        val circles = f.circles.features().orEmpty()
        assertEquals("one circle per copy: two icons and a dot", 3, circles.size)
        circles.forEach {
            val at = it.geometry() as Point
            assertEquals(movedTo.lat, at.latitude(), 1e-9)
            assertEquals(movedTo.lng, at.longitude(), 1e-9)
        }
    }

    @Test
    fun `a kept record's copy gets a circle and no halo, since the circle replaces the halo`() {
        val f = frame(listOf(member(MapLayerIds.FINDS, "kept-find")))
        assertEquals(1, f.circles.features().orEmpty().size)
        assertFalse(
            "the frame no longer carries a halo source's features: ${FanFrame::class.java.declaredFields.map { it.name }}",
            FanFrame::class.java.declaredFields.any { it.name == "halos" },
        )
    }

    @Test
    fun `a member with nothing of ours to draw gets no circle either`() {
        val f = frame(listOf(member(MapLayerIds.KEPT_TRACKS, "track"), member(MapLayerIds.FINDS, "find")))
        assertEquals(1, f.circles.features().orEmpty().size)
    }

    @Test
    fun `no members is an empty frame`() {
        val f = frame(emptyList())
        assertTrue(f.legs.features().orEmpty().isEmpty() && f.icons.features().orEmpty().isEmpty() && f.circles.features().orEmpty().isEmpty() && f.dots.features().orEmpty().isEmpty())
    }

    @Test
    fun `the hiding filter names every hidden id, by the property the layer's features carry`() {
        val text = fanOutHiddenFilter("featureId", listOf("a", "b")).toString()
        assertTrue(text, "\"a\"" in text && "\"b\"" in text)
        assertTrue(text, "featureId" in text && "!=" in text && "all" in text)
        val numeric = fanOutHiddenFilter("observationId", listOf(9001L)).toString()
        assertTrue(numeric, "observationId" in numeric && "9001" in numeric && "\"9001\"" !in numeric)
    }

    @Test
    fun `no hidden ids is a filter that shows everything`() {
        val text = fanOutHiddenFilter("featureId", emptyList()).toString()
        assertEquals("[\"all\"]", text)
        assertFalse("!=" in text)
    }
}
