package com.zynergylabs.forager.app.ui.backup

import com.zynergylabs.forager.app.domain.BackupFrequency
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.RestoreMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Backup section's rules, on the ViewModel the screen drives (the screen's own wiring is in
 * `BackupSettingsScreenTest`). Every message asserted is compared to the approved copy as a literal string, so
 * a reworded message fails here and is not silently accepted.
 */
class BackupViewModelTest {

    private val backup = FakeJournalBackup()
    private val prefs = FakeSchedulePreferences()
    private val scheduler = FakeScheduler()
    private val files = FakeBackupFiles()
    private val logged = mutableListOf<String>()
    private var restoredCallbacks = 0

    private fun viewModel() = BackupViewModel(
        backup = backup,
        preferences = prefs,
        scheduler = scheduler,
        files = files,
        errorLog = ErrorLog { _, message, error -> logged += "$message :: ${error.message}" },
        ioDispatcher = Dispatchers.Unconfined,
        afterRestore = { restoredCallbacks++ },
    )

    private fun BackupViewModel.state() = uiState.value

    @Test
    fun `the automatic backup is off by default, with no folder, and the messages are the approved words`() {
        val vm = viewModel()
        assertFalse(vm.state().schedule.enabled)
        assertNull(vm.state().schedule.folderUri)
        assertEquals("Backup saved.", BackupMessage.BACKUP_SAVED.text)
        assertEquals("Couldn't save the backup.", BackupMessage.BACKUP_FAILED.text)
        assertEquals("Automatic backup is off until you choose a folder.", BackupMessage.AUTOMATIC_NEEDS_FOLDER.text)
        assertEquals("Restore complete.", BackupMessage.RESTORE_COMPLETE.text)
        assertEquals("Couldn't restore that backup.", BackupMessage.RESTORE_FAILED.text)
    }

    @Test
    fun `turning the automatic backup on with no folder leaves it off and says so`() {
        val vm = viewModel()

        vm.controls(vm.state()).onAutomaticChanged(true)

        assertFalse(vm.state().schedule.enabled)
        assertEquals(BackupMessage.AUTOMATIC_NEEDS_FOLDER, vm.state().message)
        assertFalse("nothing was saved as on", prefs.stored.enabled)
        assertTrue("and nothing was scheduled", scheduler.applied.none { it.enabled })
    }

    @Test
    fun `with a folder chosen the automatic backup turns on, is saved, and is scheduled at its frequency`() {
        val vm = viewModel()
        vm.controls(vm.state()).onFolderChosen("content://tree/backups")

        vm.controls(vm.state()).onAutomaticChanged(true)

        assertEquals(listOf("content://tree/backups"), files.keptFolders)
        assertTrue(vm.state().schedule.enabled)
        assertNull(vm.state().message)
        assertEquals(BackupScheduleSettings(enabled = true, frequency = BackupFrequency.WEEKLY, folderUri = "content://tree/backups"), prefs.stored)
        assertEquals(prefs.stored, scheduler.applied.last())
    }

    @Test
    fun `changing how often re-schedules an enabled backup, and turning it off cancels the schedule`() {
        val vm = viewModel()
        vm.controls(vm.state()).onFolderChosen("content://tree/backups")
        vm.controls(vm.state()).onAutomaticChanged(true)

        vm.controls(vm.state()).onFrequencyChanged(BackupFrequency.MONTHLY)
        assertEquals(BackupFrequency.MONTHLY, scheduler.applied.last().frequency)
        assertTrue(scheduler.applied.last().enabled)

        vm.controls(vm.state()).onAutomaticChanged(false)
        assertFalse(scheduler.applied.last().enabled)
        assertFalse(prefs.stored.enabled)
        assertEquals("the choices are kept for next time", BackupFrequency.MONTHLY, prefs.stored.frequency)
    }

    @Test
    fun `a saved schedule is read back when the screen starts`() {
        prefs.stored = BackupScheduleSettings(enabled = true, frequency = BackupFrequency.DAILY, folderUri = "content://tree/backups")

        val vm = viewModel()

        assertEquals(prefs.stored, vm.state().schedule)
    }

    @Test
    fun `an unreadable saved schedule is logged, and the section starts from the off default rather than guessing`() {
        prefs.failGet = true

        val vm = viewModel()

        assertFalse(vm.state().schedule.enabled)
        assertTrue("the failure was logged: $logged", logged.any { "unreadable" in it })
    }

    @Test
    fun `Back up now writes the backup to the chosen file and says Backup saved`() {
        val vm = viewModel()

        vm.controls(vm.state()).onBackUpNow("content://docs/forager-backup.zip")

        assertEquals(1, backup.backUps)
        assertTrue(FakeJournalBackup.BACKUP_BYTES.contentEquals(files.written.getValue("content://docs/forager-backup.zip").toByteArray()))
        assertEquals(BackupMessage.BACKUP_SAVED, vm.state().message)
        assertFalse(vm.state().busy)
    }

    @Test
    fun `a backup that fails says Couldn't save the backup, logs why, and is not shown as saved`() {
        backup.failBackUp = true
        val vm = viewModel()

        vm.controls(vm.state()).onBackUpNow("content://docs/x.zip")

        assertEquals(BackupMessage.BACKUP_FAILED, vm.state().message)
        assertTrue(logged.any { "backup failed" in it })
    }

    @Test
    fun `a file that cannot be opened is a failed backup, not a crash and not a success`() {
        files.failOpen = true
        val vm = viewModel()

        vm.controls(vm.state()).onBackUpNow("content://docs/x.zip")

        assertEquals(BackupMessage.BACKUP_FAILED, vm.state().message)
        assertEquals("the backup was never attempted", 0, backup.backUps)
    }

    @Test
    fun `choosing a file to restore asks first and restores nothing until Replace or Merge is chosen`() {
        files.contents["content://docs/b.zip"] = "PK-bytes".toByteArray()
        val vm = viewModel()

        vm.controls(vm.state()).onRestoreFileChosen("content://docs/b.zip")

        assertEquals("content://docs/b.zip", vm.state().pendingRestoreUri)
        assertTrue(backup.restored.isEmpty())
    }

    @Test
    fun `Replace and Merge each restore the chosen file in their mode and say Restore complete`() {
        for (mode in RestoreMode.entries) {
            backup.restored.clear()
            files.contents["content://docs/b.zip"] = "PK-$mode".toByteArray()
            val vm = viewModel()
            vm.controls(vm.state()).onRestoreFileChosen("content://docs/b.zip")

            vm.controls(vm.state()).onRestoreConfirmed(mode)

            assertEquals(1, backup.restored.size)
            assertEquals(mode, backup.restored.single().first)
            assertEquals("PK-$mode", String(backup.restored.single().second))
            assertEquals(BackupMessage.RESTORE_COMPLETE, vm.state().message)
            assertNull("the prompt is closed", vm.state().pendingRestoreUri)
        }
    }

    @Test
    fun `Cancel closes the prompt and restores nothing`() {
        files.contents["content://docs/b.zip"] = "PK".toByteArray()
        val vm = viewModel()
        vm.controls(vm.state()).onRestoreFileChosen("content://docs/b.zip")
        assertEquals("the prompt was up", "content://docs/b.zip", vm.state().pendingRestoreUri)

        vm.controls(vm.state()).onRestoreCancelled()

        assertNull(vm.state().pendingRestoreUri)
        assertTrue(backup.restored.isEmpty())
        assertNull(vm.state().message)
    }

    @Test
    fun `a restore that fails says Couldn't restore that backup and logs why`() {
        backup.failRestore = true
        files.contents["content://docs/b.zip"] = "PK".toByteArray()
        val vm = viewModel()
        vm.controls(vm.state()).onRestoreFileChosen("content://docs/b.zip")

        vm.controls(vm.state()).onRestoreConfirmed(RestoreMode.MERGE)

        assertEquals(BackupMessage.RESTORE_FAILED, vm.state().message)
        assertTrue(logged.any { "restore failed" in it })
    }

    @Test
    fun `a chosen restore file that cannot be read is a failed restore before any prompt result`() {
        files.contents["content://docs/b.zip"] = "PK".toByteArray()
        val vm = viewModel()
        vm.controls(vm.state()).onRestoreFileChosen("content://docs/b.zip")
        files.failOpen = true

        vm.controls(vm.state()).onRestoreConfirmed(RestoreMode.REPLACE)

        assertEquals(BackupMessage.RESTORE_FAILED, vm.state().message)
        assertTrue(backup.restored.isEmpty())
    }

    @Test
    fun `a successful restore tells the app so its screens can reload, and a failed or cancelled one does not`() {
        files.contents["content://docs/b.zip"] = "PK".toByteArray()
        val ok = viewModel()
        ok.controls(ok.state()).onRestoreFileChosen("content://docs/b.zip")
        ok.controls(ok.state()).onRestoreConfirmed(RestoreMode.REPLACE)
        assertEquals("after a restore that worked", 1, restoredCallbacks)

        backup.failRestore = true
        val bad = viewModel()
        bad.controls(bad.state()).onRestoreFileChosen("content://docs/b.zip")
        bad.controls(bad.state()).onRestoreConfirmed(RestoreMode.REPLACE)
        val cancelled = viewModel()
        cancelled.controls(cancelled.state()).onRestoreFileChosen("content://docs/b.zip")
        cancelled.controls(cancelled.state()).onRestoreCancelled()
        assertEquals("not after one that failed or was cancelled", 1, restoredCallbacks)
    }
}
