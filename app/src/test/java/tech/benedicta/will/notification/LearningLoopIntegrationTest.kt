package tech.benedicta.will.notification

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryLearnedNoiseRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository
import tech.benedicta.will.task.Task
import tech.benedicta.will.task.TaskEventRecorder
import tech.benedicta.will.task.TaskOrigin
import tech.benedicta.will.task.TaskViewModel

/**
 * The loop as the user lives it: a notification arrives, they throw it away,
 * and eventually Will stops showing that one. Then they act on one and it
 * comes back. The unit tests pin each piece; this proves they are connected.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LearningLoopIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val tasks = MemoryTaskRepository()
    private val log = MemoryTaskEventRepository()
    private val learned = MemoryLearnedNoiseRepository()
    private val classifier = LearnedIntentionClassifier(PatternIntentionClassifier(), learned)

    private val viewModel by lazy {
        TaskViewModel(tasks, TaskEventRecorder(log), log, learned, testDispatcher)
    }

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private suspend fun aPromoArrives(): Task {
        val id = tasks.insertTask(
            Task(
                title = "Veja as novidades",
                text = "",
                sourcePackage = "com.shop",
                createdAt = 0L,
                origin = TaskOrigin.NOTIFICATION
            )
        )
        return tasks.getAllTasks().first().single { it.id == id }
    }

    private suspend fun judgePromo() =
        classifier.isIntention("Veja as novidades", "", "com.shop")

    @Test
    fun `throwing the same promo away three times stops it appearing`() = runTest {
        assertTrue("it reaches the feed the first time", judgePromo())

        repeat(3) { viewModel.deleteTask(aPromoArrives()) }

        assertFalse("after three, Will stops showing it", judgePromo())
    }

    @Test
    fun `two are not enough`() = runTest {
        repeat(2) { viewModel.deleteTask(aPromoArrives()) }

        assertTrue(judgePromo())
    }

    @Test
    fun `finishing one brings it back`() = runTest {
        repeat(3) { viewModel.deleteTask(aPromoArrives()) }
        assertFalse(judgePromo())

        viewModel.markDone(aPromoArrives())

        assertTrue("acting on it proves it mattered", judgePromo())
    }

    @Test
    fun `tidying up a task you already finished teaches nothing`() = runTest {
        repeat(3) {
            val done = aPromoArrives().copy(completedAt = 10L)
            tasks.updateTask(done)
            viewModel.deleteTask(done)
        }

        assertTrue("clearing the done screen is not a complaint", judgePromo())
    }

    @Test
    fun `a task you typed yourself never teaches Will to hide anything`() = runTest {
        repeat(5) {
            val id = tasks.insertTask(
                Task(
                    title = "Veja as novidades",
                    text = "",
                    sourcePackage = "com.shop",
                    createdAt = 0L,
                    origin = TaskOrigin.MANUAL
                )
            )
            viewModel.deleteTask(tasks.getAllTasks().first().single { it.id == id })
        }

        assertTrue(judgePromo())
    }

    @Test
    fun `clearing all data forgets what was learned`() = runTest {
        repeat(3) { viewModel.deleteTask(aPromoArrives()) }
        assertFalse(judgePromo())

        viewModel.deleteAllTasks()

        assertTrue(judgePromo())
    }
}
