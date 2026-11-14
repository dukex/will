package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticSearchPromptTest {

    private val apps = listOf(
        LauncherApp("com.itau", "Itaú"),
        LauncherApp("com.nubank", "Nubank"),
        LauncherApp("com.spotify.music", "Spotify"),
        LauncherApp("com.whatsapp", "WhatsApp")
    )

    // --- asking ---

    @Test
    fun `the prompt carries the query and every app name`() {
        val prompt = SemanticSearchPrompt.build("banco", apps)!!

        assertTrue(prompt.contains("banco"))
        apps.forEach { assertTrue("missing ${it.name}", prompt.contains(it.name)) }
    }

    @Test
    fun `the apps are numbered, because the answer comes back as numbers`() {
        val prompt = SemanticSearchPrompt.build("banco", apps)!!

        assertTrue(prompt.contains("1. Itaú"))
        assertTrue(prompt.contains("4. WhatsApp"))
    }

    @Test
    fun `nothing to ask about builds no prompt`() {
        assertNull(SemanticSearchPrompt.build("", apps))
        assertNull(SemanticSearchPrompt.build("   ", apps))
        assertNull(SemanticSearchPrompt.build("banco", emptyList()))
    }

    @Test
    fun `the list is cut to what the context window holds`() {
        val many = (1..200).map { LauncherApp("com.app$it", "App $it") }

        val prompt = SemanticSearchPrompt.build("banco", many)!!

        assertTrue(prompt.contains("App ${SemanticSearchPrompt.MAX_APPS}"))
        assertTrue(
            "the list must stop at the cap",
            !prompt.contains("App ${SemanticSearchPrompt.MAX_APPS + 1}.")
        )
    }

    // --- reading the answer ---

    @Test
    fun `numbers come back as the apps they stand for`() {
        assertEquals(
            listOf("Itaú", "Nubank"),
            SemanticSearchPrompt.parse("1, 2", apps).map { it.name }
        )
    }

    @Test
    fun `the order the model gave is kept, because it is a ranking`() {
        assertEquals(
            listOf("Nubank", "Itaú"),
            SemanticSearchPrompt.parse("2, 1", apps).map { it.name }
        )
    }

    @Test
    fun `numbers on separate lines are read just as well`() {
        assertEquals(listOf("Itaú", "Spotify"), SemanticSearchPrompt.parse("1\n3", apps).map { it.name })
    }

    @Test
    fun `prose around the numbers is ignored`() {
        assertEquals(
            listOf("Itaú"),
            SemanticSearchPrompt.parse("I think the answer is 1, hope that helps", apps).map { it.name }
        )
    }

    @Test
    fun `a number nobody offered is dropped rather than crashing`() {
        assertEquals(listOf("Itaú"), SemanticSearchPrompt.parse("1, 99, 0, -3", apps).map { it.name })
    }

    @Test
    fun `the same app named twice appears once`() {
        assertEquals(listOf("Itaú"), SemanticSearchPrompt.parse("1, 1, 1", apps).map { it.name })
    }

    @Test
    fun `an answer with no numbers finds nothing`() {
        assertTrue(SemanticSearchPrompt.parse("none of these", apps).isEmpty())
        assertTrue(SemanticSearchPrompt.parse("", apps).isEmpty())
    }

    @Test
    fun `the answer is capped, so a model that lists everything is not obeyed`() {
        val many = (1..200).map { LauncherApp("com.app$it", "App $it") }
        val everything = (1..200).joinToString(",")

        assertTrue(
            SemanticSearchPrompt.parse(everything, many).size <= SemanticSearchPrompt.MAX_RESULTS
        )
    }

    @Test
    fun `the parse is bounded by the same list the prompt offered`() {
        val many = (1..200).map { LauncherApp("com.app$it", "App $it") }

        val chosen = SemanticSearchPrompt.parse("${SemanticSearchPrompt.MAX_APPS + 5}", many)

        assertTrue("a number past the offered list is not a choice", chosen.isEmpty())
    }
}
