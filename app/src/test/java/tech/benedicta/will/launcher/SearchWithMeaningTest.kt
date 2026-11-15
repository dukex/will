package tech.benedicta.will.launcher

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchWithMeaningTest {

    private val apps = listOf(
        LauncherApp("com.itau", "Itaú"),
        LauncherApp("com.nubank", "Nubank"),
        LauncherApp("com.spotify.music", "Spotify", AppCategory.AUDIO)
    )

    private fun searchThatAnswers(vararg packages: String) = SemanticAppSearch { _, offered ->
        packages.mapNotNull { pkg -> offered.firstOrNull { it.packageName == pkg } }
    }

    @Test
    fun `a name match never reaches the model`() = runTest {
        var asked = false
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch { _, _ ->
            asked = true
            emptyList()
        })

        val found = search.filter(apps, "spot")

        assertEquals(listOf("Spotify"), found.map { it.name })
        assertTrue("the fast answer was enough", !asked)
    }

    @Test
    fun `a word the names do not contain is answered by the model`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), searchThatAnswers("com.itau", "com.nubank"))

        val found = search.filter(apps, "banco")

        assertEquals(listOf("Itaú", "Nubank"), found.map { it.name })
    }

    @Test
    fun `the model's ranking is kept`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), searchThatAnswers("com.nubank", "com.itau"))

        assertEquals(listOf("Nubank", "Itaú"), search.filter(apps, "banco").map { it.name })
    }

    @Test
    fun `without the model the search behaves exactly as it did before`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch.Unavailable)

        assertEquals(listOf("Spotify"), search.filter(apps, "spot").map { it.name })
        assertTrue(search.filter(apps, "banco").isEmpty())
    }

    @Test
    fun `a model that answers with nothing leaves the search empty, not broken`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch { _, _ -> emptyList() })

        assertTrue(search.filter(apps, "banco").isEmpty())
    }

    @Test
    fun `a model that fails is treated as having no answer`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch { _, _ ->
            error("AICore went away")
        })

        assertTrue(search.filter(apps, "banco").isEmpty())
    }

    @Test
    fun `an empty query is still the whole list, and the model is not asked`() = runTest {
        var asked = false
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch { _, _ ->
            asked = true; emptyList()
        })

        assertEquals(apps.size, search.filter(apps, "").size)
        assertTrue(!asked)
    }

    @Test
    fun `the model can only name apps it was offered`() = runTest {
        val search = SearchWithMeaning(AppMatcher(), SemanticAppSearch { _, _ ->
            listOf(LauncherApp("com.not.installed", "Something else"))
        })

        assertTrue(
            "an app that is not installed is not a result",
            search.filter(apps, "banco").isEmpty()
        )
    }
}
