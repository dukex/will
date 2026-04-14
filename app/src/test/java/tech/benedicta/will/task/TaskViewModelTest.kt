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
        val id = repo.insertTask(Task(title = "A", text = "b", sourcePackage = "pkg", createdAt = 0L))
        val viewModel = TaskViewModel(repo, testDispatcher)

        viewModel.deleteTask(viewModel.tasks.value[0])

        assertTrue(viewModel.tasks.value.isEmpty())
    }
}

private class FakeViewModelTaskRepository : TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private var nextId = 1L

    override suspend fun insertTask(task: Task): Long {
        val id = nextId++
        _tasks.value = _tasks.value + task.copy(id = id)
        return id
    }

    override fun getAllTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun deleteTask(task: Task) {
        _tasks.value = _tasks.value.filter { it.id != task.id }
    }

    override suspend fun deleteTaskById(taskId: Long) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }
}
