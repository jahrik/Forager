package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.ReturnLeg
import com.zynergylabs.forager.app.domain.WaypointNavigation
import com.zynergylabs.forager.app.domain.WaypointNavigationRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * Dispatch 2026-09-28-502: the waypoint navigation's store, in memory. [stored] is what the app would
 * read when it opens; every write is kept in [writes]. A read waits on [readGate] when one is set, so a
 * test can let the user choose before the stored value lands. [failReads] and [failWrites] refuse.
 */
internal class InMemoryWaypointNavigation(var stored: WaypointNavigation? = null) : WaypointNavigationRepository {
    val writes = mutableListOf<WaypointNavigation?>()
    var readGate: CompletableDeferred<Unit>? = null
    var failReads = false
    var failWrites = false

    override suspend fun getCurrent(): Result<WaypointNavigation?> {
        readGate?.await()
        return if (failReads) Result.failure(IllegalStateException("read refused")) else Result.success(stored)
    }

    override suspend fun setCurrent(navigation: WaypointNavigation?): Result<Unit> {
        if (failWrites) return Result.failure(IllegalStateException("write refused"))
        writes += navigation
        stored = navigation
        return Result.success(Unit)
    }
}

/**
 * Dispatch 2026-09-28-502, step 6: a return a test can start, with every pause and resume counted.
 * Pausing ends it and resuming starts it, as the return's own Stop and Return do. [onChange] lets a
 * screen test hand the change to the screen.
 */
internal class FakeReturnLeg(returning: Boolean = false, private val onChange: (Boolean) -> Unit = {}) : ReturnLeg {
    override var isReturning: Boolean = returning
        private set
    var pauses = 0
    var resumes = 0

    override fun pause() {
        pauses++
        isReturning = false
        onChange(false)
    }

    override fun resume() {
        resumes++
        isReturning = true
        onChange(true)
    }
}
