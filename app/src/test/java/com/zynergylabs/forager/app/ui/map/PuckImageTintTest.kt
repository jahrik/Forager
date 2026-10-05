package com.zynergylabs.forager.app.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.utils.BitmapUtils
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Dispatch 2026-09-28-510, found at the S22 desk step: after the approximate look had been drawn once,
 * the GPS dot stayed pale for the life of the app. MapLibre 13.5.0 builds each dot image the way
 * `LayerBitmapProvider.generateBitmap` does, `BitmapUtils.getDrawableFromRes(context, res, tint)` then
 * `getBitmapFromDrawable`, and the first calls `setTint` on the drawable it loads, with no `mutate()`
 * (read from its bytecode). A vector drawable's tint lives in the state every copy loaded from that
 * resource shares, so a tint applied once to MapLibre's own dot stays on every later copy, whatever a
 * later look asks for.
 *
 * So the dot images are built here exactly as MapLibre builds them, one look after another in one
 * process, as on the phone, and their pixels read back.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PuckImageTintTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** The foreground image MapLibre builds for [options], as `LayerBitmapProvider.generateBitmap` builds it. */
    private fun foreground(options: LocationComponentOptions): Bitmap =
        BitmapUtils.getBitmapFromDrawable(BitmapUtils.getDrawableFromRes(context, options.foregroundDrawable(), options.foregroundTintColor()))!!

    private fun Bitmap.centre(): Int = getPixel(width / 2, height / 2)

    private fun assertColourNear(what: String, rgb: Int, pixel: Int) {
        val near = listOf(16, 8, 0).all { shift -> kotlin.math.abs(((rgb shr shift) and 0xFF) - ((pixel shr shift) and 0xFF)) <= 2 }
        assertEquals("$what: ${Integer.toHexString(pixel)} within 2 a channel of ${Integer.toHexString(rgb)}", true, near)
    }

    /**
     * The second cause, read from the S22 (`ForagerDot` log, run 4): after the GPS look was applied, MapLibre's
     * style held its own opaque blue under `mapbox-location-icon` (centre ff4a90e2, 53x53), and the screen still
     * drew the approximate dot, even after a zoom. MapLibre's renderer does not redraw an image replaced by one of
     * the same size under the same name. So no look may change what MapLibre builds under its own name: every
     * look must leave it MapLibre's opaque blue.
     */
    @Test
    fun `no look changes the image MapLibre builds under its own name`() {
        PositionLook.entries.forEach { look ->
            val built = foreground(liveLocationComponentOptions(context, look = look)).centre()
            assertEquals("$look: alpha of ${Integer.toHexString(built)}", 0xFF, Color.alpha(built))
            assertEquals("$look: colour of ${Integer.toHexString(built)}", 0x4A90E2, built and 0xFFFFFF)
        }
    }

    @Test
    fun `after the approximate and last known looks, the GPS dot is MapLibre's own opaque blue again`() {
        foreground(liveLocationComponentOptions(context, look = PositionLook.APPROXIMATE))
        foreground(liveLocationComponentOptions(context, look = PositionLook.LAST_KNOWN))

        val gps = foreground(liveLocationComponentOptions(context, look = PositionLook.PRECISE)).centre()

        // maplibre_user_icon's own fill, #4A90E2, fully opaque.
        assertEquals("alpha of ${Integer.toHexString(gps)}", 0xFF, Color.alpha(gps))
        assertEquals("colour of ${Integer.toHexString(gps)}", 0x4A90E2, gps and 0xFFFFFF)
    }

    @Test
    fun `the approximate dot is see-through and the last known dot grey, both drawn without a tint`() {
        val approximate = foreground(liveLocationComponentOptions(context, look = PositionLook.APPROXIMATE)).centre()
        val lastKnown = foreground(liveLocationComponentOptions(context, look = PositionLook.LAST_KNOWN)).centre()

        assertEquals("soft: half see-through, ${Integer.toHexString(approximate)}", 0x80, Color.alpha(approximate))
        // Read back from a half-transparent pixel, a channel can round by one (#4A8FE1 for #4A90E2).
        assertColourNear("MapLibre's location blue", 0x4A90E2, approximate)
        assertEquals("opaque, ${Integer.toHexString(lastKnown)}", 0xFF, Color.alpha(lastKnown))
        assertColourNear("grey", 0xA1B0C0, lastKnown)
    }

    @Test
    fun `no look tints any of MapLibre's own images`() {
        PositionLook.entries.forEach { look ->
            val options = liveLocationComponentOptions(context, look = look)
            assertNull("$look foreground", options.foregroundTintColor())
            assertNull("$look background", options.backgroundTintColor())
            assertNull("$look bearing", options.bearingTintColor())
            assertNull("$look stale foreground", options.foregroundStaleTintColor())
            assertNull("$look stale background", options.backgroundStaleTintColor())
        }
    }
}
