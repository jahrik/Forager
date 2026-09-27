package com.zynergylabs.forager.app.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Region
import com.zynergylabs.forager.app.ui.theme.MapPalette
import com.zynergylabs.forager.app.ui.theme.Spacing

/**
 * Every site in this app that places something on the map — the offline-region picker, log-entry
 * creation, waypoint dropping, trip planning — used a long-press to do it, before this file
 * existed. **Replaced everywhere by one idiom: a marker fixed at screen centre, the map panning
 * underneath it, committed with OK or cancelled.**
 *
 * **This is not a style preference.** Long-press is unreliable for people with thicker fingers,
 * and a *draggable* marker has the identical defect — the same finger that's supposed to be
 * precise about a point is the thing covering that point while it drags. A marker pinned to
 * screen centre keeps the finger off the target entirely: the finger drives the map, never the
 * pin. This is an accessibility decision with a stated reason. Do not "improve" this into a
 * draggable marker — that reintroduces the exact defect this replaces, just with extra steps.
 *
 * ## Why this needs [MapSlot.onCameraIdle], not [MapSlot.onLongPress]
 *
 * [MapSlot] had no way to answer "where is the camera pointing right now" before this file —
 * every existing use only ever told the map where to go ([Region], [MapSlot.focusOverride]), never
 * asked it where it ended up. A centre-pinned marker needs exactly that: the picked point *is*
 * wherever the camera settles, read back once panning stops. [MapSlot.onCameraIdle] is that read
 * side, verified against the pinned `org.maplibre.gl:android-sdk:13.5.0` artifact
 * (`MapLibreMap.addOnCameraIdleListener`/`getCameraPosition` — see [SightingsMap]'s own wiring).
 *
 * ## Two shapes, one idiom
 *
 * [CentrePinLocationPicker] owns its own [mapSlot] instance — for a site with no map already on
 * screen (the offline-region picker's panel, the journal's own entry-location screen).
 * [CentrePinLocationPickerOverlay] is just the pin-and-buttons chrome, meant to sit inside a `Box`
 * that already has its own `mapSlot(...)` call — for a site where the map is already rendering
 * something else (sightings, planned trips, waypoints) that a second, separate picker map
 * instance would either duplicate or hide. Both render the identical pin and OK/Cancel row; only
 * who owns the map differs.
 */
@Composable
fun CentrePinLocationPicker(
    mapSlot: MapSlot,
    region: Region,
    basemap: Basemap,
    /**
     * Night mode, passed through to the map the same way the main screen's does. Defaulted so the
     * journal's own pickers keep today's behaviour until their callers thread it — see this
     * parameter's call sites.
     */
    night: Boolean = false,
    onConfirm: (LatLng) -> Unit,
    onCancel: () -> Unit,
    /**
     * Width-to-height ratio for **the map viewport itself**, or `null` (the default) to let the
     * map take whatever height is left over after this composable's own instruction line and
     * OK/Cancel row.
     *
     * Opt-in per call site rather than a constant here, because the two shapes want opposite
     * things. The journal's entry-location pickers ([com.zynergylabs.forager.app.ui.log.JournalTab],
     * [com.zynergylabs.forager.app.ui.log.LogPanel]) hand this composable a `weight(1f)` slot and want the map
     * to fill it — on a tall phone that is considerably more than 4:3, so pinning a ratio there
     * would make those maps *shorter*, the opposite of what the sizing is for. The offline-region
     * picker wants a determinate 4:3 map and passes one.
     *
     * Rejected alternative: putting `aspectRatio` on the caller's own wrapping `Box`, which is
     * what `OfflineMapsPanel` did before. That constrains the whole picker — instruction line and
     * button row included — so the *map* ends up the leftover, measured at 360x146dp on a 360dp
     * phone against a 360x270dp box: a 2.5:1 letterbox, not the 4:3 it looked like. Constraining
     * the map and letting the chrome add its own height is what actually makes the viewport 4:3.
     */
    mapAspectRatio: Float? = null,
    modifier: Modifier = Modifier,
) {
    // "Follow until you touch it" (owner ruling, picker-fixes dispatch F1): until the user's first
    // touch on the map, the region handed to mapSlot, and the pin, follow the caller's region — so a
    // picker opened before any location fix moves to the first one. After that touch, a new caller
    // region changes neither: the find picker's region is the device's live fix, a new one about
    // every second, and before this each one reset the pin and (tracking already ended by the pan)
    // moved SightingsMap's camera back to the device at zoom 13 — the owner's "snaps back after
    // every pan". The touch is MapRenderMode.onUserCameraGesture, not onCameraIdle, because the
    // first activation's ease to zoom 16 and every region-driven camera move end in an idle too.
    //
    // Idle events are still never fed back into mapSlot's region argument: that would re-run
    // SightingsMap's region-keyed camera effect, and zoomForRadiusKm, on every pan.
    var touched by remember { mutableStateOf(false) }
    var mapRegion by remember { mutableStateOf(region) }
    var cameraCenter by remember { mutableStateOf(LatLng(region.lat, region.lng)) }
    if (!touched && region != mapRegion) {
        // A write during composition, of state this composable owns, converging in one pass (the
        // next pass sees region == mapRegion). Chosen over a LaunchedEffect so the map is never
        // handed one frame of the old region after the caller's has changed.
        mapRegion = region
        cameraCenter = LatLng(region.lat, region.lng)
    }
    val onUserCameraGesture = remember { { touched = true } }

    // Fills its slot only when the map is the leftover (mapAspectRatio == null). With a ratio, the
    // map has a determinate height and this column wraps its content, so the caller gets a picker
    // whose height is the map plus its own chrome rather than one that stretches.
    Column(modifier = if (mapAspectRatio == null) modifier.fillMaxSize() else modifier.fillMaxWidth()) {
        Text(
            "Pan the map to position the pin, then confirm.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
        Box(
            modifier = if (mapAspectRatio == null) {
                Modifier.fillMaxWidth().weight(1f)
            } else {
                Modifier.fillMaxWidth().aspectRatio(mapAspectRatio)
            },
        ) {
            mapSlot(
                mapRegion,
                MapOverlayContent(),
                MapRenderMode(basemap, night, onUserCameraGesture = onUserCameraGesture),
                null,
                {},
                {},
                { _, _, _ -> },
                { location -> cameraCenter = location },
                Modifier.fillMaxSize(),
            )
            CentrePin(night = night, modifier = Modifier.align(Alignment.Center))
        }
        CentrePinConfirmRow(
            // UI-defects dispatch, §2: this is the pin's current map position, live from
            // onCameraIdle above — it updates continuously as the map is panned, whether or not
            // OK has ever been pressed. "Selected:" read as a completed pick and contradicted a
            // sibling "No location picked yet" line that reads the *confirmed* pick instead (a
            // different piece of state — see OfflineMapsPanel's own hasValidRegion). Both lines
            // were individually correct; only the wording claimed a selection that hadn't
            // happened yet.
            selectedText = "Pin at: ${"%.4f".format(cameraCenter.lat)}, ${"%.4f".format(cameraCenter.lng)}",
            onConfirm = { onConfirm(cameraCenter) },
            onCancel = onCancel,
        )
    }
}

/**
 * The pin-and-buttons chrome alone, for a site whose map is already on screen showing something
 * else — see this file's class doc comment. [onConfirm] takes no argument: the caller already
 * owns the [mapSlot] call this sits on top of, and is already tracking [MapSlot.onCameraIdle]
 * itself (it has to, to draw anything else that depends on the current viewport), so it already
 * has the coordinate in hand when this fires.
 */
@Composable
fun CentrePinLocationPickerOverlay(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * How far above this overlay's own bottom edge the OK/Cancel row sits. Owner finding on
     * device: the compact Map tab's content Box spans the full screen height (its bottom nav
     * floats over it and slides away in fullscreen, and the app is edge-to-edge), so a row aligned
     * to that Box's bottom lands under the app's nav outside fullscreen and under Android's own
     * navigation bar in it. The caller passes whichever of those is currently underneath: its
     * measured nav height (which already includes the system bar it consumes) or the system
     * navigation-bar inset. Zero keeps the previous behaviour for callers whose Scaffold already
     * pads for its bottom bar.
     */
    bottomInset: Dp = 0.dp,
    /**
     * Night Maps, as the map underneath is drawing it: the caller passes its own
     * `MapRenderMode.night`, so the pin's colours follow the map's (colour build C2 (e)). Defaults to
     * day for callers that have no night value.
     */
    night: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CentrePin(night = night, modifier = Modifier.align(Alignment.Center))
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomInset)
                .fillMaxWidth()
                .testTag(CENTRE_PIN_CONFIRM_ROW_TAG),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            CentrePinConfirmRow(selectedText = null, onConfirm = onConfirm, onCancel = onCancel)
        }
    }
}

/**
 * The centre pin: Material `LocationOn`, filled in [MapPalette.centrePin] with a casing behind it in
 * [MapPalette.casing], both from [MapPalette.forMode] for [night] (colour build C2 (e)). Before C2 it
 * was tinted with the theme's `primary`, which followed the device theme rather than the map. Its
 * shape, 40dp size and anchor are unchanged.
 */
@Composable
private fun CentrePin(night: Boolean, modifier: Modifier = Modifier) {
    val palette = MapPalette.forMode(night)
    val casingVector = remember(palette.casing) { centrePinCasingVector(palette.casing) }
    // LocationOn's drawn point sits at the bottom-centre of its bounding box, not the geometric
    // centre Alignment.Center gives every caller — shifted up by half the icon's own height so the
    // pin's tip, not the icon's box, is what actually marks the coordinate onConfirm reports.
    Box(modifier = modifier.padding(bottom = CENTRE_PIN_SIZE / 2).size(CENTRE_PIN_SIZE)) {
        Icon(
            imageVector = casingVector,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize(),
        )
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Pin marks the location that will be picked",
            tint = Color(palette.centrePin),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * The centre pin's casing: `LocationOn`'s own paths, filled and stroked in [casing] with round joins,
 * the stroke [CASING_WIDTH_DP] wide on each side of the outline at [CENTRE_PIN_SIZE]. Drawn behind
 * the pin, it shows only as the outline outside the pin's edge and round its hole. The paths are
 * copied, not approximated, so the casing matches the icon exactly. `LocationOn`'s root holds its
 * paths directly (no groups, no transforms); anything else is an error rather than a silently wrong
 * outline.
 */
internal fun centrePinCasingVector(casing: Int): ImageVector {
    val source = Icons.Filled.LocationOn
    // The stroke straddles the outline, so its full width is two casings, in viewport units.
    val strokeUnits = 2 * CASING_WIDTH_DP * source.viewportWidth / CENTRE_PIN_SIZE.value
    val builder = ImageVector.Builder(
        name = "LocationOnCasing",
        defaultWidth = source.defaultWidth,
        defaultHeight = source.defaultHeight,
        viewportWidth = source.viewportWidth,
        viewportHeight = source.viewportHeight,
    )
    for (node in source.root) {
        val path = node as? VectorPath ?: error("LocationOn has a ${node::class.simpleName}; the casing copies paths only")
        builder.addPath(
            pathData = path.pathData,
            pathFillType = path.pathFillType,
            fill = SolidColor(Color(casing)),
            stroke = SolidColor(Color(casing)),
            strokeLineWidth = strokeUnits,
            strokeLineJoin = StrokeJoin.Round,
            strokeLineCap = StrokeCap.Round,
        )
    }
    return builder.build()
}

@Composable
private fun CentrePinConfirmRow(selectedText: String?, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (selectedText != null) {
            Text(selectedText, style = MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) { Text("OK") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
        }
    }
}

private val CENTRE_PIN_SIZE = 40.dp

/** [CentrePinLocationPickerOverlay]'s own OK/Cancel surface — what tests measure its real placement by. */
const val CENTRE_PIN_CONFIRM_ROW_TAG = "centre-pin-confirm-row"
