package com.zynergylabs.forager.app.ui.log

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.data.local.ForagerDatabase
import com.zynergylabs.forager.app.data.repository.RoomCartographyEntryRepository
import com.zynergylabs.forager.app.data.repository.RoomMushroomLogRepository
import com.zynergylabs.forager.app.data.repository.RoomOfflineRegionDayIndex
import com.zynergylabs.forager.app.data.repository.RoomTrackRepository
import com.zynergylabs.forager.app.data.repository.RoomWaypointRepository
import com.zynergylabs.forager.app.domain.CartographyEntryRepository
import com.zynergylabs.forager.app.domain.CommitCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.ComputeTrackStatisticsUseCase
import com.zynergylabs.forager.app.domain.CreateCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.DeleteCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.GetCartographyDraftEntriesUseCase
import com.zynergylabs.forager.app.domain.GetCartographyEntriesUseCase
import com.zynergylabs.forager.app.domain.GetCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.GetDerivedTripUseCase
import com.zynergylabs.forager.app.domain.GetTripReportOfflineRegionsUseCase
import com.zynergylabs.forager.app.domain.OfflineMapRepository
import com.zynergylabs.forager.app.domain.OfflineRegionSummary
import com.zynergylabs.forager.app.domain.SaveCartographyEntryUseCase
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.Region
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Journal redesign J4b L2, headless: a Cartography entry deleted from its card is **pending** until
 * its Undo snackbar ends, held in [CartographyViewModel] by J4's [com.zynergylabs.forager.app.domain.PendingDeleteSlot]
 * the way J4's three owners hold theirs. Over a real in-memory Room database, as
 * [CartographyViewModelTest] is, with the repository's `delete` recorded so every delete assertion
 * reads the calls that really reached it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CartographyEntryPendingDeleteTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var database: ForagerDatabase
    private lateinit var repository: RecordingCartographyEntryRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val directExecutor = java.util.concurrent.Executor { it.run() }
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Application>(), ForagerDatabase::class.java)
            .setQueryExecutor(directExecutor)
            .setTransactionExecutor(directExecutor)
            .build()
        repository = RecordingCartographyEntryRepository(RoomCartographyEntryRepository(database.cartographyEntryDao()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    private fun viewModel(pendingDeleteCommitScope: CoroutineScope? = null) = CartographyViewModel(
        getEntries = GetCartographyEntriesUseCase(repository),
        getDraftEntries = GetCartographyDraftEntriesUseCase(repository),
        createEntry = CreateCartographyEntryUseCase(repository, now = { NOW }, idGenerator = { "entry-new" }),
        saveEntry = SaveCartographyEntryUseCase(repository, now = { NOW }),
        getEntry = GetCartographyEntryUseCase(repository),
        commitEntry = CommitCartographyEntryUseCase(repository, now = { NOW }),
        deleteEntry = DeleteCartographyEntryUseCase(repository),
        getDerivedTrip = GetDerivedTripUseCase(
            mushroomLogRepository = RoomMushroomLogRepository(database.mushroomLogDao()),
            trackRepository = RoomTrackRepository(database.trackDao()),
            waypointRepository = RoomWaypointRepository(database.waypointDao()),
            offlineRegionDayIndex = RoomOfflineRegionDayIndex(database.offlineRegionDao()),
        ),
        getTripReportOfflineRegions = GetTripReportOfflineRegionsUseCase(NoRegionsOfflineMapRepository),
        computeTrackStatistics = ComputeTrackStatisticsUseCase(),
        now = { NOW },
        pendingDeleteCommitScope = pendingDeleteCommitScope ?: PendingDeleteCommitScope,
    )

    private suspend fun seed() {
        repository.save(COMMITTED_A).getOrThrow()
        repository.save(COMMITTED_B).getOrThrow()
        repository.save(DRAFT).getOrThrow()
    }

    private fun TestScope.loaded(pendingDeleteCommitScope: CoroutineScope? = null): CartographyViewModel {
        val vm = viewModel(pendingDeleteCommitScope)
        advanceUntilIdle()
        assertEquals("positive control: both committed entries loaded", setOf(COMMITTED_A.id, COMMITTED_B.id), vm.uiState.value.entries.map { it.id }.toSet())
        return vm
    }

    @Test
    fun `a requested entry delete hides it from the visible entries and deletes nothing`() = runTest(dispatcher) {
        seed()
        val vm = loaded()

        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        assertEquals(COMMITTED_A.id, vm.uiState.value.pendingDelete?.item?.id)
        assertNull("entries carry no reference count", vm.uiState.value.pendingDelete?.entryReferenceCount)
        assertEquals(listOf(COMMITTED_B.id), vm.uiState.value.hidingPendingDelete().entries.map { it.id })
        assertEquals(emptyList<String>(), repository.deletedIds)
    }

    @Test
    fun `a requested draft delete hides it from the visible drafts and leaves the committed entries`() = runTest(dispatcher) {
        seed()
        val vm = loaded()
        assertEquals(listOf(DRAFT.id), vm.uiState.value.draftEntries.map { it.id })

        vm.requestDeleteEntry(DRAFT.id)
        advanceUntilIdle()

        val visible = vm.uiState.value.hidingPendingDelete()
        assertEquals(emptyList<String>(), visible.draftEntries.map { it.id })
        assertEquals(setOf(COMMITTED_A.id, COMMITTED_B.id), visible.entries.map { it.id }.toSet())
        assertEquals(true, vm.uiState.value.pendingDelete?.item?.isDraft)
        assertEquals(emptyList<String>(), repository.deletedIds)
    }

    @Test
    fun `undo of a pending entry delete shows it again and deletes nothing`() = runTest(dispatcher) {
        seed()
        val vm = loaded()
        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()
        assertEquals("positive control: hidden before the undo", listOf(COMMITTED_B.id), vm.uiState.value.hidingPendingDelete().entries.map { it.id })

        vm.undoDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        assertNull(vm.uiState.value.pendingDelete)
        assertEquals(setOf(COMMITTED_A.id, COMMITTED_B.id), vm.uiState.value.hidingPendingDelete().entries.map { it.id }.toSet())
        assertEquals(emptyList<String>(), repository.deletedIds)
    }

    @Test
    fun `committing a pending entry delete deletes exactly that entry, once`() = runTest(dispatcher) {
        seed()
        val vm = loaded()
        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        vm.commitDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()
        vm.commitDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        assertEquals(listOf(COMMITTED_A.id), repository.deletedIds)
        assertNull(vm.uiState.value.pendingDelete)
        assertEquals(listOf(COMMITTED_B.id), vm.uiState.value.entries.map { it.id })
        assertNull("gone from the database too", repository.getById(COMMITTED_A.id).getOrThrow())
    }

    @Test
    fun `a second entry delete commits the first when it replaces it`() = runTest(dispatcher) {
        seed()
        val vm = loaded()
        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        vm.requestDeleteEntry(DRAFT.id)
        advanceUntilIdle()

        assertEquals(listOf(COMMITTED_A.id), repository.deletedIds)
        assertEquals(DRAFT.id, vm.uiState.value.pendingDelete?.item?.id)
        assertEquals(listOf(COMMITTED_B.id), vm.uiState.value.hidingPendingDelete().entries.map { it.id })
        assertEquals(emptyList<String>(), vm.uiState.value.hidingPendingDelete().draftEntries.map { it.id })
    }

    @Test
    fun `an entry delete still pending when the ViewModel is cleared is committed`() = runTest(dispatcher) {
        seed()
        val testScope: CoroutineScope = this
        val store = ViewModelStore()
        val vm = ViewModelProvider(
            store,
            viewModelFactory { initializer { viewModel(pendingDeleteCommitScope = testScope) } },
        )[CartographyViewModel::class.java]
        advanceUntilIdle()
        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()
        assertEquals(emptyList<String>(), repository.deletedIds)

        store.clear()
        advanceUntilIdle()

        assertEquals(listOf(COMMITTED_A.id), repository.deletedIds)
    }

    @Test
    fun `a failed entry delete puts it back and says so`() = runTest(dispatcher) {
        seed()
        val vm = loaded()
        vm.requestDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()
        repository.deleteShouldFail = true

        vm.commitDeleteEntry(COMMITTED_A.id)
        advanceUntilIdle()

        assertEquals(listOf(COMMITTED_A.id), repository.deletedIds)
        assertEquals(setOf(COMMITTED_A.id, COMMITTED_B.id), vm.uiState.value.hidingPendingDelete().entries.map { it.id }.toSet())
        assertEquals("Couldn't delete that entry.", vm.uiState.value.saveErrorMessage)
    }

    @Test
    fun `a delete asked for an entry that is not loaded pends nothing`() = runTest(dispatcher) {
        seed()
        val vm = loaded()

        vm.requestDeleteEntry("entry-missing")
        advanceUntilIdle()

        assertNull(vm.uiState.value.pendingDelete)
        assertEquals(2, vm.uiState.value.hidingPendingDelete().entries.size)
    }
}

private const val NOW = 10_000L
private val DAY: LocalDate = LocalDate.of(2026, 8, 1)
private val COMMITTED_A = CartographyEntry.draft(id = "entry-a", date = DAY, updatedAtEpochMillis = NOW).copy(isDraft = false, text = "Ridge")
private val COMMITTED_B = CartographyEntry.draft(id = "entry-b", date = DAY.plusDays(1), updatedAtEpochMillis = NOW).copy(isDraft = false, text = "Creek")
private val DRAFT = CartographyEntry.draft(id = "entry-draft", date = DAY.plusDays(2), updatedAtEpochMillis = NOW)

/** The Room repository, with every [delete] call recorded in order and an optional forced failure. */
private class RecordingCartographyEntryRepository(private val inner: CartographyEntryRepository) : CartographyEntryRepository by inner {
    val deletedIds = mutableListOf<String>()
    var deleteShouldFail = false

    override suspend fun delete(id: String): Result<Unit> {
        deletedIds += id
        if (deleteShouldFail) return Result.failure(RuntimeException("delete failed"))
        return inner.delete(id)
    }
}

private object NoRegionsOfflineMapRepository : OfflineMapRepository {
    override suspend fun download(name: String, region: Region, onProgress: (downloaded: Int, total: Int) -> Unit): Result<OfflineRegionSummary> =
        error("not used by this test")
    override suspend fun deleteRegion(id: Long): Result<Unit> = error("not used by this test")
    override suspend fun listRegions(): Result<List<OfflineRegionSummary>> = Result.success(emptyList())
}
