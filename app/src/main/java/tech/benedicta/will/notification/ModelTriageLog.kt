package tech.benedicta.will.notification

/** A notification the model judged housekeeping, kept so the user can see it. */
data class ModelDrop(val sourcePackage: String, val title: String, val at: Long)

/**
 * What the on-device model has hidden, and the user's way back.
 *
 * CLAUDE.md sets this as the price of hiding anything: "What Will learns to
 * hide has to be visible and undoable." US-20 pays it for the noise the user
 * taught Will by dismissing things. A model's verdict is a guess rather than
 * something the user did, so it owes the same debt at least.
 *
 * [showAgain] does two things on purpose. It takes the entry off the list,
 * and it protects that shape so the model is never asked about it again. An
 * undo that only applied to the past would leave the user undoing the same
 * notification every week.
 */
interface ModelTriageLog {

    suspend fun recordDropped(sourcePackage: String, title: String, at: Long)

    /** Everything the model has hidden, newest first. */
    suspend fun dropped(): List<ModelDrop>

    /** The user disagreed: take it off the list and never drop this shape again. */
    suspend fun showAgain(sourcePackage: String, title: String)

    /** Whether the user has already said this shape is not noise. */
    suspend fun isProtected(sourcePackage: String, title: String): Boolean

    suspend fun forgetEverything()

    companion object {
        /**
         * Enough to show a week of mistakes, few enough that the list stays
         * readable and the store stays small.
         */
        const val MAX_REMEMBERED = 50
    }
}
