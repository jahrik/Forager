package com.zynergylabs.forager.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Journal redesign J4, D1: the pending-delete holder on its own, headless. The commit rules the
 * dispatch names (pend, undo, commit on timeout, commit on replacement, commit on clear, hidden while
 * pending) are each one call here; the ViewModel tests run the same rules through each owning
 * ViewModel against a fake repository.
 */
class PendingDeleteSlotTest {

    private data class Rec(val id: String, val name: String)

    private val a = Rec("a", "Creek pin")
    private val b = Rec("b", "Big oak")
    private val c = Rec("c", "Trailhead")

    private fun slot() = PendingDeleteSlot<String, Rec> { it.id }

    @Test
    fun `pend holds the record with its reference count and displaces nothing`() {
        val slot = slot()
        val displaced = slot.pend(a, entryReferenceCount = 2)

        assertNull(displaced)
        assertEquals(a, slot.pending?.item)
        assertEquals(2, slot.pending?.entryReferenceCount)
    }

    @Test
    fun `undo returns the pending record and empties the slot, so nothing is left to commit`() {
        val slot = slot()
        slot.pend(a, entryReferenceCount = null)

        assertEquals(a, slot.undo("a"))
        assertNull(slot.pending)
        assertNull("after an undo there is nothing to commit", slot.commit("a"))
    }

    @Test
    fun `commit (the snackbar timing out) returns the record once, and a second commit returns nothing`() {
        val slot = slot()
        slot.pend(a, entryReferenceCount = 0)

        assertEquals(a, slot.commit("a"))
        assertNull(slot.pending)
        assertNull(slot.commit("a"))
    }

    @Test
    fun `pending a second record displaces the first and hands it back to be committed`() {
        val slot = slot()
        slot.pend(a, entryReferenceCount = 0)

        val displaced = slot.pend(b, entryReferenceCount = 1)

        assertEquals(a, displaced)
        assertEquals(b, slot.pending?.item)
        assertNull("the displaced record is no longer undoable here", slot.undo("a"))
        assertEquals(b, slot.pending?.item)
    }

    @Test
    fun `pending the same record again after an undo gets a new token`() {
        val slot = slot()
        slot.pend(a, entryReferenceCount = 0)
        val first = slot.pending!!.token
        slot.undo("a")

        slot.pend(a, entryReferenceCount = 0)

        assertNotEquals(first, slot.pending!!.token)
    }

    @Test
    fun `undo or commit of a record that is not the pending one changes nothing`() {
        val slot = slot()
        slot.pend(b, entryReferenceCount = 0)

        assertNull(slot.undo("a"))
        assertNull(slot.commit("c"))
        assertEquals(b, slot.pending?.item)
    }

    @Test
    fun `takeAny (the owner being cleared) returns the pending record and empties the slot`() {
        val slot = slot()
        slot.pend(c, entryReferenceCount = null)

        assertEquals(c, slot.takeAny())
        assertNull(slot.pending)
        assertNull(slot.takeAny())
    }

    @Test
    fun `withoutPending hides exactly the pending record, and nothing when none is pending`() {
        val list = listOf(a, b, c)
        val slot = slot()
        assertSame(list, list.withoutPending(slot.pending) { it.id })

        slot.pend(b, entryReferenceCount = 0)

        assertEquals(listOf(a, c), list.withoutPending(slot.pending) { it.id })
    }
}
