package com.zynergylabs.forager.app.map

import android.content.Context

/**
 * Initialises MapLibre's native side once per process. `MapLibre.getInstance` is itself safe to
 * repeat, but this makes the first success and every failure visible in the log, and lets a test
 * replace the SDK call ([getInstance]): MapLibre's native library cannot load under Robolectric.
 *
 * A failure is logged and rethrown, never swallowed, and [initialize] tries again on the next call
 * (nothing is remembered as done until the SDK call returns).
 */
internal class MapLibreInitializer(
    private val getInstance: (Context) -> Unit,
    private val info: (String) -> Unit,
    private val error: (String, Throwable) -> Unit,
) {
    private var initialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        try {
            getInstance(context.applicationContext)
        } catch (e: Throwable) {
            // Throwable, not Exception: a missing or unloadable native library is an UnsatisfiedLinkError.
            error("MapLibre initialisation failed; it will be retried on the next call.", e)
            throw e
        }
        initialized = true
        info("MapLibre initialised.")
    }
}
