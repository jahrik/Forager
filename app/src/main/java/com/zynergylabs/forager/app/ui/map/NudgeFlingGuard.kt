package com.zynergylabs.forager.app.ui.map

import android.view.MotionEvent

/**
 * A quick flick under the drag threshold must spring back like a nudge, not end following (dispatch
 * 2026-09-28-477; the behaviour confirmed for -457 and -463: only a deliberate drag past the threshold
 * leaves).
 *
 * Seen on the S22 with the Part A log lines: 10 of 45 releases while following were reported as a
 * fling by MapLibre, and each ended following 1 to 2 ms later ("fling reported: mode=32", then
 * "tracking changed to 8 … byTheApp=false"). From the pinned 13.5.0 bytecode:
 * `MapGestureDetector$StandardGestureListener.onFling` fires on release when the finger's speed is at
 * least `UiSettings.getFlingThreshold()` (1,000 dp/s by default), never asking whether the move passed
 * the tracking threshold, and the location camera's fling listener (`LocationCameraController$8`) then
 * sets the camera mode to NONE unconditionally. MapLibre has no option for this on the location component.
 *
 * So, at a release while navigating and still following, MapLibre's fling is switched off for that one
 * release, before MapLibre sees it (the map view's touch listener runs before its own handling), and
 * switched back on at the next touch. A drag that has already left following (past the threshold)
 * releases with the fling on, and its momentum is as before.
 */
class NudgeFlingGuard(private val fling: FlingSwitch) {
    private var switchedOff = false

    /** [stillFollowing]: navigating, and the camera still follows the walker, at this event. */
    fun onTouch(event: MotionEvent, stillFollowing: Boolean) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> restore()
            MotionEvent.ACTION_UP -> if (stillFollowing) {
                fling.enabled = false
                switchedOff = true
            } else {
                restore()
            }
        }
    }

    private fun restore() {
        if (switchedOff) {
            fling.enabled = true
            switchedOff = false
        }
    }
}

/** MapLibre's fling, behind an interface so the guard can be tested without a MapView. */
interface FlingSwitch {
    var enabled: Boolean
}
