package com.zynergylabs.forager.app.ui.log

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.zynergylabs.forager.app.domain.PendingDelete
import com.zynergylabs.forager.app.domain.model.Waypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Where a pending delete (journal redesign J4) is committed when its owning ViewModel is cleared.
 * `viewModelScope` is already cancelled by the time `onCleared` runs, so a delete launched there
 * would never start; this scope lives as long as the process. Main, because the deletes it runs are
 * the same calls the ViewModels make from `viewModelScope` (Main) today, MapLibre's region delete
 * among them. [SupervisorJob] so one failed commit does not cancel the next.
 *
 * If the process dies before this runs, the record is simply not deleted: the safe direction.
 */
internal val PendingDeleteCommitScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

/** J4 D2 stub. */
internal data class PendingDeleteNotice(
    val type: RecordType,
    val token: Long,
    val message: String,
    val onUndo: () -> Unit,
    val onCommit: () -> Unit,
)

/** J4 D2 stub. */
internal fun waypointDeleteNotice(
    pending: PendingDelete<Waypoint>?,
    onUndo: (String) -> Unit,
    onCommit: (String) -> Unit,
): PendingDeleteNotice? = null

/** J4 D2 stub. */
@Composable
internal fun PendingDeleteSnackbarEffects(notices: List<PendingDeleteNotice>, hostState: SnackbarHostState) = Unit

/** J4 D2 stub: the tag of a swipe-to-delete row. */
internal fun swipeToDeleteTag(type: RecordType, recordId: String): String = "records-swipe-${type.name.lowercase()}-$recordId"
