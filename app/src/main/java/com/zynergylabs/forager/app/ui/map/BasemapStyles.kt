package com.zynergylabs.forager.app.ui.map

import com.zynergylabs.forager.app.map.OFFLINE_STYLE_ATTRIBUTION
import com.zynergylabs.forager.app.map.OFFLINE_STYLE_URL
import com.zynergylabs.forager.app.ui.theme.MapPalette

/**
 * The one place a [Basemap] becomes a MapLibre style — this project's own analog of the deleted
 * `BasemapTileSources.kt`, which did the same job for osmdroid's `ITileSource`.
 *
 * Every basemap here stays a **raster** source: the minimal MapLibre style JSON a raster tile
 * template needs (`"version": 8`, one `"raster"` source, one `"raster"` layer), built directly from
 * [Basemap]'s own fields rather than four separately hosted style documents — see [Basemap]'s class
 * doc for why. This is the same mechanism `MapLibreBasemapPreviewActivity`'s `rasterStyleJson`
 * proved (that scaffolding is deleted in this same change; its style-JSON shape survives here,
 * carried forward rather than reinvented).
 *
 * [glyphs] is set on every style, not only the ones that need text, because [SightingsMap] adds a
 * `SymbolLayer` with a `text-field` for the numbered foraging-area markers on top of *whichever*
 * basemap is active — a style with no `glyphs` URL renders `text-field` as nothing at all, silently
 * (this was hit for real: see `MapLibreBasemapPreviewActivity`'s history, "Fix missing area-marker
 * labels: style JSON had no glyphs URL", hardware-confirmed on PR #22). [AREA_MARKER_FONT_STACK] is
 * the same font that fix settled on, for the same reason: `"Open Sans Semibold"` is what
 * `demotiles.maplibre.org`'s own reference style actually ships in its glyph set, not a guess.
 */
/**
 * Night mode's raster paint block: the **V1** transform, applied to the basemap layer.
 *
 *  - `raster-brightness-min 1` and `raster-brightness-max 0` swap the output range, so every
 *    channel comes out as `1 - c`: the pale ground goes dark and the dark linework goes pale.
 *  - `raster-hue-rotate 180` turns every hue half way round first, which undoes the half turn the
 *    inversion gives it. The net effect, per MapLibre Native's own raster shader at the pinned
 *    `13.5.0` (cited in `docs/audits/2026-09-27-marker-swatch-board.md` section 2), is
 *    `night = clamp(c + 1 - 2 * mean(r, g, b))` per gamma-encoded channel: lightness inverts, hue
 *    stays. [nightColorOf] is the same formula for colours this project sets itself.
 *
 * **Why V1 and not plain inversion (V2).** V2 is the same block without the hue rotate. It turns the
 * whole ground blue-violet, which the owner found an eyesore at night: "I like the V1 arrangement
 * better. The blue is pretty but that can be an eyesore at night". V1 keeps blue at about the day
 * map's share (the swatch board, section 1). On the S22 V1 held hue within 1 degree at P90 on
 * Topographical and Street (`docs/audits/2026-09-27-night-inversion-spike-and-tile-measurement.md`).
 *
 * **Why the earlier blocks were dropped.** This block used to dim the ground with
 * `raster-brightness-max` (removed 2026-08-26: it left the map brighter than the dark chrome around
 * it, and a lower cap had already failed on legibility), and then only desaturated it
 * (`raster-saturation -0.35`, `raster-contrast 0.1`), which left a day-bright ground. Both were
 * stopgaps for the inversion an earlier session believed needed tile interception, because the
 * raster paint properties have no per-pixel invert. The brightness-range swap is that invert.
 *
 * **Satellite stays day** (owner: "Satellite stays as it is at night"): [basemapTakesNightPaint]
 * is the one place that says so, and [styleJsonFor] reads it.
 *
 * All three are MapLibre style-spec v8 raster paint properties, so they need no code path of their
 * own; they ride in the style JSON the basemap swap already rebuilds.
 */
private const val NIGHT_RASTER_PAINT = """,
          "paint": {
            "raster-brightness-min": 1,
            "raster-brightness-max": 0,
            "raster-hue-rotate": 180
          }"""

/**
 * Whether [basemap]'s raster style takes [NIGHT_RASTER_PAINT] when night mode is on. `false` only
 * for [Basemap.USGS_IMAGERY_ONLY], by the owner's ruling: "Satellite stays as it is at night; only
 * the markers switch to night colours". A per-basemap decision
 * rather than an inline `if` in [styleJsonFor], so a new basemap has to be placed on one side of it
 * and a test can pin the set.
 */
internal fun basemapTakesNightPaint(basemap: Basemap): Boolean = basemap != Basemap.USGS_IMAGERY_ONLY

internal fun styleJsonFor(basemap: Basemap, night: Boolean = false): String = """
    {
      "version": 8,
      "glyphs": "$GLYPHS_URL_TEMPLATE",
      "sources": {
        "$RASTER_SOURCE_ID": {
          "type": "raster",
          "tiles": ["${basemap.tileUrlTemplate}"],
          "tileSize": 256,
          "maxzoom": ${basemap.maxZoom},
          "attribution": ${basemap.attribution.toJsonStringLiteral()}
        }
      },
      "layers": [
        {"id": "$RASTER_LAYER_ID", "type": "raster", "source": "$RASTER_SOURCE_ID"${if (night && basemapTakesNightPaint(basemap)) NIGHT_RASTER_PAINT else ""}}
      ]
    }
""".trimIndent()

/**
 * Which style document [SightingsMap] hands to `Style.Builder` — Stage 2e-ii's one decision, as a
 * value rather than a `Style.Builder` so it can be asserted in a plain JVM test
 * (`OfflineStyleSwapTest`), the same boundary [styleJsonFor]/`BasemapStyleTest` already draw.
 *
 * [Json] is the in-app raster style for a [Basemap], loaded with `fromJson`. [Uri] is a style
 * loaded **by URL**, which is the only way MapLibre's offline database can serve it: the store
 * keys the style document by the URL it was downloaded against (`OfflineStyle.kt`), so a `fromJson`
 * copy of the offline style's content would never ask the store for that resource. The tiles would
 * still be found by their own URLs, but the app would be rendering a copied style free to drift
 * from the one the download stored.
 */
sealed interface MapStyleSource {
    data class Json(val json: String) : MapStyleSource
    data class Uri(val uri: String) : MapStyleSource
}

/**
 * The offline style when [useOfflineTiles] is on, regardless of [basemap] and [night]; the
 * basemap's own raster style otherwise, with the night paint when [night] is on and the basemap
 * takes it ([basemapTakesNightPaint]).
 *
 * The offline source ignores [night] because the store holds one style document, keyed by its one
 * URL: `NIGHT_RASTER_PAINT` is a raster paint block, and the offline style has 57 vector layers and
 * no raster layer, so there is nothing for it to apply to. Stage 2e-ii left night inert there (owner
 * ruling: report, do not fix). Since colour build C1 the offline style's night is applied after it
 * loads, by recolouring its own layers with the same V1 transform ([offlineNightRecolourOf], called
 * from `SightingsMap`'s `setStyle` callback), rather than by a second style URL that would need its
 * own download.
 */
internal fun mapStyleSourceFor(basemap: Basemap, night: Boolean, useOfflineTiles: Boolean): MapStyleSource =
    if (useOfflineTiles) MapStyleSource.Uri(OFFLINE_STYLE_URL) else MapStyleSource.Json(styleJsonFor(basemap, night))

/**
 * The always-visible credit for what the map is currently drawing — the offline style's own when
 * it is showing (a licensing obligation, see [OFFLINE_STYLE_ATTRIBUTION]), the [Basemap]'s
 * otherwise. Before 2e-ii the caption read [Basemap.attribution] unconditionally, which over
 * offline tiles would have credited OpenTopoMap for Protomaps geometry.
 */
internal fun mapAttributionFor(basemap: Basemap, useOfflineTiles: Boolean): String =
    if (useOfflineTiles) OFFLINE_STYLE_ATTRIBUTION else basemap.attribution

/**
 * Every credit the map's caption shows, in order: what the map is drawn on ([mapAttributionFor]'s
 * one credit) first, then each of [layerCredits] (the active layers' own, `activeLayerCredits`),
 * without repeating one already present. Map layers L0a, A5.
 */
internal fun mapCreditsFor(basemap: Basemap, useOfflineTiles: Boolean, layerCredits: List<String> = emptyList()): List<String> =
    (listOf(mapAttributionFor(basemap, useOfflineTiles)) + layerCredits).distinct()

/** [credits] as the one line the caption draws, joined by [ATTRIBUTION_SEPARATOR]. */
internal fun attributionCaption(credits: List<String>): String = credits.joinToString(ATTRIBUTION_SEPARATOR)

/**
 * Between two credits in the caption. A middle dot rather than a comma or a dash, because both of
 * those already occur inside the basemap credits ("© OpenStreetMap, SRTM, OpenTopoMap (CC-BY-SA)",
 * "USGS The National Map, orthoimagery — public domain").
 */
internal const val ATTRIBUTION_SEPARATOR = " · "

/**
 * Everything that, when it changes, means [SightingsMap] must call `setStyle` again — the
 * basemap swap's guard, as a value. Before 2e-ii the guard compared basemap and palette only
 * (`appliedBasemap == basemap && appliedPalette == mapPalette`), so an offline flag flipping on its
 * own would never have reached `setStyle` — the one place the pre-build report said a "toggle does
 * nothing" bug would come from (§2.2). Extracted so that gap is pinned by a test rather than by
 * reading the effect's keys.
 */
internal data class AppliedMapStyle(
    val basemap: Basemap,
    /**
     * The marker palette: `MapPalette.forMode` of the **raw** Night Maps toggle, on every basemap,
     * Satellite included (colour build C2; the owner ruled that on Satellite only the markers switch).
     * The overlay layers bake these colours in when the style loads, so a palette change is a reload:
     * that is why a toggle over Satellite reloads, even though [night] stays false there.
     */
    val palette: MapPalette,
    val useOfflineTiles: Boolean,
    /**
     * **Effective** night ([effectiveNight]), not the raw toggle: whether the *basemap* takes its night
     * paint. Night Maps did nothing before colour build C1 partly because this value had no night
     * component at all, so a toggle compared equal and never reached `setStyle`. Over Satellite it is
     * false either way, since Satellite's style does not change at night; the reload a toggle there
     * causes comes from [palette].
     */
    val night: Boolean,
)

/**
 * Whether the style [SightingsMap] loads should be the night one: the Night Maps toggle, except on
 * Satellite's own raster style, which stays day ([basemapTakesNightPaint]). Over the offline style
 * it is the toggle whatever the basemap, because the offline style's night is its post-load
 * recolour ([offlineNightRecolourOf]), not a raster paint.
 */
internal fun effectiveNight(basemap: Basemap, nightMode: Boolean, useOfflineTiles: Boolean): Boolean =
    nightMode && (useOfflineTiles || basemapTakesNightPaint(basemap))

/**
 * What [SightingsMap]'s style effect should load for these inputs, or `null` while the Night Maps
 * preference has not loaded ([nightModeLoaded] `false`): the cold-launch gate. Without it, the effect
 * could apply a day style before an asynchronous read reported night, and a night user's map would
 * stay day. A pure function so the gate is asserted headless (`OfflineStyleSwapTest`); the effect's
 * own keys are not reachable without a native MapView. The marker palette is chosen here too, from the
 * raw toggle, for the same reason: a test can reach it here and not in the composable.
 */
internal fun requestedMapStyle(
    basemap: Basemap,
    useOfflineTiles: Boolean,
    nightMode: Boolean,
    nightModeLoaded: Boolean,
): AppliedMapStyle? =
    if (!nightModeLoaded) {
        null
    } else {
        AppliedMapStyle(
            basemap = basemap,
            palette = MapPalette.forMode(nightMode),
            useOfflineTiles = useOfflineTiles,
            night = effectiveNight(basemap, nightMode = nightMode, useOfflineTiles = useOfflineTiles),
        )
    }

/** `true` when nothing has been applied yet, or when any part of [requested] differs from what was. */
internal fun needsStyleReload(applied: AppliedMapStyle?, requested: AppliedMapStyle): Boolean = applied != requested

/** The id every basemap's raster source/layer is added under — fixed, since a style swap replaces the whole style object anyway. */
internal const val RASTER_SOURCE_ID = "basemap"
internal const val RASTER_LAYER_ID = "basemap"

/**
 * MapLibre's own public glyph PBF endpoint (demotiles.maplibre.org, no key) — see this file's class
 * doc comment for why every style needs one.
 */
internal const val GLYPHS_URL_TEMPLATE = "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf"
internal val AREA_MARKER_FONT_STACK = arrayOf("Open Sans Semibold")

/**
 * Escapes [this] for use as a JSON string literal inside the hand-built style JSON above.
 *
 * [Basemap.attribution] is this project's own fixed constant text today (never user input), so a
 * full JSON-string escaper is more than the actual risk warrants — but "more than needed" is cheap
 * insurance against a future attribution string that happens to contain a `"` or a backslash
 * silently producing invalid JSON that `Style.Builder.fromJson` would then reject at runtime with a
 * message pointing nowhere near the real cause.
 */
private fun String.toJsonStringLiteral(): String {
    val escaped = buildString {
        append('"')
        this@toJsonStringLiteral.forEach { c ->
            when (c) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
        append('"')
    }
    return escaped
}
