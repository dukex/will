package tech.benedicta.will.launcher

/**
 * Finds apps by what the user meant rather than by how the name is spelled.
 *
 * `AppMatcher` answers "is this word in the name"; this answers "which of
 * these apps is a bank". The two are complementary, which is why this only
 * runs when the matcher found nothing: the fast, exact, always-available
 * answer first, and the model only for the question it alone can answer.
 */
fun interface SemanticAppSearch {
    suspend fun rank(query: String, apps: List<LauncherApp>): List<LauncherApp>

    companion object {
        /** What every device gets when the model is off or unavailable. */
        val Unavailable = SemanticAppSearch { _, _ -> emptyList() }
    }
}
