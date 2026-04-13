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
        // Fails: ViewModel stub does not load apps from repository
        assertEquals(expectedApps, viewModel.apps.value)
    }
}

private class FakeAppRepository(private val apps: List<LauncherApp>) : AppRepository {
    override fun getInstalledApps(): List<LauncherApp> = apps
}
