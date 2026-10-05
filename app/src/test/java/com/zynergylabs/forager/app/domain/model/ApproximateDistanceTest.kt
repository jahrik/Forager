package com.zynergylabs.forager.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Dispatch 2026-09-28-510: the HUD's "≈ …" from an approximate position. Every expected value is a
 * pinned literal, worked by hand beside it: the step is the first of 10, 20, 50, 100, 200, 500 m, 1,
 * 2, 5 … km (10, 20, 50 … 1,000 ft, then 0.1, 0.2, 0.5, 1, 2 … mi) at least the accuracy.
 */
class ApproximateDistanceTest {

    @Test
    fun `metres, rounded to a step no finer than the accuracy, always marked`() {
        // 100 m accuracy: step 100; 340 / 100 = 3.4 -> 3 -> 300.
        assertEquals("≈ 300 m", formatApproximateDistance(340.0, 100f, DistanceUnit.KILOMETERS))
        // 120 m: step 200; 250 / 200 = 1.25 -> 1 -> 200.
        assertEquals("≈ 200 m", formatApproximateDistance(250.0, 120f, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `kilometres keep the mark, one decimal while the step is under a kilometre`() {
        // Step 100; 3,240 / 100 = 32.4 -> 32 -> 3,200 m.
        assertEquals("≈ 3.2 km", formatApproximateDistance(3_240.0, 100f, DistanceUnit.KILOMETERS))
        // 60 m: step 100; 1,049 -> 10.49 -> 10 -> 1,000 m. formatDistanceWithAccuracy drops the mark here ("1.0 km").
        assertEquals("≈ 1.0 km", formatApproximateDistance(1_049.0, 60f, DistanceUnit.KILOMETERS))
        // 2,500 m: step 5,000; 12,600 / 5,000 = 2.52 -> 3 -> 15 km, whole kilometres.
        assertEquals("≈ 15 km", formatApproximateDistance(12_600.0, 2_500f, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `an accuracy coarser than the existing formatter's steps does not throw`() {
        // 600 m: past formatDistanceWithAccuracy's last metre step (500), where its first { } throws.
        // Here: step 1,000; 800 / 1,000 = 0.8 -> 1 -> 1 km; 1,700 -> 1.7 -> 2 -> 2 km.
        assertEquals("≈ 1 km", formatApproximateDistance(800.0, 600f, DistanceUnit.KILOMETERS))
        assertEquals("≈ 2 km", formatApproximateDistance(1_700.0, 600f, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `a distance that would round to nothing shows one step, never zero`() {
        // Step 100; 40 / 100 = 0.4 -> 0, shown as 100.
        assertEquals("≈ 100 m", formatApproximateDistance(40.0, 100f, DistanceUnit.KILOMETERS))
    }

    @Test
    fun `feet below a quarter mile, then miles, always marked`() {
        // 60 m = 196.85 ft: step 200 ft; 150 m = 492.13 ft -> 2.46 -> 2 -> 400 ft.
        assertEquals("≈ 400 ft", formatApproximateDistance(150.0, 60f, DistanceUnit.MILES))
        // 100 m = 328.08 ft: step 500 ft; 300 m = 984.25 ft -> 1.97 -> 2 -> 1,000 ft.
        assertEquals("≈ 1000 ft", formatApproximateDistance(300.0, 100f, DistanceUnit.MILES))
        // 100 m = 0.0621 mi: step 0.1; 3,240 m = 2.0132 mi -> 20.13 -> 20 -> 2.0 mi.
        assertEquals("≈ 2.0 mi", formatApproximateDistance(3_240.0, 100f, DistanceUnit.MILES))
        // 600 m = 0.3728 mi: step 0.5; 800 m = 0.4971 mi -> 0.99 -> 1 -> 0.5 mi; 1,700 m = 1.0563 mi -> 2.11 -> 2 -> 1.0 mi.
        assertEquals("≈ 0.5 mi", formatApproximateDistance(800.0, 600f, DistanceUnit.MILES))
        assertEquals("≈ 1.0 mi", formatApproximateDistance(1_700.0, 600f, DistanceUnit.MILES))
        // 2,500 m = 1.553 mi: step 2; 12,600 m = 7.829 mi -> 3.91 -> 4 -> 8 mi, whole miles.
        assertEquals("≈ 8 mi", formatApproximateDistance(12_600.0, 2_500f, DistanceUnit.MILES))
    }
}
