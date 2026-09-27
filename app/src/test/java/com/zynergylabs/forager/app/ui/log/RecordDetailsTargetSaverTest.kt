package com.zynergylabs.forager.app.ui.log

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Journal redesign J5c: the open details sheet's target is kept in `rememberSaveable` state through
 * [RecordDetailsTargetSaver], so it must come back as the same record, and a saved value it does not
 * recognise must fail loudly rather than quietly open nothing.
 */
class RecordDetailsTargetSaverTest {

    private val scope = SaverScope { true }

    private fun roundTrip(target: RecordDetailsTarget?): RecordDetailsTarget? {
        val saved = with(RecordDetailsTargetSaver) { scope.save(target) }!!
        return RecordDetailsTargetSaver.restore(saved)
    }

    @Test
    fun `every target, and no target, comes back as itself`() {
        val targets = listOf(
            RecordDetailsTarget.WaypointDetails("wp-creek"),
            RecordDetailsTarget.TrackDetails("track:with:colons"),
            RecordDetailsTarget.OfflineRegionDetails(7L),
            null,
        )
        for (target in targets) assertEquals(target, roundTrip(target))
    }

    @Test
    fun `a saved value that names no target fails loudly`() {
        val error = assertThrows(IllegalStateException::class.java) { RecordDetailsTargetSaver.restore("find:abc") }
        assertEquals("Not a saved record-details target: 'find:abc'", error.message)
    }
}
