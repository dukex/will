package tech.benedicta.will.notification

/**
 * Judges a notification's words as housekeeping or not.
 *
 * The port for the on-device model. [Unavailable] keeps everything, which is
 * both the behaviour on a device with no model and the safe default: a
 * classifier that cannot think should not be hiding anything.
 */
fun interface SemanticNoiseReader {
    suspend fun isNoise(title: String, text: String): Boolean

    companion object {
        /** No model, so nothing is judged and nothing is hidden. */
        val Unavailable = SemanticNoiseReader { _, _ -> false }
    }
}
