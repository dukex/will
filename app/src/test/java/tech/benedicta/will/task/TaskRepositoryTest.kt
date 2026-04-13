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
}

private class FakeTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1L

    override suspend fun insertTask(task: Task): Long {
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
}
