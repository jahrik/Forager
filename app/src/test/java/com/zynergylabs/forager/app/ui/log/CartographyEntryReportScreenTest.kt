package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.CartographyEntryMapData
import com.zynergylabs.forager.app.domain.LocationResult
import com.zynergylabs.forager.app.domain.model.CartographyEntry
import com.zynergylabs.forager.app.domain.model.DerivedTrip
import com.zynergylabs.forager.app.domain.model.DistanceUnit
import com.zynergylabs.forager.app.domain.model.FindDecision
import com.zynergylabs.forager.app.domain.model.GalleryPhoto
import com.zynergylabs.forager.app.domain.model.LogPhoto
import com.zynergylabs.forager.app.domain.model.OfflineRegionDecision
import com.zynergylabs.forager.app.domain.model.PhotoAttachment
import com.zynergylabs.forager.app.domain.model.TrackDecision
import com.zynergylabs.forager.app.domain.model.WaypointDecision
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * [CartographyEntryReportScreen] — Journal Stage 2c. Renders purely from [CartographyEntry]'s own
 * snapshotted fields, with no [candidates][com.zynergylabs.forager.app.domain.DerivedTrip]/repository parameter
 * at all — every test here constructs an entry directly, proving structurally (not just by
 * assertion) that nothing about this screen can ever depend on a kept track/waypoint/offline-region/
 * find still existing elsewhere. See that composable's own doc comment for the full reasoning.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CartographyEntryReportScreenTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private val baseEntry = CartographyEntry.draft(id = "entry-1", date = LocalDate.of(2026, 8, 1), updatedAtEpochMillis = 1_000L)
        .copy(isDraft = false)

    @Test
    fun `a wordless entry with only two kept photos renders with no incomplete treatment anywhere`() {
        val entry = baseEntry.copy(
            text = "",
            tags = emptyList(),
            photos = listOf(
                PhotoAttachment(photoId = "p1", attachedAtEpochMillis = 1_000L),
                PhotoAttachment(photoId = "p2", attachedAtEpochMillis = 2_000L),
            ),
        )
        val galleryPhotos = listOf(
            GalleryPhoto(photo = LogPhoto(id = "p1", relativePath = "photos/p1.jpg", createdAtEpochMillis = 1_000L), referencingEntryIds = emptyList()),
            GalleryPhoto(photo = LogPhoto(id = "p2", relativePath = "photos/p2.jpg", createdAtEpochMillis = 2_000L), referencingEntryIds = emptyList()),
        )

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = galleryPhotos, distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        // Data part A (dispatch 2026-09-28-667): the date reads "Aug 1, 2026" (the owner, RECORD -656).
        composeRule.onNodeWithText("Aug 1, 2026").assertIsDisplayed()
        // No section headings render at all — nothing was kept in any of them.
        composeRule.onNodeWithText("Finds").assertDoesNotExist()
        composeRule.onNodeWithText("Tracks").assertDoesNotExist()
        composeRule.onNodeWithText("Waypoints").assertDoesNotExist()
        composeRule.onNodeWithText("Offline maps").assertDoesNotExist()
        // Never "Not recorded yet." or any other placeholder — see this class's own doc comment.
        composeRule.onNodeWithText("Not recorded yet.").assertDoesNotExist()
        composeRule.onNodeWithText("recorded", substring = true, ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `text and tags are omitted when blank or empty`() {
        // Not baseEntry as-is: a fully empty entry hits the Stage 2d empty-entry message instead of
        // this screen's ordinary body, which is a separate case covered below — one kept waypoint
        // keeps this test in the ordinary body while text/tags stay blank/empty.
        val entry = baseEntry.copy(waypointDecisions = listOf(WaypointDecision(waypointId = "w1", name = "Trailhead", lat = 45.5, lng = -122.6, kept = true)))

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText("Tags:", substring = true).assertDoesNotExist()
    }

    /** Stage 2d: nothing kept, no text, no tags — the same dead end [LogEntryReportScreen] hits, with a message written for what a Cartography entry actually holds. */
    @Test
    fun `an entirely empty entry shows the empty-entry message, exactly as worded`() {
        composeRule.setContent {
            CartographyEntryReportScreen(entry = baseEntry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText(
            "This entry has nothing included yet. An entry can hold the finds, tracks, waypoints, offline " +
                "maps, and photos you choose to include from a day's records, plus anything you write. " +
                "Tap the three-dot menu, then Edit, to add something.",
        ).assertIsDisplayed()
    }

    /** Standing rule, restated for this exact case: a wordless entry with only kept photos is complete and must never show the empty-entry message. */
    @Test
    fun `a wordless entry with only kept photos shows no empty-entry message`() {
        val entry = baseEntry.copy(photos = listOf(PhotoAttachment(photoId = "p1", attachedAtEpochMillis = 1_000L)))

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText("This entry has nothing included yet.", substring = true).assertDoesNotExist()
    }

    @Test
    fun `text and tags render when present`() {
        val withWriting = baseEntry.copy(text = "A quiet ridge walk.", tags = listOf("chanterelle", "ridge"))
        composeRule.setContent {
            CartographyEntryReportScreen(entry = withWriting, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText("A quiet ridge walk.").assertIsDisplayed()
        composeRule.onNodeWithText("Tags: chanterelle, ridge").assertIsDisplayed()
    }

    @Test
    fun `only kept decisions render, never a withheld one`() {
        val entry = baseEntry.copy(
            findDecisions = listOf(
                FindDecision(findId = "f1", foundOn = LocalDate.of(2026, 8, 1), ownIdentification = "Chanterelle", hasPhotos = false, kept = true),
                FindDecision(findId = "f2", foundOn = LocalDate.of(2026, 8, 1), ownIdentification = "Withheld find", hasPhotos = false, kept = false),
            ),
            trackDecisions = listOf(
                TrackDecision(trackId = "t1", name = "Ridge Loop", distanceMeters = 4_828.0, durationMillis = 5_400_000L, pointCount = 240, kept = true),
            ),
            waypointDecisions = listOf(
                WaypointDecision(waypointId = "w1", name = "Trailhead", lat = 45.5, lng = -122.6, kept = true),
            ),
            offlineRegionDecisions = listOf(
                OfflineRegionDecision(offlineRegionId = 1L, name = "Ridge Region", lat = 45.5, lng = -122.6, radiusKm = 10, kept = true),
            ),
        )

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        // Data part A: "Finds" is now both the tile's label and the list's heading, so the tile is read by its tag.
        composeRule.onNodeWithTag(entryTileTag(TILE_FINDS)).assert(hasText("1"))
        composeRule.onNodeWithText("Find on Aug 1, 2026").assertIsDisplayed()
        composeRule.onNodeWithText("Chanterelle").assertIsDisplayed()
        composeRule.onNodeWithText("Withheld find").assertDoesNotExist()

        composeRule.onNodeWithText("Tracks").assertIsDisplayed()
        composeRule.onNodeWithText("Ridge Loop").assertIsDisplayed()

        composeRule.onNodeWithText("Waypoints").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Trailhead").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithText("Offline maps").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ridge Region").performScrollTo().assertIsDisplayed()
    }

    /**
     * The dispatch's own required case: a kept track whose underlying [com.zynergylabs.forager.app.domain.model.Track]
     * has since been deleted from Records must still render its snapshotted text, structurally
     * impossible to error on since this screen never resolves the id against anything.
     */
    @Test
    fun `a kept track survives as text even though nothing backs its id`() {
        val entry = baseEntry.copy(
            trackDecisions = listOf(
                TrackDecision(trackId = "deleted-track", name = "Gone Now Loop", distanceMeters = 3_200.0, durationMillis = 1_800_000L, pointCount = 100, kept = true),
            ),
        )

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText("Gone Now Loop").assertIsDisplayed()
        // RECORD -671: with the day's records not loaded, the track is not known to be gone, so no note.
        composeRule.onNodeWithText(TRACK_NOT_IN_RECORDS_LINE).assertDoesNotExist()
    }

    /** RECORD -671: with the day loaded and the track not in it, its name row says it is gone and has nothing to open. */
    @Test
    fun `a kept track gone from a loaded day says it is no longer in Records and has no tap`() {
        val entry = baseEntry.copy(
            trackDecisions = listOf(
                TrackDecision(trackId = "deleted-track", name = "Gone Now Loop", distanceMeters = 3_200.0, durationMillis = 1_800_000L, pointCount = 100, kept = true),
            ),
        )
        val day = DerivedTrip(date = entry.date, finds = emptyList(), tracks = emptyList(), waypoints = emptyList(), offlineRegions = emptyList())

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {}, candidates = day)
        }

        composeRule.onNodeWithText("Gone Now Loop").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(TRACK_NOT_IN_RECORDS_LINE).assertIsDisplayed()
        composeRule.onNodeWithTag(entryTrackRowTag("deleted-track")).assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    /** Stage 2c dispatch, point 5: reuses [CartographyEntryEditScreen]'s own fallback — proves it still renders when the gallery row is gone. */
    @Test
    fun `a kept photo whose gallery row is gone shows the Photo unavailable fallback`() {
        val entry = baseEntry.copy(photos = listOf(PhotoAttachment(photoId = "missing", attachedAtEpochMillis = 1_700_000_000_000L)))

        composeRule.setContent {
            CartographyEntryReportScreen(entry = entry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithText("Photo unavailable", substring = true).assertIsDisplayed()
    }

    @Test
    fun `the overflow menu's Edit entry option calls onEdit`() {
        var editCalls = 0
        composeRule.setContent {
            CartographyEntryReportScreen(entry = baseEntry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={ editCalls++ }, onDeleteEntry = {}, onBack = {})
        }

        composeRule.onNodeWithContentDescription("Entry options").performClick()
        composeRule.onNodeWithText("Edit entry").performClick()

        assertEquals(1, editCalls)
    }

    /** Planner decision: this confirms, matching [CartographyEntryEditScreen]'s own existing delete confirmation — unlike [LogEntryReportScreen]'s immediate delete. */
    @Test
    fun `the overflow menu's Delete entry option confirms before calling onDeleteEntry`() {
        var deleteCalls = 0
        composeRule.setContent {
            CartographyEntryReportScreen(entry = baseEntry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = { deleteCalls++ }, onBack = {})
        }

        composeRule.onNodeWithContentDescription("Entry options").performClick()
        composeRule.onNodeWithText("Delete entry").performClick()

        assertEquals("must not delete before the dialog confirms", 0, deleteCalls)
        composeRule.onNodeWithText("Delete this entry?").assertIsDisplayed()

        composeRule.onNodeWithText("Delete").performClick()

        assertEquals(1, deleteCalls)
    }

    @Test
    fun `cancelling the delete confirmation never calls onDeleteEntry`() {
        var deleteCalls = 0
        composeRule.setContent {
            CartographyEntryReportScreen(entry = baseEntry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = { deleteCalls++ }, onBack = {})
        }

        composeRule.onNodeWithContentDescription("Entry options").performClick()
        composeRule.onNodeWithText("Delete entry").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(0, deleteCalls)
        composeRule.onNodeWithText("Delete this entry?").assertDoesNotExist()
    }

    @Test
    fun `the back arrow calls onBack`() {
        var backCalls = 0
        composeRule.setContent {
            CartographyEntryReportScreen(entry = baseEntry, galleryPhotos = emptyList(), distanceUnit = DistanceUnit.MILES, mapSlot = NoOpMapSlot, night = false, getMapData = { _, _ -> EmptyCartographyEntryMapData }, getCoveringOfflineRegion = { _, _ -> null }, getCurrentLocation = { LocationResult.LocationUnavailable }, onEdit ={}, onDeleteEntry = {}, onBack = { backCalls++ })
        }

        composeRule.onNodeWithContentDescription("Back to Cartography").performClick()

        assertEquals(1, backCalls)
    }
}

private val NoOpMapSlot: MapSlot = { _, _, _, _, _, _, _, _, _ -> }

private val EmptyCartographyEntryMapData = CartographyEntryMapData(
    trackPolylines = emptyList(),
    findMarkers = emptyList(),
    waypointMarkers = emptyList(),
    photoMarkers = emptyList(),
    offlineRegionCircles = emptyList(),
)
