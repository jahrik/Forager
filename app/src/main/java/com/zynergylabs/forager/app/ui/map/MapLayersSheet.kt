package com.zynergylabs.forager.app.ui.map

/** Tests-first stub (map layers L0b, B1): the sheet's tags, for tests; the sheet itself comes with the build. */
internal const val MAP_LAYERS_SHEET_TAG = "map-layers-sheet"

internal fun mapLayerSwitchTag(layerId: String) = "map-layer-switch:$layerId"

internal fun mapLayerOpacityTag(layerId: String) = "map-layer-opacity:$layerId"

internal fun mapLayerReorderTag(layerId: String) = "map-layer-reorder:$layerId"

internal const val MAP_LEGEND_CHIP_TAG = "map-legend-chip"
