package com.zynergylabs.forager.app.diagnostics.walklog

import java.io.File

/** STUB for the failing-test commit (dispatch 2026-09-28-532). */
class WalkLogWriter(
    val file: File,
    private val freeBytes: () -> Long,
    private val nowNanos: () -> Long,
    private val minFreeBytes: Long = MIN_FREE_BYTES,
) {
    val isOpen: Boolean get() = false
    val stoppedReason: String? get() = null

    fun open(): Boolean = false
    fun write(lines: List<String>): Boolean = false
    fun checkSpace(): Boolean = false
    fun close(endLine: String) = Unit

    companion object {
        /** RECORD -559, choice 4: stop below 200 MB free. */
        const val MIN_FREE_BYTES = 200L * 1024 * 1024
    }
}
