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
 * no map object, so the order is testable without a `MapView`. The map calls [onMembers] when the fanned markers
 * change, [onFold] when the fan starts to fold, [onCopiesDrawn] when the renderer reports the copies on screen
 * ([awaitCopiesRendered]), and [onRest] when the fan has spread, as the last resort.
 *
 * Why the order matters: the originals' filter takes effect on the next frame, while the copies are `setGeoJson`
 * data the renderer re-tiles off the main thread, so hiding first leaves a frame or two with neither drawn (the
 * blink at the open's start, amendment -371). The copies sit exactly on their originals at progress 0 (-299), so
 * showing both for those frames is not seen. Rejected: a fixed wait before hiding (a number tuned on one phone;
 * the S26 refreshes twice as fast) and holding the fan's start (the fan must not start later).
 */
class FanOutHideGate {
    private var generation = 0
    private var hidden: List<FanMember> = emptyList()
    private var pending: List<FanMember>? = null

    /**
     * The fanned markers changed to [members] (a fan opened, another replaced it, or it was released: empty). Originals
     * that stay fanned stay hidden, every other original is shown at once (so a replaced fan's markers return as its
     * copies go), and the rest wait for [onCopiesDrawn]. An empty [members] shows everything and cancels any wait.
     */
    fun onMembers(members: List<FanMember>, spread: Boolean = false): FanOutHideStep {
        generation++
        if (members.isEmpty()) {
            hidden = emptyList()
            pending = null
            return FanOutHideStep(emptyList(), generation, awaiting = false)
        }
        val keys = members.map { it.key }.toSet()
        val keep = hidden.filter { it.key in keys }
        if (keep.size == members.size) {
            hidden = members
            pending = null
            return FanOutHideStep(members, generation, awaiting = false)
        }
        hidden = keep
        pending = members
        return FanOutHideStep(keep, generation, awaiting = true)
    }

    /** The fan started to fold: a hide still waiting is cancelled, so the originals stay shown. Nothing already hidden changes (the release shows them). */
    fun onFold() {
        pending = null
    }

    /** The copies of the wait named by [generation] are drawn: hide their originals. Null for a wait that was cancelled, replaced or already done. */
    fun onCopiesDrawn(generation: Int): FanOutHideStep? {
        val waiting = pending ?: return null
        if (generation != this.generation) return null
        hidden = waiting
        pending = null
        return FanOutHideStep(waiting, generation, awaiting = false)
    }

    /** The fan has spread and the signal never came: hide what is still waiting, flagged so the caller logs it. Null when nothing is waiting. */
    fun onRest(): FanOutHideStep? {
        val waiting = pending ?: return null
        hidden = waiting
        pending = null
        return FanOutHideStep(waiting, generation, awaiting = false, viaFallback = true)
    }
}
