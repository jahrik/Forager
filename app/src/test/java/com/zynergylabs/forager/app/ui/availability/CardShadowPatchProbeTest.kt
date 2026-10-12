package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ui.map.MapIconStackButtonColorLight
import com.zynergylabs.forager.app.ui.theme.ForagerTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Dispatch 2026-10-12-06, the planner's item 3 (a lighter rectangle seen inside the card in the Robolectric screenshots): a
 * probe, not a guard. It draws the card's surface ([mapCardSurface]) on empty boxes, with no content at all, over a flat
 * colour, and prints each pixel row's lightest and darkest red value and where the light runs are, so the patch can be found
 * (or not) where there is nothing but the surface and its shadow to draw it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h400dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CardShadowPatchProbeTest {
    private val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    @Test
    fun `probe the empty card surface for a light patch`() {
        composeRule.setContent {
            ForagerTheme(darkTheme = false) {
                Column(Modifier.background(Color(0xFFD8D4C8))) {
                    Box(Modifier.padding(top = 20.dp).fillMaxWidth().height(92.dp).mapCardSurface(MapIconStackButtonColorLight, MapCardCorners.BOTTOM))
                    Box(Modifier.padding(top = 40.dp).fillMaxWidth().height(92.dp).mapCardSurface(MapIconStackButtonColorLight, MapCardCorners.ALL))
                    Box(Modifier.height(100.dp))
                }
            }
        }
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File("build/test-results/testDebugUnitTest/rounded-map-strip-screens").apply { mkdirs() }.let { dir ->
            File(dir, "probe-card-surface.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        for (y in 0 until bitmap.height step 3) {
            val reds = (0 until bitmap.width).map { (bitmap.getPixel(it, y) shr 16) and 0xFF }
            val max = reds.max()
            val runs = mutableListOf<String>()
            var start = -1
            for (x in reds.indices) {
                val light = reds[x] >= max - 1
                if (light && start < 0) start = x
                if ((!light || x == reds.lastIndex) && start >= 0) { runs += "$start-$x"; start = -1 }
            }
            println("PROBE|y=$y|min=${reds.min()}|max=$max|lightRuns=${runs.take(6)}")
        }
    }
}
