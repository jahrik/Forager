package com.zynergylabs.forager.app.ui.log

import android.app.Application
import android.content.ComponentName
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Dispatch 2026-09-28-351: which thread [DecodedPhoto]'s decode, its state write and the
 * recomposition after it run on, under the unit tests' default and under the opt-in. See
 * [PhotoDecodeDispatcher] for why the thread matters.
 *
 * What is recorded, and where:
 * - the decode thread, by [ThreadRecordingBitmapFactoryShadow];
 * - the thread of every snapshot apply notification after the photo is requested. The effect's state
 *   write is applied by `sendApplyNotifications` on whichever thread resumed the effect, so an apply
 *   observer sees that thread without any hook in production code;
 * - the thread of every layout pass of the photo's own modifier chain.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], shadows = [ThreadRecordingBitmapFactoryShadow::class])
class PhotoDecodeThreadTest {

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

    private val applyThreads = CopyOnWriteArrayList<Thread>()
    private val layoutThreads = CopyOnWriteArrayList<Thread>()

    @Before
    fun clearRecordings() {
        ThreadRecordingBitmapFactoryShadow.DECODE_THREADS.clear()
    }


    private fun showPhotoAndWaitForIt(name: String) {
        val context = ApplicationProvider.getApplicationContext<Application>()
        File(context.filesDir, name).apply { parentFile?.mkdirs() }.writeBytes(byteArrayOf(1, 2, 3, 4))
        val handle = Snapshot.registerApplyObserver { _, _ -> applyThreads.add(Thread.currentThread()) }
        try {
            composeRule.setContent {
                DecodedPhoto(
                    relativePath = name,
                    modifier = Modifier.layout { measurable, constraints ->
                        layoutThreads.add(Thread.currentThread())
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                    },
                )
            }
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithContentDescription("Log photo").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.waitForIdle()
        } finally {
            handle.dispose()
        }
    }

    private fun describe(threads: List<Thread>) = threads.map { it.name }.distinct().toString()

    @Test
    fun `production default is Dispatchers IO when nothing overrides`() {
        PhotoDecodeDispatcher.override(null)
        assertSame(kotlinx.coroutines.Dispatchers.IO, PhotoDecodeDispatcher.current)
    }

    @Test
    fun `under the test default the decode, the state write and the recomposition all run on the main test thread`() {
        val main = Looper.getMainLooper().thread

        showPhotoAndWaitForIt("photos/thread-default.jpg")

        val decodeThreads = ThreadRecordingBitmapFactoryShadow.DECODE_THREADS.toList()
        assertEquals("reachability: the photo must have been decoded exactly once", 1, decodeThreads.size)
        assertTrue("reachability: the swap must have produced apply notifications", applyThreads.isNotEmpty())
        assertTrue("reachability: the photo must have been laid out after the swap", layoutThreads.size >= 2)
        assertTrue("the decode ran on ${describe(decodeThreads)}, not the main thread", decodeThreads.all { it === main })
        assertTrue("a snapshot apply (the decode's state write) ran on ${describe(applyThreads)}, not the main thread", applyThreads.all { it === main })
        assertTrue("a layout pass after the swap ran on ${describe(layoutThreads)}, not the main thread", layoutThreads.all { it === main })
    }

    @Test
    fun `the opt-in restores a decode on a real background thread`() {
        val main = Looper.getMainLooper().thread
        PhotoDecodeTestEnvironment.useBackgroundDecode()

        showPhotoAndWaitForIt("photos/thread-optin.jpg")

        val decodeThreads = ThreadRecordingBitmapFactoryShadow.DECODE_THREADS.toList()
        assertEquals("reachability: the photo must have been decoded exactly once", 1, decodeThreads.size)
        assertTrue("the opt-in decoded on the main thread; it should be a background worker", decodeThreads.none { it === main })
    }
}
