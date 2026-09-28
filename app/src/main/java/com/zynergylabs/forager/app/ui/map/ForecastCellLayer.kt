package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.ForecastBlock
import com.zynergylabs.forager.app.domain.ForecastCell
import com.zynergylabs.forager.app.ui.map.layers.ColourRamp
import com.zynergylabs.forager.app.ui.map.layers.ForecastCellsShown
import kotlin.math.ceil
import kotlin.math.floor
import org.maplibre.android.style.expressions.Expression
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

/**
 * The most 1-degree blocks the map asks the store for at one camera idle (map layers L0b, B5): an
 * explicit operating limit (CLAUDE.md, Architecture), chosen by the L0b coder. The synthetic store gives
 * cells for any block, 100 each, so a continent-wide view would build tens of thousands of polygons on
 * every idle. At 64 blocks (6,400 cells at most) a phone sees cells from zoom 5 or so inward. Past it
 * the map requests nothing and each colour field draws empty, logged when the view crosses the limit.
 * Whether the owner wants a message there is open.
 */
internal const val MAX_FORECAST_BLOCKS = 64

/** Half a cell's side: a cell reaches this far past its centre (0.1-degree cells, edges at x.x5). */
private const val CELL_HALF_DEGREES = 0.05

/**
 * The blocks whose cells can show in the view from ([south], [west]) to ([north], [east]), or `null`
 * when that is more than [MAX_FORECAST_BLOCKS]. A cell is in view when its square overlaps the view, so
 * its centre can be up to half a cell outside it; the centres that can be in view are the tenths from
 * `south - 0.05` to `north + 0.05`, and the blocks are the ones holding those (a cell belongs to the
 * block holding its centre).
 */
internal fun forecastBlocksToRequest(south: Double, west: Double, north: Double, east: Double): Set<ForecastBlock>? {
    fun firstTenth(degrees: Double) = ceil((degrees - CELL_HALF_DEGREES) * 10 - EPSILON) / 10
    fun lastTenth(degrees: Double) = floor((degrees + CELL_HALF_DEGREES) * 10 + EPSILON) / 10
    val blocks = ForecastBlock.touching(
        south = firstTenth(south),
        west = firstTenth(west),
        north = lastTenth(north),
        east = lastTenth(east),
    )
    return blocks.takeIf { it.size <= MAX_FORECAST_BLOCKS }
}

private const val EPSILON = 1e-9

/**
 * A colour field's cells as its source receives them: one 0.1-degree square per **applicable** cell,
 * with its `chance` for the fill colour and its centre as `featureId`. A cell that is not applicable draws
 * nothing (D55 and R5: an unscored cell is left blank), and so does one a file left out.
 */
internal fun forecastCellsFeatureCollection(cells: List<ForecastCell>): FeatureCollection =
    FeatureCollection.fromFeatures(
        cells.filter { it.applicable }.map { cell ->
            val lat = cell.centre.lat
            val lng = cell.centre.lng
            val ring = listOf(
                Point.fromLngLat(lng - CELL_HALF_DEGREES, lat - CELL_HALF_DEGREES),
                Point.fromLngLat(lng + CELL_HALF_DEGREES, lat - CELL_HALF_DEGREES),
                Point.fromLngLat(lng + CELL_HALF_DEGREES, lat + CELL_HALF_DEGREES),
                Point.fromLngLat(lng - CELL_HALF_DEGREES, lat + CELL_HALF_DEGREES),
                Point.fromLngLat(lng - CELL_HALF_DEGREES, lat - CELL_HALF_DEGREES),
            )
            Feature.fromGeometry(Polygon.fromLngLats(listOf(ring))).apply {
                addNumberProperty(CHANCE_PROPERTY, cell.chance)
                addStringProperty(FEATURE_ID_PROPERTY, "${cell.centre.lat},${cell.centre.lng}")
            }
        },
    )

/** The property a cell's fill colour reads. */
internal const val CHANCE_PROPERTY = "chance"

/**
 * A colour field's `fill-color`: its ramp interpolated linearly on each cell's [CHANCE_PROPERTY]. A plain
 * value built without a native call ([Expression] has none, per this package's other expression
 * builders), so a headless test compares it directly.
 */
internal fun colourFieldFillColour(ramp: ColourRamp): Expression =
    Expression.interpolate(
        Expression.linear(),
        Expression.get(CHANCE_PROPERTY),
        *ramp.stops.map { Expression.stop(it.chance, Expression.color(it.argb)) }.toTypedArray(),
    )

/**
 * What the legend dates a colour field by, from the cells drawn in view: their week, and the earliest
 * `weather_through` among them, so the legend never claims fresher weather than a cell on screen has.
 * Cells that draw nothing do not count. `null` when none is drawn.
 */
internal fun forecastCellsShownOf(cells: List<ForecastCell>): ForecastCellsShown? {
    val drawn = cells.filter { it.applicable }
    if (drawn.isEmpty()) return null
    return ForecastCellsShown(week = drawn.first().week, weatherThrough = drawn.minOf { it.weatherThrough })
}
