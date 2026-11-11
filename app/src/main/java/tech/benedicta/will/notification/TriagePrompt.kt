package tech.benedicta.will.notification

/**
 * Asks the on-device model whether a notification is housekeeping, and reads
 * the answer.
 *
 * This is the question US-15 set out to answer and the pattern list only
 * partly can: patterns know the housekeeping someone wrote down in advance,
 * not the promo a particular shop invented last week.
 *
 * The reading is deliberately lopsided, because the costs are. A notification
 * wrongly kept is one row the user dismisses. A notification wrongly dropped
 * is a message they never see and the feed cannot tell them about. So only an
 * unambiguous verdict of noise drops anything: silence, prose, a hedge, two
 * verdicts at once, a refusal or an unreadable answer all keep.
 *
 * `IntentionPrompt` asks a different question and its NONE is not this
 * answer. A bank balance asks the user for nothing and is still not noise.
 */
object TriagePrompt {

    fun build(title: String, text: String): String? {
        val heading = title.trim()
        val body = text.trim()
        if (heading.isEmpty() && body.isEmpty()) return null

        return """
            A phone notification arrived.

            Title: $heading
            Body: $body

            Is this housekeeping the person does not need to see, like a
            download finishing, a sync completing, a battery or storage
            notice, or an advert? Or is it something from a person, or about
            the person's own money, deliveries, appointments or accounts?

            Answer with one word: NOISE if it is housekeeping or an advert,
            KEEP for anything else. If you are unsure, answer KEEP.
        """.trimIndent()
    }

    /**
     * True only when the answer *is* the verdict, not merely contains it.
     *
     * Containment is too loose here. `{"verdict":"noise"}` and "the
     * signal-to-noise ratio" both contain the word, and a model that answers
     * in a shape it was not asked for is a model to disregard, not to read
     * creatively. A label the model likes to prefix ("Answer: NOISE") is the
     * one allowance, because it is padding around the verdict rather than a
     * different kind of answer.
     */
    fun parse(answer: String): Boolean =
        answer.substringAfterLast(':')
            .trim()
            .trimEnd('.', ',', '!')
            .equals(NOISE, ignoreCase = true)

    private const val NOISE = "NOISE"
}
