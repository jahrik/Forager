package com.zynergylabs.forager.app.map

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * [MapLibreInitializer] (dispatch 2026-09-28-106, F2), with the SDK call replaced by a lambda:
 * MapLibre's native library cannot load under Robolectric, so what these tests reach is the
 * once-only, retry-after-failure and logging behaviour, not `MapLibre.getInstance` itself. The
 * application-start test drives the real `ForagerApplication` that Robolectric boots and reads its
 * log; on the JVM the real call fails, and that failure being logged is what it observes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MapLibreInitializerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val infos = mutableListOf<String>()
    private val errors = mutableListOf<Pair<String, Throwable>>()

    private fun initializer(getInstance: (Context) -> Unit) =
        MapLibreInitializer(getInstance, { infos += it }, { message, cause -> errors += message to cause })

    @Test
    fun `the SDK is initialised once however many callers ask`() {
        var calls = 0
        val initializer = initializer { calls++ }

        repeat(3) { initializer.initialize(context) }

        assertEquals(1, calls)
        assertEquals(1, infos.size)
    }

    @Test
    fun `a failed initialisation is logged and rethrown, and the next call tries again`() {
        val boom = IllegalStateException("boom")
        var calls = 0
        val initializer = initializer { if (calls++ == 0) throw boom }

        val thrown = assertThrows(IllegalStateException::class.java) { initializer.initialize(context) }
        assertSame(boom, thrown)
        assertEquals(1, errors.size)
        assertSame(boom, errors.single().second)

        initializer.initialize(context)
        initializer.initialize(context)
        assertEquals(2, calls)
    }

    @Test
    fun `the application initialises MapLibre at start, before any screen or read`() {
        val logged = ShadowLog.getLogs().filter { it.tag == "MapLibreStorage" }

        assertTrue("no MapLibre initialisation was attempted at application start: $logged", logged.isNotEmpty())
    }
}
