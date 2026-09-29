package com.zynergylabs.forager.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * The one token: the bottom navigation bar's container colour, `MaterialTheme.colorScheme.surfaceContainer`
 * (dark [SurfaceContainerDark] `#202020`, light [SurfaceContainerLight] `#F4EFE2`).
 *
 * C1 (dispatch 2026-09-28-210). The owner, verbatim: "The map chrome isn't aligned. The search panel and map icon bar
 * are the wrong color. Have them be the same color as the bottom app navigation bar. Make sure any other pop up or
 * bubble, or the tool panel, is the same color as the app navigation bar also please." The bar and the rail read this
 * function for their default container, and every piece of map chrome takes its colour from it (directly, or
 * through `MapIconStackButtonColorDark`/`Light` and the cluster's two layers, which are built from the same two theme
 * values), so "the same colour" holds by construction and a change to the bar's colour carries to all of them.
 *
 * **Colour only.** Every alpha stays with its surface: the standing 0.8 over a map (`MAP_CHROME_OVER_MAP_ALPHA`), the
 * cluster's 0.6 container and 0.5 layers, and the 0.96 of a menu stacked on the search panel. The owner: "Opacity for
 * the icon bar is fine as is." Content colours are unchanged except where they would fail WCAG AA on this colour
 * (`ChromeContrastTest`); the search notice keeps its error colour because it signals a problem.
 */
@Composable
@ReadOnlyComposable
fun navigationBarContainerColor(): Color = MaterialTheme.colorScheme.surfaceContainer
