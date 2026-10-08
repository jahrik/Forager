package com.zynergylabs.forager.app.ui.log

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.zynergylabs.forager.app.ui.motion.BouncingIconButton

/**
 * The strip's gear chip (dispatch 2026-09-28-707; the owner: "Move the two camera options at the
 * bottom to a settings menu inside the camera itself", then "Gear chip with the others
 * (Recommended)"). A tap opens [CameraSettingsPanel], which holds "Automatically Save Location to
 * Photos" and "Lock camera to portrait", the two rows that were at the bottom of Settings.
 *
 * Built like [GridChip] and [LocationChip]: [BouncingIconButton] for the bounce, [OverlayIcon] for
 * the outline rule, [rotateWithDevice] to turn in place. Always present, last in the strip, after
 * the Location chip: every camera has these two settings. One glyph, no state of its own: the
 * panel is what shows the values.
 *
 * **Glyph:** `Icons.Filled.Settings` (material-icons-core), the gear the Tools drawer's own
 * Settings row already uses, so the two read as the same kind of thing.
 */
@Composable
internal fun CameraSettingsChip(onClick: () -> Unit, deviceRotation: Int?, displayRotation: Int) {
    BouncingIconButton(
        onClick = onClick,
        modifier = Modifier
            .rotateWithDevice(deviceRotation, displayRotation)
            .testTag(CAMERA_SETTINGS_CHIP_TAG)
            .semantics { contentDescription = CAMERA_SETTINGS_LABEL },
    ) {
        OverlayIcon(Icons.Filled.Settings, contentDescription = null)
    }
}

internal const val CAMERA_SETTINGS_CHIP_TAG = "in-app-camera-settings-chip"
internal const val CAMERA_SETTINGS_LABEL = "Camera settings"
