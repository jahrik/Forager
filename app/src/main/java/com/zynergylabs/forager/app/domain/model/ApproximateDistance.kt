package com.zynergylabs.forager.app.domain.model

import kotlin.math.roundToInt

/**
 * A distance measured from an **approximate** position, dispatch 2026-09-28-510: the HUD's "≈ …" for
 * a far target (the owner, RECORD -508). Always marked "≈", rounded to a step no finer than the
 * reading's accuracy, at every distance.
 *
 * Its own function, not [formatDistanceWithAccuracy], for two reasons found in that function while
 * verifying this dispatch: its coarsest steps (500 m, 1,000 ft) are sized for fixes the live gate lets
 * through (50 m at most), so an accuracy beyond them finds no step and `first { }` throws (a 600 m
 * reading with a target 800 m away); and at or above 1 km / a quarter mile it drops the "≈", which an
 * approximate reading must never do. That function's output is unchanged.
 *
 * The steps run 1, 2, 5 in each decade, so the step chosen is at most 2.5 times the accuracy; a
 * target is only "far" beyond twice the accuracy (the HUD's rule), so a far distance never rounds to
 * zero. A distance that would is shown as one step rather than "≈ 0".
 */
fun formatApproximateDistance(distanceMeters: Double, accuracyMeters: Float, unit: DistanceUnit): String {
    val accuracy = accuracyMeters.toDouble()
    return when (unit) {
        DistanceUnit.KILOMETERS -> {
            val step = APPROXIMATE_METRE_STEPS.firstOrNull { it >= accuracy } ?: APPROXIMATE_METRE_STEPS.last()
            val rounded = roundedToStep(distanceMeters, step)
            when {
                rounded < 1_000.0 -> "≈ ${rounded.roundToInt()} m"
                step < 1_000.0 -> "≈ ${"%.1f".format(rounded / 1_000.0)} km"
                else -> "≈ ${(rounded / 1_000.0).roundToInt()} km"
            }
        }
        DistanceUnit.MILES -> {
            // Feet below a quarter mile, as formatDistanceMeters counts them, while a step in feet covers the accuracy.
            val feet = distanceMeters * APPROXIMATE_FEET_PER_METER
            val footStep = APPROXIMATE_FOOT_STEPS.firstOrNull { it >= accuracy * APPROXIMATE_FEET_PER_METER }
            val roundedFeet = footStep?.let { roundedToStep(feet, it) }
            if (distanceMeters / APPROXIMATE_METERS_PER_MILE < 0.25 && roundedFeet != null && roundedFeet < APPROXIMATE_FEET_PER_QUARTER_MILE) {
                "≈ ${roundedFeet.roundToInt()} ft"
            } else {
                val accuracyMiles = accuracy / APPROXIMATE_METERS_PER_MILE
                val step = APPROXIMATE_MILE_STEPS.firstOrNull { it >= accuracyMiles } ?: APPROXIMATE_MILE_STEPS.last()
                val roundedMiles = roundedToStep(distanceMeters / APPROXIMATE_METERS_PER_MILE, step)
                if (step < 1.0) "≈ ${"%.1f".format(roundedMiles)} mi" else "≈ ${roundedMiles.roundToInt()} mi"
            }
        }
    }
}

/** [value] to the nearest multiple of [step], and never less than one step: "≈ 0" says nothing true. */
private fun roundedToStep(value: Double, step: Double): Double = ((value / step).roundToInt() * step).coerceAtLeast(step)

/** 1, 2, 5 in each decade: the step chosen is at most 2.5 times the accuracy. */
private val APPROXIMATE_METRE_STEPS = listOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1_000.0, 2_000.0, 5_000.0, 10_000.0, 20_000.0, 50_000.0, 100_000.0)
private val APPROXIMATE_FOOT_STEPS = listOf(10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1_000.0)
private val APPROXIMATE_MILE_STEPS = listOf(0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0)

private const val APPROXIMATE_METERS_PER_MILE = 1_609.344
private const val APPROXIMATE_FEET_PER_METER = 3.28084
private const val APPROXIMATE_FEET_PER_QUARTER_MILE = 1_320.0
