package com.zynergylabs.forager.app.ui.map

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/**
 * Dispatch 2026-09-28-463: the elastic nudge through its real entry point, the map's touch listener
 * handing it each MotionEvent. The map's side is a recording [NudgeCamera]: what MapLibre draws for the
 * padding is device-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NudgeElasticDriverTest {

    private val top = 200.0
    private val events = mutableListOf<MotionEvent>()
    private var following = true

    /** Following ends once this many paddings have been shown: a leave at a set frame, however the frames are timed. */
    private var followingEndsAfter = Int.MAX_VALUE
    private val shown = mutableListOf<DoubleArray>()
    private val camera = object : NudgeCamera {
        override fun canGive() = following && shown.size < followingEndsAfter

        override fun showPadding(padding: DoubleArray) {
            shown += padding
        }
    }
    private val driver = NudgeElasticDriver(camera, thresholdPx = 96f, viewTopPaddingPx = { top })

    @After
    fun recycle() = events.forEach(MotionEvent::recycle)

    private fun touch(action: Int, x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()
        driver.onTouch(MotionEvent.obtain(now, now, action, x, y, 0).also { events += it })
    }

    private fun secondFingerDown() {
        val now = SystemClock.uptimeMillis()
        val props = arrayOf(MotionEvent.PointerProperties().apply { id = 0 }, MotionEvent.PointerProperties().apply { id = 1 })
        val coords = arrayOf(MotionEvent.PointerCoords().apply { x = 20f; y = 0f }, MotionEvent.PointerCoords().apply { x = 200f; y = 0f })
        val action = MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        driver.onTouch(MotionEvent.obtain(now, now, action, 2, props, coords, 0, 0, 1f, 1f, 0, 0, 0, 0).also { events += it })
    }

    private fun springFor(millis: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))

    @Test
    fun `a nudge gives with the finger, then springs back past rest and settles on the view's padding`() {
        touch(MotionEvent.ACTION_DOWN, 0f, 0f)
        touch(MotionEvent.ACTION_MOVE, 40f, 0f)
        assertArrayEquals("half of 40 px, shown by twice that on the left", doubleArrayOf(40.0, top, 0.0, 0.0), shown.last(), 1e-6)
        touch(MotionEvent.ACTION_UP, 40f, 0f)
        springFor(NAVIGATION_NUDGE_SPRING_MILLIS + 100)
        assertTrue("the spring passes rest: the right side opens", shown.any { it[2] > 0.0 })
        assertArrayEquals("settled", doubleArrayOf(0.0, top, 0.0, 0.0), shown.last(), 1e-6)
    }

    @Test
    fun `a drag that left following gets no spring`() {
        touch(MotionEvent.ACTION_DOWN, 0f, 0f)
        touch(MotionEvent.ACTION_MOVE, 90f, 0f)
        following = false // MapLibre ended tracking past the threshold
        touch(MotionEvent.ACTION_MOVE, 150f, 0f)
        val before = shown.size
        touch(MotionEvent.ACTION_UP, 150f, 0f)
        springFor(NAVIGATION_NUDGE_SPRING_MILLIS + 100)
        assertEquals("nothing sent once following ended", before, shown.size)
    }

    @Test
    fun `a second finger springs back what the first gave`() {
        touch(MotionEvent.ACTION_DOWN, 0f, 0f)
        touch(MotionEvent.ACTION_MOVE, 20f, 0f)
        secondFingerDown()
        springFor(NAVIGATION_NUDGE_SPRING_MILLIS + 100)
        assertArrayEquals(doubleArrayOf(0.0, top, 0.0, 0.0), shown.last(), 1e-6)
    }

    @Test
    fun `leaving following mid-spring stops the spring`() {
        touch(MotionEvent.ACTION_DOWN, 0f, 0f)
        touch(MotionEvent.ACTION_MOVE, 40f, 0f)
        // Under Robolectric the whole spring's frames run within the first idle (seen: 18 frames inside
        // 50 ms), so the leave is set by frame, not by time: following ends after the spring's third.
        followingEndsAfter = shown.size + 3
        touch(MotionEvent.ACTION_UP, 40f, 0f)
        springFor(NAVIGATION_NUDGE_SPRING_MILLIS + 100)
        assertEquals("three spring frames, then nothing once following ended", followingEndsAfter, shown.size)
    }
}
