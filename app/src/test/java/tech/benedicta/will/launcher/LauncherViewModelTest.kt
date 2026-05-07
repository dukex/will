package tech.benedicta.will.launcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LauncherViewModelTest {

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
    fun `given empty repository, apps state is empty list`() = runTest {
        val viewModel = LauncherViewModel(FakeAppRepository(emptyList()), testDispatcher)
        assertEquals(emptyList<LauncherApp>(), viewModel.apps.value)
    }

    @Test
    fun `given repository with apps, apps state reflects repository content`() = runTest {
        val expectedApps = listOf(
            LauncherApp("com.example", "Example App"),
            LauncherApp("com.test", "Test App")
        )
        val viewModel = LauncherViewModel(FakeAppRepository(expectedApps), testDispatcher)
        assertEquals(expectedApps, viewModel.apps.value)
    }

    @Test
    fun `given empty query, filteredApps returns all apps`() = runTest {
        val apps = listOf(
            LauncherApp("com.a", "WhatsApp"),
            LauncherApp("com.b", "Telegram")
        )
        val viewModel = LauncherViewModel(FakeAppRepository(apps), testDispatcher)

        assertEquals(apps, viewModel.filteredApps.value)
    }

    @Test
    fun `given query matching app name, filteredApps returns only matching apps`() = runTest {
        val whatsapp = LauncherApp("com.a", "WhatsApp")
        val telegram = LauncherApp("com.b", "Telegram")
        val viewModel = LauncherViewModel(FakeAppRepository(listOf(whatsapp, telegram)), testDispatcher)

        viewModel.setQuery("What")

        assertEquals(listOf(whatsapp), viewModel.filteredApps.value)
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val whatsapp = LauncherApp("com.a", "WhatsApp")
        val viewModel = LauncherViewModel(FakeAppRepository(listOf(whatsapp)), testDispatcher)

        viewModel.setQuery("whatsapp")

        assertEquals(listOf(whatsapp), viewModel.filteredApps.value)
    }

    @Test
    fun `given query not matching any app, filteredApps returns empty list`() = runTest {
        val apps = listOf(LauncherApp("com.a", "WhatsApp"))
        val viewModel = LauncherViewModel(FakeAppRepository(apps), testDispatcher)

        viewModel.setQuery("zzz")

        assertEquals(emptyList<LauncherApp>(), viewModel.filteredApps.value)
    }

    @Test
    fun `clearing query after search restores all apps`() = runTest {
        val apps = listOf(
            LauncherApp("com.a", "WhatsApp"),
            LauncherApp("com.b", "Telegram")
        )
        val viewModel = LauncherViewModel(FakeAppRepository(apps), testDispatcher)
        viewModel.setQuery("What")
        viewModel.setQuery("")

        assertEquals(apps, viewModel.filteredApps.value)
    }

    // Regression: pinned apps still showed an uninstalled package because the
    // installed-apps list was loaded once in init and never refreshed.
    @Test
    fun `refresh re-queries the repository so uninstalled apps disappear`() = runTest {
        val whatsapp = LauncherApp("com.whatsapp", "WhatsApp")
        val telegram = LauncherApp("org.telegram.messenger", "Telegram")
        val repository = MutableFakeAppRepository(listOf(whatsapp, telegram))
        val viewModel = LauncherViewModel(repository, testDispatcher)
        assertEquals(listOf(whatsapp, telegram), viewModel.apps.value)

        repository.setApps(listOf(whatsapp))
        viewModel.refresh()

        assertEquals(listOf(whatsapp), viewModel.apps.value)
    }
}

private class FakeAppRepository(private val apps: List<LauncherApp>) : AppRepository {
    override fun getInstalledApps(): List<LauncherApp> = apps
}

private class MutableFakeAppRepository(initial: List<LauncherApp>) : AppRepository {
    private var apps: List<LauncherApp> = initial
    fun setApps(next: List<LauncherApp>) { apps = next }
    override fun getInstalledApps(): List<LauncherApp> = apps
}
