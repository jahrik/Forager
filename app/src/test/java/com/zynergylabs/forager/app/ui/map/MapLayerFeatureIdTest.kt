package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.RecordPolyline
import com.zynergylabs.forager.app.domain.model.RecordRegion
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.layers.LayerRenderer
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.PaletteRole
import com.zynergylabs.forager.app.ui.map.layers.TapHit
import com.zynergylabs.forager.app.ui.theme.MapPalette
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

/**
 * Map layers L0a (A4, and the owner's ruling 2 on `prompts/preserved/2026-09-27-30.md`): every
 * tappable overlay feature carries a stable id in [FEATURE_ID_PROPERTY], written by the pure
 * builders `SightingsMap` pushes into its sources; a tap reads it back through [tapHitOf]. Also
 * checks that the registry describes the layers `SightingsMap` actually builds (source and palette
 * role), since the registry now decides the order they are built in.
 *
 * Headless for the same reason as [SightingsMapOverlayDataTest]: the builders return pure GeoJSON.
 * What cannot be shown here is that MapLibre's `queryRenderedFeatures` hands the property back on
 * a device; the sighting's `observationId` property has been seen to (the bubble works on
 * hardware), and the new property uses the same mechanism. A device item.
 */
class MapLayerFeatureIdTest {

    private fun idsOf(features: List<Feature>) = features.map { it.getStringProperty(FEATURE_ID_PROPERTY) }

    @Test
    fun `each planned trip's feature carries the trip's own id`() {
        val trips = listOf(
            PlannedTrip(id = "trip-1", name = "One", location = LatLng(45.0, -122.0), date = LocalDate.of(2026, 9, 1)),
            PlannedTrip(id = "trip-2", name = "Two", location = LatLng(45.1, -122.1), date = LocalDate.of(2026, 9, 2)),
        )
        assertEquals(listOf("trip-1", "trip-2"), idsOf(plannedTripsFeatureCollection(trips).features()!!))
    }

    @Test
    fun `each waypoint's feature carries the waypoint's own id`() {
        val waypoints = listOf(
            Waypoint(id = "wp-a", lat = 45.0, lng = -122.0, altitude = null, name = "A", note = "", createdAtEpochMillis = 0L),
            Waypoint(id = "wp-b", lat = 45.1, lng = -122.1, altitude = null, name = "B", note = "", createdAtEpochMillis = 0L),
        )
        assertEquals(listOf("wp-a", "wp-b"), idsOf(waypointsFeatureCollection(waypoints).features()!!))
    }

    @Test
    fun `the search centre and the breadcrumb carry fixed ids`() {
        val centre = searchCenterFeatureCollection(Region(lat = 45.0, lng = -122.0, radiusKm = 5)).features()!!
        assertEquals(listOf(SEARCH_CENTRE_FEATURE_ID), idsOf(centre))
        val trail = breadcrumbFeatureCollection(listOf(LatLng(45.0, -122.0), LatLng(45.1, -122.1))).features()!!
        assertEquals(listOf(BREADCRUMB_FEATURE_ID), idsOf(trail))
        assertEquals("search-centre", SEARCH_CENTRE_FEATURE_ID)
        assertEquals("breadcrumb", BREADCRUMB_FEATURE_ID)
    }

    @Test
    fun `each kept track's line carries its track id, and a dropped short track shifts no other id`() {
        val tracks = listOf(
            RecordPolyline("track-short", listOf(LatLng(45.0, -122.0))),
            RecordPolyline("track-a", listOf(LatLng(45.0, -122.0), LatLng(45.1, -122.1))),
            RecordPolyline("track-b", listOf(LatLng(46.0, -123.0), LatLng(46.1, -123.1))),
        )
        assertEquals(listOf("track-a", "track-b"), idsOf(keptTracksFeatureCollection(tracks).features()!!))
    }

    @Test
    fun `each find and photo marker carries its record id`() {
        val points = listOf(RecordPoint("find-1", LatLng(45.0, -122.0)), RecordPoint("photo-2", LatLng(45.1, -122.1)))
        assertEquals(listOf("find-1", "photo-2"), idsOf(pointsFeatureCollection(points).features()!!))
    }

    @Test
    fun `each offline region circle carries its region id`() {
        val regions = listOf(
            RecordRegion("11", Region(lat = 45.0, lng = -122.0, radiusKm = 5)),
            RecordRegion("12", Region(lat = 46.0, lng = -123.0, radiusKm = 5)),
        )
        assertEquals(listOf("11", "12"), idsOf(offlineRegionCirclesFeatureCollection(regions).features()!!))
    }

    @Test
    fun `a sighting's feature keeps its observationId and gains no second id`() {
        val sighting = Sighting(
            observationId = 42L,
            taxonId = 1L,
            scientificName = "Cantharellus formosus",
            commonName = null,
            lat = 45.0,
            lng = -122.0,
            observedOn = null,
            photoUrl = null,
        )
        val feature = sightingsFeatureCollection(listOf(sighting)).features()!!.single()
        assertEquals(42L, feature.getNumberProperty("observationId").toLong())
        assertFalse(feature.hasProperty(FEATURE_ID_PROPERTY))
    }

    // tapHitOf: what a tap reads back from a feature.

    @Test
    fun `a tapped sighting's id is its observationId in decimal`() {
        val sighting = Sighting(
            observationId = 7L,
            taxonId = 1L,
            scientificName = "Morchella americana",
            commonName = null,
            lat = 45.0,
            lng = -122.0,
            observedOn = null,
            photoUrl = null,
        )
        val feature = sightingsFeatureCollection(listOf(sighting)).features()!!.single()
        assertEquals(TapHit(MapLayerIds.SIGHTINGS, "7"), tapHitOf(MapLayerIds.SIGHTINGS, feature))
    }

    @Test
    fun `a tapped waypoint's id is the one its builder wrote`() {
        val waypoint = Waypoint(id = "wp-z", lat = 45.0, lng = -122.0, altitude = null, name = "Z", note = "", createdAtEpochMillis = 0L)
        val feature = waypointsFeatureCollection(listOf(waypoint)).features()!!.single()
        assertEquals(TapHit(MapLayerIds.WAYPOINTS, "wp-z"), tapHitOf(MapLayerIds.WAYPOINTS, feature))
    }

    @Test
    fun `a tapped feature with no id reads as a hit with no id, not a made-up one`() {
        val bare = Feature.fromGeometry(Point.fromLngLat(-122.0, 45.0))
        assertEquals(TapHit(MapLayerIds.FINDS, null), tapHitOf(MapLayerIds.FINDS, bare))
        assertEquals(TapHit(MapLayerIds.SIGHTINGS, null), tapHitOf(MapLayerIds.SIGHTINGS, bare))
    }

    // The registry describes what SightingsMap builds.

    @Test
    fun `every line layer in the registry is a line the map builds, on the same source, in the role's colour`() {
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.LINE }.forEach { spec ->
            val line = lineSpecForLayer(spec.id)
            assertNotNull("no line built for ${spec.id}", line)
            assertEquals(spec.id, spec.sourceId, line!!.sourceId)
            listOf(MapPalette.DAY, MapPalette.NIGHT).forEach { palette ->
                assertEquals(spec.id, line.colour(palette), spec.paletteRole!!.colourOf(palette))
            }
        }
    }

    @Test
    fun `every symbol layer in the registry is a bitmap marker in the role's colour`() {
        MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.SYMBOL }.forEach { spec ->
            val icon = markerIconForLayer(spec.id)
            assertNotNull("no marker icon for ${spec.id}", icon)
            listOf(MapPalette.DAY, MapPalette.NIGHT).forEach { palette ->
                assertEquals(spec.id, icon!!.colour(palette), spec.paletteRole!!.colourOf(palette))
            }
        }
    }

    @Test
    fun `the circle and fill layers are the sighting dot and the offline region in their own roles`() {
        assertEquals(
            listOf(MapLayerIds.SIGHTINGS to PaletteRole.SIGHTING_DOT),
            MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.CIRCLE }.map { it.id to it.paletteRole },
        )
        assertEquals(
            listOf(MapLayerIds.OFFLINE_REGION_FILL to PaletteRole.OFFLINE_REGION),
            MAP_LAYER_REGISTRY.filter { it.renderer == LayerRenderer.FILL }.map { it.id to it.paletteRole },
        )
    }
}
