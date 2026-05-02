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

    @Test
    fun `rerun clears the persisted flag and re-anchors to first ungranted step`() = runTest {
        val repo = MemoryOnboardingRepository(initial = true)
        val inspector = MutablePermissionInspector(default = true, overlay = false, listener = false)
        val vm = OnboardingViewModel(repo, inspector, dispatcher = testDispatcher)

        vm.rerun()

        assertFalse(repo.isCompleted().first())
        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `consecutive refreshes do not skip ungranted steps`() = runTest {
        val inspector = MutablePermissionInspector(default = false, overlay = false, listener = false)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )

        // user grants only the default-launcher role and returns;
        // both onResume and the ActivityResultLauncher fire refresh().
        inspector.default = true
        vm.refresh()
        vm.refresh()

        // overlay is still ungranted, so the flow must hold on Overlay
        // instead of cascading into Notifications.
        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `refresh skips a step the user already granted out-of-band`() = runTest {
        // listener was granted from a prior install, and the user grants
        // overlay during this onboarding run. Refresh should land on Done
        // instead of forcing the user through the redundant Notifications step.
        val inspector = MutablePermissionInspector(default = true, overlay = false, listener = true)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )
        assertEquals(OnboardingStep.Overlay, vm.state.value.step)

        inspector.overlay = true
        vm.refresh()

        assertEquals(OnboardingStep.Done, vm.state.value.step)
    }

    @Test
    fun `rerun lands on Done when every grant is already in`() = runTest {
        val repo = MemoryOnboardingRepository(initial = true)
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = true)
        val vm = OnboardingViewModel(repo, inspector, dispatcher = testDispatcher)

        vm.rerun()

        assertEquals(OnboardingStep.Done, vm.state.value.step)
        assertFalse(repo.isCompleted().first())
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
