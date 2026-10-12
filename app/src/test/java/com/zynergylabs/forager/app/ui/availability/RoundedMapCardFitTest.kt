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

    private fun widths(available: Int, headingIsStatus: Boolean = false, headingPx: Int = 40, evening: Int? = null) = CardColumnsWidths(
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
        eveningPiecePx = evening,
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
    fun `then within 16 ft takes its short form`() {
        // Figure 40, short 25. After step 3 at headingPx 20: left max(40, 32) = 40, middle 32, right 100, dividers 20 = 192.
        val w = widths(191).copy(headingPx = 20, figureShortPx = 25)
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        // Short figure: left max(25, 32) = 32; total 184.
        assertTrue("the short figure", f.figureShort)
        assertFalse("the grid reference keeps its column", f.gridOwnLine)
        assertFalse(f.altitudeLabel)
    }

    @Test
    fun `last, the grid reference takes its own line, and the distance, altitude and compass keep their columns`() {
        // Step 5: left 32 + middle 32 + one divider 10 = 74.
        val w = widths(150).copy(headingPx = 20, figureShortPx = 25)
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        assertTrue(f.gridOwnLine)
        assertTrue("the altitude stays", f.altitudeShown)
        assertEquals("the turn stays at degrees: nothing past the owner's order", "169°", f.turnText)
        assertFalse(f.overflows)
    }

    @Test
    fun `where even the grid reference's own line is not enough, it overflows and says so, trimming nothing further`() {
        val w = widths(70).copy(headingPx = 20, figureShortPx = 25)
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        assertTrue("reported", f.overflows)
        assertTrue("at the last step the owner set", f.gridOwnLine && f.figureShort)
        assertEquals("the degrees stay", "169°", f.turnText)
        assertTrue("the altitude stays", f.altitudeShown)
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

    /**
     * RECORD -941: the evening pieces stack in the right column, so the column is at least as wide as the widest piece, which
     * trims in the owner's order like anything else that needs room; with the grid reference on its own line the column holds
     * the stack alone. Replaces "a short evening line sits in the right column, a long one leaves it" (RECORD -934).
     */
    @Test
    fun `the widest evening piece is room the right column needs, even with the grid reference on its own line`() {
        // Pieces no wider than the grid reference change nothing.
        assertTrue(fit(widths(244, evening = 90)).kindInColumn)
        // A piece wider than the grid reference (120 against 100) needs 20 more: the kind gives way at 244.
        assertFalse(fit(widths(244, evening = 120)).kindInColumn)
        // Step 5 with a stack: left 32 + middle 32 + divider 10 + piece 60 + divider 10 = 144.
        val w = widths(150, evening = 60).copy(headingPx = 20, figureShortPx = 25)
        val f = cardColumnsFit(w, turnWhole = "sharp right 169°", turnShort = "169°")
        assertTrue("the grid reference on its own line", f.gridOwnLine)
        assertFalse("and the stack still fits its column", f.overflows)
        assertTrue("one dp less and it is reported", cardColumnsFit(w.copy(availablePx = 143), "sharp right 169°", "169°").overflows)
    }
}
