package com.zynergylabs.forager.app.ui.map.fanout

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Which camera moves are the location follower's (dispatch 2026-09-28-380). MapLibre gives every programmatic move the same reason, so the app marks
 * its own moves and the mark lasts until the camera is idle. Pure; that the app marks every one of its moves is read from the code and recorded in
 * the report, and what a real follower does is the recordings'.
 */
class CameraMoveClassifierTest {

    private val classifier = CameraMoveClassifier()

    @Test
    fun `a touch is the user's, marked or not, following or not`() {
        assertEquals(CameraMoveCause.USER_GESTURE, classifier.classify(isGesture = true, followingLocation = true))
        classifier.markAppMove()
        assertEquals(CameraMoveCause.USER_GESTURE, classifier.classify(isGesture = true, followingLocation = true))
        assertEquals(CameraMoveCause.USER_GESTURE, classifier.classify(isGesture = true, followingLocation = false))
    }

    @Test
    fun `an unmarked move that is not a touch, while following the location, is the follower's`() {
        assertEquals(CameraMoveCause.LOCATION_FOLLOW, classifier.classify(isGesture = false, followingLocation = true))
    }

    @Test
    fun `a move the app marked is the app's, following or not`() {
        classifier.markAppMove()
        assertEquals(CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = false))
        assertEquals(CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true))
    }

    @Test
    fun `the locate tap's own move is the app's, because the mark is set before the mode changes`() {
        // The locate button marks, then sets the camera mode; the follower's first re-centre then arrives while following.
        classifier.markAppMove()
        assertEquals(
            "the user pressed locate, so the move it causes is the user's",
            CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true),
        )
    }

    @Test
    fun `a follower's move that arrives while a marked move is still in flight is classed with it, and does not clear the mark`() {
        classifier.markAppMove()
        assertEquals(CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true))
        assertEquals("still in flight: the next move is not the follower's yet", CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true))
        assertEquals(CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true))
    }

    @Test
    fun `the mark does not outlive the idle that ends its move`() {
        classifier.markAppMove()
        classifier.onCameraIdle()

        assertEquals(CameraMoveCause.LOCATION_FOLLOW, classifier.classify(isGesture = false, followingLocation = true))
    }

    @Test
    fun `a mark set after an idle works again`() {
        classifier.markAppMove()
        classifier.onCameraIdle()
        classifier.markAppMove()

        assertEquals(CameraMoveCause.APP_REQUESTED, classifier.classify(isGesture = false, followingLocation = true))
    }

    @Test
    fun `an unmarked move that is not a touch while not following is unknown`() {
        assertEquals(CameraMoveCause.UNKNOWN, classifier.classify(isGesture = false, followingLocation = false))
    }
}
