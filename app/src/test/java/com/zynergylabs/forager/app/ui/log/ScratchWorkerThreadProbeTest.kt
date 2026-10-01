package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.core.app.ApplicationProvider
import org.junit.After
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
 * SCRATCH (dispatch -317 addendum, diagnosis of the pin-3 CalledFromWrongThreadException). Never to
 * merge; reverted after the diagnosis. Forces the harness condition deterministically: the decode is
 * released while the test thread is NOT inside a Compose frame or an idle wait, so the compose
 * test's unconfined composition dispatcher resumes the LaunchedEffect on the IO worker, which then
 * writes the bitmap state and runs recomposition and apply on that thread. The same body runs against
 * the unfixed and the fixed DecodedPhoto; the question is whether each throws
 * CalledFromWrongThreadException.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [GatedBitmapFactoryShadow::class])
class ScratchWorkerThreadProbeTest {

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
    fun reset() {
        GatedDecode.reset()
    }

    @Test
    fun `decode released while the test thread is idle - which thread applies the swap`() {
        GatedDecode.holdDecodes(decodes = 1)
        val writes = java.util.Collections.synchronizedList(mutableListOf<String>())
        val measures = java.util.Collections.synchronizedList(mutableListOf<String>())
        val handle = androidx.compose.runtime.snapshots.Snapshot.registerGlobalWriteObserver { writes += Thread.currentThread().name }
        composeRule.setContent {
            DecodedPhoto(
                relativePath = "photos/none-probe.jpg",
                modifier = Modifier.layout { m, c ->
                    measures += Thread.currentThread().name
                    val pl = m.measure(c)
                    layout(pl.width, pl.height) { pl.place(0, 0) }
                },
                contentDescription = "Probe photo",
            )
        }
        composeRule.waitForIdle()
        GatedDecode.release()
        assertTrue("decode never finished", GatedDecode.awaitFinished())
        // Idle the test thread outside Compose so the IO thread's resume is not trampolined to it.
        Thread.sleep(500)
        println("PROBE thread-state: before waitUntil")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription("Probe photo").fetchSemanticsNodes().isNotEmpty()
        }
        handle.dispose()
        println("PROBE global-write threads: " + synchronized(writes) { writes.groupingBy { it }.eachCount() })
        println("PROBE measure threads: " + synchronized(measures) { measures.groupingBy { it }.eachCount() })
        println("PROBE completed without CalledFromWrongThreadException")
    }

    /**
     * Second probe: 40 photos whose decodes are released together by a helper thread while the test
     * thread is inside `waitUntil`, so some IO completions land between the test thread's frames
     * (the pin-3 condition) instead of while it sleeps. Passes or throws; the thread names of the
     * state writes are printed either way.
     */
    @Test
    fun `forty decodes released together while the test thread is inside waitUntil`() {
        val n = 40
        GatedDecode.holdDecodes(decodes = n)
        val writes = java.util.Collections.synchronizedList(mutableListOf<String>())
        val handle = androidx.compose.runtime.snapshots.Snapshot.registerGlobalWriteObserver { writes += Thread.currentThread().name }
        composeRule.setContent {
            androidx.compose.foundation.layout.Column {
                repeat(n) { i ->
                    DecodedPhoto(
                        relativePath = "photos/none-probe-$i.jpg",
                        modifier = Modifier.size(2.dp),
                        contentDescription = "Probe photo",
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val releaser = Thread { Thread.sleep(5); GatedDecode.release() }
        releaser.start()
        try {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithContentDescription("Probe photo").fetchSemanticsNodes().size == n
            }
        } finally {
            handle.dispose()
            println("PROBE2 global-write threads: " + synchronized(writes) { writes.groupingBy { it.substringBefore(" @") }.eachCount() })
        }
        println("PROBE2 completed without CalledFromWrongThreadException")
    }
}
