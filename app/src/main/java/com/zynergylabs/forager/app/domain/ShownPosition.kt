package com.zynergylabs.forager.app.domain

/**
 * Where the map, the HUD and the strip show the walker to be: dispatch 2026-09-28-510, the owner's
 * choices of RECORD -508 and their answers in the coder's window (the report,
 * `docs/navigation/2026-10-04-approximate-position-report.md`). The owner, verbatim: "We should have a
 * way to grab location just to establish a position, so users aren't stuck waiting for something."
 *
 * **Display only.** Nothing that acts on position reads this. "Arrived", the waypoint's line and a new
 * find's location all read the gated fix, `AvailabilityUiState.liveFix`, which keeps exactly its
 * meaning; recording, the off-track judge and
 * the route home run their own streams and never see the availability screen's state (the report's
 * item 1). An approximate reading is held beside the gated fix, never in it.
 *
 * The four cases, in the order they are decided:
 * - [Precise]: a fix the live gate accepted ([acceptLiveFix]) that is not yet lost
 *   ([LOST_AFTER_MILLIS]). Stale (30 s to 5 min) is still precise, dimmed as today: the owner chose
 *   "No GPS yet, or GPS lost", so a refused reading does not take over while the GPS fix is merely stale.
 * - [Approximate]: a fix the gate refused, newer than any precise one, not yet lost. Before the first
 *   GPS fix, and again once the last one is lost.
 * - [LastKnown]: nothing current. The newest position there is, of any kind: the held GPS fix, an
 *   approximate reading gone stale, or the platform's own last known location; shown greyed with its
 *   age (the owner: "Last known position, marked old").
 * - [None]: nothing at all.
 */
sealed interface ShownPosition {
    data class Precise(val fix: LocationFix.Update) : ShownPosition
    data class Approximate(val fix: LocationFix.Update) : ShownPosition
    data class LastKnown(val fix: LocationFix.Update) : ShownPosition
    data object None : ShownPosition
}

/** The fix a shown position is drawn at, or `null` for [ShownPosition.None]. */
val ShownPosition.fixOrNull: LocationFix.Update?
    get() = when (this) {
        is ShownPosition.Precise -> fix
        is ShownPosition.Approximate -> fix
        is ShownPosition.LastKnown -> fix
        ShownPosition.None -> null
    }

/**
 * See [ShownPosition]. [precise] is the gated fix as held (`AvailabilityUiState.liveFix`), [approximate]
 * the newest fix the gate refused, [lastKnown] the platform's own last known location; any may be null.
 */
fun shownPosition(
    precise: LocationFix.Update?,
    approximate: LocationFix.Update?,
    lastKnown: LocationFix.Update?,
    nowEpochMillis: Long,
): ShownPosition {
    if (precise != null && !precise.isLost(nowEpochMillis)) return ShownPosition.Precise(precise)
    // No test of "newer than the GPS fix" is needed: past this line the GPS fix is lost or absent, and a
    // reading older than a lost fix is lost too, by the same five minutes. (Revert check r02 found an
    // explicit one changed nothing, and it was removed.)
    if (approximate != null && !approximate.isLost(nowEpochMillis)) return ShownPosition.Approximate(approximate)
    val newest = listOfNotNull(precise, approximate, lastKnown).maxByOrNull { it.timestampEpochMillis } ?: return ShownPosition.None
    return ShownPosition.LastKnown(newest)
}

/** Lost by the HUD's own rule ([fixFreshness], five minutes), so the map, the strip and the HUD agree on when. */
private fun LocationFix.Update.isLost(nowEpochMillis: Long): Boolean = fixFreshness(ageMillis(nowEpochMillis)) == FixFreshness.LOST

/**
 * How long until [shownPosition] next changes with nothing arriving: when the held GPS fix, or the
 * approximate reading, turns lost. `null` when neither will, so a caller's clock can stop until a new
 * fix comes. Always more than 0 when not `null`.
 */
fun millisUntilShownPositionChanges(precise: LocationFix.Update?, approximate: LocationFix.Update?, nowEpochMillis: Long): Long? =
    listOfNotNull(precise, approximate).map { LOST_AFTER_MILLIS - it.ageMillis(nowEpochMillis) }.filter { it > 0 }.minOrNull()

/**
 * The position shown **in place of GPS**, or `null` when today's GPS display applies: an approximate
 * reading, or a last known one that is not the held GPS fix itself. A held GPS fix that is lost keeps
 * today's display ("No fix for N min" in the HUD, its coordinates in the strip, MapLibre's own grey
 * dot); only a reading other than it is shown as approximate or last known. One rule for the map's
 * label, the strip and the HUD, so the three cannot disagree.
 */
fun ShownPosition.inPlaceOfGps(liveFix: LocationFix.Update?): ShownPosition? = when (this) {
    is ShownPosition.Approximate -> this
    is ShownPosition.LastKnown -> takeIf { fix != liveFix }
    is ShownPosition.Precise, ShownPosition.None -> null
}
