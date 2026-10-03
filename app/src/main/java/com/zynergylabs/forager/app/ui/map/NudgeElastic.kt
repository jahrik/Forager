package com.zynergylabs.forager.app.ui.map

import android.view.MotionEvent

/** How far the map is shown displaced from where following puts it, in screen pixels: right and down are positive. */
data class NudgeOffset(val dxPx: Float, val dyPx: Float) {
    companion object {
        val ZERO = NudgeOffset(0f, 0f)
    }
}

/** One nudge's give. Tests-first stub (dispatch 2026-09-28-463): gives nothing. */
class NudgeGive(private val thresholdPx: Float, private val ratio: Float = NAVIGATION_NUDGE_GIVE_RATIO) {
    var offset = NudgeOffset.ZERO
        private set

    fun down(x: Float, y: Float, current: NudgeOffset) = Unit

    fun move(x: Float, y: Float, pointerCount: Int, canGive: Boolean): NudgeOffset? = null

    fun release(stillFollowing: Boolean): NudgeOffset? = null
}

/** Stub. */
fun nudgePadding(topPaddingPx: Double, offset: NudgeOffset): DoubleArray = doubleArrayOf(0.0, topPaddingPx, 0.0, 0.0)

/** Stub. */
fun nudgeSpringOffset(from: NudgeOffset, fraction: Float, tension: Double = NAVIGATION_NUDGE_OVERSHOOT_TENSION): NudgeOffset = NudgeOffset.ZERO

/** The map's side of the elastic nudge. */
interface NudgeCamera {
    fun canGive(): Boolean

    fun showPadding(padding: DoubleArray)
}

/** Stub. */
class NudgeElasticDriver(
    private val camera: NudgeCamera,
    thresholdPx: Float,
    private val viewTopPaddingPx: () -> Double,
) {
    fun onTouch(event: MotionEvent) = Unit

    fun cancel() = Unit
}
