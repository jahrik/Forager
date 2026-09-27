package com.zynergylabs.forager.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Holds [MapPalette] to the swatch board's constraints, measured against the ground the markers are
 * actually drawn on (`docs/audits/2026-09-27-marker-swatch-board.md`), in both modes:
 *
 *  - **(a)** each fill's Oklab ΔE from every ground cluster of 0.5% share or more, both basemaps of
 *    the mode (board threshold 0.15);
 *  - **(c)** each fill's WCAG contrast against its own casing (threshold 3:1);
 *  - **(d)** the smallest Oklab ΔE between any two of the 11 fill roles in one mode (threshold 0.10);
 *  - **(e)** the Oklab hue difference between a role's day and night fills (threshold 30°), where both
 *    are chromatic.
 *
 * **Ratchets, not bars.** Every figure is pinned at its measured value, floored to the board's three
 * decimals, and asserted not to get worse; the threshold and the margin sit beside each pin. A
 * threshold a pinned value fails is not asserted (planner ruling, planner-log line 2488, quoted in
 * `RECORD.md` intent `2026-09-27-27`). The values below the board's thresholds are all recorded:
 *
 *  - night [MapPalette.searchCentre] `#DEDEDE`, (a) 0.137 and (d) 0.099, and night
 *    [MapPalette.offlineRegion] `#FFFFFF`, (d) 0.099: the swatch board's "Roles that cannot meet every
 *    constraint";
 *  - the three owner overrides, which are not board candidates: the sighting-dot fill (night `#8C8C8C`,
 *    (a) 0.097), the sighting ring ([MapPalette.sightingDotStroke], white, a casing, so it has no
 *    pin of its own) and the selected ring ([MapPalette.sightingDotStrokeSelected], `#2196F3`, night (a)
 *    0.143 and (c) 1.076 against the grey dot). Their pins are owner-set ratchets. The day dot
 *    `#2B2B2B` is the planner's pick under the owner's "a near black color" (same ruling).
 *
 * **(b) is not asserted**: the casing against the full ground range (P10/P50/P90) fails for every
 * casing polarity in both modes (board §4, "No casing polarity passes (b) at every percentile in
 * either mode"). No single casing colour can meet it, so a pin there would pin a known failure of the
 * direction rather than of this palette.
 *
 * **The two night contrast tests removed earlier** (`docs/plans/contrast_assertions.md`, "What they
 * assert": a night fill at 4.0:1 against the night tile reference, and night's weakest fill no worse
 * than day's) **do not return.** A measured dark reference now exists (the V1 ground below), but it
 * does not support them: against the night ground's P50 luminance the night fills measure 1.24:1
 * (centre pin) to 5.85:1 (offline region), and night's weakest (1.24) is below day's weakest (1.84,
 * the selected ring). The palette separates marks from the ground by Oklab distance, (a), not by a
 * luminance ratio, and the casing carries luminance separation.
 *
 * ## What this does not establish
 *
 * Legibility on a real screen. The ground is 18 tiles per basemap from one forest-heavy area (board §8),
 * and the figures are colour arithmetic, not a perception test. Device checks are deferred to beta.
 */
class MapPaletteTest {

    /** One role's pinned figures in one mode: (a) ground distance, (c) contrast with its casing, (d) nearest other role. */
    private data class Pin(val role: String, val a: Double, val c: Double, val d: Double)

    // Comments give each figure's margin against the board's threshold: (a)/0.15 − 1, (c)/3 − 1,
    // (d)/0.10 − 1. A negative margin is a recorded shortfall (see the class doc), pinned, not asserted
    // against its threshold.
    private val dayPins = listOf(
        Pin("waypoint", a = 0.359, c = 15.648, d = 0.140), // (a) +1.393, (c) +4.216, (d) +0.400
        Pin("find", a = 0.307, c = 4.556, d = 0.145), // (a) +1.047, (c) +0.519, (d) +0.450
        Pin("plannedTrip", a = 0.215, c = 5.183, d = 0.145), // (a) +0.433, (c) +0.728, (d) +0.450
        Pin("photo", a = 0.217, c = 6.021, d = 0.152), // (a) +0.447, (c) +1.007, (d) +0.520
        Pin("keptTrack", a = 0.361, c = 5.122, d = 0.150), // (a) +1.407, (c) +0.707, (d) +0.500
        Pin("breadcrumb", a = 0.340, c = 9.391, d = 0.157), // (a) +1.267, (c) +2.130, (d) +0.570
        Pin("centrePin", a = 0.273, c = 10.093, d = 0.153), // (a) +0.820, (c) +2.364, (d) +0.530
        Pin("searchCentre", a = 0.545, c = 21.000, d = 0.149), // (a) +2.633, (c) +6.000, (d) +0.490
        Pin("offlineRegion", a = 0.400, c = 19.682, d = 0.139), // (a) +1.667, (c) +5.561, (d) +0.390
        // Owner override (near black; #2B2B2B is the planner's pick). (c) is against the white ring.
        Pin("sightingDot", a = 0.268, c = 14.159, d = 0.139), // (a) +0.787, (c) +3.720, (d) +0.390
        // Owner override. (c) is against the dot fill it rings (the ring is its own casing).
        Pin("sightingDotStrokeSelected", a = 0.231, c = 4.531, d = 0.210), // (a) +0.540, (c) +0.510, (d) +1.100
    )

    private val nightPins = listOf(
        Pin("waypoint", a = 0.214, c = 7.339, d = 0.137), // (a) +0.427, (c) +1.446, (d) +0.370
        Pin("find", a = 0.213, c = 7.897, d = 0.143), // (a) +0.420, (c) +1.632, (d) +0.430
        Pin("plannedTrip", a = 0.335, c = 6.113, d = 0.148), // (a) +1.233, (c) +1.038, (d) +0.480
        Pin("photo", a = 0.263, c = 4.669, d = 0.143), // (a) +0.753, (c) +0.556, (d) +0.430
        Pin("keptTrack", a = 0.230, c = 11.594, d = 0.137), // (a) +0.533, (c) +2.865, (d) +0.370
        Pin("breadcrumb", a = 0.205, c = 4.551, d = 0.143), // (a) +0.367, (c) +0.517, (d) +0.430
        Pin("centrePin", a = 0.218, c = 4.460, d = 0.148), // (a) +0.453, (c) +0.487, (d) +0.480
        // Board-recorded shortfall: (a) 0.137 against Topo night #C2D076 and (d) 0.099 against the
        // offline region (board §5, "Roles that cannot meet every constraint").
        Pin("searchCentre", a = 0.137, c = 15.609, d = 0.099), // (a) -0.087, (c) +4.203, (d) -0.010
        // Board-recorded shortfall: (d) 0.099 against the search centre (same section).
        Pin("offlineRegion", a = 0.208, c = 21.000, d = 0.099), // (a) +0.387, (c) +6.000, (d) -0.010
        // Owner override ("a mute grey color"): (a) 0.097 on the V1 ground, which the white ring separates.
        Pin("sightingDot", a = 0.097, c = 3.362, d = 0.155), // (a) -0.353, (c) +0.121, (d) +0.550
        // Owner override: the blue ring on the grey dot, 1.076:1 (glyph board §5 records it).
        Pin("sightingDotStrokeSelected", a = 0.143, c = 1.076, d = 0.169), // (a) -0.047, (c) -0.641, (d) +0.690
    )

    /**
     * Oklab hue difference, day against night, per chromatic role, pinned at its measured value rounded
     * up to 0.1° and asserted not to grow. Threshold 30°; margin (30 − Δh)/30 beside each. The search
     * centre, offline region and sighting dot are achromatic in both modes (C < 0.02), so (e) does not
     * apply to them (board §2).
     */
    private val hueDriftPins = mapOf(
        "waypoint" to 4.7, // +0.845
        "find" to 14.8, // +0.507
        "plannedTrip" to 15.1, // +0.497
        "photo" to 5.3, // +0.823
        "keptTrack" to 15.1, // +0.497
        "breadcrumb" to 10.1, // +0.663
        "centrePin" to 14.8, // +0.507
        "sightingDotStrokeSelected" to 0.0, // +1.000, one colour in both modes
    )

    /**
     * The measured ground: every cluster of 0.5% share or more, Topo then Street, from the swatch
     * board's §3 tables (18 tiles per basemap, z13 and z14). Night is the V1 transform of the same day
     * pixels, clustered independently.
     */
    private val dayGround = listOf(
        0xFFCFE193, 0xFFE4F7AF, 0xFFBBCD7C, 0xFFA8BA66, 0xFF96A34F, 0xFFE7D899, 0xFF7D8B35, 0xFF68751D,
        0xFFF6F9C5, 0xFFFBFBFA, 0xFFF0E7B1, 0xFFCBB76B, 0xFFDDC682, 0xFF8A7833, 0xFFB1A961, 0xFFA78A41,
        0xFFD2A65F, 0xFFE5E9E1, 0xFF918E47,
        0xFFADD19E, 0xFF99BA86, 0xFFAED4DD, 0xFFDCE2DC, 0xFFC6D9B3, 0xFFFCFCFB, 0xFFC6DCD3,
    ).map { it.toInt() }

    private val nightGround = listOf(
        0xFF4E6012, 0xFF627524, 0xFF334801, 0xFF798936, 0xFF879A45, 0xFF9CA754, 0xFF59490A, 0xFF465208,
        0xFF2A2D01, 0xFFAEBC65, 0xFFC2D076, 0xFF666319, 0xFF323701, 0xFF4B3A04, 0xFFA49047, 0xFF7F752A,
        0xFF020302, 0xFFBEA964, 0xFF2E5CFA,
        0xFF446835, 0xFF5C7D4A, 0xFF173E48, 0xFF3C512B, 0xFF1F342E, 0xFF22201C, 0xFF537342,
    ).map { it.toInt() }

    /** The 11 fill roles (d) compares; the sighting ring and the casing are casings, not roles. */
    private fun fills(p: MapPalette) = mapOf(
        "waypoint" to p.waypoint,
        "find" to p.find,
        "plannedTrip" to p.plannedTrip,
        "photo" to p.photo,
        "keptTrack" to p.keptTrack,
        "breadcrumb" to p.breadcrumb,
        "centrePin" to p.centrePin,
        "searchCentre" to p.searchCentre,
        "offlineRegion" to p.offlineRegion,
        "sightingDot" to p.sightingDot,
        "sightingDotStrokeSelected" to p.sightingDotStrokeSelected,
    )

    /** What each fill is drawn against for (c): the dot its ring, the selected ring the dot, the rest the casing. */
    private fun casingOf(role: String, p: MapPalette): Int = when (role) {
        "sightingDot" -> p.sightingDotStroke
        "sightingDotStrokeSelected" -> p.sightingDot
        else -> p.casing
    }

    @Test
    fun `every role holds the owner's colour, day and night`() {
        val expected = mapOf(
            "waypoint" to (0xFF350560 to 0xFFB97DF7),
            "find" to (0xFFDA02AF to 0xFFF96FAC),
            "plannedTrip" to (0xFF9553A4 to 0xFFFA01DD),
            "photo" to (0xFFC1154F to 0xFFE8046D),
            "keptTrack" to (0xFFA122F8 to 0xFFEEA7FE),
            "breadcrumb" to (0xFF650BB1 to 0xFFB228F8),
            "centrePin" to (0xFF7D0D5B to 0xFFA656A0),
            "searchCentre" to (0xFF000000 to 0xFFDEDEDE),
            "offlineRegion" to (0xFF0B0B0B to 0xFFFFFFFF),
            "sightingDot" to (0xFF2B2B2B to 0xFF8C8C8C),
            "sightingDotStrokeSelected" to (0xFF2196F3 to 0xFF2196F3),
        )
        for ((role, pair) in expected) {
            assertEquals("DAY.$role", hex(pair.first.toInt()), hex(fills(MapPalette.DAY).getValue(role)))
            assertEquals("NIGHT.$role", hex(pair.second.toInt()), hex(fills(MapPalette.NIGHT).getValue(role)))
        }
        assertEquals("DAY.sightingDotStroke", "#FFFFFF", hex(MapPalette.DAY.sightingDotStroke))
        assertEquals("NIGHT.sightingDotStroke", "#FFFFFF", hex(MapPalette.NIGHT.sightingDotStroke))
        assertEquals("DAY.casing", "#FFFFFF", hex(MapPalette.DAY.casing))
        assertEquals("NIGHT.casing", "#000000", hex(MapPalette.NIGHT.casing))
    }

    @Test
    fun `(a) day fills keep their measured distance from the day ground`() =
        checkGround("DAY", MapPalette.DAY, dayGround, dayPins)

    @Test
    fun `(a) night fills keep their measured distance from the V1 night ground`() =
        checkGround("NIGHT", MapPalette.NIGHT, nightGround, nightPins)

    @Test
    fun `(c) day fills keep their measured contrast with their casing`() = checkCasing("DAY", MapPalette.DAY, dayPins)

    @Test
    fun `(c) night fills keep their measured contrast with their casing`() = checkCasing("NIGHT", MapPalette.NIGHT, nightPins)

    @Test
    fun `(d) day roles keep their measured separation from each other`() = checkSeparation("DAY", MapPalette.DAY, dayPins)

    @Test
    fun `(d) night roles keep their measured separation from each other`() =
        checkSeparation("NIGHT", MapPalette.NIGHT, nightPins)

    @Test
    fun `(e) each role's night fill stays in its day fill's hue family`() {
        val failures = mutableListOf<String>()
        val day = fills(MapPalette.DAY)
        val night = fills(MapPalette.NIGHT)
        for ((role, dayArgb) in day) {
            val nightArgb = night.getValue(role)
            val chromatic = chroma(dayArgb) >= 0.02 && chroma(nightArgb) >= 0.02
            val pin = hueDriftPins[role]
            if (!chromatic) {
                if (pin != null) failures += "$role is pinned for (e) but is achromatic in a mode"
                continue
            }
            if (pin == null) {
                failures += "$role is chromatic in both modes but has no (e) pin"
                continue
            }
            val drift = hueDifference(hue(dayArgb), hue(nightArgb))
            if (drift > pin) failures += "%s: day/night hue differ by %.2f°, pinned at %.1f°".format(role, drift, pin)
        }
        if (failures.isNotEmpty()) fail("Hue family:\n  " + failures.joinToString("\n  "))
    }

    @Test
    fun `forMode selects the palettes, and the two are not the same object`() {
        assertNotEquals(MapPalette.DAY, MapPalette.NIGHT)
        if (MapPalette.forMode(night = false) != MapPalette.DAY) fail("forMode(false) must be DAY")
        if (MapPalette.forMode(night = true) != MapPalette.NIGHT) fail("forMode(true) must be NIGHT")
    }

    /**
     * A guard on the guards: the maths reproduces the board's own figures, so a broken calculation
     * returning a large constant cannot pass the ratchets above silently.
     */
    @Test
    fun `the contrast, distance and hue calculations reproduce the swatch board's figures`() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        assertEquals(21.0, contrastRatio(black, white), 0.01)
        assertEquals(1.0, contrastRatio(white, white), 1e-4)
        assertEquals(0.0, oklabDistance(white, white), 1e-9)
        // Board §5, day waypoint: (a) 0.359 against Topo #68751D; (c) 15.65 against white.
        assertEquals(0.3595, oklabDistance(0xFF350560.toInt(), 0xFF68751D.toInt()), 5e-4)
        assertEquals(15.65, contrastRatio(0xFF350560.toInt(), white), 0.01)
        // Board §5, waypoint (e): 4.7° between #350560 and #B97DF7; hue of #350560 is 300.3°.
        assertEquals(300.3, hue(0xFF350560.toInt()), 0.05)
        assertEquals(4.7, hueDifference(hue(0xFF350560.toInt()), hue(0xFFB97DF7.toInt())), 0.05)
    }

    private fun checkGround(mode: String, palette: MapPalette, ground: List<Int>, pins: List<Pin>) {
        val failures = mutableListOf<String>()
        val roles = fills(palette)
        assertEquals("every fill role is pinned", roles.keys, pins.map { it.role }.toSet())
        for (pin in pins) {
            val argb = roles.getValue(pin.role)
            val (nearest, distance) = ground.map { it to oklabDistance(argb, it) }.minBy { it.second }
            if (floor3(distance) < pin.a) {
                failures += "%s.%s is %.4f from ground %s, pinned at %.3f".format(mode, pin.role, distance, hex(nearest), pin.a)
            }
        }
        if (failures.isNotEmpty()) fail("Ground distance (a):\n  " + failures.joinToString("\n  "))
    }

    private fun checkCasing(mode: String, palette: MapPalette, pins: List<Pin>) {
        val failures = mutableListOf<String>()
        val roles = fills(palette)
        for (pin in pins) {
            val casing = casingOf(pin.role, palette)
            val ratio = contrastRatio(roles.getValue(pin.role), casing)
            if (floor3(ratio) < pin.c) {
                failures += "%s.%s is %.4f:1 against %s, pinned at %.3f".format(mode, pin.role, ratio, hex(casing), pin.c)
            }
        }
        if (failures.isNotEmpty()) fail("Casing contrast (c):\n  " + failures.joinToString("\n  "))
    }

    private fun checkSeparation(mode: String, palette: MapPalette, pins: List<Pin>) {
        val failures = mutableListOf<String>()
        val roles = fills(palette)
        for (pin in pins) {
            val argb = roles.getValue(pin.role)
            val (nearest, distance) = roles.filterKeys { it != pin.role }
                .map { (other, otherArgb) -> other to oklabDistance(argb, otherArgb) }
                .minBy { it.second }
            if (floor3(distance) < pin.d) {
                failures += "%s.%s is %.4f from %s, pinned at %.3f".format(mode, pin.role, distance, nearest, pin.d)
            }
        }
        if (failures.isNotEmpty()) fail("Role separation (d):\n  " + failures.joinToString("\n  "))
    }

    private fun floor3(v: Double) = floor(v * 1000) / 1000

    private fun hex(argb: Int) = "#%06X".format(argb and 0xFFFFFF)

    private fun relativeLuminance(argb: Int): Double {
        fun channel(c: Double) = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        return 0.2126 * channel(((argb shr 16) and 0xFF) / 255.0) +
            0.7152 * channel(((argb shr 8) and 0xFF) / 255.0) +
            0.0722 * channel((argb and 0xFF) / 255.0)
    }

    private fun contrastRatio(a: Int, b: Int): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun oklabDistance(a: Int, b: Int): Double {
        val x = oklab(a)
        val y = oklab(b)
        return sqrt((0..2).sumOf { (x[it] - y[it]).pow(2) })
    }

    private fun chroma(argb: Int): Double = oklab(argb).let { sqrt(it[1] * it[1] + it[2] * it[2]) }

    private fun hue(argb: Int): Double = oklab(argb).let { (Math.toDegrees(atan2(it[2], it[1])) + 360.0) % 360.0 }

    private fun hueDifference(h1: Double, h2: Double): Double = abs(h1 - h2).rem(360.0).let { minOf(it, 360.0 - it) }

    /** Björn Ottosson's Oklab, the same constants as the swatch board's `colour.py`. */
    private fun oklab(argb: Int): DoubleArray {
        fun toLinear(c: Double) = if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        val r = toLinear(((argb shr 16) and 0xFF) / 255.0)
        val g = toLinear(((argb shr 8) and 0xFF) / 255.0)
        val b = toLinear((argb and 0xFF) / 255.0)
        val l = cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
        val m = cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
        val s = cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)
        return doubleArrayOf(
            0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
            1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
            0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s,
        )
    }
}
