package tech.benedicta.will.notification

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository

class NotificationDedupIntegrationTest {

    private val processor = NotificationProcessor()

    @Test
    fun `five notifications from the same WhatsApp contact collapse into one task`() = runTest {
        val repo = MemoryTaskRepository()

        repeat(5) { i ->
            val task = processor.process(
                title = "Maria",
                text = "msg $i",
                sourcePackage = "com.whatsapp",
                timestamp = 1000L + i,
                notificationKey = "key-$i"
            )!!
            repo.upsertByConversationKey(task)
        }

        val tasks = repo.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(5, tasks[0].unreadCount)
        assertEquals("key-4", tasks[0].notificationKey)
        assertEquals(1004L, tasks[0].createdAt)
    }

    @Test
    fun `five notifications from different contacts produce five separate tasks`() = runTest {
        val repo = MemoryTaskRepository()
        val contacts = listOf("Maria", "Joao", "Ana", "Pedro", "Lucia")

        contacts.forEachIndexed { i, name ->
            val task = processor.process(
                title = name,
                text = "msg",
                sourcePackage = "com.whatsapp",
                timestamp = 1000L + i,
                notificationKey = "key-$name"
            )!!
            repo.upsertByConversationKey(task)
        }

        val tasks = repo.getAllTasks().first()
        assertEquals(5, tasks.size)
        tasks.forEach { assertEquals(1, it.unreadCount) }
    }

    @Test
    fun `tap target keeps pointing at the most recent notification after multiple updates`() = runTest {
        val repo = MemoryTaskRepository()

        repeat(3) { i ->
            val task = processor.process(
                title = "Maria",
                text = "msg $i",
                sourcePackage = "com.whatsapp",
                timestamp = 1000L + i,
                notificationKey = "key-$i"
            )!!
            repo.upsertByConversationKey(task)
        }

        val task = repo.getAllTasks().first().single()
        assertEquals("key-2", task.notificationKey)
    }

    @Test
    fun `completing a conversation does not resurrect the task on next notification`() = runTest {
        val repo = MemoryTaskRepository()

        val first = processor.process(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            timestamp = 1000L,
            notificationKey = "key-1"
        )!!
        val firstId = repo.upsertByConversationKey(first).id

        repo.updateTask(
            repo.getAllTasks().first().single().copy(
                completedAt = 1500L,
                conversationKey = null
            )
        )

        val second = processor.process(
            title = "Maria",
            text = "Are you there?",
            sourcePackage = "com.whatsapp",
            timestamp = 2000L,
            notificationKey = "key-2"
        )!!
        val secondId = repo.upsertByConversationKey(second).id

        val all = repo.getAllTasks().first()
        assertEquals(2, all.size)
        assert(firstId != secondId)
        val open = all.single { it.completedAt == null }
        assertEquals(1, open.unreadCount)
        assertEquals("key-2", open.notificationKey)
    }

    @Test
    fun `cross-app notifications with same title stay separate`() = runTest {
        val repo = MemoryTaskRepository()

        val whats = processor.process("Maria", "Hi", "com.whatsapp", 1000L, "k-w")!!
        val tg = processor.process("Maria", "Hi", "org.telegram.messenger", 1100L, "k-t")!!

        repo.upsertByConversationKey(whats)
        repo.upsertByConversationKey(tg)

        val tasks = repo.getAllTasks().first()
        assertEquals(2, tasks.size)
    }

    @Test
    fun `whitespace and case variations on title still collapse into one task`() = runTest {
        val repo = MemoryTaskRepository()

        val variants = listOf("Maria", "  maria  ", "MARIA", "  Maria")
        variants.forEachIndexed { i, title ->
            val task = processor.process(
                title = title,
                text = "msg",
                sourcePackage = "com.whatsapp",
                timestamp = 1000L + i,
                notificationKey = "key-$i"
            )
            assertNotNull(task)
            repo.upsertByConversationKey(task!!)
        }

        val tasks = repo.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(4, tasks[0].unreadCount)
    }


    @Test
    fun `only a genuinely new conversation reports itself as created`() = runTest {
        val repo = MemoryTaskRepository()
        val first = processor.process("Maria", "Hi", "com.whatsapp", 1000L, "k-1")!!
        val second = processor.process("Maria", "Still there?", "com.whatsapp", 2000L, "k-2")!!

        val firstUpsert = repo.upsertByConversationKey(first)
        val secondUpsert = repo.upsertByConversationKey(second)

        assertTrue("a new conversation is a creation", firstUpsert.created)
        assertFalse("a further message only bumps it", secondUpsert.created)
        assertEquals(firstUpsert.id, secondUpsert.id)
    }

    @Test
    fun `a conversation that starts again after being finished reports a creation`() = runTest {
        val repo = MemoryTaskRepository()
        val first = processor.process("Maria", "Hi", "com.whatsapp", 1000L, "k-1")!!
        repo.upsertByConversationKey(first)
        repo.updateTask(
            repo.getAllTasks().first().single().copy(completedAt = 1500L, conversationKey = null)
        )

        val second = processor.process("Maria", "Back", "com.whatsapp", 2000L, "k-2")!!
        val secondUpsert = repo.upsertByConversationKey(second)

        assertTrue(secondUpsert.created)
    }
}
