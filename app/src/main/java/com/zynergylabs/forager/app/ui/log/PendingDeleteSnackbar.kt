package com.zynergylabs.forager.app.ui.log

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
