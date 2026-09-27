package com.zynergylabs.forager.app.domain

/**
 * A record whose delete the user has asked for and which has not been deleted yet — journal
 * redesign J4 (owner ruling "Swipe + Undo, delayed delete (Recommended)", plan J8). The real delete
 * runs only when the Undo snackbar ends for any reason other than Undo, so Undo is exact: nothing was
 * deleted, so nothing has to be put back. J0 found every delete in this app is a hard SQL `DELETE`
 * and a region's tiles cannot be restored at all short of a re-download
 * (`docs/audits/2026-09-27-journal-j0-pulse.md`, B1), which is why the delete is deferred rather than
 * undone.
 *
 * [entryReferenceCount] is how many Cartography entries keep a reference to [item], read when the
 * delete was asked for, for the snackbar's warning (owner ruling "In the Undo snackbar
 * (Recommended)"); `null` for a record type that has no such count (finds), which the snackbar then
 * says nothing about rather than claiming zero.
 *
 * [token] tells two pends of the same record apart (delete, Undo, delete again): each gets a new one,
 * so a snackbar keyed on it shows again.
 */
data class PendingDelete<T>(
    val item: T,
    val entryReferenceCount: Int?,
    val token: Long,
)

/**
 * One pending delete per record type, held by the ViewModel that owns that type's delete (the
 * planner's call in `prompts/preserved/2026-09-27-21.md`). Pure: it decides *which* record is to be
 * deleted and when, and never deletes anything itself; the owner runs the returned record's real
 * delete. Not thread-safe: its owners call it from the main thread only.
 *
 * The commit rules, each one call:
 * - [pend]: a new pending record. A record already pending is **displaced and returned**, and the
 *   owner commits it now: two quick deletes commit the first when the second is asked for.
 * - [undo]: the snackbar's Undo. Returns the record so the caller can confirm it was still pending;
 *   nothing is to be deleted.
 * - [commit]: the snackbar ended any other way (it timed out, or a newer one replaced it). Returns
 *   the record to delete, once: a second commit of the same record returns `null`.
 * - [takeAny]: the owner is being cleared; whatever is pending is committed then.
 *
 * [undo] and [commit] act only on the record named: a key that is not the pending one (already
 * committed by a later [pend], or never pended) changes nothing and returns `null`.
 */
class PendingDeleteSlot<K, T>(private val keyOf: (T) -> K) {
    private var lastToken = 0L

    var pending: PendingDelete<T>? = null
        private set

    /** Holds [item] as pending; returns the record it displaced, which the caller must commit, or `null`. */
    fun pend(item: T, entryReferenceCount: Int?): T? {
        val displaced = pending?.item
        pending = PendingDelete(item, entryReferenceCount, ++lastToken)
        // The same record asked for twice is still one pending delete, not a commit of itself.
        return displaced?.takeUnless { keyOf(it) == keyOf(item) }
    }

    /** The snackbar's Undo: clears [key] if it is the pending record and returns it; `null` if it is not. */
    fun undo(key: K): T? = clearIfPending(key)

    /** The snackbar ended without Undo: clears [key] if it is the pending record and returns it to be deleted. */
    fun commit(key: K): T? = clearIfPending(key)

    /** The owner is being cleared: returns whatever is pending, to be deleted, and empties the slot. */
    fun takeAny(): T? {
        val item = pending?.item
        pending = null
        return item
    }

    private fun clearIfPending(key: K): T? {
        val current = pending ?: return null
        if (keyOf(current.item) != key) return null
        pending = null
        return current.item
    }
}

/**
 * This list without [pending]'s record: what a screen shows while a delete is pending (J4, "a pending
 * record is hidden from every Journal list, count and the All logbook at once"). Returns the list
 * itself when nothing is pending.
 */
fun <T, K> List<T>.withoutPending(pending: PendingDelete<T>?, keyOf: (T) -> K): List<T> {
    if (pending == null) return this
    val hidden = keyOf(pending.item)
    return filterNot { keyOf(it) == hidden }
}
