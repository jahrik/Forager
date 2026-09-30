package com.zynergylabs.forager.app.ui.map

import android.graphics.Color
import android.graphics.RectF
import com.zynergylabs.forager.app.ui.theme.MapPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

/**
 * The marker silhouettes of colour build C2 (d), at the level Robolectric reaches: the glyph board's
 * geometry (`docs/audits/2026-09-26-marker-glyph-board.md` §1) as Android [android.graphics.Path]s,
 * and the bitmaps drawn from them, read back pixel by pixel. [GraphicsMode.Mode.NATIVE] makes
 * Robolectric rasterise for real, so a pixel here is what the Canvas drew.
 *
 * What this checks: each glyph's fill extent and anchor in dp; that the anchor lands at the bitmap's
 * exact centre, which is what `icon-anchor: center` puts on the feature's coordinate; that the pixel
 * just inside the feature at the anchor is the fill colour; that the casing is drawn and the padding
 * holds all of it (every border pixel is transparent); the reticle's arms passing the ring; and that
 * the find marker is a mushroom and each icon is drawn in its own role's colour.
 *
 * What it cannot check: how MapLibre scales and resamples the image on a device, whether the shapes
 * read as distinct at a glance, and whether a tap lands on the drawn anchor. Those are device items,
 * deferred to beta.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MarkerGlyphsTest {

    private val density = 3f
    private val fill = 0xFFDA02AF.toInt()
    private val casing = 0xFFFFFFFF.toInt()

    /** The glyph board §1 fill extents and anchors, and the reticle's own dimensions (C2), in dp. */
    private val expected = mapOf(
        MarkerGlyph.WAYPOINT to floatArrayOf(22f, 28f, 11f, 28f),
        MarkerGlyph.FIND to floatArrayOf(24f, 26f, 12f, 26f),
        MarkerGlyph.PLANNED_TRIP to floatArrayOf(20f, 28f, 1.5f, 28f),
        MarkerGlyph.PHOTO to floatArrayOf(22f, 22f, 11f, 11f),
        MarkerGlyph.SEARCH_CENTRE to floatArrayOf(26f, 26f, 13f, 13f),
    )

    /**
     * Dispatch 2026-09-28-275. `icon-offset` moves the image from its anchor, positive right and down. The
     * image is anchored (`icon-anchor: center`) at the glyph's anchor, so to put the fill extent's centre
     * on the point the image moves by anchor minus half-extent: a pin whose body centre is 14dp above its
     * tip moves 14dp down.
     */
    @Test
    fun `the fan centring offset moves each glyph's fill-extent centre onto the point`() {
        val expected = mapOf(
            MarkerGlyph.PHOTO to (0f to 0f),
            MarkerGlyph.WAYPOINT to (0f to 14f),
            MarkerGlyph.FIND to (0f to 13f),
            MarkerGlyph.PLANNED_TRIP to (-8.5f to 14f),
            MarkerGlyph.SEARCH_CENTRE to (0f to 0f),
        )
        for ((glyph, xy) in expected) {
            val offset = glyph.fanCentringOffsetDp()
            assertEquals("$glyph fan centring offset x", xy.first, offset.xDp, 1e-4f)
            assertEquals("$glyph fan centring offset y", xy.second, offset.yDp, 1e-4f)
        }
    }

    @Test
    fun `each glyph's fill extent and anchor are the glyph board's`() {
        for ((glyph, dims) in expected) {
            assertEquals("$glyph width", dims[0], glyph.widthDp)
            assertEquals("$glyph height", dims[1], glyph.heightDp)
            assertEquals("$glyph anchor x", dims[2], glyph.anchorXDp)
            assertEquals("$glyph anchor y", dims[3], glyph.anchorYDp)
            val bounds = fillBounds(glyph)
            assertEquals("$glyph path left", 0f, bounds.left, 0.01f)
            assertEquals("$glyph path top", 0f, bounds.top, 0.01f)
            assertEquals("$glyph path right", dims[0], bounds.right, 0.01f)
            assertEquals("$glyph path bottom", dims[1], bounds.bottom, 0.01f)
        }
    }

    @Test
    fun `the anchor is the bitmap's exact centre, so icon-anchor center puts it on the coordinate`() {
        for (glyph in MarkerGlyph.entries) {
            val image = drawGlyph(glyph, density, fill, casing)
            assertEquals("$glyph width is even", 0, image.bitmap.width % 2)
            assertEquals("$glyph height is even", 0, image.bitmap.height % 2)
            assertEquals("$glyph anchor x", image.bitmap.width / 2, image.anchorXPx)
            assertEquals("$glyph anchor y", image.bitmap.height / 2, image.anchorYPx)
        }
    }

    @Test
    fun `the feature at the anchor is drawn in the fill colour`() {
        // Just inside each feature at its anchor: 1dp above the pin tip, the stem foot and the pole
        // foot; the centre itself for the photo (its lens) and the reticle (its crossing arms).
        val inside = mapOf(
            MarkerGlyph.WAYPOINT to (0f to -1.5f),
            MarkerGlyph.FIND to (0f to -1f),
            MarkerGlyph.PLANNED_TRIP to (0f to -1f),
            MarkerGlyph.PHOTO to (0f to 0f),
            MarkerGlyph.SEARCH_CENTRE to (0f to 0f),
        )
        for ((glyph, offset) in inside) {
            val image = drawGlyph(glyph, density, fill, casing)
            assertColour("$glyph at the anchor", fill, image, offset.first, offset.second)
        }
    }

    @Test
    fun `the bottom-anchored glyphs are cased below their anchor`() {
        for (glyph in listOf(MarkerGlyph.WAYPOINT, MarkerGlyph.FIND, MarkerGlyph.PLANNED_TRIP)) {
            val image = drawGlyph(glyph, density, fill, casing)
            assertColour("$glyph casing below the anchor", casing, image, 0f, CASING_WIDTH_DP / 2)
        }
    }

    @Test
    fun `the padding holds the whole casing, so nothing is clipped`() {
        for (glyph in MarkerGlyph.entries) {
            val bitmap = drawGlyph(glyph, density, fill, casing).bitmap
            val w = bitmap.width
            val h = bitmap.height
            val border = (0 until w).flatMap { listOf(it to 0, it to h - 1) } + (0 until h).flatMap { listOf(0 to it, w - 1 to it) }
            val painted = border.filter { (x, y) -> Color.alpha(bitmap.getPixel(x, y)) != 0 }
            assertTrue("$glyph paints its bitmap border at ${painted.take(3)}", painted.isEmpty())
            // And the casing is there: half a casing outside the fill extent's top edge, at the
            // anchor's x (inside the glyph's own span for every glyph).
            val image = drawGlyph(glyph, density, fill, casing)
            assertColour("$glyph casing above the top edge", casing, image, 0f, -glyph.anchorYDp - CASING_WIDTH_DP / 2)
        }
    }

    @Test
    fun `the reticle's arms pass the ring's outer edge`() {
        assertTrue(
            "arm half-length ${SEARCH_CENTRE_ARM_DP}dp is past the ring's outer edge and its casing",
            SEARCH_CENTRE_ARM_DP > SEARCH_CENTRE_RING_RADIUS_DP + SEARCH_CENTRE_STROKE_DP / 2 + CASING_WIDTH_DP,
        )
        val image = drawGlyph(MarkerGlyph.SEARCH_CENTRE, density, fill, casing)
        val outer = SEARCH_CENTRE_RING_RADIUS_DP + SEARCH_CENTRE_STROKE_DP / 2
        val beyond = (outer + CASING_WIDTH_DP + SEARCH_CENTRE_ARM_DP) / 2
        for ((dx, dy) in listOf(beyond to 0f, -beyond to 0f, 0f to beyond, 0f to -beyond)) {
            assertColour("arm at ($dx, $dy)dp, beyond the ring", fill, image, dx, dy)
        }
        assertColour("the ring itself", fill, image, SEARCH_CENTRE_RING_RADIUS_DP * 0.7071f, SEARCH_CENTRE_RING_RADIUS_DP * 0.7071f)
        assertTransparent("off the arms, beyond the ring", image, beyond, beyond / 2)
        assertTransparent("inside the ring, off the arms", image, 3f, 3f)
    }

    @Test
    fun `the waypoint pin carries a hollow ring on its head`() {
        val image = drawGlyph(MarkerGlyph.WAYPOINT, density, fill, casing)
        // Head centre (11, 11) from the top-left; the anchor is (11, 28), so the head is 17dp above it.
        assertColour("the ring's hollow is the pin's own fill", fill, image, 0f, -17f)
        assertColour("the ring, in the casing colour", casing, image, 2.75f, -17f)
        assertColour("the head outside the ring", fill, image, 6f, -17f)
    }

    @Test
    fun `the photo marker carries a camera in the casing colour`() {
        val image = drawGlyph(MarkerGlyph.PHOTO, density, fill, casing)
        // Body x 4..18, y 7..17 from the top-left; the anchor is (11, 11).
        assertColour("the camera body", casing, image, -5.5f, 4f)
        assertColour("the viewfinder bump", casing, image, 0f, -5.5f)
        assertColour("the lens, in the fill colour", fill, image, 0f, 1f)
        assertColour("the square outside the camera", fill, image, -8.5f, -8.5f)
    }

    /**
     * The find marker is a mushroom, not the pin it used to share with the offline region: a dome as
     * wide as the glyph just above its flat underside, and a stem foot 8dp wide. Read from the image
     * the map registers for the find icon, so the icon wiring is what is checked.
     */
    @Test
    fun `the find marker is a mushroom anchored at its stem foot`() {
        val image = markerIconImage(MarkerIcon.FIND, MapPalette.DAY, density)
        val find = MapPalette.DAY.find
        assertEquals("find icon size", drawGlyph(MarkerGlyph.FIND, density, find, MapPalette.DAY.casing).bitmap.width, image.bitmap.width)
        // Relative to the stem foot (12, 26): the dome's left rim just above its underside (0.5, 13.5)...
        assertColour("the dome's rim at its widest", find, image, -11.5f, -12.5f)
        // ...the stem's foot corners (8.5, 25.5) and (15.5, 25.5)...
        assertColour("the stem foot, left", find, image, -3.5f, -0.5f)
        assertColour("the stem foot, right", find, image, 3.5f, -0.5f)
        // ...and nothing beside the stem, under the dome (4, 22).
        assertTransparent("beside the stem, under the dome", image, -8f, -4f)
    }

    @Test
    fun `each marker icon is its own glyph in its own role's colour, day and night`() {
        val glyphs = mapOf(
            MarkerIcon.WAYPOINT to MarkerGlyph.WAYPOINT,
            MarkerIcon.FIND to MarkerGlyph.FIND,
            MarkerIcon.PLANNED_TRIP to MarkerGlyph.PLANNED_TRIP,
            MarkerIcon.PHOTO to MarkerGlyph.PHOTO,
            MarkerIcon.SEARCH_CENTRE to MarkerGlyph.SEARCH_CENTRE,
        )
        // J8's three halo icons have their own test (JournalHaloGlyphTest); every marker icon is here.
        assertEquals(MarkerIcon.entries.filterNot { it.halo }.toSet(), glyphs.keys)
        assertEquals("every image id is distinct", MarkerIcon.entries.size, MarkerIcon.entries.map { it.imageId }.toSet().size)
        for (palette in listOf(MapPalette.DAY, MapPalette.NIGHT)) {
            val roles = mapOf(
                MarkerIcon.WAYPOINT to palette.waypoint,
                MarkerIcon.FIND to palette.find,
                MarkerIcon.PLANNED_TRIP to palette.plannedTrip,
                MarkerIcon.PHOTO to palette.photo,
                MarkerIcon.SEARCH_CENTRE to palette.searchCentre,
            )
            for ((icon, glyph) in glyphs) {
                assertEquals("$icon glyph", glyph, icon.glyph)
                val image = markerIconImage(icon, palette, density)
                val anchorInside = if (glyph.anchorYDp == glyph.heightDp) -1.5f else 0f
                assertColour("$icon fill", roles.getValue(icon), image, 0f, anchorInside)
                assertColour("$icon casing", palette.casing, image, 0f, -glyph.anchorYDp - CASING_WIDTH_DP / 2)
            }
        }
    }

    private fun fillBounds(glyph: MarkerGlyph): RectF {
        val all = RectF()
        glyph.parts().filter { it.paint == GlyphPaint.FILL }.forEachIndexed { i, part ->
            val b = RectF()
            part.path.computeBounds(b, true)
            if (i == 0) all.set(b) else all.union(b)
        }
        return all
    }

    /** The pixel [dxDp], [dyDp] from the anchor. */
    private fun pixel(image: GlyphImage, dxDp: Float, dyDp: Float): Int {
        val x = (image.anchorXPx + dxDp * density).roundToInt().coerceIn(0, image.bitmap.width - 1)
        val y = (image.anchorYPx + dyDp * density).roundToInt().coerceIn(0, image.bitmap.height - 1)
        return image.bitmap.getPixel(x, y)
    }

    private fun assertColour(what: String, expected: Int, image: GlyphImage, dxDp: Float, dyDp: Float) {
        val actual = pixel(image, dxDp, dyDp)
        assertEquals("$what: #%08X".format(actual), "#%08X".format(expected), "#%08X".format(actual))
    }

    private fun assertTransparent(what: String, image: GlyphImage, dxDp: Float, dyDp: Float) {
        val actual = pixel(image, dxDp, dyDp)
        assertEquals("$what: #%08X".format(actual), 0, Color.alpha(actual))
    }
}
