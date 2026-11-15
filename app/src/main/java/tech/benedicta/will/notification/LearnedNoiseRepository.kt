package tech.benedicta.will.notification

/** A notification shape the user has taught Will to stop showing. */
data class SilencedNotification(
    val sourcePackage: String,
    val title: String,
    val dismissals: Int
)

/**
 * What Will has learned about noise from the user's own behaviour.
 *
 * The pattern list in [PatternIntentionClassifier] knows the housekeeping
 * notifications someone wrote down in advance. This knows the ones that only
 * this user, on this phone, keeps throwing away: a shop's weekly promo, an
 * app's "try premium", whatever a particular device decides to repeat.
 *
 * The signal is the user's existing behaviour, so there is nothing new to
 * teach and nothing extra to opt into: deleting a task without finishing it
 * says the notification was not worth a row, and finishing one says it was.
 * Nothing is stored but the app, the title and a count, nothing leaves the
 * device, and everything here is cleared by "delete all data".
 *
 * Deliberately slow to conclude and quick to forget: it takes
 * [DISMISSALS_BEFORE_SILENCED] dismissals to silence a title, and a single
 * completion unlearns it, because acting on one proves it mattered.
 */
interface LearnedNoiseRepository {

    suspend fun recordDismissed(sourcePackage: String, title: String)

    /** The user acted on this one, so whatever was learned about it is wrong. */
    suspend fun recordKept(sourcePackage: String, title: String)

    suspend fun isLearnedNoise(sourcePackage: String, title: String): Boolean

    /** Everything currently silenced, so the user can see and undo it. */
    suspend fun silenced(): List<SilencedNotification>

    suspend fun forget(sourcePackage: String, title: String)

    suspend fun forgetEverything()

    companion object {
        /**
         * Two could be coincidence. Three is a habit, and the cost of being
         * wrong is bounded: a single completion undoes it.
         */
        const val DISMISSALS_BEFORE_SILENCED = 3

        /**
         * The key a title is counted under. Folded so the same notification
         * counts as the same thing when the vendor changes case or spacing.
         */
        fun keyOf(sourcePackage: String, title: String): String? {
            val folded = title
                .lowercase()
                .map { ACCENTS[it] ?: it }
                .joinToString("")
                .split(" ", "\t", "\n")
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            if (folded.isEmpty()) return null
            return "$sourcePackage:$folded"
        }

        private val ACCENTS = mapOf(
            'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a',
            'é' to 'e', 'ê' to 'e',
            'í' to 'i',
            'ó' to 'o', 'õ' to 'o', 'ô' to 'o',
            'ú' to 'u',
            'ç' to 'c'
        )
    }
}
