package com.zynergylabs.forager.app.diagnostics.walklog

/** STUB for the failing-test commit (dispatch 2026-09-28-532): every function returns nothing yet. */
object WalkLogFormat {
    fun fix(atNanos: Long, fix: FixRecord): String = ""
    fun gnssStatus(atNanos: Long, satellites: List<SatelliteRecord>): List<String> = emptyList()
    fun gnssMeasurements(atNanos: Long, clock: GnssClockRecord, measurements: List<GnssMeasurementRecord>): List<String> = emptyList()
    fun gnssMeasurementsStatus(atNanos: Long, status: Int): String = ""
    fun sensor(atNanos: Long, reading: SensorRecord): String = ""
    fun sensorAccuracy(atNanos: Long, kind: WalkLogSensorKind, accuracy: Int): String = ""
    fun battery(atNanos: Long, wallClockMillis: Long, battery: BatteryRecord?, freeBytes: Long): String = ""
    fun unsupported(atNanos: Long, what: String, reason: String): String = ""
    fun stopped(atNanos: Long, reason: String, detail: String): String = ""
    fun end(atNanos: Long, reason: String): String = ""
    fun header(startWallClockMillis: Long, startNanos: Long, facts: HeaderFacts, capabilities: GnssCapabilitiesRecord?): List<String> = emptyList()
}
