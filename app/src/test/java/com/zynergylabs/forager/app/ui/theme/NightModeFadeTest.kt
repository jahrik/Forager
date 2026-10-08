package com.zynergylabs.forager.app.ui.theme

import android.app.Application
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ui.availability.layoutFixesHostActivityRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Motion Part 3, item 4 (dispatch 2026-09-28-676; the owner, RECORD -651: Night mode "Fade the colours", and RECORD -652: "fast and
 * smooth, and not ceremonial and boring"), through [ForagerTheme], the app's one theme root, which `MainActivity` hands the Night
 * mode setting as `darkTheme`. Every colour a screen draws with is read back from what that screen sees of
 * [MaterialTheme.colorScheme], on every composition.
 *
 * Nothing here has been run: written with the code, before the build.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NightModeFadeTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(composeRule)

    private val resolver get() = ApplicationProvider.getApplicationContext<Application>().contentResolver

    @After
    fun restore() {
        composeRule.mainClock.autoAdvance = true
        Settings.Global.putFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    }

    private val dark = mutableStateOf(false)

    /** Every background colour a screen under the theme was composed with, in order. */
    private val seen = mutableListOf<Color>()

    private fun setScreen() {
        composeRule.setContent {
            ForagerTheme(darkTheme = dark.value) {
                val background = MaterialTheme.colorScheme.background
                SideEffect { seen += background }
            }
        }
        composeRule.waitForIdle()
    }

    private fun flipToDarkWithTheClockStopped() {
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnUiThread {
            dark.value = true
            Snapshot.sendApplyNotifications()
        }
    }

    /**
     * Part-way between [a] and [b]: neither end, and its luminance strictly between theirs. Luminance rather than each channel,
     * because Compose blends colours in Oklab, whose lightness moves steadily from one end to the other while a single sRGB
     * channel of a tinted colour need not.
     */
    private fun Color.isBetween(a: Color, b: Color): Boolean {
        val l = luminance()
        val lo = minOf(a.luminance(), b.luminance())
        val hi = maxOf(a.luminance(), b.luminance())
        return this != a && this != b && l > lo + 0.001f && l < hi - 0.001f
    }

    @Test
    fun `turning night mode on blends every screen's colours from light to dark, and ends exactly on the dark scheme`() {
        setScreen()
        assertEquals(LightColors.background, seen.last())
        seen.clear()

        flipToDarkWithTheClockStopped()
        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }

        assertTrue(
            "some frame was drawn with a background part-way between light and dark: $seen",
            seen.any { it.isBetween(LightColors.background, DarkColors.background) },
        )
        composeRule.mainClock.autoAdvance = true
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.waitForIdle()
        assertEquals(DarkColors.background, seen.last())
    }

    @Test
    fun `the blend is quick, over in well under half a second`() {
        setScreen()
        flipToDarkWithTheClockStopped()
        // Half a second of frames.
        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }
        assertEquals("settled on the dark scheme within half a second", DarkColors.background, seen.last())
    }

    @Test
    fun `under reduced motion night mode changes every colour at once, with no frame in between`() {
        Settings.Global.putFloat(resolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 0f)
        setScreen()
        seen.clear()

        flipToDarkWithTheClockStopped()
        repeat(30) { composeRule.mainClock.advanceTimeByFrame() }

        assertTrue("composed again after the change", seen.isNotEmpty())
        assertTrue("only the two ends were ever drawn: $seen", seen.all { it == LightColors.background || it == DarkColors.background })
        assertEquals(DarkColors.background, seen.last())
    }

    @Test
    fun `half way, the roles are half way, the last one listed included`() {
        val half = lerpColorScheme(LightColors, DarkColors, 0.5f)
        assertTrue(half.background.isBetween(LightColors.background, DarkColors.background))
        assertTrue(half.surface.isBetween(LightColors.surface, DarkColors.surface))
        assertTrue(half.onSurface.isBetween(LightColors.onSurface, DarkColors.onSurface))
        // The last roles in the list, which a role missed from it would leave at the light end.
        assertTrue(half.surfaceContainerHighest.isBetween(LightColors.surfaceContainerHighest, DarkColors.surfaceContainerHighest))
    }
}
