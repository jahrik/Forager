package com.zynergylabs.forager.app.ui.adaptive

import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * A window of a stated size, for tests (lint dispatch 2026-10-17-03, Phase 2: ConfigurationScreenWidthHeight).
 *
 * The app classifies its window by `LocalWindowInfo.current.containerDpSize`, the window's own size, as lint's
 * ConfigurationScreenWidthHeight check asks. A test that simulates a different window by providing a changed
 * `LocalConfiguration` (screenWidthDp/screenHeightDp) also provides one of these, so the window it describes and the
 * configuration it describes agree. Focus and keyboard state come from the real window this wraps.
 */
internal class FixedWindowInfo(
    private val real: WindowInfo,
    private val size: DpSize,
    private val density: Float,
) : WindowInfo {
    override val isWindowFocused: Boolean get() = real.isWindowFocused
    override val keyboardModifiers: PointerKeyboardModifiers get() = real.keyboardModifiers
    override val containerDpSize: DpSize get() = size
    override val containerSize: IntSize
        get() = IntSize((size.width.value * density).roundToInt(), (size.height.value * density).roundToInt())
}
