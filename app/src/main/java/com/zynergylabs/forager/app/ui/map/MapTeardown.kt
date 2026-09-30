package com.zynergylabs.forager.app.ui.map

/**
 * What a `SightingsMap` does to its MapView when it leaves composition (dispatch 2026-09-28-267, Part B), behind an
 * interface this project owns so the order is testable without a real `MapView` (which Robolectric cannot build).
 *
 * The MapLibre `LocationComponent` keeps its location-engine listener and its animators until its own `onStop()`;
 * its `onDestroy()` does nothing (javap of `org.maplibre.gl:android-sdk:13.5.0`). A tab switch removes the map from
 * composition without an `ON_PAUSE`, so the lifecycle observer never stops it, and it goes on calling into the
 * destroyed native view (`Mbgl-NativeMapView: You're calling ... after the MapView was destroyed`).
 */
internal interface MapTeardownTarget {
    /** `LocationComponent.onStop()`: cancels the animators and removes the location-engine listener. */
    fun stopLocationUpdates()

    /** `LocationComponent.onDestroy()`. */
    fun destroyLocationComponent()

    /** `MapView.onDestroy()`. */
    fun destroyMapView()
}

internal fun tearDownMap(target: MapTeardownTarget) {
    target.destroyLocationComponent()
    target.destroyMapView()
}
