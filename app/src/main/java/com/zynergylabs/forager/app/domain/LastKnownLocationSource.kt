package com.zynergylabs.forager.app.domain

/**
 * The platform's own last known position, without Play services — dispatch 2026-09-28-510, the
 * owner's choice "Last known position, marked old": with no live reading at all, the map shows where
 * the phone last was, greyed, with its age, until anything newer arrives.
 *
 * Owned here, like [LocationTracker] and [LocationProvider], so the domain and the screen depend on
 * this interface, never on `android.location`. Read once each time the screen starts collecting live
 * fixes; it never waits for a new position, and what it returns never becomes the gated fix
 * (`AvailabilityUiState.liveFix`): it is shown, never acted on (see [ShownPosition]).
 */
fun interface LastKnownLocationSource {
    /** The newest position any provider still holds, or `null` when none holds one or permission is missing. */
    fun lastKnown(): LocationFix.Update?
}

/** No last known position: the default for every caller that has none to give, tests included. */
val NoLastKnownLocation = LastKnownLocationSource { null }
