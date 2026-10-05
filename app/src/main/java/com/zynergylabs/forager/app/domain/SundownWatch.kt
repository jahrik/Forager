package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng
import com.zynergylabs.forager.app.domain.model.SundownCountdown
import com.zynergylabs.forager.app.domain.model.Track
import com.zynergylabs.forager.app.domain.model.TrackPoint
import com.zynergylabs.forager.app.domain.model.Waypoint
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Delivers a recording's three sundown alerts: the heads-up, the leave-by and the sunset alert
 * (dispatch 2026-09-28-516, plan tasks T2 and T13, the owner's choices in RECORD -513 to -515).
 *
 * ## Why it lives here, and who drives it
 *
 * The countdown on screen rides `TrackRecordingViewModel`'s scope, which dies when the app is
 * swiped away. This is held by `AppContainer` for the life of the process, as [ReturnWatch] is,
 * and driven by `TrackRecordingService`, which is what keeps the process alive while recording:
 * [begin] when a recording starts, [onFix] for every raw fix its collector receives, [tick] on its
 * own timer (and once at the first position), [end] when the recording stops or the service is
 * destroyed. Nothing else drives it. The screen does not read it (the countdown row is plan task
 * T3); it could, rather than computing twice.
 *
 * ## What each tick works out
 *
 * - **Sunset**, from the newest fix of **any** kind, GPS or network. The owner, 2026-10-04:
 *   "Sunset moves about 4 s per km, and GPS-only would mean no sundown alert at all under canopy
 *   or indoors." An owner-approved exception to -510's rule that an approximate position never
 *   decides anything, for the sunset time only. The last known position joins this once -510's
 *   `LastKnownLocationSource` is on `main` (the owner's order: -510 merges first).
 * - **The walk back**, from [returnWalkingTime] over the stored track (read through the read seam,
 *   so the network-fix exclusion and its counts are the ones the estimate's "at least" rules
 *   expect), its origin waypoint, and the newest **GPS** fix only. GPS is told from network by
 *   [isNetworkProviderFix], the rule the track read and the off-track judge use, so the current
 *   fix and the track agree; the raw stream carries no provider. Its freshness is [fixFreshness]
 *   of its age, so a GPS fix older than five minutes withholds the estimate. The hop band is
 *   carried between ticks, as [returnWalkingTime] asks.
 * - **The decision**, [DecideSundownAlertUseCase], with what has already fired.
 *
 * **Failure direction of the timestamp rule** (recorded in the report against RECORD -519): on a
 * phone whose GPS stamps milliseconds, every GPS fix reads as network, the walk back reads
 * "unknown", and the leave-by time falls back to sunset minus the margin. Safe in direction: no
 * confident figure it does not have.
 *
 * ## When nothing more fires
 *
 * Once per recording each. After [end]. With the alerts turned off
 * ([SundownPreferencesRepository.getAlertsEnabled]). And, the owner's option B, once the walker
 * is heading back (Return tapped) and a fresh GPS fix is within [hasArrived]'s reach of the start
 * (the origin waypoint, or the track's first point before there is one): from then on the
 * recording is over as far as these alerts are concerned, even if the walker sets out again.
 * Without Return, only stopping the recording silences them.
 *
 * ## What it keeps, and for how long
 *
 * The fired set, the hop band, the two newest fixes and the arrived flag, for one recording, in
 * memory. Not in DataStore: a process death ends the recording too (a sticky restart arrives with
 * no intent and records nothing), so there is nothing to carry across one.
 *
 * ## Threads
 *
 * [onFix] is called from the service's collector and [tick] from its timer, on different
 * coroutines. The fields are read and written under [lock]; ticks are serialised by [tickMutex],
 * so two cannot decide the same alert. [AlertDelivery] is called outside [lock].
 */
class SundownWatch(
    private val alertDelivery: AlertDelivery,
    private val clock: CurrentTimeProvider,
    private val preferences: SundownPreferencesRepository,
    /** The track through the read seam ([TrackRepository.getById]). */
    private val readTrack: suspend (String) -> Result<Track?>,
    /** A waypoint by id ([WaypointRepository.getById]), for the track's origin. */
    private val readWaypoint: suspend (String) -> Result<Waypoint?>,
    /** Whether the walker is heading back on this track ([ReturnWatch]'s state). */
    private val isReturning: (String) -> Boolean,
    private val errorLog: ErrorLog,
    private val computeCountdown: ComputeSundownCountdownUseCase = ComputeSundownCountdownUseCase(),
    private val decide: DecideSundownAlertUseCase = DecideSundownAlertUseCase(),
) {
    private val lock = Any()
    private val tickMutex = Mutex()

    // Read and written only while holding [lock].
    private var trackId: String? = null
    private var newestFix: TrackPoint? = null
    private var newestGpsFix: TrackPoint? = null
    private var fired: Set<SundownAlert> = emptySet()
    private var hopBand: HopBand = HopBand.NONE
    private var arrived = false

    /** The service has started recording [trackId]. Everything from any earlier recording is dropped. */
    fun begin(trackId: String) = synchronized(lock) {
        forget()
        this.trackId = trackId
    }

    /**
     * The recording has ended. With a [trackId], only if it is the one being watched, so a late
     * stop for an old recording cannot end a new one. With `null`, whatever is being watched: the
     * service's `onDestroy`.
     */
    fun end(trackId: String?) = synchronized(lock) {
        if (trackId != null && trackId != this.trackId) return@synchronized
        forget()
    }

    /**
     * One raw fix from the service's collector. Ignored when nothing is being watched. Returns
     * `true` for the first fix of a recording, so the service can evaluate at once rather than at
     * its next tick: a recording started past the leave-by time alerts as soon as it has a place.
     */
    fun onFix(fix: TrackPoint): Boolean = synchronized(lock) {
        if (trackId == null) return@synchronized false
        val first = newestFix == null
        newestFix = fix
        if (!fix.isNetworkProviderFix()) newestGpsFix = fix
        first
    }

    /** One evaluation. Delivers at most one alert. See the class header for what it works out. */
    suspend fun tick() = tickMutex.withLock {
        val (id, anyFix, gpsFix, alreadyFired, band) = synchronized(lock) {
            val id = trackId
            if (id == null || arrived) return@withLock
            Snapshot(id, newestFix ?: return@withLock, newestGpsFix, fired, hopBand)
        }

        val enabled = preferences.getAlertsEnabled().getOrElse { error ->
            errorLog.w(TAG, "Couldn't read whether the sundown alerts are on; they stay on, the default.", error)
            true
        }
        if (!enabled) return@withLock

        val now = clock.nowEpochMillis()
        val marginMinutes = preferences.getDarknessMarginMinutes().getOrElse { error ->
            errorLog.w(TAG, "Couldn't read the darkness margin; using the default of $DEFAULT_DARKNESS_MARGIN_MINUTES minutes.", error)
            DEFAULT_DARKNESS_MARGIN_MINUTES
        }
        val countdown = computeCountdown(now, LatLng(anyFix.lat, anyFix.lng), anyFix.timestampEpochMillis, marginMinutes * 60_000L)
        if (countdown !is SundownCountdown.Known) return@withLock

        val track = readTrack(id).getOrElse { error ->
            errorLog.w(TAG, "Couldn't read track '$id' for the walk back; it is unknown for this evaluation.", error)
            null
        }
        val origin = track?.originWaypointId?.let { originId ->
            readWaypoint(originId).getOrElse { error ->
                errorLog.w(TAG, "Couldn't read the origin waypoint '$originId'; the walk back is measured to the track's first point.", error)
                null
            }
        }
        val freshness = gpsFix?.let { fixFreshness(now - it.timestampEpochMillis) } ?: FixFreshness.LOST
        val current = gpsFix?.let { LatLng(it.lat, it.lng) }

        if (track != null && gpsFix != null && current != null && freshness != FixFreshness.LOST && isReturning(id)) {
            val start = origin?.let { LatLng(it.lat, it.lng) } ?: track.points.firstOrNull()?.let { LatLng(it.lat, it.lng) }
            if (start != null && hasArrived(GeoDistance.metersBetween(current, start), gpsFix.accuracyMeters)) {
                synchronized(lock) { if (trackId == id) arrived = true }
                return@withLock
            }
        }

        val estimate = track?.let { returnWalkingTime(it, origin, current, freshness, band) }
        val walkBack = when (estimate) {
            is ReturnWalkingTime.Estimate -> if (estimate.isAtLeast) WalkBack.AtLeast(estimate.walkingMillis) else WalkBack.About(estimate.walkingMillis)
            is ReturnWalkingTime.Withheld, null -> WalkBack.Unknown
        }
        val decision = decide(countdown, walkBack.millisOrNull, alreadyFired)

        val stillThisRecording = synchronized(lock) {
            if (trackId != id) return@synchronized false
            fired = decision.spent
            if (estimate is ReturnWalkingTime.Estimate) hopBand = estimate.path.hopBand
            true
        }
        val kind = when (decision.fire) {
            SundownAlert.HEADS_UP -> AlertKind.HEADS_UP
            SundownAlert.LEAVE_BY -> AlertKind.LEAVE_BY
            SundownAlert.SUNSET -> AlertKind.SUNSET
            null -> return@withLock
        }
        if (!stillThisRecording) return@withLock
        val outcome = alertDelivery.deliverReporting(
            Alert(kind, overridesSilence = true, sundown = SundownAlertDetail(countdown.sunsetAtEpochMillis, walkBack)),
        )
        if (outcome != null && (!outcome.notificationPosted || !outcome.vibrated)) {
            errorLog.w(
                TAG,
                "The $kind alert was only partly delivered: notification ${outcome.notificationProblem ?: "posted"}, vibration ${outcome.vibrationProblem ?: "issued"}.",
                IllegalStateException("partial delivery of $kind"),
            )
        }
    }

    private fun forget() {
        trackId = null
        newestFix = null
        newestGpsFix = null
        fired = emptySet()
        hopBand = HopBand.NONE
        arrived = false
    }

    private data class Snapshot(
        val trackId: String,
        val anyFix: TrackPoint,
        val gpsFix: TrackPoint?,
        val fired: Set<SundownAlert>,
        val hopBand: HopBand,
    )

    private companion object {
        const val TAG = "SundownWatch"
    }
}
