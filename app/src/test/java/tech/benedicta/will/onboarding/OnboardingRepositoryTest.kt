package tech.benedicta.will.onboarding

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryOnboardingRepository

class OnboardingRepositoryTest {

    @Test
    fun `default state is not completed`() = runTest {
        val repo = MemoryOnboardingRepository()

        assertFalse(repo.isCompleted().first())
    }

    @Test
    fun `markCompleted flips the flag`() = runTest {
        val repo = MemoryOnboardingRepository()

        repo.markCompleted()

        assertTrue(repo.isCompleted().first())
    }

    @Test
    fun `reset clears the flag so onboarding can replay`() = runTest {
        val repo = MemoryOnboardingRepository(initial = true)

        repo.reset()

        assertFalse(repo.isCompleted().first())
    }

    @Test
    fun `markCompleted is idempotent`() = runTest {
        val repo = MemoryOnboardingRepository()

        repo.markCompleted()
        repo.markCompleted()

        assertTrue(repo.isCompleted().first())
    }
}
