package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntentionActionResolverTest {

    private val resolver = IntentionActionResolver()

    private fun resolve(text: String) = resolver.resolve(text)

    // --- calling someone ---

    @Test
    fun `ligar para alguem abre o discador com o nome`() {
        val action = resolve("ligar para Miguel")

        assertEquals(IntentionAction.DIAL, action?.action)
        assertEquals("Miguel", action?.argument)
    }

    @Test
    fun `ligar de volta para alguem tambem abre o discador`() {
        assertEquals(IntentionAction.DIAL, resolve("ligar de volta para Miguel")?.action)
    }

    @Test
    fun `calling in English resolves the same way`() {
        val action = resolve("call Sarah back")

        assertEquals(IntentionAction.DIAL, action?.action)
        assertEquals("Sarah", action?.argument)
    }

    @Test
    fun `a phone number is carried through instead of a name`() {
        assertEquals("+5531999887766", resolve("ligar para +5531999887766")?.argument)
    }

    // --- writing to someone ---

    @Test
    fun `mandar email para alguem abre o cliente de email`() {
        val action = resolve("mandar email para ana@exemplo.com")

        assertEquals(IntentionAction.EMAIL, action?.action)
        assertEquals("ana@exemplo.com", action?.argument)
    }

    @Test
    fun `emailing in English resolves the same way`() {
        assertEquals(IntentionAction.EMAIL, resolve("email ana@exemplo.com")?.action)
    }

    // --- going somewhere ---

    @Test
    fun `ir para um lugar abre o mapa`() {
        val action = resolve("ir para Rua das Flores 120")

        assertEquals(IntentionAction.MAP, action?.action)
        assertEquals("Rua das Flores 120", action?.argument)
    }

    @Test
    fun `navigating in English resolves the same way`() {
        assertEquals(IntentionAction.MAP, resolve("navigate to Pocrane")?.action)
    }

    // --- opening a link ---

    @Test
    fun `a link in the intention is opened directly`() {
        val action = resolve("ler https://example.com/contrato")

        assertEquals(IntentionAction.BROWSE, action?.action)
        assertEquals("https://example.com/contrato", action?.argument)
    }

    // --- everything else stays as it was ---

    @Test
    fun `an intention with no verb we know resolves to nothing`() {
        assertNull(resolve("pagar o boleto da luz"))
        assertNull(resolve("revisar o contrato"))
    }

    @Test
    fun `a verb with nothing after it resolves to nothing`() {
        assertNull(resolve("ligar para"))
        assertNull(resolve("call"))
    }

    @Test
    fun `an empty intention resolves to nothing`() {
        assertNull(resolve(""))
        assertNull(resolve("   "))
    }

    @Test
    fun `the verb has to start the intention, not merely appear in it`() {
        assertNull(
            "this is about a call, not an instruction to make one",
            resolve("a ligação com o Miguel caiu")
        )
    }

    @Test
    fun `case and accents do not change the verb`() {
        assertEquals(IntentionAction.DIAL, resolve("LIGAR PARA Miguel")?.action)
        assertEquals(IntentionAction.MAP, resolve("Ir Para Pocrane")?.action)
    }

    @Test
    fun `a link anywhere in the intention wins over a plain verb`() {
        val action = resolve("ler o contrato em https://example.com/doc")

        assertEquals(IntentionAction.BROWSE, action?.action)
    }
}
