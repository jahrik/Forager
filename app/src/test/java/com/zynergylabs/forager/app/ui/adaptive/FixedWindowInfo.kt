package com.zynergylabs.forager.app.ui.adaptive

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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

/**
 * Provides [configuration] and the window it describes together, for a test that simulates a rotation or a resize by
 * changing the configuration's screenWidthDp/screenHeightDp. Where [configuration] is the one already in place, the real
 * window is kept as it is.
 */
@Composable
internal fun ProvideConfigurationAndWindow(configuration: Configuration, content: @Composable () -> Unit) {
    val window = if (configuration === LocalConfiguration.current) {
        LocalWindowInfo.current
    } else {
        FixedWindowInfo(
            LocalWindowInfo.current,
            DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp),
            LocalDensity.current.density,
        )
    }
    CompositionLocalProvider(LocalConfiguration provides configuration, LocalWindowInfo provides window, content = content)
}
