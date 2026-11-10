package tech.benedicta.will.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryLearnedNoiseRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskEventRepository
import tech.benedicta.will.infrastructure.memory.MemoryTaskRepository

private const val MINUTE = 60_000L

/**
 * End to end over the pieces US-12 and the inference put together: a real
 * repository, the recorder writing the log, and the view model reading it back
 * as a suggestion. The unit tests pin the rule; this one proves the wiring,
 * which is where the parts stop agreeing in practice.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompletionSuggestionIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val tasks = MemoryTaskRepository()
    private val log = MemoryTaskEventRepository()

    @Before
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModelAt(instant: Long) = TaskViewModel(
        repository = tasks,
        events = TaskEventRecorder(log),
        eventLog = log,
        learnedNoise = MemoryLearnedNoiseRepository(),
        dispatcher = testDispatcher,
        now = { instant }
    )

    private suspend fun aTaskWorkedOnThenLeft(): Long {
        val id = tasks.insertTask(
            Task(title = "Sarah", text = "review?", sourcePackage = "com.slack", createdAt = 0L)
        )
        val recorder = TaskEventRecorder(log, now = { 0L })
        recorder.created(id)
        TaskEventRecorder(log, now = { 1 * MINUTE }).started(id, 1 * MINUTE)
        TaskEventRecorder(log, now = { 5 * MINUTE })
            .switchedAway(id, foregroundPackage = "tech.benedicta.will")
        tasks.markStartedIfNeeded(id, 1 * MINUTE)
        return id
    }

    @Test
    fun `a task worked on and left alone surfaces as a suggestion in the feed`() = runTest {
        val id = aTaskWorkedOnThenLeft()

        val suggested = viewModelAt(40 * MINUTE).completionSuggestions.first { it.isNotEmpty() }

        assertEquals(setOf(id), suggested)
    }

    @Test
    fun `the same task is not suggested while the user is still on it`() = runTest {
        aTaskWorkedOnThenLeft()

        val suggested = viewModelAt(7 * MINUTE).completionSuggestions.first()

        assertTrue(suggested.isEmpty())
    }

    @Test
    fun `completing the task through the view model clears the suggestion`() = runTest {
        val id = aTaskWorkedOnThenLeft()
        val viewModel = viewModelAt(40 * MINUTE)
        assertEquals(setOf(id), viewModel.completionSuggestions.first { it.isNotEmpty() })

        viewModel.markDone(viewModel.tasks.value.single { it.id == id })

        assertTrue(viewModel.completionSuggestions.first().isEmpty())
    }

    @Test
    fun `a notification nobody opened never turns into a suggestion`() = runTest {
        val id = tasks.insertTask(
            Task(title = "Promo", text = "", sourcePackage = "com.shop", createdAt = 0L)
        )
        TaskEventRecorder(log, now = { 0L }).created(id)

        val suggested = viewModelAt(10 * MINUTE * 60).completionSuggestions.first()

        assertTrue("an untouched task is not finished, it is ignored", suggested.isEmpty())
    }
}
