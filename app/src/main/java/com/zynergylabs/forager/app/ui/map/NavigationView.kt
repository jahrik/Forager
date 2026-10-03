package com.zynergylabs.forager.app.ui.map

import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationFix
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.location.CompassEngine
import org.maplibre.android.location.CompassListener
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.OnCameraTrackingChangedListener
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapLibreMap

/*
 * The map's navigation view (dispatch 2026-09-28-430, plan task T22; rulings in continuation
 * 2026-09-28-432). The owner's step path, confirmed with "Go on T22":
 *
 *   Tap Return > the map tilts about 45°, turns so the way you face is up, and places you a little
 *   below the centre; it follows you.
 *   Drag the map > it stops following; "Return to Route" appears; a tap brings the view back.
 *   The compass gets stuck > "Compass calibrating…"; about 15 s of retrying; if it recovers the view
 *   carries on, if not the map turns north-up and says so; facing-up comes back by itself.
 *   Stop navigating > flat and north-up, as it was.
 *
 * The figures and the wording are the planner's, provisional, each named once here.
 */

/** How far the map tilts while navigating. The owner: "enough to focus on the path ahead, not so gentle that it's a cosmetic tilt." */
const val NAVIGATION_VIEW_TILT_DEGREES = 45.0

/**
 * The share of the map's height added as top padding while navigating, so the followed point (the
 * walker) sits below the centre and more of the map ahead is shown. With a top padding of P the
 * map centres in the remaining height, so the walker sits P / 2 below the middle: a quarter of the
 * height puts them an eighth of the way down from the centre.
 */
const val NAVIGATION_VIEW_TOP_PADDING_FRACTION = 0.25

/** How long a stuck compass is retried before the map turns north-up. */
const val NAVIGATION_COMPASS_RETRY_MILLIS = 15_000L

/** How often the stuck-compass clock is read, so the retry window ends on time with no new reading. */
private const val NAVIGATION_FACING_TICK_MILLIS = 250L

/** The HUD's heading label while a stuck compass is retried (ruling E: one place, one word). */
const val COMPASS_CALIBRATING_TEXT = "Compass calibrating…"

/** The HUD's heading label once the map has turned north-up because the compass cannot be used. */
const val COMPASS_NORTH_UP_TEXT = "Compass unavailable · north up"

/** Which way the map faces while navigating. */
enum class NavigationFacing {
    /** The way the walker faces is up. Also the state with no fix yet: nothing has failed. */
    FACING_UP,

    /** The compass is stuck; the map holds its bearing while it is retried. */
    CALIBRATING,

    /** The compass could not be used: north is up, and the map still follows. */
    NORTH_UP,
}

/**
 * Tells "stuck" apart from what [TrueHeadingReading] already says, with no new threshold
 * (continuation -432): stuck is [TrueHeadingReading.Unreliable], which is
 * [com.zynergylabs.forager.app.domain.CompassTrustJudge]'s existing band. Retried for
 * [retryMillis], then north-up; back to facing-up the moment the reading is
 * [TrueHeadingReading.Available] again. [TrueHeadingReading.NoSensor] is terminal: north-up at
 * once, with no retry window (ruling B). [TrueHeadingReading.NeedsFix] is not a compass fault.
 *
 * Not caught: a sensor that silently stops reporting. Nothing in the app measures that, and no new
 * threshold is made for it.
 */
class NavigationFacingJudge(private val retryMillis: Long = NAVIGATION_COMPASS_RETRY_MILLIS) {
    private var unreliableSinceMillis: Long? = null

    fun next(reading: TrueHeadingReading, nowMillis: Long): NavigationFacing = when (reading) {
        is TrueHeadingReading.Available, TrueHeadingReading.NeedsFix -> {
            unreliableSinceMillis = null
            NavigationFacing.FACING_UP
        }
        TrueHeadingReading.NoSensor -> NavigationFacing.NORTH_UP
        TrueHeadingReading.Unreliable -> {
            val since = unreliableSinceMillis ?: nowMillis.also { unreliableSinceMillis = it }
            if (nowMillis - since >= retryMillis) NavigationFacing.NORTH_UP else NavigationFacing.CALIBRATING
        }
    }
}

/**
 * [NavigationFacingJudge] over the one true heading, while [isNavigating]; [NavigationFacing.FACING_UP]
 * otherwise. Judged on every change of the heading, and on a short tick so the 15 s window ends on
 * time even if no new reading arrives. Read the result in a leaf: it changes rarely.
 */
@Composable
fun rememberNavigationFacing(heading: State<TrueHeadingReading>, isNavigating: Boolean, currentTime: CurrentTimeProvider): State<NavigationFacing> =
    produceState(initialValue = NavigationFacing.FACING_UP, isNavigating, heading, currentTime) {
        if (!isNavigating) {
            value = NavigationFacing.FACING_UP
            return@produceState
        }
        val judge = NavigationFacingJudge()
        // At once on every change of the heading, so the label never shows the old word first; and
        // on a short tick, so the retry window ends on time with no new reading. One dispatcher, so
        // the two never run the judge at the same moment.
        launch { snapshotFlow { heading.value }.collect { value = judge.next(it, currentTime.nowEpochMillis()) } }
        while (true) {
            delay(NAVIGATION_FACING_TICK_MILLIS)
            value = judge.next(heading.value, currentTime.nowEpochMillis())
        }
    }

/**
 * What the map is asked for while navigating (null otherwise). [following] is false once the user
 * has moved away from the view (a drag, or "Reset orientation"); the map then leaves the camera
 * where they put it until [restoreRequestId] changes ("Return to Route", or locate). [onLeftView]
 * is how the map reports such a move. Built so plan task T8 can turn it on by the same predicate,
 * `AvailabilityScreen`'s `isNavigating`.
 */
data class NavigationViewRequest(
    val facing: NavigationFacing,
    val following: Boolean,
    val restoreRequestId: Int,
    val onLeftView: () -> Unit,
)

/**
 * MapLibre's [CompassEngine] fed the app's own true heading (ruling A). MapLibre's built-in engine
 * (`LocationComponentCompassEngine`) works from the rotation vector alone with no declination,
 * so it reads magnetic north: inferred from its bytecode in the pinned 13.5.0 artifact (no
 * `GeomagneticField` call; `getRotationMatrixFromVector`, then `getOrientation`), not measured on
 * a phone. On a true-north map that turned the puck about 15° off in the Pacific Northwest, and
 * would have turned a compass-following map the same. This hands MapLibre the heading the compass
 * strip and the HUD read, smoothed and trust-judged; while that is not available it hands nothing,
 * and the last heading stands.
 */
class TrueHeadingCompassEngine : CompassEngine {
    private val listeners = mutableListOf<CompassListener>()
    private var lastHeading = 0f
    private var lastStatus = SensorManager.SENSOR_STATUS_UNRELIABLE

    override fun addCompassListener(listener: CompassListener) {
        listeners += listener
    }

    override fun removeCompassListener(listener: CompassListener) {
        listeners -= listener
    }

    override fun getLastHeading(): Float = lastHeading

    override fun getLastAccuracySensorStatus(): Int = lastStatus

    /** One reading of the app's true heading. Only [TrueHeadingReading.Available] moves the heading. */
    fun update(reading: TrueHeadingReading) {
        val status = if (reading is TrueHeadingReading.Available) SensorManager.SENSOR_STATUS_ACCURACY_HIGH else SensorManager.SENSOR_STATUS_UNRELIABLE
        if (status != lastStatus) {
            lastStatus = status
            listeners.toList().forEach { it.onCompassAccuracyChange(status) }
        }
        if (reading is TrueHeadingReading.Available) {
            lastHeading = reading.degrees
            listeners.toList().forEach { it.onCompassChanged(reading.degrees) }
        }
    }
}

/** The top padding, in pixels, that places the walker below the centre of a map [mapHeightPx] tall. */
fun navigationViewTopPaddingPx(mapHeightPx: Int): Double = mapHeightPx * NAVIGATION_VIEW_TOP_PADDING_FRACTION

/**
 * What a map needs to make the app's true heading for itself (ruling A, continuation
 * 2026-09-28-432, and the planner's ruling (i) on the Journal's centre-pin picker): the compass,
 * declination, and the live fix declination is read at. Provided once by `AvailabilityScreen` to
 * every map under it, so a map that is not handed a heading (the pickers) still points its puck to
 * true north, and the Journal's own composables pass nothing new. A map makes its heading only
 * while it is composed, so the compass is not read on a tab with no map.
 */
data class MapCompass(
    val compassProvider: CompassProvider,
    val computeTrueHeading: ComputeTrueHeadingUseCase,
    val liveFix: State<LocationFix.Update?>,
)

/** See [MapCompass]. `null` outside `AvailabilityScreen`: such a map keeps MapLibre's own compass. */
val LocalMapCompass = staticCompositionLocalOf<MapCompass?> { null }

/** The time the navigation view's own camera changes take, so the tilt and the turn ease in rather than jump. */
private const val NAVIGATION_VIEW_TRANSITION_MILLIS = 750L

/**
 * The navigation view's camera changes on MapLibre's [LocationComponent], and the one listener
 * that tells the user's moves away from the view apart from the app's own. Every change here is
 * the SDK's tracking API (a camera mode, with its own bearing and tilt, and the padding while
 * tracking), never a camera move, because a camera move from the app ends MapLibre's tracking.
 *
 * The camera modes: [NavigationFacing.FACING_UP] is [CameraMode.TRACKING_COMPASS] (position, and
 * bearing from the compass engine); [NavigationFacing.CALIBRATING] is [CameraMode.TRACKING]
 * (position; the bearing holds); [NavigationFacing.NORTH_UP] is [CameraMode.TRACKING_GPS_NORTH]
 * (position, north up).
 *
 * A user's move away is any change of camera mode the app did not make, while following: a drag
 * ends tracking (NONE), and a rotate gesture ends the compass's hold on the bearing. A pinch to
 * zoom keeps MapLibre tracking and is not a move away. What MapLibre does on a phone is device-only.
 */
class NavigationModeChange {
    private var appChanging = false
    private var listeningTo: LocationComponent? = null

    /** The camera mode the view asked for while following; `null` once the user has moved away, and outside navigation. */
    var expected: Int? = null
        private set

    /** Whether the navigation view has been applied and navigation has not stopped since. */
    var active = false
        private set

    /** Runs [block], an app-made change of camera mode, so the listener does not read it as the user's. */
    fun byTheApp(block: () -> Unit) {
        appChanging = true
        try {
            block()
        } finally {
            appChanging = false
        }
    }

    /** Listens to [map]'s location component once it is activated; [view] is read when a change arrives. */
    fun listenTo(map: MapLibreMap, view: () -> NavigationViewRequest?) {
        val component = map.locationComponent
        if (!component.isLocationComponentActivated || listeningTo === component) return
        listeningTo = component
        component.addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
            override fun onCameraTrackingDismissed() = Unit

            override fun onCameraTrackingChanged(currentMode: Int) {
                if (appChanging) return
                val wanted = expected ?: return
                if (currentMode == wanted) return
                val request = view() ?: return
                if (!request.following) return
                expected = null
                request.onLeftView()
            }
        })
    }

    /** The view for [facing]: its camera mode, tilted, with the walker [topPaddingPx] below the top padding's line. */
    fun applyView(map: MapLibreMap, facing: NavigationFacing, topPaddingPx: Double) {
        val mode = when (facing) {
            NavigationFacing.FACING_UP -> CameraMode.TRACKING_COMPASS
            NavigationFacing.CALIBRATING -> CameraMode.TRACKING
            NavigationFacing.NORTH_UP -> CameraMode.TRACKING_GPS_NORTH
        }
        expected = mode
        active = true
        val component = map.locationComponent
        byTheApp { component.setCameraMode(mode, NAVIGATION_VIEW_TRANSITION_MILLIS, null, null, NAVIGATION_VIEW_TILT_DEGREES, null) }
        component.paddingWhileTracking(doubleArrayOf(0.0, topPaddingPx, 0.0, 0.0), NAVIGATION_VIEW_TRANSITION_MILLIS)
    }

    /** "Reset orientation" while navigating (ruling D): north-up, still following. The caller reports the move away. */
    fun northUpKeepingFollow(map: MapLibreMap) {
        expected = null
        byTheApp { map.locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH, NAVIGATION_VIEW_TRANSITION_MILLIS, null, null, null, null) }
    }

    /** Navigation has stopped: flat and north-up, as before it started, still following if the map was. */
    fun leaveNavigation(map: MapLibreMap) {
        expected = null
        active = false
        val component = map.locationComponent
        if (component.cameraMode != CameraMode.NONE) {
            byTheApp { component.setCameraMode(CameraMode.TRACKING, NAVIGATION_VIEW_TRANSITION_MILLIS, null, 0.0, 0.0, null) }
            component.paddingWhileTracking(doubleArrayOf(0.0, 0.0, 0.0, 0.0), NAVIGATION_VIEW_TRANSITION_MILLIS)
        } else {
            map.easeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder(map.cameraPosition).tilt(0.0).bearing(0.0).padding(0.0, 0.0, 0.0, 0.0).build()))
        }
    }
}
