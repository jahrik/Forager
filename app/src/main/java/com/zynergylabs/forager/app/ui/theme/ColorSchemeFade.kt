package com.zynergylabs.forager.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.zynergylabs.forager.app.ui.motion.LocalReduceMotion
import com.zynergylabs.forager.app.ui.motion.MotionTokens

/*
 * Motion Part 3, item 4 (dispatch 2026-09-28-676; the owner, RECORD -651: Night mode "Fade the colours", against the planner's
 * recommendation to keep it instant, and RECORD -652: "the night mode switch is already suggestive by virtue of a night mode being
 * active alone. The results aren't subtle, it's an entire UI shift, so the fade is permissible if it's fast and smooth, and not
 * ceremonial and boring").
 *
 * **How.** Every colour of the theme blends from the light scheme to the dark one, or back, over one quick spring
 * ([MotionTokens.nightModeFadeSpec], the motion scheme's fast effects spec, critically damped). Between the two ends the app is
 * drawn with a scheme in which every role is that same fraction of the way across ([lerpColorScheme]). At either end it is
 * [LightColors] or [DarkColors] itself, so outside a change nothing differs from before.
 *
 * **What it costs, and why that is the S22's to judge.** Material 3 provides the colour scheme through a static composition local
 * (read from `ColorSchemeKt`'s bytecode in material3 1.5.0-alpha26: `staticCompositionLocalOf`), and its `ColorScheme` is
 * immutable, so every frame of the blend recomposes everything under the theme: the whole app, for the few frames the spring
 * lasts. Whether that stays smooth on the phone is the device check; under reduced motion there is no blend at all.
 *
 * **What does not fade.** Anything that picks its colour from whether night mode is on rather than from the scheme
 * ([LocalForagerDarkTheme]: parts of the map's chrome) changes at the start, and so do the system bars' icons (MainActivity
 * sets them at once), and the map itself, which reloads its night or day basemap as before (scout G2). Not changed here.
 */

/**
 * The colour scheme to draw with while [darkTheme] may just have changed: blending from the other scheme to this one on
 * [MotionTokens.nightModeFadeSpec], or this one at once under reduced motion ([LocalReduceMotion]). The first composition is at the
 * end already, so the app opens in its colours with no blend.
 */
@Composable
internal fun fadingColorScheme(darkTheme: Boolean): ColorScheme {
    val reduceMotion = LocalReduceMotion.current
    val towardsDark by animateFloatAsState(
        targetValue = if (darkTheme) 1f else 0f,
        animationSpec = MotionTokens.nightModeFadeSpec(ForagerMotionScheme),
        label = "nightModeFade",
    )
    return when {
        reduceMotion -> if (darkTheme) DarkColors else LightColors
        towardsDark <= 0f -> LightColors
        towardsDark >= 1f -> DarkColors
        else -> lerpColorScheme(LightColors, DarkColors, towardsDark)
    }
}

/**
 * [start] carried [fraction] of the way to [stop], every role alike (0 is [start], 1 is [stop]). Every role of material3 1.5.0-alpha26's
 * [ColorScheme] is listed (its 48 colour fields, read from its bytecode), so no role is left at one end while the rest move.
 */
internal fun lerpColorScheme(start: ColorScheme, stop: ColorScheme, fraction: Float): ColorScheme {
    fun c(from: Color, to: Color): Color = lerp(from, to, fraction)
    return start.copy(
        primary = c(start.primary, stop.primary),
        onPrimary = c(start.onPrimary, stop.onPrimary),
        primaryContainer = c(start.primaryContainer, stop.primaryContainer),
        onPrimaryContainer = c(start.onPrimaryContainer, stop.onPrimaryContainer),
        inversePrimary = c(start.inversePrimary, stop.inversePrimary),
        secondary = c(start.secondary, stop.secondary),
        onSecondary = c(start.onSecondary, stop.onSecondary),
        secondaryContainer = c(start.secondaryContainer, stop.secondaryContainer),
        onSecondaryContainer = c(start.onSecondaryContainer, stop.onSecondaryContainer),
        tertiary = c(start.tertiary, stop.tertiary),
        onTertiary = c(start.onTertiary, stop.onTertiary),
        tertiaryContainer = c(start.tertiaryContainer, stop.tertiaryContainer),
        onTertiaryContainer = c(start.onTertiaryContainer, stop.onTertiaryContainer),
        background = c(start.background, stop.background),
        onBackground = c(start.onBackground, stop.onBackground),
        surface = c(start.surface, stop.surface),
        onSurface = c(start.onSurface, stop.onSurface),
        surfaceVariant = c(start.surfaceVariant, stop.surfaceVariant),
        onSurfaceVariant = c(start.onSurfaceVariant, stop.onSurfaceVariant),
        surfaceTint = c(start.surfaceTint, stop.surfaceTint),
        inverseSurface = c(start.inverseSurface, stop.inverseSurface),
        inverseOnSurface = c(start.inverseOnSurface, stop.inverseOnSurface),
        error = c(start.error, stop.error),
        onError = c(start.onError, stop.onError),
        errorContainer = c(start.errorContainer, stop.errorContainer),
        onErrorContainer = c(start.onErrorContainer, stop.onErrorContainer),
        outline = c(start.outline, stop.outline),
        outlineVariant = c(start.outlineVariant, stop.outlineVariant),
        scrim = c(start.scrim, stop.scrim),
        surfaceBright = c(start.surfaceBright, stop.surfaceBright),
        surfaceDim = c(start.surfaceDim, stop.surfaceDim),
        surfaceContainer = c(start.surfaceContainer, stop.surfaceContainer),
        surfaceContainerHigh = c(start.surfaceContainerHigh, stop.surfaceContainerHigh),
        surfaceContainerHighest = c(start.surfaceContainerHighest, stop.surfaceContainerHighest),
        surfaceContainerLow = c(start.surfaceContainerLow, stop.surfaceContainerLow),
        surfaceContainerLowest = c(start.surfaceContainerLowest, stop.surfaceContainerLowest),
        primaryFixed = c(start.primaryFixed, stop.primaryFixed),
        primaryFixedDim = c(start.primaryFixedDim, stop.primaryFixedDim),
        onPrimaryFixed = c(start.onPrimaryFixed, stop.onPrimaryFixed),
        onPrimaryFixedVariant = c(start.onPrimaryFixedVariant, stop.onPrimaryFixedVariant),
        secondaryFixed = c(start.secondaryFixed, stop.secondaryFixed),
        secondaryFixedDim = c(start.secondaryFixedDim, stop.secondaryFixedDim),
        onSecondaryFixed = c(start.onSecondaryFixed, stop.onSecondaryFixed),
        onSecondaryFixedVariant = c(start.onSecondaryFixedVariant, stop.onSecondaryFixedVariant),
        tertiaryFixed = c(start.tertiaryFixed, stop.tertiaryFixed),
        tertiaryFixedDim = c(start.tertiaryFixedDim, stop.tertiaryFixedDim),
        onTertiaryFixed = c(start.onTertiaryFixed, stop.onTertiaryFixed),
        onTertiaryFixedVariant = c(start.onTertiaryFixedVariant, stop.onTertiaryFixedVariant),
    )
}
