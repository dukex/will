package tech.benedicta.will.ui

enum class ThemeMode {
    SYSTEM, DARK, LIGHT;

    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode =
            entries.getOrElse(ordinal) { SYSTEM }
    }
}
