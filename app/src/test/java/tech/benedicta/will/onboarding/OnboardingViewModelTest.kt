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
    fun `refresh advances when the required grant just transitioned to true`() = runTest {
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

        // overlay is still ungranted, so the flow must hold on Overlay.
        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `consecutive refreshes hold the step when the user has prior grants on later steps`() = runTest {
        // Simulates the prior-install scenario that originally triggered "finish
        // after step 1": overlay and listener were granted previously, the user
        // is freshly running onboarding and just granted default. Refresh should
        // advance one step (DL -> Overlay) and stop, even though Overlay's grant
        // is already in.
        val inspector = MutablePermissionInspector(default = false, overlay = true, listener = true)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )
        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)

        inspector.default = true
        vm.refresh()
        vm.refresh()

        assertEquals(OnboardingStep.Overlay, vm.state.value.step)
    }

    @Test
    fun `next advances when the current step's grant is already in`() = runTest {
        // The screen calls vm.next() when the user taps the action button on a
        // step they have already cleared (re-run scenario, or grant was in
        // before they reached the step).
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = false)
        val vm = OnboardingViewModel(
            repository = MemoryOnboardingRepository(),
            inspector = inspector,
            dispatcher = testDispatcher
        )
        assertEquals(OnboardingStep.Notifications, vm.state.value.step)

        // simulate the user being on Overlay even though it is granted: rerun
        // anchored at DL
        vm.rerun()
        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)

        vm.next() // grant in, advance DL -> Overlay
        assertEquals(OnboardingStep.Overlay, vm.state.value.step)

        vm.next() // grant in, advance Overlay -> Notifications
        assertEquals(OnboardingStep.Notifications, vm.state.value.step)
    }

    @Test
    fun `rerun anchors the user back at step 1 even with all permissions granted`() = runTest {
        val repo = MemoryOnboardingRepository(initial = true)
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = true)
        val vm = OnboardingViewModel(repo, inspector, dispatcher = testDispatcher)

        vm.rerun()

        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)
        assertFalse(repo.isCompleted().first())
    }

    @Test
    fun `rerun resets observed grants so refresh does not skip steps`() = runTest {
        val repo = MemoryOnboardingRepository(initial = true)
        val inspector = MutablePermissionInspector(default = true, overlay = true, listener = true)
        val vm = OnboardingViewModel(repo, inspector, dispatcher = testDispatcher)

        vm.rerun()
        vm.refresh() // simulates the user returning from settings without changing anything

        // No grant transition happened, so the flow must not auto-advance.
        assertEquals(OnboardingStep.DefaultLauncher, vm.state.value.step)
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
