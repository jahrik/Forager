package com.zynergylabs.forager.app.ui.map

import android.graphics.Color
import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.ui.map.fanout.FanKey
import com.zynergylabs.forager.app.ui.map.fanout.FanMember
import com.zynergylabs.forager.app.ui.map.fanout.FanOffset
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.ceil

/**
 * The fan's circle as a symbol (dispatch 2026-09-28-369, amendment 3, option A): one circle feature per copy, in the icons source beside the copy's
 * glyph and below every glyph, sized by the fold's progress, in the chrome colour; one push into three sources, not four; and the glyph copies
 * Amendment 2's signal waits for unchanged. The pure builders only; that the layer draws the circle below the glyphs, and recolours on a theme
 * change, is device-only (a `MapView` cannot be built under Robolectric), so the recordings are the evidence for that part.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE) // the circle bitmap is read back pixel by pixel, as MarkerGlyphsTest reads the glyphs
class FanCircleSymbolTest {

    private fun member(layerId: String, id: String) = FanMember(FanKey(layerId, id), 45.0, -122.0, 0f, 0f, FanOffset(0f, -48f))

    private val moved = LatLng(45.001, -122.002)

    private fun frame(members: List<FanMember>, progress: Float = 0.5f) =
        fanFrameCollections(members, { moved }, null, progress, drawOrder = MAP_LAYER_REGISTRY)

    private val glyphMembers = listOf(
        member(MapLayerIds.FINDS, "f1"), member(MapLayerIds.PHOTOS, "p1"), member(MapLayerIds.WAYPOINTS, "w1"), member(MapLayerIds.PLANNED_TRIPS, "t1"),
    )

    /** What the map pushes into the icons source: the glyph copies and the circles. */
    private fun iconsPush(f: FanFrame): List<Feature> = fanPushPlan(f).single { it.first == FanOutIds.ICONS_SOURCE }.second.features().orEmpty()

    private fun Feature.isCircle() = getStringProperty(FanOutIds.IMAGE_PROPERTY) == FanOutIds.CIRCLE_IMAGE

    @Test
    fun `every copy has a circle feature in the icons push, drawn from the circle image`() {
        val circles = iconsPush(frame(glyphMembers)).filter { it.isCircle() }

        assertEquals("one circle per copy", glyphMembers.size, circles.size)
    }

    @Test
    fun `a circle is sized by the fold's progress and carries no offset`() {
        for (progress in listOf(0f, 0.5f, 1f)) {
            val circles = iconsPush(frame(glyphMembers, progress)).filter { it.isCircle() }

            assertEquals(
                "the circle's scale at progress $progress (-299: it grows with the fold)",
                List(glyphMembers.size) { progress }, circles.map { it.getNumberProperty(FanOutIds.CIRCLE_SCALE_PROPERTY)?.toFloat() },
            )
            circles.forEach {
                val offset = it.getProperty(FanOutIds.ICON_OFFSET_PROPERTY).asJsonArray
                assertEquals("a circle is centred on its coordinate", listOf(0f, 0f), listOf(offset[0].asFloat, offset[1].asFloat))
            }
        }
    }

    @Test
    fun `every circle sorts below every glyph, whichever layer the glyph is from`() {
        val features = iconsPush(frame(glyphMembers))
        val circleKeys = features.filter { it.isCircle() }.map { it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY).toInt() }
        val glyphKeys = features.filterNot { it.isCircle() }.map { it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY).toInt() }

        assertTrue("circles are present", circleKeys.isNotEmpty() && glyphKeys.isNotEmpty())
        assertTrue("every circle key ${circleKeys.max()} is below every glyph key ${glyphKeys.min()} (-318 stacking is among the glyphs only)", circleKeys.max() < glyphKeys.min())
    }

    @Test
    fun `a circle is in the same place as its copy`() {
        val features = iconsPush(frame(glyphMembers))
        val glyphPoints = features.filterNot { it.isCircle() }.map { (it.geometry() as Point).coordinates() }
        val circlePoints = features.filter { it.isCircle() }.map { (it.geometry() as Point).coordinates() }

        assertEquals(glyphPoints, circlePoints)
    }

    @Test
    fun `the glyph copies are what they were, with their ids, images, offsets and sort keys`() {
        val f = frame(glyphMembers, progress = 1f)
        val glyphs = iconsPush(f).filterNot { it.isCircle() }

        assertEquals(glyphMembers.map { it.key.featureId }, glyphs.map { it.getStringProperty(FEATURE_ID_PROPERTY) })
        glyphs.forEach {
            assertNotNull(it.getStringProperty(FanOutIds.IMAGE_PROPERTY))
            assertNotNull(it.getProperty(FanOutIds.ICON_OFFSET_PROPERTY))
            assertNotNull(it.getNumberProperty(FanOutIds.SORT_KEY_PROPERTY))
            assertNull("a glyph copy has no circle scale", it.getNumberProperty(FanOutIds.CIRCLE_SCALE_PROPERTY))
        }
        assertEquals("the glyph features are exactly the frame's icons", f.icons.features().orEmpty().size, glyphs.size)
    }

    @Test
    fun `the push goes to three sources and the circles source is gone`() {
        val plan = fanPushPlan(frame(glyphMembers))

        assertEquals(setOf(FanOutIds.LEGS_SOURCE, FanOutIds.DOTS_SOURCE, FanOutIds.ICONS_SOURCE), plan.map { it.first }.toSet())
        assertEquals("one setGeoJson per source", 3, plan.size)
    }

    @Test
    fun `an empty frame still clears all three sources`() {
        val plan = fanPushPlan(frame(emptyList()))

        assertEquals(3, plan.size)
        assertTrue(plan.all { it.second.features().orEmpty().isEmpty() })
    }

    @Test
    fun `the icon layer sizes a circle by its scale and a glyph at one`() {
        val size = fanIconLayerProperties().firstOrNull { it.name == "icon-size" }

        assertNotNull("the icon layer has an icon-size", size)
        val expression = size!!.value.toString()
        assertTrue("it reads the circle scale property: $expression", expression.contains(FanOutIds.CIRCLE_SCALE_PROPERTY))
    }

    @Test
    fun `the circle bitmap is the chrome colour at the circle's opacity, 36 dp across`() {
        val density = 2.5f
        val chrome = Color.rgb(200, 190, 170)
        val bitmap = fanCircleBitmap(density, chrome)

        val px = ceil(FAN_CIRCLE_DIAMETER_DP * density).toInt()
        assertEquals(px, bitmap.width)
        val centre = bitmap.getPixel(px / 2, px / 2)
        assertEquals("the centre's colour is the chrome colour", chrome and 0xFFFFFF, centre and 0xFFFFFF)
        assertEquals("at the circle's opacity", Math.round(255f * FAN_CIRCLE_OPACITY), Color.alpha(centre))
        assertEquals("the corner is outside the circle", 0, Color.alpha(bitmap.getPixel(0, 0)))
    }

    @Test
    fun `a different chrome colour gives a different circle`() {
        val day = fanCircleBitmap(2f, Color.rgb(240, 235, 225)).getPixel(36, 36)
        val night = fanCircleBitmap(2f, Color.rgb(40, 40, 45)).getPixel(36, 36)

        assertNotEquals("the circle follows the chrome colour on a theme change", day, night)
    }

    @Test
    fun `the signal's expectation ignores circle features and still counts the glyph copies`() {
        val features = iconsPush(frame(glyphMembers))
        val ids = renderedCopyIds(features)

        assertEquals("only the glyph copies, one per member", glyphMembers.size, ids.size)
        assertFalse("no circle is mistaken for a copy", ids.any { it.startsWith(FanOutIds.CIRCLE_IMAGE) })
        val expected = expectedCopies(glyphMembers)
        assertTrue("the signal fires when the glyphs are reported, circles or not", copiesDrawn(expected, ids, emptySet(), 0))
    }

    @Test
    fun `a rendered circle alone does not satisfy the signal`() {
        val circleOnly = iconsPush(frame(glyphMembers)).filter { it.isCircle() }
        val ids = renderedCopyIds(circleOnly)

        assertTrue(ids.isEmpty())
        assertFalse(copiesDrawn(expectedCopies(glyphMembers), ids, emptySet(), 0))
    }

    // Registering the image a second time, as the map did at every style load, moved the glyphs' sampling: the open fan's rest frame differed from the
    // current build's in about 9,500 pixels instead of about 4,600 (amendment 3 fix, measured on the S22). So the image is registered again only when
    // the colour changes: a night switch or a palette change.
    @Test
    fun `the circle image is not registered again when its colour is already the wanted one`() {
        assertFalse(fanCircleNeedsRecolour(registered = Color.rgb(240, 235, 225), wanted = Color.rgb(240, 235, 225)))
    }

    @Test
    fun `the circle image is registered again when the chrome colour changes`() {
        assertTrue(fanCircleNeedsRecolour(registered = Color.rgb(240, 235, 225), wanted = Color.rgb(40, 40, 45)))
    }

    @Test
    fun `a style with no registered colour yet gets the image`() {
        assertTrue(fanCircleNeedsRecolour(registered = null, wanted = Color.rgb(240, 235, 225)))
    }
}
