package tech.benedicta.will.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class HomeTopBarFormatterTest {

    @Test
    fun `formatDate returns day of week and date`() {
        val cal = Calendar.getInstance(Locale.US).apply {
            set(2026, Calendar.APRIL, 13) // Monday
        }
        val result = formatDate(cal.timeInMillis, Locale.US)
        assertEquals("Mon, Apr 13", result)
    }
}
