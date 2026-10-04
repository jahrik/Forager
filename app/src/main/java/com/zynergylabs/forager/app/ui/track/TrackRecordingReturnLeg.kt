package com.zynergylabs.forager.app.ui.track

import com.zynergylabs.forager.app.domain.ReturnLeg

/**
 * The return a waypoint navigation pauses and picks back up (dispatch 2026-09-28-502, step 6), as
 * `MainActivity` hands it to `AvailabilityViewModel`. Pausing is the return's own [TrackRecordingViewModel.stopReturn]
 * and resuming its own [TrackRecordingViewModel.startReturn], so the off-track rule and the walked-path line
 * are untouched: while the return is off neither runs (step 7), and the resumed return draws its line
 * afresh (the planner's ruling). Resuming with no recording any more does nothing, as Return does.
 */
class TrackRecordingReturnLeg(private val viewModel: TrackRecordingViewModel) : ReturnLeg {
    override val isReturning: Boolean get() = viewModel.uiState.value.isReturning
    override fun pause() = viewModel.stopReturn()
    override fun resume() = viewModel.startReturn()
}
