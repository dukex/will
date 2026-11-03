package tech.benedicta.will.notification

/**
 * Decides whether a notification is something the user has to act on, or noise
 * the feed is better off without.
 *
 * The judgement is one sentence long, which is why it gets its own seam: the
 * default implementation answers it with patterns, and a later one could answer
 * it with an on-device model without anything else in the pipeline changing.
 *
 * `suspend` because an implementation may have to wait on inference. Callers
 * run it off the notification callback thread.
 */
fun interface IntentionClassifier {
    suspend fun isIntention(title: String, text: String, sourcePackage: String): Boolean
}

/**
 * Pattern-based triage, and the fallback for every device.
 *
 * Covers the housekeeping notifications the system and its apps post in
 * English and Portuguese: download progress, charging, connectivity, update
 * and alarm chatter. Everything else is kept.
 *
 * Biased toward keeping on purpose. A false keep costs the user one row they
 * dismiss; a false drop means a message they never see, which the feed cannot
 * tell them about. So a title that merely mentions a noise word stays, and only
 * a title that *is* the housekeeping message goes.
 */
class PatternIntentionClassifier(
    private val ignoredPackages: Set<String> = NotificationProcessor.IGNORED_PACKAGES
) : IntentionClassifier {

    override suspend fun isIntention(
        title: String,
        text: String,
        sourcePackage: String
    ): Boolean {
        if (sourcePackage in ignoredPackages) return false
        if (title.isBlank() && text.isBlank()) return false
        return NOISE_PATTERNS.none { it.containsMatchIn(title.foldAccents()) }
    }

    private companion object {
        /**
         * Accents vary between vendors and locales for the same message, so
         * matching happens on a folded copy and the patterns are written
         * without them.
         */
        private val ACCENTS = mapOf(
            'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a',
            'é' to 'e', 'ê' to 'e',
            'í' to 'i',
            'ó' to 'o', 'õ' to 'o', 'ô' to 'o',
            'ú' to 'u',
            'ç' to 'c'
        )

        fun String.foldAccents(): String =
            map { char -> ACCENTS[char.lowercaseChar()] ?: char }.joinToString("")

        private fun noise(pattern: String) = Regex(pattern, RegexOption.IGNORE_CASE)

        val NOISE_PATTERNS: List<Regex> = listOf(
            // English
            noise("^Downloading (\\d+ )?(video|file|image|photo)"),
            noise("^WhatsApp$"),
            noise("^Charging complete$"),
            noise("^Update complete$"),
            noise("^Could not connect to"),
            noise("has no internet access$"),
            noise("has limited connectivity$"),
            noise("^Upcoming alarm$"),
            noise("^Your bedtime starts at"),
            noise("^Alarm volume"),
            // Portuguese, matched against the accent-folded title
            noise("^Baixando (\\d+ )?(video|arquivo|imagem|foto)"),
            noise("^Carregamento concluido$"),
            noise("^Bateria carregada$"),
            noise("^Atualizacao concluida$"),
            noise("^Nao foi possivel conectar"),
            noise("sem acesso a internet$"),
            noise("conectividade limitada$"),
            noise("^Proximo alarme$"),
            noise("^Alarme proximo$"),
            noise("^Sua hora de dormir comeca"),
            noise("^Volume do alarme")
        )
    }
}
