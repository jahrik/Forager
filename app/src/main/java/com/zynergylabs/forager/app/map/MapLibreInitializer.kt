package com.zynergylabs.forager.app.map

import android.content.Context

/**
 * Initialises MapLibre's native side once. Step-1 stub: base behaviour (every call reaches the SDK,
 * nothing is logged), so the tests written against the intended behaviour fail on it.
 */
internal class MapLibreInitializer(
    private val getInstance: (Context) -> Unit,
    private val info: (String) -> Unit,
    private val error: (String, Throwable) -> Unit,
) {
    fun initialize(context: Context) {
        getInstance(context.applicationContext)
    }
}
