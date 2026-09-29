package com.zynergylabs.forager.app.ui.map.fanout

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** STUB (tests-first commit): the state holds nothing and the host never animates. */
class MarkerFanOutState {
    var members by mutableStateOf<List<FanMember>>(emptyList())
    var progress by mutableFloatStateOf(0f)
    var wantOpen by mutableStateOf(false)
    var generation by mutableIntStateOf(0)

    val isOpen: Boolean get() = false

    fun open(members: List<FanMember>) {}

    fun fold() {}

    fun release() {}
}

@Composable
fun MarkerFanOutHost(state: MarkerFanOutState) {}

@Composable
fun MarkerFanOutBackHandler(state: MarkerFanOutState) {}
