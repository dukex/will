package tech.benedicta.will.launcher

/** A standard Android action an intention is asking for. */
enum class IntentionAction { DIAL, EMAIL, MAP, BROWSE }

/** The action and what it should act on. */
data class ResolvedAction(val action: IntentionAction, val argument: String)

/**
 * Reads an intention as a thing the system knows how to do, so tapping the
 * task lands on the screen rather than on an app's front door.
 *
 * `01-IDEA.md` asks for deep links: "pagar boleto" reaching the bank's scanner
 * rather than its home screen. The general case needs to know what each app's
 * screens are. This covers the part Android standardises, where the user's own
 * verb names the action: calling, writing, going somewhere, opening a link.
 * Those resolve to `ACTION_DIAL`, `ACTION_SENDTO`, a `geo:` link and
 * `ACTION_VIEW`, which every phone answers without Will knowing anything about
 * the apps installed on it.
 *
 * The verb has to start the intention. "a ligação com o Miguel caiu" is about
 * a call, not an instruction to make one, and opening the dialer there would
 * be wrong.
 *
 * Pure, with no Android dependency.
 */
class IntentionActionResolver {

    fun resolve(text: String): ResolvedAction? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        // A link is unambiguous wherever it sits, so it wins.
        LINK.find(trimmed)?.let { return ResolvedAction(IntentionAction.BROWSE, it.value) }

        val folded = trimmed.fold()
        for ((prefixes, action) in VERBS) {
            val prefix = prefixes.firstOrNull { folded.startsWith("$it ") } ?: continue
            val argument = trimmed.substring(prefix.length).trim().withoutTrailingParticle()
            if (argument.isEmpty()) return null
            return ResolvedAction(action, argument)
        }
        return null
    }

    /**
     * English puts the particle after the name: "call Sarah back" is about
     * Sarah, not about "Sarah back".
     */
    private fun String.withoutTrailingParticle(): String {
        val folded = fold()
        val particle = TRAILING_PARTICLES.firstOrNull { folded.endsWith(" $it") } ?: return this
        return dropLast(particle.length + 1).trim()
    }

    private fun String.fold(): String =
        lowercase().map { ACCENTS[it] ?: it }.joinToString("")

    private companion object {
        val LINK = Regex("""https?://\S+""")

        /**
         * Longest prefixes first, so "ligar de volta para" is not mistaken for
         * "ligar para" with a strange argument.
         */
        val VERBS: List<Pair<List<String>, IntentionAction>> = listOf(
            listOf(
                "ligar de volta para", "ligar de volta pra", "ligar para", "ligar pra",
                "call back", "call"
            ) to IntentionAction.DIAL,
            listOf(
                "mandar email para", "mandar e-mail para", "enviar email para",
                "send email to", "email"
            ) to IntentionAction.EMAIL,
            listOf(
                "ir para", "ir ate", "como chegar em", "navigate to", "directions to"
            ) to IntentionAction.MAP
        )

        val TRAILING_PARTICLES = listOf("back", "de volta")

        val ACCENTS = mapOf(
            'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a',
            'é' to 'e', 'ê' to 'e',
            'í' to 'i',
            'ó' to 'o', 'õ' to 'o', 'ô' to 'o',
            'ú' to 'u',
            'ç' to 'c'
        )
    }
}
