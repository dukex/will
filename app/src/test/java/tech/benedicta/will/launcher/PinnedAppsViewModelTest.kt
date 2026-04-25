package tech.benedicta.will.launcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import tech.benedicta.will.infrastructure.memory.MemoryPinnedAppsRepository

@OptIn(ExperimentalCoroutinesApi::class)
class PinnedAppsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private val whatsApp = LauncherApp("com.whatsapp", "WhatsApp")
    private val telegram = LauncherApp("com.telegram", "Telegram")
    private val signal = LauncherApp("org.signal", "Signal")

    private fun installed(vararg apps: LauncherApp): StateFlow<List<LauncherApp>> =
        MutableStateFlow(apps.toList()).asStateFlow()

    @Test
    fun `pinned apps list starts empty`() = runTest {
        val vm = PinnedAppsViewModel(
            MemoryPinnedAppsRepository(),
            installed(whatsApp, telegram),
            testDispatcher
        )
        assertTrue(vm.pinnedApps.value.isEmpty())
    }

    @Test
    fun `pin reflects in pinnedApps state`() = runTest {
        val vm = PinnedAppsViewModel(
            MemoryPinnedAppsRepository(),
            installed(whatsApp, telegram),
            testDispatcher
        )

        vm.pin("com.whatsapp")

        assertEquals(listOf(whatsApp), vm.pinnedApps.value)
    }

    @Test
    fun `pinned apps order matches pin order, not installed order`() = runTest {
        val vm = PinnedAppsViewModel(
            MemoryPinnedAppsRepository(),
            installed(whatsApp, telegram, signal),
            testDispatcher
        )

        vm.pin("org.signal")
        vm.pin("com.whatsapp")

        assertEquals(listOf(signal, whatsApp), vm.pinnedApps.value)
    }

    @Test
    fun `uninstalled pinned packages are filtered out`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        repo.pin("com.whatsapp")
        repo.pin("com.gone")
        val vm = PinnedAppsViewModel(repo, installed(whatsApp), testDispatcher)

        assertEquals(listOf(whatsApp), vm.pinnedApps.value)
    }

    @Test
    fun `unpin removes app from state`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        repo.pin("com.whatsapp")
        repo.pin("com.telegram")
        val vm = PinnedAppsViewModel(repo, installed(whatsApp, telegram), testDispatcher)

        vm.unpin("com.whatsapp")

        assertEquals(listOf(telegram), vm.pinnedApps.value)
    }

    @Test
    fun `togglePin pins when not pinned`() = runTest {
        val vm = PinnedAppsViewModel(
            MemoryPinnedAppsRepository(),
            installed(whatsApp),
            testDispatcher
        )

        vm.togglePin("com.whatsapp")

        assertEquals(listOf(whatsApp), vm.pinnedApps.value)
    }

    @Test
    fun `togglePin unpins when already pinned`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        repo.pin("com.whatsapp")
        val vm = PinnedAppsViewModel(repo, installed(whatsApp), testDispatcher)

        vm.togglePin("com.whatsapp")

        assertTrue(vm.pinnedApps.value.isEmpty())
    }

    @Test
    fun `canPinMore is true under limit`() = runTest {
        val vm = PinnedAppsViewModel(
            MemoryPinnedAppsRepository(),
            installed(whatsApp),
            testDispatcher
        )

        assertTrue(vm.canPinMore.value)
        vm.pin("com.whatsapp")
        assertTrue(vm.canPinMore.value)
    }

    @Test
    fun `canPinMore is false at limit`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        val apps = (1..5).map { LauncherApp("com.app$it", "App$it") }
        apps.forEach { repo.pin(it.packageName) }
        val vm = PinnedAppsViewModel(repo, MutableStateFlow(apps).asStateFlow(), testDispatcher)

        assertFalse(vm.canPinMore.value)
    }

    @Test
    fun `pin returns false when over limit`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        val apps = (1..5).map { LauncherApp("com.app$it", "App$it") } + whatsApp
        apps.take(5).forEach { repo.pin(it.packageName) }
        val vm = PinnedAppsViewModel(repo, MutableStateFlow(apps).asStateFlow(), testDispatcher)

        val result = vm.pin("com.whatsapp")

        assertFalse("pin must be rejected at limit", result)
        assertFalse(vm.pinnedApps.value.contains(whatsApp))
    }

    @Test
    fun `isPinned reflects state`() = runTest {
        val repo = MemoryPinnedAppsRepository()
        repo.pin("com.whatsapp")
        val vm = PinnedAppsViewModel(repo, installed(whatsApp, telegram), testDispatcher)

        assertTrue(vm.isPinned("com.whatsapp"))
        assertFalse(vm.isPinned("com.telegram"))
    }
}