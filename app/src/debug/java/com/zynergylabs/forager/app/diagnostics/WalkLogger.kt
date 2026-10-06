package com.zynergylabs.forager.app.diagnostics

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log
import com.zynergylabs.forager.app.ForagerApplication
import com.zynergylabs.forager.app.diagnostics.walklog.AndroidWalkLogPlatform
import com.zynergylabs.forager.app.diagnostics.walklog.WalkLogSession
import com.zynergylabs.forager.app.diagnostics.walklog.WalkLogWriter
import com.zynergylabs.forager.app.diagnostics.walklog.WalkLoggerSwitch
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking

/**
 * The walk logger, **debug builds only** (dispatch 2026-09-28-532): while a track recording runs and
 * the Diagnostics screen's "Walk logger" switch is on (Amendment 3, RECORD -560), everything the
 * phone senses is written to one plain-text file per recording, in the app's external files
 * directory under [DIRECTORY_NAME]. Nothing changes for the walker: it reads the phone and acts on
 * nothing. The release twin in `src/release` has the same calls and does nothing.
 *
 * **Where it starts and stops.** `TrackRecordingService` calls [onRecordingStarted] when a recording
 * starts and [onRecordingStopped] when it stops or the service is destroyed (RECORD -559, choice 1).
 * The process lives as long as the service does, so a swipe-away from recents does not end the log.
 * A process killed outright ends it where it stood, at most [WalkLogWriter.FLUSH_INTERVAL_NANOS]
 * short; nothing restarts it on a sticky restart, because the service starts nothing then either.
 *
 * **The switch is read when a recording starts.** Turning it on mid-recording logs from the next one.
 *
 * **Wake lock** (RECORD -559, choice 3): a partial wake lock is held while the log runs, so the
 * processor stays awake to take sensor readings with the screen off; let go when the log ends, stops
 * early (storage, a write error) or never started. The desk run measures what it costs.
 *
 * Everything runs on the logger's own thread, never the recording's: a failure here is logged and
 * ends the log; it cannot reach the recording.
 */
class WalkLogger private constructor(private val app: Context) {

    private val thread = HandlerThread(THREAD_NAME).apply { start() }
    private val handler = Handler(thread.looper)

    @Volatile private var session: WalkLogSession? = null
    @Volatile private var file: File? = null
    private var wakeLock: PowerManager.WakeLock? = null

    val isLogging: Boolean get() = session?.isActive == true

    /** The current or last walk log file, or null when none was started in this process. */
    val currentFile: File? get() = file

    fun onRecordingStarted(trackId: String) {
        handler.post { startOnThread(trackId) }
    }

    fun onRecordingStopped() {
        handler.post { stopOnThread() }
    }

    private fun startOnThread(trackId: String) {
        if (session?.isActive == true) return
        try {
            if (!switchIsOn()) return
            val directory = directory(app)
            val startWall = System.currentTimeMillis()
            val logFile = File(directory, fileName(startWall))
            val override = freeBytesOverride
            // Free space is read from the app's files root, which always exists: a folder that does
            // not exist yet reports 0 bytes free, which stopped the first recording after every fresh
            // install as storage-low (RECORD -569). walklogs/ is created inside it by the writer.
            val root = directory.parentFile ?: directory
            val platform = AndroidWalkLogPlatform(app, handler) { override?.invoke(directory) ?: root.usableSpace }
            val writer = WalkLogWriter(logFile, platform::freeBytes, platform::elapsedRealtimeNanos)
            val newSession = WalkLogSession(platform, writer, onStoppedEarly = ::releaseWakeLock)
            file = logFile
            session = newSession
            acquireWakeLock()
            newSession.start()
            if (newSession.isActive) {
                Log.i(TAG, "Walk log started for track '$trackId': ${logFile.name}")
            } else {
                releaseWakeLock()
                Log.w(TAG, "Walk log for track '$trackId' did not start (${writer.stoppedReason}); the recording carries on.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "The walk log for track '$trackId' failed to start; the recording carries on.", e)
            session?.let { runCatching { it.stop("start-failed") } }
            releaseWakeLock()
        }
    }

    private fun stopOnThread() {
        try {
            session?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "The walk log did not end cleanly.", e)
        } finally {
            releaseWakeLock()
        }
    }

    /** Off when the switch cannot be read, logged: the logger is an opt-in instrument, so failing toward off records normally. */
    private fun switchIsOn(): Boolean {
        val switch = (app as? ForagerApplication)?.container?.forecastCellStore as? WalkLoggerSwitch
        if (switch == null) {
            Log.w(TAG, "The debug container's diagnostics store is not the walk logger switch; not logging.")
            return false
        }
        return runBlocking { switch.isWalkLoggerEnabled() }.getOrElse { error ->
            Log.w(TAG, "Couldn't read the walk logger switch; not logging.", error)
            false
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire(WAKE_LOCK_CEILING_MILLIS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    companion object {
        const val DIRECTORY_NAME = "walklogs"
        const val WAKE_LOCK_TAG = "Forager:WalkLogger"
        private const val THREAD_NAME = "WalkLogger"
        private const val TAG = "WalkLog"

        /**
         * A ceiling, not the expected length: the lock is let go when the log ends. It bounds the
         * battery a lock left behind by a bug could cost. Longer than any planned walk.
         */
        private const val WAKE_LOCK_CEILING_MILLIS = 12L * 60 * 60 * 1000

        private val FILE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)

        /** Tests only: stands in for the free space where the log is written. */
        @Volatile internal var freeBytesOverride: ((File) -> Long)? = null

        @Volatile private var instance: WalkLogger? = null

        /** The process's one logger. A new application object (each Robolectric test has one) gets a new logger. */
        fun of(context: Context): WalkLogger {
            val app = context.applicationContext
            instance?.let { if (it.app === app) return it }
            synchronized(this) {
                instance?.let { if (it.app === app) return it }
                instance?.thread?.quitSafely()
                return WalkLogger(app).also { instance = it }
            }
        }

        /** `walklog-<start, UTC>.txt`, by the phone's clock. `.gitignore` keeps the pattern out of the repository. */
        fun fileName(startEpochMillis: Long): String = "walklog-${FILE_TIME.format(Instant.ofEpochMilli(startEpochMillis))}.txt"

        /** On the phone: `Android/data/<package>/files/walklogs/`, falling back to internal storage when external is unavailable. */
        fun directory(context: Context): File = File(context.getExternalFilesDir(null) ?: context.filesDir, DIRECTORY_NAME)
    }
}
