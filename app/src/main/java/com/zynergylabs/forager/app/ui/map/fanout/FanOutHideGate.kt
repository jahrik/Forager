package com.zynergylabs.forager.app.ui.map.fanout

/**
 * What to do to the originals' filters now (dispatch 2026-09-28-369, amendment -371): [hide] are the members whose originals
 * are hidden from this moment on (everything else is shown); [awaiting] is true when more are waiting for their copies to
 * be drawn, and [generation] names which wait this is, so a signal that arrives for an older one can be told from the
 * current one. [viaFallback] is true only for the step that came from [FanOutHideGate.onRest] and not from a signal.
 */
data class FanOutHideStep(
    val hide: List<FanMember>,
    val generation: Int,
    val awaiting: Boolean,
    val viaFallback: Boolean = false,
)

/**
 * The order in which a fan's originals are hidden: not before the copies that replace them are drawn. Plain state,
 * no map object, so the order is testable without a `MapView`.
 *
 * STUB, written before the fix so its tests can be seen to fail: hides at once, as the map did before the amendment.
 */
class FanOutHideGate {
    fun onMembers(members: List<FanMember>): FanOutHideStep = FanOutHideStep(hide = members, generation = 0, awaiting = false)

    fun onFold(): FanOutHideStep? = null

    fun onCopiesDrawn(generation: Int): FanOutHideStep? = null

    fun onRest(): FanOutHideStep? = null
}
