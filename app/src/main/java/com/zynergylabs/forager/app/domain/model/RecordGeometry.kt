package com.zynergylabs.forager.app.domain.model

/**
 * Geometry a map draws for one stored record, carrying that record's id beside it.
 *
 * Map layers L0a (owner's ruling 2 on `prompts/preserved/2026-09-27-30.md`, "Now, in L0a"): a map
 * feature must carry the id of the record it stands for, so a tap on it can be mapped back to the
 * record (the generic feature-tap callback now; M1's bubbles and J8 next). Before this, finds,
 * photos, kept tracks and offline-region circles reached the map as bare coordinates:
 * [com.zynergylabs.forager.app.domain.GetCartographyEntryMapDataUseCase] had each record's id in
 * hand and dropped it when it mapped to [LatLng] or [Region].
 *
 * [recordId] is the record's own id as the rest of the app holds it: a find's
 * [FindDecision.findId], a photo's [PhotoAttachment.photoId], a track's [TrackDecision.trackId], a
 * waypoint's [WaypointDecision.waypointId], and an offline region's
 * [OfflineRegionDecision.offlineRegionId] written in decimal (the one id here that is a `Long`; a
 * map feature's id is text for every layer, so one callback type serves them all).
 */
data class RecordPoint(val recordId: String, val at: LatLng)

/** A stored track's points, oldest first, with the track's id. See [RecordPoint]. */
data class RecordPolyline(val recordId: String, val points: List<LatLng>)

/** A stored offline region's circle, with the region's id. See [RecordPoint]. */
data class RecordRegion(val recordId: String, val region: Region)
