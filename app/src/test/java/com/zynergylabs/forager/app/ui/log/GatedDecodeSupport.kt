package com.zynergylabs.forager.app.ui.log

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test-only control over when [DecodedPhoto]'s `Dispatchers.IO` decode finishes (dispatch
 * 2026-09-28-317). Production has no seam for it: `BitmapFactory.decodeFile` is called directly, so
 * in a test the swap from placeholder to image lands at a moment set by a real IO thread, off the
 * Compose test clock. `GatedBitmapFactoryShadow` (Java, beside this file, because a Robolectric static shadow method needs a real static), registered per class with
 * `@Config(shadows = [GatedBitmapFactoryShadow::class])`, holds every `decodeFile` call until the
 * test opens [hold]'s latch, so a test can place the swap between a gesture's `down` and `up`
 * without any production hook.
 *
 * Both fields are process-wide, so a test that sets [hold] must clear it in `@After`
 * ([reset]); an IO thread left waiting gives up after [WAIT_SECONDS] rather than hanging the JVM.
 */
internal object GatedDecode {
    @Volatile var hold: CountDownLatch? = null

    /** Counted down by the shadow once per `decodeFile` call that returned, so a test knows the IO side is done. */
    @Volatile var finished: CountDownLatch? = null

    /** Holds every decode until [release], and expects [decodes] of them to finish. */
    fun holdDecodes(decodes: Int) {
        hold = CountDownLatch(1)
        finished = CountDownLatch(decodes)
    }

    fun release() {
        hold?.countDown()
    }

    /** True once [decodes] calls have returned; false on timeout, so the caller can fail with a message. */
    fun awaitFinished(): Boolean = finished?.await(WAIT_SECONDS, TimeUnit.SECONDS) ?: true

    fun reset() {
        hold?.countDown()
        hold = null
        finished = null
    }

    @JvmField val WAIT_SECONDS: Long = 10L
}
