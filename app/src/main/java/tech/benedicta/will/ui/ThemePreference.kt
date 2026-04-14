package tech.benedicta.will.ui

import android.content.Context

private const val PREFS_NAME = "will_prefs"
private const val KEY_THEME = "theme_mode"

fun saveThemeMode(context: Context, mode: ThemeMode) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_THEME, mode.ordinal)
        .apply()
}

fun loadThemeMode(context: Context): ThemeMode {
    val ordinal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_THEME, ThemeMode.SYSTEM.ordinal)
    return ThemeMode.fromOrdinal(ordinal)
}
