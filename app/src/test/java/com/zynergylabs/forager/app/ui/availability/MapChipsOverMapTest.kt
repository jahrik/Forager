package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.JournalEntryOnMap
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.JournalEntriesMapChip
import com.zynergylabs.forager.app.ui.theme.ForagerTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * J8 follow-ups, item 3 (continuation `2026-09-28-87`; the planner's ruling under CLAUDE.md's UX
 * defaults, "Layered fills composite to that value; they do not each carry it"): the two chips over
 * the map, J8's "N journal entries on map" and the taxon filter's "Showing: ...", each take one fill at
 * the map chrome's 0.8 and nothing else beneath it, so what covers the map is that 0.8. J8's chip
 * measured 0.833 to 0.840 on the S22, which the device check put down to a 0.8 fill over the chip's own
 * 4 dp shadow; the taxon chip had the same shadow and was not measured.
 *
 * The mechanism is no shadow elevation on either chip. Material3's `Surface` draws a shadow through a
 * `graphicsLayer` on its own node only when its `shadowElevation` is above zero, so this reads the
 * modifiers on the chip's own node (the node its test tag is on, which also carries the Surface's
 * background) and finds no shadow there. What this cannot check: the composite on a screen. Both chips
 * are re-measured on the device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MapChipsOverMapTest {

    private val composeRule = createComposeRule()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    /**
     * The modifiers on [tag]'s own node, by name, and every shadow elevation above zero among them (a
     * `graphicsLayer`'s `shadowElevation` in px, or a `shadow`'s `elevation`).
     */
    private fun modifiersAndShadowsOf(tag: String): Pair<List<String>, List<Pair<String, Any>>> {
        val infos = composeRule.onNodeWithTag(tag).fetchSemanticsNode().layoutInfo.getModifierInfo()
        val names = infos.map { (it.modifier as? InspectableValue)?.nameFallback ?: it.modifier::class.java.simpleName }
        val shadows = infos.mapNotNull { it.modifier as? InspectableValue }.flatMap { value ->
            value.inspectableElements
                .filter { it.name == "shadowElevation" || it.name == "elevation" }
                .mapNotNull { element ->
                    val amount = when (val v = element.value) {
                        is Float -> v
                        is Dp -> v.value
                        else -> null
                    }
                    if (amount != null && amount > 0f) "${value.nameFallback}.${element.name}" to (element.value as Any) else null
                }
                .toList()
        }
        return names to shadows
    }

    private fun assertOneFillAndNoShadow(tag: String) {
        val (names, shadows) = modifiersAndShadowsOf(tag)
        assertTrue("read on the chip's own node, the one with the Surface's fill: $names", "BackgroundElement" in names)
        assertEquals("no shadow under the translucent fill; modifiers $names", emptyList<Pair<String, Any>>(), shadows)
    }

    @Test
    fun `the journal entries chip has no shadow under its 0_8 fill`() {
        composeRule.setContent {
            ForagerTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize()) {
                    JournalEntriesMapChip(entries = listOf(JournalEntryOnMap("a", LocalDate.of(2026, 9, 12))), onHide = {}, onHideAll = {})
                }
            }
        }
        composeRule.waitForIdle()

        assertOneFillAndNoShadow(JOURNAL_ENTRIES_CHIP_TAG)
    }

    @Test
    fun `the taxon chip has no shadow under its 0_8 fill`() {
        composeRule.setContent {
            ForagerTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize()) {
                    TaxonMapFilterChip(label = "Golden chanterelle (3)", onClear = {})
                }
            }
        }
        composeRule.waitForIdle()

        assertOneFillAndNoShadow("map-taxon-filter-chip")
    }
}
