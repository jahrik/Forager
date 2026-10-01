package com.zynergylabs.forager.app.ui.log

import com.zynergylabs.forager.app.domain.PendingDelete
import com.zynergylabs.forager.app.domain.model.MushroomLogEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Journal redesign J4b L4 (owner: "Say 'Changes discarded' (Recommended)"): the find snackbar's
 * message by case, headless. A draft of a committed find (`draftOfEntryId` set) is what the edit
 * form of a re-edited find deletes, and the committed find stays, so its snackbar must not say the
 * find went. The UI half is in [JournalPendingDeleteTest].
 *
 * Under Robolectric only because the builders' file also holds [PendingDeleteCommitScope], whose
 * initialiser reads `Dispatchers.Main`, which a plain JVM test does not have (its first base run
 * failed on exactly that, `ExceptionInInitializerError`, not on the message). Robolectric's own
 * class loader keeps that scope from being bound to a test dispatcher shared with other classes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FindDeleteNoticeTest {

    private fun messageFor(entry: MushroomLogEntry): String =
        findDeleteNotice(PendingDelete(entry, entryReferenceCount = null, token = 1L), onUndo = {}, onCommit = {})!!.message

    @Test
    fun `a committed find says Find deleted`() {
        assertEquals("Find deleted", messageFor(COMMITTED))
    }

    @Test
    fun `a new find's own draft, with no committed original, says Find deleted`() {
        assertEquals("Find deleted", messageFor(NEW_DRAFT))
    }

    @Test
    fun `a re-edit's draft of a committed find says Changes discarded`() {
        assertEquals("Changes discarded", messageFor(REEDIT_DRAFT))
    }

    @Test
    fun `nothing pending is no notice`() {
        assertEquals(null, findDeleteNotice(null, onUndo = {}, onCommit = {}))
    }
}

private val NEW_DRAFT = MushroomLogEntry.draft(id = "draft-new", location = null, date = LocalDate.of(2026, 9, 20))
private val COMMITTED = NEW_DRAFT.copy(id = "find-a", isDraft = false)
private val REEDIT_DRAFT = NEW_DRAFT.copy(id = "draft-of-find", isDraft = true, draftOfEntryId = "find-a")
