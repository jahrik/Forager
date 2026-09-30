package com.zynergylabs.forager.app.ui.map.fanout

/**
 * Reopens the fan a user left when they opened a find on the Journal, on the Maps tab that Back creates again
 * (dispatch 2026-09-28-267, Part A). It is the decision `SightingsMap` used to make inline inside its camera-idle
 * listener, moved here so the order of the map's own events is testable with the real [MapTapHandler]: the
 * headless tests before it never ran the effect that folds a fan when the loaded style changes, which is what
 * folded the reopened fan two milliseconds after it opened on the device.
 *
 * [onContentEffect] is the map's "what it draws changed" effect (it folds an open fan). [onCameraIdle] is its camera-idle listener.
 */
class FanReopenCoordinator(
    private val handler: () -> MapTapHandler?,
    /** `MapReturnMemory.takeFanKeys`: the keys waiting for this map, once; `null` when none. */
    private val takeKeys: () -> List<FanKey>?,
    /** A reopen that comes to nothing is reported here, never silent. */
    private val onUnavailable: (Int) -> Unit,
) {
    // The reopen waits for both of the map's own events after its style loads: the content effect (which folds any
    // open fan, so a fan opened before it runs is folded at once) and a camera idle (the camera restore has settled).
    private var contentEffectRan = false
    private var idleSeen = false

    fun onContentEffect(styleLoaded: Boolean, style: Any? = styleLoaded) {
        handler()?.onContentChanged()
        if (styleLoaded) contentEffectRan = true
        reopenWhenReady()
    }

    fun onCameraIdle(styleLoaded: Boolean) {
        if (styleLoaded) idleSeen = true
        reopenWhenReady()
    }

    private fun reopenWhenReady() {
        if (!contentEffectRan || !idleSeen) return
        takeKeys()?.let { keys ->
            if (handler()?.openFanFor(keys) != true) onUnavailable(keys.size)
        }
    }
}
