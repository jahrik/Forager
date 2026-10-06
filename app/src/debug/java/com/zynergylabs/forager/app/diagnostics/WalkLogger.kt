package com.zynergylabs.forager.app.diagnostics

import android.content.Context
import java.io.File

/** STUB for the failing-test commit (dispatch 2026-09-28-532). */
class WalkLogger private constructor() {
    val isLogging: Boolean get() = false
    val currentFile: File? get() = null

    @Suppress("UNUSED_PARAMETER")
    fun onRecordingStarted(trackId: String) = Unit

    fun onRecordingStopped() = Unit

    companion object {
        const val DIRECTORY_NAME = "walklogs"
        const val WAKE_LOCK_TAG = "Forager:WalkLogger"

        /** Tests only: stands in for the free space where the log is written. */
        @Volatile internal var freeBytesOverride: ((File) -> Long)? = null

        @Suppress("UNUSED_PARAMETER")
        fun of(context: Context): WalkLogger = WalkLogger()

        /** STUB. */
        @Suppress("UNUSED_PARAMETER")
        fun fileName(startEpochMillis: Long): String = ""
    }
}
