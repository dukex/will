package tech.benedicta.will.notification

/**
 * Triage with the on-device model behind the cheap rules.
 *
 * This is what US-15 set out to build, and the order is the whole design:
 *
 * 1. [cheap] runs first: the patterns, then what the user taught Will by
 *    dismissing things. Both are free, both are more trustworthy than a
 *    guess, and most notifications never get past them.
 * 2. A shape the user has already rescued once is never judged again. An
 *    undo that only applied to the past would have them undoing the same
 *    notification every week.
 * 3. Only then is the model asked, and only when the user has turned it on.
 *
 * Everything that can go wrong keeps the notification: the switch is off, the
 * device has no model, it times out, it errors, it hedges, it answers in a
 * shape [TriagePrompt] will not read. A false keep costs one dismissable row.
 * A false drop costs a message nobody ever sees.
 *
 * Every drop is written to [log] before it takes effect, so there is no state
 * in which the model has hidden something the user cannot find. That is the
 * condition CLAUDE.md puts on hiding anything, and it is the reason this was
 * worth building rather than the model being the reason.
 */
class ModelIntentionClassifier(
    private val cheap: IntentionClassifier,
    private val model: SemanticNoiseReader,
    private val log: ModelTriageLog,
    private val enabled: () -> Boolean,
    private val now: () -> Long = System::currentTimeMillis
) : IntentionClassifier {

    override suspend fun isIntention(
        title: String,
        text: String,
        sourcePackage: String
    ): Boolean {
        if (!cheap.isIntention(title, text, sourcePackage)) return false
        if (!enabled()) return true
        if (log.isProtected(sourcePackage, title)) return true

        val noise = runCatching { model.isNoise(title, text) }.getOrDefault(false)
        if (!noise) return true

        log.recordDropped(sourcePackage, title, now())
        return false
    }
}
