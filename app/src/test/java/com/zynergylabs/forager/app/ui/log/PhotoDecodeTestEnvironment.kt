package com.zynergylabs.forager.app.ui.log

import android.os.Handler
import android.os.Looper
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * The unit tests' side of [PhotoDecodeDispatcher] (dispatch 2026-09-28-351).
 *
 * [mainLooperDispatcher] posts every block to the main looper, so a decode runs on the main test
 * thread, in a looper task, never inside a Compose frame, and `withContext` returns on that same
 * thread. Chosen over `Dispatchers.Main` / `Dispatchers.Main.immediate`: forty-odd test classes call
 * `Dispatchers.setMain`, and a `StandardTestDispatcher` installed there would silently stop every
 * decode from running; this dispatcher does not read it. Chosen posted rather than immediate: an
 * immediate decode would run inside the effect's own launch, so a test could never see the
 * placeholder first, where a real IO decode always lets it. The cost is that the decode only runs
 * when something idles the main looper, which `waitForIdle` and `waitUntil` do.
 *
 * [installDefault] is called before every Robolectric test by [PhotoDecodeTestLifecycle], so it also
 * undoes an earlier test's [useBackgroundDecode].
 */
internal object PhotoDecodeTestEnvironment {
    val mainLooperDispatcher: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Handler(Looper.getMainLooper()).post(block)
        }
    }

    fun installDefault() = PhotoDecodeDispatcher.override(mainLooperDispatcher)

    /** For a test that needs the decode off the main thread (one that holds it on a latch): a real IO worker, as in production. */
    fun useBackgroundDecode() = PhotoDecodeDispatcher.override(Dispatchers.IO)
}
