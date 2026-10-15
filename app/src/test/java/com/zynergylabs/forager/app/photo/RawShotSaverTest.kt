package com.zynergylabs.forager.app.photo

import com.zynergylabs.forager.app.domain.LOW_STORAGE_FLOOR_BYTES
import com.zynergylabs.forager.app.domain.raw.DngImage
import com.zynergylabs.forager.app.domain.raw.DngWriteResult
import com.zynergylabs.forager.app.domain.raw.DngWriter
import com.zynergylabs.forager.app.domain.worstCaseRawShotBytes
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * The shot step (`RawShotSaver`, dispatch 2026-10-15-01 Amendment 1 items 3 and 5): a DNG is written only when there is
 * an input and room for it, and a writer that refuses leaves no file and the owner's -1003 message. The writer is a fake
 * here (the real one is built on its own branch, dispatch 2026-10-15-02); what is tested is this side's handling of each
 * answer the contract allows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RawShotSaverTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val plenty = LOW_STORAGE_FLOOR_BYTES + worstCaseRawShotBytes(4, 4) + 1

    private fun saver(writer: DngWriter, free: Long = plenty, files: MutableList<File> = mutableListOf()) =
        RawShotSaver(
            newRawFile = { File(tmp.root, "shot-${files.size}.dng").also { files += it } },
            freeBytes = { free },
            writer = writer,
            ioDispatcher = Dispatchers.Unconfined,
        )

    private val shot = RawShot(dng = fakeDngImage(), dngUnavailable = null)

    @Test
    fun `a written DNG is kept, and it is the writer's own file`() = runTest {
        val writer = FakeDngWriter { _, destination -> destination.writeBytes(byteArrayOf(0x49, 0x49, 0x2A, 0)); DngWriteResult.Written(4) }
        val outcome = saver(writer).save(shot)
        val kept = outcome as? RawShotOutcome.Kept ?: throw AssertionError("expected Kept, got $outcome")
        assertArrayEquals(byteArrayOf(0x49, 0x49, 0x2A, 0), kept.dng.readBytes())
        assertEquals("the writer saw this shot's input", listOf(shot.dng), writer.images)
    }

    @Test
    fun `a writer that refuses leaves no file, and the user is given the owner's -1003 words`() = runTest {
        ShadowLog.clear()
        val files = mutableListOf<File>()
        // A writer that leaves its file behind against the contract: this side still removes it.
        val writer = FakeDngWriter { _, destination -> destination.writeBytes(byteArrayOf(1)); DngWriteResult.Failed("a planted Make tag") }
        val outcome = saver(writer, files = files).save(shot)
        assertEquals(RawShotOutcome.JpgOnly(RAW_NOT_CLEAN_MESSAGE, "the DNG writer refused the file: a planted Make tag"), outcome)
        assertEquals("The RAW file couldn't be cleaned of camera details, so this photo was saved as JPG.", RAW_NOT_CLEAN_MESSAGE)
        assertFalse("no DNG left behind", files.single().exists())
        assertTrue("logged with its cause", ShadowLog.getLogsForTag("RawShotSaver").any { it.msg.contains("a planted Make tag") })
    }

    @Test
    fun `a writer that throws is treated as a refusal, and its file is not trusted`() = runTest {
        val files = mutableListOf<File>()
        val writer = FakeDngWriter { _, destination -> destination.writeBytes(byteArrayOf(1)); throw IllegalStateException("boom") }
        val outcome = saver(writer, files = files).save(shot)
        assertTrue(outcome is RawShotOutcome.JpgOnly && outcome.message == RAW_NOT_CLEAN_MESSAGE && outcome.cause.contains("boom"))
        assertFalse(files.single().exists())
    }

    @Test
    fun `a writer that reports success with no file is not believed`() = runTest {
        val outcome = saver(FakeDngWriter { _, _ -> DngWriteResult.Written(10) }).save(shot)
        assertTrue("$outcome", outcome is RawShotOutcome.JpgOnly && outcome.message == RAW_NOT_CLEAN_MESSAGE)
    }

    @Test
    fun `below 500 MB plus the shot's worst case the DNG is never written, to the byte`() = runTest {
        val floor = LOW_STORAGE_FLOOR_BYTES + worstCaseRawShotBytes(4, 4)
        val writer = FakeDngWriter { _, destination -> destination.writeBytes(byteArrayOf(1)); DngWriteResult.Written(1) }
        val low = saver(writer, free = floor - 1).save(shot)
        assertTrue("$low", low is RawShotOutcome.JpgOnly && low.message == RAW_LOW_STORAGE_MESSAGE)
        assertEquals("the writer was never asked", 0, writer.images.size)
        assertEquals("Storage is low, so this photo was saved as JPG.", RAW_LOW_STORAGE_MESSAGE)

        assertTrue("at the floor it is written", saver(writer, free = floor).save(shot) is RawShotOutcome.Kept)
    }

    @Test
    fun `a shot the camera could not describe keeps its JPG, says so, and never reaches the writer`() = runTest {
        val writer = FakeDngWriter { _, _ -> error("not reached") }
        val outcome = saver(writer).save(RawShot(dng = null, dngUnavailable = "the capture result carried no neutral colour point"))
        assertEquals(RawShotOutcome.JpgOnly(RAW_NOT_SAVED_MESSAGE, "no DNG input: the capture result carried no neutral colour point"), outcome)
        assertEquals(0, writer.images.size)
    }

    @Test
    fun `until Forager's writer is on this branch, production refuses every file rather than write an unchecked one`() = runTest {
        val target = File(tmp.root, "never.dng").apply { writeBytes(byteArrayOf(1)) }
        val result = productionDngWriter(listOf("anything")).write(fakeDngImage(), target)
        assertTrue("$result", result is DngWriteResult.Failed)
        assertFalse(target.exists())
    }
}

/** A [DngWriter] that runs [behaviour] and records every image it was given. */
internal class FakeDngWriter(private val behaviour: (DngImage, File) -> DngWriteResult) : DngWriter {
    val images = mutableListOf<DngImage>()

    override fun write(image: DngImage, destination: File): DngWriteResult {
        images += image
        return behaviour(image, destination)
    }
}
