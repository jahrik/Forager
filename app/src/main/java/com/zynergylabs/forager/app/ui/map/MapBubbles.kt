package com.zynergylabs.forager.app.ui.map

import androidx.compose.ui.geometry.Offset
import com.zynergylabs.forager.app.domain.ForecastBlock
import com.zynergylabs.forager.app.domain.ForecastCell
import com.zynergylabs.forager.app.domain.ForecastCellStore
import com.zynergylabs.forager.app.domain.ForecastCellsResult
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.DEFAULT_STALE_THRESHOLD_DAYS
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.LogPhoto
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPointRecord
import com.zynergylabs.forager.app.domain.model.Waypoint
import java.time.LocalDate

/**
 * M1, tap a map glyph for a bubble (`prompts/preserved/2026-09-28-29.md`, continuation
 * `2026-09-28-30`; the plan's "M1 rulings"): the plain-data half of the bubbles. What a tap names,
 * the one thing a map's bubble shows, what each kind's bubble says, and how the forecast cell is
 * looked up. Nothing here names a Compose UI node or a MapLibre type (an [Offset] is a value), so it
 * all runs headless (`MapBubblesTest`). The shell that draws a bubble is `MapBubble.kt`.
 */

/** The record kinds a map glyph can be (owner: "Finds and photos, Waypoints, Tracks, Planned trips & offline maps"), plus a forecast cell. */
enum class MapBubbleKind(
    /** Whether the kind is drawn as a point, so its bubble re-anchors on the point on every camera idle (planner's M1 ruling). */
    val isPoint: Boolean,
) {
    FIND(isPoint = true),
    PHOTO(isPoint = true),
    WAYPOINT(isPoint = true),
    PLANNED_TRIP(isPoint = true),
    TRACK(isPoint = false),
    OFFLINE_REGION(isPoint = false),
    FORECAST_CELL(isPoint = false),
}

/** The kind a tap on [layerId] names, or `null` for a layer no bubble is built for. */
fun mapBubbleKindOf(layerId: String): MapBubbleKind? = null // Tests-first stub.

/**
 * The one thing a map is showing a bubble for (planner's M1 ruling: one bubble at a time, a single
 * tapped-thing state that includes sightings), with where its tail points: [anchorPx] in the map
 * slot's own coordinates and the camera bearing, as `onSightingTap` and [MapFeatureTap] report them.
 */
data class TappedMapThing(val target: MapBubbleTarget, val anchorPx: Offset, val bearingDeg: Float)

/** What a bubble is about. */
sealed interface MapBubbleTarget {
    data class SightingTarget(val sighting: Sighting) : MapBubbleTarget

    /** A record or a cell, by the layer it was tapped on and its feature id; [at] is the tapped map position. */
    data class FeatureTarget(val kind: MapBubbleKind, val layerId: String, val featureId: String, val at: LatLng) : MapBubbleTarget
}

/** The [TappedMapThing] a feature tap makes, or `null` for a layer no bubble is built for. */
fun tappedThingOf(tap: MapFeatureTap): TappedMapThing? = null // Tests-first stub.

/** The sighting [MapOverlayContent.focusedObservationId] names while this thing's bubble shows. */
val TappedMapThing?.focusedObservationId: Long?
    get() = ((this?.target) as? MapBubbleTarget.SightingTarget)?.sighting?.observationId

/** The point feature [MapOverlayContent.focusedFeature] names while this thing's bubble shows; `null` for lines, regions and cells, which keep the tap point. */
val TappedMapThing?.focusedFeature: FocusedMapFeature?
    get() = null // Tests-first stub.

/**
 * Where [focus]'s glyph is now, from the lists the map draws: the position a point bubble is
 * re-anchored on at each camera idle (F2, generalising `focusedObservationId`). `null` when the
 * record is no longer drawn, so the map stops re-anchoring rather than reviving a stale bubble.
 */
fun focusedFeaturePosition(
    focus: FocusedMapFeature,
    plannedTrips: List<PlannedTrip>,
    waypoints: List<Waypoint>,
    findMarkers: List<RecordPoint>,
    photoMarkers: List<RecordPoint>,
): LatLng? = null // Tests-first stub.

/**
 * Everything a map host looks a tapped record up in, and the J5c details sheet's inputs (B3, B4):
 * the lists the host already holds, not a new read. [onOpenFind] is the find bubble's action,
 * labelled [openFindLabel] ("Open in Journal" on the Maps tab, "Open find" on an entry's map); `null`
 * leaves the find bubble with no action.
 */
data class MapRecordSources(
    val finds: List<MushroomLogEntry> = emptyList(),
    val galleryPhotos: List<GalleryPhoto> = emptyList(),
    /** How many journal entries keep each photo (`MushroomLogUiState.cartographyEntryPhotoReferenceCounts`). */
    val photoEntryReferenceCounts: Map<String, Int> = emptyMap(),
    val waypoints: List<Waypoint> = emptyList(),
    val waypointEntryReferenceCounts: Map<String, Int> = emptyMap(),
    val tracks: List<Track> = emptyList(),
    val plannedTrips: List<PlannedTrip> = emptyList(),
    val offlineRegions: List<OfflineRegionSummary> = emptyList(),
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val staleThresholdDays: Int = DEFAULT_STALE_THRESHOLD_DAYS,
    val nowEpochMillis: () -> Long = System::currentTimeMillis,
    val getFullRecord: suspend (String) -> Result<List<TrackPointRecord>> = { Result.success(emptyList()) },
    val onOpenFind: ((String) -> Unit)? = null,
    val openFindLabel: String = OPEN_IN_JOURNAL_LABEL,
)

/** The Maps tab's find action (owner's M1 ruling 2). */
const val OPEN_IN_JOURNAL_LABEL = "Open in Journal"

/** An entry map's find action (owner, Q4: "Open find, Back returns"). */
const val OPEN_FIND_LABEL = "Open find"

/** What one bubble says, per kind (B3): each starts from what that record's row, sheet or card already shows, kept short. */
sealed interface MapBubbleContent {
    /** A find: its identification or "Find on <date>", the date when the title is the identification, and its cover photo. */
    data class Find(val findId: String, val title: String, val date: String?, val coverPhotoPath: String?) : MapBubbleContent

    /** A photo: the photo, its date, and what it is attached to. */
    data class Photo(val photo: LogPhoto, val date: String, val attachedTo: String?) : MapBubbleContent

    /** A waypoint: its name and MGRS; Directions and details. */
    data class WaypointContent(val waypoint: Waypoint, val mgrs: String?) : MapBubbleContent

    /** A track: its title, date, distance and duration; details. */
    data class TrackContent(val trackId: String, val title: String, val date: String, val distance: String, val duration: String) : MapBubbleContent

    /** A planned trip: what its Trip Planner row shows; Directions. */
    data class Trip(val trip: PlannedTrip, val date: String, val mgrs: String?, val coordinates: String) : MapBubbleContent

    /** An offline region: its name, radius, size, and whether it is stale; details. */
    data class Region(val regionId: Long, val name: String, val radius: String, val size: String, val stale: Boolean) : MapBubbleContent

    /**
     * A forecast cell (the L0b forecast-facing rulings: chance, uncertainty, top drivers and data
     * dates, beside the reference class). [layerName] is the layer's own name, so a synthetic layer
     * is never labelled with the fixed term for the real number.
     */
    data class Cell(
        val layerName: String,
        val chance: String,
        val range: String,
        val drivers: List<String>,
        val dates: String,
        val referenceClass: String,
    ) : MapBubbleContent
}

/**
 * What [target]'s bubble says, looked up by its feature id in [sources]; `null` when the record is
 * not there any more (a delete landed, a list reloaded), which the host treats as nothing to show.
 * Not for cells, which are looked up in the store ([lookUpForecastCell]).
 */
fun mapBubbleContentFor(target: MapBubbleTarget.FeatureTarget, sources: MapRecordSources): MapBubbleContent? = null // Tests-first stub.

/** A forecast cell's bubble, from the stored cell and its layer's name. */
fun forecastCellBubble(layerName: String, cell: ForecastCell): MapBubbleContent.Cell =
    MapBubbleContent.Cell(layerName, "", "", emptyList(), "", "") // Tests-first stub.

/**
 * The stored cell a tap on a colour field named, re-read from [store] by [group], [week] and block
 * (planner's M1 ruling: re-queried, not parsed from the id). The blocks asked for are every block a
 * cell under [at] can belong to; the cell is the one whose feature id is [featureId]. `null` when
 * the store no longer has it.
 */
suspend fun lookUpForecastCell(
    store: ForecastCellStore,
    group: String,
    week: LocalDate,
    at: LatLng,
    featureId: String,
): ForecastCell? = null // Tests-first stub.

/** The feature id a cell's map feature carries: the one place it is written ([forecastCellsFeatureCollection]) and matched ([lookUpForecastCell]). */
internal fun forecastCellFeatureId(cell: ForecastCell): String = "${cell.centre.lat},${cell.centre.lng}"

/**
 * Where a bubble goes and where its tail's tip is (B2, planner's M1 ruling: the tail's tip always
 * lands on the tapped point; when the clamp moves the card, the tail moves with the anchor, not
 * with the card). [topLeft] is the bubble's placed corner in the map box; [tipInBubble] is the
 * anchor in the bubble's own coordinates, so `topLeft + tipInBubble == anchor` wherever the clamp
 * put the card.
 */
data class BubblePlacement(val topLeftX: Int, val topLeftY: Int, val tipInBubble: Offset)

/**
 * Places a bubble of [bubbleWidth] by [bubbleHeight] px (the card plus [tailPx] of margin on every
 * side) so that, unclamped, its tail of [tailPx] ends on [anchor] along [arrowAngleDeg] (screen
 * space, clockwise from up, pointing from the card back to the anchor); then clamps it into the box
 * [maxWidth] by [maxHeight], no higher than [minY]. The tip stays on [anchor] either way.
 */
fun bubblePlacement(
    anchor: Offset,
    arrowAngleDeg: Float,
    bubbleWidth: Int,
    bubbleHeight: Int,
    tailPx: Float,
    maxWidth: Int,
    maxHeight: Int,
    minY: Int,
): BubblePlacement = BubblePlacement(0, 0, Offset.Zero) // Tests-first stub.
