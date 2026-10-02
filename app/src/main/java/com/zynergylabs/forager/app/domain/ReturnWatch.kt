package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.ReturnToStartInfo
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.TrackRecordingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What [ReturnWatch] tells a screen. [trackId] is the recording it is for, or `null` when it is
 * for none. A screen copies [isReturning] and [isOffTrack] only when [trackId] is the track it is
 * itself recording; see `TrackRecordingViewModel`.
 *
 * [returnToStart] is the watch's own measurement, the one the decision was made on. The screen's
 * button shows a distance the ViewModel works out for itself from its own fixes (dispatch
 * 2026-09-28-400, Amendment 2, the planner's ruling on question 1), so nothing under `ui/` reads
 * this field today. It is here because the decision cannot be checked without it.
 */
data class ReturnWatchState(
    val trackId: String? = null,
    val isBegun: Boolean = false,
    val mode: TrackRecordingMode? = null,
    val isReturning: Boolean = false,
    val isOffTrack: Boolean = false,
    val returnToStart: ReturnToStartInfo? = null,
)

/**
 * Watches a walker on the way back to a recording's start, and delivers the off-track alert.
 *
 * It owns what used to be `TrackRecordingViewModel`'s: the returning flag, the start point, the
 * rolling window of distances, the cooldown, and the one [AlertDelivery.deliver] call. The
 * decision itself is unchanged and is still [DetectOffTrackUseCase]'s, over distances from
 * [ComputeReturnToStartUseCase].
 *
 * ## Why it exists (dispatch 2026-09-28-400, Amendment 2; the owner chose "Option B")
 *
 * While all of that was the ViewModel's, it died with the Activity. A recording swiped away from
 * recents went on being recorded by `TrackRecordingService`, and nothing would say "Off track"
 * again for it (see [AlertDelivery]'s header, which named the hole). This object is held by
 * `AppContainer`, for the life of the process, and driven by the service, which is the thing that
 * keeps the process alive and has the continuous stream of fixes. The ViewModel calls it for
 * Return and copies its state. Clearing the ViewModel no longer touches the decision.
 *
 * ## Who calls what
 *
 * - **The recording service** calls [begin] when a recording starts, [onFix] for **every raw fix**
 *   its collector receives, and [end] when the recording stops or the service is destroyed.
 *   Nothing else begins it, and nothing else feeds it.
 * - **The ViewModel** calls [startReturn] and [stopReturn] from the screen's Return control, and
 *   [setStartPoint] whenever the start point it shows changes. It collects [state].
 *
 * **Every raw fix, not the sampled points.** The ViewModel fed the decision every fix its own
 * listener received, about one a second, and "three readings" means three of those. The service's
 * sampler keeps far fewer. Feeding the kept points would change what the window spans, and what
 * off track means is not this class's to change (Amendment 2, ruling 2; plan task T21 owns it).
 *
 * ## A return asked for before the service has begun
 *
 * The service begins a moment after the Record tap: the ViewModel creates the track row,
 * `MainActivity` sends the start, the service handles it. A Return tapped inside that moment must
 * not be lost (Amendment 2, ruling 3). So [startReturn] and [setStartPoint] are accepted for a
 * track before [begin], and kept if [begin] then names the same track. If it names another, they
 * are dropped. Once begun for one track, calls that name another are refused.
 *
 * ## Two callers at once
 *
 * The service's collector runs on a background thread and the ViewModel on the main thread, so
 * unlike the ViewModel's version this has two writers. Everything the object holds is read and
 * written inside one lock, including the "has the cooldown passed" check and the write that
 * closes it, so two fixes arriving together cannot both decide to alert. [state] is a
 * `StateFlow`, which is safe to read from any thread. [AlertDelivery.deliver] is called after the
 * lock is released: it posts a notification and vibrates, and nothing here should wait on that.
 *
 * ## What it keeps
 *
 * Only the readings the check reads. [DetectOffTrackUseCase] looks at the last three, so three are
 * kept. The ViewModel's list grew by one reading per fix for the whole of a return and was never
 * trimmed. `ReturnWatchTest` runs both side by side on the same readings and requires the same
 * answer at every one.
 *
 * ## Left able to take the sundown alerts, which are not built
 *
 * The two sundown alerts (plan task T2) need the same things this has: a holder that outlives the
 * Activity, the service's lifetime, and [AlertDelivery]. Nothing for them is built here.
 */
class ReturnWatch(
    private val computeReturnToStart: ComputeReturnToStartUseCase,
    private val detectOffTrack: DetectOffTrackUseCase,
    private val alertDelivery: AlertDelivery,
    /** Injected so a test can fix the cooldown's clock. */
    private val currentTime: CurrentTimeProvider = SystemCurrentTimeProvider,
) {
    private val _state = MutableStateFlow(ReturnWatchState())
    val state: StateFlow<ReturnWatchState> = _state.asStateFlow()

    private val lock = Any()

    // Everything below is read and written only while holding [lock].
    private var trackId: String? = null
    private var begun = false
    private var returning = false
    private var offTrack = false
    private var start: TrackPoint? = null
    private var lastInfo: ReturnToStartInfo? = null

    // Oldest first. Never more than KEPT_READINGS long, and empty whenever no return is under way.
    private val recentReturnDistancesMeters = ArrayDeque<Double>()

    // When the last off-track alert was delivered. Cleared when a return stops, so a new return
    // attempt is never blocked by a cooldown left over from an earlier one.
    private var lastOffTrackAlertAtMillis: Long? = null

    /** How many readings are held right now. For tests: the claim "it keeps only what the check reads" is a number. */
    internal val keptReadingCount: Int get() = synchronized(lock) { recentReturnDistancesMeters.size }

    /**
     * The service has started recording [trackId]. Anything accepted early for this same track is
     * kept; anything held for another track is dropped.
     */
    fun begin(trackId: String, @Suppress("UNUSED_PARAMETER") mode: TrackRecordingMode) = synchronized(lock) {
        if (this.trackId != trackId) forget()
        this.trackId = trackId
        begun = true
        publish()
    }

    /**
     * The recording has ended. With a [trackId], ends only if the watch is for that track, so a
     * late stop for an old recording cannot end a new one. With `null`, ends whatever is begun:
     * the service's `onDestroy`, which has no track id left to name.
     */
    fun end(trackId: String?) = synchronized(lock) {
        if (trackId == null && !begun) return@synchronized
        if (trackId != null && trackId != this.trackId) return@synchronized
        forget()
        this.trackId = null
        begun = false
        publish()
    }

    /**
     * The walker is now heading back to the start of [trackId]: the only state the off-track
     * check runs in. Returns `false`, changing nothing, if the watch is begun for another track.
     * A new return starts with an empty window; the cooldown is left as it is, as it always was.
     */
    fun startReturn(trackId: String): Boolean = synchronized(lock) {
        if (!holdFor(trackId)) return@synchronized false
        recentReturnDistancesMeters.clear()
        returning = true
        offTrack = false
        publish()
        true
    }

    /** The return is over, the recording is not. Clears the window and the cooldown. Ignored for a track the watch is not for. */
    fun stopReturn(trackId: String) = synchronized(lock) {
        if (trackId != this.trackId) return@synchronized
        recentReturnDistancesMeters.clear()
        lastOffTrackAlertAtMillis = null
        returning = false
        offTrack = false
        publish()
    }

    /**
     * Where [trackId] started, as the screen shows it: the origin waypoint once it exists, the
     * first breadcrumb before that. The watch keeps the last one it was given, so it still has it
     * after the ViewModel that gave it is gone. Ignored if the watch is begun for another track.
     */
    fun setStartPoint(trackId: String, start: TrackPoint) = synchronized(lock) {
        if (!holdFor(trackId)) return@synchronized
        this.start = start
    }

    /**
     * One raw fix from the service's collector. Not a reading until the service has begun the
     * watch, and not measurable until there is a start point.
     *
     * Every measurable fix updates [ReturnWatchState.returnToStart]. While returning it also joins
     * the window, [DetectOffTrackUseCase] re-runs, and an [Alert] is delivered if the check reads
     * off track and [OFF_TRACK_ALERT_COOLDOWN_MILLIS] has passed since the last one. Not
     * edge-triggered: a drift that lasts keeps reminding once per cooldown, as it always did.
     */
    fun onFix(current: TrackPoint) {
        val alert = synchronized(lock) {
            if (!begun) return
            val start = start ?: return
            val info = computeReturnToStart(current, start)
            lastInfo = info
            var shouldAlert = false
            if (returning) {
                recentReturnDistancesMeters.addLast(info.distanceMeters)
                while (recentReturnDistancesMeters.size > KEPT_READINGS) recentReturnDistancesMeters.removeFirst()
                offTrack = detectOffTrack(recentReturnDistancesMeters)
                if (offTrack && cooldownHasPassed()) {
                    lastOffTrackAlertAtMillis = currentTime.nowEpochMillis()
                    shouldAlert = true
                }
            }
            publish()
            shouldAlert
        }
        if (alert) {
            // overridesSilence = false: owner ruling, 2026-09-11, reversing the original. Straying
            // is often deliberate, so off-track respects a phone the user silenced. See
            // AlertDelivery's own doc comment.
            alertDelivery.deliver(Alert(kind = AlertKind.OFF_TRACK, overridesSilence = false))
        }
    }

    /** Makes the watch be for [trackId] if it may be: already is, or not begun yet. Call with [lock] held. */
    private fun holdFor(trackId: String): Boolean {
        if (this.trackId == trackId) return true
        if (begun) return false
        forget()
        this.trackId = trackId
        return true
    }

    /** Drops everything held about a track. Call with [lock] held. */
    private fun forget() {
        returning = false
        offTrack = false
        start = null
        lastInfo = null
        recentReturnDistancesMeters.clear()
        lastOffTrackAlertAtMillis = null
    }

    private fun cooldownHasPassed(): Boolean {
        val last = lastOffTrackAlertAtMillis ?: return true
        return currentTime.nowEpochMillis() - last >= OFF_TRACK_ALERT_COOLDOWN_MILLIS
    }

    /** Call with [lock] held, so the published states are in the order the changes happened. */
    private fun publish() {
        _state.value = ReturnWatchState(trackId = trackId, isReturning = returning, isOffTrack = offTrack, returnToStart = lastInfo)
    }

    private companion object {
        /**
         * How many readings [DetectOffTrackUseCase] looks at. Its own constant is private, and
         * that class is not this dispatch's to change, so the number is repeated here.
         * `ReturnWatchTest`'s side-by-side test fails if the two ever differ.
         */
        const val KEPT_READINGS = 3

        /**
         * Long enough that a forager checking their pocket after one buzz has time to actually
         * look and self-correct before a second one, short enough that a sustained drift is still
         * a real, periodic reminder rather than a single easily-missed alert — an adjustable
         * assumption in the same spirit as [DetectOffTrackUseCase]'s own threshold, not a
         * data-derived constant (this project has no field data yet on what cooldown a real
         * forager would actually want). Moved here unchanged from `TrackRecordingViewModel`.
         */
        const val OFF_TRACK_ALERT_COOLDOWN_MILLIS = 120_000L
    }
}
