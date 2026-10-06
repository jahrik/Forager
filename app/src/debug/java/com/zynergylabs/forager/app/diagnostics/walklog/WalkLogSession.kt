package com.zynergylabs.forager.app.diagnostics.walklog

/** STUB for the failing-test commit (dispatch 2026-09-28-532). */
class WalkLogSession(
    private val platform: WalkLogPlatform,
    private val writer: WalkLogWriter,
) : WalkLogEvents {
    val isActive: Boolean get() = false

    fun start() = Unit
    fun stop(reason: String = REASON_RECORDING_STOPPED) = Unit

    override fun onFix(fix: FixRecord) = Unit
    override fun onGnssStatus(satellites: List<SatelliteRecord>) = Unit
    override fun onGnssMeasurements(clock: GnssClockRecord, measurements: List<GnssMeasurementRecord>) = Unit
    override fun onGnssMeasurementsStatus(status: Int) = Unit
    override fun onSensor(reading: SensorRecord) = Unit
    override fun onSensorAccuracy(kind: WalkLogSensorKind, accuracy: Int) = Unit

    companion object {
        const val REASON_RECORDING_STOPPED = "recording-stopped"
    }
}
