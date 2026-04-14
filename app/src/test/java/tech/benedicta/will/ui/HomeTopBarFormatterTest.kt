package tech.benedicta.will.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class HomeTopBarFormatterTest {

    @Test
    fun `formatTime returns HH-mm with leading zero for morning hours`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 5)
        }
        assertEquals("09:05", formatTime(cal.timeInMillis, Locale.US))
    }

    @Test
    fun `formatTime returns correct afternoon time`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
        }
        assertEquals("14:30", formatTime(cal.timeInMillis, Locale.US))
    }

    @Test
    fun `batteryPercentage calculates correctly from level and scale`() {
        assertEquals(100, batteryPercentage(level = 100, scale = 100))
        assertEquals(50, batteryPercentage(level = 50, scale = 100))
        assertEquals(75, batteryPercentage(level = 192, scale = 256))
    }

    @Test
    fun `batteryPercentage returns 0 when level is 0`() {
        assertEquals(0, batteryPercentage(level = 0, scale = 100))
    }

    @Test
    fun `formatDate returns day of week and date`() {
        val cal = Calendar.getInstance(Locale.US).apply {
            set(2026, Calendar.APRIL, 13) // Monday
        }
        val result = formatDate(cal.timeInMillis, Locale.US)
        assertEquals("Mon, 13 Apr", result)
    }
}
