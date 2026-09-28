package com.zynergylabs.forager.app.ui.theme

import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.OFFLINE_REGION_FILL_OPACITY
import com.zynergylabs.forager.app.ui.map.layers.OpacityProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Holds [MapPalette] to the swatch board's constraints, measured against the ground the markers are
 * actually drawn on (`docs/audits/2026-09-27-marker-swatch-board.md`), in both modes:
 *
 *  - **(a)** each fill's Oklab ΔE from every ground cluster of 0.5% share or more, both basemaps of
 *    the mode (board threshold 0.15);
 *  - **(c)** each fill's WCAG contrast against its own casing (threshold 3:1);
 *  - **(d)** the smallest Oklab ΔE between any two of the 12 fill roles in one mode (threshold 0.10);
 *  - **(e)** the Oklab hue difference between a role's day and night fills (threshold 30°), where both
 *    are chromatic.
 *
 * **Ratchets, not bars.** Every figure is pinned at its measured value, floored to the board's three
 * decimals, and asserted not to get worse; the threshold and the margin sit beside each pin. A
 * threshold a pinned value fails is not asserted (planner ruling, planner-log line 2488, quoted in
 * `RECORD.md` intent `2026-09-27-27`). The values below the board's thresholds are all recorded:
 *
 *  - night [MapPalette.searchCentre] `#DEDEDE`, (a) 0.137 and (d) 0.099: the swatch board's "Roles
 *    that cannot meet every constraint". The board's night [MapPalette.offlineRegion] `#FFFFFF`, (d)
 *    0.099, was the other half of that (d) pair; the owner's `#202020` (2026-09-28) replaced it, and
 *    its own (d) is 0.360;
 *  - **night [MapPalette.offlineRegion] is not held to (a) or (c)** (owner, 2026-09-28: "Test it as
 *    drawn (Recommended)", dispatch `2026-09-28-22`, re-scoped by `2026-09-28-24`). It is not a solid
 *    mark: it is drawn at [OFFLINE_REGION_FILL_OPACITY] over the ground, under a black dashed casing,
 *    so its solid colour's distance from the ground and contrast with the casing describe nothing
 *    that is drawn. Its night pin carries (d) only. Its as-drawn figures are recorded instead, per
 *    ground cluster, in [nightOfflineRegionAsDrawn]; they are pins, not bars, and gate nothing beyond
 *    matching. The owner chose the night shade, `#202020`, from phone screenshots ("You pick from phone
 *    shots (Recommended)", then "That's my pick."). The day offline region keeps
 *    (a) and (c) unchanged;
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
 * **J8's journal-entry halo** ([MapPalette.journalEntry], day `#005577`, night `#00DDFF`) is the
 * twelfth role, proposed by the J8 coder and measured here like the others: a grid search of sRGB in
 * steps of 17 for the pair with the widest least margin over (a), (c) and (d), with (e) under 30°,
 * and with the new colour farther from every existing role than that role's own pinned (d), so no
 * existing pin moved. Its (c) is against the casing because the halo is drawn around the record's
 * own casing. The owner judges the pair on the phone.
 *
 * **The night outline's border** (`prompts/preserved/2026-09-28-79.md`; owner: "The outline should have
 * a white border", "Yes the night outline only") is a line under the offline region's dashed outline at
 * night, white at its registry opacity, so that the edge carries over the darkest night ground, where
 * the black dashes alone measured 1.08:1 to 1.45:1 on the phone (Part 1, check 5 (b)). It is not a fill
 * role and has no (a) to (e) pin. Its figures are held the same way as the rest: a ratchet per night
 * ground cluster in [nightOfflineBorderPins], and the dispatch's target, 3:1 over Part 1's darkest night
 * ground ([part1DarkestNightGround]), asserted as a bar because it passes. The coder proposed the
 * values; the owner judges them on the phone.
 *
 * ## What this does not establish
 *
 * Legibility on a real screen. The ground is 18 tiles per basemap from one forest-heavy area (board §8),
 * and the figures are colour arithmetic, not a perception test. Device checks are deferred to beta.
 */
class MapPaletteTest {

    /**
     * One role's pinned figures in one mode: (a) ground distance, (c) contrast with its casing, (d)
     * nearest other role. A null (a) or (c) means the role is not held to that check in that mode
     * (night offline region only; see the class doc).
     */
    private data class Pin(val role: String, val a: Double?, val c: Double?, val d: Double)

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
        // J8's halo, #005577. (d) is against the sighting dot; (c) against the white casing it surrounds.
        Pin("journalEntry", a = 0.202, c = 8.184, d = 0.161), // (a) +0.347, (c) +1.728, (d) +0.610
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
        // The owner's #202020 (2026-09-28); (d) is against the centre pin. At the board's #FFFFFF it was
        // 0.099 against the search centre, a board-recorded shortfall. Not held to (a) or (c): it is
        // drawn translucent, so it is checked as drawn instead (owner's re-scoping, 2026-09-28; see the
        // class doc and [nightOfflineRegionAsDrawn]).
        Pin("offlineRegion", a = null, c = null, d = 0.360), // (d) +2.600
        // Owner override ("a mute grey color"): (a) 0.097 on the V1 ground, which the white ring separates.
        Pin("sightingDot", a = 0.097, c = 3.362, d = 0.155), // (a) -0.353, (c) +0.121, (d) +0.550
        // Owner override: the blue ring on the grey dot, 1.076:1 (glyph board §5 records it).
        Pin("sightingDotStrokeSelected", a = 0.143, c = 1.076, d = 0.169), // (a) -0.047, (c) -0.641, (d) +0.690
        // J8's halo, #00DDFF. (d) is against the search centre; (c) against the black casing it surrounds.
        Pin("journalEntry", a = 0.197, c = 12.786, d = 0.163), // (a) +0.313, (c) +3.262, (d) +0.630
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
        "journalEntry" to 20.1, // +0.330
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

    /**
     * One night ground cluster's as-drawn figures for the offline region: the composited region's
     * Oklab ΔE from the plain ground, and the black casing's WCAG contrast over the composite and over
     * the plain ground. Each is floored to three decimals, like the pins above.
     */
    private data class AsDrawnPin(
        val ground: Long,
        val deltaE: Double,
        val casingOverRegion: Double,
        val casingOverGround: Double,
    )

    /**
     * Night [MapPalette.offlineRegion] `#202020` as drawn, over every [nightGround] cluster in its
     * order. Recorded, not gated (owner's re-scoping, 2026-09-28): there is no threshold here, only
     * the requirement that the figures match, so that a change to the fill, its opacity or the casing
     * shows up as a failing pin rather than passing silently. Re-measured for `#202020`, the owner's
     * pick, from the `#FFFFFF` figures first pinned here; a later shade is re-measured the same way.
     *
     * At `#202020`, the composite sits under 0.02 ΔE from the plain ground on `#2A2D01` (0.014),
     * `#1F342E` (0.013) and `#22201C` (0.001, where the fill all but vanishes), and the casing is
     * below 3:1 over the composite on 14 of the 26 clusters (lowest `#020302`, 1.052). These are
     * recorded, not asserted against any threshold. The casing-over-ground figures do not depend on
     * the fill and are unchanged from `#FFFFFF`.
     */
    private val nightOfflineRegionAsDrawn = listOf(
        AsDrawnPin(0xFF4E6012, deltaE = 0.044, casingOverRegion = 2.501, casingOverGround = 3.005),
        AsDrawnPin(0xFF627524, deltaE = 0.056, casingOverRegion = 3.233, casingOverGround = 4.089),
        AsDrawnPin(0xFF334801, deltaE = 0.028, casingOverRegion = 1.857, casingOverGround = 2.068),
        AsDrawnPin(0xFF798936, deltaE = 0.068, casingOverRegion = 4.120, casingOverGround = 5.444),
        AsDrawnPin(0xFF879A45, deltaE = 0.077, casingOverRegion = 4.978, casingOverGround = 6.738),
        AsDrawnPin(0xFF9CA754, deltaE = 0.086, casingOverRegion = 5.815, casingOverGround = 8.069),
        AsDrawnPin(0xFF59490A, deltaE = 0.033, casingOverRegion = 2.086, casingOverGround = 2.382),
        AsDrawnPin(0xFF465208, deltaE = 0.035, casingOverRegion = 2.137, casingOverGround = 2.470),
        AsDrawnPin(0xFF2A2D01, deltaE = 0.014, casingOverRegion = 1.424, casingOverGround = 1.474),
        AsDrawnPin(0xFFAEBC65, deltaE = 0.096, casingOverRegion = 7.182, casingOverGround = 10.180),
        AsDrawnPin(0xFFC2D076, deltaE = 0.106, casingOverRegion = 8.701, casingOverGround = 12.577),
        AsDrawnPin(0xFF666319, deltaE = 0.047, casingOverRegion = 2.760, casingOverGround = 3.363),
        AsDrawnPin(0xFF323701, deltaE = 0.020, casingOverRegion = 1.575, casingOverGround = 1.682),
        AsDrawnPin(0xFF4B3A04, deltaE = 0.024, casingOverRegion = 1.745, casingOverGround = 1.906),
        AsDrawnPin(0xFFA49047, deltaE = 0.076, casingOverRegion = 4.936, casingOverGround = 6.658),
        AsDrawnPin(0xFF7F752A, deltaE = 0.060, casingOverRegion = 3.491, casingOverGround = 4.480),
        AsDrawnPin(0xFF020302, deltaE = 0.045, casingOverRegion = 1.052, casingOverGround = 1.016),
        AsDrawnPin(0xFFBEA964, deltaE = 0.091, casingOverRegion = 6.457, casingOverGround = 9.048),
        AsDrawnPin(0xFF2E5CFA, deltaE = 0.073, casingOverRegion = 3.141, casingOverGround = 4.027),
        AsDrawnPin(0xFF446835, deltaE = 0.045, casingOverRegion = 2.705, casingOverGround = 3.277),
        AsDrawnPin(0xFF5C7D4A, deltaE = 0.060, casingOverRegion = 3.481, casingOverGround = 4.487),
        AsDrawnPin(0xFF173E48, deltaE = 0.021, casingOverRegion = 1.681, casingOverGround = 1.819),
        AsDrawnPin(0xFF3C512B, deltaE = 0.034, casingOverRegion = 2.090, casingOverGround = 2.403),
        AsDrawnPin(0xFF1F342E, deltaE = 0.013, casingOverRegion = 1.515, casingOverGround = 1.588),
        AsDrawnPin(0xFF22201C, deltaE = 0.001, casingOverRegion = 1.292, casingOverGround = 1.291),
        AsDrawnPin(0xFF537342, deltaE = 0.054, casingOverRegion = 3.093, casingOverGround = 3.898),
    )

    /**
     * Part 1's darkest night ground (`docs/audits/2026-09-28-stage-device-check-part-1-run-record.md`,
     * check 5 (b)), read from its overlay-off frames (`272-topo-night-off.png`, `281-street-night-off.png`,
     * outside the repository) along its fitted edge circle, centre (600.5, 1255.7), radius 426.6 px:
     * `#0E0D0A`, the mean plain ground 7 to 13 px outside the edge in the darkest 10-degree bin
     * (Topographical, 70 to 80 degrees, relative luminance 0.0061, the run record's "0.006"); and
     * `#010101`, the darkest single pixel within 13 px of the edge on either basemap. The border's
     * target is 3:1 against both (dispatch `2026-09-28-79`).
     */
    private val part1DarkestNightGround = listOf(0xFF0E0D0A, 0xFF010101).map { it.toInt() }

    /**
     * One night ground cluster's figures for the offline outline's border, each floored to three
     * decimals: the border composited over the plain ground outside the edge, as its WCAG contrast with
     * that ground ([outside]); the same over the region's composite inside the edge ([inside]); and the
     * black dash against the border, on the weaker of the two sides ([dash]).
     */
    private data class BorderPin(val ground: Long, val outside: Double, val inside: Double, val dash: Double)

    /** One ground's border figures as measured, unfloored: [BorderPin]'s three, before the pin. */
    private data class BorderFigures(val outside: Double, val inside: Double, val dash: Double)

    /**
     * The night border at `#FFFFFF` and line opacity 0.85 (the coder's proposal, dispatch
     * `2026-09-28-79`), over every [nightGround] cluster in its order, with the fill at `#202020` and
     * [OFFLINE_REGION_FILL_OPACITY] 0.2. Ratchets: each is asserted not to get worse. The border is
     * under 3:1 against its ground on the six lightest clusters (lowest `#C2D076`, 1.553 outside), where
     * the black dashes are 6.6:1 or more against the plain ground (`nightOfflineRegionAsDrawn`). Over the
     * darkest cluster, `#020302`, it is 14.636 outside and 14.267 inside, and over `#22201C`, where the
     * fill all but vanishes, 12.078 and 12.069. The dash is 14.877:1 or more against the border on every
     * cluster.
     */
    private val nightOfflineBorderPins = listOf(
        BorderPin(0xFF4E6012, outside = 5.573, inside = 6.599, dash = 16.507),
        BorderPin(0xFF627524, outside = 4.211, inside = 5.245, dash = 16.962),
        BorderPin(0xFF334801, outside = 7.866, inside = 8.647, dash = 16.060),
        BorderPin(0xFF798936, outside = 3.257, inside = 4.192, dash = 17.276),
        BorderPin(0xFF879A45, outside = 2.697, inside = 3.534, dash = 17.594),
        BorderPin(0xFF9CA754, outside = 2.297, inside = 3.081, dash = 17.916),
        BorderPin(0xFF59490A, outside = 6.915, inside = 7.816, dash = 16.309),
        BorderPin(0xFF465208, outside = 6.672, inside = 7.650, dash = 16.354),
        BorderPin(0xFF2A2D01, outside = 10.718, inside = 11.025, dash = 15.705),
        BorderPin(0xFFAEBC65, outside = 1.869, inside = 2.544, dash = 18.276),
        BorderPin(0xFFC2D076, outside = 1.553, inside = 2.151, dash = 18.725),
        BorderPin(0xFF666319, outside = 5.059, inside = 6.058, dash = 16.727),
        BorderPin(0xFF323701, outside = 9.474, inside = 10.056, dash = 15.844),
        BorderPin(0xFF4B3A04, outside = 8.431, inside = 9.195, dash = 16.048),
        BorderPin(0xFFA49047, outside = 2.715, inside = 3.561, dash = 17.582),
        BorderPin(0xFF7F752A, outside = 3.883, inside = 4.890, dash = 17.072),
        BorderPin(0xFF020302, outside = 14.636, inside = 14.267, dash = 14.877),
        BorderPin(0xFFBEA964, outside = 2.071, inside = 2.799, dash = 18.077),
        BorderPin(0xFF2E5CFA, outside = 4.228, inside = 5.316, dash = 16.700),
        BorderPin(0xFF446835, outside = 5.155, inside = 6.146, dash = 16.628),
        BorderPin(0xFF5C7D4A, outside = 3.903, inside = 4.910, dash = 17.096),
        BorderPin(0xFF173E48, outside = 8.818, inside = 9.483, dash = 15.942),
        BorderPin(0xFF3C512B, outside = 6.866, inside = 7.776, dash = 16.254),
        BorderPin(0xFF1F342E, outside = 10.020, inside = 10.424, dash = 15.802),
        BorderPin(0xFF22201C, outside = 12.078, inside = 12.069, dash = 15.598),
        BorderPin(0xFF537342, outside = 4.414, inside = 5.439, dash = 16.827),
    )

    /** The 12 fill roles (d) compares; the sighting ring and the casing are casings, not roles. */
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
        "journalEntry" to p.journalEntry,
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
            "offlineRegion" to (0xFF0B0B0B to 0xFF202020),
            "sightingDot" to (0xFF2B2B2B to 0xFF8C8C8C),
            "sightingDotStrokeSelected" to (0xFF2196F3 to 0xFF2196F3),
            "journalEntry" to (0xFF005577 to 0xFF00DDFF),
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
     * The night offline region as drawn over each night ground cluster: the composite's Oklab ΔE from
     * the plain ground, and the black casing's contrast over the composite and over the plain ground,
     * each matching its pin. Reads the real [OFFLINE_REGION_FILL_OPACITY], so an opacity edit moves
     * the pins too.
     */
    @Test
    fun `night offline region as drawn matches its recorded figures over every night ground cluster`() {
        assertEquals(
            "one as-drawn pin per night ground cluster, in order",
            nightGround.map { hex(it) },
            nightOfflineRegionAsDrawn.map { hex(it.ground.toInt()) },
        )
        // The composite itself: white at 20% over black is #333333 (0.2 × 255 = 51).
        assertEquals("#333333", hex(composite(0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 0.2)))
        val fill = MapPalette.NIGHT.offlineRegion
        val casing = MapPalette.NIGHT.casing
        val opacity = OFFLINE_REGION_FILL_OPACITY.toDouble()
        val failures = mutableListOf<String>()
        for (pin in nightOfflineRegionAsDrawn) {
            val ground = pin.ground.toInt()
            val region = composite(fill, ground, opacity)
            val measured = listOf(
                Triple("ΔE", oklabDistance(region, ground), pin.deltaE),
                Triple("casing over region", contrastRatio(casing, region), pin.casingOverRegion),
                Triple("casing over ground", contrastRatio(casing, ground), pin.casingOverGround),
            )
            for ((name, value, pinned) in measured) {
                if (abs(floor3(value) - pinned) > 1e-9) {
                    failures += "ground %s (region %s): %s is %.4f, pinned at %.3f".format(
                        hex(ground), hex(region), name, value, pinned,
                    )
                }
            }
        }
        if (failures.isNotEmpty()) {
            fail("Night offline region as drawn, fill ${hex(fill)} at $opacity:\n  " + failures.joinToString("\n  "))
        }
    }

    /**
     * The night outline's border (dispatch `2026-09-28-79`), as drawn: white at the border layer's
     * registry opacity over the ground either side of the edge, the plain ground outside and the
     * region's composite inside. The dispatch's target is asserted as a bar over Part 1's darkest
     * night ground: 3:1 for the border against the ground it lies on, on both sides, and 3:1 for the
     * black dashes against the border, so that they still read as dashes. Every night ground cluster's
     * figures are ratchets ([nightOfflineBorderPins]). The fill is pinned first: the border does not
     * change it (owner: "The fill color and opacity is fine as is").
     */
    @Test
    fun `the night outline border holds 3 to 1 over Part 1's darkest night ground, and its figures over every night ground cluster`() {
        assertEquals("NIGHT.offlineRegion is unchanged", "#202020", hex(MapPalette.NIGHT.offlineRegion))
        assertEquals("the fill's opacity is unchanged", 0.2f, OFFLINE_REGION_FILL_OPACITY, 0f)
        val spec = MAP_LAYER_REGISTRY.singleOrNull { it.id == MapLayerIds.OFFLINE_REGION_BORDER }
        assertNotNull("no border layer under the offline outline in the registry", spec)
        val border = spec!!.paletteRole!!.colourOf(MapPalette.NIGHT)
        assertEquals("the border is opaque white at night", "#FFFFFFFF", "#%08X".format(border))
        val opacity = spec.baseOpacities.single { it.property == OpacityProperty.LINE }.base.toDouble()
        val dash = MapPalette.NIGHT.casing
        val fillOpacity = OFFLINE_REGION_FILL_OPACITY.toDouble()

        fun figures(ground: Int): BorderFigures {
            val region = composite(MapPalette.NIGHT.offlineRegion, ground, fillOpacity)
            val overGround = composite(border, ground, opacity)
            val overRegion = composite(border, region, opacity)
            return BorderFigures(
                outside = contrastRatio(overGround, ground),
                inside = contrastRatio(overRegion, region),
                dash = minOf(contrastRatio(dash, overGround), contrastRatio(dash, overRegion)),
            )
        }

        val failures = mutableListOf<String>()
        for (ground in part1DarkestNightGround) {
            val f = figures(ground)
            if (f.outside < 3.0) failures += "Part 1 ground %s: border outside is %.4f:1, target 3:1".format(hex(ground), f.outside)
            if (f.inside < 3.0) failures += "Part 1 ground %s: border inside is %.4f:1, target 3:1".format(hex(ground), f.inside)
            if (f.dash < 3.0) failures += "Part 1 ground %s: dash on border is %.4f:1, target 3:1".format(hex(ground), f.dash)
        }
        assertEquals(
            "one border pin per night ground cluster, in order",
            nightGround.map { hex(it) },
            nightOfflineBorderPins.map { hex(it.ground.toInt()) },
        )
        for (pin in nightOfflineBorderPins) {
            val f = figures(pin.ground.toInt())
            listOf(
                Triple("border outside", f.outside, pin.outside),
                Triple("border inside", f.inside, pin.inside),
                Triple("dash on border", f.dash, pin.dash),
            ).forEach { (name, value, pinned) ->
                if (floor3(value) < pinned) failures += "ground %s: %s is %.4f:1, pinned at %.3f".format(hex(pin.ground.toInt()), name, value, pinned)
            }
        }
        if (failures.isNotEmpty()) {
            fail("Night outline border, ${hex(border)} at $opacity:\n  " + failures.joinToString("\n  "))
        }
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
            val pinned = pin.a ?: continue
            val argb = roles.getValue(pin.role)
            val (nearest, distance) = ground.map { it to oklabDistance(argb, it) }.minBy { it.second }
            if (floor3(distance) < pinned) {
                failures += "%s.%s is %.4f from ground %s, pinned at %.3f".format(mode, pin.role, distance, hex(nearest), pinned)
            }
        }
        if (failures.isNotEmpty()) fail("Ground distance (a):\n  " + failures.joinToString("\n  "))
    }

    private fun checkCasing(mode: String, palette: MapPalette, pins: List<Pin>) {
        val failures = mutableListOf<String>()
        val roles = fills(palette)
        for (pin in pins) {
            val pinned = pin.c ?: continue
            val casing = casingOf(pin.role, palette)
            val ratio = contrastRatio(roles.getValue(pin.role), casing)
            if (floor3(ratio) < pinned) {
                failures += "%s.%s is %.4f:1 against %s, pinned at %.3f".format(mode, pin.role, ratio, hex(casing), pinned)
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

    /**
     * [fill] at [opacity] over [ground], blended per channel in sRGB (`opacity·fill + (1 − opacity)·ground`)
     * and rounded to 8 bits. MapLibre's real blend space on the device is unverified: if it blends in
     * linear light, the drawn colours differ from these.
     */
    private fun composite(fill: Int, ground: Int, opacity: Double): Int {
        fun channel(shift: Int) =
            (opacity * ((fill shr shift) and 0xFF) + (1 - opacity) * ((ground shr shift) and 0xFF)).roundToInt()
        return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

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
