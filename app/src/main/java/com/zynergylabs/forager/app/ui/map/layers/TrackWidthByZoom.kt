package com.zynergylabs.forager.app.ui.map.layers

/**
 * One stop of a track line's width across zoom: at [zoom], the line is [fractionOfFullWidth] of its
 * full width (the `widthDp` of its `LineLayerSpec`). MapLibre's `interpolate` is linear between
 * stops and holds the end values outside them, so the first stop's fraction applies at every zoom
 * below it and the last stop's at every zoom above it.
 */
internal data class ZoomWidthStop(val zoom: Float, val fractionOfFullWidth: Float)

/**
 * Track widths by zoom (owner, 2026-09-28: "At some point in zooming out, the tracks get muddied up
 * from the thickness + distance. Can we have the track lines thin out as we zoom out?"). Every track
 * line and its casing uses these stops, so a casing keeps its ratio to its line at every zoom.
 *
 * Tests-first stub: no stops yet.
 */
internal val TRACK_WIDTH_ZOOM_STOPS: List<ZoomWidthStop> = emptyList()
