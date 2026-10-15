package com.zynergylabs.forager.app.data.raw

import com.zynergylabs.forager.app.domain.raw.DngImage
import com.zynergylabs.forager.app.domain.raw.DngWriteResult
import com.zynergylabs.forager.app.domain.raw.DngWriter
import java.io.File

/** STUB for the failing-first run of dispatch 2026-10-15-02's tests: writes nothing and always fails. */
class ForagerDngWriter internal constructor(
    private val forbiddenStrings: List<String>,
    private val beforeCheck: ((File) -> Unit)?,
) : DngWriter {

    constructor(forbiddenStrings: List<String>) : this(forbiddenStrings, null)

    override fun write(image: DngImage, destination: File): DngWriteResult =
        DngWriteResult.Failed("stub: not implemented")
}
