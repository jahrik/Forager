package com.zynergylabs.forager.app.alert

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.text.format.DateFormat
import androidx.test.core.app.ApplicationProvider
import com.zynergylabs.forager.app.domain.Alert
import com.zynergylabs.forager.app.domain.AlertKind
import com.zynergylabs.forager.app.domain.SundownAlertDetail
import com.zynergylabs.forager.app.domain.WalkBack
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Date

/**
 * What the three sundown notifications say (dispatch 2026-09-28-516, the owner's copy rulings in
 * the coder's window, 2026-10-04): the sunset time as the title, never an order; the walk back
 * "about" only when measured, "at least" when the estimate is thin, "unknown" when withheld; and
 * only a measured estimate says anything about getting back. Through [AndroidAlertDelivery.deliver],
 * the entry point the watch calls, read as the posted notification.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SundownNotificationTextTest {

    private lateinit var context: Application
    private lateinit var delivery: AndroidAlertDelivery
    private val sunset = 1_791_078_720_000L
    private val minute = 60_000L

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        delivery = AndroidAlertDelivery(context, ::postNotificationFor) { _, _ -> }
    }

    private fun post(kind: AlertKind, walkBack: WalkBack): Pair<String, String> {
        delivery.deliver(Alert(kind, overridesSilence = true, sundown = SundownAlertDetail(sunset, walkBack)))
        val n = shadowOf(context.getSystemService(NotificationManager::class.java)).getNotification(SUNDOWN_NOTIFICATION_ID)
        return n.extras.getCharSequence(Notification.EXTRA_TITLE).toString() to n.extras.getCharSequence(Notification.EXTRA_TEXT).toString()
    }

    private val sunsetTitle get() = "Sunset at " + DateFormat.getTimeFormat(context).format(Date(sunset))

    @Test
    fun `measured heads-up gives sunset and the walk back the way you came, with the 30 minutes`() {
        assertEquals(
            sunsetTitle to "The walk back the way you came is about 1 h 30. Leave within 30 min to walk it before dark.",
            post(AlertKind.HEADS_UP, WalkBack.About(90 * minute)),
        )
    }

    @Test
    fun `measured leave-by gives sunset and the walk back, nothing more`() {
        assertEquals(
            sunsetTitle to "The walk back the way you came is about 1 h 30.",
            post(AlertKind.LEAVE_BY, WalkBack.About(90 * minute)),
        )
    }

    @Test
    fun `at least never promises, on either alert`() {
        for (kind in listOf(AlertKind.HEADS_UP, AlertKind.LEAVE_BY)) {
            assertEquals(kind.name, sunsetTitle to "The walk back is at least 45 min.", post(kind, WalkBack.AtLeast(45 * minute)))
        }
    }

    @Test
    fun `unknown says unknown, on either alert`() {
        for (kind in listOf(AlertKind.HEADS_UP, AlertKind.LEAVE_BY)) {
            assertEquals(kind.name, sunsetTitle to "The walk back is unknown.", post(kind, WalkBack.Unknown))
        }
    }

    @Test
    fun `the sunset alert is unchanged`() {
        assertEquals("The sun has set" to "Light is going now.", post(AlertKind.SUNSET, WalkBack.Unknown))
    }

    @Test
    fun `walking durations round up to the minute and read as the owner wrote them`() {
        assertEquals("1 min", formatWalkDuration(1L))
        assertEquals("45 min", formatWalkDuration(45 * minute))
        assertEquals("46 min", formatWalkDuration(45 * minute + 1))
        assertEquals("1 h", formatWalkDuration(60 * minute))
        assertEquals("1 h 05", formatWalkDuration(65 * minute))
        assertEquals("1 h 30", formatWalkDuration(90 * minute))
        assertEquals("0 min", formatWalkDuration(0L))
    }
}
