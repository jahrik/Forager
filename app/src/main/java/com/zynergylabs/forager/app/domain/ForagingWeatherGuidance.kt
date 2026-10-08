package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.domain.model.TaxonSearchResult
import com.zynergylabs.forager.app.domain.model.UnitSystem
import com.zynergylabs.forager.app.domain.model.celsiusToFahrenheit
import kotlin.math.roundToInt

/**
 * What the user currently has selected, together with the broad iNaturalist group it belongs to
 * when that is known.
 *
 * This exists because [TaxonFilter.SpecificTaxon] carries a taxon id and a label and nothing else,
 * so a filter alone cannot answer "which category's guidance applies here". [TaxonSearchResult]
 * already knows the answer — it carries `iconicTaxonName` from iNaturalist and was dropping it on
 * the floor in `toFilter()` — so [fromSearchResult] is where that information is picked back up.
 *
 * The alternative, adding `iconicTaxonName` to [TaxonFilter.SpecificTaxon], was rejected: the
 * filter type's whole job is to describe an iNaturalist query, and the iconic name is not part of
 * the query a specific-taxon search sends. Keeping it out also keeps [TaxonFilter] equality
 * meaning "the same search".
 */
data class ForagingSelection(
    val filter: TaxonFilter,
    /** iNaturalist iconic taxon name, e.g. "Fungi" or "Plantae". Null when not known. */
    val iconicTaxonName: String?,
) {
    companion object {
        fun fromCategory(category: TaxonFilter.IconicCategory) =
            ForagingSelection(category, category.iconicTaxonName)

        /** A species the user searched for by name, carrying the group iNaturalist filed it under. */
        fun fromSearchResult(result: TaxonSearchResult) =
            ForagingSelection(result.toFilter(), result.iconicTaxonName)

        /**
         * The selection behind an [TaxonFilter.IconicCategory] or [TaxonFilter.SpecificTaxon]
         * chosen some way other than a name search (e.g. [TaxonFilter.LICHENS], restorable from a
         * recent search even though no picker offers it directly any more).
         *
         * A lichens selection is deliberately given a null group rather than "Fungi". iNaturalist
         * does file lichens under Fungi, but the general pattern this app states — fleshy fungi
         * fruiting some weeks after sustained rain — is about fruiting bodies, and reusing it for
         * lichenized fungi would present it as a claim about organisms it was not written about.
         * Forager has no sourced guidance for lichens, so it shows none; see
         * [ForagingWeatherGuidance].
         */
        fun forChip(filter: TaxonFilter): ForagingSelection = when (filter) {
            is TaxonFilter.IconicCategory -> fromCategory(filter)
            is TaxonFilter.SpecificTaxon -> ForagingSelection(filter, iconicTaxonName = null)
        }
    }
}

/**
 * Interpretation text for a selection: a general pattern, stated as a general pattern.
 *
 * Two rules this file exists to enforce, both load-bearing:
 *
 * 1. **Nothing here is species-specific.** iNaturalist returns thousands of species this app has
 *    no sourced information about, and writing confident fruiting triggers for an arbitrary one
 *    would be fabricating expertise. Guidance is keyed to broad groups only, and a species gets
 *    its group's text unchanged.
 * 2. **Groups do not share text.** Fungi and plants have genuinely different relationships to
 *    rainfall, and a group this file has written nothing for gets no guidance at all rather than
 *    another group's.
 *
 * What was removed, and why (dispatch 2026-09-28-695, the owner on a phone screenshot of Trip
 * Windows for Fly Agaric: "remove the bottom text about "No weather guidance for this section" and
 * the text below it. That seems like a placeholder and users might get confused"; then, shown
 * that the text was deliberate, "Both, everywhere"). Two texts used to sit here: a "No weather
 * guidance for this selection" block for a selection with no written group, and an italic "No
 * species-specific data is available for ..." sentence under every specific taxon. Both were
 * honest, and both read to the owner as placeholders. With no group there is now no heading and
 * no paragraph, and Trip Windows ends at its measurements; a species shows its group's pattern
 * with no sentence under it. Rule 1 is what still keeps that pattern from being a claim about the
 * species: the fungi text itself says the lag "varies with species" and that Forager has measured
 * nothing.
 */
object ForagingWeatherGuidance {

    /**
     * Guidance to show for a selection.
     *
     * @param paragraphs the general pattern for the selection's group, or, for plants, the plain
     *   statement that there is no weather pattern to offer.
     */
    data class Guidance(
        val heading: String,
        val paragraphs: List<String>,
    )

    /** iNaturalist's iconic taxon name for fungi, as it appears in `iconic_taxon_name`. */
    private const val FUNGI = "Fungi"

    /** iNaturalist's iconic taxon name for plants. */
    private const val PLANTAE = "Plantae"

    /**
     * The guidance for [selection]'s group, the same for a category and for a species within it, or
     * null when no guidance is written for that group (or the group is not known), in which case
     * nothing is shown.
     *
     * [unitSystem] is the Units setting, which the soil temperature band follows (dispatch 2026-09-28-549).
     */
    fun forSelection(selection: ForagingSelection, unitSystem: UnitSystem): Guidance? =
        groupGuidance(selection.iconicTaxonName, unitSystem)

    private fun groupGuidance(iconicTaxonName: String?, unitSystem: UnitSystem): Guidance? = when (iconicTaxonName) {
        FUNGI -> Guidance(
            heading = "Rain and fungi: the general pattern",
            paragraphs = listOf(
                "Many fleshy fungi fruit roughly one to three weeks after sustained rain, once " +
                    "the soil has stayed damp long enough for mycelium already in the ground to " +
                    "respond. That one-to-three-week range is long-standing foraging lore and " +
                    "general mycological writing — it is not a figure Forager has measured, and " +
                    "the real lag varies with species, substrate and temperature.",
                "Soil moisture and soil temperature in the top few centimetres are shown because " +
                    "that is the layer mycelium actually sits in; surface rainfall can overstate " +
                    "or understate how wet it is down there. A soil temperature of roughly " +
                    "${soilTemperatureBand(FruitingPatternAssumptions.TEMPERATE_FRUITING_SOIL_TEMPERATURE_C, unitSystem)} " +
                    "is often quoted as broadly typical for temperate fleshy fungi. It is a wide " +
                    "band quoted as a rough one, and plenty of species sit outside it.",
                // Worded without the words the guidance-text guard in ForagingWeatherGuidanceTest
                // looks for. A blunt substring tripwire cannot tell a claim from a disclaimer, and
                // a tripwire that has to be taught exceptions stops being a tripwire.
                "Forager has not measured any relationship between these conditions and how often " +
                    "anything is actually observed near you. The dates and measurements are what " +
                    "the weather did; whether that adds up to a trip is yours to judge. Forager " +
                    "deliberately does not rank the days for you, because it has nothing to rank " +
                    "them with.",
            ),
        )

        PLANTAE -> Guidance(
            heading = "Rain and plants: no pattern to offer",
            paragraphs = listOf(
                "Forager has no weather-based pattern for plants, and is not going to reuse the " +
                    "fungal one. Rain obviously matters to plants, but what makes a plant worth " +
                    "foraging on a particular day is mostly where it is in its own year — " +
                    "leafing, flowering, fruiting, setting seed — rather than a lag after a rain " +
                    "event the way it is for fleshy fungi.",
                "The rainfall and soil measurements are still shown, without an interpretation " +
                    "attached. The month filter on the ranked list is the seasonality signal " +
                    "Forager does have, and it is built on real observation counts.",
            ),
        )

        // No heading and no paragraph for any other group: see this object's doc comment.
        else -> null
    }

    /**
     * A Celsius band as prose in the user's units (dispatch 2026-09-28-549): "10–20 °C" under metric,
     * exactly as before, and whole °F under imperial, from the same constant ("50–68 °F"). The band
     * itself stays °C in [FruitingPatternAssumptions]; only the words convert.
     */
    private fun soilTemperatureBand(band: ClosedFloatingPointRange<Double>, unitSystem: UnitSystem): String = when (unitSystem) {
        UnitSystem.METRIC -> "${format(band.start)}–${format(band.endInclusive)} °C"
        UnitSystem.IMPERIAL ->
            "${celsiusToFahrenheit(band.start).roundToInt()}–${celsiusToFahrenheit(band.endInclusive).roundToInt()} °F"
    }

    /** Trims a whole-number double to "10" rather than "10.0" for use in prose. */
    private fun format(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
}
