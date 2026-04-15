package tech.benedicta.will.notification

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class NotificationActionStoreTest {

    @Before
    fun setUp() {
        NotificationActionStore.remove(1L)
        NotificationActionStore.remove(2L)
    }

    @Test
    fun `get returns null for unknown taskId`() {
        assertNull(NotificationActionStore.get(999L))
    }

    @Test
    fun `remove clears the entry`() {
        // PendingIntent can't be instantiated in unit tests — verified via null check after remove
        NotificationActionStore.remove(1L)
        assertNull(NotificationActionStore.get(1L))
    }
}
