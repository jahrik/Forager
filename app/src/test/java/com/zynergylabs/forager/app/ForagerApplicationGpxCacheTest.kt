package com.zynergylabs.forager.app

import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * F5 (dispatch 2026-09-28-216; owner, "3 A"): exported GPX files more than an hour old are deleted from the cache
 * at app start, and one within the hour is kept.
 *
 * Through the real entry point, [ForagerApplication.onCreate]. Robolectric has already run it once from the
 * manifest before the body starts, on an empty cache, so the body puts two exports in the cache and starts the
 * app again with the same `onCreate` a new process runs. The clean-up runs off the main thread, as the capture
 * sweep beside it does, so the check polls; the files are dated against the real clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ForagerApplicationGpxCacheTest {

    @Test
    fun `app start deletes GPX exports more than an hour old from the cache and keeps one within the hour`() {
        val app = ApplicationProvider.getApplicationContext<ForagerApplication>()
        val dir = File(app.cacheDir, "tracks").apply { mkdirs() }
        val now = System.currentTimeMillis()
        val stale = File(dir, "forager-track-2025-08-01-090000.gpx").apply { writeText("<gpx/>"); assertTrue(setLastModified(now - 61 * MINUTE_MILLIS)) }
        val recent = File(dir, "forager-track-2025-08-02-090000.gpx").apply { writeText("<gpx/>"); assertTrue(setLastModified(now - 59 * MINUTE_MILLIS)) }

        app.onCreate()

        val deadline = System.currentTimeMillis() + 5_000
        while (stale.exists() && System.currentTimeMillis() < deadline) Thread.sleep(20)
        assertFalse("the export more than an hour old is deleted at app start", stale.exists())
        // The pass that deleted it may still be walking the folder; give it time to reach the other file.
        Thread.sleep(300)
        assertTrue("the export within the hour is kept", recent.exists())
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
