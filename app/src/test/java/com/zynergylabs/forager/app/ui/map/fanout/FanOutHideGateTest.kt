package com.zynergylabs.forager.app.ui.map.fanout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The order in which a fan's originals are hidden (dispatch 2026-09-28-369, amendment -371): not before their copies are
 * drawn, and never left hidden, or shown beside an open fan, by a fold, a release, another stack or a late signal. The
 * pure gate only; that the map reports the signal from a real rendered frame, and that the filters then change, is
 * device-only (a `MapView` cannot be built under Robolectric), so the recordings are the evidence for that part.
 */
class FanOutHideGateTest {

    private fun member(layer: String, id: String) = FanMember(FanKey(layer, id), 45.0, -122.0, 0f, 0f, FanOffset(0f, -48f))

    private val a1 = member("finds", "a1")
    private val a2 = member("photos", "a2")
    private val b1 = member("finds", "b1")
    private val fanA = listOf(a1, a2)
    private val fanB = listOf(b1)

    @Test
    fun `a new fan hides nothing until its copies are drawn`() {
        val step = FanOutHideGate().onMembers(fanA)

        assertEquals("no original may be hidden before the signal", emptyList<FanMember>(), step.hide)
        assertTrue("the fan is waiting for its copies", step.awaiting)
    }

    @Test
    fun `the signal hides the originals of the fan it was waiting for`() {
        val gate = FanOutHideGate()
        val waiting = gate.onMembers(fanA)

        val done = gate.onCopiesDrawn(waiting.generation)

        assertNotNull(done)
        assertEquals(fanA, done!!.hide)
        assertFalse(done.awaiting)
        assertFalse("a signal is not the fallback", done.viaFallback)
    }

    @Test
    fun `a signal that arrives twice hides once`() {
        val gate = FanOutHideGate()
        val waiting = gate.onMembers(fanA)
        gate.onCopiesDrawn(waiting.generation)

        assertNull(gate.onCopiesDrawn(waiting.generation))
    }

    @Test
    fun `a signal for an older fan is ignored once another has opened`() {
        val gate = FanOutHideGate()
        val first = gate.onMembers(fanA)
        gate.onMembers(fanB)

        assertNull("the stale signal must not hide the first fan's originals", gate.onCopiesDrawn(first.generation))
    }

    @Test
    fun `a fold before the signal leaves the originals shown and the late signal does nothing`() {
        val gate = FanOutHideGate()
        val waiting = gate.onMembers(fanA)

        gate.onFold()

        assertNull(gate.onCopiesDrawn(waiting.generation))
        assertNull("the fallback must not hide a fan that is folding", gate.onRest())
    }

    @Test
    fun `a release before the signal shows everything and the late signal does nothing`() {
        val gate = FanOutHideGate()
        val waiting = gate.onMembers(fanA)

        val released = gate.onMembers(emptyList())

        assertEquals(emptyList<FanMember>(), released.hide)
        assertFalse(released.awaiting)
        assertNull(gate.onCopiesDrawn(waiting.generation))
    }

    @Test
    fun `a release after the originals were hidden shows them again`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)

        val released = gate.onMembers(emptyList())

        assertEquals(emptyList<FanMember>(), released.hide)
        assertFalse(released.awaiting)
    }

    @Test
    fun `another stack tapped while a fan is open shows the first fan's originals at once and hides the new ones on the signal`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)

        val next = gate.onMembers(fanB)

        assertEquals("the first fan's originals come back as its copies go", emptyList<FanMember>(), next.hide)
        assertTrue(next.awaiting)
        assertEquals(fanB, gate.onCopiesDrawn(next.generation)!!.hide)
    }

    @Test
    fun `a marker that stays in the next fan stays hidden and does not blink`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)
        val overlapping = listOf(a1, b1)

        val next = gate.onMembers(overlapping)

        assertEquals("a1 is in both fans, so its original stays hidden", listOf(a1), next.hide)
        assertTrue(next.awaiting)
        assertEquals(overlapping, gate.onCopiesDrawn(next.generation)!!.hide)
    }

    @Test
    fun `at rest the fallback hides what the signal did not, and says so`() {
        val gate = FanOutHideGate()
        gate.onMembers(fanA)

        val forced = gate.onRest()

        assertNotNull(forced)
        assertEquals(fanA, forced!!.hide)
        assertTrue("the fallback must be reported as the fallback", forced.viaFallback)
    }

    @Test
    fun `at rest after the signal there is nothing for the fallback to do`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)

        assertNull(gate.onRest())
    }

    @Test
    fun `a fan re-drawn on a fresh style, whose filters hide nothing, waits for its copies again`() {
        val step = FanOutHideGate().onMembers(fanA)

        assertEquals(emptyList<FanMember>(), step.hide)
        assertTrue(step.awaiting)
    }

    // --- the restart cases (amendment -371, the planner's review of a5185a2f): the fan-draw effect restarts when the focused
    // observation changes while a fan is up, and on a style reload; the gate must not forget what it knows.

    @Test
    fun `asking again for the members it is already waiting for keeps the same wait`() {
        val gate = FanOutHideGate()
        val first = gate.onMembers(fanA)

        val again = gate.onMembers(fanA)

        assertEquals("the wait is the same wait", first.generation, again.generation)
        assertTrue(again.awaiting)
        assertEquals(emptyList<FanMember>(), again.hide)
        assertNotNull("the signal for the first ask still counts", gate.onCopiesDrawn(first.generation))
    }

    @Test
    fun `asking again for members it has already hidden keeps them hidden with no wait`() {
        val gate = FanOutHideGate()
        val waiting = gate.onMembers(fanA)
        gate.onCopiesDrawn(waiting.generation)

        val again = gate.onMembers(fanA)

        assertEquals("a restart must not bring the originals back", fanA, again.hide)
        assertFalse("and must not start a wait", again.awaiting)
        assertNull("nothing is left waiting for a signal", gate.onCopiesDrawn(again.generation))
    }

    @Test
    fun `a fan that is already spread when the gate first sees it is hidden at once`() {
        val step = FanOutHideGate().onMembers(fanA, spread = true)

        assertEquals("copies are away from their originals, so there is nothing to wait for", fanA, step.hide)
        assertFalse(step.awaiting)
        assertNull(FanOutHideGate().also { it.onMembers(fanA, spread = true) }.onRest())
    }

    @Test
    fun `a fan seen at progress zero still waits`() {
        val step = FanOutHideGate().onMembers(fanA, spread = false)

        assertEquals(emptyList<FanMember>(), step.hide)
        assertTrue(step.awaiting)
    }

    @Test
    fun `a restart during a fold, which asks again and then folds, leaves nothing waiting`() {
        val gate = FanOutHideGate()
        gate.onMembers(fanA)
        gate.onFold()

        val restarted = gate.onMembers(fanA, spread = true)
        gate.onFold()

        assertFalse(restarted.awaiting)
        assertNull(gate.onRest())
    }

    // --- the fold's end, the mirror of the open: the copies must not be cleared before the originals they stand on are drawn again.

    @Test
    fun `a release names the originals it shows again`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)

        val released = gate.onMembers(emptyList())

        assertEquals("the copies on these must wait for them", fanA, released.reveal)
        assertEquals(emptyList<FanMember>(), released.hide)
    }

    @Test
    fun `a release before anything was hidden has nothing to wait for`() {
        val gate = FanOutHideGate()
        gate.onMembers(fanA)

        assertEquals(emptyList<FanMember>(), gate.onMembers(emptyList()).reveal)
    }

    @Test
    fun `a release of a fan whose originals only partly stayed hidden names just those`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)
        gate.onMembers(listOf(a1, b1))

        assertEquals(listOf(a1), gate.onMembers(emptyList()).reveal)
    }

    @Test
    fun `releasing an already released gate reveals nothing`() {
        val gate = FanOutHideGate()
        gate.onCopiesDrawn(gate.onMembers(fanA).generation)
        gate.onMembers(emptyList())

        assertEquals(emptyList<FanMember>(), gate.onMembers(emptyList()).reveal)
    }
}
