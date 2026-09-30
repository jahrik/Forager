package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height as layoutHeight
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-317, size part of the semantics ruling: the not-yet-loaded [DecodedPhoto]
 * occupies the same space it did when it was a plain `Box`, for every modifier shape the app's call
 * sites give it. Sizes are read while the decode is held ([GatedDecode]), so the placeholder is
 * what is measured. The expected values were measured on the unfixed build (a `Box`).
 *
 * Shapes (audited call sites, `docs/audits/2026-09-30-album-gesture-completion-report.md`):
 * `fillMaxSize` in a sized parent (album tile, gallery/picker tiles), `size(n.dp)` (editor and
 * report thumbnails, bubble, kept photo), `fillMaxWidth().height(n.dp)` (the entry card's hero), and
 * no size at all (only `DecodedPhotoTest` uses that).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [GatedBitmapFactoryShadow::class])
class DecodedPhotoPlaceholderSizeTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager)
                .addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    @After
    fun releaseAnyHeldDecode() {
        GatedDecode.reset()
    }

    private fun placeholderSize(content: @androidx.compose.runtime.Composable () -> Unit): String {
        GatedDecode.holdDecodes(decodes = 1)
        composeRule.setContent { content() }
        composeRule.waitForIdle()
        val bounds = composeRule.onNodeWithTag("sized-photo", useUnmergedTree = true).getUnclippedBoundsInRoot()
        return "${bounds.width.value}x${bounds.height.value}"
    }

    @Test
    fun `fillMaxSize in a sized parent`() {
        assertEquals("100.0x60.0", placeholderSize {
            Box(Modifier.size(100.dp, 60.dp)) { DecodedPhoto("photos/none.jpg", Modifier.fillMaxSize().testTag("sized-photo")) }
        })
    }

    @Test
    fun `size in dp`() {
        assertEquals("88.0x88.0", placeholderSize {
            Box { DecodedPhoto("photos/none.jpg", Modifier.size(88.dp).testTag("sized-photo")) }
        })
    }

    @Test
    fun `fillMaxWidth and a height`() {
        assertEquals("320.0x50.0", placeholderSize {
            Column { DecodedPhoto("photos/none.jpg", Modifier.fillMaxWidth().layoutHeight(50.dp).testTag("sized-photo")) }
        })
    }

    @Test
    fun `no size at all`() {
        assertEquals("0.0x0.0", placeholderSize {
            Column { DecodedPhoto("photos/none.jpg", Modifier.testTag("sized-photo")) }
        })
    }
}
