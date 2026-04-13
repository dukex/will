package tech.benedicta.will.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationActionStoreTest {

    @Before
    fun setUp() {
        NotificationActionStore.clear()
    }

    @Test
    fun `get returns stored action for known taskId`() {
        var executed = false
        val action = NotificationAction { executed = true }
        NotificationActionStore.put(1L, action)

        val retrieved = NotificationActionStore.get(1L)
        retrieved?.execute()

        assertTrue(executed)
    }

    @Test
    fun `get returns null for unknown taskId`() {
        assertNull(NotificationActionStore.get(999L))
    }

    @Test
    fun `remove clears the entry`() {
        NotificationActionStore.put(1L, NotificationAction {})
        NotificationActionStore.remove(1L)

        assertNull(NotificationActionStore.get(1L))
    }

    @Test
    fun `clear removes all entries`() {
        NotificationActionStore.put(1L, NotificationAction {})
        NotificationActionStore.put(2L, NotificationAction {})
        NotificationActionStore.clear()

        assertNull(NotificationActionStore.get(1L))
        assertNull(NotificationActionStore.get(2L))
    }

    @Test
    fun `put overwrites existing action for same taskId`() {
        var firstCalled = false
        var secondCalled = false
        NotificationActionStore.put(1L, NotificationAction { firstCalled = true })
        NotificationActionStore.put(1L, NotificationAction { secondCalled = true })

        NotificationActionStore.get(1L)?.execute()

        assertTrue(secondCalled)
        assertEquals(false, firstCalled)
    }
}
