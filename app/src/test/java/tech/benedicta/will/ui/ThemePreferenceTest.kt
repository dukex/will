package tech.benedicta.will.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferenceTest {

    @Test
    fun `next cycles SYSTEM to DARK`() {
        assertEquals(ThemeMode.DARK, ThemeMode.SYSTEM.next())
    }

    @Test
    fun `next cycles DARK to LIGHT`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.DARK.next())
    }

    @Test
    fun `next cycles LIGHT back to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.LIGHT.next())
    }

    @Test
    fun `fromOrdinal returns correct mode`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromOrdinal(0))
        assertEquals(ThemeMode.DARK, ThemeMode.fromOrdinal(1))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromOrdinal(2))
    }

    @Test
    fun `fromOrdinal returns SYSTEM for unknown ordinal`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromOrdinal(-1))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromOrdinal(99))
    }
}
