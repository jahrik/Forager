package com.zynergylabs.forager.app.ui.map

import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.layers.LayerKind
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.TapGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.hypot

/**
 * THROWAWAY (dispatch -369, amendment 2): logs only, to measure the open's gap and the signal's frame count. Not part of any fix.
 * Called right after the old hide-then-push, so what it sees is the base behaviour.
 */
internal object FanGapProbe {
    private const val TAG = "FANGAP"

    fun start(scope: CoroutineScope, mapView: MapView, map: MapLibreMap, members: List<FanMember>, density: Float, progress: () -> Float) {
        val t0 = SystemClock.elapsedRealtimeNanos()
        fun ms() = (SystemClock.elapsedRealtimeNanos() - t0) / 1e6
        val maxOffsetDp = members.maxOf { hypot(it.offset.xDp, it.offset.yDp) }
        val origLayers = MAP_LAYER_REGISTRY.filter { it.tapGroup == TapGroup.MARKER || (it.kind == LayerKind.MARKER && it.drawnWith != null) }.map { it.id }.toTypedArray()
        val at = map.projection.toScreenLocation(LatLng(members.first().lat, members.first().lng))
        val r = 60f * density
        val origBox = RectF(at.x - r, at.y - r, at.x + r, at.y + r)
        val wide = 140f * density
        val copyBox = RectF(at.x - wide, at.y - wide, at.x + wide, at.y + wide)
        Log.i(TAG, "open members=${members.size} (hide+push were called just before this); maxOffsetDp=%.1f".format(maxOffsetDp))
        var n = 0
        lateinit var probe: MapView.OnDidFinishRenderingFrameListener
        probe = MapView.OnDidFinishRenderingFrameListener { _, _, _ ->
            n++
            val copies = map.queryRenderedFeatures(copyBox, FanOutIds.ICONS_LAYER, FanOutIds.DOTS_LAYER).size
            val origs = map.queryRenderedFeatures(origBox, *origLayers).size
            Log.i(TAG, "frame $n t=%.1f ms copies=$copies originals=$origs progress=%.2f copyOffsetDp=%.1f".format(ms(), progress(), maxOffsetDp * progress()))
            if (n >= 40) {
                mapView.removeOnDidFinishRenderingFrameListener(probe)
                Log.i(TAG, "probe listener removed after 40 frames")
            }
        }
        mapView.addOnDidFinishRenderingFrameListener(probe)
        scope.launch {
            var frames = 0
            val ok = awaitCopiesRendered(mapView, map, members) { frames = it }
            Log.i(TAG, "SIGNAL ok=$ok after $frames queried frame(s) at t=%.1f ms progress=%.2f copyOffsetDp=%.1f; its listener is removed".format(ms(), progress(), maxOffsetDp * progress()))
        }
    }
}
