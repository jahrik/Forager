package com.zynergylabs.forager.app.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.zynergylabs.forager.app.ui.theme.Spacing

/**
 * A Records row that deletes by swipe — journal redesign J4 (plan J8; owner ruling "Swipe + Undo,
 * delayed delete (Recommended)"). It replaces the always-visible trash icon (waypoints) and the text
 * "Delete" button (offline regions). [onDelete] asks for a *pending* delete: the owning ViewModel
 * hides the record and the Undo snackbar shows; nothing is deleted here.
 *
 * - **End to start only** ([SwipeToDismissBox] with `enableDismissFromStartToEnd = false`):
 *   right-to-left in a left-to-right layout, mirrored in right-to-left by the component itself. A
 *   swipe the other way springs back and does nothing.
 * - **Behind the row**, only while it is being swiped that way: the error container colour and a
 *   trash icon at the end. The row's content gets the surface colour behind it, so a row with no
 *   background of its own (the region row) does not show the red through its text.
 * - **Accessibility.** With the icon and button gone, the swipe is the only visible delete, so the row
 *   carries a semantics custom action, "Delete", that makes the same request (the planner's call in
 *   `prompts/preserved/2026-09-27-21.md`, "not optional"). TalkBack lists it in the row's actions.
 *
 * Callers compose one of these per record inside `key(record id)`: the swipe state is remembered per
 * row, and without the key a row taking a removed row's slot would inherit its swiped-away state.
 * [testTag] names the row for tests and is on the same node as the custom action.
 */
@Composable
internal fun SwipeToDeleteRow(
    testTag: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = Spacing.lg),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        },
        modifier = modifier
            .testTag(testTag)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(DELETE_ACTION_LABEL) {
                        currentOnDelete()
                        true
                    },
                )
            },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        onDismiss = { value -> if (value == SwipeToDismissBoxValue.EndToStart) currentOnDelete() },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { content() }
    }
}

/** The custom accessibility action's label on every swipeable Records row. */
internal const val DELETE_ACTION_LABEL = "Delete"

/** The test tag of a swipeable Records row — the same in its single-type chip and in the All logbook, which never show at once. */
internal fun swipeToDeleteTag(type: RecordType, recordId: String): String = "records-swipe-${type.tagName()}-$recordId"
