package com.zynergylabs.forager.app.ui.map

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [isUserCameraGesture], the filter behind [MapRenderMode.onUserCameraGesture] (picker-fixes
 * dispatch, F1). The literals are the values `javap -constants` printed for
 * `MapLibreMap$OnCameraMoveStartedListener` in the pinned 13.5.0 artifact, written out rather than
 * read from the SDK so a version bump that renumbers them fails here instead of silently turning
 * the pickers' "follow until touched" into "never follow" or "always follow".
 */
class UserCameraGestureTest {

    @Test
    fun `a gesture reason is the user's touch`() {
        assertTrue(isUserCameraGesture(1)) // REASON_API_GESTURE
    }

    @Test
    fun `an app-made move is not, whether a developer animation or an API move`() {
        assertFalse(isUserCameraGesture(2)) // REASON_DEVELOPER_ANIMATION
        assertFalse(isUserCameraGesture(3)) // REASON_API_ANIMATION: Transform, tracking included
    }
}
