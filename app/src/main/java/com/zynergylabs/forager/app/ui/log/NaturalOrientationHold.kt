package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** STUB for the failing-tests-first commit (RECORD -985); the hold is built in the next commit. */
internal data class EdgeInsets(val left: Dp, val top: Dp, val right: Dp, val bottom: Dp)

internal fun naturalHoldQuarterTurns(displayRotation: Int, fullScreen: Boolean): Int = 0

internal fun windowEdgesInNaturalFrame(window: EdgeInsets, quarterTurns: Int): EdgeInsets = window

internal fun naturalFrameIsLandscape(windowIsLandscape: Boolean, quarterTurns: Int): Boolean = windowIsLandscape

internal val LocalNaturalOrientationHold = staticCompositionLocalOf { false }

internal val LocalCameraBandInsets = compositionLocalOf<EdgeInsets?> { null }

@Composable
internal fun HoldNaturalOrientation(
    windowIsLandscape: Boolean,
    displayRotation: Int,
    fullScreen: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(windowIsLandscape: Boolean, displayRotation: Int) -> Unit,
) {
    Box(modifier.fillMaxSize()) { content(windowIsLandscape, displayRotation) }
}

@Composable
internal fun currentWindowIsFullScreen(): Boolean = true
