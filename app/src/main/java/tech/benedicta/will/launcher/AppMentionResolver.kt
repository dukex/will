package tech.benedicta.will.launcher

/**
 * Finds the app an intention is about, by reading the name the user wrote.
 *
 * "mandar o contrato no WhatsApp" is about WhatsApp, so the task should open
 * WhatsApp when tapped instead of going nowhere. Before this, a typed task had
 * no target at all and the tap did nothing.
 *
 * Deliberately literal. It resolves a name the user actually typed; it does not
 * guess that "pagar boleto" means a bank, because being wrong here sends the
 * user into the wrong app. When it is not sure it returns null and the task
 * stays untargeted, which is exactly what used to happen to every typed task.
 *
 * The resolution is visible before it is used: the feed shows the app on the
 * task's metadata line, so the user sees where the tap will go.
 */
class AppMentionResolver(
    private val launcherPackage: String = "tech.benedicta.will"
) {

    fun resolve(text: String, apps: List<LauncherApp>): LauncherApp? {
        val haystack = text.normalise()
        if (haystack.isBlank()) return null

        return apps
            .asSequence()
            .filter { it.packageName != launcherPackage }
            .mapNotNull { app ->
                val needle = app.name.normalise()
                if (needle.length < MINIMUM_NAME_LENGTH) return@mapNotNull null
                val at = haystack.indexOfWord(needle) ?: return@mapNotNull null
                Match(app, position = at, length = needle.length)
            }
            // Earliest mention first, and on a tie the longer name, so "Google
            // Chat" beats a bare "Chat" that starts at the same place.
            .sortedWith(compareBy({ it.position }, { -it.length }))
            .firstOrNull()
            ?.app
    }

    private data class Match(val app: LauncherApp, val position: Int, val length: Int)

    /**
     * Where [needle] appears as a whole word, or null. Both sides are already
     * normalised to single-spaced alphanumerics, so a word boundary is a space
     * or an end of the string.
     */
    private fun String.indexOfWord(needle: String): Int? {
        var from = 0
        while (true) {
            val at = indexOf(needle, from)
            if (at < 0) return null
            val before = at == 0 || this[at - 1] == ' '
            val afterIndex = at + needle.length
            val after = afterIndex == length || this[afterIndex] == ' '
            if (before && after) return at
            from = at + 1
        }
    }

    private fun String.normalise(): String {
        val folded = map { char -> ACCENTS[char.lowercaseChar()] ?: char.lowercaseChar() }
        val kept = folded.map { if (it.isLetterOrDigit() || it.isWhitespace()) it else ' ' }
        return kept.joinToString("").split(" ", "\t", "\n").filter { it.isNotEmpty() }
            .joinToString(" ")
    }

    private companion object {
        /**
         * One- and two-letter app names collide with ordinary words too often
         * to be a reliable signal of what a sentence is about.
         */
        const val MINIMUM_NAME_LENGTH = 3

        val ACCENTS = mapOf(
            'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a', 'ä' to 'a',
            'é' to 'e', 'ê' to 'e', 'ë' to 'e',
            'í' to 'i', 'î' to 'i',
            'ó' to 'o', 'õ' to 'o', 'ô' to 'o', 'ö' to 'o',
            'ú' to 'u', 'û' to 'u', 'ü' to 'u',
            'ç' to 'c', 'ñ' to 'n'
        )
    }
}
