package com.zynergylabs.forager.app.ui.map.fanout

/** STUB (tests-first commit): geometry and stack detection are not built yet. */

const val FAN_TOUCH_DP = 48f
const val FAN_RING_MAX = 8
const val FAN_DURATION_MS = 400

data class FanKey(val layerId: String, val featureId: String)

data class ProbedMarker(val key: FanKey, val lat: Double, val lng: Double, val xPx: Float, val yPx: Float)

data class FanOffset(val xDp: Float, val yDp: Float)

data class FanMember(
    val key: FanKey,
    val lat: Double,
    val lng: Double,
    val trueXDp: Float,
    val trueYDp: Float,
    val offset: FanOffset,
)

fun stackOf(tapped: ProbedMarker, nearby: List<ProbedMarker>, density: Float): List<ProbedMarker> = listOf(tapped)

fun fanOffsets(count: Int): List<FanOffset> = List(count) { FanOffset(0f, 0f) }

fun memberPositionDp(member: FanMember, progress: Float): FanOffset = FanOffset(member.trueXDp, member.trueYDp)

fun fanMemberAt(members: List<FanMember>, progress: Float, xDp: Float, yDp: Float): FanMember? = null
