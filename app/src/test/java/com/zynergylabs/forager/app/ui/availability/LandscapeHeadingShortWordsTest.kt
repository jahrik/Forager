package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * RECORD -733 (the owner: "Short words, same room (Recommended)"): the landscape strip is sized from the heading's widest
 * value form ("000°" and the widest compass point) alone, so its short status words, "No compass" and "Compass?", must fit
 * within that width, measured in the strip's own readout style (14 sp, tabular figures) at fonts 1.0 and 2.0. Native graphics:
 * Robolectric's legacy text measurement would make every fit pass on fake widths.
 */
abstract class LandscapeHeadingShortWordsTests(private val expectedFontScale: Float) {
    private val rule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(layoutFixesHostActivityRule()).around(rule)

    @Test
    fun `the short heading words fit within the heading's value width`() {
        var valuePx = -1
        val wordsPx = mutableMapOf<String, Int>()
        var fontScale = 0f
        rule.setContent {
            MaterialTheme {
                val measurer = rememberTextMeasurer()
                fontScale = LocalDensity.current.fontScale
                val style = stripReadoutStyle().copy(fontFeatureSettings = "tnum")
                val widthOf: (String, TextStyle) -> Int = { text, s -> measurer.measure(text, s, maxLines = 1, softWrap = false).size.width }
                valuePx = landscapeHeadingValueWidthPx(widthOf, style)
                listOf(LANDSCAPE_NO_COMPASS_TEXT, LANDSCAPE_COMPASS_UNRELIABLE_TEXT).forEach { wordsPx[it] = widthOf(it, style) }
            }
        }
        rule.waitForIdle()
        println("MEASURED font $fontScale: heading value width $valuePx px, words $wordsPx")
        assertEquals("the font scale this test is about", expectedFontScale, fontScale, 0.01f)
        assertTrue("positive control: the value width was measured ($valuePx)", valuePx > 0)
        wordsPx.forEach { (word, px) -> assertTrue("<$word> ($px px) fits within the value's $valuePx px", px <= valuePx) }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi", fontScale = 1.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeHeadingShortWordsFont1Test : LandscapeHeadingShortWordsTests(1.0f)

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w780dp-h360dp-land-xxhdpi", fontScale = 2.0f)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LandscapeHeadingShortWordsFont2Test : LandscapeHeadingShortWordsTests(2.0f)
