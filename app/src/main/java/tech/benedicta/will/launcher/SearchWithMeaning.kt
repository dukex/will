package tech.benedicta.will.launcher

/**
 * The drawer's search: exact first, meaning second.
 *
 * [AppMatcher] answers instantly, on every device, for the way people usually
 * search, which is by some part of the name. Only when that finds nothing does
 * the question go to [semantic], because "banco" is the kind of word no amount
 * of string matching will ever connect to "Itaú".
 *
 * The model is never the only thing between the user and their apps. It cannot
 * remove a result the matcher found, it cannot name an app that is not
 * installed, and if it is off, unavailable or broken the search behaves exactly
 * as it did before it existed.
 */
class SearchWithMeaning(
    private val matcher: AppMatcher,
    private val semantic: SemanticAppSearch
) {

    suspend fun filter(apps: List<LauncherApp>, query: String): List<LauncherApp> {
        val byName = matcher.filter(apps, query)
        if (byName.isNotEmpty() || query.isBlank()) return byName

        val installed = apps.associateBy { it.packageName }
        return runCatching { semantic.rank(query, apps) }
            .getOrDefault(emptyList())
            .mapNotNull { installed[it.packageName] }
    }
}
