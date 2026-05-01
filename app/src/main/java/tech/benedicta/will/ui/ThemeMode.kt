package tech.benedicta.will.ui

enum class ThemeMode {
    SYSTEM, DARK, LIGHT;

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode =
            entries.getOrElse(ordinal) { SYSTEM }
    }
}
