package com.zynergylabs.forager.app.ui.map

import android.graphics.BitmapFactory
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Topo night stays dark at every zoom (dispatch 2026-09-28-310). The owner's S22 report: topo night
 * is dark zoomed in and goes light zoomed out. These tests run the sampled OpenTopoMap tiles
 * (`docs/audits/data/2026-09-30-topo-night/`, 3 x 3 over Oregon City at each tile zoom 7 to 15)
 * through the paint that `styleJsonFor(OPEN_TOPO_MAP, night = true)` actually produces, parsed from
 * its JSON, using [rasterShade], the headless model of the raster shader. The model's limits are
 * listed on [rasterShade]'s file; a pass here is a pass of the model on those tiles, not a device check.
 *
 * Tile level N is shown for map zoom N - 1.5 up to N - 0.5 ([rasterTileZoomFor]), and a style's
 * `["zoom"]` is the map zoom, so each tile zoom is sampled at both ends and the middle of its own
 * map-zoom range.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TopoNightZoomTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** Unique colours of every sampled tile at one tile zoom, with pixel counts. */
    private class TileSet(val tileZoom: Int, val tileCount: Int, val colours: Map<Int, Int>)

    private val tiles: Map<Int, TileSet> by lazy {
        val dir = File("../docs/audits/data/2026-09-30-topo-night/tiles")
        val files = dir.listFiles { f -> f.name.endsWith(".png") }.orEmpty()
        assertEquals("the sample is 9 tile zooms x 9 tiles; a short directory would make every check below vacuous", 81, files.size)
        files.groupBy { it.name.substringBefore('_').toInt() }.mapValues { (z, group) ->
            val counts = HashMap<Int, Int>()
            for (file in group) {
                val bitmap = BitmapFactory.decodeFile(file.path) ?: error("${file.name} did not decode")
                assertEquals("${file.name} is 256 px square", 256 to 256, bitmap.width to bitmap.height)
                val pixels = IntArray(256 * 256)
                bitmap.getPixels(pixels, 0, 256, 0, 0, 256, 256)
                for (pixel in pixels) counts.merge(pixel or (0xFF shl 24), 1, Int::plus)
            }
            TileSet(z, group.size, counts)
        }.also { byZoom ->
            assertEquals((7..15).toList(), byZoom.keys.sorted())
            byZoom.values.forEach { assertEquals("tiles at z${it.tileZoom}", 9, it.tileCount) }
            // Pillow (measure.py) counts 17,832 distinct colours across these tiles, summed per zoom; a
            // decoder that quantised, colour-managed or dropped pixels would not reproduce it.
            assertEquals("distinct colours summed per tile zoom, against Pillow's count", 17832, byZoom.values.sumOf { it.colours.size })
        }
    }

    private fun topoNightPaint(): JsonObject =
        json.parseToJsonElement(styleJsonFor(Basemap.OPEN_TOPO_MAP, night = true))
            .jsonObject.getValue("layers").jsonArray
            .single { it.jsonObject.getValue("id").jsonPrimitive.content == RASTER_LAYER_ID }
            .jsonObject.getValue("paint").jsonObject

    private fun lightness(argb: Int): Double =
        (((argb shr 16) and 0xFF) + ((argb shr 8) and 0xFF) + (argb and 0xFF)) / (3.0 * 255.0)

    /** Mean and median of the channel-mean lightness of [set]'s pixels after [transform]. */
    private fun meanAndMedian(set: TileSet, transform: (Int) -> Int): Pair<Double, Double> {
        val pixels = set.colours.map { (colour, count) -> lightness(transform(colour)) to count }.sortedBy { it.first }
        val total = pixels.sumOf { it.second }.toDouble()
        val mean = pixels.sumOf { it.first * it.second } / total
        var seen = 0
        val median = pixels.first { seen += it.second; seen >= total / 2 }.first
        return mean to median
    }

    /** Day pixels that are a label: near-neutral and dark (black text and its dark halo shadow). */
    private fun isLabelPixel(day: Int): Boolean {
        val r = (day shr 16) and 0xFF
        val g = (day shr 8) and 0xFF
        val b = day and 0xFF
        return maxOf(r, g, b) - minOf(r, g, b) < 20 && (r + g + b) / 3 < 64
    }

    /** The map zooms at which tile level [z] is on screen: both ends of its range, and its middle. */
    private fun mapZoomsShowing(z: Int): List<Double> =
        listOf(z - 1.5, z - 1.0, z - 0.5 - 1e-6).also { zooms ->
            zooms.forEach { assertEquals("map zoom $it should show tile level $z", z, rasterTileZoomFor(it)) }
        }

    private class Band(val low: Double, val high: Double) {
        fun contains(v: Double) = v in low..high
        override fun toString() = "[%.3f, %.3f]".format(low, high)
    }

    /**
     * Today's zoomed-in night, from the tiles at zoom 11 to 15 through V1 ([nightColorOf], not the
     * paint model, so the reference does not depend on what is being tested): the range of those
     * zooms' means, and of their medians, each widened by 0.03 (the zooms are nine tiles each, at one
     * place, so a band of exactly their extremes would be a statement about this sample only).
     */
    private fun zoomedInBands(): Pair<Band, Band> {
        val perZoom = (11..15).map { meanAndMedian(tiles.getValue(it), ::nightColorOf) }
        return Band(perZoom.minOf { it.first } - 0.03, perZoom.maxOf { it.first } + 0.03) to
            Band(perZoom.minOf { it.second } - 0.03, perZoom.maxOf { it.second } + 0.03)
    }

    @Test
    fun `topo night at every sampled zoom is in the band of today's zoomed-in night`() {
        val (meanBand, medianBand) = zoomedInBands()
        val paint = topoNightPaint()
        val failures = mutableListOf<String>()
        for (z in 7..15) for (mapZoom in mapZoomsShowing(z)) {
            val raster = rasterPaintAt(paint, mapZoom)
            val (mean, median) = meanAndMedian(tiles.getValue(z)) { rasterShade(it, raster) }
            if (!meanBand.contains(mean)) failures += "tile z$z at map zoom $mapZoom: mean lightness %.3f, band $meanBand".format(mean)
            if (!medianBand.contains(median)) failures += "tile z$z at map zoom $mapZoom: ground (median) lightness %.3f, band $medianBand".format(median)
        }
        if (failures.isNotEmpty()) fail("topo night leaves the zoomed-in band:\n" + failures.joinToString("\n"))
    }

    @Test
    fun `labels stay lighter than the ground at every sampled zoom`() {
        val paint = topoNightPaint()
        for (z in 7..15) for (mapZoom in mapZoomsShowing(z)) {
            val raster = rasterPaintAt(paint, mapZoom)
            val set = tiles.getValue(z)
            val (_, ground) = meanAndMedian(set) { rasterShade(it, raster) }
            val labels = set.colours.filterKeys { isLabelPixel(it) }
            val labelMean = labels.entries.sumOf { (c, n) -> lightness(rasterShade(c, raster)) * n } / labels.values.sum()
            assertTrue(
                "tile z$z at map zoom $mapZoom: labels %.3f are not at least 0.15 above the ground %.3f".format(labelMean, ground),
                labelMean >= ground + 0.15,
            )
        }
    }

    @Test
    fun `zoomed-in tiles come out exactly as today's V1`() {
        val paint = topoNightPaint()
        for (z in 11..15) {
            for (mapZoom in mapZoomsShowing(z) + listOf(z - 0.5 + 1.0, 17.0)) {
                val raster = rasterPaintAt(paint, mapZoom)
                for (day in tiles.getValue(z).colours.keys) {
                    val today = nightColorOf(day)
                    val now = rasterShade(day, raster)
                    for (shift in listOf(16, 8, 0)) {
                        val d = Math.abs(((today shr shift) and 0xFF) - ((now shr shift) and 0xFF))
                        assertTrue(
                            "tile z$z at map zoom $mapZoom, day #%06X: today's V1 %08X, this paint %08X".format(day and 0xFFFFFF, today, now),
                            d <= 1,
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `the model's V1 is nightColorOf over every sampled tile colour`() {
        val v1 = RasterPaint(brightnessMin = 1.0, brightnessMax = 0.0, hueRotate = 180.0)
        for (set in tiles.values) for (day in set.colours.keys) {
            val expected = nightColorOf(day)
            val got = rasterShade(day, v1)
            for (shift in listOf(16, 8, 0)) {
                assertTrue("day #%06X: nightColorOf %08X, model %08X".format(day and 0xFFFFFF, expected, got),
                    Math.abs(((expected shr shift) and 0xFF) - ((got shr shift) and 0xFF)) <= 1)
            }
        }
    }

    @Test
    fun `raster tile zoom is one ahead of the map zoom for 256 px tiles and rounds half up`() {
        assertEquals(10, rasterTileZoomFor(9.499))
        assertEquals(11, rasterTileZoomFor(9.5))
        assertEquals(8, rasterTileZoomFor(6.5))
        assertEquals(7, rasterTileZoomFor(6.49))
        assertEquals(9, rasterTileZoomFor(9.0, tileSize = 512))
    }

    @Test
    fun `zoom expressions evaluate as the style spec defines them, and anything else is refused`() {
        fun eval(expr: String, zoom: Double) =
            rasterPaintAt(json.parseToJsonElement("""{"raster-brightness-min": $expr}""").jsonObject, zoom).brightnessMin
        assertEquals(0.5, eval("""["step", ["zoom"], 0.5, 9.5, 1]""", 9.499), 0.0)
        assertEquals(1.0, eval("""["step", ["zoom"], 0.5, 9.5, 1]""", 9.5), 0.0)
        assertEquals(0.75, eval("""["interpolate", ["linear"], ["zoom"], 9, 0.5, 10, 1]""", 9.5), 1e-12)
        assertEquals(0.5, eval("""["interpolate", ["linear"], ["zoom"], 9, 0.5, 10, 1]""", 3.0), 0.0)
        assertEquals(1.0, eval("""["interpolate", ["linear"], ["zoom"], 9, 0.5, 10, 1]""", 12.0), 0.0)
        assertEquals(0.25, eval("0.25", 5.0), 0.0)
        for (unsupported in listOf("""["interpolate", ["exponential", 2], ["zoom"], 9, 0.5, 10, 1]""", """["get", "x"]""", """["step", ["pitch"], 0.5, 9.5, 1]""")) {
            try {
                eval(unsupported, 9.0)
                fail("$unsupported should be refused, not guessed at")
            } catch (expected: IllegalStateException) {
            } catch (expected: IllegalArgumentException) {
            }
        }
    }

    // --- The documents this change must not move, as literal bytes (`styleJsonFor`'s output before it). ---

    @Test
    fun `day style JSON is byte-identical to before for every basemap`() {
        assertEquals(SATELLITE_DAY, styleJsonFor(Basemap.USGS_IMAGERY_ONLY, night = false))
        assertEquals(TOPO_DAY, styleJsonFor(Basemap.OPEN_TOPO_MAP, night = false))
        assertEquals(STREET_DAY, styleJsonFor(Basemap.OSM_STANDARD, night = false))
    }

    @Test
    fun `Satellite's night style JSON is byte-identical to its day JSON`() {
        assertEquals(SATELLITE_DAY, styleJsonFor(Basemap.USGS_IMAGERY_ONLY, night = true))
    }

    @Test
    fun `Street's night style JSON is byte-identical to before`() {
        assertEquals(STREET_NIGHT, styleJsonFor(Basemap.OSM_STANDARD, night = true))
    }

    private val SATELLITE_DAY = listOf(
        "{",
        "  \"version\": 8,",
        "  \"glyphs\": \"https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf\",",
        "  \"sources\": {",
        "    \"basemap\": {",
        "      \"type\": \"raster\",",
        "      \"tiles\": [\"https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/{z}/{y}/{x}\"],",
        "      \"tileSize\": 256,",
        "      \"maxzoom\": 15,",
        "      \"attribution\": \"USGS The National Map, orthoimagery — public domain\"",
        "    }",
        "  },",
        "  \"layers\": [",
        "    {\"id\": \"basemap\", \"type\": \"raster\", \"source\": \"basemap\"}",
        "  ]",
        "}",
    ).joinToString("\n")

    private val TOPO_DAY = listOf(
        "{",
        "  \"version\": 8,",
        "  \"glyphs\": \"https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf\",",
        "  \"sources\": {",
        "    \"basemap\": {",
        "      \"type\": \"raster\",",
        "      \"tiles\": [\"https://a.tile.opentopomap.org/{z}/{x}/{y}.png\"],",
        "      \"tileSize\": 256,",
        "      \"maxzoom\": 17,",
        "      \"attribution\": \"© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)\"",
        "    }",
        "  },",
        "  \"layers\": [",
        "    {\"id\": \"basemap\", \"type\": \"raster\", \"source\": \"basemap\"}",
        "  ]",
        "}",
    ).joinToString("\n")

    private val STREET_DAY = listOf(
        "{",
        "  \"version\": 8,",
        "  \"glyphs\": \"https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf\",",
        "  \"sources\": {",
        "    \"basemap\": {",
        "      \"type\": \"raster\",",
        "      \"tiles\": [\"https://tile.openstreetmap.org/{z}/{x}/{y}.png\"],",
        "      \"tileSize\": 256,",
        "      \"maxzoom\": 19,",
        "      \"attribution\": \"© OpenStreetMap contributors\"",
        "    }",
        "  },",
        "  \"layers\": [",
        "    {\"id\": \"basemap\", \"type\": \"raster\", \"source\": \"basemap\"}",
        "  ]",
        "}",
    ).joinToString("\n")

    private val STREET_NIGHT = listOf(
        "{",
        "  \"version\": 8,",
        "  \"glyphs\": \"https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf\",",
        "  \"sources\": {",
        "    \"basemap\": {",
        "      \"type\": \"raster\",",
        "      \"tiles\": [\"https://tile.openstreetmap.org/{z}/{x}/{y}.png\"],",
        "      \"tileSize\": 256,",
        "      \"maxzoom\": 19,",
        "      \"attribution\": \"© OpenStreetMap contributors\"",
        "    }",
        "  },",
        "  \"layers\": [",
        "    {\"id\": \"basemap\", \"type\": \"raster\", \"source\": \"basemap\",",
        "      \"paint\": {",
        "        \"raster-brightness-min\": 1,",
        "        \"raster-brightness-max\": 0,",
        "        \"raster-hue-rotate\": 180",
        "      }}",
        "  ]",
        "}",
    ).joinToString("\n")
}
