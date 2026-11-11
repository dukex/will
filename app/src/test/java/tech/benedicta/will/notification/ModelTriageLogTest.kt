package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tech.benedicta.will.infrastructure.memory.MemoryModelTriageLog

/**
 * The contract that makes a model allowed to hide anything: whatever it hid
 * is listed, and the user can take it back.
 */
class ModelTriageLogTest {

    private val log = MemoryModelTriageLog()

    @Test
    fun `what the model hid is listed`() = runTest {
        log.recordDropped("com.shop", "Veja as novidades", at = 1L)

        assertEquals(
            listOf("Veja as novidades"),
            log.dropped().map { it.title }
        )
    }

    @Test
    fun `the same notification hidden twice is one entry, at the later time`() = runTest {
        log.recordDropped("com.shop", "Veja as novidades", at = 1L)
        log.recordDropped("com.shop", "Veja as novidades", at = 9L)

        assertEquals(1, log.dropped().size)
        assertEquals(9L, log.dropped().single().at)
    }

    @Test
    fun `the newest mistake is at the top, where the user will look`() = runTest {
        log.recordDropped("com.shop", "older", at = 1L)
        log.recordDropped("com.bank", "newer", at = 5L)

        assertEquals(listOf("newer", "older"), log.dropped().map { it.title })
    }

    @Test
    fun `showing one again takes it off the list`() = runTest {
        log.recordDropped("com.shop", "Seu pedido chegou", at = 1L)

        log.showAgain("com.shop", "Seu pedido chegou")

        assertTrue(log.dropped().isEmpty())
    }

    @Test
    fun `showing one again stops the model hiding it ever again`() = runTest {
        log.recordDropped("com.shop", "Seu pedido chegou", at = 1L)
        log.showAgain("com.shop", "Seu pedido chegou")

        assertTrue(log.isProtected("com.shop", "Seu pedido chegou"))
        log.recordDropped("com.shop", "Seu pedido chegou", at = 2L)
        assertTrue("an undo that only applied to the past is no undo", log.dropped().isEmpty())
    }

    @Test
    fun `protection is per app and per title, like everything else here`() = runTest {
        log.showAgain("com.shop", "Seu pedido chegou")

        assertFalse(log.isProtected("com.bank", "Seu pedido chegou"))
        assertFalse(log.isProtected("com.shop", "Veja as novidades"))
    }

    @Test
    fun `case, accents and spacing do not defeat the undo`() = runTest {
        log.showAgain("com.shop", "Atualização concluída")

        assertTrue(log.isProtected("com.shop", "  atualizacao   CONCLUIDA "))
    }

    @Test
    fun `nothing was hidden, so nothing is protected`() = runTest {
        assertFalse(log.isProtected("com.shop", "anything"))
        assertTrue(log.dropped().isEmpty())
    }

    @Test
    fun `a blank title is not something to list`() = runTest {
        log.recordDropped("com.shop", "   ", at = 1L)

        assertTrue(log.dropped().isEmpty())
    }

    @Test
    fun `the list stays bounded however long the phone runs`() = runTest {
        repeat(80) { index -> log.recordDropped("com.shop", "promo $index", at = index.toLong()) }

        assertEquals(ModelTriageLog.MAX_REMEMBERED, log.dropped().size)
    }

    @Test
    fun `the oldest entry is the one that falls off`() = runTest {
        repeat(ModelTriageLog.MAX_REMEMBERED + 1) { index ->
            log.recordDropped("com.shop", "promo $index", at = index.toLong())
        }

        assertFalse(log.dropped().any { it.title == "promo 0" })
        assertTrue(log.dropped().any { it.title == "promo 1" })
    }

    @Test
    fun `clearing data forgets the list and the undos with it`() = runTest {
        log.recordDropped("com.shop", "a", at = 1L)
        log.showAgain("com.bank", "b")

        log.forgetEverything()

        assertTrue(log.dropped().isEmpty())
        assertFalse(log.isProtected("com.bank", "b"))
    }
}
