package com.zynergylabs.forager.app.ui.map.layers

/**
 * One layer's user-set state. [opacity] is a **multiplier from 0 to 1** on each of the layer's own
 * [MapLayerSpec.baseOpacities] (owner's ruling 3 on 2026-09-27-30, "Multiplier, default 100%"): at 1
 * the layer draws exactly as it always has, the sighting dot's 0.7 fill and 0.85 ring included, and
 * at 0.5 both of those halve. An absolute value was rejected because one number cannot restore the
 * sighting dot's two different opacities.
 *
 * A value outside 0 to 1 (or NaN) is refused at construction rather than clamped: a clamp would be a
 * silent fallback (CLAUDE.md), and whatever produced such a value (a stored preference, L0b) is the
 * thing that is wrong.
 */
data class LayerState(val visible: Boolean = true, val opacity: Float = 1f) {
    init {
        require(opacity in 0f..1f) { "a layer's opacity is a multiplier from 0 to 1, not $opacity" }
    }

    companion object {
        val DEFAULT = LayerState()
    }
}

/**
 * Every layer's state, as one value carried to the map on
 * [MapRenderMode.layers][com.zynergylabs.forager.app.ui.map.MapRenderMode.layers].
 *
 * [layers] holds only what differs from [LayerState.DEFAULT]; a layer it does not name is visible at
 * opacity 1, which is today's map. An id it names that is not in the registry is ignored (a stored
 * state can outlive a layer). [reorderableOrder] is the user's order for the reorderable group
 * (colour fields, owner's layer ruling 1), bottom to top; ids it does not name keep their registry
 * order after the ones it does.
 */
data class MapLayersState(
    val layers: Map<String, LayerState> = emptyMap(),
    val reorderableOrder: List<String> = emptyList(),
) {
    fun stateOf(layerId: String): LayerState = layers[layerId] ?: LayerState.DEFAULT

    companion object {
        val DEFAULT = MapLayersState()
    }
}

/** One opacity property at the value [layerPaintFor] resolved for it. */
data class OpacityValue(val property: OpacityProperty, val value: Float)

/**
 * What `SightingsMap` sets on one native layer: its `visibility` layout property ([visible]) and each
 * of its opacity paint properties ([opacities]).
 */
data class LayerPaint(val layerId: String, val visible: Boolean, val opacities: List<OpacityValue>)

/**
 * [spec]'s paint under [state]: the state of [MapLayerSpec.stateOwnerId] when it has one, its own
 * otherwise; every base opacity times that state's multiplier.
 */
fun layerPaintFor(spec: MapLayerSpec, state: MapLayersState): LayerPaint =
    LayerPaint(spec.id, visible = true, opacities = emptyList())

/**
 * The credits of every layer in [registry] that is visible under [state] and has one, in draw order,
 * without repeats: what the attribution caption adds after the basemap's credit.
 */
fun activeLayerCredits(registry: List<MapLayerSpec>, state: MapLayersState): List<String> = emptyList()
