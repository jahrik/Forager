package com.zynergylabs.forager.app.ui.map.layers

import com.zynergylabs.forager.app.domain.MapLayerPreferences

/** [state] restored from stored choices, and one line for every stored choice that was refused. */
data class LayerStateRestore(val state: MapLayersState, val rejected: List<String>)

/** Tests-first stub (map layers L0b, B3). */
fun restoreMapLayersState(preferences: MapLayerPreferences, registry: List<MapLayerSpec>): LayerStateRestore =
    LayerStateRestore(MapLayersState.DEFAULT, emptyList())

/** A step in the colour fields' order: [UP] draws the layer one place higher, [DOWN] one lower. */
enum class ColourFieldMove { UP, DOWN }

/** Tests-first stub (map layers L0b, B1). */
fun moveColourField(state: MapLayersState, registry: List<MapLayerSpec>, layerId: String, move: ColourFieldMove): MapLayersState = state

/** Tests-first stub (map layers L0b, B1). */
fun colourFieldsTopFirst(registry: List<MapLayerSpec>, state: MapLayersState): List<MapLayerSpec> = emptyList()

/** Tests-first stub (map layers L0b, B5). */
fun withUnavailableColourFieldsHidden(state: MapLayersState, registry: List<MapLayerSpec>, available: Set<String>): MapLayersState = state
