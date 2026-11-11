package tech.benedicta.will.infrastructure

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.benedicta.will.notification.LearnedNoiseRepository

/**
 * `LearnedNoiseTest` pins the rule against the in-memory twin. This runs the
 * same promises against the store that actually ships, because that is the one
 * with a serialisation format, a cap and an eviction rule to get wrong.
 *
 * The two it adds on top are the ones only a real store can be asked:
 * whether a lesson survives the app being restarted, and whether the cap
 * holds without throwing away the titles the user dismissed most.
 */
@RunWith(RobolectricTestRunner::class)
class SharedPrefsLearnedNoiseRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun store() = SharedPrefsLearnedNoiseRepository(context)

    @Before
    fun startClean() = runTest { store().forgetEverything() }

    private suspend fun LearnedNoiseRepository.dismiss(
        times: Int,
        title: String,
        pkg: String = "com.shop"
    ) = repeat(times) { recordDismissed(pkg, title) }

    @Test
    fun `three dismissals silence a title, two do not`() = runTest {
        val learned = store()

        learned.dismiss(2, "Veja as novidades")
        assertFalse(learned.isLearnedNoise("com.shop", "Veja as novidades"))

        learned.dismiss(1, "Veja as novidades")
        assertTrue(learned.isLearnedNoise("com.shop", "Veja as novidades"))
    }

    @Test
    fun `a lesson survives the app being restarted`() = runTest {
        store().dismiss(3, "Experimente o premium")

        val afterRestart = store()

        assertTrue(
            "the point of persisting it is that it outlives the process",
            afterRestart.isLearnedNoise("com.shop", "Experimente o premium")
        )
        assertEquals(1, afterRestart.silenced().size)
    }

    @Test
    fun `finishing one unlearns it, and that survives a restart too`() = runTest {
        store().dismiss(3, "Seu pedido chegou")

        store().recordKept("com.shop", "Seu pedido chegou")

        assertFalse(store().isLearnedNoise("com.shop", "Seu pedido chegou"))
    }

    @Test
    fun `case, accents and spacing do not split one lesson into two`() = runTest {
        val learned = store()

        learned.recordDismissed("com.shop", "Atualização concluída")
        learned.recordDismissed("com.shop", "atualizacao concluida")
        learned.recordDismissed("com.shop", "  ATUALIZAÇÃO   CONCLUÍDA  ")

        assertTrue(learned.isLearnedNoise("com.shop", "Atualização concluída"))
        assertEquals("one entry, counted three times", 1, learned.silenced().size)
    }

    @Test
    fun `the title shown back is the one that arrived, not the folded key`() = runTest {
        val learned = store()

        learned.dismiss(3, "Atualização concluída")

        assertEquals("Atualização concluída", learned.silenced().single().title)
    }

    @Test
    fun `a title containing a pipe survives the round trip`() = runTest {
        val learned = store()

        learned.dismiss(3, "Promo | 50% off | hoje")

        assertTrue(learned.isLearnedNoise("com.shop", "Promo | 50% off | hoje"))
        assertEquals(
            "the separator must not eat the title",
            "Promo | 50% off | hoje",
            learned.silenced().single().title
        )
    }

    @Test
    fun `the lesson is per app and per title, never per app alone`() = runTest {
        val learned = store()

        learned.dismiss(3, "Veja as novidades", pkg = "com.shop")

        assertTrue(learned.isLearnedNoise("com.shop", "Veja as novidades"))
        assertFalse(learned.isLearnedNoise("com.bank", "Veja as novidades"))
        assertFalse(learned.isLearnedNoise("com.shop", "Seu pedido chegou"))
    }

    @Test
    fun `a blank title teaches nothing and stores nothing`() = runTest {
        val learned = store()

        learned.dismiss(5, "   ")

        assertFalse(learned.isLearnedNoise("com.shop", "   "))
        assertTrue(learned.silenced().isEmpty())
    }

    @Test
    fun `everything learned can be forgotten, and one lesson on its own`() = runTest {
        val learned = store()
        learned.dismiss(3, "Veja as novidades")
        learned.dismiss(3, "Experimente o premium")

        learned.forget("com.shop", "Veja as novidades")

        assertFalse(learned.isLearnedNoise("com.shop", "Veja as novidades"))
        assertTrue(learned.isLearnedNoise("com.shop", "Experimente o premium"))

        learned.forgetEverything()
        assertTrue(store().silenced().isEmpty())
    }

    @Test
    fun `the store stays bounded however long the phone runs`() = runTest {
        val learned = store()

        repeat(260) { index -> learned.recordDismissed("com.shop", "promo number $index") }

        assertTrue(
            "the cap is what stops this growing without bound, got ${countStored()}",
            countStored() <= 200
        )
    }

    @Test
    fun `filling the store keeps what the user dismissed most`() = runTest {
        val learned = store()
        learned.dismiss(5, "the one that really is noise")

        repeat(260) { index -> learned.recordDismissed("com.shop", "one off number $index") }

        assertTrue(
            "a title dismissed five times must outlive single-dismissal filler",
            learned.isLearnedNoise("com.shop", "the one that really is noise")
        )
    }

    private fun countStored() =
        context.getSharedPreferences("learned_noise", Context.MODE_PRIVATE).all.size
}
