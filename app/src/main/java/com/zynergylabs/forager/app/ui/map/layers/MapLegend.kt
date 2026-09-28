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

/**
 * The legend chip's content (map layers L0b, B4, as planner message 2 gave it), or `null` when the chip
 * is not shown: it shows only while at least one colour field is visible in [state], the state the map
 * draws with (`withUnavailableColourFieldsHidden` already applied, so a layer with no data never counts).
 *
 * Collapsed, the chip names the one visible layer, or reads "2 layers". Expanded, it lists every visible
 * layer, top of the draw order first as the sheet does, each with its name, its ramp and, while any of
 * its cells is in view ([shown]), its week and data dates ([legendDatesLine]). The chip also carries,
 * per layer, "no forecast here" beside an empty swatch, and the reference class once under the ramps;
 * both are fixed text ([LEGEND_NO_FORECAST_HERE], [LEGEND_REFERENCE_CLASS]).
 */
fun mapLegendFor(
    state: MapLayersState,
    registry: List<MapLayerSpec>,
    colourFields: List<ColourFieldSpec>,
    shown: Map<String, ForecastCellsShown>,
): MapLegend? {
    val specsById = colourFields.associateBy { it.layerId }
    val visible = colourFieldsTopFirst(registry, state)
        .filter { layerPaintFor(it, state).visible }
        .mapNotNull { specsById[it.id] }
    if (visible.isEmpty()) return null
    val collapsed = visible.singleOrNull()?.label ?: "${visible.size} layers"
    return MapLegend(
        collapsedLabel = collapsed,
        layers = visible.map { field -> LegendLayer(field.layerId, field.label, field.ramp, shown[field.layerId]?.let(::legendDatesLine)) },
    )
}

/**
 * "Week of <week>, weather to <weather_through>" (planner message 2), both as the ISO dates the cells
 * themselves carry (D55: `week` is the ISO week start, `weather_through` a date).
 */
fun legendDatesLine(shown: ForecastCellsShown): String = "Week of ${shown.week}, weather to ${shown.weatherThrough}"
