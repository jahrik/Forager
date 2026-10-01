package com.zynergylabs.forager.app.ui.availability

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.ui.map.JOURNAL_ENTRIES_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.JournalEntriesChipContainerColor
import com.zynergylabs.forager.app.ui.map.JournalEntriesMapChip
import com.zynergylabs.forager.app.domain.JournalEntryOnMap
import com.zynergylabs.forager.app.ui.map.MAP_CHROME_OVER_MAP_ALPHA
import com.zynergylabs.forager.app.ui.map.MAP_LEGEND_CHIP_TAG
import com.zynergylabs.forager.app.ui.map.MapChromeContainerColor
import com.zynergylabs.forager.app.ui.map.MapLegendChip
import com.zynergylabs.forager.app.ui.map.layers.COLOUR_FIELDS
import com.zynergylabs.forager.app.ui.map.layers.MAP_LAYER_REGISTRY
import com.zynergylabs.forager.app.ui.map.layers.MapLayerIds
import com.zynergylabs.forager.app.ui.map.layers.MapLayersState
import com.zynergylabs.forager.app.ui.map.layers.mapLegendFor
import com.zynergylabs.forager.app.ui.map.layers.withUnavailableColourFieldsHidden
import com.zynergylabs.forager.app.ui.theme.ForagerTheme
import java.time.LocalDate
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
 * C1 (dispatch 2026-09-28-210): the chips over a map, the taxon chip, the journal-entries chip and the legend, each
 * composed on its own (their entry points are the map's own composition, which draws them only for a taxon filter, a
 * shown entry and a visible colour layer; the same reasoning `JournalEntriesChipTest` gives). The owner, verbatim: "Have
 * them be the same color as the bottom app navigation bar." Each container is the navigation bar's colour aside from
 * alpha, at the 0.8 it already had; the menus, which were already `surfaceContainer`, are confirmed to be the token.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MapChromeColourChipsTest {

    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val declareHostActivity = object : ExternalResource() {
        override fun before() {
            val app = ApplicationProvider.getApplicationContext<Application>()
            Shadows.shadowOf(app.packageManager).addActivityIfNotPresent(ComponentName(app, ComponentActivity::class.java))
        }
    }

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(declareHostActivity).around(composeRule)

    private var token = Color.Unspecified
    private var menu = Color.Unspecified

    private val legend = mapLegendFor(
        withUnavailableColourFieldsHidden(MapLayersState.DEFAULT, MAP_LAYER_REGISTRY, setOf(MapLayerIds.FORECAST_CHANTERELLES)),
        MAP_LAYER_REGISTRY, COLOUR_FIELDS, emptyMap(),
    )!!

    private fun setChips(dark: Boolean) {
        composeRule.setContent {
            ForagerTheme(darkTheme = dark) {
                token = MaterialTheme.colorScheme.surfaceContainer
                menu = MenuDefaults.containerColor
                Box(Modifier.fillMaxSize()) {
                    TaxonMapFilterChip(label = "Golden chanterelle (3)", onClear = {})
                    JournalEntriesMapChip(entries = listOf(JournalEntryOnMap("a", LocalDate.of(2026, 9, 12))), onHide = {}, onHideAll = {})
                    MapLegendChip(legend = legend, expanded = false, onExpandedChange = {})
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertToken(name: String, container: Color) {
        assertEquals("$name: container alpha", MAP_CHROME_OVER_MAP_ALPHA, container.alpha, 0.002f)
        assertEquals("$name: container colour aside from alpha, the navigation bar's", token.copy(alpha = 1f), container.copy(alpha = 1f))
    }

    private fun containerOn(tag: String): Color {
        val nodes = composeRule.onAllNodes(hasTestTag(tag) and SemanticsMatcher.keyIsDefined(MapChromeContainerColor), useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals("one node tagged $tag carries MapChromeContainerColor", 1, nodes.size)
        return nodes.single().config[MapChromeContainerColor]
    }

    private fun check(dark: Boolean) {
        setChips(dark)
        assertToken("the taxon chip", containerOn("map-taxon-filter-chip"))
        assertToken("the journal-entries chip", composeRule.onNodeWithTag(JOURNAL_ENTRIES_CHIP_TAG).fetchSemanticsNode().config[JournalEntriesChipContainerColor])
        assertToken("the legend", containerOn(MAP_LEGEND_CHIP_TAG))
        assertEquals("the menus were already the navigation bar's role: MenuDefaults is the token", token, menu)
    }

    @Test fun `in dark the taxon chip, the journal-entries chip and the legend are the token at 0_8`() = check(dark = true)

    @Test fun `in light the taxon chip, the journal-entries chip and the legend are the token at 0_8`() = check(dark = false)
}
