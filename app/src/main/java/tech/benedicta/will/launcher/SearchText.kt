package tech.benedicta.will.launcher

/**
 * How Will compares what someone typed with what it has.
 *
 * Shared by the app drawer and the task search so one search box behaves the
 * same way over both: accents folded, whole words preferred, and a subsequence
 * of the letters accepted for the times people type half a word.
 */
internal object SearchText {

    fun fold(text: String): String =
        text.trim().lowercase().map { ACCENTS[it] ?: it }.joinToString("")

    /** Lower is a better match; null means no match. */
    fun score(haystack: String, needle: String): Int? {
        val folded = fold(haystack)
        return when {
            folded.startsWith(needle) -> STARTS_WITH
            folded.split(" ").any { it.startsWith(needle) } -> WORD_STARTS_WITH
            folded.contains(needle) -> CONTAINS
            folded.initials().startsWith(needle) -> INITIALS
            folded.hasSubsequence(needle) -> SUBSEQUENCE
            else -> null
        }
    }

    /**
     * Lower is better; null means no match. Unlike [score] this stops at a
     * literal match, because initials and a subsequence only make sense over
     * something short like a name. A paragraph contains almost any short
     * subsequence, so allowing it there would make every query match
     * everything.
     */
    fun scoreLiteral(haystack: String, needle: String): Int? {
        val folded = fold(haystack)
        return when {
            folded.startsWith(needle) -> STARTS_WITH
            folded.split(" ").any { it.startsWith(needle) } -> WORD_STARTS_WITH
            folded.contains(needle) -> CONTAINS
            else -> null
        }
    }

    const val STARTS_WITH = 0
    const val WORD_STARTS_WITH = 1
    const val CONTAINS = 2
    const val INITIALS = 3
    const val SUBSEQUENCE = 4
    const val CATEGORY = 5

    private fun String.initials(): String =
        split(" ").mapNotNull { it.firstOrNull() }.joinToString("")

    private fun String.hasSubsequence(needle: String): Boolean {
        var cursor = 0
        for (char in this) {
            if (cursor < needle.length && char == needle[cursor]) cursor++
        }
        return cursor == needle.length
    }

    private val ACCENTS = mapOf(
        'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a', 'ä' to 'a',
        'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
        'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
        'ó' to 'o', 'ò' to 'o', 'õ' to 'o', 'ô' to 'o', 'ö' to 'o',
        'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
        'ç' to 'c', 'ñ' to 'n'
    )
}
