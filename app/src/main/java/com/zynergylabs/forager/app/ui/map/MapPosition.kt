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
): State<ShownPosition> = produceState<ShownPosition>(ShownPosition.None, liveFix, approximateFix, lastKnownFix, currentTime) {}

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

/**
 * The soft dot: MapLibre's own location blue (`#4A90E2`, its `maplibre_location_layer_blue`) at half
 * opacity. **Provisional**, the planner's starting point, for the desk step to judge.
 */
internal const val APPROXIMATE_DOT_COLOR: Int = 0x804A90E2.toInt()

/** The approximate reading's circle: twice MapLibre's own 15%, so the circle, not the dot, reads as the position. **Provisional.** */
internal const val APPROXIMATE_ACCURACY_ALPHA = 0.3f

/** The last known position's dot, circle and heading: MapLibre's own location grey (`#A1B0C0`, `maplibre_location_layer_gray`). */
internal const val LAST_KNOWN_COLOR: Int = 0xFFA1B0C0.toInt()

/** The look for [shown], by the one rule the label, the strip and the HUD read ([com.zynergylabs.forager.app.domain.inPlaceOfGps]). */
fun positionLookOf(shown: ShownPosition, liveFix: LocationFix.Update?): PositionLook = PositionLook.PRECISE

/**
 * MapLibre's [LocationEngine], fed by the app instead of by the platform (dispatch 2026-09-28-510).
 */
class AppLocationEngine : LocationEngine {
    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) = Unit

    override fun requestLocationUpdates(request: LocationEngineRequest, callback: LocationEngineCallback<LocationEngineResult>, looper: Looper?) = Unit

    override fun requestLocationUpdates(request: LocationEngineRequest, pendingIntent: PendingIntent?) = Unit

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) = Unit

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) = Unit

    /** The fix the dot is drawn at now, or `null` for none. */
    fun update(fix: LocationFix.Update?) = Unit
}

/** The fix as the platform [Location] MapLibre draws. */
internal fun LocationFix.Update.toPlatformLocation(): Location = Location(APP_LOCATION_PROVIDER)

/** The provider name the app's locations carry; MapLibre does not read it. */
internal const val APP_LOCATION_PROVIDER = "forager"
