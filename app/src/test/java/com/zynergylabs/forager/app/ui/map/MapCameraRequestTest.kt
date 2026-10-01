package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.domain.EntryMapFrame
import com.zynergylabs.forager.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The one-shot camera request (planner message 2026-09-28-35, the entry map's opening frame): `SightingsMap`
 * applies a request once per id and never while GPS tracking owns the camera, and caps a fitted
 * frame's zoom. The native half (`getCameraForLatLngBounds`, setting the camera) is device-only.
 */
class MapCameraRequestTest {

    private val request = MapCameraRequest("entry-map-entry-1", EntryMapFrame.SinglePoint(LatLng(45.0, -122.0)))

    @Test
    fun `a new request is applied once, and the same id is not applied again`() {
        assertTrue("never applied", shouldApplyCameraRequest(isGpsTracking = false, request = request, lastAppliedRequestId = null))
        assertFalse("already applied", shouldApplyCameraRequest(isGpsTracking = false, request = request, lastAppliedRequestId = request.id))
        assertTrue(
            "a different request",
            shouldApplyCameraRequest(isGpsTracking = false, request = request.copy(id = "entry-map-entry-2"), lastAppliedRequestId = request.id),
        )
    }

    @Test
    fun `no request, or GPS tracking owning the camera, applies nothing, where the same request otherwise would`() {
        assertTrue(shouldApplyCameraRequest(isGpsTracking = false, request = request, lastAppliedRequestId = null))
        assertFalse(shouldApplyCameraRequest(isGpsTracking = true, request = request, lastAppliedRequestId = null))
        assertFalse(shouldApplyCameraRequest(isGpsTracking = false, request = null, lastAppliedRequestId = null))
    }

    @Test
    fun `a fitted zoom is capped at the frame's maximum and otherwise kept`() {
        assertEquals(17.0, cappedFrameZoom(fittedZoom = 19.3, maxZoom = 17.0), 0.0)
        assertEquals(14.2, cappedFrameZoom(fittedZoom = 14.2, maxZoom = 17.0), 0.0)
    }
}
