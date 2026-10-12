package com.zynergylabs.forager.app.ui.availability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dispatch 2026-10-12-06 (RECORD -934, the owner: "Trim extras when tight (Recommended)"): the card's columns give way in the
 * owner's order, the kind word, then the turn words to degrees, then "Alt", and never the figure, the compass or the grid
 * reference; the evening line moves out of the right column rather than being cut ("Moves to a full-width line").
 *
 * Pinned on round numbers so each step's threshold is exact: divider 10 each, gap 2, arrow 10. The full form needs
 * left max(40 + 2 + 30, 60 + 2 + 10) = 72, middle max(20 + 2 + 30, 10 + 2 + 40) = 52, right 100, plus 20 of dividers: 244.
 */
class RoundedMapCardFitTest {

    private fun widths(available: Int, headingIsStatus: Boolean = false, headingPx: Int = 40, sundown: Int? = null, backBy: Int? = null) = CardColumnsWidths(
        availablePx = available,
        dividerPx = 10,
        gapPx = 2,
        arrowPx = 10,
        figurePx = 40,
        kindPx = 30,
        turnWholePx = 60,
        turnShortPx = 20,
        altitudeLabelPx = 20,
        altitudePx = 30,
        headingPx = headingPx,
        headingIsStatus = headingIsStatus,
        headingIconPx = 12,
        coordinatesPx = 100,
        sundownPx = sundown,
        backByPx = backBy,
        eveningSeparatorPx = 6,
    )

    private fun fit(w: CardColumnsWidths) = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")

    @Test
    fun `with room everything is drawn`() {
        val f = fit(widths(244))
        assertEquals("sharp right 169°", f.turnText)
        assertTrue(f.kindInColumn)
        assertTrue(f.altitudeLabel)
        assertTrue(f.altitudeShown)
        assertFalse(f.headingAsIcon)
        assertFalse(f.overflows)
    }

    @Test
    fun `first the kind word leaves the left column, and only it`() {
        // Without the kind the left column is max(40, 72) = 72: no saving here, so the turn shrinks too at 243.
        val f = fit(widths(243))
        assertFalse("the kind goes first", f.kindInColumn)
        assertEquals("then the turn shrinks to degrees", "169°", f.turnText)
        assertTrue("Alt stays", f.altitudeLabel)
    }

    @Test
    fun `the kind alone gives way where that is enough`() {
        // A short turn ("ahead 5°", 20 wide) leaves the kind as the widest part of the left column.
        val w = widths(243).copy(turnWholePx = 20)
        val f = cardColumnsFit(w, turnWhole = "ahead 5°", turnShort = "5°")
        assertFalse(f.kindInColumn)
        assertEquals("the turn words stay whole", "ahead 5°", f.turnText)
        assertTrue(f.altitudeLabel)
    }

    @Test
    fun `then the turn words shrink to degrees and the needle`() {
        // Short turn: left = max(40, 20 + 2 + 10) = 40; total 40 + 52 + 100 + 20 = 212.
        val f = fit(widths(212))
        assertFalse(f.kindInColumn)
        assertEquals("169°", f.turnText)
        assertTrue("Alt still drawn", f.altitudeLabel)
    }

    @Test
    fun `then the Alt label goes, and the altitude stays`() {
        // Middle without the label: max(30, 52) = 52, no saving; a shorter heading makes the label the widest part.
        val w = widths(211).copy(headingPx = 20)
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        // Middle with label: max(52, 32) = 52; without: max(30, 32) = 32. Total without = 40 + 32 + 100 + 20 = 192.
        assertFalse(f.altitudeLabel)
        assertTrue(f.altitudeShown)
        assertEquals("169°", f.turnText)
    }

    @Test
    fun `past the owner's order the degrees go before the altitude, and the figure, heading and grid reference are never touched`() {
        val w = widths(160).copy(headingPx = 20, turnShortPx = 40)
        // Short turn: max(40, 52) = 52; none: max(40, 10) = 40. Middle without label 32. 40 + 32 + 100 + 20 = 192 > 160.
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        assertEquals("the needle alone", "", f.turnText)
        assertFalse("then the altitude", f.altitudeShown)
        assertTrue("nothing fits: reported, not hidden", f.overflows)
    }

    @Test
    fun `a compass status in words takes the crossed-out compass where its words do not fit`() {
        // A status 120 wide: middle with words = max(52, 132) = 132; with the icon = max(52, 12) = 52.
        val words = fit(widths(244 + 80, headingIsStatus = true, headingPx = 120))
        assertFalse("its words fit at 324", words.headingAsIcon)
        val icon = fit(widths(244, headingIsStatus = true, headingPx = 120))
        assertTrue("the icon at 244", icon.headingAsIcon)
        assertTrue("the kind stays: the icon came before trimming more", icon.kindInColumn)
    }

    @Test
    fun `a reading never becomes the icon`() {
        val f = fit(widths(150, headingIsStatus = false, headingPx = 120))
        assertFalse(f.headingAsIcon)
    }

    @Test
    fun `a short evening line sits in the right column, a long one leaves it, and nothing is cut for it`() {
        // At 300 the right column has 300 - 72 - 52 - 20 = 156.
        assertEquals(EveningInColumn.SUNDOWN, fit(widths(300, sundown = 150)).eveningInColumn)
        assertEquals(EveningInColumn.NONE, fit(widths(300, sundown = 157)).eveningInColumn)
        assertEquals(EveningInColumn.BOTH, fit(widths(300, sundown = 70, backBy = 80)).eveningInColumn)
        assertEquals("Back by moves under the columns", EveningInColumn.SUNDOWN, fit(widths(300, sundown = 100, backBy = 80)).eveningInColumn)
        assertEquals(EveningInColumn.BACK_BY, fit(widths(300, backBy = 80)).eveningInColumn)
        val f = fit(widths(300, sundown = 400))
        assertTrue("the columns are not trimmed for the evening line", f.kindInColumn && f.altitudeLabel && f.turnText == "sharp right 169°")
    }
}
