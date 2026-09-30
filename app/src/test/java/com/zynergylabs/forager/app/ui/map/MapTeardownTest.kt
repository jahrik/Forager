package com.zynergylabs.forager.app.ui.map

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Dispatch 2026-09-28-267, Part B. A map that leaves composition with its tab must stop its location component
 * before its MapView is destroyed: `LocationComponent.onDestroy()` is a no-op and only `onStop()` removes the
 * location-engine listener and cancels the animators, so without it every GPS fix calls into the destroyed view.
 * The real symptom (the log lines, the tap loss) is device-only; this holds the order and the presence of the call.
 */
class MapTeardownTest {

    private class Recording : MapTeardownTarget {
        val calls = mutableListOf<String>()
        override fun stopLocationUpdates() { calls += "stopLocationUpdates" }
        override fun destroyLocationComponent() { calls += "destroyLocationComponent" }
        override fun destroyMapView() { calls += "destroyMapView" }
    }

    @Test
    fun `location updates are stopped before the MapView is destroyed`() {
        val target = Recording()

        tearDownMap(target)

        val stopAt = target.calls.indexOf("stopLocationUpdates")
        val destroyAt = target.calls.indexOf("destroyMapView")
        assertEquals("the map's teardown never stopped its location component: ${target.calls}", true, stopAt >= 0)
        assertEquals("stopLocationUpdates must come before destroyMapView: ${target.calls}", true, stopAt < destroyAt)
    }
}
