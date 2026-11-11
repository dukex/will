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
import tech.benedicta.will.notification.ModelTriageLog

/**
 * `ModelTriageLogTest` pins the rule against the in-memory twin. This runs
 * the same promises against the store that ships, because this is the one
 * standing between a model's guess and a message the user never sees.
 */
@RunWith(RobolectricTestRunner::class)
class SharedPrefsModelTriageLogTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun store() = SharedPrefsModelTriageLog(context)

    @Before
    fun startClean() = runTest { store().forgetEverything() }

    @Test
    fun `what the model hid outlives the process`() = runTest {
        store().recordDropped("com.shop", "Veja as novidades", at = 5L)

        val afterRestart = store().dropped()

        assertEquals(listOf("Veja as novidades"), afterRestart.map { it.title })
        assertEquals(5L, afterRestart.single().at)
    }

    @Test
    fun `an undo outlives the process too, or the user does it every week`() = runTest {
        store().recordDropped("com.shop", "Seu pedido chegou", at = 1L)

        store().showAgain("com.shop", "Seu pedido chegou")

        assertTrue(store().isProtected("com.shop", "Seu pedido chegou"))
        assertTrue(store().dropped().isEmpty())
    }

    @Test
    fun `a protected shape is never written back to the list`() = runTest {
        store().showAgain("com.shop", "Seu pedido chegou")

        store().recordDropped("com.shop", "Seu pedido chegou", at = 9L)

        assertTrue(store().dropped().isEmpty())
    }

    @Test
    fun `a title containing the separator survives the round trip`() = runTest {
        store().recordDropped("com.shop", "Promo | 50% off | hoje", at = 1L)

        assertEquals("Promo | 50% off | hoje", store().dropped().single().title)
    }

    @Test
    fun `the newest mistake is on top`() = runTest {
        store().recordDropped("com.shop", "older", at = 1L)
        store().recordDropped("com.bank", "newer", at = 7L)

        assertEquals(listOf("newer", "older"), store().dropped().map { it.title })
    }

    @Test
    fun `case, accents and spacing do not defeat the undo`() = runTest {
        store().showAgain("com.shop", "Atualização concluída")

        assertTrue(store().isProtected("com.shop", "  atualizacao   CONCLUIDA "))
    }

    @Test
    fun `the same notification hidden twice is one entry`() = runTest {
        store().recordDropped("com.shop", "Veja as novidades", at = 1L)
        store().recordDropped("com.shop", "Veja as novidades", at = 9L)

        assertEquals(1, store().dropped().size)
        assertEquals(9L, store().dropped().single().at)
    }

    @Test
    fun `the list stays bounded however long the phone runs`() = runTest {
        repeat(70) { index -> store().recordDropped("com.shop", "promo $index", at = index.toLong()) }

        assertTrue(
            "got ${store().dropped().size}",
            store().dropped().size <= ModelTriageLog.MAX_REMEMBERED
        )
    }

    @Test
    fun `clearing data forgets the list and the undos with it`() = runTest {
        store().recordDropped("com.shop", "a", at = 1L)
        store().showAgain("com.bank", "b")

        store().forgetEverything()

        assertTrue(store().dropped().isEmpty())
        assertFalse(store().isProtected("com.bank", "b"))
    }

    @Test
    fun `the switch is off until the user turns it on, and remembers`() = runTest {
        val preference = SharedPrefsModelTriagePreference(context)
        assertFalse("hiding anything is never a default", preference.enabledNow())

        preference.setEnabled(true)

        assertTrue(SharedPrefsModelTriagePreference(context).enabledNow())
        preference.setEnabled(false)
    }
}
