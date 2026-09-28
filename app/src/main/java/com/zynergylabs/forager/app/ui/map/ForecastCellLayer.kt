package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.ForecastBlock
import com.zynergylabs.forager.app.domain.ForecastCell
import com.zynergylabs.forager.app.ui.map.layers.ColourRamp
import com.zynergylabs.forager.app.ui.map.layers.ForecastCellsShown
import org.maplibre.android.style.expressions.Expression
import org.maplibre.geojson.FeatureCollection

/** Tests-first stub (map layers L0b, B5). */
internal const val MAX_FORECAST_BLOCKS = 64

/** Tests-first stub (map layers L0b, B5). */
internal fun forecastBlocksToRequest(south: Double, west: Double, north: Double, east: Double): Set<ForecastBlock>? = emptySet()

/** Tests-first stub (map layers L0b, B5). */
internal fun forecastCellsFeatureCollection(cells: List<ForecastCell>): FeatureCollection = FeatureCollection.fromFeatures(emptyList())

/** Tests-first stub (map layers L0b, B6). */
internal fun colourFieldFillColour(ramp: ColourRamp): Expression = Expression.color(0)

/** Tests-first stub (map layers L0b, B4). */
internal fun forecastCellsShownOf(cells: List<ForecastCell>): ForecastCellsShown? = null
