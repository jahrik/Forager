package com.zynergylabs.forager.app.ui.map

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription

/** The centre pin's content description: how tests find the drawn pin. */
internal const val CENTRE_PIN_DESCRIPTION = "Pin marks the location that will be picked"

/**
 * The ARGB pixel of the on-screen centre pin at ([xUnits], [yUnits]) on Material `LocationOn`'s
 * 24-unit grid. Needs Robolectric's native graphics (`@GraphicsMode(NATIVE)`), which is what makes
 * `captureToImage` return what was drawn. The pin's node is its 40dp icon box, so the grid maps onto
 * the captured image's own width and height.
 *
 * `LocationOn` on that grid: head circle centre (12, 9) radius 7, so the head's top edge is y 2; the
 * hole is radius 2.5 at (12, 9); the tip is (12, 22).
 */
internal fun ComposeTestRule.centrePinPixel(xUnits: Float, yUnits: Float): Int {
    val image = onNodeWithContentDescription(CENTRE_PIN_DESCRIPTION).captureToImage()
    val x = (image.width * xUnits / 24f).toInt().coerceIn(0, image.width - 1)
    val y = (image.height * yUnits / 24f).toInt().coerceIn(0, image.height - 1)
    return image.toPixelMap()[x, y].toArgb()
}

/** A point inside the pin's head, above its hole: always the pin's fill. */
internal fun ComposeTestRule.centrePinFillPixel(): Int = centrePinPixel(12f, 4.5f)
