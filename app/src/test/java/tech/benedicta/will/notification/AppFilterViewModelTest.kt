package tech.benedicta.will.notification

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppFilterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `filters starts empty`() = runTest {
        val vm = AppFilterViewModel(FakeAppFilterRepository(), testDispatcher)
        assertTrue(vm.filters.value.isEmpty())
    }

    @Test
    fun `toggle disabled app emits updated list`() = runTest {
        val repo = FakeAppFilterRepository()
        repo.addApp("com.example", "Example")
        val vm = AppFilterViewModel(repo, testDispatcher)

        vm.toggle("com.example", false)

        assertFalse("toggle to false should disable", vm.filters.value.first().isEnabled)
    }

    @Test
    fun `toggle re-enables disabled app`() = runTest {
        val repo = FakeAppFilterRepository()
        repo.addApp("com.example", "Example")
        repo.setEnabled("com.example", false)
        val vm = AppFilterViewModel(repo, testDispatcher)

        vm.toggle("com.example", true)

        assertTrue("toggle to true should enable", vm.filters.value.first().isEnabled)
    }

    @Test
    fun `filters are sorted alphabetically by app name`() = runTest {
        val repo = FakeAppFilterRepository()
        repo.addApp("com.c", "Charlie")
        repo.addApp("com.a", "Alpha")
        repo.addApp("com.b", "Bravo")
        val vm = AppFilterViewModel(repo, testDispatcher)

        val names = vm.filters.value.map { it.appName }
        assertEquals(listOf("Alpha", "Bravo", "Charlie"), names)
    }

    @Test
    fun `toggling app off does not change list position`() = runTest {
        val repo = FakeAppFilterRepository()
        repo.addApp("com.a", "Alpha")
        repo.addApp("com.b", "Bravo")
        repo.addApp("com.c", "Charlie")
        val vm = AppFilterViewModel(repo, testDispatcher)

        vm.toggle("com.a", false)

        val names = vm.filters.value.map { it.appName }
        assertEquals(listOf("Alpha", "Bravo", "Charlie"), names)
    }

    @Test
    fun `filters list reflects repository state after toggle`() = runTest {
        val repo = FakeAppFilterRepository()
        repo.addApp("com.a", "App A")
        repo.addApp("com.b", "App B")
        val vm = AppFilterViewModel(repo, testDispatcher)

        vm.toggle("com.a", false)

        val disabled = vm.filters.value.find { it.packageName == "com.a" }
        assertFalse("com.a should be disabled", disabled!!.isEnabled)
        val enabled = vm.filters.value.find { it.packageName == "com.b" }
        assertTrue("com.b should remain enabled", enabled!!.isEnabled)
    }
}

private class FakeAppFilterRepository : AppFilterRepository {
    private val _filters = MutableStateFlow<List<AppNotificationFilter>>(emptyList())

    override fun getAllFilters(): Flow<List<AppNotificationFilter>> = _filters.asStateFlow()

    override suspend fun setEnabled(packageName: String, enabled: Boolean) {
        _filters.value = sorted(
            _filters.value.map {
                if (it.packageName == packageName) it.copy(isEnabled = enabled) else it
            }
        )
    }

    override suspend fun addApp(packageName: String, appName: String) {
        if (_filters.value.none { it.packageName == packageName }) {
            _filters.value = sorted(
                _filters.value + AppNotificationFilter(packageName, appName, true)
            )
        }
    }

    private fun sorted(list: List<AppNotificationFilter>) =
        list.sortedBy { it.appName }

    override fun isEnabled(packageName: String): Boolean =
        _filters.value.find { it.packageName == packageName }?.isEnabled ?: true
}
