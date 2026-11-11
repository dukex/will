package tech.benedicta.will.ui

import org.junit.Assert.assertEquals
import org.junit.Test

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE

class AttentionFormatterTest {

    @Test
    fun `minutes under an hour read as minutes`() {
        assertEquals("45m", attentionDuration(45 * MINUTE))
        assertEquals("1m", attentionDuration(MINUTE))
    }

    @Test
    fun `an hour and over reads as hours and minutes`() {
        assertEquals("1h 15m", attentionDuration(HOUR + 15 * MINUTE))
        assertEquals("9h 59m", attentionDuration(9 * HOUR + 59 * MINUTE))
    }

    @Test
    fun `a whole number of hours drops the minutes`() {
        assertEquals("2h", attentionDuration(2 * HOUR))
    }

    @Test
    fun `nothing reads as a dash, because zero minutes is not a measurement`() {
        assertEquals("—", attentionDuration(0L))
    }

    @Test
    fun `seconds round down rather than inventing a minute`() {
        assertEquals("—", attentionDuration(59_000L))
        assertEquals("1m", attentionDuration(119_000L))
    }

    @Test
    fun `a negative figure is treated as nothing rather than rendered`() {
        assertEquals("—", attentionDuration(-1L))
    }

    @Test
    fun `a full day does not overflow into something unreadable`() {
        assertEquals("24h", attentionDuration(24 * HOUR))
    }
}
