package tech.benedicta.will.ui

import android.content.Context

private const val PREFS_NAME = "will_prefs"
private const val KEY_THEME = "theme_mode"

fun loadThemeMode(context: Context): ThemeMode {
    val ordinal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_THEME, ThemeMode.SYSTEM.ordinal)
    return ThemeMode.fromOrdinal(ordinal)
}
