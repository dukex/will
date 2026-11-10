package tech.benedicta.will.launcher

/**
 * Matches a search query against installed app names.
 *
 * Replaces a plain `contains(query, ignoreCase = true)`, which could not find
 * "Itaú" from `itau` or "Configurações" from `configuracoes`: a real daily
 * failure on a phone used in Portuguese, because the accented letter is a
 * different character and case-insensitivity does nothing about it.
 *
 * Beyond folding accents it accepts the two things people actually type when
 * they are in a hurry: the initials of a multi-word name, and a subsequence of
 * its letters. Results are ranked so the obvious match comes first.
 *
 * Pure, with no Android dependency.
 */
class AppMatcher {

    fun filter(apps: List<LauncherApp>, query: String): List<LauncherApp> {
        val needle = query.fold()
        if (needle.isEmpty()) return apps

        return apps
            .mapIndexedNotNull { position, app ->
                score(app, needle)?.let { Ranked(app, it, position) }
            }
            .sortedWith(compareBy({ it.score }, { it.position }))
            .map { it.app }
    }

    private data class Ranked(val app: LauncherApp, val score: Int, val position: Int)

    /** Lower is better; null means no match at all. */
    private fun score(app: LauncherApp, needle: String): Int? {
        val name = app.name.fold()
        return when {
            name.startsWith(needle) -> STARTS_WITH
            name.wordStartsWith(needle) -> WORD_STARTS_WITH
            name.contains(needle) -> CONTAINS
            name.initials().startsWith(needle) -> INITIALS
            name.hasSubsequence(needle) -> SUBSEQUENCE
            app.category.matches(needle) -> CATEGORY
            else -> null
        }
    }

    private fun String.wordStartsWith(needle: String): Boolean =
        split(" ").any { it.startsWith(needle) }

    private fun String.initials(): String =
        split(" ").mapNotNull { it.firstOrNull() }.joinToString("")

    /** Every letter of [needle] appears in order, not necessarily together. */
    private fun String.hasSubsequence(needle: String): Boolean {
        var cursor = 0
        for (char in this) {
            if (cursor < needle.length && char == needle[cursor]) cursor++
        }
        return cursor == needle.length
    }

    /**
     * Whether a word names this category, in Portuguese or English. The words
     * are Android's categories, so this is searching by what an app is for
     * without anyone guessing: there is no category for money, so "banco"
     * finds nothing here.
     */
    private fun AppCategory.matches(needle: String): Boolean =
        CATEGORY_WORDS[this]?.any { it.startsWith(needle) } == true

    private fun String.fold(): String {
        val lowered = trim().lowercase()
        return lowered
            .map { char -> ACCENTS[char] ?: char }
            .joinToString("")
    }

    private companion object {
        const val STARTS_WITH = 0
        const val WORD_STARTS_WITH = 1
        const val CONTAINS = 2
        const val INITIALS = 3
        const val SUBSEQUENCE = 4
        const val CATEGORY = 5

        val CATEGORY_WORDS: Map<AppCategory, List<String>> = mapOf(
            AppCategory.GAME to listOf("jogo", "jogos", "game", "games"),
            AppCategory.AUDIO to listOf("musica", "musicas", "audio", "music", "podcast"),
            AppCategory.VIDEO to listOf("video", "videos", "filme", "filmes", "movie"),
            AppCategory.IMAGE to listOf("foto", "fotos", "imagem", "photo", "photos", "image"),
            AppCategory.SOCIAL to listOf("social", "rede social", "redes"),
            AppCategory.NEWS to listOf("noticia", "noticias", "news", "jornal"),
            AppCategory.MAPS to listOf("mapa", "mapas", "map", "maps", "navegacao"),
            AppCategory.PRODUCTIVITY to listOf("trabalho", "produtividade", "work", "productivity"),
            AppCategory.ACCESSIBILITY to listOf("acessibilidade", "accessibility")
        )

        val ACCENTS = mapOf(
            'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a', 'ä' to 'a',
            'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
            'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
            'ó' to 'o', 'ò' to 'o', 'õ' to 'o', 'ô' to 'o', 'ö' to 'o',
            'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
            'ç' to 'c', 'ñ' to 'n'
        )
    }
}
