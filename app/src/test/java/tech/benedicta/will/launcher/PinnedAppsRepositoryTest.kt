package tech.benedicta.will.launcher

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryPinnedAppsRepository

class PinnedAppsRepositoryTest {

    private fun newRepo(): PinnedAppsRepository = MemoryPinnedAppsRepository()

    @Test
    fun `starts empty`() = runTest {
        val repo = newRepo()
        assertTrue(repo.getPinnedPackages().first().isEmpty())
    }

    @Test
    fun `pin adds package to list`() = runTest {
        val repo = newRepo()
        val result = repo.pin("com.example")

        assertTrue("pin should succeed", result)
        assertEquals(listOf("com.example"), repo.getPinnedPackages().first())
    }

    @Test
    fun `pin preserves insertion order FIFO`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")
        repo.pin("com.b")
        repo.pin("com.c")

        assertEquals(listOf("com.a", "com.b", "com.c"), repo.getPinnedPackages().first())
    }

    @Test
    fun `pin is idempotent for already pinned package`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")
        val secondCall = repo.pin("com.a")

        assertTrue("re-pin should still report success", secondCall)
        assertEquals(listOf("com.a"), repo.getPinnedPackages().first())
    }

    @Test
    fun `pin enforces max limit of 5`() = runTest {
        val repo = newRepo()
        repeat(5) { i -> repo.pin("com.app$i") }

        val rejected = repo.pin("com.overflow")

        assertFalse("6th pin must be rejected", rejected)
        assertEquals(5, repo.getPinnedPackages().first().size)
        assertFalse(repo.getPinnedPackages().first().contains("com.overflow"))
    }

    @Test
    fun `re-pinning existing package at limit does not fail`() = runTest {
        val repo = newRepo()
        repeat(5) { i -> repo.pin("com.app$i") }

        val result = repo.pin("com.app2")

        assertTrue("re-pinning existing package at limit must succeed", result)
        assertEquals(5, repo.getPinnedPackages().first().size)
    }

    @Test
    fun `unpin removes package`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")
        repo.pin("com.b")

        repo.unpin("com.a")

        assertEquals(listOf("com.b"), repo.getPinnedPackages().first())
    }

    @Test
    fun `unpin is idempotent for missing package`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")

        repo.unpin("com.missing")

        assertEquals(listOf("com.a"), repo.getPinnedPackages().first())
    }

    @Test
    fun `unpin preserves order of remaining packages`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")
        repo.pin("com.b")
        repo.pin("com.c")

        repo.unpin("com.b")

        assertEquals(listOf("com.a", "com.c"), repo.getPinnedPackages().first())
    }

    @Test
    fun `unpin opens slot for new pin`() = runTest {
        val repo = newRepo()
        repeat(5) { i -> repo.pin("com.app$i") }

        repo.unpin("com.app0")
        val newPin = repo.pin("com.fresh")

        assertTrue("after unpin slot should reopen", newPin)
        assertEquals(5, repo.getPinnedPackages().first().size)
        assertTrue(repo.getPinnedPackages().first().contains("com.fresh"))
    }

    @Test
    fun `isPinned reflects state`() = runTest {
        val repo = newRepo()
        repo.pin("com.a")

        assertTrue(repo.isPinned("com.a"))
        assertFalse(repo.isPinned("com.b"))

        repo.unpin("com.a")
        assertFalse(repo.isPinned("com.a"))
    }
}