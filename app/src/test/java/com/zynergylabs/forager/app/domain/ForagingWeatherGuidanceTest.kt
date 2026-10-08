package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.TaxonFilter
import com.zynergylabs.forager.app.domain.model.TaxonSearchResult
import com.zynergylabs.forager.app.domain.model.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForagingWeatherGuidanceTest {

    // Metric unless a test says otherwise: the band's own constant is in °C, so the metric text is
    // the one the assumption tests below read against (dispatch 2026-09-28-549).
    // Null when no guidance is written for the selection's group (dispatch 2026-09-28-695).
    private fun guidanceFor(selection: ForagingSelection, unitSystem: UnitSystem = UnitSystem.METRIC) =
        ForagingWeatherGuidance.forSelection(selection, unitSystem)

    private fun writtenGuidanceFor(selection: ForagingSelection, unitSystem: UnitSystem = UnitSystem.METRIC) =
        requireNotNull(guidanceFor(selection, unitSystem)) { "expected written guidance for ${selection.filter.label}" }

    private fun textOf(selection: ForagingSelection, unitSystem: UnitSystem = UnitSystem.METRIC) =
        writtenGuidanceFor(selection, unitSystem).paragraphs.joinToString(" ")

    // ---- selection plumbing ----------------------------------------------------------------

    @Test
    fun `a category selection carries its own iconic taxon name`() {
        val selection = ForagingSelection.fromCategory(TaxonFilter.FUNGI)

        assertEquals("Fungi", selection.iconicTaxonName)
        assertEquals(TaxonFilter.FUNGI, selection.filter)
    }

    @Test
    fun `a searched species carries the group iNaturalist filed it under`() {
        val result = TaxonSearchResult(
            taxonId = 47348,
            scientificName = "Cantharellus",
            commonName = "chanterelles",
            rank = "genus",
            iconicTaxonName = "Fungi",
            photoUrl = null,
        )

        val selection = ForagingSelection.fromSearchResult(result)

        assertEquals(TaxonFilter.SpecificTaxon(taxonId = 47348, label = "chanterelles"), selection.filter)
        assertEquals("Fungi", selection.iconicTaxonName)
    }

    // ---- category guidance varies by category ----------------------------------------------

    @Test
    fun `fungi guidance states the rain lag pattern and hedges it as a rule of thumb`() {
        val text = textOf(ForagingSelection.fromCategory(TaxonFilter.FUNGI))

        assertTrue(text.contains("one to three weeks after sustained rain"))
        // Traceability: the claim is attributed and explicitly not presented as measured here.
        assertTrue(text.contains("not a figure Forager has measured"))
    }

    @Test
    fun `fungi guidance quotes the soil temperature band from the labelled assumption`() {
        val text = textOf(ForagingSelection.fromCategory(TaxonFilter.FUNGI))

        assertTrue(
            "guidance should quote ${FruitingPatternAssumptions.TEMPERATE_FRUITING_SOIL_TEMPERATURE_C}",
            text.contains("10–20 °C"),
        )
        assertEquals(10.0, FruitingPatternAssumptions.TEMPERATE_FRUITING_SOIL_TEMPERATURE_C.start, 0.0)
        assertEquals(20.0, FruitingPatternAssumptions.TEMPERATE_FRUITING_SOIL_TEMPERATURE_C.endInclusive, 0.0)
    }

    /**
     * Dispatch 2026-09-28-549: under Imperial (US) the same band reads in whole °F, converted from
     * the one °C constant (10 °C = 50 °F, 20 °C = 68 °F, worked by hand), and no °C figure is left.
     */
    @Test
    fun `under the imperial setting the fungi guidance quotes the band in Fahrenheit`() {
        val text = textOf(ForagingSelection.fromCategory(TaxonFilter.FUNGI), UnitSystem.IMPERIAL)

        assertTrue("expected \"roughly 50–68 °F\" in: $text", text.contains("roughly 50–68 °F"))
        assertFalse("no °C left in: $text", text.contains("°C"))
    }

    /** The metric text, exact, so the conversion cannot change what a metric reader sees. */
    @Test
    fun `under the metric setting the band reads exactly as before`() {
        val text = textOf(ForagingSelection.fromCategory(TaxonFilter.FUNGI), UnitSystem.METRIC)

        assertTrue(text, text.contains("A soil temperature of roughly 10–20 °C is often quoted"))
    }

    @Test
    fun `plants guidance is not the fungi text`() {
        val fungi = textOf(ForagingSelection.fromCategory(TaxonFilter.FUNGI))
        val plants = textOf(ForagingSelection.fromCategory(TaxonFilter.PLANTS))

        assertFalse(plants == fungi)
        assertFalse(
            "plants must not inherit the fungal rain-lag claim",
            plants.contains("one to three weeks after sustained rain"),
        )
    }

    @Test
    fun `plants guidance says plainly that there is no weather pattern to offer`() {
        val guidance = writtenGuidanceFor(ForagingSelection.fromCategory(TaxonFilter.PLANTS))

        assertEquals("Rain and plants: no pattern to offer", guidance.heading)
        assertTrue(guidance.paragraphs.first().contains("no weather-based pattern for plants"))
    }

    /**
     * Dispatch 2026-09-28-695: the "No weather guidance for this selection" block is gone. A group
     * with nothing written for it gets no guidance at all, so the screen shows no heading and no
     * paragraph, and still never borrows another group's text.
     */
    @Test
    fun `a category with no written guidance shows none rather than borrowing another's`() {
        val insects = TaxonFilter.IconicCategory(iconicTaxonName = "Insecta", label = "Insects")

        assertNull(guidanceFor(ForagingSelection.fromCategory(insects)))
    }

    // ---- a specific taxon shows its group's pattern, with nothing added ---------------------

    @Test
    fun `a specific species shows exactly its category's guidance`() {
        val chanterelles = ForagingSelection(
            filter = TaxonFilter.SpecificTaxon(taxonId = 47348, label = "chanterelles"),
            iconicTaxonName = "Fungi",
        )

        assertEquals(
            writtenGuidanceFor(ForagingSelection.fromCategory(TaxonFilter.FUNGI)),
            writtenGuidanceFor(chanterelles),
        )
    }

    /** Dispatch 2026-09-28-695: the italic "No species-specific data is available for ..." note is gone everywhere. */
    @Test
    fun `a specific species carries no species note and nothing names it`() {
        val flyAgaric = ForagingSelection(
            filter = TaxonFilter.SpecificTaxon(taxonId = 48715, label = "Fly Agaric"),
            iconicTaxonName = "Fungi",
        )

        val guidance = writtenGuidanceFor(flyAgaric)
        val text = (listOf(guidance.heading) + guidance.paragraphs).joinToString(" ")

        assertEquals("Rain and fungi: the general pattern", guidance.heading)
        assertFalse(text, text.contains("No species-specific data"))
        assertFalse(text, text.contains("Fly Agaric"))
    }

    @Test
    fun `a species with no known group shows no guidance at all`() {
        val unknownGroup = ForagingSelection(
            filter = TaxonFilter.SpecificTaxon(taxonId = 99999, label = "Some Beetle"),
            iconicTaxonName = null,
        )

        assertNull(guidanceFor(unknownGroup))
    }

    @Test
    fun `the lichens chip is not given the fungi fruiting pattern, or any other`() {
        // iNaturalist files lichens under Fungi, but the stated pattern is about fruiting bodies.
        // Reusing it here would present it as a claim about organisms it was not written about.
        assertNull(guidanceFor(ForagingSelection.forChip(TaxonFilter.LICHENS)))
    }

    @Test
    fun `of the default selections, fungi and plants have guidance and lichens has none`() {
        val hasGuidance = listOf(TaxonFilter.FUNGI, TaxonFilter.PLANTS, TaxonFilter.LICHENS).associate { filter ->
            filter.label to (guidanceFor(ForagingSelection.forChip(filter)) != null)
        }

        assertEquals(
            mapOf("Fungi" to true, "Plants" to true, "Lichens (approx.)" to false),
            hasGuidance,
        )
    }

    @Test
    fun `no guidance text states a score, probability or best day`() {
        val everySelection = listOf(
            ForagingSelection.fromCategory(TaxonFilter.FUNGI),
            ForagingSelection.fromCategory(TaxonFilter.PLANTS),
            ForagingSelection.forChip(TaxonFilter.LICHENS),
            ForagingSelection(TaxonFilter.SpecificTaxon(1, "Amanita phalloides"), "Fungi"),
        )
        val forbidden = listOf("%", "best day", "chance of", "score", "star", "out of 5", "likely to find")

        val offenders = UnitSystem.entries.flatMap { system -> everySelection.map { it to system } }.flatMap { (selection, system) ->
            val guidance = guidanceFor(selection, system)
            val text = listOfNotNull(guidance?.heading).plus(guidance?.paragraphs.orEmpty()).joinToString(" ")
            forbidden.filter { text.lowercase().contains(it) }.map { "${selection.filter.label}: $it" }
        }

        assertEquals(emptyList<String>(), offenders)
    }
}
