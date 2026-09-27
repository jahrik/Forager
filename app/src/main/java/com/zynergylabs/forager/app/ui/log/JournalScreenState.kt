package com.zynergylabs.forager.app.ui.log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * The Journal destination's user-set UI state: which top tab is showing and what Records is
 * filtered to. Journal redesign J1, S1 (`prompts/preserved/2026-09-27-16.md`; plan J10 and L7 in
 * `docs/plans/journal-redesign.md`; owner ruling 4 in `docs/audits/2026-09-27-journal-j0-pulse.md`:
 * "Hoisted, saveable").
 *
 * **Why it exists.** Until J1 every one of these choices was plain `remember` inside the Journal
 * branch of `when (compactTab)` (`JournalTab`'s own `selectedTopTab`, `RecordsTab`'s own
 * `selectedTab`), so leaving the Journal bottom-nav tab disposed them and coming back reset the user
 * to Cartography and Waypoints — a reset CLAUDE.md's UX defaults call a bug. This holder is created
 * with [rememberJournalScreenState] in `AvailabilityScreen`, above that branch, beside `compactTab`,
 * and passed down, so it outlives the branch.
 *
 * **Why saveable, not only hoisted.** A plain rotation does not recreate the Activity
 * (`AndroidManifest.xml`'s `configChanges`, J0 A3), but a night-mode toggle, a fold, a locale or
 * font-scale change and process death still do, and the owner's ruling covers those. [Saver] writes
 * enum *names*, not ordinals, so reordering an enum (J1 reorders [RecordsSubTab] to chip order) can
 * never silently restore a different value; an unknown name fails loudly in `valueOf` rather than
 * falling back to a default nobody logged (CLAUDE.md, errors).
 *
 * **What is not here, on purpose.** Transient state stays with the composable that owns it
 * (`JournalTab`'s `mode`, its two picker flags, the one-shot `recordsPendingSubTab` latch, the
 * external `PendingJournalDestination`, every dialog): those describe an interaction in progress,
 * not a place the user chose to be. `LogPanel` (the wide tree) keeps its own local state; bringing it
 * onto this holder is plan stage J6.
 *
 * **Room for J2/J3.** The Entries view mode (timeline/album) and the scroll positions belong here
 * too (plan J10). They are not built in J1; add each as a field below and as one more element of
 * [Saver]'s list, keeping names rather than ordinals.
 */
@Stable
internal class JournalScreenState(
    initialTopTab: JournalTopTab = JournalTopTab.CARTOGRAPHY,
    initialRecordsFilter: RecordsSubTab = DEFAULT_RECORDS_FILTER,
) {
    /** Which of the Journal's two top tabs is showing. */
    var topTab: JournalTopTab by mutableStateOf(initialTopTab)

    /**
     * What Records shows. A [MutableState] rather than only a delegated property because
     * `RecordsTab` takes it as a state object: `LogPanel`, out of scope until J6, calls `RecordsTab`
     * without one and gets `RecordsTab`'s own local default, with no change to `LogPanel.kt`.
     */
    val recordsFilterState: MutableState<RecordsSubTab> = mutableStateOf(initialRecordsFilter)
    var recordsFilter: RecordsSubTab by recordsFilterState

    // J2/J3: Entries view mode and scroll positions go here (see the class doc comment).

    companion object {
        val Saver: Saver<JournalScreenState, Any> = listSaver(
            save = { state -> listOf(state.topTab.name, state.recordsFilter.name) },
            restore = { saved ->
                JournalScreenState(
                    initialTopTab = JournalTopTab.valueOf(saved[0]),
                    initialRecordsFilter = RecordsSubTab.valueOf(saved[1]),
                )
            },
        )
    }
}

/** Creates the Journal's [JournalScreenState], saved and restored across Activity recreation — see that class. */
@Composable
internal fun rememberJournalScreenState(): JournalScreenState =
    rememberSaveable(saver = JournalScreenState.Saver) { JournalScreenState() }

/**
 * What Records shows when nothing has chosen otherwise — the holder's initial value and
 * `RecordsTab`'s own local default for a caller that passes no state (`LogPanel`).
 */
internal val DEFAULT_RECORDS_FILTER: RecordsSubTab = RecordsSubTab.ALL
