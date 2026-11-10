package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppMentionResolverTest {

    private val resolver = AppMentionResolver()

    private val installed = listOf(
        LauncherApp("com.whatsapp", "WhatsApp"),
        LauncherApp("com.google.android.calendar", "Calendar"),
        LauncherApp("com.itau", "Itaú"),
        LauncherApp("com.slack", "Slack"),
        LauncherApp("com.google.android.gm", "Gmail"),
        LauncherApp("com.spotify.music", "Spotify"),
        LauncherApp("com.google.android.apps.maps", "Maps"),
        LauncherApp("tech.benedicta.will", "Will"),
        LauncherApp("com.x", "X")
    )

    private fun resolve(text: String) = resolver.resolve(text, installed)

    @Test
    fun `an app named in the text is the target`() {
        assertEquals("com.whatsapp", resolve("mandar o contrato no WhatsApp")?.packageName)
    }

    @Test
    fun `the app name is matched regardless of case`() {
        assertEquals("com.whatsapp", resolve("mandar no whatsapp")?.packageName)
        assertEquals("com.slack", resolve("responder a Sarah no SLACK")?.packageName)
    }

    @Test
    fun `accents do not have to match`() {
        assertEquals("com.itau", resolve("pagar o boleto no itau")?.packageName)
        assertEquals("com.itau", resolve("pagar o boleto no Itaú")?.packageName)
    }

    @Test
    fun `a sentence with no app in it resolves to nothing`() {
        assertNull(resolve("pagar o boleto da luz"))
        assertNull(resolve("renew the passport"))
    }

    @Test
    fun `an empty or blank intention resolves to nothing`() {
        assertNull(resolve(""))
        assertNull(resolve("   "))
    }

    @Test
    fun `the app name has to be a whole word, not a fragment`() {
        assertNull("maps inside 'mapsa' is not a mention", resolve("comprar mapsa no mercado"))
        assertNull("x inside 'xerox' is not a mention", resolve("fazer xerox do documento"))
    }

    @Test
    fun `a very short app name is ignored, because it collides with ordinary words`() {
        assertNull("a one-letter app name is too weak a signal", resolve("mandar o x para Ana"))
    }

    @Test
    fun `the longest matching name wins`() {
        val apps = listOf(
            LauncherApp("com.google.android.apps.messaging", "Messages"),
            LauncherApp("com.google.android.apps.dynamite", "Google Chat")
        )

        assertEquals(
            "com.google.android.apps.dynamite",
            resolver.resolve("responder no Google Chat", apps)?.packageName
        )
    }

    @Test
    fun `a multi-word app name is matched as a phrase`() {
        val apps = listOf(LauncherApp("com.google.android.apps.docs", "Google Drive"))

        assertEquals(
            "com.google.android.apps.docs",
            resolver.resolve("subir o arquivo no Google Drive", apps)?.packageName
        )
        assertNull(
            "the words have to appear together",
            resolver.resolve("subir no Google e depois no Drive do trabalho", apps)
        )
    }

    @Test
    fun `punctuation around the app name does not hide it`() {
        assertEquals("com.slack", resolve("avisar no Slack: reuniao movida")?.packageName)
        assertEquals("com.whatsapp", resolve("(whatsapp) confirmar com a Quezia")?.packageName)
    }

    @Test
    fun `the launcher itself is never the target of a task`() {
        assertNull("a task that opens Will is a loop", resolve("anotar no Will"))
    }

    @Test
    fun `nothing installed resolves to nothing`() {
        assertNull(resolver.resolve("mandar no WhatsApp", emptyList()))
    }

    @Test
    fun `the first app mentioned wins when two are named`() {
        assertEquals(
            "com.whatsapp",
            resolve("mandar no WhatsApp e depois avisar no Slack")?.packageName
        )
    }
}
