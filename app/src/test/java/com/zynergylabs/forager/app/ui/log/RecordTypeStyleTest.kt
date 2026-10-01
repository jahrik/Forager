package com.zynergylabs.forager.app.ui.log

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Journal redesign J1, S2: [RecordTypeStyle] maps each record type to exactly the plan J6 roles.
 *
 * Every role in the scheme below is a distinct colour, so a swapped pair or a wrong role (say
 * `onSecondaryContainer` for `secondary`, or `surfaceVariant` for `surfaceContainerHighest`) reads
 * back as a different value rather than coinciding with a default.
 */
class RecordTypeStyleTest {

    private val scheme = lightColorScheme(
        primary = Color(0xFF000001),
        primaryContainer = Color(0xFF000002),
        secondary = Color(0xFF000003),
        secondaryContainer = Color(0xFF000004),
        tertiary = Color(0xFF000005),
        tertiaryContainer = Color(0xFF000006),
        onSurfaceVariant = Color(0xFF000007),
        surfaceContainerHighest = Color(0xFF000008),
        surfaceVariant = Color(0xFF000009),
        onSecondaryContainer = Color(0xFF00000A),
        onTertiaryContainer = Color(0xFF00000B),
        onPrimaryContainer = Color(0xFF00000C),
    )

    @Test
    fun `each record type maps to the plan J6 roles`() {
        assertEquals(RecordTypeColors(Color(0xFF000003), Color(0xFF000004)), RecordTypeStyle.colorsIn(scheme, RecordType.FINDS))
        assertEquals(RecordTypeColors(Color(0xFF000005), Color(0xFF000006)), RecordTypeStyle.colorsIn(scheme, RecordType.TRACKS))
        assertEquals(RecordTypeColors(Color(0xFF000001), Color(0xFF000002)), RecordTypeStyle.colorsIn(scheme, RecordType.WAYPOINTS))
        assertEquals(RecordTypeColors(Color(0xFF000007), Color(0xFF000008)), RecordTypeStyle.colorsIn(scheme, RecordType.OFFLINE_MAPS))
    }
}
