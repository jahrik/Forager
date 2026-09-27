package com.zynergylabs.forager.app.ui.log

import androidx.compose.runtime.saveable.Saver

/**
 * Journal redesign J5c, tests-first stub: the details sheet's target type and its saver, so the
 * tests compile. The saver saves nothing and restores nothing; the build commit replaces this file.
 */
internal sealed interface RecordDetailsTarget {
    data class WaypointDetails(val id: String) : RecordDetailsTarget
    data class TrackDetails(val id: String) : RecordDetailsTarget
    data class OfflineRegionDetails(val id: Long) : RecordDetailsTarget
}

internal val RecordDetailsTargetSaver: Saver<RecordDetailsTarget?, String> = Saver(save = { "" }, restore = { null })
