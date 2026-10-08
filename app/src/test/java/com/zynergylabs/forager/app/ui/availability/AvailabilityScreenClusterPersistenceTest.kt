package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.data.repository.DataStoreMapPreferencesRepository
import com.zynergylabs.forager.app.domain.AbsentForecastCellStore
import com.zynergylabs.forager.app.domain.ErrorLog
import com.zynergylabs.forager.app.domain.MapIconClusterPlacement
import com.zynergylabs.forager.app.domain.MapIconClusterPlacementRepository
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MINIMIZE_HANDLE_TAG = "map-icon-bar-minimize-handle"
private const val RESTORE_HANDLE_DESCRIPTION = "Show map controls"
private const val ADD_ROW_DESCRIPTION = "Plan a trip or log a find here"

/**
 * RECORD -711 through the real [AvailabilityScreen], the real [AvailabilityViewModel] and the real
 * `map_preferences` file: the Maps tab's icon cluster keeps the side it was snapped to and the height it
 * was dragged to across a restart (a fresh repository and ViewModel over the same file), is not drawn at
 * all until the stored placement has been read (so never a frame on the default side), comes back not
 * minimised (the minimised flag is session-only), and a stored height beyond this window's limits opens
 * inside them. Every drag is a real long-press-and-drag on the cluster's handle, and every position is
 * read from the real layout (CLAUDE.md, Testing).
 *
 * The bar is located by its "Fullscreen" row, whose bounds are the bar's own width and move with it, as
 * `AvailabilityScreenMapIconStackTest` does. Each "launch" releases the previous repository first (a
 * second live DataStore on one file is refused), as `DataStoreMapLayerPreferencesTest` does.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w384dp-h823dp-xxhdpi")
class AvailabilityScreenClusterPersistenceTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private fun context() = ApplicationProvider.getApplicationContext<Application>()
    private fun dataStoreFile() = File(context().filesDir, "datastore/map_preferences.preferences_pb")

    private val scopes = mutableListOf<Job>()
    private val logged = mutableListOf<String>()
    private val errorLog = ErrorLog { _, message, _ -> logged += message }
    private val map = LayersRecordingMapSlot()

    private fun launchRepository(): Pair<DataStoreMapPreferencesRepository, Job> {
        val job = SupervisorJob()
        scopes += job
        return DataStoreMapPreferencesRepository(context(), CoroutineScope(Dispatchers.IO + job)) to job
    }

    private fun stored(repository: MapIconClusterPlacementRepository): MapIconClusterPlacement? =
        runBlocking { repository.getMapIconClusterPlacement().getOrNull() }

    @Before
    fun setUp() {
        dataStoreFile().delete()
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        dataStoreFile().delete()
    }

    private fun bar(): DpRect = composeRule.onNodeWithContentDescription("Fullscreen").getUnclippedBoundsInRoot()

    private fun clusterDrawn(): Boolean =
        composeRule.onAllNodesWithContentDescription("Fullscreen").fetchSemanticsNodes().isNotEmpty()

    private fun centreOf(bounds: DpRect): Offset = with(composeRule.density) {
        Offset(((bounds.left + bounds.right) / 2).toPx(), ((bounds.top + bounds.bottom) / 2).toPx())
    }

    /** A real hold past the long-press threshold, then a move by ([dxDp], [dyDp]), on the minimise handle. */
    private fun dragHandle(dxDp: Dp, dyDp: Dp) {
        val start = centreOf(composeRule.onNodeWithTag(MINIMIZE_HANDLE_TAG).getUnclippedBoundsInRoot())
        val delta = with(composeRule.density) { Offset(dxDp.toPx(), dyDp.toPx()) }
        composeRule.onRoot().performTouchInput {
            down(start)
            advanceEventTime(600)
            moveTo(start + delta)
            advanceEventTime(50)
            up()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `the cluster's side and height survive a restart, minimised does not, and nothing is drawn before the read`() {
        // First launch: nothing stored, the cluster opens on the right, centred.
        val (firstRepository, firstScope) = launchRepository()
        val firstViewModel = mapLayersViewModel(errorLog = errorLog, clusterPlacements = firstRepository)
        var current by mutableStateOf(firstViewModel)
        composeRule.setContent { key(current) { MapLayersTestScreen(current, map.slot, AbsentForecastCellStore) } }
        composeRule.waitUntil(5_000) { clusterDrawn() }
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val before = bar()
        assertTrue("it opens on the right ($before)", before.left.value > (root.left.value + root.right.value) / 2)

        // A left-handed user drags it to the left and lower.
        dragHandle(dxDp = (-160).dp, dyDp = 120.dp)
        val dragged = bar()
        assertTrue("it snapped to the left ($dragged)", dragged.right.value < (root.left.value + root.right.value) / 2)
        assertTrue("it moved down (top ${before.top} -> ${dragged.top})", dragged.top.value > before.top.value + 50f)
        composeRule.waitUntil(5_000) { stored(firstRepository)?.portraitOnLeft == true }

        // Then minimises it, which is not stored.
        composeRule.onRoot().performTouchInput { click(centreOf(composeRule.onNodeWithTag(MINIMIZE_HANDLE_TAG).getUnclippedBoundsInRoot())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(RESTORE_HANDLE_DESCRIPTION).assertExists()

        // The process ends.
        runBlocking { firstScope.cancelAndJoin() }
        val file = rawCluster()
        assertEquals("the file holds the left side", true, file.first)
        assertTrue("the file holds a height below centre, in dp (${file.second})", file.second!! > 50f)

        // Second launch, its read held so the frames before it lands can be seen.
        val (secondRepository, _) = launchRepository()
        val held = HeldClusterRead(secondRepository)
        val secondViewModel = mapLayersViewModel(errorLog = errorLog, clusterPlacements = held)
        composeRule.runOnIdle { current = secondViewModel }
        composeRule.waitForIdle()

        assertTrue("the map is there", composeRule.onAllNodesWithTag("map-slot").fetchSemanticsNodes().isNotEmpty())
        assertEquals("no cluster is drawn before the stored side is read", false, clusterDrawn())
        assertEquals("nor its handles", 0, composeRule.onAllNodesWithTag(MINIMIZE_HANDLE_TAG).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithContentDescription(RESTORE_HANDLE_DESCRIPTION).fetchSemanticsNodes().size)

        held.release()
        composeRule.waitUntil(5_000) { clusterDrawn() }
        val restored = bar()
        assertTrue("on the left, where it was left (dragged $dragged, restored $restored)", abs(restored.left.value - dragged.left.value) <= 1f)
        assertTrue("at the height it was dragged to (dragged $dragged, restored $restored)", abs(restored.top.value - dragged.top.value) <= 1f)
        assertEquals("not minimised: that is session-only", 0, composeRule.onAllNodesWithContentDescription(RESTORE_HANDLE_DESCRIPTION).fetchSemanticsNodes().size)
        assertEquals(emptyList<String>(), logged.filter { "icon cluster" in it })
    }

    @Test
    fun `a stored height beyond this window's limits opens inside them, above the bottom nav, and is not rewritten`() {
        val (seeder, seederScope) = launchRepository()
        runBlocking {
            seeder.setMapIconClusterPlacement(MapIconClusterPlacement.DEFAULT.copy(portraitOnLeft = true, portraitOffsetDp = 5_000f)).getOrThrow()
            seederScope.cancelAndJoin()
        }

        val (repository, repositoryScope) = launchRepository()
        val viewModel = mapLayersViewModel(errorLog = errorLog, clusterPlacements = repository)
        composeRule.setContent { MapLayersTestScreen(viewModel, map.slot, AbsentForecastCellStore) }
        composeRule.waitUntil(5_000) { clusterDrawn() }
        composeRule.waitForIdle()

        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val navTop = composeRule.onNodeWithText("Maps").getUnclippedBoundsInRoot().top
        val addRowBottom = composeRule.onNodeWithContentDescription(ADD_ROW_DESCRIPTION).getUnclippedBoundsInRoot().bottom
        assertTrue("on the stored left side (${bar()})", bar().right.value < (root.left.value + root.right.value) / 2)
        assertTrue("the add row's bottom ($addRowBottom) is at or above the nav's top ($navTop)", addRowBottom.value <= navTop.value + 1f)
        val barCentreY = (bar().top.value + bar().bottom.value) / 2
        val mapMiddleY = (root.top.value + navTop.value) / 2
        assertTrue("and it is held low, not reset to centre (bar centre $barCentreY, map middle $mapMiddleY)", barCentreY > mapMiddleY + 40f)

        // Nothing was dragged, so nothing was written: the stored height is kept as the memory.
        runBlocking { repositoryScope.cancelAndJoin() }
        assertEquals(5_000f, rawCluster().second)
    }

    /** The portrait side and height as the file holds them, read with no repository in between. */
    private fun rawCluster(): Pair<Boolean?, Float?> {
        val job = SupervisorJob().also { scopes += it }
        val raw = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { dataStoreFile() }
        return runBlocking {
            val prefs = raw.data.first()
            job.cancelAndJoin()
            prefs[booleanPreferencesKey("map.icon_cluster.portrait_left")] to prefs[floatPreferencesKey("map.icon_cluster.portrait_offset_dp")]
        }
    }
}

/** [inner], with its read held until [release]: the time a cold DataStore read takes, made controllable. */
private class HeldClusterRead(private val inner: MapIconClusterPlacementRepository) : MapIconClusterPlacementRepository {
    private val gate = CompletableDeferred<Unit>()

    fun release() {
        gate.complete(Unit)
    }

    override suspend fun getMapIconClusterPlacement(): Result<MapIconClusterPlacement?> {
        gate.await()
        return inner.getMapIconClusterPlacement()
    }

    override suspend fun setMapIconClusterPlacement(placement: MapIconClusterPlacement): Result<Unit> = inner.setMapIconClusterPlacement(placement)
}
