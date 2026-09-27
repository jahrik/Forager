package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.model.TrackPoint

/** One projected point, in the box's own pixel coordinates (origin top-left, y down). */
internal data class ThumbnailPoint(val x: Float, val y: Float)

/** J3 C3 tests-first stub: the projection is not built yet, so it projects nothing. */
@Suppress("UNUSED_PARAMETER")
internal fun projectTrackToBox(points: List<TrackPoint>, width: Float, height: Float, inset: Float = 0f): List<ThumbnailPoint> = emptyList()
