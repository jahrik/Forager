package com.zynergylabs.forager.app.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

/**
 * Calls [onResized] with the new size each time this node's laid-out size changes after its first
 * measurement. STUB (F1 item 1, tests first): does nothing yet.
 */
@Composable
internal fun Modifier.onViewportResized(onResized: (IntSize) -> Unit): Modifier = this
