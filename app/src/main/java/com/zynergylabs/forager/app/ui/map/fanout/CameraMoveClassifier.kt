package com.zynergylabs.forager.app.ui.map.fanout

/** What started a camera move, as far as an open fan is concerned (dispatch 2026-09-28-380). */
enum class CameraMoveCause {
    /** The user's touch: a pan, a zoom, a rotation. The fan closes. */
    USER_GESTURE,

    /** The app moved the camera because the user just asked for something (a control, a search result, a style change). The fan closes, as before. */
    APP_REQUESTED,

    /** The map re-centring itself while it follows the location. The user did not mean it. The fan stays open. */
    LOCATION_FOLLOW,

    /** Not a touch, not marked as the app's, and not following the location. Nothing in the app is known to make one; it closes the fan, as before, and is logged. */
    UNKNOWN,
}

/**
 * Tells a location-following move from the others. MapLibre reports every programmatic move, the location follower's included, with the
 * same reason (API_ANIMATION, `javap` of `Transform`), so the reason cannot tell them apart. What can: the app marks each camera move it
 * makes itself ([markAppMove], before making it), and the mark lasts until the camera is idle ([onCameraIdle]). A move that is not a touch,
 * not marked, while the map follows the location is the follower's.
 *
 * Held where the map is built and used on the main thread only. A mark whose move never happens (the camera was already there) lasts until the next
 * idle; until then a follower's move is taken for the app's and closes the fan, which is what it did before this class existed, so the failure is the
 * old behaviour and not a fan that stays open wrongly.
 */
class CameraMoveClassifier {
    private var appMoveMarked = false

    /** The app is about to move the camera itself (a control the user pressed, a search result, a style change). Call before the move. */
    fun markAppMove() {
        appMoveMarked = true
    }

    /** The camera is idle: the marked move, if any, is over. */
    fun onCameraIdle() {
        appMoveMarked = false
    }

    /** What started a camera move that has just begun. [isGesture] is the SDK's gesture reason; [followingLocation] is whether the location component is tracking. */
    fun classify(isGesture: Boolean, followingLocation: Boolean): CameraMoveCause = when {
        isGesture -> CameraMoveCause.USER_GESTURE
        appMoveMarked -> CameraMoveCause.APP_REQUESTED
        followingLocation -> CameraMoveCause.LOCATION_FOLLOW
        else -> CameraMoveCause.UNKNOWN
    }
}
