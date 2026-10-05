package com.zynergylabs.forager.app.ui.map

import android.app.PendingIntent
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.ShownPosition
import com.zynergylabs.forager.app.domain.inPlaceOfGps
import com.zynergylabs.forager.app.domain.millisUntilShownPositionChanges
import com.zynergylabs.forager.app.domain.shownPosition
import kotlinx.coroutines.delay
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult

/**
 * The position the app has judged, for a map's dot (dispatch 2026-09-28-510; the owner chose "App feeds
 * the dot"). Provided once by `AvailabilityScreen` to every map under it, the same way [MapCompass] is.
 * [liveFix] is the gated fix as held, which tells a last known reading apart from the held GPS fix
 * ([com.zynergylabs.forager.app.domain.inPlaceOfGps]).
 */
data class MapPosition(
    val shown: State<ShownPosition>,
    val liveFix: State<LocationFix.Update?>,
)

/**
 * [shownPosition] over the screen's three fixes, kept current as they age: re-judged whenever a fix
 * changes and again at the moment one turns lost ([millisUntilShownPositionChanges]), with no ticker
 * running in between.
 */
@Composable
fun rememberShownPosition(
    liveFix: LocationFix.Update?,
    approximateFix: LocationFix.Update?,
    lastKnownFix: LocationFix.Update?,
    currentTime: CurrentTimeProvider,
): State<ShownPosition> = produceState(shownPosition(liveFix, approximateFix, lastKnownFix, currentTime.nowEpochMillis()), liveFix, approximateFix, lastKnownFix, currentTime) {
    while (true) {
        value = shownPosition(liveFix, approximateFix, lastKnownFix, currentTime.nowEpochMillis())
        delay(millisUntilShownPositionChanges(liveFix, approximateFix, currentTime.nowEpochMillis()) ?: break)
    }
}

/** See [MapPosition]. `null` outside `AvailabilityScreen`: such a map keeps MapLibre's own location engine. */
val LocalMapPosition = staticCompositionLocalOf<MapPosition?> { null }

/** How the dot is drawn. */
enum class PositionLook {
    /** MapLibre's own dot and options, exactly as before this dispatch. */
    PRECISE,

    /** A soft dot in a stronger circle sized to the reading's accuracy. */
    APPROXIMATE,

    /** Grey dot and circle: where the phone last was. */
    LAST_KNOWN,
}

/*
 * The two looks' dots are the app's own images, `res/drawable/puck_approximate.xml` (MapLibre's location
 * blue, `#4A90E2`, at half opacity) and `res/drawable/puck_last_known.xml` (its location grey, `#A1B0C0`),
 * never a tint of MapLibre's dot: see liveLocationComponentOptions for why. **Provisional**, for the desk
 * step to judge.
 */

/** The approximate reading's circle: twice MapLibre's own 15%, so the circle, not the dot, reads as the position. **Provisional.** */
internal const val APPROXIMATE_ACCURACY_ALPHA = 0.3f

/** The last known position's circle: MapLibre's own location grey (`#A1B0C0`, `maplibre_location_layer_gray`), as its dot is. */
internal const val LAST_KNOWN_COLOR: Int = 0xFFA1B0C0.toInt()

/** The look for [shown], by the one rule the label, the strip and the HUD read ([com.zynergylabs.forager.app.domain.inPlaceOfGps]). */
fun positionLookOf(shown: ShownPosition, liveFix: LocationFix.Update?): PositionLook = when (shown.inPlaceOfGps(liveFix)) {
    is ShownPosition.Approximate -> PositionLook.APPROXIMATE
    is ShownPosition.LastKnown -> PositionLook.LAST_KNOWN
    // A GPS fix, fresh, stale or lost: MapLibre's own dot, greying on its own 30 s clock, as before.
    else -> PositionLook.PRECISE
}

/**
 * MapLibre's [LocationEngine], fed by the app instead of by the platform (dispatch 2026-09-28-510; the
 * owner: "App feeds the dot"). Until this dispatch the dot was fed by MapLibre's own engine, which
 * listens to GPS and the network itself with no accuracy gate and picks between them by Android's
 * classic "better location" rule, so the dot could show a 100 m network fix as an ordinary dot while
 * the HUD and the strip said there was no fix (the report's item 5; first flagged as "the puck
 * divergence" in `docs/audits/2026-09-06-location-accuracy-prebuild-report.md`). Now the dot is drawn
 * where the app has judged the walker to be ([ShownPosition]), so the dot, the map's follow, the HUD and
 * the strip read one value. Also one fewer pair of GPS and network registrations while a map is open.
 *
 * Only what MapLibre's `LocationComponent` (13.5.0) calls is implemented: the callback requests and
 * [getLastLocation]. The [PendingIntent] requests, which it never makes (read from its bytecode), are
 * refused as unsupported rather than silently accepted. Main thread only, as MapLibre calls it and as
 * Compose feeds it.
 */
class AppLocationEngine : LocationEngine {
    private val callbacks = mutableListOf<LocationEngineCallback<LocationEngineResult>>()
    private var current: LocationFix.Update? = null

    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        val fix = current
        if (fix == null) {
            callback.onFailure(IllegalStateException("No position yet: nothing live has arrived and no last known position was held."))
        } else {
            callback.onSuccess(LocationEngineResult.create(fix.toPlatformLocation()))
        }
    }

    override fun requestLocationUpdates(request: LocationEngineRequest, callback: LocationEngineCallback<LocationEngineResult>, looper: Looper?) {
        if (callback !in callbacks) callbacks += callback
    }

    override fun requestLocationUpdates(request: LocationEngineRequest, pendingIntent: PendingIntent?) {
        throw UnsupportedOperationException("AppLocationEngine delivers to callbacks only; MapLibre's LocationComponent asks for nothing else.")
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        callbacks -= callback
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        throw UnsupportedOperationException("AppLocationEngine delivers to callbacks only; MapLibre's LocationComponent asks for nothing else.")
    }

    /**
     * The fix the dot is drawn at now. Delivered only when it differs from the last one: a repeat would
     * restart MapLibre's own 30 s stale clock, which greys a GPS dot that has stopped updating, as before.
     * `null` delivers nothing; MapLibre keeps the last position it was given.
     */
    fun update(fix: LocationFix.Update?) {
        if (fix == null || fix == current) return
        current = fix
        val location = fix.toPlatformLocation()
        callbacks.toList().forEach { it.onSuccess(LocationEngineResult.create(location)) }
    }
}

/** The fix as the platform [Location] MapLibre draws: its accuracy is the circle's radius, and "not reported" stays unset. */
internal fun LocationFix.Update.toPlatformLocation(): Location = Location(APP_LOCATION_PROVIDER).also { location ->
    location.latitude = lat
    location.longitude = lng
    location.time = timestampEpochMillis
    accuracyMeters?.let { location.accuracy = it }
    altitude?.let { location.altitude = it }
    speedMetersPerSecond?.let { location.speed = it }
}

/** The provider name the app's locations carry; MapLibre does not read it. */
internal const val APP_LOCATION_PROVIDER = "forager"
