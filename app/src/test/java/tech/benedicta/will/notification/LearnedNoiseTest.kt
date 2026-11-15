package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryLearnedNoiseRepository

class LearnedNoiseTest {

    private val learned = MemoryLearnedNoiseRepository()

    private suspend fun dismissTimes(times: Int, title: String, pkg: String = "com.shop") =
        repeat(times) { learned.recordDismissed(pkg, title) }

    @Test
    fun `something dismissed once is not noise yet`() = runTest {
        dismissTimes(1, "Veja as novidades")

        assertFalse(learned.isLearnedNoise("com.shop", "Veja as novidades"))
    }

    @Test
    fun `something dismissed twice is still not noise`() = runTest {
        dismissTimes(2, "Veja as novidades")

        assertFalse("two could be coincidence", learned.isLearnedNoise("com.shop", "Veja as novidades"))
    }

    @Test
    fun `something dismissed three times is noise from then on`() = runTest {
        dismissTimes(3, "Veja as novidades")

        assertTrue(learned.isLearnedNoise("com.shop", "Veja as novidades"))
    }

    @Test
    fun `the lesson is per app, not global`() = runTest {
        dismissTimes(3, "Veja as novidades", pkg = "com.shop")

        assertTrue(learned.isLearnedNoise("com.shop", "Veja as novidades"))
        assertFalse(learned.isLearnedNoise("com.bank", "Veja as novidades"))
    }

    @Test
    fun `the lesson is per title, not per app alone`() = runTest {
        dismissTimes(3, "Veja as novidades")

        assertFalse(
            "silencing one promo must not silence the whole app",
            learned.isLearnedNoise("com.shop", "Seu pedido chegou")
        )
    }

    @Test
    fun `titles are matched regardless of case, accents and spacing`() = runTest {
        dismissTimes(3, "Atualização concluída")

        assertTrue(learned.isLearnedNoise("com.shop", "atualizacao concluida"))
        assertTrue(learned.isLearnedNoise("com.shop", "  ATUALIZAÇÃO   CONCLUÍDA  "))
    }

    @Test
    fun `finishing one of these unlearns it, because it mattered after all`() = runTest {
        dismissTimes(3, "Seu pedido chegou")
        assertTrue(learned.isLearnedNoise("com.shop", "Seu pedido chegou"))

        learned.recordKept("com.shop", "Seu pedido chegou")

        assertFalse("you acted on it, so it is not noise", learned.isLearnedNoise("com.shop", "Seu pedido chegou"))
    }

    @Test
    fun `keeping it resets the count rather than merely pausing it`() = runTest {
        dismissTimes(3, "Seu pedido chegou")
        learned.recordKept("com.shop", "Seu pedido chegou")

        dismissTimes(2, "Seu pedido chegou")

        assertFalse("the count started over", learned.isLearnedNoise("com.shop", "Seu pedido chegou"))
    }

    @Test
    fun `nothing is learned about a title that was never dismissed`() = runTest {
        assertFalse(learned.isLearnedNoise("com.whatsapp", "Maria"))
    }

    @Test
    fun `a blank title teaches nothing`() = runTest {
        dismissTimes(5, "   ")

        assertFalse(learned.isLearnedNoise("com.shop", "   "))
    }

    @Test
    fun `what was learned can be listed, so the user can see it`() = runTest {
        dismissTimes(3, "Veja as novidades")
        dismissTimes(3, "Experimente o premium")
        dismissTimes(1, "Seu pedido chegou")

        val silenced = learned.silenced()

        assertEquals(2, silenced.size)
        assertTrue(silenced.all { it.sourcePackage == "com.shop" })
    }

    @Test
    fun `everything learned can be forgotten at once`() = runTest {
        dismissTimes(3, "Veja as novidades")

        learned.forgetEverything()

        assertFalse(learned.isLearnedNoise("com.shop", "Veja as novidades"))
        assertTrue(learned.silenced().isEmpty())
    }

    @Test
    fun `one lesson can be forgotten on its own`() = runTest {
        dismissTimes(3, "Veja as novidades")
        dismissTimes(3, "Experimente o premium")

        learned.forget("com.shop", "Veja as novidades")

        assertFalse(learned.isLearnedNoise("com.shop", "Veja as novidades"))
        assertTrue(learned.isLearnedNoise("com.shop", "Experimente o premium"))
    }
}
