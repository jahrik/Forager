package com.zynergylabs.forager.app.ui.map

import android.os.SystemClock
import android.view.MotionEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-477: the fling guard through its real entry point, the map view's touch listener
 * handing it each MotionEvent. MapLibre's fling is a recording [FlingSwitch]; what MapLibre then does
 * with a release is device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NudgeFlingGuardTest {

    private val events = mutableListOf<MotionEvent>()
    private val writes = mutableListOf<Boolean>()
    private val fling = object : FlingSwitch {
        override var enabled: Boolean = true
            set(value) {
                field = value
                writes += value
            }
    }
    private val guard = NudgeFlingGuard(fling)

    @After
    fun recycle() = events.forEach(MotionEvent::recycle)

    private fun touch(action: Int, stillFollowing: Boolean) {
        val now = SystemClock.uptimeMillis()
        guard.onTouch(MotionEvent.obtain(now, now, action, 0f, 0f, 0).also { events += it }, stillFollowing)
    }

    @Test
    fun `a release while still following has no fling, and the next touch has it back`() {
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        touch(MotionEvent.ACTION_MOVE, stillFollowing = true)
        touch(MotionEvent.ACTION_UP, stillFollowing = true)
        assertEquals("off for this release", false, fling.enabled)
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        assertEquals("on again before MapLibre sees the next touch", true, fling.enabled)
    }

    @Test
    fun `a release after a drag left following keeps its fling`() {
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        touch(MotionEvent.ACTION_MOVE, stillFollowing = false) // past the threshold: MapLibre ended following
        touch(MotionEvent.ACTION_UP, stillFollowing = false)
        assertEquals(true, fling.enabled)
        assertTrue("never touched", writes.isEmpty())
    }

    @Test
    fun `outside navigation the fling is never touched`() {
        repeat(3) {
            touch(MotionEvent.ACTION_DOWN, stillFollowing = false)
            touch(MotionEvent.ACTION_UP, stillFollowing = false)
        }
        assertTrue(writes.isEmpty())
    }

    @Test
    fun `a flick after a guarded release is guarded again, and a drag after it has its fling back`() {
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        touch(MotionEvent.ACTION_UP, stillFollowing = true)
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        touch(MotionEvent.ACTION_UP, stillFollowing = true)
        assertEquals(false, fling.enabled)
        touch(MotionEvent.ACTION_DOWN, stillFollowing = true)
        touch(MotionEvent.ACTION_UP, stillFollowing = false)
        assertEquals(true, fling.enabled)
        assertEquals(listOf(false, true, false, true), writes)
    }
}
