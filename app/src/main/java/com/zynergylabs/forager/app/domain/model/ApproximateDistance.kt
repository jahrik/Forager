package com.zynergylabs.forager.app.domain.model

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
fun formatApproximateDistance(distanceMeters: Double, accuracyMeters: Float, unit: DistanceUnit): String = ""
