package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppMatcherTest {

    private val matcher = AppMatcher()

    private val installed = listOf(
        LauncherApp("com.whatsapp", "WhatsApp"),
        LauncherApp("com.itau", "Itaú"),
        LauncherApp("com.google.android.apps.docs", "Google Drive"),
        LauncherApp("com.google.android.calendar", "Calendar"),
        LauncherApp("com.spotify.music", "Spotify"),
        LauncherApp("com.nubank", "Nubank"),
        LauncherApp("org.telegram.messenger", "Telegram"),
        LauncherApp("com.android.settings", "Configurações")
    )

    private fun search(query: String) = matcher.filter(installed, query).map { it.name }

    @Test
    fun `an empty query leaves the list as it is`() {
        assertEquals(installed.map { it.name }, search(""))
        assertEquals(installed.map { it.name }, search("   "))
    }

    @Test
    fun `typing without accents finds the accented app`() {
        assertTrue("itau must find Itaú", search("itau").contains("Itaú"))
        assertTrue("configuracoes must find Configurações", search("configuracoes").contains("Configurações"))
    }

    @Test
    fun `typing with accents still finds it`() {
        assertTrue(search("itaú").contains("Itaú"))
    }

    @Test
    fun `a prefix finds the app`() {
        assertEquals(listOf("Spotify"), search("spot"))
    }

    @Test
    fun `a match in the middle of the name counts`() {
        assertTrue(search("gram").contains("Telegram"))
    }

    @Test
    fun `initials find a multi-word app`() {
        assertTrue("gd must find Google Drive", search("gd").contains("Google Drive"))
    }

    @Test
    fun `a subsequence of the letters finds the app`() {
        assertTrue("wapp must find WhatsApp", search("wapp").contains("WhatsApp"))
        assertTrue("tlgrm must find Telegram", search("tlgrm").contains("Telegram"))
    }

    @Test
    fun `a query that matches nothing returns nothing`() {
        assertTrue(search("zzzz").isEmpty())
    }

    @Test
    fun `case is ignored both ways`() {
        assertTrue(search("WHATSAPP").contains("WhatsApp"))
        assertTrue(search("whatsapp").contains("WhatsApp"))
    }

    @Test
    fun `a name that starts with the query ranks above one that merely contains it`() {
        val apps = listOf(
            LauncherApp("com.b", "Banco do Brasil"),
            LauncherApp("com.n", "Nubank")
        )

        assertEquals(listOf("Banco do Brasil", "Nubank"), matcher.filter(apps, "ban").map { it.name })
    }

    @Test
    fun `a whole-word match ranks above a scattered subsequence`() {
        val apps = listOf(
            LauncherApp("com.scattered", "Sala de Leitura"),
            LauncherApp("com.exact", "Slack")
        )

        assertEquals("Slack", matcher.filter(apps, "slack").first().name)
    }

    @Test
    fun `ties keep the original order, which is alphabetical from the repository`() {
        val apps = listOf(
            LauncherApp("com.a", "Mapa"),
            LauncherApp("com.b", "Mapas")
        )

        assertEquals(listOf("Mapa", "Mapas"), matcher.filter(apps, "mapa").map { it.name })
    }

    @Test
    fun `whitespace around the query is ignored`() {
        assertEquals(listOf("Spotify"), search("  spot  "))
    }

    @Test
    fun `a query longer than every name matches nothing rather than failing`() {
        assertTrue(search("a".repeat(200)).isEmpty())
    }

    @Test
    fun `the subsequence has to be in order`() {
        assertTrue("ppaw is whatsapp backwards-ish and must not match", search("ppaw").isEmpty())
    }


    // --- searching by what an app is for, not what it is called ---

    private val categorised = listOf(
        LauncherApp("com.spotify.music", "Spotify", AppCategory.AUDIO),
        LauncherApp("com.google.android.apps.maps", "Maps", AppCategory.MAPS),
        LauncherApp("com.instagram.android", "Instagram", AppCategory.SOCIAL),
        LauncherApp("com.notion", "Notion", AppCategory.PRODUCTIVITY),
        LauncherApp("com.itau", "Itaú", AppCategory.UNKNOWN)
    )

    @Test
    fun `a word for a category finds the apps in it`() {
        assertEquals(
            listOf("Spotify"),
            matcher.filter(categorised, "musica").map { it.name }
        )
    }

    @Test
    fun `the category words work in English too`() {
        assertEquals(listOf("Spotify"), matcher.filter(categorised, "music").map { it.name })
        assertEquals(listOf("Instagram"), matcher.filter(categorised, "social").map { it.name })
    }

    @Test
    fun `a name match still beats a category match`() {
        val apps = listOf(
            LauncherApp("com.b", "Mapas Brasil", AppCategory.UNKNOWN),
            LauncherApp("com.a", "Waze", AppCategory.MAPS)
        )

        assertEquals("Mapas Brasil", matcher.filter(apps, "mapas").first().name)
    }

    @Test
    fun `an app with no category is not found by a category word`() {
        assertTrue(matcher.filter(categorised, "jogo").isEmpty())
    }

    @Test
    fun `a category word nobody uses matches nothing rather than everything`() {
        assertTrue(matcher.filter(categorised, "banco").isEmpty())
    }
}
