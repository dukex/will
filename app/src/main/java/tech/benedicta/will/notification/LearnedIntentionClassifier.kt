package tech.benedicta.will.notification

/**
 * Triage that also knows what this particular user keeps throwing away.
 *
 * [patterns] holds the housekeeping notifications someone wrote down in
 * advance; [learned] holds the ones only this phone produces. A notification
 * has to survive both to reach the feed.
 *
 * The order matters for cost, not for the answer: the patterns are a few
 * regexes in memory, the learned check touches storage, so the cheap one runs
 * first and most notifications never reach the second.
 */
class LearnedIntentionClassifier(
    private val patterns: IntentionClassifier,
    private val learned: LearnedNoiseRepository
) : IntentionClassifier {

    override suspend fun isIntention(
        title: String,
        text: String,
        sourcePackage: String
    ): Boolean {
        if (!patterns.isIntention(title, text, sourcePackage)) return false
        return !learned.isLearnedNoise(sourcePackage, title)
    }
}
