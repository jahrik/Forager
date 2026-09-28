package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.ForecastBlock
import com.zynergylabs.forager.app.domain.ForecastCell
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.layers.ColourRamp
import com.zynergylabs.forager.app.ui.map.layers.ForecastCellsShown
import com.zynergylabs.forager.app.ui.map.layers.RampStop
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.maplibre.android.style.expressions.Expression
import org.maplibre.geojson.Polygon

/**
 * What the map's colour-field sources receive and how their fills are coloured (map layers L0b, B5 and
 * B6), up to the native boundary `SightingsMapOverlayDataTest` describes: the pure GeoJSON and
 * `Expression` values are built and read here; `SightingsMap` only hands them to MapLibre.
 */
class ForecastCellLayerTest {

    private val week = LocalDate.of(2026, 9, 28)

    private fun cell(lat: Double, lng: Double, chance: Double, applicable: Boolean = true, weatherThrough: LocalDate = LocalDate.of(2026, 9, 26)) =
        ForecastCell(
            group = "chanterelles", week = week, centre = LatLng(lat, lng), chance = chance,
            uncertaintyLow = 0.0, uncertaintyHigh = 1.0, applicable = applicable, drivers = emptyList(),
            weatherThrough = weatherThrough, modelVersion = "synthetic-1",
        )

    @Test
    fun `only applicable cells become features, each a 0_1-degree square around its centre carrying its chance`() {
        val collection = forecastCellsFeatureCollection(listOf(cell(45.1, -122.3, 0.3), cell(45.2, -122.3, 0.9, applicable = false)))

        val features = collection.features().orEmpty()
        assertEquals("a cell that is not applicable draws nothing", 1, features.size)
        val ring = (features.single().geometry() as Polygon).coordinates().single().map { it.longitude() to it.latitude() }
        val expected = listOf(-122.35 to 45.05, -122.25 to 45.05, -122.25 to 45.15, -122.35 to 45.15, -122.35 to 45.05)
        assertEquals(expected.size, ring.size)
        ring.zip(expected).forEach { (actual, want) ->
            assertEquals(want.first, actual.first, 1e-9)
            assertEquals(want.second, actual.second, 1e-9)
        }
        assertEquals(0.3, features.single().getNumberProperty("chance").toDouble(), 1e-9)
    }

    @Test
    fun `the fill colour interpolates the ramp linearly on each cell's chance`() {
        val ramp = ColourRamp(listOf(RampStop(0f, 0xFFFFD54F.toInt()), RampStop(1f, 0xFF8D2B00.toInt())))

        assertEquals(
            Expression.interpolate(
                Expression.linear(),
                Expression.get("chance"),
                Expression.stop(0f, Expression.color(0xFFFFD54F.toInt())),
                Expression.stop(1f, Expression.color(0xFF8D2B00.toInt())),
            ),
            colourFieldFillColour(ramp),
        )
    }

    @Test
    fun `a view asks for the blocks its cells can reach, and for nothing past the operating limit`() {
        // Cells reach 0.05 degree past their centres, so a view whose south edge is 45.02 still shows
        // the cells centred on 45.0 (block 45) but none centred on 44.9 (block 44).
        assertEquals(setOf(ForecastBlock(45, -123)), forecastBlocksToRequest(south = 45.02, west = -122.98, north = 45.5, east = -122.3))
        // A north edge of 45.96 shows the cells centred on 46.0, which are in block 46.
        assertEquals(setOf(ForecastBlock(45, -123), ForecastBlock(46, -123)), forecastBlocksToRequest(south = 45.02, west = -122.98, north = 45.96, east = -122.3))
        assertNull("a 10-degree view touches 121 blocks, over the limit of $MAX_FORECAST_BLOCKS", forecastBlocksToRequest(south = 40.0, west = -125.0, north = 50.0, east = -115.0))
    }

    @Test
    fun `the dates in view are the cells' week and the earliest weather date among them`() {
        val shown = forecastCellsShownOf(
            listOf(
                cell(45.1, -122.3, 0.3, weatherThrough = LocalDate.of(2026, 9, 26)),
                cell(45.2, -122.3, 0.4, weatherThrough = LocalDate.of(2026, 9, 25)),
                cell(45.3, -122.3, 0.5, applicable = false, weatherThrough = LocalDate.of(2026, 9, 20)),
            ),
        )

        assertEquals("a cell that draws nothing does not date the legend", ForecastCellsShown(week, LocalDate.of(2026, 9, 25)), shown)
        assertNull(forecastCellsShownOf(emptyList()))
    }
}
