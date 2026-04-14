package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationKeyTrackerTest {

    private val tracker = NotificationKeyTracker()

    @Test
    fun `resolve returns taskId for tracked key`() {
        tracker.track("key-1", 42L)

        assertEquals(42L, tracker.resolve("key-1"))
    }

    @Test
    fun `resolve returns null for unknown key`() {
        assertNull(tracker.resolve("unknown"))
    }

    @Test
    fun `forget removes the key`() {
        tracker.track("key-1", 1L)
        tracker.forget("key-1")

        assertNull(tracker.resolve("key-1"))
    }

    @Test
    fun `track overwrites existing entry for same key`() {
        tracker.track("key-1", 1L)
        tracker.track("key-1", 99L)

        assertEquals(99L, tracker.resolve("key-1"))
    }

    @Test
    fun `forget on unknown key does not throw`() {
        tracker.forget("nonexistent")
    }
}
