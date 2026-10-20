package com.zynergylabs.forager.app.ui.map

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.map.fanout.CameraMoveClassifier
import com.zynergylabs.forager.app.ui.map.fanout.FanReopenCoordinator
import com.zynergylabs.forager.app.ui.map.fanout.MapTapHandler
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutState
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * What [SightingsMap] remembers for the life of its MapView, carried as one object so the map's effect groups can live in
 * their own files and take it (UI split Phase 2.2, dispatch 2026-10-19-29; survey `docs/audits/2026-10-18-ui-split-survey.md`
 * on branch `ui-split-survey`, section 5.1).
 *
 * **A view over State objects, never a copy.** [SightingsMap] still remembers every one of these where it always did, with
 * the same start value and in the same order, and hands this holder the State objects themselves. Each read here is a getter
 * on that State and each write a setter on it, so a listener registered once, or an effect launched before a later change,
 * reads exactly what it read when the State was a local delegate. A value copied in would be stale the moment the State
 * changed: the trap the frame's extraction found, where a value parameter would have saved the fullscreen preference
 * inverted (`docs/audits/2026-09-26-split-stage-g-completion-report.md`).
 *
 * **What is not here, on purpose.** Anything [SightingsMap] remembers under a key or calls conditionally has a different
 * lifetime from the MapView and stays a parameter of whichever group needs it: the app's compass and location engines, the
 * drawn layers state, the fan's space (`rememberMapFanSpace` is `remember(keepOuts)`, and the tap handler keeps the space it
 * was registered with while the layout reads the latest one), the fan's hide gate (`remember(loadedStyle)`), the heading and
 * the position look, and the first orientation-reset id. So are [SightingsMap]'s own parameters, which some effects capture
 * at launch rather than read live.
 */
internal class SightingsMapState(
    /** The one MapView, made in a `remember` as the map is composed. */
    val mapView: MapView,
    // Latest values (rememberUpdatedState in SightingsMap), read by listeners registered once and effects launched earlier.
    private val onTapState: State<() -> Unit>,
    private val onSightingTapState: State<(Sighting, Offset, Float) -> Unit>,
    private val onLongPressState: State<(LatLng) -> Unit>,
    private val onCameraIdleState: State<(LatLng) -> Unit>,
    private val onUserCameraGestureState: State<() -> Unit>,
    private val navigationViewState: State<NavigationViewRequest?>,
    private val positionLookState: State<PositionLook>,
    private val onFeatureTapState: State<(MapFeatureTap) -> Unit>,
    private val onCloseBubbleState: State<() -> Unit>,
    private val layersStateState: State<MapLayersState>,
    private val chromeColourState: State<Int>,
    private val forecastState: State<MapForecastFeed?>,
    private val sightingsState: State<List<Sighting>>,
    private val focusedObservationIdState: State<Long?>,
    private val focusedFeatureState: State<FocusedMapFeature?>,
    private val cameraMemoryState: State<MapCameraMemory?>,
    private val returnMemoryState: State<MapReturnMemory?>,
    private val plannedTripsState: State<List<PlannedTrip>>,
    private val waypointsState: State<List<Waypoint>>,
    private val findMarkersState: State<List<RecordPoint>>,
    private val photoMarkersState: State<List<RecordPoint>>,
    // Written state (remember { mutableStateOf(...) } in SightingsMap).
    private val mapLibreMapState: MutableState<MapLibreMap?>,
    private val loadedStyleState: MutableState<Style?>,
    private val appliedStyleState: MutableState<AppliedMapStyle?>,
    private val coverPhaseState: MutableState<CoverPhase>,
    private val lastAppliedCameraTargetState: MutableState<Pair<Region, LatLng?>?>,
    private val lastAppliedCameraRequestIdState: MutableState<String?>,
    private val attributionDefaultMarginsState: MutableState<IntArray?>,
    private val cameraIdleCountState: MutableIntState,
    private val navigationMapHeightPxState: MutableIntState,
    private val mapSizePxState: MutableState<IntSize>,
    private val creditWidthPxState: MutableIntState,
    // Objects made once for the MapView's life (unkeyed remember in SightingsMap), shared by reference.
    /** The position look last handed to MapLibre. */
    val appliedPositionLook: Array<PositionLook>,
    val navigationModeChange: NavigationModeChange,
    /** The chrome colour the fan's circle image holds in the loaded style. */
    val fanCircleColour: Array<Int?>,
    val cameraMoveClassifier: CameraMoveClassifier,
    /** Where the current touch came down, for the release's log line. */
    val touchDownAt: FloatArray,
    val nudgeFlingGuard: NudgeFlingGuard,
    val nudgeElastic: NudgeElasticDriver,
    /** RECORD -761: the picture a returning map draws over its first frames, or null when it draws itself as before. */
    val coverPicture: ImageBitmap?,
    val fanOut: MarkerFanOutState,
    val tapHandlerRef: TapHandlerRef,
    val fanReopen: FanReopenCoordinator,
    /** RECORD -750, item 5: the last search frame applied, for a map with no camera memory. */
    val localAppliedSearchFrameId: IntArray,
) {
    // Latest values: getters only.
    val onTap: () -> Unit get() = onTapState.value
    val onSightingTap: (Sighting, Offset, Float) -> Unit get() = onSightingTapState.value
    val onLongPress: (LatLng) -> Unit get() = onLongPressState.value
    val onCameraIdle: (LatLng) -> Unit get() = onCameraIdleState.value
    val onUserCameraGesture: () -> Unit get() = onUserCameraGestureState.value
    val navigationView: NavigationViewRequest? get() = navigationViewState.value
    val positionLook: PositionLook get() = positionLookState.value
    val onFeatureTap: (MapFeatureTap) -> Unit get() = onFeatureTapState.value
    val onCloseBubble: () -> Unit get() = onCloseBubbleState.value

    /** The layers state the map draws with: the one it is given, with the colour fields its feed does not name hidden. */
    val layersState: MapLayersState get() = layersStateState.value
    val chromeColour: Int get() = chromeColourState.value
    val forecast: MapForecastFeed? get() = forecastState.value
    val sightings: List<Sighting> get() = sightingsState.value
    val focusedObservationId: Long? get() = focusedObservationIdState.value
    val focusedFeature: FocusedMapFeature? get() = focusedFeatureState.value
    val cameraMemory: MapCameraMemory? get() = cameraMemoryState.value
    val returnMemory: MapReturnMemory? get() = returnMemoryState.value
    val plannedTrips: List<PlannedTrip> get() = plannedTripsState.value
    val waypoints: List<Waypoint> get() = waypointsState.value
    val findMarkers: List<RecordPoint> get() = findMarkersState.value
    val photoMarkers: List<RecordPoint> get() = photoMarkersState.value

    // Written state: a getter and a setter on the same State.
    var mapLibreMap: MapLibreMap?
        get() = mapLibreMapState.value
        set(value) { mapLibreMapState.value = value }

    /** The Style from the most recently completed setStyle callback. */
    var loadedStyle: Style?
        get() = loadedStyleState.value
        set(value) { loadedStyleState.value = value }

    /** Which style is applied, so an unchanged one is not reloaded. */
    var appliedStyle: AppliedMapStyle?
        get() = appliedStyleState.value
        set(value) { appliedStyleState.value = value }

    var coverPhase: CoverPhase
        get() = coverPhaseState.value
        set(value) { coverPhaseState.value = value }

    /** What the camera was last deliberately moved to by the data+camera refresh. */
    var lastAppliedCameraTarget: Pair<Region, LatLng?>?
        get() = lastAppliedCameraTargetState.value
        set(value) { lastAppliedCameraTargetState.value = value }

    /** The id of the last MapRenderMode.cameraRequest this MapView applied. */
    var lastAppliedCameraRequestId: String?
        get() = lastAppliedCameraRequestIdState.value
        set(value) { lastAppliedCameraRequestIdState.value = value }

    /** MapLibre's own attribution margins, read once the map is ready. */
    var attributionDefaultMargins: IntArray?
        get() = attributionDefaultMarginsState.value
        set(value) { attributionDefaultMarginsState.value = value }

    /** Counts camera idles; the forecast cells' feed is keyed on it. */
    var cameraIdleCount: Int
        get() = cameraIdleCountState.intValue
        set(value) { cameraIdleCountState.intValue = value }

    /** The map's height, for the navigation view's padding. */
    var navigationMapHeightPx: Int
        get() = navigationMapHeightPxState.intValue
        set(value) { navigationMapHeightPxState.intValue = value }

    /** The map's own size, for the attribution placement. */
    var mapSizePx: IntSize
        get() = mapSizePxState.value
        set(value) { mapSizePxState.value = value }

    /** The credit strip's laid-out width, for the attribution placement. */
    var creditWidthPx: Int
        get() = creditWidthPxState.intValue
        set(value) { creditWidthPxState.intValue = value }
}

/**
 * Holds the tap handler once the map is ready, for the listeners and effects that outlive one composition to reach it.
 * `internal` and in this file since UI split Phase 2.2 (dispatch 2026-10-19-29); it was private to `SightingsMap.kt`.
 */
internal class TapHandlerRef {
    var handler: MapTapHandler? = null
}

/**
 * RECORD -761: where a returning map's cover is.
 * `internal` and in this file since UI split Phase 2.2 (dispatch 2026-10-19-29); it was private to `SightingsMap.kt`.
 */
internal enum class CoverPhase { COVERING, HANDED_OVER, GONE }
