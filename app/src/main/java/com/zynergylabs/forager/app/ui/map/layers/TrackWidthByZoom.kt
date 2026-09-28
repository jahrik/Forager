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
 *
 * The stops are the planner's proposal in dispatch 2026-09-28-34, for the owner to judge on the phone:
 * today's width at zoom 15 and above, 40% of it at zoom 11 and below, linear in between. Defined
 * once, here, so a tweak is one edit. The offline outline does not use them: it is 1.5 dp at every
 * zoom, as the dispatch asked. `line-dasharray` is in multiples of the line width, so the
 * breadcrumb's dots and gaps shrink with it (a device-only look).
 */
internal val TRACK_WIDTH_ZOOM_STOPS: List<ZoomWidthStop> = listOf(
    ZoomWidthStop(zoom = 11f, fractionOfFullWidth = 0.4f),
    ZoomWidthStop(zoom = 15f, fractionOfFullWidth = 1f),
)
