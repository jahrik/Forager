package com.zynergylabs.forager.app.ui.track

import com.zynergylabs.forager.app.domain.ReturnLeg

/** The return a waypoint navigation pauses and picks back up (dispatch 2026-09-28-502, step 6). */
class TrackRecordingReturnLeg(private val viewModel: TrackRecordingViewModel) : ReturnLeg {
    override val isReturning: Boolean get() = false
    override fun pause() = Unit
    override fun resume() = Unit
}
