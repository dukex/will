package tech.benedicta.will.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tasks state starts empty when repository is empty`() = runTest {
        val viewModel = TaskViewModel(FakeViewModelTaskRepository(), testDispatcher)

        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `tasks state reflects repository tasks`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "A", text = "body", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)

        assertEquals(1, viewModel.tasks.value.size)
        assertEquals("A", viewModel.tasks.value[0].title)
    }

    @Test
    fun `addTask inserts task into repository`() = runTest {
        val repo = FakeViewModelTaskRepository()
        val viewModel = TaskViewModel(repo, testDispatcher)
        val task = Task(title = "New task", text = "body", sourcePackage = "pkg", createdAt = 0L)

        viewModel.addTask(task)

        assertEquals(1, viewModel.tasks.value.size)
        assertEquals("New task", viewModel.tasks.value[0].title)
    }

    @Test
    fun `deleteTask removes task from repository`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "A", text = "b", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)

        viewModel.deleteTask(viewModel.tasks.value[0])

        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `markDone sets completedAt on the task`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "A", text = "b", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)

        viewModel.markDone(viewModel.tasks.value[0])

        val updated = viewModel.tasks.value.find { it.title == "A" }
        assertNotNull("task should still exist in repo", updated)
        assertNotNull("completedAt should be set", updated?.completedAt)
    }

    // --- handleOverlayAction regression tests ---

    @Test
    fun `handleOverlayAction ACTION_DONE marks task as done`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "Buy milk", text = "", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)
        val taskId = viewModel.tasks.value[0].id

        viewModel.handleOverlayAction("tech.benedicta.will.TASK_DONE", taskId)

        val updated = repo.all.find { it.id == taskId }
        assertNotNull("task should still exist", updated)
        assertNotNull("completedAt should be set", updated?.completedAt)
    }

    @Test
    fun `handleOverlayAction ACTION_DELETE removes task`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "Buy milk", text = "", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)
        val taskId = viewModel.tasks.value[0].id

        viewModel.handleOverlayAction("tech.benedicta.will.TASK_DELETE", taskId)

        assertTrue("task should be deleted", repo.all.none { it.id == taskId })
    }

    @Test
    fun `handleOverlayAction ACTION_LATER leaves task unchanged`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "Buy milk", text = "", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)
        val taskId = viewModel.tasks.value[0].id

        viewModel.handleOverlayAction("tech.benedicta.will.TASK_LATER", taskId)

        val task = repo.all.find { it.id == taskId }
        assertNotNull("task should still exist", task)
        assertNull("completedAt should remain null", task?.completedAt)
    }

    @Test
    fun `handleOverlayAction with unknown taskId is a no-op`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "Buy milk", text = "", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)

        viewModel.handleOverlayAction("tech.benedicta.will.TASK_DONE", taskId = 999L)

        assertEquals(1, repo.all.size)
    }

    @Test
    fun `handleOverlayAction clears active task state`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "Buy milk", text = "", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)
        viewModel.setActiveTask(viewModel.tasks.value[0])
        val taskId = viewModel.tasks.value[0].id

        viewModel.handleOverlayAction("tech.benedicta.will.TASK_DONE", taskId)

        assertNull("active task should be cleared", viewModel.activeTask.value)
    }

    @Test
    fun `markDone persists completedAt timestamp`() = runTest {
        val repo = FakeViewModelTaskRepository()
        repo.insertTask(Task(title = "A", text = "b", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)
        val before = System.currentTimeMillis()

        viewModel.markDone(viewModel.tasks.value[0])

        val completed = repo.lastUpdated
        assertTrue(completed != null && completed >= before)
    }
}

private class FakeViewModelTaskRepository : TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L
    var lastUpdated: Long? = null
    val all: List<Task> get() = _tasks.value

    override suspend fun insertTask(task: Task): Long {
        val id = nextId++
        _tasks.value = _tasks.value + task.copy(id = id)
        return id
    }

    override fun getAllTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun updateTask(task: Task) {
        lastUpdated = task.completedAt
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun deleteTask(task: Task) {
        _tasks.value = _tasks.value.filter { it.id != task.id }
    }

    override suspend fun deleteTaskById(taskId: Long) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }
    override suspend fun setActiveTask(taskId: Long) {
        _tasks.value = _tasks.value.map { it.copy(isActive = it.id == taskId) }
    }
    override suspend fun clearActiveTask() {
        _tasks.value = _tasks.value.map { it.copy(isActive = false) }
    }
    override suspend fun deleteAllTasks() { _tasks.value = emptyList() }
}
