package com.zynergylabs.forager.app.ui.availability

/**
 * Dispatch 2026-10-12-06 (RECORD -932, -934, -935): the navigation card's three columns, and what gives way when they do not
 * fit the card's width. The pure half: every choice is a function of measured widths, so a wrong order is a pinned-literal
 * test failure (`RoundedMapCardFitTest`), not a visual one.
 *
 * The columns, as the owner's mockup and step path draw them:
 * - left: the distance figure, its kind ("by trail", "straight") beside it; under it the turn words, then the needle;
 * - middle: "Alt 344 ft"; under it the compass arrow, then the heading ("▲ 134° SE");
 * - right: the grid reference; under it a short evening line, when one fits there.
 *
 * The order, the owner's (RECORD -934, "Trim extras when tight (Recommended)"): first the kind word goes from beside the
 * figure (it then leads the status line under the columns, as RECORD -713 already had it, so nothing is lost: RECORD -935,
 * "Yes, keep everything"); then the turn words shrink to the degrees and the needle ("169° ▲"); then the "Alt" label goes. The
 * figure, the compass and the grid reference stay whole.
 *
 * Two steps past the owner's order, the coder's, reached only where the three above are not enough (reported, see the
 * dispatch's report): the degrees go and the needle stays alone; then the altitude's value goes. Neither touches the three
 * the owner named. A compass status that is words, not a reading ("Compass unavailable · north up"), takes the crossed-out
 * compass ([StripHeadingStatusIcon], RECORD -735's icon) in the middle column where its words do not fit there (RECORD -934).
 *
 * The evening line never makes anything else give way: it sits in the right column only where it fits in what the right
 * column has left; otherwise it is a full-width line under the columns (RECORD -934, "Moves to a full-width line").
 */
internal data class CardColumnsFit(
    /** The turn as drawn before the needle: the whole words, the degrees alone, or "" (the needle alone). */
    val turnText: String,
    /** Whether the kind sits beside the figure; false while a kind exists means it leads the status line. */
    val kindInColumn: Boolean,
    /** Whether "Alt" is drawn before the altitude. */
    val altitudeLabel: Boolean,
    /** Whether the altitude is drawn at all (false only at the last step, or with none to draw). */
    val altitudeShown: Boolean,
    /** Whether the compass status is drawn as the crossed-out compass rather than its words. */
    val headingAsIcon: Boolean,
    /** Which of the evening lines sit in the right column, under the grid reference. */
    val eveningInColumn: EveningInColumn,
    /** Whether even the last step left the columns wider than the room ([CardColumnsWidths.availablePx]). */
    val overflows: Boolean = false,
)

/** Which evening lines the right column holds. The rest are full-width lines under the columns. */
internal enum class EveningInColumn { NONE, SUNDOWN, BACK_BY, BOTH }

/**
 * The measured widths the fit is decided on, in pixels. `null` for a part that is not there in this state. The figure, the
 * kind, the heading and the coordinates are the texts the display would draw; [turnWholePx] and [turnShortPx] the turn's two
 * forms (`null` where the form does not exist).
 */
internal data class CardColumnsWidths(
    /** What the three columns and the two dividers between them may take: the card less its padding and the X and ⋮ column. */
    val availablePx: Int,
    /** Each divider with the gap on either side of it. */
    val dividerPx: Int,
    /** The gap between parts inside a column: the figure and its kind, the words and the needle, the label and its value. */
    val gapPx: Int,
    /** The needle's and the compass arrow's square. */
    val arrowPx: Int,
    val figurePx: Int,
    val kindPx: Int?,
    val turnWholePx: Int?,
    val turnShortPx: Int?,
    val altitudeLabelPx: Int?,
    val altitudePx: Int?,
    /** The heading's text: a reading ("134° SE") or a status's words. */
    val headingPx: Int,
    /** True when the heading is a status in words rather than a reading; only a status may become the crossed-out icon. */
    val headingIsStatus: Boolean,
    /** The crossed-out compass's square. */
    val headingIconPx: Int,
    val coordinatesPx: Int?,
    val sundownPx: Int?,
    val backByPx: Int?,
    /** The " · " between two evening parts sharing the right column. */
    val eveningSeparatorPx: Int,
)

private data class Step(val kindIn: Boolean, val turn: TurnForm, val altLabel: Boolean, val altShown: Boolean)
private enum class TurnForm { WHOLE, SHORT, NONE }

internal fun cardColumnsFit(w: CardColumnsWidths, turnWhole: String, turnShort: String?): CardColumnsFit {
    fun turnPx(form: TurnForm): Int = when (form) {
        TurnForm.WHOLE -> w.turnWholePx?.let { it + w.gapPx + w.arrowPx } ?: w.arrowPx
        TurnForm.SHORT -> (w.turnShortPx ?: w.turnWholePx)?.let { it + w.gapPx + w.arrowPx } ?: w.arrowPx
        TurnForm.NONE -> w.arrowPx
    }
    fun leftPx(s: Step): Int {
        val top = w.figurePx + if (s.kindIn && w.kindPx != null) w.gapPx + w.kindPx else 0
        return maxOf(top, turnPx(s.turn))
    }
    fun middlePx(s: Step, icon: Boolean): Int {
        val top = if (s.altShown && w.altitudePx != null) {
            w.altitudePx + if (s.altLabel && w.altitudeLabelPx != null) w.altitudeLabelPx + w.gapPx else 0
        } else {
            0
        }
        val bottom = if (icon) w.headingIconPx else w.arrowPx + w.gapPx + w.headingPx
        return maxOf(top, bottom)
    }
    fun rightPx(): Int = w.coordinatesPx ?: 0
    fun total(s: Step, icon: Boolean) = leftPx(s) + middlePx(s, icon) + rightPx() + 2 * w.dividerPx
    val ownersOrder = listOf(
        Step(kindIn = true, turn = TurnForm.WHOLE, altLabel = true, altShown = true),
        Step(kindIn = false, turn = TurnForm.WHOLE, altLabel = true, altShown = true),
        Step(kindIn = false, turn = TurnForm.SHORT, altLabel = true, altShown = true),
        Step(kindIn = false, turn = TurnForm.SHORT, altLabel = false, altShown = true),
    )
    val pastTheOrder = listOf(
        Step(kindIn = false, turn = TurnForm.NONE, altLabel = false, altShown = true),
        Step(kindIn = false, turn = TurnForm.NONE, altLabel = false, altShown = false),
    )
    // The heading's words are tried through the owner's order first; a status gives way to its icon before anything past it.
    val tries = buildList {
        ownersOrder.forEach { add(it to false) }
        if (w.headingIsStatus) ownersOrder.forEach { add(it to true) }
        pastTheOrder.forEach { add(it to w.headingIsStatus) }
    }
    val chosen = tries.firstOrNull { (s, icon) -> total(s, icon) <= w.availablePx }
    val (step, icon) = chosen ?: tries.last()
    val turnText = when (step.turn) {
        TurnForm.WHOLE -> turnWhole
        TurnForm.SHORT -> turnShort ?: turnWhole
        TurnForm.NONE -> ""
    }
    val rightRoom = w.availablePx - leftPx(step) - middlePx(step, icon) - 2 * w.dividerPx
    val evening = when {
        w.sundownPx != null && w.backByPx != null && w.sundownPx + w.eveningSeparatorPx + w.backByPx <= rightRoom -> EveningInColumn.BOTH
        w.sundownPx != null && w.sundownPx <= rightRoom -> EveningInColumn.SUNDOWN
        w.sundownPx == null && w.backByPx != null && w.backByPx <= rightRoom -> EveningInColumn.BACK_BY
        else -> EveningInColumn.NONE
    }
    return CardColumnsFit(
        turnText = turnText,
        kindInColumn = step.kindIn && w.kindPx != null,
        altitudeLabel = step.altLabel && w.altitudeLabelPx != null,
        altitudeShown = step.altShown && w.altitudePx != null,
        headingAsIcon = icon,
        eveningInColumn = evening,
        overflows = chosen == null,
    )
}
