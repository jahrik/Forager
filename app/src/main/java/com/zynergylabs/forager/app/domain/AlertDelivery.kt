package com.zynergylabs.forager.app.domain

/**
 * Which alert is being delivered.
 */
enum class AlertKind {
    OFF_TRACK,

    /**
     * Thirty minutes before the leave-by time (dispatch 2026-09-28-516, RECORD -515). Passes
     * `overridesSilence = true`, as all three sundown alerts do — owner ruling, 2026-09-11: these
     * are the alerts about not being stranded after dark, so they are alarms. Anyone who does not
     * want them turns the feature off in settings.
     */
    HEADS_UP,

    /**
     * The leave-by time: sunset, minus the darkness margin, minus the walk back. Replaced the
     * turnaround alert ("Time to head back"): the alerts inform and never order the walker back
     * (the owner: "instead of 'turn around now' just tell them when sundown is").
     */
    LEAVE_BY,

    /** The sun has set. Same override as [HEADS_UP]. */
    SUNSET,
}

/**
 * The walk back as a sundown alert states it (dispatch 2026-09-28-516; the owner's copy rulings,
 * 2026-10-04). Only [About] — a measured estimate — may say anything about getting back.
 */
sealed interface WalkBack {
    /** A measured estimate ([ReturnWalkingTime.Estimate.isAtLeast] false). */
    data class About(val millis: Long) : WalkBack

    /** An estimate on thin data: a floor, never a promise. */
    data class AtLeast(val millis: Long) : WalkBack

    /** Withheld ([ReturnWalkingTime.Withheld]) or unreadable: no number is honest. */
    data object Unknown : WalkBack

    val millisOrNull: Long?
        get() = when (this) {
            is About -> millis
            is AtLeast -> millis
            Unknown -> null
        }
}

/**
 * What a sundown alert says: the sunset time, the walk back it was decided on, and the leave-by
 * time as a clock time ("start by 5:42 PM"), so a notification read late stays true (the owner,
 * 2026-10-05).
 */
data class SundownAlertDetail(
    val sunsetAtEpochMillis: Long,
    val walkBack: WalkBack,
    val leaveByAtEpochMillis: Long,
)

/**
 * One alert to deliver. [overridesSilence] is **deliberately a parameter of the call, not a
 * constant inside the delivery** (alert-delivery dispatch, owner decision): off-track is advisory
 * and the turnaround alert, when it exists, is safety, and overriding a phone the user silenced on
 * purpose is defensible for one and arguably rude for the other. The two must be able to differ
 * without the delivery changing shape. **Off-track now passes `false`, reversing the original ruling (owner,
 * 2026-09-11).** It first passed `true`, on the reasoning that someone who started track recording
 * and walked into the woods has opted into being told they have strayed. The owner's revised
 * reading: straying is often deliberate, so off-track is the kind of thing a person may reasonably
 * want to hear only if their notifications are audible, whereas [AlertKind.HEADS_UP], [AlertKind.LEAVE_BY] and
 * [AlertKind.SUNSET] are about not being stranded after dark and override silence. The parameter
 * existing per call is what made this a one-line reversal rather than a redesign. **Do not
 * hard-code this inside an implementation**; that is the one thing it exists to prevent.
 */
data class Alert(
    val kind: AlertKind,
    val overridesSilence: Boolean,
    /** The sundown alerts' content; `null` for off-track, which has none. */
    val sundown: SundownAlertDetail? = null,
)

/**
 * The owned seam through which anything in this app interrupts the user — a notification, a
 * vibration, whatever the platform implementation decides an [Alert] is made of. Domain and
 * ViewModel code call this; the Android implementation lives in `com.zynergylabs.forager.app.alert`.
 *
 * **Why this exists (alert-delivery dispatch).** The off-track alert used to be delivered from a
 * `LaunchedEffect` in `MainActivity`'s composition, keyed on a counter the ViewModel bumped. The
 * *decision* — `TrackRecordingViewModel.returnToStart`, fed by its own location collection in
 * `viewModelScope` — already ran with the Activity stopped, but Compose's window recomposer pauses
 * below `STARTED`, so the effect, and with it the notification and the vibration, waited for the
 * next resume. The owner walked off-track with the phone in a pocket and got the alert when the
 * screen came on. Delivery now goes through this interface, called directly from the decision, and
 * the composed path is gone: there is one call site and no counter.
 *
 * **A hole this does not close, recorded here so the next person finds it without archaeology.**
 * The decision lives in `TrackRecordingViewModel`, whose lifetime is the Activity's: it survives
 * the Activity being *stopped* (screen off, pocketed, app backgrounded — the case this dispatch
 * fixed, because `TrackRecordingService` keeps the process alive while recording), but **if the
 * task is swiped away while recording, the Activity is destroyed, the ViewModel is cleared, and
 * the off-track decision dies with it** — the returning flag, the rolling window and the cooldown
 * are all ViewModel state. The foreground service keeps recording points (`START_STICKY`), so the
 * track is fine; nothing will ever say "off track" again for that recording. This predates the
 * change (the decision was already in the ViewModel) and is not fixed by it. Closing it means
 * moving navigation ownership — returning state, start point, window, cooldown, and this call —
 * into the service, with the ViewModel as a mirror. That is a real hole in a safety feature and a
 * decision the owner has not yet made.
 *
 * **Closed for the off-track alert (dispatch 2026-09-28-400, Amendment 2; the owner chose
 * "Option B").** The two paragraphs above are left as they were written, as the record of the
 * hole. The decision, the returning flag, the start point, the window, the cooldown and the call
 * to [deliver] are now [ReturnWatch]'s. It is held by `AppContainer` and begun, fed and ended by
 * `TrackRecordingService`, and the ViewModel calls it and copies its state. So the one call site
 * is `ReturnWatch.onFix`, not `TrackRecordingViewModel.returnToStart`, and a swiped-away app
 * still alerts for as long as the service runs. What is still open: a reopened app does not yet
 * show the recording it left (Part 3 of that dispatch), and the two sundown alerts have no caller
 * (plan task T2).
 *
 * **The sundown alerts now have one (dispatch 2026-09-28-516):** [SundownWatch], held by
 * `AppContainer` and driven by `TrackRecordingService` the same way, and three of them, not two.
 */
fun interface AlertDelivery {
    fun deliver(alert: Alert)

    /**
     * The same delivery, saying what happened (dispatch 2026-09-28-451; the owner chose "Report
     * and catch"). `null` from a delivery that cannot say, which is every one but the Android one.
     */
    fun deliverReporting(alert: Alert): AlertDeliveryOutcome? {
        deliver(alert)
        return null
    }
}

/**
 * What an alert's delivery did: whether its notification was posted and its vibration issued, and
 * if not, why ([notificationProblem], [vibrationProblem]: a permission denied, or the exception's
 * class). For the Return record; nothing a walker sees.
 */
data class AlertDeliveryOutcome(
    val notificationPosted: Boolean,
    val notificationProblem: String?,
    val vibrated: Boolean,
    val vibrationProblem: String?,
)
