package com.zynergylabs.forager.app.ui.map.fanout

/** STUB (tests-first commit of the continuation): placement is not built yet. */

/** A rectangle, in whatever unit its user says (dp in [fanShift], px in [FanSpace]). */
data class FanRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

/** The room a fan has: the map's visible bounds and the measured bounds of the controls over it, in px of the map view. */
interface FanSpace {
    fun boundsPx(): FanRect?
    fun keepOutsPx(): List<FanRect>

    companion object {
        val Unbounded: FanSpace = object : FanSpace {
            override fun boundsPx(): FanRect? = null
            override fun keepOutsPx(): List<FanRect> = emptyList()
        }
    }
}

/** Where [fanShift] put the fan, and how well it did. */
data class FanShift(val shift: FanOffset, val allOnScreen: Boolean, val controlsCleared: Int, val controlsTotal: Int)

fun fanShift(centreX: Float, centreY: Float, ring: List<FanOffset>, bounds: FanRect?, keepOuts: List<FanRect>): FanShift =
    FanShift(FanOffset(0f, 0f), allOnScreen = true, controlsCleared = keepOuts.size, controlsTotal = keepOuts.size)

/** The controls' measured bounds, by name, in root px, written by [com.zynergylabs.forager.app.ui.map.mapKeepOut] as each is laid out. */
class MapKeepOuts {
    private val rects = LinkedHashMap<String, FanRect>()
    fun set(id: String, rect: FanRect) { rects[id] = rect }
    fun remove(id: String) { rects.remove(id) }
    fun snapshot(): Map<String, FanRect> = LinkedHashMap(rects)
}

/** The layers that fan: the owner's own records. STUB: still every marker layer. */
fun fanOutLayerIds(drawOrder: List<com.zynergylabs.forager.app.ui.map.layers.MapLayerSpec>): List<String> =
    drawOrder.filter { it.tapGroup == com.zynergylabs.forager.app.ui.map.layers.TapGroup.MARKER }.map { it.id }
