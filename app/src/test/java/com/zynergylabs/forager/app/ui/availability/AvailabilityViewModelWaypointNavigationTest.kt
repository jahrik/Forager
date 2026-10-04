package com.zynergylabs.forager.app.ui.availability

import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.WaypointNavigation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Dispatch 2026-09-28-502 (plan tasks T8 and T9) with Amendment 1, through the real
 * [AvailabilityViewModel] over fakes: "Navigate" targets the waypoint with or without a recording;
 * a return under way is paused and picked back up when the waypoint navigation ends (step 6); and the
 * navigation is kept across the app being closed ("Pick navigation back up"), cleared when it ends.
 */
class AvailabilityViewModelWaypointNavigationTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val logged = mutableListOf<String>()
    private val store = InMemoryWaypointNavigation()
    private val leg = FakeReturnLeg()

    private fun viewModel() = mapLayersViewModel(
        errorLog = ErrorLog { _, message, _ -> logged += message },
        waypointNavigationRepository = store,
        returnLeg = leg,
    )

    @Test
    fun `Navigate with no return under way targets that waypoint, keeps it, and pauses nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()

        assertEquals(WaypointNavigation("wp-1", resumesReturn = false), vm.uiState.value.waypointNavigation)
        assertEquals(listOf<WaypointNavigation?>(WaypointNavigation("wp-1", resumesReturn = false)), store.writes)
        assertEquals(0, leg.pauses)
    }

    @Test
    fun `Navigate during a return pauses the return, and ending the waypoint navigation picks it back up`() = runTest(dispatcher) {
        val returning = FakeReturnLeg(returning = true)
        val vm = mapLayersViewModel(waypointNavigationRepository = store, returnLeg = returning)
        advanceUntilIdle()

        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()

        assertEquals("the waypoint overrules the return while active", 1, returning.pauses)
        assertEquals(false, returning.isReturning)
        assertEquals(WaypointNavigation("wp-1", resumesReturn = true), vm.uiState.value.waypointNavigation)
        assertEquals("kept with the return to pick up", WaypointNavigation("wp-1", resumesReturn = true), store.stored)

        vm.onStopWaypointNavigation()
        advanceUntilIdle()

        assertEquals("the return picks up again", 1, returning.resumes)
        assertEquals(true, returning.isReturning)
        assertNull(vm.uiState.value.waypointNavigation)
    }

    @Test
    fun `ending clears what is kept, and ending with nothing on resumes nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()

        vm.onStopWaypointNavigation()
        advanceUntilIdle()

        assertNull(vm.uiState.value.waypointNavigation)
        assertNull("nothing left to pick back up", store.stored)
        assertEquals(listOf(WaypointNavigation("wp-1", false), null), store.writes)
        assertEquals("no return was paused, so none is resumed", 0, leg.resumes)

        vm.onStopWaypointNavigation()
        advanceUntilIdle()
        assertEquals(0, leg.resumes)
        assertEquals("a second end writes nothing", 2, store.writes.size)
    }

    @Test
    fun `choosing another waypoint while navigating moves the target and keeps the paused return to pick up`() = runTest(dispatcher) {
        val returning = FakeReturnLeg(returning = true)
        val vm = mapLayersViewModel(waypointNavigationRepository = store, returnLeg = returning)
        advanceUntilIdle()
        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()

        vm.onNavigateToWaypoint("wp-2")
        advanceUntilIdle()

        assertEquals(WaypointNavigation("wp-2", resumesReturn = true), vm.uiState.value.waypointNavigation)
        assertEquals("paused once, not again", 1, returning.pauses)
        vm.onStopWaypointNavigation()
        advanceUntilIdle()
        assertEquals(1, returning.resumes)
    }

    @Test
    fun `the app opened again picks the kept navigation back up, return and all`() = runTest(dispatcher) {
        store.stored = WaypointNavigation("wp-1", resumesReturn = true)

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(WaypointNavigation("wp-1", resumesReturn = true), vm.uiState.value.waypointNavigation)
        vm.onStopWaypointNavigation()
        advanceUntilIdle()
        assertEquals("ending it picks the paused return up", 1, leg.resumes)
    }

    @Test
    fun `a kept navigation read after the walker has already chosen one does not replace it`() = runTest(dispatcher) {
        store.stored = WaypointNavigation("old", resumesReturn = false)
        store.readGate = CompletableDeferred()
        val vm = viewModel()
        advanceUntilIdle()

        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()
        store.readGate!!.complete(Unit)
        advanceUntilIdle()

        assertEquals(WaypointNavigation("wp-1", resumesReturn = false), vm.uiState.value.waypointNavigation)
    }

    @Test
    fun `a failed read is logged and nothing is navigated to`() = runTest(dispatcher) {
        store.stored = WaypointNavigation("wp-1", resumesReturn = false)
        store.failReads = true

        val vm = viewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.waypointNavigation)
        assertTrue("logged: $logged", logged.any { it.contains("waypoint navigation", ignoreCase = true) })
    }

    @Test
    fun `a failed write is logged and the navigation still runs`() = runTest(dispatcher) {
        store.failWrites = true
        val vm = viewModel()
        advanceUntilIdle()

        vm.onNavigateToWaypoint("wp-1")
        advanceUntilIdle()

        assertEquals(WaypointNavigation("wp-1", resumesReturn = false), vm.uiState.value.waypointNavigation)
        assertTrue("logged: $logged", logged.any { it.contains("waypoint navigation", ignoreCase = true) })
    }
}
