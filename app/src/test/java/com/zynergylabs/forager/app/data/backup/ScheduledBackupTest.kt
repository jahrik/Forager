package com.zynergylabs.forager.app.data.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.zynergylabs.forager.app.data.repository.DataStoreBackupSchedulePreferences
import com.zynergylabs.forager.app.domain.BackupFrequency
import com.zynergylabs.forager.app.domain.BackupScheduleSettings
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.RunScheduledBackupUseCase
import com.zynergylabs.forager.app.domain.backupFileName
import com.zynergylabs.forager.app.ui.backup.FakeBackupFiles
import com.zynergylabs.forager.app.ui.backup.FakeJournalBackup
import com.zynergylabs.forager.app.ui.backup.FakeSchedulePreferences
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The automatic backup, below the screen: the preference file, one scheduled run, the WorkManager job and the
 * worker that WorkManager calls. Off by default; one new file per run; nothing is ever deleted.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ScheduledBackupTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val zone = ZoneId.of("UTC")
    private val noon = 1_790_000_000_000L // a fixed instant, so the file name is a known date
    private val clock = CurrentTimeProvider { noon }
    private val files = FakeBackupFiles()
    private val logged = mutableListOf<String>()
    private val backup = FakeJournalBackup()
    private val on = BackupScheduleSettings(enabled = true, frequency = BackupFrequency.WEEKLY, folderUri = "content://tree/backups")

    private fun useCase(prefs: BackupScheduleSettings) =
        RunScheduledBackupUseCase(backup, FakeSchedulePreferences(prefs), files, clock, zone, ErrorLog { _, m, e -> logged += "$m :: ${e.message}" })

    // ---- the settings file ---------------------------------------------------------------------

    @Test
    fun `the saved schedule is off, weekly and without a folder until the user sets it`() = runBlocking {
        val prefs = DataStoreBackupSchedulePreferences(context)

        assertEquals(BackupScheduleSettings(enabled = false, frequency = BackupFrequency.WEEKLY, folderUri = null), prefs.get().getOrThrow())
    }

    @Test
    fun `a saved schedule reads back exactly, for every frequency`() = runBlocking {
        val prefs = DataStoreBackupSchedulePreferences(context)
        for (f in BackupFrequency.entries) {
            val settings = BackupScheduleSettings(enabled = true, frequency = f, folderUri = "content://tree/a b")
            prefs.save(settings).getOrThrow()
            assertEquals(settings, prefs.get().getOrThrow())
        }
    }

    // ---- one scheduled run ---------------------------------------------------------------------

    @Test
    fun `the default file name is forager-backup, the date, dot zip`() {
        assertEquals("forager-backup-2026-09-21.zip", backupFileName(noon, zone))
    }

    @Test
    fun `a scheduled run writes one new file to the chosen folder, named by the date`() = runBlocking {
        val result = useCase(on)()

        assertTrue(result.isSuccess)
        assertEquals(listOf("content://tree/backups" to backupFileName(noon, zone)), files.created)
        assertTrue(FakeJournalBackup.BACKUP_BYTES.contentEquals(files.written.values.single().toByteArray()))
    }

    @Test
    fun `two runs make two files and nothing is removed`() = runBlocking {
        useCase(on)()
        useCase(on)()

        assertEquals(2, files.created.size)
        assertEquals("both files are still there", 2, files.written.size)
        assertTrue("and nothing was deleted", files.deleted.isEmpty())
    }

    @Test
    fun `a run with the setting off, or with no folder, writes nothing and reports a failure`() = runBlocking {
        val off = useCase(on.copy(enabled = false))()
        val noFolder = useCase(on.copy(folderUri = null))()

        assertTrue("off: ${off.exceptionOrNull()}", off.exceptionOrNull().let { it is com.zynergylabs.forager.app.domain.BackupException && "off" in it.message!! })
        assertTrue("no folder: ${noFolder.exceptionOrNull()}", noFolder.exceptionOrNull().let { it is com.zynergylabs.forager.app.domain.BackupException && "folder" in it.message!! })
        assertTrue("never a silent success", files.created.isEmpty())
    }

    @Test
    fun `a folder that cannot be written is a failure the caller can see, not a success`() = runBlocking {
        files.failFolder = true

        val result = useCase(on)()

        assertTrue("names the folder: ${result.exceptionOrNull()}", result.exceptionOrNull().let { it is com.zynergylabs.forager.app.domain.BackupException && "folder" in it.message!! })
        assertEquals("nothing was reported as written", 0, backup.backUps)
    }

    @Test
    fun `a scheduled run that meets unreadable photos skips them and saves the backup, reporting how many`() = runBlocking {
        backup.unreadablePhotos = 2

        val result = useCase(on)()

        assertEquals(2, result.getOrThrow().photoFilesMissing)
        assertEquals("it never asks: there is no screen", listOf(com.zynergylabs.forager.app.domain.UnreadablePhotoPolicy.SKIP), backup.policies)
        assertTrue(files.deleted.isEmpty())
        assertEquals(1, files.created.size)
    }

    @Test
    fun `a scheduled run whose write fails deletes the file it created, and only that file`() = runBlocking {
        useCase(on)() // an earlier, good backup already in the folder
        val earlier = files.written.keys.single()
        backup.failWrite = true

        val result = useCase(on)()

        assertTrue(result.isFailure)
        assertEquals("only the file this run created", listOf(files.created.last().let { "${it.first}/${it.second}#2" }), files.deleted)
        assertTrue("the earlier backup is untouched", earlier in files.written.keys)
    }

    @Test
    fun `a delete that fails is logged at warn and the run is still reported as a failure`() = runBlocking {
        backup.failWrite = true
        files.failDelete = true

        val result = useCase(on)()

        assertTrue(result.isFailure)
        assertTrue("logged: $logged", logged.any { "delete" in it })
    }

    // ---- the worker ----------------------------------------------------------------------------

    private fun worker(prefs: BackupScheduleSettings, log: MutableList<String>): ListenableWorker {
        val deps = object : ScheduledBackupDependencies {
            override val runScheduledBackup = useCase(prefs)
            override val errorLog = ErrorLog { _, message, error -> log += "$message :: ${error.message}" }
        }
        return TestListenableWorkerBuilder<ScheduledBackupWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: androidx.work.WorkerParameters): ListenableWorker =
                    ScheduledBackupWorker(appContext, workerParameters, deps)
            })
            .build()
    }

    @Test
    fun `the worker runs the backup and reports success`() = runBlocking {
        val result = (worker(on, mutableListOf()) as androidx.work.CoroutineWorker).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, files.created.size)
    }

    @Test
    fun `the worker reports a failed run as a failure and logs why`() = runBlocking {
        files.failFolder = true
        val log = mutableListOf<String>()

        val result = (worker(on, log) as androidx.work.CoroutineWorker).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        assertTrue("logged: $log", log.any { "folder unreadable" in it })
    }

    // ---- the WorkManager job -------------------------------------------------------------------

    private fun workManager(): WorkManager {
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        return WorkManager.getInstance(context)
    }

    private fun infos(wm: WorkManager) = wm.getWorkInfosForUniqueWork(WorkManagerBackupScheduler.UNIQUE_WORK_NAME).get()

    @Test
    fun `enabling schedules one periodic job at the chosen frequency, and nothing is scheduled while it is off`() {
        val wm = workManager()
        val scheduler = WorkManagerBackupScheduler(wm)
        assertTrue("nothing scheduled by default", infos(wm).isEmpty())

        scheduler.apply(on.copy(frequency = BackupFrequency.DAILY))
        val daily = infos(wm).single()
        assertEquals(WorkInfo.State.ENQUEUED, daily.state)
        assertEquals(TimeUnit.DAYS.toMillis(1), daily.periodicityInfo!!.repeatIntervalMillis)

        scheduler.apply(on.copy(frequency = BackupFrequency.MONTHLY))
        val monthly = infos(wm).filter { it.state == WorkInfo.State.ENQUEUED }
        assertEquals("still one job, with the new period", 1, monthly.size)
        assertEquals(TimeUnit.DAYS.toMillis(30), monthly.single().periodicityInfo!!.repeatIntervalMillis)

        scheduler.apply(on.copy(frequency = BackupFrequency.WEEKLY))
        assertEquals(TimeUnit.DAYS.toMillis(7), infos(wm).single { it.state == WorkInfo.State.ENQUEUED }.periodicityInfo!!.repeatIntervalMillis)

        scheduler.apply(on.copy(enabled = false))
        assertTrue("turning it off cancels the job", infos(wm).none { it.state == WorkInfo.State.ENQUEUED })
    }

    @Test
    fun `an enabled setting with no folder is never scheduled`() {
        val wm = workManager()

        WorkManagerBackupScheduler(wm).apply(on.copy(folderUri = null))

        assertNull(infos(wm).firstOrNull { it.state == WorkInfo.State.ENQUEUED })
        assertFalse(infos(wm).any { it.state == WorkInfo.State.ENQUEUED })
    }
}
