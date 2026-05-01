package tech.benedicta.will.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryOnboardingRepository

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state lands on first ungranted step`() = runTest {
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = MutablePermissionInspector(default = false, overlay = false, listener = false),
            dispatcher = testDispatcher
        )

        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)
    }

    @Test
    fun `initial state resumes mid-flow when default launcher is already set`() = runTest {
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = MutablePermissionInspector(default = true, overlay = false, listener = false),
            dispatcher = testDispatcher
        )

        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `next refuses to advance a required step that is not satisfied`() = runTest {
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = MutablePermissionInspector(default = false, overlay = false, listener = false),
            dispatcher = testDispatcher
        )

        vm.next()

        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)
    }

    @Test
    fun `next advances when the required grant is in`() = runTest {
        val inspector = MutablePermissionInspector(default = false, overlay = false, listener = false)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )
        inspector.default = true
        vm.refresh()

        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `skip advances on optional step`() = runTest {
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = false)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )

        vm.skip()

        assertEquals(OnboardingStep.Done, vm.state.value.step)
    }

    @Test
    fun `skip is a no-op on required step`() = runTest {
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = MutablePermissionInspector(default = false, overlay = false, listener = false),
            dispatcher = testDispatcher
        )

        vm.skip()

        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)
    }

    @Test
    fun `reaching Done marks the repository as completed`() = runTest {
        val repo = MemoryOnboardingRepository()
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = true)
        val vm = OnboardingViewModel(repo, inspector, dispatcher = testDispatcher)

        // initial state already at Done because everything granted; persist runs
        vm.refresh()

        assertTrue(repo.isCompleted().first())
    }

    @Test
    fun `does not mark completed before Done`() = runTest {
        val repo = MemoryOnboardingRepository()
        val vm = OnboardingViewModel(
            repository = repo,
            inspector = MutablePermissionInspector(default = false, overlay = false, listener = false),
            dispatcher = testDispatcher
        )

        vm.next() // refused — required step ungranted

        assertFalse(repo.isCompleted().first())
    }

    @Test
    fun `refresh auto-advances when the user grants a step in Settings`() = runTest {
        val inspector = MutablePermissionInspector(default = false, overlay = false, listener = false)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )
        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)

        inspector.default = true
        vm.refresh()

        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }
}

private class MutablePermissionInspector(
    var default: Boolean,
    var overlay: Boolean,
    var listener: Boolean
) : PermissionInspector {
    override fun isDefaultLauncher(): Boolean = default
    override fun hasOverlayPermission(): Boolean = overlay
    override fun hasNotificationListener(): Boolean = listener
}
