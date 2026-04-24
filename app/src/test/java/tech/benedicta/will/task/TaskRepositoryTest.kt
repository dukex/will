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
        val updated = tasks.map { it.copy(isActive = it.id == taskId) }
        tasks.clear(); tasks.addAll(updated)
    }
    override suspend fun clearActiveTask() {
        val updated = tasks.map { it.copy(isActive = false) }
        tasks.clear(); tasks.addAll(updated)
    }
    override suspend fun deleteAllTasks() { tasks.clear() }
    override suspend fun clearConversationKey(taskId: Long) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) tasks[index] = tasks[index].copy(conversationKey = null)
    }
}
