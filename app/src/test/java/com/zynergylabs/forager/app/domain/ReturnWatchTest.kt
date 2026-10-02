package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.TrackPoint
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [ReturnWatch] on a plain JVM: no Activity, no Service, no Compose tree, no coroutine.
 *
 * ## The first eight tests were moved here, one for one
 *
 * They were in `TrackRecordingViewModelTest` while the off-track decision lived in the ViewModel
 * (dispatch 2026-09-28-400, Amendment 2, ruling 4). Each keeps its name and its assertions. What
 * changed is how the readings arrive: there, `vm.returnToStart(point)` after a recording was
 * started and a breadcrumb polled; here, [ReturnWatch.onFix] after [ReturnWatch.begin] and
 * [ReturnWatch.setStartPoint], which is how the recording service and the ViewModel drive it.
 * The points, the clock and the expected values are the same. The table that maps old to new is in
 * `docs/navigation/2026-10-02-alerts-in-service-part-2-completion-report.md`.
 */
class ReturnWatchTest {

    /** Every [Alert] the watch handed over, in order. Synchronized: the two-caller test delivers from two threads. */
    private val delivered = java.util.Collections.synchronizedList(mutableListOf<Alert>())
    private var nowMillis = 1_000L

    private fun watch(clock: CurrentTimeProvider = CurrentTimeProvider { nowMillis }) = ReturnWatch(
        computeReturnToStart = ComputeReturnToStartUseCase(),
        detectOffTrack = DetectOffTrackUseCase(),
        alertDelivery = { delivered += it },
        currentTime = clock,
    )

    private fun point(lat: Double, lng: Double = -122.0, t: Long) =
        TrackPoint(lat = lat, lng = lng, altitude = null, accuracyMeters = null, timestampEpochMillis = t)

    /** A watch the service has begun for `track-1`, with the start at 45.0: where each moved test's own setup left the ViewModel. */
    private fun begunWatch(clock: CurrentTimeProvider = CurrentTimeProvider { nowMillis }) = watch(clock).apply {
        begin("track-1")
        setStartPoint("track-1", point(lat = 45.0, lng = -122.0, t = 1_000L))
    }

    // ---- The eight moved tests ------------------------------------------------------------------

    @Test
    fun `moving steadily away from the start while returning sets off-track`() {
        val watch = begunWatch()
        watch.startReturn("track-1")

        // Each step ~111m further north of the start point — well past the 25m/3-reading
        // net-increase threshold DetectOffTrackUseCase uses.
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))

        assertTrue(watch.state.value.isOffTrack)
    }

    /**
     * Exactly one delivery per event, asserted as a count, not as the absence of a second. Fails
     * with the `alertDelivery.deliver` call removed (0 deliveries).
     */
    @Test
    fun `going off-track delivers exactly one alert, not one per fix`() {
        val watch = begunWatch(CurrentTimeProvider { 1_000L })
        watch.startReturn("track-1")
        assertEquals(0, delivered.size)

        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))
        assertTrue(watch.state.value.isOffTrack)
        assertEquals(1, delivered.size)
        // Off-track passes overridesSilence = false (owner ruling 2026-09-11, reversing the
        // original true) — asserted on the value the
        // delivery received, since the parameter exists precisely so it is never assumed.
        assertEquals(Alert(kind = AlertKind.OFF_TRACK, overridesSilence = false), delivered.single())

        // Still off-track (net distance keeps increasing) on the very next fix, same clock instant
        // — the cooldown, not the heuristic, is what must keep this from delivering again immediately.
        watch.onFix(point(lat = 45.004, lng = -122.0, t = 5_000L))
        assertEquals(1, delivered.size)
    }

    @Test
    fun `a sustained drift alerts again once the cooldown elapses`() {
        val watch = begunWatch()
        watch.startReturn("track-1")

        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))
        assertEquals(1, delivered.size)

        // Just short of the cooldown: still just the one alert.
        nowMillis += OFF_TRACK_ALERT_COOLDOWN_MILLIS - 1
        watch.onFix(point(lat = 45.004, lng = -122.0, t = 5_000L))
        assertEquals(1, delivered.size)

        // Cooldown elapsed, and the drift continues: a second, real reminder.
        nowMillis += 1
        watch.onFix(point(lat = 45.005, lng = -122.0, t = 6_000L))
        assertEquals(2, delivered.size)
    }

    @Test
    fun `staying on track never delivers an alert`() {
        val watch = begunWatch()
        watch.startReturn("track-1")

        watch.onFix(point(lat = 45.003, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 4_000L))

        assertEquals(0, delivered.size)
    }

    @Test
    fun `stopReturn resets the cooldown so a later return attempt can alert immediately`() {
        val watch = begunWatch(CurrentTimeProvider { 1_000L })
        watch.startReturn("track-1")
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))
        assertEquals(1, delivered.size)

        watch.stopReturn("track-1")
        watch.startReturn("track-1")
        // Same fixed clock instant as the first alert — without the cooldown reset on stopReturn(),
        // this would be blocked exactly like the immediate-repeat case above.
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 5_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 6_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 7_000L))

        assertEquals(2, delivered.size)
    }

    @Test
    fun `moving steadily toward the start while returning stays on track`() {
        val watch = begunWatch()
        watch.startReturn("track-1")

        watch.onFix(point(lat = 45.003, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 4_000L))

        assertFalse(watch.state.value.isOffTrack)
    }

    @Test
    fun `moving away from the start does not set off-track unless actively returning`() {
        val watch = begunWatch()

        // No startReturn() call — this is ordinary outbound travel.
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))

        assertFalse(watch.state.value.isOffTrack)
    }

    @Test
    fun `stopping the recording clears returning and off-track state`() {
        val watch = begunWatch()
        watch.startReturn("track-1")
        watch.onFix(point(lat = 45.001, lng = -122.0, t = 2_000L))
        watch.onFix(point(lat = 45.002, lng = -122.0, t = 3_000L))
        watch.onFix(point(lat = 45.003, lng = -122.0, t = 4_000L))
        assertTrue(watch.state.value.isOffTrack)

        watch.end("track-1") // what the service calls when the recording stops

        assertFalse(watch.state.value.isReturning)
        assertFalse(watch.state.value.isOffTrack)
    }

    // ---- New with the class ---------------------------------------------------------------------

    /**
     * Amendment 2, ruling 3: the service begins the watch, and that happens a moment after the
     * Record tap (the ViewModel creates the track row, `MainActivity` sends the start, the service
     * handles it). A Return tapped inside that moment must not be lost.
     */
    @Test
    fun `a return asked for before the service has begun the watch is kept for that track`() {
        val watch = watch()

        assertTrue("an early return for the track about to be recorded is accepted", watch.startReturn("track-1"))
        assertTrue("and the screen can show it at once", watch.state.value.isReturning)
        watch.setStartPoint("track-1", point(lat = 45.0, t = 1_000L))

        watch.begin("track-1")
        assertTrue("beginning the same track keeps the return", watch.state.value.isReturning)

        watch.onFix(point(lat = 45.001, t = 2_000L))
        watch.onFix(point(lat = 45.002, t = 3_000L))
        watch.onFix(point(lat = 45.003, t = 4_000L))
        assertEquals("and the early start point was kept too, so the walk away alerts", 1, delivered.size)
    }

    @Test
    fun `an early return for one track is dropped when the service begins a different one`() {
        val watch = watch()
        watch.startReturn("track-1")
        watch.setStartPoint("track-1", point(lat = 45.0, t = 1_000L))

        watch.begin("track-2")

        assertEquals(ReturnWatchState(trackId = "track-2"), watch.state.value)
    }

    @Test
    fun `while begun for one track, a return or a start point for another is refused and changes nothing`() {
        val watch = begunWatch()
        val before = watch.state.value

        assertFalse(watch.startReturn("track-2"))
        watch.setStartPoint("track-2", point(lat = 10.0, t = 1_000L))
        watch.stopReturn("track-2")
        watch.end("track-2")

        assertEquals(before, watch.state.value)
        watch.startReturn("track-1")
        watch.onFix(point(lat = 45.001, t = 2_000L))
        assertEquals("the start is still the one given for track-1", 111.2, watch.state.value.returnToStart!!.distanceMeters, 1.0)
    }

    @Test
    fun `a fix before the service has begun the watch is not a reading`() {
        val watch = watch()
        watch.startReturn("track-1")
        watch.setStartPoint("track-1", point(lat = 45.0, t = 1_000L))

        watch.onFix(point(lat = 45.001, t = 2_000L))
        watch.onFix(point(lat = 45.002, t = 3_000L))
        watch.onFix(point(lat = 45.003, t = 4_000L))

        assertEquals(0, delivered.size)
        assertNull(watch.state.value.returnToStart)
    }

    @Test
    fun `with no start point there is nothing to measure against, so nothing is decided`() {
        val watch = watch()
        watch.begin("track-1")
        watch.startReturn("track-1")

        watch.onFix(point(lat = 45.001, t = 2_000L))
        watch.onFix(point(lat = 45.002, t = 3_000L))
        watch.onFix(point(lat = 45.003, t = 4_000L))

        assertEquals(0, delivered.size)
        assertFalse(watch.state.value.isOffTrack)
        assertNull(watch.state.value.returnToStart)
    }

    @Test
    fun `every fix updates the distance to the start, returning or not`() {
        val watch = begunWatch()

        watch.onFix(point(lat = 45.001, t = 2_000L))
        assertEquals(111.2, watch.state.value.returnToStart!!.distanceMeters, 1.0)
        assertEquals(180.0, watch.state.value.returnToStart!!.bearingDegrees, 0.01)

        watch.startReturn("track-1")
        watch.onFix(point(lat = 45.002, t = 3_000L))
        assertEquals(222.4, watch.state.value.returnToStart!!.distanceMeters, 1.0)
    }

    @Test
    fun `ending the recording leaves nothing behind for the next one`() {
        val watch = begunWatch(CurrentTimeProvider { 1_000L })
        watch.startReturn("track-1")
        watch.onFix(point(lat = 45.001, t = 2_000L))
        watch.onFix(point(lat = 45.002, t = 3_000L))
        watch.onFix(point(lat = 45.003, t = 4_000L))
        assertEquals(1, delivered.size)

        watch.end("track-1")
        assertEquals(ReturnWatchState(), watch.state.value)

        // Same clock instant: a cooldown carried over from the first recording would block this.
        watch.begin("track-2")
        watch.setStartPoint("track-2", point(lat = 45.0, t = 5_000L))
        watch.startReturn("track-2")
        watch.onFix(point(lat = 45.001, t = 6_000L))
        watch.onFix(point(lat = 45.002, t = 7_000L))
        watch.onFix(point(lat = 45.003, t = 8_000L))
        assertEquals(2, delivered.size)
    }

    @Test
    fun `ending with no track named ends whatever is begun, as the service does when it is destroyed`() {
        val watch = begunWatch()
        watch.startReturn("track-1")

        watch.end(null)

        assertEquals(ReturnWatchState(), watch.state.value)
    }

    /**
     * Amendment 2, ruling 9: the watch keeps only the readings the check reads, where the
     * ViewModel kept every reading of a return. This runs the old arithmetic beside the new, on
     * the same readings: the real [DetectOffTrackUseCase] over a list that is never trimmed, with
     * the same cooldown. 200 random walks of 400 readings, steps large enough to cross the
     * threshold both ways, with the clock moving so the cooldown opens and closes. Every reading's
     * "off track now" and every delivery must match.
     *
     * It would also catch the check's window growing past what the watch keeps: the untrimmed side
     * would then see a reading the watch had dropped.
     */
    @Test
    fun `keeping only the readings the check reads decides exactly as keeping every reading did`() {
        val random = Random(20261002)
        val detect = DetectOffTrackUseCase()
        repeat(200) { walk ->
            delivered.clear()
            nowMillis = 1_000L
            val watch = begunWatch()
            watch.startReturn("track-1")

            val everyReading = mutableListOf<Double>()
            var lastAlertAt: Long? = null
            var expectedDeliveries = 0
            var lat = 45.0
            repeat(400) { step ->
                lat += random.nextDouble(-0.0003, 0.0003) // up to about 33 m either way per reading
                nowMillis += random.nextLong(1_000L, 40_000L)
                val current = point(lat = lat, t = nowMillis)

                everyReading += ComputeReturnToStartUseCase()(current, point(lat = 45.0, t = 1_000L)).distanceMeters
                val expectedOffTrack = detect(everyReading)
                val last = lastAlertAt
                if (expectedOffTrack && (last == null || nowMillis - last >= OFF_TRACK_ALERT_COOLDOWN_MILLIS)) {
                    lastAlertAt = nowMillis
                    expectedDeliveries++
                }

                watch.onFix(current)

                assertEquals("walk $walk, reading $step: off track now", expectedOffTrack, watch.state.value.isOffTrack)
                assertEquals("walk $walk, reading $step: deliveries so far", expectedDeliveries, delivered.size)
            }
            assertTrue("walk $walk never went off track, so it compared nothing", expectedDeliveries > 0)
            assertEquals("the watch holds only what the check reads", 3, watch.keptReadingCount)
        }
    }

    /**
     * Amendment 2, ruling 7: two callers at once. The service feeds fixes on a background thread
     * while the ViewModel calls from the main thread. Here two threads feed the same watch the
     * same walk away from the start, released together, with the clock fixed so the cooldown can
     * open only once. One alert is the only right answer: two would mean both threads read "no
     * alert yet" before either wrote it down. Repeated, because a race does not show every time.
     */
    @Test
    fun `two threads feeding the same walk away deliver one alert, not two`() {
        repeat(300) { round ->
            delivered.clear()
            val watch = begunWatch(CurrentTimeProvider { 1_000L })
            watch.startReturn("track-1")
            watch.onFix(point(lat = 45.001, t = 2_000L))
            watch.onFix(point(lat = 45.002, t = 3_000L))

            val together = CyclicBarrier(2)
            val failure = AtomicReference<Throwable?>(null)
            val done = CountDownLatch(2)
            repeat(2) { feeder ->
                thread {
                    try {
                        together.await()
                        repeat(50) { i -> watch.onFix(point(lat = 45.003 + (i * 2 + feeder) * 0.001, t = 4_000L + i)) }
                    } catch (t: Throwable) {
                        failure.compareAndSet(null, t)
                    } finally {
                        done.countDown()
                    }
                }
            }
            done.await()

            assertNull("round $round: a feeder threw", failure.get())
            assertEquals("round $round: deliveries", 1, delivered.size)
            assertEquals("round $round: readings kept", 3, watch.keptReadingCount)
        }
    }

    /** The same, with the second caller being the screen: a Return toggled while fixes arrive must never throw or leave a half-written state. */
    @Test
    fun `toggling the return from one thread while another feeds fixes never throws`() {
        val watch = begunWatch()
        val failure = AtomicReference<Throwable?>(null)
        val together = CyclicBarrier(2)
        val feeder = thread {
            try {
                together.await()
                repeat(20_000) { i -> watch.onFix(point(lat = 45.0 + (i % 50) * 0.001, t = 2_000L + i)) }
            } catch (t: Throwable) {
                failure.compareAndSet(null, t)
            }
        }
        val screen = thread {
            try {
                together.await()
                repeat(20_000) { i -> if (i % 2 == 0) watch.startReturn("track-1") else watch.stopReturn("track-1") }
            } catch (t: Throwable) {
                failure.compareAndSet(null, t)
            }
        }
        feeder.join()
        screen.join()

        assertNull(failure.get())
        watch.stopReturn("track-1")
        assertFalse(watch.state.value.isReturning)
        assertFalse(watch.state.value.isOffTrack)
        assertEquals("a stopped return keeps no readings", 0, watch.keptReadingCount)
    }

    private companion object {
        /** Mirrors [ReturnWatch]'s own constant, as `TrackRecordingViewModelTest` mirrored the ViewModel's. */
        const val OFF_TRACK_ALERT_COOLDOWN_MILLIS = 120_000L
    }
}
