package com.zynergylabs.forager.app.ui.availability

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performSemanticsAction
import com.zynergylabs.forager.app.ui.theme.Spacing
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Settings' scrolling content ends one bottom padding ([Spacing.md]) below its last row, in both
 * build types: no divider is drawn under the last row (dispatch 2026-10-11, the planner's follow-up to
 * RECORD -830: "release Settings must not end with a stray divider under Backup"). In a debug build
 * the last row is Diagnostics; in a release build, with the Crash Logs and Diagnostics rows gone, it
 * is the Backup section, and the divider that separated Backup from those rows must go with them.
 *
 * A divider carries no semantics, so it is measured rather than found: scrolled to the end, the gap
 * between the lowest node inside the scrolling column and the column's own bottom edge is the bottom
 * padding alone, 12 dp. A trailing divider makes it 25 dp (the column's 12 dp spacing, the 1 dp
 * line, then the padding).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h640dp-xhdpi")
class SettingsEndsAtItsLastRowTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(SettingsPageHarness.declareHostActivity()).around(composeRule)

    @Test
    fun `Settings ends one bottom padding below its last row, with no divider under it`() {
        SettingsPageHarness.openSettings(composeRule)
        val settings = composeRule.onNode(hasScrollAction() and hasAnyDescendant(hasText(SettingsPageHarness.SETTINGS_CONTROL_ROW)), useUnmergedTree = true)
        settings.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 100_000f) }
        composeRule.waitForIdle()

        val column = settings.fetchSemanticsNode()
        val lowest = descendantsOf(column).filter { it.boundsInRoot.height > 0f }.maxOf { it.boundsInRoot.bottom }
        val gapDp = (column.boundsInRoot.bottom - lowest) / composeRule.density.density

        assertEquals(
            "the gap under Settings' last row, in dp (${Spacing.md.value} is the padding alone; more means something is drawn under the last row)",
            Spacing.md.value,
            gapDp,
            0.5f,
        )
    }

    private fun descendantsOf(node: SemanticsNode): List<SemanticsNode> =
        node.children.flatMap { listOf(it) + descendantsOf(it) }
}
