package com.zynergylabs.forager.app.domain

/**
 * A record whose delete the user has asked for and which has not been deleted yet (journal redesign
 * J4, D1). Stub: behaviour lands in the D1 implementation commit.
 */
data class PendingDelete<T>(
    val item: T,
    val entryReferenceCount: Int?,
    val token: Long,
)

class PendingDeleteSlot<K, T>(private val keyOf: (T) -> K) {
    var pending: PendingDelete<T>? = null
        private set

    fun pend(item: T, entryReferenceCount: Int?): T? = null

    fun undo(key: K): T? = null

    fun commit(key: K): T? = null

    fun takeAny(): T? = null
}

fun <T, K> List<T>.withoutPending(pending: PendingDelete<T>?, keyOf: (T) -> K): List<T> = this
