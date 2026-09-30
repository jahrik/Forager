package com.zynergylabs.forager.app.ui.map

import android.graphics.Bitmap
import android.graphics.Color
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
 * J8's marker halos (`prompts/preserved/2026-09-28-52.md`, J8-2: "a halo or outline drawn beneath the
 * record's own glyph"), read back pixel by pixel as [MarkerGlyphsTest] reads the markers. A halo image
 * is the marker's own silhouette grown by its casing and [JOURNAL_HALO_WIDTH_DP], in the halo colour,
 * anchored at its exact centre like the marker, so the two images drawn on one coordinate line up and
 * the halo shows as a ring [JOURNAL_HALO_WIDTH_DP] wide around the marker's casing.
 *
 * What this cannot check: how the ring reads over each basemap and at night on a screen. A device item.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class JournalHaloGlyphTest {

    private val density = 3f
    private val halo = 0xFF005577.toInt()
    private val fill = 0xFFDA02AF.toInt()
    private val casing = 0xFFFFFFFF.toInt()

    /**
     * For each haloed glyph, a point on its fill's outer edge, in dp from the anchor, and the outward
     * direction there: the waypoint's head top, the mushroom's dome top, the photo's top edge.
     */
    private val edges = mapOf(
        MarkerGlyph.WAYPOINT to Triple(0f, -28f, 0f to -1f),
        MarkerGlyph.FIND to Triple(0f, -25f, 0f to -1f), // 26 before the find was scaled to 25 dp tall (dispatch 2026-09-28-286)
        MarkerGlyph.PHOTO to Triple(0f, -11f, 0f to -1f),
    )

    /**
     * The pixel [dxDp], [dyDp] from the anchor; transparent when that lies outside the image.
     *
     * The test's own [density], named explicitly: inside a `Bitmap` extension a bare `density` is the
     * receiver's `Bitmap.getDensity()` (a dpi, 160 and up), which put every probe off the image, so the
     * ring assertion read 0 and the two "nothing there" assertions passed without reading a pixel.
     */
    private fun Bitmap.at(image: GlyphImage, dxDp: Float, dyDp: Float): Int {
        val px = (image.anchorXPx + dxDp * this@JournalHaloGlyphTest.density).roundToInt()
        val py = (image.anchorYPx + dyDp * this@JournalHaloGlyphTest.density).roundToInt()
        return if (px in 0 until width && py in 0 until height) getPixel(px, py) else 0
    }

    @Test
    fun `each halo image is anchored at its exact centre, like the marker it sits under, and its padding holds the whole halo`() {
        edges.keys.forEach { glyph ->
            val image = drawGlyphHalo(glyph, density, halo)
            assertTrue("$glyph halo has a real size (${image.bitmap.width}x${image.bitmap.height})", image.bitmap.width > 2 && image.bitmap.height > 2)
            assertEquals("$glyph width is even", 0, image.bitmap.width % 2)
            assertEquals("$glyph height is even", 0, image.bitmap.height % 2)
            assertEquals("$glyph anchor x", image.bitmap.width / 2, image.anchorXPx)
            assertEquals("$glyph anchor y", image.bitmap.height / 2, image.anchorYPx)
            val bitmap = image.bitmap
            val border = (0 until bitmap.width).flatMap { listOf(it to 0, it to bitmap.height - 1) } +
                (0 until bitmap.height).flatMap { listOf(0 to it, bitmap.width - 1 to it) }
            assertEquals("$glyph: border pixels drawn", 0, border.count { (px, py) -> Color.alpha(bitmap.getPixel(px, py)) != 0 })
        }
    }

    @Test
    fun `beyond the marker's casing, and only there, the halo shows a ring of its own colour`() {
        edges.forEach { (glyph, edge) ->
            val (x, y, dir) = edge
            val marker = drawGlyph(glyph, density, fill, casing)
            val haloImage = drawGlyphHalo(glyph, density, halo)
            // Halfway across the ring: past the marker's casing, inside the halo's reach.
            val ring = CASING_WIDTH_DP + JOURNAL_HALO_WIDTH_DP / 2
            val rx = x + dir.first * ring
            val ry = y + dir.second * ring
            assertEquals("$glyph: the marker draws nothing there", 0, Color.alpha(marker.bitmap.at(marker, rx, ry)))
            assertEquals("$glyph: the halo draws its colour there", halo, haloImage.bitmap.at(haloImage, rx, ry))
            // Past the halo's reach: transparent.
            val beyond = CASING_WIDTH_DP + JOURNAL_HALO_WIDTH_DP + 1f
            assertEquals(
                "$glyph: nothing past the halo",
                0,
                Color.alpha(haloImage.bitmap.at(haloImage, x + dir.first * beyond, y + dir.second * beyond)),
            )
        }
    }

    @Test
    fun `each halo icon is its record's glyph drawn as a halo in the journal-entry colour, day and night`() {
        val halos = mapOf(
            MarkerIcon.WAYPOINT_JOURNAL_HALO to MarkerGlyph.WAYPOINT,
            MarkerIcon.FIND_JOURNAL_HALO to MarkerGlyph.FIND,
            MarkerIcon.PHOTO_JOURNAL_HALO to MarkerGlyph.PHOTO,
        )
        assertEquals(MarkerIcon.entries.filter { it.halo }.toSet(), halos.keys)
        for (palette in listOf(MapPalette.DAY, MapPalette.NIGHT)) {
            for ((icon, glyph) in halos) {
                assertEquals("$icon glyph", glyph, icon.glyph)
                val image = markerIconImage(icon, palette, density)
                assertTrue("$icon is the halo of its glyph in ${palette.journalEntry}", image.bitmap.sameAs(drawGlyphHalo(glyph, density, palette.journalEntry).bitmap))
            }
        }
    }
}
