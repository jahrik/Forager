package com.zynergylabs.forager.app.ui.map.layers

import java.time.LocalDate

/**
 * What a colour field's cells in view say about their own dates: the forecast week and the weather
 * they were computed through. [weatherThrough] is the earliest date among the cells in view, so the
 * legend never claims fresher weather than some cell on screen has.
 */
data class ForecastCellsShown(val week: LocalDate, val weatherThrough: LocalDate)

/** One visible colour field in the expanded legend. [dates] is `null` while none of its cells is in view. */
data class LegendLayer(val layerId: String, val name: String, val ramp: ColourRamp, val dates: String?)

/** The legend chip's content: [collapsedLabel] on the chip, [layers] top of the draw order first when expanded. */
data class MapLegend(val collapsedLabel: String, val layers: List<LegendLayer>)

/** Each ramp's end labels (planner message 2: "its ramp with 0% and 100% end labels"). */
const val LEGEND_RAMP_LOW_LABEL = "0%"
const val LEGEND_RAMP_HIGH_LABEL = "100%"

/** Beside the empty-cell swatch, per visible layer (the dispatch, B4, and D55: "no forecast here" in the legend). */
const val LEGEND_NO_FORECAST_HERE = "no forecast here"

/**
 * The reference class, once, under the ramps (owner: "Use that wording (Recommended)"; planner message 2).
 * Source: slayer8366/Forager-app 0172c33,
 * presentation/src/main/kotlin/com/zynergy/forager/presentation/SightingChance.kt:33-35.
 */
const val LEGEND_REFERENCE_CLASS =
    "Chance this group is reported in each 11 km cell this week, where anyone is reporting fungi. " +
        "Compare areas, not spots. A high chance is not a find."

/** Tests-first stub (map layers L0b, B4). */
fun mapLegendFor(
    state: MapLayersState,
    registry: List<MapLayerSpec>,
    colourFields: List<ColourFieldSpec>,
    shown: Map<String, ForecastCellsShown>,
): MapLegend? = null

/** Tests-first stub (map layers L0b, B4). */
fun legendDatesLine(shown: ForecastCellsShown): String = ""
