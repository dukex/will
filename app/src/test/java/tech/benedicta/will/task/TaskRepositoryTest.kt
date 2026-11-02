package tech.benedicta.will.task

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryTest {

    @Test
    fun `insert task returns assigned id`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(title = "Test", text = "Body", sourcePackage = "com.test", createdAt = 0L)

        val id = repo.insertTask(task)

        assertEquals(1L, id)
    }

    @Test
    fun `getAllTasks emits inserted tasks`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(title = "Test", text = "Body", sourcePackage = "com.test", createdAt = 0L)
        repo.insertTask(task)

        val tasks = repo.getAllTasks().first()

        assertEquals(1, tasks.size)
        assertEquals("Test", tasks[0].title)
    }

    @Test
    fun `getAllTasks emits empty list when no tasks exist`() = runTest {
        val repo = FakeTaskRepository()

        val tasks = repo.getAllTasks().first()

        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `delete task removes it from getAllTasks`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(title = "Test", text = "Body", sourcePackage = "com.test", createdAt = 0L)
        val id = repo.insertTask(task)
        val inserted = task.copy(id = id)

        repo.deleteTask(inserted)
        val tasks = repo.getAllTasks().first()

        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `update task replaces existing task`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(title = "Old", text = "Body", sourcePackage = "com.test", createdAt = 0L)
        val id = repo.insertTask(task)
        val updated = task.copy(id = id, title = "New")

        repo.updateTask(updated)
        val tasks = repo.getAllTasks().first()

        assertEquals("New", tasks[0].title)
    }

    @Test
    fun `preserves notificationKey through insert and retrieve`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(
            title = "WhatsApp msg",
            text = "Hello",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            notificationKey = "0|com.whatsapp|123|null|10042"
        )

        val id = repo.insertTask(task)
        val tasks = repo.getAllTasks().first()

        assertEquals("0|com.whatsapp|123|null|10042", tasks[0].notificationKey)
    }

    @Test
    fun `duplicate conversationKey is ignored on insert`() = runTest {
        val repo = FakeTaskRepository()
        val task1 = Task(
            title = "First msg",
            text = "Hello",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            conversationKey = "com.whatsapp:chat123"
        )
        val task2 = Task(
            title = "Second msg",
            text = "World",
            sourcePackage = "com.whatsapp",
            createdAt = 2000L,
            conversationKey = "com.whatsapp:chat123"
        )

        repo.insertTask(task1)
        val id2 = repo.insertTask(task2)

        assertEquals(-1L, id2)
        val tasks = repo.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("First msg", tasks[0].title)
    }

    @Test
    fun `null conversationKey allows multiple inserts`() = runTest {
        val repo = FakeTaskRepository()
        val task1 = Task(title = "Generic 1", text = "", sourcePackage = "com.whatsapp", createdAt = 1000L)
        val task2 = Task(title = "Generic 2", text = "", sourcePackage = "com.whatsapp", createdAt = 2000L)

        repo.insertTask(task1)
        repo.insertTask(task2)

        val tasks = repo.getAllTasks().first()
        assertEquals(2, tasks.size)
    }

    @Test
    fun `upsertByConversationKey inserts new task with unreadCount 1`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            notificationKey = "key-1",
            conversationKey = "com.whatsapp:maria"
        )

        val id = repo.upsertByConversationKey(task).id
        val tasks = repo.getAllTasks().first()

        assertEquals(1L, id)
        assertEquals(1, tasks.size)
        assertEquals(1, tasks[0].unreadCount)
        assertEquals("key-1", tasks[0].notificationKey)
    }

    @Test
    fun `upsertByConversationKey updates open task with newer notificationKey and increments unreadCount`() = runTest {
        val repo = FakeTaskRepository()
        val first = Task(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            notificationKey = "key-1",
            conversationKey = "com.whatsapp:maria"
        )
        val second = first.copy(text = "How are you?", createdAt = 2000L, notificationKey = "key-2")

        val id1 = repo.upsertByConversationKey(first).id
        val id2 = repo.upsertByConversationKey(second).id
        val tasks = repo.getAllTasks().first()

        assertEquals(id1, id2)
        assertEquals(1, tasks.size)
        assertEquals("key-2", tasks[0].notificationKey)
        assertEquals(2000L, tasks[0].createdAt)
        assertEquals(2, tasks[0].unreadCount)
    }

    @Test
    fun `upsertByConversationKey accumulates unreadCount across multiple updates`() = runTest {
        val repo = FakeTaskRepository()
        val base = Task(
            title = "Maria",
            text = "msg",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            notificationKey = "key-1",
            conversationKey = "com.whatsapp:maria"
        )

        repo.upsertByConversationKey(base)
        repo.upsertByConversationKey(base.copy(notificationKey = "key-2", createdAt = 2000L))
        repo.upsertByConversationKey(base.copy(notificationKey = "key-3", createdAt = 3000L))

        val tasks = repo.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(3, tasks[0].unreadCount)
        assertEquals("key-3", tasks[0].notificationKey)
    }

    @Test
    fun `upsertByConversationKey creates new task when existing one is completed`() = runTest {
        val repo = FakeTaskRepository()
        val first = Task(
            title = "Maria",
            text = "Hi",
            sourcePackage = "com.whatsapp",
            createdAt = 1000L,
            notificationKey = "key-1",
            conversationKey = "com.whatsapp:maria"
        )
        val id1 = repo.upsertByConversationKey(first).id
        repo.updateTask(first.copy(id = id1, completedAt = 1500L, conversationKey = null))

        val second = first.copy(notificationKey = "key-2", createdAt = 2000L)
        val id2 = repo.upsertByConversationKey(second).id

        val tasks = repo.getAllTasks().first()
        assertEquals(2, tasks.size)
        assertTrue(id1 != id2)
    }

    @Test
    fun `upsertByConversationKey with null key always inserts`() = runTest {
        val repo = FakeTaskRepository()
        val task = Task(title = "Generic", text = "", sourcePackage = "com.whatsapp", createdAt = 1000L)

        val id1 = repo.upsertByConversationKey(task).id
        val id2 = repo.upsertByConversationKey(task.copy(createdAt = 2000L)).id

        assertTrue(id1 != id2)
        assertEquals(2, repo.getAllTasks().first().size)
    }

    // --- activation transaction (status derived from timestamps) ---

    private fun newTask(title: String, createdAt: Long = 0L) =
        Task(title = title, text = "", sourcePackage = "pkg", createdAt = createdAt)

    @Test
    fun `setActiveTask stamps startedAt on the new task and leaves it un-paused`() = runTest {
        val repo = FakeTaskRepository()
        val id = repo.insertTask(newTask("A"))

        repo.setActiveTask(id)

        val a = repo.getAllTasks().first().single { it.id == id }
        assertEquals(TaskStatus.IN_PROGRESS, a.status)
        assertEquals(null, a.pausedAt)
        assertTrue("startedAt should be stamped", a.startedAt != null)
    }

    @Test
    fun `setActiveTask pauses the previously active task`() = runTest {
        val repo = FakeTaskRepository()
        val a = repo.insertTask(newTask("A"))
        val b = repo.insertTask(newTask("B"))

        repo.setActiveTask(a)
        repo.setActiveTask(b)

        val tasks = repo.getAllTasks().first()
        val taskA = tasks.single { it.id == a }
        val taskB = tasks.single { it.id == b }
        assertEquals("A must become PAUSED", TaskStatus.PAUSED, taskA.status)
        assertEquals("B must become IN_PROGRESS", TaskStatus.IN_PROGRESS, taskB.status)
    }

    @Test
    fun `at most one task is IN_PROGRESS at any time`() = runTest {
        val repo = FakeTaskRepository()
        val a = repo.insertTask(newTask("A"))
        val b = repo.insertTask(newTask("B"))
        val c = repo.insertTask(newTask("C"))

        repo.setActiveTask(a)
        repo.setActiveTask(b)
        repo.setActiveTask(c)

        val inProgress = repo.getAllTasks().first().count { it.status == TaskStatus.IN_PROGRESS }
        assertEquals(1, inProgress)
    }

    @Test
    fun `resuming a previously paused task preserves its original startedAt`() = runTest {
        val repo = FakeTaskRepository()
        val a = repo.insertTask(newTask("A"))
        val b = repo.insertTask(newTask("B"))

        repo.setActiveTask(a)
        val originalStart = repo.getAllTasks().first().single { it.id == a }.startedAt
        repo.setActiveTask(b)
        repo.setActiveTask(a)

        val resumed = repo.getAllTasks().first().single { it.id == a }
        assertEquals("startedAt must not be reset on resume", originalStart, resumed.startedAt)
        assertEquals(TaskStatus.IN_PROGRESS, resumed.status)
    }

    @Test
    fun `completing the active task leaves no IN_PROGRESS row`() = runTest {
        val repo = FakeTaskRepository()
        val a = repo.insertTask(newTask("A"))
        repo.setActiveTask(a)

        val active = repo.getAllTasks().first().single { it.id == a }
        repo.updateTask(active.copy(completedAt = 999L))

        val tasks = repo.getAllTasks().first()
        assertTrue(tasks.none { it.status == TaskStatus.IN_PROGRESS })
    }

    @Test
    fun `clearActiveTask pauses the currently active task`() = runTest {
        val repo = FakeTaskRepository()
        val a = repo.insertTask(newTask("A"))
        repo.setActiveTask(a)

        repo.clearActiveTask()

        val task = repo.getAllTasks().first().single { it.id == a }
        assertEquals(TaskStatus.PAUSED, task.status)
    }
}

private class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1L

    override suspend fun insertTask(task: Task): Long {
        if (task.conversationKey != null &&
            tasks.any { it.conversationKey == task.conversationKey }) {
            return -1L
        }
        val id = nextId++
        tasks.add(task.copy(id = id))
        return id
    }

    override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks.toList())

    override suspend fun updateTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) tasks[index] = task
    }

    override suspend fun deleteTask(task: Task) {
        tasks.removeAll { it.id == task.id }
    }

    override suspend fun deleteTaskById(taskId: Long) {
        tasks.removeAll { it.id == taskId }
    }
    override suspend fun setActiveTask(taskId: Long) {
        val now = System.currentTimeMillis()
        val updated = tasks.map { task ->
            when {
                task.id == taskId -> task.copy(
                    startedAt = task.startedAt ?: now,
                    pausedAt = null
                )
                task.isActive -> task.copy(pausedAt = now)
                else -> task
            }
        }
        tasks.clear(); tasks.addAll(updated)
    }
    override suspend fun clearActiveTask() {
        val now = System.currentTimeMillis()
        val updated = tasks.map { task ->
            if (task.isActive) task.copy(pausedAt = now) else task
        }
        tasks.clear(); tasks.addAll(updated)
    }
    override suspend fun deleteAllTasks() { tasks.clear() }
    override suspend fun clearConversationKey(taskId: Long) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) tasks[index] = tasks[index].copy(conversationKey = null)
    }

    override suspend fun markStartedIfNeeded(taskId: Long, startedAt: Long) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1 && tasks[index].startedAt == null) {
            tasks[index] = tasks[index].copy(startedAt = startedAt)
        }
    }

    override suspend fun incrementContextSwitch(taskId: Long) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(
                contextSwitchCount = tasks[index].contextSwitchCount + 1
            )
        }
    }

    override suspend fun upsertByConversationKey(task: Task): TaskUpsert {
        val key = task.conversationKey
        if (key != null) {
            val index = tasks.indexOfFirst {
                it.conversationKey == key && it.completedAt == null
            }
            if (index != -1) {
                val existing = tasks[index]
                tasks[index] = existing.copy(
                    notificationKey = task.notificationKey ?: existing.notificationKey,
                    createdAt = task.createdAt,
                    title = task.title,
                    text = task.text,
                    unreadCount = existing.unreadCount + 1
                )
                return TaskUpsert(id = existing.id, created = false)
            }
        }
        val id = nextId++
        tasks.add(task.copy(id = id, unreadCount = 1))
        return TaskUpsert(id = id, created = true)
    }
}
