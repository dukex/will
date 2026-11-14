package tech.benedicta.will.notification

/**
 * Reads a notification's words as the thing it is asking for.
 *
 * The port for the on-device model. [Unavailable] is what every device gets
 * when the model is off, absent or not yet downloaded, and it is also the
 * default, so forgetting to wire a model leaves the old behaviour rather than
 * a crash.
 */
fun interface SemanticIntentionReader {
    suspend fun read(title: String, text: String): ReadIntention?

    companion object {
        /** No model, so nothing is derived and the title arrives as it was. */
        val Unavailable = SemanticIntentionReader { _, _ -> null }
    }
}
