package com.zynergylabs.forager.app.ui.availability

import android.graphics.Bitmap
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import com.zynergylabs.forager.app.domain.CompassProvider
import com.zynergylabs.forager.app.domain.CompassReading
import com.zynergylabs.forager.app.domain.ComputeTrueHeadingUseCase
import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.DeclinationProvider
import com.zynergylabs.forager.app.domain.FixProvider
import com.zynergylabs.forager.app.domain.HeadingUncertainty
import com.zynergylabs.forager.app.domain.LocationFix
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.Waypoint
import com.zynergylabs.forager.app.domain.model.WaypointDesignation
import com.zynergylabs.forager.app.ui.map.MapSlot
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

/**
 * Dispatch 2026-10-13-02, RECORD -983 (the owner: "Another thing for the landscape mode UI, have the telemetry centered between
 * the compass arrow and the three dot menu. Right now it's way too close to the arrow"), through the real [AvailabilityScreen]
 * in landscape at 780 and 823 dp, font scale 1.0 and 2.0.
 *
 * - **Not navigating:** the readout's centre is the midpoint of the arrow's trailing edge and the three dots' leading edge,
 *   within 1 dp.
 * - **Navigating** (the line has no arrow, RECORD -950; the planner's call): the midpoint of the strip's leading edge and the
 *   dots' leading edge.
 *
 * What is measured, from the semantics tree:
 * - **The readout:** the span of every text node on the strip's line (labels, values and the "·" separators; nothing else on
 *   the strip is text here: no evening lines, the compass reading fixed).
 * - **The arrow:** its fixed 18 dp slot (COMPASS_STRIP_NEEDLE_TAG), which the needle turns inside.
 * - **The dots:** the three-dot icon's own bounds, 10/24 of the way in, where Material's MoreVert draws them (a reading of
 *   "the three dot menu" as what is seen, not the 36 dp tap target round it, which starts 8.3 dp before the dots).
 *
 * Before RECORD -983 the readout was centred in its slot, which ends 8 dp short of the tap target, so not navigating it sat
 * 12.2 dp toward the arrow from this midpoint, and navigating 8.2 dp.
 */
abstract class LandscapeStripReadoutCentredTests(private val name: String) {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(mapChromeHostActivityRule()).around(composeRule)

    private val t = 1_700_000_000_000L
    private val fix = LocationFix.Update(lat = 45.52, lng = -122.68, altitude = 104.9, accuracyMeters = 5f, timestampEpochMillis = t, provider = FixProvider.GPS)
    private val start = Waypoint(id = "origin", lat = 45.5135, lng = -122.67, altitude = null, name = "Start", note = "", createdAtEpochMillis = t, trackId = "t1", designation = WaypointDesignation.ORIGIN)
    private val map: MapSlot = { _, _, _, _, _, _, _, _, modifier -> Box(modifier) }

    private fun setScreen(navigating: Boolean) {
        Shadows.shadowOf(ShadowDisplay.getDefaultDisplay()).setRotation(Surface.ROTATION_90)
        composeRule.setContent {
            AvailabilityScreen(
                uiState = AvailabilityUiState(liveFix = fix),
                isRecording = navigating,
                isReturning = navigating,
                navigationTarget = if (navigating) start else null,
                returnRoute = ReturnRoute.Ahead(LatLng(45.5135, -122.67), 245.0),
                compassProvider = FixedCompass(134f),
                computeTrueHeading = ComputeTrueHeadingUseCase(NoDeclination),
                currentTime = CurrentTimeProvider { t + 1_000L },
                mapSlot = map,
                onUseCurrentLocation = {},
                onManualLatChanged = {},
                onManualLngChanged = {},
                onSearchManualCoordinates = {},
                onRadiusChanged = {},
                onMonthSelected = {},
                onMapTabSelected = {},
                onSeasonalTabSelected = {},
                onTaxonSearchQueryChanged = {},
                onTaxonSearchResultSelected = {},
                onDismissTaxonSuggestions = {},
                onReopenTaxonSuggestions = {},
                onPlaceTripPin = { _, _, _ -> },
                onDeletePlannedTrip = {},
                onRecentSearchSelected = {},
                onOfflineMapLatChanged = {},
                onOfflineMapLngChanged = {},
                onOfflineMapRadiusChanged = {},
                onOfflineMapNameChanged = {},
                onOfflineMapsOpened = {},
                onDownloadOfflineMaps = {},
                onDeleteOfflineRegion = {},
                onNightModeMapsChanged = {},
                onThemeModeChanged = {},
            )
        }
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage()
        val dir = File("build/test-results/testDebugUnitTest/strip-readout-centred-screens").apply { mkdirs() }
        val file = File(dir, "$name-${if (navigating) "navigating" else "strip"}.png")
        file.outputStream().use { bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("SCREENSHOT|${file.absolutePath}|${bitmap.width}x${bitmap.height}")
    }

    private fun Float.pxToDp(): Dp = with(composeRule.density) { this@pxToDp.toDp() }

    private fun descendants(n: SemanticsNode): List<SemanticsNode> = n.children.flatMap { listOf(it) + descendants(it) }

    /** The text on the strip's line: every unmerged text node inside the strip, outside the three-dot button. */
    private fun readoutSpan(): Pair<Dp, Dp> {
        val strip = composeRule.onNodeWithTag(STRIP_TAG, useUnmergedTree = true).fetchSemanticsNode()
        val texts = descendants(strip)
            .filter { it.config.getOrNull(SemanticsProperties.Text) != null }
            .filterNot { n -> generateSequence(n.parent) { it.parent }.any { it.config.getOrNull(SemanticsProperties.TestTag) == MAP_QUICK_SETTINGS_BUTTON_TAG } }
        assertTrue("the strip's line has text on it", texts.isNotEmpty())
        println("MEASURED $name readout texts: ${texts.map { "'${it.config.getOrNull(SemanticsProperties.Text)?.joinToString()}' ${it.boundsInRoot.left.pxToDp().value}..${it.boundsInRoot.right.pxToDp().value}" }}")
        return texts.minOf { it.boundsInRoot.left }.pxToDp() to texts.maxOf { it.boundsInRoot.right }.pxToDp()
    }

    private fun dotsLeading(): Dp {
        val icon = composeRule.onNode(
            hasContentDescription(MAP_QUICK_SETTINGS_DESCRIPTION, substring = true) and hasAnyAncestor(hasTestTag(MAP_QUICK_SETTINGS_BUTTON_TAG)),
            useUnmergedTree = true,
        ).getUnclippedBoundsInRoot()
        return icon.left + (icon.right - icon.left) * (10f / 24f)
    }

    private fun assertCentred(what: String, leading: Dp) {
        val (left, right) = readoutSpan()
        val dots = dotsLeading()
        val centre = (left + right) / 2
        val midpoint = (leading + dots) / 2
        println(
            "MEASURED $name $what: leading ${leading.value}, dots ${dots.value}, readout ${left.value}..${right.value}, " +
                "centre ${centre.value}, midpoint ${midpoint.value}, gaps ${(left - leading).value} and ${(dots - right).value}",
        )
        assertTrue("$what: the readout ends before the dots (readout to ${right.value}, dots from ${dots.value})", right < dots)
        assertEquals(
            "$what: the readout's centre is the midpoint of ${if (what == "navigating") "the strip's leading edge" else "the arrow's trailing edge"} " +
                "(${leading.value}) and the dots' leading edge (${dots.value}), within 1 dp; readout ${left.value}..${right.value}",
            midpoint.value, centre.value, 1f,
        )
    }

    protected fun notNavigating() {
        setScreen(navigating = false)
        val needle = composeRule.onNodeWithTag(COMPASS_STRIP_NEEDLE_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertCentred("not navigating", needle.right)
    }

    protected fun navigating() {
        setScreen(navigating = true)
        assertFalse(
            "navigating, the line has no arrow (RECORD -950)",
            composeRule.onAllNodes(hasTestTag(COMPASS_STRIP_NEEDLE_TAG), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty(),
        )
        val strip = composeRule.onNodeWithTag(STRIP_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()
        // The rail is on the right at both rotations (RECORD -956), the strip beside it, so its leading edge is its left.
        assertCentred("navigating", strip.left)
    }

    private class FixedCompass(degrees: Float) : CompassProvider {
        override val heading: Flow<CompassReading?> = MutableStateFlow(CompassReading(degrees, HeadingUncertainty.Estimated(2f), 0L))
    }

    private object NoDeclination : DeclinationProvider {
        override fun declinationDegrees(latitude: Double, longitude: Double, altitudeMeters: Double?, epochMillis: Long): Float = 0f
    }

    private companion object {
        const val STRIP_TAG = "compass-elevation-strip"
    }
}

abstract class LandscapeStripReadoutCentredCases(name: String) : LandscapeStripReadoutCentredTests(name) {
    @Test fun `the readout is centred between the arrow and the three dots`() = notNavigating()

    @Test fun `navigating, the readout is centred between the strip's leading edge and the three dots`() = navigating()
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi", fontScale = 1.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeStripReadoutCentred780Font1Test : LandscapeStripReadoutCentredCases("780-font1")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi", fontScale = 2.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeStripReadoutCentred780Font2Test : LandscapeStripReadoutCentredCases("780-font2")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi", fontScale = 1.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeStripReadoutCentred823Font1Test : LandscapeStripReadoutCentredCases("823-font1")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w823dp-h384dp-land-xxhdpi", fontScale = 2.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeStripReadoutCentred823Font2Test : LandscapeStripReadoutCentredCases("823-font2")
