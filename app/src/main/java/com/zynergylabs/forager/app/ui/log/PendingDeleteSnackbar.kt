package com.zynergylabs.forager.app.ui.log

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.PendingDelete
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
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

/**
 * One pending delete, as the Undo snackbar needs it (J4): which record type (one snackbar slot per
 * type), the pend's token (a new pend of the same type shows a new snackbar), the message, and what
 * Undo and "ended any other way" call on the owning ViewModel. Built by [waypointDeleteNotice] and its
 * siblings from the ViewModel's pending state, in `MainActivity`, and shown by
 * [PendingDeleteSnackbarEffects].
 *
 * Public only because `AvailabilityScreen`, which hosts the snackbar, is public; everything in it is
 * internal.
 */
class PendingDeleteNotice internal constructor(
    internal val type: RecordType,
    internal val token: Long,
    internal val message: String,
    internal val onUndo: () -> Unit,
    internal val onCommit: () -> Unit,
)

/**
 * The snackbar text (owner ruling "In the Undo snackbar (Recommended)"): "[noun] deleted", and while
 * Undo is possible the reference warning the old confirm dialogs carried, "· used in N journal
 * entries" (singular for one). No warning for no references, or for a type with no count (`null`),
 * rather than a claim of zero.
 */
internal fun pendingDeleteMessage(noun: String, entryReferenceCount: Int?): String = when {
    entryReferenceCount == null || entryReferenceCount <= 0 -> "$noun deleted"
    entryReferenceCount == 1 -> "$noun deleted · used in 1 journal entry"
    else -> "$noun deleted · used in $entryReferenceCount journal entries"
}

/** The waypoint snackbar for [pending], or `null` when no waypoint delete is pending. */
internal fun waypointDeleteNotice(
    pending: PendingDelete<Waypoint>?,
    onUndo: (String) -> Unit,
    onCommit: (String) -> Unit,
): PendingDeleteNotice? = pending?.let { p ->
    val id = p.item.id
    PendingDeleteNotice(
        type = RecordType.WAYPOINTS,
        token = p.token,
        message = pendingDeleteMessage("Waypoint", p.entryReferenceCount),
        onUndo = { onUndo(id) },
        onCommit = { onCommit(id) },
    )
}

/** The offline-region snackbar for [pending] ("Offline map deleted", the chip's own noun), or `null` when none is pending. */
internal fun offlineRegionDeleteNotice(
    pending: PendingDelete<OfflineRegionSummary>?,
    onUndo: (Long) -> Unit,
    onCommit: (Long) -> Unit,
): PendingDeleteNotice? = pending?.let { p ->
    val id = p.item.id
    PendingDeleteNotice(
        type = RecordType.OFFLINE_MAPS,
        token = p.token,
        message = pendingDeleteMessage("Offline map", p.entryReferenceCount),
        onUndo = { onUndo(id) },
        onCommit = { onCommit(id) },
    )
}

/** J4 D4 stub. */
internal fun findDeleteNotice(
    pending: PendingDelete<MushroomLogEntry>?,
    onUndo: (String) -> Unit,
    onCommit: (String) -> Unit,
): PendingDeleteNotice? = null

/**
 * Shows each pending delete's Undo snackbar in [hostState] and reports how it ended (J4):
 * - **Undo** ([SnackbarResult.ActionPerformed]) calls [PendingDeleteNotice.onUndo];
 * - **anything else** ([SnackbarResult.Dismissed]: the timeout, or a newer delete snackbar dismissing
 *   this one) calls [PendingDeleteNotice.onCommit], the planner's rule that a pending record is
 *   committed when its snackbar ends for any reason other than Undo.
 *
 * **Replacement.** A new delete snackbar dismisses a delete snackbar already showing, so two quick
 * deletes commit the first when the second shows. Within one type the owning ViewModel has already
 * committed the first by then (its new pend displaced it) and this effect's key change cancels the
 * old snackbar. Across types the old one's effect sees [SnackbarResult.Dismissed] and commits.
 * Any other snackbar sharing the host ("Saved to Drafts", the trip-start warning) is not dismissed:
 * the delete snackbar waits its turn behind it, as `SnackbarHostState` queues, and the record stays
 * pending that much longer.
 *
 * [SnackbarDuration.Long] (about 10 s; Material 3 lengthens it further when an accessibility service
 * asks for longer timeouts), with the action "Undo" as the snackbar's own button, which TalkBack
 * reaches like any button. An effect cancelled without an ending (the host leaving composition on an
 * Activity recreation) reports nothing: the record stays pending in its ViewModel and the snackbar
 * shows again when this recomposes.
 */
@Composable
internal fun PendingDeleteSnackbarEffects(notices: List<PendingDeleteNotice>, hostState: SnackbarHostState) {
    notices.forEach { notice ->
        key(notice.type) {
            LaunchedEffect(notice.token) {
                hostState.currentSnackbarData?.takeIf { it.visuals is PendingDeleteSnackbarVisuals }?.dismiss()
                when (hostState.showSnackbar(PendingDeleteSnackbarVisuals(notice.message))) {
                    SnackbarResult.ActionPerformed -> notice.onUndo()
                    SnackbarResult.Dismissed -> notice.onCommit()
                }
            }
        }
    }
}

/** A delete snackbar's visuals; its own class so a newer delete snackbar can tell it from the host's other snackbars. */
private class PendingDeleteSnackbarVisuals(override val message: String) : SnackbarVisuals {
    override val actionLabel: String = UNDO_LABEL
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Long
}

internal const val UNDO_LABEL = "Undo"
