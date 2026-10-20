package com.zynergylabs.forager.app.ui.map

import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.Gravity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.delay
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import com.zynergylabs.forager.app.ui.theme.MapPalette
import com.zynergylabs.forager.app.ui.theme.navigationBarContainerColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zynergylabs.forager.app.domain.JournalEntryHighlights
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.PlannedTrip
import com.zynergylabs.forager.app.domain.model.RecordPoint
import com.zynergylabs.forager.app.domain.model.RecordPolyline
import com.zynergylabs.forager.app.domain.model.RecordRegion
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.map.initializeMapLibre
import com.zynergylabs.forager.app.ui.map.fanout.CameraMoveCause
import com.zynergylabs.forager.app.ui.map.fanout.CameraMoveClassifier
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOutHideGate
import com.zynergylabs.forager.app.ui.map.fanout.FanReopenCoordinator
import com.zynergylabs.forager.app.ui.map.fanout.MapTapHandler
import com.zynergylabs.forager.app.ui.map.fanout.MapTapSinks
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutBackHandler
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutHost
import com.zynergylabs.forager.app.ui.map.fanout.MarkerFanOutState
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.TAP_BOX_DP
import com.zynergylabs.forager.app.ui.map.layers.activeLayerCredits
import com.zynergylabs.forager.app.ui.map.layers.withUnavailableColourFieldsHidden
import com.zynergylabs.forager.app.ui.map.layers.layerPaintFor
import com.zynergylabs.forager.app.ui.map.layers.orderedLayers
import com.zynergylabs.forager.app.ui.map.layers.MapTapOutcome
import com.zynergylabs.forager.app.ui.map.layers.mapTapOutcome
import com.zynergylabs.forager.app.ui.map.layers.resolveTap
import com.zynergylabs.forager.app.ui.map.layers.tappableLayerIds
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng as MapLibreLatLng
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Shows the searched region as a map with a marker per real observation ([sightings]).
 *
 * [plannedTrips] draws a second, distinct marker per planned trip — a flag since colour build C2
 * (a diamond before it; `MarkerGlyphs.kt` has every marker's silhouette), to read as
 * different from the translucent sighting dots (density of what's been observed): a planned trip
 * is a place the user chose for themselves, not derived from observation history.
 *
 * [onLongPress] fires with the geographic point under a long-press, when a caller actually listens
 * for it — the intended consumer would turn it into a planned trip (via a date picker it owns; this
 * composable knows nothing about dates or persistence, only where the gesture happened). Wired
 * through `MapLibreMap.addOnMapLongClickListener` rather than a gesture detector on the
 * [AndroidView] itself, for the same reason the previous osmdroid implementation used
 * `MapEventsOverlay`: MapLibre's own touch handling already owns pan/zoom on this `MapView`, and a
 * second, independent gesture detector on top would race it for the same touch stream.
 *
 * **No production call site consumes this as of 2026-08-28.** Both `mapSlot(...)` call sites in
 * `AvailabilityScreen.kt` (`MapTab`, `CompactMapTab`) pass `{}` for [onLongPress] — the
 * trip-planning/log-a-find interaction this parameter used to drive now goes through panning the
 * camera, tapping the add (+) button, and confirming via
 * [com.zynergylabs.forager.app.ui.map.CentrePinLocationPickerOverlay] instead. Kept wired here deliberately,
 * not left by accident: the listener costs nothing while dormant, and whether to remove it or give
 * it a new consumer is a product decision this comment fix doesn't make.
 *
 * ## Migration note (osmdroid -> MapLibre, `docs/plans/maplibre-migration.md` §2b)
 *
 * This composable is a full rewrite, not a port. Per the plan: "the dot markers ... all become
 * style layers rather than osmdroid `Overlay`s." Every one of them is now a `GeoJsonSource` + a
 * `CircleLayer`/`LineLayer`/`SymbolLayer` in
 * [initializeOverlayLayers], with actual data pushed by [refreshOverlayData] — see that function's
 * doc comment for why the two are split. [Basemap]/[styleJsonFor] are the only pieces reused as-is;
 * everything else, including [zoomForRadiusKm]'s numbers and (until colour build C2 replaced them
 * with `MapPalette`'s per-role palette) the colour constants, was carried over
 * from the deleted osmdroid version deliberately (same reasoning, same visual intent), not reused
 * as code (the two rendering APIs share nothing at the type level). The one exception is the dash
 * pattern's ratio, later moved off the connector entirely and redesigned for its new home on the
 * breadcrumb trail — see [BREADCRUMB_DASH_PATTERN]'s own doc comment.
 *
 * **What is explicitly re-confirmed, and how, not just carried forward:**
 * - *The dashed line still reads as dashed.* `line-dasharray` is a real MapLibre style
 *   property (`PropertyFactory.lineDasharray`, verified against the pinned
 *   `org.maplibre.gl:android-sdk:13.5.0` artifact with `javap` — see this file's own history for
 *   what was checked), not a Canvas `PathEffect` workaround, so this is a supported case, not an
 *   emulation of one. `SightingsMapOverlayDataTest` asserts the built `LineLayer` actually carries a
 *   non-empty `line-dasharray` after every one of `basemap`'s possible values, the direct MapLibre
 *   analogue of what `SightingsMapBasemapSwapTest` asserted for osmdroid's `PathEffect`.
 * - *Content does not paint outside this composable's slot.* The `Modifier.clipToBounds()` below is
 *   kept, but the specific mechanism the old doc comment described — osmdroid's `TilesOverlay` and
 *   `PolyOverlayWithIW` drawing raw Canvas bitmaps/paths past the view's rectangle because
 *   `AndroidViewsHandler` doesn't clip a hosted `View` — does not apply to MapLibre's renderer.
 *   MapLibre's `MapView` (a `FrameLayout`, confirmed via `javap`) hosts a GL-backed render surface
 *   (`MapView.getRenderView()`) sized to the view's own layout bounds; a GL surface's framebuffer is
 *   bounded by construction — there is no analogue of a Canvas draw call painting past a rectangle
 *   nobody clipped, because the GPU only rasterizes into the pixels the surface actually owns. This
 *   is a reasoned architectural claim from the API shape, **not a hardware observation** — this
 *   session has no device to confirm it visually, so the clip stays in place regardless (removing a
 *   defensive modifier on an argument alone, with nothing to observe the result on, is not a trade
 *   this migration takes). Flagged in the handoff as still needing an eyes-on check.
 * - *The overlay colours.* Originally left unretouched on the theory that no new palette existed yet
 *   to re-check them against, since this migration keeps all four basemaps as the *same* raster tile
 *   services osmdroid used (see [Basemap]'s doc comment). **That theory held for the connector but
 *   not the sighting dots.** The first real hardware pass of this renderer (Portland-metro, USGS
 *   Topo) confirmed the dashed connector still reads as dashed — the colour question there really
 *   was moot. But the same screenshot found the sighting dots (bark brown, at the sighting layer's 0.7 fill opacity, now in `MAP_LAYER_REGISTRY`)
 *   an unresolvable smudge in a dense cluster near Lake Oswego, overlapping each other and the
 *   cluster badge — a real legibility failure this migration's "same raster tiles, same palette"
 *   reasoning didn't predict, because the failure is about density and boundary loss between
 *   overlapping translucent dots, not about the colour reading against a *different* basemap
 *   palette. Fixed with [SIGHTING_DOT_STROKE_WIDTH_PX] and MapPalette's `sightingDotStroke` below — a stroke,
 *   not full opacity: overlap density is itself information (a muddle of dots *is* the signal that
 *   several sightings cluster there), so the fix is boundary definition, not maximum contrast. Not
 *   yet re-confirmed on hardware, and not yet checked on the imagery basemap specifically.
 *
 * **Partially rebuilt:** the tap-to-see-title/snippet popup osmdroid's `Marker.title`/`.snippet`
 * gave for free had no style-layer equivalent — until [onSightingTap], added for a real observation
 * marker's info card, which does query the tapped point back (`MapLibreMap.queryRenderedFeatures`
 * against [SIGHTING_LAYER_ID] in the click listener below) and calls out with the matching
 * [Sighting]. Since map layers L0a every other tappable layer is queried too and the winner is
 * chosen by `resolveTap` (markers, then lines, then colour fields; the topmost layer within a
 * group); a winner that is not a sighting goes to [onFeatureTap] with its layer id, the
 * `featureId` property the pure builders write ([FEATURE_ID_PROPERTY]) and where it was tapped, and
 * since M1 nothing else fires for it (`mapTapOutcome`): the caller shows its bubble.
 */
@Composable
fun SightingsMap(
    region: Region,
    sightings: List<Sighting>,
    modifier: Modifier = Modifier,
    plannedTrips: List<PlannedTrip> = emptyList(),
    basemap: Basemap = Basemap.DEFAULT,
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    focusOverride: LatLng? = null,
    onLongPress: (LatLng) -> Unit = {},
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    onTap: () -> Unit = {},
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    onSightingTap: (Sighting, Offset, Float) -> Unit = { _, _, _ -> },
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    onCameraIdle: (LatLng) -> Unit = {},
    /**
     * Night mode: the basemap's colours inverted with hue kept (the V1 transform, `BasemapStyles.kt`'s
     * `NIGHT_RASTER_PAINT`); over the offline style, the same transform applied to its own layers
     * after it loads (`applyOfflineNightRecolour`). Every overlay marker follows it: the markers draw
     * from [MapPalette.forMode] of this flag (colour build C2).
     * It is the Night Maps setting and nothing else; no twilight trigger or long-press override
     * drives it (both were replaced by the setting, `MapPreferencesRepository`).
     *
     * Not the device's dark theme, and not derived from it: see [MapPalette]'s doc comment for
     * why that was tried, measured and abandoned.
     */
    nightMode: Boolean = false,
    /** See [MapRenderMode.nightModeLoaded]'s own doc comment: no style loads while this is `false`. */
    nightModeLoaded: Boolean = true,
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    breadcrumbPoints: List<LatLng> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapSlot]'s doc comment on this same parameter. */
    waypoints: List<Waypoint> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.resumeTrackingRequestId]'s own doc comment. */
    resumeTrackingRequestId: Int = 0,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.searchFrameRequestId]'s own doc comment. */
    searchFrameRequestId: Int = 0,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.resetOrientationRequestId]'s own doc comment. */
    resetOrientationRequestId: Int = 0,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.focusedObservationId]'s own doc comment. */
    focusedObservationId: Long? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.trackLiveLocation]'s own doc comment. */
    trackLiveLocation: Boolean = true,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.showSearchCentre]'s own doc comment. */
    showSearchCentre: Boolean = true,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.useOfflineTiles]'s own doc comment. */
    useOfflineTiles: Boolean = false,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.keptTrackPolylines]'s own doc comment. */
    keptTrackPolylines: List<RecordPolyline> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.findMarkers]'s own doc comment. */
    findMarkers: List<RecordPoint> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.photoMarkers]'s own doc comment. */
    photoMarkers: List<RecordPoint> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.offlineRegionCircles]'s own doc comment. */
    offlineRegionCircles: List<RecordRegion> = emptyList(),
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.bottomInset]'s own doc comment. */
    bottomInset: Dp = 0.dp,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.onUserCameraGesture]'s own doc comment. */
    onUserCameraGesture: () -> Unit = {},
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.layers]'s own doc comment. */
    layersState: MapLayersState = MapLayersState.DEFAULT,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.onFeatureTap]'s own doc comment. */
    onFeatureTap: (MapFeatureTap) -> Unit = {},
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.onCloseBubble]'s own doc comment. */
    onCloseBubble: () -> Unit = {},
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.focusedFeature]'s own doc comment. */
    focusedFeature: FocusedMapFeature? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.forecast]'s own doc comment. */
    forecast: MapForecastFeed? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.cameraRequest]'s own doc comment. */
    cameraRequest: MapCameraRequest? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.journalHighlights]'s own doc comment. */
    journalHighlights: JournalEntryHighlights = JournalEntryHighlights.NONE,
    /** See [com.zynergylabs.forager.app.ui.map.MapOverlayContent.route]'s own doc comment. */
    route: RouteOnMap? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.cameraMemory]'s own doc comment. */
    cameraMemory: MapCameraMemory? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.leavingTab]'s own doc comment. */
    leavingTab: Boolean = false,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.attributionEndInset]'s own doc comment. */
    attributionEndInset: Dp = 0.dp,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.attributionBottomInset]'s own doc comment. */
    attributionBottomInset: Dp? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.attributionKeepClear]'s own doc comment. */
    attributionKeepClear: androidx.compose.ui.geometry.Rect? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.attributionBesideCredit]'s own doc comment. */
    attributionBesideCredit: Boolean = false,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.returnMemory]'s own doc comment. */
    returnMemory: MapReturnMemory? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.backEnabled]'s own doc comment. */
    backEnabled: Boolean = true,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.navigationView]'s own doc comment. */
    navigationView: NavigationViewRequest? = null,
    /** See [com.zynergylabs.forager.app.ui.map.MapRenderMode.trueHeading]'s own doc comment. */
    trueHeading: State<TrueHeadingReading>? = null,
) {
    val context = LocalContext.current
    // Resources through LocalResources, which Compose re-reads on a configuration change, rather than
    // LocalContext.current.resources, which it does not (lint LocalContextResourcesRead; dispatch 2026-10-17-03).
    val resources = LocalResources.current

    // The marker palette is MapPalette.forMode(nightMode) on every basemap (colour build C2). It is chosen inside requestedMapStyle, below, rather than here, so that a
    // headless test reaches it (OfflineStyleSwapTest); the style effect reads requested.palette.
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        // MapLibre is initialised at application start; this call is idempotent and is here so a
        // map composed on a path that skipped it still fails loudly (logged) rather than crashing later.
        initializeMapLibre(context)
        MapView(context).apply { onCreate(null) }
    }

    // Latest callbacks, read from inside listeners registered exactly once in the DisposableEffect
    // below — without this indirection, a listener registered once would keep calling whichever
    // onTap/onLongPress lambda instance was current at registration time, not the caller's latest.
    val currentOnTapState = rememberUpdatedState(onTap)
    val currentOnTap by currentOnTapState
    val currentOnSightingTapState = rememberUpdatedState(onSightingTap)
    val currentOnSightingTap by currentOnSightingTapState
    val currentOnLongPressState = rememberUpdatedState(onLongPress)
    val currentOnLongPress by currentOnLongPressState
    val currentOnCameraIdleState = rememberUpdatedState(onCameraIdle)
    val currentOnCameraIdle by currentOnCameraIdleState
    val currentOnUserCameraGestureState = rememberUpdatedState(onUserCameraGesture)
    val currentOnUserCameraGesture by currentOnUserCameraGestureState
    val currentNavigationViewState = rememberUpdatedState(navigationView)
    val currentNavigationView by currentNavigationViewState
    // Ruling A (continuation 2026-09-28-432): the puck, and a facing-up map, take the app's own true
    // heading, not MapLibre's magnetic one. The heading handed in, else one made here from the
    // screen's compass (LocalMapCompass), else none, and MapLibre's own engine stays. Which applies
    // is fixed for a map's life: a map is given a heading or a compass from the start, or neither.
    val mapCompass = LocalMapCompass.current
    val headingSource: State<TrueHeadingReading>? = trueHeading
        ?: mapCompass?.let { rememberTrueHeading(it.compassProvider, it.computeTrueHeading, it.liveFix) }
    val appCompassEngine = remember(headingSource != null) { if (headingSource != null) TrueHeadingCompassEngine() else null }
    LaunchedEffect(appCompassEngine, headingSource) {
        val engine = appCompassEngine ?: return@LaunchedEffect
        val source = headingSource ?: return@LaunchedEffect
        snapshotFlow { source.value }.collect { engine.update(it) }
    }
    // Dispatch 2026-09-28-510 (the owner: "App feeds the dot"): the dot is drawn where the app has judged
    // the walker to be, the screen's position (LocalMapPosition), else MapLibre's own engine stays. Fixed
    // for a map's life, like the compass. The look is read here, and changes rarely: it is derived, so a new
    // fix of the same kind does not recompose this map.
    val mapPosition = LocalMapPosition.current
    val appLocationEngine = remember(mapPosition != null) { if (mapPosition != null) AppLocationEngine() else null }
    val positionLook = mapPosition?.let { position -> remember(position) { derivedStateOf { positionLookOf(position.shown.value, position.liveFix.value) } }.value } ?: PositionLook.PRECISE
    val currentPositionLookState = rememberUpdatedState(positionLook)
    val currentPositionLook by currentPositionLookState
    // The look last handed to MapLibre, so an unchanged look is not re-applied on every style load.
    val appliedPositionLook = remember { arrayOf(PositionLook.PRECISE) }
    // The navigation view's own camera mode changes, marked so the tracking listener below does not
    // read them as the user moving away from the view; and the mode the view last asked for.
    val navigationModeChange = remember { NavigationModeChange() }
    // The map's height, for the padding that puts the walker below the centre; recomputed on resize (fullscreen, rotation).
    val navigationMapHeightPxState = remember { mutableIntStateOf(0) }
    var navigationMapHeightPx by navigationMapHeightPxState
    val currentOnFeatureTapState = rememberUpdatedState(onFeatureTap)
    val currentOnFeatureTap by currentOnFeatureTapState
    val currentOnCloseBubbleState = rememberUpdatedState(onCloseBubble)
    val currentOnCloseBubble by currentOnCloseBubbleState
    // Read by the click listener (the draw order the tap precedence ranks against) and by each
    // style load (the order the layers are added in): both were registered or launched before a
    // later state change could otherwise reach them.
    //
    // Map layers L0b (B5, and the planner's ruling on F2): the state this map draws with is the one
    // it is given with every colour field its forecast feed does not name hidden. So a map with no
    // feed (the Cartography entry map, the centre-pin pickers, any map in a release build, whose store
    // never has data) never draws, credits or hit-tests a colour field, whatever the stored choice
    // says; the stored choice is not changed.
    val drawnLayersState = remember(layersState, forecast?.groupsByLayer) {
        withUnavailableColourFieldsHidden(layersState, MAP_LAYER_REGISTRY, forecast?.groupsByLayer?.keys.orEmpty())
    }
    val currentLayersStateState = rememberUpdatedState(drawnLayersState)
    val currentLayersState by currentLayersStateState
    // The map chrome's colour (the navigation bar's), read here because it follows the app's theme.
    val chromeColour = navigationBarContainerColor().toArgb()
    val currentChromeColourState = rememberUpdatedState(chromeColour)
    val currentChromeColour by currentChromeColourState
    // The chrome colour the fan's circle image holds in the loaded style: set when the style loads (addFanOutLayers registers it), so the colour effect
    // below registers it again only for a real change of colour.
    val fanCircleColour = remember { arrayOfNulls<Int>(1) }
    // Which camera moves are the location follower's and which are the user's or the app's own (dispatch 2026-09-28-380): MapLibre reports every
    // programmatic move with one reason, so each camera move this file makes marks itself here first, and the camera-idle listener clears the mark.
    val cameraMoveClassifier = remember { CameraMoveClassifier { task -> Handler(Looper.getMainLooper()).post(task) } }
    val currentForecastState = rememberUpdatedState(forecast)
    // Counts camera idles (map layers L0b, B5): the colour fields' cell feed below is keyed on it, so
    // the store is asked for the blocks in view each time the camera goes idle.
    val cameraIdleCountState = remember { mutableIntStateOf(0) }
    var cameraIdleCount by cameraIdleCountState
    // Read inside the click listener below (registered once, see that DisposableEffect's own
    // comment) so a tapped dot resolves against whichever sightings list is current, not whichever
    // one was in scope the moment the listener was registered.
    val currentSightingsState = rememberUpdatedState(sightings)
    val currentSightings by currentSightingsState
    // See MapOverlayContent.focusedObservationId's own doc comment for the dismiss-then-reappear
    // bug this closes: read fresh on every camera-idle event rather than latched into a local var
    // at tap time, so a caller-side dismissal (which only ever changes this parameter, never reaches
    // the click listener below) is visible here too.
    val currentFocusedObservationIdState = rememberUpdatedState(focusedObservationId)
    val currentFocusedObservationId by currentFocusedObservationIdState
    // M1 (F2): the point feature a caller's bubble is showing, and the lists it is looked up in, read
    // fresh on every camera idle for the reason currentFocusedObservationId is.
    val currentFocusedFeatureState = rememberUpdatedState(focusedFeature)
    val currentFocusedFeature by currentFocusedFeatureState
    // Part 1 layout fixes, item 4: read by the camera-idle listener (registered once) and by the first
    // style load, for the reason the lines above give.
    val currentCameraMemoryState = rememberUpdatedState(cameraMemory)
    val currentCameraMemory by currentCameraMemoryState
    // Item 8: read by the camera-idle listener (registered once), for the reason the lines above give.
    val currentReturnMemoryState = rememberUpdatedState(returnMemory)
    val currentReturnMemory by currentReturnMemoryState
    val currentPlannedTripsState = rememberUpdatedState(plannedTrips)
    val currentPlannedTrips by currentPlannedTripsState
    val currentWaypointsState = rememberUpdatedState(waypoints)
    val currentWaypoints by currentWaypointsState
    val currentFindMarkersState = rememberUpdatedState(findMarkers)
    val currentFindMarkers by currentFindMarkersState
    val currentPhotoMarkersState = rememberUpdatedState(photoMarkers)
    val currentPhotoMarkers by currentPhotoMarkersState
    val mapLibreMapState = remember { mutableStateOf<MapLibreMap?>(null) }
    var mapLibreMap by mapLibreMapState
    // Dispatch 2026-09-28-463: a nudge under the drag threshold gives, then springs back, while
    // navigating and following (see NudgeElastic.kt). Fed by the map view's touch listener below.
    val nudgeThresholdPx = NAVIGATION_NUDGE_THRESHOLD_DP * LocalDensity.current.density
    // Dispatch -477, Part A: where the current touch came down, for the release's log line.
    val touchDownAt = remember { FloatArray(2) }
    // Dispatch -477, Part B: a quick flick under the drag threshold springs back instead of ending
    // following through MapLibre's fling (see NudgeFlingGuard). Fed by the map view's touch listener below.
    val nudgeFlingGuard = remember {
        NudgeFlingGuard(
            object : FlingSwitch {
                override var enabled: Boolean
                    get() = mapLibreMap?.uiSettings?.isFlingVelocityAnimationEnabled ?: true
                    set(value) {
                        mapLibreMap?.uiSettings?.isFlingVelocityAnimationEnabled = value
                    }
            },
        )
    }
    // Built once: the factory below hands this instance to the map view's listener, which is attached once.
    val nudgeElastic = remember {
        NudgeElasticDriver(
            camera = object : NudgeCamera {
                override fun canGive(): Boolean {
                    val component = mapLibreMap?.locationComponent ?: return false
                    return component.isLocationComponentActivated &&
                        currentNavigationView?.following == true &&
                        navigationModeChange.expected != null &&
                        !navigationModeChange.transitioning &&
                        component.cameraMode != CameraMode.NONE
                }

                override fun showPadding(padding: DoubleArray) {
                    mapLibreMap?.locationComponent?.paddingWhileTracking(padding, 0L)
                }
            },
            thresholdPx = nudgeThresholdPx,
            viewTopPaddingPx = { navigationViewTopPaddingPx(navigationMapHeightPx) },
        )
    }
    DisposableEffect(nudgeElastic) { onDispose { nudgeElastic.cancel() } }
    // The Style instance from the most recently completed setStyle callback. Distinct from
    // "which style is currently applied" (appliedStyle, below) because this is what the data
    // effect keys on: a new Style object means new (empty) sources that need their content pushed.
    val loadedStyleState = remember { mutableStateOf<Style?>(null) }
    var loadedStyle by loadedStyleState

    // RECORD -761 (the owner: "Picture cover, this build (Recommended)"): a map coming back to the Maps tab draws the picture the
    // leaving map took of itself over its own first frames, until it has drawn a full frame of its own (MapReturnCover.kt, whose
    // header has the why, the when-not and the cost). Decided once, as this MapView is made; every outcome is logged.
    val coverPicture = remember {
        val memory = cameraMemory
        val cover = memory?.takeCover()
        val decision = coverDecision(
            pictureCamera = cover?.camera,
            saved = memory?.saved,
            target = region to focusOverride,
            searchFrameRequestId = searchFrameRequestId,
            appliedSearchFrameId = memory?.appliedSearchFrameId ?: 0,
            cameraRequestPending = cameraRequest != null,
        )
        if (memory != null) {
            Log.i(MAPS_COMEBACK_LOG_TAG, "cover: $decision" + (if (decision == CoverDecision.SHOW) "" else "; the map draws itself as before"))
        }
        if (decision == CoverDecision.SHOW) {
            cover!!.bitmap.asImageBitmap()
        } else {
            cover?.bitmap?.recycle()
            null
        }
    }
    val coverPhaseState = remember { mutableStateOf(if (coverPicture != null) CoverPhase.COVERING else CoverPhase.GONE) }
    var coverPhase by coverPhaseState
    LaunchedEffect(Unit) {
        if (coverPicture == null) return@LaunchedEffect
        delay(MAP_COVER_READY_CAP_MS)
        if (coverPhase == CoverPhase.COVERING) {
            Log.w(MAPS_COMEBACK_LOG_TAG, "cover: no full frame within $MAP_COVER_READY_CAP_MS ms; handed over anyway")
            coverPhase = CoverPhase.HANDED_OVER
        }
    }
    // The marker fan-out (dispatch 2026-09-28-197): its state, and the tap handler that opens and folds it,
    // which exists once the map is ready (getMapAsync below).
    val fanOut = remember { MarkerFanOutState() }
    val tapHandlerRef = remember { TapHandlerRef() }
    // Item 8 (dispatch -267): reopens the fan a user left; see FanReopenCoordinator.
    val fanReopen = remember {
        FanReopenCoordinator(
            handler = { tapHandlerRef.handler },
            takeKeys = { currentReturnMemory?.takeFanKeys() },
            onUnavailable = { count -> Log.w(FAN_OUT_RESTORE_TAG, "The fan of $count markers could not be reopened: fewer than two of them are still drawn.") },
        )
    }
    // The room a fan has: this view's bounds, and the controls over it that the screen has measured (MapKeepOut.kt).
    val fanSpace = rememberMapFanSpace()
    // Guards against re-running setStyle on every recomposition, mirroring the deleted osmdroid
    // applyBasemap's own name()-comparison guard and for the same reason: setStyle discards every
    // source and layer the previous style had, so calling it when nothing about the style actually
    // changed would flash the map to blank and rebuild everything for nothing. One value holding
    // basemap, palette, the offline flag (Stage 2e-ii) and effective night (colour build C1) rather
    // than the two separate appliedBasemap/appliedPalette vars it replaced — see needsStyleReload's
    // own doc comment for the "toggle does nothing" gap two separate comparisons left open, and
    // AppliedMapStyle.night's for the same gap night itself had until C1.
    //
    // The palette is in here for the reason the old appliedPalette existed: the overlay layers are
    // built once per style load with their colours baked into the layer properties, so a palette
    // change is only visible after those layers are rebuilt.
    //
    // Restyling is not free -- setStyle discards the LocationComponent state, which is why
    // activateLiveLocationIfPermitted has to run again below. Accepted because a basemap, palette
    // or offline switch is a deliberate, roughly once-per-outing action, not something that fires
    // on every recomposition.
    val appliedStyleState = remember { mutableStateOf<AppliedMapStyle?>(null) }
    var appliedStyle by appliedStyleState

    // What the camera was last deliberately moved to by the data+camera refresh effect below —
    // *not* re-derived from mapLibreMap.cameraPosition, which changes continuously while GPS
    // tracking owns the camera and would make this comparison meaningless. See
    // shouldMoveCameraToTarget's own doc comment for the hardware-reported bug this closes.
    val lastAppliedCameraTargetState = remember { mutableStateOf<Pair<Region, LatLng?>?>(null) }
    var lastAppliedCameraTarget by lastAppliedCameraTargetState

    // The id of the last MapRenderMode.cameraRequest this MapView applied (the entry map's opening
    // frame): kept with the MapView, so a request arriving again on a later recomposition, after a
    // fullscreen switch or a find overlay's Back, does not move the camera the user has since moved.
    val lastAppliedCameraRequestIdState = remember { mutableStateOf<String?>(null) }
    var lastAppliedCameraRequestId by lastAppliedCameraRequestIdState

    // RECORD -750, item 5: the last search frame this map applied, for a map with no camera memory; one with memory keeps it
    // there (MapCameraMemory.appliedSearchFrameId), so it outlives the MapView.
    val localAppliedSearchFrameId = remember { intArrayOf(0) }

    // Part 1 layout fixes, item 5: MapLibre's own attribution margins, read once the map is ready, which
    // the caption's insets are added to below.
    val attributionDefaultMarginsState = remember { mutableStateOf<IntArray?>(null) }
    var attributionDefaultMargins by attributionDefaultMarginsState

    // Item 1 (dispatch 2026-09-29-57, amendment -262): the map's own size, written by the layout below and read by the
    // attribution placement. Declared here, with the holder's other states, since UI split Phase 2.2 (dispatch 2026-10-19-29);
    // it was declared in the attribution block until then. A plain remember with a constant start, not an observer.
    val mapSizePxState = remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var mapSizePx by mapSizePxState
    // RECORD -954: the credit strip's laid-out width, written by the layout below and read by the attribution placement.
    // Moved up here for the same reason as mapSizePx.
    val creditWidthPxState = remember { mutableIntStateOf(0) }
    var creditWidthPx by creditWidthPxState

    // UI split Phase 2.2 (dispatch 2026-10-19-29): the map's state holder, over the very State objects remembered above, so
    // the effect groups can move to their own files and take it. Nothing is copied into it: each read is a getter and each
    // write a setter on the same State this function reads (SightingsMapState's own doc comment). Made once, for the life of
    // this MapView, like every unkeyed remember above. The effect groups moved to their own files take it.
    val sightingsMapState = remember {
        SightingsMapState(
            mapView = mapView,
            onTapState = currentOnTapState,
            onSightingTapState = currentOnSightingTapState,
            onLongPressState = currentOnLongPressState,
            onCameraIdleState = currentOnCameraIdleState,
            onUserCameraGestureState = currentOnUserCameraGestureState,
            navigationViewState = currentNavigationViewState,
            positionLookState = currentPositionLookState,
            onFeatureTapState = currentOnFeatureTapState,
            onCloseBubbleState = currentOnCloseBubbleState,
            layersStateState = currentLayersStateState,
            chromeColourState = currentChromeColourState,
            forecastState = currentForecastState,
            sightingsState = currentSightingsState,
            focusedObservationIdState = currentFocusedObservationIdState,
            focusedFeatureState = currentFocusedFeatureState,
            cameraMemoryState = currentCameraMemoryState,
            returnMemoryState = currentReturnMemoryState,
            plannedTripsState = currentPlannedTripsState,
            waypointsState = currentWaypointsState,
            findMarkersState = currentFindMarkersState,
            photoMarkersState = currentPhotoMarkersState,
            mapLibreMapState = mapLibreMapState,
            loadedStyleState = loadedStyleState,
            appliedStyleState = appliedStyleState,
            coverPhaseState = coverPhaseState,
            lastAppliedCameraTargetState = lastAppliedCameraTargetState,
            lastAppliedCameraRequestIdState = lastAppliedCameraRequestIdState,
            attributionDefaultMarginsState = attributionDefaultMarginsState,
            cameraIdleCountState = cameraIdleCountState,
            navigationMapHeightPxState = navigationMapHeightPxState,
            mapSizePxState = mapSizePxState,
            creditWidthPxState = creditWidthPxState,
            appliedPositionLook = appliedPositionLook,
            navigationModeChange = navigationModeChange,
            fanCircleColour = fanCircleColour,
            cameraMoveClassifier = cameraMoveClassifier,
            touchDownAt = touchDownAt,
            nudgeFlingGuard = nudgeFlingGuard,
            nudgeElastic = nudgeElastic,
            coverPicture = coverPicture,
            fanOut = fanOut,
            tapHandlerRef = tapHandlerRef,
            fanReopen = fanReopen,
            localAppliedSearchFrameId = localAppliedSearchFrameId,
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    mapLibreMap?.locationComponent?.onStart()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    mapLibreMap?.locationComponent?.onStop()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tearDownMap(object : MapTeardownTarget {
                override fun stopLocationUpdates() {
                    mapLibreMap?.locationComponent?.onStop()
                }

                override fun destroyLocationComponent() {
                    mapLibreMap?.locationComponent?.onDestroy()
                }

                override fun destroyMapView() = mapView.onDestroy()
            })
        }
    }

    // Re-projects the glyph a shown bubble belongs to and reports it, so the bubble follows the glyph. Called at
    // every camera idle, and (Part 2 follow-ups F1 item 1) when the map's own size changes: a device rotation
    // resizes the view without any camera idle, and the bubble used to stay at its portrait place until the
    // next pan. Reads the latest lists and callbacks through the current* states, as the idle listener always did.
    fun reanchorFocusedBubble(map: MapLibreMap) {
        // Keeps a shown observation bubble glued to its own marker's real screen position
        // across a pan/zoom/rotate — a hardware report asked for exactly this ("have it stay
        // there when we move the map, so we know which one it belongs to"), and re-projecting
        // whichever sighting currentFocusedObservationId currently names on every idle (the
        // same projection call the click listener above uses once, at tap time) is what
        // answers it without this composable needing to reimplement MapLibre's own
        // screen<->geo math. Resolved fresh against currentSightings/currentFocusedObservationId
        // rather than a sighting captured at tap time, so a dismissal that's since cleared
        // the caller's own focused id (see MapOverlayContent.focusedObservationId's doc
        // comment) is reflected here too instead of silently re-reviving a closed bubble. The
        // bearing carried alongside — also re-read fresh here, not just at tap time — is what
        // lets the caller keep the bubble's own placement direction correct (screen position
        // and orientation both live, not just position) after a rotate gesture; see
        // AnchoredAtScreenPoint's own doc comment in AvailabilityScreen.kt for what it does
        // with this value.
        currentFocusedObservationId
            ?.let { id -> currentSightings.firstOrNull { it.observationId == id } }
            ?.let { sighting ->
                val screenPoint = map.projection.toScreenLocation(MapLibreLatLng(sighting.lat, sighting.lng))
                currentOnSightingTap(sighting, Offset(screenPoint.x, screenPoint.y), map.cameraPosition.bearing.toFloat())
            }
        // M1 (F2): the same for a point feature's bubble. Its glyph's own position is re-found
        // in the lists this map draws (focusedFeaturePosition) and reported as a feature tap
        // at that point, so the caller's bubble follows the glyph. A record no longer drawn
        // is not re-fired, and a dismissed bubble has no focusedFeature, so nothing revives it.
        currentFocusedFeature?.let { focus ->
            focusedFeaturePosition(focus, currentPlannedTrips, currentWaypoints, currentFindMarkers, currentPhotoMarkers)?.let { at ->
                val glyph = map.projection.toScreenLocation(MapLibreLatLng(at.lat, at.lng))
                currentOnFeatureTap(
                    MapFeatureTap(focus.layerId, focus.featureId, Offset(glyph.x, glyph.y), map.cameraPosition.bearing.toFloat(), at),
                )
            }
        }
    }

    // Registered once per composition of this MapView, not per recomposition: getMapAsync's
    // callback fires exactly once for the life of the MapView, so there's no re-registration to
    // guard against the way applyBasemap's guard above is needed for setStyle.
    DisposableEffect(mapView) {
        // A style that cannot be loaded — the offline style with no region covering the camera
        // and no network, or a worker URL that moved — fails here rather than in setStyle's own
        // callback, which simply never fires. Logged, never silently swallowed (CLAUDE.md); the
        // message is MapLibre's own. addOnDidFailLoadingMapListener verified against the pinned
        // 13.5.0 artifact with javap, the same way every other SDK call in this file was.
        mapView.addOnDidFailLoadingMapListener { message ->
            Log.w(SIGHTINGS_MAP_TAG, "MapLibre failed to load the map style: $message")
        }
        // RECORD -752 (dispatch 2026-09-28-755, item 4): logging only, to find the S22's blank and magnified frame on a return
        // to Maps. The first few frames this MapView renders, with its size and camera then; the restore's own logs are below.
        // A MapView cannot run under Robolectric, so this is read from the phone's logcat (MAPS_COMEBACK_LOG_TAG).
        val firstFramesLogged = intArrayOf(0)
        val frameLogger = MapView.OnDidFinishRenderingFrameListener { fully, _, _ ->
            // RECORD -761: the first full frame after the style has loaded (the camera was restored before it) takes over from
            // the cover.
            if (fully && loadedStyle != null && coverPhase == CoverPhase.COVERING) {
                coverPhase = CoverPhase.HANDED_OVER
                Log.i(MAPS_COMEBACK_LOG_TAG, "cover: handed over at the first full frame, zoom=${mapLibreMap?.cameraPosition?.zoom}")
            }
            if (firstFramesLogged[0] < MAPS_COMEBACK_FRAMES_LOGGED) {
                firstFramesLogged[0]++
                val camera = mapLibreMap?.cameraPosition
                Log.i(MAPS_COMEBACK_LOG_TAG, "frame ${firstFramesLogged[0]} fully=$fully view=${mapView.width}x${mapView.height} render=${mapView.renderView.width}x${mapView.renderView.height} zoom=${camera?.zoom} target=${camera?.target} style=${loadedStyle != null}")
            }
        }
        mapView.addOnDidFinishRenderingFrameListener(frameLogger)
        mapView.getMapAsync { map ->
            // Marker fan-out (dispatch 2026-09-28-197): the tap is decided by MapTapHandler, which is the
            // resolution this listener used to do inline (resolveTap over every tappable layer, a point
            // query then a TAP_BOX_DP box, then mapTapOutcome), with the fan-out in front of and behind it.
            // The four outcomes come back through the sinks below, unchanged from what the listener did.
            val handler = MapTapHandler(
                fan = fanOut,
                probe = MapLibreProbe(map, resources.displayMetrics.density) { key ->
                    // Where a fanned record is now, from the lists the map draws (the projection of it is the probe's): the
                    // same lookup a shown bubble's re-anchoring uses. Null once the record is no longer drawn.
                    focusedFeaturePosition(FocusedMapFeature(key.layerId, key.featureId), currentPlannedTrips, currentWaypoints, currentFindMarkers, currentPhotoMarkers)
                },
                space = fanSpace,
                // One layer at a time (amendment -255): a bubble showing on a sighting or a point glyph, which is
                // what a fanned marker's bubble is, means an empty-map tap closes it and leaves the fan.
                bubbleOpen = { currentFocusedObservationId != null || currentFocusedFeature != null },
                drawOrder = { orderedLayers(MAP_LAYER_REGISTRY, currentLayersState) },
                // A record on a layer switched off is not drawn, so it is not a fan member.
                warn = { message -> Log.w(SIGHTINGS_MAP_TAG, message) },
                layerDrawn = { id -> MAP_LAYER_REGISTRY.firstOrNull { it.id == id }?.let { layerPaintFor(it, currentLayersState).visible } ?: false },
                sinks = object : MapTapSinks {
                    override fun onPlainTap() = currentOnTap()

                    override fun onSightingTap(observationId: Long?, xPx: Float, yPx: Float) {
                        // The sighting path as before L0a: the dot's observationId looked back up in
                        // the current list, the bubble anchored at the tap point, onTap when the id no
                        // longer resolves.
                        val tappedSighting = observationId?.let { id -> currentSightings.firstOrNull { it.observationId == id } }
                        if (tappedSighting != null) {
                            currentOnSightingTap(tappedSighting, Offset(xPx, yPx), map.cameraPosition.bearing.toFloat())
                        } else {
                            currentOnTap()
                        }
                    }

                    override fun onFeatureTap(layerId: String, featureId: String, xPx: Float, yPx: Float, at: LatLng) {
                        // M1 (owner's ruling 1, "Bubble only"): a feature tap opens its bubble and nothing
                        // else, as a sighting tap already did.
                        currentOnFeatureTap(MapFeatureTap(layerId, featureId, Offset(xPx, yPx), map.cameraPosition.bearing.toFloat(), at))
                    }

                    override fun onCloseBubble() = currentOnCloseBubble()

                    override fun onUnidentifiedFeature(layerId: String) {
                        // Logged, never silent: a feature with no id cannot name its record, so no
                        // bubble can show, and the tap is taken as a plain one.
                        Log.w(SIGHTINGS_MAP_TAG, "Tapped a feature on $layerId with no $FEATURE_ID_PROPERTY; taken as a plain tap.")
                        currentOnTap()
                    }
                },
            )
            tapHandlerRef.handler = handler
            map.addOnMapClickListener { latLng ->
                // toScreenLocation confirmed via javap against the pinned org.maplibre.gl:android-sdk:13.5.0.
                val screenPoint = map.projection.toScreenLocation(latLng)
                handler.onMapTap(LatLng(latLng.latitude, latLng.longitude), screenPoint.x, screenPoint.y)
                // false: unconsumed, matching the deleted osmdroid MapEventsOverlay's
                // singleTapConfirmedHelper — a plain tap isn't meant to swallow the event.
                false
            }
            map.addOnMapLongClickListener { latLng ->
                currentOnLongPress(LatLng(latLng.latitude, latLng.longitude))
                true
            }
            // Only a touch counts: REASON_API_GESTURE is dispatched from MapGestureDetector's
            // listeners alone, every programmatic move from Transform as REASON_API_ANIMATION —
            // see MapRenderMode.onUserCameraGesture's doc comment for the javap check.
            // Dispatch 2026-09-28-430, ruling C: while navigating the map turns with the compass and
            // rarely rests, so a shown bubble is kept on its glyph on every camera move, not only at
            // idle. Outside navigation it is re-anchored at idle as before.
            map.addOnCameraMoveListener {
                if (currentNavigationView != null) reanchorFocusedBubble(map)
                navigationModeChange.noteCameraMove(map) // dispatch -470, Part A: logging only
            }
            // Dispatch -477, Part A: every fling MapLibre reports while navigating, with the camera mode then.
            // Logging only.
            map.addOnFlingListener {
                if (currentNavigationView != null) {
                    Log.i(NAVIGATION_VIEW_LOG_TAG, "fling reported: mode=${map.locationComponent.takeIf { it.isLocationComponentActivated }?.cameraMode}")
                }
            }
            map.addOnCameraMoveStartedListener { reason ->
                navigationModeChange.noteCameraMoveStarted(reason) // dispatch -470, Part A: logging only
                // A fanned stack folds on a camera move the user made or asked for, a gesture or the app's own; it stays open when the map re-centres
                // itself while it follows the location (dispatch 2026-09-28-380: the user did not mean that move, and the fan travels with the map).
                val following = map.locationComponent.isLocationComponentActivated && map.locationComponent.cameraMode != CameraMode.NONE
                val cause = cameraMoveClassifier.classify(isUserCameraGesture(reason), following)
                if (cause == CameraMoveCause.UNKNOWN) {
                    Log.w(SIGHTINGS_MAP_TAG, "A camera move (reason $reason) is not a touch, not marked as the app's and not the location follower's; the open fan is folded as it always was.")
                }
                tapHandlerRef.handler?.onCameraMoveStarted(cause)
                if (isUserCameraGesture(reason)) currentOnUserCameraGesture()
            }
            // OnCameraIdleListener.onCameraIdle() takes no argument (verified via javap against
            // the pinned org.maplibre.gl:android-sdk:13.5.0 artifact — MapLibreMap$OnCameraIdleListener
            // declares only `void onCameraIdle()`), so the position has to be read back explicitly
            // via getCameraPosition() inside the callback, not received as a parameter the way
            // addOnMapLongClickListener's latLng is.
            map.addOnCameraIdleListener {
                cameraMoveClassifier.onCameraIdle()
                cameraIdleCount++
                // CameraPosition.target is declared `LatLng?` in the pinned SDK itself (verified via
                // javap: the vendor's own constructor carries an org.jetbrains.annotations.Nullable
                // on this parameter) — null before the map has finished laying out a first camera
                // position, which an idle event can fire for. Nothing to report yet in that case, so
                // this skips the callback rather than fabricating a coordinate.
                map.cameraPosition.target?.let { target ->
                    currentOnCameraIdle(LatLng(target.latitude, target.longitude))
                }
                // Part 1 layout fixes, item 4: the camera this map settled on, for the map that comes
                // back after a tab change. Not before this MapView's first style has loaded: until then
                // the camera is the SDK's default, and the restore has yet to read what was saved.
                if (loadedStyle != null) {
                    val camera = map.cameraPosition
                    camera.target?.let { target ->
                        currentCameraMemory?.saved = MapCameraSnapshot(
                            target = LatLng(target.latitude, target.longitude),
                            zoom = camera.zoom,
                            bearing = camera.bearing,
                            tilt = camera.tilt,
                            following = map.locationComponent.isLocationComponentActivated &&
                                map.locationComponent.cameraMode != CameraMode.NONE,
                            appliedTarget = lastAppliedCameraTarget,
                        )
                    }
                }
                // Item 8 (dispatch 2026-09-29-57, amendment -262, "Remember and reopen"): the fan a user left when they opened a
                // find on the Journal reopens here, at the first idle after this map's style has loaded (the camera memory's
                // restore has been applied by then, and the style load's own content fold, onContentChanged, has run), through
                // openFanFor. A reopen that comes to nothing is logged, never silent. Whether a later camera move or content
                // change folds it again is device-only: a real MapView cannot be built under Robolectric.
                fanReopen.onCameraIdle(loadedStyle != null)
                // Keeps a shown bubble glued to its glyph across a pan, zoom or rotate gesture: see
                // reanchorFocusedBubble, which a rotation of the device also calls (onViewportResized below).
                reanchorFocusedBubble(map)
            }
            // MapLibre's own tap-to-reveal attribution control defaults to bottom-start — the same
            // corner this composable's own always-visible Basemap.attribution caption occupies (see
            // below), so the two painted over each other: MapLibre's control and this app's own
            // basemap-specific credit, both real and both required, neither readable. Moved to
            // bottom-end so each has its own corner rather than trying to stack them, which would
            // need a margin computed from this caption's own (basemap-dependent) text height to stay
            // correct — confirmed via javap against the pinned org.maplibre.gl:android-sdk artifact
            // that UiSettings exposes setAttributionGravity/setAttributionMargins for exactly this.
            map.uiSettings.setAttributionGravity(Gravity.BOTTOM or Gravity.END)
            // Part 1 layout fixes, item 5: MapLibre's own margins, which the insets below are added to.
            attributionDefaultMargins = map.uiSettings.let {
                intArrayOf(it.attributionMarginLeft, it.attributionMarginTop, it.attributionMarginRight, it.attributionMarginBottom)
            }
            // MapLibre's own compass view (a floating circular reset-to-north control it draws
            // itself, top-right by default) is replaced by the map icon bar's own orientation-
            // reset control — a real hardware report found the two overlapping, and the SDK's own
            // compass has no callback this project could otherwise hook into for the icon bar's
            // matching entry, only a tap target of its own with fixed positioning. Disabled here
            // rather than repositioned: keeping both would mean two controls that do the same
            // thing, in two different places, on the same screen.
            map.uiSettings.isCompassEnabled = false
            mapLibreMap = map
        }
        onDispose { mapView.removeOnDidFinishRenderingFrameListener(frameLogger) }
    }

    // Style swap: basemap, palette, the offline style (Stage 2e-ii), or effective night (colour
    // build C1: nightMode is a key, and AppliedMapStyle.night is compared). Keyed on the inputs
    // rather than driven from an AndroidView update block: setStyle is asynchronous (its callback
    // is where the new style's sources/layers can actually be added), which the old synchronous
    // update-block shape has no equivalent of.
    //
    // The offline branch loads OFFLINE_STYLE_URL by URI (see MapStyleSource's own doc comment for
    // why by URI and never fromJson), the exact string every region was downloaded against, so
    // MapLibre's offline database can serve the style document, its TileJSON and its tiles from
    // the store. Nothing else about the swap differs from a basemap swap: initializeOverlayLayers
    // re-adds every overlay in the callback exactly as it does for a basemap change, and the
    // data+camera refresh effect below re-pushes their content keyed on loadedStyle. One thing
    // deliberately left as the user will see it (owner ruling, 2e-ii: report, do not fix): the max
    // zoom preference stays the *basemap's* (17 for OpenTopoMap) over a store that stops at zoom
    // 15 — vector tiles overzoom cleanly, per OfflineMapRepository.MAX_ZOOM's doc comment, so the
    // user can zoom past the data's own ceiling without a hard stop. 2e-ii also left night inert
    // on the offline style; since colour build C1 it is applied by recolouring the loaded style's
    // own layers in the setStyle callback below (mapStyleSourceFor's doc comment).
    //
    // A style that fails to load (offline with no region covering the camera, a worker URL that
    // moved, a cold store) never reaches this callback, so appliedStyle and loadedStyle keep their
    // previous values and the map shows MapLibre's own blank. That failure is logged by the
    // OnDidFailLoadingMapListener registered in the DisposableEffect above, never swallowed; what
    // the user should be *told* in that state is a decision the pre-build report lists and this
    // dispatch did not make.
    LaunchedEffect(mapLibreMap, basemap, useOfflineTiles, nightMode, nightModeLoaded) {
        val map = mapLibreMap ?: return@LaunchedEffect
        // null until the Night Maps preference has loaded (the cold-launch gate): the effect
        // relaunches when nightModeLoaded turns true, and the first style it requests is then the
        // right one. The map shows MapLibre's own blank until then.
        val requested = requestedMapStyle(
            basemap = basemap,
            useOfflineTiles = useOfflineTiles,
            nightMode = nightMode,
            nightModeLoaded = nightModeLoaded,
        ) ?: return@LaunchedEffect
        if (!needsStyleReload(appliedStyle, requested)) return@LaunchedEffect
        // Captured before setStyle below discards the LocationComponent entirely (see
        // activateLiveLocationIfPermitted's own doc comment on why re-activation is needed at
        // all) — null only the very first time this composable ever activates the puck;
        // CameraMode.NONE if the user had already broken tracking by panning/zooming;
        // CameraMode.TRACKING if they hadn't. Restoring exactly this, rather than always
        // re-forcing TRACKING, is the fix for a real hardware report: switching basemap (or
        // toggling night mode, which goes through this same style-swap path on every basemap)
        // was recentering the
        // map on the user's location even after they had deliberately panned away — the
        // GPS/locate-me icon is the control for that, not this one.
        val previousCameraMode = if (map.locationComponent.isLocationComponentActivated) {
            map.locationComponent.cameraMode
        } else {
            null
        }
        // Part 1 layout fixes, item 4 (planner message 2026-09-28-98): a map that has left composition
        // with its tab and come back is a new MapView, which would open where a fresh map does (the
        // region move below, at zoomForRadiusKm, and the first activation's zoom-in). Its first style
        // load restores the camera the user left instead: target, zoom, bearing and tilt, the region
        // target taken as already applied (so the region move does not run, while a new search still
        // does), and the tracking mode (so the zoom-in does not run). Only on this MapView's first
        // style; a later style swap keeps its own camera, as before.
        val cameraRestore = cameraRestoreFor(if (appliedStyle == null) currentCameraMemory?.saved else null, previousCameraMode)
        cameraRestore?.let {
            Log.i(MAPS_COMEBACK_LOG_TAG, "restore before the style: view=${mapView.width}x${mapView.height} to zoom=${it.zoom} target=${it.target} (camera was zoom=${map.cameraPosition.zoom})")
            cameraMoveClassifier.markAppMove()
            applyCameraRestore(map, it)
        }
        map.setMaxZoomPreference(basemap.maxZoom.toDouble())
        val builder = when (val source = mapStyleSourceFor(basemap, night = requested.night, useOfflineTiles = useOfflineTiles)) {
            is MapStyleSource.Json -> Style.Builder().fromJson(source.json)
            is MapStyleSource.Uri -> Style.Builder().fromUri(source.uri)
        }
        map.setStyle(builder) { style ->
            // The symbol fade off for the whole style, before anything is drawn on it (PlacementTransitions.kt).
            disableSymbolFade(style)
            // Offline night (colour build C1 (d)): the offline style has no raster layer for
            // NIGHT_RASTER_PAINT to act on, so its own layers are recoloured here, after it loads
            // from its one URL and before the overlays are added, so the overlays' own line and
            // fill layers are never touched. Day, or night off, restyles nothing: the style came
            // fresh from its URI.
            if (requested.useOfflineTiles && requested.night) applyOfflineNightRecolour(style)
            initializeOverlayLayers(
                style,
                density = resources.displayMetrics.density,
                palette = requested.palette,
                drawOrder = orderedLayers(MAP_LAYER_REGISTRY, currentLayersState),
                layersState = currentLayersState,
                chromeColour = currentChromeColour,
            )
            fanCircleColour[0] = currentChromeColour
            // Dispatch 2026-09-28-510: the approximate and last known dots, by name, before the dot is activated
            // below; setStyle discards images with everything else, so on every style load.
            if (appLocationEngine != null) addPositionLookImages(style, context)
            // The data+camera refresh effect below re-pushes every source right after this, keyed
            // on loadedStyle among other things — including the sighting source, with "selected"
            // baked in from whatever focusedObservationId is current at that point. Nothing here
            // needs to seed it separately.
            // Item 4: again once the style has loaded, in case a style's own default camera replaced it,
            // and the region target recorded before loadedStyle wakes the data+camera effect below.
            cameraRestore?.let {
                Log.i(MAPS_COMEBACK_LOG_TAG, "restore on the style load: view=${mapView.width}x${mapView.height}, camera was zoom=${map.cameraPosition.zoom} target=${map.cameraPosition.target}")
                cameraMoveClassifier.markAppMove()
                applyCameraRestore(map, it)
                lastAppliedCameraTarget = it.appliedTarget
            }
            appliedStyle = requested
            loadedStyle = style
            // setStyle discards the previous style's LocationComponent state the same way it does
            // this composable's own layers (see initializeOverlayLayers' own doc comment on why
            // that function re-runs here) — so the live-location "puck" needs the same
            // re-activate-on-every-new-style treatment. Guarded by trackLiveLocation — see that
            // parameter's own doc comment for why a historical-place map instance must never seize
            // the camera for the device's current location at all.
            if (trackLiveLocation) {
                cameraMoveClassifier.markAppMove() // activating (or re-activating, after a style swap) sets the camera mode and may ease the zoom
                navigationModeChange.byTheApp {
                    activateLiveLocationIfPermitted(map, style, context, restoreCameraMode = cameraRestore?.cameraMode ?: previousCameraMode, compassEngine = appCompassEngine, locationEngine = appLocationEngine, look = currentPositionLook)
                    appliedPositionLook[0] = currentPositionLook
                    Log.i(POSITION_DOT_LOG_TAG, "activated on a style load with look $currentPositionLook")
                }
                navigationModeChange.listenTo(map) { currentNavigationView }
                navigationModeChange.gestureProtection = false
            }
        }
    }

    // Data + camera refresh. Runs on every relevant prop change *and* whenever a new style just
    // finished loading (loadedStyle changing is what makes this re-populate a freshly blank style
    // after a basemap swap) — the same "rebuild content every update, regardless of why the update
    // fired" behaviour the deleted osmdroid version had in its single `update` block, split here
    // because MapLibre's own API separates "style ready" from "camera/property changed".
    // Dispatch -497: the way back, pushed whenever it or the style changes (a new style starts empty).
    LaunchedEffect(loadedStyle, route) {
        loadedStyle?.let { updateRouteHomeLayers(it, route) }
    }
    LaunchedEffect(
        loadedStyle, region, sightings, plannedTrips, focusOverride, breadcrumbPoints, waypoints, focusedObservationId,
        keptTrackPolylines, findMarkers, photoMarkers, offlineRegionCircles, showSearchCentre, cameraRequest, journalHighlights,
        searchFrameRequestId,
    ) {
        val style = loadedStyle ?: return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect
        refreshOverlayData(
            style, region, sightings, plannedTrips, breadcrumbPoints, waypoints, focusedObservationId,
            keptTrackPolylines, findMarkers, photoMarkers, offlineRegionCircles, showSearchCentre, journalHighlights,
        )

        // Once the live-location "puck" is actively tracking (the default once permission is
        // granted — see activateLiveLocationIfPermitted), it owns the camera continuously, on its
        // own internal update loop, independent of recomposition. Jumping the camera to the search
        // region here too — on every sightings/breadcrumb update this effect already keys on,
        // which includes roughly every 15s while a track is recording — would fight it, snapping the
        // view back to the search center out from under a walker watching their live position. Once
        // a pan/zoom/the CameraMode.NONE break in activateLiveLocationIfPermitted's own doc comment
        // drops tracking, this resumes controlling the camera exactly as it did before that existed.
        val isGpsTrackingBefore = map.locationComponent.isLocationComponentActivated &&
            map.locationComponent.cameraMode != CameraMode.NONE
        val target = region to focusOverride
        // RECORD -750, item 5 (the owner: "Yes, fly to the search (Recommended)"): a new search's frame stops the camera
        // following the GPS fix, which would otherwise block the move below, and frames the searched region. Once per id.
        val appliedSearchFrameId = currentCameraMemory?.appliedSearchFrameId ?: localAppliedSearchFrameId[0]
        val searchFrame = searchFrameMove(isGpsTrackingBefore, searchFrameRequestId, appliedSearchFrameId)
        if (searchFrame != SearchFrameMove.NONE) {
            cameraMoveClassifier.markAppMove()
            if (searchFrame == SearchFrameMove.STOP_FOLLOWING_AND_FRAME) map.locationComponent.cameraMode = CameraMode.NONE
            map.cameraPosition = CameraPosition.Builder()
                .target(MapLibreLatLng(region.lat, region.lng))
                .zoom(zoomForRadiusKm(region.radiusKm))
                .build()
            lastAppliedCameraTarget = target
            localAppliedSearchFrameId[0] = searchFrameRequestId
            currentCameraMemory?.appliedSearchFrameId = searchFrameRequestId
            Log.i(SEARCH_FRAME_LOG_TAG, "search frame $searchFrameRequestId applied ($searchFrame) at ${region.lat}, ${region.lng}")
            return@LaunchedEffect
        }
        val isGpsTracking = isGpsTrackingBefore
        // A one-shot frame (the entry map's opening frame) stands in for this target's move: the
        // target is recorded as applied too, so the region move below does not follow it and undo it.
        // A later target change (a locate-me pan) still moves the camera, as before.
        val frameWanted = cameraRequest != null && shouldApplyCameraRequest(isGpsTracking, cameraRequest, lastAppliedCameraRequestId)
        if (frameWanted) cameraMoveClassifier.markAppMove()
        val frameApplied = frameWanted && applyCameraFrame(map, cameraRequest.frame, resources.displayMetrics.density)
        if (frameApplied) {
            lastAppliedCameraRequestId = cameraRequest.id
            lastAppliedCameraTarget = target
        } else if (shouldMoveCameraToTarget(isGpsTracking, target, lastAppliedCameraTarget)) {
            val center = MapLibreLatLng(region.lat, region.lng)
            // focusOverride pans the camera without moving the search-location marker or the
            // zoom-from-radius heuristic below, both of which stay anchored to region — see
            // MapSlot's doc comment on this parameter for why the two are kept independent.
            val cameraTarget = focusOverride?.let { MapLibreLatLng(it.lat, it.lng) } ?: center
            cameraMoveClassifier.markAppMove()
            map.cameraPosition = CameraPosition.Builder()
                .target(cameraTarget)
                .zoom(zoomForRadiusKm(region.radiusKm))
                .build()
            lastAppliedCameraTarget = target
        }
    }

    // Layer visibility and opacity (map layers L0a, A3): each native layer's `visibility` layout
    // property and its opacity paint properties set from layersState through layerPaintFor, on the
    // loaded style's own layers — no setStyle, so nothing is rebuilt. Keyed on loadedStyle as well,
    // so a freshly loaded style gets the current state; initializeOverlayLayers has already built
    // each layer with it, so for that case this re-sets the same values.
    // A fanned stack folds only when what the map draws changes a member (a record gone or moved, its layer switched off); a change
    // that leaves its members alone keeps it (intent 2026-09-28-274, "Fold only if members change"), and so does a replaced style
    // (dispatch 2026-09-28-279): the effects below draw the fan's layers, frame, circle colour and fade again on the new one.
    // Not when a bubble opens (focusedObservationId, focusedFeature): tapping a fanned marker keeps the fan up.
    LaunchedEffect(loadedStyle, sightings, plannedTrips, waypoints, findMarkers, photoMarkers, drawnLayersState, journalHighlights) {
        fanReopen.onContentEffect(loadedStyle != null, loadedStyle)
    }

    // Draws the fan: pushes the copies and their legs at every step of its progress (fanFrameCollections), and hides the
    // originals of the fanned markers while it is up, but not before the renderer reports the copies drawn: hiding first
    // left a frame or two with neither on screen, the blink at the open's start (dispatch 2026-09-28-369, amendment -371).
    // The order is FanOutHideGate's; the signal is FanOutRenderSignal's. Device-only: see FanOutLayers.kt.
    // The gate outlives a restart of this effect (it is keyed on focusedObservationId too, so tapping a fanned sighting restarts it with
    // the fan up) and is new only with a new style, whose layers start unfiltered: a restart then asks it again for the same members and
    // changes nothing, and the originals stay hidden (the planner's review of a5185a2f).
    val fanHideGate = remember(loadedStyle) { FanOutHideGate() }
    LaunchedEffect(loadedStyle, mapLibreMap, focusedObservationId) {
        val style = loadedStyle ?: return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect
        val density = resources.displayMetrics.density
        val effectScope = this
        val gate = fanHideGate
        var waiting: Job? = null
        var clearing: Job? = null
        var hiddenFor: List<FanMember>? = null
        // True to start with, so a restart that finds the fan folding drops a wait that was still pending (gate.onFold) on its first pass.
        var wasOpen = true
        snapshotFlow { Triple(fanOut.members, fanOut.progress, fanOut.wantOpen) }.collect { (members, progress, wantOpen) ->
            if (hiddenFor !== members) {
                hiddenFor = members
                waiting?.cancel()
                clearing?.cancel()
                val step = gate.onMembers(members, spread = progress > 0f)
                applyFanOutHiding(style, step.hide)
                if (step.reveal.isNotEmpty()) {
                    // A release: the originals are shown again, but the copies stay (they stand on them at progress 0) until the
                    // renderer reports the originals drawn, so the stack is never empty for a frame at the fold's end.
                    clearing = effectScope.launch {
                        clearCopiesWhenOriginalsDrawn(map, mapView, step.reveal) {
                            pushFanFrame(style, fanFrameCollections(emptyList(), { fanMemberLatLng(map, it, 0f, density) }, focusedObservationId, 0f, drawOrder = orderedLayers(MAP_LAYER_REGISTRY, currentLayersState)))
                        }
                    }
                }
                if (step.awaiting) {
                    waiting = effectScope.launch {
                        hideWhenCopiesDrawn(style, map, mapView, gate, members, step.generation) { fanOut.progress >= 1f }
                    }
                }
            }
            if (wasOpen && !wantOpen) {
                // Folding: a hide still waiting is dropped, so the originals stay shown until the release shows everything.
                gate.onFold()
                waiting?.cancel()
            }
            wasOpen = wantOpen
            if (members.isEmpty() && clearing?.isActive == true) return@collect // the copies stay until the originals are drawn
            pushFanFrame(
                style,
                fanFrameCollections(
                    members, { fanMemberLatLng(map, it, progress, density) }, focusedObservationId, progress,
                    drawOrder = orderedLayers(MAP_LAYER_REGISTRY, currentLayersState),
                ),
            )
        }
    }

    // Item 8: the open fan's member keys, written where "Open in Journal" reads them (a closed fan writes an empty list).
    // A map that is left (a tab switch) writes nothing on the way out, so the last list survives until the tap that read it.
    LaunchedEffect(returnMemory) {
        val memory = returnMemory ?: return@LaunchedEffect
        snapshotFlow { if (fanOut.isOpen) fanOut.members.map { it.key } else emptyList() }.collect { keys -> memory.openFanKeys = keys }
    }

    // Item 2 (dispatch 2026-09-28-265): while a fan is open the marker icons outside it draw at 80% of
    // what they drew. The one writer of these opacities, so the fade composes with the Layers sheet's
    // state (fanFadedPaint multiplies the resolved paint) and the restore on folding is the state's own
    // value. Device-only: see FanClarity.kt.
    LaunchedEffect(loadedStyle, drawnLayersState) {
        val style = loadedStyle ?: return@LaunchedEffect
        snapshotFlow { fanOut.isOpen }.collect { fanOpen ->
            MAP_LAYER_REGISTRY.forEach { spec -> applyLayerPaint(style, fanFadedPaint(spec, layerPaintFor(spec, drawnLayersState), fanOpen)) }
        }
    }

    // Item 3: the circles' colour is the map chrome's, which follows the app's theme, so a theme change
    // recolours them on the loaded style (the layer was built with the colour current at style load).
    LaunchedEffect(loadedStyle, chromeColour) {
        val style = loadedStyle ?: return@LaunchedEffect
        if (fanCircleNeedsRecolour(fanCircleColour[0], chromeColour)) {
            applyFanCircleStyle(style, chromeColour, resources.displayMetrics.density)
            fanCircleColour[0] = chromeColour
        }
    }

    // Colour-field cells (map layers L0b, B5): MapForecastCells.kt.
    MapForecastCells(state = sightingsMapState, forecast = forecast)

    // The location dot (dispatch 2026-09-28-510) and the locate button: MapLocationDot.kt.
    MapLocationDot(
        state = sightingsMapState,
        appLocationEngine = appLocationEngine,
        mapPosition = mapPosition,
        positionLook = positionLook,
        trackLiveLocation = trackLiveLocation,
        resumeTrackingRequestId = resumeTrackingRequestId,
        context = context,
        appCompassEngine = appCompassEngine,
    )

    // Dispatch 2026-09-28-430 (plan task T22): the navigation view: MapNavigationView.kt.
    MapNavigationView(
        state = sightingsMapState,
        navigationView = navigationView,
        basemap = basemap,
        trackLiveLocation = trackLiveLocation,
        context = context,
    )

    // Part 1 layout fixes, item 5, and RECORD -954: MapLibre's attribution button and logo: MapAttributionInsets.kt.
    MapAttributionInsets(
        state = sightingsMapState,
        attributionBottomInset = attributionBottomInset,
        bottomInset = bottomInset,
        attributionEndInset = attributionEndInset,
        attributionKeepClear = attributionKeepClear,
        attributionBesideCredit = attributionBesideCredit,
    )

    // The map icon bar's orientation-reset control — MapLibre's own native compass view is
    // disabled above (see the DisposableEffect(mapView) block's own comment), so this is the only
    // way to straighten the map back to north once a rotate gesture has turned it. easeCamera, not
    // an instant jump, matching this map's other camera moves; bearing only, not target or zoom.
    // Ruling D (continuation 2026-09-28-432): while navigating and following, a tap here is a move
    // away from the view: north-up, still following, and "Return to Route" shown. The first run
    // (this map's own creation) is not a tap, and keeps its old behaviour.
    val lastResetOrientationRequestId = remember { intArrayOf(resetOrientationRequestId) }
    LaunchedEffect(resetOrientationRequestId) {
        val map = mapLibreMap ?: return@LaunchedEffect
        cameraMoveClassifier.markAppMove()
        val tapped = resetOrientationRequestId != lastResetOrientationRequestId[0]
        lastResetOrientationRequestId[0] = resetOrientationRequestId
        val view = currentNavigationView
        if (tapped && view != null && view.following && map.locationComponent.isLocationComponentActivated) {
            navigationModeChange.northUpKeepingFollow(map)
            view.onLeftView()
            return@LaunchedEffect
        }
        map.easeCamera(CameraUpdateFactory.bearingTo(0.0))
    }

    // The fan's clock, and Back closing it before anything else Back would close.
    MarkerFanOutHost(fanOut)
    MarkerFanOutBackHandler(fanOut, bubbleOpen = focusedObservationId != null || focusedFeature != null, backEnabled = backEnabled)

    // RECORD -761: as the tab starts to leave, the picture the map coming back will draw over itself. MapLibre's own snapshot
    // (its GL surface; the map is a SurfaceView, which a Compose capture cannot read), with the camera it shows.
    LaunchedEffect(leavingTab) {
        if (!leavingTab) return@LaunchedEffect
        val memory = currentCameraMemory ?: return@LaunchedEffect
        val map = mapLibreMap
        if (map == null || loadedStyle == null) {
            Log.i(MAPS_COMEBACK_LOG_TAG, "cover: no picture taken, the map had not loaded")
            return@LaunchedEffect
        }
        map.snapshot { bitmap ->
            val camera = map.cameraPosition
            val target = camera.target
            if (target == null) {
                Log.w(MAPS_COMEBACK_LOG_TAG, "cover: picture dropped, the camera had no target")
                bitmap.recycle()
            } else {
                memory.keepCover(
                    MapCover(
                        bitmap,
                        MapCameraSnapshot(
                            target = LatLng(target.latitude, target.longitude),
                            zoom = camera.zoom,
                            bearing = camera.bearing,
                            tilt = camera.tilt,
                            following = map.locationComponent.isLocationComponentActivated && map.locationComponent.cameraMode != CameraMode.NONE,
                            appliedTarget = lastAppliedCameraTarget,
                        ),
                    ),
                )
                Log.i(MAPS_COMEBACK_LOG_TAG, "cover: picture kept, ${bitmap.width}x${bitmap.height} at zoom=${camera.zoom}")
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                // Gesture ownership: a drag that starts on the map pans the map, even when an
                // ancestor is scrolling. Without this, the offline-region picker
                // (OfflineMapsPanel's Column has verticalScroll) could take the drag and scroll
                // the panel instead, which made the pin hard to place — the owner's report.
                //
                // Why this is needed even though MapLibre already asks: MapView's own
                // initialisation calls requestDisallowInterceptTouchEvent(true) exactly once
                // (read from the 13.5.0 artifact's bytecode, in the same block as
                // setClickable/setLongClickable/setFocusable). That call cannot do the job here
                // for two independent reasons — the MapView is constructed inside `remember`
                // above, before AndroidView attaches it, so it has no parent to propagate the
                // request to; and ViewGroup.dispatchTouchEvent clears FLAG_DISALLOW_INTERCEPT on
                // every ACTION_DOWN, so a one-shot request at construction is gone by the first
                // touch regardless. Re-asserting it per gesture is what actually holds.
                //
                // Compose honours it: AndroidViewHolder overrides
                // requestDisallowInterceptTouchEvent and forwards it to PointerInteropFilter
                // (both read from the compose-ui 1.12.0 artifact), which then dispatches to the
                // view on the Initial pass and consumes — so the scrolling ancestor never sees
                // the change. Compose clears the flag again when the gesture ends, so this does
                // not latch.
                //
                // Returns false: MapLibre's own onTouchEvent still runs exactly as before, so
                // pan, pinch-zoom, double-tap-zoom and rotate are untouched. Safe to attach —
                // MapView never sets an OnTouchListener on itself (also checked in the artifact),
                // so nothing is being clobbered.
                mapView.setOnTouchListener { view, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    // Dispatch -463: the elastic nudge reads the finger here, before MapLibre does, and
                    // never consumes it.
                    nudgeElastic.onTouch(event)
                    // Dispatch -477: read before MapLibre handles this event, so a release while following
                    // reaches MapLibre with its fling switched off.
                    nudgeFlingGuard.onTouch(
                        event,
                        stillFollowing = currentNavigationView != null &&
                            mapLibreMap?.locationComponent?.let { it.isLocationComponentActivated && it.cameraMode != CameraMode.NONE } == true,
                    )
                    // Dispatch -477, Part A: each release while navigating, whether the map still follows and
                    // how far the finger travelled. Logging only.
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> { touchDownAt[0] = event.x; touchDownAt[1] = event.y }
                        MotionEvent.ACTION_UP -> if (currentNavigationView != null) {
                            val travelDp = kotlin.math.hypot(event.x - touchDownAt[0], event.y - touchDownAt[1]) / view.resources.displayMetrics.density
                            val mode = mapLibreMap?.locationComponent?.takeIf { it.isLocationComponentActivated }?.cameraMode
                            Log.i(NAVIGATION_VIEW_LOG_TAG, "touch released: mode=$mode, travel=${"%.0f".format(travelDp)} dp")
                        }
                    }
                    false
                }
                mapView
            },
            // Load-bearing intent carried over from osmdroid, re-reasoned rather than re-verified
            // for MapLibre — see this composable's own doc comment, "Content does not paint outside
            // this composable's slot".
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                // After the view's own layout has taken the new size (the post), so the projection is the new
                // one; a no-op while no map is ready or nothing is focused.
                .trackMapFanSpace(fanSpace)
                .onSizeChanged {
                    mapSizePx = it
                    navigationMapHeightPx = it.height
                }
                .onViewportResized { mapView.post { mapLibreMap?.let(::reanchorFocusedBubble) } },
        )
        // RECORD -761: the cover, over the map and under everything else; no pointer input, so touches reach the map.
        if (coverPicture != null && coverPhase != CoverPhase.GONE) {
            MapReturnCoverLayer(
                picture = coverPicture,
                handedOver = coverPhase == CoverPhase.HANDED_OVER,
                onGone = {
                    coverPhase = CoverPhase.GONE
                    Log.i(MAPS_COMEBACK_LOG_TAG, "cover: gone, picture released")
                },
                modifier = Modifier.onSizeChanged {
                    if (coverPhase == CoverPhase.COVERING && (it.width != coverPicture.width || it.height != coverPicture.height)) {
                        Log.w(MAPS_COMEBACK_LOG_TAG, "cover: window ${it.width}x${it.height} is not the picture's ${coverPicture.width}x${coverPicture.height}; dropped")
                        coverPhase = CoverPhase.GONE
                    }
                },
            )
        }
        // The always-visible attribution line CopyrightOverlay used to draw directly onto the
        // osmdroid MapView. MapLibre has its own tap-to-reveal attribution control
        // (UiSettings.isAttributionEnabled, on by default) built from each style source's own
        // "attribution" field (see styleJsonFor), but that is deliberately not relied on alone here
        // — see Basemap's doc comment on [Basemap.attribution] for why an always-drawn guarantee
        // matters for this app's USGS/ODbL credit and shouldn't quietly become tap-only.
        // A list of credits since map layers L0a (A5): the basemap's, then each visible layer's own
        // (none of today's layers has one, so the text is exactly what it always was).
        MapCreditCaption(
            text = attributionCaption(mapCreditsFor(basemap, useOfflineTiles, activeLayerCredits(MAP_LAYER_REGISTRY, drawnLayersState), nightMode)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = bottomInset)
                // RECORD -954: its laid-out width, for the "i" beside it in landscape.
                .onSizeChanged { creditWidthPx = it.width },
        )
    }
}

/** RECORD -752: logcat tag for a map's first frames and its camera restore on a return to Maps (logging only). */
internal const val MAPS_COMEBACK_LOG_TAG = "ForagerMapsComeback"

/** RECORD -752: how many of a new MapView's first rendered frames are logged. */
private const val MAPS_COMEBACK_FRAMES_LOGGED = 8

internal const val SIGHTINGS_MAP_TAG = "SightingsMap"
private const val FAN_OUT_RESTORE_TAG = "MarkerFanOut"
