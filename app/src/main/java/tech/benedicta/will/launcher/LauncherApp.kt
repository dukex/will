package tech.benedicta.will.launcher

/**
 * What the system says an app is for. Android attaches this to the app itself,
 * so searching by it is searching by meaning without anyone guessing.
 *
 * The list is Android's, not ours, which is also its limit: there is no
 * category for money, so "banco" will not find a bank.
 */
enum class AppCategory {
    GAME, AUDIO, VIDEO, IMAGE, SOCIAL, NEWS, MAPS, PRODUCTIVITY, ACCESSIBILITY, UNKNOWN
}

data class LauncherApp(
    val packageName: String,
    val name: String,
    val category: AppCategory = AppCategory.UNKNOWN
)
