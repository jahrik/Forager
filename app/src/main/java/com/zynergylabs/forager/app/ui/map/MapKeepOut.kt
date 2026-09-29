package com.zynergylabs.forager.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.zynergylabs.forager.app.ui.map.fanout.FanRect
import com.zynergylabs.forager.app.ui.map.fanout.FanSpace
import com.zynergylabs.forager.app.ui.map.fanout.MapKeepOuts

/** STUB (tests-first commit of the continuation): nothing registers and the space is unbounded. */
val LocalMapKeepOuts = staticCompositionLocalOf<MapKeepOuts?> { null }

internal object MapKeepOutIds {
    const val CLUSTER = "icon-cluster"
    const val LEGEND = "legend"
    const val CHIPS = "chip-row"
    const val BOTTOM_NAV = "bottom-nav"
    const val RAIL = "navigation-rail"
}

fun Modifier.mapKeepOut(id: String): Modifier = this

internal class MapFanSpace(private val keepOuts: MapKeepOuts?) : FanSpace {
    var rootOffset: Offset = Offset.Zero
    var size: IntSize = IntSize.Zero
    override fun boundsPx(): FanRect? = null
    override fun keepOutsPx(): List<FanRect> = emptyList()
}

@Composable
internal fun rememberMapFanSpace(): MapFanSpace {
    val keepOuts = LocalMapKeepOuts.current
    return remember(keepOuts) { MapFanSpace(keepOuts) }
}

internal fun Modifier.trackMapFanSpace(space: MapFanSpace): Modifier = onGloballyPositioned {
    space.rootOffset = it.positionInRoot()
    space.size = it.size
}

@Suppress("unused")
private val unusedDensity = LocalDensity
