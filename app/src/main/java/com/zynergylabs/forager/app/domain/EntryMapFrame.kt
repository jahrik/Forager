package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.GeoBoundingBox
import com.zynergylabs.forager.app.domain.model.LatLng

/**
 * Where a day entry's map opens (owner, 2026-09-28: "Can the entry map open to their last recorded
 * track location, zoomed in to where you see in the next photo?", ruled "Fit all kept records").
 */
sealed interface EntryMapFrame {
    /** Fit [bounds] with [paddingDp] on every side, zoomed no closer than [maxZoom]. */
    data class Fit(
        val bounds: GeoBoundingBox,
        val paddingDp: Int = ENTRY_MAP_FRAME_PADDING_DP,
        val maxZoom: Double = ENTRY_MAP_FRAME_MAX_ZOOM,
    ) : EntryMapFrame

    /** Everything the entry keeps is at one place: centre on it at [zoom]. */
    data class SinglePoint(val at: LatLng, val zoom: Double = ENTRY_MAP_SINGLE_POINT_ZOOM) : EntryMapFrame
}

/** The padding around the fitted bounds, in dp. */
const val ENTRY_MAP_FRAME_PADDING_DP = 48

/** The closest a fit may zoom. */
const val ENTRY_MAP_FRAME_MAX_ZOOM = 17.0

/** The zoom an entry whose kept records are all at one place opens at. */
const val ENTRY_MAP_SINGLE_POINT_ZOOM = 16.0

/** Tests-first stub: never a frame. */
fun entryMapFrame(data: CartographyEntryMapData): EntryMapFrame? = null
