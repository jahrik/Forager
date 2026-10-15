package com.zynergylabs.forager.app.data.raw

import com.zynergylabs.forager.app.domain.raw.CfaPattern
import com.zynergylabs.forager.app.domain.raw.DngWriteResult
import java.io.File
import java.util.Base64
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** TEMPORARY, removed in the next commit: prints written DNGs as base64 so they can be opened with outside readers. */
class TemporaryDngExportTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `export sample DNGs`() {
        val images = mapOf(
            "full-grbg" to SyntheticDng.full(cfa = CfaPattern.GRBG),
            "minimal-gbrg-520x300" to SyntheticDng.minimal(width = 520, height = 300, cfa = CfaPattern.GBRG),
            "minimal-rggb-64x48" to SyntheticDng.minimal(),
            "minimal-bggr-70x34" to SyntheticDng.minimal(width = 70, height = 34, cfa = CfaPattern.BGGR),
        )
        for ((name, image) in images) {
            val file = File(tmp.root, "$name.dng")
            assertTrue(ForagerDngWriter(emptyList()).write(image, file) is DngWriteResult.Written)
            println("DNGEXPORT $name ${Base64.getEncoder().encodeToString(file.readBytes())}")
            println("SAMPLES $name ${Base64.getEncoder().encodeToString(java.nio.ByteBuffer.allocate(image.samples.size * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN).also { b -> image.samples.forEach { b.putShort(it) } }.array())}")
        }
    }
}
