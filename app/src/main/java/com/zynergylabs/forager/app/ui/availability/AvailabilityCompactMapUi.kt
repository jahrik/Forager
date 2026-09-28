package com.zynergylabs.forager.app.ui.availability

// Split-AvailabilityScreen Stage F: the compact map tab, moved verbatim out of
// AvailabilityScreen.kt. Five blocks, lines of the file as of 3bd0efe: 3324-3354
// (MapIconClusterPositionState, rememberMapIconClusterPositionState), 3356-4420 (CompactMapTab),
// 4422-4434 (CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR), 4442-4443 (MAP_ICON_CLUSTER_TAG) and 5005-5006
// (MAP_MODE_PICKER_COMPACT_ANCHOR_OFFSET, since removed with the popover it anchored: map layers L0b
// replaced it with the Layers sheet). Same package as Stages A to E, so every same-package
// reference resolves unchanged. Pure move: no signature, name or body changed. Three widenings,
// private -> internal: rememberMapIconClusterPositionState, called from AvailabilityScreen.kt (and
// as CompactMapTab's parameter default here); MapIconClusterPositionState with it, since that
// function returns it; and CompactMapTab, composed from AvailabilityScreen.kt. This file is over
// the ~1,200-line target, accepted by the planner: CompactMapTab alone is 1,065 lines. It composes
// the Stage F siblings AvailabilityNavigationUi.kt, AvailabilityMapControlsUi.kt and
// AvailabilityMapOverlaysUi.kt; no symbol left behind in AvailabilityScreen.kt is reached from
// here. Seam F (the wide layout) was released by the owner for this split, as recorded in the
// Understory amendment merged in #130.

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.domain.model.ReturnToStartInfo
import com.zynergylabs.forager.app.domain.model.Sighting
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.ui.log.ScreenEdge
import com.zynergylabs.forager.app.ui.map.CentrePinLocationPicker
import com.zynergylabs.forager.app.ui.map.CentrePinLocationPickerOverlay
import com.zynergylabs.forager.app.ui.map.MAP_ICON_BAR_CORNER_RADIUS
import com.zynergylabs.forager.app.ui.map.MAP_ICON_BAR_EDGE_INSET
import com.zynergylabs.forager.app.ui.map.MIN_TOUCH_TARGET
import com.zynergylabs.forager.app.ui.map.MapIconBar
import com.zynergylabs.forager.app.ui.map.MapIconBarMinimizeHandle
import com.zynergylabs.forager.app.ui.map.MapIconBarRestoreHandle
import com.zynergylabs.forager.app.ui.map.mapIconStackBorderColor
import com.zynergylabs.forager.app.ui.map.mapIconClusterContainerColor
import com.zynergylabs.forager.app.ui.map.mapIconClusterChildColor
import com.zynergylabs.forager.app.ui.map.MapMode
import com.zynergylabs.forager.app.ui.map.LEGEND_ATTRIBUTION_CLEARANCE
import com.zynergylabs.forager.app.ui.map.MAPS_TAB_OVERLAYS
import com.zynergylabs.forager.app.ui.map.MapLayersControls
import com.zynergylabs.forager.app.ui.map.MapLayersSheet
import com.zynergylabs.forager.app.ui.map.MapLegendChip
import com.zynergylabs.forager.app.ui.map.layers.COLOUR_FIELDS
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.mapLegendFor
import com.zynergylabs.forager.app.ui.map.MapOverlayContent
import com.zynergylabs.forager.app.ui.map.MapSlot
import com.zynergylabs.forager.app.ui.map.rememberTrueHeading
import com.zynergylabs.forager.app.ui.motion.MotionTokens
import com.zynergylabs.forager.app.ui.map.MapRenderMode
import com.zynergylabs.forager.app.ui.theme.Spacing
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * The icon cluster's position on the compact Map tab, as one holder so it can live *above*
 * [CompactMapTab] — in `compactMainScaffold`, which persists across tab changes — rather than in
 * the tab's own `remember`s, which are disposed with it on every tab switch. Direct owner request:
 * "switching between tabs resets the icon position; have it remember positions when switching in
 * and out of maps." That reversed the icon-bar-position-memory dispatch's working rule that
 * nothing survives a tab change, and the owner then made the general principle explicit —
 * CLAUDE.md, "UX defaults": user-set state survives navigating away and back by default, and an
 * unrequested reset is a bug unless the exception is stated for the case. Still session-only —
 * nothing here is persisted across app restarts, which is a separate per-case decision.
 *
 * Holds the cluster's user-set state: [userChosenOffsetPx] (the single source of truth, written
 * only by drags), [displayedOffsetPx] (the Animatable that is always the clamp of it under the
 * bounds in force — kept too, so returning to the tab does not glide in from zero),
 * [isOnLeftSide], and [isMinimized] (the fullscreen-fixes dispatch's "minimising resets when the
 * user leaves the Map tab" was a planner rule with no owner exception behind it, so the default
 * applies). The cluster's measurements (heights, the bar's centre) are not here — they are re-measured
 * on every mount and mean nothing across one. On return, the tab's bounds-change effect re-clamps
 * the memory under the bounds then in force (the tab change also exited fullscreen), so a position
 * chosen low in fullscreen comes back above the nav, and re-entering fullscreen glides it down
 * again — the same behaviour a fullscreen exit already has, now also across a tab change.
 */
internal class MapIconClusterPositionState {
    var userChosenOffsetPx by mutableStateOf(0f)
    val displayedOffsetPx = Animatable(0f)
    var isOnLeftSide by mutableStateOf(false)
    var isMinimized by mutableStateOf(false)

    // Landscape B2 (S6): a short landscape window's own cluster position, separate from the
    // portrait one above so that turning the phone neither carries a portrait drag into landscape
    // nor loses it on the way back. The side is stored as port or punch-hole, not left or right,
    // and translated to a window side from the current port edge where the cluster is anchored,
    // so turning between ROTATION_90 and ROTATION_270 keeps the cluster on the same device edge.
    // Defaults to the punch-hole side. Session only, like the portrait fields.
    var landscapeOnPortSide by mutableStateOf(false)
    var landscapeUserChosenOffsetPx by mutableStateOf(0f)
    val landscapeDisplayedOffsetPx = Animatable(0f)
    var landscapeIsMinimized by mutableStateOf(false)
}

/**
 * Landscape B2 (S6): "is the cluster on the window's left" for a short landscape window, read and
 * written through [MapIconClusterPositionState.landscapeOnPortSide] against the current port and
 * punch-hole edges.
 */
private class LandscapeClusterSide(
    private val state: MapIconClusterPositionState,
    private val portEdge: ScreenEdge,
    private val punchHoleEdge: ScreenEdge,
) : ReadWriteProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        (if (state.landscapeOnPortSide) portEdge else punchHoleEdge) == ScreenEdge.Left

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        state.landscapeOnPortSide = (if (value) ScreenEdge.Left else ScreenEdge.Right) == portEdge
    }
}

/** The portrait side, [MapIconClusterPositionState.isOnLeftSide], unchanged, behind the same delegate type. */
private class PortraitClusterSide(private val state: MapIconClusterPositionState) : ReadWriteProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = state.isOnLeftSide
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        state.isOnLeftSide = value
    }
}

@Composable
internal fun rememberMapIconClusterPositionState(): MapIconClusterPositionState = remember { MapIconClusterPositionState() }

/**
 * The Maps tab in its full-bleed, compact-only form — decision #2 in `docs/plans/map-redesign.md`:
 * the map fills the entire content area, with the top compass/elevation strip and the right-edge
 * icon stack drawn over it.
 *
 * Scoped to `WindowWidthClass.COMPACT` only; `MEDIUM`/`EXPANDED` keep using the unmodified [MapTab]
 * inside [CombinedResultsPane] — see the plan doc's "Scope decision" section for why this is a
 * separate composable rather than a conditional threaded through [MapTab] itself.
 *
 * Owns the location-placing flow exactly as [MapTab] does — see that composable's doc comment for
 * the mechanics [PendingMapAction] drives; [TripDatePickerDialog]/[defaultTripName] are shared,
 * unmodified, but the "what would you like to do here" chooser itself is [AddActionTile] here
 * rather than [MapTab]'s [ThreeWayActionDialog] — see that composable's own doc comment for why.
 * The icon stack's add (+) button reuses this exact same flow — it sets [showActionMenu] directly,
 * the identical trigger the map's own dedicated button sets on [MapTab], rather than a parallel
 * dialog/handler — so the two entry points can never drift apart. Unlike before this rework, it no
 * longer needs to hand the flow a starting location itself: [CentrePinLocationPicker]'s own camera
 * tracking supplies that once a choice is made, the same as every other site.
 */
@Composable
internal fun CompactMapTab(
    uiState: AvailabilityUiState,
    mapSlot: MapSlot,
    renderMode: MapRenderMode,
    /**
     * The icon cluster's position — vertical memory, displayed offset, side — held by the caller
     * so it survives leaving and returning to this tab. See [MapIconClusterPositionState]. The
     * default keeps any other caller self-contained.
     */
    clusterPosition: MapIconClusterPositionState = rememberMapIconClusterPositionState(),
    mapMode: MapMode,
    onMapModeSelected: (MapMode) -> Unit,
    onPlaceTripPin: (LatLng, LocalDate, String) -> Unit,
    onLogFindHere: (LatLng) -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    /**
     * Fullscreen-fixes dispatch, Item 1 (third design) — this tab now hosts [ForagerBottomNav]
     * itself, inside its own content [Box] below, rather than the shared [Scaffold]'s `bottomBar`
     * slot ([compactMainScaffold]'s own `bottomBar` doc comment explains why: that slot's reported
     * height must never depend on [isFullscreen], and the only way to guarantee that is for the Map
     * tab's nav not to live there at all). [isDrawerOpen] and [onBottomNavTabSelected] are exactly
     * the two inputs [ForagerBottomNav] needs beyond its own `selectedTab` — hardcoded to
     * [CompactTab.MAP] at this composable's own call to it below, since that's the only tab this
     * composable is ever shown for. [onBottomNavHeightMeasured] reports the nav's own real measured
     * height back up to [compactMainScaffold]'s own scope — see that scope's own `bottomNavHeightPx`
     * doc comment for why this needs to be a real measurement, not a fixed constant, and why the
     * value is needed a second time there (the search-dropdown dismiss scrim and its own
     * `SearchDropdown` panel), which is why this tab doesn't just keep the measurement as private
     * local state the way [mapIconClusterHeightPx] below does.
     */
    isDrawerOpen: Boolean,
    onBottomNavTabSelected: (CompactTab) -> Unit,
    onBottomNavHeightMeasured: (Float) -> Unit,
    /**
     * Landscape B1 (Resolutions R12/R13, as revised on the owner's correction). Non-null in a
     * short landscape window: the charger-port edge, where this tab overlays
     * [ForagerNavigationRail] at 80% in place of its [ForagerBottomNav], in the same layer the
     * bottom bar occupies. The map under it stays full-bleed and never changes size; the rail is
     * absent in fullscreen, with no animation. Null everywhere else, which is today's behaviour.
     */
    railPortEdge: ScreenEdge? = null,
    /**
     * Landscape B2 (S1): the punch-hole edge (`punchHoleEdgeFor`), non-null exactly when
     * [railPortEdge] is. The search bar, its filter chip and the cluster's landscape default sit
     * on this side.
     */
    punchHoleEdge: ScreenEdge? = null,
    /** Landscape B2 (S2/S3): the search bar's capped width on the punch-hole side; null in portrait. */
    landscapeSearchWidth: Dp? = null,
    /** Reports the overlaid rail's measured width up, as [onBottomNavHeightMeasured] does the bar's. */
    onRailWidthMeasured: (Float) -> Unit = {},
    /**
     * Landscape B1: what this tab's controls are padded by, and never the map itself —
     * [compactMainScaffold]'s `mapControlsPadding` (the rail's measured width or, in fullscreen,
     * the navigation-bar inset on the port side; the cut-out inset on the sides). Applied to each
     * control's own modifier, the way portrait keeps controls clear of the bottom bar by its
     * measured height. Not applied to the tapped-sighting bubble or the centre-pin picker, which
     * are positioned against the map itself. Zero by default, so portrait is unchanged.
     */
    controlsPadding: PaddingValues = PaddingValues(0.dp),
    onLocateMe: () -> Unit,
    isRecording: Boolean,
    onToggleRecording: () -> Unit,
    startRecordingErrorMessage: String?,
    breadcrumbPoints: List<LatLng>,
    waypoints: List<Waypoint>,
    onDropWaypoint: (LatLng, String) -> Unit,
    returnToStart: ReturnToStartInfo?,
    isReturning: Boolean,
    /**
     * Whether *any* navigation mode is active — [AvailabilityScreen]'s one `isNavigating`, see its
     * doc comment. Gates the HUD's presence and the compass strip's absence together, so heading,
     * elevation and coordinates are on screen exactly once in either state. Distinct from
     * [isReturning], which is one such mode (the only one in stage one) and still drives the
     * control pill's lit return toggle and the off-track heuristic.
     */
    isNavigating: Boolean,
    isOffTrack: Boolean,
    onToggleReturning: () -> Unit,
    compassProvider: CompassProvider,
    /** See [AvailabilityScreen]'s own `computeTrueHeading` doc comment. */
    computeTrueHeading: ComputeTrueHeadingUseCase,
    /** See [AvailabilityScreen]'s own `navigationTarget` doc comment. */
    navigationTarget: Waypoint?,
    /** See [AvailabilityScreen]'s own `pathHomeMeters` doc comment. */
    pathHomeMeters: Double?,
    /** The HUD's fix-age clock — [AvailabilityScreen]'s own `currentTime`, so a test can pin an old fix as stale. */
    currentTime: CurrentTimeProvider,
    /** See [AvailabilityScreen]'s own `mapTaxonFilter` doc comment — "View on Map" from a List-tab row. */
    taxonFilter: Long?,
    onClearTaxonFilter: () -> Unit,
    /**
     * True while [AdvancedSearchDropdown]'s "Set on map" is active — a [compactMainScaffold]-owned
     * state, not local to this tab, since the dropdown that triggers it lives above the bottom-nav
     * switch and can be reached from any tab. Shows [CentrePinLocationPickerOverlay] over this same
     * map the same way [pendingAction] already does, rather than a second picker.
     */
    pickingSearchLocation: Boolean = false,
    onSearchLocationPicked: (LatLng) -> Unit = {},
    onCancelSearchLocationPick: () -> Unit = {},
    /**
     * Extra top clearance beyond the compass strip's own row, for chrome this tab doesn't know
     * about that now floats above it — SearchEntryBar, on the compact scaffold's own Map tab (see
     * that call site's own doc comment). Defaults to 0.dp rather than being required: this
     * composable has exactly one call site today, but the strip/bubble/filter-chip positioning
     * below already treats "how much is above me" as a real, named input
     * ([compassStripClearance]) rather than assuming 0 — this parameter extends that same
     * assumption to cover chrome composed outside this function entirely, instead of baking a
     * second, undocumented assumption in above it.
     */
    topInset: Dp = 0.dp,
    /**
     * SearchEntryBar (plus its SearchNotice), composed as a slot inside this composable's own
     * Box rather than passed up and rendered at the call site — a deliberate, load-bearing
     * placement, not a style choice: this bar's own 80%-alpha fill needs to blend against real
     * map imagery to read as translucent chrome the way the compass strip and the two
     * TrailheadControls pills already do, and both of those live in this exact Box, as direct
     * siblings of [mapSlot]'s own [AndroidView][androidx.compose.ui.viewinterop.AndroidView]
     * content. Composing the bar even one level further out (a sibling of this whole composable's
     * own call, in [compactMainScaffold]'s outer `Box` instead) was tried first and shipped
     * fully opaque on a real device despite an identical `Surface`/color/alpha to the strip and
     * pills, and despite its own geometry measuring correctly positioned above the map — Compose's
     * alpha-blending coordination with an embedded native `View` (this map is `AndroidView`-hosted)
     * appears to be scoped to the immediate composition that hosts it, not just correct z-order
     * anywhere in the tree above it. Defaults to an empty slot: this composable has exactly one
     * call site today (compactMainScaffold's own Map tab branch), which supplies the bar; nothing
     * else needs to know this parameter exists.
     */
    searchBarSlot: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    /**
     * Map layers L0b: the Layers sheet's choices and callbacks, the legend and every saved record this
     * tab draws (see [MapLayersControls]). Defaulted so any other caller is unchanged.
     */
    mapLayers: MapLayersControls = MapLayersControls(),
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingMapAction?>(null) }
    var pendingTripLocation by remember { mutableStateOf<LatLng?>(null) }
    var pendingWaypointLocation by remember { mutableStateOf<LatLng?>(null) }
    var tappedSighting by remember { mutableStateOf<Sighting?>(null) }
    var tappedSightingScreenPosition by remember { mutableStateOf(Offset.Zero) }
    var tappedSightingBearingDeg by remember { mutableStateOf(0f) }
    // See MapOverlayContent.resumeTrackingRequestId's own doc comment — incremented alongside the
    // existing onLocateMe() call below, not instead of it: that call still drives the compass
    // strip's own one-shot position/elevation text, this drives the map's live GPS camera puck.
    var resumeTrackingRequestId by remember { mutableStateOf(0) }
    // See MapOverlayContent.resetOrientationRequestId's own doc comment.
    var resetOrientationRequestId by remember { mutableStateOf(0) }
    // Icon-bar-unify-container dispatch: MapIconBar's own vertical centre, in px relative to the
    // top of the cluster container it now sits in (measured on the bar via boundsInParent(), so
    // it tracks whatever the bar's real height and position in the container are). This replaces
    // mapIconBarBottomPx — the bar's bottom edge that TrailheadControls used to offset itself by —
    // rather than redefining it: that value's one consumer became the container Column's own
    // spacing, so the measurement itself went away. What remains is everything that used to
    // *assume* the bar's centre sat at this Box's centre plus the drag offset — the two panels'
    // row anchors and both handles' mid-height placement — which is true of the *container* now,
    // not the bar, and would silently be ~60dp off otherwise. Combined with mapIconClusterHeightPx
    // as (centreInCluster − clusterHeight / 2), the bar's centre relative to the container's.
    var mapIconBarCentreInClusterPx by remember { mutableStateOf(0f) }
    // Fullscreen-fixes dispatch, Item 3 ("the icon bar can minimise, with a peeking handle to
    // restore it") — independent of isMapFullscreen by design (that item's own "do not tie it to
    // isMapFullscreen" instruction). Held in clusterPosition (see MapIconClusterPositionState) so
    // it survives leaving and returning to the Map tab: that dispatch's own "minimising resets
    // when the user leaves the Map tab" was a planner rule, and the owner's standing UX default
    // (CLAUDE.md, "UX defaults") is that user-set state survives a tab change unless an exception
    // is stated explicitly for the case — none has been for this.
    // Landscape B2 (S6): in a short landscape window the cluster reads and writes its own landscape
    // position (MapIconClusterPositionState's landscape fields); portrait is exactly as before.
    val landscapeCluster = railPortEdge != null && punchHoleEdge != null
    var isMapIconBarMinimized by (if (landscapeCluster) clusterPosition::landscapeIsMinimized else clusterPosition::isMinimized)
    // Direct owner request (not part of the fullscreen-fixes dispatch): the icon bar can be
    // dragged up/down to reposition it, and snaps to the left or right edge — for left-handed
    // users who want it within thumb reach on that side. Held in clusterPosition (see
    // MapIconClusterPositionState) so it survives a tab change, per the owner's later ask; still
    // session-only — not persisted to DataStore (CLAUDE.md's Room/DataStore split would put a
    // "last-used side" preference there, since it's a flat, unrelated toggle). Worth revisiting
    // if the owner wants that choice to survive an app restart.
    val clusterSide: ReadWriteProperty<Any?, Boolean> = if (railPortEdge != null && punchHoleEdge != null) {
        LandscapeClusterSide(clusterPosition, portEdge = railPortEdge, punchHoleEdge = punchHoleEdge)
    } else {
        PortraitClusterSide(clusterPosition)
    }
    var isMapIconBarOnLeftSide by clusterSide
    // Icon-bar-position-memory dispatch: the bar's vertical position is two values that derive
    // one from the other, never two that can drift. mapIconBarUserChosenOffsetPx is the single
    // source of truth — the offset the user last dragged the bar (or its restore handle) to, and
    // the only thing a drag writes. mapIconBarDisplayedOffsetPx is what's actually drawn: always
    // clampMapIconBarVerticalOffset(userChosen) under the bounds *currently* in force, snapped to
    // the finger during a drag and animated (navigationMotionSpec(), the nav's own slide spec, so
    // the bar's move and the nav's arrival stay in step rather than crossing) whenever the bounds
    // themselves change — leaving fullscreen brings the nav back over the bar's bottom band and
    // pushes the bar up out of its way; re-entering removes that bound and the bar glides back to
    // where the user had put it, because the push never touched the memory. A drag in either
    // state replaces the memory, so it only ever holds a position the user chose. The previous
    // shape (one offset, overwritten in place by an instant re-clamp) was exactly the one-way
    // correction the owner found on device. The spring's few pixels of overshoot at a bound are
    // accepted, not clamped away: every other slide here uses the same spec and accepts the same.
    // Both live in clusterPosition (see MapIconClusterPositionState) so they survive leaving and
    // returning to this tab — the owner's later ask, reversing the earlier "nothing survives a
    // tab change" ruling for the position. Session-only still.
    var mapIconBarUserChosenOffsetPx by (if (landscapeCluster) clusterPosition::landscapeUserChosenOffsetPx else clusterPosition::userChosenOffsetPx)
    val mapIconBarDisplayedOffsetPx = if (landscapeCluster) clusterPosition.landscapeDisplayedOffsetPx else clusterPosition.displayedOffsetPx
    val mapIconBarOffsetScope = rememberCoroutineScope()
    // Horizontal drag distance accumulated only during an in-progress drag gesture — read once, at
    // gesture end, to decide whether to flip isMapIconBarOnLeftSide, then reset to 0 regardless of
    // whether the side actually flipped. This keeps the bar's own resting modifier exactly
    // Alignment.CenterStart/CenterEnd with no leftover offset once a drag finishes, rather than a
    // real-time "follows the finger, then snaps back" visual (a smaller, later polish, not this
    // request's own ask).
    var mapIconBarHorizontalDragPx by remember { mutableStateOf(0f) }
    // This tab's own content Box's real measured height, in px — what mapIconBarDisplayedOffsetPx is
    // clamped against below, so a drag can't carry the bar (or its restore handle) fully off
    // screen. Set via onGloballyPositioned on that Box itself, a few lines down.
    var mapContentBoxHeightPx by remember { mutableStateOf(0f) }
    // A drag distance past this point (either direction) commits the bar to the opposite side —
    // deliberately more than a light brush, since a small accidental sideways slip while actually
    // trying to reposition vertically should not also relocate the whole bar to the other side of
    // the screen.
    val mapIconBarSideSnapThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    // Keeps at least one full touch target's worth of the bar/handle on screen at either vertical
    // extreme of a drag — reuses MIN_TOUCH_TARGET (MapChrome.kt) rather than inventing a second
    // margin constant for the same "don't let a control go fully off-screen" idea.
    val mapIconBarVerticalDragMarginPx = with(LocalDensity.current) { MIN_TOUCH_TARGET.toPx() }
    // Icon-bar-unify-container dispatch: the real measured height, in px, of the cluster container
    // that holds MapIconBar and ControlPill together — what both drag clamps below
    // use to know where the cluster's top and bottom edges currently sit, since
    // Alignment.CenterEnd/CenterStart centres the container vertically before
    // mapIconBarDisplayedOffsetPx is applied. Measured on the container's own Surface, never
    // derived from what's inside it: this is the third time the same shape of bug appeared on
    // this surface — "fully on screen" was insufficient because the nav overlays the bar, then
    // "the bar is in bounds" was insufficient because the pills extend past it (on device: the
    // record pill hanging off the bottom in fullscreen, the directions pill left sitting on the
    // nav after exiting). Each time the measured object was smaller than the thing that had to
    // stay reachable. Measuring the container makes the bound correct by construction, and
    // anything added to the cluster later inherits it instead of becoming a fourth instance
    // (the since-removed DistanceArm was inside it too, and the drag range shrank by its height
    // while returning — the same mechanism will cover whatever stage two adds). Written by the
    // container only (icon-bar-
    // position-memory dispatch's ruling, carried over): the restore handle is bounded by the
    // cluster's own range, not its own 48dp, so it can never sit where the cluster could not.
    // Keeps its last value while minimised, which is what the handle's drag is clamped against.
    var mapIconClusterHeightPx by remember { mutableStateOf(0f) }
    // Expanded-panels dispatch: this tab's own ForagerBottomNav overlay's real measured height, in
    // px — kept here (as well as reported up via onBottomNavHeightMeasured) because the drag
    // clamp's downward bound needs it: that nav is composed *after* MapIconBar in this tab's Box
    // (drawn over it, hit-tested first — see its own call-site comment for why that ordering is
    // load-bearing), so "the bar's bottom edge is on screen" is not enough for its lower rows to
    // be tappable outside fullscreen; they have to stay above the nav's own top edge. Read as 0
    // while fullscreen, where the nav has slid off entirely.
    var mapBottomNavHeightPx by remember { mutableStateOf(0f) }
    // Map layers L0b (owner's ruling on Q4, "Cluster stops above it"): the legend chip's current top
    // edge, in this tab's content Box's own coordinates, or null while no chip is shown. Measured live,
    // so the cluster's clamp follows the chip's height as it expands and collapses.
    var legendChipTopPx by remember { mutableStateOf<Float?>(null) }
    var mapContentBoxTopInRootPx by remember { mutableStateOf(0f) }
    // Landscape B1 (Resolution R18): with no bottom bar composed, its last measured height would
    // otherwise stay behind as a phantom bottom band for the cluster's drag clamp and the
    // centre-pin confirm row — onGloballyPositioned stops firing once the bar is gone.
    val showBottomNav = railPortEdge == null
    LaunchedEffect(showBottomNav) {
        if (!showBottomNav) mapBottomNavHeightPx = 0f
    }

    // AddActionTile and CentrePinLocationPickerOverlay are both plain overlays, not real Dialogs,
    // so — unlike TripDatePickerDialog below, an M3 DatePickerDialog whose own Dialog window
    // already handles system back for free — this needs its own BackHandler or system back would
    // fall straight through either, same reasoning as AvailabilityScreen's own top-level "unwind
    // before falling through" chain. One pop at a time: the picker phase first if it's showing,
    // the menu only once the picker's already closed. pickingSearchLocation joins the same picker
    // tier as pendingAction (both show the identical CentrePinLocationPickerOverlay, just for a
    // different caller) rather than a third priority level of its own.
    // Navigation is deliberately NOT here any more (navigation-chrome amendment). Stage one had
    // `|| isReturning` in this condition with `else -> onToggleReturning()`, and because this is
    // the deepest registered handler it exited navigation before fullscreen, the drawer or the
    // search dropdown unwound — see AvailabilityScreen's own back chain, where navigation now sits
    // as the last step before exit and raises a prompt rather than exiting.
    BackHandler(enabled = pendingAction != null || pickingSearchLocation || showActionMenu) {
        when {
            pendingAction != null -> pendingAction = null
            pickingSearchLocation -> onCancelSearchLocationPick()
            else -> showActionMenu = false
        }
    }

    val context = LocalContext.current
    LaunchedEffect(uiState.locateMeStatus) {
        when (uiState.locateMeStatus) {
            LocateMeStatus.PermissionDenied ->
                Toast.makeText(context, "Location permission denied. Can't center on your position.", Toast.LENGTH_SHORT).show()
            LocateMeStatus.Unavailable ->
                Toast.makeText(context, "Couldn't determine your location.", Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    // Same one-shot-per-transition shape as the locateMeStatus effect above: a refused/failed
    // startRecording() is an event ("the action you just took didn't happen"), not a persistent
    // condition — the field only clears on the next successful startRecording() (see
    // TrackRecordingViewModel), so a banner would outlive the moment it's relevant.
    LaunchedEffect(startRecordingErrorMessage) {
        startRecordingErrorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    when {
        uiState.isLoadingSightings -> Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }

        uiState.sightingsErrorMessage != null -> MapMessage(
            uiState.sightingsErrorMessage,
            modifier = modifier,
            color = MaterialTheme.colorScheme.error,
        )

        else -> {
            // The map's own viewport, never null — unlike uiState.region (only set once a real
            // search has run), so the map always has *something* to show rather than the earlier
            // revision's "choose a region" placeholder: the real search region once one exists,
            // otherwise the device's live location once locate-me above resolves one, otherwise a
            // fixed fallback while that's still pending. The project owner's own framing: the Maps
            // tab should already be showing a real map, centred on the user, the moment the app
            // opens — not a message asking them to search first.
            val located = (uiState.locateMeStatus as? LocateMeStatus.Located)?.location
            val displayRegion = uiState.region
                ?: located?.let { Region(lat = it.lat, lng = it.lng, radiusKm = JOURNAL_PICKER_DEFAULT_REGION.radiusKm) }
                ?: JOURNAL_PICKER_DEFAULT_REGION

            // The GPS/locate-me pan target for a search that's already run — remember(uiState.region),
            // not just remember, so a brand new search (a different region) drops any earlier
            // locate-me override rather than keeping the map stuck on a now-stale GPS fix; see
            // MapSlot's focusOverride doc comment for why this is independent of region itself.
            // Pre-search, displayRegion already tracks locate-me directly (see above), so this stays
            // null rather than doubly panning the same fix through two different mechanisms.
            var focusOverride by remember(uiState.region) { mutableStateOf<LatLng?>(null) }
            LaunchedEffect(uiState.locateMeStatus) {
                val status = uiState.locateMeStatus
                if (uiState.region != null && status is LocateMeStatus.Located) focusOverride = status.location
            }

            // Sightings/planned trips are only real once a search has actually run — before that,
            // displayRegion is a viewport with nothing plotted on it yet, not a stand-in search.
            val hasSearched = uiState.region != null
            // "View on Map" from a List-tab row — see MapTab's own doc comment on the identical
            // filteredSightings/mapTaxonFilterLabel pair for why this filters uiState.sightings
            // itself rather than trusting uiState.forecast's own observationCount.
            val filteredSightings = when {
                !hasSearched -> emptyList()
                taxonFilter != null -> uiState.sightings.filter { it.taxonId == taxonFilter }
                else -> uiState.sightings
            }
            val mapTaxonFilterLabel = taxonFilter?.let { id ->
                val name = uiState.forecast?.entries?.firstOrNull { it.species.taxonId == id }?.species
                    ?.let { it.commonName ?: it.scientificName }
                    ?: filteredSightings.firstOrNull()?.let { it.commonName ?: it.scientificName }
                    ?: "this species"
                "$name (${filteredSightings.size})"
            }

            var cameraCenter by remember(displayRegion) { mutableStateOf(LatLng(displayRegion.lat, displayRegion.lng)) }
            // A real clearance for "below the compass strip," derived from the strip's own actual
            // type style rather than a hardcoded touch-target constant (Part A item 1 of this
            // dispatch un-pinned the strip's height back to wrapping its text content, so a fixed
            // 48dp guess would now be too generous). Measured once via rememberTextMeasurer — the
            // same approach the since-removed DistanceArm used for its widest-string width — rather than
            // read back from the strip's real onGloballyPositioned layout: a state value written
            // during layout and read here to construct AnchoredAtScreenPoint's own minY argument
            // was tried and is a confirmed, reproducible regression — AvailabilityScreenMapIconStackTest's
            // own "tapping elsewhere on the map dismisses the observation bubble" test went from
            // passing to reliably failing on exactly that change (bisected line by line), corrupting
            // mapSlot(...)'s own onTap wiring one frame later for reasons this investigation could
            // not fully pin down inside Compose's own recomposition-scope internals. A remembered,
            // one-time measurement carries no such risk: it never changes after first composition,
            // so nothing here ever triggers a later recomposition.
            // Navigation HUD stage one: the ONE true-north heading both the compass strip and the
            // HUD read. Passed down as the State object; its .value is read only inside those two
            // leaves — reading it here would recompose this whole tab at sensor rate. See
            // rememberTrueHeading's own doc comment before touching this.
            val trueHeading = rememberTrueHeading(compassProvider, computeTrueHeading, uiState.liveFix)
            // MGRS by default, the labelled decimal pair on tap — hoisted here from the strip's
            // own leaf (navigation-chrome dispatch) because the strip and the HUD now take turns
            // showing the coordinates: a format chosen while navigating must still be the format
            // the strip shows on exit (CLAUDE.md, UX defaults — user-set state that resets on its
            // own is a bug). Local state, not AvailabilityUiState: purely which of two always-
            // computable representations of the same fix to display, nothing the ViewModel or a
            // future session needs. Still resets when this tab unmounts, as it did before.
            var showDecimalDegrees by remember { mutableStateOf(false) }
            val onToggleCoordinateFormat = { showDecimalDegrees = !showDecimalDegrees }
            val compassStripTextMeasurer = rememberTextMeasurer()
            val compassStripLabelStyle = MaterialTheme.typography.labelMedium
            val compassStripDensity = LocalDensity.current
            val compassStripClearance = remember(compassStripLabelStyle, compassStripDensity) {
                with(compassStripDensity) {
                    compassStripTextMeasurer.measure("Mg", compassStripLabelStyle).size.height.toDp()
                }
            }
            Box(
                modifier = modifier
                    .fillMaxSize()
                    // Feeds mapIconBarDisplayedOffsetPx's own clamp below — see that variable's own
                    // doc comment.
                    .onGloballyPositioned { coordinates ->
                        mapContentBoxHeightPx = coordinates.size.height.toFloat()
                        mapContentBoxTopInRootPx = coordinates.positionInRoot().y
                    },
            ) {
                mapSlot(
                    displayRegion,
                    MapOverlayContent(
                        sightings = filteredSightings,
                        plannedTrips = if (hasSearched) uiState.plannedTrips else emptyList(),
                        breadcrumbPoints = breadcrumbPoints,
                        waypoints = waypoints,
                        resumeTrackingRequestId = resumeTrackingRequestId,
                        resetOrientationRequestId = resetOrientationRequestId,
                        focusedObservationId = tappedSighting?.observationId,
                        // Map layers L0b, B2 (owner: "Every saved record"): every saved find with a
                        // location, every ended track, every located album photo and every offline
                        // region, pending deletes left out, each visible by default.
                        keptTrackPolylines = mapLayers.records.trackPolylines,
                        findMarkers = mapLayers.records.findMarkers,
                        photoMarkers = mapLayers.records.photoMarkers,
                        offlineRegionCircles = mapLayers.records.offlineRegionCircles,
                    ),
                    renderMode,
                    focusOverride,
                    {},
                    // Tapping the map restores chrome while fullscreen — decision #5 — AND, now,
                    // dismisses the observation bubble below regardless of fullscreen state (a plain
                    // tap elsewhere on the map is its whole dismiss gesture, see ObservationBubble's
                    // own doc comment). Harmless to clear when nothing is showing.
                    {
                        if (isFullscreen) onToggleFullscreen()
                        tappedSighting = null
                    },
                    { sighting, screenPosition, bearingDeg ->
                        tappedSighting = sighting
                        tappedSightingScreenPosition = screenPosition
                        tappedSightingBearingDeg = bearingDeg
                    },
                    { location -> cameraCenter = location },
                    Modifier.fillMaxSize(),
                )
                // Composed right after mapSlot — see searchBarSlot's own doc comment for why this
                // exact nesting (a direct sibling of the map's own AndroidView content, inside
                // this Box) is what makes its translucency actually work.
                searchBarSlot()
                tappedSighting?.let { sighting ->
                    // minY = compassStripClearance, a real measurement of the strip's own type
                    // style — not a hardcoded touch-target constant and not 0 — the strip is
                    // composed after this in the same Box (deliberately, so its own controls win
                    // any overlap — see CompactMapTab's own doc comment above MapIconBar), and is
                    // full-width/flush against the map's top edge. A marker tapped near the map's
                    // own top edge would otherwise anchor a bubble underneath that strip's band: its
                    // own taps (including the close icon's) would never reach this composable,
                    // silently swallowed by the strip's own Surface the exact way CLAUDE.md's
                    // "Known pitfalls" already documents for this app's map overlays — the same
                    // class of miss that entry warns visual review alone won't catch, this time
                    // guarded against directly rather than only caught by this bubble's own
                    // close-icon interaction test. See compassStripClearance's own doc comment for
                    // why it is a one-time text measurement rather than the strip's real measured
                    // layout height.
                    AnchoredAtScreenPoint(
                        anchorPx = tappedSightingScreenPosition,
                        bearingDeg = tappedSightingBearingDeg,
                        minY = topInset + compassStripClearance,
                        modifier = Modifier.fillMaxSize(),
                    ) { arrowAngleDeg ->
                        ObservationBubble(
                            sighting = sighting,
                            onViewOnINaturalist = {
                                launchINaturalistObservation(context, sighting.observationId)
                                tappedSighting = null
                            },
                            onDismiss = { tappedSighting = null },
                            arrowAngleDeg = arrowAngleDeg,
                        )
                    }
                }
                // MapIconBar composed *before* CompassElevationStrip now, not after — field-test
                // dispatch item 2 gave the strip a real touch target at its own far right edge, the
                // same horizontal column MapIconBar's CenterEnd alignment already claims.
                // MapIconBar's Surface intercepts touches across its full bounds (see this
                // composable's own CLAUDE.md-documented precedent), and on a short enough viewport
                // its vertically-centered row stack reaches all the way up into the compass strip's
                // own row — confirmed directly by AvailabilityScreenMapIconStackTest's own
                // touch-interaction test on a w360dp-h640dp viewport, not assumed from visual review
                // alone (the exact class of miss that same file's own history warns visual review
                // alone won't catch). Composition order is paint AND hit-test order for overlapping
                // siblings in a Box, so moving this earlier guarantees the strip's own control wins
                // any overlap on every screen size, not just typical ones — a small cosmetic cost
                // (the strip's opaque background could cover a sliver of one icon bar row on a
                // screen too short for MapIconBar's own rows to fit at all — already a degraded
                // state before this change) traded for a control that always actually works. Still
                // true after MapIconBar's own return-to-vehicle row was removed (see that
                // composable's own doc comment) — the overlap this guards against is with the bar's
                // Surface as a whole, not specifically with that one row.
                // Fullscreen-fixes dispatch, Item 3 ("the icon bar can minimise, with a peeking
                // handle to restore it"). MapIconBar and TrailheadControls hide/show together,
                // gated on isMapIconBarMinimized rather than isMapFullscreen — that item's own "do
                // not tie it to isMapFullscreen" instruction, and the owner's own "Minimise means
                // the chrome goes away, not that it fragments." Since the icon-bar-unify-container
                // dispatch they are one cluster container, so hiding together is by construction
                // rather than two gates that happen to agree.
                //
                // MapIconBarMinimizeHandle is composed as a later sibling of that container (at the
                // bar's own vertical centre — "mid-height" — via mapIconBarCentreShiftOffset below,
                // and moving with it) rather than nested inside it: Surface clips to its shape and
                // the handle's mark straddles the container's outer edge by design, and there is no
                // on-screen room to place a full 48dp touch target beside the bar without
                // overlapping it (the bar's own Spacing.sm edge inset is far narrower than that),
                // so the handle deliberately overlaps the bar's own outermost sliver, attached to
                // its edge the way the owner described. Composed after the container (and so
                // painted and hit-tested on top of it) so it wins that overlap, the same
                // composition-order-is-hit-test-order convention this file already uses for
                // the container itself against CompassElevationStrip (see this block's own comment
                // above).
                //
                // Direct owner request, layered on top of the above: the cluster (and its two
                // handles) can be dragged to reposition vertically and snaps to either screen edge
                // — see isMapIconBarOnLeftSide/mapIconBarUserChosenOffsetPx's own doc comments
                // above. mapIconBarSideAlignment/mapIconBarPositionOffset are shared by the
                // container and whichever handle is currently showing so they always move and
                // land on the same side together, as one unit. detectDragGesturesAfterLongPress,
                // not a plain drag detector or Modifier.draggable: a quick tap must keep reaching
                // Surface's own onClick (minimize/restore) unambiguously, and the long-press
                // threshold is what lets a tap and a drag share the same control with no gesture
                // conflict, a well-established Compose combination for exactly this pairing.
                // TrailheadControls follows the same side flip because it is laid out inside the
                // container, and nothing inside it needs mirroring (the since-removed DistanceArm
                // extended downward, side-agnostic by construction, for the same reason).
                val mapIconBarSideAlignment = if (isMapIconBarOnLeftSide) Alignment.CenterStart else Alignment.CenterEnd
                val mapIconBarPositionOffset = Modifier.offset {
                    IntOffset(mapIconBarHorizontalDragPx.roundToInt(), mapIconBarDisplayedOffsetPx.value.roundToInt())
                }
                // Icon-bar-drag-refinements dispatch, Item 4: the bar cannot be dragged up far
                // enough to rise above where SearchDropdown itself starts. compactMainScaffold's
                // own searchDropdownTopOffset (a different, outer composable scope, not reachable
                // from here) is searchBarHeight + compassStripClearance; topInset (this composable's
                // own parameter, ≈ searchBarHeight — see that parameter's own doc comment) plus this
                // exact scope's own compassStripClearance above equal the same value, reachable
                // here without new plumbing — and already the established way this file computes
                // "how far below the top the search chrome reaches" (see the taxon filter chip's
                // own topInset + compassStripClearance padding a little further down).
                val dropdownTopPx = with(compassStripDensity) { (topInset + compassStripClearance).toPx() }
                // Stale-clamp-bound dispatch (owner finding on device): every input to the clamp
                // below must be *live state*, never a plain value closed over. mapIconBarDragModifier's
                // pointerInput(Unit) block is started lazily on the first pointer event and never
                // restarted (its key is Unit, and a changed lambda instance does not restart it),
                // so the drag callback keeps the closure from the user's *first drag* for the life
                // of the handle. isFullscreen (a plain Boolean parameter) and dropdownTopPx (a
                // plain Float) were captured that way: whichever fullscreen state existed at the
                // first drag bounded every later drag — a first drag in fullscreen let later drags
                // outside it pass under the nav; a first drag outside it capped later fullscreen
                // drags at the nav's former top — while the LaunchedEffect below, re-run per
                // recomposition, always read the fresh values and corrected the position, which
                // the next drag then undid. Reproduced under Robolectric (enter fullscreen, drag
                // low, exit, drag low: 640dp vs the nav's 560dp top) before this fix. The nav's
                // height does not vary by theme; the theme the owner noticed was a different
                // first-drag order after the tab change a theme switch goes through. Every other
                // clamp input is already a MutableState delegate, read live. rememberUpdatedState
                // is the standard shape for a long-lived gesture block reading composition values —
                // one clamp, derived live, used by the drag path and the effect alike; no path
                // holds its own copy, and nothing re-runs the effect more often to paper over it.
                val currentIsFullscreen by rememberUpdatedState(isFullscreen)
                val currentDropdownTopPx by rememberUpdatedState(dropdownTopPx)
                // Map layers L0b (Q4): the chip's top, only while the chip is on the cluster's side
                // (it sits at the bottom-end corner), less a gap, as a further lowest edge for the
                // cluster. Display-only, like the nav's: the remembered position is never changed.
                val legendClusterGapPx = with(compassStripDensity) { Spacing.sm.toPx() }
                val legendBoundPx = legendChipTopPx?.takeIf { !isMapIconBarOnLeftSide }?.let { it - legendClusterGapPx }
                val currentLegendBoundPx by rememberUpdatedState(legendBoundPx)
                // See the comment on the LaunchedEffect below for both bounds' derivations.
                fun clampMapIconBarVerticalOffset(offsetPx: Float): Float {
                    // The lowest edge the bar may reach: this Box's own bottom in fullscreen, the
                    // nav's own top edge otherwise (mapBottomNavHeightPx's own doc comment).
                    val navBoundPx = mapContentBoxHeightPx - (if (currentIsFullscreen) 0f else mapBottomNavHeightPx)
                    val bottomBoundPx = currentLegendBoundPx?.let { minOf(it, navBoundPx) } ?: navBoundPx
                    val fallbackDownwardOffsetPx = (bottomBoundPx - mapContentBoxHeightPx / 2f - mapIconBarVerticalDragMarginPx).coerceAtLeast(0f)
                    val maxDownwardOffsetPx = if (mapIconClusterHeightPx > 0f) {
                        (bottomBoundPx - (mapContentBoxHeightPx + mapIconClusterHeightPx) / 2f).coerceAtLeast(0f)
                    } else {
                        fallbackDownwardOffsetPx
                    }
                    // Upward (negative) bound: the bar's own top edge, once centered then shifted
                    // by the offset, is (mapContentBoxHeightPx - mapIconClusterHeightPx) / 2 + offset —
                    // solved for the smallest offset that keeps that top edge at or below
                    // dropdownTopPx, so the bar can't rise into the dropdown's own space
                    // (icon-bar-drag-refinements dispatch, Item 4).
                    val maxUpwardOffsetPx = if (mapIconClusterHeightPx > 0f) {
                        (currentDropdownTopPx - (mapContentBoxHeightPx - mapIconClusterHeightPx) / 2f)
                            .coerceIn(-fallbackDownwardOffsetPx, 0f)
                    } else {
                        -fallbackDownwardOffsetPx
                    }
                    return offsetPx.coerceIn(maxUpwardOffsetPx, maxOf(maxUpwardOffsetPx, maxDownwardOffsetPx))
                }
                // Expanded-panels dispatch: where AddActionTile below anchors — the bar's live
                // position, not its default one. (The map mode popover anchored here too until map
                // layers L0b replaced it with the Layers sheet, a bottom sheet with no anchor.)
                // Panels align to the same edge the bar is on (mapIconBarSideAlignment) and are
                // inset from it by the bar's own MAP_ICON_BAR_EDGE_INSET, so a panel's outer edge
                // lands exactly on the bar's
                // outer edge on either side (the same overlap the old fixed CenterEnd/-Spacing.sm
                // pair produced on the right, now mirrored on the left with a positive inset).
                // The vertical term is the bar's own drag offset (the same px
                // mapIconBarPositionOffset applies to the bar), converted to dp for
                // DpOffset; each caller adds its own row's mapIconBarRowAnchorOffset on top. The
                // horizontal drag px is included too — it is always zero once a drag ends, and no
                // panel can open mid-drag (the finger is on the handle), so this is parity with
                // mapIconBarPositionOffset rather than a visible effect.
                val mapIconBarPanelAnchorOffset = with(compassStripDensity) {
                    DpOffset(
                        x = (if (isMapIconBarOnLeftSide) MAP_ICON_BAR_EDGE_INSET else -MAP_ICON_BAR_EDGE_INSET) +
                            mapIconBarHorizontalDragPx.toDp(),
                        // Plus the bar's own centre relative to the container's — see
                        // mapIconBarCentreInClusterPx's own doc comment: the container is what's
                        // centred here now, the bar sits in its top part.
                        y = (mapIconBarDisplayedOffsetPx.value + mapIconBarCentreInClusterPx - mapIconClusterHeightPx / 2f).toDp(),
                    )
                }
                // Keyed on the two edges (landscape B2, S6): the gesture block keeps the closure it
                // started with, so a turn (portrait to landscape, or 90 to 270) must restart it or a
                // drag would write through the previous orientation's position and side. Constant
                // in portrait (both null), so portrait behaves as the Unit key did.
                val mapIconBarDragModifier = Modifier.pointerInput(railPortEdge, punchHoleEdge) {
                    detectDragGesturesAfterLongPress(
                        onDragEnd = {
                            when {
                                mapIconBarHorizontalDragPx <= -mapIconBarSideSnapThresholdPx -> isMapIconBarOnLeftSide = true
                                mapIconBarHorizontalDragPx >= mapIconBarSideSnapThresholdPx -> isMapIconBarOnLeftSide = false
                            }
                            mapIconBarHorizontalDragPx = 0f
                        },
                        onDragCancel = { mapIconBarHorizontalDragPx = 0f },
                    ) { change, dragAmount ->
                        change.consume()
                        mapIconBarHorizontalDragPx += dragAmount.x
                        // The finger is the source of truth during a drag: the clamped position
                        // becomes the memory and is drawn immediately (snapTo, which also cancels
                        // any bounds-change glide still in flight) — see
                        // mapIconBarUserChosenOffsetPx's own doc comment.
                        val draggedToPx = clampMapIconBarVerticalOffset(mapIconBarDisplayedOffsetPx.value + dragAmount.y)
                        mapIconBarUserChosenOffsetPx = draggedToPx
                        mapIconBarOffsetScope.launch { mapIconBarDisplayedOffsetPx.snapTo(draggedToPx) }
                    }
                }
                // Expanded-panels dispatch (sweep finding, owner-approved "fix the clamp"): the
                // bar's own measured top and bottom edges both stay on screen now, not just "at
                // least one touch target's worth of it". The old downward bound (bar centre no
                // further than MIN_TOUCH_TARGET above the Box's bottom) let the bar's last two
                // rows — layers and add, the rows the map mode popover (since replaced by the
                // Layers sheet) and AddActionTile anchored to — leave the screen at the bottom of
                // the drag range, which would have carried both panels off with them once they
                // followed the bar. Symmetric with Item 4's own upward bound: the bar's bottom edge, once centered then shifted by the
                // offset, is (mapContentBoxHeightPx + mapIconClusterHeightPx) / 2 + offset — solved
                // for the largest offset that keeps it at or above the lowest reachable edge
                // (the nav's top outside fullscreen, this Box's bottom in it — the nav is drawn
                // over this bar, so "on screen" alone would still leave the bottom rows under
                // it, untappable; a decision taken beyond the approved "keep the bottom edge on
                // screen", reported as such). Falls back to the old margin-based bound before
                // mapIconClusterHeightPx has its first real measurement, same as the upward bound
                // always did. Re-applied (the LaunchedEffect below) whenever a bound's input
                // changes, not only during a drag: a bar dragged to the very bottom while
                // fullscreen would otherwise end up under the nav once fullscreen is exited — the
                // same untappable-rows outcome this fix exists to rule out, just reached by a
                // different route. (The other route this used to catch — the restore handle
                // dragged lower than the bar may sit — no longer exists: the handle is bounded by
                // the bar's own measured height now, see mapIconClusterHeightPx's own doc comment.)
                //
                // Icon-bar-position-memory dispatch: the target is always the clamp of the
                // *user-chosen* offset, never of the displayed one, and the move is animated on
                // the nav's own spec — so the push-up on leaving fullscreen and the glide back on
                // re-entry read as one behaviour, and the memory survives the push untouched. See
                // mapIconBarUserChosenOffsetPx's own doc comment. Not keyed on the memory itself:
                // a drag snaps the displayed value directly and is never animated.
                val mapIconBarOffsetSpec = MotionTokens.navigationMotionSpec<Float>()
                LaunchedEffect(mapIconClusterHeightPx, mapContentBoxHeightPx, mapBottomNavHeightPx, isFullscreen, landscapeCluster, legendBoundPx) {
                    val targetPx = clampMapIconBarVerticalOffset(mapIconBarUserChosenOffsetPx)
                    if (targetPx != mapIconBarDisplayedOffsetPx.value) {
                        mapIconBarDisplayedOffsetPx.animateTo(targetPx, mapIconBarOffsetSpec)
                    }
                }
                // Owner request (alongside the fullscreen-slide-out-fixes dispatch): minimising
                // slides this cluster off whichever edge it's on, and the restore handle slides in
                // from that same edge, instead of the instant cut this used to be — "like the rest
                // of the UI," i.e. the same AnimatedVisibility slide SearchEntryBar and
                // ForagerBottomNav use for fullscreen. navigationMotionSpec(), the nav's own slide
                // spec — this is navigation chrome, not a panel. Pure translations of Box
                // children, no effect on this Box's own size, same reasoning as those two slides.
                //
                // Icon-bar-unify-container dispatch: what used to be three wrappers (bar, minimize
                // handle, TrailheadControls, each aligned separately and each trusting the others
                // to land in the right place) is now one wrapper around one filled container —
                // MapIconBar and TrailheadControls in a Column, the gap between them the Column's
                // own spacing rather than an offset from a measured bottom edge. The container is
                // what gets measured (mapIconClusterHeightPx), dragged, clamped and minimised, so
                // the bound is right by construction. The minimize handle is a *sibling* of the
                // container, not a child: Surface clips to its shape, and the handle's visible
                // mark straddles the container's outer edge by design, so inside it half the mark
                // would vanish. Both handles sit at the bar's own mid-height, not the container's
                // (mapIconBarCentreShiftOffset below), which is what "mid-height of the icon bar"
                // has always meant; the restore handle uses the last-measured values since the
                // bar is unmounted while minimised. The cluster, not the bar, is what's centred
                // at rest — so the bar sits ~60dp higher by default than it did as a lone
                // centred object. Owner's call: a default derived from the container is honest,
                // and correcting it back to preserve the old look would reintroduce exactly the
                // bar-specific arithmetic the container exists to remove.
                val mapIconBarSlideOffset: (Int) -> Int = { fullWidth -> if (isMapIconBarOnLeftSide) -fullWidth else fullWidth }
                val mapIconBarCentreShiftOffset = Modifier.offset {
                    IntOffset(0, (mapIconBarCentreInClusterPx - mapIconClusterHeightPx / 2f).roundToInt())
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isMapIconBarMinimized,
                    enter = slideInHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), initialOffsetX = mapIconBarSlideOffset),
                    exit = slideOutHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), targetOffsetX = mapIconBarSlideOffset),
                    modifier = Modifier
                        .align(mapIconBarSideAlignment)
                        .padding(controlsPadding)
                        .then(mapIconBarPositionOffset)
                        .then(mapIconBarCentreShiftOffset),
                ) {
                    MapIconBarRestoreHandle(
                        onRestore = { isMapIconBarMinimized = false },
                        onLeftSide = isMapIconBarOnLeftSide,
                        // Reports nothing into mapIconClusterHeightPx — its drag is clamped to
                        // the cluster's own range, see that variable's doc comment.
                        modifier = Modifier.then(mapIconBarDragModifier),
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isMapIconBarMinimized,
                    enter = slideInHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), initialOffsetX = mapIconBarSlideOffset),
                    exit = slideOutHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), targetOffsetX = mapIconBarSlideOffset),
                    modifier = Modifier
                        .align(mapIconBarSideAlignment)
                        // Landscape B1: clear of the overlaid rail and the cut-out band.
                        .padding(controlsPadding)
                        .then(mapIconBarPositionOffset),
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(MAP_ICON_BAR_CORNER_RADIUS),
                            // The lighter of the two layered fills — see
                            // MAP_ICON_CLUSTER_CONTAINER_ALPHA's own doc comment for the
                            // compositing arithmetic and the values chosen.
                            color = mapIconClusterContainerColor(),
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, mapIconStackBorderColor()),
                            modifier = Modifier
                                .padding(MAP_ICON_BAR_EDGE_INSET)
                                // Feeds both drag clamps above — see mapIconClusterHeightPx's own
                                // doc comment. Measured on the container, never on its contents.
                                .onGloballyPositioned { coordinates ->
                                    mapIconClusterHeightPx = coordinates.size.height.toFloat()
                                }
                                .testTag(MAP_ICON_CLUSTER_TAG),
                        ) {
                            Column(
                                horizontalAlignment = if (isMapIconBarOnLeftSide) Alignment.Start else Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR),
                            ) {
                                MapIconBar(
                                    isFullscreen = isFullscreen,
                                    onToggleFullscreen = onToggleFullscreen,
                                    onLocateMe = {
                                        resumeTrackingRequestId++
                                        onLocateMe()
                                    },
                                    onResetOrientation = { resetOrientationRequestId++ },
                                    mapMode = mapMode,
                                    onOpenLayers = { showLayersSheet = true },
                                    onAdd = {
                                        // No location to grab any more — the button just opens
                                        // the menu; the location comes from
                                        // CentrePinLocationPickerOverlay's own camera tracking
                                        // once a choice is made. See this function's own doc
                                        // comment.
                                        showActionMenu = true
                                    },
                                    fillColor = mapIconClusterChildColor(),
                                    // Feeds the panels' and handles' anchors — see
                                    // mapIconBarCentreInClusterPx's own doc comment.
                                    modifier = Modifier.onGloballyPositioned { coordinates ->
                                        mapIconBarCentreInClusterPx = coordinates.boundsInParent().center.y
                                    },
                                )
                                // Composed whenever MapIconBar is (regardless of isRecording —
                                // record start/stop must stay reachable before the first
                                // recording starts, the same as it was as an always-enabled
                                // MapIconBar row before this dispatch; isRecording flows in as a
                                // plain parameter, see TrailheadControls' own doc comment, not a
                                // presence check, so a tester never sees this pill appear from
                                // nowhere the first time they hit record). Inside the container
                                // rather than gated separately: it minimises, slides, drags and
                                // clamps with the bar because it is laid out with it.
                                TrailheadControls(
                                    isRecording = isRecording,
                                    onToggleRecording = onToggleRecording,
                                    returnToStart = returnToStart,
                                    isReturning = isReturning,
                                    isOffTrack = isOffTrack,
                                    onToggleReturning = onToggleReturning,
                                    distanceUnit = uiState.distanceUnit,
                                    onLeftSide = isMapIconBarOnLeftSide,
                                )
                            }
                        }
                        MapIconBarMinimizeHandle(
                            onMinimize = { isMapIconBarMinimized = true },
                            onLeftSide = isMapIconBarOnLeftSide,
                            modifier = Modifier
                                .align(mapIconBarSideAlignment)
                                .then(mapIconBarCentreShiftOffset)
                                .then(mapIconBarDragModifier),
                        )
                    }
                }
                // Not composed at all while navigating (navigation-chrome dispatch, item 1) — the
                // HUD below carries the heading, elevation and coordinates then, and on device
                // both showing meant the heading appeared three times. Removed from composition
                // rather than made invisible: this strip's leaf is what reads the heading State
                // at sensor rate, and an invisible strip would still be recomposing at 16 Hz
                // alongside the HUD doing the same work. Gated on isNavigating, never isReturning,
                // so stage two's picker cannot bring it back by accident — see AvailabilityScreen's
                // own isNavigating doc comment.
                if (!isNavigating) {
                    CompassElevationStrip(
                        heading = trueHeading,
                        elevationMeters = uiState.liveAltitudeMeters,
                        location = uiState.liveLocation,
                        showDecimalDegrees = showDecimalDegrees,
                        onToggleCoordinateFormat = onToggleCoordinateFormat,
                        // Full width, "just below" SearchEntryBar rather than a narrow floating pill
                        // with margins on both sides, per the project owner's own redesign call — topInset
                        // is how that clearance reaches here now that the bar composes as a real overlay
                        // in the same Box as this tab's own content (compactMainScaffold's own call
                        // site) instead of a sibling Column entry above it; 0.dp (this parameter's own
                        // default) reproduces the old flush-against-the-map-top behavior exactly.
                        modifier = if (railPortEdge != null) {
                            // Landscape B2 (S4): the top corner on the rail side, below the
                            // status bar only (the Scaffold's top inset), not below the search
                            // bar, which is on the other side now; content-width.
                            Modifier
                                .align(if (railPortEdge == ScreenEdge.Left) Alignment.TopStart else Alignment.TopEnd)
                                .padding(controlsPadding)
                        } else {
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(controlsPadding)
                                .fillMaxWidth()
                                .padding(top = topInset)
                        },
                        contentWidth = railPortEdge != null,
                    )
                }

                // Below the compass strip (topInset + compassStripClearance as top padding), same
                // reasoning as AnchoredAtScreenPoint's own minY — the strip's Surface intercepts
                // touches across its full width, so a chip placed underneath it would have its own
                // "Show all species" tap silently swallowed the same way a bubble anchored there
                // would. topInset itself (see this composable's own doc comment) clears whatever
                // chrome floats above the strip too — SearchEntryBar, on the Map tab.
                mapTaxonFilterLabel?.let { label ->
                    TaxonMapFilterChip(
                        label = label,
                        onClear = onClearTaxonFilter,
                        modifier = if (punchHoleEdge != null && landscapeSearchWidth != null) {
                            // Landscape B2 (S3): directly under the search bar (the strip is in
                            // the rail corner now, not under the bar), in a column the bar's own
                            // width on the punch-hole side, aligned to the bar's start.
                            Modifier
                                .align(if (punchHoleEdge == ScreenEdge.Left) Alignment.TopStart else Alignment.TopEnd)
                                .padding(controlsPadding)
                                .padding(top = topInset + Spacing.sm)
                                .width(landscapeSearchWidth)
                                .wrapContentWidth(Alignment.Start)
                        } else {
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(controlsPadding)
                                .padding(top = topInset + compassStripClearance + Spacing.sm)
                        },
                    )
                }

                // Navigation HUD stage one. Composed after the cluster (so its own exit wins any
                // overlap with a cluster dragged up to its upward bound) and before the nav
                // below (so the nav keeps winning its own band) — see NavigationHud's own doc
                // comment for the full mounting reasoning. Gated on the same isNavigating that
                // removes the compass strip above, so the two are never on screen together; in
                // stage one that is the return mode (TrackRecordingViewModel.startReturn). Top
                // padding is topInset alone — with the strip gone there is nothing above this
                // panel but the search bar, whose fullscreen slide it follows the way the strip
                // does; compassStripClearance stays in the taxon chip's and bubble's paths only
                // because those still clear the strip while not navigating. Never touches mapSlot.
                if (isNavigating) {
                    NavigationHud(
                        heading = trueHeading,
                        liveFix = uiState.liveFix,
                        target = navigationTarget,
                        distanceUnit = uiState.distanceUnit,
                        pathHomeMeters = pathHomeMeters,
                        currentTime = currentTime,
                        showDecimalDegrees = showDecimalDegrees,
                        onToggleCoordinateFormat = onToggleCoordinateFormat,
                        onExit = onToggleReturning,
                        modifier = if (railPortEdge != null) {
                            // Landscape B2 (S4): the top corner on the rail side, below the
                            // status bar only, at most 360dp wide.
                            Modifier
                                .align(if (railPortEdge == ScreenEdge.Left) Alignment.TopStart else Alignment.TopEnd)
                                .padding(controlsPadding)
                                .widthIn(max = LANDSCAPE_HUD_MAX_WIDTH)
                                .fillMaxWidth()
                        } else {
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(controlsPadding)
                                .fillMaxWidth()
                                .padding(top = topInset)
                        },
                    )
                }

                // Map layers L0b, B4 (owner's ruling 5, "Bottom-right, above the 'i'"): the legend chip,
                // shown only while a colour field is visible. In the bottom-end corner, above MapLibre's
                // "i" (LEGEND_ATTRIBUTION_CLEARANCE) and above the nav in portrait (renderMode.bottomInset,
                // the attribution caption's own inset: the nav's height, or the system bar's in
                // fullscreen), and inside controlsPadding, so it stays clear of the landscape rail on
                // either edge. The cluster keeps clear of it through its clamp above (Q4). Composed
                // with the ambient chrome, before the nav and the modal overlays. Its placement depends
                // on real insets Robolectric reports as zero: device-only.
                mapLegendFor(renderMode.layers, MAP_LAYER_REGISTRY, COLOUR_FIELDS, mapLayers.cellsShown)?.let { legend ->
                    DisposableEffect(Unit) { onDispose { legendChipTopPx = null } }
                    MapLegendChip(
                        legend = legend,
                        expanded = mapLayers.legendExpanded,
                        onExpandedChange = mapLayers.onLegendExpandedChange,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(controlsPadding)
                            .padding(end = Spacing.sm, bottom = renderMode.bottomInset + LEGEND_ATTRIBUTION_CLEARANCE)
                            .onGloballyPositioned { coordinates ->
                                legendChipTopPx = coordinates.positionInRoot().y - mapContentBoxTopInRootPx
                            },
                    )
                }

                // Fullscreen-fixes dispatch, Item 1 (third design). Composed here — after the
                // ambient chrome above (MapIconBar/CompassElevationStrip/TrailheadControls/
                // TaxonMapFilterChip, none of which reach this bar's own bottom band) but *before*
                // the modal overlays below (AddActionTile/MapLayersSheet/CentrePinLocationPickerOverlay)
                // — deliberately, not composed last: this Box now extends the full screen height in
                // both fullscreen states (CompactMapTab's own doc comment), so those modals'
                // fillMaxSize() content now reaches all the way down into this bar's own screen
                // region too. Composing this nav after them (drawn on top, hit-tested first) was
                // tried first and is a confirmed, reproducible regression — it silently swallowed
                // CentrePinLocationPickerOverlay's own "OK" confirm tap, caught by
                // AvailabilityScreenTripPlanningFlowTest's own trip-planning-flow tests going from
                // passing to reliably failing (not flaky) on exactly that ordering, the same class
                // of miss CLAUDE.md's own "Known pitfalls" already documents twice over for chrome
                // composed over a map. selectedTab is hardcoded to CompactTab.MAP — this composable
                // is only ever shown for that tab, so there's nothing else it could mean here. The
                // other three tabs still render this same composable, unconditionally opaque, from
                // compactMainScaffold's own bottomBar slot instead (that call site's own doc
                // comment) — this overlay and that one are the two places ForagerBottomNav renders,
                // never both for the same tab at once.
                //
                // Slides down and off the bottom edge while fullscreen — fullscreen-fixes dispatch,
                // Item 2 ("slide the chrome away instead of cutting it"), a deliberate change from
                // the crossfade-to-80%-opacity this bar used before: fullscreen is exited via
                // MapIconBar's own fullscreen control, never via this nav, so the nav is not the
                // way out and can safely leave the screen entirely. 80% opacity outside fullscreen
                // (the owner's own call, from a screenshot): it floats over the map whenever it's
                // on screen at all, so the standing 80%-over-the-map rule applies to it the same as
                // to every other piece of map chrome here — see the containerColor parameter's
                // own doc comment for the two prior flips of this exact value. A pure Box-child overlay,
                // same confirmed-safe reasoning as SearchEntryBar's own slide above — animating it
                // has no bearing on this Box's own size.
                // Landscape B1: not composed at all in a short landscape window, where the rail
                // beside this tab replaces it (compactMainScaffold's showRail) — an `if`, not
                // `visible`, so turning the phone does not play this bar's slide-out in landscape.
                if (showBottomNav) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isFullscreen,
                        enter = slideInVertically(animationSpec = MotionTokens.navigationMotionSpec()) { fullHeight -> fullHeight },
                        exit = slideOutVertically(animationSpec = MotionTokens.navigationMotionSpec()) { fullHeight -> fullHeight },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        ForagerBottomNav(
                            selectedTab = CompactTab.MAP,
                            // 80%, the standing opacity for chrome over the map — see this bar's own
                            // containerColor doc comment.
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                            isDrawerOpen = isDrawerOpen,
                            onTabSelected = onBottomNavTabSelected,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    mapBottomNavHeightPx = coordinates.size.height.toFloat()
                                    onBottomNavHeightMeasured(coordinates.size.height.toFloat())
                                },
                        )
                    }
                }

                // Landscape B1 (R12/R13 revised): the rail, overlaid on the port edge in exactly
                // this layer — after the ambient chrome, before the modal overlays below, for the
                // same reasons the bottom bar above sits here. 80% over the map, like the bar. The
                // map under it keeps its size whether it shows or not; the controls are padded
                // clear of it by its measured width (controlsPadding). Absent in fullscreen, with
                // no animation — the slide toward the port edge is B2's (P10).
                // Landscape B2 (S7): on entering fullscreen the rail slides toward the port edge,
                // off the window, and back on exit, on the theme's motionScheme spatial spec (the
                // nav's own navigationMotionSpec, defaultSpatialSpec) — no ad-hoc tween. A pure
                // translation of a Box child: the map's size never changes. The rail leaves the
                // tree once its exit animation ends (AnimatedVisibility), as B1's absence did.
                if (railPortEdge != null) {
                    val railSlideOffset: (Int) -> Int = { fullWidth -> if (railPortEdge == ScreenEdge.Left) -fullWidth else fullWidth }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isFullscreen,
                        enter = slideInHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), initialOffsetX = railSlideOffset),
                        exit = slideOutHorizontally(animationSpec = MotionTokens.navigationMotionSpec(), targetOffsetX = railSlideOffset),
                        modifier = Modifier.align(if (railPortEdge == ScreenEdge.Left) Alignment.CenterStart else Alignment.CenterEnd),
                    ) {
                        ForagerNavigationRail(
                            selectedTab = CompactTab.MAP,
                            isDrawerOpen = isDrawerOpen,
                            onTabSelected = onBottomNavTabSelected,
                            portEdge = railPortEdge,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                onRailWidthMeasured(coordinates.size.width.toFloat())
                            },
                        )
                    }
                }

                // Inside this Box, not alongside it, so it can align near the add button's own
                // corner of the icon stack above — see AddActionTile's doc comment for why this
                // reads as opening "from" that button rather than as a centered system dialog.
                AddActionTile(
                    visible = showActionMenu,
                    onPlanTrip = {
                        showActionMenu = false
                        pendingAction = PendingMapAction.PLAN_TRIP
                    },
                    onLogFind = {
                        showActionMenu = false
                        pendingAction = PendingMapAction.LOG_FIND
                    },
                    onDropWaypoint = {
                        showActionMenu = false
                        pendingAction = PendingMapAction.DROP_WAYPOINT
                    },
                    onDismiss = { showActionMenu = false },
                    // Landscape B1: the same padding as the cluster it is anchored to, so the two
                    // share one frame.
                    modifier = Modifier.fillMaxSize().padding(controlsPadding),
                    // Expanded-panels dispatch: anchored to the bar's live side and drag offset
                    // (see mapIconBarPanelAnchorOffset above), plus this panel's own row.
                    anchor = mapIconBarSideAlignment,
                    anchorOffset = DpOffset(
                        x = mapIconBarPanelAnchorOffset.x,
                        y = mapIconBarPanelAnchorOffset.y + ADD_TILE_ANCHOR_OFFSET,
                    ),
                    growsFrom = if (isMapIconBarOnLeftSide) Alignment.BottomStart else Alignment.BottomEnd,
                )

                // Map layers L0b, B1: the Layers sheet, in place of the basemap-only popover this row
                // used to open. A modal bottom sheet in its own window, so it needs no anchor to the
                // cluster and no padding for the rail.
                if (showLayersSheet) {
                    MapLayersSheet(
                        mapMode = mapMode,
                        onMapModeSelected = onMapModeSelected,
                        overlays = MAPS_TAB_OVERLAYS,
                        colourFields = mapLayers.listedColourFields,
                        state = mapLayers.stored,
                        onVisibilityChanged = mapLayers.onVisibilityChanged,
                        onOpacityChanged = mapLayers.onOpacityChanged,
                        onColourFieldMoved = mapLayers.onColourFieldMoved,
                        onDismiss = { showLayersSheet = false },
                    )
                }

                // Owner finding on device: the OK/Cancel row sat under the app's nav (and under
                // Android's own navigation bar in fullscreen), because this Box spans the full
                // screen height. Outside fullscreen the nav's real measured height — which already
                // includes the system bar it consumes (CLAUDE.md, "Robolectric reports zero window
                // insets") — is what's underneath; in fullscreen the nav has slid away and only
                // the system navigation bar is. The fullscreen half is device-only by
                // construction: Robolectric reports that inset as zero.
                val centrePinConfirmBottomInset = if (isFullscreen) {
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                } else {
                    with(LocalDensity.current) { mapBottomNavHeightPx.toDp() }
                }
                if (pendingAction != null) {
                    CentrePinLocationPickerOverlay(
                        onConfirm = {
                            when (pendingAction) {
                                PendingMapAction.PLAN_TRIP -> pendingTripLocation = cameraCenter
                                PendingMapAction.LOG_FIND -> onLogFindHere(cameraCenter)
                                PendingMapAction.DROP_WAYPOINT -> pendingWaypointLocation = cameraCenter
                                null -> Unit
                            }
                            pendingAction = null
                        },
                        onCancel = { pendingAction = null },
                        modifier = Modifier.fillMaxSize(),
                        bottomInset = centrePinConfirmBottomInset,
                        // The map's own night (the renderMode handed to mapSlot above), so the pin
                        // follows Night Maps (colour build C2 (e)).
                        night = renderMode.night,
                    )
                } else if (pickingSearchLocation) {
                    // AdvancedSearchDropdown's own "Set on map" — same overlay, same already-shown
                    // map, same cameraCenter this tab already tracks via onCameraIdle; see this
                    // param's own doc comment for why it isn't a second picker.
                    CentrePinLocationPickerOverlay(
                        onConfirm = { onSearchLocationPicked(cameraCenter) },
                        onCancel = onCancelSearchLocationPick,
                        modifier = Modifier.fillMaxSize(),
                        bottomInset = centrePinConfirmBottomInset,
                        // The map's own night (the renderMode handed to mapSlot above), so the pin
                        // follows Night Maps (colour build C2 (e)).
                        night = renderMode.night,
                    )
                }
            }
        }
    }

    pendingTripLocation?.let { location ->
        TripDatePickerDialog(
            defaultName = defaultTripName(uiState.plannedTrips.size),
            onConfirm = { date, name ->
                onPlaceTripPin(location, date, name)
                pendingTripLocation = null
            },
            onDismiss = { pendingTripLocation = null },
        )
    }

    pendingWaypointLocation?.let { location ->
        WaypointNameDialog(
            defaultName = defaultWaypointName(waypoints.size),
            onConfirm = { name ->
                onDropWaypoint(location, name)
                pendingWaypointLocation = null
            },
            onDismiss = { pendingWaypointLocation = null },
        )
    }
}

/**
 * Gap between [MapIconBar]'s bottom edge and [ControlPill]'s top edge — matches [MapIconBar]'s own
 * `Spacing.sm` inset from the screen edge, so the pill reads as continuing the same margin rather
 * than sitting at an arbitrarily different distance. Icon-bar-unify-container dispatch: now the
 * cluster container Column's own `spacedBy`, no longer an offset from a measured bottom edge —
 * the gap was structural (produced by `Modifier.offset`, not padding in a shared parent), and
 * unifying the container is what changed how it is expressed. It is filled by the container at
 * [com.zynergylabs.forager.app.ui.map.MAP_ICON_CLUSTER_CONTAINER_ALPHA] and no longer passes touches to the
 * map, which is intentional; the two `@Ignore`d gap-touch tests in
 * `AvailabilityScreenMapIconStackTest` now carry a false premise on top of the Robolectric reason
 * they were parked for, and are left for the owner's own separate look.
 */
private val CONTROL_PILL_GAP_BELOW_MAP_ICON_BAR = Spacing.sm

/** The cluster container's own `Surface` — what tests measure the cluster's real extent by (icon-bar-unify-container dispatch). */
internal const val MAP_ICON_CLUSTER_TAG = "map-icon-cluster"

/** Landscape B2 (S4): the navigation HUD's width cap in the rail-side top corner. */
private val LANDSCAPE_HUD_MAX_WIDTH = 360.dp
