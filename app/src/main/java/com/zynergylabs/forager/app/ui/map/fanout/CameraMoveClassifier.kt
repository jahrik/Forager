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
 * STUB, written before the fix so its tests can be seen to fail: classifies every move as [CameraMoveCause.UNKNOWN].
 */
class CameraMoveClassifier {
    fun markAppMove() {}

    fun onCameraIdle() {}

    fun classify(isGesture: Boolean, followingLocation: Boolean): CameraMoveCause = CameraMoveCause.UNKNOWN
}
