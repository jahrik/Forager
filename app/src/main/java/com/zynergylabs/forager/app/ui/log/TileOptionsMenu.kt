package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics

/**
 * What a grid tile needs to open its long-press menu (journal redesign J4b; owner ruling "Lists
 * swipe, grids long-press (Recommended)": find tiles and album photos): the long-click's label, the
 * call that opens the menu, and the menu's items as custom accessibility actions. Built by
 * [LongPressOptionsBox] and applied by the tile with [tileClickable].
 */
internal class TileOptions(
    val longClickLabel: String,
    val onLongClick: () -> Unit,
    val actions: List<CustomAccessibilityAction>,
)

/**
 * The tile's own click, and with [options] its long-click and custom actions on the **same** node,
 * so TalkBack, which reads the focused (clickable) node's actions, reaches Edit and Delete without
 * the gesture (the dispatch's "Accessibility (not optional)"). The tap still does what it did.
 */
@OptIn(ExperimentalFoundationApi::class)
internal fun Modifier.tileClickable(onClick: () -> Unit, options: TileOptions, onClickLabel: String? = null): Modifier =
    combinedClickable(
        onClickLabel = onClickLabel,
        onLongClickLabel = options.longClickLabel,
        onLongClick = options.onLongClick,
        onClick = onClick,
    ).semantics { customActions = options.actions }

/**
 * A grid tile with a long-press menu anchored at it (journal redesign J4b, L1 find tiles and L3
 * album photos): [content] draws the tile, applying [tileClickable] with the [TileOptions] it is
 * given; a long-press opens a Material 3 `DropdownMenu` of **Edit** (only when [onEdit] is set: a
 * record with no edit path gets no Edit) and **Delete**. Delete calls [onDelete], which asks for a
 * *pending* delete (J4's delayed delete and Undo snackbar); Edit calls [onEdit]. The same two are
 * the tile's custom accessibility actions. The stable `DropdownMenu`, as the album's Add photo menu
 * uses (J2); the Expressive menus are Understory step 5's.
 */
@Composable
internal fun LongPressOptionsBox(
    longClickLabel: String,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (TileOptions) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentOnEdit by rememberUpdatedState(onEdit)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val hasEdit = onEdit != null
    val options = remember(longClickLabel, hasEdit) {
        TileOptions(
            longClickLabel = longClickLabel,
            onLongClick = { expanded = true },
            actions = listOfNotNull(
                if (hasEdit) {
                    CustomAccessibilityAction(EDIT_ACTION_LABEL) {
                        currentOnEdit?.invoke()
                        true
                    }
                } else {
                    null
                },
                CustomAccessibilityAction(DELETE_ACTION_LABEL) {
                    currentOnDelete()
                    true
                },
            ),
        )
    }
    Box(modifier = modifier) {
        content(options)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (hasEdit) {
                DropdownMenuItem(
                    text = { Text(EDIT_ACTION_LABEL) },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    onClick = {
                        expanded = false
                        currentOnEdit?.invoke()
                    },
                    modifier = Modifier.testTag(TILE_OPTIONS_EDIT_TAG),
                )
            }
            DropdownMenuItem(
                text = { Text(DELETE_ACTION_LABEL) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = {
                    expanded = false
                    currentOnDelete()
                },
                modifier = Modifier.testTag(TILE_OPTIONS_DELETE_TAG),
            )
        }
    }
}

internal const val TILE_OPTIONS_EDIT_TAG = "tile-options-edit"
internal const val TILE_OPTIONS_DELETE_TAG = "tile-options-delete"
