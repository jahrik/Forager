package com.zynergylabs.forager.app.data.diagnostics

import com.zynergylabs.forager.app.domain.CurrentTimeProvider
import com.zynergylabs.forager.app.domain.ReturnRecord
import com.zynergylabs.forager.app.domain.ReturnRecordEvent
import java.io.File

/** Work in progress (dispatch 2026-09-28-451, tests first): writes nothing yet. */
class FileReturnRecord(private val file: File, private val clock: CurrentTimeProvider, private val maxBytes: Long = RETURN_RECORD_MAX_BYTES) : ReturnRecord {
    override fun write(event: ReturnRecordEvent) = Unit
}

/** The record's size limit. Work in progress. */
const val RETURN_RECORD_MAX_BYTES = 512L * 1024L
