package com.zynergylabs.forager.app.ui.map.layers

/**
 * One feature a tap query returned: the layer it was queried on and the feature's own id (the
 * `featureId` property the pure builders write, or a sighting's `observationId`); `null` when the
 * feature carries none.
 */
data class TapHit(val layerId: String, val featureId: String?)

/**
 * The side of the square box, in dp, that a tap queries when nothing lies exactly under the tap
 * point (owner's ruling 4 on 2026-09-27-30: a finger-sized box, so thin lines are hittable).
 */
const val TAP_BOX_DP = 48f

/** The layers of [drawOrder] a tap is queried against: every layer whose [TapGroup] has a precedence. */
fun tappableLayerIds(drawOrder: List<MapLayerSpec>): List<String> = emptyList()

/**
 * Which of [hits] a tap goes to (L0 design ruling 3): markers first, then lines and area outlines,
 * then colour fields; within a group, the layer drawn on top in [drawOrder]; within one layer, the
 * first hit the query returned. `null` when no hit is on a tappable layer of [drawOrder].
 */
fun tapWinner(hits: List<TapHit>, drawOrder: List<MapLayerSpec>): TapHit? = null

/**
 * A tap resolved in two stages: [pointHits], what lies exactly under the tap point, decides when any
 * of it is tappable; only when none is does [boxHits] run (the finger-sized box, [TAP_BOX_DP]).
 */
fun resolveTap(pointHits: List<TapHit>, boxHits: () -> List<TapHit>, drawOrder: List<MapLayerSpec>): TapHit? = null
