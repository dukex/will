package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryLearnedNoiseRepository

class LearnedIntentionClassifierTest {

    private val learned = MemoryLearnedNoiseRepository()
    private val classifier = LearnedIntentionClassifier(PatternIntentionClassifier(), learned)

    private suspend fun judge(title: String, pkg: String = "com.shop") =
        classifier.isIntention(title = title, text = "", sourcePackage = pkg)

    @Test
    fun `a title the patterns already know is still noise`() = runTest {
        assertFalse(judge("Charging complete", pkg = "com.android.systemui"))
    }

    @Test
    fun `a title nobody has objected to is kept`() = runTest {
        assertTrue(judge("Seu pedido chegou"))
    }

    @Test
    fun `a title the user keeps throwing away stops appearing`() = runTest {
        repeat(3) { learned.recordDismissed("com.shop", "Veja as novidades") }

        assertFalse(judge("Veja as novidades"))
    }

    @Test
    fun `two dismissals are not enough to silence anything`() = runTest {
        repeat(2) { learned.recordDismissed("com.shop", "Veja as novidades") }

        assertTrue(judge("Veja as novidades"))
    }

    @Test
    fun `acting on one brings it back`() = runTest {
        repeat(3) { learned.recordDismissed("com.shop", "Seu pedido chegou") }
        assertFalse(judge("Seu pedido chegou"))

        learned.recordKept("com.shop", "Seu pedido chegou")

        assertTrue(judge("Seu pedido chegou"))
    }

    @Test
    fun `what was learned about one app does not silence another`() = runTest {
        repeat(3) { learned.recordDismissed("com.shop", "Veja as novidades") }

        assertTrue(judge("Veja as novidades", pkg = "com.bank"))
    }

    @Test
    fun `a message from a person is never silenced by what was learned elsewhere`() = runTest {
        repeat(5) { learned.recordDismissed("com.shop", "Veja as novidades") }

        assertTrue(judge("Maria", pkg = "com.whatsapp"))
    }
}
