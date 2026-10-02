package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.ReturnToStartInfo
import com.zynergylabs.forager.app.domain.model.TrackPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** What [ReturnWatch] tells a screen. [trackId] is the recording it is for, or `null` when it is for none. */
data class ReturnWatchState(
    val trackId: String? = null,
    val isReturning: Boolean = false,
    val isOffTrack: Boolean = false,
    val returnToStart: ReturnToStartInfo? = null,
)

/**
 * SKELETON (dispatch 2026-09-28-400, Amendment 2, tests first): the shape of the class and nothing
 * else. It decides nothing yet, so `ReturnWatchTest` fails on its assertions and not on a missing
 * symbol. The class's own documentation lands with its behaviour.
 */
class ReturnWatch(
    @Suppress("unused") private val computeReturnToStart: ComputeReturnToStartUseCase,
    @Suppress("unused") private val detectOffTrack: DetectOffTrackUseCase,
    @Suppress("unused") private val alertDelivery: AlertDelivery,
    @Suppress("unused") private val currentTime: CurrentTimeProvider = SystemCurrentTimeProvider,
) {
    private val _state = MutableStateFlow(ReturnWatchState())
    val state: StateFlow<ReturnWatchState> = _state.asStateFlow()

    internal val keptReadingCount: Int get() = 0

    fun begin(trackId: String) = Unit

    fun end(trackId: String?) = Unit

    fun startReturn(trackId: String): Boolean = false

    fun stopReturn(trackId: String) = Unit

    fun setStartPoint(trackId: String, start: TrackPoint?) = Unit

    fun onFix(current: TrackPoint) = Unit
}
